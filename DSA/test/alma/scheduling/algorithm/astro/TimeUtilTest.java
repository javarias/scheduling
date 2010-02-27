package alma.scheduling.algorithm.astro;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    public void testGetLST() {
        double gst = TimeUtil.toDecimalHours(4, 40, 5.23);
        double lst = TimeUtil.getLocalSiderealTime(gst, 64.0, 'W');
        logger.info("lst = " + lst);
    }
    
}
