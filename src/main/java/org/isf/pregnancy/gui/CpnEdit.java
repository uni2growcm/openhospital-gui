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
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import org.isf.admission.gui.AdmissionBrowser;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.admission.model.AdmittedPatient;
import org.isf.familyplanning.gui.FamilyPlanningEdit;
import org.isf.generaldata.MessageBundle;
import org.isf.hivchildfollowup.gui.HivExposedChildEdit;
import org.isf.lab.gui.LabNew;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.model.Patient;
import org.isf.patvac.gui.PatVacEdit;
import org.isf.patvac.model.PatientVaccine;
import org.isf.pregnancy.manager.PregnancyBrowserManager;
import org.isf.pregnancy.manager.PregnancyExamParameterBrowserManager;
import org.isf.pregnancy.manager.PregnancyExamResultBrowserManager;
import org.isf.pregnancy.manager.PregnancyVisitBrowserManager;
import org.isf.pregnancy.model.Pregnancy;
import org.isf.pregnancy.model.PregnancyExamDataType;
import org.isf.pregnancy.model.PregnancyExamParameter;
import org.isf.pregnancy.model.PregnancyExamResult;
import org.isf.pregnancy.model.PregnancyVisit;
import org.isf.pregtreattype.manager.PregnantTreatmentTypeBrowserManager;
import org.isf.pregtreattype.model.PregnantTreatmentType;
import org.isf.therapy.gui.TherapyEdit;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextArea;
import org.isf.utils.time.TimeTools;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.model.VaccineType;

/**
 * CPN visit screen: records/edits a {@link Pregnancy} (DDR / gestité-parité) and a {@link PregnancyVisit}
 * (the visit being registered today, with its dynamic "paramètres CPN"), and gives quick access ("raccourcis")
 * to the patient's other records (paramètres patient, examens, thérapies, vaccins).
 */
