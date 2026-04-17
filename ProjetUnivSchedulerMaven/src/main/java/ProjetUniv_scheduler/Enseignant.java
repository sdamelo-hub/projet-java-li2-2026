package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Enseignant")
public class Enseignant extends Utilisateur {

    @Column(name = "grade")
    private String grade;

    @Column(name = "specialite")
    private String specialite;

    @Column(name = "chargeHoraireAnnuelle")
    private double chargeHoraireAnnuelle;

    @Column(name = "idEnseignant")
    private String idEnseignant;

    public Enseignant() { super(); }

    public Enseignant(String identifiantConnexion, String motDePasse, String idEnseignant,
                      String grade, String specialite, double chargeHoraireAnnuelle) {
        super(identifiantConnexion, motDePasse);
        this.idEnseignant = idEnseignant;
        this.grade = grade;
        this.specialite = specialite;
        this.chargeHoraireAnnuelle = chargeHoraireAnnuelle;
    }

    public boolean reserverSallePonctuelle() { return true; }
    public void consulterEmploiTemps() {}
    public void signalerProbleme() {}

    public String getGrade() { return grade; }
    public void setGrade(String v) { this.grade = v; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String v) { this.specialite = v; }
    public double getChargeHoraireAnnuelle() { return chargeHoraireAnnuelle; }
    public void setChargeHoraireAnnuelle(double v) { this.chargeHoraireAnnuelle = v; }
    public String getIdEnseignant() { return idEnseignant; }
    public void setIdEnseignant(String v) { this.idEnseignant = v; }
}
