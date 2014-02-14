	/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
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
 * 
 */

package alma.scheduling.utils;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * 
 * @author javarias
 *
 */
public class SchedulingContextFactory {
    
    /**
     * Create a new instance of a spring context using the resource defined in the parameter.
     * The url could contain the following identifiers: <i> file: </i> or  <i>classpath: </i> 
     * </br>
     * Ex. file:../context.xml classpath:alma/scheduling/CommonContext.xml
     * </br>
     * If no resource identifier is provided will be assumed that a file's url is passed as
     * parameter. </br>
     * 
     * This method requires the file scheduling.properties located into $ACSDATA/config 
     * or defined in the property <b>alma.scheduling.properties</b></br>
     * 
     * Other factories can make use of this class to create specific factories tied to 
     * a specific Spring context file configuration (i.e {@code CommonContextFactory}
     * 
     * @since ALMA 8.0.0
     * @param contextFileURL the URL of the Context xml file to be read.
     * @return a spring Context initialized and ready to be used. 
     */
	public static AbstractApplicationContext getContext(String contextFileURL) {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(factory);
        if (contextFileURL.startsWith("file:")) {
        	beanReader.loadBeanDefinitions(new FileSystemResource(contextFileURL
                    .substring(5)));
        } else if (contextFileURL.startsWith("classpath:")) {
        	beanReader.loadBeanDefinitions(new ClassPathResource(contextFileURL
                    .substring(10)));
        } else {
        	beanReader.loadBeanDefinitions(new FileSystemResource(contextFileURL));
        }
        AbstractApplicationContext ctx = new GenericApplicationContext(factory);
        ctx.refresh();
        
        return ctx;
    }
	
	/**
	 * Create a new instance of a spring context using an array of bytes
	 * 
	 * This method requires the file scheduling.properties located into $ACSDATA/config 
     * or defined in the property <b>alma.scheduling.properties</b></br>
	 * 
	 * @param context
	 * @return a spring Context initialized and ready to be used.
	 * 
	 * @since ALMA 8.1.1
	 */
	public static AbstractApplicationContext getContext(byte[] context) {
		
		if (context == null) {
			throw new IllegalArgumentException("Spring context byte array cannot be null");
		}
//		XmlBeanFactory factory = new XmlBeanFactory(new ByteArrayResource(context));
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(factory);
		beanReader.loadBeanDefinitions(new ByteArrayResource(context));
        AbstractApplicationContext ctx = new GenericApplicationContext(factory);
        ctx.refresh();
		return ctx;
	}
}
