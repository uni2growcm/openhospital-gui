/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

package org.isf.mortuary.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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

import org.isf.accounting.gui.PatientBillEdit.PatientBillListener;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.mortuary.manager.DeathReasonManager;
import org.isf.mortuary.manager.MortuaryBrowserManager;
import org.isf.mortuary.model.DeathReason;
import org.isf.mortuary.model.Mortuary;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class MortuaryBrowser extends ModalJFrame implements PatientBillListener {

	private static final String FROM_LABEL = MessageBundle.getMessage("angal.common.from.txt") + ':';
	private static final String TO_LABEL = MessageBundle.getMessage("angal.common.to.txt") + ':';
	private static final String TEXT_ALL = MessageBundle.getMessage("angal.common.all.txt");
	private final int PAGE_SIZE = 3;
	private final JFrame myFrame;
	private long TOTAL_PAGES;
	private int CURRENT_PAGE = 0;
	private long TOTAL_MORTUARIES;
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
	private Patient patientParent;
	private MortuaryBrowserModel model;
	private JTable movTable;
	private final String rowCounterText = MessageBundle.getMessage("angal.common.count.label") + ' ';
	private JTable jTableTotal;
	private int totalQti;
	private BigDecimal totalAmount;
	private final boolean isSingleUser = GeneralData.getGeneralData().getSINGLEUSER();
	private final int[] pColumnWidth = { 30, 80, 30, 100, 100, 75, 75, 150 };
	private final String[] pColumns = {
		MessageBundle.getMessage("angal.mortuary.id.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.patient.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.sexe.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.declaring.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.provenance.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.indate.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.outdate.col").toUpperCase(),
		MessageBundle.getMessage("angal.mortuary.deathreason.col").toUpperCase(),
	};
	private final int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
		SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT
	};
	private final boolean[] pColumnBold = { true, false, false, false, false, false, false, false };
	private final boolean[] pColumnVisible = { true, true, true, true, true, true, true, true };
	private List<Mortuary> mortuaries;
	private final MortuaryBrowserManager mortuaryBrowserManager = Context.getApplicationContext().getBean(MortuaryBrowserManager.class);
	private final WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private final DeathReasonManager deathReasonManager = Context.getApplicationContext().getBean(DeathReasonManager.class);
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private JButton prevButton;
	private JButton nextButton;
	private JComboBox<Integer> pagesCombo;
	private JLabel underLabel;
	private JLabel totalMortuaryLabel;
	private boolean isSearch;

	public MortuaryBrowser() throws OHException, OHServiceException {
		super();
		myFrame = this;
		initialize();
		filterButton.doClick();
		setLocationRelativeTo(null);
	}

	@Override
	public void billInserted(AWTEvent event) {
		updateTotals();
		if (event != null) {
			Mortuary mortuaryInserted = (Mortuary) event.getSource();
			if (mortuaryInserted != null) {
				int insertedId = mortuaryInserted.getId();
				IntStream.range(0, movTable.getRowCount()).forEach(i -> {
					Mortuary aMortuary = (Mortuary) movTable.getModel().getValueAt(i, -1);
					if (aMortuary.getId() == insertedId) {
						movTable.getSelectionModel().setSelectionInterval(i, i);
					}
				});
			}
		}
	}
	private void initialize() throws OHException, OHServiceException {
		this.setTitle(MessageBundle.getMessage("angal.mortuary.browser.title"));
		this.setContentPane(getJContainPanel());
		this.setMinimumSize(new Dimension(700 + getJTableWidth(), 700));
	}

	/**
	 * This method initializes containPanel
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
		if (pickPatientButton == null) {
			pickPatientButton = new JButton();
			pickPatientButton.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
			pickPatientButton.setToolTipText(MessageBundle.getMessage("angal.billbrowser.selectapatient.tooltip"));
			pickPatientButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					SelectPatient selectPatient = new SelectPatient(MortuaryBrowser.this, false, true);
					selectPatient.addSelectionListener(MortuaryBrowser.this);
					selectPatient.setVisible(true);
					Patient pat = selectPatient.getPatient();

					try {
						patientSelected(pat);
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
			});
		}
		return pickPatientButton;
	}

	public void patientSelected(Patient patient) throws OHServiceException {
		patientParent = patient;
		patientTextfield.setText(patientParent != null ? patientParent.getName() : "");
	}

	private JButton getRemovePatientButton() {
		if (removePatientButton == null) {
			removePatientButton = new JButton();
			removePatientButton.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
			removePatientButton.setToolTipText(MessageBundle.getMessage("angal.billbrowser.removeapatient.tooltip"));
			removePatientButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					patientParent = null;
					patientTextfield.setText("");
				}
			});
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
		if (provenanceCombo == null) {
			provenanceCombo = new JComboBox<String>();
			provenanceCombo.setPreferredSize(new Dimension(200, 24));
		}
		List<Ward> wards = wardBrowserManager.getWards();
		provenanceCombo.addItem(TEXT_ALL);
		for (Ward ward : wards) {
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
		if (inOutPanel == null) {
			inOutPanel = new JPanel();
			ButtonGroup group = new ButtonGroup();
			inputRadioButton = new JRadioButton(MessageBundle.getMessage("angal.mortuary.input.txt"));
			outputRadioButton = new JRadioButton(MessageBundle.getMessage("angal.mortuary.output.txt"));
			inputRadioButton.setSelected(true);
			group.add(inputRadioButton);
			group.add(outputRadioButton);
			inOutPanel.add(inputRadioButton);
			inOutPanel.add(outputRadioButton);
		}
		return inOutPanel;
	}

	private JPanel getDateFromPanel() {
		JPanel dateFromPanel = new JPanel();
		dateFromPanel.setLayout(new BorderLayout());
		JLabel dateFromLabel = new JLabel(FROM_LABEL);
		dateFrom = new GoodDateChooser(LocalDate.now().minusWeeks(1));
		dateFromPanel.add(dateFromLabel, BorderLayout.WEST);
		dateFromPanel.add(dateFrom, BorderLayout.EAST);
		return dateFromPanel;
	}

	private JPanel getDateToPanel() {
		JPanel dateToPanel = new JPanel();
		dateToPanel.setLayout(new BorderLayout());
		JLabel dateToLabel = new JLabel(TO_LABEL);
		dateTo = new GoodDateChooser();
		dateToPanel.add(dateToLabel, BorderLayout.WEST);
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
		if (deathReasonCombo == null) {
			deathReasonCombo = new JComboBox<String>();
			deathReasonCombo.setPreferredSize(new Dimension(200, 24));
		}
		List<DeathReason> deathReasons = deathReasonManager.getAll();
		deathReasonCombo.addItem(TEXT_ALL);
		for (DeathReason deathReason : deathReasons) {
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
		if (filterButton == null) {
			filterButton = new JButton(MessageBundle.getMessage("angal.common.filter.btn"));
			filterButton.setMnemonic(MessageBundle.getMnemonic("angal.common.filter.btn.key"));
			filterButton.addActionListener(actionEvent -> {
				String provenanceSelected;
				String deathReasonSelected;
				String inputOrOutput = getInputOrOutput();

				if (!(provenanceCombo.getSelectedItem() instanceof String)) {
					provenanceSelected = (String) provenanceCombo.getSelectedItem();
				} else {
					provenanceSelected = (String) provenanceCombo.getSelectedItem();
					if (provenanceSelected.equals(TEXT_ALL)) {
						provenanceSelected = null;
					}
				}
				if (!(deathReasonCombo.getSelectedItem() instanceof String)) {
					deathReasonSelected = (String) deathReasonCombo.getSelectedItem();
				} else {
					deathReasonSelected = (String) deathReasonCombo.getSelectedItem();
					if (deathReasonSelected.equals(TEXT_ALL)) {
						deathReasonSelected = null;
					}
				}

				try {
					TOTAL_MORTUARIES = mortuaryBrowserManager.countTotalMovements(
						patientTextfield.getText().trim(),
						provenanceSelected,
						dateFrom.getDateStartOfDay(),
						dateTo.getDateStartOfDay(),
						deathReasonSelected,
						inputOrOutput
					);
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}

				try {
					model = new MortuaryBrowserModel(
						patientTextfield.getText().trim(),
						provenanceSelected,
						dateFrom.getDateStartOfDay(),
						dateTo.getDateStartOfDay(),
						deathReasonSelected,
						inputOrOutput
					);
				} catch (OHException e) {
					throw new RuntimeException(e);
				}

				totalMortuaryLabel.setText(MessageBundle.getMessage("angal.mortuary.totalmortuary.txt") + ": " + TOTAL_MORTUARIES);
				TOTAL_PAGES = (int) Math.ceil((double) TOTAL_MORTUARIES / PAGE_SIZE);
				underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
				CURRENT_PAGE = 0;

				isSearch = false;
				pagesCombo.removeAllItems();
				for (int i = 0; i < TOTAL_PAGES; i++) {
					pagesCombo.addItem(i + 1);
				}

				pagesCombo.setSelectedItem(1);

				if (mortuaries != null) {
					model.fireTableDataChanged();
					movTable.updateUI();
				}

				updateTotals();

				nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
				prevButton.setEnabled(CURRENT_PAGE > 0);

			});
		}
		return filterButton;
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
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
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
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
		if (searchButton == null) {
			searchButton = new JButton();
			searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchButton.addActionListener(actionEvent -> {
				String inputOrOutput = getInputOrOutput();
				try {
					TOTAL_MORTUARIES = mortuaryBrowserManager.countTotalMovements(
						searchTextfield.getText().trim(),
						null,
						null,
						null,
						null,
						inputOrOutput
					);
				} catch (OHServiceException e) {
					throw new RuntimeException(e);
				}

				totalMortuaryLabel.setText(MessageBundle.getMessage("angal.mortuary.totalmortuary.txt") + ": " + TOTAL_MORTUARIES);
				TOTAL_PAGES = (int) Math.ceil((double) TOTAL_MORTUARIES / PAGE_SIZE);
				underLabel.setText("/ " + TOTAL_PAGES + " " + MessageBundle.getMessage("angal.common.pages.txt"));
				CURRENT_PAGE = 0;

				isSearch = true;
				pagesCombo.removeAllItems();
				for (int i = 0; i < TOTAL_PAGES; i++) {
					pagesCombo.addItem(i + 1);
				}

				pagesCombo.setSelectedItem(1);

				updateTotals();

				nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
				prevButton.setEnabled(CURRENT_PAGE > 0);

			});
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
			nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
			nextButton.addActionListener(actionEvent -> {
				if (CURRENT_PAGE < TOTAL_PAGES - 1) {
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
						applyFilter(CURRENT_PAGE, isSearch);
					}
				}
			});
		}
		return pagesCombo;
	}

	private void applyFilter(int currentPage, boolean isSearch) {
		String provenanceSelected;
		String deathReasonSelected;
		String inputOrOutput = getInputOrOutput();

		if (!(provenanceCombo.getSelectedItem() instanceof String)) {
			provenanceSelected = (String) provenanceCombo.getSelectedItem();
		} else {
			provenanceSelected = (String) provenanceCombo.getSelectedItem();
			if (provenanceSelected.equals(TEXT_ALL)) {
				provenanceSelected = null;
			}
		}
		if (!(deathReasonCombo.getSelectedItem() instanceof String)) {
			deathReasonSelected = (String) deathReasonCombo.getSelectedItem();
		} else {
			deathReasonSelected = (String) deathReasonCombo.getSelectedItem();
			if (deathReasonSelected.equals(TEXT_ALL)) {
				deathReasonSelected = null;
			}
		}

		try {
			if (isSearch) {
				model = new MortuaryBrowserModel(
					searchTextfield.getText().trim(),
					null,
					null,
					null,
					null,
					inputOrOutput
				);
			} else {
				model = new MortuaryBrowserModel(
					patientTextfield.getText().trim(),
					provenanceSelected,
					dateFrom.getDateStartOfDay(),
					dateTo.getDateStartOfDay(),
					deathReasonSelected,
					inputOrOutput
				);
			}

		} catch (OHException e) {
			throw new RuntimeException(e);
		}

		if (mortuaries != null) {
			model.fireTableDataChanged();
			movTable.updateUI();
		}

		updateTotals();

		nextButton.setEnabled(currentPage < TOTAL_PAGES - 1 && TOTAL_PAGES != 1);
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
			totalMortuaryLabel = new JLabel(MessageBundle.getMessage("angal.mortuary.totalmortuary.txt") + ": " + TOTAL_MORTUARIES);
		}
		return totalMortuaryLabel;
	}

	private String getInputOrOutput() {
		String inputOrOutput;
		if (inputRadioButton.isSelected()) {
			inputOrOutput = "I";
		} else {
			inputOrOutput = "O";
		}
		return inputOrOutput;
	}

	public void updateTotals() {
		if (jTableTotal == null) {
			return;
		}
		totalQti = 0;
		totalAmount = new BigDecimal(0);

		jTableTotal.getModel().setValueAt(MessageBundle.getMessage("angal.common.notapplicable.txt"), 0, 4);
	}

	class MortuaryBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MortuaryBrowserModel() throws OHException {
			LocalDateTime now = TimeTools.getNow();
			LocalDateTime old = now.minusWeeks(1);
			model = new MortuaryBrowserModel(null, null, old, now, null, "I");
		}

		public MortuaryBrowserModel(
			String patientName,
			String provenance,
			LocalDateTime dateFrom,
			LocalDateTime dateTo,
			String deathReason,
			String inputOrOutput
		) throws OHException {
			mortuaries = mortuaryBrowserManager.getMortuariesWhereDataPageable(
				patientName,
				provenance,
				dateFrom,
				dateTo,
				deathReason,
				inputOrOutput,
				CURRENT_PAGE,
				PAGE_SIZE
			);
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
		 * @see org.isf.mortuary.service.MortuaryIoOperations
		 */
		@Override
		public Object getValueAt(int r, int c) {
			if (mortuaries == null) {
				return null;
			}

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
}
