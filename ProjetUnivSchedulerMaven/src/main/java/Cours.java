

public class Cours {
    private String intituleMatiere;
    private String codeCours;
    private double nbrHeure;
    private double heuresEffectuees;

    public Cours(String codeCours, String intituleMatiere, double nbrHeure) {
        this.codeCours = codeCours;
        this.intituleMatiere = intituleMatiere;
        this.nbrHeure = nbrHeure;
        this.heuresEffectuees = 0.0;
    }

    public void calculerAvancement(double nbH) {
        if (this.heuresEffectuees + nbH <= this.nbrHeure) {
            this.heuresEffectuees += nbH;
        } else {
            this.heuresEffectuees = this.nbrHeure;
        }
    }

    public double obtenirPourcentageAvancement() {
        if (this.nbrHeure == 0) return 0.0;
        return (this.heuresEffectuees / this.nbrHeure) * 100;
    }

    public String getIntituleMatiere() {
        return intituleMatiere;
    }

    public void setIntituleMatiere(String intituleMatiere) {
        this.intituleMatiere = intituleMatiere;
    }

    public String getCodeCours() {
        return codeCours;
    }

    public void setCodeCours(String codeCours) {
        this.codeCours = codeCours;
    }

    public double getNbrHeure() {
        return nbrHeure;
    }

    public void setNbrHeure(double nbrHeure) {
        this.nbrHeure = nbrHeure;
    }

    public double getHeuresEffectuees() {
        return heuresEffectuees;
    }
}