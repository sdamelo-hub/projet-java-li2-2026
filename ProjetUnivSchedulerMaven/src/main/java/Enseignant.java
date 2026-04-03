

public class Enseignant extends Utilisateur {
    private String grade;
    private String specialite;
    private double chargeHoraireAnnuelle;
    private String idEnseignant;

    public Enseignant(String identifiantConnexion, String motDePass, String idEnseignant, String grade, String specialite, double chargeHoraireAnnuelle) {
        super(identifiantConnexion, motDePass);
        this.idEnseignant = idEnseignant;
        this.grade = grade;
        this.specialite = specialite;
        this.chargeHoraireAnnuelle = chargeHoraireAnnuelle;
    }

    public boolean reserverSallePonctuelle() {
        // Logique métier pour effectuer une demande de réservation hors planning
        return true;
    }

    public void consulterEmploiTemps() {
        // Logique pour filtrer et afficher les créneaux affectés à cet enseignant
    }

    public void signalerProbleme() {
        // Méthode pour notifier le gestionnaire d'un incident en salle
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public double getChargeHoraireAnnuelle() {
        return chargeHoraireAnnuelle;
    }

    public void setChargeHoraireAnnuelle(double chargeHoraireAnnuelle) {
        this.chargeHoraireAnnuelle = chargeHoraireAnnuelle;
    }

    public String getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(String idEnseignant) {
        this.idEnseignant = idEnseignant;
    }
}