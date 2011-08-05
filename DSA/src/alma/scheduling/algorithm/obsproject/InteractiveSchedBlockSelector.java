package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class InteractiveSchedBlockSelector extends AbstractBaseSelector {

	public InteractiveSchedBlockSelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		return null;
	}

	@Override
	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
		return Restrictions.not(Restrictions.eq("sb.manual", false));
	}

}
