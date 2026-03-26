package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static ProjetUniv_scheduler.Mesclasses.MainFrame.*;
import static ProjetUniv_scheduler.Mesclasses.SalleDispoPanel.buildStyledTable;

public class SallePanel extends JPanel {

    private DefaultTableModel salleModel, batimentModel;
    private JTable salleTable, batimentTable;

    public SallePanel() {
        setLayout(new GridLayout(1, 2, 16, 0));
        setBackground(BG_DEEP);
        add(buildSallesPanel());
        add(buildBatimentsPanel());
        charger();
    }

    private JPanel buildSallesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DEEP);

        JLabel titre = new JLabel("🏫  Salles");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titre.setForeground(TEXT_MAIN);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(BG_DEEP);
        JButton add = createStyledButton("➕ Ajouter", ACCENT, Color.WHITE);
        JButton del = createStyledButton("🗑 Supprimer", DANGER, Color.WHITE);
        add.addActionListener(e -> dialogAjouterSalle());
        del.addActionListener(e -> supprimerSalle());
        toolbar.add(titre); toolbar.add(add); toolbar.add(del);

        String[] cols = {"N°", "Capacité", "Catégorie", "État", "Bâtiment"};
        salleModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        salleTable = buildStyledTable(salleModel);
        JScrollPane scroll = new JScrollPane(salleTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(BG_CARD);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBatimentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DEEP);

        JLabel titre = new JLabel("🏗  Bâtiments");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titre.setForeground(TEXT_MAIN);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(BG_DEEP);
        JButton add = createStyledButton("➕ Ajouter", ACCENT2, Color.WHITE);
        JButton del = createStyledButton("🗑 Supprimer", DANGER, Color.WHITE);
        add.addActionListener(e -> dialogAjouterBatiment());
        del.addActionListener(e -> supprimerBatiment());
        toolbar.add(titre); toolbar.add(add); toolbar.add(del);

        String[] cols = {"Code", "Nom", "Localisation", "Étages", "Type"};
        batimentModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        batimentTable = buildStyledTable(batimentModel);
        JScrollPane scroll = new JScrollPane(batimentTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(BG_CARD);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void charger() {
        salleModel.setRowCount(0);
        batimentModel.setRowCount(0);
        for (Salle s : UnivSchedulerDAO.listerTous(Salle.class)) {
            salleModel.addRow(new Object[]{
                    s.getNumeroSalle(), s.getCapacite(), s.getCategorieSalle(),
                    s.getEtatSalle(), s.getBatiment() != null ? s.getBatiment().getNomBatiment() : "—"
            });
        }
        for (Batiment b : UnivSchedulerDAO.listerTous(Batiment.class)) {
            batimentModel.addRow(new Object[]{
                    b.getCodeBatiment(), b.getNomBatiment(), b.getLocalisationBatiment(),
                    b.getNbEtage(), b.getTypeBatiment()
            });
        }
    }

    private void dialogAjouterSalle() {
        JTextField num      = createStyledField("Ex: A101");
        JTextField capacite = createStyledField("Ex: 40");
        String[] categories = {"TD", "TP", "AMPHI", "LABO", "SALLE_REUNION"};
        JComboBox<String> cat = new JComboBox<>(categories);
        cat.setBackground(BG_DEEP); cat.setForeground(TEXT_MAIN);
        List<Batiment> bats = UnivSchedulerDAO.listerTous(Batiment.class);
        JComboBox<Batiment> cbBat = new JComboBox<>(bats.toArray(new Batiment[0]));
        cbBat.setBackground(BG_DEEP); cbBat.setForeground(TEXT_MAIN);

        Object[] fields = {
                "Numéro de salle :", num,
                "Capacité :", capacite,
                "Catégorie :", cat,
                "Bâtiment :", cbBat
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Nouvelle salle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Salle s = new Salle(num.getText().trim(),
                        Integer.parseInt(capacite.getText().trim()),
                        (String) cat.getSelectedItem(), "DISPONIBLE");
                s.setBatiment((Batiment) cbBat.getSelectedItem());
                if (UnivSchedulerDAO.sauvegarder(s)) charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void dialogAjouterBatiment() {
        JTextField code  = createStyledField("Ex: BAT-A");
        JTextField nom   = createStyledField("Ex: Bâtiment Sciences");
        JTextField local = createStyledField("Ex: Campus Nord");
        JTextField etage = createStyledField("Ex: 3");
        JTextField type  = createStyledField("Ex: PEDAGOGIQUE");

        Object[] fields = {
                "Code :", code, "Nom :", nom,
                "Localisation :", local, "Nb. étages :", etage, "Type :", type
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Nouveau bâtiment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Batiment b = new Batiment(nom.getText().trim(), local.getText().trim(),
                        Integer.parseInt(etage.getText().trim()),
                        code.getText().trim(), type.getText().trim());
                if (UnivSchedulerDAO.sauvegarder(b)) charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void supprimerSalle() {
        int row = salleTable.getSelectedRow();
        if (row < 0) return;
        String id = (String) salleModel.getValueAt(row, 0);
        Salle s = UnivSchedulerDAO.charger(Salle.class, id);
        if (s != null && UnivSchedulerDAO.supprimer(s)) charger();
    }

    private void supprimerBatiment() {
        int row = batimentTable.getSelectedRow();
        if (row < 0) return;
        String id = (String) batimentModel.getValueAt(row, 0);
        Batiment b = UnivSchedulerDAO.charger(Batiment.class, id);
        if (b != null && UnivSchedulerDAO.supprimer(b)) charger();
    }
}
