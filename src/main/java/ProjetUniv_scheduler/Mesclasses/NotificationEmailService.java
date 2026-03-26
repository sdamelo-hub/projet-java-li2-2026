package ProjetUniv_scheduler.Mesclasses;

/**
 * Service de notification par email.
 * Version simplifiée sans dépendances externes pour le moment.
 */
public class NotificationEmailService {

    // ─── Envoi générique ───────────────────────────────────────────
    public static boolean envoyerEmail(String destinataire, String sujet, String corps) {
        System.out.println("📧 Email simulé - À: " + destinataire + ", Sujet: " + sujet);
        System.out.println("Corps: " + corps);
        return true; // Simulation d'envoi réussi
    }

    // ─── Notifications métier ─────────────────────────────────────────────
    /** Confirmation de réservation */
    public static void notifierConfirmationReservation(Reservation reservation) {
        if (reservation.getReservateur() == null) return;
        String email = reservation.getReservateur().getEmail();
        if (email == null || email.isBlank()) return;

        Creneau c = reservation.getMonCreneau();
        String salle = c != null && c.getSalle() != null ? c.getSalle().getNumeroSalle() : "N/A";
        String date = c != null ? c.getDateSeance().toString() : "N/A";
        String heure = c != null ? c.getHeureDebut() + " - " + c.getHeureFin() : "N/A";
        String cours = c != null && c.getCours() != null ? c.getCours().getIntituleMatiere() : "N/A";

        String sujet = "✅ Réservation confirmée — " + cours;
        String corps = String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre réservation a été confirmée avec succès.\n\n" +
                        "Cours: %s\n" +
                        "Salle: %s\n" +
                        "Date: %s\n" +
                        "Horaire: %s\n" +
                        "Motif: %s\n\n" +
                        "Merci d'utiliser UNIV-SCHEDULER.",
                reservation.getReservateur().getPrenom(),
                reservation.getReservateur().getNom(),
                cours, salle, date, heure,
                reservation.getMotifReservation() != null ? reservation.getMotifReservation() : "-"
        );
        envoyerEmail(email, sujet, corps);
    }

    /** Annulation de réservation */
    public static void notifierAnnulationReservation(Reservation reservation) {
        if (reservation.getReservateur() == null) return;
        String email = reservation.getReservateur().getEmail();
        if (email == null || email.isBlank()) return;

        String sujet = "❌ Réservation annulée — Réf. #" + reservation.getNumReservation();
        String corps = String.format(
                "Bonjour %s %s,\n\n" +
                        "Votre réservation #%d a été annulée.\n" +
                        "Si vous pensez qu'il s'agit d'une erreur, contactez l'administration.\n\n" +
                        "Cordialement,\nL'équipe UNIV-SCHEDULER",
                reservation.getReservateur().getPrenom(),
                reservation.getReservateur().getNom(),
                reservation.getNumReservation()
        );
        envoyerEmail(email, sujet, corps);
    }

    /** Rappel de séance */
    public static void envoyerRappel(Utilisateur utilisateur, Creneau creneau) {
        if (utilisateur == null || utilisateur.getEmail() == null) return;
        String cours = creneau.getCours() != null ? creneau.getCours().getIntituleMatiere() : "Séance";
        String sujet = "⏰ Rappel : " + cours + " demain";
        String corps = String.format(
                "Bonjour %s,\n\n" +
                        "Rappel : vous avez une séance de %s demain.\n" +
                        "Salle: %s | Horaire: %s → %s\n\n" +
                        "Bonne séance !\nUNIV-SCHEDULER",
                utilisateur.getPrenom(),
                cours,
                creneau.getSalle() != null ? creneau.getSalle().getNumeroSalle() : "N/A",
                creneau.getHeureDebut(), creneau.getHeureFin()
        );
        envoyerEmail(utilisateur.getEmail(), sujet, corps);
    }
}
