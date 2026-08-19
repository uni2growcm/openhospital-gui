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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.isf.medicals.model.Medical;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.priceslist.model.Price;
import org.isf.ward.model.Ward;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BillingWardSupportTest {

	private Medical medical(int code) {
		Medical medical = new Medical();
		medical.setCode(code);
		return medical;
	}

	private MedicalWard medicalWard(int medicalCode, double qty) {
		return new MedicalWard(medical(medicalCode), qty);
	}

	private Price price(int medicalCode) {
		return new Price(null, "MED", String.valueOf(medicalCode), "Medical " + medicalCode, 10.0);
	}

	@Nested
	@DisplayName("pickDefaultWard")
	class PickDefaultWard {

		@Test
		@DisplayName("The configured ward wins when present")
		void configuredWardWins() {
			Ward configured = new Ward();
			Ward admission = new Ward();

			assertThat(BillingWardSupport.pickDefaultWard(configured, admission)).isSameAs(configured);
		}

		@Test
		@DisplayName("Falls back to the admission ward when no configured ward")
		void fallsBackToAdmissionWard() {
			Ward admission = new Ward();

			assertThat(BillingWardSupport.pickDefaultWard(null, admission)).isSameAs(admission);
		}

		@Test
		@DisplayName("No selection when neither is available")
		void noSelectionWhenNeitherAvailable() {
			assertThat(BillingWardSupport.pickDefaultWard(null, null)).isNull();
		}
	}

	@Nested
	@DisplayName("isMedicalInWardStock")
	class IsMedicalInWardStock {

		@Test
		@DisplayName("A medical present in the ward stock list matches")
		void medicalPresentMatches() {
			List<MedicalWard> wardStock = List.of(medicalWard(42, 5.0));

			assertThat(BillingWardSupport.isMedicalInWardStock(price(42), wardStock)).isTrue();
		}

		@Test
		@DisplayName("A medical absent from the ward stock list does not match")
		void medicalAbsentDoesNotMatch() {
			List<MedicalWard> wardStock = List.of(medicalWard(42, 5.0));

			assertThat(BillingWardSupport.isMedicalInWardStock(price(99), wardStock)).isFalse();
		}

		@Test
		@DisplayName("An empty ward stock list matches nothing")
		void emptyWardStockMatchesNothing() {
			assertThat(BillingWardSupport.isMedicalInWardStock(price(42), List.of())).isFalse();
		}
	}

	@Nested
	@DisplayName("availableQuantity")
	class AvailableQuantity {

		@Test
		@DisplayName("Sums quantity across every lot for the same medical")
		void sumsAcrossLots() {
			List<MedicalWard> wardStock = List.of(medicalWard(42, 5.0), medicalWard(42, 3.0), medicalWard(99, 100.0));

			assertThat(BillingWardSupport.availableQuantity(price(42), wardStock)).isEqualTo(8);
		}

		@Test
		@DisplayName("Zero for a medical with no ward stock")
		void zeroForAbsentMedical() {
			List<MedicalWard> wardStock = List.of(medicalWard(42, 5.0));

			assertThat(BillingWardSupport.availableQuantity(price(99), wardStock)).isZero();
		}
	}
}
