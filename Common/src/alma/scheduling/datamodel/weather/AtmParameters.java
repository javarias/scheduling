package alma.scheduling.datamodel.weather;

public class AtmParameters {

    /** Surrogate identifier */
    Long id;
    
    /** Precipitable water vapor content (mm) */
    Double PWV;

    /** Frequency (GHz) */
    Double freq;
    
    /** Opacity (nepers) */
    Double opacity;
    
    /** Atmospheric brightness temperature (K) */
    Double atmBrightnessTemp;
        
    public AtmParameters() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPWV() {
        return PWV;
    }

    public void setPWV(Double pWV) {
        this.PWV = pWV;
    }

    public Double getFreq() {
        return freq;
    }

    public void setFreq(Double freq) {
        this.freq = freq;
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
    }

    public Double getAtmBrightnessTemp() {
        return atmBrightnessTemp;
    }

    public void setAtmBrightnessTemp(Double atmBrightnessTemp) {
        this.atmBrightnessTemp = atmBrightnessTemp;
    }
}
