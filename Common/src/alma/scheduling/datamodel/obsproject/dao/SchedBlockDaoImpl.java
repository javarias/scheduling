package alma.scheduling.datamodel.obsproject.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
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
    
    @SuppressWarnings("unchecked")
    @Override
    public List<SchedBlock> findSchedBlocksWithoutTooMuchTsysVariation(
            double variation) {
        Query query =
            getSession().getNamedQuery("SchedBlock.findSchedBlocksWithoutTooMuchTsysVariation");
        query.setParameter(0, variation);
        List<SchedBlock> schedBlocks = (List<SchedBlock>) query.list();
        return schedBlocks;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SchedBlock> findSchedBlocksWithEnoughTimeInExecutive(
            Executive exec, ObservingSeason os) throws NullPointerException {
        if (exec == null)
            throw new NullPointerException("Executive parameter cannot be null");
        if (os == null)
            throw new NullPointerException(
                    "Observing Season parameter cannot be null");
        Query query = null;
        query = getSession()
                .createQuery(
                        "select sum(ets.timeSpent) from ExecutiveTimeSpent ets "
                                + "where ets.executive = ? and ets.observingSeason = ?");
        query.setParameter(0, exec);
        query.setParameter(1, os);
        Double timeSpent = (Double) query.uniqueResult();
        if(timeSpent == null)
            timeSpent = new Double(0.0);
        query = getSession()
                .createQuery(
                        "select sb from SchedBlock sb, PI pi join pi.PIMembership pim, "
                                + "Executive e join e.executivePercentage ep "
                                + "where sb.piName = pi.email and "
                                + "pim.executive = ? and "
                                + "ep.executive = ? and "
                                + "ep.season = ? and "
                                + "ep.totalObsTimeForSeason - sb.obsUnitControl.estimatedExecutionTime >= "
                                + timeSpent.toString());
        query.setParameter(0, exec);
        query.setParameter(1, exec);
        query.setParameter(2, os);
        List<SchedBlock> schedBlocks = (List<SchedBlock>) query.list();
        return schedBlocks;
    }

	@Override
	public List<SchedBlock> findSchedBlocksBetweenHourAngles(double lowLimit,
			double highLimit) {
		Object[] params = {lowLimit, highLimit};
		return executeNamedQuery("SchedBlock.findByTargetHourAngleLimits", params);
	}
	
    @SuppressWarnings("unchecked")
    public List<SchedBlock> findSchedBlocksOutOfArea(double lowRaLimit,
            double highRaLimit, double lowDecLimit, double highDecLimit) {
        Query query = null;
        query = getSession()
                .createQuery(
                        "from SchedBlock sb where "
                                + " not (sb.schedulingConstraints.representativeTarget.source.coordinates.RA >= ? and "
                                + " sb.schedulingConstraints.representativeTarget.source.coordinates.RA <= ?) and "
                                + " not (sb.schedulingConstraints.representativeTarget.source.coordinates.Dec >= ? and "
                                + " sb.schedulingConstraints.representativeTarget.source.coordinates.Dec <= ?)");
        query.setParameter(0, lowRaLimit);
        query.setParameter(1, highRaLimit);
        query.setParameter(2, lowDecLimit);
        query.setParameter(3, highDecLimit);
        List<SchedBlock> schedBlocks = (List<SchedBlock>) query.list();
        return schedBlocks;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SchedBlock> findSchedBlockWithStatusReady() {
        Query query = null;
        query = getSession().createQuery(
                "from SchedBlock sb where sb.schedBlockControl.state = ?");
        query.setParameter(0, SchedBlockState.READY);
        return query.list();
    }
}
