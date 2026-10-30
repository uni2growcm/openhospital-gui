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
package org.isf.lab.model;

import java.io.Serializable;

/**
 * Model representing a single row in the Patient Analysis Report table.
 */
public class PatientAnalysisRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String parameter;
	private String resultValue;

	public PatientAnalysisRow() {
	}

	public PatientAnalysisRow(String parameter, String resultValue) {
		this.parameter = parameter;
		this.resultValue = resultValue;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getResultValue() {
		return resultValue;
	}

	public void setResultValue(String resultValue) {
		this.resultValue = resultValue;
	}

	@Override
	public String toString() {
		return "PatientAnalysisRow{" +
				"parameter='" + parameter + '\'' +
				", resultValue='" + resultValue + '\'' +
				'}';
	}
}
