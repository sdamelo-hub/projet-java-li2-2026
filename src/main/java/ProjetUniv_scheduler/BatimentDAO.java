package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class BatimentDAO extends BaseDAO<Batiment>{
	public BatimentDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Batiment.class);
	}
}
