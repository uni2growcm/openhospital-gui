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

import java.awt.*;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyVisitBrowserManager;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyVisit;
import org.isf.menu.manager.Context;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class MaternityVisitEdit extends JDialog {

    private static final long serialVersionUID = 1L;

    private EventListenerList visitListeners = new EventListenerList();

    public interface MaternityVisitListener extends EventListener {
        void visitInserted(AWTEvent e, PregnancyVisit visit);
        void visitUpdated(AWTEvent e, PregnancyVisit visit);
    }

    public void addMaternityVisitListener(MaternityVisitListener l) {
        visitListeners.add(MaternityVisitListener.class, l);
    }

    public void removeMaternityVisitListener(MaternityVisitListener listener) {
        visitListeners.remove(MaternityVisitListener.class, listener);
    }

    private void fireVisitInserted(PregnancyVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = visitListeners.getListeners(MaternityVisitListener.class);
        for (EventListener listener : listeners) {
            ((MaternityVisitListener) listener).visitInserted(event, visit);
        }
    }

    private void fireVisitUpdated(PregnancyVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        EventListener[] listeners = visitListeners.getListeners(MaternityVisitListener.class);
        for (EventListener listener : listeners) {
            ((MaternityVisitListener) listener).visitUpdated(event, visit);
        }
    }

    private JPanel mainPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private GoodDateTimeSpinnerChooser visitDateField;
    private JComboBox<Typology> visitTypeCombo;
    private JTextField gestationalWeeksField;
    private JTextField gestationalDaysField;
    private JTextField maternalWeightField;
    private JTextField systolicBPField;
    private JTextField diastolicBPField;
    private JTextField temperatureField;
    private JTextField fundalHeightField;
    private JTextField abdominalCircumferenceField;
    private JTextField fetalHeartRateField;
    private JComboBox<String> fetalPresentationCombo;
    private JComboBox<String> edemaPresenceCombo;
    private JComboBox<String> urineProteinCombo;
    private JComboBox<String> urineGlucoseCombo;
    private GoodDateChooser nextAppointmentDateField;
    private JTextArea clinicalNotesArea;

    private final PregnancyVisit visit;
    private final Pregnancy pregnancy;
    private final boolean insert;

    private PregnancyVisitBrowserManager visitManager;
    private List<Typology> visitTypes;

    public MaternityVisitEdit(JFrame owner, Pregnancy pregnancy, boolean inserting) {
        super(owner, true);
        this.pregnancy = pregnancy;
        this.visit = new PregnancyVisit();
        this.visit.setPregnancy(pregnancy);
        this.insert = inserting;
        initManagers();
        initialize();
        pack();
        setLocationRelativeTo(owner);
    }

    public MaternityVisitEdit(JFrame owner, PregnancyVisit existingVisit, boolean inserting) {
        super(owner, true);
        this.visit = existingVisit;
        this.pregnancy = existingVisit.getPregnancy();
        this.insert = inserting;
        initManagers();
        initialize();
        loadExistingData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        visitManager = Context.getApplicationContext().getBean(PregnancyVisitBrowserManager.class);
        TypologyBrowserManager typologyBrowserManager = Context.getApplicationContext().getBean(TypologyBrowserManager.class);
        try {
            visitTypes = typologyBrowserManager.getTypologies(Family.VISITTYPE);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            visitTypes = new java.util.ArrayList<>();
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.maternity.newvisit.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.maternity.editvisit.title"));
        }
        setMinimumSize(new Dimension(750, 650));
        setPreferredSize(new Dimension(800, 700));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (!insert && visit != null) {
            if (visit.getVisitDate() != null) {
                visitDateField.setDateTime(visit.getVisitDate());
            }
            if (visit.getVisitType() != null) {
                visitTypeCombo.setSelectedItem(visit.getVisitType());
            }
            if (visit.getGestationalWeeks() != null) {
                gestationalWeeksField.setText(String.valueOf(visit.getGestationalWeeks()));
            }
            if (visit.getGestationalDays() != null) {
                gestationalDaysField.setText(String.valueOf(visit.getGestationalDays()));
            }
            if (visit.getMaternalWeight() != null) {
                maternalWeightField.setText(String.valueOf(visit.getMaternalWeight()));
            }
            if (visit.getSystolicBP() != null) {
                systolicBPField.setText(String.valueOf(visit.getSystolicBP()));
            }
            if (visit.getDiastolicBP() != null) {
                diastolicBPField.setText(String.valueOf(visit.getDiastolicBP()));
            }
            if (visit.getTemperature() != null) {
                temperatureField.setText(String.valueOf(visit.getTemperature()));
            }
            if (visit.getFundalHeight() != null) {
                fundalHeightField.setText(String.valueOf(visit.getFundalHeight()));
            }
            if (visit.getAbdominalCircumference() != null) {
                abdominalCircumferenceField.setText(String.valueOf(visit.getAbdominalCircumference()));
            }
            if (visit.getFetalHeartRate() != null) {
                fetalHeartRateField.setText(String.valueOf(visit.getFetalHeartRate()));
            }
            if (visit.getFetalPresentation() != null) {
                fetalPresentationCombo.setSelectedItem(visit.getFetalPresentation());
            }
            if (visit.getEdemaPresence() != null) {
                edemaPresenceCombo.setSelectedItem(visit.getEdemaPresence());
            }
            if (visit.getUrineProtein() != null) {
                urineProteinCombo.setSelectedItem(visit.getUrineProtein());
            }
            if (visit.getUrineGlucose() != null) {
                urineGlucoseCombo.setSelectedItem(visit.getUrineGlucose());
            }
            if (visit.getNextAppointmentDate() != null) {
                nextAppointmentDateField.setDate(visit.getNextAppointmentDate());
            }
            if (visit.getClinicalNotes() != null) {
                clinicalNotesArea.setText(visit.getClinicalNotes());
            }
        }
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(getDataPanel());
            mainPanel.add(getClinicalNotesPanel());
        }
        return mainPanel;
    }

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new SpringLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.visit.data.border")));

            // Pregnancy info (display only) - Row 1
            JLabel pregnancyInfoLabel = new JLabel(MessageBundle.getMessage("angal.maternity.pregnancy.info"));
            JPanel pregnancyInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pregnancyInfoPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.pregnancy.id") + ": " +
                    (pregnancy.getId() != null ? pregnancy.getId() : "N/A")));
            pregnancyInfoPanel.add(new JLabel(" | " + MessageBundle.getMessage("angal.common.code.txt") + ": " +
                    (pregnancy.getPatient() != null ? pregnancy.getPatient().getCode() : "")));
            pregnancyInfoPanel.add(new JLabel(" | " + MessageBundle.getMessage("angal.common.name.txt") + ": " +
                    (pregnancy.getPatient() != null ? pregnancy.getPatient().getSecondName() + " " + pregnancy.getPatient().getFirstName() : "")));
            dataPanel.add(pregnancyInfoLabel);
            dataPanel.add(pregnancyInfoPanel);

            // Visit Date (mandatory) - Row 2
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.visit.date") + " *"));
            dataPanel.add(getVisitDateField());

            // Visit Type (mandatory) - Row 3
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.visit.type") + " *"));
            dataPanel.add(getVisitTypeCombo());

            // Gestational Age - Row 4
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.gestational.age")));
            JPanel gestationalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            gestationalPanel.add(getGestationalWeeksField());
            gestationalPanel.add(new JLabel(" " + MessageBundle.getMessage("angal.common.weeks.txt") + " "));
            gestationalPanel.add(getGestationalDaysField());
            gestationalPanel.add(new JLabel(" " + MessageBundle.getMessage("angal.common.day.txt")));
            dataPanel.add(gestationalPanel);

            // Maternal Weight - Row 5
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.maternal.weight") + " (kg)"));
            dataPanel.add(getMaternalWeightField());

            // Blood Pressure - Row 6 (two fields side by side)
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.blood.pressure") + " (mmHg)"));
            JPanel bpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bpPanel.add(getSystolicBPField());
            bpPanel.add(new JLabel(" / "));
            bpPanel.add(getDiastolicBPField());
            dataPanel.add(bpPanel);

            // Temperature - Row 7
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.temperature") + " (°C)"));
            dataPanel.add(getTemperatureField());

            // Fundal Height - Row 8
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.fundal.height") + " (cm)"));
            dataPanel.add(getFundalHeightField());

            // Abdominal Circumference - Row 9
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.abdominal.circumference") + " (cm)"));
            dataPanel.add(getAbdominalCircumferenceField());

            // Fetal Heart Rate - Row 10
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.fetal.heart.rate") + " (bpm)"));
            dataPanel.add(getFetalHeartRateField());

            // Fetal Presentation - Row 11
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.fetal.presentation")));
            dataPanel.add(getFetalPresentationCombo());

            // Edema - Row 12
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.edema.presence")));
            dataPanel.add(getEdemaPresenceCombo());

            // Urine Protein - Row 13
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.urine.protein")));
            dataPanel.add(getUrineProteinCombo());

            // Urine Glucose - Row 14
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.urine.glucose")));
            dataPanel.add(getUrineGlucoseCombo());

            // Next Appointment - Row 15 (using GoodDateChooser)
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.next.appointment")));
            dataPanel.add(getNextAppointmentDateField());

            SpringUtilities.makeCompactGrid(dataPanel, 15, 2, 10, 10, 10, 10);
        }
        return dataPanel;
    }

    private JPanel getClinicalNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.clinical.notes")));
        JScrollPane scrollPane = new JScrollPane(getClinicalNotesArea());
        scrollPane.setPreferredSize(new Dimension(680, 100));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(getOkButton());
            buttonPanel.add(getCancelButton());
        }
        return buttonPanel;
    }

    private GoodDateTimeSpinnerChooser getVisitDateField() {
        if (visitDateField == null) {
            LocalDateTime dateTime = insert ? LocalDateTime.now() : visit.getVisitDate();
            if (dateTime == null) dateTime = LocalDateTime.now();
            visitDateField = new GoodDateTimeSpinnerChooser(dateTime);
        }
        return visitDateField;
    }

    private JComboBox<Typology> getVisitTypeCombo() {
        if (visitTypeCombo == null) {
            visitTypeCombo = new JComboBox<>();
            if (visitTypes != null) {
                for (Typology type : visitTypes) {
                    visitTypeCombo.addItem(type);
                }
            }
            visitTypeCombo.setPreferredSize(new Dimension(200, 25));
        }
        return visitTypeCombo;
    }

    private JTextField getGestationalWeeksField() {
        if (gestationalWeeksField == null) {
            gestationalWeeksField = new VoLimitedTextField(2, 2);
            gestationalWeeksField.setColumns(3);
        }
        return gestationalWeeksField;
    }

    private JTextField getGestationalDaysField() {
        if (gestationalDaysField == null) {
            gestationalDaysField = new VoLimitedTextField(1, 1);
            gestationalDaysField.setColumns(3);
        }
        return gestationalDaysField;
    }

    private JTextField getMaternalWeightField() {
        if (maternalWeightField == null) {
            maternalWeightField = new VoLimitedTextField(5, 2);
            maternalWeightField.setColumns(8);
        }
        return maternalWeightField;
    }

    private JTextField getSystolicBPField() {
        if (systolicBPField == null) {
            systolicBPField = new VoLimitedTextField(3, 0);
            systolicBPField.setColumns(4);
        }
        return systolicBPField;
    }

    private JTextField getDiastolicBPField() {
        if (diastolicBPField == null) {
            diastolicBPField = new VoLimitedTextField(3, 0);
            diastolicBPField.setColumns(4);
        }
        return diastolicBPField;
    }

    private JTextField getTemperatureField() {
        if (temperatureField == null) {
            temperatureField = new VoLimitedTextField(4, 1);
            temperatureField.setColumns(6);
        }
        return temperatureField;
    }

    private JTextField getFundalHeightField() {
        if (fundalHeightField == null) {
            fundalHeightField = new VoLimitedTextField(5, 1);
            fundalHeightField.setColumns(8);
        }
        return fundalHeightField;
    }

    private JTextField getAbdominalCircumferenceField() {
        if (abdominalCircumferenceField == null) {
            abdominalCircumferenceField = new VoLimitedTextField(5, 1);
            abdominalCircumferenceField.setColumns(8);
        }
        return abdominalCircumferenceField;
    }

    private JTextField getFetalHeartRateField() {
        if (fetalHeartRateField == null) {
            fetalHeartRateField = new VoLimitedTextField(3, 0);
            fetalHeartRateField.setColumns(6);
        }
        return fetalHeartRateField;
    }

    private JComboBox<String> getFetalPresentationCombo() {
        if (fetalPresentationCombo == null) {
            String[] presentations = {
                    "",
                    MessageBundle.getMessage("angal.maternity.presentation.cephalic"),
                    MessageBundle.getMessage("angal.maternity.presentation.breech"),
                    MessageBundle.getMessage("angal.maternity.presentation.transverse"),
                    MessageBundle.getMessage("angal.maternity.presentation.other")
            };
            fetalPresentationCombo = new JComboBox<>(presentations);
            fetalPresentationCombo.setPreferredSize(new Dimension(200, 25));
        }
        return fetalPresentationCombo;
    }

    private JComboBox<String> getEdemaPresenceCombo() {
        if (edemaPresenceCombo == null) {
            String[] options = {
                    "",
                    MessageBundle.getMessage("angal.common.none.txt"),
                    MessageBundle.getMessage("angal.maternity.edema.mild"),
                    MessageBundle.getMessage("angal.maternity.edema.moderate"),
                    MessageBundle.getMessage("angal.maternity.edema.severe")
            };
            edemaPresenceCombo = new JComboBox<>(options);
            edemaPresenceCombo.setPreferredSize(new Dimension(200, 25));
        }
        return edemaPresenceCombo;
    }

    private JComboBox<String> getUrineProteinCombo() {
        if (urineProteinCombo == null) {
            String[] options = {"", "-", "+", "++", "+++"};
            urineProteinCombo = new JComboBox<>(options);
            urineProteinCombo.setPreferredSize(new Dimension(100, 25));
        }
        return urineProteinCombo;
    }

    private JComboBox<String> getUrineGlucoseCombo() {
        if (urineGlucoseCombo == null) {
            String[] options = {"", "-", "+", "++", "+++"};
            urineGlucoseCombo = new JComboBox<>(options);
            urineGlucoseCombo.setPreferredSize(new Dimension(100, 25));
        }
        return urineGlucoseCombo;
    }

    private GoodDateChooser getNextAppointmentDateField() {
        if (nextAppointmentDateField == null) {
            LocalDate date = insert ? LocalDate.now().plusWeeks(2) : null;
            if (date == null && visit.getNextAppointmentDate() != null) {
                date = visit.getNextAppointmentDate();
            }
            if (date == null) date = LocalDate.now().plusWeeks(2);
            nextAppointmentDateField = new GoodDateChooser(date);
            nextAppointmentDateField.setPreferredSize(new Dimension(150, 25));
        }
        return nextAppointmentDateField;
    }

    private JTextArea getClinicalNotesArea() {
        if (clinicalNotesArea == null) {
            clinicalNotesArea = new JTextArea(4, 50);
            clinicalNotesArea.setLineWrap(true);
            clinicalNotesArea.setWrapStyleWord(true);
        }
        return clinicalNotesArea;
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
                MessageDialog.error(this, "angal.maternity.visit.date.required");
                return;
            }

            Typology visitType = (Typology) visitTypeCombo.getSelectedItem();
            if (visitType == null) {
                MessageDialog.error(this, "angal.maternity.visit.type.required");
                return;
            }

            visit.setVisitDate(visitDate);
            visit.setVisitType(visitType);

            if (!gestationalWeeksField.getText().trim().isEmpty()) {
                visit.setGestationalWeeks(Integer.parseInt(gestationalWeeksField.getText()));
            }
            if (!gestationalDaysField.getText().trim().isEmpty()) {
                visit.setGestationalDays(Integer.parseInt(gestationalDaysField.getText()));
            }
            if (!maternalWeightField.getText().trim().isEmpty()) {
                visit.setMaternalWeight(Double.parseDouble(maternalWeightField.getText()));
            }
            if (!systolicBPField.getText().trim().isEmpty()) {
                visit.setSystolicBP(Integer.parseInt(systolicBPField.getText()));
            }
            if (!diastolicBPField.getText().trim().isEmpty()) {
                visit.setDiastolicBP(Integer.parseInt(diastolicBPField.getText()));
            }
            if (!temperatureField.getText().trim().isEmpty()) {
                visit.setTemperature(Double.parseDouble(temperatureField.getText()));
            }
            if (!fundalHeightField.getText().trim().isEmpty()) {
                visit.setFundalHeight(Double.parseDouble(fundalHeightField.getText()));
            }
            if (!abdominalCircumferenceField.getText().trim().isEmpty()) {
                visit.setAbdominalCircumference(Double.parseDouble(abdominalCircumferenceField.getText()));
            }
            if (!fetalHeartRateField.getText().trim().isEmpty()) {
                visit.setFetalHeartRate(Integer.parseInt(fetalHeartRateField.getText()));
            }

            String fetalPresentation = (String) fetalPresentationCombo.getSelectedItem();
            if (fetalPresentation != null && !fetalPresentation.isEmpty()) {
                visit.setFetalPresentation(fetalPresentation);
            }

            String edemaPresence = (String) edemaPresenceCombo.getSelectedItem();
            if (edemaPresence != null && !edemaPresence.isEmpty()) {
                visit.setEdemaPresence(edemaPresence);
            }

            String urineProtein = (String) urineProteinCombo.getSelectedItem();
            if (urineProtein != null && !urineProtein.isEmpty()) {
                visit.setUrineProtein(urineProtein);
            }

            String urineGlucose = (String) urineGlucoseCombo.getSelectedItem();
            if (urineGlucose != null && !urineGlucose.isEmpty()) {
                visit.setUrineGlucose(urineGlucose);
            }

            visit.setNextAppointmentDate(nextAppointmentDateField.getDate());
            visit.setClinicalNotes(clinicalNotesArea.getText());

            if (insert) {
                PregnancyVisit saved = visitManager.newVisit(visit);
                if (saved != null) {
                    fireVisitInserted(saved);
                    dispose();
                } else {
                    MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
                }
            } else {
                PregnancyVisit updated = visitManager.updateVisit(visit);
                if (updated != null) {
                    fireVisitUpdated(updated);
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