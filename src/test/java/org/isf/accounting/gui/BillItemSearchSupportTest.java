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

import org.isf.priceslist.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BillItemSearchSupportTest {

	@Nested
	@DisplayName("blank/null handling")
	class BlankHandling {

		@Test
		@DisplayName("A blank or null filter matches everything")
		void blankFilterMatchesEverything() {
			assertThat(BillItemSearchSupport.matches("Malaria test", "")).isTrue();
			assertThat(BillItemSearchSupport.matches("Malaria test", null)).isTrue();
			assertThat(BillItemSearchSupport.matches("Malaria test", "   ")).isTrue();
		}

		@Test
		@DisplayName("A null candidate never matches a non-blank filter")
		void nullCandidateNeverMatches() {
			assertThat(BillItemSearchSupport.matches(null, "test")).isFalse();
		}
	}

	@Nested
	@DisplayName("case insensitivity")
	class CaseInsensitivity {

		@Test
		@DisplayName("Matches regardless of letter case")
		void matchesCaseInsensitiveSubstring() {
			assertThat(BillItemSearchSupport.matches("Malaria Test", "malaria")).isTrue();
			assertThat(BillItemSearchSupport.matches("Malaria Test", "TEST")).isTrue();
			assertThat(BillItemSearchSupport.matches("malaria test", "MALARIA")).isTrue();
		}
	}

	@Nested
	@DisplayName("accent insensitivity")
	class AccentInsensitivity {

		@Test
		@DisplayName("An unaccented filter matches an accented candidate")
		void unaccentedFilterMatchesAccentedCandidate() {
			assertThat(BillItemSearchSupport.matches("Élévation", "ele")).isTrue();
			assertThat(BillItemSearchSupport.matches("Test élè", "ele")).isTrue();
		}

		@Test
		@DisplayName("An accented filter matches an unaccented candidate")
		void accentedFilterMatchesUnaccentedCandidate() {
			assertThat(BillItemSearchSupport.matches("Elevation test", "élé")).isTrue();
		}

		@Test
		@DisplayName("Accented filter matches accented candidate regardless of the specific accent used")
		void accentedFilterMatchesDifferentlyAccentedCandidate() {
			assertThat(BillItemSearchSupport.matches("Test élè", "élé")).isTrue();
		}
	}

	@Nested
	@DisplayName("no match")
	class NoMatch {

		@Test
		@DisplayName("Does not match unrelated text")
		void doesNotMatchUnrelatedText() {
			assertThat(BillItemSearchSupport.matches("Malaria Test", "xyz")).isFalse();
		}
	}

	@Nested
	@DisplayName("filter")
	class Filter {

		private Price price(String group, String item, String desc) {
			return new Price(null, group, item, desc, 10.0);
		}

		@Test
		@DisplayName("A combined list spanning multiple categories only keeps matching items, from any category")
		void combinedFilterMatchesAcrossCategories() {
			Price exam = price("EXA", "1", "Malaria test");
			Price operation = price("OPE", "1", "Appendectomy");
			Price medical = price("MED", "1", "Malaria treatment");
			Price other = price("OTH", "1", "Consultation");
			List<Price> all = List.of(exam, operation, medical, other);

			List<Price> results = BillItemSearchSupport.filter(all, "malaria");

			assertThat(results).containsExactly(exam, medical);
		}

		@Test
		@DisplayName("Each result retains its original group, so dispatch-by-group after selection still routes correctly")
		void filteredResultsRetainTheirGroup() {
			Price exam = price("EXA", "1", "Malaria test");
			Price medical = price("MED", "1", "Malaria treatment");

			List<Price> results = BillItemSearchSupport.filter(List.of(exam, medical), "malaria");

			assertThat(results).extracting(Price::getGroup).containsExactly("EXA", "MED");
		}

		@Test
		@DisplayName("A blank filter returns every item unchanged, in order")
		void blankFilterReturnsEveryItem() {
			Price exam = price("EXA", "1", "Malaria test");
			Price operation = price("OPE", "1", "Appendectomy");

			List<Price> results = BillItemSearchSupport.filter(List.of(exam, operation), "");

			assertThat(results).containsExactly(exam, operation);
		}

		@Test
		@DisplayName("No match in any category returns an empty list")
		void noMatchReturnsEmptyList() {
			List<Price> results = BillItemSearchSupport.filter(List.of(price("EXA", "1", "Malaria test")), "xyz");

			assertThat(results).isEmpty();
		}
	}
}
