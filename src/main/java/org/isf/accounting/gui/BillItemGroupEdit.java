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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.BillItemGroup;
import org.isf.accounting.model.BillItemGroupItem;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.Price;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillItemGroupEdit extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BillItemGroupEdit.class);

    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 30;
    private static final int PRICE_WIDTH = 140;
    private static final int QUANTITY_WIDTH = 100;

    private final BillBrowserManager manager =
            Context.getApplicationContext()
                    .getBean(BillBrowserManager.class);

    private final PriceListManager priceListManager =
            Context.getApplicationContext()
                    .getBean(PriceListManager.class);

    private final PricesOthersManager pricesOthersManager =
            Context.getApplicationContext()
                    .getBean(PricesOthersManager.class);

    private final OhDefaultCellRenderer cellRenderer =
            new OhDefaultCellRenderer();

    private final boolean insert;

    private BillItemGroup billItemGroup;

    private boolean confirmed;

    private BigDecimal total = BigDecimal.ZERO;

    private final List<BillItemGroupItem> items =
            new ArrayList<>();

    private final List<Price> prcListArray =
            new ArrayList<>();

    private JTextField titleTextField;
    private JTextField descriptionTextField;

    private JTextField searchTextField;
    private JTextField quantityTextField;
    private JTextField priceTextField;
    private JTextField itemDescriptionTextField;

    private JTable jTableBill;
    private JTable jTableTotal;

    private JButton jButtonAddMedical;
    private JButton jButtonAddOperation;
    private JButton jButtonAddExam;
    private JButton jButtonAddOther;

    private JButton jButtonRemoveItem;
    private JButton jButtonRemoveAllItem;

    private JPanel searchPanel;
    private JPanel jPanelButtons;

    private BillTableModel billTableModel;

    private BillItemGroupItem selectedItem;

    public BillItemGroupEdit(JDialog owner, BillItemGroup billItemGroup, boolean insert) {
        super(owner, true);

        this.billItemGroup = billItemGroup;
        this.insert = insert;

        if (this.billItemGroup == null) {
            this.billItemGroup = new BillItemGroup();
        }

        setTitle(insert ? MessageBundle.getMessage("angal.newgroupitem.title") : MessageBundle.getMessage("angal.newbill.editgroupitem.title"));
        initialize();
        loadData();
        updateTotals();
    }

    private void initialize() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(getOwner());
        Image iconImage = new ImageIcon(getClass().getResource("/icons/oh.png")).getImage();
        setIconImage(iconImage);
        initPriceList();

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setContentPane(contentPanel);
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.add(createFieldsPanel(), BorderLayout.NORTH);
        northPanel.add(getSearchPanel(), BorderLayout.SOUTH);

        contentPanel.add(northPanel, BorderLayout.NORTH);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);

        contentPanel.add(getJPanelButtons(), BorderLayout.EAST);
        contentPanel.add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));

        panel.add(new JLabel(MessageBundle.getMessage("angal.newgroupitem.titlelabel")));

        titleTextField = new JTextField();

        panel.add(titleTextField);

        panel.add(new JLabel(MessageBundle.getMessage("angal.newgroupitem.descriptionlabel")));

        descriptionTextField = new JTextField();

        panel.add(descriptionTextField);

        return panel;
    }

    private JPanel getSearchPanel() {

        if (searchPanel == null) {
            searchPanel = new JPanel(new GridBagLayout());
            searchPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.billbrowser.find")));

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(2, 5, 2, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridy = 0;

            gbc.gridx = 0;
            searchPanel.add(createLabel(MessageBundle.getMessage("angal.billbrowser.find")), gbc);

            gbc.gridx = 1;
            searchPanel.add(createLabel(MessageBundle.getMessage("angal.newbill.itemgrouptable.description")), gbc);

            gbc.gridx = 2;
            searchPanel.add(createLabel(MessageBundle.getMessage("angal.newbill.qty")), gbc);

            gbc.gridx = 3;
            searchPanel.add(createLabel(MessageBundle.getMessage("angal.newbill.amount")), gbc);

            gbc.gridy = 1;

            gbc.gridx = 0;
            gbc.weightx = 1;
            searchPanel.add(getSearchTextField(), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            searchPanel.add(getItemDescriptionTextField(), gbc);

            gbc.gridx = 2;
            gbc.weightx = 0;
            searchPanel.add(getQuantityTextField(), gbc);

            gbc.gridx = 3;
            searchPanel.add(getPriceTextField(), gbc);
        }

        return searchPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }

    private JTextField getSearchTextField() {
        if (searchTextField == null) {
            searchTextField = new JTextField(20);
            searchTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    searchTextField.selectAll();
                }
            });

            searchTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        searchItem();
                    }
                }
            });
        }

        return searchTextField;
    }

    private JTextField getItemDescriptionTextField() {
        if (itemDescriptionTextField == null) {
            itemDescriptionTextField = new JTextField();
            itemDescriptionTextField.setEditable(false);
        }

        return itemDescriptionTextField;
    }

    private JTextField getQuantityTextField() {
        if (quantityTextField == null) {
            quantityTextField = new JTextField("1", 10);
            quantityTextField.setHorizontalAlignment(JTextField.RIGHT);

            quantityTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(
                        FocusEvent e) {
                    quantityTextField.selectAll();
                }
            });

            quantityTextField.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            updateSelectedItemQuantity();
                        }
                    }
                }
            );
        }

        return quantityTextField;
    }

    private JTextField getPriceTextField() {
        if (priceTextField == null) {
            priceTextField = new JTextField();
            priceTextField.setEditable(false);
            priceTextField.setHorizontalAlignment(JTextField.RIGHT);
        }

        return priceTextField;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane billScrollPane = new JScrollPane(getJTableBill());
        JScrollPane totalScrollPane = new JScrollPane(getJTableTotal());
        totalScrollPane.setPreferredSize(new Dimension(100, 35));
        totalScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        panel.add(billScrollPane);
        panel.add(Box.createVerticalStrut(5));
        panel.add(totalScrollPane);

        return panel;
    }

    private JPanel getJPanelButtons() {
        if (jPanelButtons == null) {
            jPanelButtons = new JPanel();
            jPanelButtons.setLayout(new BoxLayout(jPanelButtons, BoxLayout.Y_AXIS));

            jPanelButtons.add(getJButtonAddMedical());
            jPanelButtons.add(Box.createVerticalStrut(5));

            jPanelButtons.add(getJButtonAddOperation());
            jPanelButtons.add(Box.createVerticalStrut(5));

            jPanelButtons.add(getJButtonAddExam());
            jPanelButtons.add(Box.createVerticalStrut(5));

            jPanelButtons.add(getJButtonAddOther());
            jPanelButtons.add(Box.createVerticalStrut(20));

            jPanelButtons.add(getJButtonRemoveItem());
            jPanelButtons.add(Box.createVerticalStrut(5));

            jPanelButtons.add(getJButtonRemoveAllItem());
            jPanelButtons.add(Box.createVerticalGlue());
        }

        return jPanelButtons;
    }

    private JButton getJButtonAddMedical() {
        if (jButtonAddMedical == null) {
            jButtonAddMedical = createAddButton("Add Medical", "MED");
        }

        return jButtonAddMedical;
    }

    private JButton getJButtonAddOperation() {
        if (jButtonAddOperation == null) {
            jButtonAddOperation = createAddButton("Add Operation", "OPE");
        }

        return jButtonAddOperation;
    }

    private JButton getJButtonAddExam() {
        if (jButtonAddExam == null) {
            jButtonAddExam = createAddButton("Add Exam", "EXA");
        }

        return jButtonAddExam;
    }

    private JButton getJButtonAddOther() {
        if (jButtonAddOther == null) {
            jButtonAddOther = createAddButton("Add Other", "OTH");
        }

        return jButtonAddOther;
    }

    private JButton createAddButton(String text, String group) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
        button.addActionListener(e -> addItemFromGroup(group));

        return button;
    }

    private JButton getJButtonRemoveItem() {
        if (jButtonRemoveItem == null) {
            jButtonRemoveItem = new JButton(MessageBundle.getMessage("angal.newbill.removeitem.btn"));
            jButtonRemoveItem.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            jButtonRemoveItem.setIcon(new ImageIcon("rsc/icons/delete_button.png"));

            jButtonRemoveItem.addActionListener(e -> {
                int row = jTableBill.getSelectedRow();

                if (row >= 0) {
                    removeItem(row);
                }
            });
        }

        return jButtonRemoveItem;
    }

    private JButton getJButtonRemoveAllItem() {
        if (jButtonRemoveAllItem == null) {
            jButtonRemoveAllItem = new JButton(MessageBundle.getMessage("angal.newbill.removeallitem.btn"));
            jButtonRemoveAllItem.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            jButtonRemoveAllItem.setIcon( new ImageIcon("rsc/icons/delete_button.png"));

            jButtonRemoveAllItem.addActionListener(e -> {
                if (items.isEmpty()) {
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    MessageBundle.getMessage("angal.newbill.removeallitems.msg"),
                    MessageBundle.getMessage("angal.newbill.confirm.msg"),
                    JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                items.clear();

                billTableModel.fireTableDataChanged();

                clearFields();

                updateTotals();
            });
        }

        return jButtonRemoveAllItem;
    }

    private JTable getJTableBill() {
        if (jTableBill == null) {

            billTableModel = new BillTableModel();
            jTableBill = new JTable(billTableModel);
            jTableBill.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jTableBill.setDefaultRenderer(Object.class, cellRenderer);
            jTableBill.getColumnModel().getColumn(1).setPreferredWidth(QUANTITY_WIDTH);
            jTableBill.getColumnModel().getColumn(2).setPreferredWidth(PRICE_WIDTH);

            jTableBill.getSelectionModel()
                .addListSelectionListener(
                    (
                            ListSelectionEvent e
                    ) -> {

                        int row =
                                jTableBill.getSelectedRow();

                        if (row < 0
                                || row >= items.size()) {

                            selectedItem = null;

                            clearFields();

                            return;
                        }

                        selectedItem = items.get(row);

                        loadSelectedItem();
                    }
                );
        }

        return jTableBill;
    }

    private JTable getJTableTotal() {

        if (jTableTotal == null) {

            jTableTotal = new JTable();

            jTableTotal.setModel(new DefaultTableModel(
                new Object[][]{
                    {
                        "TOTAL",
                        0.0
                    }
                },
                new String[]{"", ""}
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            jTableTotal.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(PRICE_WIDTH);

            jTableTotal.setRowHeight(25);
        }

        return jTableTotal;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton saveButton = new JButton(MessageBundle.getMessage("angal.newgroupitem.save"));

        JButton cancelButton = new JButton(MessageBundle.getMessage("angal.newgroupitem.cancel"));
        saveButton.addActionListener(e -> onSave());

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void initPriceList() {
        try {
            prcListArray.clear();
            prcListArray.addAll(priceListManager.getPrices());
        } catch (OHServiceException e) {
            MessageDialog.error(this, e.getMessage());
        }
    }

    private void searchItem() {
        String searchValue = searchTextField.getText().trim();

        if (searchValue.isEmpty()) {
            return;
        }

        Map<String, Price> uniquePrices = new LinkedHashMap<>();

        for (Price p : prcListArray) {

            String key = p.getGroup() + "_" + p.getItem();

            uniquePrices.putIfAbsent(key, p);
        }

        List<Price> uniqueList = new ArrayList<>(uniquePrices.values());

        OhTableModel<Price> model = new OhTableModel<>(uniqueList, true);
        Price selected = null;

        try {
            Object filtered = model.filter(searchValue);

            if (filtered instanceof Price) {
                selected = (Price) filtered;
            }
        } catch (Exception e) {
            MessageDialog.error(this, e.getMessage());
        }

        if (selected == null) {

            selected = BillItemPicker.showPicker(
                this,
                MessageBundle.getMessage("angal.newbill.item.title"),
                model,
                new ImageIcon("rsc/icons/plus_button.png")
            );
        }

        if (selected == null) {
            return;
        }

        int quantity = 1;

        try {
            quantity = Integer.parseInt(quantityTextField.getText().trim());
        } catch (Exception ignored) {
        }

        if (quantity <= 0) {
            quantity = 1;
        }

        handleSelectedPrice(selected, quantity);
        searchTextField.setText("");
    }

    private void addItemFromGroup(String group) {
        Map<String, Price> uniquePrices = new HashMap<>();

        for (Price p : prcListArray) {

            if (!group.equals(p.getGroup())) {
                continue;
            }

            String key = p.getGroup() + "_" + p.getItem();

            uniquePrices.putIfAbsent(key, p);
        }

        List<Price> filtered = new ArrayList<>(uniquePrices.values());

        OhTableModel<Price> model = new OhTableModel<>(filtered);

        Price selected = BillItemPicker.showPicker(this, MessageBundle.getMessage("angal.newbill.item.title"), model, new ImageIcon("rsc/icons/plus_button.png"));

        if (selected == null) {
            return;
        }

        int quantity = 1;

        String qtyString = JOptionPane.showInputDialog(this, "Quantity", 1);

        if (qtyString == null) {
            return;
        }

        try {
            quantity = Integer.parseInt(qtyString);
        } catch (Exception e) {
            quantity = 1;
        }

        handleSelectedPrice(selected, quantity);
    }

    private void handleSelectedPrice(Price price, int quantity) {

        if (price == null) { return; }

        double amount = price.getPrice();

        if ("OTH".equals(price.getGroup())) {
            try {
                Map<Integer, PricesOthers> map = new HashMap<>();

                for (PricesOthers other : pricesOthersManager.getOthers()) { map.put(other.getId(), other); }

                PricesOthers other = map.get(Integer.parseInt(price.getItem()));

                if (other != null) {
                    if (other.isUndefined()) {
                        String customPrice = JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.billbrowser.insertavalue.msg"), amount);

                        if (customPrice == null) {
                            return;
                        }

                        amount = Double.parseDouble(customPrice);
                    }

                    if (other.isDischarge()) {
                        amount = -amount;
                    }
                }
            } catch (Exception e) {
                MessageDialog.error(this, e.getMessage());
            }
        }

        BillItemGroupItem item = new BillItemGroupItem(billItemGroup, true, price.getGroup() + price.getItem(), price.getDesc(), amount, quantity);

        items.add(item);
        billTableModel.fireTableDataChanged();
        updateTotals();
    }

    private void updateSelectedItemQuantity() {

        if (selectedItem == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityTextField.getText().trim());

            if (quantity <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        MessageBundle.getMessage("angal.inventory.invalidquantity.msg")
                );

                return;
            }

            selectedItem.setQuantity(quantity);
            billTableModel.fireTableDataChanged();
            updateTotals();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void removeItem(int row) {
        if (row < 0 || row >= items.size()) {
            return;
        }

        items.remove(row);
        billTableModel.fireTableDataChanged();
        clearFields();
        updateTotals();
    }

    private void loadSelectedItem() {
        if (selectedItem == null) {
            return;
        }

        itemDescriptionTextField.setText(selectedItem.getDescription());
        quantityTextField.setText(String.valueOf(selectedItem.getQuantity()));
        priceTextField.setText(String.valueOf(selectedItem.getAmount()));
    }

    private void clearFields() {
        itemDescriptionTextField.setText("");
        quantityTextField.setText("");
        priceTextField.setText("");
    }

    private void loadData() {
        titleTextField.setText(billItemGroup.getTitle() != null ? billItemGroup.getTitle() : "");
        descriptionTextField.setText(billItemGroup.getDescription() != null ? billItemGroup.getDescription() : "");

        if (!insert && billItemGroup.getId() > 0) {
            try {
                items.clear();
                items.addAll(manager.getItemsByGroupId(billItemGroup.getId()));

                billTableModel.fireTableDataChanged();
            } catch (OHServiceException e) {
                MessageDialog.error(this, e.getMessage());
            }
        }
    }

    private void updateTotals() {
        total = BigDecimal.ZERO;

        for (BillItemGroupItem item : items) {
            BigDecimal amount = BigDecimal.valueOf(item.getAmount());

            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            total = total.add(amount.multiply(quantity));
        }

        ((DefaultTableModel) jTableTotal.getModel()).setValueAt(total.doubleValue(), 0, 1);
    }

    private void onSave() {

        String title = titleTextField.getText().trim();

        String description = descriptionTextField.getText().trim();

        if (title.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    MessageBundle.getMessage("angal.newgroupitem.titleerrormessage"),
                    MessageBundle.getMessage("angal.newgroupitem.validationerrortitle"),
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (items.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    MessageBundle.getMessage("angal.newgroupitem.itemerrormessage"),
                    MessageBundle.getMessage("angal.newgroupitem.validationerrortitle"),
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        billItemGroup.setTitle(title);
        billItemGroup.setDescription(description);
        billItemGroup.setTotal(total.doubleValue());

        billItemGroup.setItems(items);

        try {
            if (insert) {
                billItemGroup = manager.addBillItemGroup(billItemGroup);
            } else {
                manager.updateBillItemGroup(billItemGroup);
            }

            confirmed = true;
            dispose();
        } catch (OHServiceException e) {
            MessageDialog.error(this, e.getMessage());
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public BillItemGroup getBillItemGroup() {
        return billItemGroup;
    }

    private class BillTableModel
            extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private final String[] columns = {
                MessageBundle.getMessage("angal.newbill.itemgrouptable.description"),
                MessageBundle.getMessage("angal.newbill.qty"),
                MessageBundle.getMessage("angal.newbill.amount")
        };

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(
                int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(
                int columnIndex) {

            return switch (columnIndex) {
                case 1 -> Integer.class;
                case 2 -> Double.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(
                int rowIndex,
                int columnIndex) {

            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BillItemGroupItem item = items.get(rowIndex);

            return switch (columnIndex) {

                case 0 -> item.getDescription();

                case 1 -> item.getQuantity();

                case 2 -> BigDecimal.valueOf(item.getAmount()).multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue();

                default -> null;
            };
        }
    }
}