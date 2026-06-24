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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.hiv.manager.HIVVisitManager;
import org.isf.hiv.model.HIVInfant;
import org.isf.hiv.model.HIVVisit;
import org.isf.hiv.model.HIVVisit.PCRResult;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.TimeTools;

public class HIVVisitEdit extends JDialog {

    private static final long serialVersionUID = 1L;

    private EventListenerList visitListeners = new EventListenerList();

    public interface HIVVisitListener extends EventListener {
        void visitInserted(AWTEvent e, HIVVisit visit);
        void visitUpdated(AWTEvent e, HIVVisit visit);
    }

    public void addHIVVisitListener(HIVVisitListener l) {
        visitListeners.add(HIVVisitListener.class, l);
    }

    public void removeHIVVisitListener(HIVVisitListener listener) {
        visitListeners.remove(HIVVisitListener.class, listener);
    }

    private void fireVisitInserted(HIVVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = visitListeners.getListeners(HIVVisitListener.class);
        for (EventListener listener : listeners) {
            ((HIVVisitListener) listener).visitInserted(event, visit);
        }
    }

    private void fireVisitUpdated(HIVVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = visitListeners.getListeners(HIVVisitListener.class);
        for (EventListener listener : listeners) {
            ((HIVVisitListener) listener).visitUpdated(event, visit);
        }
    }

    // Panels
    private JPanel mainPanel;
    private JPanel infoPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;

    // Buttons
    private JButton okButton;
    private JButton cancelButton;

    // Date field
    private GoodDateTimeSpinnerChooser visitDateField;

    // Clinical data fields
    private VoLimitedTextField weightField;
    private VoLimitedTextField heightField;
    private VoLimitedTextField headCircumferenceField;
    private VoLimitedTextField temperatureField;
    private JTextArea clinicalStatusArea;

    // Lab fields
    private JComboBox<PCRResult> pcrCombo;
    private VoLimitedTextField viralLoadField;
    private VoLimitedTextField cd4CountField;
    private VoLimitedTextField cd4PercentField;
    private VoLimitedTextField hemoglobinField;
    private VoLimitedTextField adherenceField;
    private JTextArea sideEffectsArea;

    // Treatment fields
    private JComboBox<String> treatmentTypeCombo;
    private JComboBox<String> treatmentMedicationCombo;
    private GoodDateChooser treatmentStartDateField;
    private GoodDateChooser treatmentEndDateField;

    // Follow-up fields
    private GoodDateChooser nextAppointmentDateField;
    private JTextArea notesArea;

    // Data
    private HIVVisit visit;
    private HIVInfant infant;
    private boolean insert;

    // Managers
    private HIVVisitManager visitManager;


    public HIVVisitEdit(JFrame owner, HIVInfant infant, boolean inserting) {
        super(owner, true);
        this.infant = infant;
        this.visit = new HIVVisit();
        this.visit.setHivInfant(infant);
        this.insert = inserting;
        initManagers();
        initialize();
        pack();
        setLocationRelativeTo(owner);
    }

