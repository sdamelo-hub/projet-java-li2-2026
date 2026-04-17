package ProjetUniv_scheduler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CORRECTIFS :
 *  + findByWeek(LocalDate lundi)          — tous les créneaux d'une semaine
 *  + findBySalleAndDate(String, LocalDate) — créneaux d'une salle un jour donné
 *  + findAll()                            — hérité de BaseDAO, conservé ici pour clarté
 */
public class CreneauDAO extends BaseDAO<Creneau> {

    public CreneauDAO(SessionFactory sessionFactory) {
        super(sessionFactory, Creneau.class);
    }

    // ── Tous les créneaux ────────────────────────────────────────────────────

    @Override
    public List<Creneau> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Creneau", Creneau.class).getResultList();
        } catch (Exception e) {
            System.err.println("❌ CreneauDAO.findAll : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Semaine complète (Lun → Dim) ─────────────────────────────────────────
    /**
     * Retourne tous les créneaux dont la date est comprise dans la semaine
     * débutant le lundi {@code mondayOfWeek}.
     *
     * @param mondayOfWeek le lundi de la semaine (ex: LocalDate.now().with(DayOfWeek.MONDAY))
     */
    public List<Creneau> findByWeek(LocalDate mondayOfWeek) {
        if (mondayOfWeek == null) return new ArrayList<>();
        LocalDate endOfWeek = mondayOfWeek.plusDays(6);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Creneau c WHERE c.dateSeance >= :debut AND c.dateSeance <= :fin",
                Creneau.class)
                .setParameter("debut", mondayOfWeek)
                .setParameter("fin",   endOfWeek)
                .getResultList();
        } catch (Exception e) {
            System.err.println("❌ CreneauDAO.findByWeek : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Créneaux d'une salle un jour donné ───────────────────────────────────
    /**
     * Retourne tous les créneaux affectés à {@code numeroSalle} pour la {@code date} donnée.
     * Utilisé pour la détection de conflits en temps réel.
     *
     * @param numeroSalle numéro de la salle (PK)
     * @param date        la date de la séance
     */
    public List<Creneau> findBySalleAndDate(String numeroSalle, LocalDate date) {
        if (numeroSalle == null || date == null) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Creneau c WHERE c.salle.numeroSalle = :salle AND c.dateSeance = :date",
                Creneau.class)
                .setParameter("salle", numeroSalle)
                .setParameter("date",  date)
                .getResultList();
        } catch (Exception e) {
            System.err.println("❌ CreneauDAO.findBySalleAndDate : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Créneaux d'un cours ───────────────────────────────────────────────────
    public List<Creneau> findByCours(String codeCours) {
        if (codeCours == null) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Creneau c WHERE c.cours.codeCours = :code",
                Creneau.class)
                .setParameter("code", codeCours)
                .getResultList();
        } catch (Exception e) {
            System.err.println("❌ CreneauDAO.findByCours : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
