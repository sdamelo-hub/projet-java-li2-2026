

public abstract class Utilisateur {
    private String nom;
    private String prenom;
    private String identifiantConnexion;
    private String email;
    private String motDePasse;

    public Utilisateur(String identifiantConnexion, String motDePasse) {
        this.identifiantConnexion = identifiantConnexion;
        this.motDePasse = motDePasse;
    }

    public Utilisateur(String nom, String prenom, String identifiantConnexion, String email, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.identifiantConnexion = identifiantConnexion;
        this.email = email;
        this.motDePasse = motDePasse;
    }
   
    public boolean seConnecter() {
        // Logique de vérification dans la base MariaDB
        return true; 
    }

    public boolean seDeconnecter() {
        // Logique de destruction de session
        return true;
    }

    // Accesseurs et Mutateurs (Getters & Setters)
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getIdentifiantConnexion() { return identifiantConnexion; }
    public void setIdentifiantConnexion(String identifiantConnexion) { this.identifiantConnexion = identifiantConnexion; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}