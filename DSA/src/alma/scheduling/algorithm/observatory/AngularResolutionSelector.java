package alma.scheduling.algorithm.observatory;

import java.util.Collection;
import java.util.Date;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ArrayType;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.utils.Constants;

public class AngularResolutionSelector extends AbstractBaseSelector {

	private static final double convertToArcSec = 180.D / Math.PI * 3600.0D;
	private static final String LAMBDA = Constants.LIGHT_SPEED + " / ( REPR_FREQ * 1E9 )";
	
	public AngularResolutionSelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		return null;
	}

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date, ArrayConfiguration arrConf) {
		double maxBL = arrConf.getMaxBaseline();
		double lambda = Constants.LIGHT_SPEED / (sb.getRepresentativeFrequency() * 1E9);
//		System.out.println("maxBL: " + maxBL + " - lambda: " + lambda);
		if (arrConf.getArrayType().compareTo(ArrayType.TWELVE_M) == 0) {
			if (sb.getSchedulingConstraints().getMinAngularResolution() == 0 
					|| sb.getSchedulingConstraints().getMaxAngularResolution() == 0)
				return true; //cycle 1 sb have this issue
//			System.out.println(sb.getSchedulingConstraints().getMinAngularResolution() + " <= " + (lambda / maxBL * convertToArcSec) + " <= " + sb.getSchedulingConstraints().getMaxAngularResolution());
			if (sb.getSchedulingConstraints().getMinAngularResolution() <= (lambda / maxBL * convertToArcSec)
					&& sb.getSchedulingConstraints().getMaxAngularResolution() >= (lambda / maxBL * convertToArcSec))
				return true;
			return false;
		}
		return true;
		//sb.getSchedulingConstraints().getMinAngularResolution() <= (Constants.LIGHT_SPEED / sb.getRepresentativeFrequency() * 1E9)
	}

//	@Override
//	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//		Conjunction retVal = Restrictions.conjunction();
//		double maxBL = arrConf.getMaxBaseline();
//		retVal.add(Restrictions.sqlRestriction("MIN_ANG_RESOLUTION <= " + lambda + " / " + maxBL + " * " + convertToArcSec));
//		retVal.add(Restrictions.sqlRestriction("MAX_ANG_RESOLUTION >= " + lambda + " / " + maxBL + " * " + convertToArcSec));
////		retVal.add(Restrictions.leProperty("schedulingConstraints.minAngularResolution" , lambda + " / " + maxBL + " * " + convertToArcSec));
////		retVal.add(Restrictions.geProperty("schedulingConstraints.maxAngularResolution" , lambda + " / " + maxBL + " * " + convertToArcSec));
//		return retVal;
//	}
	
}
