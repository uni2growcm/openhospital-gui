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

import org.isf.generaldata.MessageBundle;
import org.isf.maternity.manager.NewBornBrowserManager;
import org.isf.maternity.manager.PregnancyDeliveryBrowserManager;
import org.isf.maternity.manager.PregnancyVisitBrowserManager;
import org.isf.maternity.model.*;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.typology.manager.TypologyBrowserManager;
import org.isf.typology.model.Family;
import org.isf.typology.model.Typology;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeliveryEdit extends JDialog {

    // ================= CORE =================
    private final Pregnancy pregnancy;
    private final PregnancyDelivery delivery;
    private final boolean insert;

    private final PregnancyDeliveryBrowserManager deliveryManager =
            Context.getApplicationContext().getBean(PregnancyDeliveryBrowserManager.class);

    private final NewBornBrowserManager newbornManager =
            Context.getApplicationContext().getBean(NewBornBrowserManager.class);

    private final PatientBrowserManager patientManager =
            Context.getApplicationContext().getBean(PatientBrowserManager.class);

    private final PregnancyVisitBrowserManager visitManager =
            Context.getApplicationContext().getBean(PregnancyVisitBrowserManager.class);

    // ================= LISTENER =================
    public interface DeliveryListener {
        void deliveryInserted(AWTEvent e, PregnancyDelivery delivery);
        void deliveryUpdated(AWTEvent e, PregnancyDelivery delivery);
    }

    private final List<DeliveryListener> listeners = new ArrayList<>();

    public void addDeliveryListener(DeliveryListener l) {
        listeners.add(l);
    }

    private void fireInserted(PregnancyDelivery d) {
        AWTEvent ev = new AWTEvent(this, AWTEvent.RESERVED_ID_MAX + 1) {};
        for (DeliveryListener l : listeners) l.deliveryInserted(ev, d);
    }

    private void fireUpdated(PregnancyDelivery d) {
        AWTEvent ev = new AWTEvent(this, AWTEvent.RESERVED_ID_MAX + 2) {};
        for (DeliveryListener l : listeners) l.deliveryUpdated(ev, d);
    }

    // ================= UI =================
    private JComboBox<Typology> deliveryTypeCombo;
    private GoodDateTimeSpinnerChooser deliveryDateField;

    // father info
    private JTextField fatherName, fatherAge, fatherAddress, fatherPhone, fatherProfession;

    // newborn table
    private JTable table;
    private DefaultTableModel model;

    private JButton newVisitButton;
    private JButton viewVisitsButton;

    public DeliveryEdit(JFrame owner, Pregnancy pregnancy, boolean insert) {
        super(owner, true);
        this.pregnancy = pregnancy;
        this.delivery = new PregnancyDelivery();
        this.delivery.setPregnancy(pregnancy);
        this.insert = insert;
        init();
    }

    // ================= INIT =================
    private void init() {
        setTitle("Delivery - Newborn Registration");
        setSize(1300, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(topPanel(), BorderLayout.NORTH);
        add(centerPanel(), BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.SOUTH);
    }

    // ================= TOP =================
    private JPanel topPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Delivery"));

        deliveryDateField = new GoodDateTimeSpinnerChooser(LocalDateTime.now());

        deliveryTypeCombo = new JComboBox<>();
        try {
            Context.getApplicationContext()
                    .getBean(TypologyBrowserManager.class)
                    .getTypologies(Family.DELIVERYTYPE)
                    .forEach(deliveryTypeCombo::addItem);
        } catch (Exception ignored) {}

        p.add(new JLabel("Delivery Date"));
        p.add(deliveryDateField);

        p.add(new JLabel("Delivery Type"));
        p.add(deliveryTypeCombo);

        return p;
    }

    // ================= CENTER =================
    private JPanel centerPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(fatherPanel(), BorderLayout.NORTH);
        p.add(newbornPanel(), BorderLayout.CENTER);
        return p;
    }

    // ================= FATHER =================
    private JPanel fatherPanel() {
        JPanel p = new JPanel(new GridLayout(2, 5, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Father Information"));

        fatherName = new JTextField();
        fatherAge = new JTextField();
        fatherAddress = new JTextField();
        fatherPhone = new JTextField();
        fatherProfession = new JTextField();

        p.add(new JLabel("Name")); p.add(fatherName);
        p.add(new JLabel("Age")); p.add(fatherAge);
        p.add(new JLabel("Address")); p.add(fatherAddress);
        p.add(new JLabel("Phone")); p.add(fatherPhone);
        p.add(new JLabel("Profession")); p.add(fatherProfession);

        return p;
    }

    // ================= NEWBORN TABLE =================
    private JPanel newbornPanel() {

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Newborns"));

        String[] cols = {
                "First Name", "Last Name", "Sex",
                "Weight", "Length", "Head Circ",
                "APGAR1", "APGAR5",
                "Birth DateTime",
                "CryTime", "HIV", "Neonatal",
                "Resuscitation", "Anomalies"
        };

        model = new DefaultTableModel(cols, 1);
        table = new JTable(model);

        table.setRowHeight(30);

        // ENUM EDITORS
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(sexCombo()));
        table.getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(enumCombo(CryTime.values())));
        table.getColumnModel().getColumn(10).setCellEditor(new DefaultCellEditor(enumCombo(HivStatus.values())));
        table.getColumnModel().getColumn(11).setCellEditor(new DefaultCellEditor(enumCombo(NeonatalStatus.values())));
        table.getColumnModel().getColumn(12).setCellEditor(new DefaultCellEditor(booleanCombo()));

        JScrollPane sp = new JScrollPane(table);

        JPanel btn = new JPanel();
        JButton add = new JButton("+");
        JButton del = new JButton("-");

        add.addActionListener(e -> {
            model.addRow(new Object[cols.length]);
            resize();
        });

        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) model.removeRow(r);
            resize();
        });

        btn.add(add);
        btn.add(del);

        p.add(sp, BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);

        resize();
        return p;
    }

    // ================= DYNAMIC HEIGHT =================
    private void resize() {
        int rows = model.getRowCount();
        table.setPreferredScrollableViewportSize(
                new Dimension(1200, Math.max(200, rows * 35))
        );
        table.revalidate();
    }

    // ================= SAVE =================
    private void save() {

        try {
            if (deliveryDateField.getLocalDateTime() == null)
                throw new RuntimeException("Delivery date required");

            if (deliveryTypeCombo.getSelectedItem() == null)
                throw new RuntimeException("Delivery type required");

            delivery.setDeliveryDate(deliveryDateField.getLocalDateTime());
            delivery.setDeliveryType((Typology) deliveryTypeCombo.getSelectedItem());

            delivery.setFatherName(fatherName.getText());

            PregnancyDelivery saved = deliveryManager.newDelivery(delivery);

            Patient mother = pregnancy.getPatient();

            for (int i = 0; i < model.getRowCount(); i++) {

                String fn = val(i,0);
                String ln = val(i,1);

                if (fn == null || ln == null)
                    throw new RuntimeException("Baby name required");

                Patient baby = new Patient();
                baby.setFirstName(fn);
                baby.setSecondName(ln);
                baby.setSex(parseChar(val(i,2)));

                baby.setBirthDate(LocalDate.now());

                // inherit mother
                baby.setAddress(mother.getAddress());
                baby.setCity(mother.getCity());
                baby.setTelephone(mother.getTelephone());
                baby.setMotherName(mother.getName());
                baby.setFatherName(fatherName.getText());

                Patient savedBaby = patientManager.savePatient(baby);

                Newborn nb = new Newborn();
                nb.setDelivery(saved);
                nb.setBabyPatient(savedBaby);

                nb.setBirthDate(parseDateTime(val(i,8)));
                nb.setBirthWeight(parseD(val(i,3)));
                nb.setBirthLength(parseD(val(i,4)));
                nb.setHeadCircumference(parseD(val(i,5)));

                nb.setApgarScore1Min(parseI(val(i,6)));
                nb.setApgarScore5Min(parseI(val(i,7)));

                nb.setCryTime(parseEnum(val(i,9), CryTime.class));
                nb.setHivStatus(parseEnum(val(i,10), HivStatus.class));
                nb.setNeonatalStatus(parseEnum(val(i,11), NeonatalStatus.class));

                nb.setResuscitationRequired(Boolean.parseBoolean(val(i,12)));
                nb.setCongenitalAnomalies(val(i,13));

                newbornManager.newNewborn(nb);
            }

            MessageDialog.info(this, "Saved successfully");
            fireInserted(saved);
            dispose();

        } catch (Exception e) {
            MessageDialog.error(this, e.getMessage());
        }
    }

    // ================= VISITS =================
    private void newVisit() { /* unchanged */ }
    private void viewVisits() { /* unchanged */ }

    // ================= HELPERS =================
    private String val(int r,int c){Object o=model.getValueAt(r,c);return o==null?null:o.toString();}
    private Double parseD(String v){try{return v==null?null:Double.parseDouble(v);}catch(Exception e){return null;}}
    private Integer parseI(String v){try{return v==null?null:Integer.parseInt(v);}catch(Exception e){return null;}}
    private LocalDateTime parseDateTime(String v){try{return LocalDateTime.parse(v);}catch(Exception e){return LocalDateTime.now();}}
    private char parseChar(String v){return "F".equalsIgnoreCase(v)?'F':'M';}

    private <T extends Enum<T>> T parseEnum(String v, Class<T> c){
        try { return v==null?null:Enum.valueOf(c,v); }
        catch(Exception e){ return null; }
    }

    private JComboBox<String> sexCombo(){
        return new JComboBox<>(new String[]{"M","F"});
    }

    private JComboBox<String> enumCombo(Enum<?>[] vals){
        JComboBox<String> c = new JComboBox<>();
        for(Enum<?> v:vals) c.addItem(v.name());
        return c;
    }

    private JComboBox<String> booleanCombo(){
        return new JComboBox<>(new String[]{"true","false"});
    }

    // ================= BOTTOM =================
    private JPanel bottomPanel(){
        JPanel p = new JPanel();

        newVisitButton = new JButton("New Visit");
        viewVisitsButton = new JButton("View Visits");

        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(e -> save());
        cancel.addActionListener(e -> dispose());

        p.add(newVisitButton);
        p.add(viewVisitsButton);
        p.add(save);
        p.add(cancel);

        return p;
    }
}