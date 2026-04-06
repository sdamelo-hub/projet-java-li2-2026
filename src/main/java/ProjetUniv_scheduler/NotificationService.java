package ProjetUniv_scheduler;

import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Service gérant la logique des notifications (Email + Base de données).
 * Conforme au cahier des charges UNIV-SCHEDULER.
 */
public class NotificationService {

    private final NotificationDAO notificationDAO;
    // L'email de l'administrateur pour les alertes incidents
    private static final String ADMIN_EMAIL = "admin.univ@gmail.com"; 

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    /**
     * Cas 1 : Notification de changement de salle (Email + DB)
     */
    public void notifierChangementSalle(String emailDestinataire, String nomCours, String ancienneSalle, String nouvelleSalle) {
        String sujet = "Changement de salle : " + nomCours;
        String corps = "Attention, votre cours de " + nomCours + " initialement prévu en " 
                     + ancienneSalle + " a été déplacé en salle " + nouvelleSalle + ".";

        // 1. Envoi de l'email réel
        EmailUtil.envoyerEmail(emailDestinataire, sujet, corps);

        // 2. Sauvegarde en base de données
        enregistrerNotification(corps, "EMAIL", "NON_LU");
    }

    /**
     * Cas 2 : Confirmation de réservation réussie
     */
    public void confirmerReservation(String emailDestinataire, String detailsCours) {
        String sujet = "Confirmation de réservation";
        String corps = "Votre réservation a été validée avec succès : " + detailsCours;

        EmailUtil.envoyerEmail(emailDestinataire, sujet, corps);
        enregistrerNotification(corps, "EMAIL", "LU");
    }

    /**
     * Cas 3 : Alerte de conflit (Système uniquement)
     */
    public void notifierConflit(String detailConflit) {
        String msg = "CONFLIT : " + detailConflit;
        
        // Pas d'email ici pour ne pas saturer la boîte, juste une alerte système
        enregistrerNotification(msg, "SYSTEME", "NON_LU");
        System.out.println("[SYSTÈME] Log de conflit généré : " + msg);
    }

    /**
     * Cas 4 : Signalement d'un incident technique (Alerte Admin)
     */
    public void notifierIncidentTechnique(String typeIncident, String salle) {
        String sujet = "ALERTE INCIDENT : Salle " + salle;
        String corps = "Un problème de type '" + typeIncident + "' a été signalé en salle " + salle + ". Intervention requise.";

        // On prévient l'admin par mail
        EmailUtil.envoyerEmail(ADMIN_EMAIL, sujet, corps);
        
        // On log l'incident dans les notifications
        enregistrerNotification(corps, "DASHBOARD", "NON_LU");
    }

    /**
     * Méthode privée pour centraliser la création de l'objet Notification
     */
    private void enregistrerNotification(String message, String canal, String statut) {
        Notification n = new Notification();
        n.setIdNotification(UUID.randomUUID().toString());
        n.setMessage(message);
        // On utilise les noms exacts de tes setters dans Notification.java
        n.setDateHeure(LocalDateTime.now()); 
        n.setStatut(statut);
        n.setTypeCanal(canal); 
        
        notificationDAO.save(n);
    }
}