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
import java.time.LocalDate;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.FamilyPlanningBrowserManager;
import org.isf.maternity.model.FPMethod;
import org.isf.maternity.model.FPStatus;
import org.isf.maternity.model.FamilyPlanning;
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
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

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

    private JComboBox<FPMethod> methodCombo;
    private GoodDateChooser startDateField;
    private GoodDateChooser endDateField;
    private JComboBox<FPStatus> statusCombo;
    private JTextArea stopReasonArea;
    private GoodDateChooser nextAppointmentDateField;
    private JTextArea notesArea;

    private FamilyPlanning fp;
    private Patient selectedPatient;
    private final boolean insert;

    private FamilyPlanningBrowserManager fpManager;
    private PatientBrowserManager patientManager;

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
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.familyplanning.new.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.familyplanning.edit.title"));
        }
        setMinimumSize(new Dimension(600, 500));
        setPreferredSize(new Dimension(650, 550));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (fp != null && !insert) {
            if (fp.getMethod() != null) {
                methodCombo.setSelectedItem(fp.getMethod());
            }
            if (fp.getStartDate() != null) {
                startDateField.setDate(fp.getStartDate());
            }
            if (fp.getEndDate() != null) {
                endDateField.setDate(fp.getEndDate());
            }
            if (fp.getStatus() != null) {
                statusCombo.setSelectedItem(fp.getStatus());
            }
            if (fp.getStopReason() != null) {
                stopReasonArea.setText(fp.getStopReason());
            }
            if (fp.getNextAppointmentDate() != null) {
                nextAppointmentDateField.setDate(fp.getNextAppointmentDate());
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
        fp.setPatient(null);
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

            gbc.gridx = 3;
            gbc.gridwidth = 1;
            pickPatientButton = new JButton();
            pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
            pickPatientButton.addActionListener(e -> openPatientSearch());
            patientPanel.add(pickPatientButton, gbc);

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

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new GridBagLayout());
            // no titled border, follows VisitEdit pattern

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;

            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.method.label") + ":"), gbc);

            gbc.gridx = 1;
            methodCombo = new JComboBox<>(FPMethod.values());
            methodCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                               boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof FPMethod method) {
                        return super.getListCellRendererComponent(list,
                                MessageBundle.getMessage(method.getKey()), index, isSelected, cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            dataPanel.add(methodCombo, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.startdate.label") + ":"), gbc);

            gbc.gridx = 1;
            startDateField = new GoodDateChooser(LocalDate.now(), false);
            dataPanel.add(startDateField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.status.label") + ":"), gbc);

            gbc.gridx = 1;
            statusCombo = new JComboBox<>(FPStatus.values());
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
            statusCombo.addActionListener(e -> {
                FPStatus selectedStatus = (FPStatus) statusCombo.getSelectedItem();
                boolean stopped = selectedStatus == FPStatus.STOPPED;
                stopReasonArea.setEnabled(stopped);
            });
            dataPanel.add(statusCombo, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.enddate.label") + ":"), gbc);

            gbc.gridx = 1;
            endDateField = new GoodDateChooser(LocalDate.now(), true, true);
            dataPanel.add(endDateField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.stopreason.label") + ":"), gbc);

            gbc.gridx = 1;
            stopReasonArea = new JTextArea(3, 30);
            stopReasonArea.setLineWrap(true);
            stopReasonArea.setWrapStyleWord(true);
            stopReasonArea.setEnabled(false);
            JScrollPane stopReasonScroll = new JScrollPane(stopReasonArea);
            stopReasonScroll.setPreferredSize(new Dimension(250, 60));
            dataPanel.add(stopReasonScroll, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.WEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.nextappointment.label") + ":"), gbc);

            gbc.gridx = 1;
            nextAppointmentDateField = new GoodDateChooser(LocalDate.now(), true, true);
            dataPanel.add(nextAppointmentDateField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.notes.label") + ":"), gbc);

            gbc.gridx = 1;
            notesArea = new JTextArea(4, 30);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(notesArea);
            notesScroll.setPreferredSize(new Dimension(250, 80));
            dataPanel.add(notesScroll, gbc);
        }
        return dataPanel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

            okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
            okButton.addActionListener(e -> save());

            cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
            cancelButton.addActionListener(e -> dispose());

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
        }
        return buttonPanel;
    }

    private void save() {
        if (selectedPatient == null) {
            MessageDialog.error(this, "angal.maternity.fpmusthavepatient.msg");
            return;
        }

        FPMethod method = (FPMethod) methodCombo.getSelectedItem();
        if (method == null) {
            MessageDialog.error(this, "angal.maternity.fpmethodrequired.msg");
            return;
        }

        LocalDate startDate = startDateField.getDate();
        if (startDate == null) {
            MessageDialog.error(this, "angal.maternity.fpstartdaterequired.msg");
            return;
        }

        if (startDate.isAfter(LocalDate.now())) {
            MessageDialog.error(this, "angal.maternity.fpstartdatecannotbeinfuture.msg");
            return;
        }

        LocalDate endDate = endDateField.getDate();
        if (endDate != null && startDate.isAfter(endDate)) {
            MessageDialog.error(this, "angal.familyplanning.startdate.beforeenddate");
            return;
        }

        FPStatus status = (FPStatus) statusCombo.getSelectedItem();
        String stopReason = stopReasonArea.getText().trim();

        if (status == FPStatus.STOPPED && (stopReason == null || stopReason.isBlank())) {
            MessageDialog.error(this, "angal.maternity.fpstopreasonrequired.msg");
            return;
        }

        LocalDate nextAppointment = nextAppointmentDateField.getDate();
        String notes = notesArea.getText().trim();

        fp.setPatient(selectedPatient);
        fp.setMethod(method);
        fp.setStartDate(startDate);
        fp.setEndDate(endDate);
        fp.setStatus(status);
        fp.setStopReason(stopReason);
        fp.setNextAppointmentDate(nextAppointment);
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
