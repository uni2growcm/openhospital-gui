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
package org.isf.pregnancy.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.pregnancy.manager.PregnancyExamParameterBrowserManager;
import org.isf.pregnancy.model.PregnancyExamDataType;
import org.isf.pregnancy.model.PregnancyExamParameter;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoDoubleTextField;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

/**
 * CRUD dialog for a single {@link PregnancyExamParameter} of the "paramètres CPN" catalog.
 */
public class CpnExamParameterEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private final EventListenerList listeners = new EventListenerList();

	public interface CpnExamParameterListener extends EventListener {

		void cpnExamParameterUpdated(AWTEvent e);

		void cpnExamParameterInserted(AWTEvent e);
	}

	public void addCpnExamParameterListener(CpnExamParameterListener l) {
		listeners.add(CpnExamParameterListener.class, l);
	}

	private void fireInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};
		for (EventListener l : listeners.getListeners(CpnExamParameterListener.class)) {
			((CpnExamParameterListener) l).cpnExamParameterInserted(event);
		}
	}

	private void fireUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};
		for (EventListener l : listeners.getListeners(CpnExamParameterListener.class)) {
			((CpnExamParameterListener) l).cpnExamParameterUpdated(event);
		}
	}

	private static final String[] TYPE_LABELS = {
			MessageBundle.getMessage("angal.cpn.prenatal.txt"),
			MessageBundle.getMessage("angal.cpn.postnatal.txt"),
			MessageBundle.getMessage("angal.cpn.both.txt")
	};
	private static final int[] TYPE_VALUES = { PregnancyExamParameter.PRENATAL, PregnancyExamParameter.POSTNATAL, PregnancyExamParameter.BOTH };

	private final PregnancyExamParameterBrowserManager examParameterManager =
			Context.getApplicationContext().getBean(PregnancyExamParameterBrowserManager.class);

	private final PregnancyExamParameter examParameter;
	private final boolean insert;

	private VoLimitedTextField codeTextField;
	private JTextField descriptionTextField;
	private JComboBox<String> typeComboBox;
	private JComboBox<PregnancyExamDataType> dataTypeComboBox;
	private JTextField unitTextField;
	private VoDoubleTextField maxValueTextField;
	private JTextField allowedValuesTextField;
	private JCheckBox defaultBooleanCheckBox;
	private JPanel dataTypeDetailPanel;
	private CardLayout dataTypeDetailLayout;

	public CpnExamParameterEdit(JFrame owner, PregnancyExamParameter examParameter, boolean insert) {
		super(owner, true);
		this.insert = insert;
		this.examParameter = examParameter;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage(insert ? "angal.cpn.newcpnexamparameter.title" : "angal.cpn.editcpnexamparameter.title"));
		setContentPane(getContentPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getContentPanel() {
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(getDataPanel(), BorderLayout.NORTH);
		contentPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		return contentPanel;
	}

	private JPanel getDataPanel() {
		JPanel dataPanel = new JPanel(new SpringLayout());

		codeTextField = new VoLimitedTextField(10);
		if (!insert) {
			codeTextField.setText(examParameter.getCode());
			codeTextField.setEnabled(false);
		}
		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':'));
		dataPanel.add(codeTextField);

		descriptionTextField = new JTextField(25);
		if (!insert) {
			descriptionTextField.setText(examParameter.getDescription());
		}
		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':'));
		dataPanel.add(descriptionTextField);

		typeComboBox = new JComboBox<>(TYPE_LABELS);
		if (!insert) {
			for (int i = 0; i < TYPE_VALUES.length; i++) {
				if (TYPE_VALUES[i] == examParameter.getExamType()) {
					typeComboBox.setSelectedIndex(i);
				}
			}
		}
		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.type.txt") + ':'));
		dataPanel.add(typeComboBox);

		dataTypeComboBox = new JComboBox<>(PregnancyExamDataType.values());
		if (!insert) {
			dataTypeComboBox.setSelectedItem(examParameter.getDataType());
		}
		dataTypeComboBox.addActionListener(e -> showDataTypeDetail((PregnancyExamDataType) dataTypeComboBox.getSelectedItem()));
		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.datatype.txt") + ':'));
		dataPanel.add(dataTypeComboBox);

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.unit.txt") + ':'));
		unitTextField = new JTextField(10);
		if (!insert && examParameter.getUnit() != null) {
			unitTextField.setText(examParameter.getUnit());
		}
		dataPanel.add(unitTextField);

		dataPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.dataconstraint.txt") + ':'));
		dataPanel.add(getDataTypeDetailPanel());

		SpringUtilities.makeCompactGrid(dataPanel, 6, 2, 5, 5, 5, 5);

		if (!insert) {
			showDataTypeDetail(examParameter.getDataType());
		} else {
			showDataTypeDetail(PregnancyExamDataType.TEXT);
		}

		return dataPanel;
	}

	private JPanel getDataTypeDetailPanel() {
		dataTypeDetailLayout = new CardLayout();
		dataTypeDetailPanel = new JPanel(dataTypeDetailLayout);

		maxValueTextField = new VoDoubleTextField(0, 5);
		if (!insert && examParameter.getMaxValue() != null) {
			maxValueTextField.setText(String.valueOf(examParameter.getMaxValue()));
		}
		JPanel numericPanel = new JPanel();
		numericPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.maxvalue.txt") + ':'));
		numericPanel.add(maxValueTextField);

		allowedValuesTextField = new JTextField(20);
		if (!insert && examParameter.getAllowedValues() != null) {
			allowedValuesTextField.setText(examParameter.getAllowedValues());
		}
		JPanel enumPanel = new JPanel();
		enumPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.allowedvaluessemicolon.txt")));
		enumPanel.add(allowedValuesTextField);

		defaultBooleanCheckBox = new JCheckBox(MessageBundle.getMessage("angal.cpn.defaulttrue.txt"));
		if (!insert) {
			defaultBooleanCheckBox.setSelected("true".equalsIgnoreCase(examParameter.getDefaultValue()));
		}
		JPanel booleanPanel = new JPanel();
		booleanPanel.add(defaultBooleanCheckBox);

		JPanel textPanel = new JPanel();
		textPanel.add(new JLabel(MessageBundle.getMessage("angal.cpn.notextconstraint.txt")));

		dataTypeDetailPanel.add(textPanel, PregnancyExamDataType.TEXT.name());
		dataTypeDetailPanel.add(numericPanel, PregnancyExamDataType.NUMERIC.name());
		dataTypeDetailPanel.add(enumPanel, PregnancyExamDataType.ENUM.name());
		dataTypeDetailPanel.add(booleanPanel, PregnancyExamDataType.BOOLEAN.name());

		return dataTypeDetailPanel;
	}

	private void showDataTypeDetail(PregnancyExamDataType dataType) {
		dataTypeDetailLayout.show(dataTypeDetailPanel, dataType.name());
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getOkButton());
		buttonPanel.add(getCancelButton());
		return buttonPanel;
	}

	private JButton getCancelButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.addActionListener(actionEvent -> dispose());
		return cancelButton;
	}

	private JButton getOkButton() {
		JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		okButton.addActionListener(actionEvent -> {
			examParameter.setCode(codeTextField.getText());
			examParameter.setDescription(descriptionTextField.getText());
			examParameter.setExamType(TYPE_VALUES[typeComboBox.getSelectedIndex()]);
			PregnancyExamDataType dataType = (PregnancyExamDataType) dataTypeComboBox.getSelectedItem();
			examParameter.setDataType(dataType);
			examParameter.setUnit(unitTextField.getText());
			examParameter.setMaxValue(null);
			examParameter.setAllowedValues(null);
			examParameter.setDefaultValue(null);
			switch (dataType) {
				case NUMERIC:
					try {
						examParameter.setMaxValue(Double.valueOf(maxValueTextField.getText()));
					} catch (NumberFormatException nfe) {
						MessageDialog.error(this, "angal.cpn.pleaseinsertavalidmaxvalue.msg");
						return;
					}
					break;
				case ENUM:
					examParameter.setAllowedValues(allowedValuesTextField.getText());
					break;
				case BOOLEAN:
					examParameter.setDefaultValue(String.valueOf(defaultBooleanCheckBox.isSelected()));
					break;
				case TEXT:
				default:
					break;
			}
			try {
				if (insert) {
					examParameterManager.newPregnancyExamParameter(examParameter);
					fireInserted();
				} else {
					examParameterManager.updatePregnancyExamParameter(examParameter);
					fireUpdated();
				}
				dispose();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		});
		return okButton;
	}
}
