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
package org.isf.admission.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportPatientVersion2;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.ModalJFrame;

public class PatientFolderReportModal extends ModalJFrame {

	private JFrame parent;
	private Integer patId;
	private Patient patient;
	private PatientBrowserManager patientManager;
	private JPanel jPanelChooser;
	private GoodDateChooser jDateChooserDateFrom;
	private GoodDateChooser jDateChooserDateTo;
	private JPanel choosePanel;
	private JButton launchReportButton;
	private JButton closeButton;
	private JPanel admissionPanel;
	private JCheckBox admissionCheck;
	private JPanel opdPanel;
	private JCheckBox opdCheck;
	private JPanel drugsPanel;
	private JCheckBox drugsCheck;
	private JPanel examinationPanel;
	private JCheckBox examinationCheck;
	private JPanel laboratoryPanel;
	private JCheckBox laboratoryCheck;
	private JPanel operationsPanel;
	private JCheckBox operationCheck;
	private JPanel vaccinePanel;
	private JCheckBox vaccineCheck;
	private JPanel homeVisitPanel;
	private JCheckBox homeVisitCheck;
	private JPanel tbPanel;
	private JCheckBox tbCheck;
	private JPanel maternityPanel;
	private JCheckBox maternityCheck;
	private JPanel fpPanel;
	private JCheckBox fpCheck;
	private JPanel hivPanel;
	private JCheckBox hivCheck;
	private JPanel allPanel;
	private JCheckBox allCheck;
	private JPanel labelPanel;
	private LocalDate fromDate;
	private String selectedReport;

