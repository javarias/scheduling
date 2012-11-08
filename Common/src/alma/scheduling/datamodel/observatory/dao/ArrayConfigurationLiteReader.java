package alma.scheduling.datamodel.observatory.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import alma.scheduling.datamodel.CannotParseDataException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;

/**
 * Reads the lite array configuration file.
 * 
 * The format of the file is the following:
 * <pre>
 * {@code 
 * # this is a comment line
 * # Array Name, Configuration Name, Number of Antennas, Min baseline, Max baseline, Start Date, End Date 
 * # Example:
 * 12-m,C32-1,32,15,150,2013.1.1,2013.3.28 # Another Comment
 * 12-m,C32-2,32,25,150,2013.3.1,2013.4.30 # most compact
 * 12-m,C32-3,32,45,150,2013.5.1,2013.4.31 # most compact
 * 7-m,normal,8,9,30,2013.1.1,2013.2.28
 * 7-m,nsextend,8,9,40,2013.3.1,2013.5.31
 * }
 * </pre>
 * 
 * @since ALMA-9.1.X
 * @author javarias
 *
 */
public class ArrayConfigurationLiteReader extends BufferedReader {

	private static final String COMMENT_STRING = "#";
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd");
	static {
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private List<ArrayConfiguration> arrayConfigs = null;
	
	public ArrayConfigurationLiteReader(Reader in) {
		super(in);
	}
	
	public List<ArrayConfiguration> getArrayConfiguration() throws IOException, CannotParseDataException {
		if (arrayConfigs != null)
			return arrayConfigs;
		ArrayList<ArrayConfiguration> ret = new ArrayList<ArrayConfiguration>();
		String line;
		long lineno = -1;
		while ((line = readLine()) != null) {
			lineno++;
			if (line.startsWith(COMMENT_STRING))
				continue;
			String[] fields = line.split(",");
			if (fields.length != 7)
				throw new CannotParseDataException(lineno, "Line: " + line);
			try {
				ArrayConfiguration tmp = new ArrayConfiguration();
				tmp.setArrayName(fields[0].split("#")[0]);
				tmp.setConfigurationName(fields[1].split("#")[0]);
				tmp.setNumberOfAntennas(Integer.valueOf(fields[2].split("#")[0]));
				tmp.setMinBaseline(Integer.valueOf(fields[3].split("#")[0]));
				tmp.setMaxBaseline(Integer.valueOf(fields[4].split("#")[0]));
				tmp.setStartTime(dateFormatter.parse(fields[5].split("#")[0]));
				tmp.setEndTime(dateFormatter.parse(fields[6].split("#")[0]));
				ret.add(tmp);
			} catch (Exception ex) {
				throw new CannotParseDataException(lineno, "Line: " + line, ex);
			}
		}
		arrayConfigs = ret;
		return arrayConfigs;
	}

}
