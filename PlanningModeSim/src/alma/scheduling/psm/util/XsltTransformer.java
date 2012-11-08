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

package alma.scheduling.psm.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import alma.scheduling.algorithm.SchedulingPolicyValidator;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;

public class XsltTransformer {
	
	public static void transform(String xslInURI, String xmlInURI, String htmlOutURI){
		try {
			transform(xslInURI, new FileInputStream(xmlInURI), new FileOutputStream(htmlOutURI));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void transform(String xslInURI, InputStream xmlIS, OutputStream htmlOS) {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		Templates translet;
		try {
			translet = tFactory.newTemplates(new StreamSource(xslInURI));
			// For each thread, instantiate a new Transformer, and perform the
			// transformations on that thread from a StreamSource to a StreamResult;
			transformer = translet.newTransformer();
			transformer.transform(new StreamSource(xmlIS), new StreamResult(htmlOS));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public static String transformSchedulingPoliciesFile(String xmlString) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlString));
		Document doc = builder.parse(is);
		if (doc.getFirstChild().getAttributes().getNamedItem("sim") == null) {
			Attr sim = doc.createAttribute("sim");
			sim.setValue("true");
			doc.getFirstChild().getAttributes().setNamedItem(sim);
		} else {
			doc.getFirstChild().getAttributes().getNamedItem("sim").setNodeValue("true");
		}
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		
		String resStr = result.getWriter().toString();
		String contextStr = SchedulingPolicyValidator.convertPolicyString(resStr);
		return contextStr;
	}
	
	public static void main (String args[]) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
"<Policies><SchedulingPolicy name=\"LalaFile-NoSelectors\"><SelectionCriteria>" +
    "</SelectionCriteria><Scorers><HourAngleScorer><weight>1.0</weight>" +
"</HourAngleScorer><TsysScorer><weight>1.0</weight></TsysScorer></Scorers></SchedulingPolicy>"+
"<SchedulingPolicy name=\"AllEqual\"><SelectionCriteria><ExecutiveSelector/>" +
"<OpacitySelector/><ArrayConfigSelector/></SelectionCriteria><Scorers><HourAngleScorer>" +
"<weight>1.0</weight></HourAngleScorer><TsysScorer><weight>1.0</weight></TsysScorer> " +
"</Scorers></SchedulingPolicy></Policies>";
		transformSchedulingPoliciesFile(xmlString);
	}
}
