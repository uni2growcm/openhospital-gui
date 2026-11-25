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
package org.isf.maternity.gui;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Opens the patient examination ("parameters": weight, height, blood pressure, temperature, ...)
 * from the maternity visit dialogs (pregnancy, family planning and HIV visits), so that these
 * values are recorded once in the patient examination instead of in each visit form.
 */
final class VisitExaminationLauncher {

	private VisitExaminationLauncher() {
	}

	/**
	 * @return {@code true} if the current user is allowed to open the patient examination
	 */
	static boolean isExaminationAllowed() {
		return MainMenu.checkUserGrants("btnadmexamination");
	}

	/**
	 * Builds the "parameters" button opening the patient examination of the given patient.
	 *
	 * @param owner the visit dialog
	 * @param patient the patient of the visit
	 * @return the button
	 */
	static JButton createExaminationButton(JDialog owner, Patient patient) {
		JButton button = new JButton(MessageBundle.getMessage("angal.admission.examination.btn"));
		button.setMnemonic(MessageBundle.getMnemonic("angal.admission.examination.btn.key"));
		button.addActionListener(e -> openExamination(owner, patient));
		return button;
	}

	/**
	 * Opens the examination of the patient. The visit dialog is disabled until the examination is closed.
	 *
	 * @param owner the visit dialog
	 * @param patient the patient of the visit
	 */
	static void openExamination(JDialog owner, Patient patient) {
		if (patient == null) {
			MessageDialog.error(owner, "angal.common.pleaseselectapatient.msg");
			return;
		}

		ExaminationBrowserManager examinationBrowserManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
		PatientExamination lastPatex = null;
		try {
			lastPatex = examinationBrowserManager.getLastByPatID(patient.getCode());
		} catch (OHServiceException ex) {
			OHServiceExceptionUtil.showMessages(ex);
		}
		PatientExamination patex = lastPatex != null
			? examinationBrowserManager.getFromLastPatientExamination(lastPatex)
			: examinationBrowserManager.getDefaultPatientExamination(patient);
		GenderPatientExamination gpatex = new GenderPatientExamination(patex, patient.getSex() == 'M');

		PatientExaminationEdit dialog = new PatientExaminationEdit(owner, gpatex);
		// the visit dialog is modal: exclude the examination frame from its modality to keep it usable
		dialog.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		owner.setEnabled(false);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				owner.setEnabled(true);
				owner.toFront();
			}
		});
		dialog.setVisible(true);
		dialog.toFront();
	}
}