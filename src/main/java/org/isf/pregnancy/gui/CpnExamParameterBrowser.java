/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.pregnancy.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.pregnancy.gui.CpnExamParameterEdit.CpnExamParameterListener;
import org.isf.pregnancy.manager.PregnancyExamParameterBrowserManager;
import org.isf.pregnancy.model.PregnancyExamParameter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Administration screen for the "paramètres CPN" catalog ({@link PregnancyExamParameter}): besides the free
 * text parameters, this is where bounded numeric parameters (e.g. hauteur utérine, max 60 cm) and closed
 * lists of values (e.g. conjonctives, présentation) are configured.
 */
public class CpnExamParameterBrowser extends ModalJFrame implements CpnExamParameterListener {

	private static final long serialVersionUID = 1L;

	private List<PregnancyExamParameter> parameters;
	private final String[] columns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.cpn.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.cpn.datatype.txt").toUpperCase()
	};
	private final int[] columnWidth = { 80, 220, 100, 100 };

	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JTable jTable;
	private CpnExamParameterBrowserModel model;
	private int selectedRow;
	private final PregnancyExamParameterBrowserManager examParameterManager =
			Context.getApplicationContext().getBean(PregnancyExamParameterBrowserManager.class);
	private PregnancyExamParameter examParameter;
	private final JFrame myFrame;

	public CpnExamParameterBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.cpn.cpnexamparameterbrowser.title"));
		setContentPane(getJContainPanel());
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
		}
		return jContainPanel;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJNewButton(), null);
			jButtonPanel.add(getJEditButton(), null);
			jButtonPanel.add(getJDeleteButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				examParameter = new PregnancyExamParameter();
				CpnExamParameterEdit newRecord = new CpnExamParameterEdit(myFrame, examParameter, true);
				newRecord.addCpnExamParameterListener(this);
				newRecord.setVisible(true);
			});
		}
		return jNewButton;
	}

	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					selectedRow = jTable.getSelectedRow();
					examParameter = (PregnancyExamParameter) model.getValueAt(selectedRow, -1);
					CpnExamParameterEdit newRecord = new CpnExamParameterEdit(myFrame, examParameter, false);
					newRecord.addCpnExamParameterListener(this);
					newRecord.setVisible(true);
				}
			});
		}
		return jEditButton;
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				} else {
					PregnancyExamParameter toDelete = (PregnancyExamParameter) model.getValueAt(jTable.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(null, "angal.cpn.deletecpnexamparameter.fmt.msg", toDelete.getDescription());
					try {
						if (answer == JOptionPane.YES_OPTION) {
							examParameterManager.deletePregnancyExamParameter(toDelete);
							parameters.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
						}
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new CpnExamParameterBrowserModel();
			jTable = new JTable(model);
			for (int i = 0; i < columnWidth.length; i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
			}
		}
		return jTable;
	}

	class CpnExamParameterBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		CpnExamParameterBrowserModel() {
			try {
				parameters = examParameterManager.getPregnancyExamParameters();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			return parameters == null ? 0 : parameters.size();
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
			PregnancyExamParameter parameter = parameters.get(r);
			switch (c) {
				case -1:
					return parameter;
				case 0:
					return parameter.getCode();
				case 1:
					return parameter.getDescription();
				case 2:
					return parameter.getExamType() == PregnancyExamParameter.PRENATAL
							? MessageBundle.getMessage("angal.cpn.prenatal.txt")
							: parameter.getExamType() == PregnancyExamParameter.POSTNATAL
									? MessageBundle.getMessage("angal.cpn.postnatal.txt")
									: MessageBundle.getMessage("angal.cpn.both.txt");
				case 3:
					return parameter.getDataType().name();
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	@Override
	public void cpnExamParameterUpdated(AWTEvent e) {
		parameters.set(selectedRow, examParameter);
		((CpnExamParameterBrowserModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if (jTable.getRowCount() > 0 && selectedRow > -1) {
			jTable.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	@Override
	public void cpnExamParameterInserted(AWTEvent e) {
		parameters.add(0, examParameter);
		((CpnExamParameterBrowserModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}
}
