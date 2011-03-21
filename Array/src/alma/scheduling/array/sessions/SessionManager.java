/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package alma.scheduling.array.sessions;

import java.util.ArrayList;
import java.util.List;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.acs.logging.AcsLogger;
import alma.acs.nc.CorbaNotificationChannel;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.lifecycle.persistence.StateArchive;
import alma.pipelineql.QlDisplayManager;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.StartSessionEvent;
import alma.scheduling.array.executor.Utils;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.utils.DSAContextFactory;
import alma.scheduling.utils.ErrorHandling;
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;
import alma.statearchiveexceptions.wrappers.AcsJNoSuchEntityEx;
import alma.statearchiveexceptions.wrappers.AcsJNullEntityIdEx;

/**
 * Keep track of observing sessions for an array & invoke Quicklook as
 * appropriate.
 * 
 * @author dclarke
 * $Id: SessionManager.java,v 1.5 2011/03/21 21:14:01 javarias Exp $
 */
public class SessionManager {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
    private ContainerServices containerServices;
    private final AcsLogger logger;
    private QlDisplayManager quicklook;
    private ModelAccessor model;
    private CorbaNotificationChannel sched_nc;
    private boolean newSessionPerExecution = false;

    private String arrayName;

	private IDLEntityRef currentSession;
	private IDLEntityRef currentSB;
	private String       currentTitle;
	private List<String> executionIds;
	private boolean      useQuickLook;
	/* End Fields
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
    public SessionManager(String            arrayName,
    		              ContainerServices cs,
    		              Services          services,
    		              boolean           newSessionPerExecution) {
        this.containerServices = cs;
        this.logger    = cs.getLogger();
        this.quicklook = getPipelineComponents();
        this.sched_nc  = getNotificationChannel();
        this.model     = services.getModel();
        this.newSessionPerExecution = newSessionPerExecution;
        
        this.arrayName = arrayName;
        
    	currentSession = null;
    	currentSB      = null;
    	currentTitle   = "";
    	useQuickLook   = false;
    }
	/* End Construction
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * External Components
	 * ================================================================
	 */
    /**
     * Have a quick look for the Quicklook... (sorry)
     */
    private QlDisplayManager getPipelineComponents() {
    	QlDisplayManager result = null;
        logger.finest(String.format(
        		"About to connect %s to QuickLook Pipeline Component",
        		arrayName));
        try {
        	final org.omg.CORBA.Object cobj =
        		containerServices.getDefaultComponent("IDL:alma/pipelineql/QlDisplayManager:1.0");
            result = alma.pipelineql.QlDisplayManagerHelper.narrow(cobj);
            logger.info(String.format(
            		"Succesfully connected %s to Quicklook",
            		arrayName));
        } catch (AcsJContainerServicesEx e) {
            ErrorHandling.severe(logger,
            		String.format("Error trying to connect %s to QuickLook - %s",
            				arrayName, e.getMessage()),
            		e);
            result = null;
        }
        
        return result;
    }
    
    /**
     * Get the notification channel we're going to use
     */
	private CorbaNotificationChannel getNotificationChannel() {
		CorbaNotificationChannel result = null;
		
		try {
			result = new CorbaNotificationChannel(
			        		alma.scheduling.CHANNELNAME_SCHEDULING.value, 
			        		containerServices);
		} catch (AcsJException e) {
            ErrorHandling.severe(logger,
            		String.format("Error trying to connect %s to notification channel - %s",
            				arrayName, e.getMessage()),
            		e);
            result = null;
		}
		
		return result;
	}

	/* End External Components
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Event Publishing
	 * ================================================================
	 */
    private void publish(org.omg.CORBA.portable.IDLEntity event,
    		             String description) {
        try {
			sched_nc.publish(event);
		} catch (AcsJException e) {
			ErrorHandling.warning(logger,
					String.format("Error publishing %s event for %s - %s",
							description,
							currentTitle,
							e.getMessage()),
							e);
		}
    }
	/* End Event Publishing
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Utility methods
	 * ================================================================
	 */
	/**
	 * Work out if the given reference refers to the given SchedBlock.
	 * 
	 * @param schedBlock - a SchedBlock
	 * @param reference - an IDLEntityRef
	 * @return <code>true</code> if they match or <code>false</code> if
	 *         they don't.
	 */
	private boolean isSameSchedBlock(SchedBlock   schedBlock,
			                         IDLEntityRef reference) {
		if (reference == null) {
			return false;
		}
		
		return schedBlock.getUid().equals(reference.entityId);
	}

	/**
	 * Work out if we should continue the same session or start a new
	 * one.
	 * 
	 * @param schedBlock - a SchedBlock
	 * @param reference - an IDLEntityRef
	 * @return <code>true</code> we should keep the same session or
	 *         <code>false</code> if we should start a new one.
	 */
	private boolean continueSession(SchedBlock   schedBlock,
			                        IDLEntityRef reference) {
		if (newSessionPerExecution) { // Should we always start a new one?
			return false;
		}
		// Otherwise keep the session going if it's the same SB
		return isSameSchedBlock(schedBlock, reference);
	}

