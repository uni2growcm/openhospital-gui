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
package org.isf.tuberculosis.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.tuberculosis.manager.TuberculosisTreatmentManager;
import org.isf.tuberculosis.model.TuberculosisTreatment;
import org.isf.tuberculosis.model.Classification;
import org.isf.tuberculosis.model.DiseaseLocation;
import org.isf.tuberculosis.model.HivStatus;
import org.isf.tuberculosis.model.TreatmentOutcome;
import org.isf.tuberculosis.model.TreatmentStatus;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class TuberculosisTreatmentEdit extends JDialog
        implements SelectPatient.SelectionListener, PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final EventListenerList tbListeners = new EventListenerList();

    public interface TuberculosisTreatmentListener extends EventListener {
        void treatmentInserted(AWTEvent e, TuberculosisTreatment treatment);
        void treatmentUpdated(AWTEvent e, TuberculosisTreatment treatment);
    }

    public void addTuberculosisTreatmentListener(TuberculosisTreatmentListener l) {
        tbListeners.add(TuberculosisTreatmentListener.class, l);
    }

    private void fireTreatmentInserted(TuberculosisTreatment treatment) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            @Serial
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : tbListeners.getListeners(TuberculosisTreatmentListener.class)) {
            ((TuberculosisTreatmentListener) listener).treatmentInserted(event, treatment);
        }
    }

    private void fireTreatmentUpdated(TuberculosisTreatment treatment) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            @Serial
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : tbListeners.getListeners(TuberculosisTreatmentListener.class)) {
            ((TuberculosisTreatmentListener) listener).treatmentUpdated(event, treatment);
        }
    }

    private TuberculosisTreatment treatment;
    private Patient selectedPatient;
    private final boolean insert;

    private TuberculosisTreatmentManager manager;
    private PatientBrowserManager patientManager;
    private List<Typology> regimenTypologies;

    private JTextField patientSearchField;
    private JButton pickPatientButton;
    private JButton trashPatientButton;

    private GoodDateTimeSpinnerChooser registrationDateField;
    private JComboBox<Classification> classificationCombo;
    private JComboBox<DiseaseLocation> diseaseLocationCombo;
    private JTextField diseaseLocationDetailsField;
    private GoodDateChooser diagnosisDateField;
    private JComboBox<Typology> regimenCombo;
    private GoodDateChooser treatmentStartDateField;
    private GoodDateChooser treatmentEndDateField;
    private JComboBox<HivStatus> hivStatusCombo;
    private GoodDateChooser hivTestDateField;
    private JCheckBox diabetesCheck;
    private JComboBox<TreatmentStatus> statusCombo;
    private JComboBox<TreatmentOutcome> outcomeCombo;
    private GoodDateChooser outcomeDateField;
    private JTextArea notesArea;

    public TuberculosisTreatmentEdit(JFrame owner, TuberculosisTreatment treatment, boolean inserting) {
        super(owner, true);
        this.insert = inserting;
        if (treatment == null) {
            MessageDialog.error(this, "angal.tb.treatment.cannotbenull.msg");
            dispose();
            return;
        }
        this.treatment = treatment;
        this.selectedPatient = treatment.getPatient();
        initManagers();
        initialize();
        if (!insert) {
            loadExistingData();
        }
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    public TuberculosisTreatmentEdit(JFrame owner, Patient patient, boolean inserting) {
        super(owner, true);
        this.selectedPatient = patient;
        this.treatment = new TuberculosisTreatment();
        if (patient != null) {
            this.treatment.setPatient(patient);
        }
        this.treatment.setStatus(TreatmentStatus.ONGOING);
        this.treatment.setRegistrationDate(LocalDateTime.now());
        this.insert = inserting;
        initManagers();
        initialize();
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        manager = Context.getApplicationContext().getBean(TuberculosisTreatmentManager.class);
        patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
        loadRegimenTypologies();
    }

    private void loadRegimenTypologies() {
        try {
            regimenTypologies = Context.getApplicationContext()
                    .getBean(TypologyBrowserManager.class)
                    .getTypologies(Family.TUBERCULOSISREGIMEN);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            regimenTypologies = List.of();
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.tb.treatment.newtitle"));
        } else {
            setTitle(MessageBundle.getMessage("angal.tb.treatment.edittitle"));
        }
        setMinimumSize(new Dimension(700, 600));
        setPreferredSize(new Dimension(800, 650));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(getPatientPanel());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(MessageBundle.getMessage("angal.tb.treatment.generaltab"), getGeneralPanel());
        tabbedPane.addTab(MessageBundle.getMessage("angal.tb.treatment.hivtab"), getHivPanel());
        tabbedPane.addTab(MessageBundle.getMessage("angal.tb.treatment.outcometab"), getOutcomePanel());
        mainPanel.add(tabbedPane);

        add(mainPanel, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (treatment != null && !insert) {
            if (treatment.getRegistrationDate() != null) {
                registrationDateField.setDateTime(treatment.getRegistrationDate());
            }
            if (treatment.getClassification() != null) {
                classificationCombo.setSelectedItem(treatment.getClassification());
            }
            if (treatment.getDiseaseLocation() != null) {
                diseaseLocationCombo.setSelectedItem(treatment.getDiseaseLocation());
            }
            if (treatment.getDiseaseLocationDetails() != null) {
                diseaseLocationDetailsField.setText(treatment.getDiseaseLocationDetails());
            }
            if (treatment.getDiagnosisDate() != null) {
                diagnosisDateField.setDate(treatment.getDiagnosisDate());
            }
            if (treatment.getRegimenCode() != null && regimenTypologies != null) {
                for (Typology typology : regimenTypologies) {
                    if (typology.getCode().equals(treatment.getRegimenCode())) {
                        regimenCombo.setSelectedItem(typology);
                        break;
                    }
                }
            }
            if (treatment.getTreatmentStartDate() != null) {
                treatmentStartDateField.setDate(treatment.getTreatmentStartDate());
            }
            if (treatment.getTreatmentEndDate() != null) {
                treatmentEndDateField.setDate(treatment.getTreatmentEndDate());
            }
            if (treatment.getHivStatus() != null) {
                hivStatusCombo.setSelectedItem(treatment.getHivStatus());
            }
            if (treatment.getHivTestDate() != null) {
                hivTestDateField.setDate(treatment.getHivTestDate());
            }
            diabetesCheck.setSelected(treatment.getDiabetes() != null && treatment.getDiabetes());
            if (treatment.getStatus() != null) {
                statusCombo.setSelectedItem(treatment.getStatus());
            }
            if (treatment.getOutcome() != null) {
                outcomeCombo.setSelectedItem(treatment.getOutcome());
            }
            if (treatment.getOutcomeDate() != null) {
                outcomeDateField.setDate(treatment.getOutcomeDate());
            }
            if (treatment.getNotes() != null) {
                notesArea.setText(treatment.getNotes());
            }
        }
    }

    private void updatePatientDisplay() {
        if (selectedPatient != null) {
            patientSearchField.setText(selectedPatient.getSecondName() + " " + selectedPatient.getFirstName());
            patientSearchField.setEditable(false);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.changepatient"));
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            trashPatientButton.setEnabled(true);
        } else {
            patientSearchField.setText("");
            patientSearchField.setEditable(true);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            trashPatientButton.setEnabled(false);
        }
    }

    private void clearPatientDisplay() {
        selectedPatient = null;
        treatment.setPatient(null);
        patientSearchField.setText("");
        patientSearchField.setEditable(true);
        pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
        pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
        trashPatientButton.setEnabled(false);
    }

    private void searchPatient(String searchText) {
        SelectPatient sp = new SelectPatient(this, searchText, true, true);
        sp.addSelectionListener(this);
        sp.setVisible(true);
    }

    private void openPatientSearch() {
        String searchText = patientSearchField.getText();
        SelectPatient sp = new SelectPatient(this, searchText, true, true);
        sp.addSelectionListener(this);
        sp.setVisible(true);
    }

    @Override
    public void patientSelected(Patient patient) {
        if (patient != null) {
            this.selectedPatient = patient;
            this.treatment.setPatient(patient);
            updatePatientDisplay();
        }
    }

    @Override
    public void patientInserted(AWTEvent e) {
        if (e.getSource() instanceof Patient patient) {
            this.selectedPatient = patient;
            this.treatment.setPatient(patient);
            updatePatientDisplay();
        }
    }

    @Override
    public void patientUpdated(AWTEvent e) {
        if (e.getSource() instanceof Patient patient) {
            this.selectedPatient = patient;
            this.treatment.setPatient(patient);
            updatePatientDisplay();
        }
    }

    private JPanel getPatientPanel() {
        JPanel patientPanel = new JPanel(new GridBagLayout());
        patientPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.treatment.patientlabel")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        patientPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + ":"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        patientSearchField = new JTextField(30);
        patientSearchField.setPreferredSize(new Dimension(250, 20));
        patientSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchPatient(patientSearchField.getText());
                }
            }
        });
        patientPanel.add(patientSearchField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        pickPatientButton = new JButton(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
        pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
        pickPatientButton.addActionListener(e -> openPatientSearch());
        patientPanel.add(pickPatientButton, gbc);

        gbc.gridx = 4;
        trashPatientButton = new JButton();
        trashPatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
        trashPatientButton.setEnabled(selectedPatient != null);
        trashPatientButton.addActionListener(e -> clearPatientDisplay());
        patientPanel.add(trashPatientButton, gbc);

        return patientPanel;
    }

    private JPanel getGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.registrationdate") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        registrationDateField = new GoodDateTimeSpinnerChooser(LocalDateTime.now());
        panel.add(registrationDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.classification") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        classificationCombo = new JComboBox<>(Classification.values());
        classificationCombo.setRenderer(new EnumRenderer());
        panel.add(classificationCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.diseaselocation") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        diseaseLocationCombo = new JComboBox<>(DiseaseLocation.values());
        diseaseLocationCombo.setRenderer(new EnumRenderer());
        panel.add(diseaseLocationCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.locationdetails") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        diseaseLocationDetailsField = new VoLimitedTextField(255, 30);
        panel.add(diseaseLocationDetailsField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.diagnosisdate") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        diagnosisDateField = new GoodDateChooser((LocalDate) null);
        panel.add(diagnosisDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.regimen") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        regimenCombo = new JComboBox<>();
        regimenCombo.addItem(null);
        if (regimenTypologies != null) {
            for (Typology typology : regimenTypologies) {
                regimenCombo.addItem(typology);
            }
        }
        regimenCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Typology typology) {
                    return super.getListCellRendererComponent(list,
                            typology.getDescription(), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            }
        });
        panel.add(regimenCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.startdate") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        treatmentStartDateField = new GoodDateChooser(LocalDate.now());
        panel.add(treatmentStartDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.enddate") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        treatmentEndDateField = new GoodDateChooser((LocalDate) null);
        panel.add(treatmentEndDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
diabetesCheck = new JCheckBox(MessageBundle.getMessage("angal.tb.treatment.diabetes"));
            panel.add(diabetesCheck, gbc);

        return panel;
    }

    private JPanel getHivPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.hivstatus") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        hivStatusCombo = new JComboBox<>(HivStatus.values());
        hivStatusCombo.setRenderer(new EnumRenderer());
        panel.add(hivStatusCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.hivtestdate") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        hivTestDateField = new GoodDateChooser((LocalDate) null);
        panel.add(hivTestDateField, gbc);

        return panel;
    }

    private JPanel getOutcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.status") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        statusCombo = new JComboBox<>(TreatmentStatus.values());
        statusCombo.setRenderer(new EnumRenderer());
        panel.add(statusCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.outcome") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        outcomeCombo = new JComboBox<>(TreatmentOutcome.values());
        outcomeCombo.setRenderer(new EnumRenderer());
        panel.add(outcomeCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.outcomedate") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        outcomeDateField = new GoodDateChooser((LocalDate) null);
        panel.add(outcomeDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.treatment.notes") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        notesArea = new JTextArea(5, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 100));
        panel.add(notesScroll, gbc);

        return panel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.addActionListener(e -> save());

        JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        cancelButton.setPreferredSize(new Dimension(90, 30));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void save() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.tb.treatment.patientrequired.msg");
            return;
        }

        if (registrationDateField.getLocalDateTime() == null) {
            MessageDialog.error(this, "angal.tb.treatment.registrationdaterequired.msg");
            return;
        }

        Classification classification = (Classification) classificationCombo.getSelectedItem();
        if (classification == null) {
            MessageDialog.error(this, "angal.tb.treatment.classificationrequired.msg");
            return;
        }

        DiseaseLocation location = (DiseaseLocation) diseaseLocationCombo.getSelectedItem();
        if (location == null) {
            MessageDialog.error(this, "angal.tb.treatment.locationrequired.msg");
            return;
        }

        if (treatmentStartDateField.getDate() == null) {
            MessageDialog.error(this, "angal.tb.treatment.startdaterequired.msg");
            return;
        }

        TreatmentStatus status = (TreatmentStatus) statusCombo.getSelectedItem();
        if (status == null) {
            MessageDialog.error(this, "angal.tb.treatment.statusrequired.msg");
            return;
        }

        treatment.setRegistrationDate(registrationDateField.getLocalDateTime());
        treatment.setClassification(classification);
        treatment.setDiseaseLocation(location);
        treatment.setDiseaseLocationDetails(diseaseLocationDetailsField.getText().trim());
        treatment.setDiagnosisDate(diagnosisDateField.getDate());

        Typology selectedRegimen = (Typology) regimenCombo.getSelectedItem();
        treatment.setRegimenCode(selectedRegimen != null ? selectedRegimen.getCode() : null);

        treatment.setTreatmentStartDate(treatmentStartDateField.getDate());
        treatment.setTreatmentEndDate(treatmentEndDateField.getDate());

        treatment.setHivStatus((HivStatus) hivStatusCombo.getSelectedItem());
        treatment.setHivTestDate(hivTestDateField.getDate());
        treatment.setDiabetes(diabetesCheck.isSelected() ? true : null);
        

        treatment.setStatus(status);
        treatment.setOutcome((TreatmentOutcome) outcomeCombo.getSelectedItem());
        treatment.setOutcomeDate(outcomeDateField.getDate());
        treatment.setNotes(notesArea.getText().trim());

        try {
            if (insert) {
                TuberculosisTreatment saved = manager.newTreatment(treatment);
                fireTreatmentInserted(saved);
            } else {
                TuberculosisTreatment saved = manager.updateTreatment(treatment);
                fireTreatmentUpdated(saved);
            }
            dispose();
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private static class EnumRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            if (value != null && value instanceof Enum<?> enumValue) {
                return super.getListCellRendererComponent(list,
                        enumValue.toString(), index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
        }
    }
}
