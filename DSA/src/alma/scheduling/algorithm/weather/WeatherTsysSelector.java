/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */
package alma.scheduling.algorithm.weather;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class WeatherTsysSelector extends AbstractBaseSelector {

    public WeatherTsysSelector(String selectorName) {
        super(selectorName);
    }

    private static Logger logger = LoggerFactory.getLogger(WeatherTsysSelector.class);
    
    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    private double tsysVariation;
    public void setTsysVariation(double tsysVariation) {
        this.tsysVariation = tsysVariation;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        logger.debug("selecting SBs");
        List<SchedBlock> selSBs =
            schedBlockDao.findSchedBlocksWithoutTooMuchTsysVariation(tsysVariation);
        logger.debug("# of SBs selected:" + selSBs.size());
        for (SchedBlock sb : selSBs) {
            logger.debug("selected SB ID: " + sb.getId());
        }
        printVerboseInfo(selSBs, arrConf.getId(), ut);
        return selSBs;
    }

//    @Override
//    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//        /*
//         * from SchedBlock sb where ((sb.weatherDependentVariables.projectedTsys
//         * - sb.weatherDependentVariables.tsys) /
//         * sb.weatherDependentVariables.tsys) < ?
//         */
//        Criterion crit = Restrictions
//                .sqlRestriction("( this_.WEATHER_VARS_PROJ_TSYS - "
//                        + "this_.WEATHER_VARS_TSYS ) /  this_.WEATHER_VARS_TSYS < "
//                        + tsysVariation);
//        return crit;
//    }

    @Override
    public boolean canBeSelected(SchedBlock sb, Date date, ArrayConfiguration arrConf) {
        return ((sb.getWeatherDependentVariables().getProjectedTsys() 
                - sb.getWeatherDependentVariables().getTsys()) / 
                sb.getWeatherDependentVariables().getProjectedTsys() ) < tsysVariation;
    }

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date,
			ArrayConfiguration arrConf) {
		return canBeSelected(sb, date);
	}
    
    

}
