package alma.scheduling.datamodel.observation.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.observation.Array;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observation.Session;

public class ObservationDaoImpl extends GenericDaoImpl implements GenericDao,
		ObservationDao {

	@Override
	@Transactional(readOnly=false)
	public void save(ExecBlock eb) {
		getHibernateTemplate().save(eb);
	}

	@Override
	@Transactional(readOnly=false)
	public void save(Array array) {
		getHibernateTemplate().save(array);
	}

	@Override
	@Transactional(readOnly=false)
	public void update(Array array) {
		getHibernateTemplate().update(array);
	}

	@Override
	@Transactional(readOnly=false)
	public void save(Session session) {
		getHibernateTemplate().save(session);
	}

	@Override
	@Transactional(readOnly=false)
	public void update(Session session) {
		getHibernateTemplate().update(session);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<ExecBlock> getAllExecBlocksForSB(String SbUid) {
		DetachedCriteria crit = DetachedCriteria.forClass(ExecBlock.class);
		crit.add(Restrictions.eq("schedBlockUid", SbUid));
		return getHibernateTemplate().findByCriteria(crit);
	}

}
