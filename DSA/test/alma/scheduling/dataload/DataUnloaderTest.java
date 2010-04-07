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
 * "@(#) $Id: DataUnloaderTest.java,v 1.1 2010/04/07 17:06:17 rhiriart Exp $"
 */
package alma.scheduling.dataload;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DataUnloaderTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(DataUnloaderTest.class);
    
    public DataUnloaderTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testWeatherDataLoading() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("alma/scheduling/dataload/context.xml");
        
        DataLoader fullLoader = (DataLoader) ctx.getBean("prjLoader");
        fullLoader.load();

        DataUnloader unloader = (DataUnloader) ctx.getBean("unloader");
        unloader.unload();
        
        fullLoader.clear();
    }
}
