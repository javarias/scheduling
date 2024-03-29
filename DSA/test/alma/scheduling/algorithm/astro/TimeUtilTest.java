/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.algorithm.astro;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.utils.TimeUtil;

import junit.framework.TestCase;

public class TimeUtilTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(TimeUtilTest.class);
    
    public TimeUtilTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetJulianDate() {
        assertEquals(2446113.75, TimeUtil.getJulianDate(1985, 2, 17.25), 0.001);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1985);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DAY_OF_MONTH, 17);
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date d = cal.getTime();
        assertEquals(2446113.75, TimeUtil.getJulianDate(d), 0.001);
    }

    public void testGetGST() {
        double utHours = TimeUtil.toDecimalHours(14, 36, 51.67);
        double gst = TimeUtil.getGreenwichMeanSiderealTime(1980, 4, 22, utHours);
        logger.info("gst = " + gst);
        
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1980);
        cal.set(Calendar.MONTH, 4);
        cal.set(Calendar.DAY_OF_MONTH, 22);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 36);
        cal.set(Calendar.SECOND, 51);
        cal.set(Calendar.MILLISECOND, 670);
        gst = TimeUtil.utToGST(cal.getTime());
        logger.info("gst = " + gst);        
    }

    public void testGetLST() {
        double gst = TimeUtil.toDecimalHours(4, 40, 5.23);
        double lst = TimeUtil.getLocalSiderealTime(gst, -64.0);
        logger.info("lst = " + lst);
    }
    
    public void testGstToUt() {
        double gst = TimeUtil.toDecimalHours(4, 40, 5.23);
        Date ut = TimeUtil.gstToUT(gst, 1980, 4, 22);
        DateFormat df = DateFormat.getTimeInstance(DateFormat.FULL);
        df.setTimeZone(TimeZone.getTimeZone("UT"));
        logger.info("ut = " + df.format(ut)); // curiously, this prints the wrong TZ
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.setTime(ut);
        logger.info("hours = " + cal.get(Calendar.HOUR_OF_DAY));
        logger.info("minutes = " + cal.get(Calendar.MINUTE));
        logger.info("seconds = " + cal.get(Calendar.SECOND));
        logger.info("milliseconds = " + cal.get(Calendar.MILLISECOND));
    }
    
    public void testLeapYears() {
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1996);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DAY_OF_MONTH, 29);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 36);
        cal.set(Calendar.SECOND, 51);
        cal.set(Calendar.MILLISECOND, 670);
        
        int days = TimeUtil.getLeapYearDaysfrom1990(cal.getTime());
        assertEquals(1, days);
    }
    
    public void testNumbersOfDaysfrom1990() {
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1980);
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DAY_OF_MONTH, 27);
        
        double days = TimeUtil.getDaysFrom1990(cal.getTime());
        assertEquals(-3444, days, 1);
    }
    
}
