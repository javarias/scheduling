/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File DynamicSchedulingAlgorithm.java
 */
 
package alma.scheduling.planning_mode_sim.scheduler.r1policy;

import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.SExec;
import alma.scheduling.planning_mode_sim.define.Clock;
import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SPolicy;
import alma.scheduling.planning_mode_sim.define.SPolicyFactor;
import alma.scheduling.planning_mode_sim.define.SiteCharacteristics;
import alma.scheduling.planning_mode_sim.define.SProject;
import alma.scheduling.planning_mode_sim.define.Priority;

import alma.scheduling.planning_mode_sim.scheduler.Scheduler;
import alma.scheduling.planning_mode_sim.define.WeatherCondition;

import alma.scheduling.planning_mode_sim.master_scheduler.ArchiveProxy;
import alma.scheduling.planning_mode_sim.master_scheduler.SchedulingException;

import java.util.logging.Logger;
import java.io.PrintStream;

/**
 * This is the dynamic scheduling algorithm for R1.
 * 
 * The way this will be implemented when we have more than one scheduling
 * policy concept is the following.  All such classes will be called
 * DynamicSchedulingAlgorithm and placed in distinct packages.  Each will
 * have a static name that identifies the algorithm.  Then we will use the
 * Java classloader to load the appropriate algorithm based on the 
 * scheduling policy. 
 * 
 * @version 1.00  Sep 26, 2003
 * @author Allen Farris
 */
public class DynamicSchedulingAlgorithm {

	static public final String name = "R1Policy"; 
	
	private SchedulingUnit[] unit;
	private Clock clock;
	private Logger log;
	private SPolicy policy;
	private ArchiveProxy archive;
		
	// The weighting factors.
	private double positionElW;
	private double positionMaxW;
	private double weatherW;
	private double priorityW;
	private double samePSameBW;
	private double samePDiffBW;
	private double diffPSameBW;
	private double diffPDiffBW;
	private double newPW;
	private double oneSBW;
	
	// A callback to the scheduler.
	private Scheduler scheduler;
	private SiteCharacteristics site;
	
	public DynamicSchedulingAlgorithm(Scheduler scheduler) {
		this.scheduler = scheduler;
		this.log = scheduler.getLogger();
		this.clock = scheduler.getClock();
		this.policy = scheduler.getSchedulingPolicy();
		this.site = scheduler.getSite();
		this.archive = scheduler.getArchive();
		// Set the weighting factors.
		setWeights();
		// Create the scheduling units.
		SUnit[] sb = scheduler.getSB();
		unit = new SchedulingUnit [sb.length];
		for (int i = 0; i < unit.length; ++i) {
			unit[i] = new SchedulingUnit(sb[i],site);
		}
		
		// Sort the list of scheduling units by scientific priority.
		SchedulingUnit tmp = null;
		int n = unit.length;
		if (n > 2) {
			int j = 0;
			int incr = n / 2;
			while (incr >= 1) {
				for (int i = incr; i < n; i++) {
					tmp = unit[i];
					j = i;
					while (j >= incr && unit[j - incr].getPriority() < tmp.getPriority()) {
						unit[j] = unit[j - incr];
						j -= incr;
					}
					unit[j] = tmp;
				}
				incr /= 2;
			}
		}
		
		log.info("DynamicSchedulingAlgorithm policy " + name);
		log.info("DynamicSchedulingAlgorithm position " + positionElW);
		log.info("DynamicSchedulingAlgorithm position " + positionMaxW);
		log.info("DynamicSchedulingAlgorithm weather " + weatherW);
		log.info("DynamicSchedulingAlgorithm priority " + priorityW);
		log.info("DynamicSchedulingAlgorithm sameProjectSameBand " + samePSameBW);
		log.info("DynamicSchedulingAlgorithm sameProjectDifferentBand " + samePDiffBW);
		log.info("DynamicSchedulingAlgorithm differentProjectSameBand " + diffPSameBW);
		log.info("DynamicSchedulingAlgorithm differentProjectDifferentBand " + diffPDiffBW);
		log.info("DynamicSchedulingAlgorithm newProject " + newPW);
		log.info("DynamicSchedulingAlgorithm oneSBRemaining " + oneSBW);
		for (int i = 0; i < unit.length; ++i) {
			log.info("DSA: " + unit[i]);
		}
	}

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;
	
