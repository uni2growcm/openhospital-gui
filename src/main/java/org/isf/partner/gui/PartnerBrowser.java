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
package org.isf.partner.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.partner.gui.PartnerEdit.PartnerListener;
import org.isf.partner.manager.PartnerBrowserManager;
import org.isf.partner.model.Partner;
import org.isf.partnertype.gui.PartnerTypeBrowser;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class PartnerBrowser extends ModalJFrame implements PartnerListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void partnerInserted(AWTEvent e) {
		reloadData();
	}

	@Override
	public void partnerUpdated(AWTEvent e) {
		reloadData();
	}

	private void reloadData() {
		try {
			partnerArray = partnerManager.searchPartners(jTextFieldSearch == null ? null : jTextFieldSearch.getText());
		} catch (OHServiceException ex) {
			OHServiceExceptionUtil.showMessages(ex);
		}
		jTablePartners.setModel(new PartnerBrowserModel());
	}

	private JTextField jTextFieldSearch;
	private JTable jTablePartners;
	private JScrollPane jScrollPaneTable;
	private JButton jButtonNew;
	private JPanel jPanelButtons;
	private JPanel jPanelSearch;
	private JButton jButtonEdit;
	private JButton jButtonDelete;
	private JButton jButtonTypes;
	private JButton jButtonClose;

	private final String[] columnNames = {
		MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
		MessageBundle.getMessage("angal.partner.type.label").toUpperCase(),
		MessageBundle.getMessage("angal.partner.contactperson.label").toUpperCase(),
		MessageBundle.getMessage("angal.partner.phone.label").toUpperCase()
	};
	private final int[] columnWidth = { 200, 120, 150, 100 };

	private PartnerBrowserManager partnerManager = Context.getApplicationContext().getBean(PartnerBrowserManager.class);

	private List<Partner> partnerArray;
	private JFrame myFrame;

	public PartnerBrowser() {
		myFrame = this;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelSearch(), BorderLayout.NORTH);
		add(getJScrollPaneTable(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.partner.title"));
	}

	private JPanel getJPanelSearch() {
		if (jPanelSearch == null) {
			jPanelSearch = new JPanel();
			jTextFieldSearch = new JTextField(20);
			jTextFieldSearch.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					reloadData();
				}
			});
			jPanelSearch.add(jTextFieldSearch);
		}
		return jPanelSearch;
	}

	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(getJTablePartners());
		}
		return jScrollPaneTable;
	}

	private JTable getJTablePartners() {
		if (jTablePartners == null) {
			jTablePartners = new JTable();
			jTablePartners.setModel(new PartnerBrowserModel());

			for (int i = 0; i < columnWidth.length; i++) {
				jTablePartners.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
			}
			jTablePartners.setAutoCreateColumnsFromModel(false);
		}
		return jTablePartners;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonNew());
			jPanelButtons.add(getJButtonEdit());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonTypes());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				PartnerEdit editDialog = new PartnerEdit(myFrame, new Partner(), true);
				editDialog.addPartnerListener(this);
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
				int selectedRow = jTablePartners.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(this, "angal.partner.pleaseselectapartnertoedit.msg");
					return;
				}
				Partner partner = partnerArray.get(selectedRow);
				PartnerEdit editDialog = new PartnerEdit(myFrame, partner, false);
				editDialog.addPartnerListener(this);
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
				int selectedRow = jTablePartners.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(this, "angal.partner.pleaseselectapartnertodelete.msg");
					return;
				}
				Partner partner = partnerArray.get(selectedRow);
				int answer = MessageDialog.yesNo(this, "angal.partner.deletepartner.msg");
				if (answer == JOptionPane.OK_OPTION) {
					try {
						partnerManager.deletePartner(partner.getCode());
						reloadData();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
				}
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonTypes() {
		if (jButtonTypes == null) {
			jButtonTypes = new JButton(MessageBundle.getMessage("angal.partner.managetypes.btn"));
			jButtonTypes.addActionListener(actionEvent -> {
				PartnerTypeBrowser typeBrowser = new PartnerTypeBrowser();
				typeBrowser.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(WindowEvent e) {
						reloadData();
					}
				});
				typeBrowser.showAsModal(myFrame);
				typeBrowser.setVisible(true);
			});
		}
		return jButtonTypes;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> dispose());
		}
		return jButtonClose;
	}

	class PartnerBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		PartnerBrowserModel() {
			try {
				partnerArray = partnerManager.searchPartners(jTextFieldSearch == null ? null : jTextFieldSearch.getText());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			return partnerArray == null ? 0 : partnerArray.size();
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
			Partner partner = partnerArray.get(r);
			return switch (c) {
				case 0 -> partner.getName();
				case 1 -> partner.getType();
				case 2 -> partner.getContactPerson();
				case 3 -> partner.getPhone();
				default -> null;
			};
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
