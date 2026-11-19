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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
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

public class GenericReportCommandsBySupplier extends DisplayReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportCommandsBySupplier.class);
	private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

	public GenericReportCommandsBySupplier(LocalDate fromDate, LocalDate toDate, Integer supplierId) {
		this(fromDate, toDate, supplierId, false);
	}

	public GenericReportCommandsBySupplier(LocalDate fromDate, LocalDate toDate, Integer supplierId, boolean toExcel) {
		try {
			if (toExcel) {
				File defaultFilename = new File(jasperReportsManager.compileDefaultFilename("CommandsBySupplier"));
				JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFilename);
				int iRetVal = fcExcel.showSaveDialog(null);
				if (iRetVal == JFileChooser.APPROVE_OPTION) {
					File exportFile = fcExcel.getSelectedFile();
					FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) fcExcel.getFileFilter();
					String extension = (selectedFilter != null && selectedFilter.getExtensions().length > 0)
							? selectedFilter.getExtensions()[0] : "xlsx";
					if (!exportFile.getName().endsWith(extension)) {
						exportFile = new File(exportFile.getAbsoluteFile() + "." + extension);
					}
					exportToExcel(fromDate, toDate, supplierId, exportFile);
				}
			} else {
				JasperReportResultDto jasperReportResultDto = getReportPdf(fromDate, toDate, supplierId);
				showReport(jasperReportResultDto);
			}
		} catch (OHReportException e) {
			OHServiceExceptionUtil.showMessages(e);
		} catch (Exception e) {
			LOGGER.error("Error executing CommandsBySupplier report", e);
			MessageDialog.error(null, "angal.stat.reporterror.msg");
		}
	}

	private void exportToExcel(LocalDate fromDate, LocalDate toDate, Integer supplierId, File exportFile) throws Exception {
		DataSource dataSource = Context.getApplicationContext().getBean(DataSource.class);

		String unspecifiedSupplier = "en".equalsIgnoreCase(GeneralData.LANGUAGE) ? "Unspecified Supplier" : "Fournisseur non spécifié";
		String statusPending = "en".equalsIgnoreCase(GeneralData.LANGUAGE) ? "Pending" : "En attente";
		String statusDelivered = "en".equalsIgnoreCase(GeneralData.LANGUAGE) ? "Delivered" : "Livré";

		String sql = "SELECT "
				+ "COALESCE(S.SUP_NAME, '" + unspecifiedSupplier + "') AS FOURNISSEUR, "
				+ "S.SUP_PHONE AS TELEPHONE, "
				+ "S.SUP_EMAIL AS EMAIL, "
				+ "C.CMD_REFNO AS NO_COMMANDE, "
				+ "DATE(C.CMD_DATE) AS DATE_COMMANDE, "
				+ "CR.CMR_MDSR_CODE AS CODE, "
				+ "CR.CMR_MDSR_DESC AS DESIGNATION, "
				+ "CR.CMR_ORDER_QTY AS QTE_COMMANDEE, "
				+ "CR.CMR_QTY_IN_STORE AS STOCK, "
				+ "IF(CR.CMR_ACTIVE = 1, '" + statusPending + "', '" + statusDelivered + "') AS STATUT "
				+ "FROM OH_COMMAND C "
				+ "JOIN OH_COMMAND_ROW CR ON C.CMD_ID = CR.CMR_CMD_ID "
				+ "LEFT JOIN OH_SUPPLIER S ON CR.CMR_SUP_ID = S.SUP_ID "
				+ "WHERE (DATE(C.CMD_DATE) BETWEEN ? AND ?) "
				+ "AND (? = -1 OR (? = 0 AND CR.CMR_SUP_ID IS NULL) OR CR.CMR_SUP_ID = ?) "
				+ "ORDER BY FOURNISSEUR ASC, C.CMD_DATE DESC, C.CMD_REFNO ASC, CR.CMR_MDSR_DESC ASC";

		try (Connection connection = dataSource.getConnection();
		     PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setDate(1, java.sql.Date.valueOf(fromDate));
			ps.setDate(2, java.sql.Date.valueOf(toDate));
			int sId = supplierId != null ? supplierId : -1;
			ps.setInt(3, sId);
			ps.setInt(4, sId);
			ps.setInt(5, sId);

			try (ResultSet rs = ps.executeQuery()) {
				ExcelExporter xlsExport = new ExcelExporter();
				if (exportFile.getName().endsWith(".xls")) {
					xlsExport.exportResultsetToExcelOLD(rs, exportFile);
				} else {
					xlsExport.exportResultsetToExcel(rs, exportFile);
				}
			}
		}
	}

	private JasperReportResultDto getReportPdf(LocalDate fromDate, LocalDate toDate, Integer supplierId) throws Exception {
		DataSource dataSource = Context.getApplicationContext().getBean(DataSource.class);
		HospitalBrowsingManager hospitalManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);

		String jasperFileFolder = "rpt_stat";
		String jasperFileName = "CommandsBySupplier";
		String jasperFilename = jasperFileFolder + File.separator + jasperFileName + ".jasper";
		String pdfDir = jasperFileFolder + File.separator + "PDF";
		File dir = new File(pdfDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String pdfFilename = pdfDir + File.separator + jasperFileName + ".pdf";

		HashMap<String, Object> parameters = new HashMap<>();

		Hospital hosp = hospitalManager.getHospital();
		if (hosp != null) {
			parameters.put("Hospital", hosp.getDescription());
			parameters.put("Address", hosp.getAddress());
			parameters.put("City", hosp.getCity());
			parameters.put("Email", hosp.getEmail());
			parameters.put("Telephone", hosp.getTelephone());
			parameters.put("Currency", hosp.getCurrencyCod());
		}

		parameters.put("fromdate", fromDate != null ? Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
		parameters.put("todate", toDate != null ? Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
		parameters.put("supplierId", supplierId != null ? supplierId : -1);
		parameters.put("IMAGE_PATH", "./rsc/images/logo_report.png");

		parameters.put(JRParameter.REPORT_LOCALE, Locale.getDefault());

		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(jasperFileName, Locale.getDefault(), new UTF8Control());
			parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
		} catch (MissingResourceException e) {
			LOGGER.warn(">> no resource bundle for '{}', attempting filesystem load", jasperFileName);
			File bundleFile = new File(jasperFileFolder, jasperFileName + "_" + GeneralData.LANGUAGE + ".properties");
			if (!bundleFile.exists()) {
				bundleFile = new File(jasperFileFolder, jasperFileName + ".properties");
			}
			if (bundleFile.exists()) {
				try (FileInputStream fis = new FileInputStream(bundleFile);
				     InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
					parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, new PropertyResourceBundle(isr));
				}
			}
		}

		File jasperFile = new File(jasperFilename);
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperFile);
		try (Connection connection = dataSource.getConnection()) {
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
			JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFilename);
			return new JasperReportResultDto(jasperPrint, jasperFilename, pdfFilename);
		}
	}
}
