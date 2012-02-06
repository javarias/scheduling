/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.datamodel.weather.dao;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.weather.AtmParameters;

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
    @Transactional
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
        retVal[1] = atmParamsCache[getFreqPosition(freq) + 1][(getPwvPosition(pwv) == 6 ? getPwvPosition(pwv) : getPwvPosition(pwv) + 1)];
        return retVal;
    }

    @Override
    public List<AtmParameters> getAllAtmParameters() {
        return this.findAll(AtmParameters.class);
    }
    
    /* returns the lower bound, always*/
    private int getFreqPosition(Double freq){
    	if (freq < 20) {
    		logger.error("Frequency is less than 20 Ghz, check your project and fix it. Using the minimum usable 20 Ghz");
    		freq = 20.0;
    	}
        int pos = new Integer((int)(freq * 10 - 200));
        return pos;
    }
    
    /* returns the upper bound, except for pwv > 5.186 which return the lower bound*/
    private int getPwvPosition(Double pwv){
        if (pwv <= 0.4722)
            return 0;
        else if (pwv > 0.4722 && pwv <= 0.658)
            return 1;
        else if (pwv > 0.658 && pwv <= 0.9134)
            return 2;
        else if (pwv > 0.9134 && pwv <= 1.262)
            return 3;
        else if (pwv > 1.262 && pwv <= 1.796)
            return 4;
        else if (pwv > 1.796 && pwv <= 2.748)
            return 5;
        else if (pwv > 2.748) //  pwv > 5.186
            return 6;
        return 0;
    }
    
    @Transactional
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
