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

import org.junit.jupiter.api.Test;

/**
 * Tests the "how should the payment table's date cell look" decision behind
 * {@link PatientBillEdit}'s payment table - {@code PatientBillEdit} itself cannot be instantiated
 * in a headless test run, so this exercises the extracted, Spring-free
 * {@link PaymentCashierSupport} directly.
 */
class PaymentCashierSupportTest {

	@Test
	void appendsTheCashierNameInBrackets() {
		assertThat(PaymentCashierSupport.formatPaymentDateWithCashier("20/08/2026 10:30", "Jean Dupont"))
			.isEqualTo("20/08/2026 10:30 [Jean Dupont]");
	}

	@Test
	void leavesTheDateAloneWhenTheCashierNameIsNull() {
		assertThat(PaymentCashierSupport.formatPaymentDateWithCashier("20/08/2026 10:30", null))
			.isEqualTo("20/08/2026 10:30");
	}

	@Test
	void leavesTheDateAloneWhenTheCashierNameIsBlank() {
		assertThat(PaymentCashierSupport.formatPaymentDateWithCashier("20/08/2026 10:30", "   "))
			.isEqualTo("20/08/2026 10:30");
	}
}
