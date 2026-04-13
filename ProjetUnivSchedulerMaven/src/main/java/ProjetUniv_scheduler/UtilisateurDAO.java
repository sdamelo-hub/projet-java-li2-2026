package ProjetUniv_scheduler;

import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

public class UtilisateurDAO extends BaseDAO<Utilisateur> {

    public UtilisateurDAO() {
        // On récupère automatiquement la SessionFactory depuis HibernateUtil
        super(HibernateUtil.getSessionFactory(), Utilisateur.class);
    }

    /**
     * Récupère tous les utilisateurs pour le TableView.
     */
    public List<Utilisateur> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Utilisateur", Utilisateur.class).list();
        } catch (Exception e) {
            System.err.println("Erreur findAll Utilisateur : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recherche par email pour l'authentification ou la vérification.
     */
    public Utilisateur findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery("from Utilisateur where email = :email", Utilisateur.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Erreur findByEmail : " + e.getMessage());
            return null;
        }
    }
}