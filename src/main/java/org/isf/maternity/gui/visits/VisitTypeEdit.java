/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.maternity.gui.visits;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyVisitTypeBrowserManager;
import org.isf.maternity.model.PregnancyVisitType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.EventListener;

public class VisitTypeEdit extends JDialog {

	private static final long serialVersionUID = 1L;

    private EventListenerList visitTypeListeners = new EventListenerList();

    public interface VisitTypeListener extends EventListener {
        void visitTypeUpdated(AWTEvent e);
        void visitTypeInserted(AWTEvent e);
    }

    public void addVisitTypeListener(VisitTypeListener l) {
        visitTypeListeners.add(VisitTypeListener.class, l);
    }

    public void removeVisitTypeListener(VisitTypeListener listener) {
        visitTypeListeners.remove(VisitTypeListener.class, listener);
    }

	private void fireVisitTypeInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = visitTypeListeners.getListeners(VisitTypeListener.class);
		for (EventListener listener : listeners) {
			((VisitTypeListener) listener).visitTypeInserted(event);
		}
	}

	private void fireVisitTypeUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = visitTypeListeners.getListeners(VisitTypeListener.class);
		for (EventListener listener : listeners) {
			((VisitTypeListener) listener).visitTypeUpdated(event);
		}
	}

	private final PregnancyVisitTypeBrowserManager visitTypeBrowserManager = Context.getApplicationContext().getBean(PregnancyVisitTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private VoLimitedTextField codeTextField;
	private String lastdescription;
	private PregnancyVisitType visitType;
	private boolean insert;
	private JPanel jDataPanel;

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public VisitTypeEdit(JFrame owner, PregnancyVisitType old, boolean inserting) {
		super(owner, true);
		insert = inserting;
		visitType = old;   //visitType will be used for every operation
		lastdescription = visitType.getDescription();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.maternity.visittype.newvisittype.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.maternity.visittype.editvisittype.title"));
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
					visitType.setDescription(descriptionTextField.getText());
					visitType.setCode(codeTextField.getText());

					if (insert) {     // inserting
						visitTypeBrowserManager.newVisitType(visitType);
						fireVisitTypeInserted();
						dispose();
					} else {            // updating
						if (descriptionTextField.getText().equals(lastdescription)) {
							dispose();
						} else {
							visitTypeBrowserManager.updateVisitType(visitType);
							fireVisitTypeUpdated();
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
				descriptionTextField.setText(visitType.getDescription());
				lastdescription=visitType.getDescription();
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
				codeTextField.setText(visitType.getCode());
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
			jDataPanel.add(new JLabel(MessageBundle.formatMessage("angal.common.codemaxchars.fmt.txt", 20) +"* :"));
			jDataPanel.add(getCodeTextField());
			jDataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + "* :"));
			jDataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(jDataPanel, 2, 2, 5, 5, 5, 5);
		}
		return jDataPanel;
	}
}
