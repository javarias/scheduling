package alma.scheduling.datamodel.config.dao;

import java.util.Date;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.utils.Constants;

public class ConfigurationDaoImpl implements ConfigurationDao {

	@Override
	public Configuration getConfiguration() {
		Configuration config = new Configuration();
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
