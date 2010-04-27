package alma.scheduling.algorithm.astro;

public class MoonAstroData extends RaDec {
    
    private double Mmp;
    private double Ec;
    private double angularDiameter;
    
    public MoonAstroData(double Ra, double Dec){
        super(Ra, Dec);
    }

    double getMmp() {
        return Mmp;
    }

    void setMmp(double mmp) {
        Mmp = mmp;
    }

    double getEc() {
        return Ec;
    }

    void setEc(double ec) {
        Ec = ec;
    }

    public double getAngularDiameter() {
        return angularDiameter;
    }

    public void setAngularDiameter(double angularDiameter) {
        this.angularDiameter = angularDiameter;
    }
    
}
