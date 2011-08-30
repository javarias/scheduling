package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.utils.TimeUtil;

public class FieldSourceObservableSelector extends AbstractBaseSelector {
    
    private static Logger logger = LoggerFactory.getLogger(FieldSourceObservableSelector.class);
    
    public FieldSourceObservableSelector(String selectorName) {
        super(selectorName);
    }

    // --- Spring set properties and accessors ---
    
    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }

    // --- SchedBlockSelector impl --

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        double longitude = configDao.getConfiguration().getArrayCenterLongitude();
        double lst = TimeUtil.getLocalSiderealTime(ut, longitude);
        logger.debug("lst = " + lst);
        logger.debug("time = " + ut.toString());
        logger.debug("longitude = " + longitude);
        Collection<SchedBlock> sbs = 
            schedBlockDao.findSchedBlocksWithVisibleRepresentativeTarget(lst);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        double longitude = configDao.getConfiguration().getArrayCenterLongitude();
        double lst = TimeUtil.getLocalSiderealTime(ut, longitude);
        System.out.println("lst = " + lst);
        System.out.println("time = " + ut.toString());
        System.out.println("longitude = " + longitude);
        /*
         *         from SchedBlock sb where 
       sb.schedulingConstraints.representativeTarget.source.observability.alwaysVisible = true
       or
       (sb.schedulingConstraints.representativeTarget.source.observability.risingTime <
        sb.schedulingConstraints.representativeTarget.source.observability.settingTime and
        sb.schedulingConstraints.representativeTarget.source.observability.risingTime < ? and
        sb.schedulingConstraints.representativeTarget.source.observability.settingTime > ?) or
       (sb.schedulingConstraints.representativeTarget.source.observability.risingTime >
        sb.schedulingConstraints.representativeTarget.source.observability.settingTime and
       (sb.schedulingConstraints.representativeTarget.source.observability.risingTime < ? or
        sb.schedulingConstraints.representativeTarget.source.observability.settingTime > ?)) */
        Conjunction conj1 = Restrictions.conjunction();
        conj1.add(Restrictions.ltProperty("s.observability.risingTime",
                "s.observability.settingTime"));
        conj1.add(Restrictions
                .lt("s.observability.risingTime", new Double(lst)));
        conj1.add(Restrictions.gt("s.observability.settingTime",
                new Double(lst)));

        Disjunction disj3 = Restrictions.disjunction();
        disj3.add(Restrictions
                .lt("s.observability.risingTime", new Double(lst)));
        disj3.add(Restrictions.gt("s.observability.settingTime",
                new Double(lst)));
        Criterion crit2 = Restrictions.and(Restrictions.gtProperty(
                "s.observability.risingTime", "s.observability.settingTime"),
                disj3);
        Criterion crit = Restrictions.or(conj1, crit2);
        crit = Restrictions.or(Restrictions.eq("s.observability.alwaysVisible", true), crit);
        return crit;
    }

}
