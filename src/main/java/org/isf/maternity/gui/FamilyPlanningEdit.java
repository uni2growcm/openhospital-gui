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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.FamilyPlanningBrowserManager;
import org.isf.maternity.model.FPStatus;
import org.isf.maternity.model.FamilyPlanning;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;

public class FamilyPlanningEdit extends JDialog
        implements SelectionListener, PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final EventListenerList fpListeners = new EventListenerList();

    public interface FamilyPlanningListener extends EventListener {
        void fpInserted(AWTEvent e, FamilyPlanning fp);
        void fpUpdated(AWTEvent e, FamilyPlanning fp);
    }

    public void addFamilyPlanningListener(FamilyPlanningListener l) {
        fpListeners.add(FamilyPlanningListener.class, l);
    }

    public void removeFamilyPlanningListener(FamilyPlanningListener listener) {
        fpListeners.remove(FamilyPlanningListener.class, listener);
    }

    private void fireFPInserted(FamilyPlanning fp) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            @Serial
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : fpListeners.getListeners(FamilyPlanningListener.class)) {
            ((FamilyPlanningListener) listener).fpInserted(event, fp);
        }
    }

    private void fireFPUpdated(FamilyPlanning fp) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            @Serial
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : fpListeners.getListeners(FamilyPlanningListener.class)) {
            ((FamilyPlanningListener) listener).fpUpdated(event, fp);
        }
    }

    private JPanel mainPanel;
    private JPanel patientPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private JTextField patientSearchField;
    private JButton pickPatientButton;
    private JButton trashPatientButton;

    private JComboBox<Typology> methodCombo;
    private GoodDateChooser registrationDateField;
    private JComboBox<FPStatus> statusCombo;
    private JTextArea notesArea;

    private FamilyPlanning fp;
    private Patient selectedPatient;
    private final boolean insert;

    private FamilyPlanningBrowserManager fpManager;
    private PatientBrowserManager patientManager;

    private List<Typology> methodTypologies;

    public FamilyPlanningEdit(JFrame owner, FamilyPlanning fp, boolean inserting) {
        super(owner, true);
        this.insert = inserting;
        if (fp == null) {
            MessageDialog.error(this, "angal.maternity.fpcannotbenull.msg");
            dispose();
            return;
        }
        this.fp = fp;
        this.selectedPatient = fp.getPatient();
        initManagers();
        initialize();
        if (!insert) {
            loadExistingData();
        }
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    public FamilyPlanningEdit(JFrame owner, Patient patient, boolean inserting) {
        super(owner, true);
        this.selectedPatient = patient;
        this.fp = new FamilyPlanning();
        if (patient != null) {
            this.fp.setPatient(patient);
        }
        this.fp.setStatus(FPStatus.ACTIVE);
        this.insert = inserting;
        initManagers();
        initialize();
        updatePatientDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        fpManager = Context.getApplicationContext().getBean(FamilyPlanningBrowserManager.class);
        patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
        loadMethodTypologies();
    }

    private void loadMethodTypologies() {
        try {
            methodTypologies = Context.getApplicationContext()
                    .getBean(TypologyBrowserManager.class)
                    .getTypologies(Family.FAMILYPLANNINGMETHODTYPE);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            methodTypologies = List.of();
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.familyplanning.new.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.familyplanning.edit.title"));
        }
        setMinimumSize(new Dimension(700, 600));
        setPreferredSize(new Dimension(750, 650));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (fp != null && !insert) {
            if (fp.getCurrentMethod() != null) {
                methodCombo.setSelectedItem(fp.getCurrentMethod());
            }
            if (fp.getRegistrationDate() != null) {
                registrationDateField.setDate(fp.getRegistrationDate().toLocalDate());
            }
            if (fp.getStatus() != null) {
                statusCombo.setSelectedItem(fp.getStatus());
            }
            if (fp.getNotes() != null) {
                notesArea.setText(fp.getNotes());
            }
        }
    }

    private void updatePatientDisplay() {
        if (selectedPatient != null) {
            patientSearchField.setText(selectedPatient.getSecondName() + " " + selectedPatient.getFirstName());
            patientSearchField.setEditable(false);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.changepatient"));
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.changethepatientassociatedwiththisexams"));
            trashPatientButton.setEnabled(true);
        } else {
            patientSearchField.setText("");
            patientSearchField.setEditable(true);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
            trashPatientButton.setEnabled(false);
        }
    }

    private void clearPatientDisplay() {
        selectedPatient = null;
        fp.setPatient(null);
        patientSearchField.setText("");
        patientSearchField.setEditable(true);
        pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
        pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
        pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
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
            this.fp.setPatient(patient);
            updatePatientDisplay();
        }
    }

    @Override
    public void patientInserted(AWTEvent e) {
        if (e.getSource() instanceof Patient patient) {
            this.selectedPatient = patient;
            this.fp.setPatient(patient);
            updatePatientDisplay();
        }
    }

    @Override
    public void patientUpdated(AWTEvent e) {
        if (e.getSource() instanceof Patient patient) {
            this.selectedPatient = patient;
            this.fp.setPatient(patient);
            updatePatientDisplay();
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
            patientPanel.setBorder(BorderFactory.createTitledBorder(
                    MessageBundle.getMessage("angal.familyplanning.patient.label")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            patientPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + ":"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 2;
            patientSearchField = new JTextField(40);
            patientSearchField.setPreferredSize(new Dimension(300, 30));
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

            gbc.gridx = 3;
            gbc.gridwidth = 1;
            pickPatientButton = new JButton(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));
            pickPatientButton.addActionListener(e -> openPatientSearch());
            patientPanel.add(pickPatientButton, gbc);

            gbc.gridx = 4;
            gbc.gridwidth = 1;
            trashPatientButton = new JButton();
            trashPatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
            trashPatientButton.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.removepatientassociationwiththisexam"));
            trashPatientButton.setEnabled(selectedPatient != null);
            trashPatientButton.addActionListener(e -> clearPatientDisplay());
            patientPanel.add(trashPatientButton, gbc);
        }
        return patientPanel;
    }

    /**
     * Data panel - Fields aligned properly
     */
    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new GridBagLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(
                    MessageBundle.getMessage("angal.familyplanning.information.label")));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.method.label") + " *:"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            methodCombo = new JComboBox<>();
            methodCombo.setPreferredSize(new Dimension(200, 25));
            if (methodTypologies != null) {
                for (Typology typology : methodTypologies) {
                    methodCombo.addItem(typology);
                }
            }
            methodCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof Typology typology) {
                        return super.getListCellRendererComponent(list,
                                typology.getDescription(), index, isSelected, cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            dataPanel.add(methodCombo, gbc);

            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.startdate.label") + " *:"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            registrationDateField = new GoodDateChooser(LocalDateTime.now().toLocalDate());
            registrationDateField.setPreferredSize(new Dimension(180, 25));
            dataPanel.add(registrationDateField, gbc);

            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.status.label") + ":"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            statusCombo = new JComboBox<>(FPStatus.values());
            statusCombo.setPreferredSize(new Dimension(180, 25));
            statusCombo.setSelectedItem(FPStatus.ACTIVE);
            statusCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof FPStatus status) {
                        return super.getListCellRendererComponent(list,
                                MessageBundle.getMessage(status.getKey()), index, isSelected, cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            dataPanel.add(statusCombo, gbc);

            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.notes.label") + ":"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            notesArea = new JTextArea(4, 30);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            notesArea.setPreferredSize(new Dimension(300, 80));
            JScrollPane notesScroll = new JScrollPane(notesArea);
            notesScroll.setPreferredSize(new Dimension(320, 80));
            dataPanel.add(notesScroll, gbc);
        }
        return dataPanel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

            okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
            okButton.setPreferredSize(new Dimension(80, 30));
            okButton.addActionListener(e -> save());

            cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
            cancelButton.setPreferredSize(new Dimension(120, 30));
            cancelButton.setMinimumSize(new Dimension(120, 30));
            cancelButton.addActionListener(e -> dispose());

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
        }
        return buttonPanel;
    }

    private void save() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.familyplanning.patient.required.msg");
            return;
        }

        Typology method = (Typology) methodCombo.getSelectedItem();
        if (method == null) {
            MessageDialog.error(this, "angal.familyplanning.method.required.msg");
            return;
        }

        LocalDateTime registrationDate = registrationDateField.getDate() != null
                ? registrationDateField.getDate().atStartOfDay()
                : null;
        if (registrationDate == null) {
            MessageDialog.error(this, "angal.familyplanning.startdate.required.msg");
            return;
        }

        if (registrationDate.isAfter(LocalDateTime.now())) {
            MessageDialog.error(this, "angal.familyplanning.startdate.cannotbefuture.msg");
            return;
        }

        FPStatus status = (FPStatus) statusCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        fp.setPatient(selectedPatient);
        fp.setCurrentMethod(method);
        fp.setRegistrationDate(registrationDate);
        fp.setStatus(status);
        fp.setNotes(notes);

        try {
            if (insert) {
                FamilyPlanning saved = fpManager.newFamilyPlanning(fp);
                fireFPInserted(saved);
            } else {
                FamilyPlanning saved = fpManager.updateFamilyPlanning(fp);
                fireFPUpdated(saved);
            }
            dispose();
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }
}