package alma.scheduling.dataload;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.executive.dao.XmlExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;

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
        GenericDao genDao = (GenericDao) dbDao;
        ArrayList<Object> objs = new ArrayList<Object>();
        objs.addAll(xmlDao.getAllExecutive());
        objs.addAll(xmlDao.getAllObservingSeason());
        objs.addAll(xmlDao.getAllPi());
        genDao.saveOrUpdate(objs);
    }

    @Override
    public void clear() {
        dbDao.deleteAll();
//        dbDao.deleteAll(dbDao.findAll(PI.class));
//        dbDao.deleteAll(dbDao.findAll(PIMembership.class));
//        dbDao.deleteAll(dbDao.findAll(ExecutivePercentage.class));
    }
}
