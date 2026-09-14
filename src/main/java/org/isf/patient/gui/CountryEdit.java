/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.patient.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.CountryBrowserManager;
import org.isf.patient.model.Country;
import org.isf.utils.jobjects.MessageDialog;

/**
 * CountryEdit - create a new {@link Country} inline from the patient form.
 */
public class CountryEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton okButton;
	private JButton cancelButton;
	private JTextField codeTextField;
	private JTextField nameTextField;
	private JTextField phoneCodeTextField;
	private final Country country;
	private boolean saved;

	private final CountryBrowserManager countryBrowserManager = Context.getApplicationContext().getBean(CountryBrowserManager.class);

	public CountryEdit(JFrame parent, Country country) {
		super(parent, true);
		this.country = country;
		initialize();
	}

	public CountryEdit(JDialog parent, Country country) {
		super(parent, true);
		this.country = country;
		initialize();
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.patient.newcountry.title"));
		this.setContentPane(getDataPanel());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
	}

	public boolean isSaved() {
		return saved;
	}

	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel();
			dataPanel.setLayout(new BorderLayout());

			JPanel fieldsPanel = new JPanel(new GridBagLayout());

			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.patient.country.code"));
			GridBagConstraints gbcCodeLabel = new GridBagConstraints();
			gbcCodeLabel.insets = new Insets(5, 5, 5, 5);
			gbcCodeLabel.gridx = 0;
			gbcCodeLabel.gridy = 0;
			fieldsPanel.add(codeLabel, gbcCodeLabel);

			GridBagConstraints gbcCodeField = new GridBagConstraints();
			gbcCodeField.fill = GridBagConstraints.HORIZONTAL;
			gbcCodeField.insets = new Insets(5, 5, 5, 5);
			gbcCodeField.gridx = 1;
			gbcCodeField.gridy = 0;
			fieldsPanel.add(getCodeTextField(), gbcCodeField);

			JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.patient.country.name"));
			GridBagConstraints gbcNameLabel = new GridBagConstraints();
			gbcNameLabel.insets = new Insets(5, 5, 5, 5);
			gbcNameLabel.gridx = 0;
			gbcNameLabel.gridy = 1;
			fieldsPanel.add(nameLabel, gbcNameLabel);

			GridBagConstraints gbcNameField = new GridBagConstraints();
			gbcNameField.fill = GridBagConstraints.HORIZONTAL;
			gbcNameField.insets = new Insets(5, 5, 5, 5);
			gbcNameField.gridx = 1;
			gbcNameField.gridy = 1;
			fieldsPanel.add(getNameTextField(), gbcNameField);

			JLabel phoneCodeLabel = new JLabel(MessageBundle.getMessage("angal.patient.country.phonecode"));
			GridBagConstraints gbcPhoneCodeLabel = new GridBagConstraints();
			gbcPhoneCodeLabel.insets = new Insets(5, 5, 5, 5);
			gbcPhoneCodeLabel.gridx = 0;
			gbcPhoneCodeLabel.gridy = 2;
			fieldsPanel.add(phoneCodeLabel, gbcPhoneCodeLabel);

			GridBagConstraints gbcPhoneCodeField = new GridBagConstraints();
			gbcPhoneCodeField.fill = GridBagConstraints.HORIZONTAL;
			gbcPhoneCodeField.insets = new Insets(5, 5, 5, 5);
			gbcPhoneCodeField.gridx = 1;
			gbcPhoneCodeField.gridy = 2;
			fieldsPanel.add(getPhoneCodeTextField(), gbcPhoneCodeField);

			dataPanel.add(fieldsPanel, BorderLayout.CENTER);
			dataPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return dataPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				String code = codeTextField.getText().trim();
				String name = nameTextField.getText().trim();
				if (code.isEmpty()) {
					MessageDialog.error(this, "angal.patient.country.pleaseinsertacode.msg");
					return;
				}
				if (name.isEmpty()) {
					MessageDialog.error(this, "angal.patient.country.pleaseinsertaname.msg");
					return;
				}
				country.setCode(code);
				country.setName(name);
				country.setPhoneCode(phoneCodeTextField.getText().trim());
				countryBrowserManager.newCountry(country);
				saved = true;
				dispose();
			});
		}
		return okButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new JTextField(10);
		}
		return codeTextField;
	}

	private JTextField getNameTextField() {
		if (nameTextField == null) {
			nameTextField = new JTextField(20);
		}
		return nameTextField;
	}

	private JTextField getPhoneCodeTextField() {
		if (phoneCodeTextField == null) {
			phoneCodeTextField = new JTextField(10);
		}
		return phoneCodeTextField;
	}
}
