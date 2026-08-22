/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.io.File;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sql.DataSource;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.isf.generaldata.GeneralData;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.db.UTF8Control;
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHReportException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * GenericReportFromDateToDate
 *  - launch all reports that have "from date" "to date" as parameters
 * 	- the class expects initialization through dadata, adata, name of the report (without .jasper)
 */
public class GenericReportFromDateToDate extends DisplayReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportFromDateToDate.class);
	private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	
	public GenericReportFromDateToDate(String fromDate, String toDate, String jasperFileFolder, String jasperFileName, String defaultName, boolean toExcel) {
		try {
			File defaultFilename = new File(jasperReportsManager.compileDefaultFilename(defaultName));

			if (toExcel) {
				JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFilename);

				int iRetVal = fcExcel.showSaveDialog(null);
				if (iRetVal == JFileChooser.APPROVE_OPTION) {
					File exportFile = fcExcel.getSelectedFile();
					FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
					String extension = selectedFilter.getExtensions()[0];
					if (!exportFile.getName().endsWith(extension)) {
						exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
					}
					jasperReportsManager.getGenericReportFromDateToDateExcel(fromDate, toDate, jasperFileFolder, jasperFileName, exportFile.getAbsolutePath());
				}
            } else {
                JasperReportResultDto jasperReportResultDto = 
                    jasperReportsManager.getGenericReportFromDateToDatePdf(fromDate, toDate, jasperFileFolder, jasperFileName);
				showReport(jasperReportResultDto);
            }
		} catch (OHReportException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	public GenericReportFromDateToDate(LocalDate fromDate, LocalDate toDate, String reductionPlan, String jasperFileFolder, String jasperFileName, String defaultName, boolean toExcel) {
		try {
			File defaultFilename = new File(jasperReportsManager.compileDefaultFilename(defaultName));

			if (toExcel) {
				JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFilename);

				int iRetVal = fcExcel.showSaveDialog(null);
				if (iRetVal == JFileChooser.APPROVE_OPTION) {
					File exportFile = fcExcel.getSelectedFile();
					FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
					String extension = selectedFilter.getExtensions()[0];
					if (!exportFile.getName().endsWith(extension)) {
						exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
					}
					jasperReportsManager.getGenericReportFromDateToDateExcel(fromDate, toDate, reductionPlan, jasperFileFolder, jasperFileName, exportFile.getAbsolutePath());
				}
			} else {
				JasperReportResultDto jasperReportResultDto =
						jasperReportsManager.getGenericReportFromDateToDatePdf(fromDate, toDate, reductionPlan, jasperFileFolder, jasperFileName);
				showReport(jasperReportResultDto);
			}
		} catch (OHReportException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	public GenericReportFromDateToDate(LocalDate fromDate, LocalDate toDate, String jasperFileFolder, String jasperFileName, String defaultName, boolean toExcel, boolean useStringDateParams) {
		try {
			File defaultFilename = new File(jasperReportsManager.compileDefaultFilename(defaultName));

			if (toExcel) {
				JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFilename);

				int iRetVal = fcExcel.showSaveDialog(null);
				if (iRetVal == JFileChooser.APPROVE_OPTION) {
					File exportFile = fcExcel.getSelectedFile();
					FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
					String extension = selectedFilter.getExtensions()[0];
					if (!exportFile.getName().endsWith(extension)) {
						exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
					}
					jasperReportsManager.getGenericReportFromDateToDateExcel(fromDate, toDate, jasperFileFolder, jasperFileName, exportFile.getAbsolutePath());
				}
			} else {
				JasperReportResultDto jasperReportResultDto = useStringDateParams
						? getGenericReportFromDateToDatePdfWithStringDates(fromDate, toDate, jasperFileFolder, jasperFileName)
						: getGenericReportFromDateToDatePdf(fromDate, toDate, jasperFileFolder, jasperFileName);
				showReport(jasperReportResultDto);
			}
		} catch (OHReportException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	private JasperReportResultDto getGenericReportFromDateToDatePdfWithStringDates(LocalDate fromDate, LocalDate toDate, String jasperFileFolder, String jasperFileName) throws Exception {
		DataSource dataSource = Context.getApplicationContext().getBean(DataSource.class);
		HospitalBrowsingManager hospitalManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);

		String jasperFilename = jasperFileFolder + File.separator + jasperFileName + ".jasper";
		String pdfFilename = jasperFileFolder + File.separator + "PDF" + File.separator + jasperFileName + ".pdf";

		HashMap<String, Object> parameters = new HashMap<>();

		Hospital hosp = hospitalManager.getHospital();
		parameters.put("Hospital", hosp.getDescription());
		parameters.put("Address", hosp.getAddress());
		parameters.put("City", hosp.getCity());
		parameters.put("Email", hosp.getEmail());
		parameters.put("Telephone", hosp.getTelephone());
		parameters.put("Currency", hosp.getCurrencyCod());

		// These reports' own SQL queries parse this parameter with STR_TO_DATE($P{fromdate}, '%d/%m/%Y') --
		// the format here must match that, not ISO, or the date silently fails to parse and every query
		// returns zero rows regardless of the range picked.
		DateTimeFormatter sqlDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		parameters.put("fromdate", fromDate != null ? fromDate.format(sqlDateFormat) : null);
		parameters.put("todate", toDate != null ? toDate.format(sqlDateFormat) : null);
		parameters.put("IMAGE_PATH", "./rsc/images/logo_report.png");

		parameters.put(JRParameter.REPORT_LOCALE, Locale.getDefault());

		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(jasperFileName, Locale.getDefault(), new UTF8Control());
			parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
		} catch (MissingResourceException e) {
			LOGGER.error(">> no resource bundle for language '{}' found for report {}", GeneralData.LANGUAGE, jasperFileName);
		}

		File jasperFile = new File(jasperFilename);
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperFile);
		Connection connection = dataSource.getConnection();
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
		connection.close();
		JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFilename);
		return new JasperReportResultDto(jasperPrint, jasperFilename, pdfFilename);
	}

	public GenericReportFromDateToDate(LocalDate fromDate, LocalDate toDate, String jasperFileFolder, String jasperFileName, String defaultName, boolean toExcel) {
		try {
			File defaultFilename = new File(jasperReportsManager.compileDefaultFilename(defaultName));

			if (toExcel) {
				JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFilename);

				int iRetVal = fcExcel.showSaveDialog(null);
				if (iRetVal == JFileChooser.APPROVE_OPTION) {
					File exportFile = fcExcel.getSelectedFile();
					FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
					String extension = selectedFilter.getExtensions()[0];
					if (!exportFile.getName().endsWith(extension)) {
						exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
					}
					jasperReportsManager.getGenericReportFromDateToDateExcel(fromDate, toDate, jasperFileFolder, jasperFileName, exportFile.getAbsolutePath());
				}
            } else {
                JasperReportResultDto jasperReportResultDto = getGenericReportFromDateToDatePdf(fromDate, toDate, jasperFileFolder, jasperFileName);
				showReport(jasperReportResultDto);
            }
		} catch (OHReportException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	private JasperReportResultDto getGenericReportFromDateToDatePdf(LocalDate fromDate, LocalDate toDate, String jasperFileFolder, String jasperFileName) throws Exception {
		DataSource dataSource = Context.getApplicationContext().getBean(DataSource.class);
		HospitalBrowsingManager hospitalManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);

		String jasperFilename = jasperFileFolder + File.separator + jasperFileName + ".jasper";
		String pdfFilename = jasperFileFolder + File.separator + "PDF" + File.separator + jasperFileName + ".pdf";

		HashMap<String, Object> parameters = new HashMap<>();

		Hospital hosp = hospitalManager.getHospital();
		parameters.put("Hospital", hosp.getDescription());
		parameters.put("Address", hosp.getAddress());
		parameters.put("City", hosp.getCity());
		parameters.put("Email", hosp.getEmail());
		parameters.put("Telephone", hosp.getTelephone());
		parameters.put("Currency", hosp.getCurrencyCod());

		parameters.put("fromdate", fromDate != null ? Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
		parameters.put("todate", toDate != null ? Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
		parameters.put("IMAGE_PATH", "./rsc/images/logo_report.png");

		parameters.put(JRParameter.REPORT_LOCALE, Locale.getDefault());

		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(jasperFileName, Locale.getDefault(), new UTF8Control());
			parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
		} catch (MissingResourceException e) {
			LOGGER.error(">> no resource bundle for language '{}' found for report {}", GeneralData.LANGUAGE, jasperFileName);
		}

		File jasperFile = new File(jasperFilename);
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperFile);
		Connection connection = dataSource.getConnection();
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
		connection.close();
		JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFilename);
		return new JasperReportResultDto(jasperPrint, jasperFilename, pdfFilename);
	}
	
}
