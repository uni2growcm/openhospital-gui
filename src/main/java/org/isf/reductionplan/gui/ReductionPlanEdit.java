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
package org.isf.reductionplan.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
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
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class ReductionPlanEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private final EventListenerList reductionPlanListeners = new EventListenerList();

	public interface ReductionPlanListener extends EventListener {
		void reductionPlanInserted(AWTEvent e);
		void reductionPlanUpdated(AWTEvent e);
	}

	public void addReductionPlanListener(ReductionPlanListener l) {
		reductionPlanListeners.add(ReductionPlanListener.class, l);
	}

	private void fireReductionPlanInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (EventListener listener : reductionPlanListeners.getListeners(ReductionPlanListener.class)) {
			((ReductionPlanListener) listener).reductionPlanInserted(event);
		}
	}

	private void fireReductionPlanUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};
		for (EventListener listener : reductionPlanListeners.getListeners(ReductionPlanListener.class)) {
			((ReductionPlanListener) listener).reductionPlanUpdated(event);
		}
	}

	private ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);
	private ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);

	private final boolean insert;
	private final ReductionPlan reductionPlan;

	private JPanel jPanelData;
	private JPanel jPanelButtons;
	private JTextField jTextFieldDescription;
	private JTextField jTextFieldMedicalRate;
	private JTextField jTextFieldExamRate;
	private JTextField jTextFieldOperationRate;
	private JTextField jTextFieldOtherRate;
	private JTabbedPane jTabbedPaneExceptions;
	private JButton jButtonOK;
	private JButton jButtonCancel;

	public ReductionPlanEdit(JFrame parent, ReductionPlan reductionPlan, boolean inserting) {
		super(parent, true);
		this.insert = inserting;
		this.reductionPlan = reductionPlan;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelData(), BorderLayout.NORTH);
		add(getJTabbedPaneExceptions(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(650, 500);
		setTitle(MessageBundle.getMessage(insert ? "angal.reductionplan.newreductionplan.title" : "angal.reductionplan.editreductionplan.title"));
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel(new SpringLayout());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.reductionplan.description.label")));
			jPanelData.add(getJTextFieldDescription());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.reductionplan.medicalrate.label")));
			jPanelData.add(getJTextFieldMedicalRate());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.reductionplan.examrate.label")));
			jPanelData.add(getJTextFieldExamRate());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.reductionplan.operationrate.label")));
			jPanelData.add(getJTextFieldOperationRate());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.reductionplan.otherrate.label")));
			jPanelData.add(getJTextFieldOtherRate());
			SpringUtilities.makeCompactGrid(jPanelData, 5, 2, 5, 5, 5, 5);
		}
		return jPanelData;
	}

	private JTextField getJTextFieldDescription() {
		if (jTextFieldDescription == null) {
			jTextFieldDescription = new VoLimitedTextField(100, 30);
			jTextFieldDescription.setText(reductionPlan.getDescription());
		}
		return jTextFieldDescription;
	}

	private JTextField getJTextFieldMedicalRate() {
		if (jTextFieldMedicalRate == null) {
			jTextFieldMedicalRate = new JTextField(6);
			jTextFieldMedicalRate.setText(String.valueOf(reductionPlan.getMedicalRate()));
		}
		return jTextFieldMedicalRate;
	}

	private JTextField getJTextFieldExamRate() {
		if (jTextFieldExamRate == null) {
			jTextFieldExamRate = new JTextField(6);
			jTextFieldExamRate.setText(String.valueOf(reductionPlan.getExamRate()));
		}
		return jTextFieldExamRate;
	}

	private JTextField getJTextFieldOperationRate() {
		if (jTextFieldOperationRate == null) {
			jTextFieldOperationRate = new JTextField(6);
			jTextFieldOperationRate.setText(String.valueOf(reductionPlan.getOperationRate()));
		}
		return jTextFieldOperationRate;
	}

	private JTextField getJTextFieldOtherRate() {
		if (jTextFieldOtherRate == null) {
			jTextFieldOtherRate = new JTextField(6);
			jTextFieldOtherRate.setText(String.valueOf(reductionPlan.getOtherRate()));
		}
		return jTextFieldOtherRate;
	}

	private JTabbedPane getJTabbedPaneExceptions() {
		if (jTabbedPaneExceptions == null) {
			jTabbedPaneExceptions = new JTabbedPane();

			List<Medical> medicals = new ArrayList<>();
			List<Exam> exams = new ArrayList<>();
			List<Operation> operations = new ArrayList<>();
			List<PricesOthers> others = new ArrayList<>();
			try {
				medicals = medicalBrowsingManager.getMedicals();
				exams = examBrowsingManager.getExams();
				operations = operationBrowserManager.getOperation();
				others = pricesOthersManager.getOthers();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}

			jTabbedPaneExceptions.addTab(MessageBundle.getMessage("angal.reductionplan.medical.tab"),
				buildExceptionsPanel(medicals, reductionPlan.getMedicalReductions(),
					MedicalReduction::getMedical, MedicalReduction::getReductionRate,
					(medical, rate) -> new MedicalReduction(reductionPlan, medical, rate)));

			jTabbedPaneExceptions.addTab(MessageBundle.getMessage("angal.reductionplan.exam.tab"),
				buildExceptionsPanel(exams, reductionPlan.getExamReductions(),
					ExamReduction::getExam, ExamReduction::getReductionRate,
					(exam, rate) -> new ExamReduction(reductionPlan, exam, rate)));

			jTabbedPaneExceptions.addTab(MessageBundle.getMessage("angal.reductionplan.operation.tab"),
				buildExceptionsPanel(operations, reductionPlan.getOperationReductions(),
					OperationReduction::getOperation, OperationReduction::getReductionRate,
					(operation, rate) -> new OperationReduction(reductionPlan, operation, rate)));

			jTabbedPaneExceptions.addTab(MessageBundle.getMessage("angal.reductionplan.other.tab"),
				buildExceptionsPanel(others, reductionPlan.getPriceOtherReductions(),
					PriceOtherReduction::getPricesOthers, PriceOtherReduction::getReductionRate,
					(other, rate) -> new PriceOtherReduction(reductionPlan, other, rate)));
		}
		return jTabbedPaneExceptions;
	}

	/**
	 * One tab: a table of the plan's current per-item exception rows for one category, plus a picker
	 * to add a new exception (from the catalog items not already overridden) and a button to remove the
	 * selected row. Shared across the 4 categories (medical/exam/operation/other) since they only differ
	 * in which catalog item type and which {@link ReductionPlan} exception list they operate on.
	 */
	private <I, T> JPanel buildExceptionsPanel(
			List<I> catalogItems,
			List<T> rows,
			Function<T, I> itemGetter,
			Function<T, BigDecimal> rateGetter,
			BiFunction<I, BigDecimal, T> factory) {

		JPanel panel = new JPanel(new BorderLayout());

		DefaultTableModel tableModel = new DefaultTableModel(
			new Object[] { MessageBundle.getMessage("angal.reductionplan.item.label"), MessageBundle.getMessage("angal.reductionplan.reductionrate.label") }, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable table = new JTable(tableModel);

		JComboBox<I> itemComboBox = new JComboBox<>();
		JTextField rateField = new JTextField(5);
		JButton addButton = new JButton(MessageBundle.getMessage("angal.reductionplan.add.btn"));
		JButton removeButton = new JButton(MessageBundle.getMessage("angal.reductionplan.remove.btn"));

		Runnable refresh = () -> {
			tableModel.setRowCount(0);
			for (T row : rows) {
				tableModel.addRow(new Object[] { itemGetter.apply(row), rateGetter.apply(row) });
			}
			itemComboBox.removeAllItems();
			for (I item : catalogItems) {
				boolean alreadyUsed = rows.stream().anyMatch(row -> itemGetter.apply(row).equals(item));
				if (!alreadyUsed) {
					itemComboBox.addItem(item);
				}
			}
		};
		refresh.run();

		addButton.addActionListener(actionEvent -> {
			I selectedItem = (I) itemComboBox.getSelectedItem();
			if (selectedItem == null) {
				return;
			}
			BigDecimal rate;
			try {
				rate = new BigDecimal(rateField.getText().trim());
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
			rows.add(factory.apply(selectedItem, rate));
			rateField.setText("");
			refresh.run();
		});

		removeButton.addActionListener(actionEvent -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow < 0) {
				return;
			}
			rows.remove(selectedRow);
			refresh.run();
		});

		JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addPanel.add(itemComboBox);
		addPanel.add(new JLabel(MessageBundle.getMessage("angal.reductionplan.reductionrate.label")));
		addPanel.add(rateField);
		addPanel.add(addButton);
		addPanel.add(removeButton);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(addPanel, BorderLayout.SOUTH);
		return panel;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(actionEvent -> {
				reductionPlan.setDescription(jTextFieldDescription.getText());
				try {
					reductionPlan.setMedicalRate(new BigDecimal(jTextFieldMedicalRate.getText().trim()));
					reductionPlan.setExamRate(new BigDecimal(jTextFieldExamRate.getText().trim()));
					reductionPlan.setOperationRate(new BigDecimal(jTextFieldOperationRate.getText().trim()));
					reductionPlan.setOtherRate(new BigDecimal(jTextFieldOtherRate.getText().trim()));
				} catch (NumberFormatException nfe) {
					MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
					return;
				}

				try {
					if (insert) {
						reductionPlanManager.add(reductionPlan);
						fireReductionPlanInserted();
					} else {
						reductionPlanManager.update(reductionPlan);
						fireReductionPlanUpdated();
					}
					dispose();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
				}
			});
		}
		return jButtonOK;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}
}
