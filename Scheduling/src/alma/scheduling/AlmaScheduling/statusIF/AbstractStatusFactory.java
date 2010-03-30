/**
 * 
 */
package alma.scheduling.AlmaScheduling.statusIF;

import java.util.logging.Logger;

import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.StatusEntityQueueBundle;
import alma.scheduling.Define.SchedulingException;

/**
 * An <em>AbstractFactory</em> for Status entity proxies.
 * 
 * @author dclarke
 *
 */
public interface AbstractStatusFactory {
	/*
	 * Creation from UIDs of status entities for which we want proxies
	 * ================================================================
	 */
	public ProjectStatusI createProjectStatus(String uid)
											throws SchedulingException;
	public OUSStatusI     createOUSStatus(String uid)
											throws SchedulingException;
	public SBStatusI      createSBStatus(String uid)
											throws SchedulingException;

	/*
	 * Creation from actual status entities for which we want proxies
	 * ================================================================
	 */
	public ProjectStatusI createProjectStatus(ProjectStatus ent)
											throws SchedulingException;
	public OUSStatusI     createOUSStatus(OUSStatus ent)
											throws SchedulingException;
	public SBStatusI      createSBStatus(SBStatus ent)
											throws SchedulingException;

	/*
	 * Initialisation of the status system
	 * ================================================================
	 */
	/**
	 * Tell the various status entity proxies which resources to use
	 * for their various activites.
	 * 
	 * @param stateSystem
	 * @param entitySerializer
	 * @param entityDeserializer
	 * @param clock
	 * @param logger
	 */
	public void setStatusSystem(StateSystemOperations stateSystem,
			                    EntitySerializer      entitySerializer,
			                    EntityDeserializer    entityDeserializer,
			                    ALMAClock             clock,
			                    Logger                logger);


	/*
	 * Access to utilities held in the factory
	 * ================================================================
	 */
	public Logger getLogger();
	
    public void setStatusQueue(StatusEntityQueueBundle statusQueue);
}
