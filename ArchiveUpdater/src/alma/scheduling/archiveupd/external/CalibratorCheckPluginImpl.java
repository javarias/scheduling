package alma.scheduling.archiveupd.external;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
			//Run the script
			Process p = Runtime.getRuntime().exec("testCalibratorsCheckScript");
			ProcessReader stdout = new ProcessReader(p.getInputStream(), "SCRIPT OUTPUT");
			stdout.start();
			ProcessReader stderr = new ProcessReader(p.getErrorStream(), "SCRIPT ERROR");
			stderr.start();
			int exitVal = p.waitFor();
			//Check the output of the script
			//If the script fails it should finish with a code distinct than 0, throw an error in this case
			//TODO: How to throw an error outside ACS
			if (exitVal == 0) {
				String outStr = stdout.getOutput().toString();
				int val = Integer.valueOf(outStr.substring(outStr.length() - 2, outStr.length() - 1));
				if (val == 0) {
					Logger.getAnonymousLogger().info("Tranistioning SB: " 
							+ schedBlock.getSchedBlockEntity().getEntityId() + " to " + StatusTStateType.READY);
						stateEngine.changeState(sbStatus, StatusTStateType.READY, Subsystem.SCHEDULING, Role.AOD);
				}
			}
			else {
				Logger.getAnonymousLogger().severe("Exit code of the script was not 0. Check the logs");
			}
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe("Failed to tranistion SB: " 
					+ schedBlock.getSchedBlockEntity().getEntityId() + " Cause: " + e.getMessage());
			e.printStackTrace();
		} 
	}

	private static class ProcessReader extends Thread{
		private InputStream is;
	    private String type;
	    private StringBuilder builder;
	    
		public ProcessReader(InputStream is, String outputType) {
			this.is = is;
			this.type = outputType;
			builder = new StringBuilder();
		}

		@Override
		public void run() {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			try {
				while((line = br.readLine()) != null) {
					System.out.println(type + ">" + line);
					builder.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public String getOutput() {
			return builder.toString();
		}
	}
}
