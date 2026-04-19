package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "batiments")
public class Batiment {

    @Id
    @Column(name = "code_batiment")
    private String codeBatiment;

    @Column(name = "nombatiment")
    private String nomBatiment;
    
    private String localisationBatiment;
    private int nbEtage;
    private String typeBatiment;

    public Batiment() {
    }

    public Batiment(String codeBatiment, String nomBatiment, String localisationBatiment, int nbEtage, String typeBatiment) {
        this.codeBatiment = codeBatiment;
        this.nomBatiment = nomBatiment;
        this.localisationBatiment = localisationBatiment;
        this.nbEtage = nbEtage;
        this.typeBatiment = typeBatiment;
    }

    // Getters et Setters
    public String getCodeBatiment() { return codeBatiment; }
    public void setCodeBatiment(String codeBatiment) { this.codeBatiment = codeBatiment; }

    public String getNomBatiment() { return nomBatiment; }
    public void setNomBatiment(String nomBatiment) { this.nomBatiment = nomBatiment; }

    public String getLocalisationBatiment() { return localisationBatiment; }
    public void setLocalisationBatiment(String localisationBatiment) { this.localisationBatiment = localisationBatiment; }

    public int getNbEtage() { return nbEtage; }
    public void setNbEtage(int nbEtage) { this.nbEtage = nbEtage; }

    public String getTypeBatiment() { return typeBatiment; }
    public void setTypeBatiment(String typeBatiment) { this.typeBatiment = typeBatiment; }
}