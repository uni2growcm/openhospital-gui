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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.ModalJFrame;

public class ReductionPlanBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
	private JButton jCloseButton;
	private String[] pbiColumn = new String[] {
			MessageBundle.getMessage("angal.common.code.txt"),
			MessageBundle.getMessage("angal.common.description.txt"),
			MessageBundle.getMessage("angal.reductionplan.medicalrate.col"),
			MessageBundle.getMessage("angal.reductionplan.examrate.col"),
			MessageBundle.getMessage("angal.reductionplan.operate.col"),
			MessageBundle.getMessage("angal.reductionplan.otherrate.col")
	};
	private final int[] columnsWidth = { 80, 200, 90, 90, 90, 100 };
	private final ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);
	List<ReductionPlan> reductionplansList;

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
		setMinimumSize(new Dimension(650, 400));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			JTable table = new JTable();
			JScrollPane scrollPane = new JScrollPane();
			try {
				table.setModel(new ReductionPlanModel());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			for (int i = 0; i < pbiColumn.length; i++) {
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
	 * vn This method initializes jNewButton
	 * @return javax.swing.JButton
	 */
	private JButton getNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
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

	private class ReductionPlanModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public ReductionPlanModel() throws OHServiceException {
			reductionplansList = reductionPlanManager.getAll();
		}

		public int getRowCount() {
			if (reductionplansList == null) {
				return 0;
			}
			return reductionplansList.size();
		}

		public String getColumnName(int c) {
			return pbiColumn[c];
		}

		public int getColumnCount() {
			return pbiColumn.length;
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