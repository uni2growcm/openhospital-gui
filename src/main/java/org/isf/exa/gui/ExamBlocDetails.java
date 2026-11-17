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
package org.isf.exa.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.exa.manager.BlockBrowsingManager;
import org.isf.exa.model.Block;
import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

public class ExamBlocDetails extends JDialog {

	private static final long serialVersionUID = 1L;

	private final Block block;
	private final BlockBrowsingManager blockBrowsingManager = Context.getApplicationContext().getBean(BlockBrowsingManager.class);

	public ExamBlocDetails(JFrame owner, Block block) {
		super(owner, true);
		this.block = block;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.exa.blocdetails.title"));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setContentPane(getJContentPane());
		setSize(new Dimension(650, 450));
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private JPanel getJContentPane() {
		JPanel content = new JPanel(new BorderLayout(10, 10));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.add(getInfoPanel(), BorderLayout.NORTH);
		content.add(new JScrollPane(getExamsTable()), BorderLayout.CENTER);
		content.add(getButtonPanel(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel getInfoPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ": "
				+ (block.getCode() != null ? block.getCode() : "")));
		panel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ": "
				+ (block.getDescription() != null ? block.getDescription() : "")));
		return panel;
	}

	private JTable getExamsTable() {
		String[] columns = {
				MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
				MessageBundle.getMessage("angal.common.description.txt").toUpperCase()
		};
		List<Exam> exams = new ArrayList<>();
		try {
			exams = blockBrowsingManager.getExamWithBlock(block.getCode());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		Object[][] data = new Object[exams.size()][2];
		for (int i = 0; i < exams.size(); i++) {
			data[i][0] = exams.get(i).getCode();
			data[i][1] = exams.get(i).getDescription();
		}
		DefaultTableModel model = new DefaultTableModel(data, columns) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable table = new JTable(model);
		table.setFillsViewportHeight(true);
		return table;
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.addActionListener(e -> dispose());
		panel.add(closeButton);
		return panel;
	}
}
