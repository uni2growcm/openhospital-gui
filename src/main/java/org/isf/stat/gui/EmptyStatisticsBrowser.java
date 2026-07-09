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
package org.isf.stat.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import  javax.swing.Scrollable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.disctype.model.DischargeType;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.dlvrtype.manager.DeliveryTypeBrowserManager;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat2.manager.StatsDeliveryManager;
import org.isf.stat2.model.StatsDelivery;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.springframework.data.domain.Page;
import org.isf.utils.jobjects.GoodDateChooser;

public class EmptyStatisticsBrowser extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private JPanel filtersPanel;
    private JPanel dataPanel;
    private JPanel paginationPanel;
    private JPanel generalPanel;
    private GoodDateChooser periodFromDateChooser;
    private GoodDateChooser periodToDateChooser;

    private JTextField ageMinField;
    private JTextField ageMaxField;
    private Integer motherAgeMin = null;
    private Integer motherAgeMax = null;

    private JPanel deliveryPanel;
    private JComboBox<String> deliveryTypeCombo;
    private JComboBox<String> deliveryResultCombo;
    private JComboBox<String> deliveryModeCombo;
    private JComboBox<String> laborDurationCombo;
    private JComboBox<String> romCombo;
    private JComboBox<String> perinealIntegrityCombo;
    private JComboBox<String> placentaCompleteCombo;
    private JComboBox<String> bloodLossCombo;

    private JPanel newbornPanel;
    private JComboBox<String> newbornSexCombo;
    private JComboBox<String> neonatalStatusCombo;
    private JComboBox<String> apgar1Combo;
    private JComboBox<String> apgar5Combo;
    private JComboBox<String> resuscitationCombo;
    private JComboBox<String> cryTimeCombo;
    private JComboBox<String> hivStatusCombo;
    private JComboBox<String> congenitalAnomaliesCombo;

    private JPanel diseasesPanel;
    private JComboBox<Disease> diseasesCombo;
    private JComboBox<DischargeType> dischargeTypesCombo;

    private JLabel resultCountLabel;
    private int resultCount = 0;
    private JButton filterBtn;
    private JButton filterResetBtn;

    private JTable jDataTable;
    private DeliveryStatsTableModel jDataTableModel;
    private List<StatsDelivery> deliveryList = new ArrayList<>();
    private final String[] jDataTableColumns = {
            MessageBundle.getMessage("angal.stat.mothersname"),
            MessageBundle.getMessage("angal.stat.mothersage"),
            MessageBundle.getMessage("angal.stat.childsex"),
            MessageBundle.getMessage("angal.stat.birthweight"),
            MessageBundle.getMessage("angal.stat.neonatalstatus")
    };
    private final int[] jDataTableColumnWidth = { 200, 50, 80, 80, 120 };

    private int _start_index = 0;
    private final int _items_per_page = 100;
    private JButton paginationFirstBtn;
    private JButton paginationLastBtn;
    private JButton paginationPrevBtn;
    private JButton paginationNextBtn;
    private JComboBox<Integer> paginationCombo;
    private final JLabel paginationLabel = new JLabel();

    private LocalDateTime periodFrom = null;
    private LocalDateTime periodTo = null;
    private String selectedSex = null;
    private String selectedDeliveryType = null;
    private String selectedDeliveryResult = null;
    private String selectedDeliveryMode = null;
    private String selectedLaborDuration = null;
    private String selectedRom = null;
    private String selectedPerinealIntegrity = null;
    private Boolean selectedPlacentaComplete = null;
    private String selectedBloodLoss = null;
    private String selectedNeonatalStatus = null;
    private String selectedApgar1 = null;
    private String selectedApgar5 = null;
    private Boolean selectedResuscitation = null;
    private String selectedCryTime = null;
    private String selectedHivStatus = null;
    private Boolean selectedCongenitalAnomalies = null;
    private String selectedDisease = null;
    private String selectedDischargeType = null;

    private JTextField newbornWeightMinField;
    private JTextField newbornWeightMaxField;
    private Double newbornWeightMin = null;
    private Double newbornWeightMax = null;

    private final StatsDeliveryManager statsManager = Context.getApplicationContext().getBean(StatsDeliveryManager.class);
    private final DiseaseBrowserManager diseasesManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
    private final AdmissionBrowserManager admissionsManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
    private final DeliveryTypeBrowserManager deliveryTypeManager = Context.getApplicationContext().getBean(DeliveryTypeBrowserManager.class);

    public EmptyStatisticsBrowser() {
        super();
        initialize();
    }

    private void initialize() {
        setTitle(MessageBundle.getMessage("angal.stat.deliverystatsbrowsing"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();

        setMinimumSize(new Dimension(1250, 700));
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(getFiltersPanel());
        splitPane.setRightComponent(getDataPanel());
        splitPane.setDividerLocation(500);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.0);
        splitPane.setBorder(null);

        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(getPaginationPanel(), BorderLayout.SOUTH);
    }

    private static class ScrollableBoxPanel extends JPanel implements Scrollable {
        ScrollableBoxPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private void restrictToNumeric(JTextField field, boolean allowDecimal) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {

            private boolean isValid(DocumentFilter.FilterBypass fb, String insertedText, int offset, int removeLength) throws BadLocationException {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = current.substring(0, offset) + insertedText + current.substring(offset + removeLength);
                if (newText.isEmpty()) {
                    return true;
                }
                String regex = allowDecimal ? "-?\\d*\\.?\\d*" : "-?\\d*";
                return newText.matches(regex);
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (isValid(fb, string, offset, 0)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (isValid(fb, text, offset, length)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
        });
    }

    private JPanel getFiltersPanel() {
        if (filtersPanel != null) {
            return filtersPanel;
        }

        JPanel mainPanel = new ScrollableBoxPanel();

        AccordionPanel general = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.general"),
                getGeneralPanel(), true);

        AccordionPanel delivery = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.delivery"),
                getDeliveryPanel(), false);

        AccordionPanel newborn = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.newborn"),
                getNewbornPanel(), false);

        AccordionPanel diseases = new AccordionPanel(
                MessageBundle.getMessage("angal.stat.title.diseases"),
                getDiseasesPanel(), false);

        List<AccordionPanel> all = List.of(general, delivery, newborn, diseases);
        general.setSiblings(all);
        delivery.setSiblings(all);
        newborn.setSiblings(all);
        diseases.setSiblings(all);

        mainPanel.add(general);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(delivery);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(newborn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(diseases);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(getFilterButtonsPanel());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        filtersPanel = new JPanel(new BorderLayout());
        filtersPanel.add(scrollPane, BorderLayout.CENTER);
        filtersPanel.setPreferredSize(new Dimension(500, 0));
        filtersPanel.setMinimumSize(new Dimension(500, 0));

        return filtersPanel;
    }

    private JPanel getGeneralPanel() {
        if (generalPanel != null) {
            return generalPanel;
        }

        generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
        generalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        periodPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateFrom") + ": Du"));
        periodFromDateChooser = new GoodDateChooser(null);
        periodPanel.add(periodFromDateChooser);
        periodPanel.add(new JLabel(MessageBundle.getMessage("angal.common.dateTo") + ":"));
        periodToDateChooser = new GoodDateChooser(null);
        periodPanel.add(periodToDateChooser);
        generalPanel.add(periodPanel);
        generalPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agePanel.add(new JLabel(MessageBundle.getMessage("angal.stat.motherage") + ": de "));
        ageMinField = new JTextField(3);
        restrictToNumeric(ageMinField, false);
        ageMaxField = new JTextField(3);
        restrictToNumeric(ageMaxField, false);
        agePanel.add(ageMinField);
        agePanel.add(new JLabel(" " + MessageBundle.getMessage("angal.stat.to") + " "));
        agePanel.add(ageMaxField);
        agePanel.add(new JLabel(" " + MessageBundle.getMessage("angal.stat.years")));
        generalPanel.add(agePanel);

        return generalPanel;
    }

    private JPanel getDeliveryPanel() {
        if (deliveryPanel != null) {
            return deliveryPanel;
        }

        deliveryPanel = new JPanel();
        deliveryPanel.setLayout(new BoxLayout(deliveryPanel, BoxLayout.Y_AXIS));
        deliveryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel(MessageBundle.getMessage("angal.stat.deliverytype") + ":"));
        deliveryTypeCombo = new JComboBox<>();
        deliveryTypeCombo.addItem(MessageBundle.getMessage("angal.stat.all"));
        try {
            TypologyBrowserManager typologyManager = Context.getApplicationContext().getBean(TypologyBrowserManager.class);
            List<Typology> types = typologyManager.getTypologies(Family.DELIVERYTYPE);
            for (Typology type : types) {
                deliveryTypeCombo.addItem(type.getDescription());
            }
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
        }
        typePanel.add(deliveryTypeCombo);
        deliveryPanel.add(typePanel);

        JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.deliveryresult") + ":"));
        deliveryResultCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.alive"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.stillborn"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.earlyneonataldeath")
        });
        resultPanel.add(deliveryResultCombo);
        deliveryPanel.add(resultPanel);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel(MessageBundle.getMessage("angal.stat.deliverymode") + ":"));
        deliveryModeCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.deliverymode.svd"),
                MessageBundle.getMessage("angal.stat.deliverymode.vacuum"),
                MessageBundle.getMessage("angal.stat.deliverymode.forceps"),
                MessageBundle.getMessage("angal.stat.deliverymode.csection.elective"),
                MessageBundle.getMessage("angal.stat.deliverymode.csection.emergency")
        });
        modePanel.add(deliveryModeCombo);
        deliveryPanel.add(modePanel);

        JPanel laborPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        laborPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.laborduration") + ":"));
        laborDurationCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                "< 6h",
                "6-12h",
                "12-24h",
                "> 24h"
        });
        laborPanel.add(laborDurationCombo);
        deliveryPanel.add(laborPanel);

        JPanel romPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        romPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.rom") + ":"));
        romCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                "< 18h",
                ">= 18h"
        });
        romPanel.add(romCombo);
        deliveryPanel.add(romPanel);

        JPanel perinealPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        perinealPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.perinealintegrity") + ":"));
        perinealIntegrityCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.perinealintegrity.intact"),
                MessageBundle.getMessage("angal.stat.perinealintegrity.firstdegree"),
                MessageBundle.getMessage("angal.stat.perinealintegrity.seconddegree"),
                MessageBundle.getMessage("angal.stat.perinealintegrity.thirddegree"),
                MessageBundle.getMessage("angal.stat.perinealintegrity.fourthdegree")
        });
        perinealPanel.add(perinealIntegrityCombo);
        deliveryPanel.add(perinealPanel);

        JPanel placentaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        placentaPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.placentacomplete") + ":"));
        placentaCompleteCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.common.yes.label"),
                MessageBundle.getMessage("angal.common.no.label")
        });
        placentaPanel.add(placentaCompleteCombo);
        deliveryPanel.add(placentaPanel);

        JPanel bloodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bloodPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.bloodloss") + ":"));
        bloodLossCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                "< 500 ml",
                "500-1000 ml",
                "> 1000 ml"
        });
        bloodPanel.add(bloodLossCombo);
        deliveryPanel.add(bloodPanel);

        return deliveryPanel;
    }

    private JPanel getNewbornPanel() {
        if (newbornPanel != null) {
            return newbornPanel;
        }

        newbornPanel = new JPanel();
        newbornPanel.setLayout(new BoxLayout(newbornPanel, BoxLayout.Y_AXIS));
        newbornPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel sexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sexPanel.add(new JLabel(MessageBundle.getMessage("angal.patient.sex") + ":"));
        newbornSexCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.sex.male"),
                MessageBundle.getMessage("angal.stat.sex.female")
        });
        sexPanel.add(newbornSexCombo);
        newbornPanel.add(sexPanel);

        JPanel weightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        weightPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.birthweight") + ":"));
        newbornWeightMinField = new JTextField(5);
        restrictToNumeric(newbornWeightMinField, true);
        newbornWeightMaxField = new JTextField(5);
        restrictToNumeric(newbornWeightMaxField, true);
        weightPanel.add(newbornWeightMinField);
        weightPanel.add(new JLabel(" " + MessageBundle.getMessage("angal.stat.to") + " "));
        weightPanel.add(newbornWeightMaxField);
        weightPanel.add(new JLabel(" kg"));
        newbornPanel.add(weightPanel);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.neonatalstatus") + ":"));
        neonatalStatusCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.alive"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.stillborn"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.earlyneonataldeath"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.transferred"),
                MessageBundle.getMessage("angal.stat.neonatalstatus.critical")
        });
        statusPanel.add(neonatalStatusCombo);
        newbornPanel.add(statusPanel);

        JPanel apgar1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        apgar1Panel.add(new JLabel(MessageBundle.getMessage("angal.stat.apgar1") + ":"));
        apgar1Combo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                "0-3",
                "4-6",
                "7-10"
        });
        apgar1Panel.add(apgar1Combo);
        newbornPanel.add(apgar1Panel);

        JPanel apgar5Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        apgar5Panel.add(new JLabel(MessageBundle.getMessage("angal.stat.apgar5") + ":"));
        apgar5Combo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                "0-3",
                "4-6",
                "7-10"
        });
        apgar5Panel.add(apgar5Combo);
        newbornPanel.add(apgar5Panel);

        JPanel resuscitationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resuscitationPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.resuscitation") + ":"));
        resuscitationCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.common.yes.label"),
                MessageBundle.getMessage("angal.common.no.label")
        });
        resuscitationPanel.add(resuscitationCombo);
        newbornPanel.add(resuscitationPanel);

        JPanel cryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cryPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.crytime") + ":"));
        cryTimeCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.crytime.immediate"),
                MessageBundle.getMessage("angal.stat.crytime.delayed"),
                MessageBundle.getMessage("angal.stat.crytime.absent")
        });
        cryPanel.add(cryTimeCombo);
        newbornPanel.add(cryPanel);

        JPanel hivPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hivPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.hivstatus") + ":"));
        hivStatusCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.stat.hivstatus.positive"),
                MessageBundle.getMessage("angal.stat.hivstatus.negative"),
                MessageBundle.getMessage("angal.stat.hivstatus.unknown")
        });
        hivPanel.add(hivStatusCombo);
        newbornPanel.add(hivPanel);

        JPanel anomaliesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        anomaliesPanel.add(new JLabel(MessageBundle.getMessage("angal.stat.congenitalanomalies") + ":"));
        congenitalAnomaliesCombo = new JComboBox<>(new String[]{
                MessageBundle.getMessage("angal.stat.all"),
                MessageBundle.getMessage("angal.common.yes.label"),
                MessageBundle.getMessage("angal.common.no.label")
        });
        anomaliesPanel.add(congenitalAnomaliesCombo);
        newbornPanel.add(anomaliesPanel);

        return newbornPanel;
    }

    private JPanel getDiseasesPanel() {
        if (diseasesPanel != null) {
            return diseasesPanel;
        }

        diseasesPanel = new JPanel();
        diseasesPanel.setLayout(new BoxLayout(diseasesPanel, BoxLayout.Y_AXIS));
        diseasesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel diseasePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diseasePanel.add(new JLabel(MessageBundle.getMessage("angal.stat.disease") + ":"));
        diseasesCombo = new JComboBox<>();
        diseasesCombo.addItem(null);
        try {
            List<Disease> diseases = diseasesManager.getDiseaseAll();
            for (Disease disease : diseases) {
                diseasesCombo.addItem(disease);
            }
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
        }
        diseasesCombo.setPreferredSize(new Dimension(280, diseasesCombo.getPreferredSize().height));
        diseasesCombo.setMaximumSize(new Dimension(280, diseasesCombo.getPreferredSize().height));
        diseasePanel.add(diseasesCombo);
        diseasesPanel.add(diseasePanel);

        JPanel dischargePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dischargePanel.add(new JLabel(MessageBundle.getMessage("angal.stat.dischargetype") + ":"));
        dischargeTypesCombo = new JComboBox<>();
        dischargeTypesCombo.addItem(null);
        try {
            List<DischargeType> dischargeTypes = admissionsManager.getDischargeType();
            for (DischargeType dischargeType : dischargeTypes) {
                dischargeTypesCombo.addItem(dischargeType);
            }
        } catch (OHServiceException e) {
            MessageDialog.showExceptions(e);
        }
        dischargeTypesCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String text = "";
                if (value instanceof DischargeType dt) {
                    text = translateDischargeType(dt.getDescription());
                }
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        dischargeTypesCombo.setPreferredSize(new Dimension(220, dischargeTypesCombo.getPreferredSize().height));
        dischargeTypesCombo.setMaximumSize(new Dimension(220, dischargeTypesCombo.getPreferredSize().height));
        dischargePanel.add(dischargeTypesCombo);
        diseasesPanel.add(dischargePanel);

        return diseasesPanel;
    }

    private JPanel getFilterButtonsPanel() {
        JPanel filterButtonPanel = new JPanel(new FlowLayout());
        filterButtonPanel.add(getFilterButton());
        filterButtonPanel.add(getFilterResetButton());
        return filterButtonPanel;
    }

    private JButton getFilterButton() {
        if (filterBtn != null) {
            return filterBtn;
        }

        filterBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.filter"));

        filterBtn.addActionListener(e -> {
            _start_index = 0;

            if (!checkPeriod()) {
                return;
            }
            if (!checkWeight()) {
                return;
            }
            collectFilters();
            runQuery(0);
            initializePaginationCombo();
        });

        return filterBtn;
    }

    private JButton getFilterResetButton() {
        if (filterResetBtn != null) {
            return filterResetBtn;
        }

        filterResetBtn = new JButton(MessageBundle.getMessage("angal.stat.btn.resetfilter"));

        filterResetBtn.addActionListener(e -> {
            _start_index = 0;
            resetAllFilters();
        });

        return filterResetBtn;
    }

    private boolean checkPeriod() {
        LocalDate dateFrom = periodFromDateChooser.getDate();
        LocalDate dateTo = periodToDateChooser.getDate();

        if (dateFrom == null && dateTo == null) {
            periodFrom = null;
            periodTo = null;
            return true;
        }

        if (dateFrom == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertadmissiondatefrom"));
            return false;
        }

        if (dateTo != null && dateTo.isBefore(dateFrom)) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidadmissionperiod"));
            return false;
        }

        periodFrom = dateFrom.atStartOfDay();
        periodTo = dateTo != null ? dateTo.atStartOfDay() : null;

        return true;
    }

    private boolean checkWeight() {
        String fromText = newbornWeightMinField.getText().trim();
        String toText = newbornWeightMaxField.getText().trim();

        if (fromText.isEmpty() && toText.isEmpty()) {
            newbornWeightMin = null;
            newbornWeightMax = null;
            return true;
        }

        try {
            if (!fromText.isEmpty()) {
                newbornWeightMin = Double.parseDouble(fromText);
            }
            if (!toText.isEmpty()) {
                newbornWeightMax = Double.parseDouble(toText);
            }
        } catch (NumberFormatException nfe) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidweightrange"));
            return false;
        }

        if (newbornWeightMin != null && newbornWeightMax != null && newbornWeightMin > newbornWeightMax) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.stat.error.pleaseinsertvalidweightrange"));
            return false;
        }

        return true;
    }

    private void collectFilters() {
        motherAgeMin = parseInteger(ageMinField.getText());
        motherAgeMax = parseInteger(ageMaxField.getText());
        selectedSex = getSelectedSex();
        newbornWeightMin = parseDouble(newbornWeightMinField.getText());
        newbornWeightMax = parseDouble(newbornWeightMaxField.getText());
        selectedDeliveryType = getSelectedDeliveryType();
        selectedDeliveryResult = getSelectedDeliveryResult();
        selectedDeliveryMode = getSelectedDeliveryMode();
        selectedLaborDuration = getSelectedLaborDuration();
        selectedRom = getSelectedRom();
        selectedPerinealIntegrity = getSelectedPerinealIntegrity();
        selectedPlacentaComplete = getSelectedPlacentaComplete();
        selectedBloodLoss = getSelectedBloodLoss();
        selectedNeonatalStatus = getSelectedNeonatalStatus();
        selectedApgar1 = getSelectedApgar1();
        selectedApgar5 = getSelectedApgar5();
        selectedResuscitation = getSelectedResuscitation();
        selectedCryTime = getSelectedCryTime();
        selectedHivStatus = getSelectedHivStatus();
        selectedCongenitalAnomalies = getSelectedCongenitalAnomalies();
        selectedDisease = getSelectedDisease();
        selectedDischargeType = getSelectedDischargeType();
    }

    private Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getSelectedSex() {
        String selected = (String) newbornSexCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.sex.male"))) {
            return "M";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.sex.female"))) {
            return "F";
        }
        return null;
    }

    private String getSelectedDeliveryType() {
        String selected = (String) deliveryTypeCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedDeliveryResult() {
        String selected = (String) deliveryResultCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.alive"))) {
            return "ALIVE";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.stillborn"))) {
            return "STILLBORN";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.earlyneonataldeath"))) {
            return "EARLY_NEONATAL_DEATH";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.transferred"))) {
            return "TRANSFERRED";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.critical"))) {
            return "CRITICAL";
        }
        return selected;
    }

    private String getSelectedDeliveryMode() {
        String selected = (String) deliveryModeCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.deliverymode.svd"))) {
            return "1";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.deliverymode.vacuum"))) {
            return "2";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.deliverymode.forceps"))) {
            return "3";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.deliverymode.csection.elective"))) {
            return "4";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.deliverymode.csection.emergency"))) {
            return "5";
        }
        return null;
    }

    private String getSelectedLaborDuration() {
        String selected = (String) laborDurationCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedRom() {
        String selected = (String) romCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedPerinealIntegrity() {
        String selected = (String) perinealIntegrityCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.perinealintegrity.intact"))) {
            return "0";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.perinealintegrity.firstdegree"))) {
            return "1";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.perinealintegrity.seconddegree"))) {
            return "2";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.perinealintegrity.thirddegree"))) {
            return "3";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.perinealintegrity.fourthdegree"))) {
            return "4";
        }
        return null;
    }

    private Boolean getSelectedPlacentaComplete() {
        String selected = (String) placentaCompleteCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        return selected.equals(MessageBundle.getMessage("angal.common.yes.label"));
    }

    private String getSelectedBloodLoss() {
        String selected = (String) bloodLossCombo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedNewbornSex() {
        String selected = (String) newbornSexCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.sex.male"))) {
            return "M";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.sex.female"))) {
            return "F";
        }
        return null;
    }

    private String getSelectedNeonatalStatus() {
        String selected = (String) neonatalStatusCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.alive"))) {
            return "ALIVE";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.stillborn"))) {
            return "STILLBORN";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.earlyneonataldeath"))) {
            return "EARLY_NEONATAL_DEATH";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.transferred"))) {
            return "TRANSFERRED";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.neonatalstatus.critical"))) {
            return "CRITICAL";
        }
        return selected;
    }

    private String getSelectedApgar1() {
        String selected = (String) apgar1Combo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private String getSelectedApgar5() {
        String selected = (String) apgar5Combo.getSelectedItem();
        return (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) ? null : selected;
    }

    private Boolean getSelectedResuscitation() {
        String selected = (String) resuscitationCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        return selected.equals(MessageBundle.getMessage("angal.common.yes.label"));
    }

    private String getSelectedCryTime() {
        String selected = (String) cryTimeCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.crytime.immediate"))) {
            return "IMMEDIATE";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.crytime.delayed"))) {
            return "DELAYED";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.crytime.absent"))) {
            return "ABSENT";
        }
        return selected;
    }

    private String getSelectedHivStatus() {
        String selected = (String) hivStatusCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.hivstatus.positive"))) {
            return "POSITIVE";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.hivstatus.negative"))) {
            return "NEGATIVE";
        }
        if (selected.equals(MessageBundle.getMessage("angal.stat.hivstatus.unknown"))) {
            return "UNKNOWN";
        }
        return selected;
    }

    private Boolean getSelectedCongenitalAnomalies() {
        String selected = (String) congenitalAnomaliesCombo.getSelectedItem();
        if (selected == null || selected.equals(MessageBundle.getMessage("angal.stat.all"))) {
            return null;
        }
        return selected.equals(MessageBundle.getMessage("angal.common.yes.label"));
    }

    private String getSelectedDisease() {
        Object selected = diseasesCombo.getSelectedItem();
        return selected != null ? selected.toString() : null;
    }

    private String getSelectedDischargeType() {
        Object selected = dischargeTypesCombo.getSelectedItem();
        return selected != null ? selected.toString() : null;
    }

    private String translateDischargeType(String description) {
        if (description == null) {
            return "";
        }
        switch (description.trim().toUpperCase()) {
            case "REFERRED":
                return MessageBundle.getMessage("angal.stat.dischargetype.referred");
            case "DEAD":
                return MessageBundle.getMessage("angal.stat.dischargetype.dead");
            case "NORMAL DISCHARGE":
                return MessageBundle.getMessage("angal.stat.dischargetype.normaldischarge");
            case "ESCAPE":
                return MessageBundle.getMessage("angal.stat.dischargetype.escape");
            default:
                return description;
        }
    }

    private void runQuery(int pageIndex) {
        try {
            Page<StatsDelivery> result = statsManager.getDeliveriesStats(
                    periodFrom, periodTo,
                    motherAgeMin, motherAgeMax,
                    selectedSex,
                    newbornWeightMin, newbornWeightMax,
                    selectedDeliveryType,
                    selectedDeliveryResult,
                    selectedDeliveryMode,
                    selectedLaborDuration,
                    selectedRom,
                    selectedPerinealIntegrity,
                    selectedPlacentaComplete,
                    selectedBloodLoss,
                    null,
                    null,
                    selectedNeonatalStatus,
                    selectedApgar1,
                    selectedApgar5,
                    selectedResuscitation,
                    selectedCryTime,
                    selectedHivStatus,
                    selectedCongenitalAnomalies,
                    selectedDisease,
                    selectedDischargeType,
                    pageIndex, _items_per_page
            );

            resultCount = (int) result.getTotalElements();
            deliveryList = new ArrayList<>(result.getContent());

            if (deliveryList.isEmpty() && resultCount == 0) {
                JOptionPane.showMessageDialog(EmptyStatisticsBrowser.this,
                        MessageBundle.getMessage("angal.stat.nomatchfound"),
                        MessageBundle.getMessage("angal.stat.operationresult"), JOptionPane.PLAIN_MESSAGE);
            }

            jDataTableModel.fireTableDataChanged();
            jDataTable.updateUI();
            resultCountLabel.setText(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);

        } catch (OHServiceException ex) {
            MessageDialog.showExceptions(ex);
        }
    }

    private void resetAllFilters() {
        _start_index = 0;
        periodFrom = null;
        periodTo = null;
        motherAgeMin = null;
        motherAgeMax = null;
        selectedSex = null;
        newbornWeightMin = null;
        newbornWeightMax = null;
        selectedDeliveryType = null;
        selectedDeliveryResult = null;
        selectedDeliveryMode = null;
        selectedLaborDuration = null;
        selectedRom = null;
        selectedPerinealIntegrity = null;
        selectedPlacentaComplete = null;
        selectedNeonatalStatus = null;
        selectedApgar1 = null;
        selectedApgar5 = null;
        selectedResuscitation = null;
        selectedCryTime = null;
        selectedHivStatus = null;
        selectedCongenitalAnomalies = null;
        selectedDisease = null;
        selectedDischargeType = null;

        periodFromDateChooser.setDate(null);
        periodToDateChooser.setDate(null);
        ageMinField.setText("");
        ageMaxField.setText("");
        newbornWeightMinField.setText("");
        newbornWeightMaxField.setText("");
        newbornSexCombo.setSelectedIndex(0);
        deliveryTypeCombo.setSelectedIndex(0);
        deliveryResultCombo.setSelectedIndex(0);
        deliveryModeCombo.setSelectedIndex(0);
        laborDurationCombo.setSelectedIndex(0);
        romCombo.setSelectedIndex(0);
        perinealIntegrityCombo.setSelectedIndex(0);
        placentaCompleteCombo.setSelectedIndex(0);
        bloodLossCombo.setSelectedIndex(0);
        neonatalStatusCombo.setSelectedIndex(0);
        apgar1Combo.setSelectedIndex(0);
        apgar5Combo.setSelectedIndex(0);
        resuscitationCombo.setSelectedIndex(0);
        cryTimeCombo.setSelectedIndex(0);
        hivStatusCombo.setSelectedIndex(0);
        congenitalAnomaliesCombo.setSelectedIndex(0);
        diseasesCombo.setSelectedItem(null);
        dischargeTypesCombo.setSelectedItem(null);

        runQuery(0);
        initializePaginationCombo();
    }

    private JPanel getDataPanel() {
        if (dataPanel != null) {
            return dataPanel;
        }
        dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(getDataTableScrollPane(), BorderLayout.CENTER);
        dataPanel.setMinimumSize(new Dimension(650, 0));
        return dataPanel;
    }

    private JScrollPane getDataTableScrollPane() {
        jDataTableModel = new DeliveryStatsTableModel();
        jDataTable = new JTable(jDataTableModel);

        for (int i = 0; i < jDataTableColumns.length; i++) {
            jDataTable.getColumnModel().getColumn(i).setMinWidth(jDataTableColumnWidth[i]);
        }

        JScrollPane tableScrollPane = new JScrollPane(jDataTable);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return tableScrollPane;
    }

    private class DeliveryStatsTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return deliveryList.size();
        }

        @Override
        public String getColumnName(int c) {
            return jDataTableColumns[c];
        }

        @Override
        public int getColumnCount() {
            return jDataTableColumns.length;
        }

        @Override
        public Object getValueAt(int r, int c) {
            StatsDelivery stats = deliveryList.get(r);
            if (c == 0) {
                return stats.getMotherName();
            } else if (c == 1) {
                return stats.getMotherAge();
            } else if (c == 2) {
                return stats.getNewbornSex();
            } else if (c == 3) {
                return stats.getNewbornWeight();
            } else if (c == 4) {
                String status = stats.getNeonatalStatus();
                return translateNeonatalStatus(status);
            }
            return null;
        }

        private String translateNeonatalStatus(String status) {
            if (status == null || status.isEmpty()) {
                return "";
            }
            switch (status) {
                case "ALIVE":
                    return MessageBundle.getMessage("angal.stat.neonatalstatus.alive");
                case "STILLBORN":
                    return MessageBundle.getMessage("angal.stat.neonatalstatus.stillborn");
                case "EARLY_NEONATAL_DEATH":
                    return MessageBundle.getMessage("angal.stat.neonatalstatus.earlyneonataldeath");
                case "TRANSFERRED":
                    return MessageBundle.getMessage("angal.stat.neonatalstatus.transferred");
                case "CRITICAL":
                    return MessageBundle.getMessage("angal.stat.neonatalstatus.critical");
                default:
                    return status;
            }
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }
    }

    private JPanel getPaginationPanel() {
        if (paginationPanel != null) {
            return paginationPanel;
        }

        paginationPanel = new JPanel(new FlowLayout());
        paginationPanel.add(getResultCountPanel());
        paginationPanel.add(getPaginationFirstButton());
        paginationPanel.add(getPaginationPrevButton());
        paginationPanel.add(getPaginationCombo());
        paginationPanel.add(paginationLabel);
        paginationPanel.add(getPaginationNextButton());
        paginationPanel.add(getPaginationLastButton());

        initializePaginationCombo();

        return paginationPanel;
    }

    private JPanel getResultCountPanel() {
        JPanel resultCountPanel = new JPanel(new FlowLayout());
        resultCountLabel = new JLabel(MessageBundle.getMessage("angal.stat.total") + " : " + resultCount);
        resultCountPanel.add(resultCountLabel);
        return resultCountPanel;
    }

    private JButton getPaginationFirstButton() {
        if (paginationFirstBtn != null) {
            return paginationFirstBtn;
        }

        paginationFirstBtn = new JButton("<<");
        paginationFirstBtn.setEnabled(false);
        paginationFirstBtn.addActionListener(e -> {
            _start_index = 0;
            paginationCombo.setSelectedItem(1);
        });

        return paginationFirstBtn;
    }

    private JButton getPaginationPrevButton() {
        if (paginationPrevBtn != null) {
            return paginationPrevBtn;
        }

        paginationPrevBtn = new JButton("<");
        paginationPrevBtn.setEnabled(false);
        paginationPrevBtn.addActionListener(e -> {
            _start_index -= _items_per_page;
            int page = _start_index / _items_per_page + 1;
            paginationCombo.setSelectedItem(page);
        });
        return paginationPrevBtn;
    }

    private JComboBox<Integer> getPaginationCombo() {
        if (paginationCombo != null) {
            return paginationCombo;
        }

        paginationCombo = new JComboBox<>();

        paginationCombo.addActionListener(e -> {
            if (paginationCombo.getItemCount() > 0 && paginationCombo.getSelectedItem() != null) {
                int pageNumber = (Integer) paginationCombo.getSelectedItem();
                _start_index = (pageNumber - 1) * _items_per_page;
                runQuery(pageNumber - 1);
                setPaginationButtons();
            }
        });

        return paginationCombo;
    }

    private JButton getPaginationNextButton() {
        if (paginationNextBtn != null) {
            return paginationNextBtn;
        }

        paginationNextBtn = new JButton(">");
        paginationNextBtn.setEnabled(false);
        paginationNextBtn.addActionListener(e -> {
            _start_index += _items_per_page;
            int page = _start_index / _items_per_page + 1;
            paginationCombo.setSelectedItem(page);
        });

        return paginationNextBtn;
    }

    private JButton getPaginationLastButton() {
        if (paginationLastBtn != null) {
            return paginationLastBtn;
        }

        paginationLastBtn = new JButton(">>");
        paginationLastBtn.setEnabled(false);
        paginationLastBtn.addActionListener(e -> {
            int lastPage = paginationCombo.getItemCount();
            if (lastPage > 0) {
                paginationCombo.setSelectedItem(lastPage);
            }
        });

        return paginationLastBtn;
    }

    public void initializePaginationCombo() {
        int j = 0;
        paginationCombo.removeAllItems();

        for (int i = 0; i < resultCount / _items_per_page; i++) {
            j = i + 1;
            paginationCombo.addItem(j);
        }

        if (j * _items_per_page < resultCount) {
            paginationCombo.addItem(j + 1);
            paginationLabel.setText("/" + (resultCount / _items_per_page + 1) + " Pages");
        } else {
            paginationLabel.setText("/" + (j == 0 ? 0 : resultCount / _items_per_page) + " Pages");
        }

        setPaginationButtons();
    }

    private void setPaginationButtons() {
        if (_start_index + _items_per_page >= resultCount) {
            paginationNextBtn.setEnabled(false);
            paginationLastBtn.setEnabled(false);
        } else {
            paginationNextBtn.setEnabled(true);
            paginationLastBtn.setEnabled(true);
        }

        if (_start_index < _items_per_page) {
            paginationPrevBtn.setEnabled(false);
            paginationFirstBtn.setEnabled(false);
        } else {
            paginationPrevBtn.setEnabled(true);
            paginationFirstBtn.setEnabled(true);
        }
    }

    private class AccordionPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredSize() {
            Dimension header = headerPanel.getPreferredSize();
            if (!expanded) {
                return new Dimension(header.width, header.height);
            }
            return super.getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        private final JPanel headerPanel;
        private final JPanel contentPanel;
        private final JLabel toggleLabel;
        private boolean expanded;
        private List<AccordionPanel> siblings = new ArrayList<>();

        public AccordionPanel(String title, JPanel content, boolean defaultExpanded) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            this.expanded = defaultExpanded;

            headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(240, 240, 240));
            headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13));

            toggleLabel = new JLabel(expanded ? " ▼" : " ▶");
            toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.BOLD, 14));

            headerPanel.add(titleLabel, BorderLayout.CENTER);
            headerPanel.add(toggleLabel, BorderLayout.EAST);

            contentPanel = content;
            contentPanel.setVisible(expanded);

            add(headerPanel, BorderLayout.NORTH);
            add(contentPanel, BorderLayout.CENTER);

            headerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggle();
                }
            });
        }

        public void setSiblings(List<AccordionPanel> siblings) {
            this.siblings = siblings;
        }

        public void toggle() {
            expanded = !expanded;

            if (expanded) {
                add(contentPanel, BorderLayout.CENTER);
            } else {
                remove(contentPanel);
            }
            contentPanel.setVisible(expanded);
            toggleLabel.setText(expanded ? " ▼" : " ▶");

            revalidateFully();
        }

        private void revalidateFully() {
            Container top = this;
            while (top.getParent() != null) {
                top = top.getParent();
            }
            top.revalidate();
            top.repaint();
        }
    }
}