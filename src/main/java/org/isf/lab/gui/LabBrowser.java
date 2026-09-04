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
package org.isf.lab.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.prescriber.manager.PrescriberBrowserManager;
import org.isf.prescriber.model.Prescriber;
import org.isf.serviceprinting.manager.PrintLabels;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.stat.gui.report.GenericReportLabExamList;
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.time.TimeTools;

import net.sf.jasperreports.engine.JRException;

/**
 * LabBrowser - list all labs
 */
public class LabBrowser extends ModalJFrame implements LabListener, LabEditListener, LabEditExtendedListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void labInserted() {
		applyFilters();
	}

	@Override
	public void labUpdated() {
		filterButton.doClick();
	}

	private static final int FILTER_WIDTH = 200;
	private static final int FILTER_HEIGHT = 22;
	private static final Dimension FILTER_DIMENSION = new Dimension(FILTER_WIDTH, FILTER_HEIGHT);

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
	private JTextField patientCodeField;
	private int pfrmHeight = 100;
	private List<Laboratory> pLabs;
	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.exam.txt").toUpperCase(),
			MessageBundle.getMessage("angal.lab.prescriber").toUpperCase(),
			MessageBundle.getMessage("angal.common.result.txt").toUpperCase(),
			MessageBundle.getMessage("angal.lab.paid").toUpperCase()
	};
	private boolean[] columnsResizable = {false, false, true, true, true, true, false};
	private int[] pColumnWidth = {80, 150, 200, 200, 150, 200, 90};
	private int[] maxWidth = {80, 150, 200, 200, 150, 200, 90};
	private boolean[] columnsVisible = { true, true, GeneralData.LABEXTENDED, true, true, true, true};
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private PatientBrowserManager patManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private PrescriberBrowserManager prescriberBrowserManager = Context.getApplicationContext().getBean(PrescriberBrowserManager.class);
	private LabBrowsingModel model;
	private Laboratory laboratory;
	private int selectedrow;
	private String typeSelected;
	private JPanel dateFilterPanel;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private final JFrame myFrame;
	private JButton printLabelButton;
	private JButton examListButton;
	private JComboBox<String> comboWithResult;
	private JComboBox<String> userComboBox;
	private JComboBox<String> paidComboBox;
	private JLabel totalPaidValueLabel;
	private JLabel totalNotPaidValueLabel;
	private JLabel totalNotFacturedValueLabel;

	/**
	 * This is the default constructor
	 */
	public LabBrowser() {
		super();
		myFrame = this;
		this.setTitle(MessageBundle.getMessage("angal.lab.laboratorybrowser.title"));
		this.setContentPane(getJContentPane());
		setSize(new Dimension(1345, 650));
		setLocationRelativeTo(null);
		applyFilters();
		setVisible(true);
	}

	/**
	 * Opens the browser pre-filtered to a single patient's lab exams. The patient filter is locked
	 * (pre-filled and disabled) so the window stays scoped to that patient; the exam-type and
	 * date-range filters remain usable.
	 */
	public LabBrowser(Patient patient) {
		super();
		myFrame = this;
		this.setTitle(MessageBundle.getMessage("angal.lab.laboratorybrowser.title"));
		this.setContentPane(getJContentPane());
		setSize(new Dimension(1345, 650));
		setResizable(false);
		setLocationRelativeTo(null);

		patientCodeField.setText(String.valueOf(patient.getCode()));
		patientCodeField.setEnabled(false);

		typeSelected = comboExams.getSelectedItem().toString();
		if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
			typeSelected = "";
		}
		model = new LabBrowsingModel(typeSelected, dateFrom.getDate(), dateTo.getDate(), patientCodeField.getText());
		model.fireTableDataChanged();
		jTable.updateUI();

		setVisible(true);
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
			jButtonPanel = new JPanel(new BorderLayout());
			JPanel buttonsPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnlaboratorynew")) {
				buttonsPanel.add(getButtonNew(), null);
			}
			if (MainMenu.checkUserGrants("btnlaboratoryedit")) {
				buttonsPanel.add(getButtonEdit(), null);
			}
			if (MainMenu.checkUserGrants("btnlaboratorydel")) {
				buttonsPanel.add(getButtonDelete(), null);
			}
			buttonsPanel.add(getPrintTableButton(), null);
			buttonsPanel.add(getPrintLabelButton(), null);
			buttonsPanel.add(getExamListButton(), null);
			buttonsPanel.add(getCloseButton(), null);
			jButtonPanel.add(getPanelTotal(), BorderLayout.NORTH);
			jButtonPanel.add(buttonsPanel, BorderLayout.SOUTH);
		}
		return jButtonPanel;
	}

	private JPanel getPanelTotal() {
		JPanel panelTotal = new JPanel();
		panelTotal.add(new JLabel(MessageBundle.getMessage("angal.lab.totalpaid") + ": "));
		panelTotal.add(getTotalPaidValueLabel());
		panelTotal.add(new JLabel(MessageBundle.getMessage("angal.lab.totalnotpaid") + ": "));
		panelTotal.add(getTotalNotPaidValueLabel());
		panelTotal.add(new JLabel(MessageBundle.getMessage("angal.lab.totalnotcharged") + ": "));
		panelTotal.add(getTotalNotFacturedValueLabel());
		return panelTotal;
	}

	private JLabel getTotalPaidValueLabel() {
		if (totalPaidValueLabel == null) {
			totalPaidValueLabel = new JLabel("0");
		}
		return totalPaidValueLabel;
	}

	private JLabel getTotalNotPaidValueLabel() {
		if (totalNotPaidValueLabel == null) {
			totalNotPaidValueLabel = new JLabel("0");
		}
		return totalNotPaidValueLabel;
	}

	private JLabel getTotalNotFacturedValueLabel() {
		if (totalNotFacturedValueLabel == null) {
			totalNotFacturedValueLabel = new JLabel("0");
		}
		return totalNotFacturedValueLabel;
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
					printManager.print("Laboratory", labs, 0);
				}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}
		return printTableButton;
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
							SelectPatient selectPatient = new SelectPatient(this, null);
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

	private JButton getExamListButton() {
		if (examListButton == null) {
			examListButton = new JButton(MessageBundle.getMessage("angal.lab.printexamlist.btn"));
			examListButton.setMnemonic(MessageBundle.getMnemonic("angal.lab.printexamlist.btn.key"));
			examListButton.addActionListener(actionEvent -> {
				int withResult = -1;
				if (comboWithResult.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.lab.withresults"))) {
					withResult = 1;
				} else if (comboWithResult.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.lab.withoutresults"))) {
					withResult = 0;
				}

				String patientCode = "all";
				String patientname = "";
				String patText = patientCodeField.getText().trim();
				if (!patText.isEmpty()) {
					try {
						Patient pat = patManager.getPatientById(Integer.parseInt(patText));
						if (pat != null) {
							patientCode = String.valueOf(pat.getCode());
							patientname = pat.getName();
						}
					} catch (OHServiceException | NumberFormatException e) {
						// ignore, keep "all"
					}
				}

				String userCode = "all";
				String name = "";
				if (!userComboBox.getSelectedItem().toString()
						.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.selectprescriber"))) {
					userCode = userComboBox.getSelectedItem().toString();
					name = userComboBox.getSelectedItem().toString();
				}

				String codeexam = "all";
				String examSel = comboExams.getSelectedItem().toString();
				if (!examSel.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
					codeexam = examSel;
				}

				String paidStatus = "all";
				String sel = paidComboBox.getSelectedItem().toString();
				if (sel.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.paid"))) {
					paidStatus = "C";
				} else if (sel.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.notpaid"))) {
					paidStatus = "O";
				} else if (sel.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.notfactured"))) {
					paidStatus = "0";
				}

				String fromDateStr = dateFrom.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
				String toDateStr = dateTo.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

				new GenericReportLabExamList(fromDateStr, toDateStr, codeexam, withResult,
						patientCode, userCode, name, patientname, paidStatus);
			});
		}
		return examListButton;
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
							pLabs.remove(jTable.getSelectedRow());
							model.fireTableDataChanged();
							jTable.updateUI();
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
			jSelectionPanel.setPreferredSize(new Dimension(FILTER_WIDTH + 20, pfrmHeight));
			jSelectionPanel.setLayout(new BoxLayout(jSelectionPanel, BoxLayout.Y_AXIS));
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.searchbycodepatientpressenter")));
			jSelectionPanel.add(getPatientCodeField());
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.selectanexam")));
			jSelectionPanel.add(getComboExams());
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.withallresults")));
			jSelectionPanel.add(getComboWithResult());
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.selectprescriber")));
			jSelectionPanel.add(getUserComboBox());
			jSelectionPanel.add(new JLabel(MessageBundle.getMessage("angal.lab.selectpaidstatus")));
			jSelectionPanel.add(getPaidComboBox());
			jSelectionPanel.add(getDateFilterPanel());
			jSelectionPanel.add(getFilterButton());
			jSelectionPanel.add(Box.createVerticalGlue());
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
			jTable = new JTable(model);
			TableColumnModel columnModel = jTable.getColumnModel();
			for (int i = 0; i < model.getColumnCount(); i++) {
				jTable.getColumnModel().getColumn(i).setMinWidth(pColumnWidth[i]);
				if (!columnsResizable[i]) {
					columnModel.getColumn(i).setMaxWidth(maxWidth[i]);
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

	/**
	 * This method initializes the patient code search text field.
	 *
	 * @return patientCodeField (JTextField)
	 */
	private JTextField getPatientCodeField() {
		if (patientCodeField == null) {
			patientCodeField = new JTextField();
			patientCodeField.setPreferredSize(FILTER_DIMENSION);
			patientCodeField.setMaximumSize(FILTER_DIMENSION);
			patientCodeField.setAlignmentX(Component.LEFT_ALIGNMENT);
			patientCodeField.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						applyFilters();
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
			comboExams.setPreferredSize(FILTER_DIMENSION);
			comboExams.setMaximumSize(FILTER_DIMENSION);
			comboExams.setAlignmentX(Component.LEFT_ALIGNMENT);
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
			dateFrom.setPreferredSize(new Dimension(120, FILTER_HEIGHT));
			dateFrom.setMaximumSize(new Dimension(120, FILTER_HEIGHT));
			dateFilterPanel.add(dateFrom);
			dateFilterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
			dateTo = new GoodDateChooser(LocalDate.now());
			dateTo.setPreferredSize(new Dimension(120, FILTER_HEIGHT));
			dateTo.setMaximumSize(new Dimension(120, FILTER_HEIGHT));
			dateFilterPanel.add(dateTo);
			SpringUtilities.makeCompactGrid(dateFilterPanel, 2, 2, 5, 5, 5, 5);
			dateFilterPanel.setPreferredSize(new Dimension(FILTER_WIDTH, dateFilterPanel.getPreferredSize().height));
			dateFilterPanel.setMaximumSize(new Dimension(FILTER_WIDTH, dateFilterPanel.getPreferredSize().height));
			dateFilterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
			filterButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			filterButton.setPreferredSize(FILTER_DIMENSION);
			filterButton.setMaximumSize(FILTER_DIMENSION);
			filterButton.addActionListener(actionEvent -> applyFilters());
		}
		return filterButton;
	}

	/**
	 * This method initializes comboWithResult, that allows to choose the result
	 * type to display on the Table: all, with results or without results.
	 *
	 * @return comboWithResult (JComboBox)
	 */
	private JComboBox<String> getComboWithResult() {
		if (comboWithResult == null) {
			comboWithResult = new JComboBox<>();
			comboWithResult.setPreferredSize(FILTER_DIMENSION);
			comboWithResult.setMaximumSize(FILTER_DIMENSION);
			comboWithResult.setAlignmentX(Component.LEFT_ALIGNMENT);
			comboWithResult.addItem(MessageBundle.getMessage("angal.lab.withallresults"));
			comboWithResult.addItem(MessageBundle.getMessage("angal.lab.withresults"));
			comboWithResult.addItem(MessageBundle.getMessage("angal.lab.withoutresults"));
		}
		return comboWithResult;
	}

	/**
	 * This method initializes userComboBox, that allows to choose the prescriber
	 * to display on the Table.
	 *
	 * @return userComboBox (JComboBox)
	 */
	private JComboBox<String> getUserComboBox() {
		if (userComboBox == null) {
			userComboBox = new JComboBox<>();
			userComboBox.setPreferredSize(FILTER_DIMENSION);
			userComboBox.setMaximumSize(FILTER_DIMENSION);
			userComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			userComboBox.addItem(MessageBundle.getMessage("angal.lab.selectprescriber"));
			try {
				List<Prescriber> prescribers = prescriberBrowserManager.getPrescribers();
				for (Prescriber prescriber : prescribers) {
					userComboBox.addItem(prescriber.getDescription());
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return userComboBox;
	}

	/**
	 * This method initializes paidComboBox, that allows to choose the financial
	 * status to display on the Table: all, paid, not paid or not charged.
	 *
	 * @return paidComboBox (JComboBox)
	 */
	private JComboBox<String> getPaidComboBox() {
		if (paidComboBox == null) {
			paidComboBox = new JComboBox<>();
			paidComboBox.setPreferredSize(FILTER_DIMENSION);
			paidComboBox.setMaximumSize(FILTER_DIMENSION);
			paidComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			paidComboBox.addItem(MessageBundle.getMessage("angal.lab.selectpaidstatus"));
			paidComboBox.addItem(MessageBundle.getMessage("angal.lab.paid"));
			paidComboBox.addItem(MessageBundle.getMessage("angal.lab.notpaid"));
			paidComboBox.addItem(MessageBundle.getMessage("angal.lab.notfactured"));
		}
		return paidComboBox;
	}

	/**
	 * This method reads the selected filters and refreshes the table and the totals.
	 */
	private void applyFilters() {
		typeSelected = comboExams.getSelectedItem().toString();
		if (typeSelected.equalsIgnoreCase(MessageBundle.getMessage("angal.common.all.txt"))) {
			typeSelected = null;
		}

		int withResult = -1;
		if (comboWithResult.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.lab.withresults"))) {
			withResult = 1;
		} else if (comboWithResult.getSelectedItem().toString().equalsIgnoreCase(MessageBundle.getMessage("angal.lab.withoutresults"))) {
			withResult = 0;
		}

		String patientCode = patientCodeField.getText().trim();

		String userCode = null;
		if (!userComboBox.getSelectedItem().toString()
						.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.selectprescriber"))) {
			userCode = userComboBox.getSelectedItem().toString();
		}

		String paidStatus = null;
		String selectedPaidStatus = paidComboBox.getSelectedItem().toString();
		if (selectedPaidStatus.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.paid"))) {
			paidStatus = "C";
		} else if (selectedPaidStatus.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.notpaid"))) {
			paidStatus = "O";
		} else if (selectedPaidStatus.equalsIgnoreCase(MessageBundle.getMessage("angal.lab.notfactured"))) {
			paidStatus = "0";
		}

		model = new LabBrowsingModel(typeSelected, dateFrom.getDate(), dateTo.getDate(), patientCode, withResult, userCode, paidStatus);
		updateTotals(typeSelected, withResult, patientCode, userCode);
		model.fireTableDataChanged();
		jTable.updateUI();
	}

	private void updateTotals(String exam, int withResult, String patientCode, String userCode) {
		try {
			totalPaidValueLabel.setText(String.valueOf(labManager.getLaboratoryCount(exam, dateFrom.getDateStartOfDay(),
					dateTo.getDateEndOfDay(), withResult, getPatientFilter(patientCode), userCode, "C")));
			totalNotPaidValueLabel.setText(String.valueOf(labManager.getLaboratoryCount(exam, dateFrom.getDateStartOfDay(),
					dateTo.getDateEndOfDay(), withResult, getPatientFilter(patientCode), userCode, "O")));
			totalNotFacturedValueLabel.setText(String.valueOf(labManager.getLaboratoryCount(exam, dateFrom.getDateStartOfDay(),
					dateTo.getDateEndOfDay(), withResult, getPatientFilter(patientCode), userCode, "0")));
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private Patient getPatientFilter(String patientCode) {
		if (patientCode.isEmpty()) {
			return null;
		}
		try {
			return patManager.getPatientById(Integer.parseInt(patientCode));
		} catch (OHServiceException | NumberFormatException e) {
			return null;
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

		public LabBrowsingModel(String exam, LocalDate dateFrom, LocalDate dateTo, String patid, int resultFilter, String userCode, String paidCode) {
			try {
				Patient pat = null;
				if (!patid.isEmpty()) {
					pat = patManager.getPatientById(Integer.parseInt(patid));
					if (pat == null) {
						pLabs = new ArrayList<>();
						return;
					}
				}
				pLabs = labManager.getLaboratory(exam, dateFrom.atStartOfDay(), dateTo.atStartOfDay(), resultFilter, pat, userCode, paidCode);
			} catch (OHServiceException e) {
				pLabs = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			} catch (NumberFormatException e) {
				pLabs = new ArrayList<>();
				MessageDialog.error(null, "angal.lab.insertvalidpatientid.msg");
			}
		}

		public LabBrowsingModel(String patid) {
			try {
				if (!patid.isEmpty()) {
					Patient pat = patManager.getPatientById(Integer.parseInt(patid));
					if (pat == null) {
						pLabs = new ArrayList<>();
					} else {
						pLabs = labManager.getLaboratory(pat);
					}
				} else {
					pLabs = new ArrayList<>();
				}
			} catch (OHServiceException e) {
				pLabs = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			} catch (NumberFormatException e) {
				pLabs = new ArrayList<>();
				MessageDialog.error(null, "angal.lab.insertvalidpatientid.msg");
			}
		}

		public LabBrowsingModel() {
			try {
				pLabs = labManager.getLaboratory();
			} catch (OHServiceException e) {
				pLabs = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
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
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
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
				return lab.getPatient() != null ? lab.getPatient().getCode() : null;
			} else if (c == 1) {
				return lab.getLabDate().format(DATE_TIME_FORMATTER);
			} else if (c == 2) {
				return lab.getPatName();
			} else if (c == 3) {
				return lab.getExam();
			} else if (c == 4) {
				return lab.getPrescriber() != null ? lab.getPrescriber() : "";
			} else if (c == 5) {
				return lab.getResult();
			} else if (c == 6) {
				if (lab.getBill() == null) {
					return MessageBundle.getMessage("angal.lab.notfactured");
				}
				return "C".equals(lab.getBill().getStatus())
						? MessageBundle.getMessage("angal.lab.alreadypaid")
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
	 * Sets the focus on the same record as before
	 */
	public void laboratoryUpdated() {
		pLabs.set(pLabs.size() - selectedrow - 1, laboratory);
		((LabBrowsingModel) jTable.getModel()).fireTableDataChanged();
		jTable.updateUI();
		if (jTable.getRowCount() > 0 && selectedrow > -1) {
			jTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}

	/**
	 * This method updates the Table because a laboratory test has been inserted
	 * Sets the focus on the first record
	 */
	public void laboratoryInserted() {
		pLabs.add(pLabs.size(), laboratory);
		((LabBrowsingModel) jTable.getModel()).fireTableDataChanged();
		if (jTable.getRowCount() > 0) {
			jTable.setRowSelectionInterval(0, 0);
		}
	}

}