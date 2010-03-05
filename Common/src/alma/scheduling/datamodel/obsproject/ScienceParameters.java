package alma.scheduling.datamodel.obsproject;

public class ScienceParameters extends ObservingParameters {

    /** Representative bandwidth (GHz) */
    private Double representativeBandwidth;
    
    /** Representative frequency (GHz) */
    private Double representativeFrequency;
    
    /** Sensitivy goal (Jy) */
    private Double sensitivityGoal;

    public ScienceParameters() {
        super();
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
