package alma.scheduling.algorithm.weather;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Criterion;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class OpacitySelector extends AbstractBaseSelector {

	public OpacitySelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canBeSelected(SchedBlock sb) {
		/*TODO: What opacity should we use? (at zenith or at source)
		 * Currently we are using opacity at source for this
		 */
		double opacity = sb.getWeatherDependentVariables().getOpacity();
        double frequency = sb.getSchedulingConstraints()
                .getRepresentativeFrequency(); // GHz
        if (frequency > 370.0){
        	if (opacity <= 0.037)
        		return true;
        }
        else if (frequency < 370.0 && frequency >= 270.0) {
        	if (opacity <= 0.061)
        		return true;
        }
        else if (frequency < 270.0) {
        	if (opacity <= 0.6)
        		return true;
        }
        return false;
	}
	

}
