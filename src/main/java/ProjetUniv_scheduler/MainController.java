package ProjetUniv_scheduler;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private VBox mainContent;
    @FXML private FlowPane containerSalles;

    // ===== PALETTE COULEURS (cohérence avec le design existant) =====
    private final String BLEU_DEEP  = "#0D1B2A";
    private final String BLEU_MOYEN = "#1565C0";
    private final String BLEU_CLAIR = "#42A5F5";
    private final String FOND_BLANC = "#F0F4FF";

    private SalleDAO salleDAO = new SalleDAO();
    private NotificationDAO notificationDAO = new NotificationDAO(HibernateUtil.getSessionFactory());
    private NotificationService notificationService = new NotificationService(notificationDAO);
    private ReservationDAO reservationDAO = new ReservationDAO(HibernateUtil.getSessionFactory());

    @FXML
    public void initialize() {
        showDashboard();
    }

    // ===================== LOGIN =====================
    @FXML
    private void showLogin() {
        Dialog<String[]> loginDialog = new Dialog<>();
        loginDialog.setTitle("UNIV-SCHEDULER");

        DialogPane dialogPane = loginDialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #0D1B2A;" +
                        "-fx-background-radius: 15;"
        );

        Label titre = new Label("🔐  Accès Sécurisé");
        titre.setStyle("-fx-text-fill: #42A5F5; -fx-font-size: 20; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        Label sousTitre = new Label("UNIV-SCHEDULER — Gestion Intelligente");
        sousTitre.setStyle("-fx-text-fill: #90CAF9; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");

        Label lblLogin = new Label("Identifiant");
        lblLogin.setStyle("-fx-text-fill: #90CAF9; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");

        TextField txtLogin = new TextField();
        txtLogin.setPromptText("Votre identifiant...");
        txtLogin.setStyle(
                "-fx-background-color: #1A2B45;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #546E7A;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 15 10 15;" +
                        "-fx-font-size: 13;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;"
        );
        txtLogin.setPrefWidth(300);

        Label lblMdp = new Label("Mot de passe");
        lblMdp.setStyle("-fx-text-fill: #90CAF9; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Votre mot de passe...");
        txtPassword.setStyle(
                "-fx-background-color: #1A2B45;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #546E7A;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 15 10 15;" +
                        "-fx-font-size: 13;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;"
        );

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #0D1B2A;");
        content.getChildren().addAll(titre, sousTitre, new Separator(), lblLogin, txtLogin, lblMdp, txtPassword);

        dialogPane.setContent(content);
        dialogPane.setHeader(null);
        dialogPane.setGraphic(null);

        ButtonType btnConnexion = new ButtonType("Se connecter", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnConnexion, ButtonType.CANCEL);

        Button btnOk = (Button) dialogPane.lookupButton(btnConnexion);
        btnOk.setStyle(
                "-fx-background-color: linear-gradient(to right, #1565C0, #42A5F5);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 25 10 25;"
        );

        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancel.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #90CAF9;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 10 25 10 25;"
        );

        loginDialog.setResultConverter(b -> b == btnConnexion
                ? new String[]{txtLogin.getText(), txtPassword.getText()} : null);

        loginDialog.showAndWait().ifPresent(creds -> {
            String login = creds[0].trim();
            String mdp   = creds[1].trim();
            if (login.isEmpty() || mdp.isEmpty()) {
                afficherAlerte("Champs vides", "Veuillez remplir le login et le mot de passe.");
                return;
            }
            afficherAlerte("✅ Connexion", "Bienvenue " + login + " !");
            showDashboard();
        });
    }

    // ===================== DASHBOARD =====================
    @FXML
    private void showDashboard() {
        mainContent.getChildren().clear();
        mainContent.setStyle("-fx-background-color: " + FOND_BLANC + ";");

        Label title = new Label("État des Salles en Temps Réel");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");

        HBox statsBox = new HBox(25);
        statsBox.setPadding(new Insets(20, 0, 30, 0));

        List<Salle> salles = salleDAO.findAll();
        long countLibres   = salles.stream()
                .filter(s -> s.getEtatSalle() != null && s.getEtatSalle().equalsIgnoreCase("Disponible"))
                .count();
        long countOccupees = salles.size() - countLibres;

        statsBox.getChildren().addAll(
                creerWidgetStat("Total Salles",   String.valueOf(salles.size())),
                creerWidgetStat("Salles Libres",  String.valueOf(countLibres)),
                creerWidgetStat("Salles Occupées",String.valueOf(countOccupees))
        );

        containerSalles = new FlowPane(20, 20);
        containerSalles.setStyle("-fx-background-color: transparent;");
        refreshSallesList(false, "");

        mainContent.getChildren().addAll(title, statsBox, new Separator(), containerSalles);
    }

    // ===================== GESTION SALLES =====================
    @FXML
    private void showSalles() {
        mainContent.getChildren().clear();

        Label title = new Label("Gestion des Salles & Réservations");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher une salle ou un bâtiment...");
        searchField.setPrefWidth(500);
        searchField.setStyle(
                "-fx-padding: 12;" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-color: " + BLEU_MOYEN + ";" +
                        "-fx-border-radius: 25;" +
                        "-fx-font-size: 13;"
        );
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshSallesList(true, newVal));

        containerSalles = new FlowPane(20, 20);
        refreshSallesList(true, "");

        mainContent.getChildren().addAll(title, searchField, containerSalles);
    }

    // ================================================================
    // ============  ASSISTANT DE RÉSERVATION  (VERSION AMÉLIORÉE)  ===
    // ================================================================
    @FXML
    private void showReservations() {
        mainContent.getChildren().clear();

        // --- En-tête ---
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label("📅");
        iconLbl.setStyle("-fx-font-size: 32;");
        VBox titleBox = new VBox(2);
        Label title = new Label("Assistant de Réservation");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");
        Label subtitle = new Label("Trouvez et réservez une salle disponible en quelques clics");
        subtitle.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 13;");
        titleBox.getChildren().addAll(title, subtitle);
        headerBox.getChildren().addAll(iconLbl, titleBox);

        // --- Style commun champs ---
        String styleChamp =
                "-fx-background-color: #1A2B45;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #546E7A;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 15 10 15;" +
                        "-fx-font-size: 13;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;";

        String styleLabelSection =
                "-fx-text-fill: " + BLEU_CLAIR + "; -fx-font-weight: bold; -fx-font-size: 13; -fx-font-family: 'Segoe UI';";

        // ---- PANNEAU DE RECHERCHE ----
        VBox searchPanel = new VBox(18);
        searchPanel.setPadding(new Insets(25));
        searchPanel.setStyle(
                "-fx-background-color: " + BLEU_DEEP + ";" +
                        "-fx-background-radius: 16;"
        );

        // === ÉTAPE 1 : Infos cours ===
        HBox step1 = creerEtapeLabel("1", "Informations du cours");
        HBox row1 = new HBox(15);
        row1.setAlignment(Pos.CENTER_LEFT);

        TextField txtClasse = new TextField();
        txtClasse.setPromptText("Classe  (ex: L2 INFO A)");
        txtClasse.setStyle(styleChamp);
        txtClasse.setPrefWidth(200);

        ComboBox<String> cboType = new ComboBox<>();
        cboType.getItems().addAll("Cours", "TD", "TP", "Réunion", "Soutenance", "Événement");
        cboType.setPromptText("Type de séance");
        cboType.setStyle(
                "-fx-background-color: #1A2B45;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-font-size: 13;"
        );
        cboType.setPrefWidth(170);

        TextField txtEffectif = new TextField();
        txtEffectif.setPromptText("Effectif  (ex: 40)");
        txtEffectif.setStyle(styleChamp);
        txtEffectif.setPrefWidth(140);

        row1.getChildren().addAll(txtClasse, cboType, txtEffectif);

        // === ÉTAPE 2 : Date & Horaires ===
        HBox step2 = creerEtapeLabel("2", "Date et horaire souhaités");
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle(
                "-fx-background-color: #1A2B45;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;"
        );
        datePicker.setPrefWidth(175);

        TextField txtHeureDebut = new TextField("08:00");
        txtHeureDebut.setPromptText("Début  (HH:mm)");
        txtHeureDebut.setStyle(styleChamp);
        txtHeureDebut.setPrefWidth(140);

        TextField txtHeureFin = new TextField("10:00");
        txtHeureFin.setPromptText("Fin  (HH:mm)");
        txtHeureFin.setStyle(styleChamp);
        txtHeureFin.setPrefWidth(140);

        row2.getChildren().addAll(datePicker, txtHeureDebut, txtHeureFin);

        // === ÉTAPE 3 : Contact & Filtres ===
        HBox step3 = creerEtapeLabel("3", "Contact & critères optionnels");
        HBox row3 = new HBox(15);
        row3.setAlignment(Pos.CENTER_LEFT);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email du professeur (confirmation par mail)");
        txtEmail.setStyle(styleChamp);
        txtEmail.setPrefWidth(310);

        ComboBox<String> cboEquip = new ComboBox<>();
        cboEquip.getItems().addAll("Aucun", "Vidéoprojecteur", "Tableau interactif", "Climatisation", "Ordinateurs");
        cboEquip.setValue("Aucun");
        cboEquip.setStyle(
                "-fx-background-color: #1A2B45;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #1565C0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-font-size: 13;"
        );
        cboEquip.setPrefWidth(190);

        row3.getChildren().addAll(txtEmail, cboEquip);

        // === Bouton de recherche ===
        Button btnFilter = new Button("🔍   RECHERCHER UNE SALLE DISPONIBLE");
        btnFilter.setMaxWidth(Double.MAX_VALUE);
        btnFilter.setStyle(
                "-fx-background-color: linear-gradient(to right, #1565C0, #42A5F5);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 14 20 14 20;"
        );

        searchPanel.getChildren().addAll(step1, row1, step2, row2, step3, row3, btnFilter);

        // --- Zone résultats ---
        Label lblResultats = new Label("Salles disponibles");
        lblResultats.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");
        lblResultats.setVisible(false);

        FlowPane resultsBox = new FlowPane(20, 20);
        resultsBox.setPadding(new Insets(10, 0, 0, 0));

        // === Action du bouton ===
        btnFilter.setOnAction(e -> {
            try {
                String classeNom  = txtClasse.getText().trim();
                String emailProf  = txtEmail.getText().trim();
                String typeSeance = cboType.getValue() != null ? cboType.getValue() : "Cours";
                LocalDate dateResa = datePicker.getValue();

                int effectifReq = 0;
                if (!txtEffectif.getText().trim().isEmpty()) {
                    effectifReq = Integer.parseInt(txtEffectif.getText().trim());
                }

                if (classeNom.isEmpty()) {
                    afficherAlerte("Champ manquant", "Veuillez préciser la classe.");
                    return;
                }

                // Validation horaires
                LocalTime heureD = null, heureF = null;
                try {
                    heureD = LocalTime.parse(txtHeureDebut.getText().trim());
                    heureF = LocalTime.parse(txtHeureFin.getText().trim());
                } catch (Exception ex) {
                    afficherAlerte("Format horaire", "Utilisez le format HH:mm (ex: 08:00)");
                    return;
                }
                if (!heureF.isAfter(heureD)) {
                    afficherAlerte("Horaire invalide", "L'heure de fin doit être après l'heure de début.");
                    return;
                }

                final int effFinal = effectifReq;
                final LocalTime hD = heureD, hF = heureF;

                resultsBox.getChildren().clear();

                List<Salle> filtrage = salleDAO.findAll().stream()
                        .filter(s -> s.getCapacite() >= effFinal
                                && s.getEtatSalle() != null
                                && s.getEtatSalle().equalsIgnoreCase("Disponible"))
                        .sorted(Comparator.comparingInt(Salle::getCapacite))
                        .collect(Collectors.toList());

                lblResultats.setVisible(true);

                if (filtrage.isEmpty()) {
                    VBox aucune = new VBox(8);
                    aucune.setAlignment(Pos.CENTER);
                    aucune.setPadding(new Insets(30));
                    aucune.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
                    Label ico = new Label("🚫");
                    ico.setStyle("-fx-font-size: 36;");
                    Label msg = new Label("Aucune salle disponible pour cet effectif.");
                    msg.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14; -fx-font-weight: bold;");
                    Label suggestion = new Label("Essayez de réduire l'effectif ou de modifier le créneau.");
                    suggestion.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 12;");
                    aucune.getChildren().addAll(ico, msg, suggestion);
                    resultsBox.getChildren().add(aucune);
                } else {
                    for (Salle s : filtrage) {
                        resultsBox.getChildren().add(
                                creerCarteReservationAméliorée(s, classeNom, emailProf, typeSeance, dateResa, hD, hF)
                        );
                    }
                }

            } catch (NumberFormatException ex) {
                afficherAlerte("Erreur", "L'effectif doit être un nombre entier.");
            } catch (Exception ex) {
                ex.printStackTrace();
                afficherAlerte("Erreur", "Une erreur inattendue : " + ex.getMessage());
            }
        });

        mainContent.getChildren().addAll(headerBox, searchPanel, lblResultats, resultsBox);
    }

    /**
     * Crée un label d'étape numéroté pour le formulaire de réservation.
     */
    private HBox creerEtapeLabel(String numero, String texte) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label numLbl = new Label(numero);
        numLbl.setStyle(
                "-fx-background-color: #1565C0;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-min-width: 24; -fx-min-height: 24;" +
                        "-fx-max-width: 24; -fx-max-height: 24;" +
                        "-fx-background-radius: 12;" +
                        "-fx-alignment: center;"
        );

        Label txtLbl = new Label(texte);
        txtLbl.setStyle("-fx-text-fill: " + BLEU_CLAIR + "; -fx-font-weight: bold; -fx-font-size: 13; -fx-font-family: 'Segoe UI';");

        box.getChildren().addAll(numLbl, txtLbl);
        return box;
    }

    /**
     * Carte de salle améliorée pour l'assistant de réservation.
     * Affiche les infos complètes + bouton de confirmation intégré.
     */
    private VBox creerCarteReservationAméliorée(Salle salle, String nomClasse, String emailProf,
                                                String typeSeance, LocalDate date,
                                                LocalTime heureDebut, LocalTime heureFin) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(270);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.15), 12, 0, 0, 4);"
        );

        // Badge type de salle
        String categorie = salle.getCategorieSalle() != null ? salle.getCategorieSalle() : "Salle";
        Label badgeType = new Label("  " + categorie.toUpperCase() + "  ");
        badgeType.setStyle(
                "-fx-background-color: rgba(21,101,192,0.10);" +
                        "-fx-text-fill: #1565C0;" +
                        "-fx-font-size: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 3 8 3 8;"
        );

        Label lNom = new Label(salle.getNumeroSalle());
        lNom.setStyle("-fx-font-weight: bold; -fx-font-size: 26; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");

        Label lBat = new Label("📍 " + (salle.getBatiment() != null ? salle.getBatiment().getNomBatiment() : "N/A"));
        lBat.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold; -fx-font-size: 12;");

        // Infos capacité et équipements
        HBox infoRow = new HBox(15);
        Label lCap = new Label("👥 " + salle.getCapacite() + " places");
        lCap.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 12;");
        infoRow.getChildren().add(lCap);

        // Créneau demandé
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        String creneauStr = (date != null ? date.format(dtf) : "Aujourd'hui")
                + "  " + heureDebut + " → " + heureFin;
        Label lCreneau = new Label("🕐 " + creneauStr);
        lCreneau.setStyle("-fx-text-fill: #1565C0; -fx-font-size: 12; -fx-font-weight: bold;");

        // Statut disponible
        Label lStatut = new Label("✅  DISPONIBLE");
        lStatut.setAlignment(Pos.CENTER);
        lStatut.setMaxWidth(Double.MAX_VALUE);
        lStatut.setStyle(
                "-fx-background-color: rgba(21,101,192,0.10);" +
                        "-fx-text-fill: #1565C0;" +
                        "-fx-padding: 6 0;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-weight: bold; -fx-font-size: 12;"
        );

        // Bouton confirmation
        Button btnConfirm = new Button("RÉSERVER POUR  " + nomClasse.toUpperCase());
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setStyle(
                "-fx-background-color: linear-gradient(to right, #1565C0, #42A5F5);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 11 0;"
        );

        btnConfirm.setOnAction(e -> {
            confirmerReservation(salle, nomClasse, emailProf, typeSeance, date, heureDebut, heureFin);
        });

        card.getChildren().addAll(badgeType, lNom, lBat, infoRow, lCreneau, lStatut, btnConfirm);
        return card;
    }

    /**
     * Logique de confirmation de réservation — appelle les DAO et le NotificationService.
     */
    private void confirmerReservation(Salle salle, String nomClasse, String emailProf,
                                      String typeSeance, LocalDate date,
                                      LocalTime heureDebut, LocalTime heureFin) {
        try {
            Creneau creneau = new Creneau();
            creneau.setIdCreneau(UUID.randomUUID().toString());
            creneau.setHeureDebut(heureDebut);
            creneau.setHeureFin(heureFin);
            creneau.setDateSeance(date != null ? date : LocalDate.now());
            creneau.setSalle(salle);
            creneau.setTypeSeance(typeSeance);

            Reservation reservation = new Reservation();
            reservation.setMonCreneau(creneau);
            reservation.setNatureSession(nomClasse);
            reservation.setEtatReservation("Validée");
            reservation.setMotifReservation(typeSeance + " – " + nomClasse);
            reservation.setDateHeureReservation(LocalDateTime.now());
            reservation.setDateDernierModification(LocalDateTime.now());
            reservation.setNiveauPriorite(1);
            reservationDAO.save(reservation);

            salle.setEtatSalle("Occupée");
            salleDAO.update(salle);

            if (emailProf != null && !emailProf.isEmpty()) {
                notificationService.confirmerReservation(emailProf,
                        typeSeance + " – " + nomClasse + " | Salle " + salle.getNumeroSalle()
                                + " | " + heureDebut + "–" + heureFin);
            } else {
                // Notification interne si pas d'email
                notificationService.notifierConflit("Réservation enregistrée : Salle "
                        + salle.getNumeroSalle() + " pour " + nomClasse);
            }

            afficherAlerte("✅ Réservation confirmée",
                    "Salle : " + salle.getNumeroSalle() +
                            "\nClasse : " + nomClasse +
                            "\nType : " + typeSeance +
                            "\nHoraire : " + heureDebut + " – " + heureFin +
                            (emailProf.isEmpty() ? "" : "\n📧 Confirmation envoyée à " + emailProf));

            showReservations();

        } catch (Exception ex) {
            ex.printStackTrace();
            afficherAlerte("Erreur", "Échec de la réservation : " + ex.getMessage());
        }
    }

    // ================================================================
    // ========  HISTORIQUE DES NOTIFICATIONS  (VERSION AMÉLIORÉE)  ===
    // ================================================================
    @FXML
    private void showNotifications() {
        mainContent.getChildren().clear();

        // --- En-tête avec compteurs ---
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label("🔔");
        iconLbl.setStyle("-fx-font-size: 30;");
        VBox titleBox = new VBox(2);
        Label title = new Label("Historique des Notifications");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");
        Label subtitle = new Label("Suivi de toutes les alertes, confirmations et incidents du système");
        subtitle.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 13;");
        titleBox.getChildren().addAll(title, subtitle);
        headerBox.getChildren().addAll(iconLbl, titleBox);

        // Charger les notifications
        List<Notification> toutes;
        try {
            toutes = notificationDAO.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            toutes = new java.util.ArrayList<>();
        }
        final List<Notification> notifications = toutes;

        // --- Widgets statistiques ---
        long nbNonLus = notifications.stream()
                .filter(n -> "NON_LU".equalsIgnoreCase(n.getStatut()))
                .count();
        long nbEmail = notifications.stream()
                .filter(n -> "EMAIL".equalsIgnoreCase(n.getTypeCanal()))
                .count();
        long nbSysteme = notifications.stream()
                .filter(n -> "SYSTEME".equalsIgnoreCase(n.getTypeCanal()) || "DASHBOARD".equalsIgnoreCase(n.getTypeCanal()))
                .count();

        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(10, 0, 10, 0));
        statsRow.getChildren().addAll(
                creerMiniStat("Total", String.valueOf(notifications.size()), "#1565C0"),
                creerMiniStat("Non lus", String.valueOf(nbNonLus), "#EF4444"),
                creerMiniStat("Emails envoyés", String.valueOf(nbEmail), "#42A5F5"),
                creerMiniStat("Système", String.valueOf(nbSysteme), "#546E7A")
        );

        // --- Barre de filtres ---
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(5, 0, 5, 0));

        Label lblFiltrer = new Label("Filtrer :");
        lblFiltrer.setStyle("-fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-font-size: 13;");

        ToggleGroup toggleGrp = new ToggleGroup();
        ToggleButton btnTous    = creerBtnFiltre("Tous",    toggleGrp, true);
        ToggleButton btnEmails  = creerBtnFiltre("Emails",  toggleGrp, false);
        ToggleButton btnSysteme = creerBtnFiltre("Système", toggleGrp, false);
        ToggleButton btnNonLus  = creerBtnFiltre("Non lus", toggleGrp, false);

        TextField searchNotif = new TextField();
        searchNotif.setPromptText("🔍 Rechercher dans les messages...");
        searchNotif.setPrefWidth(280);
        searchNotif.setStyle(
                "-fx-padding: 8 12 8 12;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + BLEU_MOYEN + ";" +
                        "-fx-border-radius: 20;" +
                        "-fx-font-size: 12;"
        );

        filterBar.getChildren().addAll(lblFiltrer, btnTous, btnEmails, btnSysteme, btnNonLus, searchNotif);

        // --- Table des notifications ---
        TableView<Notification> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(420);
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.12), 10, 0, 0, 3);"
        );
        table.setPlaceholder(new Label("Aucune notification trouvée."));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm", Locale.FRENCH);

        // Colonne statut (pastille colorée)
        TableColumn<Notification, String> colStatut = new TableColumn<>("");
        colStatut.setPrefWidth(40);
        colStatut.setMinWidth(40);
        colStatut.setMaxWidth(40);
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                boolean nonLu = "NON_LU".equalsIgnoreCase(item);
                Circle pastille = new Circle(6);
                pastille.setFill(nonLu ? Color.web("#EF4444") : Color.web("#42A5F5"));
                setGraphic(pastille);
                setStyle("-fx-alignment: CENTER;");
            }
        });

        // Colonne canal
        TableColumn<Notification, String> colCanal = new TableColumn<>("Canal");
        colCanal.setPrefWidth(100);
        colCanal.setCellValueFactory(new PropertyValueFactory<>("typeCanal"));
        colCanal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                String icon = "📧";
                String color = "#1565C0";
                String bg = "rgba(21,101,192,0.10)";
                if ("SYSTEME".equalsIgnoreCase(item)) { icon = "⚙️"; color = "#546E7A"; bg = "rgba(84,110,122,0.10)"; }
                else if ("DASHBOARD".equalsIgnoreCase(item)) { icon = "📊"; color = "#1565C0"; bg = "rgba(21,101,192,0.10)"; }
                Label lbl = new Label(icon + " " + item);
                lbl.setStyle(
                        "-fx-background-color: " + bg + ";" +
                                "-fx-text-fill: " + color + ";" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 11;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 3 8 3 8;"
                );
                setGraphic(lbl);
                setText(null);
            }
        });

        // Colonne date
        TableColumn<Notification, LocalDateTime> colDate = new TableColumn<>("Date / Heure");
        colDate.setPrefWidth(160);
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item.format(formatter));
                setStyle("-fx-text-fill: " + BLEU_DEEP + "; -fx-font-weight: bold; -fx-font-size: 12;");
            }
        });

        // Colonne message
        TableColumn<Notification, String> colMsg = new TableColumn<>("Message");
        colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));
        colMsg.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setWrapText(true);
                setStyle("-fx-text-fill: #1A2B45; -fx-font-size: 12; -fx-padding: 5 8 5 8;");
            }
        });

        // Colonne actions
        TableColumn<Notification, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Notification notif = getTableView().getItems().get(getIndex());
                Button btnMarquer = new Button(
                        "NON_LU".equalsIgnoreCase(notif.getStatut()) ? "✔ Marquer lu" : "↩ Non lu"
                );
                btnMarquer.setStyle(
                        "-fx-background-color: " + BLEU_DEEP + ";" +
                                "-fx-text-fill: #90CAF9;" +
                                "-fx-font-size: 10;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5 8 5 8;"
                );
                btnMarquer.setOnAction(e -> {
                    String nvStatut = "NON_LU".equalsIgnoreCase(notif.getStatut()) ? "LU" : "NON_LU";
                    notif.setStatut(nvStatut);
                    try { notificationDAO.save(notif); } catch (Exception ex) { ex.printStackTrace(); }
                    showNotifications();
                });
                setGraphic(btnMarquer);
            }
        });

        table.getColumns().addAll(colStatut, colCanal, colDate, colMsg, colAction);
        table.getItems().addAll(notifications);

        // Logique filtre + recherche
        Runnable appliquerFiltre = () -> {
            String recherche = searchNotif.getText().toLowerCase().trim();
            String filtre = ((ToggleButton) toggleGrp.getSelectedToggle()).getText();
            table.getItems().setAll(notifications.stream()
                    .filter(n -> {
                        if ("Emails".equals(filtre) && !"EMAIL".equalsIgnoreCase(n.getTypeCanal())) return false;
                        if ("Système".equals(filtre) && !"SYSTEME".equalsIgnoreCase(n.getTypeCanal())
                                && !"DASHBOARD".equalsIgnoreCase(n.getTypeCanal())) return false;
                        if ("Non lus".equals(filtre) && !"NON_LU".equalsIgnoreCase(n.getStatut())) return false;
                        if (!recherche.isEmpty() && n.getMessage() != null
                                && !n.getMessage().toLowerCase().contains(recherche)) return false;
                        return true;
                    })
                    .collect(Collectors.toList()));
        };

        toggleGrp.selectedToggleProperty().addListener((obs, o, nv) -> { if (nv != null) appliquerFiltre.run(); });
        searchNotif.textProperty().addListener((obs, o, nv) -> appliquerFiltre.run());

        // Bouton "Tout marquer comme lu"
        Button btnToutLu = new Button("✔  Tout marquer comme lu");
        btnToutLu.setStyle(
                "-fx-background-color: " + BLEU_DEEP + ";" +
                        "-fx-text-fill: #90CAF9;" +
                        "-fx-font-size: 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 16 8 16;"
        );
        btnToutLu.setOnAction(e -> {
            notifications.stream()
                    .filter(n -> "NON_LU".equalsIgnoreCase(n.getStatut()))
                    .forEach(n -> {
                        n.setStatut("LU");
                        try { notificationDAO.save(n); } catch (Exception ex) { ex.printStackTrace(); }
                    });
            showNotifications();
        });

        HBox actionBar = new HBox(btnToutLu);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.setPadding(new Insets(5, 0, 0, 0));

        mainContent.getChildren().addAll(headerBox, statsRow, filterBar, table, actionBar);
    }

    /**
     * Mini widget statistique pour la page notifications.
     */
    private VBox creerMiniStat(String label, String valeur, String couleur) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12, 20, 12, 20));
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.10), 8, 0, 0, 2);"
        );
        Label lVal = new Label(valeur);
        lVal.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");
        Label lLbl = new Label(label);
        lLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #546E7A;");
        box.getChildren().addAll(lVal, lLbl);
        return box;
    }

    /**
     * Bouton toggle pour les filtres de notifications.
     */
    private ToggleButton creerBtnFiltre(String texte, ToggleGroup grp, boolean selected) {
        ToggleButton btn = new ToggleButton(texte);
        btn.setToggleGroup(grp);
        btn.setSelected(selected);
        String base =
                "-fx-background-color: " + (selected ? BLEU_MOYEN : "white") + ";" +
                        "-fx-text-fill: " + (selected ? "white" : BLEU_DEEP) + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 6 16 6 16;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: " + BLEU_MOYEN + ";" +
                        "-fx-border-radius: 20;";
        btn.setStyle(base);
        btn.selectedProperty().addListener((obs, wasSelected, isNow) -> {
            btn.setStyle(
                    "-fx-background-color: " + (isNow ? BLEU_MOYEN : "white") + ";" +
                            "-fx-text-fill: " + (isNow ? "white" : BLEU_DEEP) + ";" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 6 16 6 16;" +
                            "-fx-font-size: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: " + BLEU_MOYEN + ";" +
                            "-fx-border-radius: 20;"
            );
        });
        return btn;
    }

    // ===================== UTILITAIRES =====================
    private void refreshSallesList(boolean interactionActive, String filtre) {
        containerSalles.getChildren().clear();
        try {
            List<Salle> liste = salleDAO.findAll();
            if (!filtre.isEmpty()) {
                String f = filtre.toLowerCase();
                liste = liste.stream()
                        .filter(s -> s.getNumeroSalle().toLowerCase().contains(f) ||
                                (s.getBatiment() != null && s.getBatiment().getNomBatiment() != null
                                        && s.getBatiment().getNomBatiment().toLowerCase().contains(f)))
                        .collect(Collectors.toList());
            }
            for (Salle s : liste) {
                containerSalles.getChildren().add(creerCarteSalleDynamique(s, interactionActive));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox creerCarteSalleDynamique(Salle salle, boolean avecBouton) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(260);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.15), 12, 0, 0, 4);"
        );

        Label lNom = new Label(salle.getNumeroSalle());
        lNom.setStyle("-fx-font-weight: bold; -fx-font-size: 24; -fx-text-fill: " + BLEU_DEEP + "; -fx-font-family: 'Segoe UI';");

        Label lBat = new Label("📍 " + (salle.getBatiment() != null ? salle.getBatiment().getNomBatiment() : "N/A"));
        lBat.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");

        Label lInfo = new Label("👥 Capacité: " + salle.getCapacite());
        lInfo.setStyle("-fx-text-fill: #546E7A;");

        String etat = salle.getEtatSalle() != null ? salle.getEtatSalle() : "Inconnu";
        boolean estDisponible = etat.equalsIgnoreCase("Disponible");

        Label lStatut = new Label(etat.toUpperCase());
        lStatut.setAlignment(Pos.CENTER);
        lStatut.setMaxWidth(Double.MAX_VALUE);
        lStatut.setStyle(
                "-fx-background-color: " + (estDisponible ? "rgba(21,101,192,0.12)" : "rgba(239,68,68,0.12)") + ";" +
                        "-fx-text-fill: "         + (estDisponible ? "#1565C0" : "#EF4444") + ";" +
                        "-fx-padding: 8 0;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(lNom, lBat, lInfo, lStatut);

        if (avecBouton) {
            Button actionBtn = new Button(estDisponible ? "RÉSERVER" : "LIBÉRER");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1565C0, #42A5F5);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 10 0;"
            );
            actionBtn.setOnAction(e -> gererClicSalle(salle));
            card.getChildren().add(actionBtn);
        }
        return card;
    }

    private VBox creerWidgetStat(String titre, String valeur) {
        VBox widget = new VBox(5);
        widget.setAlignment(Pos.CENTER);
        widget.setPrefSize(210, 120);
        widget.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1565C0, #0D1B2A);" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(21,101,192,0.4), 12, 0, 0, 4);"
        );
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-text-fill: #90CAF9; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        Label lblVal = new Label(valeur);
        lblVal.setStyle("-fx-text-fill: #42A5F5; -fx-font-size: 40; -fx-font-weight: bold;");
        widget.getChildren().addAll(lblTitre, lblVal);
        return widget;
    }

    private void gererClicSalle(Salle salle) {
        if (salle.getEtatSalle() != null && salle.getEtatSalle().equalsIgnoreCase("Occupée")) {
            salle.setEtatSalle("Disponible");
            salleDAO.update(salle);
            showSalles();
        } else {
            showReservations();
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
