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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.command.gui.CommandEdit.CommandListener;
import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.generaldata.GeneralData;
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
			MessageBundle.getMessage("angal.command.refno.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.col").toUpperCase()
	};
	private final int[] pColumnWidth = { 200, 200 };
	private final Class[] pColumnClass = { String.class, String.class };
	private int selectedrow;
	private List<Command> allFilteredCommands;
	private List<Command> allCommands;
	private Command command;
	private final JFrame myFrame;
	private JTextField searchField;
	private JPanel paginationPanel;
	private JLabel pageInfoLabel;
	private JComboBox<Integer> pagesCombo;
	private boolean updatingPageCombo;
	private int currentPage;
	private int totalPages;
	private List<Command> pageCommand;

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
			JPanel southPanel = new JPanel(new BorderLayout());
			southPanel.add(getPaginationPanel(), BorderLayout.NORTH);
			southPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(southPanel, BorderLayout.SOUTH);
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
			allFilteredCommands = new ArrayList<>(allCommands);
		} else {
			allFilteredCommands = allCommands.stream()
					.filter(c -> c.getRefNo().toLowerCase().contains(text))
					.collect(Collectors.toList());
		}
		currentPage = 0;
		updatePage();
	}

	private void updatePage() {
		totalPages = (int) Math.ceil((double) allFilteredCommands.size() / GeneralData.PAGINATIONPAGESIZE);
		if (totalPages == 0) {
			totalPages = 1;
		}
		if (currentPage >= totalPages) {
			currentPage = totalPages - 1;
		}
		int from = currentPage * GeneralData.PAGINATIONPAGESIZE;
		int to = Math.min(from + GeneralData.PAGINATIONPAGESIZE, allFilteredCommands.size());
		pageCommand = new ArrayList<>(allFilteredCommands.subList(from, to));
		if (model != null) {
			model.fireTableDataChanged();
		}
		if (table != null) {
			table.updateUI();
		}
		updatePaginationControls();
	}

	private void updatePaginationControls() {
		if (pagesCombo == null) {
			return;
		}
		updatingPageCombo = true;
		pagesCombo.removeAllItems();
		for (int i = 1; i <= totalPages; i++) {
			pagesCombo.addItem(i);
		}
		if (totalPages > 0) {
			pagesCombo.setSelectedIndex(currentPage);
		}
		updatingPageCombo = false;
		if (pageInfoLabel != null) {
			pageInfoLabel.setText(MessageBundle.formatMessage("angal.command.pagination.info.fmt",
					allFilteredCommands.size(), GeneralData.PAGINATIONPAGESIZE, totalPages));
		}
	}

	private JPanel getPaginationPanel() {
		if (paginationPanel == null) {
			paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JButton prevButton = new JButton(new AbstractAction("<") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentPage > 0) {
						currentPage--;
						updatePage();
					}
				}
			});
			paginationPanel.add(prevButton);
			paginationPanel.add(getPagesCombo());
			JButton nextButton = new JButton(new AbstractAction(">") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentPage < totalPages - 1) {
						currentPage++;
						updatePage();
					}
				}
			});
			paginationPanel.add(nextButton);
			pageInfoLabel = new JLabel();
			paginationPanel.add(pageInfoLabel);
		}
		return paginationPanel;
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo == null) {
			pagesCombo = new JComboBox<>();
			pagesCombo.setPreferredSize(new Dimension(70, 25));
			pagesCombo.addActionListener(actionEvent -> {
				if (pagesCombo.getItemCount() != 0 && pagesCombo.getSelectedItem() != null && !updatingPageCombo) {
					int selected = (Integer) pagesCombo.getSelectedItem();
					if (selected - 1 != currentPage) {
						currentPage = selected - 1;
						updatePage();
					}
				}
			});
		}
		return pagesCombo;
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
			table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
					Command cmd = (Command) t.getModel().getValueAt(t.convertRowIndexToModel(row), -1);
					if (cmd != null && cmd.getActive() == 0) {
						setForeground(Color.GRAY);
					} else {
						setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
					}
					return this;
				}
			});
		}
		return table;
	}

	class CommandBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		public CommandBrowserModel() {
			try {
				allCommands = commandBrowserManager.getAllOrderByDateDesc();
				allFilteredCommands = new ArrayList<>(allCommands);
				currentPage = 0;
				updatePage();
			} catch (OHServiceException e) {
				allCommands = new ArrayList<>();
				allFilteredCommands = new ArrayList<>();
				pageCommand = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pageCommand == null) {
				return 0;
			}
			return pageCommand.size();
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
			Command cmd = pageCommand.get(r);
			if (c == -1) {
				return cmd;
			} else if (c == 0) {
				return cmd.getRefNo();
			} else if (c == 1) {
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
