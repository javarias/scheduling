package alma.scheduling.datamodel.observatory.dao;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import alma.scheduling.datamodel.CannotParseDataException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;

import junit.framework.TestCase;

public class ArrayConfigurationLiteReaderTest extends TestCase {

	private ArrayConfigurationLiteReader reader;
	private static final String fileContent = "#this is a comment line\n" +
			"# Array Name, Configuration Name, Number of Antennas, Min baseline, Max baseline, Start Date, End Date\n" +  
			"# Example:\n" +
			"12-m,C32-1,32,15,150,2013.1.1,2013.3.28 # Another Comment\n" +
			"12-m,C32-2,32,25,150,2013.3.1,2013.4.30 # most compact\n" + 
			"12-m,C32-3,32,45,150,2013.5.1,2013.4.31 # most compact\n" +
			"7-m,normal,8,9,30,2013.1.1,2013.2.28\n" +
			"7-m,nsextend,8,9,40,2013.3.1,2013.5.31";
	
	
	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testReader() throws IOException, CannotParseDataException {
		StringReader strReader = new StringReader(fileContent);
		reader = new ArrayConfigurationLiteReader(strReader);
		List<ArrayConfiguration> configs = reader.getArrayConfiguration();
		for (ArrayConfiguration c: configs) {
			System.out.println(c.getArrayName() + ", " + c.getConfigurationName() + ": ");
			System.out.println(c.getNumberOfAntennas() + "; " + c.getMinBaseline() + "; " 
			+ c.getMaxBaseline() + "; " + c.getStartTime() + "; " + c.getEndTime());
		}
	}
	
	public void testExceptionInReader() throws IOException {
		String badFileContent = "#this is a comment line\n" +
				"# Array Name, Configuration Name, Number of Antennas, Min baseline, Max baseline, Start Date, End Date\n" +  
				"# Example:\n" +
				"12-m,C32-1,32,15,150,2013.1.1,2013.3.28, bla # Another Comment\n" +
				"12-m,C32-2,32,25,150,2013.3.1,2013.4.30 # most compact\n" + 
				"12-m,C32-3,32,45,150,2013.5.1,2013.4.31 # most compact\n" +
				"7-m,normal,8,9,30,2013.1.1,2013.2.28\n" +
				"7-m,nsextend,8,9,40,2013.3.1,2013.5.31";
		StringReader strReader = new StringReader(badFileContent);
		reader = new ArrayConfigurationLiteReader(strReader);
		List<ArrayConfiguration> configs = null;
		try {
			configs = reader.getArrayConfiguration();
		} catch (CannotParseDataException e) {
			System.out.println();
			System.out.println(e.getMessage());
		}
		assertNull(configs);
	}
}
