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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import org.isf.reductionplan.manager.ExamReductionManager;
import org.isf.reductionplan.manager.MedicalReductionManager;
import org.isf.reductionplan.manager.OperationReductionManager;
import org.isf.reductionplan.manager.PriceOtherReductionManager;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.reductionplan.model.ExamReduction;
import org.isf.reductionplan.model.MedicalReduction;
import org.isf.reductionplan.model.OperationReduction;
import org.isf.reductionplan.model.PriceOtherReduction;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
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
	private final MedicalReductionManager medicalReductionManager = Context.getApplicationContext().getBean(MedicalReductionManager.class);
	private final ExamReductionManager examReductionManager = Context.getApplicationContext().getBean(ExamReductionManager.class);
	private final OperationReductionManager operationReductionManager = Context.getApplicationContext().getBean(OperationReductionManager.class);
	private final PriceOtherReductionManager priceOtherReductionManager = Context.getApplicationContext().getBean(PriceOtherReductionManager.class);
	private final EventListenerList reductionPlanListener = new EventListenerList();
	private ReductionPlan reductionPlan;
	private final boolean isInsert;
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
	private List<MedicalReduction> medicalReductionList;
	private final List<MedicalReduction> medicalReductionDeletedList = new ArrayList<>();
	private JTable medicalReductionTable;
	private List<ExamReduction> examReductionList;
	private final List<ExamReduction> examReductionDeletedList = new ArrayList<>();
	private JTable examReductionTable;
	private List<OperationReduction> operationReductionList;
	private final List<OperationReduction> operationReductionDeletedList = new ArrayList<>();
	private JTable operationReductionTable;
	private List<PriceOtherReduction> priceOtherReductionList;
	private final List<PriceOtherReduction> priceOtherReductionDeletedList = new ArrayList<>();
	private JTable priceOtherReductionTable;
	private JButton jAddButton;
	private JTabbedPane tabbedPane;
	private JButton jDeleteButton;
	private JButton jSaveButton;

	public ReductionPlanEdit(ReductionPlan reductionPlan, boolean isInsert) {
		this.isInsert = isInsert;
		this.reductionPlan = reductionPlan;
		initialize();
		loadDataFromObject();
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

		setTitle(isInsert ? MessageBundle.getMessage("angal.reductionplan.createreductionplan.title")
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
		gbcButtonPane.anchor = GridBagConstraints.WEST;
		gbcButtonPane.insets = new Insets(5, 5, 5, 5);
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
		examReductionTable.setModel(new ExamReductionModel());

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
			textPriceOtherRate = new JTextField();
		}
		return textPriceOtherRate;
	}
	private JTextField getTextFieldExamRate() {
		if (textExamRate == null) {
			textExamRate = new JTextField();
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
			textMedicalRate = new JTextField();
		}
		return textMedicalRate;
	}
	private JTextField getJTextFieldOperationRate() {
		if (textOperationRate == null) {
			textOperationRate = new JTextField();
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
		buttonPanel.add(getJAddButton());
		buttonPanel.add(getJDeleteButton());
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
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
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton();
			jDeleteButton.setText(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jDeleteButton.addActionListener(actionEvent -> {
				int index;
				int tabSelected = tabbedPane.getSelectedIndex();
				switch (tabSelected) {
				case 0 -> {
					index = medicalReductionTable.getSelectedRow();
					if (medicalReductionList.get(index).getId() != 0) {
						medicalReductionDeletedList.add(medicalReductionList.get(index));
					}
					medicalReductionList.remove(index);
					MedicalReductionModel medicalReductionModel = (MedicalReductionModel) medicalReductionTable.getModel();
					medicalReductionModel.fireTableDataChanged();
				}
				case 1 -> {
					index = examReductionTable.getSelectedRow();
					if (examReductionList.get(index).getId() != 0) {
						examReductionDeletedList.add(examReductionList.get(index));
					}
					examReductionList.remove(index);
					ExamReductionModel examReductionModel = (ExamReductionModel) examReductionTable.getModel();
					examReductionModel.fireTableDataChanged();
				}
				case 2 -> {
					index = operationReductionTable.getSelectedRow();
					if (operationReductionList.get(index).getId() != 0) {
						operationReductionDeletedList.add(operationReductionList.get(index));
					}
					operationReductionList.remove(index);
					OperationReductionModel operationReductionModel = (OperationReductionModel) operationReductionTable.getModel();
					operationReductionModel.fireTableDataChanged();
				}
				case 3 -> {
					index = priceOtherReductionTable.getSelectedRow();
					if (priceOtherReductionList.get(index).getId() != 0) {
						priceOtherReductionDeletedList.add(priceOtherReductionList.get(index));
					}
					priceOtherReductionList.remove(index);
					PriceOtherReductionModel priceOtherReductionModel = (PriceOtherReductionModel) priceOtherReductionTable.getModel();
					priceOtherReductionModel.fireTableDataChanged();
				}
				default -> {
				}
				}
			});
		}
		return jDeleteButton;
	}
	private JButton getJSaveButton() {
		if (jSaveButton == null) {
			jSaveButton = new JButton();
			jSaveButton.setText(MessageBundle.getMessage("angal.common.save.btn"));
			jSaveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			jSaveButton.addActionListener(actionEvent -> {
				try {
					deleteUnsedUnderReductionPlan();
					if (loadDataInObject()) {
						if (isInsert) {
							reductionPlan = reductionPlanManager.save(reductionPlan);
						} else {
							reductionPlan = reductionPlanManager.update(reductionPlan);
						}
					}
					if (!examReductionList.isEmpty()) {
						for (ExamReduction examReduction : examReductionList) {
							examReduction.setReductionPlan(reductionPlan);
							examReductionManager.save(examReduction);
						}
					}
					if (!medicalReductionList.isEmpty()) {
						for (MedicalReduction medicalReduction : medicalReductionList) {
							medicalReduction.setReductionPlan(reductionPlan);
							medicalReductionManager.save(medicalReduction);
						}
					}
					if(!operationReductionList.isEmpty()) {
						for (OperationReduction operationReduction : operationReductionList) {
							operationReduction.setReductionPlan(reductionPlan);
							operationReductionManager.save(operationReduction);
						}
					}
					if (!priceOtherReductionList.isEmpty()) {
						for (PriceOtherReduction priceOtherReduction : priceOtherReductionList) {
							priceOtherReduction.setReductionPlan(reductionPlan);
							priceOtherReductionManager.save(priceOtherReduction);
						}
					}
					fireReductionPlanInserted();
					jCloseButton.doClick();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}
		return jSaveButton;
	}
	private void addMedicalReduction() throws OHServiceException {
		List<Medical> medicalList = medicalBrowsingManager.getMedicals();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		Medical medical = (Medical) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, medicalList.toArray(), "",
		"angal.newbill.selectamedical.txt", "angal.newbill.medical.title");

		if (medical != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			if (stringRate == null || stringRate.isEmpty()) {
				return;
			}

			try {
				rate = Double.parseDouble(stringRate);
				MedicalReduction medicalReduction = new MedicalReduction(reductionPlan, medical, rate);
				medicalReductionList.add(medicalReduction);
				MedicalReductionModel medicalReductionModel = (MedicalReductionModel) medicalReductionTable.getModel();
				medicalReductionModel.fireTableDataChanged();
			} catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}
	private void addExamReduction() throws OHServiceException {
		List<Exam> examList = examBrowsingManager.getExams();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		Exam exam = (Exam) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, examList.toArray(), "",
			"angal.newbill.selectanexam.txt", "angal.newbill.exam.title");

		if (exam != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			try {
				if (stringRate == null || stringRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(stringRate);
				ExamReduction exaReduction = new ExamReduction(reductionPlan, exam, rate);
				examReductionList.add(exaReduction);
				ExamReductionModel examModel = (ExamReductionModel) examReductionTable.getModel();
				examModel.fireTableDataChanged();
			} catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}
	private void addOperationReduction() throws OHServiceException {
		List<Operation> operationList = operationBrowserManager.getOperation();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		Operation operation = (Operation) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, operationList.toArray(), "",
			"angal.newbill.selectanoperation.txt", "angal.newbill.operation.title");

		if (operation != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			try {
				if (stringRate == null || stringRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(stringRate);
				OperationReduction opeReduction = new OperationReduction(reductionPlan, operation, rate);
				operationReductionList.add(opeReduction);
				OperationReductionModel opeModel = (OperationReductionModel) operationReductionTable.getModel();
				opeModel.fireTableDataChanged();
			} catch (Exception eee) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}
	private void addPriceOtherReduction() throws OHServiceException {
		List<PricesOthers> pricesOthersList = pricesOthersManager.getOthers();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		PricesOthers pricesOthers = (PricesOthers) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, pricesOthersList.toArray(),
			"", "angal.newbill.pleaseselectanitem.txt", "angal.newbill.item.title");

		if (pricesOthers != null) {
			double rate = 0;
			String strRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				"angal.reductionplan.reductionrate.txt", "angal.reductionplan.reductionrate.title");

			try {
				if (strRate == null || strRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(strRate);
				PriceOtherReduction priceOtherReduction = new PriceOtherReduction(reductionPlan, pricesOthers, rate);
				priceOtherReductionList.add(priceOtherReduction);
				PriceOtherReductionModel priceOtherReductionModel = (PriceOtherReductionModel) priceOtherReductionTable.getModel();
				priceOtherReductionModel.fireTableDataChanged();

			} catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		}
	}
	private boolean loadDataInObject() {
		try {
			if (!textDescription.getText().equals(reductionPlan.getDescription())
				&& !reductionPlanManager.getByDescription(textDescription.getText(), false).isEmpty()) {
				MessageDialog.error(ReductionPlanEdit.this, "angal.reductionplan.descriptionalreadyused.msg");
				return false;
			}

			reductionPlan.setDescription(textDescription.getText());
			reductionPlan.setExamRate(Double.parseDouble(textExamRate.getText()));
			reductionPlan.setMedicalRate(Double.parseDouble(textMedicalRate.getText()));
			reductionPlan.setOperationRate(Double.parseDouble(textOperationRate.getText()));
			reductionPlan.setOtherRate(Double.parseDouble(textPriceOtherRate.getText()));
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
			medicalReductionList = medicalReductionManager.getByReductionPlanId(this.reductionPlan.getId(), false);
			examReductionList = examReductionManager.getByReductionPlanId(this.reductionPlan.getId(), false);
			operationReductionList = operationReductionManager.getByReductionPlanId(this.reductionPlan.getId(), false);
			priceOtherReductionList = priceOtherReductionManager.getByReductionPlanId(this.reductionPlan.getId(), false);

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

	private void deleteUnsedUnderReductionPlan() throws OHServiceException {
		if (!examReductionDeletedList.isEmpty()) {
			examReductionManager.deleteBulk(examReductionDeletedList);
		}
		if (!medicalReductionDeletedList.isEmpty()) {
			medicalReductionManager.deleteBulk(medicalReductionDeletedList);
		}
		if (!operationReductionDeletedList.isEmpty()) {
			operationReductionManager.deleteBulk(operationReductionDeletedList);
		}
		if (!priceOtherReductionDeletedList.isEmpty()) {
			priceOtherReductionManager.deleteBulk(priceOtherReductionDeletedList);
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

		public ExamReductionModel() {
			try {
				examReductionList = examReductionManager.getByReductionPlanId(reductionPlan.getId(), false);
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
				operationReductionList = operationReductionManager.getByReductionPlanId(reductionPlan.getId(), false);
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
				priceOtherReductionList = priceOtherReductionManager.getByReductionPlanId(reductionPlan.getId(), false);
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
				medicalReductionList = medicalReductionManager.getByReductionPlanId(reductionPlan.getId(), false);
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
}

