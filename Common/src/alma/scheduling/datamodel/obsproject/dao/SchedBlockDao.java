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
