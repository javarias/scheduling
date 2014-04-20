package alma.scheduling.datamodel.output.dao;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.output.SimulationResults;

public interface StreamBasedOutputDao extends OutputDao {

	/**
	 * Serialize a collection of {@link SimulationResults} 
	 * 
	 * @param results the list of {@link SimulationResults} to be serialized
	 * @return the collection of InputStreams containing the SimulationResults serialized;
	 */
	public List<InputStream> getResultsAsStream(Collection<SimulationResults> results);
	
	/**
	 * Loads into the system the {@link SimulationResults} 
	 * 
	 * @param streams the List of Simulation Results as InputStream
	 * @return the List of the SimualtionResults de-serialized from the stream
	 */
	public List<SimulationResults> loadResults(List<InputStream> streams);
}
