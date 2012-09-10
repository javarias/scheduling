package alma.scheduling.datamodel.executive;

import alma.scheduling.utils.DailyTimeInterval;

public class TimeInterval extends DailyTimeInterval {

	public TimeInterval() {
		
	}
	
	public long getStartTime() {
		return this.startTime;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public long getDuration() {
		return this.duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public boolean isValid() {
		if (duration > 0 || startTime >= 0)
			return true;
		return false;
	}
}
