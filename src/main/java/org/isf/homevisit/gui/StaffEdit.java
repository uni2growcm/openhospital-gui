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
package org.isf.homevisit.gui;

import org.isf.homevisit.manager.StaffBrowserManager;
import org.isf.homevisit.model.Staff;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import java.util.function.Consumer;

public class StaffEdit extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaffEdit.class);

    private StaffBrowserManager manager;
    private Staff staff;
    private StaffBrowser parent;
    private Consumer<Staff> onSaveCallback;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField professionField;
    private JTextField positionField;
    private JTextField phoneField;

    public StaffEdit(StaffBrowser parent, StaffBrowserManager manager, Staff staff, Consumer<Staff> onSaveCallback) {
        this.parent = parent;
        this.manager = manager;
        this.staff = staff;
        this.onSaveCallback = onSaveCallback;
        initComponents();
        setTitle(staff == null || staff.getCode() == null || staff.getCode() == 0
                ? MessageBundle.getMessage("angal.staff.new.title")
                : MessageBundle.getMessage("angal.staff.edit.title"));
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

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        professionField = new JTextField(20);
        positionField = new JTextField(20);
        phoneField = new JTextField(15);

        if (staff != null && staff.getCode() != null && staff.getCode() != 0) {
            firstNameField.setText(staff.getFirstName());
            lastNameField.setText(staff.getLastName());
            professionField.setText(staff.getProfession());
            positionField.setText(staff.getPosition());
            phoneField.setText(staff.getPhone());
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel firstNameLabel = new JLabel(MessageBundle.getMessage("angal.staff.firstname.label") + " *");
        firstNameLabel.setPreferredSize(new Dimension(120, 25));
        fields.add(firstNameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(firstNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lastNameLabel = new JLabel(MessageBundle.getMessage("angal.staff.lastname.label") + " *");
        fields.add(lastNameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(lastNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel professionLabel = new JLabel(MessageBundle.getMessage("angal.staff.profession.label"));
        fields.add(professionLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(professionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        JLabel positionLabel = new JLabel(MessageBundle.getMessage("angal.staff.position.label") + " *");
        fields.add(positionLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(positionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel(MessageBundle.getMessage("angal.staff.phone.label"));
        fields.add(phoneLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);

        JLabel requiredLabel = new JLabel(MessageBundle.getMessage("angal.common.requiredfields"));
        requiredLabel.setFont(requiredLabel.getFont().deriveFont(Font.BOLD));
        requiredLabel.setForeground(Color.BLACK);
        fields.add(requiredLabel, gbc);

        add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton saveBtn = new JButton(MessageBundle.getMessage("angal.staff.save.btn"));
        JButton cancelBtn = new JButton(MessageBundle.getMessage("angal.staff.cancel.btn"));

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void save() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String profession = professionField.getText().trim();
        String position = positionField.getText().trim();
        String phone = phoneField.getText().trim();

        if (firstName.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.staff.validation.firstname.required.msg"));
            firstNameField.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.staff.validation.lastname.required.msg"));
            lastNameField.requestFocus();
            return;
        }

        if (position.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.staff.validation.position.required.msg"));
            positionField.requestFocus();
            return;
        }

        try {
            boolean isNew = (staff == null || staff.getCode() == null || staff.getCode() == 0);

            if (isNew) {
                staff = new Staff();
            }

            staff.setFirstName(firstName);
            staff.setLastName(lastName);
            staff.setProfession(profession);
            staff.setPosition(position);
            staff.setPhone(phone);

            Staff savedStaff = manager.saveStaff(staff);

            if (onSaveCallback != null) {
                onSaveCallback.accept(savedStaff);
            }
            dispose();
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.staff.save.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }
}