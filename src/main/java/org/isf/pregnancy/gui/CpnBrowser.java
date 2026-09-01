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
package org.isf.pregnancy.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.pregnancy.manager.PregnancyBrowserManager;
import org.isf.pregnancy.model.Pregnancy;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Main entry point of the CPN (Consultation Prénatale) module: lists every ongoing pregnancy and opens
 * {@link CpnEdit} to register a CPN visit for an existing pregnancy or to start a new one.
 */
public class CpnBrowser extends ModalJFrame implements SelectionListener {

	private static final long serialVersionUID = 1L;

	private List<Pregnancy> pregnancies;
	private final String[] columns = {
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.cpn.lmp.txt").toUpperCase(),
			MessageBundle.getMessage("angal.cpn.scheduleddelivery.txt").toUpperCase(),
			MessageBundle.getMessage("angal.cpn.npregnancies.txt").toUpperCase()
	};
	private final int[] columnWidth = { 220, 100, 100, 60 };

	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JTable jTable;
	private CpnBrowserModel model;
	private final PregnancyBrowserManager pregnancyManager = Context.getApplicationContext().getBean(PregnancyBrowserManager.class);
	private final JFrame myFrame;

	public CpnBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.cpn.cpnbrowser.title"));
		setContentPane(getJContainPanel());
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
		}
		return jContainPanel;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJNewButton(), null);
			jButtonPanel.add(getJEditButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		JButton jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		jNewButton.addActionListener(actionEvent -> {
			SelectPatient sp = new SelectPatient(myFrame, new Patient());
			sp.addSelectionListener(this);
			sp.pack();
			sp.setVisible(true);
		});
		return jNewButton;
	}

	private JButton getJEditButton() {
		JButton jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		jEditButton.addActionListener(actionEvent -> {
			if (jTable.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				Pregnancy pregnancy = (Pregnancy) model.getValueAt(jTable.getSelectedRow(), -1);
				new CpnEdit(myFrame, pregnancy.getPatient(), pregnancy).setVisible(true);
				refresh();
			}
		});
		return jEditButton;
	}

	private JButton getJCloseButton() {
		JButton jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		jCloseButton.addActionListener(actionEvent -> dispose());
		return jCloseButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new CpnBrowserModel();
			jTable = new JTable(model);
			for (int i = 0; i < columnWidth.length; i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
			}
		}
		return jTable;
	}

	private void refresh() {
		model = new CpnBrowserModel();
		jTable.setModel(model);
		jTable.updateUI();
	}

	class CpnBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		CpnBrowserModel() {
			try {
				pregnancies = pregnancyManager.getActivePregnancies();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			return pregnancies == null ? 0 : pregnancies.size();
		}

		@Override
		public String getColumnName(int c) {
			return columns[c];
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Pregnancy pregnancy = pregnancies.get(r);
			switch (c) {
				case -1:
					return pregnancy;
				case 0:
					return pregnancy.getPatient().getFirstName() + ' ' + pregnancy.getPatient().getSecondName();
				case 1:
					return pregnancy.getLmp();
				case 2:
					return pregnancy.getScheduledDelivery();
				case 3:
					return pregnancy.getnPregnancies();
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	@Override
	public void patientSelected(Patient patient) {
		new CpnEdit(myFrame, patient, null).setVisible(true);
		refresh();
	}
}
