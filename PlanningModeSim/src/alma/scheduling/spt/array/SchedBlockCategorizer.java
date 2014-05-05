package alma.scheduling.spt.array;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.input.observatory.generated.ArrayLSTRequestedInterval;
import alma.scheduling.input.observatory.generated.DateInterval;
import alma.scheduling.input.observatory.generated.IntervalRequested;
import alma.scheduling.input.observatory.generated.ObsCycleProfile;
import alma.scheduling.input.observatory.generated.ObsCycleProfiles;
import alma.scheduling.input.observatory.generated.types.ArrayTypeT;
import alma.scheduling.spt.util.ArrayConfigurationCapabilities;
import alma.scheduling.spt.util.LSTRange;
import alma.scheduling.spt.util.SimulatorContextFactory;
import alma.scheduling.utils.Constants;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.TimeUtil;

public class SchedBlockCategorizer {

	private static final double convertToArcSec = 180.D / Math.PI * 3600.0D;
	private static final double PHI = -23.022894444444443 * Math.PI/180.0;
	private ObservatoryDao obsDao;
	private SchedBlockDao sbDao;
	private Map<ArrayConfigurationCapabilities, Set<SchedBlock>> arraySBMap;
	private Map<SchedBlock, Set<ArrayConfigurationCapabilities>> sbArrayMap;
	private HashMap<ArrayType, Set<ArrayConfigurationCapabilities>> typeMap;
	private Set<SchedBlock> unmappedSBs;
	
	public SchedBlockCategorizer() {
		ApplicationContext ctx = DSAContextFactory.getContext();
		SimulatorContextFactory.doFullLoad();
		obsDao = (ObservatoryDao) ctx.getBean(DSAContextFactory.SCHEDULING_OBSERVATORY_DAO);
		sbDao = (SchedBlockDao) ctx.getBean(DSAContextFactory.SCHEDULING_SCHEDBLOCK_DAO_BEAN);
	}
	
	public Map<ArrayConfigurationCapabilities, Set<SchedBlock>> getSBArrayMapping() {
		arraySBMap = new HashMap<>();
		sbArrayMap = new HashMap<>(); 
		typeMap = new HashMap<>();
		unmappedSBs = new HashSet<>();
		for(ArrayType t: ArrayType.values()) {
			typeMap.put(t, new HashSet<ArrayConfigurationCapabilities>());
		}
		
		for (ArrayConfiguration aCnf : obsDao.findArrayConfigurations()) {
			arraySBMap.put(new ArrayConfigurationCapabilities(aCnf), new HashSet<SchedBlock>());
			typeMap.get(aCnf.getArrayType()).add(new ArrayConfigurationCapabilities(aCnf));
		}
		
		System.out.println("Grand Total SB: " + sbDao.findAll().size());
		for(SchedBlock sb: sbDao.findAll()) {
			sbArrayMap.put(sb, new HashSet<ArrayConfigurationCapabilities>());
			boolean mapped = false;
			for(ArrayConfigurationCapabilities aCnf: typeMap.get(sb.getObsUnitControl().getArrayRequested())) {
				double maxBL = aCnf.getMaxBaseline();
				double lambda = Constants.LIGHT_SPEED / (sb.getRepresentativeFrequency() * 1E9);
				if (aCnf.getArrayType().compareTo(ArrayType.TWELVE_M) == 0) {
					if (sb.getSchedulingConstraints().getMinAngularResolution() <= (lambda / maxBL * convertToArcSec)
							&& sb.getSchedulingConstraints().getMaxAngularResolution() >= (lambda / maxBL * convertToArcSec)) {
						if (sb.getLetterGrade().equals(ScienceGrade.A)) {// || sb.getLetterGrade().equals(ScienceGrade.B)) {
							arraySBMap.get(aCnf).add(sb);
							sbArrayMap.get(sb).add(aCnf);
							mapped = true;
//							break;
						}
					}
				} else {
					if (aCnf.getArrayType().equals(sb.getObsUnitControl().getArrayRequested())){
						arraySBMap.get(aCnf).add(sb);
						sbArrayMap.get(sb).add(aCnf);
						mapped = true;
//						break;
					}
				}
			}
			if (!mapped && !(sb.getLetterGrade().equals(ScienceGrade.B) || sb.getLetterGrade().equals(ScienceGrade.C) || sb.getLetterGrade().equals(ScienceGrade.D)))
				unmappedSBs.add(sb);
		}
		
		return arraySBMap;
	}
	
