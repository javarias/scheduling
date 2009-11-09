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
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.Define.Project;

public class TestALMAArchive extends ComponentClientTestCase {
    private ALMAArchive archive;

    public TestALMAArchive() throws Exception {
        super("Test ALMAArchive class");
    }
    protected void setUp() throws Exception {
        super.setUp();
        m_logger.info("SCHED_TEST: setup called");
        archive = new ALMAArchive(getContainerServices(), new ALMAClock());
        m_logger.info("SCHED_TEST: ALAMArchive created");
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetAllProject() throws Exception  {
        try {
            Project[] p = archive.getAllProject();
            assertNotNull(p);
            m_logger.info("Got "+p.length+" projects");
            String uid1;
            for (int i=0; i < p.length;i++){
                uid1 = p[i].getId();
                m_logger.info(uid1);
            }

            
        } catch (Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    /*
    public void testGetAllSB() throws Exception {
        try {
            SB[] sb = archive.getAllSB();
            assertNotNull(sb);
        } catch(Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    public void testGetNewProjects() throws Exception{
        try {

        } catch (Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    */
          
    
    public static void main(String[] args) {
        try {
            alma.acs.testsupport.tat.TATJUnitRunner.run(TestALMAArchive.class);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
