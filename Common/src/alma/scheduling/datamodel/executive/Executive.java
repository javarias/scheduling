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
	private HashSet<SchedBlockExecutivePercentage> schedBlockExecutivePercentage;
	private HashSet<ExecutivePercentage> executivePercentage;

	
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
        return schedBlockExecutivePercentage;
    }

    public void setSchedBlockExecutivePercentage(
            HashSet<SchedBlockExecutivePercentage> mSchedBlockExecutivePercentage) {
        schedBlockExecutivePercentage = mSchedBlockExecutivePercentage;
    }

    public HashSet<ExecutivePercentage> getExecutivePercentage() {
        return executivePercentage;
    }

    public void setExecutivePercentage(
            HashSet<ExecutivePercentage> mExecutivePercentage) {
        executivePercentage = mExecutivePercentage;
    }
    
    static Executive copy(alma.scheduling.input.executive.generated.Executive in){
        Executive exec = new Executive();
        exec.setName(in.getName());
        exec.setDefaultPercentage(in.getDefaultPercentage());
        if(exec.getSchedBlockExecutivePercentage() == null){
            exec.setSchedBlockExecutivePercentage(
                    new HashSet<SchedBlockExecutivePercentage>());
        }
        for (int i = 0; i < in.getSchedBlockExecutivePercentageCount() ; i++)
            exec.getSchedBlockExecutivePercentage().add(
                    SchedBlockExecutivePercentage.copy(in
                            .getSchedBlockExecutivePercentage(i)));
        if(exec.getExecutivePercentage() == null)
            exec.setExecutivePercentage(new HashSet<ExecutivePercentage>());
        return exec;
    }
    
    public void finalize() throws Throwable {

	}

}