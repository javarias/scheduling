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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import alma.scheduling.algorithm.PoliciesContainer;
import alma.scheduling.algorithm.PoliciesContainersDirectory;
import alma.scheduling.algorithm.SchedulingPolicyValidator;
/**
 * 
 * Handle Spring context singleton for the Scheduling Dynamic Algorithm names. The application
 * context xml configuration file must include the <b>alma.scheduling.algorithm.DSAContext.xml</b> and
 * define the Scheduling DSA configuration.
 * 
 * @since ALMA 8.1.0
 * @author javarias
 * $Id: DSAContextFactory.java,v 1.21 2013/02/26 23:10:30 javarias Exp $
 */
public class DSAContextFactory extends CommonContextFactory {

	public static final String DSA_POLICY_FILE_PROP = "dsa.policy.file"; 
	protected static final String SCHEDULING_DSA_DEFAULT_SPRING_CONFIG = "classpath:alma/scheduling/algorithm/DSAContext.xml";
	public static final String SCHEDULING_DSA_RESULTS_DAO_BEAN="DSAResultDAO";
	
	private static ApplicationContext context = null;
	private static ArrayList<String> availablePolicies = null;
	
	private static Logger logger = LoggerFactory.getLogger(DSAContextFactory.class);
	/**
	 * It will return the default ApplicationContext for the DSA.
	 * @return the context defined in SCHEDULING_DSA_DEFAULT_SPRING_CONFIG
	 */
	public static synchronized ApplicationContext getContext() {
		System.out.println(DSAContextFactory.class);
		if (context == null) {
			context = SchedulingContextFactory.getContext(SCHEDULING_DSA_DEFAULT_SPRING_CONFIG);
		}
		return context;
	}
	
	public static synchronized ApplicationContext getContext(String contextPath) {
		if (context == null) {
			context = SchedulingContextFactory.getContext(contextPath);
		}
		return context;
	}
	
	/**
	 * Creates a new instance, or return the current instance of a spring context for scheduling
	 * 
	 * @param policyFilePath the path of the DSA Policy file to initialize the spring context
	 * @since ALMA-9.1.5
	 * @return a spring Context for scheduling initialized and ready to be used.
	 */
	public static synchronized ApplicationContext getContextFromPoliciesFile(String policyFilePath) {
		if (context != null)
			return context;
		if(policyFilePath == null)
			return getContext();
		String contextString = SchedulingPolicyValidator.convertPolicyFile(policyFilePath);
		context = SchedulingContextFactory.getContext(contextString.getBytes());
		@SuppressWarnings("unchecked")
		Map<String, Object> policies = context.getBeansOfType(alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl.class);
		PoliciesContainer container = null;
		try {
			container = new PoliciesContainer(
					InetAddress.getLocalHost().getHostName(), "system", true);
		} catch (UnknownHostException e) {
			container = new PoliciesContainer("system", "system", true);
		}
		for (String name: policies.keySet()) {
			container.getPolicies().add(name);
		}
		PoliciesContainersDirectory.getInstance().put(container.getUuid(), container);
		return context;
	}
	
	/**
	 * Create a new instance, or return the current instance of a spring context for scheduling 
	 * using the resource defined in scheduling properties file. 
	 * <br/>
	 * The property to be read from file is <b>dsa.policy.file</b>
	 * 
	 * This convenience method calls {@link #getContextFromPoliciesFile(String)} with parameter equals to the
	 * the path read from scheduling property file.
	 * 
	 * @see SchedulingContextFactory#getContext(String)
	 * @param filePath the path to the scheduling.properties file
	 * @return a spring Context initialized and ready to be used.
	 * 
	 * @since ALMA 9.1.5
	 */
	public static synchronized ApplicationContext getContextFromPropertyFile(String filePath){
		if (context != null)
			return context;
		String path = filePath;
		InputStream isProp;
		try {
			isProp = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return getContext();
		} 
		Properties properties = new Properties();
		try {
			properties.load(isProp);
		} catch (IOException e) {
			e.printStackTrace();
			return getContext();
		}
		String policyFilePath = properties.getProperty(DSA_POLICY_FILE_PROP);
		return getContextFromPoliciesFile(policyFilePath);

	}

	/**
	 * Convenience method to call {@link #getContextFromPropertyFile(String)} with parameter equals to
	 * {@link SchedulingContextFactory#getPropertyFilePath()}
	 * 
	 * @return a spring Context initialized and ready to be used.
	 * @since ALMA 8.1.1
	 */
	public static synchronized ApplicationContext getContextFromPropertyFile() {
		return getContextFromPropertyFile(SchedulingContextFactory.getPropertyFilePath());
	}
	
	public static synchronized ApplicationContext getSimulationContextFromPropertyFile() throws IOException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException{
		if (context != null)
			return context;
		String path = SchedulingContextFactory.getPropertyFilePath();
		InputStream isProp;
		isProp = new FileInputStream(path);
		Properties properties = new Properties();
		properties.load(isProp);
		String policyFilePath = properties.getProperty(DSA_POLICY_FILE_PROP);
		if(policyFilePath == null)
			return getContext();
		InputStream isPolicies = new FileInputStream(policyFilePath);
		StringBuilder str =  new StringBuilder();
		int c;
		while ((c = isPolicies.read()) > -1)
			str.append((char) c);
		isPolicies.close();
		String simPoliciesStr = transformSchedulingPoliciesForSimulation(str.toString());
		String contextString = SchedulingPolicyValidator.convertPolicyString(simPoliciesStr);
		context = SchedulingContextFactory.getContext(contextString.getBytes());
		@SuppressWarnings("unchecked")
		Map<String, Object> policies = context.getBeansOfType(alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl.class);
		PoliciesContainer container = null;
		try {
			container = new PoliciesContainer(
					InetAddress.getLocalHost().getHostName(), "system", true);
		} catch (UnknownHostException e) {
			container = new PoliciesContainer("system", "system", true);
		}
		for (String name: policies.keySet()) {
			container.getPolicies().add(name);
		}
		PoliciesContainersDirectory.getInstance().put(container.getUuid(), container);
		return context;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> getPolicyNames() {
		if (context == null) {
			DSAContextFactory.getContextFromPropertyFile();
		}
		if (availablePolicies == null) {
			Map policies = context.getBeansOfType(alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl.class);
			availablePolicies = new ArrayList<String>(policies.keySet());
		}
		return availablePolicies;
	}
	
	public static String transformSchedulingPoliciesForSimulation(String xmlString) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
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

		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		
		return result.getWriter().toString();
	}
	
	public static void setWebApplicationContext(WebApplicationContext ctx) {
		if(context != null)
			logger.warn("Overriding already set context. This could be disastrous if the context was already set!");
		context = ctx;
	}
}
