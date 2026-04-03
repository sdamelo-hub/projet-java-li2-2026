

public class Batiment {
    // Attributs privés pour assurer l'intégrité structurelle
    private String nomBatiment;
    private String localisationBatiment;
    private int nbEtage;
    private String codeBatiment;
    private String typeBatiment;

    public Batiment() {
    }

    // Constructeur complet pour une initialisation immédiate
    public Batiment(String nomBatiment, String localisationBatiment, int nbEtage, String codeBatiment, String typeBatiment) {
        this.nomBatiment = nomBatiment;
        this.localisationBatiment = localisationBatiment;
        this.nbEtage = nbEtage;
        this.codeBatiment = codeBatiment;
        this.typeBatiment = typeBatiment;
    }

    // Méthode métier pour calculer la capacité totale
    public int obtenirCapaciteTotal() {
        // Cette méthode devra sommer la capacité de toutes les salles liées
        return 0; 
    }

    // Accesseurs et Mutateurs (Getters et Setters)
    public String getNomBatiment() {
        return nomBatiment;
    }

    public void setNomBatiment(String nomBatiment) {
        this.nomBatiment = nomBatiment;
    }

    public String getLocalisationBatiment() {
        return localisationBatiment;
    }

    public void setLocalisationBatiment(String localisationBatiment) {
        this.localisationBatiment = localisationBatiment;
    }

    public int getNbEtage() {
        return nbEtage;
    }

    public void setNbEtage(int nbEtage) {
        this.nbEtage = nbEtage;
    }

    public String getCodeBatiment() {
        return codeBatiment;
    }

    public void setCodeBatiment(String codeBatiment) {
        this.codeBatiment = codeBatiment;
    }

    public String getTypeBatiment() {
        return typeBatiment;
    }

    public void setTypeBatiment(String typeBatiment) {
        this.typeBatiment = typeBatiment;
    }
}