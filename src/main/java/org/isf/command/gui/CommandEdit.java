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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class CommandEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private EventListenerList commandListeners = new EventListenerList();

	public interface CommandListener extends EventListener {
		void commandUpdated(AWTEvent e);
		void commandInserted(AWTEvent e);
	}

	public void addCommandListener(CommandListener l) {
		commandListeners.add(CommandListener.class, l);
	}

	public void removeCommandListener(CommandListener listener) {
		commandListeners.remove(CommandListener.class, listener);
	}

	private void fireCommandInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = commandListeners.getListeners(CommandListener.class);
		for (EventListener listener : listeners) {
			((CommandListener) listener).commandInserted(event);
		}
	}

	private void fireCommandUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = commandListeners.getListeners(CommandListener.class);
		for (EventListener listener : listeners) {
			((CommandListener) listener).commandUpdated(event);
		}
	}

	private final CommandBrowserManager commandBrowserManager = Context.getApplicationContext().getBean(CommandBrowserManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField refNoTextField;
	private GoodDateChooser dateChooser;
	private Command command;
	private final boolean insert;

	public CommandEdit(JFrame parent, Command old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		command = old;
		initialize();
	}

	private void initialize() {
		setContentPane(getJContentPane());
		if (insert) {
			setTitle(MessageBundle.getMessage("angal.command.newcommand.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.command.editcommand.title"));
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

			JLabel refNoLabel = new JLabel(MessageBundle.getMessage("angal.command.refno.col"));
			GridBagConstraints gbcRefNoLabel = new GridBagConstraints();
			gbcRefNoLabel.anchor = GridBagConstraints.WEST;
			gbcRefNoLabel.insets = new Insets(0, 0, 5, 5);
			gbcRefNoLabel.gridx = 0;
			gbcRefNoLabel.gridy = 0;
			dataPanel.add(refNoLabel, gbcRefNoLabel);

			GridBagConstraints gbcRefNoTextField = new GridBagConstraints();
			gbcRefNoTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcRefNoTextField.insets = new Insets(0, 0, 5, 0);
			gbcRefNoTextField.gridx = 1;
			gbcRefNoTextField.gridy = 0;
			dataPanel.add(getRefNoTextField(), gbcRefNoTextField);

			JLabel dateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			GridBagConstraints gbcDateLabel = new GridBagConstraints();
			gbcDateLabel.anchor = GridBagConstraints.WEST;
			gbcDateLabel.insets = new Insets(0, 0, 5, 5);
			gbcDateLabel.gridx = 0;
			gbcDateLabel.gridy = 1;
			dataPanel.add(dateLabel, gbcDateLabel);

			GridBagConstraints gbcDateChooser = new GridBagConstraints();
			gbcDateChooser.fill = GridBagConstraints.HORIZONTAL;
			gbcDateChooser.insets = new Insets(0, 0, 5, 0);
			gbcDateChooser.gridx = 1;
			gbcDateChooser.gridy = 1;
			dataPanel.add(getDateChooser(), gbcDateChooser);
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
				String refNo = refNoTextField.getText().trim();
				if (refNo.isEmpty()) {
					MessageDialog.error(this, "angal.command.pleaseinsertarefno.msg");
					return;
				}
				LocalDate selectedDate = dateChooser.getDate();
				if (selectedDate == null) {
					MessageDialog.error(this, "angal.command.pleaseinsertadate.msg");
					return;
				}
				command.setRefNo(refNo);
				command.setDate(selectedDate.atStartOfDay());

				boolean result = false;
				try {
					Command saved = commandBrowserManager.saveOrUpdate(command);
					if (saved != null) {
						command.setId(saved.getId());
						command.setLock(saved.getLock());
						result = true;
					}
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (!result) {
					MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
				} else {
					if (insert) {
						fireCommandInserted();
					} else {
						fireCommandUpdated();
					}
					dispose();
				}
			});
		}
		return okButton;
	}

	private JTextField getRefNoTextField() {
		if (refNoTextField == null) {
			refNoTextField = new VoLimitedTextField(50);
			if (!insert) {
				refNoTextField.setText(command.getRefNo());
			}
		}
		return refNoTextField;
	}

	private GoodDateChooser getDateChooser() {
		if (dateChooser == null) {
			LocalDate date = null;
			if (!insert && command.getDate() != null) {
				date = command.getDate().toLocalDate();
			}
			dateChooser = new GoodDateChooser(date, true, false);
		}
		return dateChooser;
	}
}
