

public class Gestionnaire extends Utilisateur {
    private int idGestionnaire;
    private String responsabilite;

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
        // Logique pour instancier un nouvel objet Cours dans le système
    }

    public void modifierCours() {
        // Logique de mise à jour des informations d'un cours existant
    }

    public void supprimerCours() {
        // Logique pour retirer un cours de la base de données
    }

    public void assignerSalle() {
        // Logique pour lier une salle disponible à une demande de réservation
    }

    public void resoudreConflits() {
        // Algorithme d'arbitrage en cas de chevauchement détecté par la classe Reservation
    }

    public int getIdGestionnaire() {
        return idGestionnaire;
    }

    public void setIdGestionnaire(int idGestionnaire) {
        this.idGestionnaire = idGestionnaire;
    }

    public String getResponsabilite() {
        return responsabilite;
    }

    public void setResponsabilite(String responsabilite) {
        this.responsabilite = responsabilite;
    }
}