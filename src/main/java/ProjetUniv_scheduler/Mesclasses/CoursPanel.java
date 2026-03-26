package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static ProjetUniv_scheduler.Mesclasses.MainFrame.*;
import static ProjetUniv_scheduler.Mesclasses.SalleDispoPanel.buildStyledTable;

public class CoursPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public CoursPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG_DEEP);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        charger();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(BG_DEEP);

        JLabel titre = new JLabel("📚  Gestion des cours");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titre.setForeground(TEXT_MAIN);

        JButton add = createStyledButton("➕ Ajouter", ACCENT, Color.WHITE);
        JButton del = createStyledButton("🗑 Supprimer", DANGER, Color.WHITE);
        JButton ref = createStyledButton("🔄 Actualiser", BG_CARD, TEXT_MUTED);

        add.addActionListener(e -> dialogAjouter());
        del.addActionListener(e -> supprimer());
        ref.addActionListener(e -> charger());

        bar.add(titre); bar.add(add); bar.add(del); bar.add(ref);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Code", "Intitulé", "Heures totales", "Heures effectuées", "Avancement", "Enseignant", "Groupe"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildStyledTable(model);
        table.getColumnModel().getColumn(4).setMaxWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(90);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    private void charger() {
        model.setRowCount(0);
        for (Cours c : UnivSchedulerDAO.listerTous(Cours.class)) {
            model.addRow(new Object[]{
                    c.getCodeCours(),
                    c.getIntituleMatiere(),
                    c.getNbrHeure() + "h",
                    c.getHeuresEffectuees() + "h",
                    String.format("%.0f%%", c.obtenirPourcentageAvancement()),
                    c.getEnseignant() != null ? c.getEnseignant().toString() : "—",
                    c.getGroupe() != null ? c.getGroupe().toString() : "—"
            });
        }
    }

    private void dialogAjouter() {
        JTextField code   = createStyledField("Ex: INFO301");
        JTextField intit  = createStyledField("Ex: Programmation Orientée Objet");
        JTextField heures = createStyledField("Ex: 30");

        List<Enseignant> enseignants = UnivSchedulerDAO.listerTous(Enseignant.class);
        JComboBox<Enseignant> cbEns = new JComboBox<>(enseignants.toArray(new Enseignant[0]));
        cbEns.setBackground(BG_DEEP); cbEns.setForeground(TEXT_MAIN);
        cbEns.insertItemAt(null, 0); cbEns.setSelectedIndex(0);

        List<Groupe> groupes = UnivSchedulerDAO.listerTous(Groupe.class);
        JComboBox<Groupe> cbGrp = new JComboBox<>(groupes.toArray(new Groupe[0]));
        cbGrp.setBackground(BG_DEEP); cbGrp.setForeground(TEXT_MAIN);
        cbGrp.insertItemAt(null, 0); cbGrp.setSelectedIndex(0);

        Object[] fields = {
                "Code cours :", code,
                "Intitulé :", intit,
                "Volume horaire (h) :", heures,
                "Enseignant :", cbEns,
                "Groupe :", cbGrp
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Nouveau cours",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Cours c = new Cours(code.getText().trim(), intit.getText().trim(),
                        Double.parseDouble(heures.getText().trim()));
                c.setEnseignant((Enseignant) cbEns.getSelectedItem());
                c.setGroupe((Groupe) cbGrp.getSelectedItem());
                if (UnivSchedulerDAO.sauvegarder(c)) charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void supprimer() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String id = (String) model.getValueAt(row, 0);
        Cours c = UnivSchedulerDAO.charger(Cours.class, id);
        if (c != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer le cours " + c.getIntituleMatiere() + " ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && UnivSchedulerDAO.supprimer(c)) charger();
        }
    }
}
