package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class IncidentDAO extends BaseDAO<Incident>{
	public IncidentDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Incident.class);
	}
}