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
package org.isf.homevisit.gui;

import org.isf.homevisit.manager.HomeVisitBrowserManager;
import org.isf.homevisit.manager.StaffBrowserManager;
import org.isf.homevisit.model.HomeVisit;
import org.isf.homevisit.model.HomeVisitStatus;
import org.isf.homevisit.model.Staff;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class HomeVisitEdit extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeVisitEdit.class);

    private HomeVisitBrowserManager manager;
    private StaffBrowserManager staffManager;
    private HomeVisit homeVisit;
    private HomeVisitBrowser parent;
    private Consumer<HomeVisit> onSaveCallback;

    private JTextField patientField;
    private JButton selectPatientBtn;
    private JButton clearPatientBtn;
    private JComboBox<Staff> staffCombo;
    private GoodDateTimeSpinnerChooser visitStartDateChooser;
    private JTextArea purposeArea;
    private JTextArea clinicalNotesArea;
    private JTextArea observationsArea;
    private JTextField addressField;
    private JTextField contactPhoneField;
    private GoodDateTimeSpinnerChooser nextVisitDateChooser;
    private JComboBox<HomeVisitStatus> statusCombo;

    private Patient selectedPatient;

    public HomeVisitEdit(HomeVisitBrowser parent, HomeVisitBrowserManager manager, HomeVisit homeVisit, Consumer<HomeVisit> onSaveCallback) {
        this.parent = parent;
        this.manager = manager;
        this.staffManager = Context.getApplicationContext().getBean(StaffBrowserManager.class);
        this.homeVisit = homeVisit;
        this.onSaveCallback = onSaveCallback;
        initComponents();
        if (homeVisit != null && homeVisit.getId() != 0) {
            loadExistingData();
            setTitle(MessageBundle.getMessage("angal.homevisit.edit.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.homevisit.new.title"));
        }
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel patientLabel = new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + " *");
        mainPanel.add(patientLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        patientField = new JTextField(25);
        patientField.setEditable(false);
        mainPanel.add(patientField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        try {
            ImageIcon searchIcon = new ImageIcon("rsc/icons/pick_patient_button.png");
            selectPatientBtn = new JButton(MessageBundle.getMessage("angal.common.select.btn"), searchIcon);
            selectPatientBtn.setToolTipText(MessageBundle.getMessage("angal.patient.affiliation.select.patient"));
        } catch (Exception e) {
            selectPatientBtn = new JButton(MessageBundle.getMessage("angal.common.select.btn"));
            selectPatientBtn.setToolTipText("Rechercher un patient");
        }
        selectPatientBtn.addActionListener(e -> selectPatient());
        mainPanel.add(selectPatientBtn, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        try {
            ImageIcon clearIcon = new ImageIcon("rsc/icons/remove_patient_button.png");
            clearPatientBtn = new JButton(clearIcon);
            clearPatientBtn.setToolTipText(MessageBundle.getMessage("angal.common.clear.btn"));
        } catch (Exception e) {
            clearPatientBtn = new JButton("X");
            clearPatientBtn.setToolTipText(MessageBundle.getMessage("angal.common.clear.btn"));
        }
        clearPatientBtn.addActionListener(e -> clearPatient());
        mainPanel.add(clearPatientBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.staff.col") + " *"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        staffCombo = new JComboBox<>();
        loadStaffList();
        mainPanel.add(staffCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel startDateLabel = new JLabel(MessageBundle.getMessage("angal.homevisit.date.col") + " *");
        mainPanel.add(startDateLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        visitStartDateChooser = new GoodDateTimeSpinnerChooser(LocalDateTime.now());
        visitStartDateChooser.setMinDate(LocalDate.now());
        visitStartDateChooser.addDateChangeListener(e -> {
            LocalDate newVisitDate = e.getNewDate();
            if (newVisitDate != null) {
                LocalDate minNext = newVisitDate.plusDays(1);
                nextVisitDateChooser.setMinDate(minNext);
                LocalDateTime currentNext = nextVisitDateChooser.getLocalDateTime();
                if (currentNext != null && currentNext.toLocalDate().isBefore(minNext)) {
                    nextVisitDateChooser.setDateTime(minNext.atStartOfDay());
                }
            }
        });
        mainPanel.add(visitStartDateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.purpose.col")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        purposeArea = new JTextArea(2, 30);
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true);
        JScrollPane purposeScroll = new JScrollPane(purposeArea);
        purposeScroll.setPreferredSize(new Dimension(400, 60));
        purposeScroll.setMinimumSize(new Dimension(200, 60));
        purposeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        mainPanel.add(purposeScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.address.col")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        addressField = new JTextField(50);
        mainPanel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.contactphone.col")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        contactPhoneField = new JTextField(30);
        mainPanel.add(contactPhoneField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.notes.col")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        clinicalNotesArea = new JTextArea(3, 30);
        clinicalNotesArea.setLineWrap(true);
        clinicalNotesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(clinicalNotesArea);
        notesScroll.setPreferredSize(new Dimension(400, 80));
        notesScroll.setMinimumSize(new Dimension(200, 80));
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        mainPanel.add(notesScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.observations.col")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        observationsArea = new JTextArea(3, 30);
        observationsArea.setLineWrap(true);
        observationsArea.setWrapStyleWord(true);
        JScrollPane obsScroll = new JScrollPane(observationsArea);
        obsScroll.setPreferredSize(new Dimension(400, 80));
        obsScroll.setMinimumSize(new Dimension(200, 80));
        obsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        mainPanel.add(obsScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.nextvisit.col")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 4;
        nextVisitDateChooser = new GoodDateTimeSpinnerChooser(null);
        nextVisitDateChooser.setMinDate(LocalDate.now().plusDays(1));
        mainPanel.add(nextVisitDateChooser, gbc);

        if (homeVisit != null && homeVisit.getId() != 0) {
            gbc.gridx = 0;
            gbc.gridy = 9;
            gbc.gridwidth = 1;
            mainPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.status.col")), gbc);

            gbc.gridx = 1;
            gbc.gridwidth = 4;
            statusCombo = new JComboBox<>(HomeVisitStatus.values());
            mainPanel.add(statusCombo, gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 5;
        JLabel requiredLabel = new JLabel("* " + MessageBundle.getMessage("angal.common.requiredfields"));
        requiredLabel.setFont(requiredLabel.getFont().deriveFont(Font.BOLD));
        mainPanel.add(requiredLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton saveBtn = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
        JButton cancelBtn = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadStaffList() {
        staffCombo.removeAllItems();
        staffCombo.addItem(null);
        try {
            List<Staff> staffList = staffManager.getStaff();
            for (Staff staff : staffList) {
                staffCombo.addItem(staff);
            }
        } catch (OHServiceException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void selectPatient() {
        SelectPatient sp = new SelectPatient(this, false, false);
        sp.addSelectionListener(patient -> {
            selectedPatient = patient;
            patientField.setText(patient.getName());
            addressField.setText(patient.getAddress());
            contactPhoneField.setText(patient.getTelephone());
        });
        sp.setVisible(true);
    }

    private void clearPatient() {
        selectedPatient = null;
        patientField.setText("");
    }

    private void loadExistingData() {
        selectedPatient = homeVisit.getPatient();
        if (selectedPatient != null) {
            patientField.setText(selectedPatient.getName());
        }
        staffCombo.setSelectedItem(homeVisit.getStaff());
        visitStartDateChooser.setDateTime(homeVisit.getVisitStartDate());
        purposeArea.setText(homeVisit.getPurpose());
        addressField.setText(homeVisit.getAddress());
        contactPhoneField.setText(homeVisit.getContactPhone());
        clinicalNotesArea.setText(homeVisit.getClinicalNotes());
        observationsArea.setText(homeVisit.getObservations());
        if (homeVisit.getNextVisitDate() != null) {
            nextVisitDateChooser.setDateTime(homeVisit.getNextVisitDate());
        }
        if (statusCombo != null) {
            statusCombo.setSelectedItem(homeVisit.getStatus());
            statusCombo.setEnabled(homeVisit.getStatus() == HomeVisitStatus.PLANNED);
        }
    }

    private void save() {
        if (selectedPatient == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.validation.patient.required.msg"));
            return;
        }

        if (staffCombo.getSelectedItem() == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.validation.staff.required.msg"));
            return;
        }

        LocalDateTime visitStartDate = visitStartDateChooser.getLocalDateTime();
        if (visitStartDate == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.validation.startdate.required.msg"));
            return;
        }

        if (visitStartDate.isBefore(LocalDateTime.now())) {
            int confirm = MessageDialog.yesNo(this,
                    MessageBundle.getMessage("angal.homevisit.validation.startdate.past.confirm"),
                    MessageBundle.getMessage("angal.common.confirm"));
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        boolean isNew = (homeVisit == null || homeVisit.getId() == 0);
        if (isNew) {
            homeVisit = new HomeVisit();
        }

        homeVisit.setPatient(selectedPatient);
        homeVisit.setStaff((Staff) staffCombo.getSelectedItem());
        homeVisit.setVisitStartDate(visitStartDate);
        homeVisit.setPurpose(purposeArea.getText().trim());
        homeVisit.setAddress(addressField.getText().trim());
        homeVisit.setContactPhone(contactPhoneField.getText().trim());
        homeVisit.setClinicalNotes(clinicalNotesArea.getText().trim());
        homeVisit.setObservations(observationsArea.getText().trim());
        homeVisit.setNextVisitDate(nextVisitDateChooser.getLocalDateTime());

        if (!isNew && statusCombo != null) {
            homeVisit.setStatus((HomeVisitStatus) statusCombo.getSelectedItem());
        } else {
            homeVisit.setStatus(HomeVisitStatus.PLANNED);
        }

        try {
            HomeVisit saved = manager.saveHomeVisit(homeVisit);
            if (onSaveCallback != null) {
                onSaveCallback.accept(saved);
            }
            dispose();
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.save.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }
}