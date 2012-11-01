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


import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
import alma.scheduling.algorithm.PoliciesContainer;
import alma.scheduling.algorithm.PoliciesContainersDirectory;
import alma.scheduling.algorithm.sbranking.SchedBlockRanker;

public class DynamicSchedulingPolicyFactory {
	
	private AbstractApplicationContext ctx;
	private static DynamicSchedulingPolicyFactory instance;
	
	
	private DynamicSchedulingPolicyFactory(AbstractApplicationContext ctx) {
		this.ctx = ctx;
	}

	public PoliciesContainer createDSAPolicyBeans(String hostname, String filePath, String xmlContextString) {
		PoliciesContainer container = new PoliciesContainer(hostname, filePath);
		ByteArrayInputStream is = new ByteArrayInputStream(xmlContextString.getBytes());
		InputStreamResource isr = new InputStreamResource(is);
		XmlStreamBeanFactory xmlFactory = new XmlStreamBeanFactory(isr, ctx.getBeanFactory());
		ArrayList<String> policies = new ArrayList<String>();
		ArrayList<String> modifiedBeansNames = new ArrayList<String>();
		//Bean names to be fixed in the XML.
		//The fix consists in to append the uuid of the container at the beginning of the bean name
		ArrayList<String> beanNamesToFix =  new ArrayList<String>();
		for (String beanName: xmlFactory.getBeanNamesForType(SchedBlockRanker.class)){
			beanNamesToFix.add(beanName);
		}
		for (String beanName: xmlFactory.getBeanNamesForType(DynamicSchedulingAlgorithmImpl.class)) { 
			beanNamesToFix.add(beanName);
			policies.add(beanName);
			beanNamesToFix.add("preUpdateSelector_" + beanName);
			beanNamesToFix.add("postUpdateSelectorAndUpdater_" + beanName);
			beanNamesToFix.add("weatherTsysSelector_" + beanName);
			beanNamesToFix.add("projectCodeSelector_" + beanName);
			beanNamesToFix.add("projectGradeSelector_" + beanName);
		}
		for(String beanName: beanNamesToFix) {
			xmlContextString = xmlContextString.replace("\"" + beanName + "\"", "\"uuid" + container.getUuid().toString() + "-" + beanName + "\"");
			modifiedBeansNames.add("uuid" + container.getUuid().toString() + "-" + beanName);
		}
		System.out.println(xmlContextString);
		//Now read the modified XML
		is = new ByteArrayInputStream(xmlContextString.getBytes());
		isr = new InputStreamResource(is);
		xmlFactory = new XmlStreamBeanFactory(isr, ctx.getBeanFactory());
		
		BeanDefinitionRegistry c = (BeanDefinitionRegistry) ctx; 
		for (String beanName: modifiedBeansNames) {
			System.out.println("Trying to register bean: " + beanName);
			try {
				c.registerBeanDefinition(beanName, xmlFactory.getBeanDefinition(beanName));
			} catch (org.springframework.beans.factory.NoSuchBeanDefinitionException ex) {
				System.out.println(ex.getMessage());
			}
		}
		container.getPolicies().addAll(policies);
		PoliciesContainersDirectory.getInstance().put(container.getUuid(), container);
		return container;
	}
	
	public String createEmptyDsaPolicy() {
		BeanDefinitionRegistry c = (BeanDefinitionRegistry) ctx; 
		BeanDefinitionBuilder builder =  BeanDefinitionBuilder.rootBeanDefinition(DynamicSchedulingAlgorithmImpl.class);
		c.registerBeanDefinition("Dynamic DSA Policy Test", builder.getBeanDefinition());
		return "Dynamic DSA Policy Test";
	}
	
	public synchronized void removePolicies(PoliciesContainer container) {
		final String prefix = "uuid" + container.getUuid() + "-";
		BeanDefinitionRegistry c = (BeanDefinitionRegistry) ctx; 
		for (String policy: container.getPolicies()) {
			c.removeBeanDefinition(prefix + policy);
			c.removeBeanDefinition(prefix + "preUpdateSelector_" + policy);
			c.removeBeanDefinition(prefix + "postUpdateSelectorAndUpdater_" + policy);
			try {
				c.removeBeanDefinition(prefix + "weatherTsysSelector_" + policy);
			} catch (NoSuchBeanDefinitionException ex) {
				//Bean not registered... continue
			}
		}
	}
	
	public static synchronized DynamicSchedulingPolicyFactory getInstance() {
		if (DynamicSchedulingPolicyFactory.instance == null) {
			DynamicSchedulingPolicyFactory.instance = 
				new DynamicSchedulingPolicyFactory((AbstractApplicationContext) 
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
