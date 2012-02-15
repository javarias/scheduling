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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
/**
 * Custom scheduling property place holder configurator. It will look for scheduling properties file set in:</br>
 * <ol>
 * <li><b>alma.scheduling.properties</b> jvm property if it set.</li>
 * <li><b>$APRC_WORK_DIR/scheduling.properties</b> if the APRC_WORK_DIR env variable is set.</li>
 * <li>Use the default scheduling.properties files included in the war. Which configuration is the following: </br>
 * <code>
db.user=sa </br>
db.password= </br>
db.driverClass=org.hsqldb.jdbcDriver </br>
db.url=jdbc:hsqldb:hsql://localhost:8090/data_model </br>
hibernate.dialect=org.hibernate.dialect.HSQLDialect </br>
dsa.policy.file=
 * </code></li>
 * </ol>
 * 
 * @author javarias
 * @since ALMA-9.0
 */
public class SchedulingPropertyPlaceholderConfigurer extends
		PropertyPlaceholderConfigurer {

	private static Logger logger = LoggerFactory.getLogger(SchedulingPropertyPlaceholderConfigurer.class);
	private static final String WORKDIR_ENV_NAME = "APRC_WORK_DIR";
	private static final String CONFIG_PROPS_FILENAME = "alma.scheduling.properties";
	private Resource location = null;
	
	public SchedulingPropertyPlaceholderConfigurer() {
		super();
		String fileToCheck = null;
		String workDirPath = System.getenv(WORKDIR_ENV_NAME);
		String props_file = System.getProperty(CONFIG_PROPS_FILENAME);
		if (props_file != null) 
			fileToCheck = props_file;
		else
			if (workDirPath != null) 
				fileToCheck = workDirPath + "/scheduling.properties";
		if (fileToCheck == null) {
			logger.warn("Using default default scheduling policies set on webContextfile inside the web application");
			return;
		}
		File f = new File(fileToCheck);
		if(f.exists() && f.isFile() && f.canRead()) {
			logger.info("Using " + f.getAbsolutePath() + " as scheduling properties.");
			location = new FileSystemResource(f);
		} else {
			logger.warn("Using default default scheduling policies set on webContextfile inside the web application. " +
					"Property file " +f.getAbsolutePath() + " cannot be accessed.");
		}
		if (location != null )
			this.setLocation(location);
	}
	
	@Override
	public void setLocation(Resource location) {
		if (this.location != null)
			super.setLocation(this.location);
		else
			super.setLocation(location);
	}
	
}
