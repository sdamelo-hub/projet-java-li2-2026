package ProjetUniv_scheduler;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    // --- À REMPLIR AVEC TES INFOS ---
    private static final String MON_EMAIL = "elhadjimalicksy18@gmail.com"; 
    private static final String MON_MOT_DE_PASSE_APP = "rzbo rfyz fwaz kcyx"; // Tes 16 caractères
    // --------------------------------

    public static void envoyerEmail(String destinataire, String sujet, String messageTexte) {
        
        // 1. Configuration du serveur SMTP de Google
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 2. Création de la session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MON_EMAIL, MON_MOT_DE_PASSE_APP);
            }
        });

        try {
            // 3. Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MON_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("[UNIV-SCHEDULER] " + sujet);
            message.setText(messageTexte);

            // 4. Envoi réel
            Transport.send(message);
            System.out.println("Email envoyé avec succès à : " + destinataire);

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }
}