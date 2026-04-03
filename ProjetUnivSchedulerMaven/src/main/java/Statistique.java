

import java.time.LocalDate;
import java.util.List;

public class Statistique {
    private String idStatistique;
    private LocalDate date;
    private int nombreReservations;
    private int nombreAnnulations;
    private int totalConflitsEvites;

    public Statistique(String idStatistique, LocalDate date) {
        this.idStatistique = idStatistique;
        this.date = date;
        this.nombreReservations = 0;
        this.nombreAnnulations = 0;
        this.totalConflitsEvites = 0;
    }

    /**
     * Calcule le taux d'occupation global des salles.
     * La formule repose sur le ratio entre les heures occupées et la capacité totale.
     */
    public Double calculerTauxOccupation(List<Reservation> reservations, List<Salle> salles) {
        if (salles == null || salles.isEmpty()) return 0.0;
        
        double heuresOccupees = 0.0;
        for (Reservation r : reservations) {
            if (r.getEtatReservation().equalsIgnoreCase("Validée")) {
                heuresOccupees += r.getMonCreneau().calculerDureeReelle();
            }
        }
        
        // Exemple simplifié : 10 heures disponibles par salle et par jour
        double capaciteTotale = salles.size() * 10.0; 
        
        return (heuresOccupees / capaciteTotale) * 100.0;
    }

    // Accesseurs et Mutateurs corrigés (Standard Java Bean)
    public String getIdStatistique() { return idStatistique; }
    public void setIdStatistique(String idStatistique) { this.idStatistique = idStatistique; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getNombreReservations() { return nombreReservations; }
    public void setNombreReservations(int nombreReservations) { this.nombreReservations = nombreReservations; }

    public int getNombreAnnulations() { return nombreAnnulations; }
    public void setNombreAnnulations(int nombreAnnulations) { this.nombreAnnulations = nombreAnnulations; }

    public int getTotalConflitsEvites() { return totalConflitsEvites; }
    public void setTotalConflitsEvites(int totalConflitsEvites) { this.totalConflitsEvites = totalConflitsEvites; }
}