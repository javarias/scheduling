package alma.scheduling.datamodel;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

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

}
