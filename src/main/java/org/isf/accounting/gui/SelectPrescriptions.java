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
package org.isf.accounting.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.OperationRow;
import org.isf.patient.model.Patient;
import org.isf.therapy.manager.TherapyManager;
import org.isf.therapy.model.TherapyRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Lists a patient's outstanding prescriptions (medicals not yet fully billed, exams and operations
 * not yet billed) across three sections, and lets the user select which ones to add to a bill.
 */
public class SelectPrescriptions extends JDialog {

	private static final long serialVersionUID = 1L;

	public static final String MEDICAL_GROUP_CODE = "MED";
	public static final String EXAM_GROUP_CODE = "EXA";
	public static final String OPERATION_GROUP_CODE = "OPE";

	private static final int QTY_TO_BILL_COLUMN = 4;

	private final TherapyManager therapyManager = Context.getApplicationContext().getBean(TherapyManager.class);
	private final LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private final OperationRowBrowserManager operationRowManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	private final EventListenerList listenerList = new EventListenerList();

	private TherapyTableModel therapyTableModel;
	private JTable therapyTable;
	private DefaultTableModel labTableModel;
	private JTable labTable;
	private DefaultTableModel operationTableModel;
	private JTable operationTable;

	public interface PrescriptionSelectionListener extends EventListener {

		void prescriptionSelected(List<SelectedPrescription> prescriptions);
	}

	/**
	 * A prescription line selected by the user, ready to be converted into a {@code BillItems} by the
	 * caller (which owns the bill's own price lookup).
	 */
	public static final class SelectedPrescription {

		private final String itemGroup;
		private final String itemCode;
		private final int prescriptionId;
		private final String description;
		private final int quantity;

		public SelectedPrescription(String itemGroup, String itemCode, int prescriptionId, String description, int quantity) {
			this.itemGroup = itemGroup;
			this.itemCode = itemCode;
			this.prescriptionId = prescriptionId;
			this.description = description;
			this.quantity = quantity;
		}

		public String getItemGroup() {
			return itemGroup;
		}

		public String getItemCode() {
			return itemCode;
		}

		public int getPrescriptionId() {
			return prescriptionId;
		}

		public String getDescription() {
			return description;
		}

		public int getQuantity() {
			return quantity;
		}
	}

	public SelectPrescriptions(JDialog owner, Patient patient) {
		super(owner, MessageBundle.getMessage("angal.newbill.prescription.btn"), true);

		List<TherapyRow> therapyRows;
		List<Laboratory> laboratoryRows;
		List<OperationRow> operationRows;
		try {
			therapyRows = therapyManager.getOutstandingTherapyRows(patient.getCode());
			laboratoryRows = labManager.getOutstandingLaboratory(patient);
			operationRows = operationRowManager.getOutstandingOperationRows(patient);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, owner);
			therapyRows = new ArrayList<>();
			laboratoryRows = new ArrayList<>();
			operationRows = new ArrayList<>();
		}

