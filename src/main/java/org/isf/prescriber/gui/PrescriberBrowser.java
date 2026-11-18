/*
 * Open Hospital (www.open-hospital.org)
 * Copyright Â© 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.prescriber.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.prescriber.manager.PrescriberBrowserManager;
import org.isf.prescriber.model.Prescriber;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class PrescriberBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private final PrescriberBrowserManager manager = Context.getApplicationContext().getBean(PrescriberBrowserManager.class);

	private final String[] columns = {
			MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
			MessageBundle.getMessage("angal.prescriber.name.col").toUpperCase()
	};
	private List<Prescriber> prescribers = new ArrayList<>();
	private DefaultTableModel model;
	private JTable table;
	private JTextField searchField;

	public PrescriberBrowser() {
		setTitle(MessageBundle.getMessage("angal.prescriber.browser.title"));
		setMinimumSize(new Dimension(600, 400));
		setContentPane(getJContentPane());
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		loadPrescribers("");
		setVisible(true);
	}

	private JPanel getJContentPane() {
		JPanel content = new JPanel(new BorderLayout());

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		top.add(new JLabel(MessageBundle.getMessage("angal.common.search.txt") + ":"));
		searchField = new JTextField(20);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				loadPrescribers(searchField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				loadPrescribers(searchField.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				loadPrescribers(searchField.getText());
			}
		});
		top.add(searchField);
		content.add(top, BorderLayout.NORTH);

		model = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(model);
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		content.add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JButton newButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		JButton editButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		JButton deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		newButton.addActionListener(e -> createPrescriber());
		editButton.addActionListener(e -> editPrescriber());
		deleteButton.addActionListener(e -> deletePrescriber());
		closeButton.addActionListener(e -> dispose());
		buttons.add(newButton);
		buttons.add(editButton);
		buttons.add(deleteButton);
		buttons.add(closeButton);
		content.add(buttons, BorderLayout.SOUTH);

		return content;
	}

	private void loadPrescribers(String filter) {
		try {
			prescribers = manager.getPrescribers();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			prescribers = new ArrayList<>();
		}
		String lower = filter == null ? "" : filter.trim().toLowerCase();
		model.setRowCount(0);
		for (Prescriber prescriber : prescribers) {
			if (lower.isEmpty() || (prescriber.getName() != null && prescriber.getName().toLowerCase().contains(lower))) {
				model.addRow(new Object[] { prescriber.getId(), prescriber.getName() });
			}
		}
	}

	private Prescriber getSelectedPrescriber() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return null;
		}
		int id = (int) model.getValueAt(row, 0);
		for (Prescriber prescriber : prescribers) {
			if (prescriber.getId() == id) {
				return prescriber;
			}
		}
		return null;
	}

	private void createPrescriber() {
		PrescriberEdit dialog = new PrescriberEdit(this, new Prescriber(), true);
		dialog.setVisible(true);
		if (dialog.isSaved()) {
			loadPrescribers(searchField.getText());
		}
	}

	private void editPrescriber() {
		Prescriber selected = getSelectedPrescriber();
		if (selected == null) {
			MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			return;
		}
		PrescriberEdit dialog = new PrescriberEdit(this, selected, false);
		dialog.setVisible(true);
		if (dialog.isSaved()) {
			loadPrescribers(searchField.getText());
		}
	}

	private void deletePrescriber() {
		Prescriber selected = getSelectedPrescriber();
		if (selected == null) {
			MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			return;
		}
		int answer = MessageDialog.yesNo(this, "angal.prescriber.delete.confirm.msg", selected.getName());
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}
		try {
			manager.deletePrescriber(selected.getId());
			loadPrescribers(searchField.getText());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
