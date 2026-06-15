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
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyBrowserManager;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyStatus;
import org.isf.maternity.model.RiskLevel;
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
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.time.TimeTools;

public class MaternityPregnancyEdit extends JDialog implements SelectionListener, PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

    private static final long serialVersionUID = 1L;

    private EventListenerList pregnancyListeners = new EventListenerList();

    public interface MaternityPregnancyListener extends EventListener {
        void pregnancyInserted(AWTEvent e, Pregnancy pregnancy);
        void pregnancyUpdated(AWTEvent e, Pregnancy pregnancy);
    }

    public void addMaternityPregnancyListener(MaternityPregnancyListener l) {
        pregnancyListeners.add(MaternityPregnancyListener.class, l);
    }

    public void removeMaternityPregnancyListener(MaternityPregnancyListener listener) {
        pregnancyListeners.remove(MaternityPregnancyListener.class, listener);
    }

    private void firePregnancyInserted(Pregnancy pregnancy) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = pregnancyListeners.getListeners(MaternityPregnancyListener.class);
        for (EventListener listener : listeners) {
            ((MaternityPregnancyListener) listener).pregnancyInserted(event, pregnancy);
        }
    }

    private void firePregnancyUpdated(Pregnancy pregnancy) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = pregnancyListeners.getListeners(MaternityPregnancyListener.class);
        for (EventListener listener : listeners) {
            ((MaternityPregnancyListener) listener).pregnancyUpdated(event, pregnancy);
        }
    }

    private JPanel mainPanel;
    private JPanel datePanel;
    private JPanel patientPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private GoodDateTimeSpinnerChooser jCalendarDate;

    private JTextField patientSearchField;
    private JButton pickPatientButton;
    private JButton trashPatientButton;

    private GoodDateChooser lmpDateField;
    private GoodDateChooser eddLmpDateField;
    private GoodDateChooser eddScanDateField;
    private JTextField gravidityField;
    private JTextField parityField;
    private JTextField miscarriagesField;
    private JComboBox<RiskLevel> riskLevelCombo;
    private JComboBox<PregnancyStatus> statusCombo;

    private Pregnancy pregnancy;
    private Patient selectedPatient;
    private boolean insert;
    private JTextField termDeliveriesField;
    private JTextField pretermDeliveriesField;
    private JTextField livingChildrenField;
    private JTextField stillbirthsField;
    private JTextField deceasedChildrenField;
    private JTextField desiredChildrenField;
    private JCheckBox breastfeedingCheckBox;
    private JTextField lastChildYearsField;
    private JTextField lastChildMonthsField;
    private JTextField lastChildWeeksField;
    private JTextField lastChildDaysField;

    private PregnancyBrowserManager pregnancyManager;
    private PatientBrowserManager patientManager;

    public MaternityPregnancyEdit(JFrame owner, Pregnancy pregnancy, boolean inserting) {
        super(owner, true);
        if (pregnancy == null) {
           MessageDialog.error(this, "angal.maternity.pregnancycannotbenull.msg");
           dispose();
           return;
        }
        this.pregnancy = pregnancy;
        this.selectedPatient = pregnancy.getPatient();
        this.insert = inserting;
        initManagers();
        initialize();
        if (!insert) {
            loadExistingData();
        }
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    public MaternityPregnancyEdit(JFrame owner, Patient patient, boolean inserting) {
        super(owner, true);
        this.selectedPatient = patient;
        this.pregnancy = new Pregnancy();
        if (patient != null) {
            this.pregnancy.setPatient(patient);
        }
        this.pregnancy.setStatus(PregnancyStatus.ONGOING);
        this.pregnancy.setRiskLevel(RiskLevel.LOW);
        this.pregnancy.setMiscarriages(0);
        this.insert = inserting;
        initManagers();
        initialize();
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        pregnancyManager = Context.getApplicationContext().getBean(PregnancyBrowserManager.class);
        patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.maternity.newpregnancy.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.maternity.editpregnancy.title"));
        }
        setMinimumSize(new Dimension(700, 550));
        setPreferredSize(new Dimension(750, 570));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (pregnancy != null && !insert) {
            if (pregnancy.getLmp() != null) {
                lmpDateField.setDate(pregnancy.getLmp().toLocalDate());
            }
            if (pregnancy.getEddLmp() != null) {
                eddLmpDateField.setDate(pregnancy.getEddLmp().toLocalDate());
            }
            if (pregnancy.getEddScan() != null) {
                eddScanDateField.setDate(pregnancy.getEddScan().toLocalDate());
            }
            if (pregnancy.getGravidity() != null) {
                gravidityField.setText(String.valueOf(pregnancy.getGravidity()));
            }
            if (pregnancy.getParity() != null) {
                parityField.setText(String.valueOf(pregnancy.getParity()));
            }
            if (pregnancy.getMiscarriages() != null) {
                miscarriagesField.setText(String.valueOf(pregnancy.getMiscarriages()));
            }
            if (pregnancy.getRiskLevel() != null) {
                riskLevelCombo.setSelectedItem(pregnancy.getRiskLevel());
            }
            if (pregnancy.getStatus() != null) {
                statusCombo.setSelectedItem(pregnancy.getStatus());
            }
            if (pregnancy.getTermDeliveries() != null) {
                termDeliveriesField.setText(String.valueOf(pregnancy.getTermDeliveries()));
            }
            if (pregnancy.getPretermDeliveries() != null) {
                pretermDeliveriesField.setText(String.valueOf(pregnancy.getPretermDeliveries()));
            }
            if (pregnancy.getLivingChildren() != null) {
                livingChildrenField.setText(String.valueOf(pregnancy.getLivingChildren()));
            }
            if (pregnancy.getStillbirths() != null) {
                stillbirthsField.setText(String.valueOf(pregnancy.getStillbirths()));
            }
            if (pregnancy.getDeceasedChildren() != null) {
                deceasedChildrenField.setText(String.valueOf(pregnancy.getDeceasedChildren()));
            }
            if (pregnancy.getDesiredChildren() != null) {
                desiredChildrenField.setText(String.valueOf(pregnancy.getDesiredChildren()));
            }
            if (pregnancy.getBreastfeeding() != null) {
                breastfeedingCheckBox.setSelected("Y".equals(pregnancy.getBreastfeeding()));
            }
            if (pregnancy.getLastChildYears() != null) {
                lastChildYearsField.setText(String.valueOf(pregnancy.getLastChildYears()));
            }
            if (pregnancy.getLastChildMonths() != null) {
                lastChildMonthsField.setText(String.valueOf(pregnancy.getLastChildMonths()));
            }
            if (pregnancy.getLastChildWeeks() != null) {
                lastChildWeeksField.setText(String.valueOf(pregnancy.getLastChildWeeks()));
            }
            if (pregnancy.getLastChildDays() != null) {
                lastChildDaysField.setText(String.valueOf(pregnancy.getLastChildDays()));
            }

        }
    }

    private void updatePatientDisplay() {
        if (selectedPatient != null) {
            patientSearchField.setText(selectedPatient.getSecondName() + " " + selectedPatient.getFirstName());
            patientSearchField.setEditable(false);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.changepatient"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.changethepatientassociatedwiththisexams"));
            trashPatientButton.setEnabled(true);
        } else {
            patientSearchField.setText("");
            patientSearchField.setEditable(true);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
            trashPatientButton.setEnabled(false);
        }
    }

    private void clearPatientDisplay() {
        selectedPatient = null;
        pregnancy.setPatient(null);
        patientSearchField.setText("");
        patientSearchField.setEditable(true);
        pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
        pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
        trashPatientButton.setEnabled(false);
    }

    private void searchPatient(String searchText) {
        SelectPatient sp = new SelectPatient(this, searchText, true, true);
        sp.addSelectionListener(this);
        sp.setVisible(true);
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.add(getDatePanel());
            topPanel.add(getPatientPanel());

            JScrollPane scrollPane = new JScrollPane(getDataPanel());
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }
        return mainPanel;
    }

    private JPanel getDatePanel() {
        if (datePanel == null) {
            datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            datePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.date.txt")));
            datePanel.add(getJCalendarDate());
        }
        return datePanel;
    }

    private GoodDateTimeSpinnerChooser getJCalendarDate() {
        if (jCalendarDate == null) {
            LocalDateTime dateTime = TimeTools.getNow();
            jCalendarDate = new GoodDateTimeSpinnerChooser(dateTime);
        }
        return jCalendarDate;
    }

    private JPanel getPatientPanel() {
        if (patientPanel == null) {
            patientPanel = new JPanel(new GridBagLayout());
            patientPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.patient.info")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            patientPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + ":"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 2;
            patientSearchField = new JTextField(40);
            patientSearchField.setPreferredSize(new Dimension(250, 20));
            patientSearchField.setMinimumSize(new Dimension(250, 20));
            patientSearchField.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
            patientSearchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        searchPatient(patientSearchField.getText());
                    }
                }
            });
            patientPanel.add(patientSearchField, gbc);

            // Pick Patient Button
            gbc.gridx = 3;
            gbc.gridwidth = 1;
            pickPatientButton = new JButton();
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
            pickPatientButton.addActionListener(e -> openPatientSearch());
            patientPanel.add(pickPatientButton, gbc);

            // Trash Button
            gbc.gridx = 4;
            trashPatientButton = new JButton();
            trashPatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
            trashPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.removepatientassociationwiththisexam"));
            trashPatientButton.setEnabled(selectedPatient != null);
            trashPatientButton.addActionListener(e -> clearPatientDisplay());
            patientPanel.add(trashPatientButton, gbc);

        }
        return patientPanel;
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
            this.pregnancy.setPatient(patient);
            updatePatientDisplay();
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

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new GridBagLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(
                    MessageBundle.getMessage("angal.maternity.pregnancy.data.border")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            //LMP (Last Menstrual Period) - Mandatory
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.lmp.col") + " * :"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getLmpDateField(), gbc);
            row++;

            //EDD LMP
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.edd.col") + " (LMP):"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getEddLmpDateField(), gbc);
            row++;

            //EDD Scan
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.edd.col") + " (Scan):"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getEddScanDateField(), gbc);
            row++;

            // Separator
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            dataPanel.add(new JSeparator(), gbc);
            row++;
            gbc.gridwidth = 1;

            //Gravidity
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.gravidity.col") + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getGravidityField(), gbc);
            row++;

            //Parity
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.parity.col") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getParityField(), gbc);
            row++;

            //Miscarriages
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.miscarriages.col") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getMiscarriagesField(), gbc);
            row++;

            //Term Deliveries
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.term.deliveries") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getTermDeliveriesField(), gbc);
            row++;

            //Preterm Deliveries
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.preterm.deliveries") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getPretermDeliveriesField(), gbc);
            row++;

            //Living Children
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.living.children") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getLivingChildrenField(), gbc);
            row++;

            //Stillbirths
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.stillbirths") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getStillbirthsField(), gbc);
            row++;

            //Deceased Children
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.deceased.children") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getDeceasedChildrenField(), gbc);
            row++;

            //Desired Children
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.desired.children") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getDesiredChildrenField(), gbc);
            row++;

            //Breastfeeding
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.breastfeeding") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getBreastfeedingCheckBox(), gbc);
            row++;

            //Last Child Age
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.last.child.age") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getLastChildAgePanel(), gbc);
            row++;

            //Risk Level
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.risklevel.col") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getRiskLevelCombo(), gbc);
            row++;

            //Status
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.status.col") + ":"), gbc);

            gbc.gridx = 1;
            dataPanel.add(getStatusCombo(), gbc);
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

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.add(getOkButton());
            buttonPanel.add(getCancelButton());
        }
        return buttonPanel;
    }

    private GoodDateChooser getLmpDateField() {
        if (lmpDateField == null) {
            LocalDate date = null;

            if (!insert && pregnancy.getLmp() != null) {
                date = pregnancy.getLmp().toLocalDate();
            }

            lmpDateField = new GoodDateChooser(date);
            lmpDateField.setMaxDate(LocalDate.now());
            lmpDateField.addDateChangeListener(e -> calculateEddFromLmp());
        }
        return lmpDateField;
    }

    private GoodDateChooser getEddLmpDateField() {
        if (eddLmpDateField == null) {
            LocalDate date = null;
            if (pregnancy.getEddLmp() != null) {
                date = pregnancy.getEddLmp().toLocalDate();
            }
            eddLmpDateField = new GoodDateChooser(date);
        }
        return eddLmpDateField;
    }

    private GoodDateChooser getEddScanDateField() {
        if (eddScanDateField == null) {
            LocalDate date = null;
            if (pregnancy.getEddScan() != null) {
                date = pregnancy.getEddScan().toLocalDate();
            }
            eddScanDateField = new GoodDateChooser(date);
        }
        return eddScanDateField;
    }

    private void calculateEddFromLmp() {
        LocalDate lmp = lmpDateField.getDate();
        if (lmp != null) {
            LocalDate edd = lmp.plusDays(280);
            eddLmpDateField.setDate(edd);
        }
    }

    private JTextField getGravidityField() {
        if (gravidityField == null) {
            gravidityField = new VoLimitedTextField(2, 2);
            gravidityField.setColumns(5);
            if (insert) {
                gravidityField.setText("1");
            }
        }
        return gravidityField;
    }

    private JTextField getParityField() {
        if (parityField == null) {
            parityField = new VoLimitedTextField(2, 2);
            parityField.setColumns(5);
            if (insert) {
                parityField.setText("0");
            }
        }
        return parityField;
    }

    private JTextField getMiscarriagesField() {
        if (miscarriagesField == null) {
            miscarriagesField = new VoLimitedTextField(2, 2);
            miscarriagesField.setColumns(5);
            if (insert) {
                miscarriagesField.setText("0");
            }
        }
        return miscarriagesField;
    }

    private JComboBox<RiskLevel> getRiskLevelCombo() {
        if (riskLevelCombo == null) {
            riskLevelCombo = new JComboBox<>(RiskLevel.values());
            if (insert) {
                riskLevelCombo.setSelectedItem(RiskLevel.LOW);
            }
        }
        return riskLevelCombo;
    }

    private JComboBox<PregnancyStatus> getStatusCombo() {
        if (statusCombo == null) {
            statusCombo = new JComboBox<>(PregnancyStatus.values());
            if (insert) {
                statusCombo.setSelectedItem(PregnancyStatus.ONGOING);
            }
        }
        return statusCombo;
    }

    private JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
            okButton.addActionListener(e -> savePregnancy());
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

    private void savePregnancy() {
        try {
            if (selectedPatient == null && (pregnancy.getPatient() == null)) {
                MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
                return;
            }

            LocalDate lmpDate = lmpDateField.getDate();
            if (lmpDate == null) {
                MessageDialog.error(this, "angal.maternity.lmp.required.msg");
                return;
            }

            if (selectedPatient != null) {
                pregnancy.setPatient(selectedPatient);
            }

            if (insert) {
                pregnancy.setDate(LocalDateTime.now());
            }

            // === DATE FIELDS ===
            if (lmpDate != null) {
                pregnancy.setLmp(lmpDate.atStartOfDay());
            }

            LocalDate eddLmpDate = eddLmpDateField.getDate();
            if (eddLmpDate != null) {
                pregnancy.setEddLmp(eddLmpDate.atStartOfDay());
            }

            LocalDate eddScanDate = eddScanDateField.getDate();
            if (eddScanDate != null) {
                pregnancy.setEddScan(eddScanDate.atStartOfDay());
            }

            if (!gravidityField.getText().trim().isEmpty()) {
                pregnancy.setGravidity(Integer.parseInt(gravidityField.getText()));
            }

            if (!parityField.getText().trim().isEmpty()) {
                pregnancy.setParity(Integer.parseInt(parityField.getText()));
            }

            if (!miscarriagesField.getText().trim().isEmpty()) {
                pregnancy.setMiscarriages(Integer.parseInt(miscarriagesField.getText()));
            }

            if (!termDeliveriesField.getText().trim().isEmpty()) {
                pregnancy.setTermDeliveries(Integer.parseInt(termDeliveriesField.getText()));
            }

            if (!pretermDeliveriesField.getText().trim().isEmpty()) {
                pregnancy.setPretermDeliveries(Integer.parseInt(pretermDeliveriesField.getText()));
            }

            if (!livingChildrenField.getText().trim().isEmpty()) {
                pregnancy.setLivingChildren(Integer.parseInt(livingChildrenField.getText()));
            }

            if (!stillbirthsField.getText().trim().isEmpty()) {
                pregnancy.setStillbirths(Integer.parseInt(stillbirthsField.getText()));
            }

            if (!deceasedChildrenField.getText().trim().isEmpty()) {
                pregnancy.setDeceasedChildren(Integer.parseInt(deceasedChildrenField.getText()));
            }

            if (!desiredChildrenField.getText().trim().isEmpty()) {
                pregnancy.setDesiredChildren(Integer.parseInt(desiredChildrenField.getText()));
            }

            pregnancy.setBreastfeeding(breastfeedingCheckBox.isSelected() ? "Y" : "N");

            if (!lastChildYearsField.getText().trim().isEmpty()) {
                pregnancy.setLastChildYears(Integer.parseInt(lastChildYearsField.getText()));
            }
            if (!lastChildMonthsField.getText().trim().isEmpty()) {
                pregnancy.setLastChildMonths(Integer.parseInt(lastChildMonthsField.getText()));
            }
            if (!lastChildWeeksField.getText().trim().isEmpty()) {
                pregnancy.setLastChildWeeks(Integer.parseInt(lastChildWeeksField.getText()));
            }
            if (!lastChildDaysField.getText().trim().isEmpty()) {
                pregnancy.setLastChildDays(Integer.parseInt(lastChildDaysField.getText()));
            }

            pregnancy.setRiskLevel((RiskLevel) riskLevelCombo.getSelectedItem());
            pregnancy.setStatus((PregnancyStatus) statusCombo.getSelectedItem());

            if (insert) {
                Pregnancy saved = pregnancyManager.newPregnancy(pregnancy);
                if (saved != null) {
                    firePregnancyInserted(saved);
                    dispose();
                } else {
                    MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
                }
            } else {
                Pregnancy updated = pregnancyManager.updatePregnancy(pregnancy);
                if (updated != null) {
                    firePregnancyUpdated(updated);
                    dispose();
                } else {
                    MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
                }
            }

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, "angal.common.pleaseentervalidnumbers.msg");
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private JTextField getTermDeliveriesField() {
        if (termDeliveriesField == null) {
            termDeliveriesField = new VoLimitedTextField(3, 2);
            termDeliveriesField.setColumns(5);
            if (insert) termDeliveriesField.setText("0");
        }
        return termDeliveriesField;
    }

    private JTextField getPretermDeliveriesField() {
        if (pretermDeliveriesField == null) {
            pretermDeliveriesField = new VoLimitedTextField(3, 2);
            pretermDeliveriesField.setColumns(5);
            if (insert) pretermDeliveriesField.setText("0");
        }
        return pretermDeliveriesField;
    }

    private JTextField getLivingChildrenField() {
        if (livingChildrenField == null) {
            livingChildrenField = new VoLimitedTextField(3, 2);
            livingChildrenField.setColumns(5);
            if (insert) livingChildrenField.setText("0");
        }
        return livingChildrenField;
    }

    private JTextField getStillbirthsField() {
        if (stillbirthsField == null) {
            stillbirthsField = new VoLimitedTextField(3, 2);
            stillbirthsField.setColumns(5);
            if (insert) stillbirthsField.setText("0");
        }
        return stillbirthsField;
    }

    private JTextField getDeceasedChildrenField() {
        if (deceasedChildrenField == null) {
            deceasedChildrenField = new VoLimitedTextField(3, 2);
            deceasedChildrenField.setColumns(5);
            if (insert) deceasedChildrenField.setText("0");
        }
        return deceasedChildrenField;
    }

    private JTextField getDesiredChildrenField() {
        if (desiredChildrenField == null) {
            desiredChildrenField = new VoLimitedTextField(3, 2);
            desiredChildrenField.setColumns(5);
            if (insert) desiredChildrenField.setText("");
        }
        return desiredChildrenField;
    }

    private JCheckBox getBreastfeedingCheckBox() {
        if (breastfeedingCheckBox == null) {
            breastfeedingCheckBox = new JCheckBox();
            breastfeedingCheckBox.setText(MessageBundle.getMessage("angal.common.yes.txt"));
        }
        return breastfeedingCheckBox;
    }

    private JPanel getLastChildAgePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.add(getLastChildYearsField());
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.years.txt")));
        panel.add(getLastChildMonthsField());
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.months.txt")));
        panel.add(getLastChildWeeksField());
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.weeks.txt")));
        panel.add(getLastChildDaysField());
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.days.txt")));
        return panel;
    }

    private JTextField getLastChildYearsField() {
        if (lastChildYearsField == null) {
            lastChildYearsField = new VoLimitedTextField(3, 2);
            lastChildYearsField.setColumns(3);
            if (insert) lastChildYearsField.setText("");
        }
        return lastChildYearsField;
    }

    private JTextField getLastChildMonthsField() {
        if (lastChildMonthsField == null) {
            lastChildMonthsField = new VoLimitedTextField(2, 2);
            lastChildMonthsField.setColumns(3);
            if (insert) lastChildMonthsField.setText("");
        }
        return lastChildMonthsField;
    }

    private JTextField getLastChildWeeksField() {
        if (lastChildWeeksField == null) {
            lastChildWeeksField = new VoLimitedTextField(2, 2);
            lastChildWeeksField.setColumns(3);
            if (insert) lastChildWeeksField.setText("");
        }
        return lastChildWeeksField;
    }

    private JTextField getLastChildDaysField() {
        if (lastChildDaysField == null) {
            lastChildDaysField = new VoLimitedTextField(2, 2);
            lastChildDaysField.setColumns(3);
            if (insert) lastChildDaysField.setText("");
        }
        return lastChildDaysField;
    }
}