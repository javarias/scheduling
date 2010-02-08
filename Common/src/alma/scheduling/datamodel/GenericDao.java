package alma.scheduling.datamodel;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;


/**
 * Generic interface with common functions to manage data managed with Hibernate.
 * 
 * The implementation of the interface also should inherit {@link HibernateDaoSupport}
 * 
 * @author javarias
 *
 */
public interface GenericDao {

    /**
     * The implementation must save the object if it is new or update if the object exist already
     * 
     * @param <T> the Type of the Object
     * @param obj the object to be stored or updated
     */
    public <T> void saveOrUpdate(T obj);
    
    /**
     * he implementation must save an entire list of objects
     * 
     * @param <T>
     * @param objs
     */
    public <T> void saveOrUpdate(Collection<T> objs);
    
    /**
     * The implementation must delete the given object
     * 
     * @param <T> the type of the object to be deleted
     * @param obj the object to be deleted
     */
    public <T> void delete(T obj);
    
    
    /**
     * The implementation must retrieve all the object of the type given 
     * 
     * @param <T> the type of the objects to be retrieved
     * @param obj the class of the objects to be retrieved 
     * @return a List of the objects found
     */
    public <T> List<T> findAll(Class<T> obj);
    
    /**
     * The implementation must return an object with the given key value
     * 
     * @param <T> The type of the returned type
     * @param <PK> The type of the key
     * @param obj The class type of the object to be searched
     * @param key the primary key of the object
     * @return the object of the type T with the corresponding key. Null if the object doesn't exist
     */
    public <T, PK extends Serializable> T findById(Class<T> obj, PK key);
    
    /**
     * The Implementation must execute the query (queryName) with the given
     * parameters (queryArgs).
     * 
     * @param <T> The Type of elements to be returned
     * @param queryName the string of the named query defined in the mapping xml or in the annotations
     * @param queryArgs the parameters for the query.
     * @return a list of the resulted object after execute the query
     * @throws HibernateException
     */
    public <T> List<T> executeNamedQuery(String queryName, final Object[] queryArgs);

}
