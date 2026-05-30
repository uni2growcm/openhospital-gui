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
import java.util.ArrayList;
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
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyDeliveryBrowserManager;
import org.isf.maternity.manager.PregnancyVisitBrowserManager;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyDelivery;
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

public class VisitEdit extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EventListenerList visitListeners = new EventListenerList();

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
        for (EventListener listener : visitListeners.getListeners(MaternityVisitListener.class)) {
            ((MaternityVisitListener) listener).visitInserted(event, visit);
        }
    }

    private void fireVisitUpdated(PregnancyVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : visitListeners.getListeners(MaternityVisitListener.class)) {
            ((MaternityVisitListener) listener).visitUpdated(event, visit);
        }
    }

    private final PregnancyVisit visit;
    private final Pregnancy pregnancy;
    private final boolean insert;

    private boolean hasDelivery = false;

    private PregnancyVisitBrowserManager visitManager;
    private PregnancyDeliveryBrowserManager deliveryManager;
    private List<Typology> typologies;

    private JPanel mainPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private GoodDateTimeSpinnerChooser visitDateField;
    private JComboBox<Typology> typologyCombo;
    private JTextField gestationalAgeField;   // only shown when hasDelivery == false
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

    public VisitEdit(JFrame owner, Pregnancy pregnancy, boolean inserting) {
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

    public VisitEdit(JFrame owner, PregnancyVisit existingVisit, boolean inserting) {
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
        deliveryManager = Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);

        TypologyBrowserManager typologyManager =
                Context.getApplicationContext().getBean(TypologyBrowserManager.class);
        try {
            typologies = typologyManager.getTypologies(Family.VISITTYPE);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            typologies = new ArrayList<>();
        }

        if (pregnancy != null && pregnancy.getId() != null) {
            try {
                PregnancyDelivery delivery = deliveryManager.getDeliveryByPregnancy(pregnancy.getId());
                hasDelivery = (delivery != null);
            } catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
                hasDelivery = false;
            }
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setTitle(insert
                ? MessageBundle.getMessage("angal.maternity.newvisit.title")
                : MessageBundle.getMessage("angal.maternity.editvisit.title"));
        setMinimumSize(new Dimension(750, 600));
        setPreferredSize(new Dimension(800, 680));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (insert || visit == null) {
            return;
        }
        if (visit.getVisitDate() != null) {
            visitDateField.setDateTime(visit.getVisitDate());
        }
        if (visit.getVisitType() != null) {
            typologyCombo.setSelectedItem(visit.getVisitType());
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

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(getDataPanel());
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            mainPanel.add(scrollPane);
            mainPanel.add(getClinicalNotesPanel());
        }
        return mainPanel;
    }

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel(new GridBagLayout());
            dataPanel.setBorder(BorderFactory.createTitledBorder(
                    MessageBundle.getMessage("angal.maternity.visit.data.border")));

            int row = 0;

            // --- Pregnancy info (read-only header row) ---
            String patCode = (pregnancy.getPatient() != null)
                    ? String.valueOf(pregnancy.getPatient().getCode()) : "";
            String patName = (pregnancy.getPatient() != null)
                    ? pregnancy.getPatient().getSecondName() + " " + pregnancy.getPatient().getFirstName() : "";
            String pregnancyId = (pregnancy.getId() != null)
                    ? String.valueOf(pregnancy.getId()) : "N/A";

            JLabel pregnancyInfoLabel = new JLabel(
                    MessageBundle.getMessage("angal.maternity.pregnancy.id") + ": " + pregnancyId
                            + "  |  " + MessageBundle.getMessage("angal.common.code.txt") + ": " + patCode
                            + "  |  " + MessageBundle.getMessage("angal.common.name.txt") + ": " + patName);

            // Span info label across both columns
            addFullWidthRow(dataPanel, pregnancyInfoLabel, row++);

            // --- Visit Date (mandatory) ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.visit.date") + " *"),
                    getVisitDateField(),
                    row++);

            // --- Visit Type (mandatory) ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.typology") + " *"),
                    getTypologyCombo(),
                    row++);

            // --- Gestational Age (conditional — hidden when a delivery already exists) ---
            if (!hasDelivery) {
                addFormRow(dataPanel,
                        new JLabel(MessageBundle.getMessage("angal.maternity.gestational.age")),
                        getGestationalAgeField(),
                        row++);
            }

            // --- Maternal Weight ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.maternal.weight") + " (kg)"),
                    getMaternalWeightField(),
                    row++);

            // --- Blood Pressure (systolic / diastolic side by side) ---
            JPanel bpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            bpPanel.add(getSystolicBPField());
            bpPanel.add(new JLabel(" / "));
            bpPanel.add(getDiastolicBPField());
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.blood.pressure") + " (mmHg)"),
                    bpPanel,
                    row++);

            // --- Temperature ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.temperature") + " (\u00b0C)"),
                    getTemperatureField(),
                    row++);

            // --- Fundal Height ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.fundal.height") + " (cm)"),
                    getFundalHeightField(),
                    row++);

            // --- Abdominal Circumference ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.abdominal.circumference") + " (cm)"),
                    getAbdominalCircumferenceField(),
                    row++);

            // --- Fetal Heart Rate ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.fetal.heart.rate") + " (bpm)"),
                    getFetalHeartRateField(),
                    row++);

            // --- Fetal Presentation ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.fetal.presentation")),
                    getFetalPresentationCombo(),
                    row++);

            // --- Edema ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.edema.presence")),
                    getEdemaPresenceCombo(),
                    row++);

            // --- Urine Protein ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.urine.protein")),
                    getUrineProteinCombo(),
                    row++);

            // --- Urine Glucose ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.urine.glucose")),
                    getUrineGlucoseCombo(),
                    row++);

            // --- Next Appointment ---
            addFormRow(dataPanel,
                    new JLabel(MessageBundle.getMessage("angal.maternity.next.appointment")),
                    getNextAppointmentDateField(),
                    row++);

            // Vertical filler so rows stay at the top when the dialog is tall
            GridBagConstraints filler = new GridBagConstraints();
            filler.gridx = 0;
            filler.gridy = row;
            filler.gridwidth = 2;
            filler.weighty = 1.0;
            filler.fill = GridBagConstraints.VERTICAL;
            dataPanel.add(new JPanel(), filler);
        }
        return dataPanel;
    }

    private void addFormRow(JPanel panel, JLabel label, java.awt.Component field, int row) {
        Insets insets = new Insets(5, 10, 5, 10);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.fill = GridBagConstraints.NONE;
        labelConstraints.insets = insets;
        labelConstraints.weightx = 0.0;
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.anchor = GridBagConstraints.LINE_START;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = insets;
        fieldConstraints.weightx = 1.0;
        panel.add(field, fieldConstraints);
    }

    private void addFullWidthRow(JPanel panel, java.awt.Component component, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 10, 8, 10);
        constraints.weightx = 1.0;
        panel.add(component, constraints);
    }

    private JPanel getClinicalNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.maternity.clinical.notes")));
        JScrollPane scrollPane = new JScrollPane(getClinicalNotesArea());
        scrollPane.setPreferredSize(new Dimension(680, 110));
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
            LocalDateTime dateTime = (insert || visit.getVisitDate() == null)
                    ? LocalDateTime.now()
                    : visit.getVisitDate();
            visitDateField = new GoodDateTimeSpinnerChooser(dateTime);
        }
        return visitDateField;
    }

    private JComboBox<Typology> getTypologyCombo() {
        if (typologyCombo == null) {
            typologyCombo = new JComboBox<>();
            if (typologies != null) {
                for (Typology typology : typologies) {
                    typologyCombo.addItem(typology);
                }
            }
            typologyCombo.setPreferredSize(new Dimension(200, 25));
        }
        return typologyCombo;
    }

    private JTextField getGestationalAgeField() {
        if (gestationalAgeField == null) {
            String gestAge;
            if (visit == null || visit.getVisitDate() == null) {
                gestAge = pregnancy.getGestationalAge(LocalDate.now());
            } else {
                gestAge = visit.getGestationalAge();
            }
            gestationalAgeField = new JTextField(gestAge);
            gestationalAgeField.setColumns(8);
            gestationalAgeField.setEnabled(false);
        }
        return gestationalAgeField;
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
            fetalPresentationCombo = new JComboBox<>(new String[] {
                    "",
                    MessageBundle.getMessage("angal.maternity.presentation.cephalic"),
                    MessageBundle.getMessage("angal.maternity.presentation.breech"),
                    MessageBundle.getMessage("angal.maternity.presentation.transverse"),
                    MessageBundle.getMessage("angal.maternity.presentation.other")
            });
            fetalPresentationCombo.setPreferredSize(new Dimension(200, 25));
        }
        return fetalPresentationCombo;
    }

    private JComboBox<String> getEdemaPresenceCombo() {
        if (edemaPresenceCombo == null) {
            edemaPresenceCombo = new JComboBox<>(new String[] {
                    "",
                    MessageBundle.getMessage("angal.maternity.edema.none"),
                    MessageBundle.getMessage("angal.maternity.edema.mild"),
                    MessageBundle.getMessage("angal.maternity.edema.moderate"),
                    MessageBundle.getMessage("angal.maternity.edema.severe")
            });
            edemaPresenceCombo.setPreferredSize(new Dimension(200, 25));
        }
        return edemaPresenceCombo;
    }

    private JComboBox<String> getUrineProteinCombo() {
        if (urineProteinCombo == null) {
            urineProteinCombo = new JComboBox<>(new String[] { "", "-", "+", "++", "+++" });
            urineProteinCombo.setPreferredSize(new Dimension(100, 25));
        }
        return urineProteinCombo;
    }

    private JComboBox<String> getUrineGlucoseCombo() {
        if (urineGlucoseCombo == null) {
            urineGlucoseCombo = new JComboBox<>(new String[] { "", "-", "+", "++", "+++" });
            urineGlucoseCombo.setPreferredSize(new Dimension(100, 25));
        }
        return urineGlucoseCombo;
    }

    private GoodDateChooser getNextAppointmentDateField() {
        if (nextAppointmentDateField == null) {
            LocalDate date = (!insert && visit.getNextAppointmentDate() != null)
                    ? visit.getNextAppointmentDate()
                    : LocalDate.now().plusWeeks(2);
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
            okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
            okButton.addActionListener(e -> saveVisit());
        }
        return okButton;
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
            cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
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

            Typology typology = (Typology) typologyCombo.getSelectedItem();
            if (typology == null) {
                MessageDialog.error(this, "angal.maternity.typology.required");
                return;
            }

            visit.setVisitDate(visitDate);
            visit.setVisitType(typology);

            // --- Optional numeric fields ---
            String weightText = maternalWeightField.getText().trim();
            if (!weightText.isEmpty()) {
                visit.setMaternalWeight(Double.parseDouble(weightText));
            } else {
                visit.setMaternalWeight(null);
            }

            String systolicText = systolicBPField.getText().trim();
            if (!systolicText.isEmpty()) {
                visit.setSystolicBP(Integer.parseInt(systolicText));
            } else {
                visit.setSystolicBP(null);
            }

            String diastolicText = diastolicBPField.getText().trim();
            if (!diastolicText.isEmpty()) {
                visit.setDiastolicBP(Integer.parseInt(diastolicText));
            } else {
                visit.setDiastolicBP(null);
            }

            String tempText = temperatureField.getText().trim();
            if (!tempText.isEmpty()) {
                visit.setTemperature(Double.parseDouble(tempText));
            } else {
                visit.setTemperature(null);
            }

            String fundalText = fundalHeightField.getText().trim();
            if (!fundalText.isEmpty()) {
                visit.setFundalHeight(Double.parseDouble(fundalText));
            } else {
                visit.setFundalHeight(null);
            }

            String abdText = abdominalCircumferenceField.getText().trim();
            if (!abdText.isEmpty()) {
                visit.setAbdominalCircumference(Double.parseDouble(abdText));
            } else {
                visit.setAbdominalCircumference(null);
            }

            String fhrText = fetalHeartRateField.getText().trim();
            if (!fhrText.isEmpty()) {
                visit.setFetalHeartRate(Integer.parseInt(fhrText));
            } else {
                visit.setFetalHeartRate(null);
            }

            String fetalPresentation = (String) fetalPresentationCombo.getSelectedItem();
            visit.setFetalPresentation(
                    (fetalPresentation != null && !fetalPresentation.isEmpty()) ? fetalPresentation : null);

            String edemaPresence = (String) edemaPresenceCombo.getSelectedItem();
            visit.setEdemaPresence(
                    (edemaPresence != null && !edemaPresence.isEmpty()) ? edemaPresence : null);

            String urineProtein = (String) urineProteinCombo.getSelectedItem();
            visit.setUrineProtein(
                    (urineProtein != null && !urineProtein.isEmpty()) ? urineProtein : null);

            String urineGlucose = (String) urineGlucoseCombo.getSelectedItem();
            visit.setUrineGlucose(
                    (urineGlucose != null && !urineGlucose.isEmpty()) ? urineGlucose : null);

            visit.setNextAppointmentDate(nextAppointmentDateField.getDate());

            String notes = clinicalNotesArea.getText();
            visit.setClinicalNotes((notes != null && !notes.trim().isEmpty()) ? notes.trim() : null);

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