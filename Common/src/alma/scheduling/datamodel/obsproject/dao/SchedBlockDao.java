package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockDao extends GenericDao {
    List<SchedBlock> findAll();
    List<SchedBlock> findSchedBlocksWithVisibleRepresentativeTarget(double lst);
    List<SchedBlock> findSchedBlocksByEstimatedExecutionTime(double time);
    void hydrateSchedBlockObsParams(SchedBlock schedBlock);
    public List<SchedBlock> findSchedBlocksWithoutTooMuchTsysVariation(double variation);
    public List<SchedBlock> findSchedBlocksWithEnoughTimeInExecutive(Executive exec, ObservingSeason os) throws NullPointerException;
    public List<SchedBlock> findSchedBlocksBetweenHourAngles(double lowLimit, double highLimit);
    public List<SchedBlock> findSchedBlocksOutOfArea(double lowRaLimit,
            double highRaLimit, double lowDecLimit, double highDecLimit);
}
