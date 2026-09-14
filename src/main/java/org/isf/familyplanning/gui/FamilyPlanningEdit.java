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
package org.isf.familyplanning.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.isf.familyplanning.manager.FamilyPlanningBrowserManager;
import org.isf.familyplanning.model.FamilyPlanningMethod;
import org.isf.familyplanning.model.FamilyPlanningReason;
import org.isf.familyplanning.model.FamilyPlanningRecord;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextArea;

/**
 * Family planning consultation form: records the contraceptive method chosen/discussed for a
 * {@link Patient}, the reason for the visit, counseling given and the next appointment. Reachable both
 * from the main menu ({@link FamilyPlanningBrowser}) and as a shortcut from the CPN screen
 * ({@code org.isf.pregnancy.gui.CpnEdit}).
 */
public class FamilyPlanningEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final Patient patient;
	private final FamilyPlanningRecord record;
	private final boolean insert;

	private final FamilyPlanningBrowserManager manager = Context.getApplicationContext().getBean(FamilyPlanningBrowserManager.class);

	private GoodDateChooser visitDateChooser;
	private JComboBox<FamilyPlanningMethod> methodBox;
	private JComboBox<FamilyPlanningReason> reasonBox;
	private JComboBox<FamilyPlanningMethod> previousMethodBox;
	private VoLimitedTextArea sideEffectsTextArea;
	private JCheckBox counselingCheckBox;
	private JSpinner paritySpinner;
	private GoodDateChooser nextAppointmentChooser;
	private VoLimitedTextArea noteTextArea;

	private Runnable onSave;

	public FamilyPlanningEdit(JFrame owner, Patient patient, FamilyPlanningRecord record) {
		super(owner, true);
		this.patient = patient;
		this.insert = record == null;
		this.record = record == null ? new FamilyPlanningRecord(patient, LocalDate.now(), FamilyPlanningMethod.NONE, FamilyPlanningReason.NEW_ADOPTION)
				: record;
		initialize();
	}

	/**
	 * Optional callback invoked after a successful save, used by {@link FamilyPlanningBrowser} to refresh its list.
	 */
	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.familyplanning.familyplanningedit.title") + " - " + patient.getFirstName() + ' ' + patient.getSecondName());
		JPanel content = new JPanel(new BorderLayout());
		content.add(getFormPanel(), BorderLayout.CENTER);
		content.add(getButtonPanel(), BorderLayout.SOUTH);
		setContentPane(content);
		setSize(520, 480);
		setLocationRelativeTo(null);
	}

	private JPanel getFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.familyplanning.familyplanning.txt")));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		visitDateChooser = new GoodDateChooser(record.getVisitDate() == null ? LocalDate.now() : record.getVisitDate(), false, false);
		addRow(panel, gbc, row++, "angal.familyplanning.visitdate.txt", visitDateChooser);

		methodBox = new JComboBox<>(FamilyPlanningMethod.values());
		methodBox.setSelectedItem(record.getMethod() == null ? FamilyPlanningMethod.NONE : record.getMethod());
		addRow(panel, gbc, row++, "angal.familyplanning.method.txt", methodBox);

		reasonBox = new JComboBox<>(FamilyPlanningReason.values());
		reasonBox.setSelectedItem(record.getReason() == null ? FamilyPlanningReason.NEW_ADOPTION : record.getReason());
		addRow(panel, gbc, row++, "angal.familyplanning.reason.txt", reasonBox);

		previousMethodBox = new JComboBox<>(FamilyPlanningMethod.values());
		previousMethodBox.setSelectedItem(record.getPreviousMethod() == null ? FamilyPlanningMethod.NONE : record.getPreviousMethod());
		addRow(panel, gbc, row++, "angal.familyplanning.previousmethod.txt", previousMethodBox);

		paritySpinner = new JSpinner(new SpinnerNumberModel(record.getParity() == null ? 0 : record.getParity(), 0, 30, 1));
		addRow(panel, gbc, row++, "angal.familyplanning.parity.txt", paritySpinner);

		counselingCheckBox = new JCheckBox(MessageBundle.getMessage("angal.familyplanning.counselinggiven.txt"), record.isCounselingGiven());
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		panel.add(counselingCheckBox, gbc);
		gbc.gridwidth = 1;
		row++;

		nextAppointmentChooser = new GoodDateChooser(record.getNextAppointmentDate(), false, true);
		addRow(panel, gbc, row++, "angal.familyplanning.nextappointment.txt", nextAppointmentChooser);

		sideEffectsTextArea = new VoLimitedTextArea(255, 3, 20);
		sideEffectsTextArea.setText(record.getSideEffects() == null ? "" : record.getSideEffects());
		addRow(panel, gbc, row++, "angal.familyplanning.sideeffects.txt", new JScrollPane(sideEffectsTextArea));

		noteTextArea = new VoLimitedTextArea(255, 3, 20);
		noteTextArea.setText(record.getNote() == null ? "" : record.getNote());
		addRow(panel, gbc, row++, "angal.common.note.txt", new JScrollPane(noteTextArea));

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

		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.addActionListener(e -> dispose());
		buttonPanel.add(closeButton);

		return buttonPanel;
	}

	private void save() {
		record.setPatient(patient);
		record.setVisitDate(visitDateChooser.getDate());
		record.setMethod((FamilyPlanningMethod) methodBox.getSelectedItem());
		record.setReason((FamilyPlanningReason) reasonBox.getSelectedItem());
		FamilyPlanningMethod previous = (FamilyPlanningMethod) previousMethodBox.getSelectedItem();
		record.setPreviousMethod(previous == FamilyPlanningMethod.NONE ? null : previous);
		record.setParity((Integer) paritySpinner.getValue());
		record.setCounselingGiven(counselingCheckBox.isSelected());
		record.setNextAppointmentDate(nextAppointmentChooser.getDate());
		record.setSideEffects(sideEffectsTextArea.getText());
		record.setNote(noteTextArea.getText());

		try {
			if (insert) {
				manager.newRecord(record);
			} else {
				manager.updateRecord(record);
			}
			if (onSave != null) {
				onSave.run();
			}
			dispose();
		} catch (OHServiceException ohServiceException) {
			OHServiceExceptionUtil.showMessages(ohServiceException);
		}
	}
}
