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

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.isf.accounting.gui.BillItemGroupBrowser.BillItemGroupListener;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItemGroupItem;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
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
import org.isf.partner.manager.PartnerBrowserManager;
import org.isf.partner.model.Partner;
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
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.GoodDateTimeToggleChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhTableModel;
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
public class PatientBillEdit extends JDialog implements SelectionListener, BillItemGroupListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientBillEdit.class);
	private static final ImageIcon ADMISSION_ICON = new ImageIcon("rsc/icons/bed_icon.png");
	private static final String OPD_TEXT = MessageBundle.getMessage("angal.common.opd.txt");

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
					}
				} else {
					MessageDialog.error(null, "angal.newbill.thispatienthasapendingbill.msg");
					initData(patientPendingBills.get(0), false);
				}
			} else {
				if (GeneralData.ALLOWMULTIPLEOPENEDBILL) {
					int response = MessageDialog.yesNo(this, "angal.newbill.thispatienthasmorethanonependingbilldoyouwanttocreateanother.msg");
					if (response == JOptionPane.YES_OPTION) {
						insert = true;
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

    @Override
    public void groupSelected(List<BillItemGroupItem> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        if (selectedPatient == null) {

            JOptionPane.showMessageDialog(
                    PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.pleaseselectapatient")
            );

            return;
        }

        boolean stockMovementEnabled = GeneralData.STOCKMVTONBILLSAVE;

        Ward selectedWard = null;
        String wardCode = "";

        Object selected = wardComboBox.getSelectedItem();

        if (selected instanceof Ward) {
            selectedWard = (Ward) selected;
            wardCode = selectedWard.getCode();
        }

        if ((wardCode == null || wardCode.isEmpty()) && stockMovementEnabled) {

            JOptionPane.showMessageDialog(
                    PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.pleaseselectaward")
            );

            return;
        }

        List<String> errors = new ArrayList<>();
        List<BillItems> validBillItems = new ArrayList<>();

        for (BillItemGroupItem groupItem : items) {

            try {

                Price price = getPrice(groupItem.getPriceId());

                if (price == null) {

                    errors.add(
                            MessageBundle.getMessage("angal.newbill.pricenotfoundforitem")
                                    + " : "
                                    + groupItem.getDescription()
                    );

                    continue;
                }

                if ("MED".equals(price.getGroup())) {

                    MedicalWard medicalWard = getMedicalWard(price);
                    if (medicalWard == null) {

                        errors.add(groupItem.getDescription()
                                + " - "
                                        + MessageBundle.getMessage("angal.newbill.stocknotavailableforitem")
                        );

                        continue;
                    }
                }

                BillItems billItem = buildBillItem(
                        price,
                        groupItem.getQuantity(),
                        groupItem.getDescription()
                );

                if (billItem == null) {
                    continue;
                }

                if ("MED".equals(price.getGroup())) {

                    MedicalWard medicalWard = getMedicalWard(price);

                    if (medicalWard != null
                            && medicalWard.getMedical() != null) {

                        billItem.setItemDisplayCode(
                                medicalWard.getMedical().getProdCode()
                        );
                    }

                } else if ("OTH".equals(price.getGroup())) {

                    try {

                        othPrices = pricesOthersManager.getOthers();

                        PricesOthers pricesOther = null;

                        if (othPrices != null) {

                            pricesOther = othPrices.stream()
                                    .filter(p ->
                                            p.getId() == Integer.parseInt(price.getItem()))
                                    .findFirst()
                                    .orElse(null);
                        }

                        if (pricesOther != null) {
                            billItem.setItemDisplayCode(pricesOther.getCode());
                        }

                    } catch (OHServiceException e) {

                        LOGGER.error("Error loading other prices", e);
                    }
                }

                validBillItems.add(billItem);

            } catch (Exception e) {

                errors.add(
                        groupItem.getDescription()
                                + " - "
                                + e.getMessage()
                );
            }
        }

        if (!errors.isEmpty() && validBillItems.isEmpty()) {

            JOptionPane.showMessageDialog(
                    PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.allitemscontainerrors")
                            + "\n"
                            + String.join("\n", errors),
                    MessageBundle.getMessage("angal.common.errordialogtitle"),
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (!errors.isEmpty()) {

            int action = JOptionPane.showConfirmDialog(
                    PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.someitemscontainerrors")
                            + "\n\n"
                            + String.join("\n", errors)
                            + "\n\n"
                            + MessageBundle.getMessage("angal.newbill.addvaliditemsonly"),
                    MessageBundle.getMessage("angal.common.confirmactiondialogtitle"),
                    JOptionPane.YES_NO_OPTION
            );

            /*
             * NO = CANCEL EVERYTHING
             */
            if (action != JOptionPane.YES_OPTION) {
                return;
            }
        }

        billItems.addAll(validBillItems);

        modified = true;

        updateTotals();
        updateGUI();

        if (!validBillItems.isEmpty()) {

            selectedBillItem = validBillItems.get(validBillItems.size() - 1);

            loadFields();

            quantityTextField.setText(
                    String.valueOf(selectedBillItem.getItemQuantity())
            );
        }

        jTableBill.updateUI();
    }

	private static final long serialVersionUID = 1L;
	private JTable jTableBill;
	private JScrollPane jScrollPaneBill;
	private JButton jButtonAddMedical;
	private JButton jButtonAddOperation;
	private JButton jButtonAddExam;
	private JButton jButtonAddOther;
	private JButton jButtonAddPayment;
	private JButton jButtonHistory;
	private JPanel jPanelButtons;
	private JPanel jPanelDate;
	private JPanel jPanelPatient;
	private JTable jTablePayment;
	private JScrollPane jScrollPanePayment;
	private JTextField jTextFieldPatient;
	private JComboBox<PriceList> jComboBoxPriceList;
	private JPanel jPanelData;
	private JPanel jPanelPrice;
	private JTable jTableTotal;
	private JScrollPane jScrollPaneTotal;
	private JTable jTableBigTotal;
	private JScrollPane jScrollPaneBigTotal;
	private JTable jTableBalance;
	private JScrollPane jScrollPaneBalance;
	private JPanel jPanelTop;
	private GoodDateTimeToggleChooser jCalendarDate;
	private JLabel jLabelDate;
	private JLabel jLabelUser;
	private JLabel jLabelPatient;
	private JButton jButtonRemoveItem;
	private JLabel jLabelPriceList;
	private JLabel jLabelWard;
	private JButton jButtonRemovePayment;
	private JButton jButtonAddRefund;
	private JPanel jPanelButtonsPayment;
	private JPanel jPanelButtonsBill;
	private JPanel jPanelButtonsActions;
	private JButton jButtonClose;
	private JButton jButtonPaid;
    private JButton jButtonPrintBill;
	private JButton jButtonSave;
	private JButton jButtonBalance;
	private JButton jButtonCustom;
	private JButton jButtonPickPatient;
	private JButton jButtonTrashPatient;
	private JComboBox<User> jComboBoxGuarantor;
	private JLabel jLabelGuarantor;
	private JButton jButtonAddPrescription;
	private JPanel searchDisplayPanel;

	private static final int PANEL_WIDTH = 450;
	private static final int BUTTON_WIDTH = 190;
	private static final int BUTTON_WIDTH_BILL = 190;
	private static final int BUTTON_WIDTH_PAYMENT = 190;
	private static final int PRICE_WIDTH = 190;
	private static final int CURRENCY_CODE_WIDTH = 40;
	private static final int QUANTITY_WIDTH = 40;
	private static final int BILL_HEIGHT = 280;
	private static final int TOTAL_HEIGHT = 20;
	private static final int BIG_TOTAL_HEIGHT = 20;
	private static final int PAYMENT_HEIGHT = 150;
	private static final int BALANCE_HEIGHT = 20;
	private static final int BUTTON_HEIGHT = 25;
	private static final Dimension PATIENT_DIMENSION = new Dimension(200, 20);
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
    private int pbiID = 0;

	// Tables
	private Object[] billClasses = { Price.class, Integer.class, Double.class };
	private String[] billColumnNames = {
			MessageBundle.getMessage("angal.newbill.item.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.qty.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.amount.txt").toUpperCase()
	};
	private Object[] paymentClasses = { Date.class, Double.class };

	private HospitalBrowsingManager hospitalManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
	private String currencyCod;
	private PriceListManager priceListManager = Context.getApplicationContext().getBean(PriceListManager.class);
	private List<Price> prcArray;
	private List<PriceList> lstArray;
	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
	private List<PricesOthers> othPrices;
	private BillBrowserManager billBrowserManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private UserBrowsingManager userBrowserManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);
	private ReductionPlanManager reductionPlanManager = Context.getApplicationContext().getBean(ReductionPlanManager.class);

	// Prices, Items and Payments for the tables
	private List<BillItems> billItems = new ArrayList<>();
	private List<BillPayments> payItems = new ArrayList<>();
	private List<Price> prcListArray = new ArrayList<>();
	private Map<String, Price> priceHashTable;
	private int billItemsSaved;
	private int payItemsSaved;
    private List<MedicalWard> medWardList;

	private String user = UserBrowsingManager.getCurrentUser();

    private JComboBox<Object> wardComboBox;
    private JLabel wardLabel;
    private JTextField quantityTextField;
    private JTextField searchTextField;
    private Patient selectedPatient;
    private JTextField descriptionTextField;
    private JTextField priceTextField;
    private JPanel searchPanel;
    private JLabel searchLabel;
    private JLabel quantityLabel;
    private JLabel amountLabel;
    private JLabel descriptionLabel;
    private BillItems selectedBillItem = null;
    private List<BillItems> originalBillItems;

    private JButton JButtonAddGroup;
    private JButton jButtonRemoveAllItem;
	public boolean hasBillGuarantor() {
		return GeneralData.ALLOWBILLGUARANTOR;
	}

	public PatientBillEdit() {
		thisBill = new Bill();
		PatientBillEdit pbe = new PatientBillEdit(null, thisBill, true);
		pbe.setVisible(true);
	}

	public PatientBillEdit(JFrame owner, Patient patient) {
		super(owner, true);
		thisBill = new Bill();
		ReductionPlan reductionPlan = patient != null ? patient.getReductionPlan() : null;
		this.pbiID = (reductionPlan != null && reductionPlan.getId() != 0) ? reductionPlan.getId() : 0;
		loadDataset();
		initData(thisBill, true);
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				patientSelected(patient);
			}
		});
	}

	public PatientBillEdit(JFrame owner, Bill bill, boolean inserting) {
		super(owner, true);
		loadDataset();
		initData(bill, inserting);
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
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
			thisBill.setDate(today.withSecond(0).withNano(0));
            thisBill.setPriceList(lstArray.get(0));
        } else {
            try {
                thisBill = (Bill) bill.clone();
                if (thisBill.getBillPatient() != null && thisBill.getBillPatient().getCode() != 0) {
                    updatePrescriptionButtonVisibility();
                }
                try {
                    billItems = billBrowserManager.getItems(thisBill.getId());
                    payItems = billBrowserManager.getPayments(thisBill.getId());
                    applyReductionToBillItems();
                } catch (OHServiceException e) {
                    OHServiceExceptionUtil.showMessages(e, this);
                }
                originalBillItems = billItems;
            } catch (CloneNotSupportedException cnse) {
                LOGGER.debug("CloneNotSupportedException", cnse);
            }
        }
        setPriceListArray();
        setCurrencyCode();
        updateTotals();
    }

    private void applyReductionToBillItems() {

        if (pbiID == 0 || billItems == null) return;

        for (BillItems item : billItems) {

            try {

                Price price = getPriceFromBillItem(item);

                if (price == null) continue;

                Price priceCopy = createPriceCopy(price);
                Price reducedPrice = applyReduction(priceCopy);

                item.setItemAmount(reducedPrice != null ? reducedPrice.getPrice() : price.getPrice());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Price getPriceFromBillItem(BillItems item) {
        if (item == null || item.getPriceID() == null || item.getPriceID().isEmpty()) {
            return null;
        }
        return getPrice(item.getPriceID());
    }

    private Price createPriceCopy(Price original) {
        if (original == null) return null;
        Price copy = new Price();
        copy.setId(original.getId());
        copy.setGroup(original.getGroup());
        copy.setItem(original.getItem());
        copy.setDesc(original.getDesc());
        copy.setPrice(original.getPrice());
    
        if (original.getList() != null) {
            copy.setList(original.getList());
        }
        return copy;
    }

    private Price applyReduction(Price price) {

        if (price == null || pbiID == 0) return price;

        try {
            Price priceCopy = createPriceCopy(price);
            switch (priceCopy.getGroup()) {
                case "MED":
                    return reductionPlanManager.getMedicalPrice(priceCopy, pbiID);
                case "EXA":
                    return reductionPlanManager.getExamPrice(priceCopy, pbiID);
                case "OPE":
                    return reductionPlanManager.getOperationPrice(priceCopy, pbiID);
                case "OTH":
                    return reductionPlanManager.getOtherPrice(priceCopy, pbiID);
                default:
                    return priceCopy;
            }
        } catch (OHServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private BillItems buildBillItem(
            Price originalPrice,
            int quantity,
            String displayDescription
    ) throws OHServiceException {

        if (originalPrice == null) {
            return null;
        }

        Price priceCopy = createPriceCopy(originalPrice);
        Price price = applyReduction(priceCopy);
        if (price == null) {
            price = priceCopy;
        }

        boolean stockMovementEnabled = GeneralData.STOCKMVTONBILLSAVE;

        if ("MED".equals(price.getGroup()) && stockMovementEnabled) {

            MedicalWard medicalWard = getMedicalWard(price);

            if (medicalWard == null) {
                MessageDialog.error(this, "angal.newbill.stocknotavailableforitem");
                return null;
            }

            if (!containPrice(price, quantity)) {
                MessageDialog.error(this, "angal.newbill.qtynotinstock");
                return null;
            }
        }

        BillItems billItem = new BillItems(
                0,
                thisBill,
                true,
                originalPrice.getGroup() + originalPrice.getItem(),
                originalPrice.getDesc(),
                price.getPrice(),
                quantity
        );

        billItem.setItemId(price.getItem());

        if ("MED".equals(price.getGroup())) {

            MedicalWard medicalWard = getMedicalWard(price);

            if (medicalWard != null) {
                billItem.setItemDisplayCode(
                        medicalWard.getMedical().getProdCode()
                );
            }

        } else if ("OTH".equals(price.getGroup())) {

            List<PricesOthers> others = pricesOthersManager.getOthers();

            final int itemId = Integer.parseInt(price.getItem());

            PricesOthers other = others.stream()
                    .filter(o -> o.getId() == itemId)
                    .findFirst()
                    .orElse(null);

            if (other != null) {

                billItem.setItemDisplayCode(other.getCode());

                if (other.isDischarge()) {
                    billItem.setItemAmount(
                            -Math.abs(billItem.getItemAmount())
                    );
                }

                if (other.isUndefined()) {

                    Icon icon = new ImageIcon("rsc/icons/money_dialog.png");

                    String value = (String) JOptionPane.showInputDialog(
                            this,
                            MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
                            MessageBundle.getMessage("angal.common.undefined.txt"),
                            JOptionPane.PLAIN_MESSAGE,
                            icon,
                            null,
                            "0"
                    );

                    if (value == null || value.trim().isEmpty()) {
                        return null;
                    }

                    try {
                        billItem.setItemAmount(
                                Double.parseDouble(value)
                        );
                    } catch (Exception ex) {
                        MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain");
                    }
                }
            }

        } else {

            billItem.setItemDisplayCode(displayDescription);
        }

		billItem.setItemGroup(price.getGroup());

        return billItem;
    }

	private void setCurrencyCode() {
		try {
			if (thisBill != null && thisBill.isList() && thisBill.getPriceList() != null && thisBill.getPriceList().getCurrency() != null
							&& !thisBill.getPriceList().getCurrency().isEmpty()) {
				this.currencyCod = thisBill.getPriceList().getCurrency();

			} else if (!lstArray.get(0).getCurrency().isEmpty()) {
				this.currencyCod = lstArray.get(0).getCurrency();
			} else {
				this.currencyCod = hospitalManager.getHospitalCurrencyCod();
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
		}
	}

	private void updateGUI() {
		setJButtonTrashPatient();
        setSelectedWard();
		setPatientTextField();
		setJButtonPickPatient();
        setJButtonPrintBill();
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
        add(getJPanelTop(), BorderLayout.NORTH);
        add(getJPanelData(), BorderLayout.CENTER);
        add(getJPanelButtons(), BorderLayout.EAST);
        updateTitle();
        pack();
        Dimension size = getSize();
        setMinimumSize(new Dimension(size.width, size.height + 10));
    }

	private void checkBill() {
		if (thisBill.isList()) {
			Optional<PriceList> priceList = lstArray.stream().filter(pl -> pl.getId() == thisBill.getPriceList().getId()).findFirst();

			if (priceList.isEmpty()) {
				Icon icon = new ImageIcon("rsc/icons/list_dialog.png");
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
					MessageDialog.info(this,
							MessageBundle.formatMessage("angal.newbill.thepricelistcurrencycodehaschangedarrow.fmt.msg",
									priceListFound.getCurrency(), this.currencyCod));
					setCurrencyCodeFromList(priceListFound);
				}
			}
		}

		List<String> notFoundPriceList = new ArrayList<>();
		List<String> changedPriceList = new ArrayList<>();
		for (BillItems item : billItems) {
			if (item.isPrice()) {
				Price p = getPrice(item.getPriceID());
				if (p == null) {
					notFoundPriceList.add(item.getItemDescription());
					item.setPriceID("");
					item.setPrice(false);
					modified = true;
				} else if (!item.getItemDescription().equals(p.getDesc()) || !p.getPrice().equals(item.getItemAmount())) {
					changedPriceList.add(item.getItemDescription());
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
			} else {
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
		for (BillItems item : billItems.parallelStream().filter(BillItems::isPrice).collect(Collectors.toList())) {
			Price p = getPrice(item.getPriceID());
			if (p != null && (!item.getItemDescription().equals(p.getDesc()) || !p.getPrice().equals(item.getItemAmount()))) {
				item.setItemDescription(p.getDesc());
				item.setItemAmount(p.getPrice());
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

	private JPanel getJPanelWardAndGuarantor() {
		if (jPanelPrice == null) {
			jPanelPrice = new JPanel();
			jPanelPrice.setLayout(new FlowLayout(FlowLayout.LEFT));
            jPanelPrice.add(getJLabelWard());
            jPanelPrice.add(getWardComboBox());
			if (hasBillGuarantor()) {
				jPanelPrice.add(getJLabelGuarantor());
				jPanelPrice.add(getJComboBoxGuarantor());
			}
		}
		return jPanelPrice;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {
			jPanelPatient = new JPanel();
			jPanelPatient.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelPatient.add(getPatientLabel());
			jPanelPatient.add(getPatientTextField());
			jPanelPatient.add(getJButtonPickPatient());
			jPanelPatient.add(getJButtonTrashPatient());
            jPanelPatient.add(getJLabelPriceList());
            jPanelPatient.add(getJComboBoxPriceList());
		}
		return jPanelPatient;
	}

    private JPanel getSearchPanel() {

        if (searchPanel == null) {

            searchPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2,5,2,5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // ---------- ROW 0 : LABELS ----------

            gbc.gridy = 0;

            gbc.gridx = 0;
            searchPanel.add(getSearchLabel(), gbc);

            gbc.gridx = 1;
            searchPanel.add(getDescriptionLabel(), gbc);

            gbc.gridx = 2;
            searchPanel.add(getQuantityLabel(), gbc);

            gbc.gridx = 3;
            searchPanel.add(getAmountLabel(), gbc);

            // ---------- ROW 1 : TEXTFIELDS ----------

            gbc.gridy = 1;

            gbc.gridx = 0;
            searchPanel.add(getJTextFieldSearch(), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            searchPanel.add(getDescriptionTextField(), gbc);

            gbc.gridx = 2;
            gbc.weightx = 0;
            searchPanel.add(getQuantityTextField(), gbc);

            gbc.gridx = 3;
            searchPanel.add(getPriceTextField(), gbc);
        }

        return searchPanel;
    }

    private JLabel getSearchLabel() {
        if (searchLabel == null) {
            searchLabel = new JLabel(MessageBundle.getMessage("angal.newbill.recherche"));
            searchLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        }
        return searchLabel;
    }

    private JLabel getQuantityLabel() {
        if (quantityLabel == null) {
            quantityLabel = new JLabel(MessageBundle.getMessage("angal.newbill.qty"));
            quantityLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        }
        return quantityLabel;
    }

    private JLabel getAmountLabel() {
        if (amountLabel == null) {
            amountLabel = new JLabel(MessageBundle.getMessage("angal.newbill.amount"));
            amountLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        }
        return amountLabel;
    }

    private JLabel getDescriptionLabel() {
        if (descriptionLabel == null) {
            descriptionLabel = new JLabel(MessageBundle.getMessage("angal.newbill.description"));
            descriptionLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        }
        return descriptionLabel;
    }

    private JTextField getJTextFieldSearch() {

        if (searchTextField == null) {

            searchTextField = new JTextField(15);
            searchTextField.setEnabled(true);

            searchTextField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {

                    switch (e.getKeyCode()) {

                        case KeyEvent.VK_ENTER:
                            if (!searchTextField.getText().isEmpty()) {
                                searchItem();
                            }
                            break;

                        case KeyEvent.VK_TAB:
                            searchItem();
                            break;

                        case KeyEvent.VK_ESCAPE:
                            selectedBillItem = null;
                            loadFields();
                            break;
                    }
                }
            });

            searchTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    searchTextField.selectAll();
                }
            });
        }

        // enforce it every time the getter is called
        searchTextField.setEnabled(true);

        return searchTextField;
    }

    private JTextField getDescriptionTextField() {

        if (descriptionTextField == null) {
            descriptionTextField = new JTextField(10);
            descriptionTextField.setEnabled(false);
        }

        return descriptionTextField;
    }

    private JTextField getQuantityTextField() {

        if (quantityTextField == null) {

            quantityTextField = new JTextField(10);
            quantityTextField.setHorizontalAlignment(JTextField.RIGHT);

            quantityTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    quantityTextField.selectAll();
                }
            });

            quantityTextField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {

                    switch (e.getKeyCode()) {

                        case KeyEvent.VK_ENTER:
                            if (updateBillItem()) {
                                selectedBillItem = null;
                                loadFields();
                                updateTotals();
                                updateGUI();
                            }
                            break;

                        case KeyEvent.VK_ESCAPE:
                            selectedBillItem = null;
                            loadFields();
                            break;
                    }
                }
            });
        }

        return quantityTextField;
    }

    private boolean updateBillItem() {
        if(selectedBillItem == null){
            return false;
        }
        String strQty = quantityTextField.getText();
        if (strQty == null || strQty.trim().isEmpty()) {
            JOptionPane.showMessageDialog(PatientBillEdit.this,
                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"), //$NON-NLS-1$
                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"), //$NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int qty = 1;

        try {
            strQty=strQty.replaceAll(",", ".");
            qty = Integer.parseInt(strQty);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(PatientBillEdit.this,
                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"),
                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (qty <= 0) {
            JOptionPane.showMessageDialog(PatientBillEdit.this,
                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"),
                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String strPrice = priceTextField.getText();

        double price = 0;
        try {
            if (strPrice == null) {
                JOptionPane.showMessageDialog(PatientBillEdit.this,
                        MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"),
                        MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            price = Double.parseDouble(strPrice);

        } catch (Exception eee) {
            JOptionPane.showMessageDialog(PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.invalidpricepleasetryagain"),
                    MessageBundle.getMessage("angal.newbill.invalidprice"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Price medPrice = getPrice(selectedBillItem.getPriceID());

        if(medPrice !=null && medPrice.getGroup().equals("MED")){
            if(GeneralData.STOCKMVTONBILLSAVE){
                if(selectedBillItem.getId() > 0) {

                    BillItems originalItem = originalBillItems.stream().filter(
                            bi -> bi.getId() == selectedBillItem.getId()
                    ).toList().get(0);

                    double diffQty = qty - originalItem.getItemQuantity();

                    if (diffQty > 0) {
                        if(!containPrice(medPrice, diffQty)){
                            JOptionPane.showMessageDialog(PatientBillEdit.this,
                                    MessageBundle.getMessage("angal.newbill.qtynotinstock"),
                                    MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),
                                    JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                } else {
                    if(!containPrice(medPrice, qty)){
                        JOptionPane.showMessageDialog(PatientBillEdit.this,
                                MessageBundle.getMessage("angal.newbill.qtynotinstock"),
                                MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

            }
        }
        this.selectedBillItem.setItemAmount(price);
        this.selectedBillItem.setItemQuantity(qty);

        jTableBill.updateUI();
        updateTotals();

        this.modified = true;

        return true;
    }

    private JTextField getPriceTextField() {
        if (priceTextField == null) {
            priceTextField = new JTextField();
            priceTextField.setColumns(10);
        }
        priceTextField.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                priceTextField.selectAll();

            }
        });
        priceTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    if(updateBillItem()){
                        PatientBillEdit.this.selectedBillItem = null;
                        loadFields();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    PatientBillEdit.this.selectedBillItem = null;
                    loadFields();
                }
            }
        });
        priceTextField.setHorizontalAlignment(JTextField.RIGHT);
        return priceTextField;
    }

	private JLabel getPatientLabel() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel(MessageBundle.getMessage("angal.common.patient.txt"));
			jLabelPatient.setPreferredSize(LABELS_DIMENSION);
		}
		return jLabelPatient;
	}

	private JTextField getPatientTextField() {
		if (jTextFieldPatient == null) {
			jTextFieldPatient = new JTextField();
			jTextFieldPatient.setText("");
			jTextFieldPatient.setPreferredSize(PATIENT_DIMENSION);

            jTextFieldPatient.addActionListener(actionEvent -> {
                String text = jTextFieldPatient.getText().trim();

                if (!text.isEmpty()) {
					SelectPatient dialog = new SelectPatient(
                            (JDialog) SwingUtilities.getWindowAncestor(jTextFieldPatient),
                            text,
                            true
					);

                    dialog.setVisible(true);

                    selectedPatient = dialog.getPatient();

                    if (selectedPatient != null) {
                        jTextFieldPatient.setText(selectedPatient.getName());
                        patientSelected(selectedPatient);
                        thisBill.setBillPatient(selectedPatient);
                        if (jButtonTrashPatient != null) {
                            jButtonTrashPatient.setEnabled(true);
                        }
                    }

                    jTextFieldPatient.requestFocus();
                }
            });
		}
		setPatientTextField();
		return jTextFieldPatient;
	}

	private void setPatientTextField() {
		if (thisBill.isPatient()) {
			jTextFieldPatient.setText(thisBill.getPatName());
			jTextFieldPatient.setEnabled(false);
		} else {
			jTextFieldPatient.setEnabled(true);
		}
	}

	private JLabel getJLabelPriceList() {
		if (jLabelPriceList == null) {
			jLabelPriceList = new JLabel(MessageBundle.getMessage("angal.newbill.list.txt"));
			jLabelPriceList.setPreferredSize(LABELS_DIMENSION);
		}
		return jLabelPriceList;
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

		priceHashTable = prcListArray.stream().collect(
				Collectors.toMap(price -> price.getList().getId() + price.getGroup() + price.getItem(), price -> price, (a, b) -> b, HashMap::new));
	}

	private void setCurrencyCodeFromList(PriceList list) {
		String currency = list.getCurrency();
		if (!currency.isBlank()) {
			this.currencyCod = currency;
		}
	}

	private JLabel getJLabelWard() {
		if (jLabelWard == null) {
			jLabelWard = new JLabel();
			jLabelWard = new JLabel(MessageBundle.getMessage("angal.newbill.ward.label"));
		}
		return jLabelWard;
	}

	private void setSelectedWard() {
		Admission admission = thisBill.getAdmission();
		if (admission != null) {
			wardComboBox.setSelectedItem(admission.getWard().getDescription());
		}
	}

	private GoodDateTimeToggleChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			if (insert) {
                jCalendarDate = new GoodDateTimeToggleChooser(today, false);
            } else {
                jCalendarDate = new GoodDateTimeToggleChooser(thisBill.getDate() != null ? thisBill.getDate() : today, false);
                today = thisBill.getDate() != null ? thisBill.getDate() : today;
            }
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
				}
			});
		}
		return jCalendarDate;
	}

	private JLabel getJLabelGuarantor() {
		if (jLabelGuarantor == null) {
			jLabelGuarantor = new JLabel(MessageBundle.getMessage("angal.newbill.selectguarantor.label"));
		}
		return jLabelGuarantor;
	}

	private JLabel getJLabelDate() {
		if (jLabelDate == null) {
			jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			jLabelDate.setPreferredSize(LABELS_DIMENSION);
		}
		return jLabelDate;
	}

	private JComboBox<User> getJComboBoxGuarantor() {
		if (jComboBoxGuarantor == null) {
			jComboBoxGuarantor = new JComboBox<>();
			try {
				List<User> users = userBrowserManager.getUser();
				jComboBoxGuarantor.addItem(null);
				for (User user : users) {
					jComboBoxGuarantor.addItem(user);
				}

				if (thisBill != null) {
					jComboBoxGuarantor.setSelectedItem(thisBill.getGuarantor());
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			jComboBoxGuarantor.setPreferredSize(new Dimension(150, 25));
			jComboBoxGuarantor.setFont(new Font("Arial", Font.PLAIN, 14));
		}
		return jComboBoxGuarantor;
	}

	private JPanel getJPanelDate() {
		if (jPanelDate == null) {
			jPanelDate = new JPanel();
			jPanelDate.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelDate.add(getJLabelDate());
			jPanelDate.add(getJCalendarDate());
			if (!GeneralData.getGeneralData().getSINGLEUSER()) {
				jPanelDate.add(getJLabelUser());
			}
		}
		return jPanelDate;
	}

	private JLabel getJLabelUser() {
		if (jLabelUser == null) {
			jLabelUser = new JLabel(MainMenu.getUser().getUserName());
			jLabelUser.setPreferredSize(USER_DIMENSION);
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
				thisBill.setBillPatient(null);
				thisBill.setIsPatient(false);
				thisBill.setPatName("");
				thisBill.setAdmission(null);
				jTextFieldPatient.setText("");
				jTextFieldPatient.setEnabled(true);
				jButtonPickPatient.setText(MessageBundle.getMessage("angal.newbill.findpatient.btn"));
				jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.associateapatientwiththisbill.tooltip"));
				jButtonTrashPatient.setEnabled(false);
				selectedPatient = null;
				modified = true;
				updateGUI();
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

                selectedPatient = thisBill.getBillPatient();
				SelectPatient sp = new SelectPatient(this,"");
				sp.addSelectionListener(this);
				sp.pack();
				sp.setVisible(true);

                selectedPatient = sp.getPatient();
			});

		}
		setJButtonPickPatient();
		return jButtonPickPatient;
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
        ReductionPlan reductionPlan = patientSelected.getReductionPlan();

        if (reductionPlan != null && reductionPlan.getId() != 0) {
            this.pbiID = reductionPlan.getId();
        }
		thisBill.setBillPatient(patientSelected);
		thisBill.setPatName(patientSelected.getName());
        selectedPatient = patientSelected;
        
        // Set default pricelist from patient's pricelist if available
        if (patientSelected.getPriceList() != null) {
            Optional<PriceList> patientPriceList = lstArray.stream()
                    .filter(pl -> pl.getId() == patientSelected.getPriceList().getId())
                    .findFirst();
            if (patientPriceList.isPresent()) {
                thisBill.setPriceList(patientPriceList.get());
                if (jComboBoxPriceList != null) {
                    jComboBoxPriceList.setSelectedItem(patientPriceList.get());
                    setCurrencyCodeFromList(patientPriceList.get());
                }
            }
        }
        updatePrescriptionButtonVisibility();
	}

	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(getJPanelPatient());
            panel.add(getSearchPanel());
			jPanelTop = new JPanel();
			jPanelTop.setLayout(new BoxLayout(jPanelTop, BoxLayout.Y_AXIS));
			jPanelTop.add(getJPanelDate(), BorderLayout.NORTH);
			jPanelTop.add(getJPanelWardAndGuarantor(), BorderLayout.CENTER);
			jPanelTop.add(panel, BorderLayout.SOUTH);
		}
		return jPanelTop;
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
                public void mousePressed(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {

                        int selectedRow = jTableBill.getSelectedRow();
                        if (selectedRow >= 0) {
                            BillItems item = billItems.get(jTableBill.getSelectedRow());
                            selectedBillItem = item;
                            loadFields();
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

	private JTable getJTableBigTotal() {
		if (jTableBigTotal == null) {
			jTableBigTotal = new JTable(new JTableBigTotalModel());
			sizeJTableBigTotal();
		}
		return jTableBigTotal;
	}

	private void setJTableBigTotal() {
		jTableBigTotal.setModel(new JTableBigTotalModel());
		sizeJTableBigTotal();
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
            jPanelButtonsBill.add(getJButtonAddGroup());
			jPanelButtonsBill.add(getJButtonRemoveItem());
            jPanelButtonsBill.add(getJButtonRemoveAllItem());
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
			jPanelButtonsPayment.add(getJButtonHistory());
			jPanelButtonsPayment.add(getJButtonAddRefund());
			if (GeneralData.RECEIPTPRINTER) {
                jPanelButtonsPayment.add(getJButtonPrintBill());
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
				loadDataset();
				checkBill();
				boolean isFullyPaid = balance.doubleValue() == 0;
				if (!isFullyPaid) {
					Patient currentPatient = thisBill.getBillPatient();
					if (currentPatient == null || currentPatient.getCode() == 0) {
						MessageDialog.error(this, "angal.newbill.pleaseselectapatient.msg");
						return;
					}

					boolean hasPartner = false;
					boolean hasGuarantor = false;
					boolean partnersEnabled = GeneralData.PARTNERSMODULEENABLED;
					boolean guarantorEnabled = hasBillGuarantor();

					if (partnersEnabled) {
						try {
							PartnerBrowserManager partnerBrowserManager = Context.getApplicationContext().getBean(PartnerBrowserManager.class);
							hasPartner = partnerBrowserManager.patientHasPartners(currentPatient.getCode());
						} catch (OHServiceException e) {
							LOGGER.error("Error checking patient partners for bill save", e);
						}
					}

					if (guarantorEnabled) {
						User guarantor = (User) jComboBoxGuarantor.getSelectedItem();
						hasGuarantor = guarantor != null;
					}

					if (partnersEnabled && guarantorEnabled) {
						if (!hasPartner && !hasGuarantor) {
							MessageDialog.error(this, "angal.newbill.cannot.save.without.partner.or.guarantor.msg");
							return;
						}
					} else if (partnersEnabled) {
						if (!hasPartner) {
							MessageDialog.error(this, "angal.newbill.cannot.save.without.partner.msg");
							return;
						}
					} else if (guarantorEnabled) {
						if (!hasGuarantor) {
							MessageDialog.error(this, "angal.newbill.cannot.save.without.guarantor.msg");
							return;
						}
					}
				}

				if (Objects.equals(wardComboBox.getSelectedItem(), "")) {
					MessageDialog.error(this, "angal.newbill.selectward.msg");
					return;
				}
				if (thisBill.getPriceList() == null) {
					thisBill.setPriceList(lstArray.get(0));
				}
				if (insert) {
					Bill newBill = new Bill(
                        0,
                        thisBill.getDate(),
                        null,
                        true,
                        thisBill.getPriceList(), // List
                        thisBill.getPriceList().getName(), // List name
                        thisBill.isPatient(), // is a Patient?
                        thisBill.getBillPatient(), // Patient
                        thisBill.isPatient() ? thisBill.getBillPatient().getName() : jTextFieldPatient.getText(), // Patient Name
                        paid ? "C" : "O", // CLOSED or OPEN TODO: enumerate bills status
                        total.doubleValue(), // Total
                        balance.doubleValue(),
                        0,// Balance
                        user, // User
                        thisBill.getAdmission(),
                        pbiID != 0 ? thisBill.getBillPatient().getReductionPlan() : null,
                        (Ward) wardComboBox.getSelectedItem()
					);

					if (hasBillGuarantor()) {
						User guarantor = (User) jComboBoxGuarantor.getSelectedItem();
						if (balance.doubleValue() != 0 && guarantor == null) {
							boolean hasPartnerForNewBill = false;
							if (GeneralData.PARTNERSMODULEENABLED) {
								try {
									PartnerBrowserManager partnerBrowserManager = Context.getApplicationContext().getBean(PartnerBrowserManager.class);
									hasPartnerForNewBill = partnerBrowserManager.patientHasPartners(thisBill.getBillPatient().getCode());
								} catch (OHServiceException e) {
									LOGGER.error("Error checking patient partners", e);
								}
							}

							if (!hasPartnerForNewBill) {
								MessageDialog.error(this, "angal.newbill.selectguarantor.msg");
								return;
							}
						} else if (guarantor != null) {
							newBill.setGuarantor(guarantor);
						}
					}
					if (balance.doubleValue() == 0 && !paid) {
						int result = MessageDialog.yesNo(this, "angal.newbill.billsave.msg");
						newBill.setStatus(result == JOptionPane.YES_OPTION ? "C" : newBill.getStatus());
					}
					try {
						billBrowserManager.newBill(newBill, billItems, payItems);
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
                        today, // updateDate from most recent payment, will be set later
                        true, // is a PriceList? always true, non-pricelist not managed
                        thisBill.getPriceList(), // List
                        thisBill.getPriceList().getName(), // List name
                        thisBill.isPatient(), // is a Patient?
                        thisBill.getBillPatient(), // Patient
                        thisBill.isPatient() ? thisBill.getPatName() : jTextFieldPatient.getText(), // Patient Name
                        paid ? "C" : "O", // CLOSED or OPEN
                        total.doubleValue(), // Total
                        balance.doubleValue(), // Balance
						thisBill.getLock(),
                        user, // User
                        thisBill.getAdmission(),
                        pbiID != 0 ? thisBill.getBillPatient().getReductionPlan() : null,
                        (Ward) wardComboBox.getSelectedItem()
                    ); // Admission

                   if (hasBillGuarantor()) {
						User guarantor = (User) jComboBoxGuarantor.getSelectedItem();
						if (balance.doubleValue() != 0 && guarantor == null) {
							boolean hasPartnerForUpdate = false;
							if (GeneralData.PARTNERSMODULEENABLED) {
								try {
									PartnerBrowserManager partnerBrowserManager = Context.getApplicationContext().getBean(PartnerBrowserManager.class);
									hasPartnerForUpdate = partnerBrowserManager.patientHasPartners(thisBill.getBillPatient().getCode());
								} catch (OHServiceException e) {
									LOGGER.error("Error checking patient partners", e);
								}
							}

							if (!hasPartnerForUpdate) {
								MessageDialog.error(this, "angal.newbill.selectguarantor.msg");
								return;
							}
						} else if (guarantor != null) {
							updateBill.setGuarantor(guarantor);
						}
					}

					try {
						billBrowserManager.updateBill(updateBill, billItems, payItems);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex, this);
						return;
					}
					fireBillInserted(updateBill);
				}
				if (hasNewPayments()) {
					TxtPrinter.initialize();
					new GenericReportBill(thisBill.getId(), GeneralData.PATIENTBILL, false, !TxtPrinter.PRINT_WITHOUT_ASK);
				}
				if (paid && GeneralData.RECEIPTPRINTER) {
					TxtPrinter.initialize();
					if (TxtPrinter.PRINT_AS_PAID) {
						new GenericReportBill(thisBill.getId(), GeneralData.PATIENTBILL, false, !TxtPrinter.PRINT_WITHOUT_ASK);
					}
				}
				RememberDates.setLastBillDate(thisBill.getDate());
				dispose();
			});
		}
		return jButtonSave;
	}

	private boolean hasNewPayments() {
		return (insert && !payItems.isEmpty()) || (payItems.size() - payItemsSaved) > 0;
	}

    private JButton getJButtonPrintBill() {
        if (jButtonPrintBill == null) {
            jButtonPrintBill = new JButton(MessageBundle.getMessage("angal.billbrowser.receipt.btn"));
            jButtonPrintBill.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.receipt.btn.key"));
            jButtonPrintBill.setMaximumSize(BUTTON_PAYMENT_SIZE);
            jButtonPrintBill.setHorizontalAlignment(SwingConstants.LEFT);
            jButtonPrintBill.setIcon(new ImageIcon("rsc/icons/receipt_button.png"));
            jButtonPrintBill.addActionListener(actionEvent -> {
                TxtPrinter.initialize();

                if (thisBill.getStatus().equals("O") && GeneralData.ALLOWPRINTOPENEDBILL) {
                    new GenericReportBill(thisBill.getId(), GeneralData.PATIENTBILL, false, true);
                } else if (thisBill.getStatus().equals("C") ) {
                    new GenericReportBill(thisBill.getId(), GeneralData.PATIENTBILL, false, true);
                } else {
                    MessageDialog.error(this, "angal.billbrowser.thebillisstillopen.msg");
                    return;
                }
            });
        }
        return jButtonPrintBill;
    }

    private void setJButtonPrintBill() {
        if (insert) {
            jButtonPrintBill.setEnabled(false);
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
		LocalDateTime now = TimeTools.getNow().withSecond(0).withNano(0);

		LocalDateTime billDate = thisBill.getDate().withSecond(0).withNano(0);
		LocalDateTime paymentDate = datePay.withSecond(0).withNano(0);

		LocalDateTime lastPay = payItems.isEmpty()
				? billDate
				: payItems.get(payItems.size() - 1).getDate().withSecond(0).withNano(0);

		if (paymentDate.isBefore(billDate)) {
			MessageDialog.error(this, "angal.newbill.paymentmadebeforebilldate.msg");
			return false;
		} else if (paymentDate.isBefore(lastPay)) {
			MessageDialog.error(this, "angal.newbill.thedateisbeforethelastpayment.msg");
			return false;
		} else if (paymentDate.isAfter(now)) {
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

				if (balance.compareTo(BigDecimal.ZERO) == 0) {
					MessageDialog.error(this, "angal.newbill.balance.zero.msg");
					return;
				}

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");

				BigDecimal defaultAmount = balance;

				String quantity = (String) JOptionPane.showInputDialog(
						this,
						MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
						MessageBundle.getMessage("angal.common.quantity.txt"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						null,
						defaultAmount.toString()
				);

				if (quantity == null) {
					return;
				}

				BigDecimal amount;
				try {
					amount = new BigDecimal(quantity);
					if (amount.compareTo(BigDecimal.ZERO) <= 0) {
						MessageDialog.error(this, "angal.newbill.amount.greater.than.zero.msg");
						return;
					}
					if (amount.compareTo(balance) > 0) {
						MessageDialog.info(this,
								MessageBundle.formatMessage("angal.newbill.amounttorefund.msg",
										 (amount.intValue() - balance.intValue())));
						amount = balance;
					}
				} catch (Exception e) {
					MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
					return;
				}

				GoodDateTimeSpinnerChooser datePayChooser =
						new GoodDateTimeSpinnerChooser(TimeTools.getNow());

				int r = JOptionPane.showConfirmDialog(
						this,
						datePayChooser,
						MessageBundle.getMessage("angal.newbill.dateofpayment.title"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE
				);

				if (r != JOptionPane.OK_OPTION) return;

				LocalDateTime datePay = datePayChooser.getLocalDateTime();

				if (!isValidPaymentDate(datePay)) return;

				addPayment(datePay, amount.doubleValue());
			});
		}
		return jButtonAddPayment;
	}

	private JButton getJButtonHistory() {
		if (jButtonHistory == null) {
			jButtonHistory = new JButton(MessageBundle.getMessage("angal.billbrowser.billhistory"));
			jButtonHistory.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.billhistory.key"));
			jButtonHistory.setMaximumSize(BUTTON_PAYMENT_SIZE);
			jButtonHistory.setHorizontalAlignment(SwingConstants.LEFT);

			ImageIcon icon = new ImageIcon("rsc/icons/historique.png");
			Image image = icon.getImage();
			int width = 18;
			int height = 18;
			Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			ImageIcon resizedIcon = new ImageIcon(resizedImage);
			jButtonHistory.setIcon(resizedIcon);

			jButtonHistory.addActionListener(actionEvent -> {
				if (thisBill != null && thisBill.getId() > 0) {
					try {
						BillHistory billHistory = new BillHistory(this, thisBill);
						billHistory.setVisible(true);
					} catch (OHServiceException ex) {
						LOGGER.error("Error opening bill history", ex);
						MessageDialog.error(this, "angal.common.error.msg", ex.getMessage());
					}
				} else {
					MessageDialog.info(this, "angal.billbrowser.newinvoicehistorymessage.msg");
				}
			});
		}
		return jButtonHistory;
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

				boolean isPrice = true;

				Map<Integer, PricesOthers> othersHashMap = new HashMap<>();
				for (PricesOthers other : othPrices) {
					othersHashMap.put(other.getId(), other);
				}

                List<Price> otherPrices = new ArrayList<>();
                for (Price price : prcListArray) {
                    if (price.getGroup().equals("OTH")) {
                        otherPrices.add(price);
                    }
                }

                Icon icon = new ImageIcon("rsc/icons/plus_dialog.png");
                OhTableModel<Price> model = new OhTableModel<>(otherPrices);

                Price oth = BillItemPicker.showPicker(
                        this,
                        MessageBundle.getMessage("angal.newbill.item.title"),
                        model,
                        icon
                );

                if (pbiID != 0 && oth != null) {
                    try {
                        oth = reductionPlanManager.getOtherPrice(oth, pbiID);
                    } catch (OHServiceException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (oth != null) {

                    if (othersHashMap.get(Integer.valueOf(oth.getItem())).isUndefined()) {

                        icon = new ImageIcon("rsc/icons/money_dialog.png");

                        String price = (String) JOptionPane.showInputDialog(
                                this,
                                MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
                                MessageBundle.getMessage("angal.common.undefined.txt"),
                                JOptionPane.PLAIN_MESSAGE,
                                icon,
                                null,
                                "0"
                        );

                        try {
                            if (price == null) return;

                            double amount = Double.parseDouble(price);
                            oth.setPrice(amount);
                            isPrice = false;

                        } catch (Exception eee) {
                            MessageDialog.error(this,
                                    "angal.newbill.invalidpricepleasetryagain.msg");
                            return;
                        }
                    }

                    if (othersHashMap.get(Integer.valueOf(oth.getItem())).isDischarge()) {
                        double amount = oth.getPrice();
                        oth.setPrice(-amount);
                    }

                    if (othersHashMap.get(Integer.valueOf(oth.getItem())).isDaily()) {

                        int qty = 1;
                        icon = new ImageIcon("rsc/icons/calendar_dialog.png");

                        String quantity = (String) JOptionPane.showInputDialog(
                                this,
                                MessageBundle.getMessage("angal.newbill.howmanydays.txt"),
                                MessageBundle.getMessage("angal.newbill.days.title"),
                                JOptionPane.PLAIN_MESSAGE,
                                icon,
                                null,
                                qty
                        );

                        try {
                            if (quantity == null || quantity.isEmpty()) return;

                            qty = Integer.parseInt(quantity);
                            addItem(oth, qty, isPrice);

                        } catch (Exception eee) {
                            MessageDialog.error(this,
                                    "angal.newbill.invalidquantitypleasetryagain.msg");
                        }

                    } else {
                        addItem(oth, 1, isPrice);
                    }
                }
            });
        }
		return jButtonAddOther;
	}

			private JButton getJButtonAddExam() {
				if (jButtonAddExam == null) {
					jButtonAddExam = new JButton(MessageBundle.getMessage("angal.newbill.exam.btn"));
					jButtonAddExam.setMnemonic(MessageBundle.getMnemonic("angal.newbill.exam.btn.key"));
					jButtonAddExam.setMaximumSize(BUTTON_ITEM_SIZE);
					jButtonAddExam.setHorizontalAlignment(SwingConstants.LEFT);
					jButtonAddExam.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
					jButtonAddExam.addActionListener(actionEvent -> {

                List<Price> exams = prcListArray.stream()
                    .filter(p -> "EXA".equals(p.getGroup()))
                    .toList();

                OhTableModel<Price> model = new OhTableModel<>(exams);

                Icon icon = new ImageIcon("rsc/icons/exam_dialog.png");

                Price exam = BillItemPicker.showPicker(
                    this,
                    MessageBundle.getMessage("angal.newbill.exam.title"),
                    model,
                    icon
                );

                if (pbiID != 0 && exam != null) {
                    try {
                        exam = reductionPlanManager.getExamPrice(exam, pbiID);
                    } catch (OHServiceException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (exam != null) {
                    int qty = 1;
                    String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
                            MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, icon, null, qty);

                    try {
                        if (quantity == null || quantity.isEmpty()) {
                            return;
                        }
                        qty = Integer.parseInt(quantity);
                        addItem(exam, qty, true);
                    } catch (Exception eee) {
                        MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
                    }
                }
			});
		}
		return jButtonAddExam;
	}

	private JButton getJButtonAddOperation() {
		if (jButtonAddOperation == null) {
			jButtonAddOperation = new JButton(MessageBundle.getMessage("angal.newbill.operation.btn"));
			jButtonAddOperation.setMnemonic(MessageBundle.getMnemonic("angal.newbill.operation.btn.key"));
			jButtonAddOperation.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddOperation.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddOperation.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddOperation.addActionListener(actionEvent -> {
                List<Price> operations = prcListArray.stream()
                    .filter(p -> "OPE".equals(p.getGroup()))
                    .toList();

                OhTableModel<Price> model = new OhTableModel<>(operations);

                Icon icon = new ImageIcon("rsc/icons/operation_dialog.png"); //$NON-NLS-1$

                Price operation = BillItemPicker.showPicker(
                    this,
                    MessageBundle.getMessage("angal.newbill.operation.title"),
                    model,
                    icon
                );

                if (pbiID != 0 && operation != null) {
                    try {
                        operation = reductionPlanManager.getOperationPrice(operation, pbiID);
                    } catch (OHServiceException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (operation != null) {
                    addItem(operation, 1, true);
                }
			});
		}
		return jButtonAddOperation;
	}

	private JButton getJButtonAddMedical() {
		if (jButtonAddMedical == null) {
			jButtonAddMedical = new JButton(MessageBundle.getMessage("angal.newbill.medical.btn"));
			jButtonAddMedical.setMnemonic(MessageBundle.getMnemonic("angal.newbill.medical.btn"));
			jButtonAddMedical.setMaximumSize(BUTTON_ITEM_SIZE);
			jButtonAddMedical.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddMedical.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
            jButtonAddMedical.addActionListener(e -> {

                if (selectedPatient == null) {
                    JOptionPane.showMessageDialog(
                            PatientBillEdit.this,
                            MessageBundle.getMessage("angal.newbill.pleaseselectapatient")
                    );
                    return;
                }

                Ward selectedWard = null;
                String wardCode = "";

                Object selected = wardComboBox.getSelectedItem();
                if (selected instanceof Ward ward) {
                    selectedWard = ward;
                    wardCode = ward.getCode();
                }

                if (wardCode == null || wardCode.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            PatientBillEdit.this,
                            MessageBundle.getMessage("angal.newbill.pleaseselectaward")
                    );
                    return;
                }

                List<Price> medicals = prcListArray.stream()
                        .filter(p -> "MED".equals(p.getGroup()))
                        .toList();

                List<Price> medArray = new ArrayList<>();
                List<MedicalWard> medWardArray = new ArrayList<>();

                for (Price price : medicals) {

                    medArray.add(price); // always add for display

                    if (GeneralData.STOCKMVTONBILLSAVE) {

                        for (MedicalWard mw : medWardList) {

                            if (mw.getMedical().getDescription().equals(price.getDesc())) {
                                medWardArray.add(mw);
                                break;
                            }
                        }
                    }
                }

                Icon icon = new ImageIcon("rsc/icons/medical_dialog.png");

                OhTableModel<Price> model = new OhTableModel<>(medArray, true);

                Price medical = BillItemPicker.showPicker(
                    this,
                    MessageBundle.getMessage("angal.newbill.medical.title"),
                    model,
                    icon
                );

                if (medical == null) return;

                if (GeneralData.STOCKMVTONBILLSAVE) {

                    final String medicalDesc = medical.getDesc();

                    boolean foundInWard = medWardArray.stream()
                            .anyMatch(mw -> mw.getMedical().getDescription().equals(medicalDesc));
                    if (!foundInWard) {
                        JOptionPane.showMessageDialog(
                                PatientBillEdit.this,
                                MessageBundle.getMessage("angal.newbill.stocknotavailableforitem")
                        );
                        return;
                    }
                }

                if (pbiID != 0) {
                    try {
                        medical = reductionPlanManager.getMedicalPrice(medical, pbiID);
                    } catch (OHServiceException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                int qty = 1;
                String quantity = (String) JOptionPane.showInputDialog(
                        this,
                        MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
                        MessageBundle.getMessage("angal.common.quantity.txt"),
                        JOptionPane.PLAIN_MESSAGE,
                        icon,
                        null,
                        qty
                );

                try {
                    if (quantity == null || quantity.isEmpty()) return;

                    qty = Integer.parseInt(quantity);

                    addItem(medical, qty, true);

                } catch (Exception eee) {
                    MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
                }
            });
		}
		return jButtonAddMedical;
	}

    private JButton getJButtonAddGroup() {
        if (JButtonAddGroup == null) {
            JButtonAddGroup = new JButton();
            JButtonAddGroup.setText(MessageBundle.getMessage("angal.newbill.additemgroup.btn"));
            JButtonAddGroup.setMnemonic(MessageBundle.getMnemonic("angal.newbill.additemgroup.btn.key"));
            JButtonAddGroup.setMaximumSize(BUTTON_ITEM_SIZE);
            JButtonAddGroup.setHorizontalAlignment(SwingConstants.LEFT);
            JButtonAddGroup.setIcon(new ImageIcon("rsc/icons/plus_button.png"));

            JButtonAddGroup.addActionListener(e -> {
                BillItemGroupBrowser billItemGroupBrowser = new BillItemGroupBrowser(this, true);
                billItemGroupBrowser.addListener(this);
                billItemGroupBrowser.setVisible(true);
            });
        }

        return JButtonAddGroup;
    }

    private JComboBox<Object> getWardComboBox() {
        WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
        MovWardBrowserManager manager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
        Ward ward;

        try {
            ward = thisBill.getAdmission() != null ? thisBill.getAdmission().getWard() : wardBrowserManager.findWard("OPD");
        } catch (OHServiceException e) {
            throw new RuntimeException(e);
        }

        if (wardComboBox == null) {
            wardComboBox = new JComboBox<Object>();
            wardComboBox.setPreferredSize(new Dimension(130, 25));
            try {
                medWardList = manager.getMedicalsWard(ward.getCode(), true);
            } catch (OHServiceException e) {
                throw new RuntimeException(e);
            }
            try {
              ward = thisBill.getAdmission() != null ? thisBill.getAdmission().getWard() : wardBrowserManager.findWard("OPD");
            } catch (OHServiceException e) {
                throw new RuntimeException(e);
            }

			if (!insert && this.thisBill.getWard() != null) {
				ward = this.thisBill.getWard();
			}

            List<Ward> wardList = null;
            try {
                wardList = wardBrowserManager.getWards();
            } catch (OHServiceException e) {
                throw new RuntimeException(e);
            }
            boolean trouve = false;
            if(!trouve){
                wardComboBox.addItem("");
            }

            for (Ward elem : wardList) {
                if (elem.isPharmacy())
					wardComboBox.addItem(elem);
                if(insert && elem.getDescription().equalsIgnoreCase("PHARMACIE"))

                if(thisBill.getWard() != null && this.thisBill.getWard().getCode().equals(elem.getCode()))
                    wardComboBox.setSelectedItem(elem);
            }

            wardComboBox.setEnabled(true);
            if (!insert && ward!= null && !ward.getCode().trim().isEmpty()) {
                wardComboBox.setEnabled(false);
            }

        }

        wardComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object item = e.getItem();
                    Ward ward = (Ward) item;

                    try {
                        medWardList = manager.getMedicalsWard(ward.getCode(), true);
                    } catch (OHServiceException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
		if (ward != null) {
			wardComboBox.setSelectedItem(ward);
		}
        return wardComboBox;
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
                int qty = 1;
				Icon icon = new ImageIcon("rsc/icons/custom_dialog.png"); //$NON-NLS-1$
				String desc = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.chooseadescription.txt"),
								MessageBundle.getMessage("angal.newbill.customitem.title"), JOptionPane.PLAIN_MESSAGE, icon, null,
								MessageBundle.getMessage("angal.newbill.newdescription.txt"));
				if (desc == null || desc.equals("")) { //$NON-NLS-1$
					return;
				} else {
                    String quantity = (String) JOptionPane.showInputDialog(
                            this,
                            MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
                            MessageBundle.getMessage("angal.common.quantity.txt"),
                            JOptionPane.PLAIN_MESSAGE,
                            icon,
                            null,
                            qty
                    );

					icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
					String price = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
									MessageBundle.getMessage("angal.newbill.customitem.title"), JOptionPane.PLAIN_MESSAGE, icon, null, "0");

                    //$NON-NLS-2$
					try {
                        qty = Integer.parseInt(quantity);
						amount = Double.parseDouble(price);
					} catch (Exception eee) {
						MessageDialog.error(this, "angal.newbill.invalidpricepleasetryagain.msg");
						return;
					}
				}

				try {
					BillItems newItem = new BillItems(0, billBrowserManager.getBill(thisBill.getId()), false, "", //$NON-NLS-1$
									desc, amount, qty);
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

    private JButton getJButtonRemoveAllItem() {
        if (jButtonRemoveAllItem == null) {
            jButtonRemoveAllItem = new JButton(MessageBundle.getMessage("angal.newbill.removeallitem.btn"));
            jButtonRemoveAllItem.setMnemonic(MessageBundle.getMnemonic("angal.newbill.removeallitem.btn.key"));
            jButtonRemoveAllItem.setMaximumSize(BUTTON_ITEM_SIZE);
            jButtonRemoveAllItem.setHorizontalAlignment(SwingConstants.LEFT);
            jButtonRemoveAllItem.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
            jButtonRemoveAllItem.addActionListener(actionEvent -> {
                if (billItems.isEmpty()) {
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        MessageBundle.getMessage("angal.newbill.removeallitems.msg"),
                        MessageBundle.getMessage("angal.newbill.confirm.msg"),
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                billItems.clear();
                updateTotals();
                updateGUI();
            });
        }

        return jButtonRemoveAllItem;
    }

	private void loadFields() {

        if (selectedBillItem != null) {
            searchTextField.setText(selectedBillItem.getItemDisplayCode());
            descriptionTextField.setText(selectedBillItem.getItemDescription());
            quantityTextField.setText(String.valueOf(selectedBillItem.getItemQuantity()));
            priceTextField.setText(String.valueOf(selectedBillItem.getItemAmount()));
            quantityTextField.grabFocus();
            priceTextField.setEnabled(false);

            quantityTextField.grabFocus();
        } else {
            searchTextField.setText("");
            descriptionTextField.setText("");
            quantityTextField.setText("");
            searchTextField.setText("");
            searchTextField.setEnabled(true);
            searchTextField.grabFocus();

            quantityTextField.setEnabled(true);

        }
    }

	private BillItems addItem(Price prc, int qty, boolean isPrice) {
        BillItems item = null;
        if (prc != null) {
			double amount = prc.getPrice();
			try {
                item = new BillItems(0, billBrowserManager.getBill(thisBill.getId()), isPrice, prc.getGroup() + prc.getItem(), prc.getDesc(), amount, qty);
				item.setItemGroup(prc.getGroup());
				item.setPriceID(prc.getItem());
				billItems.add(item);
                modified = true;
                updateTotals();
                updateGUI();

                return item;
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
			}

		}

        return  item;
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
		if (row != -1) {
			billItems.remove(row);
            selectedBillItem = null;
			updateTotals();
			updateGUI();
		} else {
			MessageDialog.error(null, "angal.newbill.youcannotdeletealreadysaveditems.msg");
		}
	}

	private JButton getJButtonAddPrescription() {
        if (jButtonAddPrescription == null) {
            jButtonAddPrescription = new JButton(MessageBundle.getMessage("angal.newbill.prescription.btn"));
            jButtonAddPrescription.setMaximumSize(BUTTON_ITEM_SIZE);
            jButtonAddPrescription.setHorizontalAlignment(SwingConstants.LEFT);
            jButtonAddPrescription.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
            jButtonAddPrescription.addActionListener(actionEvent -> {

                if (thisBill.getBillPatient() == null || thisBill.getBillPatient().getCode() == 0) {
                    MessageDialog.error(this, "angal.patvac.pleaseselectapatient.msg");
                    return;
                }

                try {
                    if (!billBrowserManager.hasPrescription(thisBill.getBillPatient().getCode())) {
                        MessageDialog.info(this, "angal.newbill.noprescriptionforthispatient.msg");
                        return;
                    }
                } catch (OHServiceException e) {
                    MessageDialog.showExceptions(e);
                    return;
                }

                try {
                    SelectPrescriptions selectPrescriptions = new SelectPrescriptions(this, thisBill.getBillPatient());
                    selectPrescriptions.addPrescriptionSelectedListener(prescriptions -> {
						List<String> alreadyPaidItems = new ArrayList<>();
                        for (BillItems item : prescriptions) {
							boolean alreadyPaid = false;
							try {
								alreadyPaid = billBrowserManager.isPrescriptionAlreadyBilledAndPaid(
										thisBill.getBillPatient().getCode(),
										item.getPrescriptionId(),
										item.getItemGroup()
								);
							} catch (OHServiceException ex) {
							}

							if (alreadyPaid) {
								alreadyPaidItems.add(item.getItemDescription());
								continue;
							}

							boolean itemExists = billItems.stream().anyMatch(bi ->
									bi.getPrescriptionId() != null
											&& bi.getPrescriptionId().equals(item.getPrescriptionId())
											&& bi.getItemGroup() != null
											&& bi.getItemGroup().equals(item.getItemGroup())
							);

							if (!itemExists) {
								try {
									Price price = getPrice(item.getPriceID());

									if (price == null) {
										MessageDialog.warning(PatientBillEdit.this,
												MessageBundle.formatMessage("angal.newbill.pricenotfoundforitem.fmt.msg",
														item.getItemDescription()));
										continue;
									}

                                    if ("MED".equals(item.getItemGroup())) {

                                        MedicalWard medicalWard = getMedicalWard(price);

                                        if (medicalWard == null) {

                                            MessageDialog.error(
                                                    PatientBillEdit.this,
                                                    MessageBundle.getMessage("angal.newbill.stocknotavailableforitem")
                                                            + ": " + item.getItemDescription()
                                            );

                                            continue;
                                        }

                                        BillItems validatedItem =
                                                addMedical(medicalWard, item.getItemQuantity());

                                        if (validatedItem != null) {

                                            validatedItem.setPrescriptionId(item.getPrescriptionId());

                                            validatedItem.setItemAmount(item.getItemAmount());

                                            validatedItem.setItemAmountBrut(item.getItemAmountBrut());

                                            validatedItem.setPrice(true);

                                            validatedItem.setItemGroup(item.getItemGroup());

                                            validatedItem.setItemDisplayCode(
                                                    medicalWard.getMedical().getProdCode()
                                            );

                                            modified = true;
                                        }

                                    } else {

                                        BillItems validatedItem = buildBillItem(
                                                price,
                                                item.getItemQuantity(),
                                                item.getItemDescription()
                                        );

                                        if (validatedItem != null) {

                                            validatedItem.setPrescriptionId(item.getPrescriptionId());

                                            validatedItem.setItemGroup(item.getItemGroup());

                                            validatedItem.setItemAmount(item.getItemAmount());

                                            validatedItem.setItemAmountBrut(item.getItemAmountBrut());

                                            validatedItem.setPrice(true);

                                            billItems.add(validatedItem);

                                            modified = true;
                                        }
                                    }
								} catch (OHServiceException ex) {
									MessageDialog.error(PatientBillEdit.this, ex.getMessage());
								}
							} else {
								MessageDialog.warning(PatientBillEdit.this,
										MessageBundle.formatMessage("angal.newbill.prescriptionalreadyadded.fmt.msg",
												item.getItemDescription()));
							}
                        }

						if (!alreadyPaidItems.isEmpty()) {
							MessageDialog.warning(PatientBillEdit.this,
									MessageBundle.formatMessage(
											"angal.newbill.prescriptionsalreadypaid.fmt.msg",
											String.join(", ", alreadyPaidItems)));
						}

						updatePrescriptionButtonVisibility();
                        updateTotals();
                        updateGUI();
                    });
                    selectPrescriptions.setVisible(true);
                } catch (Exception ex) {
                    MessageDialog.error(this, MessageBundle.getMessage("angal.common.error.msg"));
                    LOGGER.error("Error creating SelectPrescriptions", ex);
                }
            });
            jButtonAddPrescription.setVisible(false);
        }
        return jButtonAddPrescription;
    }

    private void updatePrescriptionButtonVisibility() {
        if (jButtonAddPrescription != null) {
            if (thisBill.getBillPatient() != null && thisBill.getBillPatient().getCode() != 0) {
                try {
                    boolean hasPrescriptions = billBrowserManager.hasPrescription(thisBill.getBillPatient().getCode());
                    jButtonAddPrescription.setVisible(hasPrescriptions);
                } catch (OHServiceException e) {
                    jButtonAddPrescription.setVisible(false);
                }
            } else {
                jButtonAddPrescription.setVisible(false);
            }
        }
    }

    private BillItems addOtherPrice(Price oth, int qty) {
        if (qty <= 0) {
            qty = 1;
        }

        try {
            BillItems billItem = buildBillItem(oth, qty, oth.getDesc());
            if (billItem != null) {
				billItem.setItemGroup(oth.getGroup());
                billItems.add(billItem);
                modified = true;
                updateTotals();
                updateGUI();
                return billItem;
            }
        } catch (OHServiceException ex) {
            MessageDialog.error(this, ex.getMessage());
        }

        return null;
    }

    private BillItems addExamAndOperation(Price price) {
        try {
            BillItems billItem = buildBillItem(price, 1, price.getDesc());
            if (billItem != null) {
                billItems.add(billItem);
                modified = true;
                updateTotals();
                updateGUI();
                return billItem;
            }
        } catch (OHServiceException ex) {
            MessageDialog.error(this, ex.getMessage());
        }

        return null;
    }

    private BillItems addMedical(MedicalWard med, int qty) {
        Price price = getPrice(med);

        if (price != null) {
            try {
                BillItems billItem = buildBillItem(price, qty, price.getDesc());
                if (billItem != null) {
                    billItems.add(billItem);
                    modified = true;
                    updateTotals();
                    updateGUI();
                    return billItem;
                }
            } catch (OHServiceException ex) {
                if (ex.getMessage().contains("qtynotinstock")) {
                    JOptionPane.showMessageDialog(PatientBillEdit.this,
                            MessageBundle.getMessage("angal.newbill.qtynotinstock"),
                            MessageBundle.getMessage("angal.newbill.invalidquantity"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    MessageDialog.error(this, ex.getMessage());
                }
            }
        }

        return null;
    }

    private Price getPrice(MedicalWard med) {

       if (med != null) {
           for (Price price : prcListArray) {
               if (price != null
                       && "MED".equals(price.getGroup())
                       && Objects.equals(price.getItem(), String.valueOf(med.getMedical().getCode()))) {
                   return price;
               }
           }
       }
        return null;
    }

    private Price getPrice(String itemCode, String group) {

        for (Price price : prcListArray) {
            if (price.getGroup().equals(group)) {
                if (price.getItem().equals(itemCode)) {
                    return price;
                }
            }
        }
        return null;
    }

    private Price getPrice(PricesOthers oth) {

        for (Price price : prcListArray) {
            if (price.getGroup().equals("OTH")) {
                if (price.getItem().equals(String.valueOf(oth.getId()))) {
                    return price;
                }
            }
        }
        return null;
    }

    private MedicalWard getMedicalWard(Price price) {
        if (price == null) {
            return null;
        }

        if ("MED".equals(price.getGroup()) && GeneralData.STOCKMVTONBILLSAVE && medWardList != null && medWardList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.stocknotavailableforitem") + (price != null ? price.getDesc() : "")
            );
            return null;
        }

        if (medWardList == null || medWardList.isEmpty()) {
            return null;
        }
        
        for (MedicalWard medicalWard : medWardList) {
            if (medicalWard != null && medicalWard.getMedical() != null && medicalWard.getMedical().getDescription() != null &&
                medicalWard.getMedical().getDescription().equals(price.getDesc())) {
                return medicalWard;
            }
        }

        return null;
    }

    private boolean containPrice(Price price, double qty) {
        if (price == null || medWardList == null || medWardList.isEmpty()) return false;

        for (MedicalWard medicalWard : medWardList) {
            if (medicalWard == null || medicalWard.getMedical() == null || medicalWard.getMedical().getDescription() == null) {
                continue;
            }
            String desc = medicalWard.getMedical().getDescription();
            if (desc.equals(price.getDesc()) && medicalWard.getQty() >= qty) {
                return true;
            }
        }
        return false;
    }

    private void searchItem() {

        String searchValue = searchTextField.getText();

        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(
                PatientBillEdit.this,
                MessageBundle.getMessage("angal.newbill.pleaseselectapatient")
            );
            return;
        }

        boolean stockMove = GeneralData.STOCKMVTONBILLSAVE;

        List<Object> itemArray = new ArrayList<>();

        for (Price price : prcListArray) {

            if (!price.getGroup().equals("MED")){

                if (price.getGroup().equals("OTH")) {
                    try {
                        othPrices = pricesOthersManager.getOthers();
                    } catch (OHServiceException e) {
                        throw new RuntimeException(e);
                    }

                    PricesOthers pricesOther = null;
                    if (othPrices != null) {
                        pricesOther = othPrices.stream()
                                .filter(p -> p.getId() == Integer.parseInt(price.getItem()))
                                .findFirst()
                                .orElse(null);
                    }

                    itemArray.add(pricesOther);
                } else {
                    itemArray.add(price);
                }
            }
        }

        // Ward selection
        Ward selectedWard = null;
        String wardCode = "";

        Object selected = wardComboBox.getSelectedItem();

        if (selected instanceof Ward) {
            selectedWard = (Ward) selected;
            wardCode = selectedWard.getCode();
        }

        if ((wardCode == null || wardCode.isEmpty()) && stockMove) {
            JOptionPane.showMessageDialog(
                    PatientBillEdit.this,
                    MessageBundle.getMessage("angal.newbill.pleaseselectaward")
            );
            return;
        }

        for (Price price : prcListArray) {

            if (price.getGroup().equals("MED")) {
                itemArray.add(price);
            }
        }

        OhTableModel<Object> modelOh = new OhTableModel<>(itemArray, true);

        Object selectedItem = null;
        try {
            selectedItem = modelOh.filter(searchValue);
        } catch (OHException e) {
            throw new RuntimeException(e);
        }

        if (selectedItem == null) {

           selectedItem = BillItemPicker.showPicker(
                    this,
                    MessageBundle.getMessage("angal.newbill.item.title"),
                    modelOh,
                   new ImageIcon("rsc/icons/plus_button.png")
            );
        }

        Price price = null;
        MedicalWard med = null;
        PricesOthers oth = null;

        int qty = 1;
        BillItems billItem = null;

        if (selectedItem instanceof MedicalWard) {
            med = (MedicalWard) selectedItem;

            String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
                    MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, new ImageIcon("rsc/icons/plus_button.png"), null, qty);

            try {
                if (quantity == null || quantity.isEmpty()) {
                    return;
                }
                qty = Integer.parseInt(quantity);
                billItem = addMedical(med, qty);
            } catch (Exception eee) {
                MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
            }

        } else if (selectedItem instanceof Price) {

            price = (Price) selectedItem;

            if (price.getGroup().equals("MED")) {

                med = getMedicalWard(price);

                if (med != null) {
                    String quantity = (String) JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
                            MessageBundle.getMessage("angal.common.quantity.txt"), JOptionPane.PLAIN_MESSAGE, new ImageIcon("rsc/icons/plus_button.png"), null, qty);

                    try {
                        if (quantity == null || quantity.isEmpty()) {
                            return;
                        }
                        qty = Integer.parseInt(quantity);
                        billItem = addMedical(med, qty);
                        billItem.setItemDisplayCode(med.getMedical().getProdCode());
                    } catch (Exception eee) {
                        MessageDialog.error(this, "angal.newbill.invalidquantitypleasetryagain.msg");
                    }
                } else {
                    MessageDialog.error(this, "angal.newbill.stocknotavailableforitem");
                }

            } else if (price.getGroup().equals("EXA") || price.getGroup().equals("OPE")) {
                billItem = addExamAndOperation(price);
            } else if (price.getGroup().equals("OTH")) {
                try {
                    othPrices = pricesOthersManager.getOthers();
                } catch (OHServiceException e) {
                    throw new RuntimeException(e);
                }

                PricesOthers pricesOther = null;
                if (othPrices != null) {
                    Price finalPrice1 = price;
                    pricesOther = othPrices.stream()
                            .filter(p -> p.getId() == Integer.parseInt(finalPrice1.getItem()))
                            .findFirst()
                            .orElse(null);
                }

                billItem = addOtherPrice(price, qty);
                billItem.setItemDisplayCode(pricesOther.getCode());

            }

        } else if (selectedItem instanceof PricesOthers) {
            final PricesOthers othr = (PricesOthers) selectedItem;

            price = prcListArray.stream()
                    .filter(p -> "OTH".equals(p.getGroup()))
                    .filter(p -> p.getItem() != null)
                    .filter(p -> {
                        try {
                            return Integer.parseInt(p.getItem()) == othr.getId();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .orElse(null);

            try {
                othPrices = pricesOthersManager.getOthers();
            } catch (OHServiceException e) {
                throw new RuntimeException(e);
            }

            PricesOthers pricesOther = null;
            if (othPrices != null) {
                Price finalPrice = price;
                if (finalPrice != null) {
                    pricesOther = othPrices.stream()
                        .filter(p -> p.getId() == Integer.parseInt(finalPrice.getItem()))
                        .findFirst()
                        .orElse(null);
                } else {
                    JOptionPane.showMessageDialog(
                        PatientBillEdit.this,
                        MessageBundle.getMessage("angal.newbill.itemhasnullprice")
                    );
                    return;
                }
            }

            price = getPrice(othr);
            billItem = addOtherPrice(price, qty);
            billItem.setItemDisplayCode(pricesOther.getCode());
        }

        loadFields();
        selectedBillItem = billItem;
        quantityTextField.setText(String.valueOf(qty));
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
		public Class<?> getColumnClass(int i) {
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
		public Class<?> getColumnClass(int columnIndex) {
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
				return formatDateTime(payItems.get(r).getDate());
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
		Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class };

		private JTableTotalModel() {
			super(new Object[][] { { "<html><b>" + MessageBundle.getMessage("angal.common.total.txt").toUpperCase() + "</b></html>", currencyCod, total } },
					new String[] { "", "", "" });
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
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

	private final class JTableBalanceModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class };

		private JTableBalanceModel() {
			super(new Object[][] {
							{ "<html><b>" + MessageBundle.getMessage("angal.newbill.balance.txt").toUpperCase() + "</b></html>", currencyCod, balance } },
					new String[] { "", "", "" });
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
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