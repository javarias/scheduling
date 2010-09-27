package alma.scheduling.utils;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

public class SchedulingContextFactory {
    
    private static final String PROPERTIES_FILE = "scheduling.properties";
    private static String path = null;
    
    /**
     * Create a new instance of a spring context using the rsource defined in the url.
     * The url could contain the following identifiers: <i> file: </i> or  <i>classpath: </i> 
     * </br>
     * Ex. file:../context.xml classpath:alma/scheduling/CommonContext.xml
     * </br>
     * If no resource identifier is provided will be assumed that a file a url is passed as
     * parameter.
     * 
     * @param url
     * @return
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
