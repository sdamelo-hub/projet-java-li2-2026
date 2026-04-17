package ProjetUniv_scheduler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.Collections;
import java.util.List;

public class ReservationDAO extends BaseDAO<Reservation> {

    public ReservationDAO(SessionFactory sf) {
        super(sf, Reservation.class);
    }

    public List<Reservation> findAll() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery(
                "FROM Reservation r LEFT JOIN FETCH r.monCreneau ORDER BY r.dateHeureReservation DESC",
                Reservation.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Reservation> findByEtat(String etat) {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery(
                "FROM Reservation r WHERE r.etatReservation = :etat",
                Reservation.class)
                .setParameter("etat", etat).list();
        } catch (Exception e) { return Collections.emptyList(); }
    }
}
