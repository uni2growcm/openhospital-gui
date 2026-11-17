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
import java.time.LocalDate;
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
import org.isf.hiv.manager.HIVInfantManager;
import org.isf.hiv.model.HIVInfant;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.table.TableSorter;

public class HIVInfantPatList extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final Patient patient;
	private final HIVInfantManager infantManager;
	private List<HIVInfant> infantList = new ArrayList<>();

	private JTable jTableData;
	private HIVBrowserModel model;
	private TableSorter sorter;
	private final OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	private final String[] columns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.hiv.table.status").toUpperCase(),
			MessageBundle.getMessage("angal.hiv.filter.feeding").toUpperCase(),
			MessageBundle.getMessage("angal.hiv.label.weights").toUpperCase(),
			MessageBundle.getMessage("angal.hiv.filter.followup.date").toUpperCase(),
			MessageBundle.getMessage("angal.hiv.label.followup.end").toUpperCase(),
			MessageBundle.getMessage("angal.common.notes.txt").toUpperCase()
	};

	private final int[] columnWidths = { 120, 100, 150, 80, 120, 120, 180 };

	public HIVInfantPatList(Patient patient) {
		this.patient = patient;
		this.infantManager = Context.getApplicationContext().getBean(HIVInfantManager.class);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		loadData();

		model = new HIVBrowserModel();
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
						HIVInfant infant = (HIVInfant) sorter.getValueAt(selectedRow, -1);
						if (infant != null) {
							Window parentWindow = SwingUtilities.getWindowAncestor(HIVInfantPatList.this);
							JFrame parentFrame = parentWindow instanceof JFrame ? (JFrame) parentWindow : null;
							HIVInfantEdit edit = new HIVInfantEdit(parentFrame, infant, false);
							edit.addHIVInfantListener(new HIVInfantEdit.HIVInfantListener() {
								@Override
								public void infantInserted(AWTEvent e, HIVInfant inf) {
									reloadData();
								}

								@Override
								public void infantUpdated(AWTEvent e, HIVInfant inf) {
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
			if (i == 0 || i == 4 || i == 5) {
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
				infantList = infantManager.getInfantsByPatientCode(patient.getCode());
			} catch (Exception e) {
				infantList = new ArrayList<>();
			}
		} else {
			infantList = new ArrayList<>();
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

	public List<HIVInfant> getInfantList() {
		return infantList;
	}

	private class HIVBrowserModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return infantList != null ? infantList.size() : 0;
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
			if (infantList == null || row >= infantList.size()) {
				return null;
			}
			HIVInfant infant = infantList.get(row);
			if (column == -1) {
				return infant;
			} else if (column == 0) {
				return infant.getRegistrationDate();
			} else if (column == 1) {
				return infant.getStatus() != null ? infant.getStatus().getDescription() : "";
			} else if (column == 2) {
				return infant.getFeedingType() != null ? infant.getFeedingType().getDescription() : "";
			} else if (column == 3) {
				return infant.getBirthWeight() != null ? infant.getBirthWeight() + " kg" : "";
			} else if (column == 4) {
				return infant.getFollowUpStartDate();
			} else if (column == 5) {
				return infant.getFollowUpEndDate();
			} else if (column == 6) {
				return infant.getNotes() != null ? infant.getNotes() : "";
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
			} else if (value instanceof LocalDate) {
				setText(((LocalDate) value).format(DATE_FORMATTER));
			} else if (value == null) {
				setText("");
			}
			return this;
		}
	}
}
