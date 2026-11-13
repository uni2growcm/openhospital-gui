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
package org.isf.partner.gui;

import org.isf.partner.manager.PartnerBrowserManager;
import org.isf.partner.model.Partner;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class PartnerBrowser extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartnerBrowser.class);
    private PartnerBrowserManager partnerBrowserManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private static final String[] COLUMNS = {
            "ID",
            MessageBundle.getMessage("angal.partner.code.col"),
            MessageBundle.getMessage("angal.partner.name.col"),
            MessageBundle.getMessage("angal.partner.type.col"),
            MessageBundle.getMessage("angal.partner.contactperson.col"),
            MessageBundle.getMessage("angal.partner.phone.col"),
            MessageBundle.getMessage("angal.partner.priority.col")};

    public PartnerBrowser() {
        this.partnerBrowserManager = Context.getApplicationContext().getBean(PartnerBrowserManager.class);
        initComponents();
        loadPartners(null);
        setTitle(MessageBundle.getMessage("angal.partner.browser.title"));
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public PartnerBrowser(Frame owner, PartnerBrowserManager manager) {
        this.partnerBrowserManager = manager;
        initComponents();
        loadPartners(null);
        setTitle(MessageBundle.getMessage("angal.partner.browser.title"));
        setSize(850, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadPartners(searchField.getText().trim());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadPartners(searchField.getText().trim());
                }
            }
        });

        searchPanel.add(new JLabel(MessageBundle.getMessage("angal.common.search.txt")));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton newBtn = new JButton(MessageBundle.getMessage("angal.partner.new.btn"));
        JButton editBtn = new JButton(MessageBundle.getMessage("angal.partner.edit.btn"));
        JButton deleteBtn = new JButton(MessageBundle.getMessage("angal.partner.delete.btn"));
        JButton closeBtn = new JButton(MessageBundle.getMessage("angal.partner.close.btn"));
        JButton upBtn = new JButton(MessageBundle.getMessage("angal.partner.priorityup.btn"));
        JButton downBtn = new JButton(MessageBundle.getMessage("angal.partner.prioritydown.btn"));
        upBtn.setToolTipText(MessageBundle.getMessage("angal.partner.priority.tooltip"));
        downBtn.setToolTipText(MessageBundle.getMessage("angal.partner.priority.tooltip"));
        upBtn.addActionListener(e -> moveSelected(-1));
        downBtn.addActionListener(e -> moveSelected(1));

        newBtn.setMnemonic(MessageBundle.getMnemonic("angal.partner.new.btn.key"));
        editBtn.setMnemonic(MessageBundle.getMnemonic("angal.partner.edit.btn.key"));
        deleteBtn.setMnemonic(MessageBundle.getMnemonic("angal.partner.delete.btn.key"));
        closeBtn.setMnemonic(MessageBundle.getMnemonic("angal.partner.close.btn.key"));

        newBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            Partner selected = getSelectedPartner();
            if (selected != null) openEditor(selected);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(newBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(upBtn);
        btnPanel.add(downBtn);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void loadPartners(String filter) {
        tableModel.setRowCount(0);
        try {
            List<Partner> partners;
            if (filter == null || filter.isEmpty()) {
                partners = partnerBrowserManager.getPartnersByPriority();
            } else {
                partners = new ArrayList<>(partnerBrowserManager.searchPartners(filter));
                partners.sort(Comparator.comparing(Partner::getPriority, Comparator.nullsLast(Comparator.naturalOrder())));
            }

            for (Partner p : partners) {
                String typeDesc = p.getType() != null ? p.getType().getDescription() : "";
                tableModel.addRow(new Object[]{
                        p.getCode(),
                        p.getCode(),
                        p.getName(),
                        typeDesc,
                        p.getContactPerson(),
                        p.getPhone(),
                        p.getPriority()
                });
            }
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.partner.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Partner getSelectedPartner() {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return null;
        }
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            return partnerBrowserManager.getPartner(id);
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.partner.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void openEditor(Partner partner) {
        PartnerEdit editor = new PartnerEdit(this, partnerBrowserManager, partner, savedPartner -> {
            loadPartners(searchField.getText().trim());
        });
        editor.setVisible(true);
    }

    /**
     * Moves the selected partner up (more prioritary) or down (less prioritary) in the priority order.
     */
    private void moveSelected(int offset) {
        Partner selected = getSelectedPartner();
        if (selected == null) {
            return;
        }
        try {
            List<Partner> ordered = new ArrayList<>(partnerBrowserManager.getPartnersByPriority());
            int index = ordered.indexOf(selected);
            int target = index + offset;
            if (index < 0 || target < 0 || target >= ordered.size()) {
                return;
            }
            ordered.add(target, ordered.remove(index));
            partnerBrowserManager.updatePriorities(ordered);
            loadPartners(searchField.getText().trim());
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                if (selected.getCode().equals(tableModel.getValueAt(row, 0))) {
                    table.setRowSelectionInterval(row, row);
                    break;
                }
            }
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.partner.save.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void deleteSelected() {
        Partner selected = getSelectedPartner();
        if (selected == null) return;

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.partner.delete.confirm.msg", selected.getName()),
                MessageBundle.getMessage("angal.common.delete"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                partnerBrowserManager.deletePartner(selected.getCode());
                loadPartners(searchField.getText().trim());
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.partner.delete.error.msg"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}