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

package org.isf.mortuary.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.isf.generaldata.MessageBundle;
import org.isf.mortuary.model.DeathReason;

public class DeathReasonDetails extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane;
	private JPanel jDataPanel;
	private JTextArea descriptionTextArea;
	private JLabel titleLabel;
	private JButton closeButton;
	private final DeathReason deathReason;

	public DeathReasonDetails(JFrame parent, DeathReason old) {
		super(parent, true);
		deathReason = old;
		initialize();
	}

	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setTitle(MessageBundle.getMessage("angal.mortuary.causeofdeath.causeofdeathdetails.title"));
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContentPane() {
		if (jContentPane != null) {
			return jContentPane;
		}
		jContentPane = new JPanel();
		jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getJDataPanel(), BorderLayout.CENTER);
		jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		return jContentPane;
	}

	private JPanel getJDataPanel() {
		if (jDataPanel != null) {
			return jDataPanel;
		}
		jDataPanel = new JPanel();
		GridBagLayout gblDataPanel = new GridBagLayout();
		gblDataPanel.columnWeights = new double[] { 0.0, 1.0 };
		jDataPanel.setLayout(gblDataPanel);
		JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.causeofdeath.details.title.txt"));
		GridBagConstraints gbcCodeLabel = new GridBagConstraints();
		gbcCodeLabel.anchor = GridBagConstraints.WEST;
		gbcCodeLabel.insets = new Insets(0, 0, 5, 5);
		gbcCodeLabel.gridx = 0;
		gbcCodeLabel.gridy = 0;
		jDataPanel.add(codeLabel, gbcCodeLabel);
		GridBagConstraints gbcTitleLabel = new GridBagConstraints();
		gbcTitleLabel.fill = GridBagConstraints.HORIZONTAL;
		gbcTitleLabel.insets = new Insets(0, 0, 5, 0);
		gbcTitleLabel.gridx = 1;
		gbcTitleLabel.gridy = 0;
		jDataPanel.add(getTitleLabel(), gbcTitleLabel);

		JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.causeofdeath.description"));
		GridBagConstraints gbcDescLabel = new GridBagConstraints();
		gbcDescLabel.anchor = GridBagConstraints.WEST;
		gbcDescLabel.insets = new Insets(0, 0, 5, 5);
		gbcDescLabel.gridx = 0;
		gbcDescLabel.gridy = 1;
		jDataPanel.add(descLabel, gbcDescLabel);
		GridBagConstraints gbcDescriptionTextField = new GridBagConstraints();
		gbcDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcDescriptionTextField.insets = new Insets(0, 0, 5, 0);
		gbcDescriptionTextField.gridx = 1;
		gbcDescriptionTextField.gridy = 1;
		jDataPanel.add(getDescriptionScrollPanel(), gbcDescriptionTextField);

		return jDataPanel;
	}

	private JLabel getTitleLabel() {
		if (titleLabel != null) {
			return titleLabel;
		}
		titleLabel = new JLabel();
		titleLabel.setText(deathReason.getTitle());
		return titleLabel;
	}

	private JScrollPane getDescriptionScrollPanel() {
		JScrollPane jDescriptionScrollPane = new JScrollPane(getDescriptionTextArea());
		jDescriptionScrollPane.setVerticalScrollBar(new JScrollBar());
		jDescriptionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jDescriptionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jDescriptionScrollPane.validate();
		return jDescriptionScrollPane;
	}

	private JTextArea getDescriptionTextArea() {
		if (descriptionTextArea != null) {
			return descriptionTextArea;
		}
		descriptionTextArea = new JTextArea(6,20);
		descriptionTextArea.setAutoscrolls(true);
		descriptionTextArea.setText(deathReason.getDescription());
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setOpaque(false);
		return descriptionTextArea;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getCloseButton());
		return buttonPanel;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(actionEvent -> dispose());
		}
		return closeButton;
	}
}