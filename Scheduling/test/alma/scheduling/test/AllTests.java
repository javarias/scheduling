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
 * File AllTests.java
 */
 
package alma.scheduling.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {//extends TestSuite{
    /*
    private static TestDateTime dtTest = new TestDateTime();
    private static TestAntenna antennaTest = new TestAntenna();
    */
    public static Test suite(){
        TestSuite suite = new TestSuite();
        try {
            suite.addTestSuite(alma.scheduling.test.TestAntenna.class);
            suite.addTestSuite(alma.scheduling.test.TestDateTime.class);
            //suite.addTestSuite(alma.scheduling.test.TestALMAArchive.class);
            //suite.addTestSuite(alma.scheduling.test.TestMasterComponent.class);
            //suite.addTestSuite(alma.scheduling.test.TestALMAControl.class);
            suite.addTestSuite(alma.scheduling.test.TestStatus.class);
            suite.addTestSuite(alma.scheduling.test.TestTime.class);
        }catch(Exception e) {
            System.out.println("Error running tests");
        }
        return suite;
    }

    public static void main(String[] args) {
        //junit.textui.TestRunner.run(AllTests.class);
        try {
            alma.acs.testsupport.tat.TATJUnitRunner.run(AllTests.class);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
