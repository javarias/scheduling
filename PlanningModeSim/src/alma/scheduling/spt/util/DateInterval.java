package alma.scheduling.spt.util;

import java.util.Date;

public class DateInterval implements Comparable<DateInterval>{

	private final Date fromDate;
	private final Date toDate;
	
	public DateInterval(Date fromDate, Date toDate) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromDate == null) ? 0 : fromDate.hashCode());
		result = prime * result + ((toDate == null) ? 0 : toDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateInterval other = (DateInterval) obj;
		if (fromDate == null) {
			if (other.fromDate != null)
				return false;
		} else if (!fromDate.equals(other.fromDate))
			return false;
		if (toDate == null) {
			if (other.toDate != null)
				return false;
		} else if (!toDate.equals(other.toDate))
			return false;
		return true;
	}

	@Override
	public int compareTo(DateInterval o) {
		return fromDate.compareTo(o.getFromDate());
	}
	
	/**
	 * 
	 * @param i
	 * @return null if there is no overlap. Otherwise a DateInterval containing the overlapped interval
	 */
	public DateInterval getOverlap(DateInterval i) {
		if (toDate.getTime() <= i.getFromDate().getTime() || i.getToDate().getTime() <= fromDate.getTime())
			return null;
		if(fromDate.before(i.getFromDate())) {
			return new DateInterval(i.getFromDate(), toDate);
		} else {
			return new DateInterval(fromDate, i.getToDate());
		}
	}
	
}
