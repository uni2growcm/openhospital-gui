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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.isf.admission.model.Admission;
import org.isf.dlvrrestype.manager.DeliveryResultTypeBrowserManager;
import org.isf.dlvrrestype.model.DeliveryResultType;
import org.isf.dlvrtype.manager.DeliveryTypeBrowserManager;
import org.isf.dlvrtype.model.DeliveryType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.pregnancy.manager.PregnancyDeliveryBrowserManager;
import org.isf.pregnancy.model.Lochies;
import org.isf.pregnancy.model.NewbornFeedingMode;
import org.isf.pregnancy.model.PregnancyDelivery;
import org.isf.pregnancy.model.PregnancyNewborn;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

/**
 * Reusable panel embedded in the admission delivery workflow: it shows the length of stay in nights, the
 * mother-level delivery information (father identification, lochies, counseling, family planning method
 * chosen at discharge) and up to {@value PregnancyNewborn#MAX_CHILDREN} newborns, each with its own weight,
 * height, head/chest/arm circumference, complications, congenital malformation, feeding mode and HIV
 * exposure flag.
 * <p>
 * Call {@link #loadFor(Admission)} once the admission being edited is known (and again after it has been
 * persisted, to refresh the length of stay), and {@link #saveFor(Admission)} once the admission has a valid
 * database id.
 */
public class PregnancyDeliveryPanel extends JPanel {

