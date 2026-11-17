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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.mortuary.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model representing a single row in the Mortuary Stay Report table.
 */
public class DeathStayRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String designation;
	private Float prix_journalier;
	private Integer nombre_jours;
	private Float montant;

	public DeathStayRow() {
	}

	public DeathStayRow(String designation, Float prix_journalier, Integer nombre_jours, Float montant) {
		this.designation = designation;
		this.prix_journalier = prix_journalier;
		this.nombre_jours = nombre_jours;
		this.montant = montant;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Float getPrix_journalier() {
		return prix_journalier;
	}

	public void setPrix_journalier(Float prix_journalier) {
		this.prix_journalier = prix_journalier;
	}

	public Integer getNombre_jours() {
		return nombre_jours;
	}

	public void setNombre_jours(Integer nombre_jours) {
		this.nombre_jours = nombre_jours;
	}

	public Float getMontant() {
		return montant;
	}

	public void setMontant(Float montant) {
		this.montant = montant;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DeathStayRow that = (DeathStayRow) o;
		return Objects.equals(designation, that.designation)
				&& Objects.equals(prix_journalier, that.prix_journalier)
				&& Objects.equals(nombre_jours, that.nombre_jours)
				&& Objects.equals(montant, that.montant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(designation, prix_journalier, nombre_jours, montant);
	}

	@Override
	public String toString() {
		return "DeathStayRow{" +
				"designation='" + designation + '\'' +
				", prix_journalier=" + prix_journalier +
				", nombre_jours=" + nombre_jours +
				", montant=" + montant +
				'}';
	}
}
