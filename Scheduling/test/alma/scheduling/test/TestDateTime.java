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
 * File TestDateTime.java
 */
 
package alma.scheduling.test;

import alma.acs.component.client.ComponentClientTestCase;
//import junit.framework.TestSuite;
import junit.framework.TestCase;

import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Date;
import alma.scheduling.Define.Time;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Sohaila Lucero
 */
//public class TestDateTime extends ComponentClientTestCase{
public class TestDateTime extends TestCase{
    
    public TestDateTime() throws Exception {
        super("DateTime Test");
    }
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
    
    public void testLeap() {
        assertFalse(DateTime.isLeapYear(2003));
        assertTrue(DateTime.isLeapYear(2000));
        assertFalse(DateTime.isLeapYear(2001));
        assertTrue(DateTime.isLeapYear(2004));
        assertFalse(DateTime.isLeapYear(1900));
        assertFalse(DateTime.isLeapYear(2100));
	}
    public void testJD() {
		//testJD(1957,10,4.81);
        DateTime tmp = new DateTime(1957, 10, 4.81);
        assertEquals(2436116.31, tmp.getJD(), 0.0);
		//testJD(2000,1,1.5);		
        tmp = new DateTime(2000, 1, 1.5);
        assertEquals(2451545.0, tmp.getJD(), 0.0);
		//testJD(1999,1,1.0);		
        tmp = new DateTime(1999, 1, 1.0);
        assertEquals(2451179.5, tmp.getJD(), 0.0);
		//testJD(1987,1,27.0);		
        tmp = new DateTime(1987,1,27.0);
        assertEquals(2446822.5, tmp.getJD(), 0.0);
		//testJD(1987,6,19.5);		
        tmp = new DateTime(1987, 6,19.5);
        assertEquals(2446966.0, tmp.getJD(), 0.0);
		//testJD(1988,1,27.0);		
        tmp = new DateTime(1988, 1, 27.0);
        assertEquals(2447187.5, tmp.getJD(), 0.0);
		//testJD(1988,6,19.5);		
        tmp = new DateTime(1988, 6, 19.5);
        assertEquals(2447332.0, tmp.getJD(), 0.0);
		//testJD(1900,1,1.0);		
        tmp = new DateTime(1900, 1, 1.0);
        assertEquals(2415020.5, tmp.getJD(), 0.0);
		//testJD(1600,1,1.0);		
        tmp = new DateTime(1600, 1, 1.0);
        assertEquals(2305447.5, tmp.getJD(), 0.0);
		//testJD(1600,12,31.0);
        tmp = new DateTime(1600, 12, 31.0);
        assertEquals(2305812.5, tmp.getJD(), 0.0);
    }

