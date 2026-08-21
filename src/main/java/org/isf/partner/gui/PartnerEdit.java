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
package org.isf.partner.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.partner.manager.PartnerBrowserManager;
import org.isf.partner.model.Partner;
import org.isf.partnertype.manager.PartnerTypeBrowserManager;
import org.isf.partnertype.model.PartnerType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class PartnerEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final EventListenerList partnerListeners = new EventListenerList();

	public interface PartnerListener extends EventListener {
		void partnerInserted(AWTEvent e);
		void partnerUpdated(AWTEvent e);
	}

	public void addPartnerListener(PartnerListener l) {
		partnerListeners.add(PartnerListener.class, l);
	}

	private void firePartnerInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (EventListener listener : partnerListeners.getListeners(PartnerListener.class)) {
			((PartnerListener) listener).partnerInserted(event);
		}
	}

	private void firePartnerUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (EventListener listener : partnerListeners.getListeners(PartnerListener.class)) {
			((PartnerListener) listener).partnerUpdated(event);
		}
	}

	private PartnerBrowserManager partnerManager = Context.getApplicationContext().getBean(PartnerBrowserManager.class);
	private PartnerTypeBrowserManager partnerTypeManager = Context.getApplicationContext().getBean(PartnerTypeBrowserManager.class);

	private final boolean insert;
	private final Partner partner;

	private JPanel jPanelData;
	private JPanel jPanelButtons;
	private JTextField jTextFieldName;
	private JComboBox<PartnerType> jComboBoxType;
	private JTextField jTextFieldContactPerson;
	private JTextField jTextFieldPhone;
	private JTextField jTextFieldEmail;
	private JTextField jTextFieldAddress;
	private JTextArea jTextAreaNotes;
	private JButton jButtonOK;
	private JButton jButtonCancel;

	public PartnerEdit(JFrame parent, Partner partner, boolean inserting) {
		super(parent, true);
		this.insert = inserting;
		this.partner = partner;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(450, 420);
		setTitle(MessageBundle.getMessage(insert ? "angal.partner.newpartner.title" : "angal.partner.editpartner.title"));
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel(new SpringLayout());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.common.name.txt")));
			jPanelData.add(getJTextFieldName());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.partner.type.label")));
			jPanelData.add(getJComboBoxType());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.partner.contactperson.label")));
			jPanelData.add(getJTextFieldContactPerson());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.partner.phone.label")));
			jPanelData.add(getJTextFieldPhone());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.partner.email.label")));
			jPanelData.add(getJTextFieldEmail());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.partner.address.label")));
			jPanelData.add(getJTextFieldAddress());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.partner.notes.label")));
			jPanelData.add(new JScrollPane(getJTextAreaNotes()));
			SpringUtilities.makeCompactGrid(jPanelData, 7, 2, 5, 5, 5, 5);
		}
		return jPanelData;
	}

	private JTextField getJTextFieldName() {
		if (jTextFieldName == null) {
			jTextFieldName = new VoLimitedTextField(100, 20);
			jTextFieldName.setText(partner.getName());
		}
		return jTextFieldName;
	}

	private JComboBox<PartnerType> getJComboBoxType() {
		if (jComboBoxType == null) {
			jComboBoxType = new JComboBox<>();
			List<PartnerType> types = new ArrayList<>();
			try {
				types = partnerTypeManager.getPartnerTypes();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}
			for (PartnerType type : types) {
				jComboBoxType.addItem(type);
			}
			if (partner.getType() != null) {
				jComboBoxType.setSelectedItem(partner.getType());
			}
		}
		return jComboBoxType;
	}

	private JTextField getJTextFieldContactPerson() {
		if (jTextFieldContactPerson == null) {
			jTextFieldContactPerson = new VoLimitedTextField(100, 20);
			jTextFieldContactPerson.setText(partner.getContactPerson());
		}
		return jTextFieldContactPerson;
	}

	private JTextField getJTextFieldPhone() {
		if (jTextFieldPhone == null) {
			jTextFieldPhone = new VoLimitedTextField(50, 20);
			jTextFieldPhone.setText(partner.getPhone());
		}
		return jTextFieldPhone;
	}

	private JTextField getJTextFieldEmail() {
		if (jTextFieldEmail == null) {
			jTextFieldEmail = new VoLimitedTextField(100, 20);
			jTextFieldEmail.setText(partner.getEmail());
		}
		return jTextFieldEmail;
	}

	private JTextField getJTextFieldAddress() {
		if (jTextFieldAddress == null) {
			jTextFieldAddress = new VoLimitedTextField(255, 20);
			jTextFieldAddress.setText(partner.getAddress());
		}
		return jTextFieldAddress;
	}

	private JTextArea getJTextAreaNotes() {
		if (jTextAreaNotes == null) {
			jTextAreaNotes = new JTextArea(3, 20);
			jTextAreaNotes.setText(partner.getNotes());
		}
		return jTextAreaNotes;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(actionEvent -> {
				PartnerType selectedType = (PartnerType) jComboBoxType.getSelectedItem();
				if (selectedType == null) {
					MessageDialog.error(this, "angal.partner.validation.type.required.msg");
					return;
				}
				partner.setName(jTextFieldName.getText());
				partner.setType(selectedType);
				partner.setContactPerson(jTextFieldContactPerson.getText());
				partner.setPhone(jTextFieldPhone.getText());
				partner.setEmail(jTextFieldEmail.getText());
				partner.setAddress(jTextFieldAddress.getText());
				partner.setNotes(jTextAreaNotes.getText());
				try {
					partnerManager.savePartner(partner);
					if (insert) {
						firePartnerInserted();
					} else {
						firePartnerUpdated();
					}
					dispose();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
				}
			});
		}
		return jButtonOK;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}
}
