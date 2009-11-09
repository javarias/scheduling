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
/**
 * @author dclarke
 *
 */
public class RemoteStatusFactory implements AbstractStatusFactory {

	/*
	 * ================================================================
	 * Singleton pattern
	 * ================================================================
	 */
	private static AbstractStatusFactory singleton =
		new RemoteStatusFactory();
	
	/** Hide the constructor */
	private RemoteStatusFactory() { /* Empty */ }
	
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
		return new RemoteOUSStatus(uid);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createOUSStatus(alma.entity.xmlbinding.ousstatus.OUSStatus)
	 */
	public OUSStatusI createOUSStatus(OUSStatus ent) throws SchedulingException {
		return new RemoteOUSStatus(ent.getOUSStatusEntity().getEntityId());
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createProjectStatus(java.lang.String)
	 */
	public ProjectStatusI createProjectStatus(String uid) throws SchedulingException {
		return new RemoteProjectStatus(uid);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createProjectStatus(alma.entity.xmlbinding.projectstatus.ProjectStatus)
	 */
	public ProjectStatusI createProjectStatus(ProjectStatus ent) throws SchedulingException {
		return new RemoteProjectStatus(ent.getProjectStatusEntity().getEntityId());
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createSBStatus(java.lang.String)
	 */
	public SBStatusI createSBStatus(String uid) throws SchedulingException {
		return new RemoteSBStatus(uid);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory#createSBStatus(alma.entity.xmlbinding.sbstatus.SBStatus)
	 */
	public SBStatusI createSBStatus(SBStatus ent) throws SchedulingException {
		return new RemoteSBStatus(ent.getSBStatusEntity().getEntityId());
	}
	/*
	 * End of AbstractStatusFactory implementation
	 * ============================================================= */
}
