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
package org.isf.patient.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.Timer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.BillBrowser;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.mortuary.gui.DeathBrowser;
import org.isf.mortuary.gui.DeathEdit;
import org.isf.patient.gui.PatientInsertExtended.PatientListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.VoLimitedTextField;

import org.springframework.data.domain.Page;

public class SelectPatient extends JDialog implements PatientListener {

	private EventListenerList selectionListener = new EventListenerList();

	public interface SelectionListener extends EventListener {

		void patientSelected(Patient patient);
	}

	public void addSelectionListener(SelectionListener l) {
		selectionListener.add(SelectionListener.class, l);
	}

	private void fireSelectedPatient(Patient patient) {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = selectionListener.getListeners(SelectionListener.class);
		for (EventListener listener : listeners) {
			((SelectionListener) listener).patientSelected(patient);
		}
	}

	private static final long serialVersionUID = 1L;

	private JPanel jPanelButtons;
	private JPanel jPanelTop;
	private JPanel jPanelCenter;
	private JTable jTablePatient;
	private JScrollPane jScrollPaneTablePatient;
	private JButton jButtonCancel;
	private JButton jButtonSelect;
	private JLabel jLabelSearch;
	private JTextField jTextFieldSearchPatient;
	private JPanel jPanelDataPatient;
	private Patient patient;
	private boolean femalesOnly = false;

	public Patient getPatient() {
		return patient;
	}

	private JButton buttonNew;
	private PatientSummary ps;
	private String[] patColumns = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.name.txt").toUpperCase() };
	private int[] patColumnsWidth = { 100, 250 };
	private boolean[] patColumnsResizable = { false, true };

	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	List<Patient> patSearch = new ArrayList<>();

	private int currentPage = 0;
	private long totalRecords = 0;
	private int totalPages = 0;
    private Timer searchTimer;
	private boolean updatingPaginationControls;

	private JButton jButtonPrevious;
	private JButton jButtonNext;

	private JComboBox<Integer> jComboPage;

	private JLabel jLabelPageInfo;
	private JLabel jLabelTotalRecords;

	private JPanel jPanelPagination;

	private Integer minAge;
	private Integer maxAge;

	public SelectPatient(JFrame owner, Patient pat) {
		super(owner, true);

		patient = pat;
		ps = new PatientSummary(patient);

		initializeDialog();

		loadPatientPage();
	}

	public SelectPatient(JDialog owner, Patient pat) {
		super(owner, true);

		patient = pat;
		ps = new PatientSummary(patient);

		initializeDialog();

		loadPatientPage();
	}

	public SelectPatient(JDialog owner, String search) {
		super(owner, true);

		ps = new PatientSummary(patient);

		initializeDialog();

		jTextFieldSearchPatient.setText(search);

		loadPatientPage();
	}

    public SelectPatient(JFrame owner, String search) {
        super(owner, true);

        ps = new PatientSummary(patient);

        initializeDialog();

        jTextFieldSearchPatient.setText(search);

        loadPatientPage();
    }

	public SelectPatient(
			JDialog owner,
			String searchText,
			boolean enableAddPatient,
			boolean femaleOnly) {

		super(owner, true);

		this.femalesOnly = femaleOnly;

		ps = new PatientSummary(patient);

		initializeDialog();

		getButtonNew().setVisible(enableAddPatient);

		if (searchText != null) {
			jTextFieldSearchPatient.setText(searchText);
		}

		loadPatientPage();
	}

	public SelectPatient(JFrame owner, boolean abbleAddPatient, boolean full) {
		super(owner, true);

		ps = new PatientSummary(patient);

		initializeDialog();

		getButtonNew().setVisible(abbleAddPatient);

		loadPatientPage();
	}

	public SelectPatient(JFrame owner, String searchText, boolean enableAddPatient, boolean femaleOnly) {
		super(owner, true);

		this.femalesOnly = femaleOnly;

		ps = new PatientSummary(patient);

		initializeDialog();

		getButtonNew().setVisible(enableAddPatient);

		if (searchText != null) {
			jTextFieldSearchPatient.setText(searchText);
		}

		loadPatientPage();
	}

	public SelectPatient(JDialog owner, boolean abbleAddPatient, boolean full) {
		super(owner, true);

		ps = new PatientSummary(patient);

		initializeDialog();

		getButtonNew().setVisible(abbleAddPatient);

		loadPatientPage();
	}

