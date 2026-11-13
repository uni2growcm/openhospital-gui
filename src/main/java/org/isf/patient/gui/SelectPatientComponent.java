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
package org.isf.patient.gui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;

/**
 * Reusable panel component for selecting, changing, editing, and removing a Patient.
 */
public class SelectPatientComponent extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface PatientSelectionListener {
        void patientSelected(Patient patient);
        default void patientCleared() {}
    }

    private JTextField patientSearchField;
    private JButton pickPatientButton;
    private JButton trashPatientButton;
    private JButton editPatientButton;

    private Patient selectedPatient;
    private boolean enableAddPatient = true;
    private boolean femaleOnly = false;
    private boolean showEditButton = false;
    private int fieldWidth = 220;

    private final List<PatientSelectionListener> listeners = new ArrayList<>();

    public SelectPatientComponent() {
        this(true, false, false);
    }

    public SelectPatientComponent(boolean enableAddPatient, boolean femaleOnly) {
        this(enableAddPatient, femaleOnly, false);
    }

    public SelectPatientComponent(boolean enableAddPatient, boolean femaleOnly, boolean showEditButton) {
        this.enableAddPatient = enableAddPatient;
        this.femaleOnly = femaleOnly;
        this.showEditButton = showEditButton;
        initialize();
    }

    private void initialize() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        patientSearchField = new JTextField();
        patientSearchField.setPreferredSize(new Dimension(fieldWidth, 25));
        patientSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedPatient == null && isEnabled()) {
                    openPatientSearch();
                }
            }
        });
        add(patientSearchField);

        pickPatientButton = new JButton(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
        pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
        pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.billbrowser.selectapatient.tooltip"));
        pickPatientButton.addActionListener(e -> openPatientSearch());
        add(pickPatientButton);

        if (showEditButton) {
            editPatientButton = new JButton();
            editPatientButton.setIcon(new ImageIcon("rsc/icons/edit_button.png"));
            editPatientButton.setPreferredSize(new Dimension(25, 25));
            editPatientButton.setToolTipText(MessageBundle.getMessage("angal.common.edit.btn"));
            editPatientButton.setEnabled(false);
            editPatientButton.addActionListener(e -> editPatient());
            add(editPatientButton);
        }

        trashPatientButton = new JButton();
        trashPatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
        trashPatientButton.setPreferredSize(new Dimension(25, 25));
        trashPatientButton.setToolTipText(MessageBundle.getMessage("angal.common.clear.btn"));
        trashPatientButton.setEnabled(false);
        trashPatientButton.addActionListener(e -> clearPatient());
        add(trashPatientButton);
    }

    public void openPatientSearch() {
        if (!isEnabled()) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        String searchText = (selectedPatient != null) ? "" : patientSearchField.getText().trim();
        SelectPatient sp;
        if (owner instanceof JDialog dialogOwner) {
            sp = new SelectPatient(dialogOwner, searchText, enableAddPatient, femaleOnly);
        } else if (owner instanceof JFrame frameOwner) {
            sp = new SelectPatient(frameOwner, searchText, enableAddPatient, femaleOnly);
        } else {
            sp = new SelectPatient((JFrame) null, searchText, enableAddPatient, femaleOnly);
        }
        sp.addSelectionListener(this::setPatientAndNotify);
        sp.setVisible(true);
        Patient chosen = sp.getPatient();
        if (chosen != null && (selectedPatient == null || chosen.getCode() != selectedPatient.getCode())) {
            setPatientAndNotify(chosen);
        }
    }

    public void setPatient(Patient patient) {
        this.selectedPatient = patient;
        updateDisplay();
    }

    private void setPatientAndNotify(Patient patient) {
        if (patient != null) {
            setPatient(patient);
            for (PatientSelectionListener listener : listeners) {
                listener.patientSelected(patient);
            }
        }
    }

    public void clearPatient() {
        this.selectedPatient = null;
        updateDisplay();
        for (PatientSelectionListener listener : listeners) {
            listener.patientCleared();
        }
    }

    private void updateDisplay() {
        if (selectedPatient != null) {
            patientSearchField.setText(selectedPatient.getName());
            patientSearchField.setEditable(false);
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.changepatient"));
            trashPatientButton.setEnabled(isEnabled());
            if (editPatientButton != null) {
                editPatientButton.setEnabled(isEnabled());
            }
        } else {
            patientSearchField.setText("");
            patientSearchField.setEditable(isEnabled());
            pickPatientButton.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
            trashPatientButton.setEnabled(false);
            if (editPatientButton != null) {
                editPatientButton.setEnabled(false);
            }
        }
    }

    private void editPatient() {
        if (selectedPatient == null || !isEnabled()) {
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (GeneralData.PATIENTEXTENDED) {
            PatientInsertExtended editRecord;
            if (owner instanceof JDialog d) {
                editRecord = new PatientInsertExtended(d, selectedPatient, false);
            } else if (owner instanceof JFrame f) {
                editRecord = new PatientInsertExtended(f, selectedPatient, false);
            } else {
                editRecord = new PatientInsertExtended((JFrame) null, selectedPatient, false);
            }
            editRecord.addPatientListener(new PatientInsertExtended.PatientListener() {
                @Override
                public void patientInserted(AWTEvent e) {
                    handlePatientEvent(e);
                }

                @Override
                public void patientUpdated(AWTEvent e) {
                    handlePatientEvent(e);
                }
            });
            editRecord.setVisible(true);
        } else {
            PatientInsert editRecord;
            if (owner instanceof JDialog d) {
                editRecord = new PatientInsert(d, selectedPatient, false);
            } else if (owner instanceof JFrame f) {
                editRecord = new PatientInsert(f, selectedPatient, false);
            } else {
                editRecord = new PatientInsert((JFrame) null, selectedPatient, false);
            }
            editRecord.addPatientListener(new PatientInsert.PatientListener() {
                @Override
                public void patientInserted(AWTEvent e) {
                    handlePatientEvent(e);
                }

                @Override
                public void patientUpdated(AWTEvent e) {
                    handlePatientEvent(e);
                }
            });
            editRecord.setVisible(true);
        }
    }

    private void handlePatientEvent(AWTEvent e) {
        if (e.getSource() instanceof Patient p) {
            setPatientAndNotify(p);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        patientSearchField.setEnabled(enabled);
        patientSearchField.setEditable(enabled && selectedPatient == null);
        pickPatientButton.setEnabled(enabled);
        trashPatientButton.setEnabled(enabled && selectedPatient != null);
        if (editPatientButton != null) {
            editPatientButton.setEnabled(enabled && selectedPatient != null);
        }
    }

    public void setFieldWidth(int width) {
        this.fieldWidth = width;
        patientSearchField.setPreferredSize(new Dimension(width, 25));
        revalidate();
        repaint();
    }

    public Patient getPatient() {
        return selectedPatient;
    }

    public JTextField getPatientSearchField() {
        return patientSearchField;
    }

    public JButton getPickPatientButton() {
        return pickPatientButton;
    }

    public JButton getTrashPatientButton() {
        return trashPatientButton;
    }

    public JButton getEditPatientButton() {
        return editPatientButton;
    }

    public void addPatientSelectionListener(PatientSelectionListener listener) {
        listeners.add(listener);
    }

    public void removePatientSelectionListener(PatientSelectionListener listener) {
        listeners.remove(listener);
    }
}
