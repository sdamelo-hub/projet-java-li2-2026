package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {
    
    @Id
    private String identifiantConnexion;
    
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private boolean actif = true;

    // Constructeur par défaut indispensable pour Hibernate
    public Utilisateur() {
    }

    public Utilisateur(String identifiantConnexion, String motDePasse) {
        this.identifiantConnexion = identifiantConnexion;
        this.motDePasse = motDePasse;
    }

    public Utilisateur(String nom, String prenom, String identifiantConnexion, String email, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.identifiantConnexion = identifiantConnexion;
        this.email = email;
        this.motDePasse = motDePasse;
    }
   
    public boolean seConnecter() {
        return true; 
    }

    public boolean seDeconnecter() {
        return true;
    }

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

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + identifiantConnexion + ")";
    }
}
