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

import org.isf.priceslist.model.Price;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.utils.exception.OHServiceException;

/**
 * Dispatches a catalog {@link Price} to the right {@link ReductionPlanManager} category method based on
 * {@link Price#getGroup()}. Unlike the other *Support classes in this package, this one calls into the
 * Spring-managed {@link ReductionPlanManager}, so it isn't fully Spring-free and can't be unit-tested the
 * same no-context way.
 */
final class ReductionPlanBillSupport {

	private ReductionPlanBillSupport() {
	}

	static Price applyReduction(Price price, ReductionPlanManager reductionPlanManager, int reductionPlanId) throws OHServiceException {
		if (price == null || reductionPlanId == 0) {
			return price;
		}
		return switch (price.getGroup()) {
			case "MED" -> reductionPlanManager.getMedicalPrice(price, reductionPlanId);
			case "EXA" -> reductionPlanManager.getExamPrice(price, reductionPlanId);
			case "OPE" -> reductionPlanManager.getOperationPrice(price, reductionPlanId);
			case "OTH" -> reductionPlanManager.getOtherPrice(price, reductionPlanId);
			default -> price;
		};
	}
}
