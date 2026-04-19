package ProjetUniv_scheduler;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.web.WebView;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import java.time.LocalTime;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainController {

    @FXML private VBox mainContent;
    @FXML private Button btnLogout;
    @FXML private Button btnAdministration;
    @FXML private Button btnPlanification;
    @FXML private Button btnDashboard;
    @FXML private Button btnMonEDT;
    @FXML private Button btnMonProfil;
    @FXML private Button btnSalles;
    @FXML private Button btnReservations;
    @FXML private Button btnMesCreneaux;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnTrouverSalle;
    @FXML private Button btnNotifications;
    @FXML private VBox sidebar;

    private GestionnaireModule gestionnaireModule = null;
    private static final String BLEU_DEEP  = "#1E2732";
    private static final String VERT_LIME  = "#A3FF33";
    private static final String FOND_BLANC = "#FFFFFF";
    private static final String GRIS_TEXTE = "#64748b";
    PauseTransition pause = new PauseTransition(Duration.seconds(2));

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final CreneauDAO      creneauDAO      = new CreneauDAO(HibernateUtil.getSessionFactory());
    private final ReservationDAO  reservationDAO  = new ReservationDAO(HibernateUtil.getSessionFactory());
    private final SalleDAO        salleDAO        = new SalleDAO();
    private final EquipementDAO   equipementDAO   = new EquipementDAO();
    private final BatimentDAO     batimentDAO     = new BatimentDAO(HibernateUtil.getSessionFactory());
    private final UtilisateurDAO  utilisateurDAO  = new UtilisateurDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO(HibernateUtil.getSessionFactory());
    private final NotificationService notifService = new NotificationService(notificationDAO);
    
    // CORRECTION : Une seule déclaration pour ces variables
    private Utilisateur utilisateurConnecte = null;
    private String      roleConnecte        = null;

    // ── CHAMPS PERSISTANTS (réservation) ──────────────────────────────────────
    private TextField  txtClasse;
    private TextField  txtEffectif;
    private TextField  txtEmail;
    private DatePicker datePicker;
    private FlowPane   containerSalles;

    // ── CALLBACK LOGOUT (branché par LoginController) ─────────────────────────
    private Runnable logoutAction = null;

    public void setLogoutAction(Runnable action) {
        this.logoutAction = action;
    }

    @FXML
    public void initialize() {
        if (sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        }

        showLoginSelection();

        if (btnLogout != null) {
            btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #A3FF33; " +
                               "-fx-border-color: #A3FF33; -fx-border-radius: 5; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("LOG : Déconnexion — Retour à l'état initial (Plein écran).");
        
        if (sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        }

        if (mainContent.getScene() != null) {
            ((Stage) mainContent.getScene().getWindow()).setTitle("UNIV-SCHEDULER");
        }

        showLoginSelection(); 
    }
    
    public void filtrerMenuLateral(String role) {
        if (role == null) return;

        // 1. On active tous les boutons par défaut 
        // (la méthode setBtn sécurise l'appel si un bouton est null)
        setBtn(btnAdministration, true);
        setBtn(btnUtilisateurs,   true);
        setBtn(btnNotifications,  true);
        setBtn(btnPlanification,  true);
        setBtn(btnDashboard,      true); // Correspond à Etat des Salles
        setBtn(btnSalles,         true);
        setBtn(btnReservations,   true);
        setBtn(btnMesCreneaux,    true);
        setBtn(btnMonEDT,         true);
        setBtn(btnTrouverSalle,   true);
        setBtn(btnMonProfil,      true);

        // 2. On désactive ce qui n'est pas nécessaire selon le rôle
        switch (role) {
            case "Administrateur" -> {
                // L'administrateur ne voit QUE Administration et Mon Profil
                setBtn(btnUtilisateurs,   false);
                setBtn(btnNotifications,  false);
                setBtn(btnPlanification,  false);
                setBtn(btnDashboard,      false);
                setBtn(btnSalles,         false);
                setBtn(btnReservations,   false);
                setBtn(btnMesCreneaux,    false);
                setBtn(btnMonEDT,         false);
                setBtn(btnTrouverSalle,   false);
            }
            case "Gestionnaire" -> {
                setBtn(btnAdministration, false);
                setBtn(btnMesCreneaux,    false);
                setBtn(btnMonEDT,         false);
                setBtn(btnTrouverSalle,   false);
            }
            case "Enseignant", "Etudiant" -> {
                setBtn(btnAdministration, false);
                setBtn(btnPlanification,  false);
                setBtn(btnUtilisateurs,   false);
            }
        }
    }
    private void setBtn(Button b, boolean visible) {
        if (b == null) return;
        b.setVisible(visible);
        b.setManaged(visible);
    }
    
    @FXML
    public void showAdminPanel() {
        if (sidebar != null) { sidebar.setVisible(true); sidebar.setManaged(true); }
        filtrerMenuLateral("Administrateur");

        preparerContenu("-fx-background-color: #f0f4f8;");
        mainContent.setPadding(new Insets(0));
        mainContent.setSpacing(0);

        List<Salle>       salles  = salleDAO.findAll();
        List<Utilisateur> users   = utilisateurDAO.findAll();
        List<Equipement>  equips  = equipementDAO.findAll();
        List<Batiment>    bats    = batimentDAO.findAll();

        long sallesDispo  = salles  == null ? 0 : salles.stream().filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle())).count();
        long sallesOcc    = salles  == null ? 0 : salles.stream().filter(s -> "Occupée".equalsIgnoreCase(s.getEtatSalle())).count();
        long sallesMaint  = salles  == null ? 0 : salles.stream().filter(s -> "Maintenance".equalsIgnoreCase(s.getEtatSalle())).count();
        long totalSalles  = salles  == null ? 0 : salles.size();
        long totalUsers   = users   == null ? 0 : users.size();
        long totalEquips  = equips  == null ? 0 : equips.size();
        long equipsKO     = equips  == null ? 0 : equips.stream().filter(e -> e.getEtatFonctionnement() != null && (e.getEtatFonctionnement().contains("panne") || e.getEtatFonctionnement().contains("maintenance"))).count();
        long totalBats    = bats    == null ? 0 : bats.size();
        double tauxOcc    = totalSalles > 0 ? (sallesOcc * 100.0 / totalSalles) : 0;

        HBox topBand = new HBox();
        topBand.setAlignment(Pos.CENTER_LEFT);
        topBand.setPadding(new Insets(16, 30, 16, 30));
        topBand.setStyle("-fx-background-color: " + BLEU_DEEP + ";");

        VBox titleBox = new VBox(2);
        Label lTitle = new Label("CONSOLE DE SUPERVISION");
        lTitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: " + VERT_LIME + ";");
        Label lSub = new Label("Vue d'ensemble — Mise à jour en temps réel");
        lSub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        titleBox.getChildren().addAll(lTitle, lSub);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lTime = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm")));
        lTime.setStyle("-fx-text-fill: " + VERT_LIME + "88; -fx-font-family: 'Consolas'; -fx-font-size: 12;");

        topBand.getChildren().addAll(titleBox, sp, lTime);

        VBox scrollContent = new VBox(28);
        scrollContent.setPadding(new Insets(28, 30, 40, 30));

        HBox kpiRow = new HBox(16);
        kpiRow.getChildren().addAll(
            creerKpiCard("🏢", "SALLES TOTALES",   String.valueOf(totalSalles), VERT_LIME,   "+" + totalBats + " bâtiments"),
            creerKpiCard("✅", "DISPONIBLES",       String.valueOf(sallesDispo), VERT_LIME,   String.format("%.0f%% du réseau", totalSalles > 0 ? sallesDispo*100.0/totalSalles : 0)),
            creerKpiCard("🔴", "OCCUPEES",          String.valueOf(sallesOcc),   VERT_LIME,   String.format("Taux : %.1f%%", tauxOcc)),
            creerKpiCard("🔧", "MAINTENANCE",       String.valueOf(sallesMaint), VERT_LIME,   "Intervention requise"),
            creerKpiCard("👤", "UTILISATEURS",      String.valueOf(totalUsers),  VERT_LIME,   "Actifs dans le système"),
            creerKpiCard("⚙️", "EQUIPEMENTS",       String.valueOf(totalEquips), VERT_LIME,   equipsKO + " en panne")
        );

        HBox chartsRow = new HBox(20);
        chartsRow.setPrefHeight(320);

        // -------------------------------------------------------------------------
        // 1. GRAPHIQUE BARRES (INFRASTRUCTURE GLOBALE) avec Chart.js via WebView
        // -------------------------------------------------------------------------
        WebView barWebView = new WebView();
        barWebView.setPrefWidth(520);
        barWebView.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        
        String htmlBarTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { font-family: 'Consolas', monospace; background-color: white; margin: 0; padding: 15px; overflow: hidden; }
                    .chart-container { position: relative; height: 280px; width: 100%%; }
                    h3 { color: %s; text-align: center; margin-top: 0; font-size: 16px; }
                </style>
            </head>
            <body>
                <h3>INFRASTRUCTURE GLOBALE</h3>
                <div class="chart-container">
                    <canvas id="barChart"></canvas>
                </div>
                <script>
                    const ctx = document.getElementById('barChart').getContext('2d');
                    new Chart(ctx, {
                        type: 'bar',
                        data: {
                            labels: ['Bâtiments', 'Salles', 'Disponibles', 'Occupées', 'Maintenance', 'Utilisateurs', 'Équipements', 'Équip. KO'],
                            datasets: [{
                                data: [%d, %d, %d, %d, %d, %d, %d, %d],
                                backgroundColor: '%s',
                                borderRadius: 6,
                                borderSkipped: false
                            }]
                        },
                        options: {
                            responsive: true, maintainAspectRatio: false,
                            plugins: { legend: { display: false } },
                            scales: {
                                y: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: { color: '#64748b' } },
                                x: { grid: { display: false }, ticks: { color: '#64748b', maxRotation: 45, minRotation: 45 } }
                            }
                        }
                    });
                </script>
            </body>
            </html>
            """;

        String htmlBar = String.format(htmlBarTemplate, 
            BLEU_DEEP, 
            totalBats, totalSalles, sallesDispo, sallesOcc, sallesMaint, totalUsers, totalEquips, equipsKO,
            VERT_LIME 
        );
        barWebView.getEngine().loadContent(htmlBar);

        // -------------------------------------------------------------------------
        // 2. GRAPHIQUE CIRCULAIRE (EQUIPEMENTS) avec Chart.js via WebView
        // -------------------------------------------------------------------------
        WebView pieWebView = new WebView();
        pieWebView.setPrefWidth(280);
        pieWebView.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        String htmlPieTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { font-family: 'Consolas', monospace; background-color: white; margin: 0; padding: 15px; overflow: hidden; }
                    .chart-container { position: relative; height: 240px; width: 100%%; display: flex; justify-content: center; }
                    h3 { color: %s; text-align: center; margin-top: 0; margin-bottom: 15px; font-size: 16px; }
                </style>
            </head>
            <body>
                <h3>EQUIPEMENTS</h3>
                <div class="chart-container">
                    <canvas id="pieChart"></canvas>
                </div>
                <script>
                    const ctx = document.getElementById('pieChart').getContext('2d');
                    new Chart(ctx, {
                        type: 'pie',
                        data: {
                            labels: ['Opérationnels', 'En panne/maint.'],
                            datasets: [{
                                data: [%d, %d],
                                backgroundColor: ['%s', '#ffffff'],
                                borderColor: ['#ffffff', '%s'],
                                hoverOffset: 4,
                                borderWidth: 2
                            }]
                        },
                        options: {
                            responsive: true, maintainAspectRatio: false,
                            plugins: {
                                legend: { position: 'bottom', labels: { color: '#64748b', boxWidth: 12 } }
                            }
                        }
                    });
                </script>
            </body>
            </html>
            """;

        long equipsOperationnels = totalEquips - equipsKO;
        String htmlPie = String.format(htmlPieTemplate, 
            BLEU_DEEP, 
            equipsOperationnels, equipsKO, 
            VERT_LIME, VERT_LIME
        );
        pieWebView.getEngine().loadContent(htmlPie);

        chartsRow.getChildren().addAll(barWebView, pieWebView);

        Label lNav = new Label("ACCES RAPIDE AUX MODULES");
        lNav.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " + GRIS_TEXTE + ";");

        HBox navRow = new HBox(20);
        navRow.getChildren().addAll(
            creerTuileNav("👤 UTILISATEURS",   "Accès & Privilèges",    VERT_LIME, this::showUserManagement),
            creerTuileNav("🏢 INFRASTRUCTURE", "Salles & Bâtiments",    VERT_LIME, this::showSallesManagement),
            creerTuileNav("⚙️ INVENTAIRE",     "Equipements & IoT",      VERT_LIME, this::showInventoryManagement),
            creerTuileNav("🔔 NOTIFICATIONS",  "Historique des alertes",VERT_LIME, this::showNotifications)
        );

        // RÉINTÉGRATION DU BLOC IA ICI !
        VBox aiBlock = construireBlockIA();
        
        scrollContent.getChildren().addAll(kpiRow, chartsRow, lNav, navRow, aiBlock);

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f0f4f8;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        mainContent.getChildren().addAll(topBand, scroll);
    }
    
    public void setUtilisateurConnecte(Utilisateur u) {
        this.utilisateurConnecte = u;
        this.gestionnaireModule  = null; 
    }

    private VBox creerKpiCard(String icon, String titre, String valeur, String couleur, String detail) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setPrefWidth(170);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4); " +
                      "-fx-border-left-width: 4; -fx-border-color: transparent transparent transparent " + couleur + "; " +
                      "-fx-border-radius: 0 16 16 0;");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label lIcon = new Label(icon);
        lIcon.setStyle("-fx-font-size: 18;");
        Label lTitre = new Label(titre);
        lTitre.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        lTitre.setWrapText(true);
        topRow.getChildren().addAll(lIcon, lTitre);

        Label lVal = new Label(valeur);
        lVal.setStyle("-fx-font-size: 38; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Consolas';");

        Label lDet = new Label(detail);
        lDet.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 10; -fx-font-weight: bold;");

        card.getChildren().addAll(topRow, lVal, lDet);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + couleur + "15; -fx-background-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, " + couleur + ", 14, 0, 0, 0); " +
            "-fx-border-left-width: 4; -fx-border-color: transparent transparent transparent " + couleur + "; " +
            "-fx-border-radius: 0 16 16 0;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4); " +
            "-fx-border-left-width: 4; -fx-border-color: transparent transparent transparent " + couleur + "; " +
            "-fx-border-radius: 0 16 16 0;"));

        return card;
    }

    private VBox creerTuileNav(String titre, String desc, String couleur, Runnable action) {
        VBox tile = new VBox(10);
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setPadding(new Insets(22, 24, 22, 24));
        tile.setPrefWidth(220);
        tile.setCursor(Cursor.HAND);

        String base = "-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 14; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);";
        tile.setStyle(base);

        Label lT = new Label(titre);
        lT.setStyle("-fx-text-fill: " + couleur + "; -fx-font-family: 'Consolas'; " +
                    "-fx-font-weight: bold; -fx-font-size: 12;");
        Label lD = new Label(desc);
        lD.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        Region accent = new Region();
        accent.setPrefSize(36, 3); accent.setMaxWidth(36);
        accent.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 2;");

        tile.getChildren().addAll(lT, accent, lD);
        tile.setOnMouseClicked(e -> action.run());
        tile.setOnMouseEntered(e -> { tile.setStyle(base + "-fx-border-color: " + couleur + "; -fx-border-width: 1.5; -fx-border-radius: 14;"); tile.setTranslateY(-4); });
        tile.setOnMouseExited(e  -> { tile.setStyle(base); tile.setTranslateY(0); });

        return tile;
    }

    @FXML
    public void showManagerDashboard() {
        if (sidebar != null) { sidebar.setVisible(true); sidebar.setManaged(true); }
        filtrerMenuLateral("Gestionnaire");

        // Nettoyage et préparation du fond
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");

        // Barre supérieure avec bouton de déconnexion
        HBox topBar = construireTopBar(null, "⏻ DECONNEXION", 0);

        // En-tête personnalisé avec le nom de l'utilisateur
        String nom = utilisateurConnecte != null && utilisateurConnecte.getNom() != null 
                     ? utilisateurConnecte.getNom().toUpperCase() : "GESTIONNAIRE";
                     
        VBox header = construireHeader(
            "PORTAIL GESTIONNAIRE",
            "PLANIFICATION ET EMPLOIS DU TEMPS · Bienvenue, " + nom
        );

        // Ligne 1 des modules
        HBox row1 = new HBox(25);
        row1.setPadding(new Insets(35, 0, 0, 0));
        row1.getChildren().addAll(
            creerCarteAction("📅 GESTION DES COURS", 
                "Création, modification et suppression des unités d'enseignement.", 
                e -> moduleGestionCours()),
                
            creerCarteAction("🔑 ASSIGNATION SALLES", 
                "Lier les cours aux infrastructures physiques disponibles.", 
                e -> moduleAssignationSalles()),
                
            creerCarteAction("⚠️ RESOUDRE CONFLITS", 
                "Détection et correction IA des chevauchements d'horaires.", 
                e -> moduleResolutionConflits())
        );

        // Ligne 2 des modules (Sans les réservations)
        HBox row2 = new HBox(25);
        row2.setPadding(new Insets(15, 0, 0, 0));
        row2.getChildren().addAll(
            creerCarteAction("📄 GENERER PLANNING", 
                "Compilation finale et export des données (PDF, Excel).", 
                e -> moduleGenerationPlanning()),
                
            creerCarteAction("🏢 ETAT DES SALLES", 
                "Surveillance du réseau de salles en temps réel.", 
                e -> showDashboard())
        );

        mainContent.getChildren().addAll(topBar, header, row1, row2);
    }
    @FXML
    public void showTeacherView(Utilisateur enseignant) {
        if (sidebar != null) { sidebar.setVisible(true); sidebar.setManaged(true); }
        filtrerMenuLateral("Enseignant");

        utilisateurConnecte = enseignant;
        roleConnecte        = "Enseignant";

        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");

        HBox topBar = construireTopBar(null, "⏻ LOGOUT", 0);

        VBox header = construireHeader(
            "PORTAIL ENSEIGNANT",
            "ACCES PEDAGOGIQUE · Bienvenue, "
                + (enseignant != null ? enseignant.getNom().toUpperCase() : "ENSEIGNANT")
        );

        HBox modules = new HBox(30);
        modules.setPadding(new Insets(35, 0, 0, 0));
        modules.setAlignment(Pos.TOP_LEFT);

        modules.getChildren().addAll(
            creerCarteAction("📅 RESERVER UNE SALLE",
                "Localiser une unité libre et planifier une session.",
                e -> showReservationsEnseignant(enseignant)),

            creerCarteAction("🏢 ETAT DES UNITES",
                "Consulter l'occupation du réseau en temps réel.",
                e -> showSallesEnseignant(enseignant)),

            creerCarteAction("📋 MES CRENEAUX",
                "Voir les réservations validées sur votre nom.",
                e -> showCreneauxEnseignant(enseignant)),

            creerCarteAction("⚠️ SIGNALER INCIDENT",
                "Rapport technique sur une salle ou un équipement défectueux.",
                e -> showSignalerIncidentDialog(enseignant))
        );

        mainContent.getChildren().addAll(topBar, header, modules);
    }

    private void showSallesEnseignant(Utilisateur enseignant) {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        Button retour = btnRetour(() -> showTeacherView(enseignant));
        Label titre = creerTitre("RESEAU DES LOCAUX — ETAT EN TEMPS REEL");

        TextField search = new TextField();
        search.setPromptText("🔍 Rechercher une salle ou un bâtiment...");
        search.setPrefWidth(480);
        search.setStyle("-fx-padding: 10; -fx-background-radius: 20; -fx-border-color: " + BLEU_DEEP + "; -fx-border-radius: 20;");

        containerSalles = new FlowPane(20, 20);
        containerSalles.setPadding(new Insets(15, 0, 0, 0));
        rafraichirListeSalles(true, "");
        search.textProperty().addListener((obs, o, n) -> rafraichirListeSalles(true, n));

        ScrollPane scroll = creerScrollPane(containerSalles, 520);
        mainContent.getChildren().addAll(retour, titre, search, scroll);
    }

    private void showReservationsEnseignant(Utilisateur enseignant) {
        showReservations();
        mainContent.getChildren().add(0, btnRetour(() -> showTeacherView(enseignant)));
    }

    @FXML
    public void showMonPlanning(Utilisateur etudiant) {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        
        String nom = (etudiant != null) ? etudiant.getNom().toUpperCase() : "MON";
        HBox topBar = construireTopBar("EMPLOI DU TEMPS | " + nom, null, 0);

        VBox planningBox = new VBox(20);
        planningBox.setAlignment(Pos.CENTER);
        planningBox.setPadding(new Insets(50));
        
        Label info = new Label("📅 Calendrier hebdomadaire en cours de chargement...");
        info.setStyle("-fx-font-size: 18; -fx-text-fill: " + BLEU_DEEP + ";");
        
        planningBox.getChildren().add(info);
        mainContent.getChildren().addAll(topBar, planningBox);
    }
    
    @FXML
    public void showStudentView(Utilisateur etudiant) {
        if (sidebar != null) { sidebar.setVisible(true); sidebar.setManaged(true); }
        filtrerMenuLateral("Etudiant");
        preparerContenu("-fx-background-color: #f0f4f8;");
        mainContent.setPadding(new Insets(0));
        mainContent.setSpacing(0);

        HBox topBand = new HBox();
        topBand.setAlignment(Pos.CENTER_LEFT);
        topBand.setPadding(new Insets(14, 28, 14, 28));
        topBand.setStyle("-fx-background-color: " + BLEU_DEEP + ";");

        VBox titreBox = new VBox(2);
        Label lNom = new Label("PORTAIL ETUDIANT");
        lNom.setStyle("-fx-font-family:'Consolas'; -fx-font-size:20; -fx-font-weight:bold; -fx-text-fill:" + VERT_LIME + ";");
        String nomAffiche = etudiant != null
            ? etudiant.getNom().toUpperCase() + (etudiant.getPrenom() != null ? " " + etudiant.getPrenom() : "")
            : "ETUDIANT";
        Label lSub = new Label("Bienvenue, " + nomAffiche + "  ·  " +
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH)));
        lSub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");
        titreBox.getChildren().addAll(lNom, lSub);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnLogoutBand = new Button("⏻ DECONNEXION");
        btnLogoutBand.setStyle("-fx-background-color:transparent; -fx-text-fill:#ef4444; -fx-font-weight:bold; -fx-cursor:hand;");
        btnLogoutBand.setOnAction(e -> showLoginSelection());
        topBand.getChildren().addAll(titreBox, sp, btnLogoutBand);

        VBox scrollContent = new VBox(26);
        scrollContent.setPadding(new Insets(26, 28, 40, 28));

        List<Salle> toutesSalles = salleDAO.findAll();
        long sallesLibres = toutesSalles == null ? 0 :
            toutesSalles.stream().filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle())).count();
        long sallesTotal  = toutesSalles == null ? 0 : toutesSalles.size();
        List<Notification> mesNotifs = notificationDAO.findAll();
        long notifNonLues = mesNotifs == null ? 0 :
            mesNotifs.stream().filter(n -> n.getStatut() != null && n.getStatut().equalsIgnoreCase("NON_LU")).count();

        HBox kpiRow = new HBox(16);
        kpiRow.getChildren().addAll(
            creerKpiEtudiant("🏢", "SALLES LIBRES",    String.valueOf(sallesLibres), VERT_LIME,   "sur " + sallesTotal + " au total"),
            creerKpiEtudiant("📅", "COURS AUJOURD'HUI","0",                          "#818cf8",   "Données EDT à connecter"),
            creerKpiEtudiant("🔔", "NOTIFICATIONS",     String.valueOf(notifNonLues), "#facc15",   "non lues"),
            creerKpiEtudiant("📋", "MES RESERVATIONS", "0",                          "#38bdf8",   "salles d'étude")
        );

        VBox planningBlock = construireApercuPlanningEtudiant(etudiant);

        Label lNav = new Label("MES ACCES RAPIDES");
        lNav.setStyle("-fx-font-family:'Consolas'; -fx-font-size:13; -fx-font-weight:bold; -fx-text-fill:" + GRIS_TEXTE + ";");

        GridPane modulesGrid = new GridPane();
        modulesGrid.setHgap(16); modulesGrid.setVgap(16);

        modulesGrid.add(creerTuileEtudiant("📅 EMPLOI DU TEMPS",
            "Planning de la semaine",   "#818cf8",
            () -> showEdtEtudiant(etudiant)), 0, 0);

        modulesGrid.add(creerTuileEtudiant("🔍 TROUVER UNE SALLE",
            "Moteur de recherche smart","#22c55e",
            () -> showRechercheEtudiant(etudiant)), 1, 0);

        modulesGrid.add(creerTuileEtudiant("📚 MES RESERVATIONS",
            "Mes salles d'étude",       "#38bdf8",
            () -> showMesReservationsEtudiant(etudiant)), 2, 0);

        modulesGrid.add(creerTuileEtudiant("🔔 NOTIFICATIONS",
            "Alertes & confirmations",  "#facc15",
            () -> showNotificationsEtudiant(etudiant)), 0, 1);

        modulesGrid.add(creerTuileEtudiant("👤 MON PROFIL",
            "Groupe, promo, infos",     "#f97316",
            () -> showMonProfilEtudiant(etudiant)), 1, 1);

        // CORRECTION: Remplacement de showSallesEtudiant par showSalles
        modulesGrid.add(creerTuileEtudiant("🏢 ETAT DU CAMPUS",
            "Salles & bâtiments",       "#c084fc",
            () -> showSalles()), 2, 1);

        VBox notifBlock = construireApercuNotificationsEtudiant(mesNotifs, etudiant);

        scrollContent.getChildren().addAll(kpiRow, planningBlock, lNav, modulesGrid, notifBlock);

        ScrollPane scroll = creerScrollPane(scrollContent, Double.MAX_VALUE);
        scroll.setFitToHeight(false);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        mainContent.getChildren().addAll(topBand, scroll);
    }

    private void showEdtEtudiant(Utilisateur etudiant) {
        preparerContenu("-fx-background-color: #f8fafc;");
        mainContent.setPadding(new Insets(22, 32, 40, 32));
        mainContent.getChildren().add(btnRetour(() -> showStudentView(etudiant)));

        VBox header = construireHeader("📅 MON EMPLOI DU TEMPS",
            "Semaine du " + lundiDeLaSemaine() + " — Vue par défaut : semaine courante");

        HBox semRow = new HBox(12);
        semRow.setAlignment(Pos.CENTER_LEFT);
        semRow.setPadding(new Insets(12, 0, 16, 0));

        Button btnPrev = new Button("◀ Semaine préc.");
        Button btnNext = new Button("Semaine suiv. ▶");
        styliserBoutonSecondaire(btnPrev);
        styliserBoutonSecondaire(btnNext);
        Label lSem = new Label("Semaine en cours");
        lSem.setStyle("-fx-font-family:'Consolas'; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + "; -fx-font-size:13;");

        Region sepSem = new Region(); HBox.setHgrow(sepSem, Priority.ALWAYS);

        ToggleButton tAll = new ToggleButton("Tous"); tAll.setSelected(true);
        ToggleButton tCM  = new ToggleButton("CM");
        ToggleButton tTD  = new ToggleButton("TD");
        ToggleButton tTP  = new ToggleButton("TP");
        ToggleGroup tg = new ToggleGroup();
        tAll.setToggleGroup(tg); tCM.setToggleGroup(tg);
        tTD.setToggleGroup(tg); tTP.setToggleGroup(tg);
        String tBtnStyle = "-fx-background-color:#e2e8f0; -fx-text-fill:" + BLEU_DEEP + "; -fx-background-radius:6; -fx-cursor:hand; -fx-padding:6 14;";
        String tBtnSelStyle = "-fx-background-color:" + BLEU_DEEP + "; -fx-text-fill:" + VERT_LIME + "; -fx-background-radius:6; -fx-cursor:hand; -fx-font-weight:bold; -fx-padding:6 14;";
        for (ToggleButton tb : new ToggleButton[]{tAll, tCM, tTD, tTP}) {
            tb.setStyle(tBtnStyle);
            tb.selectedProperty().addListener((o, ov, nv) -> tb.setStyle(nv ? tBtnSelStyle : tBtnStyle));
        }
        HBox toggleRow = new HBox(6, tAll, tCM, tTD, tTP);

        semRow.getChildren().addAll(btnPrev, lSem, btnNext, sepSem, toggleRow);

        ScrollPane edtScroll = new ScrollPane(construireGrilleEDT(etudiant));
        edtScroll.setFitToWidth(true);
        edtScroll.setPrefHeight(480);
        edtScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        edtScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        edtScroll.setStyle("-fx-background-color:transparent; -fx-background:#f8fafc; -fx-border-color:transparent;");

        HBox legendeRow = new HBox(20);
        legendeRow.setPadding(new Insets(14, 0, 0, 0));
        legendeRow.setAlignment(Pos.CENTER_LEFT);
        legendeRow.getChildren().addAll(
            creerLegendeBullet("#818cf8", "CM"),
            creerLegendeBullet("#38bdf8", "TD"),
            creerLegendeBullet("#22c55e", "TP"),
            creerLegendeBullet("#facc15", "Examen"),
            creerLegendeBullet("#e2e8f0", "Libre")
        );

        HBox infoBox = new HBox(10);
        infoBox.setPadding(new Insets(14, 18, 14, 18));
        infoBox.setStyle("-fx-background-color:#eff6ff; -fx-border-color:#bfdbfe; -fx-border-radius:10; -fx-background-radius:10;");
        infoBox.setAlignment(Pos.CENTER_LEFT);
        Label icoInfo = new Label("ℹ️"); icoInfo.setStyle("-fx-font-size:16;");
        Label msgInfo = new Label("Les cours s'afficheront automatiquement dès que les créneaux seront saisis par le gestionnaire. Les cours apparaîtront colorés selon leur type (CM, TD, TP).");
        msgInfo.setStyle("-fx-text-fill:#1d4ed8; -fx-font-size:12;");
        msgInfo.setWrapText(true);
        HBox.setHgrow(msgInfo, Priority.ALWAYS);
        infoBox.getChildren().addAll(icoInfo, msgInfo);

        mainContent.getChildren().addAll(header, semRow, edtScroll, legendeRow, infoBox);
    }

    private void showRechercheEtudiant(Utilisateur etudiant) {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(22, 32, 40, 32));
        mainContent.getChildren().add(btnRetour(() -> showStudentView(etudiant)));

        VBox header = construireHeader("🔍 TROUVER UNE SALLE LIBRE",
            "Recherche intelligente en temps réel — Filtres avancés");

        VBox filtersPane = new VBox(16);
        filtersPane.setPadding(new Insets(22, 24, 22, 24));
        filtersPane.setStyle("-fx-background-color:" + BLEU_DEEP + "; -fx-background-radius:18;");

        HBox fl1 = new HBox(20); fl1.setAlignment(Pos.BOTTOM_LEFT);

        TextField fRecherche = new TextField();
        fRecherche.setPromptText("Numéro ou nom de bâtiment...");
        fRecherche.setStyle("-fx-background-color:#1a252f; -fx-text-fill:white; -fx-border-color:#2d3f50; -fx-border-radius:7; -fx-background-radius:7; -fx-padding:9; -fx-font-size:13;");
        fRecherche.setPrefWidth(260);

        TextField fCapacite = new TextField();
        fCapacite.setPromptText("Capacité min.");
        fCapacite.setStyle("-fx-background-color:#1a252f; -fx-text-fill:white; -fx-border-color:#2d3f50; -fx-border-radius:7; -fx-background-radius:7; -fx-padding:9; -fx-font-size:13;");
        fCapacite.setPrefWidth(130);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Tous types", "TD", "TP", "Amphithéâtre");
        cbType.setValue("Tous types");
        cbType.setStyle("-fx-background-color:#1a252f; -fx-text-fill:white; -fx-border-color:#2d3f50; -fx-border-radius:7; -fx-background-radius:7; -fx-font-size:13;");
        cbType.setPrefWidth(160);

        fl1.getChildren().addAll(
            creerGroupeSaisie("🔤 RECHERCHE",    fRecherche, 260),
            creerGroupeSaisie("👥 CAPACITE MIN.", fCapacite,  130),
            creerGroupeSaisie("🏷️ TYPE DE SALLE",  cbType,   160)
        );

        HBox fl2 = new HBox(20); fl2.setAlignment(Pos.BOTTOM_LEFT);

        ComboBox<String> cbBatiment = new ComboBox<>();
        cbBatiment.getItems().add("Tous les bâtiments");
        List<Batiment> bats = batimentDAO.findAll();
        if (bats != null) bats.forEach(b -> cbBatiment.getItems().add(b.getNomBatiment()));
        cbBatiment.setValue("Tous les bâtiments");
        cbBatiment.setStyle("-fx-background-color:#1a252f; -fx-text-fill:white; -fx-border-color:#2d3f50; -fx-border-radius:7; -fx-background-radius:7; -fx-font-size:13;");

        CheckBox cbVP   = new CheckBox("📽️ Vidéoprojecteur");
        CheckBox cbClim = new CheckBox("❄️ Climatisation");
        CheckBox cbWifi = new CheckBox("🌐 Wifi");
        String cbStyle = "-fx-text-fill:white; -fx-font-size:12;";
        cbVP.setStyle(cbStyle); cbClim.setStyle(cbStyle); cbWifi.setStyle(cbStyle);

        HBox equipBox = new HBox(20, cbVP, cbClim, cbWifi);
        equipBox.setAlignment(Pos.CENTER_LEFT);
        equipBox.setPadding(new Insets(8, 0, 0, 0));

        fl2.getChildren().addAll(
            creerGroupeSaisie("🏢 BATIMENT", cbBatiment, 220),
            new VBox(5, new Label("🔌 EQUIPEMENTS"){{setStyle("-fx-text-fill:" + VERT_LIME + "; -fx-font-size:10; -fx-font-weight:bold; -fx-font-family:'Consolas';");}}, equipBox)
        );

        Button btnRechercher = new Button("⚡ RECHERCHER");
        btnRechercher.setPrefHeight(44); btnRechercher.setPadding(new Insets(0, 28, 0, 28));
        styliserBoutonPrimaire(btnRechercher);

        filtersPane.getChildren().addAll(fl1, new Separator(), fl2, btnRechercher);

        Label lResultats = new Label("RESULTATS");
        lResultats.setStyle("-fx-font-family:'Consolas'; -fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:" + GRIS_TEXTE + ";");
        lResultats.setPadding(new Insets(10, 0, 4, 0));

        FlowPane resultPane = new FlowPane(18, 18);
        resultPane.setPadding(new Insets(6, 0, 10, 0));

        Runnable doSearch = () -> {
            resultPane.getChildren().clear();
            String txt = fRecherche.getText().toLowerCase();
            String typeFil = cbType.getValue();
            String batFil  = cbBatiment.getValue();
            int tempCap;
            try { 
                tempCap = Integer.parseInt(fCapacite.getText().trim()); 
            } catch (NumberFormatException e) { 
                tempCap = 0; 
            }
            
            final int capMin = tempCap;

            List<Salle> salles = salleDAO.findAll();
            if (salles == null) return;

            List<Salle> filtered = salles.stream()
                .filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle()))
                .filter(s -> txt.isEmpty()
                    || s.getNumeroSalle().toLowerCase().contains(txt)
                    || (s.getBatiment() != null && s.getBatiment().getNomBatiment().toLowerCase().contains(txt)))
                .filter(s -> s.getCapacite() >= capMin)
                .filter(s -> "Tous types".equals(typeFil) || typeFil.equals(s.getCategorieSalle()))
                .filter(s -> "Tous les bâtiments".equals(batFil)
                    || (s.getBatiment() != null && batFil.equals(s.getBatiment().getNomBatiment())))
                .sorted(Comparator.comparingInt(Salle::getCapacite))
                .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                VBox vide = new VBox(10);
                vide.setAlignment(Pos.CENTER); vide.setPrefWidth(600); vide.setPrefHeight(200);
                Label lV = new Label("Aucune salle ne correspond à vos critères.");
                lV.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:14;");
                Label lH = new Label("💡 Essayez de réduire les filtres.");
                lH.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");
                vide.getChildren().addAll(lV, lH);
                resultPane.getChildren().add(vide);
            } else {
                lResultats.setText(filtered.size() + " SALLE(S) DISPONIBLE(S)");
                filtered.forEach(s -> resultPane.getChildren().add(
                    creerCarteDetailleeSalle(s, etudiant)));
            }
        };

        doSearch.run();
        btnRechercher.setOnAction(e -> doSearch.run());
        fRecherche.textProperty().addListener((o, ov, nv) -> doSearch.run());
        cbType.valueProperty().addListener((o, ov, nv) -> doSearch.run());
        cbBatiment.valueProperty().addListener((o, ov, nv) -> doSearch.run());

        ScrollPane scroll = creerScrollPane(resultPane, 420);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        mainContent.getChildren().addAll(header, filtersPane, lResultats, scroll);
    }

    private void showMesReservationsEtudiant(Utilisateur etudiant) {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(22, 32, 40, 32));
        mainContent.getChildren().add(btnRetour(() -> showStudentView(etudiant)));

        VBox header = construireHeader("📚 MES RESERVATIONS DE SALLES D'ETUDE",
            "Gérez vos sessions de travail en groupe ou individuelle");

        Button btnNouvelle = new Button("+ RESERVER UNE SALLE");
        styliserBoutonPrimaire(btnNouvelle);
        btnNouvelle.setPrefHeight(40);
        btnNouvelle.setOnAction(e -> dialogReserverSalleEtude(etudiant));

        ComboBox<String> cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("Toutes", "En attente", "Validée", "Annulée");
        cbStatut.setValue("Toutes");
        cbStatut.setStyle("-fx-background-radius:8; -fx-font-weight:bold;");

        HBox toolbar = new HBox(16, btnNouvelle, new Region(){{HBox.setHgrow(this,Priority.ALWAYS);}},
            new Label("Filtrer :"){{setStyle("-fx-text-fill:"+GRIS_TEXTE+"; -fx-font-weight:bold;");}}, cbStatut);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 14, 0));

        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(380);
        table.setPlaceholder(new Label("Aucune réservation. Cliquez sur '+ RESERVER UNE SALLE'."));
        table.setStyle("-fx-background-radius:14; -fx-border-radius:14; -fx-border-color:#f1f5f9;");

        TableColumn<Reservation, Integer> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numReservation"));
        colNum.setPrefWidth(60);

        TableColumn<Reservation, LocalDateTime> colDate = new TableColumn<>("DATE RESERVATION");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeureReservation"));
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                setStyle("-fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + ";");
            }
        });
        colDate.setPrefWidth(160);

        TableColumn<Reservation, String> colSalle = new TableColumn<>("SALLE");
        colSalle.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getMonCreneau() != null && cd.getValue().getMonCreneau().getSalle() != null
            ? cd.getValue().getMonCreneau().getSalle().getNumeroSalle() : "—"));
        colSalle.setPrefWidth(90);

        TableColumn<Reservation, String> colNature = new TableColumn<>("NATURE");
        colNature.setCellValueFactory(new PropertyValueFactory<>("natureSession"));
        colNature.setPrefWidth(120);

        TableColumn<Reservation, String> colEtat = new TableColumn<>("ETAT");
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatReservation"));
        colEtat.setPrefWidth(110);
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v.toUpperCase());
                String c = switch (v.toLowerCase()) {
                    case "validée"    -> "#16a34a";
                    case "annulée"    -> "#ef4444";
                    case "en attente" -> "#d97706";
                    default           -> BLEU_DEEP;
                };
                setStyle("-fx-text-fill:" + c + "; -fx-font-weight:bold;");
            }
        });

        TableColumn<Reservation, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button btnAnn = new Button("✗ Annuler");
            { btnAnn.setStyle("-fx-background-color:transparent; -fx-text-fill:#ef4444; -fx-cursor:hand; -fx-font-weight:bold;");
              btnAnn.setOnAction(e -> annulerReservationEtudiant(getTableView().getItems().get(getIndex()), etudiant)); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Reservation r = getTableView().getItems().get(getIndex());
                if ("En attente".equalsIgnoreCase(r.getEtatReservation())) setGraphic(btnAnn);
                else setGraphic(null);
            }
        });

        table.getColumns().addAll(colNum, colDate, colSalle, colNature, colEtat, colActions);

        List<Reservation> toutes = null;
        try {
            ReservationDAO reservDAO = new ReservationDAO(HibernateUtil.getSessionFactory());
            toutes = reservDAO.findAll();
        } catch (Exception ignored) {}

        if (toutes != null) {
            ObservableList<Reservation> obs = FXCollections.observableArrayList(toutes);
            table.setItems(obs);
            cbStatut.valueProperty().addListener((o, ov, nv) -> {
                if ("Toutes".equals(nv)) table.setItems(obs);
                else table.setItems(obs.filtered(r ->
                    r.getEtatReservation() != null &&
                    r.getEtatReservation().equalsIgnoreCase(nv)));
            });
        }

        long total    = toutes == null ? 0 : toutes.size();
        long valides  = toutes == null ? 0 : toutes.stream().filter(r -> "Validée".equalsIgnoreCase(r.getEtatReservation())).count();
        long attente  = toutes == null ? 0 : toutes.stream().filter(r -> "En attente".equalsIgnoreCase(r.getEtatReservation())).count();

        HBox statsRow = new HBox(16);
        statsRow.setPadding(new Insets(4, 0, 14, 0));
        statsRow.getChildren().addAll(
            creerMiniStatBadge("Total",      String.valueOf(total),   "#64748b"),
            creerMiniStatBadge("Validées",   String.valueOf(valides), "#16a34a"),
            creerMiniStatBadge("En attente", String.valueOf(attente), "#d97706")
        );

        mainContent.getChildren().addAll(header, toolbar, statsRow, table);
    }

    private void showNotificationsEtudiant(Utilisateur etudiant) {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(22, 32, 40, 32));
        mainContent.getChildren().add(btnRetour(() -> showStudentView(etudiant)));

        VBox header = construireHeader("🔔 CENTRE DE NOTIFICATIONS",
            "Alertes, confirmations et annonces de l'administration");

        Button btnToutLire = new Button("✓ Tout marquer comme lu");
        btnToutLire.setStyle("-fx-background-color:transparent; -fx-text-fill:" + VERT_LIME + "; -fx-border-color:" + VERT_LIME + "; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand; -fx-font-weight:bold; -fx-padding:8 18;");

        ComboBox<String> cbFiltreNotif = new ComboBox<>();
        cbFiltreNotif.getItems().addAll("Toutes", "Non lues", "Réservations", "Système");
        cbFiltreNotif.setValue("Toutes");
        cbFiltreNotif.setStyle("-fx-background-radius:8; -fx-font-weight:bold;");

        HBox tbNotif = new HBox(16, new Region(){{HBox.setHgrow(this,Priority.ALWAYS);}},
            new Label("Filtre :"){{setStyle("-fx-text-fill:"+GRIS_TEXTE+"; -fx-font-weight:bold;");}},
            cbFiltreNotif, btnToutLire);
        tbNotif.setAlignment(Pos.CENTER_LEFT);
        tbNotif.setPadding(new Insets(8, 0, 14, 0));

        VBox notifList = new VBox(10);
        notifList.setPadding(new Insets(4, 0, 10, 0));

        List<Notification> notifs = notificationDAO.findAll();

        Runnable refreshNotifs = () -> {
            notifList.getChildren().clear();
            String filtre = cbFiltreNotif.getValue();
            if (notifs == null || notifs.isEmpty()) {
                Label vide = new Label("Aucune notification pour le moment.");
                vide.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-padding:30;");
                notifList.getChildren().add(vide);
                return;
            }
            notifs.stream()
                .filter(n -> {
                    if ("Toutes".equals(filtre)) return true;
                    if ("Non lues".equals(filtre)) return "NON_LU".equalsIgnoreCase(n.getStatut());
                    return true;
                })
                .forEach(n -> notifList.getChildren().add(creerCarteNotification(n)));
        };

        refreshNotifs.run();
        cbFiltreNotif.valueProperty().addListener((o, ov, nv) -> refreshNotifs.run());
        btnToutLire.setOnAction(e -> {
            if (notifs != null) notifs.forEach(n -> n.setStatut("LU"));
            refreshNotifs.run();
        });

        ScrollPane scroll = creerScrollPane(notifList, 480);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        mainContent.getChildren().addAll(header, tbNotif, scroll);
    }

    private void showMonProfilEtudiant(Utilisateur etudiant) {
        preparerContenu("-fx-background-color: #f0f4f8;");
        mainContent.setPadding(new Insets(22, 32, 40, 32));
        mainContent.getChildren().add(btnRetour(() -> showStudentView(etudiant)));

        HBox profilCard = new HBox(30);
        profilCard.setPadding(new Insets(30, 32, 30, 32));
        profilCard.setStyle("-fx-background-color:" + BLEU_DEEP + "; -fx-background-radius:22;");
        profilCard.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        Region avBg = new Region(); avBg.setPrefSize(80, 80); avBg.setMaxSize(80, 80);
        avBg.setStyle("-fx-background-color:" + VERT_LIME + "; -fx-background-radius:40;");
        Label avTxt = new Label(etudiant != null && etudiant.getNom() != null
            ? String.valueOf(etudiant.getNom().charAt(0)).toUpperCase() : "E");
        avTxt.setStyle("-fx-font-size:34; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + ";");
        avatar.getChildren().addAll(avBg, avTxt);

        VBox profilInfo = new VBox(6);
        String nomComplet = etudiant != null
            ? (etudiant.getNom() != null ? etudiant.getNom() : "") +
              (etudiant.getPrenom() != null ? " " + etudiant.getPrenom() : "")
            : "Etudiant";
        Label lNomComplet = new Label(nomComplet.trim().toUpperCase());
        lNomComplet.setStyle("-fx-font-family:'Consolas'; -fx-font-size:22; -fx-font-weight:bold; -fx-text-fill:white;");

        String emailAff = etudiant != null && etudiant.getEmail() != null ? etudiant.getEmail() : "—";
        Label lEmail = new Label("✉  " + emailAff);
        lEmail.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13;");

        Label lRole = new Label("🎓  Etudiant");
        lRole.setStyle("-fx-text-fill:" + VERT_LIME + "; -fx-font-weight:bold; -fx-font-size:13;");

        String matricule = etudiant != null ? etudiant.getIdentifiantConnexion() : "—";
        Label lMatricule = new Label("🪪  Matricule : " + matricule);
        lMatricule.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        profilInfo.getChildren().addAll(lNomComplet, lEmail, lRole, lMatricule);
        profilCard.getChildren().addAll(avatar, profilInfo);

        Etudiant etudiantComplet = null;
        if (etudiant != null) {
            try {
                EtudiantDAO etudDAO = new EtudiantDAO(HibernateUtil.getSessionFactory());
                etudiantComplet = (Etudiant) etudDAO.findById(etudiant.getIdentifiantConnexion());
            } catch (Exception ignored) {}
        }

        Groupe groupe = etudiantComplet != null ? etudiantComplet.getGroupe() : null;
        Promotion promo = groupe != null ? groupe.getPromotion() : null;

        HBox infoCardsRow = new HBox(16);
        infoCardsRow.setPadding(new Insets(20, 0, 0, 0));
        infoCardsRow.getChildren().addAll(
            creerInfoCard("🏫 GROUPE",
                groupe != null ? "Groupe " + groupe.getNumeroGroupe() : "Non assigné",
                groupe != null ? groupe.getSpecialiteGroupe() : "—",
                groupe != null ? groupe.getModaliteGroupe() : "—"),
            creerInfoCard("📋 PROMOTION",
                promo != null ? promo.getCodePromotion() : "—",
                promo != null ? promo.getNiveau() : "—",
                promo != null ? promo.getAnneeAcademique() : "—"),
            creerInfoCard("📊 STATISTIQUES",
                "0 cours suivis",
                "0 réservations",
                "0 incidents signalés")
        );

        VBox securityCard = new VBox(14);
        securityCard.setPadding(new Insets(22, 24, 22, 24));
        securityCard.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),10,0,0,3);");

        Label lSec = new Label("🔒 SECURITE DU COMPTE");
        lSec.setStyle("-fx-font-family:'Consolas'; -fx-font-size:15; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + ";");

        PasswordField fAncien  = new PasswordField(); fAncien.setPromptText("Ancien mot de passe");
        PasswordField fNouveau = new PasswordField(); fNouveau.setPromptText("Nouveau mot de passe");
        PasswordField fConfirm = new PasswordField(); fConfirm.setPromptText("Confirmer le nouveau mot de passe");
        String pwStyle = "-fx-background-radius:8; -fx-padding:10; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-font-size:13;";
        fAncien.setStyle(pwStyle); fNouveau.setStyle(pwStyle); fConfirm.setStyle(pwStyle);

        Button btnChgPw = new Button("CHANGER LE MOT DE PASSE");
        styliserBoutonSecondaire(btnChgPw);
        Label lMsgPw = new Label(""); lMsgPw.setStyle("-fx-font-size:11; -fx-text-fill:#ef4444;");

        btnChgPw.setOnAction(e -> {
            if (fAncien.getText().isEmpty() || fNouveau.getText().isEmpty() || fConfirm.getText().isEmpty()) {
                lMsgPw.setText("⚠ Tous les champs sont requis."); lMsgPw.setStyle("-fx-font-size:11; -fx-text-fill:#ef4444;"); return;
            }
            if (!fNouveau.getText().equals(fConfirm.getText())) {
                lMsgPw.setText("⚠ Les mots de passe ne correspondent pas."); return;
            }
            if (etudiant == null || !fAncien.getText().equals(etudiant.getMotDePasse())) {
                lMsgPw.setText("⚠ Ancien mot de passe incorrect."); return;
            }
            etudiant.setMotDePasse(fNouveau.getText());
            try {
                utilisateurDAO.update(etudiant);
                lMsgPw.setText("✓ Mot de passe mis à jour avec succès.");
                lMsgPw.setStyle("-fx-font-size:11; -fx-text-fill:#16a34a;");
                fAncien.clear(); fNouveau.clear(); fConfirm.clear();
            } catch (Exception ex) {
                lMsgPw.setText("⚠ Erreur : " + ex.getMessage());
            }
        });

        GridPane pwGrid = new GridPane(); pwGrid.setHgap(14); pwGrid.setVgap(10);
        pwGrid.add(fAncien, 0, 0); pwGrid.add(fNouveau, 1, 0); pwGrid.add(fConfirm, 2, 0);
        GridPane.setHgrow(fAncien, Priority.ALWAYS);
        GridPane.setHgrow(fNouveau, Priority.ALWAYS);
        GridPane.setHgrow(fConfirm, Priority.ALWAYS);

        securityCard.getChildren().addAll(lSec, new Separator(), pwGrid, btnChgPw, lMsgPw);

        mainContent.getChildren().addAll(profilCard, infoCardsRow, securityCard);
    }

    @FXML
    private void showSalles() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");

        if ("Enseignant".equalsIgnoreCase(roleConnecte) && utilisateurConnecte != null) {
            mainContent.getChildren().add(btnRetour(() -> showTeacherView(utilisateurConnecte)));
        } else if ("Etudiant".equalsIgnoreCase(roleConnecte) && utilisateurConnecte != null) {
            mainContent.getChildren().add(btnRetour(() -> showStudentView(utilisateurConnecte)));
        }

        Label titre = creerTitre("RESEAU DES LOCAUX — ETAT EN TEMPS REEL");

        TextField search = new TextField();
        search.setPromptText("🔍 Rechercher une salle ou un bâtiment...");
        search.setPrefWidth(480);
        search.setStyle("-fx-padding: 10; -fx-background-radius: 20; -fx-border-color: " + BLEU_DEEP + "; -fx-border-radius: 20;");
        search.textProperty().addListener((obs, o, n) -> rafraichirListeSalles(true, n));

        containerSalles = new FlowPane(20, 20);
        containerSalles.setPadding(new Insets(15, 0, 0, 0));
        rafraichirListeSalles(true, "");

        ScrollPane scroll = creerScrollPane(containerSalles, 520);
        mainContent.getChildren().addAll(titre, search, scroll);
    }

    private void dialogReserverSalleEtude(Utilisateur etudiant) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("Nouvelle réservation de salle");
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color:" + BLEU_DEEP + "; -fx-border-color:" + VERT_LIME +
            "; -fx-border-width:2; -fx-background-radius:14; -fx-border-radius:14;");

        Label titre = new Label("📚 RESERVER UNE SALLE D'ETUDE");
        titre.setStyle("-fx-text-fill:" + VERT_LIME + "; -fx-font-family:'Consolas'; -fx-font-size:17; -fx-font-weight:bold;");

        String fStyle = "-fx-background-color:#1a252f; -fx-text-fill:white; -fx-border-color:#2d3f50; " +
            "-fx-border-radius:7; -fx-background-radius:7; -fx-padding:9; -fx-font-size:13;";
        String lStyle = "-fx-text-fill:" + VERT_LIME + "99; -fx-font-size:10; -fx-font-family:'Consolas';";

        Label lSalle = new Label("SALLE DISPONIBLE"); lSalle.setStyle(lStyle);
        ComboBox<Salle> cbSalle = new ComboBox<>();
        List<Salle> dispos = salleDAO.findAll().stream()
            .filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle()))
            .collect(Collectors.toList());
        cbSalle.getItems().addAll(dispos);
        cbSalle.setMaxWidth(Double.MAX_VALUE);
        cbSalle.setStyle(fStyle);
        cbSalle.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Salle s) {
                return s == null ? "" : s.getNumeroSalle() + " — " +
                    (s.getBatiment() != null ? s.getBatiment().getNomBatiment() : "?") +
                    " (" + s.getCapacite() + " places)";
            }
            public Salle fromString(String s) { return null; }
        });

        Label lNature = new Label("NATURE DE LA SESSION"); lNature.setStyle(lStyle);
        ComboBox<String> cbNature = new ComboBox<>();
        cbNature.getItems().addAll("Révision individuelle", "Travail en groupe", "Projet", "Exposé / Répétition", "Autre");
        cbNature.setValue("Révision individuelle");
        cbNature.setMaxWidth(Double.MAX_VALUE);
        cbNature.setStyle(fStyle);

        Label lDate = new Label("DATE DE LA SESSION"); lDate.setStyle(lStyle);
        DatePicker dpDate = new DatePicker(java.time.LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.setStyle(fStyle);

        Label lCreneau = new Label("CRENEAU HORAIRE"); lCreneau.setStyle(lStyle);
        HBox creneauRow = new HBox(12);
        ComboBox<String> cbDebut = new ComboBox<>();
        ComboBox<String> cbFin   = new ComboBox<>();
        String[] heures = {"07:00","07:30","08:00","08:30","09:00","09:30","10:00","10:30",
            "11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30",
            "15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30","19:00","20:00"};
        cbDebut.getItems().addAll(heures);
        cbFin.getItems().addAll(heures);
        cbDebut.setValue("08:00"); cbFin.setValue("10:00");
        cbDebut.setStyle(fStyle); cbFin.setStyle(fStyle);
        cbDebut.setPrefWidth(150); cbFin.setPrefWidth(150);
        Label lSep = new Label("→"); lSep.setStyle("-fx-text-fill:white; -fx-font-size:16;");
        creneauRow.setAlignment(Pos.CENTER_LEFT);
        creneauRow.getChildren().addAll(cbDebut, lSep, cbFin);

        Label lNbP = new Label("NOMBRE DE PARTICIPANTS"); lNbP.setStyle(lStyle);
        TextField fNbP = new TextField("1");
        fNbP.setStyle(fStyle); fNbP.setPrefWidth(120);

        Label lComm = new Label("COMMENTAIRE (OPTIONNEL)"); lComm.setStyle(lStyle);
        TextArea taComm = new TextArea();
        taComm.setPromptText("Indiquez l'objet de la session, les besoins particuliers...");
        taComm.setPrefHeight(75); taComm.setWrapText(true);
        taComm.setStyle("-fx-control-inner-background:#1a252f; -fx-text-fill:white; -fx-border-color:#2d3f50; -fx-border-radius:7; -fx-background-radius:7;");

        Label lMsg = new Label(""); lMsg.setStyle("-fx-font-size:11;");

        Button btnReserver = new Button("✔ CONFIRMER LA RESERVATION");
        btnReserver.setMaxWidth(Double.MAX_VALUE); btnReserver.setPrefHeight(46);
        styliserBoutonPrimaire(btnReserver);

        btnReserver.setOnAction(e -> {
            if (cbSalle.getValue() == null) {
                lMsg.setText("⚠ Veuillez sélectionner une salle."); lMsg.setStyle("-fx-text-fill:#ef4444; -fx-font-size:11;"); return;
            }
            try {
                Salle salleChoisie = cbSalle.getValue();
                salleChoisie.setEtatSalle("Occupée");
                salleDAO.update(salleChoisie);

                if (etudiant != null && etudiant.getEmail() != null) {
                    notifService.confirmerReservation(etudiant.getEmail(),
                        "Réservation confirmée — Salle " + salleChoisie.getNumeroSalle() +
                        " le " + dpDate.getValue() + " de " + cbDebut.getValue() +
                        " à " + cbFin.getValue() + " — " + cbNature.getValue());
                }
                stage.close();
                afficherAlerte("Réservation confirmée",
                    "La salle " + salleChoisie.getNumeroSalle() + " est réservée pour le " +
                    dpDate.getValue() + " de " + cbDebut.getValue() + " à " + cbFin.getValue() + ".");
                showMesReservationsEtudiant(etudiant);
            } catch (Exception ex) {
                ex.printStackTrace();
                lMsg.setText("⚠ Erreur : " + ex.getMessage());
                lMsg.setStyle("-fx-text-fill:#ef4444; -fx-font-size:11;");
            }
        });

        Button btnAnnuler = new Button("ANNULER");
        btnAnnuler.setMaxWidth(Double.MAX_VALUE);
        btnAnnuler.setStyle("-fx-background-color:transparent; -fx-text-fill:" + GRIS_TEXTE + "; -fx-cursor:hand;");
        btnAnnuler.setOnAction(e -> stage.close());

        root.getChildren().addAll(
            titre, new Separator(),
            lSalle, cbSalle,
            lNature, cbNature,
            lDate, dpDate,
            lCreneau, creneauRow,
            lNbP, fNbP,
            lComm, taComm,
            lMsg, btnReserver, btnAnnuler
        );

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + BLEU_DEEP + ";");
        stage.setScene(new Scene(sp, 500, 660));
        stage.show();
    }

    private GridPane construireGrilleEDT(Utilisateur etudiant) {
        GridPane grid = new GridPane();
        grid.setHgap(3); grid.setVgap(3);
        grid.setStyle("-fx-background-color:#f1f5f9; -fx-padding:4;");

        String[] jours  = {"", "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};
        String[] slices = {"07h30","08h00","08h30","09h00","09h30","10h00","10h30",
                           "11h00","11h30","12h00","12h30","13h00","13h30","14h00",
                           "14h30","15h00","15h30","16h00","16h30","17h00","17h30","18h00"};

        for (int c = 0; c < jours.length; c++) {
            Label h = new Label(jours[c]);
            h.setMinWidth(c == 0 ? 68 : 148);
            h.setPrefWidth(c == 0 ? 68 : 148);
            h.setAlignment(Pos.CENTER);
            h.setPrefHeight(36);
            h.setStyle("-fx-background-color:" + BLEU_DEEP + "; -fx-text-fill:" + VERT_LIME +
                "; -fx-font-weight:bold; -fx-font-family:'Consolas'; -fx-font-size:11; -fx-padding:5;");
            grid.add(h, c, 0);
        }

        for (int r = 0; r < slices.length; r++) {
            Label slot = new Label(slices[r]);
            slot.setMinWidth(68); slot.setPrefWidth(68);
            slot.setPrefHeight(38); slot.setAlignment(Pos.CENTER);
            slot.setStyle("-fx-background-color:#e2e8f0; -fx-text-fill:" + BLEU_DEEP +
                "; -fx-font-weight:bold; -fx-font-size:10; -fx-font-family:'Consolas';");
            grid.add(slot, 0, r + 1);

            for (int c = 1; c < jours.length; c++) {
                Label cell = new Label("");
                cell.setMinWidth(148); cell.setPrefWidth(148);
                cell.setPrefHeight(38); cell.setAlignment(Pos.CENTER);
                boolean isBreak = slices[r].equals("12h30") || slices[r].equals("13h00");
                String cellStyle = isBreak
                    ? "-fx-background-color:#fef9c3; -fx-text-fill:#ca8a04; -fx-font-size:10;"
                    : "-fx-background-color:white; -fx-text-fill:#94a3b8; -fx-border-color:#f1f5f9; -fx-font-size:10;";
                if (isBreak && c == 1) cell.setText("Pause déjeuner");
                cell.setStyle(cellStyle);
                grid.add(cell, c, r + 1);
            }
        }
        return grid;
    }

    private VBox construireApercuPlanningEtudiant(Utilisateur etudiant) {
        VBox block = new VBox(12);
        block.setPadding(new Insets(22, 24, 22, 24));
        block.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),10,0,0,3);");

        HBox hdrRow = new HBox(12);
        hdrRow.setAlignment(Pos.CENTER_LEFT);
        Label lTitre = new Label("📅 PLANNING DE LA SEMAINE");
        lTitre.setStyle("-fx-font-family:'Consolas'; -fx-font-size:14; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + ";");
        Button btnVoirTout = new Button("Voir tout →");
        btnVoirTout.setStyle("-fx-background-color:transparent; -fx-text-fill:" + VERT_LIME + "; -fx-cursor:hand; -fx-font-weight:bold;");
        btnVoirTout.setOnAction(e -> showEdtEtudiant(etudiant));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        hdrRow.getChildren().addAll(lTitre, sp, btnVoirTout);

        GridPane mini = new GridPane();
        mini.setHgap(8); mini.setVgap(6);
        String[] jMini = {"LUN", "MAR", "MER", "JEU", "VEN"};
        String[] hMini = {"08h-10h", "10h-12h", "14h-16h"};

        for (int c = 0; c < jMini.length; c++) {
            Label jh = new Label(jMini[c]);
            jh.setMinWidth(120); jh.setAlignment(Pos.CENTER);
            jh.setStyle("-fx-background-color:" + BLEU_DEEP + "; -fx-text-fill:" + VERT_LIME +
                "; -fx-font-weight:bold; -fx-font-size:10; -fx-padding:6 4; -fx-font-family:'Consolas';");
            mini.add(jh, c, 0);
        }
        for (int r = 0; r < hMini.length; r++) {
            for (int c = 0; c < jMini.length; c++) {
                Label cell = new Label(r == 0 && c == 0 ? "Cours" : "");
                cell.setMinWidth(120); cell.setPrefHeight(32); cell.setAlignment(Pos.CENTER);
                cell.setStyle(r == 0 && c == 0
                    ? "-fx-background-color:#ede9fe; -fx-text-fill:#7c3aed; -fx-font-size:10; -fx-font-weight:bold; -fx-background-radius:6;"
                    : "-fx-background-color:#f8fafc; -fx-text-fill:#94a3b8; -fx-font-size:10; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");
                mini.add(cell, c, r + 1);
            }
        }
        block.getChildren().addAll(hdrRow, mini);
        return block;
    }

    private VBox construireApercuNotificationsEtudiant(List<Notification> notifs, Utilisateur etudiant) {
        VBox block = new VBox(10);
        block.setPadding(new Insets(20, 22, 20, 22));
        block.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),10,0,0,3);");

        HBox hdrRow = new HBox();
        Label lT = new Label("🔔 DERNIERES NOTIFICATIONS");
        lT.setStyle("-fx-font-family:'Consolas'; -fx-font-size:14; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + ";");
        Button btnAll = new Button("Tout voir →");
        btnAll.setStyle("-fx-background-color:transparent; -fx-text-fill:" + VERT_LIME + "; -fx-cursor:hand; -fx-font-weight:bold;");
        btnAll.setOnAction(e -> showNotificationsEtudiant(etudiant));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        hdrRow.getChildren().addAll(lT, sp, btnAll);
        block.getChildren().add(hdrRow);

        if (notifs == null || notifs.isEmpty()) {
            Label vide = new Label("Aucune notification.");
            vide.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12; -fx-padding:10 0;");
            block.getChildren().add(vide);
        } else {
            notifs.stream().limit(3).forEach(n -> block.getChildren().add(creerCarteNotification(n)));
        }
        return block;
    }

    private HBox creerCarteNotification(Notification notif) {
        HBox card = new HBox(14);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setAlignment(Pos.CENTER_LEFT);
        boolean nonLu = notif.getStatut() != null && notif.getStatut().equalsIgnoreCase("NON_LU");
        card.setStyle("-fx-background-color:" + (nonLu ? "#f0fdf4" : "#f8fafc") +
            "; -fx-border-color:" + (nonLu ? "#86efac" : "#e2e8f0") +
            "; -fx-border-radius:10; -fx-background-radius:10;");

        Label lIco = new Label(nonLu ? "🔔" : "🔕");
        lIco.setStyle("-fx-font-size:18;");

        VBox info = new VBox(3);
        Label lMsg = new Label(notif.getMessage() != null ? notif.getMessage() : "—");
        lMsg.setStyle("-fx-font-size:12; -fx-text-fill:" + BLEU_DEEP + ";" + (nonLu ? " -fx-font-weight:bold;" : ""));
        lMsg.setWrapText(true);

        String dateStr = notif.getDateHeure() != null
            ? notif.getDateHeure().format(DateTimeFormatter.ofPattern("dd MMM HH:mm", java.util.Locale.FRENCH))
            : "—";
        Label lDate = new Label(dateStr);
        lDate.setStyle("-fx-font-size:10; -fx-text-fill:#94a3b8;");

        info.getChildren().addAll(lMsg, lDate);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lStatut = new Label(nonLu ? "NOUVEAU" : "LU");
        lStatut.setStyle("-fx-background-color:" + (nonLu ? "#dcfce7" : "#e2e8f0") +
            "; -fx-text-fill:" + (nonLu ? "#16a34a" : "#64748b") +
            "; -fx-font-size:9; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");

        card.getChildren().addAll(lIco, info, lStatut);
        return card;
    }

    private VBox creerCarteDetailleeSalle(Salle s, Utilisateur etudiant) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20)); card.setPrefWidth(290);
        card.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.09),10,0,0,0);");

        HBox headRow = new HBox(8);
        headRow.setAlignment(Pos.CENTER_LEFT);
        Label lNum = new Label(s.getNumeroSalle());
        lNum.setStyle("-fx-font-weight:bold; -fx-font-size:22; -fx-text-fill:" + BLEU_DEEP + ";");
        Label lDispo = new Label("● LIBRE");
        lDispo.setStyle("-fx-text-fill:#16a34a; -fx-font-weight:bold; -fx-font-size:10; -fx-background-color:#dcfce7; -fx-padding:3 8; -fx-background-radius:8;");
        headRow.getChildren().addAll(lNum, new Region(){{HBox.setHgrow(this,Priority.ALWAYS);}}, lDispo);

        Label lBat = new Label("📍 " + (s.getBatiment() != null ? s.getBatiment().getNomBatiment() : "N/A"));
        lBat.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");
        Label lCap = new Label("👥 " + s.getCapacite() + " places");
        lCap.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");
        Label lType = new Label("🏷  " + (s.getCategorieSalle() != null ? s.getCategorieSalle() : "—"));
        lType.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");

        ProgressBar pbCap = new ProgressBar(0.0);
        pbCap.setMaxWidth(Double.MAX_VALUE); pbCap.setPrefHeight(7);
        pbCap.setStyle("-fx-accent:#22c55e;");

        Button btnReserv = new Button("RESERVER CETTE SALLE");
        btnReserv.setMaxWidth(Double.MAX_VALUE); btnReserv.setPrefHeight(38);
        styliserBoutonPrimaire(btnReserv);
        btnReserv.setOnAction(e -> dialogReserverSalleEtude(etudiant));

        card.getChildren().addAll(headRow, new Separator(), lBat, lCap, lType, pbCap, btnReserv);

        String base = card.getStyle();
        card.setOnMouseEntered(ev -> { card.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-effect:dropshadow(three-pass-box," + VERT_LIME + ",18,0,0,0);"); card.setTranslateY(-5); });
        card.setOnMouseExited(ev  -> { card.setStyle(base); card.setTranslateY(0); });

        return card;
    }

    private VBox creerCarteCompacteSalle(Salle s, Utilisateur etudiant) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setPrefWidth(190);
        card.setCursor(javafx.scene.Cursor.HAND);

        boolean dispo = "Disponible".equalsIgnoreCase(s.getEtatSalle());
        card.setStyle("-fx-background-color:" + (dispo ? "white" : "#fff5f5") +
            "; -fx-background-radius:14; -fx-border-color:" + (dispo ? "#e2e8f0" : "#fca5a5") +
            "; -fx-border-radius:14; -fx-border-width:1.5;");

        Label lNum = new Label(s.getNumeroSalle());
        lNum.setStyle("-fx-font-weight:bold; -fx-font-size:17; -fx-text-fill:" + BLEU_DEEP + ";");

        Label lCap = new Label("👥 " + s.getCapacite() + " pl.");
        lCap.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:11;");

        Label lEtat = new Label(dispo ? "● LIBRE" : "● OCCUPEE");
        lEtat.setStyle("-fx-text-fill:" + (dispo ? "#16a34a" : "#dc2626") +
            "; -fx-font-weight:bold; -fx-font-size:10;");

        card.getChildren().addAll(lNum, lCap, lEtat);

        if (dispo) {
            card.setOnMouseClicked(ev -> dialogReserverSalleEtude(etudiant));
            card.setOnMouseEntered(ev -> card.setStyle("-fx-background-color:#f0fdf4; -fx-background-radius:14; -fx-border-color:#86efac; -fx-border-radius:14; -fx-border-width:1.5;"));
            card.setOnMouseExited(ev  -> card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-border-color:#e2e8f0; -fx-border-radius:14; -fx-border-width:1.5;"));
        }
        return card;
    }

    private void annulerReservationEtudiant(Reservation r, Utilisateur etudiant) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Annulation"); a.setHeaderText("Annuler cette réservation ?");
        a.setContentText("La salle sera de nouveau disponible.");
        a.showAndWait().filter(resp -> resp == ButtonType.OK).ifPresent(resp -> {
            r.annulerReservation();
            if (r.getMonCreneau() != null && r.getMonCreneau().getSalle() != null) {
                Salle sal = r.getMonCreneau().getSalle();
                sal.setEtatSalle("Disponible");
                salleDAO.update(sal);
            }
            try {
                ReservationDAO rDAO = new ReservationDAO(HibernateUtil.getSessionFactory());
                rDAO.update(r);
            } catch (Exception ignored) {}
            showMesReservationsEtudiant(etudiant);
        });
    }

    private VBox creerKpiEtudiant(String ico, String titre, String val, String color, String detail) {
        VBox c = new VBox(7);
        c.setPadding(new Insets(18, 20, 18, 20));
        c.setPrefWidth(210);
        c.setStyle("-fx-background-color:white; -fx-background-radius:16; " +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,3); " +
            "-fx-border-left-width:4; -fx-border-color:transparent transparent transparent " + color +
            "; -fx-border-radius:0 16 16 0;");
        HBox top = new HBox(8); top.setAlignment(Pos.CENTER_LEFT);
        Label lI = new Label(ico); lI.setStyle("-fx-font-size:16;");
        Label lT = new Label(titre); lT.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:9; -fx-font-weight:bold; -fx-font-family:'Consolas';"); lT.setWrapText(true);
        top.getChildren().addAll(lI, lT);
        Label lV = new Label(val); lV.setStyle("-fx-font-size:34; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + "; -fx-font-family:'Consolas';");
        Label lD = new Label(detail); lD.setStyle("-fx-text-fill:" + color + "; -fx-font-size:10; -fx-font-weight:bold;");
        c.getChildren().addAll(top, lV, lD);
        return c;
    }

    private VBox creerTuileEtudiant(String titre, String desc, String color, Runnable action) {
        VBox tile = new VBox(10);
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setPadding(new Insets(20, 22, 20, 22));
        tile.setPrefWidth(260);
        tile.setCursor(javafx.scene.Cursor.HAND);
        String base = "-fx-background-color:" + BLEU_DEEP + "; -fx-background-radius:14; " +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.18),10,0,0,4);";
        tile.setStyle(base);
        Label lT = new Label(titre); lT.setStyle("-fx-text-fill:" + color + "; -fx-font-family:'Consolas'; -fx-font-weight:bold; -fx-font-size:12;");
        Region acc = new Region(); acc.setPrefSize(32, 3); acc.setMaxWidth(32);
        acc.setStyle("-fx-background-color:" + color + "; -fx-background-radius:2;");
        Label lD = new Label(desc); lD.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");
        tile.getChildren().addAll(lT, acc, lD);
        tile.setOnMouseClicked(e -> action.run());
        tile.setOnMouseEntered(e -> { tile.setStyle(base + "-fx-border-color:" + color + "; -fx-border-width:1.5; -fx-border-radius:14;"); tile.setTranslateY(-4); });
        tile.setOnMouseExited(e  -> { tile.setStyle(base); tile.setTranslateY(0); });
        return tile;
    }

    private VBox creerInfoCard(String titre, String ligne1, String ligne2, String ligne3) {
        VBox c = new VBox(7);
        c.setPadding(new Insets(18, 20, 18, 20));
        c.setPrefWidth(280);
        c.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.07),8,0,0,3);");
        Label lT = new Label(titre); lT.setStyle("-fx-font-family:'Consolas'; -fx-font-size:13; -fx-font-weight:bold; -fx-text-fill:" + BLEU_DEEP + ";");
        Label l1 = new Label(ligne1); l1.setStyle("-fx-text-fill:" + BLEU_DEEP + "; -fx-font-weight:bold; -fx-font-size:15;");
        Label l2 = new Label(ligne2); l2.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");
        Label l3 = new Label(ligne3); l3.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");
        c.getChildren().addAll(lT, new Separator(), l1, l2, l3);
        return c;
    }

    private HBox creerBadgeStat(String label, String val, String bgColor, String textColor) {
        HBox b = new HBox(8); b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(10, 18, 10, 18));
        b.setStyle("-fx-background-color:" + bgColor + "; -fx-background-radius:12;");
        Label lV = new Label(val); lV.setStyle("-fx-font-size:22; -fx-font-weight:bold; -fx-text-fill:" + textColor + "; -fx-font-family:'Consolas';");
        Label lL = new Label(label); lL.setStyle("-fx-font-size:11; -fx-font-weight:bold; -fx-text-fill:" + textColor + ";");
        b.getChildren().addAll(lV, lL);
        return b;
    }

    private HBox creerMiniStatBadge(String label, String val, String color) {
        HBox b = new HBox(8); b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(7, 16, 7, 16));
        b.setStyle("-fx-background-color:" + color + "18; -fx-border-color:" + color + "44; -fx-border-radius:10; -fx-background-radius:10;");
        Label lV = new Label(val); lV.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:" + color + "; -fx-font-family:'Consolas';");
        Label lL = new Label(label); lL.setStyle("-fx-font-size:11; -fx-text-fill:" + color + ";");
        b.getChildren().addAll(lV, lL);
        return b;
    }

    private HBox creerLegendeBullet(String color, String label) {
        HBox h = new HBox(7); h.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region(); dot.setPrefSize(14, 14); dot.setMaxSize(14, 14);
        dot.setStyle("-fx-background-color:" + color + "; -fx-background-radius:7;");
        Label l = new Label(label); l.setStyle("-fx-text-fill:" + GRIS_TEXTE + "; -fx-font-size:12;");
        h.getChildren().addAll(dot, l);
        return h;
    }

    private String lundiDeLaSemaine() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lundi = today.with(java.time.DayOfWeek.MONDAY);
        return lundi.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH));
    }
    
    @FXML
    public void showRechercheSalle() {
        System.out.println("LOG : Redirection via alias showRechercheSalle -> showReservations");
        // On appelle la méthode que tu as déjà créée pour la recherche/réservation
        showReservations();
    }
    
    /**
     * Méthode appelée par le bouton "Profil" du FXML (#showMonProfil)
     * Redirige vers la vue du profil de l'utilisateur connecté.
     */
    @FXML
    public void showMonProfil() {
        System.out.println("LOG : Redirection via alias showMonProfil -> showMonProfilEtudiant");
        // On appelle la méthode de profil que nous avons déjà, 
        // en passant l'utilisateur actuellement en session.
        showMonProfilEtudiant(utilisateurConnecte);
    }
    
    @FXML
    public void showMonEdt() {
        System.out.println("LOG : Redirection via alias showMonEdt -> showMesCreneaux");
        showMesCreneaux();
    }
    
    @FXML
    public void showMesCreneaux() {
        // 1. Nettoyage de la zone centrale
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(22, 32, 40, 32));

        // 2. Bouton retour dynamique selon le rôle
        if ("Enseignant".equalsIgnoreCase(roleConnecte)) {
            mainContent.getChildren().add(btnRetour(() -> showTeacherView(utilisateurConnecte)));
        } else {
            mainContent.getChildren().add(btnRetour(() -> showStudentView(utilisateurConnecte)));
        }

        // 3. Titre et sous-titre
        Label titre = creerTitre("📋 MES CRENEAUX DE COURS");
        Label sub = new Label("Liste des séances validées et planifiées dans l'emploi du temps.");
        sub.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 13;");

        // 4. Configuration du Tableau (TableView)
        TableView<Creneau> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);
        table.setPlaceholder(new Label("Aucun créneau de cours trouvé pour votre profil."));
        table.setStyle("-fx-background-radius: 12; -fx-border-color: #f1f5f9;");

        // Colonne : Jour
        TableColumn<Creneau, String> colJour = new TableColumn<>("JOUR");
        colJour.setCellValueFactory(new PropertyValueFactory<>("jourSemaine"));
        colJour.setPrefWidth(120);

        // Colonne : Horaire
        TableColumn<Creneau, String> colHeure = new TableColumn<>("HORAIRE");
        colHeure.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getHeureDebut() + " - " + cd.getValue().getHeureFin()));
        colHeure.setPrefWidth(150);

        // Colonne : Salle
        TableColumn<Creneau, String> colSalle = new TableColumn<>("SALLE");
        colSalle.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getSalle() != null ? cd.getValue().getSalle().getNumeroSalle() : "N/A"));
        colSalle.setPrefWidth(100);

        // Colonne : Cours / Matière
     // Colonne : Cours / Matière
     // 1. On crée la colonne
        TableColumn<Creneau, String> colCours = new TableColumn<>("UNITE D'ENSEIGNEMENT");

        // 2. On définit comment récupérer la valeur (UNE SEULE FOIS)
        colCours.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getCours() != null ? cd.getValue().getCours().getCodeCours() : "Séance libre"
        ));

        // 3. On ajoute la colonne au tableau
        table.getColumns().addAll(colJour, colHeure, colSalle, colCours);

        // 5. Chargement des données depuis le DAO
        try {
            // Idéalement, filtrer ici par l'identifiant de l'utilisateur connecté
            List<Creneau> liste = creneauDAO.findAll(); 
            if (liste != null) {
                table.getItems().addAll(liste);
            }
        } catch (Exception e) {
            System.err.println("LOG : Erreur lors de la récupération des créneaux : " + e.getMessage());
        }

        // 6. Assemblage final
        mainContent.getChildren().addAll(titre, sub, new Separator(), table);
    }
    @FXML
    public void showLoginSelection() {
        if (sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        }

        preparerContenu("-fx-background-color: #FFFFFF;");
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(0));

        VBox rootBox = new VBox(50);
        rootBox.setAlignment(Pos.CENTER);

        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);

        Label title = new Label("UNIV-SCHEDULER");
        title.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 48; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        Region limeBar = new Region();
        limeBar.setPrefSize(140, 6);
        limeBar.setMaxWidth(140);
        limeBar.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 5;");

        Label subtitle = new Label("SELECTIONNEZ VOTRE PORTAIL D'ACCES");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        header.getChildren().addAll(title, limeBar, subtitle);

        HBox cardsBox = new HBox(30);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setPadding(new Insets(20, 0, 0, 0));

        cardsBox.getChildren().addAll(
            creerCartePortail("ADMINISTRATEUR", "⚡", "Configuration globale du système", e -> showLoginForm("Administrateur")),
            creerCartePortail("GESTIONNAIRE",   "⚒",  "Planification & Emplois du temps", e -> showLoginForm("Gestionnaire")),
            creerCartePortail("ENSEIGNANT",     "📖", "Consultation & Réservation", e -> showLoginForm("Enseignant")),
            creerCartePortail("ETUDIANT",       "🎓", "Consultation des plannings", e -> showLoginForm("Etudiant"))
        );

        rootBox.getChildren().addAll(header, cardsBox);
        mainContent.getChildren().add(rootBox);
    }
    
    private void showLoginForm(String role) {
        preparerContenu("-fx-background-color: #FFFFFF;");
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(0));

        VBox rootBox = new VBox(0);
        rootBox.setAlignment(Pos.CENTER);
        rootBox.setPrefWidth(Double.MAX_VALUE);

        HBox topBand = new HBox();
        topBand.setAlignment(Pos.CENTER_LEFT);
        topBand.setPadding(new Insets(18, 30, 18, 30));
        topBand.setStyle("-fx-background-color: " + BLEU_DEEP + ";");

        Label appName = new Label("UNIV-SCHEDULER");
        appName.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: " + VERT_LIME + ";");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("← Changer de portail");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showLoginSelection());

        topBand.getChildren().addAll(appName, spacer, btnBack);

        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(45, 50, 45, 50));
        card.setMaxWidth(440);
        card.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 25, 0, 0, 8);");

        String icon = switch (role) {
            case "Administrateur" -> "⚡";
            case "Gestionnaire"   -> "⚒";
            case "Enseignant"     -> "📖";
            default               -> "🎓";
        };

        StackPane iconBox = new StackPane();
        Region circle = new Region();
        circle.setPrefSize(75, 75); circle.setMaxSize(75, 75);
        circle.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 37.5;");
        Label lIcon = new Label(icon);
        lIcon.setStyle("-fx-font-size: 30;");
        iconBox.getChildren().addAll(circle, lIcon);

        Label lRole = new Label("PORTAIL " + role.toUpperCase());
        lRole.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + VERT_LIME + ";");

        Region sep = new Region(); sep.setPrefSize(60, 3); sep.setMaxWidth(60);
        sep.setStyle("-fx-background-color: " + VERT_LIME + "66; -fx-background-radius: 2;");

        Label lId = new Label("IDENTIFIANT");
        lId.setStyle("-fx-text-fill: " + VERT_LIME + "99; -fx-font-size: 10; -fx-font-family: 'Consolas';");
        TextField txtId = new TextField();
        txtId.setPromptText("ex: admin@univ.sn");
        txtId.setStyle("-fx-background-color: #1a252f; -fx-text-fill: white; -fx-border-color: #2d3f50; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 13;");
        VBox.setMargin(txtId, new Insets(0, 0, 4, 0));

        Label lPw = new Label("MOT DE PASSE");
        lPw.setStyle("-fx-text-fill: " + VERT_LIME + "99; -fx-font-size: 10; -fx-font-family: 'Consolas';");
        PasswordField txtPw = new PasswordField();
        txtPw.setPromptText("••••••••");
        txtPw.setStyle("-fx-background-color: #1a252f; -fx-text-fill: white; -fx-border-color: #2d3f50; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 13;");

        Label lErr = new Label("");
        lErr.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-font-family: 'Consolas';");
        lErr.setVisible(false);

        Button btnLogin = new Button("SE CONNECTER →");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(46);
        styliserBoutonPrimaire(btnLogin);

        Runnable tenterConnexion = () -> {
            String identifiant = txtId.getText().trim();
            String motDePasse  = txtPw.getText();

            if (identifiant.isEmpty() || motDePasse.isEmpty()) {
                lErr.setText("⚠ Veuillez remplir tous les champs.");
                lErr.setVisible(true);
                return;
            }

            try {
                Utilisateur u = utilisateurDAO.findByIdentifiant(identifiant);

                if (u == null) {
                    lErr.setText("✗ Identifiant introuvable.");
                    lErr.setVisible(true);
                    txtPw.clear();
                    return;
                }

                if (!motDePasse.equals(u.getMotDePasse())) {
                    lErr.setText("✗ Mot de passe incorrect.");
                    lErr.setVisible(true);
                    txtPw.clear();
                    return;
                }

                if (!u.getRole().equalsIgnoreCase(role)) {
                    lErr.setText("✗ Ce compte n'est pas un profil " + role + ".");
                    lErr.setVisible(true);
                    txtPw.clear();
                    return;
                }

                pause.setOnFinished(ev -> {
                    utilisateurConnecte = u;
                    roleConnecte        = role;

                    filtrerMenuLateral(role);
                    switch (role) {
                        case "Administrateur" -> showAdminPanel();
                        case "Gestionnaire"   -> showManagerDashboard();
                        case "Enseignant"     -> showTeacherView(u);
                        default               -> showStudentView(u);
                    }
                });
                
                // 👇 AJOUTE CETTE LIGNE JUSTE ICI 👇
                pause.play();

            } catch (Exception ex) {
                lErr.setText("⚠ Erreur système : " + ex.getMessage());
                lErr.setVisible(true);
            }
        };

        btnLogin.setOnAction(e -> tenterConnexion.run());
        txtPw.setOnAction(e -> tenterConnexion.run()); 

        card.getChildren().addAll(iconBox, lRole, sep, lId, txtId, lPw, txtPw, lErr, btnLogin);

        Label footer = new Label("© 2026 UIDT Cyber-System · UNIV-SCHEDULER");
        footer.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10;");

        VBox centerWrapper = new VBox(card);
        centerWrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerWrapper, Priority.ALWAYS);
        centerWrapper.setPadding(new Insets(60, 0, 40, 0));

        rootBox.getChildren().addAll(topBand, centerWrapper, footer);
        VBox.setMargin(footer, new Insets(0, 0, 20, 0));

        mainContent.getChildren().add(rootBox);

        Platform.runLater(txtId::requestFocus);
    }

    private VBox creerCartePortail(String titre, String icon, String desc, EventHandler<MouseEvent> action) {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(270, 210);
        card.setPadding(new Insets(30));
        card.setCursor(Cursor.HAND);

        String baseStyle = "-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 20; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 15, 0, 0, 6);";
        card.setStyle(baseStyle);

        StackPane iconBox = new StackPane();
        Region circle = new Region();
        circle.setPrefSize(82, 82);
        circle.setMaxSize(82, 82);
        circle.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 41;");
        Label lIcon = new Label(icon);
        lIcon.setStyle("-fx-font-size: 32; -fx-text-fill: " + BLEU_DEEP + ";");
        iconBox.getChildren().addAll(circle, lIcon);

        Label lTitre = new Label(titre);
        lTitre.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: " + VERT_LIME + ";");

        Label lDesc = new Label(desc);
        lDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        lDesc.setWrapText(true);
        lDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconBox, lTitre, lDesc);
        card.setOnMouseClicked(action);

        card.setOnMouseEntered(e -> {
            card.setStyle(baseStyle + "-fx-border-color: " + VERT_LIME + "; -fx-border-width: 2; -fx-border-radius: 20;");
            card.setTranslateY(-6);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle);
            card.setTranslateY(0);
        });

        return card;
    }

    @FXML
    public void showDashboard() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        Label titre = creerTitre("SYSTEME DE SURVEILLANCE — ETAT DES SALLES");

        List<Salle> salles = salleDAO.findAll();
        if (salles == null || salles.isEmpty()) {
            mainContent.getChildren().addAll(titre, new Label("⚠️ Aucune donnée réseau."));
            return;
        }
        long libres   = salles.stream().filter(s -> s.getEtatSalle() != null && "Disponible".equalsIgnoreCase(s.getEtatSalle())).count();
        long occupees = salles.size() - libres;

        HBox stats = new HBox(25);
        stats.setPadding(new Insets(20, 0, 25, 0));
        stats.getChildren().addAll(
            creerWidgetStat("TOTAL SALLES",  String.valueOf(salles.size())),
            creerWidgetStat("DISPONIBLES",   String.valueOf(libres)),
            creerWidgetStat("OCCUPEES",      String.valueOf(occupees))
        );

        containerSalles = new FlowPane(20, 20);
        containerSalles.setPadding(new Insets(10, 0, 0, 0));
        rafraichirListeSalles(false, "");

        mainContent.getChildren().addAll(titre, stats, new Separator(), containerSalles);
    }
    
    @FXML
    private void showReservations() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");

        Label titre = creerTitre("SENSORS & BOOKING HUB");

        VBox console = new VBox(18);
        console.setPadding(new Insets(25));
        console.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 18;");

        HBox row1 = new HBox(20); row1.setAlignment(Pos.BOTTOM_LEFT);
        txtClasse   = new TextField(); txtEffectif = new TextField();
        datePicker  = new DatePicker(java.time.LocalDate.now());
        row1.getChildren().addAll(
            creerGroupeSaisie("📚 COURS / CLASSE", txtClasse, 240),
            creerGroupeSaisie("👥 EFFECTIF",       txtEffectif, 100),
            creerGroupeSaisie("📅 DATE",           datePicker, 170)
        );

        HBox row2 = new HBox(20); row2.setAlignment(Pos.BOTTOM_LEFT);
        txtEmail = new TextField();
        Button btnScan = new Button("⚡ ANALYSER");
        btnScan.setPrefHeight(42); btnScan.setPadding(new Insets(0, 28, 0, 28));
        styliserBoutonPrimaire(btnScan);
        row2.getChildren().addAll(creerGroupeSaisie("📧 EMAIL RESPONSABLE", txtEmail, 380), btnScan);

        console.getChildren().addAll(row1, new Separator(), row2);

        HBox stepper = construireStepper("1. CONFIGURATION", "2. ANALYSE", "3. RESERVATION");

        FlowPane resultats = new FlowPane(22, 22);
        resultats.setPadding(new Insets(8, 0, 8, 0));
        ScrollPane scroll = creerScrollPane(resultats, 420);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        btnScan.setOnAction(e -> {
            resultats.getChildren().clear();
            try {
                int effectif = Integer.parseInt(txtEffectif.getText().trim());
                List<Salle> options = salleDAO.findAll().stream()
                    .filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle()) && s.getCapacite() >= effectif)
                    .sorted(Comparator.comparingInt(Salle::getCapacite))
                    .limit(12)
                    .collect(Collectors.toList());

                if (options.isEmpty()) {
                    int maxCap = salleDAO.findAll().stream().mapToInt(Salle::getCapacite).max().orElse(0);
                    resultats.getChildren().add(construireAlerteCapacite(effectif, maxCap, btnScan));
                } else {
                    for (Salle s : options) {
                        double score = Math.max(0, Math.min(100, 100 - (s.getCapacite() - effectif) * 2));
                        resultats.getChildren().add(creerCarteSmartSalle(s, score));
                    }
                }
            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur saisie", "L'effectif doit être un nombre entier.");
            }
        });

        VBox layout = new VBox(18, titre, console, stepper, scroll);
        mainContent.getChildren().add(layout);
    }

    @FXML
    private void showNotifications() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");

        Label titre = creerTitre("🔔 HISTORIQUE DES NOTIFICATIONS");

        TableView<Notification> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(540);
        table.setPlaceholder(new Label("Aucune notification enregistrée."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm", Locale.FRENCH);

        TableColumn<Notification, LocalDateTime> colDate = new TableColumn<>("DATE / HEURE");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(fmt));
                if (!empty) setStyle("-fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
            }
        });
        colDate.setPrefWidth(180);

        TableColumn<Notification, String> colMsg = new TableColumn<>("MESSAGE");
        colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));

        TableColumn<Notification, String> colStatut = new TableColumn<>("STATUT");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);

        table.getColumns().addAll(colDate, colMsg, colStatut);

        List<Notification> notifs = notificationDAO.findAll();
        if (notifs != null) table.getItems().addAll(notifs);

        mainContent.getChildren().addAll(titre, table);
    }

    private void showCreneauxEnseignant(Utilisateur enseignant) {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.getChildren().add(btnRetour(() -> showTeacherView(enseignant)));
        Label titre = creerTitre("📅 MES RESERVATIONS");

        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER); placeholder.setPrefHeight(300);
        placeholder.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label msg = new Label("Les créneaux validés pour " +
                (enseignant != null ? enseignant.getNom() : "cet enseignant") +
                " s'afficheront ici.");
        msg.setStyle("-fx-text-fill: " + GRIS_TEXTE + ";");
        placeholder.getChildren().add(msg);

        mainContent.getChildren().addAll(titre, placeholder);
    }

    @FXML
    public void showUserManagement() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(25, 40, 40, 40));

        mainContent.getChildren().add(btnRetour(this::showAdminPanel));

        Label titre = creerTitre("👤 REPERTOIRE DES UTILISATEURS");

        TextField search = new TextField();
        search.setPromptText("🔍 Rechercher par nom ou email...");
        search.setPrefWidth(380);
        search.setStyle("-fx-background-radius: 8; -fx-padding: 9; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Button btnAjouter = new Button("+ NOUVEL UTILISATEUR");
        styliserBoutonSecondaire(btnAjouter);
        btnAjouter.setOnAction(e -> dialogAjouterUtilisateur());

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(15, search, spacer, btnAjouter);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        TableView<Utilisateur> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(480);
        table.setPlaceholder(new Label("Aucun utilisateur trouvé."));

        TableColumn<Utilisateur, String> colNom   = new TableColumn<>("NOM");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Utilisateur, String> colPrenom = new TableColumn<>("PRENOM");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Utilisateur, String> colEmail  = new TableColumn<>("EMAIL");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Utilisateur, String> colRole   = new TableColumn<>("ROLE");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setPrefWidth(140);

        TableColumn<Utilisateur, Void> colActions  = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDel  = new Button("🗑");
            private final HBox   pane    = new HBox(12, btnEdit, btnDel);
            { pane.setAlignment(Pos.CENTER);
              btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
              btnDel .setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
              btnEdit.setOnAction(e -> dialogModifierUtilisateur(getTableView().getItems().get(getIndex())));
              btnDel .setOnAction(e -> confirmerSuppressionUser(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : pane); }
        });

        table.getColumns().addAll(colNom, colPrenom, colEmail, colRole, colActions);

        List<Utilisateur> tous = utilisateurDAO.findAll();
        if (tous != null) {
            ObservableList<Utilisateur> data = FXCollections.observableArrayList(tous);
            table.setItems(data);
            search.textProperty().addListener((obs, o, n) -> {
                String f = n.toLowerCase();
                table.setItems(data.filtered(u ->
                    (u.getNom()   != null && u.getNom()  .toLowerCase().contains(f)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(f))));
            });
        }

        mainContent.getChildren().addAll(titre, toolbar, table);
    }

    private void showSallesManagement() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(25, 40, 40, 40));

        mainContent.getChildren().add(btnRetour(this::showAdminPanel));

        ComboBox<String> vueSel = new ComboBox<>();
        vueSel.getItems().addAll("Salles", "Bâtiments");
        vueSel.setValue("Salles");
        vueSel.setStyle("-fx-background-radius: 8; -fx-font-weight: bold;");
        vueSel.setOnAction(e -> {
            if ("Bâtiments".equals(vueSel.getValue())) showBatimentsManagement();
            else showSallesManagement();
        });

        HBox headerBox = new HBox(20, creerTitre("🏢 RESEAU DES LOCAUX"), vueSel);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        List<Salle> salles = salleDAO.findAll();
        if (salles == null) salles = new java.util.ArrayList<>();

        long libres   = salles.stream()
            .filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle())).count();
        long occupees = salles.size() - libres;

        PieChart pie = new PieChart(javafx.collections.FXCollections.observableArrayList(
            new PieChart.Data("Disponibles", libres),
            new PieChart.Data("Occupées",    occupees)
        ));
        pie.setTitle("REPARTITION DES LOCAUX");
        pie.setLabelsVisible(false);
        pie.setLegendVisible(true);
        pie.setAnimated(true);
        pie.setPrefSize(380, 300);

        VBox chartBox = new VBox(pie);
        chartBox.setAlignment(Pos.CENTER);
        chartBox.setPadding(new Insets(16));
        chartBox.setMaxWidth(420);
        chartBox.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 20;");

        final String C_DISPO = VERT_LIME;   
        final String C_OCC   = "#EF4444";

        javafx.application.Platform.runLater(() -> {
            java.util.List<PieChart.Data> data = pie.getData();
            String[] SLICE_COLORS = {C_DISPO, C_OCC};
            for (int i = 0; i < data.size(); i++) {
                javafx.scene.Node node = data.get(i).getNode();
                if (node == null) continue;
                String c = SLICE_COLORS[i % 2];
                node.setStyle("-fx-pie-color: " + c + "; -fx-border-color: " + c + "; -fx-border-width: 2;");
                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                glow.setColor(javafx.scene.paint.Color.web(c));
                glow.setRadius(18);
                node.setEffect(glow);
            }
            int idx = 0;
            for (javafx.scene.Node sym : pie.lookupAll(".chart-legend-item-symbol")) {
                sym.setStyle("-fx-background-color: " + SLICE_COLORS[idx % 2] + ", white;");
                idx++;
            }
            for (javafx.scene.Node item : pie.lookupAll(".chart-legend-item")) {
                if (item instanceof Label)
                    ((Label) item).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }
            javafx.scene.Node chartTitle = pie.lookup(".chart-title");
            if (chartTitle != null)
                chartTitle.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-family: 'Consolas'; -fx-font-size: 15;");
        });

        Button btnAddS = new Button("+ NOUVELLE SALLE");
        btnAddS.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME +
            "; -fx-font-weight: bold; -fx-padding: 10 18; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAddS.setOnAction(e -> dialogAjouterSalle());

        Button btnAddB = new Button("+ NOUVEAU BATIMENT");
        btnAddB.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME +
            "; -fx-font-weight: bold; -fx-padding: 10 18; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAddB.setOnAction(e -> dialogAjouterBatiment());

        HBox toolbar = new HBox(14, btnAddS, btnAddB);
        toolbar.setPadding(new Insets(0, 0, 12, 0));

        TableView<Salle> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(380);
        table.setStyle("-fx-selection-bar: " + VERT_LIME + ";");

        TableColumn<Salle, String> colNum = new TableColumn<>("N° SALLE");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numeroSalle"));

        TableColumn<Salle, String> colBat = new TableColumn<>("BATIMENT");
        colBat.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getBatiment() != null
                ? cd.getValue().getBatiment().getNomBatiment() : "—"));

        TableColumn<Salle, Integer> colCap = new TableColumn<>("CAPACITE");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colCap.setPrefWidth(90);

        TableColumn<Salle, String> colCat = new TableColumn<>("TYPE");
        colCat.setCellValueFactory(new PropertyValueFactory<>("categorieSalle"));
        colCat.setPrefWidth(100);

        TableColumn<Salle, String> colEtat = new TableColumn<>("ETAT");
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatSalle"));
        colEtat.setPrefWidth(110);
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v.toUpperCase());
                boolean dispo = "Disponible".equalsIgnoreCase(v);
                setStyle("-fx-text-fill: " + (dispo ? "#16a34a" : "#ef4444") + "; -fx-font-weight: bold;");
            }
        });

        TableColumn<Salle, Void> colAct = new TableColumn<>("ACTIONS");
        colAct.setPrefWidth(100);
        colAct.setCellFactory(p -> new TableCell<>() {
            private final Button e = new Button("✏️");
            private final Button d = new Button("🗑");
            private final HBox   h = new HBox(10, e, d);
            {
                h.setAlignment(Pos.CENTER);
                e.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                d.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
                e.setOnAction(ev -> dialogModifierSalle(getTableView().getItems().get(getIndex())));
                d.setOnAction(ev -> confirmerSuppressionSalle(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : h);
            }
        });

        table.getColumns().addAll(colNum, colBat, colCap, colCat, colEtat, colAct);
        if (!salles.isEmpty()) table.getItems().addAll(salles);

        HBox mainRow = new HBox(30, table, chartBox);
        mainRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(table, Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, toolbar, mainRow);
    }

    private void showBatimentsManagement() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(25, 40, 40, 40));

        mainContent.getChildren().add(btnRetour(this::showSallesManagement));

        Button btnAdd = new Button("+ NOUVEAU BATIMENT");
        styliserBoutonSecondaire(btnAdd);
        btnAdd.setOnAction(e -> dialogAjouterBatiment());

        HBox toolbar = new HBox(15, btnAdd);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        TableView<Batiment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(420);
        table.setPlaceholder(new Label("Aucun bâtiment enregistré."));

        TableColumn<Batiment, String> colCode = new TableColumn<>("CODE");
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeBatiment"));
        colCode.setPrefWidth(90);

        TableColumn<Batiment, String> colNom  = new TableColumn<>("NOM");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomBatiment"));

        TableColumn<Batiment, String> colLoc  = new TableColumn<>("LOCALISATION");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("localisationBatiment"));

        TableColumn<Batiment, Integer> colEt  = new TableColumn<>("ETAGES");
        colEt.setCellValueFactory(new PropertyValueFactory<>("nbEtage"));
        colEt.setPrefWidth(80);

        table.getColumns().addAll(colCode, colNom, colLoc, colEt);
        List<Batiment> bats = batimentDAO.findAll();
        if (bats != null) table.getItems().addAll(bats);

        mainContent.getChildren().addAll(creerTitre("🏗️ REPERTOIRE DES BATIMENTS"), toolbar, table);
    }

    @FXML
    private void showInventoryManagement() {
        preparerContenu("-fx-background-color: " + FOND_BLANC + ";");
        mainContent.setPadding(new Insets(25, 40, 40, 40));

        mainContent.getChildren().add(btnRetour(this::showAdminPanel));

        Button btnAdd = new Button("+ NOUVEL EQUIPEMENT");
        styliserBoutonSecondaire(btnAdd);
        btnAdd.setOnAction(e -> dialogAjouterEquipement());

        HBox toolbar = new HBox(15, btnAdd);
        toolbar.setPadding(new Insets(10, 0, 10, 0));

        TableView<Equipement> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(460);
        table.setPlaceholder(new Label("Aucun équipement enregistré."));

        TableColumn<Equipement, String> colNom   = new TableColumn<>("EQUIPEMENT");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomEquipement"));

        TableColumn<Equipement, String> colEtat  = new TableColumn<>("ETAT");
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatFonctionnement"));
        colEtat.setPrefWidth(130);
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                String color = v.contains("panne") || v.contains("maintenance") ? "#ef4444" : "#16a34a";
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        TableColumn<Equipement, String> colSalle = new TableColumn<>("SALLE");
        colSalle.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getSalle() != null ? cd.getValue().getSalle().getNumeroSalle() : "Stock"));
        colSalle.setPrefWidth(90);

        TableColumn<Equipement, Void> colAct = new TableColumn<>("ACTIONS");
        colAct.setPrefWidth(100);
        colAct.setCellFactory(p -> new TableCell<>() {
            private final Button e = new Button("✏️");
            private final Button d = new Button("🗑");
            private final HBox h  = new HBox(10, e, d);
            { h.setAlignment(Pos.CENTER);
              e.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
              d.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
              e.setOnAction(ev -> dialogModifierEquipement(getTableView().getItems().get(getIndex())));
              d.setOnAction(ev -> confirmerSuppressionEquipement(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : h); }
        });

        table.getColumns().addAll(colNom, colEtat, colSalle, colAct);
        List<Equipement> equips = equipementDAO.findAll();
        if (equips != null) table.getItems().addAll(equips);

        mainContent.getChildren().addAll(creerTitre("⚙️ INVENTAIRE MATERIEL"), toolbar, table);
    }

    @FXML
    public void moduleGestionCours() {
        preparerContenu("-fx-background-color: #f8fafc;");
        mainContent.setPadding(new Insets(20, 40, 40, 40));

        mainContent.getChildren().add(btnRetour(this::showManagerDashboard));

        VBox header = construireHeader("📅 GESTION DES COURS",
            "Création, modification et suppression des unités d'enseignement.");

        TextField search = new TextField();
        search.setPromptText("🔍 Rechercher un code ou une matière...");
        search.setPrefWidth(340);
        search.setStyle("-fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");

        Button btnAdd = new Button("+ NOUVEAU COURS");
        styliserBoutonSecondaire(btnAdd);
        btnAdd.setOnAction(e -> dialogAjouterCours());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox toolbar = new HBox(15, search, sp, btnAdd);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(20, 0, 20, 0));

        TableView<Cours> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(420);
        table.setPlaceholder(new Label("Aucun cours enregistré."));

        TableColumn<Cours, String> colCode = new TableColumn<>("CODE COURS");
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeCours"));
        colCode.setPrefWidth(120);

        TableColumn<Cours, String> colMat = new TableColumn<>("INTITULÉ DE LA MATIÈRE");
        colMat.setCellValueFactory(new PropertyValueFactory<>("intituleMatiere"));

        TableColumn<Cours, Double> colHeures = new TableColumn<>("VOLUME HORAIRE (H)");
        colHeures.setCellValueFactory(new PropertyValueFactory<>("nbrHeure"));
        colHeures.setPrefWidth(140);
        
        TableColumn<Cours, Double> colFait = new TableColumn<>("HEURES FAITES");
        colFait.setCellValueFactory(new PropertyValueFactory<>("heuresEffectuees"));
        colFait.setPrefWidth(130);

        TableColumn<Cours, Void> colAct = new TableColumn<>("ACTIONS");
        colAct.setPrefWidth(100);
        colAct.setCellFactory(p -> new TableCell<>() {
            private final Button eBtn = new Button("✏️");
            private final Button dBtn = new Button("🗑");
            private final HBox h = new HBox(10, eBtn, dBtn);
            {
                h.setAlignment(Pos.CENTER);
                eBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                dBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
                eBtn.setOnAction(ev -> dialogModifierCours(getTableView().getItems().get(getIndex())));
                dBtn.setOnAction(ev -> confirmerSuppressionCours(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : h);
            }
        });

        table.getColumns().addAll(colCode, colMat, colHeures, colFait, colAct);

        try {
            CoursDAO coursDAO = new CoursDAO(HibernateUtil.getSessionFactory());
            List<Cours> liste = coursDAO.findAll();
            if (liste != null) {
                ObservableList<Cours> data = FXCollections.observableArrayList(liste);
                table.setItems(data);
                
                search.textProperty().addListener((obs, o, n) -> {
                    String f = n.toLowerCase();
                    table.setItems(data.filtered(c -> 
                        (c.getCodeCours() != null && c.getCodeCours().toLowerCase().contains(f)) ||
                        (c.getIntituleMatiere() != null && c.getIntituleMatiere().toLowerCase().contains(f))
                    ));
                });
            }
        } catch (Exception ex) {
            System.err.println("LOG : Erreur de chargement des cours : " + ex.getMessage());
        }

        mainContent.getChildren().addAll(header, toolbar, table);
    }

    private void dialogAjouterCours() {
        Dialog<ButtonType> dlg = creerDialog("ENREGISTRER UN NOUVEAU COURS");

        TextField txtCode = creerChampCyber("Ex: INFO-301");
        TextField txtMat  = creerChampCyber("Ex: Algorithmique Avancée");
        TextField txtHeures = creerChampCyber("Ex: 40.5 (Volume global)");

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("CODE DU COURS :"), txtCode,
            creerLabel("INTITULÉ DE LA MATIÈRE :"), txtMat,
            creerLabel("VOLUME HORAIRE TOTAL :"), txtHeures
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            
            if (txtCode.getText().trim().isEmpty() || txtMat.getText().trim().isEmpty() || txtHeures.getText().trim().isEmpty()) {
                afficherAlerte("Erreur", "Tous les champs sont obligatoires.");
                return;
            }
            
            try {
                Cours c = new Cours();
                c.setCodeCours(txtCode.getText().trim().toUpperCase());
                c.setIntituleMatiere(txtMat.getText().trim());
                c.setNbrHeure(Double.parseDouble(txtHeures.getText().trim()));
                c.setHeuresEffectuees(0.0); // Initialisé à zéro à la création
                
                CoursDAO coursDAO = new CoursDAO(HibernateUtil.getSessionFactory());
                coursDAO.save(c);
                moduleGestionCours(); // Rafraîchit le tableau
                afficherAlerte("Succès", "Le cours " + c.getCodeCours() + " a été ajouté avec succès.");
            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur de saisie", "Le volume horaire doit être un nombre valide (ex: 40 ou 40.5).");
            } catch (Exception ex) {
                afficherAlerte("Erreur base de données", "Impossible de sauvegarder le cours. Le code existe peut-être déjà.");
            }
        });
    }

    private void dialogModifierCours(Cours cours) {
        Dialog<ButtonType> dlg = creerDialog("MODIFIER LE COURS " + cours.getCodeCours());

        TextField txtMat = creerChampCyber(cours.getIntituleMatiere() != null ? cours.getIntituleMatiere() : "");
        TextField txtHeures = creerChampCyber(String.valueOf(cours.getNbrHeure()));

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("INTITULÉ DE LA MATIÈRE :"), txtMat,
            creerLabel("VOLUME HORAIRE TOTAL :"), txtHeures
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            
            if (txtMat.getText().trim().isEmpty() || txtHeures.getText().trim().isEmpty()) {
                afficherAlerte("Erreur", "Tous les champs sont obligatoires.");
                return;
            }
            
            try {
                cours.setIntituleMatiere(txtMat.getText().trim());
                cours.setNbrHeure(Double.parseDouble(txtHeures.getText().trim()));
                
                CoursDAO coursDAO = new CoursDAO(HibernateUtil.getSessionFactory());
                coursDAO.update(cours);
                moduleGestionCours(); // Rafraîchit le tableau
            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur de saisie", "Le volume horaire doit être un nombre valide.");
            } catch (Exception ex) {
                afficherAlerte("Erreur base de données", "Modification échouée : " + ex.getMessage());
            }
        });
    }

    private void confirmerSuppressionCours(Cours cours) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression"); 
        a.setHeaderText("Supprimer le cours " + cours.getCodeCours() + " ?");
        a.setContentText("Attention : Cette action est irréversible. Si ce cours est lié à des créneaux dans l'emploi du temps, la suppression échouera pour préserver l'intégrité des données.");
        
        a.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                CoursDAO coursDAO = new CoursDAO(HibernateUtil.getSessionFactory());
                coursDAO.delete(cours); 
                moduleGestionCours();
            } catch (Exception ex) {
                afficherAlerte("Erreur critique", "Ce cours ne peut pas être supprimé. Il est probablement déjà assigné à un enseignant ou à un créneau.");
            }
        });
    }

    @FXML
    public void moduleAssignationSalles() {
        preparerContenu("-fx-background-color: #f8fafc;");
        mainContent.setPadding(new Insets(20, 40, 40, 40));
        mainContent.getChildren().add(btnRetour(this::showManagerDashboard));

        VBox header = construireHeader("🔑 ASSIGNATION DES SALLES",
            "Liaison des cours aux infrastructures physiques disponibles.");

        // --- TABLEAU GAUCHE : COURS ---
        TableView<Cours> tableCours = new TableView<>();
        tableCours.setPrefSize(380, 350);
        tableCours.setPlaceholder(new Label("Aucun cours disponible."));
        
        TableColumn<Cours, String> colCode = new TableColumn<>("CODE");
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeCours"));
        colCode.setPrefWidth(100);
        
        TableColumn<Cours, String> colMat = new TableColumn<>("MATIÈRE");
        colMat.setCellValueFactory(new PropertyValueFactory<>("intituleMatiere"));
        
        tableCours.getColumns().addAll(colCode, colMat);
        tableCours.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // --- TABLEAU DROITE : SALLES DISPONIBLES ---
        TableView<Salle> tableSalles = new TableView<>();
        tableSalles.setPrefSize(380, 350);
        tableSalles.setPlaceholder(new Label("Aucune salle libre."));
        
        TableColumn<Salle, String> colNum = new TableColumn<>("SALLE");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numeroSalle"));
        
        TableColumn<Salle, Integer> colCap = new TableColumn<>("CAPACITÉ");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        
        tableSalles.getColumns().addAll(colNum, colCap);
        tableSalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // --- CHARGEMENT DES DONNÉES ---
        try {
            CoursDAO coursDAO = new CoursDAO(HibernateUtil.getSessionFactory());
            List<Cours> listeCours = coursDAO.findAll();
            if (listeCours != null) tableCours.getItems().addAll(listeCours);

            List<Salle> listeSalles = salleDAO.findAll().stream()
                .filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle()))
                .collect(Collectors.toList());
            if (listeSalles != null) tableSalles.getItems().addAll(listeSalles);
        } catch (Exception e) {
            System.err.println("LOG : Erreur de chargement pour l'assignation.");
        }

        // --- MISE EN PAGE ---
        VBox boxCours = new VBox(12, new Label("📋 COURS EN ATTENTE"){{setStyle("-fx-font-weight:bold;-fx-text-fill:"+BLEU_DEEP+";");}}, tableCours);
        boxCours.setStyle("-fx-background-color:white;-fx-padding:20;-fx-background-radius:12;-fx-border-color:#e2e8f0;-fx-border-radius:12;");

        Label fleche = new Label("➡");
        fleche.setStyle("-fx-font-size: 40; -fx-text-fill: " + VERT_LIME + ";");
        VBox boxFleche = new VBox(fleche); boxFleche.setAlignment(Pos.CENTER);

        VBox boxSalles = new VBox(12, new Label("🏢 SALLES DISPONIBLES"){{setStyle("-fx-font-weight:bold;-fx-text-fill:"+BLEU_DEEP+";");}}, tableSalles);
        boxSalles.setStyle("-fx-background-color:white;-fx-padding:20;-fx-background-radius:12;-fx-border-color:#e2e8f0;-fx-border-radius:12;");

        HBox mapping = new HBox(30, boxCours, boxFleche, boxSalles);
        mapping.setAlignment(Pos.CENTER);
        mapping.setPadding(new Insets(20, 0, 30, 0));

        // --- BOUTON DE VALIDATION ---
        Button btnValider = new Button("VALIDER L'ASSIGNATION");
        styliserBoutonPrimaire(btnValider);
        btnValider.setPrefHeight(45);
        btnValider.setPrefWidth(300);
        
        btnValider.setOnAction(e -> {
            Cours c = tableCours.getSelectionModel().getSelectedItem();
            Salle s = tableSalles.getSelectionModel().getSelectedItem();
            
            if (c == null || s == null) {
                afficherAlerte("Sélection incomplète", "Veuillez sélectionner un cours à gauche ET une salle à droite.");
                return;
            }
            dialogFinaliserAssignation(c, s);
        });

        VBox layout = new VBox(15, header, mapping, btnValider);
        layout.setAlignment(Pos.TOP_CENTER);
        mainContent.getChildren().add(layout);
    }
    
    private void dialogFinaliserAssignation(Cours cours, Salle salle) {
        Dialog<ButtonType> dlg = creerDialog("PLANIFIER : " + cours.getCodeCours() + " ➔ Salle " + salle.getNumeroSalle());

        // 1. Remplacement du ComboBox par un DatePicker car la base attend une LocalDate
        DatePicker dpDate = new DatePicker(java.time.LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        dpDate.setStyle("-fx-background-color: #1a252f; -fx-control-inner-background: #1a252f; -fx-text-fill: white; -fx-border-color: #A3FF3333; -fx-border-radius: 7; -fx-background-radius: 7; -fx-padding: 4;");

        String[] heures = {"08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00"};
        ComboBox<String> cbDebut = creerComboCyber(heures);
        ComboBox<String> cbFin = creerComboCyber(heures);

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("DATE DE LA SÉANCE :"), dpDate,
            creerLabel("HEURE DE DÉBUT :"), cbDebut,
            creerLabel("HEURE DE FIN :"), cbFin
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            
            if (dpDate.getValue() == null || cbDebut.getValue() == null || cbFin.getValue() == null) {
                afficherAlerte("Erreur", "Veuillez remplir tous les champs horaires.");
                return;
            }
            
            try {
                // Création du créneau
                Creneau creneau = new Creneau();
                
                // Génération d'un identifiant unique pour le créneau
                creneau.setIdCreneau(java.util.UUID.randomUUID().toString()); 
                
                creneau.setCours(cours);
                creneau.setSalle(salle);
                
                // Récupération de la LocalDate directement depuis le DatePicker
                creneau.setDateSeance(dpDate.getValue()); 
                
                // Conversion des String ("08:00") en LocalTime
                creneau.setHeureDebut(LocalTime.parse(cbDebut.getValue())); 
                creneau.setHeureFin(LocalTime.parse(cbFin.getValue())); 
                
                // Sauvegarde en base de données
                creneauDAO.save(creneau);
                
                // Optionnel : passer la salle en occupée si c'est pour aujourd'hui
                if(dpDate.getValue().equals(java.time.LocalDate.now())) {
                    salle.setEtatSalle("Occupée");
                    salleDAO.update(salle);
                }
                
                afficherAlerte("Succès", "Le cours " + cours.getCodeCours() + " a bien été planifié le " + dpDate.getValue() + " en salle " + salle.getNumeroSalle() + " !");
                moduleAssignationSalles(); 
                
            } catch (Exception ex) {
                ex.printStackTrace();
                afficherAlerte("Erreur", "Impossible d'enregistrer l'assignation : " + ex.getMessage());
            }
        });
    }
    
    @FXML
    public void moduleGenerationPlanning() {
        preparerContenu("-fx-background-color: #f8fafc;");
        mainContent.setPadding(new Insets(20, 40, 40, 40));
        mainContent.getChildren().add(btnRetour(this::showManagerDashboard));

        VBox header = construireHeader("📄 GENERER L'EMPLOI DU TEMPS",
            "Compilation finale et export des données.");

        HBox exports = new HBox(30);
        exports.setAlignment(Pos.CENTER);
        exports.setPadding(new Insets(40, 0, 0, 0));

        exports.getChildren().addAll(
            creerNodeTHM("EXPORT PDF",   "📑", "Fichier pour impression",     e -> afficherAlerte("PDF",   "Export PDF — à connecter à iText ou Apache PDFBox.")),
            creerNodeTHM("EXPORT EXCEL", "📊", "Fichier de données tableur",  e -> afficherAlerte("Excel", "Export Excel — à connecter à Apache POI.")),
            creerNodeTHM("EXPORT WEB",   "🌐", "Publication sur intranet",    e -> afficherAlerte("Web",   "Export HTML — génération de page statique."))
        );

        mainContent.getChildren().addAll(header, exports);
    }

    private void dialogAjouterUtilisateur() {
        Dialog<ButtonType> dlg = creerDialog("ENREGISTRER UN COLLABORATEUR");

        TextField txtNom    = creerChampCyber("Nom complet");
        TextField txtPrenom = creerChampCyber("Prénom");
        TextField txtEml    = creerChampCyber("Email (ex: prof@univ.sn)");
        TextField txtIdConn = creerChampCyber("Identifiant connexion");
        PasswordField txtPw = new PasswordField();
        appliquerStyleFieldCyber(txtPw); txtPw.setPromptText("Mot de passe");

        ComboBox<String> cbRole = creerComboCyber(
            "Administrateur", "Gestionnaire", "Enseignant", "Etudiant");
        cbRole.setValue("Enseignant");

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("IDENTITE :"),  txtNom, txtPrenom,
            creerLabel("ACCES :"),     txtEml, txtIdConn, txtPw,
            creerLabel("ROLE :"),      cbRole
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            if (txtNom.getText().isEmpty() || txtEml.getText().isEmpty() || txtPw.getText().isEmpty()) {
                afficherAlerte("Champs manquants", "Nom, email et mot de passe sont obligatoires.");
                return;
            }
            Utilisateur u = new Utilisateur();
            u.setNom(txtNom.getText().trim());
            u.setPrenom(txtPrenom.getText().trim());
            u.setEmail(txtEml.getText().trim());
            u.setIdentifiantConnexion(txtIdConn.getText().isEmpty() ? txtEml.getText().trim() : txtIdConn.getText().trim());
            u.setMotDePasse(txtPw.getText());
            u.setRole(cbRole.getValue());
            try {
                utilisateurDAO.save(u);
                showUserManagement();
                afficherAlerte("Succès", u.getNom() + " a été ajouté au système.");
            } catch (Exception ex) {
                ex.printStackTrace();
                afficherAlerte("Erreur", "Impossible d'enregistrer : " + ex.getMessage());
            }
        });
    }

    private void dialogModifierUtilisateur(Utilisateur u) {
        Dialog<ButtonType> dlg = creerDialog("MODIFIER LE PROFIL");

        TextField txtNom   = creerChampCyber(u.getNom() != null ? u.getNom() : "");
        TextField txtEmail = creerChampCyber(u.getEmail() != null ? u.getEmail() : "");
        ComboBox<String> cbRole = creerComboCyber("Administrateur","Gestionnaire","Enseignant","Etudiant");
        cbRole.setValue(u.getRole());

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("NOM :"),   txtNom,
            creerLabel("EMAIL :"), txtEmail,
            creerLabel("ROLE :"),  cbRole
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            u.setNom(txtNom.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setRole(cbRole.getValue());
            try { utilisateurDAO.update(u); showUserManagement(); }
            catch (Exception ex) { afficherAlerte("Erreur", ex.getMessage()); }
        });
    }

    private void confirmerSuppressionUser(Utilisateur u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression"); alert.setHeaderText("Supprimer " + u.getNom() + " ?");
        alert.setContentText("Cette action est irréversible.");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            utilisateurDAO.delete(u); showUserManagement();
        });
    }

    private void dialogAjouterSalle() {
        Dialog<ButtonType> dlg = creerDialog("PARAMETRER UNE NOUVELLE SALLE");

        TextField txtNum = creerChampCyber("N° de salle (ex: A101)");
        TextField txtCap = creerChampCyber("Capacité maximale");
        ComboBox<Batiment> cbBat = new ComboBox<>();
        cbBat.getItems().addAll(batimentDAO.findAll());
        cbBat.setMaxWidth(Double.MAX_VALUE);
        cbBat.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Batiment b) { return b == null ? "" : b.getNomBatiment(); }
            public Batiment fromString(String s) { return null; }
        });
        appliquerStyleFieldCyber(cbBat);
        cbBat.setPromptText("Sélectionner un bâtiment");

        ComboBox<String> cbCat = creerComboCyber("TD", "TP", "Amphithéâtre");
        cbCat.setValue("TD");

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("NUMERO :"),    txtNum,
            creerLabel("CAPACITE :"),  txtCap,
            creerLabel("BATIMENT :"),  cbBat,
            creerLabel("TYPE :"),      cbCat
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            if (txtNum.getText().isEmpty() || txtCap.getText().isEmpty() || cbBat.getValue() == null) {
                afficherAlerte("Données manquantes", "Tous les champs sont obligatoires.");
                return;
            }
            try {
                Salle s = new Salle();
                s.setNumeroSalle(txtNum.getText().trim());
                s.setCapacite(Integer.parseInt(txtCap.getText().trim()));
                s.setBatiment(cbBat.getValue());
                s.setCategorieSalle(cbCat.getValue());
                s.setEtatSalle("Disponible");
                salleDAO.save(s);
                showSallesManagement();
                afficherAlerte("Succès", "Salle " + s.getNumeroSalle() + " enregistrée.");
            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur", "La capacité doit être un entier.");
            }
        });
    }

    private void dialogModifierSalle(Salle salle) {
        Dialog<ButtonType> dlg = creerDialog("MODIFIER LA SALLE " + salle.getNumeroSalle());

        TextField txtCap = creerChampCyber(String.valueOf(salle.getCapacite()));
        ComboBox<Batiment> cbBat = new ComboBox<>();
        cbBat.getItems().addAll(batimentDAO.findAll());
        cbBat.setValue(salle.getBatiment());
        cbBat.setMaxWidth(Double.MAX_VALUE);
        cbBat.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Batiment b) { return b == null ? "" : b.getNomBatiment(); }
            public Batiment fromString(String s) { return null; }
        });
        appliquerStyleFieldCyber(cbBat);

        ComboBox<String> cbCat = creerComboCyber("TD", "TP", "Amphithéâtre");
        cbCat.setValue(salle.getCategorieSalle());

        ComboBox<String> cbEtat = creerComboCyber("Disponible", "Occupée", "Maintenance");
        cbEtat.setValue(salle.getEtatSalle());

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("CAPACITE :"),  txtCap,
            creerLabel("BATIMENT :"),  cbBat,
            creerLabel("TYPE :"),      cbCat,
            creerLabel("ETAT :"),      cbEtat
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            try {
                salle.setCapacite(Integer.parseInt(txtCap.getText().trim()));
                salle.setBatiment(cbBat.getValue());
                salle.setCategorieSalle(cbCat.getValue());
                salle.setEtatSalle(cbEtat.getValue());
                salleDAO.update(salle);
                showSallesManagement();
            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur", "Capacité invalide.");
            }
        });
    }

    private void confirmerSuppressionSalle(Salle s) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression"); a.setHeaderText("Supprimer la salle " + s.getNumeroSalle() + " ?");
        a.setContentText("Cette action est irréversible.");
        a.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            salleDAO.delete(s); showSallesManagement();
        });
    }

    private void dialogAjouterBatiment() {
        Dialog<ButtonType> dlg = creerDialog("ENREGISTRER UN BATIMENT");

        TextField txtCode  = creerChampCyber("Code (ex: BAT-A)");
        TextField txtNom   = creerChampCyber("Nom du bâtiment");
        TextField txtLoc   = creerChampCyber("Localisation");
        TextField txtEt    = creerChampCyber("Nombre d'étages");
        ComboBox<String> cbType = creerComboCyber("Pédagogique", "Administratif", "Laboratoire");
        cbType.setValue("Pédagogique");

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("CODE :"),         txtCode,
            creerLabel("NOM :"),          txtNom,
            creerLabel("LOCALISATION :"), txtLoc,
            creerLabel("ETAGES :"),       txtEt,
            creerLabel("TYPE :"),         cbType
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            try {
                Batiment b = new Batiment(
                    txtCode.getText().trim(), txtNom.getText().trim(),
                    txtLoc.getText().trim(), Integer.parseInt(txtEt.getText().trim()),
                    cbType.getValue()
                );
                batimentDAO.save(b);
                showBatimentsManagement();
                afficherAlerte("Succès", "Bâtiment " + b.getNomBatiment() + " enregistré.");
            } catch (Exception ex) {
                afficherAlerte("Erreur", "Données invalides. Vérifiez le nombre d'étages.");
            }
        });
    }

    private void dialogAjouterEquipement() {
        Dialog<ButtonType> dlg = creerDialog("ENREGISTRER UN EQUIPEMENT");

        TextField txtId = creerChampCyber("Code inventaire (ex: VP-01)");
        ComboBox<String> cbType = creerComboCyber("Vidéoprojecteur","Tableau Interactif","Climatisation","Ordinateur","Autre");
        cbType.setPromptText("Type de matériel");
        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.getItems().addAll(salleDAO.findAll());
        cbSalle.setPromptText("Assigner à une salle (optionnel)");
        cbSalle.setMaxWidth(Double.MAX_VALUE);
        cbSalle.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Salle s) { return s == null ? "" : "Salle " + s.getNumeroSalle(); }
            public Salle fromString(String s) { return null; }
        });
        appliquerStyleFieldCyber(cbSalle);

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("CODE :"),  txtId,
            creerLabel("TYPE :"),  cbType,
            creerLabel("SALLE :"), cbSalle
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            if (txtId.getText().isEmpty() || cbType.getValue() == null) {
                afficherAlerte("Champs manquants", "Le code et le type sont obligatoires.");
                return;
            }
            Equipement eq = new Equipement();
            eq.setIdEquipement(txtId.getText().trim());
            eq.setNomEquipement(cbType.getValue());
            eq.setEtatFonctionnement("Opérationnel");
            eq.setSalle(cbSalle.getValue());
            try { equipementDAO.save(eq); showInventoryManagement(); }
            catch (Exception ex) { afficherAlerte("Erreur", ex.getMessage()); }
        });
    }

    private void dialogModifierEquipement(Equipement eq) {
        Dialog<ButtonType> dlg = creerDialog("ETAT : " + eq.getNomEquipement());

        ComboBox<String> cbEtat = creerComboCyber("Opérationnel", "En panne", "En maintenance");
        cbEtat.setValue(eq.getEtatFonctionnement());

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(creerLabel("NOUVEL ETAT :"), cbEtat);

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            eq.setEtatFonctionnement(cbEtat.getValue());
            equipementDAO.update(eq);
            showInventoryManagement();
        });
    }

    private void confirmerSuppressionEquipement(Equipement eq) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression"); a.setHeaderText("Supprimer " + eq.getIdEquipement() + " ?");
        a.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            equipementDAO.delete(eq); showInventoryManagement();
        });
    }

    public void showSignalerIncidentDialog(Utilisateur enseignant) {
        Dialog<ButtonType> dlg = creerDialog("⚠️ SIGNALER UN INCIDENT");

        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.getItems().addAll(salleDAO.findAll());
        cbSalle.setPromptText("Salle concernée...");
        cbSalle.setMaxWidth(Double.MAX_VALUE);
        cbSalle.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Salle s) { return s == null ? "" : "Salle " + s.getNumeroSalle(); }
            public Salle fromString(String s) { return null; }
        });
        appliquerStyleFieldCyber(cbSalle);

        ComboBox<String> cbType = creerComboCyber(
            "Equipement défectueux", "Propreté", "Température", "Eclairage", "Autre");
        cbType.setValue("Equipement défectueux");

        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Décrivez le problème rencontré...");
        txtDesc.setPrefHeight(90); txtDesc.setWrapText(true);
        txtDesc.setStyle("-fx-control-inner-background: #121a21; -fx-text-fill: white; " +
                         "-fx-border-color: #2d3f50; -fx-border-radius: 6; -fx-background-radius: 6;");

        VBox form = getDialogForm(dlg);
        form.getChildren().addAll(
            creerLabel("SALLE :"),       cbSalle,
            creerLabel("TYPE :"),        cbType,
            creerLabel("DESCRIPTION :"), txtDesc
        );

        dlg.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            if (cbSalle.getValue() == null || txtDesc.getText().trim().isEmpty()) {
                afficherAlerte("Données manquantes", "Veuillez sélectionner une salle et décrire le problème.");
                return;
            }
            String msg = "[" + cbType.getValue() + "] Salle " +
                         cbSalle.getValue().getNumeroSalle() + " — " + txtDesc.getText().trim() +
                         " (signalé par " + (enseignant != null ? enseignant.getNom() : "?") + ")";
            notifService.notifierIncidentTechnique(cbType.getValue(), cbSalle.getValue().getNumeroSalle());
            afficherAlerte("Signalement envoyé", "L'incident a été transmis à l'administration.");
        });
    }

    private VBox construireBlockIA() {
        VBox bloc = new VBox(18);
        bloc.setPadding(new Insets(30, 28, 30, 28));
        bloc.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 28; " +
                      "-fx-border-color: " + VERT_LIME + "55; -fx-border-width: 1.5; -fx-border-radius: 28;");

        Label titre = new Label("🤖 OPTIMISATION PAR IA");
        titre.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-family: 'Consolas'; " +
                       "-fx-font-size: 20; -fx-font-weight: bold;");
        
        Label desc = new Label("Analyse prédictive des ressources — Détection des conflits et des pannes.");
        desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");

        Button btnIA = new Button("LANCER LE DIAGNOSTIC SYSTEME");
        btnIA.setMaxWidth(Double.MAX_VALUE); 
        btnIA.setPrefHeight(44);
        styliserBoutonPrimaire(btnIA);

        btnIA.setOnAction(e -> {
            btnIA.setDisable(true); 
            btnIA.setText("⚡ INITIALISATION...");

            PauseTransition pLocal = new PauseTransition(Duration.seconds(1.8));
            pLocal.setOnFinished(ev -> { 
                lancerDiagnosticIA(); 
                btnIA.setDisable(false); 
                btnIA.setText("LANCER LE DIAGNOSTIC SYSTEME"); 
            });
            pLocal.play();
        });

        bloc.getChildren().addAll(titre, desc, btnIA);
        return bloc;
    }

    private void lancerDiagnosticIA() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("[ SESSION IA — ")
               .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
               .append(" ]\n\n");

        int alertes = 0, optim = 0;

        List<Equipement> equips = equipementDAO.findAll();
        if (equips != null) {
            for (Equipement eq : equips) {
                String etat = eq.getEtatFonctionnement();
                if (etat != null && (etat.contains("panne") || etat.contains("maintenance"))) {
                    alertes++;
                    rapport.append("⚠️  ").append(eq.getNomEquipement())
                           .append(" [").append(eq.getIdEquipement()).append("] — ")
                           .append(etat).append(" (Salle ")
                           .append(eq.getSalle() != null ? eq.getSalle().getNumeroSalle() : "Stock")
                           .append(")\n");
                }
            }
        }

        List<Salle> salles = salleDAO.findAll();
        if (salles != null) {
            for (Salle s : salles) {
                if (s.getCapacite() < 15) {
                    optim++;
                    rapport.append("ℹ️  Salle ").append(s.getNumeroSalle())
                           .append(" — capacité réduite (").append(s.getCapacite())
                           .append(" places) → usage privilégié pour TDs.\n");
                }
            }
        }

        rapport.append("\n─────────────────────────────\n");
        if (alertes > 0) {
            rapport.append("❌ ").append(alertes).append(" CONFLIT(S) MATERIEL DETECTE(S).\n");
            rapport.append("   → Intervention recommandée avant les réservations.\n");
        } else {
            rapport.append("✅ SYSTEME OPTIMAL — Aucun conflit matériel détecté.\n");
        }
        if (optim > 0)
            rapport.append("💡 ").append(optim).append(" optimisation(s) d'espace suggérée(s).\n");

        afficherDialogIA(rapport.toString());
    }

    private void afficherDialogIA(String contenu) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("UIDT-AI · Terminal d'Analyse");

        VBox root = new VBox(18);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + BLEU_DEEP + "; " +
                      "-fx-border-color: " + VERT_LIME + "; -fx-border-width: 2; " +
                      "-fx-background-radius: 10; -fx-border-radius: 10;");

        Label h = new Label("🤖 ANALYSE PREDICTIVE TERMINEE");
        h.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 17; " +
                   "-fx-font-weight: bold; -fx-font-family: 'Consolas';");

        TextArea area = new TextArea(contenu);
        area.setEditable(false); area.setWrapText(true); area.setPrefHeight(280);
        area.setStyle("-fx-control-inner-background: #0d1520; -fx-text-fill: " + VERT_LIME + "; " +
                      "-fx-font-family: 'Consolas'; -fx-font-size: 12;");

        Button btnClose = new Button("RETOUR A LA CONSOLE");
        btnClose.setMaxWidth(Double.MAX_VALUE); styliserBoutonPrimaire(btnClose);
        btnClose.setOnAction(e -> stage.close());

        root.getChildren().addAll(h, new Separator(), area, btnClose);
        stage.setScene(new Scene(root, 560, 440));
        stage.show();
    }

    @FXML
    public void moduleResolutionConflits() {
        preparerContenu("-fx-background-color: #f8fafc;");
        mainContent.setPadding(new Insets(20, 40, 40, 40));

        mainContent.getChildren().add(btnRetour(this::showManagerDashboard));

        VBox header = construireHeader("⚠️ RÉSOLUTION DES CONFLITS",
            "Détection et correction automatique des chevauchements d'horaires.");
        
        header.lookup("Label").setStyle(
            "-fx-font-family: 'Consolas'; -fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #dc2626;");

        Button btnScan = new Button("⚡ LANCER LA DÉTECTION IA");
        styliserBoutonPrimaire(btnScan);

        VBox resultZone = new VBox(12);
        resultZone.setPadding(new Insets(20));
        resultZone.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        
        Label lblResult = new Label("Cliquez sur 'Lancer la détection' pour analyser les conflits.");
        lblResult.setStyle("-fx-text-fill: " + (GRIS_TEXTE != null ? GRIS_TEXTE : "#64748b") + ";");
        resultZone.getChildren().add(lblResult);

        btnScan.setOnAction(e -> {
            btnScan.setDisable(true); 
            btnScan.setText("⏳ Analyse en cours...");

            PauseTransition pLocal = new PauseTransition(Duration.seconds(1.5));
            pLocal.setOnFinished(ev -> {
                resultZone.getChildren().clear();
                // On passe le bouton à la méthode pour pouvoir le réactiver
                lancerAnalyseConflits(resultZone, btnScan); 
            });
            pLocal.play();
        });

        mainContent.getChildren().addAll(header, btnScan, resultZone);
    }

    private void lancerAnalyseConflits(VBox zone, Button btnScan) {
        // L'analyse est terminée, on réactive le bouton de scan
        btnScan.setDisable(false); 
        btnScan.setText("⚡ RELANCER L'ANALYSE");

        // Simulation de conflits trouvés
        HBox conflit1 = construireLigneConflit("🚨 Conflit : L3 Informatique (Java) et L3 Math (Graphes) programmés en Salle A101 lundi à 08h00.", "#dc2626");
        HBox conflit2 = construireLigneConflit("💡 Avertissement : 3 cours n'ont pas encore de salle assignée.", "#d97706");
        
        // Création du bouton d'action magique
        Button btnResolve = new Button("🛠️ RÉSOUDRE AUTOMATIQUEMENT");
        btnResolve.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        
        // Action du bouton de résolution
        btnResolve.setOnAction(e -> {
            btnResolve.setDisable(true);
            btnResolve.setText("⏳ Résolution en cours...");
            
            PauseTransition pResolve = new PauseTransition(Duration.seconds(2));
            pResolve.setOnFinished(ev -> {
                zone.getChildren().clear();
                zone.getChildren().add(construireLigneConflit("✅ Tous les conflits ont été résolus par l'IA.", "#16a34a"));
                zone.getChildren().add(construireLigneConflit("Déplacement : 'L3 Math (Graphes)' a été réassigné en Salle B202 (Libre).", "#2563eb"));
                zone.getChildren().add(construireLigneConflit("Assignation : Les 3 cours en attente ont été placés dans des salles disponibles.", "#2563eb"));
            });
            pResolve.play();
        });

        zone.getChildren().addAll(conflit1, conflit2, new Separator(), btnResolve);
    }

    private HBox construireLigneConflit(String msg, String couleur) {
        HBox ligne = new HBox(10);
        ligne.setPadding(new Insets(12, 16, 12, 16));
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle("-fx-background-color: " + couleur + "15; -fx-border-color: " + couleur +
                       "44; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label l = new Label(msg);
        l.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 13;");
        ligne.getChildren().add(l);
        return ligne;
    }

    private VBox creerCarteSmartSalle(Salle salle, double score) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20)); card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        HBox head = new HBox();
        Label lNom = new Label(salle.getNumeroSalle());
        lNom.setStyle("-fx-font-weight: bold; -fx-font-size: 22; -fx-text-fill: " + BLEU_DEEP + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label badge = new Label((int) score + "% MATCH");
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color: " + (score >= 90 ? VERT_LIME : "#f1c40f") + "; " +
                       "-fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; " +
                       "-fx-background-radius: 10; -fx-font-size: 10;");
        head.getChildren().addAll(lNom, sp, badge);

        Label lBat = new Label("📍 " + (salle.getBatiment() != null ? salle.getBatiment().getNomBatiment() : "N/A"));
        lBat.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 12;");
        Label lCap = new Label("👥 Capacité : " + salle.getCapacite() + " places");
        lCap.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 12;");
        Label lType = new Label("🏷️ Type : " + (salle.getCategorieSalle() != null ? salle.getCategorieSalle() : "—"));
        lType.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 12;");

        Button btnRes = new Button("SELECTIONNER");
        btnRes.setMaxWidth(Double.MAX_VALUE); btnRes.setPrefHeight(38);
        styliserBoutonPrimaire(btnRes);
        btnRes.setOnAction(e -> dialogFinaliserReservation(salle));

        card.getChildren().addAll(head, new Separator(), lBat, lCap, lType, btnRes);

        String base = "-fx-background-color: white; -fx-background-radius: 18; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);";
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-8);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 18; " +
                          "-fx-effect: dropshadow(three-pass-box, " + VERT_LIME + ", 20, 0, 0, 0);");
        });
        card.setOnMouseExited(e -> { card.setTranslateY(0); card.setStyle(base); });

        return card;
    }

    private void dialogFinaliserReservation(Salle salle) {
        if (txtEmail == null || txtEmail.getText().trim().isEmpty()) {
            afficherAlerte("Email requis", "Veuillez renseigner l'email du responsable avant de sélectionner une salle.");
            if (txtEmail != null) txtEmail.requestFocus();
            return;
        }

        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("UIDT-AI · Validation de réservation");

        VBox root = new VBox(20); root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-border-color: " + VERT_LIME +
                      "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label titre = new Label("🏁 VALIDATION — SALLE " + salle.getNumeroSalle());
        titre.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-family: 'Consolas'; " +
                       "-fx-font-weight: bold; -fx-font-size: 16;");

        String si = "-fx-text-fill: white; -fx-font-family: 'Consolas'; -fx-font-size: 13;";
        Label lCours = new Label("COURS     : " + (txtClasse != null ? txtClasse.getText() : "—")); lCours.setStyle(si);
        Label lEmail = new Label("EMAIL     : " + txtEmail.getText()); lEmail.setStyle(si);
        Label lCap   = new Label("CAPACITE  : " + salle.getCapacite() + " places"); lCap.setStyle(si);
        Label lBat   = new Label("BATIMENT  : " + (salle.getBatiment() != null ? salle.getBatiment().getNomBatiment() : "N/A")); lBat.setStyle(si);

        TextField txtHeure = new TextField("08:00 - 10:00");
        txtHeure.setStyle("-fx-background-color: #121a21; -fx-text-fill: white; " +
                          "-fx-border-color: #2d3f50; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");
        Label lhLabel = new Label("CRENEAU :"); lhLabel.setStyle(si);

        Button btnOk = new Button("CONFIRMER ET NOTIFIER");
        btnOk.setMaxWidth(Double.MAX_VALUE); styliserBoutonPrimaire(btnOk);
        btnOk.setOnAction(ev -> {
            try {
                salle.setEtatSalle("Occupée");
                salleDAO.update(salle);
                String cours = (txtClasse != null ? txtClasse.getText() : "");
                notifService.confirmerReservation(txtEmail.getText(),
                    "Cours [" + cours + "] — Salle " + salle.getNumeroSalle() + " — " + txtHeure.getText());
                stage.close();
                afficherAlerte("Réservation confirmée", "Salle " + salle.getNumeroSalle() + " réservée. Email envoyé.");
                showReservations();
            } catch (Exception ex) {
                ex.printStackTrace();
                afficherAlerte("Erreur", "Impossible d'enregistrer la réservation : " + ex.getMessage());
            }
        });

        Button btnAnn = new Button("ANNULER");
        btnAnn.setMaxWidth(Double.MAX_VALUE);
        btnAnn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + GRIS_TEXTE + "; -fx-cursor: hand;");
        btnAnn.setOnAction(ev -> stage.close());

        root.getChildren().addAll(titre, new Separator(), lCours, lEmail, lCap, lBat, lhLabel, txtHeure, btnOk, btnAnn);
        stage.setScene(new Scene(root, 480, 420));
        stage.show();
    }

    private void rafraichirListeSalles(boolean avecBouton, String filtre) {
        if (containerSalles == null) return;
        containerSalles.getChildren().clear();
        try {
            List<Salle> liste = salleDAO.findAll();
            if (liste == null) return;
            if (!filtre.isBlank()) {
                String f = filtre.toLowerCase();
                liste = liste.stream().filter(s ->
                    s.getNumeroSalle().toLowerCase().contains(f) ||
                    (s.getBatiment() != null && s.getBatiment().getNomBatiment().toLowerCase().contains(f))
                ).collect(Collectors.toList());
            }
            for (Salle s : liste)
                containerSalles.getChildren().add(creerCarteSalle(s, avecBouton));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private VBox creerCarteSalle(Salle salle, boolean avecBouton) {
        VBox card = new VBox(12); card.setPadding(new Insets(20)); card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 14, 0, 0, 0);");

        Label lNum = new Label(salle.getNumeroSalle());
        lNum.setStyle("-fx-font-weight: bold; -fx-font-size: 22; -fx-text-fill: " + BLEU_DEEP + ";");

        Label lBat = new Label("📍 " + (salle.getBatiment() != null ? salle.getBatiment().getNomBatiment() : "N/A"));
        lBat.setStyle("-fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-font-size: 12;");

        Label lCap = new Label("👥 " + salle.getCapacite() + " places");
        lCap.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 12;");

        String etat = salle.getEtatSalle() != null ? salle.getEtatSalle() : "Inconnu";
        Label lEtat = new Label(etat.toUpperCase());
        lEtat.setAlignment(Pos.CENTER); lEtat.setMaxWidth(Double.MAX_VALUE);
        String etatColor = etat.equalsIgnoreCase("Disponible") ? VERT_LIME : "#FF3131";
        lEtat.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + etatColor + "; " +
                       "-fx-padding: 7 0; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11;");

        card.getChildren().addAll(lNum, lBat, lCap, lEtat);

        if (avecBouton) {
            Button btn = new Button(etat.equalsIgnoreCase("Disponible") ? "RESERVER" : "LIBERER");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; " +
                         "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8;");
            btn.setOnAction(e -> {
                if (etat.equalsIgnoreCase("Disponible")) showReservations();
                else { salle.setEtatSalle("Disponible"); salleDAO.update(salle); showSalles(); }
            });
            card.getChildren().add(btn);
        }
        return card;
    }

    private VBox construireAlerteCapacite(int effectifDemande, int maxCap, Button btnScan) {
        VBox box = new VBox(12); box.setPadding(new Insets(25)); box.setMaxWidth(500);
        box.setStyle("-fx-background-color: rgba(231,76,60,0.08); -fx-border-color: #e74c3c; " +
                      "-fx-border-radius: 12; -fx-background-radius: 12;");
        Label t = new Label("⚠️ CONFLIT DE CAPACITE DETECTE");
        t.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 15;");
        Label d = new Label("Effectif demandé : " + effectifDemande + " — Capacité maximale du réseau : " + maxCap + ".");
        d.setStyle("-fx-text-fill: " + BLEU_DEEP + ";");
        Button conseil = new Button("VOIR LES PLUS GRANDES UNITES");
        conseil.setStyle("-fx-background-color: transparent; -fx-text-fill: " + VERT_LIME + "; " +
                         "-fx-border-color: " + VERT_LIME + "; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-radius: 6;");
        if (txtEffectif != null && btnScan != null)
            conseil.setOnAction(e -> { txtEffectif.setText(String.valueOf(maxCap)); btnScan.fire(); });
        box.getChildren().addAll(t, d, conseil);
        return box;
    }

    private void preparerContenu(String style) {
        mainContent.getChildren().clear();
        mainContent.setOpacity(1.0);
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle(style);
        mainContent.setSpacing(18);
    }

    private HBox construireTopBar(String titrePaneau, String labelLogout, double maxAccent) {
        HBox bar = new HBox(); bar.setAlignment(Pos.CENTER_LEFT);

        if (titrePaneau != null) {
            VBox h = new VBox(6);
            Label t = new Label(titrePaneau);
            t.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
            Region acc = new Region(); acc.setPrefHeight(4);
            if (maxAccent > 0) acc.setMaxWidth(maxAccent);
            acc.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 2;");
            h.getChildren().addAll(t, acc);
            bar.getChildren().add(h);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        if (labelLogout != null && !labelLogout.isEmpty()) {
            Button btnLogoutTop = new Button(labelLogout);
            btnLogoutTop.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                               "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12;");
            btnLogoutTop.setOnAction(e -> showLoginSelection());
            bar.getChildren().addAll(sp, btnLogoutTop);
        }
        return bar;
    }

    private VBox construireHeader(String titre, String sous) {
        VBox h = new VBox(6);
        Label t = new Label(titre);
        t.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
        Region acc = new Region(); acc.setPrefSize(110, 4); acc.setMaxWidth(110);
        acc.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 2;");
        Label s = new Label(sous); s.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 13;");
        h.getChildren().addAll(t, acc, s);
        return h;
    }

    private HBox construireStepper(String... etapes) {
        HBox box = new HBox(30); box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 25, 14, 25));
        box.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 12;");
        for (int i = 0; i < etapes.length; i++) {
            Label l = new Label(etapes[i]);
            l.setStyle(i == 0
                ? "-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-family: 'Consolas';"
                : "-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-family: 'Consolas';");
            box.getChildren().add(l);
            if (i < etapes.length - 1) {
                Label fl = new Label(" ➔ ");
                fl.setStyle("-fx-text-fill: " + GRIS_TEXTE + ";");
                box.getChildren().add(fl);
            }
        }
        return box;
    }

    private VBox construirePlaceholder(String titre, String detail, double height) {
        VBox box = new VBox(8); box.setAlignment(Pos.CENTER); box.setPrefHeight(height);
        box.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; " +
                     "-fx-border-radius: 10; -fx-background-radius: 10;");
        Label t = new Label(titre); t.setStyle("-fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
        Label d = new Label(detail); d.setStyle("-fx-text-fill: " + GRIS_TEXTE + "; -fx-font-size: 12;");
        box.getChildren().addAll(t, d);
        return box;
    }

    private ScrollPane creerScrollPane(javafx.scene.Node contenu, double prefHeight) {
        ScrollPane sp = new ScrollPane(contenu);
        sp.setFitToWidth(true); sp.setPrefHeight(prefHeight);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        return sp;
    }

    private Button btnRetour(Runnable action) {
        Button b = new Button("← RETOUR");
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + GRIS_TEXTE + "; " +
                   "-fx-font-weight: bold; -fx-cursor: hand;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private Label creerTitre(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
        return l;
    }

    private VBox creerWidgetStat(String titre, String valeur) {
        VBox w = new VBox(6); w.setAlignment(Pos.CENTER); w.setPrefSize(210, 110);
        w.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 22;");
        Label lt = new Label(titre); lt.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 11;");
        Label lv = new Label(valeur); lv.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 38; -fx-font-weight: bold;");
        w.getChildren().addAll(lt, lv);
        return w;
    }

    public VBox creerCarteAction(String titre, String desc, EventHandler<MouseEvent> action) {
        VBox card = new VBox(14); card.setPadding(new Insets(26)); card.setPrefSize(240, 170);
        card.setAlignment(Pos.TOP_LEFT); card.setCursor(Cursor.HAND);
        String base = "-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 18; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);";
        card.setStyle(base);
        Label lt = new Label(titre); lt.setWrapText(true);
        lt.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: " + VERT_LIME + ";");
        Label ld = new Label(desc); ld.setWrapText(true);
        ld.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        card.getChildren().addAll(lt, ld);
        card.setOnMouseClicked(action);
        card.setOnMouseEntered(e -> { card.setTranslateY(-6);
            card.setStyle(base + "-fx-border-color: " + VERT_LIME + "; -fx-border-width: 2; -fx-border-radius: 18;"); });
        card.setOnMouseExited(e -> { card.setTranslateY(0); card.setStyle(base); });
        return card;
    }

    private VBox creerGroupeSaisie(String label, javafx.scene.control.Control input, double width) {
        VBox g = new VBox(5);
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        input.setPrefWidth(width);
        input.setStyle("-fx-background-color: #1a252f; -fx-text-fill: white; -fx-border-color: #34495e; " +
                       "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");
        g.getChildren().addAll(l, input);
        return g;
    }

    private VBox creerNodeTHM(String titre, String icon, String desc, EventHandler<MouseEvent> action) {
        VBox container = new VBox(16);
        container.setAlignment(Pos.CENTER_LEFT); container.setCursor(Cursor.HAND);
        container.setOnMouseClicked(action);

        StackPane panel = new StackPane(); panel.setPrefSize(215, 125);

        Region base = new Region(); base.setPrefSize(195, 42);
        base.setStyle("-fx-background-color: #060a0f; -fx-background-radius: 10 10 22 22; -fx-translate-y: 14;");

        Region body = new Region(); body.setPrefSize(195, 96);
        body.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 13; " +
                      "-fx-border-color: #1a252f; -fx-border-width: 3; -fx-border-radius: 13;");

        Label lIcon = new Label(icon);
        lIcon.setStyle("-fx-font-size: 30; -fx-background-color: rgba(0,0,0,0.35); " +
                        "-fx-padding: 7; -fx-background-radius: 9;");
        lIcon.setTranslateY(-42);

        Label lTitre = new Label(titre.toUpperCase());
        lTitre.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 9.5; " +
                        "-fx-text-fill: " + VERT_LIME + "; -fx-letter-spacing: 1;");
        lTitre.setTranslateY(18);

        panel.getChildren().addAll(base, body, lTitre, lIcon);

        Label lDesc = new Label(desc); lDesc.setWrapText(true); lDesc.setPrefWidth(195);
        lDesc.setStyle("-fx-font-size: 11; -fx-text-fill: " + GRIS_TEXTE + "; -fx-font-style: italic;");

        HBox row = new HBox(22, panel, lDesc); row.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(row);

        container.setOnMouseEntered(e -> {
            body.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 13; " +
                          "-fx-border-color: " + VERT_LIME + "; -fx-border-width: 3; -fx-border-radius: 13;");
            lTitre.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 9.5; " +
                            "-fx-text-fill: white; -fx-letter-spacing: 1;");
            container.setTranslateY(-4); container.setScaleX(1.015); container.setScaleY(1.015);
        });
        container.setOnMouseExited(e -> {
            body.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 13; " +
                          "-fx-border-color: #1a252f; -fx-border-width: 3; -fx-border-radius: 13;");
            lTitre.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 9.5; " +
                            "-fx-text-fill: " + VERT_LIME + "; -fx-letter-spacing: 1;");
            container.setTranslateY(0); container.setScaleX(1.0); container.setScaleY(1.0);
        });
        return container;
    }

    private Dialog<ButtonType> creerDialog(String titreHeader) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("UNIV-SCHEDULER — " + titreHeader);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setStyle("-fx-background-color: " + BLEU_DEEP + ";");

        Button btnOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        if (btnOk != null) styliserBoutonPrimaire(btnOk);
        return dlg;
    }

    private VBox getDialogForm(Dialog<?> dlg) {
        VBox form = new VBox(13);
        form.setPadding(new Insets(22));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 12;");
        dlg.getDialogPane().setContent(form);
        return form;
    }

    private TextField creerChampCyber(String placeholder) {
        TextField tf = new TextField(placeholder);
        appliquerStyleFieldCyber(tf);
        return tf;
    }

    private <T> ComboBox<T> creerComboCyber(T... items) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setMaxWidth(Double.MAX_VALUE);
        appliquerStyleFieldCyber(cb);
        return cb;
    }

    private void appliquerStyleFieldCyber(javafx.scene.control.Control c) {
        c.setStyle("-fx-background-color: #1a252f; -fx-text-fill: " + VERT_LIME + "; " +
                   "-fx-border-color: " + VERT_LIME + "33; -fx-border-radius: 7; " +
                   "-fx-background-radius: 7; -fx-font-weight: bold; -fx-padding: 9;");
    }

    private Label creerLabel(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-text-fill: " + VERT_LIME + "99; -fx-font-size: 10; -fx-font-family: 'Consolas';");
        return l;
    }

    private void styliserBoutonPrimaire(Button b) {
        String s = "-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; " +
                   "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
        String h = "-fx-background-color: white; -fx-text-fill: " + BLEU_DEEP + "; " +
                   "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e  -> b.setStyle(s));
    }

    private void styliserBoutonSecondaire(Button b) {
        b.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; " +
                   "-fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
    }

    private void afficherAlerte(String titre, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}