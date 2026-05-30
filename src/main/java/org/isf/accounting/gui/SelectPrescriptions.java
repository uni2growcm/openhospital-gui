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
package org.isf.accounting.gui;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.BillItems;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.OperationRow;
import org.isf.patient.model.Patient;
import org.isf.priceslist.model.ItemGroup;
import org.isf.priceslist.model.Price;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.therapy.manager.TherapyManager;
import org.isf.therapy.model.TherapyRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

public class SelectPrescriptions extends JDialog {

    private static final long serialVersionUID = 1L;

    private Patient patient;
    private List<TherapyRow> therapies = new ArrayList<>();
    private List<Laboratory> laboratories = new ArrayList<>();
    private List<OperationRow> operations = new ArrayList<>();

    private JTable tableTherapy;
    private JTable tableExam;
    private JTable tableOperation;

    private Map<Integer, Integer> therapyQuantities = new HashMap<>();
    private Map<TherapyRow, Integer> mapTherapies = new HashMap<>();

    private EventListenerList selectionListener = new EventListenerList();

    // Labels for totals
    private JLabel lblTherapyTotalVal;
    private JLabel lblTherapyTotalSelectionVal;
    private JLabel lblExamTotalVal;
    private JLabel lblExamTotalSelectionVal;
    private JLabel lblOpeTotalVal;
    private JLabel lblOpeTotalSelectionVal;
    private JLabel lblTotalVal;
    private JLabel lblTotalSelectionVal;
    private JCheckBox selectAllTherapy;
    private JCheckBox selectAllExam;
    private JCheckBox selectAllOperation;

    // Core managers
    private TherapyManager therapyManager;
    private LabManager labManager;
    private OperationRowBrowserManager opManager;
    private BillBrowserManager billManager;
    private String medicalPriceID;
    private String examPriceID;
    private String operationPriceID;

    public interface PrescriptionSelectionListener extends EventListener {
        void prescriptionSelected(List<BillItems> prescriptions);
    }

    public SelectPrescriptions(JDialog owner, Patient patient) {
        super(owner, true);
        this.patient = patient;

        initManagers();
        loadData();

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initManagers() {
        try {
            therapyManager = Context.getApplicationContext().getBean(TherapyManager.class);
            labManager = Context.getApplicationContext().getBean(LabManager.class);
            opManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
            billManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
        } catch (Exception e) {
            MessageDialog.error(this, "Error initializing managers: " + e.getMessage());
        }
    }

    private void loadData() {
        loadTherapies();
        loadLaboratories();
        loadOperations();
    }

    private void loadTherapies() {
        if (therapyManager == null) {
            therapies = new ArrayList<>();
            return;
        }

        try {
            List<TherapyRow> allTherapies = therapyManager.getTherapyRows(patient.getCode());
            therapies = new ArrayList<>();

            for (TherapyRow therapy : allTherapies) {
                double totalPrescribed = calculateTotalQuantity(
                        therapy.getStartDate(), therapy.getEndDate(),
                        therapy.getFreqInPeriod(), therapy.getFreqInDay(), therapy.getQty()
                );
                double alreadyBought = therapy.getQtyBougth() != null ? therapy.getQtyBougth() : 0.0;
                double remaining = totalPrescribed - alreadyBought;

                // Show only therapies with remaining quantity > 0
                if (remaining > 0) {
                    therapies.add(therapy);
                    mapTherapies.put(therapy, (int) Math.ceil(remaining));
                    therapyQuantities.put(therapy.getTherapyID(), (int) Math.ceil(remaining));
                }
            }
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e, this);
            therapies = new ArrayList<>();
        }
    }

    private double calculateTotalQuantity(LocalDateTime startDate, LocalDateTime endDate,
                                          int freqInPeriod, int freqInDay, double qty) {
        long diffInMillis = java.time.Duration.between(startDate, endDate).toMillis();
        long totalDays = (diffInMillis / (1000 * 60 * 60 * 24)) + 1;
        long effectiveDays = (totalDays + freqInPeriod - 1) / freqInPeriod;
        return effectiveDays * freqInDay * qty;
    }

