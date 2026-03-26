package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idIncident;

    private String description;
    private LocalDateTime dateSignalement;
    private String statut; // Ouvert, En cours, Résolu

    @ManyToOne
    @JoinColumn(name = "id_enseignant")
    private Enseignant rapporteur;

    @ManyToOne
    @JoinColumn(name = "numero_salle")
    private Salle salleConcernee;

    @ManyToOne
    @JoinColumn(name = "id_equipement")
    private Equipement equipementConcerne;

    public Incident() {
        this.dateSignalement = LocalDateTime.now();
        this.statut = "Ouvert";
    }

    public Incident(String description, Enseignant rapporteur, Salle salleConcernee) {
        this();
        this.description = description;
        this.rapporteur = rapporteur;
        this.salleConcernee = salleConcernee;
    }

    public int getIdIncident() { return idIncident; }
    public void setIdIncident(int idIncident) { this.idIncident = idIncident; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Enseignant getRapporteur() { return rapporteur; }
    public void setRapporteur(Enseignant rapporteur) { this.rapporteur = rapporteur; }

    public Salle getSalleConcernee() { return salleConcernee; }
    public void setSalleConcernee(Salle salleConcernee) { this.salleConcernee = salleConcernee; }

    public Equipement getEquipementConcerne() { return equipementConcerne; }
    public void setEquipementConcerne(Equipement equipementConcerne) { this.equipementConcerne = equipementConcerne; }
}