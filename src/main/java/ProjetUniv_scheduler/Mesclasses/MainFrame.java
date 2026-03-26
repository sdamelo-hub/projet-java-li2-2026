package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * UNIV-SCHEDULER — MainFrame (après connexion)
 * Sidebar moderne + Dashboard + Navigation avec retour possible
 */
public class MainFrame extends JFrame {

    // Couleurs héritées
    private static final Color BG_DARK      = LoginFrame.BG_DARK;
    private static final Color BG_BROWN     = LoginFrame.BG_BROWN;
    private static final Color BG_MID       = LoginFrame.BG_MID;
    private static final Color BG_CARD      = LoginFrame.BG_CARD;
    private static final Color BORDER_CLR   = LoginFrame.BORDER_CLR;
    private static final Color ACCENT       = LoginFrame.ACCENT;
    private static final Color ACCENT_LIGHT = LoginFrame.ACCENT_LIGHT;
    private static final Color ACCENT_DIM   = LoginFrame.ACCENT_DIM;
    private static final Color ACCENT_WARM  = LoginFrame.ACCENT_WARM;
    private static final Color TEXT_MAIN    = LoginFrame.TEXT_MAIN;
    private static final Color TEXT_MUTED   = LoginFrame.TEXT_MUTED;
    private static final Color TEXT_FAINT   = LoginFrame.TEXT_FAINT;

    private final Utilisateur user;
    private JPanel contentArea;
    private CardLayout contentLayout;
    private String activeNav = "dashboard";

    // Sidebar items
    private static final String[][] NAV_ITEMS = {
            {"dashboard", "⊟", "Tableau de bord"},
            {"schedule",  "📅", "Emplois du temps"},
            {"rooms",     "🏛", "Salles"},
            {"students",  "👥", "Étudiants"},
            {"calendar",  "📆", "Calendrier"},
            {"reports",   "📊", "Rapports"},
            {"settings",  "⚙", "Paramètres"},
    };