	public SelectPatient(JDialog owner, String keywords, boolean enablePatientAdd) {
		this(owner, keywords);
		getButtonNew().setVisible(enablePatientAdd);
	}

	private void initializeDialog() {

		initComponents();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				patSearch.clear();
				dispose();
			}
		});

		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelTop(), BorderLayout.NORTH);
		add(getJPanelCenter(), BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.add(getJPanelPagination());
		southPanel.add(getJPanelButtons());

		add(southPanel, BorderLayout.SOUTH);

		setTitle(MessageBundle.getMessage("angal.patient.patientselection.title"));
		pack();
	}

	private void loadPatientPage() {

		try {

			String keyword = null;

			if (jTextFieldSearchPatient != null) {

				String text = jTextFieldSearchPatient.getText();

				if (text != null && !text.trim().isEmpty()) {
					keyword = text.trim();
				}
			}

			org.springframework.data.domain.PageRequest pageable =
					org.springframework.data.domain.PageRequest.of(
							currentPage,
							GeneralData.PAGINATIONPAGESIZE,
							org.springframework.data.domain.Sort.by("name"));

			Page<Patient> page = patientBrowserManager.getPatientsByOneOfFieldsLikeWith(
					keyword,
					femalesOnly,
					minAge,
					maxAge,
					pageable);

			patSearch = new ArrayList<>(page.getContent());

			totalRecords = page.getTotalElements();

			totalPages = page.getTotalPages();

			if (totalPages > 0 && currentPage >= totalPages) {
				currentPage = totalPages - 1;
				loadPatientPage();
				return;
			}

			updatePaginationControls();

			((DefaultTableModel) jTablePatient.getModel()).fireTableDataChanged();

			handleAutoSelection();

		} catch (OHServiceException e) {

			OHServiceExceptionUtil.showMessages(e);

			patSearch.clear();

			totalRecords = 0;

			totalPages = 0;

			updatePaginationControls();

			((DefaultTableModel) jTablePatient.getModel()).fireTableDataChanged();
		}
	}

	private void handleAutoSelection() {

		if (patSearch.isEmpty()) {

			patient = null;

			updatePatientSummary();

			return;
		}

		if (patSearch.size() == 1) {

			patient = reloadSelectedPatient(patSearch.get(0).getCode());

			jTablePatient.setRowSelectionInterval(0, 0);

			updatePatientSummary();
		}
	}

	private JPanel getJPanelPagination() {

		if (jPanelPagination == null) {

			jPanelPagination = new JPanel(new FlowLayout());

			jPanelPagination.add(getJButtonPrevious());

			jPanelPagination.add(getJComboPage());

			jPanelPagination.add(getJButtonNext());

			jPanelPagination.add(getJLabelPageInfo());

			jPanelPagination.add(getJLabelTotalRecords());
		}

		return jPanelPagination;
	}

	private JButton getJButtonPrevious() {

		if (jButtonPrevious == null) {

			jButtonPrevious = new JButton("<");

			jButtonPrevious.addActionListener(e -> {

				if (currentPage > 0) {

					currentPage--;

					loadPatientPage();
				}
			});
		}

		return jButtonPrevious;
	}

	private JButton getJButtonNext() {

		if (jButtonNext == null) {

			jButtonNext = new JButton(">");

			jButtonNext.addActionListener(e -> {

				if (currentPage + 1 < totalPages) {

					currentPage++;

					loadPatientPage();
				}
			});
		}

		return jButtonNext;
	}

	private JComboBox<Integer> getJComboPage() {

		if (jComboPage == null) {

			jComboPage = new JComboBox<>();

			jComboPage.addActionListener(e -> {

				if (updatingPaginationControls) {
					return;
				}

				Integer selected = (Integer) jComboPage.getSelectedItem();

				if (selected == null) {
					return;
				}

				int page = selected - 1;

				if (page != currentPage) {

					currentPage = page;

					loadPatientPage();
				}
			});
		}

		return jComboPage;
	}

	private JLabel getJLabelPageInfo() {

		if (jLabelPageInfo == null) {
			jLabelPageInfo = new JLabel();
		}

		return jLabelPageInfo;
	}

	private JLabel getJLabelTotalRecords() {

		if (jLabelTotalRecords == null) {
			jLabelTotalRecords = new JLabel();
		}

		return jLabelTotalRecords;
	}

	private void updatePaginationControls() {

		jButtonPrevious.setEnabled(currentPage > 0);

		jButtonNext.setEnabled(currentPage + 1 < totalPages);

		updatingPaginationControls = true;

		try {

		jComboPage.removeAllItems();

		for (int i = 1; i <= totalPages; i++) {
			jComboPage.addItem(i);
		}

		if (totalPages > 0) {
			jComboPage.setSelectedItem(currentPage + 1);
		}
		} finally {
			updatingPaginationControls = false;
		}

		jLabelPageInfo.setText(
				MessageBundle.getMessage("angal.common.pages.txt")
						+ " "
						+ (totalPages == 0 ? 0 : currentPage + 1)
						+ " / "
						+ totalPages);

		jLabelTotalRecords.setText(
				MessageBundle.getMessage("angal.common.total.txt")
						+ ": "
						+ totalRecords);
	}

	private JPanel getJPanelDataPatient() {
		if (jPanelDataPatient == null) {
			jPanelDataPatient = ps.getPatientCompleteSummary();
			jPanelDataPatient.setAlignmentY(Component.TOP_ALIGNMENT);
		}
		return jPanelDataPatient;
	}

    private JTextField getJTextFieldSearchPatient() {

        if (jTextFieldSearchPatient == null) {

            jTextFieldSearchPatient = new VoLimitedTextField(100, 20);

            searchTimer = new Timer(200, e -> {
                currentPage = 0;
                loadPatientPage();
            });

            searchTimer.setRepeats(false);

            jTextFieldSearchPatient.getDocument().addDocumentListener(
                    new DocumentListener() {

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            restartSearch();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            restartSearch();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            restartSearch();
                        }

                        private void restartSearch() {
                            searchTimer.restart();
                        }
                    });
        }

        return jTextFieldSearchPatient;
    }

	private JLabel getJLabelSearch() {
		if (jLabelSearch == null) {
			jLabelSearch = new JLabel(MessageBundle.getMessage("angal.patient.searchpatient"));
		}
		return jLabelSearch;
	}

	private JButton getJButtonSelect() {
		if (jButtonSelect == null) {
			jButtonSelect = new JButton(MessageBundle.getMessage("angal.common.select.btn"));
			jButtonSelect.setMnemonic(MessageBundle.getMnemonic("angal.common.select.btn.key"));
			jButtonSelect.addActionListener(actionEvent -> {

				if (patient != null) {
					// to free memory
					patSearch.clear();
					dispose();
					fireSelectedPatient(patient);
				}
			});
		}
		return jButtonSelect;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> {
				// to free memory
				patSearch.clear();
				dispose();
			});
		}
		return jButtonCancel;
	}

	private JScrollPane getJScrollPaneTablePatient() {
		if (jScrollPaneTablePatient == null) {
			jScrollPaneTablePatient = new JScrollPane();
			jScrollPaneTablePatient.setViewportView(getJTablePatient());
			jScrollPaneTablePatient.setAlignmentY(Component.TOP_ALIGNMENT);
		}
		return jScrollPaneTablePatient;
	}

	private JTable getJTablePatient() {
		if (jTablePatient == null) {
			jTablePatient = new JTable();
			jTablePatient.setModel(new SelectPatientModel());
			jTablePatient.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			for (int i = 0; i < patColumns.length; i++) {
				jTablePatient.getColumnModel().getColumn(i).setMinWidth(patColumnsWidth[i]);
				if (!patColumnsResizable[i]) {
					jTablePatient.getColumnModel().getColumn(i).setMaxWidth(patColumnsWidth[i]);
				}
			}
			jTablePatient.setAutoCreateColumnsFromModel(false);
			jTablePatient.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());

			ListSelectionModel listSelectionModel = jTablePatient.getSelectionModel();
			listSelectionModel.addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					int index = jTablePatient.getSelectedRow();
					if (index < 0 || index >= patSearch.size()) {
						return;
					}
					Patient selectedPatient = patSearch.get(index);
					patient = reloadSelectedPatient(selectedPatient.getCode());
					updatePatientSummary();
				}
			});

			jTablePatient.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						e.consume();
						jButtonSelect.doClick();
					}
				}
			});
		}
		return jTablePatient;
	}

	private Patient reloadSelectedPatient(Integer code) {
		try {
			return patientBrowserManager.getPatientById(code);
		} catch (OHServiceException ex) {
			throw new RuntimeException(MessageBundle.getMessage("angal.patient.unable.to.load.patient"));
		}
	}

	private void updatePatientSummary() {
		jPanelCenter.remove(jPanelDataPatient);
		ps = new PatientSummary(patient);
		jPanelDataPatient = ps.getPatientCompleteSummary();
		jPanelDataPatient.setAlignmentY(Component.TOP_ALIGNMENT);

		jPanelCenter.add(jPanelDataPatient);
		jPanelCenter.validate();
		jPanelCenter.repaint();
	}

	private JPanel getJPanelCenter() {
		if (jPanelCenter == null) {
			jPanelCenter = new JPanel();
			jPanelCenter.setLayout(new BoxLayout(jPanelCenter, BoxLayout.X_AXIS));
			jPanelCenter.add(getJScrollPaneTablePatient());
			jPanelCenter.add(getJPanelDataPatient());

			if (patient != null) {
				for (int i = 0; i < patSearch.size(); i++) {
					if (patSearch.get(i).getCode().equals(patient.getCode())) {
						jTablePatient.addRowSelectionInterval(i, i);
						int j = 0;
						if (i > 10) {
							j = i - 10;
						}
						jTablePatient.scrollRectToVisible(jTablePatient.getCellRect(j, i, true));
						break;
					}
				}
			}
		}
		return jPanelCenter;
	}

	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			jPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelTop.add(getJLabelSearch());
			jPanelTop.add(getJTextFieldSearchPatient());
			if (MainMenu.checkUserGrants("btnadmnew")) {
				jPanelTop.add(getButtonNew());
			}
		}
		return jPanelTop;
	}

	private JButton getButtonNew() {
		if (buttonNew == null) {
			buttonNew = new JButton(MessageBundle.getMessage("angal.common.newpatient.btn"));
			buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.newpatient.btn.key"));
			buttonNew.addActionListener(actionEvent -> {

				if (GeneralData.PATIENTEXTENDED) {
					PatientInsertExtended newrecord = new PatientInsertExtended(this, new Patient(), true);
					newrecord.addPatientListener(this);
					newrecord.setVisible(true);
				} else {
					PatientInsert newrecord = new PatientInsert(this, new Patient(), true);
					newrecord.addPatientListener((PatientInsert.PatientListener) this);
					newrecord.setVisible(true);
				}

			});
		}
		return buttonNew;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonSelect());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	class SelectPatientModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public SelectPatientModel() {
		}

		@Override
		public int getRowCount() {
			if (patSearch == null) {
				return 0;
			}
			return patSearch.size();
		}

		@Override
		public String getColumnName(int c) {
			return patColumns[c];
		}

		@Override
		public int getColumnCount() {
			return patColumns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Patient patient = patSearch.get(r);
			if (c == -1) {
				return patient;
			} else if (c == 0) {
				return patient.getCode();
			} else if (c == 1) {
				return patient.getName();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			return cell;
		}
	}

	public void setButtonNew(JButton buttonNew) {
		this.buttonNew = buttonNew;
	}

	List<BillBrowser> billBrowserListeners = new ArrayList<>();

	public void addSelectionListener(BillBrowser l) {
		billBrowserListeners.add(l);
	}

	List<DeathBrowser> deathBrowsersListeners = new ArrayList<>();

	public void addSelectionListener(DeathBrowser l) {
		deathBrowsersListeners.add(l);
	}

	List<DeathEdit> mortuaryEditsListeners = new ArrayList<>();

	public void addSelectionListener(DeathEdit l) {
		mortuaryEditsListeners.add(l);
	}

	@Override
	public void patientUpdated(AWTEvent e) {
	}

	@Override
	public void patientInserted(AWTEvent e) {
		currentPage = 0;
		loadPatientPage();
	}

	public SelectPatient(JDialog owner, String searchText, boolean enableAddPatient, Integer minAge, Integer maxAge) {
		super(owner, true);
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.femalesOnly = false;
		ps = new PatientSummary(patient);
		initializeDialog();
		getButtonNew().setVisible(enableAddPatient);
		if (searchText != null) {
			jTextFieldSearchPatient.setText(searchText);
		}
		loadPatientPage();
	}

	public SelectPatient(JFrame owner, String searchText, boolean enableAddPatient, Integer minAge, Integer maxAge) {
		super(owner, true);
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.femalesOnly = false;
		ps = new PatientSummary(patient);
		initializeDialog();
		getButtonNew().setVisible(enableAddPatient);
		if (searchText != null) {
			jTextFieldSearchPatient.setText(searchText);
		}
		loadPatientPage();
	}

}