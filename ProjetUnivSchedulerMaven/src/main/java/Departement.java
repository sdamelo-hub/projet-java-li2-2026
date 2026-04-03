

import java.util.ArrayList;
import java.util.List;

public class Departement {
    private String nomDepartement;
    private String codeDepartement;
    private List<Filiere> filieres;

    public Departement(String codeDepartement, String nomDepartement) {
        this.codeDepartement = codeDepartement;
        this.nomDepartement = nomDepartement;
        this.filieres = new ArrayList<>();
    }

    /**
     * Retourne la liste des filières rattachées au département.
     * Le type de retour a été modifié en List pour une meilleure manipulation logicielle.
     */
    public List<Filiere> listerFilieres() {
        return new ArrayList<>(this.filieres);
    }

    public void ajouterFiliere(Filiere filiere) {
        if (filiere != null) {
            this.filieres.add(filiere);
        }
    }

    public String getNomDepartement() {
        return nomDepartement;
    }

    public void setNomDepartement(String nomDepartement) {
        this.nomDepartement = nomDepartement;
    }

    public String getCodeDepartement() {
        return codeDepartement;
    }

    public void setCodeDepartement(String codeDepartement) {
        this.codeDepartement = codeDepartement;
    }
}