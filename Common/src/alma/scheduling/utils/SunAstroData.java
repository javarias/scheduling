package alma.scheduling.utils;

import alma.scheduling.datamodel.obsproject.SkyCoordinates;

public class SunAstroData extends SkyCoordinates {
    private double trueAnomaly;
    
    private double angularDiameter;
    
    public SunAstroData(double ra, double dec) {
        super(ra, dec);
    }

    public double getTrueAnomaly() {
        return trueAnomaly;
    }

    public void setTrueAnomaly(double trueAnomaly) {
        this.trueAnomaly = trueAnomaly;
    }

    public double getAngularDiameter() {
        return angularDiameter;
    }

    public void setAngularDiameter(double angularDiameter) {
        this.angularDiameter = angularDiameter;
    }
    
}
