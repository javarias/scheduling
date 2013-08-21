package alma.scheduling.algorithm.observatory;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.Constants;

public class AngularResolutionSelector extends AbstractBaseSelector {

	private static final double convertToArcSec = 180.D / Math.PI * 3600.0D;
	private static final String lambda = Constants.LIGHT_SPEED + " / ( REPR_FREQ * 1E9 )";
	
	public AngularResolutionSelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		return null;
	}

	@Override
	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
		Conjunction retVal = Restrictions.conjunction();
		double maxBL = arrConf.getMaxBaseline();
		retVal.add(Restrictions.sqlRestriction("MIN_ANG_RESOLUTION <= " + lambda + " / " + maxBL + " * " + convertToArcSec));
		retVal.add(Restrictions.sqlRestriction("MAX_ANG_RESOLUTION >= " + lambda + " / " + maxBL + " * " + convertToArcSec));
//		retVal.add(Restrictions.leProperty("schedulingConstraints.minAngularResolution" , lambda + " / " + maxBL + " * " + convertToArcSec));
//		retVal.add(Restrictions.geProperty("schedulingConstraints.maxAngularResolution" , lambda + " / " + maxBL + " * " + convertToArcSec));
		return retVal;
	}
	
}
