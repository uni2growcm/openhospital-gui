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
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
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

    private static final String[] COLUMNS = {
            "ID",
            MessageBundle.getMessage("angal.common.patient.txt"),
            MessageBundle.getMessage("angal.homevisit.date.col"),
            MessageBundle.getMessage("angal.homevisit.status.col"),
            MessageBundle.getMessage("angal.homevisit.staff.col"),
            MessageBundle.getMessage("angal.homevisit.purpose.col")
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

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadHomeVisits();
            }
        });

        searchPanel.add(new JLabel(MessageBundle.getMessage("angal.common.search.txt")));
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.filter.status") + ":"));
        statusFilter = new JComboBox<>();
        statusFilter.addItem(null);
        for (HomeVisitStatus status : HomeVisitStatus.values()) {
            statusFilter.addItem(status);
        }
        statusFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(MessageBundle.getMessage("angal.common.all.btn"));
                }
                return this;
            }
        });
        statusFilter.addActionListener(e -> loadHomeVisits());
        filterPanel.add(statusFilter);
        topPanel.add(filterPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int selectedRow = table.getSelectedRow();
                if (row == selectedRow) {
                    table.clearSelection();
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        JPanel paginationPanel = getPaginationPanel();
        paginationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        bottomPanel.add(paginationPanel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton printBtn = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
        JButton newBtn = new JButton(MessageBundle.getMessage("angal.homevisit.new.btn"));
        JButton editBtn = new JButton(MessageBundle.getMessage("angal.homevisit.edit.btn"));
        completeBtn = new JButton(MessageBundle.getMessage("angal.homevisit.complete.btn"));
        cancelBtn = new JButton(MessageBundle.getMessage("angal.homevisit.cancel.btn"));
        postponeBtn = new JButton(MessageBundle.getMessage("angal.homevisit.postpone.btn"));
        reactivateBtn = new JButton(MessageBundle.getMessage("angal.homevisit.reactivate.btn"));
        reactivateBtn.setEnabled(false);
        JButton deleteBtn = new JButton(MessageBundle.getMessage("angal.homevisit.delete.btn"));
        JButton closeBtn = new JButton(MessageBundle.getMessage("angal.homevisit.close.btn"));

        newBtn.addActionListener(e -> openEditor(null));
        editBtn.addActionListener(e -> {
            if (table.getSelectedRow() < 0) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.common.pleaseselectarow.msg"));
                return;
            }
            openEditor(getSelectedHomeVisit());
        });
        printBtn.addActionListener(e -> {
            MessageDialog.info(this, MessageBundle.getMessage("angal.common.featurenotimplemented.msg"));
        });
        completeBtn.addActionListener(e -> completeSelectedVisit());
        cancelBtn.addActionListener(e -> cancelSelectedVisit());
        postponeBtn.addActionListener(e -> postponeSelectedVisit());
        reactivateBtn.addActionListener(e -> reactivateSelectedVisit());
        deleteBtn.addActionListener(e -> deleteSelectedVisit());
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(newBtn);
        btnPanel.add(editBtn);
        btnPanel.add(completeBtn);
        btnPanel.add(cancelBtn);
        btnPanel.add(postponeBtn);
        btnPanel.add(reactivateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(closeBtn);
        btnPanel.add(printBtn);
        bottomPanel.add(btnPanel);

        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void reactivateSelectedVisit() {
        HomeVisit visit = getSelectedHomeVisit();
        if (visit == null) return;

        if (visit.getStatus() != HomeVisitStatus.CANCELLED) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.reactivate.error"));
            return;
        }

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.homevisit.reactivate.confirm",
                        visit.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                MessageBundle.getMessage("angal.homevisit.reactivate.dialog.title"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.reactivateHomeVisit(visit.getId());
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
            Page<HomeVisit> page;
            HomeVisitStatus selectedStatus = (HomeVisitStatus) statusFilter.getSelectedItem();
            String searchText = searchField.getText().trim();

            if (selectedStatus != null) {
                page = manager.getHomeVisitsByStatus(selectedStatus, currentPage, PAGE_SIZE);
            } else {
                page = manager.getHomeVisits(currentPage, PAGE_SIZE);
            }

            totalElements = page.getTotalElements();
            totalPages = page.getTotalPages();

            for (HomeVisit hv : page.getContent()) {
                String patientName = hv.getPatient() != null ? hv.getPatient().getName() : "";
                String visitDate = hv.getVisitStartDate() != null ?
                        hv.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
                String staffName = hv.getStaff() != null ? hv.getStaff().getFullName() : "";
                String status = hv.getStatus() != null ? hv.getStatus().toString() : "";

                if (!searchText.isEmpty() && !patientName.toLowerCase().contains(searchText.toLowerCase())) {
                    continue;
                }

                tableModel.addRow(new Object[]{
                        hv.getId(),
                        patientName,
                        visitDate,
                        status,
                        staffName,
                        hv.getPurpose()
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

        if (visit.getStatus() != HomeVisitStatus.PLANNED) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.complete.error"));
            return;
        }

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.homevisit.complete.confirm",
                        visit.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                MessageBundle.getMessage("angal.homevisit.complete.dialog.title"));

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.completeHomeVisit(visit.getId());
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

        if (visit.getStatus() != HomeVisitStatus.PLANNED &&
                visit.getStatus() != HomeVisitStatus.POSTPONED &&
                visit.getStatus() != HomeVisitStatus.COMPLETED) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.cancel.error"));
            return;
        }

        int confirm = MessageDialog.yesNo(this,
                MessageBundle.formatMessage("angal.homevisit.cancel.confirm",
                        visit.getVisitStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                MessageBundle.getMessage("angal.homevisit.cancel.dialog.title"));

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.cancel.reason.prompt")),
                BorderLayout.NORTH);
        JTextArea reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(reasonArea), BorderLayout.CENTER);

        int reasonConfirm = JOptionPane.showConfirmDialog(
                this,
                panel,
                MessageBundle.getMessage("angal.homevisit.cancel.reason.dialog.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (reasonConfirm != JOptionPane.OK_OPTION) {
            return;
        }

        String reason = reasonArea.getText().trim();
        if (reason.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.cancel.reason.required"));
            return;
        }

        try {
            manager.cancelHomeVisit(visit.getId(), reason);
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

        if (visit.getStatus() != HomeVisitStatus.PLANNED &&
                visit.getStatus() != HomeVisitStatus.POSTPONED) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.postpone.error"));
            return;
        }

        LocalDateTime defaultDate = visit.getVisitStartDate().plusDays(1);
        GoodDateTimeSpinnerChooser dateChooser = new GoodDateTimeSpinnerChooser(defaultDate);
//        dateChooser.setMinDate(LocalDate.now().plusDays(1));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(MessageBundle.getMessage("angal.homevisit.postpone.newdate.prompt")), BorderLayout.NORTH);
        panel.add(dateChooser, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                MessageBundle.getMessage("angal.homevisit.postpone.dialog.title"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            LocalDateTime newDate = dateChooser.getLocalDateTime();
            if (newDate == null) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.postpone.newdate.required"));
                return;
            }
            if (!newDate.toLocalDate().isAfter(LocalDate.now())) {
                MessageDialog.error(this, MessageBundle.getMessage("angal.homevisit.postpone.newdate.future.error"));
                return;
            }
            try {
                manager.postponeHomeVisit(visit.getId(), newDate);
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

        String statusStr = (String) tableModel.getValueAt(row, 3);
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

        String visitDateStr = (String) tableModel.getValueAt(row, 2);
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
}