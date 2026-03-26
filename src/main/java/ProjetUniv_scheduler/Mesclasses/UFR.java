package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import ProjetUniv_scheduler.Mesclasses.Departement;

@Entity
@Table(name = "ufrs")
public class UFR {
    
    @Id
    private String codeUFR;
    
    private String libelleUFR;

    @OneToMany(mappedBy = "ufr", cascade = CascadeType.ALL)
    private List<Departement> departements;

    // Constructeur par défaut indispensable pour Hibernate
    public UFR() {
        this.departements = new ArrayList<>();
    }

    public UFR(String codeUFR, String libelleUFR) {
        this.codeUFR = codeUFR;
        this.libelleUFR = libelleUFR;
        this.departements = new ArrayList<>();
    }

    public void ajoutDepartement(Departement dept) {
        if (dept != null) {
            this.departements.add(dept);
            dept.setUfr(this); 
        }
    }

    public String getLibelleUFR() { return libelleUFR; }
    public void setLibelleUFR(String libelleUFR) { this.libelleUFR = libelleUFR; }

    public String getCodeUFR() { return codeUFR; }
    public void setCodeUFR(String codeUFR) { this.codeUFR = codeUFR; }

    public List<Departement> getDepartements() {
        return new ArrayList<>(departements);
    }
}