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
package org.isf.stat.gui.report;

import static org.isf.utils.Constants.DATE_FORMAT_YYYYMMDD;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.model.FamilyPlanning;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericReportFamilyPlanningRegister extends DisplayReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportFamilyPlanningRegister.class);
    private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

    public GenericReportFamilyPlanningRegister(
            LocalDate fromDate,
            LocalDate toDate,
            String statut,
            String methodCode,
            Boolean actif,
            String sexe,
            String jasperFileName) {

        System.out.println("=== GenericReportFamilyPlanningRegister ===");
        System.out.println("fromDate = " + fromDate);
        System.out.println("toDate   = " + toDate);
        System.out.println("jasper   = " + jasperFileName);

        if (fromDate == null || toDate == null) {
            return;
        }
        try {
            JasperReportResultDto jasperReportResultDto =
                    jasperReportsManager.getGenericReportFamilyPlanningRegisterPdf(fromDate, toDate, statut, methodCode, actif, sexe, jasperFileName);
            showReport(jasperReportResultDto);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la génération du rapport", e);
            e.printStackTrace();
            MessageDialog.error(null, "angal.stat.reporterror.msg");
        }
    }

    private String compileFilename(String jasperFileName,
                                   LocalDate fromDate,
                                   LocalDate toDate) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD);

        StringBuilder fileName = new StringBuilder(jasperFileName);

        fileName.append('_')
                .append(MessageBundle.getMessage("angal.common.from.txt"))
                .append('_')
                .append(formatter.format(fromDate))
                .append('_')
                .append(MessageBundle.getMessage("angal.common.to.txt"))
                .append('_')
                .append(formatter.format(toDate));

        return fileName.toString();
    }
}