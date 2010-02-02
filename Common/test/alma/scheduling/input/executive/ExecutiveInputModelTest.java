package alma.scheduling.input.executive;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import alma.scheduling.input.executive.generated.ExecutiveData;
import alma.scheduling.persistence.XmlUtil;


public class ExecutiveInputModelTest extends TestCase  {
	
	public ExecutiveInputModelTest(String name) {
        super(name);
    }
	
	protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
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
    public void testExecutiveInputModel() throws Exception{
       
        try {
            XmlUtil.validateData("../config/executive.xsd", "./projects/test0/executive/executive.xml");
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
                    new FileReader("./projects/test0/executive/executive.xml"));
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
        
    }

}
