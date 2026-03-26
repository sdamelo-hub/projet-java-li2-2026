package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "creneaux")
public class Creneau {
    
    @Id
    private String idCreneau;
    
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Double duree;
    private String typeSeance;

    @ManyToOne
    @JoinColumn(name = "code_cours")
    private Cours cours;

    @ManyToOne
    @JoinColumn(name = "numero_salle")
    private Salle salle;

    // Constructeur par défaut requis par Hibernate
    public Creneau() {
    }

    public Creneau(String idCreneau, LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin) {
        this.idCreneau = idCreneau;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.duree = calculerDureeReelle();
    }

    public final Double calculerDureeReelle() {
        if (heureDebut == null || heureFin == null) return 0.0;
        long minutes = ChronoUnit.MINUTES.between(heureDebut, heureFin);
        return minutes / 60.0;
    }

    public boolean chevauche(Creneau autre) {
        if (!this.dateSeance.equals(autre.getDateSeance())) return false;
        return this.heureDebut.isBefore(autre.getHeureFin()) && 
               autre.getHeureDebut().isBefore(this.heureFin);
    }

    // Accesseurs et Mutateurs
    public String getIdCreneau() { return idCreneau; }
    public void setIdCreneau(String idCreneau) { this.idCreneau = idCreneau; }

    public LocalDate getDateSeance() { return dateSeance; }
    public void setDateSeance(LocalDate dateSeance) { this.dateSeance = dateSeance; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { 
        this.heureDebut = heureDebut; 
        this.duree = calculerDureeReelle();
    }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { 
        this.heureFin = heureFin; 
        this.duree = calculerDureeReelle();
    }

    public Double getDuree() { return duree; }
    public void setDuree(Double duree) { this.duree = duree; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }

    public String getTypeSeance() { return typeSeance; }
    public void setTypeSeance(String typeSeance) { this.typeSeance = typeSeance; }
}
