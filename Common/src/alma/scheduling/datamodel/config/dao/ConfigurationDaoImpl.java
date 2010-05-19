package alma.scheduling.datamodel.config.dao;

import java.util.Date;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.correlatorSrc.CorrConfigValidator.SBConversionException;
import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.ScienceGradeConfig;

/* TODO Why it is necesarry to merge the XML with database? Wouldn't be cleaner to store
 * Why not to store the information from the XML in the database and then forget about the XML.
 */

@Transactional
public class ConfigurationDaoImpl extends GenericDaoImpl implements
        ConfigurationDao {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);
    
    private XmlConfigurationDaoImpl xmlDao;
    
    private Configuration config = null;
    
    public ConfigurationDaoImpl(){
        xmlDao = new XmlConfigurationDaoImpl();
    }
    
    /**
     * Get the current Configuration, merging data from the XML input file
     * that is not kept in the database with some dynamic data that is
     * updated there.
     */
    @Override
    public Configuration getConfiguration() {
        if(config != null)
            return config;
        config = xmlDao.getConfiguration(); 
        Query query = getSession().createQuery("from Configuration as config"
                + " order by config.lastLoad desc");
        query.setMaxResults(1);
        if (query.list().size() == 0) {
            config.setLastLoad(null);
        } else {
            Configuration dbConf = (Configuration) query.list().get(0);
            config.setLastLoad(dbConf.getLastLoad());
            config.setNextStepTime(dbConf.getNextStepTime());
            config.setSimulationStartTime(dbConf.getSimulationStartTime());
            config.setScienceGradeConfig(dbConf.getScienceGradeConfig());
        }
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

    @Override
    public void updateNextStep(Date nextStepTime) {
        config.setNextStepTime(nextStepTime);
        saveOrUpdate(config);
    }
    
    @Override
    public void updateSimStartTime(Date simStartTime) {
        logger.info("updating simulation start time");
        config.setSimulationStartTime(simStartTime);
        saveOrUpdate(config);
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Configuration");
    }
}
