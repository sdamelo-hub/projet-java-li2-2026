package ProjetUniv_scheduler.Mesclasses;


import javax.persistence.*;

@Entity
@Table(name = "test_hibernate")
public class TestConnexion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String message;

    // Constructeur vide (obligatoire pour Hibernate)
    public TestConnexion() {}

    public TestConnexion(String message) {
        this.message = message;
    }

    // Getters et Setters
    public int getId() { return id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
