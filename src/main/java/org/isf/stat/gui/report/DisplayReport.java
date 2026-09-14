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
package org.isf.stat.gui.report;

import java.awt.Dialog;
import java.io.IOException;
import java.util.Locale;

import org.isf.generaldata.GeneralData;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.utils.jobjects.MessageDialog;

import net.sf.jasperreports.view.JasperViewer;

public class DisplayReport {

	protected void showReport(JasperReportResultDto jasperReportResultDto) throws IOException {
		if (jasperReportResultDto.getJasperPrint().getPages().isEmpty()) {
			MessageDialog.info(null, "angal.common.documenthasnopages.msg");
			return;
		}
		if (GeneralData.INTERNALVIEWER) {
			JasperViewer jasperViewer = new JasperViewer(
					jasperReportResultDto.getJasperPrint(),
					false,
					new Locale(GeneralData.LANGUAGE));
			// This report window is a plain JFrame opened while a modal JDialog (e.g. CpnEdit) is still
			// showing. AWT's own modal-blocking - not just the window manager - keeps every window of the
			// same application behind an active application-modal dialog unless explicitly excluded from
			// its modality, which is why toFront()/requestFocus() alone (and even a WM-level always-on-top
			// request) failed to raise it. setModalExclusionType() opts this window out of that blocking so
			// it can be shown, and stay, above the modal CpnEdit dialog for as long as both are open.
			jasperViewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
			jasperViewer.setAlwaysOnTop(true);
			jasperViewer.setVisible(true);
			jasperViewer.toFront();
			jasperViewer.requestFocus();
		} else {
			Runtime rt = Runtime.getRuntime();
			rt.exec(GeneralData.VIEWER + ' ' + jasperReportResultDto.getFilename());
		}
	}
}
