package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;
public class AdministrateurDAO extends BaseDAO<Administrateur>{
	public AdministrateurDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Administrateur.class);
	}
}
