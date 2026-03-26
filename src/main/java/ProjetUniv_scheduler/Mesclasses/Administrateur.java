package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import ProjetUniv_scheduler.Mesclasses.Utilisateur;

@Entity
@Table(name = "administrateurs")
@PrimaryKeyJoinColumn(name = "identifiantConnexion")
public class Administrateur extends Utilisateur {

    private String idAdmin;

    // Constructeur par défaut indispensable pour Hibernate
    public Administrateur() {
        super();
    }

    public Administrateur(String identifiantConnexion, String motDePass, String idAdmin) {
        super(identifiantConnexion, motDePass);
        this.idAdmin = idAdmin;
    }

    public String getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(String idAdmin) {
        this.idAdmin = idAdmin;
    }

    public void consulterStatistique() {
        // Logique pour appeler les méthodes de la classe Statistique
    }

    public void gererUtilisateur() {
        // Logique CRUD pour la gestion des comptes (Enseignant, Etudiant, Gestionnaire)
    }

    public void configurerBatiments() {
        // Logique pour l'ajout et la modification des structures physiques
    }

    public void configurerSalle(Salle salle) {
        // Paramétrage des capacités et catégories des salles de cours
    }

    public void definirTypeEquipement() {
        // Définition des nouvelles catégories de matériel disponibles
    }
}