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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.isf.generaldata.MessageBundle;

public class VisitsReportDialog extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private GoodDateChooser dateFrom;
    private GoodDateChooser dateTo;
    private boolean cancel = true;
    private Runnable onOk;

    public VisitsReportDialog(JFrame parent) {
        super();
        setTitle(MessageBundle.getMessage("angal.visits.report.dialog.title"));
        setContentPane(getContentPanel());
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 180));
    }

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    private JPanel getContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(getDatePanel(), BorderLayout.CENTER);
        panel.add(getButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel getDatePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.visits.report.dateinterval")));

        panel.add(new JLabel(MessageBundle.getMessage("angal.common.from.txt") + ":"));
        dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(1));
        panel.add(dateFrom);

        panel.add(new JLabel(MessageBundle.getMessage("angal.common.to.txt") + ":"));
        dateTo = new GoodDateChooser(LocalDate.now());
        panel.add(dateTo);

        return panel;
    }

    private JPanel getButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
        okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
        okButton.addActionListener(e -> {
            if (dateFrom.getDate().isAfter(dateTo.getDate())) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            cancel = false;

            if (onOk != null) {
                onOk.run();
            }

            dispose();
        });

        JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
        cancelButton.addActionListener(e -> dispose());

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

    public boolean isCancel() {
        return cancel;
    }
}
