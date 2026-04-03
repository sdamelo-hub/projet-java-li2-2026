

import java.util.ArrayList;
import java.util.List;

public class UFR {
    private String libelleUFR;
    private String codeUFR;
    private List<Departement> departements;

    public UFR(String codeUFR, String libelleUFR) {
        this.codeUFR = codeUFR;
        this.libelleUFR = libelleUFR;
        this.departements = new ArrayList<>();
    }

    /**
     * Ajoute un département à l'UFR. 
     * Cette méthode assure la gestion de la relation de composition.
     */
    public void ajoutDepartement(Departement dept) {
        if (dept != null) {
            this.departements.add(dept);
        }
    }

    public String getLibelleUFR() {
        return libelleUFR;
    }

    public void setLibelleUFR(String libelleUFR) {
        this.libelleUFR = libelleUFR;
    }

    public String getCodeUFR() {
        return codeUFR;
    }

    public void setCodeUFR(String codeUFR) {
        this.codeUFR = codeUFR;
    }

    public List<Departement> getDepartements() {
        return new ArrayList<>(departements);
    }
}