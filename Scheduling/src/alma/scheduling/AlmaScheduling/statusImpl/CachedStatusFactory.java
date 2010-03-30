/**
 * 
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.util.logging.Logger;

import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.projectlifecycle.StateSystem;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.StatusEntityQueueBundle;
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.SchedulingException;
import alma.projectlifecycle.StateSystemOperations;

/**
 * <em>Singleton</em> implementation of the <em>Abstract Factory</em>
 * for status proxy creation. Creates proxies which hold their data
 * locally.
 * 
 * @author dclarke
 */
public class CachedStatusFactory implements AbstractStatusFactory {
	
	// TODO: (David) Refactor the interdependency between the Remote
	// and Cached status systems by abstracting the state system stuff
	// to a common superclass of CachedStatusBase and RemoteStatusBase
	// so that each strain can get the entities from the state archive
	// without reliance on the other strain.
	
	/*
	 * ================================================================
	 * Singleton pattern
	 * ================================================================
	 */
	private static AbstractStatusFactory singleton =
		new CachedStatusFactory();
	
	/** Hide the constructor */
	private CachedStatusFactory() { /* Empty */ }

    /** The status queue, statuses should be looked up here before creating one remotely */
	private StatusEntityQueueBundle statusQueue;
	
	/** 
	 * get the single instance
	 * @return AbstractStatusFactory
	 */
	public static AbstractStatusFactory getInstance() {
		return singleton;
	}
	/*
	 * End of Singleton pattern
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * AbstractStatusFactory implementation
	 * ================================================================
	 */

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#setStatusSystem(alma.projectlifecycle.StateSystem, alma.acs.entityutil.EntitySerializer, alma.acs.entityutil.EntityDeserializer, alma.scheduling.AlmaScheduling.ALMAClock, java.util.logging.Logger)
	 */
	public void setStatusSystem(StateSystemOperations stateSystem,
			                    EntitySerializer      entitySerializer,
			                    EntityDeserializer    entityDeserializer,
			                    ALMAClock             clock,
			                    Logger                logger) {
		CachedStatusBase.setStatusSystem(stateSystem,
                                         entitySerializer,
                                         entityDeserializer,
                                         clock,
                                         logger);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#getLogger()
	 */
	public Logger getLogger() {
		return RemoteStatusBase.logger;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createOUSStatus(java.lang.String)
	 */
	public OUSStatusI createOUSStatus(String uid) throws SchedulingException {
	    if (statusQueue != null) {
	        OUSStatusI status = statusQueue.getOUSStatusQueue().get(uid);
	        if (status != null) return status;
	    }
		return new CachedOUSStatus(uid);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createOUSStatus(alma.entity.xmlbinding.ousstatus.OUSStatus)
	 */
	public OUSStatusI createOUSStatus(OUSStatus ent) {
		return new CachedOUSStatus(ent);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createProjectStatus(java.lang.String)
	 */
	public ProjectStatusI createProjectStatus(String uid) throws SchedulingException {
        if (statusQueue != null) {
            ProjectStatusI status = statusQueue.getProjectStatusQueue().get(uid);
            if (status != null) return status;
        }
		return new CachedProjectStatus(uid);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createProjectStatus(alma.entity.xmlbinding.projectstatus.ProjectStatus)
	 */
	public ProjectStatusI createProjectStatus(ProjectStatus ent) {
		return new CachedProjectStatus(ent);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createSBStatus(java.lang.String)
	 */
	public SBStatusI createSBStatus(String uid) throws SchedulingException {
        if (statusQueue != null) {
            SBStatusI status = statusQueue.getSBStatusQueue().get(uid);
            if (status != null) return status;
        }
		return new CachedSBStatus(uid);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createSBStatus(alma.entity.xmlbinding.sbstatus.SBStatus)
	 */
	public SBStatusI createSBStatus(SBStatus ent) {
		return new CachedSBStatus(ent);
	}
	/*
	 * End of AbstractStatusFactory implementation
	 * ============================================================= */
	
    public void setStatusQueue(StatusEntityQueueBundle statusQueue) {
	    this.statusQueue = statusQueue;
	}
}
