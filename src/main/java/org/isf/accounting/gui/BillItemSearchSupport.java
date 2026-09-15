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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.isf.priceslist.model.Price;

/**
 * Pure, Spring-free case- and accent-insensitive matching logic backing {@link SearchableItemDialog}
 * and the combined item-search field in {@link PatientBillEdit}.
 */
class BillItemSearchSupport {

	private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}");

	private BillItemSearchSupport() {
	}

	static boolean matches(String candidateText, String filterText) {
		if (filterText == null || filterText.isBlank()) {
			return true;
		}
		if (candidateText == null) {
			return false;
		}
		return normalize(candidateText).contains(normalize(filterText));
	}

	private static String normalize(String text) {
		String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
		return COMBINING_MARKS.matcher(decomposed).replaceAll("").toLowerCase();
	}

	/**
	 * Filters {@code items} to those whose description matches {@code filterText} (see
	 * {@link #matches(String, String)}), preserving order and each item's {@code getGroup()}. Used
	 * both for a single category's picker ({@code items} already pre-filtered by group) and for the
	 * combined search across all categories ({@code items} spanning every group).
	 */
	static List<Price> filter(List<Price> items, String filterText) {
		List<Price> results = new ArrayList<>();
		for (Price item : items) {
			if (matches(item.getDesc(), filterText)) {
				results.add(item);
			}
		}
		return results;
	}
}
