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
package org.isf.operation.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.opd.model.Opd;
import org.isf.operation.model.Operation;
import org.isf.operation.model.OperationRow;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhTableOperationModel;

/**
 * Standalone, self-persisting variant of {@link OperationRowOpd}: unlike {@link OperationRowOpd} (whose added/edited
 * rows only persist once a parent {@code OpdEditExtended} save fires a surgery event), this panel persists each add
 * or edit immediately, so it's safe to open on its own without any parent save flow.
 */
public class OperationOpdExtended extends OperationRowBase {

	private static final long serialVersionUID = 1L;

	private Opd myOpd;

	public OperationOpdExtended(Opd opd) {
		super();
		myOpd = opd;

		if (myOpd != null) {
			List<OperationRow> res = new ArrayList<>();
			try {
				res = operationRowBrowserManager.getOperationRowByOpd(myOpd);
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			oprowData.addAll(res);

			Patient patient = myOpd.getPatient();
			if (patient != null) {
				addPatientInfoLine(patient);
			}
		}
		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		tableData.setModel(modelOhOpeRow);
	}

	/**
	 * Prepends a one-row grid of code/name/age/sex(/blood type) cells above the operation form.
	 */
	private void addPatientInfoLine(Patient patient) {
		List<String> cells = new ArrayList<>();
		cells.add(MessageBundle.getMessage("angal.common.code.txt") + ": " + patient.getCode());
		cells.add(MessageBundle.getMessage("angal.common.name.txt") + ": " + patient.getName());
		cells.add(MessageBundle.getMessage("angal.common.age.txt") + ": " + patient.getAge());
		cells.add(MessageBundle.getMessage("angal.common.sex.txt") + ": " + patient.getSex());
		String bloodType = patient.getBloodType();
		if (bloodType != null && !bloodType.isEmpty() && !bloodType.equalsIgnoreCase(MessageBundle.getMessage("angal.common.unknown.txt"))) {
			cells.add(MessageBundle.getMessage("angal.patient.tobm") + ": " + bloodType);
		}

		JPanel infoLine = new JPanel(new GridLayout(1, cells.size()));
		infoLine.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		for (String cell : cells) {
			infoLine.add(new JLabel(cell));
		}

		Component existingNorth = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.NORTH);
		remove(existingNorth);
		JPanel northWrapper = new JPanel(new BorderLayout());
		northWrapper.add(infoLine, BorderLayout.NORTH);
		northWrapper.add(existingNorth, BorderLayout.CENTER);
		add(northWrapper, BorderLayout.NORTH);
	}

	@Override
	public void addToGrid() {
		if ((this.textDate.getLocalDateTime() == null) || (this.comboOperation.getSelectedItem() == null)) {
			MessageDialog.error(this, "angal.operationrowedit.warningdateope");
			return;
		}

		OperationRow operationRow = new OperationRow();
		operationRow.setOpDate(this.textDate.getLocalDateTime());
		if (this.comboResult.getSelectedItem() != null) {
			String opResult = operationBrowserManager.getResultDescriptionKey((String) comboResult.getSelectedItem());
			operationRow.setOpResult(opResult);
		} else {
			operationRow.setOpResult("");
		}
		try {
			operationRow.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
		} catch (NumberFormatException e) {
			operationRow.setTransUnit(0.0F);
		}
		Operation op = (Operation) this.comboOperation.getSelectedItem();
		operationRow.setOperation(op);
		if (myOpd != null) {
			operationRow.setOpd(myOpd);
		}
		operationRow.setPrescriber(MainMenu.getUser().getUserName());
		operationRow.setRemarks(textAreaRemark.getText());

		int index = tableData.getSelectedRow();
		try {
			if (index < 0) {
				OperationRow saved = operationRowBrowserManager.newOperationRow(operationRow);
				oprowData.add(saved);
			} else {
				OperationRow opeInter = oprowData.get(index);
				opeInter.setOpDate(this.textDate.getLocalDateTime());
				opeInter.setOpResult(operationRow.getOpResult());
				opeInter.setTransUnit(operationRow.getTransUnit());
				opeInter.setOperation(op);
				opeInter.setPrescriber(operationRow.getPrescriber());
				opeInter.setRemarks(operationRow.getRemarks());
				OperationRow saved = operationRowBrowserManager.updateOperationRow(opeInter);
				oprowData.set(index, saved);
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return;
		}

		modelOhOpeRow = new OhTableOperationModel<>(oprowData);
		tableData.setModel(modelOhOpeRow);
		clearForm();
	}

	// used by addToForm()
	@Override
	public List<Operation> getOperationCollection() throws OHServiceException {
		return operationBrowserManager.getOperationOpd();
	}

}
