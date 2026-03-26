package ProjetUniv_scheduler.Mesclasses;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import java.io.File;
import java.util.List;

/**
 * DAO simplifié pour les tests.
 */
public class SimpleDAO {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            try {
                sessionFactory = new Configuration()
                        .configure(new File("resources/hibernate.cfg.xml"))
                        .buildSessionFactory();
            } catch (Exception e) {
                throw new RuntimeException("Erreur initialisation Hibernate : " + e.getMessage(), e);
            }
        }
        return sessionFactory;
    }

    public static void fermer() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    /** Sauvegarde ou met à jour un objet */
    public static <T> boolean sauvegarder(T objet) {
        Transaction tx = null;
        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.saveOrUpdate(objet);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Erreur sauvegarde : " + e.getMessage());
            return false;
        }
    }

    /** Charge un objet par son ID */
    public static <T> T charger(Class<T> clazz, Object id) {
        try (Session session = getSessionFactory().openSession()) {
            return session.get(clazz, (java.io.Serializable) id);
        } catch (Exception e) {
            System.err.println("Erreur chargement : " + e.getMessage());
            return null;
        }
    }

    /** Authentification */
    public static Utilisateur authentifier(String identifiant, String motDePasse) {
        try (Session session = getSessionFactory().openSession()) {
            return (Utilisateur) session.createQuery(
                            "FROM Utilisateur u WHERE u.identifiantConnexion = :id AND u.motDePasse = :mdp AND u.actif = true")
                    .setParameter("id", identifiant)
                    .setParameter("mdp", motDePasse)
                    .uniqueResult();
        } catch (Exception e) {
            System.err.println("Erreur authentification : " + e.getMessage());
            return null;
        }
    }
}
