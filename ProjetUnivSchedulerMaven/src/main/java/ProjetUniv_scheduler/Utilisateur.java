package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur { // Retrait de "abstract" pour permettre l'instanciation
    
    @Id
    private String identifiantConnexion; // Peut servir de matricule ou d'identifiant unique
    
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role; // Ajout du champ role (Administrateur, Enseignant, etc.)

    // Constructeur par défaut indispensable pour Hibernate
    public Utilisateur() {
    }

    // Constructeur simplifié pour la création rapide (ex: dans showAddUserDialog)
    public Utilisateur(String nom, String email, String role) {
        this.nom = nom;
        this.email = email;
        this.role = role;
        // On peut mettre l'email comme identifiant par défaut si non précisé
        this.identifiantConnexion = email; 
    }

    public Utilisateur(String nom, String prenom, String identifiantConnexion, String email, String motDePasse, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.identifiantConnexion = identifiantConnexion;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }
   
    // --- MÉTHODES MÉTIER ---
    public boolean seConnecter() {
        return true; 
    }

    public boolean seDeconnecter() {
        return true;
    }

    // --- GETTERS ET SETTERS ---
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getIdentifiantConnexion() { return identifiantConnexion; }
    public void setIdentifiantConnexion(String identifiantConnexion) { this.identifiantConnexion = identifiantConnexion; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}