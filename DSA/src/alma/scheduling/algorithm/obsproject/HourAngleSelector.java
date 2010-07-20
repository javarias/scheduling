package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
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
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		/* In the south hemisphere the calculation of the HA is: H + 12 = LST - alpha 
		 * The Hour angle to find is -4 <= H + 12 <= +4
		 */
		double raLowLimit = CoordinatesUtil.getRA(ut, 16.0, Constants.CHAJNANTOR_LONGITUDE);
		double raHighLimit = CoordinatesUtil.getRA(ut, 8.0, Constants.CHAJNANTOR_LONGITUDE);
		logger.info("RA Limits: " + raLowLimit + ", " + raHighLimit);
		List<SchedBlock> res = null;
		if (raHighLimit < raLowLimit ){
			res = sbDao.findSchedBlocksBetweenHourAngles(raLowLimit * 15.0 , 360);
			res.addAll(sbDao.findSchedBlocksBetweenHourAngles(0, raHighLimit * 15.0));
		}
		else
			res = sbDao.findSchedBlocksBetweenHourAngles(raLowLimit * 15.0, raHighLimit * 15.0);
		if(res.size() == 0)
			throw new NoSbSelectedException("sbDao.findSchedBlocksBetweenHourAngles returned no results");
		this.printVerboseInfo(res, arrConf.getId(), ut);
		logger.debug("HourAngle selector returned " + res.size() + " SchedBlock redords");
		return res;
	}

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        double raLowLimit = CoordinatesUtil.getRA(ut, 16.0,
                Constants.CHAJNANTOR_LONGITUDE);
        double raHighLimit = CoordinatesUtil.getRA(ut, 8.0,
                Constants.CHAJNANTOR_LONGITUDE);

        Criterion crit = null;
        if (raHighLimit < raLowLimit) {
            Disjunction disj = Restrictions.disjunction();
            Conjunction con1 = Restrictions.conjunction();
            con1.add(Restrictions.ge("s.coordinates.RA", new Double(raLowLimit * 15)));
            con1.add(Restrictions.le("s.coordinates.RA", new Double(360)));
            disj.add(con1);
            Conjunction con2 = Restrictions.conjunction();
            con2.add(Restrictions.ge("s.coordinates.RA", new Double(0)));
            con2.add(Restrictions.le("s.coordinates.RA", new Double(raHighLimit * 15)));
            disj.add(con2);
            crit = disj;
        } else {
            Conjunction conj = Restrictions.conjunction();
            conj.add(Restrictions.ge("s.coordinates.RA", new Double(raLowLimit * 15)));
            conj.add(Restrictions.le("s.coordinates.RA", new Double(raHighLimit * 15)));
            crit = conj;
        }
        return crit;
    }

}
