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
package org.isf.reductionplan.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.reductionplan.gui.ReductionPlanEdit.ReductionPlanListener;
import org.isf.menu.manager.Context;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class ReductionPlanBrowser extends ModalJFrame implements ReductionPlanListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
	private JButton jCloseButton;
	private JTable table;
	private ReductionPlanModel reductionPlanModel;
	private final String[] columnHeaders = new String[] {
			MessageBundle.getMessage("angal.common.code.txt"),
			MessageBundle.getMessage("angal.common.description.txt"),
			MessageBundle.getMessage("angal.reductionplan.medicalrate.col"),
			MessageBundle.getMessage("angal.reductionplan.examrate.col"),
			MessageBundle.getMessage("angal.reductionplan.operationrate.col"),
			MessageBundle.getMessage("angal.reductionplan.otherrate.col")
	};
	private final int[] columnsWidth = { 80, 200, 90, 90, 90, 90 };
	private final ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);
	List<ReductionPlan> reductionplansList;
	private ReductionPlan reductionPlan;

	/**
	 * This is the default constructor
	 */
	public ReductionPlanBrowser() {
		super();
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.reductionplan.reductionplanbrowser.title"));
		setContentPane(getJContentPane());
		setMinimumSize(new Dimension(900,400));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			table = new JTable();
			JScrollPane scrollPane = new JScrollPane();
			reductionPlanModel = new ReductionPlanModel();
			table.setModel(reductionPlanModel);
			for (int i = 0; i < columnHeaders.length; i++) {
				table.getColumnModel().getColumn(i).setMinWidth(columnsWidth[i]);
			}
			scrollPane.setViewportView(table);
			contentPane.add(scrollPane, BorderLayout.CENTER);
			contentPane.add(getButtonPane(), BorderLayout.SOUTH);
		}
		return contentPane;
	}

	/**
	 * This method initializes jButtonPanel
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPane() {
		JPanel panel = new JPanel();
		panel.add(getNewButton());
		panel.add(getEditButton());
		panel.add(getDeleteButton());
		panel.add(getCloseButton());
		return panel;
	}

	/**
	 * This method initializes jNewButton
	 * @return javax.swing.JButton
	 */
	private JButton getNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));

			jNewButton.addActionListener(actionEvent -> {
				ReductionPlanEdit reductionPlanEdit = new ReductionPlanEdit(null, true);
				reductionPlanEdit.addReductionPlanListener(ReductionPlanBrowser.this);
				reductionPlanEdit.showAsModal(ReductionPlanBrowser.this);
			});
		}
		return jNewButton;
	}

	/**
	 * This method initializes jEditButton
	 * @return javax.swing.JButton
	 */
	private JButton getEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));

			jEditButton.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(ReductionPlanBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
				} else {
					reductionPlan = (ReductionPlan) ((ReductionPlanModel)table.getModel()).getValueAt(table.getSelectedRow(), -1);
					ReductionPlanEdit reductionPlanEdit = new ReductionPlanEdit(reductionPlan, false);
					reductionPlanEdit.addReductionPlanListener(ReductionPlanBrowser.this);
					reductionPlanEdit.showAsModal(ReductionPlanBrowser.this);
				}
			});
		}
		return jEditButton;
	}

	/**
	 * This method initializes jDeleteButton
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));

			jDeleteButton.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(ReductionPlanBrowser.this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
				} else {
					try {
						reductionPlan = (ReductionPlan) ((ReductionPlanModel)table.getModel()).getValueAt(table.getSelectedRow(), -1);
						if (MessageDialog.yesNo(null, "angal.reductionplan.doyouwantdeletereductionplan.msg") == JOptionPane.YES_OPTION)
							 {
							reductionPlanManager.delete(reductionPlan);
							reductionplansList.remove(reductionPlan);
							reductionPlanModel.fireTableDataChanged();
							table.setModel(reductionPlanModel);
							table.clearSelection();
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			});
		}
		return jDeleteButton;
	}

	/**
	 * This method initializes jCloseButton
	 * @return javax.swing.JButton
	 */
	private JButton getCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}
	@Override
	public void ReductionPlanInserted(AWTEvent aEvent) {
		table.setModel(new ReductionPlanModel());
	}

	private class ReductionPlanModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public ReductionPlanModel() {
			try {
				reductionplansList = reductionPlanManager.getAll();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public int getRowCount() {
			return reductionplansList == null ? 0 : reductionplansList.size();
		}

		public String getColumnName(int c) {
			return columnHeaders[c];
		}

		public int getColumnCount() {
			return columnHeaders.length;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return reductionplansList.get(r).getId();
			} else if (c == -1) {
				return reductionplansList.get(r);
			} else if (c == 1) {
				return reductionplansList.get(r).getDescription();
			} else if (c == 2) {
				return reductionplansList.get(r).getMedicalRate();
			} else if (c == 3) {
				return reductionplansList.get(r).getExamRate();
			} else if (c == 4) {
				return reductionplansList.get(r).getOperationRate();
			} else if (c == 5) {
				return reductionplansList.get(r).getOtherRate();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}