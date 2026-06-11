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
package org.isf.command.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.isf.command.gui.CommandEdit.CommandListener;
import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.command.model.CommandRow;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class NewCommandDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private EventListenerList commandListeners = new EventListenerList();

	public void addCommandListener(CommandListener l) {
		commandListeners.add(CommandListener.class, l);
	}

	public void removeCommandListener(CommandListener listener) {
		commandListeners.remove(CommandListener.class, listener);
	}

	private void fireCommandInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = commandListeners.getListeners(CommandListener.class);
		for (EventListener listener : listeners) {
			((CommandListener) listener).commandInserted(event);
		}
	}

	private void fireCommandUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = commandListeners.getListeners(CommandListener.class);
		for (EventListener listener : listeners) {
			((CommandListener) listener).commandUpdated(event);
		}
	}

	private final CommandBrowserManager commandBrowserManager = Context.getApplicationContext().getBean(CommandBrowserManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	private JPanel jContentPane;
	private JPanel topPanel;
	private JPanel tablePanel;
	private JPanel buttonPanel;
	private JTextField refNoTextField;
	private GoodDateChooser dateChooser;
	private JTable productTable;
	private ProductTableModel productModel;
	private JButton okButton;
	private JButton cancelButton;
	private final JFrame parentFrame;
	private final Command command;
	private final boolean insert;
	private final boolean addRowsMode;
	private List<ProductRow> allProductRows = new ArrayList<>();
	private List<ProductRow> productRows = new ArrayList<>();
	private JTextField productSearchField;

	public NewCommandDialog(JFrame parent, Command cmd, boolean inserting) {
		super(parent, true);
		parentFrame = parent;
		this.command = cmd;
		this.insert = inserting;
		this.addRowsMode = cmd.getId() != null && cmd.getId() > 0;
		initialize();
	}

	private void initialize() {
		setContentPane(getJContentPane());
		if (addRowsMode) {
			setTitle(MessageBundle.getMessage("angal.command.addrowstocommand.title"));
		} else if (insert) {
			setTitle(MessageBundle.getMessage("angal.command.newcommand.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.command.editcommand.title"));
		}
		pack();
		setSize(new Dimension(900, 550));
		setLocationRelativeTo(null);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			jContentPane.add(getTopPanel(), BorderLayout.NORTH);
			jContentPane.add(getTablePanel(), BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(0, 0, 5, 5);

			JLabel refNoLabel = new JLabel(MessageBundle.getMessage("angal.command.refno.col"));
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			topPanel.add(refNoLabel, gbc);

			gbc.gridx = 1;
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			topPanel.add(getRefNoTextField(), gbc);

			JLabel dateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			gbc.gridx = 2;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 15, 5, 5);
			topPanel.add(dateLabel, gbc);

			gbc.gridx = 3;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 5, 0);
			topPanel.add(getDateChooser(), gbc);
		}
		return topPanel;
	}

	private JPanel getTablePanel() {
		if (tablePanel == null) {
			tablePanel = new JPanel(new BorderLayout());
			tablePanel.setBorder(new EmptyBorder(5, 0, 5, 0));

			JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			searchPanel.add(new JLabel(MessageBundle.getMessage("angal.command.search.product.label")));
			searchPanel.add(getProductSearchField());
			tablePanel.add(searchPanel, BorderLayout.NORTH);

			tablePanel.add(new JScrollPane(getProductTable()), BorderLayout.CENTER);
		}
		return tablePanel;
	}

	private JTextField getProductSearchField() {
		if (productSearchField == null) {
			productSearchField = new VoLimitedTextField(50, 20);
			productSearchField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					filterProducts();
				}
			});
		}
		return productSearchField;
	}

	private void filterProducts() {
		String text = productSearchField.getText().trim().toLowerCase();
		if (text.isEmpty()) {
			productRows = new ArrayList<>(allProductRows);
		} else {
			productRows = allProductRows.stream()
					.filter(pr -> pr.medical.getProdCode().toLowerCase().contains(text)
							|| pr.medical.getDescription().toLowerCase().contains(text))
					.collect(Collectors.toList());
		}
		productModel.fireTableDataChanged();
		productTable.updateUI();
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			buttonPanel.add(getOkButton());
			buttonPanel.add(getCancelButton());
		}
		return buttonPanel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(this::validateAndSave);
		}
		return okButton;
	}

	private void validateAndSave(ActionEvent actionEvent) {
		List<ProductRow> selectedRows = new ArrayList<>();
		for (ProductRow row : allProductRows) {
			if (row.orderQty > 0) {
				selectedRows.add(row);
			}
		}

		if (selectedRows.isEmpty()) {
			MessageDialog.error(this, "angal.command.new.pleaseselectatleastoneproduct.msg");
			return;
		}

		if (addRowsMode) {
			try {
				for (ProductRow pr : selectedRows) {
					CommandRow row = new CommandRow();
					row.setCommand(command);
					row.setMedical(pr.medical);
					row.setMedicalCode(pr.medical.getProdCode());
					row.setMedicalDescription(pr.medical.getDescription());
					row.setQtyInStore(pr.currentStock);
					row.setCriticalLevel(pr.criticalLevel);
					row.setOrderQty(pr.orderQty);
					row.setUserAddedQty(0);
					commandBrowserManager.saveOrUpdateRow(row);
				}
				fireCommandUpdated();
				dispose();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			return;
		}

		String refNo = refNoTextField.getText().trim();
		if (refNo.isEmpty()) {
			MessageDialog.error(this, "angal.command.pleaseinsertarefno.msg");
			return;
		}
		LocalDate selectedDate = dateChooser.getDate();
		if (selectedDate == null) {
			MessageDialog.error(this, "angal.command.pleaseinsertadate.msg");
			return;
		}

		command.setRefNo(refNo);
		command.setDate(selectedDate.atStartOfDay());
		try {
			Command saved = commandBrowserManager.saveOrUpdate(command);
			if (saved == null) {
				MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
				return;
			}
			command.setId(saved.getId());
			command.setLock(saved.getLock());
			for (ProductRow pr : selectedRows) {
				CommandRow row = new CommandRow();
				row.setCommand(saved);
				row.setMedical(pr.medical);
				row.setMedicalCode(pr.medical.getProdCode());
				row.setMedicalDescription(pr.medical.getDescription());
				row.setQtyInStore(pr.currentStock);
				row.setCriticalLevel(pr.criticalLevel);
				row.setOrderQty(pr.orderQty);
				row.setUserAddedQty(0);
				commandBrowserManager.saveOrUpdateRow(row);
			}
			if (insert) {
				fireCommandInserted();
			} else {
				fireCommandUpdated();
			}
			dispose();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private JTextField getRefNoTextField() {
		if (refNoTextField == null) {
			refNoTextField = new VoLimitedTextField(50);
			if (addRowsMode && command.getRefNo() != null) {
				refNoTextField.setText(command.getRefNo());
				refNoTextField.setEnabled(false);
			} else if (!insert && command.getRefNo() != null) {
				refNoTextField.setText(command.getRefNo());
			}
		}
		return refNoTextField;
	}

	private GoodDateChooser getDateChooser() {
		if (dateChooser == null) {
			LocalDate date = null;
			if (addRowsMode && command.getDate() != null) {
				date = command.getDate().toLocalDate();
			} else if (!insert && command.getDate() != null) {
				date = command.getDate().toLocalDate();
			}
			dateChooser = new GoodDateChooser(date, true, false);
			if (addRowsMode) {
				dateChooser.setEnabled(false);
			}
		}
		return dateChooser;
	}

	private JTable getProductTable() {
		if (productTable == null) {
			loadProducts();
			productModel = new ProductTableModel();
			productTable = new JTable(productModel);
			productTable.setPreferredScrollableViewportSize(new Dimension(850, 350));
			TableColumnModel columnModel = productTable.getColumnModel();
			columnModel.getColumn(0).setPreferredWidth(80);
			columnModel.getColumn(1).setPreferredWidth(250);
			columnModel.getColumn(2).setPreferredWidth(100);
			columnModel.getColumn(3).setPreferredWidth(100);
			columnModel.getColumn(4).setPreferredWidth(100);
			columnModel.getColumn(4).setCellEditor(new SpinnerEditor());
		}
		return productTable;
	}

	private void loadProducts() {
		try {
			List<Medical> allMedicals = medicalBrowsingManager.getMedicals();
			List<Integer> usedMedicalIds = commandBrowserManager.getMedicalIdsAlreadyInCommandRows();

			for (Medical med : allMedicals) {
				if (med.getDeleted() == 'Y') {
					continue;
				}
				if (usedMedicalIds.contains(med.getCode())) {
					continue;
				}
				double currentStock = med.getInitialqty() + med.getInqty() - med.getOutqty();
				boolean isCritical = med.getMinqty() > 0 && currentStock <= med.getMinqty();
				boolean isZeroStock = currentStock == 0;
				if (isCritical || isZeroStock) {
					ProductRow pr = new ProductRow();
					pr.medical = med;
					pr.currentStock = currentStock;
					pr.criticalLevel = med.getMinqty();
					pr.orderQty = 0;
					allProductRows.add(pr);
				}
			}
			productRows = new ArrayList<>(allProductRows);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private static class ProductRow {
		Medical medical;
		double currentStock;
		double criticalLevel;
		double orderQty;
	}

	class ProductTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private final String[] columns = {
				MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
				MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
				MessageBundle.getMessage("angal.command.row.qtyinstore.col").toUpperCase(),
				MessageBundle.getMessage("angal.command.row.criticallevel.col").toUpperCase(),
				MessageBundle.getMessage("angal.command.new.ordertableqty.col").toUpperCase()
		};

		@Override
		public int getRowCount() {
			return productRows == null ? 0 : productRows.size();
		}

		@Override
		public String getColumnName(int c) {
			return columns[c];
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			ProductRow pr = productRows.get(r);
			if (c == 0) {
				return pr.medical.getProdCode();
			} else if (c == 1) {
				return pr.medical.getDescription();
			} else if (c == 2) {
				return pr.currentStock;
			} else if (c == 3) {
				return pr.criticalLevel;
			} else if (c == 4) {
				return pr.orderQty;
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int r, int c) {
			if (c == 4) {
				try {
					productRows.get(r).orderQty = ((Number) aValue).doubleValue();
				} catch (Exception e) {
					productRows.get(r).orderQty = 0;
				}
			}
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return c == 4;
		}

		@Override
		public Class<?> getColumnClass(int c) {
			if (c <= 1) {
				return String.class;
			}
			return Double.class;
		}
	}

	static class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;

		private final JSpinner spinner;

		public SpinnerEditor() {
			spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999.0, 1.0));
			spinner.setBorder(null);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			spinner.setValue(value != null ? value : 0.0);
			return spinner;
		}

		@Override
		public Object getCellEditorValue() {
			return spinner.getValue();
		}
	}
}
