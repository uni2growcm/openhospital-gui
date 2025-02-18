/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

package org.isf.pregnancycare.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.springframework.data.domain.Page;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class PregnancyCareBrowser extends JFrame implements PatientInsert.PatientListener, PatientInsertExtended.PatientListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String[] columnHeaders = {
		MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.address.txt").toUpperCase()
	};

	private final int[] columnWidths = { 20, 200, 20, 150 };

	private final String[] vColumns = {
		MessageBundle.getMessage("angal.pregnancy.pregnancynumber.col").toUpperCase(),
		MessageBundle.getMessage("angal.pregnancy.visitdate.col").toUpperCase(),
		MessageBundle.getMessage("angal.pregnancy.visittype.col").toUpperCase(),
		MessageBundle.getMessage("angal.pregnancy.visitnote.col").toUpperCase()
	};

	private final int[] vColumnWidths = { 20, 40, 40, 220 };

	private final PregnancyCareBrowser myFrame;
	List<Admission> patientList = new ArrayList<>();

	private final AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);

	private JTable patientTable;
	private JTable visitTable;
	private JButton nextButton;
	private JButton prevButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel underLabel;
	private JLabel totalPatientsLabel;
	private JButton jNewPatientButton;
	private JButton jEditPatientButton;
	private JButton jDeletePatientButton;
	private JButton jNewPrenatalVisitButton;
	private JButton jDeleteVisitButton;
	private JButton jCloseButton;
	private JButton jExamsButton;
	private JButton jVaccinButton;
	private JButton jReportButton;
	private JButton jNewPostnatalVisitButton;
	private JButton jNewPregnancyButton;
	private JButton jEditVisitButton;
	private JButton jDeliveryButton;
	private Patient patient;
	private JButton jSearchButton;
	private JTextField searchPatientTextField;
	private DefaultTableModel model;
	private int TOTAL_PAGES = 0;
	private int CURRENT_PAGE = 1;
	private long TOTAL_PATIENTS = 0;
	private final int PAGE_SIZE = 100;

	/**
	 * Constructor called from the main menu
	 */
	public PregnancyCareBrowser() throws OHServiceException {
		setTitle(MessageBundle.getMessage("angal.pregnancy.patientsbrowser.title"));
		myFrame = this;
		initComponents();
		jSearchButton.doClick();
		pack();
		setLocationRelativeTo(null);
		this.setVisible(true);
		myFrame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	/**
	 * constructor for the AdmissionBrowser to see only the pregnancyvisits for the patient
	 * @param admission the admitted patient
	 */
	public PregnancyCareBrowser(Patient admission) throws OHServiceException {
		setTitle(MessageBundle.getMessage("angal.pregnancy.patientsbrowser.title"));
		myFrame = this;
		this.patient = admission;
		initComponents();
		jSearchButton.doClick();
		pack();
		setLocationRelativeTo(null);
		this.setVisible(true);
		myFrame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	/**
	 * intis the components
	 */
	private void initComponents() throws OHServiceException {
		getContentPane().add(getPatientPanel(), BorderLayout.NORTH);
		getContentPane().add(getVisitPanel(), BorderLayout.CENTER);
		getContentPane().add(getPregnancyButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel getPatientPanel() throws OHServiceException {
		JPanel dataPatientListPanel = new JPanel(new BorderLayout());
		dataPatientListPanel.add(getSearchPanel(), BorderLayout.WEST);
		dataPatientListPanel.add(getPatientScrollPanel(), BorderLayout.CENTER);
		dataPatientListPanel.add(getPatientButtonPanel(), BorderLayout.EAST);
		return dataPatientListPanel;
	}

	private JPanel getPatientScrollPanel() throws OHServiceException {
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(getPatientTablePanel(), BorderLayout.CENTER);
		centerPanel.add(getPaginationPanel(), BorderLayout.SOUTH);
		return centerPanel;
	}

	private JPanel getPaginationPanel() throws OHServiceException {
		JPanel paginatePanel = new JPanel(new WrapLayout());
		paginatePanel.add(getPrevButton());
		paginatePanel.add(getPagesCombo());
		paginatePanel.add(getUnderLabel());
		paginatePanel.add(getNextButton());
		paginatePanel.add(getTotalPatientsLabelsLabel());
		return paginatePanel;
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton(">");
			nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES != 1);
			nextButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE < TOTAL_PAGES) {
					CURRENT_PAGE++;
					pagesCombo.setSelectedItem(CURRENT_PAGE);
				}
			});
		}
		return nextButton;
	}

	private JButton getPrevButton() {
		if (prevButton == null) {
			prevButton = new JButton("<");
			prevButton.setEnabled(CURRENT_PAGE > 1);
			prevButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE > 1) {
					CURRENT_PAGE--;
					pagesCombo.setSelectedItem(CURRENT_PAGE);
				}
			});
		}
		return prevButton;
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo == null) {
			pagesCombo = new JComboBox<>();
			pagesCombo.setPreferredSize(new Dimension(100, 25));
		}

		return pagesCombo;
	}

	private JLabel getUnderLabel() throws OHServiceException {
		if (underLabel == null) {
			underLabel = new JLabel("/ " + (TOTAL_PAGES + 1) + " " + MessageBundle.getMessage("angal.common.page.label"));
			underLabel.setPreferredSize(new Dimension(60, 30));
		}
		return underLabel;
	}

	private JLabel getTotalPatientsLabelsLabel() throws OHServiceException {
		if (totalPatientsLabel == null) {
			totalPatientsLabel = new JLabel(MessageBundle.getMessage("angal.pregnancy.totalpatient.label") + ": " + TOTAL_PATIENTS);
		}
		return totalPatientsLabel;
	}

	private JPanel getSearchPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel searchPanel = new JPanel();
		searchPatientTextField = new JTextField();
		searchPatientTextField.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					try {
						fetchData(true);
					} catch (OHServiceException ex) {
						throw new RuntimeException(ex);
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		searchPatientTextField.setPreferredSize(new Dimension(100, 20));
		searchPanel.add(searchPatientTextField);
		setMyBorder(searchPanel, MessageBundle.getMessage("angal.common.searchkey.txt"));

		panel.add(searchPanel, BorderLayout.NORTH);

		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			searchPanel.add(jSearchButton);
			jSearchButton.addActionListener(actionEvent -> {
				try {
					fetchData(true);
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}
			});
		}

		JPanel panelPregnantPrint = new JPanel();
		panel.add(panelPregnantPrint, BorderLayout.SOUTH);
		panelPregnantPrint.setLayout(new BorderLayout(0, 0));
		JButton updateDelivery = new JButton(MessageBundle.getMessage("angal.pregnancy.updatedelivery.btn"));
		panelPregnantPrint.add(updateDelivery, BorderLayout.NORTH);
		JButton declarationBirth = new JButton(MessageBundle.getMessage("angal.pregnancy.declaration_birth_but.btn"));
		panelPregnantPrint.add(declarationBirth, BorderLayout.CENTER);
		JButton declarationCertificate = new JButton(MessageBundle.getMessage("angal.pregnancy.declaration_certificate_but.btn"));
		panelPregnantPrint.add(declarationCertificate, BorderLayout.SOUTH);
		return panel;
	}

	private void setMyBorder(JPanel panel, String title) {
		Border border = BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder(title),
			BorderFactory.createEmptyBorder(0, 0, 0, 0)
		);
		panel.setBorder(border);
	}

	private JPanel getPatientButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		int buttonsize = 0;
		getJNewPatientButton();
		getJEditPatientButton();
		getJDelPatientButton();

		if (jNewPatientButton.getText().length() > buttonsize) {
			buttonsize = jNewPatientButton.getText().length();
		}

		if (jEditPatientButton.getText().length() > buttonsize) {
			buttonsize = jEditPatientButton.getText().length();
		}

		if (jDeletePatientButton.getText().length() > buttonsize) {
			buttonsize = jDeletePatientButton.getText().length();
		}

		jNewPatientButton.setPreferredSize(new Dimension(180, 30));
		jEditPatientButton.setPreferredSize(new Dimension(180, 30));
		jDeletePatientButton.setPreferredSize(new Dimension(180, 30));
		jNewPatientButton.setMinimumSize(new Dimension(buttonsize + 100, 30));
		jEditPatientButton.setMinimumSize(new Dimension(buttonsize + 100, 30));
		jDeletePatientButton.setMinimumSize(new Dimension(buttonsize + 100, 30));
		jNewPatientButton.setMaximumSize(new Dimension(buttonsize + 150, 30));
		jEditPatientButton.setMaximumSize(new Dimension(buttonsize + 150, 30));
		jDeletePatientButton.setMaximumSize(new Dimension(buttonsize + 150, 30));
		buttonPanel.add(jNewPatientButton);
		buttonPanel.add(jEditPatientButton);
		buttonPanel.add(jDeletePatientButton);
		return buttonPanel;
	}

	private JScrollPane getPatientTablePanel() {
		try {
			model = new PatientsTableModel("");
			patientTable = new JTable(model);
			patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		patientTable.setAutoCreateRowSorter(true);

		for (int i = 0; i < columnHeaders.length; i++) {
			patientTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}

		int tableWidth = 0;
		for (int columnWidth : columnWidths) {
			tableWidth += columnWidth;
		}

		TableListener listener = new TableListener();
		patientTable.getSelectionModel().addListSelectionListener(listener);
		patientTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);

		JScrollPane patientScrollPane = new JScrollPane(patientTable);
		patientScrollPane.setPreferredSize(new Dimension(tableWidth + 400, 300));
		return patientScrollPane;
	}

	private JPanel getPregnancyButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getJNewPregnancyButton());
		buttonPanel.add(getJNewPrenatalVisitButton());
		buttonPanel.add(getJNewPostnatalVisitButton());
		buttonPanel.add(getJDeliveryButton());
		buttonPanel.add(getJEditVisitButton());
		buttonPanel.add(getJExamsButton());
		buttonPanel.add(getJVaccinButton());
		buttonPanel.add(getJDeleteVisitButton());
		buttonPanel.add(getJReportButton());
		buttonPanel.add(getJCloseButton());
		return buttonPanel;
	}

	private JButton getJNewPatientButton() {
		if (jNewPatientButton == null) {
			jNewPatientButton = new JButton(MessageBundle.getMessage("angal.common.newpatient.btn"));
			jNewPatientButton.setMnemonic(MessageBundle.getMnemonic("angal.common.newpatient.btn.key"));
		}
		if (patient != null) {
			jNewPatientButton.setEnabled(false);
		}
		return jNewPatientButton;
	}

	private JButton getJEditPatientButton() {
		if (jEditPatientButton == null) {
			jEditPatientButton = new JButton(MessageBundle.getMessage("angal.pregnancy.editpatient.btn"));
			jEditPatientButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.editpatient.btn.key"));
			jEditPatientButton.addActionListener(actionEvent -> {
				if (patientTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}

				Admission adPatient = (Admission) patientTable.getValueAt(patientTable.getSelectedRow(),
					-1);
				if (adPatient != null) {
					patient = adPatient.getPatient();
				} else {
					patient = null;
				}
			});
		}
		if (patient != null) {
			jEditPatientButton.setEnabled(false);
		}
		return jEditPatientButton;
	}

	private JButton getJDelPatientButton() {
		if (jDeletePatientButton == null) {
			jDeletePatientButton = new JButton(MessageBundle.getMessage("angal.pregnancy.deletepatient.btn"));
			jDeletePatientButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.deletepatient.btn.key"));
			jDeletePatientButton.addActionListener(actionEvent -> {
				if (patientTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Admission adPatient = (Admission) patientTable.getValueAt(patientTable.getSelectedRow(),
					-1);
				if (adPatient != null) {
					patient = adPatient.getPatient();
				} else {
					patient = null;
				}
			});
		}
		if (patient != null) {
			jDeletePatientButton.setEnabled(false);
		}
		return jDeletePatientButton;
	}

	private JButton getJNewPrenatalVisitButton() {
		if (jNewPrenatalVisitButton == null) {
			jNewPrenatalVisitButton = new JButton(MessageBundle.getMessage("angal.pregnancy.newprenatalvisit.btn"));
			jNewPrenatalVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.newprenatalvisit.btn.key"));
		}
		return jNewPrenatalVisitButton;
	}

	private JButton getJDeleteVisitButton() {
		if (jDeleteVisitButton == null) {
			jDeleteVisitButton = new JButton(MessageBundle.getMessage("angal.pregnancy.deletevisit.btn"));
			jDeleteVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.deletevisit.btn.key"));
		}
		return jDeleteVisitButton;
	}

	private JButton getJReportButton() {
		if (jReportButton == null) {
			jReportButton = new JButton(MessageBundle.getMessage("angal.pregnancy.report.btn"));
			jReportButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.report.btn.key"));
		}
		return jReportButton;
	}

	private JButton getJNewPostnatalVisitButton() {
		if (jNewPostnatalVisitButton == null) {
			jNewPostnatalVisitButton = new JButton(MessageBundle.getMessage("angal.pregnancy.newpostnatalvisit.btn"));
			jNewPostnatalVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.newpostnatalvisit.btn.key"));
		}
		return jNewPostnatalVisitButton;
	}

	private JButton getJNewPregnancyButton() {
		if (jNewPregnancyButton == null) {
			jNewPregnancyButton = new JButton(MessageBundle.getMessage("angal.pregnancy.newpregnancy.btn"));
			jNewPregnancyButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.newpregnancy.btn.key"));
		}
		return jNewPregnancyButton;
	}

	private JButton getJEditVisitButton() {
		if (jEditVisitButton == null) {
			jEditVisitButton = new JButton(MessageBundle.getMessage("angal.pregnancy.editvisit.btn"));
			jEditVisitButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.editvisit.btn.key"));
		}
		return jEditVisitButton;
	}

	private JButton getJDeliveryButton() {
		if (jDeliveryButton == null) {
			jDeliveryButton = new JButton(MessageBundle.getMessage("angal.pregnancy.newdelivery.btn"));
			jDeliveryButton.setMnemonic(MessageBundle.getMnemonic("angal.pregnancy.newdelivery.btn.key"));
		}
		if (patient != null) {
			jDeliveryButton.setEnabled(false);
		}
		return jDeliveryButton;
	}

	private JButton getJExamsButton() {
		if (jExamsButton == null) {
			jExamsButton = new JButton(MessageBundle.getMessage("angal.opd.exams.btn"));
			jExamsButton.setMnemonic(MessageBundle.getMnemonic("angal.opd.exams.btn.key"));
		}
		return jExamsButton;
	}

	private JButton getJVaccinButton() {
		if (jVaccinButton == null) {
			jVaccinButton = new JButton(MessageBundle.getMessage("angal.cpn.vaccin.btn"));
			jVaccinButton.setMnemonic(MessageBundle.getMnemonic("angal.cpn.vaccin.btn.key"));
		}
		return jVaccinButton;
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private JPanel getVisitPanel() {
		JPanel visitListPanel = new JPanel(new BorderLayout());
		visitListPanel.add(getVisitScrollPane(), BorderLayout.NORTH);
		visitListPanel.add(getPregnancyDetailsPanel(), BorderLayout.EAST);
		return visitListPanel;
	}

	private JScrollPane getVisitScrollPane() {
		visitTable = new JTable(new  PregnancyVisitsTableModel());

		for (int i = 0; i < vColumns.length; i++) {
			visitTable.getColumnModel().getColumn(i).setPreferredWidth(vColumnWidths[i]);
		}

		int tableWidth = 0;

		for (int vColumnWidth : vColumnWidths) {
			tableWidth += vColumnWidth;
		}

		JScrollPane visitScrollPane = new JScrollPane(visitTable);
		visitScrollPane.setPreferredSize(new Dimension(tableWidth + 400, 200));
		return visitScrollPane;
	}

	private JPanel getPregnancyDetailsPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(180, 100));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		return panel;
	}

	class PregnancyVisitsTableModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public String getColumnName(int c) {
			return vColumns[c];
		}

		public int getColumnCount() {
			return vColumns.length;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

	}

	class PatientsTableModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public PatientsTableModel(String keywords) throws OHServiceException {
			Page<Admission> pagedResult = admissionBrowserManager.getAdmittedPatientsBySexAndNamePaged(
				'F', keywords, PAGE_SIZE, CURRENT_PAGE
			);
			patientList = pagedResult.getContent();
			TOTAL_PATIENTS = pagedResult.getTotalElements();
			TOTAL_PAGES = pagedResult.getTotalPages();
		}

		public int getRowCount() {
			if (patientList == null) {
				return 0;
			}
			return patientList.size();
		}

		public String getColumnName(int c) {
			return columnHeaders[c];
		}

		public int getColumnCount() {
			return columnHeaders.length;
		}

		public Object getValueAt(int r, int c) {
			if (patientList.isEmpty() || r >= patientList.size()) {
				return null;
			}

			if (c == -1) {
				return patientList.get(r);
			} else if (c == 0) {
				return patientList.get(r).getPatient().getCode() + "";
			} else if (c == 1) {
				return patientList.get(r).getPatient().getSecondName() + " "
					+ patientList.get(r).getPatient().getFirstName();
			} else if (c == 2) {
				return patientList.get(r).getPatient().getAge();

			} else if (c == 3) {
				return patientList.get(r).getPatient().getCity() + " " + patientList.get(r).getPatient().getAddress();
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	@Override
	public void patientUpdated(AWTEvent e) {
		try {
			fetchData(true);
		} catch (OHServiceException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void patientInserted(AWTEvent e) {
		try {
			fetchData(true);
		} catch (OHServiceException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void fetchData(boolean initPagination) throws OHServiceException {
		Page<Admission> pagedResult = admissionBrowserManager.getAdmittedPatientsBySexAndNamePaged(
			'F', searchPatientTextField.getText().trim(), PAGE_SIZE, CURRENT_PAGE - 1
		);

		patientList = pagedResult.getContent();

		if (initPagination) {
			TOTAL_PATIENTS = pagedResult.getTotalElements();
			TOTAL_PAGES = pagedResult.getTotalPages();
			CURRENT_PAGE = 1;

			underLabel.setText(
				"/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.page.label")
			);

			totalPatientsLabel.setText(
				MessageBundle.getMessage("angal.pregnancy.totalpatient.label") + ": " + TOTAL_PATIENTS
			);

			// Remove listeners to avoid extra SQL request
			for (var al : pagesCombo.getActionListeners()) {
				pagesCombo.removeActionListener(al);
			}

			pagesCombo.removeAllItems();

			for (int i = 0; i < TOTAL_PAGES; i++) {
				pagesCombo.addItem(i + 1);
			}

			pagesCombo.setSelectedItem(1);

			// Set back the listener
			pagesCombo.addActionListener(actionEvent -> {
				if (pagesCombo.getItemCount() > 0 && pagesCombo.getSelectedItem() != null) {
					CURRENT_PAGE = (Integer) pagesCombo.getSelectedItem();
					try {
						fetchData(false);
					} catch (OHServiceException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}

		model.fireTableDataChanged();
		patientTable.updateUI();

		updateNavigationButtons();
	}

	private void updateNavigationButtons() {
		nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES > 1);
		prevButton.setEnabled(CURRENT_PAGE > 1);
	}

	private void filterVisit() {
		visitTable.setModel(new  PregnancyVisitsTableModel());
		if (visitTable.getRowCount() > 0) {
			visitTable.setRowSelectionInterval(0, 0);
		}
	}

	class TableListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int row = patientTable.getSelectedRow();
			if (arg0.getValueIsAdjusting() && row > -1) {
				Admission adPatient = (Admission) patientTable.getValueAt(patientTable.getSelectedRow(),
					-1);
				patient = adPatient.getPatient();
				filterVisit();
			}
		}
	}
}
