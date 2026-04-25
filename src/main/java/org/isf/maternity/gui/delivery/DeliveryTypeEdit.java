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
package org.isf.maternity.gui.delivery;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyDeliveryTypeBrowserManager;
import org.isf.maternity.model.PregnancyDeliveryType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.EventListener;

public class DeliveryTypeEdit extends JDialog {

	private static final long serialVersionUID = 1L;

    private final EventListenerList deliveryTypeListeners = new EventListenerList();

    public interface DeliveryTypeListener extends EventListener {
        void deliveryTypeUpdated(AWTEvent e);
        void deliveryTypeInserted(AWTEvent e);
    }

    public void addDeliveryTypeListener(DeliveryTypeListener l) {
        deliveryTypeListeners.add(DeliveryTypeListener.class, l);
    }

    public void removeDeliveryTypeListener(DeliveryTypeListener listener) {
        deliveryTypeListeners.remove(DeliveryTypeListener.class, listener);
    }

	private void fireDeliveryTypeInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deliveryTypeListeners.getListeners(DeliveryTypeListener.class);
		for (EventListener listener : listeners) {
			((DeliveryTypeListener) listener).deliveryTypeInserted(event);
		}
	}

	private void fireDeliveryTypeUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deliveryTypeListeners.getListeners(DeliveryTypeListener.class);
		for (EventListener listener : listeners) {
			((DeliveryTypeListener) listener).deliveryTypeUpdated(event);
		}
	}

	private final PregnancyDeliveryTypeBrowserManager deliveryTypeBrowserManager = Context.getApplicationContext().getBean(PregnancyDeliveryTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private VoLimitedTextField codeTextField;
	private String lastdescription;
	private final PregnancyDeliveryType deliveryType;
	private final boolean insert;
	private JPanel jDataPanel;

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public DeliveryTypeEdit(JFrame owner, PregnancyDeliveryType old, boolean inserting) {
		super(owner, true);
		insert = inserting;
		deliveryType = old;   //deliveryType will be used for every operation
		lastdescription = deliveryType.getDescription();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.maternity.deliverytype.newdeliverytype.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.maternity.deliverytype.editdeliverytype.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.pack();
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), BorderLayout.NORTH);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes dataPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel();
			dataPanel.add(getJDataPanel(), null);
		}
		return dataPanel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				try {
					deliveryType.setDescription(descriptionTextField.getText());
					deliveryType.setCode(codeTextField.getText());

					if (insert) {     // inserting
						deliveryTypeBrowserManager.newDeliveryType(deliveryType);
						fireDeliveryTypeInserted();
						dispose();
					} else {            // updating
						if (descriptionTextField.getText().equals(lastdescription)) {
							dispose();
						} else {
							deliveryTypeBrowserManager.updateDeliveryType(deliveryType);
							fireDeliveryTypeUpdated();
							dispose();
						}
					}
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes descriptionTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			descriptionTextField = new JTextField(20);
			if (!insert) {
				descriptionTextField.setText(deliveryType.getDescription());
				lastdescription=deliveryType.getDescription();
			} 
		}
		return descriptionTextField;
	}
	
	/**
	 * This method initializes codeTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new VoLimitedTextField(20);
			if (!insert) {
				codeTextField.setText(deliveryType.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	/**
	 * This method initializes jDataPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel(new SpringLayout());
			jDataPanel.add(new JLabel(MessageBundle.formatMessage("angal.common.codemaxchars.fmt.txt", 20) + "* :"));
			jDataPanel.add(getCodeTextField());
			jDataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + "* :"));
			jDataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(jDataPanel, 2, 2, 5, 5, 5, 5);
		}
		return jDataPanel;
	}
}
