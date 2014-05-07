package alma.scheduling.spt.array;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.input.observatory.generated.ArrayConfigurationLite;
import alma.scheduling.spt.util.ArrayLSTRequestedIntervalWrapper;
import alma.scheduling.spt.util.DateInterval;

public class ArrayConfigurationsExplorerGreedy {

	private TreeMap<DateInterval, ArrayConfiguration> intervals;
	private HashMap<ArrayLSTRequestedIntervalWrapper, TreeMap<Double, Boolean>> checkedArrayLST;
	private final static long WEEK_DURATION_MS = 7 * 24 * 60 *60 * 1000;
	
	/**
	 * Non thread safe
	 */
	public ArrayConfigurationsExplorerGreedy() {
		intervals = new TreeMap<>();
		checkedArrayLST = new HashMap<ArrayLSTRequestedIntervalWrapper, TreeMap<Double,Boolean>>();
	}
	
	public List<ArrayConfiguration> findSolution(Set<ArrayLSTRequestedIntervalWrapper> arraySet) {
		init(arraySet);
		ArrayList<ArrayLSTRequestedIntervalWrapper> arrayList = new ArrayList<>(arraySet);
		int nChecks = 0;
		while (nChecks < 1000) {
			ArrayLSTRequestedIntervalWrapper aliw = arrayList.get((int)(arrayList.size() * Math.random()));
			ArrayList<Double> proposedStartDatesLSTs = new ArrayList<>(aliw.getProposedStartDates().keySet());
			double lst = proposedStartDatesLSTs.get((int) (proposedStartDatesLSTs.size() * Math.random()));
			if (checkedArrayLST.get(aliw).get(lst)) {
				//already used that LST
				nChecks++;
				continue;
			}
			ArrayConfiguration ac = convert(aliw);
			ArrayList<Date> startDateSet = new ArrayList<>(aliw.getProposedStartDates().get(lst));
			Date startDate = startDateSet.get((int)(startDateSet.size() * Math.random()));
			Date endDate = new Date(startDate.getTime() + 4 * WEEK_DURATION_MS);
			DateInterval i = new DateInterval(startDate, endDate);
			ac.setStartTime(startDate);
			ac.setEndTime(endDate);
			//Check for collisions
			boolean collision = false;
			for (DateInterval ki : intervals.keySet()) {
				if (ki.getOverlap(i) != null) {
					collision = true;
					nChecks++;
					break;
				}
			}
			if (collision)
				continue;
			intervals.put(i, ac);
			checkedArrayLST.get(aliw).put(lst, true);
			nChecks = 0;
		}
		return new ArrayList<>(intervals.values());
	}
	
	public void reset() {
		intervals = new TreeMap<>();
		checkedArrayLST = new HashMap<>();
	}
	
	private void init(Set<ArrayLSTRequestedIntervalWrapper> arraySet) {
		for(ArrayLSTRequestedIntervalWrapper aliw: arraySet) {
			checkedArrayLST.put(aliw, new TreeMap<Double,Boolean>());
			for (Double lst: aliw.getProposedStartDates().keySet()) {
				checkedArrayLST.get(aliw).put(lst, false);
			}
		}
	}
	
	private List<ArrayConfigurationLite> resolveCollision() {
		return null;
	}
	
	private ArrayConfiguration convert(ArrayLSTRequestedIntervalWrapper xmlAC) {
		ArrayConfiguration ac = new ArrayConfiguration();
		ac.setStartTime(xmlAC.getStartTime());
		ac.setEndTime(xmlAC.getEndTime());
		ac.setConfigurationName(xmlAC.getConfigurationName());
		ac.setArrayName(xmlAC.getArrayName());
		ac.setNumberOfAntennas(xmlAC.getNumberOfAntennas());
		ac.setMinBaseline(xmlAC.getMinBaseLine());
		ac.setMaxBaseline(xmlAC.getMaxBaseLine());
		if (ac.getArrayName().toLowerCase().equals("12-m"))
			ac.setAntennaDiameter(12.0);
		else if (ac.getArrayName().toLowerCase().equals("7-m"))
			ac.setAntennaDiameter(7.0);
		else
			ac.setAntennaDiameter(12.0);
		switch (xmlAC.getArrayType()) {
		case ACA:
			ac.setArrayType(ArrayType.ACA);
			break;
		case SEVEN_M:
			ac.setArrayType(ArrayType.SEVEN_M);
			break;
		case TP_ARRAY:
			ac.setArrayType(ArrayType.TP_ARRAY);
			break;
		case TWELVE_M:
			ac.setArrayType(ArrayType.TWELVE_M);
			break;
		}
		return ac;
	}
}