    public static void main(String[] args){
        junit.textui.TestRunner.run(TestDateTime.class);
    }
    /*
	private static void testJD(int y, int m, double d) {
		DateTime t = new DateTime(y,m,d);
		System.out.println("date " + y + "-" + m + "-" + d + "  \tjd = " + t.getJD());
	}
	private static void testJD(int y, int m, int d, int h, int mm, double s) {
		DateTime t = new DateTime(y,m,d,h,mm,s);
		System.out.println("date " + y + "-" + m + "-" + d +
			"T" + h + ":" + mm + ":" + s +  
			"  \tjd = " + t.getJD());
	}
	private static void testJD(Date d, Time t) {
		DateTime x = new DateTime(d,t);
		System.out.println("date " + d + " time " + t +
			"  \tjd = " + x.getJD());
	}
	private static void testDT(double jd) {
		DateTime x = new DateTime(jd);
		System.out.println("jd = " + jd + " date = " + x);
	}
	private static void testDN(int year, int month, int day) {
		String[] dayName = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
		};
		DateTime x = new DateTime(year,month,day);
		System.out.println("The day number of " + x + " is " + x.getDayOfYear() +
			" and it is " + dayName[x.getDayOfWeek()]);
	}

	public static void main(String[] args) {
		System.out.println("Test of DateTime class.");
		// Create the output file.
	//	out = null;
	//	try {
			// Create the output text file.
	//		File outFile = new File("D:\\Users\\afarris\\projects\\workspace\\af\\test","outDateTime.txt");
	//		out = new PrintStream (new FileOutputStream (outFile));
	//	} catch (IOException ioerr) {
	//		 ioerr.printStackTrace();
	//		 System.exit(0);
	//	}
		///////////////////////////////////////////////////////////////////

		
		// For this test we'll use UT.
		System.out.println("Timezone is UT.");
		
		System.out.println();
		System.out.println("Meeus, ex. 12.b, p. 89");
		DateTime d = new DateTime (1987,4,10,19,21,0.0);
		System.out.println("Mean sideral time at Greenwich on " + d + " is " + 
			d.getGreenwichMeanSiderealTime() + " hours.");
*/
		/*
		out.println("debug>>>");
		DateTime x = DateTime.lstToLocalTime(d.getGreenwichMeanSiderealTime(),d.getDate(),clock);
		out.println("x = " + x);
		*/
	/*	
		System.out.println("Meeus, ex. 12.a, p. 88");
		d = new DateTime (1987,4,10.0);
		System.out.println("Mean sideral time at Greenwich on " + d + " is " + 
			d.getGreenwichMeanSiderealTime() + " hours.");		
		
		System.out.println();
		System.out.println("Meeus, p. 61-62");
		testLeap(2003);
		testLeap(2000);
		testLeap(2001);
		testLeap(2004);
		testLeap(1900);
		testLeap(2100);
		
		
		testJD(1970,1,1.0);
				
		testJD(1957,10,4,19,26,24.0);
		testJD(2000,1,1,12,0,0.0);		

		testJD(new Date(1957,10,4),new Time(19,26,24.0));
		testJD(new Date(2000,1,1),new Time(12,0,0.0));
		
		testJD(1858,11,17.0);		

		testDT(2436116.31);
		testDT(2451545.0);
		testDT(2451179.5);
		testDT(2446822.5);
		testDT(2446966.0);
		testDT(2447187.5);
		testDT(2447332.0);
		testDT(2415020.5);
		testDT(2305447.5);
		testDT(2305812.5);
		
		System.out.println();
		// Now use MDT -- The VLA clock.
		DateTime.setClockCoordinates(107.6177275,34.0787491666667,-6);
		System.out.println("Timezone is MDT.");
		testJD(1957,10,4.81);
		testJD(2000,1,1.5);		
		testJD(1999,1,1.0);		
		testJD(1987,1,27.0);		
		testJD(1987,6,19.5);		
		testJD(1988,1,27.0);		
		testJD(1988,6,19.5);		
		testJD(1900,1,1.0);		
		testJD(1600,1,1.0);		
		testJD(1600,12,31.0);

		testDT(2436116.31);
		testDT(2451545.0);
		testDT(2451179.5);
		testDT(2446822.5);
		testDT(2446966.0);
		testDT(2447187.5);
		testDT(2447332.0);
		testDT(2415020.5);
		testDT(2305447.5);
		testDT(2305812.5);

		DateTime now = new DateTime(2003,10,2,11,19,30);
		System.out.println("The current time is " + now);
		System.out.println("The UT is " + now.getUT());
		System.out.println("The day number is " + now.getDayOfYear());
		System.out.println("The day of the week is " + now.getDayOfWeek());
		
		testDN(2003,1,1);
		testDN(2003,3,1);
		testDN(2003,12,31);
		testDN(2004,3,1);
		testDN(2004,12,31);
		
		int y = 2003; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		y = 2000; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		y = 2005; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		y = 2015; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		
		
		
		long currentSystemTime = System.currentTimeMillis();
		DateTime current  = new DateTime(currentSystemTime / 86400000.0 + 2440587.5);
		System.out.println("The current time, as a DateTime is " + current.toString());
		System.out.println("The currrent system time is " + currentSystemTime);
		long millisec = (long)((current.getJD() - 2440587.5) * 86400000.0 + 0.5);
		System.out.println("The number of millisec since Jan. 1 1970 " + millisec);

		DateTime t1 = DateTime.currentSystemTime();
		System.out.println("x " + t1.toString());
		DateTime t2 = new DateTime(System.currentTimeMillis());
		System.out.println("x " + t2.toString());
		System.out.println("t1 " + t1.getMillisec());
		System.out.println(" " + System.currentTimeMillis());
		
		///////////////////////////////////////////////////////////////////
		//System.out.close();
		System.out.println("End test.");

	}*/
}