    public HIVVisitEdit(JFrame owner, HIVVisit visit, boolean inserting) {
        super(owner, true);
        this.visit = visit;
        this.infant = visit.getHivInfant();
        this.insert = inserting;
        initManagers();
        initialize();
        if (!insert) {
            loadExistingData();
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        visitManager = Context.getApplicationContext().getBean(HIVVisitManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.hiv.visit.new.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.hiv.visit.edit.title"));
        }
        setMinimumSize(new Dimension(650, 750));
        setPreferredSize(new Dimension(700, 850));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (visit != null && !insert) {
            if (visit.getVisitDate() != null) {
                visitDateField.setDateTime(visit.getVisitDate());
            }
            if (visit.getWeight() != null) {
                weightField.setText(String.valueOf(visit.getWeight()));
            }
            if (visit.getHeight() != null) {
                heightField.setText(String.valueOf(visit.getHeight()));
            }
            if (visit.getHeadCircumference() != null) {
                headCircumferenceField.setText(String.valueOf(visit.getHeadCircumference()));
            }
            if (visit.getTemperature() != null) {
                temperatureField.setText(String.valueOf(visit.getTemperature()));
            }
            if (visit.getClinicalStatus() != null) {
                clinicalStatusArea.setText(visit.getClinicalStatus());
            }
            if (visit.getPcrResult() != null) {
                pcrCombo.setSelectedItem(visit.getPcrResult());
            }
            if (visit.getViralLoad() != null) {
                viralLoadField.setText(String.valueOf(visit.getViralLoad()));
            }
            if (visit.getCd4Count() != null) {
                cd4CountField.setText(String.valueOf(visit.getCd4Count()));
            }
            if (visit.getCd4Percent() != null) {
                cd4PercentField.setText(String.valueOf(visit.getCd4Percent()));
            }
            if (visit.getHemoglobin() != null) {
                hemoglobinField.setText(String.valueOf(visit.getHemoglobin()));
            }
            if (visit.getAdherence() != null) {
                adherenceField.setText(String.valueOf(visit.getAdherence()));
            }
            if (visit.getSideEffects() != null) {
                sideEffectsArea.setText(visit.getSideEffects());
            }
            if (visit.getNextAppointmentDate() != null) {
                nextAppointmentDateField.setDate(visit.getNextAppointmentDate());
            }
            if (visit.getNotes() != null) {
                notesArea.setText(visit.getNotes());
            }
        }
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(getDataPanel());
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            mainPanel.add(scrollPane);
        }
        return mainPanel;
    }

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new GridBagLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.data.border")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // Infant Information
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            dataPanel.add(getInfantInfoPanel(), gbc);
            row++;

            // Separator
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(new JSeparator(), gbc);
            row++;

            // Visit Date
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.visitdate.col") + " *:"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getVisitDateField(), gbc);
            row++;

            // Clinical Data
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            dataPanel.add(getClinicalDataPanel(), gbc);
            row++;

            // Lab Data
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(getLabDataPanel(), gbc);
            row++;

            // Treatment Data
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(getTreatmentPanel(), gbc);
            row++;

            // Side Effects
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(getSideEffectsPanel(), gbc);
            row++;

            // Follow-up
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(getFollowUpPanel(), gbc);
            row++;

