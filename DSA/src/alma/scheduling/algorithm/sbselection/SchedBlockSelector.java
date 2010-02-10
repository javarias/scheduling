package alma.scheduling.algorithm.sbselection;

import java.util.Collection;

import alma.scheduling.datamodel.obsproject.SchedBlock;

public interface SchedBlockSelector {

    public Collection<SchedBlock> select() throws NoSbSelectedException;
}
