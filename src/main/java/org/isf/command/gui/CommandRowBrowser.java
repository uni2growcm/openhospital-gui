/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.command.gui.CommandRowEdit.CommandRowListener;
import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.command.model.CommandRow;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class CommandRowBrowser extends ModalJFrame implements CommandRowListener {

	private static final long serialVersionUID = 1L;

	private final CommandBrowserManager commandBrowserManager = Context.getApplicationContext().getBean(CommandBrowserManager.class);

	private JPanel jContentPane;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
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
			MessageBundle.getMessage("angal.command.row.stillqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.command.row.useraddedqty.col").toUpperCase()
	};
	private final int[] pColumnWidth = { 80, 200, 100, 100, 100, 100, 100 };
	private final Class[] pColumnClass = { String.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class };
	private int selectedrow;
	private List<CommandRow> pCommandRow;
	private CommandRow commandRow;
	private final Command command;
	private final JFrame myFrame;

	public CommandRowBrowser(JFrame parent, Command cmd) {
		myFrame = this;
		this.command = cmd;
		setTitle(MessageBundle.getMessage("angal.command.rowbrowser.title") + " - " + cmd.getRefNo());
		setContentPane(getJContentPane());
		setMinimumSize(new Dimension(700, 400));
		setPreferredSize(new Dimension(900, 400));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void commandRowInserted(AWTEvent e) {
		pCommandRow.add(0, commandRow);
		((CommandRowBrowserModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void commandRowUpdated(AWTEvent e) {
		pCommandRow.set(selectedrow, commandRow);
		((CommandRowBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new WrapLayout());
			jButtonPanel.add(getJNewButton());
			jButtonPanel.add(getJEditButton());
			jButtonPanel.add(getJDeleteButton());
			jButtonPanel.add(getJCloseButton());
		}
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				commandRow = new CommandRow();
				commandRow.setCommand(command);
				CommandRowEdit newrecord = new CommandRowEdit(myFrame, commandRow, true);
				newrecord.addCommandRowListener(this);
				newrecord.setVisible(true);
			});
		}
		return jNewButton;
	}

	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(actionEvent -> {
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
			jDeleteButton.addActionListener(actionEvent -> {
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
			table = new JTable(model);
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
			try {
				pCommandRow = commandBrowserManager.getRowsByCommand(command);
			} catch (OHServiceException e) {
				pCommandRow = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
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
				return row.getStillQty();
			} else if (c == 6) {
				return row.getUserAddedQty();
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
