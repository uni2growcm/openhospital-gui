/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;

import org.isf.generaldata.GeneralData;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.mortuary.model.Death;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRParameter;

/**
 * Generates and prints a death certificate for a selected mortuary record.
 */
public final class DeathCertificateReportLauncher {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeathCertificateReportLauncher.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private DeathCertificateReportLauncher() {
	}

	public static void print(Death death) {
		if (death == null) {
			return;
		}

		try {
			Map<String, Object> parameters = new HashMap<>();
			Hospital hospital = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospital();
			if (hospital != null) {
				parameters.put("Hospital", hospital.getDescription());
				parameters.put("Address", hospital.getAddress());
				parameters.put("City", hospital.getCity());
				parameters.put("Telephone", hospital.getTelephone());
				parameters.put("Email", hospital.getEmail());
			}
			parameters.put("CertificateDate", LocalDate.now().format(DATE_FORMATTER));
			parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, loadReportBundle());

			PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
			printManager.print("certificateOfDeath", List.of(death), parameters, PrintManager.toDisplay);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (IOException e) {
			LOGGER.error("Unable to load the death certificate translations", e);
		}
	}

	private static PropertyResourceBundle loadReportBundle() throws IOException {
		File localizedBundle = new File("rpt_base/certificateOfDeath_" + GeneralData.LANGUAGE + ".properties");
		File bundle = localizedBundle.isFile() ? localizedBundle : new File("rpt_base/certificateOfDeath.properties");
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(bundle), StandardCharsets.UTF_8)) {
			return new PropertyResourceBundle(reader);
		}
	}
}
