package alma.scheduling.spt.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class DateIntervalTests extends TestCase {

	private final static SimpleDateFormat utcFormat;
	static{
		utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testOverlap() throws Exception {
		Date startDate1 = utcFormat.parse("2014-06-08T23:00:00");
		Date endDate1 = utcFormat.parse("2014-07-06T23:00:00");
		Date startDate2 = utcFormat.parse("2014-06-15T23:00:00");
		Date endDate2 = utcFormat.parse("2014-07-13T23:00:00");
		DateInterval di1 = new DateInterval(startDate1, endDate1);
		DateInterval di2 = new DateInterval(startDate2, endDate2);
		DateInterval overlap = di1.getOverlap(di2);
		System.out.println(overlap);
		overlap = di2.getOverlap(di1);
		System.out.println(overlap);
	}
	
}
