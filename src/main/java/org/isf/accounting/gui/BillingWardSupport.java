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
package org.isf.accounting.gui;

import java.util.List;

import org.isf.medicalstockward.model.MedicalWard;
import org.isf.priceslist.model.Price;
import org.isf.ward.model.Ward;

/**
 * Pure, Spring-free logic backing {@link PatientBillEdit}'s ward combo box default-selection
 * precedence and its ward-stock-scoped medical picker, extracted so it can be unit-tested without a
 * Swing/Spring context.
 */
class BillingWardSupport {

	private BillingWardSupport() {
	}

	/**
	 * Default-ward precedence for a new bill: the configured ward wins if present, otherwise the
	 * patient's admission ward, otherwise no selection.
	 */
	static Ward pickDefaultWard(Ward configuredWard, Ward admissionWard) {
		return configuredWard != null ? configuredWard : admissionWard;
	}

	/**
	 * Whether {@code price} (a "MED" group price-list entry, whose {@code getItem()} is the
	 * medical's code) has any in-stock row in {@code wardStock}.
	 */
	static boolean isMedicalInWardStock(Price price, List<MedicalWard> wardStock) {
		for (MedicalWard medicalWard : wardStock) {
			if (String.valueOf(medicalWard.getMedical().getCode()).equals(price.getItem())) {
				return true;
			}
		}
		return false;
	}

	/** Total available quantity for {@code price}'s medical, summed across every lot in {@code wardStock}. */
	static int availableQuantity(Price price, List<MedicalWard> wardStock) {
		int total = 0;
		for (MedicalWard medicalWard : wardStock) {
			if (String.valueOf(medicalWard.getMedical().getCode()).equals(price.getItem())) {
				total += medicalWard.getQty().intValue();
			}
		}
		return total;
	}
}