public class CpnEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JFrame ownerFrame;
	private final Patient patient;
	private final Pregnancy pregnancy;
	private final boolean insertPregnancy;

	private final PregnancyBrowserManager pregnancyManager = Context.getApplicationContext().getBean(PregnancyBrowserManager.class);
	private final PregnancyVisitBrowserManager visitManager = Context.getApplicationContext().getBean(PregnancyVisitBrowserManager.class);
	private final PregnancyExamParameterBrowserManager examParameterManager =
			Context.getApplicationContext().getBean(PregnancyExamParameterBrowserManager.class);
	private final PregnancyExamResultBrowserManager examResultManager =
			Context.getApplicationContext().getBean(PregnancyExamResultBrowserManager.class);
	private final PregnantTreatmentTypeBrowserManager treatmentTypeManager =
			Context.getApplicationContext().getBean(PregnantTreatmentTypeBrowserManager.class);
	private final AdmissionBrowserManager admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);

	private PregnancyDeliveryPanel deliveryPanel;

	private GoodDateChooser lmpChooser;
	private JTextField scheduledDeliveryField;
	private JSpinner nPregnanciesSpinner;
	private JSpinner nTermDeliveriesSpinner;
	private JSpinner nPretermDeliveriesSpinner;
	private JSpinner nAbortionsSpinner;
	private JSpinner nLiveChildrenSpinner;
	private JSpinner nStillbirthsSpinner;
	private JSpinner nDeceasedChildrenSpinner;
	private JSpinner nTotalDesiredChildrenSpinner;
	private JSpinner ageOfLastChildSpinner;
	private JCheckBox breastfeedingCheckBox;

	private JComboBox<String> visitTypeComboBox;
	private GoodDateTimeSpinnerChooser visitDateChooser;
	private GoodDateChooser nextVisitDateChooser;
	private JList<PregnantTreatmentType> treatmentTypeList;
	private VoLimitedTextArea noteTextArea;

	private JPanel parametersPanel;
	private final Map<String, JComponent> parameterControls = new LinkedHashMap<>();
	private List<PregnancyExamParameter> currentParameters;

	public CpnEdit(JFrame owner, Patient patient, Pregnancy pregnancy) {
		super(owner, true);
		this.ownerFrame = owner;
		this.patient = patient;
		this.insertPregnancy = pregnancy == null;
		this.pregnancy = pregnancy == null ? new Pregnancy(patient, LocalDate.now()) : pregnancy;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.cpn.cpnedit.title") + " - " + patient.getFirstName() + ' ' + patient.getSecondName());
		JPanel content = new JPanel(new BorderLayout());
		content.add(getShortcutsPanel(), BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getPregnancyPanel(), getVisitPanel());
		splitPane.setResizeWeight(0.5);

		JTabbedPane mainTabs = new JTabbedPane();
		mainTabs.addTab(MessageBundle.getMessage("angal.cpn.visit.txt"), splitPane);
		mainTabs.addTab(MessageBundle.getMessage("angal.cpn.delivery.tab.title"), getDeliveryTab());
		content.add(mainTabs, BorderLayout.CENTER);

		content.add(getButtonPanel(), BorderLayout.SOUTH);
		setContentPane(content);
		setSize(950, 650);
		setLocationRelativeTo(null);
	}

	/*
	 * ----------------------------------------------------------------
	 * Raccourcis : paramètres patient / examens / thérapies / vaccins
	 * ----------------------------------------------------------------
	 */
	private JPanel getShortcutsPanel() {
		JPanel shortcuts = new JPanel();
		shortcuts.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.shortcuts.txt")));

		JButton patientParamsButton = new JButton(MessageBundle.getMessage("angal.cpn.shortcut.patientparams.btn"));
		patientParamsButton.addActionListener(e -> new PatientInsertExtended(this, patient, false).setVisible(true));
		shortcuts.add(patientParamsButton);

		JButton examsButton = new JButton(MessageBundle.getMessage("angal.cpn.shortcut.exams.btn"));
		examsButton.addActionListener(e -> {
			// LabNew shows itself synchronously from its constructor, so this dialog must already be
			// hidden before it is built - otherwise, being application-modal, it blocks that JFrame from
			// ever receiving focus/input.
			setVisible(false);
			LabNew labNew = new LabNew(ownerFrame, patient);
			labNew.addWindowListener(closeListenerToRestoreThisDialog());
		});
		shortcuts.add(examsButton);

		JButton therapiesButton = new JButton(MessageBundle.getMessage("angal.cpn.shortcut.therapies.btn"));
		therapiesButton.addActionListener(e -> {
			setVisible(false);
			TherapyEdit therapyEdit = new TherapyEdit(ownerFrame, patient, false);
			therapyEdit.addWindowListener(closeListenerToRestoreThisDialog());
			therapyEdit.setVisible(true);
		});
		shortcuts.add(therapiesButton);

		JButton vaccinesButton = new JButton(MessageBundle.getMessage("angal.cpn.shortcut.vaccines.btn"));
		vaccinesButton.addActionListener(e -> {
			PatientVaccine patientVaccine = new PatientVaccine(0, 0, TimeTools.getNow(), patient,
					new Vaccine("", "", new VaccineType("", "")), 0);
			new PatVacEdit(ownerFrame, patientVaccine, true).setVisible(true);
		});
		shortcuts.add(vaccinesButton);

		JButton familyPlanningButton = new JButton(MessageBundle.getMessage("angal.cpn.shortcut.familyplanning.btn"));
		familyPlanningButton.addActionListener(e -> new FamilyPlanningEdit(ownerFrame, patient, null).setVisible(true));
		shortcuts.add(familyPlanningButton);

		JButton hivFollowUpButton = new JButton(MessageBundle.getMessage("angal.cpn.shortcut.hivchildfollowup.btn"));
		hivFollowUpButton.addActionListener(e -> new HivExposedChildEdit(ownerFrame, patient, null).setVisible(true));
		shortcuts.add(hivFollowUpButton);

		return shortcuts;
	}

	/**
	 * Restores this (application-modal) dialog once a shortcut window opened while it was hidden - see
	 * {@link #getShortcutsPanel()} - is closed.
	 */
	private WindowAdapter closeListenerToRestoreThisDialog() {
		return new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				setVisible(true);
				toFront();
			}
		};
	}

	/*
	 * ----------------------------------------------------------------
	 * Grossesse : DDR -> DPA auto, gestité / parité
	 * ----------------------------------------------------------------
	 */
	private JPanel getPregnancyPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.pregnancy.txt")));

		lmpChooser = new GoodDateChooser(pregnancy.getLmp() == null ? LocalDate.now() : pregnancy.getLmp(), false, false);
		lmpChooser.addDateChangeListener(e -> updateScheduledDelivery());
		panel.add(new JLabel(MessageBundle.getMessage("angal.cpn.lmp.txt") + ':'));
		panel.add(lmpChooser);

		scheduledDeliveryField = new JTextField();
		scheduledDeliveryField.setEditable(false);
		panel.add(new JLabel(MessageBundle.getMessage("angal.cpn.scheduleddelivery.txt") + ':'));
		panel.add(scheduledDeliveryField);
		updateScheduledDelivery();

		nPregnanciesSpinner = addIntSpinner(panel, "angal.cpn.npregnancies.txt", pregnancy.getnPregnancies());
		nTermDeliveriesSpinner = addIntSpinner(panel, "angal.cpn.ntermdeliveries.txt", pregnancy.getnTermDeliveries());
		nPretermDeliveriesSpinner = addIntSpinner(panel, "angal.cpn.npretermdeliveries.txt", pregnancy.getnPretermDeliveries());
		nAbortionsSpinner = addIntSpinner(panel, "angal.cpn.nabortions.txt", pregnancy.getnAbortions());
		nLiveChildrenSpinner = addIntSpinner(panel, "angal.cpn.nlivechildren.txt", pregnancy.getnLiveChildren());
		nStillbirthsSpinner = addIntSpinner(panel, "angal.cpn.nstillbirths.txt", pregnancy.getnStillbirths());
		nDeceasedChildrenSpinner = addIntSpinner(panel, "angal.cpn.ndeceasedchildren.txt", pregnancy.getnDeceasedChildren());
		nTotalDesiredChildrenSpinner = addIntSpinner(panel, "angal.cpn.ntotaldesiredchildren.txt", pregnancy.getnTotalDesiredChildren());
		ageOfLastChildSpinner = addIntSpinner(panel, "angal.cpn.ageoflastchild.txt",
				pregnancy.getAgeOfLastChild() == null ? 0 : pregnancy.getAgeOfLastChild());

		breastfeedingCheckBox = new JCheckBox(MessageBundle.getMessage("angal.cpn.breastfeeding.txt"));
		breastfeedingCheckBox.setSelected(pregnancy.isBreastfeeding());
		panel.add(breastfeedingCheckBox);
		panel.add(new JLabel());

		return panel;
	}

	private JSpinner addIntSpinner(JPanel panel, String labelKey, int initial) {
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 0, 50, 1));
		panel.add(new JLabel(MessageBundle.getMessage(labelKey) + ':'));
		panel.add(spinner);
		return spinner;
	}

	private void updateScheduledDelivery() {
		LocalDate lmp = lmpChooser.getDate();
		LocalDate scheduledDelivery = lmp == null ? null : lmp.plusMonths(9);
		scheduledDeliveryField.setText(scheduledDelivery == null ? "" : scheduledDelivery.toString());
	}

	/*
	 * ----------------------------------------------------------------
	 * Visite du jour : type, prochaine visite, traitements, paramètres CPN
	 * ----------------------------------------------------------------
	 */
	private JPanel getVisitPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.visit.txt")));

		JPanel header = new JPanel(new GridLayout(0, 2, 5, 5));

		visitTypeComboBox = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.cpn.prenatal.txt"),
				MessageBundle.getMessage("angal.cpn.postnatal.txt") });
		visitTypeComboBox.addActionListener(e -> refreshParametersPanel());
		header.add(new JLabel(MessageBundle.getMessage("angal.cpn.visittype.txt") + ':'));
		header.add(visitTypeComboBox);

		visitDateChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());
		header.add(new JLabel(MessageBundle.getMessage("angal.cpn.visitdate.txt") + ':'));
		header.add(visitDateChooser);

		nextVisitDateChooser = new GoodDateChooser(null, true, true);
		header.add(new JLabel(MessageBundle.getMessage("angal.cpn.nextvisitdate.txt") + ':'));
		header.add(nextVisitDateChooser);

		panel.add(header, BorderLayout.NORTH);

		JPanel center = new JPanel(new BorderLayout());
		center.add(getTreatmentTypePanel(), BorderLayout.WEST);
		parametersPanel = new JPanel();
		parametersPanel.setLayout(new GridLayout(0, 2, 5, 5));
		center.add(new JScrollPane(parametersPanel), BorderLayout.CENTER);
		panel.add(center, BorderLayout.CENTER);

		noteTextArea = new VoLimitedTextArea(500, 3, 30);
		JPanel notePanel = new JPanel(new BorderLayout());
		notePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.note.txt")));
		notePanel.add(new JScrollPane(noteTextArea), BorderLayout.CENTER);
		panel.add(notePanel, BorderLayout.SOUTH);

		refreshParametersPanel();

		return panel;
	}

	private JPanel getTreatmentTypePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.treatmenttypes.txt")));
		DefaultListModel<PregnantTreatmentType> model = new DefaultListModel<>();
		try {
			for (PregnantTreatmentType t : treatmentTypeManager.getPregnantTreatmentType()) {
				model.addElement(t);
			}
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		treatmentTypeList = new JList<>(model);
		treatmentTypeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panel.add(new JScrollPane(treatmentTypeList), BorderLayout.CENTER);
		return panel;
	}

	private int getSelectedVisitType() {
		return visitTypeComboBox.getSelectedIndex() == 0 ? PregnancyVisit.PRENATAL : PregnancyVisit.POSTNATAL;
	}

	private void refreshParametersPanel() {
		parametersPanel.removeAll();
		parameterControls.clear();
		try {
			currentParameters = examParameterManager.getPregnancyExamParametersForVisitType(getSelectedVisitType());
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
			currentParameters = List.of();
		}
		for (PregnancyExamParameter parameter : currentParameters) {
			String label = parameter.getDescription() + (parameter.getUnit() != null ? " (" + parameter.getUnit() + ')' : "");
			parametersPanel.add(new JLabel(label + ':'));
			JComponent control = buildControlFor(parameter);
			parameterControls.put(parameter.getCode(), control);
			parametersPanel.add(control);
		}
		parametersPanel.revalidate();
		parametersPanel.repaint();
	}

	private JComponent buildControlFor(PregnancyExamParameter parameter) {
		switch (parameter.getDataType()) {
			case NUMERIC:
				double max = parameter.getMaxValue() == null ? Double.MAX_VALUE : parameter.getMaxValue();
				return new JSpinner(new SpinnerNumberModel(0.0, 0.0, max, 0.5));
			case ENUM:
				JComboBox<String> combo = new JComboBox<>();
				combo.addItem("");
				for (String value : parameter.getAllowedValuesList()) {
					combo.addItem(value);
				}
				return combo;
			case BOOLEAN:
				return new JCheckBox();
			case TEXT:
			default:
				return new JTextField(15);
		}
	}

	private String getValueOf(JComponent control) {
		if (control instanceof JSpinner) {
			return String.valueOf(((JSpinner) control).getValue());
		} else if (control instanceof JComboBox) {
			Object selected = ((JComboBox<?>) control).getSelectedItem();
			return selected == null ? "" : selected.toString();
		} else if (control instanceof JCheckBox) {
			return String.valueOf(((JCheckBox) control).isSelected());
		} else if (control instanceof JTextField) {
			return ((JTextField) control).getText();
		}
		return "";
	}

	/*
	 * ----------------------------------------------------------------
	 * Accouchement : rattaché directement à la grossesse, sans nécessiter d'hospitalisation.
	 * L'hospitalisation reste possible depuis cet onglet pour les cas qui le nécessitent
	 * (complications, séjour prolongé), mais n'est plus un préalable pour enregistrer l'accouchement.
	 * ----------------------------------------------------------------
	 */
	private JPanel getDeliveryTab() {
		JPanel panel = new JPanel(new BorderLayout());
		deliveryPanel = new PregnancyDeliveryPanel();
		deliveryPanel.loadFor(pregnancy);
		panel.add(deliveryPanel, BorderLayout.CENTER);
		panel.add(getDeliveryButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel getDeliveryButtonPanel() {
		JPanel panel = new JPanel();

		JButton saveDeliveryButton = new JButton(MessageBundle.getMessage("angal.cpn.savedelivery.btn"));
		saveDeliveryButton.addActionListener(e -> {
			if (deliveryPanel.saveFor(pregnancy)) {
				MessageDialog.info(this, "angal.cpn.deliverysavedsuccessfully.msg");
			}
		});
		panel.add(saveDeliveryButton);

		JButton hospitalizeButton = new JButton(MessageBundle.getMessage("angal.cpn.hospitalizepatient.btn"));
		hospitalizeButton.addActionListener(e -> hospitalizePatient());
		panel.add(hospitalizeButton);

		return panel;
	}

	private void hospitalizePatient() {
		if (admissionManager.getCurrentAdmission(patient) != null) {
			MessageDialog.info(this, "angal.cpn.patientalreadyadmitted.msg");
			return;
		}
		// AdmissionBrowser is application-modal-blocked by this dialog, exactly like LabNew/TherapyEdit
		// above, and it also shows itself synchronously from its constructor.
		setVisible(false);
		AdmissionBrowser admissionBrowser = new AdmissionBrowser(ownerFrame, new AdmittedPatient(patient, null), false);
		admissionBrowser.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				setVisible(true);
				toFront();
			}
		});
	}

	/*
	 * ----------------------------------------------------------------
	 * Sauvegarde
	 * ----------------------------------------------------------------
	 */
	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();
		JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(e -> save());
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
        closeButton.addActionListener(e -> dispose());
		panel.add(saveButton);
		panel.add(closeButton);
		return panel;
	}

	private void save() {
		LocalDateTime visitDate = visitDateChooser.getLocalDateTime();
		if (visitDate == null) {
			MessageDialog.error(this, "angal.cpn.pleaseinsertavalidvisitdate.msg");
			return;
		}

		Map<PregnancyExamParameter, String> outcomes = new LinkedHashMap<>();
		for (PregnancyExamParameter parameter : currentParameters) {
			String value = getValueOf(parameterControls.get(parameter.getCode()));
			if (value != null && !value.isBlank()) {
				outcomes.put(parameter, value);
			}
		}

		try {
			// validate every CPN parameter value before persisting anything
			for (Map.Entry<PregnancyExamParameter, String> entry : outcomes.entrySet()) {
				examParameterManager.validateOutcome(entry.getKey(), entry.getValue());
			}

			pregnancy.setPatient(patient);
			pregnancy.setLmp(lmpChooser.getDate());
			pregnancy.setnPregnancies((Integer) nPregnanciesSpinner.getValue());
			pregnancy.setnTermDeliveries((Integer) nTermDeliveriesSpinner.getValue());
			pregnancy.setnPretermDeliveries((Integer) nPretermDeliveriesSpinner.getValue());
			pregnancy.setnAbortions((Integer) nAbortionsSpinner.getValue());
			pregnancy.setnLiveChildren((Integer) nLiveChildrenSpinner.getValue());
			pregnancy.setnStillbirths((Integer) nStillbirthsSpinner.getValue());
			pregnancy.setnDeceasedChildren((Integer) nDeceasedChildrenSpinner.getValue());
			pregnancy.setnTotalDesiredChildren((Integer) nTotalDesiredChildrenSpinner.getValue());
			pregnancy.setAgeOfLastChild((Integer) ageOfLastChildSpinner.getValue());
			pregnancy.setBreastfeeding(breastfeedingCheckBox.isSelected());

			Pregnancy savedPregnancy = insertPregnancy ? pregnancyManager.newPregnancy(pregnancy) : pregnancyManager.updatePregnancy(pregnancy);

			PregnancyVisit visit = new PregnancyVisit(savedPregnancy, patient, visitDate, getSelectedVisitType());
			visit.setNextVisitDate(nextVisitDateChooser.getDate() == null ? null : nextVisitDateChooser.getDate().atStartOfDay());
			visit.setNote(noteTextArea.getText());
			visit.setTreatmentTypes(treatmentTypeList.getSelectedValuesList());
			PregnancyVisit savedVisit = visitManager.newVisit(visit);

			for (Map.Entry<PregnancyExamParameter, String> entry : outcomes.entrySet()) {
				PregnancyExamResult result = new PregnancyExamResult(savedVisit, entry.getKey(), entry.getValue());
				examResultManager.saveResult(result);
			}

			MessageDialog.info(this, "angal.cpn.cpnvisitsavedsuccessfully.msg");
			dispose();
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
	}
}
