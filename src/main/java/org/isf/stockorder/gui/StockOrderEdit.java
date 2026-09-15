/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.stockorder.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.menu.manager.Context;
import org.isf.stockorder.manager.StockOrderBrowserManager;
import org.isf.stockorder.model.StockOrder;
import org.isf.stockorder.model.StockOrderRow;
import org.isf.stockorder.model.StockOrderStatus;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add/edit dialog for a {@link StockOrder} ("fiche de commande"): a reference, a date, an optional
 * supplier and charge type, and a table of ordered medicals with their quantity.
 */
public class StockOrderEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(StockOrderEdit.class);
	private static final int CODE_COLUMN_WIDTH = 100;

	private final StockOrderBrowserManager stockOrderBrowserManager = Context.getApplicationContext().getBean(StockOrderBrowserManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final MedicalDsrStockMovementTypeBrowserManager medicalDsrStockMovementTypeBrowserManager = Context.getApplicationContext()
					.getBean(MedicalDsrStockMovementTypeBrowserManager.class);
	private final SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);

	private StockOrder order;
	private boolean insert;
	private boolean saved;
	private Map<String, Medical> medicalMap;

	private JTextField referenceField;
	private GoodDateChooser dateChooser;
	private JComboBox<Supplier> supplierComboBox;
	private JComboBox<MovementType> chargeTypeComboBox;
	private JTextField searchField;
	private JTable rowsTable;
	private RowsModel rowsModel;

	private final String[] columnNames = {
					MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
					MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
					MessageBundle.getMessage("angal.common.qty.txt").toUpperCase()
	};

	public StockOrderEdit(JFrame owner, StockOrder order) {
		super(owner, true);
		this.insert = order == null;
		this.order = insert ? new StockOrder() : order;
		initialize();
	}

	public boolean isSaved() {
		return saved;
	}

	private void initialize() {
		loadMedicals();
		setTitle(insert
						? MessageBundle.getMessage("angal.stockorder.neworder.title")
						: MessageBundle.getMessage("angal.stockorder.editorder.title"));
		setContentPane(getContentPane(getOwner()));
		setMinimumSize(new Dimension(650, 450));
		pack();
		setLocationRelativeTo(getOwner());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void loadMedicals() {
		medicalMap = new HashMap<>();
		try {
			for (Medical med : medicalBrowsingManager.getMedicals().stream().filter(m -> m.getDeleted() == 'N').toList()) {
				String key = med.getProdCode();
				if (key == null || key.isEmpty()) {
					key = med.getType().getCode() + med.getDescription();
				}
				medicalMap.put(key, med);
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private java.awt.Container getContentPane(java.awt.Window owner) {
		JPanel contentPane = new JPanel(new BorderLayout(5, 5));
		contentPane.add(getHeaderPanel(), BorderLayout.NORTH);
		contentPane.add(getTablePanel(), BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		return contentPane;
	}

	private JPanel getHeaderPanel() {
		JPanel headerPanel = new JPanel(new GridBagLayout());
		headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0;
		gbc.gridy = 0;
		headerPanel.add(new javax.swing.JLabel(MessageBundle.getMessage("angal.stockorder.reference.label") + ':'), gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		headerPanel.add(getReferenceField(), gbc);
		gbc.fill = GridBagConstraints.NONE;

		gbc.gridx = 2;
		headerPanel.add(new javax.swing.JLabel(MessageBundle.getMessage("angal.common.date.txt") + ':'), gbc);
		gbc.gridx = 3;
		headerPanel.add(getDateChooser(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		headerPanel.add(new javax.swing.JLabel(MessageBundle.getMessage("angal.stockorder.supplier.label") + ':'), gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		headerPanel.add(getSupplierComboBox(), gbc);
		gbc.fill = GridBagConstraints.NONE;

		gbc.gridx = 2;
		headerPanel.add(new javax.swing.JLabel(MessageBundle.getMessage("angal.stockorder.chargetype.label") + ':'), gbc);
		gbc.gridx = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		headerPanel.add(getChargeTypeComboBox(), gbc);
		gbc.fill = GridBagConstraints.NONE;

		if (insert) {
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 4;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			headerPanel.add(getSearchField(), gbc);
		}

		return headerPanel;
	}

	private JTextField getReferenceField() {
		referenceField = new JTextField(15);
		if (insert) {
			try {
				referenceField.setText(stockOrderBrowserManager.generateReferenceNumber(TimeTools.getNow()));
			} catch (OHServiceException e) {
				LOGGER.error("Unable to generate an automatic reference number", e);
			}
		} else {
			referenceField.setText(order.getRefNo());
			referenceField.setEditable(false);
		}
		return referenceField;
	}

	private GoodDateChooser getDateChooser() {
		LocalDate date = insert || order.getOrderDate() == null ? LocalDate.now() : order.getOrderDate().toLocalDate();
		dateChooser = new GoodDateChooser(date, true, false);
		dateChooser.setEnabled(insert);
		return dateChooser;
	}

	private JComboBox<Supplier> getSupplierComboBox() {
		supplierComboBox = new JComboBox<>();
		supplierComboBox.addItem(null);
		try {
			List<Supplier> suppliers = supplierBrowserManager.getList();
			suppliers.sort(new Supplier.SupplierNameComparator());
			for (Supplier supplier : suppliers) {
				supplierComboBox.addItem(supplier);
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (!insert) {
			supplierComboBox.setSelectedItem(order.getSupplier());
		}
		return supplierComboBox;
	}

	private JComboBox<MovementType> getChargeTypeComboBox() {
		chargeTypeComboBox = new JComboBox<>();
		chargeTypeComboBox.addItem(null);
		try {
			for (MovementType type : medicalDsrStockMovementTypeBrowserManager.getMedicalDsrStockMovementType()) {
				if (type.getType().contains("+")) {
					chargeTypeComboBox.addItem(type);
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (!insert) {
			chargeTypeComboBox.setSelectedItem(order.getChargeType());
			chargeTypeComboBox.setEnabled(false);
		}
		return chargeTypeComboBox;
	}

	private JTextField getSearchField() {
		searchField = new JTextField();
		TextPrompt suggestion = new TextPrompt(
						MessageBundle.getMessage("angal.medicalstock.typeacodeoradescriptionandpressenter"),
						searchField,
						Show.FOCUS_LOST);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		suggestion.setForeground(Color.GRAY);
		suggestion.setHorizontalAlignment(SwingConstants.CENTER);
		suggestion.changeAlpha(0.5f);
		suggestion.changeStyle(Font.BOLD + Font.ITALIC);

		searchField.addActionListener(actionEvent -> {
			String text = searchField.getText();
			Medical medical = medicalMap.containsKey(text) ? medicalMap.get(text) : chooseMedical(text.toLowerCase());
			if (medical == null) {
				return;
			}
			int qty = askQuantity(medical);
			if (qty <= 0) {
				return;
			}
			rowsModel.addRow(new StockOrderRow(medical, qty));
			searchField.setText("");
			searchField.requestFocus();
		});
		return searchField;
	}

	private Medical chooseMedical(String text) {
		List<Medical> candidates = new ArrayList<>();
		for (Medical med : medicalMap.values()) {
			if (NormalizeString.normalizeContains(med.getDescription().toLowerCase(), text)) {
				candidates.add(med);
			}
		}
		Collections.sort(candidates);
		if (candidates.isEmpty()) {
			return null;
		}

		JTable medicalTable = new JTable(new PickerModel(candidates));
		medicalTable.getColumnModel().getColumn(0).setMaxWidth(CODE_COLUMN_WIDTH);
		medicalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel panel = new JPanel();
		panel.add(new JScrollPane(medicalTable));

		int ok = JOptionPane.showConfirmDialog(this, panel,
						MessageBundle.getMessage("angal.medicalstock.multiplecharging.chooseamedical"),
						JOptionPane.OK_CANCEL_OPTION);
		if (ok == JOptionPane.OK_OPTION) {
			int row = medicalTable.getSelectedRow();
			if (row >= 0) {
				return candidates.get(row);
			}
		}
		return null;
	}

	private int askQuantity(Medical medical) {
		String title = MessageBundle.getMessage("angal.common.quantity.txt");
		do {
			String value = JOptionPane.showInputDialog(this, medical.toString(), title, JOptionPane.QUESTION_MESSAGE);
			if (value == null) {
				return 0;
			}
			try {
				int qty = Integer.parseInt(value.trim());
				if (qty <= 0) {
					throw new NumberFormatException();
				}
				return qty;
			} catch (NumberFormatException e) {
				MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
			}
		} while (true);
	}

	private JPanel getTablePanel() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		rowsModel = new RowsModel(insert ? new ArrayList<>() : new ArrayList<>(order.getRows()));
		rowsTable = new JTable(rowsModel);
		rowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowsTable.getColumnModel().getColumn(0).setMaxWidth(CODE_COLUMN_WIDTH);
		tablePanel.add(new JScrollPane(rowsTable), BorderLayout.CENTER);

		if (insert) {
			JButton removeRowButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			removeRowButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			removeRowButton.addActionListener(actionEvent -> {
				int row = rowsTable.getSelectedRow();
				if (row < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				rowsModel.removeRowAt(row);
			});
			JPanel south = new JPanel();
			south.add(removeRowButton);
			tablePanel.add(south, BorderLayout.SOUTH);
		}
		return tablePanel;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();

		JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(actionEvent -> save());
		buttonPanel.add(saveButton);

		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.addActionListener(actionEvent -> dispose());
		buttonPanel.add(cancelButton);

		return buttonPanel;
	}

	private void save() {
		if (rowsTable.isEditing()) {
			rowsTable.getCellEditor().stopCellEditing();
		}

		String reference = referenceField.getText().trim();
		if (reference.isEmpty()) {
			MessageDialog.error(this, "angal.inventory.mustenterareference.msg");
			return;
		}
		if (rowsModel.getRowsList().isEmpty()) {
			MessageDialog.error(this, "angal.stockorder.cannotsaveorderwithoutproducts.msg");
			return;
		}

		order.setRefNo(reference);
		LocalDateTime date = dateChooser.getDateStartOfDay();
		order.setOrderDate(date != null ? date : TimeTools.getNow());
		order.setSupplier((Supplier) supplierComboBox.getSelectedItem());
		order.setChargeType((MovementType) chargeTypeComboBox.getSelectedItem());
		if (insert) {
			order.setStatus(StockOrderStatus.open);
		}
		order.setRows(rowsModel.getRowsList());

		try {
			if (insert) {
				stockOrderBrowserManager.newOrder(order);
			} else {
				stockOrderBrowserManager.updateOrder(order);
			}
			saved = true;
			dispose();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private class RowsModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private final List<StockOrderRow> rows;

		RowsModel(List<StockOrderRow> rows) {
			this.rows = rows;
		}

		List<StockOrderRow> getRowsList() {
			return rows;
		}

		void addRow(StockOrderRow row) {
			rows.add(row);
			fireTableDataChanged();
		}

		void removeRowAt(int index) {
			rows.remove(index);
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return rows == null ? 0 : rows.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 2;
		}

		@Override
		public Object getValueAt(int row, int column) {
			StockOrderRow orderRow = rows.get(row);
			return switch (column) {
				case 0 -> orderRow.getMedical() != null ? orderRow.getMedical().getProdCode() : "";
				case 1 -> orderRow.getMedical() != null ? orderRow.getMedical().getDescription() : "";
				case 2 -> orderRow.getQuantity();
				default -> null;
			};
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 2) {
				try {
					int qty = Integer.parseInt(value.toString().trim());
					if (qty > 0) {
						rows.get(row).setQuantity(qty);
					}
				} catch (NumberFormatException e) {
					// ignore invalid edits, keep the previous value
				}
			}
			fireTableCellUpdated(row, column);
		}
	}

	private static class PickerModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private final List<Medical> medicals;

		PickerModel(List<Medical> medicals) {
			this.medicals = medicals;
		}

		@Override
		public int getRowCount() {
			return medicals == null ? 0 : medicals.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return column == 0
							? MessageBundle.getMessage("angal.common.code.txt").toUpperCase()
							: MessageBundle.getMessage("angal.common.description.txt").toUpperCase();
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Medical medical = medicals.get(row);
			return column == 0 ? medical.getProdCode() : medical.getDescription();
		}
	}
}
