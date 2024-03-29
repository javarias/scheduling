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

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.HorizonCoordinates;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.utils.CoordinatesUtil;
import alma.scheduling.utils.MoonAstroData;
import alma.scheduling.utils.SunAstroData;

public class CoordinatesUtilTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(CoordinatesUtilTest.class);
    
    public CoordinatesUtilTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetHourAngle() {
        double ra = 18.0 + 32.0 / 60.0 + 21.0 / 3600.0;
        double longitude = -64.0; // 64 degrees W
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1980);
        cal.set(Calendar.MONTH, 4);
        cal.set(Calendar.DAY_OF_MONTH, 22);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 36);
        cal.set(Calendar.SECOND, 51);
        cal.set(Calendar.MILLISECOND, 670);
        double ha = CoordinatesUtil.getHourAngle(cal.getTime(), ra, longitude);
        logger.info("hour angle = " + ha);
        assertEquals(5.862286, ha, 0.1);
    }
    
    public void testGetRA() {
        double ha = 5.0 + 51.0 / 60.0 + 44.0 / 3600.0;
        double longitude = -64.0; // 64 degrees W
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1980);
        cal.set(Calendar.MONTH, 4);
        cal.set(Calendar.DAY_OF_MONTH, 22);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 36);
        cal.set(Calendar.SECOND, 51);
        cal.set(Calendar.MILLISECOND, 670);
        double ra = CoordinatesUtil.getRA(cal.getTime(), ha, longitude);
        logger.info("right ascencion = " + ra);
        assertEquals(18.538230, ra, 0.1);
    }
    
    public void testGetHorizonFromEquatorial() {
        double ra = 18.0 + 32.0 / 60.0 + 21.0 / 3600.0; // hours
        ra = ra * 15.0; // degrees
        double dec = 23.0 + 13.0 / 60.0 + 10.0 / 3600.0; // degrees
        double latitude = 52.0; // 52 degrees N
        double longitude = -64.0; // 64 degrees W
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1980);
        cal.set(Calendar.MONTH, 4);
        cal.set(Calendar.DAY_OF_MONTH, 22);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 36);
        cal.set(Calendar.SECOND, 51);
        cal.set(Calendar.MILLISECOND, 670);
        HorizonCoordinates coords = CoordinatesUtil.equatorialToHorizon(
                new SkyCoordinates(ra, dec), cal.getTime(), latitude, longitude);
        assertEquals(283.271, coords.getAzimuth(), 0.001);
        assertEquals(19.333, coords.getAltitude(), 0.001);
        logger.info("azimuth = " + coords.getAzimuth());
        logger.info("altitude = " + coords.getAltitude());
        
    }
    
    public void testGetRisingAndSettingParameters() {
        double ra = 23.0 + 39.0 / 60.0 + 20.0 / 3600.0;
        ra = ra * 15.0;
        double dec = 21.0 + 42.0 /60.0;
        double latitude = 30.0;
        double longitude = 64.0;
        SkyCoordinates coords = new SkyCoordinates(ra, dec);
        FieldSourceObservability fso =
            CoordinatesUtil.getRisingAndSettingParameters(coords, latitude, longitude);
        logger.info("azimuth rising = " + fso.getAzimuthRising());
        logger.info("azimuth setting = " + fso.getAzimuthSetting());
        logger.info("rising time = " + fso.getRisingTime());
        logger.info("setting time = " + fso.getSettingTime());
    }
    
    public void testSunAstroData() {
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1980);
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DAY_OF_MONTH, 27);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 000);
        
        SunAstroData sunData = CoordinatesUtil.getSunAstroData(cal.getTime());
//        assertEquals((8.0 + 25.0/60.0 + 46.0/3600.0 ) * 15.0, sunData.ra, 1.0);
//        assertEquals(19.0 + 13.0/60.0 + 48.0/3600.0 , sunData.dec, 0.3);
    }
    
    
    public void testMoonAstroData() {
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, 1979);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 26);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 000);
        
        MoonAstroData moonData = CoordinatesUtil.getMoonAstroData(cal.getTime());
//        assertEquals( (22.0 + 33.0/60.0 + 27.0/3600.0 ) * 15.0, moonData.ra, 0.5);
//        assertEquals( -8.0 + 1.0/60.0 + 00.0/3600.0 , moonData.dec, 0.3);
    }
}
