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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillHistory extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BillHistory.class);

    private JPanel panelFooter;
    private JPanel panelContent;
    private JTable jTableBillItems;
    private JTable jTablePaymentRow;

    private final String[] billItemColumnNames = {
            MessageBundle.getMessage("angal.common.date"),
            MessageBundle.getMessage("angal.billbrowser.description"),
            MessageBundle.getMessage("angal.billbrowser.quantity"),
            MessageBundle.getMessage("angal.billbrowser.action")
    };

    private final String[] paymentsColumnNames = {
            MessageBundle.getMessage("angal.common.date"),
            MessageBundle.getMessage("angal.billbrowser.auteur"),
            MessageBundle.getMessage("angal.billbrowser.montant"),
            MessageBundle.getMessage("angal.billbrowser.action")
    };

    private final int[] billItemsColumnWidths = { 120, 250, 80, 100 };
    private final int[] paymentsColumnWidths = { 120, 150, 100, 100 };

    private final BillBrowserManager billManager;
    private final List<BillItems> billItems;
    private final List<BillPayments> billPayments;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    public BillHistory(JFrame owner, Bill bill) throws OHServiceException {
        super(owner, true);
        this.billManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
        this.billItems = billManager.getAllBillItems(bill);
        this.billPayments = billManager.getAllBillPayments(bill);
        initComponents();
    }

    public BillHistory(JDialog owner, Bill bill) throws OHServiceException {
        super(owner, true);
        this.billManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
        this.billItems = billManager.getAllBillItems(bill);
        this.billPayments = billManager.getAllBillPayments(bill);
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setTitle(MessageBundle.getMessage("angal.billhistory.title"));

        getContentPane().setLayout(new BorderLayout());

        panelContent = getPanelContent();
        getContentPane().add(panelContent, BorderLayout.CENTER);

        panelFooter = getPanelFooter();
        getContentPane().add(panelFooter, BorderLayout.SOUTH);

        adjustWidth();
    }

    private JPanel getPanelContent() {
        if (panelContent == null) {
            panelContent = new JPanel();
            panelContent.setLayout(new BorderLayout());

            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));

            String historyTitle = MessageBundle.getMessage("angal.billbrowser.history.title");
            String paymentTitle = MessageBundle.getMessage("angal.billbrowser.payment.title");

            JPanel invoicePanel = new JPanel(new BorderLayout());
            invoicePanel.setBorder(BorderFactory.createTitledBorder(historyTitle));
            JScrollPane invoiceScrollPane = new JScrollPane(getJTableBillItems());
            invoiceScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            invoicePanel.add(invoiceScrollPane, BorderLayout.CENTER);
            tablePanel.add(invoicePanel);

            tablePanel.add(Box.createVerticalStrut(20));

            JPanel paymentPanel = new JPanel(new BorderLayout());
            paymentPanel.setBorder(BorderFactory.createTitledBorder(paymentTitle));
            JScrollPane paymentScrollPane = new JScrollPane(getJTablePaymentRow());
            paymentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            paymentPanel.add(paymentScrollPane, BorderLayout.CENTER);
            tablePanel.add(paymentPanel);

            panelContent.add(tablePanel, BorderLayout.CENTER);
        }
        return panelContent;
    }

    private JPanel getPanelFooter() {
        if (panelFooter == null) {
            panelFooter = new JPanel();
            panelFooter.add(getCloseButton());
        }
        return panelFooter;
    }

    private JButton getCloseButton() {
        JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close"));
        closeButton.setMnemonic(KeyEvent.VK_C);
        closeButton.addActionListener(e -> dispose());
        return closeButton;
    }

    private JTable getJTableBillItems() {
        if (jTableBillItems == null) {
            jTableBillItems = new JTable();
            jTableBillItems.setFillsViewportHeight(true);
            jTableBillItems.setModel(new BillItemRowModel());
        }
        return jTableBillItems;
    }

    private JTable getJTablePaymentRow() {
        if (jTablePaymentRow == null) {
            jTablePaymentRow = new JTable();
            jTablePaymentRow.setFillsViewportHeight(true);
            jTablePaymentRow.setModel(new PaymentRowModel());
        }
        return jTablePaymentRow;
    }

    private void adjustWidth() {
        for (int i = 0; i < billItemsColumnWidths.length && i < jTableBillItems.getColumnCount(); i++) {
            jTableBillItems.getColumnModel().getColumn(i).setMinWidth(billItemsColumnWidths[i]);
        }
        for (int i = 0; i < paymentsColumnWidths.length && i < jTablePaymentRow.getColumnCount(); i++) {
            jTablePaymentRow.getColumnModel().getColumn(i).setMinWidth(paymentsColumnWidths[i]);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATE_FORMATTER.format(dateTime);
    }

    class BillItemRowModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return billItems == null ? 0 : billItems.size();
        }

        @Override
        public int getColumnCount() {
            return billItemColumnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return billItemColumnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row >= billItems.size()) {
                return null;
            }
            BillItems item = billItems.get(row);
            int qty = item.getItemQuantity();

            switch (column) {
                case 0:
                    return formatDateTime(item.getItemDate());
                case 1:
                    return item.getItemDescription();
                case 2:
                    return qty > 0 ? qty : -qty;
                case 3:
                    return qty > 0
                            ? MessageBundle.getMessage("angal.billbrowser.add")
                            : MessageBundle.getMessage("angal.billbrowser.retour");
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Integer.class;
            }
            return String.class;
        }
    }

    class PaymentRowModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return billPayments == null ? 0 : billPayments.size();
        }

        @Override
        public int getColumnCount() {
            return paymentsColumnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return paymentsColumnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row >= billPayments.size()) {
                return null;
            }
            BillPayments payment = billPayments.get(row);
            double amount = payment.getAmount();

            switch (column) {
                case 0:
                    return formatDateTime(payment.getDate());
                case 1:
                    return payment.getUser() != null ? payment.getUser() : "";
                case 2:
                    return amount > 0 ? amount : -amount;
                case 3:
                    return amount > 0
                            ? MessageBundle.getMessage("angal.billbrowser.buy")
                            : MessageBundle.getMessage("angal.billbrowser.refund");
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Double.class;
            }
            return String.class;
        }
    }
}