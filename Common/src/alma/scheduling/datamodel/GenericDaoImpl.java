package alma.scheduling.datamodel;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implementation of {@link GenericDao}
 * 
 * @author javarias
 *
 */
public class GenericDaoImpl extends HibernateDaoSupport implements GenericDao {

    protected HibernateTemplate template;
    protected Session session;
    
    public GenericDaoImpl(){
        template = getHibernateTemplate();
        session = getSession();
    }
    
    @Override
    public <T> void delete(T obj) {
        template.delete(obj);
    }

    @Override
    public <T> void saveOrUpdate(T obj) {
        template.saveOrUpdate(obj);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, PK extends Serializable> T findById(Class<T> obj, PK key) {
        return (T) template.load(obj, key);
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> executeNamedQuery(String queryName, Object[] queryArgs) {
        Query namedQuery = session.getNamedQuery(queryName);
        String[] params = namedQuery.getNamedParameters();
        for(int i = 0; i< params.length; i++){
            Object arg = queryArgs[i];
            namedQuery.setParameter(i, arg);
        }
        return (List<T>)namedQuery.list();
    }

}
