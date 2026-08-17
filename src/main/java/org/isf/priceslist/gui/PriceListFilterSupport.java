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
package org.isf.priceslist.gui;

/**
 * The search-filtering logic behind {@link PricesBrowser}'s search field, pulled out of that Swing
 * class so it can be tested without a live window or Spring context. Operates purely on the
 * existing in-memory {@link PriceNode} tree - it never touches the database - so filtering can
 * never discard an edited-but-unsaved {@link org.isf.priceslist.model.Price}.
 */
final class PriceListFilterSupport {

	private PriceListFilterSupport() {
	}

	/**
	 * {@code true} if {@code description} contains {@code filterText}, case-insensitive. A blank
	 * (or {@code null}) filter matches everything.
	 */
	static boolean matches(String description, String filterText) {
		String normalizedFilter = normalize(filterText);
		if (normalizedFilter.isEmpty()) {
			return true;
		}
		return description != null && description.toLowerCase().contains(normalizedFilter);
	}

	/**
	 * A filtered copy of {@code category}: a new {@link PriceNode} wrapping the same
	 * {@link org.isf.priceslist.model.Price} header, containing only the leaf nodes (reused, not
	 * cloned, so in-progress edits on them survive filtering) whose description matches
	 * {@code filterText}. Returns {@code null} if no leaf matches, so the caller can omit an
	 * entirely-empty category from the filtered tree rather than showing an empty header.
	 */
	static PriceNode filterCategory(PriceNode category, String filterText) {
		PriceNode filtered = new PriceNode(category.getPrice());
		for (Object itemObj : category.getItems()) {
			PriceNode leaf = (PriceNode) itemObj;
			if (matches(leaf.getPrice().getDesc(), filterText)) {
				filtered.addItem(leaf);
			}
		}
		return filtered.getItems().length == 0 ? null : filtered;
	}

	private static String normalize(String filterText) {
		return filterText == null ? "" : filterText.trim().toLowerCase();
	}
}
