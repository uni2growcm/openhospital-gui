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
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.hivchildfollowup.manager.HivExposedChildBrowserManager;
import org.isf.hivchildfollowup.model.HivExposedChild;
import org.isf.hivchildfollowup.model.HivExposedChildStatus;
import org.isf.hivchildfollowup.model.HivExposedChildVisit;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.pregnancy.manager.PregnancyDeliveryBrowserManager;
import org.isf.pregnancy.model.NewbornFeedingMode;
import org.isf.pregnancy.model.PregnancyDelivery;
import org.isf.pregnancy.model.PregnancyNewborn;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextArea;

/**
 * PTME follow-up enrollment form for a child born to an HIV-positive mother ({@link Patient}): ARV/CTX
 * prophylaxis, feeding mode, final outcome, and the history of follow-up visits (6 weeks / 9 months / 18
 * months), each with a weight and an HIV test result. Reachable both from the main menu
 * ({@link HivExposedChildBrowser}) and as a shortcut from the CPN screen ({@code org.isf.pregnancy.gui.CpnEdit}).
 */
public class HivExposedChildEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final Patient motherPatient;
	private final HivExposedChild child;
	private final boolean insert;

	private final HivExposedChildBrowserManager manager = Context.getApplicationContext().getBean(HivExposedChildBrowserManager.class);
	private final PregnancyDeliveryBrowserManager deliveryManager = Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);

	private JComboBox<Object> newbornBox;
	private JTextField childNameField;
	private GoodDateChooser dateOfBirthChooser;
	private JTextField motherHivStatusField;
	private JCheckBox arvGivenCheckBox;
	private JTextField arvRegimenField;
	private JCheckBox ctxGivenCheckBox;
	private JComboBox<Object> feedingModeBox;
	private JComboBox<HivExposedChildStatus> statusBox;
	private VoLimitedTextArea noteTextArea;

	private final List<HivExposedChildVisit> visits;
	private JTable visitsTable;
	private VisitsTableModel visitsTableModel;

	private Runnable onSave;

	public HivExposedChildEdit(JFrame owner, Patient motherPatient, HivExposedChild child) {
		super(owner, true);
		this.motherPatient = motherPatient;
		this.insert = child == null;
		this.child = child == null ? new HivExposedChild(motherPatient, LocalDate.now()) : child;
		this.visits = new ArrayList<>(this.child.getVisits());
		initialize();
	}

	/**
	 * Optional callback invoked after a successful save, used by {@link HivExposedChildBrowser} to refresh its list.
	 */
	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.hivchildfollowup.hivexposedchildedit.title") + " - " + motherPatient.getFirstName() + ' '
				+ motherPatient.getSecondName());
		JPanel content = new JPanel(new BorderLayout());
		content.add(getFormPanel(), BorderLayout.NORTH);
		content.add(getVisitsPanel(), BorderLayout.CENTER);
		content.add(getButtonPanel(), BorderLayout.SOUTH);
		setContentPane(content);
		setSize(700, 620);
		setLocationRelativeTo(null);
	}

	private JPanel getFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hivchildfollowup.hivexposedchild.txt")));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		newbornBox = new JComboBox<>();
		newbornBox.addItem(MessageBundle.getMessage("angal.hivchildfollowup.nonewborn.txt"));
		List<PregnancyNewborn> hivExposedNewborns = getHivExposedNewbornsOfMother();
		for (PregnancyNewborn newborn : hivExposedNewborns) {
			newbornBox.addItem(newborn);
			if (child.getNewborn() != null && child.getNewborn().getId() == newborn.getId()) {
				newbornBox.setSelectedItem(newborn);
			}
		}
		newbornBox.addActionListener(e -> {
			Object selected = newbornBox.getSelectedItem();
			if (selected instanceof PregnancyNewborn) {
				PregnancyNewborn newborn = (PregnancyNewborn) selected;
				if (childNameField.getText().isBlank() && newborn.getChildName() != null) {
					childNameField.setText(newborn.getChildName());
				}
				if (newborn.getDelivery() != null && newborn.getDelivery().getDeliveryDate() != null) {
					dateOfBirthChooser.setDate(newborn.getDelivery().getDeliveryDate().toLocalDate());
				}
			}
		});
		addRow(panel, gbc, row++, "angal.hivchildfollowup.linkedbirth.txt", newbornBox);

		childNameField = new JTextField(child.getChildName() == null ? "" : child.getChildName());
		addRow(panel, gbc, row++, "angal.cpn.childname.txt", childNameField);

		dateOfBirthChooser = new GoodDateChooser(child.getDateOfBirth() == null ? LocalDate.now() : child.getDateOfBirth(), false, false);
		addRow(panel, gbc, row++, "angal.hivchildfollowup.dateofbirth.txt", dateOfBirthChooser);

		motherHivStatusField = new JTextField(child.getMotherHivStatus() == null ? "" : child.getMotherHivStatus());
		addRow(panel, gbc, row++, "angal.hivchildfollowup.motherhivstatus.txt", motherHivStatusField);

		arvGivenCheckBox = new JCheckBox(MessageBundle.getMessage("angal.hivchildfollowup.arvprophylaxisgiven.txt"), child.isArvProphylaxisGiven());
		addRow(panel, gbc, row++, "angal.hivchildfollowup.arvprophylaxis.txt", arvGivenCheckBox);

		arvRegimenField = new JTextField(child.getArvRegimen() == null ? "" : child.getArvRegimen());
		addRow(panel, gbc, row++, "angal.hivchildfollowup.arvregimen.txt", arvRegimenField);

		ctxGivenCheckBox = new JCheckBox(MessageBundle.getMessage("angal.hivchildfollowup.ctxprophylaxisgiven.txt"), child.isCtxProphylaxisGiven());
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		panel.add(ctxGivenCheckBox, gbc);
		gbc.gridwidth = 1;
		row++;

		feedingModeBox = new JComboBox<>();
		feedingModeBox.addItem("");
		feedingModeBox.addItem(NewbornFeedingMode.EXCLUSIVE_BREASTFEEDING);
		feedingModeBox.addItem(NewbornFeedingMode.ARTIFICIAL);
		feedingModeBox.addItem(NewbornFeedingMode.MIXED);
		feedingModeBox.setSelectedItem(child.getFeedingMode() == null ? "" : child.getFeedingMode());
		addRow(panel, gbc, row++, "angal.cpn.feedingmode.txt", feedingModeBox);

		statusBox = new JComboBox<>(HivExposedChildStatus.values());
		statusBox.setSelectedItem(child.getFinalStatus() == null ? HivExposedChildStatus.ON_FOLLOWUP : child.getFinalStatus());
		addRow(panel, gbc, row++, "angal.hivchildfollowup.finalstatus.txt", statusBox);

		noteTextArea = new VoLimitedTextArea(255, 2, 20);
		noteTextArea.setText(child.getNote() == null ? "" : child.getNote());
		addRow(panel, gbc, row++, "angal.common.note.txt", new JScrollPane(noteTextArea));

		return panel;
	}

	/**
	 * Newborns of this mother whose delivery marked them {@link PregnancyNewborn#isHivExposed()}, so a
	 * follow-up enrollment can be linked back to the birth it originates from when one is known.
	 */
	private List<PregnancyNewborn> getHivExposedNewbornsOfMother() {
		List<PregnancyNewborn> result = new ArrayList<>();
		try {
			List<PregnancyDelivery> deliveries = deliveryManager.getByPatientId(motherPatient.getCode());
			for (PregnancyDelivery delivery : deliveries) {
				for (PregnancyNewborn newborn : delivery.getNewborns()) {
					if (newborn.isHivExposed()) {
						result.add(newborn);
					}
				}
			}
		} catch (OHServiceException ohServiceException) {
			// Best-effort: the linked-birth combo is a convenience, not a requirement — enrollment still works
			// with a manually entered child name and date of birth if this lookup fails.
		}
		return result;
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

	private JPanel getVisitsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.hivchildfollowup.visits.title")));

		visitsTableModel = new VisitsTableModel();
		visitsTable = new JTable(visitsTableModel);
		panel.add(new JScrollPane(visitsTable), BorderLayout.CENTER);

		JPanel visitsButtonPanel = new JPanel(new GridLayout(1, 0, 5, 5));
		JButton addVisitButton = new JButton(MessageBundle.getMessage("angal.hivchildfollowup.addvisit.btn"));
		addVisitButton.addActionListener(e -> {
			HivExposedChildVisit visit = new HivExposedChildVisit(child, LocalDate.now());
			HivExposedChildVisitDialog dialog = new HivExposedChildVisitDialog((JFrame) getOwner(), visit);
			dialog.setVisible(true);
			if (dialog.isSaved()) {
				visits.add(visit);
				visitsTableModel.fireTableDataChanged();
			}
		});
		visitsButtonPanel.add(addVisitButton);

		JButton removeVisitButton = new JButton(MessageBundle.getMessage("angal.hivchildfollowup.removevisit.btn"));
		removeVisitButton.addActionListener(e -> {
			int selectedRow = visitsTable.getSelectedRow();
			if (selectedRow < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
				return;
			}
			visits.remove(selectedRow);
			visitsTableModel.fireTableDataChanged();
		});
		visitsButtonPanel.add(removeVisitButton);

		panel.add(visitsButtonPanel, BorderLayout.SOUTH);
		return panel;
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
		child.setMotherPatient(motherPatient);
		Object selectedNewborn = newbornBox.getSelectedItem();
		child.setNewborn(selectedNewborn instanceof PregnancyNewborn ? (PregnancyNewborn) selectedNewborn : null);
		child.setChildName(childNameField.getText());
		child.setDateOfBirth(dateOfBirthChooser.getDate());
		child.setMotherHivStatus(motherHivStatusField.getText());
		child.setArvProphylaxisGiven(arvGivenCheckBox.isSelected());
		child.setArvRegimen(arvRegimenField.getText());
		child.setCtxProphylaxisGiven(ctxGivenCheckBox.isSelected());
		Object feedingMode = feedingModeBox.getSelectedItem();
		child.setFeedingMode(feedingMode instanceof NewbornFeedingMode ? (NewbornFeedingMode) feedingMode : null);
		child.setFinalStatus((HivExposedChildStatus) statusBox.getSelectedItem());
		child.setNote(noteTextArea.getText());
		child.getVisits().clear();
		child.getVisits().addAll(visits);

		try {
			manager.saveOrUpdate(child);
			if (onSave != null) {
				onSave.run();
			}
			dispose();
		} catch (OHServiceException ohServiceException) {
			OHServiceExceptionUtil.showMessages(ohServiceException);
		}
	}

	private class VisitsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		private final String[] columns = {
				MessageBundle.getMessage("angal.hivchildfollowup.visitdate.txt"),
				MessageBundle.getMessage("angal.hivchildfollowup.ageatvisitmonths.txt"),
				MessageBundle.getMessage("angal.hivchildfollowup.weightkg.txt"),
				MessageBundle.getMessage("angal.hivchildfollowup.testtype.txt"),
				MessageBundle.getMessage("angal.hivchildfollowup.testresult.txt"),
				MessageBundle.getMessage("angal.familyplanning.nextappointment.txt")
		};

		@Override
		public int getRowCount() {
			return visits == null ? 0 : visits.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int row, int column) {
			HivExposedChildVisit visit = visits.get(row);
			switch (column) {
				case 0:
					return visit.getVisitDate() == null ? "" : visit.getVisitDate().format(dateFormatter);
				case 1:
					return visit.getAgeAtVisitMonths();
				case 2:
					return visit.getWeight();
				case 3:
					return visit.getTestType() == null ? "" : visit.getTestType();
				case 4:
					return visit.getTestResult() == null ? "" : visit.getTestResult();
				case 5:
					return visit.getNextAppointmentDate() == null ? "" : visit.getNextAppointmentDate().format(dateFormatter);
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
