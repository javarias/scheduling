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
 * File TestStatus.java
 */
package alma.scheduling.test;

import alma.scheduling.Define.Status;
import alma.scheduling.Define.DateTime;
import junit.framework.TestCase;

/**
 * Test the Status Class from the Define package
 * 
 * @author Sohaila Lucero
 */
public class TestStatus extends TestCase {

    public TestStatus() throws Exception {
        super("TestStatus");
    }
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
    public void test1Status(){
        Status s = new Status();
        assertEquals("notdefined", s.toString());
    }
    public void test2Status() {
        Status s = new Status();
		DateTime t = new DateTime (2004,03,12,9,0,0);
		s.setStarted(t); 
        assertEquals("running", s.getStatus());
		s.setReady(); 
        assertEquals("ready", s.getStatus());
		s.setRunning(); 
        assertEquals("running", s.getStatus());
		s.setWaiting();
        assertEquals("waiting", s.getStatus());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestStatus.class);
    }
}


