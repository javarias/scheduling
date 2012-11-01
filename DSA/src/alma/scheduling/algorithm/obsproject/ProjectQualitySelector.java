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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class ProjectQualitySelector extends AbstractBaseSelector {

	private Set<String> allowedGrades;
	private ObsProjectDao prjDao;
	
    public ProjectQualitySelector(String selectorName) {
        super(selectorName);
    }

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
    	ArrayList<ScienceGrade> enumAllowedGrades = new ArrayList<ScienceGrade>();
    	if (allowedGrades != null || allowedGrades.size() > 0) {
    		for(String grade: allowedGrades) {
    			try {
    				enumAllowedGrades.add(ScienceGrade.valueOf(grade));
    			} catch (RuntimeException ex) {
    				System.out.println("Unknown grade detected: " + grade);
    			}
    		}
    		List<String> uids = prjDao.getObsProjectsUidsbySciGrade(enumAllowedGrades);
    		if (uids.size() == 0)
    			return null;
    		return Restrictions.in("projectUid", uids);
    	}
        return null;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        return null;
    }

	public Set<String> getAllowedGrades() {
		return allowedGrades;
	}

	public void setAllowedGrades(Set<String> allowedGrades) {
		this.allowedGrades = allowedGrades;
	}

	public ObsProjectDao getPrjDao() {
		return prjDao;
	}

	public void setPrjDao(ObsProjectDao prjDao) {
		this.prjDao = prjDao;
	}

    
}
