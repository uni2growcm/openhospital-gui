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
package org.isf.medicals.gui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;

class TestPharmaceuticalStockReport {

	@Test
	void testCompileAndFillPharmaceuticalStockReports() throws Exception {
		// 1. Compile ver4
		JasperCompileManager.compileReportToFile(
				"rpt_base/PharmaceuticalStock_ver4.jrxml",
				"rpt_base/PharmaceuticalStock_ver4.jasper"
		);
		File jasper4 = new File("rpt_base/PharmaceuticalStock_ver4.jasper");
		assertThat(jasper4).exists().isNotEmpty();

		// 2. Compile ver4_1
		JasperCompileManager.compileReportToFile(
				"rpt_base/PharmaceuticalStock_ver4_1.jrxml",
				"rpt_base/PharmaceuticalStock_ver4_1.jasper"
		);
		File jasper41 = new File("rpt_base/PharmaceuticalStock_ver4_1.jasper");
		assertThat(jasper41).exists().isNotEmpty();

		// 3. Compile ver5
		JasperCompileManager.compileReportToFile(
				"rpt_base/PharmaceuticalStock_ver5.jrxml",
				"rpt_base/PharmaceuticalStock_ver5.jasper"
		);
		File jasper5 = new File("rpt_base/PharmaceuticalStock_ver5.jasper");
		assertThat(jasper5).exists().isNotEmpty();

		File rptDir = new File("rpt_base");
		ClassLoader rptClassLoader = new URLClassLoader(new URL[]{rptDir.toURI().toURL()});

		// 4. Fill and export test PDF for ver4
		Map<String, Object> params4 = new HashMap<>();
		params4.put("Hospital", "Hôpital de District de Djoungolo");
		params4.put("City", "Yaoundé");
		params4.put("Address", "BP 123 Yaoundé");
		params4.put("Telephone", "+237 222 00 00 00");
		params4.put("Email", "contact@djoungolo.cm");
		params4.put("Date", "03/11/2026");
		params4.put(JRParameter.REPORT_LOCALE, Locale.FRENCH);
		params4.put(JRParameter.REPORT_RESOURCE_BUNDLE, ResourceBundle.getBundle("PharmaceuticalStock_ver4", Locale.FRENCH, rptClassLoader));

		List<Map<String, ?>> rows4 = new ArrayList<>();
		Map<String, Object> row1 = new HashMap<>();
		row1.put("MDSRT_DESC", "Antibiotiques");
		row1.put("MDSR_CODE", "AMX500");
		row1.put("MDSR_DESC", "Amoxicilline 500mg Gélule");
		row1.put("MDSR_MIN_STOCK_QTI", 10.0f);
		row1.put("STOCK", 100.0);
		row1.put("AVG_COST", 25.5);
		row1.put("VALUE", 2550.0);
		row1.put("CRITICAL", 0);
		rows4.add(row1);

		Map<String, Object> row2 = new HashMap<>();
		row2.put("MDSRT_DESC", "Antibiotiques");
		row2.put("MDSR_CODE", "CIP500");
		row2.put("MDSR_DESC", "Ciprofloxacine 500mg");
		row2.put("MDSR_MIN_STOCK_QTI", 20.0f);
		row2.put("STOCK", 50.0);
		row2.put("AVG_COST", 50.0);
		row2.put("VALUE", 2500.0);
		row2.put("CRITICAL", 0);
		rows4.add(row2);

		Map<String, Object> row3 = new HashMap<>();
		row3.put("MDSRT_DESC", "Antalgiques");
		row3.put("MDSR_CODE", "PAR500");
		row3.put("MDSR_DESC", "Paracétamol 500mg Comprimé");
		row3.put("MDSR_MIN_STOCK_QTI", 50.0f);
		row3.put("STOCK", 200.0);
		row3.put("AVG_COST", 10.0);
		row3.put("VALUE", 2000.0);
		row3.put("CRITICAL", 0);
		rows4.add(row3);

		JasperPrint print4 = JasperFillManager.fillReport("rpt_base/PharmaceuticalStock_ver4.jasper", params4, new JRMapCollectionDataSource(rows4));
		File pdf4 = new File("target/test-stock-ver4.pdf");
		JasperExportManager.exportReportToPdfFile(print4, pdf4.getAbsolutePath());
		assertThat(pdf4).exists().isNotEmpty();

		// 5. Fill and export test PDF for ver4_1
		Map<String, Object> params41 = new HashMap<>();
		params41.put("Hospital", "Hôpital de District de Djoungolo");
		params41.put("City", "Yaoundé");
		params41.put("Address", "BP 123 Yaoundé");
		params41.put("Telephone", "+237 222 00 00 00");
		params41.put("Email", "contact@djoungolo.cm");
		params41.put("Date", "03/11/2026");
		params41.put(JRParameter.REPORT_LOCALE, Locale.FRENCH);
		params41.put(JRParameter.REPORT_RESOURCE_BUNDLE, ResourceBundle.getBundle("PharmaceuticalStock_ver4_1", Locale.FRENCH, rptClassLoader));

		JasperPrint print41 = JasperFillManager.fillReport("rpt_base/PharmaceuticalStock_ver4_1.jasper", params41, new JRMapCollectionDataSource(rows4));
		File pdf41 = new File("target/test-stock-ver4_1.pdf");
		JasperExportManager.exportReportToPdfFile(print41, pdf41.getAbsolutePath());
		assertThat(pdf41).exists().isNotEmpty();

		// 6. Fill and export test PDF for ver5
		Map<String, Object> params5 = new HashMap<>();
		params5.put("Hospital", "Hôpital de District de Djoungolo");
		params5.put("City", "Yaoundé");
		params5.put("Address", "BP 123 Yaoundé");
		params5.put("Telephone", "+237 222 00 00 00");
		params5.put("Email", "contact@djoungolo.cm");
		params5.put("Date", "03/11/2026");
		params5.put(JRParameter.REPORT_LOCALE, Locale.FRENCH);
		params5.put(JRParameter.REPORT_RESOURCE_BUNDLE, ResourceBundle.getBundle("PharmaceuticalStock_ver5", Locale.FRENCH, rptClassLoader));

		List<Map<String, ?>> rows5 = new ArrayList<>();
		Map<String, Object> r5_1 = new HashMap<>();
		r5_1.put("MDSRT_DESC", "Antibiotiques");
		r5_1.put("MDSR_CODE", "AMX500");
		r5_1.put("MDSR_DESC", "Amoxicilline 500mg Gélule");
		r5_1.put("LT_ID_A", "LOT-001");
		r5_1.put("LOT_PREP_DATE", new Date());
		r5_1.put("LOT_DUE_DATE", new Date());
		r5_1.put("MDSR_MIN_STOCK_QTI", 10.0f);
		r5_1.put("STOCK", 60.0);
		r5_1.put("AVG_COST", 25.0);
		r5_1.put("VALUE", 1500.0);
		r5_1.put("CRITICAL", 0);
		rows5.add(r5_1);

		Map<String, Object> r5_2 = new HashMap<>();
		r5_2.put("MDSRT_DESC", "Antibiotiques");
		r5_2.put("MDSR_CODE", "AMX500");
		r5_2.put("MDSR_DESC", "Amoxicilline 500mg Gélule");
		r5_2.put("LT_ID_A", "LOT-002");
		r5_2.put("LOT_PREP_DATE", new Date());
		r5_2.put("LOT_DUE_DATE", new Date());
		r5_2.put("MDSR_MIN_STOCK_QTI", 10.0f);
		r5_2.put("STOCK", 40.0);
		r5_2.put("AVG_COST", 26.25);
		r5_2.put("VALUE", 1050.0);
		r5_2.put("CRITICAL", 0);
		rows5.add(r5_2);

		JasperPrint print5 = JasperFillManager.fillReport("rpt_base/PharmaceuticalStock_ver5.jasper", params5, new JRMapCollectionDataSource(rows5));
		File pdf5 = new File("target/test-stock-ver5.pdf");
		JasperExportManager.exportReportToPdfFile(print5, pdf5.getAbsolutePath());
		assertThat(pdf5).exists().isNotEmpty();
	}
}
