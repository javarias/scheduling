package alma.scheduling.algorithm.weather;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;

public class WeatherUpdater implements ModelUpdater {

    static final double BOLTZMANN = 1.38e-16 * 1.0e23;  //[Jy/K]
    static final double PLANCK    = 6.626e-34; // [J s]
    static final double LIGHTSPEED= 2.99792458e8; //[m/s]

    private static Logger logger = LoggerFactory.getLogger(WeatherUpdater.class);
        
    AtmParametersDao dao;

    public void setDao(AtmParametersDao dao) {
        this.dao = dao;
    }
    
    @Override
    public boolean needsToUpdate(Date date) {
        return true;
    }

    @Override
    public void update() {
        logger.debug("updating...");
        double decl = 45.0; // degrees
        double latitude = -23.0 + 1.0 / 60.0 + 22.42 / 3600.0;
        double frequency = 21.533; // GHz
        double pwv = 1.5; // mm
        double tsys = getTsys(decl, latitude, frequency, pwv);
        logger.debug("tsys: " + tsys);
    }

    protected double[] interpolateOpacityAndTemperature(double pwv, double freq) {
        double[] retVal = new double[2];
        logger.debug("pwv: " + pwv);
        logger.debug("freq: " + freq);
        // First get the PWV interval
        Double[] pwvInterval = dao.getEnclosingPwvInterval(pwv);
        logger.debug("pwv lower bound: " + pwvInterval[0]);
        logger.debug("pwv upper bound: " + pwvInterval[1]);
        // For the PWV lower bound, interpolate opacity and temperature as functions of frequency
        AtmParameters[] atm;
        atm = dao.getEnclosingIntervalForPwvAndFreq(pwvInterval[0], freq);
        logger.debug("freq lower bound: " + atm[0].getFreq());
        logger.debug("freq upper bound: " + atm[1].getFreq());
        double interpOpacity1 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getOpacity(), atm[1].getOpacity());
        logger.debug("interpolated opacity 1: " + interpOpacity1);
        double interpTemp1 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getAtmBrightnessTemp(), atm[1].getAtmBrightnessTemp());
        logger.debug("interpolated temperature 1: " + interpTemp1);
        // For the PWV upper bound, interpolate opacity and temperature as functions of frequency
        atm = dao.getEnclosingIntervalForPwvAndFreq(pwvInterval[1], freq);
        double interpOpacity2 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getOpacity(), atm[1].getOpacity());
        logger.debug("interpolated opacity 2: " + interpOpacity2);
        double interpTemp2 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getAtmBrightnessTemp(), atm[1].getAtmBrightnessTemp());
        logger.debug("interpolated temperature 2: " + interpTemp2);
        // Finally, interpolate opacity and temperature again as functions of PWV
        double finalOpacity = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                interpOpacity1, interpOpacity2);
        logger.debug("final opacity: " + finalOpacity);
        double finalTemp = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                interpTemp1, interpTemp2);
        logger.debug("final temperature: " + finalTemp);
        retVal[0] = finalOpacity;
        retVal[1] = finalTemp;
        return retVal;
    }
    
    private double interpolate(double x, double x1, double x2, double y1, double y2) {
        return y1 + ( y2 - y1 ) * ( x - x1 ) / ( x2 - x1 );
    }
    
    protected double getTsys(double decDeg, double latitudeDeg, double frequencyGHz, double pwv ) {
                
        double latitudeRad = Math.toRadians(latitudeDeg);
        double decRad = Math.toRadians(decDeg);

        double sinDec = Math.sin(decRad);
        double sinLat = Math.sin(latitudeRad);
        double cosDec = Math.cos(decRad);
        double cosLat = Math.cos(latitudeRad);
        double sinAltitude = sinDec * sinLat + cosDec * cosLat; 
        
        double Airmass = 1.0 / sinAltitude;

        double Tamb = 270; // Ambient temperature (260 - 280 K)
        double eta_feed = 0.95; // forward efficiency
        double Trx = getReceiverTemperature(frequencyGHz);
        
        double[] tmp = interpolateOpacityAndTemperature(pwv, frequencyGHz);
        double tau_zero = tmp[0];
        double Tatm = tmp[1];
        
        double f = Math.exp(tau_zero * Airmass);
        double Tcmb = 2.725; // [K]
        
        Trx  = planck(frequencyGHz, Trx);
        Tatm = planck(frequencyGHz, Tatm);
        Tamb = planck(frequencyGHz, Tamb);

        double Tsys =
            (Trx
                    + Tatm
                    * eta_feed
                    * (1.0 - 1 / f)
                    + Tamb * (1.0 - eta_feed));
        // GHz, K
        Tsys = f * Tsys + Tcmb;
        return Tsys;
    }

    /**
     * Planck correction for temperature
     * 
     * @param frequencyGHz [GHz]
     * @param temp[K]
     * @return corrected temperature[K]
     */
    private double planck(double frequencyGHz, double temp) {
        final double k = 1.38E-23;
        double f = frequencyGHz * 1.0E9; // [Hz]
        
        double arg0 = PLANCK * f / k;
        double ret = arg0 / (Math.exp(arg0 / temp) - 1.0);

        return ret;
    }
    
    /**
     * Return the receiver temperature
     * 
     * @param frequency in GHz.
     * @return receiver temperature in deg. K
     * 
     * TODO AB Replace this quick and dirty impl. by one using the Receiver class.
     * 
     * @author ab
     *
     */
    private double getReceiverTemperature (double frequency) {
        if (frequency >=31.3 && frequency <= 45.0)
            return 17.0;
        else if (frequency >=67.0 && frequency <= 90.0)
            return 30.0;
        else if (frequency >=84.0 && frequency <= 116.0)
            return 37.0;
        else if (frequency >=125.0 && frequency < 163.0)
            return 51.0;
        else if (frequency >=163.0 && frequency < 211.0)
            return 65.0;
        else if (frequency >=211.0 && frequency < 275.0)
            return 83.0;
        else if (frequency >=275.0 && frequency <= 373.0)
            return 147.0;
        else if (frequency >=385.0 && frequency <= 500.0)
            return 196.0;
        else if (frequency >=602.0 && frequency <= 720.0)
            return 175.0;
        else if (frequency >=787.0 && frequency <= 950.0)
            return 230.0;
        else
            return -1.0;
    }
    
}
