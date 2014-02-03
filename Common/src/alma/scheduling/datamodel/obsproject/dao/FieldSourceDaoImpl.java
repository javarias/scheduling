package alma.scheduling.datamodel.obsproject.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import alma.scheduling.datamodel.obsproject.FieldSource;

public class FieldSourceDaoImpl implements FieldSourceDao {

	private HashMap<String, HashMap<Long, FieldSource>> fieldSources;
	
	public FieldSourceDaoImpl () {
		fieldSources = new HashMap<>();
	}
	
	@Override
	public Collection<FieldSource> getSourcesWithoutRiseAndSetTimes() {
		ArrayList<FieldSource> ret = new ArrayList<>();
		for (HashMap<Long, FieldSource> sbfs: fieldSources.values()) {
			for (FieldSource fs: sbfs.values()) {
				if (fs.getObservability() == null)
					ret.add(fs);
			}
		}
		return ret;
	}

	@Override
	public int getNumberOfSourcesWithoutUpdate() {
		ArrayList<FieldSource> ret = new ArrayList<>();
		for (HashMap<Long, FieldSource> sbfs: fieldSources.values()) {
			for (FieldSource fs: sbfs.values()) {
				if (fs.getObservability() == null)
					ret.add(fs);
			}
		}
		return ret.size();
	}

	@Override
	public int saveOrUpdate(FieldSource fs) {
		HashMap<Long, FieldSource> sbfs = null;
		if (fieldSources.containsKey(fs.getSbUid()))
			sbfs = fieldSources.get(fs.getSbUid());
		else {
			sbfs = new HashMap<>();
			fieldSources.put(fs.getSbUid(), sbfs);
		}
		
		synchronized (sbfs) {
			if (fs.getId() == null)
				fs.setId((long) sbfs.size());
			sbfs.put(fs.getId(), fs);
		}
		return 0;
	}

}
