package alma.scheduling.spt.util;

import org.springframework.context.support.GenericApplicationContext;

import alma.scheduling.datamodel.config.dao.ConfigurationDaoImpl;
import alma.scheduling.psm.sim.InputActions;
import alma.scheduling.utils.DSAContextFactory;

public class SimulatorContextFactory extends DSAContextFactory {

	public static final String WEATHER_PARAMS_LOADER_BEAN = "weatherSimDataLoader";
	public static final String ALMA_ARCHIVE_FULL_DATA_LOADER = "AlmaArchiveFullDataLoader";
	public static final String OBSPROJECT_DATA_LOADER_BEAN = "obsProjectDataLoader";
	public static final String ALMA_ARCHIVE_OBSPROJECT_DATA_LOADER_BEAN = "AlmaArchiveObsProjectDataLoader";
	public static final String ARCHIVE_PROJECT_DAO_BEAN = "archProjectDao";
	public static final String CONFIGURATION_DAO_BEAN = "configDao";
	public static final String IMMUTABLE_DATA_LOADER_BEAN = "immutableDataLoader";
	public static final String DATA_CLEANER_BEAN = "dataCleaner";
	
	public static synchronized void closeContext() {
		((GenericApplicationContext)context).close();
		context = null;
	}
	
	public static synchronized void doFullLoad() {
		String workDir = System.getProperty(ConfigurationDaoImpl.PROP_WORK_DIR);
		InputActions.setApplicationContext(getContext());
		InputActions input = InputActions.getInstance(workDir);
		//load
		
		try {
			input.fullLoad(InputActions.IMMUTABLE_DATA_LOADER_BEAN);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
