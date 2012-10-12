package alma.scheduling.archiveupd.external;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;

import java.util.logging.Logger;


import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.config.StateSystemContextFactory;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
import alma.lifecycle.stateengine.action.plugin.CalibratorCheckPlugin;
import alma.lifecycle.stateengine.constants.Role;
import alma.lifecycle.stateengine.constants.Subsystem;

public class CalibratorCheckPluginImpl implements CalibratorCheckPlugin {

	private static StateEngine stateEngine;
	private static StateArchive stateArchive;
	
	public CalibratorCheckPluginImpl() {
		synchronized (CalibratorCheckPluginImpl.class) {
			if (!StateSystemContextFactory.INSTANCE.isInitialized()) {
				StateSystemContextFactory.INSTANCE.init(STATE_SYSTEM_SPRING_CONFIG,
								Logger.getAnonymousLogger());
			}
			if(stateArchive == null)
				stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
			if(stateEngine == null)
				stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
		}
	}
	
	@Override
	public void processSchedBlock(SchedBlock schedBlock) {
		SBStatusEntityT sbStatus = new SBStatusEntityT();
		sbStatus.setEntityId(schedBlock.getSBStatusRef().getEntityId());
		try {
			Logger.getAnonymousLogger().info("Tranistioning SB: " 
					+ schedBlock.getSchedBlockEntity().getEntityId() 
					+ " from: "+ schedBlock.getStatus() +" to " + StatusTStateType.CALIBRATORCHECK);
			stateEngine.changeState(sbStatus, StatusTStateType.CALIBRATORCHECK, Subsystem.SCHEDULING, Role.AOD);
			//TODO: Call the script to check for available calibrations
//			Process p = Runtime.getRuntime().exec("script");
//			p.waitFor();
//			if(p.exitValue() == 0)
			Logger.getAnonymousLogger().info("Tranistioning SB: " 
					+ schedBlock.getSchedBlockEntity().getEntityId() + " to " + StatusTStateType.READY);
				stateEngine.changeState(sbStatus, StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe("Failed to tranistion SB: " 
					+ schedBlock.getSchedBlockEntity().getEntityId() + " Cause: " + e.getMessage());
			e.printStackTrace();
		} 
	}

}
