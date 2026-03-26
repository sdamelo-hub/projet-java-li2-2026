package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name = "batiments")
public class Batiment {

    @Id
    @Column(name = "code_batiment")
    private String codeBatiment;

    private String nomBatiment;
    private String localisationBatiment;
    private int nbEtage;
    private String typeBatiment;

    public Batiment() {
    }

    public Batiment(String nomBatiment, String localisationBatiment, int nbEtage, String codeBatiment, String typeBatiment) {
        this.nomBatiment = nomBatiment;
        this.localisationBatiment = localisationBatiment;
        this.nbEtage = nbEtage;
        this.codeBatiment = codeBatiment;
        this.typeBatiment = typeBatiment;
    }

    public int obtenirCapaciteTotal() {
        return 0; 
    }

    // Gardez vos getters et setters tels quels en bas
    public String getNomBatiment() { return nomBatiment; }
    public void setNomBatiment(String nomBatiment) { this.nomBatiment = nomBatiment; }
    public String getLocalisationBatiment() { return localisationBatiment; }
    public void setLocalisationBatiment(String localisationBatiment) { this.localisationBatiment = localisationBatiment; }
    public int getNbEtage() { return nbEtage; }
    public void setNbEtage(int nbEtage) { this.nbEtage = nbEtage; }
    public String getCodeBatiment() { return codeBatiment; }
    public void setCodeBatiment(String codeBatiment) { this.codeBatiment = codeBatiment; }
    public String getTypeBatiment() { return typeBatiment; }
    public void setTypeBatiment(String typeBatiment) { this.typeBatiment = typeBatiment; }
}