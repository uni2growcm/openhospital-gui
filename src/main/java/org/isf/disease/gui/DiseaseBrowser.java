/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.disease.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.isf.disease.gui.DiseaseEdit.DiseaseListener;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * DiseaseBrowser - This class shows a list of diseases.
 * 					It is possible to filter data with a selection combo box
 * 					and edit-insert-delete records
 */
public class DiseaseBrowser extends ModalJFrame implements DiseaseListener {

	private static final long serialVersionUID = 1L;
	private static final DiseaseType ALL_DISEASETYPES = new DiseaseType("", MessageBundle.getMessage("angal.common.all.txt").toUpperCase());

	@Override
	public void diseaseInserted(AWTEvent e) {
		pDisease.add(0, disease);
		((DiseaseBrowserModel) table.getModel()).fireTableDataChanged();
		//table.updateUI();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	@Override
	public void diseaseUpdated(AWTEvent e) {
		pDisease.set(selectedrow, disease);
		((DiseaseBrowserModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}

	}
	
	private int selectedrow;
	private JComboBox<DiseaseType> pbox;
	private JTextField searchField;
	private List<Disease> pDisease;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase()
	};
	private int[] pColumnWidth = {50, 180, 200};
	private Disease disease;
	private DefaultTableModel model;
	private JTable table;
	private JFrame myFrame;
	private DiseaseType pSelection;
	private DiseaseBrowserManager diseaseBrowserManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private DiseaseTypeBrowserManager diseaseTypeBrowserManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);


	public DiseaseBrowser() {

		setTitle(MessageBundle.getMessage("angal.disease.diseasesbrowser.title"));
		myFrame = this;

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.common.search.txt") + ": ");
		topPanel.add(searchLabel);

		searchField = new JTextField(20);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterDiseases(searchField.getText());
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				filterDiseases(searchField.getText());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				filterDiseases(searchField.getText());
			}
		});
		topPanel.add(searchField);

		topPanel.add(Box.createHorizontalStrut(20));

		JLabel typeLabel = new JLabel(MessageBundle.getMessage("angal.disease.selecttype"));
		topPanel.add(typeLabel);

		pbox = new JComboBox<>();
		pbox.addItem(ALL_DISEASETYPES);
		List<DiseaseType> type = null;
		try {
			type = diseaseTypeBrowserManager.getDiseaseType();
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		// for efficiency in the sequent for
		if (type != null) {
			for (DiseaseType elem : type) {
				pbox.addItem(elem);
			}
		}
		pbox.addActionListener(actionEvent -> {
			pSelection = (DiseaseType) pbox.getSelectedItem();
			filterDiseases(searchField.getText());
		});
		topPanel.add(pbox);

		add(topPanel, BorderLayout.NORTH);

		model = new DiseaseBrowserModel();
		table = new JTable(model);
		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.getColumnModel().getColumn(0).setMaxWidth(pColumnWidth[0]);
		table.getColumnModel().getColumn(1).setPreferredWidth(pColumnWidth[1]);
		table.getColumnModel().getColumn(2).setPreferredWidth(pColumnWidth[2]);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();

		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			disease = new Disease(null, "", new DiseaseType("", ""));    //disease will reference the new record
			DiseaseEdit newrecord = new DiseaseEdit(myFrame, disease, true);
			newrecord.addDiseaseListener(this);
			newrecord.setVisible(true);
		});
		buttonPanel.add(buttonNew);

		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				disease = (Disease) model.getValueAt(selectedrow, -1);
				DiseaseEdit editrecord = new DiseaseEdit(myFrame, disease, false);
				editrecord.addDiseaseListener(this);
				editrecord.setVisible(true);
			}
		});
		buttonPanel.add(buttonEdit);

		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.getSelectedRow();
				disease = (Disease) model.getValueAt(selectedrow, -1);
				int answer = MessageDialog.yesNo(this, "angal.disease.deletedisease.fmt.msg", disease.getDescription());
				try {
					if (answer == JOptionPane.YES_OPTION) {
						diseaseBrowserManager.deleteDisease(disease);
						disease.setIpdInInclude(false);
						disease.setIpdOutInclude(false);
						disease.setOpdInclude(false);
						diseaseUpdated(null);
					}
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			}
		});
		buttonPanel.add(buttonDelete);

		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(actionEvent -> dispose());
		buttonPanel.add(buttonClose);

		add(buttonPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	
	class DiseaseBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public DiseaseBrowserModel(String s) {
			try {
				pDisease = diseaseBrowserManager.getDisease(s);
			} catch(OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}
		public DiseaseBrowserModel() {
			try {
				pDisease = diseaseBrowserManager.getDiseaseAll();
			} catch(OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}
		@Override
		public int getRowCount() {
			if (pDisease == null) {
				return 0;
			}
			return pDisease.size();
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
			Disease disease = pDisease.get(r);
			if (c == 0) {
				return disease.getCode();
			} else if (c == -1) {
				return disease;
			} else if (c == 1) {
				return disease.getType().getDescription();
			} else if (c == 2) {
				return disease.getDescription();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	class ColorTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			if (!((Disease) table.getValueAt(row, -1)).getIpdInInclude() &&
					!((Disease) table.getValueAt(row, -1)).getIpdOutInclude() &&
					!((Disease) table.getValueAt(row, -1)).getOpdInclude()) {
				cell.setForeground(Color.GRAY);
			}
			return cell;
		}
	}

	private void filterDiseases(String searchText) {
		String currentTypeCode = pSelection != null ? pSelection.getCode() : "";
		String searchLower = searchText.toLowerCase().trim();

		List<Disease> filteredList;

		try {
			if (currentTypeCode.isEmpty() || currentTypeCode.equals(ALL_DISEASETYPES.getCode())) {
				filteredList = diseaseBrowserManager.getDiseaseAll();
			} else {
				filteredList = diseaseBrowserManager.getDisease(currentTypeCode);
			}

			if (!searchLower.isEmpty()) {
				filteredList = filteredList.stream()
						.filter(d -> d.getCode().toLowerCase().contains(searchLower) ||
								d.getDescription().toLowerCase().contains(searchLower))
						.collect(java.util.stream.Collectors.toList());
			}

			pDisease = filteredList;
			((DiseaseBrowserModel) table.getModel()).fireTableDataChanged();
			table.updateUI();

		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
	}
}
