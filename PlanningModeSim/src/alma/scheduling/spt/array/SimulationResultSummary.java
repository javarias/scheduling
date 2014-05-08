package alma.scheduling.spt.array;

import java.util.SortedMap;
import java.util.TreeMap;

public class SimulationResultSummary {

	private final TreeMap<String, Integer> completedProjects;
	private final TreeMap<String, Integer> nonStartedProjects;
	private final TreeMap<String, Integer> incompleteProjects;

	public SimulationResultSummary() {
		completedProjects = new TreeMap<>();
		nonStartedProjects = new TreeMap<>();
		incompleteProjects = new TreeMap<>();
	}
	
	public SortedMap<String, Integer> getCompletedProjects() {
		return completedProjects;
	}

	public SortedMap<String, Integer> getNonStartedProjects() {
		return nonStartedProjects;
	}

	public SortedMap<String, Integer> getIncompleteProjects() {
		return incompleteProjects;
	}

	@Override
	public String toString() {
		return "SimulationResultSummary [completedProjects="
				+ completedProjects + ", nonStartedProjects="
				+ nonStartedProjects + ", incompleteProjects="
				+ incompleteProjects + "]";
	}
}
