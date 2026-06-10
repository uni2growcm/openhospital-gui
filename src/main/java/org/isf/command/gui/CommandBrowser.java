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
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.command.gui.CommandEdit.CommandListener;
import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class CommandBrowser extends ModalJFrame implements CommandListener {

	private static final long serialVersionUID = 1L;

	private final CommandBrowserManager commandBrowserManager = Context.getApplicationContext().getBean(CommandBrowserManager.class);

	private JPanel jContentPane;
	private JPanel jTopPanel;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
	private JButton jDetailsButton;
	private JButton jCloseButton;
	private JScrollPane jScrollPane;
	private JTable table;
	private DefaultTableModel model;
	private final String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.command.refno.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.col").toUpperCase()
	};
	private final int[] pColumnWidth = { 60, 200, 200 };
	private final Class[] pColumnClass = { Integer.class, String.class, String.class };
	private int selectedrow;
	private List<Command> pCommand;
	private List<Command> allCommands;
	private Command command;
	private final JFrame myFrame;
	private JTextField searchField;

	public CommandBrowser() {
		myFrame = this;
		setTitle(MessageBundle.getMessage("angal.command.commandbrowser.title"));
		setContentPane(getJContentPane());
		setMinimumSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(800, 400));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void commandInserted(AWTEvent e) {
		allCommands.add(0, command);
		filterCommands();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void commandUpdated(AWTEvent e) {
		allCommands.set(selectedrow, command);
		filterCommands();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.add(getJTopPanel(), BorderLayout.NORTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getJTopPanel() {
		if (jTopPanel == null) {
			jTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.command.search.label"));
			jTopPanel.add(searchLabel);
			jTopPanel.add(getSearchField());
		}
		return jTopPanel;
	}

	private JTextField getSearchField() {
		if (searchField == null) {
			searchField = new VoLimitedTextField(50, 20);
			searchField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					filterCommands();
				}
			});
		}
		return searchField;
	}

	private void filterCommands() {
		String text = searchField.getText().trim().toLowerCase();
		if (text.isEmpty()) {
			pCommand = new ArrayList<>(allCommands);
		} else {
			pCommand = allCommands.stream()
					.filter(c -> c.getRefNo().toLowerCase().contains(text))
					.collect(Collectors.toList());
		}
		model.fireTableDataChanged();
		table.updateUI();
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new WrapLayout());
			jButtonPanel.add(getJNewButton());
			jButtonPanel.add(getJEditButton());
			jButtonPanel.add(getJDeleteButton());
			jButtonPanel.add(getJDetailsButton());
			jButtonPanel.add(getJCloseButton());
		}
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				command = new Command(null, "", null);
				NewCommandDialog newrecord = new NewCommandDialog(myFrame, command, true);
				newrecord.addCommandListener(this);
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
					command = (Command) model.getValueAt(table.getSelectedRow(), -1);
					CommandEdit editrecord = new CommandEdit(myFrame, command, false);
					editrecord.addCommandListener(this);
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
					Command cmd = (Command) model.getValueAt(table.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(this, "angal.command.deletecommand.fmt.msg", cmd.getRefNo());
					try {
						if (answer == JOptionPane.YES_OPTION) {
							commandBrowserManager.delete(cmd);
							allCommands.remove(cmd);
							filterCommands();
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JButton getJDetailsButton() {
		if (jDetailsButton == null) {
			jDetailsButton = new JButton(MessageBundle.getMessage("angal.command.details.btn"));
			jDetailsButton.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					Command cmd = (Command) model.getValueAt(table.getSelectedRow(), -1);
					new CommandRowBrowser(myFrame, cmd);
				}
			});
		}
		return jDetailsButton;
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
			model = new CommandBrowserModel();
			table = new JTable(model);
			TableColumnModel columnModel = table.getColumnModel();
			for (int i = 0; i < pColumnWidth.length; i++) {
				columnModel.getColumn(i).setPreferredWidth(pColumnWidth[i]);
			}
		}
		return table;
	}

	class CommandBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		public CommandBrowserModel() {
			try {
				allCommands = commandBrowserManager.getAllOrderByDateDesc();
				pCommand = new ArrayList<>(allCommands);
			} catch (OHServiceException e) {
				allCommands = new ArrayList<>();
				pCommand = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pCommand == null) {
				return 0;
			}
			return pCommand.size();
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
			Command cmd = pCommand.get(r);
			if (c == -1) {
				return cmd;
			} else if (c == 0) {
				return cmd.getId();
			} else if (c == 1) {
				return cmd.getRefNo();
			} else if (c == 2) {
				return cmd.getDate() != null ? cmd.getDate().format(formatter) : "";
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
