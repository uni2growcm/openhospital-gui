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
package org.isf.priceslist.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Box;
import java.awt.FlowLayout;

import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.Price;
import org.isf.priceslist.model.PriceList;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.treetable.JTreeTable;

/**
 * Browsing of table PriceList
 *
 * @author Alessandro
 */
public class PricesBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jPanelNorth;
	private JComboBox jComboBoxLists;
	private JScrollPane jScrollPaneList;
	private JTreeTable jTreeTable;
	private JPanel jPanelButtons;
	private JButton jButtonSave;
	private JButton jButtonCancel;
	private JLabel jLabelDescription;
	private JPanel jPanelSelection;
	private JPanel jPanelConfig;
	private JButton jButtonManage;
	private JButton jPrintTableButton;
	private JPanel jPanelDescription;
	protected static String[] cCategories = { "EXA", "OPE", "MED", "OTH" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	protected static String[] cCategoriesNames = { MessageBundle.getMessage("angal.priceslist.exams"), MessageBundle.getMessage("angal.priceslist.operations"),
			MessageBundle.getMessage("angal.priceslist.medicals"),
			MessageBundle.getMessage("angal.priceslist.others") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private boolean[] columnsResizable = { true, false };
	private int[] columnWidth = { 400, 150 };

	private PriceListManager priceListManager = Context.getApplicationContext().getBean(PriceListManager.class);
	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);

	private List<PriceList> listArray;
	private List<Price> priceArray;
	private PriceList listSelected;

	private PriceNode examNodes;
	private List<Exam> examArray;

	private PriceNode opeNodes;
	private List<Operation> operArray;

	private PriceNode medNodes;
	private List<Medical> mediArray;

	private PriceNode othNodes;
	private List<PricesOthers> othArray;
	private JTextField searchField;

	public PricesBrowser() {
		updateFromDB();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		filterPrices("");
		setVisible(true);
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
		setForeground(Color.black);
		checkLists();
		add(getJPanelNorth(), BorderLayout.NORTH);
		add(getJScrollPaneList(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.priceslist.pricebrowser.title"));
		setSize(900, 600);
	}

	private void checkLists() {
		if (listArray.isEmpty()) {
			MessageDialog.error(null, "angal.priceslist.pleasecreatealistfirst");
			ListBrowser browseList = new ListBrowser();
			browseList.setVisible(true);
			dispose();
		}
	}

	private JButton getPrintTableButton() {
		if (jPrintTableButton == null) {
			jPrintTableButton = new JButton(MessageBundle.getMessage("angal.priceslist.printing.btn"));
			jPrintTableButton.setMnemonic(MessageBundle.getMnemonic("angal.priceslist.printing.btn.key"));
			jPrintTableButton.setVisible(true);
			jPrintTableButton.addActionListener(actionEvent -> {

				try {
					printManager.print("PriceList", priceListManager.convertPrice(listSelected, priceArray), 0);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
				}
			});
		}
		return jPrintTableButton;
	}

	private JButton getJButtonManage() {
		if (jButtonManage == null) {
			jButtonManage = new JButton(MessageBundle.getMessage("angal.priceslist.managelists.btn"));
			jButtonManage.setMnemonic(MessageBundle.getMnemonic("angal.priceslist.managelists.btn.key"));
			jButtonManage.addActionListener(actionEvent -> {
					ListBrowser browseList = new ListBrowser();
					browseList.setVisible(true);
					dispose();
			});
		}
		return jButtonManage;
	}

	private JPanel getJPanelConfig() {
		if (jPanelConfig == null) {
			jPanelConfig = new JPanel();
			jPanelConfig.setLayout(new FlowLayout(FlowLayout.RIGHT));
			jPanelConfig.add(getJButtonManage());
		}
		return jPanelConfig;
	}

	protected void updateDescription() {
		jLabelDescription.setText(getTextDescription());

	}

	private JPanel getJPanelSelection() {
		if (jPanelSelection == null) {
			jPanelSelection = new JPanel();
			jPanelSelection.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelSelection.add(getJComboBoxLists());
		}
		return jPanelSelection;
	}

	private JLabel getJLabelDescription() {
		if (jLabelDescription == null) {
			jLabelDescription = new JLabel(getTextDescription());
		}
		return jLabelDescription;
	}

	private String getTextDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(listSelected.getDescription().toUpperCase());
		sb.append(" (");
		sb.append(listSelected.getCurrency());
		sb.append(')');
		return sb.toString();
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}

	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			jButtonSave.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			jButtonSave.addActionListener(actionEvent -> {
				int option = JOptionPane.showConfirmDialog(null,
						MessageBundle.getMessage("angal.priceslist.thiswillsavecurrentpricescontinue"),  //$NON-NLS-1$
						MessageBundle.getMessage("angal.priceslist.savelist"),  //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION);

				if (option == 0) {

					List<Price> updateList = convertTreeToArray();
					try {
						priceListManager.updatePrices(listSelected, updateList);
						MessageDialog.info(null, "angal.priceslist.listsaved");
						updateFromDB();

						String currentSearch = searchField.getText();
						filterPrices(currentSearch);

						validate();
						repaint();
					} catch (OHServiceException e) {
						MessageDialog.error(null, "angal.priceslist.listcouldnotbesaved");
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			});
		}
		return jButtonSave;
	}

	private List<Price> convertTreeToArray() {
		Map<String, Price> allPrices = new HashMap<>();
		for (Price price : priceArray) {
			String key = price.getGroup() + "|" + price.getItem();
			allPrices.put(key, price);
		}

		PriceNode currentRoot = (PriceNode) jTreeTable.getTree().getModel().getRoot();
		for (int cat = 0; cat < currentRoot.getItems().length; cat++) {
			PriceNode categoryNode = (PriceNode) currentRoot.getItems()[cat];
			for (int i = 0; i < categoryNode.getItems().length; i++) {
				PriceNode itemNode = (PriceNode) categoryNode.getItems()[i];
				Price modifiedPrice = itemNode.getPrice();

				String key = modifiedPrice.getGroup() + "|" + modifiedPrice.getItem();
				Price original = allPrices.get(key);
				if (original != null) {
					original.setPrice(modifiedPrice.getPrice());
				}
			}
		}

		return new ArrayList<>(allPrices.values());
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonSave());
			jPanelButtons.add(getPrintTableButton());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JTreeTable getJTreeList() {
		if (jTreeTable == null) {

			updateFromDB();
		    PriceNode root = getTreeContent();

		    jTreeTable = new JTreeTable(new PriceModel(root));

		    jTreeTable.getTree().expandRow(4);
		    jTreeTable.getTree().expandRow(3);
		    jTreeTable.getTree().expandRow(2);
		    jTreeTable.getTree().expandRow(1);

			for (int i = 0; i < columnWidth.length; i++) {
				jTreeTable.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);

				if (!columnsResizable[i]) {
					jTreeTable.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i]);
				}
			}
		    jTreeTable.setAutoCreateColumnsFromModel(false);
		}
		return jTreeTable;
	}

	private void updateFromDB() {

		try {
			listArray = priceListManager.getLists();
			priceArray = priceListManager.getPrices();
			examArray = examBrowsingManager.getExams();
			operArray = operationBrowserManager.getOperation();
			mediArray = medicalBrowsingManager.getMedicalsSortedByName();
			othArray = pricesOthersManager.getOthers();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private PriceNode getTreeContent() {

		Map<String, Price> priceHashTable = new HashMap<>();
		for (Price price : priceArray) {
			priceHashTable.put(price.getList().getId() +
					price.getGroup() +
					price.getItem(), price);
		}

		examNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[0], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (Exam exa : examArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[0] + exa.getCode());
			double priceValue = p != null ? p.getPrice() : 0.;
			examNodes.addItem(new PriceNode(new Price(null, cCategories[0], exa.getCode(), exa.getDescription(), priceValue)));
		}

		opeNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[1], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (Operation ope : operArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[1] + ope.getCode());
			double priceValue = p != null ? p.getPrice() : 0.;
			opeNodes.addItem(new PriceNode(new Price(null, cCategories[1], ope.getCode(), ope.getDescription(), priceValue)));
		}

		medNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[2], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (Medical med : mediArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[2] + med.getCode().toString());
			double priceValue = p != null ? p.getPrice() : 0.;
			medNodes.addItem(new PriceNode(new Price(null, cCategories[2], med.getCode().toString(), med.getDescription(), priceValue)));
		}

		othNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[3], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (PricesOthers oth : othArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[3] + oth.getId());
			double priceValue = p != null ? p.getPrice() : 0.;
			othNodes.addItem(
					new PriceNode(new Price(null, cCategories[3], Integer.toString(oth.getId()), oth.getDescription(), priceValue, !oth.isUndefined())));
		}

		PriceNode root = new PriceNode(new Price(null, "", "", listSelected.getName(), null)); //$NON-NLS-1$ //$NON-NLS-2$
		root.addItem(examNodes);
		root.addItem(opeNodes);
		root.addItem(medNodes);
		root.addItem(othNodes);

		return root;
	}

	private JScrollPane getJScrollPaneList() {
		if (jScrollPaneList == null) {
			jScrollPaneList = new JScrollPane();
			jScrollPaneList.setViewportView(getJTreeList());
		}
		return jScrollPaneList;
	}

	private JComboBox getJComboBoxLists() {
		if (jComboBoxLists == null) {
			jComboBoxLists = new JComboBox();
			for (PriceList elem : listArray) {
				jComboBoxLists.addItem(elem);
			}
			jComboBoxLists.addActionListener(actionEvent -> {

				int option = JOptionPane.showConfirmDialog(null,
						MessageBundle.getMessage("angal.priceslist.doyoureallywanttochangelist"),  //$NON-NLS-1$
						MessageBundle.getMessage("angal.priceslist.changelist"),  //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION);

				if (option == 0) {
					listSelected = (PriceList) jComboBoxLists.getSelectedItem();

					updateFromDB();

					String currentSearch = searchField.getText();
					filterPrices(currentSearch);

					updateDescription();
					validate();
					repaint();
				} else {
					jComboBoxLists.setSelectedItem(listSelected);
				}
			});
			listSelected = (PriceList) jComboBoxLists.getSelectedItem();
			jComboBoxLists.setDoubleBuffered(false);
			jComboBoxLists.setBorder(null);
		}
		return jComboBoxLists;
	}

	private JPanel getJPanelNorth() {
		if (jPanelNorth == null) {
			jPanelNorth = new JPanel();
			jPanelNorth.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
			JLabel searchLabel = new JLabel(MessageBundle.getMessage("angal.common.search.txt") + ": ");
			searchPanel.add(searchLabel);

			searchField = new JTextField(15);
			searchField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					filterPrices(searchField.getText());
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					filterPrices(searchField.getText());
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					filterPrices(searchField.getText());
				}
			});
			searchPanel.add(searchField);
			jPanelNorth.add(searchPanel);
			jPanelNorth.add(Box.createHorizontalStrut(10));
			jPanelNorth.add(getJPanelSelection());
			jPanelNorth.add(getJPanelDescription());
			jPanelNorth.add(getJPanelConfig());
		}
		return jPanelNorth;
	}

	private JPanel getJPanelDescription() {
		if (jPanelDescription == null) {
			jPanelDescription = new JPanel();
			jPanelDescription.add(getJLabelDescription());
		}
		return jPanelDescription;
	}

	private void filterPrices(String searchText) {
		String searchLower = searchText.toLowerCase().trim();

		if (searchLower.isEmpty()) {
			updateFromDB();
			PriceNode root = getTreeContent();
			jTreeTable.setModel(new PriceModel(root));
			jTreeTable.getTree().expandRow(3);
			jTreeTable.getTree().expandRow(2);
			jTreeTable.getTree().expandRow(1);
			jTreeTable.updateUI();
			return;
		}

		Map<String, Price> priceHashTable = new HashMap<>();
		for (Price price : priceArray) {
			priceHashTable.put(price.getList().getId() +
					price.getGroup() +
					price.getItem(), price);
		}

		PriceNode filteredExamNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[0], null));
		for (Exam exa : examArray) {
			if (exa.getCode().toLowerCase().contains(searchLower) ||
					exa.getDescription().toLowerCase().contains(searchLower)) {
				Price p = priceHashTable.get(listSelected.getId() + cCategories[0] + exa.getCode());
				double priceValue = p != null ? p.getPrice() : 0.;
				filteredExamNodes.addItem(new PriceNode(new Price(null, cCategories[0], exa.getCode(), exa.getDescription(), priceValue)));
			}
		}

		PriceNode filteredOpeNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[1], null));
		for (Operation ope : operArray) {
			if (ope.getCode().toLowerCase().contains(searchLower) ||
					ope.getDescription().toLowerCase().contains(searchLower)) {
				Price p = priceHashTable.get(listSelected.getId() + cCategories[1] + ope.getCode());
				double priceValue = p != null ? p.getPrice() : 0.;
				filteredOpeNodes.addItem(new PriceNode(new Price(null, cCategories[1], ope.getCode(), ope.getDescription(), priceValue)));
			}
		}

		PriceNode filteredMedNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[2], null));
		for (Medical med : mediArray) {
			if (med.getCode().toString().toLowerCase().contains(searchLower) ||
					med.getDescription().toLowerCase().contains(searchLower)) {
				Price p = priceHashTable.get(listSelected.getId() + cCategories[2] + med.getCode().toString());
				double priceValue = p != null ? p.getPrice() : 0.;
				filteredMedNodes.addItem(new PriceNode(new Price(null, cCategories[2], med.getCode().toString(), med.getDescription(), priceValue)));
			}
		}

		PriceNode filteredOthNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[3], null));
		for (PricesOthers oth : othArray) {
			if (oth.getDescription().toLowerCase().contains(searchLower)) {
				Price p = priceHashTable.get(listSelected.getId() + cCategories[3] + oth.getId());
				double priceValue = p != null ? p.getPrice() : 0.;
				filteredOthNodes.addItem(
						new PriceNode(new Price(null, cCategories[3], Integer.toString(oth.getId()), oth.getDescription(), priceValue, !oth.isUndefined())));
			}
		}

		PriceNode root = new PriceNode(new Price(null, "", "", listSelected.getName(), null));
		root.addItem(filteredExamNodes);
		root.addItem(filteredOpeNodes);
		root.addItem(filteredMedNodes);
		root.addItem(filteredOthNodes);

		jTreeTable.setModel(new PriceModel(root));
		jTreeTable.getTree().expandRow(3);
		jTreeTable.getTree().expandRow(2);
		jTreeTable.getTree().expandRow(1);
		jTreeTable.updateUI();
	}
}
