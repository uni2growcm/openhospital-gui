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
package org.isf.mortuary.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.BodyCompartmentManager;
import org.isf.mortuary.manager.DeathReasonManager;
import org.isf.mortuary.manager.DeathManager;
import org.isf.mortuary.model.BodyCompartment;
import org.isf.mortuary.model.Death;
import org.isf.mortuary.model.DeathReason;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class DeathEdit extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
	private final DeathManager deathManager = Context.getApplicationContext().getBean(DeathManager.class);
	private final DeathReasonManager deathReasonManager = Context.getApplicationContext().getBean(DeathReasonManager.class);
	private final WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private final BodyCompartmentManager bodyCompartmentManager = Context.getApplicationContext().getBean(BodyCompartmentManager.class);
	private final EventListenerList deathListeners = new EventListenerList();
	private JPanel buttonPanel;
	private JPanel jContentPanel;
	private JPanel dataPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JButton pickPatientButton;
	private JComboBox<Object> deathReasonCombo;
	private JComboBox<Object> provenanceCombo;
	private JCheckBox bodyLiftCheckBox;
	private JTextField deathPlace;
	private JTextField declaringName;
	private JTextField declaringNid;
	private JTextField declaringPhoneNumber;
	private JTextField familyName;
	private JTextField familyNid;
	private JTextField familyPhoneNumber;
	private JComboBox<Object> bodyCompartmentCombo;
	private JTextField patientTextField;
	private GoodDateChooser deathDate;
	private GoodDateChooser admissionDate;
	private GoodDateChooser estimatedDischargeDate;
	private Patient patientParent;
	private final boolean insert;
	private final Death death;

	public DeathEdit(JFrame parent, Death old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		death = old;
		initialize();
	}

	public void addDeathListener(DeathListener l) {
		deathListeners.add(DeathListener.class, l);
	}

	private void fireDeathInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deathListeners.getListeners(DeathListener.class);
		for (EventListener listener : listeners) {
			((DeathListener) listener).deathInserted(event);
		}
	}

	private void fireDeathUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deathListeners.getListeners(DeathListener.class);
		for (EventListener listener : listeners) {
			((DeathListener) listener).deathUpdated(event);
		}
	}

	private void initialize() {
		this.setContentPane(getJContentPanel());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.newmortuary.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.editmortuary.title"));
		}
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContentPanel() {
		if (jContentPanel != null) {
			return jContentPanel;
		}

		jContentPanel = new JPanel();
		jContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		jContentPanel.setLayout(new BorderLayout());
		jContentPanel.add(getDataPanel(), BorderLayout.CENTER);
		jContentPanel.add(getButtonPanel(), BorderLayout.SOUTH);

		return jContentPanel;
	}

	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel(new BorderLayout());
			dataPanel.add(getLeftPanel(), BorderLayout.WEST);
			dataPanel.add(getRightPanel(), BorderLayout.EAST);
		}
		return dataPanel;
	}

	private JPanel getLeftPanel() {
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(getDeathNewsPanel(), BorderLayout.NORTH);
		leftPanel.add(getLiftPanel(), BorderLayout.SOUTH);
		return leftPanel;
	}

	private JPanel getDeathNewsPanel() {
		JPanel deathNewsPanel = new JPanel();
		deathNewsPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.deathinformation.border")));
		GridBagLayout gblDeathNewsPanel = new GridBagLayout();
		gblDeathNewsPanel.columnWeights = new double[] { 0.0, 1.0 };
		deathNewsPanel.setLayout(gblDeathNewsPanel);

		JLabel patientLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.searchpatient.label"));
		GridBagConstraints gbcPatientLabel = new GridBagConstraints();
		gbcPatientLabel.anchor = GridBagConstraints.WEST;
		gbcPatientLabel.insets = new Insets(0, 0, 5, 5);
		gbcPatientLabel.gridx = 0;
		gbcPatientLabel.gridy = 0;
		deathNewsPanel.add(patientLabel, gbcPatientLabel);
		GridBagConstraints gbcPatientPanel = new GridBagConstraints();
		gbcPatientPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcPatientPanel.insets = new Insets(0, 0, 5, 0);
		gbcPatientPanel.gridx = 1;
		gbcPatientPanel.gridy = 0;
		deathNewsPanel.add(getPatientPanel(), gbcPatientPanel);

		JLabel provenanceLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.provenance.label"));
		GridBagConstraints gbcProvenanceLabel = new GridBagConstraints();
		gbcProvenanceLabel.anchor = GridBagConstraints.WEST;
		gbcProvenanceLabel.insets = new Insets(0, 0, 5, 5);
		gbcProvenanceLabel.gridx = 0;
		gbcProvenanceLabel.gridy = 1;
		deathNewsPanel.add(provenanceLabel, gbcProvenanceLabel);
		GridBagConstraints gbcProvenanceCombo = new GridBagConstraints();
		gbcProvenanceCombo.fill = GridBagConstraints.HORIZONTAL;
		gbcProvenanceCombo.insets = new Insets(0, 0, 5, 0);
		gbcProvenanceCombo.gridx = 1;
		gbcProvenanceCombo.gridy = 1;
		deathNewsPanel.add(getProvenanceCombo(), gbcProvenanceCombo);

		JLabel deathDateLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.deathdate.label"));
		GridBagConstraints gbcDeathDateLabel = new GridBagConstraints();
		gbcDeathDateLabel.anchor = GridBagConstraints.WEST;
		gbcDeathDateLabel.insets = new Insets(0, 0, 5, 5);
		gbcDeathDateLabel.gridx = 0;
		gbcDeathDateLabel.gridy = 2;
		deathNewsPanel.add(deathDateLabel, gbcDeathDateLabel);
		GridBagConstraints gbcDeathDate = new GridBagConstraints();
		gbcDeathDate.fill = GridBagConstraints.HORIZONTAL;
		gbcDeathDate.insets = new Insets(0, 0, 5, 0);
		gbcDeathDate.gridx = 1;
		gbcDeathDate.gridy = 2;
		deathNewsPanel.add(getDeathDate(), gbcDeathDate);

		JLabel deathPlaceLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.deathplace.label"));
		GridBagConstraints gbcDeathPlaceLabel = new GridBagConstraints();
		gbcDeathPlaceLabel.anchor = GridBagConstraints.WEST;
		gbcDeathPlaceLabel.insets = new Insets(0, 0, 5, 5);
		gbcDeathPlaceLabel.gridx = 0;
		gbcDeathPlaceLabel.gridy = 3;
		deathNewsPanel.add(deathPlaceLabel, gbcDeathPlaceLabel);
		GridBagConstraints gbcDeathPlaceTextField = new GridBagConstraints();
		gbcDeathPlaceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcDeathPlaceTextField.insets = new Insets(0, 0, 5, 0);
		gbcDeathPlaceTextField.gridx = 1;
		gbcDeathPlaceTextField.gridy = 3;
		deathNewsPanel.add(getDeathPlaceTextField(), gbcDeathPlaceTextField);

		JLabel admissionDateLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.admissiondate.label"));
		GridBagConstraints gbcAdmissionDateLabel = new GridBagConstraints();
		gbcAdmissionDateLabel.anchor = GridBagConstraints.WEST;
		gbcAdmissionDateLabel.insets = new Insets(0, 0, 5, 5);
		gbcAdmissionDateLabel.gridx = 0;
		gbcAdmissionDateLabel.gridy = 4;
		deathNewsPanel.add(admissionDateLabel, gbcAdmissionDateLabel);
		GridBagConstraints gbcAdmissionDate = new GridBagConstraints();
		gbcAdmissionDate.fill = GridBagConstraints.HORIZONTAL;
		gbcAdmissionDate.insets = new Insets(0, 0, 5, 0);
		gbcAdmissionDate.gridx = 1;
		gbcAdmissionDate.gridy = 4;
		deathNewsPanel.add(getAdmissionDate(), gbcAdmissionDate);

		JLabel dischargeDateLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.dischargedate.label"));
		GridBagConstraints gbcDischargeDateLabel = new GridBagConstraints();
		gbcDischargeDateLabel.anchor = GridBagConstraints.WEST;
		gbcDischargeDateLabel.insets = new Insets(0, 0, 5, 5);
		gbcDischargeDateLabel.gridx = 0;
		gbcDischargeDateLabel.gridy = 5;
		deathNewsPanel.add(dischargeDateLabel, gbcDischargeDateLabel);
		GridBagConstraints gbcEstimatedDischargeDate = new GridBagConstraints();
		gbcEstimatedDischargeDate.fill = GridBagConstraints.HORIZONTAL;
		gbcEstimatedDischargeDate.insets = new Insets(0, 0, 5, 0);
		gbcEstimatedDischargeDate.gridx = 1;
		gbcEstimatedDischargeDate.gridy = 5;
		deathNewsPanel.add(getEstimatedDischargeDate(), gbcEstimatedDischargeDate);

		JLabel deathReasonLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.causeofdeath.label"));
		GridBagConstraints gbcDeathReasonLabel = new GridBagConstraints();
		gbcDeathReasonLabel.anchor = GridBagConstraints.WEST;
		gbcDeathReasonLabel.insets = new Insets(0, 0, 5, 5);
		gbcDeathReasonLabel.gridx = 0;
		gbcDeathReasonLabel.gridy = 6;
		deathNewsPanel.add(deathReasonLabel, gbcDeathReasonLabel);
		GridBagConstraints gbcDeathReasonCombo = new GridBagConstraints();
		gbcDeathReasonCombo.fill = GridBagConstraints.HORIZONTAL;
		gbcDeathReasonCombo.insets = new Insets(0, 0, 5, 0);
		gbcDeathReasonCombo.gridx = 1;
		gbcDeathReasonCombo.gridy = 6;
		deathNewsPanel.add(getDeathReasonCombo(), gbcDeathReasonCombo);

		return deathNewsPanel;
	}

	private JPanel getPatientPanel() {
		JPanel patientPanel = new JPanel();
		patientTextField = new JTextField();
		patientTextField.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			patientTextField.setText(death.getPatient().getName());
			patientTextField.setEnabled(false);
		}
		patientPanel.add(patientTextField);
		patientPanel.add(getPickPatientButton());
		return patientPanel;
	}

	private JButton getPickPatientButton() {
		if (pickPatientButton != null) {
			return pickPatientButton;
		}

		pickPatientButton = new JButton();
		pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
		pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.billbrowser.selectapatient.tooltip"));
		if (!insert) {
			pickPatientButton.setEnabled(false);
		}
		pickPatientButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				SelectPatient selectPatient = new SelectPatient(DeathEdit.this, false, true);
				selectPatient.addSelectionListener(DeathEdit.this);
				selectPatient.setVisible(true);
				Patient pat = selectPatient.getPatient();
				patientSelected(pat);
			}
		});

		return pickPatientButton;
	}

	public void patientSelected(Patient patient) {
		patientParent = patient;
		patientTextField.setText(patientParent != null ? patientParent.getName() : "");
	}

	private JComboBox<Object> getProvenanceCombo() {
		if (provenanceCombo == null) {
			provenanceCombo = new JComboBox<Object>();
			provenanceCombo.setPreferredSize(new Dimension(270, 27));
		}
		List<Ward> wards = new ArrayList<>();
		try {
			wards = wardBrowserManager.getWards();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		provenanceCombo.addItem("");
		for (Ward ward : wards) {
			provenanceCombo.addItem(ward);
		}
		if (!insert) {
			provenanceCombo.setSelectedItem(death.getWard());
		}
		return provenanceCombo;
	}

	private GoodDateChooser getDeathDate() {
		if (deathDate == null) {
			deathDate = new GoodDateChooser();
			if (!insert) {
				deathDate.setDate(death.getDate().toLocalDate());
			}
		}
		return deathDate;
	}

	private JTextField getDeathPlaceTextField() {
		if (deathPlace != null) {
			return deathPlace;
		}
		deathPlace = new JTextField();
		deathPlace.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			deathPlace.setText(death.getPlace());
		}
		return deathPlace;
	}

	private GoodDateChooser getAdmissionDate() {
		if (admissionDate == null) {
			admissionDate = new GoodDateChooser();
			if (!insert) {
				admissionDate.setDate(death.getAdmissionDate().toLocalDate());
			}
		}
		return admissionDate;
	}

	private GoodDateChooser getEstimatedDischargeDate() {
		if (estimatedDischargeDate == null) {
			estimatedDischargeDate = new GoodDateChooser();
			if (!insert) {
				estimatedDischargeDate.setDate(death.getEstimatedDischargeDate().toLocalDate());
			}
		}
		return estimatedDischargeDate;
	}

	private JComboBox<Object> getDeathReasonCombo() {
		if (deathReasonCombo == null) {
			deathReasonCombo = new JComboBox<Object>();
			deathReasonCombo.setPreferredSize(new Dimension(270, 27));
		}

		List<DeathReason> deathReasons = new ArrayList<>();
		try {
			deathReasons = deathReasonManager.getAll();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		deathReasonCombo.addItem("");
		for (DeathReason deathReason : deathReasons) {
			deathReasonCombo.addItem(deathReason);
			if (!insert) {
				if (Objects.equals(deathReason.getTitle(), death.getDeathReason().getTitle())) {
					deathReasonCombo.setSelectedItem(deathReason);
				}
			}
		}

		return deathReasonCombo;
	}

	private JPanel getLiftPanel() {
		JPanel liftPanel = new JPanel();
		liftPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.bodylift.border")));
		JLabel bodyLiftLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.bodyliftperformed.label"));
		bodyLiftCheckBox = new JCheckBox();

		if (!insert && death.getDischargeDate() != null) {
			bodyLiftCheckBox.setSelected(true);
		}

		liftPanel.add(bodyLiftCheckBox);
		liftPanel.add(bodyLiftLabel);
		pack();
		return liftPanel;
	}

	private JPanel getRightPanel() {
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(getDeclaringPanel(), BorderLayout.NORTH);
		rightPanel.add(getLockerPanel(), BorderLayout.SOUTH);
		return rightPanel;
	}

	private JPanel getDeclaringPanel() {
		JPanel declaringPanel = new JPanel(new BorderLayout());
		declaringPanel.add(getDeclaringSubPanel(), BorderLayout.NORTH);
		declaringPanel.add(getFamilyPanel(), BorderLayout.SOUTH);
		return declaringPanel;
	}

	private JPanel getDeclaringSubPanel() {
		JPanel declaringSubPanel = new JPanel();
		declaringSubPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.declaringinformation.border")));
		GridBagLayout gblDeathNewsPanel = new GridBagLayout();
		gblDeathNewsPanel.columnWeights = new double[] { 0.0, 1.0 };
		declaringSubPanel.setLayout(gblDeathNewsPanel);

		JLabel declaringNameLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.declaringfullname.label"));
		GridBagConstraints gbcDeclaringNameLabel = new GridBagConstraints();
		gbcDeclaringNameLabel.anchor = GridBagConstraints.WEST;
		gbcDeclaringNameLabel.insets = new Insets(0, 0, 5, 5);
		gbcDeclaringNameLabel.gridx = 0;
		gbcDeclaringNameLabel.gridy = 0;
		declaringSubPanel.add(declaringNameLabel, gbcDeclaringNameLabel);
		GridBagConstraints gbcDeclaringNameTextField = new GridBagConstraints();
		gbcDeclaringNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcDeclaringNameTextField.insets = new Insets(0, 0, 5, 0);
		gbcDeclaringNameTextField.gridx = 1;
		gbcDeclaringNameTextField.gridy = 0;
		declaringSubPanel.add(getDeclaringNameTextField(), gbcDeclaringNameTextField);

		JLabel declaringPhoneNumberLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.declaringphonenumber.label"));
		GridBagConstraints gbcDeclaringPhoneNumberLabel = new GridBagConstraints();
		gbcDeclaringPhoneNumberLabel.anchor = GridBagConstraints.WEST;
		gbcDeclaringPhoneNumberLabel.insets = new Insets(0, 0, 5, 5);
		gbcDeclaringPhoneNumberLabel.gridx = 0;
		gbcDeclaringPhoneNumberLabel.gridy = 1;
		declaringSubPanel.add(declaringPhoneNumberLabel, gbcDeclaringPhoneNumberLabel);
		GridBagConstraints gbcDeclaringPhoneNumberTextField = new GridBagConstraints();
		gbcDeclaringPhoneNumberTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcDeclaringPhoneNumberTextField.insets = new Insets(0, 0, 5, 0);
		gbcDeclaringPhoneNumberTextField.gridx = 1;
		gbcDeclaringPhoneNumberTextField.gridy = 1;
		declaringSubPanel.add(getDeclaringPhoneNumberTextField(), gbcDeclaringPhoneNumberTextField);

		JLabel declaringNidLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.declaringnid.label"));
		GridBagConstraints gbcDeclaringNidLabel = new GridBagConstraints();
		gbcDeclaringNidLabel.anchor = GridBagConstraints.WEST;
		gbcDeclaringNidLabel.insets = new Insets(0, 0, 5, 5);
		gbcDeclaringNidLabel.gridx = 0;
		gbcDeclaringNidLabel.gridy = 2;
		declaringSubPanel.add(declaringNidLabel, gbcDeclaringNidLabel);
		GridBagConstraints gbcDeclaringNidTextField = new GridBagConstraints();
		gbcDeclaringNidTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcDeclaringNidTextField.insets = new Insets(0, 0, 5, 0);
		gbcDeclaringNidTextField.gridx = 1;
		gbcDeclaringNidTextField.gridy = 2;
		declaringSubPanel.add(getDeclaringNidTextField(), gbcDeclaringNidTextField);

		return declaringSubPanel;
	}

	private JTextField getDeclaringNameTextField() {
		if (declaringName != null) {
			return declaringName;
		}

		declaringName = new JTextField();
		declaringName.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			declaringName.setText(death.getDeclaringName());
		}

		return declaringName;
	}

	private JTextField getDeclaringPhoneNumberTextField() {
		if (declaringPhoneNumber != null) {
			return declaringPhoneNumber;
		}

		declaringPhoneNumber = new JTextField();
		declaringPhoneNumber.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			declaringPhoneNumber.setText(death.getDeclaringPhone());
		}

		return declaringPhoneNumber;
	}

	private JTextField getDeclaringNidTextField() {
		if (declaringNid != null) {
			return declaringNid;
		}

		declaringNid = new JTextField();
		declaringNid.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			declaringNid.setText(death.getDeclaringNid());
		}

		return declaringNid;
	}

	private JPanel getFamilyPanel() {
		JPanel FamilyPanel = new JPanel();
		FamilyPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.familyinformation.border")));
		GridBagLayout gblDeathNewsPanel = new GridBagLayout();
		gblDeathNewsPanel.columnWeights = new double[] { 0.0, 1.0 };
		FamilyPanel.setLayout(gblDeathNewsPanel);

		JLabel familyNameLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.familyfullname.label"));
		GridBagConstraints gbcFamilyNameLabel = new GridBagConstraints();
		gbcFamilyNameLabel.anchor = GridBagConstraints.WEST;
		gbcFamilyNameLabel.insets = new Insets(0, 0, 5, 5);
		gbcFamilyNameLabel.gridx = 0;
		gbcFamilyNameLabel.gridy = 0;
		FamilyPanel.add(familyNameLabel, gbcFamilyNameLabel);
		GridBagConstraints gbcFamilyNameTextField = new GridBagConstraints();
		gbcFamilyNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcFamilyNameTextField.insets = new Insets(0, 0, 5, 0);
		gbcFamilyNameTextField.gridx = 1;
		gbcFamilyNameTextField.gridy = 0;
		FamilyPanel.add(getFamilyNameTextField(), gbcFamilyNameTextField);

		JLabel familyPhoneNumberLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.familyphonenumber.label"));
		GridBagConstraints gbcFamilyPhoneNumberLabel = new GridBagConstraints();
		gbcFamilyPhoneNumberLabel.anchor = GridBagConstraints.WEST;
		gbcFamilyPhoneNumberLabel.insets = new Insets(0, 0, 5, 5);
		gbcFamilyPhoneNumberLabel.gridx = 0;
		gbcFamilyPhoneNumberLabel.gridy = 1;
		FamilyPanel.add(familyPhoneNumberLabel, gbcFamilyPhoneNumberLabel);
		GridBagConstraints gbcFamilyPhoneNumberTextField = new GridBagConstraints();
		gbcFamilyPhoneNumberTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcFamilyPhoneNumberTextField.insets = new Insets(0, 0, 5, 0);
		gbcFamilyPhoneNumberTextField.gridx = 1;
		gbcFamilyPhoneNumberTextField.gridy = 1;
		FamilyPanel.add(getFamilyPhoneNumberTextField(), gbcFamilyPhoneNumberTextField);

		JLabel familyNidLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.familynid.label"));
		GridBagConstraints gbcFamilyNidLabel = new GridBagConstraints();
		gbcFamilyNidLabel.anchor = GridBagConstraints.WEST;
		gbcFamilyNidLabel.insets = new Insets(0, 0, 5, 5);
		gbcFamilyNidLabel.gridx = 0;
		gbcFamilyNidLabel.gridy = 2;
		FamilyPanel.add(familyNidLabel, gbcFamilyNidLabel);
		GridBagConstraints gbcFamilyNidTextField = new GridBagConstraints();
		gbcFamilyNidTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcFamilyNidTextField.insets = new Insets(0, 0, 5, 0);
		gbcFamilyNidTextField.gridx = 1;
		gbcFamilyNidTextField.gridy = 2;
		FamilyPanel.add(getFamilyNidTextField(), gbcFamilyNidTextField);

		return FamilyPanel;
	}

	private JTextField getFamilyNameTextField() {
		if (familyName != null) {
			return familyName;
		}

		familyName = new JTextField();
		familyName.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			familyName.setText(death.getFamilyName());
		}

		return familyName;
	}

	private JTextField getFamilyPhoneNumberTextField() {
		if (familyPhoneNumber != null) {
			return familyPhoneNumber;
		}

		familyPhoneNumber = new JTextField();
		familyPhoneNumber.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			familyPhoneNumber.setText(death.getFamilyPhone());
		}

		return familyPhoneNumber;
	}

	private JTextField getFamilyNidTextField() {
		if (familyNid != null) {
			return familyNid;
		}

		familyNid = new JTextField();
		familyNid.setPreferredSize(new Dimension(270,27));
		if (!insert) {
			familyNid.setText(death.getFamilyNid());
		}

		return familyNid;
	}

	private JPanel getLockerPanel() {
		JPanel lockerPanel = new JPanel();
		lockerPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.locker.border")));
		JLabel lockerNumberLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.lockerNumber.label"));
		lockerPanel.add(lockerNumberLabel);
		lockerPanel.add(getBodyCompartmentCombo());
		return lockerPanel;
	}

	private JComboBox<Object> getBodyCompartmentCombo() {
		if (bodyCompartmentCombo == null) {
			bodyCompartmentCombo = new JComboBox<Object>();
			bodyCompartmentCombo.setPreferredSize(new Dimension(270, 27));
		}

		List<BodyCompartment> bodyCompartments = new ArrayList<>();
		try {
			bodyCompartments = bodyCompartmentManager.getBodyCompartments();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		bodyCompartmentCombo.addItem("");
		for (BodyCompartment bodyCompartment : bodyCompartments) {
			bodyCompartmentCombo.addItem(bodyCompartment);
			if (!insert) {
				if (bodyCompartment.getLabel().equals(death.getBodyCompartment().getLabel())) {
					bodyCompartmentCombo.setSelectedItem(bodyCompartment);
				}
			}
		}

		return bodyCompartmentCombo;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton());
			buttonPanel.add(getCancelButton());
		}
		return buttonPanel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	private JButton getOkButton() {
		if (okButton != null) {
			return okButton;
		}

		okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		okButton.addActionListener(actionEvent -> {
			String patientName = patientTextField.getText().trim();
			Patient patient = patientParent;
			Ward provenanceWard = null;
			LocalDateTime date = deathDate.getDateStartOfDay();
			String place = deathPlace.getText().trim();
			LocalDateTime dateAdmission = admissionDate.getDateStartOfDay();
			LocalDateTime dateEstimatedToDischarge = estimatedDischargeDate.getDateStartOfDay();
			DeathReason deathReason = null;
			String declaringFullName = declaringName.getText().trim();
			String declaringPhone = declaringPhoneNumber.getText().trim();
			String declaringNum = declaringNid.getText().trim();
			String familyFullName = familyName.getText().trim();
			String familyPhone = familyPhoneNumber.getText().trim();
			String familyNum = familyNid.getText();
			BodyCompartment bodyCompartment = null;

			if (!(provenanceCombo.getSelectedItem() instanceof String)) {
				provenanceWard = (Ward) provenanceCombo.getSelectedItem();
			}
			if (!(deathReasonCombo.getSelectedItem() instanceof String)) {
				deathReason = (DeathReason) deathReasonCombo.getSelectedItem();
			}
			if (!(bodyCompartmentCombo.getSelectedItem() instanceof String)) {
				bodyCompartment = (BodyCompartment) bodyCompartmentCombo.getSelectedItem();
			}
			if (insert) {
				if (patient == null || patientName.isEmpty()) {
					MessageDialog.error(this, "angal.mortuary.pleaseselectpatient.msg");
					return;
				}
			}
			if (provenanceWard == null) {
				MessageDialog.error(this, "angal.mortuary.pleaseselectaprovenanceward.msg");
				return;
			}
			if (date == null) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertadeathdate.msg");
				return;
			}
			if (place.isEmpty()) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertadeathplace.msg");
				return;
			}
			if (dateAdmission == null) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertanadmissiondate.msg");
				return;
			}
			if (dateEstimatedToDischarge == null) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertadischargedate.msg");
				return;
			}
			if (deathReason == null) {
				MessageDialog.error(this, "angal.mortuary.pleaseselectadeathreason.msg");
				return;
			}
			if (declaringFullName.isEmpty()) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertadeclaringfullname.msg");
				return;
			}
			if (declaringPhone.isEmpty()) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertadeclaringphonenumber.msg");
				return;
			}
			if (declaringNum.isEmpty()) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertadeclaringnid.msg");
				return;
			}
			if (bodyCompartment == null) {
				MessageDialog.error(this, "angal.mortuary.pleaseinsertalockernumber.msg");
				return;
			}

			if (insert) {
				death.setPatient(patient);
			}

			death.setDate(date);
			death.setAdmissionDate(dateAdmission);

			if (bodyLiftCheckBox.isSelected()) {
				death.setDischargeDate(LocalDateTime.now());
			} else {
				death.setDischargeDate(null);
			}

			death.setEstimatedDischargeDate(dateEstimatedToDischarge);
			death.setDeathReason(deathReason);
			death.setWard(provenanceWard);
			death.setPlace(place);
			death.setDeclaringName(declaringFullName);
			death.setDeclaringPhone(declaringPhone);
			death.setDeclaringNid(declaringNum);
			death.setFamilyName(familyFullName);
			death.setFamilyNid(familyNum);
			death.setFamilyPhone(familyPhone);
			death.setBodyCompartment(bodyCompartment);

			boolean result = false;
			Death savedDeath;
			if (insert) {
				try {
					savedDeath = deathManager.add(death);
					result = (savedDeath != null);
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (result) {
					fireDeathInserted();
				}
			} else {
				try {
					savedDeath = deathManager.update(death);
					result = (savedDeath != null);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (result) {
					fireDeathUpdated();
				}
			}

			if (!result) {
				MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
			} else {
				dispose();
			}
		});

		return okButton;
	}

	public interface DeathListener extends EventListener {

		void deathUpdated(AWTEvent e);

		void deathInserted(AWTEvent e);
	}
}