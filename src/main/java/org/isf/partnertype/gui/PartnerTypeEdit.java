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
package org.isf.partnertype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.partnertype.manager.PartnerTypeBrowserManager;
import org.isf.partnertype.model.PartnerType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class PartnerTypeEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final EventListenerList partnerTypeListeners = new EventListenerList();

	public interface PartnerTypeListener extends EventListener {
		void partnerTypeInserted(AWTEvent e);
		void partnerTypeUpdated(AWTEvent e);
	}

	public void addPartnerTypeListener(PartnerTypeListener l) {
		partnerTypeListeners.add(PartnerTypeListener.class, l);
	}

	private void firePartnerTypeInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (EventListener listener : partnerTypeListeners.getListeners(PartnerTypeListener.class)) {
			((PartnerTypeListener) listener).partnerTypeInserted(event);
		}
	}

	private void firePartnerTypeUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (EventListener listener : partnerTypeListeners.getListeners(PartnerTypeListener.class)) {
			((PartnerTypeListener) listener).partnerTypeUpdated(event);
		}
	}

	private PartnerTypeBrowserManager partnerTypeManager = Context.getApplicationContext().getBean(PartnerTypeBrowserManager.class);

	private final boolean insert;
	private final PartnerType partnerType;

	private JPanel jPanelData;
	private JPanel jPanelButtons;
	private JTextField jTextFieldCode;
	private JTextField jTextFieldDescription;
	private JButton jButtonOK;
	private JButton jButtonCancel;

	public PartnerTypeEdit(JFrame parent, PartnerType partnerType, boolean inserting) {
		super(parent, true);
		this.insert = inserting;
		this.partnerType = partnerType;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(400, 180);
		setTitle(MessageBundle.getMessage(insert ? "angal.partnertype.newpartnertype.title" : "angal.partnertype.editpartnertype.title"));
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel(new SpringLayout());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt")));
			jPanelData.add(getJTextFieldCode());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt")));
			jPanelData.add(getJTextFieldDescription());
			SpringUtilities.makeCompactGrid(jPanelData, 2, 2, 5, 5, 5, 5);
		}
		return jPanelData;
	}

	private JTextField getJTextFieldCode() {
		if (jTextFieldCode == null) {
			jTextFieldCode = new VoLimitedTextField(20, 20);
			jTextFieldCode.setText(partnerType.getCode());
			jTextFieldCode.setEditable(insert);
		}
		return jTextFieldCode;
	}

	private JTextField getJTextFieldDescription() {
		if (jTextFieldDescription == null) {
			jTextFieldDescription = new VoLimitedTextField(255, 20);
			jTextFieldDescription.setText(partnerType.getDescription());
		}
		return jTextFieldDescription;
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
				partnerType.setCode(jTextFieldCode.getText());
				partnerType.setDescription(jTextFieldDescription.getText());
				try {
					if (insert) {
						partnerTypeManager.newPartnerType(partnerType);
						firePartnerTypeInserted();
					} else {
						partnerTypeManager.updatePartnerType(partnerType);
						firePartnerTypeUpdated();
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
