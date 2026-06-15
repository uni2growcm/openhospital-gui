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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hiv.manager.HIVInfantManager;
import org.isf.hiv.manager.HIVVisitManager;
import org.isf.hiv.model.HIVInfant;
import org.isf.hiv.model.HIVInfant.HIVInfantStatus;
import org.isf.hiv.model.HIVInfant.FeedingType;
import org.isf.hiv.model.HIVVisit;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

import org.springframework.data.domain.Page;

public class HIVFollowUpBrowser extends JFrame implements SelectionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MIN_AGE_MONTHS = 0;
    private static final int MAX_AGE_MONTHS = 60;

    private final String[] columnHeaders = {
            "ID",
            MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
            MessageBundle.getMessage("angal.hiv.table.status").toUpperCase(),
            MessageBundle.getMessage("angal.hiv.table.last.visit").toUpperCase(),
            ""
    };

    private final int[] columnWidths = { 50, 80, 180, 60, 120, 120, 60 };

    private final String[] visitColumnHeaders = {
            MessageBundle.getMessage("angal.maternity.visitdate.col").toUpperCase(),
            "PCR",
            MessageBundle.getMessage("angal.maternity.visitnote.col").toUpperCase()
    };

    private final int[] visitColumnWidths = { 120, 100, 400 };

    private List<HIVInfant> infantList = new ArrayList<>();
    private List<HIVVisit> visitList = new ArrayList<>();

    private HIVInfantManager infantManager;
    private HIVVisitManager visitManager;

    private JTable infantTable;
    private JTable visitTable;
    private JButton nextButton;
    private JButton prevButton;
    private javax.swing.JComboBox<Integer> pagesCombo;
    private JLabel underLabel;
    private JLabel totalInfantsLabel;
    private InfantsTableModel model;
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
    private javax.swing.JComboBox<HIVInfantStatus> statusCombo;
    private javax.swing.JComboBox<FeedingType> feedingCombo;
    private JButton searchButton;
    private JButton resetButton;

    private javax.swing.JComboBox<String> arvCombo;
    private javax.swing.JComboBox<String> prophylaxisCombo;

    private JRadioButton allVisitsRadio;
    private JRadioButton pcrVisitsRadio;
    private JRadioButton clinicalVisitsRadio;
    private javax.swing.JComboBox<HIVVisit.PCRResult> pcrResultCombo;


    private HIVInfant selectedInfant;
    private int selectedVisitRow = -1;

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
        statusCombo = new javax.swing.JComboBox<>();
        statusCombo.addItem(null);
        for (HIVInfantStatus status : HIVInfantStatus.values()) {
            statusCombo.addItem(status);
        }
    }

    private void initializeFeedingCombo() {
        feedingCombo = new javax.swing.JComboBox<>();
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

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filters.infants")));
        filterPanel.setMinimumSize(new Dimension(320, 500));

        // Code patient
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        // Intervalle date enregistrement
        JPanel regDatePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        regDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filter.registration.date")));
        regDateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        regDateTo = new GoodDateChooser(LocalDate.now());
        regDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        regDatePanel.add(regDateFrom);
        regDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        regDatePanel.add(regDateTo);
        filterPanel.add(regDatePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        // Intervalle date suivi
        JPanel followupDatePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        followupDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filter.followup.date")));
        followupDateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        followupDateTo = new GoodDateChooser(LocalDate.now());
        followupDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        followupDatePanel.add(followupDateFrom);
        followupDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        followupDatePanel.add(followupDateTo);
        filterPanel.add(followupDatePanel);

        filterPanel.add(Box.createVerticalStrut(5));

        // Tranche âge (mois)
        JPanel agePanel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        agePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.ageinterval.label")));
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

        // Statut
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filter.status")));
        statusPanel.add(statusCombo);
        filterPanel.add(statusPanel);

        filterPanel.add(Box.createVerticalStrut(5));

        // Alimentation
        JPanel feedingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        feedingPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filter.feeding")));
        feedingPanel.add(feedingCombo);
        filterPanel.add(feedingPanel);

        filterPanel.add(Box.createVerticalStrut(5));

        // Traitement ARV (à remplir avec les typologies)
        JPanel arvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arvPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filter.arv")));
        arvCombo = new javax.swing.JComboBox<>();
        arvCombo.addItem(MessageBundle.getMessage("angal.hiv.filter.arv.all"));
        // TODO: Charger les typologies ARV depuis OH_TYPOLOGIES
        arvPanel.add(arvCombo);
        filterPanel.add(arvPanel);

        filterPanel.add(Box.createVerticalStrut(5));

        // Prophylaxie (à remplir avec les typologies)
        JPanel prophylaxisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prophylaxisPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.filter.prophylaxis")));
        prophylaxisCombo = new javax.swing.JComboBox<>();
        prophylaxisCombo.addItem(MessageBundle.getMessage("angal.hiv.filter.prophylaxis.all"));
        // TODO: Charger les typologies Prophylaxie depuis OH_TYPOLOGIES
        prophylaxisPanel.add(prophylaxisCombo);
        filterPanel.add(prophylaxisPanel);

        filterPanel.add(Box.createVerticalStrut(10));

        // Boutons Rechercher / Réinitialiser
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
        panel.add(getInfantTablePanel(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getInfantTablePanel() {
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

        JScrollPane scrollPane = new JScrollPane(infantTable);
        return scrollPane;
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
            nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES != 1);
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
            prevButton.setEnabled(CURRENT_PAGE > 1);
            prevButton.addActionListener(e -> {
                if (CURRENT_PAGE > 1) {
                    CURRENT_PAGE--;
                    performSearch();
                }
            });
        }
        return prevButton;
    }

    private javax.swing.JComboBox<Integer> getPagesCombo() {
        if (pagesCombo == null) {
            pagesCombo = new javax.swing.JComboBox<>();
            pagesCombo.setPreferredSize(new Dimension(100, 25));
            pagesCombo.addActionListener(e -> {
                if (pagesCombo.getSelectedItem() != null) {
                    CURRENT_PAGE = (Integer) pagesCombo.getSelectedItem();
                    performSearch();
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
            totalInfantsLabel = new JLabel(MessageBundle.getMessage("angal.maternity.total.label") + ": 0");
        }
        return totalInfantsLabel;
    }

    private JPanel getMiddlePanel() {
        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(getVisitFilterPanel(), BorderLayout.WEST);
        middlePanel.add(getVisitListPanel(), BorderLayout.CENTER);
        return middlePanel;
    }

    private JPanel getVisitFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtres visites"));
        filterPanel.setPreferredSize(new Dimension(350, 180));
        filterPanel.setMinimumSize(new Dimension(350, 180));

        // Type de visite (RadioButtons)
        JPanel typePanel = new JPanel();
        typePanel.setBorder(BorderFactory.createTitledBorder("Type de visite"));
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        typePanel.setAlignmentX(LEFT_ALIGNMENT);

        allVisitsRadio = new JRadioButton("Toutes");
        pcrVisitsRadio = new JRadioButton("PCR");
        clinicalVisitsRadio = new JRadioButton("Clinique");

        ButtonGroup visitTypeGroup = new ButtonGroup();
        visitTypeGroup.add(allVisitsRadio);
        visitTypeGroup.add(pcrVisitsRadio);
        visitTypeGroup.add(clinicalVisitsRadio);
        allVisitsRadio.setSelected(true);

        typePanel.add(allVisitsRadio);
        typePanel.add(pcrVisitsRadio);
        typePanel.add(clinicalVisitsRadio);
        filterPanel.add(typePanel);

        filterPanel.add(Box.createVerticalStrut(10));

        // Résultat PCR
        JPanel pcrResultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pcrResultPanel.setBorder(BorderFactory.createTitledBorder("Résultat PCR"));
        pcrResultPanel.setAlignmentX(LEFT_ALIGNMENT);
        pcrResultCombo = new javax.swing.JComboBox<>();
        pcrResultCombo.setPreferredSize(new Dimension(180, 25));
        pcrResultCombo.addItem(null);  // null = Tous
        for (HIVVisit.PCRResult result : HIVVisit.PCRResult.values()) {
            pcrResultCombo.addItem(result);
        }
        pcrResultPanel.add(pcrResultCombo);
        filterPanel.add(pcrResultPanel);

        return filterPanel;
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

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.actions.label")));

        buttonPanel.add(getJNewInfantButton());
        buttonPanel.add(getJUpdateInfantButton());
        buttonPanel.add(getJDeleteInfantButton());
        buttonPanel.add(getJNewVisitButton());
        buttonPanel.add(getJUpdateVisitButton());
        buttonPanel.add(getJDeleteVisitButton());
        buttonPanel.add(getJTherapyButton());
        buttonPanel.add(getJReportButton());
        buttonPanel.add(getJCloseButton());

        return buttonPanel;
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
        // À implémenter
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
        // À implémenter
    }

    private void newInfant() {
        // À implémenter
    }

    private void updateInfant() {
        // À implémenter
    }

    private void deleteInfant() {
        // À implémenter
    }

    private void newVisit() {
        // À implémenter
    }

    private void updateVisit() {
        // À implémenter
    }

    private void deleteVisit() {
        // À implémenter
    }

    private void therapy() {
        // À implémenter
    }

    private void report() {
        // À implémenter
    }

    @Override
    public void patientSelected(Patient patient) {
        // À implémenter
    }

    class InfantsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            if (infantList == null || r >= infantList.size()) {
                return null;
            }

            HIVInfant infant = infantList.get(r);
            Patient patient = infant.getPatient();

            if (c == 0) {
                return infant.getId();
            } else if (c == 1) {
                return patient != null ? patient.getCode() : "";
            } else if (c == 2) {
                return patient != null ? patient.getFirstName() + " " + patient.getSecondName() : "";
            } else if (c == 3) {
                return patient != null ? patient.getAge() + " mois" : "";
            } else if (c == 4) {
                return infant.getStatus() != null ? infant.getStatus().getDescription() : "";
            } else if (c == 5) {
                return "";
            } else if (c == 6) {
                return "...";
            }
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
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            if (visitList == null || r >= visitList.size()) {
                return null;
            }

            HIVVisit visit = visitList.get(r);

            if (c == 0) {
                return visit.getVisitDate() != null ? visit.getVisitDate().format(formatter) : "";
            } else if (c == 1) {
                return visit.getPcrResult() != null ? visit.getPcrResult().getDescription() : "";
            } else if (c == 2) {
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