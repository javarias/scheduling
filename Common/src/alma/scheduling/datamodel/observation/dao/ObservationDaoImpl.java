package alma.scheduling.datamodel.observation.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import alma.scheduling.datamodel.observation.CreatedArray;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observation.Session;

public class ObservationDaoImpl implements ObservationDao {

	private HashMap<String, List<ExecBlock>> ebs;
	
	
	public ObservationDaoImpl() {
		ebs = new HashMap<>();
	}
	
	@Override
	public void save(ExecBlock eb) {
		List<ExecBlock> list = null;
		if (ebs.containsKey(eb.getSchedBlockUid())) {
			list = ebs.get(eb.getSchedBlockUid());
		} else {
			list = new ArrayList<>();
			ebs.put(eb.getSchedBlockUid(), list);
		}
		list.add(eb);
	}

	@Override
	public void save(CreatedArray array) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(CreatedArray array) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(Session session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Session session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ExecBlock> getAllExecBlocksForSB(String SbUid) {
		if (!ebs.containsKey(SbUid))
			return new ArrayList<>();
		return ebs.get(SbUid);
	}

	@Override
	public int getNumberOfExecutionsForSb(String SbUid) {
		if (!ebs.containsKey(SbUid))
			return 0;
		return ebs.get(SbUid).size();
	}

	@Override
	public double getAccumulatedObservingTimeForSb(String SbUid) {
		if (!ebs.containsKey(SbUid))
			return 0;
		double ret = 0;
		for (ExecBlock eb: ebs.get(SbUid))
			ret += eb.getTimeOnSource();
		return ret;
	}

}
