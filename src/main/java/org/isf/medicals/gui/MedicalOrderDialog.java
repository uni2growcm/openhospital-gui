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
package org.isf.medicals.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.isf.command.manager.CommandBrowserManager;
import org.isf.command.model.Command;
import org.isf.command.model.CommandRow;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class MedicalOrderDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private enum OrderAction {
		NEW_COMMAND,
		ADD_TO_EXISTING,
		REPLACE_EXPIRED
	}

	private final List<OrderAction> availableActions = new ArrayList<>();
	private Command pendingCommand = null;
	private boolean isAlreadyOrdered = false;

	private final CommandBrowserManager commandBrowserManager =
			Context.getApplicationContext().getBean(CommandBrowserManager.class);

	private final Medical medical;
	private final double currentStock;
	private final double expiredStock;
	private final double validStock;

	private JComboBox<String> actionComboBox;
	private JComboBox<Command> targetCommandComboBox;
	private JTextField refNoTextField;
	private GoodDateChooser dateChooser;
	private JSpinner qtySpinner;
	private JLabel targetCommandLabel;
	private JLabel refNoLabel;
	private JLabel dateLabel;
	private JButton confirmButton;

	private List<Command> openCommands = new ArrayList<>();
	private boolean commandProcessed = false;

	public MedicalOrderDialog(Window owner, Medical medical, double currentStock, double expiredStock, double validStock) {
		super(owner, ModalityType.APPLICATION_MODAL);
		this.medical = medical;
		this.currentStock = currentStock;
		this.expiredStock = expiredStock;
		this.validStock = validStock;

		loadOpenCommands();
		checkExistingPendingCommand();
		initComponents();
		pack();
		int minW = 660;
		int minH = isAlreadyOrdered ? 460 : 400;
		setMinimumSize(new Dimension(minW, minH));
		setPreferredSize(new Dimension(minW, minH));
		setLocationRelativeTo(owner);
	}

	public boolean isCommandProcessed() {
		return commandProcessed;
	}

	private void checkExistingPendingCommand() {
		try {
			List<Integer> usedMedicalIds = commandBrowserManager.getMedicalIdsAlreadyInCommandRows();
			if (usedMedicalIds != null && usedMedicalIds.contains(medical.getCode())) {
				isAlreadyOrdered = true;
				if (openCommands != null) {
					for (Command cmd : openCommands) {
						List<CommandRow> rows = commandBrowserManager.getActiveRowsByCommand(cmd);
						if (rows != null) {
							for (CommandRow row : rows) {
								if (row.getMedical() != null && row.getMedical().getCode().equals(medical.getCode())) {
									pendingCommand = cmd;
									break;
								}
							}
						}
						if (pendingCommand != null) {
							break;
						}
					}
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void loadOpenCommands() {
		try {
			List<Command> allCommands = commandBrowserManager.getAllOrderByDateDesc();
			if (allCommands != null) {
				for (Command cmd : allCommands) {
					if (cmd.getActive() != 0) {
						openCommands.add(cmd);
					}
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void initComponents() {
		setTitle(MessageBundle.formatMessage("angal.command.medicalorder.title", medical.getDescription()));
		setLayout(new BorderLayout(10, 10));
		JPanel contentPane = new JPanel(new BorderLayout(10, 12));
		contentPane.setBorder(new EmptyBorder(12, 16, 12, 16));
		contentPane.setBackground(new Color(248, 250, 252));

		contentPane.add(createTopInfoPanel(), BorderLayout.NORTH);

		contentPane.add(createFormPanel(), BorderLayout.CENTER);

		contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

		setContentPane(contentPane);
		updateFormVisibility();
	}

	private JPanel createTopInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 8));
		panel.setOpaque(false);

		JPanel titleBox = new JPanel(new BorderLayout(5, 4));
		titleBox.setOpaque(false);

		JLabel title = new JLabel(medical.getDescription() + " (" + medical.getProdCode() + ")");
		title.setFont(new Font("SansSerif", Font.BOLD, 15));
		title.setForeground(new Color(25, 45, 75));

		StringBuilder stats = new StringBuilder();
		stats.append(MessageBundle.getMessage("angal.medicals.details.badges.valid")).append(": ").append((int) validStock).append(" | ");
		stats.append(MessageBundle.getMessage("angal.medicals.details.badges.expired")).append(": ").append((int) expiredStock).append(" | ");
		stats.append(MessageBundle.getMessage("angal.medicals.criticallevel.txt")).append(": ").append((int) medical.getMinqty());

		JLabel subtitle = new JLabel(stats.toString());
		subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
		subtitle.setForeground(new Color(100, 115, 130));

		titleBox.add(title, BorderLayout.NORTH);
		titleBox.add(subtitle, BorderLayout.SOUTH);
		panel.add(titleBox, BorderLayout.NORTH);

		if (isAlreadyOrdered) {
			JPanel alertBanner = new JPanel(new BorderLayout(8, 0));
			alertBanner.setBackground(new Color(254, 243, 199));
			alertBanner.setBorder(new CompoundBorder(
					new LineBorder(new Color(217, 119, 6), 1, true),
					new EmptyBorder(8, 12, 8, 12)
			));

			JLabel alertIcon = new JLabel("⚠️");
			alertIcon.setFont(new Font("SansSerif", Font.PLAIN, 18));

			String warningDetail;
			if (pendingCommand != null) {
				String dateStr = pendingCommand.getDate() != null ? pendingCommand.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
				warningDetail = MessageBundle.formatMessage("angal.command.medicalorder.alert.alreadyordered.withcmd",
						pendingCommand.getRefNo(), dateStr);
			} else {
				warningDetail = MessageBundle.getMessage("angal.command.medicalorder.alert.alreadyordered.generic");
			}

			JTextArea alertMsg = new JTextArea(MessageBundle.getMessage("angal.messagedialog.warning.title") + " : " + warningDetail, 2, 45);
			alertMsg.setWrapStyleWord(true);
			alertMsg.setLineWrap(true);
			alertMsg.setEditable(false);
			alertMsg.setFocusable(false);
			alertMsg.setOpaque(false);
			alertMsg.setFont(new Font("SansSerif", Font.PLAIN, 12));
			alertMsg.setForeground(new Color(146, 64, 14));

			alertBanner.setPreferredSize(new Dimension(620, 60));
			alertBanner.add(alertIcon, BorderLayout.WEST);
			alertBanner.add(alertMsg, BorderLayout.CENTER);
			panel.add(alertBanner, BorderLayout.SOUTH);
		}

		return panel;
	}

	private JPanel createFormPanel() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Color.WHITE);
		form.setBorder(new CompoundBorder(
				new LineBorder(new Color(220, 228, 238), 1, true),
				new EmptyBorder(14, 16, 14, 16)
		));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		JLabel actionLabel = new JLabel(MessageBundle.getMessage("angal.command.medicalorder.action"));
		actionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		form.add(actionLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		availableActions.clear();
		List<String> actions = new ArrayList<>();

		if (!isAlreadyOrdered) {
			availableActions.add(OrderAction.NEW_COMMAND);
			actions.add(MessageBundle.getMessage("angal.command.medicalorder.action.newcommand"));
		}

		if (!openCommands.isEmpty()) {
			availableActions.add(OrderAction.ADD_TO_EXISTING);
			actions.add(MessageBundle.getMessage("angal.command.medicalorder.action.addtoexisting"));
		}

		if (expiredStock > 0 && (!isAlreadyOrdered || !openCommands.isEmpty())) {
			availableActions.add(OrderAction.REPLACE_EXPIRED);
			actions.add(MessageBundle.formatMessage("angal.command.medicalorder.action.replaceexpired", (int) expiredStock));
		}

		if (actions.isEmpty()) {
			actions.add(MessageBundle.getMessage("angal.command.medicalorder.action.alreadyordered.noneopen"));
		}

		actionComboBox = new JComboBox<>(actions.toArray(new String[0]));
		actionComboBox.setPreferredSize(new Dimension(340, 28));
		actionComboBox.addActionListener(e -> onActionChanged());
		form.add(actionComboBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		targetCommandLabel = new JLabel(MessageBundle.getMessage("angal.command.medicalorder.targetcommand"));
		targetCommandLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		form.add(targetCommandLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		targetCommandComboBox = new JComboBox<>(openCommands.toArray(new Command[0]));
		targetCommandComboBox.setPreferredSize(new Dimension(340, 28));
		final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		targetCommandComboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof Command) {
					Command cmd = (Command) value;
					String dateStr = cmd.getDate() != null ? cmd.getDate().format(df) : "";
					setText(cmd.getRefNo() + " (" + dateStr + ")");
				} else if (value == null) {
					setText("");
				}
				return this;
			}
		});
		if (pendingCommand != null) {
			targetCommandComboBox.setSelectedItem(pendingCommand);
		}
		form.add(targetCommandComboBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		refNoLabel = new JLabel(MessageBundle.getMessage("angal.command.refno.col") + " :");
		refNoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		form.add(refNoLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		refNoTextField = new VoLimitedTextField(50);
		refNoTextField.setPreferredSize(new Dimension(340, 28));
		form.add(refNoTextField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		dateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt") + " :");
		dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		form.add(dateLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		dateChooser = new GoodDateChooser(LocalDate.now(), true, false);
		form.add(dateChooser, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		JLabel qtyLabel = new JLabel(MessageBundle.getMessage("angal.command.medicalorder.orderqty"));
		qtyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		form.add(qtyLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		qtyPanel.setOpaque(false);

		OrderAction firstAction = availableActions.isEmpty() ? null : availableActions.get(0);
		int initialQty = calculateDefaultQuantity(firstAction);
		qtySpinner = new JSpinner(new SpinnerNumberModel(initialQty, 1, 999999, 1));
		qtySpinner.setPreferredSize(new Dimension(100, 28));
		qtyPanel.add(qtySpinner);

		if (medical.getPcsperpck() > 0) {
			JLabel packInfo = new JLabel("  " + MessageBundle.getMessage("angal.medicals.pcsperpck.col") + ": " + medical.getPcsperpck());
			packInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
			packInfo.setForeground(new Color(110, 120, 135));
			qtyPanel.add(packInfo);
		}
		form.add(qtyPanel, gbc);

		return form;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		panel.setOpaque(false);

		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		Dimension prefCancel = cancelButton.getPreferredSize();
		cancelButton.setPreferredSize(new Dimension(Math.max(105, prefCancel.width + 24), 32));
		cancelButton.addActionListener(e -> dispose());

		confirmButton = new JButton(MessageBundle.getMessage("angal.command.medicalorder.confirm"));
		confirmButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		confirmButton.setBackground(new Color(39, 174, 96));
		confirmButton.setForeground(Color.WHITE);
		Dimension prefConfirm = confirmButton.getPreferredSize();
		confirmButton.setPreferredSize(new Dimension(Math.max(220, prefConfirm.width + 30), 32));
		confirmButton.setEnabled(!availableActions.isEmpty());
		confirmButton.addActionListener(e -> processOrder());

		panel.add(cancelButton);
		panel.add(confirmButton);
		return panel;
	}

	private OrderAction getSelectedAction() {
		int idx = actionComboBox.getSelectedIndex();
		if (idx >= 0 && idx < availableActions.size()) {
			return availableActions.get(idx);
		}
		return null;
	}

	private int calculateDefaultQuantity(OrderAction action) {
		if (action == OrderAction.REPLACE_EXPIRED && expiredStock > 0) {
			return (int) expiredStock;
		}
		if (validStock <= medical.getMinqty() && medical.getMinqty() > 0) {
			int needed = (int) ((medical.getMinqty() * 2) - validStock);
			return Math.max(1, needed);
		}
		if (medical.getPcsperpck() > 0) {
			return medical.getPcsperpck();
		}
		return 10;
	}

	private void onActionChanged() {
		updateFormVisibility();
		OrderAction action = getSelectedAction();
		if (action != null) {
			qtySpinner.setValue(calculateDefaultQuantity(action));
		}
	}

	private void updateFormVisibility() {
		OrderAction action = getSelectedAction();
		boolean isNew = action == OrderAction.NEW_COMMAND;
		boolean isAddToExisting = action == OrderAction.ADD_TO_EXISTING;
		boolean isReplace = action == OrderAction.REPLACE_EXPIRED;

		refNoLabel.setVisible(isNew || (isReplace && openCommands.isEmpty()));
		refNoTextField.setVisible(isNew || (isReplace && openCommands.isEmpty()));
		dateLabel.setVisible(isNew || (isReplace && openCommands.isEmpty()));
		dateChooser.setVisible(isNew || (isReplace && openCommands.isEmpty()));

		targetCommandLabel.setVisible(isAddToExisting || (isReplace && !openCommands.isEmpty()));
		targetCommandComboBox.setVisible(isAddToExisting || (isReplace && !openCommands.isEmpty()));

		if (confirmButton != null) {
			confirmButton.setEnabled(!availableActions.isEmpty());
		}
		if (qtySpinner != null) {
			qtySpinner.setEnabled(!availableActions.isEmpty());
		}
	}

	private void processOrder() {
		OrderAction action = getSelectedAction();
		if (action == null) {
			return;
		}

		if (isAlreadyOrdered && action == OrderAction.NEW_COMMAND) {
			MessageDialog.error(this, "angal.command.medicalorder.error.cannotcreatenewwhenpending");
			return;
		}

		double orderQty = ((Number) qtySpinner.getValue()).doubleValue();

		if (orderQty <= 0) {
			MessageDialog.error(this, "angal.medicals.minquantitycannotbelessthan0.msg");
			return;
		}

		boolean isNewCommand = action == OrderAction.NEW_COMMAND ||
				(action == OrderAction.REPLACE_EXPIRED && openCommands.isEmpty());

		if (isNewCommand) {
			if (isAlreadyOrdered) {
				MessageDialog.error(this, "angal.command.medicalorder.error.cannotcreatenewwhenpending");
				return;
			}
			processNewCommand(orderQty);
		} else {
			processAddToExistingCommand(orderQty);
		}
	}

	private void processNewCommand(double orderQty) {
		String refNo = refNoTextField.getText().trim();
		if (refNo.isEmpty()) {
			MessageDialog.error(this, "angal.command.pleaseinsertarefno.msg");
			return;
		}

		LocalDate date = dateChooser.getDate();
		if (date == null) {
			MessageDialog.error(this, "angal.command.pleaseinsertadate.msg");
			return;
		}

		try {
			Command newCmd = new Command();
			newCmd.setRefNo(refNo);
			newCmd.setDate(date.atStartOfDay());
			Command savedCmd = commandBrowserManager.saveOrUpdate(newCmd);
			if (savedCmd == null) {
				MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
				return;
			}

			CommandRow row = new CommandRow();
			row.setCommand(savedCmd);
			row.setMedical(medical);
			row.setMedicalCode(medical.getProdCode());
			row.setMedicalDescription(medical.getDescription());
			row.setQtyInStore(currentStock);
			row.setCriticalLevel(medical.getMinqty());
			row.setOrderQty(orderQty);
			row.setUserAddedQty(0);
			commandBrowserManager.saveOrUpdateRow(row);

			commandProcessed = true;
			MessageDialog.info(this, MessageBundle.formatMessage("angal.command.medicalorder.success.created",
					savedCmd.getRefNo(), (int) orderQty, medical.getDescription()));
			dispose();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void processAddToExistingCommand(double orderQty) {
		Command targetCmd = (Command) targetCommandComboBox.getSelectedItem();
		if (targetCmd == null) {
			MessageDialog.error(this, "angal.command.medicalorder.noopencommand");
			return;
		}

		try {
			List<CommandRow> existingRows = commandBrowserManager.getRowsByCommand(targetCmd);
			CommandRow targetRow = null;
			if (existingRows != null) {
				for (CommandRow row : existingRows) {
					if (row.getMedical() != null && row.getMedical().getCode().equals(medical.getCode())) {
						targetRow = row;
						break;
					}
				}
			}

			if (targetRow != null) {
				double newTotalQty = (targetRow.getOrderQty() != null ? targetRow.getOrderQty() : 0) + orderQty;
				targetRow.setOrderQty(newTotalQty);
				commandBrowserManager.saveOrUpdateRow(targetRow);
			} else {
				CommandRow newRow = new CommandRow();
				newRow.setCommand(targetCmd);
				newRow.setMedical(medical);
				newRow.setMedicalCode(medical.getProdCode());
				newRow.setMedicalDescription(medical.getDescription());
				newRow.setQtyInStore(currentStock);
				newRow.setCriticalLevel(medical.getMinqty());
				newRow.setOrderQty(orderQty);
				newRow.setUserAddedQty(0);
				commandBrowserManager.saveOrUpdateRow(newRow);
			}

			commandProcessed = true;
			MessageDialog.info(this, MessageBundle.formatMessage("angal.command.medicalorder.success.added",
					(int) orderQty, medical.getDescription(), targetCmd.getRefNo()));
			dispose();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
