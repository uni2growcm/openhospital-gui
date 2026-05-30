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
package org.isf.archive.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;
import static org.isf.utils.Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS;
import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.manager.ArchiveManager;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.ArchivedBill;
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
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.JMonthChooser;
import org.isf.utils.jobjects.JYearChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class ArchiveBrowser extends ModalJFrame {

    protected static final String NO_USERNAME = null;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveBrowser.class);
    private static final ImageIcon ADMISSION_ICON = new ImageIcon("rsc/icons/bed_icon.png");

    private JTabbedPane jTabbedPaneArchives;
    private JTable jTableClosed;
    private JScrollPane jScrollPaneClosed;
    private JTable jTableToday;
    private JTable jTablePeriod;
    private JTable jTableUser;
    private JPanel jPanelRange;
    private JPanel jPanelButtons;
    private JPanel jPanelSouth;
    private JPanel jPanelTotals;
    private JButton jButtonPrintReceipt;
    private JButton jButtonClose;
    private Patient patientParent;
    private JTextField jAffiliatePersonJTextField;
    private JButton jButtonReport;
    private JButton jButtonExcel;
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
    private boolean updatingPageCombo;
    private String rowCounterText = MessageBundle.getMessage("angal.accounting.count.label") + ' ';

    private String[] columnNames = {
            MessageBundle.getMessage("angal.archivebrowser.user.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
            MessageBundle.getMessage("angal.archivebrowser.patientID.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.amount.txt").toUpperCase(),
            MessageBundle.getMessage("angal.archivebrowser.lastpayment.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.status.txt").toUpperCase(),
            MessageBundle.getMessage("angal.archivebrowser.balance.col").toUpperCase(),
            MessageBundle.getMessage("angal.archivebrowser.inout.col").toUpperCase()
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

    private ArchiveManager archiveManager = Context.getApplicationContext().getBean(ArchiveManager.class);
    private BillBrowserManager billBrowserManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
    private String currencyCod;

    private String user = UserBrowsingManager.getCurrentUser();
    private List<String> users;
    private UserBrowsingManager userBrowserManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

    private int closedCurrentPage = 0;
    private long closedTotalRows = 0;
    private int closedTotalPages = 0;
    private static final int PAGE_SIZE = 100;

    private JButton exportSageButton;

//    public boolean hasBillGuarantor() {
//        return GeneralData.ALLOWBILLGUARANTOR;
//    }

    public ArchiveBrowser() {
        try {
            this.currencyCod = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospitalCurrencyCod();
        } catch (OHServiceException ohServiceException) {
            this.currencyCod = null;
            MessageDialog.showExceptions(ohServiceException);
        }

        try {
            users = archiveManager.getArchivedUsers();
        } catch (OHServiceException ohServiceException) {
            MessageDialog.showExceptions(ohServiceException);
        }
        initComponents();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            loadClosedBillsPage();
            updateTotals();
        });
        setVisible(true);
    }

    private void updateTotals() {
        try {
            String guarantorId = getSelectedGuarantorId();
            Integer patientId = patientParent != null ? patientParent.getCode() : null;
            double totalToday = archiveManager.sumArchivedPaymentsByFilters(null, dateToday0, dateToday24, patientId, guarantorId);
            double balanceToday = archiveManager.sumArchivedBalanceByFilters(null, dateToday0, dateToday24, patientId, guarantorId);
            double totalPeriod = archiveManager.sumArchivedPaymentsByFilters(null, dateFrom, dateTo, patientId, guarantorId);
            double balancePeriod = archiveManager.sumArchivedBalanceByFilters(null, dateFrom, dateTo, patientId, guarantorId);
            double userToday = archiveManager.sumArchivedPaymentsByUserAndFilters(user, null, dateToday0, dateToday24, patientId, guarantorId);
            double userPeriod = archiveManager.sumArchivedPaymentsByUserAndFilters(user, null, dateFrom, dateTo, patientId, guarantorId);

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
        loadClosedBillsPage();
    }

    private JLabel getJLabelGuarantor() {
        if (jLabelGuarantor == null) {
            jLabelGuarantor = new JLabel(MessageBundle.getMessage("angal.newbill.selectguarantor.label"));
        }
        return jLabelGuarantor;
    }

    private User getSelectedGuarantor() {
        if (jComboBoxGuarantor != null) {
            return (User) jComboBoxGuarantor.getSelectedItem();
        }
        return null;
    }

    private String getSelectedGuarantorId() {
        User guarantor = getSelectedGuarantor();
        return guarantor != null ? guarantor.getUserName() : null;
    }

    private Integer getSelectedPatientId() {
        return patientParent != null ? patientParent.getCode() : null;
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

    private void loadClosedBillsPage() {
        try {
            String guarantorId = getSelectedGuarantorId();
            Integer patientId = getSelectedPatientId();
            Page<ArchivedBill> billPage = archiveManager.getArchivedBillsWithFilters(
                    "C", dateFrom, dateTo, patientId, guarantorId, closedCurrentPage, PAGE_SIZE);

            List<ArchivedBill> bills = billPage.getContent();
            closedTotalRows = billPage.getTotalElements();
            closedTotalPages = billPage.getTotalPages();

            if (closedCurrentPage >= closedTotalPages && closedCurrentPage > 0) {
                closedCurrentPage = closedTotalPages - 1;
                loadClosedBillsPage();
                return;
            }

            jTableClosed.setModel(new ArchivedBillTableModel(bills));
            updateClosedPaginationControls();
            jTableClosed.updateUI();
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
            if (MainMenu.checkUserGrants("btnbillreceipt") && GeneralData.RECEIPTPRINTER) {
                jPanelButtons.add(getJButtonPrintReceipt());
            }
            if (MainMenu.checkUserGrants("btnbillreport")) {
                jPanelButtons.add(getJButtonReport());
            }
            jPanelButtons.add(getJButtonExcel());
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
                SelectPatient selectPatient = new SelectPatient(ArchiveBrowser.this, false, true);
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
                refreshAfterChange();
            }
        });

        return priceListLabelPanel;
    }

    private JTabbedPane getJTabbedPaneArchives() {
        if (jTabbedPaneArchives == null) {
            jTabbedPaneArchives = new JTabbedPane();

            JPanel closedPanel = new JPanel(new BorderLayout());
            closedPanel.add(getJScrollPaneClosed(), BorderLayout.CENTER);
            closedPanel.add(getClosedPaginationPanel(), BorderLayout.SOUTH);
            jTabbedPaneArchives.addTab(MessageBundle.getMessage("angal.billbrowser.closed.title"), closedPanel);
        }
        return jTabbedPaneArchives;
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
            jTableClosed.setModel(new ArchivedBillTableModel("C", NO_USERNAME));
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

    private JButton getJButtonPrintReceipt() {
        if (jButtonPrintReceipt == null) {
            jButtonPrintReceipt = new JButton(MessageBundle.getMessage("angal.billbrowser.receipt.btn"));
            jButtonPrintReceipt.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.receipt.btn.key"));
            jButtonPrintReceipt.addActionListener(actionEvent -> {
                try {
                    if (jScrollPaneClosed.isShowing()) {
                        int rowsSelected = jTableClosed.getSelectedRowCount();
                        if (rowsSelected == 1) {
                            int rowSelected = jTableClosed.getSelectedRow();
                            ArchivedBill editBill = (ArchivedBill) jTableClosed.getValueAt(rowSelected, -1);
                            new GenericReportBill(editBill.getId(), GeneralData.PATIENTARCHIVEDBILL);
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
                    new GenericReportPatient(patientParent.getCode(), GeneralData.PATIENTARCHIVEBILLSTATEMENT);
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
                    ArchivedBill bill = null;
                    int selectedRow = jTableClosed.getSelectedRow();
                    if (selectedRow >= 0) {
                        bill = (ArchivedBill) jTableClosed.getValueAt(selectedRow, -1);
                    }
                    if (bill != null) {
                        Integer patId = bill.getBillPatientId();
                        if (patId != null && patId > 0) {
                            patient = new Patient();
                            patient.setCode(patId);
                        }
                    }
                    if (patient == null) {
                        MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
                        return;
                    }
                    new GenericReportPatient(patient.getCode(), GeneralData.PATIENTARCHIVEBILLSTATEMENT);
                    return;
                }

                options = new ArrayList<>();
                options.add(MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebt.txt"));
                options.add(MessageBundle.getMessage("angal.billbrowser.fullreportallbills.txt"));

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
            });
        }
        return jButtonReport;
    }

    private JButton getJButtonExcel() {
        if (jButtonExcel == null) {
            jButtonExcel = new JButton(MessageBundle.getMessage("angal.common.excel.btn"));
            jButtonExcel.setMnemonic(MessageBundle.getMnemonic("angal.common.excel.btn.key"));
            jButtonExcel.addActionListener(actionEvent -> {
                String fileName = "ArchivedBills";
                File defaultFileName = new File(fileName);
                JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFileName);
                int iRetVal = fcExcel.showSaveDialog(this);
                if (iRetVal == JFileChooser.APPROVE_OPTION) {
                    File exportFile = fcExcel.getSelectedFile();
                    if (!exportFile.getName().endsWith(".xls") && !exportFile.getName().endsWith(".xlsx")) {
                        if (fcExcel.getFileFilter().getDescription().contains("*.xlsx")) {
                            exportFile = new File(exportFile.getAbsoluteFile() + ".xlsx");
                        } else {
                            exportFile = new File(exportFile.getAbsoluteFile() + ".xls");
                        }
                    }
                    ExcelExporter xlsExport = new ExcelExporter();
                    try {
                        JTable currentTable = getCurrentExcelTable();
                        if (exportFile.getName().endsWith(".xlsx")) {
                            xlsExport.exportTableToExcel(currentTable, exportFile);
                        } else {
                            xlsExport.exportTableToExcelOLD(currentTable, exportFile);
                        }
                    } catch (IOException exc) {
                        JOptionPane.showMessageDialog(ArchiveBrowser.this, exc.getMessage(),
                                MessageBundle.getMessage("angal.messagedialog.error.title"), JOptionPane.PLAIN_MESSAGE);
                        LOGGER.error("Export to excel error : {}", exc.getMessage());
                    }
                }
            });
        }
        return jButtonExcel;
    }

    private JTable getCurrentExcelTable() {
        return jTableClosed;
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
                    closedCurrentPage = 0;
                    jButtonToday.setEnabled(true);
                    loadClosedBillsPage();
                    updateTotals();
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
                                MessageDialog.info(ArchiveBrowser.this, "angal.medicalstock.exportsage.succes");
                            } catch (Exception ex) {
                                MessageDialog.error(ArchiveBrowser.this, "angal.medicalstock.exportsage.error");
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
                    closedCurrentPage = 0;
                    jButtonToday.setEnabled(true);
                    loadClosedBillsPage();
                    updateTotals();
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
                updateTotals();
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
                closedCurrentPage = 0;
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
                updateTables();
                updateTotals();
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

    public boolean hasBillGuarantor() {
        return GeneralData.ALLOWBILLGUARANTOR;
    }

    public void patientSelected(Patient patient) throws OHServiceException {
        patientParent = patient;
        jAffiliatePersonJTextField.setText(patientParent != null ? patientParent.getName() : "");

        if (patientParent != null) {
            updateTables();
            updateTotals();
        }
    }

    private void refreshAfterChange() {
        updateTables();
        updateTotals();
    }

    private void initComponents() {
        add(getJPanelRange(), BorderLayout.NORTH);
        add(getJTabbedPaneArchives(), BorderLayout.CENTER);
        add(getJPanelSouth(), BorderLayout.SOUTH);
        setTitle(MessageBundle.getMessage("angal.billbrowser.patientarchivebillmanagment.title"));
        setMinimumSize(new Dimension(1150, 600));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        pack();
    }

    public class ArchivedBillTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;
        private List<ArchivedBill> tableArray = new ArrayList<>();

        public ArchivedBillTableModel(List<ArchivedBill> bills) {
            this.tableArray = bills != null ? bills : new ArrayList<>();
        }

        public ArchivedBillTableModel(String status, String username) {
            this.tableArray = new ArrayList<>();
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
            ArchivedBill thisBill = tableArray.get(r);
            if (c == index) return thisBill;
            if (c == ++index) return thisBill.getUser();
            if (c == ++index) return thisBill.getId();
            if (c == ++index) return TimeTools.formatDateTime(thisBill.getDate(), DATE_FORMAT_DD_MM_YYYY_HH_MM);
            if (c == ++index) {
                Integer patID = thisBill.getBillPatientId();
                return patID == null || patID == 0 ? "" : String.valueOf(patID);
            }
            if (c == ++index) return thisBill.getPatName();
            if (c == ++index) return thisBill.getAmount();
            if (c == ++index) return TimeTools.formatDateTime(thisBill.getUpdate(), DATE_FORMAT_DD_MM_YYYY_HH_MM);
            if (c == ++index) return thisBill.getStatus();
            if (c == ++index) return thisBill.getBalance();
            if (c == ++index) return thisBill.getAdmissionId() != null ? ADMISSION_ICON : null;
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
                    ArchivedBill bill = (ArchivedBill) target.getValueAt(row, -1);
                    Integer patId = bill.getBillPatientId();
                    if (patId != null && patId > 0) {
                        Patient pat = new Patient();
                        pat.setCode(patId);
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
}
