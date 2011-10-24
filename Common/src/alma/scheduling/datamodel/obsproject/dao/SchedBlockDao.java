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
package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockDao extends GenericDao {
    List<SchedBlock> findAll();
    int countAll();
    List<SchedBlock> findSchedBlocksWithVisibleRepresentativeTarget(double lst);
    List<SchedBlock> findSchedBlocksByEstimatedExecutionTime(double time);
    void hydrateSchedBlockObsParams(SchedBlock schedBlock);
    public List<SchedBlock> findSchedBlocksWithoutTooMuchTsysVariation(double variation);
    public List<SchedBlock> findSchedBlocksWithEnoughTimeInExecutive(Executive exec, ObservingSeason os) throws NullPointerException;
    public List<SchedBlock> findSchedBlocksBetweenHourAngles(double lowLimit, double highLimit);
    public List<SchedBlock> findSchedBlocksOutOfArea(double lowRaLimit,
            double highRaLimit, double lowDecLimit, double highDecLimit);
    public List<SchedBlock> findSchedBlockWithStatusReady();
    public List<SchedBlock> findSchedBlockBetweenFrequencies(double lowFreq, double highFreq);
    
    /**
     * @param crit The Hibernate Criteria used to do the search
     * 
     * @return The Sched Blocks found according to the criteria
     */
    public List<SchedBlock> findSchedBlocks(Criterion crit);
    
    public List<SchedBlock> findSchedBlocks(Criterion crit, List<SchedBlock> sbs);
    
    public SchedBlock findByEntityId(String entityId);
    
    public List<SchedBlock> findSchedBlocksForProject(ObsProject project);
    
    public void hydrateObsUnitSet(ObsUnitSet ous);
}
