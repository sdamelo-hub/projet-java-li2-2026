package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class CreneauDAO extends BaseDAO<Creneau>{
	public CreneauDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Creneau.class);
	}
}
