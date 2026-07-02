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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Optional;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hiv.manager.HIVInfantManager;
import org.isf.hiv.manager.HIVVisitManager;
import org.isf.hiv.model.HIVInfant;
import org.isf.hiv.model.HIVInfant.HIVInfantStatus;
import org.isf.hiv.model.HIVInfant.FeedingType;
import org.isf.hiv.model.HIVVisit;
import org.isf.hiv.model.HIVVisit.PCRResult;
import org.isf.maternity.manager.NewBornBrowserManager;
import org.isf.maternity.model.Newborn;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.TimeTools;

public class HIVInfantEdit extends JDialog implements SelectionListener, PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

    private static final long serialVersionUID = 1L;

    private EventListenerList infantListeners = new EventListenerList();

    public interface HIVInfantListener extends EventListener {
        void infantInserted(AWTEvent e, HIVInfant infant);
        void infantUpdated(AWTEvent e, HIVInfant infant);
    }

    public void addHIVInfantListener(HIVInfantListener l) {
        infantListeners.add(HIVInfantListener.class, l);
    }

    public void removeHIVInfantListener(HIVInfantListener listener) {
        infantListeners.remove(HIVInfantListener.class, listener);
    }

    private void fireInfantInserted(HIVInfant infant) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = infantListeners.getListeners(HIVInfantListener.class);
        for (EventListener listener : listeners) {
            ((HIVInfantListener) listener).infantInserted(event, infant);
        }
    }

    private void fireInfantUpdated(HIVInfant infant) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = infantListeners.getListeners(HIVInfantListener.class);
        for (EventListener listener : listeners) {
            ((HIVInfantListener) listener).infantUpdated(event, infant);
        }
    }

    // Panels
    private JPanel mainPanel;
    private JPanel datePanel;
    private JPanel patientPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;

    // Buttons
    private JButton okButton;
    private JButton cancelButton;

    // Date field
    private GoodDateTimeSpinnerChooser registrationDateField;
    private GoodDateChooser firstVisitNextAppointmentField;

    // Patient fields
    private JTextField patientSearchField;
    private JButton pickPatientButton;
    private JButton trashPatientButton;
    private JTextField motherSearchField;
    private JButton pickMotherButton;
    private JButton trashMotherButton;

    // Birth data fields
    private VoLimitedTextField birthWeightField;
    private VoLimitedTextField gestationalAgeField;
    private JComboBox<FeedingType> feedingCombo;

    // Follow-up fields
    private JComboBox<HIVInfantStatus> statusCombo;
    private GoodDateChooser followUpStartDateField;
    private GoodDateChooser followUpEndDateField;
    private JTextArea notesArea;

    // First visit option
    private JCheckBox addFirstVisitCheckBox;
    private JPanel firstVisitPanel;
    private GoodDateChooser firstVisitDateField;
    private VoLimitedTextField firstVisitWeightField;
    private VoLimitedTextField firstVisitHeightField;
    private JComboBox<PCRResult> firstVisitPcrCombo;
    private JTextArea firstVisitNotesArea;

    private HIVInfant infant;
    private Patient selectedPatient;
    private Patient selectedMother;
    private boolean insert;

    private HIVInfantManager infantManager;
    private HIVVisitManager visitManager;
    private PatientBrowserManager patientManager;

    public HIVInfantEdit(JFrame owner, HIVInfant infant, boolean inserting) {
        super(owner, true);
        this.infant = infant;
        this.selectedPatient = infant != null ? infant.getPatient() : null;
        this.selectedMother = infant != null ? infant.getMother() : null;
        this.insert = inserting;
        initManagers();
        initialize();
        if (!insert && infant != null) {
            loadExistingData();
        }
        updatePatientDisplay();
        updateMotherDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    public HIVInfantEdit(JFrame owner, Patient patient, boolean inserting) {
        super(owner, true);
        this.selectedPatient = patient;
        this.infant = new HIVInfant();
        if (patient != null) {
            this.infant.setPatient(patient);
        }
        this.infant.setStatus(HIVInfantStatus.ACTIVE);
        this.insert = inserting;
        initManagers();
        initialize();
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        infantManager = Context.getApplicationContext().getBean(HIVInfantManager.class);
        visitManager = Context.getApplicationContext().getBean(HIVVisitManager.class);
        patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.hiv.edit.new.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.hiv.edit.update.title"));
        }
        setMinimumSize(new Dimension(600, 550));
        setPreferredSize(new Dimension(650, 600));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (infant != null && !insert) {
            if (infant.getBirthWeight() != null) {
                birthWeightField.setText(String.valueOf(infant.getBirthWeight()));
            }
            if (infant.getGestationalAge() != null) {
                gestationalAgeField.setText(String.valueOf(infant.getGestationalAge()));
            }
            if (infant.getFeedingType() != null) {
                feedingCombo.setSelectedItem(infant.getFeedingType());
            }
            if (infant.getStatus() != null) {
                statusCombo.setSelectedItem(infant.getStatus());
            }
            if (infant.getFollowUpStartDate() != null) {
                followUpStartDateField.setDate(infant.getFollowUpStartDate());
            }
            if (infant.getFollowUpEndDate() != null) {
                followUpEndDateField.setDate(infant.getFollowUpEndDate());
            }
            if (infant.getNotes() != null) {
                notesArea.setText(infant.getNotes());
            }
        }
    }

    private void updatePatientDisplay() {
        if (selectedPatient != null) {
            patientSearchField.setText(selectedPatient.getFirstName() + " " + selectedPatient.getSecondName() + " (" + selectedPatient.getCode() + ")");
            patientSearchField.setEditable(false);
            patientSearchField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
            patientSearchField.setEnabled(false);

            pickPatientButton.setText(MessageBundle.getMessage("angal.hiv.button.change.infant"));
            pickPatientButton.setEnabled(true);
            trashPatientButton.setEnabled(true);
        } else {
            patientSearchField.setText("");
            patientSearchField.setEditable(true);
            patientSearchField.setBackground(UIManager.getColor("TextField.background"));
            patientSearchField.setEnabled(true);

            pickPatientButton.setText(MessageBundle.getMessage("angal.hiv.button.select.infant"));
            pickPatientButton.setEnabled(true);
            trashPatientButton.setEnabled(false);
        }
    }

    private void updateMotherDisplay() {
        if (selectedMother != null) {
            motherSearchField.setText(selectedMother.getFirstName() + " " + selectedMother.getSecondName() + " (" + selectedMother.getCode() + ")");
            motherSearchField.setEditable(false);
            motherSearchField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
            motherSearchField.setEnabled(false);

            pickMotherButton.setText(MessageBundle.getMessage("angal.hiv.button.change.mother"));
            pickMotherButton.setEnabled(true);
            trashMotherButton.setEnabled(true);
        } else {
            motherSearchField.setText("");
            motherSearchField.setEditable(true);
            motherSearchField.setBackground(UIManager.getColor("TextField.background"));
            motherSearchField.setEnabled(true);

            pickMotherButton.setText(MessageBundle.getMessage("angal.hiv.button.select.mother"));
            pickMotherButton.setEnabled(true);
            trashMotherButton.setEnabled(false);
        }
    }

    private void clearPatientDisplay() {
        selectedPatient = null;
        infant.setPatient(null);
        patientSearchField.setText("");
        patientSearchField.setEditable(true);
        patientSearchField.setBackground(UIManager.getColor("TextField.background"));
        patientSearchField.setEnabled(true);
        pickPatientButton.setText(MessageBundle.getMessage("angal.hiv.button.select.infant"));
        trashPatientButton.setEnabled(false);
    }

    private void clearMotherDisplay() {
        selectedMother = null;
        infant.setMother(null);
        motherSearchField.setText("");
        motherSearchField.setEditable(true);
        motherSearchField.setBackground(UIManager.getColor("TextField.background"));
        motherSearchField.setEnabled(true);
        pickMotherButton.setText(MessageBundle.getMessage("angal.hiv.button.select.mother"));
        trashMotherButton.setEnabled(false);
    }

    private void searchPatient(String searchText) {
        int maxAgeYears = (int) Math.ceil(GeneralData.HIV_INFANT_MAX_AGE_MONTHS / 12.0);
        SelectPatient sp = new SelectPatient(this, searchText, true, 0, maxAgeYears);
        sp.addSelectionListener(this);
        sp.setVisible(true);
    }

    private void searchMother(String searchText) {
        SelectPatient sp = new SelectPatient(this, searchText, true, false);
        sp.addSelectionListener(selectedMother -> {
            if (selectedMother != null) {
                HIVInfantEdit.this.selectedMother = selectedMother;
                infant.setMother(selectedMother);
                updateMotherDisplay();
            }
        });
        sp.setVisible(true);
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.add(getDatePanel());
            topPanel.add(getPatientPanel());
            contentPanel.add(topPanel);

            contentPanel.add(getDataPanel());

            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getVerticalScrollBar().setBlockIncrement(32);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }
        return mainPanel;
    }

    private JPanel getDatePanel() {
        if (datePanel == null) {
            datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            datePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.date.txt")));
            datePanel.add(getRegistrationDateField());
        }
        return datePanel;
    }

    private GoodDateTimeSpinnerChooser getRegistrationDateField() {
        if (registrationDateField == null) {
            LocalDateTime dateTime = TimeTools.getNow();
            registrationDateField = new GoodDateTimeSpinnerChooser(dateTime);
        }
        return registrationDateField;
    }

    private JPanel getPatientPanel() {
        if (patientPanel == null) {
            patientPanel = new JPanel(new GridBagLayout());
            patientPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.section.patient")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridy = 0;

            // Label Infant
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            patientPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.patient") + ":"), gbc);

            // Champ search Infant
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            patientSearchField = new JTextField(25);
            patientSearchField.setToolTipText(MessageBundle.getMessage("angal.hiv.tooltip.select.infant"));
            patientSearchField.setEditable(true);
            patientSearchField.setPreferredSize(new Dimension(250, 30));
            patientSearchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        searchPatient(patientSearchField.getText());
                    }
                }
            });
            patientPanel.add(patientSearchField, gbc);

            // Bouton Select Infant
            gbc.gridx = 3;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            pickPatientButton = new JButton(MessageBundle.getMessage("angal.hiv.button.select.infant"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.hiv.tooltip.select.infant"));
            pickPatientButton.setPreferredSize(new Dimension(180, 30));
            pickPatientButton.addActionListener(e -> searchPatient(patientSearchField.getText()));
            patientPanel.add(pickPatientButton, gbc);

            // Bouton suppression Infant
            gbc.gridx = 4;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            trashPatientButton = new JButton();
            trashPatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
            trashPatientButton.setToolTipText(MessageBundle.getMessage("angal.hiv.tooltip.remove.infant"));
            trashPatientButton.setPreferredSize(new Dimension(40, 30));
            trashPatientButton.setEnabled(false);
            trashPatientButton.addActionListener(e -> clearPatientDisplay());
            patientPanel.add(trashPatientButton, gbc);

            gbc.gridy = 1;

            // Label Mother
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            patientPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.mother") + ":"), gbc);

            // Champ search mother
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            motherSearchField = new JTextField(20);
            motherSearchField.setToolTipText(MessageBundle.getMessage("angal.hiv.tooltip.select.mother"));
            motherSearchField.setEditable(true);
            motherSearchField.setPreferredSize(new Dimension(250, 30));
            motherSearchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        searchMother(motherSearchField.getText());
                    }
                }
            });
            patientPanel.add(motherSearchField, gbc);

            // Bouton Select Mother
            gbc.gridx = 3;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            pickMotherButton = new JButton(MessageBundle.getMessage("angal.hiv.button.select.mother"));
            pickMotherButton.setToolTipText(MessageBundle.getMessage("angal.hiv.tooltip.select.mother"));
            pickMotherButton.setPreferredSize(new Dimension(180, 30));
            pickMotherButton.addActionListener(e -> searchMother(motherSearchField.getText()));
            patientPanel.add(pickMotherButton, gbc);

            // button delete mother
            gbc.gridx = 4;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            trashMotherButton = new JButton();
            trashMotherButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
            trashMotherButton.setToolTipText(MessageBundle.getMessage("angal.hiv.tooltip.remove.mother"));
            trashMotherButton.setPreferredSize(new Dimension(40, 30));
            trashMotherButton.setEnabled(false);
            trashMotherButton.addActionListener(e -> clearMotherDisplay());
            patientPanel.add(trashMotherButton, gbc);
        }
        return patientPanel;
    }

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new GridBagLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.section.birth")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // Birth Weight
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.birth.weight") + " (kg) *:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getBirthWeightField(), gbc);
            row++;

            // Gestational Age
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.gestational.age") + " (" + MessageBundle.getMessage("angal.hiv.label.weeks") + "):"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getGestationalAgeField(), gbc);
            row++;

            // Feeding
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.filter.feeding") + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getFeedingCombo(), gbc);
            row++;

            // Separator
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(new JSeparator(), gbc);
            row++;
            gbc.gridwidth = 1;

            // Status
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.status") + "*:"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getStatusCombo(), gbc);
            row++;

            // Follow-up Start Date
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.followup.start") + "*:"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getFollowUpStartDateField(), gbc);
            row++;

            // Follow-up End Date
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.followup.end") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getFollowUpEndDateField(), gbc);
            row++;

            // Notes
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.notes") + ":"), gbc);

            gbc.gridx = 1;
            gbc.weighty = 0.5;
            dataPanel.add(getNotesScrollPane(), gbc);
            row++;

            // Separator
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.weighty = 0.0;
            dataPanel.add(new JSeparator(), gbc);
            row++;
            gbc.gridwidth = 1;

            if (insert) {
                // First Visit Checkbox
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 2;
                dataPanel.add(getAddFirstVisitCheckBox(), gbc);
                row++;

                // First Visit Panel
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                dataPanel.add(getFirstVisitPanel(), gbc);
                row++;
            }

            // Add vertical filler
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.VERTICAL;
            dataPanel.add(new JPanel(), gbc);
        }
        return dataPanel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.add(getOkButton());
            buttonPanel.add(getCancelButton());
        }
        return buttonPanel;
    }

    private VoLimitedTextField getBirthWeightField() {
        if (birthWeightField == null) {
            birthWeightField = new VoLimitedTextField(5, 6);
            birthWeightField.setColumns(10);
        }
        return birthWeightField;
    }

    private VoLimitedTextField getGestationalAgeField() {
        if (gestationalAgeField == null) {
            gestationalAgeField = new VoLimitedTextField(3, 3);
            gestationalAgeField.setColumns(5);
        }
        return gestationalAgeField;
    }

    private JComboBox<FeedingType> getFeedingCombo() {
        if (feedingCombo == null) {
            feedingCombo = new JComboBox<>(FeedingType.values());
            feedingCombo.insertItemAt(null, 0);
            feedingCombo.setSelectedIndex(0);
        }
        return feedingCombo;
    }

    private JComboBox<HIVInfantStatus> getStatusCombo() {
        if (statusCombo == null) {
            statusCombo = new JComboBox<>(HIVInfantStatus.values());
            if (insert) {
                statusCombo.setSelectedItem(HIVInfantStatus.ACTIVE);
            }
        }
        return statusCombo;
    }

    private GoodDateChooser getFollowUpStartDateField() {
        if (followUpStartDateField == null) {
            followUpStartDateField = new GoodDateChooser(LocalDate.now());
        }
        return followUpStartDateField;
    }

    private GoodDateChooser getFollowUpEndDateField() {
        if (followUpEndDateField == null) {
            followUpEndDateField = new GoodDateChooser(null);
        }
        return followUpEndDateField;
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

    private JCheckBox getAddFirstVisitCheckBox() {
        if (addFirstVisitCheckBox == null) {
            addFirstVisitCheckBox = new JCheckBox(MessageBundle.getMessage("angal.hiv.label.add.first.visit"));
            addFirstVisitCheckBox.addActionListener(e -> {
                boolean selected = addFirstVisitCheckBox.isSelected();
                getFirstVisitPanel().setVisible(selected);
                pack();
            });
        }
        return addFirstVisitCheckBox;
    }

    private JPanel getFirstVisitPanel() {
        if (firstVisitPanel == null) {
            firstVisitPanel = new JPanel(new GridBagLayout());
            firstVisitPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.section.first.visit")));
            firstVisitPanel.setVisible(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // Visit Date
            gbc.gridx = 0;
            gbc.gridy = row;
            firstVisitPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.visitdate.col") + "*:"), gbc);

            gbc.gridx = 1;
            firstVisitPanel.add(getFirstVisitDateField(), gbc);
            row++;

            // Weight
            gbc.gridx = 0;
            gbc.gridy = row;
            firstVisitPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.weights") + " (kg)*:"), gbc);

            gbc.gridx = 1;
            firstVisitPanel.add(getFirstVisitWeightField(), gbc);
            row++;

            // Height
            gbc.gridx = 0;
            gbc.gridy = row;
            firstVisitPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.height") + " (cm):"), gbc);

            gbc.gridx = 1;
            firstVisitPanel.add(getFirstVisitHeightField(), gbc);
            row++;

            // Next Appointment
            gbc.gridx = 0;
            gbc.gridy = row;
            firstVisitPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.next.appointment") + ":"), gbc);

            gbc.gridx = 1;
            firstVisitPanel.add(getFirstVisitNextAppointmentField(), gbc);
            row++;

            // PCR
            gbc.gridx = 0;
            gbc.gridy = row;
            firstVisitPanel.add(new JLabel("PCR:"), gbc);

            gbc.gridx = 1;
            firstVisitPanel.add(getFirstVisitPcrCombo(), gbc);
            row++;

            // Notes
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.NONE;
            firstVisitPanel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.notes") + ":"), gbc);

            gbc.gridx = 1;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            firstVisitPanel.add(getFirstVisitNotesScrollPane(), gbc);
        }
        return firstVisitPanel;
    }

    private GoodDateChooser getFirstVisitDateField() {
        if (firstVisitDateField == null) {
            firstVisitDateField = new GoodDateChooser(LocalDate.now());
        }
        return firstVisitDateField;
    }

    private VoLimitedTextField getFirstVisitWeightField() {
        if (firstVisitWeightField == null) {
            firstVisitWeightField = new VoLimitedTextField(5, 6);
            firstVisitWeightField.setColumns(10);
        }
        return firstVisitWeightField;
    }

    private VoLimitedTextField getFirstVisitHeightField() {
        if (firstVisitHeightField == null) {
            firstVisitHeightField = new VoLimitedTextField(5, 6);
            firstVisitHeightField.setColumns(10);
        }
        return firstVisitHeightField;
    }

    private JComboBox<PCRResult> getFirstVisitPcrCombo() {
        if (firstVisitPcrCombo == null) {
            firstVisitPcrCombo = new JComboBox<>(PCRResult.values());
            firstVisitPcrCombo.insertItemAt(null, 0);
            firstVisitPcrCombo.setSelectedIndex(0);
        }
        return firstVisitPcrCombo;
    }

    private JTextArea getFirstVisitNotesArea() {
        if (firstVisitNotesArea == null) {
            firstVisitNotesArea = new JTextArea(8, 30);
            firstVisitNotesArea.setLineWrap(true);
            firstVisitNotesArea.setWrapStyleWord(true);
        }
        return firstVisitNotesArea;
    }

    private JScrollPane getFirstVisitNotesScrollPane() {
        JScrollPane scrollPane = new JScrollPane(getFirstVisitNotesArea());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(400, 120));
        return scrollPane;
    }

    private JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
            okButton.addActionListener(e -> saveInfant());
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

    private void saveInfant() {
        try {
            if (selectedPatient == null && (infant.getPatient() == null)) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectapatient.msg"));
                return;
            }

            LocalDateTime registrationDate = registrationDateField.getLocalDateTime();
            if (registrationDate == null) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseinsertavaliddate.msg"));
                return;
            }

            LocalDate followUpStart = followUpStartDateField.getDate();
            if (followUpStart == null) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseinsertavaliddate.msg"));
                return;
            }

            LocalDate followUpEnd = followUpEndDateField.getDate();
            if (followUpEnd != null && followUpEnd.isBefore(followUpStart)) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.hiv.message.followup.end.before.start"));
                return;
            }

            if (insert && addFirstVisitCheckBox.isSelected()
                    && firstVisitWeightField.getText().trim().isEmpty()) {
                MessageDialog.error(this,
                        MessageBundle.getMessage("angal.hiv.message.weight.required"));
                return;
            }
            if (selectedPatient != null) {
                infant.setPatient(selectedPatient);
            }

            if (selectedMother != null) {
                infant.setMother(selectedMother);
            }

            if (insert) {
                infant.setRegistrationDate(TimeTools.getNow());
            } else {
                infant.setRegistrationDate(registrationDate);
            }
            if (birthWeightField.getText().trim().isEmpty()) {
                MessageDialog.error(this,
                        MessageBundle.getMessage("angal.hiv.message.weight.required"));
                return;
            }
            if (!gestationalAgeField.getText().trim().isEmpty()) {
                infant.setGestationalAge(Integer.parseInt(gestationalAgeField.getText().trim()));
            }

            infant.setFeedingType((FeedingType) feedingCombo.getSelectedItem());
            infant.setStatus((HIVInfantStatus) statusCombo.getSelectedItem());
            infant.setFollowUpStartDate(followUpStart);
            infant.setFollowUpEndDate(followUpEndDateField.getDate());
            infant.setNotes(notesArea.getText());

            HIVInfant savedInfant;
            if (insert) {
                savedInfant = infantManager.newInfant(infant);
                fireInfantInserted(savedInfant);

                if (addFirstVisitCheckBox.isSelected()) {
                    HIVVisit firstVisit = new HIVVisit();
                    firstVisit.setHivInfant(savedInfant);
                    firstVisit.setVisitDate(firstVisitDateField.getDate().atStartOfDay());

                    firstVisit.setWeight(Double.parseDouble(firstVisitWeightField.getText().trim()));

                    if (!firstVisitHeightField.getText().trim().isEmpty()) {
                        firstVisit.setHeight(Double.parseDouble(firstVisitHeightField.getText().trim()));
                    }
                    firstVisit.setPcrResult((PCRResult) firstVisitPcrCombo.getSelectedItem());
                    firstVisit.setNotes(firstVisitNotesArea.getText());

                    if (firstVisitNextAppointmentField.getDate() != null) {
                        firstVisit.setNextAppointmentDate(firstVisitNextAppointmentField.getDate());
                    }

                    visitManager.newVisit(firstVisit);
                }
                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.hiv.message.save.success"));
                dispose();
            } else {
                savedInfant = infantManager.updateInfant(infant);
                fireInfantUpdated(savedInfant);

                MessageDialog.info(this,
                        MessageBundle.getMessage("angal.hiv.message.save.success"));
                dispose();
            }

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseentervalidnumbers.msg"));
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    @Override
    public void patientSelected(Patient patient) {
        if (patient != null) {
            int ageInMonths = patient.getAge();
            if (ageInMonths > GeneralData.HIV_INFANT_MAX_AGE_MONTHS) {
                MessageDialog.error(this, MessageBundle.formatMessage("angal.hiv.message.age.constraint", GeneralData.HIV_INFANT_MAX_AGE_MONTHS));
                return;
            }
            this.selectedPatient = patient;
            this.infant.setPatient(patient);
            updatePatientDisplay();
            loadNewbornInfo(patient);
        }
    }

    @Override
    public void patientUpdated(AWTEvent e) {
        Patient updatedPatient = (Patient) e.getSource();
        patientSelected(updatedPatient);
    }

    @Override
    public void patientInserted(AWTEvent e) {
        Patient newPatient = (Patient) e.getSource();
        patientSelected(newPatient);
    }

    private void loadNewbornInfo(Patient patient) {
        try {
            NewBornBrowserManager newbornManager = Context.getApplicationContext().getBean(NewBornBrowserManager.class);
            Optional<Newborn> newbornOpt = newbornManager.findByPatientCode(patient.getCode());

            if (newbornOpt.isPresent()) {
                Newborn nb = newbornOpt.get();

                if (nb.getBirthWeight() != null) {
                    birthWeightField.setText(String.valueOf(nb.getBirthWeight()));
                }

                if (nb.getDelivery() != null && nb.getDelivery().getPregnancy() != null) {
                    Patient mother = nb.getDelivery().getPregnancy().getPatient();
                    if (mother != null) {
                        selectedMother = mother;
                        infant.setMother(mother);
                        updateMotherDisplay();
                    }
                }
            }
        } catch (OHServiceException e) {

        }
    }

    private GoodDateChooser getFirstVisitNextAppointmentField() {
        if (firstVisitNextAppointmentField == null) {
            firstVisitNextAppointmentField = new GoodDateChooser(LocalDate.now().plusMonths(1));
        }
        return firstVisitNextAppointmentField;
    }
}