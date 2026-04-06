package ProjetUniv_scheduler;

import jakarta.persistence.*; // Pour les annotations @Entity, @Id, etc.
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @Column(name = "id_notification")
    private String idNotification;
    
    private String message;
    
    @Column(name = "date_heure")
    private LocalDateTime dateHeure;
    private LocalDateTime dateNotification;
    
    private String statut;    // ex: "LU", "NON_LU"
    private String typeCanal; // ex: "INTERFACE", "SYSTEME"

    // 1. Constructeur par défaut : OBLIGATOIRE pour Hibernate
    // Sans lui, Hibernate ne peut pas recréer l'objet quand il lit la base
    public Notification() {
    }

    // 2. Ton constructeur personnalisé pour l'application
    public Notification(String idNotification, String message, String statut, String typeCanal) {
        this.idNotification = idNotification;
        this.message = message;
        this.dateHeure = LocalDateTime.now(); // Génère l'heure actuelle automatiquement
        this.statut = statut;
        this.typeCanal = typeCanal;
    }

    // --- Getters et Setters ---
    public String getIdNotification() { return idNotification; }
    public void setIdNotification(String idNotification) { this.idNotification = idNotification; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTypeCanal() { return typeCanal; }
    public void setTypeCanal(String typeCanal) { this.typeCanal = typeCanal; }
}