package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs") // Indique que tout le monde va ici
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public class Utilisateur {

    @Id
    @Column(name = "identifiantConnexion", nullable = false)
    private String identifiantConnexion;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;

    @Column(name = "motDePasse")
    private String motDePasse;

    // ── Constructeur Hibernate (obligatoire) ──────────────────────────────────
    public Utilisateur() {}

    // ── Constructeur appelé par Enseignant, Etudiant, Administrateur, Gestionnaire
    //    via super(identifiantConnexion, motDePasse) ──────────────────────────
    public Utilisateur(String identifiantConnexion, String motDePasse) {
        this.identifiantConnexion = identifiantConnexion;
        this.motDePasse           = motDePasse;
    }

    // ── Constructeur complet pour créations depuis l'UI admin ─────────────────
    public Utilisateur(String nom, String prenom, String email, String role,
                       String identifiantConnexion, String motDePasse) {
        this.nom                  = nom;
        this.prenom               = prenom;
        this.email                = email;
        this.role                 = role;
        this.identifiantConnexion = identifiantConnexion;
        this.motDePasse           = motDePasse;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public String getIdentifiantConnexion()         { return identifiantConnexion; }
    public void   setIdentifiantConnexion(String v) { this.identifiantConnexion = v; }

    public String getNom()                          { return nom; }
    public void   setNom(String v)                  { this.nom = v; }

    public String getPrenom()                       { return prenom; }
    public void   setPrenom(String v)               { this.prenom = v; }

    public String getEmail()                        { return email; }
    public void   setEmail(String v)                { this.email = v; }

    public String getRole()                         { return role; }
    public void   setRole(String v)                 { this.role = v; }

    public String getMotDePasse()                   { return motDePasse; }
    public void   setMotDePasse(String v)           { this.motDePasse = v; }

    @Override
    public String toString() {
        return (nom != null ? nom : "?") + " " + (prenom != null ? prenom : "")
             + " [" + (role != null ? role : "?") + "]";
    }
}