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
package org.isf.maternity.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.FamilyPlanningBrowserManager;
import org.isf.maternity.model.FamilyPlanning;
import org.isf.maternity.model.FamilyPlanningVisit;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.table.TableSorter;

public class FamilyPlanningPatList extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final Patient patient;
	private final FamilyPlanningBrowserManager fpManager;
	private List<FamilyPlanning> fpList = new ArrayList<>();

	private JTable jTableData;
	private FPBrowserModel model;
	private TableSorter sorter;
	private final OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	private final String[] columns = {
			MessageBundle.getMessage("angal.familyplanning.startdate.col").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.method.col").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.status.col").toUpperCase(),
			MessageBundle.getMessage("angal.familyplanning.notes.label").toUpperCase()
	};

	private final int[] columnWidths = { 130, 200, 120, 250 };

	public FamilyPlanningPatList(Patient patient) {
		this.patient = patient;
		this.fpManager = Context.getApplicationContext().getBean(FamilyPlanningBrowserManager.class);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		loadData();

		model = new FPBrowserModel();
		sorter = new TableSorter(model);
		jTableData = new JTable(sorter);

		jTableData.setDefaultRenderer(Object.class, cellRenderer);
		jTableData.setDefaultRenderer(Double.class, cellRenderer);

		jTableData.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				if (itsRow >= 0 && itsRow < aTable.getModel().getRowCount()) {
					cellRenderer.setHoveredRow(itsRow);
				} else {
					cellRenderer.setHoveredRow(-1);
				}
				aTable.repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});

		jTableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}

			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					int selectedRow = jTableData.getSelectedRow();
					if (selectedRow >= 0) {
						FamilyPlanning fp = (FamilyPlanning) sorter.getValueAt(selectedRow, -1);
						if (fp != null) {
							Window parentWindow = SwingUtilities.getWindowAncestor(FamilyPlanningPatList.this);
							JFrame parentFrame = parentWindow instanceof JFrame ? (JFrame) parentWindow : null;
							FamilyPlanningEdit edit = new FamilyPlanningEdit(parentFrame, fp, false);
							edit.addFamilyPlanningListener(new FamilyPlanningEdit.FamilyPlanningListener() {
								@Override
								public void fpInserted(AWTEvent e, FamilyPlanning fp) {
									reloadData();
								}

								@Override
								public void fpUpdated(AWTEvent e, FamilyPlanning fp) {
									reloadData();
								}
							});
							edit.setVisible(true);
						}
					}
				}
			}
		});

		DateCellRenderer dateCellRenderer = new DateCellRenderer();
		for (int i = 0; i < columns.length; i++) {
			jTableData.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
			if (i == 0) {
				jTableData.getColumnModel().getColumn(i).setCellRenderer(dateCellRenderer);
			}
		}

		sorter.sortByColumn(0, false);

		JScrollPane scrollPane = new JScrollPane(jTableData);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void loadData() {
		if (patient != null && patient.getCode() != null) {
			try {
				fpList = fpManager.getFamilyPlanningsByPatient(patient.getCode());
			} catch (Exception e) {
				fpList = new ArrayList<>();
			}
		} else {
			fpList = new ArrayList<>();
		}
	}

	public void reloadData() {
		loadData();
		if (model != null) {
			model.fireTableDataChanged();
		}
	}

	public JTable getTable() {
		return jTableData;
	}

	public List<FamilyPlanning> getFpList() {
		return fpList;
	}

	private class FPBrowserModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return fpList != null ? fpList.size() : 0;
		}

		@Override
		public String getColumnName(int c) {
			return columns[c];
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (fpList == null || row >= fpList.size()) {
				return null;
			}
			FamilyPlanning fp = fpList.get(row);
			if (column == -1) {
				return fp;
			} else if (column == 0) {
				return fp.getRegistrationDate();
			} else if (column == 1) {
				return fp.getCurrentMethod() != null ? fp.getCurrentMethod().getDescription() : "";
			} else if (column == 2) {
				return fp.getStatus() != null ? MessageBundle.getMessage(fp.getStatus().getKey()) : "";
			} else if (column == 3) {
				return fp.getNotes() != null ? fp.getNotes() : "";
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private static class DateCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof LocalDateTime) {
				setText(((LocalDateTime) value).format(DATE_FORMATTER));
			} else if (value == null) {
				setText("");
			}
			return this;
		}
	}
}
