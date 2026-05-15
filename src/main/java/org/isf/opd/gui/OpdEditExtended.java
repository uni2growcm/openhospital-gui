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
package org.isf.opd.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

import org.isf.anamnesis.gui.PatientHistoryEdit;
import org.isf.anamnesis.manager.PatientHistoryManager;
import org.isf.anamnesis.model.PatientHistory;
import org.isf.anamnesis.model.PatientPatientHistory;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.operation.gui.OperationRowOpd;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsert.PatientListener;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.db.RememberData;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.GoodDateTimeVisitChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import org.isf.disease.gui.DiseaseEdit;
import java.awt.event.ItemEvent;

/**
 * OpdEditExtended - add/edit an OPD registration
 */
public class OpdEditExtended extends ModalJFrame implements PatientInsertExtended.PatientListener, PatientListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(OpdEditExtended.class);

	public static final int DEFAULT_VISIT_DURATION = 30;
	

	private PatientHistoryManager patientHistoryManager = Context.getApplicationContext().getBean(PatientHistoryManager.class);

	

	@Override
	public void patientInserted(AWTEvent e) {
		opdPatient = (Patient) e.getSource();
		setPatient(opdPatient);
		jComboPatResult.addItem(opdPatient);
		jComboPatResult.setSelectedItem(opdPatient);
		jPatientEditButton.setEnabled(true);
	}

	@Override
	public void patientUpdated(AWTEvent e) {
		setPatient(opdPatient);
	}

	private EventListenerList surgeryListeners = new EventListenerList();
	
	public interface SurgeryListener extends EventListener {
		void surgeryUpdated(AWTEvent e, Opd opd);
		void surgeryInserted(AWTEvent e, Opd opd);
	}
	
	public void addSurgeryListener(SurgeryListener l) {
		surgeryListeners.add(SurgeryListener.class, l);
	}
	
	public void removeSurgeryListener(SurgeryListener listener) {
		surgeryListeners.remove(SurgeryListener.class, listener);
	}

	private void fireSurgeryInserted(Opd opd) {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = surgeryListeners.getListeners(SurgeryListener.class);
		for (EventListener listener : listeners) {
			((SurgeryListener) listener).surgeryInserted(event, opd);
		}
	}

	private void fireSurgeryUpdated(Opd opd) {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = surgeryListeners.getListeners(SurgeryListener.class);
		for (EventListener listener : listeners) {
			((SurgeryListener) listener).surgeryUpdated(event, opd);
		}
	}

	private static final String LAST_OPD_LABEL = "<html><i>" + MessageBundle.getMessage("angal.opd.lastopdvisitm.txt") + "</i></html>:";
	private static final String LAST_NOTE_LABEL = "<html><i>" + MessageBundle.getMessage("angal.opd.lastopdnote.txt") + "</i></html>:";

	private JPanel jPanelMain;
	private JPanel jPanelNorth;
	private JPanel jPanelCentral;
	private JPanel jPanelData;
	private JPanel jPanelButtons;

	private JComboBox diseaseTypeBox;
	private LocalDateTime visitDateOpd;
	private DateTimeFormatter currentDateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY_HH_MM, new Locale(GeneralData.LANGUAGE));
	private GoodDateTimeSpinnerChooser opdDateFieldCal;
	private JButton okButton;
	private JButton cancelButton;
	private JButton jButtonExamination;
	private JButton jAnamnesisButton;
	private JRadioButton rePatientButton;
	private JRadioButton newPatientButton;
	private JCheckBox referralToCheckBox;
	private JCheckBox referralFromCheckBox;

	private JPanel jPanelPatient;

	private VoLimitedTextField jFieldFirstName;
	private VoLimitedTextField jFieldSecondName;
	private VoLimitedTextField jFieldAddress;
	private VoLimitedTextField jFieldCity;
	private VoLimitedTextField jFieldNextKin;
	private VoLimitedTextField jFieldAge;

	private Opd opd;
	private boolean insert;
	private DiseaseType allType = new DiseaseType(MessageBundle.getMessage("angal.common.alltypes.txt"), MessageBundle.getMessage("angal.common.alltypes.txt"));

	private VoLimitedTextField jTextPatientSrc;
	private JComboBox jComboPatResult;
	private JRadioButton radiof;
	private JRadioButton radiom;
	private JButton jPatientEditButton;
	private JButton jSearchButton;
	private JLabel jLabelLastOpdVisit;
	private JLabel jFieldLastOpdVisit;
	private JLabel jLabelLastOpdNote;
	private JLabel jFieldLastOpdNote;

	private Patient opdPatient;
	private JPanel jNotePanel;
	private JTextArea jNoteTextArea;
	private JPanel jPatientNotePanel;
	private JTextArea jPatientNote;
	private JPanel jOpdNumberPanel;
	private JTextField jOpdNumField;
	private JComboBox<Ward> opdWardBox;
	private JComboBox<Ward> nextVisitWardBox;
	private JButton nextVisitClearButton;

	/*
	 * Managers and Arrays
	 */
	private DiseaseTypeBrowserManager diseaseTypeBrowserManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
	private DiseaseBrowserManager diseaseBrowserManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private OpdBrowserManager opdBrowserManager = Context.getApplicationContext().getBean(OpdBrowserManager.class);
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private VisitManager visitManager = Context.getApplicationContext().getBean(VisitManager.class);
	private ExaminationBrowserManager examinationBrowserManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);

	private List<DiseaseType> types;
	private List<Disease> diseasesOPD;
	private List<Disease> diseasesAll;
	private List<Ward> wardsOPDList;
	private List<Ward> wardsList;
	private List<Patient> pat = new ArrayList<>();

	private Disease lastOPDDisease1;
	private int visitDuration;

	private JTabbedPane jTabbedPaneOpd;
	private JPanel jPanelOperation;

	/**
	* Additional diagnoses components
	*/
	private JPanel additionalDiagnosisPanel;
	private JTextField searchDiagnosisField;
	private JButton searchDiagnosisButton;
	private JComboBox<Disease> browseDiagnosisCombo;
	private JButton addDiagnosisButton;
	private JPanel selectedDiagnosisContainer;
	private DefaultListModel<Disease> selectedDiagnosisModel;
	private JScrollPane selectedDiagnosisScrollPane;

	/**
	 * Opd next visit fields
	 */
	private JLabel nextVisitLabel;
	private GoodDateTimeVisitChooser opdNextVisitDate;

	private GridBagConstraints gbcOpdNextVisitDate; //needed to update the component

	/**
	 * This method initializes
	 */
	public OpdEditExtended(JFrame owner, Opd old, boolean inserting) {
		super();
		opd = old;
		insert = inserting;
		try {
			types = diseaseTypeBrowserManager.getDiseaseType();
			diseasesOPD = diseaseBrowserManager.getDiseaseOpd();
			diseasesAll = diseaseBrowserManager.getDiseaseAll();
			wardsOPDList = wardBrowserManager.getOpdWards();
			wardsList = wardBrowserManager.getWards();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			if (!insert) {
				opdPatient = opd.getPatient();
				if (opdPatient != null && opd.getPatient().getCode() != 0) {
					opdPatient = patientBrowserManager.getPatientAll(opd.getPatient().getCode());
				} else { //old OPD has no PAT_ID => Create Patient from OPD
					opdPatient = new Patient(opd);
					opdPatient.setCode(0);
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		initialize();
	}
	
	public OpdEditExtended(JFrame owner, Opd opd, Patient patient, boolean inserting) {
		super();
		this.opd = opd;
		opdPatient = patient;
		insert = inserting;
		try {
			types = diseaseTypeBrowserManager.getDiseaseType();
			diseasesOPD = diseaseBrowserManager.getDiseaseOpd();
			diseasesAll = diseaseBrowserManager.getDiseaseAll();
			wardsOPDList = wardBrowserManager.getOpdWards();
			wardsList = wardBrowserManager.getWards();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			if (!insert) {
				opdPatient = opd.getPatient();
				if (opdPatient != null && opd.getPatient().getCode() != 0) { 
					opdPatient = patientBrowserManager.getPatientAll(opd.getPatient().getCode());
				} else { //old OPD has no PAT_ID => Create Patient from OPD
					opdPatient = new Patient(opd);
					opdPatient.setCode(0);
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		initialize();
	}

	private void setPatient(Patient p) {
		jFieldAge.setText(TimeTools.getFormattedAge(p.getBirthDate()));
		jFieldFirstName.setText(p.getFirstName());
		jFieldAddress.setText(p.getAddress());
		jFieldCity.setText(p.getCity());
		jFieldSecondName.setText(p.getSecondName());
		jFieldNextKin.setText(p.getNextKin());
		jPatientNote.setText(opdPatient.getNote());
		setMyMatteBorder(jPanelPatient, MessageBundle.formatMessage("angal.opd.patientcode.fmt.msg", opdPatient.getCode()));
		if (p.getSex() == 'M') {
			radiom.setSelected(true);
		} else if (p.getSex() == 'F') {
			radiof.setSelected(true);
		}
		if (insert) {
			getLastOpd(p.getCode());
		}
		opdNextVisitDate.setEnabled(true);
		nextVisitWardBox.setEnabled(true);
	}
	
	private void resetPatient() {
		jFieldAge.setText("");
		jFieldFirstName.setText("");
		jFieldAddress.setText("");
		jFieldCity.setText("");
		jFieldSecondName.setText("");
		jFieldNextKin.setText("");
		jPatientNote.setText("");
		setMyMatteBorder(jPanelPatient, MessageBundle.getMessage("angal.common.patient.txt"));
		radiom.setSelected(true);
		opdPatient = null;
		opdNextVisitDate.setEnabled(false);
		nextVisitWardBox.setEnabled(false);
	}
	
	//Alex: Resetting history from the last OPD visit for the patient
	private boolean getLastOpd(int code) {
		Opd lastOpd = null;
		try {
			lastOpd = opdBrowserManager.getLastOpd(code);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		
		if (lastOpd == null) {
			newPatientButton.setSelected(true);
			jLabelLastOpdVisit.setText("");
			jFieldLastOpdVisit.setText("");
			jLabelLastOpdNote.setText("");
			jFieldLastOpdNote.setText("");
			jNoteTextArea.setText("");
			return false;
		}
		
		lastOPDDisease1 = null;
		Disease lastOPDDisease2 = null;
		Disease lastOPDDisease3 = null;
		
		for (Disease disease : diseasesOPD) {
			
			if (lastOpd.getDisease() != null && disease.getCode().compareTo(lastOpd.getDisease().getCode()) == 0) {
					lastOPDDisease1 = disease;
			}
			if (lastOpd.getDisease2() != null && disease.getCode().compareTo(lastOpd.getDisease2().getCode()) == 0) {
				lastOPDDisease2 = disease;
			}
			if (lastOpd.getDisease3() != null && disease.getCode().compareTo(lastOpd.getDisease3().getCode()) == 0) {
				lastOPDDisease3 = disease;
			}
		}

		// TODO: this should be a formatted message in the bundle and not "appended" together
		StringBuilder lastOPDDisease = new StringBuilder();
		lastOPDDisease.append(MessageBundle.getMessage("angal.opd.on.txt")).append(' ').append(currentDateFormat.format(lastOpd.getDate())).append(" - ");
		if (lastOPDDisease1 != null) {
			lastOPDDisease.append(lastOPDDisease1.getDescription());
		} 
		if (lastOPDDisease2 != null) {
			lastOPDDisease.append(", ").append(lastOPDDisease2.getDescription());
		}
		if (lastOPDDisease3 != null) {
			lastOPDDisease.append(", ").append(lastOPDDisease3.getDescription());
		}
		jLabelLastOpdVisit.setText(LAST_OPD_LABEL);
		jFieldLastOpdVisit.setText(lastOPDDisease.toString());
		jLabelLastOpdNote.setText(LAST_NOTE_LABEL);
		String note = lastOpd.getNote();
		jFieldLastOpdNote.setText(note.equals("") ? MessageBundle.getMessage("angal.opd.none.txt") : note);
		jNoteTextArea.setText(lastOpd.getNote());
		
		return true;		
	}

	/**
	 * @return the jPanelNorth
	 */
	private JPanel getjPanelNorth() {
		if (jPanelNorth == null) {
			String referralTo;
			String referralFrom;
			jPanelNorth = new JPanel(new FlowLayout());

			rePatientButton = new JRadioButton(MessageBundle.getMessage("angal.opd.reattendance.txt"));
			newPatientButton = new JRadioButton(MessageBundle.getMessage("angal.opd.newattendance.txt"));
			ButtonGroup attendanceGroup = new ButtonGroup();
			attendanceGroup.add(rePatientButton);
			attendanceGroup.add(newPatientButton);
			jPanelNorth.add(rePatientButton);
			jPanelNorth.add(newPatientButton);

			if (!insert) {
				if (opd.getNewPatient() == 'N') {
					newPatientButton.setSelected(true);
				} else {
					rePatientButton.setSelected(true);
				}
			}
			referralFromCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.referral.txt"));
			jPanelNorth.add(referralFromCheckBox);
			if (!insert) {
				referralFrom = opd.getReferralFrom();
				if (referralFrom == null) {
					referralFrom = "";
				}
				if (referralFrom.equals("R")) {
					referralFromCheckBox.setSelected(true);
				}
			}
			referralToCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.referralto.txt"));
			jPanelNorth.add(referralToCheckBox);
			if (!insert) {
				referralTo = opd.getReferralTo();
				if (referralTo == null) {
					referralTo = "";
				}
				if (referralTo.equals("R")) {
					referralToCheckBox.setSelected(true);
				}
			}
		}
		return jPanelNorth;
	}

	/**
	 * @return the jPanelCentral
	 */
	private JPanel getjPanelCentral() {
		if (jPanelCentral == null) {
			jPanelCentral = new JPanel();
			jPanelCentral.setLayout(new BoxLayout(jPanelCentral, BoxLayout.Y_AXIS));
			jPanelCentral.add(getDataPanel());
			jPanelCentral.add(Box.createVerticalStrut(10));
			jPanelCentral.add(getJTabbedPaneOpd());
		}
		return jPanelCentral;
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getMainPanel());
		pack();
		setPreferredSize(new Dimension(850, 750));
		setMinimumSize(this.getSize());
		this.setTitle(LAST_NOTE_LABEL);
		setLocationRelativeTo(null);

		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.opd.newopdregistration.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.opd.editopdregistration.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		if (insert) {
			jTextPatientSrc.requestFocusInWindow();
		} else {
			jNoteTextArea.requestFocusInWindow();
		}
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				//to free memory
				pat.clear();
				diseasesAll.clear();
				diseasesOPD.clear();
				types.clear();
				jComboPatResult.removeAllItems();
				diseaseTypeBox.removeAllItems();
				dispose();
			}
		});
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainPanel() {
		if (jPanelMain == null) {
			jPanelMain = new JPanel();
			jPanelMain.setLayout(new BorderLayout());
			jPanelMain.add(getjPanelNorth(), BorderLayout.NORTH);
			jPanelMain.add(getJNotePanel(), BorderLayout.EAST);
			jPanelMain.add(getjPanelCentral(), BorderLayout.CENTER);
			jPanelMain.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jPanelMain;
	}

	/**
	 * This method initializes jPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDataPanel() {
		if (jPanelData == null) {
			jPanelData = new JPanel();
			GridBagLayout gblPanelData = new GridBagLayout();
			gblPanelData.columnWidths = new int[] { 80, 40, 20, 80, 20 };
			gblPanelData.rowHeights = new int[] { 20, 20, 20, 20, 20, 20, 20, 20 };
			gblPanelData.columnWeights = new double[] { 0.0, 0.1, 0.0, 1.0, 0.0 };
			gblPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
			jPanelData.setLayout(gblPanelData);

			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.opd.attendancedate.txt"));
			GridBagConstraints gbcLabelDate = new GridBagConstraints();
			gbcLabelDate.fill = GridBagConstraints.VERTICAL;
			gbcLabelDate.anchor = GridBagConstraints.WEST;
			gbcLabelDate.insets = new Insets(5, 5, 5, 5);
			gbcLabelDate.gridx = 0;
			gbcLabelDate.gridy = 0;
			jPanelData.add(jLabelDate, gbcLabelDate);
			GridBagConstraints gbcDateFieldCal = new GridBagConstraints();
			gbcDateFieldCal.weightx = 0.5;
			gbcDateFieldCal.fill = GridBagConstraints.HORIZONTAL;
			gbcDateFieldCal.insets = new Insets(5, 5, 5, 5);
			gbcDateFieldCal.gridx = 1;
			gbcDateFieldCal.gridy = 0;
			jPanelData.add(getOpdDateFieldCal(), gbcDateFieldCal);
			GridBagConstraints gbcOpdNumberPanel = new GridBagConstraints();
			gbcOpdNumberPanel.weightx = 0.5;
			gbcOpdNumberPanel.gridwidth = 2;
			gbcOpdNumberPanel.insets = new Insets(5, 5, 5, 5);
			gbcOpdNumberPanel.gridx = 2;
			gbcOpdNumberPanel.gridy = 0;
			jPanelData.add(getJOpdNumberPanel(), gbcOpdNumberPanel);
			JLabel jLabelOpdWard = new JLabel(MessageBundle.getMessage("angal.common.ward.txt"));
			GridBagConstraints gbcLabelOpdWard = new GridBagConstraints();
			gbcLabelOpdWard.insets = new Insets(0, 0, 5, 0);
			gbcLabelOpdWard.anchor = GridBagConstraints.EAST;
			gbcLabelOpdWard.gridx = 3;
			gbcLabelOpdWard.gridy = 0;
			jPanelData.add(jLabelOpdWard, gbcLabelOpdWard);
			GridBagConstraints gbcWardPanel = new GridBagConstraints();
			gbcWardPanel.weightx = 0.5;
			gbcWardPanel.anchor = GridBagConstraints.WEST;
			gbcWardPanel.fill = GridBagConstraints.BOTH;
			gbcWardPanel.insets = new Insets(5, 5, 5, 5);
			gbcWardPanel.gridx = 4;
			gbcWardPanel.gridy = 0;
			jPanelData.add(getWardBox(), gbcWardPanel);
			JLabel jSearchLabel = new JLabel(MessageBundle.getMessage("angal.common.search.txt"));
			GridBagConstraints gbcSearchLabel = new GridBagConstraints();
			gbcSearchLabel.fill = GridBagConstraints.VERTICAL;
			gbcSearchLabel.anchor = GridBagConstraints.WEST;
			gbcSearchLabel.insets = new Insets(5, 5, 5, 5);
			gbcSearchLabel.gridx = 0;
			gbcSearchLabel.gridy = 1;
			jPanelData.add(jSearchLabel, gbcSearchLabel);
			GridBagConstraints gbcTextPatientSrc = new GridBagConstraints();
			gbcTextPatientSrc.weightx = 0.5;
			gbcTextPatientSrc.fill = GridBagConstraints.HORIZONTAL;
			gbcTextPatientSrc.insets = new Insets(5, 5, 5, 5);
			gbcTextPatientSrc.gridx = 1;
			gbcTextPatientSrc.gridy = 1;
			jPanelData.add(getJTextPatientSrc(), gbcTextPatientSrc);
			GridBagConstraints gbcSearchButton = new GridBagConstraints();
			gbcSearchButton.insets = new Insets(5, 5, 5, 5);
			gbcSearchButton.gridx = 2;
			gbcSearchButton.gridy = 1;
			jPanelData.add(getJSearchButton(), gbcSearchButton);
			GridBagConstraints gbcSearchBox = new GridBagConstraints();
			gbcSearchBox.weightx = 0.5;
			gbcSearchBox.fill = GridBagConstraints.HORIZONTAL;
			gbcSearchBox.insets = new Insets(5, 5, 5, 5);
			gbcSearchBox.gridwidth = 2;
			gbcSearchBox.gridx = 3;
			gbcSearchBox.gridy = 1;
			jPanelData.add(getSearchBox(), gbcSearchBox);
			GridBagConstraints gbcPatientEditButton = new GridBagConstraints();
			gbcPatientEditButton.insets = new Insets(5, 5, 5, 0);
			gbcPatientEditButton.gridx = 5;
			gbcPatientEditButton.gridy = 1;
			jPanelData.add(getJPatientEditButton(), gbcPatientEditButton);

			JLabel jLabelDiseaseType1 = new JLabel(MessageBundle.getMessage("angal.opd.diseasetype.txt"));
			GridBagConstraints gbcLabelDiseaseType1 = new GridBagConstraints();
			gbcLabelDiseaseType1.fill = GridBagConstraints.VERTICAL;
			gbcLabelDiseaseType1.insets = new Insets(5, 5, 5, 5);
			gbcLabelDiseaseType1.anchor = GridBagConstraints.WEST;
			gbcLabelDiseaseType1.gridx = 0;
			gbcLabelDiseaseType1.gridy = 2;
			jPanelData.add(jLabelDiseaseType1, gbcLabelDiseaseType1);
			GridBagConstraints gbcDiseaseTypeBox = new GridBagConstraints();
			gbcDiseaseTypeBox.insets = new Insets(5, 5, 5, 5);
			gbcDiseaseTypeBox.fill = GridBagConstraints.HORIZONTAL;
			gbcDiseaseTypeBox.gridwidth = 4;
			gbcDiseaseTypeBox.gridx = 1;
			gbcDiseaseTypeBox.gridy = 2;
			jPanelData.add(getDiseaseTypeBox(), gbcDiseaseTypeBox);

			// Additional diagnoses panel
			initAdditionalDiagnosisPanel();
			GridBagConstraints gbcAdditional = new GridBagConstraints();
			gbcAdditional.gridwidth = 5;
			gbcAdditional.fill = GridBagConstraints.HORIZONTAL;
			gbcAdditional.insets = new Insets(5, 5, 5, 5);
			gbcAdditional.gridx = 0;
			gbcAdditional.gridy = 3;
			gbcAdditional.weighty = 1.0;
			jPanelData.add(additionalDiagnosisPanel, gbcAdditional);
			/////////////Search text field/////////////
			// Last OPD Visit Label
			jLabelLastOpdVisit = new JLabel(" ");
			jLabelLastOpdVisit.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelLastOpdVisit.setForeground(Color.RED);
			GridBagConstraints gbcLabelLastOpdVisit = new GridBagConstraints();
			gbcLabelLastOpdVisit.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelLastOpdVisit.insets = new Insets(5, 5, 5, 5);
			gbcLabelLastOpdVisit.anchor = GridBagConstraints.EAST;
			gbcLabelLastOpdVisit.gridx = 0;
			gbcLabelLastOpdVisit.gridy = 4;
			jPanelData.add(jLabelLastOpdVisit, gbcLabelLastOpdVisit);
			jFieldLastOpdVisit = new JLabel(" ");
			jFieldLastOpdVisit.setFocusable(false);
			/////////////Diseases combo/////////////
			GridBagConstraints gbcFieldLastOpdVisit = new GridBagConstraints();
			gbcFieldLastOpdVisit.insets = new Insets(5, 5, 5, 0);
			gbcFieldLastOpdVisit.fill = GridBagConstraints.HORIZONTAL;
			gbcFieldLastOpdVisit.gridwidth = 4;
			gbcFieldLastOpdVisit.gridx = 1;
			gbcFieldLastOpdVisit.gridy = 4;
			jPanelData.add(jFieldLastOpdVisit, gbcFieldLastOpdVisit);

			jLabelLastOpdNote = new JLabel(" ");
			jLabelLastOpdNote.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelLastOpdNote.setForeground(Color.RED);
			GridBagConstraints gbcLabelLastOpdNote = new GridBagConstraints();
			gbcLabelLastOpdNote.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelLastOpdNote.insets = new Insets(5, 5, 0, 5);
			gbcLabelLastOpdNote.anchor = GridBagConstraints.EAST;
			gbcLabelLastOpdNote.gridx = 0;
			gbcLabelLastOpdNote.gridy = 5;
			jPanelData.add(jLabelLastOpdNote, gbcLabelLastOpdNote);
			jFieldLastOpdNote = new JLabel(" ");
			jFieldLastOpdNote.setPreferredSize(new Dimension(500, 30));
			jFieldLastOpdNote.setFocusable(false);
			GridBagConstraints gbcFieldLastOpdNote = new GridBagConstraints();
			gbcFieldLastOpdNote.anchor = GridBagConstraints.WEST;
			gbcFieldLastOpdNote.insets = new Insets(5, 5, 0, 0);
			gbcFieldLastOpdNote.gridwidth = 4;
			gbcFieldLastOpdNote.gridx = 1;
			gbcFieldLastOpdNote.gridy = 5;
			jPanelData.add(jFieldLastOpdNote, gbcFieldLastOpdNote);

			GridBagConstraints gbcNextVisitLabel = new GridBagConstraints();
			gbcNextVisitLabel.insets = new Insets(0, 0, 0, 5);
			gbcNextVisitLabel.gridx = 0;
			gbcNextVisitLabel.gridy = 6;
			jPanelData.add(getNextVisitLabel(), gbcNextVisitLabel);
			gbcOpdNextVisitDate = new GridBagConstraints();
			gbcOpdNextVisitDate.insets = new Insets(0, 0, 0, 5);
			gbcOpdNextVisitDate.fill = GridBagConstraints.HORIZONTAL;
			gbcOpdNextVisitDate.gridx = 1;
			gbcOpdNextVisitDate.gridy = 6;
			jPanelData.add(getOpdNextVisitDate(), gbcOpdNextVisitDate);
			GridBagConstraints gbcLabelNextVisitWard = new GridBagConstraints();
			gbcLabelNextVisitWard.insets = new Insets(0, 0, 5, 0);
			gbcLabelNextVisitWard.gridwidth = 2;
			gbcLabelNextVisitWard.anchor = GridBagConstraints.WEST;
			gbcLabelNextVisitWard.gridx = 2;
			gbcLabelNextVisitWard.gridy = 6;
			jPanelData.add(getJNextVisitWardPanel(), gbcLabelNextVisitWard);
		}
		return jPanelData;
	}

	private GoodDateTimeSpinnerChooser getOpdDateFieldCal() {
		if (opdDateFieldCal == null) {
			if (insert) {
				if (RememberDates.getLastOpdVisitDate() == null) {
					visitDateOpd = TimeTools.getNow();
				} else {
					visitDateOpd = RememberDates.getLastOpdVisitDate();
				}
			} else {
				visitDateOpd  = opd.getDate();
			}
			opdDateFieldCal = new GoodDateTimeSpinnerChooser(visitDateOpd);
		}
		return opdDateFieldCal;
	}
	
	private JPanel getJNextVisitWardPanel() {
		JPanel jNextVisitWardPanel = new JPanel();
		jNextVisitWardPanel.add(new JLabel(MessageBundle.getMessage("angal.opd.ward.txt")));
		jNextVisitWardPanel.add(getNextVisitWardBox());
		jNextVisitWardPanel.add(getNextVisitClearButton());
		return jNextVisitWardPanel;
	}
	
	private JPanel getJOpdNumberPanel() {
		if (jOpdNumberPanel == null) {
			jOpdNumberPanel = new JPanel();

			jOpdNumField = new JTextField(10);
			
			jOpdNumField.setFocusable(true);
			if (insert) {
				jOpdNumField.setText(String.valueOf(getOpdProgYear(RememberDates.getLastOpdVisitDate())));
			} else {
				jOpdNumField.setText(String.valueOf(opd.getProgYear()));
				jOpdNumField.setEditable(false);
			}

			jOpdNumberPanel.add(new JLabel(MessageBundle.getMessage("angal.opd.opdnumber.txt")));
			jOpdNumberPanel.add(jOpdNumField);
		}
		return jOpdNumberPanel;
	}
	
	private JComboBox getNextVisitWardBox() {
		if (nextVisitWardBox == null) {
			nextVisitWardBox = new JComboBox();
			Patient patient = opd.getPatient();
			nextVisitWardBox.addItem(null);
			if (patient != null) {
				boolean isFemalePatient = patient.getSex() == 'F';
				boolean isMalePatient = !isFemalePatient;
				wardsList.stream()
						.filter(ward -> !(isFemalePatient && !ward.isFemale()))
						.filter(ward -> !(isMalePatient && !ward.isMale()))
						.forEach(nextVisitWardBox::addItem);
			} else {
				// this is new visit with patient not yet selected
				for (Ward elem : wardsList) {
					nextVisitWardBox.addItem(elem);
				}
			}
			Visit nextVisit = opd.getNextVisit();
			if (!insert && nextVisit != null) {
				nextVisitWardBox.setSelectedItem(nextVisit.getWard());
			} else {
				nextVisitWardBox.setSelectedItem(RememberData.getLastOpdWard());
			}

			nextVisitWardBox.addItemListener(itemEvent -> {
				LocalDateTime visitDate = opdNextVisitDate.getLocalDateTimePermissive();
				Ward wardSelected = (Ward) itemEvent.getItem();
				int newDuration = wardSelected.getVisitDuration();
				nextVisitWardBox.setSelectedItem(wardSelected);
				jPanelData.remove(opdNextVisitDate);
				opdNextVisitDate = new GoodDateTimeVisitChooser(null, newDuration, false);
				if (visitDate != null) {
					if (newDuration != visitDuration) {
						opdNextVisitDate.setDate(visitDate.toLocalDate());
					} else {
						opdNextVisitDate.setDateTime(visitDate);
					}
				}
				visitDuration = newDuration;
				jPanelData.add(opdNextVisitDate, gbcOpdNextVisitDate);
				jPanelData.validate();
				jPanelData.repaint();
			});
			
			if (opdPatient == null) {
				nextVisitWardBox.setEnabled(false);
			}
		}
		return nextVisitWardBox;
	}

	private JComboBox getWardBox() {
		if (opdWardBox == null) {
			opdWardBox = new JComboBox();
			
			for (Ward elem : wardsOPDList) {
				opdWardBox.addItem(elem);
			}
			if (insert) {
				Ward lastOpdWard = RememberData.getLastOpdWard();
				if (lastOpdWard != null) {
					opdWardBox.setSelectedItem(lastOpdWard);
				} else {
					if (opdWardBox.getItemCount() == 1) {
						opdWardBox.setSelectedIndex(0);
					}
				}
			} else {
				if (opd.getWard() != null) {
					opdWardBox.setSelectedItem(opd.getWard());
				}
			}
			
			opdWardBox.addItemListener(itemEvent -> {
				LocalDateTime date = opdNextVisitDate.getLocalDateTime();
				Ward wardSelected = (Ward) itemEvent.getItem();
				int duration = wardSelected.getVisitDuration();

				opdWardBox.setSelectedItem(wardSelected);
				if (date == null) {
					nextVisitWardBox.setSelectedItem(wardSelected);
					jPanelData.remove(opdNextVisitDate);
					opdNextVisitDate = new GoodDateTimeVisitChooser(date, duration);
					jPanelData.add(opdNextVisitDate, gbcOpdNextVisitDate);
					jPanelData.validate();
					jPanelData.repaint();
				}
			});
		}
		return opdWardBox;
	}

	private int getOpdProgYear(LocalDateTime date) {
		int opdNum = 0;
		if (date == null) {
			date = TimeTools.getNow();
		}
		try {
			opdNum = opdBrowserManager.getProgYear(date.getYear()) + 1;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (insert) {
			jOpdNumField.setEditable(opdNum == 1);
		}
		return opdNum;
	}

	private JPanel getJNotePanel() {
		if (jNotePanel == null) {
			jNotePanel = new JPanel();
			jNotePanel = setMyBorder(jNotePanel, MessageBundle.getMessage("angal.opd.notessymptom.txt"));
			JScrollPane jNoteScrollPane = new JScrollPane(getJTextArea());
			jNoteScrollPane.setVerticalScrollBar(new JScrollBar());
			jNoteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jNoteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jNoteScrollPane.validate();
			jNotePanel.setLayout(new BorderLayout(0, 0));
			jNotePanel.add(jNoteScrollPane);
		}
		return jNotePanel;
	}
	
	private JTextArea getJTextArea() {
		if (jNoteTextArea == null) {
			jNoteTextArea = new JTextArea(15, 20);
			jNoteTextArea.setAutoscrolls(true);
			if (!insert) {
				jNoteTextArea.setText(opd.getNote());
			}
			jNoteTextArea.setWrapStyleWord(true);
			jNoteTextArea.setLineWrap(true);
		}
		return jNoteTextArea;
	}

	/**
	 * This method initializes diseaseTypeBox
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiseaseTypeBox() {
		if (diseaseTypeBox == null) {
			diseaseTypeBox = new JComboBox();

			DiseaseType elem2 = null;
			diseaseTypeBox.setMaximumSize(new Dimension(400, 50));
			diseaseTypeBox.addItem(allType);
			for (DiseaseType elem : types) {
				if (!insert && opd.getDisease().getType() != null) {
					if (opd.getDisease().getType().getCode().equals(elem.getCode())) {
						elem2 = elem;
					}
				}
				diseaseTypeBox.addItem(elem);
			}
			if (elem2 != null) {
				diseaseTypeBox.setSelectedItem(elem2);
			} else {
				diseaseTypeBox.setSelectedIndex(0);
			}
		}
		return diseaseTypeBox;
	}
	private VoLimitedTextField getJTextPatientSrc() {
		if (jTextPatientSrc == null) {
			jTextPatientSrc = new VoLimitedTextField(16, 20);
			jTextPatientSrc.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						jSearchButton.doClick();
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
		return jTextPatientSrc;
	}
	private JButton getNextVisitClearButton() {
		if (nextVisitClearButton == null) {
			nextVisitClearButton = new JButton();
			nextVisitClearButton.setIcon(new ImageIcon("rsc/icons/trash_button.png"));
			nextVisitClearButton.setBorderPainted(false);
			nextVisitClearButton.setPreferredSize(new Dimension(20, 20));
			nextVisitClearButton.addActionListener(actionEvent -> {
				nextVisitWardBox.setSelectedIndex(0);
				jPanelData.remove(opdNextVisitDate);
				opdNextVisitDate = new GoodDateTimeVisitChooser(null, visitDuration, false);
				jPanelData.add(getOpdNextVisitDate(), gbcOpdNextVisitDate);
				jPanelData.validate();
				jPanelData.repaint();
			});
		}
		return nextVisitClearButton;
	}

	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setBorderPainted(false);
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			jSearchButton.addActionListener(actionEvent -> {
				jComboPatResult.removeAllItems();
				try {
					pat = patientBrowserManager.getPatientsByOneOfFieldsLike(jTextPatientSrc.getText());
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
					pat = new ArrayList<>();
				}
				getSearchBox(jTextPatientSrc.getText());
			});
		}
		return jSearchButton;
	}

	private void getSearchBox(String key) {
		String[] s1;

		if (key == null || key.compareTo("") == 0) {
			jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.selectapatient.txt"));
			jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.enteranewpatient.txt"));
			jLabelLastOpdVisit.setText(" ");
			jFieldLastOpdVisit.setText(" ");
			jLabelLastOpdNote.setText(" ");
			jFieldLastOpdNote.setText(" ");
			if (jNoteTextArea != null) {
				jNoteTextArea.setText("");
			}
			if (jPanelPatient != null) {
				resetPatient();
			}
		}

		for (Patient elem : pat) {
			if (key != null) {
				s1 = key.split(" ");
				String name = elem.getSearchString();
				int a = 0;
				for (String value : s1) {
					if (name.contains(value.toLowerCase())) {
						a++;
					}
				}
				if (a == s1.length) {
					jComboPatResult.addItem(elem);
				}
			} else {
				jComboPatResult.addItem(elem);
			}
		}
		//ADDED: Workaround for no items
		if (jComboPatResult.getItemCount() == 0) {
			opdPatient = null;
			if (jPanelPatient != null) {
				resetPatient();
			}
			jPatientEditButton.setEnabled(true);
		}
		//ADDED: Workaround for one item only
		if (jComboPatResult.getItemCount() == 1) {
			opdPatient = (Patient) jComboPatResult.getSelectedItem();
			setPatient(opdPatient);
			jPatientEditButton.setEnabled(true);
		}
		//ADDED: Workaround for first item
		if (jComboPatResult.getItemCount() > 0) {

			if (jComboPatResult.getItemAt(0) instanceof Patient) {
				opdPatient = (Patient) jComboPatResult.getItemAt(0);
				setPatient(opdPatient);
				jPatientEditButton.setEnabled(true);
			}
		}
		jTextPatientSrc.requestFocus();
	}
	
	private JComboBox getSearchBox() {
		if (jComboPatResult == null) {
			jComboPatResult = new JComboBox();
			if (opdPatient != null) {
				jComboPatResult.addItem(opdPatient);
				jComboPatResult.setEnabled(false);
				jTextPatientSrc.setEnabled(false);
				jSearchButton.setEnabled(false);
				return jComboPatResult;
			} else {
				jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.selectapatient.txt"));
				jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.enteranewpatient.txt"));
			}

			jComboPatResult.addActionListener(actionEvent -> {

				if (jComboPatResult.getSelectedItem() != null) {
					if (jComboPatResult.getSelectedItem().toString().compareTo(MessageBundle.getMessage("angal.opd.enteranewpatient.txt")) == 0) {
						if (GeneralData.PATIENTEXTENDED) {
							PatientInsertExtended newrecord = new PatientInsertExtended(this, new Patient(), true);
							newrecord.addPatientListener(this);
							newrecord.setVisible(true);
						} else {
							PatientInsert newrecord = new PatientInsert(this, new Patient(), true);
							newrecord.addPatientListener(this);
							newrecord.setVisible(true);
						}

					} else if (jComboPatResult.getSelectedItem().toString().compareTo(MessageBundle.getMessage("angal.opd.selectapatient.txt")) == 0) {
						jPatientEditButton.setEnabled(false);

					} else {
						opdPatient = (Patient) jComboPatResult.getSelectedItem();
						setPatient(opdPatient);
						jPatientEditButton.setEnabled(true);
					}
				}
			});
		}
		return jComboPatResult;
	}
	
	//ADDED: Alex
	private JButton getJPatientEditButton() {
		if (jPatientEditButton == null) {
			jPatientEditButton = new JButton();
			jPatientEditButton.setIcon(new ImageIcon("rsc/icons/edit_button.png"));
			jPatientEditButton.setBorderPainted(false);
			jPatientEditButton.setPreferredSize(new Dimension(20, 20));
			jPatientEditButton.addActionListener(actionEvent -> {
				if (opdPatient != null) {
					if (GeneralData.PATIENTEXTENDED) {
						PatientInsertExtended editrecord = new PatientInsertExtended(this, opdPatient, false);
						editrecord.addPatientListener(this);
						editrecord.setVisible(true);
					} else {
						PatientInsert editrecord = new PatientInsert(this, opdPatient, false);
						editrecord.addPatientListener(this);
						editrecord.setVisible(true);
					}
				}
			});
			if (!insert) {
				jPatientEditButton.setEnabled(false);
			}
		}	
		return jPatientEditButton;
	}

	private JTabbedPane getJTabbedPaneOpd() {
		if (jTabbedPaneOpd == null) {
			jTabbedPaneOpd = new JTabbedPane();
			jTabbedPaneOpd.addTab(MessageBundle.getMessage("angal.common.patient.txt"), getJPanelPatient());
			if (insert && MainMenu.checkUserGrants("btnopdnewoperation")
					|| !insert && MainMenu.checkUserGrants("btnopdeditoperation")) {
				jTabbedPaneOpd.addTab(MessageBundle.getMessage("angal.admission.operation"), getMultiOperationTab());
			}
			jTabbedPaneOpd.setPreferredSize(new Dimension(200, 400));
		}
		return jTabbedPaneOpd;
	}
	
	private JPanel getMultiOperationTab() {
		if (jPanelOperation == null) {
			jPanelOperation = new JPanel();
			jPanelOperation.setLayout(new BorderLayout(0, 0));
			OperationRowOpd operationop = new OperationRowOpd(opd);
			addSurgeryListener(operationop);
			jPanelOperation.add(operationop);
		}
		return jPanelOperation;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {

			jPanelPatient = new JPanel();
			GridBagLayout gblPanelPatient = new GridBagLayout();
			gblPanelPatient.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
			gblPanelPatient.columnWeights = new double[] { 0.0, 1.0, 1.0 };
			gblPanelPatient.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
			jPanelPatient.setLayout(gblPanelPatient);
			setMyMatteBorder(jPanelPatient, MessageBundle.getMessage("angal.common.patient.txt"));

			JLabel jLabelFirstName = new JLabel(MessageBundle.getMessage("angal.opd.firstname.txt") + '\t');
			GridBagConstraints gbcLabelFirstName = new GridBagConstraints();
			gbcLabelFirstName.fill = GridBagConstraints.BOTH;
			gbcLabelFirstName.insets = new Insets(5, 5, 5, 5);
			gbcLabelFirstName.gridx = 0;
			gbcLabelFirstName.gridy = 0;
			jPanelPatient.add(jLabelFirstName, gbcLabelFirstName);
			jFieldFirstName = new VoLimitedTextField(50, 20);
			jFieldFirstName.setEditable(false);
			jFieldFirstName.setFocusable(false);
			GridBagConstraints gbcFieldFirstName = new GridBagConstraints();
			gbcFieldFirstName.insets = new Insets(5, 5, 5, 5);
			gbcFieldFirstName.fill = GridBagConstraints.BOTH;
			gbcFieldFirstName.gridx = 1;
			gbcFieldFirstName.gridy = 0;
			jPanelPatient.add(jFieldFirstName, gbcFieldFirstName);
			JLabel jLabelSecondName = new JLabel(MessageBundle.getMessage("angal.opd.secondname.txt") + '\t');
			GridBagConstraints gbcLabelSecondName = new GridBagConstraints();
			gbcLabelSecondName.insets = new Insets(5, 5, 5, 5);
			gbcLabelSecondName.fill = GridBagConstraints.BOTH;
			gbcLabelSecondName.gridx = 0;
			gbcLabelSecondName.gridy = 1;
			jPanelPatient.add(jLabelSecondName, gbcLabelSecondName);
			jFieldSecondName = new VoLimitedTextField(50, 20);
			jFieldSecondName.setEditable(false);
			jFieldSecondName.setFocusable(false);
			GridBagConstraints gbcFieldSecondName = new GridBagConstraints();
			gbcFieldSecondName.fill = GridBagConstraints.BOTH;
			gbcFieldSecondName.insets = new Insets(5, 5, 5, 5);
			gbcFieldSecondName.gridx = 1;
			gbcFieldSecondName.gridy = 1;
			jPanelPatient.add(jFieldSecondName, gbcFieldSecondName);
			JLabel jLabelAddress = new JLabel(MessageBundle.getMessage("angal.common.address.txt"));
			GridBagConstraints gbcLabelAddress = new GridBagConstraints();
			gbcLabelAddress.fill = GridBagConstraints.BOTH;
			gbcLabelAddress.insets = new Insets(5, 5, 5, 5);
			gbcLabelAddress.gridx = 0;
			gbcLabelAddress.gridy = 2;
			jPanelPatient.add(jLabelAddress, gbcLabelAddress);
			jFieldAddress = new VoLimitedTextField(50, 20);
			jFieldAddress.setEditable(false);
			jFieldAddress.setFocusable(false);
			GridBagConstraints gbcFieldAddress = new GridBagConstraints();
			gbcFieldAddress.fill = GridBagConstraints.BOTH;
			gbcFieldAddress.insets = new Insets(5, 5, 5, 5);
			gbcFieldAddress.gridx = 1;
			gbcFieldAddress.gridy = 2;
			jPanelPatient.add(jFieldAddress, gbcFieldAddress);
			JLabel jLabelCity = new JLabel(MessageBundle.getMessage("angal.common.city.txt"));
			GridBagConstraints gbcLabelCity = new GridBagConstraints();
			gbcLabelCity.fill = GridBagConstraints.BOTH;
			gbcLabelCity.insets = new Insets(5, 5, 5, 5);
			gbcLabelCity.gridx = 0;
			gbcLabelCity.gridy = 3;
			jPanelPatient.add(jLabelCity, gbcLabelCity);
			jFieldCity = new VoLimitedTextField(50, 20);
			jFieldCity.setEditable(false);
			jFieldCity.setFocusable(false);
			GridBagConstraints gbcFieldCity = new GridBagConstraints();
			gbcFieldCity.fill = GridBagConstraints.BOTH;
			gbcFieldCity.insets = new Insets(5, 5, 5, 5);
			gbcFieldCity.gridx = 1;
			gbcFieldCity.gridy = 3;
			jPanelPatient.add(jFieldCity, gbcFieldCity);
			JLabel jLabelNextKin = new JLabel(MessageBundle.getMessage("angal.opd.nextofkin.txt"));
			GridBagConstraints gbcLabelNextKin = new GridBagConstraints();
			gbcLabelNextKin.fill = GridBagConstraints.BOTH;
			gbcLabelNextKin.insets = new Insets(5, 5, 5, 5);
			gbcLabelNextKin.gridx = 0;
			gbcLabelNextKin.gridy = 4;
			jPanelPatient.add(jLabelNextKin, gbcLabelNextKin);
			jFieldNextKin = new VoLimitedTextField(50, 20);
			jFieldNextKin.setEditable(false);
			jFieldNextKin.setFocusable(false);
			GridBagConstraints gbcFieldNextKin = new GridBagConstraints();
			gbcFieldNextKin.fill = GridBagConstraints.BOTH;
			gbcFieldNextKin.insets = new Insets(5, 5, 5, 5);
			gbcFieldNextKin.gridx = 1;
			gbcFieldNextKin.gridy = 4;
			jPanelPatient.add(jFieldNextKin, gbcFieldNextKin);
			JLabel jLabelAge = new JLabel(MessageBundle.getMessage("angal.common.age.txt"));
			GridBagConstraints gbcLabelAge = new GridBagConstraints();
			gbcLabelAge.fill = GridBagConstraints.BOTH;
			gbcLabelAge.insets = new Insets(5, 5, 5, 5);
			gbcLabelAge.gridx = 0;
			gbcLabelAge.gridy = 5;
			jPanelPatient.add(jLabelAge, gbcLabelAge);
			jFieldAge = new VoLimitedTextField(50, 20);
			jFieldAge.setEditable(false);
			jFieldAge.setFocusable(false);
			GridBagConstraints gbcFieldAge = new GridBagConstraints();
			gbcFieldAge.fill = GridBagConstraints.BOTH;
			gbcFieldAge.insets = new Insets(5, 5, 5, 5);
			gbcFieldAge.gridx = 1;
			gbcFieldAge.gridy = 5;
			jPanelPatient.add(jFieldAge, gbcFieldAge);
			JLabel jLabelSex = new JLabel(MessageBundle.getMessage("angal.common.sex.txt"));
			GridBagConstraints gbcLabelSex = new GridBagConstraints();
			gbcLabelSex.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelSex.insets = new Insets(5, 5, 5, 5);
			gbcLabelSex.gridx = 0;
			gbcLabelSex.gridy = 6;
			jPanelPatient.add(jLabelSex, gbcLabelSex);
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			JPanel jPanelSex = new JPanel();
			jPanelSex.add(radiom);
			jPanelSex.add(radiof);
			GridBagConstraints gbcPanelSex = new GridBagConstraints();
			gbcPanelSex.insets = new Insets(5, 5, 5, 5);
			gbcPanelSex.fill = GridBagConstraints.HORIZONTAL;
			gbcPanelSex.gridx = 1;
			gbcPanelSex.gridy = 6;
			jPanelPatient.add(jPanelSex, gbcPanelSex);
			GridBagConstraints gbcPatientNote = new GridBagConstraints();
			gbcPatientNote.fill = GridBagConstraints.BOTH;
			gbcPatientNote.insets = new Insets(5, 5, 5, 5);
			gbcPatientNote.gridx = 2;
			gbcPatientNote.gridy = 0;
			gbcPatientNote.gridheight = 7;
			jPanelPatient.add(getJPatientNote(), gbcPatientNote);

			ButtonGroup group = new ButtonGroup();
			group.add(radiom);
			group.add(radiof);
			radiom.setSelected(true);
			radiom.setEnabled(false);
			radiof.setEnabled(false);
			radiom.setFocusable(false);
			radiof.setFocusable(false);

			if (opdPatient != null) {
				setPatient(opdPatient);
			}
		}
		return jPanelPatient;
	}
	
	private JPanel getJPatientNote() {
		if (jPatientNotePanel == null) {
			jPatientNotePanel = new JPanel(new BorderLayout());
			JScrollPane jPatientScrollNote = new JScrollPane(getJPatientNoteArea());
			jPatientScrollNote.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jPatientScrollNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jPatientScrollNote.setAutoscrolls(false);
			jPatientScrollNote.validate();
			jPatientNotePanel.add(jPatientScrollNote, BorderLayout.CENTER);
		}
		return jPatientNotePanel;
	}
	
	private JTextArea getJPatientNoteArea() {
		if (jPatientNote == null) {
			jPatientNote = new JTextArea(15, 15);
			if (!insert) {
				jPatientNote.setText(opdPatient.getNote());
			}
			jPatientNote.setLineWrap(true);
			jPatientNote.setEditable(false);
			jPatientNote.setFocusable(false);
		}
		return jPatientNote;
	}

	/**
	 * This method initializes jPanelButtons	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getOkButton(), null);
			if (insert && MainMenu.checkUserGrants("btnopdnewexamination") || 
					!insert && MainMenu.checkUserGrants("btnopdeditexamination")) {
				jPanelButtons.add(getJAnamnesisButton(), null);
				jPanelButtons.add(getJButtonExamination(), null);
			}
			jPanelButtons.add(getCancelButton(), null);
		}
		return jPanelButtons;
	}
	
	private JButton getJButtonExamination() {
		if (jButtonExamination == null) {
			jButtonExamination = new JButton(MessageBundle.getMessage("angal.opd.examination.btn"));
			jButtonExamination.setMnemonic(MessageBundle.getMnemonic("angal.opd.examination.btn.key"));
			
			jButtonExamination.addActionListener(actionEvent -> {
				if (opdPatient == null) {
					MessageDialog.error(this,"angal.common.pleaseselectapatient.msg");
					return;
				}

				PatientExamination patex;
				PatientExamination lastPatex = null;
				try {
					lastPatex = examinationBrowserManager.getLastByPatID(opdPatient.getCode());
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (lastPatex != null) {
					patex = examinationBrowserManager.getFromLastPatientExamination(lastPatex);
				} else {
					patex = examinationBrowserManager.getDefaultPatientExamination(opdPatient);
				}

				GenderPatientExamination gpatex = new GenderPatientExamination(patex, opdPatient.getSex() == 'M');

				PatientExaminationEdit dialog = new PatientExaminationEdit(this, gpatex);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.showAsModal(this);
			});
		}
		return jButtonExamination;
	}

	private JButton getJAnamnesisButton() {
		if (jAnamnesisButton == null) {
			jAnamnesisButton = new JButton(MessageBundle.getMessage("angal.anamnesis.open.anamnesis.btn"));
			jAnamnesisButton.setMnemonic(MessageBundle.getMnemonic("angal.opd.anamnesis.btn.key"));
			jAnamnesisButton.addActionListener(actionEvent -> {
				try {
					if (opdPatient == null) {
						MessageDialog.error(this,"angal.common.pleaseselectapatient.msg");
						return;
					}
					PatientHistory ph = new PatientHistory();
					ph.setPatientId(opdPatient.getCode());
					Patient patient = this.patientBrowserManager.getPatientById(opdPatient.getCode());
					PatientHistory patientHistory = Optional.ofNullable(this.patientHistoryManager.getByPatientId(opdPatient.getCode())).orElse(ph);
					PatientPatientHistory pph = new PatientPatientHistory(patientHistory, patient);
					PatientHistoryEdit dialog = new PatientHistoryEdit(this, pph, true);
					dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setModal(insert);
					dialog.setVisible(true);
				} catch (OHServiceException e) {
					LOGGER.error("Exception in getJAnamnesisButton method.", e);
				}
			});
		}
		return jAnamnesisButton;
	}

	
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				
				visitDateOpd = opdDateFieldCal.getLocalDateTime();
				if (visitDateOpd != null) {
					opd.setDate(visitDateOpd);
				} else {
					opd.setDate(TimeTools.getNow());
				}
				int opdProgYear = 0;
				
				if (jOpdNumField.isEditable()) {
					try {
						opdProgYear = Integer.parseInt(jOpdNumField.getText());
						if (opdBrowserManager.isExistOpdNum(opdProgYear, visitDateOpd.getYear())) {
							MessageDialog.error(this, "angal.opd.opdnumberalreadyexist.msg");
							if (insert) {
								jOpdNumField.setText(String.valueOf(getOpdProgYear(visitDateOpd)));
							}
							jOpdNumField.requestFocusInWindow();
							return;
						}
					} catch (NumberFormatException e) {
						MessageDialog.error(this, "angal.opd.opdnumbermustbeanumber.msg");
						jOpdNumField.requestFocusInWindow();
						return;
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				} else {
					opdProgYear = getOpdProgYear(visitDateOpd);
				}
				
				char newPatient;
				String referralTo;
				String referralFrom;
				Ward opdWard = (Ward) opdWardBox.getSelectedItem();

				if (newPatientButton.isSelected()) {
					newPatient = 'N';
				} else {
					newPatient = 'R';
				}
				if (referralToCheckBox.isSelected()) {
					referralTo = "R";
				} else {
					referralTo = "";
				}
				if (referralFromCheckBox.isSelected()) {
					referralFrom = "R";
				} else {
					referralFrom = "";
				}

				// nextVisit - the presence of opdNextVisitDate drives the management of the visit linked to the OPD
				Visit nextVisit = opd.getNextVisit();
				boolean isNextVisit = false;
				LocalDateTime nextVisitDateTime = opdNextVisitDate.getLocalDateTimePermissive();
				if (nextVisitDateTime != null) {
					if (nextVisitDateTime.getMinute() == 0 && nextVisitDateTime.getHour() == 0) {
						MessageDialog.error(this, "angal.opd.pleasechooseavalidtimeforthenextvisit.msg");
						return;
					}
					if (nextVisitDateTime.isBefore(visitDateOpd)) {
						MessageDialog.error(this, "angal.opd.cannotsetadateinthepastfornextvisit.msg");
						return;
					}
					Ward ward = (Ward) nextVisitWardBox.getSelectedItem();
					if (ward == null) {
						MessageDialog.error(this, "angal.opd.pleasechooseawardforthenextvisit.msg");
						return;
					}
					isNextVisit = true;
					if (nextVisit == null) {
						nextVisit = new Visit();
					}
					nextVisit.setPatient(opdPatient);
					nextVisit.setDate(nextVisitDateTime);
					nextVisit.setWard(ward);
					nextVisit.setDuration(ward.getVisitDuration());
					nextVisit.setService(""); // future developments
					//nextVisit.setNote(); // future developments
				}

				opd.setNote(jNoteTextArea.getText());
				opd.setPatient(opdPatient);
				opd.setNewPatient(newPatient);
				opd.setReferralFrom(referralFrom);
				opd.setReferralTo(referralTo);
				updateOpdDiagnosesList();
				opd.setUserID(UserBrowsingManager.getCurrentUser());
				opd.setWard(opdWard);

				try {
					if (insert) { // Insert
						opd.setProgYear(opdProgYear);
						if (isNextVisit) {
							try {
								nextVisit = visitManager.newVisit(nextVisit);
							} catch (OHServiceException e) {
								OHServiceExceptionUtil.showMessages(e);
								return;
							}
							opd.setNextVisit(nextVisit);
						}
						Opd insertedOpd = opdBrowserManager.newOpd(opd);
						if (insertedOpd != null) {
							RememberDates.setLastOpdVisitDate(visitDateOpd);
							RememberData.setLastOpdWard(opdWard);
							fireSurgeryInserted(opd);
							dispose();
						} else {
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
                        }
					} else { // Update
						if (isNextVisit) {
							nextVisit = visitManager.updateVisit(nextVisit);
							opd.setNextVisit(nextVisit);
						} else {
							opd.setNextVisit(null);
						}
						Opd updatedOpd = opdBrowserManager.updateOpd(opd);
						if (updatedOpd == null) {
							MessageDialog.error(this, "angal.common.datacouldnotbesaved.msg");
						} else {
							fireSurgeryUpdated(updatedOpd);
							// can't delete the visit info until the OPD is updated
							if (!isNextVisit && nextVisit != null) {
								visitManager.deleteVisit(nextVisit);
							}
							dispose();
						}
					}
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
			});
		}
		return okButton;
	}
	
	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> {
				//to free Memory
				pat.clear();
				diseasesAll.clear();
				diseasesOPD.clear();
				types.clear();
				jComboPatResult.removeAllItems();
				diseaseTypeBox.removeAllItems();
				dispose();
			});
		}
		return cancelButton;
	}
	
	/*
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel c, String title) {
		Border b2 = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), BorderFactory
						.createEmptyBorder(0, 0, 0, 0));
		c.setBorder(b2);
		return c;
	}
	
	/*
	 * Set a specific border+title+matte to a panel
	 */
	private JPanel setMyMatteBorder(JPanel c, String title) {
		c.setBorder(new TitledBorder(new MatteBorder(1, 20, 1, 1, new Color(153, 180, 209)), title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		return c;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

	}


	private JLabel getNextVisitLabel() {
		if (nextVisitLabel == null) {
			nextVisitLabel = new JLabel(MessageBundle.getMessage("angal.opd.nextvisitdate.txt"));
		}
		return nextVisitLabel;
	}
	
	private GoodDateTimeVisitChooser getOpdNextVisitDate() {
		if (opdNextVisitDate == null) {

			LocalDateTime nextDate = null;
			Visit nextVisit = opd.getNextVisit();
			visitDuration = DEFAULT_VISIT_DURATION;
			if (!insert) {
				if (nextVisit != null) {
					nextDate = nextVisit.getDate();
					visitDuration = nextVisit.getWard().getVisitDuration();
				} else {
					Ward selectedWard = RememberData.getLastOpdWard();
					if (selectedWard != null) {
						visitDuration = selectedWard.getVisitDuration();
					}
				}
			}

			opdNextVisitDate = new GoodDateTimeVisitChooser(nextDate, visitDuration, false);

			if (opdPatient == null) {
				opdNextVisitDate.setEnabled(false);
			}
		}
		return opdNextVisitDate;
	}
	/**
	 * Initializes the additional diagnoses panel
	 */
	private void initAdditionalDiagnosisPanel() {

		selectedDiagnosisModel = new DefaultListModel<>();
		if (!insert && opd.getExtraDiagnosesList() != null) {
			for (Disease disease : opd.getExtraDiagnosesList()) {
				if (disease != null) {
					selectedDiagnosisModel.addElement(disease);
				}
			}
		}

		additionalDiagnosisPanel = new JPanel(new BorderLayout());
		additionalDiagnosisPanel.setBorder(BorderFactory.createTitledBorder("Add New Diagnostic"));
		additionalDiagnosisPanel.setPreferredSize(new Dimension(600, 150));
		additionalDiagnosisPanel.setMinimumSize(new Dimension(500, 150));
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchDiagnosisField = new JTextField(15);
		topPanel.add(searchDiagnosisField);

		searchDiagnosisButton = new JButton();
		searchDiagnosisButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		searchDiagnosisButton.setPreferredSize(new Dimension(24, 24));
		searchDiagnosisButton.addActionListener(e -> performDiagnosisSearch());
		topPanel.add(searchDiagnosisButton);

		topPanel.add(Box.createHorizontalStrut(10));

		browseDiagnosisCombo = new JComboBox<>();
		browseDiagnosisCombo.setPreferredSize(new Dimension(250, 28));
		browseDiagnosisCombo.addItem(null);
		for (Disease disease : diseasesOPD) {
			browseDiagnosisCombo.addItem(disease);
		}
		browseDiagnosisCombo.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				Disease selected = (Disease) e.getItem();
				if (selected != null) {
					SwingUtilities.invokeLater(() -> {
						addSelectedDiagnosisToModel(selected);
						browseDiagnosisCombo.setSelectedItem(null);
					});
				}
			}
		});

		topPanel.add(browseDiagnosisCombo);

		addDiagnosisButton = new JButton("Add New Disease");
		addDiagnosisButton.addActionListener(e -> showAddDiseaseDialog());
		topPanel.add(addDiagnosisButton);

		additionalDiagnosisPanel.add(topPanel, BorderLayout.NORTH);
		selectedDiagnosisContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		selectedDiagnosisContainer.setVisible(false);

		selectedDiagnosisScrollPane = new JScrollPane(selectedDiagnosisContainer);
		selectedDiagnosisScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		selectedDiagnosisScrollPane.setPreferredSize(new Dimension(500, 200));
		refreshSelectedDisplay();
		searchDiagnosisField.addActionListener(e -> performDiagnosisSearch());
	}

	/**
	 * Shows the dialog to add a new disease
	 *
	 * @return
	 */
	private Disease showAddDiseaseDialog() {
		Disease newDisease = new Disease();
		newDisease.setOpdInclude(true);
		newDisease.setIpdInInclude(false);
		newDisease.setIpdOutInclude(false);
		DiseaseEdit dialog = new DiseaseEdit((JFrame) getOwner(), newDisease, true);
		dialog.setModal(true);
		dialog.setVisible(true);

		if (newDisease.getCode() != null && !newDisease.getCode().isEmpty()) {
			diseasesOPD.add(newDisease);
			browseDiagnosisCombo.addItem(newDisease);
			addSelectedDiagnosisToModel(newDisease);
		}
		return newDisease;
	}

	/**
	 * Performs the disease search
	 */
	private void performDiagnosisSearch() {
		String query = searchDiagnosisField.getText().trim().toLowerCase();

		browseDiagnosisCombo.removeAllItems();
		browseDiagnosisCombo.addItem(null);

		for (Disease disease : diseasesOPD) {
			if (query.isEmpty() || disease.getDescription().toLowerCase().contains(query)) {
				browseDiagnosisCombo.addItem(disease);
			}
		}

		browseDiagnosisCombo.showPopup();
	}

	/**
	 * Adds a disease to the selected diagnoses model
	 */
	private void addSelectedDiagnosisToModel(Disease disease) {
		if (disease == null) return;

		for (int i = 0; i < selectedDiagnosisModel.size(); i++) {
			if (selectedDiagnosisModel.get(i).getCode().equals(disease.getCode())) {
				MessageDialog.warning(this, "Diagnosis already added");
				return;
			}
		}

		selectedDiagnosisModel.addElement(disease);
		refreshSelectedDisplay();
		updateOpdDiagnosesList();

		browseDiagnosisCombo.setSelectedItem(null);
		searchDiagnosisField.setText("");
		performDiagnosisSearch();
	}

    /**
     * Refreshes the display of selected diagnoses (tags with remove buttons)
     */
    private void refreshSelectedDisplay() {
        selectedDiagnosisContainer.removeAll();

        if (selectedDiagnosisModel.isEmpty()) {
            if (selectedDiagnosisScrollPane.getParent() != null) {
                additionalDiagnosisPanel.remove(selectedDiagnosisScrollPane);
            }
        } else {
            if (selectedDiagnosisScrollPane.getParent() == null) {
                additionalDiagnosisPanel.add(selectedDiagnosisScrollPane, BorderLayout.CENTER);
            }

            for (int i = 0; i < selectedDiagnosisModel.size(); i++) {
                Disease disease = selectedDiagnosisModel.get(i);
                JPanel tagPanel = createTagPanel(i + 1, disease);
                selectedDiagnosisContainer.add(tagPanel);
            }

            selectedDiagnosisContainer.setVisible(true);
        }

        selectedDiagnosisContainer.revalidate();
        selectedDiagnosisContainer.repaint();
        additionalDiagnosisPanel.revalidate();
        additionalDiagnosisPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            additionalDiagnosisPanel.repaint();
            if (getContentPane() != null) {
                getContentPane().revalidate();
                getContentPane().repaint();
            }
        });
    }

	/**
	 * Creates a tag panel for a single diagnosis
	 */
	private JPanel createTagPanel(int index, Disease disease) {
		JPanel tag = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
		tag.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 1));
		tag.setBackground(new Color(220, 240, 255));

		JLabel numberLabel = new JLabel(index + ".");
		numberLabel.setFont(numberLabel.getFont().deriveFont(Font.BOLD));
		tag.add(numberLabel);

		JLabel diseaseLabel = new JLabel(disease.getDescription());
		tag.add(diseaseLabel);

		JButton removeButton = new JButton("×");
		removeButton.setPreferredSize(new Dimension(20, 20));
		removeButton.setBorderPainted(false);
		removeButton.setContentAreaFilled(false);
		removeButton.addActionListener(e -> {
			selectedDiagnosisModel.removeElement(disease);
			refreshSelectedDisplay();
			updateOpdDiagnosesList();
		});
		tag.add(removeButton);

		return tag;
	}

	/**
	 * Updates the OPD object with the current list of diagnoses
	 */
	private void updateOpdDiagnosesList() {
		List<Disease> list = new ArrayList<>();
		for (int i = 0; i < selectedDiagnosisModel.size(); i++) {
			list.add(selectedDiagnosisModel.get(i));
		}
		opd.setExtraDiagnosesList(list);
	}

}
