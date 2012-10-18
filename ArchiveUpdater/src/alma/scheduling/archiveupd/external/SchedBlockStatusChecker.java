package alma.scheduling.archiveupd.external;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;

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
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;
import alma.statearchiveexceptions.wrappers.AcsJNoSuchEntityEx;
import alma.stateengineexceptions.wrappers.AcsJNoSuchTransitionEx;
import alma.stateengineexceptions.wrappers.AcsJNotAuthorizedEx;
import alma.stateengineexceptions.wrappers.AcsJPostconditionFailedEx;
import alma.stateengineexceptions.wrappers.AcsJPreconditionFailedEx;

public class SchedBlockStatusChecker {

	private static StateEngine stateEngine;
	private static StateArchive stateArchive;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static final long TIME_TO_SUSPEND = 30 * 24 * 60 * 60 * 1000; //30 days
	private static final long TIME_TO_CHECK_FOR_CALIBRATIONS = 30 * 60 * 1000; //30 mins
	
	public SchedBlockStatusChecker() {
		synchronized(SchedBlockStatusChecker.class) {
			if (!StateSystemContextFactory.INSTANCE.isInitialized()) {
				StateSystemContextFactory.INSTANCE.init(STATE_SYSTEM_SPRING_CONFIG,
								Logger.getAnonymousLogger());
				stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
				stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
			}
		}
	}
	
	public void checkForStatus() {
		checkForCalibrations();
	}
	
	public void forceCheckForStatus() {
		forceCheckForCalibrations();
	}
	
	private void checkForCalibrations() {
		String[] states = {StatusTStateType.CALIBRATORCHECK.toString()};
		Date currentDate = new Date();
		try {
			SBStatus[] statuses = stateArchive.findSBStatusByState(states);
			for(SBStatus s: statuses) {
				if(s.getTimeOfUpdate() != null) {
					try {
						Date date = dateFormat.parse(s.getTimeOfUpdate());
						//check if the SB has been waiting for calibrations too long 
						if (date.getTime() < (currentDate.getTime() - TIME_TO_SUSPEND)) {
							stateEngine.changeState(s.getSBStatusEntity(),  StatusTStateType.SUSPENDED, Subsystem.SCHEDULING, Role.AOD);
						}
						//Check if last update of SBStatus was not too soon
						else if(date.getTime() < (currentDate.getTime() - TIME_TO_CHECK_FOR_CALIBRATIONS)) { 
							//TODO: Run script and check for calibrations
						} 
					} catch (AcsJNoSuchTransitionEx e) {
						e.printStackTrace();
					} catch (AcsJNotAuthorizedEx e) {
						e.printStackTrace();
					} catch (AcsJPreconditionFailedEx e) {
						e.printStackTrace();
					} catch (AcsJPostconditionFailedEx e) {
						e.printStackTrace();
					} catch (AcsJNoSuchEntityEx e) {
						e.printStackTrace();
					}
				}
			}
		} catch (AcsJIllegalArgumentEx e) {
			e.printStackTrace();
		} catch (AcsJInappropriateEntityTypeEx e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void forceCheckForCalibrations() {
		String[] states = { StatusTStateType.CALIBRATORCHECK.toString() };
		SBStatus[] statuses;
		try {
			statuses = stateArchive.findSBStatusByState(states);
			for (SBStatus s : statuses) {
				// TODO: Run script and check for calibrations
			}
		} catch (AcsJIllegalArgumentEx e) {
			e.printStackTrace();
		} catch (AcsJInappropriateEntityTypeEx e) {
			e.printStackTrace();
		}

	}
}