	private void setWeights() {
		if (!policy.getName().equals("R1Policy")) {
			log.severe("Scheduling policy " + policy.getName() + " is not supported");
			System.exit(0);
		}
		SPolicyFactor[] factor = policy.getFactor();
		if (factor.length != 10) {
			log.severe("Scheduling policy has wrong number of factors");
			System.exit(0);			
		}
		String name = null;
		for (int i= 0; i < factor.length; ++i) {
			name = factor[i].getName();
			if (name.equals("positionElevation")) 
				positionElW = factor[i].getWeight();
			else if (name.equals("positionMaximum")) 
				positionMaxW = factor[i].getWeight();
			else if (name.equals("weather"))
				weatherW = factor[i].getWeight();
			else if (name.equals("priority"))
				priorityW = factor[i].getWeight();
			else if (name.equals("sameProjectSameBand"))
				samePSameBW = factor[i].getWeight();
			else if (name.equals("sameProjectDifferentBand"))
				samePDiffBW = factor[i].getWeight();
			else if (name.equals("differentProjectSameBand"))
				diffPSameBW = factor[i].getWeight();
			else if (name.equals("differentProjectDifferentBand"))
				diffPDiffBW = factor[i].getWeight();
			else if (name.equals("newProject"))
				newPW = factor[i].getWeight();
			else if (name.equals("oneSBRemaining"))
				oneSBW = factor[i].getWeight();
			else {
				log.severe("Improper scheduling factor name " + name);
				System.exit(0);			
			}
		}
	}

