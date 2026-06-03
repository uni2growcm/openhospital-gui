
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

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;
import static org.isf.utils.Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS;
import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.PatientBillEdit.PatientBillListener;
import org.isf.accounting.manager.ArchiveManager;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.SageConfig;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportBill;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.isf.stat.gui.report.GenericReportPatient;
import org.isf.stat.gui.report.GenericReportUserInDate;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.JMonthChooser;
import org.isf.utils.jobjects.JYearChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;
import org.springframework.data.domain.Page;

public class BillBrowser extends ModalJFrame implements PatientBillListener {

	protected static final String NO_USERNAME = null;
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(BillBrowser.class);
	private static final ImageIcon ADMISSION_ICON = new ImageIcon("rsc/icons/bed_icon.png");

	private JTabbedPane jTabbedPaneBills;
	private JTable jTableBills;
	private JScrollPane jScrollPaneBills;
	private JTable jTablePending;
	private JScrollPane jScrollPanePending;
	private JTable jTableClosed;
	private JScrollPane jScrollPaneClosed;
	private JTable jTableToday;
	private JTable jTablePeriod;
	private JTable jTableUser;
	private JPanel jPanelRange;
	private JPanel jPanelButtons;
	private JPanel jPanelSouth;
	private JPanel jPanelTotals;
	private JButton jButtonNew;
	private JButton jButtonEdit;
	private JButton jButtonRefund;
	private JButton jButtonPrintReceipt;
	private JButton jButtonDelete;
	private JButton jButtonClose;
	private Patient patientParent;
	private JTextField jAffiliatePersonJTextField;
	private JButton jButtonReport;
	private JButton jButtonArchive;
	private JComboBox<String> jComboUsers;
	private JMonthChooser jComboBoxMonths;
	private JYearChooser jComboBoxYears;
	private JPanel panelSupRange;
	private GoodDateChooser jCalendarTo;
	private GoodDateChooser jCalendarFrom;
	private LocalDateTime dateFrom = TimeTools.getNow();
	private LocalDateTime dateTo = TimeTools.getNow();
	private LocalDateTime dateToday0 = TimeTools.getDateToday0();
	private LocalDateTime dateToday24 = TimeTools.getDateToday24();
	private JLabel jLabelGuarantor;
	private JButton jButtonToday;
	private JComboBox<User> jComboBoxGuarantor;
	private JButton closedPrevButton;
	private JButton closedNextButton;
	private JComboBox<Integer> closedPagesCombo;
	private JLabel closedUnderLabel;
	private JLabel closedRowCounter;
	private JButton pendingPrevButton;
	private JButton pendingNextButton;
	private JComboBox<Integer> pendingPagesCombo;
	private JLabel pendingUnderLabel;
	private JLabel pendingRowCounter;
	private JButton prevButton;
	private JButton nextButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel underLabel;
	private boolean updatingPageCombo;
	private JLabel rowCounter;
	private String rowCounterText = MessageBundle.getMessage("angal.accounting.count.label") + ' ';

