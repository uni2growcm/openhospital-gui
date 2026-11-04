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

import java.io.IOException;

import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints the list of the patients grouped by partner, with the reduction plans they benefit from.
 */
public class PatientsByPartnerReport extends DisplayReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientsByPartnerReport.class);

	public static final String REPORT_NAME = "PatientsByPartner";

	private final JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	/**
	 * @param partnerId the partner to print, {@code null} for all partners
	 * @param reductionPlanId only the patients benefiting from this reduction plan, {@code null} for no filter
	 */
	public PatientsByPartnerReport(Integer partnerId, Integer reductionPlanId) {
		try {
			JasperReportResultDto result = jasperReportsManager.getPatientsByPartnerPdf(partnerId, reductionPlanId, REPORT_NAME);
			showReport(result);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}
}
