package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "statistiques")
public class Statistique {

    @Id
    private String idStatistique;
    
    private LocalDate date;
    private int nombreReservations;
    private int nombreAnnulations;
    private int totalConflitsEvites;

    @ManyToOne
    @JoinColumn(name = "id_salle_plus_utilisee")
    private Salle sallePlusUtilisee;

    @ManyToOne
    @JoinColumn(name = "id_salle_moins_utilisee")
    private Salle salleMoinsUtilisee;

    public Statistique() {
    }

    public Statistique(String idStatistique, LocalDate date) {
        this.idStatistique = idStatistique;
        this.date = date;
        this.nombreReservations = 0;
        this.nombreAnnulations = 0;
        this.totalConflitsEvites = 0;
    }

    public Double calculerTauxOccupation(List<Reservation> reservations, List<Salle> salles) {
        if (salles == null || salles.isEmpty()) return 0.0;
        
        double heuresOccupees = 0.0;
        for (Reservation r : reservations) {
            if (r.getEtatReservation().equalsIgnoreCase("Validée")) {
                heuresOccupees += r.getMonCreneau().calculerDureeReelle();
            }
        }
        
        double capaciteTotale = salles.size() * 10.0; 
        return (heuresOccupees / capaciteTotale) * 100.0;
    }

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

    public Salle getSallePlusUtilisee() { return sallePlusUtilisee; }
    public void setSallePlusUtilisee(Salle sallePlusUtilisee) { this.sallePlusUtilisee = sallePlusUtilisee; }

    public Salle getSalleMoinsUtilisee() { return salleMoinsUtilisee; }
    public void setSalleMoinsUtilisee(Salle salleMoinsUtilisee) { this.salleMoinsUtilisee = salleMoinsUtilisee; }
}