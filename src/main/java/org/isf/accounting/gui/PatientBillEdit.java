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
package org.isf.accounting.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.admission.gui.AdmittedPatientBrowser;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.TxtPrinter;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.Price;
import org.isf.priceslist.model.PriceList;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.reductionplan.manager.ReductionPlanManager;
import org.isf.reductionplan.model.ReductionPlan;
import org.isf.stat.gui.report.GenericReportBill;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.GoodDateTimeToggleChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import com.github.lgooddatepicker.zinternaltools.TimeChangeEvent;

/**
 * Create a single Patient Bill which affects tables BILLS, BILLITEMS and BILLPAYMENTS
 *
 * @author Mwithi
 */
public class PatientBillEdit extends JDialog implements SelectionListener, SelectPrescriptions.PrescriptionSelectionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientBillEdit.class);

	// LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList patientBillListener = new EventListenerList();

	public interface PatientBillListener extends EventListener {

		void billInserted(AWTEvent aEvent);
	}

	public void addPatientBillListener(PatientBillListener l) {
		patientBillListener.add(PatientBillListener.class, l);
	}

	private void fireBillInserted(Bill aBill) {
		AWTEvent event = new AWTEvent(aBill, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = patientBillListener.getListeners(PatientBillListener.class);
		for (EventListener listener : listeners) {
			((PatientBillListener) listener).billInserted(event);
		}
	}
	// ---------------------------------------------------------------------------

	@Override
	public void patientSelected(Patient patient) {
		setPatientSelected(patient);
		List<Bill> patientPendingBills = new ArrayList<>();
		Admission patientAdmission = admissionBrowserManager.getCurrentAdmission(patient);

		try {
			patientPendingBills = billBrowserManager.getPendingBills(patient.getCode());
		} catch (OHServiceException ohServiceException) {
			LOGGER.error(ohServiceException.getMessage(), ohServiceException);
		}
		if (patientPendingBills.isEmpty()) {
			// BILL
			thisBill.setBillPatient(patient);
			thisBill.setIsPatient(true);
			thisBill.setPatName(patient.getName());
			thisBill.setAdmission(patientAdmission);
			modified = true;
		} else {
			if (patientPendingBills.size() == 1) {
				if (GeneralData.ALLOWMULTIPLEOPENEDBILL) {
					int response = MessageDialog.yesNo(this, "angal.newbill.thispatienthasapendingbilldoyouwanttocreateanother.msg");
					if (response == JOptionPane.YES_OPTION) {
						insert = true;
						thisBill.setBillPatient(patient);
						thisBill.setIsPatient(true);
						thisBill.setPatName(patient.getName());
						thisBill.setAdmission(patientAdmission);
						modified = true;
					} else {
						initData(patientPendingBills.get(0), false);

						/* ****** Check if it is same month ************** */
						// checkIfSameMonth();
						/* *********************************************** */
					}
				} else {
					MessageDialog.error(null, "angal.newbill.thispatienthasapendingbill.msg");
					initData(patientPendingBills.get(0), false);

					/* ****** Check if it is same month ************** */
					// checkIfSameMonth();
					/* *********************************************** */
				}
			} else {
				if (GeneralData.ALLOWMULTIPLEOPENEDBILL) {
					int response = MessageDialog.yesNo(this, "angal.newbill.thispatienthasmorethanonependingbilldoyouwanttocreateanother.msg");
					if (response == JOptionPane.YES_OPTION) {
						insert = true;
						// thisBill.setPatID(patientSelected.getCode());
						thisBill.setBillPatient(patient);
						thisBill.setIsPatient(true);
						thisBill.setPatName(patient.getName());
						thisBill.setAdmission(patientAdmission);
						modified = true;
					} else if (response == JOptionPane.NO_OPTION) {
						// something must be proposed
						int resp = MessageDialog.yesNo(this,
										"angal.newbill.thispatienthasmorethanonependingbilldoyouwanttoopenthelastpendingbill.msg");
						if (resp == JOptionPane.YES_OPTION) {
							initData(patientPendingBills.get(0), false);
							/* ****** Check if it is same month ************** */
							// checkIfSameMonth();
							/* *********************************************** */
						} else {
							dispose();
						}
					} else {
						return;
					}
				} else {
					MessageDialog.error(null, "angal.newbill.multipleopenedbillsnotallowedpleasesolve.msg");
					dispose();
				}
			}
		}
		updateGUI();
		checkBill();
	}

	private static final long serialVersionUID = 1L;
	private JTable jTableBill;
	private JScrollPane jScrollPaneBill;
	private JButton jButtonAddMedical;
	private JButton jButtonAddOperation;
	private JButton jButtonAddExam;
	private JButton jButtonAddOther;
	private JButton jButtonAddPrescription;
	private JButton jButtonAddPayment;
	private JPanel jPanelButtons;
	private JPanel jPanelDate;
	private JPanel jPanelPatient;
	private JTable jTablePayment;
	private JScrollPane jScrollPanePayment;
	private JTextField jTextFieldPatient;
	private JComboBox<PriceList> jComboBoxPriceList;
	private JPanel jPanelData;
	private JTable jTableTotal;
	private JScrollPane jScrollPaneTotal;
	private JTable jTableBigTotal;
	private JScrollPane jScrollPaneBigTotal;
	private JTable jTableBalance;
	private JScrollPane jScrollPaneBalance;
	private JPanel jPanelTop;
	private JPanel jPanelNorthContainer;
	private JPanel jPanelItemSearch;
	private JTextField jTextFieldItemSearch;
	private JList<Price> jListItemSearchResults;
	private JScrollPane jScrollPaneItemSearchResults;
	private final DefaultListModel<Price> itemSearchResultsModel = new DefaultListModel<>();
	private GoodDateTimeToggleChooser jCalendarDate;
	private JLabel jLabelDate;
	private JLabel jLabelUser;
	private JLabel jLabelPatient;
	private JButton jButtonRemoveItem;
	private JLabel jLabelPriceList;
	private JComboBox<Ward> jComboBoxWard;
	private JLabel jLabelWard;
	private JPanel jPanelWardAndList;
	private boolean wardManuallySelected;
	private boolean applyingWardDefault;
	private JButton jButtonRemovePayment;
	private JButton jButtonAddRefund;
	private JPanel jPanelButtonsPayment;
	private JPanel jPanelButtonsBill;
	private JPanel jPanelButtonsActions;
	private JButton jButtonClose;
	private JButton jButtonPaid;
	private JButton jButtonPrintPayment;
	private JButton jButtonSave;
	private JButton jButtonBalance;
	private JButton jButtonCustom;
	private JButton jButtonPickPatient;
	private JButton jButtonTrashPatient;
	private JDialog jItemSearchWindow;
	private JTextField jDialogSearchField;
	private JTextField jDialogItemDescription;
	private JTextField jDialogItemQuantity;
	private JTextField jDialogItemPrice;
	private BillItems editingBillItem;
	private int editingBillItemRow = -1;

	private static final int PANEL_WIDTH = 450;
	private static final int BUTTON_WIDTH = 190;
	private static final int BUTTON_WIDTH_BILL = 190;
	private static final int BUTTON_WIDTH_PAYMENT = 190;
	private static final int PRICE_WIDTH = 190;
	private static final int CURRENCY_CODE_WIDTH = 40;
	private static final int QUANTITY_WIDTH = 40;
	private static final int BILL_HEIGHT = 200;
	private static final int TOTAL_HEIGHT = 20;
	private static final int BIG_TOTAL_HEIGHT = 20;
	private static final int PAYMENT_HEIGHT = 150;
	private static final int BALANCE_HEIGHT = 20;
	private static final int BUTTON_HEIGHT = 25;
	private static final Dimension PATIENT_DIMENSION = new Dimension(300, 20);
	private static final Dimension LABELS_DIMENSION = new Dimension(60, 20);
	private static final Dimension USER_DIMENSION = new Dimension(220, 20);
	private static final Dimension WARD_DIMENSION = new Dimension(195, 20);
	private static final Dimension TOTAL_TABLE_SIZE = new Dimension(PANEL_WIDTH, TOTAL_HEIGHT);
	private static final Dimension BIGTOTAL_TABLE_SIZE = new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT);
	private static final Dimension BALANCE_TABLE_SIZE = new Dimension(PANEL_WIDTH, BALANCE_HEIGHT);
	private static final Dimension BUTTON_ITEM_SIZE = new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT);
	private static final Dimension BUTTON_PAYMENT_SIZE = new Dimension(BUTTON_WIDTH_PAYMENT, BUTTON_HEIGHT);
	private static final Dimension BUTTON_ACTION_SIZE = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);

	private BigDecimal total = new BigDecimal(0);
	private BigDecimal bigTotal = new BigDecimal(0);
	private BigDecimal balance = new BigDecimal(0);
	private boolean insert;
	private boolean modified;
	private boolean keepDate = true;
	private boolean paid;
	private LocalDateTime today = TimeTools.getNow();

	/**
	 * thisObject
	 */
	private Bill thisBill;

	// Tables
	private Object[] billClasses = { Price.class, Integer.class, Double.class };
	private String[] billColumnNames = {
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.qty.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.amount.txt").toUpperCase()
	};
	private Object[] paymentClasses = { Date.class, Double.class };

	// Hospital info
	private HospitalBrowsingManager hospitalManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
	private String currencyCod;

	// Prices and Lists (ALL)
	private PriceListManager priceListManager = Context.getApplicationContext().getBean(PriceListManager.class);
	private List<Price> prcArray;
	private List<PriceList> lstArray;

	// PricesOthers (ALL)
	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
	private List<PricesOthers> othPrices;

	// Items and Payments (ALL)
	private BillBrowserManager billBrowserManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);
	private final Map<String, String> cashierDisplayNameCache = new HashMap<>();
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);
	private int reductionPlanId;

	// Prices, Items and Payments for the tables
	private List<BillItems> billItems = new ArrayList<>();
	private List<BillPayments> payItems = new ArrayList<>();
	private List<Price> prcListArray = new ArrayList<>();
	private Map<String, Price> priceHashTable;
	private int billItemsSaved;
	private int payItemsSaved;

	private String user = UserBrowsingManager.getCurrentUser();

	/**
	 * new bill from {@link MainMenu}
	 */
	public PatientBillEdit() {
		thisBill = new Bill();
		PatientBillEdit pbe = new PatientBillEdit(null, thisBill, true);
		pbe.setVisible(true);
	}

	/**
	 * new bill from {@link AdmittedPatientBrowser}
	 */
	public PatientBillEdit(JFrame owner, Patient patient) {
		super(owner, true);
		thisBill = new Bill();
		loadDataset();
		initData(thisBill, true);
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		// Workaround to run patientSelected method after the GUI is completed and showing
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				patientSelected(patient);
			}
		});
	}

	/**
	 * new or edit bill from {@link BillBrowser}
	 */
	public PatientBillEdit(JFrame owner, Bill bill, boolean inserting) {
		super(owner, true);
		loadDataset();
		initData(bill, inserting);
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);

		// Workaround to run checkBill method after the GUI is completed and showing
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				if (!insert) {
					checkBill();
				}
			}
		});
	}

	private void loadDataset() {
		try {
			this.prcArray = priceListManager.getPrices();
			this.lstArray = priceListManager.getLists();
			this.othPrices = pricesOthersManager.getOthers();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}

	private void initData(Bill bill, boolean inserting) {
		insert = inserting;
		if (insert) {
			thisBill = new Bill();
			LocalDateTime date = RememberDates.getLastBillDate();
			if (date != null) {
				thisBill.setDate(date);
			}
			thisBill.setPriceList(lstArray.get(0));
		} else {
			try {
				thisBill = (Bill) bill.clone();
				if (insert) {
					LocalDateTime date = RememberDates.getLastBillDate();
					if (date != null) {
						thisBill.setDate(date);
					}
				} else {
					thisBill.setDate(bill.getDate());
					thisBill.setAdmission(bill.getAdmission());
					thisBill.setBillPatient(bill.getBillPatient());
					thisBill.setPriceList(bill.getPriceList());

					try {
						billItems = billBrowserManager.getItems(thisBill.getId());
						payItems = billBrowserManager.getPayments(thisBill.getId());
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e, this);
					}
				}
				billItemsSaved = billItems.size();
				payItemsSaved = payItems.size();

			} catch (CloneNotSupportedException cnse) {
				LOGGER.debug("CloneNotSupportedException", cnse);
			}
		}
		setPriceListArray();
		setCurrencyCode();
		updateTotals();
	}

	private void setCurrencyCode() {
		try {
			if (thisBill != null && thisBill.isList() && thisBill.getPriceList() != null && thisBill.getPriceList().getCurrency() != null
							&& !thisBill.getPriceList().getCurrency().equals("")) {
				// if bill is defined (editing), then currency is the one of its pricelist
				this.currencyCod = thisBill.getPriceList().getCurrency();

			} else if (!lstArray.get(0).getCurrency().equals("")) {
				// if bill is not defined (inserting), then currency is the one of the first pricelist (default)
				this.currencyCod = lstArray.get(0).getCurrency();

			} else {
				// fallback to hospital currency if not defined for pricelist
				this.currencyCod = hospitalManager.getHospitalCurrencyCod();
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}

	private void updateGUI() {
		setJButtonTrashPatient();
		applyWardDefaultIfNeeded();
		setJTextFieldPatient();
		setJButtonPickPatient();
		setJButtonPrintPayment();
		setJTableBigTotal();
		setJTableBill();
		setJTableTotal();
		setJTablePayment();
		setJTableBalance();
	}

	private void updateTitle() {
		if (insert) {
			setTitle(MessageBundle.getMessage("angal.patientbill.newpatientbill.title"));
		} else {
			setTitle(MessageBundle.formatMessage("angal.patientbill.editpatientbill.fmt.title", thisBill.getId()));
		}
	}

	private void initComponents() {
		add(getJPanelNorthContainer(), BorderLayout.NORTH);
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.EAST);
		updateTitle();
		pack();
	}

	/*
	 * check if PriceList, prices and Patient still exist
	 */
	private void checkBill() {
		if (GeneralData.ENABLEREDUCTIONPLAN && thisBill.isPatient() && thisBill.getBillPatient() != null) {
			ReductionPlan patientReductionPlan = thisBill.getBillPatient().getReductionPlan();
			reductionPlanId = patientReductionPlan != null ? patientReductionPlan.getId() : 0;
		}
		if (thisBill.isList()) {
			Optional<PriceList> priceList = lstArray.stream().filter(pl -> pl.getId() == thisBill.getPriceList().getId()).findFirst();

			if (!priceList.isPresent()) { // PriceList not found
				Icon icon = new ImageIcon("rsc/icons/list_dialog.png"); //$NON-NLS-1$
				PriceList list = (PriceList) JOptionPane.showInputDialog(this,
								MessageBundle.getMessage("angal.newbill.thepricelistassociatedwiththisbillnolongerexists.msg"),
								MessageBundle.getMessage("angal.newbill.selectapricelist.title"), JOptionPane.OK_OPTION, icon, lstArray.toArray(), "");
				if (list == null) {
					MessageDialog.warning(this, "angal.newbill.nopricelistselectedwillbeused.fmt.msg", lstArray.get(0).getName());
					list = lstArray.get(0);
				}
				thisBill.setPriceList(list);
				thisBill.setListName(list.getName());
				modified = true;
			} else {
				PriceList priceListFound = priceList.get();
				if (!priceListFound.getCurrency().equals("") && !priceListFound.getCurrency().equals(this.currencyCod)) {
					// the currency has changed since last read (editing)
					MessageDialog.info(this,
									MessageBundle.formatMessage("angal.newbill.thepricelistcurrencycodehaschangedarrow.fmt.msg",
													priceListFound.getCurrency(), this.currencyCod));
					setCurrencyCodeFromList(priceListFound);
				}
				// NOTE: there is no way to spot currency changes after last save because the currency is not saved along with the bill but with the pricelist.
				// So, when opening the bill (editing), we don't know the currency, which is taken from the linked pricelist (whatever it is) and automatically
				// applied, without alert. We don't have (yet) a versioning of price lists, nor a currency field for an invoice.
			}
		}

		List<String> notFoundPriceList = new ArrayList<>();
		List<String> changedPriceList = new ArrayList<>();
		for (BillItems item : billItems) {
			if (item.isPrice()) {
				Price p = getPrice(item.getPriceID());
				if (p == null) {
					notFoundPriceList.add(item.getItemDescription());
					item.setPriceID(""); // Update items straightway, no option for the user
					item.setPrice(false);
					modified = true;
				} else {
					try {
						p = ReductionPlanBillSupport.applyReduction(p, reductionPlanManager, reductionPlanId);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e, this);
					}
					if (BillItemPriceSupport.requiresReconciliation(item, p)) {
						changedPriceList.add(item.getItemDescription());
					}
				}
			}
		}

		if (!notFoundPriceList.isEmpty() && !changedPriceList.isEmpty()) {
			int ok = MessageDialog.yesNo(this, createMessage(notFoundPriceList, changedPriceList));
			if (ok == JOptionPane.OK_OPTION) {
				updatePrices();
			}
		} else if (!notFoundPriceList.isEmpty()) {
			MessageDialog.warning(this,
							MessageBundle.formatMessage("angal.newbill.somepricesnotfound.fmt.msg", String.join(", ", notFoundPriceList)));
		} else if (!changedPriceList.isEmpty()) {
			int ok = MessageDialog.yesNo(this, "angal.newbill.somepriceshavebeenchangeddoyouwanttoupdatetheitemsprices.fmt.msg", String.join(", ", changedPriceList));
			if (ok == JOptionPane.OK_OPTION) {
				updatePrices();
			}
		}

		if (thisBill.isPatient()) {

			Patient patient = null;
			try {
				patient = patientBrowserManager.getPatientById(thisBill.getBillPatient().getCode());
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
			if (patient != null) {
				setPatientSelected(patient);
				Admission currentAdmission = admissionBrowserManager.getCurrentAdmission(patient);

				Icon icon = UIManager.getIcon("OptionPane.warningIcon");
				if (thisBill.getAdmission() == null && currentAdmission != null) {
					int ok = MessageDialog.yesNo(this, icon,
									"angal.newbill.thispatientisadmittednowdoyouwanttolinkthisbilltothecurrentadmission.msg");
					if (ok == JOptionPane.OK_OPTION) {
						thisBill.setAdmission(currentAdmission);
						modified = true;
					}
				}
				if (thisBill.getAdmission() != null && currentAdmission == null) {
					int ok = MessageDialog.yesNo(this, icon,
									"angal.newbill.thispatientisnolongeradmitteddoyouwanttounlinkthisbillfromthepreviousadmission.msg");
					if (ok == JOptionPane.OK_OPTION) {
						thisBill.setAdmission(currentAdmission);
						modified = true;
					}
				}
				if (thisBill.getAdmission() != null && currentAdmission != null && thisBill.getAdmission().getId() != currentAdmission.getId()) {
					int ok = MessageDialog.yesNo(this, icon,
									"angal.newbill.thisbillwaslinkedtoapreviousadmissiondoyouwanttolinkittothecurrentadmissioninstead.msg");
					if (ok == JOptionPane.OK_OPTION) {
						thisBill.setAdmission(currentAdmission);
						modified = true;
					}
				}
				if (thisBill.getAdmission() != null && currentAdmission != null && !thisBill.getAdmission().getWard().equals(currentAdmission.getWard())) {
					MessageDialog.info(this,
									MessageBundle.formatMessage("angal.newbill.thepatienthaschangedwardsarrow.fmt.msg",
													thisBill.getAdmission().getWard(), currentAdmission.getWard()));
				}

			} else { // Patient not found
				MessageDialog.warning(this, "angal.newbill.patientassociatedwiththisbillnolongerexists.msg");
				thisBill.setIsPatient(false);
				thisBill.getBillPatient().setCode(0);
				thisBill.setAdmission(null);
				modified = true;
			}
		}
		updateGUI();
	}

	private Price getPrice(String priceID) {
		return priceHashTable.get(thisBill.getPriceList().getId() + priceID);
	}

	private void updatePrices() {
		for (BillItems item : billItems.stream().filter(BillItems::isPrice).collect(Collectors.toList())) {
			Price p = getPrice(item.getPriceID());
			if (p == null) {
				continue;
			}
			try {
				p = ReductionPlanBillSupport.applyReduction(p, reductionPlanManager, reductionPlanId);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
				continue;
			}
			if (BillItemPriceSupport.requiresReconciliation(item, p)) {
				if (BillItemPriceSupport.descriptionChanged(item, p)) {
					item.setItemDescription(p.getDesc());
				}
				if (BillItemPriceSupport.priceChanged(item, p)) {
					item.setItemAmount(p.getPrice());
				}
				modified = true;
			}
		}
		updateTotals();
	}

	private String createMessage(List<String> notFoundPriceList, List<String> changedPriceList) {
		return MessageBundle.formatMessage("angal.newbill.somepricesnotfoundandsomeotherchangeddoyouwanttoupdatetheitemsprices.fmt.msg",
						String.join(", ", notFoundPriceList), String.join(", ", changedPriceList));
	}
	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel();
			jPanelData.setLayout(new BoxLayout(jPanelData, BoxLayout.Y_AXIS));
			jPanelData.add(getJScrollPaneTotal());
			jPanelData.add(getJScrollPaneBill());
			jPanelData.add(getJScrollPaneBigTotal());
			jPanelData.add(getJScrollPanePayment());
			jPanelData.add(getJScrollPaneBalance());
		}
		return jPanelData;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {
			jPanelPatient = new JPanel();
			jPanelPatient.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelPatient.add(getJLabelPatient());
			jPanelPatient.add(getJTextFieldPatient());
		}
		return jPanelPatient;
	}

	private JLabel getJLabelPatient() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel(MessageBundle.getMessage("angal.common.patient.txt"));
			jLabelPatient.setPreferredSize(LABELS_DIMENSION);
		}
		return jLabelPatient;
	}

	private JTextField getJTextFieldPatient() {
		if (jTextFieldPatient == null) {
			jTextFieldPatient = new JTextField();
			jTextFieldPatient.setText(""); //$NON-NLS-1$
			jTextFieldPatient.setPreferredSize(PATIENT_DIMENSION);
			jTextFieldPatient.addActionListener(actionEvent -> openPatientSearchWindow());
		}
		setJTextFieldPatient();
		return jTextFieldPatient;
	}

	private void setJTextFieldPatient() {
		if (thisBill.isPatient()) {
			jTextFieldPatient.setText(thisBill.getPatName());
		}
	}

	private JLabel getJLabelPriceList() {
		if (jLabelPriceList == null) {
			jLabelPriceList = new JLabel(MessageBundle.getMessage("angal.newbill.list.txt"));
		}
		return jLabelPriceList;
	}

	private JLabel getJLabelWard() {
		if (jLabelWard == null) {
			jLabelWard = new JLabel(MessageBundle.getMessage("angal.common.ward.txt"));
		}
		return jLabelWard;
	}

	/**
	 * Row above {@link #getJPanelPatient()}: price list and ward selectors, grouped together since
	 * both affect what's billable (the ward determines available medical stock; the price list
	 * determines item prices).
	 */
	private JPanel getJPanelWardAndList() {
		if (jPanelWardAndList == null) {
			jPanelWardAndList = new JPanel();
			jPanelWardAndList.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelWardAndList.add(getJLabelWard());
			jPanelWardAndList.add(getJComboBoxWard());
			jPanelWardAndList.add(getJLabelPriceList());
			jPanelWardAndList.add(getJComboBoxPriceList());
		}
		return jPanelWardAndList;
	}

	private JComboBox<PriceList> getJComboBoxPriceList() {
		if (jComboBoxPriceList == null) {
			jComboBoxPriceList = new JComboBox<>();
			PriceList list = null;
			for (PriceList lst : lstArray) {

				jComboBoxPriceList.addItem(lst);

				if (!insert) {
					if (lst.getId() == thisBill.getPriceList().getId()) {
						list = lst;
					}
				}
			}
			if (list != null) {
				jComboBoxPriceList.setSelectedItem(list);
				setCurrencyCodeFromList(list);
			}
			jComboBoxPriceList.addActionListener(actionEvent -> {

				PriceList selectedPricelist = (PriceList) jComboBoxPriceList.getSelectedItem();
				thisBill.setPriceList(selectedPricelist);
				thisBill.setIsList(true);
				setCurrencyCodeFromList(selectedPricelist);
				setPriceListArray();
				checkBill();
				updateGUI();
			});
		}
		return jComboBoxPriceList;
	}

	private void setPriceListArray() {
		this.prcListArray = this.prcArray.stream().filter(price -> price.getList().getId() == thisBill.getPriceList().getId()).collect(Collectors.toList());

		/*
		 * Create a hashTable with the selected prices.
		 */
		priceHashTable = prcListArray.stream().collect(
						Collectors.toMap(price -> price.getList().getId() + price.getGroup() + price.getItem(), price -> price, (a, b) -> b, HashMap::new));
	}

	private void setCurrencyCodeFromList(PriceList list) {
		String currency = list.getCurrency();
		if (!currency.isBlank()) {
			this.currencyCod = currency;
		}
	}

	private JComboBox<Ward> getJComboBoxWard() {
		if (jComboBoxWard == null) {
			jComboBoxWard = new JComboBox<>();
			jComboBoxWard.setPreferredSize(WARD_DIMENSION); // TODO: improve Layouts avoiding fixed dimensions
			try {
				for (Ward ward : wardBrowserManager.getWards()) {
					jComboBoxWard.addItem(ward);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}
			if (!insert) {
				jComboBoxWard.setSelectedItem(thisBill.getWard());
			} else {
				applyWardDefaultIfNeeded();
			}
			jComboBoxWard.addItemListener(itemEvent -> {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED && !applyingWardDefault) {
					wardManuallySelected = true;
				}
			});
		}
		return jComboBoxWard;
	}

	/**
	 * Applies the default-ward precedence for new bills only (existing bills always keep their own
	 * stored ward, selected once in {@link #getJComboBoxWard()}): {@code GeneralData.DEFAULTWARDINNEWBILL}
	 * first, falling back to the patient's current admission ward, otherwise no selection. A no-op
	 * once the user has manually picked a ward, so it's safe to call again (e.g. from
	 * {@link #updateGUI()}) after the admission becomes known post-construction.
	 */
	private void applyWardDefaultIfNeeded() {
		if (!insert || wardManuallySelected || jComboBoxWard == null) {
			return;
		}
		applyingWardDefault = true;
		jComboBoxWard.setSelectedItem(resolveDefaultWard());
		applyingWardDefault = false;
	}

	private Ward resolveDefaultWard() {
		Ward configuredWard = null;
		if (GeneralData.DEFAULTWARDINNEWBILL != null && !GeneralData.DEFAULTWARDINNEWBILL.isEmpty()) {
			try {
				configuredWard = wardBrowserManager.findWard(GeneralData.DEFAULTWARDINNEWBILL);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}
		}
		Admission admission = thisBill.getAdmission();
		Ward admissionWard = admission != null ? admission.getWard() : null;
		return BillingWardSupport.pickDefaultWard(configuredWard, admissionWard);
	}

	private GoodDateTimeToggleChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			jCalendarDate = new GoodDateTimeToggleChooser(thisBill.getDate(), false);
			jCalendarDate.addDateTimeChangeListener(event -> {
				DateChangeEvent dateChangeEvent = event.getDateChangeEvent();
				TimeChangeEvent timeChangeEvent = event.getTimeChangeEvent();
				if (dateChangeEvent != null) {
					// if the time is blank set it to the current time; otherwise leave it alone
					TimePicker timePicker = event.getTimePicker();
					if (timePicker.getTime() == null) {
						timePicker.setTime(LocalTime.now());
					}
				}
				if (!insert) {
					LocalDateTime date = thisBill.getDate();
					boolean isUnchanged = true;
					if (dateChangeEvent != null) {
						isUnchanged = date.toLocalDate().isEqual(dateChangeEvent.getNewDate());
					}
					LocalTime billTime = LocalTime.of(date.getHour(), date.getMinute());
					if (timeChangeEvent != null) {
						isUnchanged = timeChangeEvent.getNewTime().equals(billTime);
					}
					if (keepDate && !isUnchanged) {
						int ok = MessageDialog.yesNo(this, "angal.newbill.doyouwanttochangetheoriginaldate.msg");
						if (ok == JOptionPane.YES_OPTION) {
							keepDate = false;
							modified = true;
							thisBill.setDate(jCalendarDate.getLocalDateTime());
						} else {
							Runnable resetDateTime = () -> jCalendarDate.setDateTime(date);
							SwingUtilities.invokeLater(resetDateTime);
						}
					}
				} else {
					// new bill: no "original date" to protect, so the picker's value applies directly
					thisBill.setDate(jCalendarDate.getLocalDateTime());
					modified = true;
				}
			});
		}
		return jCalendarDate;
	}

	private JLabel getJLabelDate() {
		if (jLabelDate == null) {
			jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			jLabelDate.setPreferredSize(LABELS_DIMENSION);
		}
		return jLabelDate;
	}

	private JPanel getJPanelDate() {
		if (jPanelDate == null) {
			jPanelDate = new JPanel();
			jPanelDate.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelDate.add(getJLabelDate());
			jPanelDate.add(getJCalendarDate());
			jPanelDate.add(getJButtonPickPatient());
			jPanelDate.add(getJButtonTrashPatient());
			if (!GeneralData.getGeneralData().getSINGLEUSER()) {
				jPanelDate.add(getJLabelUser());
			}
		}
		return jPanelDate;
	}

	private JLabel getJLabelUser() {
		if (jLabelUser == null) {
			jLabelUser = new JLabel(MainMenu.getUser().getUserName());
			jLabelUser.setPreferredSize(USER_DIMENSION); // improve Layouts avoiding fixed dimensions
			jLabelUser.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUser.setForeground(Color.BLUE);
			jLabelUser.setFont(new Font(jLabelUser.getFont().getName(), Font.BOLD, jLabelUser.getFont().getSize() + 2));
		}
		return jLabelUser;
	}

	private JButton getJButtonTrashPatient() {
		if (jButtonTrashPatient == null) {
			jButtonTrashPatient = new JButton();
			jButtonTrashPatient.setPreferredSize(new Dimension(25, 25));
			jButtonTrashPatient.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
			jButtonTrashPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.removethepatientassociatedwiththisbill.tooltip"));
			if (thisBill.getBillPatient() == null) {
				jButtonTrashPatient.setEnabled(false);
			}
			jButtonTrashPatient.addActionListener(actionEvent -> {
				// BILL
				thisBill.setBillPatient(null);
				thisBill.setIsPatient(false);
				thisBill.setPatName(""); //$NON-NLS-1$
				thisBill.setAdmission(null);
				// INTERFACE
				jTextFieldPatient.setText("");
				jButtonPickPatient.setText(MessageBundle.getMessage("angal.newbill.findpatient.btn"));
				jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.associateapatientwiththisbill.tooltip"));
				jButtonTrashPatient.setEnabled(false);
			});
		}
		return jButtonTrashPatient;
	}

	private void setJButtonTrashPatient() {
		if (!thisBill.isPatient()) {
			jButtonTrashPatient.setEnabled(false);
		}
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton(MessageBundle.getMessage("angal.newbill.findpatient.btn"));
			jButtonPickPatient.setPreferredSize(new Dimension(150, 25));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.newbill.findpatient.btn.key"));
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
			jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.associateapatientwiththisbill.tooltip"));
			jButtonPickPatient.addActionListener(actionEvent -> {

				Patient patientSelected = thisBill.getBillPatient();
				SelectPatient sp = new SelectPatient(this, patientSelected);
				sp.addSelectionListener(this);
				sp.pack();
				sp.setVisible(true);
			});

		}
		setJButtonPickPatient();
		return jButtonPickPatient;
	}

	private void openPatientSearchWindow() {
		SelectPatient sp = new SelectPatient(this, jTextFieldPatient.getText());
		sp.addSelectionListener(this);
		sp.pack();
		sp.setVisible(true);
	}

	private void setJButtonPickPatient() {
		if (thisBill.isPatient()) {
			jButtonPickPatient.setText(MessageBundle.getMessage("angal.newbill.changepatient.btn"));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.newbill.changepatient.btn.key"));
			jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.changethepatientassociatedwiththisbill.tooltip"));
			if (jButtonTrashPatient != null) {
				jButtonTrashPatient.setEnabled(true);	
			}
		}
	}

	private void setPatientSelected(Patient patientSelected) {
		thisBill.setIsPatient(true);
		thisBill.setBillPatient(patientSelected);
		thisBill.setPatName(patientSelected.getName());
		reductionPlanId = GeneralData.ENABLEREDUCTIONPLAN && patientSelected.getReductionPlan() != null
						? patientSelected.getReductionPlan().getId() : 0;
	}

	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			jPanelTop = new JPanel();
			jPanelTop.setLayout(new BoxLayout(jPanelTop, BoxLayout.Y_AXIS));
			jPanelTop.add(getJPanelDate());
			jPanelTop.add(getJPanelWardAndList());
			jPanelTop.add(getJPanelPatient());
		}
		return jPanelTop;
	}

	/**
	 * Wraps {@link #getJPanelTop()} (date/patient info) and {@link #getJPanelItemSearch()} (the
	 * combined item search) into a single NORTH region, so the search field sits directly below the
	 * patient panel and above {@link #getJPanelData()}'s bill items table.
	 */
	private JPanel getJPanelNorthContainer() {
		if (jPanelNorthContainer == null) {
			jPanelNorthContainer = new JPanel();
			jPanelNorthContainer.setLayout(new BoxLayout(jPanelNorthContainer, BoxLayout.Y_AXIS));
			jPanelNorthContainer.add(getJPanelTop());
			jPanelNorthContainer.add(getJPanelItemSearch());
		}
		return jPanelNorthContainer;
	}

	/**
	 * A search field plus a live-filtered, scrollable list of matches across every category
	 * (Medical, Exam, Operation, Other) combined. Empty when the field is empty; matching is case-
	 * and accent-insensitive via {@link BillItemSearchSupport}. Selecting a result adds it using the
	 * same add flow as its category's own Add button.
	 */
	private JPanel getJPanelItemSearch() {
		if (jPanelItemSearch == null) {
			jPanelItemSearch = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 4, 2, 4);
			gbc.anchor = GridBagConstraints.WEST;

			gbc.gridx = 0;
			gbc.gridy = 0;
			jPanelItemSearch.add(new JLabel(MessageBundle.getMessage("angal.newbill.searchitem.txt")), gbc);
			gbc.gridx = 1;
			jPanelItemSearch.add(new JLabel(MessageBundle.getMessage("angal.newbill.item.col")), gbc);
			gbc.gridx = 2;
			jPanelItemSearch.add(new JLabel(MessageBundle.getMessage("angal.common.qty.txt")), gbc);
			gbc.gridx = 3;
			jPanelItemSearch.add(new JLabel(MessageBundle.getMessage("angal.common.amount.txt")), gbc);

			getJTextFieldItemSearch().setPreferredSize(new Dimension(180, 22));
			getJDialogItemDescription().setPreferredSize(new Dimension(180, 22));
			getJDialogItemQuantity().setPreferredSize(new Dimension(50, 22));
			getJDialogItemPrice().setPreferredSize(new Dimension(80, 22));

			gbc.gridy = 1;
			gbc.gridx = 0;
			jPanelItemSearch.add(getJTextFieldItemSearch(), gbc);
			gbc.gridx = 1;
			jPanelItemSearch.add(getJDialogItemDescription(), gbc);
			gbc.gridx = 2;
			jPanelItemSearch.add(getJDialogItemQuantity(), gbc);
			gbc.gridx = 3;
			jPanelItemSearch.add(getJDialogItemPrice(), gbc);

			gbc.gridx = 4;
			gbc.gridy = 0;
			gbc.gridheight = 2;
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			jPanelItemSearch.add(Box.createHorizontalGlue(), gbc);
		}
		return jPanelItemSearch;
	}

	private JTextField getJDialogItemDescription() {
		if (jDialogItemDescription == null) {
			jDialogItemDescription = new JTextField();
			jDialogItemDescription.setEditable(false);
		}
		return jDialogItemDescription;
	}

	private JTextField getJDialogItemQuantity() {
		if (jDialogItemQuantity == null) {
			jDialogItemQuantity = new JTextField();
			jDialogItemQuantity.addActionListener(actionEvent -> confirmEditedItem());
		}
		return jDialogItemQuantity;
	}

	private JTextField getJDialogItemPrice() {
		if (jDialogItemPrice == null) {
			jDialogItemPrice = new JTextField();
			jDialogItemPrice.addActionListener(actionEvent -> confirmEditedItem());
		}
		return jDialogItemPrice;
	}

	private JTextField getJTextFieldItemSearch() {
		if (jTextFieldItemSearch == null) {
			jTextFieldItemSearch = new JTextField(25);
			jTextFieldItemSearch.addActionListener(actionEvent -> openItemSearchWindow());
		}
		return jTextFieldItemSearch;
	}

	private JTextField getJDialogSearchField() {
		if (jDialogSearchField == null) {
			jDialogSearchField = new JTextField(25);
			jDialogSearchField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void insertUpdate(DocumentEvent event) {
					refreshWindowSearchResults();
				}

				@Override
				public void removeUpdate(DocumentEvent event) {
					refreshWindowSearchResults();
				}

				@Override
				public void changedUpdate(DocumentEvent event) {
					refreshWindowSearchResults();
				}
			});
		}
		return jDialogSearchField;
	}

	private void openItemSearchWindow() {
		filterResults(jTextFieldItemSearch.getText());
		if (itemSearchResultsModel.isEmpty()) {
			MessageDialog.warning(this, "angal.newbill.noitemfound.msg");
			return;
		}

		JDialog window = getJItemSearchWindow();
		jDialogSearchField.setText(jTextFieldItemSearch.getText());
		window.setVisible(true);
		jDialogSearchField.requestFocusInWindow();
		jDialogSearchField.selectAll();
	}

	private JDialog getJItemSearchWindow() {
		if (jItemSearchWindow == null) {
			jItemSearchWindow = new JDialog(this, MessageBundle.getMessage("angal.newbill.searchitem.txt"), false);

			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JPanel searchFieldRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
			searchFieldRow.add(new JLabel(MessageBundle.getMessage("angal.newbill.searchitem.txt")));
			searchFieldRow.add(getJDialogSearchField());

			contentPanel.add(searchFieldRow, BorderLayout.NORTH);
			contentPanel.add(new JScrollPane(getJListItemSearchResults()), BorderLayout.CENTER);

			jItemSearchWindow.setLayout(new BorderLayout());
			jItemSearchWindow.add(contentPanel, BorderLayout.CENTER);
			jItemSearchWindow.setSize(550, 400);
			jItemSearchWindow.setLocationRelativeTo(this);
		}
		return jItemSearchWindow;
	}

	private void filterResults(String filterText) {
		itemSearchResultsModel.clear();
		if (filterText != null && !filterText.isBlank()) {
			for (Price price : BillItemSearchSupport.filter(prcListArray, filterText)) {
				itemSearchResultsModel.addElement(price);
			}
		}
	}

	private void refreshWindowSearchResults() {
		filterResults(jDialogSearchField.getText());
	}

	private JList<Price> getJListItemSearchResults() {
		if (jListItemSearchResults == null) {
			jListItemSearchResults = new JList<>(itemSearchResultsModel);
			jListItemSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jListItemSearchResults.setVisibleRowCount(5);
			jListItemSearchResults.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2) {
						addItemFromSearchWindow(jListItemSearchResults.getSelectedValue());
					}
				}
			});
			jListItemSearchResults.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent event) {
					if (event.getKeyCode() == KeyEvent.VK_ENTER) {
						addItemFromSearchWindow(jListItemSearchResults.getSelectedValue());
					}
				}
			});
		}
		return jListItemSearchResults;
	}

	private void addItemFromSearchWindow(Price selected) {
		if (selected == null) {
			return;
		}

		closeItemSearchWindow();

		if (selected.getGroup().equals("OTH")) {
			// "Other" garde sa gestion spéciale (discharge/daily/undefined), déjà prix+qty inclus
			addSelectedOther(selected);
			return;
		}

		try {
			selected = ReductionPlanBillSupport.applyReduction(selected, reductionPlanManager, reductionPlanId);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
			return;
		}

		if (BillItemPriceSupport.requiresPricePrompt(selected)) {
			Icon moneyIcon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
			String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
					MessageBundle.getMessage("angal.newbill.item.title"), JOptionPane.PLAIN_MESSAGE, moneyIcon, null, selected.getPrice());
			if (price == null) {
				return;
			}
			try {
				selected.setPrice(BillItemPriceSupport.parseAmount(price));
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
		}

		String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
				MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, null, null, 1);
		if (quantity == null) {
			return;
		}
		int qty;
		try {
			qty = Integer.parseInt(quantity.trim());
			if (qty <= 0) {
				MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
				return;
			}
		} catch (NumberFormatException nfe) {
			MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
			return;
		}

		if ("MED".equals(selected.getGroup())) {
			Ward selectedWard = (Ward) jComboBoxWard.getSelectedItem();
			if (selectedWard != null) {
				List<MedicalWard> wardStock;
				try {
					wardStock = movWardBrowserManager.getMedicalsWard(selectedWard.getCode(), true);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
					return;
				}
				if (qty > BillingWardSupport.availableQuantity(selected, wardStock)) {
					MessageDialog.error(this, "angal.newbill.notenoughstockinward.msg");
					return;
				}
			}
		}

		addItem(selected, qty, true);
		closeItemSearchWindow();
	}

	private void closeItemSearchWindow() {
		jTextFieldItemSearch.setText("");
		if (jDialogSearchField != null) {
			jDialogSearchField.setText("");
		}
		itemSearchResultsModel.clear();
		if (jItemSearchWindow != null) {
			jItemSearchWindow.setVisible(false);
		}
	}

	private void openEditQuantityDialog(int row) {
		if (jItemSearchWindow != null) {
			jItemSearchWindow.setVisible(false);
		}
		BillItems item = billItems.get(row);
		editingBillItem = item;
		editingBillItemRow = row;

		Price matchingPrice = item.isPrice() ? getPrice(item.getPriceID()) : null;
		jDialogItemDescription.setText(item.getItemDescription());
		jDialogItemQuantity.setText(String.valueOf(item.getItemQuantity()));
		jDialogItemPrice.setText(String.valueOf(item.getItemAmount()));
		jDialogItemPrice.setEditable(matchingPrice == null || BillItemPriceSupport.requiresPricePrompt(matchingPrice));

		jDialogItemQuantity.requestFocusInWindow();
		jDialogItemQuantity.selectAll();
	}

	private void confirmEditedItem() {
		if (editingBillItem == null) {
			return;
		}

		int qty;
		try {
			qty = Integer.parseInt(jDialogItemQuantity.getText().trim());
			if (qty <= 0) {
				MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
				return;
			}
		} catch (NumberFormatException nfe) {
			MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
			return;
		}

		if (editingBillItem.isPrice()) {
			Price matchingPrice = getPrice(editingBillItem.getPriceID());
			if (matchingPrice != null && "MED".equals(matchingPrice.getGroup())) {
				Ward selectedWard = (Ward) jComboBoxWard.getSelectedItem();
				if (selectedWard != null) {
					List<MedicalWard> wardStock;
					try {
						wardStock = movWardBrowserManager.getMedicalsWard(selectedWard.getCode(), true);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e, this);
						return;
					}
					// an already-saved item's current quantity is already deducted from the ward's on-hand stock,
					// so it must be added back before comparing against the requested new quantity
					int alreadyReserved = editingBillItemRow < billItemsSaved ? editingBillItem.getItemQuantity() : 0;
					int available = BillingWardSupport.availableQuantity(matchingPrice, wardStock) + alreadyReserved;
					if (qty > available) {
						MessageDialog.error(this, "angal.newbill.notenoughstockinward.msg");
						return;
					}
				}
			}
		}

		if (jDialogItemPrice.isEditable()) {
			try {
				editingBillItem.setItemAmount(BillItemPriceSupport.parseAmount(jDialogItemPrice.getText().trim()));
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
		}

		editingBillItem.setItemQuantity(qty);
		modified = true;
		updateTotals();
		updateGUI();

		editingBillItem = null;
		editingBillItemRow = -1;
		jDialogItemDescription.setText("");
		jDialogItemQuantity.setText("");
		jDialogItemPrice.setText("");
	}

	private JScrollPane getJScrollPaneBill() {
		if (jScrollPaneBill == null) {
			jScrollPaneBill = new JScrollPane();
			jScrollPaneBill.setBorder(null);
			jScrollPaneBill.setViewportView(getJTableBill());
			Dimension size = new Dimension(PANEL_WIDTH, BILL_HEIGHT);
			jScrollPaneBill.setMaximumSize(size);
			jScrollPaneBill.setMinimumSize(size);
			jScrollPaneBill.setPreferredSize(size);
		}
		return jScrollPaneBill;
	}

	private JTable getJTableBill() {
		if (jTableBill == null) {
			jTableBill = new JTable(new BillTableModel());
			jTableBill.getColumnModel().getColumn(1).setMinWidth(QUANTITY_WIDTH);
			jTableBill.getColumnModel().getColumn(1).setMaxWidth(QUANTITY_WIDTH);
			jTableBill.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
			jTableBill.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
			jTableBill.setAutoCreateColumnsFromModel(false);
			jTableBill.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2) {
						int row = jTableBill.getSelectedRow();
						if (row > -1) {
							openEditQuantityDialog(row);
						}
					}
				}
			});
		}
		return jTableBill;
	}

	private void setJTableBill() {
		jTableBill.setModel(new BillTableModel());
	}

	private JScrollPane getJScrollPaneBigTotal() {
		if (jScrollPaneBigTotal == null) {
			jScrollPaneBigTotal = new JScrollPane();
			jScrollPaneBigTotal.setViewportView(getJTableBigTotal());
			jScrollPaneBigTotal.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPaneBigTotal.setMaximumSize(BIGTOTAL_TABLE_SIZE);
			jScrollPaneBigTotal.setMinimumSize(BIGTOTAL_TABLE_SIZE);
			jScrollPaneBigTotal.setPreferredSize(BIGTOTAL_TABLE_SIZE);
		}
		return jScrollPaneBigTotal;
	}

	private JScrollPane getJScrollPaneTotal() {
		if (jScrollPaneTotal == null) {
			jScrollPaneTotal = new JScrollPane();
			jScrollPaneTotal.setViewportView(getJTableTotal());
			jScrollPaneTotal.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPaneTotal.setMaximumSize(TOTAL_TABLE_SIZE);
			jScrollPaneTotal.setMinimumSize(TOTAL_TABLE_SIZE);
			jScrollPaneTotal.setPreferredSize(TOTAL_TABLE_SIZE);
		}
		return jScrollPaneTotal;
	}

	private JTable getJTableBigTotal() {
		if (jTableBigTotal == null) {
			jTableBigTotal = new JTable(new JTableBigTotalModel());
			sizeJTableBigTotal();
		}
		return jTableBigTotal;
	}

	private void sizeJTableBigTotal() {
		jTableBigTotal.getColumnModel().getColumn(1).setMinWidth(CURRENCY_CODE_WIDTH);
		jTableBigTotal.getColumnModel().getColumn(1).setMaxWidth(CURRENCY_CODE_WIDTH);
		jTableBigTotal.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
		jTableBigTotal.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
		jTableBigTotal.setMaximumSize(BIGTOTAL_TABLE_SIZE);
		jTableBigTotal.setMinimumSize(BIGTOTAL_TABLE_SIZE);
		jTableBigTotal.setPreferredSize(BIGTOTAL_TABLE_SIZE);
	}

	private void setJTableBigTotal() {
		jTableBigTotal.setModel(new JTableBigTotalModel());
		sizeJTableBigTotal();
	}

	private JTable getJTableTotal() {
		if (jTableTotal == null) {
			jTableTotal = new JTable(new JTableTotalModel());
			sizeJTableTotal();
		}
		return jTableTotal;
	}

	private void setJTableTotal() {
		jTableTotal.setModel(new JTableTotalModel());
		sizeJTableTotal();
	}

	private void sizeJTableTotal() {
		jTableTotal.getColumnModel().getColumn(1).setMinWidth(CURRENCY_CODE_WIDTH);
		jTableTotal.getColumnModel().getColumn(1).setMaxWidth(CURRENCY_CODE_WIDTH);
		jTableTotal.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
		jTableTotal.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
		jTableTotal.setMaximumSize(TOTAL_TABLE_SIZE);
		jTableTotal.setMinimumSize(TOTAL_TABLE_SIZE);
		jTableTotal.setPreferredSize(TOTAL_TABLE_SIZE);
	}

	private JScrollPane getJScrollPanePayment() {
		if (jScrollPanePayment == null) {
			jScrollPanePayment = new JScrollPane();
			jScrollPanePayment.setBorder(null);
			jScrollPanePayment.setViewportView(getJTablePayment());
			Dimension size = new Dimension(PANEL_WIDTH, PAYMENT_HEIGHT);
			jScrollPanePayment.setMaximumSize(size);
			jScrollPanePayment.setMinimumSize(size);
			jScrollPanePayment.setPreferredSize(size);
		}
		return jScrollPanePayment;
	}

	private JTable getJTablePayment() {
		if (jTablePayment == null) {
			jTablePayment = new JTable(new PaymentTableModel());
			jTablePayment.getColumnModel().getColumn(1).setMinWidth(PRICE_WIDTH);
			jTablePayment.getColumnModel().getColumn(1).setMaxWidth(PRICE_WIDTH);
		}
		return jTablePayment;
	}

	private void setJTablePayment() {
		jTablePayment.setModel(new PaymentTableModel());
	}

	private JScrollPane getJScrollPaneBalance() {
		if (jScrollPaneBalance == null) {
			jScrollPaneBalance = new JScrollPane();
			jScrollPaneBalance.setViewportView(getJTableBalance());
			jScrollPaneBalance.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPaneBalance.setMaximumSize(BALANCE_TABLE_SIZE);
			jScrollPaneBalance.setMinimumSize(BALANCE_TABLE_SIZE);
			jScrollPaneBalance.setPreferredSize(BALANCE_TABLE_SIZE);
		}
		return jScrollPaneBalance;
	}

	private JTable getJTableBalance() {
		if (jTableBalance == null) {
			jTableBalance = new JTable(new JTableBalanceModel());
			sizeJTableBalance();
		}
		return jTableBalance;
	}

	private void setJTableBalance() {
		jTableBalance.setModel(new JTableBalanceModel());
		sizeJTableBalance();
	}

	private void sizeJTableBalance() {
		jTableBalance.getColumnModel().getColumn(1).setMinWidth(CURRENCY_CODE_WIDTH);
		jTableBalance.getColumnModel().getColumn(1).setMaxWidth(CURRENCY_CODE_WIDTH);
		jTableBalance.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
		jTableBalance.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
		jTableBalance.setMaximumSize(BALANCE_TABLE_SIZE);
		jTableBalance.setMinimumSize(BALANCE_TABLE_SIZE);
		jTableBalance.setPreferredSize(BALANCE_TABLE_SIZE);
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new BoxLayout(jPanelButtons, BoxLayout.Y_AXIS));
			jPanelButtons.add(getJPanelButtonsBill());
			jPanelButtons.add(getJPanelButtonsPayment());
			jPanelButtons.add(Box.createVerticalGlue());
			jPanelButtons.add(getJPanelButtonsActions());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelButtonsBill() {
		if (jPanelButtonsBill == null) {
			jPanelButtonsBill = new JPanel();
			jPanelButtonsBill.setLayout(new BoxLayout(jPanelButtonsBill, BoxLayout.Y_AXIS));
			jPanelButtonsBill.add(getJButtonAddPrescription());
			jPanelButtonsBill.add(getJButtonAddMedical());
			jPanelButtonsBill.add(getJButtonAddOperation());
			jPanelButtonsBill.add(getJButtonAddExam());
			jPanelButtonsBill.add(getJButtonAddOther());
			jPanelButtonsBill.add(getJButtonAddCustom());
			jPanelButtonsBill.add(getJButtonRemoveItem());
			Dimension size = new Dimension(BUTTON_WIDTH, BILL_HEIGHT + TOTAL_HEIGHT);
			jPanelButtonsBill.setMinimumSize(size);
			jPanelButtonsBill.setMaximumSize(size);
			jPanelButtonsBill.setPreferredSize(size);

		}
		return jPanelButtonsBill;
	}

	private JPanel getJPanelButtonsPayment() {
		if (jPanelButtonsPayment == null) {
			jPanelButtonsPayment = new JPanel();
			jPanelButtonsPayment.setLayout(new BoxLayout(jPanelButtonsPayment, BoxLayout.Y_AXIS));
			jPanelButtonsPayment.add(getJButtonAddPayment());
			jPanelButtonsPayment.add(getJButtonAddRefund());
			if (GeneralData.RECEIPTPRINTER) {
				jPanelButtonsPayment.add(getJButtonPrintPayment());
			}
			jPanelButtonsPayment.add(getJButtonRemovePayment());
			Dimension size = new Dimension(BUTTON_WIDTH, PAYMENT_HEIGHT);
			jPanelButtonsPayment.setMinimumSize(size);
			jPanelButtonsPayment.setMaximumSize(size);
		}
		return jPanelButtonsPayment;
	}

	private JPanel getJPanelButtonsActions() {
		if (jPanelButtonsActions == null) {
			jPanelButtonsActions = new JPanel();
			jPanelButtonsActions.setLayout(new BoxLayout(jPanelButtonsActions, BoxLayout.Y_AXIS));
			jPanelButtonsActions.add(getJButtonBalance());
			jPanelButtonsActions.add(getJButtonSave());
			jPanelButtonsActions.add(getJButtonPaid());
			jPanelButtonsActions.add(getJButtonClose());
		}
		return jPanelButtonsActions;
	}

	private JButton getJButtonBalance() {
		if (jButtonBalance == null) {
			jButtonBalance = new JButton(MessageBundle.getMessage("angal.newbill.givechange.btn"));
			jButtonBalance.setMnemonic(MessageBundle.getMnemonic("angal.newbill.givechange.btn.key"));
			jButtonBalance.setMaximumSize(BUTTON_ACTION_SIZE);
			jButtonBalance.setIcon(new ImageIcon("rsc/icons/money_button.png"));
			jButtonBalance.setHorizontalAlignment(SwingConstants.LEFT);
			toggleJButtonBalance();
			jButtonBalance.addActionListener(actionEvent -> {

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");
				BigDecimal amount = new BigDecimal(0);

				String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.entercustomercash.txt"),
								MessageBundle.getMessage("angal.newbill.givechange.title"), JOptionPane.OK_CANCEL_OPTION, icon, null, amount);

				if (quantity != null) {
					try {
						amount = new BigDecimal(quantity);
						if (amount.equals(new BigDecimal(0)) || amount.compareTo(balance) < 0) {
							return;
						}
						JOptionPane.showMessageDialog(this,
										MessageBundle.formatMessage("angal.newbill.givechange.fmt.msg", amount.subtract(balance)),
										MessageBundle.getMessage("angal.newbill.givechange.title"), JOptionPane.OK_OPTION, icon);
					} catch (Exception eee) {
						MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
					}
				}
			});
		}
		return jButtonBalance;
	}

	private void toggleJButtonBalance() {
		if (insert) {
			jButtonBalance.setEnabled(false);
		}
	}

	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			jButtonSave.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			jButtonSave.setMaximumSize(BUTTON_ACTION_SIZE);
			jButtonSave.setIcon(new ImageIcon("rsc/icons/save_button.png"));
			jButtonSave.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonSave.addActionListener(actionEvent -> {

				if (jComboBoxWard.getSelectedItem() == null) {
					MessageDialog.error(this, "angal.newbill.pleaseselectawardfirst.msg");
					return;
				}

				/*
				 * we check again for underlying data changes
				 */
				loadDataset();
				checkBill();

				if (thisBill.getPriceList() == null) { // FIXME: workaround ? to be removed ?
					thisBill.setPriceList(lstArray.get(0));
				}

				if (insert) {
					Bill newBill = new Bill(
									0, // Bill ID
									thisBill.getDate(), // from calendar
									null, // updateDate from most recent payment, will be set later
									true, // is a PriceList? always true, non-pricelist not managed
									thisBill.getPriceList(), // List
									thisBill.getPriceList().getName(), // List name
									thisBill.isPatient(), // is a Patient?
									thisBill.getBillPatient(), // Patient
									thisBill.isPatient() ? thisBill.getBillPatient().getName() : jTextFieldPatient.getText(), // Patient Name
									paid ? "C" : "O", // CLOSED or OPEN TODO: enumerate bills status
									total.doubleValue(), // Total
									balance.doubleValue(), // Balance
									user, // User
									thisBill.getAdmission()); // Admission
					newBill.setWard((Ward) jComboBoxWard.getSelectedItem());

					try {
						billBrowserManager.newBill(newBill, billItems, payItems); // TODO: to verify if when can just pass thisBill
						thisBill.setId(newBill.getId());
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex, this);
						return;
					}
					fireBillInserted(newBill);
					dispose();

				} else {
					Bill updateBill = new Bill(
									thisBill.getId(), // Bill ID
									thisBill.getDate(), // from calendar
									null, // updateDate from most recent payment, will be set later
									true, // is a PriceList? always true, non-pricelist not managed
									thisBill.getPriceList(), // List
									thisBill.getPriceList().getName(), // List name
									thisBill.isPatient(), // is a Patient?
									thisBill.getBillPatient(), // Patient
									thisBill.isPatient() ? thisBill.getPatName() : jTextFieldPatient.getText(), // Patient Name
									paid ? "C" : "O", // CLOSED or OPEN
									total.doubleValue(), // Total
									balance.doubleValue(), // Balance
									user, // User
									thisBill.getAdmission()); // Admission
					updateBill.setLock(thisBill.getLock());
					updateBill.setWard((Ward) jComboBoxWard.getSelectedItem());

					try {
						billBrowserManager.updateBill(updateBill, billItems, payItems); // TODO: to verify if when can just pass thisBill
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex, this);
						return;
					}
					fireBillInserted(updateBill);
				}
				if (hasNewPayments()) {
					TxtPrinter.initialize();
					new GenericReportBill(thisBill.getId(), "PatientBillPayments", false, !TxtPrinter.PRINT_WITHOUT_ASK);
				}
				if (paid && GeneralData.RECEIPTPRINTER) {
					TxtPrinter.initialize();
					if (TxtPrinter.PRINT_AS_PAID) {
						new GenericReportBill(thisBill.getId(), GeneralData.PATIENTBILL, false, !TxtPrinter.PRINT_WITHOUT_ASK);
					}
				}
				RememberDates.setLastBillDate(thisBill.getDate()); // to remember for next INSERT
				dispose();
			});
		}
		return jButtonSave;
	}

	private boolean hasNewPayments() {
		return (insert && !payItems.isEmpty()) || (payItems.size() - payItemsSaved) > 0;
	}

	private JButton getJButtonPrintPayment() {
		if (jButtonPrintPayment == null) {
			jButtonPrintPayment = new JButton(MessageBundle.getMessage("angal.newbill.paymentreceipt.btn"));
			jButtonPrintPayment.setMnemonic(MessageBundle.getMnemonic("angal.newbill.paymentreceipt.btn.key"));
			jButtonPrintPayment.setMaximumSize(BUTTON_PAYMENT_SIZE);
			jButtonPrintPayment.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonPrintPayment.setIcon(new ImageIcon("rsc/icons/receipt_button.png"));
			jButtonPrintPayment.addActionListener(actionEvent -> {
				TxtPrinter.initialize();
				new GenericReportBill(thisBill.getId(), "PatientBillPayments", false, !TxtPrinter.PRINT_WITHOUT_ASK);
			});
		}
		return jButtonPrintPayment;
	}

	private void setJButtonPrintPayment() {
		if (insert) {
			jButtonPrintPayment.setEnabled(false);
		}
	}

	private JButton getJButtonPaid() {
		if (jButtonPaid == null) {
			jButtonPaid = new JButton(MessageBundle.getMessage("angal.newbill.paid.btn"));
			jButtonPaid.setMnemonic(MessageBundle.getMnemonic("angal.newbill.paid.btn.key"));
			jButtonPaid.setMaximumSize(BUTTON_ACTION_SIZE);
			jButtonPaid.setIcon(new ImageIcon("rsc/icons/ok_button.png"));
			jButtonPaid.setHorizontalAlignment(SwingConstants.LEFT);
			toggleJButtonPaid();
			jButtonPaid.addActionListener(actionEvent -> {

				LocalDateTime datePay;

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
				int ok = MessageDialog.yesNo(this, icon, "angal.newbill.doyouwanttosetthecurrentbillaspaid.msg");
				if (ok == JOptionPane.NO_OPTION) {
					return;
				}

				if (balance.compareTo(new BigDecimal(0)) > 0) {
					if (thisBill.getDate().isBefore(today)) { // if Bill is in the past the user will be asked for PAID date

						icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$

						GoodDateTimeSpinnerChooser datePayChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());

						int r = JOptionPane.showConfirmDialog(this, datePayChooser,
										MessageBundle.getMessage("angal.newbill.dateofpayment.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
										icon);

						if (r == JOptionPane.OK_OPTION) {
							datePay = datePayChooser.getLocalDateTime();
						} else {
							return;
						}

						if (isValidPaymentDate(datePay)) {
							addPayment(datePay, balance.doubleValue());
						} else {
							return;
						}
					} else {
						datePay = TimeTools.getNow();
						addPayment(datePay, balance.doubleValue());
					}
				}
				paid = true;
				updateBalance();
				jButtonSave.doClick();
			});
		}
		return jButtonPaid;
	}

	private void toggleJButtonPaid() {
		if (insert) {
			jButtonPaid.setEnabled(false);
		}
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.setMaximumSize(BUTTON_ACTION_SIZE);
			jButtonClose.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			jButtonClose.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonClose.addActionListener(actionEvent -> {
				if (modified) {
					int ok = MessageDialog.yesNoCancel(this, "angal.newbill.billhasbeenchangedwouldyouliketosavethechanges.msg");
					if (ok == JOptionPane.YES_OPTION) {
						jButtonSave.doClick();
					} else if (ok == JOptionPane.NO_OPTION) {
						dispose();
					}
				} else {
					dispose();
				}
			});
		}
		return jButtonClose;
	}

	private JButton getJButtonAddRefund() {
		if (jButtonAddRefund == null) {
			jButtonAddRefund = new JButton(MessageBundle.getMessage("angal.newbill.refund.btn"));
			jButtonAddRefund.setMnemonic(MessageBundle.getMnemonic("angal.newbill.refund.btn.key"));
			jButtonAddRefund.setMaximumSize(BUTTON_PAYMENT_SIZE);
			jButtonAddRefund.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddRefund.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddRefund.addActionListener(actionEvent -> {

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");
				BigDecimal amount = new BigDecimal(0);

				LocalDateTime datePay;

				String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
								MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, icon, null, amount);
				if (quantity != null) {
					try {
						amount = new BigDecimal(quantity).negate();
						if (amount.equals(new BigDecimal(0))) {
							return;
						}
					} catch (Exception eee) {
						MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
						return;
					}
				} else {
					return;
				}

				if (thisBill.getDate().isBefore(today)) { // if is a bill in the past the user will be asked for date of payment

					GoodDateTimeSpinnerChooser datePayChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());
					int r = JOptionPane.showConfirmDialog(this, datePayChooser, MessageBundle.getMessage("angal.newbill.dateofpayment.title"),
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

					if (r == JOptionPane.OK_OPTION) {
						datePay = datePayChooser.getLocalDateTime();
					} else {
						return;
					}

					if (isValidPaymentDate(datePay)) {
						addPayment(datePay, amount.doubleValue());
					}
				} else {
					datePay = TimeTools.getNow();
					addPayment(datePay, amount.doubleValue());
				}
			});
		}
		return jButtonAddRefund;
	}

	private boolean isValidPaymentDate(LocalDateTime datePay) {
		LocalDateTime now = TimeTools.getNow();
		LocalDateTime lastPay;
		if (!payItems.isEmpty()) {
			lastPay = payItems.get(payItems.size() - 1).getDate();
		} else {
			lastPay = thisBill.getDate();
		}
		if (datePay.isBefore(thisBill.getDate())) {
			MessageDialog.error(this, "angal.newbill.paymentmadebeforebilldate.msg");
			return false;
		} else if (datePay.isBefore(lastPay)) {
			MessageDialog.error(this, "angal.newbill.thedateisbeforethelastpayment.msg");
			return false;
		} else if (datePay.isAfter(now)) {
			MessageDialog.error(this, "angal.newbill.payementsinthefuturearenotallowed.msg");
			return false;
		}
		return true;
	}

	private JButton getJButtonAddPayment() {
		if (jButtonAddPayment == null) {
			jButtonAddPayment = new JButton(MessageBundle.getMessage("angal.newbill.payment.btn"));
			jButtonAddPayment.setMnemonic(MessageBundle.getMnemonic("angal.newbill.payment.btn.key"));
			jButtonAddPayment.setMaximumSize(BUTTON_PAYMENT_SIZE);
			jButtonAddPayment.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddPayment.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddPayment.addActionListener(actionEvent -> {

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");
				BigDecimal amount = balance;

				LocalDateTime datePay;

				String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
								MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, icon, null, amount);
				if (quantity != null) {
					try {
						amount = new BigDecimal(quantity);
						if (amount.equals(new BigDecimal(0))) {
							return;
						}
					} catch (Exception eee) {
						MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
						return;
					}
				} else {
					return;
				}

				if (thisBill.getDate().isBefore(today)) { // if is a bill in the past the user will be asked for date of payment

					GoodDateTimeSpinnerChooser datePayChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());
					int r = JOptionPane.showConfirmDialog(this, datePayChooser, MessageBundle.getMessage("angal.newbill.dateofpayment.title"),
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

					if (r == JOptionPane.OK_OPTION) {
						datePay = datePayChooser.getLocalDateTime();
					} else {
						return;
					}

					if (isValidPaymentDate(datePay)) {
						addPayment(datePay, amount.doubleValue());
					}
				} else {
					datePay = TimeTools.getNow();
					addPayment(datePay, amount.doubleValue());
				}
			});
		}
		return jButtonAddPayment;
	}

	private JButton getJButtonRemovePayment() {
		if (jButtonRemovePayment == null) {
			jButtonRemovePayment = new JButton(MessageBundle.getMessage("angal.newbill.removepayment.btn"));
			jButtonRemovePayment.setMnemonic(MessageBundle.getMnemonic("angal.newbill.removepayment.btn.key"));
			jButtonRemovePayment.setMaximumSize(BUTTON_PAYMENT_SIZE);
			jButtonRemovePayment.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonRemovePayment.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jButtonRemovePayment.addActionListener(actionEvent -> {
				int row = jTablePayment.getSelectedRow();
				if (row > -1) {
					removePayment(row);
				}
			});
		}
		return jButtonRemovePayment;
	}

	private JButton getJButtonAddOther() {
		if (jButtonAddOther == null) {
			jButtonAddOther = new JButton(MessageBundle.getMessage("angal.newbill.other.btn"));
			jButtonAddOther.setMnemonic(MessageBundle.getMnemonic("angal.newbill.other.btn.key"));
			jButtonAddOther.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddOther.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddOther.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddOther.addActionListener(actionEvent -> {

				List<Price> othArray = new ArrayList<>();
				for (Price price : prcListArray) {
					if (price.getGroup().equals("OTH")) { // TODO: enumerate price categories
						othArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/plus_dialog.png");
				Price oth = SearchableItemDialog.show(this, MessageBundle.getMessage("angal.newbill.item.title"), icon,
								MessageBundle.getMessage("angal.newbill.pleaseselectanitem.txt"), othArray);

				if (oth != null) {
					addSelectedOther(oth);
				}
			});
		}
		return jButtonAddOther;
	}

	private JButton getJButtonAddPrescription() {
		if (jButtonAddPrescription == null) {
			jButtonAddPrescription = new JButton(MessageBundle.getMessage("angal.newbill.prescription.btn"));
			jButtonAddPrescription.setMnemonic(MessageBundle.getMnemonic("angal.newbill.prescription.btn.key"));
			jButtonAddPrescription.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddPrescription.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddPrescription.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddPrescription.addActionListener(actionEvent -> {

				if (!thisBill.isPatient() || thisBill.getBillPatient() == null) {
					MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
					return;
				}

				if (jComboBoxWard.getSelectedItem() == null) {
					MessageDialog.error(this, "angal.newbill.pleaseselectawardfirst.msg");
					return;
				}

				try {
					if (!billBrowserManager.hasOutstandingPrescriptions(thisBill.getBillPatient())) {
						MessageDialog.info(this, "angal.newbill.noprescriptionforthispatient.msg");
						return;
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
					return;
				}

				SelectPrescriptions selectPrescriptions = new SelectPrescriptions(this, thisBill.getBillPatient());
				selectPrescriptions.addPrescriptionSelectedListener(this);
				selectPrescriptions.setVisible(true);
			});
		}
		return jButtonAddPrescription;
	}

	@Override
	public void prescriptionSelected(List<SelectPrescriptions.SelectedPrescription> prescriptions) {
		Ward selectedWard = (Ward) jComboBoxWard.getSelectedItem();
		List<String> warnings = new ArrayList<>();

		for (SelectPrescriptions.SelectedPrescription prescription : prescriptions) {
			boolean alreadyOnBill = billItems.stream().anyMatch(item -> item.getPrescriptionId() != null
							&& item.getPrescriptionId() == prescription.getPrescriptionId()
							&& prescription.getItemGroup().equals(item.getItemGroup()));
			if (alreadyOnBill) {
				warnings.add(MessageBundle.formatMessage("angal.newbill.prescriptionalreadyadded.fmt.msg", prescription.getDescription()));
				continue;
			}

			try {
				if (billBrowserManager.isPrescriptionAlreadyBilledAndPaid(thisBill.getBillPatient().getCode(), prescription.getPrescriptionId(),
								prescription.getItemGroup())) {
					warnings.add(MessageBundle.formatMessage("angal.newbill.prescriptionalreadybilled.fmt.msg", prescription.getDescription()));
					continue;
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
				continue;
			}

			Price price = getPrice(prescription.getItemGroup() + prescription.getItemCode());
			if (price == null) {
				warnings.add(MessageBundle.formatMessage("angal.newbill.prescriptionnotinpricelist.fmt.msg", prescription.getDescription()));
				continue;
			}

			try {
				price = ReductionPlanBillSupport.applyReduction(price, reductionPlanManager, reductionPlanId);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
				continue;
			}

			if (BillItemPriceSupport.requiresPricePrompt(price)) {
				Icon moneyIcon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
				String priceInput = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
								prescription.getDescription(), JOptionPane.PLAIN_MESSAGE, moneyIcon, null, price.getPrice());
				if (priceInput == null) {
					// user cancelled: skip just this item, keep processing the rest of the batch
					continue;
				}
				try {
					price.setPrice(BillItemPriceSupport.parseAmount(priceInput));
				} catch (NumberFormatException nfe) {
					MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
					continue;
				}
			}

			int qty = prescription.getQuantity();
			if (SelectPrescriptions.MEDICAL_GROUP_CODE.equals(prescription.getItemGroup())) {
				List<MedicalWard> wardStock;
				try {
					wardStock = movWardBrowserManager.getMedicalsWard(selectedWard.getCode(), true);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
					continue;
				}
				if (qty > BillingWardSupport.availableQuantity(price, wardStock)) {
					warnings.add(MessageBundle.formatMessage("angal.newbill.notenoughstockinwardforfmt.msg", price.getDesc()));
					continue;
				}
			}

			addPrescriptionItem(price, qty, prescription.getItemGroup(), prescription.getPrescriptionId());
		}

		if (!warnings.isEmpty()) {
			JOptionPane.showMessageDialog(this, String.join("\n", warnings), MessageDialog.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE);
		}

		updateTotals();
		updateGUI();
	}

	private void addPrescriptionItem(Price price, int qty, String itemGroup, int prescriptionId) {
		try {
			BillItems item = new BillItems(0, billBrowserManager.getBill(thisBill.getId()), true, price.getGroup() + price.getItem(), price.getDesc(),
							price.getPrice(), qty);
			item.setItemGroup(itemGroup);
			item.setPrescriptionId(prescriptionId);
			billItems.add(item);
			modified = true;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}

	private void addSelectedOther(Price oth) {
		boolean isPrice = true;

		try {
			oth = ReductionPlanBillSupport.applyReduction(oth, reductionPlanManager, reductionPlanId);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
			return;
		}

		Map<Integer, PricesOthers> othersHashMap = new HashMap<>();
		for (PricesOthers other : othPrices) {
			othersHashMap.put(other.getId(), other);
		}

		if (othersHashMap.get(Integer.valueOf(oth.getItem())).isUndefined()) {
			Icon icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
			String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
							MessageBundle.getMessage("angal.common.undefined.txt"), JOptionPane.PLAIN_MESSAGE, icon, null, "0"); //$NON-NLS-2$
			try {
				if (price == null) {
					return;
				}
				double amount = Double.parseDouble(price);
				oth.setPrice(amount);
				isPrice = false;
			} catch (Exception eee) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
		}
		if (othersHashMap.get(Integer.valueOf(oth.getItem())).isDischarge()) {
			double amount = oth.getPrice();
			oth.setPrice(-amount);
		}
		if (othersHashMap.get(Integer.valueOf(oth.getItem())).isDaily()) {
			int qty = 1;
			Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
			String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmanydays.txt"),
							MessageBundle.getMessage("angal.newbill.days.title"), JOptionPane.PLAIN_MESSAGE, icon, null, qty);
			try {
				if (quantity == null || quantity.isEmpty()) {
					return;
				}
				qty = Integer.parseInt(quantity);
				addItem(oth, qty, isPrice);
			} catch (Exception eee) {
				MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
			}
		} else {
			addItem(oth, 1, isPrice);
		}
	}

	private JButton getJButtonAddExam() {
		if (jButtonAddExam == null) {
			jButtonAddExam = new JButton(MessageBundle.getMessage("angal.newbill.exam.btn"));
			jButtonAddExam.setMnemonic(MessageBundle.getMnemonic("angal.newbill.exam.btn.key"));
			jButtonAddExam.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddExam.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddExam.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddExam.addActionListener(actionEvent -> {

				List<Price> exaArray = new ArrayList<>();
				for (Price price : prcListArray) {

					if (price.getGroup().equals("EXA")) {
						exaArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/exam_dialog.png"); //$NON-NLS-1$
				Price exa = SearchableItemDialog.show(this, MessageBundle.getMessage("angal.newbill.exam.title"), icon,
								MessageBundle.getMessage("angal.newbill.selectanexam.txt"), exaArray);
				if (exa != null) {
					addSelectedExam(exa);
				}
			});
		}
		return jButtonAddExam;
	}

	private void addSelectedExam(Price exa) {
		try {
			exa = ReductionPlanBillSupport.applyReduction(exa, reductionPlanManager, reductionPlanId);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
			return;
		}
		if (BillItemPriceSupport.requiresPricePrompt(exa)) {
			Icon moneyIcon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
			String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
							MessageBundle.getMessage("angal.newbill.exam.title"), JOptionPane.PLAIN_MESSAGE, moneyIcon, null, exa.getPrice());
			if (price == null) {
				return;
			}
			try {
				exa.setPrice(BillItemPriceSupport.parseAmount(price));
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
		}
		addItem(exa, 1, true);
	}

	private JButton getJButtonAddOperation() {
		if (jButtonAddOperation == null) {
			jButtonAddOperation = new JButton(MessageBundle.getMessage("angal.newbill.operation.btn"));
			jButtonAddOperation.setMnemonic(MessageBundle.getMnemonic("angal.newbill.operation.btn.key"));
			jButtonAddOperation.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddOperation.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddOperation.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddOperation.addActionListener(actionEvent -> {

				List<Price> opeArray = new ArrayList<>();
				for (Price price : prcListArray) {

					if (price.getGroup().equals("OPE")) {
						opeArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/operation_dialog.png"); //$NON-NLS-1$
				Price ope = SearchableItemDialog.show(this, MessageBundle.getMessage("angal.newbill.operation.title"), icon,
								MessageBundle.getMessage("angal.newbill.selectanoperation.txt"), opeArray);
				if (ope != null) {
					addSelectedOperation(ope);
				}
			});
		}
		return jButtonAddOperation;
	}

	private void addSelectedOperation(Price ope) {
		try {
			ope = ReductionPlanBillSupport.applyReduction(ope, reductionPlanManager, reductionPlanId);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
			return;
		}
		if (BillItemPriceSupport.requiresPricePrompt(ope)) {
			Icon moneyIcon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
			String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
							MessageBundle.getMessage("angal.newbill.operation.title"), JOptionPane.PLAIN_MESSAGE, moneyIcon, null, ope.getPrice());
			if (price == null) {
				return;
			}
			try {
				ope.setPrice(BillItemPriceSupport.parseAmount(price));
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
		}
		addItem(ope, 1, true);
	}

	private JButton getJButtonAddMedical() {
		if (jButtonAddMedical == null) {
			jButtonAddMedical = new JButton(MessageBundle.getMessage("angal.newbill.medical.btn"));
			jButtonAddMedical.setMnemonic(MessageBundle.getMnemonic("angal.newbill.medical.btn"));
			jButtonAddMedical.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddMedical.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddMedical.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddMedical.addActionListener(actionEvent -> {

				Ward selectedWard = (Ward) jComboBoxWard.getSelectedItem();
				if (selectedWard == null) {
					MessageDialog.error(this, "angal.newbill.pleaseselectawardfirst.msg");
					return;
				}

				List<MedicalWard> wardStock;
				try {
					wardStock = movWardBrowserManager.getMedicalsWard(selectedWard.getCode(), true);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, this);
					return;
				}

				List<Price> medArray = new ArrayList<>();
				for (Price price : prcListArray) {

					if (price.getGroup().equals("MED") && BillingWardSupport.isMedicalInWardStock(price, wardStock)) {
						medArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/medical_dialog.png"); //$NON-NLS-1$
				Price med = SearchableItemDialog.show(this, MessageBundle.getMessage("angal.newbill.medical.title"), icon,
								MessageBundle.getMessage("angal.newbill.selectamedical.txt"), medArray);
				if (med != null) {
					addSelectedMedical(med);
				}
			});
		}
		return jButtonAddMedical;
	}


	private void addSelectedMedical(Price med) {
		try {
			med = ReductionPlanBillSupport.applyReduction(med, reductionPlanManager, reductionPlanId);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
			return;
		}
		Icon icon = new ImageIcon("rsc/icons/medical_dialog.png"); //$NON-NLS-1$
		int qty = 1;
		String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
						MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, icon, null, qty);
		try {
			if (quantity == null || quantity.equals("")) {
				return;
			}
			qty = Integer.parseInt(quantity);
		} catch (Exception eee) {
			MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
			return;
		}

		Ward selectedWard = (Ward) jComboBoxWard.getSelectedItem();
		if (selectedWard != null) {
			List<MedicalWard> wardStock;
			try {
				wardStock = movWardBrowserManager.getMedicalsWard(selectedWard.getCode(), true);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
				return;
			}
			if (qty > BillingWardSupport.availableQuantity(med, wardStock)) {
				MessageDialog.error(this, "angal.newbill.notenoughstockinward.msg");
				return;
			}
		}

		if (BillItemPriceSupport.requiresPricePrompt(med)) {
			Icon moneyIcon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
			String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
							MessageBundle.getMessage("angal.newbill.medical.title"), JOptionPane.PLAIN_MESSAGE, moneyIcon, null, med.getPrice());
			if (price == null) {
				return;
			}
			try {
				med.setPrice(BillItemPriceSupport.parseAmount(price));
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
				return;
			}
		}
		addItem(med, qty, true);
	}

	private JButton getJButtonAddCustom() {
		if (jButtonCustom == null) {
			jButtonCustom = new JButton(MessageBundle.getMessage("angal.newbill.custom.btn"));
			jButtonCustom.setMnemonic(MessageBundle.getMnemonic("angal.newbill.custom.btn.key"));
			jButtonCustom.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonCustom.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonCustom.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonCustom.addActionListener(actionEvent -> {
				double amount;
				Icon icon = new ImageIcon("rsc/icons/custom_dialog.png"); //$NON-NLS-1$
				String desc = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.chooseadescription.txt"),
								MessageBundle.getMessage("angal.newbill.customitem.title"), JOptionPane.PLAIN_MESSAGE, icon, null,
								MessageBundle.getMessage("angal.newbill.newdescription.txt"));
				if (desc == null || desc.equals("")) { //$NON-NLS-1$
					return;
				} else {
					icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
					String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
									MessageBundle.getMessage("angal.newbill.customitem.title"), JOptionPane.PLAIN_MESSAGE, icon, null, "0"); //$NON-NLS-2$
					try {
						amount = Double.parseDouble(price);
					} catch (Exception eee) {
						MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
						return;
					}

				}

				try {
					BillItems newItem = new BillItems(0, billBrowserManager.getBill(thisBill.getId()), false, "", //$NON-NLS-1$
									desc, amount, 1);
					addItem(newItem);
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			});
		}
		return jButtonCustom;
	}

	private JButton getJButtonRemoveItem() {
		if (jButtonRemoveItem == null) {
			jButtonRemoveItem = new JButton(MessageBundle.getMessage("angal.newbill.removeitem.btn"));
			jButtonRemoveItem.setMnemonic(MessageBundle.getMnemonic("angal.newbill.removeitem.btn.key"));
			jButtonRemoveItem.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonRemoveItem.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonRemoveItem.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jButtonRemoveItem.addActionListener(actionEvent -> {
				int row = jTableBill.getSelectedRow();
				if (row > -1) {
					removeItem(row);
				}
			});
		}
		return jButtonRemoveItem;
	}

	private void updateTotal() { // only positive items make the bill's total
		total = new BigDecimal(0);
		for (BillItems item : billItems) {
			double amount = item.getItemAmount();
			if (amount > 0) {
				BigDecimal itemAmount = new BigDecimal(Double.toString(amount));
				total = total.add(itemAmount.multiply(new BigDecimal(item.getItemQuantity())));
			}
		}
	}

	private void updateBigTotal() { // the big total (to pay) is made by all items
		bigTotal = new BigDecimal(0);
		for (BillItems item : billItems) {
			BigDecimal itemAmount = new BigDecimal(Double.toString(item.getItemAmount()));
			bigTotal = bigTotal.add(itemAmount.multiply(new BigDecimal(item.getItemQuantity())));
		}
	}

	private void updateBalance() { // the balance is what remaining after payments
		balance = new BigDecimal(0);
		BigDecimal payments = new BigDecimal(0);
		for (BillPayments pay : payItems) {
			BigDecimal payAmount = new BigDecimal(Double.toString(pay.getAmount()));
			payments = payments.add(payAmount);
		}
		balance = bigTotal.subtract(payments);
		if (jButtonPaid != null) {
			jButtonPaid.setEnabled(balance.compareTo(new BigDecimal(0)) >= 0);
		}
		if (jButtonBalance != null) {
			jButtonBalance.setEnabled(balance.compareTo(new BigDecimal(0)) >= 0);
		}
	}

	private void addItem(Price prc, int qty, boolean isPrice) {
		if (prc != null) {
			double amount = prc.getPrice();
			try {
				BillItems item = new BillItems(0, billBrowserManager.getBill(thisBill.getId()), isPrice, prc.getGroup() + prc.getItem(), prc.getDesc(), amount,
								qty);
				billItems.add(item);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}
			modified = true;
			updateTotals();
			updateGUI();
		}
	}

	private void updateTotals() {
		updateTotal();
		updateBigTotal();
		updateBalance();
	}

	private void addItem(BillItems item) {
		if (item != null) {
			billItems.add(item);
			modified = true;
			updateTotals();
			updateGUI();
		}
	}

	private void addPayment(LocalDateTime datePay, double qty) {
		if (qty != 0) {
			try {
				BillPayments pay = new BillPayments(0, billBrowserManager.getBill(thisBill.getId()), datePay, qty, user);
				payItems.add(pay);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}
			modified = true;
			Collections.sort(payItems);
			updateBalance();
			updateGUI();
		}
	}

	private void removeItem(int row) {
		if (row != -1 && row >= billItemsSaved) {
			billItems.remove(row);
			updateTotals();
			updateGUI();
		} else {
			MessageDialog.error(null, "angal.newbill.youcannotdeletealreadysaveditems.msg");
		}
	}

	private void removePayment(int row) {
		if (row != -1 && row >= payItemsSaved) {
			payItems.remove(row);
			updateTotals();
			updateGUI();
		} else {
			MessageDialog.error(null, "angal.newbill.youcannotdeletepastpayments.msg");
		}
	}

	public class BillTableModel implements TableModel {

		public BillTableModel() {
		}

		@Override
		public Class< ? > getColumnClass(int i) {
			return billClasses[i].getClass();
		}

		@Override
		public int getColumnCount() {
			return billClasses.length;
		}

		@Override
		public int getRowCount() {
			if (billItems == null) {
				return 0;
			}
			return billItems.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			BillItems item = billItems.get(r);
			if (c == -1) {
				return item;
			}
			if (c == 0) {
				return item.getItemDescription();
			}
			if (c == 1) {
				return item.getItemQuantity();
			}
			if (c == 2) {
				BigDecimal qty = new BigDecimal(item.getItemQuantity());
				BigDecimal amount = new BigDecimal(Double.toString(item.getItemAmount()));
				return amount.multiply(qty).doubleValue();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return c == 1;
		}

		@Override
		public void setValueAt(Object item, int r, int c) {
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public String getColumnName(int columnIndex) {
			return billColumnNames[columnIndex];
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}

	}

	public class PaymentTableModel implements TableModel {

		public PaymentTableModel() {
			updateBalance();
		}

		@Override
		public void addTableModelListener(TableModelListener l) {

		}

		@Override
		public Class< ? > getColumnClass(int columnIndex) {
			return paymentClasses[columnIndex].getClass();
		}

		@Override
		public int getColumnCount() {
			return paymentClasses.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return null;
		}

		@Override
		public int getRowCount() {
			return payItems.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (c == -1) {
				return payItems.get(r);
			}
			if (c == 0) {
				BillPayments payment = payItems.get(r);
				return PaymentCashierSupport.formatPaymentDateWithCashier(
					formatDateTime(payment.getDate()), resolveCashierDisplayName(payment.getUser()));
			}
			if (c == 1) {
				return payItems.get(r).getAmount();
			}
			return null;
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
		}
	}

	private final class JTableTotalModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		Class< ? >[] types = new Class< ? >[] { JLabel.class, JLabel.class, Double.class, };

		private JTableTotalModel() {
			super(new Object[][] { { "<html><b>" + MessageBundle.getMessage("angal.common.total.txt").toUpperCase() + "</b></html>", currencyCod, total } },
							new String[] { "", "", "" });
		}
		@Override
		public Class< ? > getColumnClass(int columnIndex) {
			return types[columnIndex];
		}
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	public String formatDateTime(LocalDateTime time) {
		return DATE_TIME_FORMATTER.format(time);
	}

	private String resolveCashierDisplayName(String username) {
		if (username == null || username.isBlank()) {
			return null;
		}
		return cashierDisplayNameCache.computeIfAbsent(username, name -> {
			try {
				User cashier = userBrowsingManager.getUserByName(name);
				if (cashier != null && cashier.getDesc() != null && !cashier.getDesc().isBlank()) {
					return cashier.getDesc();
				}
			} catch (OHServiceException e) {
				LOGGER.error(e.getMessage(), e);
			}
			return name;
		});
	}

	private final class JTableBalanceModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		Class< ? >[] types = new Class< ? >[] { JLabel.class, JLabel.class, Double.class, };

		private JTableBalanceModel() {
			super(new Object[][] {
					{ "<html><b>" + MessageBundle.getMessage("angal.newbill.balance.txt").toUpperCase() + "</b></html>", currencyCod, balance } },
							new String[] { "", "", "" });
		}
		@Override
		public Class< ? > getColumnClass(int columnIndex) {
			return types[columnIndex];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private final class JTableBigTotalModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		Class< ? >[] types = new Class< ? >[] { JLabel.class, JLabel.class, Double.class, };

		private JTableBigTotalModel() {
			super(new Object[][] { { "<html><b>" + MessageBundle.getMessage("angal.newbill.topay.txt") + "</b></html>", currencyCod, bigTotal } },
							new String[] { "", "", "" });
		}
		@Override
		public Class< ? > getColumnClass(int columnIndex) {
			return types[columnIndex];
		}
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
