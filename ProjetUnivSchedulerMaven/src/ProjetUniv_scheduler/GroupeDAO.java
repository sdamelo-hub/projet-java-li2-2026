package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class GroupeDAO extends BaseDAO<Groupe>{
	public GroupeDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Groupe.class);
	}
}