	public Map<ArrayConfigurationCapabilities, Set<SchedBlock>> findCriticalSBSet() {
		HashMap<ArrayConfigurationCapabilities, Set<SchedBlock>> retVal = new HashMap<>();
		for (ArrayConfigurationCapabilities ac: arraySBMap.keySet()) {
			if (typeMap.get(ac.getArrayType()).size() == 1)
				continue;
			retVal.put(ac, new HashSet<SchedBlock>());
			for(SchedBlock sb: arraySBMap.get(ac)) {
				if (sbArrayMap.get(sb).size() > 1)
					continue;
				retVal.get(ac).add(sb);
			}
			if(retVal.get(ac).size() == 0)
				retVal.remove(ac);
		}
		return retVal;
	}
	
	public Map<SchedBlock, Set<ArrayConfigurationCapabilities>> findComplementToCriticalSet(
			Map<ArrayConfigurationCapabilities, Set<SchedBlock>> criticalSBSet) {
		HashMap<SchedBlock, Set<ArrayConfigurationCapabilities>> retVal = new HashMap<>();
		HashSet<SchedBlock> sbCriticalList = new HashSet<>();
		for(ArrayConfigurationCapabilities c: criticalSBSet.keySet()) {
			for (SchedBlock sb: criticalSBSet.get(c)) {
				sbCriticalList.add(sb);
			}
		}
		for (SchedBlock sb: sbArrayMap.keySet()) {
			if (!sbCriticalList.contains(sb) && sbArrayMap.get(sb).size() != 1)
				if (!(sb.getLetterGrade().equals(ScienceGrade.B) || sb.getLetterGrade().equals(ScienceGrade.C) || sb.getLetterGrade().equals(ScienceGrade.D)))
					retVal.put(sb, sbArrayMap.get(sb));
		}
		return retVal;
	}
	
