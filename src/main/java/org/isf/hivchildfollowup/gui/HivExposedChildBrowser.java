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
package org.isf.hivchildfollowup.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.hivchildfollowup.manager.HivExposedChildBrowserManager;
import org.isf.hivchildfollowup.model.HivExposedChild;
import org.isf.hivchildfollowup.model.HivExposedChildStatus;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportHivExposedChild;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.springframework.data.domain.Page;

/**
 * Main entry point of the HIV-exposed child follow-up module (PTME): lists every child enrolled (most
 * recent first, with search/filter/pagination) and gives access to a new enrollment and to the printable
 * register.
 */
public class HivExposedChildBrowser extends ModalJFrame implements SelectionListener {

	private static final long serialVersionUID = 1L;
	private static final int PAGE_SIZE = 20;

	private List<HivExposedChild> children = new ArrayList<>();
	private final String[] columns = {
			MessageBundle.getMessage("angal.hivchildfollowup.mother.txt").toUpperCase(),
			MessageBundle.getMessage("angal.cpn.childname.txt").toUpperCase(),
			MessageBundle.getMessage("angal.hivchildfollowup.dateofbirth.txt").toUpperCase(),
			MessageBundle.getMessage("angal.hivchildfollowup.finalstatus.txt").toUpperCase()
	};
	private final int[] columnWidth = { 220, 160, 100, 140 };

	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JTable jTable;
	private HivExposedChildBrowserModel model;
	private final HivExposedChildBrowserManager hivChildManager = Context.getApplicationContext().getBean(HivExposedChildBrowserManager.class);
	private final JFrame myFrame;

	private JTextField searchField;
	private JComboBox<Object> statusFilterComboBox;
	private GoodDateChooser dateFromChooser;
	private GoodDateChooser dateToChooser;

	private int startIndex;
	private int totalPages = 1;
	private boolean suppressPageComboEvents;
	private JButton previousPageButton;
	private JButton nextPageButton;
	private final JComboBox<Integer> pagesComboBox = new JComboBox<>();
	private final JLabel ofPagesLabel = new JLabel(MessageBundle.formatMessage("angal.common.pages.fmt.txt", 1));

