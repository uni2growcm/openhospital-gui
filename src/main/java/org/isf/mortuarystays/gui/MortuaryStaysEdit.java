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
package org.isf.mortuarystays.gui;


import java.util.EventListener;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuarystays.manager.MortuaryStayManager;
import org.isf.mortuarystays.model.MortuaryStay;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class MortuaryStaysEdit extends JDialog {
    private static final long serialVersionUID = 1L;
    private EventListenerList mortuaryStaysListeners = new EventListenerList();

    public interface MortuaryStaysListener extends EventListener {

        void mortuaryStaysUpdated(AWTEvent e);

        void mortuaryStaysInserted(AWTEvent e);
    }

    public void addMortuaryStaysListener(MortuaryStaysListener l) {
        mortuaryStaysListeners.add(MortuaryStaysListener.class, l);
    }

    public void removeMortuaryStaysListener(MortuaryStaysListener listener) {
        mortuaryStaysListeners.add(MortuaryStaysListener.class, listener);
    }

    private void fireMortuaryStaysInserted() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

            private static final long serialVersionUID = 1L;
        };

        EventListener[] listeners = mortuaryStaysListeners.getListeners(MortuaryStaysEdit.MortuaryStaysListener.class);
        for (EventListener listener : listeners) {
            ((MortuaryStaysEdit.MortuaryStaysListener) listener).mortuaryStaysInserted(event);
        }
    }

    private void fireMortuaryStaysUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

            private static final long serialVersionUID = 1L;
        };

        EventListener[] listeners = mortuaryStaysListeners.getListeners(MortuaryStaysEdit.MortuaryStaysListener.class);
        for (EventListener listener : listeners) {
            ((MortuaryStaysEdit.MortuaryStaysListener) listener).mortuaryStaysUpdated(event);
        }
    }

    private MortuaryStayManager mortuaryStayManager = Context.getApplicationContext().getBean(MortuaryStayManager.class);

    private JPanel jContentPane;
    private JPanel dataPanel;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField codeTextField;
    private JTextField maxDTextField;
    private JTextField minDTextField;
    private MortuaryStay mortuaryStay;
    private boolean insert;
    private String name;
    private String code;
    private String desc;
    private int maxD;
    private int minD;


    /**
     * This is the default constructor; we pass the parent frame
     * (because it is a jdialog), the arraylist and the selected
     * row because we need to update them
     */
    public MortuaryStaysEdit(JFrame parent, MortuaryStay old, boolean inserting) {
        super(parent, true);
        insert = inserting;
        mortuaryStay = old;
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setContentPane(getJContentPane());
        if (insert) {
            this.setTitle(MessageBundle.getMessage("angal.mortuarystays.newmortuarystays.title"));
        } else {
            this.setTitle(MessageBundle.getMessage("angal.mortuarystays.editmortuarystays.title"));
        }
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * This method initializes jContentPane
     *
     * @return JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getDataPanel(), BorderLayout.CENTER);
            jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes dataPanel
     *
     * @return JPanel
     */
    private JPanel getDataPanel() {
        if (dataPanel == null) {
            dataPanel = new JPanel();
            GridBagLayout gblDataPanel = new GridBagLayout();
            gblDataPanel.columnWeights = new double[] { 0.0, 1.0 };
            dataPanel.setLayout(gblDataPanel);
            JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.codestar"));
            GridBagConstraints gbcCodeLabel = new GridBagConstraints();
            gbcCodeLabel.anchor = GridBagConstraints.WEST;
            gbcCodeLabel.insets = new Insets(0, 0, 5, 5);
            gbcCodeLabel.gridx = 0;
            gbcCodeLabel.gridy = 0;
            dataPanel.add(codeLabel, gbcCodeLabel);
            GridBagConstraints gbcCodeTextField = new GridBagConstraints();
            gbcCodeTextField.fill = GridBagConstraints.HORIZONTAL;
            gbcCodeTextField.insets = new Insets(0, 0, 5, 0);
            gbcCodeTextField.gridx = 1;
            gbcCodeTextField.gridy = 0;
            dataPanel.add(getCodeTextField(), gbcCodeTextField);

            JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.mortuarystays.nameedit.txt"));
            GridBagConstraints gbcNameLabel = new GridBagConstraints();
            gbcNameLabel.anchor = GridBagConstraints.WEST;
            gbcNameLabel.insets = new Insets(0, 0, 5, 5);
            gbcNameLabel.gridx = 0;
            gbcNameLabel.gridy = 1;
            dataPanel.add(nameLabel, gbcNameLabel);
            GridBagConstraints gbcNameTextField = new GridBagConstraints();
            gbcNameTextField.fill = GridBagConstraints.HORIZONTAL;
            gbcNameTextField.insets = new Insets(0, 0, 5, 0);
            gbcNameTextField.gridx = 1;
            gbcNameTextField.gridy = 1;
            dataPanel.add(getNameTextField(), gbcNameTextField);

            JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.mortuarystays.descriptionedit.txt"));
            GridBagConstraints gbcDescLabel = new GridBagConstraints();
            gbcDescLabel.anchor = GridBagConstraints.WEST;
            gbcDescLabel.insets = new Insets(0, 0, 5, 5);
            gbcDescLabel.gridx = 0;
            gbcDescLabel.gridy = 2;
            dataPanel.add(descLabel, gbcDescLabel);
            GridBagConstraints gbcDescriptionTextField = new GridBagConstraints();
            gbcDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
            gbcDescriptionTextField.insets = new Insets(0, 0, 5, 0);
            gbcDescriptionTextField.gridx = 1;
            gbcDescriptionTextField.gridy = 2;
            dataPanel.add(getDescriptionTextField(), gbcDescriptionTextField);

            GridBagConstraints gbcMinDTextField = new GridBagConstraints();
            gbcMinDTextField.insets = new Insets(0, 0, 5, 0);
            gbcMinDTextField.gridx = 1;
            gbcMinDTextField.gridy = 3;
            dataPanel.add(getMinDTextField(), gbcMinDTextField);
            JLabel minDLabel = new JLabel(MessageBundle.getMessage("angal.mortuarystays.mindays.txt"));
            GridBagConstraints gbcMinDLabel = new GridBagConstraints();
            gbcMinDLabel.anchor = GridBagConstraints.WEST;
            gbcMinDLabel.insets = new Insets(0, 0, 5, 5);
            gbcMinDLabel.gridx = 0;
            gbcMinDLabel.gridy = 3;
            dataPanel.add(minDLabel, gbcMinDLabel);

            JLabel maxDLabel = new JLabel(MessageBundle.getMessage("angal.mortuarystays.maxdays.txt"));
            GridBagConstraints gbcMaxDLabel = new GridBagConstraints();
            gbcMaxDLabel.anchor = GridBagConstraints.WEST;
            gbcMaxDLabel.insets = new Insets(0, 0, 5, 5);
            gbcMaxDLabel.gridx = 0;
            gbcMaxDLabel.gridy = 4;
            dataPanel.add(maxDLabel, gbcMaxDLabel);
            GridBagConstraints gbcMaxDTextField = new GridBagConstraints();
            gbcMaxDTextField.fill = GridBagConstraints.HORIZONTAL;
            gbcMaxDTextField.insets = new Insets(0, 0, 5, 0);
            gbcMaxDTextField.gridx = 1;
            gbcMaxDTextField.gridy = 4;
            dataPanel.add(getMaxDTextField(), gbcMaxDTextField);

            JLabel requiredLabel = new JLabel(MessageBundle.getMessage("angal.mortuarystays.requiredfields.txt"));
            GridBagConstraints gbcRequiredLabel = new GridBagConstraints();
            gbcRequiredLabel.gridwidth = 2;
            gbcRequiredLabel.anchor = GridBagConstraints.EAST;
            gbcRequiredLabel.gridx = 0;
            gbcRequiredLabel.gridy = 5;
            dataPanel.add(requiredLabel, gbcRequiredLabel);
        }
        return dataPanel;
    }

    /**
     * This method initializes buttonPanel
     *
     * @return JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.add(getOkButton());
            buttonPanel.add(getCancelButton());
        }
        return buttonPanel;
    }

    /**
     * This method initializes cancelButton
     *
     * @return JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
            cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
            cancelButton.addActionListener(actionEvent -> dispose());
        }
        return cancelButton;
    }

    /**
     * This method initializes okButton
     *
     * @return JButton
     */
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
                    if (code.length() > 11) {
                        MessageDialog.error(this, "angal.common.thecodeistoolongmax1char.msg");
                        return;
                    }
                    try {
                        if (mortuaryStayManager.isCodePresent(code)) {
                            MessageDialog.error(this, "angal.common.codealreadyinuse.msg");
                            return;
                        }
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                }
                name = nameTextField.getText().trim();
                if (name.isEmpty()) {
                    MessageDialog.error(this, "angal.common.pleaseinsertavalidname.msg");
                    return;
                }
                desc = descriptionTextField.getText().trim();
                if (desc.isEmpty()) {
                    MessageDialog.error(this, "angal.common.pleaseinsertavaliddescription.msg");
                    return;
                }
                try {
                    minD = Integer.parseInt(minDTextField.getText());
                } catch (NumberFormatException f) {
                    MessageDialog.error(this, "angal.mortuarystays.insertavalidmindnumber.msg");
                    return;
                }
                if (minD < 0) {
                    MessageDialog.error(this, "angal.mortuarystays.insertavalidmindnumber.msg");
                    return;
                }
                try {
                    maxD = Integer.parseInt(maxDTextField.getText());
                } catch (NumberFormatException f) {
                    MessageDialog.error(this, "angal.mortuarystays.insertavalidmaxdnumber.msg");
                    return;
                }
                if (maxD < 0) {
                    MessageDialog.error(this, "angal.mortuarystays.insertavalidmaxdnumber.msg");
                    return;
                }
                if (minD > maxD) {
                    MessageDialog.error(this, "angal.mortuarystays.insertcoherencemaxminvalues.msg");
                    return;
                }

                mortuaryStay.setDescription(desc);
                mortuaryStay.setName(name);
                mortuaryStay.setCode(codeTextField.getText());
                mortuaryStay.setMinDays(minD);
                mortuaryStay.setMaxDays(maxD);

                boolean result = false;
                MortuaryStay savedMortuaryStay;
                if (insert) { // inserting
                    try {
                        savedMortuaryStay = mortuaryStayManager.add(mortuaryStay);
                        result = (savedMortuaryStay != null);
                    } catch (OHServiceException ex) {
                        OHServiceExceptionUtil.showMessages(ex);
                    }
                    if (result) {
                        fireMortuaryStaysInserted();
                    }
                } else {
                    try { // updating
                        savedMortuaryStay = mortuaryStayManager.update(mortuaryStay);
                        result = (savedMortuaryStay != null);
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

    /**
     * This method initializes nameTextField
     *
     * @return JTextField
     */
    private JTextField getNameTextField() {
        if (nameTextField == null) {
            nameTextField = new VoLimitedTextField(50);
            if (!insert) {
                nameTextField.setText(mortuaryStay.getName());
            }
        }
        return nameTextField;
    }

    /**
     * This method initializes descriptionTextField
     *
     * @return JTextField
     */
    private JTextField getDescriptionTextField() {
        if (descriptionTextField == null) {
            descriptionTextField = new VoLimitedTextField(50);
            if (!insert) {
                descriptionTextField.setText(mortuaryStay.getDescription());
            }
        }
        return descriptionTextField;
    }

    /**
     * This method initializes codeTextField
     *
     * @return JTextField
     */
    private JTextField getCodeTextField() {
        if (codeTextField == null) {
            codeTextField = new VoLimitedTextField(11, 20);
            if (!insert) {
                codeTextField.setText(mortuaryStay.getCode());
                codeTextField.setEnabled(false);
            }
        }
        return codeTextField;
    }

    /**
     * This method initializes telTextField
     *
     * @return JTextField
     */
    private JTextField getMinDTextField() {
        if (minDTextField == null) {
            minDTextField = new VoLimitedTextField(50, 20);
            if (!insert) {
                minDTextField.setText(String.valueOf(mortuaryStay.getMinDays()));
            }
        }
        return minDTextField;
    }

    /**
     * This method initializes faxTextField
     *
     * @return JTextField
     */
    private JTextField getMaxDTextField() {
        if (maxDTextField == null) {
            maxDTextField = new VoLimitedTextField(50, 20);
            if (!insert) {
                maxDTextField.setText(String.valueOf(mortuaryStay.getMaxDays()));
            }
        }
        return maxDTextField;
    }
}