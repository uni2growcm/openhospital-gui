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
package org.isf.medicals.gui;

import java.math.BigDecimal;

/**
 * One row of the "MedicalList" print report: mirrors the columns of the medicals browser table.
 * Property names intentionally match the {@code MedicalList.jrxml} report fields, which are bound
 * by JasperReports' JavaBean data source using exact-name getters.
 */
public class MedicalListPrintRow {

	private final String type;
	private final String code;
	private final String description;
	private final Integer pcsPerPck;
	private final Double stock;
	private final Double critLevel;
	private final BigDecimal lastPrice;
	private final Double avgQty;

	public MedicalListPrintRow(String type, String code, String description, Integer pcsPerPck, Double stock,
					Double critLevel, BigDecimal lastPrice, Double avgQty) {
		this.type = type;
		this.code = code;
		this.description = description;
		this.pcsPerPck = pcsPerPck;
		this.stock = stock;
		this.critLevel = critLevel;
		this.lastPrice = lastPrice;
		this.avgQty = avgQty;
	}

	public String getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public Integer getPcsPerPck() {
		return pcsPerPck;
	}

	public Double getStock() {
		return stock;
	}

	public Double getCritLevel() {
		return critLevel;
	}

	public BigDecimal getLastPrice() {
		return lastPrice;
	}

	public Double getAvgQty() {
		return avgQty;
	}
}
