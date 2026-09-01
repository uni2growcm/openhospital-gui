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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.isf.articlefamily.manager.ArticleFamilyBrowserManager;
import org.isf.articlefamily.model.ArticleFamily;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;

/**
 * Add/edit dialog for a single {@link ArticleFamily}.
 */
public class ArticleFamilyEdit extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private final ArticleFamilyBrowserManager articleFamilyManager;
	private ArticleFamily articleFamily;
	private final boolean insert;
	private final Consumer<ArticleFamily> onSaveCallback;

	private JTextField codeField;
	private JTextField descriptionField;

	public ArticleFamilyEdit(ArticleFamilyBrowser parent, ArticleFamilyBrowserManager articleFamilyManager, ArticleFamily articleFamily,
					Consumer<ArticleFamily> onSaveCallback) {
		super();
		this.articleFamilyManager = articleFamilyManager;
		this.insert = articleFamily == null;
		this.articleFamily = insert ? new ArticleFamily() : articleFamily;
		this.onSaveCallback = onSaveCallback;
		initComponents();
		setTitle(insert
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

		codeField = new VoLimitedTextField(50, 15);
		descriptionField = new VoLimitedTextField(255, 30);
		if (!insert) {
			codeField.setText(articleFamily.getCode());
			descriptionField.setText(articleFamily.getDescription());
		}

		gbc.gridx = 0;
		gbc.gridy = 0;
		fields.add(new JLabel(MessageBundle.getMessage("angal.articlefamily.code.label")), gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fields.add(codeField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		fields.add(new JLabel(MessageBundle.getMessage("angal.articlefamily.description.label")), gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fields.add(descriptionField, gbc);

		add(fields, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		saveButton.addActionListener(actionEvent -> save());
		cancelButton.addActionListener(actionEvent -> dispose());
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void save() {
		String code = codeField.getText().trim().toUpperCase();
		String description = descriptionField.getText().trim();

		if (code.isEmpty()) {
			MessageDialog.error(this, "angal.common.pleaseinsertacode.msg");
			codeField.requestFocus();
			return;
		}
		if (description.isEmpty()) {
			MessageDialog.error(this, "angal.common.pleaseinsertavaliddescription.msg");
			descriptionField.requestFocus();
			return;
		}

		try {
			Optional<ArticleFamily> existing = articleFamilyManager.getArticleFamilyByCode(code);
			if (existing.isPresent() && (insert || existing.get().getId() != articleFamily.getId())) {
				MessageDialog.error(this, "angal.common.thecodeisalreadyinuse.msg");
				codeField.requestFocus();
				return;
			}

			articleFamily.setCode(code);
			articleFamily.setDescription(description);
			articleFamily = articleFamilyManager.saveArticleFamily(articleFamily);

			if (onSaveCallback != null) {
				onSaveCallback.accept(articleFamily);
			}
			dispose();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
