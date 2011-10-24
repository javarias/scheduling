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
package alma.scheduling.datamodel.obsproject.dao;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.WeatherConstraints;

public class ObsUnitDaoTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ObsUnitDaoTest.class);
    
    public ObsUnitDaoTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
 
    public void testSomething() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/datamodel/obsproject/dao/context.xml");
        ObsUnitDao dao = (ObsUnitDao) ctx.getBean("obsUnitDao");
        SchedBlock sb = new SchedBlock();
        sb.setPiName("me");
        sb.setWeatherConstraints(new WeatherConstraints(0.0, 0.0, 0.0, 0.0));
        dao.save(sb);
        ObsUnitSet ous = new ObsUnitSet();
        SchedBlock sb2 = new SchedBlock();
        sb2.setWeatherConstraints(new WeatherConstraints(1.0, 1.0, 1.0, 1.0));
        SchedBlock sb3 = new SchedBlock();
        sb3.setWeatherConstraints(new WeatherConstraints(2.0, 2.0, 2.0, 2.0));
        ous.addObsUnit(sb2);
        ous.addObsUnit(sb3);
        dao.save(ous);
    }
    
}
