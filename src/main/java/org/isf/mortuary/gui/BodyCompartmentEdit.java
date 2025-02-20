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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.BodyCompartmentManager;
import org.isf.mortuary.model.BodyCompartment;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class BodyCompartmentEdit extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
	private final EventListenerList bodyCompartmentListeners = new EventListenerList();

	public interface BodyCompartmentListener extends EventListener {

		void bodyCompartmentUpdated(AWTEvent e);

		void bodyCompartmentInserted(AWTEvent e);
	}

	public void addBodyCompartmentListener(BodyCompartmentEdit.BodyCompartmentListener l) {
		bodyCompartmentListeners.add(BodyCompartmentEdit.BodyCompartmentListener.class, l);
	}

	private void fireBodyCompartmentsInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = bodyCompartmentListeners.getListeners(BodyCompartmentEdit.BodyCompartmentListener.class);
		for (EventListener listener : listeners) {
			((BodyCompartmentEdit.BodyCompartmentListener) listener).bodyCompartmentInserted(event);
		}
	}

	private void fireBodyCompartmentsUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = bodyCompartmentListeners.getListeners(BodyCompartmentEdit.BodyCompartmentListener.class);
		for (EventListener listener : listeners) {
			((BodyCompartmentEdit.BodyCompartmentListener) listener).bodyCompartmentUpdated(event);
		}
	}

	private JPanel jContentPane;
	private JPanel jDataPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JTextField labelTextField;
	private JTextField descriptionTextField;
	private String label;
	private final boolean insert;
	private BodyCompartment bodyCompartment;

	private final BodyCompartmentManager bodyCompartmentManager = Context.getApplicationContext().getBean(BodyCompartmentManager.class);

	public BodyCompartmentEdit(JDialog parent, BodyCompartment old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		bodyCompartment = old;
		initialize();
	}

	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.bodycompartment.newbodycompartment.title"));
			bodyCompartment = new BodyCompartment(0,"","",false);
		} else {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.bodycompartment.editbodycompartment.title"));
		}
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJDataPanel(), BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel();
			GridBagLayout gblDataPanel = new GridBagLayout();
			gblDataPanel.columnWeights = new double[] { 0.0, 1.0 };
			jDataPanel.setLayout(gblDataPanel);
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.bodycompartment.label.txt"));
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
			jDataPanel.add(getLabelTextField(), gbcCodeTextField);

			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.bodycompartment.descriptionedit.txt"));
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
			jDataPanel.add(getDescriptionTextField(), gbcDescriptionTextField);

		}
		return jDataPanel;
	}

	private JTextField getLabelTextField() {
		if (labelTextField == null) {
			labelTextField = new VoLimitedTextField(6, 20);
			if (!insert) {
				labelTextField.setText(bodyCompartment.getLabel());
				labelTextField.setEnabled(false);
			}
		}
		return labelTextField;
	}

	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			descriptionTextField = new JTextField();
			if (!insert) {
				descriptionTextField.setText(bodyCompartment.getDescription());
			}
		}
		return descriptionTextField;
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
			if (insert) {
				label = labelTextField.getText().trim();
				if (label.isEmpty()) {
					MessageDialog.error(this, "angal.mortuary.bodycompartment.pleaseinsertalabel.msg");
					return;
				}
				if (label.length() > 6) {
					MessageDialog.error(this, "angal.mortuary.bodycompartment.thelabelistoolongmax6char.msg");
					return;
				}
				try {
					if (bodyCompartmentManager.isLabelPresent(label)) {
						MessageDialog.error(this, "angal.mortuarystays.codealreadyinuse.msg");
						return;
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}

			bodyCompartment.setDescription(descriptionTextField.getText().trim());
			bodyCompartment.setLabel(labelTextField.getText().trim());

			boolean result = false;
			BodyCompartment savedBodyCompartment;
			if (insert) {
				try {
					savedBodyCompartment = bodyCompartmentManager.add(bodyCompartment);
					result = savedBodyCompartment != null;
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (result) {
					fireBodyCompartmentsInserted();
				}
			} else {
				try {
					savedBodyCompartment = bodyCompartmentManager.update(bodyCompartment);
					result = savedBodyCompartment != null;
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (result) {
					fireBodyCompartmentsUpdated();
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