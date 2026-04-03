

import java.util.ArrayList;
import java.util.List;

public class Filiere {
    public enum Niveau { LICENCE, MASTER, DOCTORAT }

    private String nomFiliere;
    private Niveau niveauDiplome;
    private String codeFiliere;
    private String responsableFiliere;
    private List<Promotion> promotions;

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

    public String getNomFiliere() { return nomFiliere; }
    public void setNomFiliere(String nomFiliere) { this.nomFiliere = nomFiliere; }

    public Niveau getNiveauDiplome() { return niveauDiplome; }
    public void setNiveauDiplome(Niveau niveauDiplome) { this.niveauDiplome = niveauDiplome; }

    public String getCodeFiliere() { return codeFiliere; }
    public void setCodeFiliere(String codeFiliere) { this.codeFiliere = codeFiliere; }

    public String getResponsableFiliere() { return responsableFiliere; }
    public void setResponsableFiliere(String responsableFiliere) { this.responsableFiliere = responsableFiliere; }

    public List<Promotion> getPromotions() { return promotions; }
    public void ajouterPromotion(Promotion p) { if (p != null) this.promotions.add(p); }
}