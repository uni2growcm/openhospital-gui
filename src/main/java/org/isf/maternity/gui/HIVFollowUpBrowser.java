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
package org.isf.maternity.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hiv.manager.HIVInfantManager;
import org.isf.hiv.manager.HIVVisitManager;
import org.isf.hiv.model.HIVInfant;
import org.isf.hiv.model.HIVInfant.FeedingType;
import org.isf.hiv.model.HIVInfant.HIVInfantStatus;
import org.isf.hiv.model.HIVVisit;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.therapy.gui.TherapyEdit;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.isf.utils.ExcelExporter;

public class HIVFollowUpBrowser extends JFrame implements SelectionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int HIV_PAGINATION_PAGESIZE = 100;
    private static final int MIN_AGE_MONTHS = 0;
    private static final int MAX_AGE_MONTHS = GeneralData.HIV_INFANT_MAX_AGE_MONTHS;
    private static final Logger logger = LoggerFactory.getLogger(HIVFollowUpBrowser.class);
    private static final boolean HIV_EXPORT_FILE_PREFIX = true;


    private final String[] columnHeaders = {
            MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
            MessageBundle.getMessage("angal.hiv.table.status").toUpperCase(),
            MessageBundle.getMessage("angal.hiv.table.last.visit").toUpperCase(),
            MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
            MessageBundle.getMessage("angal.hiv.filter.feeding").toUpperCase()
    };
    private final int[] columnWidths = { 80, 180, 60, 120, 120, 120, 120 };

    private final String[] visitColumnHeaders = {
            MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.visitdate.col").toUpperCase(),
            MessageBundle.getMessage("angal.hiv.label.next.appointment").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.visitnote.col").toUpperCase()
    };

    private final int[] visitColumnWidths = { 80, 140, 140, 350 };

    private List<HIVInfant> infantList = new ArrayList<>();
    private List<HIVVisit> visitList = new ArrayList<>();

    private HIVInfantManager infantManager;
    private HIVVisitManager visitManager;

    private JTable infantTable;
    private JTable visitTable;
    private InfantsTableModel model;

    private JButton nextButton;
    private JButton prevButton;
    private JComboBox<Integer> pagesCombo;
    private JLabel underLabel;
    private JLabel totalInfantsLabel;
    private int TOTAL_PAGES = 0;
    private int CURRENT_PAGE = 1;
    private long TOTAL_INFANTS = 0;

    private JTextField patientCodeFilter;
    private GoodDateChooser regDateFrom;
    private GoodDateChooser regDateTo;
    private GoodDateChooser followupDateFrom;
    private GoodDateChooser followupDateTo;
    private VoLimitedTextField ageFromField;
    private VoLimitedTextField ageToField;
    private JComboBox<HIVInfantStatus> statusCombo;
    private JComboBox<FeedingType> feedingCombo;
    private JButton searchButton;
    private JButton resetButton;

    private JTextField visitCodeFilter;
    private GoodDateChooser visitDateFrom;
    private GoodDateChooser visitDateTo;
    private JButton visitSearchButton;
    private JButton visitResetButton;
    private HIVInfant selectedInfant;
    private int selectedVisitRow = -1;

    private AdmissionBrowserManager admissionManager;

    public HIVFollowUpBrowser() {
        setTitle(MessageBundle.getMessage("angal.menu.hivfollowup"));
        initManagers();
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        addCloseListener();
        performSearch();
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText(MessageBundle.getMessage("angal.common.details.btn"));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(MessageBundle.getMessage("angal.common.details.btn"));
            return this;
        }
    }

    private void addCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void initManagers() {
        infantManager = Context.getApplicationContext().getBean(HIVInfantManager.class);
        visitManager = Context.getApplicationContext().getBean(HIVVisitManager.class);
        admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        initializeStatusCombo();
        initializeFeedingCombo();

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setTopComponent(getTopPanel());
        mainSplit.setBottomComponent(getMiddlePanel());
        mainSplit.setResizeWeight(0.6);

        add(mainSplit, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void initializeStatusCombo() {
        statusCombo = new JComboBox<>();
        statusCombo.addItem(null);
        for (HIVInfantStatus status : HIVInfantStatus.values()) {
            statusCombo.addItem(status);
        }
    }

    private void initializeFeedingCombo() {
        feedingCombo = new JComboBox<>();
        feedingCombo.addItem(null);
        for (FeedingType type : FeedingType.values()) {
            feedingCombo.addItem(type);
        }
    }

    private JPanel getTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getFilterPanel(), BorderLayout.WEST);
        topPanel.add(getInfantListPanel(), BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel getMiddlePanel() {
        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(getVisitFilterPanel(), BorderLayout.WEST);
        middlePanel.add(getVisitListPanel(), BorderLayout.CENTER);
        return middlePanel;
    }

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.hiv.filters.infants")));
        filterPanel.setMinimumSize(new Dimension(320, 500));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel regDatePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        regDatePanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.hiv.filter.registration.date")));
        regDateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        regDateTo = new GoodDateChooser(LocalDate.now());
        regDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        regDatePanel.add(regDateFrom);
        regDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        regDatePanel.add(regDateTo);
        filterPanel.add(regDatePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel followupDatePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        followupDatePanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.hiv.filter.followup.date")));
        followupDateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        followupDateTo = new GoodDateChooser(LocalDate.now());
        followupDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        followupDatePanel.add(followupDateFrom);
        followupDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        followupDatePanel.add(followupDateTo);
        filterPanel.add(followupDatePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel agePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        agePanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.common.ageinterval.label")));
        ageFromField = new VoLimitedTextField(3, 3);
        ageFromField.setText(String.valueOf(MIN_AGE_MONTHS));
        ageToField = new VoLimitedTextField(3, 3);
        ageToField.setText(String.valueOf(MAX_AGE_MONTHS));
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.agefrom.label") + " (mois)"));
        agePanel.add(ageFromField);
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.ageto.label") + " (mois)"));
        agePanel.add(ageToField);
        filterPanel.add(agePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder( MessageBundle.getMessage("angal.hiv.filter.status")));
        statusPanel.add(statusCombo);
        filterPanel.add(statusPanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel feedingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        feedingPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.hiv.filter.feeding")));
        feedingPanel.add(feedingCombo);
        filterPanel.add(feedingPanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
        searchButton.addActionListener(e -> performSearch());
        resetButton = new JButton(MessageBundle.getMessage("angal.common.reset.btn"));
        resetButton.addActionListener(e -> resetFilters());
        buttonPanel.add(searchButton);
        buttonPanel.add(resetButton);
        filterPanel.add(buttonPanel);

        return filterPanel;
    }

    private JPanel getInfantListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getInfantTableScrollPane(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getInfantTableScrollPane() {
        model = new InfantsTableModel();
        infantTable = new JTable(model);
        infantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        infantTable.setAutoCreateRowSorter(true);
        infantTable.setRowHeight(20);
        infantTable.setFillsViewportHeight(true);
        infantTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        for (int i = 0; i < columnHeaders.length; i++) {
            infantTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        infantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = infantTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = infantTable.convertRowIndexToModel(viewRow);
                    if (modelRow >= 0 && modelRow < infantList.size()) {
                        selectedInfant = infantList.get(modelRow);
                        selectedVisitRow = -1;
                        filterVisits();
                    }
                }
            }
        });

        return new JScrollPane(infantTable);
    }

    private JPanel getVisitFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.hiv.filters.visits")));
        filterPanel.setPreferredSize(new Dimension(250, 220));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        visitCodeFilter = new JTextField(10);
        codePanel.add(visitCodeFilter);
        filterPanel.add(codePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel datePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        datePanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.common.date.txt")));

        visitDateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
        visitDateTo = new GoodDateChooser(LocalDate.now());

        datePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        datePanel.add(visitDateFrom);
        datePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        datePanel.add(visitDateTo);

        filterPanel.add(datePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        visitSearchButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
        visitSearchButton.addActionListener(e -> filterVisits());

        visitResetButton = new JButton(MessageBundle.getMessage("angal.common.reset.btn"));
        visitResetButton.addActionListener(e -> resetVisitFilters());

        buttonPanel.add(visitSearchButton);
        buttonPanel.add(visitResetButton);

        filterPanel.add(buttonPanel);

        return filterPanel;
    }

    private void resetVisitFilters() {
        visitCodeFilter.setText("");
        visitDateFrom.setDate(LocalDate.now().minusWeeks(1));
        visitDateTo.setDate(LocalDate.now());
        filterVisits();
    }

    private JScrollPane getVisitListPanel() {
        visitTable = new JTable(new VisitsTableModel());
        visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        visitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedVisitRow = visitTable.getSelectedRow();
            }
        });

        for (int i = 0; i < visitColumnHeaders.length; i++) {
            visitTable.getColumnModel().getColumn(i).setPreferredWidth(visitColumnWidths[i]);
        }

        JScrollPane visitScrollPane = new JScrollPane(visitTable);
        visitScrollPane.setPreferredSize(new Dimension(600, 300));
        return visitScrollPane;
    }

    private JPanel getPaginationPanel() {
        JPanel paginatePanel = new JPanel();
        paginatePanel.add(getPrevButton());
        paginatePanel.add(getPagesCombo());
        paginatePanel.add(getUnderLabel());
        paginatePanel.add(getNextButton());
        paginatePanel.add(getTotalInfantsLabel());
        return paginatePanel;
    }

    private JButton getNextButton() {
        if (nextButton == null) {
            nextButton = new JButton(">");
            nextButton.setEnabled(false);
            nextButton.addActionListener(e -> {
                if (CURRENT_PAGE < TOTAL_PAGES) {
                    CURRENT_PAGE++;
                    performSearch();
                }
            });
        }
        return nextButton;
    }

    private JButton getPrevButton() {
        if (prevButton == null) {
            prevButton = new JButton("<");
            prevButton.setEnabled(false);
            prevButton.addActionListener(e -> {
                if (CURRENT_PAGE > 1) {
                    CURRENT_PAGE--;
                    performSearch();
                }
            });
        }
        return prevButton;
    }

    private JComboBox<Integer> getPagesCombo() {
        if (pagesCombo == null) {
            pagesCombo = new JComboBox<>();
            pagesCombo.setPreferredSize(new Dimension(100, 25));
            pagesCombo.addActionListener(e -> {
                if (pagesCombo.getSelectedItem() != null) {
                    int selected = (Integer) pagesCombo.getSelectedItem();
                    if (selected != CURRENT_PAGE) {
                        CURRENT_PAGE = selected;
                        performSearch();
                    }
                }
            });
        }
        return pagesCombo;
    }

    private JLabel getUnderLabel() {
        if (underLabel == null) {
            underLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.common.page.label"));
            underLabel.setPreferredSize(new Dimension(80, 30));
        }
        return underLabel;
    }

    private JLabel getTotalInfantsLabel() {
        if (totalInfantsLabel == null) {
            totalInfantsLabel = new JLabel(
                    MessageBundle.getMessage("angal.maternity.total.label") + ": 0");
        }
        return totalInfantsLabel;
    }

    private void updatePaginationControls() {
        prevButton.setEnabled(CURRENT_PAGE > 1);
        nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES);

        java.awt.event.ActionListener[] listeners = pagesCombo.getActionListeners();
        for (java.awt.event.ActionListener l : listeners) {
            pagesCombo.removeActionListener(l);
        }
        pagesCombo.removeAllItems();
        for (int i = 1; i <= TOTAL_PAGES; i++) {
            pagesCombo.addItem(i);
        }
        if (TOTAL_PAGES > 0) {
            pagesCombo.setSelectedItem(CURRENT_PAGE);
        }
        for (java.awt.event.ActionListener l : listeners) {
            pagesCombo.addActionListener(l);
        }

        underLabel.setText("/ " + TOTAL_PAGES + " "
                + MessageBundle.getMessage("angal.common.page.label"));
        totalInfantsLabel.setText(
                MessageBundle.getMessage("angal.maternity.total.label") + ": " + TOTAL_INFANTS);
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.common.actions.label")));

        if (MainMenu.checkUserGrants("hiv.new")) {
            buttonPanel.add(getJNewInfantButton());
        }
        if (MainMenu.checkUserGrants("hiv.update")) {
            buttonPanel.add(getJUpdateInfantButton());
        }
        if (MainMenu.checkUserGrants("hiv.delete")) {
            buttonPanel.add(getJDeleteInfantButton());
        }
        if (MainMenu.checkUserGrants("hiv.newvisit")) {
            buttonPanel.add(getJNewVisitButton());
        }
        if (MainMenu.checkUserGrants("hiv.updatevisit")) {
            buttonPanel.add(getJUpdateVisitButton());
        }
        if (MainMenu.checkUserGrants("hiv.deletevisit")) {
            buttonPanel.add(getJDeleteVisitButton());
        }
        if (MainMenu.checkUserGrants("hiv.therapy")) {
            buttonPanel.add(getJTherapyButton());
        }
        if (MainMenu.checkUserGrants("hiv.report")) {
            buttonPanel.add(getJReportButton());
        }
        buttonPanel.add(getJExportButton());
        buttonPanel.add(getJCloseButton());
        return buttonPanel;
    }

    /**
     * Creates the export button for exporting HIV follow-up data to Excel.
     *
     * @return the export button
     */
    private JButton getJExportButton() {
        JButton exportButton = new JButton(MessageBundle.getMessage("angal.hiv.export.btn"));
        exportButton.setMnemonic(KeyEvent.VK_E);
        exportButton.setIcon(new ImageIcon("rsc/icons/excel_button.png"));
        exportButton.addActionListener(e -> exportToExcel());
        exportButton.setPreferredSize(new Dimension(120, 25));
        return exportButton;
    }

    /**
     * Exports the current HIV follow-up data to an Excel file.
     * Uses the same logic as the BillBrowser export.
     */
    private void exportToExcel() {
        if (infantList == null || infantList.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.export.nodata.msg"));
            return;
        }

        JFileChooser fcExcel = new JFileChooser();
        FileNameExtensionFilter excelFilter = new FileNameExtensionFilter(
                MessageBundle.getMessage("angal.hiv.export.excelfilter"), "xls");
        fcExcel.addChoosableFileFilter(excelFilter);
        fcExcel.setFileFilter(excelFilter);
        fcExcel.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String defaultFileName = HIV_EXPORT_FILE_PREFIX +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xls";
        fcExcel.setSelectedFile(new File(defaultFileName));

        int iRetVal = fcExcel.showSaveDialog(HIVFollowUpBrowser.this);
        if (iRetVal == JFileChooser.APPROVE_OPTION) {
            File exportFile = fcExcel.getSelectedFile();
            if (!exportFile.getName().endsWith("xls")) {
                exportFile = new File(exportFile.getAbsolutePath() + ".xls");
            }

            try {
                ExcelExporter xlsExport = new ExcelExporter();
                xlsExport.exportHIVInfantsToExcel(infantList, exportFile);

                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.hiv.export.success.msg") + " " + exportFile.getAbsolutePath());

                Desktop.getDesktop().open(exportFile);
            } catch (IOException exc) {
                logger.error("Export to excel error : " + exc.getMessage());
                MessageDialog.error(this,
                        MessageBundle.getMessage("angal.hiv.export.error.msg") + ": " + exc.getMessage());
            }
        }
    }

    private JButton getJNewInfantButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.new"));
        button.addActionListener(e -> newInfant());
        return button;
    }

    private JButton getJUpdateInfantButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.edit"));
        button.addActionListener(e -> updateInfant());
        return button;
    }

    private JButton getJDeleteInfantButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.delete"));
        button.addActionListener(e -> deleteInfant());
        return button;
    }


    private JButton getJNewVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.newvisit"));
        button.addActionListener(e -> newVisit());
        return button;
    }

    private JButton getJUpdateVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.editvisit"));
        button.addActionListener(e -> updateVisit());
        return button;
    }

    private JButton getJDeleteVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.deletevisit"));
        button.addActionListener(e -> deleteVisit());
        return button;
    }

    private JButton getJTherapyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.hiv.button.therapy"));
        button.addActionListener(e -> therapy());
        return button;
    }

    private JButton getJReportButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.common.report.btn"));
        button.addActionListener(e -> report());
        return button;
    }

    private JButton getJCloseButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
        button.addActionListener(e -> dispose());
        return button;
    }

    private void performSearch() {
        try {
            Integer patientCode = null;
            String codeText = patientCodeFilter.getText().trim();
            if (!codeText.isEmpty()) {
                try {
                    patientCode = Integer.parseInt(codeText);
                } catch (NumberFormatException ignored) {}
            }

            HIVInfantStatus status = (HIVInfantStatus) statusCombo.getSelectedItem();
            FeedingType feeding = (FeedingType) feedingCombo.getSelectedItem();
            LocalDate regFrom = regDateFrom.getDate();
            LocalDate regTo = regDateTo.getDate();
            LocalDate followFrom = followupDateFrom.getDate();
            LocalDate followTo = followupDateTo.getDate();

            org.springframework.data.domain.Pageable pageable = PageRequest.of(
                    CURRENT_PAGE - 1,
                    HIV_PAGINATION_PAGESIZE,
                    Sort.by("id").descending());

            Page<HIVInfant> page = infantManager.getInfantsByFilters(
                    patientCode, status, feeding,
                    regFrom, regTo, followFrom, followTo,
                    pageable);

            infantList = new ArrayList<>(page.getContent());
            TOTAL_INFANTS = page.getTotalElements();
            TOTAL_PAGES = page.getTotalPages();

            if (TOTAL_PAGES > 0 && CURRENT_PAGE > TOTAL_PAGES) {
                CURRENT_PAGE = TOTAL_PAGES;
                performSearch();
                return;
            }

            model.fireTableDataChanged();
            updatePaginationControls();

            visitList.clear();
            ((DefaultTableModel) visitTable.getModel()).fireTableDataChanged();
            selectedInfant = null;

        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
        }
    }

    private void resetFilters() {
        patientCodeFilter.setText("");
        regDateFrom.setDate(LocalDate.now().minusMonths(6));
        regDateTo.setDate(LocalDate.now());
        followupDateFrom.setDate(LocalDate.now().minusMonths(6));
        followupDateTo.setDate(LocalDate.now());
        ageFromField.setText(String.valueOf(MIN_AGE_MONTHS));
        ageToField.setText(String.valueOf(MAX_AGE_MONTHS));
        statusCombo.setSelectedIndex(0);
        feedingCombo.setSelectedIndex(0);
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void filterVisits() {
        if (selectedInfant == null) {
            visitList.clear();
            ((DefaultTableModel) visitTable.getModel()).fireTableDataChanged();
            return;
        }

        try {
            List<HIVVisit> allVisits = visitManager.getVisitsByInfantId(selectedInfant.getId());

            String visitCodeText = visitCodeFilter.getText().trim();
            LocalDate dateFrom = visitDateFrom.getDate();
            LocalDate dateTo = visitDateTo.getDate();

            visitList = new ArrayList<>();

            for (HIVVisit visit : allVisits) {

                if (!visitCodeText.isEmpty()) {
                    String visitCode = String.valueOf(visit.getId());

                    if (!visitCode.contains(visitCodeText)) {
                        continue;
                    }
                }

                if (visit.getVisitDate() != null) {
                    LocalDate visitDate = visit.getVisitDate().toLocalDate();

                    if (dateFrom != null && visitDate.isBefore(dateFrom)) {
                        continue;
                    }

                    if (dateTo != null && visitDate.isAfter(dateTo)) {
                        continue;
                    }
                } else {
                    if (dateFrom != null || dateTo != null) {
                        continue;
                    }
                }

                visitList.add(visit);
            }

            ((DefaultTableModel) visitTable.getModel()).fireTableDataChanged();

        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            visitList.clear();
            ((DefaultTableModel) visitTable.getModel()).fireTableDataChanged();
        }
    }

    private void viewInfantDetails(HIVInfant infant) {
        if (infant == null) return;
    }

    private void newInfant() {
        HIVInfantEdit edit = new HIVInfantEdit(this, (Patient) null, true);
        edit.addHIVInfantListener(new HIVInfantEdit.HIVInfantListener() {
            @Override
            public void infantInserted(AWTEvent e, HIVInfant infant) {
                performSearch();
            }

            @Override
            public void infantUpdated(AWTEvent e, HIVInfant infant) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void updateInfant() {
        int viewRow = infantTable.getSelectedRow();
        if (viewRow < 0) {
            MessageDialog.error(this,
                    MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }
        int modelRow = infantTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= infantList.size()) {
            MessageDialog.error(this,
                    MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }

        HIVInfant infant = infantList.get(modelRow);
        HIVInfantEdit edit = new HIVInfantEdit(this, infant, false);
        edit.addHIVInfantListener(new HIVInfantEdit.HIVInfantListener() {
            @Override
            public void infantInserted(AWTEvent e, HIVInfant i) {
                performSearch();
            }

            @Override
            public void infantUpdated(AWTEvent e, HIVInfant i) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void deleteInfant() {
        int viewRow = infantTable.getSelectedRow();
        if (viewRow < 0) {
            MessageDialog.error(this,
                    MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }
        int modelRow = infantTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= infantList.size()) {
            MessageDialog.error(this,
                    MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }

        HIVInfant infant = infantList.get(modelRow);
        int answer = MessageDialog.yesNo(this,
                MessageBundle.getMessage("angal.hiv.message.delete.confirm"));
        if (answer == JOptionPane.YES_OPTION) {
            try {
                infantManager.deleteInfant(infant);
                performSearch();
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.hiv.message.delete.success"));
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void newVisit() {
        if (selectedInfant == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.select.infant"));
            return;
        }

        HIVVisitEdit edit = new HIVVisitEdit(this, selectedInfant, true);
        edit.addHIVVisitListener(new HIVVisitEdit.HIVVisitListener() {
            @Override
            public void visitInserted(AWTEvent e, HIVVisit visit) {
                filterVisits();
                performSearch();
            }

            @Override
            public void visitUpdated(AWTEvent e, HIVVisit visit) {
                filterVisits();
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void updateVisit() {
        if (selectedInfant == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.select.infant"));
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }

        HIVVisit selectedVisit = visitList.get(selectedVisitRow);

        HIVVisitEdit edit = new HIVVisitEdit(this, selectedVisit, false);
        edit.addHIVVisitListener(new HIVVisitEdit.HIVVisitListener() {
            @Override
            public void visitInserted(AWTEvent e, HIVVisit visit) {
                filterVisits();
                performSearch();
            }

            @Override
            public void visitUpdated(AWTEvent e, HIVVisit visit) {
                filterVisits();
                performSearch();
            }
        });
        edit.setVisible(true);
    }
    private void deleteVisit() {
        if (selectedInfant == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.select.infant"));
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }

        HIVVisit selectedVisit = visitList.get(selectedVisitRow);

        int answer = MessageDialog.yesNo(this, MessageBundle.getMessage("angal.hiv.message.visit.delete.confirm"));
        if (answer == JOptionPane.YES_OPTION) {
            try {
                visitManager.deleteVisit(selectedVisit);
                filterVisits();
                performSearch();
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.hiv.message.visit.delete.success"));
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void therapy() {
        if (selectedInfant == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.select.infant"));
            return;
        }

        Patient patient = selectedInfant.getPatient();
        if (patient == null) {
            MessageDialog.error(this, "angal.hiv.message.no.patient.associated");
            return;
        }

        Admission currentAdmission = admissionManager.getCurrentAdmission(patient);
        boolean isAdmitted = (currentAdmission != null);

        TherapyEdit therapyEdit = new TherapyEdit(this, patient, isAdmitted);
        therapyEdit.setVisible(true);
    }

    private void report() {
        // To be implemented
    }

    @Override
    public void patientSelected(Patient patient) {
        // To be implemented
    }

    class InfantsTableModel extends DefaultTableModel {

        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Override
        public int getRowCount() {
            return infantList != null ? infantList.size() : 0;
        }

        @Override
        public String getColumnName(int c) {
            return columnHeaders[c];
        }

        @Override
        public int getColumnCount() {
            return columnHeaders.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (infantList == null || r >= infantList.size()) return null;

            HIVInfant infant = infantList.get(r);
            Patient patient = infant.getPatient();

            if (c == 0) return patient != null ? patient.getCode() : "";
            else if (c == 1)
                return patient != null
                        ? patient.getFirstName() + " " + patient.getSecondName() : "";
            else if (c == 2)
                return patient != null ? patient.getAge() + " mois" : "";
            else if (c == 3)
                return infant.getStatus() != null
                        ? infant.getStatus().getDescription() : "";
            else if (c == 4)
                return "";
            else if (c == 5)
                return infant.getRegistrationDate() != null
                        ? infant.getRegistrationDate().format(formatter) : "";
            else if (c == 6)
                return infant.getFeedingType() != null
                        ? infant.getFeedingType().getDescription() : "";

            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    class VisitsTableModel extends DefaultTableModel {

        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Override
        public String getColumnName(int c) {
            return visitColumnHeaders[c];
        }

        @Override
        public int getColumnCount() {
            return visitColumnHeaders.length;
        }

        @Override
        public int getRowCount() {
            return visitList != null ? visitList.size() : 0;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (visitList == null || r >= visitList.size()) return null;

            HIVVisit visit = visitList.get(r);

            if (c == 0) {
                return visit.getId();
            } else if (c == 1) {
                return visit.getVisitDate() != null
                        ? visit.getVisitDate().format(formatter)
                        : "";
            }else if (c == 2) {
                return visit.getNextAppointmentDate() != null
                        ? visit.getNextAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
            } else if (c == 3) {
                String notes = visit.getNotes();
                if (notes != null && notes.length() > 50) {
                    notes = notes.substring(0, 50) + "...";
                }
                return notes != null ? notes : "";
            }

            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }
}