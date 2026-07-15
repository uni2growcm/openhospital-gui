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
import org.isf.homevisit.model.HomeVisit;
import org.isf.homevisit.model.HomeVisitStatus;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportHomeVisitActivity;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.isf.utils.layout.SpringUtilities;

import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomeVisitBrowser extends ModalJFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeVisitBrowser.class);
    private HomeVisitBrowserManager manager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<HomeVisitStatus> statusFilter;
    private JButton completeBtn;
    private JButton cancelBtn;
    private JButton postponeBtn;
    private JButton reactivateBtn;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 100;
    private int totalPages = 0;
    private JButton jPrevButton;
    private JButton jNextButton;
    private JLabel jPageLabel;
    private JComboBox<Integer> jPageComboBox;
    private long totalElements = 0;
    private JLabel jTotalLabel;
    private JTextField codeFilterField;
    private JRadioButton radioAll;
    private JRadioButton radioMale;
    private JRadioButton radioFemale;
    private JTextField ageFromField;
    private JTextField ageToField;
    private GoodDateChooser dateFromChooser;
    private GoodDateChooser dateToChooser;

    private static final String[] COLUMNS = {
            "ID",
            MessageBundle.getMessage("angal.common.code.txt"),
            MessageBundle.getMessage("angal.common.patient.txt"),
            MessageBundle.getMessage("angal.homevisit.date.col"),
            MessageBundle.getMessage("angal.homevisit.status.col"),
            MessageBundle.getMessage("angal.homevisit.staff.col"),
            MessageBundle.getMessage("angal.homevisit.nextvisit.col")
    };

    public HomeVisitBrowser() {
        this.manager = Context.getApplicationContext().getBean(HomeVisitBrowserManager.class);
        initComponents();
        loadHomeVisits();
        setTitle(MessageBundle.getMessage("angal.homevisit.browser.title"));
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel filtersContent = new JPanel(new SpringLayout());
        filtersContent.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.homevisit.filters.border")));

        codeFilterField = new JTextField(10);
        filtersContent.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        filtersContent.add(codeFilterField);

        statusFilter = new JComboBox<>();
        statusFilter.addItem(null);
        for (HomeVisitStatus status : HomeVisitStatus.values()) {
            statusFilter.addItem(status);
        }
        statusFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) setText(MessageBundle.getMessage("angal.common.all.btn"));
                return this;
            }
        });
        statusFilter.addActionListener(e -> {
            currentPage = 0;
            loadHomeVisits();
        });
        filtersContent.add(new JLabel(MessageBundle.getMessage("angal.homevisit.filter.status") + ":"));
        filtersContent.add(statusFilter);

        JPanel sexPanel = new JPanel();
        ButtonGroup sexGroup = new ButtonGroup();
        radioAll = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"), true);
        radioMale = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
        radioFemale = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
        sexGroup.add(radioAll);
        sexGroup.add(radioMale);
        sexGroup.add(radioFemale);
        sexPanel.add(radioAll);
        sexPanel.add(radioMale);
        sexPanel.add(radioFemale);
        filtersContent.add(new JLabel(MessageBundle.getMessage("angal.common.sex.txt") + ":"));
        filtersContent.add(sexPanel);

        JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ageFromField = new JTextField(4);
        ageToField = new JTextField(4);
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.agefrom.label")));
        agePanel.add(ageFromField);
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.common.ageto.label")));
        agePanel.add(ageToField);
        filtersContent.add(new JLabel(MessageBundle.getMessage("angal.common.age.txt") + ":"));
        filtersContent.add(agePanel);

        dateFromChooser = new GoodDateChooser(LocalDate.now().minusWeeks(1), true, true);
        dateToChooser = new GoodDateChooser(LocalDate.now(), true, true);
        filtersContent.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        filtersContent.add(dateFromChooser);
        filtersContent.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        filtersContent.add(dateToChooser);

        SpringUtilities.makeCompactGrid(filtersContent, 6, 2, 5, 5, 5, 5);

        JPanel searchBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton searchBtn = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
        JButton resetBtn = new JButton(MessageBundle.getMessage("angal.opd.reset.btn"));
        searchBtnPanel.add(searchBtn);
        searchBtnPanel.add(resetBtn);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchPanel.add(new JLabel(MessageBundle.getMessage("angal.common.search.txt") + ":"));
        searchPanel.add(searchField);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                currentPage = 0;
                loadHomeVisits();
            }
        });

        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.add(searchPanel);
        leftContent.add(filtersContent);
        leftContent.add(searchBtnPanel);

        leftPanel.add(leftContent, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateButtonStates();
        });

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel paginationPanel = getPaginationPanel();
        paginationPanel.setBorder(BorderFactory.createEtchedBorder());
        centerWrapper.add(paginationPanel, BorderLayout.SOUTH);

        JPanel btnPanel     = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton newBtn      = new JButton(MessageBundle.getMessage("angal.homevisit.new.btn"));
        JButton editBtn     = new JButton(MessageBundle.getMessage("angal.homevisit.edit.btn"));
        completeBtn         = new JButton(MessageBundle.getMessage("angal.homevisit.complete.btn"));
        cancelBtn           = new JButton(MessageBundle.getMessage("angal.homevisit.cancel.btn"));
        postponeBtn         = new JButton(MessageBundle.getMessage("angal.homevisit.postpone.btn"));
        reactivateBtn       = new JButton(MessageBundle.getMessage("angal.homevisit.reactivate.btn"));
        JButton deleteBtn   = new JButton(MessageBundle.getMessage("angal.homevisit.delete.btn"));
        JButton printBtn    = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
        JButton closeBtn    = new JButton(MessageBundle.getMessage("angal.homevisit.close.btn"));

        completeBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        postponeBtn.setEnabled(false);
        reactivateBtn.setEnabled(false);

        printBtn.addActionListener(e->generateReport());
        newBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            if (table.getSelectedRow() < 0) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
                return;
            }
            openEditor(getSelectedHomeVisit());
        });
        completeBtn.addActionListener(e -> completeSelectedVisit());
        cancelBtn.addActionListener(e -> cancelSelectedVisit());
        postponeBtn.addActionListener(e -> postponeSelectedVisit());
        reactivateBtn.addActionListener(e -> reactivateSelectedVisit());
        deleteBtn.addActionListener(e -> deleteSelectedVisit());
        closeBtn.addActionListener(e -> dispose());

        searchBtn.addActionListener(e -> {
            currentPage = 0;
            loadHomeVisits();
        });

        resetBtn.addActionListener(e -> {
            codeFilterField.setText("");
            statusFilter.setSelectedIndex(0);
            radioAll.setSelected(true);
            ageFromField.setText("");
            ageToField.setText("");
            dateFromChooser.setDate(LocalDate.now().minusWeeks(1));
            dateToChooser.setDate(LocalDate.now());
            searchField.setText("");
            currentPage = 0;
            loadHomeVisits();
        });

        btnPanel.add(newBtn);
        btnPanel.add(editBtn);
        btnPanel.add(completeBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(postponeBtn);
        btnPanel.add(reactivateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(printBtn);
        btnPanel.add(closeBtn);

        add(centerWrapper, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { dispose(); }
        });
    }

    private void reactivateSelectedVisit() {
        HomeVisit visit = getSelectedHomeVisit();
        if (visit == null) return;

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.homevisit.reactivate.confirm",
                        visit.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                MessageBundle.getMessage("angal.homevisit.reactivate.dialog.title"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                visit.setStatus(HomeVisitStatus.PLANNED);
                manager.updateHomeVisit(visit);
                loadHomeVisits();
                MessageDialog.info(this, MessageBundle.getMessage("angal.homevisit.reactivate.success"));
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.reactivate.error"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void loadHomeVisits() {
        tableModel.setRowCount(0);
        try {
            HomeVisitStatus selectedStatus = (HomeVisitStatus) statusFilter.getSelectedItem();

            char sex = radioMale.isSelected() ? 'M' : radioFemale.isSelected() ? 'F' : 'A';
            Character sexFilter = (sex == 'A') ? null : sex;

            String ageFromStr = ageFromField.getText().trim();
            String ageToStr = ageToField.getText().trim();
            Integer ageFromVal = null;
            Integer ageToVal = null;
            try {
                if (!ageFromStr.isEmpty()) ageFromVal = Integer.parseInt(ageFromStr);
                if (!ageToStr.isEmpty()) ageToVal = Integer.parseInt(ageToStr);
            } catch (NumberFormatException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseinsertavalidnumber.msg"));
                return;
            }

            String codeStr = codeFilterField.getText().trim();
            Integer codeVal = null;
            if (!codeStr.isEmpty()) {
                try {
                    codeVal = Integer.parseInt(codeStr);
                } catch (NumberFormatException e) {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseinsertavalidnumber.msg"));
                    return;
                }
            }

            LocalDate from = dateFromChooser.getDate();
            LocalDate to = dateToChooser.getDate();
            LocalDateTime dateFromDT = (from != null) ? from.atStartOfDay() : null;
            LocalDateTime dateToDT = (to != null) ? to.atTime(23, 59, 59) : null;

            String searchText = searchField.getText().trim();
            String searchTextFilter = searchText.isEmpty() ? null : searchText;

            Page<HomeVisit> page = manager.getHomeVisitsWithFilters(
                    codeVal, selectedStatus, dateFromDT, dateToDT, sexFilter,
                    ageFromVal, ageToVal, searchTextFilter, currentPage, PAGE_SIZE);

            totalElements = page.getTotalElements();
            totalPages = page.getTotalPages();

            for (HomeVisit hv : page.getContent()) {
                Integer patientCode = hv.getPatient() != null ? hv.getPatient().getCode() : null;
                String patientName = hv.getPatient() != null ? hv.getPatient().getName() : "";
                String visitDate = hv.getVisitStartDate() != null ?
                        hv.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
                String staffName = hv.getStaff() != null ? hv.getStaff().getFullName() : "";
                String statusStr = hv.getStatus() != null ? hv.getStatus().toString() : "";

                String nextVisitDate = hv.getNextVisitDate() != null ?
                        hv.getNextVisitDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";

                tableModel.addRow(new Object[]{
                        hv.getId(), patientCode, patientName, visitDate, statusStr, staffName, nextVisitDate
                });
            }

            updatePaginationControls();
            updateButtonStates();

        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private HomeVisit getSelectedHomeVisit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            return manager.getHomeVisit(id);
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.load.error.msg"));
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void openEditor(HomeVisit homeVisit) {
        HomeVisitEdit editor = new HomeVisitEdit(this, manager, homeVisit, saved -> {
            loadHomeVisits();
        });
        editor.setVisible(true);
    }

    private void completeSelectedVisit() {
        HomeVisit visit = getSelectedHomeVisit();
        if (visit == null) return;

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.homevisit.complete.confirm",
                        visit.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                MessageBundle.getMessage("angal.homevisit.complete.dialog.title"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                visit.setStatus(HomeVisitStatus.COMPLETED);
                manager.updateHomeVisit(visit);
                loadHomeVisits();
                MessageDialog.info(this, MessageBundle.getMessage("angal.homevisit.complete.success"));
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.complete.error"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void cancelSelectedVisit() {
        HomeVisit visit = getSelectedHomeVisit();
        if (visit == null) return;

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.homevisit.cancel.confirm",
                        visit.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                MessageBundle.getMessage("angal.homevisit.cancel.dialog.title"));

        if (confirm != JOptionPane.YES_OPTION) return;

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.cancel.reason.prompt")),
                BorderLayout.NORTH);
        JTextArea reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(reasonArea), BorderLayout.CENTER);

        int reasonConfirm = JOptionPane.showConfirmDialog(
                this, panel,
                MessageBundle.getMessage("angal.homevisit.cancel.reason.dialog.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (reasonConfirm != JOptionPane.OK_OPTION) return;

        String reason = reasonArea.getText().trim();
        if (reason.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.cancel.reason.required"));
            return;
        }

        try {
            visit.setStatus(HomeVisitStatus.CANCELLED);
            visit.setCancellationReason(reason);
            manager.updateHomeVisit(visit);
            loadHomeVisits();
            MessageDialog.info(this, MessageBundle.getMessage("angal.homevisit.cancel.success"));
        } catch (OHServiceException e) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.cancel.error"));
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void postponeSelectedVisit() {
        HomeVisit visit = getSelectedHomeVisit();
        if (visit == null) return;

        LocalDateTime defaultDate = visit.getVisitStartDate().plusDays(1);
        GoodDateTimeSpinnerChooser dateChooser = new GoodDateTimeSpinnerChooser(defaultDate);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.postpone.newdate.prompt")),
                BorderLayout.NORTH);
        panel.add(dateChooser, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, panel,
                MessageBundle.getMessage("angal.homevisit.postpone.dialog.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            LocalDateTime newDate = dateChooser.getLocalDateTime();
            try {
                visit.setStatus(HomeVisitStatus.POSTPONED);
                visit.setVisitStartDate(newDate);
                manager.updateHomeVisit(visit);
                loadHomeVisits();
                MessageDialog.info(this, MessageBundle.getMessage("angal.homevisit.postpone.success"));
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.postpone.error"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void deleteSelectedVisit() {
        HomeVisit visit = getSelectedHomeVisit();
        if (visit == null) return;

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.getMessage("angal.homevisit.delete.confirm"),
                MessageBundle.getMessage("angal.common.delete"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.deleteHomeVisit(visit.getId());
                loadHomeVisits();
                MessageDialog.info(this, MessageBundle.getMessage("angal.homevisit.delete.success"));
            } catch (OHServiceException e) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.delete.error"));
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void updateButtonStates() {
        int row = table.getSelectedRow();
        if (row < 0) {
            completeBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            postponeBtn.setEnabled(false);
            reactivateBtn.setEnabled(false);
            return;
        }

        String statusStr = (String) tableModel.getValueAt(row, 4);
        HomeVisitStatus status = null;
        for (HomeVisitStatus s : HomeVisitStatus.values()) {
            if (s.name().equals(statusStr) || s.toString().equals(statusStr)) {
                status = s;
                break;
            }
        }
        if (status == null) {
            completeBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            postponeBtn.setEnabled(false);
            reactivateBtn.setEnabled(false);
            return;
        }

        String visitDateStr = (String) tableModel.getValueAt(row, 3);
        boolean visitDateReached = false;
        try {
            LocalDateTime visitDate = LocalDateTime.parse(visitDateStr,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            visitDateReached = !visitDate.toLocalDate().isAfter(LocalDate.now());
        } catch (Exception e) {
            LOGGER.warn("Impossible de parser la date: {}", visitDateStr);
        }

        completeBtn.setEnabled(
                visitDateReached && (
                        status == HomeVisitStatus.PLANNED ||
                                status == HomeVisitStatus.POSTPONED
                )
        );
        cancelBtn.setEnabled(
                status == HomeVisitStatus.PLANNED ||
                        status == HomeVisitStatus.POSTPONED ||
                        status == HomeVisitStatus.COMPLETED
        );
        postponeBtn.setEnabled(
                status == HomeVisitStatus.PLANNED ||
                        status == HomeVisitStatus.POSTPONED
        );
        reactivateBtn.setEnabled(
                status == HomeVisitStatus.CANCELLED
        );
    }

    private JPanel getPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        jPrevButton = new JButton("<");
        jPrevButton.setEnabled(false);
        jPrevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadHomeVisits();
            }
        });

        jPageComboBox = new JComboBox<>();
        jPageComboBox.addActionListener(e -> {
            Integer selected = (Integer) jPageComboBox.getSelectedItem();
            if (selected != null && selected - 1 != currentPage) {
                currentPage = selected - 1;
                loadHomeVisits();
            }
        });

        jPageLabel = new JLabel("/ 1");
        jNextButton = new JButton(">");
        jNextButton.setEnabled(false);
        jNextButton.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadHomeVisits();
            }
        });

        panel.add(jPrevButton);
        panel.add(jPageComboBox);
        panel.add(jPageLabel);
        panel.add(jNextButton);

        jTotalLabel = new JLabel();
        jTotalLabel.setFont(jTotalLabel.getFont().deriveFont(Font.BOLD));
        panel.add(jTotalLabel);

        return panel;
    }


    private void updatePaginationControls() {
        jPrevButton.setEnabled(currentPage > 0);
        jNextButton.setEnabled(currentPage < totalPages - 1);
        jPageComboBox.setEnabled(totalPages > 1);

        if (jPageComboBox.getItemCount() != totalPages) {
            ActionListener[] listeners = jPageComboBox.getActionListeners();
            for (ActionListener l : listeners) {
                jPageComboBox.removeActionListener(l);
            }

            jPageComboBox.removeAllItems();
            for (int i = 1; i <= totalPages; i++) {
                jPageComboBox.addItem(i);
            }

            jPageComboBox.addActionListener(e -> {
                Integer selected = (Integer) jPageComboBox.getSelectedItem();
                if (selected != null && selected - 1 != currentPage) {
                    currentPage = selected - 1;
                    loadHomeVisits();
                }
            });
        }

        if (totalPages > 0) {
            jPageComboBox.setSelectedItem(currentPage + 1);
            jPageLabel.setText("/ " + totalPages + " " + MessageBundle.getMessage("angal.common.pages.txt"));
        } else {
            jPageLabel.setText("/ 0 " + MessageBundle.getMessage("angal.common.pages.txt"));
        }
        jTotalLabel.setText(MessageBundle.formatMessage("angal.common.total.label", totalElements));
    }

    private void generateReport() {
        HomeVisitReportDialog dialog = new HomeVisitReportDialog(this);
        dialog.setOnOk(() -> {
            LocalDate fromDate = dialog.getDateFromValue();
            LocalDate toDate = dialog.getDateToValue();
            new GenericReportHomeVisitActivity(fromDate, toDate, "HomeVisitActivityReport");
        });
        dialog.showAsModal(this);
    }

}