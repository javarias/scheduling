package alma.scheduling.datamodel.weather;

public class WeatherHistRecord {

    /** Time (UT) */
    protected Double time;

    /** Temperature (degrees C) */
    protected Double value;

    /** RMS variation (degrees C) */
    protected Double rms;

    /** Slope (degrees C / days) */
    protected Double slope;

    public WeatherHistRecord(Double time, Double value, Double rms,
            Double slope) {
        this.time = time;
        this.value = value;
        this.rms = rms;
        this.slope = slope;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getRms() {
        return rms;
    }

    public void setRms(Double rms) {
        this.rms = rms;
    }

    public Double getSlope() {
        return slope;
    }

    public void setSlope(Double slope) {
        this.slope = slope;
    }

}