package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.algorithm.astro.MoonAstroData;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class MoonAvoidanceSelector extends AbstractBaseSelector {

    private SchedBlockDao sbDao;
    
    public MoonAvoidanceSelector(String selectorName) {
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
        MoonAstroData moonData = CoordinatesUtil.getMoonAstroData(ut);
        double highRa, lowRa, highDec, lowDec;
        highRa = moonData.getRa() + moonData.getAngularDiameter()/2;
        lowRa = moonData.getRa() - moonData.getAngularDiameter()/2;
        highDec = moonData.getDec() + moonData.getAngularDiameter()/2;
        lowDec = moonData.getDec() - moonData.getAngularDiameter()/2;
        List<SchedBlock> sbs = sbDao.findSchedBlocksOutOfArea(lowRa, highRa, lowDec, highDec);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        MoonAstroData moonData = CoordinatesUtil.getMoonAstroData(ut);
        double highRa, lowRa, highDec, lowDec;
        highRa = moonData.getRa() + moonData.getAngularDiameter() / 2;
        lowRa = moonData.getRa() - moonData.getAngularDiameter() / 2;
        highDec = moonData.getDec() + moonData.getAngularDiameter() / 2;
        lowDec = moonData.getDec() - moonData.getAngularDiameter() / 2;
        Criterion crit1 = Restrictions.not(Restrictions.conjunction().add(
                Restrictions.and(Restrictions.ge("s.coordinates.RA",
                        new Double(lowRa)), Restrictions.le("s.coordinates.RA",
                        new Double(highRa)))));
        Criterion crit2 = Restrictions.not(Restrictions.conjunction().add(
                Restrictions.and(Restrictions.ge("s.coordinates.Dec",
                        new Double(lowDec)), Restrictions.le(
                        "s.coordinates.Dec", new Double(highDec)))));
        Criterion crit = Restrictions.and(crit1, crit2);
        return crit;
    }

}
