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
package org.isf.lab.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
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
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import net.sf.jasperreports.engine.JRException;

import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.LabEdit.LabEditListener;
import org.isf.lab.gui.LabEditExtended.LabEditExtendedListener;
import org.isf.lab.gui.LabNew.LabListener;
import org.isf.lab.manager.LabManager;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryForPrint;
import org.isf.lab.model.LaboratoryResultFilter;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.serviceprinting.manager.PrintLabels;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.pagination.PagedResponse;
import org.isf.utils.time.TimeTools;

/**
 * LabBrowser - list all labs
 */
public class LabBrowser extends ModalJFrame implements LabListener, LabEditListener, LabEditExtendedListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void labInserted() {
		currentPage = 0;
		loadCurrentPage();
		if (withPaid)
			updateTotals();
	}

	@Override
	public void labUpdated() {
		loadCurrentPage();
		if (withPaid)
			updateTotals();
	}

	@Override
	public void prescribersUpdated() {
		refreshPrescriberCombo();
	}

	private JPanel jContentPane;
	private JPanel jButtonPanel;
	private JButton buttonEdit;
	private JButton buttonNew;
	private JButton buttonDelete;
	private JButton buttonClose;
	private JButton printTableButton;
	private JButton filterButton;
	private JPanel jSelectionPanel;
	private JTable jTable;
	private JComboBox comboExams;
	private JComboBox<String> comboPrescriber;
	private JTextField patientCodeField;
	private int pfrmHeight = 100;
	private List<Laboratory> pLabs;
	private Patient pPatient;
	private boolean patientFilterActive;
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private final int PAGE_SIZE = GeneralData.PAGINATIONPAGESIZE;
	private JPanel paginationPanel;
	private JButton prevPageButton;
	private JButton nextPageButton;
	private JComboBox<Integer> pageCombo;
	private JLabel pageInfoLabel;
	private JLabel totalElementsLabel;
	private boolean updatingPageCombo;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.exam.txt").toUpperCase(),
			MessageBundle.getMessage("angal.lab.prescriber.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.result.txt").toUpperCase()
	};
	private String[] pColumnsWithPaid = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.exam.txt").toUpperCase(),
			MessageBundle.getMessage("angal.lab.prescriber.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.result.txt").toUpperCase(),
			MessageBundle.getMessage("angal.lab.paid").toUpperCase()
	};
	private JComboBox<String> comboResultFilter;
	private static final String FILTER_ALL = MessageBundle.getMessage("angal.lab.result.filter.all");
	private static final String FILTER_NON_EMPTY = MessageBundle.getMessage("angal.lab.result.filter.nonempty");
	private static final String FILTER_EMPTY = MessageBundle.getMessage("angal.lab.result.filter.empty");
	private static final String FILTER_PAID_SELECT = MessageBundle.getMessage("angal.laboratory.selectpaidstatus");
	private static final String FILTER_PAID = MessageBundle.getMessage("angal.laboratory.paid");
	private static final String FILTER_NOT_PAID = MessageBundle.getMessage("angal.laboratory.notpaid");
	private static final String FILTER_NOT_FACTURED = MessageBundle.getMessage("angal.lab.notfactured");
	private boolean[] columnsResizable = {false, true, true, true, false};
	private boolean[] columnsResizableWithPaid = {false, true, true, true, false, false};
	private int[] pColumnWidth = {150, 200, 200, 150, 200};
	private int[] pColumnWidthWithPaid = {150, 200, 200, 150, 200, 90};
	private int[] maxWidth = {150, 200, 200, 150, 200};
	private int[] maxWidthWithPaid = {150, 200, 200, 150, 200, 90};
	private boolean[] columnsVisible = { true, GeneralData.LABEXTENDED, true, true, true};
	private boolean[] columnsVisibleWithPaid = { true, GeneralData.LABEXTENDED, true, true, true, true};
	private JComboBox<String> paidComboBox;
	private JPanel panelTotal;
	private JLabel totalPaidLabel;
	private JLabel totalPaidValueLabel;
	private JLabel totalNotPaidLabel;
	private JLabel totalNotPaidValueLabel;
	private JLabel totalNotFacturedLabel;
	private JLabel totalNotFacturedValueLabel;
	private boolean withPaid = GeneralData.CREATELABORATORYAUTO;
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private PatientBrowserManager patManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private LabBrowsingModel model;
	private Laboratory laboratory;
	private int selectedrow;
	private String typeSelected;
	private JPanel dateFilterPanel;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private final JFrame myFrame;
	private JButton printLabelButton;

	/**
	 * This is the default constructor
	 */
	public LabBrowser() {
		super();
		myFrame = this;
		this.setTitle(MessageBundle.getMessage("angal.lab.laboratorybrowser.title"));
		this.setContentPane(getJContentPane());
		setSize(new Dimension(1345, 650));
		setResizable(false);
		loadCurrentPage();
		setVisible(true);
		setLocationRelativeTo(null);
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
			JPanel centerPanel = new JPanel(new BorderLayout());
			centerPanel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
			centerPanel.add(getPaginationPanel(), BorderLayout.SOUTH);
			jContentPane.add(centerPanel, BorderLayout.CENTER);
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
			jButtonPanel = new JPanel();
			jButtonPanel.setLayout(new BorderLayout());
			if (withPaid) {
				jButtonPanel.add(getPanelTotal(), BorderLayout.NORTH);
			}
			JPanel buttonPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnlaboratorynew")) {
				buttonPanel.add(getButtonNew(), null);
			}
			if (MainMenu.checkUserGrants("btnlaboratoryedit")) {
				buttonPanel.add(getButtonEdit(), null);
			}
			if (MainMenu.checkUserGrants("btnlaboratorydel")) {
				buttonPanel.add(getButtonDelete(), null);
			}
			buttonPanel.add(getPrintTableButton(), null);
			buttonPanel.add(getPrintLabelButton(), null);
			buttonPanel.add(getCloseButton(), null);
			jButtonPanel.add(buttonPanel, BorderLayout.SOUTH);
		}
		return jButtonPanel;
	}

	private JButton getPrintTableButton() {
		if (printTableButton == null) {
			printTableButton = new JButton(MessageBundle.getMessage("angal.lab.printtable.btn"));
			printTableButton.setMnemonic(MessageBundle.getMnemonic("angal.lab.printtable.btn.key"));
			printTableButton.addActionListener(actionEvent -> {
				typeSelected = comboExams.getSelectedItem().toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					typeSelected = null;
				}

				try {
					List<LaboratoryForPrint> labs;
					labs = labManager.getLaboratoryForPrint(typeSelected, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay());
					if (!labs.isEmpty()) {
						printManager.print(MessageBundle.getMessage("angal.common.laboratory.txt"), labs, 0);
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}
		return printTableButton;
	}

	private JComboBox<String> getComboResultFilter() {
		if (comboResultFilter == null) {
			comboResultFilter = new JComboBox<>();
			comboResultFilter.setPreferredSize(new Dimension(225, 30));
			comboResultFilter.addItem(FILTER_ALL);
			comboResultFilter.addItem(FILTER_NON_EMPTY);
			comboResultFilter.addItem(FILTER_EMPTY);
		}
		return comboResultFilter;
	}

	private JComboBox<String> getComboPrescriber() {
		if (comboPrescriber == null) {
			comboPrescriber = new JComboBox<>();
			comboPrescriber.setPreferredSize(new Dimension(225, 30));
			comboPrescriber.addItem(MessageBundle.getMessage("angal.lab.prescriber.all"));
			try {
				List<String> prescribers = labManager.getDistinctPrescribers();
				prescribers.forEach(comboPrescriber::addItem);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return comboPrescriber;
	}

	private JButton getPrintLabelButton() {
		if (printLabelButton == null) {
			printLabelButton = new JButton(MessageBundle.getMessage("angal.labnew.printlabel.btn"));
			printLabelButton.setMnemonic(MessageBundle.getMnemonic("angal.labnew.printlabel.btn.key"));
			printLabelButton.addActionListener(actionEvent -> {
				Integer patId = null;
				if (GeneralData.LABEXTENDED) {
					selectedrow = jTable.getSelectedRow();
					if (selectedrow < 0) {
						int ok = MessageDialog.yesNoCancel(this, "angal.lab.nopatientselectedprintempylabel.msg");
						if (ok == JOptionPane.NO_OPTION) {
							SelectPatient selectPatient = new SelectPatient(this, (Patient) null);
							selectPatient.setVisible(true);
							Patient patient = selectPatient.getPatient();
							if (patient != null) {
								patId = selectPatient.getPatient().getCode();
							} else {
								return;
							}
						}
						if (ok == JOptionPane.CANCEL_OPTION) {
							return;
						}
					} else {
						laboratory = (Laboratory) model.getValueAt(selectedrow, -1);
						patId = laboratory.getPatient().getCode();
					}
				}
				try {
					new PrintLabels("LabelForSamples", patId);
				} catch (OHException e) {
					OHServiceExceptionUtil.showMessages(new OHServiceException(new OHExceptionMessage(e.getMessage())));
				} catch (JRException e) {
					OHServiceExceptionUtil.showMessages(new OHServiceException(new OHExceptionMessage(MessageBundle.getMessage("angal.lab.noprinter.msg"))));
				}
			});
		}
		return printLabelButton;
	}

	private JButton getButtonEdit() {
		if (buttonEdit == null) {
			buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			buttonEdit.addActionListener(actionEvent -> {
				selectedrow = jTable.getSelectedRow();
				if (selectedrow < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}
				laboratory = (Laboratory) model.getValueAt(selectedrow, -1);
				if (withPaid && !GeneralData.CREATELABORATORYAUTOWITHOPENEDBILL
						&& (laboratory.getPaidStatus() == null || !laboratory.getPaidStatus().equals("C"))) {
					MessageDialog.error(null, "angal.common.notallowedtomodifies.msg");
					return;
				}
				if (GeneralData.LABEXTENDED) {
					LabEditExtended editrecord = new LabEditExtended(myFrame, laboratory, false);
					editrecord.addLabEditExtendedListener(this);
					editrecord.showAsModal(this);
				} else {
					LabEdit editrecord = new LabEdit(myFrame, laboratory, false);
					editrecord.addLabEditListener(this);
					editrecord.showAsModal(this);
				}
			});
		}
		return buttonEdit;
	}

	/**
	 * This method initializes buttonNew, that loads LabEdit Mask
	 *
	 * @return buttonNew (JButton)
	 */
	private JButton getButtonNew() {
		if (buttonNew == null) {
			buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			buttonNew.addActionListener(actionEvent -> {
				laboratory = new Laboratory(0, new Exam("", "",
						new ExamType("", ""), 0, ""),
						TimeTools.getNow(), "P", "", new Patient(), "");
				if (GeneralData.LABEXTENDED) {
					if (GeneralData.LABMULTIPLEINSERT) {
						LabNew editrecord = new LabNew(myFrame);
						editrecord.addLabListener(this);
						editrecord.setVisible(true);
					} else {
						LabEditExtended editrecord = new LabEditExtended(myFrame, laboratory, true);
						editrecord.addLabEditExtendedListener(this);
						editrecord.setVisible(true);
					}
				} else {
					LabEdit editrecord = new LabEdit(myFrame, laboratory, true);
					editrecord.addLabEditListener(this);
					editrecord.setVisible(true);
				}
			});
		}
		return buttonNew;
	}

	/**
	 * This method initializes buttonDelete, that deletes the selected records
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
				} else {
					Laboratory lab = (Laboratory) model.getValueAt(jTable.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(this, "angal.lab.deletelabexam.fmt.msg",
							lab.getCreatedDate().format(DATE_TIME_FORMATTER),
							lab.getLabDate().format(DATE_TIME_FORMATTER),
							lab.getExam(),
							lab.getPatName(),
							lab.getResult());

					if (answer == JOptionPane.YES_OPTION) {
						try {
							labManager.deleteLaboratory(lab);
							if (pLabs.size() == 1 && currentPage > 0) {
								currentPage--;
							}
							loadCurrentPage();
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
				}
			});
		}
		return buttonDelete;
	}

	/**
	 * This method initializes buttonClose, that disposes the entire Frame
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
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.searchbycodeorname")));
			jSelectionPanel.add(getPatientCodeField());
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.selectanexam")));
			jSelectionPanel.add(getComboExams());
			jSelectionPanel.add(getComboResultFilter());
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.prescriber.filter")));
			jSelectionPanel.add(getComboPrescriber());
			if (withPaid) {
				jSelectionPanel.add(getPaidComboBox());
			}
			jSelectionPanel.add(getDateFilterPanel());
			jSelectionPanel.add(getFilterButton());
		}
		return jSelectionPanel;
	}

	/**
	 * This method initializes jTable, that contains the information about the
	 * Laboratory Tests
	 *
	 * @return jTable (JTable)
	 */
	private JTable getJTable() {
		if (jTable == null) {
			model = new LabBrowsingModel();
			if (withPaid) {
				updateTotals();
			}
			jTable = new JTable(model);
			boolean[] resizable = withPaid ? columnsResizableWithPaid : columnsResizable;
			int[] widths = withPaid ? pColumnWidthWithPaid : pColumnWidth;
			int[] maxWidths = withPaid ? maxWidthWithPaid : maxWidth;
			boolean[] visible = withPaid ? columnsVisibleWithPaid : columnsVisible;
			TableColumnModel columnModel = jTable.getColumnModel();
			for (int i = 0; i < model.getColumnCount(); i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(widths[i]);
				if (!resizable[i]) {
					columnModel.getColumn(i).setMaxWidth(maxWidths[i]);
				}
				if (!visible[i]) {
					columnModel.getColumn(i).setMaxWidth(0);
					columnModel.getColumn(i).setMinWidth(0);
					columnModel.getColumn(i).setPreferredWidth(0);
				}
			}
		}
		return jTable;
	}

	/**
	 * This method initializes the patient code search text field.
	 *
	 * @return patientCodeField (JTextField)
	 */
	private JTextField getPatientCodeField() {
		if (patientCodeField == null) {
			patientCodeField = new JTextField();
			patientCodeField.setPreferredSize(new Dimension(215, 30));
			patientCodeField.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						if (updatePatientFilter()) {
							currentPage = 0;
							loadCurrentPage();
						}
					}
				}
				@Override public void keyReleased(KeyEvent e) {}
				@Override public void keyTyped(KeyEvent e) {}
			});
		}
		return patientCodeField;
	}


	/**
	 * This method initializes comboExams, that allows to choose which Exam the
	 * user want to display on the Table
	 *
	 * @return comboExams (JComboBox)
	 */
	private JComboBox getComboExams() {
		if (comboExams == null) {
			comboExams = new JComboBox();
			comboExams.setPreferredSize(new Dimension(225, 30));
			comboExams.addItem(new Exam("", MessageBundle.getMessage("angal.common.all.txt"), new ExamType("", ""), 0, ""));
			List<Exam> type;
			try {
				type = examBrowsingManager.getExams();
			} catch (OHServiceException e1) {
				type = null;
				OHServiceExceptionUtil.showMessages(e1);
			} // for efficiency in the sequent for
			if (null != type) {
				for (Exam elem : type) {
					comboExams.addItem(elem);
				}
			}
			comboExams.addActionListener(actionEvent -> {
				typeSelected = comboExams.getSelectedItem().toString();
				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					typeSelected = null;
				}

			});
		}
		return comboExams;
	}

	private Component getDateFilterPanel() {
		if (dateFilterPanel == null) {
			dateFilterPanel = new JPanel(new SpringLayout());
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
			dateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
			dateFilterPanel.add(dateFrom);
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			dateTo = new GoodDateChooser(LocalDate.now());
			dateFilterPanel.add(dateTo);
			SpringUtilities.makeCompactGrid(dateFilterPanel, 2, 2, 5, 5, 5, 5);
		}
		return dateFilterPanel;
	}

	/**
	 * This method initializes filterButton, which is the button that perform
	 * the filtering and calls the methods to refresh the Table
	 *
	 * @return filterButton (JButton)
	 */
	private JButton getFilterButton() {
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.search.btn.key"));
			filterButton.addActionListener(actionEvent -> {
				typeSelected = comboExams.getSelectedItem().toString();

				if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					typeSelected = "";
				}

				if (updatePatientFilter()) {
					currentPage = 0;
					loadCurrentPage();
				}

				if (withPaid) {
					updateTotals();
				}
			});
		}
		return filterButton;
	}

	/**
	 * Maps the selected item of the paid status combo box to the {@code paidCode} used by the queries:
	 * {@code "C"} paid, {@code "O"} not paid (open), {@code "0"} not billed, {@code null} no filter.
	 *
	 * @return the paid code, or {@code null} when no filter is selected
	 */
	private String getSelectedPaidStatus() {
		Object selected = paidComboBox.getSelectedItem();
		if (selected == null) {
			return null;
		}
		String paidStatus = selected.toString();
		if (FILTER_PAID.equals(paidStatus)) {
			return "C";
		}
		if (FILTER_NOT_PAID.equals(paidStatus)) {
			return "O";
		}
		if (FILTER_NOT_FACTURED.equals(paidStatus)) {
			return "0";
		}
		return null;
	}

	private JComboBox<String> getPaidComboBox() {
		if (paidComboBox == null) {
			paidComboBox = new JComboBox<>();
			paidComboBox.setPreferredSize(new Dimension(225, 30));
			paidComboBox.addItem(FILTER_PAID_SELECT);
			paidComboBox.addItem(FILTER_PAID);
			paidComboBox.addItem(FILTER_NOT_PAID);
			paidComboBox.addItem(FILTER_NOT_FACTURED);
		}
		return paidComboBox;
	}

	private JPanel getPanelTotal() {
		if (panelTotal == null) {
			panelTotal = new JPanel();
			panelTotal.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			panelTotal.add(getTotalPaidLabel());
			panelTotal.add(getTotalPaidValueLabel());
			panelTotal.add(getTotalNotPaidLabel());
			panelTotal.add(getTotalNotPaidValueLabel());
			panelTotal.add(getTotalNotFacturedLabel());
			panelTotal.add(getTotalNotFacturedValueLabel());
		}
		return panelTotal;
	}

	private JLabel getTotalPaidLabel() {
		if (totalPaidLabel == null) {
			totalPaidLabel = new JLabel(MessageBundle.getMessage("angal.lobaratory.totalPaidLabel") + ": ");
			totalPaidLabel.setFont(totalPaidLabel.getFont().deriveFont(java.awt.Font.BOLD));
		}
		return totalPaidLabel;
	}

	private JLabel getTotalPaidValueLabel() {
		if (totalPaidValueLabel == null) {
			totalPaidValueLabel = new JLabel("");
		}
		return totalPaidValueLabel;
	}

	private JLabel getTotalNotPaidLabel() {
		if (totalNotPaidLabel == null) {
			totalNotPaidLabel = new JLabel("  " + MessageBundle.getMessage("angal.lobaratory.totalNotPaidLabel") + ": ");
			totalNotPaidLabel.setFont(totalNotPaidLabel.getFont().deriveFont(java.awt.Font.BOLD));
		}
		return totalNotPaidLabel;
	}

	private JLabel getTotalNotPaidValueLabel() {
		if (totalNotPaidValueLabel == null) {
			totalNotPaidValueLabel = new JLabel("");
		}
		return totalNotPaidValueLabel;
	}

	private JLabel getTotalNotFacturedLabel() {
		if (totalNotFacturedLabel == null) {
			totalNotFacturedLabel = new JLabel("  " + MessageBundle.getMessage("angal.lobaratory.totalNotFacturedLabel") + ": ");
			totalNotFacturedLabel.setFont(totalNotFacturedLabel.getFont().deriveFont(java.awt.Font.BOLD));
		}
		return totalNotFacturedLabel;
	}

	private JLabel getTotalNotFacturedValueLabel() {
		if (totalNotFacturedValueLabel == null) {
			totalNotFacturedValueLabel = new JLabel("");
		}
		return totalNotFacturedValueLabel;
	}

	private void updateTotals() {
		String type = comboExams.getSelectedItem().toString();
		if (type.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
			type = "";
		}
		Patient patient = null;
		String patid = patientCodeField.getText();
		if (patid != null && !patid.isEmpty()) {
			try {
				patient = patManager.getPatientById(Integer.parseInt(patid));
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			} catch (NumberFormatException e) {
				patient = null;
			}
		}
		try {
			long totalPaid = labManager.getLaboratoryCount(type, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay(),
					patient, "C");
			long totalNotPaid = labManager.getLaboratoryCount(type, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay(),
					patient, "O");
			long totalNotFactured = labManager.getLaboratoryCount(type, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay(),
					patient, "0");
			totalPaidValueLabel.setText(totalPaid + "");
			totalNotPaidValueLabel.setText(totalNotPaid + "");
			totalNotFacturedValueLabel.setText(totalNotFactured + "");
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	/**
	 * This class defines the model for the Table
	 *
	 * @author theo
	 *
	 */
	class LabBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

        public LabBrowsingModel() {
		}

		@Override
		public int getRowCount() {
			if (pLabs == null) {
				return 0;
			}
			return pLabs.size();
		}

		@Override
		public String getColumnName(int c) {
			return (withPaid ? pColumnsWithPaid : pColumns)[c];
		}

		@Override
		public int getColumnCount() {
			return withPaid ? pColumnsWithPaid.length : pColumns.length;
		}

		/**
		 * Note: We must get the objects in a reversed way because of the query
		 *
		 * @see org.isf.lab.service.LabIoOperations
		 */
		@Override
		public Object getValueAt(int r, int c) {
			Laboratory lab = pLabs.get(r);
			if (c == -1) {
				return lab;
			} else if (c == 0) {
				return lab.getLabDate().format(DATE_TIME_FORMATTER);
			} else if (c == 1) {
				return lab.getPatName();
			} else if (c == 2) {
				return lab.getExam();
			} else if (c == 3) {
				return lab.getPrescriber() != null ? lab.getPrescriber() : "";
			} else if (c == 4) {
				return lab.getResult();
			} else if (c == 5) {
				String paidStatus = lab.getPaidStatus();
				if (paidStatus == null) {
					return FILTER_NOT_FACTURED;
				}
				return "C".equals(paidStatus) ? MessageBundle.getMessage("angal.lab.alreadypaid")
						: MessageBundle.getMessage("angal.lab.notalreadypaid");
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	/**
	 * This method updates the Table because a laboratory test has been updated
	 * Reloads the current page
	 */
	public void laboratoryUpdated() {
		loadCurrentPage();
	}

	/**
	 * This method updates the Table because a laboratory test has been inserted
	 * Reloads the first page
	 */
	public void laboratoryInserted() {
		currentPage = 0;
		loadCurrentPage();
	}

	/**
	 * This method resolves the patient filter from the patient code/name field.
	 *
	 * @return {@code true} if the filter is valid and the table can be loaded, {@code false} otherwise
	 */
	private boolean updatePatientFilter() {
		pPatient = null;
		patientFilterActive = false;
		String input = patientCodeField.getText().trim();
		if (input.isEmpty()) {
			return true;
		}
		try {
			if (input.matches("\\d+")) {
				pPatient = patManager.getPatientById(Integer.parseInt(input));
				patientFilterActive = true;
				return true;
			}
			List<Patient> patients = patManager.getPatientByCodeOrName(input);
			if (patients == null || patients.isEmpty()) {
				patientFilterActive = true;
				return true;
			}
			if (patients.size() == 1) {
				pPatient = patients.get(0);
				patientFilterActive = true;
				return true;
			}
			MessageDialog.error(null, "angal.lab.insertvalidpatientid.msg");
			return false;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return false;
		}
	}

	/**
	 * This method loads the current page of laboratory records into the table
	 * applying the active filters (exam, date range, patient, prescriber, result status, paid status).
	 */
	private void loadCurrentPage() {
		try {
			if (patientFilterActive && pPatient == null) {
				pLabs = new ArrayList<>();
				totalPages = 0;
				totalElements = 0;
				model.fireTableDataChanged();
				jTable.updateUI();
				updatePaginationControls();
				return;
			}

			String exam = null;
			if (comboExams.getSelectedItem() != null) {
				exam = comboExams.getSelectedItem().toString();
				if (exam.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					exam = null;
				}
			}

			String prescriber = null;
			if (comboPrescriber.getSelectedItem() != null) {
				prescriber = comboPrescriber.getSelectedItem().toString();
				if (prescriber.equals(MessageBundle.getMessage("angal.lab.prescriber.all"))) {
					prescriber = null;
				}
			}

			LaboratoryResultFilter resultFilter = LaboratoryResultFilter.ALL;
			if (comboResultFilter.getSelectedItem() != null) {
				String selected = comboResultFilter.getSelectedItem().toString();
				if (FILTER_NON_EMPTY.equals(selected)) {
					resultFilter = LaboratoryResultFilter.NON_EMPTY;
				} else if (FILTER_EMPTY.equals(selected)) {
					resultFilter = LaboratoryResultFilter.EMPTY;
				}
			}

			String paidCode = null;
			if (withPaid) {
				paidCode = getSelectedPaidStatus();
			}

			PagedResponse<Laboratory> response = labManager.getLaboratoryPageable(exam, dateFrom.getDateStartOfDay(), dateTo.getDateEndOfDay(),
					pPatient, prescriber, resultFilter, paidCode, currentPage, PAGE_SIZE);
			if (response == null || response.getData() == null) {
				pLabs = new ArrayList<>();
				totalPages = 0;
				totalElements = 0;
			} else {
				pLabs = new ArrayList<>(response.getData());
				totalPages = response.getPageInfo().getTotalPages();
				totalElements = response.getPageInfo().getTotalNbOfElements();
			}

			model.fireTableDataChanged();
			jTable.updateUI();
			updatePaginationControls();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	/**
	 * This method initializes the pagination panel with the navigation buttons,
	 * the page selector and the counters.
	 *
	 * @return paginationPanel (JPanel)
	 */
	private JPanel getPaginationPanel() {
		if (paginationPanel == null) {
			paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
			paginationPanel.setBorder(BorderFactory.createEtchedBorder());

			prevPageButton = new JButton("<");
			prevPageButton.setEnabled(false);
			prevPageButton.addActionListener(actionEvent -> {
				if (currentPage > 0) {
					currentPage--;
					loadCurrentPage();
				}
			});

			pageCombo = new JComboBox<>();
			pageCombo.setPreferredSize(new Dimension(70, 25));
			pageCombo.addActionListener(actionEvent -> {
				if (!updatingPageCombo && pageCombo.getSelectedItem() != null) {
					int selected = (Integer) pageCombo.getSelectedItem();
					if (selected - 1 != currentPage) {
						currentPage = selected - 1;
						loadCurrentPage();
					}
				}
			});

			nextPageButton = new JButton(">");
			nextPageButton.setEnabled(false);
			nextPageButton.addActionListener(actionEvent -> {
				if (currentPage < totalPages - 1) {
					currentPage++;
					loadCurrentPage();
				}
			});

			pageInfoLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.common.pages.txt"));
			totalElementsLabel = new JLabel(MessageBundle.formatMessage("angal.lab.pagination.elements.found.fmt", 0));

			paginationPanel.add(prevPageButton);
			paginationPanel.add(pageCombo);
			paginationPanel.add(pageInfoLabel);
			paginationPanel.add(nextPageButton);
			paginationPanel.add(totalElementsLabel);
		}
		return paginationPanel;
	}

	/**
	 * This method updates the pagination controls (buttons, page selector and labels)
	 * according to the current pagination state.
	 */
	private void updatePaginationControls() {
		if (prevPageButton == null || nextPageButton == null || pageCombo == null || pageInfoLabel == null) {
			return;
		}

		boolean hasMultiplePages = totalPages > 1;

		if (hasMultiplePages && pageCombo.getItemCount() != totalPages && totalPages > 0) {
			updatingPageCombo = true;
			pageCombo.removeAllItems();
			for (int i = 1; i <= totalPages; i++) {
				pageCombo.addItem(i);
			}
			updatingPageCombo = false;
		}

		if (hasMultiplePages && totalPages > 0) {
			updatingPageCombo = true;
			pageCombo.setSelectedItem(currentPage + 1);
			updatingPageCombo = false;
		}

		prevPageButton.setEnabled(currentPage > 0 && hasMultiplePages);
		nextPageButton.setEnabled(currentPage < totalPages - 1 && hasMultiplePages);
		pageCombo.setEnabled(hasMultiplePages);

		if (totalPages <= 0) {
			pageInfoLabel.setText("/ 0 " + MessageBundle.getMessage("angal.common.pages.txt"));
			pageCombo.setEnabled(false);
			prevPageButton.setEnabled(false);
			nextPageButton.setEnabled(false);
		} else {
			pageInfoLabel.setText("/ " + totalPages + " " + MessageBundle.getMessage("angal.common.pages.txt"));
		}

		totalElementsLabel.setText(MessageBundle.formatMessage("angal.lab.pagination.elements.found.fmt", totalElements));
	}

	private void refreshPrescriberCombo() {
		if (comboPrescriber != null) {
			String currentSelection = (String) comboPrescriber.getSelectedItem();
			comboPrescriber.removeAllItems();
			comboPrescriber.addItem(MessageBundle.getMessage("angal.lab.prescriber.all"));
			try {
				List<String> prescribers = labManager.getDistinctPrescribers();
				for (String prescriber : prescribers) {
					comboPrescriber.addItem(prescriber);
				}
				if (currentSelection != null && comboPrescriber.getItemCount() > 0) {
					comboPrescriber.setSelectedItem(currentSelection);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			comboPrescriber.repaint();
		}
	}
}