	public void score() {
		success();
		rank();		
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].isComplete()) continue;
			unit[i].setScore(unit[i].getSuccess() * unit[i].getRank());
		}
	}
	
	private double totalScore = 0.0;
	private double totalSuccess = 0.0;
	private double totalRank = 0.0;
	private double totalPositionEl = 0.0;
	private double totalPositionMax = 0.0;
	private double totalWeather = 0.0;
	private int totalNumber = 0;

	public String getBest() {
		if (unit.length == 0)
			return null;
		int i = 0;
		for (; i < unit.length; ++i) {
			if (!unit[i].isComplete())
				break;
		}
		if (i == unit.length)
			return null;
		SchedulingUnit max = unit[i];
		for (; i < unit.length; ++i) {
			if (unit[i].isComplete()) continue;
			if (unit[i].getScore() > max.getScore())
				max = unit[i];
		}
		if (max.getScore() <= 0.0) 
			return null;
		log.info("SB Best Score: " + max.getScore() + " Rank: " + max.getRank() + " Success: " 
			+ max.getSuccess() + " Time: " + clock.getDateTime());
		
		// update the totals
		++totalNumber;
		totalScore += max.getScore();
		totalSuccess += max.getSuccess();
		totalRank += max.getRank();
		totalPositionEl += max.getPositionEl();
		totalPositionMax += max.getPositionMax();
		totalWeather += max.getWeather();
		
		return max.getSB().getId();
	}
	
	public static final String REASON[] = { 
		"No sources were above the horizon.",
		"The weather conditions were unfavorable.",
		"The highest score was less than 0.0"
	};
	public int getReason() {
		double maxPosition = 0.0;
		double maxWeather = 0.0;
		double maxScore = 0.0;
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].isComplete()) continue;
			if (unit[i].getPositionEl() > maxPosition) {
				maxPosition = unit[i].getPositionEl();
				if (unit[i].getWeather() > maxWeather)
					maxWeather = unit[i].getWeather();
			}	
			if (unit[i].getPositionMax() > maxPosition) {
				maxPosition = unit[i].getPositionMax();
				if (unit[i].getWeather() > maxWeather)
					maxWeather = unit[i].getWeather();
			}	
		}
		if (maxPosition <= 0.0)
			return 0;
		if (maxWeather <= 0.0)
			return 1;
		return 2;
	}
	
	public void deleteSB(String sUnitId) {
		// We don't really delete the SB, we merely 
		// mark it complete.
		int i = 0;
		for (; i < unit.length; ++i) {
			if (unit[i].getSB().getId().equals(sUnitId))
				break;
		}
		if (i < unit.length) {
			unit[i].setComplete(true);
		}
	}
	
	public int remaining() {
		return unit.length;
	}
	
	private void rank() {
		SchedulingUnit u = null;
		double x = 0.0;
		String currentProjectId = scheduler.getCurrentProjectId();
		int currentBand = scheduler.getCurrentFrequencyBand();
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].isComplete()) continue;
			u = unit[i];
			if (u.getSuccess() == 0.0)
				u.setRank(0.0);
			else {
				setState(u,currentProjectId,currentBand);
				x = priorityW * u.getPriority();
				if (u.isSameProjectSameBand())
					x += samePSameBW;
				if (u.isSameProjectDifferentBand())
					x += samePDiffBW;
				if (u.isDifferentProjectSameBand())
					x += diffPSameBW;
				if (u.isDifferentProjectDifferentBand())
					x += diffPDiffBW;
				if (u.isNewProject())
					x += newPW;
				if (u.isOneSBRemaining())
					x += oneSBW;
				u.setRank(x);
			}
		}
	}
	
	private void success() {
		double pEl = 0.0;
		double pMax = 0.0;
		double w = 0.0;
		SUnit sb = null;
		SchedulingUnit u = null;
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].isComplete()) continue;
			u = unit[i];
			sb = u.getSB();
			pEl = positionEl(u);
			pMax = positionMax(u);
			w = weather(sb);
			u.setPositionEl(pEl);
			u.setPositionMax(pMax);
			u.setWeather(w);
			if (pEl == 0.0 || pMax == 0.0 || w == 0.0) {
				u.setSuccess(0.0);
			} else {
				u.setSuccess((positionElW * pEl + positionMaxW * pMax + weatherW * w) / (positionElW + positionMaxW + weatherW));
			}
		}
	}

	private void setState(SchedulingUnit u, String currentProjectId, int currentBand) {
		SUnit sb = u.getSB();
		if (currentProjectId.equals(sb.getProjectId())) {
			if (currentBand == sb.getFrequencyBand()) {
				u.setSameProjectSameBand(true);
				u.setSameProjectDifferentBand(false);
				u.setDifferentProjectDifferentBand(false);
				u.setDifferentProjectSameBand(false);
			} else {
				u.setSameProjectSameBand(false);
				u.setSameProjectDifferentBand(true);
				u.setDifferentProjectDifferentBand(false);
				u.setDifferentProjectSameBand(false);
			}
		} else {
			if (currentBand == sb.getFrequencyBand()) {
				u.setSameProjectSameBand(false);
				u.setSameProjectDifferentBand(false);
				u.setDifferentProjectDifferentBand(false);
				u.setDifferentProjectSameBand(true);
			} else {
				u.setSameProjectSameBand(false);
				u.setSameProjectDifferentBand(false);
				u.setDifferentProjectDifferentBand(true);
				u.setDifferentProjectSameBand(false);
			}
		}
		if (scheduler.isNewProject(sb)) {
			u.setNewProject(true);
		}
		if (scheduler.getRemainingSB(sb.getProjectId()) == 1) {
			u.setOneSBRemaining(true);
		}
	}
	
	private double positionEl(SchedulingUnit u) {
		if (u.isVisible(clock.getDateTime()))
			return Math.sin(u.getElevation(clock.getDateTime(),site));
		return 0.0;
	}

	private double positionMax(SchedulingUnit u) {
		// First, we make sure the source is visible.
		DateTime t = clock.getDateTime();
		if (!u.isVisible(t))
			return 0.0;
		// Second, make sure it is still visible at the end of the observing time.
		t.add(u.getSB().getMaximumTimeInSeconds());
		if (!u.isVisible(t))
			return 0.0;
		// Ok, now the idea is to give a high weight to a position if it is close 
		// to the maximum elevation of the source.
		// We compute a delta; its units are hours.  This delta is the absolute value of 
		// the difference between the current LST and the LST at the maximum elevation.  
		// However, the LST at the maximum elevation is adjusted so that its maximum elevation 
		// would occur in the middle of this scheduling unit's observing period.
		double delta = Math.abs((u.getLstMax() * radToHour) - 
								(u.getSB().getMaximumTimeInSeconds() / 7200.0) - 
								clock.getTimeOfDay());
		if (0.0 <= delta && delta < 0.5) return 1.0;
		if (0.5 <= delta && delta < 1.0) return 0.9;
		if (1.0 <= delta && delta < 1.5) return 0.8;
		return 0.0;
		
	}
	
	private double weather(SUnit sb) {
		double x = 1.0;
		try {
			WeatherCondition w = sb.getWeatherConstraint();
			if (w != null)
				x =  w.evaluate();
		} catch (Exception err) {
			err.printStackTrace();
			System.exit(0);
		}
		return x;
	}


	private DateTime day = null;
	
	public void generateOutputRecord(PrintStream out, short subarrayId, SUnit sb, SExec ex) {
		SchedulingUnit u = null;
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].getSB() == sb) {
				u = unit[i];
				break;
			}
		}
		if (u == null) {
			System.out.println("SB " + sb.getId() + " was not found.");
			System.exit(0);
		}
		if (day == null) {
			DateTime t = ex.getStartTime();
			day = new DateTime(ex.getStartTime().getDate(),0.0);
			out.println(day + " LST");
			out.println();
			out.println("SB-id  Exec# Array   Start      End         Pos  Weather Success  Rank");
		} else {
			DateTime y = DateTime.add(day,86400);
			if (ex.getStartTime().gt(y)) {
				out.println();
				day = new DateTime(ex.getStartTime().getDate(),0.0);
				out.println(day + " LST");				
				out.println();
				out.println("SB-id  Exec# Array   Start      End         Pos  Weather Success  Rank");
			}
		}
		String blank = "          ";
		String id = sb.getSchedBlockId();
		id = id + blank.substring(id.length());
		
		String st1 = ex.getStartTime().toString();
		String st2 = ex.getEndTime().toString();
		st1 = st1.substring(st1.indexOf("T") + 1);
		st2 = st2.substring(st2.indexOf("T") + 1);
		double pos = (positionElW*u.getPositionEl() + positionMaxW*u.getPositionMax()) / (positionElW + positionMaxW);
		int p1 = (int)(pos * 100.0 + 0.5);
		String sp1 = " ";
		if (p1 >= 100) sp1 = sp1 + p1; else sp1 = sp1 + " " + p1;
		int w1 = (int)(u.getWeather() * 100.0 + 0.5);
		String sw1 = " ";
		if (w1 >= 100) sw1 = sw1 + w1; else sw1 = sw1 + " " + w1;
		int s1 = (int)(u.getSuccess() * 100.0 + 0.5);
		String ss1 = " ";
		if (s1 >= 100) ss1 = ss1 + s1; else ss1 = ss1 + " " + s1;
		int r1 = (int)(u.getRank() + 0.5);
		String sr1 = " ";
		if (r1 >= 100) sr1 = sr1 + r1; else sr1 = sr1 + " " + r1;
		out.println(id +  
			ex.getMemberIndex() + "    " + 
			subarrayId + "     " +
			st1 + "   " + 
			st2 + "   " +
			sp1 + "   " +
			sw1 + "   " +
			ss1 + "   " +
			sr1);
	}

	public void showSummary(PrintStream out) {
		out.println();
		out.println("Sources - Visibility and Executions");
		out.println("\t(\'-\' is visible   \'+\' is visible and was executed");
		out.println("\t(\'X\' max elevation   \'*\' max elevation and was executed)");
		out.println();
		double lst = 0.0;
		final int scale = 48;
		final double scaleFactor = 2.0;
		char[] s = new char [scale];
		String line = "";
		boolean isVisible = false;
		double nextLstRise = 0.0;
		double prevLstSet = 0.0;
		DateTime t = new DateTime(clock.getDateTime().getDate(),0.0);
		String blank = "          ";
		String id = null;
		int maxPos = 0;
		SUnit sb = null;
		int iex = 0;
		SExec ex = null;
		for (int i = 0; i < unit.length; ++i) {
			for (int ilst = 0; ilst < scale; ++ilst) {
				t.add((int)(3600.0 / scaleFactor));
				if (unit[i].isVisible(t))
					s[ilst] = '-';
				else
					s[ilst] = ' ';
			}			
			maxPos = (int)(unit[i].getSB().getTarget().getCenter().getRa() * radToHour * scaleFactor + 0.5);
			if (maxPos > (scale - 1)) 
				maxPos = 0;
			s[maxPos] = 'X';
			sb = unit[i].getSB();
			double td = 0.0;
			double tdEnd = 0.0;
			for (int j = 0; j < sb.getNumberMembers(); ++j) {
				ex = (SExec)sb.getMember(j);
				td = ex.getStartTime().getTimeOfDay();
				tdEnd = ex.getEndTime().getTimeOfDay();
				while (td <= tdEnd) {
					iex = (int)(td * scaleFactor + 0.5);
					if (iex > (scale - 1))
						iex = 0;
					if (s[iex] == ' ') out.println("Something's wrong here!" + ex.getParent());
					else if (s[iex] == '-') s[iex] = '+';
					else if (s[iex] == 'X') s[iex] = '*';
					else if (s[iex] == '+') s[iex] = '+';
					else if (s[iex] == '*') s[iex] = '*';
					else s[iex] = '?';
					td += 1.0 / scaleFactor;
					if (td >= 24.0)
						td -= 24.0;
				}
			}
			line = new String (s);
			id = unit[i].getSB().getSchedBlockId();
			id = id + blank.substring(id.length());
			out.println(id + " |" + line + "|");
		}
		id = blank;
		out.println(id + " |+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|");
		out.println(id + " |0   2   4   6   8  10  12  14  16  18  20  22   |");
		out.println(id + "                        LST ");
		
		projectSummary(out);
		statistics(out);
	}
	
	private void projectSummary(PrintStream out) {
		out.println();
		out.println("Project Summary");
		out.println();
		SProject[] prj = null;
		DateTime t = null;
		try {
			prj = archive.getAllProject();
			for (int i = 0; i < prj.length; ++i) {
				out.println("Project " + prj[i].getObsProjectId());
				out.println("\tNumber of scheduling blocks completed  " + prj[i].getNumberUnitsCompleted());
				out.println("\tNumber of scheduling blocks incomplete " + 
							(prj[i].getTotalUnits() - prj[i].getNumberUnitsCompleted()));
				t = prj[i].getStartTime();
				if (t.getJD() == 0.0)
					out.println("\tStart time: never started");
				else
					out.println("\tStart time: " + t);
				t = prj[i].getEndTime();
				if (t.getJD() == 0.0)
					out.println("\tEnd time:   never ended");
				else
					out.println("\tEnd time:   " + t);
			}
		} catch (SchedulingException err) {
			err.printStackTrace();
			System.exit(0);
		}
	}
	
	private void statistics(PrintStream out) {
		out.println();
		out.println("Scheduling Statistics");
		out.println();

		out.println("Number of executions             " + totalNumber); 		

		SUnit[] u = null;
		SExec[] ex = null;
		
		// All time units are hours.
		double totalTime = DateTime.difference(scheduler.getEndTime(),scheduler.getBeginTime()) * 24.0;
		double totalScienceTime = 0.0;
		double totalWeightedScienceTime = 0.0;
		double possibleScienceTime = 0.0;
		double x = 0.0;
		
		try {
			u = archive.getAllSUnit();
			for (int i = 0; i < u.length; ++i) {
				ex = u[i].getSExec();
				possibleScienceTime += u[i].getMaximumTimeInSeconds() * (u[i].getMaximumNumberOfRepeats() + 1) / 3600.0;
				for (int j = 0; j < ex.length; ++j) {
					x = DateTime.difference(ex[j].getEndTime(), ex[j].getStartTime());
					totalScienceTime += x * 24.0;
					totalWeightedScienceTime += x * u[i].getScientificPriority().getPriorityAsInt() * 24.0;
				}
			}
		} catch (SchedulingException err) {
			err.printStackTrace();
			System.exit(0);
		}

		double effiency = totalScienceTime / totalTime;
		double weightedEffiency = totalWeightedScienceTime / (totalTime * Priority.HIGHEST.getPriorityAsInt());

		out.println();
		out.println("Efficiency (%)                   " + (float)(effiency * 100.0));
		out.println("Weighted Efficiency  (%)         " + (float)(weightedEffiency * 100.0));
		out.println("% of science time executed       " + (float)((totalScienceTime / possibleScienceTime) * 100.0));

		out.println();
		out.println("Total time (hours)               " + (float)totalTime);
		out.println("Total science time (hours)       " + (float)totalScienceTime);
		out.println("Possible science time (hours)    " + (float)possibleScienceTime);

		out.println();
		out.println("Average score                    " + (float)(totalScore / totalNumber));
		out.println("Average rank                     " + (float)(totalRank / totalNumber));
		out.println("Average success factor           " + (float)(totalSuccess / totalNumber));
		out.println("Average positionElevation factor " + (float)(totalPositionEl / totalNumber));
		out.println("Average positionMaximum factor   " + (float)(totalPositionMax / totalNumber));
		out.println("Average weather factor           " + (float)(totalWeather / totalNumber));
	}
}