	private final PregnancyDeliveryBrowserManager deliveryManager = Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);
	private final DeliveryTypeBrowserManager deliveryTypeBrowserManager = Context.getApplicationContext().getBean(DeliveryTypeBrowserManager.class);
	private final DeliveryResultTypeBrowserManager deliveryResultTypeBrowserManager = Context.getApplicationContext()
					.getBean(DeliveryResultTypeBrowserManager.class);

	private List<DeliveryType> deliveryTypeList = new ArrayList<>();
	private List<DeliveryResultType> deliveryResultTypeList = new ArrayList<>();

	private JTextField nightsOfStayField;
	private JComboBox<Object> lochiesBox;
	private JTextField counselingField;
	private JTextField familyPlanningMethodField;

	private JTextField fatherNameField;
	private JTextField fatherOccupationField;
	private JTextField fatherResidenceField;
	private JTextField fatherBirthPlaceField;
	private JSpinner fatherAgeSpinner;
	private JTextField fatherCniField;

	private final NewbornRowPanel[] newbornRows = new NewbornRowPanel[PregnancyNewborn.MAX_CHILDREN];

	private PregnancyDelivery currentDelivery;

	public PregnancyDeliveryPanel() {
		super();
		try {
			deliveryTypeList = deliveryTypeBrowserManager.getDeliveryType();
			deliveryResultTypeList = deliveryResultTypeBrowserManager.getDeliveryResultType();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel motherPanel = new JPanel(new GridBagLayout());
		motherPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.delivery.title")));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		nightsOfStayField = new JTextField(5);
		nightsOfStayField.setEditable(false);
		nightsOfStayField.setFocusable(false);
		addField(motherPanel, c, 0, 0, "angal.cpn.nightsofstay.txt", nightsOfStayField);

		lochiesBox = new JComboBox<>();
		lochiesBox.addItem("");
		lochiesBox.addItem(Lochies.NORMAL);
		lochiesBox.addItem(Lochies.ABUNDANT);
		lochiesBox.addItem(Lochies.ABSENT);
		addField(motherPanel, c, 1, 0, "angal.cpn.lochies.txt", lochiesBox);

		counselingField = new JTextField(20);
		addField(motherPanel, c, 0, 1, "angal.cpn.counseling.txt", counselingField);

		familyPlanningMethodField = new JTextField(20);
		addField(motherPanel, c, 1, 1, "angal.cpn.familyplanningmethod.txt", familyPlanningMethodField);

		JPanel fatherPanel = new JPanel(new GridBagLayout());
		fatherPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.father.title")));
		GridBagConstraints fc = new GridBagConstraints();
		fc.insets = new Insets(3, 5, 3, 5);
		fc.fill = GridBagConstraints.HORIZONTAL;

		fatherNameField = new JTextField(20);
		addField(fatherPanel, fc, 0, 0, "angal.cpn.fathername.txt", fatherNameField);
		fatherOccupationField = new JTextField(20);
		addField(fatherPanel, fc, 1, 0, "angal.cpn.fatheroccupation.txt", fatherOccupationField);
		fatherResidenceField = new JTextField(20);
		addField(fatherPanel, fc, 0, 1, "angal.cpn.fatherresidence.txt", fatherResidenceField);
		fatherBirthPlaceField = new JTextField(20);
		addField(fatherPanel, fc, 1, 1, "angal.cpn.fatherbirthplace.txt", fatherBirthPlaceField);
		fatherAgeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
		addField(fatherPanel, fc, 0, 2, "angal.cpn.fatherage.txt", fatherAgeSpinner);
		fatherCniField = new JTextField(20);
		addField(fatherPanel, fc, 1, 2, "angal.cpn.fathercni.txt", fatherCniField);

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.add(motherPanel);
		headerPanel.add(fatherPanel);

		JPanel newbornsPanel = new JPanel();
		newbornsPanel.setLayout(new BoxLayout(newbornsPanel, BoxLayout.Y_AXIS));
		newbornsPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.newborns.title")));
		for (int i = 0; i < PregnancyNewborn.MAX_CHILDREN; i++) {
			newbornRows[i] = new NewbornRowPanel(i + 1, deliveryTypeList, deliveryResultTypeList);
			newbornsPanel.add(newbornRows[i]);
		}

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(headerPanel);
		topPanel.add(newbornsPanel);

		add(new JScrollPane(topPanel), BorderLayout.CENTER);
	}

	private void addField(JPanel panel, GridBagConstraints template, int col, int row, String labelKey, java.awt.Component field) {
		GridBagConstraints labelConstraints = (GridBagConstraints) template.clone();
		labelConstraints.gridx = col * 2;
		labelConstraints.gridy = row;
		panel.add(new JLabel(MessageBundle.getMessage(labelKey)), labelConstraints);

		GridBagConstraints fieldConstraints = (GridBagConstraints) template.clone();
		fieldConstraints.gridx = col * 2 + 1;
		fieldConstraints.gridy = row;
		panel.add(field, fieldConstraints);
	}

	/**
	 * Loads (or resets, for a brand new admission) the delivery data for the given admission. Safe to call
	 * repeatedly, e.g. once when the delivery tab is built and again right after the admission has been
	 * persisted, so that the length of stay reflects the saved discharge date.
	 */
	public void loadFor(Admission admission) {
		nightsOfStayField.setText(String.valueOf(deliveryManager.computeNightsOfStay(admission)));

		currentDelivery = null;
		if (admission.getId() > 0) {
			try {
				currentDelivery = deliveryManager.getCurrentForAdmission(admission.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		if (currentDelivery == null) {
			lochiesBox.setSelectedIndex(0);
			counselingField.setText("");
			familyPlanningMethodField.setText("");
			fatherNameField.setText("");
			fatherOccupationField.setText("");
			fatherResidenceField.setText("");
			fatherBirthPlaceField.setText("");
			fatherAgeSpinner.setValue(0);
			fatherCniField.setText("");
			for (NewbornRowPanel row : newbornRows) {
				row.reset();
			}
			newbornRows[0].setActive(true);
			return;
		}

		lochiesBox.setSelectedItem(currentDelivery.getLochies() == null ? "" : currentDelivery.getLochies());
		counselingField.setText(nullToEmpty(currentDelivery.getCounseling()));
		familyPlanningMethodField.setText(nullToEmpty(currentDelivery.getFamilyPlanningMethodChosen()));
		fatherNameField.setText(nullToEmpty(currentDelivery.getFatherName()));
		fatherOccupationField.setText(nullToEmpty(currentDelivery.getFatherOccupation()));
		fatherResidenceField.setText(nullToEmpty(currentDelivery.getFatherResidence()));
		fatherBirthPlaceField.setText(nullToEmpty(currentDelivery.getFatherBirthPlace()));
		fatherAgeSpinner.setValue(currentDelivery.getFatherAge() == null ? 0 : currentDelivery.getFatherAge());
		fatherCniField.setText(currentDelivery.getFatherCni() == null ? "" : String.valueOf(currentDelivery.getFatherCni()));

		for (NewbornRowPanel row : newbornRows) {
			row.reset();
		}
		for (PregnancyNewborn newborn : currentDelivery.getNewborns()) {
			int index = newborn.getChildNumber() - 1;
			if (index >= 0 && index < newbornRows.length) {
				newbornRows[index].populate(newborn);
			}
		}
	}

	/**
	 * Persists the delivery data (mother-level fields and the active newborn rows) for an admission that has
	 * already been saved (i.e. {@code admission.getId() > 0}). Does nothing if no newborn row is active and
	 * no mother-level field has been filled in, so that admissions unrelated to a pregnancy are not affected.
	 */
	public void saveFor(Admission admission) {
		boolean anyNewbornActive = false;
		for (NewbornRowPanel row : newbornRows) {
			if (row.isActive()) {
				anyNewbornActive = true;
				break;
			}
		}
		boolean anyMotherField = lochiesBox.getSelectedIndex() > 0
						|| !counselingField.getText().isBlank()
						|| !familyPlanningMethodField.getText().isBlank()
						|| !fatherNameField.getText().isBlank();

		if (!anyNewbornActive && !anyMotherField && currentDelivery == null) {
			return;
		}

		PregnancyDelivery delivery = currentDelivery != null ? currentDelivery : new PregnancyDelivery(admission);
		delivery.setAdmission(admission);
		delivery.setDeliveryDate(admission.getDeliveryDate());
		delivery.setLochies(lochiesBox.getSelectedIndex() <= 0 ? null : (Lochies) lochiesBox.getSelectedItem());
		delivery.setCounseling(emptyToNull(counselingField.getText()));
		delivery.setFamilyPlanningMethodChosen(emptyToNull(familyPlanningMethodField.getText()));
		delivery.setFatherName(emptyToNull(fatherNameField.getText()));
		delivery.setFatherOccupation(emptyToNull(fatherOccupationField.getText()));
		delivery.setFatherResidence(emptyToNull(fatherResidenceField.getText()));
		delivery.setFatherBirthPlace(emptyToNull(fatherBirthPlaceField.getText()));
		Integer fatherAge = (Integer) fatherAgeSpinner.getValue();
		delivery.setFatherAge(fatherAge == null || fatherAge == 0 ? null : fatherAge);
		String cni = fatherCniField.getText();
		try {
			delivery.setFatherCni(cni == null || cni.isBlank() ? null : Long.valueOf(cni.trim()));
		} catch (NumberFormatException e) {
			delivery.setFatherCni(null);
		}

		List<PregnancyNewborn> newborns = new ArrayList<>();
		for (NewbornRowPanel row : newbornRows) {
			if (row.isActive()) {
				newborns.add(row.toNewborn(delivery));
			}
		}
		delivery.getNewborns().clear();
		delivery.getNewborns().addAll(newborns);

		try {
			currentDelivery = deliveryManager.saveOrUpdate(delivery);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static String emptyToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	/**
	 * One newborn's fields (a real, addable/removable row backed by a {@link PregnancyNewborn}), toggled on
	 * and off with the "active" checkbox instead of a dynamic table, which keeps the layout stable while
	 * still allowing between 1 and {@value PregnancyNewborn#MAX_CHILDREN} children per delivery.
	 */
	private static final class NewbornRowPanel extends JPanel {

		private final int childNumber;
		private final List<DeliveryType> deliveryTypeList;
		private final List<DeliveryResultType> deliveryResultTypeList;

		private final JCheckBox activeCheckBox;
		private final JTextField childNameField;
		private final JComboBox<String> sexBox;
		private final JComboBox<Object> deliveryTypeBox;
		private final JComboBox<Object> deliveryResultTypeBox;
		private final JSpinner weightSpinner;
		private final JSpinner heightSpinner;
		private final JSpinner headCircumferenceSpinner;
		private final JSpinner chestCircumferenceSpinner;
		private final JSpinner armCircumferenceSpinner;
		private final JTextField complicationsField;
		private final JTextField malformationField;
		private final JComboBox<Object> feedingModeBox;
		private final JCheckBox hivExposedCheckBox;

		private final List<java.awt.Component> dependentFields = new ArrayList<>();

		NewbornRowPanel(int childNumber, List<DeliveryType> deliveryTypeList, List<DeliveryResultType> deliveryResultTypeList) {
			this.childNumber = childNumber;
			this.deliveryTypeList = deliveryTypeList;
			this.deliveryResultTypeList = deliveryResultTypeList;

			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createTitledBorder(MessageBundle.formatMessage("angal.cpn.newborn.fmt.txt", childNumber)));
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(2, 4, 2, 4);
			c.fill = GridBagConstraints.HORIZONTAL;

			activeCheckBox = new JCheckBox(MessageBundle.getMessage("angal.cpn.active.txt"));
			c.gridx = 0;
			c.gridy = 0;
			add(activeCheckBox, c);

			childNameField = new JTextField(15);
			addLabeledField(c, 1, 0, "angal.cpn.childname.txt", childNameField);

			sexBox = new JComboBox<>(new String[] { "F", "M" });
			addLabeledField(c, 2, 0, "angal.cpn.sex.txt", sexBox);

			deliveryTypeBox = new JComboBox<>();
			deliveryTypeBox.addItem("");
			for (DeliveryType type : deliveryTypeList) {
				deliveryTypeBox.addItem(type);
			}
			addLabeledField(c, 0, 1, "angal.cpn.deliverytype.txt", deliveryTypeBox);

			deliveryResultTypeBox = new JComboBox<>();
			deliveryResultTypeBox.addItem("");
			for (DeliveryResultType type : deliveryResultTypeList) {
				deliveryResultTypeBox.addItem(type);
			}
			addLabeledField(c, 1, 1, "angal.cpn.deliveryresulttype.txt", deliveryResultTypeBox);

			weightSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 10.0));
			addLabeledField(c, 2, 1, "angal.cpn.weightgrams.txt", weightSpinner);

			heightSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
			addLabeledField(c, 0, 2, "angal.cpn.heightcm.txt", heightSpinner);

			headCircumferenceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
			addLabeledField(c, 1, 2, "angal.cpn.headcircumference.txt", headCircumferenceSpinner);

			chestCircumferenceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 50.0, 0.5));
			addLabeledField(c, 2, 2, "angal.cpn.chestcircumference.txt", chestCircumferenceSpinner);

			armCircumferenceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 30.0, 0.5));
			addLabeledField(c, 0, 3, "angal.cpn.armcircumference.txt", armCircumferenceSpinner);

			complicationsField = new JTextField(15);
			addLabeledField(c, 1, 3, "angal.cpn.complications.txt", complicationsField);

			malformationField = new JTextField(15);
			addLabeledField(c, 2, 3, "angal.cpn.congenitalmalformation.txt", malformationField);

			feedingModeBox = new JComboBox<>();
			feedingModeBox.addItem("");
			feedingModeBox.addItem(NewbornFeedingMode.EXCLUSIVE_BREASTFEEDING);
			feedingModeBox.addItem(NewbornFeedingMode.ARTIFICIAL);
			feedingModeBox.addItem(NewbornFeedingMode.MIXED);
			addLabeledField(c, 0, 4, "angal.cpn.feedingmode.txt", feedingModeBox);

			hivExposedCheckBox = new JCheckBox(MessageBundle.getMessage("angal.cpn.hivexposed.txt"));
			c.gridx = 1;
			c.gridy = 4;
			add(hivExposedCheckBox, c);

			dependentFields.add(childNameField);
			dependentFields.add(sexBox);
			dependentFields.add(deliveryTypeBox);
			dependentFields.add(deliveryResultTypeBox);
			dependentFields.add(weightSpinner);
			dependentFields.add(heightSpinner);
			dependentFields.add(headCircumferenceSpinner);
			dependentFields.add(chestCircumferenceSpinner);
			dependentFields.add(armCircumferenceSpinner);
			dependentFields.add(complicationsField);
			dependentFields.add(malformationField);
			dependentFields.add(feedingModeBox);
			dependentFields.add(hivExposedCheckBox);

			activeCheckBox.addActionListener(e -> updateEnabled());
			// first child is active by default, the others start disabled (i.e. "removed") until checked
			activeCheckBox.setSelected(childNumber == 1);
			updateEnabled();
		}

		private void addLabeledField(GridBagConstraints template, int col, int row, String labelKey, java.awt.Component field) {
			GridBagConstraints labelConstraints = (GridBagConstraints) template.clone();
			labelConstraints.gridx = col * 2;
			labelConstraints.gridy = row;
			add(new JLabel(MessageBundle.getMessage(labelKey)), labelConstraints);

			GridBagConstraints fieldConstraints = (GridBagConstraints) template.clone();
			fieldConstraints.gridx = col * 2 + 1;
			fieldConstraints.gridy = row;
			add(field, fieldConstraints);
		}

		private void updateEnabled() {
			boolean active = activeCheckBox.isSelected();
			for (java.awt.Component field : dependentFields) {
				field.setEnabled(active);
			}
		}

		boolean isActive() {
			return activeCheckBox.isSelected();
		}

		void setActive(boolean active) {
			activeCheckBox.setSelected(active);
			updateEnabled();
		}

		void reset() {
			setActive(childNumber == 1);
			childNameField.setText("");
			sexBox.setSelectedIndex(0);
			deliveryTypeBox.setSelectedIndex(0);
			deliveryResultTypeBox.setSelectedIndex(0);
			weightSpinner.setValue(0.0);
			heightSpinner.setValue(0.0);
			headCircumferenceSpinner.setValue(0.0);
			chestCircumferenceSpinner.setValue(0.0);
			armCircumferenceSpinner.setValue(0.0);
			complicationsField.setText("");
			malformationField.setText("");
			feedingModeBox.setSelectedIndex(0);
			hivExposedCheckBox.setSelected(false);
		}

		void populate(PregnancyNewborn newborn) {
			setActive(true);
			childNameField.setText(nullToEmpty(newborn.getChildName()));
			sexBox.setSelectedItem(String.valueOf(newborn.getSex()));
			deliveryTypeBox.setSelectedItem(newborn.getDeliveryType() == null ? "" : newborn.getDeliveryType());
			deliveryResultTypeBox.setSelectedItem(newborn.getDeliveryResultType() == null ? "" : newborn.getDeliveryResultType());
			weightSpinner.setValue(newborn.getWeight() == null ? 0.0 : newborn.getWeight().doubleValue());
			heightSpinner.setValue(newborn.getHeight() == null ? 0.0 : newborn.getHeight().doubleValue());
			headCircumferenceSpinner.setValue(newborn.getHeadCircumference() == null ? 0.0 : newborn.getHeadCircumference());
			chestCircumferenceSpinner.setValue(newborn.getChestCircumference() == null ? 0.0 : newborn.getChestCircumference());
			armCircumferenceSpinner.setValue(newborn.getArmCircumference() == null ? 0.0 : newborn.getArmCircumference());
			complicationsField.setText(nullToEmpty(newborn.getComplications()));
			malformationField.setText(nullToEmpty(newborn.getCongenitalMalformation()));
			feedingModeBox.setSelectedItem(newborn.getFeedingMode() == null ? "" : newborn.getFeedingMode());
			hivExposedCheckBox.setSelected(newborn.isHivExposed());
		}

		PregnancyNewborn toNewborn(org.isf.pregnancy.model.PregnancyDelivery delivery) {
			PregnancyNewborn newborn = new PregnancyNewborn(delivery, childNumber);
			newborn.setChildName(emptyToNull(childNameField.getText()));
			newborn.setSex(((String) sexBox.getSelectedItem()).charAt(0));
			newborn.setDeliveryType(deliveryTypeBox.getSelectedIndex() <= 0 ? null : (DeliveryType) deliveryTypeBox.getSelectedItem());
			newborn.setDeliveryResultType(
							deliveryResultTypeBox.getSelectedIndex() <= 0 ? null : (DeliveryResultType) deliveryResultTypeBox.getSelectedItem());
			newborn.setWeight((float) (double) (Double) weightSpinner.getValue());
			newborn.setHeight((float) (double) (Double) heightSpinner.getValue());
			newborn.setHeadCircumference((Double) headCircumferenceSpinner.getValue());
			newborn.setChestCircumference((Double) chestCircumferenceSpinner.getValue());
			newborn.setArmCircumference((Double) armCircumferenceSpinner.getValue());
			newborn.setComplications(emptyToNull(complicationsField.getText()));
			newborn.setCongenitalMalformation(emptyToNull(malformationField.getText()));
			newborn.setFeedingMode(feedingModeBox.getSelectedIndex() <= 0 ? null : (NewbornFeedingMode) feedingModeBox.getSelectedItem());
			newborn.setHivExposed(hivExposedCheckBox.isSelected());
			return newborn;
		}

		private static String nullToEmpty(String value) {
			return value == null ? "" : value;
		}

		private static String emptyToNull(String value) {
			return value == null || value.isBlank() ? null : value.trim();
		}
	}
}
