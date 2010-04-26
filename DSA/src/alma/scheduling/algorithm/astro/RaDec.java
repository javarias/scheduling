package alma.scheduling.algorithm.astro;

public class RaDec {

    /** Right Ascencion in degrees */
    protected double ra;
    
    /** Declination in degrees */
    protected double dec;
    

    public RaDec() {
        ra = 0.0;
        dec = 0.0;
    }
    
    public RaDec(double ra, double dec) {
        this.ra = ra;
        this.dec = dec;
    }
    
    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public double getDec() {
        return dec;
    }

    public void setDec(double dec) {
        this.dec = dec;
    }
    
}
