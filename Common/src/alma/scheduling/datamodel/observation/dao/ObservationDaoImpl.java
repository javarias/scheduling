package alma.scheduling.datamodel.observation.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.observation.CreatedArray;
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
	public void save(CreatedArray array) {
		getHibernateTemplate().save(array);
	}

	@Override
	@Transactional(readOnly=false)
	public void update(CreatedArray array) {
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

	@Override
	@Transactional(readOnly=true)
	public int getNumberOfExecutionsForSb(String SbUid) {
		DetachedCriteria crit = DetachedCriteria.forClass(ExecBlock.class);
		crit.setProjection(Projections.count("execBlockUid"));
		crit.add(Restrictions.eq("schedBlockUid", SbUid));
		return (Integer) getHibernateTemplate().findByCriteria(crit).get(0);
	}

	@Override
	public double getAccumulatedObservingTimeForSb(String SbUid) {
		DetachedCriteria crit = DetachedCriteria.forClass(ExecBlock.class);
		crit.setProjection(Projections.sum("timeOnSource"));
		crit.add(Restrictions.eq("schedBlockUid", SbUid));
		return (Double) getHibernateTemplate().findByCriteria(crit).get(0);
	}

}
