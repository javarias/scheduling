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
 * File TestTime.java
 */
 
package alma.scheduling.test;

import junit.framework.TestCase;
import alma.scheduling.Define.Time;

public class TestTime extends TestCase {

    public TestTime() throws Exception {
        super("Time class Test!");
    }
    
    protected void setUp() throws Exception {}
    protected void tearDown() throws Exception {}

    public void test1Time() {
        Time t = new Time(22, 45, 55.6789);
        assertEquals("22:45:55.67890000000517", t.toString());
    }
   
    public void test2Time() {
        Time t = new Time(0, 0, 0.0);
        assertEquals("0:0:0.0", t.toString());
    }
    public void test3Time() {
        Time t = new Time(0, 0, 0.1);
        assertEquals("0:0:0.1", t.toString());
    }
    public void test4Time() {
        Time t = new Time(23, 59, 59.999999);
        assertEquals("23:59:59.99999900000631", t.toString());
    }

    public void test5Time() {
        Time t = new Time(23.0);
        assertEquals("23:0:0.0", t.toString());
    }
    public static void main(String[] args){
        junit.textui.TestRunner.run(TestTime.class);
    }
}
