/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2009
 * (c) Associated Universities Inc., 2009
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
 * File SchedulingMasterComponentImpl.java
 */
package alma.scheduling.AlmaScheduling;

import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.SchedulingException;


/**
 * A convenience class to hold the various ??Status entities we use
 * 
 * @author dclarke
 * @version $Id: StatusEntityQueueBundle.java,v 1.5 2010/08/05 15:27:29 dclarke Exp $
 */
public class StatusEntityQueueBundle {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	private final ProjectStatusQueue projectStatusQueue;
	private final OUSStatusQueue ousStatusQueue;
	private final SBStatusQueue sbStatusQueue;
    /* End of Fields 
     * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * Construct this bundle from the supplied arguments
	 * 
	 * @param projectStatusQueue
	 * @param ousStatusQueue
	 * @param sbStatusQueue
	 */
	public StatusEntityQueueBundle(ProjectStatusQueue projectStatusQueue,
			                       OUSStatusQueue ousStatusQueue,
			                       SBStatusQueue sbStatusQueue) {
		super();
		this.projectStatusQueue = projectStatusQueue;
		this.ousStatusQueue = ousStatusQueue;
		this.sbStatusQueue = sbStatusQueue;
	}
	
	/**
	 * Construct this bundle from scratch
	 */
	public StatusEntityQueueBundle(AcsLogger logger) {
		this(new ProjectStatusQueue(logger),
		     new OUSStatusQueue(logger),
			 new SBStatusQueue(logger));
	}
    /* End of Construction 
     * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Access to the queues
	 * ================================================================
	 */
	/**
	 * @return the ousStatusQueue
	 */
	public OUSStatusQueue getOUSStatusQueue() {
		return ousStatusQueue;
	}

	/**
	 * @return the projectStatusQueue
	 */
	public ProjectStatusQueue getProjectStatusQueue() {
		return projectStatusQueue;
	}

	/**
	 * @return the sbStatusQueue
	 */
	public SBStatusQueue getSBStatusQueue() {
		return sbStatusQueue;
	}
    /* End of Access to the queues 
     * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Shortcut access to the entities in the queues
	 * ================================================================
	 */
	/**
	 * @param ref A reference to the desired ProjectStatusI
	 * @return The ProjectStatusI referred to or null if there is no such entity.
	 */
	public synchronized ProjectStatusI get(ProjectStatusRefT ref) {
		return getProjectStatusQueue().get(ref.getEntityId());
	}
	
	/**
	 * @param refs References to the desired ProjectStatusis
	 * @return The ProjectStatusIs referred to.
	 */
	public synchronized ProjectStatusI[] get(ProjectStatusRefT[] refs) {
		return getProjectStatusQueue().get(refs);
	}
	
	/**
	 * @param ref A reference to the desired OUSStatusI
	 * @return The OUSStatusI referred to or null if there is no such entity.
	 */
	public synchronized OUSStatusI get(OUSStatusRefT ref) {
		return getOUSStatusQueue().get(ref.getEntityId());
	}
	
	/**
	 * @param refs References to the desired OUSStatusIs
	 * @return The OUSStatusIs referred to.
	 */
	public synchronized OUSStatusI[] get(OUSStatusRefT[] refs) {
		return getOUSStatusQueue().get(refs);
	}
	
	/**
	 * @param ref A reference to the desired SBStatusI
	 * @return The SBStatusI referred to or null if there is no such entity.
	 */
	public synchronized SBStatusI get(SBStatusRefT ref) {
		return getSBStatusQueue().get(ref.getEntityId());
	}
	
	/**
	 * @param refs References to the desired SBStatusIs
	 * @return The SBStatusIs referred to.
	 */
	public synchronized SBStatusI[] get(SBStatusRefT[] refs) {
		return getSBStatusQueue().get(refs);
	}
    /* End of Shortcut access to the entities in the queues 
     * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Queue management
	 * ================================================================
	 */

	public void updateWith(ProjectStatusQueue newQ) {
		getProjectStatusQueue().updateWith(newQ);
	}

	public void updateWith(OUSStatusQueue newQ) {
		getOUSStatusQueue().updateWith(newQ);
	}

	public void updateWith(SBStatusQueue newQ) {
		getSBStatusQueue().updateWith(newQ);
	}

