package alma.scheduling.utils;

import alma.scheduling.datamodel.obsproject.SkyCoordinates;

public class MoonAstroData extends SkyCoordinates {
    
    private double Mmp;
    private double Ec;
    private double angularDiameter;
    
    public MoonAstroData(double Ra, double Dec){
        super(Ra, Dec);
    }

    public double getMmp() {
        return Mmp;
    }

    public void setMmp(double mmp) {
        Mmp = mmp;
    }

    public double getEc() {
        return Ec;
    }

    public void setEc(double ec) {
        Ec = ec;
    }

    public double getAngularDiameter() {
        return angularDiameter;
    }

    public void setAngularDiameter(double angularDiameter) {
        this.angularDiameter = angularDiameter;
    }
    
}
