package alma.scheduling.datamodel.observatory.dao;

import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import junit.framework.TestCase;

public class TmcdbDaoImplTest extends TestCase {

	private TmcdbDaoImpl dao = null;
	
	@Override
	protected void setUp() throws Exception {
		dao = new TmcdbDaoImpl();
	}
	
	public void testGetArrayConfiguration() throws Exception {
		String [] antennas = {"DV01" , "DV02", "DV03", "DA41"};
		ArrayConfiguration config = dao.getConfigurationForArray("Array001", antennas);
		System.out.println("Min. Baseline: " + config.getMinBaseline());
		System.out.println("Max. Baseline: " + config.getMaxBaseline());
		System.out.println("Number of antennas: " + config.getNumberOfAntennas());
		System.out.println("Antenna Diameter: " + config.getAntennaDiameter());
		System.out.println(config.getAntennaInstallations());
	}
	
	public void testGetArrayAgain() throws Exception {
		String [] antennas = {"DV01" , "DV02", "DV03", "DA41"};
		ArrayConfiguration config = dao.getConfigurationForArray("Array001", antennas);
		System.out.println("Min. Baseline: " + config.getMinBaseline());
		System.out.println("Max. Baseline: " + config.getMaxBaseline());
		System.out.println("Number of antennas: " + config.getNumberOfAntennas());
		System.out.println("Antenna Diameter: " + config.getAntennaDiameter());
		System.out.println(config.getAntennaInstallations());
	}

	
	
	
}
