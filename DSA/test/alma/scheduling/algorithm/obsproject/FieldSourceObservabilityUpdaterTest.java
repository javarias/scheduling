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
 * "@(#) $Id: FieldSourceObservabilityUpdaterTest.java,v 1.1 2010/03/12 17:14:04 rhiriart Exp $"
 */
package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.SchedBlock;

import junit.framework.TestCase;

public class FieldSourceObservabilityUpdaterTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(FieldSourceObservabilityUpdaterTest.class);
    
    public FieldSourceObservabilityUpdaterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSourceUpdating() throws Exception {
        ApplicationContext ctx =
            new ClassPathXmlApplicationContext("alma/scheduling/algorithm/obsproject/context.xml");
        // load the obsproject into the database
        DataLoader loader = (DataLoader) ctx.getBean("dataLoader");
        loader.load();
        // run the source updater
        ModelUpdater sourceUpd = (ModelUpdater) ctx.getBean("sourceUpdater");
        Date ut = new Date();
        sourceUpd.update(ut);
        // select SchedBlocks
        SchedBlockSelector sourceSel = (SchedBlockSelector) ctx.getBean("sourceSelector");
        Collection<SchedBlock> sbs = sourceSel.select();
        logger.info("number of sbs selected: " + sbs.size());
    }

}