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
package alma.scheduling.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import alma.scheduling.persistence.XmlUtil;

public class SchemaChecker {

	static final String JAXP_SCHEMA_LANGUAGE =
		"http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	static final String W3C_XML_SCHEMA =
		"http://www.w3.org/2001/XMLSchema"; 
	
	static final String JAXP_SCHEMA_SOURCE =
	    "http://java.sun.com/xml/jaxp/properties/schemaSource";



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Scheduling Subsystem - Schema Checker");
		if( args.length != 2 ){
			System.out.println("ERROR: Missing arguments");
			help();
			System.exit(1);
		}
		check( args[0], args[1]);
		System.out.println("SUCCESS: The file was succesfully checked against the schema");
		System.exit(0);
	}

	private static void help(){
		System.out.println( "\n" + 
				"This tool allows to check XML input files against their schema." + "\n" +
				"alma.scheduling.utils.SchemaChecker <file_to_check> <schema_of_the_file>" + "\n"
		);

	}

	private static void check( String file, String schema ){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		} 
		catch (IllegalArgumentException x) {
			System.out.println(x.getMessage());
			// Happens if the parser does not support JAXP 1.2
		}
		factory.setAttribute(JAXP_SCHEMA_SOURCE, new File(schema)); 
		DocumentBuilder db = null;
		try {
			db = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			db.parse( file );
		} catch (SAXException e1) {
			System.out.println("Parse error, please check your XML file:");
			System.out.println( e1.getMessage() );
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e1) {
			System.out.println("I/O error, check permissions and files:");
			System.out.println( e1.getMessage() );
			System.exit(1);
		}

	}

}
