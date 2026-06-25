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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
import org.isf.patient.model.Patient;
import org.isf.stat2.manager.StatsBrowserManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.springframework.data.domain.Page;

import com.toedter.calendar.JDateChooser;

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

    private JDateChooser periodFromDateChooser;
    private JDateChooser periodToDateChooser;
    private JTextField ageFromField;
    private JTextField ageToField;

    private JComboBox<String> examsCombo;
    private JComboBox<String> examResultsCombo;
    private JDateChooser examPeriodFrom;
    private JDateChooser examPeriodTo;

    private JComboBox<String> vaccinesCombo;
    private JDateChooser vaccinePeriodFrom;
    private JDateChooser vaccinePeriodTo;

    private JComboBox<Disease> diseasesCombo;
    private JComboBox<DischargeType> dischargeTypesCombo;

    private JCheckBox heightCheck;
    private JCheckBox weightCheck;
    private JCheckBox arterialPressureCheck;
    private JCheckBox cardiacFrequencyCheck;
    private JCheckBox temperatureCheck;
    private JCheckBox saturationCheck;
    private JCheckBox respiratoryRateCheck;

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
    private Integer _age_from = null;
    private Integer _age_to = null;
    private String _selectedExam = "";
    private String _selectedExamResult = "";
    private String _selectedVaccine = "";
    private String _selectedDisease = "";
    private String _selectedDischargeType = "";
    private LocalDateTime _period_from = null;
    private LocalDateTime _period_to = null;
    private LocalDateTime _exam_period_from = null;
    private LocalDateTime _exam_period_to = null;
    private LocalDateTime _vaccine_period_from = null;
    private LocalDateTime _vaccine_period_to = null;
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

        int frameMinWidth = getContentPane().getPreferredSize().width;
        int frameMinHeight = getContentPane().getPreferredSize().height;
        setMinimumSize(new Dimension(frameMinWidth + 30, frameMinHeight + 60));

        setResizable(true);
        setExtendedState(JFrame.NORMAL);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        getContentPane().add(getFiltersPanel(), BorderLayout.WEST);
        getContentPane().add(getDataPanel(), BorderLayout.CENTER);
        getContentPane().add(getPaginationPanel(), BorderLayout.SOUTH);
    }

    private JPanel getFiltersPanel() {
        if (filtersPanel != null) {
            return filtersPanel;
        }

        filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

        filtersPanel.add(getPeriodAndAgePanel());
        filtersPanel.add(getVaccinesPanel());
        filtersPanel.add(getExamsPanel());
        filtersPanel.add(getDiseasesAndDischargeTypesPanel());
        filtersPanel.add(getParametersPanel());
        filtersPanel.add(getFilterButtonsPanel());

        JPanel filtersNorthPanel = new JPanel(new BorderLayout());
        filtersNorthPanel.add(filtersPanel, BorderLayout.NORTH);

        return filtersNorthPanel;
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
        periodFromDateChooser = new JDateChooser();
        periodFromDateChooser.setDateFormatString(DATE_FORMAT);

        JLabel periodToLabel = new JLabel(MessageBundle.getMessage("angal.common.dateTo"));
        periodToDateChooser = new JDateChooser();
        periodToDateChooser.setDateFormatString(DATE_FORMAT);

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

        vaccinePeriodFrom = new JDateChooser();
        vaccinePeriodFrom.setDateFormatString(DATE_FORMAT);
        vaccinePeriodTo = new JDateChooser();
        vaccinePeriodTo.setDateFormatString(DATE_FORMAT);

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
//            if (!_selectedExam.isEmpty()) {
//                try {
//                    Exam selectedExam = examsManager.getExamDes(_selectedExam);
//                    examResultsData = examResultsManager.getExamRow(selectedExam.getCode(), null);
//                    examResultsCombo.addItem("");
//                    for (ExamRow examRow : examResultsData) {
//                        examResultsCombo.addItem(examRow.getDescription());
//                    }
//                } catch (OHServiceException ex) {
//                    MessageDialog.showExceptions(ex);
//                }
//            }
        });

        examResultsCombo.addActionListener(e -> {
            Object selected = examResultsCombo.getSelectedItem();
            _selectedExamResult = selected != null ? selected.toString() : "";
        });

        examPeriodFrom = new JDateChooser();
        examPeriodFrom.setDateFormatString(DATE_FORMAT);
        examPeriodTo = new JDateChooser();
        examPeriodTo.setDateFormatString(DATE_FORMAT);

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
            Page<Patient> result = statsManager.getPregnanciesStats(
                    _age_from, _age_to, _period_from, _period_to,
                    _selectedExam, _selectedExamResult, _exam_period_from, _exam_period_to,
                    _selectedVaccine, _vaccine_period_from, _vaccine_period_to,
                    _selectedDisease, _selectedDischargeType,
                    _parameter_height_check, _parameter_weight_check, _parameter_art_press_check,
                    _parameter_card_freq_check, _parameter_temp_check, _parameter_saturation_check,
                    _parameter_resp_rate_check,
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
            return filterResetBtn;
        }

        filterResetBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.resetfilter"));

        filterResetBtn.addActionListener(e -> {
            _start_index = 0;
            _age_from = null;
            _age_to = null;
            _selectedExam = "";
            _selectedExamResult = "";
            _selectedVaccine = "";
            _selectedDisease = "";
            _selectedDischargeType = "";
            _period_from = null;
            _period_to = null;
            _exam_period_from = null;
            _exam_period_to = null;
            _vaccine_period_from = null;
            _vaccine_period_to = null;
            _parameter_height_check = false;
            _parameter_weight_check = false;
            _parameter_art_press_check = false;
            _parameter_card_freq_check = false;
            _parameter_temp_check = false;
            _parameter_saturation_check = false;
            _parameter_resp_rate_check = false;

            periodFromDateChooser.setCalendar(null);
            periodToDateChooser.setCalendar(null);
            ageFromField.setText("");
            ageToField.setText("");
            vaccinesCombo.setSelectedItem(null);
            vaccinePeriodFrom.setCalendar(null);
            vaccinePeriodTo.setCalendar(null);
            examsCombo.setSelectedItem(null);
            examResultsCombo.setSelectedItem(null);
            examPeriodFrom.setCalendar(null);
            examPeriodTo.setCalendar(null);
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
        LocalDate dateFrom = toLocalDate(periodFromDateChooser);
        LocalDate dateTo = toLocalDate(periodToDateChooser);

        if (dateFrom == null && dateTo == null) {
            _period_from = null;
            _period_to = null;
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

        _period_from = dateFrom.atStartOfDay();
        _period_to = dateTo != null ? dateTo.atStartOfDay() : null;

        return true;
    }

    private boolean checkAge() {
        String fromText = ageFromField.getText().trim();
        String toText = ageToField.getText().trim();

        if (fromText.isEmpty() && toText.isEmpty()) {
            _age_from = null;
            _age_to = null;
            return true;
        }

        try {
            _age_from = fromText.isEmpty() ? null : Integer.valueOf(fromText);
            _age_to = toText.isEmpty() ? null : Integer.valueOf(toText);
        } catch (NumberFormatException nfe) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidagerange"));
            return false;
        }

        if (_age_from != null && _age_to != null && _age_from > _age_to) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidagerange"));
            return false;
        }

        return true;
    }

    private boolean checkVaccinePeriod() {
        LocalDate dateFrom = toLocalDate(vaccinePeriodFrom);
        LocalDate dateTo = toLocalDate(vaccinePeriodTo);

        if (dateFrom == null && dateTo == null) {
            _vaccine_period_from = null;
            _vaccine_period_to = null;
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

        _vaccine_period_from = dateFrom.atStartOfDay();
        _vaccine_period_to = dateTo != null ? dateTo.atStartOfDay() : null;

        return true;
    }

    private boolean checkExamPeriod() {
        LocalDate dateFrom = toLocalDate(examPeriodFrom);
        LocalDate dateTo = toLocalDate(examPeriodTo);

        if (dateFrom == null && dateTo == null) {
            _exam_period_from = null;
            _exam_period_to = null;
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

        _exam_period_from = dateFrom.atStartOfDay();
        _exam_period_to = dateTo != null ? dateTo.atStartOfDay() : null;

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

    private LocalDate toLocalDate(JDateChooser chooser) {
        if (chooser.getDate() == null) {
            return null;
        }
        java.util.Date date = chooser.getDate();
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private JPanel setPanelBorder(JPanel panel, String panelTitle) {
        javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(panelTitle),
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.setBorder(b2);
        return panel;
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
}