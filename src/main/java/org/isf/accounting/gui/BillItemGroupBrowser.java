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

import static org.isf.utils.jobjects.MessageDialog.warning;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.BillItemGroup;
import org.isf.accounting.model.BillItemGroupItem;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Browser for managing Bill Item Groups with CRUD operations
 * 
 * @author Hema
 */
public class BillItemGroupBrowser extends JDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(BillItemGroupBrowser.class);
	private static final ImageIcon ICON = new ImageIcon("rsc/icons/oh.png");

	private final BillBrowserManager manager = Context.getApplicationContext().getBean(BillBrowserManager.class);
	private final boolean enableGroupSelection;
	private final List<BillItemGroupListener> listeners = new ArrayList<>();

	private List<BillItemGroup> groupList;
	private BillItemGroupTableModel model;
	private JTable table;
	private JButton newButton;
	private JButton editButton;
	private JButton deleteButton;
	private JButton applyButton;
	private JButton closeButton;

	/**
	 * Constructor with optional group selection capability
	 */
	public BillItemGroupBrowser(JDialog owner, boolean enableGroupSelection) {
        super(owner, true);
		this.enableGroupSelection = enableGroupSelection;
		this.setTitle(MessageBundle.getMessage("angal.newbill.itemgroupbrowser.title"));
		this.setIconImage(ICON.getImage());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		initComponent();
		this.setLocationRelativeTo(null);
	}

	/**
	 * Default constructor for browse-only mode
	 */
	public BillItemGroupBrowser() {
        this((JDialog) null, false);
	}

	/**
	 * Initialize UI components
	 */
	private void initComponent() {
		try {
			groupList = new ArrayList<>(manager.getAllBillItemGroups());
		} catch (Exception e) {
			groupList = new ArrayList<>();
			warning(this, "angal.newbill.itemgroupbrowser.loaderror");
		}

		model = new BillItemGroupTableModel(groupList);
		table = new JTable(model);
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateColumnsFromModel(false);
		table.getTableHeader().setReorderingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(table);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(getNewButton());
		buttonPanel.add(getEditButton());
		buttonPanel.add(getApplyButton());
		buttonPanel.add(getDeleteButton());
		buttonPanel.add(getCloseButton());

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.setSize(700, 400);
		this.setResizable(true);
	}

	/**
	 * Create new group button
	 */
	private JButton getNewButton() {
		if (newButton == null) {
			newButton = new JButton(MessageBundle.getMessage("angal.newbill.additemgroup.newgroup"));
			newButton.setMnemonic(KeyEvent.VK_N);
			newButton.addActionListener(e -> createNewGroup());
            newButton.setPreferredSize(new Dimension(140,25));
		}
		return newButton;
	}

	/**
	 * Edit selected group button
	 */
	private JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton(MessageBundle.getMessage("angal.newbill.additemgroup.editgroup"));
			editButton.setMnemonic(KeyEvent.VK_E);
			editButton.addActionListener(e -> editSelectedGroup());
            editButton.setPreferredSize(new Dimension(140,25));
		}
		return editButton;
	}

	/**
	 * Delete selected group button
	 */
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton(MessageBundle.getMessage("angal.newbill.additemgroup.delete"));
			deleteButton.setMnemonic(KeyEvent.VK_D);
			deleteButton.addActionListener(e -> deleteSelectedGroup());
            deleteButton.setPreferredSize(new Dimension(100,25));
		}
		return deleteButton;
	}

	/**
	 * Apply selected group button
	 */
	private JButton getApplyButton() {
		if (applyButton == null) {
			applyButton = new JButton(MessageBundle.getMessage("angal.newbill.additemgroup.apply"));
			applyButton.setMnemonic(KeyEvent.VK_A);
			applyButton.addActionListener(e -> applySelectedGroup());
			applyButton.setEnabled(enableGroupSelection);
            applyButton.setPreferredSize(new Dimension(1,25));
		}
		return applyButton;
	}

	/**
	 * Close button
	 */
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(KeyEvent.VK_C);
			closeButton.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			closeButton.addActionListener(e -> dispose());
		}
		return closeButton;
	}

	/**
	 * Create new bill item group
	 */
	private void createNewGroup() {
		try {
			BillItemGroup newGroup = new BillItemGroup();
			BillItemGroupEdit editor = new BillItemGroupEdit(this, newGroup, true);
			editor.setVisible(true);

			if (editor.isConfirmed() && editor.getBillItemGroup().getId() > 0) {
				groupList.add(editor.getBillItemGroup());
				model.fireTableDataChanged();
			}
		} catch (Exception ex) {
			MessageDialog.error(this, ex.getMessage());

		}
	}

	/**
	 * Edit selected bill item group
	 */
	private void editSelectedGroup() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			warning(this, "angal.newbill.additemgroup.selectgroup");
			return;
		}

		try {
			BillItemGroup selected = model.getElementAt(selectedRow);
			BillItemGroupEdit editor = new BillItemGroupEdit(this, selected, false);
			editor.setVisible(true);

			if (editor.isConfirmed()) {
				model.fireTableRowsUpdated(selectedRow, selectedRow);
			}
		} catch (Exception ex) {
			MessageDialog.error(this, ex.getMessage());
		}
	}

	/**
	 * Delete selected bill item group
	 */
	private void deleteSelectedGroup() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			warning(this, "angal.newbill.additemgroup.selectgroup");
			return;
		}

		BillItemGroup selected = model.getElementAt(selectedRow);
		int confirm = JOptionPane.showConfirmDialog(this,
				MessageBundle.formatMessage("angal.newbill.additemgroup.deleteconfirm.fmt.msg", selected.getTitle()),
				MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
				JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

        try {
            manager.deleteBillItemGroup(selected.getId());

            groupList.remove(selectedRow);
            model.fireTableRowsDeleted(selectedRow, selectedRow);

        } catch (OHServiceException ex) {
            MessageDialog.error(this, ex.getMessage());
        }
	}

	/**
	 * Apply selected group (notify listeners)
	 */
	private void applySelectedGroup() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			warning(this, "angal.newbill.additemgroup.selectgroup");
			return;
		}

		try {
			BillItemGroup selected = model.getElementAt(selectedRow);
			List<BillItemGroupItem> items = manager.getItemsByGroupId(selected.getId());
			dispose();
			notifyListeners(items);
		} catch (Exception ex) {
			MessageDialog.error(this, ex.getMessage());
		}
	}

	/**
	 * Notify all listeners of group selection
	 */
	private void notifyListeners(List<BillItemGroupItem> items) {
		listeners.forEach(listener -> listener.groupSelected(items));
	}

	/**
	 * Add listener for group selection events
	 */
	public void addListener(BillItemGroupListener listener) {
		listeners.add(listener);
	}

	/**
	 * Listener interface for group selection events
	 */
	public interface BillItemGroupListener extends EventListener {
		void groupSelected(List<BillItemGroupItem> items);
	}

	/**
	 * Table model for displaying bill item groups
	 */
    public static class BillItemGroupTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private final List<BillItemGroup> data;

        private final String[] columnNames = {
                MessageBundle.getMessage("angal.newbill.itemgrouptable.title"),
                MessageBundle.getMessage("angal.newbill.itemgrouptable.description"),
                MessageBundle.getMessage("angal.newbill.itemgrouptable.total")
        };

        public BillItemGroupTableModel(List<BillItemGroup> data) {
            this.data = data;
        }

        public BillItemGroup getElementAt(int rowIndex) {

            if (rowIndex < 0 || rowIndex >= data.size()) {
                return null;
            }

            return data.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {

            return switch (columnIndex) {
                case 2 -> Double.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            BillItemGroup group = getElementAt(rowIndex);

            if (group == null) {
                return "";
            }

            return switch (columnIndex) {

                case 0 -> group.getTitle();

                case 1 -> group.getDescription();

                case 2 -> group.getTotal();

                default -> "";
            };
        }
    }
}
