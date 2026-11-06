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
package org.isf.reductionplan.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.reductionplan.manager.PatientReductionPlanManager;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.utils.exception.OHServiceException;

/**
 * Dialog letting the user tick the reduction plans to assign to a patient.
 */
public final class ReductionPlanChooser {

	private ReductionPlanChooser() {
	}

	/**
	 * @param parent the parent component
	 * @param title the dialog title
	 * @param available the plans that can be assigned
	 * @param current the plans currently assigned (always displayed, ticked)
	 * @param manager the manager used to display the number of patients of each plan
	 * @return the ticked plans, {@code null} if the dialog was cancelled
	 * @throws OHServiceException if an error occurs while retrieving data
	 */
	public static List<ReductionPlan> choose(Component parent, String title, List<ReductionPlan> available, List<ReductionPlan> current,
		PatientReductionPlanManager manager) throws OHServiceException {
		Set<ReductionPlan> plans = new LinkedHashSet<>(current);
		plans.addAll(available);
		List<ReductionPlan> rows = new ArrayList<>(plans);

		String[] columns = {
			MessageBundle.getMessage("angal.common.select"),
			MessageBundle.getMessage("angal.common.description.txt"),
			MessageBundle.getMessage("angal.reductionplan.expirationdate.col"),
			MessageBundle.getMessage("angal.reductionplan.patients.col")
		};
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnIndex == 0 ? Boolean.class : String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}
		};
		LocalDateTime now = LocalDateTime.now();
		for (ReductionPlan plan : rows) {
			LocalDateTime expiration = plan.getExpirationDate();
			String expirationText = expiration == null ? "" : expiration.toLocalDate().toString();
			if (plan.isExpiredAt(now)) {
				expirationText += " (" + MessageBundle.getMessage("angal.reductionplan.expired.txt") + ")";
			}
			long count = manager.getPatientCount(plan);
			String patients = plan.getMaxPatients() == null ? String.valueOf(count) : count + " / " + plan.getMaxPatients();
			model.addRow(new Object[] { current.contains(plan), plan.getDescription(), expirationText, patients });
		}

		Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
		if (parent instanceof Window window) {
			owner = window;
		}
		JDialog dialog = new JDialog(owner, title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new BorderLayout(10, 10));

		JTable table = new JTable(model);
		table.getColumnModel().getColumn(0).setMaxWidth(70);
		table.getColumnModel().getColumn(1).setPreferredWidth(220);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(520, 220));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		List<ReductionPlan> result = new ArrayList<>();
		boolean[] confirmed = { false };
		JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		okButton.addActionListener(e -> {
			if (table.isEditing()) {
				table.getCellEditor().stopCellEditing();
			}
			for (int i = 0; i < model.getRowCount(); i++) {
				if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
					result.add(rows.get(i));
				}
			}
			confirmed[0] = true;
			dialog.dispose();
		});
		cancelButton.addActionListener(e -> dialog.dispose());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		dialog.add(mainPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return confirmed[0] ? result : null;
	}
}
