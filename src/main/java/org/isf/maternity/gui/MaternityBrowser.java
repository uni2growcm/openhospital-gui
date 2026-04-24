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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyVisit;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class MaternityBrowser extends JFrame implements PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

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
            MessageBundle.getMessage("angal.maternity.number.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.visitdate.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.visittype.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.visitnote.col").toUpperCase()
    };

    private final int[] vColumnWidths = { 80, 100, 100, 300 };

    private final MaternityBrowser myFrame;
    List<Patient> patientList = new ArrayList<>();
    List<Object> visitList = new ArrayList<>();

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

    public MaternityBrowser() throws OHServiceException {
        setTitle(MessageBundle.getMessage("angal.maternity.browser.title"));
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

    public MaternityBrowser(Patient admission) throws OHServiceException {
        setTitle(MessageBundle.getMessage("angal.maternity.browser.title"));
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
        add(getTopPanel(), BorderLayout.NORTH);
        add(getMiddlePanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
        if (selectedPatient != null) {
            loadPatientData();
        } else {
            performSearch();
        }
    }

    private JPanel getTopPanel() throws OHServiceException {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getFilterPanel(), BorderLayout.WEST);
        topPanel.add(getPatientListPanel(), BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.setPreferredSize(new Dimension(300, 400));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        JPanel datePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        datePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.dateinterval.label")));
        dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        dateTo = new GoodDateChooser(LocalDate.now());
        datePanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label") + ":"));
        datePanel.add(dateFrom);
        datePanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label") + ":"));
        datePanel.add(dateTo);
        filterPanel.add(datePanel);

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

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.deliverystatus.label")));
        deliveryStatusCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.common.all.txt"),
                MessageBundle.getMessage("angal.maternity.delivered.label"),
                MessageBundle.getMessage("angal.maternity.notdelivered.label")
        });
        statusPanel.add(deliveryStatusCombo);
        filterPanel.add(statusPanel);

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
        model = new PatientsTableModel();
        patientTable = new JTable(model);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
            totalPatientsLabel = new JLabel(MessageBundle.getMessage("angal.maternity.total.label") + ": 0");
        }
        return totalPatientsLabel;
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
        filterPanel.setPreferredSize(new Dimension(200, 300));

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

        prenatalRadio.addActionListener(e -> filterVisits());
        postnatalRadio.addActionListener(e -> filterVisits());
        allVisitsRadio.addActionListener(e -> filterVisits());

        return filterPanel;
    }

    private JScrollPane getVisitListPanel() {
        visitTable = new JTable(new MaternityVisitsTableModel());
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

        buttonPanel.add(getJNewMaternityButton());
        buttonPanel.add(getJUpdateMaternityButton());
        buttonPanel.add(getJDeleteMaternityButton());
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

    private JButton getJNewMaternityButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.new.btn"));
        button.addActionListener(e -> newMaternity());
        return button;
    }

    private JButton getJUpdateMaternityButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.update.btn"));
        button.addActionListener(e -> updateMaternity());
        return button;
    }

    private JButton getJDeleteMaternityButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.maternity.delete.btn"));
        button.addActionListener(e -> deleteMaternity());
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
            LocalDate fromDate = dateFrom.getDate();
            LocalDate toDate = dateTo.getDate();
            int ageFrom = Integer.parseInt(ageFromField.getText());
            int ageTo = Integer.parseInt(ageToField.getText());
            String deliveryStatus = (String) deliveryStatusCombo.getSelectedItem();

            if (fromDate.isAfter(toDate)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            if (ageFrom > ageTo) {
                MessageDialog.error(this, "angal.common.agefrommustbelowerthanageto.msg");
                return;
            }

            updatePaginationUI();
            model.fireTableDataChanged();
            patientTable.updateUI();

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, "angal.common.pleaseentervalidnumbers.msg");
        }
    }

    private void updatePaginationUI() {
        underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.page.label"));
        totalPatientsLabel.setText(MessageBundle.getMessage("angal.maternity.total.label") + ": " + TOTAL_PATIENTS);

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
        ((MaternityVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
    }

    private void loadPatientData() {
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void newMaternity() {
    }

    private void updateMaternity() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }
    }

    private void deleteMaternity() {
        int row = patientTable.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }
    }


    private void newVisit() {
        Patient tempPatient = new Patient();
        tempPatient.setCode(1);
        tempPatient.setFirstName("Test");
        tempPatient.setSecondName("Patient");

        Pregnancy tempPregnancy = new Pregnancy();
        tempPregnancy.setPatient(tempPatient);

        MaternityVisitEdit edit = new MaternityVisitEdit(this, tempPregnancy, true);
        edit.addMaternityVisitListener(new MaternityVisitEdit.MaternityVisitListener() {
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
        // Vérifier qu'une ligne est sélectionnée dans la table des visites
        int visitRow = visitTable.getSelectedRow();
        if (visitRow < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        // Vérifier qu'une grossesse est sélectionnée
        int pregnancyRow = patientTable.getSelectedRow();
        if (pregnancyRow < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        // TODO: Récupérer la vraie visite sélectionnée
        // PregnancyVisit selectedVisit = (PregnancyVisit) visitTable.getValueAt(visitRow, -1);

        // Pour l'instant, créer une visite temporaire pour tester
        PregnancyVisit selectedVisit = new PregnancyVisit();
        selectedVisit.setId(1);
        selectedVisit.setVisitDate(LocalDateTime.now());

        Patient selectedPatient = (Patient) patientTable.getValueAt(pregnancyRow, -1);
        Pregnancy pregnancy = new Pregnancy();
        pregnancy.setPatient(selectedPatient);
        selectedVisit.setPregnancy(pregnancy);

        MaternityVisitEdit edit = new MaternityVisitEdit(this, selectedVisit, false);
        edit.addMaternityVisitListener(new MaternityVisitEdit.MaternityVisitListener() {
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
        // Vérifier qu'une ligne est sélectionnée dans la table des visites
        int visitRow = visitTable.getSelectedRow();
        if (visitRow < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        // Vérifier qu'une grossesse est sélectionnée
        int pregnancyRow = patientTable.getSelectedRow();
        if (pregnancyRow < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        // TODO: Récupérer la vraie visite sélectionnée
        // PregnancyVisit selectedVisit = (PregnancyVisit) visitTable.getValueAt(visitRow, -1);

        // Message de confirmation
        String confirmMessage = MessageBundle.getMessage("angal.maternity.deletevisit.confirm.msg");
        int answer = MessageDialog.yesNo(this, confirmMessage);

        if (answer == JOptionPane.YES_OPTION) {
            // TODO: Appeler le manager pour supprimer
            // try {
            //     visitManager.deleteVisit(selectedVisit);
            // } catch (OHServiceException ex) {
            //     OHServiceExceptionUtil.showMessages(ex);
            //     return;
            // }

            // Rafraîchir la liste des visites
            filterVisits();

            // Message de succès
            MessageDialog.info(this,
                    MessageBundle.getMessage("angal.common.info.title"),
                    MessageBundle.getMessage("angal.maternity.deletevisit.success.msg"));
        }
    }

    private void newDelivery() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
    }

    private void updateDelivery() {
    }

    private void deleteDelivery() {
    }

    private void admission() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
    }

    private void exams() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
    }

    private void vaccins() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
    }

    private void therapy() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.common.pleaseselectapatientfirst.msg");
            return;
        }
    }

    class MaternityVisitsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        public String getColumnName(int c) {
            return vColumns[c];
        }

        public int getColumnCount() {
            return vColumns.length;
        }

        public int getRowCount() {
            return 0;
        }

        public Object getValueAt(int r, int c) {
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

            Patient patient = patientList.get(r);

            if (c == -1) {
                return patient;
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
                selectedPatient = (Patient) patientTable.getValueAt(row, -1);
                filterVisits();
            }
        }
    }
}