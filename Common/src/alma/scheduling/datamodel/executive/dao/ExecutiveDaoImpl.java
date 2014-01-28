package alma.scheduling.datamodel.executive.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;

public class ExecutiveDaoImpl implements ExecutiveDAO {

	private HashMap<String, Executive> executives;
	private TreeMap<Date, ObservingSeason> obsSeasons;
	private HashMap<String, PI> pis;
	private HashMap<ObservingSeason, List<ExecutiveTimeSpent>> timeSpent;
	private HashMap<ObservingSeason, HashMap<Executive, ExecutivePercentage>> execPercentage;
	//Extra idxs
	private HashMap<ObservingSeason, HashMap<Executive, List<ExecutiveTimeSpent>>> timeSpentExecIdx;
	
	public ExecutiveDaoImpl () {
		initializeDataStructures();
	}
	
	@Override
	public Collection<Executive> getAllExecutive() {
		return executives.values();
	}

	@Override
	public Collection<ObservingSeason> getAllObservingSeason() {
		return obsSeasons.values();
	}

	@Override
	public Collection<PI> getAllPi() {
		return pis.values();
	}

	@Override
	public ObservingSeason getCurrentSeason() {
		return obsSeasons.lastEntry().getValue();
	}

	@Override
	public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
			ObservingSeason os) {
		return timeSpentExecIdx.get(os).get(ex);
	}

	@Override
	public Executive getExecutive(String piEmail) {
		return pis.get(piEmail).getPIMembership().iterator().next().getExecutive();
	}

	@Override
	public PI getPIFromEmail(String piEmail) {
		return pis.get(piEmail);
	}

	@Override
	public void saveObservingSeasonsAndExecutives(
			List<ObservingSeason> seasons, List<Executive> executives) {
		for (Executive e: executives)
			this.executives.put(e.getName(), e);
		for (ObservingSeason os: seasons) {
			this.obsSeasons.put(os.getStartDate(), os);
			HashMap<Executive, ExecutivePercentage> execPercExecIdx 
				= new HashMap<Executive, ExecutivePercentage>();
			for (ExecutivePercentage ep: os.getExecutivePercentage())
				execPercExecIdx.put(ep.getExecutive(), ep);
			execPercentage.put(os, execPercExecIdx);
		}
		
	}

	@Override
	public ExecutivePercentage getExecutivePercentage(Executive exec,
			ObservingSeason os) {
		return execPercentage.get(os).get(exec);
	}

	@Override
	public void deleteAll() {
		initializeDataStructures();
	}

	@Override
	public void saveOrUpdate(ExecutiveTimeSpent execTS) {
		List<ExecutiveTimeSpent> list = null;
		if (!timeSpent.containsKey(execTS.getObservingSeason())) {
			list = new ArrayList<>();
			timeSpent.put(execTS.getObservingSeason(), list);
		} else 
			list = timeSpent.get(execTS.getObservingSeason());
		list.add(execTS);
		
		HashMap<Executive, List<ExecutiveTimeSpent>> timeSpentExecMap = null;
		if (!timeSpentExecIdx.containsKey(execTS.getObservingSeason())) {
			timeSpentExecMap = new HashMap<Executive, List<ExecutiveTimeSpent>>();
			timeSpentExecIdx.put(execTS.getObservingSeason(), timeSpentExecMap);
		} else 
			timeSpentExecMap = timeSpentExecIdx.get(execTS.getObservingSeason());
		list = null;
		if (!timeSpentExecMap.containsKey(execTS.getExecutive())) {
			list = new ArrayList<>();
			timeSpentExecMap.put(execTS.getExecutive(), list);
		} else 
			list = timeSpentExecMap.get(execTS.getExecutive());
		list.add(execTS);
	}

	@Override
	public void saveOrUpdate(Collection<PI> pis) {
		for (PI pi: pis) 
			this.pis.put(pi.getEmail(), pi);
	}

	@Override
	public void cleanExecutiveTimeSpent() {
		timeSpent = new HashMap<>();
		timeSpentExecIdx = new HashMap<>();

	}

	private void initializeDataStructures() {
		executives = new HashMap<>();
		obsSeasons = new TreeMap<>();
		pis = new HashMap<>();
		timeSpent = new HashMap<>();
		execPercentage = new HashMap<>();
		timeSpentExecIdx = new HashMap<>();
	}
}
