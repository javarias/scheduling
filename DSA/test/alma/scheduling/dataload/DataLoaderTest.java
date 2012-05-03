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
 * "@(#) $Id: DataLoaderTest.java,v 1.3 2012/05/03 21:54:06 javarias Exp $"
 */
package alma.scheduling.dataload;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.weather.PathFluctHistRecord;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;
import alma.scheduling.utils.DSAContextFactory;

public class DataLoaderTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(DataLoaderTest.class);
    
    public DataLoaderTest(String name) throws Exception {
        super(name);    	
        System.setProperty("alma.scheduling.properties", System.getProperty("user.dir") + "/test/scheduling.properties");
        ApplicationContext ctx = DSAContextFactory.getContextFromPropertyFile();
    	DataLoader loader = (DataLoader) ctx.getBean("weatherSimDataLoader");
      	loader.load();
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
//    public void testWeatherDataLoading() throws Exception {
//        ApplicationContext ctx = DSAContextFactory.getContextFromPropertyFile();
//        
//        WeatherHistoryDAO wdao = (WeatherHistoryDAO) ctx.getBean("weatherSimDao");
//        wdao.setSimulationStartTime(new Date());
//        
//        DataLoader loader = (DataLoader) ctx.getBean("weatherSimDataLoader");
//        loader.load();
//        
//        DataLoader fullLoader = (DataLoader) ctx.getBean("fullDataLoader");
//        fullLoader.load();
//        
//        // testing the weather updater. this should be moved to a different test
//        
//        WeatherUpdater updater = (WeatherUpdater) ctx.getBean("weatherUpdater");
//        
//        Date ut = new Date();
//        for (int i = 0; i < 10; i++) {
//            logger.info("--- update # " + i + " ---");
//            ut.setTime(ut.getTime() + 4000000); // shift in 1.11 hours
//            updater.update(ut);            
//        }
//        
//        fullLoader.clear();
//    }
    
    public void testPathFluctDataLoading() throws Exception{
    	ApplicationContext ctx = DSAContextFactory.getContextFromPropertyFile();
    	WeatherHistoryDAO wdao = (WeatherHistoryDAO) ctx.getBean("weatherSimDao");
    	Calendar cal = Calendar.getInstance();
    	cal.set(2000, 0, 1, 0, 0, 0);
    	PathFluctHistRecord p = wdao.getPathFluctForTime(cal.getTime());
    	assertEquals(0.0, p.getTime(), 0.05);
    }
}
