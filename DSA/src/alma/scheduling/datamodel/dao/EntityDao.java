package alma.scheduling.datamodel.dao;

import java.util.List;

import alma.scheduling.datamodel.Entity;

public class EntityDao extends AbstractHibernateDao {


	public void delete(Entity e) {
		super.delete(e);
	}

	public Object find(Long id) {
		return super.find(Entity.class, id);
	}

	public List<Entity> findAll() {
		return super.findAll(Entity.class);
	}

	public void saveOrUpdate(Entity e) {
		super.saveOrUpdate(e);
	}
	
}
