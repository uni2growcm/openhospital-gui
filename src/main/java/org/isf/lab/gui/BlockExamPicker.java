package org.isf.lab.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

import org.isf.exa.model.Block;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableModelBlockExam;

public class BlockExamPicker extends JPanel {

	private static final long serialVersionUID = 1L;
	private final OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
	private JTable jTableData;
	private OhTableModelBlockExam blockModel;
	private List<Block> selectedBlocks = new ArrayList<>();

	public BlockExamPicker(OhTableModelBlockExam model) {
		this.blockModel = model;
		initComponents();
	}

	private void initComponents() {
		JPanel jPanel3 = new JPanel();
		JPanel jPanel1 = new JPanel();

		setLayout(new BorderLayout(10, 10));
		add(jPanel1, BorderLayout.CENTER);
		GridBagLayout gbl_jPanel1 = new GridBagLayout();
		gbl_jPanel1.columnWidths = new int[]{575, 0};
		gbl_jPanel1.rowHeights = new int[]{268, 0};
		gbl_jPanel1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_jPanel1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		jPanel1.setLayout(gbl_jPanel1);

		JScrollPane jScrollPane1 = new JScrollPane();
		jTableData = new JTable();
		jTableData.setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (super.isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				} else {
					super.addSelectionInterval(index0, index1);
				}
			}
		});
		jTableData.setDefaultRenderer(Object.class, cellRenderer);
		jTableData.setDefaultRenderer(Double.class, cellRenderer);
		jTableData.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				cellRenderer.setHoveredRow(itsRow >= 0 ? itsRow : -1);
				aTable.repaint();
			}
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		jTableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}
		});
		jTableData.setModel(blockModel);
		jTableData.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTableData.setShowVerticalLines(false);
		jTableData.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					int row = jTableData.rowAtPoint(evt.getPoint());
					if (row >= 0 && !jTableData.isRowSelected(row)) {
						jTableData.addRowSelectionInterval(row, row);
					}
					validateSelection();
				}
			}
		});
		jTableData.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					validateSelection();
				}
				super.keyPressed(e);
			}
		});

		jScrollPane1.setViewportView(jTableData);
		GridBagConstraints scrollGbc = new GridBagConstraints();
		scrollGbc.insets = new Insets(0, 15, 0, 15);
		scrollGbc.fill = GridBagConstraints.BOTH;
		scrollGbc.gridx = 0;
		scrollGbc.gridy = 0;
		jPanel1.add(jScrollPane1, scrollGbc);

		add(jPanel3, BorderLayout.NORTH);
		GridBagLayout gbl_jPanel3 = new GridBagLayout();
		gbl_jPanel3.columnWidths = new int[] {90, 237, 0};
		gbl_jPanel3.rowHeights = new int[] {50, 0};
		gbl_jPanel3.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_jPanel3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		jPanel3.setLayout(gbl_jPanel3);

		JLabel jLabelImage = new JLabel();
		jLabelImage.setIcon(new javax.swing.ImageIcon("rsc/icons/operation_dialog.png"));
		jLabelImage.setText(MessageBundle.getMessage("angal.common.search.btn"));
		GridBagConstraints labelGbc = new GridBagConstraints();
		labelGbc.anchor = GridBagConstraints.WEST;
		labelGbc.insets = new Insets(0, 15, 0, 5);
		labelGbc.gridx = 0;
		labelGbc.gridy = 0;
		jPanel3.add(jLabelImage, labelGbc);

		JTextField jTextFieldFind = new JTextField();
		jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { jTableData.clearSelection(); }
			@Override
			public void removeUpdate(DocumentEvent e) { jTableData.clearSelection(); }
			@Override
			public void changedUpdate(DocumentEvent e) { jTableData.clearSelection(); }
		});
		jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { applyFilter(jTextFieldFind.getText()); }
			@Override
			public void removeUpdate(DocumentEvent e) { applyFilter(jTextFieldFind.getText()); }
			@Override
			public void changedUpdate(DocumentEvent e) { }
		});
		jTextFieldFind.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) { validateSelection(); }
				super.keyPressed(e);
			}
		});
		GridBagConstraints fieldGbc = new GridBagConstraints();
		fieldGbc.insets = new Insets(0, 0, 0, 15);
		fieldGbc.fill = GridBagConstraints.HORIZONTAL;
		fieldGbc.gridx = 1;
		fieldGbc.gridy = 0;
		jPanel3.add(jTextFieldFind, fieldGbc);

		JPanel jPanel2 = new JPanel();
		JButton jButtonSelect = new JButton(MessageBundle.getMessage("angal.common.select.btn"));
		JButton jButtonQuit = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));

		jButtonSelect.addActionListener(actionEvent -> validateSelection());
		jButtonQuit.addActionListener(actionEvent -> {
			selectedBlocks.clear();
			setVisible(false);
			if (getParentFrame() != null) getParentFrame().dispose();
		});

		jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		jPanel2.add(jButtonSelect);
		jPanel2.add(jButtonQuit);
		add(jPanel2, BorderLayout.SOUTH);
	}

	private void applyFilter(String text) {
		blockModel.filter(text);
		if (jTableData.getRowCount() > 0) {
			jTableData.setRowSelectionInterval(0, 0);
		}
		jTableData.repaint();
	}

	private void validateSelection() {
		selectedBlocks.clear();
		int[] selectedRows = jTableData.getSelectedRows();
		for (int row : selectedRows) {
			Block block = blockModel.getObjectAt(row);
			if (block != null) {
				selectedBlocks.add(block);
			}
		}
		setVisible(false);
		if (getParentFrame() != null) getParentFrame().dispose();
	}

	public List<Block> getAllSelectedBlocks() {
		return selectedBlocks;
	}

	private JDialog parentFrame;

	public JDialog getParentFrame() {
		return parentFrame;
	}

	public void setParentFrame(JDialog parentFrame) {
		this.parentFrame = parentFrame;
	}
}