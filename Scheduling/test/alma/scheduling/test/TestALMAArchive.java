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
 * File TestALMAArchive.java
 */
 
package alma.scheduling.test;

import alma.acs.component.client.ComponentClientTestCase;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.Define.*;

public class TestALMAArchive extends ComponentClientTestCase {
    private ALMAArchive archive;

    public TestALMAArchive() throws Exception {
        super("Test ALMAArchive class");
    }
    protected void setUp() throws Exception {
        super.setUp();
        m_logger.info("SCHED_TEST: setup called");
        archive = new ALMAArchive(getContainerServices());
        m_logger.info("SCHED_TEST: ALAMArchive created");
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetAllProject() {
        try {
            Project[] p = archive.getAllProject();
            assertNotNull(p);
        } catch (Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
        }
    }
    public void testGetAllSB() {
        try {
            SB[] sb = archive.getAllSB();
            assertNotNull(sb);
        } catch(Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(TestALMAArchive.class);
        alma.acs.testsupport.tat.TATJUnitRunner.run(TestALMAArchive.class);
    }
}
