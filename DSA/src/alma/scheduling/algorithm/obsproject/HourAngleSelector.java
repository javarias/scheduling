package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.astro.Constants;
import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class HourAngleSelector extends AbstractBaseSelector {

	private static Logger logger = LoggerFactory.getLogger(HourAngleSelector.class);
	
	SchedBlockDao sbDao = null;
	
	public HourAngleSelector(String selectorName) {
		super(selectorName);
	}
	
	public void setSbDao(SchedBlockDao sbDao) {
		this.sbDao = sbDao;
	}

	@Override
	public Collection<SchedBlock> select() throws NoSbSelectedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SchedBlock> select(Date ut) throws NoSbSelectedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SchedBlock> select(ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		/* In the south hemisphere the calculation of the HA is: H + 12 = LST - alpha 
		 * The Hour angle to find is -4 <= H + 12 <= +4
		 */
		double raLowLimit = CoordinatesUtil.getRA(ut, 16.0, Constants.CHAJNANTOR_LONGITUDE);
		double raHighLimit = CoordinatesUtil.getRA(ut, 8.0, Constants.CHAJNANTOR_LONGITUDE);
		System.out.println("RA Limits: " + raLowLimit + ", " + raHighLimit);
		List<SchedBlock> res = null;
		if (raHighLimit < raLowLimit ){
			res = sbDao.findSchedBlocksBetweenHourAngles(raLowLimit * 15.0 , 360);
			res.addAll(sbDao.findSchedBlocksBetweenHourAngles(0, raHighLimit * 15.0));
		}
		else
			res = sbDao.findSchedBlocksBetweenHourAngles(raLowLimit, raHighLimit);
		if(res.size() == 0)
			throw new NoSbSelectedException("sbDao.findSchedBlocksBetweenHourAngles returned no results");
		this.printVerboseInfo(res, arrConf.getId(), ut);
		logger.debug("HourAngle selector returned " + res.size() + " SchedBlock redords");
		return res;
	}

}
