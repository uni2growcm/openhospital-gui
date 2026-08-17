/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	private JPanel jPanelTop;
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
	private JPanel jPanelSearch;
	private JTextField jTextFieldSearch;
	private String currentSearchText = "";
	protected static String[] cCategories = { "EXA", "OPE", "MED", "OTH" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	protected static String[] cCategoriesNames = { MessageBundle.getMessage("angal.priceslist.exams"), MessageBundle.getMessage("angal.priceslist.operations"),
			MessageBundle.getMessage("angal.priceslist.medicals"),
			MessageBundle.getMessage("angal.priceslist.others") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private boolean[] columnsResizable = { true, false, false };
	private int[] columnWidth = { 400, 150, 80 };

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

	public PricesBrowser() {
		updateFromDB();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
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
		setSize(647, 440);
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

	/**
	 * Second row of the north panel: holds the "Rechercher" label and the search text field,
	 * kept separate from {@link #getJPanelSelection()} so the combo/description/manage row and
	 * the search row can be stacked on their own lines.
	 */
	private JPanel getJPanelSearch() {
		if (jPanelSearch == null) {
			jPanelSearch = new JPanel();
			jPanelSearch.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelSearch.add(new JLabel(MessageBundle.getMessage("angal.priceslist.search.txt")));
			jPanelSearch.add(getJTextFieldSearch());
		}
		return jPanelSearch;
	}

	private JTextField getJTextFieldSearch() {
		if (jTextFieldSearch == null) {
			jTextFieldSearch = new JTextField(15);
			jTextFieldSearch.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void insertUpdate(DocumentEvent event) {
					filterPrices();
				}

				@Override
				public void removeUpdate(DocumentEvent event) {
					filterPrices();
				}

				@Override
				public void changedUpdate(DocumentEvent event) {
					filterPrices();
				}
			});
		}
		return jTextFieldSearch;
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
						refreshTree();
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
		List<Price> listPrices = new ArrayList<>();
		for (int i = 0; i < examNodes.getItems().length; i++) {
			PriceNode newPriceNode = (PriceNode) examNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		for (int i = 0; i < opeNodes.getItems().length; i++) {
			PriceNode newPriceNode = (PriceNode) opeNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		for (int i = 0; i < medNodes.getItems().length; i++) {
			PriceNode newPriceNode = (PriceNode) medNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		for (int i = 0; i < othNodes.getItems().length; i++) {
			PriceNode newPriceNode = (PriceNode) othNodes.getItems()[i];
			listPrices.add(newPriceNode.getPrice());
		}
		return listPrices;
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
			getTreeContent();

			jTreeTable = new JTreeTable(new PriceModel(buildFilteredRoot(currentSearchText)));

			expandAllRows();

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
			Price leaf = new Price(null, cCategories[0], exa.getCode(), exa.getDescription(), priceValue);
			if (p != null) {
				leaf.setVariable(p.isVariable());
			}
			examNodes.addItem(new PriceNode(leaf));
		}

		opeNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[1], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (Operation ope : operArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[1] + ope.getCode());
			double priceValue = p != null ? p.getPrice() : 0.;
			Price leaf = new Price(null, cCategories[1], ope.getCode(), ope.getDescription(), priceValue);
			if (p != null) {
				leaf.setVariable(p.isVariable());
			}
			opeNodes.addItem(new PriceNode(leaf));
		}

		medNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[2], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (Medical med : mediArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[2] + med.getCode().toString());
			double priceValue = p != null ? p.getPrice() : 0.;
			Price leaf = new Price(null, cCategories[2], med.getCode().toString(), med.getDescription(), priceValue);
			if (p != null) {
				leaf.setVariable(p.isVariable());
			}
			medNodes.addItem(new PriceNode(leaf));
		}

		othNodes = new PriceNode(new Price(null, "", "", cCategoriesNames[3], null)); //$NON-NLS-1$ //$NON-NLS-2$
		for (PricesOthers oth : othArray) {
			Price p = priceHashTable.get(listSelected.getId() + cCategories[3] + oth.getId());
			double priceValue = p != null ? p.getPrice() : 0.;
			Price leaf = new Price(null, cCategories[3], Integer.toString(oth.getId()), oth.getDescription(), priceValue, !oth.isUndefined());
			if (p != null) {
				leaf.setVariable(p.isVariable());
			}
			othNodes.addItem(new PriceNode(leaf));
		}

		PriceNode root = new PriceNode(new Price(null, "", "", listSelected.getName(), null)); //$NON-NLS-1$ //$NON-NLS-2$
		root.addItem(examNodes);
		root.addItem(opeNodes);
		root.addItem(medNodes);
		root.addItem(othNodes);

		return root;
	}

	/**
	 * Rebuilds the category fields ({@code examNodes}/{@code opeNodes}/{@code medNodes}/{@code othNodes})
	 * from the database, then re-installs the tree model, applying the currently active search
	 * filter on top. Used whenever the underlying data changes (initial load, list switch, after
	 * save) so the active search term is never silently dropped.
	 */
	private void refreshTree() {
		updateFromDB();
		getTreeContent();
		filterPrices();
	}

	/**
	 * Rebuilds the displayed tree from the search field's current text, filtering the *existing*
	 * in-memory category nodes rather than reloading from the database - so an edited-but-unsaved
	 * price keeps its edited value whether or not it's currently visible.
	 */
	private void filterPrices() {
		currentSearchText = jTextFieldSearch.getText();
		jTreeTable.setModel(new PriceModel(buildFilteredRoot(currentSearchText)));
		expandAllRows();
	}

	/**
	 * Builds a filtered view of the in-memory tree: each category keeps only the leaf nodes whose
	 * description contains {@code filterText} (case-insensitive; a blank filter keeps everything),
	 * reusing the same leaf {@link PriceNode} instances (not copies) so in-progress edits survive
	 * filtering. A category with no matching leaves is omitted entirely.
	 */
	private PriceNode buildFilteredRoot(String filterText) {
		PriceNode root = new PriceNode(new Price(null, "", "", listSelected.getName(), null)); //$NON-NLS-1$ //$NON-NLS-2$
		addFilteredCategory(root, examNodes, filterText);
		addFilteredCategory(root, opeNodes, filterText);
		addFilteredCategory(root, medNodes, filterText);
		addFilteredCategory(root, othNodes, filterText);
		return root;
	}

	private void addFilteredCategory(PriceNode root, PriceNode category, String filterText) {
		PriceNode filteredCategory = PriceListFilterSupport.filterCategory(category, filterText);
		if (filteredCategory != null) {
			root.addItem(filteredCategory);
		}
	}

	/**
	 * Expands every row currently in the tree, from the last row up to (but excluding) the root -
	 * descending order so expanding a row (which inserts its children right after it) never shifts
	 * the index of a row not yet processed. Unlike a hardcoded {@code expandRow(1..4)} sequence,
	 * this adapts to however many category rows are actually present, which varies once filtering
	 * can omit a category entirely.
	 */
	private void expandAllRows() {
		for (int row = jTreeTable.getRowCount() - 1; row >= 1; row--) {
			jTreeTable.getTree().expandRow(row);
		}
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

					refreshTree();

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

	/**
	 * Top row of the north panel: combo box, description label and the "manage lists" button,
	 * laid out side by side as before.
	 */
	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			jPanelTop = new JPanel();
			jPanelTop.setLayout(new BoxLayout(jPanelTop, BoxLayout.X_AXIS));
			jPanelTop.add(getJPanelSelection());
			jPanelTop.add(getJPanelDescription());
			jPanelTop.add(getJPanelConfig());
		}
		return jPanelTop;
	}

	/**
	 * North panel now stacks two rows vertically: the top row (combo/description/manage button)
	 * and, below it, the search row (label + text field).
	 */
	private JPanel getJPanelNorth() {
		if (jPanelNorth == null) {
			jPanelNorth = new JPanel();
			jPanelNorth.setLayout(new BoxLayout(jPanelNorth, BoxLayout.Y_AXIS));
			jPanelNorth.add(getJPanelTop());
			jPanelNorth.add(getJPanelSearch());
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

}