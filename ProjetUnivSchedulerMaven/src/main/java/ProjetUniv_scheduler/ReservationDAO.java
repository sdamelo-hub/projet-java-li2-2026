package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;
public class ReservationDAO extends BaseDAO<Reservation>{
	public ReservationDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Reservation.class);
	}
}