	public PatientFolderReportModal(JFrame parent, Integer code, LocalDate fromDate, LocalDate toDate, String selectedReport) {
		this.parent = parent;
		this.patId = code;
		if (Context.getApplicationContext() != null) {
			this.patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
		}
		if (this.patientManager != null) {
			try {
				this.patient = this.patientManager.getPatientById(code);
			} catch (Exception e) {
				this.patient = null;
			}
		}
		this.fromDate = fromDate;
		this.selectedReport = selectedReport.toUpperCase();
		switch(this.selectedReport) {
			case "ADMISSION":
			case "OPD":
			case "LABORATORY":
			case "OPERATION":
			case "DRUGS":
			case "EXAMINATION":
			case "VACCINES":
			case "HOMEVISIT":
			case "TUBERCULOSIS":
			case "MATERNITY":
			case "FAMILYPLANNING":
			case "HIV":
				break;  // valid values
			default:
				this.selectedReport = "ALL";
			}
		initialize();
	}

	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setTitle(MessageBundle.getMessage("angal.admission.report.title"));
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		showAsModal(parent);
	}
	
	private JPanel getJContentPane() {

		if (jPanelChooser == null) {
			jPanelChooser = new JPanel();
			
			GridBagLayout gblPanelChooser = new GridBagLayout();
			gblPanelChooser.columnWidths = new int[] { 0, 0, 0, 0 };
			gblPanelChooser.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			gblPanelChooser.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
			gblPanelChooser.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelChooser.setLayout(gblPanelChooser);

			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.datefrom.label"));
			GridBagConstraints gbcLabelDate = new GridBagConstraints();
			gbcLabelDate.anchor = GridBagConstraints.WEST;
			gbcLabelDate.insets = new Insets(10, 5, 5, 5);
			gbcLabelDate.gridx = 0;
			gbcLabelDate.gridy = 0;
			jPanelChooser.add(jLabelDate, gbcLabelDate);
			
			GridBagConstraints gbcDateChooserDate = new GridBagConstraints();
			gbcDateChooserDate.anchor = GridBagConstraints.WEST;
			gbcDateChooserDate.insets = new Insets(10, 5, 5, 5);
			gbcDateChooserDate.gridx = 1;
			gbcDateChooserDate.gridy = 0;
			jPanelChooser.add(getJDateChooserDateFrom(), gbcDateChooserDate);
			
			JLabel jLabelDateto = new JLabel(MessageBundle.getMessage("angal.common.dateto.label"));
			GridBagConstraints gbcLabelDateto = new GridBagConstraints();
			gbcLabelDateto.anchor = GridBagConstraints.WEST;
			gbcLabelDateto.insets = new Insets(10, 5, 5, 5);
			gbcLabelDateto.gridx = 0;
			gbcLabelDateto.gridy = 1;
			jPanelChooser.add(jLabelDateto, gbcLabelDateto);
			
			GridBagConstraints gbcDateChooserDateto = new GridBagConstraints();
			gbcDateChooserDateto.anchor = GridBagConstraints.WEST;
			gbcDateChooserDateto.insets = new Insets(10, 5, 5, 5);
			gbcDateChooserDateto.gridx = 1;
			gbcDateChooserDateto.gridy = 1;
			jPanelChooser.add(getJDateChooserDateTo(), gbcDateChooserDateto);
			
			GridBagConstraints gbcSliderHeight = new GridBagConstraints();
			gbcSliderHeight.insets = new Insets(5, 5, 5, 5);
			gbcSliderHeight.fill = GridBagConstraints.HORIZONTAL;
			gbcSliderHeight.gridwidth = 2;
			gbcSliderHeight.gridx = 0;
			gbcSliderHeight.gridy = 2;
			jPanelChooser.add(getValueReport(), gbcSliderHeight);

			GridBagConstraints gbcPrintButton = new GridBagConstraints();
			gbcPrintButton.insets = new Insets(5, 5, 5, 5);
			gbcPrintButton.fill = GridBagConstraints.HORIZONTAL;
			gbcPrintButton.gridx = 0;
			gbcPrintButton.gridy = 3;
			jPanelChooser.add(getPrintButton(), gbcPrintButton);

			GridBagConstraints gbcCloseButton = new GridBagConstraints();
			gbcCloseButton.insets = new Insets(5, 5, 5, 5);
			gbcCloseButton.fill = GridBagConstraints.HORIZONTAL;
			gbcCloseButton.gridx = 1;
			gbcCloseButton.gridy = 3;
			jPanelChooser.add(getCloseButton(), gbcCloseButton);
		}
		return jPanelChooser;
	}
	
	private JButton getPrintButton() {
		if (launchReportButton == null) {
			launchReportButton = new JButton(MessageBundle.getMessage("angal.common.launchreport.btn"));
			launchReportButton.setMnemonic(MessageBundle.getMnemonic("angal.common.launchreport.btn.key"));
			launchReportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new GenericReportPatientVersion2(patId, getParameterString(), getDateFromValue(), getDateToValue(), GeneralData.PATIENTSHEET);
				}
				
				protected String getParameterString() {
					StringBuilder parameterString = new StringBuilder();
					if (getAllValue()) {
						parameterString.append("All");
						return parameterString.toString();
					}
					if (getDrugsValue()) {
						parameterString.append("Drugs");
					}
					if (getExaminationValue()) {
						parameterString.append("Examination");
					}
					if (getAdmissionValue()) {
						parameterString.append("Admission");
					}
					if (getOpdValue()) {
						parameterString.append("Opd");
					}
					if (getLaboratoryValue()) {
						parameterString.append("Laboratory");
					}
					if (getOperationValue()) {
						parameterString.append("Operations");
					}
					if (getVaccineValue()) {
						parameterString.append("Vaccines");
					}
					if (getHomeVisitValue()) {
						parameterString.append("HomeVisit");
					}
					if (getTuberculosisValue()) {
						parameterString.append("Tuberculosis");
					}
					if (getMaternityValue()) {
						parameterString.append("Maternity");
					}
					if (getFamilyPlanningValue()) {
						parameterString.append("FamilyPlanning");
					}
					if (getHIVValue()) {
						parameterString.append("HIV");
					}
					
					return parameterString.toString();
				}
			});
		}
		return launchReportButton;
	}
	
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(actionEvent -> dispose());
		}
		return closeButton;
	}
	
	private JPanel getValueReport() {

		if (choosePanel == null) {

			choosePanel = new JPanel();
			choosePanel.setLayout(new BoxLayout(choosePanel, BoxLayout.Y_AXIS));
			
			choosePanel.add(getPanelLabel());
			choosePanel.add(getPanelAll());

			JPanel checkGrid = new JPanel(new GridLayout(0, 2, 5, 0));
			checkGrid.setAlignmentX(LEFT_ALIGNMENT);
			checkGrid.add(getPanelAdmission());
			checkGrid.add(getPanelOpd());
			checkGrid.add(getPanelLaboratory());
			checkGrid.add(getPanelOperations());
			checkGrid.add(getPanelDrugs());
			checkGrid.add(getPanelExamination());
			checkGrid.add(getPanelVaccines());
			if (GeneralData.HOMEVISITMODULEENABLED) {
				checkGrid.add(getPanelHomeVisit());
			}
			if (GeneralData.TUBERCULOSISMODULEENABLED) {
				checkGrid.add(getPanelTuberculosis());
			}
			if (GeneralData.MATERNITYMODULEENABLED && (patient == null || Character.toUpperCase(patient.getSex()) == 'F')) {
				checkGrid.add(getPanelMaternity());
			}
			if (GeneralData.FAMILYPLANNINGMODULEENABLED && (patient == null || Character.toUpperCase(patient.getSex()) == 'F')) {
				checkGrid.add(getPanelFamilyPlanning());
			}
			if (GeneralData.HIVMODULEENABLED) {
				checkGrid.add(getPanelHIV());
			}
			choosePanel.add(checkGrid);
		}
		return choosePanel;
	}
	
	private JPanel getPanelLabel() {
		if (labelPanel == null) {
			labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			labelPanel.setAlignmentY(LEFT_ALIGNMENT);
			labelPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.reportfor.txt") + ':'), BorderLayout.CENTER);
		}
		return labelPanel;
	}

	private JPanel getPanelAll() {
		if (allPanel == null) {
			allPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			allPanel.setAlignmentY(LEFT_ALIGNMENT);
			allCheck = new JCheckBox();
            if ("ALL".equals(selectedReport)) {
                allCheck.setSelected(true);
			}
			allPanel.add(allCheck);
			allPanel.add(new JLabel(MessageBundle.getMessage("angal.common.all.txt").toUpperCase()), BorderLayout.CENTER);
			allCheck.addActionListener(actionEvent -> {
				examinationCheck.setSelected(false);
				admissionCheck.setSelected(false);
				drugsCheck.setSelected(false);
				opdCheck.setSelected(false);
				operationCheck.setSelected(false);
				laboratoryCheck.setSelected(false);
				if (vaccineCheck != null) {
					vaccineCheck.setSelected(false);
				}
				if (homeVisitCheck != null) {
					homeVisitCheck.setSelected(false);
				}
				if (tbCheck != null) {
					tbCheck.setSelected(false);
				}
				if (maternityCheck != null) {
					maternityCheck.setSelected(false);
				}
				if (fpCheck != null) {
					fpCheck.setSelected(false);
				}
				if (hivCheck != null) {
					hivCheck.setSelected(false);
				}
			});
		}
		return allPanel;
	}

	private JPanel getPanelExamination() {
		if (examinationPanel == null) {
			examinationPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			examinationCheck = new JCheckBox();
			if ("EXAMINATION".equals(selectedReport)) {
				examinationCheck.setSelected(true);
			}
			examinationPanel.add(examinationCheck);
			examinationPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.examination.txt")), BorderLayout.CENTER);
			examinationCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
			
		}
		return examinationPanel;
	}
	
	private JPanel getPanelOperations() {
		if (operationsPanel == null) {
			operationsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			operationCheck = new JCheckBox();
			if ("OPERATION".equals(selectedReport)) {
				operationCheck.setSelected(true);
			}
			operationsPanel.add(operationCheck);
			operationsPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.operation.txt")), BorderLayout.CENTER);
			operationCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return operationsPanel;
	}
	
	private JPanel getPanelLaboratory() {
		if (laboratoryPanel == null) {
			laboratoryPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			laboratoryCheck = new JCheckBox();
			if ("LABORATORY".equals(selectedReport)) {
				laboratoryCheck.setSelected(true);
			}
			laboratoryPanel.add(laboratoryCheck);
			laboratoryPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.laboratory.txt")), BorderLayout.CENTER);
			laboratoryCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return laboratoryPanel;
	}
	
	private JPanel getPanelDrugs() {
		if (drugsPanel == null) {
			drugsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			drugsCheck = new JCheckBox();
			if ("DRUGS".equals(selectedReport)) {
				drugsCheck.setSelected(true);
			}
			drugsPanel.add(drugsCheck);
			drugsPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.drugs.txt")), BorderLayout.CENTER);
			drugsCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return drugsPanel;
	}
	
	private JPanel getPanelOpd() {
		if (opdPanel == null) {
			opdPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			opdCheck = new JCheckBox();
			if ("OPD".equals(selectedReport)) {
				opdCheck.setSelected(true);
			}
			opdPanel.add(opdCheck);
			opdPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.opd.txt")), BorderLayout.CENTER);
			opdCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return opdPanel;
	}

	private JPanel getPanelAdmission() {
		if (admissionPanel == null) {
			admissionPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			admissionCheck = new JCheckBox();
			if ("ADMISSION".equals(selectedReport)) {
				admissionCheck.setSelected(true);
			}
			admissionPanel.add(admissionCheck);
			admissionPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.admission.txt")), BorderLayout.CENTER);
			admissionCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return admissionPanel;
	}

	private JPanel getPanelVaccines() {
		if (vaccinePanel == null) {
			vaccinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			vaccineCheck = new JCheckBox();
			if ("VACCINES".equals(selectedReport)) {
				vaccineCheck.setSelected(true);
			}
			vaccinePanel.add(vaccineCheck);
			vaccinePanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.vaccines.txt")), BorderLayout.CENTER);
			vaccineCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return vaccinePanel;
	}

	private JPanel getPanelHomeVisit() {
		if (homeVisitPanel == null) {
			homeVisitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			homeVisitCheck = new JCheckBox();
			if ("HOMEVISIT".equals(selectedReport)) {
				homeVisitCheck.setSelected(true);
			}
			homeVisitPanel.add(homeVisitCheck);
			homeVisitPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.homevisit.txt")), BorderLayout.CENTER);
			homeVisitCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return homeVisitPanel;
	}

	private JPanel getPanelTuberculosis() {
		if (tbPanel == null) {
			tbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			tbCheck = new JCheckBox();
			if ("TUBERCULOSIS".equals(selectedReport)) {
				tbCheck.setSelected(true);
			}
			tbPanel.add(tbCheck);
			tbPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.tuberculosis.txt")), BorderLayout.CENTER);
			tbCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return tbPanel;
	}

	private JPanel getPanelMaternity() {
		if (maternityPanel == null) {
			maternityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			maternityCheck = new JCheckBox();
			if ("MATERNITY".equals(selectedReport)) {
				maternityCheck.setSelected(true);
			}
			maternityPanel.add(maternityCheck);
			maternityPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.maternity.txt")), BorderLayout.CENTER);
			maternityCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return maternityPanel;
	}

	private JPanel getPanelFamilyPlanning() {
		if (fpPanel == null) {
			fpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			fpCheck = new JCheckBox();
			if ("FAMILYPLANNING".equals(selectedReport)) {
				fpCheck.setSelected(true);
			}
			fpPanel.add(fpCheck);
			fpPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.familyplanning.txt")), BorderLayout.CENTER);
			fpCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return fpPanel;
	}

	private JPanel getPanelHIV() {
		if (hivPanel == null) {
			hivPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			hivCheck = new JCheckBox();
			if ("HIV".equals(selectedReport)) {
				hivCheck.setSelected(true);
			}
			hivPanel.add(hivCheck);
			hivPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.hiv.txt")), BorderLayout.CENTER);
			hivCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return hivPanel;
	}
	
	public boolean getOperationValue() {
		return operationCheck != null && operationCheck.isSelected();
	}
	public boolean getLaboratoryValue() {
		return laboratoryCheck != null && laboratoryCheck.isSelected();
	}
	public boolean getAdmissionValue() {
		return admissionCheck != null && admissionCheck.isSelected();
	}
	public boolean getOpdValue() {
		return opdCheck != null && opdCheck.isSelected();
	}
	public boolean getAllValue() {
		return allCheck != null && allCheck.isSelected();
	}
	public boolean getDrugsValue() {
		return drugsCheck != null && drugsCheck.isSelected();
	}
	public boolean getExaminationValue() {
		return examinationCheck != null && examinationCheck.isSelected();
	}
	public boolean getVaccineValue() {
		return vaccineCheck != null && vaccineCheck.isSelected();
	}
	public boolean getHomeVisitValue() {
		return homeVisitCheck != null && homeVisitCheck.isSelected();
	}
	public boolean getTuberculosisValue() {
		return tbCheck != null && tbCheck.isSelected();
	}
	public boolean getMaternityValue() {
		return maternityCheck != null && maternityCheck.isSelected();
	}
	public boolean getFamilyPlanningValue() {
		return fpCheck != null && fpCheck.isSelected();
	}
	public boolean getHIVValue() {
		return hivCheck != null && hivCheck.isSelected();
	}

	private GoodDateChooser getJDateChooserDateFrom() {
		if (jDateChooserDateFrom == null) {
			jDateChooserDateFrom = new GoodDateChooser(fromDate);
		}
		return jDateChooserDateFrom;
	}

	private GoodDateChooser getJDateChooserDateTo() {
		if (jDateChooserDateTo == null) {
			jDateChooserDateTo = new GoodDateChooser(LocalDate.now());
		}
		return jDateChooserDateTo;
	}

	public LocalDateTime getDateToValue() {
		LocalDate date = jDateChooserDateTo.getDate();
		if (date == null) {
			jDateChooserDateTo.setDate(LocalDate.now());
		}
		return jDateChooserDateTo.getDateEndOfDay();
	}

	public LocalDateTime getDateFromValue() {
		LocalDate date = jDateChooserDateFrom.getDate();
		if (date == null) {
			jDateChooserDateFrom.setDate(LocalDate.now());
		}
		return jDateChooserDateFrom.getDateStartOfDay();
	}
}
