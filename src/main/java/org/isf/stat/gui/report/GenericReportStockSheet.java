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
package org.isf.stat.gui.report;

import java.time.LocalDateTime;
import java.util.List;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the "stock sheet" (fiche de stock) report, either for a single medical or, combined into
 * one document, for every medical of a category.
 */
public class GenericReportStockSheet extends DisplayReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportStockSheet.class);
	private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportStockSheet(Medical medical, LocalDateTime dateFrom, LocalDateTime dateTo) {
		try {
			JasperReportResultDto result = jasperReportsManager.getStockSheetReport(medical, dateFrom, dateTo);
			showReport(result);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	public GenericReportStockSheet(List<Medical> medicals, LocalDateTime dateFrom, LocalDateTime dateTo) {
		try {
			JasperReportResultDto result = jasperReportsManager.getStockSheetReportForMedicals(medicals, dateFrom, dateTo);
			showReport(result);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

}
