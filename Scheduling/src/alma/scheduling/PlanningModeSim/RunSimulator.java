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
 * File RunSimulator.java
 */

package alma.scheduling.PlanningModeSim;
import alma.scheduling.PlanningModeSim.Simulator;
import alma.scheduling.PlanningModeSim.Define.SimulationException;

/**
 * The RunSimulator class runs the simulator.  It takes four parameters.
 * <ul>
 * <li> The full path name of the directory in which the input file is located
 * <li>	The name of the input properties file
 * <li>	The name of the output file 
 * <li>	The name of the generated logfile
 * </ul> 
 * 
 * @version 2.00  April 19, 2004
 * @author Allen Farris
 */
public class RunSimulator {
	private static void error(String message) {
		System.out.println(message);
		System.out.println("Syntax: RunSimulator parm1 parm2 parm3 parm4");
		System.out.println("    parm1: The full path name of the directory in which the input file is located");
		System.out.println("    parm2: The name of the input properties file");
		System.out.println("    parm3: The name of the output file");
		System.out.println("    parm4: The name of the generated logfile");
		System.exit(0);
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			error("Improper number of parameters.");
		}
		Simulator sim = new Simulator ();
		try {
			sim.initialize(args[0],args[1],args[2],args[3]);
		} catch (SimulationException e) {
			e.printStackTrace(System.out);
			System.exit(0);
		}
		Thread t = new Thread (sim);
		t.start();
		
	}
}
