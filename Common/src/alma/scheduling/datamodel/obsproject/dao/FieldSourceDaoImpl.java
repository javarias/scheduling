package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.obsproject.FieldSource;

@Transactional
public class FieldSourceDaoImpl extends GenericDaoImpl implements FieldSourceDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<FieldSource> getSourcesWithoutRiseAndSetTimes() {
		Query query = getSession().createQuery("from FieldSource fs where fs.observability is null");
		return query.list();
	}

	@Override
	public int getNumberOfSourcesWithoutUpdate() {
		Query query = getSession().createQuery("select count(*) from FieldSource fs where fs.observability is null");
		return ((Long)query.uniqueResult()).intValue();
	}
}
