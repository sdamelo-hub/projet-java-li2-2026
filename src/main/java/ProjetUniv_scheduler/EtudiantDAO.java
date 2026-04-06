package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class EtudiantDAO extends BaseDAO<Etudiant>{
	public EtudiantDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Etudiant.class);
	}
}