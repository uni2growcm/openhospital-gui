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
package org.isf.homevisit.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.homevisit.manager.HomeVisitBrowserManager;
import org.isf.homevisit.model.HomeVisit;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.table.TableSorter;

public class HomeVisitPatList extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final Patient patient;
	private final HomeVisitBrowserManager homeVisitManager;
	private List<HomeVisit> homeVisitList = new ArrayList<>();

	private JTable jTableData;
	private HomeVisitBrowserModel model;
	private TableSorter sorter;
	private final OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	private final String[] columns = {
			MessageBundle.getMessage("angal.homevisit.date.col").toUpperCase(),
			MessageBundle.getMessage("angal.homevisit.staff.col").toUpperCase(),
			MessageBundle.getMessage("angal.homevisit.status.col").toUpperCase(),
			MessageBundle.getMessage("angal.homevisit.purpose.col").toUpperCase(),
			MessageBundle.getMessage("angal.homevisit.nextvisit.col").toUpperCase(),
			MessageBundle.getMessage("angal.homevisit.notes.col").toUpperCase()
	};

	private final int[] columnWidths = { 130, 150, 100, 150, 130, 200 };

	public HomeVisitPatList(Patient patient) {
		this.patient = patient;
		this.homeVisitManager = Context.getApplicationContext().getBean(HomeVisitBrowserManager.class);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		loadData();

		model = new HomeVisitBrowserModel();
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
						HomeVisit visit = (HomeVisit) sorter.getValueAt(selectedRow, -1);
						if (visit != null) {
							HomeVisitEdit edit = new HomeVisitEdit(null, homeVisitManager, visit, updatedVisit -> reloadData());
							edit.setVisible(true);
						}
					}
				}
			}
		});

		DateCellRenderer dateCellRenderer = new DateCellRenderer();
		for (int i = 0; i < columns.length; i++) {
			jTableData.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
			if (i == 0 || i == 4) {
				jTableData.getColumnModel().getColumn(i).setCellRenderer(dateCellRenderer);
			}
		}

		sorter.sortByColumn(0, false);

		JScrollPane scrollPane = new JScrollPane(jTableData);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void loadData() {
		if (patient != null) {
			try {
				homeVisitList = homeVisitManager.getHomeVisitsByPatient(patient);
			} catch (OHServiceException e) {
				homeVisitList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
		} else {
			homeVisitList = new ArrayList<>();
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

	public List<HomeVisit> getHomeVisitList() {
		return homeVisitList;
	}

	private class HomeVisitBrowserModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return homeVisitList != null ? homeVisitList.size() : 0;
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
			if (homeVisitList == null || row >= homeVisitList.size()) {
				return null;
			}
			HomeVisit visit = homeVisitList.get(row);
			if (column == -1) {
				return visit;
			} else if (column == 0) {
				return visit.getVisitStartDate();
			} else if (column == 1) {
				return visit.getStaff() != null ? (visit.getStaff().getFirstName() + " " + visit.getStaff().getLastName()) : "";
			} else if (column == 2) {
				return visit.getStatus() != null ? MessageBundle.getMessage(visit.getStatus().getMessageKey()) : "";
			} else if (column == 3) {
				return visit.getPurpose() != null ? visit.getPurpose() : "";
			} else if (column == 4) {
				return visit.getNextVisitDate();
			} else if (column == 5) {
				if (visit.getClinicalNotes() != null && !visit.getClinicalNotes().isEmpty()) {
					return visit.getClinicalNotes();
				}
				return visit.getObservations() != null ? visit.getObservations() : "";
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
				setText(((LocalDateTime) value).format(DATE_TIME_FORMATTER));
			} else if (value == null) {
				setText("");
			}
			return this;
		}
	}
}
