package alma.scheduling.datamodel.executive;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import alma.scheduling.datamodel.executive.dao.XmlExecutiveDaoImpl;
import alma.scheduling.input.executive.generated.ExecutiveData;
import alma.scheduling.persistence.HibernateUtil;
import alma.scheduling.persistence.XmlUtil;

import junit.framework.TestCase;

public class ExecutiveTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ExecutiveTest.class);
    private Session session;
    
    public ExecutiveTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        session = HibernateUtil.getSessionFactory().openSession();
    }

    protected void tearDown() throws Exception {
        session.close();
        HibernateUtil.shutdown();        
        super.tearDown();
    }
    
    public void testExecutive() throws Exception{
        ExecutiveData execData = null;
        try {
            XmlUtil.validateData("../config/executive.xsd", "./projects/test0/executive/executive.xml");
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        XmlExecutiveDaoImpl xmlDao =  
            new XmlExecutiveDaoImpl("./projects/test0/executive/executive.xml");
        
        
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for(ObservingSeason tmp: xmlDao.getAllObservingSeason())
                session.save(tmp);
            for(Executive tmp: xmlDao.getAllExecutive())
                session.save(tmp);
            for(PI tmp: xmlDao.getAllPi())
                session.save(tmp);
            tx.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            tx.rollback();
            throw ex;
        }
    }
}
