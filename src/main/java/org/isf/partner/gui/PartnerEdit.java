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
package org.isf.partner.gui;

import org.isf.partner.manager.PartnerBrowserManager;
import org.isf.partner.model.Partner;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;


public class PartnerEdit extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerEdit.class);

    private PartnerBrowserManager partnerManager;
    private TypologyBrowserManager typologyManager;
    private Partner partner;
    private PartnerBrowser parent;

    private JTextField nameField;
    private JComboBox<Typology> typeCombo;
    private JTextField contactPersonField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextArea notesArea;

    private Consumer<Partner> onSaveCallback;

    public PartnerEdit(PartnerBrowser parent, PartnerBrowserManager manager, Partner partner, Consumer<Partner> onSaveCallback) {
        this.parent = parent;
        this.partnerManager = manager;
        this.typologyManager = Context.getApplicationContext().getBean(TypologyBrowserManager.class);
        this.partner = partner;
        this.onSaveCallback = onSaveCallback;
        initComponents();
        setTitle(partner == null || partner.getCode() == null || partner.getCode() == 0
                ? MessageBundle.getMessage("angal.partner.new.title")
                : MessageBundle.getMessage("angal.partner.edit.title"));
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 10));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(30);
        typeCombo = new JComboBox<>();
        loadPartnerTypes();

        contactPersonField = new JTextField(25);
        phoneField = new JTextField(15);
        emailField = new JTextField(25);
        addressField = new JTextField(35);
        notesArea = new JTextArea(3, 35);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        if (partner != null && partner.getCode() != null && partner.getCode() != 0) {
            nameField.setText(partner.getName());
            if (partner.getType() != null) {
                typeCombo.setSelectedItem(partner.getType());
            }
            contactPersonField.setText(partner.getContactPerson());
            phoneField.setText(partner.getPhone());
            emailField.setText(partner.getEmail());
            addressField.setText(partner.getAddress());
            notesArea.setText(partner.getNotes());
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.partner.name.label") + " *");
        nameLabel.setPreferredSize(new Dimension(120, 25));
        fields.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel typeLabel = new JLabel(MessageBundle.getMessage("angal.partner.type.label"));
        fields.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel contactLabel = new JLabel(MessageBundle.getMessage("angal.partner.contactperson.label"));
        fields.add(contactLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(contactPersonField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel(MessageBundle.getMessage("angal.partner.phone.label"));
        fields.add(phoneLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        JLabel emailLabel = new JLabel(MessageBundle.getMessage("angal.partner.email.label"));
        fields.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        JLabel addressLabel = new JLabel(MessageBundle.getMessage("angal.partner.address.label"));
        fields.add(addressLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        JLabel notesLabel = new JLabel(MessageBundle.getMessage("angal.partner.notes.label"));
        fields.add(notesLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(notesScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);

        JLabel requiredLabel = new JLabel(MessageBundle.getMessage("angal.partner.requiredfields.txt"));
        requiredLabel.setFont(requiredLabel.getFont().deriveFont(Font.BOLD));
        requiredLabel.setForeground(Color.BLACK);
        fields.add(requiredLabel, gbc);

        add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton saveBtn = new JButton(MessageBundle.getMessage("angal.partner.save.btn"));
        JButton cancelBtn = new JButton(MessageBundle.getMessage("angal.partner.cancel.btn"));

        saveBtn.setMnemonic(MessageBundle.getMnemonic("angal.partner.save.btn.key"));
        cancelBtn.setMnemonic(MessageBundle.getMnemonic("angal.partner.cancel.btn.key"));

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadPartnerTypes() {
        try {
            List<Typology> types = typologyManager.getTypologies(Family.PARTNERTYPE);
            typeCombo.removeAllItems();
            typeCombo.addItem(null);
            for (Typology type : types) {
                typeCombo.addItem(type);
            }
        } catch (OHServiceException e) {
            LOGGER.error("Error loading partner types", e);
        }
    }

    private void save() {
        String name = nameField.getText().trim();
        Typology type = (Typology) typeCombo.getSelectedItem();
        String contactPerson = contactPersonField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String notes = notesArea.getText().trim();

        if (name.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.partner.name.required.msg"));
            nameField.requestFocus();
            return;
        }

        if (type == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.partner.type.required.msg"));
            typeCombo.requestFocus();
            return;
        }

        try {
            boolean isNew = (partner == null || partner.getCode() == null || partner.getCode() == 0);

            if (isNew) {
                partner = new Partner();
            }

            partner.setName(name);
            partner.setType(type);
            partner.setContactPerson(contactPerson);
            partner.setPhone(phone);
            partner.setEmail(email);
            partner.setAddress(address);
            partner.setNotes(notes);

            Partner savedPartner = partnerManager.savePartner(partner);

            if (onSaveCallback != null) {
                onSaveCallback.accept(savedPartner);
            }
            dispose();
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.partner.save.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }
}