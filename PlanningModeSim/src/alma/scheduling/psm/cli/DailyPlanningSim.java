package alma.scheduling.psm.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import net.sf.jasperreports.engine.util.FileBufferedWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import alma.scheduling.Array;
import alma.scheduling.ArrayHelper;
import alma.scheduling.SchedBlockQueueItem;
import alma.scheduling.algorithm.DynamicSchedulingAlgorithm;
import alma.scheduling.algorithm.sbranking.SBRank;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ArrayConfigurationLiteReader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.psm.util.SimpleClient;
import alma.scheduling.utils.DSAContextFactory;

public class DailyPlanningSim {

	private Options options;
	private TreeMap<Date, SchedBlock> observationList;
	private TreeMap<Date, List<SBRank>> resultsList;
	private ApplicationContext context;
	private String arrayName;
	private boolean answerYesToQuestions;
	private int selectedArrayConfig;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private static final long OBSERVING_TIME_MS = 80 * 60 * 1000; 
	
	public DailyPlanningSim() {
		initializeOptions();
		observationList = new TreeMap<Date, SchedBlock>();
		resultsList = new TreeMap<Date, List<SBRank>>();
		answerYesToQuestions = false;
	}

	private void initializeOptions() {
		options = new Options();
		Option startDate = OptionBuilder.withArgName("yyyy-mm-ddThh:MM:ss").withLongOpt("start-date").
				hasArg().withDescription("Sets the start date (UTC) of the simulation. e.g. 2013-02-20T19:00:00")
				.isRequired().create("s");
		Option endDate = OptionBuilder.withArgName("yyyy-mm-ddThh:MM:ss").withLongOpt("end-date").
				hasArg().withDescription("Sets the end date (UTC) of the simulation. e.g. 2013-02-30T13:00:00")
				.isRequired().create("e");
		options.addOption(startDate);
		options.addOption(endDate);
		Option arrayConfigFile = OptionBuilder.withArgName("file").withLongOpt("array-config-file").
				hasArg().withDescription("Sets the array configuration to be used")
				.isRequired().create("f");
		options.addOption(arrayConfigFile);
		Option dsaPolicy = OptionBuilder.withArgName("policy").withLongOpt("dsa-policy").
				hasArg().withDescription("Sets the DSA policy to run the selection algorithm")
				.isRequired().create("p");
		options.addOption(dsaPolicy);
		Option arrayNameOp = OptionBuilder.withArgName("array name").withLongOpt("array-name").
				hasArg().withDescription("Uses the given array name to queue results and other operations. e.g Array001")
				.isRequired().create("a");
		options.addOption(arrayNameOp);
//		Option propertiesFile = OptionBuilder.withArgName( "alma.scheduling.properties=path" )
//                .hasArgs(2)
//                .withValueSeparator()
//                .withDescription( "Sets the scheduling.properties file to use. By default is used the properties file located at '$ACSDATA/config/'" )
//                .create( "D" );
//		options.addOption(propertiesFile);
		Option dsaPolicyFile = OptionBuilder.withArgName("file").withLongOpt("dsa-policy-file").
				hasArg().withDescription("Sets the DSA policy file to be used within the simulator. By default is used the file defined in the $ACSDATA/config/scheduling.properties file")
				.create("d");
		options.addOption(dsaPolicyFile);
		Option showHelp = OptionBuilder.withLongOpt("help")
				.withDescription("Shows this help")
				.create("h");
		options.addOption(showHelp);
		Option yes = OptionBuilder.withLongOpt("yes")
				.withDescription("Answers yes to all the questions")
				.create("y");
		options.addOption(yes);
		Option configToUse = OptionBuilder.withArgName("number").withLongOpt("array-config").hasArg().
				withDescription("The array configuration read from array-config-file to use in the simulation. By default will use the first one." +
						"To select another use this option with the number of the configuration starting from 0. e.g. 1st - 0, 2nd - 1 and so on.").
						create("c");
		options.addOption(configToUse);
		
	}
	
