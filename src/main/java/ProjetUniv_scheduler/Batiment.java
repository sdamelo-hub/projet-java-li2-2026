package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "batiments")
public class Batiment {

    @Id
    @Column(name = "code_batiment")
    private String codeBatiment;

    @Column(name = "nomBatiment")
    private String nomBatiment;

    @Column(name = "localisationBatiment")
    private String localisationBatiment;

    @Column(name = "nbEtage")
    private int nbEtage;

    @Column(name = "typeBatiment")
    private String typeBatiment;

    public Batiment() {}

    public Batiment(String nomBatiment, String localisationBatiment, int nbEtage, String codeBatiment, String typeBatiment) {
        this.nomBatiment = nomBatiment;
        this.localisationBatiment = localisationBatiment;
        this.nbEtage = nbEtage;
        this.codeBatiment = codeBatiment;
        this.typeBatiment = typeBatiment;
    }

    public int obtenirCapaciteTotal() { return 0; }

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


