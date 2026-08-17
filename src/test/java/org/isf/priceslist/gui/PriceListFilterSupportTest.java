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

import static org.assertj.core.api.Assertions.assertThat;

import org.isf.priceslist.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the search-filtering logic behind {@link PricesBrowser}'s search field. {@code PricesBrowser}
 * itself cannot be instantiated in a headless test run (its constructor pulls manager beans from the
 * live Spring {@code Context}), so this exercises the extracted, Spring-free {@link PriceListFilterSupport}
 * directly.
 */
class PriceListFilterSupportTest {

	private PriceNode leaf(String desc) {
		return new PriceNode(new Price(null, "EXA", desc, desc, 10.0));
	}

	private PriceNode category(String headerDesc, PriceNode... leaves) {
		PriceNode category = new PriceNode(new Price(null, "", "", headerDesc, null));
		for (PriceNode leaf : leaves) {
			category.addItem(leaf);
		}
		return category;
	}

	@Nested
	@DisplayName("matches")
	class Matches {

		@Test
		@DisplayName("A blank or null filter matches everything")
		void blankFilterMatchesEverything() {
			assertThat(PriceListFilterSupport.matches("Malaria test", "")).isTrue();
			assertThat(PriceListFilterSupport.matches("Malaria test", null)).isTrue();
			assertThat(PriceListFilterSupport.matches("Malaria test", "   ")).isTrue();
		}

		@Test
		@DisplayName("Matches a substring, case-insensitively")
		void matchesCaseInsensitiveSubstring() {
			assertThat(PriceListFilterSupport.matches("Malaria Test", "malaria")).isTrue();
			assertThat(PriceListFilterSupport.matches("Malaria Test", "TEST")).isTrue();
		}

		@Test
		@DisplayName("Does not match unrelated text")
		void doesNotMatchUnrelatedText() {
			assertThat(PriceListFilterSupport.matches("Malaria Test", "xyz")).isFalse();
		}

		@Test
		@DisplayName("A null description never matches a non-blank filter")
		void nullDescriptionNeverMatches() {
			assertThat(PriceListFilterSupport.matches(null, "test")).isFalse();
		}
	}

	@Nested
	@DisplayName("filterCategory")
	class FilterCategory {

		@Test
		@DisplayName("A blank filter keeps every leaf")
		void blankFilterKeepsEveryLeaf() {
			PriceNode malaria = leaf("Malaria test");
			PriceNode xray = leaf("X-Ray");
			PriceNode category = category("Exams", malaria, xray);

			PriceNode filtered = PriceListFilterSupport.filterCategory(category, "");

			assertThat(filtered).isNotNull();
			assertThat(filtered.getItems()).containsExactly(malaria, xray);
		}

		@Test
		@DisplayName("Only matching leaves are kept, reusing the same instances")
		void keepsOnlyMatchingLeavesBySameInstance() {
			PriceNode malaria = leaf("Malaria test");
			PriceNode xray = leaf("X-Ray");
			PriceNode category = category("Exams", malaria, xray);

			PriceNode filtered = PriceListFilterSupport.filterCategory(category, "malaria");

			assertThat(filtered).isNotNull();
			assertThat(filtered.getItems()).containsExactly(malaria);
			assertThat(filtered.getItems()[0]).isSameAs(malaria);
		}

		@Test
		@DisplayName("An edit made through the original leaf is visible through the filtered view")
		void editsOnReusedLeavesSurviveFiltering() {
			PriceNode malaria = leaf("Malaria test");
			PriceNode category = category("Exams", malaria);

			PriceNode filtered = PriceListFilterSupport.filterCategory(category, "malaria");
			malaria.getPrice().setPrice(42.0);

			PriceNode filteredLeaf = (PriceNode) filtered.getItems()[0];
			assertThat(filteredLeaf.getPrice().getPrice()).isEqualTo(42.0);
		}

		@Test
		@DisplayName("A category with no matching leaf is omitted (returns null)")
		void noMatchesReturnsNull() {
			PriceNode category = category("Exams", leaf("Malaria test"), leaf("X-Ray"));

			assertThat(PriceListFilterSupport.filterCategory(category, "xyz")).isNull();
		}

		@Test
		@DisplayName("The header Price of the filtered category is the same object as the original")
		void headerPriceIsPreserved() {
			PriceNode category = category("Exams", leaf("Malaria test"));

			PriceNode filtered = PriceListFilterSupport.filterCategory(category, "");

			assertThat(filtered.getPrice()).isSameAs(category.getPrice());
		}
	}
}
