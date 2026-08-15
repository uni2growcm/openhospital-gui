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
package org.isf.vaccinestock.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.isf.generaldata.MessageBundle;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccinestock.model.VaccineStockMovementReason;
import org.isf.vactype.model.VaccineType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the presentation logic behind {@link VaccineStockBrowser}, {@link VaccineStockChargeEdit}
 * and {@link VaccineStockDischargeEdit} via the extracted {@link VaccineStockGuiSupport} helper -
 * none of these screens can be instantiated directly in a headless test run since their
 * constructors pull manager beans from the live Spring {@code Context}.
 */
class VaccineStockGuiSupportTest {

	private Vaccine vaccineWithMinQuantity(Integer minQuantity) {
		Vaccine vaccine = new Vaccine("Z", "TestVaccine", new VaccineType("Z", "TestType"));
		vaccine.setMinQuantity(minQuantity);
		return vaccine;
	}

	@Nested
	@DisplayName("VaccineStockBrowser: low-stock and near-expiry highlighting")
	class BrowserRowHighlighting {

		@Test
		@DisplayName("Below the configured minimum quantity")
		void isBelowMinQuantity_whenBalanceUnderThreshold() {
			assertThat(VaccineStockGuiSupport.isBelowMinQuantity(vaccineWithMinQuantity(5), 3)).isTrue();
		}

		@Test
		@DisplayName("At or above the configured minimum quantity")
		void isBelowMinQuantity_whenBalanceAtOrAboveThreshold() {
			assertThat(VaccineStockGuiSupport.isBelowMinQuantity(vaccineWithMinQuantity(5), 5)).isFalse();
			assertThat(VaccineStockGuiSupport.isBelowMinQuantity(vaccineWithMinQuantity(5), 10)).isFalse();
		}

		@Test
		@DisplayName("Never flagged when no threshold is configured")
		void isBelowMinQuantity_whenNoThresholdConfigured() {
			assertThat(VaccineStockGuiSupport.isBelowMinQuantity(vaccineWithMinQuantity(null), 0)).isFalse();
		}

		@Test
		@DisplayName("Expiry within the alert window is flagged")
		void isNearExpiry_withinWindow() {
			assertThat(VaccineStockGuiSupport.isNearExpiry(LocalDate.now().plusDays(10), 30)).isTrue();
			assertThat(VaccineStockGuiSupport.isNearExpiry(LocalDate.now(), 30)).isTrue();
		}

		@Test
		@DisplayName("Expiry already in the past is flagged")
		void isNearExpiry_alreadyPast() {
			assertThat(VaccineStockGuiSupport.isNearExpiry(LocalDate.now().minusDays(1), 30)).isTrue();
		}

		@Test
		@DisplayName("Expiry beyond the alert window is not flagged")
		void isNearExpiry_beyondWindow() {
			assertThat(VaccineStockGuiSupport.isNearExpiry(LocalDate.now().plusDays(60), 30)).isFalse();
		}

		@Test
		@DisplayName("No lot available means no expiry to flag")
		void isNearExpiry_withNoLot() {
			assertThat(VaccineStockGuiSupport.isNearExpiry(null, 30)).isFalse();
		}

		@Test
		@DisplayName("Every movement reason has a localized label")
		void formatReason_mapsEveryKnownReason() {
			for (VaccineStockMovementReason reason : VaccineStockMovementReason.values()) {
				String expected = MessageBundle.getMessage("angal.vaccinestock.reason." + reason.name().toLowerCase().replace("_", ""));
				assertThat(VaccineStockGuiSupport.formatReason(reason.name())).isEqualTo(expected);
			}
		}

		@Test
		@DisplayName("An unrecognized reason falls back to the raw value")
		void formatReason_fallsBackForUnknownReason() {
			assertThat(VaccineStockGuiSupport.formatReason("NOT_A_REAL_REASON")).isEqualTo("NOT_A_REAL_REASON");
		}
	}

	@Nested
	@DisplayName("VaccineStockChargeEdit: cost parsing")
	class ChargeEditLogic {

		@Test
		@DisplayName("A blank cost is optional and parses to null")
		void parseCost_blankIsNull() {
			assertThat(VaccineStockGuiSupport.parseCost(null)).isNull();
			assertThat(VaccineStockGuiSupport.parseCost("")).isNull();
			assertThat(VaccineStockGuiSupport.parseCost("   ")).isNull();
		}

		@Test
		@DisplayName("Both '.' and ',' are accepted as the decimal separator")
		void parseCost_acceptsDotAndComma() {
			assertThat(VaccineStockGuiSupport.parseCost("12.50")).isEqualByComparingTo(new BigDecimal("12.50"));
			assertThat(VaccineStockGuiSupport.parseCost("12,50")).isEqualByComparingTo(new BigDecimal("12.50"));
		}

		@Test
		@DisplayName("Invalid cost text is rejected")
		void parseCost_rejectsInvalidText() {
			assertThatThrownBy(() -> VaccineStockGuiSupport.parseCost("not-a-number")).isInstanceOf(NumberFormatException.class);
		}
	}
}
