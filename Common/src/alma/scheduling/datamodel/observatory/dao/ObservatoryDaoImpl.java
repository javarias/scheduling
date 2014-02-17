package alma.scheduling.datamodel.observatory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;

public class ObservatoryDaoImpl implements ObservatoryDao {

	private List<ArrayConfiguration> arrayConfigs;
	
	public ObservatoryDaoImpl() {
		arrayConfigs = new ArrayList<>();
	}
	
	@Override
	public List<ArrayConfiguration> findArrayConfigurations() {
		return arrayConfigs;
	}

	@Override
	public void deleteAllArrayConfigurations() {
		arrayConfigs = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void saveOrUpdate(Collection<T> objs) {
		if (objs.isEmpty())
			return;
		if (objs.iterator().next() instanceof ArrayConfiguration) {
			Collection<? extends ArrayConfiguration> arrConfs 
				= (Collection<? extends ArrayConfiguration>) objs;
			for (ArrayConfiguration c: arrConfs) {
				c.setId((long)arrayConfigs.size());
				arrayConfigs.add(c);
			}
		}
	}

	@Override
	public <T> void deletaAll(Collection<T> objs) {
		deleteAllArrayConfigurations();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> findAll(Class<T> clazz) {
		if (clazz.isInstance(new ArrayConfiguration()))
			return (Collection<T>) arrayConfigs;
		return null;
	}

	@Override
	public void deleteAll(Object findAll) {
		deleteAllArrayConfigurations();
	}

}
