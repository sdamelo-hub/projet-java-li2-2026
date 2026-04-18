package ProjetUniv_scheduler;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int numReservation;

    private LocalDateTime dateHeureReservation;
    private String etatReservation;
    private String motifReservation;
    private String dernierModificateur;
    private LocalDateTime dateDernierModification;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_creneau")
    private Creneau monCreneau;

    private String natureSession;
    private int niveauPriorite;

    // Constructeur par défaut requis par Hibernate
    public Reservation() {
    }

    public Reservation(int numReservation, Creneau monCreneau, String natureSession, int niveauPriorite) {
        this.numReservation = numReservation;
        this.monCreneau = monCreneau;
        this.natureSession = natureSession;
        this.niveauPriorite = niveauPriorite;
        this.dateHeureReservation = LocalDateTime.now();
        this.etatReservation = "En attente";
    }

    public Boolean detecterConflit(Reservation autre) {
        if (autre == null || autre.getMonCreneau() == null || this.monCreneau == null) {
            return false;
        }
        return this.monCreneau.chevauche(autre.getMonCreneau());
    }

    public void enregistrerModification(Utilisateur u) {
        if (u != null) {
            this.dernierModificateur = u.getIdentifiantConnexion();
            this.dateDernierModification = LocalDateTime.now();
        }
    }

    public Boolean annulerReservation() {
        this.etatReservation = "Annulée";
        return true;
    }

    public Boolean validerReservation() {
        this.etatReservation = "Validée";
        return true;
    }

    // Accesseurs et Mutateurs
    public int getNumReservation() { return numReservation; }
    public void setNumReservation(int numReservation) { this.numReservation = numReservation; }

    public LocalDateTime getDateHeureReservation() { return dateHeureReservation; }
    public void setDateHeureReservation(LocalDateTime dateHeureReservation) { this.dateHeureReservation = dateHeureReservation; }

    public String getEtatReservation() { return etatReservation; }
    public void setEtatReservation(String etatReservation) { this.etatReservation = etatReservation; }

    public String getMotifReservation() { return motifReservation; }
    public void setMotifReservation(String motifReservation) { this.motifReservation = motifReservation; }

    public String getDernierModificateur() { return dernierModificateur; }
    public void setDernierModificateur(String dernierModificateur) { this.dernierModificateur = dernierModificateur; }

    public LocalDateTime getDateDernierModification() { return dateDernierModification; }
    public void setDateDernierModification(LocalDateTime dateDernierModification) { this.dateDernierModification = dateDernierModification; }

    public Creneau getMonCreneau() { return monCreneau; }
    public void setMonCreneau(Creneau monCreneau) { this.monCreneau = monCreneau; }

    public String getNatureSession() { return natureSession; }
    public void setNatureSession(String natureSession) { this.natureSession = natureSession; }

    public int getNiveauPriorite() { return niveauPriorite; }
    public void setNiveauPriorite(int niveauPriorite) { this.niveauPriorite = niveauPriorite; }
}