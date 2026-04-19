package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@Table(name = "equipements")
public class Equipement {

    @Id
    private String idEquipement;
    
    private String nomEquipement;
    private String etatFonctionnement;

    @ManyToOne
    @JoinColumn(name = "numero_salle")
    private Salle salle;

    // Constructeur par défaut requis par Hibernate
    public Equipement() {
    }

    public Equipement(String idEquipement, String nomEquipement, String etatFonctionnement) {
        this.idEquipement = idEquipement;
        this.nomEquipement = nomEquipement;
        this.etatFonctionnement = etatFonctionnement;
    }

    public void signalerPanne() {
        this.etatFonctionnement = "En panne";
    }

    public String getIdEquipement() { return idEquipement; }
    public void setIdEquipement(String idEquipement) { this.idEquipement = idEquipement; }

    public String getNomEquipement() { return nomEquipement; }
    public void setNomEquipement(String nomEquipement) { this.nomEquipement = nomEquipement; }

    public String getEtatFonctionnement() { return etatFonctionnement; }
    public void setEtatFonctionnement(String etatFonctionnement) { this.etatFonctionnement = etatFonctionnement; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }
}