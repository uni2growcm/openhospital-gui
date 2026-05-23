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
package org.isf.country.gui;

import org.isf.country.manager.CountryBrowserManager;
import org.isf.country.model.Country;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.table.DefaultTableModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * CountryBrowser - list all countries. Let the user search, add, edit or delete a country
 */
public class CountryBrowser extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryBrowser.class);
    private CountryBrowserManager countryBrowserManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private static final String[] COLUMNS = {
            "ID",
            MessageBundle.getMessage("angal.country.isocode.col"),
            MessageBundle.getMessage("angal.country.name.col"),
            MessageBundle.getMessage("angal.country.phonecode.col")};

    public CountryBrowser() {
        this.countryBrowserManager = Context.getApplicationContext().getBean(CountryBrowserManager.class);
        initComponents();
        loadCountries(null);
        setTitle(MessageBundle.getMessage("angal.country.browser.title"));
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public CountryBrowser(Frame owner, CountryBrowserManager manager) {
        this.countryBrowserManager = manager;
        initComponents();
        loadCountries(null);
        setTitle(MessageBundle.getMessage("angal.country.browser.title"));
        setSize(600, 450);
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
                loadCountries(searchField.getText().trim());
            }

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadCountries(searchField.getText().trim());
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
        JButton newBtn    = new JButton(MessageBundle.getMessage("angal.country.new.btn"));
        JButton editBtn   = new JButton(MessageBundle.getMessage("angal.country.edit.btn"));
        JButton deleteBtn = new JButton(MessageBundle.getMessage("angal.country.delete.btn"));
        JButton closeBtn  = new JButton(MessageBundle.getMessage("angal.country.close.btn"));

        newBtn.setMnemonic(MessageBundle.getMnemonic("angal.country.new.btn.key"));
        editBtn.setMnemonic(MessageBundle.getMnemonic("angal.country.edit.btn.key"));
        deleteBtn.setMnemonic(MessageBundle.getMnemonic("angal.country.delete.btn.key"));
        closeBtn.setMnemonic(MessageBundle.getMnemonic("angal.country.close.btn.key"));

        newBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            Country selected = getSelectedCountry();
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

    private void loadCountries(String filter) {
        tableModel.setRowCount(0);
        try {
            List<Country> countries;
            if (filter == null || filter.isEmpty()) {
                countries = countryBrowserManager.getCountries();
            } else {

                countries = countryBrowserManager.searchCountries(filter);
            }

            for (Country c : countries) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getIsoCode(),
                        c.getName(),
                        c.getPhoneCode()
                });
            }
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.country.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Country getSelectedCountry() {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return null;
        }
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            return countryBrowserManager.getCountry(id);
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.country.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void openEditor(Country country) {
        CountryEdit editor = new CountryEdit(this, countryBrowserManager, country, savedCountry ->{
            loadCountries(searchField.getText().trim());
        });
        editor.setVisible(true);

    }

    private void deleteSelected() {
        Country selected = getSelectedCountry();
        if (selected == null) return;
        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.country.delete.confirm.msg", selected.getName()),
                MessageBundle.getMessage("angal.common.delete"));
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                countryBrowserManager.deleteCountry(selected.getId());
                loadCountries(searchField.getText().trim());
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.country.save.error.msg"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}