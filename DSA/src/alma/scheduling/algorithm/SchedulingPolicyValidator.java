/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.algorithm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;



import org.springframework.context.support.AbstractApplicationContext;
import org.xml.sax.SAXException;

import alma.scheduling.utils.DSAContextFactory;

public class SchedulingPolicyValidator {

	private final URL schemaURL = getClass().getClassLoader().getResource("alma/scheduling/algorithm/SchedulingPolicy.xsd");
	private final URL xslURL = getClass().getClassLoader().getResource("alma/scheduling/algorithm/SchedulingPolicy.xsl");
	private final SchemaFactory sFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
	private final TransformerFactory tFactory = TransformerFactory.newInstance();
	
	/**
	 * @param args: The Scheduling Policy files to be validated with this tool
	 * 
	 */
	public static void main(String[] args) {
		SchedulingPolicyValidator validator = new SchedulingPolicyValidator();
		if(args.length > 0) {
			for(String file: args){
				SchedulingPolicyValidator.convertPolicyFile(file);
//				if (validator.validate(file)){
//					String outFile = validator.convert(".", file);
//					if(outFile != null)
//						validator.loadContext(outFile);
//				}
			}
		}
		else {
			help();
		}
	}	
	
	public static String convertPolicyFile(String filePath) {
		SchedulingPolicyValidator validator = new SchedulingPolicyValidator();
		try {
			validator.validate(new StreamSource(filePath));
		} catch (SAXException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			return "";
		} catch (IOException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			return "";
		}
		System.out.println("SUCCESS.");
		try {
			Transformer transformer = validator.tFactory.newTransformer(new StreamSource(validator.xslURL.toString()));
			StreamResult res = new StreamResult();
			StringWriter writer = new StringWriter();
			res.setWriter(writer);
			transformer.transform(new StreamSource(filePath), res);
			String xmlString = writer.getBuffer().toString();
			System.out.println(xmlString);
			return xmlString;
		} catch (TransformerConfigurationException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (TransformerException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public static String convertPolicyString(String policyXML) throws TransformerException, SAXException, IOException {
		SchedulingPolicyValidator validator = new SchedulingPolicyValidator();
		validator.validate(new StreamSource(new ByteArrayInputStream(policyXML.getBytes())));
//		try {
			Transformer transformer = validator.tFactory.newTransformer(new StreamSource(validator.xslURL.toString()));
			StreamResult res = new StreamResult();
			StringWriter writer = new StringWriter();
			res.setWriter(writer);
			transformer.transform(new StreamSource(new ByteArrayInputStream(policyXML.getBytes())), res);
			String xmlString = writer.getBuffer().toString();
			System.out.println(xmlString);
			return xmlString;
//		} catch (TransformerConfigurationException e) {
//			System.out.println("FAILED.");
//			System.out.println("Reason: " + e.getMessage());
//			e.printStackTrace();
//			return null;
//		} catch (TransformerException e) {
//			System.out.println("FAILED.");
//			System.out.println("Reason: " + e.getMessage());
//			e.printStackTrace();
//			return null;
//		}
	}
	
	private void loadContext(String file) {
		System.out.println("Loading DSA application context file ... ");
		AbstractApplicationContext context = DSAContextFactory.getContext(file);
		System.out.println("Available Algorithm Policies: ");
		for (String policyName: DSAContextFactory.getPolicyNames()) {
			System.out.println("   * " + policyName);
			context.getBean(policyName);
		}
		context.getBean(DSAContextFactory.SCHEDULING_DSA_RESULTS_DAO_BEAN);
		
	}

	private String convert(String dirPath, String file) {
		System.out.print("Generating DSA application context file" + file +" ... ");
		try {
			Transformer transformer = tFactory.newTransformer(new StreamSource(xslURL.toString()));
			transformer.transform(new StreamSource(file), new StreamResult(file +".context.xml"));
			System.out.println("	SUCCESS. Output file: " + dirPath + "/" + file + ".context.xml");
			return dirPath + "/" + file + ".context.xml";
		} catch (TransformerConfigurationException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			return null;
		} catch (TransformerException e) {
			System.out.println("FAILED.");
			System.out.println("Reason: " + e.getMessage());
			return null;
		}
	}

	private boolean validate(StreamSource stream) throws SAXException, IOException {
			Schema schema = sFactory.newSchema(schemaURL);
			Validator validator = schema.newValidator();
			validator.validate(stream);
			System.out.println("SUCCESS.");
			return true;
	}

	private static void help() {
		System.out.println("Usage: "
				+ SchedulingPolicyValidator.class.toString() + " <FILE>...");
		System.out
				.println("This tool must be used for validation and conversion ");
		System.out.println("of the scheduling policy files.");
		System.out
				.println("This tool will not concatenate the file received as parameter");
	}

}
