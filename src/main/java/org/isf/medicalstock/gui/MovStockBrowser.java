/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.medicalstock.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.isf.utils.Constants.DATE_FORMAT_YYYYMMDD;
import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovBrowserManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStockCard;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.StockCardDialog;
import org.isf.utils.jobjects.StockLedgerDialog;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * MovStockBrowser - list medicals movement. Let the user search for movements and insert a new movement.
 */
public class MovStockBrowser extends ModalJFrame {

	private static final int defaultInitDelay = ToolTipManager.sharedInstance().getInitialDelay();
	private static final Color defaultBackgroundColor = (Color) UIManager.get("ToolTip.background");

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MovStockBrowser.class);

	private static final String FROM_LABEL = MessageBundle.getMessage("angal.common.from.txt") + ':';
	private static final String TO_LABEL = MessageBundle.getMessage("angal.common.to.txt") + ':';
	private static final String TEXT_ALL = MessageBundle.getMessage("angal.common.all.txt");
	private static final String TEXT_ALLCHARGES = MessageBundle.getMessage("angal.medicalstock.allcharges.txt");
	private static final String TEXT_ALLDISCHARGES = MessageBundle.getMessage("angal.medicalstock.alldischarges.txt");

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY);

	private final JFrame myFrame;
	private JButton filterButton;
	private JButton nextButton;
	private JButton prevButton;
	private JButton resetButton;
	private JCheckBox jCheckBoxKeepFilter;
	private JComboBox medicalBox;
	private JComboBox medicalTypeBox;
	private JComboBox movementTypeBox;
	private JComboBox<Integer> pagesCombo;
	private JComboBox wardBox;
	private GoodDateChooser movDateFrom;
	private GoodDateChooser movDateTo;
	private GoodDateChooser lotPrepFrom;
	private GoodDateChooser lotPrepTo;
	private GoodDateChooser lotDueFrom;
	private GoodDateChooser lotDueTo;
	private JLabel underLabel;
	private JLabel totalMovementsLabel;
	private JTable movTable;
	private JTable jTableTotal;
	private int totalQti;
	private int pages = 0;
	private int currentPage = 0;
	private int totalMovements = 0;
	private final int PAGE_SIZE = 10;
	private BigDecimal totalAmount;
	private MovBrowserModel model;
	private List<Movement> allMoves;
	private List<Movement> movements = new ArrayList<>();
	private final String[] pColumns = {
		MessageBundle.getMessage("angal.medicalstock.refno.col").toUpperCase(),
		MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.category.col").toUpperCase(),
		MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.ward.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.qty.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.pharmaceutical.col").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.medtype.col").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.lot.col").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.prepdate.col").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.origin.col").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.cost.col").toUpperCase(),
		MessageBundle.getMessage("angal.common.total.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.userid").toUpperCase()
	};
	private final boolean[] pColumnBold = { true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false };
	private final int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
		SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER,
		SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.CENTER };
	private final boolean isSingleUser = GeneralData.getGeneralData().getSINGLEUSER();
	private final boolean[] pColumnVisible = { true, true, false, true, true, true, true, true, true, !GeneralData.AUTOMATICLOT_IN, !GeneralData.AUTOMATICLOT_IN,
		true, true,
		GeneralData.LOTWITHCOST, GeneralData.LOTWITHCOST, !isSingleUser };

	private final int[] pColumnWidth = { 50, 90, 45, 45, 130, 50, 30, 150, 70, 70, 80, 80, 50, 50, 70, 70 };

	/*
	 * Adds to facilitate the selection of products
	 */
	private JTextField searchTextField;
	private JButton searchButton;

	private Map<Integer, String> supMap = new HashMap<>();

	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final MedicalTypeBrowserManager medicalTypeBrowserManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private final MedicalDsrStockMovementTypeBrowserManager medicalDsrStockMovementTypeBrowserManager = Context.getApplicationContext()
		.getBean(MedicalDsrStockMovementTypeBrowserManager.class);
	private final MovBrowserManager movBrowserManager = Context.getApplicationContext().getBean(MovBrowserManager.class);
	private final HospitalBrowsingManager hospitalBrowsingManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
	private final SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);
	private final WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);

	public MovStockBrowser() throws OHServiceException {
		myFrame = this;
		setTitle(MessageBundle.getMessage("angal.medicalstock.stockmovementbrowser.title"));
		try {
			supMap = supplierBrowserManager.getHashMap(true);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		setContentPane(getContentpane());
		filterButton.doClick();

		updateTotals();
		setMinimumSize(new Dimension(775, 655));
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private JPanel getContentpane() throws OHServiceException {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(getPaginatePanel(), BorderLayout.NORTH);
		contentPane.add(getFilterPanel(), BorderLayout.WEST);
		contentPane.add(getTablesPanel(), BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		return contentPane;
	}

	/**
	 * This method controls if the automaticlot option is on
	 * @return
	 */
	private boolean isAutomaticLot() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	private JPanel getPaginatePanel() throws OHServiceException {
		JPanel paginatePanel = new JPanel(new WrapLayout());
		paginatePanel.add(getPrevButton());
		paginatePanel.add(getPagesCombo());
		paginatePanel.add(getUnderLabel());
		paginatePanel.add(getNextButton());
		paginatePanel.add(getTotalMovementsLabel());
		return paginatePanel;
	}

	private JPanel getButtonPanel() throws OHServiceException {
		JPanel buttonPanel = new JPanel(new WrapLayout());
		if (MainMenu.checkUserGrants("btnpharmstockcharge")) {
			buttonPanel.add(getChargeButton());
		}
		if (MainMenu.checkUserGrants("btnpharmstockdischarge")) {
			buttonPanel.add(getDischargeButton());
		}
		if (MainMenu.checkUserGrants("btnpharmstockcmovdelete")) {
			buttonPanel.add(getDeleteLastMovementButton());
		}

		buttonPanel.add(getExportToExcelButton());
		buttonPanel.add(getStockCardButton());
		buttonPanel.add(getStockLedgerButton());
		buttonPanel.add(getCloseButton());
		return buttonPanel;
	}

	private void applyFilter(int currentPage) {
		Integer medicalSelected = null;
		String medicalTypeSelected = null;
		String movementTypeSelected;
		String wardSelected = null;
		boolean dateOk = true;

		LocalDateTime movFrom = movDateFrom.getDateStartOfDay();
		LocalDateTime movTo = movDateTo.getDateStartOfDay();
		if ((movFrom == null) || (movTo == null)) {
			if (!((movFrom == null) && (movTo == null))) {
				MessageDialog.error(null, "angal.medicalstock.chooseavalidmovementdate.msg");
				dateOk = false;
			}
		} else if (movFrom.isAfter(movTo)) {
			MessageDialog.error(null, "angal.medicalstock.movementdatefromcannotbelaterthanmovementdateto");
			dateOk = false;
		}

		if (!isAutomaticLot()) {
			LocalDateTime prepFrom = lotPrepFrom.getDateStartOfDay();
			LocalDateTime prepTo = lotPrepTo.getDateStartOfDay();
			if ((prepFrom == null) || (prepTo == null)) {
				if (!((prepFrom == null) && (prepTo == null))) {
					MessageDialog.error(null, "angal.medicalstock.chooseavalidpreparationdate");
					dateOk = false;
				}
			} else if (prepFrom.isAfter(prepTo)) {
				MessageDialog.error(null, "angal.medicalstock.preparationdatefromcannotbelaterpreparationdateto");
				dateOk = false;
			}
		}

		if (dateOk) {
			if (medicalBox.isEnabled()) {
				if (!(medicalBox.getSelectedItem() instanceof String)) {
					medicalSelected = ((Medical) Objects.requireNonNull(medicalBox
						.getSelectedItem())).getCode();
				}
			} else {
				if (!(medicalTypeBox.getSelectedItem() instanceof String)) {
					medicalTypeSelected = ((MedicalType) Objects.requireNonNull(medicalTypeBox
						.getSelectedItem())).getCode();
				}
			}

			if (!(movementTypeBox.getSelectedItem() instanceof String)) {
				movementTypeSelected = ((MovementType) Objects.requireNonNull(movementTypeBox
					.getSelectedItem())).getCode();
			} else {
				movementTypeSelected = (String) movementTypeBox.getSelectedItem();
				if (movementTypeSelected.equals(TEXT_ALL)) {
					movementTypeSelected = null;
				} else if (movementTypeSelected.equals(TEXT_ALLCHARGES)) {
					movementTypeSelected = "+";
				} else if (movementTypeSelected.equals(TEXT_ALLDISCHARGES)) {
					movementTypeSelected = "-";
				}
			}

			if (!(wardBox.getSelectedItem() instanceof String)) {
				wardSelected = ((Ward) Objects.requireNonNull(wardBox.getSelectedItem()))
					.getCode();
			}

			if (!isAutomaticLot()) {
				model = new MovBrowserModel(medicalSelected,
					medicalTypeSelected, wardSelected, movementTypeSelected,
					movDateFrom.getDateStartOfDay(),
					movDateTo.getDateStartOfDay(),
					lotPrepFrom.getDateStartOfDay(),
					lotPrepTo.getDateStartOfDay(),
					lotDueFrom.getDateStartOfDay(),
					lotDueTo.getDateStartOfDay(),
					currentPage,
					PAGE_SIZE);
				try {
					totalMovements = movBrowserManager.getMovements(
						medicalSelected,
						medicalTypeSelected,
						wardSelected,
						movementTypeSelected,
						movDateFrom.getDateStartOfDay(),
						movDateTo.getDateStartOfDay(),
						lotPrepFrom.getDateStartOfDay(),
						lotPrepTo.getDateStartOfDay(),
						lotDueFrom.getDateStartOfDay(),
						lotDueTo.getDateStartOfDay()).size();
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}
			} else {
				model = new MovBrowserModel(medicalSelected,
					medicalTypeSelected, wardSelected, movementTypeSelected,
					movDateFrom.getDateStartOfDay(),
					movDateTo.getDateStartOfDay(),
					null,
					null,
					lotDueFrom.getDateStartOfDay(),
					lotDueTo.getDateStartOfDay(),
					currentPage,
					PAGE_SIZE);
				try {
					totalMovements = movBrowserManager.getMovements(
						medicalSelected,
						medicalTypeSelected,
						wardSelected,
						movementTypeSelected,
						movDateFrom.getDateStartOfDay(),
						movDateTo.getDateStartOfDay(),
						null,
						null,
						lotDueFrom.getDateStartOfDay(),
						lotDueTo.getDateStartOfDay()).size();
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}
			}

			totalMovementsLabel.setText(MessageBundle.getMessage("angal.medicalstock.totalmovement.txt") + ": " + totalMovements);
			pages = (int) Math.ceil((double) totalMovements / PAGE_SIZE);
			underLabel.setText("/ " + pages + " " + MessageBundle.getMessage("angal.common.pages.txt"));

			if (movements != null) {
				model.fireTableDataChanged();
				movTable.updateUI();
			}
			updateTotals();

			nextButton.setEnabled(currentPage < pages -1 && pages != 1);
			prevButton.setEnabled(currentPage > 0);
		}
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton(">");
			nextButton.setEnabled(currentPage < pages -1 && pages != 1);
			nextButton.addActionListener(actionEvent -> {
				if (currentPage < pages -1) {
					currentPage++;
					pagesCombo.setSelectedItem(currentPage + 1);
				}
			});
		}
		return nextButton;
	}

	private JButton getPrevButton() {
		if (prevButton == null) {
			prevButton = new JButton("<");
			prevButton.setEnabled(currentPage > 0);
			prevButton.addActionListener(actionEvent -> {
				if (currentPage > 0) {
					currentPage--;
					pagesCombo.setSelectedItem(currentPage + 1);
				}
			});
		}
		return prevButton;
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo == null) {
			pagesCombo = new JComboBox<>();
			pagesCombo.setPreferredSize(new Dimension(100, 25));
			for (int i = 0; i <= pages; i++) {
				pagesCombo.addItem(i + 1);
			}
			pagesCombo.addActionListener(actionEvent -> {
				if (pagesCombo.getItemCount() != 0) {
					if (pagesCombo.getSelectedItem() != null) {
						currentPage = (Integer) pagesCombo.getSelectedItem() - 1;
						applyFilter(currentPage);
					}
				}
			});
		}
		return pagesCombo;
	}

	private JLabel getUnderLabel() throws OHServiceException {
		if (underLabel == null) {
			underLabel = new JLabel("/ " + (pages + 1) + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			underLabel.setPreferredSize(new Dimension(60, 30));
		}
		return underLabel;
	}

	private JLabel getTotalMovementsLabel() throws OHServiceException {
		if (totalMovementsLabel == null) {
			totalMovementsLabel = new JLabel(MessageBundle.getMessage("angal.medicalstock.totalmovement.txt") +": "+ totalMovements);
		}
		return totalMovementsLabel;
	}

	private JButton getStockCardButton() {
		JButton stockCardButton = new JButton(MessageBundle.getMessage("angal.common.stockcard.btn"));
		stockCardButton.setMnemonic(MessageBundle.getMnemonic("angal.common.stockcard.btn.key"));
		stockCardButton.addActionListener(actionEvent -> {
			Medical medical = null;
			if (movTable.getSelectedRow() > -1) {
				Movement movement = (Movement) (model.getValueAt(movTable.getSelectedRow(), -1));
				medical = movement.getMedical();
			}

			StockCardDialog stockCardDialog = new StockCardDialog(this,
				medical,
				movDateFrom.getDateStartOfDay(),
				movDateTo.getDateStartOfDay());
			medical = stockCardDialog.getMedical();
			if (!stockCardDialog.isCancel()) {
				if (medical == null) {
					MessageDialog.error(this, "angal.medicalstock.chooseamedical.msg");
					return;
				}
				LocalDateTime dateFrom = stockCardDialog.getLocalDateTimeFrom();
				LocalDateTime dateTo = stockCardDialog.getLocalDateTimeTo();
				boolean toExcel = stockCardDialog.isExcel();
				new GenericReportPharmaceuticalStockCard("ProductLedger", dateFrom, dateTo, medical, null, toExcel);
			}
		});
		return stockCardButton;

	}

	private JButton getStockLedgerButton() {
		JButton stockLedgerButton = new JButton(MessageBundle.getMessage("angal.common.stockledger.btn"));
		stockLedgerButton.setMnemonic(MessageBundle.getMnemonic("angal.common.stockledger.btn.key"));
		stockLedgerButton.addActionListener(actionEvent -> {

			StockLedgerDialog stockCardDialog = new StockLedgerDialog(this, movDateFrom.getDateStartOfDay(), movDateTo.getDateStartOfDay());
			if (!stockCardDialog.isCancel()) {
				new GenericReportPharmaceuticalStockCard("ProductLedger_multi", stockCardDialog.getLocalDateTimeFrom(), stockCardDialog.getLocalDateTimeTo(),
					null, null, false);
			}
		});
		return stockLedgerButton;
	}

	private JPanel getTablesPanel() throws OHServiceException {
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(getTable(), BorderLayout.CENTER);
		tablePanel.add(getSubTablePanel(), BorderLayout.SOUTH);
		return tablePanel;
	}

	private JPanel getSubTablePanel() throws OHServiceException {
		JPanel subTablePanel = new JPanel();
		subTablePanel.setLayout(new BorderLayout());
		subTablePanel.add(getTableTotal(), BorderLayout.CENTER);
		subTablePanel.add(getPaginatePanel(), BorderLayout.SOUTH);
		return subTablePanel;
	}

	private JScrollPane getTable() {
		JScrollPane scrollPane = new JScrollPane(getMovTable());
		int totWidth = 0;
		for (int colWidth : pColumnWidth) {
			totWidth += colWidth;
		}
		scrollPane.setPreferredSize(new Dimension(totWidth, 450));
		return scrollPane;
	}

	private JScrollPane getTableTotal() {
		JScrollPane scrollPane = new JScrollPane(getJTableTotal());
		int totWidth = 0;
		for (int colWidth : pColumnWidth) {
			totWidth += colWidth;
		}
		scrollPane.setPreferredSize(new Dimension(totWidth, 20));
		scrollPane.setColumnHeaderView(null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		return scrollPane;
	}

	public void updateTotals() {
		if (jTableTotal == null) {
			return;
		}
		totalQti = 0;
		totalAmount = new BigDecimal(0);

		// quantity
		if (!medicalBox.getSelectedItem().equals(TEXT_ALL)) {
			for (Movement mov : movements) {
				if (mov.getType().getType().contains("+")) {
					totalQti += mov.getQuantity();
				} else {
					totalQti -= mov.getQuantity();
				}
			}
			jTableTotal.getModel().setValueAt(totalQti, 0, 4);
		} else {
			jTableTotal.getModel().setValueAt(MessageBundle.getMessage("angal.common.notapplicable.txt"), 0, 4);
		}

		// amount
		for (Movement mov : movements) {
			BigDecimal itemAmount = new BigDecimal(mov.getQuantity());
			if (GeneralData.LOTWITHCOST && mov.getLot().getCost() != null) {
				if (mov.getType().getType().contains("+")) {
					totalAmount = totalAmount.add(itemAmount.multiply(mov.getLot().getCost()));
				} else {
					totalQti -= mov.getQuantity();
					totalAmount = totalAmount.subtract(itemAmount.multiply(mov.getLot().getCost()));
				}
			}
		}
		jTableTotal.getModel().setValueAt(totalAmount, 0, 12);
	}

	private JPanel getFilterPanel() {
		JPanel filterPanel = new JPanel(); // the outer panel get maximum height (as per WEST from outer container)
		filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
			.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.medicalstock.selectionpanel")));
		filterPanel.add(getFilterContentPanel()); // the inner panel can use any layout
		return filterPanel;
	}

	private JPanel getFilterContentPanel() {
		JPanel filterContentPanel = new JPanel();
		filterContentPanel.setLayout(new BoxLayout(filterContentPanel, BoxLayout.Y_AXIS));
		filterContentPanel.add(getMedicalPanel());
		filterContentPanel.add(getMovementPanel());
		if (!isAutomaticLot()) {
			filterContentPanel.add(getLotPreparationDatePanel());
		}
		filterContentPanel.add(getLotDueDatePanel());
		JPanel filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		filterButtonPanel.add(getFilterButton());
		filterButtonPanel.add(getResetButton());
		filterButtonPanel.add(getJCheckBoxKeepFilter());
		filterContentPanel.add(filterButtonPanel);
		return filterContentPanel;
	}

	private JCheckBox getJCheckBoxKeepFilter() {
		if (jCheckBoxKeepFilter == null) {
			jCheckBoxKeepFilter = new JCheckBox(MessageBundle.getMessage("angal.medicalstock.keep"));
		}
		return jCheckBoxKeepFilter;
	}

	private JPanel getMedicalPanel() {
		JPanel medicalPanel = new JPanel();
		medicalPanel.setLayout(new BoxLayout(medicalPanel, BoxLayout.Y_AXIS));
		medicalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
			.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.medicalstock.pharmaceutical")));
		JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label1Panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.codeordescription.txt")));
		medicalPanel.add(label1Panel);
		medicalPanel.add(getMedicalSearchPanel());
		JPanel medicalDescPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		medicalDescPanel.add(getMedicalBox());
		medicalPanel.add(medicalDescPanel);
		JPanel label2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label2Panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.type")));
		medicalPanel.add(label2Panel);
		JPanel medicalTypePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		medicalTypePanel.add(getMedicalTypeBox());
		medicalPanel.add(medicalTypePanel);
		return medicalPanel;
	}

	private JPanel getMovementPanel() {
		JPanel movementPanel = new JPanel();
		movementPanel.setLayout(new BoxLayout(movementPanel, BoxLayout.Y_AXIS));
		movementPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
			.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.medicalstock.movement")));
		JPanel label3Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label3Panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.type")));
		movementPanel.add(label3Panel);
		JPanel movementTypePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		movementTypePanel.add(getMovementTypeBox());
		movementPanel.add(movementTypePanel);

		JPanel label2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label2Panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.ward")));
		movementPanel.add(label2Panel);
		JPanel wardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		wardPanel.add(getWardBox());
		movementPanel.add(wardPanel);

		JPanel label4Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		label4Panel.add(new JLabel(MessageBundle.getMessage("angal.common.date.txt")));
		movementPanel.add(label4Panel);

		JPanel moveFromPanel = new JPanel(new BorderLayout());
		JLabel labelFrom = new JLabel(FROM_LABEL);
		labelFrom.setVerticalAlignment(SwingConstants.TOP);
		moveFromPanel.add(labelFrom, BorderLayout.WEST);
		moveFromPanel.add(getMovDateFrom(), BorderLayout.EAST);
		movementPanel.add(moveFromPanel);
		JPanel moveToPanel = new JPanel(new BorderLayout());
		JLabel labelTo = new JLabel(TO_LABEL);
		labelTo.setVerticalAlignment(SwingConstants.TOP);
		moveToPanel.add(labelTo, BorderLayout.WEST);
		moveToPanel.add(getMovDateTo(), BorderLayout.EAST);
		movementPanel.add(moveToPanel);
		return movementPanel;
	}

	private JPanel getLotPreparationDatePanel() {
		JPanel lotPreparationDatePanel = new JPanel();
		lotPreparationDatePanel.setLayout(new BoxLayout(
			lotPreparationDatePanel, BoxLayout.Y_AXIS));
		lotPreparationDatePanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(Color.GRAY),
			MessageBundle.getMessage("angal.medicalstock.lotpreparationdate")));

		JPanel lotPrepFromPanel = new JPanel(new BorderLayout());
		lotPrepFromPanel.add(new JLabel(FROM_LABEL), BorderLayout.WEST);
		lotPrepFromPanel.add(getLotPrepFrom(), BorderLayout.EAST);
		lotPreparationDatePanel.add(lotPrepFromPanel);
		JPanel lotPrepToPanel = new JPanel(new BorderLayout());
		lotPrepToPanel.add(new JLabel(TO_LABEL), BorderLayout.WEST);
		lotPrepToPanel.add(getLotPrepTo(), BorderLayout.EAST);
		lotPreparationDatePanel.add(lotPrepToPanel);

		return lotPreparationDatePanel;
	}

	private JPanel getLotDueDatePanel() {
		JPanel lotDueDatePanel = new JPanel();
		lotDueDatePanel.setLayout(new BoxLayout(lotDueDatePanel,
			BoxLayout.Y_AXIS));
		lotDueDatePanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.medicalstock.lotduedate")));

		JPanel lotDueFromPanel = new JPanel(new BorderLayout());
		lotDueFromPanel.add(new JLabel(FROM_LABEL), BorderLayout.WEST);
		lotDueFromPanel.add(getLotDueFrom(), BorderLayout.EAST);
		lotDueDatePanel.add(lotDueFromPanel);
		JPanel lotDueToPanel = new JPanel(new BorderLayout());
		lotDueToPanel.add(new JLabel(TO_LABEL), BorderLayout.WEST);
		lotDueToPanel.add(getLotDueTo(), BorderLayout.EAST);
		lotDueDatePanel.add(lotDueToPanel);

		return lotDueDatePanel;
	}

	private JComboBox getWardBox() {
		wardBox = new JComboBox();
		wardBox.setPreferredSize(new Dimension(200, 25));
		wardBox.addItem(TEXT_ALL);
		List<Ward> wardsList;
		try {
			wardsList = wardBrowserManager.getWards();
			wardsList.sort(new Ward.WardDescriptionComparator());
		} catch (OHServiceException e) {
			wardsList = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
		for (Ward elem : wardsList) {
			wardBox.addItem(elem);
		}
		wardBox.setEnabled(false);
		return wardBox;
	}

	private JPanel getMedicalSearchPanel() {
		searchButton = new JButton();
		searchButton.setPreferredSize(new Dimension(20, 20));
		searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		searchButton.addActionListener(actionEvent -> {
			medicalBox.removeAllItems();
			List<Medical> medicals;
			try {
				medicals = medicalBrowsingManager.getMedicalsSortedByName();
			} catch (OHServiceException e1) {
				medicals = null;
				OHServiceExceptionUtil.showMessages(e1);
			}
			if (null != medicals) {
				List<Medical> results = getSearchMedicalsResults(searchTextField.getText(), medicals);
				int originalSize = medicals.size();
				int resultsSize = results.size();
				if (originalSize == resultsSize) {
					medicalBox.addItem(TEXT_ALL);
				}
				for (Medical aMedical : results) {
					medicalBox.addItem(aMedical);
				}
			}
		});

		searchTextField = new JTextField(10);
		searchTextField.setToolTipText(MessageBundle.getMessage("angal.medicalstock.pharmaceutical"));
		searchTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		searchPanel.add(searchTextField);
		searchPanel.add(searchButton);
		searchPanel.setPreferredSize(new Dimension(150, 25));
		return searchPanel;
	}

	private JComboBox getMedicalBox() {
		medicalBox = new JComboBox();
		medicalBox.setRenderer(new ToolTipListCellRenderer());
		medicalBox.setPreferredSize(new Dimension(200, 25));
		List<Medical> medical;
		try {
			medical = medicalBrowsingManager.getMedicalsSortedByName();
		} catch (OHServiceException e1) {
			medical = null;
			OHServiceExceptionUtil.showMessages(e1);
		}
		medicalBox.addItem(TEXT_ALL);
		if (null != medical) {
			for (Medical aMedical : medical) {
				medicalBox.addItem(aMedical);
			}
		}
		medicalBox.addActionListener(actionEvent -> {
			medicalBox.setToolTipText(getTooltipFromObject(medicalBox.getSelectedItem()));
		});
		medicalBox.addMouseListener(new MouseListener() {

			@Override
			public void mouseExited(MouseEvent e) {
				ToolTipManager.sharedInstance().setInitialDelay(defaultInitDelay);
				UIManager.put("ToolTip.background", defaultBackgroundColor);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				ToolTipManager.sharedInstance().setInitialDelay(0);
				UIManager.put("ToolTip.background", Color.white);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				medicalBox.setEnabled(true);
				medicalTypeBox.setSelectedIndex(0);
				medicalTypeBox.setEnabled(false);
			}
		});
		return medicalBox;
	}

	private JComboBox getMedicalTypeBox() {
		medicalTypeBox = new JComboBox();
		medicalTypeBox.setPreferredSize(new Dimension(200, 25));
		List<MedicalType> medical;

		medicalTypeBox.addItem(TEXT_ALL);

		try {
			medical = medicalTypeBrowserManager.getAllActiveMedicalType();

			for (MedicalType aMedicalType : medical) {
				medicalTypeBox.addItem(aMedicalType);
			}
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}

		medicalTypeBox.addMouseListener(new MouseListener() {

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				medicalTypeBox.setEnabled(true);
				medicalBox.setSelectedIndex(0);
				medicalBox.setEnabled(false);
			}
		});
		medicalTypeBox.setEnabled(false);
		return medicalTypeBox;
	}

	private JComboBox getMovementTypeBox() {
		movementTypeBox = new JComboBox();
		movementTypeBox.setPreferredSize(new Dimension(200, 25));
		List<MovementType> movementTypeList;
		try {
			movementTypeList = medicalDsrStockMovementTypeBrowserManager.getMedicalDsrStockMovementType();
		} catch (OHServiceException e1) {
			movementTypeList = null;
			OHServiceExceptionUtil.showMessages(e1);
		}
		movementTypeBox.addItem(TEXT_ALL);
		movementTypeBox.addItem(TEXT_ALLCHARGES);
		movementTypeBox.addItem(TEXT_ALLDISCHARGES);
		if (null != movementTypeList) {
			for (MovementType movementType : movementTypeList) {
				movementTypeBox.addItem(movementType);
			}
		}
		movementTypeBox.addActionListener(actionEvent -> {
			Object selectedMovementType = movementTypeBox.getSelectedItem();

			if (!(selectedMovementType instanceof String)) {
				MovementType selected = (MovementType) selectedMovementType;
				if (selected.getType().contains("-")) {
					wardBox.setEnabled(true);
				} else {
					wardBox.setSelectedIndex(0);
					wardBox.setEnabled(false);
				}
			} else if (TEXT_ALLDISCHARGES.equals(selectedMovementType)) {
				wardBox.setEnabled(true);
			} else {
				wardBox.setSelectedIndex(0);
				wardBox.setEnabled(false);
			}
		});
		return movementTypeBox;
	}

	private JTable getMovTable() {
		LocalDateTime now = TimeTools.getNow();
		LocalDateTime old = now.minusWeeks(1);

		model = new MovBrowserModel(false);
		movTable = new JTable(model);

		for (int i = 0; i < pColumns.length; i++) {
			movTable.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
			movTable.getColumnModel().getColumn(i).setPreferredWidth(pColumnWidth[i]);
			if (!pColumnVisible[i]) {
				movTable.getColumnModel().getColumn(i).setMinWidth(0);
				movTable.getColumnModel().getColumn(i).setMaxWidth(0);
				movTable.getColumnModel().getColumn(i).setWidth(0);
			}
		}

		TableColumn costColumn = movTable.getColumnModel().getColumn(11);
		costColumn.setCellRenderer(new DecimalFormatRenderer());

		TableColumn totalColumn = movTable.getColumnModel().getColumn(12);
		totalColumn.setCellRenderer(new DecimalFormatRenderer());

		return movTable;
	}

	private JTable getJTableTotal() {
		if (jTableTotal == null) {
			jTableTotal = new JTable();

			String currencyCod;
			try {
				currencyCod = hospitalBrowsingManager.getHospitalCurrencyCod();
			} catch (OHServiceException e) {
				currencyCod = null;
				OHServiceExceptionUtil.showMessages(e);
			}

			jTableTotal.setModel(new DefaultTableModel(
				new Object[][] {
					{ "", "", "", "<html><b>Total Qty: </b></html>", totalQti, "", "", "", "", "", "<html><b>"
						+ MessageBundle.getMessage("angal.common.total.txt") + ": </b></html>",
						currencyCod, totalAmount }
				}, new String[pColumns.length]) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTableTotal.setTableHeader(null);
			jTableTotal.setShowVerticalLines(false);
			jTableTotal.setShowHorizontalLines(false);
			jTableTotal.setRowSelectionAllowed(false);
			jTableTotal.setCellSelectionEnabled(false);
			jTableTotal.setColumnSelectionAllowed(false);

			for (int i = 0; i < pColumns.length; i++) {
				jTableTotal.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
				jTableTotal.getColumnModel().getColumn(i).setPreferredWidth(pColumnWidth[i]);
				if (!pColumnVisible[i]) {
					jTableTotal.getColumnModel().getColumn(i).setMinWidth(0);
					jTableTotal.getColumnModel().getColumn(i).setMaxWidth(0);
					jTableTotal.getColumnModel().getColumn(i).setWidth(0);
				}
			}

			jTableTotal.getColumnModel().getColumn(3).setCellRenderer(new RightAlignCellRenderer());
			TableColumn totalColumn = jTableTotal.getColumnModel().getColumn(4);
			totalColumn.setCellRenderer(new DecimalFormatRenderer());

			jTableTotal.getColumnModel().getColumn(11).setCellRenderer(new RightAlignCellRenderer());
			TableColumn totalAmountColumn = jTableTotal.getColumnModel().getColumn(12);
			totalAmountColumn.setCellRenderer(new DecimalFormatRenderer());
		}
		return jTableTotal;
	}

	private GoodDateChooser getMovDateFrom() {
		movDateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
		return movDateFrom;
	}

	private GoodDateChooser getMovDateTo() {
		movDateTo = new GoodDateChooser();
		return movDateTo;
	}

	private GoodDateChooser getLotPrepFrom() {
		lotPrepFrom = new GoodDateChooser(null);
		return lotPrepFrom;
	}

	private GoodDateChooser getLotPrepTo() {
		lotPrepTo = new GoodDateChooser(null);
		return lotPrepTo;
	}

	private GoodDateChooser getLotDueFrom() {
		lotDueFrom = new GoodDateChooser(null);
		return lotDueFrom;
	}

	private GoodDateChooser getLotDueTo() {
		lotDueTo = new GoodDateChooser(null);
		return lotDueTo;
	}

	/**
	 * This method creates the button that filters the data
	 * @return
	 */
	private JButton getFilterButton() {
		filterButton = new JButton(MessageBundle.getMessage("angal.common.filter.btn"));
		filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.filter.btn.key"));
		filterButton.addActionListener(actionEvent -> {
			Integer medicalSelected = null;
			String medicalTypeSelected = null;
			String movementTypeSelected = null;
			String wardSelected = null;
			boolean dateOk = true;

			LocalDateTime movFrom = movDateFrom.getDateStartOfDay();
			LocalDateTime movTo = movDateTo.getDateStartOfDay();
			if ((movFrom == null) || (movTo == null)) {
				if (!((movFrom == null) && (movTo == null))) {
					MessageDialog.error(null, "angal.medicalstock.chooseavalidmovementdate.msg");
					dateOk = false;
				}
			} else if (movFrom.isAfter(movTo)) {
				MessageDialog.error(null, "angal.medicalstock.movementdatefromcannotbelaterthanmovementdateto");
				dateOk = false;
			}

			if (!isAutomaticLot()) {
				LocalDateTime prepFrom = lotPrepFrom.getDateStartOfDay();
				LocalDateTime prepTo = lotPrepTo.getDateStartOfDay();
				if ((prepFrom == null) || (prepTo == null)) {
					if (!((prepFrom == null) && (prepTo == null))) {
						MessageDialog.error(null, "angal.medicalstock.chooseavalidpreparationdate");
						dateOk = false;
					}
				} else if (prepFrom.isAfter(prepTo)) {
					MessageDialog.error(null, "angal.medicalstock.preparationdatefromcannotbelaterpreparationdateto");
					dateOk = false;
				}
			}

			LocalDateTime dueFrom = lotDueFrom.getDateStartOfDay();
			LocalDateTime dueTo = lotDueTo.getDateStartOfDay();
			if ((dueFrom == null) || (dueTo == null)) {
				if (!((dueFrom == null) && (dueTo == null))) {
					MessageDialog.error(null, "angal.medicalstock.chooseavalidduedate.msg");
					dateOk = false;
				}
			} else if (dueFrom.isAfter(dueTo)) {
				MessageDialog.error(null, "angal.medicalstock.duedatefromcannotbelaterthanduedateto");
				dateOk = false;
			}

			if (!dateOk) {
				return;
			}

			if (medicalBox.isEnabled()) {
				if (!(medicalBox.getSelectedItem() instanceof String)) {
					medicalSelected = ((Medical) Objects.requireNonNull(medicalBox
						.getSelectedItem())).getCode();
				}
			} else {
				if (!(medicalTypeBox.getSelectedItem() instanceof String)) {
					medicalTypeSelected = ((MedicalType) Objects.requireNonNull(medicalTypeBox
						.getSelectedItem())).getCode();
				}
			}

			if (!(movementTypeBox.getSelectedItem() instanceof String)) {
				movementTypeSelected = ((MovementType) Objects.requireNonNull(movementTypeBox
					.getSelectedItem())).getCode();
			} else {
				movementTypeSelected = (String) movementTypeBox.getSelectedItem();
				if (movementTypeSelected.equals(TEXT_ALL)) {
					movementTypeSelected = null;
				} else if (movementTypeSelected.equals(TEXT_ALLCHARGES)) {
					movementTypeSelected = "+";
				} else if (movementTypeSelected.equals(TEXT_ALLDISCHARGES)) {
					movementTypeSelected = "-";
				}
			}

			if (!(wardBox.getSelectedItem() instanceof String)) {
				wardSelected = ((Ward) Objects.requireNonNull(wardBox.getSelectedItem()))
					.getCode();
			}

			if (!isAutomaticLot()) {
				try {
					totalMovements = movBrowserManager.getMovements(
						medicalSelected,
						medicalTypeSelected,
						wardSelected,
						movementTypeSelected,
						movDateFrom.getDateStartOfDay(),
						movDateTo.getDateStartOfDay(),
						lotPrepFrom.getDateStartOfDay(),
						lotPrepTo.getDateStartOfDay(),
						lotDueFrom.getDateStartOfDay(),
						lotDueTo.getDateStartOfDay()
					).size();
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}
			} else {
				try {
					totalMovements = movBrowserManager.getMovements(
						medicalSelected,
						medicalTypeSelected,
						wardSelected,
						movementTypeSelected,
						movDateFrom.getDateStartOfDay(),
						movDateTo.getDateStartOfDay(),
						null,
						null,
						lotDueFrom.getDateStartOfDay(),
						lotDueTo.getDateStartOfDay()
					).size();
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}
			}

			totalMovementsLabel.setText(MessageBundle.getMessage("angal.medicalstock.totalmovement.txt") + ": " + totalMovements);
			pages = (int) Math.ceil((double) totalMovements / PAGE_SIZE);
			underLabel.setText("/ " + pages + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			currentPage = 0;

			pagesCombo.removeAllItems();
			for (int i = 0; i < pages; i++) {
				pagesCombo.addItem(i + 1);
			}

			pagesCombo.setSelectedItem(1);

			if (movements != null) {
				model.fireTableDataChanged();
				movTable.updateUI();
			}

			updateTotals();

			nextButton.setEnabled(currentPage < pages -1 && pages != 1);
			prevButton.setEnabled(currentPage > 0);

		});
		return filterButton;
	}

	private JButton getResetButton() {
		resetButton = new JButton(MessageBundle.getMessage("angal.medicalstock.reset.btn"));
		resetButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.reset.btn.key"));
		resetButton.addActionListener(actionEvent -> {

			searchTextField.setText("");
			searchButton.doClick();
			medicalTypeBox.setSelectedIndex(0);
			movementTypeBox.setSelectedItem(MessageBundle.getMessage("angal.common.all.txt"));
			wardBox.setSelectedItem(MessageBundle.getMessage("angal.common.all.txt"));

			movDateFrom.setDate(LocalDate.now().minusWeeks(1));
			movDateTo.setDate(LocalDate.now());

			if (!isAutomaticLot()) {
				lotPrepFrom.setDate(null);
				lotPrepTo.setDate(null);
			}
			lotDueFrom.setDate(null);
			lotDueTo.setDate(null);

			if (jCheckBoxKeepFilter.isSelected()) {
				filterButton.doClick();
			}
		});
		return resetButton;

	}

	/**
	 * This method creates the button that close the mask
	 * @return
	 */
	private JButton getCloseButton() {
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		return closeButton;
	}

	/**
	 * This method creates the button that load the charging movement mask
	 * @return
	 */
	private JButton getChargeButton() {
		JButton chargeButton = new JButton(MessageBundle.getMessage("angal.medicalstock.charge.btn"));
		chargeButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.charge.btn.key"));
		chargeButton.addActionListener(actionEvent -> {
			new MovStockMultipleCharging(myFrame);
			model = new MovBrowserModel(true);
			movTable.updateUI();
			updateTotals();
			if (jCheckBoxKeepFilter.isSelected()) {
				filterButton.doClick();
			}
		});
		return chargeButton;
	}

	/**
	 * This method creates the button that load the discharging movement mask
	 * @return
	 */
	private JButton getDischargeButton() {
		JButton dischargeButton = new JButton(MessageBundle.getMessage("angal.medicalstock.discharge.btn"));
		dischargeButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.discharge.btn.key"));
		dischargeButton.addActionListener(actionEvent -> {
			new MovStockMultipleDischarging(myFrame);
			model = new MovBrowserModel(true);
			movTable.updateUI();
			updateTotals();
			if (jCheckBoxKeepFilter.isSelected()) {
				filterButton.doClick();
			}
		});
		return dischargeButton;
	}

	/**
	 * This method creates the button that delete the last stock {@link Movement)
	 * @return
	 */
	private JButton getDeleteLastMovementButton() {
		JButton deleteMovementButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteMovementButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteMovementButton.addActionListener(actionEvent -> {

			if (movTable.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.medicalstock.pleaseselectonlyonemovement.msg");
				return;
			}
			int selectedRow = movTable.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.medicalstock.pleaseselectamovement.msg");
				return;
			}
			Movement selectedMovement = (Movement) movTable.getValueAt(selectedRow, -1);
			try {
				Movement lastMovement = movBrowserManager.getLastMovement();
				if (lastMovement.getCode() == selectedMovement.getCode()) {
					int delete = MessageDialog.yesNo(null, "angal.medicalstock.doyoureallywanttodeletethismovement.msg");
					if (delete == JOptionPane.YES_OPTION) {
						movBrowserManager.deleteLastMovement(lastMovement);
					} else {
						return;
					}
				} else {
					MessageDialog.error(this, "angal.medicalstock.onlythelastmovementcanbedeleted.msg");
					return;
				}
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
				return;
			}
			MessageDialog.info(this, "angal.medicalstock.deletemovementsuccess.msg");
			filterButton.doClick();
		});
		return deleteMovementButton;

	}

	private JButton getExportToExcelButton() {
		JButton exportToExcel = new JButton(MessageBundle.getMessage("angal.medicalstock.exporttoexcel.btn"));
		exportToExcel.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.exporttoexcel.btn.key"));
		exportToExcel.addActionListener(actionEvent -> {

			String fileName = compileFileName();
			File defaultFileName = new File(fileName);
			JFileChooser fcExcel = ExcelExporter.getJFileChooserExcel(defaultFileName);

			int iRetVal = fcExcel.showSaveDialog(this);
			if (iRetVal == JFileChooser.APPROVE_OPTION) {
				File exportFile = fcExcel.getSelectedFile();
				if (!exportFile.getName().endsWith(".xls") && !exportFile.getName().endsWith(".xlsx")) {
					if (fcExcel.getFileFilter().getDescription().contains("*.xlsx")) {
						exportFile = new File(exportFile.getAbsoluteFile() + ".xlsx");
					} else {
						exportFile = new File(exportFile.getAbsoluteFile() + ".xls");
					}
				}
				ExcelExporter xlsExport = new ExcelExporter();
				JTable movTableCopy = new JTable(movTable.getModel());
				movTableCopy.setModel(new MovBrowserModel(null, null, null, null, movDateFrom.getDateStartOfDay(),
					movDateTo.getDateStartOfDay(),
					lotPrepFrom.getDateStartOfDay(),
					lotPrepTo.getDateStartOfDay(),
					lotDueFrom.getDateStartOfDay(),
					lotDueTo.getDateStartOfDay()));
				try {
					if (exportFile.getName().endsWith(".xlsx")) {
						xlsExport.exportTableToExcel(movTableCopy, exportFile);
					} else {
						xlsExport.exportTableToExcelOLD(movTableCopy, exportFile);
					}
				} catch (IOException exc) {
					JOptionPane.showMessageDialog(this,
						exc.getMessage(),
						MessageBundle.getMessage("angal.messagedialog.error.title"),
						JOptionPane.PLAIN_MESSAGE);
					LOGGER.info("Export to excel error : {}", exc.getMessage());
				}

			}
		});
		return exportToExcel;
	}

	private String compileFileName() {
		StringBuilder filename = new StringBuilder("Stock Ledger");
		if (medicalBox.isEnabled()
			&& !medicalBox.getSelectedItem().equals(
			TEXT_ALL)) {

			filename.append('_').append(medicalBox.getSelectedItem());
		}
		if (medicalTypeBox.isEnabled()
			&& !medicalTypeBox.getSelectedItem().equals(
			TEXT_ALL)) {

			filename.append('_').append(medicalTypeBox.getSelectedItem());
		}
		if (movementTypeBox.isEnabled() &&
			!movementTypeBox.getSelectedItem().equals(TEXT_ALL)) {
			filename.append('_').append(movementTypeBox.getSelectedItem());
		}
		if (wardBox.isEnabled() &&
			!wardBox.getSelectedItem().equals(TEXT_ALL)) {
			filename.append('_').append(wardBox.getSelectedItem());
		}
		filename.append('_').append(TimeTools.formatDateTime(movDateFrom.getDateStartOfDay(), DATE_FORMAT_YYYYMMDD))
			.append('_').append(TimeTools.formatDateTime(movDateTo.getDateStartOfDay(), DATE_FORMAT_YYYYMMDD));
		return filename.toString();
	}

	private List<Medical> getSearchMedicalsResults(String s, List<Medical> medicalsList) {
		String query = s.trim();
		List<Medical> results = new ArrayList<>();
		for (Medical medoc : medicalsList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String code = medoc.getProdCode().toLowerCase();
				String description = medoc.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (code.contains(pattern.toLowerCase()) || description.contains(pattern.toLowerCase())) {
						patternFound = true;
						// It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(medoc);
				}
			} else {
				results.add(medoc);
			}
		}
		return results;
	}
	private String formatDate(LocalDateTime time) {
		if (time == null) {
			return MessageBundle.getMessage("angal.medicalstock.nodate");
		}
		return DATE_FORMATTER.format(time);
	}
	private String formatDateTime(LocalDateTime time) {
		if (time == null) {
			return MessageBundle.getMessage("angal.medicalstock.nodate");
		}
		return DATE_TIME_FORMATTER.format(time);
	}
	private String getTooltipFromObject(Object value) {
		String tooltip = "";
		if (value instanceof Medical) {
			tooltip = ((Medical) value).getDescription();
		} else if (value instanceof String) {
			tooltip = (String) value;
		}
		return tooltip;
	}

	/**
	 * This is the table model
	 */
	class MovBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MovBrowserModel(boolean initialize) {
			if (initialize) {
				LocalDateTime now = TimeTools.getNow();
				LocalDateTime old = now.minusWeeks(1);

				new MovBrowserModel(null, null, null, null, old, now, null, null, null, null);
				updateTotals();
			}
		}

		public MovBrowserModel(Integer medicalCode, String medicalType, String ward, String movType, LocalDateTime movFrom, LocalDateTime movTo,
			LocalDateTime lotPrepFrom, LocalDateTime lotPrepTo, LocalDateTime lotDueFrom, LocalDateTime lotDueTo, int currentPage, int pageSize) {
			try {
				movements = movBrowserManager.getMovements(medicalCode, medicalType, ward,
					movType, movFrom, movTo, lotPrepFrom, lotPrepTo,
					lotDueFrom, lotDueTo, currentPage, PAGE_SIZE);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

			updateTotals();
		}

		public MovBrowserModel(Integer medicalCode, String medicalType, String ward, String movType, LocalDateTime movFrom, LocalDateTime movTo,
			LocalDateTime lotPrepFrom, LocalDateTime lotPrepTo, LocalDateTime lotDueFrom, LocalDateTime lotDueTo) {
			try {
				movements = movBrowserManager.getMovements(medicalCode, medicalType, ward,
					movType, movFrom, movTo, lotPrepFrom, lotPrepTo,
					lotDueFrom, lotDueTo);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

			updateTotals();
		}

		@Override
		public int getRowCount() {
			if (movements == null) {
				return 0;
			}
			return movements.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		/**
		 * Note: We must get the objects in a reversed way because of the query
		 * @see org.isf.medicalstock.service.MedicalStockIoOperations
		 */
		@Override
		public Object getValueAt(int r, int c) {
			if (movements.isEmpty() || r >= movements.size()) {
				return null;
			}
			Movement movement = movements.get(r);
			Medical medical = movement.getMedical();
			Lot lot = movement.getLot();
			BigDecimal cost = lot.getCost();
			int qty = movement.getQuantity();
			int col = -1;
			if (c == col) {
				return movement;
			} else if (c == ++col) {
				return movement.getRefNo();
			} else if (c == ++col) {
				return formatDateTime(movement.getDate());
			} else if (c == ++col) {
				return movement.getType().getCategory();
			} else if (c == ++col) {
				return movement.getType().toString();
			} else if (c == ++col) {
				Ward ward = movement.getWard();
				if (ward != null) {
					return ward;
				} else {
					return "";
				}
			} else if (c == ++col) {
				return qty;
			} else if (c == ++col) {
				return medical.getProdCode();
			} else if (c == ++col) {
				return medical.getDescription();
			} else if (c == ++col) {
				return medical.getType().getDescription();
			} else if (c == ++col) {
				if (isAutomaticLot()) {
					return MessageBundle.getMessage("angal.medicalstock.generated");
				} else {
					return lot;
				}
			} else if (c == ++col) {
				return formatDate(lot.getPreparationDate());
			} else if (c == ++col) {
				return formatDate(lot.getDueDate());
			} else if (c == ++col) {
				Supplier origin = movement.getOrigin();
				return origin != null ? supMap.get(origin.getSupId()) : "";
			} else if (c == ++col) {
				return cost;
			} else if (c == ++col && cost != null) {
				return cost.multiply(new BigDecimal(qty));
			} else if (c == ++col) {
				return movement.getCreatedBy();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

	}

	class EnabledTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			if (pColumnBold[column]) {
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}

	class RightAlignCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(SwingConstants.RIGHT);
			return cell;
		}
	}

	class DecimalFormatRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private final DecimalFormat formatter10 = new DecimalFormat("#,##0.00");
		private final DecimalFormat formatter1 = new DecimalFormat("#,##0");

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			if (column == 4 && value instanceof Number) {
				value = formatter1.format(value);
			}
			if (column == 11 && value instanceof Number) {
				value = formatter10.format(value);
			}
			if (column == 12 && value instanceof Number) {
				value = formatter10.format(value);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	public class ToolTipListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// I'd extract the basic "text" representation of the value
			// and pass that to the super call, which will apply it to the
			// JLabel via the setText method, otherwise it will use the
			// objects toString method to generate a representation
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			String tooltip = getTooltipFromObject(value);
			setToolTipText(tooltip);
			return this;
		}
	}
}
