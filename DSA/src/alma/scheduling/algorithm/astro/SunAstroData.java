package alma.scheduling.algorithm.astro;

public class SunAstroData extends RaDec {
    private double trueAnomaly;
    
    private double angularDiameter;
    
    public SunAstroData(double ra, double dec) {
        super(ra, dec);
    }

    double getTrueAnomaly() {
        return trueAnomaly;
    }

    void setTrueAnomaly(double trueAnomaly) {
        this.trueAnomaly = trueAnomaly;
    }

    public double getAngularDiameter() {
        return angularDiameter;
    }

    public void setAngularDiameter(double angularDiameter) {
        this.angularDiameter = angularDiameter;
    }
    
}
