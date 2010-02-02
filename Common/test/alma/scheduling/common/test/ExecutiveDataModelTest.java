package alma.scheduling.common.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import alma.scheduling.datamodel.executive.BeanFactory;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.input.executive.generated.ExecutiveData;
import alma.scheduling.persistence.HibernateUtil;


public class ExecutiveDataModelTest {

    
    public static void validateData(String schemaLoc, String xmlFileLoc) throws SAXException, IOException{
        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(schemaLoc));
        Validator validator = schema.newValidator();
        validator.validate(new SAXSource(new InputSource(xmlFileLoc)));
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        Configuration cfg = new Configuration().configure();
        SchemaExport schemaExport = new SchemaExport(cfg);
        schemaExport.create(false, true);
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            validateData("./config/executive.xsd", "test/projects/test0/executive/executive.xml");
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        ExecutiveData execData = null;
        try {
            execData = ExecutiveData.unmarshalExecutiveData(
                    new FileReader("test/projects/test0/executive/executive.xml"));
        } catch (MarshalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println(execData.getExecutive()[0].getName());
        
        ArrayList<Executive> execOut =  new ArrayList<Executive>();
        ArrayList<PI> piOut =  new ArrayList<PI>();
        ArrayList<ExecutivePercentage> epOut =  new ArrayList<ExecutivePercentage>();
        ArrayList<ObservingSeason> osOut = new ArrayList<ObservingSeason>();
        
        BeanFactory.copyExecutiveFromXMLGenerated(execData, execOut, piOut, epOut, osOut, etsOut);
        System.out.println(execOut);
        
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for(ObservingSeason tmp: osOut)
                session.save(tmp);
            for(Executive tmp: execOut)
                session.save(tmp);
            for(PI tmp: piOut)
                session.save(tmp);
            tx.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            tx.rollback();
        }
        
        session.close();
        HibernateUtil.shutdown();
    }

}
