package alma.scheduling.archiveupd.external;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import alma.ACSErrTypeCommon.wrappers.AcsJIllegalArgumentEx;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.config.StateSystemContextFactory;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.scheduling.external.CalibratorCheckPluginImpl.ProcessReader;

public class SchedBlockStatusChecker {

	private static StateEngine stateEngine;
	private static StateArchive stateArchive;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static final long TIME_TO_SUSPEND = 30 * 24 * 60 * 60 * 1000; //30 days
	private static final long TIME_TO_CHECK_FOR_CALIBRATIONS = 30 * 60 * 1000; //30 mins
	
	private Logger logger;
	
	public SchedBlockStatusChecker(Logger logger) {
		this.logger = logger;
		synchronized(SchedBlockStatusChecker.class) {
			if (!StateSystemContextFactory.INSTANCE.isInitialized()) {
				StateSystemContextFactory.INSTANCE.init(STATE_SYSTEM_SPRING_CONFIG,
								logger);
				stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
				stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
			}
			else if (stateArchive == null)
				stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
			else if (stateEngine == null)
				stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
		}
	}
	
	public void checkForStatus() {
		logger.info("Starting checking for calibrators");
		checkForCalibrations();
	}
	
	public void forceCheckForStatus() {
		forceCheckForCalibrations();
	}
	
	private void checkForCalibrations() {
		String[] states = {StatusTStateType.CALIBRATORCHECK.toString(), StatusTStateType.READY.toString()};
		Date currentDate = new Date();
		try {
			SBStatus[] statuses = stateArchive.findSBStatusByState(states);
			for(SBStatus s: statuses) {
				logger.fine("Check calibrators for SB: " + s.getSchedBlockRef().getEntityId());
				if(s.getTimeOfUpdate() != null) {
					try {
						Date lastUpdate = dateFormat.parse(s.getTimeOfUpdate());
						//check if the SB has been waiting for calibrations too long 
						if ((s.getStatus().getState().getType() == StatusTStateType.CALIBRATORCHECK_TYPE) && 
								(lastUpdate.getTime() < (currentDate.getTime() - TIME_TO_SUSPEND)) ) {
							stateEngine.changeState(s.getSBStatusEntity(),  StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
						}
						//Check if last update of SBStatus was not too soon
						else if(lastUpdate.getTime() < (currentDate.getTime() - TIME_TO_CHECK_FOR_CALIBRATIONS)) { 
							Process p = Runtime.getRuntime().exec("testCalibratorsCheckScript");
							ProcessReader stdout = new ProcessReader(p.getInputStream(), "SCRIPT OUTPUT");
							stdout.start();
							ProcessReader stderr = new ProcessReader(p.getErrorStream(), "SCRIPT ERROR");
							stderr.start();
							int exitVal = p.waitFor();
							//Check the output of the script
							//If the script fails it should finish with a code distinct than 0, throw an error in this case
							if (exitVal == 0) {
								String outStr = stdout.getOutput().toString();
								int val = Integer.valueOf(outStr.substring(outStr.length() - 2, outStr.length() - 1));
								if (val == 0 && s.getStatus().getState().getType() == StatusTStateType.CALIBRATORCHECK_TYPE) {
										logger.info("Tranistioning SB: " 
												+ s.getSchedBlockRef().getEntityId() + " to " + StatusTStateType.READY);
										stateEngine.changeState(s.getSBStatusEntity(), StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
								}
								else if (val != 0 && s.getStatus().getState().getType() == StatusTStateType.READY_TYPE) {
									logger.info("Tranistioning SB: " 
											+ s.getSchedBlockRef().getEntityId() + " to " + StatusTStateType.CALIBRATORCHECK);
									stateEngine.changeState(s.getSBStatusEntity(), StatusTStateType.CALIBRATORCHECK, Subsystem.SCHEDULING, Role.AOD);
								}
							}
							else {
								logger.severe("Exit code of the script was not 0.\n" + stderr.getOutput().toString());
							}
						} 						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void forceCheckForCalibrations() {
		String[] states = {StatusTStateType.CALIBRATORCHECK.toString(), StatusTStateType.READY.toString()};
		SBStatus[] statuses;
		try {
			statuses = stateArchive.findSBStatusByState(states);
			for (SBStatus s : statuses) {
				Process p = Runtime.getRuntime().exec("testCalibratorsCheckScript");
				ProcessReader stdout = new ProcessReader(p.getInputStream(), "SCRIPT OUTPUT");
				stdout.start();
				ProcessReader stderr = new ProcessReader(p.getErrorStream(), "SCRIPT ERROR");
				stderr.start();
				int exitVal = p.waitFor();
				//Check the output of the script
				//If the script fails it should finish with a code distinct than 0, throw an error in this case
				if (exitVal == 0) {
					String outStr = stdout.getOutput().toString();
					int val = Integer.valueOf(outStr.substring(outStr.length() - 2, outStr.length() - 1));
					if (val == 0 && s.getStatus().getState().getType() == StatusTStateType.CALIBRATORCHECK_TYPE) {
						logger.info("Tranistioning SB: " 
								+ s.getSchedBlockRef().getEntityId() + " to " + StatusTStateType.READY);
						stateEngine.changeState(s.getSBStatusEntity(), StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
					}
					else if (val != 0 && s.getStatus().getState().getType() == StatusTStateType.READY_TYPE) {
						logger.info("Tranistioning SB: " 
								+ s.getSchedBlockRef().getEntityId() + " to " + StatusTStateType.CALIBRATORCHECK);
						stateEngine.changeState(s.getSBStatusEntity(), StatusTStateType.CALIBRATORCHECK, Subsystem.SCHEDULING, Role.AOD);
					}
				}
				else {
					logger.severe("Exit code of the script was not 0.\n" + stderr.getOutput().toString());
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
