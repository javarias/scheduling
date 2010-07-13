package alma.scheduling.algorithm.weather;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.astro.SystemTemperatureCalculator;
import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherDependentVariables;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;

public class MemoryWeatherUpdater extends WeatherUpdater implements
        ModelUpdater {

    private HashMap<Date, PWV> cache;
    
    private static Logger logger = LoggerFactory.getLogger(MemoryWeatherUpdater.class);
    
    public MemoryWeatherUpdater(){
        super();
        cache = new HashMap<Date, PWV>();
    }
    
    @Override
    public synchronized void update(Date date, Collection<SchedBlock> sbs) {
        /*this is to assure of the atomicity of the update operation*/
        if(needsToUpdate(date) == false)
            return;
        lastUpdate = date;
        
        logger.trace("entering");
        logger.debug("updating for time " + date);
        
        if(latitude == null)
            latitude = configDao.getConfiguration().getArrayCenterLatitude();

        // get current PWV
        TemperatureHistRecord tr = weatherDao.getTemperatureForTime(date);
        logger.info("temperature record: time = " + tr.getTime() + "; value = "
                + tr.getValue());
        HumidityHistRecord hr = weatherDao.getHumidityForTime(date);
        logger.info("humidity record: time = " + hr.getTime() + "; value = "
                + hr.getValue());
        double pwv = estimatePWV(hr.getValue(), tr.getValue()); // mm

        long deltaT = (long) (projTimeIncr * 3600.0 * 1000.0); // delta T in
                                                               // milliseconds
        Date projDate = new Date(date.getTime() + deltaT);
        TemperatureHistRecord ptr = weatherDao.getTemperatureForTime(projDate);
        HumidityHistRecord phr = weatherDao.getHumidityForTime(projDate);
        double ppwv = estimatePWV(phr.getValue(), ptr.getValue()); // projected
                                                                   // PWV, in mm

        for (Iterator<SchedBlock> iter = sbs.iterator(); iter.hasNext();) {
            SchedBlock sb = iter.next();
            double frequency = sb.getSchedulingConstraints()
                    .getRepresentativeFrequency(); // GHz
            Target target = sb.getSchedulingConstraints()
                    .getRepresentativeTarget();
            FieldSource src = target.getSource();
            double decl = src.getCoordinates().getDec(); // degrees

            double[] tmp = interpolateOpacityAndTemperature(pwv, frequency);
            double tau_zero = tmp[0];
            double Tatm = tmp[1];
            double tsys = SystemTemperatureCalculator.getTsys(decl, latitude,
                    frequency, tau_zero, Tatm);
            logger.info("tsys: " + tsys);

            tmp = interpolateOpacityAndTemperature(ppwv, frequency);
            tau_zero = tmp[0];
            Tatm = tmp[1];
            double ptsys = SystemTemperatureCalculator.getTsys(decl, latitude,
                    frequency, tau_zero, Tatm);

            WeatherDependentVariables vars = new WeatherDependentVariables();
            vars.setTsys(tsys);
            vars.setProjectedTsys(ptsys);
            vars.setProjectionTimeIncr(projTimeIncr);
            sb.setWeatherDependentVariables(vars);
        }
    }

    @Override
    public void update(Date date, SchedBlock sb) {
        if(latitude == null)
            latitude = configDao.getConfiguration().getArrayCenterLatitude();
        if(cache.keySet().size() > 100)
            cache.clear();
        if (cache.get(date) == null) {
            // get current PWV
            System.out.println("Start Calculations");
            Date t1 = new Date();
            TemperatureHistRecord tr = weatherDao.getTemperatureForTime(date);
            logger.info("temperature record: time = " + tr.getTime()
                    + "; value = " + tr.getValue());
            HumidityHistRecord hr = weatherDao.getHumidityForTime(date);
            logger.info("humidity record: time = " + hr.getTime()
                    + "; value = " + hr.getValue());
            double pwv = estimatePWV(hr.getValue(), tr.getValue()); // mm

            long deltaT = (long) (projTimeIncr * 3600.0 * 1000.0); // delta T in
            // milliseconds
            Date projDate = new Date(date.getTime() + deltaT);
            TemperatureHistRecord ptr = weatherDao
                    .getTemperatureForTime(projDate);
            HumidityHistRecord phr = weatherDao.getHumidityForTime(projDate);
            double ppwv = estimatePWV(phr.getValue(), ptr.getValue()); // projected
            // PWV, in mm
            PWV tmp = new PWV();
            tmp.setPwv(pwv);
            tmp.setPpwv(ppwv);
            cache.put(date, tmp);
            Date t2 = new Date();
            System.out.println("Weather Calculations takes: " + (t2.getTime() - t1.getTime()) + " ms");
        }
        double pwv = cache.get(date).getPwv();
        double ppwv = cache.get(date).getPpwv();
        
        // inside the for of method of above
        double frequency = sb.getSchedulingConstraints()
                .getRepresentativeFrequency(); // GHz
        Target target = sb.getSchedulingConstraints().getRepresentativeTarget();
        FieldSource src = target.getSource();
        double decl = src.getCoordinates().getDec(); // degrees

        double[] tmp = interpolateOpacityAndTemperature(pwv, frequency);
        double tau_zero = tmp[0];
        double Tatm = tmp[1];
        double tsys = SystemTemperatureCalculator.getTsys(decl, latitude,
                frequency, tau_zero, Tatm);
        logger.info("tsys: " + tsys);

        tmp = interpolateOpacityAndTemperature(ppwv, frequency);
        tau_zero = tmp[0];
        Tatm = tmp[1];
        double ptsys = SystemTemperatureCalculator.getTsys(decl, latitude,
                frequency, tau_zero, Tatm);

        WeatherDependentVariables vars = new WeatherDependentVariables();
        vars.setTsys(tsys);
        vars.setProjectedTsys(ptsys);
        vars.setProjectionTimeIncr(projTimeIncr);
        sb.setWeatherDependentVariables(vars);
    }

    private class PWV{
        private double pwv;
        private double ppwv;
        
        public double getPwv() {
            return pwv;
        }
        
        public void setPwv(double pwv) {
            this.pwv = pwv;
        }
        
        public double getPpwv() {
            return ppwv;
        }
        
        public void setPpwv(double ppwv) {
            this.ppwv = ppwv;
        }
        
    }
}
