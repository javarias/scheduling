package alma.scheduling.datamodel.obsproject.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
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
            // hydrate Targets and FieldSources
            Set<Target> targets = sb.getTargets();
            for (Iterator<Target> targetIter = targets.iterator(); targetIter.hasNext();) {
                Target target = targetIter.next();
                FieldSource src = target.getSource();
                SkyCoordinates coords = src.getCoordinates();
            }
        }        
        return sbs;
    }

    @Override
    public List<SchedBlock> findSchedBlocksWithVisibleRepresentativeTarget(
            double lst) {
        Query query =
            getSession().getNamedQuery("SchedBlock.findSchedBlocksWithVisibleRepresentativeTarget");
        query.setParameter(0, lst);
        query.setParameter(1, lst);
        query.setParameter(2, lst);
        query.setParameter(3, lst);
        List<SchedBlock> schedBlocks = (List<SchedBlock>) query.list();
        return schedBlocks;
    }
    
    @Override
    public List<SchedBlock> findSchedBlocksByEstimatedExecutionTime(double time) {
        Double [] args = {new Double(time)}; 
        executeNamedQuery("SchedBlock.findByEstimatedExecutionTime", args);
        return executeNamedQuery("SchedBlock.findByEstimatedExecutionTime", args);
    }

    @Override
    public void hydrateSchedBlockObsParams(SchedBlock schedBlock) {
        getHibernateTemplate().lock(schedBlock, LockMode.NONE);
        Set<ObservingParameters> obsParams = schedBlock.getObservingParameters();
        for (Iterator<ObservingParameters> iter = obsParams.iterator();
            iter.hasNext();) {
            ObservingParameters params = iter.next();
            if (params instanceof ScienceParameters) {
                ((ScienceParameters) params).getRepresentativeBandwidth();
            }
        }
        schedBlock.getSchedulingConstraints().getRepresentativeTarget().getSource().getCoordinates();
    }
    
    @Override
    public List<SchedBlock> findSchedBlocksWithoutTooMuchTsysVariation(
            double variation) {
        Query query =
            getSession().getNamedQuery("SchedBlock.findSchedBlocksWithoutTooMuchTsysVariation");
        query.setParameter(0, variation);
        List<SchedBlock> schedBlocks = (List<SchedBlock>) query.list();
        return schedBlocks;
    }
    
}
