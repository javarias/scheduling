package alma.scheduling.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
import alma.scheduling.algorithm.sbranking.SchedBlockRanker;

public class DynamicSchedulingPolicyFactory {
	
	private GenericApplicationContext ctx;
	private static DynamicSchedulingPolicyFactory instance;
	
	
	private DynamicSchedulingPolicyFactory(GenericApplicationContext ctx) {
		this.ctx = ctx;
	}

	public String[] createDSAPolicyBeans(String xmlContextString) {
		ByteArrayInputStream is = new ByteArrayInputStream(xmlContextString.getBytes());
		InputStreamResource isr = new InputStreamResource(is);
		XmlBeanFactory xmlFactory = new XmlBeanFactory(isr);
		ArrayList<String> policies = new ArrayList<String>();
		for (String beanName: xmlFactory.getBeanNamesForType(SchedBlockRanker.class))
			ctx.registerBeanDefinition(beanName, xmlFactory.getBeanDefinition(beanName));
		for (String beanName: xmlFactory.getBeanNamesForType(DynamicSchedulingAlgorithmImpl.class)) { 
			ctx.registerBeanDefinition(beanName, xmlFactory.getBeanDefinition(beanName));
			policies.add(beanName);
		}
		String[] retVal =  new String[policies.size()];
		return policies.toArray(retVal);
	}
	
	public String createEmptyDsaPolicy() {
		BeanDefinitionBuilder builder =  BeanDefinitionBuilder.rootBeanDefinition(DynamicSchedulingAlgorithmImpl.class);
		ctx.registerBeanDefinition("Dynamic DSA Policy Test", builder.getBeanDefinition());
		return "Dynamic DSA Policy Test";
	}
	
	public static synchronized DynamicSchedulingPolicyFactory getInstance() {
		if (DynamicSchedulingPolicyFactory.instance == null) {
			DynamicSchedulingPolicyFactory.instance = 
				//TODO: Change this to read default file from properties,
				//TODO: Otherwise use default context
				new DynamicSchedulingPolicyFactory((GenericApplicationContext) 
						DSAContextFactory.getContext());
		}
		return DynamicSchedulingPolicyFactory.instance;
	}
}
