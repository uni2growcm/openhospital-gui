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
import java.io.Serial;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import org.isf.mortuarystays.gui.MortuaryStaysBrowser;
import org.isf.mortuary.gui.MortuaryBodyCompartmentEdit.BodyCompartmentListener;
import org.isf.mortuarystays.gui.MortuaryStaysEdit;
import org.isf.mortuarystays.model.MortuaryStay;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class MortuaryBodyCompartment extends ModalJFrame implements BodyCompartmentListener {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public void bodyCompartmentUpdated(AWTEvent e) {
		bodyCompartmentList.set(selectedrow, bodyCompartment);
		((MortuaryBodyCompartment.MortuaryBodyCompartmentModel) bcTable.getModel()).fireTableDataChanged();
		bcTable.updateUI();
		if (bcTable.getRowCount() > 0 && selectedrow > -1) {
			bcTable.setRowSelectionInterval(selectedrow, selectedrow);
		}
	}
	@Override
	public void bodyCompartmentInserted(AWTEvent e) {
		bodyCompartmentList.add(0, bodyCompartment);
		((MortuaryBodyCompartment.MortuaryBodyCompartmentModel) bcTable.getModel()).fireTableDataChanged();
		if (bcTable.getRowCount() > 0) {
			bcTable.setRowSelectionInterval(0, 0);
		}
	}

	private final JFrame myFrame;
	private JPanel jContentPane;
	private JButton jCloseButton;
	private JButton jDeleteButton;
	private JButton jEditButton;
	private JButton jNewButton;
	private JButton jSearchButton;
	private JScrollPane jScrollPane;
	private JTextField jSearchTextFiled;
	private JTable bcTable;
	private DefaultTableModel model;
	private final int pfrmBase = 8;
	private final int pfrmWidth = 6;
	private final int pfrmHeight = 4;
	private int pfrmBordX;
	private int pfrmBordY;
	private List<BodyCompartment> bodyCompartmentList;
	private String[] pColums = { MessageBundle.getMessage("angal.mortuary.bodycompartment.id.col"),
		MessageBundle.getMessage("angal.mortuary.bodycompartment.code.col"),
		MessageBundle.getMessage("angal.mortuary.bodycompartment.description.col")
	};
	private int[] pColumwidth = {50, 50, 70};
	private Class[] pColumnClass = {int.class, String.class, String.class};
	private BodyCompartment bodyCompartment;
	private int selectedrow;

	private BodyCompartmentManager bodyCompartmentManager = Context.getApplicationContext().getBean(BodyCompartmentManager.class);

	public MortuaryBodyCompartment() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.mortuary.bodycompartment.title"));
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) / 2;
		pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX,pfrmBordY,screensize.width / pfrmBase * pfrmWidth,screensize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(this);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJSearchPanel(), BorderLayout.NORTH);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
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
		}
		return jSearchTextFiled;
	}

	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
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
				bodyCompartment = new BodyCompartment(0,"", "", false);
				MortuaryBodyCompartmentEdit newrecord = new MortuaryBodyCompartmentEdit(myFrame, bodyCompartment, true);
				newrecord.addBodyCompartmentListener(MortuaryBodyCompartment.this);
				newrecord.setVisible(true);
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
					if (bcTable.getSelectedRow() < 0) {
						MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					} else {
						selectedrow = bcTable.getSelectedRow();
						bodyCompartment = (BodyCompartment) model.getValueAt(bcTable.getSelectedRow(), -1);
						MortuaryBodyCompartmentEdit editrecord = new MortuaryBodyCompartmentEdit(myFrame, bodyCompartment, false);
						editrecord.addBodyCompartmentListener(MortuaryBodyCompartment.this);
						editrecord.setVisible(true);
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
				if (bcTable.getSelectedRow() < 0) {
					MessageDialog.info(this, "angal.common.pleaseselectarow.msg");
				} else {
					BodyCompartment bodyCompartments = (BodyCompartment) model.getValueAt(bcTable.getSelectedRow(), -1);
					int answer = MessageDialog.yesNo(this, "angal.mortuary.bodycompartment.deletemortuarystays.fmt.msg", bodyCompartments.getCode());
					try {
						if (answer == JOptionPane.YES_OPTION) {
							BodyCompartment bodyCompartmentDeleted = bodyCompartmentManager.delete(bodyCompartments);
							if (bodyCompartmentDeleted != null) {
								bodyCompartmentList.remove(bcTable.getSelectedRow());
								model.fireTableDataChanged();
								bcTable.updateUI();
							} else {
								MessageDialog.info(this, "angal.common.suppressionfailed.msg");
							}
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
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

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	private JTable getJTable() {
		if (bcTable == null) {
			model = new MortuaryBodyCompartmentModel();
			bcTable = new JTable(model);
			bcTable.getColumnModel().getColumn(0).setMaxWidth(pColumwidth[0]);
			bcTable.getColumnModel().getColumn(1).setPreferredWidth(pColumwidth[1]);
			bcTable.getColumnModel().getColumn(2).setPreferredWidth(pColumwidth[2]);
		}
		return bcTable;
	}

	class MortuaryBodyCompartmentModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MortuaryBodyCompartmentModel() {
			try {
				bodyCompartmentList = bodyCompartmentManager.getAll();
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
			return pColums[c];
		}

		@Override
		public int getColumnCount() {
			return pColums.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			BodyCompartment bodyCompartment = bodyCompartmentList.get(r);
			if (c == 0) {
				return bodyCompartment.getId();
			} else if (c == -1) {
				return bodyCompartment;
			} else if (c == 1) {
				return bodyCompartment.getCode();
			} else if (c == 2) {
				return bodyCompartment.getDescription();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return pColumnClass[columnIndex];
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
