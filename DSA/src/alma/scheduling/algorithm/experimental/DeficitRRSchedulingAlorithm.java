package alma.scheduling.algorithm.experimental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithmImpl;
import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class DeficitRRSchedulingAlorithm extends DynamicSchedulingAlgorithmImpl implements DynamicSchedulingAlgorithm{

	private static Logger logger = LoggerFactory.getLogger(DeficitRRSchedulingAlorithm.class);
	
	private static final double QUANTA_BASE_VALUE = 4D/3D; //hours (1 hr 20 in)
	
	private TreeMap<Executive, List<SchedBlock>> serviceQueues;
	private TreeMap<Executive, Double> deficitCounters;
	private TreeMap<Executive, Double> quantumSize;
	private List<SchedBlock> too; //targets of opportunity 
	private ExecutiveDAO execDao;
	
	private Executive rrPtr = null;
	private Executive prevRrPtr = null;
	
	@Override
	public void rankSchedBlocks() {
		serviceQueues = new TreeMap<>();
		deficitCounters = new TreeMap<>();
		quantumSize = new TreeMap<>();
		too = new ArrayList<SchedBlock>();
	}

	@Override
	public List<SBRank> rankSchedBlocks(Date ut) {
		return super.rankSchedBlocks(ut);
	}

	@Override
	public void selectCandidateSB() throws NoSbSelectedException {
		super.selectCandidateSB();
	}

	@Override
	public void updateCandidateSB(Date ut) throws NoSbSelectedException {
		super.updateCandidateSB(ut);
	}

	@Override
	public void selectCandidateSB(Date ut) throws NoSbSelectedException {
		if (too.size() == 0)
			super.selectCandidateSB(ut);
		else
			logger.warn("Found TOO. Do something here!!");
	}

	@Override
	public void updateModel(Date ut) {
		super.updateModel(ut);
	}

	@Override
	public SchedBlock getSelectedSchedBlock() {
		updateQueues();
		SchedBlock sb = null;
		while (sb == null) {
			if(serviceQueues.get(rrPtr).size() == 0) {
				if (serviceQueues.higherKey(rrPtr) != null)
					rrPtr = serviceQueues.higherKey(rrPtr);
				else
					rrPtr = serviceQueues.firstKey();
			}
			double currDeficit = deficitCounters.get(rrPtr);
			if (!rrPtr.equals(prevRrPtr))
				currDeficit += quantumSize.get(rrPtr);
			if (currDeficit < serviceQueues.get(rrPtr).get(0).getSchedBlockControl().getSbMaximumTime()) {
				sb = serviceQueues.get(rrPtr).get(0);
				currDeficit -= sb.getSchedBlockControl().getSbMaximumTime();
				deficitCounters.put(rrPtr, currDeficit);
				prevRrPtr = rrPtr;
			}
			else {
				deficitCounters.put(rrPtr, currDeficit);
				if (serviceQueues.higherKey(rrPtr) != null)
					rrPtr = serviceQueues.higherKey(rrPtr);
				else
					rrPtr = serviceQueues.firstKey();
			}
		}
		return sb;
	}

	@Override
	public void setArray(ArrayConfiguration arrConf) {
		super.setArray(arrConf);
	}

	@Override
	public ArrayConfiguration getArray() {
		return super.getArray();
	}

	@Override
	public void setVerboseLevel(VerboseLevel verboseLvl) {
		super.setVerboseLevel(verboseLvl);
	}

	@Override
	public void initialize(Date ut) {
		Collection<Executive> execs = execDao.getAllExecutive();
		for (Executive e: execs) {
			serviceQueues.put(e, (List<SchedBlock>)new ArrayList<SchedBlock>());
			deficitCounters.put(e, 0D);
			quantumSize.put(e, e.getDefaultPercentage()/100D);
		}
		double maxQuantaFactor = 1D / Collections.max(quantumSize.values());
		for(Executive e: quantumSize.keySet()) {
			quantumSize.put(e, quantumSize.get(e) * maxQuantaFactor * QUANTA_BASE_VALUE);
			logger.debug("Executive: " + e.getName() + " Quanta value: " + quantumSize.get(e));
		}
		
		rrPtr = serviceQueues.firstKey();
		super.initialize(ut);
	}

	public void setExecDao(ExecutiveDAO execDao) {
		this.execDao = execDao;
	}
	
	private void updateQueues() {
		for (Executive e: serviceQueues.keySet()) {
			serviceQueues.get(e).clear();
		}
		Collections.sort(ranks);
		Collections.reverse(ranks);
		for (SBRank r: ranks) {
			SchedBlock sb = sbs.get(r.getUid());
			if (sb.getExecutive().getName().equalsIgnoreCase("EA_NA") 
					|| sb.getExecutive().getName().equalsIgnoreCase("EA/NA")) {
				for (Executive e: serviceQueues.keySet()) {
					if (e.getName().equalsIgnoreCase("NA") || e.getName().equalsIgnoreCase("EA"))
						serviceQueues.get(e).add(sb);
				}
			} else 
				serviceQueues.get(sb.getExecutive()).add(sb);
		}
	}
	
}
