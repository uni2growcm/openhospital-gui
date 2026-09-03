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
package org.isf.pregnancy.gui;

import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.gui.report.DisplayReport;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints the two birth documents for the CPN delivery workflow, reusing the same JasperReports mechanism
 * already used elsewhere in the application for single-record documents (e.g. {@code GenericReportBill},
 * {@code GenericReportAdmission}): the declaration of birth, one per {@code PregnancyNewborn} (each child
 * is declared individually), and the certificate of declaration, one per {@code PregnancyDelivery} (it
 * only carries mother/delivery-level data, so it is issued once for the whole birth event).
 */
public class BirthDocumentPrinter extends DisplayReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(BirthDocumentPrinter.class);

	private final JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public void printDeclarationOfBirth(int newbornId) {
		try {
			JasperReportResultDto result = jasperReportsManager.getDeclarationOfBirthPdf(newbornId, "declarationOfBirth");
			showReport(result);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	public void printCertificateOfDeclaration(int deliveryId) {
		try {
			JasperReportResultDto result = jasperReportsManager.getCertificateOfDeclarationPdf(deliveryId, "certificateOfDeclaration");
			showReport(result);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}
}
