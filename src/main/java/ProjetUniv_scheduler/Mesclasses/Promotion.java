package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    private String codePromotion;
    
    private String niveau;
    private String anneeAcademique;

    @ManyToOne
    @JoinColumn(name = "code_filiere")
    private Filiere filiere;

    @Transient
    private List<Groupe> groupes;

    // Constructeur par défaut indispensable pour Hibernate
    public Promotion() {
        this.groupes = new ArrayList<>();
    }

    public Promotion(String codePromotion, String niveau, String anneeAcademique) {
        this.codePromotion = codePromotion;
        this.niveau = niveau;
        this.anneeAcademique = anneeAcademique;
        this.groupes = new ArrayList<>();
    }

    public int calculerEffectifTotal() {
        int total = 0;
        if (groupes != null) {
            for (Groupe g : groupes) {
                total += g.getEffectifGroupe();
            }
        }
        return total;
    }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getCodePromotion() { return codePromotion; }
    public void setCodePromotion(String codePromotion) { this.codePromotion = codePromotion; }

    public String getAnneeAcademique() { return anneeAcademique; }
    public void setAnneeAcademique(String anneeAcademique) { this.anneeAcademique = anneeAcademique; }

    public Filiere getFiliere() { return filiere; }
    public void setFiliere(Filiere filiere) { this.filiere = filiere; }

    public List<Groupe> getGroupes() { return groupes; }
    public void ajouterGroupe(Groupe groupe) {
        if (groupe != null) {
            this.groupes.add(groupe);
        }
    }
}