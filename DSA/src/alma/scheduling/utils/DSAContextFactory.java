package alma.scheduling.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import org.springframework.context.support.AbstractApplicationContext;

import com.thoughtworks.xstream.persistence.FileStreamStrategy;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
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
 * $Id: DSAContextFactory.java,v 1.15 2011/08/29 20:48:12 javarias Exp $
 */
public class DSAContextFactory extends CommonContextFactory {

	private static final String DSA_POLICY_FILE_PROP = "dsa.policy.file"; 
	protected static final String SCHEDULING_DSA_DEFAULT_SPRING_CONFIG = "classpath:alma/scheduling/algorithm/DSAContext.xml";
	//protected static final String SCHEDULING_DSA_DEFAULT_SPRING_CONFIG = "file:/export/home/griffin/javarias/scm/ALMA/HEAD/SCHEDULING/DSA/test/testPolicy.xml.context.xml";
//	protected static final String SCHEDULING_DSA_DEFAULT_SPRING_CONFIG = "file:/export/home/flaminia/dclarke/ALMA/Development/TRUNK/HEAD/SCHEDULING/DSA/test/testPolicy.xml.context.xml";
	public static final String SCHEDULING_DSA_RESULTS_DAO_BEAN="DSAResultDAO";
	
	private static AbstractApplicationContext context = null;
	private static ArrayList<String> availablePolicies = null;
	
	/**
	 * It will return the default ApplicationContext for the DSA.
	 * @return the context defined in SCHEDULING_DSA_DEFAULT_SPRING_CONFIG
	 */
	public static synchronized AbstractApplicationContext getContext() {
		System.out.println(DSAContextFactory.class);
		if (context == null) {
			context = SchedulingContextFactory.getContext(SCHEDULING_DSA_DEFAULT_SPRING_CONFIG);
		}
		return context;
	}
	
	public static synchronized AbstractApplicationContext getContext(String contextPath) {
		if (context == null) {
			context = SchedulingContextFactory.getContext(contextPath);
		}
		return context;
	}
	
	/**
	 * Create a new instance of a spring context using the resource defined in 
	 * scheduling properties file. The property to be read is <b>dsa.policy.file</b>
	 * 
	 * @see SchedulingContextFactory#getContext(String)
	 * @return a spring Context initialized and ready to be used.
	 * 
	 * @since ALMA 8.1.1
	 */
	public static synchronized AbstractApplicationContext getContextFromPropertyFile(){
		if (context != null)
			return context;
		String path = SchedulingContextFactory.setPropertyFilePath();
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
	
}
