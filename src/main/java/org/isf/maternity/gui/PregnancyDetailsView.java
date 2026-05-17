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
package org.isf.maternity.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.NewBornBrowserManager;
import org.isf.maternity.manager.PregnancyDeliveryBrowserManager;
import org.isf.maternity.model.*;
import org.isf.menu.manager.Context;
import org.isf.stat.gui.report.GenericReportPregnancyBirthDeclaration;
import org.isf.stat.gui.report.GenericReportPregnancyCertificateOfDeclaration;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

public class PregnancyDetailsView extends JDialog {

    private static final long serialVersionUID = 1L;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Pregnancy pregnancy;
    private PregnancyDelivery delivery;
    private List<Newborn> newborns;

    private PregnancyDeliveryBrowserManager deliveryManager;
    private NewBornBrowserManager newbornManager;

    private JTabbedPane tabbedPane;

    public PregnancyDetailsView(JFrame parent, Pregnancy pregnancy) {
        super(parent, MessageBundle.getMessage("angal.maternity.pregnancy.details.title") + " - " + pregnancy.getPatient().getFirstName() + " " + pregnancy.getPatient().getSecondName() + " (" + MessageBundle.getMessage("angal.common.code.txt") + ": " + pregnancy.getPatient().getCode() + ")", false);
        this.pregnancy = pregnancy;

        initManagers();
        loadData();
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(750, 600);
        setVisible(true);
    }

