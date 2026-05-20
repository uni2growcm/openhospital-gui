package org.isf.accounting.gui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.isf.accounting.model.BillItems;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.OperationRow;
import org.isf.patient.model.Patient;
import org.isf.priceslist.model.ItemGroup;
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

    private EventListenerList selectionListener = new EventListenerList();

    // Core managers - obtained from Spring Context
    private TherapyManager therapyManager;
    private LabManager labManager;
    private OperationRowBrowserManager opManager;

    public interface PrescriptionSelectionListener extends EventListener {
        void prescriptionSelected(List<BillItems> prescriptions);
    }

    public SelectPrescriptions(JDialog owner, Patient patient) {
        super(owner, true);
        this.patient = patient;

        // Initialize managers from Spring Context
        initManagers();

        loadTherapies();
        loadLaboratories();
        loadOperations();

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initManagers() {
        try {
            therapyManager = Context.getApplicationContext().getBean(TherapyManager.class);
            labManager = Context.getApplicationContext().getBean(LabManager.class);
            opManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
        } catch (Exception e) {
            MessageDialog.error(this, "Error initializing managers: " + e.getMessage());
            // Fallback: create empty lists
            therapies = new ArrayList<>();
            laboratories = new ArrayList<>();
            operations = new ArrayList<>();
        }
    }

    private void loadTherapies() {
        if (therapyManager == null) {
            therapies = new ArrayList<>();
            return;
        }

        try {
            therapies = therapyManager.getTherapyRows(patient.getCode());

            // Filter therapies with remaining quantity > 0
            therapies.removeIf(therapy -> {
                double remaining = therapy.getQty();
                if (remaining > 0) {
                    therapyQuantities.put(therapy.getTherapyID(), (int) Math.ceil(remaining));
                    return false;
                }
                return true;
            });
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e, this);
            therapies = new ArrayList<>();
        }
    }

    private void loadLaboratories() {
        if (labManager == null) {
            laboratories = new ArrayList<>();
            return;
        }

        try {
            // Get laboratories without bill using patient code
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
            // Get operations without bill - pass Patient object, not String
            operations = opManager.getOperationWithoutBill(patient);
            if (operations == null) {
                operations = new ArrayList<>();
            }
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e, this);
            operations = new ArrayList<>();
        }
    }

    public void addPrescriptionSelectedListener(PrescriptionSelectionListener listener) {
        selectionListener.add(PrescriptionSelectionListener.class, listener);
    }

    private void firePrescriptionSelected() {
        List<BillItems> prescriptions = new ArrayList<>();

        // Add selected therapies
        if (tableTherapy != null) {
            int[] selectedRows = tableTherapy.getSelectedRows();
            for (int row : selectedRows) {
                if (row < therapies.size()) {
                    TherapyRow therapy = therapies.get(row);
                    BillItems item = new BillItems();
                    item.setItemDescription(getMedicalDescription(therapy.getMedicalId()));
                    item.setItemGroup(ItemGroup.MEDICAL.getCode());
                    item.setItemId(String.valueOf(therapy.getMedicalId()));
                    item.setPrescriptionId(therapy.getTherapyID());
                    item.setItemQuantity(therapyQuantities.getOrDefault(therapy.getTherapyID(), 0));
                    prescriptions.add(item);
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
                    item.setPrescriptionId(lab.getCode());
                    item.setItemQuantity(1);
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
                    item.setPrescriptionId(op.getId());
                    item.setItemQuantity(1);
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

    private void initComponents() {
        setTitle(MessageBundle.getMessage("angal.patientbill.prescription"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel(MessageBundle.getMessage("angal.therapy.therapyofpatientname") + " " + patient.getName(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        // Select all checkbox
        JCheckBox selectAllCheckBox = new JCheckBox(MessageBundle.getMessage("angal.selectprescription.selectall"));
        selectAllCheckBox.addActionListener(e -> {
            if (selectAllCheckBox.isSelected()) {
                selectAllRows();
            } else {
                clearAllSelections();
            }
        });
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(selectAllCheckBox);
        mainPanel.add(topPanel);

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
            firePrescriptionSelected();
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(validateButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 650);
    }

    private void selectAllRows() {
        if (tableTherapy != null) tableTherapy.selectAll();
        if (tableExam != null) tableExam.selectAll();
        if (tableOperation != null) tableOperation.selectAll();
    }

    private void clearAllSelections() {
        if (tableTherapy != null) tableTherapy.clearSelection();
        if (tableExam != null) tableExam.clearSelection();
        if (tableOperation != null) tableOperation.clearSelection();
    }

    private JPanel createTherapyPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                MessageBundle.getMessage("angal.therapy.startdate"),
                MessageBundle.getMessage("angal.priceslist.medicals"),
                MessageBundle.getMessage("angal.newbill.qty"),
                MessageBundle.getMessage("angal.therapy.remaining")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (TherapyRow therapy : therapies) {
            LocalDateTime startDate = therapy.getStartDate();
            String startDateStr = startDate != null ? startDate.format(formatter) : "";

            int totalQty = therapy.getQty().intValue();
            int remaining = totalQty;

            Object[] row = {
                    startDateStr,
                    getMedicalDescription(therapy.getMedicalId()),
                    totalQty,
                    remaining
            };
            model.addRow(row);
        }

        tableTherapy = new JTable(model);
        tableTherapy.getColumnModel().getColumn(3).setCellEditor(new SpinnerNumberEditor(0, 9999, 1));
        tableTherapy.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableTherapy.getSelectionModel().addListSelectionListener(e -> updateTherapyQuantity());

        panel.add(new JScrollPane(tableTherapy), BorderLayout.CENTER);
        return panel;
    }

    private void updateTherapyQuantity() {
        int selectedRow = tableTherapy.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < therapies.size()) {
            int remaining = (Integer) tableTherapy.getValueAt(selectedRow, 3);
            TherapyRow therapy = therapies.get(selectedRow);
            therapyQuantities.put(therapy.getTherapyID(), remaining);
        }
    }

    private JPanel createExamPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                MessageBundle.getMessage("angal.agetype.description")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Laboratory lab : laboratories) {
            model.addRow(new Object[]{lab.getExam().getDescription()});
        }

        tableExam = new JTable(model);
        tableExam.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        panel.add(new JScrollPane(tableExam), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                MessageBundle.getMessage("angal.agetype.description")
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (OperationRow op : operations) {
            model.addRow(new Object[]{op.getOperation().getDescription()});
        }

        tableOperation = new JTable(model);
        tableOperation.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        panel.add(new JScrollPane(tableOperation), BorderLayout.CENTER);
        return panel;
    }

    class SpinnerNumberEditor extends AbstractCellEditor implements TableCellEditor {
        private JSpinner spinner;

        public SpinnerNumberEditor(int min, int max, int step) {
            spinner = new JSpinner(new SpinnerNumberModel(min, min, max, step));
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