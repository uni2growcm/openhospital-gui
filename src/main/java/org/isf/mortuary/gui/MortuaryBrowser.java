package org.isf.mortuary.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.DeathReasonManager;
import org.isf.mortuary.manager.MortuaryBrowserManager;
import org.isf.mortuary.model.DeathReason;
import org.isf.mortuary.model.Mortuary;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class MortuaryBrowser extends ModalJFrame {

	private static long TOTAL_PAGES;
	private static int CURRENT_PAGE = 0;
	private static long TOTAL_MOVEMENTS;
	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
	private JButton jCertificateButton;
	private JButton jMortuaryStayButton;
	private JButton filterButton;
	private JButton jRapportButton;
	private JButton jCloseButton;
	private JButton pickPatientButton;
	private JButton removePatientButton;
	private JButton searchButton;
	private JRadioButton inputRadioButton;
	private JRadioButton outputRadioButton;
	private JPanel inOutPanel;
	private JComboBox<String> provenanceCombo;
	private JComboBox<String> deathReasonCombo;
	private JTextField patientTextfield;
	private JTextField searchTextfield;
	private JLabel rowCounter;
	private MortuaryBrowserModel model;
	private JTable movTable;
	private String rowCounterText = MessageBundle.getMessage("angal.common.count.label") + ' ';
	//private List<Mortuary> pSur;
	private JTable jTableTotal;
	private int totalQti;
	private BigDecimal totalAmount;
	private int[] pColumnWidth = {30, 80, 30, 100, 100, 75, 75, 150};
	private String[] pColumns = {
		MessageBundle.getMessage("angal.mortuary.id.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.patient.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.sexe.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.declaring.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.provenance.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.indate.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.outdate.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.deathreason.col").toUpperCase(),
	};
	private static final String FROM_LABEL = MessageBundle.getMessage("angal.common.from.txt") + ':';
	private static final String TO_LABEL = MessageBundle.getMessage("angal.common.to.txt") + ':';
	private static final String TEXT_ALL = MessageBundle.getMessage("angal.common.all.txt");
	private int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
		SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT
	};
	private boolean[] pColumnBold = { true, false, false, false, false, false, false, false};
	private boolean[] pColumnVisible = { true, true, true, true, true, true, true, true};
	private List<Mortuary> mortuaries;
	private final JFrame myFrame;

	private MortuaryBrowserManager mortuaryBrowserManager = Context.getApplicationContext().getBean(MortuaryBrowserManager.class);
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private DeathReasonManager deathReasonManager = Context.getApplicationContext().getBean(DeathReasonManager.class);
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private JButton prevButton;
	private JButton nextButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel underLabel;
	private JLabel totalMortuaryLabel;
	public MortuaryBrowser() throws OHException, OHServiceException {
		super();
		myFrame = this;
		initialize();
		setLocationRelativeTo(null);
	}

	private void initialize() throws OHException, OHServiceException {
		this.setTitle(MessageBundle.getMessage("angal.mortuary.browser.title"));
		this.setContentPane(getJContainPanel());
		this.setMinimumSize(new Dimension(700 + getJTableWidth(), 700));//rowCounter.setText(rowCounterText + pSur.size());
		validate();
	}

	/**
	 * This method initializes containPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContainPanel() throws OHException, OHServiceException {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(getJSelectionPanel(), BorderLayout.WEST);
			jContainPanel.add(getTablePanel(), BorderLayout.CENTER);
			validate();
		}
		return jContainPanel;
	}

	private JPanel getJSelectionPanel() throws OHServiceException {
		JPanel jSelectionPanel = new JPanel();
		jSelectionPanel.add(getJSelectionContentPanel());
		return jSelectionPanel;
	}

	private JPanel getJSelectionContentPanel() throws OHServiceException {
		JPanel jSelectionContentPanel = new JPanel();
		jSelectionContentPanel.setLayout(new BoxLayout(jSelectionContentPanel, BoxLayout.Y_AXIS));
		jSelectionContentPanel.add(getSearchPatientPanel());
		jSelectionContentPanel.add(getProvenancePanel());
		jSelectionContentPanel.add(getInOutPanel());
		jSelectionContentPanel.add(getDeathReasonPanel());
		jSelectionContentPanel.add(getFilterButtonPanel());
		return jSelectionContentPanel;
	}

	private JPanel getSearchPatientPanel() {
		JPanel searchPatientPanel = new JPanel();
		searchPatientPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.searchpatient.border")));
		patientTextfield = new JTextField(14);
		searchPatientPanel.add(patientTextfield);
		searchPatientPanel.add(getPickPatientButton());
		searchPatientPanel.add(getRemovePatientButton());
		return searchPatientPanel;
	}

	private JButton getPickPatientButton() {
		if(pickPatientButton == null) {
			pickPatientButton = new JButton();
			pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
			pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.billbrowser.selectapatient.tooltip"));
		}
		return pickPatientButton;
	}

	private JButton getRemovePatientButton() {
		if(removePatientButton == null) {
			removePatientButton = new JButton();
			removePatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
			removePatientButton.setToolTipText(MessageBundle.getMessage("angal.billbrowser.removeapatient.tooltip"));
		}
		return removePatientButton;
	}

	private JPanel getProvenancePanel() throws OHServiceException {
		JPanel provenancePanel = new JPanel();
		provenancePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.provenance.border")));
		provenancePanel.add(getProvenanceCombo());
		return provenancePanel;
	}

	private JComboBox<String> getProvenanceCombo() throws OHServiceException {
		if(provenanceCombo == null) {
			provenanceCombo = new JComboBox<String>();
			provenanceCombo.setPreferredSize(new Dimension(200, 24));
		}
		List<Ward> wards = wardBrowserManager.getWards();
		provenanceCombo.addItem(TEXT_ALL);
		for(Ward ward : wards) {
			provenanceCombo.addItem(ward.getDescription());
		}
		return provenanceCombo;
	}

	private JPanel getInOutPanel() {
		JPanel InOutPanel = new JPanel();
		InOutPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.inout.border")));
		InOutPanel.setLayout(new BoxLayout(InOutPanel, BoxLayout.Y_AXIS));
		InOutPanel.add(getEnteredPanel());
		InOutPanel.add(getDateFromPanel());
		InOutPanel.add(getDateToPanel());
		return InOutPanel;
	}

	private JPanel getEnteredPanel() {
		if(inOutPanel == null) {
			inOutPanel = new JPanel();
			inputRadioButton = new JRadioButton();
			outputRadioButton = new JRadioButton();
			JLabel inputLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.input.txt"));
			JLabel outputLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.output.txt"));
			inputRadioButton.setSelected(true);
			inOutPanel.add(inputRadioButton);
			inOutPanel.add(inputLabel);
			inOutPanel.add(outputRadioButton);
			inOutPanel.add(outputLabel);
		}
		return inOutPanel;
 	}

	private JPanel getDateFromPanel() {
		JPanel dateFromPanel = new JPanel();
		dateFromPanel.setLayout(new BorderLayout());
		JLabel dateFromLabel = new JLabel(FROM_LABEL);
		dateFrom = new GoodDateChooser();
		dateFromPanel.add(dateFromLabel, BorderLayout.WEST);
		dateFromPanel.add(dateFrom, BorderLayout.EAST);
		return dateFromPanel;
	}

	private JPanel getDateToPanel() {
		JPanel dateToPanel = new JPanel();
		dateToPanel.setLayout(new BorderLayout());
		JLabel dateToLabel = new JLabel(TO_LABEL);
		dateTo = new GoodDateChooser();
		dateToPanel.add(dateToLabel,BorderLayout.WEST);
		dateToPanel.add(dateTo, BorderLayout.EAST);
		return dateToPanel;
	}

	private JPanel getDeathReasonPanel() throws OHServiceException {
		JPanel provenancePanel = new JPanel();
		provenancePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.mortuary.deathreason.border")));
		provenancePanel.add(getDeathReasonCombo());
		return provenancePanel;
	}

	private JComboBox<String> getDeathReasonCombo() throws OHServiceException {
		if(deathReasonCombo == null) {
			deathReasonCombo = new JComboBox<String>();
			deathReasonCombo.setPreferredSize(new Dimension(200, 24));
		}
		List<DeathReason> deathReasons = deathReasonManager.getAll();
		deathReasonCombo.addItem(TEXT_ALL);
		for(DeathReason deathReason : deathReasons) {
			deathReasonCombo.addItem(deathReason.getDescription());
		}
		return deathReasonCombo;
	}

	private JPanel getFilterButtonPanel() {
		JPanel jFilterButtonPanel = new JPanel();
		jFilterButtonPanel.add(getFilterButton());
		return jFilterButtonPanel;
	}

	private JButton getFilterButton() {
		if(filterButton == null)  {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.filter.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.filter.btn.key"));
			filterButton.addActionListener(actionEvent -> {

			});
		}
		return filterButton;
	}

	private JPanel getJButtonPanel() {
		if(jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getNewButton());
			jButtonPanel.add(getEditButton());
			jButtonPanel.add(getDeleteButton());
			jButtonPanel.add(getCertificateButton());
			jButtonPanel.add(getMortuaryStayButton());
			jButtonPanel.add(getRapportButton());
			jButtonPanel.add(getCloseButton());
		}
		return jButtonPanel;
	}

	private int getJTableWidth() {
		return Arrays.stream(pColumnWidth).sum();
	}

	private JButton getNewButton() {
		if(jNewButton == null) {
			jNewButton =  new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		}
		return jNewButton;
	}

	private JButton getEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		}
		return jEditButton;
	}

	private JButton getDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		}
		return jDeleteButton;
	}

	private JButton getCertificateButton() {
		if (jCertificateButton == null) {
			jCertificateButton = new JButton(MessageBundle.getMessage("angal.mortuary.certificate.btn"));
			jCertificateButton.setMnemonic(MessageBundle.getMnemonic("angal.mortuary.certificate.btn.key"));
		}
		return jCertificateButton;
	}

	private JButton getMortuaryStayButton() {
		if (jMortuaryStayButton == null) {
			jMortuaryStayButton = new JButton(MessageBundle.getMessage("angal.mortuary.mortuarystay.btn"));
			jMortuaryStayButton.setMnemonic(MessageBundle.getMnemonic("angal.mortuary.mortuarystay.btn.key"));
		}
		return jMortuaryStayButton;
	}

	private JButton getRapportButton() {
		if (jRapportButton == null) {
			jRapportButton = new JButton(MessageBundle.getMessage("angal.mortuary.rapport.btn"));
			jRapportButton.setMnemonic(MessageBundle.getMnemonic("angal.mortuary.rapport.btn.key"));
		}
		return jRapportButton;
	}

	private JButton getCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	private JPanel getTablePanel() throws OHException, OHServiceException {
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(getSearchPanel(), BorderLayout.NORTH);
		tablePanel.add(getTable(), BorderLayout.CENTER);
		tablePanel.add(getPaginationPanel(), BorderLayout.SOUTH);
		return tablePanel;
	}

	private JPanel getSearchPanel() {
		JPanel searchPanel = new JPanel();
		searchTextfield = new JTextField(20);
		searchPanel.add(searchTextfield);
		searchPanel.add(getSearchButton());
		return searchPanel;
	}

	private JButton getSearchButton() {
		if(searchButton == null){
			searchButton = new JButton();
			searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		}
		return searchButton;
	}

	private JScrollPane getTable() throws OHException {
		JScrollPane scrollPane = new JScrollPane(getMovTable());
		int totWidth = 0;
		for (int colWidth : pColumnWidth) {
			totWidth += colWidth;
		}
		scrollPane.setPreferredSize(new Dimension(totWidth, 450));
		return scrollPane;
	}

	private JTable getMovTable() throws OHException {

		model = new MortuaryBrowserModel();
		movTable = new JTable(model);

		for (int i = 0; i < pColumns.length; i++) {
			movTable.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
			movTable.getColumnModel().getColumn(i).setPreferredWidth(pColumnWidth[i]);
			if (!pColumnVisible[i]) {
				movTable.getColumnModel().getColumn(i).setMinWidth(0);
				movTable.getColumnModel().getColumn(i).setMaxWidth(0);
				movTable.getColumnModel().getColumn(i).setWidth(0);
			}
		}
		return movTable;
	}

	private JPanel getPaginationPanel() throws OHServiceException {
		JPanel jPaginationPanel = new JPanel();
		jPaginationPanel.add(getPrevButton());
		jPaginationPanel.add(getPagesCombo());
		jPaginationPanel.add(getUnderLabel());
		jPaginationPanel.add(getNextButton());
		jPaginationPanel.add(getTotalMortuaryLabel());
		return jPaginationPanel;
	}

	private JButton getPrevButton() {
		if (prevButton == null) {
			prevButton = new JButton("<");
			prevButton.setEnabled(CURRENT_PAGE > 0);
			prevButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE > 0) {
					CURRENT_PAGE--;
					pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
				}
			});
		}
		return prevButton;
	}

	private JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton(">");
			nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES -1 && TOTAL_PAGES != 1);
			nextButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE < TOTAL_PAGES -1) {
					CURRENT_PAGE++;
					pagesCombo.setSelectedItem(CURRENT_PAGE + 1);
				}
			});
		}
		return nextButton;
	}

	private JComboBox<Integer> getPagesCombo() {
		if (pagesCombo == null) {
			pagesCombo = new JComboBox<>();
			pagesCombo.setPreferredSize(new Dimension(100, 25));
			for (int i = 0; i <= TOTAL_PAGES; i++) {
				pagesCombo.addItem(i + 1);
			}
			pagesCombo.addActionListener(actionEvent -> {
				if (pagesCombo.getItemCount() != 0) {
					if (pagesCombo.getSelectedItem() != null) {
						CURRENT_PAGE = (Integer) pagesCombo.getSelectedItem() - 1;
						applyFilter(CURRENT_PAGE);
					}
				}
			});
		}
		return pagesCombo;
	}

	private void applyFilter(int currentPage) {
		nextButton.setEnabled(currentPage < TOTAL_PAGES -1 && TOTAL_PAGES != 1);
		prevButton.setEnabled(currentPage > 0);
	}

	private JLabel getUnderLabel() {
		if (underLabel == null) {
			underLabel = new JLabel("/ " + (TOTAL_PAGES + 1) + " " + MessageBundle.getMessage("angal.common.pages.txt"));
			underLabel.setPreferredSize(new Dimension(60, 30));
		}
		return underLabel;
	}

	private JLabel getTotalMortuaryLabel() throws OHServiceException {
		if (totalMortuaryLabel == null) {
			totalMortuaryLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.totalmortuary.txt") +": "+ TOTAL_MOVEMENTS);
		}
		return totalMortuaryLabel;
	}

	class MortuaryBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MortuaryBrowserModel() throws OHException {
			mortuaries = mortuaryBrowserManager.getAll();
			updateTotals();
		}

		@Override
		public int getRowCount() {
			if (mortuaries == null) {
				return 0;
			}
			return mortuaries.size();
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}

		/**
		 * Note: We must get the objects in a reversed way because of the query
		 *
		 * @see org.isf.mortuary.service.MortuaryIoOperations
		 */
		@Override
		public Object getValueAt(int r, int c) {
			Mortuary mortuary = mortuaries.get(r);
			Patient patient = mortuary.getPatient();
			DeathReason deathReason = mortuary.getDeathReason();
			int col = -1;
			if (c == col) {
				return mortuary;
			} else if (c == ++col) {
				return mortuary.getId();
			} else if (c == ++col) {
				return patient.getName();
			} else if (c == ++col) {
				return patient.getSex();
			} else if (c == ++col) {
				return mortuary.getDeclaringName();
			} else if (c == ++col) {
				return mortuary.getProvenance();
			} else if (c == ++col) {
				return mortuary.getEnteredDate();
			} else if (c == ++col) {
				return mortuary.getReleaseDate();
			} else if (c == ++col) {
				return deathReason.getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	public void updateTotals() {
		if (jTableTotal == null) {
			return;
		}
		totalQti = 0;
		totalAmount = new BigDecimal(0);

		jTableTotal.getModel().setValueAt(MessageBundle.getMessage("angal.common.notapplicable.txt"), 0, 4);
	}

	class EnabledTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			if (pColumnBold[column]) {
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}

	class DecimalFormatRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private final DecimalFormat formatter10 = new DecimalFormat("#,##0.00");
		private final DecimalFormat formatter1 = new DecimalFormat("#,##0");

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			if (column == 4 && value instanceof Number) {
				value = formatter1.format(value);
			}
			if (column == 11 && value instanceof Number) {
				value = formatter10.format(value);
			}
			if (column == 12 && value instanceof Number) {
				value = formatter10.format(value);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}