	public void updateWith(StatusEntityQueueBundle newQs) {
		updateWith(newQs.getProjectStatusQueue());
		updateWith(newQs.getOUSStatusQueue());
		updateWith(newQs.getSBStatusQueue());
	}

    public void updateIncrWith(ProjectStatusQueue newQ) {
        getProjectStatusQueue().updateIncrWith(newQ);
    }

    public void updateIncrWith(OUSStatusQueue newQ) {
        getOUSStatusQueue().updateIncrWith(newQ);
    }

    public void updateIncrWith(SBStatusQueue newQ) {
        getSBStatusQueue().updateIncrWith(newQ);
    }
	
	public void updateIncrWith(StatusEntityQueueBundle newQs) {
        updateIncrWith(newQs.getProjectStatusQueue());
        updateIncrWith(newQs.getOUSStatusQueue());
        updateIncrWith(newQs.getSBStatusQueue());	    
	}

    public void remove(ProjectStatusQueue that) {
        getProjectStatusQueue().remove(that);
    }

    public void remove(OUSStatusQueue that) {
        getOUSStatusQueue().remove(that);
    }

    public void remove(SBStatusQueue that) {
        getSBStatusQueue().remove(that);
    }
	
	public void remove(StatusEntityQueueBundle that) {
		remove(that.getProjectStatusQueue());
		remove(that.getOUSStatusQueue());
		remove(that.getSBStatusQueue());	    
	}
	
	public void clear() {
		getProjectStatusQueue().clear();
		getOUSStatusQueue().clear();
		getSBStatusQueue().clear();	    
	}
    /* Queue management
     * ============================================================= */

    
    
	/*
	 * ================================================================
	 * Status updating
	 * ================================================================
	 */

	/**
	 * Refresh the status information for the project with the given
	 * UID. <em>Note:</em> This takes an ObsProject ID, not a
	 * ProjectStatus ID. 
	 * 
	 * @param projectId
	 * @param factory
	 * @throws SchedulingException 
	 */
	public void refreshProject(String projectId,
			                   AbstractStatusFactory factory)
			throws SchedulingException {
		final ProjectStatusI ps = getProjectStatusQueue()
		                 .getStatusFromProjectId(projectId);
		
		if (ps != null) {
			forgetAllAboutProject(ps);
			try {
				loadProjectStatus(ps.getUID(), factory);
			} catch (IndexOutOfBoundsException e) {
				throw new SchedulingException(
						String.format("Internal Error in refreshProject(%s)",
								projectId), e);
			}
		}
	}
	
	private void loadProjectStatus(String                psId,
                                   AbstractStatusFactory factory)
			throws IndexOutOfBoundsException, SchedulingException {
		final ProjectStatusI ps = factory.createProjectStatus(psId);
		getProjectStatusQueue().add(ps);
		loadOUSStatus(ps.getObsProgramStatus());
	}

	private void loadOUSStatus(OUSStatusI ousStatus)
			throws IndexOutOfBoundsException, SchedulingException {
		getOUSStatusQueue().add(ousStatus);
		for (final OUSStatusI childOUSS : ousStatus.getOUSStatus()) {
			loadOUSStatus(childOUSS);
		}
		for (final SBStatusI childSBS : ousStatus.getSBStatus()) {
			getSBStatusQueue().add(childSBS);
		}
	}

	private void forgetAllAboutProject(ProjectStatusI ps) {
		getProjectStatusQueue().remove(ps.getUID());
		forgetAllAboutOUSStatus(ps.getObsProgramStatusRef().getEntityId());
	}
	
	private void forgetAllAboutOUSStatus(String oussId) {
		final OUSStatusI ouss = getOUSStatusQueue().get(oussId);
		
		if (ouss != null) {
			getOUSStatusQueue().remove(oussId);
			final OUSStatusChoice choice = ouss.getOUSStatusChoice();
			for (final OUSStatusRefT childOUSSRef : choice.getOUSStatusRef()) {
				forgetAllAboutOUSStatus(childOUSSRef.getEntityId());
			}
			for (final SBStatusRefT childSBSRef : choice.getSBStatusRef()) {
				getSBStatusQueue().remove(childSBSRef.getEntityId());
			}
		}
	}
    /* Status updating
     * ============================================================= */
}
