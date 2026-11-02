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
package org.isf.lab.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.lab.manager.LabRowManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryRow;
import org.isf.lab.model.PatientAnalysisRow;
import org.isf.menu.manager.Context;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

/**
 * Helper class to centralize the generation and printing of PatientAnalysisReport.
 */
public final class PatientAnalysisReportLauncher {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private PatientAnalysisReportLauncher() {
	}

	/**
	 * Prints the PatientAnalysisReport for the given Laboratory test.
	 *
	 * @param lab the Laboratory test to print
	 */
	public static void print(Laboratory lab) {
		print(lab, null, null);
	}

	/**
	 * Prints the PatientAnalysisReport for the given Laboratory test with optional override parameters and custom rows.
	 *
	 * @param lab the Laboratory test
	 * @param customParameters optional parameters to override default values
	 * @param customRows optional list of rows (e.g. from current UI state); if empty or null, rows will be fetched automatically
	 */
	public static void print(Laboratory lab, Map<String, Object> customParameters, List<PatientAnalysisRow> customRows) {
		if (lab == null) {
			return;
		}

		try {
			PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
			LabRowManager labRowManager = Context.getApplicationContext().getBean(LabRowManager.class);

			Map<String, Object> extraParameters = new HashMap<>();

			LocalDateTime examDate = lab.getLabDate();
			if (examDate != null) {
				extraParameters.put("examDate", examDate.format(DATE_TIME_FORMATTER));
				extraParameters.put("PeriodLabel", examDate.toLocalDate().format(DATE_FORMATTER));
			} else {
				extraParameters.put("examDate", "");
				extraParameters.put("PeriodLabel", "");
			}

			try {
				Hospital hosp = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospital();
				if (hosp != null) {
					extraParameters.put("Hospital", hosp.getDescription() != null ? hosp.getDescription() : "");
					extraParameters.put("Address", hosp.getAddress() != null ? hosp.getAddress() : "");
					extraParameters.put("City", hosp.getCity() != null ? hosp.getCity() : "");
					extraParameters.put("Telephone", hosp.getTelephone() != null ? hosp.getTelephone() : "");
					extraParameters.put("Email", hosp.getEmail() != null ? hosp.getEmail() : "");
				}
			} catch (Exception ignored) {
			}

			Exam exam = lab.getExam();
			extraParameters.put("examName", exam != null ? exam.getDescription() : "");

			String material = lab.getMaterial();
			if (material != null && MessageBundle.getMessage("angal.lab.undefined.txt").equals(material)) {
				material = "";
			}
			extraParameters.put("material", material != null ? material : "");

			extraParameters.put("prescriber", lab.getPrescriber() != null ? lab.getPrescriber() : "");
			extraParameters.put("note", lab.getNote() != null ? lab.getNote() : "");

			String patName = lab.getPatName();
			if ((patName == null || patName.isEmpty()) && lab.getPatient() != null) {
				patName = lab.getPatient().getName();
			}
			extraParameters.put("patientName", patName != null ? patName : "");

			String patCode = "";
			if (lab.getPatient() != null && lab.getPatient().getCode() != null) {
				patCode = String.valueOf(lab.getPatient().getCode());
			}
			extraParameters.put("patientCode", patCode);

			Integer age = lab.getAge();
			if (age == null && lab.getPatient() != null) {
				age = lab.getPatient().getAge();
			}
			extraParameters.put("patientAge", age != null ? String.valueOf(age) : "");

			String sex = lab.getSex();
			if ((sex == null || sex.isEmpty()) && lab.getPatient() != null) {
				sex = String.valueOf(lab.getPatient().getSex());
			}
			extraParameters.put("patientSex", sex != null ? sex : "");

			String inOut = lab.getInOutPatient();
			extraParameters.put("patientType", inOut != null ? inOut : "");

			if (customParameters != null) {
				extraParameters.putAll(customParameters);
			}

			List<PatientAnalysisRow> rows = new ArrayList<>();
			if (customRows != null && !customRows.isEmpty()) {
				rows.addAll(customRows);
			} else {
				int proc = exam != null && exam.getProcedure() != null ? exam.getProcedure() : 1;
				if (proc == 2) {
					if (lab.getCode() != null) {
						try {
							List<LaboratoryRow> dbRows = labRowManager.getLabRowByLabId(lab.getCode());
							if (dbRows != null) {
								for (LaboratoryRow lr : dbRows) {
									String desc = lr.getDescription();
									String val = lr.getResValue();
									if (desc != null && desc.contains("$")) {
										int idx = desc.indexOf("$");
										val = desc.substring(idx + 1);
										desc = desc.substring(0, idx);
									}
									rows.add(new PatientAnalysisRow(desc, val != null ? val : ""));
								}
							}
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
				} else {
					String desc = exam != null ? exam.getDescription() : "";
					String res = lab.getResult() != null ? lab.getResult() : "";
					rows.add(new PatientAnalysisRow(desc, res));
				}
			}

			if (rows.isEmpty()) {
				String desc = exam != null ? exam.getDescription() : "";
				String res = lab.getResult() != null ? lab.getResult() : "";
				rows.add(new PatientAnalysisRow(desc, res));
			}

			printManager.print("PatientAnalysisReport", rows, extraParameters, 0);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
