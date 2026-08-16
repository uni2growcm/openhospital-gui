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
package org.isf.stockorder.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.menu.manager.Context;
import org.isf.stockorder.manager.StockOrderBrowserManager;
import org.isf.stockorder.model.StockOrder;
import org.isf.stockorder.model.StockOrderRow;
import org.isf.stockorder.model.StockOrderStatus;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.RequestFocusListener;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

/**
 * List of {@link StockOrder}s ("fiches de commande"): search/filter, create/edit/delete order forms,
 * and confirm one to create the corresponding stock-in (charging) movement.
 */
public class StockOrderBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(StockOrderBrowser.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final int PAGE_SIZE = 20;

	private final StockOrderBrowserManager stockOrderBrowserManager = Context.getApplicationContext().getBean(StockOrderBrowserManager.class);
	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private final SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);

	private JTable table;
	private OrdersModel model;
	private List<StockOrder> orders = new ArrayList<>();

	private JTextField searchField;
	private JComboBox<String> statusFilterComboBox;
	private JComboBox<Supplier> supplierFilterComboBox;
	private GoodDateChooser dateFromChooser;
	private GoodDateChooser dateToChooser;

	private int startIndex;
	private int totalPages = 1;
	private boolean suppressPageComboEvents;
	private JButton previousPageButton;
	private JButton nextPageButton;
	private final JComboBox<Integer> pagesComboBox = new JComboBox<>();
	private final JLabel ofPagesLabel = new JLabel(MessageBundle.formatMessage("angal.common.pages.fmt.txt", 1));

	private final String[] columnNames = {
					MessageBundle.getMessage("angal.common.reference.label").toUpperCase(),
					MessageBundle.getMessage("angal.common.date.col").toUpperCase(),
					MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
					MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
					MessageBundle.getMessage("angal.stockorder.medical.col").toUpperCase(),
					MessageBundle.getMessage("angal.stockorder.qtytoorder.col").toUpperCase(),
					MessageBundle.getMessage("angal.stockorder.supplier.label").toUpperCase(),
					MessageBundle.getMessage("angal.inventory.status.label").toUpperCase()
	};

	public StockOrderBrowser() {
		setTitle(MessageBundle.getMessage("angal.stockorder.stockorderbrowser.title"));
		setContentPane(buildContentPane());
		setMinimumSize(new java.awt.Dimension(900, 500));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private JPanel buildContentPane() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(getFilterPanel(), BorderLayout.WEST);
		contentPane.add(getTablePanel(), BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		loadOrders(0);
		return contentPane;
	}

	private JPanel getFilterPanel() {
		JPanel filterPanel = new JPanel();
		filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
						javax.swing.BorderFactory.createLineBorder(Color.GRAY),
						MessageBundle.getMessage("angal.medicalstock.selectionpanel")));
		filterPanel.add(getFilterContentPanel());
		return filterPanel;
	}

	private JPanel getFilterContentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

		panel.add(getFilterFieldPanel("angal.common.search.txt", getSearchField()));
		panel.add(getFilterFieldPanel("angal.inventory.status.label", getStatusFilterComboBox()));
		panel.add(getFilterFieldPanel("angal.stockorder.supplier.label", getSupplierFilterComboBox()));
		panel.add(getFilterFieldPanel("angal.common.datefrom.label", getDateFromChooser()));
		panel.add(getFilterFieldPanel("angal.common.dateto.label", getDateToChooser()));

		JButton refreshButton = new JButton(MessageBundle.getMessage("angal.common.refresh.btn"));
		refreshButton.setMnemonic(MessageBundle.getMnemonic("angal.common.refresh.btn.key"));
		refreshButton.addActionListener(actionEvent -> loadOrders(0));
		JPanel refreshPanel = new JPanel();
		refreshPanel.add(refreshButton);
		panel.add(refreshPanel);

		return panel;
	}

	private JPanel getFilterFieldPanel(String labelKey, java.awt.Component field) {
		JPanel panel = new JPanel();
		panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
		JLabel label = new JLabel(MessageBundle.getMessage(labelKey));
		label.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		if (field instanceof javax.swing.JComponent jComponent) {
			jComponent.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
		}
		panel.add(label);
		panel.add(field);
		return panel;
	}

	private JTextField getSearchField() {
		searchField = new JTextField(15);
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.typeacodeoradescriptionandpressenter"), searchField,
						Show.FOCUS_LOST);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
		suggestion.setForeground(Color.GRAY);
		searchField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loadOrders(0);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		return searchField;
	}

	private JComboBox<String> getStatusFilterComboBox() {
		statusFilterComboBox = new JComboBox<>();
		statusFilterComboBox.addItem(MessageBundle.getMessage("angal.common.all.txt"));
		for (StockOrderStatus status : StockOrderStatus.values()) {
			statusFilterComboBox.addItem(MessageBundle.getMessage("angal.stockorder.status." + status.name() + ".txt"));
		}
		statusFilterComboBox.addActionListener(actionEvent -> loadOrders(0));
		return statusFilterComboBox;
	}

	private JComboBox<Supplier> getSupplierFilterComboBox() {
		supplierFilterComboBox = new JComboBox<>();
		supplierFilterComboBox.addItem(null);
		try {
			List<Supplier> suppliers = supplierBrowserManager.getList();
			suppliers.sort(new Supplier.SupplierNameComparator());
			for (Supplier supplier : suppliers) {
				supplierFilterComboBox.addItem(supplier);
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		supplierFilterComboBox.addActionListener(actionEvent -> loadOrders(0));
		return supplierFilterComboBox;
	}

	private GoodDateChooser getDateFromChooser() {
		dateFromChooser = new GoodDateChooser(null, true, true);
		dateFromChooser.addDateChangeListener(event -> loadOrders(0));
		return dateFromChooser;
	}

	private GoodDateChooser getDateToChooser() {
		dateToChooser = new GoodDateChooser(null, true, true);
		dateToChooser.addDateChangeListener(event -> loadOrders(0));
		return dateToChooser;
	}

	private JPanel getTablePanel() {
		model = new OrdersModel();
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}

	private void loadOrders(int page) {
		try {
			String search = searchField.getText() == null || searchField.getText().isBlank() ? null : searchField.getText().trim();
			StockOrderStatus status = statusFilterComboBox.getSelectedIndex() > 0
							? StockOrderStatus.values()[statusFilterComboBox.getSelectedIndex() - 1]
							: null;
			Supplier supplier = (Supplier) supplierFilterComboBox.getSelectedItem();
			Integer supplierId = supplier != null ? supplier.getSupId() : null;
			LocalDateTime dateFrom = dateFromChooser.getDateStartOfDay();
			LocalDateTime dateTo = dateToChooser.getDateEndOfDay();

			Page<StockOrder> result = stockOrderBrowserManager.getOrdersFiltered(search, status, supplierId, dateFrom, dateTo, page, PAGE_SIZE);
			orders = new ArrayList<>(result.getContent());
			totalPages = Math.max(result.getTotalPages(), 1);
			startIndex = page * PAGE_SIZE;
		} catch (OHServiceException e) {
			orders = new ArrayList<>();
			totalPages = 1;
			startIndex = 0;
			OHServiceExceptionUtil.showMessages(e);
		}
		if (model != null) {
			model.fireTableDataChanged();
		}
		initializePagesCombo();
	}

	private void initializePagesCombo() {
		suppressPageComboEvents = true;
		pagesComboBox.removeAllItems();
		for (int i = 1; i <= totalPages; i++) {
			pagesComboBox.addItem(i);
		}
		pagesComboBox.setSelectedItem(startIndex / PAGE_SIZE + 1);
		if (previousPageButton != null) {
			previousPageButton.setEnabled(startIndex > 0);
		}
		if (nextPageButton != null) {
			nextPageButton.setEnabled(startIndex / PAGE_SIZE < totalPages - 1);
		}
		ofPagesLabel.setText(MessageBundle.formatMessage("angal.common.pages.fmt.txt", totalPages));
		suppressPageComboEvents = false;
	}

	private JPanel getPaginationPanel() {
		JPanel panel = new JPanel();
		previousPageButton = new JButton(MessageBundle.getMessage("angal.inventory.arrowprevious.btn"));
		previousPageButton.addActionListener(actionEvent -> {
			int page = startIndex / PAGE_SIZE;
			if (page > 0) {
				loadOrders(page - 1);
			}
		});
		nextPageButton = new JButton(MessageBundle.getMessage("angal.inventory.arrownext.btn"));
		nextPageButton.addActionListener(actionEvent -> {
			int page = startIndex / PAGE_SIZE;
			if (page < totalPages - 1) {
				loadOrders(page + 1);
			}
		});
		pagesComboBox.addItemListener(itemEvent -> {
			if (suppressPageComboEvents || itemEvent.getStateChange() != ItemEvent.SELECTED) {
				return;
			}
			loadOrders((Integer) pagesComboBox.getSelectedItem() - 1);
		});
		panel.add(previousPageButton);
		panel.add(pagesComboBox);
		panel.add(ofPagesLabel);
		panel.add(nextPageButton);
		return panel;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(getPaginationPanel(), BorderLayout.NORTH);

		JPanel actionsPanel = new JPanel();

		JButton newButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		newButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		newButton.addActionListener(actionEvent -> {
			StockOrderQuickCreate quickCreate = new StockOrderQuickCreate(this);
			if (quickCreate.isSaved()) {
				loadOrders(0);
			}
		});
		actionsPanel.add(newButton);

		JButton editButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		editButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		editButton.addActionListener(actionEvent -> {
			StockOrder selected = getSelectedOrder();
			if (selected == null) {
				return;
			}
			if (selected.getStatus() != StockOrderStatus.open) {
				MessageDialog.error(this, "angal.stockorder.ordernoteditable.msg");
				return;
			}
			StockOrderEdit edit = new StockOrderEdit(this, selected);
			if (edit.isSaved()) {
				loadOrders(startIndex / PAGE_SIZE);
			}
		});
		actionsPanel.add(editButton);

		JButton deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteButton.addActionListener(actionEvent -> {
			StockOrder selected = getSelectedOrder();
			if (selected == null) {
				return;
			}
			int confirm = MessageDialog.yesNo(this, "angal.stockorder.deleteorder.confirm.msg");
			if (confirm == JOptionPane.YES_OPTION) {
				try {
					stockOrderBrowserManager.deleteOrder(selected);
					loadOrders(startIndex / PAGE_SIZE);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		});
		actionsPanel.add(deleteButton);

		JButton confirmButton = new JButton(MessageBundle.getMessage("angal.stockorder.confirm.btn"));
		confirmButton.setMnemonic(MessageBundle.getMnemonic("angal.stockorder.confirm.btn.key"));
		confirmButton.addActionListener(actionEvent -> confirmOrder());
		actionsPanel.add(confirmButton);

		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		actionsPanel.add(closeButton);

		buttonPanel.add(actionsPanel, BorderLayout.SOUTH);
		return buttonPanel;
	}

	private StockOrder getSelectedOrder() {
		int row = table.getSelectedRow();
		if (row < 0) {
			MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			return null;
		}
		return orders.get(row);
	}

	private void confirmOrder() {
		StockOrder selected = getSelectedOrder();
		if (selected == null) {
			return;
		}
		if (selected.getStatus() != StockOrderStatus.open) {
			MessageDialog.error(this, "angal.stockorder.ordernoteditable.msg");
			return;
		}
		int ok = MessageDialog.yesNo(this, "angal.stockorder.confirm.question.msg");
		if (ok != JOptionPane.YES_OPTION) {
			return;
		}

		// the stock entry is dated when the goods are actually received (now), not when the order was
		// originally placed with the supplier - otherwise confirming an older order would try to backdate
		// the movement before more recent ones, which is rejected.
		LocalDateTime receptionDate = TimeTools.getNow();

		List<Movement> movements = new ArrayList<>();
		for (StockOrderRow row : selected.getRows()) {
			Lot lot = askLot(row.getMedical());
			if (lot == null) {
				return; // user cancelled the lot dialog for a row: abort the whole confirmation
			}
			movements.add(new Movement(row.getMedical(), selected.getChargeType(), null, lot,
							receptionDate, row.getQuantity(), selected.getSupplier(), selected.getRefNo()));
		}

		try {
			List<Movement> inserted = stockOrderBrowserManager.confirmOrder(selected, movements);
			MessageDialog.info(this, "angal.stockorder.confirm.success.fmt.msg", inserted.size());
			loadOrders(startIndex / PAGE_SIZE);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private boolean isAutomaticLotIn() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	private Lot askLot(Medical medical) {
		JTextField lotCodeField = new JTextField(15);
		lotCodeField.addAncestorListener(new RequestFocusListener());
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotCodeField);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		suggestion.setForeground(Color.GRAY);

		LocalDate now = LocalDate.now();
		GoodDateChooser preparationDateChooser = new GoodDateChooser(now);
		GoodDateChooser expireDateChooser = new GoodDateChooser(now);
		boolean automatic = isAutomaticLotIn();
		boolean withCost = GeneralData.LOTWITHCOST;

		JTextField costField = new JTextField(15);

		int rows = (automatic ? 1 : 2) + (withCost ? 1 : 0) + 1;
		JPanel panel = new JPanel(new GridLayout(rows, 2));
		panel.add(new JLabel(medical.toString()));
		panel.add(new JLabel());
		if (!automatic) {
			panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb")));
			panel.add(lotCodeField);
		}
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
		panel.add(expireDateChooser);
		if (withCost) {
			panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost")));
			panel.add(costField);
		}

		while (true) {
			int ok = JOptionPane.showConfirmDialog(this, panel,
							MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"),
							JOptionPane.OK_CANCEL_OPTION);
			if (ok != JOptionPane.OK_OPTION) {
				return null;
			}

			LocalDateTime preparationDate = TimeTools.getNow();
			LocalDateTime dueDate = expireDateChooser.getDateStartOfDay();
			if (dueDate == null) {
				MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
				continue;
			}
			if (dueDate.isBefore(preparationDate)) {
				MessageDialog.error(this, "angal.medicalstock.multiplecharging.expirydatebeforepreparationdate");
				continue;
			}

			BigDecimal cost = BigDecimal.ZERO;
			if (withCost) {
				try {
					cost = new BigDecimal(costField.getText().trim().replace(',', '.'));
				} catch (NumberFormatException nfe) {
					cost = BigDecimal.ZERO;
				}
				if (cost.compareTo(BigDecimal.ZERO) <= 0) {
					MessageDialog.error(this, "angal.medicalstock.multiplecharging.zerocostsarenotallowed.msg");
					continue;
				}
			}

			String lotCode = automatic ? "" : lotCodeField.getText().trim();
			try {
				if (!automatic) {
					if (lotCode.isEmpty()) {
						MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
						continue;
					}
					if (movStockInsertingManager.lotExists(lotCode)) {
						MessageDialog.error(this, "angal.medicalstock.multiplecharging.theinsertedlotcodealreaedyexists.msg");
						continue;
					}
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				return null;
			}

			return new Lot(medical, lotCode, preparationDate, dueDate, cost);
		}
	}

	private class OrdersModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return orders == null ? 0 : orders.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Object getValueAt(int row, int column) {
			StockOrder order = orders.get(row);
			StockOrderRow firstRow = order.getRows().isEmpty() ? null : order.getRows().get(0);
			Medical medical = firstRow != null ? firstRow.getMedical() : null;
			return switch (column) {
				case 0 -> order.getRefNo();
				case 1 -> order.getOrderDate() != null ? DATE_FORMATTER.format(order.getOrderDate()) : "";
				case 2 -> medical != null && medical.getType() != null ? medical.getType().getDescription() : "";
				case 3 -> medical != null ? medical.getProdCode() : "";
				case 4 -> medical != null ? medical.getDescription() : "";
				case 5 -> firstRow != null ? firstRow.getQuantity() : "";
				case 6 -> order.getSupplier() != null ? order.getSupplier().toString() : "";
				case 7 -> MessageBundle.getMessage("angal.stockorder.status." + order.getStatus().name() + ".txt");
				default -> null;
			};
		}
	}
}
