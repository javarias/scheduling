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
 * $Id: SchedulingContextFactory.java,v 1.4 2011/01/28 00:35:31 javarias Exp $
 */

package alma.scheduling.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
    
    private static final String BASE_PROPERTIES_FILE = "scheduling.properties";
    private static final String FILE_SUFFIX_ENVIRONMENT = "LOCATION";
    private static final String PROPERTIES_FILE = calculateFile(
    		BASE_PROPERTIES_FILE, FILE_SUFFIX_ENVIRONMENT);
    private static String path = null;
    
    /**
     * Work out a file to use. If the given environmental variable is
     * defined, use it's value as a suffix on the end of the base. If
     * it is not defined, then just use the base.
     *  
     * @param base - the root name of the filename for which we are
     *               looking;
     * @param env  - the name of the environmental variable in which to
     *               look for any suffix to apply to base.
     * @return
     */
    private static String calculateFile(String base, String env) {
    	final String result;
    	final String suffix = System.getenv(env);
    	
    	if (suffix == null) {
    		result = base;
    	} else {
    		result = String.format("%s.%s", base, suffix);
    	}
    	return result;
    }
    
    /**
     * Create a new instance of a spring context using the resource defined in the url.
     * The url could contain the following identifiers: <i> file: </i> or  <i>classpath: </i> 
     * </br>
     * Ex. file:../context.xml classpath:alma/scheduling/CommonContext.xml
     * </br>
     * If no resource identifier is provided will be assumed that a file's url is passed as
     * parameter. </br>
     * 
     * This method requires the file scheduling.properties located into $ACSDATA/config </br>
     * There is no way to use other config file yet. </br>
     * 
     * Other factories can make use of this class to create specific factories tied to 
     * a specific Spring context file configuration (i.e {@code CommonContextFactory}
     * 
     * @since ALMA 8.0.0
     * @param url the url of the Context xml file to be read.
     * @return a spring Context initialized and ready to be used. 
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
        
		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String str;
			while ((str = in.readLine()) != null) {
				System.out.println(str);
			}
			in.close();
		} catch (IOException e) {
		}
        
        return ctx;
    }
}
