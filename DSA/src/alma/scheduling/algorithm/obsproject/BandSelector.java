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
package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class BandSelector extends AbstractBaseSelector {

	private Set<Integer> allowedBands;
	private SchedBlockDao schedBlockDao;
	
	public BandSelector(String selectorName) {
		super(selectorName);
	}

	@Override
	public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
			throws NoSbSelectedException {
		return null;
	}

	@Override
	public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
		Integer [] b = new Integer[allowedBands.size()];
		allowedBands.toArray(b);
		Criterion c = Restrictions.in("sb.representativeBand", b);
		return c;
	}
	
	public void setAllowedBands(Set<Integer> bands) {
		this.allowedBands = bands;
	}
	
	public Set<Integer> getAllowedBands() {
		return allowedBands;
	}

	public SchedBlockDao getSchedBlockDao() {
		return schedBlockDao;
	}

	public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
		this.schedBlockDao = schedBlockDao;
	}

}