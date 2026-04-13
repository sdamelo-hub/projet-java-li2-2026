package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class EnseignantDAO extends BaseDAO<Enseignant>{
	public EnseignantDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Enseignant.class);
	}
}