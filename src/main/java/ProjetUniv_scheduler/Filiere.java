package ProjetUniv_scheduler;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "filieres")
public class Filiere {
    
    public enum Niveau { LICENCE, MASTER, DOCTORAT }

    @Id
    private String codeFiliere;
    
    private String nomFiliere;
    
    @Enumerated(EnumType.STRING)
    private Niveau niveauDiplome;
    
    private String responsableFiliere;

    @ManyToOne
    @JoinColumn(name = "code_departement")
    private Departement departement;

    // Pour l'instant, nous laissons les promotions sans annotation 
    // ou nous les commentons pour éviter une nouvelle erreur en cascade
    @Transient
    private List<Promotion> promotions;

    // Constructeur par défaut obligatoire
    public Filiere() {
        this.promotions = new ArrayList<>();
    }

    public Filiere(String codeFiliere, String nomFiliere, Niveau niveauDiplome, String responsableFiliere) {
        this.codeFiliere = codeFiliere;
        this.nomFiliere = nomFiliere;
        this.niveauDiplome = niveauDiplome;
        this.responsableFiliere = responsableFiliere;
        this.promotions = new ArrayList<>();
    }

    public int calculerEffectifTotal() {
        int total = 0;
        if (promotions != null) {
            for (Promotion p : promotions) {
                total += p.calculerEffectifTotal();
            }
        }
        return total;
    }

    // Accesseurs et Mutateurs
    public String getNomFiliere() { return nomFiliere; }
    public void setNomFiliere(String nomFiliere) { this.nomFiliere = nomFiliere; }

    public Niveau getNiveauDiplome() { return niveauDiplome; }
    public void setNiveauDiplome(Niveau niveauDiplome) { this.niveauDiplome = niveauDiplome; }

    public String getCodeFiliere() { return codeFiliere; }
    public void setCodeFiliere(String codeFiliere) { this.codeFiliere = codeFiliere; }

    public String getResponsableFiliere() { return responsableFiliere; }
    public void setResponsableFiliere(String responsableFiliere) { this.responsableFiliere = responsableFiliere; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public List<Promotion> getPromotions() { return promotions; }
    public void ajouterPromotion(Promotion p) { if (p != null) this.promotions.add(p); }
}