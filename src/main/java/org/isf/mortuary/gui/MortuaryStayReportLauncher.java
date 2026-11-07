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
package org.isf.mortuary.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.mortuary.model.Death;
import org.isf.mortuary.model.DeathStayRow;
import org.isf.mortuarystays.manager.MortuaryStayManager;
import org.isf.mortuarystays.model.MortuaryStay;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Helper class to centralize the generation and printing of rapportEtatSejoursMorgue.
 */
public final class MortuaryStayReportLauncher {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private MortuaryStayReportLauncher() {
	}

	/**
	 * Computes the list of stay rows based on entry date, discharge date, and stay tiers.
	 *
	 * @param dateEntree admission/entry date
	 * @param dateSortie release/discharge date
	 * @param plages list of MortuaryStay tiers
	 * @return calculated list of DeathStayRow, or null if validation fails
	 */
	public static List<DeathStayRow> computeDeathStayRows(LocalDateTime dateEntree, LocalDateTime dateSortie, List<MortuaryStay> plages) {
		if (dateEntree == null || dateSortie == null) {
			return null;
		}

		if (dateSortie.toLocalDate().isBefore(dateEntree.toLocalDate())) {
			MessageDialog.error(null, "angal.opd.datefrommustbebefordateto.msg");
			return null;
		}

		long days = ChronoUnit.DAYS.between(dateEntree.toLocalDate(), dateSortie.toLocalDate()) + 1;
		int n = (int) days;

		if (plages == null || plages.isEmpty()) {
			MessageDialog.info(null, "angal.distype.nondefineprice");
			return null;
		}

		List<MortuaryStay> sortedPlages = new ArrayList<>(plages);
		sortedPlages.sort(Comparator.comparingInt(MortuaryStay::getMinDays));

		int min0 = 1;
		int i = 0;
		List<DeathStayRow> rows = new ArrayList<>();

		while (n > 0 && i < sortedPlages.size()) {
			MortuaryStay plage = sortedPlages.get(i);
			int k = i + 1;
			int nb = plage.getMaxDays() - plage.getMinDays() + 1;

			if (min0 < plage.getMinDays() && i == 0) {
				MessageDialog.error(
						null,
						MessageBundle.getMessage("angal.distype.nondefineprice") + " "
								+ MessageBundle.getMessage("angal.distype.for") + " " + min0 + " "
								+ MessageBundle.getMessage("angal.mortuarybrowser.to") + " " + (plage.getMinDays() - 1) + " "
								+ MessageBundle.getMessage("angal.distype.days")
				);
				return null;
			}

			if (k < sortedPlages.size()) {
				MortuaryStay plage2 = sortedPlages.get(k);
				if (plage.getMaxDays() < (plage2.getMinDays() - 1)) {
					if ((plage2.getMinDays() - 1) == (plage.getMaxDays() + 1)) {
						MessageDialog.error(
								null,
								MessageBundle.getMessage("angal.distype.nondefineprice") + " "
										+ MessageBundle.getMessage("angal.distype.for") + " " + (plage2.getMinDays() - 1) + " "
										+ MessageBundle.getMessage("angal.distype.days")
						);
						return null;
					} else {
						MessageDialog.error(
								null,
								MessageBundle.getMessage("angal.distype.nondefineprice") + " "
										+ MessageBundle.getMessage("angal.distype.for") + " " + (plage.getMaxDays() + 1) + " "
										+ MessageBundle.getMessage("angal.mortuarybrowser.to") + " " + (plage2.getMinDays() - 1) + " "
										+ MessageBundle.getMessage("angal.distype.days")
						);
						return null;
					}
				}
			}

			String desc = plage.getDescription() + " "
					+ MessageBundle.getMessage("angal.mortuarybrowser.from") + " " + plage.getMinDays() + " "
					+ MessageBundle.getMessage("angal.mortuarybrowser.to") + " " + plage.getMaxDays() + " "
					+ MessageBundle.getMessage("angal.mortuarybrowser.days");

			if (n <= nb) {
				rows.add(new DeathStayRow(desc, 0.0f, n, 0.0f));
				n = 0;
				i++;
			} else {
				rows.add(new DeathStayRow(desc, 0.0f, nb, 0.0f));
				n = n - nb;
				int j = i + 1;
				if (j == sortedPlages.size() && n > 0) {
					MessageDialog.error(
							null,
							MessageBundle.getMessage("angal.distype.nondefinepricemore") + " " + plage.getMaxDays() + " "
									+ MessageBundle.getMessage("angal.distype.days")
					);
					return null;
				}
				i++;
			}
		}

		return rows;
	}

