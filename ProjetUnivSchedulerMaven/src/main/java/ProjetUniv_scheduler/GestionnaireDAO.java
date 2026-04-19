package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class GestionnaireDAO extends BaseDAO<Gestionnaire>{
	public GestionnaireDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Gestionnaire.class);
	}
}