package ProjetUniv_scheduler.Mesclasses;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import java.io.File;
import java.util.List;
import ProjetUniv_scheduler.Mesclasses.Salle;

/**
 * Service central DAO — toutes les opérations CRUD passent ici.
 * Utilise le pattern Singleton pour la SessionFactory.
 */
public class UnivSchedulerDAO {

    private static SessionFactory sessionFactory;

    // ── Initialisation ───────────────────────────────────────────────────────
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

    // ── Méthodes génériques ──────────────────────────────────────────────────

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

    /** Supprime un objet */
    public static <T> boolean supprimer(T objet) {
        Transaction tx = null;
        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(objet);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Erreur suppression : " + e.getMessage());
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

    /** Liste tous les objets d'une classe */
    @SuppressWarnings("unchecked")
    public static <T> List<T> listerTous(Class<T> clazz) {
        try (Session session = getSessionFactory().openSession()) {
            return session.createQuery("FROM " + clazz.getSimpleName()).list();
        } catch (Exception e) {
            System.err.println("Erreur listage : " + e.getMessage());
            return List.of();
        }
    }

    // ── Authentification ─────────────────────────────────────────────────────

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

    // ── Salles disponibles ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static List<Salle> trouverSallesDisponibles(
            java.time.LocalDate date, java.time.LocalTime debut, java.time.LocalTime fin) {
        try (Session session = getSessionFactory().openSession()) {
            String hql = "FROM Salle s WHERE s.etatSalle = 'DISPONIBLE' " +
                    "AND s.numeroSalle NOT IN (" +
                    "  SELECT c.salle.numeroSalle FROM Creneau c " +
                    "  WHERE c.dateSeance = :date " +
                    "  AND c.heureDebut < :fin AND c.heureFin > :debut" +
                    ")";
            return session.createQuery(hql)
                    .setParameter("date", date)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .list();
        } catch (Exception e) {
            System.err.println("Erreur recherche salles : " + e.getMessage());
            return List.of();
        }
    }

    // ── Conflits ─────────────────────────────────────────────────────────────

    public static boolean detecterConflitSalle(String numeroSalle,
                                               java.time.LocalDate date, java.time.LocalTime debut, java.time.LocalTime fin, String idCreneauExclure) {
        try (Session session = getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(c) FROM Creneau c " +
                    "WHERE c.salle.numeroSalle = :salle " +
                    "AND c.dateSeance = :date " +
                    "AND c.heureDebut < :fin AND c.heureFin > :debut " +
                    (idCreneauExclure != null ? "AND c.idCreneau != :exclu" : "");
            org.hibernate.query.Query<Long> q = session.createQuery(hql, Long.class)
                    .setParameter("salle", numeroSalle)
                    .setParameter("date", date)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin);
            if (idCreneauExclure != null) q.setParameter("exclu", idCreneauExclure);
            return q.uniqueResult() > 0;
        } catch (Exception e) {
            System.err.println("Erreur détection conflit : " + e.getMessage());
            return false;
        }
    }

    // ── Statistiques ─────────────────────────────────────────────────────────

    public static long compterSalles() {
        try (Session session = getSessionFactory().openSession()) {
            return (Long) session.createQuery("SELECT COUNT(s) FROM Salle s").uniqueResult();
        } catch (Exception e) { return 0; }
    }

    public static long compterCours() {
        try (Session session = getSessionFactory().openSession()) {
            return (Long) session.createQuery("SELECT COUNT(c) FROM Cours c").uniqueResult();
        } catch (Exception e) { return 0; }
    }

    public static long compterReservations() {
        try (Session session = getSessionFactory().openSession()) {
            return (Long) session.createQuery("SELECT COUNT(r) FROM Reservation r").uniqueResult();
        } catch (Exception e) { return 0; }
    }

    public static long compterUtilisateurs() {
        try (Session session = getSessionFactory().openSession()) {
            return (Long) session.createQuery("SELECT COUNT(u) FROM Utilisateur u").uniqueResult();
        } catch (Exception e) { return 0; }
    }
}
