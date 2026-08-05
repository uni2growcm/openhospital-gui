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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.stat.gui.report;

import java.time.LocalDate;

import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Generates and shows the laboratory exam list report ("Prescriber list exam"),
 * using the same filters as the LabBrowser.
 */
public class GenericReportPrescriberListExam extends DisplayReport {

	private static final String JASPER_FILE_NAME = "Prescriber_list_exam";

	private final JasperReportsManager jasperReportsManager =
			Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportPrescriberListExam(LocalDate fromDate, LocalDate toDate, String exam, Integer resultFilter,
			String patientCode, String userCode, String prescriberName, String patientName, String paidCode) {

		if (fromDate == null || toDate == null) {
			return;
		}

		try {
			JasperReportResultDto jasperReportResultDto = jasperReportsManager.getPrescriberListExamPdf(
					fromDate, toDate, exam, resultFilter, patientCode, userCode,
					prescriberName, patientName, paidCode);

			showReport(jasperReportResultDto);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}
}
