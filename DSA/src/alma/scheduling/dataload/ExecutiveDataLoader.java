package alma.scheduling.dataload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.executive.dao.XmlExecutiveDAO;

public class ExecutiveDataLoader implements DataLoader {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveDataLoader.class);
    
    private ExecutiveDAO dbDao;
    private XmlExecutiveDAO xmlDao;
    
    public ExecutiveDAO getDbDao() {
        return dbDao;
    }

    public void setDbDao(ExecutiveDAO dbDao) {
        this.dbDao = dbDao;
    }

    public XmlExecutiveDAO getXmlDao() {
        return xmlDao;
    }

    public void setXmlDao(XmlExecutiveDAO xmlDao) {
        this.xmlDao = xmlDao;
    }

    @Override
    public void load() {
        logger.info("Populating the DB with Exec data");
        dbDao.saveObservingSeasonsAndExecutives(xmlDao.getAllObservingSeason(), xmlDao.getAllExecutive());
        dbDao.saveOrUpdate(xmlDao.getAllPi());
    }

    @Override
    public void clear() {
        dbDao.deleteAll();
    }
}
