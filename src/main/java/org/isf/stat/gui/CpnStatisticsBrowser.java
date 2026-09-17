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
package org.isf.stat.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.isf.disctype.manager.DischargeTypeBrowserManager;
import org.isf.disctype.model.DischargeType;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.pregnancy.manager.PregnancyExamParameterBrowserManager;
import org.isf.pregnancy.model.PregnancyExamParameter;
import org.isf.pregnancy.model.PregnancyVisit;
import org.isf.stat2.manager.StatsManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;

/**
 * CPN (ANC) statistics browsing, backed by the OH-538 pregnancy module
 * ({@link StatsManager#getPregnanciesStats}): visits, pregnancy history
 * and generic CPN exam parameters.
 */
public class CpnStatisticsBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private final StatsManager statsManager = Context.getApplicationContext().getBean(StatsManager.class);
	private final DiseaseBrowserManager diseasesManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private final DischargeTypeBrowserManager dischargeTypesManager = Context.getApplicationContext().getBean(DischargeTypeBrowserManager.class);
	private final VaccineBrowserManager vaccinesManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
	private final PregnancyExamParameterBrowserManager examParametersManager =
			Context.getApplicationContext().getBean(PregnancyExamParameterBrowserManager.class);

	private JPanel filtersPanel;
	private JPanel dataPanel;
	private JPanel paginationPanel;

	private GoodDateChooser periodFromChooser;
	private GoodDateChooser periodToChooser;
	private JTextField ageFromField;
	private JTextField ageToField;

	private JComboBox<PregnancyExamParameter> examParametersCombo;
	private JTextField examParameterValueField;

	private JComboBox<Vaccine> vaccinesCombo;
	private GoodDateChooser vaccinePeriodFromChooser;
	private GoodDateChooser vaccinePeriodToChooser;

	private JComboBox<Disease> diseasesCombo;
	private JComboBox<DischargeType> dischargeTypesCombo;

	private JPanel pregnancyPanel;
	private JComboBox<String> gravidityCombo;
	private JComboBox<String> miscarriageCombo;
	private JComboBox<String> gestationalAgeCombo;

	private JPanel pregnancyVisitPanel;
	private JComboBox<String> visitTypeCombo;
	private JComboBox<String> visitCountCombo;

	private List<Disease> diseasesData;
	private List<DischargeType> dischargeTypesData;
	private List<Vaccine> vaccinesData;
	private List<PregnancyExamParameter> examParametersData;

	private JTable jDataTable;
	private CpnStatsTableModel jDataTableModel;
	private List<Patient> patientList = new ArrayList<>();

	private final String[] jDataTableColumns = {
			MessageBundle.getMessage("angal.patient.firstname1"),
			MessageBundle.getMessage("angal.report.labregister.age")
	};
	private final int[] jDataTableColumnWidth = { 200, 50 };

	private JLabel resultCountLabel;
	private int resultCount = 0;
	private int _start_index = 0;
	private int _items_per_page = 20;

	private JButton filterBtn;
	private JButton filterResetBtn;

	private JButton paginationFirstBtn;
	private JButton paginationPrevBtn;
	private JButton paginationNextBtn;
	private JButton paginationLastBtn;
	private JComboBox<Integer> paginationCombo;
	private JLabel paginationLabel = new JLabel();

	public CpnStatisticsBrowser() {
		setTitle(MessageBundle.getMessage("angal.stat.menu.cpn"));
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 50, 1100, 700);
		setContentPane(buildMainPanel());
		setVisible(true);
	}

	private JPanel buildMainPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());

		filtersPanel = new JPanel(new BorderLayout());
		filtersPanel.add(buildFilterButtonsPanel(), BorderLayout.NORTH);

		JPanel accordions = new JPanel();
		accordions.setLayout(new javax.swing.BoxLayout(accordions, javax.swing.BoxLayout.Y_AXIS));

		JPanel generalPanel = buildGeneralFiltersPanel();
		JPanel cpnPanel = buildCpnFiltersPanel();
		JPanel vaccinesPanel = buildVaccinesFiltersPanel();
		JPanel diseasesPanel = buildDiseasesFiltersPanel();
		JPanel pregnancyPanel = buildPregnancyFiltersPanel();
		JPanel visitsPanel = buildVisitFiltersPanel();

		List<AccordionPanel> siblings = new ArrayList<>();
		AccordionPanel generalAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.title.general"), generalPanel, true);
		AccordionPanel cpnAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.cpn.cpnexamparameterbrowser.title"), cpnPanel, false);
		AccordionPanel vaccinesAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.vaccine"), vaccinesPanel, false);
		AccordionPanel diseasesAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.title.diseases"), diseasesPanel, false);
		AccordionPanel pregnancyAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.cpn.pregnancy.txt"), pregnancyPanel, false);
		AccordionPanel visitsAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.cpn.visittype.txt"), visitsPanel, false);

		siblings.add(generalAccordion);
		siblings.add(cpnAccordion);
		siblings.add(vaccinesAccordion);
		siblings.add(diseasesAccordion);
		siblings.add(pregnancyAccordion);
		siblings.add(visitsAccordion);
		siblings.forEach(s -> s.setSiblings(siblings));

		accordions.add(generalAccordion);
		accordions.add(cpnAccordion);
		accordions.add(vaccinesAccordion);
		accordions.add(diseasesAccordion);
		accordions.add(pregnancyAccordion);
		accordions.add(visitsAccordion);

		JScrollPane filtersScroll = new JScrollPane(accordions);
		filtersScroll.setPreferredSize(new Dimension(1100, 380));
		filtersPanel.add(filtersScroll, BorderLayout.CENTER);

		dataPanel = new JPanel(new BorderLayout());
		jDataTableModel = new CpnStatsTableModel();
		jDataTable = new JTable(jDataTableModel);
		jDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		dataPanel.add(new JScrollPane(jDataTable), BorderLayout.CENTER);
		dataPanel.add(getPaginationPanel(), BorderLayout.SOUTH);

		mainPanel.add(filtersPanel, BorderLayout.NORTH);
		mainPanel.add(dataPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	private JPanel buildFilterButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.filter"));
		filterBtn.addActionListener(e -> {
			_start_index = 0;
			runQuery(0);
		});
		filterResetBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.resetfilter"));
		filterResetBtn.addActionListener(e -> {
			resetAllFilters();
			runQuery(0);
		});
		buttonsPanel.add(filterBtn);
		buttonsPanel.add(filterResetBtn);
		return buttonsPanel;
	}

	private JPanel buildGeneralFiltersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		periodFromChooser = new GoodDateChooser(null, true, true);
		periodToChooser = new GoodDateChooser(null, true, true);

		ageFromField = new JTextField(5);
		ageToField = new JTextField(5);

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.dateFrom") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(periodFromChooser, gbc);
		gbc.gridx = 2;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.dateTo") + ":"), gbc);
		gbc.gridx = 3;
		panel.add(periodToChooser, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.report.labregister.age") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(ageFromField, gbc);
		gbc.gridx = 2;
		panel.add(new JLabel(" " + MessageBundle.getMessage("angal.stat.to") + " "), gbc);
		gbc.gridx = 3;
		panel.add(ageToField, gbc);

		return panel;
	}

	private JPanel buildCpnFiltersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		examParametersCombo = new JComboBox<>();
		examParameterValueField = new JTextField(12);
		try {
			examParametersData = examParametersManager.getPregnancyExamParameters();
			examParametersData.forEach(p -> examParametersCombo.addItem(p));
		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.cpn.cpnexamparameterbrowser.title") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(examParametersCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.cpn.type.txt") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(examParameterValueField, gbc);

		return panel;
	}

	private JPanel buildVaccinesFiltersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		vaccinesCombo = new JComboBox<>();
		vaccinePeriodFromChooser = new GoodDateChooser(null, true, true);
		vaccinePeriodToChooser = new GoodDateChooser(null, true, true);
		try {
			vaccinesData = vaccinesManager.getVaccine();
			vaccinesData.forEach(v -> vaccinesCombo.addItem(v));
		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.vaccine") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(vaccinesCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.dateFrom") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(vaccinePeriodFromChooser, gbc);
		gbc.gridx = 2;
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.dateTo") + ":"), gbc);
		gbc.gridx = 3;
		panel.add(vaccinePeriodToChooser, gbc);

		return panel;
	}

	private JPanel buildDiseasesFiltersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		diseasesCombo = new JComboBox<>();
		dischargeTypesCombo = new JComboBox<>();
		try {
			diseasesData = diseasesManager.getDiseaseIpdOut();
			diseasesData.forEach(d -> diseasesCombo.addItem(d));
			dischargeTypesData = dischargeTypesManager.getDischargeType();
			dischargeTypesData.forEach(d -> dischargeTypesCombo.addItem(d));
		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.diseases") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(diseasesCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.admission.dischargetype.border") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(dischargeTypesCombo, gbc);

		return panel;
	}

	private JPanel buildPregnancyFiltersPanel() {
		pregnancyPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gravidityCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				MessageBundle.getMessage("angal.stat.count.1"),
				MessageBundle.getMessage("angal.stat.count.2"),
				MessageBundle.getMessage("angal.stat.count.3"),
				MessageBundle.getMessage("angal.stat.count.4plus")
		});
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel_addLabelAndCombo(pregnancyPanel, gbc, 0, MessageBundle.getMessage("angal.stat.gravidity"), gravidityCombo);

		miscarriageCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				"0",
				"1",
				"2",
				"3+"
		});
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel_addLabelAndCombo(pregnancyPanel, gbc, 1, MessageBundle.getMessage("angal.stat.miscarriage"), miscarriageCombo);

		gestationalAgeCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				MessageBundle.getMessage("angal.stat.gestage.lt12"),
				MessageBundle.getMessage("angal.stat.gestage.12to24"),
				MessageBundle.getMessage("angal.stat.gestage.24to36"),
				MessageBundle.getMessage("angal.stat.gestage.gt36")
		});
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel_addLabelAndCombo(pregnancyPanel, gbc, 2, MessageBundle.getMessage("angal.stat.gestationalage"), gestationalAgeCombo);

		return pregnancyPanel;
	}

	private void panel_addLabelAndCombo(JPanel panel, GridBagConstraints gbc, int y,
			String label, JComboBox<String> combo) {
		gbc.gridx = 0;
		gbc.gridy = y;
		panel.add(new JLabel(label + ":"), gbc);
		gbc.gridx = 1;
		panel.add(combo, gbc);
	}

	private JPanel buildVisitFiltersPanel() {
		pregnancyVisitPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		visitTypeCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				MessageBundle.getMessage("angal.cpn.prenatal.txt"),
				MessageBundle.getMessage("angal.cpn.postnatal.txt")
		});
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel_addLabelAndCombo(pregnancyVisitPanel, gbc, 0, MessageBundle.getMessage("angal.cpn.visittype.txt"), visitTypeCombo);

		visitCountCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				"1+",
				"2+",
				"3+",
				"4+"
		});
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel_addLabelAndCombo(pregnancyVisitPanel, gbc, 1, MessageBundle.getMessage("angal.stat.visittype"), visitCountCombo);

		return pregnancyVisitPanel;
	}

	private Integer parseAge(String text) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String selectedValueOf(JComboBox<String> combo) {
		String selected = (String) combo.getSelectedItem();
		if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
			return null;
		}
		return selected;
	}

	private void runQuery(int pageIndex) {
		try {
			Integer ageFrom = parseAge(ageFromField.getText());
			Integer ageTo = parseAge(ageToField.getText());

			String periodFrom = periodFromChooser.getDate() != null ? periodFromChooser.getDate().toString() : null;
			String periodTo = periodToChooser.getDate() != null ? periodToChooser.getDate().toString() : null;

			Integer visitType = null;
			String visitTypeSelected = selectedValueOf(visitTypeCombo);
			if (visitTypeSelected != null) {
				visitType = visitTypeSelected.equals(MessageBundle.getMessage("angal.cpn.prenatal.txt"))
						? PregnancyVisit.PRENATAL : PregnancyVisit.POSTNATAL;
			}

			Integer visitCountMin = null;
			String visitCountSelected = selectedValueOf(visitCountCombo);
			if (visitCountSelected != null && visitCountSelected.endsWith("+")) {
				try {
					visitCountMin = Integer.parseInt(visitCountSelected.substring(0, visitCountSelected.length() - 1));
				} catch (NumberFormatException e) {
					visitCountMin = null;
				}
			}

			String examParameterCode = null;
			String examParameterValue = null;
			PregnancyExamParameter selectedParameter = (PregnancyExamParameter) examParametersCombo.getSelectedItem();
			if (selectedParameter != null && !selectedParameter.getCode().isEmpty()) {
				examParameterCode = selectedParameter.getCode();
				examParameterValue = examParameterValueField.getText();
				if (examParameterValue != null && examParameterValue.trim().isEmpty()) {
					examParameterValue = null;
				}
			}

			String examResult = null;
			String examPeriodFrom = null;
			String examPeriodTo = null;

			String vaccine = null;
			String vaccinePeriodFrom = null;
			String vaccinePeriodTo = null;
			Object selectedVaccine = vaccinesCombo.getSelectedItem();
			if (selectedVaccine != null && vaccinesCombo.getSelectedIndex() > 0) {
				vaccine = selectedVaccine.toString();
				vaccinePeriodFrom = vaccinePeriodFromChooser.getDate() != null
						? vaccinePeriodFromChooser.getDate().toString() : null;
				vaccinePeriodTo = vaccinePeriodToChooser.getDate() != null
						? vaccinePeriodToChooser.getDate().toString() : null;
			}

			String disease = null;
			Object selectedDisease = diseasesCombo.getSelectedItem();
			if (selectedDisease != null && diseasesCombo.getSelectedIndex() > 0) {
				disease = selectedDisease.toString();
			}

			String dischargeType = null;
			Object selectedDischarge = dischargeTypesCombo.getSelectedItem();
			if (selectedDischarge != null && dischargeTypesCombo.getSelectedIndex() > 0) {
				dischargeType = selectedDischarge.toString();
			}

			Integer gravidityMin = null;
			Integer gravidityMax = null;
			String graviditySelected = selectedValueOf(gravidityCombo);
			if (graviditySelected != null) {
				if (graviditySelected.equals(MessageBundle.getMessage("angal.stat.count.4plus"))) {
					gravidityMin = 4;
				} else {
					try {
						gravidityMin = Integer.parseInt(graviditySelected);
						gravidityMax = gravidityMin;
					} catch (NumberFormatException e) {
						gravidityMin = null;
					}
				}
			}

			Integer miscarriageMin = null;
			Integer miscarriageMax = null;
			String miscarriageSelected = selectedValueOf(miscarriageCombo);
			if (miscarriageSelected != null) {
				if (miscarriageSelected.endsWith("+")) {
					try {
						miscarriageMin = Integer.parseInt(miscarriageSelected.substring(0, miscarriageSelected.length() - 1));
					} catch (NumberFormatException e) {
						miscarriageMin = null;
					}
				} else {
					try {
						miscarriageMin = Integer.parseInt(miscarriageSelected);
						miscarriageMax = miscarriageMin;
					} catch (NumberFormatException e) {
						miscarriageMin = null;
					}
				}
			}

			Integer gestAgeMinWeeks = null;
			Integer gestAgeMaxWeeks = null;
			String gestAgeSelected = selectedValueOf(gestationalAgeCombo);
			if (gestAgeSelected != null) {
				if (gestAgeSelected.equals(MessageBundle.getMessage("angal.stat.gestage.lt12"))) {
					gestAgeMaxWeeks = 12;
				} else if (gestAgeSelected.equals(MessageBundle.getMessage("angal.stat.gestage.12to24"))) {
					gestAgeMinWeeks = 12;
					gestAgeMaxWeeks = 24;
				} else if (gestAgeSelected.equals(MessageBundle.getMessage("angal.stat.gestage.24to36"))) {
					gestAgeMinWeeks = 24;
					gestAgeMaxWeeks = 36;
				} else if (gestAgeSelected.equals(MessageBundle.getMessage("angal.stat.gestage.gt36"))) {
					gestAgeMinWeeks = 36;
				}
			}

			List<Patient> patients = statsManager.getPregnanciesStats(
					ageFrom, ageTo, periodFrom, periodTo,
					visitType, visitCountMin, null,
					examParameterCode, examParameterValue,
					examResult, examPeriodFrom, examPeriodTo,
					vaccine, vaccinePeriodFrom, vaccinePeriodTo,
					disease, dischargeType,
					gravidityMin, gravidityMax,
					miscarriageMin, miscarriageMax,
					gestAgeMinWeeks, gestAgeMaxWeeks,
					_start_index, _items_per_page
			);

			patientList = new ArrayList<>(patients);

			if (patientList.isEmpty() && _start_index == 0) {
				JLabel centeredMsg = new JLabel(MessageBundle.getMessage("angal.stat.nomatchfound"), JLabel.CENTER);
				JOptionPane.showMessageDialog(CpnStatisticsBrowser.this,
						centeredMsg,
						MessageBundle.getMessage("angal.stat.operationresult"), JOptionPane.PLAIN_MESSAGE);
			}

			jDataTableModel.fireTableDataChanged();
			jDataTable.updateUI();

		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}
	}

	private void resetAllFilters() {
		_start_index = 0;
		periodFromChooser.setDate(null);
		periodToChooser.setDate(null);
		ageFromField.setText("");
		ageToField.setText("");
		examParametersCombo.setSelectedIndex(0);
		examParameterValueField.setText("");
		vaccinesCombo.setSelectedIndex(0);
		vaccinePeriodFromChooser.setDate(null);
		vaccinePeriodToChooser.setDate(null);
		diseasesCombo.setSelectedIndex(0);
		dischargeTypesCombo.setSelectedIndex(0);
		gravidityCombo.setSelectedIndex(0);
		miscarriageCombo.setSelectedIndex(0);
		gestationalAgeCombo.setSelectedIndex(0);
		visitTypeCombo.setSelectedIndex(0);
		visitCountCombo.setSelectedIndex(0);
	}

	private JPanel getPaginationPanel() {
		if (paginationPanel != null) {
			return paginationPanel;
		}

		paginationPanel = new JPanel(new FlowLayout());
		JPanel resultCountPanel = new JPanel(new FlowLayout());
		resultCountLabel = new JLabel(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
		resultCountPanel.add(resultCountLabel);
		paginationPanel.add(resultCountPanel);
		paginationPanel.add(getPaginationFirstButton());
		paginationPanel.add(getPaginationPrevButton());
		paginationPanel.add(getPaginationCombo());
		paginationPanel.add(paginationLabel);
		paginationPanel.add(getPaginationNextButton());
		paginationPanel.add(getPaginationLastButton());

		return paginationPanel;
	}

	private JButton getPaginationFirstButton() {
		if (paginationFirstBtn != null) {
			return paginationFirstBtn;
		}
		paginationFirstBtn = new JButton("<<");
		paginationFirstBtn.setEnabled(false);
		paginationFirstBtn.addActionListener(e -> {
			_start_index = 0;
			paginationCombo.setSelectedItem(1);
		});
		return paginationFirstBtn;
	}

	private JButton getPaginationPrevButton() {
		if (paginationPrevBtn != null) {
			return paginationPrevBtn;
		}
		paginationPrevBtn = new JButton("<");
		paginationPrevBtn.setEnabled(false);
		paginationPrevBtn.addActionListener(e -> {
			_start_index -= _items_per_page;
			int page = _start_index / _items_per_page + 1;
			paginationCombo.setSelectedItem(page);
		});
		return paginationPrevBtn;
	}

	private JComboBox<Integer> getPaginationCombo() {
		if (paginationCombo != null) {
			return paginationCombo;
		}
		paginationCombo = new JComboBox<>();
		paginationCombo.addActionListener(e -> {
			if (paginationCombo.getItemCount() > 0 && paginationCombo.getSelectedItem() != null) {
				int pageNumber = (Integer) paginationCombo.getSelectedItem();
				_start_index = (pageNumber - 1) * _items_per_page;
				runQuery(pageNumber - 1);
				setPaginationButtons();
			}
		});
		return paginationCombo;
	}

	private JButton getPaginationNextButton() {
		if (paginationNextBtn != null) {
			return paginationNextBtn;
		}
		paginationNextBtn = new JButton(">");
		paginationNextBtn.setEnabled(false);
		paginationNextBtn.addActionListener(e -> {
			_start_index += _items_per_page;
			int page = _start_index / _items_per_page + 1;
			paginationCombo.setSelectedItem(page);
		});
		return paginationNextBtn;
	}

	private JButton getPaginationLastButton() {
		if (paginationLastBtn != null) {
			return paginationLastBtn;
		}
		paginationLastBtn = new JButton(">>");
		paginationLastBtn.setEnabled(false);
		paginationLastBtn.addActionListener(e -> {
			int lastPage = paginationCombo.getItemCount();
			if (lastPage > 0) {
				paginationCombo.setSelectedItem(lastPage);
			}
		});
		return paginationLastBtn;
	}

	private void setPaginationButtons() {
		if (_start_index + _items_per_page >= resultCount) {
			paginationNextBtn.setEnabled(false);
			paginationLastBtn.setEnabled(false);
		} else {
			paginationNextBtn.setEnabled(true);
			paginationLastBtn.setEnabled(true);
		}
		if (_start_index < _items_per_page) {
			paginationPrevBtn.setEnabled(false);
			paginationFirstBtn.setEnabled(false);
		} else {
			paginationPrevBtn.setEnabled(true);
			paginationFirstBtn.setEnabled(true);
		}
	}

	private class CpnStatsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return patientList != null ? patientList.size() : 0;
		}

		@Override
		public String getColumnName(int c) {
			return jDataTableColumns[c];
		}

		@Override
		public int getColumnCount() {
			return jDataTableColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Patient patient = patientList.get(r);
			if (c == 0) {
				return patient.getName();
			} else if (c == 1) {
				return patient.getAge();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	private class AccordionPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		@Override
		public Dimension getPreferredSize() {
			Dimension header = headerPanel.getPreferredSize();
			if (!expanded) {
				return new Dimension(header.width, header.height);
			}
			return super.getPreferredSize();
		}

		@Override
		public Dimension getMaximumSize() {
			Dimension pref = getPreferredSize();
			return new Dimension(Integer.MAX_VALUE, pref.height);
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		private final JPanel headerPanel;
		private final JPanel contentPanel;
		private final JLabel toggleLabel;
		private boolean expanded;
		private List<AccordionPanel> siblings = new ArrayList<>();

		public AccordionPanel(String title, JPanel content, boolean defaultExpanded) {
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
					BorderFactory.createEmptyBorder(2, 2, 2, 2)
			));
			this.expanded = defaultExpanded;

			headerPanel = new JPanel(new BorderLayout());
			headerPanel.setBackground(new Color(240, 240, 240));
			headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			JLabel titleLabel = new JLabel(title);
			titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13));

			toggleLabel = new JLabel(expanded ? " ▼" : " ▶");
			toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.BOLD, 14));

			headerPanel.add(titleLabel, BorderLayout.CENTER);
			headerPanel.add(toggleLabel, BorderLayout.EAST);

			contentPanel = content;
			contentPanel.setVisible(expanded);

			add(headerPanel, BorderLayout.NORTH);
			add(contentPanel, BorderLayout.CENTER);

			headerPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					toggle();
				}
			});
		}

		public void setSiblings(List<AccordionPanel> siblings) {
			this.siblings = siblings;
		}

		public void toggle() {
			expanded = !expanded;

			if (expanded) {
				siblings.forEach(s -> {
					if (s != this && s.expanded) {
						s.toggle();
					}
				});
				add(contentPanel, BorderLayout.CENTER);
			} else {
				remove(contentPanel);
			}
			contentPanel.setVisible(expanded);
			toggleLabel.setText(expanded ? " ▼" : " ▶");

			revalidateFully();
		}

		private void revalidateFully() {
			Container top = this;
			while (top.getParent() != null) {
				top = top.getParent();
			}
			top.revalidate();
			top.repaint();
		}
	}
}
