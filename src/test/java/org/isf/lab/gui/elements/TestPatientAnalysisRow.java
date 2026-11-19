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
package org.isf.lab.gui.elements;

import static org.assertj.core.api.Assertions.assertThat;

import org.isf.lab.model.PatientAnalysisRow;
import org.junit.jupiter.api.Test;

class TestPatientAnalysisRow {

	@Test
	void testPatientAnalysisRowConstructorAndGetters() {
		PatientAnalysisRow row = new PatientAnalysisRow("Hémoglobine", "13.5 g/dL");

		assertThat(row.getParameter()).isEqualTo("Hémoglobine");
		assertThat(row.getResultValue()).isEqualTo("13.5 g/dL");
	}

	@Test
	void testPatientAnalysisRowSetters() {
		PatientAnalysisRow row = new PatientAnalysisRow();
		row.setParameter("Glycémie");
		row.setResultValue("0.95 g/L");

		assertThat(row.getParameter()).isEqualTo("Glycémie");
		assertThat(row.getResultValue()).isEqualTo("0.95 g/L");
		assertThat(row.toString()).contains("Glycémie").contains("0.95 g/L");
	}

	@Test
	void testCompilePatientAnalysisReport() throws Exception {
		net.sf.jasperreports.engine.JasperCompileManager.compileReportToFile(
				"rpt_base/PatientAnalysisReport.jrxml",
				"rpt_base/PatientAnalysisReport.jasper"
		);
		java.io.File jasperFile = new java.io.File("rpt_base/PatientAnalysisReport.jasper");
		assertThat(jasperFile).exists().isNotEmpty();
	}
}
