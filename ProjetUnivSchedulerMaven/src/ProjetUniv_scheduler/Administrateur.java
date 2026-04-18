package ProjetUniv_scheduler;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Administrateur")
public class Administrateur extends Utilisateur {

    @Column(name = "idAdmin")
    private String idAdmin;

    public Administrateur() { super(); }

    public Administrateur(String identifiantConnexion, String motDePasse, String idAdmin) {
        super(identifiantConnexion, motDePasse);
        this.idAdmin = idAdmin;
    }

    public String getIdAdmin() { return idAdmin; }
    public void setIdAdmin(String idAdmin) { this.idAdmin = idAdmin; }

    public void consulterStatistique() {}
    public void gererUtilisateur() {}
    public void configurerBatiments() {}
    public void configurerSalle(Salle salle) {}
    public void definirTypeEquipement() {}
}
