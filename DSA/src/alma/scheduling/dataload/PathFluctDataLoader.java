/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2006 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.dataload;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.dataload.WeatherDataReader.WeatherData;
import alma.scheduling.datamodel.weather.PathFluctHistRecord;


public class PathFluctDataLoader extends WeatherParameterDataLoader {

	private static Logger logger = LoggerFactory.getLogger(PathFluctDataLoader.class);
	
	@Override
	public void load() {
		try {
			List<PathFluctHistRecord> records = new ArrayList<PathFluctHistRecord>();
			WeatherData wd;
			while ((wd = getNextWeatherDatum()) != null) {
				PathFluctHistRecord record = new PathFluctHistRecord(wd.getTime(),
                        wd.getValue(), wd.getRms(), wd.getSlope());
				records.add(record);
			}
			dao.loadPathFluctHistory(records);
		} catch (Exception e) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(out, true); 
			e.printStackTrace(writer);
			writer.close();
			logger.error("Exception caught during loading of Path Fluct Data: " + out.toString());
			e.printStackTrace();
		} 
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
