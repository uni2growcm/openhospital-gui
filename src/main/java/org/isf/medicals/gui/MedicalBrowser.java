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
package org.isf.medicals.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.gui.MedicalEdit.MedicalListener;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.isf.stat.gui.report.GenericReportPharmaceuticalAMC;
import org.isf.stat.gui.report.GenericReportPharmaceuticalOrder;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStock;
import org.isf.stat.gui.report.GenericReportPharmaceuticalStockCard;
import org.isf.utils.excel.ExcelExporter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodFromDateToDateChooser;
import org.isf.utils.jobjects.JMonthYearChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;
import org.springframework.data.domain.Page;

public class MedicalBrowser extends ModalJFrame implements MedicalListener {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MedicalBrowser.class);
	private static final String STR_ALL = MessageBundle.getMessage("angal.common.all.txt");
	private static final String STR_ACTIVE_ONLY = MessageBundle.getMessage("angal.medicals.activeonly.txt");
	private static final String STR_DISABLED_ONLY = MessageBundle.getMessage("angal.medicals.disabledonly.txt");

	private List<Medical> medicalList = new ArrayList<>();
	private int selectedrow;
	private JComboBox<Object> pbox;
	private JComboBox<String> activeComboBox;
	private JButton nextButton;
	private JButton prevButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel underLabel;
	private JLabel totalMedicalsLabel;
	private int PAGES = 0;
	private int CURRENT_PAGE = 0;
	private long TOTAL_PAGES = 0;
	private final int PAGE_SIZE = 100;
	protected AbstractButton searchBoxButton;

	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.medicals.pcsperpck.col"),
			MessageBundle.getMessage("angal.medicals.stock.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicals.critlevel.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicals.outofstock.col").toUpperCase()
	};

	private String[] pColumnsSorter = { "MDSRT_DESC", "MDSR_CODE", "MDSR_DESC", null, "STOCK", "MDSR_MIN_STOCK_QTI", "STOCK" };
	private boolean[] pColumnsNormalSorting = { true, true, true, true, true, true, false };
	private int[] pColumnWidth = { 100, 100, 400, 60, 60, 80, 100 };
	private boolean[] pColumnResizable = { true, true, true, true, true, true, true };

	private Medical medical;
	private DefaultTableModel model;
	private JTable table;
	private final JFrame me;
	private String pSelection;
	private String activeSelection = STR_ACTIVE_ONLY;
	private JTextField searchString;
	private JButton buttonAMC;
	private JComboBox<Object> jComboBoxWard;
	private boolean updatingPageCombo;
	private boolean altKeyReleased;
	private String monthFrom;
	private String monthTo;
	private List<Medical> pMedicals;

	private final WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private final MedicalTypeBrowserManager medicalTypeManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);

	private Map<Integer, LocalDate> expiryDateCache = new HashMap<>();
	private Map<Integer, Double> actualQtyCache = new HashMap<>();

	public MedicalBrowser() {
		me = this;
		setTitle(MessageBundle.getMessage("angal.medicals.pharmaceuticalbrowser.title"));
		setPreferredSize(new Dimension(1220, 550));
		setMinimumSize(new Dimension(940, 550));
		getComboBoxMedicalType();
		setContentPane(getContentpane());
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
		loadCurrentPage();
		searchString.requestFocus();
	}

	@Override
	public void medicalInserted(Medical medical) {
		medicalList.add(0, medical);
		((MedicalBrowsingModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
		repaint();
	}

	@Override
	public void medicalUpdated(AWTEvent e) {
		medicalList.set(selectedrow, medical);
		((MedicalBrowsingModel) table.getModel()).fireTableDataChanged();
		table.updateUI();
		if (table.getRowCount() > 0 && selectedrow > -1) {
			table.setRowSelectionInterval(selectedrow, selectedrow);
		}
		repaint();
		activeComboBox.setSelectedItem(activeComboBox.getSelectedItem());
		int keyCode = 0;
		KeyEvent enterPressed = new KeyEvent(searchString, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, keyCode, '\n');
		searchString.dispatchEvent(enterPressed);
	}

	public void updateMedicalList(List<Medical> medicalList) {
		this.medicalList = medicalList;
	}

	private JPanel getContentpane() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(getTopPanel(), BorderLayout.NORTH);
		contentPane.add(getSubPanel(), BorderLayout.CENTER);
		contentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		return contentPane;
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel searchBoxLabel = new JLabel(MessageBundle.getMessage("angal.medicals.searchbox.label"));
		topPanel.add(searchBoxLabel);
		JTextField searchBox = getSearchBox();
		topPanel.add(searchBox);

		searchBoxButton = new JButton("");
		searchBoxButton.setPreferredSize(new Dimension(20, 20));
		searchBoxButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		topPanel.add(searchBoxButton);
		searchBoxButton.addActionListener(actionEvent -> {
			applyFilter();
			updatePageCombo();
		});
		return topPanel;
	}

	private JPanel getSubPanel() {
		JPanel subPanel = new JPanel(new BorderLayout());
		subPanel.add(getPaginationPanel(), BorderLayout.SOUTH);
		subPanel.add(getScrollPane(), BorderLayout.CENTER);
		return subPanel;
	}

	private JScrollPane getScrollPane() {
		JScrollPane scrollPane = new JScrollPane(getJTable());
		int totWidth = 0;
		for (int colWidth : pColumnWidth) {
			totWidth += colWidth;
		}
		scrollPane.setPreferredSize(new Dimension(totWidth, 450));
		return scrollPane;
	}

	private JTable getJTable() {
		if (table == null) {
			model = new MedicalBrowsingModel("", "", true, CURRENT_PAGE, PAGE_SIZE);
			table = new JTable(model);
			table.setAutoCreateRowSorter(true);
			table.setAutoCreateColumnsFromModel(false);

			ColorTableCellRenderer renderer = new ColorTableCellRenderer();
			table.setDefaultRenderer(Object.class, renderer);
			table.setDefaultRenderer(Integer.class, renderer);
			table.setDefaultRenderer(Double.class, renderer);
			table.setDefaultRenderer(Boolean.class, renderer);
			table.setDefaultRenderer(String.class, renderer);

			for (int i = 0; i < pColumnWidth.length; i++) {
				table.getColumnModel().getColumn(i).setMinWidth(pColumnWidth[i]);
				if (!pColumnResizable[i]) {
					table.getColumnModel().getColumn(i).setMaxWidth(pColumnWidth[i]);
				}
			}
			updatePageCombo();
		}
		return table;
	}

	private JPanel getJButtonPanel() {
		JPanel buttonPanel = new JPanel(new WrapLayout());
		buttonPanel.add(getComboBoxActive());
		buttonPanel.add(new JLabel(MessageBundle.getMessage("angal.medicals.selecttype")));
		buttonPanel.add(getComboBoxMedicalType());
		if (MainMenu.checkUserGrants("btnpharmaceuticalnew")) {
			buttonPanel.add(getJButtonNew());
		}
		if (MainMenu.checkUserGrants("btnpharmaceuticaledit")) {
			buttonPanel.add(getJButtonEdit());
		}
		if (MainMenu.checkUserGrants("btnpharmaceuticaldel")) {
			buttonPanel.add(getJButtonDelete());
		}
		buttonPanel.add(getJButtonReport());
		buttonPanel.add(getJButtonStock());
		buttonPanel.add(getJButtonStockCard());
		buttonPanel.add(getJButtonOrderList());
		buttonPanel.add(getJButtonExpiring());
		buttonPanel.add(getJButtonAMC());
		buttonPanel.add(getJButtonClose());
		return buttonPanel;
	}

	private JButton getJButtonAMC() {
		if (buttonAMC == null) {
			buttonAMC = new JButton(MessageBundle.getMessage("angal.medicals.averagemonthlyconsumption.btn"));
			buttonAMC.setMnemonic(MessageBundle.getMnemonic("angal.medicals.averagemonthlyconsumption.btn.key"));
			buttonAMC.addActionListener(actionEvent -> {
				List<String> dateOptions = new ArrayList<>();
				dateOptions.add(MessageBundle.getMessage("angal.medicals.today"));
				dateOptions.add(MessageBundle.getMessage("angal.common.date.txt"));

				Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png");
				String dateOption = (String) MessageDialog.inputDialog(this, icon, dateOptions.toArray(), dateOptions.get(0), "angal.medicals.pleaseselectareport.msg");

				if (dateOption == null) return;

				int i = 0;
				if (dateOptions.indexOf(dateOption) == i) {
					new GenericReportPharmaceuticalAMC(null, GeneralData.PHARMACEUTICALAMC, false);
					new GenericReportPharmaceuticalAMC(null, GeneralData.PHARMACEUTICALAMC, true);
					return;
				}
				if (dateOptions.indexOf(dateOption) == ++i) {
					icon = new ImageIcon("rsc/icons/calendar_dialog.png");
					GoodDateChooser dateChooser = new GoodDateChooser(LocalDate.now(), true, false);
					int r = JOptionPane.showConfirmDialog(this, dateChooser, MessageBundle.getMessage("angal.common.date.txt"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
					if (r == JOptionPane.OK_OPTION) {
						new GenericReportPharmaceuticalAMC(dateChooser.getDateEndOfDay(), GeneralData.PHARMACEUTICALAMC, false);
						new GenericReportPharmaceuticalAMC(dateChooser.getDateEndOfDay(), GeneralData.PHARMACEUTICALAMC, true);
					}
				}
			});
		}
		return buttonAMC;
	}

	private JTextField getSearchBox() {
		if (searchString == null) {
			searchString = new JTextField();
			searchString.setColumns(15);
			searchString.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						applyFilter();
						updatePageCombo();
					}
				}
				@Override
				public void keyReleased(KeyEvent e) {
					altKeyReleased = true;
				}
			});
		}
		return searchString;
	}

	private JButton getJButtonClose() {
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		return closeButton;
	}

	private JButton getJButtonExpiring() {
		JButton buttonExpiring = new JButton(MessageBundle.getMessage("angal.medicals.expiring.btn"));
		buttonExpiring.setMnemonic(MessageBundle.getMnemonic("angal.medicals.expiring.btn.key"));
		buttonExpiring.addActionListener(actionEvent -> launchExpiringReport());
		return buttonExpiring;
	}

	private JButton getJButtonOrderList() {
		JButton buttonOrderList = new JButton(MessageBundle.getMessage("angal.medicals.order.btn"));
		buttonOrderList.setMnemonic(MessageBundle.getMnemonic("angal.medicals.order.btn.key"));
		buttonOrderList.addActionListener(actionEvent -> {
			boolean includeNonZeroQty = false;
			int ok = MessageDialog.yesNoCancel(this, "angal.medicals.showonlycriticalstock.msg");
			if (ok == JOptionPane.CANCEL_OPTION) return;
			if (ok == JOptionPane.YES_OPTION) {
				includeNonZeroQty = true;
			}
			new GenericReportPharmaceuticalOrder(GeneralData.PHARMACEUTICALORDER, includeNonZeroQty);
		});
		return buttonOrderList;
	}

	private JButton getJButtonStock() {
		JButton buttonStock = new JButton(MessageBundle.getMessage("angal.medicals.stock.btn"));
		buttonStock.setMnemonic(MessageBundle.getMnemonic("angal.medicals.stock.btn.key"));
		buttonStock.addActionListener(actionEvent -> {
			List<String> dateOptions = new ArrayList<>();
			dateOptions.add(MessageBundle.getMessage("angal.medicals.today"));
			dateOptions.add(MessageBundle.getMessage("angal.common.date.txt"));

			Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png");
			String dateOption = (String) MessageDialog.inputDialog(this, icon, dateOptions.toArray(), dateOptions.get(0), "angal.medicals.pleaseselectareport.msg");

			if (dateOption == null) return;

			List<String> lotOptions = new ArrayList<>();
			lotOptions.add(MessageBundle.getMessage("angal.medicals.onlyquantity"));
			lotOptions.add(MessageBundle.getMessage("angal.medicals.withlot"));

			String lotOption = (String) MessageDialog.inputDialog(this,
							icon,
							lotOptions.toArray(),
							lotOptions.get(0),
							"angal.medicals.pleaseselectareport.msg");

            if (lotOption == null ) return;

            boolean includeZeroQuantity = true;
            if (lotOptions.indexOf(lotOption) == 0) {
                int ok = MessageDialog.yesNoCancel(this, "angal.medicals.includezeroqtyinstock");
                if (ok == JOptionPane.CANCEL_OPTION) return;
                if (ok == JOptionPane.NO_OPTION) {
                    includeZeroQuantity=false;
                }
            }

			String sortBy;
			String groupBy = null;
			String filter = '%' + searchString.getText() + '%';
			if (pbox.getSelectedItem() instanceof MedicalType) {
				groupBy = ((MedicalType) pbox.getSelectedItem()).getDescription();
			}
			List<?> sortedKeys = table.getRowSorter().getSortKeys();
			if (!sortedKeys.isEmpty()) {
				int sortedColumn = ((SortKey) sortedKeys.get(0)).getColumn();
				SortOrder sortedOrder = ((SortKey) sortedKeys.get(0)).getSortOrder();

				String columnName = pColumnsSorter[sortedColumn];
				String columnOrder = sortedOrder.toString().equals("ASCENDING") ? "ASC" : "DESC";
				if (!pColumnsNormalSorting[sortedColumn]) {
					columnOrder = sortedOrder.toString().equals("ASCENDING") ? "DESC" : "ASC";
				}
				if (groupBy == null) {
					groupBy = "%";
					sortBy = "MDSRT_DESC, " + columnName + ' ' + columnOrder;
				} else {
					sortBy = columnName + ' ' + columnOrder;
				}
			} else {
				groupBy = "%%";
				sortBy = "MDSRT_DESC, MDSR_DESC";
			}

			String report = "";
			int i = 0;
			if (lotOptions.indexOf(lotOption) == i) {
				report = includeZeroQuantity ? GeneralData.PHARMACEUTICALSTOCK : GeneralData.PHARMACEUTICALSTOCKNOZERO;
			}
			if (lotOptions.indexOf(lotOption) == ++i) {
				report = GeneralData.PHARMACEUTICALSTOCKLOT;
			}

			i = 0;
			if (dateOptions.indexOf(dateOption) == i) {
				new GenericReportPharmaceuticalStock(null, report, filter, groupBy, sortBy, false);
				new GenericReportPharmaceuticalStock(null, report, filter, groupBy, sortBy, true);
				return;
			}
			if (dateOptions.indexOf(dateOption) == ++i) {

				icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$

				GoodDateChooser dateChooser = new GoodDateChooser(LocalDate.now(), true, false);
				int r = JOptionPane.showConfirmDialog(this,
								dateChooser,
								MessageBundle.getMessage("angal.common.date.txt"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE,
								icon);

				if (r == JOptionPane.OK_OPTION) {
					new GenericReportPharmaceuticalStock(dateChooser.getDateEndOfDay(), report, filter, groupBy, sortBy, false);
					new GenericReportPharmaceuticalStock(dateChooser.getDateEndOfDay(), report, filter, groupBy, sortBy, true);
				}
			}
		});
		return buttonStock;
	}

	private JButton getJButtonStockCard() {
		JButton buttonStockCard = new JButton(MessageBundle.getMessage("angal.common.stockcard.btn"));
		buttonStockCard.setMnemonic(MessageBundle.getMnemonic("angal.common.stockcard.btn.key"));
		buttonStockCard.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				medical = (Medical) model.getValueAt(selectedrow, -1);
				GoodFromDateToDateChooser dataRange = new GoodFromDateToDateChooser(this, getJComboBoxWard());
				dataRange.setTitle(MessageBundle.getMessage("angal.messagedialog.question.title"));
				dataRange.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dataRange.setVisible(true);

				LocalDate dateFrom = dataRange.getDateFrom();
				LocalDate dateTo = dataRange.getDateTo();
				boolean toExcel = dataRange.isExcel();

				if (!dataRange.isCancel()) {
					Object ward = dataRange.getSelectedWard();
					if (ward instanceof String) {
						new GenericReportPharmaceuticalStockCard("ProductLedger", dateFrom.atStartOfDay(), dateTo.atTime(LocalTime.MAX), medical, null, toExcel);
					} else {
						new GenericReportPharmaceuticalStockCard("WardProductLedger", dateFrom.atStartOfDay(), dateTo.atTime(LocalTime.MAX), medical, (Ward) ward, toExcel);
					}
				}
			}
		});
		return buttonStockCard;
	}

	private JComboBox<Object> getJComboBoxWard() {
		if (jComboBoxWard == null) {
			jComboBoxWard = new JComboBox<>();
			List<Ward> wardList;
			try {
				wardList = wardBrowserManager.getWards();
			} catch (OHServiceException e) {
				wardList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}

			jComboBoxWard.addItem(MessageBundle.getMessage("angal.medicalstockward.selectaward"));
			for (Ward wardCombo : wardList) {
				jComboBoxWard.addItem(wardCombo);
			}
			jComboBoxWard.setBorder(null);
			jComboBoxWard.setPreferredSize(new Dimension(340, 26));
		}
		jComboBoxWard.setSelectedItem(MessageBundle.getMessage("angal.medicalstockward.selectaward"));
		return jComboBoxWard;
	}

	private JButton getJButtonReport() {
		JButton buttonExport = new JButton(MessageBundle.getMessage("angal.medicals.export.btn"));
		buttonExport.setMnemonic(MessageBundle.getMnemonic("angal.medicals.export.btn.key"));
		buttonExport.addActionListener(actionEvent -> {
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
				try {
					if (exportFile.getName().endsWith(".xlsx")) {
						xlsExport.exportTableToExcel(table, exportFile);
					} else {
						xlsExport.exportTableToExcelOLD(table, exportFile);
					}
				} catch (IOException exc) {
					JOptionPane.showMessageDialog(this, exc.getMessage(), MessageBundle.getMessage("angal.messagedialog.error.title"), JOptionPane.PLAIN_MESSAGE);
					LOGGER.error("Export to excel error : {}", exc.getMessage());
				}
			}
		});
		return buttonExport;
	}

	private String compileFileName() {
		StringBuilder filename = new StringBuilder(MessageBundle.getMessage("angal.medicals.stock.txt"));
		if (pbox.isEnabled() && !pbox.getSelectedItem().equals(MessageBundle.getMessage("angal.common.all.txt").toUpperCase())) {
			filename.append('_').append(pbox.getSelectedItem());
		}
		return filename.toString();
	}

	private JButton getJButtonDelete() {
		JButton buttonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDelete.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				medical = (Medical) model.getValueAt(selectedrow, -1);
				int answer = MessageDialog.yesNo(this, "angal.medicals.deletemedical.fmt.msg", medical.getDescription());
				if (answer == JOptionPane.YES_OPTION) {
					try {
						medicalBrowsingManager.deleteMedical(medical);
						medicalList.remove(selectedrow);
						model.fireTableDataChanged();
						table.updateUI();
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			}
		});
		return buttonDelete;
	}

	private JButton getJButtonEdit() {
		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			} else {
				selectedrow = table.convertRowIndexToModel(table.getSelectedRow());
				medical = (Medical) model.getValueAt(selectedrow, -1);
				MedicalEdit editrecord = new MedicalEdit(medical, false, me);
				editrecord.addMedicalListener(this);
				editrecord.setVisible(true);
			}
		});
		return buttonEdit;
	}

	private JButton getJButtonNew() {
		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			medical = new Medical(null, new MedicalType("", ""), "", "", 0, 0, 0, 0);
			MedicalEdit newrecord = new MedicalEdit(medical, true, me);
			newrecord.addMedicalListener(this);
			newrecord.setVisible(true);
		});
		return buttonNew;
	}

	private JComboBox<String> getComboBoxActive() {
		if (activeComboBox == null) {
			activeComboBox = new JComboBox<>();
			activeComboBox.addItem(STR_ACTIVE_ONLY);
			activeComboBox.addItem(STR_ALL);
			activeComboBox.addItem(STR_DISABLED_ONLY);
			activeSelection = STR_ACTIVE_ONLY;
			activeComboBox.setSelectedItem(STR_ACTIVE_ONLY);
		}
		activeComboBox.addActionListener(actionEvent -> {
			activeSelection = activeComboBox.getSelectedItem().toString();
			applyFilter();
			updatePageCombo();
			CURRENT_PAGE = 0;
			loadCurrentPage();
		});
		return activeComboBox;
	}

	private JComboBox<Object> getComboBoxMedicalType() {
		if (pbox == null) {
			pbox = new JComboBox<>();
			pbox.addItem(STR_ALL);
			List<MedicalType> type;
			try {
				type = medicalTypeManager.getAllActiveMedicalType();
				for (MedicalType elem : type) {
					pbox.addItem(elem);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			pSelection = STR_ALL;
			pbox.setSelectedItem(STR_ALL);
		}
		pbox.addActionListener(actionEvent -> {
			pSelection = pbox.getSelectedItem().toString();
			applyFilter();
			updatePageCombo();
			CURRENT_PAGE = 0;
			loadCurrentPage();
		});
		return pbox;
	}

	protected void launchExpiringReport() {
		List<String> options = new ArrayList<>();
		options.add(MessageBundle.getMessage("angal.medicals.today"));
		options.add(MessageBundle.getMessage("angal.medicals.thismonth"));
		options.add(MessageBundle.getMessage("angal.medicals.nextmonth"));
		options.add(MessageBundle.getMessage("angal.medicals.nexttwomonths"));
		options.add(MessageBundle.getMessage("angal.medicals.nextthreemonths"));
		options.add(MessageBundle.getMessage("angal.medicals.othermonth"));

		Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png");
		String option = (String) MessageDialog.inputDialog(this, icon, options.toArray(), options.get(0), "angal.medicals.pleaseselectperiod.msg");
		if (option == null) return;

		String from = null;
		String to = null;
		int index = options.indexOf(option);
		LocalDate today = LocalDate.now();

		switch (index) {
			case 0:
				from = format(today.atStartOfDay());
				to = from;
				break;
			case 1:
				LocalDate startThisMonth = today.withDayOfMonth(1);
				LocalDate endThisMonth = today.with(TemporalAdjusters.lastDayOfMonth());
				from = format(startThisMonth.atStartOfDay());
				to = format(endThisMonth.atTime(LocalTime.MAX));
				break;
			case 2:
				setMonthRange(today, 1);
				from = monthFrom;
				to = monthTo;
				break;
			case 3:
				setMonthRange(today, 2);
				from = monthFrom;
				to = monthTo;
				break;
			case 4:
				setMonthRange(today, 3);
				from = monthFrom;
				to = monthTo;
				break;
			case 5:
				JMonthYearChooser chooser = new JMonthYearChooser();
				int r = JOptionPane.showConfirmDialog(this, chooser, MessageBundle.getMessage("angal.billbrowser.month.txt"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, icon);
				if (r != JOptionPane.OK_OPTION) return;
				LocalDate selectedMonth = chooser.getLocalDate();
				LocalDate start = selectedMonth.withDayOfMonth(1);
				LocalDate end = selectedMonth.with(TemporalAdjusters.lastDayOfMonth());
				from = format(start.atStartOfDay());
				to = format(end.atTime(LocalTime.MAX));
				break;
		}
		new GenericReportFromDateToDate(from, to, "rpt_base", "PharmaceuticalExpiration", MessageBundle.getMessage("angal.medicals.expiringreport"), false);
	}

	private String format(LocalDateTime dateTime) {
		return TimeTools.formatDateTime(dateTime, DATE_FORMAT_DD_MM_YYYY);
	}

	private void setMonthRange(LocalDate today, int monthsAhead) {
		LocalDate start = today.plusMonths(1).withDayOfMonth(1);
		LocalDate end = today.plusMonths(monthsAhead).with(TemporalAdjusters.lastDayOfMonth());
		monthFrom = format(start.atStartOfDay());
		monthTo = format(end.atTime(LocalTime.MAX));
	}

	class MedicalBrowsingModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		private List<Medical> medicalList = new ArrayList<>();

		public MedicalBrowsingModel(String key, String description, boolean isType, int page, int size) {
			if (isType) {
				try {
					Page<Medical> medicalPage;
					if (activeSelection.equals(STR_ACTIVE_ONLY)) {
						medicalPage = medicalBrowsingManager.getMedicalsByTypeAndDescription(key, description, 'N', false, page, size);
					} else if (activeSelection.equals(STR_DISABLED_ONLY)) {
						medicalPage = medicalBrowsingManager.getMedicalsByTypeAndDescription(key, description, 'Y', false, page, size);
					} else {
						medicalPage = medicalBrowsingManager.getMedicalsByTypeAndDescription(key, description, null, false, page, size);
					}
					medicalList = new ArrayList<>(medicalPage.getContent());
					TOTAL_PAGES = medicalPage.getTotalElements();
					PAGES = medicalPage.getTotalPages();
				} catch (OHServiceException e) {
					medicalList = new ArrayList<>();
					TOTAL_PAGES = 0;
					PAGES = 0;
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		}
		@Override
		public Class<?> getColumnClass(int c) {
			if (c == 0) return String.class;
			else if (c == 1) return String.class;
			else if (c == 2) return String.class;
			else if (c == 3) return Integer.class;
			else if (c == 4) return Double.class;
			else if (c == 5) return Double.class;
			else if (c == 6) return Boolean.class;
			return null;
		}

		@Override
		public int getRowCount() {
			if (medicalList == null) return 0;
			return medicalList.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Medical med = medicalList.get(r);
			double actualQty = actualQtyCache.getOrDefault(med.getCode(), med.getInitialqty() + med.getInqty() - med.getOutqty());
			double minQuantity = med.getMinqty();
			if (c == -1) return med;
			else if (c == 0) return med.getType().getDescription();
			else if (c == 1) return med.getProdCode();
			else if (c == 2) return med.getDescription();
			else if (c == 3) return med.getPcsperpck();
			else if (c == 4) return actualQty;
			else if (c == 5) return minQuantity;
			else if (c == 6) return actualQty == 0;
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private boolean highlightexpiringmedical() {
		return GeneralData.HIGHLIGHTEXPIRINGMEDICAL;
	}

	private int highlightexpiringmedicaldays() {
		return GeneralData.HIGHLIGHTEXPIRINGMEDICALDAYS;
	}

	class ColorTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		                                               boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			((JLabel) cell).setOpaque(true);

			if (table.getModel() instanceof MedicalBrowsingModel) {
				MedicalBrowsingModel model = (MedicalBrowsingModel) table.getModel();
				if (row < model.getRowCount()) {
					Medical med = (Medical) model.getValueAt(row, -1);
					if (med != null) {
						double actualQty = actualQtyCache.getOrDefault(med.getCode(), med.getInitialqty() + med.getInqty() - med.getOutqty());
						Color bgColor = Color.WHITE;
						Color fgColor = Color.BLACK;

						if (highlightexpiringmedical()) {
							LocalDate expiryDate = expiryDateCache.get(med.getCode());
							LocalDate today = LocalDate.now();

							if (expiryDate != null) {
								if (expiryDate.isBefore(today)) {
									bgColor = Color.DARK_GRAY;
									fgColor = Color.WHITE;
								} else if (ChronoUnit.DAYS.between(today, expiryDate) <= highlightexpiringmedicaldays()) {
									bgColor = Color.ORANGE;
									fgColor = Color.BLACK;
								}
							}
						}

						cell.setBackground(bgColor);
						cell.setForeground(fgColor);

						if (actualQty == 0) {
							cell.setForeground(Color.GRAY);
						}
						if (med.getMinqty() != 0 && actualQty <= med.getMinqty()) {
							cell.setForeground(Color.RED);
						}
						if (activeSelection.equals(STR_ALL) && med.getDeleted() == 'Y') {
							Map attributes = cell.getFont().getAttributes();
							attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
							cell.setFont(new Font(attributes));
							cell.setForeground(Color.GRAY);
						}
					}
				}
			}

			if (isSelected) {
				cell.setBackground(table.getSelectionBackground());
				cell.setForeground(table.getSelectionForeground());
			}

			return cell;
		}
	}

	private JPanel getPaginationPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
		panel.setBorder(BorderFactory.createEtchedBorder());

		prevButton = new JButton("<");
		prevButton.addActionListener(e -> {
			if (CURRENT_PAGE > 0) {
				CURRENT_PAGE--;
				loadCurrentPage();
			}
		});

		pagesCombo = new JComboBox<>();
		pagesCombo.setPreferredSize(new Dimension(70, 25));
		pagesCombo.addActionListener(e -> {
			if (!updatingPageCombo && pagesCombo.getSelectedItem() != null) {
				int selected = (Integer) pagesCombo.getSelectedItem();
				if (selected - 1 != CURRENT_PAGE) {
					CURRENT_PAGE = selected - 1;
					loadCurrentPage();
				}
			}
		});

		nextButton = new JButton(">");
		nextButton.addActionListener(e -> {
			if (CURRENT_PAGE < PAGES - 1) {
				CURRENT_PAGE++;
				loadCurrentPage();
			}
		});

		underLabel = new JLabel("/ 0 Pages");
		totalMedicalsLabel = new JLabel(MessageBundle.getMessage("angal.medicals.totalmovement.txt") + ": 0");

		panel.add(prevButton);
		panel.add(pagesCombo);
		panel.add(underLabel);
		panel.add(nextButton);
		panel.add(totalMedicalsLabel);

		return panel;
	}

	private void updatePaginationControls() {
		if (prevButton == null || nextButton == null || pagesCombo == null) {
			return;
		}

		boolean hasMultiplePages = PAGES > 1;

		if (hasMultiplePages && pagesCombo.getItemCount() != PAGES && PAGES > 0) {
			updatingPageCombo = true;
			pagesCombo.removeAllItems();
			for (int i = 1; i <= PAGES; i++) {
				pagesCombo.addItem(i);
			}
			updatingPageCombo = false;
		}

		if (hasMultiplePages && PAGES > 0) {
			updatingPageCombo = true;
			pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
			updatingPageCombo = false;
		}

		prevButton.setEnabled(CURRENT_PAGE > 0 && hasMultiplePages);
		nextButton.setEnabled(CURRENT_PAGE < PAGES - 1 && hasMultiplePages);
		pagesCombo.setEnabled(hasMultiplePages);

		if (PAGES <= 0) {
			underLabel.setText("/ 0 Pages");
			pagesCombo.setEnabled(false);
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
		} else {
			underLabel.setText("/ " + PAGES + " Pages");
		}

		totalMedicalsLabel.setText(MessageBundle.getMessage("angal.medicals.totalmovement.txt") + ": " + TOTAL_PAGES);
	}

	private void loadCurrentPage() {
		try {
			boolean showActive = false;
			boolean showDisabled = false;

			if (activeSelection.equals(STR_ACTIVE_ONLY)) {
				showActive = true;
				showDisabled = false;
			} else if (activeSelection.equals(STR_DISABLED_ONLY)) {
				showActive = false;
				showDisabled = true;
			} else if (activeSelection.equals(STR_ALL)) {
				showActive = true;
				showDisabled = true;
			}

			String medicalTypeCode = null;
			if (pbox.getSelectedItem() instanceof MedicalType) {
				medicalTypeCode = ((MedicalType) pbox.getSelectedItem()).getCode();
			}

			Character active = null;
			if (showActive && !showDisabled) {
				active = 'N';
			} else if (!showActive && showDisabled) {
				active = 'Y';
			} else if (showActive && showDisabled) {
				active = null;
			}

			boolean nameSorted = false;

			Page<Medical> medicalPage = medicalBrowsingManager.getMedicalsByTypeAndDescription(
					medicalTypeCode,
					searchString.getText().trim(),
					active,
					nameSorted,
					CURRENT_PAGE,
					PAGE_SIZE
			);

			if (medicalPage != null) {
				medicalList = new ArrayList<>(medicalPage.getContent());
				TOTAL_PAGES = medicalPage.getTotalElements();
				PAGES = medicalPage.getTotalPages();
			} else {
				medicalList = new ArrayList<>();
				TOTAL_PAGES = 0;
				PAGES = 0;
			}

			precomputeCaches();

			if (model instanceof MedicalBrowsingModel) {
				((MedicalBrowsingModel) model).medicalList = medicalList;
				((MedicalBrowsingModel) model).fireTableDataChanged();
			} else {
				model = new MedicalBrowsingModel("", "", true, CURRENT_PAGE, PAGE_SIZE);
				table.setModel(model);
			}

			table.updateUI();
			updatePaginationControls();

		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			medicalList = new ArrayList<>();
			TOTAL_PAGES = 0;
			PAGES = 0;
			updatePaginationControls();
		}
	}

	private void precomputeCaches() throws OHServiceException {
		actualQtyCache = new HashMap<>();
		for (Medical med : medicalList) {
			actualQtyCache.put(med.getCode(), med.getInitialqty() + med.getInqty() - med.getOutqty());
		}

		expiryDateCache = new HashMap<>();
		if (highlightexpiringmedical() && !medicalList.isEmpty()) {
			List<Integer> codes = new ArrayList<>();
			for (Medical med : medicalList) {
				codes.add(med.getCode());
			}
			expiryDateCache = movStockInsertingManager.getNearestExpiryDateByMedicals(codes);
		}
	}

	public void updatePageCombo() {
		totalMedicalsLabel.setText(MessageBundle.getMessage("angal.medicals.totalmovement.txt") + ": " + TOTAL_PAGES);
		initializeCombo(PAGES);
		underLabel.setText("/ " + PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton(">");
			nextButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE < PAGES - 1) {
					CURRENT_PAGE++;
					loadCurrentPage();
				}
			});
		}
		return nextButton;
	}

	private JButton getPrevButton() {
		if (prevButton == null) {
			prevButton = new JButton("<");
			prevButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE > 0) {
					CURRENT_PAGE--;
					loadCurrentPage();
				}
			});
		}
		return prevButton;
	}

	public void initializeCombo(int page) {
		pagesCombo.removeAllItems();
		for (int i = 0; i < page; i++) {
			pagesCombo.addItem(i + 1);
		}
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo == null) {
			pagesCombo = new JComboBox<>();
			pagesCombo.setPreferredSize(new Dimension(100, 25));
			pagesCombo.addActionListener(actionEvent -> {
				if (pagesCombo.getItemCount() != 0 && pagesCombo.getSelectedItem() != null && !updatingPageCombo) {
					int selected = (Integer) pagesCombo.getSelectedItem();
					if (selected - 1 != CURRENT_PAGE) {
						CURRENT_PAGE = selected - 1;
						loadCurrentPage();
					}
				}
			});
		}
		return pagesCombo;
	}

	private void applyFilter() {
		if ((pSelection == null) || (pSelection.compareTo(STR_ALL) == 0)) {
			pSelection = "";
		}
		CURRENT_PAGE = 0;
		loadCurrentPage();
	}

	private JLabel getUnderLabel() {
		if (underLabel == null) {
			underLabel = new JLabel("/ " + PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			underLabel.setPreferredSize(new Dimension(60, 30));
		}
		return underLabel;
	}

	private JLabel getTotalMovementsLabel() {
		if (totalMedicalsLabel == null) {
			totalMedicalsLabel = new JLabel(MessageBundle.getMessage("angal.medicals.totalmovement.txt") + ": " + TOTAL_PAGES);
		}
		return totalMedicalsLabel;
	}
}