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
package org.isf.utils.jobjects;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.visits.manager.VisitManager;

public class VisitReportDialog extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private final boolean administrator;
    private final VisitManager visitManager;

    private GoodDateChooser dateFrom;
    private GoodDateChooser dateTo;

    private JCheckBox allUsersCheckBox;
    private JList<User> usersList;

    private boolean cancel = true;
    private Runnable onOk;

    public VisitReportDialog(JFrame parent, boolean administrator) {
        super();

        this.administrator = administrator;
        this.visitManager = Context.getApplicationContext()
                .getBean(VisitManager.class);

        setTitle(MessageBundle.getMessage(
                "angal.visit.report.dialog.title"));

        setContentPane(getContentPanel());

        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        if (administrator) {
            setMinimumSize(new Dimension(450, 420));
            setPreferredSize(new Dimension(450, 420));
        } else {
            setMinimumSize(new Dimension(320, 190));
            setPreferredSize(new Dimension(320, 190));
        }

        pack();

        if (administrator) {
            loadUsersWithAppointments();
        }
    }

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    private JPanel getContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(getDatePanel(), BorderLayout.NORTH);

        if (administrator) {
            panel.add(getUsersPanel(), BorderLayout.CENTER);
        }

        panel.add(getButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel getDatePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));

        panel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage(
                        "angal.visit.report.date.interval")));

        panel.add(new JLabel(
                MessageBundle.getMessage("angal.common.from.txt") + ":"));

        dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        panel.add(dateFrom);

        panel.add(new JLabel(
                MessageBundle.getMessage("angal.common.to.txt") + ":"));

        dateTo = new GoodDateChooser(LocalDate.now());
        panel.add(dateTo);

        dateFrom.addDateChangeListener(event -> {
            if (administrator && event.getNewDate() != null) {
                loadUsersWithAppointments();
            }
        });

        dateTo.addDateChangeListener(event -> {
            if (administrator && event.getNewDate() != null) {
                loadUsersWithAppointments();
            }
        });

        return panel;
    }

    private JPanel getUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage(
                        "angal.visit.report.users.selection")));

        allUsersCheckBox = new JCheckBox(
                MessageBundle.getMessage(
                        "angal.visit.report.allappointments"));

        allUsersCheckBox.setSelected(true);

        allUsersCheckBox.addActionListener(event -> {
            boolean allUsers = allUsersCheckBox.isSelected();

            usersList.setEnabled(!allUsers);

            if (allUsers) {
                usersList.clearSelection();
            }
        });

        panel.add(allUsersCheckBox, BorderLayout.NORTH);

        usersList = new JList<>();
        usersList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        usersList.setEnabled(false);

        usersList.setCellRenderer(new DefaultListCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                String displayedValue = "";

                if (value instanceof User user) {
                    displayedValue = user.getUserName();
                }

                return super.getListCellRendererComponent(
                        list,
                        displayedValue,
                        index,
                        isSelected,
                        cellHasFocus);
            }
        });

        JScrollPane scrollPane = new JScrollPane(usersList);
        scrollPane.setPreferredSize(new Dimension(380, 190));

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton(
                MessageBundle.getMessage(
                        "angal.visit.report.refreshusers.btn"));

        refreshButton.addActionListener(event ->
                loadUsersWithAppointments());

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);

        panel.add(refreshPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadUsersWithAppointments() {
        if (!administrator || usersList == null) {
            return;
        }

        LocalDate from = dateFrom != null ? dateFrom.getDate() : null;
        LocalDate to = dateTo != null ? dateTo.getDate() : null;

        if (from == null || to == null || from.isAfter(to)) {
            usersList.setListData(new User[0]);
            return;
        }

        try {
            List<User> users = visitManager.getUsersWithAppointments(
                    from,
                    to
            );

            if (users == null) {
                users = Collections.emptyList();
            }

            usersList.setListData(users.toArray(new User[0]));

        } catch (OHServiceException exception) {
            usersList.setListData(new User[0]);
            OHServiceExceptionUtil.showMessages(exception);
        }
    }

    private JPanel getButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton okButton = new JButton(
                MessageBundle.getMessage("angal.common.ok.btn"));

        okButton.setMnemonic(
                MessageBundle.getMnemonic("angal.common.ok.btn.key"));

        okButton.addActionListener(event -> {
            LocalDate from = dateFrom.getDate();
            LocalDate to = dateTo.getDate();

            if (from == null || to == null) {
                MessageDialog.error(
                        this,
                        "angal.visit.report.dates.required.msg");
                return;
            }

            if (from.isAfter(to)) {
                MessageDialog.error(
                        this,
                        "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            if (administrator
                    && !isAllUsersSelected()
                    && usersList.getSelectedValuesList().isEmpty()) {

                MessageDialog.error(
                        this,
                        "angal.visit.report.user.required.msg");
                return;
            }

            cancel = false;

            if (onOk != null) {
                onOk.run();
            }

            dispose();
        });

        JButton cancelButton = new JButton(
                MessageBundle.getMessage("angal.common.cancel.btn"));

        cancelButton.setMnemonic(
                MessageBundle.getMnemonic("angal.common.cancel.btn.key"));

        cancelButton.addActionListener(event -> dispose());

        panel.add(okButton);
        panel.add(cancelButton);

        return panel;
    }

    public LocalDateTime getDateFrom() {
        return dateFrom.getDateStartOfDay();
    }

    public LocalDateTime getDateTo() {
        return dateTo.getDateStartOfDay();
    }

    public boolean isAllUsersSelected() {
        return administrator
                && allUsersCheckBox != null
                && allUsersCheckBox.isSelected();
    }

    public List<String> getSelectedUserIds() {
        if (!administrator) {
            String currentUser = UserBrowsingManager.getCurrentUser();

            if (currentUser == null || currentUser.isBlank()) {
                return Collections.emptyList();
            }

            return List.of(currentUser);
        }
        if (isAllUsersSelected()) {
            return Collections.emptyList();
        }

        List<String> selectedUserIds = new ArrayList<>();

        for (User user : usersList.getSelectedValuesList()) {
            if (user != null && user.getUserName() != null) {
                selectedUserIds.add(user.getUserName());
            }
        }

        return selectedUserIds;
    }

    public boolean isCancel() {
        return cancel;
    }
}