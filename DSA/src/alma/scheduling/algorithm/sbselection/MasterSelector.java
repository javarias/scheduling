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
package alma.scheduling.algorithm.sbselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class MasterSelector implements SchedBlockSelector {

    protected Collection<SchedBlockSelector> selectors; 
    protected SchedBlockDao sbDao;
    
    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    public Collection<SchedBlockSelector> getSelectors() {
        return selectors;
    }

    public void setSelectors(Collection<SchedBlockSelector> selectors) {
        this.selectors = selectors;
    }
    
    private List<SchedBlock> findSchedBlocks(Date ut, ArrayConfiguration arrConf) {
    	ArrayList<SchedBlock> ret = new ArrayList<>();
    	for (SchedBlock sb: sbDao.findAll()) {
    		boolean isSelec = true;
    		for (SchedBlockSelector sel: selectors) {
    			if (!sel.canBeSelected(sb, ut, arrConf)) {
    				isSelec = false;
    				break;
    			}
    		}
    		if (isSelec)
    			ret.add(sb);
    	}
    	return ret;
    }

//    @Override
//    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//        Conjunction conj = Restrictions.conjunction();
//        for (SchedBlockSelector selector : selectors) {
//            Criterion c = selector.getCriterion(ut, arrConf);
//            if (c == null)
//                System.out.println(selector.toString()
//                        + " has a null Criterion");
//            else
//                conj.add(c);
//        }
//        return conj;
//    }
    
    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        Date t1= new Date();
        List<SchedBlock> sbs = findSchedBlocks(ut, arrConf);
        Date t2 = new Date();
        System.out.println("Size of criteria query: " + sbs.size() );
        System.out.println("Time used: " + (t2.getTime() - t1.getTime()) + " ms");
        return sbs;
    }

    public boolean canBeSelected(SchedBlock sb, Date date) {
    	throw new java.lang.RuntimeException("Not Implemented");
    }

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date,
			ArrayConfiguration arrConf) {
		return canBeSelected(sb, date);
	}

}
