/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.maternity.gui.visits;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.gui.visits.VisitTypeEdit.VisitTypeListener;
import org.isf.maternity.manager.PregnancyVisitTypeBrowserManager;
import org.isf.maternity.model.PregnancyVisitType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * VisitTypeBrowser - list all exam types. Let the user select an exam type to edit
 */
public class VisitTypeBrowser extends ModalJFrame implements VisitTypeListener {

    private static final long serialVersionUID = 1L;

    private final PregnancyVisitTypeBrowserManager visitTypeBrowserManager =
            Context.getApplicationContext().getBean(PregnancyVisitTypeBrowserManager.class);

    private List<PregnancyVisitType> visitTypes;

    private JTable table;
    private VisitTypeBrowserModel model;

    private int selectedRow = -1;
    private PregnancyVisitType visitType;

    private final JFrame parentFrame;

    private final String[] columns = {
            MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
    };

    public VisitTypeBrowser() {
        parentFrame = this;
        initialize();
        loadData();
        setVisible(true);
    }

    private void initialize() {
        setTitle(MessageBundle.getMessage("angal.maternity.visittype.visittypebrowser.title"));
        setLayout(new BorderLayout());
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void loadData() {
        try {
            visitTypes = visitTypeBrowserManager.getVisitTypes();
            model.setData(visitTypes);
        } catch (OHServiceException e) {
            visitTypes = List.of();
            OHServiceExceptionUtil.showMessages(e);
        }
    }

    private JScrollPane buildTablePanel() {
        model = new VisitTypeBrowserModel();
        table = new JTable(model);

        table.getColumnModel().getColumn(0).setMinWidth(80);
        table.getColumnModel().getColumn(1).setMinWidth(200);

        return new JScrollPane(table);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();

        panel.add(createButton("angal.common.new.btn", this::onNew));
        panel.add(createButton("angal.common.edit.btn", this::onEdit));
        panel.add(createButton("angal.common.delete.btn", this::onDelete));
        panel.add(createButton("angal.common.close.btn", e -> dispose()));

        return panel;
    }

    private JButton createButton(String key, java.awt.event.ActionListener action) {
        JButton button = new JButton(MessageBundle.getMessage(key));
        button.setMnemonic(MessageBundle.getMnemonic(key + ".key"));
        button.addActionListener(action);
        return button;
    }

    private void onNew(java.awt.event.ActionEvent e) {
        visitType = new PregnancyVisitType("", "");
        openEditor(true);
    }

    private void onEdit(java.awt.event.ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        selectedRow = row;
        visitType = model.getVisitTypeAt(row);
        openEditor(false);
    }

    private void onDelete(java.awt.event.ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        PregnancyVisitType selected = model.getVisitTypeAt(row);

        int answer = MessageDialog.yesNo(
                this,
                "angal.maternity.visittype.deletevisittype.fmt.msg",
                selected.getDescription()
        );

        if (answer == JOptionPane.YES_OPTION) {
            try {
                visitTypeBrowserManager.deleteVisitType(selected);
                loadData(); // 🔥 always reload from DB
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void openEditor(boolean isNew) {
        VisitTypeEdit editor = new VisitTypeEdit(parentFrame, visitType, isNew);
        editor.addVisitTypeListener(this);
        editor.setVisible(true);
    }

    class VisitTypeBrowserModel extends DefaultTableModel {

        private List<PregnancyVisitType> data = List.of();

        public void setData(List<PregnancyVisitType> data) {
            this.data = data;
            fireTableDataChanged();
        }

        public PregnancyVisitType getVisitTypeAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            PregnancyVisitType vt = data.get(row);
            return switch (col) {
                case 0 -> vt.getCode();
                case 1 -> vt.getDescription();
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    @Override
    public void visitTypeUpdated(AWTEvent e) {
        loadData();
        if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    @Override
    public void visitTypeInserted(AWTEvent e) {
        loadData();
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }
}