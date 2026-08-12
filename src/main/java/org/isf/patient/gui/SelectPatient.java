/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.patient.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.BillBrowser;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsertExtended.PatientListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.PaginationPanel;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.pagination.PageInfo;
import org.isf.utils.pagination.PagedResponse;

public class SelectPatient extends JDialog implements PatientListener {

//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList selectionListener = new EventListenerList();

	public interface SelectionListener extends EventListener {

		void patientSelected(Patient patient);
	}

	public void addSelectionListener(SelectionListener l) {
		selectionListener.add(SelectionListener.class, l);
	}

	private void fireSelectedPatient(Patient patient) {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = selectionListener.getListeners(SelectionListener.class);
		for (EventListener listener : listeners) {
			((SelectionListener) listener).patientSelected(patient);
		}
	}

//---------------------------------------------------------------------------	
	private static final long serialVersionUID = 1L;
	private JPanel jPanelButtons;
	private JPanel jPanelTop;
	private JPanel jPanelCenter;
	private JTable jTablePatient;
	private JScrollPane jScrollPaneTablePatient;
	private JButton jButtonCancel;
	private JButton jButtonSelect;
	private JLabel jLabelSearch;
	private JTextField jTextFieldSearchPatient;
	private JButton jSearchButton;
	private PaginationPanel paginationPanel;
	private JPanel jPanelDataPatient;
	private Patient patient;
	private int currentPage;
	private PageInfo lastPageInfo;

	public Patient getPatient() {
		return patient;
	}

