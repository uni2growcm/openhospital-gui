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
package org.isf.patvac.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.patvac.manager.PatVacManager;
import org.isf.patvac.model.PatientVaccine;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.pagination.PageInfo;
import org.isf.utils.pagination.PagedResponse;
import org.isf.utils.time.TimeTools;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * PatVacBrowser - list all patient's vaccines
 */
public class PatVacBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private VaccineTypeBrowserManager vaccineTypeBrowserManager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);
	private	VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
	private PatVacManager patVacManager = Context.getApplicationContext().getBean(PatVacManager.class);

	private JPanel jContentPane;
	private JPanel jButtonPanel;
	private JButton buttonEdit;
	private JButton buttonNew;
	private JButton buttonDelete;
	private JButton buttonClose;
	private JButton filterButton;
	private JPanel jSelectionPanel;
	private JPanel jAgePanel;
	private VoLimitedTextField jAgeFromTextField;
	private VoLimitedTextField jAgeToTextField;
	private Integer ageTo = 0;
	private Integer ageFrom = 0;
	private JPanel sexPanel;
	private JRadioButton radiom;
	private JRadioButton radiof;
	private JLabel rowCounter;
	private String rowCounterText = MessageBundle.getMessage("angal.patvac.count") + ": ";

	private JPanel paginationPanel;
	private JButton previousPageButton;
	private JButton nextPageButton;
	private JComboBox<Integer> pagesComboBox = new JComboBox<>();
	private JLabel ofPagesLabel = new JLabel(MessageBundle.formatMessage("angal.common.pages.fmt.txt", 1));
	private int currentPage;
	private boolean filterApplied;
	private boolean updatingPagesComboProgrammatically;
	private PageInfo lastPageInfo;

	private JTable jTable;
	private JComboBox vaccineComboBox;
	private JComboBox vaccineTypeComboBox;
	private int pfrmHeight;
	private List<PatientVaccine> lPatVac;

	// Patient filter
	private JComboBox<Patient> patientFilterComboBox;
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private List<Patient> filteredPatientsForFilter;
	private boolean updatingPatientComboProgrammatically;

	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.patvac.vaccine.col").toUpperCase(),
			MessageBundle.getMessage("angal.patvac.vaccinetype.col").toUpperCase()
	};
	private int[] pColumnWidth = {100, 150, 50, 50, 150, 150};
	private boolean[] columnsVisible = {true, GeneralData.PATIENTVACCINEEXTENDED, true, true, true, true};
	private PatVacBrowsingModel model;
	private PatientVaccine patientVaccine;
	private int selectedrow;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private JPanel dateFilterPanel;
	private final JFrame myFrame;

	public PatVacBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	/**
	 * This method initializes this Frame, sets the correct Dimensions
	 */
	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.patvac.patientvaccinebrowser.title"));
		this.setContentPane(getJContentPane());
		setPreferredSize(new Dimension(1680, 670));
		setMinimumSize(new Dimension(880, 510));
		pack();
		updateRowCounter();
		updatePaginationControls();
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane, adds the main parts of the frame
	 *
	 * @return jContentPanel (JPanel)
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJSelectionPanel(), BorderLayout.WEST);
			jContentPane.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
			validate();
		}
		return jContentPane;
	}


	/**
	 * This method initializes JButtonPanel, that contains the buttons of the
	 * frame (on the bottom)
	 *
	 * @return JButtonPanel (JPanel)
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new WrapLayout());
			if (MainMenu.checkUserGrants("btnpatientvaccinenew")) {
				jButtonPanel.add(getButtonNew(), null);
			}
			if (MainMenu.checkUserGrants("btnpatientvaccineedit")) {
				jButtonPanel.add(getButtonEdit(), null);
			}
			if (MainMenu.checkUserGrants("btnpatientvaccinedel")) {
				jButtonPanel.add(getButtonDelete(), null);
			}
			jButtonPanel.add(getCloseButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes buttonNew, that loads patientVaccineEdit Mask
	 *
	 * @return buttonNew (JButton)
	 */
	private JButton getButtonNew() {
		if (buttonNew == null) {
			buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			buttonNew.addActionListener(actionEvent -> {
				LocalDateTime now = TimeTools.getNow();
				patientVaccine = new PatientVaccine(0, 0, now, new Patient(),
						new Vaccine("", "", new VaccineType("", "")), 0);

				PatientVaccine last = new PatientVaccine(0, 0, now, new Patient(),
						new Vaccine("", "", new VaccineType("", "")), 0);
				new PatVacEdit(myFrame, patientVaccine, true);

				if (!last.equals(patientVaccine)) {
					currentPage = 0;
					refreshCurrentPage();
					if (jTable.getRowCount() > 0) {
						jTable.setRowSelectionInterval(0, 0);
					}
				}
			});
		}
		return buttonNew;
	}

	/**
	 * This method initializes buttonEdit, that loads patientVaccineEdit Mask
	 *
	 * @return buttonEdit (JButton)
	 */
	private JButton getButtonEdit() {
		if (buttonEdit == null) {
			buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			buttonEdit.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}

				selectedrow = jTable.getSelectedRow();
				patientVaccine = (PatientVaccine) model.getValueAt(selectedrow, -1);

				PatientVaccine last = new PatientVaccine(patientVaccine.getCode(),
						patientVaccine.getProgr(),
						patientVaccine.getVaccineDate(),
						patientVaccine.getPatient(),
						patientVaccine.getVaccine(),
						patientVaccine.getLock());

				new PatVacEdit(myFrame, patientVaccine, false);

				if (!last.equals(patientVaccine)) {
					refreshCurrentPage();
					if (jTable.getRowCount() > 0 && selectedrow > -1) {
						jTable.setRowSelectionInterval(selectedrow, selectedrow);
					}
				}
			});
		}
		return buttonEdit;
	}

	/**
	 * This method initializes buttonDelete, that loads patientVaccineEdit Mask
	 *
	 * @return buttonDelete (JButton)
	 */
	private JButton getButtonDelete() {
		if (buttonDelete == null) {
			buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			buttonDelete.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedrow = jTable.getSelectedRow();
				patientVaccine = (PatientVaccine) model.getValueAt(selectedrow, -1);
				int answer = MessageDialog.yesNo(null, "angal.patvac.deletepatientvaccine.fmt.msg",
						patientVaccine.getVaccineDate().format(DATE_FORMATTER),
						patientVaccine.getVaccine().getDescription(),
						patientVaccine.getPatName());

				if (answer == JOptionPane.YES_OPTION) {
					try {
						patVacManager.deletePatientVaccine(patientVaccine);
						refreshCurrentPage();
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			});
		}
		return buttonDelete;
	}

	/**
	 * This method initializes buttonClose
	 *
	 * @return buttonClose (JButton)
	 */
	private JButton getCloseButton() {
		if (buttonClose == null) {
			buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			buttonClose.addActionListener(actionEvent -> dispose());
		}
		return buttonClose;
	}

	/**
	 * This method initializes JSelectionPanel, that contains the filter objects
	 *
	 * @return JSelectionPanel (JPanel)
	 */
	private JPanel getJSelectionPanel() {
		if (jSelectionPanel == null) {
			jSelectionPanel = new JPanel();
			jSelectionPanel.setPreferredSize(new Dimension(225, pfrmHeight));

			jSelectionPanel.add(getVaccineTypePanel());
			jSelectionPanel.add(getVaccinePanel());

			jSelectionPanel.add(getPatientPanel());

			jSelectionPanel.add(getDateFilterPanel());
			jSelectionPanel.add(getAgePanel());

			jSelectionPanel.add(getSexPanel());
			jSelectionPanel.add(getFilterPanel());
			jSelectionPanel.add(getRowCounterPanel());
			jSelectionPanel.add(getPaginationPanel());
		}
		return jSelectionPanel;
	}

	/**
	 * This method initializes getVaccineTypePanel
	 *
	 * @return vaccineTypePanel  (JPanel)
	 */
	private JPanel getVaccineTypePanel() {

		JPanel vaccineTypePanel = new JPanel();

		vaccineTypePanel.setLayout(new BoxLayout(vaccineTypePanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel(MessageBundle.getMessage("angal.patvac.selectavaccinetype")));
		vaccineTypePanel.add(label1Panel);

		label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getComboVaccineTypes());
		vaccineTypePanel.add(label1Panel, null);
		return vaccineTypePanel;
	}

	/**
	 * This method initializes getVaccinePanel
	 *
	 * @return vaccinePanel  (JPanel)
	 */
	private JPanel getVaccinePanel() {

		JPanel vaccinePanel = new JPanel();

		vaccinePanel.setLayout(new BoxLayout(vaccinePanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel(MessageBundle.getMessage("angal.patvac.selectavaccine")));
		vaccinePanel.add(label1Panel);

		label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getComboVaccines());
		vaccinePanel.add(label1Panel, null);
		return vaccinePanel;
	}

	/**
	 * This method initializes getAgePanel
	 *
	 * @return jAgePanel  (JPanel)
	 */
	private JPanel getAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.setLayout(new BoxLayout(getAgePanel(), BoxLayout.Y_AXIS));

			JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			label1Panel.add(new JLabel(MessageBundle.getMessage("angal.common.agefrom.label")), null);
			jAgePanel.add(label1Panel);
			label1Panel.add(getJAgeFromTextField(), null);
			jAgePanel.add(label1Panel);

			label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			label1Panel.add(new JLabel(MessageBundle.getMessage("angal.common.ageto.label")), null);
			jAgePanel.add(label1Panel);
			label1Panel.add(getJAgeToTextField(), null);
			jAgePanel.add(label1Panel);
		}
		return jAgePanel;
	}

	/**
	 * This method initializes getSexPanel
	 *
	 * @return sexPanel  (JPanel)
	 */
	public JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			sexPanel.setLayout(new BoxLayout(sexPanel, BoxLayout.Y_AXIS));
			JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			label1Panel.add(new JLabel(MessageBundle.getMessage("angal.common.selectsex.txt")), null);
			sexPanel.add(label1Panel);

			label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			ButtonGroup group = new ButtonGroup();
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			JRadioButton radioa = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioa.setSelected(true);
			group.add(radiom);
			group.add(radiof);
			group.add(radioa);

			label1Panel.add(radioa);
			sexPanel.add(label1Panel);
			label1Panel.add(radiom);
			sexPanel.add(label1Panel);
			label1Panel.add(radiof);
			sexPanel.add(label1Panel);
		}
		return sexPanel;
	}

	/**
	 * This method initializes getFilterPanel
	 *
	 * @return filterPanel  (JPanel)
	 */
	private JPanel getFilterPanel() {

		JPanel filterPanel = new JPanel();
		filterPanel.setPreferredSize(new Dimension(225, 30));
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getFilterButton());
		filterPanel.add(label1Panel);
		return filterPanel;
	}

	/**
	 * This method initializes getRowCounterPanel
	 *
	 * @return rowCounterPanel  (JPanel)
	 */
	private JPanel  getRowCounterPanel() {

		JPanel rowCounterPanel = new JPanel();

		rowCounterPanel.setLayout(new BoxLayout(rowCounterPanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		rowCounter = new JLabel(MessageBundle.getMessage("angal.patvac.rowcounter"));
		label1Panel.add(rowCounter, null);
		rowCounterPanel.add(label1Panel);
		return rowCounterPanel;
	}

	/**
	 * This method initializes the pagination control panel: Previous/Next buttons, an editable
	 * page-number combo box, and a "/ N Pages" label — the same shape already used by
	 * {@code InventoryBrowser}, unlike that screen's combo box this one is cleared before every
	 * repopulation to avoid accumulating stale/duplicate page numbers across filter changes.
	 *
	 * @return paginationPanel (JPanel)
	 */
	private JPanel getPaginationPanel() {
		if (paginationPanel == null) {
			paginationPanel = new JPanel();
			paginationPanel.setLayout(new BoxLayout(paginationPanel, BoxLayout.Y_AXIS));

			previousPageButton = new JButton(MessageBundle.getMessage("angal.common.previouspage.btn"));
			previousPageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			previousPageButton.addActionListener(actionEvent -> {
				if (currentPage > 0) {
					currentPage--;
					refreshCurrentPage();
				}
			});

			nextPageButton = new JButton(MessageBundle.getMessage("angal.common.nextpage.btn"));
			nextPageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			nextPageButton.addActionListener(actionEvent -> {
				currentPage++;
				refreshCurrentPage();
			});

			pagesComboBox.setEditable(true);
			pagesComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
			pagesComboBox.addItemListener(itemEvent -> {
				if (updatingPagesComboProgrammatically || itemEvent.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				Object selected = pagesComboBox.getSelectedItem();
				if (selected instanceof Integer pageNumber) {
					currentPage = pageNumber - 1;
					refreshCurrentPage();
				}
			});

			ofPagesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

			JPanel navRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
			navRow.add(previousPageButton);
			navRow.add(nextPageButton);
			navRow.setAlignmentX(Component.CENTER_ALIGNMENT);

			JPanel comboRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
			comboRow.add(pagesComboBox);
			comboRow.add(ofPagesLabel);
			comboRow.setAlignmentX(Component.CENTER_ALIGNMENT);

			paginationPanel.add(navRow);
			paginationPanel.add(comboRow);
		}
		return paginationPanel;
	}

	private Patient allPatientsPlaceholder;

	private Patient getAllPatientsPlaceholder() {
		if (allPatientsPlaceholder == null) {
			allPatientsPlaceholder = new Patient();
			// Patient.equals() dereferences getCode() unconditionally (this.getCode().equals(...)), so a
			// null code here would NPE the moment Swing compares combo items (e.g. clicking a popup row) -
			// use 0 as the "no real patient" sentinel instead (never a real DB identity value), matching
			// the code == 0 checks in buildModelForCurrentPage()/displayNameFor().
			allPatientsPlaceholder.setCode(0);
			allPatientsPlaceholder.setFirstName(MessageBundle.getMessage("angal.patvac.allpatients"));
		}
		return allPatientsPlaceholder;
	}

	/**
	 * Builds the table model for {@link #currentPage}: the default (no-filter) view before any search,
	 * or the active filter criteria once {@link #filterApplied} is set by {@link #getFilterButton()}.
	 */
	private PatVacBrowsingModel buildModelForCurrentPage() {
		if (!filterApplied) {
			return new PatVacBrowsingModel();
		}

		Object selected = patientFilterComboBox.getSelectedItem();
		Integer patientCode = null;
		if (selected instanceof Patient) {
			Patient p = (Patient) selected;
			if (p.getCode() != null && p.getCode() != 0) {
				patientCode = p.getCode();
			}
		}

		String vaccineTypeCode = ((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode();
		String vaccineCode = ((Vaccine) vaccineComboBox.getSelectedItem()).getCode();

		if (vaccineTypeComboBox.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.patvac.allvaccinetype"))) {
			vaccineTypeCode = null;
		}
		if (vaccineComboBox.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.patvac.allvaccine"))) {
			vaccineCode = null;
		}
		char sex;
		if (radiof.isSelected()) {
			sex = 'F';
		} else if (radiom.isSelected()) {
			sex = 'M';
		} else {
			sex = 'A';
		}

		return new PatVacBrowsingModel(patientCode, vaccineTypeCode, vaccineCode, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay(), sex, ageFrom, ageTo);
	}

	/**
	 * Re-fetches {@link #currentPage} from the server (rather than patching the in-memory list) so the
	 * displayed rows and the true total always stay correct under pagination — used after a search, a
	 * page-navigation action, and after insert/edit/delete.
	 */
	private void refreshCurrentPage() {
		model = buildModelForCurrentPage();
		if (lastPageInfo != null && currentPage > 0 && currentPage >= lastPageInfo.getTotalPages()) {
			// the page we asked for no longer exists (e.g. deleted the last row on the last page) - step back
			currentPage = Math.max(0, lastPageInfo.getTotalPages() - 1);
			model = buildModelForCurrentPage();
		}
		jTable.setModel(model);
		updateRowCounter();
		updatePaginationControls();
	}

	/**
	 * Refreshes the page-number combo box and the Previous/Next buttons' enabled state from
	 * {@link #lastPageInfo}.
	 */
	private void updatePaginationControls() {
		updatingPagesComboProgrammatically = true;
		try {
			pagesComboBox.removeAllItems();
			int totalPages = lastPageInfo != null ? Math.max(1, lastPageInfo.getTotalPages()) : 1;
			for (int i = 1; i <= totalPages; i++) {
				pagesComboBox.addItem(i);
			}
			pagesComboBox.setSelectedItem(currentPage + 1);
			ofPagesLabel.setText(MessageBundle.formatMessage("angal.common.pages.fmt.txt", totalPages));
			previousPageButton.setEnabled(lastPageInfo != null && lastPageInfo.isHasPreviousPage());
			nextPageButton.setEnabled(lastPageInfo != null && lastPageInfo.isHasNextPage());
		} finally {
			updatingPagesComboProgrammatically = false;
		}
	}

	/**
	 * This method initializes jAgeFromTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeFromTextField() {
		if (jAgeFromTextField == null) {
			jAgeFromTextField = new VoLimitedTextField(3, 2);
			jAgeFromTextField.setText("0");
			jAgeFromTextField.setMinimumSize(new Dimension(100, 50));
			ageFrom = 0;
			jAgeFromTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
						if (ageFrom < 0 || ageFrom > 200) {
							jAgeFromTextField.setText("0");
							ageFrom = Integer.parseInt(jAgeFromTextField.getText());
							MessageDialog.error(null, "angal.patvac.insertvalidage");
						}
					} catch (NumberFormatException ex) {
						jAgeFromTextField.setText("0");
						ageFrom = Integer.parseInt(jAgeFromTextField.getText());
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
	 * This method initializes jTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private VoLimitedTextField getJAgeToTextField() {
		if (jAgeToTextField == null) {
			jAgeToTextField = new VoLimitedTextField(3, 2);
			jAgeToTextField.setText("0");
			jAgeToTextField.setMaximumSize(new Dimension(100, 50));
			ageTo = 0;
			jAgeToTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						ageTo = Integer.parseInt(jAgeToTextField.getText());
						if (ageTo < 0 || ageTo > 200) {
							jAgeToTextField.setText("0");
							ageTo = Integer.parseInt(jAgeToTextField.getText());
							MessageDialog.error(null, "angal.patvac.insertvalidage");
						}
						if (ageFrom > ageTo) {
							MessageDialog.error(null, "angal.patvac.agefrommustbelowerthanageto");
							jAgeFromTextField.setText(ageTo.toString());
							ageFrom = ageTo;
						}
					} catch (NumberFormatException ex) {
						jAgeToTextField.setText("0");
						ageTo = Integer.parseInt(jAgeToTextField.getText());
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
	 * This method initializes getComboVaccineTypes
	 *
	 * @return vaccineTypeComboBox (jComboBox)
	 */
	private JComboBox getComboVaccineTypes() {
		if (vaccineTypeComboBox == null) {
			vaccineTypeComboBox = new JComboBox();
			vaccineTypeComboBox.setPreferredSize(new Dimension(200, 30));
			vaccineTypeComboBox.addItem(new VaccineType("", MessageBundle.getMessage("angal.patvac.allvaccinetype")));

			List<VaccineType> types = null;
			try {
				types = vaccineTypeBrowserManager.getVaccineType();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			if (types != null) {
				for (VaccineType elem : types) {
					vaccineTypeComboBox.addItem(elem);
				}
			}

			vaccineTypeComboBox.addActionListener(actionEvent -> {
				vaccineComboBox.removeAllItems();
				getComboVaccines();
			});
		}
		return vaccineTypeComboBox;
	}

	/**
	 * This method initializes comboVaccine.
	 * It used to display available vaccine
     *
	 * @return vaccineComboBox (JComboBox)
	 */
	private JComboBox getComboVaccines() {
		if (vaccineComboBox == null) {
			vaccineComboBox = new JComboBox();
			vaccineComboBox.setPreferredSize(new Dimension(200, 30));
		}

		List<Vaccine> allVac = null;
		vaccineComboBox.addItem(new Vaccine("", MessageBundle.getMessage("angal.patvac.allvaccine"), new VaccineType("", "")));
		try {
			if (((VaccineType) vaccineTypeComboBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.patvac.allvaccinetype"))) {
				allVac = vaccineBrowserManager.getVaccine();
			} else {
				allVac = vaccineBrowserManager.getVaccine(((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode());
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		if (allVac != null) {
			for (Vaccine elem : allVac) {
				vaccineComboBox.addItem(elem);
			}
		}
		return vaccineComboBox;
	}

	private Component getDateFilterPanel() {
		if (dateFilterPanel == null) {
			dateFilterPanel = new JPanel(new SpringLayout());
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			if (!GeneralData.ENHANCEDSEARCH) {
				dateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
			} else {
				dateFrom = new GoodDateChooser(LocalDate.now());
			}
			dateFilterPanel.add(dateFrom);
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			dateTo = new GoodDateChooser(LocalDate.now());
			dateFilterPanel.add(dateTo);
			SpringUtilities.makeCompactGrid(dateFilterPanel, 2, 2, 5, 5, 5, 5);
		}
		return dateFilterPanel;
	}

	/**
	 * This method initializes getPatientPanel
	 *
	 * @return patientPanel  (JPanel)
	 */
	private JPanel getPatientPanel() {

		JPanel patientPanel = new JPanel();

		patientPanel.setLayout(new BoxLayout(patientPanel, BoxLayout.Y_AXIS));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel(MessageBundle.getMessage("angal.patvac.selectapatient")));
		patientPanel.add(label1Panel);

		label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(getComboPatients());
		patientPanel.add(label1Panel, null);
		return patientPanel;
	}

	/**
	 * This method initializes patientFilterComboBox as an editable autocomplete field:
	 * on open it shows the first {@code GeneralData.PAGESIZE} patients; as the user types,
	 * the dropdown is repopulated via {@code patientBrowserManager.getPatientsByOneOfFieldsLike}
	 * (same search used by {@link PatVacEdit}).
	 *
	 * @return patientFilterComboBox (JComboBox)
	 */
	private JComboBox<Patient> getComboPatients() {
		if (patientFilterComboBox == null) {
			patientFilterComboBox = new JComboBox<>();
			patientFilterComboBox.setEditable(true);
			patientFilterComboBox.setPreferredSize(new Dimension(200, 30));

			patientFilterComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
				JLabel label = new JLabel();
				label.setOpaque(true);
				if (value != null) {
					label.setText(displayNameFor(value));
				}
				if (isSelected) {
					label.setBackground(list.getSelectionBackground());
					label.setForeground(list.getSelectionForeground());
				} else {
					label.setBackground(list.getBackground());
					label.setForeground(list.getForeground());
				}
				return label;
			});

			JTextField editorField = new JTextField();
			patientFilterComboBox.setEditor(new javax.swing.ComboBoxEditor() {
				private Object current;

				@Override
				public Component getEditorComponent() {
					return editorField;
				}

				@Override
				public void setItem(Object anObject) {
					current = anObject;
					if (anObject == null) {
						editorField.setText("");
					} else if (anObject instanceof Patient) {
						editorField.setText(displayNameFor((Patient) anObject));
					} else {
						editorField.setText(anObject.toString());
					}
				}

				@Override
				public Object getItem() {
					return current;
				}

				@Override
				public void selectAll() {
					editorField.selectAll();
				}

				@Override
				public void addActionListener(java.awt.event.ActionListener l) {
					editorField.addActionListener(l);
				}

				@Override
				public void removeActionListener(java.awt.event.ActionListener l) {
					editorField.removeActionListener(l);
				}
			});

			loadPatientsIntoFilterCombo(null);

			// debounced search: avoids firing an unbounded DB query on every keystroke
			Runnable doSearch = () -> {
				String typed = editorField.getText();
				loadPatientsIntoFilterCombo(typed);
				editorField.setText(typed);
				editorField.requestFocus();
				if (patientFilterComboBox.getItemCount() > 0) {
					patientFilterComboBox.showPopup();
				}
			};
			Timer patientFilterSearchTimer = new Timer(1000, e -> doSearch.run());
			patientFilterSearchTimer.setRepeats(false);

			// clicking into the field while it shows the placeholder clears it, ready for typing;
			// leaving it empty afterward resets the filter to "all patients"
			editorField.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					if (editorField.getText().equals(displayNameFor(getAllPatientsPlaceholder()))) {
						editorField.setText("");
					}
				}

				@Override
				public void focusLost(FocusEvent e) {
					if (editorField.getText().trim().isEmpty()) {
						patientFilterSearchTimer.stop();
						loadPatientsIntoFilterCombo(null);
					}
				}
			});

			editorField.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						e.consume(); // empêche le binding natif "accepter & fermer popup" de s'exécuter juste après
						patientFilterSearchTimer.stop();
						doSearch.run();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					int code = e.getKeyCode();
					if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN
							|| code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
						return;
					}
					patientFilterSearchTimer.restart();
				}
			});

			patientFilterComboBox.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					// la fermeture du popup laisse parfois un artefact de rendu sur les composants
					// qu'il recouvrait (ex: le panneau de dates juste en dessous) - forcer un repaint
					SwingUtilities.invokeLater(() -> {
						jSelectionPanel.revalidate();
						jSelectionPanel.repaint();
					});
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}
			});
		}
		return patientFilterComboBox;
	}

	/**
	 * (Re)loads the dropdown content: full/limited list when {@code searchKey} is blank,
	 * matching patients otherwise.
	 */
	private void loadPatientsIntoFilterCombo(String searchKey) {
		updatingPatientComboProgrammatically = true;
		try {
			patientFilterComboBox.removeAllItems();
			patientFilterComboBox.addItem(getAllPatientsPlaceholder());

			try {
				if (searchKey == null || searchKey.trim().isEmpty()) {
					filteredPatientsForFilter = patientBrowserManager.getPatient(0, GeneralData.PAGESIZE);
				} else {
					filteredPatientsForFilter = patientBrowserManager.getPatientsByOneOfFieldsLike(searchKey.trim());
				}
			} catch (OHServiceException e) {
				filteredPatientsForFilter = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}

			if (filteredPatientsForFilter != null) {
				for (Patient elem : filteredPatientsForFilter) {
					patientFilterComboBox.addItem(elem);
				}
			}
		} finally {
			updatingPatientComboProgrammatically = false;
		}
	}

	private String displayNameFor(Patient p) {
		if (p == null) {
			return "";
		}
		if (p.getCode() == null || p.getCode() == 0) {
			return p.getFirstName(); // "All patients" placeholder
		}
		return p.getName(); // adjust if Patient exposes a different full-name getter
	}

	/**
	 * This method initializes filterButton, which is the button that performs
	 * the filtering and calls the methods to refresh the Table
	 *
	 * @return filterButton (JButton)
	 */
	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(actionEvent -> {

				if (dateFrom.getDate() == null) {
					MessageDialog.error(null, "angal.patvac.pleaseinsertvaliddatefrom");
					return;
				}

				if (dateTo.getDate() == null) {
					MessageDialog.error(null, "angal.patvac.pleaseinsertvaliddateto");
					return;
				}

				filterApplied = true;
				currentPage = 0;
				refreshCurrentPage();
			});
		}
		return filterButton;
	}

	/**
	 * This method initializes jTable, that contains the information about the
	 * patient's vaccines
	 *
	 * @return jTable (JTable)
	 */
	private JTable getJTable() {
		if (jTable == null) {
			model = new PatVacBrowsingModel();
			jTable = new JTable(model);
			TableColumnModel columnModel = jTable.getColumnModel();
			if (GeneralData.PATIENTVACCINEEXTENDED) {
				columnModel.getColumn(0).setMinWidth(pColumnWidth[0]);
				columnModel.getColumn(1).setMinWidth(pColumnWidth[1]);
				columnModel.getColumn(2).setMinWidth(pColumnWidth[2]);
				columnModel.getColumn(3).setMinWidth(pColumnWidth[3]);
				columnModel.getColumn(4).setMinWidth(pColumnWidth[4]);
				columnModel.getColumn(5).setMinWidth(pColumnWidth[5]);
			} else {
				columnModel.getColumn(0).setMinWidth(pColumnWidth[0]);
				columnModel.getColumn(1).setMaxWidth(pColumnWidth[2]);
				columnModel.getColumn(2).setMinWidth(pColumnWidth[3]);
				columnModel.getColumn(3).setMinWidth(pColumnWidth[4]);
				columnModel.getColumn(4).setMinWidth(pColumnWidth[5]);
			}
		}
		return jTable;
	}

	/**
	 * This class defines the model for the Table
	 */
	class PatVacBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public PatVacBrowsingModel() {
			try {
				PagedResponse<PatientVaccine> response = patVacManager.getPatientVaccinePageable(!GeneralData.ENHANCEDSEARCH, currentPage,
						GeneralData.PAGESIZE);
				lPatVac = response.getData();
				lastPageInfo = response.getPageInfo();
			} catch (OHServiceException e) {
				lPatVac = null;
				lastPageInfo = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public PatVacBrowsingModel(Integer patientCode, String vaccineTypeCode, String vaccineCode, LocalDateTime dateFrom, LocalDateTime dateTo,
		                           char sex, int ageFrom, int ageTo) {
			try {
				PagedResponse<PatientVaccine> response = patVacManager.getPatientVaccinePageable(patientCode, vaccineTypeCode, vaccineCode, dateFrom, dateTo,
						sex, ageFrom, ageTo, currentPage, GeneralData.PAGESIZE);
				lPatVac = response.getData();
				lastPageInfo = response.getPageInfo();
			} catch (OHServiceException e) {
				lPatVac = null;
				lastPageInfo = null;
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (lPatVac == null) {
				return 0;
			}
			return lPatVac.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[getNumber(c)];
		}

		@Override
		public int getColumnCount() {
			int c = 0;
			for (boolean b : columnsVisible) {
				if (b) {
					c++;
				}
			}
			return c;
		}

		/**
		 * This method converts a column number in the table
		 * to the right number in the data.
		 */
		protected int getNumber(int col) {
			// right number to return
			int n = col;
			int i = 0;
			do {
				if (!columnsVisible[i]) {
					n++;
				}
				i++;
			} while (i < n);
			// If we are on an invisible column,
			// we have to go one step further
			while (!columnsVisible[n]) {
				n++;
			}
			return n;
		}

		@Override
		public Object getValueAt(int r, int c) {
			PatientVaccine patVac = lPatVac.get(r);
			if (c == -1) {
				return patVac;
			} else if (getNumber(c) == 0) {
				return patVac.getVaccineDate().format(DATE_FORMATTER);
			} else if (getNumber(c) == 1) {
				return patVac.getPatient().getName();
			} else if (getNumber(c) == 2) {
				return patVac.getPatSex();
			} else if (getNumber(c) == 3) {
				return patVac.getPatAge();
			} else if (getNumber(c) == 4) {
				return patVac.getVaccine().getDescription();
			} else if (getNumber(c) == 5) {
				return patVac.getVaccine().getVaccineType().getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

	}

	private void updateRowCounter() {
		long total = lastPageInfo != null ? lastPageInfo.getTotalNbOfElements() : 0;
		rowCounter.setText(rowCounterText + total);
	}

}
