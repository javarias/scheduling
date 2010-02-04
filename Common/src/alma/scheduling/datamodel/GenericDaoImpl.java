package alma.scheduling.datamodel;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implementation of {@link GenericDao}
 * 
 * @author javarias
 *
 */
public abstract class GenericDaoImpl extends HibernateDaoSupport implements GenericDao {

    @Override
    public <T> void delete(T obj) {
        getHibernateTemplate().delete(obj);
    }

    @Override
    public <T> void saveOrUpdate(T obj) {
        getHibernateTemplate().saveOrUpdate(obj);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, PK extends Serializable> T findById(Class<T> obj, PK key) {
        return (T) getHibernateTemplate().load(obj, key);
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> executeNamedQuery(String queryName, Object[] queryArgs) {
        Query namedQuery = getSession().getNamedQuery(queryName);
        String[] params = namedQuery.getNamedParameters();
        for(int i = 0; i< params.length; i++){
            Object arg = queryArgs[i];
            namedQuery.setParameter(i, arg);
        }
        return (List<T>)namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> findAll(Class<T> obj) {
        return (List<T>)getHibernateTemplate().find("from " + obj.getName());
    }

}
