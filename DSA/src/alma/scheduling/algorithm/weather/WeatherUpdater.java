package alma.scheduling.algorithm.weather;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherDependentVariables;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;

public class WeatherUpdater implements ModelUpdater {

    static final double BOLTZMANN = 1.38e-16 * 1.0e23;  //[Jy/K]
    static final double PLANCK    = 6.626e-34; // [J s]
    static final double LIGHTSPEED= 2.99792458e8; //[m/s]

    private static Logger logger = LoggerFactory.getLogger(WeatherUpdater.class);
        
    private ConfigurationDao configDao;
    
    private AtmParametersDao dao;

    private SchedBlockDao schedBlockDao;
    
    private WeatherHistoryDAO weatherDao;
    
    private Double projTimeIncr;
    
    public void setDao(AtmParametersDao dao) {
        this.dao = dao;
    }
    
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    public void setWeatherDao(WeatherHistoryDAO weatherDao) {
        this.weatherDao = weatherDao;
    }
    
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }
    
    public void setProjTimeIncr(Double projTimeIncr) {
        this.projTimeIncr = projTimeIncr;
    }
    
    @Override
    public boolean needsToUpdate(Date date) {
        return true;
    }

    @Override
    public void update(Date date) {
        logger.trace("entering");
        logger.debug("updating for time " + date);
        
        double latitude = configDao.getConfiguration().getArrayCenterLatitude();

        // get current PWV
        TemperatureHistRecord tr = weatherDao.getTemperatureForTime(date);
        logger.info("temperature record: time = " + tr.getTime() + "; value = " + tr.getValue());
        HumidityHistRecord hr = weatherDao.getHumidityForTime(date);
        logger.info("humidity record: time = " + hr.getTime() + "; value = " + hr.getValue());        
        double pwv = estimatePWV(hr.getValue(), tr.getValue()); // mm

        projTimeIncr = 0.5;
        long deltaT = (long)( projTimeIncr * 3600.0 * 1000.0 ); // delta T in milliseconds
        Date projDate = new Date(date.getTime() + deltaT);
        TemperatureHistRecord ptr = weatherDao.getTemperatureForTime(projDate);
        HumidityHistRecord phr = weatherDao.getHumidityForTime(projDate);
        double ppwv = estimatePWV(phr.getValue(), ptr.getValue()); // projected PWV, in mm
        
        List<SchedBlock> sbs = schedBlockDao.findAll(); // replace this by a selector
        for (Iterator<SchedBlock> iter = sbs.iterator(); iter.hasNext();) {
            SchedBlock sb = iter.next();
            double frequency = sb.getSchedulingConstraints().getRepresentativeFrequency(); // GHz
            Target target = sb.getSchedulingConstraints().getRepresentativeTarget();
            FieldSource src = target.getSource();
            double decl = src.getCoordinates().getDec(); // degrees
            double tsys = getTsys(decl, latitude, frequency, pwv);
            logger.info("tsys: " + tsys);
            double ptsys = getTsys(decl, latitude, frequency, ppwv);
            WeatherDependentVariables vars = new WeatherDependentVariables();
            vars.setTsys(tsys);
            vars.setProjectedTsys(ptsys);
            vars.setProjectionTimeIncr(projTimeIncr);
            sb.setWeatherDependentVariables(vars);
            schedBlockDao.saveOrUpdate(sb);            
        }        
    }

    /**
     * Estimate Precipitable Water Vapor (PWV)
     * 
     * @param humidity Humidty [0/1]
     * @param temperature Temperature [C]
     * @return PWV in mm
     */
    private double estimatePWV(double humidity, double temperature) {
        double h; // PWV
        double P_0; // water vapor partial pressure
        double theta; // inverse temperature [K]
        double m_w = 18 * 1.660538782E-27; // mass of a water molecule (18 amu in Kg)
        double H = 1.5E3; // scale height of water vapor distribution
        double rho_l = 1e3; // desity of water [Kg/m^3]
        double k = 1.3806503E-23; // Boltzmann constant [m^2 Kg s^-2 K^-1]
        double T_0; // ground temperature in Kelvins
        
        // convert temperature to degrees Kelvin
        T_0 = temperature + 273.15;
        theta = 300.0/T_0;
        P_0 = 2.409E12 * humidity * Math.pow(theta, 4) * Math.exp(-22.64 * theta);
        logger.debug("P_0 = " + P_0);
        
        h = ( m_w * P_0 * H ) / ( rho_l * k * T_0 );
        logger.debug("h = " + h);
        return h * 1E3; // in mm
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
        
        // Finally, interpolate opacity and temperature again as functions of PWV.
        // Do this only if the PWV's are different, if not just return the first interpolated
        // values.
        if (pwvInterval[0] != pwvInterval[1]) {
            double finalOpacity = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                    interpOpacity1, interpOpacity2);
            logger.debug("final opacity: " + finalOpacity);
            double finalTemp = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                    interpTemp1, interpTemp2);
            logger.debug("final temperature: " + finalTemp);
            retVal[0] = finalOpacity;
            retVal[1] = finalTemp;
        } else {
            retVal[0] = interpOpacity1;
            retVal[1] = interpTemp1;
        }
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
        double sinAltitude = sinDec * sinLat + cosDec * cosLat; // missing cosH?
        
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
