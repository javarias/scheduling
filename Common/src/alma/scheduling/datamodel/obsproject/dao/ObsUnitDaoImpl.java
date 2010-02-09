package alma.scheduling.datamodel.obsproject.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ObsUnitDaoImpl extends HibernateDaoSupport implements ObsUnitDao {

    public ObsUnitDaoImpl() {        
    }
    
    public void save(Object domainObject) {
        getHibernateTemplate().save(domainObject);
    }
}
