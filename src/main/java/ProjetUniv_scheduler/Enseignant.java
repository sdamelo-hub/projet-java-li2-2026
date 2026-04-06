package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "enseignants")
@PrimaryKeyJoinColumn(name = "identifiantConnexion")
public class Enseignant extends Utilisateur {

    private String grade;
    private String specialite;
    private double chargeHoraireAnnuelle;
    private String idEnseignant;

    // Constructeur par défaut indispensable pour Hibernate
    public Enseignant() {
        super();
    }

    public Enseignant(String identifiantConnexion, String motDePass, String idEnseignant, String grade, String specialite, double chargeHoraireAnnuelle) {
        super(identifiantConnexion, motDePass);
        this.idEnseignant = idEnseignant;
        this.grade = grade;
        this.specialite = specialite;
        this.chargeHoraireAnnuelle = chargeHoraireAnnuelle;
    }

    public boolean reserverSallePonctuelle() {
        return true;
    }

    public void consulterEmploiTemps() {
    }

    public void signalerProbleme() {
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public double getChargeHoraireAnnuelle() { return chargeHoraireAnnuelle; }
    public void setChargeHoraireAnnuelle(double chargeHoraireAnnuelle) { this.chargeHoraireAnnuelle = chargeHoraireAnnuelle; }

    public String getIdEnseignant() { return idEnseignant; }
    public void setIdEnseignant(String idEnseignant) { this.idEnseignant = idEnseignant; }
}