package ProjetUniv_scheduler;

import org.hibernate.Session;
import java.util.List;

public class UtilisateurDAO extends BaseDAO<Utilisateur> {

    public UtilisateurDAO() {
        super(HibernateUtil.getSessionFactory(), Utilisateur.class);
    }

    public List<Utilisateur> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Utilisateur", Utilisateur.class).list();
        } catch (Exception e) {
            System.err.println("❌ findAll Utilisateur : " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    public Utilisateur findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Utilisateur u WHERE u.email = :email", Utilisateur.class)
                .setParameter("email", email)
                .uniqueResult();
        } catch (Exception e) {
            System.err.println("❌ findByEmail : " + e.getMessage());
            return null;
        }
    }

    /**
     * Recherche par identifiantConnexion OU email.
     * Logs détaillés pour diagnostiquer les problèmes de connexion en console Eclipse.
     */
    public Utilisateur findByIdentifiant(String identifiant) {
        System.out.println("🔍 Recherche utilisateur pour identifiant : [" + identifiant + "]");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Utilisateur u = session.createQuery(
                "FROM Utilisateur u WHERE u.identifiantConnexion = :id OR u.email = :id",
                Utilisateur.class)
                .setParameter("id", identifiant)
                .uniqueResult();

            if (u != null) {
                System.out.println("✅ Trouvé : " + u.getNom()
                    + " | Rôle : [" + u.getRole() + "]"
                    + " | MDP stocké : [" + u.getMotDePasse() + "]");
            } else {
                System.out.println("❌ Aucun utilisateur pour [" + identifiant + "]. Vérifiez la table 'utilisateurs'.");
            }
            return u;

        } catch (Exception e) {
            System.err.println("❌ ERREUR findByIdentifiant : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
