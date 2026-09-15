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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.isf.accounting.model.BillItems;
import org.isf.priceslist.model.Price;
import org.junit.jupiter.api.Test;

/**
 * Tests the "does this Price require a price prompt" decision and amount-parsing logic behind
 * {@link PatientBillEdit}'s Add Exam/Operation/Medical handlers. {@code PatientBillEdit} itself
 * cannot be instantiated in a headless test run (its constructor pulls manager beans from the live
 * Spring {@code Context}), so this exercises the extracted, Spring-free {@link BillItemPriceSupport}
 * directly.
 */
class BillItemPriceSupportTest {

	@Test
	void requiresPricePromptIsTrueForAVariablePrice() {
		Price price = new Price(null, "EXA", "code", "Malaria test", 10.0);
		price.setVariable(true);

		assertThat(BillItemPriceSupport.requiresPricePrompt(price)).isTrue();
	}

	@Test
	void requiresPricePromptIsFalseForANonVariablePrice() {
		Price price = new Price(null, "EXA", "code", "Malaria test", 10.0);

		assertThat(BillItemPriceSupport.requiresPricePrompt(price)).isFalse();
	}

	@Test
	void requiresPricePromptIsFalseForANullPrice() {
		assertThat(BillItemPriceSupport.requiresPricePrompt(null)).isFalse();
	}

	@Test
	void parseAmountParsesAValidNumber() {
		assertThat(BillItemPriceSupport.parseAmount("12.5")).isEqualTo(12.5);
	}

	@Test
	void parseAmountRejectsANonNumericValue() {
		assertThatThrownBy(() -> BillItemPriceSupport.parseAmount("abc"))
				.isInstanceOf(NumberFormatException.class);
	}

	private BillItems billItem(String desc, double amount) {
		return new BillItems(0, null, true, "EXAcode", desc, amount, 1);
	}

	@Test
	void priceChangedIsFalseForAVariableCatalogPriceEvenIfAmountsDiffer() {
		Price catalog = new Price(null, "EXA", "code", "Malaria test", 10.0);
		catalog.setVariable(true);
		BillItems item = billItem("Malaria test", 25.0);

		assertThat(BillItemPriceSupport.priceChanged(item, catalog)).isFalse();
	}

	@Test
	void priceChangedIsTrueForANonVariableCatalogPriceWithDifferentAmount() {
		Price catalog = new Price(null, "EXA", "code", "Malaria test", 10.0);
		BillItems item = billItem("Malaria test", 25.0);

		assertThat(BillItemPriceSupport.priceChanged(item, catalog)).isTrue();
	}

	@Test
	void descriptionChangedIsTrueRegardlessOfVariableFlag() {
		Price catalog = new Price(null, "EXA", "code", "Malaria test (renamed)", 10.0);
		catalog.setVariable(true);
		BillItems item = billItem("Malaria test", 25.0);

		assertThat(BillItemPriceSupport.descriptionChanged(item, catalog)).isTrue();
	}

	@Test
	void requiresReconciliationIsFalseForAVariableItemWithOnlyThePriceDiffering() {
		Price catalog = new Price(null, "EXA", "code", "Malaria test", 10.0);
		catalog.setVariable(true);
		BillItems item = billItem("Malaria test", 25.0);

		assertThat(BillItemPriceSupport.requiresReconciliation(item, catalog)).isFalse();
	}

	@Test
	void requiresReconciliationIsTrueForAVariableItemWithADifferentDescription() {
		Price catalog = new Price(null, "EXA", "code", "Malaria test (renamed)", 10.0);
		catalog.setVariable(true);
		BillItems item = billItem("Malaria test", 25.0);

		assertThat(BillItemPriceSupport.requiresReconciliation(item, catalog)).isTrue();
	}
}
