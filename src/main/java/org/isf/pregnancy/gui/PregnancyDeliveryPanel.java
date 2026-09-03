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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

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
import org.isf.pregnancy.model.Pregnancy;
import org.isf.pregnancy.model.PregnancyDelivery;
import org.isf.pregnancy.model.PregnancyNewborn;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Reusable panel embedded in the delivery workflow: it shows the length of stay in nights (when linked to an
 * admission), the mother-level delivery information (delivery date, father identification, lochies,
 * counseling, family planning method chosen at discharge) and the newborns, each added on demand (up to
 * {@value PregnancyNewborn#MAX_CHILDREN}) with its own weight, height, head/chest/arm circumference,
 * complications, congenital malformation, feeding mode and HIV exposure flag.
 * <p>
 * A delivery can be recorded two ways, which this panel supports symmetrically:
 * <ul>
 * <li>{@link #loadFor(Admission)} / {@link #saveFor(Admission)} - from a ward admission (e.g. a maternity
 * stay), which also gives the computed length of stay; the delivery date itself is taken from the
 * admission's own legacy delivery-date field and is not editable here.</li>
 * <li>{@link #loadFor(Pregnancy)} / {@link #saveFor(Pregnancy)} - from the CPN module directly, without
 * requiring the mother to be hospitalized; the delivery date is entered on this panel and
 * {@link Pregnancy#getId()} must already be a saved (non-zero) id.</li>
 * </ul>
 */
public class PregnancyDeliveryPanel extends JPanel {

	private final PregnancyDeliveryBrowserManager deliveryManager = Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);
	private final DeliveryTypeBrowserManager deliveryTypeBrowserManager = Context.getApplicationContext().getBean(DeliveryTypeBrowserManager.class);
	private final DeliveryResultTypeBrowserManager deliveryResultTypeBrowserManager = Context.getApplicationContext()
					.getBean(DeliveryResultTypeBrowserManager.class);

	private List<DeliveryType> deliveryTypeList = new ArrayList<>();
	private List<DeliveryResultType> deliveryResultTypeList = new ArrayList<>();

	private JTextField nightsOfStayField;
	private GoodDateChooser deliveryDateChooser;
	private JComboBox<Object> lochiesBox;
	private JTextField counselingField;
	private JTextField familyPlanningMethodField;

	private JTextField fatherNameField;
	private JTextField fatherOccupationField;
	private JTextField fatherResidenceField;
	private JTextField fatherBirthPlaceField;
	private JSpinner fatherAgeSpinner;
	private JTextField fatherCniField;

	private JPanel newbornsListPanel;
	private JButton addNewbornButton;
	private JButton printCertificateButton;
	private final List<NewbornRowPanel> newbornRows = new ArrayList<>();
	private final BirthDocumentPrinter birthDocumentPrinter = new BirthDocumentPrinter();

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

		deliveryDateChooser = new GoodDateChooser(LocalDate.now(), false, false);
		addField(motherPanel, c, 1, 0, "angal.cpn.deliverydate.txt", deliveryDateChooser);

		lochiesBox = new JComboBox<>();
		lochiesBox.addItem("");
		lochiesBox.addItem(Lochies.NORMAL);
		lochiesBox.addItem(Lochies.ABUNDANT);
		lochiesBox.addItem(Lochies.ABSENT);
		addField(motherPanel, c, 0, 1, "angal.cpn.lochies.txt", lochiesBox);

		counselingField = new JTextField(20);
		addField(motherPanel, c, 1, 1, "angal.cpn.counseling.txt", counselingField);

		familyPlanningMethodField = new JTextField(20);
		addField(motherPanel, c, 0, 2, "angal.cpn.familyplanningmethod.txt", familyPlanningMethodField);

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

		newbornsListPanel = new JPanel();
		newbornsListPanel.setLayout(new BoxLayout(newbornsListPanel, BoxLayout.Y_AXIS));

		addNewbornButton = new JButton(MessageBundle.getMessage("angal.cpn.addnewborn.btn"));
		addNewbornButton.addActionListener(e -> addNewborn());

		printCertificateButton = new JButton(MessageBundle.getMessage("angal.cpn.printcertificateofdeclaration.txt"));
		printCertificateButton.setEnabled(false);
		printCertificateButton.addActionListener(e -> birthDocumentPrinter.printCertificateOfDeclaration(currentDelivery.getId()));

		JPanel newbornsButtonPanel = new JPanel();
		newbornsButtonPanel.add(addNewbornButton);
		newbornsButtonPanel.add(printCertificateButton);

		JPanel newbornsPanel = new JPanel(new BorderLayout());
		newbornsPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.cpn.newborns.title")));
		newbornsPanel.add(newbornsListPanel, BorderLayout.CENTER);
		newbornsPanel.add(newbornsButtonPanel, BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(headerPanel);
		topPanel.add(newbornsPanel);

		add(new JScrollPane(topPanel), BorderLayout.CENTER);
	}

	private void addField(JPanel panel, GridBagConstraints template, int col, int row, String labelKey, Component field) {
		GridBagConstraints labelConstraints = (GridBagConstraints) template.clone();
		labelConstraints.gridx = col * 2;
		labelConstraints.gridy = row;
		panel.add(new JLabel(MessageBundle.getMessage(labelKey)), labelConstraints);

		GridBagConstraints fieldConstraints = (GridBagConstraints) template.clone();
		fieldConstraints.gridx = col * 2 + 1;
		fieldConstraints.gridy = row;
		panel.add(field, fieldConstraints);
	}

	/*
	 * ----------------------------------------------------------------
	 * Newborns : added on demand (no pre-defined slots), up to PregnancyNewborn.MAX_CHILDREN.
	 * ----------------------------------------------------------------
	 */
	private void addNewborn() {
		if (newbornRows.size() >= PregnancyNewborn.MAX_CHILDREN) {
			return;
		}
		NewbornRowPanel row = new NewbornRowPanel(deliveryTypeList, deliveryResultTypeList, this::removeNewborn);
		newbornRows.add(row);
		newbornsListPanel.add(row);
		renumberNewborns();
		newbornsListPanel.revalidate();
		newbornsListPanel.repaint();
	}

	private void removeNewborn(NewbornRowPanel row) {
		newbornRows.remove(row);
		newbornsListPanel.remove(row);
		renumberNewborns();
		newbornsListPanel.revalidate();
		newbornsListPanel.repaint();
	}

	private void renumberNewborns() {
		for (int i = 0; i < newbornRows.size(); i++) {
			newbornRows.get(i).setDisplayNumber(i + 1);
		}
		addNewbornButton.setEnabled(newbornRows.size() < PregnancyNewborn.MAX_CHILDREN);
		updatePrintCertificateButtonState();
	}

	private void clearNewbornRows() {
		newbornsListPanel.removeAll();
		newbornRows.clear();
		addNewbornButton.setEnabled(true);
		updatePrintCertificateButtonState();
	}

	private void updatePrintCertificateButtonState() {
		printCertificateButton.setEnabled(currentDelivery != null && currentDelivery.getId() > 0);
	}

	/**
	 * Loads (or resets, for a brand new admission) the delivery data for the given admission. Safe to call
	 * repeatedly, e.g. once when the delivery tab is built and again right after the admission has been
	 * persisted, so that the length of stay reflects the saved discharge date. The delivery date is taken
	 * from the admission's own field and is not editable on this panel in this mode.
	 */
	public void loadFor(Admission admission) {
		nightsOfStayField.setText(String.valueOf(deliveryManager.computeNightsOfStay(admission)));
		deliveryDateChooser.setEnabled(false);

		PregnancyDelivery delivery = null;
		if (admission.getId() > 0) {
			try {
				delivery = deliveryManager.getCurrentForAdmission(admission.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		loadDeliveryData(delivery, admission.getDeliveryDate());
	}

	/**
	 * Loads (or resets, for a pregnancy with no delivery recorded yet) the delivery data for the given
	 * pregnancy, independently of any hospital admission. {@link Pregnancy#getId()} does not need to be a
	 * saved id yet - in that case the panel simply starts blank.
	 */
	public void loadFor(Pregnancy pregnancy) {
		nightsOfStayField.setText("");
		deliveryDateChooser.setEnabled(true);

		PregnancyDelivery delivery = null;
		if (pregnancy.getId() > 0) {
			try {
				delivery = deliveryManager.getCurrentForPregnancy(pregnancy.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		loadDeliveryData(delivery, null);
	}

	private void loadDeliveryData(PregnancyDelivery delivery, LocalDateTime fallbackDeliveryDate) {
		currentDelivery = delivery;
		clearNewbornRows();

		if (currentDelivery == null) {
			deliveryDateChooser.setDate(fallbackDeliveryDate != null ? fallbackDeliveryDate.toLocalDate() : LocalDate.now());
			lochiesBox.setSelectedIndex(0);
			counselingField.setText("");
			familyPlanningMethodField.setText("");
			fatherNameField.setText("");
			fatherOccupationField.setText("");
			fatherResidenceField.setText("");
			fatherBirthPlaceField.setText("");
			fatherAgeSpinner.setValue(0);
			fatherCniField.setText("");
			return;
		}

		deliveryDateChooser.setDate(currentDelivery.getDeliveryDate() == null ? null : currentDelivery.getDeliveryDate().toLocalDate());
		lochiesBox.setSelectedItem(currentDelivery.getLochies() == null ? "" : currentDelivery.getLochies());
		counselingField.setText(nullToEmpty(currentDelivery.getCounseling()));
		familyPlanningMethodField.setText(nullToEmpty(currentDelivery.getFamilyPlanningMethodChosen()));
		fatherNameField.setText(nullToEmpty(currentDelivery.getFatherName()));
		fatherOccupationField.setText(nullToEmpty(currentDelivery.getFatherOccupation()));
		fatherResidenceField.setText(nullToEmpty(currentDelivery.getFatherResidence()));
		fatherBirthPlaceField.setText(nullToEmpty(currentDelivery.getFatherBirthPlace()));
		fatherAgeSpinner.setValue(currentDelivery.getFatherAge() == null ? 0 : currentDelivery.getFatherAge());
		fatherCniField.setText(currentDelivery.getFatherCni() == null ? "" : String.valueOf(currentDelivery.getFatherCni()));

		for (PregnancyNewborn newborn : currentDelivery.getNewborns()) {
			NewbornRowPanel row = new NewbornRowPanel(deliveryTypeList, deliveryResultTypeList, this::removeNewborn);
			row.populate(newborn);
			newbornRows.add(row);
			newbornsListPanel.add(row);
		}
		renumberNewborns();
		newbornsListPanel.revalidate();
		newbornsListPanel.repaint();
	}

	/**
	 * Persists the delivery data (mother-level fields and every newborn row added) for an admission that has
	 * already been saved (i.e. {@code admission.getId() > 0}). Does nothing if no newborn was added and no
	 * mother-level field has been filled in, so that admissions unrelated to a pregnancy are not affected.
	 */
	public void saveFor(Admission admission) {
		if (!hasAnyDeliveryDataEntered() && currentDelivery == null) {
			return;
		}
		PregnancyDelivery delivery = currentDelivery != null ? currentDelivery : new PregnancyDelivery(admission);
		delivery.setAdmission(admission);
		populateDeliveryFields(delivery, admission.getDeliveryDate());
		persist(delivery);
	}

	/**
	 * Persists the delivery data for the given pregnancy, without requiring any hospital admission.
	 * {@code pregnancy.getId()} must already be a saved (non-zero) id - the caller is responsible for saving
	 * the pregnancy itself first. Does nothing (and returns {@code false}) if no newborn was added and no
	 * mother-level field has been filled in.
	 *
	 * @return {@code true} if the delivery was actually persisted.
	 */
	public boolean saveFor(Pregnancy pregnancy) {
		if (pregnancy.getId() <= 0) {
			MessageDialog.error(null, "angal.cpn.pleasesavethepregnancyfirst.msg");
			return false;
		}
		if (!hasAnyDeliveryDataEntered() && currentDelivery == null) {
			return false;
		}
		PregnancyDelivery delivery = currentDelivery != null ? currentDelivery : new PregnancyDelivery(pregnancy);
		delivery.setPregnancy(pregnancy);
		populateDeliveryFields(delivery, deliveryDateChooser.getDateStartOfDay());
		persist(delivery);
		return true;
	}

	private boolean hasAnyDeliveryDataEntered() {
		return !newbornRows.isEmpty()
						|| lochiesBox.getSelectedIndex() > 0
						|| !counselingField.getText().isBlank()
						|| !familyPlanningMethodField.getText().isBlank()
						|| !fatherNameField.getText().isBlank();
	}

	private void populateDeliveryFields(PregnancyDelivery delivery, LocalDateTime deliveryDate) {
		delivery.setDeliveryDate(deliveryDate);
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
		for (int i = 0; i < newbornRows.size(); i++) {
			newborns.add(newbornRows.get(i).toNewborn(delivery, i + 1));
		}
		delivery.getNewborns().clear();
		delivery.getNewborns().addAll(newborns);
	}

	private void persist(PregnancyDelivery delivery) {
		try {
			currentDelivery = deliveryManager.saveOrUpdate(delivery);
			List<PregnancyNewborn> saved = currentDelivery.getNewborns();
			for (int i = 0; i < newbornRows.size(); i++) {
				newbornRows.get(i).setNewbornId(i < saved.size() ? saved.get(i).getId() : 0);
			}
			updatePrintCertificateButtonState();
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
	 * One newborn's fields, added and removed on demand from the newborns list (instead of a fixed pool of
	 * slots), backed by a {@link PregnancyNewborn}. Its displayed number and persisted
	 * {@code PNB_CHILD_NUMBER} both simply reflect its current position in that list.
	 */
	private static final class NewbornRowPanel extends JPanel {

		private final TitledBorder titledBorder;
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
		private final JButton removeButton;
		private final JButton declarationButton;
		private final BirthDocumentPrinter birthDocumentPrinter = new BirthDocumentPrinter();

		private int newbornId;

		NewbornRowPanel(List<DeliveryType> deliveryTypeList, List<DeliveryResultType> deliveryResultTypeList,
						java.util.function.Consumer<NewbornRowPanel> onRemove) {
			setLayout(new GridBagLayout());
			titledBorder = BorderFactory.createTitledBorder(MessageBundle.formatMessage("angal.cpn.newborn.fmt.txt", 1));
			setBorder(titledBorder);
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(2, 4, 2, 4);
			c.fill = GridBagConstraints.HORIZONTAL;

			childNameField = new JTextField(15);
			addLabeledField(c, 0, 0, "angal.cpn.childname.txt", childNameField);

			sexBox = new JComboBox<>(new String[] { "F", "M" });
			addLabeledField(c, 1, 0, "angal.cpn.sex.txt", sexBox);

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
			c.gridx = 2;
			c.gridy = 4;
			add(hivExposedCheckBox, c);

			removeButton = new JButton(MessageBundle.getMessage("angal.cpn.removenewborn.btn"));
			removeButton.addActionListener(e -> onRemove.accept(this));
			c.gridx = 3;
			c.gridy = 4;
			add(removeButton, c);

			declarationButton = new JButton(MessageBundle.getMessage("angal.cpn.printdeclarationofbirth.txt"));
			declarationButton.addActionListener(e -> birthDocumentPrinter.printDeclarationOfBirth(newbornId));
			declarationButton.setEnabled(false);
			c.gridx = 4;
			c.gridy = 4;
			add(declarationButton, c);
		}

		private void addLabeledField(GridBagConstraints template, int col, int row, String labelKey, Component field) {
			GridBagConstraints labelConstraints = (GridBagConstraints) template.clone();
			labelConstraints.gridx = col * 2;
			labelConstraints.gridy = row;
			add(new JLabel(MessageBundle.getMessage(labelKey)), labelConstraints);

			GridBagConstraints fieldConstraints = (GridBagConstraints) template.clone();
			fieldConstraints.gridx = col * 2 + 1;
			fieldConstraints.gridy = row;
			add(field, fieldConstraints);
		}

		void setDisplayNumber(int number) {
			titledBorder.setTitle(MessageBundle.formatMessage("angal.cpn.newborn.fmt.txt", number));
			repaint();
		}

		void setNewbornId(int newbornId) {
			this.newbornId = newbornId;
			declarationButton.setEnabled(newbornId > 0);
		}

		int getNewbornId() {
			return newbornId;
		}

		void populate(PregnancyNewborn newborn) {
			setNewbornId(newborn.getId());
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

		PregnancyNewborn toNewborn(PregnancyDelivery delivery, int childNumber) {
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
