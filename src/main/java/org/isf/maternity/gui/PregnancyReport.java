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

import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.stat.gui.report.DisplayReport;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class PregnancyReport extends DisplayReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(PregnancyReport.class);
    private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

    public PregnancyReport(Integer pregId) {
        try {
            HospitalBrowsingManager hospitalBrowsingManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
            Hospital hospital = hospitalBrowsingManager.getHospital();

            String parametersString = "Hospital=" + hospital.getDescription();
            parametersString += "&Address=" + hospital.getAddress();
            parametersString += "&City=" + hospital.getCity();
            parametersString += "&Email=" + hospital.getEmail();
            parametersString += "&Telephone=" + hospital.getTelephone();
            parametersString += "&PregId=" + pregId;
            parametersString += "&PathLogoImage=" + getClass().getClassLoader().getResource("logo_hospital.png");

            Integer patientId = null;

            JasperReportResultDto jasperReportResultDto = jasperReportsManager.getGenericReportPatientVersion2Pdf(
                patientId,
                parametersString,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "PregnancyReport"
            );
            
            LOGGER.info("Report generated successfully. Pages: {}", 
                jasperReportResultDto != null ? "unknown" : "null result");
            
            showReport(jasperReportResultDto);
        } catch (OHServiceException e) {
            LOGGER.error("Error generating pregnancy report", e);
            OHServiceExceptionUtil.showMessages(e, null);
        } catch (Exception e) {
            LOGGER.error("Error generating pregnancy report", e);
            MessageDialog.error(null, "angal.stat.reporterror.msg");
        }
    }
}
