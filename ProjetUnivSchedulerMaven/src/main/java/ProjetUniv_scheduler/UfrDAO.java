package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class UfrDAO extends BaseDAO<UFR> {
    public UfrDAO(SessionFactory sessionFactory) {
        super(sessionFactory, UFR.class);
    }
    
    // Vous pouvez ajouter ici des méthodes spécifiques à l'UFR si besoin
}