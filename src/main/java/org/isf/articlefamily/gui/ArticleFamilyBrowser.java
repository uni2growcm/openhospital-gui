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
package org.isf.articlefamily.gui;

import org.isf.articlefamily.manager.ArticleFamilyBrowserManager;
import org.isf.articlefamily.model.ArticleFamily;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ArticleFamilyBrowser extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleFamilyBrowser.class);
    private ArticleFamilyBrowserManager articleFamilyBrowserManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private static final String[] COLUMNS = {
            "ID",
            MessageBundle.getMessage("angal.articlefamily.code.col"),
            MessageBundle.getMessage("angal.articlefamily.description.col"),
    };

    public ArticleFamilyBrowser() {
        this.articleFamilyBrowserManager = Context.getApplicationContext().getBean(ArticleFamilyBrowserManager.class);
        initComponents();
        loadArticleFamilies(null);
        setTitle(MessageBundle.getMessage("angal.articlefamily.browser.title"));
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public ArticleFamilyBrowser(Frame owner, ArticleFamilyBrowserManager manager) {
        this.articleFamilyBrowserManager = manager;
        initComponents();
        loadArticleFamilies(null);
        setTitle(MessageBundle.getMessage("angal.articlefamily.browser.title"));
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(25);
        searchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                loadArticleFamilies(searchField.getText().trim());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadArticleFamilies(searchField.getText().trim());
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
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton newBtn = new JButton(MessageBundle.getMessage("angal.articlefamily.new.btn"));
        JButton editBtn = new JButton(MessageBundle.getMessage("angal.articlefamily.edit.btn"));
        JButton deleteBtn = new JButton(MessageBundle.getMessage("angal.articlefamily.delete.btn"));
        JButton closeBtn = new JButton(MessageBundle.getMessage("angal.articlefamily.close.btn"));

        newBtn.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.new.btn.key"));
        editBtn.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.edit.btn.key"));
        deleteBtn.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.delete.btn.key"));
        closeBtn.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.close.btn.key"));

        newBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            ArticleFamily selected = getSelectedArticleFamily();
            if (selected != null) {
                openEditor(selected);
            }
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

    private void loadArticleFamilies(String filter) {
        tableModel.setRowCount(0);
        try {
            List<ArticleFamily> families;
            if (filter == null || filter.isEmpty()) {
                families = articleFamilyBrowserManager.getArticleFamilies();
            } else {
                families = articleFamilyBrowserManager.searchArticleFamilies(filter);
            }

            for (ArticleFamily af : families) {
                tableModel.addRow(new Object[]{
                        af.getId(),
                        af.getCode(),
                        af.getDescription(),
                });
            }
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.articlefamily.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ArticleFamily getSelectedArticleFamily() {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
            return null;
        }
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            return articleFamilyBrowserManager.getArticleFamily(id);
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.articlefamily.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void openEditor(ArticleFamily articleFamily) {
        ArticleFamilyEdit editor = new ArticleFamilyEdit(
                this,
                articleFamilyBrowserManager,
                articleFamily,
                savedFamily -> {
                    loadArticleFamilies(searchField.getText().trim());
                }
        );
        editor.setVisible(true);
    }

    private void deleteSelected() {
        ArticleFamily selected = getSelectedArticleFamily();
        if (selected == null) {
            return;
        }

        int confirm = MessageDialog.yesNo(
                this,
                MessageBundle.formatMessage("angal.articlefamily.delete.confirm.msg", selected.getCode() + " - " + selected.getDescription()),
                MessageBundle.getMessage("angal.common.delete")
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                articleFamilyBrowserManager.deleteArticleFamily(selected.getId());
                loadArticleFamilies(searchField.getText().trim());
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.articlefamily.delete.error.msg"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}