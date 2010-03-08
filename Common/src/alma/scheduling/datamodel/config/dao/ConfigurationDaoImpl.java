package alma.scheduling.datamodel.config.dao;

import java.util.Date;

import org.hibernate.Query;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.config.Configuration;

public class ConfigurationDaoImpl extends GenericDaoImpl implements
        ConfigurationDao {

    private XmlConfigurationDaoImpl xmlDao;
    
    private Configuration config = null;
    
    public ConfigurationDaoImpl(){
        xmlDao = new XmlConfigurationDaoImpl();
    }
    
    @Override
    public Configuration getConfiguration() {
        if(config != null)
            return config;
        config = xmlDao.getConfiguration();
        Query query = getSession().createQuery("from Configuration as config"
                + "order by os.lastLoad desc");
        query.setMaxResults(1);
        if(query.list().size() == 0)
            config.setLastLoad(null);
        else
            config.setLastLoad((Date) query.list().get(0));
        return config;
    }

    /**
     * Store in the DB a new last Load timestamp
     */
    @Override
    public void updateConfig() {
        config.setLastLoad(new Date());
        saveOrUpdate(config);
    }

}
