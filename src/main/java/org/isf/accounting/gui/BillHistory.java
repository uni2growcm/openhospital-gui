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
import java.util.ArrayList;
import java.util.Comparator;
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
import org.isf.accounting.model.ItemPayments;
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
    private JTable jTableItemPayments;

    private final String[] billItemColumnNames = {
            MessageBundle.getMessage("angal.common.date"),
            MessageBundle.getMessage("angal.billbrowser.description"),
            MessageBundle.getMessage("angal.billbrowser.quantity"),
            MessageBundle.getMessage("angal.billbrowser.action"),
            MessageBundle.getMessage("angal.billbrowser.auteur")
    };

    private final String[] paymentsColumnNames = {
            MessageBundle.getMessage("angal.common.date"),
            MessageBundle.getMessage("angal.billbrowser.auteur"),
            MessageBundle.getMessage("angal.billbrowser.montant"),
            MessageBundle.getMessage("angal.billbrowser.action")
    };

    private final String[] itemPaymentsColumnNames = {
            MessageBundle.getMessage("angal.common.date"),
            MessageBundle.getMessage("angal.billbrowser.description"),
            MessageBundle.getMessage("angal.billbrowser.montant"),
            MessageBundle.getMessage("angal.billbrowser.auteur"),
            MessageBundle.getMessage("angal.billbrowser.action")
    };

    private final int[] billItemsColumnWidths = { 120, 250, 80, 100 };
    private final int[] paymentsColumnWidths = { 120, 150, 100, 100 };
    private final int[] itemPaymentsColumnWidths = { 120, 250, 100, 150, 100 };

    private final BillBrowserManager billManager;
    private final List<BillItems> billItems;
    private final List<BillPayments> billPayments;
    private final List<ItemPayments> itemPayments;

    private final List<BillHistoryRow> billItemRows = new ArrayList<>();
    private final List<BillHistoryRow> paymentRows = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    public BillHistory(JFrame owner, Bill bill) throws OHServiceException {
        super(owner, true);
        this.billManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
        this.billItems = billManager.getAllBillItems(bill);
        this.billPayments = billManager.getAllBillPayments(bill);
        this.itemPayments = billManager.getItemPayments(bill.getId());
        buildHistoryRows();
        initComponents();
    }

    public BillHistory(JDialog owner, Bill bill) throws OHServiceException {
        super(owner, true);
        this.billManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
        this.billItems = billManager.getAllBillItems(bill);
        this.billPayments = billManager.getAllBillPayments(bill);
        this.itemPayments = billManager.getItemPayments(bill.getId());
        buildHistoryRows();
        initComponents();
    }

    /**
     * Builds the history rows. A soft deleted item/payment is displayed twice: once as added at its
     * creation date and once as removed at its last modification date.
     */
    private void buildHistoryRows() {
        billItemRows.clear();
        for (BillItems item : billItems) {
            int quantity = Math.abs(item.getItemQuantity());
            String addAction = item.getItemQuantity() > 0
                    ? MessageBundle.getMessage("angal.billbrowser.add")
                    : MessageBundle.getMessage("angal.billbrowser.retour");
            boolean removed = item.getActive() == 0;
            LocalDateTime addDate = removed && item.getCreatedDate() != null
                    ? item.getCreatedDate()
                    : item.getItemDate();
            billItemRows.add(new BillHistoryRow(addDate, item.getItemDescription(), item.getCreatedBy(), quantity, null, addAction));
            if (removed) {
                LocalDateTime removeDate = item.getLastModifiedDate() != null
                        ? item.getLastModifiedDate()
                        : item.getItemDate();
                billItemRows.add(new BillHistoryRow(removeDate, item.getItemDescription(), item.getLastModifiedBy(), quantity, null,
                        MessageBundle.getMessage("angal.billbrowser.remove")));
            }
        }
        billItemRows.sort(Comparator.comparing(BillHistoryRow::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        paymentRows.clear();
        for (BillPayments payment : billPayments) {
            double amount = Math.abs(payment.getAmount());
            String addAction = payment.getAmount() > 0
                    ? MessageBundle.getMessage("angal.billbrowser.buy")
                    : MessageBundle.getMessage("angal.billbrowser.refund");
            boolean removed = payment.getActive() == 0;
            LocalDateTime addDate = removed && payment.getCreatedDate() != null
                    ? payment.getCreatedDate()
                    : payment.getDate();
            paymentRows.add(new BillHistoryRow(addDate, null, payment.getUser(), null, amount, addAction));
            if (removed) {
                LocalDateTime removeDate = payment.getLastModifiedDate() != null
                        ? payment.getLastModifiedDate()
                        : payment.getDate();
                paymentRows.add(new BillHistoryRow(removeDate, null, payment.getLastModifiedBy(), null, amount,
                        MessageBundle.getMessage("angal.billbrowser.remove")));
            }
        }
        paymentRows.sort(Comparator.comparing(BillHistoryRow::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
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

            /*---- Item payment in bill history ----*/
//            tablePanel.add(Box.createVerticalStrut(20));
            String itemPaymentTitle = MessageBundle.getMessage("angal.billbrowser.itempayment.title");
            JPanel itemPaymentPanel = new JPanel(new BorderLayout());
            itemPaymentPanel.setBorder(BorderFactory.createTitledBorder(itemPaymentTitle));
            JScrollPane itemPaymentScrollPane = new JScrollPane(getJTableItemPayments());
            itemPaymentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            itemPaymentPanel.add(itemPaymentScrollPane, BorderLayout.CENTER);
//            tablePanel.add(itemPaymentPanel);

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

    private JTable getJTableItemPayments() {
        if (jTableItemPayments == null) {
            jTableItemPayments = new JTable();
            jTableItemPayments.setFillsViewportHeight(true);
            jTableItemPayments.setModel(new ItemPaymentsRowModel());
        }
        return jTableItemPayments;
    }

    private void adjustWidth() {
        for (int i = 0; i < billItemsColumnWidths.length && i < jTableBillItems.getColumnCount(); i++) {
            jTableBillItems.getColumnModel().getColumn(i).setMinWidth(billItemsColumnWidths[i]);
        }
        for (int i = 0; i < paymentsColumnWidths.length && i < jTablePaymentRow.getColumnCount(); i++) {
            jTablePaymentRow.getColumnModel().getColumn(i).setMinWidth(paymentsColumnWidths[i]);
        }
        for (int i = 0; i < itemPaymentsColumnWidths.length && i < jTableItemPayments.getColumnCount(); i++) {
            jTableItemPayments.getColumnModel().getColumn(i).setMinWidth(itemPaymentsColumnWidths[i]);
        }
    }

    class ItemPaymentsRowModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return itemPayments == null ? 0 : itemPayments.size();
        }

        @Override
        public int getColumnCount() {
            return itemPaymentsColumnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return itemPaymentsColumnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row >= itemPayments.size()) {
                return null;
            }
            ItemPayments ip = itemPayments.get(row);
            double amount = ip.getAmount();

            switch (column) {
                case 0:
                    return formatDateTime(ip.getDate());
                case 1:
                    return ip.getItemDescription();
                case 2:
                    return amount < 0 ? -amount : amount;
                case 3:
                    return ip.getUser() != null ? ip.getUser() : "";
                case 4:
                    return ip.isRefund()
                            ? MessageBundle.getMessage("angal.billbrowser.refund")
                            : MessageBundle.getMessage("angal.billbrowser.buy");
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

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATE_FORMATTER.format(dateTime);
    }

    private static final class BillHistoryRow {

        private final LocalDateTime date;
        private final String description;
        private final String user;
        private final Integer quantity;
        private final Double amount;
        private final String action;

        private BillHistoryRow(LocalDateTime date, String description, String user, Integer quantity, Double amount, String action) {
            this.date = date;
            this.description = description;
            this.user = user;
            this.quantity = quantity;
            this.amount = amount;
            this.action = action;
        }

        private LocalDateTime getDate() {
            return date;
        }
    }

    class BillItemRowModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return billItemRows.size();
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
            if (row >= billItemRows.size()) {
                return null;
            }
            BillHistoryRow item = billItemRows.get(row);

            return switch (column) {
                case 0 -> formatDateTime(item.date);
                case 1 -> item.description;
                case 2 -> item.quantity;
                case 3 -> item.action;
                case 4 -> item.user;
                default -> null;
            };
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
            return paymentRows.size();
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
            if (row >= paymentRows.size()) {
                return null;
            }
            BillHistoryRow payment = paymentRows.get(row);

            switch (column) {
                case 0:
                    return formatDateTime(payment.date);
                case 1:
                    return payment.user != null ? payment.user : "";
                case 2:
                    return payment.amount;
                case 3:
                    return payment.action;
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