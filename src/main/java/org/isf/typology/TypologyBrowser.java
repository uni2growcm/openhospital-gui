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
package org.isf.typology;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.springframework.data.domain.Page;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TypologyBrowser extends ModalJFrame implements TypologyEdit.TypologyListener {

    private final TypologyBrowserManager manager =
            Context.getApplicationContext().getBean(TypologyBrowserManager.class);

    private JTable table;
    private TypologyTableModel model;

    private JComboBox<Family> familyFilter;
    private JTextField searchField;

    private JButton nextBtn;
    private JButton prevBtn;

    private JLabel pageLabel;
    private JLabel totalLabel;

    private int currentPage = 0;
    private int totalPages = 0;

    private List<Typology> typologies;

    private Typology selectedTypology;
    private int selectedRow = -1;

    private final JFrame parentFrame;

    private final String[] columns = {
        MessageBundle.getMessage("angal.typology.code.col"),
        MessageBundle.getMessage("angal.typology.description.col"),
        MessageBundle.getMessage("angal.typology.family.col")
    };

    public TypologyBrowser() {
        parentFrame = this;
        initialize();
        loadData();
        setVisible(true);
    }

    private void initialize() {
        setTitle(MessageBundle.getMessage("angal.typology.typologybrowser.title"));
        setLayout(new BorderLayout());

        add(getFilterPanel(), BorderLayout.NORTH);
        add(getCenterPanel(), BorderLayout.CENTER);
        add(getActionPanel(), BorderLayout.SOUTH);

        setSize(750, 500);
        setLocationRelativeTo(null);
    }

    private JPanel getFilterPanel() {
        JPanel panel = new JPanel();

        familyFilter = new JComboBox<>(Family.values());
        familyFilter.insertItemAt(null, 0);
        familyFilter.setSelectedIndex(0);

        searchField = new JTextField(20);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });

        familyFilter.addActionListener(e -> resetPageAndLoad());

        panel.add(new JLabel(MessageBundle.getMessage("angal.typology.family.label")));
        panel.add(familyFilter);

        panel.add(new JLabel(MessageBundle.getMessage("angal.typology.search.label")));
        panel.add(searchField);

        return panel;
    }

    private void onSearchChanged() {
        currentPage = 0;
        loadData();
    }

    private JPanel getCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getTablePanel(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getTablePanel() {
        model = new TypologyTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }

    private JPanel getPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        prevBtn = new JButton("<");
        nextBtn = new JButton(">");

        pageLabel = new JLabel();
        totalLabel = new JLabel();

        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });

        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });

        panel.add(prevBtn);
        panel.add(pageLabel);
        panel.add(nextBtn);

        panel.add(Box.createHorizontalStrut(20));
        panel.add(totalLabel);

        return panel;
    }

    private JPanel getActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        panel.add(createButton("angal.common.new.btn", this::onNew));
        panel.add(createButton("angal.common.edit.btn", this::onEdit));
        panel.add(createButton("angal.common.delete.btn", this::onDelete));
        panel.add(createButton("angal.common.close.btn", e -> dispose()));

        return panel;
    }

    private JButton createButton(String key, java.awt.event.ActionListener action) {
        JButton button = new JButton(MessageBundle.getMessage(key));
        button.addActionListener(action);
        return button;
    }

    private void loadData() {
        try {
            Family family = (Family) familyFilter.getSelectedItem();
            String search = searchField.getText() != null ? searchField.getText().trim() : "";

            Page<Typology> page = manager.searchTypologies(search, family, currentPage, GeneralData.PAGINATIONPAGESIZE);

            if (currentPage >= page.getTotalPages() && page.getTotalPages() > 0) {
                currentPage = page.getTotalPages() - 1;
                page = manager.searchTypologies(search, family, currentPage, GeneralData.PAGINATIONPAGESIZE);
            }

            typologies = page.getContent();
            model.setData(typologies);

            totalPages = page.getTotalPages();

            pageLabel.setText(
                MessageBundle.formatMessage(
                    "angal.typology.page.label",
                    totalPages == 0 ? 0 : currentPage + 1,
                    Math.max(totalPages, 1)
                )
            );

            totalLabel.setText(
                MessageBundle.formatMessage(
                    "angal.typology.total.label",
                    page.getTotalElements()
                )
            );

            prevBtn.setEnabled(currentPage > 0);
            nextBtn.setEnabled(currentPage < totalPages - 1);

        } catch (OHServiceException e) {
            typologies = List.of();
            model.setData(typologies);
            OHServiceExceptionUtil.showMessages(e);
        }
    }

    private void resetPageAndLoad() {
        currentPage = 0;
        loadData();
    }

    private void onNew(java.awt.event.ActionEvent e) {
        selectedTypology = new Typology();
        openEditor(true);
    }

    private void onEdit(java.awt.event.ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.typology.selectarow.msg");
            return;
        }

        selectedRow = row;
        selectedTypology = model.getAt(row);
        openEditor(false);
    }

    private void onDelete(java.awt.event.ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.typology.selectarow.msg");
            return;
        }

        Typology t = model.getAt(row);

        int answer = MessageDialog.yesNo(
            this,
            "angal.typology.doyouwanttodelete.msg",
            t.getDescription()
        );

        if (answer == JOptionPane.YES_OPTION) {
            try {
                manager.deleteTypology(t);
                loadData();
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void openEditor(boolean insert) {
        TypologyEdit editor = new TypologyEdit(parentFrame, selectedTypology, insert);
        editor.addTypologyListener(this);
        editor.setVisible(true);
    }

    class TypologyTableModel extends DefaultTableModel {

        private List<Typology> data = List.of();

        public void setData(List<Typology> data) {
            this.data = data;
            fireTableDataChanged();
        }

        public Typology getAt(int row) {
            return data.get(row);
        }

        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int col) {
            Typology t = data.get(row);

            return switch (col) {
                case 0 -> t.getCode();
                case 1 -> t.getDescription();
                case 2 -> t.getFamily();
                default -> null;
            };
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    @Override
    public void typologyUpdated(java.awt.AWTEvent e) {
        loadData();
    }

    @Override
    public void typologyInserted(java.awt.AWTEvent e) {
        loadData();
    }
}