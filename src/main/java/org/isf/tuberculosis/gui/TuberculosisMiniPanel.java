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
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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

    private boolean showContacts = false;

    public TuberculosisMiniPanel(Patient patient) {
        this(patient, false);
    }

    public TuberculosisMiniPanel(Patient patient, boolean showContacts) {
        this.patient = patient;
        this.showContacts = showContacts;
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
        if (showContacts) {
            contactManager = Context.getApplicationContext().getBean(TuberculosisContactManager.class);
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());

        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));

        treatmentTable = new JTable(new TreatmentTableModel());
        treatmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treatmentTable.setRowHeight(20);
        treatmentTable.setFillsViewportHeight(true);
        treatmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        treatmentTable.getSelectionModel().addListSelectionListener(new TreatmentSelectionListener());
        treatmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editTreatment();
                }
            }
        });
        for (int i = 0; i < treatmentHeaders.length; i++) {
            treatmentTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        JScrollPane treatmentScroll = new JScrollPane(treatmentTable);
        treatmentScroll.setPreferredSize(new Dimension(600, showContacts ? 100 : 150));
        tablesPanel.add(treatmentScroll);

        JLabel visitsLabel = new JLabel(MessageBundle.getMessage("angal.tb.browser.visitstab"));
        visitsLabel.setFont(visitsLabel.getFont().deriveFont(Font.BOLD));
        visitsLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
        tablesPanel.add(visitsLabel);

        visitTable = new JTable(new VisitTableModel());
        visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visitTable.setRowHeight(20);
        visitTable.setFillsViewportHeight(true);
        visitTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        visitTable.getSelectionModel().addListSelectionListener(new VisitSelectionListener());
        visitTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editVisit();
                }
            }
        });
        for (int i = 0; i < visitHeaders.length; i++) {
            visitTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        JScrollPane visitScroll = new JScrollPane(visitTable);
        visitScroll.setPreferredSize(new Dimension(600, showContacts ? 100 : 150));
        tablesPanel.add(visitScroll);

        if (showContacts) {
            JLabel contactsLabel = new JLabel(MessageBundle.getMessage("angal.tb.browser.contactstab"));
            contactsLabel.setFont(contactsLabel.getFont().deriveFont(Font.BOLD));
            contactsLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
            tablesPanel.add(contactsLabel);

            contactTable = new JTable(new ContactTableModel());
            contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contactTable.setRowHeight(20);
            contactTable.setFillsViewportHeight(true);
            contactTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            contactTable.getSelectionModel().addListSelectionListener(new ContactSelectionListener());
            contactTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        editContact();
                    }
                }
            });
            for (int i = 0; i < contactHeaders.length; i++) {
                contactTable.getColumnModel().getColumn(i).setPreferredWidth(100);
            }
            JScrollPane contactScroll = new JScrollPane(contactTable);
            contactScroll.setPreferredSize(new Dimension(600, 100));
            tablesPanel.add(contactScroll);
        }

        JScrollPane mainScroll = new JScrollPane(tablesPanel);
        add(mainScroll, BorderLayout.CENTER);

        if (patient != null) {
            loadTreatments();
        }
    }

    private void loadTreatments() {
        if (patient == null) {
            treatmentList = new ArrayList<>();
            ((TreatmentTableModel) treatmentTable.getModel()).fireTableDataChanged();
            selectedTreatment = null;
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
        visitList = new ArrayList<>();
        contactList = new ArrayList<>();
        if (visitTable != null) ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
        if (contactTable != null) ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
        if (!treatmentList.isEmpty()) {
            treatmentTable.setRowSelectionInterval(0, 0);
        }
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
        if (contactTable != null) {
            ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
        }
    }

    private void editTreatment() {
        if (selectedTreatment == null) {
            int selectedRow = treatmentTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < treatmentList.size()) {
                selectedTreatment = treatmentList.get(selectedRow);
            }
        }
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

    private void editVisit() {
        if (selectedVisit == null) {
            int selectedRow = visitTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < visitList.size()) {
                selectedVisit = visitList.get(selectedRow);
            }
        }
        if (selectedVisit == null) return;
        TuberculosisVisitEdit edit = new TuberculosisVisitEdit(
                (JFrame) SwingUtilities.getWindowAncestor(this), selectedVisit, false);
        edit.setVisible(true);
        loadVisits();
    }

    private void editContact() {
        if (selectedContact == null) {
            int selectedRow = contactTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < contactList.size()) {
                selectedContact = contactList.get(selectedRow);
            }
        }
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
                    loadVisits();
                    if (showContacts) loadContacts();
                } else {
                    selectedTreatment = null;
                    loadVisits();
                    if (showContacts) loadContacts();
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
                } else {
                    selectedVisit = null;
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
                } else {
                    selectedContact = null;
                }
            }
        }
    }
}
