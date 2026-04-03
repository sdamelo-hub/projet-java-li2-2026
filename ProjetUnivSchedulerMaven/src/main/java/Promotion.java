

import java.util.ArrayList;
import java.util.List;


public class Promotion {
    private String niveau;
    private String codePromotion;
    private String anneeAcademique;
    private List<Groupe> groupes;

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

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getCodePromotion() {
        return codePromotion;
    }

    public void setCodePromotion(String codePromotion) {
        this.codePromotion = codePromotion;
    }

    public String getAnneeAcademique() {
        return anneeAcademique;
    }

    public void setAnneeAcademique(String anneeAcademique) {
        this.anneeAcademique = anneeAcademique;
    }

    public List<Groupe> getGroupes() {
        return groupes;
    }

    public void ajouterGroupe(Groupe groupe) {
        if (groupe != null) {
            this.groupes.add(groupe);
        }
    }
}