    private void initManagers() {
        deliveryManager = Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);
        newbornManager = Context.getApplicationContext().getBean(NewBornBrowserManager.class);
    }

    private void loadData() {
        try {
            delivery = deliveryManager.getDeliveryByPregnancy(pregnancy.getId());
            if (delivery != null) {
                newborns = newbornManager.getNewbornsByDelivery(delivery.getId());
            }
        } catch (OHServiceException e) {
            OHServiceExceptionUtil.showMessages(e);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD));

        tabbedPane.addTab(MessageBundle.getMessage("angal.maternity.pregnancy.tab"), createPregnancyPanel());

        tabbedPane.addTab(MessageBundle.getMessage("angal.maternity.delivery.tab"), createDeliveryPanel());

        tabbedPane.addTab(MessageBundle.getMessage("angal.maternity.newborns.tab"), createNewbornsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        add(createPrintButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createPregnancyPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Patient Information Section
        JPanel patientInfoPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.patient.info.header"));
        patientInfoPanel.setLayout(new GridLayout(2, 2, 5, 3));
        patientInfoPanel.add(createLabel(MessageBundle.getMessage("angal.common.name.txt") + ":"));
        patientInfoPanel.add(createValueLabel(pregnancy.getPatient().getFirstName() + " " + pregnancy.getPatient().getSecondName()));
        patientInfoPanel.add(createLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        patientInfoPanel.add(createValueLabel(String.valueOf(pregnancy.getPatient().getCode())));
        formPanel.add(patientInfoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Pregnancy Information Section
        JPanel pregnancyInfoPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.pregnancy.info.header"));
        pregnancyInfoPanel.setLayout(new GridLayout(7, 2, 5, 3));
        pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.pregnancy.id.col") + ":"));
        pregnancyInfoPanel.add(createValueLabel(String.valueOf(pregnancy.getId())));
        pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.creationdate.col") + ":"));
        pregnancyInfoPanel.add(createValueLabel(pregnancy.getDate() != null ? pregnancy.getDate().format(dateTimeFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.lmp.col") + ":"));
        pregnancyInfoPanel.add(createValueLabel(pregnancy.getLmp() != null ? pregnancy.getLmp().format(dateFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.edd.col") + ":"));
        pregnancyInfoPanel.add(createValueLabel(pregnancy.getEddLmp() != null ? pregnancy.getEddLmp().format(dateFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        if (delivery == null) {
            pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.gestationalage.col") + ":"));
            pregnancyInfoPanel.add(createValueLabel(getGestationalAgeSafely()));
        }
        pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.risklevel.col") + ":"));
        pregnancyInfoPanel.add(createValueLabel(pregnancy.getRiskLevel() != null ? pregnancy.getRiskLevel().getDescription() : MessageBundle.getMessage("angal.common.na.label")));
        pregnancyInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.status.col") + ":"));
        pregnancyInfoPanel.add(createValueLabel(pregnancy.getStatus() != null ? pregnancy.getStatus().getDescription() : MessageBundle.getMessage("angal.common.na.label")));
        formPanel.add(pregnancyInfoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Obstetric History Section
        JPanel obstetricPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.obstetric.history.header"));
        obstetricPanel.setLayout(new GridLayout(3, 2, 5, 3));
        obstetricPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.gravidity.label") + ":"));
        obstetricPanel.add(createValueLabel(pregnancy.getGravidity() != null ? String.valueOf(pregnancy.getGravidity()) : MessageBundle.getMessage("angal.common.na.label")));
        obstetricPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.parity.label") + ":"));
        obstetricPanel.add(createValueLabel(pregnancy.getParity() != null ? String.valueOf(pregnancy.getParity()) : MessageBundle.getMessage("angal.common.na.label")));
        obstetricPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.miscarriages.label") + ":"));
        obstetricPanel.add(createValueLabel(pregnancy.getMiscarriages() != null ? String.valueOf(pregnancy.getMiscarriages()) : MessageBundle.getMessage("angal.common.na.label")));
        formPanel.add(obstetricPanel);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createDeliveryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (delivery == null) {
            JLabel noDataLabel = new JLabel(MessageBundle.getMessage("angal.maternity.no.delivery.recorded"));
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.ITALIC, 12));
            mainPanel.add(noDataLabel, BorderLayout.CENTER);
            return mainPanel;
        }

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Delivery Information Section
        JPanel deliveryInfoPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.delivery.info.header"));
        deliveryInfoPanel.setLayout(new GridLayout(8, 2, 5, 3));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.delivery.id.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(String.valueOf(delivery.getId())));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.delivery.date.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getDeliveryDate() != null ? delivery.getDeliveryDate().format(dateTimeFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.delivery.type.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getDeliveryType() != null ? delivery.getDeliveryType().getDescription() : MessageBundle.getMessage("angal.common.na.label")));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.delivery.mode.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getDeliveryMode() != null ? getDeliveryModeLabel(delivery.getDeliveryMode()) : MessageBundle.getMessage("angal.common.na.label")));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.labor.onset.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getLaborOnsetDateTime() != null ? delivery.getLaborOnsetDateTime().format(dateTimeFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.rupture.membranes.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getRuptureMembranesDateTime() != null ? delivery.getRuptureMembranesDateTime().format(dateTimeFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.anesthesia.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getAnesthesiaUsed() != null ? delivery.getAnesthesiaUsed() : MessageBundle.getMessage("angal.common.na.label")));
        deliveryInfoPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.perineal.integrity.label") + ":"));
        deliveryInfoPanel.add(createValueLabel(delivery.getPerinealIntegrity() != null ? getPerinealIntegrityLabel(delivery.getPerinealIntegrity()) : MessageBundle.getMessage("angal.common.na.label")));
        formPanel.add(deliveryInfoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Delivery Outcomes Section
        JPanel outcomesPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.delivery.outcomes.header"));
        outcomesPanel.setLayout(new GridLayout(4, 2, 5, 3));
        outcomesPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.placenta.complete.label") + ":"));
        outcomesPanel.add(createValueLabel(delivery.isPlacentaComplete() != null ? (delivery.isPlacentaComplete() ? MessageBundle.getMessage("angal.common.yes.label") : MessageBundle.getMessage("angal.common.no.label")) :MessageBundle.getMessage("angal.common.na.label")));
        outcomesPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.placenta.weight.label") + ":"));
        outcomesPanel.add(createValueLabel(delivery.getPlacentaWeight() != null ? delivery.getPlacentaWeight() + " g" : MessageBundle.getMessage("angal.common.na.label")));
        outcomesPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.estimated.blood.loss.label") + ":"));
        outcomesPanel.add(createValueLabel(delivery.getEstimatedBloodLoss() != null ? delivery.getEstimatedBloodLoss() + " ml" : MessageBundle.getMessage("angal.common.na.label")));
        outcomesPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.attending.clinician.label") + ":"));
        outcomesPanel.add(createValueLabel(delivery.getAttendingClinicianId() != null ? delivery.getAttendingClinicianId() : MessageBundle.getMessage("angal.common.na.label")));
        formPanel.add(outcomesPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Father Information Section
        JPanel fatherPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.father.info.header"));
        fatherPanel.setLayout(new GridLayout(5, 2, 5, 3));
        fatherPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.father.name.label") + ":"));
        fatherPanel.add(createValueLabel(delivery.getFatherName() != null ? delivery.getFatherName() : MessageBundle.getMessage("angal.common.na.label")));
        fatherPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.father.age.label") + ":"));
        fatherPanel.add(createValueLabel(delivery.getFatherAge() != null ? String.valueOf(delivery.getFatherAge()) : MessageBundle.getMessage("angal.common.na.label")));
        fatherPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.father.birthplace.label") + ":"));
        fatherPanel.add(createValueLabel(delivery.getFatherBirthplace() != null ? delivery.getFatherBirthplace() : MessageBundle.getMessage("angal.common.na.label")));
        fatherPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.father.address.label") + ":"));
        fatherPanel.add(createValueLabel(delivery.getFatherAddress() != null ? delivery.getFatherAddress() : MessageBundle.getMessage("angal.common.na.label")));
        fatherPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.father.profession.label") + ":"));
        fatherPanel.add(createValueLabel(delivery.getFatherProfession() != null ? delivery.getFatherProfession() : MessageBundle.getMessage("angal.common.na.label")));
        formPanel.add(fatherPanel);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createNewbornsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));

        if (newborns == null || newborns.isEmpty()) {
            JLabel noDataLabel = new JLabel(MessageBundle.getMessage("angal.maternity.no.newborns.recorded"));
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.ITALIC, 12));
            cardsPanel.add(noDataLabel);
        } else {
            for (int i = 0; i < newborns.size(); i++) {
                Newborn nb = newborns.get(i);
                cardsPanel.add(createNewbornCard(nb, i + 1));
                if (i < newborns.size() - 1) {
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createNewbornCard(Newborn nb, int number) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel(MessageBundle.getMessage("angal.maternity.newborn.label") + " #" + number);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // Personal Information
        JPanel personalPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.personal.info.header"));
        personalPanel.setLayout(new GridLayout(4, 2, 5, 2));
        personalPanel.add(createLabel(MessageBundle.getMessage("angal.common.name.txt") + ":"));
        personalPanel.add(createValueLabel(nb.getBabyPatient().getName()));
        personalPanel.add(createLabel(MessageBundle.getMessage("angal.common.code.txt") + ":"));
        personalPanel.add(createValueLabel(String.valueOf(nb.getBabyPatient().getCode())));
        personalPanel.add(createLabel(MessageBundle.getMessage("angal.common.sex.label")));
        personalPanel.add(createValueLabel(String.valueOf(nb.getBabyPatient().getSex())));
        personalPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.birth.date.label") + ":"));
        personalPanel.add(createValueLabel(nb.getBirthDate() != null ? nb.getBirthDate().format(dateTimeFormatter) : MessageBundle.getMessage("angal.common.na.label")));
        card.add(personalPanel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // Birth Information
        JPanel birthPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.birth.info.header"));
        birthPanel.setLayout(new GridLayout(4, 2, 5, 2));
        birthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.birth.weight.label") + ":"));
        birthPanel.add(createValueLabel(nb.getBirthWeight() != null ? nb.getBirthWeight() + " kg" : MessageBundle.getMessage("angal.common.na.label")));
        birthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.birth.length.label") + ":"));
        birthPanel.add(createValueLabel(nb.getBirthLength() != null ? nb.getBirthLength() + " cm" : MessageBundle.getMessage("angal.common.na.label")));
        birthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.head.circumference.label") + ":"));
        birthPanel.add(createValueLabel(nb.getHeadCircumference() != null ? nb.getHeadCircumference() + " cm" : MessageBundle.getMessage("angal.common.na.label")));
        birthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.birth.order.label") + ":"));
        birthPanel.add(createValueLabel(nb.getBirthOrder() != null ? nb.getBirthOrder() : MessageBundle.getMessage("angal.common.na.label")));
        card.add(birthPanel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // Health Information
        JPanel healthPanel = createSectionPanel(MessageBundle.getMessage("angal.maternity.health.info.header"));
        healthPanel.setLayout(new GridLayout(5, 2, 5, 2));
        healthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.neonatal.status.label") + ":"));
        healthPanel.add(createValueLabel(nb.getNeonatalStatus() != null ? getNeonatalStatusLabel(nb.getNeonatalStatus()) : MessageBundle.getMessage("angal.common.na.label")));
        healthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.apgar.1min.label") + ":"));
        healthPanel.add(createValueLabel(nb.getApgarScore1Min() != null ? String.valueOf(nb.getApgarScore1Min()) : MessageBundle.getMessage("angal.common.na.label")));
        healthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.apgar.5min.label") + ":"));
        healthPanel.add(createValueLabel(nb.getApgarScore5Min() != null ? String.valueOf(nb.getApgarScore5Min()) : MessageBundle.getMessage("angal.common.na.label")));
        healthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.resuscitation.label") + ":"));
        healthPanel.add(createValueLabel(nb.getResuscitationRequired() != null ? (nb.getResuscitationRequired() ? MessageBundle.getMessage("angal.common.yes.label") : MessageBundle.getMessage("angal.common.no.label")) : MessageBundle.getMessage("angal.common.na.label")));
        healthPanel.add(createLabel(MessageBundle.getMessage("angal.maternity.hiv.status.label") + ":"));
        healthPanel.add(createValueLabel(nb.getHivStatus() != null ? getHivStatusLabel(nb.getHivStatus()) : MessageBundle.getMessage("angal.common.na.label")));
        card.add(healthPanel);

        JButton birthDeclarationBtn = new JButton(MessageBundle.getMessage("angal.maternity.birth.declaration.btn"));
        birthDeclarationBtn.setFont(birthDeclarationBtn.getFont().deriveFont(Font.PLAIN, 11));
        birthDeclarationBtn.setPreferredSize(new Dimension(140, 26));
        birthDeclarationBtn.setFocusPainted(false);
        birthDeclarationBtn.addActionListener(e -> printBirthDeclaration(e, nb));
        card.add(birthDeclarationBtn);

        return card;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150)),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            panel.getFont().deriveFont(Font.BOLD, 11),
            new Color(50, 50, 50)
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11));
        return label;
    }

    private String getGestationalAgeSafely() {
        try {
            String age = pregnancy.getCurrentGestationalAge();
            return age != null ? age : MessageBundle.getMessage("angal.common.na.label");
        } catch (Exception e) {
            return MessageBundle.getMessage("angal.common.na.label");
        }
    }

    private String getDeliveryModeLabel(DeliveryMode mode) {
        if (mode == null) return MessageBundle.getMessage("angal.common.na.label");
        switch (mode) {
            case SVD: return MessageBundle.getMessage(DeliveryMode.SVD.getKey());
            case VACUUM: return MessageBundle.getMessage(DeliveryMode.VACUUM.getKey());
            case FORCEPS: return MessageBundle.getMessage(DeliveryMode.FORCEPS.getKey());
            case C_SECTION_ELECTIVE: return MessageBundle.getMessage(DeliveryMode.C_SECTION_ELECTIVE.getKey());
            case C_SECTION_EMERGENCY: return MessageBundle.getMessage(DeliveryMode.C_SECTION_EMERGENCY.getKey());
            default: return MessageBundle.getMessage("angal.common.na.label");
        }
    }

    private String getPerinealIntegrityLabel(PerinealIntegrity integrity) {
        if (integrity == null) return MessageBundle.getMessage("angal.common.na.label");
        switch (integrity) {
            case INTACT: return MessageBundle.getMessage(PerinealIntegrity.INTACT.getKey());
            case FIRST_DEGREE: return MessageBundle.getMessage(PerinealIntegrity.FIRST_DEGREE.getKey());
            case SECOND_DEGREE: return MessageBundle.getMessage(PerinealIntegrity.SECOND_DEGREE.getKey());
            case THIRD_DEGREE: return MessageBundle.getMessage(PerinealIntegrity.THIRD_DEGREE.getKey());
            case FOURTH_DEGREE: return MessageBundle.getMessage(PerinealIntegrity.FOURTH_DEGREE.getKey());
            case EPISIOTOMY: return MessageBundle.getMessage(PerinealIntegrity.EPISIOTOMY.getKey());
            default: return MessageBundle.getMessage("angal.common.na.label");
        }
    }

    private String getNeonatalStatusLabel(NeonatalStatus status) {
        if (status == null) return MessageBundle.getMessage("angal.common.na.label");
        switch (status) {
            case ALIVE: return MessageBundle.getMessage(NeonatalStatus.ALIVE.getKey());
            case STILLBORN: return MessageBundle.getMessage(NeonatalStatus.STILLBORN.getKey());
            case EARLY_NEONATAL_DEATH: return MessageBundle.getMessage(NeonatalStatus.EARLY_NEONATAL_DEATH.getKey());
            case TRANSFERRED: return MessageBundle.getMessage(NeonatalStatus.TRANSFERRED.getKey());
            case CRITICAL: return MessageBundle.getMessage(NeonatalStatus.CRITICAL.getKey());
            default: return MessageBundle.getMessage("angal.common.na.label");
        }
    }

    private String getCryTimeLabel(CryTime cryTime) {
        if (cryTime == null) return MessageBundle.getMessage("angal.common.na.label");
        switch (cryTime) {
            case IMMEDIATE: return MessageBundle.getMessage(CryTime.IMMEDIATE.getKey());
            case DELAYED: return MessageBundle.getMessage(CryTime.DELAYED.getKey());
            case NO_CRY: return MessageBundle.getMessage(CryTime.NO_CRY.getKey());
            case UNKNOWN: return MessageBundle.getMessage(CryTime.UNKNOWN.getKey());
            default: return MessageBundle.getMessage("angal.common.na.label");
        }
    }

    private String getHivStatusLabel(HivStatus status) {
        if (status == null) return MessageBundle.getMessage("angal.common.na.label");
        switch (status) {
            case POSITIVE: return MessageBundle.getMessage(HivStatus.POSITIVE.getKey());
            case NEGATIVE: return MessageBundle.getMessage(HivStatus.NEGATIVE.getKey());
            case UNKNOWN: return MessageBundle.getMessage(HivStatus.UNKNOWN.getKey());
            default: return MessageBundle.getMessage("angal.common.na.label");
        }
    }

    private JPanel createPrintButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));

        JButton birthCertificateBtn = new JButton(MessageBundle.getMessage("angal.maternity.birth.certificate.btn"));
        birthCertificateBtn.setFont(birthCertificateBtn.getFont().deriveFont(Font.PLAIN, 11));
        birthCertificateBtn.setPreferredSize(new Dimension(140, 26));
        birthCertificateBtn.setFocusPainted(false);
        birthCertificateBtn.addActionListener(this::printBirthCertificate);

        JButton closeBtn = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
        closeBtn.setFont(closeBtn.getFont().deriveFont(Font.PLAIN, 11));
        closeBtn.setPreferredSize(new Dimension(100, 26));
        closeBtn.addActionListener(e -> dispose());
        panel.add(birthCertificateBtn);
        panel.add(closeBtn);

        return panel;
    }

    private void printBirthDeclaration(ActionEvent e, Newborn newborn) {
        if (newborn == null) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.maternity.cannot.generate.declaration.msg"));
            return;
        }
        new GenericReportPregnancyBirthDeclaration(newborn.getId().longValue());
    }

    private void printBirthCertificate(ActionEvent e) {
        if (delivery == null || newborns == null || newborns.isEmpty()) {
            MessageDialog.error(this, MessageBundle.getMessage("angal.maternity.cannot.generate.certificate.msg"));
            return;
        }
        new GenericReportPregnancyCertificateOfDeclaration(pregnancy.getId().longValue());
    }
}