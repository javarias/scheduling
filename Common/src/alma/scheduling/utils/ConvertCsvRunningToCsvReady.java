package alma.scheduling.utils;

import java.util.List;
import java.util.logging.Logger;

import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;


public class ConvertCsvRunningToCsvReady {

	private static final StatusTStateType csvRunningState = StatusTStateType.CSVRUNNING;
	private static final StatusTStateType csvReadyState =  StatusTStateType.CSVREADY;
	
	public static void main (String[] args) {
		ModelAccessor model = null;
		try {
			model =  new ModelAccessor();
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe("Unable to initialize access to Databases. Check the exception below.");
			e.printStackTrace();
			Logger.getAnonymousLogger().severe("Quitting...");
			System.exit(-1);
		}
		List<SchedBlock> sbs = model.getAllSchedBlocks(true);
		StateArchive sa = model.getStateArchive();
		StateEngine se = model.getStateEngine();
		
		Logger.getAnonymousLogger().info("Found " + sbs.size() + " manual SBs in SWDB. Checking states of them and doing transitions when is necessary" );
		for(SchedBlock sb: sbs) {
			SBStatus status = sa.getSBStatus(sb.getStatusEntity());
			Logger.getAnonymousLogger().info("Checking SB uid: " + sb.getUid());
			if (status.getStatus().getState().getType() == csvRunningState.getType()) {
				Logger.getAnonymousLogger().info("Status of SB uid: " + status.getStatus().getState().toString() +
						". Doing transition to " + csvReadyState.toString() +"...");
				se.changeState(status.getSBStatusEntity(), csvReadyState, Subsystem.SCHEDULING, Role.AOD);
				Logger.getAnonymousLogger().info("Transition of SB uid:" + sb.getUid() +" completed.");
			} else {
				Logger.getAnonymousLogger().info("Status of SB uid: " + status.getStatus().getState().toString() +
						". Nothing to do here.");
			}
		}
	}
}
