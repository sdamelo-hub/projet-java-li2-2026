

public class Equipement {
    private String idEquipement;
    private String nomEquipement;
    private String etatFonctionnement;

    public Equipement(String idEquipement, String nomEquipement, String etatFonctionnement) {
        this.idEquipement = idEquipement;
        this.nomEquipement = nomEquipement;
        this.etatFonctionnement = etatFonctionnement;
    }

    /**
     * Met à jour l'état de l'équipement pour signaler une défaillance.
     */
    public void signalerPanne() {
        this.etatFonctionnement = "En panne";
        // Une logique de notification pourrait être ajoutée ici
    }

    public String getIdEquipement() {
        return idEquipement;
    }

    public void setIdEquipement(String idEquipement) {
        this.idEquipement = idEquipement;
    }

    public String getNomEquipement() {
        return nomEquipement;
    }

    public void setNomEquipement(String nomEquipement) {
        this.nomEquipement = nomEquipement;
    }

    public String getEtatFonctionnement() {
        return etatFonctionnement;
    }

    public void setEtatFonctionnement(String etatFonctionnement) {
        this.etatFonctionnement = etatFonctionnement;
    }
}