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

package org.isf.articlefamily.gui;

import org.isf.articlefamily.manager.ArticleFamilyBrowserManager;
import org.isf.articlefamily.model.ArticleFamily;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.BorderFactory;
import javax.swing.text.DocumentFilter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.util.Optional;
import java.util.function.Consumer;

public class ArticleFamilyEdit extends ModalJFrame {

    private ArticleFamilyBrowserManager manager;
    private ArticleFamily articleFamily;
    private ArticleFamilyBrowser parent;

    private JTextField codeField;
    private JTextField descriptionField;
    private JTextArea notesArea;

    private Consumer<ArticleFamily> onSaveCallback;

    public ArticleFamilyEdit(
            ArticleFamilyBrowser parent,
            ArticleFamilyBrowserManager manager,
            ArticleFamily articleFamily,
            Consumer<ArticleFamily> onSaveCallback) {
        this.parent = parent;
        this.manager = manager;
        this.articleFamily = articleFamily;
        this.onSaveCallback = onSaveCallback;
        initComponents();
        setTitle(articleFamily == null || articleFamily.getId() == 0
                ? MessageBundle.getMessage("angal.articlefamily.new.title")
                : MessageBundle.getMessage("angal.articlefamily.edit.title"));
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

        codeField = new JTextField(15);
        descriptionField = new JTextField(30);
        notesArea = new JTextArea(4, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        applyCodeFieldFilter();

        if (articleFamily != null && articleFamily.getId() != 0) {
            codeField.setText(articleFamily.getCode());
            descriptionField.setText(articleFamily.getDescription());
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.articlefamily.code.label"));
        codeLabel.setPreferredSize(new Dimension(120, 25));
        fields.add(codeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(codeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.articlefamily.description.label"));
        descLabel.setPreferredSize(new Dimension(120, 25));
        fields.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fields.add(descriptionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(10, 5, 5, 5);

        JLabel requiredLabel = new JLabel(MessageBundle.getMessage("angal.articlefamily.requiredfields.txt"));
        requiredLabel.setFont(requiredLabel.getFont().deriveFont(Font.BOLD));
        requiredLabel.setForeground(Color.BLACK);
        fields.add(requiredLabel, gbc);

        add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton saveBtn = new JButton(MessageBundle.getMessage("angal.articlefamily.save.btn"));
        JButton cancelBtn = new JButton(MessageBundle.getMessage("angal.articlefamily.cancel.btn"));

        saveBtn.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.save.btn.key"));
        cancelBtn.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.cancel.btn.key"));

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
                if (string == null) {
                    return;
                }

                String newString = string.toUpperCase();
                if (newString.matches("[A-Z0-9_]*")) {
                    super.insertString(fb, offset, newString, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }

                String newString = text.toUpperCase();
                if (newString.matches("[A-Z0-9_]*")) {
                    super.replace(fb, offset, length, newString, attrs);
                }
            }
        });
    }

    private void save() {
        String code = codeField.getText().trim().toUpperCase();
        String description = descriptionField.getText().trim();

        if (code.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.articlefamily.code.required.msg"));
            codeField.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.articlefamily.description.required.msg"));
            descriptionField.requestFocus();
            return;
        }

        try {
            if (articleFamily == null || articleFamily.getId() == 0) {
                Optional<ArticleFamily> existing = manager.getArticleFamilyByCode(code);
                if (existing.isPresent()) {
                    MessageDialog.error(
                            this,
                            MessageBundle.formatMessage("angal.articlefamily.code.exists.msg", code)
                    );
                    codeField.requestFocus();
                    return;
                }
                articleFamily = new ArticleFamily();
            } else {
                Optional<ArticleFamily> existing = manager.getArticleFamilyByCode(code);
                if (existing.isPresent() && existing.get().getId() != articleFamily.getId()) {
                    MessageDialog.error(
                            this,
                            MessageBundle.formatMessage("angal.articlefamily.code.exists.msg", code)
                    );
                    codeField.requestFocus();
                    return;
                }
            }

            articleFamily.setCode(code);
            articleFamily.setDescription(description);

            manager.saveArticleFamily(articleFamily);

            if (onSaveCallback != null) {
                onSaveCallback.accept(articleFamily);
            }
            dispose();

        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.articlefamily.save.error.msg"));
        }
    }
}