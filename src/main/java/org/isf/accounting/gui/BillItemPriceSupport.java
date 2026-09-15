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
package org.isf.accounting.gui;

import org.isf.accounting.model.BillItems;
import org.isf.priceslist.model.Price;

/**
 * Pure, Spring-free logic backing the "variable price" prompt in {@link PatientBillEdit}'s
 * Add Exam/Operation/Medical handlers, and the catalog-reconciliation check in
 * {@code checkBill()}/{@code updatePrices()}, extracted so it can be unit-tested without a
 * Swing/Spring context.
 */
class BillItemPriceSupport {

	private BillItemPriceSupport() {
	}

	static boolean requiresPricePrompt(Price price) {
		return price != null && price.isVariable();
	}

	static double parseAmount(String input) {
		return Double.parseDouble(input);
	}

	/**
	 * A variable item's billed amount is expected to diverge from the catalog price - that is
	 * the point of the feature - so it is never treated as "changed". Its description is still
	 * reconciled against the catalog, in case the underlying exam/operation/medical was renamed.
	 */
	static boolean descriptionChanged(BillItems item, Price catalogPrice) {
		return !item.getItemDescription().equals(catalogPrice.getDesc());
	}

	static boolean priceChanged(BillItems item, Price catalogPrice) {
		return !catalogPrice.isVariable() && !catalogPrice.getPrice().equals(item.getItemAmount());
	}

	static boolean requiresReconciliation(BillItems item, Price catalogPrice) {
		return descriptionChanged(item, catalogPrice) || priceChanged(item, catalogPrice);
	}
}
