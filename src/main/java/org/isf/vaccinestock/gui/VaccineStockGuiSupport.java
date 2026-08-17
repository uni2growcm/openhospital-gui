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
package org.isf.vaccinestock.gui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.isf.generaldata.MessageBundle;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccinestock.model.VaccineLot;
import org.isf.vaccinestock.model.VaccineStockMovementReason;

/**
 * Presentation logic shared by {@link VaccineStockBrowser}, {@link VaccineStockChargeEdit} and
 * {@link VaccineStockDischargeEdit}, pulled out of those Swing classes so it can be tested without
 * a live window or Spring context.
 */
final class VaccineStockGuiSupport {

	private VaccineStockGuiSupport() {
	}

	/**
	 * {@code true} if the vaccine has a configured minimum-quantity threshold and {@code balance} is
	 * below it.
	 */
	static boolean isBelowMinQuantity(Vaccine vaccine, int balance) {
		Integer min = vaccine.getMinQuantity();
		return min != null && balance < min;
	}

	/**
	 * {@code true} if {@code nearestExpiry} falls within {@code alertDays} of today (or is already
	 * past).
	 */
	static boolean isNearExpiry(LocalDate nearestExpiry, int alertDays) {
		return nearestExpiry != null && !nearestExpiry.isAfter(LocalDate.now().plusDays(alertDays));
	}

	/**
	 * The localized label for a {@link VaccineStockMovementReason} name, falling back to the raw
	 * value if it isn't a recognized reason.
	 */
	static String formatReason(String reason) {
		try {
			VaccineStockMovementReason parsed = VaccineStockMovementReason.valueOf(reason);
			return MessageBundle.getMessage("angal.vaccinestock.reason." + parsed.name().toLowerCase().replace("_", ""));
		} catch (IllegalArgumentException illegalArgumentException) {
			return reason;
		}
	}

	/**
	 * Parses a lot cost typed by the user, accepting both {@code .} and {@code ,} as the decimal
	 * separator. Returns {@code null} for a blank input (cost is optional).
	 *
	 * @throws NumberFormatException if the text is non-blank and not a valid number.
	 */
	static BigDecimal parseCost(String costText) {
		String trimmed = costText == null ? "" : costText.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return new BigDecimal(trimmed.replace(',', '.'));
	}

	/**
	 * The vaccines from {@code allVaccines} whose code or description contains {@code filterText}
	 * (case-insensitive). A blank filter matches everything.
	 */
	static List<Vaccine> filterVaccines(List<Vaccine> allVaccines, String filterText) {
		String filter = filterText == null ? "" : filterText.trim().toLowerCase();
		List<Vaccine> matches = new ArrayList<>();
		for (Vaccine vaccine : allVaccines) {
			if (filter.isEmpty() || vaccine.getCode().toLowerCase().contains(filter) || vaccine.getDescription().toLowerCase().contains(filter)) {
				matches.add(vaccine);
			}
		}
		return matches;
	}

	/**
	 * {@code true} if one of {@code lots} already has the given code - used when creating a new lot
	 * to avoid silently colliding with an existing one.
	 */
	static boolean isLotCodeInUse(List<VaccineLot> lots, String code) {
		for (VaccineLot lot : lots) {
			if (code.equals(lot.getCode())) {
				return true;
			}
		}
		return false;
	}
}
