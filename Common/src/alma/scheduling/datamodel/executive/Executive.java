package alma.scheduling.datamodel.executive;

import java.util.HashSet;
import java.util.Set;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:42 AM
 */
public class Executive {

	private float defaultPercentage;
	private String name;
	private Set<SchedBlockExecutivePercentage> schedBlockExecutivePercentage;
	private Set<ExecutivePercentage> executivePercentage;

	
    public Executive(){

    }
	
	public float getDefaultPercentage() {
        return defaultPercentage;
    }

    public void setDefaultPercentage(float defaultPercentage) {
        this.defaultPercentage = defaultPercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public Set<SchedBlockExecutivePercentage> getSchedBlockExecutivePercentage() {
        return schedBlockExecutivePercentage;
    }

    public void setSchedBlockExecutivePercentage(
            Set<SchedBlockExecutivePercentage> mSchedBlockExecutivePercentage) {
        schedBlockExecutivePercentage = mSchedBlockExecutivePercentage;
    }

    public Set<ExecutivePercentage> getExecutivePercentage() {
        return executivePercentage;
    }

    public void setExecutivePercentage(
            Set<ExecutivePercentage> mExecutivePercentage) {
        executivePercentage = mExecutivePercentage;
    }
    
    public void finalize() throws Throwable {

	}

}