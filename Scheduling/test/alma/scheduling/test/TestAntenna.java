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
 * File TestAntenna.java
 */

package alma.scheduling.test;

import alma.acs.component.client.ComponentClientTestCase;
//import junit.framework.TestSuite;
import junit.framework.TestCase;

import alma.scheduling.Define.Antenna;

//public class TestAntenna extends ComponentClientTestCase {
public class TestAntenna extends TestCase {
    protected Antenna antenna;

    public TestAntenna() throws Exception {
        super("AntennaClassTest");
        antenna = new Antenna((short)10, 100, false);
    }
    //protected void setUp() throws Exception {
        //super.setUp();
        // create an antenna with id = 10, locaition =100, and it
        // doesnt have a nutator
      //  antenna = new Antenna((short)10, 100, false);
    //}
    //public void tearDown() throws Exception {
    //}

    public void testID() {
        assertEquals(10, antenna.getAntennaId());
    }
    
    public void testLocation() {
        assertEquals(100, antenna.getLocationId());
    }
    
    public void testIsNutator() {
        assertEquals(false, antenna.isNutator());
    }

    public void testMoveTo() {
        assertEquals(100, antenna.getLocationId());
        antenna.moveTo(200);
        assertEquals(200, antenna.getLocationId());
    }

    public void testAllocated() {
        assertFalse(antenna.isAllocated());
        antenna.setAllocated((short)10);
        assertTrue(antenna.isAllocated());
        antenna.unAllocated();
        assertFalse(antenna.isAllocated());
        antenna.unAllocated();
        assertFalse(antenna.isAllocated());
    }

    public void testSubarrayId() {
        antenna.setAllocated((short)10);
        assertEquals(10, antenna.getSubarrayId());
        antenna.unAllocated();
        antenna.setAllocated((short)20);
        assertEquals(20, antenna.getSubarrayId());
        antenna.unAllocated();
    }
    
    public void testIdle() {
        antenna.setIdle();
        assertTrue(antenna.isIdle());
        antenna.setBusy();
        assertFalse(antenna.isIdle());
        antenna.setIdle();
    }

    public void testOnline() {
        assertFalse(antenna.isOnline());
        antenna.setOnline();
        assertTrue(antenna.isOnline());
        antenna.setOffline();
        assertFalse(antenna.isOnline());
        assertFalse(antenna.isAllocated());
    }

    public void testManual(){
        assertFalse(antenna.isManual());
        antenna.setManual();
        assertTrue(antenna.isManual());
        antenna.setAutomatic();
        assertFalse(antenna.isManual());
    }

    public void testFrequency() {
        antenna.setCurrentFrequency(10.0);
        assertEquals(10.0, antenna.getCurrentFrequency(),0);
        antenna.setStandbyFrequency(20.0);
        assertEquals(20.0, antenna.getStandbyFrequency(),0);
        
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestAntenna.class);
    }
}
 

