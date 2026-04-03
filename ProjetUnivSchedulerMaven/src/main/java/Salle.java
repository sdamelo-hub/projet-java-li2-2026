

import java.util.ArrayList;
import java.util.List;

public class Salle {
    private String numeroSalle;
    private int capacite;
    private String categorieSalle;
    private String etatSalle;
    private List<Equipement> equipements;

    public Salle(String numeroSalle, int capacite, String categorieSalle, String etatSalle) {
        this.numeroSalle = numeroSalle;
        this.capacite = capacite;
        this.categorieSalle = categorieSalle;
        this.etatSalle = etatSalle;
        this.equipements = new ArrayList<>();
    }

    /**
     * Vérifie si la salle peut accueillir un effectif et possède le matériel nécessaire.
     * La condition mathématique est : capacite >= effectifAttendu
     */
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

    public List<Equipement> getEquipements() { return equipements; }
    public void ajouterEquipement(Equipement e) { if (e != null) this.equipements.add(e); }
}