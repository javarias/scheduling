package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.algorithm.astro.SunAstroData;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class SunAvoidanceSelector extends AbstractBaseSelector {

    private SchedBlockDao sbDao;
    
    public SunAvoidanceSelector(String selectorName) {
        super(selectorName);
    }
    
    public SchedBlockDao getSbDao() {
        return sbDao;
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
        SunAstroData sunData = CoordinatesUtil.getSunAstroData(ut);
        double highRa, lowRa, highDec, lowDec;
        highRa = sunData.getRA() + sunData.getAngularDiameter()/2;
        lowRa = sunData.getRA() - sunData.getAngularDiameter()/2;
        highDec = sunData.getDec() + sunData.getAngularDiameter()/2;
        lowDec = sunData.getDec() - sunData.getAngularDiameter()/2;
        List<SchedBlock> sbs = sbDao.findSchedBlocksOutOfArea(lowRa, highRa, lowDec, highDec);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        SunAstroData sunData = CoordinatesUtil.getSunAstroData(ut);
        double highRa, lowRa, highDec, lowDec;
        highRa = sunData.getRA() + sunData.getAngularDiameter()/2;
        lowRa = sunData.getRA() - sunData.getAngularDiameter()/2;
        highDec = sunData.getDec() + sunData.getAngularDiameter() / 2;
        lowDec = sunData.getDec() - sunData.getAngularDiameter() / 2;

        Conjunction conj = Restrictions.conjunction();
        conj.add(Restrictions.ge("s.coordinates.RA", new Double(lowRa)));
        conj.add(Restrictions.le("s.coordinates.RA", new Double(highRa)));
        conj.add(Restrictions.ge("s.coordinates.Dec", new Double(lowDec)));
        conj.add(Restrictions.le("s.coordinates.Dec", new Double(highDec)));
        Criterion crit = Restrictions.not(conj);
        return crit;
    }

}
