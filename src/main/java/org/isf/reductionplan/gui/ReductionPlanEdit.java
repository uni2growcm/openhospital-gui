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
package org.isf.reductionplan.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;

import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.reductionplan.model.ExamReduction;
import org.isf.reductionplan.model.MedicalReduction;
import org.isf.reductionplan.model.OperationReduction;
import org.isf.reductionplan.model.PriceOtherReduction;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.utils.exception.OHDataValidationException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class ReductionPlanEdit extends ModalJFrame {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String[] medicalReductionColumn = new String[] {
		MessageBundle.getMessage("angal.reductionplan.medical.col").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.reductionrate.col").toUpperCase() };

	private final String[] examReductionColumn = new String[] {
		MessageBundle.getMessage("angal.reductionplan.exam.col").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.reductionrate.col").toUpperCase() };

	private final String[] operationReductionColumn = new String[] {
		MessageBundle.getMessage("angal.reductionplan.operation.col").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.reductionrate.col").toUpperCase() };
	private final String[] priceOtherReductionColumn = new String[] {
		MessageBundle.getMessage("angal.reductionplan.other.col").toUpperCase(),
		MessageBundle.getMessage("angal.reductionplan.reductionrate.col").toUpperCase() };

	private final ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private final OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private final PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
	private final EventListenerList reductionPlanListener = new EventListenerList();
	private ReductionPlan reductionPlan;
	private boolean isInsert = false;
	private JPanel contentPane;
	private JButton jCloseButton;
	private JPanel buttonPanel;
	private JLabel labelDescription;
	private JTextField textDescription;
	private JLabel labelMedicalRate;
	private JTextField textMedicalRate;
	private JLabel labelOperationRate;
	private JTextField textOperationRate;
	private JLabel labelExamRate;
	private JTextField textExamRate;
	private JLabel labelPriceOtherRate;
	private JTextField textPriceOtherRate;
	private List<MedicalReduction> medicalReductionList = new ArrayList<>();
	private JTable medicalReductionTable;
	private List<ExamReduction> examReductionList = new ArrayList<>();
	private JTable examReductionTable;
	private List<OperationReduction> operationReductionList = new ArrayList<>();
	private JTable operationReductionTable;
	private List<PriceOtherReduction> priceOtherReductionList = new ArrayList<>();
	private JTable priceOtherReductionTable;
	private JButton jAddButton;
	private JTabbedPane tabbedPane;
	private JButton jRemoveButton;
	private JButton jRemoveAllButton;
	private JButton jSaveButton;

	public ReductionPlanEdit(ReductionPlan reductionPlan, boolean isInsert) {
		this.isInsert = isInsert;
		if (this.isInsert) {
			this.reductionPlan = new ReductionPlan();
			initialize();
		} else {
			this.reductionPlan = reductionPlan;
			initialize();
			loadDataFromObject();
		}
	}

	public void addReductionPlanListener(ReductionPlanListener l) {
		reductionPlanListener.add(ReductionPlanListener.class, l);
	}

	private void fireReductionPlanInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = reductionPlanListener.getListeners(ReductionPlanListener.class);
		for (EventListener listener : listeners) {
			((ReductionPlanListener) listener).ReductionPlanInserted(event);
		}
	}

	private void initialize() {

		setTitle(this.isInsert ? MessageBundle.getMessage("angal.reductionplan.createreductionplan.title")
			: MessageBundle.getMessage("angal.reductionplan.editreductionplan.title"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 350);
		setContentPane(getMainContentPane());
		setLocationRelativeTo(null);
	}

	private JPanel getMainContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
		}
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100 };
		gridBagLayout.rowHeights = new int[] { 20, 20, 20 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0 };
		contentPane.setLayout(gridBagLayout);

		JPanel detailsPane = getDetailPane();
		GridBagConstraints gbcDetail = new GridBagConstraints();
		gbcDetail.anchor = GridBagConstraints.WEST;
		gbcDetail.fill = GridBagConstraints.HORIZONTAL;
		gbcDetail.insets = new Insets(5, 5, 5, 5);
		gbcDetail.gridx = 0;
		gbcDetail.gridy = 0;
		contentPane.add(detailsPane, gbcDetail);

		JPanel tabPane = getJTabbedPane();

		GridBagConstraints gbcTabbedPane = new GridBagConstraints();
		gbcTabbedPane.anchor = GridBagConstraints.WEST;
		gbcTabbedPane.fill = GridBagConstraints.BOTH;
		gbcTabbedPane.insets = new Insets(5, 5, 5, 5);
		gbcTabbedPane.gridx = 0;
		gbcTabbedPane.gridy = 1;
		contentPane.add(tabPane, gbcTabbedPane);

		GridBagConstraints gbcButtonPane = new GridBagConstraints();
		gbcButtonPane.anchor = GridBagConstraints.CENTER;
		gbcButtonPane.gridx = 0;
		gbcButtonPane.gridy = 2;
		contentPane.add(getButtonPanel(), gbcButtonPane);

		return contentPane;
	}

	private JPanel getDetailPane() {
		JPanel detailPane = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 100, 100, 100 };
		gridBagLayout.rowHeights = new int[] { 20, 20, 20 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		detailPane.setLayout(gridBagLayout);

		labelDescription = getJLabelDescription();
		GridBagConstraints gbc_labelDescription = new GridBagConstraints();
		gbc_labelDescription.anchor = GridBagConstraints.WEST;
		gbc_labelDescription.insets = new Insets(5, 5, 5, 5);
		gbc_labelDescription.gridx = 0;
		gbc_labelDescription.gridy = 0;
		detailPane.add(labelDescription, gbc_labelDescription);

		textDescription = getJTextFieldDescription();
		GridBagConstraints gbc_textDescription = new GridBagConstraints();
		gbc_textDescription.anchor = GridBagConstraints.NORTH;
		gbc_textDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_textDescription.insets = new Insets(0, 0, 5, 0);
		gbc_textDescription.gridwidth = 3;
		gbc_textDescription.gridx = 1;
		gbc_textDescription.gridy = 0;
		detailPane.add(textDescription, gbc_textDescription);
		textDescription.setColumns(10);

		labelMedicalRate = getJLabelMedicalRate();
		GridBagConstraints gbc_labelMedicalRate = new GridBagConstraints();
		gbc_labelMedicalRate.anchor = GridBagConstraints.SOUTH;
		gbc_labelMedicalRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelMedicalRate.insets = new Insets(5, 5, 5, 5);
		gbc_labelMedicalRate.gridx = 0;
		gbc_labelMedicalRate.gridy = 1;
		detailPane.add(labelMedicalRate, gbc_labelMedicalRate);

		textMedicalRate = getJTextFieldMedicalRate();
		GridBagConstraints gbc_textMedicalRate = new GridBagConstraints();
		gbc_textMedicalRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textMedicalRate.anchor = GridBagConstraints.NORTH;
		gbc_textMedicalRate.insets = new Insets(0, 0, 5, 5);
		gbc_textMedicalRate.gridx = 1;
		gbc_textMedicalRate.gridy = 1;
		detailPane.add(textMedicalRate, gbc_textMedicalRate);
		textMedicalRate.setColumns(10);

		labelOperationRate = getJLabelOperationRate();
		GridBagConstraints gbc_labelOperationRate = new GridBagConstraints();
		gbc_labelOperationRate.anchor = GridBagConstraints.NORTH;
		gbc_labelOperationRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelOperationRate.insets = new Insets(5, 5, 5, 5);
		gbc_labelOperationRate.gridx = 2;
		gbc_labelOperationRate.gridy = 1;
		detailPane.add(labelOperationRate, gbc_labelOperationRate);

		textOperationRate = getJTextFieldOperationRate();
		GridBagConstraints gbc_textOperationRate = new GridBagConstraints();
		gbc_textOperationRate.anchor = GridBagConstraints.NORTH;
		gbc_textOperationRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textOperationRate.insets = new Insets(0, 0, 5, 0);
		gbc_textOperationRate.gridx = 3;
		gbc_textOperationRate.gridy = 1;
		detailPane.add(textOperationRate, gbc_textOperationRate);
		textOperationRate.setColumns(10);

		labelExamRate = getJLabelExamRate();
		GridBagConstraints gbc_labelExamRate = new GridBagConstraints();
		gbc_labelExamRate.anchor = GridBagConstraints.SOUTH;
		gbc_labelExamRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelExamRate.insets = new Insets(5, 5, 5, 5);
		gbc_labelExamRate.gridx = 0;
		gbc_labelExamRate.gridy = 2;
		detailPane.add(labelExamRate, gbc_labelExamRate);

		textExamRate = getTextFieldExamRate();
		GridBagConstraints gbc_textExamRate = new GridBagConstraints();
		gbc_textExamRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textExamRate.anchor = GridBagConstraints.SOUTH;
		gbc_textExamRate.insets = new Insets(0, 0, 0, 5);
		gbc_textExamRate.gridx = 1;
		gbc_textExamRate.gridy = 2;
		detailPane.add(textExamRate, gbc_textExamRate);
		textExamRate.setColumns(10);

		labelPriceOtherRate = getJLabelPriceOtherRate();
		GridBagConstraints gbc_labelPriceOtherRate = new GridBagConstraints();
		gbc_labelPriceOtherRate.anchor = GridBagConstraints.SOUTHWEST;
		gbc_labelPriceOtherRate.insets = new Insets(5, 5, 5, 5);
		gbc_labelPriceOtherRate.gridx = 2;
		gbc_labelPriceOtherRate.gridy = 2;
		detailPane.add(labelPriceOtherRate, gbc_labelPriceOtherRate);

		textPriceOtherRate = getTextFieldPriceOtherRate();
		GridBagConstraints gbc_textPriceOtherRate = new GridBagConstraints();
		gbc_textPriceOtherRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textPriceOtherRate.anchor = GridBagConstraints.NORTH;
		gbc_textPriceOtherRate.gridx = 3;
		gbc_textPriceOtherRate.gridy = 2;
		detailPane.add(textPriceOtherRate, gbc_textPriceOtherRate);
		textPriceOtherRate.setColumns(10);

		return detailPane;
	}

	private JPanel getMedicalReductionPanel() {
		JPanel medicalReductionPanel = new JPanel();
		medicalReductionPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		medicalReductionPanel.add(scrollPane, BorderLayout.CENTER);

		medicalReductionTable = new JTable();
		medicalReductionTable.setModel(new MedicalReductionModel());

		scrollPane.setViewportView(medicalReductionTable);
		return medicalReductionPanel;
	}

	private JPanel getExamReductionPanel() {
		JPanel examReductionPanel = new JPanel();
		examReductionPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		examReductionPanel.add(scrollPane, BorderLayout.CENTER);

		examReductionTable = new JTable();
		examReductionTable.setModel(new ExamReductionModel(this.isInsert));

		scrollPane.setViewportView(examReductionTable);
		return examReductionPanel;
	}

	private JPanel getOperationReductionPanel() {
		JPanel operationReductionPanel = new JPanel();
		operationReductionPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		operationReductionPanel.add(scrollPane, BorderLayout.CENTER);

		operationReductionTable = new JTable();
		operationReductionTable.setModel(new OperationReductionModel());

		scrollPane.setViewportView(operationReductionTable);
		return operationReductionPanel;
	}

	private JPanel getPriceOtherReductionPanel() {
		JPanel priceOtherReductionPanel = new JPanel();
		priceOtherReductionPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		priceOtherReductionPanel.add(scrollPane, BorderLayout.CENTER);

		priceOtherReductionTable = new JTable();
		priceOtherReductionTable.setModel(new PriceOtherReductionModel());

		scrollPane.setViewportView(priceOtherReductionTable);
		return priceOtherReductionPanel;
	}

	private JLabel getJLabelPriceOtherRate() {
		if (labelPriceOtherRate == null) {
			labelPriceOtherRate = new JLabel(MessageBundle.getMessage("angal.reductionplan.otherrate.label"));
		}

		return labelPriceOtherRate;
	}

	private JTextField getTextFieldPriceOtherRate() {
		if (textPriceOtherRate == null) {
			textPriceOtherRate = new JTextField("0.00");
		}
		return textPriceOtherRate;
	}

	private JTextField getTextFieldExamRate() {
		if (textExamRate == null) {
			textExamRate = new JTextField("0.00");
		}

		return textExamRate;
	}

	private JLabel getJLabelExamRate() {
		if (labelExamRate == null) {
			labelExamRate = new JLabel(MessageBundle.getMessage("angal.reductionplan.examrate.label"));
		}

		return labelExamRate;
	}

	private JLabel getJLabelOperationRate() {
		if (labelOperationRate == null) {
			labelOperationRate = new JLabel(MessageBundle.getMessage("angal.reductionplan.operationrate.label"));
		}

		return labelOperationRate;
	}

	private JLabel getJLabelMedicalRate() {
		if (labelMedicalRate == null) {
			labelMedicalRate = new JLabel(MessageBundle.getMessage("angal.reductionplan.medicalrate.label"));
		}

		return labelMedicalRate;
	}

	private JTextField getJTextFieldDescription() {
		if (textDescription == null) {
			textDescription = new JTextField();
		}

		return textDescription;
	}

	private JTextField getJTextFieldMedicalRate() {
		if (textMedicalRate == null) {
			textMedicalRate = new JTextField("0.00");
		}

		return textMedicalRate;
	}

	private JTextField getJTextFieldOperationRate() {
		if (textOperationRate == null) {
			textOperationRate = new JTextField("0.00");
		}

		return textOperationRate;
	}

	private JLabel getJLabelDescription() {
		if (labelDescription == null) {
			labelDescription = new JLabel(MessageBundle.getMessage("angal.reductionplan.description.label"));
		}

		return labelDescription;
	}

	private JPanel getJTabbedPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(MessageBundle.getMessage("angal.reductionplan.medical.title"), getMedicalReductionPanel());
		tabbedPane.addTab(MessageBundle.getMessage("angal.reductionplan.exam.title"), getExamReductionPanel());
		tabbedPane.addTab(MessageBundle.getMessage("angal.reductionplan.operation.title"), getOperationReductionPanel());
		tabbedPane.addTab(MessageBundle.getMessage("angal.reductionplan.other.title"), getPriceOtherReductionPanel());
		panel.add(tabbedPane, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(new EmptyBorder(22, 3, 0, 0));

		JButton addButton = getJAddButton();
		JButton removeButton = getJRemoveButton();
		JButton removeAllButton = getJRemoveAllButton();
		JButton[] buttons = { addButton, removeButton, removeAllButton};

		Dimension maxSize = new Dimension(130, 35);

		for (JButton b : buttons) {
			b.setPreferredSize(maxSize);
			b.setMinimumSize(maxSize);
			b.setMaximumSize(maxSize);
			b.setAlignmentX(Component.CENTER_ALIGNMENT);
			buttonPanel.add(b);
			buttonPanel.add(Box.createVerticalStrut(5));
		}

		panel.add(buttonPanel, BorderLayout.EAST);
		return panel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getJSaveButton());
			buttonPanel.add(getJCloseButton());
		}

		return buttonPanel;
	}

	private JButton getJAddButton() {
		if (jAddButton == null) {
			jAddButton = new JButton();
			Dimension uniformSize = new Dimension(70, jAddButton.getPreferredSize().height);
			jAddButton.setPreferredSize(uniformSize);
			jAddButton.setText(MessageBundle.getMessage("angal.reductionplan.add.btn"));
			jAddButton.setMnemonic(MessageBundle.getMnemonic("angal.reductionplan.add.btn.key"));
			jAddButton.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jAddButton.addActionListener(actionEvent -> {
				int tabSelected = tabbedPane.getSelectedIndex();
				try {
					switch (tabSelected) {
						case 0 -> addMedicalReduction();
						case 1 -> addExamReduction();
						case 2 -> addOperationReduction();
						case 3 -> addPriceOtherReduction();
						default -> {}
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}

		return jAddButton;
	}

	private JButton getJRemoveButton() {
		if (jRemoveButton == null) {
			jRemoveButton = new JButton();
			Dimension uniformSize = new Dimension(70, jRemoveButton.getPreferredSize().height);
			jRemoveButton.setPreferredSize(uniformSize);
			jRemoveButton.setText(MessageBundle.getMessage("angal.common.remove.btn"));
			jRemoveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.remove.btn.key"));
			jRemoveButton.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jRemoveButton.addActionListener(actionEvent -> {
				int index;
				int tabSelected = tabbedPane.getSelectedIndex();
				switch (tabSelected) {
					case 0 -> {
						index = medicalReductionTable.getSelectedRow();
						if (index < 0) {
							MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
						} else {
							medicalReductionList.remove(index);
							MedicalReductionModel medicalReductionModel = (MedicalReductionModel) medicalReductionTable.getModel();
							medicalReductionModel.fireTableDataChanged();
						}
					}

					case 1 -> {
						index = examReductionTable.getSelectedRow();
						if (index < 0) {
							MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
						} else {
							examReductionList.remove(index);
							ExamReductionModel examReductionModel = (ExamReductionModel) examReductionTable.getModel();
							examReductionModel.fireTableDataChanged();
						}
					}

					case 2 -> {
						index = operationReductionTable.getSelectedRow();
						if (index < 0) {
							MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
						} else {
							operationReductionList.remove(index);
							OperationReductionModel operationReductionModel = (OperationReductionModel) operationReductionTable.getModel();
							operationReductionModel.fireTableDataChanged();
						}
					}

					case 3 -> {
						index = priceOtherReductionTable.getSelectedRow();
						if (index < 0) {
							MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
						} else {
							priceOtherReductionList.remove(index);
							PriceOtherReductionModel priceOtherReductionModel = (PriceOtherReductionModel) priceOtherReductionTable.getModel();
							priceOtherReductionModel.fireTableDataChanged();
						}
					}

					default -> {
					}
				}
			});
		}

		return jRemoveButton;
	}

	private JButton getJRemoveAllButton() {
		if (jRemoveAllButton == null) {
			jRemoveAllButton = new JButton();
			Dimension uniformSize = new Dimension(70, jRemoveAllButton.getPreferredSize().height);
			jRemoveButton.setPreferredSize(uniformSize);
			jRemoveAllButton.setText(MessageBundle.getMessage("angal.common.removeall.btn"));
			jRemoveAllButton.setMnemonic(MessageBundle.getMnemonic("angal.common.removeall.btn.key"));
			jRemoveAllButton.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jRemoveAllButton.addActionListener(actionEvent -> {
				int tabSelected = tabbedPane.getSelectedIndex();
				switch (tabSelected) {
				case 0 -> {
					medicalReductionList.clear();
					MedicalReductionModel medicalReductionModel = (MedicalReductionModel) medicalReductionTable.getModel();
					medicalReductionModel.fireTableDataChanged();
				}

				case 1 -> {
					examReductionList.clear();
					ExamReductionModel examReductionModel = (ExamReductionModel) examReductionTable.getModel();
					examReductionModel.fireTableDataChanged();
				}

				case 2 -> {
					operationReductionList.clear();
					OperationReductionModel operationReductionModel = (OperationReductionModel) operationReductionTable.getModel();
					operationReductionModel.fireTableDataChanged();
				}

				case 3 -> {
					priceOtherReductionList.clear();
					PriceOtherReductionModel priceOtherReductionModel = (PriceOtherReductionModel) priceOtherReductionTable.getModel();
					priceOtherReductionModel.fireTableDataChanged();
				}

				default -> {
				}
				}
			});
		}

		return jRemoveAllButton;
	}

	private JButton getJSaveButton() {
		if (jSaveButton == null) {
			jSaveButton = new JButton();
			jSaveButton.setText(MessageBundle.getMessage("angal.common.save.btn"));
			jSaveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			jSaveButton.addActionListener(actionEvent -> {
				try {
					if (!examReductionList.isEmpty()) {
						reductionPlan.setExamReductions(examReductionList);
					}

					if (!medicalReductionList.isEmpty()) {
						reductionPlan.setMedicalReductions(medicalReductionList);
					}

					if(!operationReductionList.isEmpty()) {
						reductionPlan.setOperationReductions(operationReductionList);
					}

					if (!priceOtherReductionList.isEmpty()) {
						reductionPlan.setPriceOtherReductions(priceOtherReductionList);
					}


					if (loadDataInObject()) {
						List<OHExceptionMessage> errors = validateReductionPlan(reductionPlan);
						if (errors.isEmpty()) {
							if (this.isInsert) {
								reductionPlanManager.add(reductionPlan);
							} else {
								reductionPlanManager.update(reductionPlan);
							}
						} else {
							throw new OHDataValidationException(errors);
						}

						fireReductionPlanInserted();
						jCloseButton.doClick();
					}

				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}

		return jSaveButton;
	}

	private void addMedicalReduction() throws OHServiceException {
		List<Medical> medicalList = medicalBrowsingManager.getMedicals();

		Medical medical = (Medical) MessageDialog.inputDialog(
			ReductionPlanEdit.this,
			new ImageIcon(""),
			medicalList.toArray(),
			"",
			"angal.newbill.selectamedical.txt",
			"angal.newbill.medical.title"
		);

		if (medical == null) {
			return;
		}

		String stringRate = (String) MessageDialog.inputDialog(
			ReductionPlanEdit.this,
			new ImageIcon(""),
			null,
			null,
			"angal.reductionplan.reductionrate.txt",
			"angal.reductionplan.reductionrate.title"
		);

		if (stringRate == null || stringRate.isEmpty()) {
			return;
		}

		try {
			double rate;
			rate = Double.parseDouble(stringRate);

			if (!isValidRate(BigDecimal.valueOf(rate))) {
				throw new OHDataValidationException(new OHExceptionMessage(
					MessageBundle.getMessage("angal.reductionplan.oneinvalidmedicalreductionrate.msg")
				));
			}

			MedicalReduction medicalReduction = new MedicalReduction(reductionPlan, medical, BigDecimal.valueOf(rate));

			if (medicalReductionList != null && !medicalReductionList.isEmpty() &&
				medicalReductionList.stream().anyMatch(existing ->
					existing.getMedical().equals(medicalReduction.getMedical()))) {
				throw new OHDataValidationException(new OHExceptionMessage(
					MessageBundle.getMessage("angal.reductionplan.duplicatemedicalfound.msg")
				));
			}

			medicalReductionList.add(medicalReduction);
			((MedicalReductionModel) medicalReductionTable.getModel()).fireTableDataChanged();

		} catch (OHDataValidationException validationEx) {
			throw validationEx;
		}  catch (Exception ex) {
			MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
		}
	}

	private void addExamReduction() throws OHServiceException {
		List<Exam> examList = examBrowsingManager.getExams();

		Exam exam = (Exam) MessageDialog.inputDialog(ReductionPlanEdit.this, new ImageIcon(""), examList.toArray(), "",
			"angal.newbill.selectanexam.txt", "angal.newbill.exam.title");

		if (exam != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, new ImageIcon(""), null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			try {
				if (stringRate == null || stringRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(stringRate);
				ExamReduction exaReduction = new ExamReduction(reductionPlan, exam, BigDecimal.valueOf(rate));
				if (!isValidRate(exaReduction.getReductionRate())) {
					throw new OHDataValidationException(new OHExceptionMessage(
						MessageBundle.getMessage("angal.reductionplan.oneinvalidexamreductionrate.msg")
					));
				} else {
					if (examReductionList != null && !examReductionList.isEmpty()) {
						boolean duplicateFound = examReductionList.stream()
							.anyMatch(existing -> existing.getExam().equals(exaReduction.getExam()));

						if (duplicateFound) {
							throw new OHDataValidationException(new OHExceptionMessage(
								MessageBundle.getMessage("angal.reductionplan.duplicateexamfound.msg")
							));
						} else {
							examReductionList.add(exaReduction);
						}
					} else {
						examReductionList.add(exaReduction);
					}
				}

				ExamReductionModel examModel = (ExamReductionModel) examReductionTable.getModel();
				examModel.fireTableDataChanged();
			} catch (OHDataValidationException validationEx) {
				throw validationEx;
			}  catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}
	private void addOperationReduction() throws OHServiceException {
		List<Operation> operationList = operationBrowserManager.getOperation();

		Operation operation = (Operation) MessageDialog.inputDialog(ReductionPlanEdit.this, new ImageIcon(""), operationList.toArray(), "",
			"angal.newbill.selectanoperation.txt", "angal.newbill.operation.title");

		if (operation != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, new ImageIcon(""), null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			try {
				if (stringRate == null || stringRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(stringRate);
				OperationReduction opeReduction = new OperationReduction(reductionPlan, operation, BigDecimal.valueOf(rate));
				if (!isValidRate(opeReduction.getReductionRate())) {
					throw new OHDataValidationException(new OHExceptionMessage(
						MessageBundle.getMessage("angal.reductionplan.oneinvalidoperationreductionrate.msg")
					));
				} else {
					if (operationReductionList != null && !operationReductionList.isEmpty()) {
						boolean duplicateFound = operationReductionList.stream()
							.anyMatch(existing -> existing.getOperation().equals(opeReduction.getOperation()));

						if (duplicateFound) {
							throw new OHDataValidationException(new OHExceptionMessage(
								MessageBundle.getMessage("angal.reductionplan.duplicateoperationfound.msg")
							));
						} else {
							operationReductionList.add(opeReduction);
						}
					} else {
						operationReductionList.add(opeReduction);
					}
				}

				OperationReductionModel opeModel = (OperationReductionModel) operationReductionTable.getModel();
				opeModel.fireTableDataChanged();
			} catch (OHDataValidationException validationEx) {
				throw validationEx;
			}  catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}
	private void addPriceOtherReduction() throws OHServiceException {
		List<PricesOthers> pricesOthersList = pricesOthersManager.getOthers();

		PricesOthers pricesOthers = (PricesOthers) MessageDialog.inputDialog(ReductionPlanEdit.this, new ImageIcon(""), pricesOthersList.toArray(),
			"", "angal.newbill.pleaseselectanitem.txt", "angal.newbill.item.title");

		if (pricesOthers != null) {
			double rate = 0;
			String strRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, new ImageIcon(""), null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			try {
				if (strRate == null || strRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(strRate);
				PriceOtherReduction priceOtherReduction = new PriceOtherReduction(reductionPlan, pricesOthers, BigDecimal.valueOf(rate));
				if (!isValidRate(priceOtherReduction.getReductionRate())) {
					throw new OHDataValidationException(new OHExceptionMessage(
						MessageBundle.getMessage("angal.reductionplan.oneinvalidotherreductionrate.msg")
					));
				} else {
					if (priceOtherReductionList != null && !priceOtherReductionList.isEmpty()) {
						boolean duplicateFound = priceOtherReductionList.stream()
							.anyMatch(existing -> existing.getPricesOthers().equals(priceOtherReduction.getPricesOthers()));

						if (duplicateFound) {
							throw new OHDataValidationException(new OHExceptionMessage(
								MessageBundle.getMessage("angal.reductionplan.duplicatepriceotherfound.msg")
							));
						} else {
							priceOtherReductionList.add(priceOtherReduction);
						}
					} else {
						priceOtherReductionList.add(priceOtherReduction);
					}
				}

				PriceOtherReductionModel priceOtherReductionModel = (PriceOtherReductionModel) priceOtherReductionTable.getModel();
				priceOtherReductionModel.fireTableDataChanged();

			} catch (OHDataValidationException validationEx) {
				throw validationEx;
			}  catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}

	private boolean loadDataInObject() {
		try {
			if (!textDescription.getText().equals(reductionPlan.getDescription())
				&& !reductionPlanManager.getByDescription(textDescription.getText()).isEmpty()) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.reductionplan.descriptionalreadyused.msg");

				return false;
			}
			try {
				double examRate = Double.parseDouble(textExamRate.getText());
				double medicalRate = Double.parseDouble(textMedicalRate.getText());
				double operationRate = Double.parseDouble(textOperationRate.getText());
				double priceOtherRate = Double.parseDouble(textPriceOtherRate.getText());

				reductionPlan.setDescription(textDescription.getText());
				reductionPlan.setExamRate(BigDecimal.valueOf(examRate));
				reductionPlan.setMedicalRate(BigDecimal.valueOf(medicalRate));
				reductionPlan.setOperationRate(BigDecimal.valueOf(operationRate));
				reductionPlan.setOtherRate(BigDecimal.valueOf(priceOtherRate));

			} catch (Exception ex) {
				throw new OHDataValidationException(
					new OHExceptionMessage(MessageBundle.getMessage("angal.reductionplan.invalidrateformat.msg"))
				);
			}

			return true;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		return false;
	}

	private void loadDataFromObject() {
		textDescription.setText(this.reductionPlan.getDescription());
		textMedicalRate.setText(String.valueOf(this.reductionPlan.getMedicalRate()));
		textExamRate.setText(String.valueOf(this.reductionPlan.getExamRate()));
		textOperationRate.setText(String.valueOf(this.reductionPlan.getOperationRate()));
		textPriceOtherRate.setText(String.valueOf(this.reductionPlan.getOtherRate()));

		try {
			medicalReductionList = reductionPlanManager.getMedicalReductionsByReductionPlanId(this.reductionPlan.getId());
			operationReductionList = reductionPlanManager.getOperationReductionsByReductionPlanId(this.reductionPlan.getId());
			priceOtherReductionList = reductionPlanManager.getPriceOtherReductionsByReductionPlanId(this.reductionPlan.getId());
			examReductionList = reductionPlanManager.getExamReductionsByReductionPlanId(this.reductionPlan.getId());

			((ExamReductionModel) examReductionTable.getModel()).fireTableDataChanged();
			examReductionTable.updateUI();
			((MedicalReductionModel) medicalReductionTable.getModel()).fireTableDataChanged();
			medicalReductionTable.updateUI();
			((OperationReductionModel) operationReductionTable.getModel()).fireTableDataChanged();
			operationReductionTable.updateUI();
			((PriceOtherReductionModel) priceOtherReductionTable.getModel()).fireTableDataChanged();
			priceOtherReductionTable.updateUI();

		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton();
			jCloseButton.setText(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}

		return jCloseButton;
	}
	public interface ReductionPlanListener extends EventListener {
		void ReductionPlanInserted(AWTEvent aEvent);
	}

	private class ExamReductionModel extends DefaultTableModel {
		@Serial
		private static final long serialVersionUID = 1L;

		public ExamReductionModel(boolean isInsert) {
			try {
				if (!isInsert){
					examReductionList = reductionPlanManager.getExamReductionsByReductionPlanId(reductionPlan.getId());
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public int getRowCount() {
			return examReductionList == null ? 0 : examReductionList.size();
		}

		public String getColumnName(int c) {
			return examReductionColumn[c];
		}

		public int getColumnCount() {
			return examReductionColumn.length;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return examReductionList.get(r).getExam().getDescription();
			} else if (c == -1) {
				return examReductionList.get(r);
			} else if (c == 1) {
				return examReductionList.get(r).getReductionRate();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private class OperationReductionModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public OperationReductionModel() {
			try {
				operationReductionList = reductionPlanManager.getOperationReductionsByReductionPlanId(reductionPlan.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public int getRowCount() {
			return operationReductionList == null ? 0 : operationReductionList.size();
		}

		public String getColumnName(int c) {
			return operationReductionColumn[c];
		}

		public int getColumnCount() {
			return operationReductionColumn.length;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return operationReductionList.get(r).getOperation().getDescription();
			} else if (c == -1) {
				return operationReductionList.get(r);
			} else if (c == 1) {
				return operationReductionList.get(r).getReductionRate();
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private class PriceOtherReductionModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		public PriceOtherReductionModel() {
			try {
				priceOtherReductionList = reductionPlanManager.getPriceOtherReductionsByReductionPlanId(reductionPlan.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public int getRowCount() {
			return priceOtherReductionList == null ? 0 : priceOtherReductionList.size();
		}

		public String getColumnName(int c) {
			return priceOtherReductionColumn[c];
		}

		public int getColumnCount() {
			return priceOtherReductionColumn.length;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return priceOtherReductionList.get(r).getPricesOthers().getDescription();
			} else if (c == -1) {
				return priceOtherReductionList.get(r);
			} else if (c == 1) {
				return priceOtherReductionList.get(r).getReductionRate();
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	private class MedicalReductionModel extends DefaultTableModel {
		@Serial
		private static final long serialVersionUID = 1L;

		public MedicalReductionModel() {
			try {
				medicalReductionList = reductionPlanManager.getMedicalReductionsByReductionPlanId(reductionPlan.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		public int getRowCount() {
			return medicalReductionList == null ? 0 : medicalReductionList.size();
		}

		public String getColumnName(int c) {
			return medicalReductionColumn[c];
		}

		public int getColumnCount() {
			return medicalReductionColumn.length;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return medicalReductionList.get(r).getMedical().getDescription();
			} else if (c == -1) {
				return medicalReductionList.get(r);
			} else if (c == 1) {
				return medicalReductionList.get(r).getReductionRate();
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	public List<OHExceptionMessage> validateReductionPlan(ReductionPlan reductionPlan) {
		List<OHExceptionMessage> errors = new ArrayList<>();

		if (reductionPlan.getExamReductions() != null && !reductionPlan.getExamReductions().isEmpty()) {
			Set<Exam> duplicates = reductionPlan.getExamReductions().stream()
				.collect(Collectors.groupingBy(ExamReduction::getExam, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

			if (!duplicates.isEmpty()) {
				errors.add(
					new OHExceptionMessage(MessageBundle.getMessage("angal.reductionplan.duplicateexamfound.msg"))
				);
			}
		}

		if (reductionPlan.getMedicalReductions() != null && !reductionPlan.getDescription().isEmpty()) {
			Set<Medical> duplicates = reductionPlan.getMedicalReductions().stream()
				.collect(Collectors.groupingBy(MedicalReduction::getMedical, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

			if (!duplicates.isEmpty()) {
				errors.add(
					new OHExceptionMessage(MessageBundle.getMessage("angal.reductionplan.duplicatemedicalfound.msg"))
				);
			}
		}

		if (reductionPlan.getMedicalReductions() != null && !reductionPlan.getMedicalReductions().isEmpty()) {
			Set<Operation> duplicates = reductionPlan.getOperationReductions().stream()
				.collect(Collectors.groupingBy(OperationReduction::getOperation, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

			if (!duplicates.isEmpty()) {
				errors.add(
					new OHExceptionMessage(MessageBundle.getMessage(
						"angal.reductionplan.duplicateoperationfound.msg"))
				);
			}
		}

		if (reductionPlan.getPriceOtherReductions() != null && !reductionPlan.getPriceOtherReductions().isEmpty()) {
			Set<PricesOthers> duplicates = reductionPlan.getPriceOtherReductions().stream()
				.collect(Collectors.groupingBy(PriceOtherReduction::getPricesOthers, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

			if (!duplicates.isEmpty()) {
				errors.add(
					new OHExceptionMessage(MessageBundle.getMessage(
						"angal.reductionplan.duplicatepriceotherfound.msg"))
				);
			}
		}

		errors.addAll(validateReductionRates(reductionPlan));

		return errors;
	}

	private boolean isValidRate(BigDecimal rate) {
		return rate.compareTo(BigDecimal.ZERO) > 0 && rate.compareTo(BigDecimal.valueOf(100)) <= 0;
	}

	private boolean isValidGlobalRate(BigDecimal rate) {
		return rate.compareTo(BigDecimal.ZERO) >= 0 && rate.compareTo(BigDecimal.valueOf(100)) <= 0;
	}

	private <T> List<OHExceptionMessage> validateRates(Collection<T> reductions,
		Function<T, BigDecimal> rateExtractor, String errorMsg) {
		if (reductions == null || reductions.isEmpty()) {
			return Collections.emptyList();
		}
		boolean anyInvalid = reductions.stream()
			.anyMatch(r -> !isValidRate(rateExtractor.apply(r)));
		if (anyInvalid) {
			return Collections.singletonList(
				new OHExceptionMessage(errorMsg)
			);
		}
		return Collections.emptyList();
	}

	public List<OHExceptionMessage> validateReductionRates(ReductionPlan reductionPlan) {
		List<OHExceptionMessage> errors = new ArrayList<>();
		String errorMsg = MessageBundle.getMessage("angal.reductionplan.invalidglobalreductionrate.msg");

		if (!isValidGlobalRate(reductionPlan.getExamRate())) {
			errors.add(new OHExceptionMessage(errorMsg));
		}

		if (!isValidGlobalRate(reductionPlan.getMedicalRate())) {
			errors.add(new OHExceptionMessage(errorMsg));
		}

		if (!isValidGlobalRate(reductionPlan.getOperationRate())) {
			errors.add(new OHExceptionMessage(errorMsg));
		}

		if (!isValidGlobalRate(reductionPlan.getOtherRate())) {
			errors.add(new OHExceptionMessage(errorMsg));
		}

		return errors;
	}
}

