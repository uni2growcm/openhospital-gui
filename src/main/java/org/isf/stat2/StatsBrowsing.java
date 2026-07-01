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
package org.isf.stat2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.*;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.patient.model.Patient;
import org.isf.stat2.manager.StatsManager;
import org.isf.stat2.model.DiseaseStat;
import org.isf.stat2.model.ExamStat;
import org.isf.stat2.model.OperationStat;
import org.isf.stat2.model.VaccineStat;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class StatsBrowsing extends ModalJFrame {

    @Serial
    private static final long serialVersionUID = 3534376108035586486L;

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public enum ResultatType {
        REUSSIE,
        COMPLIQUEE,
        DECES,
        AUTRE
    }

    private JPanel filtersPanel;
    private JPanel dataPanel;
    private JPanel paginationPanel;

    private JPanel periodPanel;
    private JPanel ageAndSexPanel;
    private JPanel wardsPanel;
    private JPanel vaccinesPanel;
    private JPanel examsPanel;
    private JPanel operationsPanel;
    private JPanel diseasesAndDischargeTypesPanel;

    private JLabel periodFromLabel;
    private JLabel periodToLabel;
    private GoodDateChooser periodFromDateChooser;
    private GoodDateChooser periodToDateChooser;

    private JLabel sexLabel;
    private JComboBox<String> sexCombo;
    private JLabel ageFromLabel;
    private JLabel ageToLabel;
    private JTextField ageFromField;
    private JTextField ageToField;

    private JComboBox<Ward> wardsCombo;

    private JLabel examsLabel;
    private JLabel examResultsLabel;
    private JComboBox<String> examsCombo;
    private JComboBox<String> examResultsCombo;
    private GoodDateChooser examPeriodFrom;
    private GoodDateChooser examPeriodTo;
    private JCheckBox allExamsCheck;

    private JComboBox<String> vaccinesCombo;
    private GoodDateChooser vaccinePeriodFrom;
    private GoodDateChooser vaccinePeriodTo;
    private JCheckBox allVaccinesCheck;

    private JLabel operationsLabel;
    private JLabel operationResultsLabel;
    private JComboBox<String> operationsCombo;
    private JComboBox<String> operationResultsCombo;
    private GoodDateChooser operationPeriodFrom;
    private GoodDateChooser operationPeriodTo;
    private JCheckBox allOperationsCheck;

    private JLabel diseasesLabel;
    private JLabel dischargeTypesLabel;
    private JComboBox<Disease> diseasesCombo;
    private JComboBox<DischargeType> dischargeTypesCombo;
    private JCheckBox allDiseasesCheck;

    private JLabel resultCountLabel;
    private int resultCount = 0;
    private JButton filterBtn;
    private JButton filterResetBtn;
    private boolean isApplyFilter = false;

    private final String[] sexData = { "", "F", "M" };
    private List<Exam> examsData;
    private List<ExamRow> examResultsData;
    private List<Vaccine> vaccinesData;
    private List<Operation> operationsData;
    private List<Disease> diseasesData;
    private List<DischargeType> dischargeTypesData;
    private List<Ward> wardsData;
    private JTable jDataTable;

    private StatsBrowsingTableModel jDataTableModel;
    private VaccinesTableModel jVaccineTableModel;
    private ExamsTableModel jExamTableModel;
    private DiseasesTableModel jDiseaseTableModel;
    private OperationsTableModel jOperationTableModel;

    private List<Patient> patientList = new ArrayList<>();
    private List<VaccineStat> vaccinesList = new ArrayList<>();
    private List<ExamStat> examsList = new ArrayList<>();
    private List<DiseaseStat> diseasesList = new ArrayList<>();
    private List<OperationStat> operationsList = new ArrayList<>();

    private final String[] jDataTableColumns = {
            MessageBundle.getMessage("angal.patient.firstname1"),
            MessageBundle.getMessage("angal.patient.sex"),
            MessageBundle.getMessage("angal.report.labregister.age")
    };
    private final String[] vaccinesTableColumns = {
            MessageBundle.getMessage("angal.stat.vaccine"),
            MessageBundle.getMessage("angal.stat.men"),
            MessageBundle.getMessage("angal.stat.women")
    };
    private final String[] examsTableColumns = {
            MessageBundle.getMessage("angal.stat.exam"),
            MessageBundle.getMessage("angal.stat.men"),
            MessageBundle.getMessage("angal.stat.women")
    };
    private final String[] diseasesTableColumns = {
            MessageBundle.getMessage("angal.stat.disease"),
            MessageBundle.getMessage("angal.stat.men"),
            MessageBundle.getMessage("angal.stat.women")
    };
    private final String[] operationsTableColumns = {
            MessageBundle.getMessage("angal.stat.operation"),
            MessageBundle.getMessage("angal.stat.men"),
            MessageBundle.getMessage("angal.stat.women")
    };

    private final int[] jDataTableColumnWidth = { 200, 50, 50 };

    private int _start_index = 0;
    private static int _items_per_page = 100;
    private int _age_from = 0;
    private int _age_to = 0;
    private String _selectedSex = "";
    private String _selectedWard = "";
    private String _selectedExam = "";
    private String _selectedExamResult = "";
    private String _selectedVaccine = "";
    private String _selectedOperation = "";
    private String _selectedOperationResult = "";
    private String _selectedDisease = "";
    private String _selectedDischargeType = "";
    private String _period_from = "";
    private String _period_to = "";
    private String _exam_period_from = "";
    private String _exam_period_to = "";
    private String _vaccine_period_from = "";
    private String _vaccine_period_to = "";
    private String _operation_period_from = "";
    private String _operation_period_to = "";
    private String _selectedFilter = "general";

    private JButton paginationFirstBtn;
    private JButton paginationLastBtn;
    private JButton paginationPrevBtn;
    private JButton paginationNextBtn;
    private JComboBox<Integer> paginationCombo;
    private JLabel paginationLabel = new JLabel();

    private final VaccineBrowserManager vaccinesManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
    private final ExamBrowsingManager examsManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
    private final ExamRowBrowsingManager examResultsManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);
    private final OperationBrowserManager operationsManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
    private final DiseaseBrowserManager diseasesManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
    private final AdmissionBrowserManager admissionsManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
    private final WardBrowserManager wardsManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
    private final StatsManager statsManager = Context.getApplicationContext().getBean(StatsManager.class);

    public StatsBrowsing() {
        super();
        initialize();
    }

    private void loadDefaultData() {
        _start_index = 0;
        _period_from = LocalDate.now().minusDays(7).format(DATE_FORMATTER);
        _period_to = LocalDate.now().format(DATE_FORMATTER);
        filterBtn.doClick();
    }

    private void initialize() {
        setTitle(MessageBundle.getMessage("angal.stat.generalstatsbrowsing"));

        initComponents();
        pack();

        int frameMinWidth = getContentPane().getPreferredSize().width;
        int frameMinHeight = getContentPane().getPreferredSize().height;

        setMinimumSize(new Dimension(frameMinWidth + 30, frameMinHeight + 40));
        setLocationRelativeTo(null);

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (examsData != null) examsData.clear();
                if (vaccinesData != null) vaccinesData.clear();
                if (operationsData != null) operationsData.clear();
                if (diseasesData != null) diseasesData.clear();

                if (patientList != null) patientList.clear();
                if (vaccinesList != null) vaccinesList.clear();
                if (examsList != null) examsList.clear();
                if (diseasesList != null) diseasesList.clear();
                if (operationsList != null) operationsList.clear();

                dispose();
            }
        });
    }

    private void initComponents() {
        getContentPane().add(getFiltersPanel(), BorderLayout.WEST);
        getContentPane().add(getDataPanel(), BorderLayout.CENTER);
        getContentPane().add(getPaginationPanel(), BorderLayout.SOUTH);
    }

    private JPanel getFiltersPanel() {
        if (filtersPanel != null) return filtersPanel;

        filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

        filtersPanel.add(getPeriodPanel());
        filtersPanel.add(getAgeAndSexPanel());
        filtersPanel.add(getWardsPanel());
        filtersPanel.add(getVaccinesPanel());
        filtersPanel.add(getExamsPanel());
        filtersPanel.add(getOperationsPanel());
        filtersPanel.add(getDiseasesAndDischargeTypesPanel());
        filtersPanel.add(getFilterButtonsPanel());

        JPanel filtersNorthPanel = new JPanel(new BorderLayout());
        filtersNorthPanel.add(filtersPanel, BorderLayout.NORTH);

        return filtersNorthPanel;
    }

    private JPanel getDataPanel() {
        if (dataPanel != null) return dataPanel;

        dataPanel = new JPanel();
        JScrollPane tableScroll = getDataTableScrollPane();

        Dimension filtersPanelDimension = filtersPanel.getPreferredSize();
        filtersPanelDimension.width = tableScroll.getPreferredSize().width;
        filtersPanelDimension.height -= 40;
        tableScroll.setPreferredSize(filtersPanelDimension);

        dataPanel.add(tableScroll);
        return dataPanel;
    }

    private JScrollPane getDataTableScrollPane() {
        jDataTableModel = new StatsBrowsingTableModel();
        jVaccineTableModel = new VaccinesTableModel();
        jExamTableModel = new ExamsTableModel();
        jDiseaseTableModel = new DiseasesTableModel();
        jOperationTableModel = new OperationsTableModel();

        jDataTable = new JTable(jDataTableModel);

        jDataTable.getColumnModel().getColumn(0).setMinWidth(jDataTableColumnWidth[0]);
        jDataTable.getColumnModel().getColumn(1).setMinWidth(jDataTableColumnWidth[1]);
        jDataTable.getColumnModel().getColumn(2).setMinWidth(jDataTableColumnWidth[2]);

        JScrollPane tableScrollPane = new JScrollPane(jDataTable);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return tableScrollPane;
    }

    private JPanel getPaginationPanel() {
        if (paginationPanel != null) return paginationPanel;

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

    private JPanel getPeriodPanel() {
        if (periodPanel != null) return periodPanel;

        periodFromLabel = new JLabel(MessageBundle.getMessage("angal.common.dateFrom"));
        periodFromDateChooser = new GoodDateChooser(LocalDate.now().minusDays(7));
        periodToLabel = new JLabel(MessageBundle.getMessage("angal.common.dateTo"));
        periodToDateChooser = new GoodDateChooser(LocalDate.now());
        periodPanel = new JPanel(new FlowLayout());
        periodPanel.add(periodFromLabel);
        periodPanel.add(periodFromDateChooser);
        periodPanel.add(periodToLabel);
        periodPanel.add(periodToDateChooser);
        periodPanel = setPanelBorder(periodPanel, MessageBundle.getMessage("angal.stat.admissionperiod"));

        return periodPanel;
    }

    private JPanel getAgeAndSexPanel() {
        if (ageAndSexPanel != null) return ageAndSexPanel;

        sexLabel = new JLabel(MessageBundle.getMessage("angal.patient.sex"));
        sexCombo = new JComboBox<>(sexData);
        sexCombo.addActionListener(e -> {
            if (sexCombo.getSelectedItem() != null) {
                _selectedSex = sexCombo.getSelectedItem().toString();
            } else {
                _selectedSex = "";
            }
        });

        ageFromLabel = new JLabel(MessageBundle.getMessage("angal.report.billsreport.from"));
        ageFromField = new JTextField();
        ageFromField.setColumns(3);

        ageToLabel = new JLabel(MessageBundle.getMessage("angal.report.billsreport.to"));
        ageToField = new JTextField();
        ageToField.setColumns(3);

        ageAndSexPanel = new JPanel(new FlowLayout());
        ageAndSexPanel.add(sexLabel);
        ageAndSexPanel.add(sexCombo);
        ageAndSexPanel.add(ageFromLabel);
        ageAndSexPanel.add(ageFromField);
        ageAndSexPanel.add(ageToLabel);
        ageAndSexPanel.add(ageToField);
        ageAndSexPanel = setPanelBorder(ageAndSexPanel, MessageBundle.getMessage("angal.stat.sexandage"));

        return ageAndSexPanel;
    }

    private JPanel getWardsPanel() {
        if (wardsPanel != null) return wardsPanel;

        wardsCombo = new JComboBox<>();
        try {
            wardsData = wardsManager.getWards();
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            wardsData = new ArrayList<>();
        }

        wardsCombo.addItem(null);
        wardsCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return super.getListCellRendererComponent(list, MessageBundle.getMessage("angal.stat.allwards"), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        for (Ward ward : wardsData) {
            wardsCombo.addItem(ward);
        }

        wardsCombo.addActionListener(e -> {
            Object selected = wardsCombo.getSelectedItem();
            if (selected != null) {
                _selectedWard = selected.toString();
            } else {
                _selectedWard = "";
            }
        });

        wardsPanel = new JPanel(new FlowLayout());
        wardsPanel.add(wardsCombo);
        wardsPanel = setPanelBorder(wardsPanel, MessageBundle.getMessage("angal.stat.wards"));

        return wardsPanel;
    }

    private JPanel getVaccinesPanel() {
        if (vaccinesPanel != null) return vaccinesPanel;

        try {
            vaccinesData = vaccinesManager.getVaccine();
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            vaccinesData = new ArrayList<>();
        }

        vaccinesCombo = new JComboBox<>();
        vaccinesCombo.addItem(MessageBundle.getMessage("angal.stat.allvaccines"));

        for (Vaccine vaccine : vaccinesData) {
            vaccinesCombo.addItem(vaccine.getDescription());
        }

        vaccinesCombo.addActionListener(e -> {
            if (vaccinesCombo.getSelectedItem() != null) {
                _selectedVaccine = vaccinesCombo.getSelectedItem().toString();
            } else {
                _selectedVaccine = "";
            }
        });

        vaccinePeriodFrom = new GoodDateChooser(LocalDate.now().minusDays(7));
        vaccinePeriodTo = new GoodDateChooser(LocalDate.now());

        allVaccinesCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.allvaccinescheck"));
        allVaccinesCheck.addActionListener(e -> {
            if (allVaccinesCheck.isSelected()) {
                _selectedFilter = "vaccine";
                allDiseasesCheck.setEnabled(false);
                allExamsCheck.setEnabled(false);
                allOperationsCheck.setEnabled(false);
                sexCombo.setEnabled(false);
                vaccinesCombo.setEnabled(false);
            } else {
                _selectedFilter = "general";
                allDiseasesCheck.setEnabled(true);
                allExamsCheck.setEnabled(true);
                allOperationsCheck.setEnabled(true);
                sexCombo.setEnabled(true);
                vaccinesCombo.setEnabled(true);
            }
        });

        JPanel vaccinePeriodPanel = new JPanel(new FlowLayout());
        vaccinePeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodfrom")));
        vaccinePeriodPanel.add(vaccinePeriodFrom);
        vaccinePeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodto")));
        vaccinePeriodPanel.add(vaccinePeriodTo);
        vaccinePeriodPanel.add(allVaccinesCheck);

        vaccinesPanel = new JPanel();
        vaccinesPanel.setLayout(new BoxLayout(vaccinesPanel, BoxLayout.Y_AXIS));
        vaccinesPanel.add(vaccinesCombo);
        vaccinesPanel.add(vaccinePeriodPanel);
        vaccinesPanel = setPanelBorder(vaccinesPanel, MessageBundle.getMessage("angal.patvac.vaccine"));

        return vaccinesPanel;
    }

    private JPanel getExamsPanel() {
        if (examsPanel != null) return examsPanel;

        JPanel examsFlowPanel = new JPanel(new FlowLayout());
        examsLabel = new JLabel(MessageBundle.getMessage("angal.reduction.exam"));
        try {
            examsData = examsManager.getExams();
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            examsData = new ArrayList<>();
        }

        examsCombo = new JComboBox<>();
        examsCombo.addItem(MessageBundle.getMessage("angal.stat.allexams"));
        for (Exam exam : examsData) {
            examsCombo.addItem(exam.getDescription());
        }

        examsFlowPanel.add(examsLabel);
        examsFlowPanel.add(examsCombo);

        JPanel examResultsFlowPanel = new JPanel(new FlowLayout());
        examResultsLabel = new JLabel(MessageBundle.getMessage("angal.stat.operationresult"));
        examResultsCombo = new JComboBox<>();
        examResultsFlowPanel.add(examResultsLabel);
        examResultsFlowPanel.add(examResultsCombo);

        examsCombo.addActionListener(e -> {
            if (examsCombo.getSelectedItem() != null) {
                _selectedExam = examsCombo.getSelectedItem().toString();
            } else {
                _selectedExam = "";
            }

            if (_selectedExam != null && !_selectedExam.isEmpty()) {
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
                    } else {
                        examResultsData = new ArrayList<>();
                    }
                } catch (OHServiceException ex) {
                    OHServiceExceptionUtil.showMessages(ex);
                    examResultsData = new ArrayList<>();
                }

                examResultsCombo.removeAllItems();
                examResultsCombo.addItem("");
                for (ExamRow examRow : examResultsData) {
                    examResultsCombo.addItem(examRow.getDescription());
                }
            } else {
                examResultsCombo.removeAllItems();
            }
        });

        examResultsCombo.addActionListener(e -> {
            if (examResultsCombo.getSelectedItem() != null) {
                _selectedExamResult = examResultsCombo.getSelectedItem().toString();
            } else {
                _selectedExamResult = "";
            }
        });

        examPeriodFrom = new GoodDateChooser(LocalDate.now().minusDays(7));
        examPeriodTo = new GoodDateChooser(LocalDate.now());

        allExamsCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.allexamscheck"));
        allExamsCheck.addActionListener(e -> {
            if (allExamsCheck.isSelected()) {
                _selectedFilter = "exam";
                allVaccinesCheck.setEnabled(false);
                allDiseasesCheck.setEnabled(false);
                allOperationsCheck.setEnabled(false);
                sexCombo.setEnabled(false);
                examsCombo.setEnabled(false);
            } else {
                _selectedFilter = "general";
                allVaccinesCheck.setEnabled(true);
                allDiseasesCheck.setEnabled(true);
                allOperationsCheck.setEnabled(true);
                sexCombo.setEnabled(true);
                examsCombo.setEnabled(true);
            }
        });

        JPanel examPeriodPanel = new JPanel(new FlowLayout());
        examPeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodfrom")));
        examPeriodPanel.add(examPeriodFrom);
        examPeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodto")));
        examPeriodPanel.add(examPeriodTo);
        examPeriodPanel.add(allExamsCheck);

        examsPanel = new JPanel();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        examsPanel.add(examsFlowPanel);
        examsPanel.add(examResultsFlowPanel);
        examsPanel.add(examPeriodPanel);
        examsPanel = setPanelBorder(examsPanel, MessageBundle.getMessage("angal.stat.examsandresults"));

        return examsPanel;
    }

    private JPanel getOperationsPanel() {
        if (operationsPanel != null) return operationsPanel;

        JPanel operationsFlowPanel = new JPanel(new FlowLayout());
        operationsLabel = new JLabel(MessageBundle.getMessage("angal.stat.operation"));
        operationsCombo = new JComboBox<>();
        operationsCombo.addItem(MessageBundle.getMessage("angal.stat.alloperations"));

        try {
            operationsData = operationsManager.getOperation();
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            operationsData = new ArrayList<>();
        }

        for (Operation operation : operationsData) {
            operationsCombo.addItem(operation.getDescription());
        }

        operationsFlowPanel.add(operationsLabel);
        operationsFlowPanel.add(operationsCombo);

        JPanel operationResultsFlowPanel = new JPanel(new FlowLayout());
        operationResultsLabel = new JLabel(MessageBundle.getMessage("angal.stat.operationresult"));
        operationResultsCombo = new JComboBox<>();
        operationResultsCombo.addItem(MessageBundle.getMessage("angal.stat.allresults"));
        for (ResultatType result : ResultatType.values()) {
            operationResultsCombo.addItem(result.toString());
        }

        operationResultsFlowPanel.add(operationResultsLabel);
        operationResultsFlowPanel.add(operationResultsCombo);

        operationResultsCombo.addActionListener(e -> {
            Object selected = operationResultsCombo.getSelectedItem();
            if (selected != null && !selected.equals(MessageBundle.getMessage("angal.stat.allresults"))) {
                _selectedOperationResult = selected.toString();
            } else {
                _selectedOperationResult = "";
            }
        });

        operationResultsCombo.addActionListener(e -> {
            if (operationResultsCombo.getSelectedItem() != null) {
                _selectedOperationResult = operationResultsCombo.getSelectedItem().toString();
            } else {
                _selectedOperationResult = "";
            }
        });

        operationPeriodFrom = new GoodDateChooser(LocalDate.now().minusDays(7));
        operationPeriodTo = new GoodDateChooser(LocalDate.now());

        allOperationsCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.alloperationscheck"));
        allOperationsCheck.addActionListener(e -> {
            if (allOperationsCheck.isSelected()) {
                _selectedFilter = "operation";
                allVaccinesCheck.setEnabled(false);
                allDiseasesCheck.setEnabled(false);
                allExamsCheck.setEnabled(false);
                sexCombo.setEnabled(false);
                operationsCombo.setEnabled(false);
            } else {
                _selectedFilter = "general";
                allVaccinesCheck.setEnabled(true);
                allDiseasesCheck.setEnabled(true);
                allExamsCheck.setEnabled(true);
                sexCombo.setEnabled(true);
                operationsCombo.setEnabled(true);
            }
        });

        JPanel operationPeriodPanel = new JPanel(new FlowLayout());
        operationPeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodfrom")));
        operationPeriodPanel.add(operationPeriodFrom);
        operationPeriodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.periodto")));
        operationPeriodPanel.add(operationPeriodTo);
        operationPeriodPanel.add(allOperationsCheck);

        operationsPanel = new JPanel();
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
        operationsPanel.add(operationsFlowPanel);
        operationsPanel.add(operationResultsFlowPanel);
        operationsPanel.add(operationPeriodPanel);
        operationsPanel = setPanelBorder(operationsPanel, MessageBundle.getMessage("angal.stat.operations"));

        return operationsPanel;
    }

    private JPanel getDiseasesAndDischargeTypesPanel() {
        if (diseasesAndDischargeTypesPanel != null) return diseasesAndDischargeTypesPanel;

        JPanel diseasesPanel = new JPanel(new FlowLayout());
        diseasesLabel = new JLabel(MessageBundle.getMessage("angal.stat.disease"));
        diseasesCombo = new JComboBox<>();
        try {
            diseasesData = diseasesManager.getDiseaseAll();
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            diseasesData = new ArrayList<>();
        }
        allDiseasesCheck = new JCheckBox(MessageBundle.getMessage("angal.stat.alldiseasescheck"));

        diseasesCombo.addItem(null);
        diseasesCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return super.getListCellRendererComponent(list, MessageBundle.getMessage("angal.stat.alldiseases"), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        for (Disease disease : diseasesData) {
            diseasesCombo.addItem(disease);
        }
        diseasesPanel.add(diseasesLabel);
        diseasesPanel.add(diseasesCombo);

        JPanel dischargeTypesPanel = new JPanel(new FlowLayout());
        dischargeTypesLabel = new JLabel(MessageBundle.getMessage("angal.stat.dischargetype"));
        dischargeTypesCombo = new JComboBox<>();
        try {
            dischargeTypesData = admissionsManager.getDischargeType();
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            dischargeTypesData = new ArrayList<>();
        }

        dischargeTypesCombo.addItem(null);
        dischargeTypesCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    return super.getListCellRendererComponent(list, MessageBundle.getMessage("angal.stat.alldischargetypes"), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        for (DischargeType dischargeType : dischargeTypesData) {
            dischargeTypesCombo.addItem(dischargeType);
        }
        dischargeTypesPanel.add(dischargeTypesLabel);
        dischargeTypesPanel.add(dischargeTypesCombo);
        dischargeTypesPanel.add(allDiseasesCheck);

        diseasesCombo.addActionListener(e -> {
            Object selected = diseasesCombo.getSelectedItem();
            if (selected != null) {
                _selectedDisease = selected.toString();
            } else {
                _selectedDisease = "";
            }
        });

        dischargeTypesCombo.addActionListener(e -> {
            Object selected = dischargeTypesCombo.getSelectedItem();
            if (selected != null) {
                _selectedDischargeType = selected.toString();
            } else {
                _selectedDischargeType = "";
            }
        });

        allDiseasesCheck.addActionListener(e -> {
            if (allDiseasesCheck.isSelected()) {
                _selectedFilter = "disease";
                allVaccinesCheck.setEnabled(false);
                allExamsCheck.setEnabled(false);
                allOperationsCheck.setEnabled(false);
                diseasesCombo.setEnabled(false);
                sexCombo.setEnabled(false);
            } else {
                _selectedFilter = "general";
                allVaccinesCheck.setEnabled(true);
                allExamsCheck.setEnabled(true);
                allOperationsCheck.setEnabled(true);
                diseasesCombo.setEnabled(true);
                sexCombo.setEnabled(true);
            }
        });

        diseasesAndDischargeTypesPanel = new JPanel();
        diseasesAndDischargeTypesPanel.setLayout(new BoxLayout(diseasesAndDischargeTypesPanel, BoxLayout.Y_AXIS));
        diseasesAndDischargeTypesPanel.add(diseasesPanel);
        diseasesAndDischargeTypesPanel.add(dischargeTypesPanel);
        diseasesAndDischargeTypesPanel = setPanelBorder(diseasesAndDischargeTypesPanel,
                MessageBundle.getMessage("angal.stat.diseasesanddischargetypes"));

        return diseasesAndDischargeTypesPanel;
    }

    private JPanel getFilterButtonsPanel() {
        JPanel filterButtonPanel = new JPanel(new FlowLayout());
        filterButtonPanel.add(getFilterButton());
        filterButtonPanel.add(getFilterResetButton());
        return filterButtonPanel;
    }

    private JButton getFilterButton() {
        if (filterBtn != null) return filterBtn;

        filterBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.filter"));
        filterBtn.addActionListener(e -> {
            _start_index = 0;

            if (!checkAdmissionPeriod()) return;
            if (!checkAge()) return;
            if (!checkVaccinePeriod()) return;
            if (!checkExamPeriod()) return;
            if (!checkOperationPeriod()) return;
            try {
                switch (_selectedFilter) {
                    case "exam":
                        resultCount = statsManager.getExamsStatsCount(
                                _age_from, _age_to, _period_from, _period_to,
                                _selectedWard, _selectedExamResult,
                                _exam_period_from, _exam_period_to,
                                _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                                _selectedOperation, _selectedOperationResult,
                                _operation_period_from, _operation_period_to,
                                _selectedDisease, _selectedDischargeType);
                        break;
                    case "vaccine":
                        resultCount = statsManager.getVaccinesStatsCount(
                                _age_from, _age_to, _period_from, _period_to,
                                _selectedWard,
                                _selectedExam, _selectedExamResult,
                                _exam_period_from, _exam_period_to,
                                _vaccine_period_from, _vaccine_period_to,
                                _selectedOperation, _selectedOperationResult,
                                _operation_period_from, _operation_period_to,
                                _selectedDisease, _selectedDischargeType);
                        break;
                    case "disease":
                        resultCount = statsManager.getDiseasesStatsCount(
                                _age_from, _age_to, _period_from, _period_to,
                                _selectedWard,
                                _selectedExam, _selectedExamResult,
                                _exam_period_from, _exam_period_to,
                                _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                                _selectedOperation, _selectedOperationResult,
                                _operation_period_from, _operation_period_to,
                                _selectedDischargeType);
                        break;
                    case "operation":
                        resultCount = statsManager.getOperationsStatsCount(
                                _age_from, _age_to, _period_from, _period_to,
                                _selectedWard,
                                _selectedExam, _selectedExamResult,
                                _exam_period_from, _exam_period_to,
                                _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                                _selectedOperationResult,
                                _operation_period_from, _operation_period_to,
                                _selectedDisease, _selectedDischargeType);
                        break;
                    default:
                        resultCount = statsManager.getPatientsStatsCount(
                                _age_from, _age_to, _period_from, _period_to,
                                _selectedSex, _selectedWard,
                                _selectedExam, _selectedExamResult,
                                _exam_period_from, _exam_period_to,
                                _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                                _selectedOperation, _selectedOperationResult,
                                _operation_period_from, _operation_period_to,
                                _selectedDisease, _selectedDischargeType);
                        break;
                }
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }

            if (resultCount == 0) {
                emptyJDataTable();
                resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
            }

            applyFilters();
            initializePaginationCombo();
        });

        return filterBtn;
    }

    private JButton getFilterResetButton() {
        if (filterResetBtn != null) return filterResetBtn;

        filterResetBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.resetfilter"));
        filterResetBtn.addActionListener(e -> {
            _start_index = 0;
            _items_per_page = 100;
            _selectedFilter = "general";
            _age_from = 0;
            _age_to = 0;
            _selectedSex = "";
            _selectedWard = "";
            _selectedExam = "";
            _selectedExamResult = "";
            _selectedVaccine = "";
            _selectedOperation = "";
            _selectedOperationResult = "";
            _selectedDisease = "";
            _selectedDischargeType = "";
            _period_from = "";
            _period_to = "";
            _exam_period_from = "";
            _exam_period_to = "";
            _vaccine_period_from = "";
            _vaccine_period_to = "";
            _operation_period_from = "";
            _operation_period_to = "";

            periodFromDateChooser.setDate(null);
            periodToDateChooser.setDate(null);
            sexCombo.setSelectedItem(null);
            ageFromField.setText("");
            ageToField.setText("");
            vaccinesCombo.setSelectedItem(null);
            wardsCombo.setSelectedItem(null);
            vaccinePeriodFrom.setDate(null);
            vaccinePeriodTo.setDate(null);
            examsCombo.setSelectedItem(null);
            examResultsCombo.setSelectedItem(null);
            examPeriodFrom.setDate(null);
            examPeriodTo.setDate(null);
            operationsCombo.setSelectedItem(null);
            operationResultsCombo.setSelectedItem(null);
            operationPeriodFrom.setDate(null);
            operationPeriodTo.setDate(null);
            diseasesCombo.setSelectedItem(null);
            dischargeTypesCombo.setSelectedItem(null);
            allDiseasesCheck.setSelected(false);
            allDiseasesCheck.setEnabled(true);
            allExamsCheck.setSelected(false);
            allExamsCheck.setEnabled(true);
            allVaccinesCheck.setSelected(false);
            allVaccinesCheck.setEnabled(true);
            allOperationsCheck.setSelected(false);
            allOperationsCheck.setEnabled(true);
            sexCombo.setEnabled(true);
            diseasesCombo.setEnabled(true);
            vaccinesCombo.setEnabled(true);
            examsCombo.setEnabled(true);
            operationsCombo.setEnabled(true);

            emptyJDataTable();
            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : 0");
            initializePaginationCombo();

            System.out.println("Filter reset");
        });

        return filterResetBtn;
    }

    private void applyFilters() {
        emptyJDataTable();

        switch (_selectedFilter) {
            case "vaccine":
                vaccineFilter();
                break;
            case "exam":
                examFilter();
                break;
            case "disease":
                diseaseFilter();
                break;
            case "operation":
                operationFilter();
                break;
            default:
                generalFilter();
                break;
        }
        isApplyFilter = true;
    }

    private void generalFilter() {
        try {
            if (resultCount > 0) {
                patientList = statsManager.getPatientsStats(
                        _start_index, _start_index + _items_per_page,
                        _age_from, _age_to,
                        _period_from, _period_to,
                        _selectedSex, _selectedWard,
                        _selectedExam, _selectedExamResult,
                        _exam_period_from, _exam_period_to,
                        _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                        _selectedOperation, _selectedOperationResult,
                        _operation_period_from, _operation_period_to,
                        _selectedDisease, _selectedDischargeType);
            } else {
                patientList = new ArrayList<>();
            }

            if (!patientList.isEmpty()) {
                jDataTableModel.fireTableDataChanged();
                jDataTable.setModel(jDataTableModel);
                jDataTable.updateUI();
            } else {
                JOptionPane.showMessageDialog(StatsBrowsing.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"),
                        JOptionPane.PLAIN_MESSAGE);
            }

            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void vaccineFilter() {
        try {
            if (resultCount > 0) {
                vaccinesList = statsManager.getVaccinesStats(
                        _start_index, _start_index + _items_per_page,
                        _age_from, _age_to,
                        _period_from, _period_to,
                        _selectedWard,
                        _selectedExam, _selectedExamResult,
                        _exam_period_from, _exam_period_to,
                        _vaccine_period_from, _vaccine_period_to,
                        _selectedOperation, _selectedOperationResult,
                        _operation_period_from, _operation_period_to,
                        _selectedDisease, _selectedDischargeType);
            } else {
                vaccinesList = new ArrayList<>();
            }

            if (!vaccinesList.isEmpty()) {
                jVaccineTableModel.fireTableDataChanged();
                jDataTable.setModel(jVaccineTableModel);
                jDataTable.updateUI();
            } else {
                JOptionPane.showMessageDialog(StatsBrowsing.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"),
                        JOptionPane.PLAIN_MESSAGE);
            }

            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void examFilter() {
        try {
            if (resultCount > 0) {
                examsList = statsManager.getExamsStats(
                        _start_index, _start_index + _items_per_page,
                        _age_from, _age_to,
                        _period_from, _period_to,
                        _selectedWard,
                        _selectedExamResult,
                        _exam_period_from, _exam_period_to,
                        _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                        _selectedOperation, _selectedOperationResult,
                        _operation_period_from, _operation_period_to,
                        _selectedDisease, _selectedDischargeType);
            } else {
                examsList = new ArrayList<>();
            }

            if (!examsList.isEmpty()) {
                jExamTableModel.fireTableDataChanged();
                jDataTable.setModel(jExamTableModel);
                jDataTable.updateUI();
            } else {
                JOptionPane.showMessageDialog(StatsBrowsing.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"),
                        JOptionPane.PLAIN_MESSAGE);
            }

            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void diseaseFilter() {
        try {
            if (resultCount > 0) {
                diseasesList = statsManager.getDiseasesStats(
                        _start_index, _start_index + _items_per_page,
                        _age_from, _age_to,
                        _period_from, _period_to,
                        _selectedWard,
                        _selectedExam, _selectedExamResult,
                        _exam_period_from, _exam_period_to,
                        _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                        _selectedOperation, _selectedOperationResult,
                        _operation_period_from, _operation_period_to,
                        _selectedDischargeType);
            } else {
                diseasesList = new ArrayList<>();
            }

            if (!diseasesList.isEmpty()) {
                jDiseaseTableModel.fireTableDataChanged();
                jDataTable.setModel(jDiseaseTableModel);
                jDataTable.updateUI();
            } else {
                JOptionPane.showMessageDialog(StatsBrowsing.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"),
                        JOptionPane.PLAIN_MESSAGE);
            }

            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void operationFilter() {
        try {
            if (resultCount > 0) {
                operationsList = statsManager.getOperationsStats(
                        _start_index, _start_index + _items_per_page,
                        _age_from, _age_to,
                        _period_from, _period_to,
                        _selectedWard,
                        _selectedExam, _selectedExamResult,
                        _exam_period_from, _exam_period_to,
                        _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                        _selectedOperationResult,
                        _operation_period_from, _operation_period_to,
                        _selectedDisease, _selectedDischargeType);
            } else {
                operationsList = new ArrayList<>();
            }

            if (!operationsList.isEmpty()) {
                jOperationTableModel.fireTableDataChanged();
                jDataTable.setModel(jOperationTableModel);
                jDataTable.updateUI();
            } else {
                JOptionPane.showMessageDialog(StatsBrowsing.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"),
                        JOptionPane.PLAIN_MESSAGE);
            }

            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private JButton getPaginationFirstButton() {
        if (paginationFirstBtn != null) return paginationFirstBtn;

        paginationFirstBtn = new JButton("<<");
        paginationFirstBtn.setEnabled(false);
        paginationFirstBtn.addActionListener(e -> {
            _start_index = 0;
            setPaginationButtons();
            paginationCombo.setSelectedItem(_start_index / _items_per_page + 1);
            applyFilters();
        });

        return paginationFirstBtn;
    }

    private JButton getPaginationPrevButton() {
        if (paginationPrevBtn != null) return paginationPrevBtn;

        paginationPrevBtn = new JButton("<");
        paginationPrevBtn.setEnabled(false);
        paginationPrevBtn.addActionListener(e -> {
            _start_index -= _items_per_page;
            setPaginationButtons();
            paginationCombo.setSelectedItem(_start_index / _items_per_page + 1);
            applyFilters();
        });

        return paginationPrevBtn;
    }

    private JComboBox<Integer> getPaginationCombo() {
        if (paginationCombo != null) return paginationCombo;

        paginationCombo = new JComboBox<>();
        paginationCombo.addActionListener(e -> {
            if (paginationCombo.getItemCount() > 0) {
                Integer pageNumber = (Integer) paginationCombo.getSelectedItem();
                if (pageNumber != null) {
                    _start_index = (pageNumber - 1) * _items_per_page;
                    applyFilters();
                    setPaginationButtons();
                }
            }
        });

        return paginationCombo;
    }

    private JButton getPaginationNextButton() {
        if (paginationNextBtn != null) return paginationNextBtn;

        paginationNextBtn = new JButton(">");
        paginationNextBtn.setEnabled(false);
        paginationNextBtn.addActionListener(e -> {
            _start_index += _items_per_page;
            setPaginationButtons();
            paginationCombo.setSelectedItem(_start_index / _items_per_page + 1);
            applyFilters();
        });

        return paginationNextBtn;
    }

    private JButton getPaginationLastButton() {
        if (paginationLastBtn != null) return paginationLastBtn;

        paginationLastBtn = new JButton(">>");
        paginationLastBtn.setEnabled(false);
        paginationLastBtn.addActionListener(e -> {
        });

        return paginationLastBtn;
    }

    public void initializePaginationCombo() {
        paginationCombo.removeAllItems();
        int totalPages = resultCount / _items_per_page;
        for (int i = 0; i < totalPages; i++) {
            paginationCombo.addItem(i + 1);
        }
        if (totalPages * _items_per_page < resultCount) {
            paginationCombo.addItem(totalPages + 1);
            paginationLabel.setText("/" + (resultCount / _items_per_page + 1 + " Pages"));
        } else if (isApplyFilter) {
            paginationLabel.setText("/" + (resultCount / _items_per_page + 1 + " Pages"));
        } else {
            paginationLabel.setText("/" + resultCount / _items_per_page + " Pages");
        }
        setPaginationButtons();
    }

    private void setPaginationButtons() {
        if (_start_index + _items_per_page > resultCount) {
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
            return true;
        }

        if (dateFrom == null) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertadmissiondatefrom");
            return false;
        }

        if (dateTo != null && dateTo.isBefore(dateFrom)) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertvalidadmissionperiod");
            return false;
        }

        if (dateFrom != null) {
            _period_from = dateFrom.format(DATE_FORMATTER);
        }
        if (dateTo != null) {
            _period_to = dateTo.format(DATE_FORMATTER);
        }

        return true;
    }

    private boolean checkAge() {
        if (ageFromField.getText().isEmpty() && ageToField.getText().isEmpty()) {
            return true;
        }

        if (!ageFromField.getText().isEmpty()) {
            _age_from = Integer.parseInt(ageFromField.getText());
        }

        if (!ageToField.getText().isEmpty()) {
            _age_to = Integer.parseInt(ageToField.getText());
        }

        if (_age_to > 0 && _age_from > _age_to) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertvalidagerange");
            return false;
        }

        return true;
    }

    private boolean checkVaccinePeriod() {
        LocalDate vaccineDateFrom = vaccinePeriodFrom.getDate();
        LocalDate vaccineDateTo = vaccinePeriodTo.getDate();

        if (vaccineDateFrom == null && vaccineDateTo == null) {
            return true;
        }

        if (vaccineDateFrom == null) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertvaccinedatefrom");
            return false;
        }

        if (vaccineDateTo != null && vaccineDateTo.isBefore(vaccineDateFrom)) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertvalidvaccineperiod");
            return false;
        }

        if (vaccineDateFrom != null) {
            _vaccine_period_from = vaccineDateFrom.format(DATE_FORMATTER);
        }
        if (vaccineDateTo != null) {
            _vaccine_period_to = vaccineDateTo.format(DATE_FORMATTER);
        }

        return true;
    }

    private boolean checkExamPeriod() {
        LocalDate examDateFrom = examPeriodFrom.getDate();
        LocalDate examDateTo = examPeriodTo.getDate();

        if (examDateFrom == null && examDateTo == null) {
            return true;
        }

        if (examDateFrom == null) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertexamdatefrom");
            return false;
        }

        if (examDateTo != null && examDateTo.isBefore(examDateFrom)) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertvalidexamperiod");
            return false;
        }

        if (examDateFrom != null) {
            _exam_period_from = examDateFrom.format(DATE_FORMATTER);
        }
        if (examDateTo != null) {
            _exam_period_to = examDateTo.format(DATE_FORMATTER);
        }

        return true;
    }

    private boolean checkOperationPeriod() {
        LocalDate operationDateFrom = operationPeriodFrom.getDate();
        LocalDate operationDateTo = operationPeriodTo.getDate();

        if (operationDateFrom == null && operationDateTo == null) {
            return true;
        }

        if (operationDateFrom == null) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertoperationdatefrom");
            return false;
        }

        if (operationDateTo != null && operationDateTo.isBefore(operationDateFrom)) {
            MessageDialog.error(StatsBrowsing.this, "angal.stat.error.pleaseinsertvalidoperationperiod");
            return false;
        }

        if (operationDateFrom != null) {
            _operation_period_from = operationDateFrom.format(DATE_FORMATTER);
        }
        if (operationDateTo != null) {
            _operation_period_to = operationDateTo.format(DATE_FORMATTER);
        }

        return true;
    }

    private JPanel setPanelBorder(JPanel panel, String panelTitle) {
        javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(panelTitle),
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.setBorder(b2);
        return panel;
    }

    private void emptyJDataTable() {
        patientList = new ArrayList<>();
        vaccinesList = new ArrayList<>();
        examsList = new ArrayList<>();
        operationsList = new ArrayList<>();
        diseasesList = new ArrayList<>();
        jDataTableModel.fireTableDataChanged();
        jDataTable.updateUI();
    }

    class StatsBrowsingTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return patientList.size();
        }

        @Override
        public String getColumnName(int c) {
            return jDataTableColumns[c];
        }

        @Override
        public int getColumnCount() {
            return jDataTableColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return patientList.get(r).getName();
            } else if (c == 1) {
                return patientList.get(r).getSex();
            } else if (c == 2) {
                return patientList.get(r).getAge();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    class VaccinesTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return vaccinesList.size();
        }

        @Override
        public String getColumnName(int c) {
            return vaccinesTableColumns[c];
        }

        @Override
        public int getColumnCount() {
            return vaccinesTableColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return vaccinesList.get(r).getVaccine().getDescription();
            } else if (c == 1) {
                return vaccinesList.get(r).getMenNumber();
            } else if (c == 2) {
                return vaccinesList.get(r).getWomenNumber();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    class ExamsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return examsList.size();
        }

        @Override
        public String getColumnName(int c) {
            return examsTableColumns[c];
        }

        @Override
        public int getColumnCount() {
            return examsTableColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return examsList.get(r).getExam().getDescription();
            } else if (c == 1) {
                return examsList.get(r).getMenNumber();
            } else if (c == 2) {
                return examsList.get(r).getWomenNumber();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    class DiseasesTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return diseasesList.size();
        }

        @Override
        public String getColumnName(int c) {
            return diseasesTableColumns[c];
        }

        @Override
        public int getColumnCount() {
            return diseasesTableColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return diseasesList.get(r).getDisease().getDescription();
            } else if (c == 1) {
                return diseasesList.get(r).getMenNumber();
            } else if (c == 2) {
                return diseasesList.get(r).getWomenNumber();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    class OperationsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return operationsList.size();
        }

        @Override
        public String getColumnName(int c) {
            return operationsTableColumns[c];
        }

        @Override
        public int getColumnCount() {
            return operationsTableColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return operationsList.get(r).getOperation().getDescription();
            } else if (c == 1) {
                return operationsList.get(r).getMenNumber();
            } else if (c == 2) {
                return operationsList.get(r).getWomenNumber();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }
}