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
import java.time.LocalDateTime;
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
import org.isf.dlvrtype.manager.DeliveryTypeBrowserManager;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.stat2.manager.StatsDeliveryManager;
import org.isf.stat2.model.StatsDelivery;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.springframework.data.domain.Page;

/**
 * Delivery statistics browsing, backed by the OH-538 pregnancy module
 * ({@link StatsDeliveryManager}).
 */
public class DeliveryStatisticsBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private final StatsDeliveryManager statsManager = Context.getApplicationContext().getBean(StatsDeliveryManager.class);
	private final DiseaseBrowserManager diseasesManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private final DischargeTypeBrowserManager dischargeTypesManager = Context.getApplicationContext().getBean(DischargeTypeBrowserManager.class);
	private final DeliveryTypeBrowserManager deliveryTypeManager = Context.getApplicationContext().getBean(DeliveryTypeBrowserManager.class);

	private JPanel filtersPanel;
	private JPanel dataPanel;
	private JPanel paginationPanel;
	private JPanel headerPanel;

	private GoodDateChooser periodFromChooser;
	private GoodDateChooser periodToChooser;
	private JTextField motherAgeMinField;
	private JTextField motherAgeMaxField;

	private JComboBox<String> deliveryTypeCombo;
	private JComboBox<String> deliveryResultCombo;
	private JComboBox<String> newbornSexCombo;
	private JTextField weightMinField;
	private JTextField weightMaxField;
	private JComboBox<String> hivExposedCombo;
	private JComboBox<String> congenitalAnomaliesCombo;
	private JComboBox<Disease> diseasesCombo;
	private JComboBox<DischargeType> dischargeTypesCombo;

	private List<Disease> diseasesData;
	private List<DischargeType> dischargeTypesData;

	private JTable jDataTable;
	private DeliveryStatsTableModel jDataTableModel;
	private List<StatsDelivery> deliveryList = new ArrayList<>();

	private final String[] jDataTableColumns = {
			MessageBundle.getMessage("angal.stat.mothersname"),
			MessageBundle.getMessage("angal.stat.mothersage"),
			MessageBundle.getMessage("angal.stat.childsex"),
			MessageBundle.getMessage("angal.stat.birthweight"),
			MessageBundle.getMessage("angal.stat.deliveryresult")
	};

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

	private LocalDateTime periodFrom;
	private LocalDateTime periodTo;
	private Integer motherAgeMin;
	private Integer motherAgeMax;
	private String selectedSex;
	private Double newbornWeightMin;
	private Double newbornWeightMax;
	private String selectedDeliveryType;
	private String selectedDeliveryResult;
	private Boolean selectedHivExposed;
	private Boolean selectedCongenitalAnomalies;
	private String selectedDisease;
	private String selectedDischargeType;

	public DeliveryStatisticsBrowser() {
		setTitle(MessageBundle.getMessage("angal.stat.deliverystatsbrowsing"));
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 50, 1200, 750);
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
		JPanel deliveryPanel = buildDeliveryFiltersPanel();
		JPanel newbornPanel = buildNewbornFiltersPanel();
		JPanel diseasesPanel = buildDiseasesFiltersPanel();

		List<AccordionPanel> siblings = new ArrayList<>();
		AccordionPanel generalAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.title.general"), generalPanel, true);
		AccordionPanel deliveryAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.title.delivery"), deliveryPanel, false);
		AccordionPanel newbornAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.title.newborn"), newbornPanel, false);
		AccordionPanel diseasesAccordion = new AccordionPanel(
				MessageBundle.getMessage("angal.stat.title.diseases"), diseasesPanel, false);

		siblings.add(generalAccordion);
		siblings.add(deliveryAccordion);
		siblings.add(newbornAccordion);
		siblings.add(diseasesAccordion);
		generalAccordion.setSiblings(siblings);
		deliveryAccordion.setSiblings(siblings);
		newbornAccordion.setSiblings(siblings);
		diseasesAccordion.setSiblings(siblings);

		accordions.add(generalAccordion);
		accordions.add(deliveryAccordion);
		accordions.add(newbornAccordion);
		accordions.add(diseasesAccordion);

		JScrollPane filtersScroll = new JScrollPane(accordions);
		filtersScroll.setPreferredSize(new Dimension(1200, 380));
		filtersPanel.add(filtersScroll, BorderLayout.CENTER);

		dataPanel = new JPanel(new BorderLayout());
		jDataTableModel = new DeliveryStatsTableModel();
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
			collectFilters();
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

		motherAgeMinField = new JTextField(5);
		motherAgeMaxField = new JTextField(5);

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
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.motherage") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(motherAgeMinField, gbc);
		gbc.gridx = 2;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.to") + " "), gbc);
		gbc.gridx = 3;
		panel.add(motherAgeMaxField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel(MessageBundle.getMessage("angal.patient.sex") + ":"), gbc);
		gbc.gridx = 1;
		newbornSexCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				MessageBundle.getMessage("angal.stat.sex.male"),
				MessageBundle.getMessage("angal.stat.sex.female")
		});
		panel.add(newbornSexCombo, gbc);

		return panel;
	}

	private JPanel buildDeliveryFiltersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		deliveryTypeCombo = new JComboBox<>();
		deliveryTypeCombo.addItem(MessageBundle.getMessage("angal.stat.all"));
		try {
			deliveryTypeManager.getDeliveryType().forEach(type ->
					deliveryTypeCombo.addItem(type.getDescription()));
		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.deliverytype") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(deliveryTypeCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.deliveryresult") + ":"), gbc);
		gbc.gridx = 1;
		deliveryResultCombo = new JComboBox<>();
		deliveryResultCombo.addItem(MessageBundle.getMessage("angal.stat.all"));
		try {
			Context.getApplicationContext().getBean(org.isf.dlvrrestype.manager.DeliveryResultTypeBrowserManager.class)
					.getDeliveryResultType().forEach(result -> deliveryResultCombo.addItem(result.getDescription()));
		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}
		panel.add(deliveryResultCombo, gbc);

		return panel;
	}

	private JPanel buildNewbornFiltersPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.birthweight") + " (min):"), gbc);
		gbc.gridx = 1;
		weightMinField = new JTextField(5);
		weightMaxField = new JTextField(5);

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel(MessageBundle.getMessage("angal.stat.birthweight") + ":"), gbc);
		gbc.gridx = 1;
		panel.add(weightMinField, gbc);
		gbc.gridx = 2;
		panel.add(new JLabel(" " + MessageBundle.getMessage("angal.stat.to") + " "), gbc);
		gbc.gridx = 3;
		panel.add(weightMaxField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel(MessageBundle.getMessage("angal.cpn.hivexposed.txt") + ":"), gbc);
		gbc.gridx = 1;
		hivExposedCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				MessageBundle.getMessage("angal.common.yes.label"),
				MessageBundle.getMessage("angal.common.no.label")
		});
		panel.add(hivExposedCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel(MessageBundle.getMessage("angal.cpn.congenitalmalformation.txt") + ":"), gbc);
		gbc.gridx = 1;
		congenitalAnomaliesCombo = new JComboBox<>(new String[] {
				MessageBundle.getMessage("angal.stat.all"),
				MessageBundle.getMessage("angal.common.yes.label"),
				MessageBundle.getMessage("angal.common.no.label")
		});
		panel.add(congenitalAnomaliesCombo, gbc);

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

	private void collectFilters() {
		periodFrom = periodFromChooser.getDate() != null ? periodFromChooser.getDate().atStartOfDay() : null;
		periodTo = periodToChooser.getDate() != null ? periodToChooser.getDate().atTime(23, 59, 59) : null;

		motherAgeMin = parseAge(motherAgeMinField.getText());
		motherAgeMax = parseAge(motherAgeMaxField.getText());

		selectedSex = getSelectedSex();

		newbornWeightMin = parseDouble(weightMinField.getText());
		newbornWeightMax = parseDouble(weightMaxField.getText());

		selectedDeliveryType = getComboSelectedOrNull(deliveryTypeCombo);
		selectedDeliveryResult = getComboSelectedOrNull(deliveryResultCombo);

		selectedHivExposed = getSelectedYesNo(hivExposedCombo);
		selectedCongenitalAnomalies = getSelectedYesNo(congenitalAnomaliesCombo);

		Object selectedDiseaseObj = diseasesCombo.getSelectedItem();
		selectedDisease = selectedDiseaseObj != null ? selectedDiseaseObj.toString() : null;

		Object selectedDischargeObj = dischargeTypesCombo.getSelectedItem();
		selectedDischargeType = selectedDischargeObj != null ? selectedDischargeObj.toString() : null;
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

	private Double parseDouble(Object value) {
		if (value == null || value.toString().trim().isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(value.toString().trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String getSelectedSex() {
		String selected = (String) newbornSexCombo.getSelectedItem();
		if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
			return null;
		}
		if (selected.equals(MessageBundle.getMessage("angal.stat.sex.male"))) {
			return "M";
		}
		if (selected.equals(MessageBundle.getMessage("angal.stat.sex.female"))) {
			return "F";
		}
		return null;
	}

	private String getComboSelectedOrNull(JComboBox<String> combo) {
		String selected = (String) combo.getSelectedItem();
		if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
			return null;
		}
		return selected;
	}

	private Boolean getSelectedYesNo(JComboBox<String> combo) {
		String selected = (String) combo.getSelectedItem();
		if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
			return null;
		}
		return selected.equals(MessageBundle.getMessage("angal.common.yes.label"));
	}

	private void runQuery(int pageIndex) {
		try {
			Page<StatsDelivery> result = statsManager.getDeliveriesStats(
					periodFrom, periodTo,
					motherAgeMin, motherAgeMax,
					selectedSex,
					newbornWeightMin, newbornWeightMax,
					selectedDeliveryType,
					selectedDeliveryResult,
					selectedHivExposed,
					selectedCongenitalAnomalies,
					selectedDisease,
					selectedDischargeType,
					pageIndex, _items_per_page
			);

			resultCount = (int) result.getTotalElements();
			deliveryList = new ArrayList<>(result.getContent());

			if (deliveryList.isEmpty() && resultCount == 0) {
				JLabel centeredMsg = new JLabel(MessageBundle.getMessage("angal.stat.nomatchfound"), JLabel.CENTER);
				JOptionPane.showMessageDialog(DeliveryStatisticsBrowser.this,
						centeredMsg,
						MessageBundle.getMessage("angal.stat.operationresult"), JOptionPane.PLAIN_MESSAGE);
			}

			jDataTableModel.fireTableDataChanged();
			jDataTable.updateUI();
			resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
			initializePaginationCombo();

		} catch (OHServiceException ex) {
			MessageDialog.showExceptions(ex);
		}
	}

	private void resetAllFilters() {
		_start_index = 0;
		periodFromChooser.setDate(null);
		periodToChooser.setDate(null);
		motherAgeMinField.setText("");
		motherAgeMaxField.setText("");
		newbornSexCombo.setSelectedIndex(0);
		weightMinField.setText("");
		weightMaxField.setText("");
		deliveryTypeCombo.setSelectedIndex(0);
		deliveryResultCombo.setSelectedIndex(0);
		hivExposedCombo.setSelectedIndex(0);
		congenitalAnomaliesCombo.setSelectedIndex(0);
		diseasesCombo.setSelectedIndex(0);
		dischargeTypesCombo.setSelectedIndex(0);
		collectFilters();
	}

	private JPanel getPaginationPanel() {
		if (paginationPanel != null) {
			return paginationPanel;
		}

		paginationPanel = new JPanel(new FlowLayout());
		paginationPanel.add(getResultCountPanel());
		paginationPanel.add(getPaginationFirstButton());
		paginationPanel.add(getPaginationPrevButton());
		paginationPanel.add(getPaginationCombo());
		paginationPanel.add(paginationLabel);
		paginationPanel.add(getPaginationNextButton());
		paginationPanel.add(getPaginationLastButton());

		return paginationPanel;
	}

	private JPanel getResultCountPanel() {
		JPanel resultCountPanel = new JPanel(new FlowLayout());
		resultCountLabel = new JLabel(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
		resultCountPanel.add(resultCountLabel);
		return resultCountPanel;
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

	public void initializePaginationCombo() {
		int j = 0;
		paginationCombo.removeAllItems();
		for (int i = 0; i < resultCount / _items_per_page; i++) {
			j = i + 1;
			paginationCombo.addItem(j);
		}
		if (j * _items_per_page < resultCount) {
			paginationCombo.addItem(j + 1);
			paginationLabel.setText("/" + (resultCount / _items_per_page + 1) + " Pages");
		} else {
			paginationLabel.setText("/" + (j == 0 ? 0 : resultCount / _items_per_page) + " Pages");
		}
		setPaginationButtons();
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

	private class DeliveryStatsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return deliveryList != null ? deliveryList.size() : 0;
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
			StatsDelivery stats = deliveryList.get(r);
			switch (c) {
				case 0:
					return stats.getMotherName();
				case 1:
					return stats.getMotherAge();
				case 2:
					return stats.getNewbornSex();
				case 3:
					return stats.getNewbornWeight();
				case 4:
					return stats.getNewbornDeliveryResult();
				default:
					return null;
			}
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
