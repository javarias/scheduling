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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.omg.CORBA.portable.IDLEntity;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.acs.exceptions.AcsJException;
import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.AudienceLogger.Audience;
import alma.acs.nc.AcsEventPublisher;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.SessionT;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.entity.xmlbinding.valuetypes.ExecBlockRefT;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.constants.Subsystem;
import alma.pipelineql.QlDisplayManager;
import alma.scheduling.EndSessionEvent;
import alma.scheduling.SchedulingException;
import alma.scheduling.StartSessionEvent;
import alma.scheduling.array.executor.Utils;
import alma.scheduling.array.executor.services.Services;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.utils.AudienceFlogger;
import alma.scheduling.utils.Constants;
import alma.scheduling.utils.ErrorHandling;
import alma.scheduling.utils.FakeAudienceFlogger;
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;
import alma.statearchiveexceptions.wrappers.AcsJNoSuchEntityEx;
import alma.statearchiveexceptions.wrappers.AcsJNullEntityIdEx;
import alma.statearchiveexceptions.wrappers.AcsJStateIOFailedEx;

/**
 * Keep track of observing sessions for an array & invoke Quicklook as
 * appropriate.
 * 
 * @author dclarke
 * $Id: SessionManager.java,v 1.13 2012/12/04 00:16:21 javarias Exp $
 */
public class SessionManager {

