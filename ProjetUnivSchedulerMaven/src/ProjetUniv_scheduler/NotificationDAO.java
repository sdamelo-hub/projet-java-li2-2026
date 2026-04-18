package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class NotificationDAO extends BaseDAO<Notification>{
	public NotificationDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Notification.class);
	}
}