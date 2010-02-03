package alma.scheduling.datamodel;

import java.io.Serializable;
import java.util.List;

public interface GenericDao {

    public <T> void saveOrUpdate(T obj);
    
    public <T> void delete(T obj);
    
    public <T, PK extends Serializable> T findById(Class<T> obj, PK key);
    
    public <T> List<T> executeNamedQuery(String queryName, final Object[] queryArgs);
}
