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
import java.time.LocalDate;
import java.util.List;

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
import org.isf.tuberculosis.manager.TuberculosisContactManager;
import org.isf.tuberculosis.model.TuberculosisContact;
import org.isf.tuberculosis.model.TuberculosisTreatment;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class TuberculosisContactEdit extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private TuberculosisContact contact;
    private final TuberculosisTreatment treatment;
    private final boolean insert;

    private TuberculosisContactManager manager;
    private List<Typology> relationshipTypologies;

    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderCombo;
    private JComboBox<Typology> relationshipCombo;
    private JCheckBox screenedCheck;
    private GoodDateChooser screeningDateField;
    private JCheckBox tbInfectedCheck;
    private JCheckBox tbDiseaseCheck;
    private JCheckBox tptStartedCheck;
    private JTextArea notesArea;

    public TuberculosisContactEdit(JFrame owner, TuberculosisTreatment treatment, boolean inserting) {
        super(owner, true);
        this.treatment = treatment;
        this.contact = new TuberculosisContact(treatment, "");
        this.insert = inserting;
        initManagers();
        initialize();
        pack();
        setLocationRelativeTo(owner);
    }

    public TuberculosisContactEdit(JFrame owner, TuberculosisContact contact, boolean inserting) {
        super(owner, true);
        this.contact = contact;
        this.treatment = contact.getTreatment();
        this.insert = inserting;
        initManagers();
        initialize();
        loadExistingData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initManagers() {
        manager = Context.getApplicationContext().getBean(TuberculosisContactManager.class);
        loadRelationshipTypologies();
    }

    private void loadRelationshipTypologies() {
        try {
            relationshipTypologies = Context.getApplicationContext()
                    .getBean(TypologyBrowserManager.class)
                    .getTypologies(Family.TUBERCULOSISCONTACT);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            relationshipTypologies = List.of();
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());
        if (insert) {
            setTitle(MessageBundle.getMessage("angal.tb.contact.newtitle"));
        } else {
            setTitle(MessageBundle.getMessage("angal.tb.contact.edittitle"));
        }
        setMinimumSize(new Dimension(500, 400));
        setPreferredSize(new Dimension(550, 450));
        add(getDataPanel(), BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private void loadExistingData() {
        if (contact != null && !insert) {
            nameField.setText(contact.getName());
            if (contact.getAge() != null) {
                ageField.setText(String.valueOf(contact.getAge()));
            }
            if (contact.getGender() != null) {
                String genderStr = String.valueOf(contact.getGender());
                for (int i = 0; i < genderCombo.getItemCount(); i++) {
                    if (genderCombo.getItemAt(i).equals(genderStr)) {
                        genderCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (contact.getRelationship() != null && relationshipTypologies != null) {
                for (Typology typology : relationshipTypologies) {
                    if (typology.getCode().equals(contact.getRelationship())) {
                        relationshipCombo.setSelectedItem(typology);
                        break;
                    }
                }
            }
            screenedCheck.setSelected(contact.getScreened() != null && contact.getScreened());
            if (contact.getScreeningDate() != null) {
                screeningDateField.setDate(contact.getScreeningDate());
            }
            tbInfectedCheck.setSelected(contact.getTbInfected() != null && contact.getTbInfected());
            tbDiseaseCheck.setSelected(contact.getTbDisease() != null && contact.getTbDisease());
            tptStartedCheck.setSelected(contact.getTptStarted() != null && contact.getTptStarted());
            if (contact.getNotes() != null) {
                notesArea.setText(contact.getNotes());
            }
        }
    }

    private JPanel getDataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.contact.information")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.name.txt") + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new VoLimitedTextField(100, 30);
        panel.add(nameField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.contact.age") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        ageField = new JTextField(5);
        panel.add(ageField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.common.gender.txt") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        genderCombo = new JComboBox<>(new String[] { "", "M", "F" });
        panel.add(genderCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.contact.relationship") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        relationshipCombo = new JComboBox<>();
        relationshipCombo.addItem(null);
        if (relationshipTypologies != null) {
            for (Typology typology : relationshipTypologies) {
                relationshipCombo.addItem(typology);
            }
        }
        relationshipCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Typology typology) {
                    return super.getListCellRendererComponent(list,
                            typology.getDescription(), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            }
        });
        panel.add(relationshipCombo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        screenedCheck = new JCheckBox(MessageBundle.getMessage("angal.tb.contact.screened"));
        panel.add(screenedCheck, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        screeningDateField = new GoodDateChooser((LocalDate) null);
        panel.add(screeningDateField, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        tbInfectedCheck = new JCheckBox(MessageBundle.getMessage("angal.tb.contact.tbinfected"));
        panel.add(tbInfectedCheck, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tbDiseaseCheck = new JCheckBox(MessageBundle.getMessage("angal.tb.contact.tbdisease"));
        panel.add(tbDiseaseCheck, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        tptStartedCheck = new JCheckBox(MessageBundle.getMessage("angal.tb.contact.tptstarted"));
        panel.add(tptStartedCheck, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(MessageBundle.getMessage("angal.tb.contact.notes") + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        notesArea = new JTextArea(3, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 60));
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
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            MessageDialog.error(this, "angal.tb.contact.namerequired.msg");
            return;
        }

        contact.setName(name);

        String ageText = ageField.getText().trim();
        if (!ageText.isEmpty()) {
            try {
                contact.setAge(Integer.parseInt(ageText));
            } catch (NumberFormatException e) {
                MessageDialog.error(this, "angal.common.pleaseinsertavalidnumber.msg");
                return;
            }
        } else {
            contact.setAge(null);
        }

        String gender = (String) genderCombo.getSelectedItem();
        if (gender != null && !gender.isEmpty()) {
            contact.setGender(gender.charAt(0));
        } else {
            contact.setGender(null);
        }

        Typology selectedTypology = (Typology) relationshipCombo.getSelectedItem();
        contact.setRelationship(selectedTypology != null ? selectedTypology.getCode() : null);

        contact.setScreened(screenedCheck.isSelected());
        contact.setScreeningDate(screeningDateField.getDate());
        contact.setTbInfected(tbInfectedCheck.isSelected());
        contact.setTbDisease(tbDiseaseCheck.isSelected());
        contact.setTptStarted(tptStartedCheck.isSelected());
        contact.setNotes(notesArea.getText().trim());

        try {
            if (insert) {
                manager.newContact(contact);
            } else {
                manager.updateContact(contact);
            }
            dispose();
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }
}
