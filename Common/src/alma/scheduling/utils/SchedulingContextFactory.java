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
 * $Id: SchedulingContextFactory.java,v 1.3 2010/09/27 21:51:55 javarias Exp $
 */

package alma.scheduling.utils;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * 
 * @author javarias
 *
 */
public class SchedulingContextFactory {
    
    private static final String PROPERTIES_FILE = "scheduling.properties";
    private static String path = null;
    
    /**
     * Create a new instance of a spring context using the rsource defined in the url.
     * The url could contain the following identifiers: <i> file: </i> or  <i>classpath: </i> 
     * </br>
     * Ex. file:../context.xml classpath:alma/scheduling/CommonContext.xml
     * </br>
     * If no resource identifier is provided will be assumed that a file's url is passed as
     * parameter.
     * 
     * @param url the url of the Context xml file to be read.
     * @return a spring Context initializated and ready to be used. 
     */
    public static AbstractApplicationContext getContext(String url) {
        XmlBeanFactory factory = null;
        if (path == null) {
            try {
                path = System.getProperty("alma.scheduling.properties");
                if(path == null){
                    path = System.getenv("ACSDATA") + "/config/" + PROPERTIES_FILE;
                }
            } catch (NullPointerException ex) {
                path = System.getenv("ACSDATA") + "/config/" + PROPERTIES_FILE;
            }
        }
        if (url.startsWith("file:")) {
            factory = new XmlBeanFactory(new FileSystemResource(url
                    .substring(5)));
        } else if (url.startsWith("classpath:")) {
            factory = new XmlBeanFactory(new ClassPathResource(url
                    .substring(10)));
        } else {
            factory = new XmlBeanFactory(new FileSystemResource(url));
        }
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        cfg.setLocation(new FileSystemResource(path));
        cfg.postProcessBeanFactory(factory);
        AbstractApplicationContext ctx = new GenericApplicationContext(factory);
        ctx.refresh();
        return ctx;
    }
}