	/**
	 * Prints the rapportEtatSejoursMorgue for the given Death entry.
	 *
	 * @param death the Death record to print stay report for
	 */
	public static void print(Death death) {
		print(death, null, null);
	}

	/**
	 * Prints the rapportEtatSejoursMorgue for the given Death record with optional parameters and rows.
	 *
	 * @param death the Death record
	 * @param customParameters optional parameters to override default values
	 * @param customRows optional list of rows; if null, rows will be computed automatically
	 */
	public static void print(Death death, Map<String, Object> customParameters, List<DeathStayRow> customRows) {
		if (death == null) {
			return;
		}

		LocalDateTime dateSortie = death.getDischargeDate() != null
				? death.getDischargeDate()
				: death.getEstimatedDischargeDate();
		if (dateSortie == null) {
			MessageDialog.warning(null, "angal.mortuaryedit.providedateleaving");
			return;
		}

		LocalDateTime dateEntree = death.getAdmissionDate() != null
				? death.getAdmissionDate()
				: death.getDate();
		if (dateEntree == null) {
			MessageDialog.warning(null, "angal.mortuary.pleaseinsertanadmissiondate.msg");
			return;
		}

		List<DeathStayRow> rows;
		if (customRows != null) {
			rows = customRows;
		} else {
			try {
				MortuaryStayManager mortuaryStayManager = Context.getApplicationContext().getBean(MortuaryStayManager.class);
				List<MortuaryStay> plages = mortuaryStayManager.getAll(false);
				rows = computeDeathStayRows(dateEntree, dateSortie, plages);
				if (rows == null) {
					return;
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				return;
			}
		}

		int nbjours = 0;
		float total = 0f;
		for (DeathStayRow r : rows) {
			if (r.getNombre_jours() != null) {
				nbjours += r.getNombre_jours();
			}
			if (r.getMontant() != null) {
				total += r.getMontant();
			}
		}

		Map<String, Object> extraParameters = new HashMap<>();
		extraParameters.put("deathID", String.valueOf(death.getId()));
		String patName = death.getPatient() != null ? death.getPatient().getName() : "";
		extraParameters.put("PAT_NAME", patName);
		extraParameters.put("DECES_DATE", dateEntree.format(DATE_FORMATTER));
		extraParameters.put("DECES_DATE_SORTIE", dateSortie.format(DATE_FORMATTER));
		extraParameters.put("TOTAL_JOURS", nbjours);
		extraParameters.put("TOTAL_MONTANT", total);
		extraParameters.put("PeriodLabel", dateEntree.format(DATE_FORMATTER) + " - " + dateSortie.format(DATE_FORMATTER));

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

		try {
			java.io.File bundleFile = new java.io.File("rpt_base/rapportEtatSejoursMorgue_" + org.isf.generaldata.GeneralData.LANGUAGE + ".properties");
			if (!bundleFile.exists()) {
				bundleFile = new java.io.File("rpt_base/rapportEtatSejoursMorgue.properties");
			}
			if (bundleFile.exists()) {
				try (java.io.FileInputStream fis = new java.io.FileInputStream(bundleFile)) {
					extraParameters.put(
							net.sf.jasperreports.engine.JRParameter.REPORT_RESOURCE_BUNDLE,
							new java.util.PropertyResourceBundle(new java.io.InputStreamReader(fis, java.nio.charset.StandardCharsets.UTF_8))
					);
				}
			}
		} catch (Exception ignored) {
		}

		if (customParameters != null) {
			extraParameters.putAll(customParameters);
		}

		try {
			PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
			printManager.print("rapportEtatSejoursMorgue", rows, extraParameters, PrintManager.toDisplay);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
