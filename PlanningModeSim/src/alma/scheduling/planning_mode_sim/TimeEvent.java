package alma.scheduling.planning_mode_sim;

import java.util.Date;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class TimeEvent implements Comparable<TimeEvent>{
    
    private EventType type;
    private ArrayConfiguration array;
    private SchedBlock sb;
    /**
     * When the event occurs
     */
    private Date time;
    
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

    /**
     * Compare the date when will occur
     */
    @Override
    public int compareTo(TimeEvent o) {
        return time.compareTo(o.getTime());
    }
    
    
}
