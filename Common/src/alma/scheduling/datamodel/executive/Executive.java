package alma.scheduling.datamodel.executive;

import java.util.HashSet;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:42 AM
 */
public class Executive {

	private float defaultPercentage;
	private String name;
	private HashSet<SchedBlockExecutivePercentage> SchedBlockExecutivePercentage;
	private HashSet<ExecutivePercentage> ExecutivePercentage;

	
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

	public HashSet<SchedBlockExecutivePercentage> getSchedBlockExecutivePercentage() {
        return SchedBlockExecutivePercentage;
    }

    public void setM_SchedBlockExecutivePercentage(
            HashSet<SchedBlockExecutivePercentage> mSchedBlockExecutivePercentage) {
        SchedBlockExecutivePercentage = mSchedBlockExecutivePercentage;
    }

    public HashSet<ExecutivePercentage> getExecutivePercentage() {
        return ExecutivePercentage;
    }

    public void setM_ExecutivePercentage(
            HashSet<ExecutivePercentage> mExecutivePercentage) {
        ExecutivePercentage = mExecutivePercentage;
    }

    public void finalize() throws Throwable {

	}

}