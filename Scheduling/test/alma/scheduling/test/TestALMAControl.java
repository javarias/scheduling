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
import alma.scheduling.AlmaScheduling.ALMAControl;
import alma.acs.component.client.ComponentClientTestCase;

public class TestALMAControl extends ComponentClientTestCase {
    private ALMAControl control; 
    
    public TestALMAControl() throws Exception {
        super("Testing ALMAControl");
    }

    protected void setUp() throws Exception {
        super.setUp();
        control = new ALMAControl(getContainerServices());
        m_logger.info("SCHED_TEST: Setup complete");
    }

    protected void tearDown() throws Exception {
    }

    public void test1CreateSubarray() {
        //short[] antennas = {1,2,3,4,5,6,7,8,9};
        String[] antennas = {"1","2","3","4","5","6","7"};
        short subarray = -1;
        try {
            subarray = control.createSubarray(antennas);
            assertTrue(subarray != -1);
        } catch(Exception e) {
            m_logger.severe("SCHED_TEST: error");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        alma.acs.testsupport.tat.TATJUnitRunner.run(TestALMAControl.class);
    }
}
