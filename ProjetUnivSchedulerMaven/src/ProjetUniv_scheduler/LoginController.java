package ProjetUniv_scheduler;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LoginController {

    // ── Charte graphique ───────────────────────────────────────────────────────
    private static final String BLEU_DEEP  = "#1E2732";
    private static final String VERT_LIME  = "#A3FF33";
    private static final String FOND_BLANC = "#FFFFFF";

    // ── Dépendances ────────────────────────────────────────────────────────────
    private final Stage           primaryStage;
    private final MainController  mainController;
    private final Scene           mainScene;          // ← NOUVEAU : scène à restaurer
    private final UtilisateurDAO  utilisateurDAO = new UtilisateurDAO();

    // ── Profils disponibles ────────────────────────────────────────────────────
    public enum Profil {
        ADMINISTRATEUR("Administrateur", "⚡", "Configuration globale du système"),
        GESTIONNAIRE  ("Gestionnaire",   "🛠️", "Planification & Emplois du temps"),
        ENSEIGNANT    ("Enseignant",      "📖", "Consultation & Réservation"),
        ETUDIANT      ("Étudiant",        "🎓", "Consultation des plannings");

        final String label;
        final String icon;
        final String desc;

        Profil(String label, String icon, String desc) {
            this.label = label;
            this.icon  = icon;
            this.desc  = desc;
        }
    }

    // ── Constructeur ───────────────────────────────────────────────────────────
    public LoginController(Stage primaryStage, MainController mainController, Scene mainScene) {
        this.primaryStage   = primaryStage;
        this.mainController = mainController;
        this.mainScene      = mainScene;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ÉTAPE 1 : Écran de sélection du profil
    // ══════════════════════════════════════════════════════════════════════════
    public void show() {
        VBox root = new VBox(50);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: " + FOND_BLANC + ";");

        // ── En-tête ────────────────────────────────────────────────────────
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        Label logo = new Label("UNIV-SCHEDULER");
        logo.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: " + BLEU_DEEP + ";");

        Label subtitle = new Label("SÉLECTIONNEZ VOTRE PORTAIL D'ACCÈS");
        subtitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14; -fx-text-fill: #64748b;");

        Region accent = new Region();
        accent.setPrefSize(120, 4);
        accent.setMaxWidth(120);
        accent.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 2;");

        header.getChildren().addAll(logo, accent, subtitle);

        // ── Grille des profils ─────────────────────────────────────────────
        HBox profileGrid = new HBox(30);
        profileGrid.setAlignment(Pos.CENTER);

        for (Profil p : Profil.values()) {
            profileGrid.getChildren().add(creerCarteProfile(p));
        }

        root.getChildren().addAll(header, profileGrid);

        Scene loginScene = new Scene(root, 1100, 550);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("UNIV-SCHEDULER — Connexion");
        primaryStage.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Carte cliquable pour chaque profil
    // ══════════════════════════════════════════════════════════════════════════
    private VBox creerCarteProfile(Profil profil) {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 25, 30, 25));
        card.setPrefSize(220, 200);
        card.setCursor(javafx.scene.Cursor.HAND);

        String styleBase = "-fx-background-color: " + BLEU_DEEP + "; " +
                           "-fx-background-radius: 20; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 12, 0, 0, 4);";
        card.setStyle(styleBase);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(64, 64);
        iconWrap.setMaxSize(64, 64);
        iconWrap.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 32;");

        Label lIcon = new Label(profil.icon);
        lIcon.setStyle("-fx-font-size: 26;");
        iconWrap.getChildren().add(lIcon);

        Label lLabel = new Label(profil.label.toUpperCase());
        lLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 13; " +
                        "-fx-text-fill: " + VERT_LIME + ";");

        Label lDesc = new Label(profil.desc);
        lDesc.setWrapText(true);
        lDesc.setAlignment(Pos.CENTER);
        lDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        card.getChildren().addAll(iconWrap, lLabel, lDesc);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-8);
            card.setStyle(styleBase +
                "-fx-border-color: " + VERT_LIME + "; " +
                "-fx-border-width: 2; -fx-border-radius: 20;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle(styleBase);
        });

        card.setOnMouseClicked(e -> showLoginForm(profil));
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ÉTAPE 2 : Formulaire de connexion (dialog modal)
    // ══════════════════════════════════════════════════════════════════════════
    private void showLoginForm(Profil profil) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Connexion — " + profil.label);
        dialog.setResizable(false);

        VBox root = new VBox(25);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: " + BLEU_DEEP + "; " +
                      "-fx-border-color: " + VERT_LIME + "; " +
                      "-fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

        // ── Titre ──────────────────────────────────────────────────────────
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label lIcon = new Label(profil.icon);
        lIcon.setStyle("-fx-font-size: 28;");

        VBox titleInfo = new VBox(3);
        Label lTitle = new Label("CONNEXION " + profil.label.toUpperCase());
        lTitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 17; -fx-text-fill: " + VERT_LIME + ";");
        Label lSub = new Label(profil.desc);
        lSub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");
        titleInfo.getChildren().addAll(lTitle, lSub);

        titleRow.getChildren().addAll(lIcon, titleInfo);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + VERT_LIME + "44;");

        // ── Style des champs ───────────────────────────────────────────────
        String fieldStyle = "-fx-background-color: #121a21; " +
                            "-fx-text-fill: white; " +
                            "-fx-prompt-text-fill: #5e6d7a; " +
                            "-fx-border-color: #2d3f50; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; " +
                            "-fx-padding: 10; -fx-font-size: 13;";

        // ── Champ identifiant ──────────────────────────────────────────────
        VBox gIdentifiant = new VBox(5);
        Label lIdentifiant = new Label(getIdentifiantLabel(profil));
        lIdentifiant.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        TextField txtIdentifiant = new TextField();
        txtIdentifiant.setPromptText(getIdentifiantPlaceholder(profil));
        txtIdentifiant.setPrefWidth(340);
        txtIdentifiant.setStyle(fieldStyle);
        gIdentifiant.getChildren().addAll(lIdentifiant, txtIdentifiant);

        // ── Champ mot de passe ─────────────────────────────────────────────
        VBox gMdp = new VBox(5);
        Label lMdp = new Label("🔒 MOT DE PASSE");
        lMdp.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        PasswordField txtMdp = new PasswordField();
        txtMdp.setPromptText("••••••••");
        txtMdp.setPrefWidth(340);
        txtMdp.setStyle(fieldStyle);
        gMdp.getChildren().addAll(lMdp, txtMdp);

        VBox formFields = new VBox(16);
        formFields.getChildren().addAll(gIdentifiant, gMdp);

        // ── Label d'erreur ─────────────────────────────────────────────────
        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill: #FF3131; -fx-font-size: 12;");
        errLabel.setVisible(false);
        errLabel.setManaged(false);

        // ── Bouton CONNEXION ───────────────────────────────────────────────
        Button btnLogin = new Button("SE CONNECTER");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(44);
        btnLogin.setCursor(javafx.scene.Cursor.HAND);

        String btnStyle = "-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP +
                          "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 14;";
        String btnHover = "-fx-background-color: white; -fx-text-fill: " + BLEU_DEEP +
                          "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 14;";
        btnLogin.setStyle(btnStyle);
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(btnHover));
        btnLogin.setOnMouseExited (e -> btnLogin.setStyle(btnStyle));

        btnLogin.setOnAction(e -> {
            String identifiant = txtIdentifiant.getText().trim();
            String mdp         = txtMdp.getText();      // NE PAS trim() le mot de passe

            if (identifiant.isEmpty() || mdp.isEmpty()) {
                afficherErreur(errLabel, "⚠️ Tous les champs sont obligatoires.");
                return;
            }

            Utilisateur u = authentifier(profil, identifiant, mdp);

            if (u == null) {
                afficherErreur(errLabel, "❌ Identifiant ou mot de passe incorrect.");
                txtMdp.clear();
                return;
            }

            // ── Connexion réussie ──────────────────────────────────────────
            dialog.close();
            // CORRECTIF CRITIQUE : restaurer la scène principale AVANT de router
            primaryStage.setScene(mainScene);
            primaryStage.setTitle("UNIV-SCHEDULER — " + profil.label);
            routerVersVue(profil, u);
        });

        txtMdp.setOnAction(e -> btnLogin.fire());

        // ── Lien Retour ────────────────────────────────────────────────────
        Button btnRetour = new Button("← Choisir un autre profil");
        btnRetour.setStyle("-fx-background-color: transparent; -fx-text-fill: #5e6d7a; -fx-cursor: hand; -fx-font-size: 11;");
        btnRetour.setOnAction(e -> { dialog.close(); show(); });

        root.getChildren().addAll(titleRow, sep, formFields, errLabel, btnLogin, btnRetour);

        Scene scene = new Scene(root, 420, 470);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();

        txtIdentifiant.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ÉTAPE 3 : Authentification
    // ══════════════════════════════════════════════════════════════════════════
    private Utilisateur authentifier(Profil profil, String identifiant, String mdp) {
        try {
            Utilisateur u = utilisateurDAO.findByIdentifiant(identifiant);
            if (u == null) return null;

            // Vérification mot de passe (comparaison simple en clair)
            // TODO : migrer vers BCrypt pour la production
            if (!mdp.equals(u.getMotDePasse())) return null;

            // Vérification que le rôle correspond au profil sélectionné
            if (!roleCorrespondAuProfil(u.getRole(), profil)) return null;

            return u;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'authentification : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean roleCorrespondAuProfil(String roleEnBase, Profil profil) {
        if (roleEnBase == null) return false;
        // Utilisation de contains() pour ignorer les accents et la casse
        String role = roleEnBase.trim().toLowerCase()
                                .replace("é", "e").replace("è", "e").replace("ê", "e");
        return switch (profil) {
            case ADMINISTRATEUR -> role.equals("administrateur");
            case GESTIONNAIRE   -> role.equals("gestionnaire");
            case ENSEIGNANT     -> role.equals("enseignant");
            case ETUDIANT       -> role.equals("etudiant") || role.equals("etudiante");
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ÉTAPE 4 : Routing vers la vue du profil
    // ══════════════════════════════════════════════════════════════════════════
    private void routerVersVue(Profil profil, Utilisateur utilisateur) {
        
    	mainController.filtrerMenuLateral(profil.name());
        mainController.setUtilisateurConnecte(utilisateur);

        switch (profil) {
            case ADMINISTRATEUR -> mainController.showAdminPanel();
            case GESTIONNAIRE   -> mainController.showManagerDashboard();
            case ENSEIGNANT     -> mainController.showTeacherView(utilisateur);
            case ETUDIANT       -> mainController.showStudentView(utilisateur);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void afficherErreur(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private String getIdentifiantLabel(Profil profil) {
        return switch (profil) {
            case ADMINISTRATEUR -> "🔑 IDENTIFIANT ADMIN";
            case GESTIONNAIRE   -> "🛠️ IDENTIFIANT GESTIONNAIRE";
            case ENSEIGNANT     -> "📧 EMAIL PROFESSIONNEL";
            case ETUDIANT       -> "🎓 NUMÉRO MATRICULE";
        };
    }

    private String getIdentifiantPlaceholder(Profil profil) {
        return switch (profil) {
            case ADMINISTRATEUR -> "ex : admin@univ.sn";
            case GESTIONNAIRE   -> "ex : gestionnaire@univ.sn";
            case ENSEIGNANT     -> "ex : prof.nom@univ.sn";
            case ETUDIANT       -> "ex : 20231234";
        };
    }
}
