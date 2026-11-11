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
package org.isf.command.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportCommandsBySupplier;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;

public class CommandReportDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final SupplierBrowserManager supplierManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);

	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private JComboBox<SupplierItem> supplierCombo;
	private JButton btnReport;
	private JButton btnExcel;
	private JButton btnClose;

	public static class SupplierItem {
		private final Integer id;
		private final String description;

		public SupplierItem(Integer id, String description) {
			this.id = id;
			this.description = description;
		}

		public Integer getId() {
			return id;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return description;
		}
	}

	public CommandReportDialog(JFrame owner) {
		super(owner, true);
		setTitle(MessageBundle.getMessage("angal.command.report.title"));
		setContentPane(getJContentPane());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
		setLocationRelativeTo(owner);
	}

	private JPanel getJContentPane() {
		JPanel content = new JPanel(new BorderLayout(10, 10));
		content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		content.add(getFormPanel(), BorderLayout.CENTER);
		content.add(getButtonPanel(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel getFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				MessageBundle.getMessage("angal.common.filter.label")));

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(6, 6, 6, 6);
		c.fill = GridBagConstraints.HORIZONTAL;

		// Row 0: Date interval
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")), c);

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(3));
		panel.add(dateFrom, c);

		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")), c);

		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.5;
		dateTo = new GoodDateChooser(LocalDate.now());
		panel.add(dateTo, c);

		// Row 1: Supplier
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.command.report.supplier.label")), c);

		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		c.weightx = 1.0;
		panel.add(getSupplierCombo(), c);

		return panel;
	}

	private JComboBox<SupplierItem> getSupplierCombo() {
		if (supplierCombo == null) {
			supplierCombo = new JComboBox<>();
			supplierCombo.addItem(new SupplierItem(-1, MessageBundle.getMessage("angal.command.report.allsuppliers")));
			supplierCombo.addItem(new SupplierItem(0, MessageBundle.getMessage("angal.command.report.nosupplier")));

			List<Supplier> suppliers;
			try {
				suppliers = supplierManager.getList();
			} catch (OHServiceException e) {
				suppliers = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			for (Supplier s : suppliers) {
				supplierCombo.addItem(new SupplierItem(s.getSupId(), s.getSupName()));
			}
		}
		return supplierCombo;
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		panel.add(getBtnReport());
		panel.add(getBtnExcel());
		panel.add(getBtnClose());
		return panel;
	}

	private JButton getBtnReport() {
		if (btnReport == null) {
			btnReport = new JButton(MessageBundle.getMessage("angal.common.launchreport.btn"));
			btnReport.setMnemonic(MessageBundle.getMnemonic("angal.common.launchreport.btn.key"));
			btnReport.addActionListener(e -> launchReport(false));
		}
		return btnReport;
	}

	private JButton getBtnExcel() {
		if (btnExcel == null) {
			btnExcel = new JButton(MessageBundle.getMessage("angal.common.excel.btn"));
			btnExcel.setMnemonic(MessageBundle.getMnemonic("angal.common.excel.btn.key"));
			btnExcel.addActionListener(e -> launchReport(true));
		}
		return btnExcel;
	}

	private JButton getBtnClose() {
		if (btnClose == null) {
			btnClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			btnClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			btnClose.addActionListener(e -> dispose());
		}
		return btnClose;
	}

	private void launchReport(boolean toExcel) {
		if (dateFrom.getDate() == null || dateTo.getDate() == null) {
			MessageDialog.error(this, "angal.command.pleaseinsertadate.msg");
			return;
		}
		if (dateFrom.getDate().isAfter(dateTo.getDate())) {
			MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
			return;
		}

		SupplierItem selected = (SupplierItem) supplierCombo.getSelectedItem();
		Integer supplierId = (selected != null) ? selected.getId() : -1;

		new GenericReportCommandsBySupplier(dateFrom.getDate(), dateTo.getDate(), supplierId, toExcel);
	}
}
