package org.isf.reductionplan.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.EventListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

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
import org.isf.utils.jobjects.ModalJFrame;

public class ReductionPlanEdit extends ModalJFrame {
	
	@Serial
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton jCloseButton;
	private final String[] medicalReductionColumn = new String[] {
					MessageBundle.getMessage("angal.reduction.medical"),
					MessageBundle.getMessage("angal.reduction.reductionrate") };
	private final String[] examReductionColumn = new String[] {
					MessageBundle.getMessage("angal.reduction.exam"),
					MessageBundle.getMessage("angal.reduction.reductionrate") };
	private final String[] operationReductionColumn = new String[] {
					MessageBundle.getMessage("angal.reduction.operation"),
					MessageBundle.getMessage("angal.reduction.reductionrate") };
	private final String[] priceOtherReductionColumn = new String[] {
					MessageBundle.getMessage("angal.reduction.other"),
					MessageBundle.getMessage("angal.reduction.reductionrate") };
	private ReductionPlan reductionPlan=new ReductionPlan();
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
	private JTable medicalReductionTable;

	private List<ExamReduction> examReductionList;
	private JTable examReductionTable;

	private List<OperationReduction> operationReductionList;
	private JTable operationReductionTable;

	private List<PriceOtherReduction> priceOtherReductionList;
	private JTable priceOtherReductionTable;
	private JButton jAddbutton;
	private JTabbedPane tabbedPane;
	private JButton jDeleteButton;
	private boolean isInsert;
	private JButton jSaveButton;