	/*
	 * ================================================================
	 * Statics
	 * ================================================================
	 */
	private final static DateFormat dateFormat =
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	/* End Statics
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
    private ContainerServices containerServices;
    private final AcsLogger logger;
    private final AudienceFlogger operatorLog;
    private QlDisplayManager quicklook;
    private ModelAccessor model;
    private AcsEventPublisher<IDLEntity> sched_nc;
    private boolean newSessionPerExecution = false;

    private String arrayName;

	private IDLEntityRef currentSessionRef;
	private IDLEntityRef currentSB;
	private String       currentTitle;
	private List<String> executionIds;
	private boolean      useQuickLook;
	private SessionT     currentSession;
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
        this.logger      = cs.getLogger();
        this.operatorLog = new AudienceFlogger(this.logger, Audience.OPERATOR);
        this.quicklook   = getPipelineComponents();
        this.sched_nc    = getNotificationChannel();
        this.model       = services.getModel();
        this.newSessionPerExecution = newSessionPerExecution;
        
        this.arrayName = arrayName;
        
    	currentSessionRef = null;
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
        		containerServices.getDefaultComponent(Constants.QUICKLOOK_IF);
            result = alma.pipelineql.QlDisplayManagerHelper.narrow(cobj);
            logger.info(String.format(
            		"Succesfully connected %s to Quicklook",
            		arrayName));
            operatorLog.info("Succesfully connected %s to Quicklook",
            		arrayName);
        } catch (AcsJContainerServicesEx e) {
            ErrorHandling.severe(logger,
            		String.format("Error trying to connect %s to QuickLook - %s",
            				arrayName, e.getMessage()),
            		e);
            operatorLog.severe("Could not connect %s to Quicklook",
            		arrayName);
            result = null;
        }
        
        return result;
    }
    
    private void startQlSession(IDLEntityRef session, IDLEntityRef sb, String array, String title, int attempts) {
    	try {
			quicklook.startQlSession(session, sb, array, title);
		} catch (Exception e) {
			if (attempts > 1) {
				// Reset the quicklook connection and try again
				quicklook = getPipelineComponents();
				startQlSession(session, sb, array, title, attempts-1);
			} else {
				final String message = String.format("Error starting observing session %s on %s - %s",
						                             title, array, e.getMessage());
				ErrorHandling.warning(logger, message, e);
				quicklook = null;
			}
		}
    }
    
    private void endQlSession(IDLEntityRef session, IDLEntityRef sb, String array, String title, int attempts) {
    	try {
			quicklook.endQlSession(session, sb);
		} catch (Exception e) {
			if (attempts > 1) {
				// Reset the quicklook connection and try again
				quicklook = getPipelineComponents();
				endQlSession(session, sb, array, title, attempts-1);
			} else {
				final String message = String.format("Error ending observing session %s on %s - %s",
						                             title, array, e.getMessage());
				ErrorHandling.warning(logger, message, e);
				quicklook = null;
			}
		}
    }

    
    /**
     * Get the notification channel we're going to use
     */
	private AcsEventPublisher<IDLEntity> getNotificationChannel() {
		AcsEventPublisher<IDLEntity> result = null;
		
		try {
			result = containerServices.createNotificationChannelPublisher(alma.scheduling.CHANNELNAME_SCHEDULING.value, IDLEntity.class);
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
			sched_nc.publishEvent(event);
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
			projectCode = "codeless ObsProject";
		}
		try {
			if (sb.getName().equals("")) {
				sbName = "unnamed SchedBlock";
			}
		} catch (NullPointerException ex) {
			sbName = "unnamed SchedBlock";
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
        ObsUnitSet ous = null;
        try {
        	SchedBlock freshSb = model.getSchedBlockDao().findByEntityId(sb.getUid());
        	ous = freshSb.getParent();
        	model.getSchedBlockDao().hydrateObsUnitSet(ous);
        } catch (Exception ex) {
        	//HACK: The Archive Updater removed the old reference for the SchedBlock
        	//TODO: Remove this after create a proper merge strategy for SWDB
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
        	SchedBlock freshSb = model.getSchedBlockDao().findByEntityId(sb.getUid());
        	ous = freshSb.getParent();
        	model.getSchedBlockDao().hydrateObsUnitSet(ous);
        }
        
        final StateArchive stateArchive = model.getStateArchive();
        OUSStatus ouss = null;
        ouss = stateArchive.getOUSStatus(ous.getStatusEntity());

        return ouss;
	}

	/**
	 * Get the OUSStatus entity corresponding to the given id
	 * 
	 * @param id - an OUSStatus entity id
	 * @return
	 */
	private OUSStatus getOUSStatusFor(String id) {
		String[] ids = new String[1];
		ids[0] = id;
		
        final StateArchive stateArchive = model.getStateArchive();
        OUSStatus[] ousss = null;
        ousss = stateArchive.getOUSStatusList(ids);

        return ousss[0];
	}

	/**
	 * Write the modified OUSStatus object back to the StateArchive
	 * @param ouss
	 * @throws AcsJStateIOFailedEx
	 * @throws AcsJNoSuchEntityEx
	 */
	private void updateOUSStatus(OUSStatus ouss)
				throws AcsJStateIOFailedEx,
					   AcsJNoSuchEntityEx {
       final StateArchive stateArchive = model.getStateArchive();
       stateArchive.insertOrUpdate(ouss, Subsystem.SCHEDULING);
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

			currentSessionRef = new IDLEntityRef();
			currentSessionRef.entityId        = ouss.getOUSStatusEntity().getEntityId();
			currentSessionRef.partId          = Utils.genPartId(ouss);
			currentSessionRef.entityTypeName  = ouss.getOUSStatusEntity().getEntityTypeName();
			currentSessionRef.instanceVersion = "1.0";
			
			currentSession = createSessionObject(currentSessionRef, sb);
			ouss.addSession(currentSession);
			updateOUSStatus(ouss);

			currentSB      = new IDLEntityRef();
			currentSB.entityId        = sb.getUid();
			currentSB.partId          = "";
			currentSB.entityTypeName  = "SchedBlock";
			currentSB.instanceVersion = "1.0";

			useQuickLook = sb.getRunQuicklook();
			if (useQuickLook) {
				if (quicklook == null) {
					quicklook = getPipelineComponents();
				}
				startQlSession(currentSessionRef, currentSB, arrayName, currentTitle, 2);
			}

			final StartSessionEvent event = new StartSessionEvent(
					UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
					currentSessionRef,
					currentSB);
			publish(event, "start session");
			executionIds = new ArrayList<String>();
		} catch (Exception e) {
			ErrorHandling.warning(logger,
					String.format("Error starting observing session %s - %s",
							currentTitle, e.getMessage()),
							e);
			currentSB         = null;
			currentSessionRef = null;
			currentSession    = null;
		}
		
	}
    
    /**
     * Create an RefT to the given EntityT.
     * 
     * @param entity - the SBStatusEntityT to which we need a reference
     * @return
     */
    private SBStatusRefT createReferenceTo(SBStatusEntityT entity) {
    	final SBStatusRefT result = new SBStatusRefT();

    	result.setDocumentVersion(entity.getDocumentVersion());
    	result.setEntityId(entity.getEntityId());
    	result.setEntityTypeName(entity.getEntityTypeName());
    	// No need to do PartId as it's a whole entity reference.

    	return result;
    }
	
    private SessionT createSessionObject(IDLEntityRef reference, SchedBlock sb) {
		final SessionT result = new SessionT();
		result.setEntityPartId(reference.partId);
		result.setStartTime(dateFormat.format(new Date()));
		result.setEndTime("End time not known yet.");
		result.clearExecBlockRef();
		
		SBStatusRefT statusRef = createReferenceTo(sb.getStatusEntity());
		result.setSBStatusRef(statusRef);
		
		return result;
	}

