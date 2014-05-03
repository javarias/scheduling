package alma.scheduling.spt.util;

public class LSTRange implements Comparable<LSTRange>{

	private Double startLST;
	private Double endLST;
	
	public LSTRange(Double startLST, Double endLST) {
		super();
		this.startLST = startLST;
		if (startLST < 0)
			this.startLST += 24;
		this.endLST = endLST;
		if (endLST > 24) 
			this.endLST -= 24;
	}
	
	public Double duration() {
		if (endLST < startLST)
			return endLST + 24 - startLST;
		return endLST - startLST;
	}
	
	public Double getStartLST() {
		return startLST;
	}

	public void setStartLST(Double startLST) {
		this.startLST = startLST;
	}

	public Double getEndLST() {
		return endLST;
	}

	public void setEndLST(Double endLST) {
		this.endLST = endLST;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endLST == null) ? 0 : endLST.hashCode());
		result = prime * result
				+ ((startLST == null) ? 0 : startLST.hashCode());
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
		LSTRange other = (LSTRange) obj;
		if (endLST == null) {
			if (other.endLST != null)
				return false;
		} else if (!endLST.equals(other.endLST))
			return false;
		if (startLST == null) {
			if (other.startLST != null)
				return false;
		} else if (!startLST.equals(other.startLST))
			return false;
		return true;
	}
	
	public boolean isEnclosingLSTRange(LSTRange shortRange) {
		if ((shortRange.getStartLST() + 24) >= (startLST + 24) &&
				(shortRange.getEndLST()) + 24 <= (endLST + 24))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "LSTRange [startLST=" + startLST + ", endLST=" + endLST + "]";
	}

	@Override
	public int compareTo(LSTRange o) {
		return getStartLST().compareTo(o.getStartLST());
	}
}
