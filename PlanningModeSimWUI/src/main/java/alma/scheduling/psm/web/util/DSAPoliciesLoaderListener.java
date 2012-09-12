/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.psm.web.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.xml.sax.SAXException;

import alma.scheduling.algorithm.PoliciesContainer;
import alma.scheduling.algorithm.SchedulingPolicyValidator;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.DynamicSchedulingPolicyFactory;

public class DSAPoliciesLoaderListener implements ServletContextListener {

	private static Logger logger = LoggerFactory.getLogger(DSAPoliciesLoaderListener.class);
	
	//TODO: This is horrible!!!!! Find a way to remove it, try to get the servlet context in another way
	public static ServletContext servletContext = null;
	
	public void contextInitialized(ServletContextEvent sce) {
		//Initialize policies
		servletContext = sce.getServletContext();
		WebApplicationContext ctx = 
				WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
		if (ctx == null)
			throw new RuntimeException("This Listener must be defined after the spring web application context.");
		DSAContextFactory.setWebApplicationContext(ctx);
		String path = System.getProperty("alma.scheduling.properties");
		if (path == null) {
			logger.info("No policies no register");
			return;
		}
		InputStream isProp = null;
		try {
			isProp = new FileInputStream(path);
			Properties properties = new Properties();
			properties.load(isProp);
			String policyFilePath = properties.getProperty(DSAContextFactory.DSA_POLICY_FILE_PROP);
			if (policyFilePath == null) {
				logger.info("No policies no register. " + DSAContextFactory.DSA_POLICY_FILE_PROP + " property is empty");
				return;
			}
			logger.info("Trying to add policies located in " + policyFilePath);
			InputStream isPolicies = new FileInputStream(policyFilePath);
			StringBuilder str =  new StringBuilder();
			int c;
			while ((c = isPolicies.read()) > -1)
				str.append((char) c);
			isPolicies.close();
			String simPoliciesStr = DSAContextFactory.transformSchedulingPoliciesForSimulation(str.toString());
			String springCtxXml = SchedulingPolicyValidator
					.convertPolicyString(simPoliciesStr);
			PoliciesContainer container = DynamicSchedulingPolicyFactory
					.getInstance().createDSAPolicyBeans("localhost", policyFilePath, springCtxXml);
			logger.info("New Policies file loaded successfully (" + "localhost"
					+ ":" + policyFilePath + ") : "
					+ Arrays.toString(container.getPoliciesAsArray()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} finally {
			if (isProp != null)
				try {
					isProp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		//Set the ACS.manager property
		System.setProperty("ACS.manager", "corbaloc::localhost:3000/Manager");
	}

	public void contextDestroyed(ServletContextEvent sce) {

	}

}
