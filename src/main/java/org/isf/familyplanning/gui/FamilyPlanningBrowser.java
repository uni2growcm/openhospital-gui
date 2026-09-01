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
package org.isf.familyplanning.gui;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.familyplanning.manager.FamilyPlanningBrowserManager;
import org.isf.familyplanning.model.FamilyPlanningRecord;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportFamilyPlanning;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Main entry point of the family planning module: lists every record (across all patients, most recent
 * first) and gives access to a new consultation and to the printable register.
 */
public class FamilyPlanningBrowser extends ModalJFrame implements SelectionListener {

	private static final long serialVersionUID = 1L;

	private List<FamilyPlanningRecord> records;
	private final String[] columns = {
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.visitdate.txt").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.method.txt").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.reason.txt").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.nextappointment.txt").toUpperCase()
	};
	private final int[] columnWidth = { 220, 100, 140, 140, 100 };

	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JTable jTable;
	private FamilyPlanningBrowserModel model;
	private final FamilyPlanningBrowserManager familyPlanningManager =
			Context.getApplicationContext().getBean(FamilyPlanningBrowserManager.class);
	private final JFrame myFrame;

	public FamilyPlanningBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.familyplanning.familyplanningbrowser.title"));
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
			jButtonPanel.add(getJRegisterButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		JButton jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		jNewButton.addActionListener(actionEvent -> {
			SelectPatient sp = new SelectPatient(myFrame, new Patient());
			sp.addSelectionListener(this);
			sp.pack();
			sp.setVisible(true);
		});
		return jNewButton;
	}

	private JButton getJEditButton() {
		JButton jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		jEditButton.addActionListener(actionEvent -> {
			if (jTable.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				FamilyPlanningRecord record = (FamilyPlanningRecord) model.getValueAt(jTable.getSelectedRow(), -1);
				FamilyPlanningEdit edit = new FamilyPlanningEdit(myFrame, record.getPatient(), record);
				edit.setOnSave(this::refresh);
				edit.setVisible(true);
			}
		});
		return jEditButton;
	}

	private JButton getJRegisterButton() {
		JButton jRegisterButton = new JButton(MessageBundle.getMessage("angal.familyplanning.printregister.btn"));
		jRegisterButton.addActionListener(actionEvent -> {
			GoodDateChooser fromChooser = new GoodDateChooser(LocalDate.now().withDayOfMonth(1), false, false);
			GoodDateChooser toChooser = new GoodDateChooser(LocalDate.now(), false, false);
			JPanel panel = new JPanel();
			panel.add(new javax.swing.JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			panel.add(fromChooser);
			panel.add(new javax.swing.JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			panel.add(toChooser);
			int result = javax.swing.JOptionPane.showConfirmDialog(myFrame, panel,
					MessageBundle.getMessage("angal.familyplanning.printregister.btn"), javax.swing.JOptionPane.OK_CANCEL_OPTION);
			if (result == javax.swing.JOptionPane.OK_OPTION) {
				LocalDateTime dateFrom = fromChooser.getDateStartOfDay();
				LocalDateTime dateTo = toChooser.getDateEndOfDay();
				new GenericReportFamilyPlanning(dateFrom, dateTo);
			}
		});
		return jRegisterButton;
	}

	private JButton getJCloseButton() {
		JButton jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		jCloseButton.addActionListener(actionEvent -> dispose());
		return jCloseButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new FamilyPlanningBrowserModel();
			jTable = new JTable(model);
			for (int i = 0; i < columnWidth.length; i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
			}
		}
		return jTable;
	}

	private void refresh() {
		model = new FamilyPlanningBrowserModel();
		jTable.setModel(model);
		jTable.updateUI();
	}

	class FamilyPlanningBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		FamilyPlanningBrowserModel() {
			try {
				records = familyPlanningManager.getByDateRange(LocalDate.now().minusYears(5), LocalDate.now().plusYears(1));
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			return records == null ? 0 : records.size();
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
			FamilyPlanningRecord record = records.get(r);
			switch (c) {
				case -1:
					return record;
				case 0:
					return record.getPatient().getFirstName() + ' ' + record.getPatient().getSecondName();
				case 1:
					return record.getVisitDate();
				case 2:
					return record.getMethod();
				case 3:
					return record.getReason();
				case 4:
					return record.getNextAppointmentDate();
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
	public void patientSelected(Patient patient) {
		FamilyPlanningEdit edit = new FamilyPlanningEdit(myFrame, patient, null);
		edit.setOnSave(this::refresh);
		edit.setVisible(true);
	}
}
