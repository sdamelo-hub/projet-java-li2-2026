package ProjetUniv_scheduler.Mesclasses;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import ProjetUniv_scheduler.Mesclasses.UFR;
import ProjetUniv_scheduler.Mesclasses.Filiere;

@Entity
@Table(name = "departements")
public class Departement {

    @Id
    private String codeDepartement;
    
    private String nomDepartement;

    @ManyToOne
    @JoinColumn(name = "code_ufr")
    private UFR ufr;

    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL)
    private List<Filiere> filieres;

    // Constructeur par défaut obligatoire pour Hibernate
    public Departement() {
        this.filieres = new ArrayList<>();
    }

    public Departement(String codeDepartement, String nomDepartement) {
        this.codeDepartement = codeDepartement;
        this.nomDepartement = nomDepartement;
        this.filieres = new ArrayList<>();
    }

    public List<Filiere> listerFilieres() {
        return new ArrayList<>(this.filieres);
    }

    public void ajouterFiliere(Filiere filiere) {
        if (filiere != null) {
            this.filieres.add(filiere);
            filiere.setDepartement(this);
        }
    }

    // Accesseurs et Mutateurs
    public String getNomDepartement() { return nomDepartement; }
    public void setNomDepartement(String nomDepartement) { this.nomDepartement = nomDepartement; }

    public String getCodeDepartement() { return codeDepartement; }
    public void setCodeDepartement(String codeDepartement) { this.codeDepartement = codeDepartement; }

    public UFR getUfr() { return ufr; }
    public void setUfr(UFR ufr) { this.ufr = ufr; }

    public List<Filiere> getFilieres() { return filieres; }
    public void setFilieres(List<Filiere> filieres) { this.filieres = filieres; }
}