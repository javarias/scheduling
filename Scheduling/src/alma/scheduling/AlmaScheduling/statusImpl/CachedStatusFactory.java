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
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.SchedulingException;

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
	public void setStatusSystem(StateSystem stateSystem, EntitySerializer entitySerializer, EntityDeserializer entityDeserializer, ALMAClock clock, Logger logger) {
		// We still need to set up the status system for the cached
		// status entities, as we need to be able to get the things to
		// cache.
		RemoteStatusBase.setStatusSystem(stateSystem,
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
		return new RemoteOUSStatus(uid).asLocal();
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
		return new RemoteProjectStatus(uid).asLocal();
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
		return new RemoteSBStatus(uid).asLocal();
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
}
