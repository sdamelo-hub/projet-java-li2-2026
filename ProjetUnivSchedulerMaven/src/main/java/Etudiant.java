

public class Etudiant extends Utilisateur {
    private String ineEtudiant;
    private String niveauEtude;
    private String cycle;

    public Etudiant(String identifiantConnexion, String motDePass, String ineEtudiant, String niveauEtude, String cycle) {
        super(identifiantConnexion, motDePass);
        this.ineEtudiant = ineEtudiant;
        this.niveauEtude = niveauEtude;
        this.cycle = cycle;
    }

    /**
     * Permet à l'étudiant de visualiser son emploi du temps personnalisé.
     */
    public void consulterPlanning() {
        // Logique pour récupérer et afficher les réservations liées au groupe de l'étudiant
    }

    /**
     * Aide l'étudiant à localiser une salle spécifique sur le campus.
     */
    public void rechercherSalle() {
        // Logique d'interaction avec la classe Batiment et ses méthodes de localisation
    }

    public String getIneEtudiant() {
        return ineEtudiant;
    }

    public void setIneEtudiant(String ineEtudiant) {
        this.ineEtudiant = ineEtudiant;
    }

    public String getNiveauEtude() {
        return niveauEtude;
    }

    public void setNiveauEtude(String niveauEtude) {
        this.niveauEtude = niveauEtude;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }
}