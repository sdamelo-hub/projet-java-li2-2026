package ProjetUniv_scheduler;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

    private SalleDAO salleDAO = new SalleDAO();
    private NotificationDAO notificationDAO = new NotificationDAO(HibernateUtil.getSessionFactory());
    private NotificationService notificationService = new NotificationService(notificationDAO);

    @FXML
    public void initialize() {
        showDashboard(); 
    }

    // --- 1. TABLEAU DE BORD ---
    @FXML
    private void showDashboard() {
        mainContent.getChildren().clear();
        mainContent.setStyle("-fx-background-color: " + FOND_BLANC + ";");

        Label title = new Label("État des Salles en Temps Réel");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        HBox statsBox = new HBox(25);
        statsBox.setPadding(new Insets(20, 0, 30, 0));
        
        List<Salle> salles = salleDAO.findAll();
        long countLibres = salles.stream()
                .filter(s -> s.getEtatSalle() != null && s.getEtatSalle().equalsIgnoreCase("Disponible"))
                .count();
        long countOccupees = salles.size() - countLibres;

        statsBox.getChildren().addAll(
            creerWidgetStat("Total Salles", String.valueOf(salles.size())),
            creerWidgetStat("Salles Libres", String.valueOf(countLibres)),
            creerWidgetStat("Salles Occupées", String.valueOf(countOccupees))
        );

        containerSalles = new FlowPane(20, 20);
        containerSalles.setStyle("-fx-background-color: transparent;");
        refreshSallesList(false, ""); 

        mainContent.getChildren().addAll(title, statsBox, new Separator(), containerSalles);
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
    @FXML 
    private void showReservations() {
        mainContent.getChildren().clear();
        Label title = new Label("📅 Assistant de Réservation Intuitif");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        VBox searchPanel = new VBox(15);
        searchPanel.setPadding(new Insets(20));
        searchPanel.setStyle("-fx-background-color: " + BLEU_DEEP + "; -fx-background-radius: 15;");

        Label instructions = new Label("1. Détails du cours et Email du Professeur");
        instructions.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-weight: bold;");

        // Première ligne : Classe et Effectif
        HBox row1 = new HBox(15);
        row1.setAlignment(Pos.CENTER_LEFT);
        TextField txtClasse = new TextField(); txtClasse.setPromptText("Classe (ex: L2 INFO)");
        TextField txtEffectif = new TextField(); txtEffectif.setPromptText("Effectif (ex: 30)");
        row1.getChildren().addAll(txtClasse, txtEffectif);

        // Deuxième ligne : Email et Bouton
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER_LEFT);
        TextField txtEmail = new TextField(); 
        txtEmail.setPromptText("Email du professeur (pour confirmation)");
        txtEmail.setPrefWidth(315);

        Button btnFilter = new Button("TROUVER UNE SALLE");
        btnFilter.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-cursor: hand;");

        row2.getChildren().addAll(txtEmail, btnFilter);
        searchPanel.getChildren().addAll(instructions, row1, row2);

        FlowPane resultsBox = new FlowPane(20, 20);
        resultsBox.setPadding(new Insets(20, 0, 0, 0));

        btnFilter.setOnAction(e -> {
            try {
                int effectifReq = Integer.parseInt(txtEffectif.getText());
                String classeNom = txtClasse.getText();
                String emailProf = txtEmail.getText();

                if (classeNom.isEmpty() || emailProf.isEmpty()) { 
                    afficherAlerte("Champs vides", "Précisez la classe et votre email."); return; 
                }

                resultsBox.getChildren().clear();
                
                // --- LOGIQUE DE TRI : On cherche le plus proche de l'effectif demandé en premier ---
                List<Salle> filtrage = salleDAO.findAll().stream()
                    .filter(s -> s.getCapacite() >= effectifReq && s.getEtatSalle().equalsIgnoreCase("Disponible"))
                    .sorted(Comparator.comparingInt(Salle::getCapacite)) // Tri ASC (30, puis 35, puis 40...)
                    .collect(Collectors.toList());

                if (filtrage.isEmpty()) {
                    resultsBox.getChildren().add(new Label("Aucune salle trouvée pour cet effectif."));
                } else {
                    for (Salle s : filtrage) { 
                        resultsBox.getChildren().add(creerCarteReservationSpecifique(s, classeNom, emailProf)); 
                    }
                }
            } catch (Exception ex) { afficherAlerte("Erreur", "Vérifiez que l'effectif est un nombre."); }
        });

        mainContent.getChildren().addAll(title, searchPanel, resultsBox);
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
}