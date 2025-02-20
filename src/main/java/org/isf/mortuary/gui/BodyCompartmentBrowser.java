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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.BodyCompartmentManager;
import org.isf.mortuary.model.BodyCompartment;
import org.isf.mortuary.gui.BodyCompartmentEdit.BodyCompartmentListener;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.springframework.data.domain.Page;

public class BodyCompartmentBrowser extends JDialog implements BodyCompartmentListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private final JDialog MY_JDIALOG;
	private final int PAGE_SIZE = 100;
	private int CURRENT_PAGE = 0;
	private int TOTAL_PAGES;
	private JPanel jContentPane;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JButton jEditButton;
	private JButton jNewButton;
	private JButton nextButton;
	private JButton prevButton;
	private JButton jSearchButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel totalBodyCompartmentLabel;
	private JLabel underLabel;
	private JScrollPane jScrollPane;
	private JTextField jSearchTextFiled;
	private JTable bodyCompartmentsTable;
	private DefaultTableModel model;
	private List<BodyCompartment> bodyCompartmentList;
	private final String[] columnNames = { MessageBundle.getMessage("angal.mortuary.bodycompartment.id.col"),
		MessageBundle.getMessage("angal.mortuary.bodycompartment.code.col"),
		MessageBundle.getMessage("angal.mortuary.bodycompartment.description.col")
	};
	private final int[] columnWidths = {50, 50, 70};
	private final Class[] columnClasses = {int.class, String.class, String.class};
	private BodyCompartment bodyCompartment;
	private int selectedRow;
	private long totalBodyCompartments;

	private final BodyCompartmentManager bodyCompartmentManager = Context.getApplicationContext().getBean(BodyCompartmentManager.class);

	public BodyCompartmentBrowser(JFrame parentFrame) {
		super(parentFrame, true);
		MY_JDIALOG = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.mortuary.bodycompartment.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int pfrmBase = 8;
		int pfrmWidth = 6;
		int pfrmBordX = (screenSize.width - (screenSize.width / pfrmBase * pfrmWidth)) / 2;
		int pfrmHeight = 4;
		int pfrmBordY = (screenSize.height - (screenSize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX, pfrmBordY,screenSize.width / pfrmBase * pfrmWidth,screenSize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(this);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJSearchSubPanel(), BorderLayout.NORTH);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJContentSubPanel(), BorderLayout.CENTER);
		}
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
		if (jSearchTextFiled == null) {
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
		}
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
		jButtonPanel.add(getJCloseButton());
		return jButtonPanel;
	}

	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton();
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(actionEvent -> {
				BodyCompartmentEdit newRecord = new BodyCompartmentEdit(MY_JDIALOG, null, true);
				newRecord.addBodyCompartmentListener(BodyCompartmentBrowser.this);
				newRecord.setVisible(true);
			});
		}
		return jNewButton;
	}

	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton();
			jEditButton.setText(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(actionEvent -> {
					if (bodyCompartmentsTable.getSelectedRow() < 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						selectedRow = bodyCompartmentsTable.getSelectedRow();
						bodyCompartment = (BodyCompartment) model.getValueAt(bodyCompartmentsTable.getSelectedRow(), -1);
						BodyCompartmentEdit editRecord = new BodyCompartmentEdit(MY_JDIALOG, bodyCompartment, false);
						editRecord.addBodyCompartmentListener(BodyCompartmentBrowser.this);
						editRecord.setVisible(true);
					}
				}
			);
		}
		return jEditButton;
	}

	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton();
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(actionEvent -> {
				if (bodyCompartmentsTable.getSelectedRow() < 0) {
					MessageDialog.info(this, "angal.common.pleaseselectarow.msg");
				} else {
					BodyCompartment bodyCompartments = (BodyCompartment) model.getValueAt(bodyCompartmentsTable.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(
						this,
						"angal.mortuary.bodycompartment.deletebodycompartment.fmt.msg",
						bodyCompartments.getLabel()
					);

					if (answer == JOptionPane.YES_OPTION) {
						try {
							boolean isDeleted = bodyCompartmentManager.delete(bodyCompartments);
							if (isDeleted) {
								bodyCompartmentList.remove(bodyCompartmentsTable.getSelectedRow());
								model.fireTableDataChanged();
								bodyCompartmentsTable.updateUI();
							} else {
								MessageDialog.info(this, "angal.common.suppressionfailed.msg");
							}
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
				}
			});
		}
		return jDeleteButton;
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton();
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
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
		if (bodyCompartmentsTable == null) {
			model = new BodyCompartmentTableModel("");
			bodyCompartmentsTable = new JTable(model);
			bodyCompartmentsTable.getColumnModel().getColumn(0).setMaxWidth(columnWidths[0]);
			bodyCompartmentsTable.getColumnModel().getColumn(1).setPreferredWidth(columnWidths[1]);
			bodyCompartmentsTable.getColumnModel().getColumn(2).setPreferredWidth(columnWidths[2]);
		}
		return bodyCompartmentsTable;
	}

	private JPanel getPaginationPanel() {
		JPanel jPaginationPanel = new JPanel();
		jPaginationPanel.add(getPrevButton());
		jPaginationPanel.add(getPagesCombo());
		jPaginationPanel.add(getUnderLabel());
		jPaginationPanel.add(getNextButton());
		jPaginationPanel.add(getTotalBodyCompartmentLabel());
		return jPaginationPanel;
	}

	private JButton getPrevButton() {
		if (prevButton == null) {
			prevButton = new JButton("<");
			prevButton.setEnabled(CURRENT_PAGE > 0);
			prevButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE > 0) {
					CURRENT_PAGE--;
					pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
				}
			});
		}
		return prevButton;
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton(">");
			nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
			nextButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE < TOTAL_PAGES - 1) {
					CURRENT_PAGE++;
					pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
				}
			});
		}
		return nextButton;
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo == null) {
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
		}
		return pagesCombo;
	}

	private void applyFilter(boolean isNextOrPrevButton) {

		model = new BodyCompartmentTableModel(jSearchTextFiled.getText().trim());

		if (!isNextOrPrevButton) {
			totalBodyCompartmentLabel.setText(MessageBundle.getMessage("angal.mortuary.totalmortuary.txt") + ": " + totalBodyCompartments);
			underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			CURRENT_PAGE = 0;

			pagesCombo.removeAllItems();
			for (int i = 0; i < TOTAL_PAGES; i++) {
				pagesCombo.addItem(i + 1);
			}

			pagesCombo.setSelectedItem(1);
		}

		if (bodyCompartmentList != null) {
			model.fireTableDataChanged();
			bodyCompartmentsTable.updateUI();
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

	private JLabel getTotalBodyCompartmentLabel() {
		if (totalBodyCompartmentLabel == null) {
			totalBodyCompartmentLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.totalmortuary.txt") + ": " + totalBodyCompartments);
		}
		return totalBodyCompartmentLabel;
	}

	class BodyCompartmentTableModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public BodyCompartmentTableModel(String key) {
			try {
				Page<BodyCompartment> bodyCompartmentPage = bodyCompartmentManager.getByLabelOrDescriptionPageable(key, key, CURRENT_PAGE, PAGE_SIZE);
				bodyCompartmentList = new ArrayList<>(bodyCompartmentPage.getContent());
				totalBodyCompartments = bodyCompartmentPage.getTotalElements();
				TOTAL_PAGES = bodyCompartmentPage.getTotalPages();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (bodyCompartmentList == null) {
				return 0;
			}
			return bodyCompartmentList.size();
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
			BodyCompartment bodyCompartment = bodyCompartmentList.get(r);
			if (c == 0) {
				return bodyCompartment.getId();
			} else if (c == -1) {
				return bodyCompartment;
			} else if (c == 1) {
				return bodyCompartment.getLabel();
			} else if (c == 2) {
				return bodyCompartment.getDescription();
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

	@Override
	public void bodyCompartmentUpdated(AWTEvent e) {
		bodyCompartmentList.set(selectedRow, bodyCompartment);
		((BodyCompartmentTableModel) bodyCompartmentsTable.getModel()).fireTableDataChanged();
		bodyCompartmentsTable.updateUI();
		if (bodyCompartmentsTable.getRowCount() > 0 && selectedRow > -1) {
			bodyCompartmentsTable.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	@Override
	public void bodyCompartmentInserted(AWTEvent e) {
		applyFilter(false);
		if (bodyCompartmentsTable.getRowCount() > 0) {
			bodyCompartmentsTable.setRowSelectionInterval(0, 0);
		}
	}

}