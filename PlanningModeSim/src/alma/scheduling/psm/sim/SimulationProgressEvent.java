/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.psm.sim;

import java.util.Date;

public class SimulationProgressEvent {
	
	private Date startTime;
	private Date currentTime;
	private Date stopTime;
	private Double p = null;
	
	public SimulationProgressEvent(Date startTime, Date currentTime, Date stopTime) {
		this.startTime = startTime;
		this.currentTime = currentTime;
		this.stopTime = stopTime;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public Date getCurrentTime() {
		return currentTime;
	}
	
	public Date getStopTime() {
		return stopTime;
	}
	
	public double getProgressPercentage() {
		if (p == null) 
			p = (1.0 - (((double)(getStopTime().getTime() - getCurrentTime().getTime()))
					/ ((double)(getStopTime().getTime() - getStartTime().getTime())))) * 100.0;
		return p;
	}
	
	public String getFormattedProgressPercentage() {
		return String.format("%.2f", p);
	}
}