    private void loadLaboratories() {
        if (labManager == null) {
            laboratories = new ArrayList<>();
            return;
        }

        try {
            laboratories = labManager.getLabWithoutBill(patient.getCode());
            if (laboratories == null) {
                laboratories = new ArrayList<>();
            }
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e, this);
            laboratories = new ArrayList<>();
        }
    }

    private void loadOperations() {
        if (opManager == null) {
            operations = new ArrayList<>();
            return;
        }

        try {
            operations = opManager.getOperationWithoutBill(patient);
            if (operations == null) {
                operations = new ArrayList<>();
            }
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e, this);
            operations = new ArrayList<>();
        }
    }

    private double getTherapyPrice(TherapyRow therapy, boolean withReduction) {
        try {
            Price price;
            if (withReduction) {
                price = billManager.getPrice(String.valueOf(therapy.getMedicalId()), ItemGroup.MEDICAL, patient);
            } else {
                price = billManager.getPriceFromListWithoutReduction(String.valueOf(therapy.getMedicalId()), ItemGroup.MEDICAL, patient);
            }

            if (price != null && price.getItem() != null) {
                medicalPriceID = price.getGroup() + price.getItem();
            }
            return price != null ? price.getPrice() : 0.0;
        } catch (OHServiceException e) {
            return 0.0;
        }
    }

    private double getExamPrice(Laboratory lab, boolean withReduction) {
        try {
            Price price;
            if (withReduction) {
                price = billManager.getPrice(lab.getExam().getCode(), ItemGroup.EXAM, patient);
            } else {
                price = billManager.getPriceFromListWithoutReduction(lab.getExam().getCode(), ItemGroup.EXAM, patient);
            }

            if (price != null && price.getItem() != null) {
                examPriceID = price.getGroup() + price.getItem();
            }
            return price != null ? price.getPrice() : 0.0;
        } catch (OHServiceException e) {
            return 0.0;
        }
    }

    private double getOperationPrice(OperationRow op, boolean withReduction) {
        try {
            Price price;
            if (withReduction) {
                price = billManager.getPrice(op.getOperation().getCode(), ItemGroup.OPERATION, patient);
            } else {
                price = billManager.getPriceFromListWithoutReduction(op.getOperation().getCode(), ItemGroup.OPERATION, patient);
            }

            if (price != null && price.getItem() != null) {
                operationPriceID = price.getGroup() + price.getItem();
            }
            return price != null ? price.getPrice() : 0.0;
        } catch (OHServiceException e) {
            return 0.0;
        }
    }

    public void addPrescriptionSelectedListener(PrescriptionSelectionListener listener) {
        selectionListener.add(PrescriptionSelectionListener.class, listener);
    }

    private void fireSelectedPrescription() {
        List<BillItems> prescriptions = new ArrayList<>();

        // Add selected therapies
        if (tableTherapy != null) {
            int[] selectedRows = tableTherapy.getSelectedRows();
            for (int row : selectedRows) {
                if (row < therapies.size()) {
                    TherapyRow therapy = therapies.get(row);
                    int quantityToBill = mapTherapies.getOrDefault(therapy, 0);

                    if (quantityToBill > 0) {
                        BillItems item = new BillItems();
                        item.setItemDescription(getMedicalDescription(therapy.getMedicalId()));
                        item.setItemGroup(ItemGroup.MEDICAL.getCode());
                        item.setItemId(String.valueOf(therapy.getMedicalId()));
                        item.setItemQuantity(quantityToBill);
                        item.setPrescriptionId(Integer.valueOf(therapy.getTherapyID()));

                        double priceBrut = getTherapyPrice(therapy, false);
                        double priceWithReduction = priceBrut;

                        if (patient.getReductionPlan() != null) {
                            ReductionPlan reductionPlan = patient.getReductionPlan();
                            priceWithReduction = applyReductionPlan(priceBrut, reductionPlan.getOperationRate());
                        }

                        item.setItemAmount(priceWithReduction);
                        item.setItemAmountBrut(priceBrut);
                        item.setPriceID(medicalPriceID);
                        item.setPrice(true);

                        prescriptions.add(item);
                    }
                }
            }
        }

        // Add selected exams
        if (tableExam != null) {
            int[] selectedRows = tableExam.getSelectedRows();
            for (int row : selectedRows) {
                if (row < laboratories.size()) {
                    Laboratory lab = laboratories.get(row);
                    BillItems item = new BillItems();
                    item.setItemDescription(lab.getExam().getDescription());
                    item.setItemGroup(ItemGroup.EXAM.getCode());
                    item.setItemId(lab.getExam().getCode());
                    // lab.getCode() returns Integer already, no conversion needed
                    item.setPrescriptionId(lab.getCode());
                    item.setItemQuantity(1);

                    double priceBrut = getExamPrice(lab, false);
                    double priceWithReduction = priceBrut;

                    if (patient.getReductionPlan() != null) {
                        ReductionPlan reductionPlan = patient.getReductionPlan();
                        priceWithReduction = applyReductionPlan(priceBrut, reductionPlan.getExamRate());
                    }

                    item.setItemAmount(priceWithReduction);
                    item.setItemAmountBrut(priceBrut);
                    item.setPriceID(examPriceID);
                    item.setPrice(true);

                    prescriptions.add(item);
                }
            }
        }

        // Add selected operations
        if (tableOperation != null) {
            int[] selectedRows = tableOperation.getSelectedRows();
            for (int row : selectedRows) {
                if (row < operations.size()) {
                    OperationRow op = operations.get(row);
                    BillItems item = new BillItems();
                    item.setItemDescription(op.getOperation().getDescription());
                    item.setItemGroup(ItemGroup.OPERATION.getCode());
                    item.setItemId(op.getOperation().getCode());
                    // Convert int to Integer using valueOf
                    item.setPrescriptionId(Integer.valueOf(op.getId()));
                    item.setItemQuantity(1);

                    double priceBrut = getOperationPrice(op, false);
                    double priceWithReduction = priceBrut;

                    if (patient.getReductionPlan() != null) {
                        ReductionPlan reductionPlan = patient.getReductionPlan();
                        priceWithReduction = applyReductionPlan(priceBrut, reductionPlan.getOperationRate());
                    }

                    item.setItemAmount(priceWithReduction);
                    item.setItemAmountBrut(priceBrut);
                    item.setPriceID(operationPriceID);
                    item.setPrice(true);

                    prescriptions.add(item);
                }
            }
        }

        EventListener[] listeners = selectionListener.getListeners(PrescriptionSelectionListener.class);
        for (EventListener listener : listeners) {
            ((PrescriptionSelectionListener) listener).prescriptionSelected(prescriptions);
        }
    }

    private String getMedicalDescription(Integer medicalId) {
        if (therapyManager == null) {
            return String.valueOf(medicalId);
        }
        try {
            return therapyManager.getMedical(medicalId).getDescription();
        } catch (OHServiceException e) {
            return String.valueOf(medicalId);
        }
    }

    private double applyReductionPlan(double initialPrice, BigDecimal reductionPlanPercentage) {

        if (reductionPlanPercentage != null && reductionPlanPercentage.compareTo(BigDecimal.ZERO) > 0) {
            double percentage = reductionPlanPercentage.doubleValue();
            double reductionAmount = initialPrice * (percentage / 100);
            return initialPrice - reductionAmount;
        }

        return initialPrice;
    }

    private void initComponents() {
        setTitle(MessageBundle.getMessage("angal.patientbill.prescription") + " - " + patient.getName());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel(MessageBundle.getMessage("angal.therapy.therapyofpatientname") + " " + patient.getName(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        // Therapies panel
        if (!therapies.isEmpty()) {
            JPanel therapyPanel = createTherapyPanel();
            therapyPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.selectprescription.medicallist")));
            mainPanel.add(new JScrollPane(therapyPanel));
        }

        // Exams panel
        if (!laboratories.isEmpty()) {
            JPanel examPanel = createExamPanel();
            examPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.selectprescription.examslist")));
            mainPanel.add(new JScrollPane(examPanel));
        }

        // Operations panel
        if (!operations.isEmpty()) {
            JPanel operationPanel = createOperationPanel();
            operationPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.selectprescription.operationslist")));
            mainPanel.add(new JScrollPane(operationPanel));
        }

        // If no data, show message
        if (therapies.isEmpty() && laboratories.isEmpty() && operations.isEmpty()) {
            JLabel noDataLabel = new JLabel(MessageBundle.getMessage("angal.newbill.noprescriptionforthispatient.msg"), SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
            noDataLabel.setForeground(Color.RED);
            mainPanel.add(noDataLabel);
        }

        // Global totals panel
        JPanel globalPanel = createGlobalPanel();
        mainPanel.add(globalPanel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        JButton validateButton = new JButton(MessageBundle.getMessage("angal.therapy.validateselection"));

        cancelButton.addActionListener(e -> dispose());
        validateButton.addActionListener(e -> {
            boolean hasSelection = (tableTherapy != null && tableTherapy.getSelectedRowCount() > 0) ||
                    (tableExam != null && tableExam.getSelectedRowCount() > 0) ||
                    (tableOperation != null && tableOperation.getSelectedRowCount() > 0);
            if (!hasSelection) {
                MessageDialog.warning(this, "angal.billbrowser.pleaseselectatleestonerow");
                return;
            }
            fireSelectedPrescription();
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(validateButton);

        updateTotals();

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(1000, 750);
    }

    private JPanel createTherapyPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                MessageBundle.getMessage("angal.therapy.startdate"),
                MessageBundle.getMessage("angal.priceslist.medicals"),
                MessageBundle.getMessage("angal.newbill.totalqty"),
                MessageBundle.getMessage("angal.newbill.alreadybought"),
                MessageBundle.getMessage("angal.therapy.remaining"),
                MessageBundle.getMessage("angal.common.stock.txt"),
                MessageBundle.getMessage("angal.newbill.unitprice.txt")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only "remaining" column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5) {
                    return Integer.class;
                }
                if (columnIndex == 6) {
                    return Double.class;
                }
                return String.class;
            }
        };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (TherapyRow therapy : therapies) {
            LocalDateTime startDate = therapy.getStartDate();
            String startDateStr = startDate != null ? startDate.format(formatter) : "";

            double totalQty = calculateTotalQuantity(
                    therapy.getStartDate(), therapy.getEndDate(),
                    therapy.getFreqInPeriod(), therapy.getFreqInDay(), therapy.getQty()
            );
            double alreadyBought = therapy.getQtyBougth() != null ? therapy.getQtyBougth() : 0.0;
            int remaining = (int) Math.ceil(totalQty - alreadyBought);
            double unitPrice = getTherapyPrice(therapy, true);

            Object[] row = {
                    startDateStr,
                    getMedicalDescription(therapy.getMedicalId()),
                    (int) Math.ceil(totalQty),
                    (int) Math.ceil(alreadyBought),
                    remaining,
                    0, // Stock - would need to be retrieved
                    unitPrice
            };
            model.addRow(row);
        }

        tableTherapy = new JTable(model);

        // Set column widths
        TableColumn qtyColumn = tableTherapy.getColumnModel().getColumn(4);
        qtyColumn.setCellEditor(new SpinnerNumberEditor(0, 9999, 1));

        tableTherapy.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Add selection listener for totals
        tableTherapy.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (selectAllTherapy != null) {
                    boolean allSelected = tableTherapy.getSelectedRowCount() == tableTherapy.getRowCount()
                            && tableTherapy.getRowCount() > 0;
                    selectAllTherapy.setSelected(allSelected);
                }
                updateTotals();
            }
        });

        // Add header with select all checkbox
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllTherapy = new JCheckBox(MessageBundle.getMessage("angal.selectprescription.selectallmedicals"));
        selectAllTherapy.addActionListener(e -> {
            if (selectAllTherapy.isSelected()) {
                tableTherapy.selectAll();
            } else {
                tableTherapy.clearSelection();
            }
            updateTotals();
        });

        JPanel therapyTotalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        therapyTotalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.total.txt") + ":"));
        lblTherapyTotalVal = new JLabel("0.00");
        therapyTotalPanel.add(lblTherapyTotalVal);
        therapyTotalPanel.add(new JLabel("  " + MessageBundle.getMessage("angal.common.selected.txt") + ":"));
        lblTherapyTotalSelectionVal = new JLabel("0.00");
        therapyTotalPanel.add(lblTherapyTotalSelectionVal);

        headerPanel.add(selectAllTherapy);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(therapyTotalPanel);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableTherapy), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExamPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                MessageBundle.getMessage("angal.agetype.description"),
                MessageBundle.getMessage("angal.newbill.unitprice.txt")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Double.class;
                return String.class;
            }
        };

        for (Laboratory lab : laboratories) {
            double price = getExamPrice(lab, true);
            Object[] row = {lab.getExam().getDescription(), price};
            model.addRow(row);
        }

        tableExam = new JTable(model);
        tableExam.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        tableExam.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (selectAllExam != null) {
                    boolean allSelected = tableExam.getRowCount() > 0
                            && tableExam.getSelectedRowCount() == tableExam.getRowCount();
                    selectAllExam.setSelected(allSelected);
                }
                updateTotals();
            }
        });

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllExam = new JCheckBox(MessageBundle.getMessage("angal.selectprescription.selectallexams"));
        selectAllExam.addActionListener(e -> {
            if (selectAllExam.isSelected()) {
                tableExam.selectAll();
            } else {
                tableExam.clearSelection();
            }
            updateTotals();
        });

        JPanel examTotalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        examTotalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.total.txt") + ":"));
        lblExamTotalVal = new JLabel("0.00");
        examTotalPanel.add(lblExamTotalVal);
        examTotalPanel.add(new JLabel("  " + MessageBundle.getMessage("angal.common.selected.txt") + ":"));
        lblExamTotalSelectionVal = new JLabel("0.00");
        examTotalPanel.add(lblExamTotalSelectionVal);

        headerPanel.add(selectAllExam);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(examTotalPanel);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableExam), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                MessageBundle.getMessage("angal.agetype.description"),
                MessageBundle.getMessage("angal.newbill.unitprice.txt")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Double.class;
                return String.class;
            }
        };

        for (OperationRow op : operations) {
            double price = getOperationPrice(op, true);
            Object[] row = {op.getOperation().getDescription(), price};
            model.addRow(row);
        }

        tableOperation = new JTable(model);
        tableOperation.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        tableOperation.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (selectAllOperation != null) {
                    boolean allSelected = tableOperation.getRowCount() > 0
                            && tableOperation.getSelectedRowCount() == tableOperation.getRowCount();
                    selectAllOperation.setSelected(allSelected);
                }
                updateTotals();
            }
        });

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllOperation = new JCheckBox(MessageBundle.getMessage("angal.selectprescription.selectalloperations"));
        selectAllOperation.addActionListener(e -> {
            if (selectAllOperation.isSelected()) {
                tableOperation.selectAll();
            } else {
                tableOperation.clearSelection();
            }
            updateTotals();
        });

        JPanel opeTotalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        opeTotalPanel.add(new JLabel(MessageBundle.getMessage("angal.common.total.txt") + ":"));
        lblOpeTotalVal = new JLabel("0.00");
        opeTotalPanel.add(lblOpeTotalVal);
        opeTotalPanel.add(new JLabel("  " + MessageBundle.getMessage("angal.common.selected.txt") + ":"));
        lblOpeTotalSelectionVal = new JLabel("0.00");
        opeTotalPanel.add(lblOpeTotalSelectionVal);

        headerPanel.add(selectAllOperation);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(opeTotalPanel);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableOperation), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGlobalPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.summary.txt")));

        panel.add(new JLabel(MessageBundle.getMessage("angal.common.total.txt") + ":"));
        lblTotalVal = new JLabel("0.00");
        panel.add(lblTotalVal);

        panel.add(new JLabel("    " + MessageBundle.getMessage("angal.common.selected.txt") + ":"));
        lblTotalSelectionVal = new JLabel("0.00");
        panel.add(lblTotalSelectionVal);

        return panel;
    }

    private void updateTotals() {
        double therapyTotal = 0.0;
        double therapySelected = 0.0;
        double examTotal = 0.0;
        double examSelected = 0.0;
        double opeTotal = 0.0;
        double opeSelected = 0.0;

        // Therapy totals
        if (tableTherapy != null) {
            for (int i = 0; i < tableTherapy.getRowCount(); i++) {
                double price = (Double) tableTherapy.getValueAt(i, 6);
                int quantity = (Integer) tableTherapy.getValueAt(i, 4);
                double total = price * quantity;
                therapyTotal += total;

                if (tableTherapy.isRowSelected(i)) {
                    therapySelected += total;
                }
            }
        }

        // Exam totals
        if (tableExam != null) {
            for (int i = 0; i < tableExam.getRowCount(); i++) {
                double price = (Double) tableExam.getValueAt(i, 1);
                examTotal += price;

                if (tableExam.isRowSelected(i)) {
                    examSelected += price;
                }
            }
        }

        // Operation totals
        if (tableOperation != null) {
            for (int i = 0; i < tableOperation.getRowCount(); i++) {
                double price = (Double) tableOperation.getValueAt(i, 1);
                opeTotal += price;

                if (tableOperation.isRowSelected(i)) {
                    opeSelected += price;
                }
            }
        }

        // Update labels
        if (lblTherapyTotalVal != null) {
            lblTherapyTotalVal.setText(String.format("%.2f", therapyTotal));
            lblTherapyTotalSelectionVal.setText(String.format("%.2f", therapySelected));
        }
        if (lblExamTotalVal != null) {
            lblExamTotalVal.setText(String.format("%.2f", examTotal));
            lblExamTotalSelectionVal.setText(String.format("%.2f", examSelected));
        }
        if (lblOpeTotalVal != null) {
            lblOpeTotalVal.setText(String.format("%.2f", opeTotal));
            lblOpeTotalSelectionVal.setText(String.format("%.2f", opeSelected));
        }

        lblTotalVal.setText(String.format("%.2f", therapyTotal + examTotal + opeTotal));
        lblTotalSelectionVal.setText(String.format("%.2f", therapySelected + examSelected + opeSelected));
    }

    class SpinnerNumberEditor extends AbstractCellEditor implements TableCellEditor {
        private JSpinner spinner;

        public SpinnerNumberEditor(int min, int max, int step) {
            spinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
            spinner.addChangeListener(e -> {
                fireEditingStopped();
                updateTotals();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(value);
            return spinner;
        }
    }
}