package ProjetUniv_scheduler;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "creneaux")
public class Creneau {

    @Id
    @Column(name = "idCreneau")
    private String idCreneau;

    @Column(name = "dateSeance")
    private LocalDate dateSeance;

    @Column(name = "heureDebut")
    private LocalTime heureDebut;

    @Column(name = "heureFin")
    private LocalTime heureFin;

    @Column(name = "duree")
    private Double duree;

    @Column(name = "typeSeance")
    private String typeSeance;

    @ManyToOne
    @JoinColumn(name = "code_cours")
    private Cours cours;

    @ManyToOne
    @JoinColumn(name = "numero_salle")
    private Salle salle;

    public Creneau() {
        this.idCreneau = UUID.randomUUID().toString();
    }

    public Creneau(String idCreneau, LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin) {
        this.idCreneau = idCreneau != null ? idCreneau : UUID.randomUUID().toString();
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
        if (autre == null || this.dateSeance == null || autre.getDateSeance() == null) return false;
        if (!this.dateSeance.equals(autre.getDateSeance())) return false;
        return this.heureDebut.isBefore(autre.getHeureFin()) &&
                autre.getHeureDebut().isBefore(this.heureFin);
    }

    public String getIdCreneau() { return idCreneau; }
    public void setIdCreneau(String idCreneau) { this.idCreneau = idCreneau; }
    public LocalDate getDateSeance() { return dateSeance; }
    public void setDateSeance(LocalDate dateSeance) { this.dateSeance = dateSeance; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; this.duree = calculerDureeReelle(); }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; this.duree = calculerDureeReelle(); }
    public Double getDuree() { return duree; }
    public void setDuree(Double duree) { this.duree = duree; }
    public String getTypeSeance() { return typeSeance; }
    public void setTypeSeance(String typeSeance) { this.typeSeance = typeSeance; }
    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }
    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }
}