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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
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
        setMinimumSize(new Dimension(550, 550));
        setPreferredSize(new Dimension(600, 570));
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
        SelectPatient sp = new SelectPatient(this, searchText, true);
        sp.addSelectionListener(this);
        sp.setVisible(true);
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(getDatePanel());
            mainPanel.add(getPatientPanel());
            mainPanel.add(getDataPanel());
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
            patientSearchField = new JTextField(25);
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
        SelectPatient sp = new SelectPatient(this, searchText, true);
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
            dataPanel = new JPanel(new SpringLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.pregnancy.data.border")));

            // LMP (Last Menstrual Period) - Obligatoire
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.lmp.col") + " * :"));
            dataPanel.add(getLmpDateField());

            // EDD LMP (Estimated Delivery Date from LMP)
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.edd.col") + " (LMP):"));
            dataPanel.add(getEddLmpDateField());

            // EDD Scan (Estimated Delivery Date from Ultrasound)
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.edd.col") + " (Scan):"));
            dataPanel.add(getEddScanDateField());

            // Gravidity
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.gravidity.col") + ":"));
            dataPanel.add(getGravidityField());

            // Parity
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.parity.col") + ":"));
            dataPanel.add(getParityField());

            // Miscarriages
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.miscarriages.col") + ":"));
            dataPanel.add(getMiscarriagesField());

            // Risk Level
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.risklevel.col") + ":"));
            dataPanel.add(getRiskLevelCombo());

            // Status
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.status.col") + ":"));
            dataPanel.add(getStatusCombo());

            SpringUtilities.makeCompactGrid(dataPanel, 8, 2, 8, 12, 8, 12);
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
}