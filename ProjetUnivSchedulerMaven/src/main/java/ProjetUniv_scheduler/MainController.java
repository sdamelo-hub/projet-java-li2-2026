package ProjetUniv_scheduler;

import javafx.event.EventHandler;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor; 
import javafx.scene.shape.Line; 
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle; 
import javafx.scene.shape.Line;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MainController {

    @FXML private VBox mainContent;
    @FXML private FlowPane containerSalles;
    

    // --- CHARTE GRAPHIQUE CYBER TECH ---
    private final String BLEU_DEEP = "#1E2732";   
    private final String VERT_LIME = "#A3FF33";   
    private final String FOND_BLANC = "#FFFFFF";
 // --- Champs de l'Assistant (Déclarés ici pour être vus par tout le code) ---
    private TextField txtClasse;
    private TextField txtEffectif;
    private TextField txtEmail; // Déjà fait normalement
    private DatePicker datePicker;
    private SalleDAO salleDAO = new SalleDAO();
    private NotificationDAO notificationDAO = new NotificationDAO(HibernateUtil.getSessionFactory());
    private NotificationService notificationService = new NotificationService(notificationDAO);
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private EquipementDAO equipementDAO = new EquipementDAO();
    private BatimentDAO batimentDAO = new BatimentDAO(HibernateUtil.getSessionFactory());
    @FXML
    public void initialize() {
        showDashboard(); 
    }

    // --- 1. TABLEAU DE BORD ---
    
    @FXML
    private void showDashboard() {
        mainContent.getChildren().clear();
        // On garde le fond général propre, mais on va styliser les composants
        mainContent.setStyle("-fx-background-color: " + FOND_BLANC + ";");

        Label title = new Label("SYSTÈME DE SURVEILLANCE - ÉTAT DES SALLES");
        title.setStyle("-fx-font-size: 26; -fx-font-family: 'Consolas', 'Monospace'; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-letter-spacing: 2;");

        // --- 1. CALCUL DES DONNÉES ---
        List<Salle> salles = salleDAO.findAll();
        long countLibres = salles.stream()
                .filter(s -> s.getEtatSalle() != null && s.getEtatSalle().equalsIgnoreCase("Disponible"))
                .count();
        long countOccupees = salles.size() - countLibres;

        // --- 2. WIDGETS DE STATS ---
        HBox statsBox = new HBox(25);
        statsBox.setPadding(new Insets(20, 0, 30, 0));
        statsBox.getChildren().addAll(
            creerWidgetStat("CAPACITÉ RÉSEAU", String.valueOf(salles.size())),
            creerWidgetStat("UNITÉS LIBRES", String.valueOf(countLibres)),
            creerWidgetStat("CHARGE ACTUELLE", String.valueOf(countOccupees))
        );

        // --- 3. LE GRAPHIQUE CYBER (NEON PIE CHART) ---
        PieChart pcOccupation = new PieChart();
        pcOccupation.setTitle("ANALYSE DE RÉPARTITION");
        pcOccupation.setLegendVisible(true);
        pcOccupation.setLabelsVisible(false);
        pcOccupation.setPrefSize(450, 400);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Libres", countLibres),
            new PieChart.Data("Occupées", countOccupees)
        );
        pcOccupation.setData(pieData);

        // Conteneur sombre pour le graphique (Essentiel pour le look Cyber)
        VBox chartContainer = new VBox(pcOccupation);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPadding(new Insets(20));
        chartContainer.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 25; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 10);");

        // Application du style après rendu (Platform.runLater pour s'assurer que les nodes existent)
        javafx.application.Platform.runLater(() -> {
            // Style du titre du graphique
            pcOccupation.lookup(".chart-title").setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-family: 'Consolas'; -fx-font-size: 18;");
            
            // Style de la légende
            pcOccupation.lookup(".chart-legend").setStyle("-fx-background-color: transparent;");
            for (javafx.scene.Node node : pcOccupation.lookupAll(".chart-legend-item")) {
                if (node instanceof Label) ((Label) node).setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }

            // --- EFFET NÉON SUR LES TRANCHES ---
            for (PieChart.Data data : pcOccupation.getData()) {
                javafx.scene.Node node = data.getNode();
                String color = data.getName().equals("Libres") ? VERT_LIME : "#FF3131"; // Vert vs Rouge Néon

                // Style de la tranche : Couleur semi-transparente avec bordure vive
                node.setStyle("-fx-pie-color: " + color + "44; -fx-border-color: " + color + "; -fx-border-width: 3;");

                // Effet de luminescence (Glow)
                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                glow.setColor(javafx.scene.paint.Color.web(color));
                glow.setRadius(25);
                glow.setSpread(0.4);
                node.setEffect(glow);
                
                // Animation au survol
                node.setOnMouseEntered(e -> { node.setScaleX(1.05); node.setScaleY(1.05); });
                node.setOnMouseExited(e -> { node.setScaleX(1.0); node.setScaleY(1.0); });
            }
        });

        // --- 4. ASSEMBLAGE FINAL ---
        containerSalles = new FlowPane(20, 20);
        containerSalles.setStyle("-fx-background-color: transparent;");
        refreshSallesList(false, ""); 

        mainContent.getChildren().addAll(title, statsBox, new Separator(), chartContainer, new Separator(), containerSalles);
    }

    // --- 2. GESTION DES SALLES ---
    @FXML
    private void showSalles() {
        mainContent.getChildren().clear();
        Label title = new Label("Gestion des Salles & Réservations");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher une salle ou un bâtiment...");
        searchField.setPrefWidth(500);
        searchField.setStyle("-fx-padding: 12; -fx-background-radius: 25; -fx-border-color: " + BLEU_DEEP + ";");
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshSallesList(true, newVal));

        containerSalles = new FlowPane(20, 20);
        refreshSallesList(true, ""); 
        
        mainContent.getChildren().addAll(title, searchField, containerSalles);
    }

    // --- 3. RÉSERVATIONS (Assistant Intelligent mis à jour) ---
    

    private VBox creerCarteSmartSalle(Salle salle, double score, String nomClasse, String emailProf) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // --- HEADER : Nom de la salle + Badge Match ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblNom = new Label(salle.getNumeroSalle());
        lblNom.setStyle("-fx-font-weight: bold; -fx-font-size: 22; -fx-text-fill: " + BLEU_DEEP + ";");
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label badge = new Label((int)score + "% MATCH");
        badge.setPadding(new Insets(5, 10, 5, 10));
        String badgeColor = score > 90 ? VERT_LIME : "#f1c40f"; 
        badge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-background-radius: 10; -fx-font-size: 10;");
        header.getChildren().addAll(lblNom, spacer, badge);

        // --- INFOS CAPACITÉ ---
        VBox capBox = new VBox(5);
        Label lblCap = new Label("Capacité : " + salle.getCapacite() + " places");
        lblCap.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        
        ProgressBar pbCap = new ProgressBar();
        pbCap.setPrefWidth(Double.MAX_VALUE);
        // On calcule le ratio réel par rapport à l'effectif demandé
        try {
            double effectif = Double.parseDouble(txtEffectif.getText());
            pbCap.setProgress(effectif / salle.getCapacite());
        } catch(Exception ex) { pbCap.setProgress(0.5); }
        
        pbCap.setStyle("-fx-accent: " + (score > 90 ? VERT_LIME : BLEU_DEEP) + ";");
        capBox.getChildren().addAll(lblCap, pbCap);

        // --- ÉQUIPEMENTS ---
        HBox equipIcons = new HBox(10);
        equipIcons.setAlignment(Pos.CENTER_LEFT);
        // On peut imaginer une logique ici pour vérifier les vrais équipements de la salle
        equipIcons.getChildren().addAll(creerMiniIcone("📽️", true), creerMiniIcone("❄️", true), creerMiniIcone("🌐", false));

        // --- BOUTON DE RÉSERVATION (Le moteur) ---
        Button btnConfirm = new Button("SÉLECTIONNER");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setCursor(javafx.scene.Cursor.HAND);
        btnConfirm.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        
        // ACTION DU BOUTON
        btnConfirm.setOnAction(e -> {
            // Log de débug dans ta console Eclipse
            System.out.println(">>> Tentative de réservation pour la salle : " + salle.getNumeroSalle());

            // SÉCURITÉ : On vérifie si l'email a été saisi avant de lancer la suite
            if (emailProf == null || emailProf.trim().isEmpty()) {
                afficherAlerte("ACTION REQUISE", "Veuillez saisir l'email du responsable dans le formulaire en haut avant de sélectionner une unité.");
                // On met le focus sur le champ email pour aider l'utilisateur
                txtEmail.requestFocus(); 
            } else {
                // APPEL DE LA MÉTHODE DE FINALISATION (La fenêtre Cyber)
                finaliserReservation(salle, nomClasse, emailProf);
            }
        });

        card.getChildren().addAll(header, new Separator(), capBox, equipIcons, btnConfirm);

        // --- EFFETS INTERACTIFS ---
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-10);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, " + VERT_LIME + ", 20, 0, 0, 0);");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        });

        return card;
    }
    
    private void finaliserReservation(Salle salle, String nomClasse, String emailProf) {
        try {
            System.out.println("--- DÉBUT FINALISATION ---");
            
            // 1. Mise à jour visuelle (on sécurise l'appel)
            try {
                updateStepperHorizontal(2);
            } catch (Exception e) {
                System.out.println("⚠️ Note: Le stepper n'a pas pu être mis à jour, mais on continue...");
            }

            // 2. Création de la fenêtre
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setTitle("UIDT-AI : Validation");

            VBox root = new VBox(20);
            root.setPadding(new Insets(25));
            root.setStyle("-fx-background-color: " + (BLEU_DEEP != null ? BLEU_DEEP : "#1a252f") + 
                         "; -fx-border-color: " + (VERT_LIME != null ? VERT_LIME : "#00ff00") + 
                         "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

            Label header = new Label("🏁 VALIDATION DE LA RÉSERVATION");
            header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 18;");

            VBox infoBox = new VBox(8);
            String styleInfo = "-fx-text-fill: white; -fx-font-family: 'Consolas'; -fx-font-size: 13;";
            infoBox.getChildren().addAll(
                new Label("UNITÉ : " + salle.getNumeroSalle()),
                new Label("COURS : " + nomClasse),
                new Label("DESTINATAIRE : " + emailProf)
            );
            infoBox.getChildren().forEach(n -> n.setStyle(styleInfo));

            TextField txtHeure = new TextField("08:00 - 10:00");
            txtHeure.setStyle("-fx-background-color: #121a21; -fx-text-fill: white; -fx-border-color: #34495e;");

            Button btnOk = new Button("CONFIRMER ET ENVOYER L'EMAIL");
            btnOk.setMaxWidth(Double.MAX_VALUE);
            btnOk.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold;");
            
            updateStepperHorizontal(2);
            btnOk.setOnAction(ev -> {
                try {
                    salle.setEtatSalle("Occupée");
                    salleDAO.update(salle);
                    
                    if (notificationService != null) {
                        notificationService.confirmerReservation(emailProf, "Cours [" + nomClasse + "] validé.");
                    }

                    afficherAlerte("SYSTÈME IA", "Réservation confirmée.");
                    dialog.close();
                    showReservations();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    afficherAlerte("ERREUR", "Échec lors de l'enregistrement.");
                }
            });

            root.getChildren().addAll(header, new Separator(), infoBox, new Label("CRÉNEAU :"), txtHeure, btnOk);
            
            dialog.setScene(new Scene(root, 500, 400));
            System.out.println("--- AFFICHAGE DU DIALOGUE ---");
            dialog.show();

        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE dans finaliserReservation :");
            e.printStackTrace(); // Ceci va te dire EXACTEMENT quelle ligne pose problème
        }
    }
    
    private void updateStepperHorizontal(int indexActive) {
        // On cherche le conteneur du stepper (le HBox footerStepper)
        // S'il est dans un VBox mainLayout, on y accède ainsi :
        VBox mainLayout = (VBox) mainContent.getChildren().get(0);
        HBox footer = (HBox) mainLayout.getChildren().get(2); // Index 2 si c'est le 3ème élément

        int labelIndex = 0;
        for (javafx.scene.Node n : footer.getChildren()) {
            if (n instanceof Label) {
                Label l = (Label) n;
                // On ignore les flèches "➔"
                if (!l.getText().contains("➔")) {
                    if (labelIndex == indexActive) {
                        // Étape active : Vert Lime + Gras
                        l.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
                    } else {
                        // Étape inactive : Gris
                        l.setStyle("-fx-text-fill: #5e6d7a; -fx-font-family: 'Consolas';");
                    }
                    labelIndex++;
                }
            }
        }
    }
    
    
    private void updateStepper(int etapeIndex) {
        // On suppose que sidebar est le VBox contenant tes labels d'étapes
        // et que stepBox est le conteneur des labels (VBox interne)
        VBox sidebar = (VBox) ((BorderPane)mainContent.getChildren().get(0)).getLeft();
        VBox stepBox = (VBox) sidebar.getChildren().get(2);
        
        for (int i = 0; i < stepBox.getChildren().size(); i++) {
            Label l = (Label) stepBox.getChildren().get(i);
            if (i == etapeIndex) {
                l.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
            } else {
                l.setStyle("-fx-text-fill: #5e6d7a;"); // Gris pour les étapes inactives
            }
        }
    }

    // Petite fonction utilitaire pour les icônes d'équipement
    private Label creerMiniIcone(String icone, boolean disponible) {
        Label l = new Label(icone);
        l.setOpacity(disponible ? 1.0 : 0.2); // Grisé si pas dispo
        l.setStyle("-fx-font-size: 16;");
        return l;
    }
    // --- FONCTIONS UTILITAIRES POUR LE DESIGN ---

    private Label creerIndicateurEtape(String texte, boolean active) {
        Label l = new Label(texte);
        l.setStyle(active ? "-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;" : "-fx-text-fill: #5e6d7a;");
        return l;
    }

    private ToggleButton creerToggleButtonCyber(String texte) {
        ToggleButton tb = new ToggleButton(texte);
        tb.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 15;");
        tb.selectedProperty().addListener((obs, oldV, newV) -> {
            tb.setStyle(newV ? "-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold;" : "-fx-background-color: #2c3e50; -fx-text-fill: white;");
        });
        return tb;
    }

    @FXML 
    private void showReservations() {
        mainContent.getChildren().clear();
        mainContent.setPadding(new Insets(20));
        
        // Conteneur principal vertical
        VBox mainLayout = new VBox(20); 
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // --- 1. ENTÊTE ---
        Label mainTitle = new Label("SENSORS & BOOKING HUB");
        mainTitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        // --- 2. CONSOLE DE RECHERCHE (Le bloc de saisie) ---
        VBox searchConsole = new VBox(20);
        searchConsole.setPadding(new Insets(25));
        searchConsole.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 20; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 10);");

        // Ligne 1 : Paramètres de base
        HBox row1 = new HBox(25);
        row1.setAlignment(Pos.BOTTOM_LEFT);
        txtClasse = new TextField(); 
        txtEffectif = new TextField();
        datePicker = new DatePicker(java.time.LocalDate.now());

        row1.getChildren().addAll(
            creerGroupeSaisie("📚 CLASSE / COURS", txtClasse, 250),
            creerGroupeSaisie("👥 EFFECTIF", txtEffectif, 100),
            creerGroupeSaisie("📅 DATE DE SESSION", datePicker, 180)
        );

        // Ligne 2 : Email et Action
        HBox row2 = new HBox(25);
        row2.setAlignment(Pos.BOTTOM_LEFT);
        txtEmail = new TextField();
        Button btnScan = new Button("DÉMARRER L'ANALYSE PRÉDICTIVE");
        btnScan.setPrefHeight(45);
        btnScan.setPadding(new Insets(0, 30, 0, 30));
        btnScan.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");

        // Style Hover interactif
        btnScan.setOnMouseEntered(e -> btnScan.setStyle("-fx-background-color: white; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-background-radius: 10;"));
        btnScan.setOnMouseExited(e -> btnScan.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-background-radius: 10;"));

        row2.getChildren().addAll(creerGroupeSaisie("📧 EMAIL DU RESPONSABLE", txtEmail, 400), btnScan);
        searchConsole.getChildren().addAll(row1, new Separator(), row2);

        // --- 3. BARRE DE PROGRESSION (Placée ICI entre saisie et résultats) ---
        HBox statusStepper = new HBox(50);
        statusStepper.setAlignment(Pos.CENTER);
        statusStepper.setPadding(new Insets(15, 30, 15, 30));
        statusStepper.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label step1 = creerIndicateurEtapeHorizontal("1. CONFIGURATION", true);
        Label step2 = creerIndicateurEtapeHorizontal("2. ANALYSE IA", false);
        Label step3 = creerIndicateurEtapeHorizontal("3. RÉSERVATION", false);
        statusStepper.getChildren().addAll(step1, new Label(" ➔ "), step2, new Label(" ➔ "), step3);

        // --- 4. ZONE DE RÉSULTATS (Tout en bas) ---
        FlowPane resultsContainer = new FlowPane(25, 25);
        resultsContainer.setPadding(new Insets(10, 0, 10, 0));
        ScrollPane scroll = new ScrollPane(resultsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS); // Occupe tout l'espace restant

        // --- LOGIQUE DU SCAN ---
        btnScan.setOnAction(e -> {
            resultsContainer.getChildren().clear();
            try {
                int reqSize = Integer.parseInt(txtEffectif.getText().trim());
                
                // On active visuellement l'étape 2
                step1.setStyle("-fx-text-fill: #5e6d7a; -fx-font-family: 'Consolas';");
                step2.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
                
                updateStepperHorizontal(1);
                List<Salle> options = salleDAO.findAll().stream()
                    .filter(s -> s.getCapacite() >= reqSize && s.getEtatSalle().equalsIgnoreCase("Disponible"))
                    .sorted(Comparator.comparingInt(Salle::getCapacite))
                    .limit(12)
                    .collect(Collectors.toList());

                if (options.isEmpty()) {
                    // --- LOOK CYBER POUR L'ALERTE ---
                    VBox alertBox = new VBox(10);
                    alertBox.setAlignment(Pos.CENTER);
                    alertBox.setPadding(new Insets(30));
                    alertBox.setStyle("-fx-background-color: rgba(231, 76, 60, 0.1); -fx-border-color: #e74c3c; -fx-border-radius: 15; -fx-background-radius: 15;");

                    Label errorTitle = new Label("⚠️ CONFLIT DE CAPACITÉ DÉTECTÉ");
                    errorTitle.setStyle("-fx-text-fill: #e74c3c; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 16;");

                    // On cherche la plus grande salle pour informer l'utilisateur
                    int maxCap = salleDAO.findAll().stream().mapToInt(Salle::getCapacite).max().orElse(0);
                    
                    Label errorDesc = new Label("L'effectif demandé (100) dépasse la capacité maximale du réseau (" + maxCap + ").");
                    errorDesc.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas';");

                    Button btnAdvice = new Button("VOIR LES PLUS GRANDES UNITÉS DISPONIBLES");
                    btnAdvice.setStyle("-fx-background-color: transparent; -fx-text-fill: " + VERT_LIME + "; -fx-border-color: " + VERT_LIME + "; -fx-cursor: hand;");
                    
                    // Action : Relancer le scan avec la taille max au lieu de 100
                    btnAdvice.setOnAction(ev -> {
                        txtEffectif.setText(String.valueOf(maxCap));
                        btnScan.fire(); // On relance le clic automatiquement !
                    });

                    alertBox.getChildren().addAll(errorTitle, errorDesc, btnAdvice);
                    resultsContainer.getChildren().add(alertBox);
                } else {
                    for (Salle s : options) {
                        double score = 100 - (s.getCapacite() - reqSize);
                        resultsContainer.getChildren().add(creerCarteSmartSalle(s, score, txtClasse.getText(), txtEmail.getText()));
                    }
                }
            } catch (Exception ex) {
                afficherAlerte("Erreur Analyse", "L'effectif doit être un nombre valide.");
            }
        });

        // ASSEMBLAGE DANS L'ORDRE LOGIQUE
        mainLayout.getChildren().addAll(mainTitle, searchConsole, statusStepper, scroll);
        mainContent.getChildren().add(mainLayout);
    }

    /**
     * Helper pour les indicateurs d'étapes horizontaux
     */
    private Label creerIndicateurEtapeHorizontal(String texte, boolean active) {
        Label l = new Label(texte);
        l.setStyle(active ? "-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-family: 'Consolas';" 
                          : "-fx-text-fill: #5e6d7a; -fx-font-family: 'Consolas';");
        return l;
    }

    /**
     * Helper pour créer des groupes de saisie compacts avec étiquettes Consolas.
     */
    private VBox creerGroupeSaisie(String label, Control input, double width) {
        VBox group = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        input.setPrefWidth(width);
        input.setStyle("-fx-background-color: #1a252f; -fx-text-fill: white; -fx-border-color: #34495e; -fx-border-radius: 5; -fx-background-radius: 5;");
        group.getChildren().addAll(lbl, input);
        return group;
    }
    
    // --- CARTE DE RÉSERVATION (Prend l'email en paramètre) ---
    private VBox creerCarteReservationSpecifique(Salle salle, String nomClasse, String emailProf) {
        VBox card = creerCarteSalleDynamique(salle, false);
        
        Button btnReserve = new Button("CONFIRMER POUR " + nomClasse);
        btnReserve.setMaxWidth(Double.MAX_VALUE);
        btnReserve.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-cursor: hand;");
        
        btnReserve.setOnAction(e -> {
            // Dialogue pour les horaires (Dernière étape)
            Dialog<String[]> timeDialog = new Dialog<>();
            timeDialog.setTitle("Horaires");
            timeDialog.setHeaderText("Salle " + salle.getNumeroSalle() + " | Prof: " + emailProf);
            ButtonType btnType = new ButtonType("Finaliser", ButtonBar.ButtonData.OK_DONE);
            timeDialog.getDialogPane().getButtonTypes().addAll(btnType, ButtonType.CANCEL);

            GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
            TextField hDebut = new TextField("08:00"); TextField hFin = new TextField("10:00");
            grid.add(new Label("Début:"), 0, 0); grid.add(hDebut, 1, 0);
            grid.add(new Label("Fin:"), 0, 1); grid.add(hFin, 1, 1);
            timeDialog.getDialogPane().setContent(grid);

            timeDialog.setResultConverter(b -> b == btnType ? new String[]{hDebut.getText(), hFin.getText()} : null);

            timeDialog.showAndWait().ifPresent(times -> {
                salle.setEtatSalle("Occupée");
                salleDAO.update(salle);
                // Utilisation de l'email saisi dans le formulaire
                notificationService.confirmerReservation(emailProf, 
                    "Cours " + nomClasse + " | Salle " + salle.getNumeroSalle() + " | " + times[0] + "-" + times[1]);
                
                afficherAlerte("Succès", "Mail envoyé à " + emailProf);
                showReservations();
            });
        });
        card.getChildren().add(btnReserve);
        return card;
    }

    // --- 4. NOTIFICATIONS (Réparé) ---
    @FXML 
    private void showNotifications() {
        mainContent.getChildren().clear();
        Label title = new Label("🔔 Historique des Notifications");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-padding: 0 0 20 0;");

        TableView<Notification> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(500);
        table.setStyle("-fx-selection-bar: " + BLEU_DEEP + ";");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM 'à' HH:mm", Locale.FRENCH);

        TableColumn<Notification, LocalDateTime> colDate = new TableColumn<>("Date / Heure");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else { setText(item.format(formatter)); setStyle("-fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold;"); }
            }
        });

        TableColumn<Notification, String> colMsg = new TableColumn<>("Message envoyé");
        colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));

        table.getColumns().addAll(colDate, colMsg);
        table.getItems().addAll(notificationDAO.findAll());
        mainContent.getChildren().addAll(title, table);
    }

    // --- LOGIQUE COMMUNE ---

    private void refreshSallesList(boolean interactionActive, String filtre) {
        containerSalles.getChildren().clear();
        try {
            List<Salle> liste = salleDAO.findAll();
            if (!filtre.isEmpty()) {
                String f = filtre.toLowerCase();
                liste = liste.stream()
                    .filter(s -> s.getNumeroSalle().toLowerCase().contains(f) || 
                                (s.getBatiment() != null && s.getBatiment().getNomBatiment().toLowerCase().contains(f)))
                    .collect(Collectors.toList());
            }
            for (Salle s : liste) { containerSalles.getChildren().add(creerCarteSalleDynamique(s, interactionActive)); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox creerCarteSalleDynamique(Salle salle, boolean avecBouton) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 0);");
        Label lNom = new Label(salle.getNumeroSalle());
        lNom.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: " + BLEU_DEEP + ";");
        Label lBat = new Label("📍 " + (salle.getBatiment() != null ? salle.getBatiment().getNomBatiment() : "N/A"));
        lBat.setStyle("-fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold;");
        Label lInfo = new Label("👥 Capacité: " + salle.getCapacite());
        String etat = salle.getEtatSalle();
        Label lStatut = new Label(etat.toUpperCase());
        lStatut.setAlignment(Pos.CENTER); lStatut.setMaxWidth(Double.MAX_VALUE);
        lStatut.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-padding: 8 0; -fx-background-radius: 10; -fx-font-weight: bold;");
        card.getChildren().addAll(lNom, lBat, lInfo, lStatut);
        if (avecBouton) {
            Button actionBtn = new Button(etat.equalsIgnoreCase("Disponible") ? "RÉSERVER" : "LIBÉRER");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 10;");
            actionBtn.setOnAction(e -> gererClicSalle(salle));
            card.getChildren().add(actionBtn);
        }
        return card;
    }

    private VBox creerWidgetStat(String titre, String valeur) {
        VBox widget = new VBox(5);
        widget.setAlignment(Pos.CENTER); widget.setPrefSize(210, 120);
        widget.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 25; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 0);");
        Label lblTitre = new Label(titre); lblTitre.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
        Label lblVal = new Label(valeur); lblVal.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 40; -fx-font-weight: bold;");
        widget.getChildren().addAll(lblTitre, lblVal);
        return widget;
    }

    private void gererClicSalle(Salle salle) {
        if (salle.getEtatSalle().equalsIgnoreCase("Occupée")) {
            salle.setEtatSalle("Disponible"); salleDAO.update(salle); showSalles();
        } else { showReservations(); }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
    
 // --- 5. MODULE ADMINISTRATEUR (Gestion Globale & IA) ---

    @FXML
    private void showAdminPanel() {
        mainContent.getChildren().clear();
        // Fond blanc pur pour un contraste maximal avec le Vert Lime
        mainContent.setStyle("-fx-background-color: #FFFFFF;"); 
        mainContent.setPadding(new Insets(30));

        // --- 1. HEADER (Style Terminal High-Tech) ---
        VBox header = new VBox(8);
        Label title = new Label("INTERFACE ADMINISTRATEUR");
        title.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Petite barre de progression décorative (Vert Lime)
        Region accentBar = new Region();
        accentBar.setPrefHeight(4);
        accentBar.setMaxWidth(150);
        accentBar.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 2;");
        header.getChildren().addAll(title, accentBar);

        // --- 2. ZONE DE CHEMIN (Style TryHackMe - Circuit Actif) ---
        Pane pathContainer = new Pane();
        pathContainer.setPrefHeight(650); 

        // Création des modules (Nodes)
        VBox node1 = creerNodeTHM("UTILISATEURS", "👤", "Accès et Privilèges", e -> showUserManagement());
        node1.setLayoutX(430); node1.setLayoutY(20);

        VBox node2 = creerNodeTHM("INFRASTRUCTURE", "🏢", "Salles & Bâtiments", e -> showSallesManagement());
        node2.setLayoutX(80); node2.setLayoutY(210);

        VBox node3 = creerNodeTHM("INVENTAIRE", "⚙️", "Maintenance IoT", e -> showInventoryManagement());
        node3.setLayoutX(430); node3.setLayoutY(400);

        // Lignes de connexion (Câblage en VERT LIME LUMINEUX)
        // On remplace le gris par ton vert lime avec un effet de lueur
        Line line1 = new Line(520, 120, 200, 230); 
        Line line2 = new Line(200, 340, 520, 420); 
        
        String limeLineStyle = "-fx-stroke: " + VERT_LIME + "; " +
                               "-fx-stroke-width: 4; " +
                               "-fx-stroke-dash-array: 15; " +
                               "-fx-opacity: 0.6; " + // Un peu de transparence pour le style
                               "-fx-effect: dropshadow(three-pass-box, " + VERT_LIME + ", 10, 0, 0, 0);";
        
        line1.setStyle(limeLineStyle); 
        line2.setStyle(limeLineStyle);

        pathContainer.getChildren().addAll(line1, line2, node1, node2, node3);

        // --- 3. SECTION IA (Néon sur Bleu Deep) ---
        VBox aiSection = new VBox(20);
        aiSection.setPadding(new Insets(35, 30, 35, 30));
        
        // On ajoute une bordure VERT LIME au bloc pour le faire ressortir sur le blanc
        aiSection.setStyle("-fx-background-color: " + BLEU_DEEP + "; " +
                           "-fx-background-radius: 30; " +
                           "-fx-border-color: " + VERT_LIME + "; " +
                           "-fx-border-width: 1.5; " +
                           "-fx-border-radius: 30; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,255,157,0.2), 25, 0, 0, 10);");
        
        Label aiTitle = new Label("🤖 OPTIMISATION PAR IA");
        aiTitle.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-family: 'Consolas'; -fx-font-size: 22; -fx-font-weight: bold; -fx-letter-spacing: 2;");

        Separator aiSep = new Separator();
        aiSep.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-opacity: 0.4;");

        Label aiDesc = new Label("Algorithmes neuronaux activés : Analyse de charge et résolution de conflits.");
        aiDesc.setWrapText(true);
        aiDesc.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-style: italic; -fx-font-size: 15; -fx-opacity: 0.9;");

        // Bouton Vert Lime Plein (Imposant)
        Button btnRunAI = new Button("LANCER LE DIAGNOSTIC SYSTÈME");
        btnRunAI.setMaxWidth(Double.MAX_VALUE);
        btnRunAI.setCursor(javafx.scene.Cursor.HAND);
        btnRunAI.setStyle("-fx-background-color: " + VERT_LIME + "; " +
                          "-fx-text-fill: " + BLEU_DEEP + "; " + // Texte bleu foncé sur fond lime = lisibilité parfaite
                          "-fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 15 25;");

        btnRunAI.setOnAction(e -> {
            btnRunAI.setDisable(true);
            btnRunAI.setText("⚡ INITIALISATION DU SCAN...");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(ev -> {
                lancerDiagnosticIA();
                btnRunAI.setDisable(false);
                btnRunAI.setText("LANCER LE DIAGNOSTIC SYSTÈME");
            });
            pause.play();
        });

        aiSection.getChildren().addAll(aiTitle, aiSep, aiDesc, btnRunAI);

        // ScrollPane invisible pour laisser le blanc respirer
        ScrollPane scroll = new ScrollPane(pathContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(450);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-viewport-background: transparent;");

        mainContent.getChildren().addAll(header, scroll, aiSection);
    }
    
    private VBox creerNodeTHM(String titre, String icon, String desc, EventHandler<MouseEvent> action) {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setCursor(Cursor.HAND);
        container.setOnMouseClicked(action);

        // --- LE PANNEAU DE CONTRÔLE ISOMÉTRIQUE ---
        StackPane isometricPanel = new StackPane();
        isometricPanel.setPrefSize(220, 130);

        // 1. La Base (Profondeur)
        Region isoBase = new Region();
        isoBase.setPrefSize(200, 45);
        isoBase.setStyle("-fx-background-color: #080c12; -fx-background-radius: 10 10 25 25; -fx-translate-y: 15;");

        // 2. Le Corps Principal (Style Cadre Word)
        Region mainBody = new Region();
        mainBody.setPrefSize(200, 100);
        mainBody.setStyle("-fx-background-color: " + BLEU_DEEP + "; " +
                         "-fx-background-radius: 15; " +
                         "-fx-border-color: #1a252f; " +
                         "-fx-border-width: 4; " +
                         "-fx-border-radius: 15; " +
                         "-fx-border-insets: 2;");

        // 3. Le "Data Stack" (Panneau de monitoring)
        StackPane dataStack = new StackPane();
        dataStack.setPrefSize(180, 70);
        dataStack.setMaxSize(180, 70);
        Region dataSurface = new Region();
        dataSurface.setStyle("-fx-background-color: rgba(163, 255, 51, 0.05); -fx-background-radius: 10;");
        
        // Motifs de données (Lignes de scan)
        HBox patterns = new HBox(5);
        patterns.setPadding(new Insets(15));
        patterns.setAlignment(Pos.CENTER_LEFT);
        Line scanLine = new Line(0, 0, 90, 0); 
        scanLine.setStyle("-fx-stroke: " + VERT_LIME + "; -fx-stroke-width: 0.8; -fx-opacity: 0.3;");
        
        HBox activeFlow = new HBox(4);
        Line a1 = new Line(0, 0, 12, 0); a1.setStyle("-fx-stroke: " + VERT_LIME + "; -fx-stroke-width: 2; -fx-opacity: 0;");
        Line a2 = new Line(0, 0, 8, 0);  a2.setStyle("-fx-stroke: " + VERT_LIME + "; -fx-stroke-width: 2; -fx-opacity: 0;");
        activeFlow.getChildren().addAll(a1, a2);
        
        patterns.getChildren().addAll(scanLine, activeFlow);
        dataStack.getChildren().addAll(dataSurface, patterns);
        dataStack.setTranslateY(-8);

        // 4. L'Icône (Floating Cyber-Badge) - Positionnée en haut
        Label lIcon = new Label(icon);
        lIcon.setStyle("-fx-font-size: 32; -fx-background-color: rgba(0,0,0,0.4); -fx-padding: 8; -fx-background-radius: 10;");
        lIcon.setTranslateY(-45); 

        // --- 5. LE TITRE INTERNE (Écrit en Vert Lime, juste en bas du logo) ---
        Label lTitreInterne = new Label(titre.toUpperCase());
        lTitreInterne.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 10; " +
                              "-fx-text-fill: " + VERT_LIME + "; -fx-letter-spacing: 1.2;");
        lTitreInterne.setTranslateY(18); // Placé sur la partie basse du panneau bleu

        isometricPanel.getChildren().addAll(isoBase, mainBody, dataStack, lTitreInterne, lIcon);

        // --- TEXTES EXTERNES (Seulement la description) ---
        VBox textInfo = new VBox(3);
        Label lDesc = new Label(desc);
        lDesc.setWrapText(true);
        lDesc.setPrefWidth(200);
        lDesc.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b; -fx-font-style: italic;");
        textInfo.getChildren().add(lDesc);

        HBox globalBox = new HBox(25);
        globalBox.setAlignment(Pos.CENTER_LEFT);
        globalBox.getChildren().addAll(isometricPanel, textInfo);
        container.getChildren().add(globalBox);

        // --- EFFET HOVER : MISE SOUS TENSION ---
        container.setOnMouseEntered(e -> {
            String glowStyle = "-fx-stroke: " + VERT_LIME + "; -fx-stroke-width: 2; -fx-opacity: 1; -fx-effect: dropshadow(three-pass-box, " + VERT_LIME + ", 10, 0, 0, 0);";
            a1.setStyle(glowStyle); a2.setStyle(glowStyle);
            
            // Le titre s'illumine légèrement au survol
            lTitreInterne.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 10; " +
                                  "-fx-text-fill: white; -fx-letter-spacing: 1.2; " +
                                  "-fx-effect: dropshadow(three-pass-box, " + VERT_LIME + ", 8, 0, 0, 0);");

            mainBody.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15; " +
                             "-fx-border-color: " + VERT_LIME + "; -fx-border-width: 4; -fx-border-radius: 15; -fx-border-insets: 2;");
            
            container.setTranslateY(-5);
            container.setScaleX(1.02); container.setScaleY(1.02);
        });

        container.setOnMouseExited(e -> {
            a1.setOpacity(0); a2.setOpacity(0);
            
            lTitreInterne.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 10; " +
                                  "-fx-text-fill: " + VERT_LIME + "; -fx-letter-spacing: 1.2;");

            mainBody.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15; " +
                             "-fx-border-color: #1a252f; -fx-border-width: 4; -fx-border-radius: 15; -fx-border-insets: 2;");
            
            container.setTranslateY(0);
            container.setScaleX(1.0); container.setScaleY(1.0);
        });

        return container;
    } 
    
    
    // Fonction utilitaire pour créer des cartes d'action Admin
    	private VBox creerCarteAdmin(String titre, String icon, String description, EventHandler<MouseEvent> action) {
    	    VBox card = new VBox(15);
    	    card.setPadding(new Insets(25));
    	    card.setPrefSize(260, 220);
    	    card.setAlignment(Pos.TOP_LEFT);
    	    
    	    // Style CYBER
    	    String styleBase = "-fx-background-color: " + BLEU_DEEP + "; " +
    	                       "-fx-background-radius: 25; " +
    	                       "-fx-cursor: hand; " +
    	                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);";
    	    
    	    card.setStyle(styleBase);

    	    // --- ICI ON LIE L'ACTION DU CLIC ---
    	    card.setOnMouseClicked(action);

    	    // --- ICON CONTAINER ---
    	    StackPane iconContainer = new StackPane();
    	    iconContainer.setPrefSize(50, 50);
    	    iconContainer.setMaxSize(50, 50);
    	    iconContainer.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 12;");
    	    
    	    Label lIcon = new Label(icon);
    	    lIcon.setStyle("-fx-font-size: 24; -fx-text-fill: " + VERT_LIME + ";");
    	    iconContainer.getChildren().add(lIcon);

    	    // --- TEXTES ---
    	    Label lTitre = new Label(titre.toUpperCase());
    	    lTitre.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: " + VERT_LIME + ";");

    	    Label lDesc = new Label(description);
    	    lDesc.setWrapText(true);
    	    lDesc.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 12; -fx-opacity: 0.7;");

    	    card.getChildren().addAll(iconContainer, lTitre, lDesc);

    	    // Animations (Survol)
    	    card.setOnMouseEntered(e -> {
    	        card.setTranslateY(-10);
    	        card.setStyle(styleBase + "-fx-border-color: " + VERT_LIME + "; -fx-border-width: 2; -fx-border-radius: 25;");
    	    });
    	    card.setOnMouseExited(e -> {
    	        card.setTranslateY(0);
    	        card.setStyle(styleBase);
    	    });

    	    return card;
    	}
    
 // --- MOTEUR DE DIAGNOSTIC IA ---

    	private void lancerDiagnosticIA() {
    	    List<Salle> salles = salleDAO.findAll();
    	    List<Equipement> equipements = equipementDAO.findAll();
    	    StringBuilder rapport = new StringBuilder();
    	    
    	    int alertesCritiques = 0;
    	    int optimisations = 0;

    	    rapport.append("--- [ SESSION D'ANALYSE IA : ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(" ] ---\n\n");
    	    rapport.append("> SCAN DU RÉSEAU DES LOCAUX...\n");

    	    // 1. Analyse des pannes matérielles (Impact sur les cours)
    	    for (Equipement e : equipements) {
    	        if ("En panne".equals(e.getEtatFonctionnement()) || "En maintenance".equals(e.getEtatFonctionnement())) {
    	            alertesCritiques++;
    	            rapport.append("⚠️ [ALERTE MATÉRIEL] ").append(e.getNomEquipement())
    	                   .append(" (ID: ").append(e.getIdEquipement()).append(") est indisponible ")
    	                   .append("en Salle ").append(e.getSalle() != null ? e.getSalle().getNumeroSalle() : "Stock").append(".\n");
    	        }
    	    }

    	    // 2. Analyse de la capacité des salles (Optimisation d'espace)
    	    for (Salle s : salles) {
    	        if (s.getCapacite() < 15) {
    	            optimisations++;
    	            rapport.append("ℹ️ [OPTIMISATION] Salle ").append(s.getNumeroSalle())
    	                   .append(" : Capacité très réduite. À réserver uniquement pour des travaux de groupe.\n");
    	        }
    	    }

    	    // 3. Synthèse finale
    	    rapport.append("\n------------------------------------------------\n");
    	    if (alertesCritiques > 0) {
    	        rapport.append("❌ RÉSULTAT : ").append(alertesCritiques).append(" CONFLITS OPÉRATIONNELS DÉTECTÉS.\n");
    	        rapport.append("👉 CONSEIL : Vérifiez l'inventaire avant les prochaines réservations.");
    	    } else {
    	        rapport.append("✅ RÉSULTAT : SYSTÈME OPTIMAL. Aucun conflit de ressources détecté.");
    	    }

    	    // Affichage du rapport dans ta console "Cyber"
    	    showIAReportDialog(rapport.toString());
    	}

    	private void showIAReportDialog(String message) {
    	    javafx.stage.Stage dialog = new javafx.stage.Stage();
    	    // Bloque l'interaction avec la fenêtre principale tant que le rapport est ouvert
    	    dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL); 
    	    dialog.setTitle("UIDT-AI : Terminal d'Analyse");

    	    VBox root = new VBox(20);
    	    root.setPadding(new Insets(25));
    	    // Fond bleu profond avec bordure néon verte
    	    root.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-border-color: " + VERT_LIME + "; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

    	    Label header = new Label("🤖 ANALYSE PRÉDICTIVE TERMINÉE");
    	    header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 18; -fx-font-weight: bold; -fx-letter-spacing: 2;");

    	    TextArea area = new TextArea(message);
    	    area.setEditable(false);
    	    area.setWrapText(true);
    	    
    	    // --- STYLE CONSOLE CYBER ---
    	    // On ajoute une bordure discrète autour du texte pour l'effet "Écran"
    	    area.setStyle("-fx-control-inner-background: #121a21; " +
    	                  "-fx-text-fill: " + VERT_LIME + "; " +
    	                  "-fx-font-family: 'Consolas', 'Monospace'; " +
    	                  "-fx-font-size: 13; " +
    	                  "-fx-border-color: " + VERT_LIME + "44; " + 
    	                  "-fx-border-radius: 5; " +
    	                  "-fx-background-radius: 5;");
    	    area.setPrefHeight(300);

    	    // Bouton de fermeture avec style interactif
    	    Button btnClose = new Button("RETOUR À LA CONSOLE");
    	    btnClose.setMaxWidth(Double.MAX_VALUE);
    	    btnClose.setCursor(javafx.scene.Cursor.HAND);
    	    
    	    String btnStyle = "-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-background-radius: 8;";
    	    String btnHover = "-fx-background-color: #FFFFFF; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-background-radius: 8;";
    	    
    	    btnClose.setStyle(btnStyle);
    	    btnClose.setOnMouseEntered(e -> btnClose.setStyle(btnHover));
    	    btnClose.setOnMouseExited(e -> btnClose.setStyle(btnStyle));
    	    
    	    btnClose.setOnAction(e -> dialog.close());

    	    root.getChildren().addAll(header, new Separator(), area, btnClose);
    	    
    	    javafx.scene.Scene scene = new Scene(root, 580, 480);
    	    dialog.setScene(scene);
    	    dialog.show();
    	}
    
    @FXML
    private void showUserManagement() {
        mainContent.getChildren().clear();
        
        Label title = new Label("👤 RÉPERTOIRE DES UTILISATEURS");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        // Barre d'outils (Recherche + Ajout)
        HBox toolBar = new HBox(15);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(20, 0, 20, 0));
        
        TextField searchUser = new TextField();
        searchUser.setPromptText("🔍 Rechercher un nom ou un email...");
        searchUser.setPrefWidth(350);
        searchUser.setStyle("-fx-background-radius: 15; -fx-padding: 8;");

        Button btnAddUser = new Button("+ NOUVEL UTILISATEUR");
        btnAddUser.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAddUser.setOnAction(e -> showAddUserDialog());

        toolBar.getChildren().addAll(searchUser, btnAddUser);

        TableView<Utilisateur> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);

        // --- COLONNES ---
        TableColumn<Utilisateur, String> colNom = new TableColumn<>("Nom Complet");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Utilisateur, String> colEmail = new TableColumn<>("Email Professionnel");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Utilisateur, String> colRole = new TableColumn<>("Rôle / Statut");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // --- COLONNE ACTIONS MISE À JOUR ---
        TableColumn<Utilisateur, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            // Correction Unicode pour l'icône de suppression
            private final Button btnDel = new Button("\uD83D\uDDD1"); 
            private final HBox pane = new HBox(15, btnEdit, btnDel);
            {
                pane.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16;");
                btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 16;");
                
                // LIEN AVEC LA MÉTHODE DE MODIFICATION
                btnEdit.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    showEditUserDialog(u); // On appelle le formulaire de mise à jour
                });
                
                btnDel.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    confirmerSuppressionUser(u);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colNom, colEmail, colRole, colActions);
        
        // Logique de chargement et recherche
        List<Utilisateur> allUsers = utilisateurDAO.findAll();
        table.getItems().addAll(allUsers);
        
        searchUser.textProperty().addListener((obs, oldVal, newVal) -> {
            table.getItems().clear();
            String filter = newVal.toLowerCase();
            table.getItems().addAll(allUsers.stream()
                .filter(u -> u.getNom().toLowerCase().contains(filter) || u.getEmail().toLowerCase().contains(filter))
                .collect(Collectors.toList()));
        });

        mainContent.getChildren().addAll(title, toolBar, table);
    }
    
    private void showAddUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Système - Nouvel Utilisateur");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("ENREGISTRER UN COLLABORATEUR");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 16;");

        // --- CRÉATION DES CHAMPS ---
        TextField txtNom = new TextField(); 
        txtNom.setPromptText("Nom Complet");
        
        TextField txtEmail = new TextField(); 
        txtEmail.setPromptText("Email (ex: prof@univ.sn)");
        
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Administrateur", "Gestionnaire", "Enseignant", "Étudiant");
        cbRole.setValue("Enseignant");
        cbRole.setMaxWidth(Double.MAX_VALUE);

        // --- CONFIGURATION DU STYLE (VERT VIF / NEON) ---
        // 'aa' à la fin de la couleur HEX permet une opacité vive pour le texte d'aide
        String fieldStyle = "-fx-background-color: #2c3e50; " +
                             "-fx-text-fill: " + VERT_LIME + "; " + 
                             "-fx-prompt-text-fill: " + VERT_LIME + "aa; " + 
                             "-fx-border-color: " + VERT_LIME + "44; " + 
                             "-fx-background-radius: 5; " +
                             "-fx-border-radius: 5; " +
                             "-fx-font-weight: bold; " +
                             "-fx-padding: 8;";

        txtNom.setStyle(fieldStyle);
        txtEmail.setStyle(fieldStyle);
        cbRole.setStyle(fieldStyle);

        // Config de l'affichage de la sélection (le texte visible quand la liste est fermée)
        cbRole.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item);
                    setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
                }
            }
        });

        // Config de l'affichage des éléments dans la liste déroulante
        cbRole.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: #2c3e50;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
                    this.setOnMouseEntered(e -> setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + ";"));
                    this.setOnMouseExited(e -> setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + ";"));
                }
            }
        });

        form.getChildren().addAll(header, new Separator(), txtNom, txtEmail, cbRole);
        dialog.getDialogPane().setContent(form);

        // --- LOGIQUE DE SAUVEGARDE HIBERNATE ---
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (txtNom.getText().isEmpty() || txtEmail.getText().isEmpty()) {
                    afficherAlerte("Erreur", "Tous les champs sont obligatoires.");
                    return;
                }
                
                // Création de l'objet (Maintenant possible car Utilisateur n'est plus abstract)
                Utilisateur u = new Utilisateur();
                u.setNom(txtNom.getText());
                u.setEmail(txtEmail.getText());
                u.setRole(cbRole.getValue());
                // On utilise l'email comme identifiant par défaut pour la connexion
                u.setIdentifiantConnexion(txtEmail.getText()); 
                
                try {
                    utilisateurDAO.save(u); // Persistance via le DAO corrigé
                    showUserManagement();    // Rafraîchir le tableau immédiatement
                    afficherAlerte("Succès", "L'utilisateur " + u.getNom() + " a été intégré au système.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    afficherAlerte("Erreur DB", "Échec de l'enregistrement dans la base MySQL.");
                }
            }
        });
    }
    
    private void showEditUserDialog(Utilisateur u) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Mise à jour - " + u.getNom());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("MODIFIER LE PROFIL UTILISATEUR");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 16;");

        // Champs pré-remplis
        TextField txtNom = new TextField(u.getNom());
        TextField txtEmail = new TextField(u.getEmail());
        
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Administrateur", "Gestionnaire", "Enseignant", "Étudiant");
        cbRole.setValue(u.getRole());
        cbRole.setMaxWidth(Double.MAX_VALUE);

        // Style Cyber
        String style = "-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-background-radius: 5;";
        txtNom.setStyle(style); txtEmail.setStyle(style); cbRole.setStyle(style);

        form.getChildren().addAll(header, new Label("Nom :"), txtNom, new Label("Email :"), txtEmail, new Label("Rôle :"), cbRole);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                u.setNom(txtNom.getText());
                u.setEmail(txtEmail.getText());
                u.setRole(cbRole.getValue());
                
                try {
                    utilisateurDAO.update(u); // Mise à jour via Hibernate
                    showUserManagement();      // Rafraîchir le tableau
                    afficherAlerte("Succès", "Profil de " + u.getNom() + " mis à jour.");
                } catch (Exception ex) {
                    afficherAlerte("Erreur", "Impossible de modifier l'utilisateur.");
                }
            }
        });
    }
    
    private void confirmerSuppressionUser(Utilisateur u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer l'utilisateur : " + u.getNom());
        alert.setContentText("Cette action est irréversible. Continuer ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                utilisateurDAO.delete(u);
                showUserManagement();
            }
        });
    }
    
 // --- GESTION DE L'INVENTAIRE MATÉRIEL ---
    @FXML
    private void showInventoryManagement() {
        mainContent.getChildren().clear();
        
        Label title = new Label("⚙️ INVENTAIRE DU MATÉRIEL");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        HBox toolBar = new HBox(15);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(20, 0, 20, 0));

        Button btnAddEquip = new Button("+ NOUVEL ÉQUIPEMENT");
        btnAddEquip.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAddEquip.setOnAction(e -> showAddEquipementDialog());

        toolBar.getChildren().add(btnAddEquip);

        TableView<Equipement> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);

        TableColumn<Equipement, String> colNom = new TableColumn<>("Équipement");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomEquipement"));

        TableColumn<Equipement, String> colEtat = new TableColumn<>("État");
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatFonctionnement"));

        TableColumn<Equipement, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(cellData -> {
            Salle s = cellData.getValue().getSalle();
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.getNumeroSalle() : "Stock");
        });

        // --- NOUVELLE COLONNE ACTIONS ---
        TableColumn<Equipement, Void> colActions = new TableColumn<>("COMMANDES");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("📝");
            private final Button btnDel = new Button("\uD83D\uDDD1"); 
            private final HBox pane = new HBox(15, btnEdit, btnDel);
            {
                pane.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16;");
                btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 16;");
                
                btnEdit.setOnAction(e -> showEditEquipementDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> confirmerSuppressionEquipement(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colNom, colEtat, colSalle, colActions);
        table.getItems().addAll(equipementDAO.findAll());

        mainContent.getChildren().addAll(title, toolBar, table);
    }
    

    
 // --- GESTION DES SALLES (ADMIN) ---
    private void showSallesManagement() {
        mainContent.getChildren().clear();
        
        // --- 1. EN-TÊTE AVEC SÉLECTEUR DE VUE ---
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("🏢 RÉSEAU DES LOCAUX");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        // ComboBox pour basculer entre les Salles et les Bâtiments
        ComboBox<String> viewSelector = new ComboBox<>();
        viewSelector.getItems().addAll("Liste des Salles", "Liste des Bâtiments");
        viewSelector.setValue("Liste des Salles"); 
        viewSelector.setStyle("-fx-background-color: white; -fx-border-color: " + BLEU_DEEP + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-font-weight: bold;");
        
        // Action lors du changement de vue
        viewSelector.setOnAction(e -> {
            if (viewSelector.getValue().equals("Liste des Bâtiments")) {
                showBatimentsManagement(); // Assure-toi que cette méthode est bien dans ce fichier
            } else {
                showSallesManagement();
            }
        });

        headerBox.getChildren().addAll(title, viewSelector);

        // --- 2. BARRE D'OUTILS (Boutons d'ajout) ---
        HBox toolBar = new HBox(15);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(0, 0, 20, 0));

        Button btnAddSalle = new Button("+ NOUVELLE SALLE");
        btnAddSalle.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAddSalle.setOnAction(e -> showAddSalleDialog());

        Button btnAddBat = new Button("+ NOUVEAU BÂTIMENT");
        btnAddBat.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
        btnAddBat.setOnAction(e -> showAddBatimentDialog());

        toolBar.getChildren().addAll(btnAddSalle, btnAddBat);

        // --- 3. TABLEAU DES SALLES ---
        TableView<Salle> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(450);
        table.setStyle("-fx-selection-bar: " + VERT_LIME + "; -fx-selection-bar-non-focused: #d1ff9a;");

        TableColumn<Salle, String> colNum = new TableColumn<>("N° Salle");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numeroSalle"));

        TableColumn<Salle, String> colBat = new TableColumn<>("Bâtiment");
        colBat.setCellValueFactory(cellData -> {
            Batiment b = cellData.getValue().getBatiment();
            return new javafx.beans.property.SimpleStringProperty(b != null ? b.getNomBatiment() : "Non assigné");
        });

        TableColumn<Salle, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacite"));

        // Colonne Commandes avec correction de l'icône de suppression
        TableColumn<Salle, Void> colActions = new TableColumn<>("COMMANDES");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("📝");
            // Utilisation du code Unicode \uD83D\uDDD1 pour éviter le carré rouge de la poubelle
            private final Button btnDel = new Button("\uD83D\uDDD1"); 
            private final HBox pane = new HBox(15, btnEdit, btnDel);
            {
                pane.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16;");
                // Couleur rouge pour le bouton supprimer
                btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 16;");
                
                btnEdit.setOnAction(e -> showEditSalleDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> confirmerSuppression(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        table.getColumns().addAll(colNum, colBat, colCap, colActions);
        
        // Chargement des données via Hibernate [cite: 37]
        table.getItems().addAll(salleDAO.findAll());

        // --- 4. AFFICHAGE FINAL ---
        mainContent.getChildren().addAll(headerBox, toolBar, table);
    }
    
    private void showAddEquipementDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Système - Nouvel Équipement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("ENREGISTRER UN MATÉRIEL");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 16;");

        // --- CHAMPS ---
        TextField txtId = new TextField(); 
        txtId.setPromptText("Code Inventaire (ex: VP-01)");
        
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Vidéoprojecteur", "Tableau Interactif", "Climatisation", "Ordinateur");
        cbType.setPromptText("Type de matériel");
        cbType.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.getItems().addAll(salleDAO.findAll());
        cbSalle.setPromptText("Assigner à une salle");
        cbSalle.setMaxWidth(Double.MAX_VALUE);
        
        // --- STYLE CYBER VIF ---
        String fieldStyle = "-fx-background-color: #2c3e50; " +
                            "-fx-text-fill: " + VERT_LIME + "; " +
                            "-fx-prompt-text-fill: " + VERT_LIME + "aa; " +
                            "-fx-border-color: " + VERT_LIME + "44; " +
                            "-fx-background-radius: 5; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8;";

        txtId.setStyle(fieldStyle);
        cbType.setStyle(fieldStyle);
        cbSalle.setStyle(fieldStyle);

        // --- CONFIGURATION DES LISTES (COULEUR VERT) ---
        
        // 1. Pour le type de matériel (String)
        cbType.setButtonCell(createStringCell());
        cbType.setCellFactory(lv -> createStringCell());

        // 2. Pour la salle (Objet Salle)
        cbSalle.setConverter(new javafx.util.StringConverter<Salle>() {
            @Override public String toString(Salle s) { return s == null ? "" : s.getNumeroSalle(); }
            @Override public Salle fromString(String s) { return null; }
        });
        cbSalle.setButtonCell(creerSalleCellCyber());
        cbSalle.setCellFactory(lv -> creerSalleCellCyber());

        form.getChildren().addAll(header, new Separator(), txtId, cbType, cbSalle);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (txtId.getText().isEmpty() || cbType.getValue() == null) {
                    afficherAlerte("Champs manquants", "Le code et le type sont obligatoires.");
                    return;
                }
                
                Equipement e = new Equipement();
                e.setIdEquipement(txtId.getText());
                e.setNomEquipement(cbType.getValue());
                e.setEtatFonctionnement("Opérationnel");
                e.setSalle(cbSalle.getValue());
                
                try {
                    equipementDAO.save(e);
                    showInventoryManagement(); // Rafraîchir le tableau
                    afficherAlerte("Succès", "L'équipement " + e.getNomEquipement() + " a été ajouté.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    afficherAlerte("Erreur", "Impossible d'enregistrer dans la base.");
                }
            }
        });
    }

    private void showEditEquipementDialog(Equipement e) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Maintenance - " + e.getNomEquipement());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("MODIFIER L'ÉTAT DU MATÉRIEL");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");

        ComboBox<String> cbEtat = new ComboBox<>();
        cbEtat.getItems().addAll("Opérationnel", "En panne", "En maintenance");
        cbEtat.setValue(e.getEtatFonctionnement());
        cbEtat.setMaxWidth(Double.MAX_VALUE);
        cbEtat.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");

        form.getChildren().addAll(header, new Label("État actuel :"), cbEtat);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                e.setEtatFonctionnement(cbEtat.getValue());
                equipementDAO.update(e); // Mise à jour Hibernate
                showInventoryManagement(); // Rafraîchir
            }
        });
    }
    
    private void confirmerSuppressionEquipement(Equipement e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ALERTE INVENTAIRE");
        alert.setHeaderText("Supprimer l'équipement : " + e.getIdEquipement());
        alert.setContentText("Voulez-vous retirer cet objet de l'inventaire ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                equipementDAO.delete(e);
                showInventoryManagement();
            }
        });
    }
    // --- FONCTIONS UTILITAIRES POUR LE STYLE DES LISTES ---

    private ListCell<String> createStringCell() {
        return new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #2c3e50;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
                    this.setOnMouseEntered(e -> setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + ";"));
                    this.setOnMouseExited(e -> setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + ";"));
                }
            }
        };
    }

    private ListCell<Salle> creerSalleCellCyber() {
        return new ListCell<Salle>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setStyle("-fx-background-color: #2c3e50;");
                } else {
                    setText("Salle " + s.getNumeroSalle());
                    setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
                    this.setOnMouseEntered(e -> setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + ";"));
                    this.setOnMouseExited(e -> setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + ";"));
                }
            }
        };
    }
    
    private void showAddBatimentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Configuration - Nouveau Bâtiment");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("ENREGISTRER UNE INFRASTRUCTURE");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 16;");

        // Champs de saisie basés sur ton entité Batiment 
        TextField txtCode = new TextField(); txtCode.setPromptText("Code (ex: BAT-A)");
        TextField txtNom = new TextField(); txtNom.setPromptText("Nom du Bâtiment");
        TextField txtLoc = new TextField(); txtLoc.setPromptText("Localisation (ex: Campus Nord)");
        TextField txtEtages = new TextField(); txtEtages.setPromptText("Nombre d'étages");

        String style = "-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-prompt-text-fill: " + VERT_LIME + "aa; -fx-font-weight: bold;";
        txtCode.setStyle(style); txtNom.setStyle(style); txtLoc.setStyle(style); txtEtages.setStyle(style);

        form.getChildren().addAll(header, new Separator(), txtCode, txtNom, txtLoc, txtEtages);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Batiment b = new Batiment(
                        txtCode.getText(), 
                        txtNom.getText(), 
                        txtLoc.getText(), 
                        Integer.parseInt(txtEtages.getText()), 
                        "Pédagogique"
                    );
                    batimentDAO.save(b); // Persistance Hibernate
                    afficherAlerte("Succès", "Le bâtiment " + b.getNomBatiment() + " est opérationnel.");
                } catch (Exception ex) {
                    afficherAlerte("Erreur", "Vérifiez les données saisies.");
                }
            }
        });
    }
    
    private void showBatimentsManagement() {
        mainContent.getChildren().clear();
        Label title = new Label("🏢 RÉPERTOIRE DES BÂTIMENTS");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        TableView<Batiment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Batiment, String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeBatiment"));

        TableColumn<Batiment, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomBatiment"));

        TableColumn<Batiment, String> colLoc = new TableColumn<>("Localisation");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("localisationBatiment"));

        table.getColumns().addAll(colCode, colNom, colLoc);
        table.getItems().addAll(batimentDAO.findAll());

        mainContent.getChildren().addAll(title, table);
    }
    private void showEditSalleDialog(Salle salle) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modification - " + salle.getNumeroSalle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("MODIFIER LE LOCAL");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 16;");

        TextField txtCap = new TextField(String.valueOf(salle.getCapacite()));
        
        // Ajout du sélecteur de bâtiment pour la modification
        ComboBox<Batiment> cbBat = new ComboBox<>();
        cbBat.getItems().addAll(batimentDAO.findAll());
        cbBat.setValue(salle.getBatiment());
        cbBat.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll("TD", "TP", "Amphithéâtre"); // Harmonisé avec showAddSalle 
        cbCat.setValue(salle.getCategorieSalle());
        cbCat.setMaxWidth(Double.MAX_VALUE);

        String style = "-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;";
        txtCap.setStyle(style); cbBat.setStyle(style); cbCat.setStyle(style);

        // Formattage du nom du bâtiment
        cbBat.setConverter(new javafx.util.StringConverter<Batiment>() {
            @Override public String toString(Batiment b) { return b == null ? "" : b.getNomBatiment(); }
            @Override public Batiment fromString(String s) { return null; }
        });

        form.getChildren().addAll(header, new Label("Capacité :"), txtCap, new Label("Bâtiment :"), cbBat, new Label("Type :"), cbCat);
        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    salle.setCapacite(Integer.parseInt(txtCap.getText()));
                    salle.setCategorieSalle(cbCat.getValue());
                    salle.setBatiment(cbBat.getValue()); // Mise à jour de la liaison
                    
                    salleDAO.update(salle); 
                    showSallesManagement(); 
                } catch (Exception ex) {
                    afficherAlerte("Erreur", "Données invalides.");
                }
            }
        });
    }
    
    private void confirmerSuppression(Salle salle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ALERTE SYSTÈME");
        alert.setHeaderText("Suppression de la salle " + salle.getNumeroSalle());
        alert.setContentText("Êtes-vous certain de vouloir déconnecter ce local du réseau ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                salleDAO.delete(salle); // Hibernate supprime la ligne
                showSallesManagement(); // Refresh du tableau
                System.out.println("LOG: Salle " + salle.getNumeroSalle() + " supprimée.");
            }
        });
    }

    // --- FORMULAIRE D'AJOUT (DIALOGUE CYBER) ---
    private void showAddSalleDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Système - Nouvel Enregistrement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label header = new Label("PARAMÉTRAGE NOUVELLE SALLE");
        header.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold; -fx-font-size: 16;");

        // --- CHAMPS DE SAISIE ---
        TextField txtNum = new TextField(); 
        txtNum.setPromptText("Numéro de salle (ex: A101)");
        
        TextField txtCap = new TextField(); 
        txtCap.setPromptText("Capacité maximale");
        
        // --- NOUVEAU : SÉLECTION DU BÂTIMENT ---
        ComboBox<Batiment> cbBat = new ComboBox<>();
        cbBat.setPromptText("Sélectionner un bâtiment");
        cbBat.setMaxWidth(Double.MAX_VALUE);
        // On charge les bâtiments depuis la base via le DAO 
        cbBat.getItems().addAll(batimentDAO.findAll());

        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll("TD", "TP", "Amphithéâtre");
        cbCat.setValue("TD");
        cbCat.setMaxWidth(Double.MAX_VALUE);

        // --- STYLE CYBER OPTIMISÉ ---
        String fieldStyle = "-fx-background-color: #2c3e50; " +
                            "-fx-text-fill: " + VERT_LIME + "; " +
                            "-fx-prompt-text-fill: " + VERT_LIME + "aa; " +
                            "-fx-background-radius: 5; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8;";

        txtNum.setStyle(fieldStyle);
        txtCap.setStyle(fieldStyle);
        cbBat.setStyle(fieldStyle);
        cbCat.setStyle(fieldStyle);

        // --- CONFIGURATION DE L'AFFICHAGE DU BÂTIMENT (NOM AU LIEU DE L'OBJET) ---
        cbBat.setConverter(new javafx.util.StringConverter<Batiment>() {
            @Override
            public String toString(Batiment b) { return (b == null) ? "" : b.getNomBatiment(); }
            @Override
            public Batiment fromString(String s) { return null; }
        });

        // --- CELL FACTORIES POUR LES COMBOBOX (LOOK VERT SUR BLEU) ---
        cbBat.setButtonCell(creerListCellCyber());
        cbBat.setCellFactory(lv -> creerListCellCyber());
        cbCat.setButtonCell(new ListCell<String>() { // Spécifique pour String
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
            }
        });

        form.getChildren().addAll(header, new Separator(), txtNum, txtCap, cbBat, cbCat);
        dialog.getDialogPane().setContent(form);

        // --- LOGIQUE DE SAUVEGARDE ---
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (txtNum.getText().isEmpty() || txtCap.getText().isEmpty() || cbBat.getValue() == null) {
                    afficherAlerte("Données manquantes", "Veuillez remplir tous les champs et choisir un bâtiment.");
                    return;
                }

                Salle s = new Salle();
                s.setNumeroSalle(txtNum.getText());
                s.setCapacite(Integer.parseInt(txtCap.getText()));
                s.setCategorieSalle(cbCat.getValue()); // Type TD/TP/Amphi [cite: 37]
                s.setBatiment(cbBat.getValue());      // Liaison avec l'infrastructure 
                s.setEtatSalle("Disponible");
                
                salleDAO.save(s); 
                showSallesManagement(); 
                
                afficherAlerte("Succès", "Salle " + s.getNumeroSalle() + " enregistrée dans le bâtiment " + s.getBatiment().getNomBatiment());
            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur", "La capacité doit être un nombre.");
            } catch (Exception ex) {
                ex.printStackTrace();
                afficherAlerte("Erreur Système", "Impossible de lier la salle au bâtiment.");
            }
        }
    }

    // Fonction utilitaire pour éviter la répétition du style des cellules
    private ListCell<Batiment> creerListCellCyber() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Batiment b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) {
                    setText(null);
                    setStyle("-fx-background-color: #2c3e50;");
                } else {
                    setText(b.getNomBatiment());
                    setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");
                    this.setOnMouseEntered(e -> setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + ";"));
                    this.setOnMouseExited(e -> setStyle("-fx-background-color: #2c3e50; -fx-text-fill: " + VERT_LIME + ";"));
                }
            }
        };
    }
    
    
    @FXML
    private void showManagerDashboard() {
        mainContent.getChildren().clear();
        mainContent.setStyle("-fx-background-color: #FFFFFF;");
        mainContent.setPadding(new Insets(30));

        // --- 1. HEADER OPÉRATIONNEL ---
        VBox header = new VBox(5);
        Label title = new Label("STATION DE GESTION OPÉRATIONNELLE");
        title.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label subTitle = new Label("● SURVEILLANCE DU CAMPUS EN TEMPS RÉEL");
        subTitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11; -fx-text-fill: " + VERT_LIME + "; -fx-background-color: #1e293b; -fx-padding: 2 8;");
        header.getChildren().addAll(title, subTitle);

        // --- 2. BARRE DE STATUTS RAPIDES (Widgets) ---
        HBox quickStats = new HBox(20);
        quickStats.setPadding(new Insets(20, 0, 20, 0));
        quickStats.getChildren().addAll(
            creerStatWidget("SALLES OCCUPÉES", "12 / 45", "🔴"),
            creerStatWidget("CONFLITS IA", "00", "🛡️"),
            creerStatWidget("TEMPÉRATURE MOY.", "22°C", "🌡️")
        );

        // --- 3. ZONE DE CONTRÔLE (Vue Grille des Salles) ---
        VBox controlZone = new VBox(15);
        Label sectionTitle = new Label("ÉTAT DES UNITÉS D'ENSEIGNEMENT");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

        ScrollPane scrollRooms = new ScrollPane();
        FlowPane roomGrid = new FlowPane(15, 15);
        roomGrid.setPadding(new Insets(10));
        
        // Simulation de quelques salles pour le test visuel
        for(int i=1; i<=6; i++) {
            roomGrid.getChildren().add(creerMiniRoomCard("Salle A0" + i, (i%2==0 ? "Libre" : "Occupée")));
        }

        scrollRooms.setContent(roomGrid);
        scrollRooms.setFitToWidth(true);
        scrollRooms.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        controlZone.getChildren().addAll(sectionTitle, scrollRooms);

        mainContent.getChildren().addAll(header, quickStats, controlZone);
    }

    /**
     * Petit widget de stat pour le haut de l'écran
     */
    private VBox creerStatWidget(String titre, String valeur, String icone) {
        VBox widget = new VBox(5);
        widget.setPadding(new Insets(15));
        widget.setPrefWidth(200);
        widget.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");
        
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10; -fx-font-weight: bold;");
        Label lblVal = new Label(icone + " " + valeur);
        lblVal.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        
        widget.getChildren().addAll(lblTitre, lblVal);
        return widget;
    }

    /**
     * Petite carte de salle compacte pour le gestionnaire
     */
    private VBox creerMiniRoomCard(String nom, String etat) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefSize(160, 100);
        
        String color = etat.equals("Libre") ? VERT_LIME : "#e74c3c";
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;");
        
        Label lblNom = new Label(nom);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");
        
        Label lblStatus = new Label(etat.toUpperCase());
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10; -fx-font-weight: bold;");
        
        card.getChildren().addAll(lblNom, new Separator(), lblStatus);
        return card;
    }
}