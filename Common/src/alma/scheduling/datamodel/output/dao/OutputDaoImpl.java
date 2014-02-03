package alma.scheduling.datamodel.output.dao;

import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.output.Results;

public class OutputDaoImpl implements OutputDao {

	@Override
	public void saveResults(Results results) {
	}

	@Override
	public void saveResults(Collection<Results> results) {
	}

	@Override
	public List<Results> getResults() {
		return null;
	}

	@Override
	public void deleteAll() {

	}

	@Override
	public Results getResult(long id) {
		return null;
	}

	@Override
	public Results getLastResult() {
		return null;
	}

	@Override
	public long getLastResultId() {
		return 0;
	}

}
