package alma.scheduling.datamodel.executive;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class PI {

	private String name;
	private Set<PIMembership> pIMembership;

	public PI(){

	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PIMembership> getPIMembership() {
        return pIMembership;
    }

    public void setPIMembership(Set<PIMembership> mPIMembership) {
        pIMembership = mPIMembership;
    }

    public void finalize() throws Throwable {

	}

}