            // Add vertical filler
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.VERTICAL;
            dataPanel.add(new JPanel(), gbc);
        }
        return dataPanel;
    }

    private JPanel getInfantInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.infant.info")));

        Patient patient = infant.getPatient();
        if (patient != null) {
            JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.hiv.label.patient") + ": " + patient.getFirstName() + " " + patient.getSecondName() + " (" + patient.getCode() + ")");
            JLabel ageLabel = new JLabel(MessageBundle.getMessage("angal.common.age.txt") + ": " + patient.getAge() + " mois");
            JLabel statusLabel = new JLabel(MessageBundle.getMessage("angal.hiv.label.status") + ": " + (infant.getStatus() != null ? infant.getStatus().getDescription() : ""));
            JLabel motherLabel = new JLabel(MessageBundle.getMessage("angal.hiv.label.mother") + ": " + (infant.getMother() != null ? infant.getMother().getFirstName() + " " + infant.getMother().getSecondName() : "N/A"));
            JLabel feedingLabel = new JLabel(MessageBundle.getMessage("angal.hiv.filter.feeding") + ": " + (infant.getFeedingType() != null ? infant.getFeedingType().getDescription() : "N/A"));

            panel.add(nameLabel);
            panel.add(ageLabel);
            panel.add(statusLabel);
            panel.add(motherLabel);
            panel.add(feedingLabel);
        }

        return panel;
    }

    private JPanel getClinicalDataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.clinical.data")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Weight
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.weights") + "*:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(getWeightField(), gbc);
        row++;

        // Height
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.height") + " (cm):"), gbc);

        gbc.gridx = 1;
        panel.add(getHeightField(), gbc);
        row++;

        // Head Circumference
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.head.circumference") + " (cm):"), gbc);

        gbc.gridx = 1;
        panel.add(getHeadCircumferenceField(), gbc);
        row++;

        // Temperature
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.temperature") + " (°C):"), gbc);

        gbc.gridx = 1;
        panel.add(getTemperatureField(), gbc);
        row++;

        // Clinical Status
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.clinical.status") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weighty = 0.5;
        panel.add(getClinicalStatusScrollPane(), gbc);

        return panel;
    }

    private JPanel getLabDataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.lab.data")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // PCR
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel("PCR:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(getPcrCombo(), gbc);
        row++;

        // Viral Load
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.viral.load") + " (copies/mL):"), gbc);

        gbc.gridx = 1;
        panel.add(getViralLoadField(), gbc);
        row++;

        // CD4 Count
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.cd4.count") + " (cells/µL):"), gbc);

        gbc.gridx = 1;
        panel.add(getCd4CountField(), gbc);
        row++;

        // CD4 Percent
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.cd4.percent") + " (%):"), gbc);

        gbc.gridx = 1;
        panel.add(getCd4PercentField(), gbc);
        row++;

        // Hemoglobin
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.hemoglobin") + " (g/dL):"), gbc);

        gbc.gridx = 1;
        panel.add(getHemoglobinField(), gbc);
        row++;

        // Adherence
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.adherence") + " (%):"), gbc);

        gbc.gridx = 1;
        panel.add(getAdherenceField(), gbc);

        return panel;
    }

    private JPanel getTreatmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.treatment")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Treatment Type
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.treatment.type") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(getTreatmentTypeCombo(), gbc);
        row++;

        // Medication
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.treatment.medication") + ":"), gbc);

        gbc.gridx = 1;
        panel.add(getTreatmentMedicationCombo(), gbc);
        row++;

        // Start Date
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.treatment.start.date") + ":"), gbc);

        gbc.gridx = 1;
        panel.add(getTreatmentStartDateField(), gbc);
        row++;

        // End Date
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.treatment.end.date") + ":"), gbc);

        gbc.gridx = 1;
        panel.add(getTreatmentEndDateField(), gbc);
        row++;

        return panel;
    }

    private JPanel getSideEffectsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.side.effects")));

        panel.add(getSideEffectsScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel getFollowUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.followup")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Next Appointment
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.next.appointment") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(getNextAppointmentDateField(), gbc);
        row++;

        // Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.notes") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weighty = 0.5;
        panel.add(getNotesScrollPane(), gbc);

        return panel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.add(getOkButton());
            buttonPanel.add(getCancelButton());
        }
        return buttonPanel;
    }

    // Getters for components
    private GoodDateTimeSpinnerChooser getVisitDateField() {
        if (visitDateField == null) {
            LocalDateTime dateTime = (insert || visit.getVisitDate() == null) ? LocalDateTime.now() : visit.getVisitDate();
            visitDateField = new GoodDateTimeSpinnerChooser(dateTime);
        }
        return visitDateField;
    }

    private VoLimitedTextField getWeightField() {
        if (weightField == null) {
            weightField = new VoLimitedTextField(5, 2);
            weightField.setColumns(8);
        }
        return weightField;
    }

    private VoLimitedTextField getHeightField() {
        if (heightField == null) {
            heightField = new VoLimitedTextField(5, 1);
            heightField.setColumns(8);
        }
        return heightField;
    }

    private VoLimitedTextField getHeadCircumferenceField() {
        if (headCircumferenceField == null) {
            headCircumferenceField = new VoLimitedTextField(5, 1);
            headCircumferenceField.setColumns(8);
        }
        return headCircumferenceField;
    }

    private VoLimitedTextField getTemperatureField() {
        if (temperatureField == null) {
            temperatureField = new VoLimitedTextField(4, 1);
            temperatureField.setColumns(6);
        }
        return temperatureField;
    }

    private JTextArea getClinicalStatusArea() {
        if (clinicalStatusArea == null) {
            clinicalStatusArea = new JTextArea(3, 20);
            clinicalStatusArea.setLineWrap(true);
            clinicalStatusArea.setWrapStyleWord(true);
        }
        return clinicalStatusArea;
    }

    private JScrollPane getClinicalStatusScrollPane() {
        return new JScrollPane(getClinicalStatusArea());
    }

    private JComboBox<PCRResult> getPcrCombo() {
        if (pcrCombo == null) {
            pcrCombo = new JComboBox<>(PCRResult.values());
            pcrCombo.insertItemAt(null, 0);
            pcrCombo.setSelectedIndex(0);
        }
        return pcrCombo;
    }

    private VoLimitedTextField getViralLoadField() {
        if (viralLoadField == null) {
            viralLoadField = new VoLimitedTextField(8, 0);
            viralLoadField.setColumns(10);
        }
        return viralLoadField;
    }

    private VoLimitedTextField getCd4CountField() {
        if (cd4CountField == null) {
            cd4CountField = new VoLimitedTextField(4, 0);
            cd4CountField.setColumns(6);
        }
        return cd4CountField;
    }

    private VoLimitedTextField getCd4PercentField() {
        if (cd4PercentField == null) {
            cd4PercentField = new VoLimitedTextField(3, 0);
            cd4PercentField.setColumns(5);
        }
        return cd4PercentField;
    }

    private VoLimitedTextField getHemoglobinField() {
        if (hemoglobinField == null) {
            hemoglobinField = new VoLimitedTextField(4, 1);
            hemoglobinField.setColumns(6);
        }
        return hemoglobinField;
    }

    private VoLimitedTextField getAdherenceField() {
        if (adherenceField == null) {
            adherenceField = new VoLimitedTextField(3, 0);
            adherenceField.setColumns(5);
        }
        return adherenceField;
    }

    private JTextArea getSideEffectsArea() {
        if (sideEffectsArea == null) {
            sideEffectsArea = new JTextArea(3, 20);
            sideEffectsArea.setLineWrap(true);
            sideEffectsArea.setWrapStyleWord(true);
        }
        return sideEffectsArea;
    }

    private JScrollPane getSideEffectsScrollPane() {
        return new JScrollPane(getSideEffectsArea());
    }

    private JComboBox<String> getTreatmentTypeCombo() {
        if (treatmentTypeCombo == null) {
            treatmentTypeCombo = new JComboBox<>();
            treatmentTypeCombo.addItem(MessageBundle.getMessage("angal.hiv.treatment.type.prophylaxis"));
            treatmentTypeCombo.addItem(MessageBundle.getMessage("angal.hiv.treatment.type.arv"));
            treatmentTypeCombo.setSelectedIndex(0);
        }
        return treatmentTypeCombo;
    }

    private JComboBox<String> getTreatmentMedicationCombo() {
        if (treatmentMedicationCombo == null) {
            treatmentMedicationCombo = new JComboBox<>();
            // Prophylaxis
            treatmentMedicationCombo.addItem("Névirapine");
            treatmentMedicationCombo.addItem("AZT");
            treatmentMedicationCombo.addItem("AZT+NVP");
            treatmentMedicationCombo.addItem("Bactrim");
            treatmentMedicationCombo.addItem(MessageBundle.getMessage("angal.hiv.treatment.other"));
        }
        return treatmentMedicationCombo;
    }

    private GoodDateChooser getTreatmentStartDateField() {
        if (treatmentStartDateField == null) {
            treatmentStartDateField = new GoodDateChooser(LocalDate.now());
        }
        return treatmentStartDateField;
    }

    private GoodDateChooser getTreatmentEndDateField() {
        if (treatmentEndDateField == null) {
            treatmentEndDateField = new GoodDateChooser(null);
        }
        return treatmentEndDateField;
    }

    private GoodDateChooser getNextAppointmentDateField() {
        if (nextAppointmentDateField == null) {
            LocalDate date = (!insert && visit.getNextAppointmentDate() != null) ? visit.getNextAppointmentDate() : LocalDate.now().plusMonths(1);
            nextAppointmentDateField = new GoodDateChooser(date);
        }
        return nextAppointmentDateField;
    }

    private JTextArea getNotesArea() {
        if (notesArea == null) {
            notesArea = new JTextArea(3, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
        }
        return notesArea;
    }

    private JScrollPane getNotesScrollPane() {
        return new JScrollPane(getNotesArea());
    }

    private JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
            okButton.addActionListener(e -> saveVisit());
        }
        return okButton;
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
            cancelButton.addActionListener(e -> dispose());
        }
        return cancelButton;
    }

    private void saveVisit() {
        try {
            LocalDateTime visitDate = visitDateField.getLocalDateTime();
            if (visitDate == null) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseinsertavaliddate.msg"));
                return;
            }

            visit.setVisitDate(visitDate);

            if (nextAppointmentDateField.getDate() != null) {
                LocalDate nextAppointment = nextAppointmentDateField.getDate();
                LocalDate visitDateLocal = visitDate.toLocalDate();
                if (nextAppointment.isBefore(visitDateLocal)) {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.next.appointment.before.visit"));
                    return;
                }
            }

            // Clinical data
            String weightText = weightField.getText().trim();
            if (!weightText.isEmpty()) {
                visit.setWeight(Double.parseDouble(weightText));
            } else {
                visit.setWeight(null);
            }

            String heightText = heightField.getText().trim();
            if (!heightText.isEmpty()) {
                visit.setHeight(Double.parseDouble(heightText));
            } else {
                visit.setHeight(null);
            }

            String headCircText = headCircumferenceField.getText().trim();
            if (!headCircText.isEmpty()) {
                visit.setHeadCircumference(Double.parseDouble(headCircText));
            } else {
                visit.setHeadCircumference(null);
            }

            String tempText = temperatureField.getText().trim();
            if (!tempText.isEmpty()) {
                visit.setTemperature(Double.parseDouble(tempText));
            } else {
                visit.setTemperature(null);
            }

            String clinicalStatus = clinicalStatusArea.getText();
            visit.setClinicalStatus((clinicalStatus != null && !clinicalStatus.trim().isEmpty()) ? clinicalStatus.trim() : null);

            // Lab data
            visit.setPcrResult((PCRResult) pcrCombo.getSelectedItem());

            String viralLoadText = viralLoadField.getText().trim();
            if (!viralLoadText.isEmpty()) {
                visit.setViralLoad(Double.parseDouble(viralLoadText));
            } else {
                visit.setViralLoad(null);
            }

            String cd4CountText = cd4CountField.getText().trim();
            if (!cd4CountText.isEmpty()) {
                visit.setCd4Count(Integer.parseInt(cd4CountText));
            } else {
                visit.setCd4Count(null);
            }

            String cd4PercentText = cd4PercentField.getText().trim();
            if (!cd4PercentText.isEmpty()) {
                visit.setCd4Percent(Integer.parseInt(cd4PercentText));
            } else {
                visit.setCd4Percent(null);
            }

            String hemoglobinText = hemoglobinField.getText().trim();
            if (!hemoglobinText.isEmpty()) {
                visit.setHemoglobin(Double.parseDouble(hemoglobinText));
            } else {
                visit.setHemoglobin(null);
            }

            String adherenceText = adherenceField.getText().trim();
            if (!adherenceText.isEmpty()) {
                visit.setAdherence(Integer.parseInt(adherenceText));
            } else {
                visit.setAdherence(null);
            }

            String sideEffects = sideEffectsArea.getText();
            visit.setSideEffects((sideEffects != null && !sideEffects.trim().isEmpty()) ? sideEffects.trim() : null);

            // Treatment data
            String treatmentType = (String) treatmentTypeCombo.getSelectedItem();
            if (treatmentType != null && !treatmentType.isEmpty()) {
                String medication = (String) treatmentMedicationCombo.getSelectedItem();
                LocalDate startDate = treatmentStartDateField.getDate();
                LocalDate endDate = treatmentEndDateField.getDate();

                if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.treatment.end.before.start"));
                    return;
                }
            }

            visit.setNextAppointmentDate(nextAppointmentDateField.getDate());

            String notes = notesArea.getText();
            visit.setNotes((notes != null && !notes.trim().isEmpty()) ? notes.trim() : null);

            if (insert) {
                HIVVisit saved = visitManager.newVisit(visit);
                if (saved != null) {
                    fireVisitInserted(saved);
                    dispose();
                } else {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.common.datacouldnotbesaved.msg"));
                }
            } else {
                HIVVisit updated = visitManager.updateVisit(visit);
                if (updated != null) {
                    fireVisitUpdated(updated);
                    dispose();
                } else {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.common.datacouldnotbesaved.msg"));
                }
            }

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseentervalidnumbers.msg"));
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }
}