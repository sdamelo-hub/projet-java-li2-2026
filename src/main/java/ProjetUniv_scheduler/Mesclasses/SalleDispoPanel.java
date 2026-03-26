package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ProjetUniv_scheduler.Mesclasses.MainFrame.*;

/**
 * Panel de recherche de salles disponibles avec filtres.
 */
public class SalleDispoPanel extends JPanel {

    private JSpinner dateSpinner;
    private JSpinner heureDebutSpinner;
    private JSpinner heureFinSpinner;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public SalleDispoPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_DEEP);
        add(buildFiltersPanel(), BorderLayout.NORTH);
        add(buildResultsPanel(), BorderLayout.CENTER);
    }

    private JPanel buildFiltersPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Titre
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        JLabel titre = new JLabel("🔍  Rechercher une salle disponible");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titre.setForeground(TEXT_MAIN);
        card.add(titre, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;

        // Date
        gbc.gridx = 0;
        card.add(buildFieldLabel("Date"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        styleSpinner(dateSpinner);
        card.add(dateSpinner, gbc);

        // Heure début
        gbc.gridx = 2;
        card.add(buildFieldLabel("Heure début"), gbc);
        gbc.gridx = 3;
        heureDebutSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor hDebEditor = new JSpinner.DateEditor(heureDebutSpinner, "HH:mm");
        heureDebutSpinner.setEditor(hDebEditor);
        styleSpinner(heureDebutSpinner);
        card.add(heureDebutSpinner, gbc);

        // Heure fin
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(buildFieldLabel("Heure fin"), gbc);
        gbc.gridx = 1;
        heureFinSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor hFinEditor = new JSpinner.DateEditor(heureFinSpinner, "HH:mm");
        heureFinSpinner.setEditor(hFinEditor);
        styleSpinner(heureFinSpinner);
        card.add(heureFinSpinner, gbc);

        // Bouton
        gbc.gridx = 3; gbc.gridy = 2;
        JButton searchBtn = createStyledButton("🔍  Rechercher", ACCENT, Color.WHITE);
        searchBtn.addActionListener(e -> lancerRecherche());
        card.add(searchBtn, gbc);

        return card;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DEEP);

        statusLabel = new JLabel("Utilisez les filtres ci-dessus pour rechercher.");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(TEXT_MUTED);

        String[] cols = {"N° Salle", "Capacité", "Catégorie", "État", "Bâtiment", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        resultsTable = buildStyledTable(tableModel);
        resultsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        resultsTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this));
        resultsTable.getColumn("Capacité").setMaxWidth(80);
        resultsTable.getColumn("État").setMaxWidth(100);
        resultsTable.getColumn("Action").setMaxWidth(130);

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBackground(BG_DEEP);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        scroll.getViewport().setBackground(BG_CARD);

        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void lancerRecherche() {
        tableModel.setRowCount(0);
        try {
            java.util.Date dateSel   = (java.util.Date) dateSpinner.getValue();
            java.util.Date hDebSel  = (java.util.Date) heureDebutSpinner.getValue();
            java.util.Date hFinSel  = (java.util.Date) heureFinSpinner.getValue();

            LocalDate date  = dateSel.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalTime debut = hDebSel.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                    .withSecond(0).withNano(0);
            LocalTime fin   = hFinSel.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                    .withSecond(0).withNano(0);

            if (!debut.isBefore(fin)) {
                showError("L'heure de début doit être avant l'heure de fin."); return;
            }

            List<Salle> salles = UnivSchedulerDAO.trouverSallesDisponibles(date, debut, fin);
            if (salles.isEmpty()) {
                statusLabel.setText("Aucune salle disponible pour ce créneau.");
                statusLabel.setForeground(WARNING);
            } else {
                statusLabel.setText(salles.size() + " salle(s) disponible(s) trouvée(s).");
                statusLabel.setForeground(SUCCESS);
                for (Salle s : salles) {
                    tableModel.addRow(new Object[]{
                            s.getNumeroSalle(),
                            s.getCapacite(),
                            s.getCategorieSalle(),
                            s.getEtatSalle(),
                            s.getBatiment() != null ? s.getBatiment().getNomBatiment() : "—",
                            "Réserver"
                    });
                }
            }
        } catch (Exception ex) {
            showError("Erreur lors de la recherche : " + ex.getMessage());
        }
    }

    void ouvrirReservation(int row) {
        String numeroSalle = (String) tableModel.getValueAt(row, 0);
        JOptionPane.showMessageDialog(this,
                "Réservation de la salle : " + numeroSalle + "\n(Fonctionnalité à compléter dans ReservationPanel)",
                "Nouvelle réservation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        statusLabel.setText("⚠ " + msg);
        statusLabel.setForeground(DANGER);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    static JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_MAIN);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setGridColor(BORDER_CLR);
        table.setSelectionBackground(new Color(0x1e2a4a));
        table.setSelectionForeground(TEXT_MAIN);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(BG_SIDEBAR);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        return table;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(BG_DEEP);
        spinner.setForeground(TEXT_MAIN);
        spinner.setPreferredSize(new Dimension(130, 36));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_DEEP);
            tf.setForeground(TEXT_MAIN);
            tf.setCaretColor(ACCENT);
            tf.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        }
    }

    private JLabel buildFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    // ─── Button Renderer/Editor pour le tableau ───────────────────────────
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(ACCENT);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText(v != null ? v.toString() : "");
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;
        private SalleDispoPanel panel;

        public ButtonEditor(JCheckBox cb, SalleDispoPanel panel) {
            super(cb);
            this.panel = panel;
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(ACCENT);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) {
            label = v != null ? v.toString() : "";
            button.setText(label);
            clicked = true;
            row = r;
            return button;
        }

        @Override public Object getCellEditorValue() {
            if (clicked) panel.ouvrirReservation(row);
            clicked = false;
            return label;
        }
    }
}
