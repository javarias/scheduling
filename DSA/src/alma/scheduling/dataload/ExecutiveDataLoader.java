package alma.scheduling.dataload;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;

public class ExecutiveDataLoader implements DataLoader {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveDataLoader.class);
    
    private ExecutiveDAO dbDao;
    private ExecutiveDAO xmlDao;
    
    public ExecutiveDAO getDbDao() {
        return dbDao;
    }

    public void setDbDao(ExecutiveDAO dbDao) {
        this.dbDao = dbDao;
    }

    public ExecutiveDAO getXmlDao() {
        return xmlDao;
    }

    public void setXmlDao(ExecutiveDAO xmlDao) {
        this.xmlDao = xmlDao;
    }

    @Override
    public void load() {
        logger.info("Populating the DB with Exec data");
        GenericDao genDao = (GenericDao) dbDao;
        ArrayList<Object> objs = new ArrayList<Object>();
        objs.addAll(xmlDao.getAllExecutive());
        objs.addAll(xmlDao.getAllObservingSeason());
        objs.addAll(xmlDao.getAllPi());
        genDao.saveOrUpdate(objs);
    }
}
