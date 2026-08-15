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
import java.math.BigDecimal;
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
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccinestock.manager.VaccineStockManager;
import org.isf.vaccinestock.model.VaccineLot;

/**
 * Records a charge (entry) of doses for a vaccine, mirroring the pharmacy's charging scenario: pick
 * the product first (search field + list, with an info panel showing its code/name/type once
 * selected), enter quantity/date/note, then - only once that's confirmed - pick or create the lot
 * and its cost. The stock movement (and new lot, if any) is only created at the very end, in a
 * single call to {@link VaccineStockManager#newCharge}, which is transactional: any failure rolls
 * back everything.
 */
public class VaccineStockChargeEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final String CARD_PRODUCT = "product";
	private static final String CARD_LOT = "lot";
	private static final String CARD_NEW_LOT = "newlot";

	private final VaccineStockManager vaccineStockManager = Context.getApplicationContext().getBean(VaccineStockManager.class);
	private final VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);

	private List<Vaccine> allVaccines = new ArrayList<>();
	private Vaccine selectedVaccine;
	private final List<VaccineLot> lotRows = new ArrayList<>();

	private CardLayout cardLayout;
	private JPanel cardPanel;
	private JPanel productPanel;
	private JPanel lotPanel;
	private JPanel newLotPanel;

	private JTextField vaccineFilterTextField;
	private JComboBox<Vaccine> vaccineComboBox;
	private TitledBorder vaccineInfoTitledBorder;
	private JLabel vaccineNameValueLabel;
	private JLabel vaccineTypeValueLabel;
	private JSpinner quantitySpinner;
	private GoodDateChooser movementDateChooser;
	private JTextField noteTextField;
	private JButton nextButton;
	private JButton productCancelButton;

	private final String[] lotColumns = {
			MessageBundle.getMessage("angal.medicalstock.lot.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
			MessageBundle.getMessage("angal.vaccinestock.cost.txt").toUpperCase()
	};

	private JTable lotTable;
	private LotTableModel lotTableModel;
	private JButton newLotButton;
	private JButton okButton;
	private JButton lotCancelButton;

	private VoLimitedTextField newLotCodeTextField;
	private GoodDateChooser newLotPreparationDateChooser;
	private GoodDateChooser newLotDueDateChooser;
	private JTextField newLotCostTextField;
	private JButton newLotValidateButton;
	private JButton newLotCancelButton;

	public VaccineStockChargeEdit(JFrame owner, Vaccine preselectedVaccine) {
		super(owner, true);
		this.selectedVaccine = preselectedVaccine;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.vaccinestock.newcharge.title"));
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
			cardPanel.add(getNewLotPanel(), CARD_NEW_LOT);
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

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.quantity.txt") + ':'));
		dataPanel.add(getQuantitySpinner());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.date.txt") + ':'));
		dataPanel.add(getMovementDateChooser());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.note.txt") + ':'));
		dataPanel.add(getNoteTextField());

		SpringUtilities.makeCompactGrid(dataPanel, 5, 2, 5, 5, 5, 5);
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
		populateLotRows();
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
			lotPanel.add(getLotToolbarPanel(), BorderLayout.NORTH);
			lotPanel.add(new JScrollPane(getLotTable()), BorderLayout.CENTER);
			lotPanel.add(getLotButtonPanel(), BorderLayout.SOUTH);
		}
		return lotPanel;
	}

	private JPanel getLotToolbarPanel() {
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.add(getNewLotButton(), null);
		return toolbarPanel;
	}

	private JButton getNewLotButton() {
		if (newLotButton == null) {
			newLotButton = new JButton(MessageBundle.getMessage("angal.vaccinestock.newlot.btn"));
			newLotButton.addActionListener(actionEvent -> {
				resetNewLotForm();
				cardLayout.show(cardPanel, CARD_NEW_LOT);
			});
		}
		return newLotButton;
	}

	private JTable getLotTable() {
		if (lotTable == null) {
			lotTableModel = new LotTableModel();
			lotTable = new JTable(lotTableModel);
			lotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lotTable.setPreferredScrollableViewportSize(new Dimension(400, 120));
		}
		return lotTable;
	}

	private void populateLotRows() {
		lotRows.clear();
		lotRows.addAll(vaccineStockManager.getLots(selectedVaccine));
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
		VaccineLot lotToCharge = lotRows.get(selectedRow);

		int quantity = (Integer) quantitySpinner.getValue();
		LocalDate movementDate = movementDateChooser.getDate();

		try {
			vaccineStockManager.newCharge(selectedVaccine, lotToCharge, quantity, movementDate.atStartOfDay(), noteTextField.getText());
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
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	// ---- step 2b: create a new lot ----

	private JPanel getNewLotPanel() {
		if (newLotPanel == null) {
			newLotPanel = new JPanel(new BorderLayout());
			newLotPanel.add(getNewLotDataPanel(), BorderLayout.NORTH);
			newLotPanel.add(getNewLotButtonPanel(), BorderLayout.SOUTH);
		}
		return newLotPanel;
	}

	private JPanel getNewLotDataPanel() {
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new SpringLayout());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.lot.col") + ':'));
		dataPanel.add(getNewLotCodeTextField());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.vaccinestock.preparationdate.txt") + ':'));
		dataPanel.add(getNewLotPreparationDateChooser());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.duedate") + ':'));
		dataPanel.add(getNewLotDueDateChooser());

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.vaccinestock.cost.txt") + ':'));
		dataPanel.add(getNewLotCostTextField());

		SpringUtilities.makeCompactGrid(dataPanel, 4, 2, 5, 5, 5, 5);
		return dataPanel;
	}

	private VoLimitedTextField getNewLotCodeTextField() {
		if (newLotCodeTextField == null) {
			newLotCodeTextField = new VoLimitedTextField(50, 20);
		}
		return newLotCodeTextField;
	}

	private GoodDateChooser getNewLotPreparationDateChooser() {
		if (newLotPreparationDateChooser == null) {
			newLotPreparationDateChooser = new GoodDateChooser(LocalDate.now());
		}
		return newLotPreparationDateChooser;
	}

	private GoodDateChooser getNewLotDueDateChooser() {
		if (newLotDueDateChooser == null) {
			newLotDueDateChooser = new GoodDateChooser(LocalDate.now());
		}
		return newLotDueDateChooser;
	}

	private JTextField getNewLotCostTextField() {
		if (newLotCostTextField == null) {
			newLotCostTextField = new JTextField(10);
		}
		return newLotCostTextField;
	}

	private void resetNewLotForm() {
		getNewLotCodeTextField().setText("");
		getNewLotPreparationDateChooser().setDate(LocalDate.now());
		getNewLotDueDateChooser().setDate(LocalDate.now());
		getNewLotCostTextField().setText("");
	}

	private JPanel getNewLotButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getNewLotValidateButton(), null);
		buttonPanel.add(getNewLotCancelButton(), null);
		return buttonPanel;
	}

	private JButton getNewLotValidateButton() {
		if (newLotValidateButton == null) {
			newLotValidateButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			newLotValidateButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			newLotValidateButton.addActionListener(actionEvent -> onNewLotValidate());
		}
		return newLotValidateButton;
	}

	private void onNewLotValidate() {
		String code = getNewLotCodeTextField().getText().trim();
		if (code.isEmpty()) {
			MessageDialog.error(this, "angal.vaccinestock.pleaseinsertalotcode.msg");
			return;
		}
		if (VaccineStockGuiSupport.isLotCodeInUse(lotRows, code)) {
			MessageDialog.error(this, "angal.vaccinestock.lotcodealreadyused.fmt.msg", code);
			return;
		}
		LocalDate preparationDate = getNewLotPreparationDateChooser().getDate();
		LocalDate dueDate = getNewLotDueDateChooser().getDate();
		if (preparationDate == null || dueDate == null) {
			MessageDialog.error(this, "angal.vaccinestock.pleaseinsertthelotdates.msg");
			return;
		}
		if (dueDate.isBefore(preparationDate)) {
			MessageDialog.error(this, "angal.vaccinestock.duedatemustbeafterthepreparationdate.msg");
			return;
		}

		VaccineLot newLot = new VaccineLot(selectedVaccine, code, preparationDate.atStartOfDay(), dueDate.atStartOfDay());
		try {
			BigDecimal cost = VaccineStockGuiSupport.parseCost(getNewLotCostTextField().getText());
			if (cost != null) {
				newLot.setCost(cost);
			}
		} catch (NumberFormatException numberFormatException) {
			MessageDialog.error(this, "angal.vaccinestock.pleaseinsertavalidcost.msg");
			return;
		}

		lotRows.add(0, newLot);
		lotTableModel.fireTableDataChanged();
		lotTable.setRowSelectionInterval(0, 0);
		cardLayout.show(cardPanel, CARD_LOT);
	}

	private JButton getNewLotCancelButton() {
		if (newLotCancelButton == null) {
			newLotCancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			newLotCancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			newLotCancelButton.addActionListener(actionEvent -> cardLayout.show(cardPanel, CARD_LOT));
		}
		return newLotCancelButton;
	}
}
