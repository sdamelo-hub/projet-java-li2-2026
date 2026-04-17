package ProjetUniv_scheduler;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestionnaireModule — Module de planification complet.
 *
 * Fonctionnalités :
 *  1. Dashboard KPIs temps réel
 *  2. CRUD Cours + barre de progression
 *  3. Calendrier hebdomadaire visuel (grille 7h-21h × Lun-Sam)
 *  4. Assignation IA (suggestion automatique de salle)
 *  5. Détection et résolution intelligente des conflits
 *  6. Statistiques BarChart + PieChart + tableau récap
 *  7. Historique réservations avec filtres + validation/annulation
 */
public class GestionnaireModule {

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final String BD   = "#1E2732";
    private static final String VL   = "#A3FF33";
    private static final String BG   = "#F8FAFC";
    private static final String RED  = "#EF4444";
    private static final String WARN = "#F59E0B";
    private static final String CARD = "#FFFFFF";

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final CoursDAO        coursDAO   = new CoursDAO(HibernateUtil.getSessionFactory());
    private final CreneauDAO      creneauDAO = new CreneauDAO(HibernateUtil.getSessionFactory());
    private final SalleDAO        salleDAO   = new SalleDAO();
    private final ReservationDAO  reservDAO  = new ReservationDAO(HibernateUtil.getSessionFactory());
    private final NotificationService notifSvc = new NotificationService(
            new NotificationDAO(HibernateUtil.getSessionFactory()));

    // ── État ──────────────────────────────────────────────────────────────────
    private final VBox       mainContent;
    private final Utilisateur gestionnaire;
    private LocalDate semaineActive =
            LocalDate.now().with(WeekFields.of(Locale.FRENCH).dayOfWeek(), 1);
    private final List<String> journal = new ArrayList<>();

    public GestionnaireModule(VBox mainContent, Utilisateur gestionnaire) {
        this.mainContent  = mainContent;
        this.gestionnaire = gestionnaire;
        log("Session ouverte par " + (gestionnaire != null ? gestionnaire.getNom() : "?"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════
    public void showDashboard() {
        clear();

        VBox titleBox = new VBox(4);
        Label title = lbl26("STATION DE PLANIFICATION", BD);
        Label sub   = lbl12("Gestionnaire : " + (gestionnaire != null
                ? gestionnaire.getNom() + " " + nvl(gestionnaire.getPrenom()) : "—"), "#64748b");
        titleBox.getChildren().addAll(title, accentBar(120), sub);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label clock = lbl12(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)), "#94a3b8");
        topBar.getChildren().addAll(titleBox, sp, clock);

        // KPIs
        List<Cours>  cours  = coursDAO.findAll();
        List<Salle>  salles = salleDAO.findAll();
        long conflicts  = detecterConflits(creneauDAO.findByWeek(semaineActive)).size();
        long libres     = salles.stream()
                .filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle())).count();

        HBox kpis = new HBox(20);
        kpis.setPadding(new Insets(20, 0, 20, 0));
        kpis.getChildren().addAll(
            kpi("📚 COURS TOTAL",    String.valueOf(cours.size()), BD, VL),
            kpi("📅 SÉANCES / SEM.", String.valueOf(creneauDAO.findByWeek(semaineActive).size()), BD, VL),
            kpi("🏫 SALLES LIBRES",  String.valueOf(libres), BD, VL),
            kpi("⚠️ CONFLITS",       String.valueOf(conflicts),
                    conflicts > 0 ? "#7f1d1d" : BD, conflicts > 0 ? RED : VL)
        );

