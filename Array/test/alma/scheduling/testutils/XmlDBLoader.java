/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * "@(#) $Id: XmlDBLoader.java,v 1.1 2010/07/27 16:50:28 rhiriart Exp $"
 */
package alma.scheduling.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.dataload.DataLoader;

/**
 * Utility class to run the full data loader. What exactly the full data loader
 * does depends on  the configurations in the Spring context file. Under a normal
 * configuration, this loader will populate the relational database
 * with data from the XML files located in $APRC_WORK_DIR directory, populating
 * the ObsProject, Executive and Observatory sections.
 * 
 * @author rhiriart
 *
 */
public class XmlDBLoader {

    private static Logger logger = LoggerFactory.getLogger(XmlDBLoader.class);
    
    public XmlDBLoader() {
    }

    public static void main(String[] args)  {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/CommonContext.xml");
        DataLoader loader = (DataLoader) ctx.getBean("fullDataLoader");
        try {
			loader.load();
		} catch (Exception ex) {
			logger.error("error loading the database: " + ex);
			ex.printStackTrace();
		}
    }
    
}
