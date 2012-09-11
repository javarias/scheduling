/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.psm.sim;

import java.util.Date;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class TimeEvent implements Comparable<TimeEvent>{
    
    private EventType type;
    private ArrayConfiguration array;
    private SchedBlock sb;
    /** Duration of the event in ms (used only for SB observations)*/
    //For now this is fixed to 80 mins
    private static long duration = 5160000;
    /**
     * When the event occurs
     */
    private Date time;
    
    private boolean endOfInterval = false;
    
    public EventType getType() {
        return type;
    }
    
    public void setType(EventType type) {
        this.type = type;
    }
    
    public ArrayConfiguration getArray() {
        return array;
    }
    
    public void setArray(ArrayConfiguration array) {
        this.array = array;
    }
    
    public SchedBlock getSb() {
        return sb;
    }
    
    public void setSb(SchedBlock sb) {
        this.sb = sb;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    
    public long getDuration() {
    	return duration;
    }
    
	public boolean isEndOfInterval() {
		return endOfInterval;
	}

	public void setEndOfInterval(boolean endOfInterval) {
		this.endOfInterval = endOfInterval;
	}

    /**
     * Compare the date when will occur
     */
    @Override
    public int compareTo(TimeEvent o) {
    	int retVal = time.compareTo(o.getTime());
        return retVal;
    }
    
    @Override
    public String toString() {
        return "TimeEvent type=" + type.toString() + "; array=" + array.getId() + "; time=" +
            time + "; sb=" + sb;
    }
    
}
