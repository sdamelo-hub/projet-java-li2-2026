package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import ProjetUniv_scheduler.Mesclasses.Utilisateur;

@Entity
@Table(name = "etudiants")
@PrimaryKeyJoinColumn(name = "identifiantConnexion")
public class Etudiant extends Utilisateur {

    private String ineEtudiant;
    private String niveauEtude;
    private String cycle;

    @ManyToOne
    @JoinColumn(name = "code_groupe")
    private Groupe groupe;

    // Constructeur par défaut indispensable pour Hibernate
    public Etudiant() {
        super();
    }

    public Etudiant(String identifiantConnexion, String motDePass, String ineEtudiant, String niveauEtude, String cycle) {
        super(identifiantConnexion, motDePass);
        this.ineEtudiant = ineEtudiant;
        this.niveauEtude = niveauEtude;
        this.cycle = cycle;
    }

    public void consulterPlanning() {
    }

    public void rechercherSalle() {
    }

    public String getIneEtudiant() { return ineEtudiant; }
    public void setIneEtudiant(String ineEtudiant) { this.ineEtudiant = ineEtudiant; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }

    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }

    public Groupe getGroupe() { return groupe; }
    public void setGroupe(Groupe groupe) { this.groupe = groupe; }
}