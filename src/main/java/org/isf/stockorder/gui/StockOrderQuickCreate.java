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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quick order creation screen: lists every medical that is out of stock or at/under its critical level
 * and not already part of an open order, lets the user set the quantity (and, optionally, the supplier)
 * to order for each one, then creates one separate {@link StockOrder} per medical for which a quantity
 * greater than zero was entered.
 */
public class StockOrderQuickCreate extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(StockOrderQuickCreate.class);

	private final StockOrderBrowserManager stockOrderBrowserManager = Context.getApplicationContext().getBean(StockOrderBrowserManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final MedicalDsrStockMovementTypeBrowserManager medicalDsrStockMovementTypeBrowserManager = Context.getApplicationContext()
					.getBean(MedicalDsrStockMovementTypeBrowserManager.class);
	private final SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);

	private boolean saved;
	private List<Supplier> suppliers;
	private JComboBox<Supplier> globalSupplierComboBox;
	private JComboBox<MovementType> chargeTypeComboBox;
	private GoodDateChooser dateChooser;
	private JTable table;
	private RowsModel model;
	private final List<QuickOrderRow> rows = new ArrayList<>();

	private final String[] columnNames = {
					MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
					MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
					MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
					MessageBundle.getMessage("angal.medicals.stock.col").toUpperCase(),
					MessageBundle.getMessage("angal.medicals.critlevel.col").toUpperCase(),
					MessageBundle.getMessage("angal.stockorder.qtytoorder.col").toUpperCase(),
					MessageBundle.getMessage("angal.stockorder.supplier.label").toUpperCase()
	};

	public StockOrderQuickCreate(JFrame owner) {
		super(owner, true);
		loadRows();
		setTitle(MessageBundle.getMessage("angal.stockorder.neworder.title"));
		setContentPane(buildContentPane());
		setMinimumSize(new Dimension(800, 500));
		pack();
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	public boolean isSaved() {
		return saved;
	}

	private void loadRows() {
		rows.clear();
		try {
			Set<Integer> alreadyOrdered = new HashSet<>(stockOrderBrowserManager.getMedicalCodesInOpenOrders());
			for (Medical medical : medicalBrowsingManager.getMedicals()) {
				if (medical.getDeleted() == 'Y' || alreadyOrdered.contains(medical.getCode())) {
					continue;
				}
				double stock = medical.getTotalQuantity();
				double critical = medical.getMinqty();
				boolean outOfStock = stock == 0;
				boolean underCritical = critical != 0 && stock <= critical;
				if (outOfStock || underCritical) {
					rows.add(new QuickOrderRow(medical, stock, critical));
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private JPanel buildContentPane() {
		JPanel contentPane = new JPanel(new BorderLayout(5, 5));
		contentPane.add(getHeaderPanel(), BorderLayout.NORTH);
		contentPane.add(getTablePanel(), BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		return contentPane;
	}

	private JPanel getHeaderPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.date.txt") + ':'), gbc);
		gbc.gridx = 1;
		dateChooser = new GoodDateChooser(java.time.LocalDate.now(), true, false);
		panel.add(dateChooser, gbc);

		gbc.gridx = 2;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stockorder.chargetype.label") + ':'), gbc);
		gbc.gridx = 3;
		panel.add(getChargeTypeComboBox(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stockorder.supplierforall.label") + ':'), gbc);
		gbc.gridx = 1;
		panel.add(getGlobalSupplierComboBox(), gbc);

		gbc.gridx = 2;
		JButton applyToAllButton = new JButton(MessageBundle.getMessage("angal.stockorder.applytoall.btn"));
		applyToAllButton.addActionListener(actionEvent -> {
			Supplier supplier = (Supplier) globalSupplierComboBox.getSelectedItem();
			for (QuickOrderRow row : rows) {
				row.supplier = supplier;
			}
			model.fireTableDataChanged();
		});
		panel.add(applyToAllButton, gbc);

		return panel;
	}

	private JComboBox<MovementType> getChargeTypeComboBox() {
		chargeTypeComboBox = new JComboBox<>();
		try {
			for (MovementType type : medicalDsrStockMovementTypeBrowserManager.getMedicalDsrStockMovementType()) {
				if (type.getType().contains("+")) {
					chargeTypeComboBox.addItem(type);
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		return chargeTypeComboBox;
	}

	private JComboBox<Supplier> getGlobalSupplierComboBox() {
		globalSupplierComboBox = new JComboBox<>(getSuppliersComboModel());
		return globalSupplierComboBox;
	}

	private Supplier[] getSuppliersComboModel() {
		try {
			suppliers = supplierBrowserManager.getList();
			suppliers.sort(new Supplier.SupplierNameComparator());
		} catch (OHServiceException e) {
			suppliers = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
		Supplier[] items = new Supplier[suppliers.size() + 1];
		items[0] = null;
		for (int i = 0; i < suppliers.size(); i++) {
			items[i + 1] = suppliers.get(i);
		}
		return items;
	}

	private JPanel getTablePanel() {
		model = new RowsModel();
		table = new JTable(model);
		table.setRowHeight(24);

		JComboBox<Supplier> supplierEditorCombo = new JComboBox<>(getSuppliersComboModel());
		TableColumn supplierColumn = table.getColumnModel().getColumn(6);
		supplierColumn.setCellEditor(new DefaultCellEditor(supplierEditorCombo));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
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
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}

		MovementType chargeType = (MovementType) chargeTypeComboBox.getSelectedItem();
		if (chargeType == null) {
			MessageDialog.error(this, "angal.stockorder.pleasechooseachargetype.msg");
			return;
		}

		List<QuickOrderRow> toOrder = rows.stream().filter(row -> row.quantity > 0).toList();
		if (toOrder.isEmpty()) {
			MessageDialog.error(this, "angal.stockorder.pleaseenteraquantityforatleastonemedical.msg");
			return;
		}

		Supplier globalSupplier = (Supplier) globalSupplierComboBox.getSelectedItem();
		for (QuickOrderRow row : toOrder) {
			if (row.supplier == null && globalSupplier == null) {
				MessageDialog.error(this, "angal.stockorder.pleasechooseasupplierforeverymedical.fmt.msg", row.medical.getDescription());
				return;
			}
		}

		LocalDateTime date = dateChooser.getDateStartOfDay();
		if (date == null) {
			date = TimeTools.getNow();
		}

		int created = 0;
		try {
			for (QuickOrderRow row : toOrder) {
				StockOrder order = new StockOrder();
				order.setRefNo(stockOrderBrowserManager.generateReferenceNumber(date));
				order.setOrderDate(date);
				order.setSupplier(row.supplier != null ? row.supplier : globalSupplier);
				order.setChargeType(chargeType);
				order.setStatus(StockOrderStatus.open);
				order.setRows(List.of(new StockOrderRow(row.medical, row.quantity)));
				stockOrderBrowserManager.newOrder(order);
				created++;
			}
			saved = true;
			MessageDialog.info(this, "angal.stockorder.ordercreated.success.fmt.msg", created);
			dispose();
		} catch (OHServiceException e) {
			LOGGER.error("Failed after creating {} order(s)", created, e);
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private static class QuickOrderRow {

		private final Medical medical;
		private final double stock;
		private final double critical;
		private int quantity;
		private Supplier supplier;

		QuickOrderRow(Medical medical, double stock, double critical) {
			this.medical = medical;
			this.stock = stock;
			this.critical = critical;
		}
	}

	private class RowsModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

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
			return column == 5 || column == 6;
		}

		@Override
		public Object getValueAt(int rowIndex, int column) {
			QuickOrderRow row = rows.get(rowIndex);
			return switch (column) {
				case 0 -> row.medical.getType() != null ? row.medical.getType().getDescription() : "";
				case 1 -> row.medical.getProdCode();
				case 2 -> row.medical.getDescription();
				case 3 -> row.stock;
				case 4 -> row.critical;
				case 5 -> row.quantity;
				case 6 -> row.supplier;
				default -> null;
			};
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int column) {
			QuickOrderRow row = rows.get(rowIndex);
			if (column == 5) {
				try {
					int qty = Integer.parseInt(value.toString().trim());
					row.quantity = Math.max(qty, 0);
				} catch (NumberFormatException e) {
					// ignore invalid edits, keep the previous value
				}
			} else if (column == 6) {
				row.supplier = (Supplier) value;
			}
			fireTableCellUpdated(rowIndex, column);
		}
	}
}
