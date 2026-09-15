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
package org.isf.hivchildfollowup.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.isf.generaldata.MessageBundle;
import org.isf.hivchildfollowup.model.HivExposedChildVisit;
import org.isf.hivchildfollowup.model.HivTestResult;
import org.isf.hivchildfollowup.model.HivTestType;
import org.isf.utils.jobjects.GoodDateChooser;

/**
 * Small modal dialog to enter one {@link HivExposedChildVisit} of the PTME follow-up calendar (typically at
 * 6 weeks, 9 months or 18 months of age), used by {@link HivExposedChildEdit} to add a row to the visit
 * history table.
 */
public class HivExposedChildVisitDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final HivExposedChildVisit visit;
	private boolean saved;

	private GoodDateChooser visitDateChooser;
	private JSpinner ageMonthsSpinner;
	private JSpinner weightSpinner;
	private JComboBox<Object> testTypeBox;
	private JComboBox<Object> testResultBox;
	private GoodDateChooser nextAppointmentChooser;
	private JTextField noteField;

	public HivExposedChildVisitDialog(JFrame owner, HivExposedChildVisit visit) {
		super(owner, true);
		this.visit = visit;
		initialize();
	}

	public boolean isSaved() {
		return saved;
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.hivchildfollowup.visit.title"));
		JPanel content = new JPanel(new BorderLayout());
		content.add(getFormPanel(), BorderLayout.CENTER);
		content.add(getButtonPanel(), BorderLayout.SOUTH);
		setContentPane(content);
		setSize(420, 340);
		setLocationRelativeTo(null);
	}

	private JPanel getFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hivchildfollowup.visit.title")));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		visitDateChooser = new GoodDateChooser(visit.getVisitDate() == null ? LocalDate.now() : visit.getVisitDate(), false, false);
		addRow(panel, gbc, row++, "angal.hivchildfollowup.visitdate.txt", visitDateChooser);

		ageMonthsSpinner = new JSpinner(new SpinnerNumberModel(visit.getAgeAtVisitMonths() == null ? 0 : visit.getAgeAtVisitMonths(), 0, 240, 1));
		addRow(panel, gbc, row++, "angal.hivchildfollowup.ageatvisitmonths.txt", ageMonthsSpinner);

		weightSpinner = new JSpinner(new SpinnerNumberModel(visit.getWeight() == null ? 0.0 : visit.getWeight(), 0.0, 100.0, 0.1));
		addRow(panel, gbc, row++, "angal.hivchildfollowup.weightkg.txt", weightSpinner);

		testTypeBox = new JComboBox<>();
		testTypeBox.addItem("");
		testTypeBox.addItem(HivTestType.PCR);
		testTypeBox.addItem(HivTestType.SEROLOGY);
		testTypeBox.setSelectedItem(visit.getTestType() == null ? "" : visit.getTestType());
		addRow(panel, gbc, row++, "angal.hivchildfollowup.testtype.txt", testTypeBox);

		testResultBox = new JComboBox<>();
		testResultBox.addItem("");
		testResultBox.addItem(HivTestResult.NEGATIVE);
		testResultBox.addItem(HivTestResult.POSITIVE);
		testResultBox.addItem(HivTestResult.PENDING);
		testResultBox.setSelectedItem(visit.getTestResult() == null ? "" : visit.getTestResult());
		addRow(panel, gbc, row++, "angal.hivchildfollowup.testresult.txt", testResultBox);

		nextAppointmentChooser = new GoodDateChooser(visit.getNextAppointmentDate(), false, true);
		addRow(panel, gbc, row++, "angal.familyplanning.nextappointment.txt", nextAppointmentChooser);

		noteField = new JTextField(visit.getNote() == null ? "" : visit.getNote());
		addRow(panel, gbc, row++, "angal.common.note.txt", noteField);

		return panel;
	}

	private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelKey, java.awt.Component field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		panel.add(new JLabel(MessageBundle.getMessage(labelKey)), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(field, gbc);
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();

		JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.addActionListener(e -> save());
		buttonPanel.add(saveButton);

		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.addActionListener(e -> dispose());
		buttonPanel.add(cancelButton);

		return buttonPanel;
	}

	private void save() {
		visit.setVisitDate(visitDateChooser.getDate());
		visit.setAgeAtVisitMonths((Integer) ageMonthsSpinner.getValue());
		visit.setWeight(((Number) weightSpinner.getValue()).floatValue());
		Object testType = testTypeBox.getSelectedItem();
		visit.setTestType(testType instanceof HivTestType ? (HivTestType) testType : null);
		Object testResult = testResultBox.getSelectedItem();
		visit.setTestResult(testResult instanceof HivTestResult ? (HivTestResult) testResult : null);
		visit.setNextAppointmentDate(nextAppointmentChooser.getDate());
		visit.setNote(noteField.getText());
		saved = true;
		dispose();
	}
}
