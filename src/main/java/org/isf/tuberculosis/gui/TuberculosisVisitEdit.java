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
package org.isf.tuberculosis.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.time.LocalDateTime;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.tuberculosis.manager.TuberculosisVisitManager;
import org.isf.tuberculosis.model.*;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;

public class TuberculosisVisitEdit extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private TuberculosisVisit visit;
    private final TuberculosisTreatment treatment;
    private final boolean insert;

    private TuberculosisVisitManager manager;

    private GoodDateTimeSpinnerChooser visitDateField;
    private JCheckBox symptomsImprovedCheck;
    private JTextField adherenceField;
    private JComboBox<DotStatus> dotStatusCombo;
    private JTextArea sideEffectsArea;
    private JTextArea notesArea;

    public TuberculosisVisitEdit(JFrame owner, TuberculosisTreatment treatment, boolean inserting) {
        super(owner, true);
        this.treatment = treatment;
        this.visit = new TuberculosisVisit(treatment, LocalDateTime.now());
        this.insert = inserting;
        initManagers();
        initialize();
        pack();
        setLocationRelativeTo(owner);
    }

    public TuberculosisVisitEdit(JFrame owner, TuberculosisVisit visit, boolean inserting) {
        super(owner, true);
        this.visit = visit;
        this.treatment = visit.getTreatment();
        this.insert = inserting;
        initManagers();
        initialize();
        loadExistingData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        manager = Context.getApplicationContext().getBean(TuberculosisVisitManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.tb.visit.newtitle"));
        } else {
            setTitle(MessageBundle.getMessage("angal.tb.visit.edittitle"));
        }
        setMinimumSize(new Dimension(450, 400));
        setPreferredSize(new Dimension(500, 450));
        add(getMainPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (visit != null && !insert) {
            if (visit.getVisitDate() != null) {
                visitDateField.setDateTime(visit.getVisitDate());
            }
            symptomsImprovedCheck.setSelected(visit.getSymptomsImproved() != null && visit.getSymptomsImproved());
            if (visit.getAdherence() != null) {
                adherenceField.setText(String.valueOf(visit.getAdherence()));
            }
            if (visit.getDotStatus() != null) {
                dotStatusCombo.setSelectedItem(visit.getDotStatus());
            }
            if (visit.getSideEffects() != null) {
                sideEffectsArea.setText(visit.getSideEffects());
            }
            if (visit.getNotes() != null) {
                notesArea.setText(visit.getNotes());
            }
        }
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(getClinicalPanel(), BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel getClinicalPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.visit.visitdate") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        visitDateField = new GoodDateTimeSpinnerChooser(LocalDateTime.now());
        panel.add(visitDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.gridwidth = 2;
        symptomsImprovedCheck = new JCheckBox(MessageBundle.getMessage("angal.tb.visit.symptomsimproved"));
        panel.add(symptomsImprovedCheck, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.visit.adherence") + " (%):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        adherenceField = new JTextField(5);
        panel.add(adherenceField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.visit.dotstatus") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dotStatusCombo = new JComboBox<>(DotStatus.values());
        dotStatusCombo.setRenderer(new EnumRenderer());
        panel.add(dotStatusCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.visit.sideeffects") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        sideEffectsArea = new JTextArea(3, 25);
        sideEffectsArea.setLineWrap(true);
        sideEffectsArea.setWrapStyleWord(true);
        JScrollPane sideScroll = new JScrollPane(sideEffectsArea);
        sideScroll.setPreferredSize(new Dimension(250, 60));
        panel.add(sideScroll, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.notes.txt") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        notesArea = new JTextArea(5, 25);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(250, 100));
        panel.add(notesScroll, gbc);

        return panel;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.addActionListener(e -> save());

        JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void save() {
        if (visitDateField.getLocalDateTime() == null) {
            MessageDialog.error(this, "angal.tb.visit.visitdaterequired.msg");
            return;
        }

        visit.setVisitDate(visitDateField.getLocalDateTime());
        visit.setSymptomsImproved(symptomsImprovedCheck.isSelected() ? true : null);

        String adherenceText = adherenceField.getText().trim();
        if (!adherenceText.isEmpty()) {
            try {
                int adherence = Integer.parseInt(adherenceText);
                if (adherence < 0 || adherence > 100) {
                    MessageDialog.error(this, "angal.tb.visit.adherencerange.msg");
                    return;
                }
                visit.setAdherence(adherence);
            } catch (NumberFormatException e) {
                MessageDialog.error(this, "angal.tb.visit.invalidadherence.msg");
                return;
            }
        }

        visit.setDotStatus((DotStatus) dotStatusCombo.getSelectedItem());
        visit.setSideEffects(sideEffectsArea.getText().trim());
        visit.setNotes(notesArea.getText().trim());

        try {
            if (insert) {
                manager.newVisit(visit);
            } else {
                manager.updateVisit(visit);
            }
            dispose();
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private static class EnumRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            if (value != null && value instanceof Enum<?> enumValue) {
                return super.getListCellRendererComponent(list,
                        enumValue.toString(), index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
        }
    }
}
