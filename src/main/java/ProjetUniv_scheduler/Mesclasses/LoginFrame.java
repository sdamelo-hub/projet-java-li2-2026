package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.MultipleGradientPaint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * UNIV-SCHEDULER — LoginFrame
 * Palette : Marron chaud / Noir profond / Or ambre
 * Flux : Accueil → [Emploi du temps | Salles | Calendrier | Connexion] → MainFrame
 */
public class LoginFrame extends JFrame {

    // ── Palette Marron / Noir / Or ────────────────────────────────────────
    static final Color BG_DARK       = new Color(0x0a0705);
    static final Color BG_BROWN      = new Color(0x1c1008);
    static final Color BG_MID        = new Color(0x2c1a0e);
    static final Color BG_CARD       = new Color(0x1f1309);
    static final Color BG_FIELD      = new Color(0x120c05);
    static final Color BORDER_CLR    = new Color(0x3d2410);
    static final Color ACCENT        = new Color(0xc8892a);
    static final Color ACCENT_LIGHT  = new Color(0xe8aa50);
    static final Color ACCENT_DIM    = new Color(0x8a5e1a);
    static final Color ACCENT_WARM   = new Color(0xff6b35);
    static final Color TEXT_MAIN     = new Color(0xf2e8d9);
    static final Color TEXT_MUTED    = new Color(0x8a7060);
    static final Color TEXT_FAINT    = new Color(0x3d2e20);
    static final Color DANGER        = new Color(0xff5545);

    // ── Navigation ────────────────────────────────────────────────────────
    static final String SCREEN_WELCOME  = "welcome";
    static final String SCREEN_SCHEDULE = "schedule";
    static final String SCREEN_ROOMS    = "rooms";
    static final String SCREEN_CALENDAR = "calendar";
    static final String SCREEN_LOGIN    = "login";

    private JPanel     mainContainer;
    private CardLayout cardLayout;
    private String     previousScreen = SCREEN_WELCOME;

    private JTextField     fieldId;
    private JPasswordField fieldMdp;
    private JLabel         errorLabel;
    private JButton        loginBtn;
    private boolean        loading = false;

    public LoginFrame() {
        setTitle("UNIV-SCHEDULER");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout    = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.add(buildWelcomeScreen(),  SCREEN_WELCOME);
        mainContainer.add(buildScheduleScreen(), SCREEN_SCHEDULE);
        mainContainer.add(buildRoomsScreen(),    SCREEN_ROOMS);
        mainContainer.add(buildCalendarScreen(), SCREEN_CALENDAR);
        mainContainer.add(buildLoginScreen(),    SCREEN_LOGIN);

        add(mainContainer);
        cardLayout.show(mainContainer, SCREEN_WELCOME);
    }

    // =========================================================================
    //  NAVIGATION
    // =========================================================================

    void navigateTo(String screen) {
        previousScreen = getCurrentScreenId();
        cardLayout.show(mainContainer, screen);
        if (screen.equals(SCREEN_LOGIN)) {
            if (errorLabel != null) errorLabel.setText(" ");
            if (fieldId  != null) fieldId.setText("");
            if (fieldMdp != null) fieldMdp.setText("");
        }
    }

    private String getCurrentScreenId() {
        return previousScreen;
    }

    void goBack() {
        cardLayout.show(mainContainer, SCREEN_WELCOME);
    }

    // =========================================================================
    //  BARRE DE NAVIGATION COMMUNE
    // =========================================================================

