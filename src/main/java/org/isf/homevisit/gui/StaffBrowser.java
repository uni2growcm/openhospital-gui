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
package org.isf.homevisit.gui;

import org.isf.homevisit.manager.StaffBrowserManager;
import org.isf.homevisit.model.Staff;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class StaffBrowser extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaffBrowser.class);
    private StaffBrowserManager staffBrowserManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private static final String[] COLUMNS = {
            "ID",
            MessageBundle.getMessage("angal.staff.code.col"),
            MessageBundle.getMessage("angal.staff.firstname.col"),
            MessageBundle.getMessage("angal.staff.lastname.col"),
            MessageBundle.getMessage("angal.staff.profession.col"),
            MessageBundle.getMessage("angal.staff.phone.col")
    };

    public StaffBrowser() {
        this.staffBrowserManager = Context.getApplicationContext().getBean(StaffBrowserManager.class);
        initComponents();
        loadStaff(null);
        setTitle(MessageBundle.getMessage("angal.staff.browser.title"));
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public StaffBrowser(Frame owner) {
        this.staffBrowserManager = Context.getApplicationContext().getBean(StaffBrowserManager.class);
        initComponents();
        loadStaff(null);
        setTitle(MessageBundle.getMessage("angal.staff.browser.title"));
        setSize(800, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadStaff(searchField.getText().trim());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadStaff(searchField.getText().trim());
                }
            }
        });

        searchPanel.add(new JLabel(MessageBundle.getMessage("angal.common.search.txt")));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table
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

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton newBtn = new JButton(MessageBundle.getMessage("angal.staff.new.btn"));
        JButton editBtn = new JButton(MessageBundle.getMessage("angal.staff.edit.btn"));
        JButton deleteBtn = new JButton(MessageBundle.getMessage("angal.staff.delete.btn"));
        JButton closeBtn = new JButton(MessageBundle.getMessage("angal.staff.close.btn"));

        newBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            Staff selected = getSelectedStaff();
            if (selected != null) openEditor(selected);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(newBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void loadStaff(String filter) {
        tableModel.setRowCount(0);
        try {
            List<Staff> staffList;
            if (filter == null || filter.isEmpty()) {
                staffList = staffBrowserManager.getStaff();
            } else {
                staffList = staffBrowserManager.searchStaff(filter);
            }

            for (Staff s : staffList) {
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getCode(),
                        s.getFirstName(),
                        s.getLastName(),
                        s.getProfession(),
                        s.getPhone()
                });
            }
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.staff.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Staff getSelectedStaff() {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return null;
        }
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            return staffBrowserManager.getStaff(id);
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.staff.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void openEditor(Staff staff) {
        StaffEdit editor = new StaffEdit(this, staffBrowserManager, staff, savedStaff -> {
            loadStaff(searchField.getText().trim());
        });
        editor.setVisible(true);
    }

    private void deleteSelected() {
        Staff selected = getSelectedStaff();
        if (selected == null) return;

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.staff.delete.confirm.msg",
                        selected.getFirstName(), selected.getLastName()),
                MessageBundle.getMessage("angal.common.delete"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                staffBrowserManager.deleteStaff(selected.getId());
                loadStaff(searchField.getText().trim());
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.staff.delete.error.msg"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}