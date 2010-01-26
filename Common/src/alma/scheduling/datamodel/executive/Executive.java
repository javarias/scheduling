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
	private HashSet<SchedBlockExecutivePercentage> m_SchedBlockExecutivePercentage;
	private HashSet<ExecutivePercentage> m_ExecutivePercentage;

	
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

	public HashSet<SchedBlockExecutivePercentage> getM_SchedBlockExecutivePercentage() {
        return m_SchedBlockExecutivePercentage;
    }

    public void setM_SchedBlockExecutivePercentage(
            HashSet<SchedBlockExecutivePercentage> mSchedBlockExecutivePercentage) {
        m_SchedBlockExecutivePercentage = mSchedBlockExecutivePercentage;
    }

    public HashSet<ExecutivePercentage> getM_ExecutivePercentage() {
        return m_ExecutivePercentage;
    }

    public void setM_ExecutivePercentage(
            HashSet<ExecutivePercentage> mExecutivePercentage) {
        m_ExecutivePercentage = mExecutivePercentage;
    }

    public void finalize() throws Throwable {

	}

}