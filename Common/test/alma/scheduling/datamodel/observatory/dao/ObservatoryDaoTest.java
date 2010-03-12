package alma.scheduling.datamodel.observatory.dao;

import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.TelescopeEquipment;

public class ObservatoryDaoTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ObservatoryDaoTest.class);
    
    public ObservatoryDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
 
    public void testXmlObsProjectDao() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/observatory/dao/context.xml");
        XmlObservatoryDao xmldao = (XmlObservatoryDao) ctx.getBean("xmlObservatoryDao");
        List<TelescopeEquipment> equipments = xmldao.getAllEquipments();
        List<ArrayConfiguration> arrConfigs = xmldao.getAllArrayConfigurations();
        ObservatoryDao dao = (ObservatoryDao) ctx.getBean("observatoryDao");
        dao.saveOrUpdate(equipments);
        dao.saveOrUpdate(arrConfigs);
    }
    
}
