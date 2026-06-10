/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.command.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.CommandRow;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class CommandRowEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private EventListenerList commandRowListeners = new EventListenerList();

	public interface CommandRowListener extends EventListener {
		void commandRowUpdated(AWTEvent e);
		void commandRowInserted(AWTEvent e);
	}

	public void addCommandRowListener(CommandRowListener l) {
		commandRowListeners.add(CommandRowListener.class, l);
	}

	public void removeCommandRowListener(CommandRowListener listener) {
		commandRowListeners.remove(CommandRowListener.class, listener);
	}

	private void fireCommandRowInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = commandRowListeners.getListeners(CommandRowListener.class);
		for (EventListener listener : listeners) {
			((CommandRowListener) listener).commandRowInserted(event);
		}
	}

	private void fireCommandRowUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = commandRowListeners.getListeners(CommandRowListener.class);
		for (EventListener listener : listeners) {
			((CommandRowListener) listener).commandRowUpdated(event);
		}
	}

	private final CommandBrowserManager commandBrowserManager = Context.getApplicationContext().getBean(CommandBrowserManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JComboBox<Medical> medicalComboBox;
	private JTextField qtyInStoreTextField;
	private JTextField criticalLevelTextField;
	private JTextField orderQtyTextField;
	private JTextField stillQtyTextField;
	private JTextField userAddedQtyTextField;
	private CommandRow commandRow;
	private final boolean insert;

	public CommandRowEdit(JFrame parent, CommandRow old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		commandRow = old;
		initialize();
	}

	private void initialize() {
		setContentPane(getJContentPane());
		if (insert) {
			setTitle(MessageBundle.getMessage("angal.command.row.newrow.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.command.row.editrow.title"));
		}
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel();
			GridBagLayout gblDataPanel = new GridBagLayout();
			gblDataPanel.columnWeights = new double[] { 0.0, 1.0 };
			dataPanel.setLayout(gblDataPanel);

			JLabel medicalLabel = new JLabel(MessageBundle.getMessage("angal.common.medical.txt"));
			GridBagConstraints gbcMedicalLabel = new GridBagConstraints();
			gbcMedicalLabel.anchor = GridBagConstraints.WEST;
			gbcMedicalLabel.insets = new Insets(0, 0, 5, 5);
			gbcMedicalLabel.gridx = 0;
			gbcMedicalLabel.gridy = 0;
			dataPanel.add(medicalLabel, gbcMedicalLabel);

			GridBagConstraints gbcMedicalCombo = new GridBagConstraints();
			gbcMedicalCombo.fill = GridBagConstraints.HORIZONTAL;
			gbcMedicalCombo.insets = new Insets(0, 0, 5, 0);
			gbcMedicalCombo.gridx = 1;
			gbcMedicalCombo.gridy = 0;
			dataPanel.add(getMedicalComboBox(), gbcMedicalCombo);

			JLabel qtyInStoreLabel = new JLabel(MessageBundle.getMessage("angal.command.row.qtyinstore.col"));
			GridBagConstraints gbcQtyInStoreLabel = new GridBagConstraints();
			gbcQtyInStoreLabel.anchor = GridBagConstraints.WEST;
			gbcQtyInStoreLabel.insets = new Insets(0, 0, 5, 5);
			gbcQtyInStoreLabel.gridx = 0;
			gbcQtyInStoreLabel.gridy = 1;
			dataPanel.add(qtyInStoreLabel, gbcQtyInStoreLabel);

			GridBagConstraints gbcQtyInStoreField = new GridBagConstraints();
			gbcQtyInStoreField.fill = GridBagConstraints.HORIZONTAL;
			gbcQtyInStoreField.insets = new Insets(0, 0, 5, 0);
			gbcQtyInStoreField.gridx = 1;
			gbcQtyInStoreField.gridy = 1;
			dataPanel.add(getQtyInStoreTextField(), gbcQtyInStoreField);

			JLabel criticalLevelLabel = new JLabel(MessageBundle.getMessage("angal.command.row.criticallevel.col"));
			GridBagConstraints gbcCriticalLevelLabel = new GridBagConstraints();
			gbcCriticalLevelLabel.anchor = GridBagConstraints.WEST;
			gbcCriticalLevelLabel.insets = new Insets(0, 0, 5, 5);
			gbcCriticalLevelLabel.gridx = 0;
			gbcCriticalLevelLabel.gridy = 2;
			dataPanel.add(criticalLevelLabel, gbcCriticalLevelLabel);

			GridBagConstraints gbcCriticalLevelField = new GridBagConstraints();
			gbcCriticalLevelField.fill = GridBagConstraints.HORIZONTAL;
			gbcCriticalLevelField.insets = new Insets(0, 0, 5, 0);
			gbcCriticalLevelField.gridx = 1;
			gbcCriticalLevelField.gridy = 2;
			dataPanel.add(getCriticalLevelTextField(), gbcCriticalLevelField);

			JLabel orderQtyLabel = new JLabel(MessageBundle.getMessage("angal.command.row.orderqty.col"));
			GridBagConstraints gbcOrderQtyLabel = new GridBagConstraints();
			gbcOrderQtyLabel.anchor = GridBagConstraints.WEST;
			gbcOrderQtyLabel.insets = new Insets(0, 0, 5, 5);
			gbcOrderQtyLabel.gridx = 0;
			gbcOrderQtyLabel.gridy = 3;
			dataPanel.add(orderQtyLabel, gbcOrderQtyLabel);

			GridBagConstraints gbcOrderQtyField = new GridBagConstraints();
			gbcOrderQtyField.fill = GridBagConstraints.HORIZONTAL;
			gbcOrderQtyField.insets = new Insets(0, 0, 5, 0);
			gbcOrderQtyField.gridx = 1;
			gbcOrderQtyField.gridy = 3;
			dataPanel.add(getOrderQtyTextField(), gbcOrderQtyField);

			JLabel stillQtyLabel = new JLabel(MessageBundle.getMessage("angal.command.row.stillqty.col"));
			GridBagConstraints gbcStillQtyLabel = new GridBagConstraints();
			gbcStillQtyLabel.anchor = GridBagConstraints.WEST;
			gbcStillQtyLabel.insets = new Insets(0, 0, 5, 5);
			gbcStillQtyLabel.gridx = 0;
			gbcStillQtyLabel.gridy = 4;
			dataPanel.add(stillQtyLabel, gbcStillQtyLabel);

			GridBagConstraints gbcStillQtyField = new GridBagConstraints();
			gbcStillQtyField.fill = GridBagConstraints.HORIZONTAL;
			gbcStillQtyField.insets = new Insets(0, 0, 5, 0);
			gbcStillQtyField.gridx = 1;
			gbcStillQtyField.gridy = 4;
			dataPanel.add(getStillQtyTextField(), gbcStillQtyField);

			JLabel userAddedQtyLabel = new JLabel(MessageBundle.getMessage("angal.command.row.useraddedqty.col"));
			GridBagConstraints gbcUserAddedQtyLabel = new GridBagConstraints();
			gbcUserAddedQtyLabel.anchor = GridBagConstraints.WEST;
			gbcUserAddedQtyLabel.insets = new Insets(0, 0, 5, 5);
			gbcUserAddedQtyLabel.gridx = 0;
			gbcUserAddedQtyLabel.gridy = 5;
			dataPanel.add(userAddedQtyLabel, gbcUserAddedQtyLabel);

			GridBagConstraints gbcUserAddedQtyField = new GridBagConstraints();
			gbcUserAddedQtyField.fill = GridBagConstraints.HORIZONTAL;
			gbcUserAddedQtyField.insets = new Insets(0, 0, 5, 0);
			gbcUserAddedQtyField.gridx = 1;
			gbcUserAddedQtyField.gridy = 5;
			dataPanel.add(getUserAddedQtyTextField(), gbcUserAddedQtyField);
		}
		return dataPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton());
			buttonPanel.add(getCancelButton());
		}
		return buttonPanel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				Medical selectedMedical = (Medical) medicalComboBox.getSelectedItem();
				if (selectedMedical == null || selectedMedical.getCode() == null) {
					MessageDialog.error(this, "angal.command.row.pleaseinsertamedical.msg");
					return;
				}

				double qtyInStore;
				try {
					qtyInStore = Double.parseDouble(qtyInStoreTextField.getText().trim());
				} catch (NumberFormatException e) {
					MessageDialog.error(this, "angal.command.row.pleaseinsertavalidqty.msg");
					return;
				}

				double criticalLevel;
				try {
					criticalLevel = Double.parseDouble(criticalLevelTextField.getText().trim());
				} catch (NumberFormatException e) {
					MessageDialog.error(this, "angal.command.row.pleaseinsertavalidqty.msg");
					return;
				}

				Double orderQty = null;
				if (!orderQtyTextField.getText().trim().isEmpty()) {
					try {
						orderQty = Double.parseDouble(orderQtyTextField.getText().trim());
					} catch (NumberFormatException e) {
						MessageDialog.error(this, "angal.command.row.pleaseinsertavalidqty.msg");
						return;
					}
				}

				Double stillQty = null;
				if (!stillQtyTextField.getText().trim().isEmpty()) {
					try {
						stillQty = Double.parseDouble(stillQtyTextField.getText().trim());
					} catch (NumberFormatException e) {
						MessageDialog.error(this, "angal.command.row.pleaseinsertavalidqty.msg");
						return;
					}
				}

				double userAddedQty;
				try {
					userAddedQty = Double.parseDouble(userAddedQtyTextField.getText().trim());
				} catch (NumberFormatException e) {
					MessageDialog.error(this, "angal.command.row.pleaseinsertavalidqty.msg");
					return;
				}

				commandRow.setMedical(selectedMedical);
				commandRow.setMedicalCode(selectedMedical.getProdCode());
				commandRow.setMedicalDescription(selectedMedical.getDescription());
				commandRow.setQtyInStore(qtyInStore);
				commandRow.setCriticalLevel(criticalLevel);
				commandRow.setOrderQty(orderQty);
				commandRow.setStillQty(stillQty);
				commandRow.setUserAddedQty(userAddedQty);

				boolean result = false;
				try {
					CommandRow saved = commandBrowserManager.saveOrUpdateRow(commandRow);
					if (saved != null) {
						commandRow.setId(saved.getId());
						result = true;
					}
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (!result) {
					MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
				} else {
					if (insert) {
						fireCommandRowInserted();
					} else {
						fireCommandRowUpdated();
					}
					dispose();
				}
			});
		}
		return okButton;
	}

	private JComboBox<Medical> getMedicalComboBox() {
		if (medicalComboBox == null) {
			medicalComboBox = new JComboBox<>();
			try {
				List<Medical> medicals = medicalBrowsingManager.getMedicals();
				for (Medical med : medicals) {
					medicalComboBox.addItem(med);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			if (!insert && commandRow.getMedical() != null) {
				medicalComboBox.setSelectedItem(commandRow.getMedical());
			}
		}
		return medicalComboBox;
	}

	private JTextField getQtyInStoreTextField() {
		if (qtyInStoreTextField == null) {
			qtyInStoreTextField = new VoLimitedTextField(10);
			if (!insert) {
				qtyInStoreTextField.setText(String.valueOf(commandRow.getQtyInStore()));
			} else {
				qtyInStoreTextField.setText("0");
			}
		}
		return qtyInStoreTextField;
	}

	private JTextField getCriticalLevelTextField() {
		if (criticalLevelTextField == null) {
			criticalLevelTextField = new VoLimitedTextField(10);
			if (!insert) {
				criticalLevelTextField.setText(String.valueOf(commandRow.getCriticalLevel()));
			} else {
				criticalLevelTextField.setText("0");
			}
		}
		return criticalLevelTextField;
	}

	private JTextField getOrderQtyTextField() {
		if (orderQtyTextField == null) {
			orderQtyTextField = new VoLimitedTextField(10);
			if (!insert && commandRow.getOrderQty() != null) {
				orderQtyTextField.setText(String.valueOf(commandRow.getOrderQty()));
			}
		}
		return orderQtyTextField;
	}

	private JTextField getStillQtyTextField() {
		if (stillQtyTextField == null) {
			stillQtyTextField = new VoLimitedTextField(10);
			if (!insert && commandRow.getStillQty() != null) {
				stillQtyTextField.setText(String.valueOf(commandRow.getStillQty()));
			}
		}
		return stillQtyTextField;
	}

	private JTextField getUserAddedQtyTextField() {
		if (userAddedQtyTextField == null) {
			userAddedQtyTextField = new VoLimitedTextField(10);
			if (!insert) {
				userAddedQtyTextField.setText(String.valueOf(commandRow.getUserAddedQty()));
			} else {
				userAddedQtyTextField.setText("0");
			}
		}
		return userAddedQtyTextField;
	}
}
