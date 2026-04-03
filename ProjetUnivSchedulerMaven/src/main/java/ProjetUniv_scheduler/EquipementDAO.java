package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class EquipementDAO extends BaseDAO<Equipement>{
	public EquipementDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Equipement.class);
	}
}