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
package org.isf.articlefamily.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.isf.articlefamily.manager.ArticleFamilyBrowserManager;
import org.isf.articlefamily.model.ArticleFamily;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Manages the list of {@link ArticleFamily} (family/category a medical belongs to).
 */
public class ArticleFamilyBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private final ArticleFamilyBrowserManager articleFamilyManager = Context.getApplicationContext().getBean(ArticleFamilyBrowserManager.class);
	private JTable table;
	private DefaultTableModel tableModel;
	private JTextField searchField;

	private static final String[] COLUMNS = {
			"ID",
			MessageBundle.getMessage("angal.articlefamily.code.col"),
			MessageBundle.getMessage("angal.articlefamily.description.col"),
	};

	public ArticleFamilyBrowser() {
		initComponents();
		loadArticleFamilies(null);
		setTitle(MessageBundle.getMessage("angal.articlefamily.browser.title"));
		setSize(600, 450);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchField = new JTextField(25);
		searchField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				loadArticleFamilies(searchField.getText().trim());
			}
		});
		searchPanel.add(new JLabel(MessageBundle.getMessage("angal.common.search.txt")));
		searchPanel.add(searchField);
		add(searchPanel, BorderLayout.NORTH);

		tableModel = new DefaultTableModel(COLUMNS, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getColumn(0).setMinWidth(0);
		table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);
		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton newButton = new JButton(MessageBundle.getMessage("angal.articlefamily.new.btn"));
		JButton editButton = new JButton(MessageBundle.getMessage("angal.articlefamily.edit.btn"));
		JButton deleteButton = new JButton(MessageBundle.getMessage("angal.articlefamily.delete.btn"));
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));

		newButton.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.new.btn.key"));
		editButton.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.edit.btn.key"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.articlefamily.delete.btn.key"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));

		newButton.addActionListener(actionEvent -> openEditor(null));
		editButton.addActionListener(actionEvent -> {
			ArticleFamily selected = getSelectedArticleFamily();
			if (selected != null) {
				openEditor(selected);
			}
		});
		deleteButton.addActionListener(actionEvent -> deleteSelected());
		closeButton.addActionListener(actionEvent -> dispose());

		buttonPanel.add(newButton);
		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadArticleFamilies(String filter) {
		tableModel.setRowCount(0);
		try {
			List<ArticleFamily> families = articleFamilyManager.searchArticleFamilies(filter);
			for (ArticleFamily family : families) {
				tableModel.addRow(new Object[] { family.getId(), family.getCode(), family.getDescription() });
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private ArticleFamily getSelectedArticleFamily() {
		int row = table.getSelectedRow();
		if (row < 0) {
			MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			return null;
		}
		int id = (int) tableModel.getValueAt(row, 0);
		try {
			return articleFamilyManager.getArticleFamily(id).orElse(null);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return null;
		}
	}

	private void openEditor(ArticleFamily articleFamily) {
		ArticleFamilyEdit editor = new ArticleFamilyEdit(this, articleFamilyManager, articleFamily,
						savedFamily -> loadArticleFamilies(searchField.getText().trim()));
		editor.setVisible(true);
	}

	private void deleteSelected() {
		ArticleFamily selected = getSelectedArticleFamily();
		if (selected == null) {
			return;
		}
		int confirm = MessageDialog.yesNo(this,
						MessageBundle.formatMessage("angal.articlefamily.delete.confirm.fmt.msg", selected.getCode() + " - " + selected.getDescription()));
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				articleFamilyManager.deleteArticleFamily(selected.getId());
				loadArticleFamilies(searchField.getText().trim());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
	}
}
