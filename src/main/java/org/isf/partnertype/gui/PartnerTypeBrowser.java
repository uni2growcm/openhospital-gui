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
package org.isf.partnertype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.partnertype.gui.PartnerTypeEdit.PartnerTypeListener;
import org.isf.partnertype.manager.PartnerTypeBrowserManager;
import org.isf.partnertype.model.PartnerType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class PartnerTypeBrowser extends ModalJFrame implements PartnerTypeListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void partnerTypeInserted(AWTEvent e) {
		reloadData();
	}

	@Override
	public void partnerTypeUpdated(AWTEvent e) {
		reloadData();
	}

	private void reloadData() {
		try {
			partnerTypeArray = partnerTypeManager.getPartnerTypes();
		} catch (OHServiceException ex) {
			OHServiceExceptionUtil.showMessages(ex);
		}
		jTablePartnerTypes.setModel(new PartnerTypeBrowserModel());
	}

	private JTable jTablePartnerTypes;
	private JScrollPane jScrollPaneTable;
	private JButton jButtonNew;
	private JPanel jPanelButtons;
	private JButton jButtonEdit;
	private JButton jButtonDelete;
	private JButton jButtonClose;

	private final String[] columnNames = {
		MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
	};
	private final int[] columnWidth = { 100, 300 };

	private PartnerTypeBrowserManager partnerTypeManager = Context.getApplicationContext().getBean(PartnerTypeBrowserManager.class);

	private List<PartnerType> partnerTypeArray;
	private JFrame myFrame;

	public PartnerTypeBrowser() {
		myFrame = this;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJScrollPaneTable(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.partnertype.title"));
	}

	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(getJTablePartnerTypes());
		}
		return jScrollPaneTable;
	}

	private JTable getJTablePartnerTypes() {
		if (jTablePartnerTypes == null) {
			jTablePartnerTypes = new JTable();
			jTablePartnerTypes.setModel(new PartnerTypeBrowserModel());

			for (int i = 0; i < columnWidth.length; i++) {
				jTablePartnerTypes.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
			}
			jTablePartnerTypes.setAutoCreateColumnsFromModel(false);
		}
		return jTablePartnerTypes;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonNew());
			jPanelButtons.add(getJButtonEdit());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				PartnerTypeEdit editDialog = new PartnerTypeEdit(myFrame, new PartnerType(), true);
				editDialog.addPartnerTypeListener(this);
				editDialog.setVisible(true);
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				int selectedRow = jTablePartnerTypes.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(this, "angal.partnertype.pleaseselectatypetoedit.msg");
					return;
				}
				PartnerType partnerType = partnerTypeArray.get(selectedRow);
				PartnerTypeEdit editDialog = new PartnerTypeEdit(myFrame, partnerType, false);
				editDialog.addPartnerTypeListener(this);
				editDialog.setVisible(true);
			});
		}
		return jButtonEdit;
	}

	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				int selectedRow = jTablePartnerTypes.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(this, "angal.partnertype.pleaseselectatypetodelete.msg");
					return;
				}
				PartnerType partnerType = partnerTypeArray.get(selectedRow);
				int answer = MessageDialog.yesNo(this, "angal.partnertype.deletepartnertype.msg");
				if (answer == JOptionPane.OK_OPTION) {
					try {
						partnerTypeManager.deletePartnerType(partnerType);
						reloadData();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
				}
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> dispose());
		}
		return jButtonClose;
	}

	class PartnerTypeBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		PartnerTypeBrowserModel() {
			try {
				partnerTypeArray = partnerTypeManager.getPartnerTypes();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			return partnerTypeArray == null ? 0 : partnerTypeArray.size();
		}

		@Override
		public String getColumnName(int c) {
			return columnNames[c];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			PartnerType partnerType = partnerTypeArray.get(r);
			return switch (c) {
				case 0 -> partnerType.getCode();
				case 1 -> partnerType.getDescription();
				default -> null;
			};
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
