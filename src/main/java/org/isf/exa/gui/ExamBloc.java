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
package org.isf.exa.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.isf.exa.manager.BlockBrowsingManager;
import org.isf.exa.model.Block;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * ExamBloc - list all exam blocks. Let the user create, edit or delete a block.
 */
public class ExamBloc extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private int selectedrow;
	private List<Block> blockList;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private int[] pColumnWidth = { 200, 400 };
	private Block block;

	private DefaultTableModel model;
	private JTable table;
	private final JFrame myFrame;
	private JButton jButtonNew;
	private JButton jButtonEdit;
	private JButton jButtonDelete;
	private JButton jButtonClose;
	private JPanel jContentPanel;
	private JTextField searchField;
	private BlockBrowsingManager blockBrowsingManager = Context.getApplicationContext().getBean(BlockBrowsingManager.class);

	public ExamBloc() {
		myFrame = this;
		setTitle(MessageBundle.getMessage("angal.exa.exambrowsingBloc"));
		this.setContentPane(getJContentPanel());
		setMinimumSize(new Dimension(700, 400));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		filterBlocks("");
		setVisible(true);
	}

	private JPanel getJContentPanel() {
		if (jContentPanel == null) {
			jContentPanel = new JPanel();
			jContentPanel.setLayout(new BorderLayout());

			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

			JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.exams.findbloc") + ": ");
			topPanel.add(searchLabel);

			searchField = new JTextField(20);
			searchField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					filterBlocks(searchField.getText());
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					filterBlocks(searchField.getText());
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					filterBlocks(searchField.getText());
				}
			});
			topPanel.add(searchField);

			topPanel.add(Box.createHorizontalStrut(20));

			jContentPanel.add(topPanel, BorderLayout.NORTH);

			jContentPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);

			jContentPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPanel;
	}

	private JPanel getJButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttonPanel.add(getJButtonNew());
		buttonPanel.add(getJButtonEdit());
		buttonPanel.add(getJButtonDelete());
		buttonPanel.add(getJButtonClose());
		return buttonPanel;
	}

	private JTable getJTable() {
		if (table == null) {
			model = new ExamBrowsingModel();
			table = new JTable(model);
			table.setAutoCreateColumnsFromModel(false);
			table.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			table.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					int selectedRow = table.getSelectedRow();
					if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
						selectedrow = table.convertRowIndexToModel(selectedRow);
						Object value = model.getValueAt(selectedrow, -1);
						if (value instanceof Block) {
							block = (Block) value;
						}
					} else {
						selectedrow = -1;
					}
				}
			});
		}
		return table;
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				block = new Block("", "");
				ExamBlocEdit newrecord = new ExamBlocEdit(myFrame, block, true);
				newrecord.setVisible(true);
				filterBlocks(searchField.getText());
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				} else {
					selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
					block = (Block) model.getValueAt(selectedrow, -1);
					ExamBlocEdit editrecord = new ExamBlocEdit(myFrame, block, false);
					editrecord.setVisible(true);
					filterBlocks(searchField.getText());
				}
			});
		}
		return jButtonEdit;
	}

	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				Block blockToDelete = (Block) model.getValueAt(selectedrow, -1);
				int answer = MessageDialog.yesNo(null, "angal.bloc.deletefolowingexam.fmt.msg",
						blockToDelete.getCode(), blockToDelete.getDescription());
				if (answer == JOptionPane.YES_OPTION) {
					boolean deleted = false;
					try {
						blockBrowsingManager.deleteBlock(blockToDelete.getCode());
						deleted = true;
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
					}
					if (deleted) {
						filterBlocks(searchField.getText());
					}
				}
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> dispose());
		}
		return jButtonClose;
	}

	class ExamBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public ExamBrowsingModel() {
		}

		@Override
		public int getRowCount() {
			if (blockList == null) {
				return 0;
			}
			return blockList.size();
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
			if (blockList == null || r >= blockList.size() || r < 0) {
				return null;
			}
			Block block = blockList.get(r);
			if (c == -1) {
				return block;
			} else if (c == 0) {
				return block.getCode();
			} else if (c == 1) {
				return block.getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private void filterBlocks(String searchText) {
		String searchLower = searchText == null ? "" : searchText.toLowerCase().trim();
		List<Block> filteredList;
		try {
			filteredList = blockBrowsingManager.getBlocks();
			if (!searchLower.isEmpty()) {
				filteredList = filteredList.stream()
						.filter(b -> b.getCode().toLowerCase().contains(searchLower)
								|| b.getDescription().toLowerCase().contains(searchLower))
						.collect(java.util.stream.Collectors.toList());
			}
			blockList = filteredList;

			if (table != null && table.getModel() instanceof ExamBrowsingModel) {
				((ExamBrowsingModel) table.getModel()).fireTableDataChanged();
				table.updateUI();
			}

			if (table != null && table.getRowCount() > 0) {
				table.setRowSelectionInterval(0, 0);
				selectedrow = 0;
			} else {
				selectedrow = -1;
			}

		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
