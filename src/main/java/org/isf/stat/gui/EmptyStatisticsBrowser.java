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
package org.isf.stat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.disctype.model.DischargeType;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.stat2.manager.StatsBrowserManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.springframework.data.domain.Page;
import org.isf.utils.jobjects.GoodDateChooser;

public class EmptyStatisticsBrowser extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private JPanel filtersPanel;
    private JPanel dataPanel;
    private JPanel paginationPanel;

    private JPanel periodAndAgePanel;
    private JPanel examsPanel;
    private JPanel vaccinesPanel;
    private JPanel diseasesAndDischargeTypesPanel;
    private JPanel parametersPanel;

    private GoodDateChooser periodFromDateChooser;
    private GoodDateChooser periodToDateChooser;
    private JTextField ageFromField;
    private JTextField ageToField;

    private JComboBox<String> examsCombo;
    private JComboBox<String> examResultsCombo;
    private GoodDateChooser examPeriodFrom;
    private GoodDateChooser examPeriodTo;

    private JComboBox<String> vaccinesCombo;
    private GoodDateChooser vaccinePeriodFrom;
    private GoodDateChooser vaccinePeriodTo;

    private JComboBox<Disease> diseasesCombo;
    private JComboBox<DischargeType> dischargeTypesCombo;

    private JCheckBox heightCheck;
    private JCheckBox weightCheck;
    private JCheckBox arterialPressureCheck;
    private JCheckBox cardiacFrequencyCheck;
    private JCheckBox temperatureCheck;
    private JCheckBox saturationCheck;
    private JCheckBox respiratoryRateCheck;

    private JPanel pregnancyPanel;
    private JComboBox<String> riskLevelCombo;
    private JComboBox<String> statusCombo;
    private JComboBox<String> gravidityCombo;
    private JComboBox<String> parityCombo;
    private JComboBox<String> miscarriageCombo;
    private JComboBox<String> gestationalAgeCombo;

    private JPanel pregnancyVisitPanel;
    private JComboBox<String> visitTypeCombo;
    private JComboBox<String> visitCountCombo;
    private JComboBox<String> maternalWeightCombo;
    private JComboBox<String> urineProteinCombo;
    private JComboBox<String> edemaCombo;
    private JComboBox<String> fetalPresentationCombo;
    private JComboBox<String> systolicBpCombo;
    private JComboBox<String> diastolicBpCombo;

    private JLabel resultCountLabel;
    private int resultCount = 0;
    private JButton filterBtn;
    private JButton filterResetBtn;

    private List<Exam> examsData;
    private List<ExamRow> examResultsData;
    private List<Vaccine> vaccinesData;
    private List<Disease> diseasesData;
    private List<DischargeType> dischargeTypesData;
    private JTable jDataTable;
    private StatsPregnancyBrowsingTableModel jDataTableModel;
    private List<Patient> patientList = new ArrayList<>();
    private final String[] jDataTabelColumns = {
            MessageBundle.getMessage("angal.patient.firstname1"),
            MessageBundle.getMessage("angal.report.labregister.age")
    };
    private final int[] jDataTableColumnWidth = { 200, 50 };

    private int _start_index = 0;
    private final int _items_per_page = 100;
    private Integer ageFrom = null;
    private Integer ageTo = null;
    private String _selectedExam = "";
    private String _selectedExamResult = "";
    private String _selectedVaccine = "";
    private String _selectedDisease = "";
    private String _selectedDischargeType = "";
    private LocalDateTime periodFrom = null;
    private LocalDateTime periodTo = null;
    private LocalDateTime examperiodFrom = null;
    private LocalDateTime examperiodTo = null;
    private LocalDateTime vaccineperiodFrom = null;
    private LocalDateTime vaccineperiodTo = null;
    private boolean _parameter_height_check = false;
    private boolean _parameter_weight_check = false;
    private boolean _parameter_art_press_check = false;
    private boolean _parameter_card_freq_check = false;
    private boolean _parameter_temp_check = false;
    private boolean _parameter_saturation_check = false;
    private boolean _parameter_resp_rate_check = false;

    private JButton paginationFirstBtn;
    private JButton paginationLastBtn;
    private JButton paginationPrevBtn;
    private JButton paginationNextBtn;
    private JComboBox<Integer> paginationCombo;
    private final JLabel paginationLabel = new JLabel();

    private final VaccineBrowserManager vaccinesManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
    private final ExamBrowsingManager examsManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
    private final ExamRowBrowsingManager examResultsManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);
    private final DiseaseBrowserManager diseasesManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
    private final AdmissionBrowserManager admissionsManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
    private final StatsBrowserManager statsManager = Context.getApplicationContext().getBean(StatsBrowserManager.class);

    public EmptyStatisticsBrowser() {
        super();
        initialize();
    }

    private void initialize() {
        setTitle(MessageBundle.getMessage("angal.stat.menu.cpn"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();

        setMinimumSize(new Dimension(900, 600));
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        getContentPane().add(getFiltersPanel(), BorderLayout.WEST);
        getContentPane().add(getDataPanel(), BorderLayout.CENTER);
        getContentPane().add(getPaginationPanel(), BorderLayout.SOUTH);
    }

    private JPanel getPregnancyVisitPanel() {
        if (pregnancyVisitPanel != null) {
            return pregnancyVisitPanel;
        }

        pregnancyVisitPanel = new JPanel();
        pregnancyVisitPanel.setLayout(new BoxLayout(pregnancyVisitPanel, BoxLayout.Y_AXIS));
        pregnancyVisitPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.stat.pregnancy.visit")));

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 10, 5));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel visitTypePanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.visit.type"),
                visitTypeCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.visittype.anc"),
                        MessageBundle.getMessage("angal.stat.visittype.pnc")
                })
        );
        gridPanel.add(visitTypePanel);

        JPanel weightPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.maternal.weight"),
                maternalWeightCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.maternalweight.lt50"),
                        MessageBundle.getMessage("angal.stat.maternalweight.50to70"),
                        MessageBundle.getMessage("angal.stat.maternalweight.70to90"),
                        MessageBundle.getMessage("angal.stat.maternalweight.gt90")
                })
        );
        gridPanel.add(weightPanel);

        JPanel proteinPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.urine.protein"),
                urineProteinCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.urineprotein.positive"),
                        MessageBundle.getMessage("angal.stat.urineprotein.negative")
                })
        );
        gridPanel.add(proteinPanel);

        JPanel visitCountPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.visit.count"),
                visitCountCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.count.1"),
                        MessageBundle.getMessage("angal.stat.count.2"),
                        MessageBundle.getMessage("angal.stat.count.2to3"),
                        MessageBundle.getMessage("angal.stat.count.4to7"),
                        MessageBundle.getMessage("angal.stat.count.8plus")
                })
        );
        gridPanel.add(visitCountPanel);

        JPanel edemaPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.edema"),
                edemaCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.edema.present"),
                        MessageBundle.getMessage("angal.stat.edema.absent")
                })
        );
        gridPanel.add(edemaPanel);

        JPanel presentationPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.fetal.presentation"),
                fetalPresentationCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.fetalpresentation.cephalic"),
                        MessageBundle.getMessage("angal.stat.fetalpresentation.breech"),
                        MessageBundle.getMessage("angal.stat.fetalpresentation.transverse"),
                        MessageBundle.getMessage("angal.stat.fetalpresentation.other")
                })
        );
        gridPanel.add(presentationPanel);

        JPanel systolicPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.systolic.bp"),
                systolicBpCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.systolic.lt120"),
                        MessageBundle.getMessage("angal.stat.systolic.120to139"),
                        MessageBundle.getMessage("angal.stat.systolic.gt140")
                })        );
        gridPanel.add(systolicPanel);

        JPanel diastolicPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.diastolic.bp"),
                diastolicBpCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.diastolic.lt80"),
                        MessageBundle.getMessage("angal.stat.diastolic.80to89"),
                        MessageBundle.getMessage("angal.stat.diastolic.gt90")
                })        );
        gridPanel.add(diastolicPanel);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gridPanel.add(emptyPanel);

        pregnancyVisitPanel.add(gridPanel);
        return pregnancyVisitPanel;
    }

    private JPanel getPregnancyPanel() {
        if (pregnancyPanel != null) {
            return pregnancyPanel;
        }

        pregnancyPanel = new JPanel();
        pregnancyPanel.setLayout(new BoxLayout(pregnancyPanel, BoxLayout.Y_AXIS));
        pregnancyPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.stat.pregnancy")));

        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel riskPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.risk.level"),
                riskLevelCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.risklevel.low"),
                        MessageBundle.getMessage("angal.stat.risklevel.medium"),
                        MessageBundle.getMessage("angal.stat.risklevel.high")
                })        );
        gridPanel.add(riskPanel);

        JPanel parityPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.parity"),
                parityCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.count.0"),
                        MessageBundle.getMessage("angal.stat.count.1"),
                        MessageBundle.getMessage("angal.stat.count.2"),
                        MessageBundle.getMessage("angal.stat.count.3plus")
                })        );
        gridPanel.add(parityPanel);

        JPanel miscarriagePanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.miscarriages"),
                miscarriageCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.count.0"),
                        MessageBundle.getMessage("angal.stat.count.1"),
                        MessageBundle.getMessage("angal.stat.count.2"),
                        MessageBundle.getMessage("angal.stat.count.3plus")
                })        );
        gridPanel.add(miscarriagePanel);

        JPanel statusPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.status"),
                statusCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.status.ongoing"),
                        MessageBundle.getMessage("angal.stat.status.completed")
                })        );
        gridPanel.add(statusPanel);

        JPanel gravidityPanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.gravidity"),
                gravidityCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.count.1"),
                        MessageBundle.getMessage("angal.stat.count.2"),
                        MessageBundle.getMessage("angal.stat.count.3"),
                        MessageBundle.getMessage("angal.stat.count.4plus")
                })        );
        gridPanel.add(gravidityPanel);

        JPanel gestationalAgePanel = createFilterPanel(
                MessageBundle.getMessage("angal.stat.gestational.age"),
                gestationalAgeCombo = new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.stat.all"),
                        MessageBundle.getMessage("angal.stat.gestage.lt12"),
                        MessageBundle.getMessage("angal.stat.gestage.12to24"),
                        MessageBundle.getMessage("angal.stat.gestage.24to36"),
                        MessageBundle.getMessage("angal.stat.gestage.gt36")
                })        );
        gridPanel.add(gestationalAgePanel);

        pregnancyPanel.add(gridPanel);
        return pregnancyPanel;
    }

    private JPanel createFilterPanel(String label, JComboBox<String> combo) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getFiltersPanel() {
        if (filtersPanel != null) {
            return filtersPanel;
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        AccordionPanel general = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.general"),
                getGeneralFiltersPanel(), true);

        AccordionPanel visites = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.visits"),
                getPregnancyVisitPanel(), false);

        AccordionPanel grossesse = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.pregnancy"),
                getPregnancyPanel(), false);

        List<AccordionPanel> all = List.of(general, visites, grossesse);
        general.setSiblings(all);
        visites.setSiblings(all);
        grossesse.setSiblings(all);

        mainPanel.add(general);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(visites);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(grossesse);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(getFilterButtonsPanel());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        filtersPanel = new JPanel(new BorderLayout());
        filtersPanel.add(scrollPane, BorderLayout.CENTER);

        filtersPanel.setPreferredSize(new Dimension(860, 0));

        return filtersPanel;
    }

    private JPanel getGeneralFiltersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getPeriodAndAgePanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getVaccinesPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getExamsPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getDiseasesAndDischargeTypesPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getParametersPanel());

        return panel;
    }

    private JPanel getDataPanel() {
        if (dataPanel != null) {
            return dataPanel;
        }
        dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(getDataTableScrollPane(), BorderLayout.CENTER);
        return dataPanel;
    }

    private JScrollPane getDataTableScrollPane() {
        jDataTableModel = new StatsPregnancyBrowsingTableModel();
        jDataTable = new JTable(jDataTableModel);

        jDataTable.getColumnModel().getColumn(0).setMinWidth(jDataTableColumnWidth[0]);
        jDataTable.getColumnModel().getColumn(1).setMinWidth(jDataTableColumnWidth[1]);

        JScrollPane tableScrollPane = new JScrollPane(jDataTable);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return tableScrollPane;
    }

    private JPanel getPaginationPanel() {
        if (paginationPanel != null) {
            return paginationPanel;
        }

        paginationPanel = new JPanel(new FlowLayout());
        paginationPanel.add(getResultCountPanel());
        paginationPanel.add(getPaginationFirstButton());
        paginationPanel.add(getPaginationPrevButton());
        paginationPanel.add(getPaginationCombo());
        paginationPanel.add(paginationLabel);
        paginationPanel.add(getPaginationNextButton());
        paginationPanel.add(getPaginationLastButton());

        initializePaginationCombo();

        return paginationPanel;
    }

    private JPanel getPeriodAndAgePanel() {
        if (periodAndAgePanel != null) {
            return periodAndAgePanel;
        }

        JLabel periodFromLabel = new JLabel(MessageBundle.getMessage("angal.common.dateFrom"));
        JLabel periodToLabel = new JLabel(MessageBundle.getMessage("angal.common.dateTo"));
        periodFromDateChooser = new GoodDateChooser(LocalDate.now());
        periodToDateChooser = new GoodDateChooser(LocalDate.now());

        JLabel ageFromLabel = new JLabel(MessageBundle.getMessage("angal.stat.agefrom"));
        ageFromField = new JTextField();
        ageFromField.setColumns(3);

        JLabel ageToLabel = new JLabel(MessageBundle.getMessage("angal.stat.ageto"));
        ageToField = new JTextField();
        ageToField.setColumns(3);

        periodAndAgePanel = new JPanel(new FlowLayout());
        periodAndAgePanel.add(periodFromLabel);
        periodAndAgePanel.add(periodFromDateChooser);
        periodAndAgePanel.add(periodToLabel);
        periodAndAgePanel.add(periodToDateChooser);
        periodAndAgePanel.add(ageFromLabel);
        periodAndAgePanel.add(ageFromField);
        periodAndAgePanel.add(ageToLabel);
        periodAndAgePanel.add(ageToField);

        periodAndAgePanel = setPanelBorder(periodAndAgePanel, MessageBundle.getMessage("angal.stat.admissionperiod"));

        return periodAndAgePanel;
    }

    private JPanel getVaccinesPanel() {
        if (vaccinesPanel != null) {
            return vaccinesPanel;
        }

        try {
            vaccinesData = vaccinesManager.getVaccine("P");
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
            vaccinesData = new ArrayList<>();
        }

        vaccinesCombo = new JComboBox<>();
        vaccinesCombo.addItem("");
        for (Vaccine vaccine : vaccinesData) {
            vaccinesCombo.addItem(vaccine.getDescription());
        }

        vaccinesCombo.addActionListener(e -> {
            Object selected = vaccinesCombo.getSelectedItem();
            _selectedVaccine = selected != null ? selected.toString() : "";
        });

        vaccinePeriodFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        vaccinePeriodTo = new GoodDateChooser(LocalDate.now());

        JPanel vaccinePeriodPanel = new JPanel(new FlowLayout());
        vaccinePeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodfrom")));
        vaccinePeriodPanel.add(vaccinePeriodFrom);
        vaccinePeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodto")));
        vaccinePeriodPanel.add(vaccinePeriodTo);

        vaccinesPanel = new JPanel();
        vaccinesPanel.setLayout(new BoxLayout(vaccinesPanel, BoxLayout.Y_AXIS));
        vaccinesPanel.add(vaccinesCombo);
        vaccinesPanel.add(vaccinePeriodPanel);

        vaccinesPanel = setPanelBorder(vaccinesPanel, MessageBundle.getMessage("angal.patvac.vaccine"));

        return vaccinesPanel;
    }

    private JPanel getExamsPanel() {
        if (examsPanel != null) {
            return examsPanel;
        }

        JPanel examsFlowPanel = new JPanel(new FlowLayout());
        JLabel examsLabel = new JLabel(MessageBundle.getMessage("angal.reduction.exam"));

        try {
            examsData = examsManager.getExams();
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
            examsData = new ArrayList<>();
        }

        examsCombo = new JComboBox<>();
        examsCombo.addItem("");
        for (Exam exam : examsData) {
            examsCombo.addItem(exam.getDescription());
        }

        examsFlowPanel.add(examsLabel);
        examsFlowPanel.add(examsCombo);

        JPanel examResultsFlowPanel = new JPanel(new FlowLayout());
        JLabel examResultsLabel = new JLabel(MessageBundle.getMessage("angal.stat.operationresult"));
        examResultsCombo = new JComboBox<>();
        examResultsFlowPanel.add(examResultsLabel);
        examResultsFlowPanel.add(examResultsCombo);

        examsCombo.addActionListener(e -> {
            Object selected = examsCombo.getSelectedItem();
            _selectedExam = selected != null ? selected.toString() : "";

            examResultsCombo.removeAllItems();
            if (!_selectedExam.isEmpty()) {
                try {
                    Exam selectedExam = null;
                    for (Exam exam : examsData) {
                        if (exam.getDescription().equals(_selectedExam)) {
                            selectedExam = exam;
                            break;
                        }
                    }

                    if (selectedExam != null) {
                        examResultsData = examResultsManager.getExamRowByExamCode(selectedExam.getCode());
                        examResultsCombo.addItem("");
                        for (ExamRow examRow : examResultsData) {
                            examResultsCombo.addItem(examRow.getDescription());
                        }
                    }
                } catch (NumberFormatException nfe) {
                    MessageDialog.error(EmptyStatisticsBrowser.this,
                            MessageBundle.getMessage("angal.stat.error.invalidexamcode"));
                } catch (OHServiceException ex) {
                    MessageDialog.showExceptions(ex);
                }
            }
        });

        examResultsCombo.addActionListener(e -> {
            Object selected = examResultsCombo.getSelectedItem();
            _selectedExamResult = selected != null ? selected.toString() : "";
        });

        examPeriodFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        examPeriodTo = new GoodDateChooser(LocalDate.now());

        JPanel examPeriodPanel = new JPanel(new FlowLayout());
        examPeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodfrom")));
        examPeriodPanel.add(examPeriodFrom);
        examPeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodto")));
        examPeriodPanel.add(examPeriodTo);

        examsPanel = new JPanel();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        examsPanel.add(examsFlowPanel);
        examsPanel.add(examResultsFlowPanel);
        examsPanel.add(examPeriodPanel);
        examsPanel = setPanelBorder(examsPanel, MessageBundle.getMessage("angal.stat.examsandresults"));

        return examsPanel;
    }

    private JPanel getDiseasesAndDischargeTypesPanel() {
        if (diseasesAndDischargeTypesPanel != null) {
            return diseasesAndDischargeTypesPanel;
        }

        JPanel diseasesPanel = new JPanel(new FlowLayout());
        JLabel diseasesLabel = new JLabel(MessageBundle.getMessage("angal.stat.disease"));
        diseasesCombo = new JComboBox<>();

        try {
            diseasesData = diseasesManager.getDiseaseAll();
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
            diseasesData = new ArrayList<>();
        }

        diseasesCombo.addItem(null);
        for (Disease disease : diseasesData) {
            diseasesCombo.addItem(disease);
        }
        diseasesPanel.add(diseasesLabel);
        diseasesPanel.add(diseasesCombo);

        JPanel dischargeTypesPanel = new JPanel(new FlowLayout());
        JLabel dischargeTypesLabel = new JLabel(MessageBundle.getMessage("angal.stat.dischargetype"));
        dischargeTypesCombo = new JComboBox<>();

        try {
            dischargeTypesData = admissionsManager.getDischargeType();
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
            dischargeTypesData = new ArrayList<>();
        }

        dischargeTypesCombo.addItem(null);
        for (DischargeType dischargeType : dischargeTypesData) {
            dischargeTypesCombo.addItem(dischargeType);
        }
        dischargeTypesPanel.add(dischargeTypesLabel);
        dischargeTypesPanel.add(dischargeTypesCombo);

        diseasesCombo.addActionListener(e -> {
            Object selected = diseasesCombo.getSelectedItem();
            _selectedDisease = selected != null ? selected.toString() : "";
        });

        dischargeTypesCombo.addActionListener(e -> {
            Object selected = dischargeTypesCombo.getSelectedItem();
            _selectedDischargeType = selected != null ? selected.toString() : "";
        });

        diseasesAndDischargeTypesPanel = new JPanel();
        diseasesAndDischargeTypesPanel.setLayout(new BoxLayout(diseasesAndDischargeTypesPanel, BoxLayout.Y_AXIS));
        diseasesAndDischargeTypesPanel.add(diseasesPanel);
        diseasesAndDischargeTypesPanel.add(dischargeTypesPanel);

        diseasesAndDischargeTypesPanel = setPanelBorder(diseasesAndDischargeTypesPanel,
                MessageBundle.getMessage("angal.stat.diseasesanddischargetypes"));

        return diseasesAndDischargeTypesPanel;
    }

    private JPanel getParametersPanel() {
        if (parametersPanel != null) {
            return parametersPanel;
        }

        parametersPanel = new JPanel();

        JPanel parametersPanelLine1 = new JPanel(new FlowLayout());
        JPanel parametersPanelLine2 = new JPanel(new FlowLayout());

        heightCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.height"));
        weightCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.weight"));
        arterialPressureCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.arterialpressure"));
        cardiacFrequencyCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.cardiacfrequency"));
        parametersPanelLine1.add(heightCheck);
        parametersPanelLine1.add(weightCheck);
        parametersPanelLine1.add(arterialPressureCheck);
        parametersPanelLine1.add(cardiacFrequencyCheck);

        temperatureCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.temperature"));
        saturationCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.saturation"));
        respiratoryRateCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.parameters.respiratoryrate"));
        parametersPanelLine2.add(temperatureCheck);
        parametersPanelLine2.add(saturationCheck);
        parametersPanelLine2.add(respiratoryRateCheck);

        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
        parametersPanel.add(parametersPanelLine1);
        parametersPanel.add(parametersPanelLine2);

        parametersPanel = setPanelBorder(parametersPanel, MessageBundle.getMessage("angal.stat.parameters"));

        return parametersPanel;
    }

    private JPanel getFilterButtonsPanel() {
        JPanel filterButtonPanel = new JPanel(new FlowLayout());
        filterButtonPanel.add(getFilterButton());
        filterButtonPanel.add(getFilterResetButton());
        return filterButtonPanel;
    }

    private JButton getFilterButton() {
        if (filterBtn != null) {
            return filterBtn;
        }

        filterBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.filter"));

        filterBtn.addActionListener(e -> {
            _start_index = 0;

            if (!checkAdmissionPeriod()) {
                return;
            }
            if (!checkAge()) {
                return;
            }
            if (!checkVaccinePeriod()) {
                return;
            }
            if (!checkExamPeriod()) {
                return;
            }
            checkParameters();

            runQuery(0);
            initializePaginationCombo();
        });

        return filterBtn;
    }

    private void runQuery(int pageIndex) {
        try {

            String riskLevel = getSelectedRiskLevel();
            String status = getSelectedStatus();
            Integer gravidityMin = getGravidityMin();
            Integer gravidityMax = getGravidityMax();
            Integer parityMin = getParityMin();
            Integer parityMax = getParityMax();
            Integer miscarriageMin = getMiscarriageMin();
            Integer miscarriageMax = getMiscarriageMax();
            Integer gestationalAgeMin = getGestationalAgeMin();
            Integer gestationalAgeMax = getGestationalAgeMax();

            String visitType = getSelectedVisitType();
            Integer visitCountMin = getVisitCountMin();
            Integer visitCountMax = getVisitCountMax();
            String maternalWeight = getSelectedMaternalWeight();
            String urineProtein = getSelectedUrineProtein();
            String edema = getSelectedEdema();
            String fetalPresentation = getSelectedFetalPresentation();
            String systolicBp = getSelectedSystolicBp();
            String diastolicBp = getSelectedDiastolicBp();

            Page<Patient> result = statsManager.getPregnanciesStats(
                    ageFrom, ageTo, periodFrom, periodTo,
                    _selectedExam, _selectedExamResult, examperiodFrom, examperiodTo,
                    _selectedVaccine, vaccineperiodFrom, vaccineperiodTo,
                    _selectedDisease, _selectedDischargeType,
                    _parameter_height_check, _parameter_weight_check, _parameter_art_press_check,
                    _parameter_card_freq_check, _parameter_temp_check, _parameter_saturation_check,
                    _parameter_resp_rate_check,
                    riskLevel, status,
                    gravidityMin, gravidityMax,
                    parityMin, parityMax,
                    miscarriageMin, miscarriageMax,
                    gestationalAgeMin, gestationalAgeMax,
                    visitType, visitCountMin, visitCountMax,
                    maternalWeight, urineProtein, edema,
                    fetalPresentation, systolicBp, diastolicBp,

                    pageIndex, _items_per_page
            );

            resultCount = (int) result.getTotalElements();
            patientList = new ArrayList<>(result.getContent());

            if (patientList.isEmpty() && resultCount == 0) {
                JOptionPane.showMessageDialog(EmptyStatisticsBrowser.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"), JOptionPane.PLAIN_MESSAGE);
            }

            jDataTableModel.fireTableDataChanged();
            jDataTable.updateUI();
            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        } catch (OHServiceException ex) {
            MessageDialog.showExceptions(ex);
        }
    }

    private JButton getFilterResetButton() {
        if (filterResetBtn != null) {
            visitTypeCombo.setSelectedIndex(0);
            visitCountCombo.setSelectedIndex(0);
            maternalWeightCombo.setSelectedIndex(0);
            urineProteinCombo.setSelectedIndex(0);
            edemaCombo.setSelectedIndex(0);
            fetalPresentationCombo.setSelectedIndex(0);
            systolicBpCombo.setSelectedIndex(0);
            diastolicBpCombo.setSelectedIndex(0);
            return filterResetBtn;
        }

        filterResetBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.resetfilter"));

        filterResetBtn.addActionListener(e -> {
            _start_index = 0;
            ageFrom = null;
            ageTo = null;
            _selectedExam = "";
            _selectedExamResult = "";
            _selectedVaccine = "";
            _selectedDisease = "";
            _selectedDischargeType = "";
            periodFrom = null;
            periodTo = null;
            examperiodFrom = null;
            examperiodTo = null;
            vaccineperiodFrom = null;
            vaccineperiodTo = null;
            _parameter_height_check = false;
            _parameter_weight_check = false;
            _parameter_art_press_check = false;
            _parameter_card_freq_check = false;
            _parameter_temp_check = false;
            _parameter_saturation_check = false;
            _parameter_resp_rate_check = false;

            riskLevelCombo.setSelectedIndex(0);
            statusCombo.setSelectedIndex(0);
            gravidityCombo.setSelectedIndex(0);
            parityCombo.setSelectedIndex(0);
            miscarriageCombo.setSelectedIndex(0);
            gestationalAgeCombo.setSelectedIndex(0);

            periodFromDateChooser.setDate(null);
            periodToDateChooser.setDate(null);
            ageFromField.setText("");
            ageToField.setText("");
            vaccinesCombo.setSelectedItem(null);
            vaccinePeriodFrom.setDate(null);
            vaccinePeriodTo.setDate(null);
            examsCombo.setSelectedItem(null);
            examResultsCombo.setSelectedItem(null);
            examPeriodFrom.setDate(null);
            examPeriodTo.setDate(null);
            diseasesCombo.setSelectedItem(null);
            dischargeTypesCombo.setSelectedItem(null);
            heightCheck.setSelected(false);
            weightCheck.setSelected(false);
            arterialPressureCheck.setSelected(false);
            cardiacFrequencyCheck.setSelected(false);
            temperatureCheck.setSelected(false);
            saturationCheck.setSelected(false);
            respiratoryRateCheck.setSelected(false);
        });

        return filterResetBtn;
    }

    private JButton getPaginationFirstButton() {
        if (paginationFirstBtn != null) {
            return paginationFirstBtn;
        }

        paginationFirstBtn = new JButton("<<");
        paginationFirstBtn.setEnabled(false);
        paginationFirstBtn.addActionListener(e -> {
            _start_index = 0;
            paginationCombo.setSelectedItem(1);
        });

        return paginationFirstBtn;
    }

    private JButton getPaginationPrevButton() {
        if (paginationPrevBtn != null) {
            return paginationPrevBtn;
        }

        paginationPrevBtn = new JButton("<");
        paginationPrevBtn.setEnabled(false);
        paginationPrevBtn.addActionListener(e -> {
            _start_index -= _items_per_page;
            int page = _start_index / _items_per_page + 1;
            paginationCombo.setSelectedItem(page);
        });
        return paginationPrevBtn;
    }

    private JComboBox<Integer> getPaginationCombo() {
        if (paginationCombo != null) {
            return paginationCombo;
        }

        paginationCombo = new JComboBox<>();

        paginationCombo.addActionListener(e -> {
            if (paginationCombo.getItemCount() > 0 && paginationCombo.getSelectedItem() != null) {
                int pageNumber = (Integer) paginationCombo.getSelectedItem();
                _start_index = (pageNumber - 1) * _items_per_page;
                runQuery(pageNumber - 1);
                setPaginationButtons();
            }
        });

        return paginationCombo;
    }

    private JButton getPaginationNextButton() {
        if (paginationNextBtn != null) {
            return paginationNextBtn;
        }

        paginationNextBtn = new JButton(">");
        paginationNextBtn.setEnabled(false);
        paginationNextBtn.addActionListener(e -> {
            _start_index += _items_per_page;
            int page = _start_index / _items_per_page + 1;
            paginationCombo.setSelectedItem(page);
        });

        return paginationNextBtn;
    }

    private JButton getPaginationLastButton() {
        if (paginationLastBtn != null) {
            return paginationLastBtn;
        }

        paginationLastBtn = new JButton(">>");
        paginationLastBtn.setEnabled(false);
        paginationLastBtn.addActionListener(e -> {
            int lastPage = paginationCombo.getItemCount();
            if (lastPage > 0) {
                paginationCombo.setSelectedItem(lastPage);
            }
        });

        return paginationLastBtn;
    }

    public void initializePaginationCombo() {
        int j = 0;
        paginationCombo.removeAllItems();

        for (int i = 0; i < resultCount / _items_per_page; i++) {
            j = i + 1;
            paginationCombo.addItem(j);
        }

        if (j * _items_per_page < resultCount) {
            paginationCombo.addItem(j + 1);
            paginationLabel.setText("/" + (resultCount / _items_per_page + 1) + " Pages");
        } else {
            paginationLabel.setText("/" + (j == 0 ? 0 : resultCount / _items_per_page) + " Pages");
        }

        setPaginationButtons();
    }

    private void setPaginationButtons() {
        if (_start_index + _items_per_page >= resultCount) {
            paginationNextBtn.setEnabled(false);
            paginationLastBtn.setEnabled(false);
        } else {
            paginationNextBtn.setEnabled(true);
            paginationLastBtn.setEnabled(true);
        }

        if (_start_index < _items_per_page) {
            paginationPrevBtn.setEnabled(false);
            paginationFirstBtn.setEnabled(false);
        } else {
            paginationPrevBtn.setEnabled(true);
            paginationFirstBtn.setEnabled(true);
        }
    }

    private JPanel getResultCountPanel() {
        JPanel resultCountPanel = new JPanel(new FlowLayout());
        resultCountLabel = new JLabel(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        resultCountPanel.add(resultCountLabel);
        return resultCountPanel;
    }

    private boolean checkAdmissionPeriod() {
        LocalDate dateFrom = periodFromDateChooser.getDate();
        LocalDate dateTo = periodToDateChooser.getDate();

        if (dateFrom == null && dateTo == null) {
            periodFrom = null;
            periodTo = null;
            return true;
        }

        if (dateFrom == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertadmissiondatefrom"));
            return false;
        }

        if (dateTo != null && dateTo.isBefore(dateFrom)) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidadmissionperiod"));
            return false;
        }

        periodFrom = dateFrom.atStartOfDay();
        periodTo = dateTo != null ? dateTo.atStartOfDay() : null;

        return true;
    }

    private boolean checkAge() {
        String fromText = ageFromField.getText().trim();
        String toText = ageToField.getText().trim();

        if (fromText.isEmpty() && toText.isEmpty()) {
            ageFrom = null;
            ageTo = null;
            return true;
        }

        try {
            ageFrom = fromText.isEmpty() ? null : Integer.valueOf(fromText);
            ageTo = toText.isEmpty() ? null : Integer.valueOf(toText);
        } catch (NumberFormatException nfe) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidagerange"));
            return false;
        }

        if (ageFrom != null && ageTo != null && ageFrom > ageTo) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidagerange"));
            return false;
        }

        return true;
    }

    private boolean checkVaccinePeriod() {
        LocalDate dateFrom = vaccinePeriodFrom.getDate();
        LocalDate dateTo = vaccinePeriodTo.getDate();

        if (dateFrom == null && dateTo == null) {
            vaccineperiodFrom = null;
            vaccineperiodTo = null;
            return true;
        }

        if (dateFrom == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvaccinedatefrom"));
            return false;
        }

        if (dateTo != null && dateTo.isBefore(dateFrom)) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidvaccineperiod"));
            return false;
        }

        vaccineperiodFrom = dateFrom.atStartOfDay();
        vaccineperiodTo = dateTo != null ? dateTo.atStartOfDay() : null;

        return true;
    }

    private boolean checkExamPeriod() {
        LocalDate dateFrom = examPeriodFrom.getDate();
        LocalDate dateTo = examPeriodTo.getDate();

        if (dateFrom == null && dateTo == null) {
            examperiodFrom = null;
            examperiodTo = null;
            return true;
        }

        if (dateFrom == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertexamdatefrom"));
            return false;
        }

        if (dateTo != null && dateTo.isBefore(dateFrom)) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidexamperiod"));
            return false;
        }

        examperiodFrom = dateFrom.atStartOfDay();
        examperiodTo = dateTo != null ? dateTo.atStartOfDay() : null;

        return true;
    }

    private void checkParameters() {
        _parameter_height_check = heightCheck.isSelected();
        _parameter_weight_check = weightCheck.isSelected();
        _parameter_art_press_check = arterialPressureCheck.isSelected();
        _parameter_card_freq_check = cardiacFrequencyCheck.isSelected();
        _parameter_temp_check = temperatureCheck.isSelected();
        _parameter_saturation_check = saturationCheck.isSelected();
        _parameter_resp_rate_check = respiratoryRateCheck.isSelected();
    }

    private JPanel setPanelBorder(JPanel panel, String panelTitle) {
        javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(panelTitle),
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.setBorder(b2);
        return panel;
    }

    private String getSelectedRiskLevel() {
        String selected = (String) riskLevelCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedStatus() {
        String selected = (String) statusCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private Integer getGravidityMin() {
        String selected = (String) gravidityCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.4plus"))) return 4;
        return Integer.parseInt(selected);
    }

    private Integer getGravidityMax() {
        String selected = (String) gravidityCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.4plus"))) return Integer.MAX_VALUE;
        return Integer.parseInt(selected);
    }

    private Integer getParityMin() {
        String selected = (String) parityCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.3plus"))) return 3;
        return Integer.parseInt(selected);
    }

    private Integer getParityMax() {
        String selected = (String) parityCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.3plus"))) return Integer.MAX_VALUE;
        return Integer.parseInt(selected);
    }

    private Integer getMiscarriageMin() {
        String selected = (String) miscarriageCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.3plus"))) return 3;
        return Integer.parseInt(selected);
    }

    private Integer getMiscarriageMax() {
        String selected = (String) miscarriageCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.3plus"))) return Integer.MAX_VALUE;
        return Integer.parseInt(selected);
    }

    private Integer getGestationalAgeMin() {
        String selected = (String) gestationalAgeCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.lt12"))) return 0;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.12to24"))) return 12;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.24to36"))) return 24;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.gt36"))) return 36;
        return null;
    }

    private Integer getGestationalAgeMax() {
        String selected = (String) gestationalAgeCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.lt12"))) return 11;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.12to24"))) return 24;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.24to36"))) return 36;
        if (selected.equals(MessageBundle.getMessage("angal.stat.gestage.gt36"))) return Integer.MAX_VALUE;
        return null;
    }

    private String getSelectedVisitType() {
        String selected = (String) visitTypeCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedMaternalWeight() {
        String selected = (String) maternalWeightCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedUrineProtein() {
        String selected = (String) urineProteinCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedEdema() {
        String selected = (String) edemaCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedFetalPresentation() {
        String selected = (String) fetalPresentationCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedSystolicBp() {
        String selected = (String) systolicBpCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedDiastolicBp() {
        String selected = (String) diastolicBpCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private Integer getVisitCountMin() {
        String selected = (String) visitCountCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.1"))) return 1;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.2"))) return 2;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.2to3"))) return 2;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.4to7"))) return 4;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.8plus"))) return 8;
        return null;
    }

    private Integer getVisitCountMax() {
        String selected = (String) visitCountCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) return null;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.1"))) return 1;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.2"))) return 2;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.2to3"))) return 3;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.4to7"))) return 7;
        if (selected.equals(MessageBundle.getMessage("angal.stat.count.8plus"))) return Integer.MAX_VALUE;
        return null;
    }

    private class StatsPregnancyBrowsingTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return patientList.size();
        }

        @Override
        public String getColumnName(int c) {
            return jDataTabelColumns[c];
        }

        @Override
        public int getColumnCount() {
            return jDataTabelColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return patientList.get(r).getName();
            } else if (c == 1) {
                return patientList.get(r).getAge();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    private class AccordionPanel extends JPanel {

        @Override
        public Dimension getPreferredSize() {
            Dimension header = headerPanel.getPreferredSize();
            if (!expanded) {
                return new Dimension(header.width, header.height);
            }
            return super.getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        private static final long serialVersionUID = 1L;
        private final JPanel headerPanel;
        private final JPanel contentPanel;
        private final JLabel toggleLabel;
        private boolean expanded;
        private List<AccordionPanel> siblings = new ArrayList<>();

        public AccordionPanel(String title, JPanel content, boolean defaultExpanded) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            this.expanded = defaultExpanded;

            headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(240, 240, 240));
            headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13));

            toggleLabel = new JLabel(expanded ? " ▼" : " ▶");
            toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.BOLD, 14));

            headerPanel.add(titleLabel, BorderLayout.CENTER);
            headerPanel.add(toggleLabel, BorderLayout.EAST);

            contentPanel = content;
            contentPanel.setVisible(expanded);

            add(headerPanel, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);

            headerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggle();
                }
            });
        }

        public void setSiblings(List<AccordionPanel> siblings) {
            this.siblings = siblings;
        }

        public void toggle() {
            expanded = !expanded;

            if (expanded) {
                add(contentPanel, BorderLayout.CENTER);
            } else {
                remove(contentPanel);
            }
            contentPanel.setVisible(expanded);
            toggleLabel.setText(expanded ? " ▼" : " ▶");

            revalidateFully();
        }

        private void revalidateFully() {
            Container top = this;
            while (top.getParent() != null) {
                top = top.getParent();
            }
            top.revalidate();
            top.repaint();
        }
    }
}