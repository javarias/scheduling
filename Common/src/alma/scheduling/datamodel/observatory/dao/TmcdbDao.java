package alma.scheduling.datamodel.observatory.dao;

import alma.TmcdbErrType.wrappers.AcsJTmcdbNoSuchRowEx;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;

public interface TmcdbDao {

	public ArrayConfiguration getConfigurationForArray(String arrayName, String[] antennas) throws AcsJTmcdbNoSuchRowEx;
	
}
