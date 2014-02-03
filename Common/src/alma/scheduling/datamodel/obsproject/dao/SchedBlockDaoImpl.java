package alma.scheduling.datamodel.obsproject.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.Target;

public class SchedBlockDaoImpl implements SchedBlockDao {

	private HashMap<String, SchedBlock> schedulingBlocks;
	private FieldSourceDao fsDao;
	
	public SchedBlockDaoImpl() {
		schedulingBlocks = new HashMap<>();
	}
	
	@Override
	public Collection<SchedBlock> findAll() {
		return schedulingBlocks.values();
	}

	@Override
	public int countAll() {
		return schedulingBlocks.size();
	}

	@Override
	public Collection<SchedBlock> findSchedBlocksWithVisibleRepresentativeTarget(
			double lst) {
		ArrayList<SchedBlock> ret = new ArrayList<>();
		for (SchedBlock sb: schedulingBlocks.values()) {
			Target repTarget = sb.getSchedulingConstraints().getRepresentativeTarget();
			if (repTarget == null)
				continue;
			if (repTarget.getSource().getObservability().getAlwaysVisible()) 
				ret.add(sb);
			else if (repTarget.getSource().getObservability().getRisingTime() <
					repTarget.getSource().getObservability().getSettingTime() ) {
				if (repTarget.getSource().getObservability().getRisingTime() < lst &&
						repTarget.getSource().getObservability().getSettingTime() > lst)
					ret.add(sb);
			} else if (repTarget.getSource().getObservability().getRisingTime() >
					repTarget.getSource().getObservability().getSettingTime() ) {
				if (repTarget.getSource().getObservability().getRisingTime() < lst ||
						repTarget.getSource().getObservability().getSettingTime() > lst)
					ret.add(sb);
			}
		}
		return ret;
	}

	@Override
	public Collection<SchedBlock> findSchedBlocksByEstimatedExecutionTime(double time) {
		ArrayList<SchedBlock> ret = new ArrayList<>();
		for (SchedBlock sb: schedulingBlocks.values()) {
			if (sb.getObsUnitControl().getEstimatedExecutionTime() < time)
				ret.add(sb);
		}
		return ret;
	}

	@Override
	public void hydrateSchedBlockObsParams(SchedBlock schedBlock) {
		//NO-OP

	}

	@Override
	public List<SchedBlock> findSchedBlocksWithoutTooMuchTsysVariation(
			double variation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchedBlock> findSchedBlocksWithEnoughTimeInExecutive(
			Executive exec, ObservingSeason os) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchedBlock> findSchedBlocksBetweenHourAngles(double lowLimit,
			double highLimit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchedBlock> findSchedBlocksOutOfArea(double lowRaLimit,
			double highRaLimit, double lowDecLimit, double highDecLimit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchedBlock> findSchedBlockWithStatusReady() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchedBlock> findSchedBlockBetweenFrequencies(double lowFreq,
			double highFreq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchedBlock findByEntityId(String entityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchedBlock> findSchedBlocksForProject(ObsProject project) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void hydrateObsUnitSet(ObsUnitSet ous) {
		//NO-OP

	}

	@Override
	public void deleteAll() {
		schedulingBlocks = new HashMap<>();

	}

	@Override
	public void saveOrUpdate(SchedBlock sb) {
		schedulingBlocks.put(sb.getUid(), sb);

	}

	@Override
	public void saveOrUpdate(Collection<SchedBlock> sb) {
		for(SchedBlock s: sb) {
			schedulingBlocks.put(s.getUid(), s);
			for (Target t: s.getTargets())
				if (t.getSource() != null)
					fsDao.saveOrUpdate(t.getSource());
		}

	}

	@Override
	public SchedBlock findById(String uid) {
		return schedulingBlocks.get(uid);
	}

	public void setFsDao(FieldSourceDao fsDao) {
		this.fsDao = fsDao;
	}
	
}
