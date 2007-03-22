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
 
package alma.scheduling.PlanningModeSim;

import alma.scheduling.PlanningModeSim.Define.BasicComponent;
import alma.scheduling.PlanningModeSim.Define.SimulationException;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Date;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.BestSB;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.SiteCharacteristics;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.NothingCanBeScheduled;
import alma.scheduling.Define.Priority;

import java.util.logging.Level;
import java.util.HashMap;
import java.io.*;
//import java.io.FileOutputStream;
//import java.io.IOException;
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
	private PrintStream graph;
	private PrintStream execStatsOut;
	
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

	private DateTime currentDay = null; // This is the current day in the simulation.
	
	public void execStart(String arrayname, String sbId, DateTime time) {
	}
	
	public void execEnd(String arrayname, String sbId, String execId, DateTime time) throws SchedulingException {
		ExecBlock ex = archive.getExec(execId);
		SB sb = archive.getSB(sbId);
		DateTime startTime = ex.getStatus().getStartTime();
		DateTime endTime = ex.getStatus().getEndTime();
		BestSB best = ex.getBest();

		if (currentDay == null) {
			currentDay = new DateTime(startTime.getDate(),0.0);
		    out.println("Current DAY: "+currentDay + " LST");				
			out.println();
			out.println("SB-id  Exec# Array    Start       End        Score  Success  Rank");
		} else {
			DateTime y = DateTime.add(currentDay,86400);
			if (startTime.gt(y)) {
				out.println();
				currentDay = new DateTime(startTime.getDate(),0.0);
				out.println("Current DAY: "+currentDay + " LST");				
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
		ExecBlock[] n = sb.getExec();
		for (int i = 0; i < n.length; ++i) {
			if (n[i].getId().equals(ex.getId())) {
				executionNumber = i;
				break;
			}
		}
		out.println(id +  
				executionNumber + "    " + 
				arrayname + "     " +
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
            execStatsOut = new PrintStream(new FileOutputStream(input.getStatsFile()));
		} catch (IOException ioerr) {
			 error("Could not open file " + input.getOutFile().getAbsolutePath() + " -- " + ioerr.toString());
		}
		writeBeginning();
	}
	
	private void writeBeginning() {
		// Write the initial data in the output file. Place this in Reporter.
		SiteCharacteristics site = input.getSite();
		out.println("ALMA Simulator Release R3.1 - 2006");
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
		showSummary(out,endTime);
		projectSummary(out,endTime);
		statistics(out,endTime);
        detailedExecutionStatistics(execStatsOut);
        runAnalysisScripts();
	}
	
	private void showSummary(PrintStream o, DateTime endTime) {
		
		o.println();
		o.println("Sources - Visibility and Executions");
		o.println("\t(\'-\' is visible   \'+\' is visible and was executed");
		o.println("\t(\'X\' max elevation   \'*\' max elevation and was executed)");
		o.println();
		//double lst = 0.0;
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
			err.printStackTrace(o);
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
				err.printStackTrace(o);
				System.exit(0);
			}
            //o.println(unit[i].getId() +" has "+ ex.length+" execution(s)");
			for (int j = 0; j < ex.length; ++j) {
            //    o.println(ex[j].getParent());
				td = ex[j].getStatus().getStartTime().getTimeOfDay();
				tdEnd = ex[j].getStatus().getEndTime().getTimeOfDay();
				while (td <= tdEnd) {
                    String tmp =new String(s);
                    //o.println("@@"+tmp+"@@"+tmp.length());
					iex = (int)(td * scaleFactor + 0.5);
					if (iex > (scale - 1))
						iex = 0;
					if (s[iex] == ' '){
                        o.println("Something's wrong here!" + ex[j].getParent());
                        //o.println(iex+" : "+j+" : "+td+" : "+tdEnd);
                        break;
                    }
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
			o.println(id + " |" + line + "|");
		}
		id = blank;
		o.println(id + " |+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|");
		o.println(id + " |0   2   4   6   8  10  12  14  16  18  20  22   |");
		o.println(id + "                        LST ");
			
	}

    private void detailedScheduleSummary(PrintStream o, DateTime endTime){
		String blank = "               ";//15 blank spaces
		final int scale = 71;
		final double scaleFactor = 2.0;
        Date startDay = input.getBeginTime().getDate();
        Date endDay = input.getEndTime().getDate();
		String id = null;
		ExecBlock[] ex = null;
		SB[] unit = null; // These units are really scheduling units.
        //double td = 0.0;
        //double tdEnd = 0.0;
        Date ex_start=null;
        String line=null;
		try {
			unit = archive.getAllSB();
		} catch (SchedulingException err) {
			err.printStackTrace(o);
			System.exit(0);
		}
        HashMap<Date, String> graphLines =new HashMap();
		for (int i = 0; i < unit.length; ++i) {
			try {
				ex = archive.getExec(unit[i]);
			} catch (SchedulingException err) {
				err.printStackTrace(o);
				System.exit(0);
			}
            for(int j=0;j < ex.length; ++j) {
                ex_start = ex[j].getStatus().getStartTime().getDate();
                if( (line = graphLines.get(ex_start)) != null){
                    line = "additional line stuff";
                   // graphLines.add(ex_start, line);
                }else {//add a new line
                    line = new String("new line stuff");
                   // graphLines.add(ex_start, line);
                }
            }
            id = unit[i].getId();
            id = id + blank.substring(id.length());
        }
        id = blank;
		o.println(id + 
            "|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|");
		o.println(id + 
            "0     2     4     6     8     10    12    14    16    18    20    22    24");
		o.println(id + 
            "                                    LST ");
    }

    private int totalPossibleExecutions=0;
	private void projectSummary(PrintStream o, DateTime endTime) {
		o.println();
		o.println("Project Summary");
		o.println();
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
				o.println("Project " + prj[i].getObsProjectId() + " priority: " + prj[i].getScientificPriority());
                SB[] tmpsbs = prj[i].getAllSBs();
                o.println("\tNumber of SBs in project "+tmpsbs.length);
				o.println("\tNumber of scheduling blocks completed  " + prj[i].getNumberSBsCompleted());
                int totalReps=0;
                int numExec=0;
                for(int j=0; j< tmpsbs.length; j++){
                    totalReps += tmpsbs[j].getMaximumNumberOfExecutions();
                    numExec += tmpsbs[j].getNumberExec();
                }
                totalPossibleExecutions += totalReps + tmpsbs.length;
				o.println("\tNumber of possible repeats: " + totalReps+"; Total number of executions: "+numExec);
				o.println("\tNumber of scheduling blocks incomplete " + 
						(prj[i].getTotalSBs() - prj[i].getNumberSBsCompleted()));
				t = prj[i].getStatus().getStartTime();
				if (t == null || t.isNull())
					o.println("\tStart time: never started");
				else
					o.println("\tStart time: " + t);
				t = prj[i].getStatus().getEndTime();
				if (t == null || t.isNull())
					o.println("\tEnd time:   never ended");
				else
					o.println("\tEnd time:   " + t);				
			}
			
			
		} catch (SchedulingException err) {
			err.printStackTrace(System.out);
			System.exit(0);
		}
	}
	
	private void statistics(PrintStream o, DateTime endTime) {
		o.println();
		o.println("Scheduling Statistics");
		o.println();

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
		int totalPossibleNumber = 0;
		int totalSBs = 0;
		
		ExecBlock[] ex = null;
		Project[] prj = null;
        SB[] allSBs=null;
		try {
			ex = archive.getAllExec();
            System.out.println("SCHED: in reporter, all execs = "+ex.length);
			prj = archive.getAllProject();
            System.out.println("SCHED: in reporter, all projs ="+prj.length);
		} catch (SchedulingException err) {
			String msg = "Reporter.error: Error accessing archive! " + err.toString();
			logger.severe(msg);
			o.println(msg);
			return;
		}
		totalNumber = ex.length;
		
		totalTime = DateTime.difference(endTime, beginTime) * 24.0;
		for (int i = 0; i < prj.length; ++i) {
			possibleScienceTime += prj[i].getTotalRequiredTimeInSeconds();
			totalSBs += prj[i].getTotalSBs();
            allSBs = prj[i].getAllSBs();
            for(int x=0;x<allSBs.length; x++){
		        totalPossibleNumber += allSBs[x].getMaximumNumberOfExecutions();
            }
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
        totalPossibleNumber += totalSBs; //add # of sbs coz # of repeats is in addition to first exec.
		
		dform.setMaximumFractionDigits(2);
		
		o.println("Number of executions             " + totalNumber); 		
		o.println("Number of possible executions    " + totalPossibleNumber); 		
		o.println("Number of possible executions #2 " + totalPossibleExecutions); 		
				
		o.println();
		o.println("Efficiency (%)                   " + dform.format(efficiency * 100.0));
		o.println("Weighted Efficiency  (%)         " + dform.format(weightedEfficiency * 100.0 ));
		o.println("% of science time executed       " + dform.format((totalScienceTime / possibleScienceTime) * 100.0));

		o.println();
		o.println("Total time (hours)               " + dform.format(totalTime));
		o.println("Total science time (hours)       " + dform.format(totalScienceTime));
		o.println("Possible science time (hours)    " + dform.format(possibleScienceTime));
		o.println("Number of scheduling blocks      " + totalSBs);

		o.println();
		o.println("Average score                    " + dform.format(avScore));
		o.println("Average success factor (%)       " + dform.format(avSuccess));
		o.println("Average rank                     " + dform.format(avRank));
        o.println();
		
	}

    
    private void detailedExecutionStatistics(PrintStream o) {
        ExecutionStatistics[] stats;
        try {
            stats = archive.getAllExecutionStatistics();
            if (stats.length > 0) {
                o.println(stats[0].getColumnsInfoString());
                //o.println(stats[0].getCurrentWeatherColumnInfoString());
                for(int i=0; i < stats.length; i++){
                    o.println(stats[i].toString());
                    //o.println(stats[i].getCurrentWeatherInfo());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    private void runAnalysisScripts(){
        String cmd1 = "ALMASched_lst_vs_day";
        String cmd2 = "ALMASchedSim_antennaLocation";
        String stats = input.getStatsFile().getAbsolutePath();
        String inputfilename = input.getInputFile().getAbsolutePath();
        String outputfilename = inputfilename.substring(0, (inputfilename.length() -4))+"_graph";
        String scheduleCmdString = cmd1 +" "+ stats +" "+ outputfilename +" "+ inputfilename;
        String antennaPlotCmdString = cmd2 +" "+ inputfilename;
        System.out.println(scheduleCmdString);
        System.out.println(inputfilename);
        Process p;
        try{
            p = Runtime.getRuntime().exec(scheduleCmdString);
            p = Runtime.getRuntime().exec(antennaPlotCmdString);
        } catch(Exception e){
            e.printStackTrace();
            logger.warning("Error writing analysis files");
        }
    }

    public String getOutputFilename(){
        return input.getOutFile().getName(); 
    }
	
    public String getScheduleGraphFilename(){
        String inputfile = input.getInputFile().getAbsolutePath();
    //    try {
      //  System.out.println("1"+input.getInputFile().getCanonicalPath());
        //System.out.println("2"+input.getInputFile().getParent());
  //      System.out.println("3"+input.getInputFile().getPath());
//        System.out.println("4"+input.getInputFile().getParentFile().getCanonicalPath());
    //    } catch(Exception e){}
        return inputfile.substring( 0, (inputfile.length() -4))+"_graph.gif";
    }

    public String getAntennaConfigFilename(){
        String f="";
        try{
            f= input.getInputFile().getParentFile().getCanonicalPath()+
                File.separator+"antenna_positions.gif";
        }catch(Exception e){}
        return f;
    }
}
