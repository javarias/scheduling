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
 * File TestProjectUtil.java
 */
package alma.scheduling.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.scheduling.AlmaScheduling.ProjectUtil;
import alma.scheduling.AlmaScheduling.StatusEntityQueueBundle;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Equatorial;
import alma.scheduling.Define.Priority;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.Target;


/**
 * A test of the ProjectUtil class.
 * 
 * @version 1.00 Mar 15, 2004
 * @author Allen Farris
 */
public class TestProjectUtil {
	private Logger logger=null;
		
	private static void doTree(PrintStream out, Project prj, String message) {
		System.out.println(message);
		System.out.println("===================================================");
		System.out.println("The lite version of the project tree");
		prj.printTreeLite(out,"");
		System.out.println("");
		System.out.println("The complete version of the project tree");
		prj.printTree(out,"");
	}
	private static void doLiteTree(PrintStream out, Project prj, String message) {
		out.println(message);
		out.println("===================================================");
		out.println("The lite version of the project tree");
		prj.printTreeLite(out,"");
		out.println("");
	}
	
	static int entityIdCount = 0;
	static int partIdCount = 0;
	static final String zeros = "0000000000000000";
	static private String genEntityId() { // uid://X0000000000000079/X00000000
		++entityIdCount;
		String n = Integer.toString(entityIdCount);
		return "uid://X" + zeros.substring(0,16 - n.length()) + n + "/X00000000";
	}
	static private String genPartId() { // X00000001
		++partIdCount;
		String n = Integer.toString(partIdCount);
		return "X" + zeros.substring(0,8 - n.length()) + n;
	}
	
	public static Project createProject() {
		
		Project prj = new Project (genEntityId(),genEntityId(),"TestProject","v01","Allen Farris", "Continuum", "TWELVE-M", Logger.getLogger("TestProjectUtil"));
		prj.setProjectStatusId(genEntityId());
		Program program = new Program (genPartId(), genEntityId());
		prj.setProgram(program);
		program.setScientificPriority(Priority.HIGH);
		program.setUserPriority(Priority.HIGH);
		program.setOUSStatusId(genPartId());
		
		// Use the VLA as the default clock.
		DateTime.setClockCoordinates(107.6177275,34.0787491666667,-6);
		
		ArrayList sbList = new ArrayList ();
		Program[] set = new Program [3];
		for (int i = 0; i < set.length; ++i) {
			set[i] = new Program (genPartId(), genEntityId());
			set[i].setProject(prj);
			set[i].setParent(program);
			set[i].setOUSStatusId(genPartId());
			program.addMember(set[i]);
			Program[] subset = new Program [2];
			for (int j = 0; j < subset.length; ++j) {
				subset[j] = new Program (genPartId(), genEntityId());
				subset[j].setProject(prj);
				subset[j].setParent(set[i]);
				subset[j].setOUSStatusId(genPartId());
				set[i].addMember(subset[j]);
				SB[] unit = new SB [3];
				for (int k = 0; k < unit.length; ++k) {
					unit[k] = new SB (genEntityId());
					unit[k].setParent(subset[j]);
					unit[k].setImagingScript("myImagingScript");
					unit[k].setObservingScript("myObervingScript");
					unit[k].setMaximumTimeInSeconds(1800);
					unit[k].setSbStatusId(genPartId());
					Equatorial coord = new Equatorial (9 + i + j + k, 53, 0.0, 54 + i + j + k, 54, 0.0);
					Target t = new Target(coord,0.1,0.2);
					unit[k].setTarget(t);
					subset[j].addMember(unit[k]);
					sbList.add(unit[k]);
				}
			}
		}
		
		prj.setTimeOfCreation(new DateTime(2004,4,6,1,0,0));
		prj.setTimeOfUpdate(prj.getTimeOfCreation());
		prj.setMemberLink();
		DateTime time = new DateTime(2004,4,6,11,0,0);
		prj.setReady(time);
		
		return prj;
	}
	
    /*
	public static Session executeSB(Project project) {
		SB[] sbList = project.getAllSBs();
		SB x = sbList[0];
		DateTime beg = new DateTime(2004,4,6,15,0,0);
		x.setStartTime(beg);
		ExecBlock ex = new ExecBlock (genEntityId(),1);
		ex.setParent(x);
		ex.setStartTime(beg);
		ex.setExecStatusId(genPartId());
		ex.setProject(project);
		ex.setTimeOfCreation(beg);
		ex.setTimeOfUpdate(beg);
		String[] sa = new String [1];
		sa[0] = x.getId();
		String[] sb = new String [1];
		sb[0] = "scorestring";
		double da[] = new double [1];
		da[0] = 90.0;
		double db[] = new double [1];
		db[0] = 90.0;
		double dc[] = new double [1];
		dc[0] = 100.0;
		BestSB best = new BestSB (sa,sb,da,db,dc,beg);
		ex.setBest(best);
		x.setRunning();
		DateTime end = DateTime.add(beg,1800);
		ex.setEndTime(end,Status.COMPLETE);
		x.execEnd(ex,end,Status.COMPLETE);
		// Create a session object
		ObservedSession session = new ObservedSession();
		session.setSessionId(genEntityId());
		session.setProgram(x.getParent());
		session.setStartTime(beg);
		session.setEndTime(end);
		session.addExec(ex);
		// Add the session to the Program.
		x.getParent().addObservedSession(session);
		
		Session s =  ProjectUtil.createSession(session);
				//genEntityId(),project.getProjectStatusId(),
		//		x.getParent().getObsUnitSetStatusId(),
		//		beg,end,ex.getExecStatusId());
		//x.getParent().
		return s;
	}
    */
	
