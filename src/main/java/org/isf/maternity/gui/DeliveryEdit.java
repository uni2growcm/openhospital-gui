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
package org.isf.maternity.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.NewBornBrowserManager;
import org.isf.maternity.manager.PregnancyDeliveryBrowserManager;
import org.isf.maternity.model.CryTime;
import org.isf.maternity.model.DeliveryMode;
import org.isf.maternity.model.HivStatus;
import org.isf.maternity.model.NeonatalStatus;
import org.isf.maternity.model.Newborn;
import org.isf.maternity.model.PerinealIntegrity;
import org.isf.maternity.model.Pregnancy;
import org.isf.maternity.model.PregnancyDelivery;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class DeliveryEdit extends ModalJFrame {

    private final Pregnancy pregnancy;
    private PregnancyDelivery delivery;
    private final PregnancyDeliveryBrowserManager deliveryManager = Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);
    private final NewBornBrowserManager newbornManager = Context.getApplicationContext().getBean(NewBornBrowserManager.class);
    private final PatientBrowserManager patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);

    private JComboBox deliveryTypeCombo;
    private GoodDateTimeSpinnerChooser deliveryDateField;
    private JTextField anesthesiaField;
    private JComboBox perinealCombo;
    private JCheckBox placentaCompleteCheck;
    private JTextField clinicianField;
    private JTextField fatherName, fatherPhone, fatherAddress;
    private JTextField fatherAge, fatherBirthplace, fatherProfession;
    private JCheckBox fatherAliveCheck, motherAliveCheck;
    private JComboBox deliveryModeCombo;
    private JTextField feedingModeField;
    private JTextField lochiaField;
    private JTextArea noteArea;
    private JTable newbornTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScroll;
    private java.util.Map<Integer, Newborn> existingNewborns = new java.util.HashMap<>();

    public DeliveryEdit(JFrame owner, Pregnancy pregnancy) {
        this.pregnancy = pregnancy;
        init();
        setLocationRelativeTo(owner);
    }

    private void init() {
        loadOrCreateDelivery();
        setTitle(MessageBundle.getMessage("angal.maternity.delivery.title.txt"));
        setSize(1600, 850);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(topPanel(), BorderLayout.NORTH);
        add(centerPanel(), BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.SOUTH);
        loadDeliveryData();
        resizeTable();
    }

    private void loadOrCreateDelivery() {
        try {
            PregnancyDelivery existing = deliveryManager.getDeliveryByPregnancy(pregnancy.getId());
            if (existing != null) {
                delivery = existing;
            } else {
                delivery = new PregnancyDelivery();
                delivery.setPregnancy(pregnancy);
            }
        } catch (Exception e) {
            MessageDialog.error(this, e.getMessage());
        }
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.delivery.info.txt")));
        deliveryDateField = new GoodDateTimeSpinnerChooser(LocalDateTime.now());
        deliveryDateField.setMaxDate(LocalDate.now());
        deliveryTypeCombo = new JComboBox<>();
        deliveryTypeCombo.setPreferredSize(new Dimension(deliveryDateField.getPreferredSize().width, deliveryDateField.getPreferredSize().height));
        try {
            Context.getApplicationContext().getBean(TypologyBrowserManager.class).getTypologies(Family.DELIVERYTYPE).forEach(deliveryTypeCombo::addItem);
        } catch (Exception ignored) {
        }
        panel.add(row(MessageBundle.getMessage("angal.maternity.delivery.date.label"), deliveryDateField));
        panel.add(row(MessageBundle.getMessage("angal.maternity.delivery.type.label"), deliveryTypeCombo));
        return panel;
    }

    private JPanel centerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel fatherOtherPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        fatherOtherPanel.add(fatherPanel());
        fatherOtherPanel.add(otherInfoPanel());
        panel.add(fatherOtherPanel);
        panel.add(newbornPanel());
        return panel;
    }

    private JPanel fatherPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.delivery.father.txt")));

        GridBagConstraints gbc = new GridBagConstraints();

        fatherName = new JTextField(50);
        fatherAge = new JTextField(50);
        fatherPhone = new JTextField(50);
        fatherBirthplace = new JTextField(50);
        fatherAddress = new JTextField(50);
        fatherProfession = new JTextField(50);
        fatherAliveCheck = new JCheckBox(MessageBundle.getMessage("angal.maternity.delivery.fatheralive.label"), true);
        motherAliveCheck = new JCheckBox(MessageBundle.getMessage("angal.maternity.delivery.motheralive.label"), true);

        int row = 0;
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.fathername.label"), fatherName);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.fatherage.label"), fatherAge);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.fatherphone.label"), fatherPhone);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.fatherbirthplace.label"), fatherBirthplace);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.address.label"), fatherAddress);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.fatherprofession.label"), fatherProfession);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(8, 4, 4, 4);
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checks.add(fatherAliveCheck);
        checks.add(motherAliveCheck);
        panel.add(checks, gbc);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int rowIndex, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = rowIndex;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 4);
        panel.add(field, gbc);
    }

    private void addNoteRow(JPanel panel, GridBagConstraints gbc, int rowIndex, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = rowIndex;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(4, 4, 4, 8);
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(4, 0, 4, 4);
        panel.add(field, gbc);
    }

    private JPanel otherInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.delivery.other.txt")));

        GridBagConstraints gbc = new GridBagConstraints();

        anesthesiaField = new JTextField(50);
        perinealCombo = new JComboBox<>(PerinealIntegrity.values());
        deliveryModeCombo = new JComboBox<>();
        for (DeliveryMode mode : DeliveryMode.values()) {
            deliveryModeCombo.addItem(mode.name());
        }
        perinealCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PerinealIntegrity pi) {
                    setText(MessageBundle.getMessage(pi.getKey()));
                }
                return this;
            }
        });
        placentaCompleteCheck = new JCheckBox();
        clinicianField = new JTextField(50);
        JTextField indicationField = new JTextField(50);
        JTextField laborOnsetField = new JTextField(50);
        JTextField romField = new JTextField(50);
        JTextField placentaWeightField = new JTextField(50);
        JTextField bloodLossField = new JTextField(50);

        feedingModeField = new JTextField(50);
        lochiaField = new JTextField(50);
        noteArea = new JTextArea(4, 38);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);

        int row = 0;
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.mode.label"), deliveryModeCombo);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.indication.label"), indicationField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.laboronset.label"), laborOnsetField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.rom.label"), romField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.anasthesia.label"), anesthesiaField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.perineal.label"), perinealCombo);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.placentacomplete.label"), placentaCompleteCheck);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.placentaweight.label"), placentaWeightField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.estimatedbloodloss.label"), bloodLossField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.clinician.label"), clinicianField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.feedingmode.label"), feedingModeField);
        addRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.lochia.label"), lochiaField);
        addNoteRow(panel, gbc, row++, MessageBundle.getMessage("angal.maternity.delivery.note.label"), noteScroll);

        return panel;
    }

    private JPanel newbornPanel() {
        JPanel newbornPanel = new JPanel(new BorderLayout());
        newbornPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.maternity.delivery.newborns.txt")));
        String[] cols = {
                MessageBundle.getMessage("angal.maternity.delivery.firstname.col"),
                MessageBundle.getMessage("angal.maternity.delivery.lastname.col"),
                MessageBundle.getMessage("angal.maternity.delivery.sex.col"),
                MessageBundle.getMessage("angal.maternity.delivery.birthorder.col"),
                MessageBundle.getMessage("angal.maternity.delivery.birthdate.col"),
                MessageBundle.getMessage("angal.maternity.delivery.weight.col"),
                MessageBundle.getMessage("angal.maternity.delivery.birthlength.col"),
                MessageBundle.getMessage("angal.maternity.delivery.headcircumference.col"),
                MessageBundle.getMessage("angal.maternity.delivery.apgarscore1min.col"),
                MessageBundle.getMessage("angal.maternity.delivery.apgarscore5min.col"),
                MessageBundle.getMessage("angal.maternity.delivery.crytime.col"),
                MessageBundle.getMessage("angal.maternity.delivery.resuscitation.col"),
                MessageBundle.getMessage("angal.maternity.delivery.congenitalanomalies.col"),
                MessageBundle.getMessage("angal.maternity.delivery.complication.col"),
                MessageBundle.getMessage("angal.maternity.delivery.neonatalstatus.col"),
                MessageBundle.getMessage("angal.maternity.delivery.hiv.col")
        };

        tableModel = new DefaultTableModel(cols, 1);
        newbornTable = new JTable(tableModel);
        newbornTable.setRowHeight(40);
        tableScroll = new JScrollPane(newbornTable);

        setupEditors();
        JButton add = new JButton("+");
        JButton remove = new JButton("-");
        add.addActionListener(e -> {
            tableModel.addRow(new Object[cols.length]);
            resizeTable();
        });
        remove.addActionListener(e -> {
            int r = newbornTable.getSelectedRow();
            if (r >= 0) {
                tableModel.removeRow(r);
                resizeTable();
            }
        });
        JPanel addRemoveButtonPanel = new JPanel();
        addRemoveButtonPanel.add(add);
        addRemoveButtonPanel.add(remove);
        newbornPanel.add(tableScroll, BorderLayout.CENTER);
        newbornPanel.add(addRemoveButtonPanel, BorderLayout.SOUTH);
        return newbornPanel;
    }

    private void setupEditors() {

        newbornTable.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(
                    new JComboBox<>(new String[]{
                        MessageBundle.getMessage("angal.common.male.txt"),
                        MessageBundle.getMessage("angal.common.female.txt")
                    })
                ));

        newbornTable.getColumnModel().getColumn(4).setCellEditor(new DateTimeCellEditor());
        newbornTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(createNumberSpinner(0, 10)));
        newbornTable.getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(createNumberSpinner(0, 10)));
        newbornTable.getColumnModel().getColumn(10).setCellEditor(new DefaultCellEditor(createCryTimeCombo()));

        newbornTable.getColumnModel().getColumn(11).setCellEditor(
            new DefaultCellEditor(new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.common.yes.txt"),
                MessageBundle.getMessage("angal.common.no.txt")
            }))
        );

        newbornTable.getColumnModel().getColumn(14).setCellEditor(new DefaultCellEditor(createNeonatalStatusCombo()));
        newbornTable.getColumnModel().getColumn(15).setCellEditor(new DefaultCellEditor(createHivStatusCombo()));
    }

    private JComboBox<String> createCryTimeCombo() {
        JComboBox<String> combo = new JComboBox<>();
        for (CryTime value : CryTime.values()) {
            combo.addItem(MessageBundle.getMessage(value.getKey()));
        }
        return combo;
    }

    private JComboBox<String> createNeonatalStatusCombo() {
        JComboBox<String> combo = new JComboBox<>();
        for (NeonatalStatus value : NeonatalStatus.values()) {
            combo.addItem(MessageBundle.getMessage(value.getKey()));
        }
        return combo;
    }

    private JComboBox<String> createHivStatusCombo() {
        JComboBox<String> combo = new JComboBox<>();
        for (HivStatus value : HivStatus.values()) {
            combo.addItem(MessageBundle.getMessage(value.getKey()));
        }
        return combo;
    }

    private JComboBox<Integer> createNumberSpinner(int min, int max) {
        JComboBox<Integer> combo = new JComboBox<>();
        for (int i = min; i <= max; i++) {
            combo.addItem(i);
        }
        return combo;
    }

    private void resizeTable() {
        int rows = tableModel.getRowCount();
        int tableHeight = newbornTable.getTableHeader().getHeight() + (rows * newbornTable.getRowHeight());
        int maxHeight = 300;
        int preferredHeight = Math.min(Math.max(tableHeight, 150), maxHeight);
        tableScroll.setPreferredSize(new Dimension(1400, preferredHeight));
        pack();
    }

    private void save() {
        try {
            if (!validateDelivery()) return;

            if (tableModel.getRowCount() == 0) {
                MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.atleastonenewborn.msg"));
                return;
            }

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (!validateNewbornRow(i)) return;
            }

            delivery.setDeliveryDate(deliveryDateField.getLocalDateTime());
            delivery.setDeliveryType((Typology) deliveryTypeCombo.getSelectedItem());
            delivery.setFatherName(fatherName.getText());
            
            if (!fatherAge.getText().isEmpty()) {
                try {
                    delivery.setFatherAge(Integer.parseInt(fatherAge.getText()));
                } catch (NumberFormatException e) {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.maternity.delivery.invalidfatherage.msg"));
                    return;
                }
            }
            delivery.setFatherBirthplace(fatherBirthplace.getText());
            delivery.setFatherProfession(fatherProfession.getText());
            delivery.setFatherAddress(fatherAddress.getText());
            delivery.setAnesthesiaUsed(anesthesiaField.getText());
            delivery.setPerinealIntegrity((PerinealIntegrity) perinealCombo.getSelectedItem());
            delivery.setPlacentaComplete(placentaCompleteCheck.isSelected());
            delivery.setDeliveryMode(DeliveryMode.valueOf((String) deliveryModeCombo.getSelectedItem()));
            delivery.setAttendingClinicianId(clinicianField.getText());
            delivery.setFeedingMode(feedingModeField.getText());
            delivery.setLochia(lochiaField.getText());
            delivery.setNote(noteArea.getText());

            // 4. Save/Update Delivery
            boolean isNew = (delivery.getId() == null);
            if (isNew) {
                deliveryManager.newDelivery(delivery);
            } else {
                deliveryManager.updateDelivery(delivery);
            }

            boolean newbornsSuccess = isNew ? saveNewborns() : updateNewborns();

            if (newbornsSuccess) {
                MessageDialog.info(this, MessageBundle.getMessage(isNew ? "angal.maternity.delivery.deliverysuccessfullycreated" : "angal.maternity.delivery.deliverysuccessfullyupdated.msg"));
                dispose();
            }
        } catch (Exception e) {
            MessageDialog.error(this, e.getMessage());
        }
    }

    private boolean saveNewborns() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Newborn newborn = buildNewborn(i, null);
            if (newborn == null) return false;

            try {
                newbornManager.newNewborn(newborn);
            } catch (OHServiceException e) {
                MessageDialog.error(this, e.getMessage());
                return false;
            }
        }

        return true;
    }

    private boolean updateNewborns() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Newborn existing = existingNewborns.get(i);
            Newborn newborn = buildNewborn(i, existing);
            if (newborn == null) return false;
            try {
                if (newborn.getId() == null) {
                    newbornManager.newNewborn(newborn);
                } else {
                    newbornManager.updateNewborn(newborn);
                }
            } catch (OHServiceException e) {
                MessageDialog.error(this, e.getMessage());
                return false;
            }
        }
        return true;
    }

    private Newborn buildNewborn(int row, Newborn existing) {

        String first = getString(row, 0);
        String last = getString(row, 1);
        String sexStr = getString(row, 2);
        String birthOrder = getString(row, 3);
        LocalDateTime birth = getDateTime(row, 4);
        Double weight = getDouble(row, 5);
        Double length = getDouble(row, 6);
        Double headCirc = getDouble(row, 7);
        Integer apgar1 = getInteger(row, 8);
        Integer apgar5 = getInteger(row, 9);
        String cryTimeStr = getString(row, 10);
        Boolean resuscitation = getBoolean(row, 11);
        String anomalies = getString(row, 12);
        String complication = getString(row, 13);
        String statusStr = getString(row, 14);
        String hivStr = getString(row, 15);

        Patient baby = (existing == null) ? new Patient() : existing.getBabyPatient();

        baby.setFirstName(first);
        baby.setSecondName(last);
        baby.setName(first + " " + last);
        baby.setSex(MessageBundle.getMessage("angal.common.male.txt").equals(sexStr) ? 'M' : 'F');

        if (birth != null) {
            baby.setBirthDate(birth.toLocalDate());
        }

        baby.setAddress(pregnancy.getPatient().getAddress());
        baby.setCity(pregnancy.getPatient().getCity());
        baby.setMotherName(pregnancy.getPatient().getName());
        baby.setFatherName(!fatherName.getText().isEmpty() ? fatherName.getText() : "");
        baby.setMother(motherAliveCheck.isSelected() ? 'A' : 'D');
        baby.setFather(fatherAliveCheck.isSelected() ? 'A' : 'D');

        Newborn nb = (existing == null) ? new Newborn() : existing;
        nb.setBabyPatient(baby);
        nb.setDelivery(delivery);
        nb.setBirthDate(birth);
        nb.setBirthWeight(weight);
        nb.setBirthLength(length);
        nb.setHeadCircumference(headCirc);
        nb.setApgarScore1Min(apgar1);
        nb.setApgarScore5Min(apgar5);
        nb.setCryTime(findCryTimeByDisplayString(cryTimeStr));
        nb.setResuscitationRequired(resuscitation);
        nb.setCongenitalAnomalies(anomalies);
        nb.setComplication(complication);
        nb.setNeonatalStatus(findNeonatalStatusByDisplayString(statusStr));
        nb.setHivStatus(findHivStatusByDisplayString(hivStr));
        nb.setBirthOrder(birthOrder);

        return nb;
    }

    private boolean validateDelivery() {
        if (deliveryDateField.getLocalDateTime() == null) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.daterequired.msg"));
            return false;
        }
        if (deliveryTypeCombo.getSelectedItem() == null) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.typerequired.msg"));
            return false;
        }
        if (deliveryDateField.getLocalDateTime().isAfter(LocalDateTime.now())) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.futuredatenotallowed.msg"));
            return false;
        }
        return true;
    }

    private boolean validateNewbornRow(int row) {
        String first = getString(row, 0);
        String last = getString(row, 1);
        String sex = getString(row, 2);
        LocalDateTime birth = getDateTime(row, 4);
        Double weight = getDouble(row, 5);

        if (first == null || first.isBlank()) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.firstnamerequired.msg"));
            return false;
        }
        if (last == null || last.isBlank()) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.lastnamerequired.msg"));
            return false;
        }
        if (sex == null) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.sexisrequired.msg"));
            return false;
        }
        if (birth == null) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.birthdateisrequired.msg"));
            return false;
        }
        if (weight == null) {
            MessageDialog.warning(this, MessageBundle.getMessage("angal.maternity.delivery.weightisrequired.msg"));
            return false;
        }
        return true;
    }

    private String getString(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        return val != null ? val.toString() : "";
    }

    private Double getDouble(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        if (val instanceof Double d) return d;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return null; }
    }

    private Integer getInteger(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        if (val instanceof Integer i) return i;
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return null; }
    }

    private Boolean getBoolean(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        if (val instanceof Boolean b) return b;
        String s = String.valueOf(val);
        return s.equalsIgnoreCase(MessageBundle.getMessage("angal.common.yes.txt")) || s.equalsIgnoreCase("true");
    }

    private LocalDateTime getDateTime(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        return (val instanceof LocalDateTime ldt) ? ldt : null;
    }

    private JPanel row(String label, Component field) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r.add(new JLabel(label));
        r.add(field);
        return r;
    }

    static class DateTimeCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final GoodDateTimeSpinnerChooser chooser;
        public DateTimeCellEditor() {
            chooser = new GoodDateTimeSpinnerChooser(LocalDateTime.now());
            chooser.setMaxDate(LocalDate.now());
        }
        @Override public Object getCellEditorValue() { return chooser.getLocalDateTime(); }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof LocalDateTime ldt) {
                if (ldt.isAfter(LocalDateTime.now())) {
                    ldt = LocalDateTime.now();
                }
                chooser.setDateTime(ldt);
            }

            return chooser;
        }
    }

    private JPanel bottomPanel() {
        JPanel p = new JPanel();
        JButton save = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
        JButton cancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        save.addActionListener(e -> save());
        cancel.addActionListener(e -> dispose());
        p.add(save);
        p.add(cancel);
        return p;
    }

    private void loadDeliveryData() {
        if (delivery == null || delivery.getId() == null) return;

        deliveryDateField.setDateTime(delivery.getDeliveryDate());
        deliveryTypeCombo.setSelectedItem(delivery.getDeliveryType());
        fatherName.setText(delivery.getFatherName());
        if (delivery.getFatherAge() != null) fatherAge.setText(String.valueOf(delivery.getFatherAge()));
        fatherBirthplace.setText(delivery.getFatherBirthplace());
        fatherProfession.setText(delivery.getFatherProfession());
        fatherAddress.setText(delivery.getFatherAddress());
        anesthesiaField.setText(delivery.getAnesthesiaUsed());
        perinealCombo.setSelectedItem(delivery.getPerinealIntegrity());
        placentaCompleteCheck.setSelected(delivery.isPlacentaComplete());
        if (delivery.getDeliveryMode() != null) deliveryModeCombo.setSelectedItem(delivery.getDeliveryMode().name());
        clinicianField.setText(delivery.getAttendingClinicianId());
        feedingModeField.setText(delivery.getFeedingMode());
        lochiaField.setText(delivery.getLochia());
        noteArea.setText(delivery.getNote());

        try {
            List<Newborn> newborns = newbornManager.getNewbornsByDelivery(delivery.getId());
            tableModel.setRowCount(0);
            existingNewborns.clear();
            for (int i = 0; i < newborns.size(); i++) {
                Newborn nb = newborns.get(i);
                existingNewborns.put(i, nb);
                tableModel.addRow(new Object[]{
                        nb.getBabyPatient().getFirstName(),
                        nb.getBabyPatient().getSecondName(),
                        nb.getBabyPatient().getSex() == 'M' ? MessageBundle.getMessage("angal.common.male.txt") : MessageBundle.getMessage("angal.common.female.txt"),
                        nb.getBirthOrder(),
                        nb.getBirthDate(),
                        nb.getBirthWeight(),
                        nb.getBirthLength(),
                        nb.getHeadCircumference(),
                        nb.getApgarScore1Min(),
                        nb.getApgarScore5Min(),
                        nb.getCryTime() != null ? MessageBundle.getMessage(nb.getCryTime().getKey()) : "",
                        nb.getResuscitationRequired() ? MessageBundle.getMessage("angal.common.yes.txt") : MessageBundle.getMessage("angal.common.no.txt"),
                        nb.getCongenitalAnomalies(),
                        nb.getComplication(),
                        nb.getNeonatalStatus() != null ? MessageBundle.getMessage(nb.getNeonatalStatus().getKey()) : "",
                        nb.getHivStatus() != null ? MessageBundle.getMessage(nb.getHivStatus().getKey()) : ""
                });
            }
            if (!newborns.isEmpty()) {
                motherAliveCheck.setSelected(newborns.get(0).getBabyPatient().getMother() == 'A');
                fatherAliveCheck.setSelected(newborns.get(0).getBabyPatient().getFather() == 'A');
            }
        } catch (OHServiceException e) {
            MessageDialog.error(this, e.getMessage());
        }
    }

    private CryTime findCryTimeByDisplayString(String s) {
        for (CryTime v : CryTime.values()) if (MessageBundle.getMessage(v.getKey()).equals(s)) return v;
        return null;
    }

    private NeonatalStatus findNeonatalStatusByDisplayString(String s) {
        for (NeonatalStatus v : NeonatalStatus.values()) if (MessageBundle.getMessage(v.getKey()).equals(s)) return v;
        return null;
    }

    private HivStatus findHivStatusByDisplayString(String s) {
        for (HivStatus v : HivStatus.values()) if (MessageBundle.getMessage(v.getKey()).equals(s)) return v;
        return HivStatus.UNKNOWN;
    }
}