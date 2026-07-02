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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Box;
import java.awt.FlowLayout;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.operation.enums.OperationTarget;
import org.isf.operation.gui.OperationEdit.OperationListener;
import org.isf.articlefamily.manager.ArticleFamilyBrowserManager;
import org.isf.articlefamily.model.ArticleFamily;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.opetype.manager.OperationTypeBrowserManager;
import org.isf.opetype.model.OperationType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * This class shows a list of operations. It is possible to filter data with a selection combo box and edit-insert-delete records
 * @author Rick, Vero, Pupo
 */
public class OperationBrowser extends ModalJFrame implements OperationListener {

	//TODO: replace with mapping mnemonic / translation in OperationBrowserManager
	public static final String OPD = MessageBundle.getMessage("angal.admission.opd.txt").toUpperCase();
	public static final String ADMISSION = MessageBundle.getMessage("angal.admission.admission.txt").toUpperCase();
	public static final String OPD_ADMISSION = OPD + " / " + ADMISSION;
	private static final long serialVersionUID = 1L;
	private static final String STR_ALL = MessageBundle.getMessage("angal.common.all.txt").toUpperCase();
	private static final int pfrmBase = 8;
	private static final int pfrmWidth = 5;
	private static final int pfrmHeight = 5;
	private int selectedrow;
	private final JComboBox<OperationType> diseaseTypeFilter;
	private JComboBox<ArticleFamily> articleFamilyFilter;
	private List<Operation> pOperation;
	private final String[] pColumns = {
		MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
		MessageBundle.getMessage("angal.operation.articlefamily.col").toUpperCase(),
		MessageBundle.getMessage("angal.operation.operationcontext.col").toUpperCase()
	};
	private final int[] pColumnWidth = { 50, 180, 200, 130, 100 };
	private Operation operation;
	private DefaultTableModel model;
	private final JTable table;
	private final JFrame myFrame;
	private JTextField searchField;
	private String pSelection;
	private final OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private final OperationTypeBrowserManager operationTypeBrowserManager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);
	private final ArticleFamilyBrowserManager articleFamilyManager = Context.getApplicationContext().getBean(ArticleFamilyBrowserManager.class);

	public OperationBrowser() {
        JTable table1;

        setTitle(MessageBundle.getMessage("angal.operation.operationsbrowser.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		int pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) / 2;
		int pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX, pfrmBordY, screensize.width / pfrmBase * pfrmWidth,
			screensize.height / pfrmBase * pfrmHeight);
		myFrame = this;
		model = new OperationBrowserModel();
		table1 = new JTable(model);
		table1.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
		table1.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table1.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table1.getColumnModel().getColumn(3).setPreferredWidth(pColumnWidth[3]);
		table1.getColumnModel().getColumn(4).setPreferredWidth(pColumnWidth[4]);
		table1.getColumnModel().getColumn(4).setCellRenderer(new CenterAlignmentCellRenderer());

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.common.search.txt") + ": ");
		topPanel.add(searchLabel);

		searchField = new JTextField(20);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterOperations(searchField.getText());
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				filterOperations(searchField.getText());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterOperations(searchField.getText());
			}
		});
		topPanel.add(searchField);

		topPanel.add(Box.createHorizontalStrut(20));

		diseaseTypeFilter = new JComboBox<>();
		diseaseTypeFilter.addItem(new OperationType("", MessageBundle.getMessage("angal.common.all.txt").toUpperCase()));
		List<OperationType> type;
		try {
			type = operationTypeBrowserManager.getOperationType();
			for (OperationType elem : type) {
				diseaseTypeFilter.addItem(elem);
			}
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}

		diseaseTypeFilter.addActionListener(actionEvent -> {
			filterOperations(searchField.getText());
		});
		topPanel.add(diseaseTypeFilter);

		add(topPanel, BorderLayout.NORTH);

		model = new OperationBrowserModel();
		table1 = new JTable(model);
        table = table1;
        table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table.getColumnModel().getColumn(3).setPreferredWidth(pColumnWidth[3]);
		table.getColumnModel().getColumn(3).setCellRenderer(new CenterAlignmentCellRenderer());

		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		JLabel familylabel = new JLabel(MessageBundle.getMessage("angal.exam.filter.family"));
		buttonPanel.add(familylabel);
		articleFamilyFilter = new JComboBox<>();
		articleFamilyFilter.addItem(null);
		articleFamilyFilter.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
			                                              int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value == null) {
					setText(STR_ALL);
				}
				return this;
			}
		});
		try {
			List<ArticleFamily> families = articleFamilyManager.getArticleFamilies();
			for (ArticleFamily af : families) {
				articleFamilyFilter.addItem(af);
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		articleFamilyFilter.addActionListener(actionEvent -> reloadTable());
		buttonPanel.add(articleFamilyFilter);

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			operation = new Operation(null, "", new OperationType("", ""), 0); // operation will reference the new record
			OperationEdit newrecord = new OperationEdit(myFrame, operation, true);
			newrecord.addOperationListener(this);
			newrecord.setVisible(true);
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				operation = (Operation) (model.getValueAt(table.getSelectedRow(), -1));
				OperationEdit editrecord = new OperationEdit(myFrame, operation, false);
				editrecord.addOperationListener(this);
				editrecord.setVisible(true);
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				Operation operation = (Operation) model.getValueAt(table.getSelectedRow(), -1);
				int answer = MessageDialog.yesNo(null, "angal.operation.deleteoperation.fmt.msg", operation.getDescription());
				if (answer == JOptionPane.YES_OPTION) {
					try {
						operationBrowserManager.deleteOperation(operation);
						pOperation.remove(table.getSelectedRow());
						model.fireTableDataChanged();
						table.updateUI();
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			}
		});
		buttonPanel.add(buttonDelete);

		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(actionEvent -> dispose());
		buttonPanel.add(buttonClose);
		add(buttonPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	private void reloadTable() {
		String selectedType = diseaseTypeFilter.getSelectedItem().toString();
		String typeDesc = selectedType.equals(STR_ALL) ? null : selectedType;
		ArticleFamily selectedFamily = (ArticleFamily) articleFamilyFilter.getSelectedItem();
		model = new OperationBrowserModel(typeDesc, selectedFamily);
		model.fireTableDataChanged();
		table.updateUI();
	}

	@Override
	public void operationInserted(AWTEvent e) {
		pOperation.add(0, operation);
		((OperationBrowserModel) table.getModel()).fireTableDataChanged();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}
	@Override
	public void operationUpdated(AWTEvent e) {
		pOperation.set(selectedrow, operation);
		((OperationBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if ((table.getRowCount() > 0) && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	class OperationBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public OperationBrowserModel() {
		}

		public OperationBrowserModel(String typeDesc, ArticleFamily family) {
			try {
				pOperation = operationBrowserManager.getOperationsByFilters(typeDesc, family);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pOperation == null) {
				return 0;
			}
			return pOperation.size();
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
			Operation operation = pOperation.get(r);
			OperationTarget opeFor = operation.getOpeFor();
			if (c == 0) {
				return operation.getCode();
			} else if (c == -1) {
				return operation;
			} else if (c == 1) {
				return operation.getType().getDescription();
			} else if (c == 2) {
				return operation.getDescription();
			} else if (c == 3) {
				return operation.getArticleFamily() != null ? operation.getArticleFamily().toString() : "";
			} else if (c == 4) { // TODO: use bundles
				if (opeFor != null) {
					return switch (opeFor) {
						case admission -> ADMISSION;
						case opd -> OPD;
						default -> OPD_ADMISSION;
					};
				} else {
					return MessageBundle.getMessage("angal.common.notdefined.txt");
				}
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	class CenterAlignmentCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(SwingConstants.CENTER);
			return cell;
		}
	}

	private void filterOperations(String searchText) {
		String selectedTypeDesc = diseaseTypeFilter.getSelectedItem().toString();
		String searchLower = searchText.toLowerCase().trim();

		List<Operation> filteredList;

		try {
			if (selectedTypeDesc.equals(STR_ALL)) {
				filteredList = operationBrowserManager.getOperation();
			} else {
				filteredList = operationBrowserManager.getOperationByTypeDescription(selectedTypeDesc);
			}

			if (!searchLower.isEmpty()) {
				filteredList = filteredList.stream()
						.filter(o -> o.getCode().toLowerCase().contains(searchLower) ||
								o.getDescription().toLowerCase().contains(searchLower))
						.collect(java.util.stream.Collectors.toList());
			}

			pOperation = filteredList;
			((OperationBrowserModel) table.getModel()).fireTableDataChanged();
			table.updateUI();

		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

}
