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
 * $Id: WeatherModelTest.java,v 1.1 2009/10/26 20:46:51 rhiriart Exp $
 */

package alma.scheduling.model;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import alma.scheduling.model.generated.WeatherParameters;
import alma.scheduling.model.generated.WeatherStationT;
import junit.framework.TestCase;

/**
 * Basic tests to exemplify the use of Castor to serialize XML files
 * to/from Java entities. The Java classes are generated from an XML Schema.
 */
public class WeatherModelTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // ...
    }

    @Override
    protected void tearDown() throws Exception {
        // ...
        super.tearDown();
    }

    /**
     * Test that the example XML file(s) comply with the Schema.
     */
    public void testSchemaValidation() {
        String xmlfile = "Weather.xml";
        String xsdfile = "../config/Weather.xsd";
        try {
            validateFile(xmlfile, xsdfile);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Validation error, " + xmlfile + " doesn't comply with " + xsdfile);
        }
    }

    /**
     * Simple test that loads the example XML file to the generated
     * Java entity, accesses several of its fields, and serialize back to
     * XML.
     */
    public void testLoadWeatherParametersFile() throws Exception {
        // Deserialize from XML
        WeatherParameters wparams =
            WeatherParameters.unmarshalWeatherParameters(new FileReader("Weather.xml"));
        
        // Access some of the fields
        WeatherStationT[] wstations = wparams.getWeatherStation();
        assertEquals(2, wstations.length);
        assertEquals("WS001", wstations[0].getName());
        assertEquals("WS002", wstations[1].getName());
        
        // Change something
        wstations[0].setName("WSFOO");
        
        // Serialize back to XML
        StringWriter sw = new StringWriter();
        wparams.marshal(sw);
        // System.out.println("XML:\n" + sw.toString());
        assertTrue(sw.toString().contains("WSFOO"));
    }
    
    /**
     * Validates an XML file agains an XSD Schema file.
     * 
     * @param xmlfile XML file
     * @param xsdfile XML Schema file
     * @throws SAXException If the XML file doesn't comply with the XSD file
     * @throws IOException In case if I/O exceptions accessing the files 
     */
    private void validateFile(String xmlfile, String xsdfile)
        throws SAXException, IOException {

        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdfile));
        Validator validator = schema.newValidator();

        ErrorHandler errHandler = new ErrorHandler() {
                public void error(SAXParseException e) { System.out.println(e); }
                public void fatalError(SAXParseException e) { System.out.println(e); }
                public void warning(SAXParseException e) { System.out.println(e); }
            };
        validator.setErrorHandler(errHandler);
        validator.validate(new SAXSource(new InputSource(xmlfile)));
    }
}