    JPanel buildNavBar(String title, String icon) {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // fond semi-transparent
                g2.setColor(new Color(12, 7, 5, 230));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // ── CORRECTION : suppression du GradientPaint invalide ──
                // On utilise uniquement LinearGradientPaint (3 couleurs supportées)
                g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), 0,
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{new Color(200, 137, 42, 0), ACCENT, new Color(200, 137, 42, 0)}
                ));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        bar.setPreferredSize(new Dimension(0, 62));

        // Bouton retour animé
        JButton back = new JButton() {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(200, 137, 42, 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(200, 137, 42, 80));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.setColor(hovered ? ACCENT : TEXT_MUTED);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawLine(cx + 6, cy - 5, cx - 2, cy);
                g2.drawLine(cx - 2, cy, cx + 6, cy + 5);
                g2.drawLine(cx - 2, cy, cx + 10, cy);
                g2.dispose();
            }
        };
        back.setPreferredSize(new Dimension(36, 36));
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setFocusPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setToolTipText("Retour à l'accueil");
        back.addActionListener(e -> goBack());

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleRow.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLbl.setForeground(ACCENT);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 16));
        titleLbl.setForeground(TEXT_MAIN);
        JLabel appName = new JLabel("  ·  UNIV-SCHEDULER");
        appName.setFont(new Font("Courier New", Font.PLAIN, 11));
        appName.setForeground(TEXT_FAINT);
        titleRow.add(iconLbl);
        titleRow.add(titleLbl);
        titleRow.add(appName);

        JButton loginShortcut = buildSmallAccentButton("Se connecter →");
        loginShortcut.addActionListener(e -> navigateTo(SCREEN_LOGIN));

        bar.add(back,          BorderLayout.WEST);
        bar.add(titleRow,      BorderLayout.CENTER);
        bar.add(loginShortcut, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    //  ÉCRAN D'ACCUEIL
    // =========================================================================

    private JPanel buildWelcomeScreen() {
        JPanel screen = new WelcomeBackground();
        screen.setLayout(new BorderLayout());

        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);
        hero.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setBorder(BorderFactory.createEmptyBorder(0, 80, 60, 80));

        heroContent.add(buildAnimatedBadge());
        heroContent.add(Box.createVerticalStrut(28));

        JLabel h1a = new JLabel("Bienvenue sur");
        h1a.setFont(new Font("Palatino Linotype", Font.PLAIN, 28));
        h1a.setForeground(TEXT_MUTED);
        h1a.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel h1b = new JLabel("UNIV-SCHEDULER");
        h1b.setFont(new Font("Palatino Linotype", Font.BOLD, 52));
        h1b.setForeground(TEXT_MAIN);
        h1b.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel h1c = new JLabel("Votre université, organisée.");
        h1c.setFont(new Font("Palatino Linotype", Font.ITALIC, 26));
        h1c.setForeground(ACCENT);
        h1c.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroContent.add(h1a);
        heroContent.add(h1b);
        heroContent.add(h1c);
        heroContent.add(Box.createVerticalStrut(20));

        JTextArea desc = new JTextArea("Explorez les emplois du temps, les salles et le calendrier\nsans avoir besoin de vous connecter.");
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setFocusable(false);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(TEXT_MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        heroContent.add(desc);
        heroContent.add(Box.createVerticalStrut(48));

        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        cardsRow.add(buildFeatureCard("📅", "Emplois du temps",
                "Consultez les plannings\nde toutes les filières", SCREEN_SCHEDULE, new Color(0xc8892a)));
        cardsRow.add(buildFeatureCard("🏛", "Salles",
                "Visualisez la disponibilité\ndes salles en temps réel", SCREEN_ROOMS, new Color(0xd4874a)));
        cardsRow.add(buildFeatureCard("📆", "Calendrier",
                "Retrouvez les événements\net dates importantes", SCREEN_CALENDAR, new Color(0xb87333)));

        heroContent.add(cardsRow);
        heroContent.add(Box.createVerticalStrut(40));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = buildMainLoginButton();
        btnRow.add(loginBtn);
        btnRow.add(Box.createHorizontalStrut(16));

        JLabel hint = new JLabel("Pour accéder à la gestion complète");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_FAINT);
        btnRow.add(hint);
        heroContent.add(btnRow);

        hero.add(heroContent);
        screen.add(hero, BorderLayout.CENTER);

        JPanel footer = buildWelcomeFooter();
        screen.add(footer, BorderLayout.SOUTH);

        return screen;
    }

    private JPanel buildFeatureCard(String emoji, String title, String desc, final String target, Color accent) {
        // On utilise un tableau pour le survol car c'est une valeur mutable
        // partagée entre le paintComponent et les listeners
        final boolean[] hovered = {false};

        // Déclarez le panel comme final pour qu'il soit accessible partout
        final JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // ... reste de votre code de dessin ...
                Color bg = hovered[0] ? new Color(0x2c1a0e) : BG_CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // ... (le reste est correct)
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; card.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                // Le Timer a besoin que 'target' et 'card' soient finaux
                final int[] count = {0};
                Timer flash = new Timer(60, null);
                flash.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        hovered[0] = (count[0] % 2 == 0);
                        card.repaint();
                        count[0]++;
                        if (count[0] > 4) {
                            flash.stop();
                            navigateTo(target);
                        }
                    }
                });
                flash.start();
            }
        });

        // ... (Ajout des labels comme avant) ...
        return card;
    }

    private JPanel buildAnimatedBadge() {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 137, 42, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(200, 137, 42, 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 18));
        badge.setMaximumSize(new Dimension(280, 30));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        dot.setForeground(ACCENT);
        new Timer(800, e -> dot.setForeground(
                dot.getForeground().equals(ACCENT) ? ACCENT_DIM : ACCENT
        )).start();

        JLabel txt = new JLabel("Système de gestion universitaire  ·  2026");
        txt.setFont(new Font("Courier New", Font.PLAIN, 11));
        txt.setForeground(TEXT_MUTED);

        badge.add(dot);
        badge.add(txt);
        return badge;
    }

    private JButton buildMainLoginButton() {
        JButton btn = new JButton("Se connecter") {
            boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACCENT_DIM : hov ? ACCENT_LIGHT : ACCENT;
                if (hov) {
                    g2.setColor(new Color(200, 137, 42, 35));
                    g2.fillRoundRect(-5, 3, getWidth()+10, getHeight()+6, 12, 12);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(4, 2, getWidth()-8, getHeight()/2 - 2, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(0x1c1008));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 46));
        btn.addActionListener(e -> navigateTo(SCREEN_LOGIN));
        return btn;
    }

    private JButton buildSmallAccentButton(String text) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov) {
                    g2.setColor(new Color(200, 137, 42, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(200, 137, 42, 80));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(ACCENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT_LIGHT); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(ACCENT); }
        });
        return btn;
    }

    private JPanel buildWelcomeFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(200, 137, 42, 20));
                g2.drawLine(0, 0, getWidth(), 0);
            }
        };
        f.setOpaque(false);
        String[] items = {"© 2026 UNIV-SCHEDULER", "v1.0", "Système universitaire intégré"};
        for (String item : items) {
            JLabel l = new JLabel(item);
            l.setFont(new Font("Courier New", Font.PLAIN, 10));
            l.setForeground(TEXT_FAINT);
            f.add(l);
        }
        return f;
    }

    // =========================================================================
    //  ÉCRAN EMPLOI DU TEMPS (public)
    // =========================================================================

    private JPanel buildScheduleScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DARK);
        screen.add(buildNavBar("Emplois du temps", "📅"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filters.setOpaque(false);

        String[] filieres = {"Toutes les filières", "Informatique", "Mathématiques", "Physique", "Économie"};
        JComboBox<String> filiereBox = buildStyledCombo(filieres);
        String[] semestres = {"Semestre en cours", "S1", "S2", "S3", "S4"};
        JComboBox<String> semBox = buildStyledCombo(semestres);

        filters.add(new StyledLabel("Filière :", false));
        filters.add(filiereBox);
        filters.add(Box.createHorizontalStrut(8));
        filters.add(new StyledLabel("Semestre :", false));
        filters.add(semBox);

        content.add(filters, BorderLayout.NORTH);

        JPanel grid = buildScheduleGrid();
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));
        content.add(scroll, BorderLayout.CENTER);

        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildScheduleGrid() {
        String[] days  = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
        String[] hours = {"08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00"};
        String[][] courses = {
                {"Algorithmes", "Base de données", "", "Réseaux", "IA"},
                {"Analyse", "Algèbre", "Probabilités", "", "Statistiques"},
                {"", "Physique quantique", "Optique", "Thermodynamique", ""},
                {"Microéconomie", "", "Macroéconomie", "Finance", "Comptabilité"},
                {"POO Java", "Web Dev", "UML", "", "Génie logiciel"}
        };
        Color[] palette = {
                new Color(200, 137, 42, 160),
                new Color(212, 135, 74, 140),
                new Color(160, 96, 32, 150),
        };

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridy = 0; gbc.weightx = 0.08; gbc.weighty = 0.05;
        gbc.gridx = 0;
        grid.add(buildGridCell("", true, false, null), gbc);
        gbc.weightx = 0.184;
        for (String day : days) {
            gbc.gridx++;
            grid.add(buildGridCell(day, true, false, null), gbc);
        }

        Random rand = new Random(42);
        for (int h = 0; h < hours.length; h++) {
            gbc.gridy = h + 1;
            gbc.weighty = 0.1;
            gbc.weightx = 0.08;
            gbc.gridx = 0;
            grid.add(buildGridCell(hours[h], false, true, null), gbc);
            gbc.weightx = 0.184;
            for (int d = 0; d < days.length; d++) {
                gbc.gridx = d + 1;
                String course = h < courses[d].length ? courses[d][h] : "";
                Color c = course.isEmpty() ? null : palette[rand.nextInt(3)];
                grid.add(buildGridCell(course, false, false, c), gbc);
            }
        }
        return grid;
    }

    private JPanel buildGridCell(String text, boolean isHeader, boolean isTime, Color accent) {
        JPanel cell = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (isHeader || isTime) {
                    g2.setColor(new Color(0x2c1a0e));
                } else if (accent != null) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                } else {
                    g2.setColor(new Color(0x120c05));
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (accent != null) {
                    g2.setColor(accent);
                    g2.fillRect(0, 0, 3, getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cell.setOpaque(false);
        if (!text.isEmpty()) {
            JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setFont(isHeader
                    ? new Font("Palatino Linotype", Font.BOLD, 13)
                    : (isTime ? new Font("Courier New", Font.PLAIN, 11) : new Font("Segoe UI", Font.PLAIN, 12)));
            lbl.setForeground(isHeader ? ACCENT : (isTime ? TEXT_MUTED : TEXT_MAIN));
            cell.add(lbl);
        }
        cell.setMinimumSize(new Dimension(isTime ? 60 : 120, 52));
        return cell;
    }

    // =========================================================================
    //  ÉCRAN SALLES (public)
    // =========================================================================

    private JPanel buildRoomsScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DARK);
        screen.add(buildNavBar("Disponibilité des salles", "🏛"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.add(buildLegendItem(new Color(0x2d6a2d), "Disponible"));
        legend.add(buildLegendItem(new Color(0x8a3020), "Occupée"));
        legend.add(buildLegendItem(new Color(0x3d2c00), "Réservée"));
        content.add(legend, BorderLayout.NORTH);

        JPanel roomsGrid = new JPanel(new GridLayout(0, 4, 14, 14));
        roomsGrid.setOpaque(false);
        roomsGrid.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        String[] buildings = {"Bloc A", "Bloc B", "Bloc C", "Amphi"};
        int[] roomCounts = {8, 6, 5, 3};
        Random rand = new Random(7);
        int[] states = {0, 1, 2};
        Color[] stateColors = {new Color(0x2d6a2d), new Color(0x8a3020), new Color(0x3d2c00)};
        String[] stateLabels = {"Libre", "Occupée", "Réservée"};

        for (int b = 0; b < buildings.length; b++) {
            for (int r = 1; r <= roomCounts[b]; r++) {
                int state = states[rand.nextInt(3)];
                roomsGrid.add(buildRoomCard(buildings[b] + " - " + String.format("%02d", r), stateColors[state], stateLabels[state]));
            }
        }

        JScrollPane scroll = new JScrollPane(roomsGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        content.add(scroll, BorderLayout.CENTER);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildRoomCard(String name, Color stateColor, String stateLabel) {
        boolean[] hov = {false};
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov[0] ? new Color(0x2c1a0e) : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hov[0] ? BORDER_CLR : new Color(0x1e1208));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(stateColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(0, 90));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hov[0] = true;  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hov[0] = false; card.repaint(); }
        });

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 14));
        nameLbl.setForeground(TEXT_MAIN);

        JLabel stateLbl = new JLabel("● " + stateLabel);
        stateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stateLbl.setForeground(stateColor.brighter());

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(stateLbl, BorderLayout.WEST);

        card.add(nameLbl, BorderLayout.NORTH);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildLegendItem(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setForeground(color.brighter());
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // =========================================================================
    //  ÉCRAN CALENDRIER (public)
    // =========================================================================

    private JPanel buildCalendarScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DARK);
        screen.add(buildNavBar("Calendrier universitaire", "📆"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(24, 24));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel left = buildMiniCalendar();
        JPanel right = buildEventsList();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setOpaque(false);
        split.setBackground(BG_DARK);
        split.setDividerLocation(320);
        split.setBorder(null);
        split.setDividerSize(8);

        content.add(split, BorderLayout.CENTER);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildMiniCalendar() {
        JPanel cal = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        cal.setOpaque(false);
        cal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel month = new JLabel("Mars 2026", SwingConstants.CENTER);
        month.setFont(new Font("Palatino Linotype", Font.BOLD, 18));
        month.setForeground(ACCENT);
        header.add(month, BorderLayout.CENTER);
        cal.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(7, 7, 4, 4));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        String[] dayNames = {"L", "M", "M", "J", "V", "S", "D"};
        for (String d : dayNames) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Courier New", Font.BOLD, 11));
            l.setForeground(ACCENT);
            grid.add(l);
        }
        int today = 26;
        int[] events = {5, 12, 15, 20, 26, 28};
        for (int day = 1; day <= 31; day++) {
            final int d = day;
            boolean isToday = (d == today);
            boolean hasEvent = false;
            for (int ev : events) if (ev == d) { hasEvent = true; break; }
            JPanel dayCell = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isToday) {
                        g2.setColor(ACCENT);
                        g2.fillOval(1, 1, getWidth()-2, getHeight()-2);
                    } else if (hasEvent) {
                        g2.setColor(new Color(200, 137, 42, 40));
                        g2.fillOval(1, 1, getWidth()-2, getHeight()-2);
                        g2.setColor(new Color(200, 137, 42, 100));
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawOval(1, 1, getWidth()-3, getHeight()-3);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            dayCell.setOpaque(false);
            JLabel dl = new JLabel(String.valueOf(d), SwingConstants.CENTER);
            dl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dl.setForeground(isToday ? BG_DARK : (hasEvent ? ACCENT : TEXT_MUTED));
            if (isToday) dl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dayCell.add(dl);
            grid.add(dayCell);
        }
        cal.add(grid, BorderLayout.CENTER);
        return cal;
    }

    private JPanel buildEventsList() {
        JPanel events = new JPanel();
        events.setOpaque(false);
        events.setLayout(new BoxLayout(events, BoxLayout.Y_AXIS));
        events.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JLabel title = new JLabel("Événements à venir");
        title.setFont(new Font("Palatino Linotype", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        events.add(title);
        events.add(Box.createVerticalStrut(20));

        Object[][] evts = {
                {"05 Mars", "Début des examens partiels",          ACCENT},
                {"12 Mars", "Journée portes ouvertes",             new Color(0xd4874a)},
                {"15 Mars", "Remise des projets M2",               new Color(0xa06020)},
                {"20 Mars", "Conférence IA & Éducation",           ACCENT_LIGHT},
                {"26 Mars", "Aujourd'hui — Conseil pédagogique",   ACCENT_WARM},
                {"28 Mars", "Vacances de printemps",               new Color(0x8a7060)},
        };

        for (Object[] ev : evts) {
            JPanel row = buildEventRow((String) ev[0], (String) ev[1], (Color) ev[2]);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            events.add(row);
            events.add(Box.createVerticalStrut(10));
        }
        return events;
    }

    private JPanel buildEventRow(String date, String label, Color accent) {
        boolean[] hov = {false};
        JPanel row = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov[0] ? new Color(0x2c1a0e) : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 3, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hov[0] = true;  row.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hov[0] = false; row.repaint(); }
        });

        JLabel dateLbl = new JLabel(date);
        dateLbl.setFont(new Font("Courier New", Font.BOLD, 11));
        dateLbl.setForeground(accent);
        dateLbl.setPreferredSize(new Dimension(80, 20));

        JLabel evtLbl = new JLabel(label);
        evtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        evtLbl.setForeground(TEXT_MAIN);

        row.add(dateLbl, BorderLayout.WEST);
        row.add(evtLbl,  BorderLayout.CENTER);
        return row;
    }

    // =========================================================================
    //  ÉCRAN DE CONNEXION
    // =========================================================================

    private JPanel buildLoginScreen() {
        JPanel screen = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), BG_BROWN);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(200, 137, 42, 15));
                g2.fillOval(getWidth()/2 - 250, -120, 500, 340);
            }
        };
        screen.setBackground(BG_DARK);
        screen.add(buildNavBar("Connexion", "🔐"), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(buildLoginCard());
        screen.add(center, BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                LinearGradientPaint topLine = new LinearGradientPaint(
                        40, 0, getWidth()-40, 0,
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{new Color(200, 137, 42, 0), ACCENT, new Color(200, 137, 42, 0)}
                );
                g2.setPaint(topLine);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(40, 0, getWidth()-40, 0);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(44, 48, 44, 48));
        card.setPreferredSize(new Dimension(420, 440));

        JLabel iconLbl = new JLabel("🔐");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLbl);
        card.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("Connexion administrateur");
        title.setFont(new Font("Palatino Linotype", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel sub = new JLabel("Accédez à la gestion complète");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(36));

        card.add(makeFieldLabel("Identifiant"));
        card.add(Box.createVerticalStrut(8));
        fieldId = buildTextField();
        card.add(fieldId);
        card.add(Box.createVerticalStrut(20));

        card.add(makeFieldLabel("Mot de passe"));
        card.add(Box.createVerticalStrut(8));
        fieldMdp = buildPasswordField();
        card.add(fieldMdp);
        card.add(Box.createVerticalStrut(12));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(20));

        loginBtn = new JButton("Se connecter") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = loading ? ACCENT_DIM :
                        getModel().isPressed()  ? ACCENT_DIM :
                                getModel().isRollover() ? ACCENT_LIGHT : ACCENT;
                if (getModel().isRollover()) {
                    g2.setColor(new Color(200, 137, 42, 30));
                    g2.fillRoundRect(-4, 3, getWidth()+8, getHeight()+6, 12, 12);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(4, 2, getWidth()-8, getHeight()/2-2, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setForeground(new Color(0x1c1008));
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> tenterConnexion());
        card.add(loginBtn);

        KeyAdapter enter = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) tenterConnexion();
            }
        };
        fieldId.addKeyListener(enter);
        fieldMdp.addKeyListener(enter);

        return card;
    }

    // =========================================================================
    //  COMPOSANTS COMMUNS
    // =========================================================================

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField buildTextField() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_FIELD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                if (isFocusOwner()) {
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(2, getHeight()-1, 2, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_FIELD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(isFocusOwner() ? ACCENT : BORDER_CLR);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                if (isFocusOwner()) {
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(2, getHeight()-1, 2, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e)   { f.repaint(); }
        });
        return f;
    }

    private JComboBox<String> buildStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBackground(BG_CARD);
        combo.setForeground(TEXT_MAIN);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));
        return combo;
    }

    // =========================================================================
    //  LOGIQUE CONNEXION
    // =========================================================================

    private void tenterConnexion() {
        if (loading) return;
        String id  = fieldId.getText().trim();
        String mdp = new String(fieldMdp.getPassword()).trim();
        if (id.isEmpty() || mdp.isEmpty()) { shakeError("Veuillez remplir tous les champs."); return; }

        loading = true;
        loginBtn.setText("Vérification…");
        loginBtn.repaint();
        errorLabel.setText(" ");

        SwingWorker<Utilisateur, Void> worker = new SwingWorker<>() {
            @Override protected Utilisateur doInBackground() { return UnivSchedulerDAO.authentifier(id, mdp); }
            @Override protected void done() {
                loading = false;
                loginBtn.setText("Se connecter");
                loginBtn.repaint();
                try {
                    Utilisateur user = get();
                    if (user != null) { dispose(); new MainFrame(user).setVisible(true); }
                    else { shakeError("Identifiant ou mot de passe incorrect."); fieldMdp.setText(""); }
                } catch (Exception ex) { shakeError("Erreur de connexion à la base de données."); }
            }
        };
        worker.execute();
    }

    private void shakeError(String message) {
        errorLabel.setText(message);
        Point origin = getLocation();
        Timer t = new Timer(28, null);
        int[] c = {0};
        int[] off = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        t.addActionListener(e -> {
            if (c[0] < off.length) setLocation(origin.x + off[c[0]++], origin.y);
            else { setLocation(origin); t.stop(); }
        });
        t.start();
    }

    // =========================================================================
    //  INNER CLASS : Fond animé de l'accueil (particules flottantes)
    // =========================================================================

    static class WelcomeBackground extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private final Random rand = new Random();

        WelcomeBackground() {
            setBackground(BG_DARK);
            for (int i = 0; i < 55; i++) particles.add(new Particle());
            Timer anim = new Timer(30, e -> {
                particles.forEach(p -> p.update(getWidth(), getHeight()));
                repaint();
            });
            anim.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fond dégradé marron/noir (GradientPaint valide : 2 couleurs seulement)
            GradientPaint gp = new GradientPaint(
                    0, 0, BG_DARK,
                    getWidth() * 0.6f, getHeight(), new Color(0x1c1008)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(200, 137, 42, 18));
            g2.fillOval(-60, getHeight()/2 - 200, 400, 400);

            g2.setColor(new Color(255, 107, 53, 12));
            g2.fillOval(getWidth() - 300, -80, 450, 450);

            for (Particle p : particles) p.draw(g2);

            g2.setColor(new Color(200, 137, 42, 8));
            g2.setStroke(new BasicStroke(80f));
            g2.drawLine(0, getHeight(), getWidth() / 2, 0);

            g2.setColor(new Color(12, 7, 5, 160));
            g2.fillRect(getWidth() * 55 / 100, 0, getWidth(), getHeight());
        }

        static class Particle {
            float x, y, vx, vy, size, alpha;
            Color color;
            static Random r = new Random();

            Particle() { reset(800, 600); }

            void reset(int w, int h) {
                x     = r.nextFloat() * w;
                y     = r.nextFloat() * h;
                vx    = (r.nextFloat() - 0.5f) * 0.4f;
                vy    = -r.nextFloat() * 0.5f - 0.1f;
                size  = r.nextFloat() * 3 + 1f;
                alpha = r.nextFloat() * 0.5f + 0.1f;
                int choice = r.nextInt(3);
                color = choice == 0 ? new Color(200, 137, 42)
                        : choice == 1 ? new Color(255, 107, 53)
                        :               new Color(180, 120, 60);
            }

            void update(int w, int h) {
                x += vx; y += vy;
                alpha -= 0.002f;
                if (y < -10 || alpha <= 0) reset(w, h);
            }

            void draw(Graphics2D g2) {
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        Math.max(0, Math.min(255, (int)(alpha * 255)))));
                g2.fillOval((int)x, (int)y, (int)size, (int)size);
            }
        }
    }

    static class StyledLabel extends JLabel {
        StyledLabel(String text, boolean accent) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(accent ? ACCENT : TEXT_MUTED);
        }
    }

    // =========================================================================
    //  MAIN
    // =========================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
    private JPanel buildRoomsScreen() { return new JPanel(); }
    private JPanel buildCalendarScreen() { return new JPanel(); }
    private JPanel buildLoginScreen() { return new JPanel(); } ;

}
