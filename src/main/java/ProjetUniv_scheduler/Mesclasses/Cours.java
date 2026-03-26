package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours")
public class Cours {

    @Id
    private String codeCours;
    
    private String intituleMatiere;
    private double nbrHeure;
    private double heuresEffectuees;

    @ManyToOne
    @JoinColumn(name = "id_enseignant")
    private Enseignant enseignant;

    @ManyToOne
    @JoinColumn(name = "code_groupe")
    private Groupe groupe;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL)
    private List<Creneau> creneaux;

    // Constructeur par défaut indispensable pour Hibernate
    public Cours() {
        this.creneaux = new ArrayList<>();
    }

    public Cours(String codeCours, String intituleMatiere, double nbrHeure) {
        this.codeCours = codeCours;
        this.intituleMatiere = intituleMatiere;
        this.nbrHeure = nbrHeure;
        this.heuresEffectuees = 0.0;
        this.creneaux = new ArrayList<>();
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

    // Getters et Setters
    public String getIntituleMatiere() { return intituleMatiere; }
    public void setIntituleMatiere(String intituleMatiere) { this.intituleMatiere = intituleMatiere; }

    public String getCodeCours() { return codeCours; }
    public void setCodeCours(String codeCours) { this.codeCours = codeCours; }

    public double getNbrHeure() { return nbrHeure; }
    public void setNbrHeure(double nbrHeure) { this.nbrHeure = nbrHeure; }

    public double getHeuresEffectuees() { return heuresEffectuees; }
    public void setHeuresEffectuees(double heuresEffectuees) { this.heuresEffectuees = heuresEffectuees; }

    public List<Creneau> getCreneaux() { return creneaux; }
    public void setCreneaux(List<Creneau> creneaux) { this.creneaux = creneaux; }

    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }

    public Groupe getGroupe() { return groupe; }
    public void setGroupe(Groupe groupe) { this.groupe = groupe; }
}
