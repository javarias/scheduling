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
 */

package alma.scheduling.psm.cli;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

public class PsmCli {

	/**
	 * @param args
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		System.out.println(	"\n" +
							"Planning Mode Simulator - APRC Tool \n" +
							"Version 0.8.1 \n" + 
							"More info at: http://almasw.hq.eso.org/almasw/bin/view/SCHEDULING/PlanningModeSimulator \n"
				);
		if( args.length == 0 ){
			Console console = Console.getConsole();
			console.help();
		}else if (args[0].compareTo("remote") == 0) {
			System.out.println("Entering Remote mode");
			RMIServer server = new RMIServer();
			server.start();
		} else {
			System.out.println("Entering Local mode");
			Console console = Console.getConsole();
			console.run(args);
			// Wait until HSQLDB thread is finished to do his job
			// This case applies when the simulator is using HSQLDB in File mode
//			if (Thread.activeCount() > 1) {
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
		System.exit(0); // Exit code 0: normal termination
	}
}
