/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

package org.isf.pregnancycare.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.springframework.data.domain.Page;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class PregnancyCareBrowser extends JFrame implements PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] columnHeaders = {
            MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.address.txt").toUpperCase()
    };

    private final int[] columnWidths = { 50, 200, 50, 200 };

    private final String[] vColumns = {
            MessageBundle.getMessage("angal.pregnancy.pregnancynumber.col").toUpperCase(),
            MessageBundle.getMessage("angal.pregnancy.visitdate.col").toUpperCase(),
            MessageBundle.getMessage("angal.pregnancy.visittype.col").toUpperCase(),
            MessageBundle.getMessage("angal.pregnancy.visitnote.col").toUpperCase()
    };

    private final int[] vColumnWidths = { 80, 100, 100, 300 };

    private final PregnancyCareBrowser myFrame;
    List<Admission> patientList = new ArrayList<>();
    List<Object> visitList = new ArrayList<>(); // Replace with your actual visit model

    private final AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);

    private JTable patientTable;
    private JTable visitTable;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox<Integer> pagesCombo;
    private JLabel underLabel;
    private JLabel totalPatientsLabel;
    private DefaultTableModel model;
    private int TOTAL_PAGES = 0;
    private int CURRENT_PAGE = 1;
    private long TOTAL_PATIENTS = 0;
    private final int PAGE_SIZE = 100;

    // Filter components
    private JTextField patientCodeFilter;
    private GoodDateChooser dateFrom;
    private GoodDateChooser dateTo;
    private VoLimitedTextField ageFromField;
    private VoLimitedTextField ageToField;
    private JComboBox<String> deliveryStatusCombo;
    private JRadioButton prenatalRadio;
    private JRadioButton postnatalRadio;
    private JButton searchButton;
    private JButton resetButton;

    private Patient selectedPatient;

    public PregnancyCareBrowser() throws OHServiceException {
        setTitle(MessageBundle.getMessage("angal.pregnancy.patientsbrowser.title"));
        myFrame = this;
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
        myFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public PregnancyCareBrowser(Patient admission) throws OHServiceException {
        setTitle(MessageBundle.getMessage("angal.pregnancy.patientsbrowser.title"));
        myFrame = this;
        this.selectedPatient = admission;
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
        myFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void initComponents() throws OHServiceException {
        setLayout(new BorderLayout());

        // Top Panel with Patient list and filters
        add(getTopPanel(), BorderLayout.NORTH);

        // Middle Panel with Visits
        add(getMiddlePanel(), BorderLayout.CENTER);

        // Bottom Panel with Buttons
        add(getButtonPanel(), BorderLayout.SOUTH);

        // Load initial data
        if (selectedPatient != null) {
            loadPatientData();
        } else {
            performSearch();
        }
    }

    private JPanel getTopPanel() throws OHServiceException {
        JPanel topPanel = new JPanel(new BorderLayout());

        // Left filter panel
        topPanel.add(getFilterPanel(), BorderLayout.WEST);

        // Center patient list panel with pagination
        topPanel.add(getPatientListPanel(), BorderLayout.CENTER);

        return topPanel;
    }

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.setPreferredSize(new Dimension(300, 400));

        // Patient Code Filter
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        // Date Interval Filter
        JPanel datePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        datePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.dateinterval.label")));
        dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        dateTo = new GoodDateChooser(LocalDate.now());
        datePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label") + ":"));
        datePanel.add(dateFrom);
        datePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label") + ":"));
        datePanel.add(dateTo);
        filterPanel.add(datePanel);

        // Age Interval Filter
        JPanel agePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        agePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.ageinterval.label")));
        ageFromField = new VoLimitedTextField(3, 3);
        ageFromField.setText("0");
        ageToField = new VoLimitedTextField(3, 3);
        ageToField.setText("200");
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.agefrom.label") + ":"));
        agePanel.add(ageFromField);
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.ageto.label") + ":"));
        agePanel.add(ageToField);
        filterPanel.add(agePanel);

        // Delivery Status Filter
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.pregnancy.deliverystatus.label")));
        deliveryStatusCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.common.all.txt"),
                MessageBundle.getMessage("angal.pregnancy.delivered.label"),
                MessageBundle.getMessage("angal.pregnancy.notdelivered.label")
        });
        statusPanel.add(deliveryStatusCombo);
        filterPanel.add(statusPanel);

        // Search and Reset buttons
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

    private JPanel getPatientListPanel() throws OHServiceException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getPatientTablePanel(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getPatientTablePanel() {
        try {
            model = new PatientsTableModel("");
            patientTable = new JTable(model);
            patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
        }

        patientTable.setAutoCreateRowSorter(true);

        for (int i = 0; i < columnHeaders.length; i++) {
            patientTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        TableListener listener = new TableListener();
        patientTable.getSelectionModel().addListSelectionListener(listener);

        JScrollPane patientScrollPane = new JScrollPane(patientTable);
        patientScrollPane.setPreferredSize(new Dimension(800, 400));
        return patientScrollPane;
    }

    private JPanel getPaginationPanel() {
        JPanel paginatePanel = new JPanel(new WrapLayout());
        paginatePanel.add(getPrevButton());
        paginatePanel.add(getPagesCombo());
        paginatePanel.add(getUnderLabel());
        paginatePanel.add(getNextButton());
        paginatePanel.add(getTotalPatientsLabel());
        return paginatePanel;
    }

    private JButton getNextButton() {
        if (nextButton == null) {
            nextButton = new JButton(">");
            nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES != 1);
            nextButton.addActionListener(actionEvent -> {
                if (CURRENT_PAGE < TOTAL_PAGES) {
                    CURRENT_PAGE++;
                    pagesCombo.setSelectedItem(CURRENT_PAGE);
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
                    pagesCombo.setSelectedItem(CURRENT_PAGE);
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

    private JLabel getTotalPatientsLabel() {
        if (totalPatientsLabel == null) {
            totalPatientsLabel = new JLabel(MessageBundle.getMessage("angal.pregnancy.totalpatient.label") + ": 0");
        }
        return totalPatientsLabel;
    }

    private JPanel getMiddlePanel() {
        JPanel middlePanel = new JPanel(new BorderLayout());

        // Left filter panel for visits
        middlePanel.add(getVisitFilterPanel(), BorderLayout.WEST);

        // Center visit list panel
        middlePanel.add(getVisitListPanel(), BorderLayout.CENTER);

        return middlePanel;
    }

    private JPanel getVisitFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.pregnancy.visitfilter.label")));
        filterPanel.setPreferredSize(new Dimension(200, 300));

        ButtonGroup visitTypeGroup = new ButtonGroup();
        prenatalRadio = new JRadioButton(MessageBundle.getMessage("angal.pregnancy.prenatal.label"));
        postnatalRadio = new JRadioButton(MessageBundle.getMessage("angal.pregnancy.postnatal.label"));
        JRadioButton allVisitsRadio = new JRadioButton(MessageBundle.getMessage("angal.common.all.label"));

        allVisitsRadio.setSelected(true);

        visitTypeGroup.add(prenatalRadio);
        visitTypeGroup.add(postnatalRadio);
        visitTypeGroup.add(allVisitsRadio);

        filterPanel.add(prenatalRadio);
        filterPanel.add(postnatalRadio);
        filterPanel.add(allVisitsRadio);

        // Add listeners
        prenatalRadio.addActionListener(e -> filterVisits());
        postnatalRadio.addActionListener(e -> filterVisits());
        allVisitsRadio.addActionListener(e -> filterVisits());

        return filterPanel;
    }

    private JScrollPane getVisitListPanel() {
        visitTable = new JTable(new PregnancyVisitsTableModel());
        visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.newpregnancy.btn"));
        button.addActionListener(e -> newPregnancy());
        return button;
    }

    private JButton getJUpdatePregnancyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.updatepregnancy.btn"));
        button.addActionListener(e -> updatePregnancy());
        return button;
    }

    private JButton getJDeletePregnancyButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.deletepregnancy.btn"));
        button.addActionListener(e -> deletePregnancy());
        return button;
    }

    private JButton getJNewVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.newvisit.btn"));
        button.addActionListener(e -> newVisit());
        return button;
    }

    private JButton getJUpdateVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.updatevisit.btn"));
        button.addActionListener(e -> updateVisit());
        return button;
    }

    private JButton getJDeleteVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.deletevisit.btn"));
        button.addActionListener(e -> deleteVisit());
        return button;
    }

    private JButton getJNewDeliveryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.newdelivery.btn"));
        button.addActionListener(e -> newDelivery());
        return button;
    }

    private JButton getJUpdateDeliveryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.updatedelivery.btn"));
        button.addActionListener(e -> updateDelivery());
        return button;
    }

    private JButton getJDeleteDeliveryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.deletedelivery.btn"));
        button.addActionListener(e -> deleteDelivery());
        return button;
    }

    private JButton getJAdmissionButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.admission.btn"));
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
        JButton button = new JButton(MessageBundle.getMessage("angal.pregnancy.therapy.btn"));
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
            LocalDate fromDate = dateFrom.getDate();
            LocalDate toDate = dateTo.getDate();
            int ageFrom = Integer.parseInt(ageFromField.getText());
            int ageTo = Integer.parseInt(ageToField.getText());
            String deliveryStatus = (String) deliveryStatusCombo.getSelectedItem();

            // Validate dates
            if (fromDate.isAfter(toDate)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            // Validate ages
            if (ageFrom > ageTo) {
                MessageDialog.error(this, "angal.common.agefrommustbelowerthanageto.msg");
                return;
            }

            // Call your service method with all filters
            // For now, using existing method - you'll need to update your manager
            Page<Admission> pagedResult = admissionBrowserManager.getAdmittedPatientsBySexAndNamePaged(
                    'F', patientCode, PAGE_SIZE, CURRENT_PAGE - 1
            );

            patientList = pagedResult.getContent();
            TOTAL_PATIENTS = pagedResult.getTotalElements();
            TOTAL_PAGES = pagedResult.getTotalPages();

            updatePaginationUI();
            model.fireTableDataChanged();
            patientTable.updateUI();

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, "angal.common.pleaseentervalidnumbers.msg");
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void updatePaginationUI() {
        underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.page.label"));
        totalPatientsLabel.setText(MessageBundle.getMessage("angal.pregnancy.totalpatient.label") + ": " + TOTAL_PATIENTS);

        pagesCombo.removeAllItems();
        for (int i = 1; i <= TOTAL_PAGES; i++) {
            pagesCombo.addItem(i);
        }
        if (TOTAL_PAGES > 0) {
            pagesCombo.setSelectedItem(CURRENT_PAGE);
        }

        nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES > 1);
        prevButton.setEnabled(CURRENT_PAGE > 1);
    }

    private void resetFilters() {
        patientCodeFilter.setText("");
        dateFrom.setDate(LocalDate.now().minusMonths(6));
        dateTo.setDate(LocalDate.now());
        ageFromField.setText("0");
        ageToField.setText("200");
        deliveryStatusCombo.setSelectedIndex(0);
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void filterVisits() {
        // Implement visit filtering based on prenatal/postnatal selection
        ((PregnancyVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
    }

    private void loadPatientData() {
        // Load data for specific patient
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void newPregnancy() {
        // Implement new pregnancy
    }

    private void updatePregnancy() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }
        // Implement update pregnancy
    }

    private void deletePregnancy() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }
        // Implement delete pregnancy
    }

    private void newVisit() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
        // Implement new visit
    }

    private void updateVisit() {
        int row = visitTable.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }
        // Implement update visit
    }

    private void deleteVisit() {
        int row = visitTable.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }
        // Implement delete visit
    }

    private void newDelivery() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
        // Implement new delivery
    }

    private void updateDelivery() {
        // Implement update delivery
    }

    private void deleteDelivery() {
        // Implement delete delivery
    }

    private void admission() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
        // Implement admission
    }

    private void exams() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
        // Implement exams
    }

    private void vaccins() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
        // Implement vaccins
    }

    private void therapy() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
        // Implement therapy
    }

    class PregnancyVisitsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        public String getColumnName(int c) {
            return vColumns[c];
        }

        public int getColumnCount() {
            return vColumns.length;
        }

        public int getRowCount() {
            // Return filtered count based on radio selection
            return 0; // Implement with actual data
        }

        public Object getValueAt(int r, int c) {
            // Return visit data
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    class PatientsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        public PatientsTableModel(String keywords) throws OHServiceException {
            // Data is loaded in performSearch()
        }

        public int getRowCount() {
            if (patientList == null) {
                return 0;
            }
            return patientList.size();
        }

        public String getColumnName(int c) {
            return columnHeaders[c];
        }

        public int getColumnCount() {
            return columnHeaders.length;
        }

        public Object getValueAt(int r, int c) {
            if (patientList.isEmpty() || r >= patientList.size()) {
                return null;
            }

            Admission admission = patientList.get(r);
            Patient patient = admission.getPatient();

            if (c == -1) {
                return admission;
            } else if (c == 0) {
                return patient.getCode();
            } else if (c == 1) {
                return patient.getSecondName() + " " + patient.getFirstName();
            } else if (c == 2) {
                return patient.getAge();
            } else if (c == 3) {
                return patient.getCity() + " " + patient.getAddress();
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
            int row = patientTable.getSelectedRow();
            if (!arg0.getValueIsAdjusting() && row > -1) {
                Admission admission = (Admission) patientTable.getValueAt(row, -1);
                selectedPatient = admission.getPatient();
                filterVisits();
            }
        }
    }
}