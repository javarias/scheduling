package alma.scheduling.datamodel.weather.dao;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.weather.AtmParameters;

@Transactional
public class AtmParametersDaoImpl extends GenericDaoImpl implements AtmParametersDao {

    /* freq , pwv*/
    private AtmParameters[][] atmParamsCache = null; 
    
    public AtmParametersDaoImpl() {

    }
    
    @Override
    public void loadAtmParameter(Object domainObject) {
        getHibernateTemplate().save(domainObject);
    }

    @Override
    public void loadAtmParameters(Set<AtmParameters> params) {
        saveOrUpdate(params);
    }
    
    @Override
    public Double[] getEnclosingPwvInterval(Double pwv) {
        if(atmParamsCache == null)
            fillCache();
        Double[] retVal = new Double[2];
        if (getPwvPosition(pwv) > 0 && getPwvPosition(pwv) < 6){
            retVal[0] = atmParamsCache[0][getPwvPosition(pwv) - 1].getPWV();
            retVal[1] = atmParamsCache[0][getPwvPosition(pwv)].getPWV();
        }
        else {
            AtmParameters tmp = atmParamsCache[0][getPwvPosition(pwv)];
            retVal[0] = atmParamsCache[0][getPwvPosition(pwv)].getPWV();
            retVal[1] = atmParamsCache[0][getPwvPosition(pwv)].getPWV();
        }
        return retVal;
    }

    @Override
    public AtmParameters[] getEnclosingIntervalForPwvAndFreq(Double pwv, Double freq) {
        if(atmParamsCache == null)
            fillCache();
        AtmParameters[] retVal = new AtmParameters[2];
        retVal[0] = atmParamsCache[getFreqPosition(freq)][getPwvPosition(pwv)];
        retVal[1] = atmParamsCache[getFreqPosition(freq) + 1][getPwvPosition(pwv) + 1];
        return retVal;
    }

    @Override
    public List<AtmParameters> getAllAtmParameters() {
        return this.findAll(AtmParameters.class);
    }
    
    /* returns the lower bound, always*/
    private int getFreqPosition(Double freq){
        int pos = new Integer((int)(freq * 10 - 200));
        return pos;
    }
    
    /* returns the upper bound, except for pwv > 5.186 which return the lower bound*/
    private int getPwvPosition(Double pwv){
        if (pwv == 0.4722 || pwv < 0.4722)
            return 0;
        else if (pwv == 0.658 || (pwv > 0.4722 && pwv < 0.658))
            return 1;
        else if (pwv == 0.9134 || (pwv > 0.658 && pwv < 0.9134))
            return 2;
        else if (pwv == 1.262 || (pwv > 0.9134 && pwv < 1.262))
            return 3;
        else if (pwv == 1.796 || (pwv > 1.262 && pwv < 1.796))
            return 4;
        else if (pwv == 2.748 || (pwv > 1.796 && pwv < 2.748))
            return 5;
        else if (pwv == 5.186 || ( pwv > 2.748 && pwv < 5.186 ) || pwv > 5.186)
            return 6;
        return -1;
    }
    
    private void fillCache(){
        logger.debug("Filling ATM Parameters Cache");
        List<AtmParameters> atmParams = getAllAtmParameters();
        atmParamsCache = new AtmParameters[(10000 - 200 + 1) * 7][7];
        for(AtmParameters atm: atmParams){
            if(atmParamsCache[getFreqPosition(atm.getFreq())]
                           [getPwvPosition(atm.getPWV())] != null){
                logger.debug(atm.getFreq() + "->" + getFreqPosition(atm.getFreq()) + " " +
                        atm.getPWV() + "->" + getPwvPosition(atm.getPWV()));
                logger.debug(atm.getFreq() * 10 - 200);
            }
            atmParamsCache[getFreqPosition(atm.getFreq())]
                           [getPwvPosition(atm.getPWV())] = atm;         
        }
    }
}