	public static void main(String[] arg) {
//		System.out.println("Test of Project");
//        /*
//		PrintStream out = null;
//		try {
//			//out = new java.io.PrintStream (new java.io.FileOutputStream (new java.io.File (outDir,"out.txt")));
//			out = new java.io.PrintStream (new java.io.FileOutputStream (new java.io.File ("out.txt")));
//		} catch (Exception err) {
//			err.printStackTrace();
//			System.exit(0);
//		}*/
//		//logger = Logger.getLogger("TestProjectUtil");
//		Project prj = createProject();
//		doTree(System.out, prj, "The initial version of the tree.");
//		
//		/*
//		try {
//			System.out.println("Creating ProjectStatus ...");
//			PrintWriter xmlOut = new PrintWriter(new BufferedWriter(
//					new FileWriter(new java.io.File (outDir,"xmlOut1.xml"))));
//			ProjectStatus pStatus = ProjectUtil.map(prj,new DateTime(2004,4,6,12,0,0));
//			pStatus.marshal(xmlOut);
//			System.out.println("...complete");
//		} catch (SchedulingException err) {
//			err.printStackTrace();
//			return;
//		} catch (IOException err) {
//			err.printStackTrace();
//			return;
//		} catch (Exception err) {
//			err.printStackTrace();
//			return;
//		}
//		*/
//		
//		// Now simulate the execution of one SB.
//		//Session session = executeSB(prj);
//		doTree(System.out,prj,"The version after ending a SB.");
//		
//		try {
//			System.out.println("Creating ProjectStatus ...");
//			Logger logger = Logger.getLogger("TestProjectUtil");
//			AcsLogger aLogger = null;
//			PrintWriter xmlOut = new PrintWriter(new BufferedWriter(
//					new FileWriter(new java.io.File ("xmlProjectStatus-1.xml"))));
//					//new FileWriter(new java.io.File (outDir,"xmlProjectStatus-1.xml"))));
//			ProjectStatus pStatus = new ProjectUtil(logger, new StatusEntityQueueBundle(aLogger)).map(prj,new DateTime(2004,4,6,12,0,0));
//			//pStatus.marshal(xmlOut);
//			System.out.println("...complete");
//			
//			//System.out.println("Creating Session ...");
//            /*
//			xmlOut = new PrintWriter(new BufferedWriter(
//					new FileWriter(new java.io.File ("xmlSession-1.xml"))));
//					//new FileWriter(new java.io.File (outDir,"xmlSession-1.xml"))));
//			//session.marshal(xmlOut);
//			System.out.println("...complete");
//			*/
//		} catch (SchedulingException err) {
//			err.printStackTrace();
//			return;
//		} catch (IOException err) {
//			err.printStackTrace();
//			return;
//		} catch (Exception err) {
//			err.printStackTrace();
//			return;
//		}
//
//		/*<---
//		// "Start" an SB.	
//		SB[] sbList = prj.getAllSBs();
//		SB x = sbList[0];
//		DateTime now = new DateTime(2004,4,6,15,0,0);
//		x.setStartTime(now);
//		ExecBlock ex = new ExecBlock ("001",1);
//		ex.setParent(x);
//		ex.setStartTime(now);
//		doTree(out,prj,"The version after starting a SB.");
//
//		now.add(1800);
//		ex.setEndTime(now,Status.COMPLETE);
//		x.execEnd(ex,now,Status.COMPLETE);
//		doTree(out,prj,"The version after ending a SB.");
//
//		// Now, mark all the SBs complete.
//		for (int i = 1; i < sbList.length; ++i) {
//			now.add(60);
//			x = (SB)sbList[i];
//			x.setStartTime(now);
//			ex = new ExecBlock ("00" + Integer.toString(i + 1),1);
//			ex.setParent(x);
//			ex.setStartTime(now);
//			doLiteTree(out,prj,"Starting SB.");
//			now.add(1800);
//			ex.setEndTime(now,Status.COMPLETE);
//			x.execEnd(ex,now,Status.COMPLETE);
//			doLiteTree(out,prj,"Ending SB.");
//		}
//		doTree(out,prj,"The version after all SBs are complete.");
//		--->*/
//		
//		System.out.println("End test of Project");	
//				
	}

	
}
