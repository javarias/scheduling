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
package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.TemporalConstraint;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.TimeUtil;

public class FieldSourceObservableSelector extends AbstractBaseSelector {
    
    private static Logger logger = LoggerFactory.getLogger(FieldSourceObservableSelector.class);
    
    public FieldSourceObservableSelector(String selectorName) {
        super(selectorName);
    }

    // --- Spring set properties and accessors ---
    
    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }

    // --- SchedBlockSelector impl --

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        double longitude = configDao.getConfiguration().getArrayCenterLongitude();
        double lst = TimeUtil.getLocalSiderealTime(ut, longitude);
        logger.debug("lst = " + lst);
        logger.debug("time = " + ut.toString());
        logger.debug("longitude = " + longitude);
        Collection<SchedBlock> sbs = 
            schedBlockDao.findSchedBlocksWithVisibleRepresentativeTarget(lst);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date,
			ArrayConfiguration arrConf) {
		return canBeSelected(sb, date);
	}

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date) {
		double longitude = configDao.getConfiguration().getArrayCenterLongitude();
        double lst = TimeUtil.getLocalSiderealTime(date, longitude);
		Target repTarget = sb.getSchedulingConstraints().getRepresentativeTarget();
		if (repTarget.getSource().getObservability().getAlwaysVisible()) 
			return true;
		else if (repTarget.getSource().getObservability().getRisingTime() <
				repTarget.getSource().getObservability().getSettingTime() ) {
			if (repTarget.getSource().getObservability().getRisingTime() < lst &&
					repTarget.getSource().getObservability().getSettingTime() > lst)
				return true;
		} else if (repTarget.getSource().getObservability().getRisingTime() >
				repTarget.getSource().getObservability().getSettingTime() ) {
			if (repTarget.getSource().getObservability().getRisingTime() < lst ||
					repTarget.getSource().getObservability().getSettingTime() > lst)
				return true;
		}
		return false;
//TODO: Add time constrained observations
	}
	
	

//    @Override
//    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//        double longitude = configDao.getConfiguration().getArrayCenterLongitude();
//        double lst = TimeUtil.getLocalSiderealTime(ut, longitude);
//        System.out.println("lst = " + lst);
//        System.out.println("time = " + ut.toString());
//        System.out.println("longitude = " + longitude);
//        /*
//         *         from SchedBlock sb where 
//       sb.schedulingConstraints.representativeTarget.source.observability.alwaysVisible = true
//       or
//       (sb.schedulingConstraints.representativeTarget.source.observability.risingTime <
//        sb.schedulingConstraints.representativeTarget.source.observability.settingTime and
//        sb.schedulingConstraints.representativeTarget.source.observability.risingTime < ? and
//        sb.schedulingConstraints.representativeTarget.source.observability.settingTime > ?) or
//       (sb.schedulingConstraints.representativeTarget.source.observability.risingTime >
//        sb.schedulingConstraints.representativeTarget.source.observability.settingTime and
//       (sb.schedulingConstraints.representativeTarget.source.observability.risingTime < ? or
//        sb.schedulingConstraints.representativeTarget.source.observability.settingTime > ?)) */
//        Conjunction conj1 = Restrictions.conjunction();
//        conj1.add(Restrictions.ltProperty("s.observability.risingTime",
//                "s.observability.settingTime"));
//        conj1.add(Restrictions
//                .lt("s.observability.risingTime", new Double(lst)));
//        conj1.add(Restrictions.gt("s.observability.settingTime",
//                new Double(lst)));
//
//        Disjunction disj3 = Restrictions.disjunction();
//        disj3.add(Restrictions
//                .lt("s.observability.risingTime", new Double(lst)));
//        disj3.add(Restrictions.gt("s.observability.settingTime",
//                new Double(lst)));
//        Criterion crit2 = Restrictions.and(Restrictions.gtProperty(
//                "s.observability.risingTime", "s.observability.settingTime"),
//                disj3);
//        Criterion crit = Restrictions.or(conj1, crit2);
//        crit = Restrictions.or(Restrictions.eq("s.observability.alwaysVisible", true), crit);
//        return crit;
//    }

}
