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
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EventListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.FamilyPlanningVisitBrowserManager;
import org.isf.maternity.model.FPVisitType;
import org.isf.maternity.model.FamilyPlanning;
import org.isf.maternity.model.FamilyPlanningVisit;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;

public class FamilyPlanningVisitEdit extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private final EventListenerList visitListeners = new EventListenerList();

    public interface FPVisitListener extends EventListener {
        void visitInserted(AWTEvent e, FamilyPlanningVisit visit);
        void visitUpdated(AWTEvent e, FamilyPlanningVisit visit);
    }

    public void addFPVisitListener(FPVisitListener l) {
        visitListeners.add(FPVisitListener.class, l);
    }

    public void removeFPVisitListener(FPVisitListener listener) {
        visitListeners.remove(FPVisitListener.class, listener);
    }

    private void fireVisitInserted(FamilyPlanningVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            @Serial
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : visitListeners.getListeners(FPVisitListener.class)) {
            ((FPVisitListener) listener).visitInserted(event, visit);
        }
    }

    private void fireVisitUpdated(FamilyPlanningVisit visit) {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            @Serial
            private static final long serialVersionUID = 1L;
        };
        for (EventListener listener : visitListeners.getListeners(FPVisitListener.class)) {
            ((FPVisitListener) listener).visitUpdated(event, visit);
        }
    }

    private final FamilyPlanningVisit visit;
    private final FamilyPlanning fp;
    private final boolean insert;

    private FamilyPlanningVisitBrowserManager visitManager;

    private JPanel mainPanel;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private GoodDateTimeSpinnerChooser visitDateField;
    private JComboBox<FPVisitType> visitTypeCombo;
    private JTextArea complaintsArea;
    private JTextArea notesArea;
    private GoodDateChooser nextAppointmentDateField;

    public FamilyPlanningVisitEdit(JFrame owner, FamilyPlanning fp, boolean inserting) {
        super(owner, true);
        this.fp = fp;
        this.visit = new FamilyPlanningVisit();
        this.visit.setFamilyPlanning(fp);
        this.insert = inserting;
        initManagers();
        initialize();
        pack();
        setLocationRelativeTo(owner);
    }

    public FamilyPlanningVisitEdit(JFrame owner, FamilyPlanningVisit existingVisit, boolean inserting) {
        super(owner, true);
        this.fp = existingVisit.getFamilyPlanning();
        this.visit = existingVisit;
        this.insert = inserting;
        initManagers();
        initialize();
        if (!insert) {
            loadExistingData();
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        visitManager = Context.getApplicationContext().getBean(FamilyPlanningVisitBrowserManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.familyplanning.newvisit.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.familyplanning.editvisit.title"));
        }
        setMinimumSize(new Dimension(500, 400));
        setPreferredSize(new Dimension(550, 450));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (visit != null && !insert) {
            if (visit.getVisitDate() != null) {
                visitDateField.setDateTime(visit.getVisitDate());
            }
            if (visit.getVisitType() != null) {
                visitTypeCombo.setSelectedItem(visit.getVisitType());
            }
            if (visit.getComplaints() != null) {
                complaintsArea.setText(visit.getComplaints());
            }
            if (visit.getNotes() != null) {
                notesArea.setText(visit.getNotes());
            }
            if (visit.getNextAppointmentDate() != null) {
                nextAppointmentDateField.setDate(visit.getNextAppointmentDate());
            }
        }
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(getDataPanel(), BorderLayout.CENTER);
        }
        return mainPanel;
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
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.visitdate.label") + ":"), gbc);

            gbc.gridx = 1;
            visitDateField = new GoodDateTimeSpinnerChooser(TimeTools.getNow());
            dataPanel.add(visitDateField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.visittype.label") + ":"), gbc);

            gbc.gridx = 1;
            visitTypeCombo = new JComboBox<>(FPVisitType.values());
            visitTypeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                               boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof FPVisitType visitType) {
                        return super.getListCellRendererComponent(list,
                                MessageBundle.getMessage(visitType.getKey()), index, isSelected, cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            dataPanel.add(visitTypeCombo, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.complaints.label") + ":"), gbc);

            gbc.gridx = 1;
            complaintsArea = new JTextArea(3, 30);
            complaintsArea.setLineWrap(true);
            complaintsArea.setWrapStyleWord(true);
            JScrollPane complaintsScroll = new JScrollPane(complaintsArea);
            complaintsScroll.setPreferredSize(new Dimension(250, 60));
            dataPanel.add(complaintsScroll, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.visitnotes.label") + ":"), gbc);

            gbc.gridx = 1;
            notesArea = new JTextArea(4, 30);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            JScrollPane notesScroll = new JScrollPane(notesArea);
            notesScroll.setPreferredSize(new Dimension(250, 80));
            dataPanel.add(notesScroll, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.WEST;
            dataPanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.nextappointment.label") + ":"), gbc);

            gbc.gridx = 1;
            nextAppointmentDateField = new GoodDateChooser(LocalDate.now(), true, true);
            dataPanel.add(nextAppointmentDateField, gbc);
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
        LocalDateTime visitDate = visitDateField.getLocalDateTime();
        if (visitDate == null) {
            MessageDialog.error(this, "angal.maternity.fpvisitdaterequired.msg");
            return;
        }

        if (visitDate.isAfter(LocalDateTime.now())) {
            MessageDialog.error(this, "angal.maternity.fpvisitdatecannotbeinfuture.msg");
            return;
        }

        FPVisitType visitType = (FPVisitType) visitTypeCombo.getSelectedItem();
        if (visitType == null) {
            MessageDialog.error(this, "angal.maternity.fpvisittyperequired.msg");
            return;
        }

        String complaints = complaintsArea.getText().trim();
        String notes = notesArea.getText().trim();
        LocalDate nextAppointment = nextAppointmentDateField.getDate();

        visit.setFamilyPlanning(fp);
        visit.setVisitDate(visitDate);
        visit.setVisitType(visitType);
        visit.setComplaints(complaints);
        visit.setNotes(notes);
        visit.setNextAppointmentDate(nextAppointment);

        try {
            if (insert) {
                FamilyPlanningVisit saved = visitManager.newVisit(visit);
                fireVisitInserted(saved);
            } else {
                FamilyPlanningVisit saved = visitManager.updateVisit(visit);
                fireVisitUpdated(saved);
            }
            dispose();
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }
}
