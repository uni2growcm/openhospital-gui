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
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.command.gui.CommandEdit.CommandListener;
import org.isf.command.gui.CommandRowEdit.CommandRowListener;
import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.command.model.CommandRow;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.menu.manager.Context;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class CommandRowBrowser extends ModalJFrame implements CommandRowListener {

	private static final long serialVersionUID = 1L;

	private final CommandBrowserManager commandBrowserManager = Context.getApplicationContext().getBean(CommandBrowserManager.class);
	private final SupplierBrowserManager supplierManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);
	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private final MedicalDsrStockMovementTypeBrowserManager movementTypeManager =
			Context.getApplicationContext().getBean(MedicalDsrStockMovementTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel topPanel;
	private JComboBox<Supplier> globalSupplierCombo;
	private JButton applyButton;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
	private JButton jLotButton;
	private JButton jCloseButton;
	private JScrollPane jScrollPane;
	private JTable table;
	private DefaultTableModel model;
	private final String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.command.row.qtyinstore.col").toUpperCase(),
			MessageBundle.getMessage("angal.command.row.criticallevel.col").toUpperCase(),
			MessageBundle.getMessage("angal.command.row.orderqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.command.row.lot.col").toUpperCase(),
			MessageBundle.getMessage("angal.command.row.supplier.col").toUpperCase()
	};
	private final int[] pColumnWidth = { 80, 200, 100, 100, 100, 100, 150 };
	private final Class[] pColumnClass = { String.class, String.class, Double.class, Double.class, Double.class, String.class, String.class };
	private int selectedrow;
	private List<CommandRow> pCommandRow;
	private CommandRow commandRow;
	private final Command command;
	private final JFrame myFrame;
	private List<Supplier> suppliers;
	private MovementType chargeType;
	private boolean chargeTypeLoaded;

	public CommandRowBrowser(JFrame parent, Command cmd) {
		myFrame = this;
		this.command = cmd;
		loadSuppliers();
		setTitle(MessageBundle.getMessage("angal.command.rowbrowser.title") + " - " + cmd.getRefNo());
		setContentPane(getJContentPane());
		setMinimumSize(new Dimension(700, 450));
		setPreferredSize(new Dimension(900, 450));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void loadSuppliers() {
		try {
			suppliers = supplierManager.getList();
		} catch (OHServiceException e) {
			suppliers = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void loadChargeType() {
		if (chargeTypeLoaded) {
			return;
		}
		try {
			List<MovementType> types = movementTypeManager.getMedicalDsrStockMovementType();
			for (MovementType t : types) {
				if (t.getType().contains("+")) {
					chargeType = t;
					break;
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		chargeTypeLoaded = true;
	}

	@Override
	public void commandRowInserted(AWTEvent e) {
		pCommandRow.add(0, commandRow);
		model.fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void commandRowUpdated(AWTEvent e) {
		pCommandRow.set(selectedrow, commandRow);
		model.fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.add(getTopPanel(), BorderLayout.NORTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			topPanel.add(new JLabel(MessageBundle.getMessage("angal.command.row.globalsupplier.label")));
			topPanel.add(getGlobalSupplierCombo());
		}
		return topPanel;
	}

	private JComboBox<Supplier> getGlobalSupplierCombo() {
		if (globalSupplierCombo == null) {
			globalSupplierCombo = new JComboBox<>();
			globalSupplierCombo.addItem(null);
			for (Supplier s : suppliers) {
				globalSupplierCombo.addItem(s);
			}
			globalSupplierCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list,
							value instanceof Supplier ? ((Supplier) value).getSupName() : "",
							index, isSelected, cellHasFocus);
					return this;
				}
			});
		}
		return globalSupplierCombo;
	}

	private JButton getApplyButton() {
		if (applyButton == null) {
			applyButton = new JButton(MessageBundle.getMessage("angal.command.row.apply.btn"));
			applyButton.setEnabled(!isReadOnly());
			applyButton.addActionListener(actionEvent -> apply());
		}
		return applyButton;
	}

	private void apply() {
		if (isReadOnly()) {
			return;
		}
		loadChargeType();
		if (chargeType == null) {
			MessageDialog.error(this, "angal.medicalstock.pleasechooseatype.msg");
			return;
		}

		Supplier globalSupplier = (Supplier) globalSupplierCombo.getSelectedItem();

		List<String> missingInfo = new ArrayList<>();
		for (int i = 0; i < pCommandRow.size(); i++) {
			CommandRow row = pCommandRow.get(i);
			if (row.getOrderQty() == null || row.getOrderQty() <= 0) {
				continue;
			}
			if (row.getLot() == null) {
				missingInfo.add(row.getMedicalDescription() + ": " +
						MessageBundle.getMessage("angal.command.row.nolot.fmt.msg"));
				continue;
			}
			Supplier rowSupplier = row.getSupplier() != null ? row.getSupplier() : globalSupplier;
			if (rowSupplier == null) {
				missingInfo.add(row.getMedicalDescription() + ": " +
						MessageBundle.getMessage("angal.command.row.nosupplier.fmt.msg"));
			}
		}

		if (!missingInfo.isEmpty()) {
			StringBuilder errMsg = new StringBuilder(
					MessageBundle.getMessage("angal.command.row.allrowsneedlotandsupplier.msg"));
			for (String m : missingInfo) {
				errMsg.append("\n- ").append(m);
			}
			MessageDialog.error(this, errMsg.toString());
			return;
		}

		List<CommandRow> rowsToProcess = new ArrayList<>();

		for (int i = 0; i < pCommandRow.size(); i++) {
			CommandRow row = pCommandRow.get(i);
			if (row.getOrderQty() == null || row.getOrderQty() <= 0) {
				continue;
			}
			rowsToProcess.add(row);
		}

		if (rowsToProcess.isEmpty()) {
			MessageDialog.error(this, "angal.command.row.norowstoapply.msg");
			return;
		}

		StringBuilder msg = new StringBuilder(
				MessageBundle.getMessage("angal.command.row.confirmapply.fmt.msg"));
		msg.append(" (").append(rowsToProcess.size()).append(" ")
				.append(MessageBundle.getMessage("angal.command.row.products.txt")).append(")");

		int answer = MessageDialog.yesNo(this, msg.toString());
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}

		JDialog loader = new JDialog(this, MessageBundle.getMessage("angal.common.pleasewait.txt"), true);
		JPanel loaderPanel = new JPanel(new BorderLayout(10, 10));
		loaderPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		loaderPanel.add(new JLabel(MessageBundle.getMessage("angal.command.row.processing.msg")), BorderLayout.NORTH);
		loaderPanel.add(progressBar, BorderLayout.CENTER);
		loader.getContentPane().add(loaderPanel);
		loader.setSize(300, 100);
		loader.setLocationRelativeTo(this);
		loader.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			private OHServiceException error;

			@Override
			protected Boolean doInBackground() {
				List<Movement> movements = new ArrayList<>();
				for (CommandRow row : rowsToProcess) {
					Supplier rowSupplier = row.getSupplier() != null ? row.getSupplier()
							: (Supplier) globalSupplierCombo.getSelectedItem();

					String movRefNo = command.getRefNo() != null ? command.getRefNo() + "-" + row.getMedical().getProdCode()
							: "CMD-" + command.getId();

					Movement mov = new Movement(row.getMedical(), chargeType, null, row.getLot(),
							LocalDateTime.now(), row.getOrderQty().intValue(), rowSupplier, movRefNo);
					movements.add(mov);
				}

				try {
					movStockInsertingManager.newMultipleChargingMovements(movements, null);

					for (int i = 0; i < movements.size(); i++) {
						Movement m = movements.get(i);
						CommandRow row = rowsToProcess.get(i);
						row.setLot(m.getLot());
						row.setSupplier(m.getSupplier() != null ? m.getSupplier()
								: (Supplier) globalSupplierCombo.getSelectedItem());
						row.setActive(0);
						commandBrowserManager.saveOrUpdateRow(row);
					}

					command.setActive(0);
					commandBrowserManager.saveOrUpdate(command);
					return true;
				} catch (OHServiceException ex) {
					error = ex;
					return false;
				}
			}

			@Override
			protected void done() {
				loader.dispose();
				if (error != null) {
					OHServiceExceptionUtil.showMessages(error);
				} else {
					MessageDialog.info(CommandRowBrowser.this, "angal.command.row.applysuccess.msg");
					dispose();
				}
			}
		};

		worker.execute();
		loader.setVisible(true);
	}

	private void refreshModel() {
		try {
			pCommandRow = commandBrowserManager.getRowsByCommand(command);
		} catch (OHServiceException e) {
			pCommandRow = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
		if (model != null) {
			model.fireTableDataChanged();
		}
		if (table != null) {
			table.updateUI();
		}
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new WrapLayout());
			jButtonPanel.add(getJNewButton());
			jButtonPanel.add(getJEditButton());
			jButtonPanel.add(getJDeleteButton());
			jButtonPanel.add(getJLotButton());
			jButtonPanel.add(getApplyButton());
			jButtonPanel.add(getJCloseButton());
		}
		return jButtonPanel;
	}

	private boolean isReadOnly() {
		return command.getActive() == 0;
	}

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.setEnabled(!isReadOnly());
			jNewButton.addActionListener(actionEvent -> {
				if (isReadOnly()) {
					return;
				}
				NewCommandDialog dialog = new NewCommandDialog(myFrame, command, true);
				dialog.addCommandListener(new CommandListener() {
					@Override
					public void commandUpdated(AWTEvent e) {
						refreshModel();
					}

					@Override
					public void commandInserted(AWTEvent e) {
						refreshModel();
					}
				});
				dialog.setVisible(true);
			});
		}
		return jNewButton;
	}

	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.setEnabled(!isReadOnly());
			jEditButton.addActionListener(actionEvent -> {
				if (isReadOnly()) {
					return;
				}
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.getSelectedRow();
					commandRow = (CommandRow) model.getValueAt(table.getSelectedRow(), -1);
					CommandRowEdit editrecord = new CommandRowEdit(myFrame, commandRow, false);
					editrecord.addCommandRowListener(this);
					editrecord.setVisible(true);
				}
			});
		}
		return jEditButton;
	}

	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.setEnabled(!isReadOnly());
			jDeleteButton.addActionListener(actionEvent -> {
				if (isReadOnly()) {
					return;
				}
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					CommandRow row = (CommandRow) model.getValueAt(table.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(this, "angal.command.row.deleterow.fmt.msg", row.getMedicalDescription());
					try {
						if (answer == JOptionPane.YES_OPTION) {
							commandBrowserManager.deleteRow(row);
							pCommandRow.remove(table.getSelectedRow());
							model.fireTableDataChanged();
							table.updateUI();
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JButton getJLotButton() {
		if (jLotButton == null) {
			jLotButton = new JButton(MessageBundle.getMessage("angal.command.row.lot.btn"));
			jLotButton.setMnemonic(MessageBundle.getMnemonic("angal.command.row.lot.btn.key"));
			jLotButton.setEnabled(!isReadOnly());
			jLotButton.addActionListener(actionEvent -> {
				if (isReadOnly()) {
					return;
				}
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedrow = table.getSelectedRow();
				commandRow = (CommandRow) model.getValueAt(table.getSelectedRow(), -1);
				Lot existingLot = commandRow.getLot();
				boolean isNewLot = existingLot == null;

				LocalDateTime preparationDate = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
				JTextField lotCodeTextField = new JTextField(15);
				GoodDateChooser preparationDateChooser = new GoodDateChooser(preparationDate.toLocalDate());
				GoodDateChooser expireDateChooser = new GoodDateChooser(preparationDate.toLocalDate().plusYears(1));

				if (!isNewLot) {
					lotCodeTextField.setText(existingLot.getCode());
					preparationDateChooser.setDate(existingLot.getPreparationDate().toLocalDate());
					expireDateChooser.setDate(existingLot.getDueDate().toLocalDate());
				}

				JComboBox<Supplier> rowSupplierCombo = new JComboBox<>();
				rowSupplierCombo.addItem(null);
				for (Supplier s : suppliers) {
					rowSupplierCombo.addItem(s);
				}
				if (commandRow.getSupplier() != null) {
					rowSupplierCombo.setSelectedItem(commandRow.getSupplier());
				}

				rowSupplierCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
					private static final long serialVersionUID = 1L;

					@Override
					public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						super.getListCellRendererComponent(list,
								value instanceof Supplier ? ((Supplier) value).getSupName() : "",
								index, isSelected, cellHasFocus);
						return this;
					}
				});

				JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
				panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb")));
				panel.add(lotCodeTextField);
				panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.preparationdate")));
				panel.add(preparationDateChooser);
				panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
				panel.add(expireDateChooser);
				panel.add(new JLabel(MessageBundle.getMessage("angal.command.row.perrowsupplier.label")));
				panel.add(rowSupplierCombo);

				int ok = JOptionPane.showConfirmDialog(myFrame, panel,
						MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);

				if (ok == JOptionPane.OK_OPTION) {
					String lotCode = lotCodeTextField.getText().trim();
					LocalDate prepDate = preparationDateChooser.getDate();
					LocalDate expDate = expireDateChooser.getDate();

					if (prepDate == null || expDate == null) {
						MessageDialog.error(this, "angal.medicalstock.insertavalidpreparationdate.msg");
						return;
					}
					if (prepDate.isAfter(expDate)) {
						MessageDialog.error(this, "angal.medicalstock.thepreparationdatecannotbyaftertheduedate.msg");
						return;
					}
					try {
						if (!isNewLot && existingLot != null) {
							existingLot.setCode(lotCode);
							existingLot.setPreparationDate(prepDate.atStartOfDay());
							existingLot.setDueDate(expDate.atStartOfDay());
							if (GeneralData.LOTWITHCOST) {
								String costStr = JOptionPane.showInputDialog(myFrame,
										MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"),
										existingLot.getCost());
								if (costStr != null) {
									try {
										existingLot.setCost(new BigDecimal(costStr));
									} catch (NumberFormatException ex) {
										MessageDialog.error(this, "angal.common.error.txt");
									}
								}
							}
							Lot savedLot = movStockInsertingManager.updateLot(existingLot);
							commandRow.setLot(savedLot);
						} else {
							Lot lot = new Lot(commandRow.getMedical(), lotCode,
									prepDate.atStartOfDay(), expDate.atStartOfDay());
							if (GeneralData.LOTWITHCOST) {
								String costStr = JOptionPane.showInputDialog(myFrame,
										MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"),
										"0");
								if (costStr != null) {
									try {
										lot.setCost(new BigDecimal(costStr));
									} catch (NumberFormatException ex) {
										MessageDialog.error(this, "angal.common.error.txt");
									}
								}
							}
							Lot savedLot = movStockInsertingManager.storeLot(lot.getCode(), lot, commandRow.getMedical());
							commandRow.setLot(savedLot);
						}

						commandRow.setSupplier((Supplier) rowSupplierCombo.getSelectedItem());
						commandBrowserManager.saveOrUpdateRow(commandRow);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}

				SwingUtilities.invokeLater(() -> {
					refreshModel();
					if (selectedrow >= 0 && selectedrow < table.getRowCount()) {
						table.setRowSelectionInterval(selectedrow, selectedrow);
					}
				});
				}
			});
		}
		return jLotButton;
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	private JTable getJTable() {
		if (table == null) {
			model = new CommandRowBrowserModel();
			table = new JTable(model) {

				private static final long serialVersionUID = 1L;

				@Override
				public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
					Component c = super.prepareRenderer(renderer, row, column);
					CommandRow r = (CommandRow) model.getValueAt(row, -1);
					if (r != null && r.getLot() != null) {
						if (!isRowSelected(row)) {
							c.setBackground(new java.awt.Color(230, 255, 230));
						}
					} else {
						if (!isRowSelected(row)) {
							c.setBackground(getBackground());
						}
					}
					return c;
				}
			};
			TableColumnModel columnModel = table.getColumnModel();
			for (int i = 0; i < pColumnWidth.length; i++) {
				columnModel.getColumn(i).setPreferredWidth(pColumnWidth[i]);
			}
		}
		return table;
	}

	class CommandRowBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public CommandRowBrowserModel() {
			loadData();
		}

		private void loadData() {
			try {
				pCommandRow = commandBrowserManager.getRowsByCommand(command);
			} catch (OHServiceException e) {
				pCommandRow = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			if (pCommandRow == null) {
				return 0;
			}
			return pCommandRow.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			CommandRow row = pCommandRow.get(r);
			if (c == -1) {
				return row;
			} else if (c == 0) {
				return row.getMedicalCode();
			} else if (c == 1) {
				return row.getMedicalDescription();
			} else if (c == 2) {
				return row.getQtyInStore();
			} else if (c == 3) {
				return row.getCriticalLevel();
			} else if (c == 4) {
				return row.getOrderQty();
			} else if (c == 5) {
				Lot lot = row.getLot();
				return lot != null ? lot.getCode() : "";
			} else if (c == 6) {
				Supplier supplier = row.getSupplier();
				return supplier != null ? supplier.getSupName() : "";
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return pColumnClass[columnIndex];
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
