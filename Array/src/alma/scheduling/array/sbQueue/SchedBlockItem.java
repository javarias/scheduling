/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
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
package alma.scheduling.array.sbQueue;

import alma.scheduling.SchedBlockQueueItem;

/**
 * The Scheduling Array manages a queue of SchedBlocks to be submitted for
 * execution. The same SchedBlock, identified by its UID, could be submitted more
 * than once for execution. In order to identify different submissions of the same
 * SchedBlock, we use a queue of SchedBlockItems, where each submission is identified
 * by the SchedBlock UID and the timestamp of the submission.
 * 
 * @author rhiriart
 *
 */
public class SchedBlockItem {

    /** Submitted SchedBlock unique identifier*/
    private String uid;
    
    /** Timestamp of the submission in milliseconds from midnight January 1st, 1970 UTC */
    private long timestamp;

    public SchedBlockItem(String uid, long timestamp) {
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public SchedBlockItem(SchedBlockQueueItem item) {
        this(item.uid, item.timestamp);
    }
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
        SchedBlockItem other = (SchedBlockItem) obj;
        if (timestamp != other.timestamp)
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SchedBlockItem [uid=" + uid + ", timestamp=" + timestamp + "]";
    }    
}
