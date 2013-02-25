package alma.scheduling.datamodel.weather.dao;

import java.util.Date;

import junit.framework.TestCase;

public class WeatherStationDaoTest extends TestCase {

	public void testPWVForecast() throws Exception {
		WeatherStationDao dao = new WeatherStationDao();
		dao.hasPWV();
		double pwv1 = dao.getPwvForTime(new Date());
		double pwv2 = dao.getPwvForTime(new Date());
		
		assertEquals(pwv1, pwv2, 0.1);
	}

}
