package org.isf.mortuary.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.layout.SpringUtilities;

public class MortuaryBrowser extends ModalJFrame {

	private JPanel jContainPanel;
	private JPanel jButtonPanel;
	private JButton jNewButton;
	private JButton jEditButton;
	private JButton jDeleteButton;
	private JButton jCertificateButton;
	private JButton jMortuaryStayButton;
	private JButton jRapportButton;
	private JButton jCloseButton;
	private JTextField patientTextfield;
	private JButton pickPatientButton;
	private JButton removePatientButton;
	private JLabel rowCounter;
	private String rowCounterText = MessageBundle.getMessage("angal.common.count.label") + ' ';
	//private List<Mortuary> pSur;
	private int[] pColumnWidth = {50, 80, 100, 130, 70, 150, 30, 30, 195, 195, 50, 50};
	private final JFrame myFrame;
	/**
	 * This method initializes
	 */
	public MortuaryBrowser() {
		super();
		myFrame = this;
		initialize();
		setLocationRelativeTo(null);
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.mortuary.browser.title"));
		this.setContentPane(getJContainPanel());
		this.setMinimumSize(new Dimension(400 + getJTableWidth(), 700));//rowCounter.setText(rowCounterText + pSur.size());
		validate();
	}

	/**
	 * This method initializes containPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jContainPanel.add(getJSelectionPanel(), BorderLayout.WEST);
			validate();
		}
		return jContainPanel;
	}

	private JPanel getJSelectionPanel() {
		JPanel jSelectionPanel = new JPanel(); //the outer panel get maximum height (as per WEST from outer container)
		jSelectionPanel.add(getJSelectionContentPanel()); //the inner panel can use any layout
		return jSelectionPanel;
	}

	private JPanel getJSelectionContentPanel() {
		JPanel jSelectionContentPanel = new JPanel();
		jSelectionContentPanel.add(getSearchPatientPanel());
	//	jSelectionContentPanel.add(getOtherFiltersPanel());
	//	jSelectionContentPanel.add(getButtonsPanel());
		///SpringUtilities.makeCompactGrid(jSelectionContentPanel, 1, 1, 5, 5, 5, 5);
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
}
