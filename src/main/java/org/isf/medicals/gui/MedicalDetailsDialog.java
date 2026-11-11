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
package org.isf.medicals.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class MedicalDetailsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final MovStockInsertingManager movStockInsertingManager =
			Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private final WardBrowserManager wardBrowserManager =
			Context.getApplicationContext().getBean(WardBrowserManager.class);
	private final MovWardBrowserManager movWardBrowserManager =
			Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private final HospitalBrowsingManager hospitalBrowsingManager =
			Context.getApplicationContext().getBean(HospitalBrowsingManager.class);

	private final Medical medical;
	private String currencyCod = "";

	// Data
	private List<Lot> allLots = new ArrayList<>();
	private List<Lot> filteredLots = new ArrayList<>();
	private List<WardStockRow> allWardStockRows = new ArrayList<>();
	private List<WardStockRow> filteredWardStockRows = new ArrayList<>();
	private Map<String, Double> wardStockTotals = new HashMap<>();
	private List<Ward> activeWards = new ArrayList<>();

	// Aggregated quantities
	private int totalMainStoreQty = 0;
	private double totalWardsQty = 0.0;
	private double totalOverallQty = 0.0;
	private double totalExpiredQty = 0.0;
	private double totalValidQty = 0.0;

	// UI Components
	private JPanel headerPanel;
	private JPanel badgesContainer;
	private JPanel wardsChipsPanel;
	private JTabbedPane tabbedPane;
	private JLabel critLevelTitleLabel;
	private JLabel critLevelValueLabel;

	// Lots Tab UI
	private JComboBox<String> lotStatusFilterCombo;
	private JCheckBox lotHideZeroCheck;
	private JTextField lotSearchField;
	private JTable lotsTable;
	private LotsTableModel lotsTableModel;

	// Services Tab UI
	private JComboBox<Object> wardFilterCombo;
	private JCheckBox wardHideZeroCheck;
	private JTable wardStockTable;
	private WardStockTableModel wardStockTableModel;
	private JLabel wardTotalLabel;

	public MedicalDetailsDialog(Window owner, Medical medical) {
		super(owner, ModalityType.APPLICATION_MODAL);
		this.medical = medical;

		try {
			currencyCod = hospitalBrowsingManager.getHospitalCurrencyCod();
			if (currencyCod == null) {
				currencyCod = "";
			}
		} catch (Exception e) {
			currencyCod = "";
		}

		loadData();
		initComponents();
		pack();
		setSize(980, 650);
		setMinimumSize(new Dimension(850, 550));
		setLocationRelativeTo(owner);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (wardsChipsPanel != null) {
					wardsChipsPanel.revalidate();
				}
			}
		});
	}

	private void loadData() {
		loadLotsData();
		loadWardsData();
		computeStockSummary();
	}

	private void loadLotsData() {
		try {
			allLots = movStockInsertingManager.getLotByMedical(medical, false);
			if (allLots == null) {
				allLots = new ArrayList<>();
			}
		} catch (OHServiceException e) {
			allLots = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void loadWardsData() {
		allWardStockRows.clear();
		wardStockTotals.clear();
		activeWards.clear();

		if (!GeneralData.INTERNALPHARMACIES) {
			return;
		}

		try {
			activeWards = wardBrowserManager.getWards();
			if (activeWards == null) {
				activeWards = new ArrayList<>();
			}

			for (Ward ward : activeWards) {
				double wardTotal = 0.0;
				try {
					List<MedicalWard> medicalWards = movWardBrowserManager.getMedicalsWard(ward.getCode(), medical.getCode(), false);
					if (medicalWards != null) {
						for (MedicalWard mw : medicalWards) {
							double qty = mw.getQty() != null ? mw.getQty() : (mw.getIn_quantity() - mw.getOut_quantity());
							if (qty > 0 || !mw.getId().getLot().getCode().isEmpty()) {
								WardStockRow row = new WardStockRow();
								row.ward = ward;
								row.lot = mw.getId().getLot();
								row.quantity = qty;
								allWardStockRows.add(row);
								wardTotal += qty;
							}
						}
					}
				} catch (OHServiceException ex) {
					// Ward without records or error
				}
				wardStockTotals.put(ward.getCode(), wardTotal);
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void computeStockSummary() {
		totalMainStoreQty = 0;
		totalWardsQty = 0.0;
		totalExpiredQty = 0.0;

		LocalDateTime now = LocalDateTime.now();

		for (Lot lot : allLots) {
			int mainQty = lot.getMainStoreQuantity() != null ? lot.getMainStoreQuantity() : 0;
			double wardQty = lot.getWardsTotalQuantity() != null ? lot.getWardsTotalQuantity() : 0.0;
			double lotTotal = mainQty + wardQty;

			totalMainStoreQty += mainQty;
			totalWardsQty += wardQty;

			if (lot.getDueDate() != null && lot.getDueDate().isBefore(now)) {
				totalExpiredQty += lotTotal;
			}
		}

		totalOverallQty = totalMainStoreQty + totalWardsQty;
		totalValidQty = Math.max(0.0, totalOverallQty - totalExpiredQty);
	}

	private void initComponents() {
		String titleText = MessageBundle.getMessage("angal.medicals.details.title") + " - " +
				medical.getDescription() + " (" + medical.getProdCode() + ")";
		setTitle(titleText);

		JPanel contentPane = new JPanel(new BorderLayout(5, 8));
		contentPane.setBackground(new Color(245, 247, 250));
		contentPane.setBorder(new CompoundBorder(
				new LineBorder(new Color(180, 190, 205), 1),
				new EmptyBorder(10, 12, 10, 12)
		));

		// 1. Header Panel
		contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

		// 2. Tabbed Pane
		tabbedPane = new JTabbedPane();
		tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
		tabbedPane.addTab("  " + MessageBundle.getMessage("angal.medicals.details.tab.lots") + "  ", createLotsTab());
		tabbedPane.addTab("  " + MessageBundle.getMessage("angal.medicals.details.tab.wards") + "  ", createWardsTab());
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		// 3. Bottom Action Panel
		contentPane.add(createBottomActionPanel(), BorderLayout.SOUTH);

		setContentPane(contentPane);
	}

	private JPanel createHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new JPanel(new BorderLayout(10, 6));
			headerPanel.setBackground(Color.WHITE);
			headerPanel.setBorder(new CompoundBorder(
					new LineBorder(new Color(210, 220, 230), 1, true),
					new EmptyBorder(10, 14, 10, 14)
			));

			// Meta Info
			JPanel infoPanel = new JPanel(new GridLayout(2, 4, 15, 6));
			infoPanel.setOpaque(false);

			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.common.description.txt") + " :",
					medical.getDescription(), true));
			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.common.code.txt") + " :",
					medical.getProdCode(), false));
			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.medicals.selecttype") + " :",
					medical.getType() != null ? medical.getType().getDescription() : "", false));
			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.exam.filter.family") + " :",
					medical.getArticleFamily() != null ? medical.getArticleFamily().getDescription() : "-", false));

			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.medicals.pcsperpck.col") + " :",
					medical.getPcsperpck() + " " + MessageBundle.getMessage("angal.medicals.pcsperpck.txt"), false));

			JPanel critPanel = new JPanel(new BorderLayout(4, 0));
			critPanel.setOpaque(false);
			critLevelTitleLabel = new JLabel(MessageBundle.getMessage("angal.medicals.critlevel.col") + " :");
			critLevelValueLabel = new JLabel();
			critPanel.add(critLevelTitleLabel, BorderLayout.WEST);
			critPanel.add(critLevelValueLabel, BorderLayout.CENTER);
			updateCriticalLevelDisplay();
			infoPanel.add(critPanel);

			// Average cost / total
			Double avgCostVal = null;
			try {
				Map<Integer, Double> avgCostMap = movStockInsertingManager
						.getAverageCostPricesByMedicals(Collections.singletonList(medical.getCode()));
				if (avgCostMap != null) {
					avgCostVal = avgCostMap.get(medical.getCode());
				}
			} catch (Exception e) {
				// Keep default zero if calculation fails
			}
			BigDecimal avgCost = avgCostVal != null ? BigDecimal.valueOf(avgCostVal) : BigDecimal.ZERO;
			String avgCostStr = avgCost.setScale(2, RoundingMode.HALF_UP) + " " + currencyCod;
			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.medicals.averagecostprice.col") + " :",
					avgCostStr, false));

			BigDecimal totalCost = avgCost.multiply(BigDecimal.valueOf(totalOverallQty));
			infoPanel.add(createMetaField(MessageBundle.getMessage("angal.medicals.totalcost.col") + " :",
					totalCost.setScale(2, RoundingMode.HALF_UP) + " " + currencyCod, false));

			headerPanel.add(infoPanel, BorderLayout.NORTH);

			// Stats container (Badges row + Wards chips row)
			JPanel statsContainer = new JPanel(new BorderLayout(0, 6));
			statsContainer.setOpaque(false);

			badgesContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
			badgesContainer.setOpaque(false);
			badgesContainer.setBorder(new CompoundBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 242)),
					new EmptyBorder(6, 0, 0, 0)
			));
			rebuildBadges();
			statsContainer.add(badgesContainer, BorderLayout.NORTH);

			wardsChipsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
			wardsChipsPanel.setOpaque(false);
			wardsChipsPanel.setBorder(new CompoundBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(240, 243, 248)),
					new EmptyBorder(4, 0, 0, 0)
			));
			rebuildWardsChips();
			statsContainer.add(wardsChipsPanel, BorderLayout.SOUTH);

			headerPanel.add(statsContainer, BorderLayout.CENTER);
		}
		return headerPanel;
	}

	private void updateCriticalLevelDisplay() {
		if (critLevelTitleLabel != null && critLevelValueLabel != null) {
			boolean isCritical = medical.getMinqty() > 0 && totalOverallQty <= medical.getMinqty();
			if (isCritical) {
				critLevelTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
				critLevelTitleLabel.setForeground(new Color(192, 57, 43));
				critLevelValueLabel.setText((int) medical.getMinqty() + " (" + MessageBundle.getMessage("angal.medicals.details.critlevel.reached") + ")");
				critLevelValueLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
				critLevelValueLabel.setForeground(new Color(192, 57, 43));
			} else {
				critLevelTitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				critLevelTitleLabel.setForeground(new Color(100, 110, 125));
				critLevelValueLabel.setText(String.valueOf((int) medical.getMinqty()));
				critLevelValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				critLevelValueLabel.setForeground(new Color(40, 50, 60));
			}
		}
	}

	private void rebuildBadges() {
		badgesContainer.removeAll();
		badgesContainer.add(createBadge(MessageBundle.getMessage("angal.medicals.details.badges.mainstore"),
				String.valueOf(totalMainStoreQty), new Color(41, 128, 185), new Color(235, 245, 255)));
		badgesContainer.add(createBadge(MessageBundle.getMessage("angal.medicals.details.badges.wards"),
				String.valueOf((int) totalWardsQty), new Color(142, 68, 173), new Color(245, 238, 250)));

		boolean isCritical = medical.getMinqty() > 0 && totalOverallQty <= medical.getMinqty();
		Color totalFg = isCritical ? new Color(192, 57, 43) : new Color(39, 174, 96);
		Color totalBg = isCritical ? new Color(253, 237, 236) : new Color(235, 250, 240);
		String totalTitle = isCritical ?
				MessageBundle.getMessage("angal.medicals.details.badges.totalcritical") :
				MessageBundle.getMessage("angal.medicals.details.badges.total");
		badgesContainer.add(createBadge(totalTitle,
				String.valueOf((int) totalOverallQty), totalFg, totalBg));

		badgesContainer.add(createBadge(MessageBundle.getMessage("angal.medicals.details.badges.expired"),
				String.valueOf((int) totalExpiredQty), new Color(192, 57, 43), new Color(253, 237, 236)));
		badgesContainer.add(createBadge(MessageBundle.getMessage("angal.medicals.details.badges.valid"),
				String.valueOf((int) totalValidQty), new Color(44, 62, 80), new Color(240, 243, 244)));
		badgesContainer.revalidate();
		badgesContainer.repaint();
	}

	private void rebuildWardsChips() {
		wardsChipsPanel.removeAll();
		JLabel wardTitle = new JLabel(MessageBundle.getMessage("angal.medicals.details.stockperward.label") + " ");
		wardTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
		wardTitle.setForeground(new Color(110, 80, 140));
		wardsChipsPanel.add(wardTitle);

		if (!GeneralData.INTERNALPHARMACIES || activeWards.isEmpty()) {
			JLabel emptyLabel = new JLabel(MessageBundle.getMessage("angal.medicals.details.nowardsconfig.msg"));
			emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
			emptyLabel.setForeground(Color.GRAY);
			wardsChipsPanel.add(emptyLabel);
		} else {
			for (Ward ward : activeWards) {
				double qty = wardStockTotals.getOrDefault(ward.getCode(), 0.0);
				wardsChipsPanel.add(createWardChip(ward.getDescription() + " (" + ward.getCode() + ")", (int) qty));
			}
		}
		wardsChipsPanel.revalidate();
		wardsChipsPanel.repaint();
	}

	private JPanel createMetaField(String label, String value, boolean highlight) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		p.setOpaque(false);
		JLabel l = new JLabel(label);
		l.setFont(new Font("SansSerif", Font.PLAIN, 12));
		l.setForeground(new Color(100, 110, 125));

		JLabel v = new JLabel(value != null ? value : "");
		v.setFont(new Font("SansSerif", highlight ? Font.BOLD : Font.PLAIN, 12));
		v.setForeground(highlight ? new Color(20, 30, 50) : new Color(40, 50, 60));

		p.add(l);
		p.add(v);
		return p;
	}

	private JPanel createBadge(String title, String qty, Color fgColor, Color bgColor) {
		JPanel badge = new JPanel(new BorderLayout(5, 2));
		badge.setBackground(bgColor);
		badge.setBorder(new CompoundBorder(
				new LineBorder(fgColor.brighter(), 1, true),
				new EmptyBorder(4, 10, 4, 10)
		));

		JLabel t = new JLabel(title);
		t.setFont(new Font("SansSerif", Font.PLAIN, 11));
		t.setForeground(fgColor.darker());

		JLabel q = new JLabel(qty, SwingConstants.CENTER);
		q.setFont(new Font("SansSerif", Font.BOLD, 15));
		q.setForeground(fgColor);

		badge.add(t, BorderLayout.NORTH);
		badge.add(q, BorderLayout.CENTER);
		return badge;
	}

	private JPanel createWardChip(String wardName, int qty) {
		JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		boolean hasStock = qty > 0;
		chip.setBackground(hasStock ? new Color(245, 240, 252) : new Color(248, 249, 250));
		chip.setBorder(new CompoundBorder(
				new LineBorder(hasStock ? new Color(190, 160, 220) : new Color(220, 225, 230), 1, true),
				new EmptyBorder(2, 6, 2, 6)
		));

		JLabel nameLabel = new JLabel(wardName + " :");
		nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
		nameLabel.setForeground(hasStock ? new Color(60, 40, 80) : new Color(130, 140, 150));

		JLabel qtyLabel = new JLabel(String.valueOf(qty));
		qtyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		qtyLabel.setForeground(hasStock ? new Color(142, 68, 173) : new Color(160, 165, 175));

		chip.add(nameLabel);
		chip.add(qtyLabel);
		return chip;
	}

	// ==========================================
	// LOTS TAB
	// ==========================================

	private JPanel createLotsTab() {
		JPanel panel = new JPanel(new BorderLayout(5, 8));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));

		// Filter bar
		JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
		filterBar.setOpaque(false);

		JLabel statusLabel = new JLabel(MessageBundle.getMessage("angal.medicals.details.status.col") + " :");
		String[] statusOptions = {
				MessageBundle.getMessage("angal.medicals.details.status.all"),
				MessageBundle.getMessage("angal.medicals.details.status.expired"),
				MessageBundle.formatMessage("angal.medicals.details.status.expiring", getExpiringDays()),
				MessageBundle.getMessage("angal.medicals.details.status.valid")
		};
		lotStatusFilterCombo = new JComboBox<>(statusOptions);
		lotStatusFilterCombo.setPreferredSize(new Dimension(210, 26));
		lotStatusFilterCombo.addActionListener(e -> applyLotsFilter());

		lotHideZeroCheck = new JCheckBox(MessageBundle.getMessage("angal.medicals.details.hidezero.lots"), true);
		lotHideZeroCheck.setOpaque(false);
		lotHideZeroCheck.setFont(new Font("SansSerif", Font.BOLD, 12));
		lotHideZeroCheck.addActionListener(e -> applyLotsFilter());

		JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.medicals.details.searchlot"));
		lotSearchField = new VoLimitedTextField(50, 12);
		lotSearchField.setPreferredSize(new Dimension(130, 26));
		lotSearchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				applyLotsFilter();
			}
		});

		filterBar.add(statusLabel);
		filterBar.add(lotStatusFilterCombo);
		filterBar.add(new JSeparator(SwingConstants.VERTICAL));
		filterBar.add(lotHideZeroCheck);
		filterBar.add(new JSeparator(SwingConstants.VERTICAL));
		filterBar.add(searchLabel);
		filterBar.add(lotSearchField);

		panel.add(filterBar, BorderLayout.NORTH);

		// Table
		lotsTableModel = new LotsTableModel();
		lotsTable = new JTable(lotsTableModel);
		lotsTable.setRowHeight(30);
		lotsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lotsTable.getTableHeader().setPreferredSize(new Dimension(lotsTable.getWidth(), 30));
		lotsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		lotsTable.getTableHeader().setBackground(new Color(230, 236, 245));
		lotsTable.getTableHeader().setForeground(new Color(30, 45, 65));

		TableColumnModel cm = lotsTable.getColumnModel();
		cm.getColumn(0).setPreferredWidth(110);
		cm.getColumn(1).setPreferredWidth(125);
		cm.getColumn(2).setPreferredWidth(125);
		cm.getColumn(3).setPreferredWidth(115);
		cm.getColumn(4).setPreferredWidth(95);
		cm.getColumn(5).setPreferredWidth(130);
		cm.getColumn(6).setPreferredWidth(120);
		cm.getColumn(7).setPreferredWidth(110);

		lotsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				Lot lot = (Lot) lotsTableModel.getValueAt(row, -1);
				if (lot != null) {
					LocalDateTime now = LocalDateTime.now();
					boolean isExpired = lot.getDueDate() != null && lot.getDueDate().isBefore(now);
					boolean isExpiringSoon = !isExpired && lot.getDueDate() != null &&
							ChronoUnit.DAYS.between(now.toLocalDate(), lot.getDueDate().toLocalDate()) <= getExpiringDays();

					if (!isSelected) {
						if (isExpired) {
							c.setBackground(Color.DARK_GRAY);
							c.setForeground(Color.WHITE);
						} else if (isExpiringSoon) {
							c.setBackground(Color.ORANGE);
							c.setForeground(Color.BLACK);
						} else {
							c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
							double totalQty = (lot.getMainStoreQuantity() != null ? lot.getMainStoreQuantity() : 0) +
									(lot.getWardsTotalQuantity() != null ? lot.getWardsTotalQuantity() : 0.0);
							if (totalQty == 0) {
								c.setForeground(Color.GRAY);
							} else {
								c.setForeground(Color.BLACK);
							}
						}
					}

					if (column == 3) {
						setFont(getFont().deriveFont(Font.BOLD));
					}
				}

				if (column >= 4) {
					setHorizontalAlignment(SwingConstants.RIGHT);
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}
				return c;
			}
		});

		JScrollPane scrollPane = new JScrollPane(lotsTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		applyLotsFilter();
		return panel;
	}

	private int getExpiringDays() {
		return (GeneralData.HIGHLIGHTEXPIRINGMEDICALDAYS != null && GeneralData.HIGHLIGHTEXPIRINGMEDICALDAYS > 0)
				? GeneralData.HIGHLIGHTEXPIRINGMEDICALDAYS
				: 30;
	}

	private void applyLotsFilter() {
		int statusIndex = lotStatusFilterCombo.getSelectedIndex();
		boolean hideZero = lotHideZeroCheck.isSelected();
		String searchText = lotSearchField.getText().trim().toLowerCase();

		LocalDateTime now = LocalDateTime.now();
		filteredLots = allLots.stream().filter(lot -> {
			double totalQty = (lot.getMainStoreQuantity() != null ? lot.getMainStoreQuantity() : 0) +
					(lot.getWardsTotalQuantity() != null ? lot.getWardsTotalQuantity() : 0);

			if (hideZero && totalQty <= 0) {
				return false;
			}

			if (!searchText.isEmpty() && !lot.getCode().toLowerCase().contains(searchText)) {
				return false;
			}

			boolean isExpired = lot.getDueDate() != null && lot.getDueDate().isBefore(now);
			boolean isExpiringSoon = !isExpired && lot.getDueDate() != null &&
					ChronoUnit.DAYS.between(now.toLocalDate(), lot.getDueDate().toLocalDate()) <= getExpiringDays();

			if (statusIndex == 1 && !isExpired) {
				return false;
			}
			if (statusIndex == 2 && !isExpiringSoon) {
				return false;
			}
			if (statusIndex == 3 && (isExpired || isExpiringSoon)) {
				return false;
			}

			return true;
		}).collect(Collectors.toList());

		lotsTableModel.fireTableDataChanged();
	}

	class LotsTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private final String[] columns = {
				MessageBundle.getMessage("angal.medicals.details.lotcode.col"),
				MessageBundle.getMessage("angal.medicals.details.prepdate.col"),
				MessageBundle.getMessage("angal.medicals.details.duedate.col"),
				MessageBundle.getMessage("angal.medicals.details.status.col"),
				MessageBundle.getMessage("angal.medicals.details.unitcost.col"),
				MessageBundle.getMessage("angal.medicals.details.mainstore.col"),
				MessageBundle.getMessage("angal.medicals.details.wards.col"),
				MessageBundle.getMessage("angal.medicals.details.total.col")
		};

		@Override
		public int getRowCount() {
			return filteredLots == null ? 0 : filteredLots.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (filteredLots == null || rowIndex >= filteredLots.size()) {
				return null;
			}
			Lot lot = filteredLots.get(rowIndex);
			if (columnIndex == -1) {
				return lot;
			}
			if (columnIndex == 0) {
				return lot.getCode();
			}
			if (columnIndex == 1) {
				return lot.getPreparationDate() != null ? lot.getPreparationDate().format(DATE_FORMATTER) : "";
			}
			if (columnIndex == 2) {
				return lot.getDueDate() != null ? lot.getDueDate().format(DATE_FORMATTER) : "";
			}
			if (columnIndex == 3) {
				LocalDateTime now = LocalDateTime.now();
				if (lot.getDueDate() != null && lot.getDueDate().isBefore(now)) {
					return MessageBundle.getMessage("angal.medicals.details.status.badge.expired");
				} else if (lot.getDueDate() != null && ChronoUnit.DAYS.between(now.toLocalDate(), lot.getDueDate().toLocalDate()) <= getExpiringDays()) {
					return MessageBundle.getMessage("angal.medicals.details.status.badge.expiring");
				}
				return MessageBundle.getMessage("angal.medicals.details.status.badge.valid");
			}
			if (columnIndex == 4) {
				return (lot.getCost() != null ? lot.getCost() : BigDecimal.ZERO) + " " + currencyCod;
			}
			if (columnIndex == 5) {
				return lot.getMainStoreQuantity() != null ? lot.getMainStoreQuantity() : 0;
			}
			if (columnIndex == 6) {
				return lot.getWardsTotalQuantity() != null ? (int) lot.getWardsTotalQuantity().doubleValue() : 0;
			}
			if (columnIndex == 7) {
				int main = lot.getMainStoreQuantity() != null ? lot.getMainStoreQuantity() : 0;
				double wards = lot.getWardsTotalQuantity() != null ? lot.getWardsTotalQuantity() : 0.0;
				return (int) (main + wards);
			}
			return null;
		}
	}

	private JPanel createWardsTab() {
		JPanel panel = new JPanel(new BorderLayout(5, 8));
		panel.setBackground(Color.WHITE);
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));

		if (!GeneralData.INTERNALPHARMACIES) {
			JLabel infoLabel = new JLabel(MessageBundle.getMessage("angal.medicals.details.nowardsconfig.msg"), SwingConstants.CENTER);
			infoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
			infoLabel.setForeground(Color.GRAY);
			panel.add(infoLabel, BorderLayout.CENTER);
			return panel;
		}

		JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
		filterBar.setOpaque(false);

		JLabel wardLabel = new JLabel(MessageBundle.getMessage("angal.medicals.details.filterward"));
		List<Object> wardOptions = new ArrayList<>();
		wardOptions.add(MessageBundle.getMessage("angal.medicals.details.allwards"));
		wardOptions.addAll(activeWards);
		wardFilterCombo = new JComboBox<>(wardOptions.toArray(new Object[0]));
		wardFilterCombo.setPreferredSize(new Dimension(240, 26));
		wardFilterCombo.addActionListener(e -> applyWardFilter());

		wardHideZeroCheck = new JCheckBox(MessageBundle.getMessage("angal.medicals.details.hidezero.wards"), true);
		wardHideZeroCheck.setOpaque(false);
		wardHideZeroCheck.setFont(new Font("SansSerif", Font.BOLD, 12));
		wardHideZeroCheck.addActionListener(e -> applyWardFilter());

		filterBar.add(wardLabel);
		filterBar.add(wardFilterCombo);
		filterBar.add(new JSeparator(SwingConstants.VERTICAL));
		filterBar.add(wardHideZeroCheck);

		panel.add(filterBar, BorderLayout.NORTH);

		wardStockTableModel = new WardStockTableModel();
		wardStockTable = new JTable(wardStockTableModel);
		wardStockTable.setRowHeight(30);
		wardStockTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
		wardStockTable.getTableHeader().setPreferredSize(new Dimension(wardStockTable.getWidth(), 30));
		wardStockTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		wardStockTable.getTableHeader().setBackground(new Color(230, 236, 245));
		wardStockTable.getTableHeader().setForeground(new Color(30, 45, 65));

		TableColumnModel cm = wardStockTable.getColumnModel();
		cm.getColumn(0).setPreferredWidth(100);
		cm.getColumn(1).setPreferredWidth(220);
		cm.getColumn(2).setPreferredWidth(130);
		cm.getColumn(3).setPreferredWidth(130);
		cm.getColumn(4).setPreferredWidth(130);
		cm.getColumn(5).setPreferredWidth(160);

		wardStockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				WardStockRow r = (WardStockRow) wardStockTableModel.getValueAt(row, -1);
				if (r != null && r.lot != null) {
					LocalDateTime now = LocalDateTime.now();
					boolean isExpired = r.lot.getDueDate() != null && r.lot.getDueDate().isBefore(now);
					boolean isExpiringSoon = !isExpired && r.lot.getDueDate() != null &&
							ChronoUnit.DAYS.between(now.toLocalDate(), r.lot.getDueDate().toLocalDate()) <= getExpiringDays();

					if (!isSelected) {
						if (isExpired) {
							c.setBackground(Color.DARK_GRAY);
							c.setForeground(Color.WHITE);
						} else if (isExpiringSoon) {
							c.setBackground(Color.ORANGE);
							c.setForeground(Color.BLACK);
						} else {
							c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
							if (r.quantity == 0) {
								c.setForeground(Color.GRAY);
							} else {
								c.setForeground(Color.BLACK);
							}
						}
					}
				}

				if (column == 5) {
					setHorizontalAlignment(SwingConstants.RIGHT);
					setFont(getFont().deriveFont(Font.BOLD));
				} else {
					setHorizontalAlignment(SwingConstants.LEFT);
				}
				return c;
			}
		});

		JScrollPane scrollPane = new JScrollPane(wardStockTable);

		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
		totalPanel.setBackground(new Color(245, 247, 250));
		wardTotalLabel = new JLabel();
		wardTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		wardTotalLabel.setForeground(new Color(142, 68, 173));
		totalPanel.add(wardTotalLabel);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		centerPanel.add(totalPanel, BorderLayout.SOUTH);

		panel.add(centerPanel, BorderLayout.CENTER);

		applyWardFilter();
		return panel;
	}

	private void applyWardFilter() {
		Object selectedWard = wardFilterCombo.getSelectedItem();
		boolean hideZero = wardHideZeroCheck.isSelected();

		filteredWardStockRows = allWardStockRows.stream().filter(row -> {
			if (selectedWard instanceof Ward) {
				Ward w = (Ward) selectedWard;
				if (!row.ward.getCode().equals(w.getCode())) {
					return false;
				}
			}
			if (hideZero && row.quantity <= 0) {
				return false;
			}
			return true;
		}).collect(Collectors.toList());

		wardStockTableModel.fireTableDataChanged();

		double totalFilteredQty = filteredWardStockRows.stream().mapToDouble(r -> r.quantity).sum();
		wardTotalLabel.setText(MessageBundle.formatMessage("angal.medicals.details.totalwards.fmt", (int) totalFilteredQty));
	}

	static class WardStockRow {
		Ward ward;
		Lot lot;
		double quantity;
	}

	class WardStockTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private final String[] columns = {
				MessageBundle.getMessage("angal.common.code.txt"),
				MessageBundle.getMessage("angal.medicals.details.wardname.col"),
				MessageBundle.getMessage("angal.medicals.details.lotcode.col"),
				MessageBundle.getMessage("angal.medicals.details.duedate.col"),
				MessageBundle.getMessage("angal.medicals.details.status.col"),
				MessageBundle.getMessage("angal.common.quantity.txt")
		};

		@Override
		public int getRowCount() {
			return filteredWardStockRows == null ? 0 : filteredWardStockRows.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (filteredWardStockRows == null || rowIndex >= filteredWardStockRows.size()) {
				return null;
			}
			WardStockRow row = filteredWardStockRows.get(rowIndex);
			if (columnIndex == -1) {
				return row;
			}
			if (columnIndex == 0) {
				return row.ward != null ? row.ward.getCode() : "";
			}
			if (columnIndex == 1) {
				return row.ward != null ? row.ward.getDescription() : "";
			}
			if (columnIndex == 2) {
				return row.lot != null ? row.lot.getCode() : "";
			}
			if (columnIndex == 3) {
				return row.lot != null && row.lot.getDueDate() != null ? row.lot.getDueDate().format(DATE_FORMATTER) : "";
			}
			if (columnIndex == 4) {
				if (row.lot == null || row.lot.getDueDate() == null) {
					return "";
				}
				LocalDateTime now = LocalDateTime.now();
				if (row.lot.getDueDate().isBefore(now)) {
					return MessageBundle.getMessage("angal.medicals.details.status.badge.expired");
				} else if (ChronoUnit.DAYS.between(now.toLocalDate(), row.lot.getDueDate().toLocalDate()) <= getExpiringDays()) {
					return MessageBundle.getMessage("angal.medicals.details.status.badge.expiring");
				}
				return MessageBundle.getMessage("angal.medicals.details.status.badge.valid");
			}
			if (columnIndex == 5) {
				return (int) row.quantity;
			}
			return null;
		}
	}

	private JPanel createBottomActionPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 5));
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(6, 0, 0, 0));

		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		leftPanel.setOpaque(false);

		if (GeneralData.COMMANDMODULEENABLED) {
			JButton orderButton = new JButton(MessageBundle.getMessage("angal.medicals.details.order.btn"));
			orderButton.setMnemonic(MessageBundle.getMnemonic("angal.medicals.details.order.btn.key"));
			orderButton.setFont(new Font("SansSerif", Font.BOLD, 13));
			orderButton.setBackground(new Color(41, 128, 185));
			orderButton.setForeground(Color.WHITE);
			Dimension prefOrder = orderButton.getPreferredSize();
			orderButton.setPreferredSize(new Dimension(Math.max(160, prefOrder.width + 24), 32));
			orderButton.addActionListener(e -> openOrderDialog());
			leftPanel.add(orderButton);
		}

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightPanel.setOpaque(false);

		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		Dimension prefClose = closeButton.getPreferredSize();
		closeButton.setPreferredSize(new Dimension(Math.max(95, prefClose.width + 20), 32));
		closeButton.addActionListener(e -> dispose());
		rightPanel.add(closeButton);

		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.EAST);
		return panel;
	}

	private void openOrderDialog() {
		MedicalOrderDialog orderDialog = new MedicalOrderDialog(this, medical, totalOverallQty, totalExpiredQty, totalValidQty);
		orderDialog.setVisible(true);

		if (orderDialog.isCommandProcessed()) {
			loadData();
			updateCriticalLevelDisplay();
			rebuildBadges();
			rebuildWardsChips();
			applyLotsFilter();
			applyWardFilter();
		}
	}

	private static class WrapLayout extends FlowLayout {

		private static final long serialVersionUID = 1L;

		public WrapLayout() {
			super();
		}

		public WrapLayout(int align) {
			super(align);
		}

		public WrapLayout(int align, int hgap, int vgap) {
			super(align, hgap, vgap);
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return layoutSize(target, true);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			Dimension minimum = layoutSize(target, false);
			minimum.width -= (getHgap() + 1);
			return minimum;
		}

		private Dimension layoutSize(Container target, boolean preferred) {
			synchronized (target.getTreeLock()) {
				int targetWidth = target.getWidth();

				if (targetWidth <= 0) {
					Container parent = target.getParent();
					while (parent != null) {
						if (parent.getWidth() > 0) {
							targetWidth = parent.getWidth();
							break;
						}
						parent = parent.getParent();
					}
				}

				if (targetWidth <= 0) {
					targetWidth = 800;
				}

				int hgap = getHgap();
				int vgap = getVgap();
				Insets insets = target.getInsets();
				int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
				int maxWidth = targetWidth - horizontalInsetsAndGap;

				Dimension dim = new Dimension(0, 0);
				int rowWidth = 0;
				int rowHeight = 0;
				int nmembers = target.getComponentCount();

				for (int i = 0; i < nmembers; i++) {
					Component m = target.getComponent(i);

					if (m.isVisible()) {
						Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

						if (rowWidth != 0 && rowWidth + d.width > maxWidth) {
							addRow(dim, rowWidth, rowHeight);
							rowWidth = 0;
							rowHeight = 0;
						}

						if (rowWidth != 0) {
							rowWidth += hgap;
						}

						rowWidth += d.width;
						rowHeight = Math.max(rowHeight, d.height);
					}
				}

				addRow(dim, rowWidth, rowHeight);

				dim.width += horizontalInsetsAndGap;
				dim.height += insets.top + insets.bottom + vgap * 2;

				return dim;
			}
		}

		private void addRow(Dimension dim, int rowWidth, int rowHeight) {
			dim.width = Math.max(dim.width, rowWidth);
			if (dim.height > 0) {
				dim.height += getVgap();
			}
			dim.height += rowHeight;
		}
	}
}
