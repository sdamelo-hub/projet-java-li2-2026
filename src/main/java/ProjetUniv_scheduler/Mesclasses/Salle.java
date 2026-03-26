package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salles")
public class Salle {

    @Id
    private String numeroSalle;
    
    private int capacite;
    private String categorieSalle;
    private String etatSalle;

    @ManyToOne
    @JoinColumn(name = "code_batiment")
    private Batiment batiment;

    @OneToMany(mappedBy = "salle")
    private List<Creneau> creneaux;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL)
    private List<Equipement> equipements;

    public Salle() {
        this.equipements = new ArrayList<>();
        this.creneaux = new ArrayList<>();
    }

    public Salle(String numeroSalle, int capacite, String categorieSalle, String etatSalle) {
        this.numeroSalle = numeroSalle;
        this.capacite = capacite;
        this.categorieSalle = categorieSalle;
        this.etatSalle = etatSalle;
        this.equipements = new ArrayList<>();
        this.creneaux = new ArrayList<>();
    }

    public boolean estAdaptee(int effectifAttendu, List<String> besoinsMateriels) {
        if (this.capacite < effectifAttendu) {
            return false;
        }
        
        if (besoinsMateriels != null && !besoinsMateriels.isEmpty()) {
            for (String besoin : besoinsMateriels) {
                boolean trouve = false;
                for (Equipement e : equipements) {
                    if (e.getNomEquipement().equalsIgnoreCase(besoin) && 
                        e.getEtatFonctionnement().equalsIgnoreCase("Fonctionnel")) {
                        trouve = true;
                        break;
                    }
                }
                if (!trouve) return false;
            }
        }
        return true;
    }

    public String getNumeroSalle() { return numeroSalle; }
    public void setNumeroSalle(String numeroSalle) { this.numeroSalle = numeroSalle; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getCategorieSalle() { return categorieSalle; }
    public void setCategorieSalle(String categorieSalle) { this.categorieSalle = categorieSalle; }

    public String getEtatSalle() { return etatSalle; }
    public void setEtatSalle(String etatSalle) { this.etatSalle = etatSalle; }

    public Batiment getBatiment() { return batiment; }
    public void setBatiment(Batiment batiment) { this.batiment = batiment; }

    public List<Creneau> getCreneaux() { return creneaux; }
    public void setCreneaux(List<Creneau> creneaux) { this.creneaux = creneaux; }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) { this.equipements = equipements; }

    public void ajouterEquipement(Equipement e) { 
        if (e != null) {
            this.equipements.add(e);
            e.setSalle(this); 
        }
    }
}