		JPanel sections = new JPanel();
		sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));

		if (!therapyRows.isEmpty()) {
			sections.add(buildTherapySection(therapyRows));
		}
		if (!laboratoryRows.isEmpty()) {
			sections.add(buildLaboratorySection(laboratoryRows));
		}
		if (!operationRows.isEmpty()) {
			sections.add(buildOperationSection(operationRows));
		}

		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.addActionListener(actionEvent -> dispose());

		JButton validateButton = new JButton(MessageBundle.getMessage("angal.therapy.validateselection"));
		validateButton.addActionListener(actionEvent -> fireSelectedPrescriptions());

		JPanel buttons = new JPanel();
		buttons.add(cancelButton);
		buttons.add(validateButton);

		setLayout(new BorderLayout());
		add(new JScrollPane(sections), BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
		setSize(new Dimension(700, 500));
		setLocationRelativeTo(owner);
	}

	public void addPrescriptionSelectedListener(PrescriptionSelectionListener listener) {
		listenerList.add(PrescriptionSelectionListener.class, listener);
	}

	private void fireSelectedPrescriptions() {
		List<SelectedPrescription> selected = new ArrayList<>();

		if (therapyTable != null) {
			for (int row : therapyTable.getSelectedRows()) {
				int qty = therapyTableModel.getQtyToBill(row);
				if (qty <= 0) {
					continue;
				}
				TherapyRow therapyRow = therapyTableModel.getRow(row);
				selected.add(new SelectedPrescription(MEDICAL_GROUP_CODE, String.valueOf(therapyRow.getMedical()), therapyRow.getTherapyID(),
					resolveMedicalDescription(therapyRow.getMedical()), qty));
			}
		}
		addSelectedFrom(labTable, selected);
		addSelectedFrom(operationTable, selected);

		if (selected.isEmpty()) {
			MessageDialog.error(this, "angal.billbrowser.pleaseselectatleestonerow");
			return;
		}

		for (PrescriptionSelectionListener listener : listenerList.getListeners(PrescriptionSelectionListener.class)) {
			listener.prescriptionSelected(selected);
		}
		dispose();
	}

	private void addSelectedFrom(JTable table, List<SelectedPrescription> selected) {
		if (table == null) {
			return;
		}
		if (table.getModel() instanceof SourceRowTableModel) {
			SourceRowTableModel model = (SourceRowTableModel) table.getModel();
			for (int row : table.getSelectedRows()) {
				selected.add(model.toSelectedPrescription(row));
			}
		}
	}

	private JPanel buildTherapySection(List<TherapyRow> rows) {
		therapyTableModel = new TherapyTableModel(rows);
		therapyTable = new JTable(therapyTableModel);
		return buildSection(MessageBundle.getMessage("angal.selectprescription.medicallist"), therapyTable);
	}

	private JPanel buildLaboratorySection(List<Laboratory> rows) {
		labTableModel = new LaboratoryTableModel(rows);
		labTable = new JTable(labTableModel);
		return buildSection(MessageBundle.getMessage("angal.selectprescription.examslist"), labTable);
	}

	private JPanel buildOperationSection(List<OperationRow> rows) {
		operationTableModel = new OperationTableModel(rows);
		operationTable = new JTable(operationTableModel);
		return buildSection(MessageBundle.getMessage("angal.selectprescription.operationslist"), operationTable);
	}

	private JPanel buildSection(String title, JTable table) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(javax.swing.BorderFactory.createTitledBorder(title));

		JCheckBox selectAll = new JCheckBox(MessageBundle.getMessage("angal.selectprescription.selectall"));
		selectAll.addItemListener(itemEvent -> {
			if (selectAll.isSelected()) {
				table.selectAll();
			} else {
				table.clearSelection();
			}
		});

		panel.add(selectAll, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
		return panel;
	}

	private String resolveMedicalDescription(Integer medicalId) {
		try {
			Medical medical = medicalBrowsingManager.getMedical(medicalId);
			return medical != null ? medical.getDescription() : String.valueOf(medicalId);
		} catch (OHServiceException e) {
			return String.valueOf(medicalId);
		}
	}

	/**
	 * A read-only table model backing a source-record section (exams/operations), able to convert a
	 * selected row back into a {@link SelectedPrescription}.
	 */
	private interface SourceRowTableModel {

		SelectedPrescription toSelectedPrescription(int row);
	}

	private final class TherapyTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private final List<TherapyRow> rows;

		TherapyTableModel(List<TherapyRow> rows) {
			super(new Object[] {
				MessageBundle.getMessage("angal.common.date.txt"),
				MessageBundle.getMessage("angal.newbill.medical.title"),
				MessageBundle.getMessage("angal.common.quantity.txt"),
				MessageBundle.getMessage("angal.newbill.prescription.remaining"),
				MessageBundle.getMessage("angal.newbill.prescription.qtytobill")
			}, 0);
			this.rows = rows;
			for (TherapyRow row : rows) {
				int remaining = (int) row.getRemainingQty();
				addRow(new Object[] { row.getStartDate(), resolveMedicalDescription(row.getMedical()), row.getQty(), remaining, remaining });
			}
		}

		TherapyRow getRow(int index) {
			return rows.get(index);
		}

		int getQtyToBill(int index) {
			return (Integer) getValueAt(index, QTY_TO_BILL_COLUMN);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == QTY_TO_BILL_COLUMN;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column != QTY_TO_BILL_COLUMN) {
				super.setValueAt(value, row, column);
				return;
			}
			int remaining = (int) rows.get(row).getRemainingQty();
			int qty;
			try {
				qty = Integer.parseInt(value.toString().trim());
			} catch (NumberFormatException nfe) {
				return;
			}
			super.setValueAt(Math.max(0, Math.min(qty, remaining)), row, column);
		}
	}

	private final class LaboratoryTableModel extends DefaultTableModel implements SourceRowTableModel {

		private static final long serialVersionUID = 1L;

		private final List<Laboratory> rows;

		LaboratoryTableModel(List<Laboratory> rows) {
			super(new Object[] { MessageBundle.getMessage("angal.common.date.txt"), MessageBundle.getMessage("angal.lab.exam") }, 0);
			this.rows = rows;
			for (Laboratory row : rows) {
				addRow(new Object[] { row.getLabDate(), row.getExam().getDescription() });
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public SelectedPrescription toSelectedPrescription(int row) {
			Laboratory laboratory = rows.get(row);
			return new SelectedPrescription(EXAM_GROUP_CODE, laboratory.getExam().getCode(), laboratory.getCode(), laboratory.getExam().getDescription(), 1);
		}
	}

	private final class OperationTableModel extends DefaultTableModel implements SourceRowTableModel {

		private static final long serialVersionUID = 1L;

		private final List<OperationRow> rows;

		OperationTableModel(List<OperationRow> rows) {
			super(new Object[] { MessageBundle.getMessage("angal.common.date.txt"), MessageBundle.getMessage("angal.newbill.operation.title") }, 0);
			this.rows = rows;
			for (OperationRow row : rows) {
				addRow(new Object[] { row.getOpDate(), row.getOperation().getDescription() });
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public SelectedPrescription toSelectedPrescription(int row) {
			OperationRow operationRow = rows.get(row);
			return new SelectedPrescription(OPERATION_GROUP_CODE, operationRow.getOperation().getCode(), operationRow.getId(),
				operationRow.getOperation().getDescription(), 1);
		}
	}
}
