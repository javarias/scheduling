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
import java.util.List;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.CoordinatesUtil;
import alma.scheduling.utils.MoonAstroData;

public class MoonAvoidanceSelector extends AbstractBaseSelector {

    private SchedBlockDao sbDao;
    
    public MoonAvoidanceSelector(String selectorName) {
        super(selectorName);
    }
    
    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        MoonAstroData moonData = CoordinatesUtil.getMoonAstroData(ut);
        double highRa, lowRa, highDec, lowDec;
        highRa = moonData.getRA() + moonData.getAngularDiameter()/2;
        lowRa = moonData.getRA() - moonData.getAngularDiameter()/2;
        highDec = moonData.getDec() + moonData.getAngularDiameter()/2;
        lowDec = moonData.getDec() - moonData.getAngularDiameter()/2;
        List<SchedBlock> sbs = sbDao.findSchedBlocksOutOfArea(lowRa, highRa, lowDec, highDec);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date,
			ArrayConfiguration arrConf) {
		return canBeSelected(sb, date);
	}

	public boolean canBeSelected(SchedBlock sb, Date date) {
		MoonAstroData moonData = CoordinatesUtil.getMoonAstroData(date);
		double highRa, lowRa, highDec, lowDec;
		highRa = moonData.getRA() + moonData.getAngularDiameter() / 2;
		lowRa = moonData.getRA() - moonData.getAngularDiameter() / 2;
		highDec = moonData.getDec() + moonData.getAngularDiameter() / 2;
		lowDec = moonData.getDec() - moonData.getAngularDiameter() / 2;
		
		if (sb.getRepresentativeCoordinates().getRA() > highRa &&
				sb.getRepresentativeCoordinates().getRA() < lowRa &&
				sb.getRepresentativeCoordinates().getDec() > highDec &&
				sb.getRepresentativeCoordinates().getDec() < lowDec)
			return true;
		return false;
	}
	
	

//    @Override
//    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//        MoonAstroData moonData = CoordinatesUtil.getMoonAstroData(ut);
//        double highRa, lowRa, highDec, lowDec;
//        highRa = moonData.getRA() + moonData.getAngularDiameter() / 2;
//        lowRa = moonData.getRA() - moonData.getAngularDiameter() / 2;
//        highDec = moonData.getDec() + moonData.getAngularDiameter() / 2;
//        lowDec = moonData.getDec() - moonData.getAngularDiameter() / 2;
//        Conjunction conj = Restrictions.conjunction();
//        conj.add(Restrictions.ge("s.coordinates.RA", new Double(lowRa)));
//        conj.add(Restrictions.le("s.coordinates.RA", new Double(highRa)));
//        conj.add(Restrictions.ge("s.coordinates.Dec", new Double(lowDec)));
//        conj.add(Restrictions.le("s.coordinates.Dec", new Double(highDec)));
//        Criterion crit = Restrictions.not(conj);
//        return crit;
//    }

}
