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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.DeathReasonManager;
import org.isf.mortuary.model.DeathReason;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

public class DeathReasonEdit extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
	private final EventListenerList deathReasonListeners = new EventListenerList();

	public interface DeathReasonListener extends EventListener {

		void deathReasonUpdated(AWTEvent e);

		void deathReasonInserted(AWTEvent e);
	}

	public void addDeathReasonListener(DeathReasonEdit.DeathReasonListener l) {
		deathReasonListeners.add(DeathReasonEdit.DeathReasonListener.class, l);
	}

	private void fireDeathReasonsInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deathReasonListeners.getListeners(DeathReasonEdit.DeathReasonListener.class);
		for (EventListener listener : listeners) {
			((DeathReasonEdit.DeathReasonListener) listener).deathReasonInserted(event);
		}
	}

	private void fireDeathReasonsUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deathReasonListeners.getListeners(DeathReasonEdit.DeathReasonListener.class);
		for (EventListener listener : listeners) {
			((DeathReasonEdit.DeathReasonListener) listener).deathReasonUpdated(event);
		}
	}

	private JPanel jContentPane;
	private JPanel jDataPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JTextField titleTextField;
	private JTextArea descriptionTextArea;
	private final boolean insert;
	private DeathReason deathReason;

	private final DeathReasonManager deathReasonManager = Context.getApplicationContext().getBean(DeathReasonManager.class);

	public DeathReasonEdit(JFrame parent, DeathReason old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		deathReason = old;
		initialize();
	}

	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.deathreason.newdeathreason.title"));
			deathReason = new DeathReason(0,"","",false);
		} else {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.deathreason.editdeathreason.title"));
		}
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
		JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.deathreason.code.txt"));
		GridBagConstraints gbcCodeLabel = new GridBagConstraints();
		gbcCodeLabel.anchor = GridBagConstraints.WEST;
		gbcCodeLabel.insets = new Insets(0, 0, 5, 5);
		gbcCodeLabel.gridx = 0;
		gbcCodeLabel.gridy = 0;
		jDataPanel.add(codeLabel, gbcCodeLabel);
		GridBagConstraints gbcCodeTextField = new GridBagConstraints();
		gbcCodeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcCodeTextField.insets = new Insets(0, 0, 5, 0);
		gbcCodeTextField.gridx = 1;
		gbcCodeTextField.gridy = 0;
		jDataPanel.add(getTitleTextField(), gbcCodeTextField);

		JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.deathreason.description"));
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

	private JTextField getTitleTextField() {
		if (titleTextField != null) {
			return titleTextField;
		}
		titleTextField = new JTextField();
		if (!insert) {
			titleTextField.setText(deathReason.getTitle());
		}
		return titleTextField;
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
		if (!insert) {
			descriptionTextArea.setText(deathReason.getDescription());
		}
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		return descriptionTextArea;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getSaveButton());
		buttonPanel.add(getCancelButton());
		return buttonPanel;
	}

	private JButton getSaveButton() {
		if (saveButton != null) {
			return saveButton;
		}
		saveButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		saveButton.addActionListener(actionEvent -> {
			if (insert && titleTextField.getText().trim().isEmpty()) {
				MessageDialog.error(this, "angal.mortuary.deathreason.pleaseinsertatitle.msg");
				return;
			}

			deathReason.setDescription(descriptionTextArea.getText().trim());
			deathReason.setTitle(titleTextField.getText().trim());

			boolean result = false;
			DeathReason savedDeathReason;
			if (insert) {
				try {
					savedDeathReason = deathReasonManager.add(deathReason);
					result = savedDeathReason != null;
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (result) {
					fireDeathReasonsInserted();
				}
			} else {
				try {
					savedDeathReason = deathReasonManager.update(deathReason);
					result = savedDeathReason != null;
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (result) {
					fireDeathReasonsUpdated();
				}
			}
			if (!result) {
				MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
			} else {
				dispose();
			}
		});
		return saveButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}
}
