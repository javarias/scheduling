package alma.scheduling.datamodel.obsproject.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.Target;

@Transactional
public class SchedBlockDaoImpl extends GenericDaoImpl implements SchedBlockDao {

    @Override
    public List<SchedBlock> findAll() {
        List<SchedBlock> sbs = super.findAll(SchedBlock.class);
        for (Iterator<SchedBlock> iter = sbs.iterator(); iter.hasNext();) {
            SchedBlock sb = iter.next();
            sb.getSchedulingConstraints().getRepresentativeFrequency();
            Set<Target> targets = sb.getTargets();
            for (Iterator<Target> targetIter = targets.iterator(); targetIter.hasNext();) {
                Target target = targetIter.next();
                FieldSource src = target.getSource();
                SkyCoordinates coords = src.getCoordinates();
            }
        }        
        return sbs;
    }
    
}
