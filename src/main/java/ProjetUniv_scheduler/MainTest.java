package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class MainTest {
    public static void main(String[] args) {
        Configuration configuration = new Configuration().configure();

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            System.out.println("Connexion etablie. Mode : Update.");

            // INITIALISATION DES DAOS
            UfrDAO ufrDAO = new UfrDAO(sessionFactory);
            BatimentDAO batimentDAO = new BatimentDAO(sessionFactory);
            SalleDAO salleDAO = new SalleDAO(sessionFactory);

            /* * TEST 1 & 2 (Anciens) : Commentés pour éviter l'erreur "Duplicate entry '2S'" 
             * car ces données sont déjà présentes en base.
             */
            /*
            UFR ufrSante = new UFR("2S", "Sciences de la Sante");
            Departement deptMed = new Departement("MED", "Medecine");
            ufrSante.ajoutDepartement(deptMed);
            ufrDAO.save(ufrSante);
            
            Batiment batA = new Batiment("Faculte de Medecine", "Nord du Campus", 2, "A", "Enseignement");
            Salle salleLabo = new Salle("A302", 30, "Laboratoire", "Disponible");
            salleLabo.setBatiment(batA);
            batimentDAO.save(batA);
            salleDAO.save(salleLabo);
            */

            // NOVEAU TEST : CHAINE BATIMENT B -> SALLE B101
            System.out.println("\n--- Debut du Test : Creation Batiment B ---");

            // 1. On crée le nouveau bâtiment
            // Ordre : nomBatiment, localisationBatiment, nbEtage, codeBatiment, typeBatiment
            Batiment batB = new Batiment("Faculte des Lettres", "Sud du Campus", 3, "B", "Enseignement");
            
            // 2. On crée la nouvelle salle
            // Ordre : numeroSalle, capacite, categorieSalle, etatSalle
            Salle salleB101 = new Salle("B101", 100, "Amphitheatre", "Disponible");

            // 3. Liaison : On attache la salle au bâtiment B
            salleB101.setBatiment(batB);
            
            // 4. On sauvegarde (Le parent d'abord, puis l'enfant)
            batimentDAO.save(batB);
            salleDAO.save(salleB101);
            System.out.println("Succes : Batiment B et Salle B101 enregistres.");

            // TEST DE LECTURE : On verifie que Hibernate recupere bien le lien
            Salle checkSalle = salleDAO.findById("B101");
            if (checkSalle != null && checkSalle.getBatiment() != null) {
                System.out.println("Verification Java :");
                System.out.println("-> Salle : " + checkSalle.getNumeroSalle());
                System.out.println("-> Dans le Batiment : " + checkSalle.getBatiment().getNomBatiment());
                System.out.println("-> Code Batiment (Clé étrangère) : " + checkSalle.getBatiment().getCodeBatiment());
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'execution : " + e.getMessage());
            e.printStackTrace();
        }
        
     // Test d'envoi d'email
        EmailUtil.envoyerEmail("elhadjimalicksy18@gmail.com", "Test de Notification", 
                               "Ceci est un test pour le projet UNIV-SCHEDULER.");
    }
}