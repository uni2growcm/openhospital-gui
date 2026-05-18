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
package org.isf.opd.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;
import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.gui.OpdEditExtended.SurgeryListener;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.patient.model.Patient;
import org.isf.therapy.gui.TherapyEdit;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.springframework.data.domain.Page;
import org.isf.lab.gui.LabNew;


/**
 * OpdBrowser - list all OPD. Let the user select an opd to edit or delete
 */
public class OpdBrowser extends ModalJFrame implements OpdEdit.SurgeryListener, SurgeryListener {

	private static final long serialVersionUID = 2372745781159245861L;

	private JPanel jButtonPanel;
	private JPanel jContainPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jExamButton;
	private JButton jTherapyButton;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JPanel dateFilterPanel;
	private JPanel jSelectionDiseasePanel;
	private JPanel jAgeFromPanel;
	private VoLimitedTextField jAgeFromTextField;
	private JPanel jAgeToPanel;
	private VoLimitedTextField jAgeToTextField;
	private JPanel jAgePanel;
	private JComboBox<DiseaseType> jDiseaseTypeBox;
	private JComboBox<Disease> jDiseaseBox;
	private JComboBox jWardBox;
	private JPanel sexPanel;
	private Integer ageTo = 0;
	private Integer ageFrom = 0;
	private DiseaseType allDiseaseType = new DiseaseType(
			MessageBundle.getMessage("angal.common.alldiseasetypes.txt"),
			MessageBundle.getMessage("angal.common.alldiseasetypes.txt"));
	private Disease allDisease = new Disease(
			MessageBundle.getMessage("angal.opd.alldiseases.txt"),
			MessageBundle.getMessage("angal.opd.alldiseases.txt"),
			allDiseaseType);
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.opdnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.ward.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.patientid.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.fullname.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.opd.disease.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.diseasetype.col").toUpperCase(),
			MessageBundle.getMessage("angal.opd.patientstatus.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.user.col").toUpperCase()
	};

	private DiseaseTypeBrowserManager diseaseTypeBrowserManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
	private OpdBrowserManager opdBrowserManager = Context.getApplicationContext().getBean(OpdBrowserManager.class);
	private DiseaseBrowserManager diseaseBrowserManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);

	private boolean isSingleUser = GeneralData.getGeneralData().getSINGLEUSER();
	private List<Opd> pSur;
	private JTable jTable;
	private OpdBrowsingModel model;
	private List<Disease> diseasesAll;
	private int[] pColumnWidth = {50, 80, 100, 130, 70, 150, 30, 30, 195, 195, 50, 50};
	private boolean[] columnResizable = { false, false, false, false, false, true, false, false, true, true, false, false };
	private boolean[] columnsVisible = { true, true, GeneralData.OPDEXTENDED, true, GeneralData.OPDEXTENDED, GeneralData.OPDEXTENDED, true, true, true, true, true, !isSingleUser };
	private int[] columnsAlignment = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT };
	private boolean[] columnsBold = { true, true, false, false, true, false, false, false, false, false, false, false };
	private int selectedrow;
	private JButton filterButton;
	private String rowCounterText = MessageBundle.getMessage("angal.common.count.label") + ' ';
	private JLabel rowCounter;
	private JRadioButton radioNewAttendance;
	private JRadioButton radioAllPatiens;
	private final JFrame myFrame;
	private JRadioButton radioMale;
	private JRadioButton radioAllGender;
	private List<Disease> diseases;
	protected AbstractButton searchDiseaseButton;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private JButton resetButton;
	private JTextField opdCodeFilter;
	private JTextField progYearFilter;
	private JTextField patientCodeFilter;
	private JRadioButton radioMyPatients;
	private JRadioButton radioAllPatients;

	private static final int PAGE_SIZE = 100;
	private int currentPage = 0;
	private long totalRows = 0;
	private int totalPages = 0;
	private boolean updatingPageCombo;
	private SearchMode searchMode = SearchMode.FILTERS;
	private int searchCode;

	private JButton prevButton;
	private JButton nextButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel underLabel;
	private JLabel totalPatientsLabel;

	private enum SearchMode {
		FILTERS,
		OPD_CODE,
		PROG_YEAR,
		PATIENT_ID
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new OpdBrowsingModel();
			jTable = new JTable(model);
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			TableColumnModel columnModel = jTable.getColumnModel();
			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			for (int i = 0; i < model.getColumnCount(); i++) {
				columnModel.getColumn(i).setMinWidth(pColumnWidth[i]);
				columnModel.getColumn(i).setCellRenderer(new AlignmentCellRenderer());
				if (!columnResizable[i]) {
					columnModel.getColumn(i).setMaxWidth(pColumnWidth[i]);
				}
				if (!columnsVisible[i]) {
					columnModel.getColumn(i).setMaxWidth(0);
					columnModel.getColumn(i).setMinWidth(0);
					columnModel.getColumn(i).setPreferredWidth(0);
				}
			}
		}
		return jTable;
	}

	private int getJTableWidth() {
	    return Arrays.stream(pColumnWidth).sum();
	}

	class AlignmentCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnsAlignment[column]);
			if (columnsBold[column]) {
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}

	/**
	 * This method initializes
	 */
	public OpdBrowser() {
		super();
		myFrame = this;
		initialize();
		setLocationRelativeTo(null);
	}

	public OpdBrowser(Patient patient) {
		super();
		myFrame = this;
		initialize();
		Opd newOpd = new Opd(0, ' ', -1, null);
		OpdEditExtended editrecord = new OpdEditExtended(myFrame, newOpd, patient, true);
		editrecord.addSurgeryListener(this);
		editrecord.showAsModal(myFrame);
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jButtonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnopdnew")) {
				jButtonPanel.add(getJNewButton(), null);
			}
			if (MainMenu.checkUserGrants("btnopdedit")) {
				jButtonPanel.add(getJEditButton(), null);
			}
			if (MainMenu.checkUserGrants("btnadmlab")) {
				jButtonPanel.add(getJExamButton(), null);
			}
			if (MainMenu.checkUserGrants("btnadmtherapy")) {
				jButtonPanel.add(getJTherapyButton(), null);
			}
			if (MainMenu.checkUserGrants("btnopddel")) {
				jButtonPanel.add(getJDeleteButton(), null);
			}
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JPanel getPaginationPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
		panel.setBorder(BorderFactory.createEtchedBorder());

		prevButton = new JButton("<");
		prevButton.addActionListener(e -> {
			if (currentPage > 0) {
				currentPage--;
				loadCurrentPage();
			}
		});

		pagesCombo = new JComboBox<>();
		pagesCombo.setPreferredSize(new Dimension(70, 25));
		pagesCombo.addActionListener(e -> {
			if (!updatingPageCombo && pagesCombo.getSelectedItem() != null) {
				int selected = (Integer) pagesCombo.getSelectedItem();
				if (selected - 1 != currentPage) {
					currentPage = selected - 1;
					loadCurrentPage();
				}
			}
		});

		nextButton = new JButton(">");
		nextButton.addActionListener(e -> {
			if (currentPage < totalPages - 1) {
				currentPage++;
				loadCurrentPage();
			}
		});

		underLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.opd.pagination.pages"));
		totalPatientsLabel = new JLabel(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": 0");

		panel.add(prevButton);
		panel.add(pagesCombo);
		panel.add(underLabel);
		panel.add(nextButton);
		panel.add(totalPatientsLabel);

		return panel;
	}

	private void updatePaginationControls() {
		if (prevButton == null || nextButton == null || pagesCombo == null) {
			return;
		}

		boolean hasMultiplePages = totalPages > 1;

		if (hasMultiplePages && pagesCombo.getItemCount() != totalPages && totalPages > 0) {
			updatingPageCombo = true;
			pagesCombo.removeAllItems();
			for (int i = 1; i <= totalPages; i++) {
				pagesCombo.addItem(i);
			}
			updatingPageCombo = false;
		}

		if (hasMultiplePages && totalPages > 0) {
			updatingPageCombo = true;
			pagesCombo.setSelectedItem(currentPage + 1);
			updatingPageCombo = false;
		}

		prevButton.setEnabled(currentPage > 0 && hasMultiplePages);
		nextButton.setEnabled(currentPage < totalPages - 1 && hasMultiplePages);

		pagesCombo.setEnabled(hasMultiplePages);

		if (totalPages <= 0) {
			underLabel.setText("/ 0 " + MessageBundle.getMessage("angal.opd.pagination.pages"));
			pagesCombo.setEnabled(false);
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
		} else {
			underLabel.setText("/ " + totalPages + " " + MessageBundle.getMessage("angal.opd.pagination.pages"));
		}
	}

	private int calculatePages(long rows) {
		return rows == 0 ? 0 : (int) Math.ceil((double) rows / PAGE_SIZE);
	}

	private void refreshModel() {
		loadExtraDiagnoses(pSur);
		if (currentPage >= totalPages && currentPage > 0) {
			currentPage = totalPages - 1;
			loadCurrentPage();
			return;
		}

		if (model == null) {
			model = new OpdBrowsingModel();
		}
		jTable.setModel(model);
		TableColumnModel columnModel = jTable.getColumnModel();
		for (int i = 0; i < model.getColumnCount(); i++) {
			columnModel.getColumn(i).setMinWidth(pColumnWidth[i]);
			columnModel.getColumn(i).setCellRenderer(new AlignmentCellRenderer());
			if (!columnResizable[i]) {
				columnModel.getColumn(i).setMaxWidth(pColumnWidth[i]);
			}
			if (!columnsVisible[i]) {
				columnModel.getColumn(i).setMaxWidth(0);
				columnModel.getColumn(i).setMinWidth(0);
				columnModel.getColumn(i).setPreferredWidth(0);
			}
		}
		model.fireTableDataChanged();
		jTable.updateUI();
		updatePaginationControls();
		rowCounter.setText(rowCounterText + totalRows);
	}

	private void loadExtraDiagnoses(List<Opd> opds) {
		if (opds == null || opds.isEmpty()) {
			return;
		}
		List<Disease> allDiseases = getDiseasesAll();
		if (allDiseases == null || allDiseases.isEmpty()) {
			return;
		}
		for (Opd opd : opds) {
			if (opd != null) {
				opd.loadExtraDiagnosesFromString(allDiseases);
			}
		}
	}

	private List<Disease> getDiseasesAll() {
		if (diseasesAll == null) {
			try {
				diseasesAll = diseaseBrowserManager.getDiseaseAll();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
				diseasesAll = new ArrayList<>();
			}
		}
		return diseasesAll;
	}

	private String getDiagnosesDescription(Opd opd) {
		String diagnoses = joinDiagnoses(opd, false);
		return diagnoses.isEmpty() ? '[' + MessageBundle.getMessage("angal.opd.notspecified.msg") + ']' : diagnoses;
	}

	private String getDiagnosisTypesDescription(Opd opd) {
		return joinDiagnoses(opd, true);
	}

	private String joinDiagnoses(Opd opd, boolean diseaseTypes) {
		if (opd == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		List<String> added = new ArrayList<>();
		for (Disease disease : opd.getAllDiagnoses()) {
			if (disease == null || disease.getCode() == null || added.contains(disease.getCode())) {
				continue;
			}
			String value = "";
			if (diseaseTypes) {
				if (disease.getType() != null && disease.getType().getDescription() != null) {
					value = disease.getType().getDescription();
				}
			} else {
				value = disease.getDescription() != null ? disease.getDescription() : disease.getCode();
			}
			if (!value.isEmpty()) {
				if (builder.length() > 0) {
					builder.append(", ");
				}
				builder.append(value);
			}
			added.add(disease.getCode());
		}
		return builder.toString();
	}

	private void loadCurrentPage() {
		try {
			Page<Opd> opdPage;

			if (searchMode == SearchMode.PROG_YEAR) {
				opdPage = opdBrowserManager.getOpdListByProgYear(searchCode, currentPage, PAGE_SIZE);
				pSur = new ArrayList<>(opdPage.getContent());
				totalRows = opdPage.getTotalElements();
				totalPages = opdPage.getTotalPages();
				refreshModel();
				rowCounter.setText(rowCounterText + totalRows);
				totalPatientsLabel.setText(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": " + totalRows);
				return;
			}

			if (searchMode == SearchMode.PATIENT_ID) {

				opdPage = opdBrowserManager.getOpdListByPatientId(searchCode, currentPage, PAGE_SIZE);
				pSur = new ArrayList<>(opdPage.getContent());
				totalRows = opdPage.getTotalElements();
				totalPages = opdPage.getTotalPages();
				refreshModel();
				rowCounter.setText(rowCounterText + totalRows);
				totalPatientsLabel.setText(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": " + totalRows);
				return;
			}

			if (searchMode == SearchMode.OPD_CODE) {
				Optional<Opd> opd = opdBrowserManager.getOpdById(searchCode);
				pSur = new ArrayList<>();
				opd.ifPresent(pSur::add);
				totalRows = pSur.size();
				totalPages = calculatePages(totalRows);
				refreshModel();
				rowCounter.setText(rowCounterText + totalRows);
				totalPatientsLabel.setText(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": " + totalRows);
				return;
			}

			Page<Opd> opdPageResponse = opdBrowserManager.getOpds(
					getSelectedWard(), getSelectedDiseaseType(), getSelectedDisease(),
					dateFrom.getDate(), dateTo.getDate(), ageFrom, ageTo,
					getGender(), getPatientAttendance(), currentPage, PAGE_SIZE);
			pSur = new ArrayList<>(opdPageResponse.getContent());
			totalRows = opdPageResponse.getTotalElements();
			totalPages = opdPageResponse.getTotalPages();
			refreshModel();
			rowCounter.setText(rowCounterText + totalRows);
			totalPatientsLabel.setText(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": " + totalRows);

		} catch (OHServiceException ohServiceException) {
			pSur = new ArrayList<>();
			totalRows = 0;
			totalPages = 0;
			refreshModel();
			rowCounter.setText(rowCounterText + "0");
			totalPatientsLabel.setText(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": 0");
			MessageDialog.showExceptions(ohServiceException);
		}
		updateCounters();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.opd.opdoutpatientdepartment.title"));
		this.setContentPane(getJContainPanel());
		this.setMinimumSize(new Dimension(400 + getJTableWidth(), 700));
		rowCounter.setText(rowCounterText + pSur.size());
		applyFilter();
		validate();
	}

	/**
	 * This method initializes containPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(getJSelectionPanel(), BorderLayout.WEST);

			JPanel centerPanel = new JPanel(new BorderLayout());
			centerPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
			centerPanel.add(getPaginationPanel(), BorderLayout.SOUTH);
			jContainPanel.add(centerPanel, BorderLayout.CENTER);

			validate();
		}
		return jContainPanel;
	}
	private void applyFilter() {
		if (!validateFilters()) {
			return;
		}
		loadCurrentPage();
	}

	/**
	 * This method initializes jNewButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				Opd newOpd = new Opd(0, ' ', -1, null);
				if (GeneralData.OPDEXTENDED) {
					OpdEditExtended newrecord = new OpdEditExtended(myFrame, newOpd, true);
					newrecord.addSurgeryListener(this);
					newrecord.showAsModal(myFrame);
				} else {
					OpdEdit newrecord = new OpdEdit(myFrame, newOpd, true);
					newrecord.addSurgeryListener(this);
					newrecord.setLocationRelativeTo(myFrame);
					newrecord.setVisible(true);
				}
			});
		}
		return jNewButton;
	}

	/**
	 * This method initializes jEditButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedrow = jTable.getSelectedRow();
				Opd opd = (Opd) model.getValueAt(selectedrow, -1);
				if (GeneralData.OPDEXTENDED) {
					OpdEditExtended editrecord = new OpdEditExtended(myFrame, opd, false);
					editrecord.addSurgeryListener(this);
					editrecord.showAsModal(myFrame);
				} else {
					OpdEdit editrecord = new OpdEdit(myFrame, opd, false);
					editrecord.addSurgeryListener(this);
					editrecord.setLocationRelativeTo(myFrame);
					editrecord.setVisible(true);
				}
			});
		}
		return jEditButton;
	}

	/**
	 * This method initializes jCloseButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private JButton getJTherapyButton() {
		if (jTherapyButton == null) {
			jTherapyButton = new JButton(MessageBundle.getMessage("angal.admission.therapy.btn"));
			jTherapyButton.setMnemonic(KeyEvent.VK_T);
			jTherapyButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Opd opd = (Opd) model.getValueAt(jTable.getSelectedRow(), -1);
				Patient patient = opd.getPatient();
				if (patient == null) {
					MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
					return;
				}
				Admission currentAdmission = admissionBrowserManager.getCurrentAdmission(patient);
				TherapyEdit therapy = new TherapyEdit(this, patient, currentAdmission != null);
				therapy.setLocationRelativeTo(null);
				therapy.setVisible(true);
			});
		}
		return jTherapyButton;
	}

	/**
	 * This method initializes jDeleteButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Opd opd = (Opd) model.getValueAt(jTable.getSelectedRow(), -1);

				String message;
				if (GeneralData.OPDEXTENDED) {
					message = MessageBundle.formatMessage("angal.opd.deletefollowingopdextended.fmt.msg",
							opd.getPatient().getName(),
							opd.getCreatedDate() == null
									? opd.getDate().format(DATE_FORMATTER)
									: opd.getCreatedDate().format(DATE_FORMATTER),
							getDiagnosesDescription(opd),
							opd.getAge(),
							opd.getSex(),
							opd.getDate().format(DATE_TIME_FORMATTER));
				} else {
					message = MessageBundle.formatMessage("angal.opd.deletefollowingopd.fmt.msg",
							opd.getCreatedDate() == null
									? opd.getDate().format(DATE_FORMATTER)
									: opd.getCreatedDate().format(DATE_FORMATTER),
							getDiagnosesDescription(opd),
							opd.getAge(),
							opd.getSex(),
							opd.getDate().format(DATE_FORMATTER));
				}

				int n = JOptionPane.showConfirmDialog(null, message,
						MessageBundle.getMessage("angal.messagedialog.question.title"), JOptionPane.YES_NO_OPTION);
				try {
					if (n == JOptionPane.YES_OPTION) {
						opdBrowserManager.deleteOpd(opd);
						pSur.remove(pSur.size() - jTable.getSelectedRow() - 1);
						model.fireTableDataChanged();
						jTable.updateUI();
					}
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			});
		}
		return jDeleteButton;
	}

	/**
	 * This method initializes jSelectionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSelectionPanel() {
		JPanel jSelectionPanel = new JPanel(); //the outer panel get maximum height (as per WEST from outer container)
		jSelectionPanel.add(getJSelectionContentPanel()); //the inner panel can use any layout
		return jSelectionPanel;
	}

	private JPanel getJSelectionContentPanel() {
		JPanel jSelectionContentPanel = new JPanel(new SpringLayout());
		jSelectionContentPanel.add(getSearchCodesPanel());
		jSelectionContentPanel.add(getOtherFiltersPanel());
		jSelectionContentPanel.add(getButtonsPanel());
		SpringUtilities.makeCompactGrid(jSelectionContentPanel, 3, 1, 5, 5, 5, 5);
		return jSelectionContentPanel;
	}

	private JPanel getButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		JPanel filterButtonPanel = new JPanel();
		filterButtonPanel.add(getFilterButton());
		filterButtonPanel.add(getResetButton());
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		buttonsPanel.add(filterButtonPanel);
		buttonsPanel.add(getRowCounter());
		return buttonsPanel;
	}

	private JPanel getOtherFiltersPanel() {
		JPanel panel = new JPanel(new SpringLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.opd.otherfilters.border")));
		panel.add(getWardBoxPanel());
		panel.add(getJSelectionDiseasePanel());
		panel.add(getSexPanel());
		panel.add(getDateFilterPanel());
		panel.add(getJAgePanel());
		panel.add(getPatientTypePanel());
		if (isSingleUser) {
			panel.add(Box.createVerticalGlue());
			SpringUtilities.makeCompactGrid(panel, 7, 1, 5, 5, 5, 5);
		} else {
			panel.add(getUserPanel());
			panel.add(Box.createVerticalGlue());
			SpringUtilities.makeCompactGrid(panel, 8, 1, 5, 5, 5, 5);
		}
		return panel;
	}

	private JPanel getWardBoxPanel() {
		JPanel wardBoxPanel = new JPanel();
		wardBoxPanel.setLayout(new BoxLayout(wardBoxPanel, BoxLayout.Y_AXIS));
		wardBoxPanel.add(getWardBox());
		return wardBoxPanel;
	}

	public JPanel getUserPanel() {
		JPanel userPanel = new JPanel();
		ButtonGroup groupUserFilter = new ButtonGroup();
		radioMyPatients = new JRadioButton(MessageBundle.getMessage("angal.opd.mypatient.btn"));
		radioAllPatients = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
		radioAllPatients.setSelected(true);
		groupUserFilter.add(radioMyPatients);
		groupUserFilter.add(radioAllPatients);
		userPanel.add(radioMyPatients);
		userPanel.add(radioAllPatients);
		return userPanel;
	}

	private Component getSearchCodesPanel() {
		JPanel searchCodesPanel = new JPanel(new SpringLayout());
		searchCodesPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.opd.searchbycodespressenter.border")));
		opdCodeFilter = new JTextField(3);
		opdCodeFilter.addKeyListener(new SearchByOPDCodeListener());
		progYearFilter = new JTextField(3);
		progYearFilter.addKeyListener(new SearchByProgYearListener());
		patientCodeFilter = new JTextField(3);
		patientCodeFilter.addKeyListener(new SearchByPatientIdListener());
		searchCodesPanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt")));
		searchCodesPanel.add(new JLabel(MessageBundle.getMessage("angal.opd.opdnumber.txt")));
		searchCodesPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patientID")));
		searchCodesPanel.add(opdCodeFilter);
		searchCodesPanel.add(progYearFilter);
		searchCodesPanel.add(patientCodeFilter);
		SpringUtilities.makeCompactGrid(searchCodesPanel, 2, 3, 5, 5, 5, 5);
		return searchCodesPanel;
	}

	private JButton getResetButton() {
		if (resetButton == null) {
			resetButton = new JButton(MessageBundle.getMessage("angal.opd.reset.btn"));
			resetButton.setMnemonic(MessageBundle.getMnemonic("angal.opd.reset.btn.key"));
			resetButton.addActionListener(actionEvent -> resetAllFilters());
		}
		return resetButton;
	}

	private void resetAllFilters() {
		jDiseaseTypeBox.setSelectedIndex(0);
		jDiseaseBox.setSelectedIndex(0);
		jWardBox.setSelectedIndex(0);
		if (opdCodeFilter != null) {
			opdCodeFilter.setText("");
		}
		if (progYearFilter != null) {
			progYearFilter.setText("");
		}
		if (patientCodeFilter != null) {
			patientCodeFilter.setText("");
		}
		resetDates();
		jAgeFromTextField.setText("0");
		ageFrom = 0;
		jAgeToTextField.setText("0");
		ageTo = 0;
		radioAllGender.setSelected(true);
		radioAllPatiens.setSelected(true);
		radioAllPatients.setSelected(true);
		currentPage = 0;
		searchMode = SearchMode.FILTERS;
		applyFilter();
	}

	private boolean validateFilters() {
		LocalDate dateFromDate = dateFrom.getDate();
		LocalDate dateToDate = dateTo.getDate();

		if (dateFromDate.isAfter(dateToDate)) {
			MessageDialog.error(this, "angal.opd.datefrommustbebefordateto.msg");
			return false;
		}
		if (ageFrom > ageTo) {
			MessageDialog.error(this, "angal.opd.agefrommustbelowerthanageto.msg");
			jAgeFromTextField.setText(ageTo.toString());
			ageFrom = ageTo;
			return false;
		}
		if (TimeTools.getDaysBetweenDates(dateFromDate, dateToDate, true) >= 360) {
			int ok = JOptionPane.showConfirmDialog(this,
					MessageBundle.getMessage("angal.common.thiscouldretrievealargeamountofdataproceed.msg"),
					MessageBundle.getMessage("angal.messagedialog.question.title"),
					JOptionPane.OK_CANCEL_OPTION);
			return ok == JOptionPane.OK_OPTION;
		}
		return true;
	}

	private Ward getSelectedWard() {
		try {
			return (Ward) jWardBox.getSelectedItem();
		} catch (ClassCastException ex) {
			return null;
		}
	}

	private String getSelectedDiseaseType() {
		String diseaseType = ((DiseaseType) jDiseaseTypeBox.getSelectedItem()).getCode();
		return diseaseType.equals(allDiseaseType.getCode()) ? null : diseaseType;
	}
	private String getSelectedDisease() {
		String disease = ((Disease) jDiseaseBox.getSelectedItem()).getCode();
		return disease.equals(allDisease.getCode()) ? null : disease;
	}
	private JLabel getRowCounter() {
		if (rowCounter == null) {
			rowCounter = new JLabel();
			rowCounter.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return rowCounter;
	}

	private Component getDateFilterPanel() {
		if (dateFilterPanel == null) {
			dateFilterPanel = new JPanel(new SpringLayout());

			dateFrom = new GoodDateChooser(LocalDate.now());
			dateTo = new GoodDateChooser(LocalDate.now());

			resetDates();
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			dateFilterPanel.add(dateFrom);
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			dateFilterPanel.add(dateTo);
			SpringUtilities.makeCompactGrid(dateFilterPanel, 2, 2, 5, 5, 5, 5);
		}
		return dateFilterPanel;
	}

	private void resetDates() {
		if (!GeneralData.ENHANCEDSEARCH) {
			dateFrom.setDate(LocalDate.now().minusWeeks(1));
		} else {
			dateFrom.setDate(LocalDate.now());
		}
		dateTo.setDate(LocalDate.now());
	}

	public class DocumentLimit extends DefaultStyledDocument {

		private static final long serialVersionUID = -5098766139884585921L;

		private final int maximumNumberOfCharacters;

		public DocumentLimit(int numeroMassimoCaratteri) {
			maximumNumberOfCharacters = numeroMassimoCaratteri;
		}

		@Override
		public void insertString(int off, String text, AttributeSet att) throws BadLocationException {
			int numberOfCharactersInDocument = getLength();
			int newTextLength = text.length();
			if (numberOfCharactersInDocument + newTextLength > maximumNumberOfCharacters) {
				int numeroCaratteriInseribili = maximumNumberOfCharacters - numberOfCharactersInDocument;
				if (numeroCaratteriInseribili > 0) {
					String parteNuovoTesto = text.substring(0, numeroCaratteriInseribili);
					super.insertString(off, parteNuovoTesto, att);
				}
			} else {
				super.insertString(off, text, att);
			}
		}
	}

	/**
	 * This method initializes jWardBox
	 *
	 * @return javax.swing.JComboBox
	 */
	public JComboBox getWardBox() {
		if (jWardBox == null) {
			jWardBox = new JComboBox<>();

			WardBrowserManager wardManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
			List<Ward> wards = wardManager.getOpdWards();

			jWardBox.addItem(MessageBundle.getMessage("angal.opd.allwards.txt"));
			if (wards != null) {
				for (Ward elem : wards) {
					jWardBox.addItem(elem);
				}
			}
		}
		return jWardBox;
	}

	/**
	 * This method initializes jDiseaseTypeBox
	 *
	 * @return javax.swing.JComboBox
	 */
	public JComboBox<DiseaseType> getDiseaseTypeBox() {
		if (jDiseaseTypeBox == null) {
			jDiseaseTypeBox = new JComboBox<>();

			List<DiseaseType> types = null;
			try {
				types = diseaseTypeBrowserManager.getDiseaseType();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}

			jDiseaseTypeBox.addItem(allDiseaseType);
			if (types != null) {
				for (DiseaseType elem : types) {
					jDiseaseTypeBox.addItem(elem);
				}
			}

			jDiseaseTypeBox.addActionListener(actionEvent -> {
				jDiseaseBox.removeAllItems();
				getDiseaseBox();
			});
		}
		return jDiseaseTypeBox;
	}

	/**
	 * This method initializes jDiseaseBox
	 *
	 * @return javax.swing.JComboBox
	 */
	public JComboBox<Disease> getDiseaseBox() {
		if (jDiseaseBox == null) {
			jDiseaseBox = new JComboBox<>();

		}
		try {
			if (((DiseaseType) jDiseaseTypeBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.common.alldiseasetypes.txt"))) {
				diseases = diseaseBrowserManager.getDiseaseOpd();
			} else {
				diseases = diseaseBrowserManager.getDiseaseOpd(((DiseaseType) jDiseaseTypeBox.getSelectedItem()).getCode());
			}
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}

		jDiseaseBox.addItem(allDisease);
		if (diseases != null) {
			for (Disease elem : diseases) {
				jDiseaseBox.addItem(elem);
			}
		}
		jDiseaseBox.setPreferredSize(new Dimension(300, 25));
		jDiseaseBox.setMaximumSize(new Dimension(300, 25));
		return jDiseaseBox;
	}

	/**
	 * This method initializes sexPanel
	 *
	 * @return javax.swing.JPanel
	 */
	public JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			ButtonGroup group = new ButtonGroup();
			radioMale = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			JRadioButton radioFemale = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			radioAllGender = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioAllGender.setSelected(true);
			group.add(radioMale);
			group.add(radioFemale);
			group.add(radioAllGender);
			sexPanel.add(radioAllGender);
			sexPanel.add(radioMale);
			sexPanel.add(radioFemale);
		}
		return sexPanel;
	}

	public JPanel getPatientTypePanel() {
		JPanel patientTypePanel = new JPanel();
		JLabel patientTypeLabel = new JLabel(MessageBundle.getMessage("angal.opd.patienttype.label"));

		ButtonGroup groupNewPatient = new ButtonGroup();
		radioNewAttendance = new JRadioButton(MessageBundle.getMessage("angal.opd.new.btn"));
		JRadioButton radioReAttendance = new JRadioButton(MessageBundle.getMessage("angal.opd.reattendance.btn"));
		radioAllPatiens = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
		radioAllPatiens.setSelected(true);
		groupNewPatient.add(radioAllPatiens);
		groupNewPatient.add(radioNewAttendance);
		groupNewPatient.add(radioReAttendance);

		patientTypePanel.add(patientTypeLabel);
		patientTypePanel.add(radioAllPatiens);
		patientTypePanel.add(radioNewAttendance);
		patientTypePanel.add(radioReAttendance);
		return patientTypePanel;
	}

	/**
	 * This method initializes jSelectionDiseasePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSelectionDiseasePanel() {
		if (jSelectionDiseasePanel == null) {
			jSelectionDiseasePanel = new JPanel();
			jSelectionDiseasePanel.setLayout(new BoxLayout(jSelectionDiseasePanel, BoxLayout.Y_AXIS));
			jSelectionDiseasePanel.add(getDiseaseTypeBox(), null);
			jSelectionDiseasePanel.add(getJSearchDiseaseTextFieldPanel(), null);
			jSelectionDiseasePanel.add(getDiseaseBox(), null);
		}
		return jSelectionDiseasePanel;
	}

	private JPanel getJSearchDiseaseTextFieldPanel() {

		JPanel searchFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextField searchDiseasetextField = new JTextField(10);
		JLabel searchDiseaseLabel = new JLabel(MessageBundle.getMessage("angal.opd.searchdisease.label"));
		searchFieldPanel.add(searchDiseaseLabel);
		searchFieldPanel.add(searchDiseasetextField);
		searchDiseasetextField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					searchDiseaseButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		searchDiseaseButton = new JButton("");
		searchDiseaseButton.setPreferredSize(new Dimension(20, 20));
		searchDiseaseButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		searchFieldPanel.add(searchDiseaseButton);
		searchDiseaseButton.addActionListener(actionEvent -> {
			jDiseaseBox.removeAllItems();
			for (Disease disease : getSearchDiagnosisResults(searchDiseasetextField.getText(), diseases)) {
				jDiseaseBox.addItem(disease);
			}

			if (jDiseaseBox.getItemCount() >= 2) {
				jDiseaseBox.setSelectedIndex(1);
			}
			jDiseaseBox.requestFocus();
			if (jDiseaseBox.getItemCount() > 2) {
				jDiseaseBox.showPopup();
			}
		});

		return searchFieldPanel;
	}

	private List<Disease> getSearchDiagnosisResults(String s, List<Disease> diseaseList) {
		String query = s.trim();
		List<Disease> results = new ArrayList<>();
		for (Disease disease : diseaseList) {
			if (!query.isEmpty()) {
				String[] patterns = query.split(" ");
				String name = disease.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (name.contains(pattern.toLowerCase())) {
						patternFound = true;
						// It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(disease);
				}
			} else {
				results.add(disease);
			}
		}
		return results;
	}

	/**
	 * This method initializes jAgePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeFromPanel() {
		if (jAgeFromPanel == null) {
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.common.agefrom.label"));
			jAgeFromPanel = new JPanel();
			jAgeFromPanel.add(jLabel, null);
			jAgeFromPanel.add(getJAgeFromTextField(), null);
		}
		return jAgeFromPanel;
	}

	/**
	 * This method initializes jAgeFromTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeFromTextField() {
		if (jAgeFromTextField == null) {
			jAgeFromTextField = new VoLimitedTextField(3, 3);
			jAgeFromTextField.setText("0");
			ageFrom = 0;
			jAgeFromTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
						if (ageFrom < 0 || ageFrom > 200) {
							jAgeFromTextField.setText("");
							MessageDialog.error(OpdBrowser.this, "angal.opd.insertavalidage.msg");
						}
					} catch (NumberFormatException ex) {
						jAgeFromTextField.setText("");
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeFromTextField;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeToPanel() {
		if (jAgeToPanel == null) {
			JLabel jLabel = new JLabel(MessageBundle.getMessage("angal.common.ageto.label"));
			jAgeToPanel = new JPanel();
			jAgeToPanel.add(jLabel, null);
			jAgeToPanel.add(getJAgeToTextField(), null);
		}
		return jAgeToPanel;
	}

	/**
	 * This method initializes jTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeToTextField() {
		if (jAgeToTextField == null) {
			jAgeToTextField = new VoLimitedTextField(3, 3);
			jAgeToTextField.setText("0");
			ageTo = 0;
			jAgeToTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageTo = Integer.parseInt(jAgeToTextField.getText());
						if (ageTo < 0 || ageTo > 200) {
							jAgeToTextField.setText("");
							MessageDialog.error(OpdBrowser.this, "angal.opd.insertavalidage.msg");
						}
					} catch (NumberFormatException ex) {
						jAgeToTextField.setText("");
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
		}
		return jAgeToTextField;
	}

	/**
	 * This method initializes jAgePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.setLayout(new BoxLayout(jAgePanel, BoxLayout.Y_AXIS));
			jAgePanel.add(getJAgeFromPanel());
			jAgePanel.add(getJAgeToPanel());
		}

		return jAgePanel;
	}

	class OpdBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = -9129145534999353730L;

		public OpdBrowsingModel(Ward ward, String diseaseTypeCode, String diseaseCode, LocalDate dateFrom, LocalDate dateTo, int ageFrom, int ageTo,
				char sex, char newPatient, String user) {
			try {
				pSur = opdBrowserManager.getOpd(ward, diseaseTypeCode, diseaseCode, dateFrom, dateTo, ageFrom, ageTo, sex, newPatient, user);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		public OpdBrowsingModel() {
			try {
				pSur = opdBrowserManager.getOpd(!GeneralData.ENHANCEDSEARCH);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		@Override
		public int getRowCount() {
			if (pSur == null) {
				return 0;
			}

			return pSur.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Opd opd = pSur.get(pSur.size() - r - 1);
			Patient pat = opd.getPatient();
			int i = 0;
			if (c == -1) {
				return opd;
			} else if (c == i) {
				return opd.getCode();
			}  else if (c == ++i) {
				return opd.getProgYear();
			} else if (c == ++i) {
				if (GeneralData.OPDEXTENDED) {
					return opd.getWard().getDescription();
				}
			} else if (c == ++i) {
				if (GeneralData.OPDEXTENDED) {
					return opd.getDate().format(DATE_TIME_FORMATTER);
				}
				return opd.getDate().format(DATE_FORMATTER);
			} else if (c == ++i) {
				return pat != null ? opd.getPatient().getCode() : null;
			} else if (c == ++i) {
				return pat != null ? opd.getFullName() : null;
			} else if (c == ++i) {
				return opd.getSex();
			} else if (c == ++i) {
				return opd.getAge();
			} else if (c == ++i) {
				return getDiagnosesDescription(opd);
			} else if (c == ++i) {
				return getDiagnosisTypesDescription(opd);
			} else if (c == ++i) {
				String patientStatus;
				if (opd.getNewPatient() == 'N') {
					patientStatus = MessageBundle.getMessage("angal.opd.new.btn");
				} else {
					patientStatus = MessageBundle.getMessage("angal.opd.reattendance.btn");
				}
				return patientStatus;
			} else if (c == ++i) {
				return opd.getUserID();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	@Override
	public void surgeryUpdated(AWTEvent e, Opd opd) {
		loadExtraDiagnoses(List.of(opd));
		pSur.set(pSur.size() - selectedrow - 1, opd);
		((OpdBrowsingModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if (jTable.getRowCount() > 0 && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
		loadCurrentPage();
	}

	@Override
	public void surgeryInserted(AWTEvent e, Opd opd) {
		loadExtraDiagnoses(List.of(opd));
		currentPage = 0;
		searchMode = SearchMode.FILTERS;
		pSur.add(pSur.size(), opd);
		((OpdBrowsingModel) jTable.getModel()).fireTableDataChanged();
		loadCurrentPage();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
		rowCounter.setText(rowCounterText + pSur.size());
	}
	private void updateCounters() {
		rowCounter.setText(rowCounterText + totalRows);
		totalPatientsLabel.setText(MessageBundle.getMessage("angal.opd.pagination.totalpatients") + ": " + totalRows);
	}

	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
            filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(actionEvent -> {
				String diseasetype = ((DiseaseType)jDiseaseTypeBox.getSelectedItem()).getCode();
				if (diseasetype.equals(allDiseaseType.getCode())) {
					diseasetype = null;
				}
				String disease = ((Disease)jDiseaseBox.getSelectedItem()).getCode();
				if (disease.equals(allDisease.getCode())) {
					disease = null;
				}
				Ward ward = null;
				try {
					ward = (Ward) jWardBox.getSelectedItem();
				} catch (ClassCastException e) {
					// AllWards selected
				}

				char sex = getGender();
				char newPatient = getPatientAttendance();
				String user = getUser();

				LocalDate dateFromDate = dateFrom.getDate();
				LocalDate dateToDate = dateTo.getDate();

				if (dateFromDate.isAfter(dateToDate)) {
					MessageDialog.error(this, "angal.opd.datefrommustbebefordateto.msg");
					return;
				}

				if (ageFrom > ageTo) {
					MessageDialog.error(this, "angal.opd.agefrommustbelowerthanageto.msg");
					jAgeFromTextField.setText(ageTo.toString());
					ageFrom = ageTo;
					return;
				}

				//TODO: to retrieve resultset size instead of assuming 1 year as limit for the warning
				if (TimeTools.getDaysBetweenDates(dateFromDate, dateToDate, true) >= 360) {
					int ok = JOptionPane.showConfirmDialog(this,
							MessageBundle.getMessage("angal.common.thiscouldretrievealargeamountofdataproceed.msg"),
							MessageBundle.getMessage("angal.messagedialog.question.title"),
							JOptionPane.OK_CANCEL_OPTION);
					if (ok != JOptionPane.OK_OPTION) {
						return;
					}
				}
				currentPage = 0;
				searchMode = SearchMode.FILTERS;
				opdCodeFilter.setText("");
				progYearFilter.setText("");
				patientCodeFilter.setText("");
				loadCurrentPage();
			});
		}
		return filterButton;
	}

	private char getGender() {
		char sex;
		if (radioAllGender.isSelected()) {
			sex = 'A';
		} else {
			if (radioMale.isSelected()) {
				sex = 'M';
			} else {
				sex = 'F';
			}
		}
		return sex;
	}

	private String getUser() {
		if (!isSingleUser && radioMyPatients.isSelected()) {
			return MainMenu.getUser().getUserName();
		}
		return null;
	}

	private char getPatientAttendance() {
		if (radioAllPatiens.isSelected()) {
			return 'A';
		} else if (radioNewAttendance.isSelected()) {
			return 'N';
		}
		return 'R';
	}

	class SearchByOPDCodeListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				String codeHint = ((JTextField) e.getSource()).getText();
				int code;
				try {
					code = Integer.parseInt(codeHint);
				} catch (NumberFormatException e1) {
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseinsertavalidnumber.msg");
					return;
				}
				progYearFilter.setText("");
				patientCodeFilter.setText("");
				Optional<Opd> opd = opdBrowserManager.getOpdById(code);
				pSur = new ArrayList<>();
				if (opd.isPresent()) {
					pSur.add(opd.get());
				}
				totalRows = pSur.size();
				totalPages = calculatePages(totalRows);
				currentPage = 0;
				searchMode = SearchMode.FILTERS;
				refreshModel();
				updateCounters();

				if (pSur.isEmpty()) {
					MessageDialog.info(OpdBrowser.this, "angal.common.nodatatoshow.msg");
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

	class SearchByProgYearListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				String codeHint = ((JTextField) e.getSource()).getText();
				int code;
				try {
					code = Integer.parseInt(codeHint);
				} catch (NumberFormatException e1) {
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseinsertavalidnumber.msg");
					return;
				}
				opdCodeFilter.setText("");
				patientCodeFilter.setText("");
				pSur = opdBrowserManager.getOpdByProgYear(code);
				totalRows = pSur.size();
				totalPages = calculatePages(totalRows);
				currentPage = 0;
				searchMode = SearchMode.FILTERS;
				refreshModel();
				updateCounters();
				if (pSur.isEmpty()) {
					MessageDialog.info(OpdBrowser.this, "angal.common.nodatatoshow.msg");
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}

	class SearchByPatientIdListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				String codeHint = ((JTextField) e.getSource()).getText();
				int code;
				try {
					code = Integer.parseInt(codeHint);
				} catch (NumberFormatException e1) {
					MessageDialog.error(OpdBrowser.this, "angal.common.pleaseinsertavalidnumber.msg");
					return;
				}
				opdCodeFilter.setText("");
				progYearFilter.setText("");
				try {
					pSur = opdBrowserManager.getOpdList(code);
					totalRows = pSur.size();
					totalPages = calculatePages(totalRows);
					currentPage = 0;
					searchMode = SearchMode.FILTERS;
					refreshModel();
					updateCounters();

					if (pSur.isEmpty()) {
						MessageDialog.info(OpdBrowser.this, "angal.common.nodatatoshow.msg");
					}
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}
	private JButton getJExamButton() {
		if (jExamButton == null) {
			jExamButton = new JButton(MessageBundle.getMessage("angal.opd.exams.btn"));
			jExamButton.setMnemonic(KeyEvent.VK_X);
			jExamButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Opd opd = (Opd) model.getValueAt(jTable.getSelectedRow(), -1);
				Patient patient = opd.getPatient();
				if (patient == null) {
					MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
					return;
				}
				LabNew labNew = new LabNew(myFrame, patient);
				labNew.setVisible(true);
			});
		}
		return jExamButton;
	}
}
