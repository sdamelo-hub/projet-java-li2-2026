package ProjetUniv_scheduler;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * LoginController — Connexion UNIV-SCHEDULER.
 *
 * CORRECTIONS APPLIQUÉES :
 *  1. Import "Color" supprimé (inutile — remplacé par couleur CSS string)
 *  2. profil.name() → profil.label dans filtrerMenuLateral()
 *     (.name() retourne "ADMINISTRATEUR" ; .label retourne "Administrateur")
 *  3. Modality importée explicitement (plus de référence qualifiée longue)
 */
public class LoginController {

    // ── Charte graphique ───────────────────────────────────────────────────────
    private static final String BLEU_DEEP  = "#1E2732";
    private static final String VERT_LIME  = "#A3FF33";
    private static final String FOND_BLANC = "#FFFFFF";

    // ── Dépendances ────────────────────────────────────────────────────────────
    private final Stage          primaryStage;
    private final MainController mainController;
    private final Scene          mainScene;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // ── Profils disponibles ────────────────────────────────────────────────────
    public enum Profil {
        ADMINISTRATEUR("Administrateur", "⚡", "Configuration globale du système"),
        GESTIONNAIRE  ("Gestionnaire",   "🛠️", "Planification & Emplois du temps"),
        ENSEIGNANT    ("Enseignant",      "📖", "Consultation & Réservation"),
        ETUDIANT      ("Etudiant",        "🎓", "Consultation des plannings");

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
    //  ÉCRAN 1 : Sélection du profil
    // ══════════════════════════════════════════════════════════════════════════
    public void show() {
        VBox root = new VBox(50);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60));
        root.setStyle("-fx-background-color: " + FOND_BLANC + ";");

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        Label logo = new Label("UNIV-SCHEDULER");
        logo.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 32; -fx-font-weight: bold;"
                    + " -fx-text-fill: " + BLEU_DEEP + ";");

        Label subtitle = new Label("SÉLECTIONNEZ VOTRE PORTAIL D'ACCÈS");
        subtitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14; -fx-text-fill: #64748b;");

        Region accent = new Region();
        accent.setPrefSize(120, 4);
        accent.setMaxWidth(120);
        accent.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 2;");

        header.getChildren().addAll(logo, accent, subtitle);

        HBox profileGrid = new HBox(30);
        profileGrid.setAlignment(Pos.CENTER);
        for (Profil p : Profil.values()) {
            profileGrid.getChildren().add(creerCarteProfile(p));
        }

        root.getChildren().addAll(header, profileGrid);

        primaryStage.setScene(new Scene(root, 1100, 550));
        primaryStage.setTitle("UNIV-SCHEDULER — Connexion");
        primaryStage.show();
    }

    // ── Carte profil cliquable ─────────────────────────────────────────────────
    private VBox creerCarteProfile(Profil profil) {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 25, 30, 25));
        card.setPrefSize(220, 200);
        card.setCursor(javafx.scene.Cursor.HAND);

        String styleBase = "-fx-background-color: " + BLEU_DEEP + ";"
                         + "-fx-background-radius: 20;"
                         + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 12, 0, 0, 4);";
        card.setStyle(styleBase);

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(64, 64);
        iconWrap.setMaxSize(64, 64);
        iconWrap.setStyle("-fx-background-color: " + VERT_LIME + "; -fx-background-radius: 32;");
        Label lIcon = new Label(profil.icon);
        lIcon.setStyle("-fx-font-size: 26;");
        iconWrap.getChildren().add(lIcon);

        Label lLabel = new Label(profil.label.toUpperCase());
        lLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 13;"
                      + " -fx-text-fill: " + VERT_LIME + ";");

        Label lDesc = new Label(profil.desc);
        lDesc.setWrapText(true);
        lDesc.setAlignment(Pos.CENTER);
        lDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        card.getChildren().addAll(iconWrap, lLabel, lDesc);

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-8);
            card.setStyle(styleBase
                + "-fx-border-color: " + VERT_LIME + ";"
                + "-fx-border-width: 2; -fx-border-radius: 20;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle(styleBase);
        });
        card.setOnMouseClicked(e -> showLoginForm(profil));
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ÉCRAN 2 : Formulaire de connexion (dialog modal)
    // ══════════════════════════════════════════════════════════════════════════
    private void showLoginForm(Profil profil) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);   // ← import explicite, plus d'ambiguïté
        dialog.setTitle("Connexion — " + profil.label);
        dialog.setResizable(false);

        VBox root = new VBox(25);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: " + BLEU_DEEP + ";"
                    + "-fx-border-color: " + VERT_LIME + ";"
                    + "-fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Titre
        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label lIcon = new Label(profil.icon); lIcon.setStyle("-fx-font-size: 28;");
        VBox titleInfo = new VBox(3);
        Label lTitle = new Label("CONNEXION " + profil.label.toUpperCase());
        lTitle.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 17;"
                      + " -fx-text-fill: " + VERT_LIME + ";");
        Label lSub = new Label(profil.desc);
        lSub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");
        titleInfo.getChildren().addAll(lTitle, lSub);
        titleRow.getChildren().addAll(lIcon, titleInfo);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + VERT_LIME + "44;");

        // Style champs
        String fieldStyle = "-fx-background-color: #121a21;"
                          + "-fx-text-fill: white;"
                          + "-fx-prompt-text-fill: #5e6d7a;"
                          + "-fx-border-color: #2d3f50;"
                          + "-fx-border-radius: 6; -fx-background-radius: 6;"
                          + "-fx-padding: 10; -fx-font-size: 13;";

        // Champ identifiant
        VBox gId = new VBox(5);
        Label lId = new Label(getIdentifiantLabel(profil));
        lId.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10;"
                   + " -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        TextField txtId = new TextField();
        txtId.setPromptText(getIdentifiantPlaceholder(profil));
        txtId.setPrefWidth(340);
        txtId.setStyle(fieldStyle);
        gId.getChildren().addAll(lId, txtId);

        // Champ mot de passe
        VBox gMdp = new VBox(5);
        Label lMdp = new Label("🔒 MOT DE PASSE");
        lMdp.setStyle("-fx-text-fill: " + VERT_LIME + "; -fx-font-size: 10;"
                    + " -fx-font-weight: bold; -fx-font-family: 'Consolas';");
        PasswordField txtMdp = new PasswordField();
        txtMdp.setPromptText("••••••••");
        txtMdp.setPrefWidth(340);
        txtMdp.setStyle(fieldStyle);
        gMdp.getChildren().addAll(lMdp, txtMdp);

        VBox formFields = new VBox(16);
        formFields.getChildren().addAll(gId, gMdp);

        // Message d'erreur (caché par défaut)
        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill: #FF3131; -fx-font-size: 12;");
        errLabel.setVisible(false);
        errLabel.setManaged(false);

        // Bouton connexion
        Button btnLogin = new Button("SE CONNECTER");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(44);
        btnLogin.setCursor(javafx.scene.Cursor.HAND);
        String bs = "-fx-background-color: " + VERT_LIME + "; -fx-text-fill: " + BLEU_DEEP
                  + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 14;";
        String bh = "-fx-background-color: white; -fx-text-fill: " + BLEU_DEEP
                  + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 14;";
        btnLogin.setStyle(bs);
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(bh));
        btnLogin.setOnMouseExited (e -> btnLogin.setStyle(bs));

        btnLogin.setOnAction(e -> {
            String identifiant = txtId.getText().trim();
            String mdp         = txtMdp.getText(); // PAS de trim() sur le mot de passe

            if (identifiant.isEmpty() || mdp.isEmpty()) {
                afficherErreur(errLabel, "⚠️ Tous les champs sont obligatoires.");
                return;
            }

            Utilisateur u = authentifier(profil, identifiant, mdp);

            if (u == null) {
                afficherErreur(errLabel, "❌ Identifiant ou mot de passe incorrect.");
                txtMdp.clear();
                txtMdp.requestFocus();
                return;
            }

            // Connexion réussie
            dialog.close();
            primaryStage.setScene(mainScene);
            primaryStage.setTitle("UNIV-SCHEDULER — " + profil.label);
            routerVersVue(profil, u);
        });

        // Entrée depuis le champ mot de passe = clic bouton
        txtMdp.setOnAction(e -> btnLogin.fire());

        // Bouton retour
        Button btnRetour = new Button("← Choisir un autre profil");
        btnRetour.setStyle("-fx-background-color: transparent; -fx-text-fill: #5e6d7a;"
                         + " -fx-cursor: hand; -fx-font-size: 11;");
        btnRetour.setOnAction(e -> { dialog.close(); show(); });

        root.getChildren().addAll(titleRow, sep, formFields, errLabel, btnLogin, btnRetour);

        // ── CORRECTION 2 : pas de Color.TRANSPARENT (import Color supprimé)
        // On utilise un fond blanc via CSS sur la scène, aucun import Color nécessaire
        Scene scene = new Scene(root, 420, 470);
        dialog.setScene(scene);
        dialog.show();
        txtId.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AUTHENTIFICATION
    // ══════════════════════════════════════════════════════════════════════════
    private Utilisateur authentifier(Profil profil, String identifiant, String mdp) {
        try {
            Utilisateur u = utilisateurDAO.findByIdentifiant(identifiant);
            if (u == null) {
                System.out.println("❌ Aucun utilisateur pour : [" + identifiant + "]");
                return null;
            }

            // Vérification mot de passe
            String mdpBDD = u.getMotDePasse();
            if (mdpBDD == null || !mdp.equals(mdpBDD)) {
                System.out.println("❌ MDP incorrect. Saisi:[" + mdp + "] BDD:[" + mdpBDD + "]");
                return null;
            }

            // Vérification rôle
            if (!roleCorrespondAuProfil(u.getRole(), profil)) {
                System.out.println("❌ Rôle incompatible. Profil:[" + profil.label
                    + "] Rôle BDD:[" + u.getRole() + "]");
                return null;
            }

            System.out.println("✅ Connexion réussie : " + u.getNom() + " [" + u.getRole() + "]");
            return u;

        } catch (Exception e) {
            System.err.println("❌ Erreur auth : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean roleCorrespondAuProfil(String roleEnBase, Profil profil) {
        if (roleEnBase == null) return false;
        // Normalisation pour ignorer accents et casse
        String role = roleEnBase.trim().toLowerCase()
                                .replace("é", "e")
                                .replace("è", "e")
                                .replace("ê", "e");
        return switch (profil) {
            case ADMINISTRATEUR -> role.equals("administrateur");
            case GESTIONNAIRE   -> role.equals("gestionnaire");
            case ENSEIGNANT     -> role.equals("enseignant");
            case ETUDIANT       -> role.equals("etudiant") || role.equals("etudiante");
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ROUTING
    // ══════════════════════════════════════════════════════════════════════════
    private void routerVersVue(Profil profil, Utilisateur utilisateur) {
        // ── CORRECTION 1 : profil.label (ex: "Gestionnaire") au lieu de profil.name()
        //    qui retourne "GESTIONNAIRE" (enum constant name, tout en majuscules)
        //    filtrerMenuLateral() attend "Gestionnaire", "Administrateur", etc.
        mainController.filtrerMenuLateral(profil.label);
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
