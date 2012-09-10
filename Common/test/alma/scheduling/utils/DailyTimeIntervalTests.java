package alma.scheduling.utils;

import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


import alma.scheduling.input.executive.generated.TimeIntervalT;

import junit.framework.TestCase;

public class DailyTimeIntervalTests extends TestCase {

	private DailyTimeInterval ti;
	private TimeIntervalT castorTi;
	private static String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<DailyTimeInterval><StartTime>20:00:00Z</StartTime>"
			+ "<EndTime>12:00:00Z</EndTime></DailyTimeInterval>";
	
	@Override
	protected void setUp() throws Exception {
		StringReader reader = new StringReader(s);
		castorTi = TimeIntervalT.unmarshalTimeIntervalT(reader);
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testTimeInterval() throws Exception {
		ti = new DailyTimeInterval(castorTi.getStartTime().toDate()
				, castorTi.getEndTime().toDate());
		assertEquals(16 * 60 * 60 * 1000, ti.getDuration());
		assertEquals(20 * 60 * 60 * 1000, ti.getStartTime());
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(19 * 60 * 60 * 1000 + 25 * 60 * 1000 + 50 * 1000 );
		Date r = ti.getStartNextInterval(cal.getTime());
		assertEquals(20 * 60 * 60 *1000, r.getTime());
		cal.setTimeInMillis(27 * 60 * 60 * 1000 + 25 * 60 * 1000 + 50 * 1000 );
		r = ti.getStartNextInterval(cal.getTime());
		assertEquals(24 * 60 * 60 * 1000 + 20 * 60 * 60 * 1000, r.getTime());
		cal.setTimeInMillis(21 * 60 * 60 * 1000 + 25 * 60 * 1000 + 50 * 1000 );
		r = ti.getEndInterval(cal.getTime());
		assertEquals(36 * 60 * 60 * 1000, r.getTime());
		cal.setTimeInMillis(40 * 60 * 60 * 1000 + 25 * 60 * 1000 + 50 * 1000 );
		r = ti.getEndInterval(cal.getTime());
		assertEquals(60 * 60 * 60 * 1000, r.getTime());
	}
	
}
