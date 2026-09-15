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
package org.isf.vaccinestock.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccinestock.manager.VaccineStockManager;
import org.isf.vaccinestock.model.VaccineLot;
import org.isf.vaccinestock.model.VaccineStockMovement;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

/**
 * Shows, for every vaccine, the current stock balance and nearest lot expiry, plus the movement
 * history for the selected vaccine. Allows recording manual charges and discharges.
 */
public class VaccineStockBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private static final int EXPIRY_ALERT_DAYS = 30;
	private static final Color LOW_STOCK_COLOR = new Color(255, 205, 205);
	private static final Color NEAR_EXPIRY_COLOR = new Color(255, 245, 180);

	private final VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
	private final VaccineStockManager vaccineStockManager = Context.getApplicationContext().getBean(VaccineStockManager.class);
	private final VaccineTypeBrowserManager vaccineTypeBrowserManager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel jButtonPanel;
	private JPanel jSelectionPanel;
	private JButton jChargeButton;
	private JButton jDischargeButton;
	private JButton jCloseButton;
	private JSplitPane jSplitPane;
	private JTable jStockTable;
	private JTable jMovementTable;
	private StockTableModel stockModel;
	private MovementTableModel movementModel;
	private TableRowSorter<StockTableModel> stockSorter;
	private VoLimitedTextField codeFilterTextField;
	private VoLimitedTextField descriptionFilterTextField;
	private JComboBox<VaccineType> typeFilterComboBox;

	private final String[] stockColumns = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.balance.txt").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.minquantity.txt").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.nearestexpiry.txt").toUpperCase()
	};
	private final int[] stockColumnWidth = { 70, 160, 80, 70, 90, 100 };

	private final String[] movementColumns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.lot.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.reason.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.note.txt").toUpperCase()
	};
	private final int[] movementColumnWidth = { 100, 100, 60, 130, 150 };

	public VaccineStockBrowser() {
		super();
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.vaccinestock.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		final int pfrmBase = 6;
		final int pfrmWidth = 4;
		final int pfrmHeight = 4;
		setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase) / 2,
				screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		setContentPane(getJContentPane());
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJSelectionPanel(), BorderLayout.WEST);
			jContentPane.add(getJSplitPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(getJStockTable()), new JScrollPane(getJMovementTable()));
			jSplitPane.setResizeWeight(0.6);
		}
		return jSplitPane;
	}

	private JTable getJStockTable() {
		if (jStockTable == null) {
			stockModel = new StockTableModel();
			jStockTable = new JTable(stockModel);
			for (int i = 0; i < stockColumnWidth.length; i++) {
				jStockTable.getColumnModel().getColumn(i).setPreferredWidth(stockColumnWidth[i]);
			}
			jStockTable.setDefaultRenderer(Object.class, new StockRowRenderer());
			stockSorter = new TableRowSorter<>(stockModel);
			jStockTable.setRowSorter(stockSorter);
			jStockTable.getSelectionModel().addListSelectionListener(this::onStockSelectionChanged);
		}
		return jStockTable;
	}

	private void onStockSelectionChanged(ListSelectionEvent event) {
		if (event.getValueIsAdjusting()) {
			return;
		}
		refreshMovements();
	}

	private JTable getJMovementTable() {
		if (jMovementTable == null) {
			movementModel = new MovementTableModel();
			jMovementTable = new JTable(movementModel);
			for (int i = 0; i < movementColumnWidth.length; i++) {
				jMovementTable.getColumnModel().getColumn(i).setPreferredWidth(movementColumnWidth[i]);
			}
			jMovementTable.setAutoCreateRowSorter(true);
		}
		return jMovementTable;
	}

	private JPanel getJSelectionPanel() {
		if (jSelectionPanel == null) {
			jSelectionPanel = new JPanel();
			jSelectionPanel.setLayout(new BoxLayout(jSelectionPanel, BoxLayout.Y_AXIS));
			jSelectionPanel.add(getCodeFilterPanel());
			jSelectionPanel.add(getDescriptionFilterPanel());
			jSelectionPanel.add(getTypeFilterPanel());
			jSelectionPanel.add(Box.createVerticalGlue());
		}
		return jSelectionPanel;
	}

	private JPanel getCodeFilterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labelPanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt")));
		panel.add(labelPanel);
		JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		fieldPanel.add(getCodeFilterTextField());
		panel.add(fieldPanel);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		return panel;
	}

	private JPanel getDescriptionFilterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labelPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt")));
		panel.add(labelPanel);
		JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		fieldPanel.add(getDescriptionFilterTextField());
		panel.add(fieldPanel);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		return panel;
	}

	private JPanel getTypeFilterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labelPanel.add(new JLabel(MessageBundle.getMessage("angal.common.type.txt")));
		panel.add(labelPanel);
		JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		fieldPanel.add(getTypeFilterComboBox());
		panel.add(fieldPanel);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		return panel;
	}

	private VoLimitedTextField getCodeFilterTextField() {
		if (codeFilterTextField == null) {
			codeFilterTextField = new VoLimitedTextField(10, 15);
			addFilterListener(codeFilterTextField);
		}
		return codeFilterTextField;
	}

	private VoLimitedTextField getDescriptionFilterTextField() {
		if (descriptionFilterTextField == null) {
			descriptionFilterTextField = new VoLimitedTextField(50, 15);
			addFilterListener(descriptionFilterTextField);
		}
		return descriptionFilterTextField;
	}

	private JComboBox<VaccineType> getTypeFilterComboBox() {
		if (typeFilterComboBox == null) {
			typeFilterComboBox = new JComboBox<>();
			typeFilterComboBox.addItem(new VaccineType("", MessageBundle.getMessage("angal.common.all.txt").toUpperCase()));
			try {
				for (VaccineType vaccineType : vaccineTypeBrowserManager.getVaccineType()) {
					typeFilterComboBox.addItem(vaccineType);
				}
			} catch (OHServiceException serviceException) {
				OHServiceExceptionUtil.showMessages(serviceException);
			}
			typeFilterComboBox.addActionListener(actionEvent -> applyFilters());
		}
		return typeFilterComboBox;
	}

	private void addFilterListener(VoLimitedTextField field) {
		field.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent event) {
				applyFilters();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				applyFilters();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				applyFilters();
			}
		});
	}

	private void applyFilters() {
		List<RowFilter<Object, Object>> filters = new ArrayList<>();

		String code = getCodeFilterTextField().getText().trim();
		if (!code.isEmpty()) {
			filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(code), 0));
		}

		String description = getDescriptionFilterTextField().getText().trim();
		if (!description.isEmpty()) {
			filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(description), 1));
		}

		VaccineType selectedType = (VaccineType) getTypeFilterComboBox().getSelectedItem();
		if (selectedType != null && !selectedType.getCode().isEmpty()) {
			filters.add(new RowFilter<Object, Object>() {

				@Override
				public boolean include(Entry<?, ?> entry) {
					int modelRow = (Integer) entry.getIdentifier();
					VaccineType rowType = stockModel.getRowAt(modelRow).vaccine.getVaccineType();
					return rowType != null && selectedType.getCode().equals(rowType.getCode());
				}
			});
		}

		stockSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			if (MainMenu.checkUserGrants("btnvaccinestockcharge")) {
				jButtonPanel.add(getJChargeButton(), null);
			}
			if (MainMenu.checkUserGrants("btnvaccinestockdischarge")) {
				jButtonPanel.add(getJDischargeButton(), null);
			}
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JButton getJChargeButton() {
		if (jChargeButton == null) {
			jChargeButton = new JButton(MessageBundle.getMessage("angal.vaccinestock.charge"));
			jChargeButton.addActionListener(actionEvent -> {
				new VaccineStockChargeEdit(this, getSelectedVaccine()).setVisible(true);
				refreshAll();
			});
		}
		return jChargeButton;
	}

	private JButton getJDischargeButton() {
		if (jDischargeButton == null) {
			jDischargeButton = new JButton(MessageBundle.getMessage("angal.vaccinestock.discharge"));
			jDischargeButton.addActionListener(actionEvent -> {
				new VaccineStockDischargeEdit(this, getSelectedVaccine()).setVisible(true);
				refreshAll();
			});
		}
		return jDischargeButton;
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private Vaccine getSelectedVaccine() {
		int row = jStockTable.getSelectedRow();
		// The row sorter works in view coordinates, which no longer match the model's row order
		// once the user sorts a column - translate back to the model index before indexing rows.
		return row < 0 ? null : stockModel.getRowAt(jStockTable.convertRowIndexToModel(row)).vaccine;
	}

	private void refreshAll() {
		stockModel.reload();
		stockModel.fireTableDataChanged();
		refreshMovements();
	}

	private void refreshMovements() {
		movementModel.reload(getSelectedVaccine());
		movementModel.fireTableDataChanged();
	}

	private class StockRowRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JComponent cell = (JComponent) component;
			// row is a view index; the row sorter can reorder rows relative to the model.
			StockRow stockRow = stockModel.getRowAt(table.convertRowIndexToModel(row));
			if (!isSelected) {
				if (stockRow.isBelowMinQuantity()) {
					component.setBackground(LOW_STOCK_COLOR);
				} else if (stockRow.isNearExpiry(EXPIRY_ALERT_DAYS)) {
					component.setBackground(NEAR_EXPIRY_COLOR);
				} else {
					component.setBackground(Color.WHITE);
				}
			}
			if (stockRow.isBelowMinQuantity()) {
				cell.setToolTipText(MessageBundle.getMessage("angal.vaccinestock.lowstock.tooltip"));
			} else if (stockRow.isNearExpiry(EXPIRY_ALERT_DAYS)) {
				cell.setToolTipText(MessageBundle.getMessage("angal.vaccinestock.nearexpiry.tooltip"));
			} else {
				cell.setToolTipText(null);
			}
			return component;
		}
	}

	private static class StockRow {

		private final Vaccine vaccine;
		private final int balance;
		private final LocalDate nearestExpiry;

		StockRow(Vaccine vaccine, int balance, LocalDate nearestExpiry) {
			this.vaccine = vaccine;
			this.balance = balance;
			this.nearestExpiry = nearestExpiry;
		}

		boolean isBelowMinQuantity() {
			return VaccineStockGuiSupport.isBelowMinQuantity(vaccine, balance);
		}

		boolean isNearExpiry(int days) {
			return VaccineStockGuiSupport.isNearExpiry(nearestExpiry, days);
		}
	}

	private class StockTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private List<StockRow> rows = new ArrayList<>();

		StockTableModel() {
			reload();
		}

		void reload() {
			List<StockRow> newRows = new ArrayList<>();
			try {
				for (Vaccine vaccine : vaccineBrowserManager.getVaccine()) {
					int balance = vaccineStockManager.getQuantity(vaccine);
					List<VaccineLot> available = vaccineStockManager.getAvailableLots(vaccine);
					LocalDate nearestExpiry = available.isEmpty() ? null : available.get(0).getDueDate().toLocalDate();
					newRows.add(new StockRow(vaccine, balance, nearestExpiry));
				}
			} catch (OHServiceException serviceException) {
				OHServiceExceptionUtil.showMessages(serviceException);
			}
			rows = newRows;
		}

		StockRow getRowAt(int row) {
			return rows.get(row);
		}

		@Override
		public int getRowCount() {
			return rows == null ? 0 : rows.size();
		}

		@Override
		public String getColumnName(int column) {
			return stockColumns[column];
		}

		@Override
		public int getColumnCount() {
			return stockColumns.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			StockRow stockRow = rows.get(row);
			Vaccine vaccine = stockRow.vaccine;
			switch (column) {
				case 0:
					return vaccine.getCode();
				case 1:
					return vaccine.getDescription();
				case 2:
					return vaccine.getVaccineType();
				case 3:
					return stockRow.balance;
				case 4:
					return vaccine.getMinQuantity() == null ? "-" : vaccine.getMinQuantity();
				case 5:
					return stockRow.nearestExpiry == null ? "-" : stockRow.nearestExpiry.format(DATE_FORMATTER);
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private class MovementTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private List<VaccineStockMovement> movements = new ArrayList<>();

		void reload(Vaccine vaccine) {
			if (vaccine == null) {
				movements = new ArrayList<>();
				return;
			}
			movements = vaccineStockManager.getMovements(vaccine, null, null);
		}

		@Override
		public int getRowCount() {
			return movements == null ? 0 : movements.size();
		}

		@Override
		public String getColumnName(int column) {
			return movementColumns[column];
		}

		@Override
		public int getColumnCount() {
			return movementColumns.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			VaccineStockMovement movement = movements.get(row);
			switch (column) {
				case 0:
					return movement.getDate().format(DATE_FORMATTER);
				case 1:
					return movement.getLot().getCode();
				case 2:
					return movement.getQuantity();
				case 3:
					return VaccineStockGuiSupport.formatReason(movement.getReason());
				case 4:
					return movement.getNote() == null ? "" : movement.getNote();
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
