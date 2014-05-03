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

import java.io.File;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl;
import alma.scheduling.psm.sim.InputActions;
import alma.scheduling.psm.sim.Simulator;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.utils.DSAContextFactory;

public class SimulatorCLI {

	private Options opts;
	private String workDir;
	private String policyFile;
	/**
	 * @param args
	 * @throws TransformerFactoryConfigurationError 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		SimulatorCLI cli = new SimulatorCLI();
		cli.initializeOptions();
		cli.parseOptions(args);
		cli.runCompleteSimulation();
	}
	
	@SuppressWarnings("static-access")
	private void initializeOptions() {
		opts = new Options();
		
		Option help = OptionBuilder.withArgName("help")
									.withLongOpt("help")
									.withDescription("Displays this help message.")
									.create('h');
		opts.addOption(help);
		
		Option workDir = OptionBuilder.withArgName("workDir")
									.withLongOpt("work-dir")
									.withDescription("Sets the scheduling working dir. Overrides -D" + ConfigurationDaoImpl.PROP_WORK_DIR
											+ " property and " + ConfigurationDaoImpl.ENV_VARIABLE + " environment variable.")
									.hasArg()
									.create('d');
		opts.addOption(workDir);
		
		Option policyFile = OptionBuilder.withArgName("file")
										.withLongOpt("policy-file")
										.withDescription("Sets the polic file containing DSA configurations")
										.hasArg()
										.isRequired()
										.create('p');
		opts.addOption(policyFile);
		
	}
	
	private void parseOptions(String[] args) {
		CommandLineParser parser = new GnuParser();
		CommandLine cl = null;
		try {
			cl = parser.parse(opts, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (cl.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("schedulingSimulator", opts);
		}
		if (cl.hasOption('d')) {
			if (System.getProperty(ConfigurationDaoImpl.PROP_WORK_DIR) != null) {
				System.out.println("Overriding property -D" + ConfigurationDaoImpl.PROP_WORK_DIR
						+ "=" + System.getProperty(ConfigurationDaoImpl.PROP_WORK_DIR));
			} else {
				if (System.getenv(ConfigurationDaoImpl.ENV_VARIABLE) != null)
					System.out.println("Overriding env variable " + ConfigurationDaoImpl.ENV_VARIABLE
							+ "=" + System.getenv(ConfigurationDaoImpl.ENV_VARIABLE));
			}
			System.out.println("Setting -D" + ConfigurationDaoImpl.PROP_WORK_DIR + "=" + cl.getOptionValue('d'));
			System.setProperty(ConfigurationDaoImpl.PROP_WORK_DIR, cl.getOptionValue('d'));
			
			workDir = System.getProperty(ConfigurationDaoImpl.PROP_WORK_DIR);
		}
		if (cl.hasOption('p')) {
			File test = new File(cl.getOptionValue('p'));
			if(!test.exists() || test.isDirectory() || !test.canRead())
				throw new RuntimeException(cl.getOptionValue('p') + " not usable.");
			policyFile = cl.getOptionValue('p');
		}
	}
	
	private void runCompleteSimulation() throws Exception {
		ApplicationContext ctx = DSAContextFactory.getContextFromPoliciesFile(policyFile);
		InputActions.setApplicationContext(ctx);
		InputActions input = InputActions.getInstance(workDir);
		//load
		
		input.fullLoad(InputActions.IMMUTABLE_DATA_LOADER_BEAN);
		//run
		PsmContext.setApplicationContext(ctx);
		Simulator sim = new Simulator(workDir);
		sim.run("AllSelectors");
	}
}
