package alma.scheduling.spt.array;

import java.util.SortedMap;

import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.SimulationResults;

public class SimulationResultsAnalyzer {

	public SimulationResultSummary analyzeResult (SimulationResults result) {
		SimulationResultSummary sum = new SimulationResultSummary();
		for(ObservationProject op: result.getObservationProject()) {
			SortedMap<String, Integer> counter = null;
			switch(op.getStatus()) {
			case COMPLETE:
				counter = sum.getCompletedProjects();
				break;
			case INCOMPLETE:
				counter = sum.getIncompleteProjects();
				break;
			case NOT_STARTED:
				counter = sum.getNonStartedProjects();
				break;
			}
			if (!counter.containsKey(op.getGrade()))
				counter.put(op.getGrade(), 0);
			counter.put(op.getGrade(), counter.get(op.getGrade()) + 1);
		}
		return sum;
	}
}
