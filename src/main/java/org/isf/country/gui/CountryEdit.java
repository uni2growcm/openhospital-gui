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
package org.isf.country.gui;

import org.isf.country.manager.CountryBrowserManager;
import org.isf.country.model.Country;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * CountryEdit - form to add or edit a country. Validate ISO code and phone code format
 */
public class CountryEdit extends ModalJFrame {

    private CountryBrowserManager manager;
    private Country country;
    private CountryBrowser parent;

    private JTextField codeField;
    private JTextField nameField;
    private JTextField phoneCodeField;

    private Consumer<Country> onSaveCallback;

    public CountryEdit(CountryBrowser parent, CountryBrowserManager manager, Country country, Consumer<Country> onSaveCallback) {
        this.parent = parent;
        this.manager = manager;
        this.country = country;
        this.onSaveCallback = onSaveCallback;
        initComponents();
        setTitle(country == null || country.getId() == 0
                ? MessageBundle.getMessage("angal.country.new.title")
                : MessageBundle.getMessage("angal.country.edit.title"));
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
        codeField = new JTextField(2);
        nameField = new JTextField(20);
        phoneCodeField = new JTextField(8);

        applyCodeFieldFilter();
        applyPhoneCodeFieldFilter();

        if (country != null && country.getId() != 0) {
            codeField.setText(country.getIsoCode());
            nameField.setText(country.getName());
            phoneCodeField.setText(country.getPhoneCode());
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.country.isocode.label"));
        codeLabel.setPreferredSize(new Dimension(100, 25));
        fields.add(codeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.country.name.label"));
        nameLabel.setPreferredSize(new Dimension(100, 25));
        fields.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel(MessageBundle.getMessage("angal.country.phonecode.label"));
        phoneLabel.setPreferredSize(new Dimension(100, 25));
        fields.add(phoneLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(phoneCodeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);

        JLabel requiredLabel = new JLabel(MessageBundle.getMessage("angal.country.requiredfields.txt"));
        requiredLabel.setFont(requiredLabel.getFont().deriveFont(Font.BOLD));
        requiredLabel.setForeground(Color.BLACK);
        fields.add(requiredLabel, gbc);

        add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton saveBtn = new JButton(MessageBundle.getMessage("angal.country.save.btn"));
        JButton cancelBtn = new JButton(MessageBundle.getMessage("angal.country.cancel.btn"));

        saveBtn.setMnemonic(MessageBundle.getMnemonic("angal.country.save.btn.key"));
        cancelBtn.setMnemonic(MessageBundle.getMnemonic("angal.country.cancel.btn.key"));

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void applyCodeFieldFilter() {
        ((AbstractDocument) codeField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;

                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());

                if (currentText.length() + string.length() > 2) {
                    return;
                }

                String newString = string.toUpperCase();
                if (newString.matches("[A-Z]*")) {
                    super.insertString(fb, offset, newString, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;

                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                if (newText.length() > 2) {
                    return;
                }

                String newString = text.toUpperCase();
                if (newString.matches("[A-Z]*")) {
                    super.replace(fb, offset, length, newString, attrs);
                }
            }
        });
    }

    private void applyPhoneCodeFieldFilter() {
        ((AbstractDocument) phoneCodeField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;

                if (string.matches("[0-9()]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;

                if (text.matches("[0-9()]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void save() {
        String code = codeField.getText().trim().toUpperCase();
        String name = nameField.getText().trim();
        String phoneCode = phoneCodeField.getText().trim();

        if (code.isEmpty()) {
            MessageDialog.error(this, "angal.country.isocode.required.msg");
            codeField.requestFocus();
            return;
        }

        if (code.length() != 2) {
            MessageDialog.error(this, "angal.country.isocode.toolong.msg");
            codeField.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            MessageDialog.error(this, "angal.country.name.required.msg");
            nameField.requestFocus();
            return;
        }

        try {

            if (country == null || country.getId() == 0) {

                Optional<Country> existingCountry = manager.getCountryByIsoCode(code);
                if (existingCountry.isPresent()) {
                    MessageDialog.error(this, MessageBundle.formatMessage("angal.country.isocode.exists.msg", code));
                    codeField.requestFocus();
                    return;
                }
                country = new Country();
            } else {

                Optional<Country> existingCountry = manager.getCountryByIsoCode(code);
                if (existingCountry.isPresent() && existingCountry.get().getId() != country.getId()) {
                    MessageDialog.error(this, MessageBundle.formatMessage("angal.country.isocode.exists.msg", code));
                    codeField.requestFocus();
                    return;
                }
            }

            country.setIsoCode(code);
            country.setName(name);
            country.setPhoneCode(phoneCode);
            manager.saveCountry(country);

            if (onSaveCallback != null) {
                onSaveCallback.accept(country);
            }
            dispose();
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.country.save.error.msg"));
        }
    }
}