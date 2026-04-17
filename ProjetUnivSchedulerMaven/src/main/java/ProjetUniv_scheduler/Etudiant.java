package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Etudiant")
public class Etudiant extends Utilisateur {

    @Column(name = "ineEtudiant")
    private String ineEtudiant;

    @Column(name = "niveauEtude")
    private String niveauEtude;

    @Column(name = "cycle")
    private String cycle;

    @ManyToOne
    @JoinColumn(name = "code_groupe")
    private Groupe groupe;

    public Etudiant() { super(); }

    public Etudiant(String identifiantConnexion, String motDePasse,
                    String ineEtudiant, String niveauEtude, String cycle) {
        super(identifiantConnexion, motDePasse);
        this.ineEtudiant = ineEtudiant;
        this.niveauEtude = niveauEtude;
        this.cycle = cycle;
    }

    public void consulterPlanning() {}
    public void rechercherSalle() {}

    public String getIneEtudiant() { return ineEtudiant; }
    public void setIneEtudiant(String v) { this.ineEtudiant = v; }
    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String v) { this.niveauEtude = v; }
    public String getCycle() { return cycle; }
    public void setCycle(String v) { this.cycle = v; }
    public Groupe getGroupe() { return groupe; }
    public void setGroupe(Groupe v) { this.groupe = v; }
}
