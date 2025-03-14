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

package org.isf.mortuary.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.DeathReasonManager;
import org.isf.mortuary.model.DeathReason;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.springframework.data.domain.Page;

public class DeathReasonBrowser extends ModalJFrame implements DeathReasonEdit.DeathReasonListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private final int PAGE_SIZE = 100;
	private int CURRENT_PAGE = 0;
	private int TOTAL_PAGES;
	private JPanel jContentPane;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JButton jDetailsButton;
	private JButton jEditButton;
	private JButton jNewButton;
	private JButton nextButton;
	private JButton prevButton;
	private JButton jSearchButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel totalDeathReasonLabel;
	private JLabel underLabel;
	private JScrollPane jScrollPane;
	private JTextField jSearchTextFiled;
	private JTable deathReasonsTable;
	private DefaultTableModel model;
	private List<DeathReason> deathReasonList;
	private final String[] columnNames = { MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.causeofdeath.code.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.causeofdeath.description").toUpperCase()
	};
	private final int[] columnWidths = {50, 50, 70};
	private final Class[] columnClasses = {int.class, String.class, String.class};
	private DeathReason deathReason;
	private int selectedRow;
	private long totalDeathReasons;

	private final DeathReasonManager deathReasonManager = Context.getApplicationContext().getBean(DeathReasonManager.class);

	public DeathReasonBrowser() {
		super();
		initialize();
		setVisible(true);
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.mortuary.causeofdeath.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int pfrmBase = 10;
		int pfrmWidth = 4;
		int pfrmBordX = (screenSize.width - (screenSize.width / pfrmBase * pfrmWidth)) / 2;
		int pfrmHeight = 5;
		int pfrmBordY = (screenSize.height - (screenSize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX, pfrmBordY,screenSize.width / pfrmBase * pfrmWidth,screenSize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(this);
	}

	private JPanel getJContentPane() {
		if (jContentPane != null) {
			return jContentPane;
		}
		jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getJSearchSubPanel(), BorderLayout.NORTH);
		jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		jContentPane.add(getJContentSubPanel(), BorderLayout.CENTER);
		return jContentPane;
	}

	private JPanel getJSearchSubPanel() {
		JPanel jSearchSubPanel = new JPanel(new BorderLayout());
		jSearchSubPanel.add(getJSearchPanel(), BorderLayout.EAST);
		return jSearchSubPanel;
	}

	private JPanel getJSearchPanel() {
		JPanel jSearchPanel = new JPanel();
		jSearchPanel.add(getJSearchTextField());
		jSearchPanel.add(getJSearchButton());
		return jSearchPanel;
	}

	private JTextField getJSearchTextField() {
		if (jSearchTextFiled != null) {
			return jSearchTextFiled;
		}
		jSearchTextFiled = new JTextField();
		jSearchTextFiled.setPreferredSize(new Dimension(270, 27));
		jSearchTextFiled.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					applyFilter(false);
				}
			}
		});
		return jSearchTextFiled;
	}

	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.addActionListener(actionEvent -> applyFilter(false));
		}
		return jSearchButton;
	}

	private JPanel getJButtonPanel() {
		JPanel jButtonPanel = new JPanel();
		jButtonPanel.add(getJNewButton());
		jButtonPanel.add(getJEditButton());
		jButtonPanel.add(getJDeleteButton());
		jButtonPanel.add(getDetailsButton());
		jButtonPanel.add(getJCloseButton());
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		if (jNewButton != null) {
			return jNewButton;
		}
		jNewButton = new JButton();
		jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		jNewButton.addActionListener(actionEvent -> {
			DeathReasonEdit newRecord = new DeathReasonEdit(this, null, true);
			newRecord.addDeathReasonListener(DeathReasonBrowser.this);
			newRecord.setVisible(true);
		});
		return jNewButton;
	}

	private JButton getJEditButton() {
		if (jEditButton != null) {
			return jEditButton;
		}
		jEditButton = new JButton();
		jEditButton.setText(MessageBundle.getMessage("angal.common.edit.btn"));
		jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		jEditButton.addActionListener(actionEvent -> {
			if (deathReasonsTable.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedRow = deathReasonsTable.getSelectedRow();
				deathReason = (DeathReason) model.getValueAt(deathReasonsTable.getSelectedRow(), -1);
				DeathReasonEdit editRecord = new DeathReasonEdit(this, deathReason, false);
				editRecord.addDeathReasonListener(DeathReasonBrowser.this);
				editRecord.setVisible(true);
			}
		});
		return jEditButton;
	}

	private JButton getJDeleteButton() {
		if (jDeleteButton != null) {
			return jDeleteButton;
		}
		jDeleteButton = new JButton();
		jDeleteButton.setText(MessageBundle.getMessage("angal.common.delete.btn"));
		jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		jDeleteButton.addActionListener(actionEvent -> {
			if (deathReasonsTable.getSelectedRow() < 0) {
				MessageDialog.info(this, "angal.common.pleaseselectarow.msg");
			} else {
				DeathReason deathReasons = (DeathReason) model.getValueAt(deathReasonsTable.getSelectedRow(), -1);
				int answer = MessageDialog.yesNo(
					this,
					"angal.mortuary.causeofdeath.deletecauseofdeath.fmt.msg",
					deathReasons.getTitle()
				);

				if (answer == JOptionPane.YES_OPTION) {
					try {
						boolean isDeleted = deathReasonManager.delete(deathReasons);
						if (isDeleted) {
							deathReasonList.remove(deathReasonsTable.getSelectedRow());
							model.fireTableDataChanged();
							deathReasonsTable.updateUI();
							applyFilter(false);
						} else {
							MessageDialog.info(this, "angal.common.suppressionfailed.msg");
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			}
		});
		return jDeleteButton;
	}

	private JButton getDetailsButton() {
		if (jDetailsButton != null) {
			return jDetailsButton;
		}
		jDetailsButton = new JButton(MessageBundle.getMessage("angal.mortuary.causeofdeath.details.btn"));
		jDetailsButton.setMnemonic(MessageBundle.getMnemonic("angal.mortuary.causeofdeath.details.btn.key"));
		jDetailsButton.addActionListener(actionEvent -> {
			if (deathReasonsTable.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
			} else {
				selectedRow = deathReasonsTable.getSelectedRow();
				deathReason = (DeathReason) model.getValueAt(deathReasonsTable.getSelectedRow(), -1);
				DeathReasonDetails detailsRecord = new DeathReasonDetails(null, deathReason);
				detailsRecord.setVisible(true);
			}
		});
		return jDetailsButton;
	}

	private JButton getJCloseButton() {
		if (jCloseButton != null) {
			return jCloseButton;
		}
		jCloseButton = new JButton();
		jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		jCloseButton.addActionListener(actionEvent -> dispose());
		return jCloseButton;
	}

	private JPanel getJContentSubPanel() {
		JPanel jContentSubPanel = new JPanel(new BorderLayout());
		jContentSubPanel.add(getJScrollPane(), BorderLayout.CENTER);
		jContentSubPanel.add(getPaginationPanel(), BorderLayout.SOUTH);
		return jContentSubPanel;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	private JTable getJTable() {
		if (deathReasonsTable != null) {
			return deathReasonsTable;
		}
		model = new DeathReasonTableModel("");
		deathReasonsTable = new JTable(model);
		deathReasonsTable.getColumnModel().getColumn(0).setMaxWidth(columnWidths[0]);
		deathReasonsTable.getColumnModel().getColumn(1).setPreferredWidth(columnWidths[1]);
		deathReasonsTable.getColumnModel().getColumn(2).setPreferredWidth(columnWidths[2]);
		deathReasonsTable.getColumnModel().getColumn(2).setCellRenderer(new TruncatedTextRenderer(200));
		return deathReasonsTable;
	}

	private JPanel getPaginationPanel() {
		JPanel jPaginationPanel = new JPanel();
		jPaginationPanel.add(getPrevButton());
		jPaginationPanel.add(getPagesCombo());
		jPaginationPanel.add(getUnderLabel());
		jPaginationPanel.add(getNextButton());
		jPaginationPanel.add(getTotalDeathReasonLabel());
		return jPaginationPanel;
	}

	private JButton getPrevButton() {
		if (prevButton != null) {
			return prevButton;
		}
		prevButton = new JButton("<");
		prevButton.setEnabled(CURRENT_PAGE > 0);
		prevButton.addActionListener(actionEvent -> {
			if (CURRENT_PAGE > 0) {
				CURRENT_PAGE--;
				pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
			}
		});
		return prevButton;
	}

	private JButton getNextButton() {
		if (nextButton != null) {
			return nextButton;
		}
		nextButton = new JButton(">");
		nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
		nextButton.addActionListener(actionEvent -> {
			if (CURRENT_PAGE < TOTAL_PAGES - 1) {
				CURRENT_PAGE++;
				pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
			}
		});
		return nextButton;
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo != null) {
			return pagesCombo;
		}
		pagesCombo = new JComboBox<>();
		pagesCombo.setPreferredSize(new Dimension(100, 25));
		for (int i = 0; i < TOTAL_PAGES; i++) {
			pagesCombo.addItem(i + 1);
		}
		pagesCombo.addActionListener(actionEvent -> {
			if (pagesCombo.getItemCount() != 0) {
				if (pagesCombo.getSelectedItem() != null) {
					CURRENT_PAGE = (Integer) pagesCombo.getSelectedItem() - 1;
					applyFilter(true);
				}
			}
		});
		return pagesCombo;
	}

	private void applyFilter(boolean isNextOrPrevButton) {

		model = new DeathReasonTableModel(jSearchTextFiled.getText().trim());

		if (!isNextOrPrevButton) {
			totalDeathReasonLabel.setText(MessageBundle.getMessage("angal.mortuary.totalcauseofdeath.txt") + ": " + totalDeathReasons);
			underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			CURRENT_PAGE = 0;

			pagesCombo.removeAllItems();
			for (int i = 0; i < TOTAL_PAGES; i++) {
				pagesCombo.addItem(i + 1);
			}

			pagesCombo.setSelectedItem(1);
		}

		if (deathReasonList != null) {
			model.fireTableDataChanged();
			deathReasonsTable.updateUI();
		}

		nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
		prevButton.setEnabled(CURRENT_PAGE > 0);
	}

	private JLabel getUnderLabel() {
		if (underLabel == null) {
			underLabel = new JLabel("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			underLabel.setPreferredSize(new Dimension(60, 30));
		}
		return underLabel;
	}

	private JLabel getTotalDeathReasonLabel() {
		if (totalDeathReasonLabel == null) {
			totalDeathReasonLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.totalcauseofdeath.txt") + ": " + totalDeathReasons);
		}
		return totalDeathReasonLabel;
	}

	class DeathReasonTableModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public DeathReasonTableModel(String key) {
			try {
				Page<DeathReason> deathReasonPage = deathReasonManager.getByTitleOrDescriptionPageable(key, CURRENT_PAGE, PAGE_SIZE);
				deathReasonList = new ArrayList<>(deathReasonPage.getContent());
				totalDeathReasons = deathReasonPage.getTotalElements();
				TOTAL_PAGES = deathReasonPage.getTotalPages();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (deathReasonList == null) {
				return 0;
			}
			return deathReasonList.size();
		}

		@Override
		public String getColumnName(int c) {
			return columnNames[c];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			DeathReason deathReason = deathReasonList.get(r);
			if (c == 0) {
				return deathReason.getId();
			} else if (c == -1) {
				return deathReason;
			} else if (c == 1) {
				return deathReason.getTitle();
			} else if (c == 2) {
				return deathReason.getDescription();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	static class TruncatedTextRenderer extends JLabel implements TableCellRenderer {
		private final int maxLength;

		public TruncatedTextRenderer(int maxLength) {
			this.maxLength = maxLength;
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			String text = (value != null) ? value.toString() : "";

			if (text.length() > maxLength) {
				text = text.substring(0, maxLength) + "...";
			}

			setText(text);
			setFont(table.getFont());

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}

			return this;
		}
	}

	@Override
	public void deathReasonUpdated(AWTEvent e) {
		deathReasonList.set(selectedRow, deathReason);
		((DeathReasonBrowser.DeathReasonTableModel) deathReasonsTable.getModel()).fireTableDataChanged();
		deathReasonsTable.updateUI();
		if (deathReasonsTable.getRowCount() > 0 && selectedRow > -1) {
			deathReasonsTable.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	@Override
	public void deathReasonInserted(AWTEvent e) {
		applyFilter(false);
	}
}