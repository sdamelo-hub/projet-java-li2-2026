package ProjetUniv_scheduler.Mesclasses;


import java.time.LocalDateTime;

public class Notification {
    private String idNotification;
    private String message;
    private LocalDateTime dateHeure;
    private String statut;
    private String typeCanal;

    public Notification(String idNotification, String message, String statut, String typeCanal) {
        this.idNotification = idNotification;
        this.message = message;
        this.dateHeure = LocalDateTime.now();
        this.statut = statut;
        this.typeCanal = typeCanal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTypeCanal() {
        return typeCanal;
    }

    public void setTypeCanal(String typeCanal) {
        this.typeCanal = typeCanal;
    }

    public String getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(String idNotification) {
        this.idNotification = idNotification;
    }
}