package ProjetUniv_scheduler;

import org.hibernate.SessionFactory;

public class PromotionDAO extends BaseDAO<Promotion>{
	public PromotionDAO(SessionFactory SessionFactory) {
		super(SessionFactory, Promotion.class);
	}
}