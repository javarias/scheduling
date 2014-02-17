package alma.scheduling.datamodel.config.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.ScienceGradeConfig;

public abstract class BaseConfigurationDaoImpl implements ConfigurationDao {

	private static Logger logger = LoggerFactory.getLogger(BaseConfigurationDaoImpl.class);
    protected static String workDir = "";
	public static final String ENV_VARIABLE = "SCHED_WORK_DIR";
	protected static final String ENV_WORK_DIR;
	public static final String PROP_WORK_DIR = "scheduling.workdir";
	static {
		ENV_WORK_DIR = System.getenv(ENV_VARIABLE);
		if (ENV_WORK_DIR != null && System.getProperty(PROP_WORK_DIR) == null)
			System.setProperty(PROP_WORK_DIR, ENV_WORK_DIR);
	}
	protected static final String APRC_CONFIG_FILE = "aprc-config.xml";
	
	protected Configuration config = null;
	
	public BaseConfigurationDaoImpl() {
	}

	@Override
	public Configuration getConfiguration() {
		if (config != null)
			return config;
		if (ENV_WORK_DIR == null && System.getProperty(PROP_WORK_DIR) == null)
			throw new IllegalArgumentException(ENV_VARIABLE + " env variable and " + PROP_WORK_DIR + "java property not set");
		FileReader reader = null;
		alma.scheduling.input.config.generated.Configuration xmlConfig = null;
		try {
			if (System.getProperty(PROP_WORK_DIR) != null)
				reader = new FileReader(new File(System.getProperty(PROP_WORK_DIR) + "/" + APRC_CONFIG_FILE));
			else
				reader = new FileReader(new File(System.getenv(ENV_VARIABLE) + "/" + APRC_CONFIG_FILE));
			xmlConfig = alma.scheduling.input.config.generated.Configuration.unmarshal(reader);
		} catch (MarshalException | ValidationException| FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		config = new Configuration();
		config.setWorkDirectory(
				(System.getProperty(PROP_WORK_DIR) != null) ?  
						System.getProperty(PROP_WORK_DIR) : 
							System.getenv(ENV_VARIABLE));
		config.setProjectDirectory(xmlConfig.getProjectDirectory());
		config.setWeatherDirectory(xmlConfig.getWeatherDirectory());
		config.setObservatoryDirectory(xmlConfig.getObservatoryDirectory());
		config.setExecutiveDirectory(xmlConfig.getExecutiveDirectory());
		config.setOutputDirectory(xmlConfig.getOutputDirectory());
		config.setReportDirectory(xmlConfig.getReportDirectory());
		config.setLastLoad(null); // for now
		config.setContextFilePath(xmlConfig.getContextFilePath());
		config.setArrayCenterLatitude(xmlConfig.getArrayCenterLatitude());
		config.setArrayCenterLongitude(xmlConfig.getArrayCenterLongitude());
		config.setMaxWindSpeed(xmlConfig.getMaxWindSpeed());
		ScienceGradeConfig sc = new ScienceGradeConfig();
		sc.setnGradeAPrj((int) xmlConfig.getGradeA());
		sc.setnGradeBPrj((int) xmlConfig.getGradeB());
		sc.setnGradeCPrj((int) xmlConfig.getGradeC());
		config.setScienceGradeConfig(sc);
		return config;
	}

}