	public HivExposedChildBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.hivchildfollowup.hivexposedchildbrowser.title"));
		setContentPane(getJContainPanel());
		setMinimumSize(new java.awt.Dimension(950, 550));
		pack();
		setLocationRelativeTo(null);
		loadChildren(0);
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getFilterPanel(), BorderLayout.WEST);
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
		}
		return jContainPanel;
	}

	private JPanel getFilterPanel() {
		JPanel filterPanel = new JPanel();
		filterPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.common.filter.btn")));
		filterPanel.add(getFilterContentPanel());
		return filterPanel;
	}

	private JPanel getFilterContentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

		panel.add(getFilterFieldPanel("angal.common.search.txt", getSearchField()));
		panel.add(getFilterFieldPanel("angal.hivchildfollowup.finalstatus.txt", getStatusFilterComboBox()));
		panel.add(getFilterFieldPanel("angal.common.datefrom.label", getDateFromChooser()));
		panel.add(getFilterFieldPanel("angal.common.dateto.label", getDateToChooser()));

		JButton refreshButton = new JButton(MessageBundle.getMessage("angal.common.refresh.btn"));
		refreshButton.setMnemonic(MessageBundle.getMnemonic("angal.common.refresh.btn.key"));
		refreshButton.addActionListener(actionEvent -> loadChildren(0));
		JPanel refreshPanel = new JPanel();
		refreshPanel.add(refreshButton);
		panel.add(refreshPanel);

		return panel;
	}

	private JPanel getFilterFieldPanel(String labelKey, java.awt.Component field) {
		JPanel panel = new JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
		JLabel label = new JLabel(MessageBundle.getMessage(labelKey));
		label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		if (field instanceof javax.swing.JComponent jComponent) {
			jComponent.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		}
		panel.add(label);
		panel.add(field);
		return panel;
	}

	private JTextField getSearchField() {
		searchField = new JTextField(15);
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.hivchildfollowup.mother.txt"), searchField, Show.FOCUS_LOST);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
		suggestion.setForeground(Color.GRAY);
		searchField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loadChildren(0);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		return searchField;
	}

	private JComboBox<Object> getStatusFilterComboBox() {
		statusFilterComboBox = new JComboBox<>();
		statusFilterComboBox.addItem(MessageBundle.getMessage("angal.common.all.txt"));
		for (HivExposedChildStatus status : HivExposedChildStatus.values()) {
			statusFilterComboBox.addItem(status);
		}
		statusFilterComboBox.addActionListener(actionEvent -> loadChildren(0));
		return statusFilterComboBox;
	}

	private GoodDateChooser getDateFromChooser() {
		dateFromChooser = new GoodDateChooser(null, true, true);
		dateFromChooser.addDateChangeListener(event -> loadChildren(0));
		return dateFromChooser;
	}

	private GoodDateChooser getDateToChooser() {
		dateToChooser = new GoodDateChooser(null, true, true);
		dateToChooser.addDateChangeListener(event -> loadChildren(0));
		return dateToChooser;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new BorderLayout());
			jButtonPanel.add(getPaginationPanel(), BorderLayout.NORTH);

			JPanel actionsPanel = new JPanel();
			actionsPanel.add(getJNewButton(), null);
			actionsPanel.add(getJEditButton(), null);
			actionsPanel.add(getJRegisterButton(), null);
			actionsPanel.add(getJCloseButton(), null);
			jButtonPanel.add(actionsPanel, BorderLayout.SOUTH);
		}
		return jButtonPanel;
	}

	private JPanel getPaginationPanel() {
		JPanel panel = new JPanel();
		previousPageButton = new JButton(MessageBundle.getMessage("angal.inventory.arrowprevious.btn"));
		previousPageButton.addActionListener(actionEvent -> {
			int page = startIndex / PAGE_SIZE;
			if (page > 0) {
				loadChildren(page - 1);
			}
		});
		nextPageButton = new JButton(MessageBundle.getMessage("angal.inventory.arrownext.btn"));
		nextPageButton.addActionListener(actionEvent -> {
			int page = startIndex / PAGE_SIZE;
			if (page < totalPages - 1) {
				loadChildren(page + 1);
			}
		});
		pagesComboBox.addItemListener(itemEvent -> {
			if (suppressPageComboEvents || itemEvent.getStateChange() != ItemEvent.SELECTED) {
				return;
			}
			loadChildren((Integer) pagesComboBox.getSelectedItem() - 1);
		});
		panel.add(previousPageButton);
		panel.add(pagesComboBox);
		panel.add(ofPagesLabel);
		panel.add(nextPageButton);
		return panel;
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
				HivExposedChild child = children.get(jTable.getSelectedRow());
				HivExposedChildEdit edit = new HivExposedChildEdit(myFrame, child.getMotherPatient(), child);
				edit.setOnSave(this::refresh);
				edit.setVisible(true);
			}
		});
		return jEditButton;
	}

	private JButton getJRegisterButton() {
		JButton jRegisterButton = new JButton(MessageBundle.getMessage("angal.hivchildfollowup.printregister.btn"));
		jRegisterButton.addActionListener(actionEvent -> {
			GoodDateChooser fromChooser = new GoodDateChooser(LocalDate.now().withDayOfMonth(1), false, false);
			GoodDateChooser toChooser = new GoodDateChooser(LocalDate.now(), false, false);
			JPanel panel = new JPanel();
			panel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			panel.add(fromChooser);
			panel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			panel.add(toChooser);
			int result = JOptionPane.showConfirmDialog(myFrame, panel, MessageBundle.getMessage("angal.hivchildfollowup.printregister.btn"),
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				LocalDateTime dateFrom = fromChooser.getDateStartOfDay();
				LocalDateTime dateTo = toChooser.getDateEndOfDay();
				new GenericReportHivExposedChild(dateFrom, dateTo);
			}
		});
		return jRegisterButton;
	}

	private JButton getJCloseButton() {
		JButton jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		jCloseButton.addActionListener(actionEvent -> dispose());
		return jCloseButton;
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new HivExposedChildBrowserModel();
			jTable = new JTable(model);
			for (int i = 0; i < columnWidth.length; i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
			}
		}
		return jTable;
	}

	private void refresh() {
		loadChildren(startIndex / PAGE_SIZE);
	}

	private void loadChildren(int page) {
		try {
			String search = searchField.getText() == null || searchField.getText().isBlank() ? null : searchField.getText().trim();
			HivExposedChildStatus status = statusFilterComboBox.getSelectedIndex() > 0
					? (HivExposedChildStatus) statusFilterComboBox.getSelectedItem()
					: null;
			LocalDate dateFrom = dateFromChooser.getDate();
			LocalDate dateTo = dateToChooser.getDate();

			Page<HivExposedChild> result = hivChildManager.getFiltered(search, status, dateFrom, dateTo, page, PAGE_SIZE);
			children = new ArrayList<>(result.getContent());
			totalPages = Math.max(result.getTotalPages(), 1);
			startIndex = page * PAGE_SIZE;
		} catch (OHServiceException e) {
			children = new ArrayList<>();
			totalPages = 1;
			startIndex = 0;
			OHServiceExceptionUtil.showMessages(e);
		}
		if (model != null) {
			model.fireTableDataChanged();
		}
		initializePagesCombo();
	}

	private void initializePagesCombo() {
		suppressPageComboEvents = true;
		pagesComboBox.removeAllItems();
		for (int i = 1; i <= totalPages; i++) {
			pagesComboBox.addItem(i);
		}
		pagesComboBox.setSelectedItem(startIndex / PAGE_SIZE + 1);
		if (previousPageButton != null) {
			previousPageButton.setEnabled(startIndex > 0);
		}
		if (nextPageButton != null) {
			nextPageButton.setEnabled(startIndex / PAGE_SIZE < totalPages - 1);
		}
		ofPagesLabel.setText(MessageBundle.formatMessage("angal.common.pages.fmt.txt", totalPages));
		suppressPageComboEvents = false;
	}

	class HivExposedChildBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return children == null ? 0 : children.size();
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
			HivExposedChild child = children.get(r);
			switch (c) {
				case 0:
					return child.getMotherPatient().getFirstName() + ' ' + child.getMotherPatient().getSecondName();
				case 1:
					return child.getChildName();
				case 2:
					return child.getDateOfBirth();
				case 3:
					return child.getFinalStatus();
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
		HivExposedChildEdit edit = new HivExposedChildEdit(myFrame, patient, null);
		edit.setOnSave(this::refresh);
		edit.setVisible(true);
	}
}
