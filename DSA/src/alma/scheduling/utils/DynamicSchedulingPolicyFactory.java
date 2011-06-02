package alma.scheduling.utils;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

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
		XmlStreamBeanFactory xmlFactory = new XmlStreamBeanFactory(isr, ctx.getBeanFactory());
		ArrayList<String> policies = new ArrayList<String>();
		for (String beanName: xmlFactory.getBeanNamesForType(SchedBlockRanker.class)){
			System.out.println(beanName);
			ctx.registerBeanDefinition(beanName, xmlFactory.getBeanDefinition(beanName));
		}
		for (String beanName: xmlFactory.getBeanNamesForType(DynamicSchedulingAlgorithmImpl.class)) { 
			System.out.println(beanName);
			ctx.registerBeanDefinition(beanName, xmlFactory.getBeanDefinition(beanName));
			ctx.registerBeanDefinition("preUpdateSelector_" + beanName, xmlFactory.getBeanDefinition("preUpdateSelector_" + beanName));
			ctx.registerBeanDefinition("postUpdateSelectorAndUpdater_" + beanName, xmlFactory.getBeanDefinition("postUpdateSelectorAndUpdater_" + beanName));
			try {
				ctx.registerBeanDefinition("weatherTsysSelector_" + beanName, xmlFactory.getBeanDefinition("weatherTsysSelector_" + beanName));
			}
			catch (org.springframework.beans.factory.NoSuchBeanDefinitionException ex) {
				//No weather selector available
			}
			policies.add(beanName);
		}
		String[] retVal =  new String[policies.size()];
		policies.toArray(retVal);
		return retVal;
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
	
	private class XmlStreamBeanFactory extends DefaultListableBeanFactory{
		private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
		
		public XmlStreamBeanFactory(Resource resource) throws BeansException {
			this(resource, null);
		}
		
		public XmlStreamBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException { 
			super(parentBeanFactory);
			reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
			int beanDefs = this.reader.loadBeanDefinitions(resource);
			System.out.println("Bean Definitions:" + beanDefs);
		}
	}
}