	private final ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);
	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final ExamBrowsingManager examBrowsingManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private final OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private final PricesOthersManager pricesOthersManager = Context .getApplicationContext().getBean(PricesOthersManager.class);


	private final EventListenerList reductionPlanListener = new EventListenerList();

	public interface ReductionPlanListener extends EventListener {
		public void ReductionPlanInserted(AWTEvent aEvent);
	}

	public void addReductionPlanListener(ReductionPlanListener l) {
		reductionPlanListener.add(ReductionPlanListener.class, l);
	}

	private void fireReductionPlanInserted(ReductionPlan reductionPlan) {
		AWTEvent event = new AWTEvent(reductionPlan, AWTEvent.RESERVED_ID_MAX + 1) {
			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = reductionPlanListener.getListeners(ReductionPlanListener.class);
		for (EventListener listener : listeners) {
			((ReductionPlanListener) listener).ReductionPlanInserted(event);
		}
	}


	public ReductionPlanEdit(ReductionPlan reductionPlan, boolean isInsert) {
		this.isInsert=isInsert;
		this.reductionPlan=reductionPlan;
		initialize();
		loadDataFromObject();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.reduction.editreductionplan"));
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
			labelPriceOtherRate = new JLabel(MessageBundle.getMessage("angal.reduction.otherrate"));
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
			labelExamRate = new JLabel(MessageBundle.getMessage("angal.reduction.examrate"));
		}
		return labelExamRate;
	}

	private JLabel getJLabelOperationRate() {
		if (labelOperationRate == null) {
			labelOperationRate = new JLabel(MessageBundle.getMessage("angal.reduction.operate"));
		}
		return labelOperationRate;

	}

	private JLabel getJLabelMedicalRate() {
		if (labelMedicalRate == null) {
			labelMedicalRate = new JLabel(MessageBundle.getMessage("angal.reduction.medicalrate"));
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
			labelDescription = new JLabel(MessageBundle.getMessage("angal.common.description"));
		}
		return labelDescription;
	}

	private JPanel getJTabbedPane() {
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(MessageBundle.getMessage("angal.reduction.medical") , getMedicalReductionPanel());
		tabbedPane.addTab(MessageBundle.getMessage("angal.reduction.exam"), getExamReductionPanel());
		tabbedPane.addTab(MessageBundle.getMessage("angal.reduction.operation"), getOperationReductionPanel());
		tabbedPane.addTab(MessageBundle.getMessage("angal.reduction.other"), getPriceOtherReductionPanel());
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
		if (jAddbutton == null) {
			jAddbutton = new JButton();
			jAddbutton.setText(MessageBundle.getMessage("angal.reductionplan.add.btn"));
			jAddbutton.setMnemonic(MessageBundle.getMnemonic("angal.reductionplan.add.btn.key"));
			jAddbutton.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jAddbutton.addActionListener(actionEvent -> {
				int tabSelected = tabbedPane.getSelectedIndex();
				switch (tabSelected) {
					case 0 -> {
						try {
							addMedicalReduction();
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
					case 1 -> {
						try {
							addExamReduction();
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
					case 2 -> {
						try {
							addOperationReduction();
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
					case 3 -> {
						try {
							addPriceOtherReduction();
						} catch (OHServiceException e) {
							throw new RuntimeException(e);
						}
					}
					default -> {}
				}
			});
		}
		return jAddbutton;
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
						medicalReductionList.remove(index);
						MedicalReductionModel medicalReductionModel = (MedicalReductionModel)medicalReductionTable.getModel();
						medicalReductionModel.fireTableDataChanged();
					}
					case 1 -> {
						index=examReductionTable.getSelectedRow();
						examReductionList.remove(index);
						ExamReductionModel examReductionModel=(ExamReductionModel)examReductionTable.getModel();
						examReductionModel.fireTableDataChanged();
					}
					case 2 -> {
						index= operationReductionTable.getSelectedRow();
						operationReductionList.remove(index);
						OperationReductionModel operationReductionModel=(OperationReductionModel) operationReductionTable.getModel();
						operationReductionModel.fireTableDataChanged();
					}
					case 3 -> {
						index= priceOtherReductionTable.getSelectedRow();
						priceOtherReductionList.remove(index);
						PriceOtherReductionModel priceOtherReductionModel=(PriceOtherReductionModel) priceOtherReductionTable.getModel();
						priceOtherReductionModel.fireTableDataChanged();
					}
					default -> {}
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
					if(loadDataInObject()){
						if(isInsert){
							reductionPlanManager.save(reductionPlan);
						}
						else{
							reductionPlanManager.update(reductionPlan);
						}
						fireReductionPlanInserted(reductionPlan);
						ReductionPlanEdit.this.dispose();
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

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		Medical medical = (Medical) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, medicalList.toArray(), "",
			MessageBundle.getMessage("angal.newbill.selectamedical"), MessageBundle.getMessage("angal.newbill.medical"));

		if (medical != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				MessageBundle.getMessage("angal.reduction.reductionrate"),  MessageBundle.getMessage("angal.reduction.reductionrate"));

			if (stringRate == null || stringRate.isEmpty()) {
				return;
			}

			try	{
				rate = Double.parseDouble(stringRate);
				MedicalReduction medicalReduction = new MedicalReduction(reductionPlan, medical, rate);
				medicalReductionList.add(medicalReduction);
				MedicalReductionModel medicalReductionModel=(MedicalReductionModel)medicalReductionTable.getModel();
				medicalReductionModel.fireTableDataChanged();
			} catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain"),
					MessageBundle.getMessage("angal.newbill.invalidquantity"));
			}
		}
	}

	private void addExamReduction() throws OHServiceException {
		List<Exam> examList = examBrowsingManager.getExams();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		Exam exam = (Exam) MessageDialog.inputDialog(ReductionPlanEdit.this, icon,examList.toArray(), "",
			MessageBundle.getMessage("angal.newbill.selectanexam"), MessageBundle.getMessage("angal.newbill.exam"));

		if (exam != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				MessageBundle.getMessage("angal.reduction.reductionrate"), MessageBundle.getMessage("angal.reduction.reductionrate"));

			try {
				if (stringRate == null || stringRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(stringRate);
				ExamReduction exaReduction=new ExamReduction(reductionPlan, exam, rate);
				examReductionList.add(exaReduction);
				ExamReductionModel examModel=(ExamReductionModel)examReductionTable.getModel();
				examModel.fireTableDataChanged();
			} catch (Exception ex) {
				MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain"),
					MessageBundle.getMessage("angal.newbill.invalidquantity"));
			}
		}
	}

	private void addOperationReduction() throws OHServiceException {
		List<Operation> operationList = operationBrowserManager.getOperation();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		Operation operation = (Operation) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, operationList.toArray(), "",
			MessageBundle.getMessage("angal.newbill.selectanoperation"), MessageBundle.getMessage("angal.newbill.operation"));

		if (operation != null) {
			double rate = 0;
			String stringRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				MessageBundle.getMessage("angal.reduction.reductionrate"), MessageBundle.getMessage("angal.reduction.reductionrate"));

			try {
				if (stringRate == null || stringRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(stringRate);
				OperationReduction opeReduction=new OperationReduction(reductionPlan, operation, rate);
				operationReductionList.add(opeReduction);
				OperationReductionModel opeModel=(OperationReductionModel) operationReductionTable.getModel();
				opeModel.fireTableDataChanged();
			} catch (Exception eee) {
				MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain"),
					MessageBundle.getMessage("angal.newbill.invalidquantity"));
			}
		}
	}
	private void addPriceOtherReduction() throws OHServiceException {
		List<PricesOthers> pricesOthersList = pricesOthersManager.getOthers();

		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");
		PricesOthers pricesOthers = (PricesOthers) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, pricesOthersList.toArray(),
			"", MessageBundle.getMessage("angal.newbill.pleaseselectanitem"), MessageBundle.getMessage("angal.newbill.item"));

		if (pricesOthers != null) {
			double rate = 0;
			String strRate = (String) MessageDialog.inputDialog(ReductionPlanEdit.this, icon, null, rate,
				MessageBundle.getMessage("angal.reduction.reductionrate"), MessageBundle.getMessage("angal.reduction.reductionrate"));

			try {
				if (strRate == null || strRate.isEmpty()) {
					return;
				}
				rate = Double.parseDouble(strRate);
				PriceOtherReduction priceOtherReduction=new PriceOtherReduction(reductionPlan, pricesOthers, rate);
				priceOtherReductionList.add(priceOtherReduction);
				PriceOtherReductionModel priceOtherReductionModel=(PriceOtherReductionModel) priceOtherReductionTable.getModel();
				priceOtherReductionModel.fireTableDataChanged();

			} catch (Exception eee) {
				MessageDialog.error(ReductionPlanEdit.this, MessageBundle.getMessage("angal.newbill.invalidquantitypleasetryagain"),
					MessageBundle.getMessage("angal.newbill.invalidquantity"));
			}
		}
	}
	private boolean loadDataInObject() {
		try {
			if(!textDescription.getText().equals(reductionPlan.getDescription())) {
				if(reductionPlanManager.getByDescription(textDescription.getText()) != null) {
					JOptionPane.showMessageDialog(ReductionPlanEdit.this, MessageBundle.getMessage("angal.reduction.descriptionused"));
					return false;
				}
			}

			reductionPlan.setDescription(textDescription.getText());
			reductionPlan.setExamReductions(examReductionList);
			reductionPlan.setExamRate(Double.valueOf(textExamRate.getText()));
			reductionPlan.setMedicalRate(Double.valueOf(textMedicalRate.getText()));
			reductionPlan.setMedicalsReductions(medicalReductionList);
			reductionPlan.setOperationreductions(operationReductionList);
			reductionPlan.setOperationRate(Double.valueOf(textOperationRate.getText()));
			reductionPlan.setOtherReductions(priceOtherReductionList);
			reductionPlan.setOtherRate(Double.valueOf(textPriceOtherRate.getText()));
			return true;
		} catch (NumberFormatException e) {
			throw new OHException(e.getMessage());
		}
	}
	private void loadDataFromObject(){
		textDescription.setText(this.reductionPlan.getDescription());
		textMedicalRate.setText(String.valueOf(this.reductionPlan.getMedicalRate()));
		textExamRate.setText(String.valueOf(this.reductionPlan.getExamRate()));
		textOperationRate.setText(String.valueOf(this.reductionPlan.getOperationRate()));
		textPriceOtherRate.setText(String.valueOf(this.reductionPlan.getOtherRate()));

		try {
			medicalReductionList=manager.getMedicalsReductions(this.reductionPlan.getId());
			examReductionList = manager.getExamsReductions(this.reductionPlan
							.getId());
			operationReductionList =manager.getOperationsReductions(this.reductionPlan.getId());
			priceOtherReductionList = manager.getOtherReductions(this.reductionPlan
							.getId());

			((ExamReductionModel) examReductionTable.getModel()).fireTableDataChanged();
			examReductionTable.updateUI();
			((MedicalReductionModel) medicalReductionTable.getModel()).fireTableDataChanged();
			medicalReductionTable.updateUI();
			((OperationReductionModel) operationReductionTable.getModel()).fireTableDataChanged();
			operationReductionTable.updateUI();
			((PriceOtherReductionModel) priceOtherReductionTable.getModel()).fireTableDataChanged();
			priceOtherReductionTable.updateUI();

		} catch (OHException e) {
			e.printStackTrace();
		}


	}
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton();
			jCloseButton
							.setText(MessageBundle.getMessage("angal.common.close"));
			jCloseButton.setMnemonic(KeyEvent.VK_C);
			jCloseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
		}
		return jCloseButton;
	}

	private class ExamReductionModel extends DefaultTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private ExamBrowsingManager examManager = new ExamBrowsingManager();

		public ExamReductionModel() throws OHException {
			ReductionPlanManager manager = new ReductionPlanManager();
			try{
				examReductionList = manager.getExamsReductions(reductionPlan
								.getId());
			}
			catch(NullPointerException e){
				e.printStackTrace();
			}
		}

		public int getRowCount() {
			if (examReductionList == null)
				return 0;
			return examReductionList.size();
		}

		public String getColumnName(int c) {
			return examReductionColumn[c];
		}

		public int getColumnCount() {
			return examReductionColumn.length;
		}

		// { "CODE", "DESCRIPTION","MEDICALRATE","EXAMRATE","OPERATIONRATE",
		// "OTHERRATE"};
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return examManager.getExam(
								examReductionList.get(r).getExaCode()).getDescription();
			} else if (c == -1) {
				return examReductionList.get(r);
			} else if (c == 1) {
				return examReductionList.get(r).getReductionRate();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	private class OperationReductionModel extends DefaultTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private OperationBrowserManager opeManager = new OperationBrowserManager();

		public OperationReductionModel() throws OHException {
			ReductionPlanManager manager = new ReductionPlanManager();
			try {
				operationReductionList = manager
								.getOperationsReductions(reductionPlan.getId());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		public int getRowCount() {
			if (operationReductionList == null)
				return 0;
			return operationReductionList.size();
		}

		public String getColumnName(int c) {
			return operationReductionColumn[c];
		}

		public int getColumnCount() {
			return operationReductionColumn.length;
		}

		// { "CODE", "DESCRIPTION","MEDICALRATE","EXAMRATE","OPERATIONRATE",
		// "OTHERRATE"};
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return opeManager.getOperationByCode(
								operationReductionList.get(r).getOpeCode()).getDescription();
			} else if (c == -1) {
				return operationReductionList.get(r);
			} else if (c == 1) {
				return operationReductionList.get(r).getReductionRate();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	private class PriceOtherReductionModel extends DefaultTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private PricesOthersManager othManager = new PricesOthersManager();

		public PriceOtherReductionModel() throws OHException {
			ReductionPlanManager manager = new ReductionPlanManager();
			try{
				priceOtherReductionList = manager.getOtherReductions(reductionPlan
								.getId());
			}
			catch(NullPointerException e){
				e.printStackTrace();
			}
		}

		public int getRowCount() {
			if (priceOtherReductionList == null)
				return 0;
			return priceOtherReductionList.size();
		}

		public String getColumnName(int c) {
			return priceOtherReductionColumn[c];
		}

		public int getColumnCount() {
			return priceOtherReductionColumn.length;
		}

		// { "CODE", "DESCRIPTION","MEDICALRATE","EXAMRATE","OPERATIONRATE",
		// "OTHERRATE"};
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return othManager.getOther(priceOtherReductionList.get(r).getOthID());
			} else if (c == -1) {
				return priceOtherReductionList.get(r);
			} else if (c == 1) {
				return priceOtherReductionList.get(r).getReductionRate();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}

	private class MedicalReductionModel extends DefaultTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		MedicalBrowsingManager medManager = new MedicalBrowsingManager();

		public MedicalReductionModel() throws OHException {
			ReductionPlanManager manager = new ReductionPlanManager();
			try {
				medicalReductionList = manager
								.getMedicalsReductions(reductionPlan.getId());
			}
			catch(NullPointerException e){
				e.printStackTrace();
			}
		}

		public int getRowCount() {
			if (medicalReductionList == null)
				return 0;
			return medicalReductionList.size();
		}

		public String getColumnName(int c) {
			return medicalReductionColumn[c];
		}

		public int getColumnCount() {
			return medicalReductionColumn.length;
		}

		// { "CODE", "DESCRIPTION","MEDICALRATE","EXAMRATE","OPERATIONRATE",
		// "OTHERRATE"};
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return medManager.getMedical(
												medicalReductionList.get(r).getMedID())
								.getDescription();
			} else if (c == -1) {
				return medicalReductionList.get(r);
			} else if (c == 1) {
				return medicalReductionList.get(r).getReductionRate();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// return super.isCellEditable(arg0, arg1);
			return false;
		}
	}
}

