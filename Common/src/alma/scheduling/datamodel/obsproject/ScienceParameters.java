package alma.scheduling.datamodel.obsproject;

public class ScienceParameters extends ObservingParameters {

    private Double integrationTime;
    
    private Double representativeBandwidth;
    
    private Double representativeFrequency;
    
    private Double sensitivityGoal;

    public ScienceParameters() {
        super();
    }
    
    public Double getIntegrationTime() {
        return integrationTime;
    }

    public void setIntegrationTime(Double integrationTime) {
        this.integrationTime = integrationTime;
    }

    public Double getRepresentativeBandwidth() {
        return representativeBandwidth;
    }

    public void setRepresentativeBandwidth(Double representativeBandwidth) {
        this.representativeBandwidth = representativeBandwidth;
    }

    public Double getRepresentativeFrequency() {
        return representativeFrequency;
    }

    public void setRepresentativeFrequency(Double representativeFrequency) {
        this.representativeFrequency = representativeFrequency;
    }

    public Double getSensitivityGoal() {
        return sensitivityGoal;
    }

    public void setSensitivityGoal(Double sensitivityGoal) {
        this.sensitivityGoal = sensitivityGoal;
    }
    
}
