package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Gestionnaire")
public class Gestionnaire extends Utilisateur {

    @Column(name = "idGestionnaire")
    private int idGestionnaire;

    @Column(name = "responsabilite")
    private String responsabilite;

    public Gestionnaire() { super(); }

    public Gestionnaire(String identifiantConnexion, String motDePasse,
                        int idGestionnaire, String responsabilite) {
        super(identifiantConnexion, motDePasse);
        this.idGestionnaire = idGestionnaire;
        this.responsabilite = responsabilite;
    }

    public void genererEmploiTemps() {}
    public void validerReservation(Reservation reservation) {
        if (reservation != null) reservation.validerReservation();
    }
    public void creerCours() {}
    public void modifierCours() {}
    public void supprimerCours() {}
    public void assignerSalle() {}
    public void resoudreConflits() {}

    public int getIdGestionnaire() { return idGestionnaire; }
    public void setIdGestionnaire(int v) { this.idGestionnaire = v; }
    public String getResponsabilite() { return responsabilite; }
    public void setResponsabilite(String v) { this.responsabilite = v; }
}
