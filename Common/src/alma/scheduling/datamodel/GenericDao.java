package alma.scheduling.datamodel;

import java.io.Serializable;

public interface GenericDao {

    public <T> void saveOrUpdate(T obj);
    
    public <T> void delete(T obj);
    
    public <T, PK extends Serializable> T findById(Class<T> obj, PK key);
    
}
