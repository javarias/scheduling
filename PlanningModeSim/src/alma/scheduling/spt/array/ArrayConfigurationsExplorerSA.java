package alma.scheduling.spt.array;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import alma.scheduling.input.observatory.generated.ArrayLSTRequestedInterval;
import alma.scheduling.input.observatory.generated.IntervalRequested;
import alma.scheduling.input.observatory.generated.ObsCycleProfiles;
import alma.scheduling.utils.TimeUtil;

public class ArrayConfigurationsExplorerSA {

	private ObsCycleProfiles arrayProfiles;
	private NavigableMap<Double, NavigableSet<Date>> LSTDateMap;
	private NavigableMap<Date, Double> dateLSTMap;
	
	private final static double ALMA_LONGITUDE = -67.75492777777778;
	private final static double LST_TOLERANCE = 1.0;
	private final static long WEEK_DURATION_MS = 7 * 24 * 60 *60 * 1000;
	private final static SimpleDateFormat utcFormat;
	static{
		utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public ArrayConfigurationsExplorerSA() {
		SchedBlockCategorizer sbc = new SchedBlockCategorizer();
		arrayProfiles = sbc.calculateObsCycleProfiles();
		sbc.cleanUp();
		
		LSTDateMap = new TreeMap<>();
		dateLSTMap = new TreeMap<>();
		Date currDate = arrayProfiles.getObsCycleProfile(0).getDateInterval().getStartDate();
		currDate = new Date(currDate.getTime() + 23*60*60*1000);
		Date end = arrayProfiles.getObsCycleProfile(0).getDateInterval().getEndDate();
		while (currDate.before(end)) {
			double lst = TimeUtil.getLocalSiderealTime(currDate, ALMA_LONGITUDE);
			NavigableSet<Date> dateSet = null;
			if (!LSTDateMap.containsKey(lst)) {
				dateSet = new TreeSet<>();
				LSTDateMap.put(lst, dateSet);
			}
			dateSet = LSTDateMap.get(lst);
			dateSet.add(currDate);
			dateLSTMap.put(currDate, lst);
			currDate = new Date(currDate.getTime() + WEEK_DURATION_MS);
		}
	}
	
	private void calculateDateIntervals() {
		for (ArrayLSTRequestedInterval ari: arrayProfiles.getObsCycleProfile(0).getArrayLSTRequestedInterval()) {
			for (IntervalRequested interval: ari.getIntervalRequested()) {
				double startLST = selectNearestLST(interval.getStartLST());
				double endLST = selectNearestLST(interval.getEndLST());
				selectWeeklyIntervals(startLST, endLST);
			}
		}
	}
	
	private double selectNearestLST(double lst) {
		Double floorStart = LSTDateMap.floorKey(lst);
		if (floorStart == null)
			floorStart = LSTDateMap.lastKey();
		Double ceilingStart = LSTDateMap.ceilingKey(lst);
		if (ceilingStart == null)
			ceilingStart = LSTDateMap.firstKey();
		if (Math.abs(lst - floorStart) > Math.abs(lst - ceilingStart))
			return floorStart;
		else 
			return ceilingStart;
	}
	
	private NavigableMap<Date, Date> selectWeeklyIntervals(double startLST, double endLST) {
		NavigableMap<Date, Date> retVal = new TreeMap<>();
		NavigableSet<Date> startDates = new TreeSet<>();
		NavigableSet<Date> endDates = new TreeSet<>();
		//First look for start dates
		double lstFrom = (startLST - LST_TOLERANCE) < 0 ? startLST + 24 - LST_TOLERANCE: startLST - LST_TOLERANCE;
		double lstTo = (startLST + LST_TOLERANCE) >= 24 ? startLST - 24 + LST_TOLERANCE: startLST + LST_TOLERANCE;
		for(Entry<Double, NavigableSet<Date>> e: LSTDateMap.subMap(lstFrom, true, lstTo, true).entrySet()) {
			startDates.addAll(e.getValue());
		}
		//Then look for end dates
		lstFrom = (endLST - LST_TOLERANCE) < 0 ? endLST + 24 - LST_TOLERANCE: endLST - LST_TOLERANCE;
		lstTo = (endLST + LST_TOLERANCE) >= 24 ? endLST - 24 + LST_TOLERANCE: endLST + LST_TOLERANCE;
		for(Entry<Double, NavigableSet<Date>> e: LSTDateMap.subMap(lstFrom, true, lstTo, true).entrySet()) {
			endDates.addAll(e.getValue());
		}
		System.out.println(startLST + "->" + endLST);
		for (Date d: startDates) {
			System.out.println(utcFormat.format(d));
		}
		System.out.println(startDates + " -- " + endDates);
		
		//Remove non-weekly starts
		final Date cycleStartDate = arrayProfiles.getObsCycleProfile(0).getDateInterval().getStartDate();
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime(cycleStartDate);
		
		
		return retVal;
	}
	
	
	public static void main(String[] args) {
		ArrayConfigurationsExplorerSA sa = new ArrayConfigurationsExplorerSA();
		sa.calculateDateIntervals();
	}
}
