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
package org.isf.accounting.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.isf.generaldata.MessageBundle;
import org.isf.priceslist.model.Price;

/**
 * A small modal dialog offering a live-filtered, searchable list of {@link Price} items, replacing
 * {@code JOptionPane.showInputDialog(...)}'s unfiltered combo box at the four Add-item entry points
 * in {@link PatientBillEdit}. Filtering is case- and accent-insensitive (see
 * {@link BillItemSearchSupport}).
 */
class SearchableItemDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final List<Price> allItems;
	private final JTextField searchField = new JTextField(20);
	private final DefaultListModel<Price> listModel = new DefaultListModel<>();
	private final JList<Price> list = new JList<>(listModel);
	private Price selected;

	private SearchableItemDialog(Window owner, String title, Icon icon, String promptMessage, List<Price> items) {
		super(owner, title, ModalityType.APPLICATION_MODAL);
		this.allItems = items;

		JLabel promptLabel = new JLabel(promptMessage, icon, JLabel.LEFT);
		JPanel searchFieldRow = new JPanel(new BorderLayout());
		searchFieldRow.add(new JLabel(MessageBundle.getMessage("angal.priceslist.search.txt")), BorderLayout.WEST);
		searchFieldRow.add(searchField, BorderLayout.CENTER);

		JPanel top = new JPanel(new BorderLayout());
		top.add(promptLabel, BorderLayout.NORTH);
		top.add(searchFieldRow, BorderLayout.SOUTH);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		okButton.addActionListener(actionEvent -> confirmSelection());
		cancelButton.addActionListener(actionEvent -> cancelSelection());
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		setLayout(new BorderLayout());
		add(top, BorderLayout.NORTH);
		add(new JScrollPane(list), BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent event) {
				refreshList(searchField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				refreshList(searchField.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				refreshList(searchField.getText());
			}
		});

		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					confirmSelection();
				}
			}
		});

		searchField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					confirmSelection();
				} else if (event.getKeyCode() == KeyEvent.VK_DOWN && !listModel.isEmpty()) {
					list.requestFocusInWindow();
					list.setSelectedIndex(0);
				}
			}
		});

		getRootPane().registerKeyboardAction(actionEvent -> cancelSelection(),
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		refreshList("");

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(420, 380);
		setLocationRelativeTo(owner);
	}

	private void refreshList(String filterText) {
		Price previouslySelected = list.getSelectedValue();
		listModel.clear();
		for (Price item : BillItemSearchSupport.filter(allItems, filterText)) {
			listModel.addElement(item);
		}
		if (previouslySelected != null && listModel.contains(previouslySelected)) {
			list.setSelectedValue(previouslySelected, true);
		} else if (!listModel.isEmpty()) {
			list.setSelectedIndex(0);
		}
	}

	private void confirmSelection() {
		selected = list.getSelectedValue();
		dispose();
	}

	private void cancelSelection() {
		selected = null;
		dispose();
	}

	/**
	 * Shows a modal searchable picker over {@code items} and returns the selected {@link Price}, or
	 * {@code null} if the user cancelled - the same return contract
	 * {@code JOptionPane.showInputDialog(...)} has at the call sites this replaces.
	 */
	static Price show(Component parent, String title, Icon icon, String promptMessage, List<Price> items) {
		Window owner = SwingUtilities.getWindowAncestor(parent);
		SearchableItemDialog dialog = new SearchableItemDialog(owner, title, icon, promptMessage, items);
		dialog.setVisible(true);
		return dialog.selected;
	}
}