	/**
	 * Construct a suitable title for a session of observing the given
	 * SchedBlock (which is expected to be in the given Project). If
	 * all the fields are there, this will be:
	 * 
	 * 	project-name (project-code) sb-name (sb entityId)
	 * 
	 * If the project code is not set then we use the project's entity
	 * Id. If either of the entity names are not set then we use a
	 * placeholder for them
	 * 
	 * @param project
	 * @param sb
	 * @return
	 */
	private String suitableTitleFor(ObsProject project, SchedBlock sb) {
		String projectName = project.getName();
		String projectCode = project.getCode();
		String sbName = sb.getName();
		String sbUid  = sb.getUid();
		try {
			if (project.getName().equals("")) {
				projectName = "unnamed ObsProject";
			}
		} catch (NullPointerException ex) {
			projectName = "unnamed ObsProject";
		}
		try {
			if (project.getCode().equals("")) {
				projectCode = project.getUid();
			}
		} catch (NullPointerException ex) {
			projectName = "unnamed ObsProject";
		}
		try {
			if (sb.getName().equals("")) {
				projectName = "unnamed SchedBlock";
			}
		} catch (NullPointerException ex) {
			projectName = "unnamed ObsProject";
		}
		
		return String.format("%s (%s) %s (%s)",
				             projectName,
				             projectCode,
				             sbName,
				             sbUid);
	}

	/**
	 * Get the status entity for the OUS which contains the given
	 * SchedBlock.
	 * 
	 * @param sb
	 * @return
	 * @throws AcsJNullEntityIdEx
	 * @throws AcsJNoSuchEntityEx
	 * @throws AcsJInappropriateEntityTypeEx
	 */
	private OUSStatus getOUSStatusFor(SchedBlock sb)
				throws AcsJNullEntityIdEx,
				       AcsJNoSuchEntityEx,
				       AcsJInappropriateEntityTypeEx {
        ObsUnitSet ous = sb.getParent();
        model.getSchedBlockDao().hydrateObsUnitSet(ous);
        
        final StateArchive stateArchive = model.getStateArchive();
        OUSStatus ouss = null;
        ouss = stateArchive.getOUSStatus(ous.getStatusEntity());

        return ouss;
	}
	
	/**
	 * Start a new observing session for the given SchedBlock
	 * 
	 * @param project
	 * @param sb
	 */
	private void startObservingSession(ObsProject project, SchedBlock sb) {
		logger.info(String.format("%s.startObservingSession(ObsProject %s, SchedBlock %s)",
				arrayName,
				project==null? "Null project": project.getUid(),
				sb==null? "Null SB": sb.getUid()));
		currentTitle = suitableTitleFor(project, sb);
		
		try {
			final OUSStatus ouss = getOUSStatusFor(sb);

			currentSession = new IDLEntityRef();
			currentSession.entityId        = ouss.getOUSStatusEntity().getEntityId();
			currentSession.partId          = Utils.genPartId();
			currentSession.entityTypeName  = ouss.getOUSStatusEntity().getEntityTypeName();
			currentSession.instanceVersion = "1.0";

			currentSB      = new IDLEntityRef();
			currentSB.entityId        = sb.getUid();
			currentSB.partId          = "";
			currentSB.entityTypeName  = "SchedBlock";
			currentSB.instanceVersion = "1.0";

			useQuickLook = sb.getRunQuicklook();
			if (useQuickLook) {
				quicklook.startQlSession(currentSession,
										 currentSB,
										 arrayName,
										 currentTitle);
			}

			final StartSessionEvent event = new StartSessionEvent(
					UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
					currentSession,
					currentSB);
			publish(event, "start session");
			executionIds = new ArrayList<String>();
		} catch (Exception e) {
			ErrorHandling.warning(logger,
					String.format("Error starting observing session %s - %s",
							currentTitle, e.getMessage()),
							e);
			currentSB      = null;
			currentSession = null;
		}
		
	}
	/* End Utility methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Public Interface
	 * ================================================================
	 */
	public IDLEntityRef getCurrentSession() {
		return currentSession;
	}
	
	public IDLEntityRef getCurrentSB() {
		return currentSB;
	}
	
	public void addExecution(String execId) {
		logger.info(String.format("%s.addExecution(%s)",
				arrayName, execId));
		executionIds.add(execId);
	}
	
	public IDLEntityRef observeSB(String sbId) {
		SchedBlock sb = model.getSchedBlockFromEntityId(sbId);
		return observeSB(sb);
	}

	public IDLEntityRef observeSB(SchedBlock sb) {
		if (!continueSession(sb, currentSB)) {
			// Start a new session
			endObservingSession();
			final String projectUID = sb.getProjectUid();
			final ObsProject op = model.getObsProjectDao().findByEntityId(projectUID);
			startObservingSession(op, sb);
		} else {
			logger.info(String.format("%s.continueObservingSession",
					arrayName));
		}

		return getCurrentSession();
	}

	public void endObservingSession() {
		if (currentSession != null) {
			logger.info(String.format("%s.endObservingSession()",
					arrayName));
			try {
				if (useQuickLook) {
					quicklook.endQlSession(currentSession, currentSB);
				}
				final EndSessionEvent event = new EndSessionEvent(
						UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
						currentSession,
						currentSB,
						executionIds.toArray(new String[0]));
				publish(event, "end session");
			} catch (Exception e) {
				ErrorHandling.warning(logger,
						String.format("Error ending observing session %s - %s",
								currentTitle, e.getMessage()),
								e);
			} finally {
				currentSB      = null;
				currentSession = null;
				currentTitle   = "";
			}
		}
	}
	/* End Public Interface
	 * ============================================================= */
}
