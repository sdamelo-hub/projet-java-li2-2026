package ProjetUniv_scheduler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.Collections;
import java.util.List;

public class CoursDAO extends BaseDAO<Cours> {

    public CoursDAO(SessionFactory sf) {
        super(sf, Cours.class);
    }

    public List<Cours> findAll() {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("FROM Cours ORDER BY codeCours", Cours.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Cours findByCode(String code) {
        try (Session s = sessionFactory.openSession()) {
            return s.createQuery("FROM Cours c WHERE c.codeCours = :code", Cours.class)
                    .setParameter("code", code).uniqueResult();
        } catch (Exception e) { return null; }
    }
}
