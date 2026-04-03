

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Creneau {
    private String idCreneau;
    private LocalDate dateSeance;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Double duree;

    public Creneau(String idCreneau, LocalDate dateSeance, LocalTime heureDebut, LocalTime heureFin) {
        this.idCreneau = idCreneau;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.duree = calculerDureeReelle();
    }

    /**
     * Calcule la durée entre l'heure de début et de fin.
     * Retourne la durée en heures (ex: 1.5 pour 1h30).
     */
    public final Double calculerDureeReelle() {
        if (heureDebut == null || heureFin == null) return 0.0;
        long minutes = ChronoUnit.MINUTES.between(heureDebut, heureFin);
        return minutes / 60.0;
    }

    /**
     * Méthode d'intelligence métier ajoutée pour détecter si ce créneau
     * entre en conflit avec un autre sur la même date.
     */
    public boolean chevauche(Creneau autre) {
        if (!this.dateSeance.equals(autre.getDateSeance())) return false;
        
        // Un conflit existe si (Début1 < Fin2) ET (Début2 < Fin1)
        return this.heureDebut.isBefore(autre.getHeureFin()) && 
               autre.getHeureDebut().isBefore(this.heureFin);
    }

    // Accesseurs et Mutateurs (Getters & Setters)
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
}