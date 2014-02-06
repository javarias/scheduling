package alma.scheduling.algorithm.observatory;

import java.util.Collection;
import java.util.Date;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ArrayTypeSelector extends AbstractBaseSelector {

	public ArrayTypeSelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		throw new RuntimeException("Not implemented");
	}

//	@Override
//	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//		if (arrConf.getArrayType() == null)
//			return null;
//		Criterion retval = Restrictions.eq("obsUnitControl.arrayRequested", arrConf.getArrayType());
//		return retval;
//	}

	@Override
	public String toString() {
		return this.getClass().toString();
	}

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date,
			ArrayConfiguration arrConf) {
		if (sb.getObsUnitControl().getArrayRequested().compareTo(arrConf.getArrayType()) == 0)
			return true;
		return false;
	}

	
	
}
