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
 * File Reporter.java
 */
 
package alma.Scheduling.PlanningModeSim;

import alma.Scheduling.PlanningModeSim.Define.BasicComponent;
import alma.Scheduling.PlanningModeSim.Define.SimulationException;
import alma.Scheduling.Define.DateTime;
import alma.Scheduling.Define.ExecBlock;
import alma.Scheduling.Define.SB;
import alma.Scheduling.Define.BestSB;
import alma.Scheduling.Define.SchedulingException;
import alma.Scheduling.Define.SiteCharacteristics;
import alma.Scheduling.Define.Project;
import alma.Scheduling.Define.NothingCanBeScheduled;
import alma.Scheduling.Define.Priority;

import java.util.logging.Level;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 * Description 
 * 
 * @version 1.00  Dec 12, 2003
 * @author Allen Farris
 */
public class Reporter extends BasicComponent {
	
	static private final double radToDeg = 180.0 / Math.PI;

	private ArchiveSimulator archive;
	private SimulationInput input;
	private PrintStream out;
	
	private DateTime beginTime;
	private DateTime endTime;
	private NumberFormat dform;
	
	// The accumulated list of messages.
	private ArrayList message;
	

	/**
	 * 
	 */
	public Reporter() {
		message = new ArrayList ();
		dform = NumberFormat.getInstance();
		dform.setMaximumFractionDigits(0);
	}

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		logger.severe("Reporter.error " + message);
		throw new SimulationException("Reporter","Reporter " + Level.SEVERE + " " + message);
	}

	public void initialize() throws SimulationException {
		input = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
		archive = (ArchiveSimulator)containerServices.getComponent(Container.ARCHIVE);
		logger.info(instanceName + ".initialized");
	}

	private DateTime currrentDay = null; // This is the current day in the simulation.
	
	public void execStart(int subarrayId, String sbId, DateTime time) {
	}
	
	public void execEnd(int subarrayId, String sbId, String execId, DateTime time) throws SchedulingException {
		ExecBlock ex = archive.getExec(execId);
		SB sb = archive.getSB(sbId);
		DateTime startTime = ex.getStatus().getStartTime();
		DateTime endTime = ex.getStatus().getEndTime();
		BestSB best = ex.getBest();

		if (currrentDay == null) {
			currrentDay = new DateTime(startTime.getDate(),0.0);
			out.println(currrentDay + " LST");
			out.println();
			out.println("SB-id  Exec# Array    Start       End        Score  Success  Rank");
		} else {
			DateTime y = DateTime.add(currrentDay,86400);
			if (startTime.gt(y)) {
				out.println();
				currrentDay = new DateTime(startTime.getDate(),0.0);
				out.println(currrentDay + " LST");				
				out.println();
				out.println("SB-id  Exec# Array    Start       End        Score  Success  Rank");
			}
		}
		
		checkMessages();
		
		String blank = "          ";
		String id = sbId;
		id = id + blank.substring(id.length());
		
		String st1 = startTime.toString();
		String st2 = endTime.toString();
		st1 = st1.substring(st1.indexOf("T") + 1);
		st2 = st2.substring(st2.indexOf("T") + 1);
		int sel = best.getSelection();
		double score = best.getScore()[sel];
		double rank = best.getRank()[sel];
		double success = best.getSuccess()[sel] * 100.0;
		String sScore = dform.format(score);
		String sRank = dform.format(success);
		String sSuccess = dform.format(rank);
		sScore = ((sScore.length() == 2) ? "      " : "     ") + sScore;
		sSuccess = ((sSuccess.length() == 2) ? "      " : "     ") + sSuccess;
		sRank = ((sRank.length() == 2) ? "      " : "     ") + sRank;
		int executionNumber = 0;
		String[] n = sb.getExec();
		for (int i = 0; i < n.length; ++i) {
			if (n[i].equals(ex.getId())) {
				executionNumber = i;
				break;
			}
		}
		out.println(id +  
				executionNumber + "    " + 
				subarrayId + "     " +
				st1 + "   " + 
				st2 + sScore + sSuccess + sRank);
		
		
		
	}
	private void checkMessages() {
		if (message.size() != 0) {
			NothingCanBeScheduled[] x = new NothingCanBeScheduled [message.size()];
			x = (NothingCanBeScheduled[])message.toArray(x);
			int[] reason = new int [NothingCanBeScheduled.Other + 1];
			for (int i = 0; i < reason.length; ++i)
				reason[i] = 0;
			for (int i = 0; i < x.length; ++i) {
				reason[x[i].getReason()] = 1;
			}
			String st1 = x[0].getTime().toString();
			String st2 = x[x.length - 1].getTime().toString();
			st1 = st1.substring(st1.indexOf("T") + 1);
			st2 = st2.substring(st2.indexOf("T") + 1);
			out.println();
			out.println("Nothing could be scheduled from " + st1 + " to " + st2);
			for (int i = 0; i < reason.length; ++i) {
				if (reason[i] == 1) 
					out.println(NothingCanBeScheduled.Message[i]);
			}
			out.println();
			message.clear();
		}
	}
	
	public void nothingCouldBeScheduled(NothingCanBeScheduled x) {
		// Store the object in message.
		message.add(x);
	}
	
	public void schedulingIsComplete(DateTime endTime, int reason, String comment) {
		this.endTime = new DateTime (endTime);
		out.println();
		out.println("End of Schedule");
		writeEnding(endTime);
	}
	
	public void schedulingIsBeginning(DateTime t) throws SimulationException {
		beginTime = new DateTime (t);
		try {
			// Create the output text file.
			out = new PrintStream (new FileOutputStream (input.getOutFile()));
		} catch (IOException ioerr) {
			 error("Could not open file " + input.getOutFile().getAbsolutePath() + " -- " + ioerr.toString());
		}
		writeBeginning();
	}
	
	private void writeBeginning() {
		// Write the initial data in the output file. Place this in Reporter.
		SiteCharacteristics site = input.getSite();
		out.println("ALMA Simulator Release R2.1 - May 4, 2004");
		out.println();
		out.println("Beginning simulation run.  System time: " + DateTime.currentSystemTime());
		out.println("Input properties from file: " + input.getInputFile().getAbsolutePath());
		out.println("Site location: " + site.getLongitude() * radToDeg + " deg longitude " + 
					site.getLatitude() * radToDeg + " deg latitude");
		out.println("Number of antennas: " + site.getNumberAntennas());
		out.println();
		out.println("Starting time: " + input.getBeginCivilTime());
		out.println("Ending time: " + input.getEndCivilTime());
		out.println("All subsequent times are LST");
		out.println();
		out.println("Starting time: " + input.getBeginTime() + " LST");
		out.println("Ending time: " + input.getEndTime() + " LST");
		out.println();
		out.println("Schedule");
		out.println();
	}
	
	private void writeEnding(DateTime endTime) {
		//showSummary(out,endTime);
		projectSummary(out,endTime);
		statistics(out,endTime);
	}
	
	private void showSummary(PrintStream out, DateTime endTime) {
		
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
		DateTime t = new DateTime(endTime.getDate(),0.0);
		String blank = "          ";
		String id = null;
		int maxPos = 0;
		int iex = 0;
		ExecBlock[] ex = null;
		SB[] unit = null; // These units are really scheduling units.
		try {
			unit = archive.getAllSB();
		} catch (SchedulingException err) {
			err.printStackTrace(out);
			System.exit(0);
		}
		for (int i = 0; i < unit.length; ++i) {
			for (int ilst = 0; ilst < scale; ++ilst) {
				t.add((int)(3600.0 / scaleFactor));
				if (unit[i].getTarget().isVisible(t))
					s[ilst] = '-';
				else
					s[ilst] = ' ';
			}			
			maxPos = (int)(unit[i].getTarget().getCenter().getRaInHours() * scaleFactor + 0.5);
			if (maxPos > (scale - 1)) 
				maxPos = 0;
			s[maxPos] = 'X';
			double td = 0.0;
			double tdEnd = 0.0;
			try {
				ex = archive.getExec(unit[i]);
			} catch (SchedulingException err) {
				err.printStackTrace(out);
				System.exit(0);
			}
			for (int j = 0; j < ex.length; ++j) {
				td = ex[j].getStatus().getStartTime().getTimeOfDay();
				tdEnd = ex[j].getStatus().getEndTime().getTimeOfDay();
				while (td <= tdEnd) {
					iex = (int)(td * scaleFactor + 0.5);
					if (iex > (scale - 1))
						iex = 0;
					if (s[iex] == ' ') out.println("Something's wrong here!" + ex[j].getParent());
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
			id = unit[i].getId();
			id = id + blank.substring(id.length());
			out.println(id + " |" + line + "|");
		}
		id = blank;
		out.println(id + " |+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|");
		out.println(id + " |0   2   4   6   8  10  12  14  16  18  20  22   |");
		out.println(id + "                        LST ");
			
	}
	
	private void projectSummary(PrintStream out, DateTime endTime) {
		out.println();
		out.println("Project Summary");
		out.println();
		Project[] prj = null;
		DateTime t = null;
		try {
			prj = archive.getAllProject();
			// Sort the list of projects by scientific priority.
			Project tmp = null;
			int n = prj.length;
			if (n > 2) {
				int j = 0;
				int incr = n / 2;
				while (incr >= 1) {
					for (int i = incr; i < n; i++) {
						tmp = prj[i];
						j = i;
						while (j >= incr && prj[j - incr].getScientificPriority().getPriorityAsInt() < tmp.getScientificPriority().getPriorityAsInt()) {
							prj[j] = prj[j - incr];
							j -= incr;
						}
						prj[j] = tmp;
					}
					incr /= 2;
				}
			}
			for (int i = 0; i < prj.length; ++i) {
				out.println("Project " + prj[i].getObsProjectId() + " priority: " + prj[i].getScientificPriority());
				out.println("\tNumber of scheduling blocks completed  " + prj[i].getNumberSBsCompleted());
				out.println("\tNumber of scheduling blocks incomplete " + 
						(prj[i].getTotalSBs() - prj[i].getNumberSBsCompleted()));
				t = prj[i].getStatus().getStartTime();
				if (t == null || t.isNull())
					out.println("\tStart time: never started");
				else
					out.println("\tStart time: " + t);
				t = prj[i].getStatus().getEndTime();
				if (t == null || t.isNull())
					out.println("\tEnd time:   never ended");
				else
					out.println("\tEnd time:   " + t);				
			}
			
			
		} catch (SchedulingException err) {
			err.printStackTrace();
			System.exit(0);
		}
	}
	
	private void statistics(PrintStream out, DateTime endTime) {
		out.println();
		out.println("Scheduling Statistics");
		out.println();

		double totalTime = 0.0;
		double totalScienceTime = 0.0;
		double totalWeightedScienceTime = 0.0;
		double possibleScienceTime = 0.0;
		double efficiency = 0.0;
		double weightedEfficiency = 0.0;
		double avScore = 0.0;
		double avSuccess = 0.0;
		double avRank = 0.0;
		int totalNumber = 0;
		int totalSBs = 0;
		
		ExecBlock[] ex = null;
		Project[] prj = null;
		try {
			ex = archive.getAllExec();
			prj = archive.getAllProject();
		} catch (SchedulingException err) {
			String msg = "Reporter.error: Error accessing archive! " + err.toString();
			logger.severe(msg);
			out.println(msg);
			return;
		}
		totalNumber = ex.length;
		
		totalTime = DateTime.difference(endTime, beginTime) * 24.0;
		for (int i = 0; i < prj.length; ++i) {
			possibleScienceTime += prj[i].getTotalRequiredTimeInSeconds();
			totalSBs += prj[i].getTotalSBs();
			totalScienceTime += prj[i].getTotalUsedTimeInSeconds();
			totalWeightedScienceTime += prj[i].getTotalUsedTimeInSeconds() * 
						prj[i].getProgram().getScientificPriority().getPriorityAsInt();
		}

		possibleScienceTime /= 3600.0;
		totalScienceTime /= 3600.0;
		totalWeightedScienceTime /= 3600.0;
		efficiency = totalScienceTime / totalTime;
		weightedEfficiency = totalWeightedScienceTime / (totalTime * Priority.HIGHEST.getPriorityAsInt());
		
		BestSB b = null;
		for (int i = 0; i < ex.length; ++i) {
			b = ex[i].getBest();
			avScore += b.getScore()[b.getSelection()];
			avSuccess += b.getSuccess()[b.getSelection()];
			avRank += b.getRank()[b.getSelection()];
		}
		avScore /= totalNumber;
		avSuccess = avSuccess / totalNumber * 100.0;
		avRank /= totalNumber;
		
		dform.setMaximumFractionDigits(2);
		
		out.println("Number of executions             " + totalNumber); 		
				
		out.println();
		out.println("Efficiency (%)                   " + dform.format(efficiency * 100.0));
		out.println("Weighted Efficiency  (%)         " + dform.format(weightedEfficiency * 100.0 ));
		out.println("% of science time executed       " + dform.format((totalScienceTime / possibleScienceTime) * 100.0));

		out.println();
		out.println("Total time (hours)               " + dform.format(totalTime));
		out.println("Total science time (hours)       " + dform.format(totalScienceTime));
		out.println("Possible science time (hours)    " + dform.format(possibleScienceTime));
		out.println("Number of scheduling blocks      " + totalSBs);

		out.println();
		out.println("Average score                    " + dform.format(avScore));
		out.println("Average success factor (%)       " + dform.format(avSuccess));
		out.println("Average rank                     " + dform.format(avRank));

		
		
		
		/*
		out.println();
		out.println("Scheduling Statistics");
		out.println();

		out.println("Number of executions             " + totalNumber); 		

		SB[] u = null;
		Exec[] ex = null;
		
		// All time units are hours.
		double totalTime = DateTime.difference(scheduler.getEndTime(),scheduler.getBeginTime()) * 24.0;
		double totalScienceTime = 0.0;
		double totalWeightedScienceTime = 0.0;
		double possibleScienceTime = 0.0;
		double x = 0.0;
		
		try {
			u = archive.getAllSB();
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
		*/
	}
	
}
