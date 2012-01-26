/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.psm.util;


import java.util.List;
import java.util.logging.Logger;

import alma.scheduling.datamodel.obsproject.ObsProject;

public class Ph1mSynchronizerService implements Ph1mSynchronizer {

	private static Logger logger = Logger.getLogger(Ph1mSynchronizerService.class
			.getName());
	private String workDir = null;
	private Ph1mSynchronizerImpl ph1mImpl = null;
		
	public Ph1mSynchronizerService() {
		
	}
	
	@Override
	public void setWorkDir(String workDir){
		this.workDir = workDir;
		ph1mImpl = new Ph1mSynchronizerImpl( this.workDir );
	}

	/**
	 * 
	 * @param p
	 *            the ObsProject to be synchronized
	 * @throws IllegalArgumentException
	 *             if the project is null of the uid is invalid
	 * @throws NullPointerException
	 *             if the proposal retrieved is null
	 */
	@Override
	public void syncrhonizeProject(ObsProject p)
			throws IllegalArgumentException, NullPointerException {

	}

	@Override
	public void synchPh1m() throws IllegalArgumentException {
		this.ph1mImpl.synchPh1m();
	}

	@Override
	public List<ProposalComparison> listPh1mProposals() {
		return this.ph1mImpl.listPh1mProposals();
	}

}
