package alma.scheduling.datamodel.observatory.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

import org.hibernate.Session;
import org.hibernate.Transaction;

import alma.TmcdbErrType.wrappers.AcsJTmcdbNoSuchRowEx;
import alma.acs.time.TimeHelper;
import alma.scheduling.datamodel.observatory.Antenna;
import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.Pad;
import alma.scheduling.utils.Constants;
import alma.tmcdb.access.TmcdbHibernateAccessor;
import alma.tmcdb.domain.AntennaToPad;
import alma.tmcdb.domain.BaseElement;
import alma.tmcdb.domain.BaseElementType;
import alma.tmcdb.domain.HwConfiguration;
import alma.tmcdb.utils.HibernateUtil;

public class TmcdbDaoImpl extends TmcdbHibernateAccessor implements TmcdbDao {

	private static LinkedHashMap<String, ArrayConfiguration> cache = new LinkedHashMap<String, ArrayConfiguration>();;
	
	public TmcdbDaoImpl() throws Exception {
		super();
	}
	
	@Override
	public synchronized ArrayConfiguration getConfigurationForArray(String arrayName, String[] antennas) throws AcsJTmcdbNoSuchRowEx {
		if (cache.containsKey(arrayName))
			return cache.get(arrayName);
		
		ArrayList<String> antennaList = new ArrayList<String>(Arrays.asList(antennas));
		LinkedHashSet<AntennaToPad> apFound =  new LinkedHashSet<AntennaToPad>();
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		
		try {
			HwConfiguration hwc = getLocalHwConfiguration(session);
			for (BaseElement be: hwc.getBaseElements()) {
				if (be.getType().equals(BaseElementType.Pad)) {
					alma.tmcdb.domain.Pad pad = (alma.tmcdb.domain.Pad) be;
					for (AntennaToPad ap: pad.getScheduledAntennas()) {
						if (ap.getEndTime() == null && 
								antennaList.contains(ap.getAntenna().getName())) {
							apFound.add(ap);
						}
					}
				}
			}
			
		} finally {
			tx.commit();
			session.close();
		}
		
		ArrayConfiguration retVal = new ArrayConfiguration();
		Set<AntennaInstallation> installations = new HashSet<AntennaInstallation>();
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		for (AntennaToPad ap: apFound) {
			Antenna a = new Antenna();
			a.setName(ap.getAntenna().getName());
			cal.setTimeInMillis(TimeHelper.utcOmgToJava(ap.getStartTime()));
			a.setCommissionDate(cal.getTime());
			a.setDiameter(ap.getAntenna().getDiameter());
			
			Pad p = new Pad();
			p.setxPosition(ap.getPad().getPosition().getX());
			p.setyPosition(ap.getPad().getPosition().getY());
			p.setzPosition(ap.getPad().getPosition().getZ());
			cal.setTimeInMillis(TimeHelper.utcOmgToJava(ap.getPad().getCommissionDate()));
			p.setCommissionDate(cal.getTime());
			
			AntennaInstallation ai = new AntennaInstallation();
			cal.setTimeInMillis(TimeHelper.utcOmgToJava(ap.getStartTime()));
			ai.setStartTime(cal.getTime());
			ai.setEndTime(null);
			ai.setAntenna(a);
			ai.setPad(p);
			
			installations.add(ai);
		}
		
		retVal.setAntennaInstallations(installations);
		retVal.setNumberOfAntennas(installations.size());
		
		//Calculate all baselines looking for the max and min baselines
		double maxBL = 0.0D;
		double minBL = Double.POSITIVE_INFINITY;
		{
			AntennaInstallation[] tmp = new AntennaInstallation[installations.size()];
			installations.toArray(tmp);
			for (int i = 0; i < installations.size() - 1; i++) {
				double absi = Math.sqrt(tmp[i].getPad().getxPosition() * tmp[i].getPad().getxPosition() +
						tmp[i].getPad().getyPosition() * tmp[i].getPad().getyPosition() +
						tmp[i].getPad().getzPosition() * tmp[i].getPad().getzPosition());
				for (int j = i + 1; j < installations.size(); j++) {
					double dot = tmp[i].getPad().getxPosition() * tmp[j].getPad().getxPosition() +
							tmp[i].getPad().getyPosition() * tmp[j].getPad().getyPosition() +
							tmp[i].getPad().getzPosition() * tmp[j].getPad().getzPosition();
					double absj = Math.sqrt(tmp[j].getPad().getxPosition() * tmp[j].getPad().getxPosition() +
							tmp[j].getPad().getyPosition() * tmp[j].getPad().getyPosition() +
							tmp[j].getPad().getzPosition() * tmp[j].getPad().getzPosition());
					double angle = Math.acos(Math.abs(dot / (absi * absj)));
					double distance = angle * Constants.EARTH_RADIUS;
					if (distance > maxBL)
						maxBL = distance;
					if (distance < minBL)
						minBL = distance;
				}
			}
		}
		retVal.setMinBaseline(minBL);
		retVal.setMaxBaseline(maxBL);
		
		//Check the antenna diameters, if 7m antenna is found put the installation as 7m
		retVal.setAntennaDiameter(12.0D);
		for (AntennaInstallation ai: installations) {
			if (ai.getAntenna().getDiameter() == 7.0) {
				retVal.setAntennaDiameter(7.0D);
				break;
			}
		}
		
		cache.put(arrayName, retVal);
		
		return retVal;
	}
}
