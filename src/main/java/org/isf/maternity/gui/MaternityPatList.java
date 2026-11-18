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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.PregnancyBrowserManager;
import org.isf.maternity.manager.PregnancyVisitBrowserManager;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyVisit;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.table.TableSorter;

public class MaternityPatList extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final Patient patient;
	private final PregnancyBrowserManager pregnancyManager;
	private final PregnancyVisitBrowserManager visitManager;

	private List<Pregnancy> pregnancyList = new ArrayList<>();
	private List<PregnancyVisit> visitList = new ArrayList<>();
	private Pregnancy selectedPregnancy;
	private PregnancyVisit selectedVisit;

	private JTable jTableData;
	private MaternityBrowserModel model;
	private TableSorter sorter;

	private JTable jTableVisits;
	private MaternityVisitsModel visitsModel;
	private TableSorter visitsSorter;

	private final OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
	private final OhDefaultCellRenderer visitCellRenderer = new OhDefaultCellRenderer();

	private final String[] columns = {
			MessageBundle.getMessage("angal.maternity.lmp.col").toUpperCase(),
			MessageBundle.getMessage("angal.maternity.edd.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.status.txt").toUpperCase(),
			MessageBundle.getMessage("angal.maternity.risklevel.col").toUpperCase(),
			MessageBundle.getMessage("angal.maternity.gestational.age").toUpperCase(),
			(MessageBundle.getMessage("angal.maternity.gravidity.label") + " / " + MessageBundle.getMessage("angal.maternity.parity.label")).toUpperCase()
	};

	private final int[] columnWidths = { 130, 150, 100, 120, 130, 120 };

	private final String[] visitColumns = {
			MessageBundle.getMessage("angal.maternity.visit.date").toUpperCase(),
			MessageBundle.getMessage("angal.maternity.typology").toUpperCase(),
			MessageBundle.getMessage("angal.maternity.gestational.age").toUpperCase(),
			(MessageBundle.getMessage("angal.maternity.maternal.weight") + " (kg)").toUpperCase(),
			(MessageBundle.getMessage("angal.maternity.blood.pressure") + " (mmHg)").toUpperCase(),
			(MessageBundle.getMessage("angal.maternity.fundal.height") + " (cm)").toUpperCase(),
			(MessageBundle.getMessage("angal.maternity.fetal.heart.rate") + " (bpm)").toUpperCase()
	};

	private final int[] visitColumnWidths = { 130, 150, 100, 100, 110, 100, 100 };

	public MaternityPatList(Patient patient) {
		this.patient = patient;
		this.pregnancyManager = Context.getApplicationContext().getBean(PregnancyBrowserManager.class);
		this.visitManager = Context.getApplicationContext().getBean(PregnancyVisitBrowserManager.class);
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		loadData();

		// Pregnancy Table
		model = new MaternityBrowserModel();
		sorter = new TableSorter(model);
		jTableData = new JTable(sorter);
		jTableData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
			public void mouseDragged(MouseEvent e) {}
		});

		jTableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}

			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					openPregnancyDetails();
				}
			}
		});

		jTableData.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int selectedRow = jTableData.getSelectedRow();
					if (selectedRow >= 0 && selectedRow < pregnancyList.size()) {
						selectedPregnancy = (Pregnancy) sorter.getValueAt(selectedRow, -1);
						loadVisits();
					} else {
						selectedPregnancy = null;
						loadVisits();
					}
				}
			}
		});

		DateCellRenderer dateCellRenderer = new DateCellRenderer(DATE_FORMATTER);
		for (int i = 0; i < columns.length; i++) {
			jTableData.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
			if (i == 0 || i == 1) {
				jTableData.getColumnModel().getColumn(i).setCellRenderer(dateCellRenderer);
			}
		}

		sorter.sortByColumn(0, false);

		// Visits Table
		visitsModel = new MaternityVisitsModel();
		visitsSorter = new TableSorter(visitsModel);
		jTableVisits = new JTable(visitsSorter);
		jTableVisits.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableVisits.setDefaultRenderer(Object.class, visitCellRenderer);
		jTableVisits.setDefaultRenderer(Double.class, visitCellRenderer);

		jTableVisits.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				if (itsRow >= 0 && itsRow < aTable.getModel().getRowCount()) {
					visitCellRenderer.setHoveredRow(itsRow);
				} else {
					visitCellRenderer.setHoveredRow(-1);
				}
				aTable.repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {}
		});

		jTableVisits.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				visitCellRenderer.setHoveredRow(-1);
			}

			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					openEditVisit();
				}
			}
		});

		jTableVisits.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int selectedRow = jTableVisits.getSelectedRow();
					if (selectedRow >= 0 && selectedRow < visitList.size()) {
						selectedVisit = (PregnancyVisit) visitsSorter.getValueAt(selectedRow, -1);
					} else {
						selectedVisit = null;
					}
				}
			}
		});

		DateCellRenderer visitDateRenderer = new DateCellRenderer(DATE_TIME_FORMATTER);
		for (int i = 0; i < visitColumns.length; i++) {
			jTableVisits.getColumnModel().getColumn(i).setPreferredWidth(visitColumnWidths[i]);
			if (i == 0) {
				jTableVisits.getColumnModel().getColumn(i).setCellRenderer(visitDateRenderer);
			}
		}

		visitsSorter.sortByColumn(0, false);

		// Layout
		JPanel tablesPanel = new JPanel();
		tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));

		JScrollPane pregnancyScroll = new JScrollPane(jTableData);
		pregnancyScroll.setPreferredSize(new Dimension(600, 150));
		tablesPanel.add(pregnancyScroll);

		JLabel visitsLabel = new JLabel(MessageBundle.getMessage("angal.tb.browser.visitstab"));
		visitsLabel.setFont(visitsLabel.getFont().deriveFont(Font.BOLD));
		visitsLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
		tablesPanel.add(visitsLabel);

		JScrollPane visitScroll = new JScrollPane(jTableVisits);
		visitScroll.setPreferredSize(new Dimension(600, 150));
		tablesPanel.add(visitScroll);

		JScrollPane mainScroll = new JScrollPane(tablesPanel);
		add(mainScroll, BorderLayout.CENTER);

		// Select the first pregnancy if available
		if (!pregnancyList.isEmpty()) {
			jTableData.setRowSelectionInterval(0, 0);
		}
	}

	private void openPregnancyDetails() {
		if (selectedPregnancy == null) {
			int selectedRow = jTableData.getSelectedRow();
			if (selectedRow >= 0) {
				selectedPregnancy = (Pregnancy) sorter.getValueAt(selectedRow, -1);
			}
		}
		if (selectedPregnancy != null) {
			Window parentWindow = SwingUtilities.getWindowAncestor(this);
			JFrame parentFrame = parentWindow instanceof JFrame ? (JFrame) parentWindow : null;
			PregnancyDetailsView details = new PregnancyDetailsView(parentFrame, selectedPregnancy);
			details.setVisible(true);
			reloadData();
		}
	}

	private void openEditVisit() {
		if (selectedVisit == null) {
			int selectedRow = jTableVisits.getSelectedRow();
			if (selectedRow >= 0) {
				selectedVisit = (PregnancyVisit) visitsSorter.getValueAt(selectedRow, -1);
			}
		}
		if (selectedVisit != null) {
			Window parentWindow = SwingUtilities.getWindowAncestor(this);
			JFrame parentFrame = parentWindow instanceof JFrame ? (JFrame) parentWindow : null;
			VisitEdit edit = new VisitEdit(parentFrame, selectedVisit, false);
			edit.addMaternityVisitListener(new VisitEdit.MaternityVisitListener() {
				@Override
				public void visitInserted(java.awt.AWTEvent e, PregnancyVisit v) {
					loadVisits();
				}
				@Override
				public void visitUpdated(java.awt.AWTEvent e, PregnancyVisit v) {
					loadVisits();
				}
			});
			edit.setVisible(true);
		}
	}

	private void loadData() {
		if (patient != null && patient.getCode() != null) {
			try {
				pregnancyList = pregnancyManager.getPregnanciesByPatient(patient.getCode());
			} catch (Exception e) {
				pregnancyList = new ArrayList<>();
			}
		} else {
			pregnancyList = new ArrayList<>();
		}
	}

	private void loadVisits() {
		if (selectedPregnancy != null && selectedPregnancy.getId() != null) {
			try {
				visitList = visitManager.getVisitsByPregnancy(selectedPregnancy.getId());
			} catch (Exception e) {
				visitList = new ArrayList<>();
			}
		} else {
			visitList = new ArrayList<>();
		}
		selectedVisit = null;
		if (visitsModel != null) {
			visitsModel.fireTableDataChanged();
		}
	}

	public void reloadData() {
		loadData();
		if (model != null) {
			model.fireTableDataChanged();
		}
		if (!pregnancyList.isEmpty()) {
			int rowToSelect = 0;
			if (selectedPregnancy != null) {
				for (int i = 0; i < pregnancyList.size(); i++) {
					if (pregnancyList.get(i).getId().equals(selectedPregnancy.getId())) {
						rowToSelect = i;
						break;
					}
				}
			}
			jTableData.setRowSelectionInterval(rowToSelect, rowToSelect);
		} else {
			selectedPregnancy = null;
			loadVisits();
		}
	}

	public JTable getTable() {
		return jTableData;
	}

	public JTable getVisitsTable() {
		return jTableVisits;
	}

	public List<Pregnancy> getPregnancyList() {
		return pregnancyList;
	}

	public List<PregnancyVisit> getVisitList() {
		return visitList;
	}

	private class MaternityBrowserModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return pregnancyList != null ? pregnancyList.size() : 0;
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
			if (pregnancyList == null || row >= pregnancyList.size()) {
				return null;
			}
			Pregnancy p = pregnancyList.get(row);
			if (column == -1) {
				return p;
			} else if (column == 0) {
				return p.getLmp() != null ? p.getLmp() : p.getDate();
			} else if (column == 1) {
				return p.getEddLmp() != null ? p.getEddLmp() : p.getEddScan();
			} else if (column == 2) {
				return p.getStatus() != null ? p.getStatus().getDescription() : "";
			} else if (column == 3) {
				return p.getRiskLevel() != null ? p.getRiskLevel().getDescription() : "";
			} else if (column == 4) {
				return p.getCurrentGestationalAge() != null ? p.getCurrentGestationalAge() : "";
			} else if (column == 5) {
				return "G:" + (p.getGravidity() != null ? p.getGravidity() : "-") + " P:" + (p.getParity() != null ? p.getParity() : "-");
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private class MaternityVisitsModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return visitList != null ? visitList.size() : 0;
		}

		@Override
		public String getColumnName(int c) {
			return visitColumns[c];
		}

		@Override
		public int getColumnCount() {
			return visitColumns.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (visitList == null || row >= visitList.size()) {
				return null;
			}
			PregnancyVisit v = visitList.get(row);
			if (column == -1) {
				return v;
			} else if (column == 0) {
				return v.getVisitDate();
			} else if (column == 1) {
				return v.getVisitType() != null ? v.getVisitType().getDescription() : "";
			} else if (column == 2) {
				return v.getGestationalAge() != null ? v.getGestationalAge() : "";
			} else if (column == 3) {
				return v.getMaternalWeight() != null ? String.valueOf(v.getMaternalWeight()) : "";
			} else if (column == 4) {
				if (v.getSystolicBP() != null && v.getDiastolicBP() != null) {
					return v.getSystolicBP() + "/" + v.getDiastolicBP();
				} else if (v.getSystolicBP() != null) {
					return String.valueOf(v.getSystolicBP());
				}
				return "";
			} else if (column == 5) {
				return v.getFundalHeight() != null ? String.valueOf(v.getFundalHeight()) : "";
			} else if (column == 6) {
				return v.getFetalHeartRate() != null ? String.valueOf(v.getFetalHeartRate()) : "";
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
		private final DateTimeFormatter formatter;

		public DateCellRenderer(DateTimeFormatter formatter) {
			this.formatter = formatter;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof LocalDateTime) {
				setText(((LocalDateTime) value).format(formatter));
			} else if (value instanceof LocalDate) {
				setText(((LocalDate) value).format(formatter));
			} else if (value == null) {
				setText("");
			}
			return this;
		}
	}
}
