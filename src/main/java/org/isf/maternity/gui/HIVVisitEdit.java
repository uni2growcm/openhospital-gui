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
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
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

    // the sections are displayed on two columns when the form is at least this wide
    private static final int TWO_COLUMNS_MIN_WIDTH = 900;
    // the dialog never takes more than this part of the screen
    private static final double MAX_SCREEN_RATIO = 0.9;
    private static final Dimension MIN_SIZE = new Dimension(480, 400);
    // default width, large enough to display the sections on two columns
    private static final int DEFAULT_WIDTH = 1000;

    // Panels
    private JPanel mainPanel;
    private JPanel infoPanel;
    private JPanel dataPanel;
    private JPanel sectionsPanel;
    private List<JPanel> sections;
    private boolean twoColumns;
    private JPanel buttonPanel;

    // Buttons
    private JButton okButton;
    private JButton cancelButton;

    // Date field
    private GoodDateTimeSpinnerChooser visitDateField;

    // Clinical data fields (weight, height and temperature are recorded through the patient examination)
    private VoLimitedTextField headCircumferenceField;
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
        fitToScreen();
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
        fitToScreen();
        setLocationRelativeTo(owner);
    }

    /**
     * Sizes the dialog on its content, without exceeding the available screen space (the content scrolls if needed).
     */
    private void fitToScreen() {
        pack();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int maxWidth = (int) (screen.width * MAX_SCREEN_RATIO);
        int maxHeight = (int) (screen.height * MAX_SCREEN_RATIO);
        Dimension preferred = getPreferredSize();
        setMinimumSize(new Dimension(Math.min(MIN_SIZE.width, maxWidth), Math.min(MIN_SIZE.height, maxHeight)));
        setSize(Math.min(Math.max(preferred.width, DEFAULT_WIDTH), maxWidth), Math.min(preferred.height, maxHeight));
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
        setResizable(true);
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (visit != null && !insert) {
            if (visit.getVisitDate() != null) {
                visitDateField.setDateTime(visit.getVisitDate());
            }
            if (visit.getHeadCircumference() != null) {
                headCircumferenceField.setText(String.valueOf(visit.getHeadCircumference()));
            }
            if (visit.getClinicalStatus() != null) {
                clinicalStatusArea.setText(visit.getClinicalStatus());
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
            mainPanel = new JPanel(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(getDataPanel());
            scrollPane.setBorder(null);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }
        return mainPanel;
    }

    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new ScrollablePanel(new GridBagLayout());
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
            gbc.fill = GridBagConstraints.NONE;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.maternity.visitdate.col") + " *:"), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 1;
            gbc.weightx = 1.0;
            dataPanel.add(getVisitDateField(), gbc);
            row++;

            // Clinical data, treatment, side effects and follow-up
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            dataPanel.add(getSectionsPanel(), gbc);
            row++;

            // Add vertical filler
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.VERTICAL;
            dataPanel.add(new JPanel(), gbc);

            dataPanel.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    boolean wide = dataPanel.getWidth() >= TWO_COLUMNS_MIN_WIDTH;
                    if (wide != twoColumns) {
                        layoutSections(wide);
                    }
                }
            });
        }
        return dataPanel;
    }

    private JPanel getSectionsPanel() {
        if (sectionsPanel == null) {
            sectionsPanel = new JPanel(new GridBagLayout());
            sections = List.of(getClinicalDataPanel(), getTreatmentPanel(), getSideEffectsPanel(), getFollowUpPanel());
            layoutSections(false);
        }
        return sectionsPanel;
    }

    /**
     * Places the sections on two columns (wide window) or on a single column (narrow window).
     *
     * @param onTwoColumns {@code true} for two columns
     */
    private void layoutSections(boolean onTwoColumns) {
        twoColumns = onTwoColumns;
        sectionsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 5, 0);
        for (int i = 0; i < sections.size(); i++) {
            gbc.gridx = onTwoColumns ? i % 2 : 0;
            gbc.gridy = onTwoColumns ? i / 2 : i;
            if (onTwoColumns) {
                gbc.insets = new Insets(0, gbc.gridx == 0 ? 0 : 5, 5, 0);
            }
            sectionsPanel.add(sections.get(i), gbc);
        }
        sectionsPanel.revalidate();
        sectionsPanel.repaint();
    }

    /**
     * Panel following the width of the scroll pane viewport, so that the form is laid out again when the dialog is resized
     * (only vertical scrolling).
     */
    private static class ScrollablePanel extends JPanel implements Scrollable {

        private static final long serialVersionUID = 1L;

        ScrollablePanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport viewport && viewport.getHeight() > getPreferredSize().height;
        }
    }

    private JPanel getInfantInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hiv.visit.infant.info")));

        Patient patient = infant.getPatient();
        if (patient != null) {
            JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.hiv.label.patient") + ": " + patient.getFirstName() + " " + patient.getSecondName() + " (" + patient.getCode() + ")");
            JLabel ageLabel = new JLabel(MessageBundle.getMessage("angal.common.age.txt") + ": " + HIVInfantEdit.formatAge(patient));
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

        // Head Circumference
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.hiv.label.head.circumference") + " (cm):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(getHeadCircumferenceField(), gbc);
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
            if (VisitExaminationLauncher.isExaminationAllowed()) {
                buttonPanel.add(VisitExaminationLauncher.createExaminationButton(this, infant != null ? infant.getPatient() : null));
            }
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

    private VoLimitedTextField getHeadCircumferenceField() {
        if (headCircumferenceField == null) {
            headCircumferenceField = new VoLimitedTextField(5, 1);
            headCircumferenceField.setColumns(8);
        }
        return headCircumferenceField;
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
                MessageDialog.error(this, "angal.common.pleaseinsertavaliddate.msg");
                return;
            }

            if (infant != null && infant.getRegistrationDate() != null
                    && visitDate.toLocalDate().isBefore(infant.getRegistrationDate().toLocalDate())) {
                MessageDialog.error(this, "angal.hiv.message.visit.date.before.registration");
                return;
            }

            visit.setVisitDate(visitDate);

            if (nextAppointmentDateField.getDate() != null) {
                LocalDate nextAppointment = nextAppointmentDateField.getDate();
                LocalDate visitDateLocal = visitDate.toLocalDate();

                if (nextAppointment.isBefore(visitDateLocal)) {
                    MessageDialog.error(this,
                            "angal.hiv.message.next.appointment.before.visit");
                    return;
                }
            }
            String headCircText = headCircumferenceField.getText().trim();
            if (!headCircText.isEmpty()) {
                visit.setHeadCircumference(Double.parseDouble(headCircText));
            } else {
                visit.setHeadCircumference(null);
            }

            String clinicalStatus = clinicalStatusArea.getText();
            visit.setClinicalStatus(
                    clinicalStatus != null && !clinicalStatus.trim().isEmpty()
                            ? clinicalStatus.trim()
                            : null
            );

            String sideEffects = sideEffectsArea.getText();
            visit.setSideEffects(
                    sideEffects != null && !sideEffects.trim().isEmpty()
                            ? sideEffects.trim()
                            : null
            );

            String treatmentType = (String) treatmentTypeCombo.getSelectedItem();
            if (treatmentType != null && !treatmentType.isEmpty()) {
                LocalDate startDate = treatmentStartDateField.getDate();
                LocalDate endDate = treatmentEndDateField.getDate();

                if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                    MessageDialog.error(this,
                            "angal.hiv.message.treatment.end.before.start");
                    return;
                }
            }

            visit.setNextAppointmentDate(nextAppointmentDateField.getDate());

            String notes = notesArea.getText();
            visit.setNotes(
                    notes != null && !notes.trim().isEmpty()
                            ? notes.trim()
                            : null
            );

            if (insert) {
                HIVVisit saved = visitManager.newVisit(visit);

                if (saved != null) {
                    fireVisitInserted(saved);
                    dispose();
                } else {
                    MessageDialog.error(this,
                            "angal.common.datacouldnotbesaved.msg");
                }
            } else {
                HIVVisit updated = visitManager.updateVisit(visit);

                if (updated != null) {
                    fireVisitUpdated(updated);
                    dispose();
                } else {
                    MessageDialog.error(this,
                            "angal.common.datacouldnotbesaved.msg");
                }
            }

        } catch (NumberFormatException ex) {
            MessageDialog.error(this, "angal.common.pleaseentervalidnumbers.msg");
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }
}