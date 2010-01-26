package alma.scheduling.datamodel.executive;

import java.util.HashSet;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class PI {

	private String name;
	private HashSet<PIMembership> m_PIMembership;

	public PI(){

	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<PIMembership> getM_PIMembership() {
        return m_PIMembership;
    }

    public void setM_PIMembership(HashSet<PIMembership> mPIMembership) {
        m_PIMembership = mPIMembership;
    }

    public void finalize() throws Throwable {

	}

}