package alma.scheduling.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class TestUtilTests extends TestCase {

	public void testNHoursInDateTimeIntervalEasy() throws Exception {
		Calendar startTimeCal = Calendar.getInstance();
		startTimeCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		startTimeCal.set(2014, 0, 1);
		startTimeCal.setTimeInMillis(0 * TimeUtil.MSECS_IN_HOUR);
		Date startDate = startTimeCal.getTime();
		Date endDate = new Date(startDate.getTime() + TimeUtil.MSECS_IN_DAY * 3);
		DailyTimeInterval ti = new DailyTimeInterval(12 * TimeUtil.MSECS_IN_HOUR, 8 * TimeUtil.MSECS_IN_HOUR);
		double hours = TimeUtil.getHoursInDateTimeInterval(startDate, endDate, ti);
		assertEquals(24.0, hours);
	}
	
	public void testNHoursInDateTimeIntervalOvernight() throws Exception {
		Calendar startTimeCal = Calendar.getInstance();
		startTimeCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		startTimeCal.set(2014, 0, 1);
		startTimeCal.setTimeInMillis(8 * TimeUtil.MSECS_IN_HOUR);
		Date startDate = startTimeCal.getTime();
		Date endDate = new Date(startDate.getTime() + TimeUtil.MSECS_IN_DAY * 4 + 16 * TimeUtil.MSECS_IN_HOUR);
		DailyTimeInterval ti = new DailyTimeInterval(16 * TimeUtil.MSECS_IN_HOUR, 12 * TimeUtil.MSECS_IN_HOUR);
		double hours = TimeUtil.getHoursInDateTimeInterval(startDate, endDate, ti);
		assertEquals(56.0, hours);
	}
	
	public void testNHoursInDateTimeIntervalOvernightOddDays() throws Exception {
		Calendar startTimeCal = Calendar.getInstance();
		startTimeCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		startTimeCal.set(2014, 0, 1);
		startTimeCal.setTimeInMillis(8 * TimeUtil.MSECS_IN_HOUR);
		Date startDate = startTimeCal.getTime();
		Date endDate = new Date(startDate.getTime() + TimeUtil.MSECS_IN_DAY * 5 + 16 * TimeUtil.MSECS_IN_HOUR);
		DailyTimeInterval ti = new DailyTimeInterval(16 * TimeUtil.MSECS_IN_HOUR, 12 * TimeUtil.MSECS_IN_HOUR);
		double hours = TimeUtil.getHoursInDateTimeInterval(startDate, endDate, ti);
		assertEquals(68.0, hours);
	}
	
	public void testNHoursInDateTimeIntervalOvernightDifficult() throws Exception {
		Calendar startTimeCal = Calendar.getInstance();
		startTimeCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		startTimeCal.set(2014, 1, 1);
		startTimeCal.setTimeInMillis(17 * TimeUtil.MSECS_IN_HOUR);
		Date startDate = startTimeCal.getTime();
		Date endDate = new Date(startDate.getTime() + TimeUtil.MSECS_IN_DAY * 4 + 10 * TimeUtil.MSECS_IN_HOUR);
		DailyTimeInterval ti = new DailyTimeInterval(16 * TimeUtil.MSECS_IN_HOUR, 12 * TimeUtil.MSECS_IN_HOUR);
		double hours = TimeUtil.getHoursInDateTimeInterval(startDate, endDate, ti);
		assertEquals(58.0, hours);
	}
}