	private String[] columnNames = {
			MessageBundle.getMessage("angal.billbrowser.user.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.patientID.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.amount.txt").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.lastpayment.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.status.txt").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.balance.col").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.inout.col").toUpperCase()
	};

	private boolean isSingleUser = GeneralData.getGeneralData().getSINGLEUSER();
	private int[] columnsWidth = { 50, 50, 150, 50, 50, 100, 150, 50, 100, 50 };
	private int[] maxWidth = { 70, 150, 150, 150, 200, 100, 150, 50, 100, 50 };
	private boolean[] columnsResizable = { false, false, false, false, true, false, false, false, false, false };
	private Class<?>[] columnsClasses = { String.class, Integer.class, String.class, String.class, String.class, Double.class, String.class, String.class,
			Double.class, ImageIcon.class };
	private boolean[] alignStringCenter = { false, true, true, true, false, false, true, true, false, false };
	private boolean[] alingStringBoldCenter = { false, true, false, false, false, false, false, false, false, false };
	private boolean[] showColumn = { !isSingleUser, true, true, true, true, true, true, true, true, true, true };

	private BigDecimal totalToday;
	private BigDecimal balanceToday;
	private BigDecimal totalPeriod;
	private BigDecimal balancePeriod;
	private BigDecimal userToday;
	private BigDecimal userPeriod;
	private int month;
	private int year;

	private BillBrowserManager billBrowserManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
	private List<Bill> billPeriod;
	private List<BillPayments> paymentsPeriod;
	private List<Bill> billFromPayments;
	private String currencyCod;

	private String user = UserBrowsingManager.getCurrentUser();
	private List<String> users;
	private UserBrowsingManager userBrowserManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	private int currentPage = 0;
	private long totalRows = 0;
	private int totalPages = 0;
	private int closedCurrentPage = 0;
	private long closedTotalRows = 0;
	private int closedTotalPages = 0;
	private int pendingCurrentPage = 0;
	private long pendingTotalRows = 0;
	private int pendingTotalPages = 0;
	private static final int PAGE_SIZE = 100;

	private JButton exportSageButton;

	public boolean hasBillGuarantor() {
		return GeneralData.ALLOWBILLGUARANTOR;
	}

	public BillBrowser() {
		try {
			this.currencyCod = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospitalCurrencyCod();
		} catch (OHServiceException ohServiceException) {
			this.currencyCod = null;
			MessageDialog.showExceptions(ohServiceException);
		}

		try {
			users = billBrowserManager.getUsers();
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		SwingUtilities.invokeLater(() -> loadCurrentPage());
		setVisible(true);
	}

	@Override
	public void billInserted(AWTEvent event) {
		User selectedGuarantor = getSelectedGuarantor();
		try {
			if (patientParent != null) {
				if (selectedGuarantor == null) {
					updateDataSet(dateFrom, dateTo, patientParent);
				} else {
					updateDataSetByGuarantor(dateFrom, dateTo, patientParent, selectedGuarantor);
				}
			} else {
				if (selectedGuarantor == null) {
					updateDataSet(dateFrom, dateTo);
				} else {
					updateDataSetByGuarantor(dateFrom, dateTo, null, selectedGuarantor);
				}
			}
		} catch (OHServiceException ohServiceException) {
			LOGGER.error(ohServiceException.getMessage(), ohServiceException);
		}
		updateTables();
		updateTotals();
		if (event != null) {
			Bill billInserted = (Bill) event.getSource();
			if (billInserted != null) {
				int insertedId = billInserted.getId();
				IntStream.range(0, jTableBills.getRowCount()).forEach(i -> {
					Bill aBill = (Bill) jTableBills.getModel().getValueAt(i, -1);
					if (aBill.getId() == insertedId) {
						jTableBills.getSelectionModel().setSelectionInterval(i, i);
					}
				});
			}
			if (!isSingleUser && MainMenu.checkUserGrants("cashiersfilter")) {
				if (!users.contains(user)) {
					users.add(user);
					jComboUsers.addItem(user);
				}
				ActionListener[] listeners = jComboUsers.getActionListeners();
				for (ActionListener l : listeners) {
					jComboUsers.removeActionListener(l);
				}
				jComboUsers.setSelectedItem(user);
				for (ActionListener l : listeners) {
					jComboUsers.addActionListener(l);
				}
			}
		}
	}

	private void updateDataSet() {}

	private void updateDataSet(LocalDateTime dateFrom, LocalDateTime dateTo) {}

	private void updateDataSet(LocalDateTime dateFrom, LocalDateTime dateTo, Patient patient) throws OHServiceException {
		billPeriod = billBrowserManager.getBills(dateFrom, dateTo, patient)
			.stream().filter(b -> b.getParentId() == null).collect(java.util.stream.Collectors.toList());
		paymentsPeriod = billBrowserManager.getPayments(dateFrom, dateTo, patient);
		billFromPayments = billBrowserManager.getBills(paymentsPeriod);
	}

	private void updateTotals() {
		try {
			User guarantor = getSelectedGuarantor();
			double totalToday = billBrowserManager.sumPaymentsByFilters(dateToday0, dateToday24, patientParent, guarantor);
			double balanceToday = billBrowserManager.sumBalanceByFilters(null, dateToday0, dateToday24, patientParent, guarantor);
			double totalPeriod = billBrowserManager.sumPaymentsByFilters(dateFrom, dateTo, patientParent, guarantor);
			double balancePeriod = billBrowserManager.sumBalanceByFilters(null, dateFrom, dateTo, patientParent, guarantor);
			double userToday = billBrowserManager.sumPaymentsByUserAndFilters(user, dateToday0, dateToday24, patientParent, guarantor);
			double userPeriod = billBrowserManager.sumPaymentsByUserAndFilters(user, dateFrom, dateTo, patientParent, guarantor);

			jTableToday.setValueAt(totalToday, 0, 2);
			jTableToday.setValueAt(balanceToday, 0, 5);
			jTablePeriod.setValueAt(totalPeriod, 0, 2);
			jTablePeriod.setValueAt(balancePeriod, 0, 5);
			if (jTableUser != null) {
				jTableUser.setValueAt(userToday, 0, 1);
				jTableUser.setValueAt(userPeriod, 0, 3);
			}
		} catch (OHServiceException e) {
			MessageDialog.showExceptions(e);
		}
	}

	public void updateTables() {
		int selectedIndex = jTabbedPaneBills != null ? jTabbedPaneBills.getSelectedIndex() : 0;
		switch (selectedIndex) {
			case 0:
				loadCurrentPage();
				break;
			case 1:
				loadClosedBillsPage();
				break;
			case 2:
				loadPendingBillsPage();
				break;
			default:
				loadCurrentPage();
				break;
		}
	}

	private JLabel getJLabelGuarantor() {
		if (jLabelGuarantor == null) {
			jLabelGuarantor = new JLabel(MessageBundle.getMessage("angal.newbill.selectguarantor.label"));
		}
		return jLabelGuarantor;
	}

	private void updateDataSetByGuarantor(LocalDateTime dateFrom, LocalDateTime dateTo, Patient patient, User guarantor) throws OHServiceException {
		if (patient != null) {
			billPeriod = billBrowserManager.getBillsByDatePatientAndGuarantor(dateFrom, dateTo, patient, guarantor);
			paymentsPeriod = billBrowserManager.getPaymentsByDatePatientAndGuarantor(dateFrom, dateTo, patient, guarantor);
		} else {
			billPeriod = billBrowserManager.getBillsByDatePatientAndGuarantor(dateFrom, dateTo, null, guarantor);
			paymentsPeriod = billBrowserManager.getPaymentsByDatePatientAndGuarantor(dateFrom, dateTo, null, guarantor);
		}
		billPeriod = billPeriod.stream().filter(b -> b.getParentId() == null).collect(java.util.stream.Collectors.toList());
		billFromPayments = billBrowserManager.getBillsByGuarantor(paymentsPeriod, guarantor);
	}

	private User getSelectedGuarantor() {
		if (jComboBoxGuarantor != null) {
			return (User) jComboBoxGuarantor.getSelectedItem();
		}
		return null;
	}

	private JPanel getPaginationPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
		panel.setBorder(BorderFactory.createEtchedBorder());

		prevButton = new JButton("<");
		prevButton.addActionListener(e -> {
			if (currentPage > 0) {
				currentPage--;
				loadCurrentPage();
			}
		});

		pagesCombo = new JComboBox<>();
		pagesCombo.setPreferredSize(new Dimension(70, 25));
		pagesCombo.addActionListener(e -> {
			if (!updatingPageCombo && pagesCombo.getSelectedItem() != null) {
				int selected = (Integer) pagesCombo.getSelectedItem();
				if (selected - 1 != currentPage) {
					currentPage = selected - 1;
					loadCurrentPage();
				}
			}
		});

		nextButton = new JButton(">");
		nextButton.addActionListener(e -> {
			if (currentPage < totalPages - 1) {
				currentPage++;
				loadCurrentPage();
			}
		});

		underLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.common.pages.txt"));

		rowCounter = new JLabel(rowCounterText + "0");
		rowCounter.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.add(prevButton);
		panel.add(pagesCombo);
		panel.add(underLabel);
		panel.add(nextButton);
		panel.add(rowCounter);

		updatePaginationControls();
		return panel;
	}

	private void updatePaginationControls() {
		if (prevButton == null || nextButton == null || pagesCombo == null || underLabel == null) {
			return;
		}

		int comboCount = pagesCombo.getItemCount();
		if (comboCount != totalPages) {
			updatingPageCombo = true;
			pagesCombo.removeAllItems();
			for (int i = 1; i <= totalPages; i++) {
				pagesCombo.addItem(i);
			}
			updatingPageCombo = false;
		}
		if (totalPages > 0) {
			updatingPageCombo = true;
			pagesCombo.setSelectedItem(currentPage + 1);
			updatingPageCombo = false;
		}

		boolean hasOnlyOnePage = totalPages <= 1;
		prevButton.setEnabled(currentPage > 0 && !hasOnlyOnePage);
		nextButton.setEnabled(currentPage < totalPages - 1 && !hasOnlyOnePage);
		pagesCombo.setEnabled(!hasOnlyOnePage);
		underLabel.setText("/ " + totalPages + " " + MessageBundle.getMessage("angal.common.pages.txt"));
		rowCounter.setText(rowCounterText + totalRows);
	}

	private void updateClosedPaginationControls() {
		if (closedPrevButton == null || closedNextButton == null || closedPagesCombo == null || closedUnderLabel == null) {
			return;
		}

		int comboCount = closedPagesCombo.getItemCount();
		if (comboCount != closedTotalPages) {
			updatingPageCombo = true;
			closedPagesCombo.removeAllItems();
			for (int i = 1; i <= closedTotalPages; i++) {
				closedPagesCombo.addItem(i);
			}
			updatingPageCombo = false;
		}
		if (closedTotalPages > 0) {
			updatingPageCombo = true;
			closedPagesCombo.setSelectedItem(closedCurrentPage + 1);
			updatingPageCombo = false;
		}

		boolean hasOnlyOnePage = closedTotalPages <= 1;
		closedPrevButton.setEnabled(closedCurrentPage > 0 && !hasOnlyOnePage);
		closedNextButton.setEnabled(closedCurrentPage < closedTotalPages - 1 && !hasOnlyOnePage);
		closedPagesCombo.setEnabled(!hasOnlyOnePage);
		closedUnderLabel.setText("/ " + closedTotalPages + " " + MessageBundle.getMessage("angal.common.pages.txt"));
		closedRowCounter.setText(rowCounterText + closedTotalRows);
	}

	private void updatePendingPaginationControls() {
		if (pendingPrevButton == null || pendingNextButton == null || pendingPagesCombo == null || pendingUnderLabel == null) {
			return;
		}

		int comboCount = pendingPagesCombo.getItemCount();
		if (comboCount != pendingTotalPages) {
			updatingPageCombo = true;
			pendingPagesCombo.removeAllItems();
			for (int i = 1; i <= pendingTotalPages; i++) {
				pendingPagesCombo.addItem(i);
			}
			updatingPageCombo = false;
		}
		if (pendingTotalPages > 0) {
			updatingPageCombo = true;
			pendingPagesCombo.setSelectedItem(pendingCurrentPage + 1);
			updatingPageCombo = false;
		}

		boolean hasOnlyOnePage = pendingTotalPages <= 1;
		pendingPrevButton.setEnabled(pendingCurrentPage > 0 && !hasOnlyOnePage);
		pendingNextButton.setEnabled(pendingCurrentPage < pendingTotalPages - 1 && !hasOnlyOnePage);
		pendingPagesCombo.setEnabled(!hasOnlyOnePage);
		pendingUnderLabel.setText("/ " + pendingTotalPages + " " + MessageBundle.getMessage("angal.common.pages.txt"));
		pendingRowCounter.setText(rowCounterText + pendingTotalRows);
	}

	private void loadCurrentPage() {
		try {
			User guarantor = getSelectedGuarantor();
			Page<Bill> billPage = billBrowserManager.getBillsWithFilters(
					null, dateFrom, dateTo, patientParent, guarantor, currentPage, PAGE_SIZE);

			List<Bill> bills = billPage.getContent();
			totalRows = billPage.getTotalElements();
			totalPages = billPage.getTotalPages();

			if (currentPage >= totalPages && currentPage > 0) {
				currentPage = totalPages - 1;
				loadCurrentPage();
				return;
			}

			jTableBills.setModel(new BillTableModel(bills));
			updatePaginationControls();
			jTableBills.updateUI();
			updateTotals();
		} catch (OHServiceException e) {
			MessageDialog.showExceptions(e);
		}
	}

	private void loadClosedBillsPage() {
		try {
			User guarantor = getSelectedGuarantor();
			Page<Bill> billPage = billBrowserManager.getBillsWithFilters(
					"C", dateFrom, dateTo, patientParent, guarantor, closedCurrentPage, PAGE_SIZE);

			List<Bill> bills = billPage.getContent();
			closedTotalRows = billPage.getTotalElements();
			closedTotalPages = billPage.getTotalPages();

			if (closedCurrentPage >= closedTotalPages && closedCurrentPage > 0) {
				closedCurrentPage = closedTotalPages - 1;
				loadClosedBillsPage();
				return;
			}

			jTableClosed.setModel(new BillTableModel(bills));
			updateClosedPaginationControls();
			jTableClosed.updateUI();
		} catch (OHServiceException e) {
			MessageDialog.showExceptions(e);
		}
	}

	private void loadPendingBillsPage() {
		try {
			User guarantor = getSelectedGuarantor();
			Page<Bill> billPage = billBrowserManager.getBillsWithFilters(
					"O", dateFrom, dateTo, patientParent, guarantor, pendingCurrentPage, PAGE_SIZE);

			List<Bill> bills = billPage.getContent();
			pendingTotalRows = billPage.getTotalElements();
			pendingTotalPages = billPage.getTotalPages();

			if (pendingCurrentPage >= pendingTotalPages && pendingCurrentPage > 0) {
				pendingCurrentPage = pendingTotalPages - 1;
				loadPendingBillsPage();
				return;
			}

			jTablePending.setModel(new BillTableModel(bills));
			updatePendingPaginationControls();
			jTablePending.updateUI();
		} catch (OHServiceException e) {
			MessageDialog.showExceptions(e);
		}
	}

	private JPanel getJPanelSouth() {
		if (jPanelSouth == null) {
			jPanelSouth = new JPanel();
			jPanelSouth.setLayout(new BoxLayout(jPanelSouth, BoxLayout.X_AXIS));
			jPanelSouth.add(getJPanelTotals());
			jPanelSouth.add(getJPanelButtons());
		}
		return jPanelSouth;
	}

	private JPanel getJPanelTotals() {
		if (jPanelTotals == null) {
			jPanelTotals = new JPanel();
			jPanelTotals.setLayout(new BoxLayout(jPanelTotals, BoxLayout.Y_AXIS));
			jPanelTotals.add(getJTableToday());
			jPanelTotals.add(getJTablePeriod());
			if (!isSingleUser) {
				jPanelTotals.add(getJTableUser());
			}
			updateTotals();
		}
		return jPanelTotals;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel(new WrapLayout());
			if (MainMenu.checkUserGrants("btnbillnew")) {
				jPanelButtons.add(getJButtonNew());
			}
			if (MainMenu.checkUserGrants("btnbilledit")) {
				jPanelButtons.add(getJButtonEdit());
			}
			if (MainMenu.checkUserGrants("btnbilldelete")) {
				jPanelButtons.add(getJButtonDelete());
			}
			if (MainMenu.checkUserGrants("btnbillreceipt") && GeneralData.RECEIPTPRINTER) {
				jPanelButtons.add(getJButtonPrintReceipt());
			}
			if (MainMenu.checkUserGrants("btnbillarchive")) {
				jPanelButtons.add(getJButtonArchive());
			}
			if (MainMenu.checkUserGrants("btnbillreport")) {
				jPanelButtons.add(getJButtonReport());
			}
			if (MainMenu.checkUserGrants("btnbillrefund") && GeneralData.ENABLEMEDICALREFUND) {
				jPanelButtons.add(getJButtonRefund());
			}
			if (SageConfig.ENABLE_SAGE_INTEGRATION) {
				jPanelButtons.add(getExportSageButton());
			}
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelRange() {
		if (jPanelRange == null) {
			jPanelRange = new JPanel();
			jPanelRange.setLayout(new BorderLayout(0, 0));
			jPanelRange.add(getPanelSupRange(), BorderLayout.NORTH);
		}
		return jPanelRange;
	}

	private JPanel getPanelSupRange() {
		if (panelSupRange == null) {
			panelSupRange = new JPanel();
			if (!isSingleUser && MainMenu.checkUserGrants("cashiersfilter")) {
				panelSupRange.add(getJComboUsers());
			}
			panelSupRange.add(getJButtonToday());
			panelSupRange.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			panelSupRange.add(getJCalendarFrom());
			panelSupRange.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			panelSupRange.add(getJCalendarTo());
			panelSupRange.add(getJComboMonths());
			panelSupRange.add(getJComboYears());
			panelSupRange.add(getPanelChoosePatient());
			if (hasBillGuarantor()) {
				panelSupRange.add(getJLabelGuarantor());
				panelSupRange.add(getJComboBoxGuarantor());
			}
		}
		return panelSupRange;
	}

	private JPanel getPanelChoosePatient() {
		JPanel priceListLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton jAffiliatePersonJButtonAdd = new JButton();
		jAffiliatePersonJButtonAdd.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
		jAffiliatePersonJButtonAdd.setToolTipText(MessageBundle.getMessage("angal.billbrowser.selectapatient.tooltip"));

		JButton jAffiliatePersonJButtonSupp = new JButton();
		jAffiliatePersonJButtonSupp.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
		jAffiliatePersonJButtonSupp.setToolTipText(MessageBundle.getMessage("angal.billbrowser.removeapatient.tooltip"));

		jAffiliatePersonJTextField = new JTextField(14);
		jAffiliatePersonJTextField.setEnabled(false);
		priceListLabelPanel.add(jAffiliatePersonJTextField);
		priceListLabelPanel.add(jAffiliatePersonJButtonAdd);
		priceListLabelPanel.add(jAffiliatePersonJButtonSupp);

		jAffiliatePersonJButtonAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				SelectPatient selectPatient = new SelectPatient(BillBrowser.this, false, true);
				selectPatient.addSelectionListener(BillBrowser.this);
				selectPatient.setVisible(true);
				Patient pat = selectPatient.getPatient();

				try {
					patientSelected(pat);
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			}
		});

		jAffiliatePersonJButtonSupp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				patientParent = null;
				jAffiliatePersonJTextField.setText("");
				billInserted(null);
			}
		});

		return priceListLabelPanel;
	}

	private JTabbedPane getJTabbedPaneBills() {
		if (jTabbedPaneBills == null) {
			jTabbedPaneBills = new JTabbedPane();

			JPanel allPanel = new JPanel(new BorderLayout());
			allPanel.add(getJScrollPaneBills(), BorderLayout.CENTER);
			allPanel.add(getPaginationPanel(), BorderLayout.SOUTH);
			jTabbedPaneBills.addTab(MessageBundle.getMessage("angal.billbrowser.bills.title"), allPanel);

			JPanel closedPanel = new JPanel(new BorderLayout());
			closedPanel.add(getJScrollPaneClosed(), BorderLayout.CENTER);
			closedPanel.add(getClosedPaginationPanel(), BorderLayout.SOUTH);
			jTabbedPaneBills.addTab(MessageBundle.getMessage("angal.billbrowser.closed.title"), closedPanel);

			JPanel pendingPanel = new JPanel(new BorderLayout());
			pendingPanel.add(getJScrollPanePending(), BorderLayout.CENTER);
			pendingPanel.add(getPendingPaginationPanel(), BorderLayout.SOUTH);
			jTabbedPaneBills.addTab(MessageBundle.getMessage("angal.billbrowser.pending.title"), pendingPanel);

			jTabbedPaneBills.addChangeListener(e -> {
				int selectedIndex = jTabbedPaneBills.getSelectedIndex();
				if (selectedIndex == 0) {
					loadCurrentPage();
				} else if (selectedIndex == 1) {
					loadClosedBillsPage();
				} else if (selectedIndex == 2) {
					loadPendingBillsPage();
				}
			});
		}
		return jTabbedPaneBills;
	}

	private JScrollPane getJScrollPaneBills() {
		if (jScrollPaneBills == null) {
			jScrollPaneBills = new JScrollPane();
			jScrollPaneBills.setViewportView(getJTableBills());
		}
		return jScrollPaneBills;
	}

	private JTable getJTableBills() {
		if (jTableBills == null) {
			jTableBills = new JTable();
			jTableBills.setModel(new BillTableModel(new ArrayList<>()));
			decorateTable(jTableBills);
			jTableBills.setAutoCreateColumnsFromModel(false);
			jTableBills.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTableBills.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTableBills.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
			jTableBills.addMouseListener(new MouseDoubleClickApapter());
		}
		return jTableBills;
	}

	private JScrollPane getJScrollPanePending() {
		if (jScrollPanePending == null) {
			jScrollPanePending = new JScrollPane();
			jScrollPanePending.setViewportView(getJTablePending());
		}
		return jScrollPanePending;
	}

	private JTable getJTablePending() {
		if (jTablePending == null) {
			jTablePending = new JTable();
			jTablePending.setModel(new BillTableModel("O", NO_USERNAME));
			decorateTable(jTablePending);
			jTablePending.setAutoCreateColumnsFromModel(false);
			jTablePending.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTablePending.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTablePending.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
			jTablePending.addMouseListener(new MouseDoubleClickApapter());
		}
		return jTablePending;
	}

	private JScrollPane getJScrollPaneClosed() {
		if (jScrollPaneClosed == null) {
			jScrollPaneClosed = new JScrollPane();
			jScrollPaneClosed.setViewportView(getJTableClosed());
		}
		return jScrollPaneClosed;
	}

	private JTable getJTableClosed() {
		if (jTableClosed == null) {
			jTableClosed = new JTable();
			jTableClosed.setModel(new BillTableModel("C", NO_USERNAME));
			decorateTable(jTableClosed);
			jTableClosed.setAutoCreateColumnsFromModel(false);
			jTableClosed.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTableClosed.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTableClosed.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
			jTableClosed.addMouseListener(new MouseDoubleClickApapter());
		}
		return jTableClosed;
	}

	private JTable getJTableToday() {
		if (jTableToday == null) {
			jTableToday = new JTable();
			jTableToday.setModel(
					new DefaultTableModel(new Object[][] {
							{
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.paidtodaycolon.txt") + "</b></html>",
									currencyCod,
									totalToday,
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.notpaidcolon.txt") + "</b></html>",
									currencyCod,
									balanceToday
							}
					},
							new String[] { "", "", "", "", "", "" }) {

						private static final long serialVersionUID = 1L;
						Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class };

						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return types[columnIndex];
						}

						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					});
			jTableToday.getColumnModel().getColumn(1).setMinWidth(3);
			jTableToday.getColumnModel().getColumn(4).setMinWidth(3);
			jTableToday.setRowSelectionAllowed(false);
			jTableToday.setGridColor(Color.WHITE);
		}
		return jTableToday;
	}

	private JTable getJTablePeriod() {
		if (jTablePeriod == null) {
			jTablePeriod = new JTable();
			jTablePeriod.setModel(new DefaultTableModel(
					new Object[][] {
							{
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.paidperiodcolon.txt") + "</b></html>",
									currencyCod,
									totalPeriod,
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.notpaidcolon.txt") + "</b></html>",
									currencyCod,
									balancePeriod }
					},
					new String[] { "", "", "", "", "", "" }) {

				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class };

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTablePeriod.getColumnModel().getColumn(1).setMinWidth(3);
			jTablePeriod.getColumnModel().getColumn(4).setMinWidth(3);
			jTablePeriod.setRowSelectionAllowed(false);
			jTablePeriod.setGridColor(Color.WHITE);
		}
		return jTablePeriod;
	}

	private JTable getJTableUser() {
		if (jTableUser == null) {
			jTableUser = new JTable();
			jTableUser.setModel(
					new DefaultTableModel(new Object[][] { {
							"<html><b>" + user + ' ' + MessageBundle.getMessage("angal.billbrowser.todaycolon.txt") + "</b></html>",
							userToday,
							"<html><b>" + user + ' ' + MessageBundle.getMessage("angal.billbrowser.periodcolon.txt") + "</b></html>",
							userPeriod
					} },
							new String[] { "", "", "", "" }) {

						private static final long serialVersionUID = 1L;
						Class<?>[] types = new Class<?>[] { JLabel.class, Double.class, JLabel.class, Double.class };

						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return types[columnIndex];
						}

						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					});
			jTableUser.setRowSelectionAllowed(false);
			jTableUser.setGridColor(Color.WHITE);
		}
		return jTableUser;
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.billbrowser.newbill.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.newbill.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				PatientBillEdit newBill = new PatientBillEdit(this, new Bill(), true);
				newBill.addPatientBillListener(this);
				newBill.setVisible(true);
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.billbrowser.editbill.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.editbill.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				if (jScrollPaneBills.isShowing()) {
					if (!isOnlyOneSelected(jTableBills)) {
						return;
					}
					int rowSelected = jTableBills.getSelectedRow();
					Bill editBill = (Bill) jTableBills.getValueAt(rowSelected, -1);
					if (MainMenu.checkUserGrants("editclosedbills") || editBill.getStatus().equals("O")) {
						PatientBillEdit pbe = new PatientBillEdit(this, editBill, false);
						pbe.addPatientBillListener(this);
						pbe.setVisible(true);
					} else {
						MessageDialog.error(this, "angal.billbrowser.youcannoteditaclosedbill.msg");
						return;
					}
				}
				if (jScrollPanePending.isShowing()) {
					if (!isOnlyOneSelected(jTablePending)) {
						return;
					}
					int rowSelected = jTablePending.getSelectedRow();
					Bill editBill = (Bill) jTablePending.getValueAt(rowSelected, -1);
					PatientBillEdit pbe = new PatientBillEdit(this, editBill, false);
					pbe.addPatientBillListener(this);
					pbe.setVisible(true);
				}
				if (jScrollPaneClosed.isShowing()) {
					if (!isOnlyOneSelected(jTableClosed)) {
						return;
					}
					int rowSelected = jTableClosed.getSelectedRow();
					Bill editBill = (Bill) jTableClosed.getValueAt(rowSelected, -1);
					if (user.equals("admin")) {
						PatientBillEdit pbe = new PatientBillEdit(this, editBill, false);
						pbe.addPatientBillListener(this);
						pbe.setVisible(true);
					} else {
						MessageDialog.error(this, "angal.billbrowser.youcannoteditaclosedbill.msg");
					}
				}
			});
		}
		return jButtonEdit;
	}

	private JButton getJButtonArchive() {
		if (jButtonArchive == null) {
			jButtonArchive = new JButton(MessageBundle.getMessage("angal.billbrowser.archive.btn"));
			jButtonArchive.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.archive.btn.key"));
			jButtonArchive.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					StringBuilder sb = new StringBuilder();
					sb.append("<html><body>");
					sb.append("<h4>" + MessageBundle.getMessage("angal.billbrowser.realywanttoarchivebills.confirm") + "</h4><br>");
					sb.append("<p>" + MessageBundle.getMessage("angal.billbrowser.operationmaytakefewminutes") + "</p>");
					sb.append("</body></html>");

					int ok = JOptionPane.showConfirmDialog(BillBrowser.this,
							sb.toString(),
							MessageBundle.getMessage("angal.billbrowser.archive.btn"),
							JOptionPane.YES_NO_OPTION);

					if (ok == JOptionPane.YES_OPTION) {
						final JDialog spinnerDialog = new JDialog();
						JPanel panel = new JPanel(new GridBagLayout());
						panel.add(new JLabel(
								MessageBundle.getMessage("angal.billbrowser.billsarchiveisprogressing"), JLabel.CENTER
						));
						spinnerDialog.getContentPane().add(panel);
						spinnerDialog.setBounds(getBounds());
						spinnerDialog.setLocation(getLocation());
						spinnerDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
						spinnerDialog.setModal(true);
						spinnerDialog.setUndecorated(true);
						spinnerDialog.getRootPane().setOpaque(false);

						SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

							@Override
							protected Void doInBackground() throws Exception {
								ArchiveManager archiveManager = Context.getApplicationContext().getBean(ArchiveManager.class);
								archiveManager.archiveClosedBills();
								return null;
							}

							@Override
							protected void done() {

								spinnerDialog.setVisible(false);
								spinnerDialog.dispose();

								try {
									get();
									JOptionPane.showMessageDialog(
											BillBrowser.this,
											MessageBundle.getMessage("angal.billbrowser.archive.process.completed"),
											MessageBundle.getMessage("angal.billbrowser.archive.btn"),
											JOptionPane.INFORMATION_MESSAGE);

									billInserted(null);

								} catch (InterruptedException e) {

									Thread.currentThread().interrupt();

								} catch (ExecutionException e) {

									Throwable cause = e.getCause();

									if (cause instanceof OHServiceException) {

										OHServiceExceptionUtil.showMessages(
												(OHServiceException) cause,
												BillBrowser.this);

									} else {

										LOGGER.error("Unexpected error", cause);

										OHServiceExceptionUtil.showMessages(
												new OHServiceException(
														cause,
														new OHExceptionMessage(
																"angal.accounting.archive.execution.error"
														)
												),
												BillBrowser.this);
									}
								}
							}

						};
						worker.execute();
						spinnerDialog.setVisible(true);

						try {
							worker.get();
							JOptionPane.showMessageDialog(BillBrowser.this,
									MessageBundle.getMessage("angal.billbrowser.archive.process.completed"),
									MessageBundle.getMessage("angal.billbrowser.archive.btn"),
									JOptionPane.INFORMATION_MESSAGE);
							billInserted(null);

						} catch (InterruptedException | ExecutionException e1) {
							LOGGER.error("Erreur lors de l'archivage via ArchiveManager", e1);
							OHExceptionMessage exceptionMessage = new OHExceptionMessage(e1.getMessage());
							OHServiceExceptionUtil.showMessages(new OHServiceException(exceptionMessage), BillBrowser.this);
						}
					}
				}
			});
		}
		return jButtonArchive;
	}

	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.billbrowser.deletebill.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.deletebill.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				Bill deleteBill = null;
				int ok = JOptionPane.NO_OPTION;
				if (jScrollPaneBills.isShowing()) {
					if (!isOnlyOneSelected(jTableBills)) {
						return;
					}
					int rowSelected = jTableBills.getSelectedRow();
					deleteBill = (Bill) jTableBills.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (jScrollPanePending != null && jScrollPanePending.isShowing()) {
					if (!isOnlyOneSelected(jTablePending)) {
						return;
					}
					int rowSelected = jTablePending.getSelectedRow();
					deleteBill = (Bill) jTablePending.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (jScrollPaneClosed != null && jScrollPaneClosed.isShowing()) {
					if (!isOnlyOneSelected(jTableClosed)) {
						return;
					}
					int rowSelected = jTableClosed.getSelectedRow();
					deleteBill = (Bill) jTableClosed.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (ok == JOptionPane.YES_OPTION) {
					try {
						billBrowserManager.deleteBill(deleteBill);
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
				billInserted(null);
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonPrintReceipt() {
		if (jButtonPrintReceipt == null) {
			jButtonPrintReceipt = new JButton(MessageBundle.getMessage("angal.billbrowser.receipt.btn"));
			jButtonPrintReceipt.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.receipt.btn.key"));
			jButtonPrintReceipt.addActionListener(actionEvent -> {
				try {
					if (jScrollPaneBills.isShowing()) {
						int rowsSelected = jTableBills.getSelectedRowCount();
						if (rowsSelected == 1) {
							int rowSelected = jTableBills.getSelectedRow();
							Bill editBill = (Bill) jTableBills.getValueAt(rowSelected, -1);
							if (editBill.getStatus().equals("C")) {
								new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, true, true);
							} else if (editBill.getStatus().equals("D")) {
								MessageDialog.error(this, "angal.billbrowser.thebilldeleted.msg");
								return;
							} else if (editBill.getStatus().equals("O") && GeneralData.ALLOWPRINTOPENEDBILL) {
								new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, true, true);
							} else {
								MessageDialog.error(this, "angal.billbrowser.thebillisstillopen.msg");
								return;
							}
						} else if (rowsSelected > 1) {
							if (patientParent == null) {
								MessageDialog.error(this, "angal.billbrowser.pleaseselectonlyonebill.msg");
								return;
							}
							Bill billTemp;
							int[] billIdIndex = jTableBills.getSelectedRows();
							List<Integer> billsIdList = new ArrayList<>();

							for (int idIndex : billIdIndex) {
								billTemp = (Bill) jTableBills.getValueAt(idIndex, -1);
								if (!billTemp.getStatus().equals("D")) {
									billsIdList.add(billTemp.getId());
								}
							}
							String fromDate = dateFrom.format(DATE_TIME_FORMATTER);
							String toDate = dateTo.format(DATE_TIME_FORMATTER);
							new GenericReportBill(billsIdList.get(0), GeneralData.PATIENTBILLGROUPED, patientParent, billsIdList, fromDate, toDate, true, true);
						} else {
							throw new Exception();
						}
					}
					if (jScrollPanePending.isShowing()) {
						int rowsSelected = jTablePending.getSelectedRowCount();
						if (rowsSelected == 1) {
							int rowSelected = jTablePending.getSelectedRow();
							Bill editBill = (Bill) jTablePending.getValueAt(rowSelected, -1);
							if (editBill.getStatus().equals("O") && GeneralData.ALLOWPRINTOPENEDBILL) {
								new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, true, true);
							} else {
								PatientBillEdit pbe = new PatientBillEdit(this, editBill, false);
								pbe.addPatientBillListener(this);
								pbe.setVisible(true);
							}
						} else if (rowsSelected > 1) {
							if (patientParent == null) {
								MessageDialog.error(this, "angal.billbrowser.pleaseselectonlyonebill.msg");
								return;
							} else if (GeneralData.ALLOWPRINTOPENEDBILL) {
								Bill billTemp;
								int[] billIdIndex = jTablePending.getSelectedRows();
								List<Integer> billsIdList = new ArrayList<>();

								for (int idIndex : billIdIndex) {
									billTemp = (Bill) jTablePending.getValueAt(idIndex, -1);
									billsIdList.add(billTemp.getId());
								}
								String fromDate = dateFrom.format(DATE_TIME_FORMATTER);
								String toDate = dateTo.format(DATE_TIME_FORMATTER);
								new GenericReportBill(billsIdList.get(0), GeneralData.PATIENTBILLGROUPED, patientParent, billsIdList, fromDate, toDate, true, true);
							} else {
								MessageDialog.error(this, "angal.billbrowser.thebillisstillopen.msg");
								return;
							}
						} else {
							throw new Exception();
						}
					}
					if (jScrollPaneClosed.isShowing()) {
						int rowsSelected = jTableClosed.getSelectedRowCount();
						if (rowsSelected == 1) {
							int rowSelected = jTableClosed.getSelectedRow();
							Bill editBill = (Bill) jTableClosed.getValueAt(rowSelected, -1);
							new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL);
						} else if (rowsSelected > 1) {
							MessageDialog.error(this, "angal.billbrowser.pleaseselectonlyonebill.msg");
						} else {
							throw new Exception();
						}
					}
				} catch (Exception ex) {
					MessageDialog.error(this, "angal.billbrowser.pleaseselectabill.msg");
				}
			});
		}
		return jButtonPrintReceipt;
	}

	private JButton getJButtonReport() {
		if (jButtonReport == null) {
			jButtonReport = new JButton(MessageBundle.getMessage("angal.billbrowser.report.btn"));
			jButtonReport.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.report.btn.key"));
			jButtonReport.addActionListener(actionEvent -> {
				List<String> options = new ArrayList<>();
				if (patientParent != null) {
					options.add(MessageBundle.getMessage("angal.billbrowser.patientstatement.txt"));
				}
				options.add(MessageBundle.getMessage("angal.billbrowser.todayclosure.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.today.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.period.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.thismonth.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.selectmonth.txt"));
				if (patientParent == null) {
					options.add(MessageBundle.getMessage("angal.billbrowser.patientstatement.txt"));
				}
				Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png");
				String option = (String) MessageDialog.inputDialog(this,
						icon,
						options.toArray(),
						options.get(0),
						"angal.billbrowser.pleaseselectareport.msg");
				if (option == null) {
					return;
				}

				String from = null;
				String to = null;

				int i = 0;

				if (patientParent != null && options.indexOf(option) == i) {
					new GenericReportPatient(patientParent.getCode(), GeneralData.PATIENTBILLSTATEMENT);
					return;
				}
				if (options.indexOf(option) == i) {
					from = TimeTools.formatDateTime(dateToday0, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
					to = TimeTools.formatDateTime(dateToday24, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
					String user;
					if (isSingleUser) {
						user = "admin";
					} else {
						user = UserBrowsingManager.getCurrentUser();
					}
					new GenericReportUserInDate(from, to, user, "BillsReportUserInDate");
					return;
				}
				if (options.indexOf(option) == ++i) {
					from = TimeTools.formatDateTime(dateToday0, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(dateToday24, DATE_FORMAT_DD_MM_YYYY);
				}
				if (options.indexOf(option) == ++i) {
					from = TimeTools.formatDateTime(dateFrom, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(dateTo, DATE_FORMAT_DD_MM_YYYY);
				}
				if (options.indexOf(option) == ++i) {
					month = jComboBoxMonths.getMonth() + 1;
					LocalDateTime thisMonthFrom = dateFrom.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(1)
							.atStartOfDay()
							.truncatedTo(ChronoUnit.SECONDS);
					LocalDateTime thisMonthTo = dateTo.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(YearMonth.of(dateFrom.getYear(), month).lengthOfMonth())
							.atStartOfDay()
							.toLocalDate()
							.atTime(LocalTime.MAX)
							.truncatedTo(ChronoUnit.SECONDS);
					from = TimeTools.formatDateTime(thisMonthFrom, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(thisMonthTo, DATE_FORMAT_DD_MM_YYYY);
				}
				if (options.indexOf(option) == ++i) {
					icon = new ImageIcon("rsc/icons/calendar_dialog.png");
					int month;
					JMonthChooser monthChooser = new JMonthChooser();

					int r = JOptionPane.showConfirmDialog(this,
							monthChooser,
							MessageBundle.getMessage("angal.billbrowser.month.txt"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE,
							icon);

					if (r == JOptionPane.OK_OPTION) {
						month = monthChooser.getMonth() + 1;
					} else {
						return;
					}

					LocalDateTime thisMonthFrom = dateFrom.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(1)
							.atStartOfDay()
							.truncatedTo(ChronoUnit.SECONDS);
					LocalDateTime thisMonthTo = dateTo.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(YearMonth.of(dateFrom.getYear(), month).lengthOfMonth())
							.atStartOfDay()
							.toLocalDate()
							.atTime(LocalTime.MAX)
							.truncatedTo(ChronoUnit.SECONDS);
					from = TimeTools.formatDateTime(thisMonthFrom, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(thisMonthTo, DATE_FORMAT_DD_MM_YYYY);
				}
				if (patientParent == null && options.indexOf(option) == ++i) {
					Patient patient = null;
					Bill bill = null;
					int selectedRow;
					int currentTab = jTabbedPaneBills.getSelectedIndex();
					switch (currentTab) {
						case 0:
							selectedRow = jTableBills.getSelectedRow();
							if (selectedRow >= 0) {
								bill = (Bill) jTableBills.getValueAt(selectedRow, -1);
							}
							break;
						case 1:
							selectedRow = jTablePending.getSelectedRow();
							if (selectedRow >= 0) {
								bill = (Bill) jTablePending.getValueAt(selectedRow, -1);
							}
							break;
						case 2:
							selectedRow = jTableClosed.getSelectedRow();
							if (selectedRow >= 0) {
								bill = (Bill) jTableClosed.getValueAt(selectedRow, -1);
							}
							break;
						default:
							break;
					}
					if (bill != null) {
						patient = bill.getBillPatient();
					}
					if (patient == null) {
						MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
						return;
					}
					new GenericReportPatient(patient.getCode(), GeneralData.PATIENTBILLSTATEMENT);
					return;
				}

				options = new ArrayList<>();
				options.add(MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebt.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.fullreportallbills.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.paymentsbyuser.txt"));
				options.add(MessageBundle.getMessage("angal.report.oh004alldebtsgroupedbyitemcategories.txt"));

				icon = new ImageIcon("rsc/icons/list_dialog.png");
				option = (String) MessageDialog.inputDialog(this,
						icon,
						options.toArray(),
						options.get(0),
						"angal.billbrowser.pleaseselectareport.msg");
				if (option == null) {
					return;
				}

				if (options.indexOf(option) == 0) {
					new GenericReportFromDateToDate(from, to, "rpt_stat", GeneralData.BILLSREPORTPENDING,
							MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebt.txt"), false);
				}
				if (options.indexOf(option) == 1) {
					new GenericReportFromDateToDate(from, to, "rpt_stat", GeneralData.BILLSREPORT,
							MessageBundle.getMessage("angal.billbrowser.fullreportallbills.txt"), false);
				}
				if (options.indexOf(option) == 2) {
					new GenericReportFromDateToDate(from, to, "rpt_base", "BillsPaymentReportUserAllInDate",
							MessageBundle.getMessage("angal.billbrowser.paymentsbyuser.txt"), false);
				}
				if (options.indexOf(option) == 3) {
					new GenericReportFromDateToDate(from, to, "rpt_base", "OH004_03_AllDebtsGroupByItemCategories",
							MessageBundle.getMessage("angal.report.oh004alldebtsgroupedbyitemcategories.txt"), false);
				}
			});
		}
		return jButtonReport;
	}

	private JButton getJButtonRefund() {
		if (jButtonRefund == null) {
			jButtonRefund = new JButton(MessageBundle.getMessage("angal.billbrowser.refund"));
			jButtonRefund.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.refund.key"));
			jButtonRefund.setIcon(new ImageIcon("rsc/icons/money_button.png"));
			jButtonRefund.addActionListener(actionEvent -> {
				Bill selectedBill = getSelectedBillFromActiveTab();
				if (selectedBill == null) {
					MessageDialog.error(this, "angal.billbrowser.pleaseselectabill.msg");
					return;
				}
				if (!"C".equalsIgnoreCase(selectedBill.getStatus())) {
					MessageDialog.error(this, "angal.billbrowser.onlyclosedbillcanberefunded.msg");
					return;
				}
				if (selectedBill.getParentId() != null && selectedBill.getParentId() != 0) {
					MessageDialog.error(this, "angal.billbrowser.cannotrefundarefundbill.msg");
					return;
				}
				BillRefund dialog = new BillRefund(BillBrowser.this, selectedBill);
				dialog.addPatientBillListener(BillBrowser.this);
				dialog.setVisible(true);
			});
		}
		return jButtonRefund;
	}

	private Bill getSelectedBillFromActiveTab() {
		if (jScrollPaneBills.isShowing() && jTableBills.getSelectedRow() >= 0) {
			return (Bill) jTableBills.getValueAt(jTableBills.getSelectedRow(), -1);
		}
		if (jScrollPaneClosed.isShowing() && jTableClosed.getSelectedRow() >= 0) {
			return (Bill) jTableClosed.getValueAt(jTableClosed.getSelectedRow(), -1);
		}
		return null;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> {
				dispose();
			});
		}
		return jButtonClose;
	}

	private GoodDateChooser getJCalendarFrom() {
		if (jCalendarFrom == null) {
			jCalendarFrom = new GoodDateChooser(LocalDate.now());
			jCalendarFrom.addDateChangeListener(event -> {
				LocalDate newDate = event.getNewDate();
				if (newDate != null) {
					dateFrom = newDate.atStartOfDay();
					currentPage = 0;
					jButtonToday.setEnabled(true);
					loadCurrentPage();
				}
			});
		}
		return jCalendarFrom;
	}

	private JButton getExportSageButton() {
		if (exportSageButton == null) {
			exportSageButton = new JButton(MessageBundle.getMessage("angal.billbrowser.exportsage"));
			exportSageButton.setMnemonic(KeyEvent.VK_E);
			exportSageButton.addActionListener(e -> {
				JFileChooser fcTxt = new JFileChooser();
				fcTxt.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int iRetVal = fcTxt.showSaveDialog(this);
				if (iRetVal == JFileChooser.APPROVE_OPTION) {
					File txtSageDirectory = fcTxt.getSelectedFile();

					if (!txtSageDirectory.exists()) {
						txtSageDirectory.mkdirs();
					}

					String strDate = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
					String from = dateFrom.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
					String to = dateTo.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

					final File salesFile = new File(txtSageDirectory.getAbsoluteFile() + File.separator
							+ "exportvente_" + strDate + "_from_" + from + "_to_" + to + ".txt");
					final File cashFile = new File(txtSageDirectory.getAbsoluteFile() + File.separator
							+ "exportcaisse_" + strDate + "_from_" + from + "_to_" + to + ".txt");
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							billBrowserManager.exportSagePaymentsStreaming(cashFile, dateFrom, dateTo);
							billBrowserManager.exportSagePaymentsStreaming(salesFile, dateFrom, dateTo);
							return null;
						}

						@Override
						protected void done() {
							try {
								get();
								MessageDialog.info(BillBrowser.this, "angal.medicalstock.exportsage.succes");
							} catch (Exception ex) {
								MessageDialog.error(BillBrowser.this, "angal.medicalstock.exportsage.error");
								LOGGER.error("Export to sage error: ", ex);
							}
						}
					};
					worker.execute();
				}
			});
		}
		return exportSageButton;
	}

	private GoodDateChooser getJCalendarTo() {
		if (jCalendarTo == null) {
			jCalendarTo = new GoodDateChooser(LocalDate.now());
			jCalendarTo.addDateChangeListener(event -> {
				LocalDate newDate = event.getNewDate();
				if (newDate != null) {
					dateTo = newDate.atTime(LocalTime.MAX);
					currentPage = 0;
					jButtonToday.setEnabled(true);
					loadCurrentPage();
				}
			});
		}
		return jCalendarTo;
	}

	private JButton getJButtonToday() {
		if (jButtonToday == null) {
			jButtonToday = new JButton(MessageBundle.getMessage("angal.billbrowser.today.btn"));
			jButtonToday.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.today.btn.key"));
			jButtonToday.addActionListener(actionEvent -> {
				dateFrom = dateToday0;
				dateTo = dateToday24;
				jCalendarFrom.setDate(dateFrom.toLocalDate());
				jCalendarTo.setDate(dateTo.toLocalDate());
				jButtonToday.setEnabled(false);
			});
			jButtonToday.setEnabled(false);
		}
		return jButtonToday;
	}

	private JComboBox<String> getJComboUsers() {
		if (jComboUsers == null) {
			jComboUsers = new JComboBox<>();

			for (String user : users) {
				jComboUsers.addItem(user);
			}

			if (users.contains(user)) {
				jComboUsers.setSelectedItem(user);
			} else {
				jComboUsers.setSelectedItem("admin");
			}

			jComboUsers.addActionListener(actionEvent -> {
				user = (String) jComboUsers.getSelectedItem();
				jTableUser.setValueAt("<html><b>" + user + ' ' + MessageBundle.getMessage("angal.billbrowser.todaycolon.txt") + "</b></html>", 0, 0);
				jTableUser.setValueAt("<html><b>" + user + ' ' + MessageBundle.getMessage("angal.billbrowser.periodcolon.txt") + "</b></html>", 0, 2);
				updateTotals();
				currentPage = 0;
				closedCurrentPage = 0;
				pendingCurrentPage = 0;
				updateTables();
			});
		}
		return jComboUsers;
	}

	private JComboBox<User> getJComboBoxGuarantor() {
		if (jComboBoxGuarantor == null) {
			jComboBoxGuarantor = new JComboBox<>();
			try {
				jComboBoxGuarantor.addItem(null);
				List<User> users = userBrowserManager.getUser();
				for (User user : users) {
					jComboBoxGuarantor.addItem(user);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			jComboBoxGuarantor.setPreferredSize(new Dimension(150, 25));
			jComboBoxGuarantor.setFont(new Font("Arial", Font.PLAIN, 14));
			jComboBoxGuarantor.addActionListener(actionEvent -> {
				User selectedGuarantor = (User) jComboBoxGuarantor.getSelectedItem();
				try {
					if (selectedGuarantor != null) {
						updateDataSetByGuarantor(dateFrom, dateTo, patientParent, selectedGuarantor);
					} else if (patientParent == null) {
						updateDataSet(dateFrom, dateTo);
					} else {
						updateDataSet(dateFrom, dateTo, patientParent);
					}
					updateTables();
					updateTotals();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}
		return jComboBoxGuarantor;
	}

	private JMonthChooser getJComboMonths() {
		if (jComboBoxMonths == null) {
			jComboBoxMonths = new JMonthChooser();
			jComboBoxMonths.addPropertyChangeListener("month", propertyChangeEvent -> {
				month = jComboBoxMonths.getMonth() + 1;
				dateFrom = dateFrom.toLocalDate()
						.withMonth(month)
						.withDayOfMonth(1)
						.atStartOfDay();
				dateTo = dateTo.toLocalDate()
						.withMonth(month)
						.withDayOfMonth(YearMonth.of(dateFrom.getYear(), month).lengthOfMonth())
						.atStartOfDay()
						.toLocalDate()
						.atTime(LocalTime.MAX);
				jCalendarFrom.setDate(dateFrom.toLocalDate());
				jCalendarTo.setDate(dateTo.toLocalDate());
			});
		}
		return jComboBoxMonths;
	}

	private JYearChooser getJComboYears() {
		if (jComboBoxYears == null) {
			jComboBoxYears = new JYearChooser();
			jComboBoxYears.getModel().addChangeListener(e -> {
				year = jComboBoxYears.getYear();
				dateFrom = LocalDate.now()
						.withYear(year)
						.withMonth(1)
						.withDayOfMonth(1)
						.atStartOfDay();
				dateTo = LocalDate.now()
						.withYear(year)
						.withMonth(12)
						.withDayOfMonth(YearMonth.of(year, 12).lengthOfMonth())
						.atStartOfDay()
						.toLocalDate()
						.atTime(LocalTime.MAX);
				jCalendarFrom.setDate(dateFrom.toLocalDate());
				jCalendarTo.setDate(dateTo.toLocalDate());
			});
		}
		return jComboBoxYears;
	}

	private JPanel getClosedPaginationPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
		panel.setBorder(BorderFactory.createEtchedBorder());

		closedPrevButton = new JButton("<");
		closedPrevButton.addActionListener(e -> {
			if (closedCurrentPage > 0) {
				closedCurrentPage--;
				loadClosedBillsPage();
			}
		});

		closedPagesCombo = new JComboBox<>();
		closedPagesCombo.setPreferredSize(new Dimension(70, 25));
		closedPagesCombo.addActionListener(e -> {
			if (!updatingPageCombo && closedPagesCombo.getSelectedItem() != null) {
				int selected = (Integer) closedPagesCombo.getSelectedItem();
				if (selected - 1 != closedCurrentPage) {
					closedCurrentPage = selected - 1;
					loadClosedBillsPage();
				}
			}
		});

		closedNextButton = new JButton(">");
		closedNextButton.addActionListener(e -> {
			if (closedCurrentPage < closedTotalPages - 1) {
				closedCurrentPage++;
				loadClosedBillsPage();
			}
		});

		closedUnderLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.common.pages.txt"));

		closedRowCounter = new JLabel(rowCounterText + "0");

		panel.add(closedPrevButton);
		panel.add(closedPagesCombo);
		panel.add(closedUnderLabel);
		panel.add(closedNextButton);
		panel.add(closedRowCounter);

		return panel;
	}

	private JPanel getPendingPaginationPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
		panel.setBorder(BorderFactory.createEtchedBorder());

		pendingPrevButton = new JButton("<");
		pendingPrevButton.addActionListener(e -> {
			if (pendingCurrentPage > 0) {
				pendingCurrentPage--;
				loadPendingBillsPage();
			}
		});

		pendingPagesCombo = new JComboBox<>();
		pendingPagesCombo.setPreferredSize(new Dimension(70, 25));
		pendingPagesCombo.addActionListener(e -> {
			if (!updatingPageCombo && pendingPagesCombo.getSelectedItem() != null) {
				int selected = (Integer) pendingPagesCombo.getSelectedItem();
				if (selected - 1 != pendingCurrentPage) {
					pendingCurrentPage = selected - 1;
					loadPendingBillsPage();
				}
			}
		});

		pendingNextButton = new JButton(">");
		pendingNextButton.addActionListener(e -> {
			if (pendingCurrentPage < pendingTotalPages - 1) {
				pendingCurrentPage++;
				loadPendingBillsPage();
			}
		});

		pendingUnderLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.common.pages.txt"));

		pendingRowCounter = new JLabel(rowCounterText + "0");

		panel.add(pendingPrevButton);
		panel.add(pendingPagesCombo);
		panel.add(pendingUnderLabel);
		panel.add(pendingNextButton);
		panel.add(pendingRowCounter);

		return panel;
	}

	private void decorateTable(JTable table) {
		IntStream.range(0, columnsWidth.length).forEach(idx -> {
			table.getColumnModel().getColumn(idx).setMinWidth(columnsWidth[idx]);
			if (!columnsResizable[idx]) {
				table.getColumnModel().getColumn(idx).setMaxWidth(maxWidth[idx]);
				if (!showColumn[idx]) {
					table.getColumnModel().getColumn(idx).setWidth(0);
					table.getColumnModel().getColumn(idx).setMinWidth(0);
					table.getColumnModel().getColumn(idx).setMaxWidth(0);
				}
			}
			if (alignStringCenter[idx]) {
				table.getColumnModel().getColumn(idx).setCellRenderer(new StringCenterTableCellRenderer());
				if (alingStringBoldCenter[idx]) {
					table.getColumnModel().getColumn(idx).setCellRenderer(new StringCenterBoldTableCellRenderer());
				}
			}
		});
	}

	private void formatCellByBillStatus(JTable table, int row, Component cell) {
		int statusColumn = table.getColumnModel().getColumnIndex(MessageBundle.getMessage("angal.common.status.txt").toUpperCase());
		if ((table.getValueAt(row, statusColumn)).equals("C")) {
			cell.setForeground(Color.GRAY);
		}
		if ((table.getValueAt(row, statusColumn)).equals("D")) {
			cell.setForeground(Color.RED);
		}
	}

	private boolean isOnlyOneSelected(JTable table) {
		int rowsSelected = table.getSelectedRowCount();
		if (rowsSelected > 1) {
			MessageDialog.error(this, "angal.billbrowser.pleaseselectonlyonebill.msg");
			return false;
		}
		if (rowsSelected == 0) {
			MessageDialog.error(this, "angal.billbrowser.pleaseselectabill.msg");
			return false;
		}
		return true;
	}

	public void patientSelected(Patient patient) throws OHServiceException {
		patientParent = patient;
		jAffiliatePersonJTextField.setText(patientParent != null ? patientParent.getName() : "");

		if (patientParent != null) {
			updateDataSet(dateFrom, dateTo, patientParent);
			updateTables();
			updateTotals();
		}
	}

	private void initComponents() {
		add(getJPanelRange(), BorderLayout.NORTH);
		add(getJTabbedPaneBills(), BorderLayout.CENTER);
		add(getJPanelSouth(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.billbrowser.patientbillmanagment.title"));
		setMinimumSize(new Dimension(1150, 600));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		pack();
	}

	public class BillTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<Bill> tableArray = new ArrayList<>();

		public BillTableModel(List<Bill> bills) {
			this.tableArray = bills != null ? bills : new ArrayList<>();
		}

		public BillTableModel(String status, String username) {
			this.tableArray = new ArrayList<>();
		}

		private void loadData(String status, String username) {
			try {
				tableArray = new BillDataLoader(billPeriod, billFromPayments, patientParent, billBrowserManager).loadBills(status, username);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error("BillDataLoader error: ", ohServiceException);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnsClasses[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			if (tableArray == null) {
				return 0;
			}
			return tableArray.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			int index = -1;
			Bill thisBill = tableArray.get(r);
			if (c == index) return thisBill;
			if (c == ++index) return thisBill.getUser();
			if (c == ++index) return thisBill.getId();
			if (c == ++index) return TimeTools.formatDateTime(thisBill.getDate(), DATE_FORMAT_DD_MM_YYYY_HH_MM);
			if (c == ++index) {
				int patID = thisBill.getBillPatient().getCode();
				return patID == 0 ? "" : String.valueOf(patID);
			}
			if (c == ++index) return thisBill.getPatName();
			if (c == ++index) return thisBill.getAmount();
			if (c == ++index) return TimeTools.formatDateTime(thisBill.getUpdate(), DATE_FORMAT_DD_MM_YYYY_HH_MM);
			if (c == ++index) return thisBill.getStatus();
			if (c == ++index) return thisBill.getBalance();
			if (c == ++index) return thisBill.getAdmission() != null ? ADMISSION_ICON : null;
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	class StringTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class StringCenterTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class StringCenterBoldTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			cell.setFont(new Font(null, Font.BOLD, 12));
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class IntegerTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			cell.setFont(new Font(null, Font.BOLD, 12));
			setHorizontalAlignment(CENTER);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class DoubleTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(RIGHT);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class MouseDoubleClickApapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			if (mouseEvent.getClickCount() == 2) {
				JTable target = (JTable) mouseEvent.getSource();
				int row = target.getSelectedRow();
				if (row >= 0) {
					Patient pat = ((Bill) target.getValueAt(row, -1)).getBillPatient();
					try {
						patientSelected(pat);
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
			}
		}
	}
}