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
package org.isf.tuberculosis.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.tuberculosis.manager.TuberculosisContactManager;
import org.isf.tuberculosis.manager.TuberculosisTreatmentManager;
import org.isf.tuberculosis.manager.TuberculosisVisitManager;
import org.isf.tuberculosis.model.TuberculosisContact;
import org.isf.tuberculosis.model.TuberculosisTreatment;
import org.isf.tuberculosis.model.TuberculosisVisit;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

public class TuberculosisMiniPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TuberculosisTreatmentManager treatmentManager;
    private TuberculosisVisitManager visitManager;
    private TuberculosisContactManager contactManager;

    private Patient patient;
    private List<TuberculosisTreatment> treatmentList = new ArrayList<>();
    private List<TuberculosisVisit> visitList = new ArrayList<>();
    private List<TuberculosisContact> contactList = new ArrayList<>();
    private TuberculosisTreatment selectedTreatment;
    private TuberculosisVisit selectedVisit;
    private TuberculosisContact selectedContact;

    private JTable treatmentTable;
    private JTable visitTable;
    private JTable contactTable;
    private JButton editTreatmentButton;
    private JButton newVisitButton;
    private JButton newContactButton;
    private JButton editVisitButton;
    private JButton editContactButton;

    private final String[] treatmentHeaders = {
            MessageBundle.getMessage("angal.tb.browser.id.col"),
            MessageBundle.getMessage("angal.tb.browser.classification.col"),
            MessageBundle.getMessage("angal.tb.browser.status.col"),
            MessageBundle.getMessage("angal.tb.browser.startdate.col")
    };

    private final String[] visitHeaders = {
            MessageBundle.getMessage("angal.tb.browser.visitdate.col"),
            MessageBundle.getMessage("angal.tb.browser.dotstatus.col"),
            MessageBundle.getMessage("angal.tb.browser.visitnotes.col")
    };

    private final String[] contactHeaders = {
            MessageBundle.getMessage("angal.tb.browser.contactname.col"),
            MessageBundle.getMessage("angal.tb.browser.contactage.col"),
            MessageBundle.getMessage("angal.tb.browser.contactgender.col"),
            MessageBundle.getMessage("angal.tb.browser.contactscreened.col"),
            MessageBundle.getMessage("angal.tb.browser.contactnotes.col")
    };

    public TuberculosisMiniPanel(Patient patient) {
        this.patient = patient;
        initManagers();
        initialize();
    }

    public void setPatient(Patient newPatient) {
        this.patient = newPatient;
        removeAll();
        initialize();
        revalidate();
        repaint();
    }

    private void initManagers() {
        treatmentManager = Context.getApplicationContext().getBean(TuberculosisTreatmentManager.class);
        visitManager = Context.getApplicationContext().getBean(TuberculosisVisitManager.class);
        contactManager = Context.getApplicationContext().getBean(TuberculosisContactManager.class);
    }

    private void initialize() {
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (patient != null) {
            headerPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + ": "
                    + patient.getCode() + " - " + patient.getSecondName() + " " + patient.getFirstName()));
        } else {
            headerPanel.add(new JLabel(MessageBundle.getMessage("angal.common.patient.txt") + ": "
                    + MessageBundle.getMessage("angal.opd.selectapatient.txt")));
        }
        add(headerPanel, BorderLayout.NORTH);

        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));

        treatmentTable = new JTable(new TreatmentTableModel());
        treatmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treatmentTable.setRowHeight(20);
        treatmentTable.setFillsViewportHeight(true);
        treatmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        treatmentTable.getSelectionModel().addListSelectionListener(new TreatmentSelectionListener());
        for (int i = 0; i < treatmentHeaders.length; i++) {
            treatmentTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        JScrollPane treatmentScroll = new JScrollPane(treatmentTable);
        treatmentScroll.setPreferredSize(new Dimension(600, 100));
        tablesPanel.add(treatmentScroll);

        tablesPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.visitstab")));
        visitTable = new JTable(new VisitTableModel());
        visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visitTable.setRowHeight(20);
        visitTable.setFillsViewportHeight(true);
        visitTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        visitTable.getSelectionModel().addListSelectionListener(new VisitSelectionListener());
        for (int i = 0; i < visitHeaders.length; i++) {
            visitTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        JScrollPane visitScroll = new JScrollPane(visitTable);
        visitScroll.setPreferredSize(new Dimension(600, 100));
        tablesPanel.add(visitScroll);

        tablesPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.contactstab")));
        contactTable = new JTable(new ContactTableModel());
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.setRowHeight(20);
        contactTable.setFillsViewportHeight(true);
        contactTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        contactTable.getSelectionModel().addListSelectionListener(new ContactSelectionListener());
        for (int i = 0; i < contactHeaders.length; i++) {
            contactTable.getColumnModel().getColumn(i).setPreferredWidth(100);
        }
        JScrollPane contactScroll = new JScrollPane(contactTable);
        contactScroll.setPreferredSize(new Dimension(600, 100));
        tablesPanel.add(contactScroll);

        JScrollPane mainScroll = new JScrollPane(tablesPanel);
        add(mainScroll, BorderLayout.CENTER);

        add(getButtonPanel(), BorderLayout.SOUTH);

        if (patient != null) {
            loadTreatments();
        }
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton newTreatmentButton = new JButton(MessageBundle.getMessage("angal.tb.browser.newtreatment.btn"));
        newTreatmentButton.addActionListener(e -> newTreatment());
        buttonPanel.add(newTreatmentButton);

        editTreatmentButton = new JButton(MessageBundle.getMessage("angal.tb.browser.edittreatment.btn"));
        editTreatmentButton.setEnabled(false);
        editTreatmentButton.addActionListener(e -> editTreatment());
        buttonPanel.add(editTreatmentButton);

        newVisitButton = new JButton(MessageBundle.getMessage("angal.tb.browser.newvisit.btn"));
        newVisitButton.setEnabled(false);
        newVisitButton.addActionListener(e -> newVisit());
        buttonPanel.add(newVisitButton);

        editVisitButton = new JButton(MessageBundle.getMessage("angal.tb.browser.editvisit.btn"));
        editVisitButton.setEnabled(false);
        editVisitButton.addActionListener(e -> editVisit());
        buttonPanel.add(editVisitButton);

        newContactButton = new JButton(MessageBundle.getMessage("angal.tb.browser.newcontact.btn"));
        newContactButton.setEnabled(false);
        newContactButton.addActionListener(e -> newContact());
        buttonPanel.add(newContactButton);

        editContactButton = new JButton(MessageBundle.getMessage("angal.tb.browser.editcontact.btn"));
        editContactButton.setEnabled(false);
        editContactButton.addActionListener(e -> editContact());
        buttonPanel.add(editContactButton);

        return buttonPanel;
    }

    private void loadTreatments() {
        if (patient == null) {
            treatmentList = new ArrayList<>();
            ((TreatmentTableModel) treatmentTable.getModel()).fireTableDataChanged();
            selectedTreatment = null;
            editTreatmentButton.setEnabled(false);
            newVisitButton.setEnabled(false);
            newContactButton.setEnabled(false);
            visitList = new ArrayList<>();
            contactList = new ArrayList<>();
            if (visitTable != null) ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
            if (contactTable != null) ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
            return;
        }
        try {
            treatmentList = treatmentManager.getTreatmentsByPatientCode(patient.getCode());
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
            treatmentList = new ArrayList<>();
        }
        ((TreatmentTableModel) treatmentTable.getModel()).fireTableDataChanged();
        selectedTreatment = null;
        editTreatmentButton.setEnabled(false);
        newVisitButton.setEnabled(false);
        newContactButton.setEnabled(false);
        visitList = new ArrayList<>();
        contactList = new ArrayList<>();
        if (visitTable != null) ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
        if (contactTable != null) ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
    }

    private void loadVisits() {
        if (selectedTreatment == null) {
            visitList = new ArrayList<>();
        } else {
            try {
                visitList = visitManager.getVisitsByTreatmentId(selectedTreatment.getId());
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
                visitList = new ArrayList<>();
            }
        }
        selectedVisit = null;
        editVisitButton.setEnabled(false);
        if (visitTable != null) {
            ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
        }
    }

    private void loadContacts() {
        if (selectedTreatment == null) {
            contactList = new ArrayList<>();
        } else {
            try {
                contactList = contactManager.getContactsByTreatmentId(selectedTreatment.getId());
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
                contactList = new ArrayList<>();
            }
        }
        selectedContact = null;
        editContactButton.setEnabled(false);
        if (contactTable != null) {
            ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
        }
    }

    private void newTreatment() {
        if (patient == null) return;
        TuberculosisTreatmentEdit edit = new TuberculosisTreatmentEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), patient, true);
        edit.addTuberculosisTreatmentListener(new TuberculosisTreatmentEdit.TuberculosisTreatmentListener() {
            @Override
            public void treatmentInserted(java.awt.AWTEvent e, TuberculosisTreatment treatment) {
                loadTreatments();
            }
            @Override
            public void treatmentUpdated(java.awt.AWTEvent e, TuberculosisTreatment treatment) {
                loadTreatments();
            }
        });
        edit.setVisible(true);
    }

    private void editTreatment() {
        if (selectedTreatment == null) return;
        TuberculosisTreatmentEdit edit = new TuberculosisTreatmentEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), selectedTreatment, false);
        edit.addTuberculosisTreatmentListener(new TuberculosisTreatmentEdit.TuberculosisTreatmentListener() {
            @Override
            public void treatmentInserted(java.awt.AWTEvent e, TuberculosisTreatment treatment) {
                loadTreatments();
            }
            @Override
            public void treatmentUpdated(java.awt.AWTEvent e, TuberculosisTreatment treatment) {
                loadTreatments();
            }
        });
        edit.setVisible(true);
    }

    private void newVisit() {
        if (selectedTreatment == null) return;
        TuberculosisVisitEdit edit = new TuberculosisVisitEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), selectedTreatment, true);
        edit.setVisible(true);
        loadVisits();
    }

    private void editVisit() {
        if (selectedVisit == null) return;
        TuberculosisVisitEdit edit = new TuberculosisVisitEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), selectedVisit, false);
        edit.setVisible(true);
        loadVisits();
    }

    private void newContact() {
        if (selectedTreatment == null) return;
        TuberculosisContactEdit edit = new TuberculosisContactEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), selectedTreatment, true);
        edit.setVisible(true);
        loadContacts();
    }

    private void editContact() {
        if (selectedContact == null) return;
        TuberculosisContactEdit edit = new TuberculosisContactEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), selectedContact, false);
        edit.setVisible(true);
        loadContacts();
    }

    class TreatmentTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return treatmentList != null ? treatmentList.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return treatmentHeaders.length;
        }

        @Override
        public String getColumnName(int c) {
            return treatmentHeaders[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (treatmentList == null || r >= treatmentList.size()) return null;
            TuberculosisTreatment t = treatmentList.get(r);
            return switch (c) {
                case 0 -> t.getId();
                case 1 -> t.getClassification() != null ? t.getClassification().toString() : "";
                case 2 -> t.getStatus() != null ? t.getStatus().toString() : "";
                case 3 -> t.getTreatmentStartDate() != null ? t.getTreatmentStartDate().format(dateFormatter) : "";
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    class VisitTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return visitList != null ? visitList.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return visitHeaders.length;
        }

        @Override
        public String getColumnName(int c) {
            return visitHeaders[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (visitList == null || r >= visitList.size()) return null;
            TuberculosisVisit v = visitList.get(r);
            return switch (c) {
                case 0 -> v.getVisitDate() != null ? v.getVisitDate().format(dateFormatter) : "";
                case 1 -> v.getDotStatus() != null ? v.getDotStatus().toString() : "";
                case 2 -> v.getNotes() != null ? v.getNotes() : "";
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    class ContactTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return contactList != null ? contactList.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return contactHeaders.length;
        }

        @Override
        public String getColumnName(int c) {
            return contactHeaders[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (contactList == null || r >= contactList.size()) return null;
            TuberculosisContact ct = contactList.get(r);
            return switch (c) {
                case 0 -> ct.getName() != null ? ct.getName() : "";
                case 1 -> ct.getAge() != null ? ct.getAge().toString() : "";
                case 2 -> ct.getGender() != null ? ct.getGender().toString() : "";
                case 3 -> ct.getScreened() != null ? ct.getScreened().toString() : "";
                case 4 -> ct.getNotes() != null ? ct.getNotes() : "";
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    class TreatmentSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int viewRow = treatmentTable.getSelectedRow();
                if (viewRow >= 0 && viewRow < treatmentList.size()) {
                    selectedTreatment = treatmentList.get(viewRow);
                    editTreatmentButton.setEnabled(true);
                    newVisitButton.setEnabled(true);
                    newContactButton.setEnabled(true);
                    loadVisits();
                    loadContacts();
                } else {
                    selectedTreatment = null;
                    editTreatmentButton.setEnabled(false);
                    newVisitButton.setEnabled(false);
                    newContactButton.setEnabled(false);
                    loadVisits();
                    loadContacts();
                }
            }
        }
    }

    class VisitSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int viewRow = visitTable.getSelectedRow();
                if (viewRow >= 0 && viewRow < visitList.size()) {
                    selectedVisit = visitList.get(viewRow);
                    editVisitButton.setEnabled(true);
                } else {
                    selectedVisit = null;
                    editVisitButton.setEnabled(false);
                }
            }
        }
    }

    class ContactSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int viewRow = contactTable.getSelectedRow();
                if (viewRow >= 0 && viewRow < contactList.size()) {
                    selectedContact = contactList.get(viewRow);
                    editContactButton.setEnabled(true);
                } else {
                    selectedContact = null;
                    editContactButton.setEnabled(false);
                }
            }
        }
    }
}
