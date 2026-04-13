package ProjetUniv_scheduler;

import org.hibernate.Session;
import java.util.List;

public class EquipementDAO extends BaseDAO<Equipement> {
    
    /**
     * Constructeur sans argument.
     * Il récupère automatiquement la SessionFactory depuis HibernateUtil.
     */
    public EquipementDAO() {
        super(HibernateUtil.getSessionFactory(), Equipement.class);
    }

    /**
     * Récupère la liste de tout le matériel pour l'inventaire.
     */
    public List<Equipement> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Requête HQL pour récupérer tous les équipements
            return session.createQuery("from Equipement", Equipement.class).list();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'inventaire : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}