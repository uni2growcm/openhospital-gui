package org.isf.typology;

import org.isf.generaldata.MessageBundle;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.EventListener;

public class TypologyEdit extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EventListenerList typologyListeners = new EventListenerList();

    public interface TypologyListener extends EventListener {
        void typologyUpdated(AWTEvent e);
        void typologyInserted(AWTEvent e);
    }

    public void addTypologyListener(TypologyListener l) {
        typologyListeners.add(TypologyListener.class, l);
    }

    private void fireInserted() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {};
        for (TypologyListener l : typologyListeners.getListeners(TypologyListener.class)) {
            l.typologyInserted(event);
        }
    }

    private void fireUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {};
        for (TypologyListener l : typologyListeners.getListeners(TypologyListener.class)) {
            l.typologyUpdated(event);
        }
    }

    private final TypologyBrowserManager manager =
            Context.getApplicationContext().getBean(TypologyBrowserManager.class);

    private JPanel content;

    private JTextField descriptionField;
    private VoLimitedTextField codeField;
    private JComboBox<Family> familyCombo;

    private final Typology typology;
    private final boolean insert;
    private String lastDescription;

    public TypologyEdit(JFrame owner, Typology typology, boolean insert) {
        super(owner, true);
        this.typology = typology;
        this.insert = insert;
        this.lastDescription = typology.getDescription();
        initialize();
    }

    private void initialize() {
        setContentPane(getContent());
        setTitle(insert
                ? MessageBundle.getMessage("angal.typology.new.title")
                : MessageBundle.getMessage("angal.typology.edit.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel getContent() {
        if (content == null) {
            content = new JPanel(new BorderLayout());
            content.add(getDataPanel(), BorderLayout.CENTER);
            content.add(getButtonPanel(), BorderLayout.SOUTH);
        }
        return content;
    }

    private JPanel getDataPanel() {
        JPanel panel = new JPanel(new SpringLayout());

        panel.add(new JLabel(MessageBundle.getMessage("angal.typology.code.label") + "* :"));
        panel.add(getCodeField());

        panel.add(new JLabel(MessageBundle.getMessage("angal.typology.description.label") + "* :"));
        panel.add(getDescriptionField());

        panel.add(new JLabel(MessageBundle.getMessage("angal.typology.family.label") + "* :"));
        panel.add(getFamilyCombo());

        SpringUtilities.makeCompactGrid(panel, 3, 2, 5, 5, 5, 5);
        return panel;
    }

    private JComboBox<Family> getFamilyCombo() {
        if (familyCombo == null) {
            familyCombo = new JComboBox<>(Family.values());

            if (!insert && typology.getFamily() != null) {
                familyCombo.setSelectedItem(typology.getFamily());
            } else {
                familyCombo.setSelectedItem(Family.DELIVERYTYPE);
            }
        }
        return familyCombo;
    }

    // ---------------- BUTTONS ----------------

    private JPanel getButtonPanel() {
        JPanel panel = new JPanel();

        JButton ok = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
        ok.addActionListener(e -> onSave());

        JButton cancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        cancel.addActionListener(e -> dispose());

        panel.add(ok);
        panel.add(cancel);
        return panel;
    }

    // ---------------- FIELDS ----------------

    private JTextField getDescriptionField() {
        if (descriptionField == null) {
            descriptionField = new JTextField(20);
            if (!insert) {
                descriptionField.setText(typology.getDescription());
            }
        }
        return descriptionField;
    }

    private JTextField getCodeField() {
        if (codeField == null) {
            codeField = new VoLimitedTextField(20);
            if (!insert) {
                codeField.setText(typology.getCode());
                codeField.setEnabled(false);
            }
        }
        return codeField;
    }

    // ---------------- SAVE ----------------

    private void onSave() {
        try {
            typology.setCode(codeField.getText().trim().toUpperCase());
            typology.setDescription(descriptionField.getText().trim());
            typology.setFamily((Family) familyCombo.getSelectedItem());

            if (insert) {
                manager.newTypology(typology);
                fireInserted();
            } else {
                if (!descriptionField.getText().equals(lastDescription)) {
                    manager.updateTypology(typology);
                    fireUpdated();
                }
            }

            dispose();

        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
        }
    }
}