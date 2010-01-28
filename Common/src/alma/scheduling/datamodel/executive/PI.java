package alma.scheduling.datamodel.executive;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class PI {

	private String name;
	private HashSet<PIMembership> pIMembership;

	public PI(){

	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<PIMembership> getPIMembership() {
        return pIMembership;
    }

    public void setPIMembership(HashSet<PIMembership> mPIMembership) {
        pIMembership = mPIMembership;
    }

    static PI copy(alma.scheduling.input.executive.generated.PI in, HashMap<String, Executive> execs){
        PI pi = new PI();
        pi.setName(in.getName());
        if(pi.getPIMembership() == null)
            pi.setPIMembership(new HashSet<PIMembership>());
        for (int i = 0; i < in.getPIMembershipCount(); i++){
            Executive e = execs.get(
                    in.getPIMembership(i).getExecutiveRef().getNameRef());
            PIMembership pim = PIMembership.copy(in.getPIMembership(i));
            pim.setExecutive(e);
            pi.getPIMembership().add(pim);
        }
        return pi;
    }
    
    public void finalize() throws Throwable {

	}

}