package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:42 AM
 */
public class Executive {

	private float defaultPercentage;
	private String name;
	public SchedBlockExecutivePercentage m_SchedBlockExecutivePercentage;

	
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

	public void finalize() throws Throwable {

	}

}