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
 * "@(#) $Id: DynamicSchedulingAlgorithmImpl.java,v 1.12 2010/07/09 17:03:00 rhiriart Exp $"
 */
package alma.scheduling.algorithm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbranking.SchedBlockRanker;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DynamicSchedulingAlgorithmImpl implements DynamicSchedulingAlgorithm {
    
    private static Logger logger = LoggerFactory.getLogger(DynamicSchedulingAlgorithmImpl.class);

    private SchedBlockRanker ranker;
    /** The master selector, it should contains all the others preUpdateSelectors*/
    private Collection<SchedBlockSelector> preUpdateSelectors;
    private Collection<SchedBlockSelector> postUpdateSelectors;
    private Collection<ModelUpdater> updaters;
    private ArrayConfiguration array;
    private static int nProjects;
    /**
     * Stores the current SBs selected from selectors
     */
    private HashMap<Long, SchedBlock> sbs;
    private List<SBRank> ranks;
    
    public DynamicSchedulingAlgorithmImpl(){
        sbs =  new HashMap<Long, SchedBlock>();
    }

    public void setRanker(SchedBlockRanker ranker) {
        this.ranker = ranker;
    }

    public void setPostUpdateSelectors(Collection<SchedBlockSelector> postUpdateSelectors) {
        this.postUpdateSelectors = postUpdateSelectors;
    }
    
    public void setPreUpdateSelectors(Collection<SchedBlockSelector> preUpdateSelectors) {
        this.preUpdateSelectors = preUpdateSelectors;
    }
    
    public void setUpdaters(Collection<ModelUpdater> updaters) {
        this.updaters = updaters;
    }

    public SchedBlock getSelectedSchedBlock(){
	    return ranker.getBestSB(ranks);
	}
	
    public void rankSchedBlocks(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        rankSchedBlocks(calendar.getTime());
    }
    
	/* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#rankSchedBlocks()
     */
	public void rankSchedBlocks(Date ut){
	    ranks = ranker.rank(new ArrayList<SchedBlock>(sbs.values()), array, ut, nProjects);
	}

    @Override
    @Transactional
    //TODO: The selectSB function should be removed or improved
    public void selectCandidateSB(Date ut) throws NoSbSelectedException {
        sbs.clear();
        Date t1 = new Date();
        HashMap<Long, SchedBlock> pre = selectSBs(ut, preUpdateSelectors);
        Date t2 = new Date();        
        System.out.println("Pre Selectors takes: " + (t2.getTime() - t1.getTime()) + " ms");
        t1 = new Date();
        updateModel(ut, pre.values());
        t2 =  new Date();
        System.out.println("Update takes: " + (t2.getTime() - t1.getTime()) + " ms");
        t1= new Date();
        HashMap<Long, SchedBlock> post = selectSBs(ut, postUpdateSelectors);
        t2 = new Date();
        System.out.println("Post Selectors takes: " + (t2.getTime() - t1.getTime()) + " ms");
        sbs = post;
		if(sbs.size() == 0)
			throw new NoSbSelectedException("Intersection of Selectors doesn't contain common SchedBlocks");
		t1 = new Date();
	    Iterator it = sbs.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        SchedBlock tmpSB = (SchedBlock)pairs.getValue();
	        ObsProject tmpOP = tmpSB.getProject();
	        ObsUnit ou = tmpSB.getProject().getObsUnit();
	        hydrateObsUnit( ou );
	    }
	    t2 = new Date();
        System.out.println("Hidrate ObsUnit takes: " + (t2.getTime() - t1.getTime()) + " ms");	    

    }
    
    private void hydrateObsUnit(ObsUnit ou) {
        //getHibernateTemplate().lock(ou, LockMode.NONE);
        
        logger.trace("hydrating ObsUnit");
        logger.debug("ObsUnit class: " + ou.getClass().getName());
        if (ou == null)
            logger.warn("ObsUnit is null");
        if (ou instanceof SchedBlock) {
            logger.trace("hydrating SchedBlock");
            SchedBlock sb = (SchedBlock) ou;
            sb.getSchedulingConstraints().getMaxAngularResolution();
            logger.debug("successfully casted SchedBlock");
            return;
        } else if (ou instanceof ObsUnitSet) {
            logger.debug("hydrating ObsUnitSet");
            ObsUnitSet ous = (ObsUnitSet) ou;
            logger.debug("# of ObsUnits in ObsUnitSet: " + ous.getObsUnits().size());
            for (ObsUnit sou : ous.getObsUnits()) {
                hydrateObsUnit(sou);
            }
        }
    }
    
	 /* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#selectCandidateSB()
     */
	public void selectCandidateSB() throws NoSbSelectedException{
	    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
	    selectCandidateSB(calendar.getTime());
	}
	
	
	/* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#updateModel()
     */
	@Transactional
	public void updateModel(Date ut, Collection<SchedBlock> filteredSBs){
	    for(ModelUpdater updater: updaters)
	        if (updater.needsToUpdate(ut)) 
	            updater.update(ut, filteredSBs);
	}
	
	@Transactional
    public void updateModel(Date ut){
        for(ModelUpdater updater: updaters)
            if (updater.needsToUpdate(ut)) 
                updater.update(ut);
    }

    @Override
    public ArrayConfiguration getArray() {
        return array;
    }

    @Override
    public void setArray(ArrayConfiguration arrConf) {
        this.array =  arrConf;
    }

    @Override
    public void setVerboseLevel(VerboseLevel verboseLvl) {
        AbstractBaseSelector.setVerboseLevel(verboseLvl);
        AbstractBaseRanker.setVerboseLevel(verboseLvl);
        
    }
	
    private HashMap<Long, SchedBlock> selectSBs(Date ut, Collection<SchedBlockSelector> selectors) throws NoSbSelectedException{
        HashMap<Long, SchedBlock> internal_sbs = new HashMap<Long, SchedBlock>();
        ArrayList<HashMap<Long, SchedBlock>> selectedSbs = 
            new ArrayList<HashMap<Long,SchedBlock>>();
        int i = 0;
        for (SchedBlockSelector s : selectors) {
            selectedSbs.add(new HashMap<Long, SchedBlock>());
            try {
                for (SchedBlock sb : s.select(ut, array))
                    selectedSbs.get(i).put(sb.getId(), sb);
            } catch (NoSbSelectedException e) {
                logger.warn("DSA cannot continue if selector " + s.toString()
                        + " cannot get at least one SB");
                throw new NoSbSelectedException(e.getMessage());
            } catch (NullPointerException e) {
                logger.warn("DSA cannot continue if selector " + s.toString()
                        + " cannot get at least one SB");
            }
            i++;
        }
        HashMap<Long, SchedBlock> smallestSet = selectedSbs.get(0);
        for(i = 1; i<selectedSbs.size(); i++){
            if (smallestSet.size() > selectedSbs.get(i).size())
                smallestSet = selectedSbs.get(i);
        }
        // Check if the SchedBlock was selected by the others selectors
        for(SchedBlock sb: smallestSet.values()){
            boolean verified = true;
            for(i = 0; i < selectedSbs.size(); i++){
                verified = true;
                if (selectedSbs.get(i) == smallestSet)
                    continue;
                if(selectedSbs.get(i).get(sb.getId()) == null){
                    verified = false;
                    break;
                }
            }
            // Add to the selected sb if that sb was selected by all the others selectors
            if(verified)
                internal_sbs.put(sb.getId(), sb);
        }
        if (internal_sbs.isEmpty()){
            logger.warn("DSA cannot continue if it doesn't have SBs to rank");
            String strCause = "Cannot get any SB valid to be ranked using ";
            for(SchedBlockSelector s: selectors)
                strCause += s.toString() + " ";
            throw new NoSbSelectedException(strCause);
        }   
        
        return internal_sbs;
    }

    public static int getnProjects() {
        return nProjects;
    }

    public static void setnProjects(int nProjects) {
        DynamicSchedulingAlgorithmImpl.nProjects = nProjects;
    }
    
}
