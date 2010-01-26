package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class PIMembership {

	private float membershipPercentage;
	public Executive m_Executive;

	public PIMembership(){

	}

	public float getMembershipPercentage() {
        return membershipPercentage;
    }

    public void setMembershipPercentage(float membershipPercentage) {
        this.membershipPercentage = membershipPercentage;
    }

    public void finalize() throws Throwable {

	}

}