package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class DepartementDAO extends BaseDAO<Departement>{
	public DepartementDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Departement.class);
	}
}
