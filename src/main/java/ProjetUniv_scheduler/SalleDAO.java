package ProjetUniv_scheduler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;
import java.util.Collections;

public class SalleDAO extends BaseDAO<Salle> {

    // 1. Constructeur utilisé par ton MainController (sans paramètres)
    public SalleDAO() {
        // On appelle le constructeur parent avec la SessionFactory globale
        // Assure-toi que ta classe HibernateUtil.getSessionFactory() existe
        super(HibernateUtil.getSessionFactory(), Salle.class);
    }

    // 2. Constructeur spécifique (si tu en as besoin ailleurs)
    public SalleDAO(SessionFactory sessionFactory) {
        super(sessionFactory, Salle.class);
    }

    /**
     * Récupère toutes les salles de la base de données.
     * Cette méthode est appelée par ton MainController.
     */
    public List<Salle> getAll() {
        try (Session session = sessionFactory.openSession()) {
            // Requête HQL pour récupérer toutes les entités Salle
            return session.createQuery("from Salle", Salle.class).list();
        } catch (Exception e) {
            System.err.println("Erreur dans SalleDAO.getAll() : " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}