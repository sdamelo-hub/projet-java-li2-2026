package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class FiliereDAO extends BaseDAO<Filiere>{
	public FiliereDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Filiere.class);
	}
}