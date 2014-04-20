/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.algorithm.weather;

import java.util.Collection;
import java.util.Date;

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

//	@Override
//	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public boolean canBeSelected(SchedBlock sb, Date date, ArrayConfiguration arrConf) {
		double opacity = sb.getWeatherDependentVariables().getZenithOpacity();
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

	public boolean canBeSelected(SchedBlock sb, Date date) {
		return canBeSelected(sb, date);
	}
	

}
