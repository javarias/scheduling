/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.output;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

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

import alma.scheduling.output.generated.Array;
import alma.scheduling.output.generated.Results;
import alma.scheduling.output.generated.types.ExecutionStatus;
import alma.scheduling.persistence.XmlUtil;


public class OutputTest extends TestCase  {
	
	public OutputTest(String name) {
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
            XmlUtil.validateData("../config/output.xsd", "./projects/outputTest02/output/output.xml");
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Results results = null;
        try {
        	results = Results.unmarshalResults(
                    new FileReader("./projects/outputTest02/output/output.xml"));
        } catch (MarshalException e) {
            e.printStackTrace();
        	fail("Unmarshalling problems. Please re-generate the castor classes.");
        } catch (ValidationException e) {
            e.printStackTrace();
            fail("XML validation failed. Please check your XML for syntax errors.");
        } catch (FileNotFoundException e) {
        	fail("File not found, check for correct path and filename of the xml.");
            e.printStackTrace();
            throw e;
        }
        
        assertEquals( results.getArrayCount(), 2);
        assertEquals( "Array01", results.getArray(0).getId() );
        assertEquals( results.getObservationProject(0).getStatus(), ExecutionStatus.COMPLETE );
        assertEquals( results.getObservationProject(0).getSchedBlock(0).getArrayRef().getArrayRef() , results.getArray(0).getId() );
        
    }
    
    public void testOutputXMLGeneration() throws Exception {
        Results result = new Results();
        result.setAvailableTime(100.0);
        result.setMaintenanceTime(100.0);
        result.setOperationTime(100.0);
        result.setScientificTime(100.0);
        Array array = new Array();
        array.setAvailablelTime(100.0);
        array.setCreationDate(new Date("2010-03-24"));
        array.setDeletionDate(new Date("2010-05-15"));
        array.setMaintenanceTime(100.0);
        array.setScientificTime(100.0);
        result.setArray(new Array[] {array});
        StringWriter writer = new StringWriter();
        result.marshal(writer);
        String xml = writer.getBuffer().toString();
        System.out.println("Output XML:\n" + xml);
        Results result2 = Results.unmarshalResults(new StringReader(xml));
        assertEquals(1, result2.getArrayCount());        
    }

}
