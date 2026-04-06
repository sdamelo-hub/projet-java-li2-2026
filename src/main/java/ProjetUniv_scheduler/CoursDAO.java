package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class CoursDAO extends BaseDAO<Cours>{
	public CoursDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Cours.class);
	}
}
