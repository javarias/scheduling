package alma.scheduling.datamodel.obsproject;

public class GenericObservingParameters extends ObservingParameters {

	public GenericObservingParameters() {
        super();
    }

	private String type;
	
    public void setType(String type) {
		this.type = type;
	}
	
    public String getType() {
		return type;
	}
}