	public void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("schedulingDailyPlanningSim", options, true);
	}
	
	public Options getOptions() {
		return options;
	}
	
	
	public void runSimulation(Date startTime, Date endTime, ArrayConfiguration arrayConfiguration, String policyToUse, String dsaPoliciesFile) {
		System.out.println("Initializing DSA Context using dsa policies file: " + dsaPoliciesFile);
		context = DSAContextFactory.getContextFromPoliciesFile(dsaPoliciesFile);
		runSimulation(startTime, endTime, arrayConfiguration, policyToUse);
	}
	
	public void runSimulation(Date startTime, Date endTime, ArrayConfiguration arrayConfiguration, String policyToUse) {
		if (context == null)
			context = DSAContextFactory.getContextFromPropertyFile();
		System.out.println("Using DSA Policy: " + policyToUse);
		DynamicSchedulingAlgorithm dsa = null;
		try {
			dsa = (DynamicSchedulingAlgorithm) context.getBean(policyToUse); 
		} catch (NoSuchBeanDefinitionException e) {
			System.err.println("No policy named: " + policyToUse + " is defined");
			e.printStackTrace();
			System.exit(-1);
		}
		Date currentSimTime = startTime;
		
		dsa.setArray(arrayConfiguration);
		dsa.initialize(startTime);
		while (currentSimTime.getTime() <= (endTime.getTime() - OBSERVING_TIME_MS)) {
			currentSimTime = step(dsa, currentSimTime);
		}
		
		printResultstoScreen();

		String answer = null;
		boolean done = false;
		
		while (!done) {
			answer = getAnswerForQuestion("Would you like to queue the results into " + arrayName + "? (y/N)", answerYesToQuestions);
			if (answer.equals("") || answer.equals("n") || answer.equals("N")) {
				done = true;//Do nothing
			} else if (answer.equals("y") || answer.equals("Y")) {
				done = true;
				System.out.println("Queueing SchedBlocks...");
				try {
					queueResultsIntoArray(arrayName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		writeResultsToFiles();
	}
	
	private Date step(DynamicSchedulingAlgorithm dsa, Date selectionTime) {
		List<SBRank> results = null;
		SchedBlock sb = null;
		try {
			dsa.selectCandidateSB(selectionTime);
			dsa.updateCandidateSB(selectionTime);
			results = dsa.rankSchedBlocks(selectionTime);
			results = sortScores(results);
			sb = dsa.getSelectedSchedBlock();
		} catch (NoSbSelectedException e) {
			System.out.println("No suitable SchedBlocks have been found for time: " + dateFormat.format(selectionTime));
			e.printStackTrace();
		}
		
		if (results != null && sb != null) {
			resultsList.put(selectionTime, results);
			observationList.put(selectionTime, sb);
			return new Date(selectionTime.getTime() + OBSERVING_TIME_MS);
		}
		
		observationList.put(selectionTime, sb);
		
		return new Date(selectionTime.getTime() + OBSERVING_TIME_MS);
	}
	
	private void printResultstoScreen() {
		ObsProjectDao prjDao = (ObsProjectDao) context.getBean(DSAContextFactory.SCHEDULING_OBSPROJECT_DAO_BEAN);
		System.out.println("-==Results==-");
		System.out.println("Observation Time (UTC)\tSchedBlock UID\t\tProject Code\t\tProject grade");
		for (Date d: observationList.keySet()) {
			SchedBlock sb = observationList.get(d);
			ObsProject prj = prjDao.findByEntityId(sb.getProjectUid());
			System.out.println(
					String.format("%s\t%s\t%s\t%s", dateFormat.format(d), sb.getUid(), prj.getCode(), sb.getLetterGrade()));
		}
	}
	
	private void queueResultsIntoArray(String arrayName) throws Exception {
		String fixedArrayName = arrayName.toLowerCase();
		fixedArrayName = fixedArrayName.substring(0, 1).toUpperCase() + fixedArrayName.substring(1, fixedArrayName.length());
		String componentName = "SCHEDULING/" + fixedArrayName;
		
		SimpleClient acsClient = SimpleClient.getInstance();
		org.omg.CORBA.Object o = acsClient.getContainerServices().getComponentNonSticky(componentName);
		Array arrayComponent = ArrayHelper.narrow(o);
		for (Date d: observationList.keySet()) {
			if (observationList.get(d) == null) {
				continue;
			}
			System.out.print("Putting in the queue SchedBlock: " + observationList.get(d).getUid() + "... ");
			SchedBlockQueueItem item = new SchedBlockQueueItem(System.currentTimeMillis(), observationList.get(d).getUid());
			arrayComponent.push(item);
			System.out.println("DONE");
		}
	}
	
	private String getAnswerForQuestion (String question, boolean automaticYesAnswer) {
		System.out.print(question + " ");
		String answer = null;
		if (automaticYesAnswer) {
			answer = "y";
			System.out.println(answer);
			return answer;
		}
		
		BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			answer = stdinReader.readLine();
		} catch (IOException e) {
			//This should not happen never
			e.printStackTrace();
		}
		return answer;
	}
	
	private void writeResultsToFiles() {
		String currentPath = null;
		try {
			currentPath = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			//The current path cannot be read nothin to do
			e.printStackTrace();
		}
		
		String answer = getAnswerForQuestion(
				"Would you like to save the results to " + currentPath + "? (Y/n)" , this.answerYesToQuestions );
		boolean done = false;
		while (!done) {
			if (answer.compareToIgnoreCase("n") == 0)
				done = true;
			else if (answer.compareToIgnoreCase("y") == 0 || answer.equals("")) {
				System.out.println("Wrting to files...");
				ObsProjectDao prjDao = (ObsProjectDao) context.getBean(DSAContextFactory.SCHEDULING_OBSPROJECT_DAO_BEAN);
				done = true;
				try {
					if (observationList.size() == 0 ) {
						//No results, nothing to do here
						return;
					}
					System.out.print("summary.csv ...");
					BufferedWriter summary = new BufferedWriter(new FileWriter("summary.csv"));
					summary.write("\"time\",\"sb uid\",\"project code\",\"project grade\"\n");
					for (Date d: observationList.keySet()) {
						SchedBlock sb = observationList.get(d);
						ObsProject prj = prjDao.findByEntityId(sb.getProjectUid());
						String line = String.format("%s,%s,%s,%s\n", dateFormat.format(d), sb.getUid(), prj.getCode(), sb.getLetterGrade());
						summary.write(line);
					}
					summary.close();
					System.out.println("DONE.");
					if (resultsList.size() == 0) {
						//No results, nothing to so here
						return;
					}
					System.out.print("details.csv ...");
					BufferedWriter details = new BufferedWriter(new FileWriter("details.csv"));
					String line = "\"time\",\"sb uid\",\"project code\",\"position\"";
					for (SBRank scorer: resultsList.firstEntry().getValue().get(0).getBreakdownScore())
						line += ",\"" + scorer.getDetails() + "\"";
					line += ",\"final score\"\n";
					details.write(line);
					for (Date d: resultsList.keySet()) {
						int position = 0;
						for (SBRank r: resultsList.get(d)) {
							SchedBlock sb = observationList.get(d);
							ObsProject prj = prjDao.findByEntityId(sb.getProjectUid());
							line = String.format("%s,%s,%s,%d", dateFormat.format(d), r.getUid(), prj.getCode(), (position + 1) );
							for (SBRank scorer: r.getBreakdownScore())
								line += "," + scorer.getRank();
							line += "," + r.getRank() + "\n";
							details.write(line);
							position++;
						}
					}
					details.close();
					System.out.println("DONE.");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	private List<SBRank> sortScores(List<SBRank> scores) {
		if (scores.size() == 0)
			return scores;
		Collections.sort(scores);
		ArrayList<SBRank> descSortedList =  new ArrayList<SBRank>(scores.size());
		for (int i = scores.size() - 1; i >= 0; i--){
			descSortedList.add(scores.get(i));
		}
		return descSortedList;
	}
	
	public static void main(String[] args) {
		System.out.println("1-Day Planning Simulator. Version 1.0");
		DailyPlanningSim sim = new DailyPlanningSim();
		CommandLineParser parser = new GnuParser();
		CommandLine cl = null;
		Date startSim = null, endSim = null;
		try {
			cl = parser.parse(sim.options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			sim.help();
			System.exit(1);
		}
		if (cl.hasOption('h')) {
			sim.help();
			System.exit(0);
		}
		try {
			startSim = dateFormat.parse(cl.getOptionValue('s'));
		} catch (java.text.ParseException e) {
			System.err.println("Trying to parse parameter 's': " + e.getMessage());
			sim.help();
			System.exit(1);
		}
		try {
			endSim = dateFormat.parse(cl.getOptionValue('e'));
		} catch (java.text.ParseException e) {
			System.err.println("Trying to parse parameter 'e': " + e.getMessage());
			sim.help();
			System.exit(1);
		}
		File arrayConfigFile = new File(cl.getOptionValue('f'));
		if (!arrayConfigFile.exists()){
			System.err.println("Array Configuration file does not exist. File: " + cl.getOptionValue('f'));
			sim.help();
			System.exit(1);
		}
		String policyName = cl.getOptionValue('p');
		sim.arrayName = cl.getOptionValue('a');
		String policyFile = null;
		if (cl.hasOption('d'))
			policyFile = cl.getOptionValue('d');
		if(cl.hasOption('y'))
			sim.answerYesToQuestions = true;
		if(cl.hasOption('c'))
			sim.selectedArrayConfig = Integer.valueOf(cl.getOptionValue('c'));
		
		ArrayConfigurationLiteReader arrConfReader = null;
		List<ArrayConfiguration> arrayConfigList = null;
		try {
			arrConfReader = new ArrayConfigurationLiteReader(new FileReader(arrayConfigFile));
			arrayConfigList = arrConfReader.getArrayConfiguration();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} finally {
			if (arrConfReader != null)
				try {
					arrConfReader.close();
				} catch (IOException e) {
				}
		}
		
		if (sim.selectedArrayConfig > (arrayConfigList.size() - 1)) {
			System.err.println("Selected invalid array configuration. There are only " + arrayConfigList.size() + " configurations available");
			System.err.println("Array Config file path: " + arrayConfigFile.getAbsolutePath());
			System.exit(1);
		}
		
		if (policyFile != null)
			sim.runSimulation(startSim, endSim, arrayConfigList.get(sim.selectedArrayConfig), policyName, policyFile);
		else
			sim.runSimulation(startSim, endSim, arrayConfigList.get(sim.selectedArrayConfig), policyName);
	}
}
