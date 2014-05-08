package alma.scheduling.spt.array;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.output.SimulationResults;
import alma.scheduling.input.observatory.generated.ArrayConfigurationLite;
import alma.scheduling.input.observatory.generated.ArrayLSTRequestedInterval;
import alma.scheduling.input.observatory.generated.IntervalRequested;
import alma.scheduling.input.observatory.generated.ObsCycleProfiles;
import alma.scheduling.psm.cli.SimulatorCLI;
import alma.scheduling.spt.util.ArrayLSTRequestedIntervalWrapper;
import alma.scheduling.spt.util.SimulatorContextFactory;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.TimeUtil;

public class ArrayConfigurationsExplorerSA {

	private ObsCycleProfiles arrayProfiles;
	private NavigableMap<Double, NavigableSet<Date>> startLSTDateMap;
	private NavigableMap<Double, NavigableSet<Date>> endLSTDateMap;
	private Set<ArrayLSTRequestedIntervalWrapper> arrayConfigurations;
//	private NavigableMap<Date, Double> dateLSTMap;
	
	private final static double ALMA_LONGITUDE = -67.75492777777778;
	private final static double LST_TOLERANCE = 3.0;
	private final static long WEEK_DURATION_MS = 7 * 24 * 60 *60 * 1000;
	private final static SimpleDateFormat utcFormat;
	static{
		utcFormat = new SimpleDateFormat("yyyy-MM-dd");
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public ArrayConfigurationsExplorerSA() {
		SchedBlockCategorizer sbc = new SchedBlockCategorizer();
		arrayProfiles = sbc.calculateObsCycleProfiles();
		
		startLSTDateMap = new TreeMap<>();
		endLSTDateMap = new TreeMap<>();
		arrayConfigurations = new HashSet<>();
//		dateLSTMap = new TreeMap<>();
		Date currDate = arrayProfiles.getObsCycleProfile(0).getDateInterval().getStartDate();
		currDate = new Date(currDate.getTime() + 23*60*60*1000);
		Date end = arrayProfiles.getObsCycleProfile(0).getDateInterval().getEndDate();
		while (currDate.before(end)) {
			double lst = TimeUtil.getLocalSiderealTime(currDate, ALMA_LONGITUDE);
			NavigableSet<Date> dateSet = null;
			if (!startLSTDateMap.containsKey(lst)) {
				dateSet = new TreeSet<>();
				startLSTDateMap.put(lst, dateSet);
			}
			dateSet = startLSTDateMap.get(lst);
			dateSet.add(currDate);
//			dateLSTMap.put(currDate, lst);
			
			Date endObsWeekTime = new Date(currDate.getTime() + 8*60*60*1000);
			lst = TimeUtil.getLocalSiderealTime(endObsWeekTime, ALMA_LONGITUDE);
			dateSet = null;
			if (!endLSTDateMap.containsKey(lst)) {
				dateSet = new TreeSet<>();
				endLSTDateMap.put(lst, dateSet);
			}
			dateSet = endLSTDateMap.get(lst);
			dateSet.add(endObsWeekTime);
			
			currDate = new Date(currDate.getTime() + WEEK_DURATION_MS);
		}
		
		sbc.cleanUp();
	}
	
	private Set<ArrayLSTRequestedIntervalWrapper> calculateDateIntervals() {
		HashSet<ArrayLSTRequestedIntervalWrapper> retval = new HashSet<>();
		for (ArrayLSTRequestedInterval ari: arrayProfiles.getObsCycleProfile(0).getArrayLSTRequestedInterval()) {
			ArrayLSTRequestedIntervalWrapper a = new ArrayLSTRequestedIntervalWrapper(ari);
			for (IntervalRequested interval: ari.getIntervalRequested()) {
				double startLST = selectNearestLST(interval.getStartLST());
				double endLST = selectNearestLST(interval.getEndLST());
				System.out.println(ari.getConfigurationName());
				NavigableSet<Date>[] dates = selectWeeklyIntervals(startLST, endLST);
				a.getProposedStartDates().put(startLST, dates[0]);
				a.getProposedEndDates().put(endLST, dates[1]);
			}
			retval.add(a);
		}
		return retval;
	}
	
	private double selectNearestLST(double lst) {
		Double floorStart = startLSTDateMap.floorKey(lst);
		if (floorStart == null)
			floorStart = startLSTDateMap.lastKey();
		Double ceilingStart = startLSTDateMap.ceilingKey(lst);
		if (ceilingStart == null)
			ceilingStart = startLSTDateMap.firstKey();
		if (Math.abs(lst - floorStart) > Math.abs(lst - ceilingStart))
			return floorStart;
		else 
			return ceilingStart;
	}
	
	/**
	 * 
	 * @param startLST
	 * @param endLST
	 * @return the potential start dates and end dates for the given lst interval 
	 */
	private NavigableSet<Date>[] selectWeeklyIntervals(double startLST, double endLST) {
		NavigableSet<Date>[] retVal = new TreeSet[2];
		NavigableSet<Date> startDates = new TreeSet<>();
		NavigableSet<Date> endDates = new TreeSet<>();
		//First look for start dates
		double lstFrom = (startLST - LST_TOLERANCE) < 0 ? startLST + 24 - LST_TOLERANCE: startLST - LST_TOLERANCE;
		double lstTo = (startLST + LST_TOLERANCE) >= 24 ? startLST - 24 + LST_TOLERANCE: startLST + LST_TOLERANCE;
		if (lstFrom > lstTo) {
			for(Entry<Double, NavigableSet<Date>> e: startLSTDateMap.subMap(lstFrom, true, 24.0, true).entrySet()) {
				startDates.addAll(e.getValue());
			}
			for(Entry<Double, NavigableSet<Date>> e: startLSTDateMap.subMap(0.0, true, lstTo, true).entrySet()) {
				startDates.addAll(e.getValue());
			}
		} else {
			for(Entry<Double, NavigableSet<Date>> e: startLSTDateMap.subMap(lstFrom, true, lstTo, true).entrySet()) {
				startDates.addAll(e.getValue());
			}
		}
		//Then look for end dates
		lstFrom = (endLST - LST_TOLERANCE) < 0 ? endLST + 24 - LST_TOLERANCE: endLST - LST_TOLERANCE;
		lstTo = (endLST + LST_TOLERANCE) >= 24 ? endLST - 24 + LST_TOLERANCE: endLST + LST_TOLERANCE;
		if (lstFrom > lstTo) {
			for(Entry<Double, NavigableSet<Date>> e: endLSTDateMap.subMap(lstFrom, true, 24.0, true).entrySet()) {
				endDates.addAll(e.getValue());
			}
			for(Entry<Double, NavigableSet<Date>> e: endLSTDateMap.subMap(0.0, true, lstTo, true).entrySet()) {
				endDates.addAll(e.getValue());
			}
		} else {
			for(Entry<Double, NavigableSet<Date>> e: endLSTDateMap.subMap(lstFrom, true, lstTo, true).entrySet()) {
				endDates.addAll(e.getValue());
			}
		}
		System.out.println(startLST + "->" + endLST);
		System.out.println("Potential start dates:");
		for (Date d: startDates) {
			System.out.println(utcFormat.format(d));
		}
		System.out.println("Potential end dates:");
		for (Date d: endDates) {
			System.out.println(utcFormat.format(d));
		}
		System.out.println("------------------------------------------------------------------------------------");
		
		retVal[0] = startDates;
		retVal[1] = endDates;
		
		return retVal;
	}
	
	private List<ArrayConfigurationLite> neighbour() {
		final ArrayList<ArrayConfigurationLite> retval = new ArrayList<>();
		
		return retval;
	}
	
	private List<ArrayConfiguration> findInitialSolution(Set<ArrayLSTRequestedIntervalWrapper> arraySet) {
		ArrayConfigurationsExplorerGreedy greedy = new ArrayConfigurationsExplorerGreedy();
		return greedy.findSolution(arraySet);
	}
	
	private void printSolution(List<ArrayConfiguration> sol) {
		System.out.println("Solution for run");
		for(ArrayConfiguration ac: sol) {
			System.out.println(ac + " " + utcFormat.format(ac.getStartTime()) + " -- " + utcFormat.format(ac.getEndTime()));
		}
		System.out.println("----------------------------------------------------------------------------------");
	}
	
	public static void main(String[] args) throws Exception {
		ArrayConfigurationsExplorerSA sa = new ArrayConfigurationsExplorerSA();
		Set<ArrayLSTRequestedIntervalWrapper> arraySet = sa.calculateDateIntervals();
		for (int i = 0; i < 2; i++) {
			List<ArrayConfiguration> s = sa.findInitialSolution(arraySet);
			sa.printSolution(s);
			SimulatorCLI cli = new SimulatorCLI();
			cli.parseOptions(args);
			cli.loadData();
			ObservatoryDao obsDao = (ObservatoryDao) DSAContextFactory.getContext().getBean(DSAContextFactory.SCHEDULING_OBSERVATORY_DAO);
			obsDao.deleteAllArrayConfigurations();
			obsDao.saveOrUpdate(s);
			SimulationResults result = cli.runSimulationDataAlreadyLoaded();
			SimulationResultSummary summary = new SimulationResultsAnalyzer().analyzeResult(result);
			System.out.println(summary);
//			if (s.size() > 6)
//				sa.printSolution(s);
			SimulatorContextFactory.closeContext();
		}
	}
}
