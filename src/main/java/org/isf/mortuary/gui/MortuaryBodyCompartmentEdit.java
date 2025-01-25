package org.isf.mortuary.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.BodyCompartmentManager;
import org.isf.mortuary.model.BodyCompartment;
import org.isf.mortuarystays.manager.MortuaryStayManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class MortuaryBodyCompartmentEdit extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
	private EventListenerList bodyCompartmentListeners = new EventListenerList();

	public interface BodyCompartmentListener extends EventListener {

		void bodyCompartmentUpdated(AWTEvent e);

		void bodyCompartmentInserted(AWTEvent e);
	}

	public void addBodyCompartmentListener(MortuaryBodyCompartmentEdit.BodyCompartmentListener l) {
		bodyCompartmentListeners.add(MortuaryBodyCompartmentEdit.BodyCompartmentListener.class, l);
	}

	private void fireMortuaryStaysInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = bodyCompartmentListeners.getListeners(MortuaryBodyCompartmentEdit.BodyCompartmentListener.class);
		for (EventListener listener : listeners) {
			((MortuaryBodyCompartmentEdit.BodyCompartmentListener) listener).bodyCompartmentInserted(event);
		}
	}

	private void fireMortuaryStaysUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			@Serial
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = bodyCompartmentListeners.getListeners(MortuaryBodyCompartmentEdit.BodyCompartmentListener.class);
		for (EventListener listener : listeners) {
			((MortuaryBodyCompartmentEdit.BodyCompartmentListener) listener).bodyCompartmentUpdated(event);
		}
	}

	private JPanel jContentPane;
	private JPanel jDataPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField codeTextField;
	private JTextField descriptionTextField;
	private String code;
	private String desc;
	private boolean insert;
	private BodyCompartment bodyCompartment;

	private BodyCompartmentManager bodyCompartmentManager = Context.getApplicationContext().getBean(BodyCompartmentManager.class);

	public MortuaryBodyCompartmentEdit(JFrame parent, BodyCompartment old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		bodyCompartment = old;
		initialize();
	}

	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.bodycompartment.newmortuarystays.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.mortuary.bodycompartment.editmortuarystays.title"));
		}
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJDataPanel(), BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel();
			GridBagLayout gblDataPanel = new GridBagLayout();
			gblDataPanel.columnWeights = new double[] { 0.0, 1.0 };
			jDataPanel.setLayout(gblDataPanel);
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.codestar"));
			GridBagConstraints gbcCodeLabel = new GridBagConstraints();
			gbcCodeLabel.anchor = GridBagConstraints.WEST;
			gbcCodeLabel.insets = new Insets(0, 0, 5, 5);
			gbcCodeLabel.gridx = 0;
			gbcCodeLabel.gridy = 0;
			jDataPanel.add(codeLabel, gbcCodeLabel);
			GridBagConstraints gbcCodeTextField = new GridBagConstraints();
			gbcCodeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcCodeTextField.insets = new Insets(0, 0, 5, 0);
			gbcCodeTextField.gridx = 1;
			gbcCodeTextField.gridy = 0;
			jDataPanel.add(getCodeTextField(), gbcCodeTextField);

			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.mortuarystays.descriptionedit.txt"));
			GridBagConstraints gbcDescLabel = new GridBagConstraints();
			gbcDescLabel.anchor = GridBagConstraints.WEST;
			gbcDescLabel.insets = new Insets(0, 0, 5, 5);
			gbcDescLabel.gridx = 0;
			gbcDescLabel.gridy = 1;
			jDataPanel.add(descLabel, gbcDescLabel);
			GridBagConstraints gbcDescriptionTextField = new GridBagConstraints();
			gbcDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcDescriptionTextField.insets = new Insets(0, 0, 5, 0);
			gbcDescriptionTextField.gridx = 1;
			gbcDescriptionTextField.gridy = 1;
			jDataPanel.add(getDescriptionTextField(), gbcDescriptionTextField);

		}
		return jDataPanel;
	}

	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new VoLimitedTextField(11, 20);
			if (!insert) {
				codeTextField.setText(bodyCompartment.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			descriptionTextField = new VoLimitedTextField(50);
			if (!insert) {
				descriptionTextField.setText(bodyCompartment.getDescription());
			}
		}
		return descriptionTextField;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getOkButton());
		buttonPanel.add(getCancelButton());
		return buttonPanel;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				if (insert) {
					code = codeTextField.getText().trim();
					if (code.isEmpty()) {
						MessageDialog.error(this, "angal.common.pleaseinsertacode.msg");
						return;
					}
					if (code.length() > 6) {
						MessageDialog.error(this, "angal.common.thecodeistoolongmax1char.msg");
						return;
					}
					try {
						if (bodyCompartmentManager.isCodePresent(code)) {
							MessageDialog.error(this, "angal.mortuarystays.codealreadyinuse.msg");
							return;
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
				desc = descriptionTextField.getText().trim();
				if (desc.isEmpty()) {
					MessageDialog.error(this, "angal.common.pleaseinsertavaliddescription.msg");
					return;
				}

				bodyCompartment.setDescription(desc);
				bodyCompartment.setCode(codeTextField.getText());

				boolean result = false;
				BodyCompartment savedBodyCompartment;
				if (insert) {
					try {
						savedBodyCompartment = bodyCompartmentManager.add(bodyCompartment);
						result = (savedBodyCompartment != null);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					if (result) {
						fireMortuaryStaysInserted();
					}
				} else {
					try {
						savedBodyCompartment = bodyCompartmentManager.update(bodyCompartment);
						result = (savedBodyCompartment != null);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					if (result) {
						fireMortuaryStaysUpdated();
					}
				}
				if (!result) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				} else {
					dispose();
				}
			});
		}
		return okButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}
}