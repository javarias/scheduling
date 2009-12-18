package alma.scheduling.datamodel.dao;

import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public abstract class AbstractHibernateDao extends HibernateDaoSupport {

	public AbstractHibernateDao() {
	}

	protected void saveOrUpdate(Object obj) {
		getHibernateTemplate().saveOrUpdate(obj);
	}

	protected void delete(Object obj) {
		getHibernateTemplate().delete(obj);
	}

	protected Object find(Class<?> obj, Long id) {
		return getHibernateTemplate().load(obj, id);
	}

	protected List findAll(Class obj) {
		return getHibernateTemplate().find("from " + obj.getName());
	}

}
