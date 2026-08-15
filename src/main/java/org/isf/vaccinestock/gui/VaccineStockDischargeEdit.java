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

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.layout.SpringUtilities;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccinestock.manager.VaccineStockManager;
import org.isf.vaccinestock.model.VaccineLot;
import org.isf.vaccinestock.model.VaccineStockMovementReason;

/**
 * Records a manual discharge (loss, breakage, expired stock removed, correction) for a vaccine.
 * Mirrors {@link VaccineStockChargeEdit}'s two-step scenario: pick the product first (search field
 * + list, with an info panel once selected), enter reason/date/note, then - only once that's
 * confirmed - pick which lot to draw from off a table of lots that currently have a positive
 * balance. The stock movement is only created at the very end, in a single call to
 * {@link VaccineStockManager#newManualDischarge}, which is transactional: any failure (including
 * asking for more than the lot's balance) rolls back everything.
 */
public class VaccineStockDischargeEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final String CARD_PRODUCT = "product";
	private static final String CARD_LOT = "lot";

	private static final VaccineStockMovementReason[] MANUAL_REASONS = {
			VaccineStockMovementReason.MANUAL_DISCHARGE, VaccineStockMovementReason.CORRECTION
	};

	private final VaccineStockManager vaccineStockManager = Context.getApplicationContext().getBean(VaccineStockManager.class);
	private final VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);

	private List<Vaccine> allVaccines = new ArrayList<>();
	private Vaccine selectedVaccine;
	private final List<VaccineLot> lotRows = new ArrayList<>();

	private CardLayout cardLayout;
	private JPanel cardPanel;
	private JPanel productPanel;
	private JPanel lotPanel;

	private JTextField vaccineFilterTextField;
	private JComboBox<Vaccine> vaccineComboBox;
	private TitledBorder vaccineInfoTitledBorder;
	private JLabel vaccineNameValueLabel;
	private JLabel vaccineTypeValueLabel;
	private JComboBox<VaccineStockMovementReason> reasonComboBox;
	private JSpinner quantitySpinner;
	private GoodDateChooser movementDateChooser;
	private JTextField noteTextField;
	private JButton nextButton;
	private JButton productCancelButton;

	private final String[] lotColumns = {
			MessageBundle.getMessage("angal.medicalstock.lot.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.cost.txt").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.balance.txt").toUpperCase()
	};

	private JTable lotTable;
	private LotTableModel lotTableModel;
	private JButton okButton;
	private JButton lotCancelButton;

	public VaccineStockDischargeEdit(JFrame owner, Vaccine preselectedVaccine) {
		super(owner, true);
		this.selectedVaccine = preselectedVaccine;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.vaccinestock.newdischarge.title"));
		loadVaccines();
		setContentPane(getCardPanel());
		pack();
		setLocationRelativeTo(null);
	}

	private void loadVaccines() {
		try {
			allVaccines = vaccineBrowserManager.getVaccine();
		} catch (OHServiceException serviceException) {
			OHServiceExceptionUtil.showMessages(serviceException);
			allVaccines = new ArrayList<>();
		}
	}

	private JPanel getCardPanel() {
		if (cardPanel == null) {
			cardLayout = new CardLayout();
			cardPanel = new JPanel(cardLayout);
			cardPanel.add(getProductPanel(), CARD_PRODUCT);
			cardPanel.add(getLotPanel(), CARD_LOT);
			applyVaccineFilter();
			if (selectedVaccine != null) {
				for (Vaccine vaccine : allVaccines) {
					if (vaccine.equals(selectedVaccine)) {
						getVaccineComboBox().setSelectedItem(vaccine);
						break;
					}
				}
			}
		}
		return cardPanel;
	}

	// ---- step 1: product ----

	private JPanel getProductPanel() {
		if (productPanel == null) {
			productPanel = new JPanel(new BorderLayout());
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
			topPanel.add(getProductFormPanel());
			topPanel.add(getVaccineInfoPanel());
			productPanel.add(topPanel, BorderLayout.NORTH);
			productPanel.add(getProductButtonPanel(), BorderLayout.SOUTH);
		}
		return productPanel;
	}

	private JPanel getProductFormPanel() {
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new SpringLayout());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':'));
		dataPanel.add(getVaccineFilterTextField());

		dataPanel.add(new JLabel());
		dataPanel.add(getVaccineComboBox());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.vaccinestock.reason.txt") + ':'));
		dataPanel.add(getReasonComboBox());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.quantity.txt") + ':'));
		dataPanel.add(getQuantitySpinner());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.date.txt") + ':'));
		dataPanel.add(getMovementDateChooser());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.note.txt") + ':'));
		dataPanel.add(getNoteTextField());

		SpringUtilities.makeCompactGrid(dataPanel, 6, 2, 5, 5, 5, 5);
		return dataPanel;
	}

	private JPanel getVaccineInfoPanel() {
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		vaccineInfoTitledBorder = BorderFactory.createTitledBorder(
				MessageBundle.getMessage("angal.vaccinestock.vaccinedata.title"));
		infoPanel.setBorder(vaccineInfoTitledBorder);

		infoPanel.add(new JLabel(MessageBundle.getMessage("angal.common.name.txt") + ':'));
		vaccineNameValueLabel = new JLabel();
		infoPanel.add(vaccineNameValueLabel);

		infoPanel.add(new JLabel(MessageBundle.getMessage("angal.common.type.txt") + ':'));
		vaccineTypeValueLabel = new JLabel();
		infoPanel.add(vaccineTypeValueLabel);

		onVaccineSelected();
		return infoPanel;
	}

	private JTextField getVaccineFilterTextField() {
		if (vaccineFilterTextField == null) {
			vaccineFilterTextField = new JTextField(15);
			vaccineFilterTextField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void insertUpdate(DocumentEvent event) {
					applyVaccineFilter();
				}

				@Override
				public void removeUpdate(DocumentEvent event) {
					applyVaccineFilter();
				}

				@Override
				public void changedUpdate(DocumentEvent event) {
					applyVaccineFilter();
				}
			});
		}
		return vaccineFilterTextField;
	}

	private JComboBox<Vaccine> getVaccineComboBox() {
		if (vaccineComboBox == null) {
			vaccineComboBox = new JComboBox<>();
			vaccineComboBox.setRenderer(new DefaultListCellRenderer() {

				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value instanceof Vaccine vaccine) {
						setText(vaccine.getCode() + " - " + vaccine.getDescription());
					}
					return this;
				}
			});
			vaccineComboBox.addActionListener(actionEvent -> onVaccineSelected());
		}
		return vaccineComboBox;
	}

	private void applyVaccineFilter() {
		List<Vaccine> matches = VaccineStockGuiSupport.filterVaccines(allVaccines, getVaccineFilterTextField().getText());

		Vaccine previousSelection = selectedVaccine;
		getVaccineComboBox().removeAllItems();
		for (Vaccine vaccine : matches) {
			getVaccineComboBox().addItem(vaccine);
		}
		if (previousSelection != null && matches.contains(previousSelection)) {
			getVaccineComboBox().setSelectedItem(previousSelection);
		} else if (!matches.isEmpty()) {
			getVaccineComboBox().setSelectedIndex(0);
		} else {
			getVaccineComboBox().setSelectedItem(null);
		}
		onVaccineSelected();
	}

	private void onVaccineSelected() {
		selectedVaccine = (Vaccine) getVaccineComboBox().getSelectedItem();
		if (vaccineNameValueLabel == null) {
			return;
		}
		if (selectedVaccine == null) {
			vaccineInfoTitledBorder.setTitle(MessageBundle.getMessage("angal.vaccinestock.vaccinedata.title"));
			vaccineNameValueLabel.setText("");
			vaccineTypeValueLabel.setText("");
		} else {
			vaccineInfoTitledBorder.setTitle(
					MessageBundle.formatMessage("angal.vaccinestock.vaccinedata.fmt.title", selectedVaccine.getCode()));
			vaccineNameValueLabel.setText(selectedVaccine.getDescription());
			vaccineTypeValueLabel.setText(selectedVaccine.getVaccineType().getDescription());
		}
		vaccineNameValueLabel.getParent().repaint();
	}

	private JComboBox<VaccineStockMovementReason> getReasonComboBox() {
		if (reasonComboBox == null) {
			reasonComboBox = new JComboBox<>(MANUAL_REASONS);
			reasonComboBox.setRenderer(new DefaultListCellRenderer() {

				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value == VaccineStockMovementReason.MANUAL_DISCHARGE) {
						setText(MessageBundle.getMessage("angal.vaccinestock.reason.manualdischarge"));
					} else if (value == VaccineStockMovementReason.CORRECTION) {
						setText(MessageBundle.getMessage("angal.vaccinestock.reason.correction"));
					}
					return this;
				}
			});
		}
		return reasonComboBox;
	}

	private JSpinner getQuantitySpinner() {
		if (quantitySpinner == null) {
			quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
		}
		return quantitySpinner;
	}

	private GoodDateChooser getMovementDateChooser() {
		if (movementDateChooser == null) {
			movementDateChooser = new GoodDateChooser(LocalDate.now());
		}
		return movementDateChooser;
	}

	private JTextField getNoteTextField() {
		if (noteTextField == null) {
			noteTextField = new JTextField(20);
		}
		return noteTextField;
	}

	private JPanel getProductButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getNextButton(), null);
		buttonPanel.add(getProductCancelButton(), null);
		return buttonPanel;
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton(MessageBundle.getMessage("angal.vaccinestock.next.btn"));
			nextButton.addActionListener(actionEvent -> onNext());
		}
		return nextButton;
	}

	private void onNext() {
		if (selectedVaccine == null) {
			MessageDialog.error(this, "angal.vaccinestock.pleaseselectavaccine.msg");
			return;
		}
		if (movementDateChooser.getDate() == null) {
			MessageDialog.error(this, "angal.vaccinestock.pleaseinsertavaliddate.msg");
			return;
		}
		List<VaccineLot> available = vaccineStockManager.getAvailableLots(selectedVaccine);
		if (available.isEmpty()) {
			MessageDialog.error(this, "angal.vaccinestock.nostockavailable.fmt.msg", selectedVaccine.getDescription());
			return;
		}
		populateLotRows(available);
		cardLayout.show(cardPanel, CARD_LOT);
	}

	private JButton getProductCancelButton() {
		if (productCancelButton == null) {
			productCancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			productCancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			productCancelButton.addActionListener(actionEvent -> dispose());
		}
		return productCancelButton;
	}

	// ---- step 2: lot ----

	private JPanel getLotPanel() {
		if (lotPanel == null) {
			lotPanel = new JPanel(new BorderLayout());
			lotPanel.add(new JScrollPane(getLotTable()), BorderLayout.CENTER);
			lotPanel.add(getLotButtonPanel(), BorderLayout.SOUTH);
			lotTable.setPreferredScrollableViewportSize(new Dimension(400, 120));
		}
		return lotPanel;
	}

	private JTable getLotTable() {
		if (lotTable == null) {
			lotTableModel = new LotTableModel();
			lotTable = new JTable(lotTableModel);
			lotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return lotTable;
	}

	private void populateLotRows(List<VaccineLot> available) {
		lotRows.clear();
		lotRows.addAll(available);
		lotTableModel.fireTableDataChanged();
		if (!lotRows.isEmpty()) {
			lotTable.setRowSelectionInterval(0, 0);
		}
	}

	private JPanel getLotButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getOkButton(), null);
		buttonPanel.add(getLotCancelButton(), null);
		return buttonPanel;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> onOk());
		}
		return okButton;
	}

	private void onOk() {
		int selectedRow = lotTable.getSelectedRow();
		if (selectedRow < 0) {
			MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			return;
		}
		VaccineLot lot = lotRows.get(selectedRow);
		VaccineStockMovementReason reason = (VaccineStockMovementReason) reasonComboBox.getSelectedItem();
		int quantity = (Integer) quantitySpinner.getValue();
		LocalDate movementDate = movementDateChooser.getDate();

		try {
			vaccineStockManager.newManualDischarge(selectedVaccine, lot, quantity, reason, movementDate.atStartOfDay(), noteTextField.getText());
			dispose();
		} catch (OHServiceException serviceException) {
			OHServiceExceptionUtil.showMessages(serviceException);
		}
	}

	private JButton getLotCancelButton() {
		if (lotCancelButton == null) {
			lotCancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			lotCancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			lotCancelButton.addActionListener(actionEvent -> dispose());
		}
		return lotCancelButton;
	}

	private class LotTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return lotRows.size();
		}

		@Override
		public int getColumnCount() {
			return lotColumns.length;
		}

		@Override
		public String getColumnName(int column) {
			return lotColumns[column];
		}

		@Override
		public Object getValueAt(int row, int column) {
			VaccineLot lot = lotRows.get(row);
			switch (column) {
				case 0:
					return lot.getCode();
				case 1:
					return lot.getDueDate() == null ? "-" : lot.getDueDate().format(DATE_FORMATTER);
				case 2:
					return lot.getCost() == null ? "-" : lot.getCost().toString();
				case 3:
					return vaccineStockManager.getQuantity(lot);
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
