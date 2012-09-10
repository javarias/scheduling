package alma.scheduling.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DailyTimeInterval {

	/**
	 * Start time in milliseconds considering the start at the beginning of the day in UT (00:00:00.000) 
	 */
	private long startTime;
	/**
	 * Duration of the interval in milliseconds 
	 */
	private long duration;
	
	public DailyTimeInterval (long startTime, long duration) {
		this.startTime = startTime;
		this.duration = duration;
	}
	
	public DailyTimeInterval (Date startTime, Date endTime) {
		Calendar calStart = Calendar.getInstance();
		calStart.setTimeZone(TimeZone.getTimeZone("UTC"));
		calStart.setTimeInMillis(startTime.getTime());
		this.startTime = calStart.get(Calendar.HOUR_OF_DAY) * 60 * 60  * 1000;
		this.startTime += calStart.get(Calendar.MINUTE) * 60 * 1000;
		this.startTime += calStart.get(Calendar.SECOND) * 1000;
		this.startTime += calStart.get(Calendar.MILLISECOND);
		this.duration = endTime.getTime() - startTime.getTime();
		if (startTime.getTime() >= endTime.getTime()) {
			duration += 24 * 60 * 60 * 1000;
		}
	}
	
	public long getMillisStartNextInterval(Date currTime) {
		Calendar currCal = Calendar.getInstance();
		currCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		currCal.setTimeInMillis(currTime.getTime());
		long timeOfDay = currCal.get(Calendar.HOUR_OF_DAY) * 60 * 60  * 1000;
		timeOfDay += currCal.get(Calendar.MINUTE) * 60 * 1000;
		timeOfDay += currCal.get(Calendar.SECOND) * 1000;
		timeOfDay += currCal.get(Calendar.MILLISECOND);
		if (timeOfDay <= this.startTime) {
			return (this.startTime - timeOfDay);
		} else {
			return ((24 * 60 * 60 * 1000) - timeOfDay + this.startTime);
		}		
	}
	
	public Date getStartNextInterval(Date currTime) {
		Date retVal = new Date(getMillisStartNextInterval(currTime) + currTime.getTime());
		return retVal;
	}
	
	public long getMillisEndInterval(Date currTime) {
		Calendar currCal = Calendar.getInstance();
		currCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		currCal.setTimeInMillis(currTime.getTime());
		long timeOfDay = currCal.get(Calendar.HOUR_OF_DAY) * 60 * 60  * 1000;
		timeOfDay += currCal.get(Calendar.MINUTE) * 60 * 1000;
		timeOfDay += currCal.get(Calendar.SECOND) * 1000;
		timeOfDay += currCal.get(Calendar.MILLISECOND);
		return (this.startTime - timeOfDay + this.duration);
	}
	
	public Date getEndInterval(Date currTime) {
		Date retVal = new Date(getMillisEndInterval(currTime) + currTime.getTime());
		return retVal;
	}
	
	//For testing purposes only
	long getStartTime() {
		return startTime;
	}
	
	long getDuration() {
		return duration;
	}
}