	private SessionT findSession(OUSStatus ouss, String partId)
			throws SchedulingException {
		for (final SessionT session : ouss.getSession()) {
			if (session.getEntityPartId().equals(partId)) {
				return session;
			}
		}
		throw new SchedulingException(String.format(
				"Cannot find session %s in OUSStatus %s",
				partId, ouss.getOUSStatusEntity().getEntityId()));
	}
	
	private void appendExecution(String       executionId,
			                     IDLEntityRef currentSessionRef,
			                     SessionT     currentSession)
	 		throws SchedulingException,
	 		       AcsJStateIOFailedEx,
	 		       AcsJNoSuchEntityEx {

		// Get the OUSStatus for the current session
		final OUSStatus ouss = getOUSStatusFor(currentSessionRef.entityId);
		
		// Get the SessionT for the current session within the OUSStatus
		final SessionT session = findSession(
				ouss,
				currentSession.getEntityPartId());
		
		// Create the ExecBlockRef for the executionId and add
		// it to the SessionT
		final ExecBlockRefT ref = new ExecBlockRefT();
		ref.setExecBlockId(executionId);
		session.addExecBlockRef(ref);
		
		// Remember the changes in the StateArchive.
		updateOUSStatus(ouss);
	}
	
	private void bookkeepingForEndOfSession(
					IDLEntityRef currentSessionRef,
					SessionT     currentSession)
	 		throws SchedulingException,
	 		       AcsJStateIOFailedEx,
	 		       AcsJNoSuchEntityEx {

		// Get the OUSStatus for the current session
		final OUSStatus ouss = getOUSStatusFor(currentSessionRef.entityId);
		
		// Get the SessionT for the current session within the OUSStatus
		final SessionT session = findSession(
				ouss,
				currentSession.getEntityPartId());
		
		// Set the end time.
		session.setEndTime(dateFormat.format(new Date()));
		
		// Remember the changes in the StateArchive.
		updateOUSStatus(ouss);
	}
	

	/* End Utility methods
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Public Interface
	 * ================================================================
	 */
	public IDLEntityRef getCurrentSession() {
		return currentSessionRef;
	}
	
	public IDLEntityRef getCurrentSB() {
		return currentSB;
	}
	
	public void addExecution(String execId) {
		logger.info(String.format("%s.addExecution(%s)",
				arrayName, execId));
		executionIds.add(execId);
		try {
			appendExecution(execId, currentSessionRef, currentSession);
		} catch (Exception e) {
			ErrorHandling.warning(logger,
					String.format("Error adding execution %s to current observing session %s - %s",
							execId,
							currentTitle,
							e.getMessage()),
							e);
		}
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
			ObsProject op = model.getObsProjectDao().findByEntityId(projectUID);
			//HACK: Sometimes the project cannot be found because it was deleted to be refreshed
            //TODO: Remove the following 'while' after fix the data synchronization issues
			while(op == null) {
				op = model.getObsProjectDao().findByEntityId(projectUID);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					//Do nothing
				}
			}
			startObservingSession(op, sb);
		} else {
			logger.info(String.format("%s.continueObservingSession",
					arrayName));
		}

		return getCurrentSession();
	}

	public void endObservingSession() {
		if (currentSessionRef != null) {
			logger.info(String.format("%s.endObservingSession()",
					arrayName));
			try {
				bookkeepingForEndOfSession(currentSessionRef, currentSession);
				if (useQuickLook) {
					if (quicklook == null) {
						quicklook = getPipelineComponents();
					}
					endQlSession(currentSessionRef, currentSB, arrayName, currentTitle, 2);
				}
				final EndSessionEvent event = new EndSessionEvent(
						UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
						currentSessionRef,
						currentSB,
						executionIds.toArray(new String[0]));
				publish(event, "end session");
			} catch (Exception e) {
				ErrorHandling.warning(logger,
						String.format("Error ending observing session %s - %s",
								currentTitle, e.getMessage()),
								e);
			} finally {
				currentSB         = null;
				currentSessionRef = null;
				currentTitle      = "";
			}
		}
	}
	/* End Public Interface
	 * ============================================================= */
	
	
	public static void main(String args[]) {
		final FakeAudienceFlogger operatorLog =
			new FakeAudienceFlogger("Test", Audience.OPERATOR);
		final String arrayName = "Array666";
		
		// initialize()
		operatorLog.info("Connected to Weather Station Controller Component");
		operatorLog.warning("Unable to retrieve Weather Station Controller Component %s", Constants.WEATHER_STATION_IF);
		operatorLog.warning("Unable to retrieve Weather Station Controller Component %s", Constants.WEATHER_STATION_IF);
	}
}
