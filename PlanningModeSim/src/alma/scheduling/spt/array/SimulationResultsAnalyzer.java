package alma.scheduling.spt.array;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import alma.scheduling.datamodel.output.Array;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.SchedBlockResult;
import alma.scheduling.datamodel.output.SimulationResults;
import alma.scheduling.datamodel.output.dao.XmlOutputDaoImpl;
import alma.scheduling.spt.util.DateInterval;

public class SimulationResultsAnalyzer {

	private static final SimpleDateFormat utcFormat;
	static {
		utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
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
	
	public void analyzeFairness(SimulationResults result, PrintStream ps) {
		for(Array a: result.getArray()) {
			TreeMap<DateInterval, TreeMap<String, Double>> weeklyAccount = new TreeMap<DateInterval, TreeMap<String,Double>>();
			Date startDate = a.getCreationDate();
			Date endDate = a.getDeletionDate();
			Date currDate = startDate;
			while(currDate.getTime() < endDate.getTime()) {
				weeklyAccount.put(new DateInterval(currDate, new Date(currDate.getTime() + 7 * 24 *  60 * 60 * 1000)),
						new TreeMap<String,Double>());
				currDate = new Date(currDate.getTime() + 7 * 24 * 60 * 60 * 1000);
			}
			for(ObservationProject op: result.getObservationProject()) {
				String execName = op.getAffiliation().iterator().next().getExecutive();
				for (SchedBlockResult sb: op.getSchedBlock()) {
					if (sb.getStartDate().getTime() < startDate.getTime() || sb.getStartDate().getTime() > sb.getEndDate().getTime())
						continue;
					if (sb.getArrayRef() != a)
						continue;
					TreeMap<String, Double> intervalMap = weeklyAccount.floorEntry(new DateInterval(sb.getStartDate(), sb.getEndDate())).getValue();
					if (!intervalMap.containsKey(execName))
						intervalMap.put(execName, 0.0);
					intervalMap.put(execName, intervalMap.get(execName) + sb.getExecutionTime());
				}
			}
//			System.out.println(weeklyAccount);
			String[] execs = {"CL","EA", "EA_NA", "EU", "NA", "OTHER"}; 
			ps.println("Analysis per week " + a.getConfigurationName() + ":");
			ps.println("start,end,CL,EA,EA_NA,EU,NA,OTHER");
			for (DateInterval i: weeklyAccount.keySet()) {
				ps.print(utcFormat.format(i.getFromDate()) + "," + utcFormat.format(i.getToDate()));
//				double totalTime = 0.0;
//				for (Double h: weeklyAccount.get(i).values())
//					totalTime += h;
				for(String exec: execs) {
					if (!weeklyAccount.get(i).containsKey(exec))
						ps.print("," + 0.0);
					else
						ps.print("," + weeklyAccount.get(i).get(exec));
				}
				ps.println();
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length == 0) {
			System.out.println("Nothing to do");
			System.exit(-1);
		}
		String[] fileNames = new String[args.length];
		XmlOutputDaoImpl dao = new XmlOutputDaoImpl();
		ArrayList<InputStream> inStreams =  new ArrayList<>();
		int i = 0;
		for(String pathStr: args) {
			String[] tmpName = pathStr.split("/");
			fileNames[i++] = tmpName[tmpName.length - 1];
			try {
				inStreams.add(new FileInputStream(pathStr));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		SimulationResultsAnalyzer analyzer = new SimulationResultsAnalyzer();
		List<SimulationResults> results = dao.loadResults(inStreams);
		i = 0;
		for (SimulationResults r: results) {
			SimulationResultSummary sum = analyzer.analyzeResult(r);
			System.out.println(sum);
			
			PrintStream ps = new PrintStream(fileNames[i++] + ".csv");
			analyzer.analyzeFairness(r, ps);
			ps.close();
		}
	}
}
