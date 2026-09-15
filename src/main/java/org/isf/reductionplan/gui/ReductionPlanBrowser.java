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
package org.isf.reductionplan.gui;

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
import org.isf.reductionplan.gui.ReductionPlanEdit.ReductionPlanListener;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class ReductionPlanBrowser extends ModalJFrame implements ReductionPlanListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void reductionPlanInserted(AWTEvent e) {
		reloadData();
	}

	@Override
	public void reductionPlanUpdated(AWTEvent e) {
		reloadData();
	}

	private void reloadData() {
		try {
			reductionPlanArray = reductionPlanManager.getAll();
		} catch (OHServiceException ex) {
			OHServiceExceptionUtil.showMessages(ex);
		}
		jTableReductionPlans.setModel(new ReductionPlanBrowserModel());
	}

	private JTable jTableReductionPlans;
	private JScrollPane jScrollPaneTable;
	private JButton jButtonNew;
	private JPanel jPanelButtons;
	private JButton jButtonEdit;
	private JButton jButtonDelete;
	private JButton jButtonClose;

	private final String[] columnNames = {
		MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.medicalrate.label").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.examrate.label").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.operationrate.label").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.otherrate.label").toUpperCase()
	};
	private final int[] columnWidth = { 250, 100, 100, 100, 100 };

	private ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);

	private List<ReductionPlan> reductionPlanArray;
	private JFrame myFrame;

	public ReductionPlanBrowser() {
		myFrame = this;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJScrollPaneTable(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.reductionplan.title"));
	}

	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(getJTableReductionPlans());
		}
		return jScrollPaneTable;
	}

	private JTable getJTableReductionPlans() {
		if (jTableReductionPlans == null) {
			jTableReductionPlans = new JTable();
			jTableReductionPlans.setModel(new ReductionPlanBrowserModel());

			for (int i = 0; i < columnWidth.length; i++) {
				jTableReductionPlans.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
			}
			jTableReductionPlans.setAutoCreateColumnsFromModel(false);
		}
		return jTableReductionPlans;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonNew());
			jPanelButtons.add(getJButtonEdit());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				ReductionPlanEdit editDialog = new ReductionPlanEdit(myFrame, new ReductionPlan(), true);
				editDialog.addReductionPlanListener(this);
				editDialog.setVisible(true);
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				int selectedRow = jTableReductionPlans.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(this, "angal.reductionplan.pleaseselectaplantoedit.msg");
					return;
				}
				ReductionPlan reductionPlan = reductionPlanArray.get(selectedRow);
				ReductionPlanEdit editDialog = new ReductionPlanEdit(myFrame, reductionPlan, false);
				editDialog.addReductionPlanListener(this);
				editDialog.setVisible(true);
			});
		}
		return jButtonEdit;
	}

	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				int selectedRow = jTableReductionPlans.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(this, "angal.reductionplan.pleaseselectaplantodelete.msg");
					return;
				}
				ReductionPlan reductionPlan = reductionPlanArray.get(selectedRow);
				int answer = MessageDialog.yesNo(this, "angal.reductionplan.deletereductionplan.msg");
				if (answer == JOptionPane.OK_OPTION) {
					try {
						reductionPlanManager.delete(reductionPlan);
						reloadData();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
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

	class ReductionPlanBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		ReductionPlanBrowserModel() {
			try {
				reductionPlanArray = reductionPlanManager.getAll();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			return reductionPlanArray == null ? 0 : reductionPlanArray.size();
		}

		@Override
		public String getColumnName(int c) {
			return columnNames[c];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			ReductionPlan reductionPlan = reductionPlanArray.get(r);
			return switch (c) {
				case 0 -> reductionPlan.getDescription();
				case 1 -> reductionPlan.getMedicalRate();
				case 2 -> reductionPlan.getExamRate();
				case 3 -> reductionPlan.getOperationRate();
				case 4 -> reductionPlan.getOtherRate();
				default -> null;
			};
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
