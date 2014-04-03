package alma.scheduling.datamodel.output.dao;

import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.output.SimulationResults;

public class OutputDaoImpl implements OutputDao {

	@Override
	public void saveResults(SimulationResults results) {
	}

	@Override
	public void saveResults(Collection<SimulationResults> results) {
	}

	@Override
	public List<SimulationResults> getResults() {
		return null;
	}

	@Override
	public void deleteAll() {

	}

	@Override
	public SimulationResults getResult(long id) {
		return null;
	}

	@Override
	public SimulationResults getLastResult() {
		return null;
	}

	@Override
	public long getLastResultId() {
		return 0;
	}

}
