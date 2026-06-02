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
package org.isf.accounting.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.PatientBillEdit.PatientBillListener;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.accounting.dto.RefundBillItemDto;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;

/**
 * Dialog allowing the user to refund selected items of a closed bill.
 * A new refund bill (with {@code parentId} pointing to the original) is created,
 * together with its items and a single negative payment.
 *
 * @author Duval Donfack
 */
public class BillRefund extends JDialog {

	private static final long serialVersionUID = 1L;

	// ---- Listener -------------------------------------------------------

	private static final EventListenerList LISTENERS = new EventListenerList();

	public void addPatientBillListener(PatientBillListener l) {
		LISTENERS.add(PatientBillListener.class, l);
	}

	private void fireRefundCompleted(Bill refundBill) {
		AWTEvent event = new AWTEvent(refundBill, AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (PatientBillListener l : LISTENERS.getListeners(PatientBillListener.class)) {
			l.billInserted(event);
		}
	}

	// ---- Layout constants -----------------------------------------------

	private static final int PANEL_WIDTH = 700;
	private static final int TABLE_HEIGHT = 300;
	private static final int FOOTER_HEIGHT = 30;

	// ---- Table columns --------------------------------------------------

	private static final int COL_DESCRIPTION = 0;
	private static final int COL_TOTAL_QTY = 1;
	private static final int COL_UNIT_PRICE = 2;
	private static final int COL_ALREADY_REFUNDED = 3;
	private static final int COL_REFUND_QTY = 4;

	private final boolean[] columnEditable = { false, false, false, false, true };
	private final String[] columnNames = {
		MessageBundle.getMessage("angal.billrefund.item.col"),
		MessageBundle.getMessage("angal.billrefund.qty.col"),
		MessageBundle.getMessage("angal.billrefund.unitprice.col"),
		MessageBundle.getMessage("angal.billrefund.alreadyrefunded.col"),
		MessageBundle.getMessage("angal.billrefund.refundqty.col"),
	};

	// ---- UI components --------------------------------------------------

	private JTable jTableItems;
	private JTable jTableTotal;
	private JTextField jTextFieldEditor;

	// ---- State ----------------------------------------------------------

	private final BillBrowserManager billManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
	private final String user = UserBrowsingManager.getCurrentUser();
	private final Bill originalBill;
	private List<RefundBillItemDto> refundItems = new ArrayList<>();
	private BigDecimal totalToRefund = BigDecimal.ZERO;

	// ---- Constructor ----------------------------------------------------

	public BillRefund(JFrame owner, Bill bill) {
		super(owner, true);
		this.originalBill = bill;
		loadRefundItems();
		initComponents();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
	}

	// ---- Initialisation -------------------------------------------------

	private void loadRefundItems() {
		try {
			refundItems = billManager.getRefundItems(originalBill.getId());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.billrefund.title") + " #" + originalBill.getId());
		setLayout(new BorderLayout());
		add(buildInfoPanel(), BorderLayout.NORTH);
		add(buildDataPanel(), BorderLayout.CENTER);
		add(buildButtonPanel(), BorderLayout.SOUTH);
		pack();
	}

	// ---- Info panel (date + patient) ------------------------------------

	private JPanel buildInfoPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		panel.add(new JLabel(MessageBundle.getMessage("angal.common.date.txt") + ":"));
		JTextField dateField = new JTextField(
			TimeTools.getNow().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
		dateField.setEditable(false);
		dateField.setPreferredSize(new Dimension(160, 25));
		panel.add(dateField);

		panel.add(Box.createHorizontalStrut(20));
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + ":"));
		JTextField patientField = new JTextField(originalBill.getPatName());
		patientField.setEditable(false);
		patientField.setPreferredSize(new Dimension(220, 25));
		panel.add(patientField);

		panel.add(Box.createHorizontalStrut(20));
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.ward.txt") + ":"));
		String wardName = originalBill.getWard() != null ? originalBill.getWard().getDescription() : "";
		JTextField wardField = new JTextField(wardName);
		wardField.setEditable(false);
		wardField.setPreferredSize(new Dimension(130, 25));
		panel.add(wardField);

		return panel;
	}

	// ---- Data panel (help text + items table + total row) ---------------

