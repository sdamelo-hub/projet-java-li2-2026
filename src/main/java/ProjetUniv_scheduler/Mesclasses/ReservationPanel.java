package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static ProjetUniv_scheduler.Mesclasses.MainFrame.*;
import static ProjetUniv_scheduler.Mesclasses.SalleDispoPanel.buildStyledTable;

/**
 * Panel de gestion des réservations.
 * Permet de créer, valider, annuler et notifier par email.
 */
public class ReservationPanel extends JPanel {

    private final Utilisateur utilisateur;
    private DefaultTableModel tableModel;
    private JTable table;

    public ReservationPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_DEEP);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        chargerReservations();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(BG_DEEP);

        JButton btnNouvelle = createStyledButton("➕  Nouvelle réservation", ACCENT, Color.WHITE);
        JButton btnAnnuler  = createStyledButton("❌  Annuler", DANGER, Color.WHITE);
        JButton btnValider  = createStyledButton("✅  Valider", SUCCESS, Color.WHITE);
        JButton btnEmail    = createStyledButton("📧  Envoyer email", new Color(0x7b68ee), Color.WHITE);
        JButton btnRefresh  = createStyledButton("🔄  Actualiser", BG_CARD, TEXT_MUTED);

        btnNouvelle.addActionListener(e -> ouvrirDialogNouvelle());
        btnAnnuler.addActionListener(e -> annulerSelectionne());
        btnValider.addActionListener(e -> validerSelectionne());
        btnEmail.addActionListener(e -> envoyerEmailSelectionne());
        btnRefresh.addActionListener(e -> chargerReservations());

        bar.add(btnNouvelle);
        bar.add(btnAnnuler);
        bar.add(btnValider);
        bar.add(btnEmail);
        bar.add(btnRefresh);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"#", "Salle", "Cours", "Date", "Heure", "Nature", "État", "Réservé par"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildStyledTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setMaxWidth(110);

        // Colorer les lignes selon l'état
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String etat = (String) t.getValueAt(r, 6);
                    if ("VALIDEE".equals(etat))    comp.setBackground(new Color(0x1a2e1a));
                    else if ("ANNULEE".equals(etat)) comp.setBackground(new Color(0x2e1a1a));
                    else                             comp.setBackground(BG_CARD);
                }
                ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    private void chargerReservations() {
        tableModel.setRowCount(0);
        List<Reservation> reservations = UnivSchedulerDAO.listerTous(Reservation.class);
        for (Reservation r : reservations) {
            Creneau c = r.getMonCreneau();
            tableModel.addRow(new Object[]{
                    r.getNumReservation(),
                    c != null && c.getSalle() != null ? c.getSalle().getNumeroSalle() : "—",
                    c != null && c.getCours() != null ? c.getCours().getIntituleMatiere() : "—",
                    c != null ? c.getDateSeance().toString() : "—",
                    c != null ? c.getHeureDebut() + "→" + c.getHeureFin() : "—",
                    r.getNatureSession(),
                    r.getEtatReservation(),
                    r.getReservateur() != null ? r.getReservateur().toString() : "—"
            });
        }
    }

    private void ouvrirDialogNouvelle() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Nouvelle réservation", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG_CARD);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Charger les salles et cours disponibles
        List<Salle> salles = UnivSchedulerDAO.listerTous(Salle.class);
        List<Cours> cours  = UnivSchedulerDAO.listerTous(Cours.class);

        JComboBox<Salle> cbSalle = new JComboBox<>(salles.toArray(new Salle[0]));
        JComboBox<Cours> cbCours = new JComboBox<>(cours.toArray(new Cours[0]));
        styleCombo(cbSalle); styleCombo(cbCours);

        JTextField tfDate  = createStyledField("AAAA-MM-JJ");
        JTextField tfDebut = createStyledField("HH:MM (ex: 08:00)");
        JTextField tfFin   = createStyledField("HH:MM (ex: 10:00)");
        JTextField tfMotif = createStyledField("Motif de la réservation");

        String[] natures = {"COURS", "TD", "TP", "CONFERENCE", "REUNION", "SOUTENANCE", "AUTRE"};
        JComboBox<String> cbNature = new JComboBox<>(natures);
        styleCombo(cbNature);

        form.add(buildFieldLabel("Salle")); form.add(cbSalle);
        form.add(buildFieldLabel("Cours")); form.add(cbCours);
        form.add(buildFieldLabel("Date (AAAA-MM-JJ)")); form.add(tfDate);
        form.add(buildFieldLabel("Heure début")); form.add(tfDebut);
        form.add(buildFieldLabel("Heure fin")); form.add(tfFin);
        form.add(buildFieldLabel("Nature")); form.add(cbNature);
        form.add(buildFieldLabel("Motif")); form.add(tfMotif);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(BG_CARD);
        JButton annuler = createStyledButton("Annuler", BORDER_CLR, TEXT_MUTED);
        JButton confirmer = createStyledButton("Confirmer", ACCENT, Color.WHITE);

        annuler.addActionListener(e -> dialog.dispose());
        confirmer.addActionListener(e -> {
            try {
                Salle salle = (Salle) cbSalle.getSelectedItem();
                Cours c = (Cours) cbCours.getSelectedItem();
                java.time.LocalDate date = java.time.LocalDate.parse(tfDate.getText().trim());
                java.time.LocalTime debut = java.time.LocalTime.parse(tfDebut.getText().trim());
                java.time.LocalTime fin   = java.time.LocalTime.parse(tfFin.getText().trim());

                // Vérifier conflit
                boolean conflit = UnivSchedulerDAO.detecterConflitSalle(
                        salle.getNumeroSalle(), date, debut, fin, null);
                if (conflit) {
                    JOptionPane.showMessageDialog(dialog,
                            "⚠ Conflit détecté : cette salle est déjà occupée sur ce créneau !",
                            "Conflit horaire", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Créer le créneau
                String idCreneau = "CR-" + System.currentTimeMillis();
                Creneau creneau = new Creneau(idCreneau, date, debut, fin);
                creneau.setSalle(salle);
                creneau.setCours(c);
                creneau.setTypeSeance((String) cbNature.getSelectedItem());

                // Créer la réservation
                Reservation reservation = new Reservation(creneau, (String) cbNature.getSelectedItem(),
                        1, utilisateur);
                reservation.setMotifReservation(tfMotif.getText().trim());

                if (UnivSchedulerDAO.sauvegarder(reservation)) {
                    chargerReservations();
                    dialog.dispose();
                    // Envoyer email de confirmation
                    if (utilisateur != null && utilisateur.getEmail() != null) {
                        new Thread(() -> NotificationEmailService
                                .notifierConfirmationReservation(reservation)).start();
                    }
                    JOptionPane.showMessageDialog(this,
                            "✅ Réservation créée avec succès !\nUn email de confirmation a été envoyé.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(annuler); btnPanel.add(confirmer);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void annulerSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Sélectionnez une réservation."); return; }
        int num = (Integer) tableModel.getValueAt(row, 0);
        Reservation r = UnivSchedulerDAO.charger(Reservation.class, num);
        if (r == null) return;
        r.annulerReservation();
        if (UnivSchedulerDAO.sauvegarder(r)) {
            chargerReservations();
            new Thread(() -> NotificationEmailService.notifierAnnulationReservation(r)).start();
            showInfo("Réservation annulée. Email envoyé.");
        }
    }

    private void validerSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Sélectionnez une réservation."); return; }
        int num = (Integer) tableModel.getValueAt(row, 0);
        Reservation r = UnivSchedulerDAO.charger(Reservation.class, num);
        if (r == null) return;
        r.validerReservation();
        if (UnivSchedulerDAO.sauvegarder(r)) chargerReservations();
    }

    private void envoyerEmailSelectionne() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("Sélectionnez une réservation."); return; }
        int num = (Integer) tableModel.getValueAt(row, 0);
        Reservation r = UnivSchedulerDAO.charger(Reservation.class, num);
        if (r == null) return;
        new Thread(() -> {
            NotificationEmailService.notifierConfirmationReservation(r);
            SwingUtilities.invokeLater(() -> showInfo("Email envoyé !"));
        }).start();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel buildFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private <T> void styleCombo(JComboBox<T> cb) {
        cb.setBackground(BG_DEEP);
        cb.setForeground(TEXT_MAIN);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }
}
