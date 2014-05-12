package alma.scheduling.spt.array;

import java.util.SortedMap;
import java.util.TreeMap;

public class SimulationResultSummary implements Comparable<SimulationResultSummary>{

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

	@Override
	public int compareTo(SimulationResultSummary o) {
		if (completedProjects.get("A") > o.getCompletedProjects().get("A"))
			return (completedProjects.get("A") - o.getCompletedProjects().get("A")) * 1000000;
		if (completedProjects.get("A") < o.getCompletedProjects().get("A"))
			return (completedProjects.get("A") - o.getCompletedProjects().get("A")) * 1000000;
		if (completedProjects.get("B") > o.getCompletedProjects().get("B"))
			return (completedProjects.get("B") - o.getCompletedProjects().get("B")) * 100000;
		if (completedProjects.get("B") < o.getCompletedProjects().get("B"))
			return (completedProjects.get("B") - o.getCompletedProjects().get("B")) * 100000;
		if (completedProjects.get("C") > o.getCompletedProjects().get("C"))
			return (completedProjects.get("C") - o.getCompletedProjects().get("C")) * 10000;
		if (completedProjects.get("C") < o.getCompletedProjects().get("C"))
			return (completedProjects.get("C") - o.getCompletedProjects().get("C")) * 10000;
		if (incompleteProjects.get("A") > o.getIncompleteProjects().get("A"))
			return (incompleteProjects.get("A") - o.getIncompleteProjects().get("A")) * 1000;
		if (incompleteProjects.get("A") < o.getIncompleteProjects().get("A"))
			return (incompleteProjects.get("A") - o.getIncompleteProjects().get("A")) * 1000;
		if (incompleteProjects.get("B") > o.getIncompleteProjects().get("B"))
			return (incompleteProjects.get("B") - o.getIncompleteProjects().get("B")) * 100;
		if (incompleteProjects.get("B") < o.getIncompleteProjects().get("B"))
			return (incompleteProjects.get("B") - o.getIncompleteProjects().get("B")) * 100;
		if (incompleteProjects.get("C") > o.getIncompleteProjects().get("C"))
			return incompleteProjects.get("C") - o.getIncompleteProjects().get("C");
		if (incompleteProjects.get("C") < o.getIncompleteProjects().get("C"))
			return incompleteProjects.get("C") - o.getIncompleteProjects().get("C");
		return 0;
		
	}
}