    public MainFrame(Utilisateur user) {
        this.user = user;
        setTitle("UNIV-SCHEDULER — " + (user != null ? user.getNom() : "Admin"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);

        // ── Top bar ─────────────────────────────────────────────────────
        root.add(buildTopBar(), BorderLayout.NORTH);

        // ── Sidebar + Contenu ────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(BG_DARK);
        body.add(buildSidebar(), BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentArea   = new JPanel(contentLayout);
        contentArea.setBackground(BG_DARK);

        // Panels de contenu
        contentArea.add(buildDashboard(),       "dashboard");
        contentArea.add(buildSchedulePanel(),   "schedule");
        contentArea.add(buildRoomsPanel(),      "rooms");
        contentArea.add(buildStudentsPanel(),   "students");
        contentArea.add(buildCalendarPanel(),   "calendar");
        contentArea.add(buildReportsPanel(),    "reports");
        contentArea.add(buildSettingsPanel(),   "settings");

        contentLayout.show(contentArea, "dashboard");
        body.add(contentArea, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        add(root);
    }

    // =========================================================================
    //  TOP BAR
    // =========================================================================

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(10, 7, 5, 250));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Ligne ambre en bas
                g2.setPaint(new LinearGradientPaint(0, 0, getWidth(), 0,
                        new float[]{0f, 0.3f, 0.7f, 1f},
                        new Color[]{new Color(200, 137, 42, 0), ACCENT, ACCENT, new Color(200, 137, 42, 0)}
                ));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        bar.setPreferredSize(new Dimension(0, 58));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        JLabel logoIcon = new JLabel("⊟");
        logoIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        logoIcon.setForeground(ACCENT);
        JLabel logoText = new JLabel("UNIV-SCHEDULER");
        logoText.setFont(new Font("Palatino Linotype", Font.BOLD, 16));
        logoText.setForeground(TEXT_MAIN);
        logo.add(logoIcon);
        logo.add(logoText);
        bar.add(logo, BorderLayout.WEST);

        // Recherche rapide au centre
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        searchWrap.setOpaque(false);
        JTextField search = new JTextField("🔍  Rechercher…") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1f1309));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0x3d2410));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        search.setOpaque(false);
        search.setForeground(TEXT_FAINT);
        search.setCaretColor(ACCENT);
        search.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        search.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        search.setPreferredSize(new Dimension(280, 32));
        search.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (search.getText().contains("Rechercher")) { search.setText(""); search.setForeground(TEXT_MAIN); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (search.getText().isEmpty()) { search.setText("🔍  Rechercher…"); search.setForeground(TEXT_FAINT); }
            }
        });
        searchWrap.add(search);
        bar.add(searchWrap, BorderLayout.CENTER);

        // Droite : infos utilisateur + déconnexion
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        // Notification bell
        JLabel bell = new JLabel("🔔");
        bell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        bell.setForeground(TEXT_MUTED);
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        right.add(bell);

        // Avatar utilisateur
        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2c1a0e));
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(34, 34));
        String initials = user != null && user.getNom() != null
                ? user.getNom().substring(0, Math.min(2, user.getNom().length())).toUpperCase()
                : "AD";
        JLabel initLbl = new JLabel(initials);
        initLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 12));
        initLbl.setForeground(ACCENT);
        avatar.add(initLbl);
        right.add(avatar);

        JLabel userName = new JLabel(user != null ? user.getNom() : "Administrateur");
        userName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userName.setForeground(TEXT_MUTED);
        right.add(userName);

        // Séparateur
        JLabel sep = new JLabel("|");
        sep.setForeground(TEXT_FAINT);
        right.add(sep);

        // Déconnexion
        JButton logout = new JButton("Déconnexion") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getModel().isRollover()) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(200, 137, 42, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logout.setForeground(TEXT_MUTED);
        logout.setBorderPainted(false);
        logout.setContentAreaFilled(false);
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { logout.setForeground(ACCENT); }
            @Override public void mouseExited(MouseEvent e)  { logout.setForeground(TEXT_MUTED); }
        });
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        right.add(logout);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    //  SIDEBAR
    // =========================================================================

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x120c05), 0, getHeight(), BG_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Ligne droite
                g2.setColor(new Color(0x3d2410));
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        // Section label
        JLabel sectionLbl = new JLabel("NAVIGATION");
        sectionLbl.setFont(new Font("Courier New", Font.BOLD, 9));
        sectionLbl.setForeground(TEXT_FAINT);
        sectionLbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 0));
        sectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sectionLbl);

        // Items de navigation
        for (String[] item : NAV_ITEMS) {
            sidebar.add(buildSidebarItem(item[0], item[1], item[2]));
            if (item[0].equals("schedule")) sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Séparateur
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        // Profil en bas
        sidebar.add(buildSidebarProfile());

        return sidebar;
    }

    private JPanel buildSidebarItem(String id, String icon, String label) {
        boolean[] active = {id.equals(activeNav)};
        boolean[] hov    = {false};

        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active[0]) {
                    g2.setColor(new Color(200, 137, 42, 25));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    // Barre gauche accent
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 4, 3, getHeight()-8, 3, 3);
                } else if (hov[0]) {
                    g2.setColor(new Color(200, 137, 42, 12));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLbl.setForeground(active[0] ? ACCENT : TEXT_MUTED);
        iconLbl.setPreferredSize(new Dimension(22, 22));

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", active[0] ? Font.BOLD : Font.PLAIN, 13));
        nameLbl.setForeground(active[0] ? TEXT_MAIN : TEXT_MUTED);

        item.add(iconLbl);
        item.add(nameLbl);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!active[0]) { hov[0] = true; nameLbl.setForeground(TEXT_MAIN); item.repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                hov[0] = false;
                nameLbl.setForeground(active[0] ? TEXT_MAIN : TEXT_MUTED);
                item.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                activeNav = id;
                // Reset all items et refresh
                contentLayout.show(contentArea, id);
                // Animation flash
                active[0] = true;
                iconLbl.setForeground(ACCENT);
                nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                nameLbl.setForeground(TEXT_MAIN);
                item.repaint();
                // Rafraîchir sidebar
                Container parent = item.getParent();
                if (parent != null) for (Component c : parent.getComponents()) c.repaint();
            }
        });

        return item;
    }

    private JPanel buildSidebarProfile() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1f1309));
                g2.fillRoundRect(8, 0, getWidth()-16, getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(8, 0, getWidth()-17, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mini avatar
        JPanel av = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2c1a0e));
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(ACCENT_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, getWidth()-2, getHeight()-2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        av.setOpaque(false);
        av.setPreferredSize(new Dimension(32, 32));
        String initials = user != null && user.getNom() != null
                ? user.getNom().substring(0, Math.min(2, user.getNom().length())).toUpperCase()
                : "AD";
        JLabel initLbl = new JLabel(initials);
        initLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 11));
        initLbl.setForeground(ACCENT);
        av.add(initLbl);
        p.add(av);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(user != null ? user.getNom() : "Administrateur");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(TEXT_MAIN);
        JLabel roleLbl = new JLabel(user != null ? user.getRole() : "Admin");
        roleLbl.setFont(new Font("Courier New", Font.PLAIN, 10));
        roleLbl.setForeground(ACCENT_DIM);
        info.add(nameLbl);
        info.add(roleLbl);
        p.add(info);

        return p;
    }

    // =========================================================================
    //  DASHBOARD
    // =========================================================================

    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Titre page
        JPanel pageHeader = new JPanel(new BorderLayout());
        pageHeader.setOpaque(false);
        pageHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 28, 0));
        JLabel pageTitle = new JLabel("Tableau de bord");
        pageTitle.setFont(new Font("Palatino Linotype", Font.BOLD, 28));
        pageTitle.setForeground(TEXT_MAIN);
        JLabel pageSubtitle = new JLabel("Vue d'ensemble du système universitaire");
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pageSubtitle.setForeground(TEXT_MUTED);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(pageTitle);
        titleStack.add(pageSubtitle);
        pageHeader.add(titleStack, BorderLayout.WEST);

        // Date
        java.time.LocalDate now = java.time.LocalDate.now();
        JLabel dateLbl = new JLabel(now.getDayOfMonth() + " " +
                now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) +
                " " + now.getYear());
        dateLbl.setFont(new Font("Courier New", Font.PLAIN, 12));
        dateLbl.setForeground(TEXT_FAINT);
        pageHeader.add(dateLbl, BorderLayout.EAST);
        panel.add(pageHeader, BorderLayout.NORTH);

        // Grille principale
        JPanel grid = new JPanel(new BorderLayout(20, 20));
        grid.setOpaque(false);

        // ── Ligne 1 : KPI cards ──────────────────────────────────────────
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(buildKpiCard("👥 Étudiants",   "1 248", "+12 ce mois",  ACCENT));
        kpiRow.add(buildKpiCard("📅 Cours/semaine","87",    "5 filières",   new Color(0xd4874a)));
        kpiRow.add(buildKpiCard("🏛 Salles",       "34",    "28 disponibles", new Color(0xb87333)));
        kpiRow.add(buildKpiCard("⚠ Conflits",      "3",     "À résoudre",   new Color(0xff5545)));

        grid.add(kpiRow, BorderLayout.NORTH);

        // ── Ligne 2 : Chart + activités récentes ─────────────────────────
        JPanel row2 = new JPanel(new GridLayout(1, 2, 20, 0));
        row2.setOpaque(false);
        row2.add(buildOccupancyChart());
        row2.add(buildRecentActivity());

        grid.add(row2, BorderLayout.CENTER);

        // ── Ligne 3 : Prochains cours ────────────────────────────────────
        grid.add(buildUpcomingCourses(), BorderLayout.SOUTH);

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildKpiCard(String title, String value, String sub, Color accent) {
        boolean[] hov = {false};
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov[0] ? new Color(0x2c1a0e) : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(hov[0] ? accent : BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                // Barre couleur haut
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 12, 12);
                // Lueur subtile
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 15));
                g2.fillRect(0, 0, getWidth(), getHeight()/2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        card.setPreferredSize(new Dimension(0, 120));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hov[0] = true;  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hov[0] = false; card.repaint(); }
        });

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 36));
        valueLbl.setForeground(TEXT_MAIN);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Courier New", Font.PLAIN, 10));
        subLbl.setForeground(accent);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(valueLbl);
        south.add(subLbl);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(south,    BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOccupancyChart() {
        JPanel card = buildContentCard("📊 Taux d'occupation des salles");

        // Barres verticales simulées
        JPanel bars = new JPanel(new GridLayout(1, 7, 8, 0)) {
            final int[] values = {72, 45, 88, 60, 95, 30, 55};
            final String[] days = {"L", "M", "M", "J", "V", "S", "D"};

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int bw = (getWidth() - 6 * 8) / 7;
                int maxH = getHeight() - 30;
                for (int i = 0; i < values.length; i++) {
                    int x = i * (bw + 8);
                    int barH = (int)(maxH * values[i] / 100.0);
                    int y = maxH - barH;
                    // Barre de fond
                    g2.setColor(new Color(0x2c1a0e));
                    g2.fillRoundRect(x, 0, bw, maxH, 4, 4);
                    // Barre valeur
                    Color bc = values[i] > 80 ? ACCENT_WARM : values[i] > 50 ? ACCENT : ACCENT_DIM;
                    GradientPaint gp = new GradientPaint(x, y, bc, x, maxH, new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 100));
                    g2.setPaint(gp);
                    g2.fillRoundRect(x, y, bw, barH, 4, 4);
                    // Label jour
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Courier New", Font.PLAIN, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(days[i], x + (bw - fm.stringWidth(days[i]))/2, getHeight()-4);
                    // Valeur
                    g2.setColor(TEXT_MAIN);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String pct = values[i] + "%";
                    g2.drawString(pct, x + (bw - fm.stringWidth(pct))/2, y - 4);
                }
            }
        };
        bars.setOpaque(false);
        bars.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.add(bars, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentActivity() {
        JPanel card = buildContentCard("🕐 Activité récente");
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        Object[][] activities = {
                {"Aujourd'hui 09:42", "Cours ajouté — Algo L3",          ACCENT},
                {"Aujourd'hui 08:15", "Salle B04 réservée — 14h",        new Color(0xd4874a)},
                {"Hier 17:30",        "Emploi du temps modifié — M1",    ACCENT_DIM},
                {"Hier 14:00",        "Étudiant inscrit — Jean Dupont",  new Color(0x8a7060)},
                {"25 mars 11:20",     "Rapport généré — Semestre S2",    ACCENT_LIGHT},
        };

        for (Object[] a : activities) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

            JPanel dotCol = new JPanel(new GridBagLayout());
            dotCol.setOpaque(false);
            dotCol.setPreferredSize(new Dimension(10, 10));
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            dot.setForeground((Color) a[2]);
            dotCol.add(dot);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            JLabel lbl = new JLabel((String) a[1]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(TEXT_MAIN);
            JLabel time = new JLabel((String) a[0]);
            time.setFont(new Font("Courier New", Font.PLAIN, 10));
            time.setForeground(TEXT_FAINT);
            info.add(lbl);
            info.add(time);

            row.add(dotCol, BorderLayout.WEST);
            row.add(info,   BorderLayout.CENTER);

            // Séparateur
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0x1f1309));

            list.add(row);
            list.add(sep);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildUpcomingCourses() {
        JPanel card = buildContentCard("📅 Prochains cours aujourd'hui");
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        Object[][] courses = {
                {"10:00 - 12:00", "Algorithmique avancée", "Salle A02", "L3 Info",    ACCENT},
                {"14:00 - 16:00", "Base de données",       "Salle B11", "M1 Info",    new Color(0xd4874a)},
                {"14:00 - 15:30", "Analyse numérique",     "Amphi 1",   "L2 Maths",   new Color(0xb87333)},
                {"16:00 - 17:30", "Probabilités",          "Salle C05", "L3 Maths",   ACCENT_DIM},
        };

        for (Object[] c : courses) {
            row.add(buildCourseCard((String)c[0], (String)c[1], (String)c[2], (String)c[3], (Color)c[4]));
        }
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCourseCard(String time, String name, String room, String group, Color accent) {
        boolean[] hov = {false};
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov[0] ? new Color(0x2c1a0e) : new Color(0x120c05));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 3, getHeight(), 4, 4);
                if (hov[0]) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fillRect(3, 0, getWidth()-3, getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 14));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hov[0] = true;  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hov[0] = false; card.repaint(); }
        });

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Courier New", Font.BOLD, 11));
        timeLbl.setForeground(accent);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(TEXT_MAIN);

        JLabel roomLbl = new JLabel(room + "  ·  " + group);
        roomLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roomLbl.setForeground(TEXT_MUTED);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(timeLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(nameLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(roomLbl);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    // ── Helper : Carte avec titre ────────────────────────────────────────

    private JPanel buildContentCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 15));
        titleLbl.setForeground(TEXT_MAIN);
        card.add(titleLbl, BorderLayout.NORTH);
        return card;
    }

    // =========================================================================
    //  PANELS STUB (Schedule, Rooms, Students, Calendar, Reports, Settings)
    //  — chacun a son propre contenu minimal mais fonctionnel
    // =========================================================================

    private JPanel buildSchedulePanel() {
        return buildStubPanel("📅 Emplois du temps",
                "Gérez et modifiez les emplois du temps de toutes les filières.",
                new String[]{"Nouveau cours", "Importer", "Exporter PDF"});
    }

    private JPanel buildRoomsPanel() {
        return buildStubPanel("🏛 Gestion des salles",
                "Réservez et gérez la disponibilité des salles et amphithéâtres.",
                new String[]{"Réserver une salle", "Voir le plan", "Rapport d'occupation"});
    }

    private JPanel buildStudentsPanel() {
        return buildStubPanel("👥 Gestion des étudiants",
                "Inscriptions, suivi et gestion des étudiants par filière.",
                new String[]{"Ajouter un étudiant", "Importer CSV", "Générer liste"});
    }

    private JPanel buildCalendarPanel() {
        return buildStubPanel("📆 Calendrier universitaire",
                "Gérez les événements, examens et congés académiques.",
                new String[]{"Ajouter événement", "Synchroniser", "Imprimer"});
    }

    private JPanel buildReportsPanel() {
        return buildStubPanel("📊 Rapports & statistiques",
                "Générez des rapports détaillés sur l'activité universitaire.",
                new String[]{"Rapport hebdo", "Rapport mensuel", "Exporter Excel"});
    }

    private JPanel buildSettingsPanel() {
        return buildStubPanel("⚙ Paramètres du système",
                "Configurez les utilisateurs, rôles et préférences de l'application.",
                new String[]{"Gérer utilisateurs", "Sauvegarder config", "À propos"});
    }

    private JPanel buildStubPanel(String title, String desc, String[] actions) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 28, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Palatino Linotype", Font.BOLD, 26));
        titleLbl.setForeground(TEXT_MAIN);
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLbl.setForeground(TEXT_MUTED);
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(descLbl);
        panel.add(header, BorderLayout.NORTH);

        // Boutons d'action
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btns.setOpaque(false);
        for (String action : actions) {
            JButton btn = new JButton(action) {
                boolean hov = false;
                { addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
                }); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = getModel().isPressed() ? ACCENT_DIM : hov ? ACCENT : new Color(0x2c1a0e);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    if (!hov && !getModel().isPressed()) {
                        g2.setColor(BORDER_CLR);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setForeground(TEXT_MAIN);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(160, 40));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setForeground(BG_DARK); }
                @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_MAIN); }
            });
            btns.add(btn);
        }
        panel.add(btns, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    //  MAIN (TEST)
    // =========================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new MainFrame(null).setVisible(true);
        });
    }
}
