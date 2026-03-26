package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;

@Entity
@Table(name = "groupes")
public class Groupe {

    @Id
    private String codeGroupe;
    
    private String modaliteGroupe;
    private String specialiteGroupe;
    private int effectifGroupe;
    private int numeroGroupe;

    @ManyToOne
    @JoinColumn(name = "code_promotion")
    private Promotion promotion;

    // Constructeur par défaut indispensable pour Hibernate
    public Groupe() {
    }

    public Groupe(String codeGroupe, String modaliteGroupe, String specialiteGroupe, int effectifGroupe, int numeroGroupe) {
        this.codeGroupe = codeGroupe;
        this.modaliteGroupe = modaliteGroupe;
        this.specialiteGroupe = specialiteGroupe;
        this.effectifGroupe = effectifGroupe;
        this.numeroGroupe = numeroGroupe;
    }

    public String getModaliteGroupe() { return modaliteGroupe; }
    public void setModaliteGroupe(String modaliteGroupe) { this.modaliteGroupe = modaliteGroupe; }

    public int getNumeroGroupe() { return numeroGroupe; }
    public void setNumeroGroupe(int numeroGroupe) { this.numeroGroupe = numeroGroupe; }

    public String getSpecialiteGroupe() { return specialiteGroupe; }
    public void setSpecialiteGroupe(String specialiteGroupe) { this.specialiteGroupe = specialiteGroupe; }

    public int getEffectifGroupe() { return effectifGroupe; }
    public void setEffectifGroupe(int effectifGroupe) { this.effectifGroupe = effectifGroupe; }

    public String getCodeGroupe() { return codeGroupe; }
    public void setCodeGroupe(String codeGroupe) { this.codeGroupe = codeGroupe; }

    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
}