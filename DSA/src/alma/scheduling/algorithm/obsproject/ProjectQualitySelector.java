package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;

public class ProjectQualitySelector extends AbstractBaseSelector {

    public ProjectQualitySelector(String selectorName) {
        super(selectorName);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        Criterion crit = Restrictions.ne("letterGrade", ScienceGrade.D);
        return crit;
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        return null;
    }

}
