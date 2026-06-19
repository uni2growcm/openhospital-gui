package org.isf.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.isf.hiv.model.HIVInfant;
import org.isf.patient.model.Patient;

public class ExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void exportHIVInfantsToExcel(List<HIVInfant> infants, File file) throws IOException {
        if (infants == null || infants.isEmpty()) {
            throw new IOException("No data to export");
        }

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("HIV Follow Up");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {
                    "ID",
                    "Patient Code",
                    "Patient Name",
                    "Age (months)",
                    "Sex",
                    "Status",
                    "Feeding Type",
                    "Registration Date",
                    "Birth Weight (kg)",
                    "Gestational Age (weeks)",
                    "Mother Name",
                    "Mother Code",
                    "Follow-up Start Date",
                    "Follow-up End Date",
                    "Notes"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (HIVInfant infant : infants) {
                Patient patient = infant.getPatient();
                Patient mother = infant.getMother();

                Row row = sheet.createRow(rowNum++);

                if (infant.getId() != null) {
                    row.createCell(0).setCellValue(infant.getId().intValue());
                } else {
                    row.createCell(0).setCellValue("");
                }

                // Patient Code
                if (patient != null && patient.getCode() != null) {
                    row.createCell(1).setCellValue(patient.getCode().intValue());
                } else {
                    row.createCell(1).setCellValue("");
                }

                // Patient Name
                if (patient != null && patient.getName() != null) {
                    row.createCell(2).setCellValue(patient.getName());
                } else {
                    row.createCell(2).setCellValue("");
                }

                // Age
                if (patient != null) {
                    row.createCell(3).setCellValue(patient.getAge());
                } else {
                    row.createCell(3).setCellValue(0);
                }

                // Sex
                if (patient != null) {
                    row.createCell(4).setCellValue(String.valueOf(patient.getSex()));
                } else {
                    row.createCell(4).setCellValue("");
                }

                // Status
                if (infant.getStatus() != null) {
                    row.createCell(5).setCellValue(infant.getStatus().getDescription());
                } else {
                    row.createCell(5).setCellValue("");
                }

                // Feeding Type
                if (infant.getFeedingType() != null) {
                    row.createCell(6).setCellValue(infant.getFeedingType().getDescription());
                } else {
                    row.createCell(6).setCellValue("");
                }

                // Registration Date
                if (infant.getRegistrationDate() != null) {
                    row.createCell(7).setCellValue(infant.getRegistrationDate().format(DATE_FORMATTER));
                } else {
                    row.createCell(7).setCellValue("");
                }

                // Birth Weight - utiliser doubleValue()
                if (infant.getBirthWeight() != null) {
                    row.createCell(8).setCellValue(infant.getBirthWeight().doubleValue());
                } else {
                    row.createCell(8).setCellValue(0.0);
                }

                // Gestational Age - utiliser intValue()
                if (infant.getGestationalAge() != null) {
                    row.createCell(9).setCellValue(infant.getGestationalAge().intValue());
                } else {
                    row.createCell(9).setCellValue(0);
                }

                // Mother Name
                if (mother != null && mother.getName() != null) {
                    row.createCell(10).setCellValue(mother.getName());
                } else {
                    row.createCell(10).setCellValue("");
                }

                // Mother Code - utiliser intValue()
                if (mother != null && mother.getCode() != null) {
                    row.createCell(11).setCellValue(mother.getCode().intValue());
                } else {
                    row.createCell(11).setCellValue("");
                }

                // Follow-up Start Date
                if (infant.getFollowUpStartDate() != null) {
                    row.createCell(12).setCellValue(infant.getFollowUpStartDate().format(DATE_ONLY_FORMATTER));
                } else {
                    row.createCell(12).setCellValue("");
                }

                // Follow-up End Date
                if (infant.getFollowUpEndDate() != null) {
                    row.createCell(13).setCellValue(infant.getFollowUpEndDate().format(DATE_ONLY_FORMATTER));
                } else {
                    row.createCell(13).setCellValue("");
                }

                // Notes
                if (infant.getNotes() != null) {
                    row.createCell(14).setCellValue(infant.getNotes());
                } else {
                    row.createCell(14).setCellValue("");
                }
            }

            // Ajuster les colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }
}