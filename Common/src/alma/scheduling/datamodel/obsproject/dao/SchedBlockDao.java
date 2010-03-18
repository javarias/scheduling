package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockDao extends GenericDao {
    List<SchedBlock> findAll();
    List<SchedBlock> findSchedBlocksWithVisibleRepresentativeTarget(double lst);
    void hydrateSchedBlockObsParams(SchedBlock schedBlock);
}
