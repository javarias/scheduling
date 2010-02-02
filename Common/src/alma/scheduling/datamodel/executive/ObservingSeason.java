package alma.scheduling.datamodel.executive;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rhiriart
 * @version 1.0
 * @created 26-Jan-2010 9:26:43 AM
 */
public class ObservingSeason {

	private Date endDate;
	private String name;
	private Date startDate;
	private Set<ExecutivePercentage> executivePercentage;
	
	public ObservingSeason(){

	}

	public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Set<ExecutivePercentage> getExecutivePercentage() {
        return executivePercentage;
    }

    public void setExecutivePercentage(
            Set<ExecutivePercentage> mExecutivePercentage) {
        executivePercentage = mExecutivePercentage;
    }

    public static ObservingSeason copy(alma.scheduling.input.executive.generated.ObservingSeason in){
        ObservingSeason os = new ObservingSeason();
        os.setEndDate(in.getEndDate().toDate());
        os.setName(in.getName());
        os.setStartDate(in.getStartDate().toDate());
        if (os.getExecutivePercentage() == null)
            os.setExecutivePercentage(new HashSet<ExecutivePercentage>());
        return os;
    }
    
    public void finalize() throws Throwable {
	}

}