	private JPanel buildDataPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(buildHelpPanel());
		panel.add(buildItemsScrollPane());
		panel.add(buildTotalScrollPane());
		return panel;
	}

	private JPanel buildHelpPanel() {
		JPanel helpPanel = new JPanel(new FlowLayout());
		JLabel label = new JLabel(MessageBundle.getMessage("angal.billrefund.helptext.lbl"));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		helpPanel.add(label);
		helpPanel.setMaximumSize(new Dimension(PANEL_WIDTH, FOOTER_HEIGHT));
		return helpPanel;
	}

	private JScrollPane buildItemsScrollPane() {
		JScrollPane scroll = new JScrollPane(buildItemsTable());
		scroll.setPreferredSize(new Dimension(PANEL_WIDTH, TABLE_HEIGHT));
		scroll.setMaximumSize(new Dimension(PANEL_WIDTH, TABLE_HEIGHT));
		scroll.setMinimumSize(new Dimension(PANEL_WIDTH, TABLE_HEIGHT));
		return scroll;
	}

	private JTable buildItemsTable() {
		jTextFieldEditor = new JTextField();

		jTableItems = new JTable(new RefundItemsTableModel());
		jTableItems.setFillsViewportHeight(true);
		jTableItems.setAutoCreateColumnsFromModel(false);

		jTableItems.getColumnModel().getColumn(COL_TOTAL_QTY).setMinWidth(60);
		jTableItems.getColumnModel().getColumn(COL_TOTAL_QTY).setMaxWidth(70);
		jTableItems.getColumnModel().getColumn(COL_UNIT_PRICE).setMinWidth(80);
		jTableItems.getColumnModel().getColumn(COL_UNIT_PRICE).setMaxWidth(100);
		jTableItems.getColumnModel().getColumn(COL_ALREADY_REFUNDED).setMinWidth(100);
		jTableItems.getColumnModel().getColumn(COL_ALREADY_REFUNDED).setMaxWidth(130);
		jTableItems.getColumnModel().getColumn(COL_REFUND_QTY).setMinWidth(100);
		jTableItems.getColumnModel().getColumn(COL_REFUND_QTY).setMaxWidth(130);

		jTableItems.setDefaultEditor(Integer.class, new DefaultCellEditor(jTextFieldEditor));

		jTableItems.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int row = jTableItems.getSelectedRow();
					if (row >= 0) {
						jTableItems.editCellAt(row, COL_REFUND_QTY);
						jTableItems.setSurrendersFocusOnKeystroke(true);
						Component editor = jTableItems.getEditorComponent();
						if (editor instanceof JTextField) {
							editor.requestFocus();
							((JTextField) editor).selectAll();
						}
					}
				}
			}
		});

		return jTableItems;
	}

	private JScrollPane buildTotalScrollPane() {
		jTableTotal = new JTable(new DefaultTableModel(
			new Object[][] {{
				"<html><b>" + MessageBundle.getMessage("angal.billrefund.totaltorefund.lbl") + "</b></html>",
				totalToRefund.doubleValue()
			}},
			new String[] { "", "" }
		) {
			private static final long serialVersionUID = 1L;
			@Override public boolean isCellEditable(int r, int c) { return false; }
			@Override public Class<?> getColumnClass(int c) { return c == 1 ? Double.class : String.class; }
		});
		jTableTotal.getColumnModel().getColumn(1).setMinWidth(100);
		jTableTotal.getColumnModel().getColumn(1).setMaxWidth(130);

		JScrollPane scroll = new JScrollPane(jTableTotal);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scroll.setPreferredSize(new Dimension(PANEL_WIDTH, FOOTER_HEIGHT));
		scroll.setMaximumSize(new Dimension(PANEL_WIDTH, FOOTER_HEIGHT));
		scroll.setMinimumSize(new Dimension(PANEL_WIDTH, FOOTER_HEIGHT));
		return scroll;
	}

	// ---- Button panel ---------------------------------------------------

	private JPanel buildButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout());

		JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.setIcon(new ImageIcon("rsc/icons/save_button.png"));
		saveButton.addActionListener(e -> onSave());

		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.setIcon(new ImageIcon("rsc/icons/close_button.png"));
		cancelButton.addActionListener(e -> {
			if (MessageDialog.yesNo(this, "angal.billrefund.cancelconfirm.msg") == JOptionPane.YES_OPTION) {
				dispose();
			}
		});

		panel.add(saveButton);
		panel.add(cancelButton);
		return panel;
	}

	// ---- Save logic -----------------------------------------------------

	private void onSave() {
		if (jTableItems.isEditing()) {
			jTableItems.getCellEditor().stopCellEditing();
		}

		List<BillItems> itemsToRefund = buildRefundItemsList();
		if (itemsToRefund.isEmpty()) {
			MessageDialog.error(this, "angal.billrefund.pleaseselectitems.msg");
			return;
		}

		LocalDateTime now = TimeTools.getNow();

		Bill refundBill = new Bill(
			0, now, now,
			originalBill.isList(), originalBill.getPriceList(), originalBill.getListName(),
			originalBill.isPatient(), originalBill.getBillPatient(), originalBill.getPatName(),
			"C",
			totalToRefund.doubleValue(), 0.0,
			0, user,
			originalBill.getAdmission(), null,
			originalBill.getWard(), originalBill.getGuarantor()
		);
		refundBill.setParentId(originalBill.getId());

		BillPayments payment = new BillPayments(0, null, now, -totalToRefund.doubleValue(), user);
		List<BillPayments> payments = new ArrayList<>();
		payments.add(payment);

		try {
			Bill saved = billManager.refundBill(originalBill, refundBill, itemsToRefund, payments);
			fireRefundCompleted(saved);
			dispose();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}

	private List<BillItems> buildRefundItemsList() {
		List<BillItems> items = new ArrayList<>();
		for (RefundBillItemDto rbi : refundItems) {
			if (rbi.getRefundQty() > 0) {
				BillItems item = new BillItems(rbi.getBillItem());
				item.setId(0);
				item.setBill(null);
				item.setItemQuantity(rbi.getRefundQty());
				items.add(item);
			}
		}
		return items;
	}

	// ---- Total update ---------------------------------------------------

	private void updateTotal() {
		totalToRefund = BigDecimal.ZERO;
		for (RefundBillItemDto rbi : refundItems) {
			if (rbi.getRefundQty() > 0) {
				totalToRefund = totalToRefund.add(
					BigDecimal.valueOf(rbi.getBillItem().getItemAmount())
						.multiply(BigDecimal.valueOf(rbi.getRefundQty()))
				);
			}
		}
		if (jTableTotal != null) {
			jTableTotal.getModel().setValueAt(totalToRefund.doubleValue(), 0, 1);
		}
	}

	// ---- Table model ----------------------------------------------------

	private class RefundItemsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override public int getRowCount() { return refundItems == null ? 0 : refundItems.size(); }
		@Override public int getColumnCount() { return columnNames.length; }
		@Override public String getColumnName(int c) { return columnNames[c]; }
		@Override public boolean isCellEditable(int r, int c) { return columnEditable[c]; }

		@Override
		public Class<?> getColumnClass(int c) {
			return switch (c) {
				case COL_TOTAL_QTY, COL_ALREADY_REFUNDED, COL_REFUND_QTY -> Integer.class;
				case COL_UNIT_PRICE -> Double.class;
				default -> String.class;
			};
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (refundItems == null || r >= refundItems.size()) return null;
			RefundBillItemDto rbi = refundItems.get(r);
			return switch (c) {
				case -1 -> rbi;
				case COL_DESCRIPTION -> rbi.getBillItem().getItemDescription();
				case COL_TOTAL_QTY -> rbi.getBillItem().getItemQuantity();
				case COL_UNIT_PRICE -> rbi.getBillItem().getItemAmount();
				case COL_ALREADY_REFUNDED -> rbi.getAlreadyRefundedQty();
				case COL_REFUND_QTY -> rbi.getRefundQty();
				default -> null;
			};
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			if (c != COL_REFUND_QTY || refundItems == null || r >= refundItems.size()) return;

			int qty = 0;
			try {
				qty = Integer.parseInt(value.toString().trim());
			} catch (NumberFormatException ignored) {
			}
			if (qty < 0) qty = 0;

			RefundBillItemDto rbi = refundItems.get(r);
			if (qty > rbi.getRefundableQty()) {
				MessageDialog.error(BillRefund.this, "angal.billrefund.qtygreaterthanrefundable.msg");
				return;
			}
			rbi.setRefundQty(qty);
			updateTotal();
		}
	}
}
