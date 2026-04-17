package org.isf.accounting.gui;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.OhTableModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class BillItemPicker<T> extends JDialog {

    private final JTable table;
    private static final int CODE_COLUMN_WIDTH = 100;
    private final JTextField searchField;
    private T selectedObject;

    public BillItemPicker(Window parent, String title, TableModel model, Icon dialogIcon) {

        super(parent, title, ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        if (dialogIcon != null) {
            JLabel iconLabel = new JLabel(dialogIcon);
            topPanel.add(iconLabel, BorderLayout.WEST);
        }

        JPanel searchPanel = new JPanel();
        GridBagLayout gbl_searchPanel = new GridBagLayout();
        gbl_searchPanel.columnWidths = new int[] { 90, 237, 0 };
        gbl_searchPanel.rowHeights = new int[] { 50, 0 };
        gbl_searchPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_searchPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        searchPanel.setLayout(gbl_searchPanel);

        JLabel searchFieldLabel = new JLabel();
        searchFieldLabel.setText(MessageBundle.getMessage("angal.billbrowser.find"));
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.anchor = GridBagConstraints.WEST;
        gbc_label.insets = new Insets(0, 15, 0, 5);
        gbc_label.gridx = 0;
        gbc_label.gridy = 0;
        searchPanel.add(searchFieldLabel, gbc_label);

        searchField = new JTextField();
        GridBagConstraints gbc_textFieldFind = new GridBagConstraints();
        gbc_textFieldFind.insets = new Insets(0, 0, 0, 15);
        gbc_textFieldFind.fill = GridBagConstraints.HORIZONTAL;
        gbc_textFieldFind.gridx = 1;
        gbc_textFieldFind.gridy = 0;
        searchPanel.add(searchField, gbc_textFieldFind);

        topPanel.add(searchPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        table = new JTable(model);
        table.getColumnModel().getColumn(0).setMaxWidth(CODE_COLUMN_WIDTH);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton btnSelect = new JButton(MessageBundle.getMessage("angal.billbrowser.select"));
        JButton btnCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));

        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(sorter); }
            @Override public void removeUpdate(DocumentEvent e) { filter(sorter); }
            @Override public void changedUpdate(DocumentEvent e) { filter(sorter); }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> table.requestFocus();
                    case KeyEvent.VK_ENTER -> confirmSelection();
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmSelection();
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });

        btnSelect.addActionListener(e -> confirmSelection());
        btnCancel.addActionListener(e -> dispose());

        setSize(650, 450);
        setLocationRelativeTo(parent);
    }

    private void filter(TableRowSorter<TableModel> sorter) {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private void confirmSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            selectedObject = (T) ((OhTableModel<?>) table.getModel()).getObjectAt(modelRow);
            dispose();
        }
    }

    public T getSelectedObject() {
        return selectedObject;
    }

    public static <T> T showPicker(Component parent, String title, TableModel model, Icon icon) {

        Window window = SwingUtilities.getWindowAncestor(parent);

        BillItemPicker<T> dialog = new BillItemPicker<>(window, title, model, icon);

        dialog.setVisible(true);
        return dialog.getSelectedObject();
    }
}