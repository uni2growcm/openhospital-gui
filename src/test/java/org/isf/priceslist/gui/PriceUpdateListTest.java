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

import java.util.ArrayList;
import java.util.List;

import org.isf.priceslist.model.Price;
import org.isf.priceslist.model.PriceList;
import org.junit.jupiter.api.Test;

class PriceUpdateListTest {

	private final PriceList listA = new PriceList(1, "A", "List A", "List A", "XAF");
	private final PriceList listB = new PriceList(2, "B", "List B", "List B", "XAF");

	private Price price(PriceList list, String group, String item, double value) {
		return new Price(0, list, group, item, item + " description", value);
	}

	private PriceNode item(String group, String item, double value) {
		return new PriceNode(new Price(null, group, item, item + " description", value));
	}

	private PriceNode treeRoot(PriceNode... categories) {
		PriceNode root = new PriceNode(new Price(null, "", "", "Root", null));
		for (PriceNode category : categories) {
			root.addItem(category);
		}
		return root;
	}

	private PriceNode category(String name, PriceNode... items) {
		PriceNode categoryNode = new PriceNode(new Price(null, "", "", name, null));
		for (PriceNode item : items) {
			categoryNode.addItem(item);
		}
		return categoryNode;
	}

	@Test
	void shouldPersistEditedValueForExistingPrice() {
		List<Price> dbPrices = new ArrayList<>();
		dbPrices.add(price(listA, "EXA", "E01", 100));
		PriceNode tree = treeRoot(category("Exams", item("EXA", "E01", 150)));

		List<Price> result = PricesBrowser.buildPriceUpdateList(dbPrices, listA, tree);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getGroup()).isEqualTo("EXA");
		assertThat(result.get(0).getItem()).isEqualTo("E01");
		assertThat(result.get(0).getPrice()).isEqualTo(150.0);
	}

	@Test
	void shouldNotDuplicateOrPolluteOtherListWhenItemIsShared() {
		List<Price> dbPrices = new ArrayList<>();
		dbPrices.add(price(listA, "EXA", "E01", 100));
		dbPrices.add(price(listB, "EXA", "E01", 120));
		PriceNode tree = treeRoot(category("Exams", item("EXA", "E01", 150)));

		List<Price> result = PricesBrowser.buildPriceUpdateList(dbPrices, listA, tree);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getList()).isSameAs(listA);
		assertThat(result.get(0).getPrice()).isEqualTo(150.0);
		assertThat(dbPrices.stream().filter(p -> p.getList() == listB).findFirst().orElseThrow().getPrice()).isEqualTo(120.0);
	}

	@Test
	void shouldCreateNewPriceRowWhenItemHadNoPrice() {
		List<Price> dbPrices = new ArrayList<>();
		dbPrices.add(price(listA, "MED", "M01", 50));
		PriceNode tree = treeRoot(category("Medicals", item("MED", "M02", 75)));

		List<Price> result = PricesBrowser.buildPriceUpdateList(dbPrices, listA, tree);

		assertThat(result).hasSize(2);
		Price newPrice = result.stream().filter(p -> "M02".equals(p.getItem())).findFirst().orElseThrow();
		assertThat(newPrice.getList()).isSameAs(listA);
		assertThat(newPrice.getGroup()).isEqualTo("MED");
		assertThat(newPrice.getPrice()).isEqualTo(75.0);
	}

	@Test
	void shouldKeepStoredValuesForItemsHiddenBySearchFilter() {
		List<Price> dbPrices = new ArrayList<>();
		dbPrices.add(price(listA, "EXA", "E01", 100));
		dbPrices.add(price(listA, "MED", "M01", 50));
		PriceNode tree = treeRoot(category("Exams", item("EXA", "E01", 150)));

		List<Price> result = PricesBrowser.buildPriceUpdateList(dbPrices, listA, tree);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Price::getPrice).containsExactlyInAnyOrder(150.0, 50.0);
	}

	@Test
	void shouldPersistEditedValuesWhenSaveSetIsBuiltFromFullTree() {
		List<Price> dbPrices = new ArrayList<>();
		dbPrices.add(price(listA, "EXA", "E01", 100));
		dbPrices.add(price(listA, "MED", "M01", 50));
		dbPrices.add(price(listA, "OTH", "901", 30));
		PriceNode tree = treeRoot(
				category("Exams", item("EXA", "E01", 150)),
				category("Medicals", item("MED", "M01", 60)),
				category("Others", item("OTH", "901", 40)));

		List<Price> result = PricesBrowser.buildPriceUpdateList(dbPrices, listA, tree);

		assertThat(result).hasSize(3);
		assertThat(result).extracting(Price::getItem).containsExactlyInAnyOrder("E01", "M01", "901");
		assertThat(result).extracting(Price::getPrice).containsExactlyInAnyOrder(150.0, 60.0, 40.0);
	}

	@Test
	void shouldIncludeEditedItemOfFilteredOutCategoryWhenBuiltFromFullTree() {
		List<Price> dbPrices = new ArrayList<>();
		dbPrices.add(price(listA, "EXA", "E01", 100));
		dbPrices.add(price(listA, "OTH", "901", 30));
		PriceNode tree = treeRoot(category("Exams", item("EXA", "E01", 150)));

		List<Price> result = PricesBrowser.buildPriceUpdateList(dbPrices, listA, tree);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Price::getItem).containsExactlyInAnyOrder("E01", "901");
		assertThat(result).extracting(Price::getPrice).containsExactlyInAnyOrder(150.0, 30.0);
	}
}
