package alma.scheduling.datamodel.config.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.utils.Constants;

public class ConfigurationDaoImpl implements ConfigurationDao {

	private static final String ENV_WORK_DIR;
	private static final String PROP_WORK_DIR = "scheduling.workdir";
	static {
		ENV_WORK_DIR = System.getenv("SCHED_WORK_DIR");
		if (ENV_WORK_DIR != null && System.getProperty(PROP_WORK_DIR) == null)
			System.setProperty(PROP_WORK_DIR, ENV_WORK_DIR);
	}
	private static final String APRC_CONFIG_FILE = "aprc-config.xml";
	
	@Override
	public Configuration getConfiguration() {
		if (ENV_WORK_DIR == null && System.getProperty(PROP_WORK_DIR) == null)
			throw new IllegalArgumentException("SCHED_WORK_DIR env variable and " + PROP_WORK_DIR + "java property not set");
		FileReader reader = null;
		alma.scheduling.input.config.generated.Configuration xmlConfig = null;
		try {
			reader = new FileReader(new File(System.getProperty(PROP_WORK_DIR) + "/" + APRC_CONFIG_FILE));
			xmlConfig = alma.scheduling.input.config.generated.Configuration.unmarshal(reader);
		} catch (MarshalException | ValidationException| FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Configuration config = new Configuration();
		config.setExecutiveDirectory(xmlConfig.getExecutiveDirectory());
		config.setProjectDirectory(xmlConfig.getProjectDirectory());
		config.setWorkDirectory(System.getProperty(PROP_WORK_DIR));
		config.setArrayCenterLongitude(Constants.CHAJNANTOR_LONGITUDE);
		config.setArrayCenterLatitude(Constants.CHAJNANTOR_LATITUDE);
		return config;
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateConfig(Date lastUpdateTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateConfig(String simStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNextStep(Date nextStepTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSimStartTime(Date simStartTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteForSimulation() {
		// TODO Auto-generated method stub

	}

}
