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
 * "@(#) $Id: WeatherFileLoaderTest.java,v 1.4 2010/03/02 02:22:02 rhiriart Exp $"
 */
package alma.scheduling.dataload;

import java.util.Date;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.weather.WeatherUpdater;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;

public class WeatherFileLoaderTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(WeatherFileLoaderTest.class);
    
    public WeatherFileLoaderTest(String name) {
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
        
        WeatherHistoryDAO wdao = (WeatherHistoryDAO) ctx.getBean("weatherDao");
        wdao.setSimulationStartTime(new Date());
        
        DataLoader loader = (DataLoader) ctx.getBean("weatherDataLoader");
        loader.load();
        
        DataLoader fullLoader = (DataLoader) ctx.getBean("fullDataLoader");
        fullLoader.load();
        
        WeatherUpdater updater = (WeatherUpdater) ctx.getBean("weatherUpdater");
        
        Date ut = new Date();
        for (int i = 0; i < 10; i++) {
            logger.info("--- update # " + i + " ---");
            ut.setTime(ut.getTime() + 4000000); // shift in 1.11 hours
            updater.update(ut);            
        }
    }
}
