package alma.scheduling.datamodel.executive;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class PIMembership {

	private float membershipPercentage;
	private Executive Executive;

	public PIMembership(){

	}

	public float getMembershipPercentage() {
        return membershipPercentage;
    }

    public void setMembershipPercentage(float membershipPercentage) {
        this.membershipPercentage = membershipPercentage;
    }

    public Executive getExecutive() {
        return Executive;
    }

    public void setExecutive(Executive mExecutive) {
        Executive = mExecutive;
    }

    static PIMembership copy(alma.scheduling.input.executive.generated.PIMembership in){
        PIMembership pim = new PIMembership();
        pim.setMembershipPercentage(pim.getMembershipPercentage());
        return pim;
    }
    
    public void finalize() throws Throwable {

	}

}