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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.FamilyPlanningBrowserManager;
import org.isf.maternity.manager.FamilyPlanningVisitBrowserManager;
import org.isf.maternity.model.FPStatus;
import org.isf.maternity.model.FamilyPlanning;
import org.isf.maternity.model.FamilyPlanningVisit;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;

import org.springframework.data.domain.Page;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class FamilyPlanningBrowser extends ModalJFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] columnHeaders = {
            MessageBundle.getMessage("angal.familyplanning.id.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.code.txt.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
            MessageBundle.getMessage("angal.familyplanning.method.col").toUpperCase(),
            MessageBundle.getMessage("angal.familyplanning.status.col").toUpperCase(),
            MessageBundle.getMessage("angal.maternity.lmp.col").toUpperCase()
    };

    private final int[] columnWidths = { 50, 70, 150, 100, 90, 120 };

    private final String[] visitColumns = {
            MessageBundle.getMessage("angal.familyplanning.visitdate.col").toUpperCase(),
            MessageBundle.getMessage("angal.familyplanning.visittype.col").toUpperCase(),
            MessageBundle.getMessage("angal.familyplanning.visitnotes.col").toUpperCase()
    };

    private final int[] visitColumnWidths = { 130, 120, 350 };

    private final FamilyPlanningBrowser myFrame;
    private List<FamilyPlanning> fpList = new ArrayList<>();
    private List<FamilyPlanningVisit> visitList = new ArrayList<>();

    private FamilyPlanningBrowserManager fpManager;
    private FamilyPlanningVisitBrowserManager visitManager;

    private JTable fpTable;
    private JTable visitTable;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox<Integer> pagesCombo;
    private JLabel underLabel;
    private JLabel totalLabel;
    private FPTableModel model;
    private int TOTAL_PAGES = 0;
    private int CURRENT_PAGE = 1;
    private long TOTAL_RECORDS = 0;

    private JTextField patientCodeFilter;
    private GoodDateChooser dateFrom;
    private GoodDateChooser dateTo;
    private JComboBox<Object> methodFilterCombo;
    private JComboBox<Object> statusFilterCombo;
    private JButton searchButton;
    private JButton resetButton;

    private FamilyPlanning selectedFP;
    private int selectedVisitRow = -1;

    private List<Typology> methodTypologies = new ArrayList<>();

    public FamilyPlanningBrowser() {
        setTitle(MessageBundle.getMessage("angal.familyplanning.browser.title"));
        myFrame = this;
        initManagers();
        initComponents();
        pack();
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(bounds);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        addCloseListener();
    }

    private void addCloseListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void initManagers() {
        fpManager = Context.getApplicationContext().getBean(FamilyPlanningBrowserManager.class);
        visitManager = Context.getApplicationContext().getBean(FamilyPlanningVisitBrowserManager.class);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        loadMethodTypologies();
        initializeStatusCombo();

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setTopComponent(getTopPanel());
        mainSplit.setBottomComponent(getBottomPanel());
        mainSplit.setResizeWeight(0.6);

        add(mainSplit, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);

        performSearch();
    }

    private void loadMethodTypologies() {
        try {
            methodTypologies = Context.getApplicationContext()
                    .getBean(TypologyBrowserManager.class)
                    .getTypologies(Family.FAMILYPLANNINGMETHODTYPE);
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
            methodTypologies = new ArrayList<>();
        }
    }

    private void initializeStatusCombo() {
        statusFilterCombo = new JComboBox<>();
        statusFilterCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (FPStatus status : FPStatus.values()) {
            statusFilterCombo.addItem(status);
        }
        statusFilterCombo.setRenderer(new EnumRenderer());
    }

    private class EnumRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Typology typology) {
                return super.getListCellRendererComponent(list, typology.getDescription(),
                        index, isSelected, cellHasFocus);
            }
            if (value instanceof FPStatus status) {
                return super.getListCellRendererComponent(list, MessageBundle.getMessage(status.getKey()),
                        index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private JPanel getTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getFilterPanel(), BorderLayout.WEST);
        topPanel.add(getFPListPanel(), BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.setMinimumSize(new Dimension(280, 350));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.familyplanning.patient.filter") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        JPanel dateIntervalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        dateIntervalPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.familyplanning.date.interval.label")));
        dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        dateTo = new GoodDateChooser(LocalDate.now());
        dateIntervalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        dateIntervalPanel.add(dateFrom);
        dateIntervalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        dateIntervalPanel.add(dateTo);
        filterPanel.add(dateIntervalPanel);

        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.familyplanning.method.filter")));
        methodFilterCombo = new JComboBox<>();
        methodFilterCombo.addItem(null);
        if (methodTypologies != null) {
            for (Typology typology : methodTypologies) {
                methodFilterCombo.addItem(typology);
            }
        }
        methodFilterCombo.setRenderer(new EnumRenderer());
        methodPanel.add(methodFilterCombo);
        filterPanel.add(methodPanel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.familyplanning.status.filter")));
        statusPanel.add(statusFilterCombo);
        filterPanel.add(statusPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
        searchButton.addActionListener(e -> performSearch());
        resetButton = new JButton(MessageBundle.getMessage("angal.common.reset.btn"));
        resetButton.addActionListener(e -> resetFilters());
        buttonPanel.add(searchButton);
        buttonPanel.add(resetButton);
        filterPanel.add(buttonPanel);

        return filterPanel;
    }

    private JPanel getFPListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getFPTablePanel(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getFPTablePanel() {
        model = new FPTableModel();
        fpTable = new JTable(model);
        fpTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fpTable.setAutoCreateRowSorter(true);
        fpTable.setRowHeight(20);
        fpTable.setFillsViewportHeight(true);
        fpTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        for (int i = 0; i < columnHeaders.length; i++) {
            fpTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        fpTable.getSelectionModel().addListSelectionListener(new FPTableListener());

        return new JScrollPane(fpTable);
    }

    private JPanel getPaginationPanel() {
        JPanel paginatePanel = new JPanel(new WrapLayout());
        paginatePanel.add(getPrevButton());
        paginatePanel.add(getPagesCombo());
        paginatePanel.add(getUnderLabel());
        paginatePanel.add(getNextButton());
        paginatePanel.add(getTotalLabel());
        return paginatePanel;
    }

    private JButton getNextButton() {
        if (nextButton == null) {
            nextButton = new JButton(">");
            nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES != 1);
            nextButton.addActionListener(actionEvent -> {
                if (CURRENT_PAGE < TOTAL_PAGES) {
                    CURRENT_PAGE++;
                    performSearch();
                }
            });
        }
        return nextButton;
    }

    private JButton getPrevButton() {
        if (prevButton == null) {
            prevButton = new JButton("<");
            prevButton.setEnabled(CURRENT_PAGE > 1);
            prevButton.addActionListener(actionEvent -> {
                if (CURRENT_PAGE > 1) {
                    CURRENT_PAGE--;
                    performSearch();
                }
            });
        }
        return prevButton;
    }

    private JComboBox<Integer> getPagesCombo() {
        if (pagesCombo == null) {
            pagesCombo = new JComboBox<>();
            pagesCombo.setPreferredSize(new Dimension(100, 25));
            pagesCombo.addActionListener(actionEvent -> {
                if (pagesCombo.getSelectedItem() != null) {
                    CURRENT_PAGE = (Integer) pagesCombo.getSelectedItem();
                    performSearch();
                }
            });
        }
        return pagesCombo;
    }

    private JLabel getUnderLabel() {
        if (underLabel == null) {
            underLabel = new JLabel("/ 0 " + MessageBundle.getMessage("angal.common.page.label"));
            underLabel.setPreferredSize(new Dimension(80, 30));
        }
        return underLabel;
    }

    private JLabel getTotalLabel() {
        if (totalLabel == null) {
            totalLabel = new JLabel(MessageBundle.getMessage("angal.familyplanning.total.label") + ": 0");
        }
        return totalLabel;
    }

    private JPanel getBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(getVisitListPanel(), BorderLayout.CENTER);
        return bottomPanel;
    }

    private JScrollPane getVisitListPanel() {
        visitTable = new JTable(new FPVisitsTableModel());
        visitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        visitTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedVisitRow = visitTable.getSelectedRow();
            }
        });

        for (int i = 0; i < visitColumns.length; i++) {
            visitTable.getColumnModel().getColumn(i).setPreferredWidth(visitColumnWidths[i]);
        }

        JScrollPane visitScrollPane = new JScrollPane(visitTable);
        visitScrollPane.setPreferredSize(new Dimension(600, 200));
        return visitScrollPane;
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.actions.label")));

        if (MainMenu.checkUserGrants("familyplanning.new")) {
            buttonPanel.add(getNewFPButton());
        }
        if (MainMenu.checkUserGrants("familyplanning.update")) {
            buttonPanel.add(getUpdateFPButton());
        }
        if (MainMenu.checkUserGrants("familyplanning.update")) {
            buttonPanel.add(getStopFPButton());
        }
        if (MainMenu.checkUserGrants("familyplanning.delete")) {
            buttonPanel.add(getDeleteFPButton());
        }
        if (MainMenu.checkUserGrants("familyplanning.newvisit")) {
            buttonPanel.add(getNewVisitButton());
        }
        if (MainMenu.checkUserGrants("familyplanning.updatevisit")) {
            buttonPanel.add(getUpdateVisitButton());
        }
        if (MainMenu.checkUserGrants("familyplanning.deletevisit")) {
            buttonPanel.add(getDeleteVisitButton());
        }
        buttonPanel.add(getCloseButton());

        return buttonPanel;
    }

    private JButton getNewFPButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.new.btn"));
        button.addActionListener(e -> newFP());
        return button;
    }

    private JButton getUpdateFPButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.update.btn"));
        button.addActionListener(e -> updateFP());
        return button;
    }

    private JButton getStopFPButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.stop.btn"));
        button.addActionListener(e -> stopFP());
        return button;
    }

    private JButton getDeleteFPButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.delete.btn"));
        button.addActionListener(e -> deleteFP());
        return button;
    }

    private JButton getNewVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.newvisit.btn"));
        button.addActionListener(e -> newVisit());
        return button;
    }

    private JButton getUpdateVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.updatevisit.btn"));
        button.addActionListener(e -> updateVisit());
        return button;
    }

    private JButton getDeleteVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.familyplanning.deletevisit.btn"));
        button.addActionListener(e -> deleteVisit());
        return button;
    }

    private JButton getCloseButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
        button.addActionListener(e -> dispose());
        return button;
    }

    private void performSearch() {
        try {
            String patientCode = patientCodeFilter.getText().trim();
            LocalDate dateBegin = dateFrom.getDate();
            LocalDate dateEnd = dateTo.getDate();

            if (dateBegin.isAfter(dateEnd)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            Object selectedMethod = methodFilterCombo.getSelectedItem();
            Object selectedStatus = statusFilterCombo.getSelectedItem();

            String methodCode = null;
            if (selectedMethod instanceof Typology typology) {
                methodCode = typology.getCode();
            }

            FPStatus status = null;
            if (selectedStatus != null && !selectedStatus.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                status = (FPStatus) selectedStatus;
            }

            Integer patientCodeInt = null;
            if (!patientCode.isEmpty()) {
                try {
                    patientCodeInt = Integer.parseInt(patientCode);
                } catch (NumberFormatException e) {
                    MessageDialog.error(this, "angal.common.pleaseinsertavalidnumber.msg");
                    return;
                }
            }

            Page<FamilyPlanning> pagedResult = fpManager.searchFamilyPlannings(
                    patientCodeInt, methodCode, status,
                    dateBegin != null ? dateBegin.atStartOfDay() : null,
                    dateEnd != null ? dateEnd.atTime(23, 59, 59) : null,
                    CURRENT_PAGE - 1, GeneralData.PAGINATIONPAGESIZE);

            fpList = pagedResult.getContent();
            TOTAL_RECORDS = pagedResult.getTotalElements();
            TOTAL_PAGES = pagedResult.getTotalPages();

            updatePaginationUI();
            model.fireTableDataChanged();
            fpTable.updateUI();

            selectedFP = null;
            visitList = new ArrayList<>();
            ((FPVisitsTableModel) visitTable.getModel()).fireTableDataChanged();

        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void updatePaginationUI() {
        underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.page.label"));
        totalLabel.setText(MessageBundle.getMessage("angal.familyplanning.total.label") + ": " + TOTAL_RECORDS);

        ActionListener[] listeners = pagesCombo.getActionListeners();
        for (ActionListener listener : listeners) {
            pagesCombo.removeActionListener(listener);
        }

        pagesCombo.removeAllItems();
        for (int i = 1; i <= TOTAL_PAGES; i++) {
            pagesCombo.addItem(i);
        }
        if (TOTAL_PAGES > 0) {
            pagesCombo.setSelectedItem(CURRENT_PAGE);
        }

        for (ActionListener listener : listeners) {
            pagesCombo.addActionListener(listener);
        }

        nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES && TOTAL_PAGES > 1);
        prevButton.setEnabled(CURRENT_PAGE > 1);
    }

    private void resetFilters() {
        patientCodeFilter.setText("");
        dateFrom.setDate(LocalDate.now().minusMonths(6));
        dateTo.setDate(LocalDate.now());
        methodFilterCombo.setSelectedIndex(0);
        statusFilterCombo.setSelectedIndex(0);
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void loadVisits() {
        if (selectedFP == null) {
            visitList = new ArrayList<>();
            ((FPVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
            return;
        }

        try {
            List<FamilyPlanningVisit> visits = visitManager.getVisitsByFamilyPlanning(selectedFP.getId());
            visitList = visits != null ? visits : new ArrayList<>();
            ((FPVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
            selectedVisitRow = -1;
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
            visitList = new ArrayList<>();
            ((FPVisitsTableModel) visitTable.getModel()).fireTableDataChanged();
        }
    }

    private void newFP() {
        FamilyPlanningEdit edit = new FamilyPlanningEdit(this, (Patient) null, true);
        edit.addFamilyPlanningListener(new FamilyPlanningEdit.FamilyPlanningListener() {
            @Override
            public void fpInserted(AWTEvent e, FamilyPlanning fp) {
                performSearch();
            }

            @Override
            public void fpUpdated(AWTEvent e, FamilyPlanning fp) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void updateFP() {
        if (selectedFP == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        FamilyPlanningEdit edit = new FamilyPlanningEdit(this, selectedFP, false);
        edit.addFamilyPlanningListener(new FamilyPlanningEdit.FamilyPlanningListener() {
            @Override
            public void fpInserted(AWTEvent e, FamilyPlanning fp) {
                performSearch();
            }

            @Override
            public void fpUpdated(AWTEvent e, FamilyPlanning fp) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void stopFP() {
        if (selectedFP == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedFP.getStatus() != FPStatus.ACTIVE) {
            MessageDialog.error(this, "angal.common.onlyactivecanbestopped.msg");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                MessageBundle.getMessage("angal.familyplanning.stop.confirm.msg"),
                MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                String reason = JOptionPane.showInputDialog(this,
                        MessageBundle.getMessage("angal.familyplanning.stopreason.label"));
                if (reason != null) {
                    fpManager.stopFamilyPlanning(selectedFP.getId(), LocalDateTime.now(), reason);
                    performSearch();
                }
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void deleteFP() {
        if (selectedFP == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                MessageBundle.getMessage("angal.familyplanning.delete.confirm.msg"),
                MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                fpManager.deleteFamilyPlanning(selectedFP);
                performSearch();
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void newVisit() {
        if (selectedFP == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        FamilyPlanningVisitEdit edit = new FamilyPlanningVisitEdit(this, selectedFP, true);
        edit.addFPVisitListener(new FamilyPlanningVisitEdit.FPVisitListener() {
            @Override
            public void visitInserted(AWTEvent e, FamilyPlanningVisit visit) {
                loadVisits();
            }

            @Override
            public void visitUpdated(AWTEvent e, FamilyPlanningVisit visit) {
                loadVisits();
            }
        });
        edit.setVisible(true);
    }

    private void updateVisit() {
        if (selectedFP == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        FamilyPlanningVisit visit = visitList.get(selectedVisitRow);
        FamilyPlanningVisitEdit edit = new FamilyPlanningVisitEdit(this, visit, false);
        edit.addFPVisitListener(new FamilyPlanningVisitEdit.FPVisitListener() {
            @Override
            public void visitInserted(AWTEvent e, FamilyPlanningVisit visit) {
                loadVisits();
            }

            @Override
            public void visitUpdated(AWTEvent e, FamilyPlanningVisit visit) {
                loadVisits();
            }
        });
        edit.setVisible(true);
    }

    private void deleteVisit() {
        if (selectedFP == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                MessageBundle.getMessage("angal.familyplanning.deletevisit.confirm.msg"),
                MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                visitManager.deleteVisit(visitList.get(selectedVisitRow));
                loadVisits();
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private String getEnumDisplayName(Object enumValue) {
        if (enumValue instanceof FPStatus status) {
            return MessageBundle.getMessage(status.getKey());
        }
        if (enumValue instanceof Typology typology) {
            return typology.getDescription();
        }
        return "";
    }

    class FPTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public int getRowCount() {
            return fpList != null ? fpList.size() : 0;
        }

        @Override
        public String getColumnName(int c) {
            return columnHeaders[c];
        }

        @Override
        public int getColumnCount() {
            return columnHeaders.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (fpList == null || r >= fpList.size()) {
                return null;
            }

            FamilyPlanning fp = fpList.get(r);
            Patient patient = fp.getPatient();

            if (c == -1) {
                return fp;
            } else if (c == 0) {
                return fp.getId();
            } else if (c == 1) {
                return patient != null ? patient.getCode() : "";
            } else if (c == 2) {
                return patient != null ? patient.getSecondName() + " " + patient.getFirstName() : "";
            } else if (c == 3) {
                return fp.getCurrentMethod() != null ? fp.getCurrentMethod().getDescription() : "";
            } else if (c == 4) {
                return fp.getStatus() != null ? getEnumDisplayName(fp.getStatus()) : "";
            } else if (c == 5) {
                return fp.getRegistrationDate() != null ? fp.getRegistrationDate().format(formatter) : "";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    class FPVisitsTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Override
        public String getColumnName(int c) {
            return visitColumns[c];
        }

        @Override
        public int getColumnCount() {
            return visitColumns.length;
        }

        @Override
        public int getRowCount() {
            return visitList != null ? visitList.size() : 0;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (visitList == null || r >= visitList.size()) {
                return null;
            }

            FamilyPlanningVisit visit = visitList.get(r);

            if (c == -1) {
                return visit;
            } else if (c == 0) {
                return visit.getVisitDate() != null ? visit.getVisitDate().format(formatter) : "";
            } else if (c == 1) {
                return visit.getVisitType() != null ? getEnumDisplayName(visit.getVisitType()) : "";
            } else if (c == 2) {
                String notes = visit.getNotes();
                if (notes != null && notes.length() > 50) {
                    notes = notes.substring(0, 50) + "...";
                }
                return notes != null ? notes : "";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    class FPTableListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            int viewRow = fpTable.getSelectedRow();
            if (!arg0.getValueIsAdjusting() && viewRow > -1) {
                int modelRow = fpTable.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < fpList.size()) {
                    selectedFP = fpList.get(modelRow);
                    selectedVisitRow = -1;
                    loadVisits();
                }
            }
        }
    }
}
