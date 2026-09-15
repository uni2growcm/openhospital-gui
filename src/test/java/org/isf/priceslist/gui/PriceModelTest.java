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
import org.junit.jupiter.api.Test;

/**
 * Tests the "Variable" (column 2) wiring of {@link PriceModel}, which is Spring-free and can be
 * exercised directly without a live {@link PricesBrowser}.
 */
class PriceModelTest {

	private final PriceModel model = new PriceModel(null);

	@Test
	void getValueAtReadsVariableFlag() {
		PriceNode leaf = new PriceNode(new Price(null, "EXA", "code", "Malaria test", 10.0));

		assertThat(model.getValueAt(leaf, 2)).isEqualTo(false);

		leaf.getPrice().setVariable(true);
		assertThat(model.getValueAt(leaf, 2)).isEqualTo(true);
	}

	@Test
	void setValueAtWritesVariableFlag() {
		PriceNode leaf = new PriceNode(new Price(null, "EXA", "code", "Malaria test", 10.0));

		model.setValueAt(true, leaf, 2);

		assertThat(leaf.getPrice().isVariable()).isTrue();
	}

	@Test
	void variableColumnIsEditableForAPriceLeafRegardlessOfEditableFlag() {
		Price price = new Price(null, "OTH", "code", "Consultation", 10.0, false);
		PriceNode leaf = new PriceNode(price);

		assertThat(price.isEditable()).isFalse();
		assertThat(model.isCellEditable(leaf, 2)).isTrue();
	}

	@Test
	void variableColumnIsNotEditableForACategoryHeaderNode() {
		PriceNode category = new PriceNode(new Price(null, "", "", "Exams", null));

		assertThat(model.isCellEditable(category, 2)).isFalse();
	}
}