	private void showGnuPlot(List<SchedBlock> sbs, String title) {
		FileWriter fw = null;
		File tmpGnuPlot = new File("/tmp/tmp" + System.currentTimeMillis() + ".plot");
		try {
			fw = new FileWriter(tmpGnuPlot);
			fw.write("set xrange [0:24]\n");
			fw.write("set yrange [0:90]\n");
			fw.write("set multiplot\n");
			for(SchedBlock sb: sbs) {
				SkyCoordinates c = sb.getRepresentativeCoordinates();
				fw.write("plot asin(sin(" + (-23.022894444444443*Math.PI/180) + ")*sin(" + (c.getDec()*Math.PI/180) + ")"
						+ "+cos("+ (-23.022894444444443*Math.PI/180) + ")*cos(" + (c.getDec()*Math.PI/180) + ")*cos(x*"+(Math.PI/12)+"-"+(c.getRA())*Math.PI/180 +"))*180/"+ Math.PI +" title '"+ title +"';\n");
			}
			fw.write("unset multiplot;");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
			}
		}
		try {
			Runtime.getRuntime().exec("gnuplot --persist " + tmpGnuPlot.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void calculateLSTForInterval(Date start, Date end, File out) {
		FileWriter fw = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			fw = new FileWriter(out);
			Date currDate = start;
			while (currDate.before(end)) {
				fw.write(format.format(currDate)+ ";" + TimeUtil.getLocalSiderealTime(currDate, -23.022894444444443) + "\n");
				currDate = new Date(currDate.getTime() + 15 * 60 * 1000);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
				}
		}
	}
	
	public Map<Double, Double> getHighestAltitudeLSTand15LST(SkyCoordinates c) {
		HashMap<Double, Double> retVal = new HashMap<>();
		double LST15 = (Math.sin(15.0 * Math.PI/180.0) - Math.sin(PHI)*Math.sin(c.getDec()*Math.PI/180)) / (Math.cos(PHI)*Math.cos(c.getDec()*Math.PI/180));
		LST15 = Math.acos(LST15) * 12 / Math.PI;
		retVal.put(c.getRA()*12.0/180.0, LST15);
		return retVal;
	}
	
	/**
	 * 
	 * @param sbSet
	 * @param obsPeriodDuration in Hours
	 * @return
	 */
	public Map<ArrayConfigurationCapabilities, Set<LSTRange>> getProposedLSTRanges(
			Map<ArrayConfigurationCapabilities, Set<SchedBlock>> sbSet, double obsPeriodDuration) {
		HashMap<ArrayConfigurationCapabilities, Set<LSTRange>> retVal = new HashMap<>();
		for (ArrayConfigurationCapabilities ac: sbSet.keySet()) {
			TreeMap<LSTRange, Set<SchedBlock>> lstRanges = new TreeMap<>();
			TreeMap<Double, Set<SchedBlock>> sourcesLst = new TreeMap<>();
			for(SchedBlock sb: sbSet.get(ac)) {
				double lst = sb.getRepresentativeCoordinates().getRA()*12.0/180.0;
				if (sourcesLst.containsKey(lst))
					sourcesLst.get(lst).add(sb);
				else {
					HashSet<SchedBlock> sbs = new HashSet<>();
					sbs.add(sb);
					sourcesLst.put(lst, sbs);
				}
			}
			//Copy the sources, put them at the beginning deducting 24 and put them at the end adding 24 
			{
				TreeMap<Double, Set<SchedBlock>> tmp = new TreeMap<>();
				for (Double s : ((NavigableSet<Double>)sourcesLst.keySet()).subSet(0.0, true, 24.0, true)) {
					tmp.put((s - 24.0), sourcesLst.get(s));
					tmp.put((s + 24.0), sourcesLst.get(s));
				}
				tmp.putAll(sourcesLst);
				sourcesLst = tmp;
			}
			//start testing for lst ranges
			Double s = 0.0;
			while (s < 24.0) {
				s = sourcesLst.higherKey(s);
				LSTRange range = null;
				Set<SchedBlock> nSources = new HashSet<>();
				for (int i = 0; i < 17; i++) {
					double lstStart = s - obsPeriodDuration * (16 - i) * 1.0/16.0;
					double lstEnd = s + obsPeriodDuration * i * 1.0/16.0;
					Set<SchedBlock> tmp = new HashSet<>(); 
					for (Entry<Double, Set<SchedBlock>> e:	sourcesLst.subMap(lstStart, true, lstEnd, true).entrySet())
						tmp.addAll(e.getValue());
					if (tmp.size() > nSources.size()) {
						range = new LSTRange(lstStart, lstEnd);
						nSources = tmp;
					}
				}
				lstRanges.put(range, nSources);
			}
			
			int weeks = 4;
			Set<LSTRange> rangesToRemove = new HashSet<>();
			TreeMap<LSTRange, Set<SchedBlock>> tmpLstRanges = new TreeMap<>();
			for (LSTRange range: lstRanges.keySet()) {
				if (rangesToRemove.contains(range))
					continue;
				LSTRange selectedRange = range;
				Set<SchedBlock> nSources = new HashSet<>(lstRanges.get(range));
				for (int i = 0; i < 101; i++) {
					double lstStart = range.getStartLST() - weeks * 27.57888/60.0 * (100 - i)/100.0;
					double lstEnd = range.getEndLST() + weeks * 27.57888/60.0 * i/100.0;
					LSTRange enclosingRange = new LSTRange(lstStart, lstEnd);
					for (LSTRange tmp: lstRanges.keySet()) {
						if (!enclosingRange.isEnclosingLSTRange(tmp))
							continue;
						HashSet<SchedBlock> tmpSrc = new HashSet<>();
						tmpSrc.addAll(nSources);
						tmpSrc.addAll(lstRanges.get(tmp));
						if (tmpSrc.size() >= nSources.size()) {
							nSources.addAll(lstRanges.get(tmp));
							selectedRange = enclosingRange;
							rangesToRemove.add(tmp);
						}
					}
				}
				tmpLstRanges.put(selectedRange, nSources);
			}
			lstRanges = tmpLstRanges;
			retVal.put(ac, lstRanges.keySet());
		}
		return retVal;
	}
	
	private static class SkyCoordinatesComparator implements Comparator<SchedBlock> {

		@Override
		public int compare(SchedBlock o1, SchedBlock o2) {
			return (int)((o1.getRepresentativeCoordinates().getDec() - o2.getRepresentativeCoordinates().getDec())*1000 
					+ (o1.getRepresentativeCoordinates().getRA() - o2.getRepresentativeCoordinates().getRA())*1000000);
		}
		
	}
	
	public ObsCycleProfiles getObsCycleProfiles(Map<ArrayConfigurationCapabilities, Set<LSTRange>> LSTRanges) {
		ObsCycleProfiles profiles = new ObsCycleProfiles();
		ExecutiveDAO execDao = (ExecutiveDAO) DSAContextFactory.getContext().getBean(DSAContextFactory.SCHEDULING_EXECUTIVE_DAO_BEAN);
		ObservingSeason cycle = execDao.getCurrentSeason();
		ObsCycleProfile prof = new ObsCycleProfile();
		DateInterval di = new DateInterval();
		di.setStartDate(cycle.getStartDate());
		di.setEndDate(cycle.getEndDate());
		prof.setDateInterval(di);
		profiles.addObsCycleProfile(prof);
		for (ArrayConfigurationCapabilities ac: LSTRanges.keySet()) {
			ArrayLSTRequestedInterval a = new ArrayLSTRequestedInterval();
			a.setArrayName(ac.getArrayName());
			a.setArrayType(ArrayTypeT.fromValue(ac.getArrayType().toString()));
			a.setConfigurationName(ac.getConfigurationName());
			a.setEndTime(ac.getEndTime());
			a.setMaxBaseLine(ac.getMaxBaseline());
			a.setMinBaseLine(ac.getMinBaseline());
			a.setNumberOfAntennas(ac.getNumberOfAntennas());
			a.setStartTime(ac.getStartTime());
			for(LSTRange lr: LSTRanges.get(ac)) {
				IntervalRequested i = new IntervalRequested();
				i.setEndLST(lr.getEndLST());
				i.setStartLST(lr.getStartLST());
				a.addIntervalRequested(i);
			}
			prof.addArrayLSTRequestedInterval(a);
		}
		return profiles;
//		try {
//			FileWriter out = new FileWriter(new File("/tmp/test.xml"));
//			profiles.marshal(out);
//			System.out.println(out.toString());
//		} catch (MarshalException | ValidationException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public ObsCycleProfiles calculateObsCycleProfiles() {
		HashSet<SchedBlock> allSB = new HashSet<>();
		getSBArrayMapping();
		Map<ArrayConfigurationCapabilities, Set<SchedBlock>> sbCriticalSet = findCriticalSBSet();
		System.out.println(sbCriticalSet);
		
		for(ArrayConfigurationCapabilities ac: sbCriticalSet.keySet()) {
			System.out.println(ac);
			ArrayList<SchedBlock> sortedList = new ArrayList<>(sbCriticalSet.get(ac));
			Collections.sort(sortedList, new SkyCoordinatesComparator());
			for (SchedBlock sb: sortedList) {
				double lstMaxH = (sb.getRepresentativeCoordinates().getRA()) *12.0/180.0;
				double lstMaxHMin = (lstMaxH - 2) < 0? lstMaxH + 22:lstMaxH - 2;
				double lstMaxHMax = (lstMaxH + 2) >= 24? lstMaxH - 22:lstMaxH + 2;
				System.out.println(String.format("%s\t%s\tRa: %.4f\tDec: %.4f\tLST:%.4f\tLST Range: %.4f -- %.4f", 
						sb.getUid(), sb.getLetterGrade(), sb.getRepresentativeCoordinates().getRA() *12.0/180.0, sb.getRepresentativeCoordinates().getDec(),
						lstMaxH, lstMaxHMin, lstMaxHMax));
//				System.out.println(sbc.getHighestAltitudeLSTand15LST(sb.getRepresentativeCoordinates()));
			}
//			sbc.showGnuPlot(sortedList, ac.getConfigurationName());
			System.out.println("---------------------------------------------------------------------------\n");
		}
		allSB.addAll(unmappedSBs);
		for(ArrayConfigurationCapabilities ac:arraySBMap.keySet()) {
			allSB.addAll(arraySBMap.get(ac));
		}
		System.out.println("Total SB: " + allSB.size());
		System.out.println("Unmapped Scheduling Blocks: " + unmappedSBs.size());
//		for (SchedBlock sb: sbc.unmappedSBs) {
//			System.out.println(sb.getUid() + "\t" + sb.getObsUnitControl().getArrayRequested());
//		}
		
		Map<SchedBlock, Set<ArrayConfigurationCapabilities>> complementToSbCritSet = findComplementToCriticalSet(sbCriticalSet);
		for (SchedBlock sb: complementToSbCritSet.keySet()) {
			System.out.println(String.format("%s\t%s\tRa: %.4f\tDec: %.4f\t", 
					sb.getUid(), sb.getLetterGrade(), sb.getRepresentativeCoordinates().getRA() *12.0/180.0, sb.getRepresentativeCoordinates().getDec()));
			System.out.println(complementToSbCritSet.get(sb));
		}
		
		Map<ArrayConfigurationCapabilities, Set<LSTRange>> ranges = getProposedLSTRanges(sbCriticalSet, 8.0);
		for (Entry<ArrayConfigurationCapabilities, Set<LSTRange>> e: ranges.entrySet()) {
			System.out.println(e.getKey());
			for(LSTRange r: e.getValue()) {
				System.out.println(r);
			}
			System.out.println("------------------------------------------------------------------------\n");
		}
		return getObsCycleProfiles(ranges);
//		sbc.calculateLSTForInterval(startDate, endDate, new File("lstRange.csv"));
	}
	
	public void cleanUp() {
		SimulatorContextFactory.closeContext();
	}
	
}
