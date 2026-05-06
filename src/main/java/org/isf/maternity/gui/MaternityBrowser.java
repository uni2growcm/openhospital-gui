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
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.gui.AdmissionBrowser;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.admission.model.AdmittedPatient;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.LabBrowser;
import org.isf.maternity.manager.PregnancyBrowserManager;
import org.isf.maternity.manager.PregnancyDeliveryBrowserManager;
import org.isf.maternity.manager.PregnancyVisitBrowserManager;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyDelivery;
import org.isf.maternity.model.PregnancyStatus;
import org.isf.maternity.model.PregnancyVisit;
import org.isf.maternity.model.RiskLevel;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.patvac.gui.PatVacBrowser;
import org.isf.therapy.gui.TherapyEdit;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.springframework.data.domain.Page;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class MaternityBrowser extends JFrame implements PatientInsert.PatientListener, PatientInsertExtended.PatientListener, SelectionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MIN_AGE = 10;
    private static final int MAX_AGE = 60;
    
    private final String[] columnHeaders = {
            MessageBundle.getMessage("angal.maternity.pregnancy.id.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.code.txt.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.date.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.lmp.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.edd.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.risklevel.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.status.col").toUpperCase()
    };

    private final int[] columnWidths = { 50, 70, 150, 50, 120, 100, 100, 80, 100 };

    private final String[] vColumns = {
            MessageBundle.getMessage("angal.maternity.visitdate.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.typology.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.visitnote.col").toUpperCase()
    };

    private final int[] vColumnWidths = { 100, 150, 400 };

    private final MaternityBrowser myFrame;
    private List<Pregnancy> pregnancyList = new ArrayList<>();
    private List<PregnancyVisit> visitList = new ArrayList<>();

    private PregnancyBrowserManager pregnancyManager;
    private PregnancyVisitBrowserManager visitManager;

    private JTable pregnancyTable;
    private JTable visitTable;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox<Integer> pagesCombo;
    private JLabel underLabel;
    private JLabel totalPregnanciesLabel;
    private PregnanciesTableModel model;
    private int TOTAL_PAGES = 0;
    private int CURRENT_PAGE = 1;
    private long TOTAL_PREGNANCIES = 0;
    private final int PAGE_SIZE = 3;

    private JTextField patientCodeFilter;
    private GoodDateChooser dateFrom;
    private GoodDateChooser dateTo;
    private GoodDateChooser lmpDateFrom;
    private GoodDateChooser lmpDateTo;
    private VoLimitedTextField ageFromField;
    private VoLimitedTextField ageToField;
    private JComboBox<Object> pregnancyStatusCombo;
    private JComboBox<Object> riskLevelCombo;
    private JRadioButton prenatalRadio;
    private JRadioButton postnatalRadio;
    private JButton searchButton;
    private JButton resetButton;

    private JComboBox<Typology> typologyFilterCombo;
    private List<Typology> typologyList;

    private Pregnancy selectedPregnancy;
    private int selectedVisitRow = -1;

    public MaternityBrowser() throws OHServiceException {
        setTitle(MessageBundle.getMessage("angal.maternity.browser.title"));
        myFrame = this;
        initManagers();
        loadTypologies();
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
        addCloseListener();
    }

    public MaternityBrowser(Patient patient) throws OHServiceException {
        setTitle(MessageBundle.getMessage("angal.maternity.browser.title"));
        myFrame = this;
        initManagers();
        loadTypologies();
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);

        if (patient != null) {
            patientCodeFilter.setText(String.valueOf(patient.getCode()));
            performSearch();
        }

        addCloseListener();
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
        pregnancyManager = Context.getApplicationContext().getBean(PregnancyBrowserManager.class);
        visitManager = Context.getApplicationContext().getBean(PregnancyVisitBrowserManager.class);
    }

    private void loadTypologies() {
        try {
            TypologyBrowserManager typologyManager = Context.getApplicationContext().getBean(TypologyBrowserManager.class);
            typologyList = typologyManager.getTypologies(Family.VISITTYPE);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            typologyList = new ArrayList<>();
        }
    }

    private void initComponents() throws OHServiceException {
        setLayout(new BorderLayout());
        initializeStatusCombo();
        initializeRiskLevelCombo();
        add(getTopPanel(), BorderLayout.NORTH);
        add(getMiddlePanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
        performSearch();
    }

    private void initializeStatusCombo() {
        pregnancyStatusCombo = new JComboBox<>();
        pregnancyStatusCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (PregnancyStatus status : PregnancyStatus.values()) {
            pregnancyStatusCombo.addItem(status);
        }
    }

    private void initializeRiskLevelCombo() {
        riskLevelCombo = new JComboBox<>();
        riskLevelCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (RiskLevel risk : RiskLevel.values()) {
            riskLevelCombo.addItem(risk);
        }
    }

    private JPanel getTopPanel() throws OHServiceException {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getFilterPanel(), BorderLayout.WEST);
        topPanel.add(getPregnancyListPanel(), BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.setPreferredSize(new Dimension(350, 650));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        // Filtre par date de création
        JPanel creationDatePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        creationDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.date.interval.label")));
        dateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
        dateTo = new GoodDateChooser(LocalDate.now());
        creationDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        creationDatePanel.add(dateFrom);
        creationDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        creationDatePanel.add(dateTo);
        filterPanel.add(creationDatePanel);

        // Filtre par LMP
        JPanel lmpDatePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        lmpDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.lmpinterval.label")));
        lmpDateFrom = new GoodDateChooser(LocalDate.now().minusMonths(12));
        lmpDateTo = new GoodDateChooser(LocalDate.now());
        lmpDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        lmpDatePanel.add(lmpDateFrom);
        lmpDatePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        lmpDatePanel.add(lmpDateTo);
        filterPanel.add(lmpDatePanel);

        // Filtre par âge
        JPanel agePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        agePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.ageinterval.label")));
        ageFromField = new VoLimitedTextField(3, 3);
        ageFromField.setText(String.valueOf(MIN_AGE));
        ageToField = new VoLimitedTextField(3, 3);
        ageToField.setText(String.valueOf(MAX_AGE));
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.agefrom.label")));
        agePanel.add(ageFromField);
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.ageto.label")));
        agePanel.add(ageToField);
        filterPanel.add(agePanel);

        // Filtre par statut
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.status.label")));
        statusPanel.add(pregnancyStatusCombo);
        filterPanel.add(statusPanel);

        // Filtre par niveau de risque
        JPanel riskPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        riskPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.risklevel.label")));
        riskPanel.add(riskLevelCombo);
        filterPanel.add(riskPanel);

        // Boutons
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

    private JPanel getPregnancyListPanel() throws OHServiceException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getPregnancyTablePanel(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getPregnancyTablePanel() {
        model = new PregnanciesTableModel();
        pregnancyTable = new JTable(model);
        pregnancyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pregnancyTable.setAutoCreateRowSorter(true);

        for (int i = 0; i < columnHeaders.length; i++) {
            pregnancyTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        pregnancyTable.getSelectionModel().addListSelectionListener(new TableListener());

        JScrollPane pregnancyScrollPane = new JScrollPane(pregnancyTable);
        pregnancyScrollPane.setPreferredSize(new Dimension(900, 400));
        return pregnancyScrollPane;
    }

    private JPanel getPaginationPanel() {
        JPanel paginatePanel = new JPanel(new WrapLayout());
        paginatePanel.add(getPrevButton());
        paginatePanel.add(getPagesCombo());
        paginatePanel.add(getUnderLabel());
        paginatePanel.add(getNextButton());
        paginatePanel.add(getTotalPregnanciesLabel());
        return paginatePanel;
    }

    private JButton getNextButton() {
        if (nextButton == null) {
            nextButton = new JButton(">");
            nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES != 1);
            nextButton.addActionListener(actionEvent -> {
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
            prevButton.addActionListener(actionEvent -> {
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
            pagesCombo.addActionListener(actionEvent -> {
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

    private JLabel getTotalPregnanciesLabel() {
        if (totalPregnanciesLabel == null) {
            totalPregnanciesLabel = new JLabel(MessageBundle.getMessage("angal.maternity.total.label") + ": 0");
        }
        return totalPregnanciesLabel;
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
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.visitfilter.label")));
        filterPanel.setPreferredSize(new Dimension(220, 250));

        ButtonGroup visitTypeGroup = new ButtonGroup();
        prenatalRadio = new JRadioButton(MessageBundle.getMessage("angal.maternity.prenatal.label"));
        postnatalRadio = new JRadioButton(MessageBundle.getMessage("angal.maternity.postnatal.label"));
        JRadioButton allVisitsRadio = new JRadioButton(MessageBundle.getMessage("angal.common.all.label"));

        allVisitsRadio.setSelected(true);

        visitTypeGroup.add(prenatalRadio);
        visitTypeGroup.add(postnatalRadio);
        visitTypeGroup.add(allVisitsRadio);

        filterPanel.add(prenatalRadio);
        filterPanel.add(postnatalRadio);
        filterPanel.add(allVisitsRadio);

        filterPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel typologyFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typologyFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.typology.filter") + ":"));
        typologyFilterCombo = new JComboBox<>();

        typologyFilterCombo.addItem(null);

        if (typologyList != null) {
            for (Typology typology : typologyList) {
                typologyFilterCombo.addItem(typology);
            }
        }

        typologyFilterCombo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return super.getListCellRendererComponent(list, MessageBundle.getMessage("angal.common.all.label"), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        typologyFilterCombo.setPreferredSize(new Dimension(180, 25));
        typologyFilterPanel.add(typologyFilterCombo);
        filterPanel.add(typologyFilterPanel);

        prenatalRadio.addActionListener(e -> filterVisits());
        postnatalRadio.addActionListener(e -> filterVisits());
        allVisitsRadio.addActionListener(e -> filterVisits());
        typologyFilterCombo.addActionListener(e -> filterVisits());

        return filterPanel;
    }

    private JScrollPane getVisitListPanel() {
        visitTable = new JTable(new MaternityVisitsTableModel());
        visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        visitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedVisitRow = visitTable.getSelectedRow();
            }
        });

        for (int i = 0; i < vColumns.length; i++) {
            visitTable.getColumnModel().getColumn(i).setPreferredWidth(vColumnWidths[i]);
        }

        JScrollPane visitScrollPane = new JScrollPane(visitTable);
        visitScrollPane.setPreferredSize(new Dimension(600, 300));
        return visitScrollPane;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.actions.label")));

        buttonPanel.add(getJNewPregnancyButton());
        buttonPanel.add(getJUpdatePregnancyButton());
        buttonPanel.add(getJDeletePregnancyButton());
        buttonPanel.add(getJNewVisitButton());
        buttonPanel.add(getJUpdateVisitButton());
        buttonPanel.add(getJDeleteVisitButton());
        buttonPanel.add(getJNewDeliveryButton());
        buttonPanel.add(getJUpdateDeliveryButton());
        buttonPanel.add(getJDeleteDeliveryButton());
        buttonPanel.add(getJAdmissionButton());
        buttonPanel.add(getJExamsButton());
        buttonPanel.add(getJVaccinButton());
        buttonPanel.add(getJTherapyButton());
        buttonPanel.add(getJCloseButton());

        return buttonPanel;
    }

    private JButton getJNewPregnancyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.new.btn"));
        button.addActionListener(e -> newPregnancy());
        return button;
    }

    private JButton getJUpdatePregnancyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.update.btn"));
        button.addActionListener(e -> updatePregnancy());
        return button;
    }

    private JButton getJDeletePregnancyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.delete.btn"));
        button.addActionListener(e -> deletePregnancy());
        return button;
    }

    private JButton getJNewVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.newvisit.btn"));
        button.addActionListener(e -> newVisit());
        return button;
    }

    private JButton getJUpdateVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.updatevisit.btn"));
        button.addActionListener(e -> updateVisit());
        return button;
    }

    private JButton getJDeleteVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.deletevisit.btn"));
        button.addActionListener(e -> deleteVisit());
        return button;
    }

    private JButton getJNewDeliveryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.newdelivery.btn"));
        button.addActionListener(e -> newDelivery());
        return button;
    }

    private JButton getJUpdateDeliveryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.updatedelivery.btn"));
        button.addActionListener(e -> updateDelivery());
        return button;
    }

    private JButton getJDeleteDeliveryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.deletedelivery.btn"));
        button.addActionListener(e -> deleteDelivery());
        return button;
    }

    private JButton getJAdmissionButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.admission.btn"));
        button.addActionListener(e -> admission());
        return button;
    }

    private JButton getJExamsButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.opd.exams.btn"));
        button.addActionListener(e -> exams());
        return button;
    }

    private JButton getJVaccinButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.cpn.vaccin.btn"));
        button.addActionListener(e -> vaccins());
        return button;
    }

    private JButton getJTherapyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.therapy.btn"));
        button.addActionListener(e -> therapy());
        return button;
    }

    private JButton getJCloseButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
        button.addActionListener(e -> dispose());
        return button;
    }

    private void performSearch() {
        try {
            String patientCode = patientCodeFilter.getText().trim();

            LocalDate dateBegin = dateFrom.getDate();
            LocalDate dateEnd = dateTo.getDate();
            LocalDate lmpFrom = lmpDateFrom.getDate();
            LocalDate lmpTo = lmpDateTo.getDate();

            int ageFrom = Integer.parseInt(ageFromField.getText());
            int ageTo = Integer.parseInt(ageToField.getText());

            Object selectedStatus = pregnancyStatusCombo.getSelectedItem();
            Object selectedRisk = riskLevelCombo.getSelectedItem();

            if (dateBegin.isAfter(dateEnd)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            if (lmpFrom.isAfter(lmpTo)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            if (ageFrom > ageTo) {
                MessageDialog.error(this, "angal.common.agefrommustbelowerthanageto.msg");
                return;
            }

            PregnancyStatus status = null;
            if (selectedStatus != null && !selectedStatus.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                status = (PregnancyStatus) selectedStatus;
            }

            RiskLevel risk = null;
            if (selectedRisk != null && !selectedRisk.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                risk = (RiskLevel) selectedRisk;
            }

            LocalDateTime dateFromDateTime = dateBegin.atStartOfDay();
            LocalDateTime dateToDateTime = dateEnd.plusDays(1).atStartOfDay();
            LocalDateTime lmpFromDateTime = lmpFrom.atStartOfDay();
            LocalDateTime lmpToDateTime = lmpTo.plusDays(1).atStartOfDay();

            Integer patientCodeInt = null;
            if (!patientCode.isEmpty()) {
                try {
                    patientCodeInt = Integer.parseInt(patientCode);
                } catch (NumberFormatException e) {
                    MessageDialog.error(this, "angal.common.pleaseinsertavalidnumber.msg");
                    return;
                }
            }

            Page<Pregnancy> pagedResult = pregnancyManager.searchPregnancies(
                    patientCodeInt, status, risk, dateFromDateTime, dateToDateTime,
                    lmpFromDateTime, lmpToDateTime, CURRENT_PAGE - 1, PAGE_SIZE);

            pregnancyList = pagedResult.getContent();
            TOTAL_PREGNANCIES = pagedResult.getTotalElements();
            TOTAL_PAGES = pagedResult.getTotalPages();

            List<Pregnancy> filteredByAge = new ArrayList<>();
            for (Pregnancy pregnancy : pregnancyList) {
                Patient patient = pregnancy.getPatient();
                if (patient != null) {
                    int age = patient.getAge();
                    if (age >= ageFrom && age <= ageTo) {
                        filteredByAge.add(pregnancy);
                    }
                }
            }

            if (ageFrom > MIN_AGE || ageTo < MAX_AGE) {
                pregnancyList = filteredByAge;
                TOTAL_PREGNANCIES = filteredByAge.size();
                TOTAL_PAGES = (int) Math.ceil((double) TOTAL_PREGNANCIES / PAGE_SIZE);
                if (TOTAL_PAGES == 0) {
                    TOTAL_PAGES = 1;
                }
            } else {
                TOTAL_PREGNANCIES = pagedResult.getTotalElements();
                TOTAL_PAGES = pagedResult.getTotalPages();
            }

            updatePaginationUI();
            model.fireTableDataChanged();
            pregnancyTable.updateUI();

            if (pregnancyList.isEmpty()) {
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.common.info.title"),
                        MessageBundle.getMessage("angal.common.nodatatoshow.msg"));
            }

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, "angal.common.pleaseentervalidnumbers.msg");
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void updatePaginationUI() {
        underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.page.label"));
        totalPregnanciesLabel.setText(MessageBundle.getMessage("angal.maternity.total.label") + ": " + TOTAL_PREGNANCIES);

        ActionListener[] listeners = pagesCombo.getActionListeners();
        for (ActionListener listener : listeners) {
            pagesCombo.removeActionListener(listener);
        }

        pagesCombo.removeAllItems();
        for (int i = 1; i <= TOTAL_PAGES; i++) {
            pagesCombo.addItem(i);
        }
        if (TOTAL_PAGES > 0) {
            pagesCombo.setSelectedItem(CURRENT_PAGE);
        }

        for (ActionListener listener : listeners) {
            pagesCombo.addActionListener(listener);
        }

        nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES > 1);
        prevButton.setEnabled(CURRENT_PAGE > 1);
    }

    private void resetFilters() {
        patientCodeFilter.setText("");
        dateFrom.setDate(LocalDate.now().minusMonths(6));
        dateTo.setDate(LocalDate.now());
        lmpDateFrom.setDate(LocalDate.now().minusMonths(12));
        lmpDateTo.setDate(LocalDate.now());
        ageFromField.setText(String.valueOf(MIN_AGE));
        ageToField.setText(String.valueOf(MAX_AGE));
        pregnancyStatusCombo.setSelectedIndex(0);
        riskLevelCombo.setSelectedIndex(0);
        typologyFilterCombo.setSelectedIndex(0);
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void filterVisits() {
        if (selectedPregnancy == null) {
            visitList = new ArrayList<>();
            ((MaternityVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
            return;
        }

        try {
            List<PregnancyVisit> visits = new ArrayList<>();
            String selectedTypologyCode = null;

            Object selectedItem = typologyFilterCombo.getSelectedItem();
            if (selectedItem != null && selectedItem instanceof Typology) {
                selectedTypologyCode = ((Typology) selectedItem).getCode();
            }

            if (selectedTypologyCode != null) {
                visits = visitManager.getVisitsByFilters(
                        selectedPregnancy.getId(), null, null, selectedTypologyCode);
            } else if (prenatalRadio.isSelected()) {
                visits = visitManager.getVisitsByFilters(
                        selectedPregnancy.getId(), null, null, "ANC");
            } else if (postnatalRadio.isSelected()) {
                visits = visitManager.getVisitsByFilters(
                        selectedPregnancy.getId(), null, null, "PNC");
            } else {
                visits = visitManager.getVisitsByPregnancy(selectedPregnancy.getId());
            }

            visitList = visits != null ? visits : new ArrayList<>();
            ((MaternityVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
            selectedVisitRow = -1;

        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
            visitList = new ArrayList<>();
            ((MaternityVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
        }
    }

    private void newPregnancy() {
        MaternityPregnancyEdit edit = new MaternityPregnancyEdit(this, (Patient) null, true);
        edit.addMaternityPregnancyListener(new MaternityPregnancyEdit.MaternityPregnancyListener() {
            @Override
            public void pregnancyInserted(AWTEvent e, Pregnancy pregnancy) {
                performSearch();
            }

            @Override
            public void pregnancyUpdated(AWTEvent e, Pregnancy pregnancy) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }


    private void updatePregnancy() {
        int viewRow = pregnancyTable.getSelectedRow();
        if (viewRow < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int modelRow = pregnancyTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= pregnancyList.size()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }

        Pregnancy selectedPregnancy = pregnancyList.get(modelRow);

        MaternityPregnancyEdit edit = new MaternityPregnancyEdit(this, selectedPregnancy, false);
        edit.addMaternityPregnancyListener(new MaternityPregnancyEdit.MaternityPregnancyListener() {
            @Override
            public void pregnancyInserted(AWTEvent e, Pregnancy pregnancy) {
                performSearch();
            }

            @Override
            public void pregnancyUpdated(AWTEvent e, Pregnancy pregnancy) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void deletePregnancy() {
        int viewRow = pregnancyTable.getSelectedRow();
        if (viewRow < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int modelRow = pregnancyTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= pregnancyList.size()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return;
        }

        Pregnancy selectedPregnancy = pregnancyList.get(modelRow);

        String confirmMessage = MessageBundle.getMessage("angal.maternity.deletepregnancy.confirm.msg");
        int answer = MessageDialog.yesNo(this, confirmMessage);

        if (answer == JOptionPane.YES_OPTION) {
            try {
                pregnancyManager.deletePregnancy(selectedPregnancy);
                performSearch();
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.common.info.title"),
                        MessageBundle.getMessage("angal.maternity.deletepregnancy.success.msg"));
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void newVisit() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        VisitEdit edit = new VisitEdit(this, selectedPregnancy, true);
        edit.addMaternityVisitListener(new VisitEdit.MaternityVisitListener() {
            @Override
            public void visitInserted(AWTEvent e, PregnancyVisit visit) {
                filterVisits();
            }

            @Override
            public void visitUpdated(AWTEvent e, PregnancyVisit visit) {
                filterVisits();
            }
        });
        edit.setVisible(true);
    }

    private void updateVisit() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        PregnancyVisit selectedVisit = visitList.get(selectedVisitRow);

        VisitEdit edit = new VisitEdit(this, selectedVisit, false);
        edit.addMaternityVisitListener(new VisitEdit.MaternityVisitListener() {
            @Override
            public void visitInserted(AWTEvent e, PregnancyVisit visit) {
                filterVisits();
            }

            @Override
            public void visitUpdated(AWTEvent e, PregnancyVisit visit) {
                filterVisits();
            }
        });
        edit.setVisible(true);
    }

    private void deleteVisit() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        PregnancyVisit selectedVisit = visitList.get(selectedVisitRow);

        String confirmMessage = MessageBundle.getMessage("angal.maternity.deletevisit.confirm.msg");
        int answer = MessageDialog.yesNo(this, confirmMessage);

        if (answer == JOptionPane.YES_OPTION) {
            try {
                visitManager.deleteVisit(selectedVisit);
                filterVisits();
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.common.info.title"),
                        MessageBundle.getMessage("angal.maternity.deletevisit.success.msg"));
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void newDelivery() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        DeliveryEdit edit = new DeliveryEdit(this, selectedPregnancy);

        edit.setVisible(true);
    }
    
    private void updateDelivery() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        try {
            PregnancyDelivery delivery = Context.getApplicationContext()
                    .getBean(PregnancyDeliveryBrowserManager.class)
                    .getDeliveryByPregnancy(selectedPregnancy.getId());

            if (delivery == null) {
                MessageDialog.error(this, "angal.maternity.delivery.nodeliveryfound.msg");
                return;
            }

            DeliveryEdit edit = new DeliveryEdit(this, selectedPregnancy);
            edit.setVisible(true);

        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void deleteDelivery() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        try {
            PregnancyDeliveryBrowserManager deliveryManager = 
                    Context.getApplicationContext()
                    .getBean(PregnancyDeliveryBrowserManager.class);

            PregnancyDelivery delivery = deliveryManager.getDeliveryByPregnancy(selectedPregnancy.getId());

            if (delivery == null) {
                MessageDialog.error(this, "angal.maternity.delivery.nodeliveryfound.msg");
                return;
            }

            String confirmMessage = MessageBundle.getMessage("angal.maternity.delivery.deletedelivery.confirm.msg");
            int answer = MessageDialog.yesNo(this, confirmMessage);

            if (answer == JOptionPane.YES_OPTION) {
                deliveryManager.deleteDelivery(delivery);
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.common.info.title"),
                        MessageBundle.getMessage("angal.maternity.delivery.deletedelivery.success.msg"));
            }

        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void admission() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        Patient patient = selectedPregnancy.getPatient();

        AdmissionBrowserManager admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
        Admission currentAdmission = admissionManager.getCurrentAdmission(patient);

        AdmittedPatient admittedPatient = new AdmittedPatient(patient, currentAdmission);
        boolean editing = (currentAdmission != null);

        AdmissionBrowser admissionBrowser = new AdmissionBrowser(this, admittedPatient, editing);
        admissionBrowser.setVisible(true);
    }

    private void exams() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        LabBrowser labBrowser = new LabBrowser();
        labBrowser.setVisible(true);
    }

    private void vaccins() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        PatVacBrowser patVacBrowser = new PatVacBrowser();
        patVacBrowser.setVisible(true);
    }

    private void therapy() {
        if (selectedPregnancy == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapregnancyfirst.msg");
            return;
        }

        Patient patient = selectedPregnancy.getPatient();
        if (patient == null) {
            MessageDialog.error(this, "angal.maternity.pregnancy.patient.notfound");
            return;
        }

        AdmissionBrowserManager admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
        Admission currentAdmission = admissionManager.getCurrentAdmission(patient);
        boolean isAdmitted = (currentAdmission != null);

        TherapyEdit therapyEdit = new TherapyEdit(this, patient, isAdmitted);
        therapyEdit.setVisible(true);
    }

    @Override
    public void patientSelected(Patient patient) {
        if (patient != null) {
            MaternityPregnancyEdit edit = new MaternityPregnancyEdit(this, patient, true);
            edit.addMaternityPregnancyListener(new MaternityPregnancyEdit.MaternityPregnancyListener() {
                @Override
                public void pregnancyInserted(AWTEvent e, Pregnancy pregnancy) {
                    performSearch();
                }

                @Override
                public void pregnancyUpdated(AWTEvent e, Pregnancy pregnancy) {
                    performSearch();
                }
            });
            edit.setVisible(true);
        }
    }

    class MaternityVisitsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public String getColumnName(int c) {
            return vColumns[c];
        }

        public int getColumnCount() {
            return vColumns.length;
        }

        public int getRowCount() {
            return visitList != null ? visitList.size() : 0;
        }

        public Object getValueAt(int r, int c) {
            if (visitList == null || r >= visitList.size()) {
                return null;
            }

            PregnancyVisit visit = visitList.get(r);

            if (c == -1) {
                return visit;
            } else if (c == 0) {
                return visit.getVisitDate() != null ? visit.getVisitDate().format(formatter) : "";
            } else if (c == 1) {
                return visit.getVisitType() != null ? visit.getVisitType().getDescription() : "";
            } else if (c == 2) {
                String notes = visit.getClinicalNotes();
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

    class PregnanciesTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public int getRowCount() {
            return pregnancyList != null ? pregnancyList.size() : 0;
        }

        public String getColumnName(int c) {
            return columnHeaders[c];
        }

        public int getColumnCount() {
            return columnHeaders.length;
        }

        public Object getValueAt(int r, int c) {
            if (pregnancyList == null || r >= pregnancyList.size()) {
                return null;
            }

            Pregnancy pregnancy = pregnancyList.get(r);
            Patient patient = pregnancy.getPatient();

            if (c == -1) {
                return pregnancy;
            } else if (c == 0) {
                return pregnancy.getId();
            } else if (c == 1) {
                return patient != null ? patient.getCode() : "";
            } else if (c == 2) {
                return patient != null ? patient.getSecondName() + " " + patient.getFirstName() : "";
            } else if (c == 3) {
                return patient != null ? patient.getAge() : "";
            } else if (c == 4) {
                return pregnancy.getDate() != null ? pregnancy.getDate().format(dateTimeFormatter) : "";
            } else if (c == 5) {
                return pregnancy.getLmp() != null ? pregnancy.getLmp().format(formatter) : "";
            } else if (c == 6) {
                return pregnancy.getEddLmp() != null ? pregnancy.getEddLmp().format(formatter) : "";
            } else if (c == 7) {
                return pregnancy.getRiskLevel() != null ? pregnancy.getRiskLevel().getDescription() : "";
            } else if (c == 8) {
                return pregnancy.getStatus() != null ? pregnancy.getStatus().toString() : "";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    @Override
    public void patientUpdated(AWTEvent e) {
        performSearch();
    }

    @Override
    public void patientInserted(AWTEvent e) {
        performSearch();
    }

    class TableListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            int viewRow = pregnancyTable.getSelectedRow();
            if (!arg0.getValueIsAdjusting() && viewRow > -1) {
                int modelRow = pregnancyTable.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < pregnancyList.size()) {
                    selectedPregnancy = pregnancyList.get(modelRow);
                    selectedVisitRow = -1;
                    filterVisits();
                }
            }
        }
    }
}