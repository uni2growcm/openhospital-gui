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
package org.isf.prescriber.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.prescriber.manager.PrescriberBrowserManager;
import org.isf.prescriber.model.Prescriber;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;

public class PrescriberEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final PrescriberBrowserManager manager = Context.getApplicationContext().getBean(PrescriberBrowserManager.class);
	private final Prescriber prescriber;
	private final boolean insert;

	private JTextField nameField;
	private Prescriber savedPrescriber;
	private boolean saved;

	public PrescriberEdit(JFrame owner, Prescriber prescriber, boolean insert) {
		super(owner, true);
		this.prescriber = prescriber != null ? prescriber : new Prescriber();
		this.insert = insert;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage(insert ? "angal.prescriber.new.title" : "angal.prescriber.edit.title"));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout(10, 10));
		content.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.prescriber.name.label") + ":"));
		nameField = new JTextField(30);
		if (!insert) {
			nameField.setText(prescriber.getName());
		}
		dataPanel.add(nameField);
		content.add(dataPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		okButton.addActionListener(e -> save());
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		content.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(content);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private void save() {
		try {
			prescriber.setName(nameField.getText());
			savedPrescriber = manager.savePrescriber(prescriber);
			saved = true;
			dispose();
		} catch (OHServiceException e) {
			MessageDialog.showExceptions(e);
		}
	}

	public boolean isSaved() {
		return saved;
	}

	public Prescriber getSavedPrescriber() {
		return savedPrescriber;
	}
}
