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
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.SpringLayout;

import org.isf.exa.manager.BlockBrowsingManager;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Block;
import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

/**
 * ExamBlocEdit - add/edit an exam block (code + description + associated exams).
 */
public class ExamBlocEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private VoLimitedTextField descriptionTextField;
	private VoLimitedTextField codeTextField;
	private Block block;
	private JList<Exam> jList;
	private boolean insert;

	private BlockBrowsingManager blockBrowsingManager = Context.getApplicationContext().getBean(BlockBrowsingManager.class);
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);

	public ExamBlocEdit(JFrame owner) {
		super(owner, true);
		initialize();
	}

	public ExamBlocEdit(JFrame owner, Block old, boolean inserting) {
		super(owner, true);
		insert = inserting;
		block = old;
		initialize();
	}

	private void initialize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		final int pfrmBase = 10;
		final int pfrmWidth = 5;
		final int pfrmHeight = 6;
		this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase) / 2,
				(screensize.height - screensize.height * pfrmHeight / pfrmBase) / 2,
				screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.exa.neweditresult"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.exa.editexam"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.add(getDataPanel(), BorderLayout.NORTH);
			jContentPane.add(getExamListPanel(), BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getDataPanel() {
		if (dataPanel == null) {
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':');
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':');
			dataPanel = new JPanel(new SpringLayout());
			dataPanel.add(codeLabel);
			dataPanel.add(getCodeTextField());
			dataPanel.add(descLabel);
			dataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(dataPanel, 2, 2, 5, 5, 5, 5);
		}
		return dataPanel;
	}

	private JPanel getExamListPanel() {
		JLabel selectionLabel = new JLabel(MessageBundle.getMessage("angal.exa.selectionexam"));
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(selectionLabel, BorderLayout.NORTH);
		panel.add(new JScrollPane(getExamList()), BorderLayout.CENTER);
		return panel;
	}

	private JList<Exam> getExamList() {
		if (jList == null) {
			List<Exam> listExam;
			try {
				listExam = examBrowsingManager.getExams();
			} catch (OHServiceException e) {
				listExam = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			jList = new JList<>(listExam.toArray(new Exam[0]));
			jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			jList.setVisibleRowCount(4);
			if (!insert) {
				List<Integer> selectedIndices = new ArrayList<>();
				try {
					List<Exam> blockExams = blockBrowsingManager.getExamWithBlock(block.getCode());
					for (Exam blockExam : blockExams) {
						for (int i = 0; i < listExam.size(); i++) {
							if (listExam.get(i).getCode().equals(blockExam.getCode())) {
								selectedIndices.add(i);
								break;
							}
						}
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				int[] indices = new int[selectedIndices.size()];
				for (int i = 0; i < selectedIndices.size(); i++) {
					indices[i] = selectedIndices.get(i);
				}
				jList.setSelectedIndices(indices);
			}
		}
		return jList;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton());
			buttonPanel.add(getCancelButton());
		}
		return buttonPanel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				if (codeTextField.getText().trim().isEmpty() || descriptionTextField.getText().trim().isEmpty()
						|| jList.getSelectedValuesList().isEmpty()) {
					MessageDialog.error(null, "angal.exa.pleaseinsertcodeoranddescription");
					return;
				}
				String code = codeTextField.getText().toUpperCase();
				String desc = descriptionTextField.getText();
				block.setCode(code);
				block.setDescription(desc);
				List<Exam> selectedExams = jList.getSelectedValuesList();

				boolean inError = false;
				if (insert) {
					try {
						if (blockBrowsingManager.isCodePresent(code)) {
							MessageDialog.error(this, "angal.exa.changethecodebecauseisalreadyinuse");
							return;
						}
						blockBrowsingManager.newBlock(block);
						blockBrowsingManager.saveExamBlocks(block, selectedExams);
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
						inError = true;
					}
				} else {
					try {
						blockBrowsingManager.updateBlock(block);
						blockBrowsingManager.saveExamBlocks(block, selectedExams);
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
						inError = true;
					}
				}
				if (inError) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				} else {
					dispose();
				}
			});
		}
		return okButton;
	}

	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			descriptionTextField = new VoLimitedTextField(255);
			if (!insert) {
				descriptionTextField.setText(block.getDescription());
			}
		}
		return descriptionTextField;
	}

	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new VoLimitedTextField(100);
			if (!insert) {
				codeTextField.setText(block.getCode());
				codeTextField.setEditable(false);
			}
		}
		return codeTextField;
	}
}