        // 6 modules
        Label secLbl = lbl12("MODULES DE PLANIFICATION", "#94a3b8");
        secLbl.setPadding(new Insets(10, 0, 5, 0));

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20);
        grid.add(moduleCard("📚", "GESTION DES COURS",    "Créer, modifier et suivre les cours",       e -> showGestionCours()),     0, 0);
        grid.add(moduleCard("📅", "EMPLOI DU TEMPS",      "Calendrier hebdomadaire & créneaux",        e -> showCalendrier()),        1, 0);
        grid.add(moduleCard("🏫", "ASSIGNATION SALLES",   "Affecter et optimiser l'occupation",        e -> showAssignationSalles()), 0, 1);
        grid.add(moduleCard("⚠️", "RÉSOLUTION CONFLITS",  "Détecter et résoudre les chevauchements",  e -> showResolutionConflits()), 1, 1);
        grid.add(moduleCard("📊", "STATISTIQUES",         "Taux d'occupation et rapports",             e -> showStatistiques()),      0, 2);
        grid.add(moduleCard("📋", "RÉSERVATIONS",         "Historique et gestion des demandes",        e -> showReservations()),      1, 2);

        mainContent.getChildren().addAll(topBar, kpis, new Separator(), secLbl, grid);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. GESTION DES COURS
    // ══════════════════════════════════════════════════════════════════════════
    public void showGestionCours() {
        clear();
        mainContent.getChildren().add(navBar("GESTION DES COURS", e -> showDashboard()));

        HBox tb = new HBox(15); tb.setAlignment(Pos.CENTER_LEFT); tb.setPadding(new Insets(12, 0, 12, 0));
        TextField search = new TextField(); search.setPromptText("🔍 Rechercher un cours..."); search.setPrefWidth(300);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnAdd = actionBtn("+ NOUVEAU COURS", VL, BD, e -> dialogAjouterCours());
        tb.getChildren().addAll(search, sp, btnAdd);

        TableView<Cours> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(420);
        table.setPlaceholder(new Label("Aucun cours — cliquez '+ NOUVEAU COURS'"));

        TableColumn<Cours, String> cCode = tcol("CODE",     "codeCours",       100);
        TableColumn<Cours, String> cNom  = tcol("INTITULÉ", "intituleMatiere", 250);
        TableColumn<Cours, Double> cH    = new TableColumn<>("H. TOTAL");
        cH.setCellValueFactory(new PropertyValueFactory<>("nbrHeure")); cH.setPrefWidth(90);
        TableColumn<Cours, Double> cHE   = new TableColumn<>("H. EFFECTUÉES");
        cHE.setCellValueFactory(new PropertyValueFactory<>("heuresEffectuees")); cHE.setPrefWidth(110);

        TableColumn<Cours, Void> cProg = new TableColumn<>("AVANCEMENT");
        cProg.setPrefWidth(150);
        cProg.setCellFactory(p -> new TableCell<>() {
            final ProgressBar pb = new ProgressBar();
            { pb.setPrefWidth(130); }
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Cours c = getTableView().getItems().get(getIndex());
                double pct = c.getNbrHeure() > 0 ? c.getHeuresEffectuees() / c.getNbrHeure() : 0;
                pb.setProgress(pct);
                pb.setStyle("-fx-accent:" + (pct >= 1.0 ? VL : pct > 0.5 ? WARN : RED) + ";");
                setGraphic(pb);
            }
        });

        TableColumn<Cours, Void> cAct = actionCol3(
                c -> dialogModifierCours(c),
                c -> dialogSupprimerCours(c),
                c -> dialogAjouterCreneau(c));
        cAct.setPrefWidth(140);

        table.getColumns().addAll(cCode, cNom, cH, cHE, cProg, cAct);
        List<Cours> all = coursDAO.findAll();
        table.getItems().setAll(all);
        search.textProperty().addListener((o, ov, nv) -> {
            String f = nv.toLowerCase();
            table.getItems().setAll(all.stream()
                .filter(c -> (c.getCodeCours() != null && c.getCodeCours().toLowerCase().contains(f))
                          || c.getIntituleMatiere().toLowerCase().contains(f))
                .collect(Collectors.toList()));
        });

        mainContent.getChildren().addAll(tb, table);
    }

    private void dialogAjouterCours() {
        Stage dlg = modal("Nouveau Cours");
        VBox form = darkForm();
        form.getChildren().add(formTitle("📚 CRÉER UN COURS"));
        String fs = fs();
        TextField tCode   = tf(fs, "Code (ex: INFO301)");
        TextField tNom    = tf(fs, "Intitulé de la matière");
        TextField tHeures = tf(fs, "Nombre d'heures total");
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("CM", "TD", "TP", "Projet", "Séminaire");
        cbType.setValue("CM"); cbType.setMaxWidth(Double.MAX_VALUE); cbType.setStyle(fs);

        Button btnOk = actionBtn("✅ ENREGISTRER", VL, BD, e -> {
            if (tCode.getText().isEmpty() || tNom.getText().isEmpty() || tHeures.getText().isEmpty()) {
                popup("Champs manquants", "Remplissez tous les champs."); return;
            }
            try {
                Cours c = new Cours(tCode.getText().trim(), tNom.getText().trim(),
                        Double.parseDouble(tHeures.getText().trim()));
                coursDAO.save(c);
                log("Cours créé : " + c.getCodeCours());
                dlg.close(); showGestionCours();
                popup("Succès", "Cours « " + c.getIntituleMatiere() + " » enregistré.");
            } catch (NumberFormatException ex) { popup("Erreur", "Heures invalides."); }
            catch (Exception ex) { popup("Erreur BD", ex.getMessage()); }
        });

        form.getChildren().addAll(sep(), tCode, tNom, tHeures, cbType, btnOk);
        dlg.setScene(new Scene(form, 440, 400)); dlg.show(); tCode.requestFocus();
    }

    private void dialogModifierCours(Cours c) {
        Stage dlg = modal("Modifier — " + c.getCodeCours());
        VBox form = darkForm();
        form.getChildren().add(formTitle("✏️ MODIFIER LE COURS"));
        String fs = fs();
        TextField tNom  = tf(fs, ""); tNom.setText(c.getIntituleMatiere());
        TextField tH    = tf(fs, ""); tH.setText(String.valueOf(c.getNbrHeure()));
        TextField tHEff = tf(fs, ""); tHEff.setText(String.valueOf(c.getHeuresEffectuees()));
        Button btnOk = actionBtn("✅ SAUVEGARDER", VL, BD, e -> {
            try {
                c.setIntituleMatiere(tNom.getText().trim());
                c.setNbrHeure(Double.parseDouble(tH.getText().trim()));
                c.setHeuresEffectuees(Double.parseDouble(tHEff.getText().trim()));
                coursDAO.update(c);
                log("Cours modifié : " + c.getCodeCours());
                dlg.close(); showGestionCours();
            } catch (Exception ex) { popup("Erreur", ex.getMessage()); }
        });
        form.getChildren().addAll(sep(),
                smallLbl("INTITULÉ :"), tNom,
                smallLbl("HEURES TOTAL :"), tH,
                smallLbl("HEURES EFFECTUÉES :"), tHEff, btnOk);
        dlg.setScene(new Scene(form, 440, 400)); dlg.show();
    }

    private void dialogSupprimerCours(Cours c) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression cours");
        a.setHeaderText(c.getCodeCours() + " — " + c.getIntituleMatiere());
        a.setContentText("Supprimer ce cours et tous ses créneaux ?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try { coursDAO.delete(c); log("Cours supprimé : " + c.getCodeCours()); showGestionCours(); }
                catch (Exception ex) { popup("Erreur", ex.getMessage()); }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AJOUTER CRÉNEAU (détection conflit temps réel)
    // ══════════════════════════════════════════════════════════════════════════
    private void dialogAjouterCreneau(Cours cours) {
        Stage dlg = modal("Nouveau Créneau — " + cours.getCodeCours());
        VBox form = darkForm();
        form.getChildren().add(formTitle("📅 PLANIFIER UNE SÉANCE"));

        Label lCours = new Label("Cours : " + cours.getIntituleMatiere());
        lCours.setStyle("-fx-text-fill:white;-fx-font-size:12;");

        String fs = fs();
        DatePicker dp = new DatePicker(LocalDate.now());
        dp.setPrefWidth(Double.MAX_VALUE);
        dp.setStyle("-fx-background-color:#1a252f;-fx-text-fill:white;");

        ComboBox<String> cbDeb = comboHeure(fs, "08:00");
        ComboBox<String> cbFin = comboHeure(fs, "10:00");

        ComboBox<Salle> cbSalle = new ComboBox<>();
        cbSalle.getItems().addAll(salleDAO.findAll());
        cbSalle.setMaxWidth(Double.MAX_VALUE); cbSalle.setStyle(fs);
        cbSalle.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Salle s) {
                return s == null ? "" : s.getNumeroSalle() + " (cap. " + s.getCapacite() + ") — " + nvl(s.getEtatSalle());
            }
            public Salle fromString(String x) { return null; }
        });

        Label lblCf = new Label();
        lblCf.setStyle("-fx-text-fill:#ff4d4d;-fx-font-size:11;");
        lblCf.setVisible(false); lblCf.setManaged(false);

        Runnable check = () -> {
            if (dp.getValue() != null && cbSalle.getValue() != null
                    && cbDeb.getValue() != null && cbFin.getValue() != null) {
                LocalTime debut = LocalTime.parse(cbDeb.getValue());
                LocalTime fin   = LocalTime.parse(cbFin.getValue());
                boolean cf = creneauDAO.findBySalleAndDate(cbSalle.getValue().getNumeroSalle(), dp.getValue())
                        .stream().anyMatch(x -> debut.isBefore(x.getHeureFin()) && x.getHeureDebut().isBefore(fin));
                lblCf.setText("⚠️  CONFLIT : salle déjà occupée sur ce créneau !");
                lblCf.setVisible(cf); lblCf.setManaged(cf);
            }
        };
        dp.valueProperty().addListener((o, ov, nv) -> check.run());
        cbSalle.valueProperty().addListener((o, ov, nv) -> check.run());
        cbDeb.valueProperty().addListener((o, ov, nv) -> check.run());
        cbFin.valueProperty().addListener((o, ov, nv) -> check.run());

        Button btnOk = actionBtn("✅ CRÉER LE CRÉNEAU", VL, BD, e -> {
            if (dp.getValue() == null || cbSalle.getValue() == null) {
                popup("Manquant", "Date et salle requises."); return;
            }
            LocalTime debut = LocalTime.parse(cbDeb.getValue());
            LocalTime fin   = LocalTime.parse(cbFin.getValue());
            if (!fin.isAfter(debut)) { popup("Heure invalide", "La fin doit être après le début."); return; }

            boolean cf = creneauDAO.findBySalleAndDate(cbSalle.getValue().getNumeroSalle(), dp.getValue())
                    .stream().anyMatch(x -> debut.isBefore(x.getHeureFin()) && x.getHeureDebut().isBefore(fin));
            if (cf) {
                Alert ca = new Alert(Alert.AlertType.CONFIRMATION);
                ca.setTitle("Conflit"); ca.setHeaderText("Salle déjà occupée.");
                ca.setContentText("Forcer l'enregistrement quand même ?");
                Optional<ButtonType> res = ca.showAndWait();
                if (res.isEmpty() || res.get() != ButtonType.OK) return;
                notifSvc.notifierConflit("Conflit forcé : " + cbSalle.getValue().getNumeroSalle() + " le " + dp.getValue());
            }
            try {
                String id = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Creneau cr = new Creneau(id, dp.getValue(), debut, fin);
                cr.setCours(cours); cr.setSalle(cbSalle.getValue());
                creneauDAO.save(cr);
                if (dp.getValue().equals(LocalDate.now())) {
                    cbSalle.getValue().setEtatSalle("Occupée"); salleDAO.update(cbSalle.getValue());
                }
                log("Créneau créé : " + cours.getCodeCours() + " → " + cbSalle.getValue().getNumeroSalle());
                dlg.close(); showGestionCours();
                popup("Planifié", "Séance enregistrée.");
            } catch (Exception ex) { popup("Erreur BD", ex.getMessage()); }
        });

        form.getChildren().addAll(sep(), lCours,
                smallLbl("DATE :"), dp,
                smallLbl("HEURE DÉBUT :"), cbDeb,
                smallLbl("HEURE FIN :"), cbFin,
                smallLbl("SALLE :"), cbSalle,
                lblCf, btnOk);
        dlg.setScene(new Scene(form, 500, 530)); dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. CALENDRIER HEBDOMADAIRE
    // ══════════════════════════════════════════════════════════════════════════
    public void showCalendrier() {
        clear();
        mainContent.getChildren().add(navBar("EMPLOI DU TEMPS — SEMAINE", e -> showDashboard()));

        DateTimeFormatter wfmt = DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
        HBox weekNav = new HBox(15); weekNav.setAlignment(Pos.CENTER_LEFT); weekNav.setPadding(new Insets(10, 0, 10, 0));
        Button bPrev  = actionBtn("◀ Préc.", BD, VL,  e -> { semaineActive = semaineActive.minusWeeks(1); showCalendrier(); });
        Button bNext  = actionBtn("Suiv. ▶", BD, VL,  e -> { semaineActive = semaineActive.plusWeeks(1);  showCalendrier(); });
        Button bToday = actionBtn("Aujourd'hui", "#475569", "white", e -> {
            semaineActive = LocalDate.now().with(WeekFields.of(Locale.FRENCH).dayOfWeek(), 1); showCalendrier();
        });
        Label lblSem = lbl26("Semaine du " + semaineActive.format(wfmt)
                + " au " + semaineActive.plusDays(6).format(wfmt), BD);
        lblSem.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + BD + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button bAdd = actionBtn("+ PLANIFIER", VL, BD, e -> choisirEtPlanifier(LocalDate.now(), LocalTime.of(8, 0)));
        weekNav.getChildren().addAll(bPrev, bToday, bNext, sp, lblSem, sp, bAdd);

        ScrollPane scroll = new ScrollPane(buildGrid());
        scroll.setFitToWidth(true); scroll.setPrefHeight(520);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:#f8fafc;");

        mainContent.getChildren().addAll(weekNav, scroll);
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(3); grid.setVgap(3);
        grid.setStyle("-fx-background-color:" + BG + ";-fx-padding:10;");

        String[] JOURS = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM"};
        DateTimeFormatter dfmt = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter tfmt = DateTimeFormatter.ofPattern("HH:mm");

        // Entête colonnes
        grid.add(headCell("HEURE", 80), 0, 0);
        for (int j = 0; j < 6; j++) {
            LocalDate jour  = semaineActive.plusDays(j);
            boolean   today = jour.equals(LocalDate.now());
            Label lj = new Label(JOURS[j] + "\n" + jour.format(dfmt));
            lj.setAlignment(Pos.CENTER); lj.setMaxWidth(Double.MAX_VALUE);
            lj.setStyle("-fx-background-color:" + (today ? VL : BD) + ";"
                    + "-fx-text-fill:" + (today ? BD : VL) + ";"
                    + "-fx-font-weight:bold;-fx-font-size:11;"
                    + "-fx-padding:8;-fx-background-radius:5;-fx-font-family:'Consolas';");
            GridPane.setHgrow(lj, Priority.ALWAYS);
            grid.add(lj, j + 1, 0);
        }

        List<Creneau> semaine = creneauDAO.findByWeek(semaineActive);

        for (int h = 7; h <= 20; h++) {
            Label lh = new Label(String.format("%02d:00", h));
            lh.setStyle("-fx-text-fill:#64748b;-fx-font-size:11;-fx-padding:6 8;-fx-font-family:'Consolas';");
            grid.add(lh, 0, h - 6);

            for (int j = 0; j < 6; j++) {
                LocalDate jour     = semaineActive.plusDays(j);
                LocalTime slotDeb  = LocalTime.of(h, 0);
                LocalTime slotFin  = LocalTime.of(h + 1, 0);

                List<Creneau> matching = semaine.stream()
                    .filter(cr -> cr.getDateSeance() != null && cr.getDateSeance().equals(jour)
                               && cr.getHeureDebut() != null && !cr.getHeureDebut().isAfter(slotDeb)
                               && cr.getHeureFin()   != null && cr.getHeureFin().isAfter(slotDeb))
                    .collect(Collectors.toList());

                if (!matching.isEmpty()) {
                    Creneau cr    = matching.get(0);
                    String  cours = cr.getCours() != null ? cr.getCours().getIntituleMatiere() : "?";
                    String  salle = cr.getSalle() != null ? cr.getSalle().getNumeroSalle() : "?";
                    boolean first = cr.getHeureDebut().equals(slotDeb)
                        || (cr.getHeureDebut().isAfter(LocalTime.of(h - 1, 59))
                         && cr.getHeureDebut().isBefore(slotFin));

                    if (first) {
                        VBox cell = new VBox(2); cell.setPadding(new Insets(5));
                        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        String cs = "-fx-background-color:" + VL + "22;-fx-border-color:" + VL
                                + ";-fx-border-radius:5;-fx-background-radius:5;-fx-border-width:1.5;-fx-cursor:hand;";
                        cell.setStyle(cs);
                        Label lC = new Label(cours); lC.setStyle("-fx-font-weight:bold;-fx-font-size:10;-fx-text-fill:" + BD + ";");
                        Label lS = new Label("🏫 " + salle); lS.setStyle("-fx-font-size:9;-fx-text-fill:#475569;");
                        Label lT = new Label(cr.getHeureDebut().format(tfmt) + "→" + cr.getHeureFin().format(tfmt));
                        lT.setStyle("-fx-font-size:9;-fx-text-fill:#64748b;");
                        cell.getChildren().addAll(lC, lS, lT);
                        final Creneau crF = cr;
                        cell.setOnMouseClicked(evt -> dialogOptionsCreneau(crF));
                        cell.setOnMouseEntered(evt -> cell.setStyle("-fx-background-color:" + VL + "44;-fx-border-color:" + VL + ";-fx-border-radius:5;-fx-background-radius:5;-fx-border-width:2;-fx-cursor:hand;"));
                        cell.setOnMouseExited (evt -> cell.setStyle(cs));
                        grid.add(cell, j + 1, h - 6);
                    } else {
                        Region cont = new Region();
                        cont.setStyle("-fx-background-color:" + VL + "11;-fx-border-color:" + VL + "33;-fx-border-width:0 1.5 0 1.5;");
                        grid.add(cont, j + 1, h - 6);
                    }
                } else {
                    Region empty = new Region(); empty.setPrefHeight(45); empty.setMaxWidth(Double.MAX_VALUE);
                    String es = "-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;-fx-border-width:0.5;-fx-cursor:hand;";
                    empty.setStyle(es);
                    final LocalDate jF = jour; final int hF = h;
                    empty.setOnMouseEntered(evt -> empty.setStyle("-fx-background-color:" + VL + "11;-fx-border-color:" + VL + "55;-fx-border-width:1;-fx-cursor:hand;"));
                    empty.setOnMouseExited (evt -> empty.setStyle(es));
                    empty.setOnMouseClicked(evt -> choisirEtPlanifier(jF, LocalTime.of(hF, 0)));
                    grid.add(empty, j + 1, h - 6);
                }
                GridPane.setHgrow(grid.getChildren().get(grid.getChildren().size() - 1), Priority.ALWAYS);
            }
        }
        return grid;
    }

    private void dialogOptionsCreneau(Creneau cr) {
        Stage dlg = modal("Options Créneau");
        VBox form = darkForm();
        String cNom  = cr.getCours() != null ? cr.getCours().getIntituleMatiere() : "?";
        String sNom  = cr.getSalle() != null ? cr.getSalle().getNumeroSalle() : "?";
        form.getChildren().addAll(
            formTitle("📅 " + cNom),
            smallLbl("Salle : " + sNom + "  |  " + cr.getDateSeance()
                    + "  " + cr.getHeureDebut() + " → " + cr.getHeureFin()),
            sep(),
            actionBtn("🗑️ SUPPRIMER CE CRÉNEAU", RED, "white", e -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setTitle("Supprimer"); a.setContentText("Supprimer ce créneau ?");
                a.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        try { creneauDAO.delete(cr); dlg.close(); showCalendrier(); }
                        catch (Exception ex) { popup("Erreur", ex.getMessage()); }
                    }
                });
            }),
            actionBtn("Fermer", "#475569", "white", e -> dlg.close())
        );
        dlg.setScene(new Scene(form, 400, 280)); dlg.show();
    }

    private void choisirEtPlanifier(LocalDate date, LocalTime debut) {
        List<Cours> cours = coursDAO.findAll();
        if (cours.isEmpty()) { popup("Aucun cours", "Créez d'abord un cours."); return; }
        Stage dlg = modal("Choisir le cours");
        VBox form = darkForm();
        form.getChildren().add(formTitle("📚 QUEL COURS PLANIFIER ?"));
        ComboBox<Cours> cb = new ComboBox<>();
        cb.getItems().addAll(cours); cb.setMaxWidth(Double.MAX_VALUE); cb.setStyle(fs());
        cb.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Cours c) { return c == null ? "" : c.getCodeCours() + " — " + c.getIntituleMatiere(); }
            public Cours fromString(String s) { return null; }
        });
        if (!cours.isEmpty()) cb.setValue(cours.get(0));
        Button btnOk = actionBtn("CONTINUER →", VL, BD, e -> { if (cb.getValue() != null) { dlg.close(); dialogAjouterCreneau(cb.getValue()); } });
        form.getChildren().addAll(sep(), cb, btnOk);
        dlg.setScene(new Scene(form, 380, 250)); dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. ASSIGNATION IA
    // ══════════════════════════════════════════════════════════════════════════
    public void showAssignationSalles() {
        clear();
        mainContent.getChildren().add(navBar("ASSIGNATION INTELLIGENTE DES SALLES", e -> showDashboard()));

        VBox console = new VBox(16); console.setPadding(new Insets(20));
        console.setStyle("-fx-background-color:" + BD + ";-fx-background-radius:18;");
        Label titre = lbl26("🤖 MOTEUR D'ASSIGNATION IA", VL);
        Label sub   = lbl12("Entrez les critères — le système propose les meilleures salles disponibles.", "#94a3b8");

        String fs = fs();
        DatePicker dp = new DatePicker(LocalDate.now()); dp.setPrefWidth(200); dp.setStyle("-fx-background-color:#0d1117;-fx-text-fill:white;");
        ComboBox<String> cbDeb = comboHeure(fs, "08:00"); cbDeb.setPrefWidth(120);
        ComboBox<String> cbFin = comboHeure(fs, "10:00"); cbFin.setPrefWidth(120);
        TextField tEff = new TextField(); tEff.setPromptText("Effectif prévu"); tEff.setStyle(fs); tEff.setPrefWidth(130);

        HBox row = new HBox(20); row.setAlignment(Pos.BOTTOM_LEFT);
        row.getChildren().addAll(
            grpSaisie("📅 DATE", dp, 200),
            grpSaisie("🕗 DÉBUT", cbDeb, 120),
            grpSaisie("🕙 FIN", cbFin, 120),
            grpSaisie("👥 EFFECTIF", tEff, 130)
        );
        Button btnScan = actionBtn("🔍 LANCER L'ANALYSE IA", VL, BD, null);
        btnScan.setPrefHeight(44);
        console.getChildren().addAll(titre, sub, row, btnScan);

        FlowPane results = new FlowPane(20, 20); results.setPadding(new Insets(20, 0, 10, 0));
        btnScan.setOnAction(e -> {
            results.getChildren().clear();
            try {
                int eff = Integer.parseInt(tEff.getText().trim());
                LocalDate date  = dp.getValue();
                LocalTime debut = LocalTime.parse(cbDeb.getValue());
                LocalTime fin   = LocalTime.parse(cbFin.getValue());
                List<Salle> cands = salleDAO.findAll().stream()
                    .filter(s -> s.getCapacite() >= eff)
                    .sorted(Comparator.comparingInt(Salle::getCapacite))
                    .collect(Collectors.toList());

                if (cands.isEmpty()) {
                    results.getChildren().add(lbl12("❌ Aucune salle avec capacité suffisante.", RED));
                } else {
                    for (Salle s : cands) {
                        boolean libre = creneauDAO.findBySalleAndDate(s.getNumeroSalle(), date)
                            .stream().noneMatch(cr -> debut.isBefore(cr.getHeureFin()) && cr.getHeureDebut().isBefore(fin));
                        double score = libre ? Math.min(100, 100 - (s.getCapacite() - eff) * 2) : 0;
                        results.getChildren().add(carteAssignation(s, score, libre, ev -> {
                            if (!libre) { popup("Conflit", "Salle déjà réservée sur ce créneau."); return; }
                            dialogAssigner(s, date, debut, fin);
                        }));
                    }
                }
            } catch (NumberFormatException ex) { popup("Erreur", "Entrez un effectif valide."); }
        });

        ScrollPane scroll = new ScrollPane(results);
        scroll.setFitToWidth(true); scroll.setPrefHeight(380);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        mainContent.getChildren().addAll(console, scroll);
    }

    private VBox carteAssignation(Salle s, double score, boolean libre,
            EventHandler<javafx.scene.input.MouseEvent> action) {
        VBox card = new VBox(12); card.setPadding(new Insets(18)); card.setPrefWidth(250);
        card.setStyle("-fx-background-color:" + CARD + ";-fx-background-radius:15;"
                + "-fx-border-color:" + (libre ? VL : RED) + ";-fx-border-width:2;-fx-border-radius:15;"
                + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.1),8,0,0,0);");
        Label lN = lbl26(s.getNumeroSalle(), BD); lN.setStyle("-fx-font-size:20;-fx-font-weight:bold;-fx-text-fill:" + BD + ";");
        Label lC = lbl12("👥 " + s.getCapacite() + " places", "#475569");
        Label lE = lbl12(libre ? "✅ LIBRE" : "🔴 OCCUPÉE", libre ? "#059669" : RED);
        lE.setStyle("-fx-font-size:11;-fx-font-weight:bold;-fx-text-fill:" + (libre ? "#059669" : RED) + ";");
        Label lSc = new Label((int) score + "% MATCH");
        lSc.setPadding(new Insets(4, 10, 4, 10));
        lSc.setStyle("-fx-background-color:" + (score > 80 ? VL : score > 50 ? WARN : RED) + ";"
                + "-fx-text-fill:" + BD + ";-fx-font-weight:bold;-fx-background-radius:8;-fx-font-size:11;");
        Button btn = new Button(libre ? "ASSIGNER" : "FORCER");
        btn.setMaxWidth(Double.MAX_VALUE); btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setStyle("-fx-background-color:" + (libre ? BD : "#7f1d1d") + ";-fx-text-fill:" + (libre ? VL : "white") + ";-fx-font-weight:bold;-fx-background-radius:8;");
        btn.setOnMouseClicked(action);
        card.getChildren().addAll(lN, lC, lE, lSc, btn);
        return card;
    }

    private void dialogAssigner(Salle salle, LocalDate date, LocalTime debut, LocalTime fin) {
        List<Cours> cours = coursDAO.findAll();
        if (cours.isEmpty()) { popup("Aucun cours", "Créez d'abord un cours."); return; }
        Stage dlg = modal("Assigner — " + salle.getNumeroSalle());
        VBox form = darkForm();
        form.getChildren().add(formTitle("📚 ASSIGNER « " + salle.getNumeroSalle() + " »"));
        ComboBox<Cours> cb = new ComboBox<>();
        cb.getItems().addAll(cours); cb.setMaxWidth(Double.MAX_VALUE); cb.setStyle(fs());
        cb.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Cours c) { return c == null ? "" : c.getCodeCours() + " — " + c.getIntituleMatiere(); }
            public Cours fromString(String s) { return null; }
        });
        if (!cours.isEmpty()) cb.setValue(cours.get(0));
        Button btnOk = actionBtn("✅ CONFIRMER", VL, BD, e -> {
            if (cb.getValue() == null) return;
            try {
                String id = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Creneau cr = new Creneau(id, date, debut, fin);
                cr.setCours(cb.getValue()); cr.setSalle(salle);
                creneauDAO.save(cr);
                if (date.equals(LocalDate.now())) { salle.setEtatSalle("Occupée"); salleDAO.update(salle); }
                log("Salle " + salle.getNumeroSalle() + " assignée à " + cb.getValue().getCodeCours());
                dlg.close(); showAssignationSalles();
                popup("Succès", "Salle " + salle.getNumeroSalle() + " assignée.");
            } catch (Exception ex) { popup("Erreur", ex.getMessage()); }
        });
        form.getChildren().addAll(sep(), cb, btnOk);
        dlg.setScene(new Scene(form, 380, 260)); dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. RÉSOLUTION DES CONFLITS
    // ══════════════════════════════════════════════════════════════════════════
    public void showResolutionConflits() {
        clear();
        mainContent.getChildren().add(navBar("RÉSOLUTION DES CONFLITS", e -> showDashboard()));

        List<Creneau> semaineCreneaux = creneauDAO.findByWeek(semaineActive);
        List<String[]> conflits = detecterConflits(semaineCreneaux);

        HBox banner = new HBox(15); banner.setAlignment(Pos.CENTER_LEFT); banner.setPadding(new Insets(18));
        banner.setStyle("-fx-background-color:" + (conflits.isEmpty() ? "#064e3b" : "#7f1d1d")
                + ";-fx-background-radius:14;-fx-border-color:" + (conflits.isEmpty() ? "#059669" : RED)
                + ";-fx-border-width:2;-fx-border-radius:14;");
        banner.getChildren().add(lbl26(conflits.isEmpty()
                ? "✅  AUCUN CONFLIT DÉTECTÉ CETTE SEMAINE"
                : "⚠️  " + conflits.size() + " CONFLIT(S) CETTE SEMAINE",
                conflits.isEmpty() ? "#6ee7b7" : "#fca5a5"));
        mainContent.getChildren().add(banner);

        if (!conflits.isEmpty()) {
            Label sec = lbl12("DÉTAIL DES CONFLITS", "#94a3b8");
            sec.setPadding(new Insets(15, 0, 5, 0));
            mainContent.getChildren().add(sec);
            for (String[] conf : conflits) {
                HBox row = new HBox(15); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(14));
                row.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#fee2e2;-fx-border-width:1.5;-fx-border-radius:12;");
                VBox info = new VBox(4);
                info.getChildren().addAll(
                    lbl26("🏫 Salle : " + conf[0], BD),
                    lbl12("📅 " + conf[1] + "  |  " + conf[2] + " → " + conf[3], "#475569"),
                    lbl12("⚡ " + conf[4], RED)
                );
                Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
                Button btnRes = actionBtn("RÉSOUDRE", WARN, BD, e -> dialogResoudreConflit(conf[0], conf[1]));
                row.getChildren().addAll(info, sp2, btnRes);
                mainContent.getChildren().add(row);
            }
        } else {
            Label sec = lbl12("CRÉNEAUX DE LA SEMAINE (" + semaineCreneaux.size() + ")", "#94a3b8");
            sec.setPadding(new Insets(15, 0, 5, 0));
            TableView<Creneau> t = new TableView<>();
            t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            t.setPrefHeight(380);
            TableColumn<Creneau, String> cD = new TableColumn<>("DATE");
            cD.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDateSeance() + ""));
            TableColumn<Creneau, String> cH = new TableColumn<>("HORAIRE");
            cH.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getHeureDebut() + " → " + cd.getValue().getHeureFin()));
            TableColumn<Creneau, String> cC = new TableColumn<>("COURS");
            cC.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCours() != null ? cd.getValue().getCours().getIntituleMatiere() : "?"));
            TableColumn<Creneau, String> cS = new TableColumn<>("SALLE");
            cS.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getSalle() != null ? cd.getValue().getSalle().getNumeroSalle() : "?"));
            t.getColumns().addAll(cD, cH, cC, cS);
            t.getItems().setAll(semaineCreneaux);
            mainContent.getChildren().addAll(sec, t);
        }
    }

    private List<String[]> detecterConflits(List<Creneau> creneaux) {
        List<String[]> conflits = new ArrayList<>();
        Map<String, List<Creneau>> parSalle = creneaux.stream()
            .filter(c -> c.getSalle() != null)
            .collect(Collectors.groupingBy(c -> c.getSalle().getNumeroSalle()));
        for (Map.Entry<String, List<Creneau>> entry : parSalle.entrySet()) {
            String salle = entry.getKey();
            List<Creneau> liste = entry.getValue();
            for (int i = 0; i < liste.size(); i++) {
                for (int j = i + 1; j < liste.size(); j++) {
                    Creneau a = liste.get(i), b = liste.get(j);
                    if (a.getDateSeance() != null && a.getDateSeance().equals(b.getDateSeance())
                            && a.getHeureDebut() != null && b.getHeureDebut() != null
                            && a.getHeureDebut().isBefore(b.getHeureFin())
                            && b.getHeureDebut().isBefore(a.getHeureFin())) {
                        String desc = (a.getCours() != null ? a.getCours().getIntituleMatiere() : "?")
                                + " ↔ " + (b.getCours() != null ? b.getCours().getIntituleMatiere() : "?");
                        conflits.add(new String[]{salle, a.getDateSeance() + "", a.getHeureDebut() + "", a.getHeureFin() + "", desc});
                    }
                }
            }
        }
        return conflits;
    }

    private void dialogResoudreConflit(String salle, String date) {
        Stage dlg = modal("Résoudre — Salle " + salle);
        VBox form = darkForm();
        form.getChildren().addAll(formTitle("⚠️ RÉSOLUTION SALLE " + salle + " | " + date), sep(),
            actionBtn("🔓 LIBÉRER TOUS LES CRÉNEAUX SUR CETTE SALLE", WARN, BD, e -> {
                creneauDAO.findBySalleAndDate(salle, LocalDate.parse(date))
                    .forEach(c -> { c.setSalle(null); creneauDAO.update(c); });
                notifSvc.notifierConflit("Salle " + salle + " libérée — conflit le " + date);
                log("Conflit résolu : salle " + salle + " libérée le " + date);
                dlg.close(); showResolutionConflits();
                popup("Résolu", "Créneaux détachés de la salle.");
            }),
            actionBtn("📧 NOTIFIER LES ENSEIGNANTS", BD, VL, e -> {
                notifSvc.notifierConflit("CONFLIT salle " + salle + " le " + date + ". Vérification requise.");
                log("Notification envoyée pour conflit salle " + salle);
                dlg.close(); popup("Notifié", "Notifications enregistrées.");
            }),
            actionBtn("Annuler", "#475569", "white", e -> dlg.close())
        );
        dlg.setScene(new Scene(form, 430, 300)); dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. STATISTIQUES
    // ══════════════════════════════════════════════════════════════════════════
    public void showStatistiques() {
        clear();
        mainContent.getChildren().add(navBar("STATISTIQUES D'OCCUPATION", e -> showDashboard()));

        List<Salle>   salles   = salleDAO.findAll();
        List<Creneau> creneaux = creneauDAO.findAll();
        Map<String, Long> countBySalle = creneaux.stream()
            .filter(c -> c.getSalle() != null)
            .collect(Collectors.groupingBy(c -> c.getSalle().getNumeroSalle(), Collectors.counting()));

        // BarChart
        CategoryAxis xA = new CategoryAxis(); NumberAxis yA = new NumberAxis();
        xA.setLabel("Salles"); yA.setLabel("Nb séances");
        BarChart<String, Number> bar = new BarChart<>(xA, yA);
        bar.setTitle("SÉANCES PAR SALLE"); bar.setPrefHeight(300);
        bar.setLegendVisible(false); bar.setStyle("-fx-background-color:transparent;");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        salles.forEach(s -> series.getData().add(
            new XYChart.Data<>(s.getNumeroSalle(), countBySalle.getOrDefault(s.getNumeroSalle(), 0L))));
        bar.getData().add(series);
        javafx.application.Platform.runLater(() ->
            series.getData().forEach(d -> { if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill:" + VL + ";"); }));

        // PieChart
        javafx.scene.chart.PieChart pie = new javafx.scene.chart.PieChart();
        pie.setTitle("ÉTAT DES SALLES");
        long libres = salles.stream().filter(s -> "Disponible".equalsIgnoreCase(s.getEtatSalle())).count();
        pie.setData(FXCollections.observableArrayList(
            new javafx.scene.chart.PieChart.Data("Libres", libres),
            new javafx.scene.chart.PieChart.Data("Occupées", salles.size() - libres)));
        pie.setPrefSize(360, 300); pie.setLegendVisible(true);

        HBox charts = new HBox(30); charts.setAlignment(Pos.CENTER);
        charts.getChildren().addAll(bar, pie);

        // Tableau récap
        Label tabTitre = lbl12("RÉCAPITULATIF PAR SALLE", "#94a3b8");
        tabTitre.setPadding(new Insets(15, 0, 5, 0));
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(240);
        TableColumn<String[], String> cS = new TableColumn<>("SALLE");     cS.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[0]));
        TableColumn<String[], String> cB = new TableColumn<>("BÂTIMENT"); cB.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[1]));
        TableColumn<String[], String> cC = new TableColumn<>("CAPACITÉ"); cC.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[2]));
        TableColumn<String[], String> cN = new TableColumn<>("SÉANCES");  cN.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[3]));
        TableColumn<String[], String> cT = new TableColumn<>("TAUX");     cT.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[4]));
        table.getColumns().addAll(cS, cB, cC, cN, cT);
        salles.forEach(s -> {
            long nb  = countBySalle.getOrDefault(s.getNumeroSalle(), 0L);
            String bat = s.getBatiment() != null ? s.getBatiment().getNomBatiment() : "—";
            table.getItems().add(new String[]{s.getNumeroSalle(), bat, String.valueOf(s.getCapacite()),
                    String.valueOf(nb), String.format("%.1f%%", nb * 100.0 / 70)});
        });

        mainContent.getChildren().addAll(charts, tabTitre, table);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. RÉSERVATIONS
    // ══════════════════════════════════════════════════════════════════════════
    public void showReservations() {
        clear();
        mainContent.getChildren().add(navBar("HISTORIQUE DES RÉSERVATIONS", e -> showDashboard()));

        HBox filters = new HBox(15); filters.setAlignment(Pos.CENTER_LEFT); filters.setPadding(new Insets(10, 0, 10, 0));
        ComboBox<String> cbEtat = new ComboBox<>();
        cbEtat.getItems().addAll("Tous", "En attente", "Validée", "Annulée");
        cbEtat.setValue("Tous");
        Button btnFilt = actionBtn("Filtrer", BD, VL, null);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnNew = actionBtn("+ NOUVELLE RÉSERVATION", VL, BD, e -> dialogNouvelleReservation());
        filters.getChildren().addAll(new Label("État :"), cbEtat, btnFilt, sp, btnNew);

        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(430);
        table.setPlaceholder(new Label("Aucune réservation."));

        TableColumn<Reservation, String> cN = new TableColumn<>("N°");
        cN.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getNumReservation()))); cN.setPrefWidth(55);
        TableColumn<Reservation, String> cD = new TableColumn<>("DATE");
        cD.setCellValueFactory(cd -> { Creneau cr = cd.getValue().getMonCreneau(); return new SimpleStringProperty(cr != null ? cr.getDateSeance() + "" : "?"); });
        TableColumn<Reservation, String> cCr = new TableColumn<>("CRÉNEAU");
        cCr.setCellValueFactory(cd -> { Creneau cr = cd.getValue().getMonCreneau(); return new SimpleStringProperty(cr != null ? cr.getHeureDebut() + " → " + cr.getHeureFin() : "?"); });
        TableColumn<Reservation, String> cSa = new TableColumn<>("SALLE");
        cSa.setCellValueFactory(cd -> { Creneau cr = cd.getValue().getMonCreneau(); return new SimpleStringProperty(cr != null && cr.getSalle() != null ? cr.getSalle().getNumeroSalle() : "?"); });
        TableColumn<Reservation, String> cE = new TableColumn<>("ÉTAT");
        cE.setCellValueFactory(new PropertyValueFactory<>("etatReservation"));
        cE.setCellFactory(p -> new TableCell<>() {
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle("-fx-text-fill:" + ("Validée".equals(v) ? "#059669" : "Annulée".equals(v) ? RED : WARN) + ";-fx-font-weight:bold;");
            }
        });
        TableColumn<Reservation, Void> cAct = new TableColumn<>("ACTIONS");
        cAct.setPrefWidth(140);
        cAct.setCellFactory(p -> new TableCell<>() {
            final Button bV = actionBtn("✅", "#064e3b", "#6ee7b7", null);
            final Button bA = actionBtn("❌", "#7f1d1d", "#fca5a5", null);
            final HBox   box = new HBox(10, bV, bA);
            { box.setAlignment(Pos.CENTER);
              bV.setOnAction(e -> { Reservation r = getTableView().getItems().get(getIndex()); r.validerReservation(); reservDAO.update(r); showReservations(); });
              bA.setOnAction(e -> { Reservation r = getTableView().getItems().get(getIndex()); r.annulerReservation(); reservDAO.update(r); showReservations(); });
            }
            protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : box); }
        });
        table.getColumns().addAll(cN, cD, cCr, cSa, cE, cAct);
        List<Reservation> all = reservDAO.findAll();
        table.getItems().setAll(all);
        btnFilt.setOnAction(e -> {
            String f = cbEtat.getValue();
            table.getItems().setAll("Tous".equals(f) ? all
                : all.stream().filter(r -> f.equals(r.getEtatReservation())).collect(Collectors.toList()));
        });
        mainContent.getChildren().addAll(filters, table);
    }

    private void dialogNouvelleReservation() {
        Stage dlg = modal("Nouvelle Réservation");
        VBox form = darkForm();
        form.getChildren().add(formTitle("📋 CRÉER UNE RÉSERVATION"));
        String fs = fs();
        DatePicker dp = new DatePicker(LocalDate.now()); dp.setPrefWidth(Double.MAX_VALUE); dp.setStyle("-fx-background-color:#0d1117;-fx-text-fill:white;");
        ComboBox<String> cbDeb = comboHeure(fs, "08:00");
        ComboBox<String> cbFin = comboHeure(fs, "10:00");
        ComboBox<Salle>  cbS   = new ComboBox<>();
        cbS.getItems().addAll(salleDAO.findAll()); cbS.setMaxWidth(Double.MAX_VALUE); cbS.setStyle(fs);
        cbS.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Salle s) { return s == null ? "" : s.getNumeroSalle() + " (" + s.getCapacite() + " places)"; }
            public Salle fromString(String x) { return null; }
        });
        ComboBox<String> cbNat = new ComboBox<>();
        cbNat.getItems().addAll("Cours", "TD", "TP", "Examen", "Soutenance", "Réunion");
        cbNat.setValue("Cours"); cbNat.setMaxWidth(Double.MAX_VALUE); cbNat.setStyle(fs);
        TextField tMotif = tf(fs, "Motif (ex: Soutenance…)");

        Button btnOk = actionBtn("✅ CRÉER LA RÉSERVATION", VL, BD, e -> {
            if (dp.getValue() == null || cbS.getValue() == null) { popup("Manquant", "Date et salle requises."); return; }
            try {
                String id = "CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Creneau cr = new Creneau(id, dp.getValue(),
                        LocalTime.parse(cbDeb.getValue()), LocalTime.parse(cbFin.getValue()));
                cr.setSalle(cbS.getValue()); creneauDAO.save(cr);
                Reservation r = new Reservation(0, cr, cbNat.getValue(), 1);
                r.setMotifReservation(tMotif.getText()); r.validerReservation();
                reservDAO.save(r);
                log("Réservation créée : " + cbS.getValue().getNumeroSalle() + " le " + dp.getValue());
                dlg.close(); showReservations();
            } catch (Exception ex) { popup("Erreur", ex.getMessage()); }
        });

        form.getChildren().addAll(sep(),
            smallLbl("DATE :"), dp, smallLbl("DÉBUT :"), cbDeb, smallLbl("FIN :"), cbFin,
            smallLbl("SALLE :"), cbS, smallLbl("NATURE :"), cbNat, smallLbl("MOTIF :"), tMotif, btnOk);
        dlg.setScene(new Scene(form, 500, 520)); dlg.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS UI — types corrects, aucune ambiguïté de package
    // ══════════════════════════════════════════════════════════════════════════

    /** Bouton avec EventHandler<ActionEvent> — le seul type utilisé pour setOnAction */
    private Button actionBtn(String text, String bg, String fg, EventHandler<ActionEvent> handler) {
        Button b = new Button(text);
        b.setCursor(javafx.scene.Cursor.HAND);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg
                + ";-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 18;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited (e -> b.setOpacity(1.0));
        if (handler != null) b.setOnAction(handler);
        return b;
    }

    /** Barre de navigation avec bouton Retour (utilise ActionEvent via setOnAction) */
    private HBox navBar(String titre, EventHandler<ActionEvent> onBack) {
        HBox nav = new HBox(15); nav.setAlignment(Pos.CENTER_LEFT); nav.setPadding(new Insets(0, 0, 20, 0));
        Button btnBack = new Button("← TABLEAU DE BORD");
        btnBack.setStyle("-fx-background-color:transparent;-fx-text-fill:#64748b;-fx-font-weight:bold;-fx-cursor:hand;");
        btnBack.setOnAction(onBack);
        Label l = lbl26(titre, BD); l.setStyle("-fx-font-size:22;-fx-font-weight:bold;-fx-text-fill:" + BD + ";");
        VBox box = new VBox(4, l, accentBar(80));
        nav.getChildren().addAll(btnBack, new Label(" / "), box);
        return nav;
    }

    private Stage modal(String title) {
        Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.setTitle(title); s.setResizable(false);
        return s;
    }

    private VBox darkForm() {
        VBox f = new VBox(12); f.setPadding(new Insets(28));
        f.setStyle("-fx-background-color:" + BD + ";"); return f;
    }

    private Label formTitle(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:" + VL + ";-fx-font-family:'Consolas';-fx-font-weight:bold;-fx-font-size:16;");
        return l;
    }

    private Label smallLbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:" + VL + "bb;-fx-font-size:10;"); return l;
    }

    private Label lbl26(String t, String color) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:26;-fx-font-weight:bold;-fx-text-fill:" + color + ";-fx-font-family:'Consolas';");
        return l;
    }

    private Label lbl12(String t, String color) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";"); return l;
    }

    private Region accentBar(double w) {
        Region r = new Region(); r.setPrefSize(w, 4); r.setMaxWidth(w);
        r.setStyle("-fx-background-color:" + VL + ";-fx-background-radius:2;"); return r;
    }

    private Separator sep() {
        Separator s = new Separator(); s.setStyle("-fx-opacity:0.3;"); return s;
    }

    private TextField tf(String style, String prompt) {
        TextField t = new TextField();
        t.setPromptText(prompt); t.setStyle(style); t.setPrefWidth(Double.MAX_VALUE); return t;
    }

    private String fs() {
        return "-fx-background-color:#0d1117;-fx-text-fill:white;-fx-prompt-text-fill:#4a5568;"
             + "-fx-border-color:#2d3f50;-fx-border-radius:6;-fx-background-radius:6;"
             + "-fx-padding:10;-fx-font-size:13;";
    }

    private ComboBox<String> comboHeure(String style, String defaultVal) {
        ComboBox<String> cb = new ComboBox<>();
        List<String> slots = new ArrayList<>();
        for (int h = 7; h <= 21; h++) { slots.add(String.format("%02d:00", h)); slots.add(String.format("%02d:30", h)); }
        cb.getItems().addAll(slots); cb.setValue(defaultVal);
        cb.setMaxWidth(Double.MAX_VALUE); cb.setStyle(style); return cb;
    }

    private Label headCell(String t, double w) {
        Label l = new Label(t); l.setAlignment(Pos.CENTER); l.setPrefWidth(w);
        l.setStyle("-fx-background-color:#e2e8f0;-fx-text-fill:#475569;-fx-font-weight:bold;"
                + "-fx-font-size:10;-fx-padding:8;-fx-font-family:'Consolas';");
        return l;
    }

    private <T> TableColumn<T, String> tcol(String title, String prop, double w) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop)); c.setPrefWidth(w); return c;
    }

    private <T> TableColumn<T, Void> actionCol3(
            java.util.function.Consumer<T> onEdit,
            java.util.function.Consumer<T> onDel,
            java.util.function.Consumer<T> onExtra) {
        TableColumn<T, Void> col = new TableColumn<>("ACTIONS");
        col.setCellFactory(p -> new TableCell<>() {
            final Button bE = new Button("✏️");
            final Button bD = new Button("🗑️");
            final Button bP = new Button("📅");
            final HBox   box = new HBox(8, bE, bD, bP);
            {
                box.setAlignment(Pos.CENTER);
                bE.setStyle("-fx-background-color:transparent;-fx-cursor:hand;-fx-font-size:14;");
                bD.setStyle("-fx-background-color:transparent;-fx-text-fill:#ef4444;-fx-cursor:hand;-fx-font-size:14;");
                bP.setStyle("-fx-background-color:transparent;-fx-text-fill:#3b82f6;-fx-cursor:hand;-fx-font-size:14;");
                Tooltip.install(bP, new Tooltip("Ajouter un créneau"));
                bE.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                bD.setOnAction(e -> onDel.accept(getTableView().getItems().get(getIndex())));
                bP.setOnAction(e -> onExtra.accept(getTableView().getItems().get(getIndex())));
            }
            protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : box); }
        });
        return col;
    }

    private VBox kpi(String titre, String valeur, String bg, String fg) {
        VBox w = new VBox(6); w.setAlignment(Pos.CENTER); w.setPrefSize(190, 105);
        w.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:18;-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.15),10,0,0,2);");
        Label lt = new Label(titre); lt.setAlignment(Pos.CENTER);
        lt.setStyle("-fx-font-size:11;-fx-font-weight:bold;-fx-text-fill:" + fg + ";-fx-font-family:'Consolas';");
        Label lv = new Label(valeur);
        lv.setStyle("-fx-font-size:36;-fx-font-weight:bold;-fx-text-fill:" + fg + ";-fx-font-family:'Consolas';");
        w.getChildren().addAll(lt, lv); return w;
    }

    private VBox moduleCard(String icon, String titre, String desc,
            EventHandler<javafx.scene.input.MouseEvent> action) {
        VBox card = new VBox(12); card.setPadding(new Insets(24)); card.setPrefSize(290, 130);
        card.setCursor(javafx.scene.Cursor.HAND);
        String base = "-fx-background-color:" + CARD + ";-fx-background-radius:16;"
                + "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.08),10,0,0,2);"
                + "-fx-border-color:#e2e8f0;-fx-border-width:1.5;-fx-border-radius:16;";
        card.setStyle(base); card.setOnMouseClicked(action);
        Label li = new Label(icon + " " + titre);
        li.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + BD + ";");
        Label ld = new Label(desc); ld.setWrapText(true);
        ld.setStyle("-fx-font-size:11;-fx-text-fill:#64748b;");
        card.getChildren().addAll(li, ld);
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-4);
            card.setStyle("-fx-background-color:" + BD + ";-fx-background-radius:16;"
                    + "-fx-effect:dropshadow(three-pass-box," + VL + ",14,0,0,0);"
                    + "-fx-border-color:" + VL + ";-fx-border-width:2;-fx-border-radius:16;");
            li.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + VL + ";");
            ld.setStyle("-fx-font-size:11;-fx-text-fill:#94a3b8;");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0); card.setStyle(base);
            li.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + BD + ";");
            ld.setStyle("-fx-font-size:11;-fx-text-fill:#64748b;");
        });
        return card;
    }

    private VBox grpSaisie(String lbl, Control input, double w) {
        VBox g = new VBox(5);
        Label l = new Label(lbl);
        l.setStyle("-fx-text-fill:" + VL + ";-fx-font-size:10;-fx-font-weight:bold;-fx-font-family:'Consolas';");
        input.setPrefWidth(w); g.getChildren().addAll(l, input); return g;
    }

    private void log(String msg) {
        journal.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + " — " + msg);
        System.out.println("[GEST] " + msg);
    }

    private void popup(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
 