package alma.scheduling.datamodel.config.dao;

import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;

public class ConfigurationDaoTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationDaoTest.class);
    
    public ConfigurationDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
 
    public void testConfigurationDao() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/config/dao/context.xml");
        ConfigurationDao dao = (ConfigurationDao) ctx.getBean("configDao");
        Configuration config = dao.getConfiguration();
        assertNotNull(config);
        List<String> prjFiles = config.getProjectFiles();
        assertEquals(1, prjFiles.size());
    }
    
}
