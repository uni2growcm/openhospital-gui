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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyBrowserManager;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyStatus;
import org.isf.maternity.model.RiskLevel;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class MaternityPregnancyEdit extends JDialog implements SelectionListener {

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
    private JPanel patientPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private JTextField patientCodeField;
    private JTextField patientNameField;
    private JButton searchPatientButton;

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

    public MaternityPregnancyEdit(JFrame owner, Pregnancy pregnancy, boolean inserting) {
        super(owner, true);
        this.pregnancy = pregnancy;
        this.selectedPatient = pregnancy.getPatient();
        this.insert = inserting;
        initManagers();
        initialize();
        if (!insert) {
            loadExistingData();
        }
        pack();
        setLocationRelativeTo(owner);
    }

    public MaternityPregnancyEdit(JFrame owner, Patient patient, boolean inserting) {
        super(owner, true);
        this.selectedPatient = patient;
        this.pregnancy = new Pregnancy();
        this.pregnancy.setPatient(patient);
        this.pregnancy.setStatus(PregnancyStatus.ONGOING);
        this.pregnancy.setRiskLevel(RiskLevel.LOW);
        this.pregnancy.setMiscarriages(0);
        this.insert = inserting;
        initManagers();
        initialize();
        if (selectedPatient != null) {
            updatePatientDisplay();
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        pregnancyManager = Context.getApplicationContext().getBean(PregnancyBrowserManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.maternity.newpregnancy.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.maternity.editpregnancy.title"));
        }
        setMinimumSize(new Dimension(550, 520));
        setPreferredSize(new Dimension(600, 540));
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
            updatePatientDisplay();
        }
    }

    private void updatePatientDisplay() {
        if (selectedPatient != null) {
            patientCodeField.setText(String.valueOf(selectedPatient.getCode()));
            patientNameField.setText(selectedPatient.getSecondName() + " " + selectedPatient.getFirstName());
        }
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(getPatientPanel());
            mainPanel.add(getDataPanel());
        }
        return mainPanel;
    }

    private JPanel getPatientPanel() {
        if (patientPanel == null) {
            patientPanel = new JPanel(new GridBagLayout());
            patientPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.patient.info")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Code Patient
            gbc.gridx = 0;
            gbc.gridy = 0;
            patientPanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"), gbc);

            gbc.gridx = 1;
            patientCodeField = new JTextField(8);
            patientCodeField.setEditable(false);
            patientPanel.add(patientCodeField, gbc);

            // Nom Patient
            gbc.gridx = 2;
            patientPanel.add(new JLabel(MessageBundle.getMessage("angal.common.name.txt") + ":"), gbc);

            gbc.gridx = 3;
            patientNameField = new JTextField(15);
            patientNameField.setEditable(false);
            patientPanel.add(patientNameField, gbc);

            // Bouton Search
            gbc.gridx = 4;
            searchPatientButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
            searchPatientButton.addActionListener(e -> openPatientSearch());
            patientPanel.add(searchPatientButton, gbc);

            if (selectedPatient != null) {
                updatePatientDisplay();
            }
        }
        return patientPanel;
    }

    private void openPatientSearch() {
        SelectPatient sp = new SelectPatient(this, selectedPatient);
        sp.addSelectionListener(this);
        sp.pack();
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

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new SpringLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.pregnancy.data.border")));

            // LMP (Last Menstrual Period)
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.lmp.col") + ":"));
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
            LocalDate date = insert ? LocalDate.now().minusWeeks(12) : null;
            if (date == null && pregnancy.getLmp() != null) {
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

            if (selectedPatient != null) {
                pregnancy.setPatient(selectedPatient);
            }

            LocalDate lmpDate = lmpDateField.getDate();
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