	private JButton buttonNew;
	private PatientSummary ps;
	private String[] patColumns = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase() };
	private int[] patColumnsWidth = { 100, 250 };
	private boolean[] patColumnsResizable = { false, true };

	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	List<Patient> patArray = new ArrayList<>();
	List<Patient> patSearch = new ArrayList<>();
	private Timer searchTimer = new Timer(1000, e -> searchPatients(0));

	public SelectPatient(JFrame owner, Patient pat) {
		super(owner, true);
		patient = pat;
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		searchPatients(0);
	}

	public SelectPatient(JDialog owner, Patient pat) {
		super(owner, true);
		patient = pat;
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		searchPatients(0);
	}

	public SelectPatient(JDialog owner, String search) {
		super(owner, true);
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		jTextFieldSearchPatient.setText(search);
		searchPatients(0);
	}

	/**
	 * @param full unused since patient search is always paginated - kept for caller compatibility.
	 */
	public SelectPatient(JFrame owner, boolean abbleAddPatient, boolean full) {
		super(owner, true);
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		buttonNew.setVisible(abbleAddPatient);
		searchPatients(0);
	}

	/**
	 * @param full unused since patient search is always paginated - kept for caller compatibility.
	 */
	public SelectPatient(JDialog owner, boolean abbleAddPatient, boolean full) {
		super(owner, true);
		ps = new PatientSummary(patient);
		initComponents();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			}
		});
		setLocationRelativeTo(null);
		buttonNew.setVisible(abbleAddPatient);
		searchPatients(0);
	}

	private void initComponents() {
		add(getJPanelTop(), BorderLayout.NORTH);
		add(getJPanelCenter(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.patient.patientselection.title"));
		pack();
	}

	private JPanel getJPanelDataPatient() {
		if (jPanelDataPatient == null) {
			jPanelDataPatient = ps.getPatientCompleteSummary();
			jPanelDataPatient.setAlignmentY(Component.TOP_ALIGNMENT);
		}
		return jPanelDataPatient;
	}

	private JTextField getJTextFieldSearchPatient() {
		if (jTextFieldSearchPatient == null) {
			jTextFieldSearchPatient = new VoLimitedTextField(100, 20);
			jTextFieldSearchPatient.setText("");
			jTextFieldSearchPatient.selectAll();
			jTextFieldSearchPatient.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						searchPatients(0);
					} else {
						searchTimer.setRepeats(false);
						searchTimer.start();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
		}
		return jTextFieldSearchPatient;
	}

	/**
	 * Renders the already server-filtered, already paginated {@code patArray} as {@code patSearch},
	 * auto-selecting the patient when exactly one result comes back.
	 */
	private void filterPatient() {
		patSearch = patArray;

		if (jTablePatient.getRowCount() == 0) {

			patient = null;
			updatePatientSummary();
		}
		if (jTablePatient.getRowCount() == 1) {

			Patient selectedPatient = (Patient) jTablePatient.getValueAt(0, -1);
			patient = reloadSelectedPatient(selectedPatient.getCode());
			updatePatientSummary();
		}
		jTablePatient.updateUI();
		jTextFieldSearchPatient.requestFocus();
	}

	private JLabel getJLabelSearch() {
		if (jLabelSearch == null) {
			jLabelSearch = new JLabel(MessageBundle.getMessage("angal.patient.searchpatient"));
		}
		return jLabelSearch;
	}

	private JButton getJButtonSelect() {
		if (jButtonSelect == null) {
			jButtonSelect = new JButton(MessageBundle.getMessage("angal.common.select.btn"));
			jButtonSelect.setMnemonic(MessageBundle.getMnemonic("angal.common.select.btn.key"));
			jButtonSelect.addActionListener(actionEvent -> {

				if (patient != null) {
					// to free memory
					patArray.clear();
					patSearch.clear();
					dispose();
					fireSelectedPatient(patient);
				}
			});
		}
		return jButtonSelect;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> {
				// to free memory
				patArray.clear();
				patSearch.clear();
				dispose();
			});
		}
		return jButtonCancel;
	}

	private JScrollPane getJScrollPaneTablePatient() {
		if (jScrollPaneTablePatient == null) {
			jScrollPaneTablePatient = new JScrollPane();
			jScrollPaneTablePatient.setViewportView(getJTablePatient());
			jScrollPaneTablePatient.setAlignmentY(Component.TOP_ALIGNMENT);
		}
		return jScrollPaneTablePatient;
	}

	private JTable getJTablePatient() {
		if (jTablePatient == null) {
			jTablePatient = new JTable();
			jTablePatient.setModel(new SelectPatientModel());
			jTablePatient.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0; i < patColumns.length; i++) {
				jTablePatient.getColumnModel().getColumn(i).setMinWidth(patColumnsWidth[i]);
				if (!patColumnsResizable[i]) {
					jTablePatient.getColumnModel().getColumn(i).setMaxWidth(patColumnsWidth[i]);
				}
			}
			jTablePatient.setAutoCreateColumnsFromModel(false);
			jTablePatient.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

			ListSelectionModel listSelectionModel = jTablePatient.getSelectionModel();
			listSelectionModel.addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					int index = jTablePatient.getSelectedRow();
					Patient selectedPatient = (Patient) jTablePatient.getValueAt(index, -1);
					patient = reloadSelectedPatient(selectedPatient.getCode());
					updatePatientSummary();
				}
			});

			jTablePatient.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						e.consume();
						jButtonSelect.doClick();
					}
				}
			});
		}
		return jTablePatient;
	}

	private Patient reloadSelectedPatient(Integer code) {
		try {
			return patientBrowserManager.getPatientById(code);
		} catch (OHServiceException ex) {
			throw new RuntimeException("Unable to load patient");
		}
	}

	private void updatePatientSummary() {
		jPanelCenter.remove(jPanelDataPatient);
		ps = new PatientSummary(patient);
		jPanelDataPatient = ps.getPatientCompleteSummary();
		jPanelDataPatient.setAlignmentY(Component.TOP_ALIGNMENT);

		jPanelCenter.add(jPanelDataPatient);
		jPanelCenter.validate();
		jPanelCenter.repaint();
	}

	private JPanel getJPanelCenter() {
		if (jPanelCenter == null) {
			jPanelCenter = new JPanel();
			jPanelCenter.setLayout(new BoxLayout(jPanelCenter, BoxLayout.X_AXIS));
			jPanelCenter.add(getJScrollPaneTablePatient());
			jPanelCenter.add(getJPanelDataPatient());

			if (patient != null) {
				for (int i = 0; i < patSearch.size(); i++) {
					if (patSearch.get(i).getCode().equals(patient.getCode())) {
						jTablePatient.addRowSelectionInterval(i, i);
						int j = 0;
						if (i > 10) {
							j = i - 10; // to center the selected row
						}
						jTablePatient.scrollRectToVisible(jTablePatient.getCellRect(j, i, true));
						break;
					}
				}
			}
		}
		return jPanelCenter;
	}

	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			jPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelTop.add(getJLabelSearch());
			jPanelTop.add(getJTextFieldSearchPatient());
			if (MainMenu.checkUserGrants("btnadmnew")) {
				jPanelTop.add(getButtonNew());
			}
			jPanelTop.add(getJSearchButton());
			jPanelTop.add(getPaginationPanel());
		}
		return jPanelTop;
	}

	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			jSearchButton.addActionListener(actionEvent -> searchPatients(0));
		}
		return jSearchButton;
	}

	/**
	 * Triggered by a filter-changing action (search text change, dialog open). Always forces a fresh
	 * total-count query, since the total may have changed for the new search.
	 */
	private void searchPatients(int page) {
		searchPatients(page, null);
	}

	/**
	 * Triggered by {@link PaginationPanel} navigation on an unchanged search. Skips the total-count query
	 * by reusing the total already known from the last fetch.
	 */
	private void navigateToPage(int page) {
		searchPatients(page, lastPageInfo == null ? null : lastPageInfo.getTotalNbOfElements());
	}

	private void searchPatients(int page, Long knownTotalElements) {
		try {
			PagedResponse<Patient> response = patientBrowserManager.getPatientsByOneOfFieldsLike(jTextFieldSearchPatient.getText(), page,
					knownTotalElements);
			patArray = new ArrayList<>(response.getData());
			lastPageInfo = response.getPageInfo();
			currentPage = page;
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
			patArray = new ArrayList<>();
			lastPageInfo = null;
		}
		getPaginationPanel().update(lastPageInfo);
		filterPatient();
	}

	private PaginationPanel getPaginationPanel() {
		if (paginationPanel == null) {
			paginationPanel = new PaginationPanel(this::navigateToPage);
		}
		return paginationPanel;
	}
	private JButton getButtonNew() {
		buttonNew = new JButton(MessageBundle.getMessage("angal.common.newpatient.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.newpatient.btn.key"));
		buttonNew.addActionListener(actionEvent -> {

			if (GeneralData.PATIENTEXTENDED) {
				PatientInsertExtended newrecord = new PatientInsertExtended(this, new Patient(), true);
				newrecord.addPatientListener(this);
				newrecord.setVisible(true);
			} else {
				PatientInsert newrecord = new PatientInsert(this, new Patient(), true);
				newrecord.addPatientListener((PatientInsert.PatientListener) this);
				newrecord.setVisible(true);
			}

		});
		return buttonNew;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonSelect());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	class SelectPatientModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public SelectPatientModel() {
		}

		@Override
		public int getRowCount() {
			if (patSearch == null) {
				return 0;
			}
			return patSearch.size();
		}

		@Override
		public String getColumnName(int c) {
			return patColumns[c];
		}

		@Override
		public int getColumnCount() {
			return patColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Patient patient = patSearch.get(r);
			if (c == -1) {
				return patient;
			} else if (c == 0) {
				return patient.getCode();
			} else if (c == 1) {
				return patient.getName();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			return cell;
		}
	}

	public void setButtonNew(JButton buttonNew) {
		this.buttonNew = buttonNew;
	}

	List<BillBrowser> billBrowserListeners = new ArrayList<>();

	public void addSelectionListener(BillBrowser l) {
		billBrowserListeners.add(l);
	}

	@Override
	public void patientUpdated(AWTEvent e) {
	}

	@Override
	public void patientInserted(AWTEvent e) {
		Patient patient = (Patient) e.getSource();
		patSearch.add(0, patient);
	}
}
