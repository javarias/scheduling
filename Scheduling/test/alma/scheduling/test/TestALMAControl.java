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
 * File TestALMAControl.java
 */
 
package alma.scheduling.test;

import alma.Control.AutomaticArrayCommand;
import alma.scheduling.AlmaScheduling.ALMAControl;
import alma.acs.component.client.ComponentClientTestCase;

public class TestALMAControl extends ComponentClientTestCase {
    private ALMAControl control = null; 
    
    public TestALMAControl() throws Exception {
        super("Testing ALMAControl");
        System.out.println("Constructor");
    }

    public void test1Connect() throws Exception {
        m_logger.info("Test1");
        control = new ALMAControl(getContainerServices(),null);
        m_logger.info("SCHED_TEST: Setup complete");
        assertNotNull(control);
    }


    public void test2CreateSubarray() throws Exception {
        m_logger.info("Test2");
        control = new ALMAControl(getContainerServices(),null);
        String[] antennas = {"1","2","3","4","5","6","7"};
        String subarray = control.createArray(antennas,"Dynamic");
        assertNotNull(subarray);
    }

    public void test3ObserveNow() throws Exception {
        m_logger.info("Test3");
        /*
        control = new ALMAControl(getContainerServices());
        String[] antennas = {"1","2","3","4","5","6","7"};
        String subarray = control.createArray(antennas);
        assertNotNull(subarray);
        control.execSB(subarray, "SBID");
       */ 
    }
    
    

    public static void main(String[] args) {
        System.out.println("Test");
        alma.acs.testsupport.tat.TATJUnitRunner.run(TestALMAControl.class);
    }
}
