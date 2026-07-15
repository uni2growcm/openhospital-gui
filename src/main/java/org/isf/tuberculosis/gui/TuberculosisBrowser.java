/*
 * Open Hospital (www.open-hospital.org)
 * Copyright Â© 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.LabNew;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.tuberculosis.manager.TuberculosisContactManager;
import org.isf.tuberculosis.manager.TuberculosisTreatmentManager;
import org.isf.tuberculosis.manager.TuberculosisVisitManager;
import org.isf.tuberculosis.model.Classification;
import org.isf.tuberculosis.model.DiseaseLocation;
import org.isf.tuberculosis.model.DotStatus;
import org.isf.tuberculosis.model.TuberculosisContact;
import org.isf.tuberculosis.model.TuberculosisTreatment;
import org.isf.tuberculosis.model.TreatmentStatus;
import org.isf.tuberculosis.model.TuberculosisVisit;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.stat.gui.report.GenericReportTuberculosis;
import org.isf.utils.jobjects.TuberculosisReportDialog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

public class TuberculosisBrowser extends ModalJFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] columnHeaders = {
            MessageBundle.getMessage("angal.tb.browser.id.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.code.txt.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.classification.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.location.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.status.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.startdate.col").toUpperCase()
    };

    private final int[] columnWidths = { 50, 70, 150, 100, 120, 90, 100 };

    private final String[] visitColumns = {
            MessageBundle.getMessage("angal.tb.browser.visitdate.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.dotstatus.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.visit.adherence").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.visitnotes.col").toUpperCase()
    };

    private final int[] visitColumnWidths = { 130, 100, 100, 350 };

    private final String[] contactColumns = {
            MessageBundle.getMessage("angal.tb.browser.contactname.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.contactage.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.contactgender.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.contactscreened.col").toUpperCase(),
            MessageBundle.getMessage("angal.tb.browser.contactnotes.col").toUpperCase()
    };

    private final int[] contactColumnWidths = { 150, 60, 60, 80, 350 };

    private final TuberculosisBrowser myFrame;
    private List<TuberculosisTreatment> treatmentList = new ArrayList<>();
    private List<TuberculosisVisit> visitList = new ArrayList<>();
    private List<TuberculosisContact> contactList = new ArrayList<>();

    private TuberculosisTreatmentManager manager;
    private TuberculosisVisitManager visitManager;
    private TuberculosisContactManager contactManager;
    private ExaminationBrowserManager examinationManager;

    private JTable treatmentTable;
    private JTable visitTable;
    private JTable contactTable;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox<Integer> pagesCombo;
    private JLabel underLabel;
    private JLabel totalLabel;
    private TreatmentTableModel model;
    private int TOTAL_PAGES = 0;
    private int CURRENT_PAGE = 1;
    private long TOTAL_RECORDS = 0;

    private JTextField patientCodeFilter;
    private GoodDateChooser dateFrom;
    private GoodDateChooser dateTo;
    private GoodDateChooser startDateFrom;
    private GoodDateChooser startDateTo;
    private JComboBox<Object> statusFilterCombo;
    private JComboBox<Object> classificationFilterCombo;
    private JComboBox<Object> diseaseLocationFilterCombo;
    private JButton searchButton;
    private JButton resetButton;

    private GoodDateChooser visitDateFrom;
    private GoodDateChooser visitDateTo;
    private JComboBox<Object> visitDotStatusCombo;
    private JTextField visitAdherenceMinField;

    private JTextField contactNameField;
    private JTextField contactAgeFromField;
    private JTextField contactAgeToField;
    private JComboBox<String> contactScreenedCombo;

    private TuberculosisTreatment selectedTreatment;
    private int selectedVisitRow = -1;
    private int selectedContactRow = -1;

    private JTabbedPane detailTabbedPane;

    public TuberculosisBrowser() {
        setTitle(MessageBundle.getMessage("angal.tb.browser.title"));
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
        manager = Context.getApplicationContext().getBean(TuberculosisTreatmentManager.class);
        visitManager = Context.getApplicationContext().getBean(TuberculosisVisitManager.class);
        contactManager = Context.getApplicationContext().getBean(TuberculosisContactManager.class);
        examinationManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setTopComponent(getTopPanel());
        mainSplit.setBottomComponent(getBottomPanel());
        mainSplit.setResizeWeight(0.6);

        add(mainSplit, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);

        performSearch();
    }

    private JPanel getTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(getFilterPanel(), BorderLayout.WEST);
        topPanel.add(getTreatmentListPanel(), BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel getFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.setMinimumSize(new Dimension(280, 350));

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.patientfilter") + ":"));
        patientCodeFilter = new JTextField(10);
        codePanel.add(patientCodeFilter);
        filterPanel.add(codePanel);

        JPanel dateIntervalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        dateIntervalPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.browser.dateinterval.label")));
        dateFrom = new GoodDateChooser(LocalDate.now().minusMonths(6));
        dateTo = new GoodDateChooser(LocalDate.now());
        dateIntervalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        dateIntervalPanel.add(dateFrom);
        dateIntervalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        dateIntervalPanel.add(dateTo);
        filterPanel.add(dateIntervalPanel);

        JPanel startDateIntervalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        startDateIntervalPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.browser.startdateinterval.label")));
        startDateFrom = new GoodDateChooser((LocalDate) null);
        startDateTo = new GoodDateChooser((LocalDate) null);
        startDateIntervalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        startDateIntervalPanel.add(startDateFrom);
        startDateIntervalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        startDateIntervalPanel.add(startDateTo);
        filterPanel.add(startDateIntervalPanel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.browser.statusfilter")));
        statusFilterCombo = new JComboBox<>();
        statusFilterCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (TreatmentStatus status : TreatmentStatus.values()) {
            statusFilterCombo.addItem(status);
        }
        statusFilterCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
                if (value instanceof TreatmentStatus status) {
                    return super.getListCellRendererComponent(list,
                            MessageBundle.getMessage(status.getKey()), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        statusPanel.add(statusFilterCombo);
        filterPanel.add(statusPanel);

        JPanel classificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        classificationPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.treatment.classification")));
        classificationFilterCombo = new JComboBox<>();
        classificationFilterCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (Classification cl : Classification.values()) {
            classificationFilterCombo.addItem(cl);
        }
        classificationFilterCombo.setRenderer(new EnumRenderer());
        classificationPanel.add(classificationFilterCombo);
        filterPanel.add(classificationPanel);

        JPanel diseaseLocationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diseaseLocationPanel.setBorder(BorderFactory.createTitledBorder(
                MessageBundle.getMessage("angal.tb.treatment.diseaselocation")));
        diseaseLocationFilterCombo = new JComboBox<>();
        diseaseLocationFilterCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (DiseaseLocation dl : DiseaseLocation.values()) {
            diseaseLocationFilterCombo.addItem(dl);
        }
        diseaseLocationFilterCombo.setRenderer(new EnumRenderer());
        diseaseLocationPanel.add(diseaseLocationFilterCombo);
        filterPanel.add(diseaseLocationPanel);

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

    private JPanel getTreatmentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getTreatmentTablePanel(), BorderLayout.CENTER);
        panel.add(getPaginationPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane getTreatmentTablePanel() {
        model = new TreatmentTableModel();
        treatmentTable = new JTable(model);
        treatmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treatmentTable.setAutoCreateRowSorter(true);
        treatmentTable.setRowHeight(20);
        treatmentTable.setFillsViewportHeight(true);
        treatmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        for (int i = 0; i < columnHeaders.length; i++) {
            treatmentTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        treatmentTable.getSelectionModel().addListSelectionListener(new TreatmentTableListener());

        return new JScrollPane(treatmentTable);
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
            totalLabel = new JLabel(MessageBundle.getMessage("angal.tb.browser.total.label") + ": 0");
        }
        return totalLabel;
    }

    private JPanel getBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        detailTabbedPane = new JTabbedPane();
        detailTabbedPane.addTab(MessageBundle.getMessage("angal.tb.browser.visitstab"), getVisitListPanel());
        detailTabbedPane.addTab(MessageBundle.getMessage("angal.tb.browser.contactstab"), getContactListPanel());
        bottomPanel.add(detailTabbedPane, BorderLayout.CENTER);
        return bottomPanel;
    }

    private JPanel getVisitListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.datefrom.label")));
        visitDateFrom = new GoodDateChooser((LocalDate) null);
        filterPanel.add(visitDateFrom);
        filterPanel.add(Box.createVerticalStrut(5));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateto.label")));
        visitDateTo = new GoodDateChooser((LocalDate) null);
        filterPanel.add(visitDateTo);
        filterPanel.add(Box.createVerticalStrut(5));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.dotstatus.col")));
        visitDotStatusCombo = new JComboBox<>();
        visitDotStatusCombo.addItem(MessageBundle.getMessage("angal.common.all.label"));
        for (DotStatus ds : DotStatus.values()) {
            visitDotStatusCombo.addItem(ds);
        }
        visitDotStatusCombo.setRenderer(new EnumRenderer());
        filterPanel.add(visitDotStatusCombo);
        filterPanel.add(Box.createVerticalStrut(5));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.visit.adherence")));
        visitAdherenceMinField = new JTextField(5);
        filterPanel.add(visitAdherenceMinField);
        filterPanel.add(Box.createVerticalStrut(10));
        JButton visitFilterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
        visitFilterButton.addActionListener(e -> loadVisits());
        filterPanel.add(visitFilterButton);
        filterPanel.add(Box.createVerticalStrut(5));
        JButton visitResetButton = new JButton(MessageBundle.getMessage("angal.common.reset.btn"));
        visitResetButton.addActionListener(e -> {
            visitDateFrom.setDate((LocalDate) null);
            visitDateTo.setDate((LocalDate) null);
            visitDotStatusCombo.setSelectedIndex(0);
            visitAdherenceMinField.setText("");
            loadVisits();
        });
        filterPanel.add(visitResetButton);

        panel.add(filterPanel, BorderLayout.WEST);

        visitTable = new JTable(new VisitTableModel());
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
        panel.add(visitScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getContactListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.filter.label")));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.contactname.col")));
        contactNameField = new JTextField(10);
        filterPanel.add(contactNameField);
        filterPanel.add(Box.createVerticalStrut(5));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.contactage.col") + " " + MessageBundle.getMessage("angal.common.from.label")));
        contactAgeFromField = new JTextField(5);
        filterPanel.add(contactAgeFromField);
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.common.to.label")));
        contactAgeToField = new JTextField(5);
        filterPanel.add(contactAgeToField);
        filterPanel.add(Box.createVerticalStrut(5));
        filterPanel.add(new JLabel(MessageBundle.getMessage("angal.tb.browser.contactscreened.col")));
        contactScreenedCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.common.all.label"),
                MessageBundle.getMessage("angal.common.yes"),
                MessageBundle.getMessage("angal.common.no")
        });
        filterPanel.add(contactScreenedCombo);
        filterPanel.add(Box.createVerticalStrut(10));
        JButton contactFilterButton = new JButton(MessageBundle.getMessage("angal.common.search.btn"));
        contactFilterButton.addActionListener(e -> loadContacts());
        filterPanel.add(contactFilterButton);
        filterPanel.add(Box.createVerticalStrut(5));
        JButton contactResetButton = new JButton(MessageBundle.getMessage("angal.common.reset.btn"));
        contactResetButton.addActionListener(e -> {
            contactNameField.setText("");
            contactAgeFromField.setText("");
            contactAgeToField.setText("");
            contactScreenedCombo.setSelectedIndex(0);
            loadContacts();
        });
        filterPanel.add(contactResetButton);

        panel.add(filterPanel, BorderLayout.WEST);

        contactTable = new JTable(new ContactTableModel());
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        contactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedContactRow = contactTable.getSelectedRow();
            }
        });

        for (int i = 0; i < contactColumns.length; i++) {
            contactTable.getColumnModel().getColumn(i).setPreferredWidth(contactColumnWidths[i]);
        }

        JScrollPane contactScrollPane = new JScrollPane(contactTable);
        contactScrollPane.setPreferredSize(new Dimension(600, 200));
        panel.add(contactScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton getReportButton() {
        JButton button = new JButton( MessageBundle.getMessage("angal.common.report.btn"));
        button.addActionListener(e -> report());
        return button;
    }

    private void report() { TuberculosisReportDialog dialog = new TuberculosisReportDialog(this);
        dialog.setOnOk(() -> new GenericReportTuberculosis(dialog.getDateFrom().toLocalDate(), dialog.getDateTo().toLocalDate(), "TuberculosisReport"));

        dialog.setVisible(true);
    }
    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.actions.label")));

        if (MainMenu.checkUserGrants("tuberculosis.new")) {
            buttonPanel.add(getNewTreatmentButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.update")) {
            buttonPanel.add(getUpdateTreatmentButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.delete")) {
            buttonPanel.add(getDeleteTreatmentButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.newvisit")) {
            buttonPanel.add(getNewVisitButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.updatevisit")) {
            buttonPanel.add(getUpdateVisitButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.deletevisit")) {
            buttonPanel.add(getDeleteVisitButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.newcontact")) {
            buttonPanel.add(getNewContactButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.updatecontact")) {
            buttonPanel.add(getUpdateContactButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.deletecontact")) {
            buttonPanel.add(getDeleteContactButton());
        }
        if (MainMenu.checkUserGrants("tuberculosis.report")) {
            buttonPanel.add(getReportButton());
        }
        buttonPanel.add(getExaminationButton());
        buttonPanel.add(getLaboratoryButton());
        buttonPanel.add(getCloseButton());

        return buttonPanel;
    }

    private JButton getNewTreatmentButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.newtreatment.btn"));
        button.addActionListener(e -> newTreatment());
        return button;
    }

    private JButton getUpdateTreatmentButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.edittreatment.btn"));
        button.addActionListener(e -> updateTreatment());
        return button;
    }

    private JButton getDeleteTreatmentButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
        button.addActionListener(e -> deleteTreatment());
        return button;
    }

    private JButton getNewVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.newvisit.btn"));
        button.addActionListener(e -> newVisit());
        return button;
    }

    private JButton getUpdateVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.editvisit.btn"));
        button.addActionListener(e -> updateVisit());
        return button;
    }

    private JButton getDeleteVisitButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.deletevisit.btn"));
        button.addActionListener(e -> deleteVisit());
        return button;
    }

    private JButton getNewContactButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.newcontact.btn"));
        button.addActionListener(e -> newContact());
        return button;
    }

    private JButton getUpdateContactButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.editcontact.btn"));
        button.addActionListener(e -> updateContact());
        return button;
    }

    private JButton getDeleteContactButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.deletecontact.btn"));
        button.addActionListener(e -> deleteContact());
        return button;
    }

    private JButton getExaminationButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.examination.btn"));
        button.addActionListener(e -> {
            if (selectedTreatment == null) {
                MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
                return;
            }
            Patient patient = selectedTreatment.getPatient();
            if (patient == null) {
                MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
                return;
            }
            PatientExamination patex;
            PatientExamination lastPatex = null;
            try {
                lastPatex = examinationManager.getLastByPatID(patient.getCode());
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
            if (lastPatex != null) {
                patex = examinationManager.getFromLastPatientExamination(lastPatex);
            } else {
                patex = examinationManager.getDefaultPatientExamination(patient);
            }
            GenderPatientExamination gpatex = new GenderPatientExamination(patex, patient.getSex() == 'M');
            PatientExaminationEdit dialog = new PatientExaminationEdit(this, gpatex);
            dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.showAsModal(this);
        });
        return button;
    }

    private JButton getLaboratoryButton() {
        JButton button = new JButton(MessageBundle.getMessage("angal.tb.browser.laboratory.btn"));
        button.addActionListener(e -> {
            if (selectedTreatment == null) {
                MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
                return;
            }
            Patient patient = selectedTreatment.getPatient();
            if (patient == null) {
                MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
                return;
            }
            LabNew labNew = new LabNew(myFrame, patient);
            labNew.setVisible(true);
        });
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

            if (dateBegin != null && dateEnd != null && dateBegin.isAfter(dateEnd)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            LocalDate startBegin = startDateFrom.getDate();
            LocalDate startEnd = startDateTo.getDate();

            if (startBegin != null && startEnd != null && startBegin.isAfter(startEnd)) {
                MessageDialog.error(this, "angal.common.datefrommustbebeforedateto.msg");
                return;
            }

            Object selectedStatus = statusFilterCombo.getSelectedItem();
            Object selectedClassification = classificationFilterCombo.getSelectedItem();
            Object selectedLocation = diseaseLocationFilterCombo.getSelectedItem();

            TreatmentStatus status = null;
            if (selectedStatus != null && !selectedStatus.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                status = (TreatmentStatus) selectedStatus;
            }

            Classification classification = null;
            if (selectedClassification != null && !selectedClassification.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                classification = (Classification) selectedClassification;
            }

            DiseaseLocation diseaseLocation = null;
            if (selectedLocation != null && !selectedLocation.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                diseaseLocation = (DiseaseLocation) selectedLocation;
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

            Page<TuberculosisTreatment> pagedResult = manager.getTreatmentsByFilters(
                    patientCodeInt, status, classification, diseaseLocation,
                    dateBegin, dateEnd, startBegin, startEnd,
                    PageRequest.of(CURRENT_PAGE - 1, GeneralData.PAGINATIONPAGESIZE));

            treatmentList = pagedResult.getContent();
            TOTAL_RECORDS = pagedResult.getTotalElements();
            TOTAL_PAGES = pagedResult.getTotalPages();

            updatePaginationUI();
            model.fireTableDataChanged();
            treatmentTable.updateUI();

            selectedTreatment = null;
            visitList = new ArrayList<>();
            contactList = new ArrayList<>();
            ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
            ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();

        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
        }
    }

    private void updatePaginationUI() {
        underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.page.label"));
        totalLabel.setText(MessageBundle.getMessage("angal.tb.browser.total.label") + ": " + TOTAL_RECORDS);

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
        startDateFrom.setDate((LocalDate) null);
        startDateTo.setDate((LocalDate) null);
        statusFilterCombo.setSelectedIndex(0);
        classificationFilterCombo.setSelectedIndex(0);
        diseaseLocationFilterCombo.setSelectedIndex(0);
        CURRENT_PAGE = 1;
        performSearch();
    }

    private void loadVisits() {
        if (selectedTreatment == null) {
            visitList = new ArrayList<>();
            ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
            return;
        }

        try {
            List<TuberculosisVisit> visits = visitManager.getVisitsByTreatmentId(selectedTreatment.getId());
            List<TuberculosisVisit> filtered = visits != null ? visits : new ArrayList<>();

            LocalDate filterDateFrom = visitDateFrom.getDate();
            LocalDate filterDateTo = visitDateTo.getDate();
            Object selectedDot = visitDotStatusCombo.getSelectedItem();
            DotStatus filterDotStatus = null;
            if (selectedDot != null && !selectedDot.equals(MessageBundle.getMessage("angal.common.all.label"))) {
                filterDotStatus = (DotStatus) selectedDot;
            }
            String adherenceText = visitAdherenceMinField.getText().trim();
            Integer minAdherence = null;
            if (!adherenceText.isEmpty()) {
                try {
                    minAdherence = Integer.parseInt(adherenceText);
                } catch (NumberFormatException ignored) {
                }
            }

            if (filterDateFrom != null || filterDateTo != null || filterDotStatus != null || minAdherence != null) {
                Integer finalMinAdherence = minAdherence;
                DotStatus finalFilterDotStatus = filterDotStatus;
                LocalDate finalFilterDateFrom = filterDateFrom;
                LocalDate finalFilterDateTo = filterDateTo;
                filtered = filtered.stream().filter(v -> {
                    LocalDate visitDate = v.getVisitDate() != null ? v.getVisitDate().toLocalDate() : null;
                    if (finalFilterDateFrom != null && visitDate != null && visitDate.isBefore(finalFilterDateFrom))
                        return false;
                    if (finalFilterDateTo != null && visitDate != null && visitDate.isAfter(finalFilterDateTo))
                        return false;
                    if (finalFilterDotStatus != null && v.getDotStatus() != finalFilterDotStatus)
                        return false;
                    if (finalMinAdherence != null && v.getAdherence() != null && v.getAdherence() < finalMinAdherence)
                        return false;
                    return true;
                }).collect(Collectors.toList());
            }

            visitList = filtered;
            ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
            selectedVisitRow = -1;
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
            visitList = new ArrayList<>();
            ((VisitTableModel) visitTable.getModel()).fireTableDataChanged();
        }
    }

    private void loadContacts() {
        if (selectedTreatment == null) {
            contactList = new ArrayList<>();
            ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
            return;
        }

        try {
            List<TuberculosisContact> contacts = contactManager.getContactsByTreatmentId(selectedTreatment.getId());
            List<TuberculosisContact> filtered = contacts != null ? contacts : new ArrayList<>();

            String filterName = contactNameField.getText().trim().toLowerCase();
            String ageFromText = contactAgeFromField.getText().trim();
            String ageToText = contactAgeToField.getText().trim();
            Integer filterAgeFrom = null;
            Integer filterAgeTo = null;
            if (!ageFromText.isEmpty()) {
                try {
                    filterAgeFrom = Integer.parseInt(ageFromText);
                } catch (NumberFormatException ignored) {
                }
            }
            if (!ageToText.isEmpty()) {
                try {
                    filterAgeTo = Integer.parseInt(ageToText);
                } catch (NumberFormatException ignored) {
                }
            }
            Object selectedScreened = contactScreenedCombo.getSelectedItem();
            String screenedLabel = selectedScreened != null ? selectedScreened.toString() : "";
            Boolean filterScreened = null;
            if (screenedLabel.equals(MessageBundle.getMessage("angal.common.yes"))) {
                filterScreened = true;
            } else if (screenedLabel.equals(MessageBundle.getMessage("angal.common.no"))) {
                filterScreened = false;
            }

            if (!filterName.isEmpty() || filterAgeFrom != null || filterAgeTo != null || filterScreened != null) {
                String finalFilterName = filterName;
                Integer finalFilterAgeFrom = filterAgeFrom;
                Integer finalFilterAgeTo = filterAgeTo;
                Boolean finalFilterScreened = filterScreened;
                filtered = filtered.stream().filter(c -> {
                    if (!finalFilterName.isEmpty() && (c.getName() == null || !c.getName().toLowerCase().contains(finalFilterName)))
                        return false;
                    if (finalFilterAgeFrom != null && c.getAge() != null && c.getAge() < finalFilterAgeFrom)
                        return false;
                    if (finalFilterAgeTo != null && c.getAge() != null && c.getAge() > finalFilterAgeTo)
                        return false;
                    if (finalFilterScreened != null && c.getScreened() != finalFilterScreened)
                        return false;
                    return true;
                }).collect(Collectors.toList());
            }

            contactList = filtered;
            ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
            selectedContactRow = -1;
        } catch (OHServiceException ex) {
            OHServiceExceptionUtil.showMessages(ex);
            contactList = new ArrayList<>();
            ((ContactTableModel) contactTable.getModel()).fireTableDataChanged();
        }
    }

    private void newTreatment() {
        TuberculosisTreatmentEdit edit = new TuberculosisTreatmentEdit(this, (Patient) null, true);
        edit.addTuberculosisTreatmentListener(new TuberculosisTreatmentEdit.TuberculosisTreatmentListener() {
            @Override
            public void treatmentInserted(AWTEvent e, TuberculosisTreatment treatment) {
                performSearch();
            }

            @Override
            public void treatmentUpdated(AWTEvent e, TuberculosisTreatment treatment) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void updateTreatment() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        TuberculosisTreatmentEdit edit = new TuberculosisTreatmentEdit(this, selectedTreatment, false);
        edit.addTuberculosisTreatmentListener(new TuberculosisTreatmentEdit.TuberculosisTreatmentListener() {
            @Override
            public void treatmentInserted(AWTEvent e, TuberculosisTreatment treatment) {
                performSearch();
            }

            @Override
            public void treatmentUpdated(AWTEvent e, TuberculosisTreatment treatment) {
                performSearch();
            }
        });
        edit.setVisible(true);
    }

    private void deleteTreatment() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                MessageBundle.getMessage("angal.tb.browser.deletetreatment.confirm.msg"),
                MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                manager.deleteTreatment(selectedTreatment);
                performSearch();
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    private void newVisit() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        TuberculosisVisitEdit edit = new TuberculosisVisitEdit(this, selectedTreatment, true);
        edit.setVisible(true);
        loadVisits();
    }

    private void updateVisit() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        TuberculosisVisit visit = visitList.get(selectedVisitRow);
        TuberculosisVisitEdit edit = new TuberculosisVisitEdit(this, visit, false);
        edit.setVisible(true);
        loadVisits();
    }

    private void deleteVisit() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedVisitRow < 0 || selectedVisitRow >= visitList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                MessageBundle.getMessage("angal.tb.browser.deletevisit.confirm.msg"),
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

    private void newContact() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        TuberculosisContactEdit edit = new TuberculosisContactEdit(this, selectedTreatment, true);
        edit.setVisible(true);
        loadContacts();
    }

    private void updateContact() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedContactRow < 0 || selectedContactRow >= contactList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        TuberculosisContact contact = contactList.get(selectedContactRow);
        TuberculosisContactEdit edit = new TuberculosisContactEdit(this, contact, false);
        edit.setVisible(true);
        loadContacts();
    }

    private void deleteContact() {
        if (selectedTreatment == null) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        if (selectedContactRow < 0 || selectedContactRow >= contactList.size()) {
            MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                MessageBundle.getMessage("angal.tb.browser.deletecontact.confirm.msg"),
                MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                contactManager.deleteContact(contactList.get(selectedContactRow));
                loadContacts();
            } catch (OHServiceException ex) {
                OHServiceExceptionUtil.showMessages(ex);
            }
        }
    }

    class TreatmentTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public int getRowCount() {
            return treatmentList != null ? treatmentList.size() : 0;
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
            if (treatmentList == null || r >= treatmentList.size()) {
                return null;
            }

            TuberculosisTreatment treatment = treatmentList.get(r);
            Patient patient = treatment.getPatient();

            if (c == 0) {
                return treatment.getId();
            } else if (c == 1) {
                return patient != null ? patient.getCode() : "";
            } else if (c == 2) {
                return patient != null ? patient.getSecondName() + " " + patient.getFirstName() : "";
            } else if (c == 3) {
                return treatment.getClassification() != null ? treatment.getClassification().toString() : "";
            } else if (c == 4) {
                return treatment.getDiseaseLocation() != null ? treatment.getDiseaseLocation().toString() : "";
            } else if (c == 5) {
                return treatment.getStatus() != null ? treatment.getStatus().toString() : "";
            } else if (c == 6) {
                return treatment.getTreatmentStartDate() != null
                        ? treatment.getTreatmentStartDate().format(formatter) : "";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    class VisitTableModel extends DefaultTableModel {
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

            TuberculosisVisit visit = visitList.get(r);

            if (c == 0) {
                return visit.getVisitDate() != null ? visit.getVisitDate().format(formatter) : "";
            } else if (c == 1) {
                return visit.getDotStatus() != null ? visit.getDotStatus().toString() : "";
            } else if (c == 2) {
                return visit.getAdherence() != null ? visit.getAdherence().toString() : "";
            } else if (c == 3) {
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

    class ContactTableModel extends DefaultTableModel {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public String getColumnName(int c) {
            return contactColumns[c];
        }

        @Override
        public int getColumnCount() {
            return contactColumns.length;
        }

        @Override
        public int getRowCount() {
            return contactList != null ? contactList.size() : 0;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (contactList == null || r >= contactList.size()) {
                return null;
            }

            TuberculosisContact contact = contactList.get(r);

            if (c == 0) {
                return contact.getName();
            } else if (c == 1) {
                return contact.getAge() != null ? contact.getAge() : "";
            } else if (c == 2) {
                return contact.getGender() != null ? String.valueOf(contact.getGender()) : "";
            } else if (c == 3) {
                return contact.getScreened() != null && contact.getScreened() ? "Yes" : "No";
            } else if (c == 4) {
                String notes = contact.getNotes();
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

    private static class EnumRenderer extends DefaultListCellRenderer {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Enum<?> enumValue) {
                return super.getListCellRendererComponent(list,
                        enumValue.toString(), index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    class TreatmentTableListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            int viewRow = treatmentTable.getSelectedRow();
            if (!arg0.getValueIsAdjusting() && viewRow > -1) {
                int modelRow = treatmentTable.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < treatmentList.size()) {
                    selectedTreatment = treatmentList.get(modelRow);
                    selectedVisitRow = -1;
                    selectedContactRow = -1;
                    loadVisits();
                    loadContacts();
                }
            }
        }
    }
}
