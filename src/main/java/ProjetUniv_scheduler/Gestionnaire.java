package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "gestionnaires")
@PrimaryKeyJoinColumn(name = "identifiantConnexion")
public class Gestionnaire extends Utilisateur {
    
    private int idGestionnaire;
    private String responsabilite;

    // Constructeur par défaut indispensable pour Hibernate
    public Gestionnaire() {
        super();
    }

    public Gestionnaire(String identifiantConnexion, String motDePass, int idGestionnaire, String responsabilite) {
        super(identifiantConnexion, motDePass);
        this.idGestionnaire = idGestionnaire;
        this.responsabilite = responsabilite;
    }

    public void genererEmploiTemps() {
        // Logique pour compiler les réservations validées en un planning global
    }

    public void validerReservation(Reservation reservation) {
        if (reservation != null) {
            reservation.validerReservation();
        }
    }

    public void creerCours() {
        // Logique pour instancier un nouvel objet Cours
    }

    public void modifierCours() {
        // Logique de mise à jour
    }

    public void supprimerCours() {
        // Logique de retrait
    }

    public void assignerSalle() {
        // Logique de liaison
    }

    public void resoudreConflits() {
        // Algorithme d'arbitrage
    }

    public int getIdGestionnaire() { return idGestionnaire; }
    public void setIdGestionnaire(int idGestionnaire) { this.idGestionnaire = idGestionnaire; }

    public String getResponsabilite() { return responsabilite; }
    public void setResponsabilite(String responsabilite) { this.responsabilite = responsabilite; }
}	