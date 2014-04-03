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
package alma.scheduling.algorithm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.algorithm.sbranking.AbstractBaseRanker;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbranking.SchedBlockRanker;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DynamicSchedulingAlgorithmImpl implements DynamicSchedulingAlgorithm {
    
    private static Logger logger = LoggerFactory.getLogger(DynamicSchedulingAlgorithmImpl.class);

    private SchedBlockRanker ranker;
    /** it should contains all the Pre-Update Selectors*/
    private Collection<SchedBlockSelector> preUpdateSelectors;
    private Collection<SchedBlockSelector> postUpdateSelectors;
    private Collection<ModelUpdater> updaters;
    private Collection<ModelUpdater> firstRunUpdaters;
    private ArrayConfiguration array;
    private static int nProjects;
    /**
     * Stores the current SBs selected from selectors
     */
    private HashMap<String, SchedBlock> sbs;
    private List<SBRank> ranks;
    
    public DynamicSchedulingAlgorithmImpl(){
        sbs =  new HashMap<String, SchedBlock>();
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
	
    public Collection<ModelUpdater> getFirstRunUpdaters() {
        return firstRunUpdaters;
    }

    public void setFirstRunUpdaters(Collection<ModelUpdater> firstRunUpdaters) {
        this.firstRunUpdaters = firstRunUpdaters;
    }

    public void rankSchedBlocks(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        rankSchedBlocks(calendar.getTime());
    }
    
	/* (non-Javadoc)
     * @see alma.scheduling.algorithm.DynamicSchedulingAlgorithm#rankSchedBlocks()
     */
    @Override
	public List<SBRank> rankSchedBlocks(Date ut){
	    ranks = ranker.rank(new ArrayList<SchedBlock>(sbs.values()), array, ut, nProjects);
	    return ranks;
	}

    @Override
    //TODO: The selectSB function should be removed or improved
    public void selectCandidateSB(Date ut) throws NoSbSelectedException {
        sbs.clear();
        Date t1 = new Date();
        HashMap<String, SchedBlock> pre = selectSBs(ut, preUpdateSelectors);
        Date t2 = new Date();        
        System.out.println("Pre Selectors takes: " + (t2.getTime() - t1.getTime()) + " ms");
//        t1 = new Date();
//        updateModel(ut, pre.values());
//        t2 =  new Date();
//        System.out.println("Update takes: " + (t2.getTime() - t1.getTime()) + " ms");
//        t1= new Date();
//        HashMap<Long, SchedBlock> post = selectSBs(ut, postUpdateSelectors);
//        t2 = new Date();
//        System.out.println("Post Selectors takes: " + (t2.getTime() - t1.getTime()) + " ms");
        sbs = pre;
		if(sbs.size() == 0)
			throw new NoSbSelectedException("Intersection of Selectors doesn't contain common SchedBlocks");    	
    }
    
    @Override
    public void updateCandidateSB(Date ut) throws NoSbSelectedException {
        Date t1 = new Date();
        updateModel(ut, sbs.values());
        Date t2 =  new Date();
        System.out.println("Update takes: " + (t2.getTime() - t1.getTime()) + " ms");
        t1= new Date();
        HashMap<String, SchedBlock> post = selectSBs(ut, postUpdateSelectors);
        t2 = new Date();
        System.out.println("Post Selectors took: " + (t2.getTime() - t1.getTime()) + " ms");
        sbs = post;
		if(sbs.size() == 0)
			throw new NoSbSelectedException("Intersection of Selectors doesn't contain common SchedBlocks");    	
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
	public void updateModel(Date ut, Collection<SchedBlock> filteredSBs){
	    for(ModelUpdater updater: updaters)
	        if (updater.needsToUpdate(ut)) 
	            updater.update(ut, filteredSBs, array);
	}
	
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
	
    private HashMap<String, SchedBlock> selectSBs(Date ut, Collection<SchedBlockSelector> selectors) throws NoSbSelectedException{
        HashMap<String, SchedBlock> internal_sbs = new HashMap<>();
        ArrayList<HashMap<String, SchedBlock>> selectedSbs = 
            new ArrayList<>();
        int i = 0;
        for (SchedBlockSelector s : selectors) {
            selectedSbs.add(new HashMap<String, SchedBlock>());
            try {
                for (SchedBlock sb : s.select(ut, array))
                    selectedSbs.get(i).put(sb.getUid(), sb);
            } catch (NoSbSelectedException e) {
                logger.warn("DSA cannot continue if selector " + s.toString()
                        + " cannot get at least one SB");
                throw new NoSbSelectedException(e);
            } catch (NullPointerException e) {
                logger.warn("DSA cannot continue if selector " + s.toString()
                        + " cannot get at least one SB");
                throw new NoSbSelectedException(e);
            }
            i++;
        }
        HashMap<String, SchedBlock> smallestSet = selectedSbs.get(0);
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
                internal_sbs.put(sb.getUid(), sb);
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

    @Override
    public void initialize(Date ut) {
        for(ModelUpdater updater: firstRunUpdaters)
            if (updater.needsToUpdate(ut)) 
                updater.update(ut);
    }
    
}
