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
package alma.scheduling.array.executor;

import java.util.logging.Logger;

import alma.Control.ExecBlockStartedEvent;
import alma.acs.exceptions.AcsJException;
import alma.acs.util.UTCUtility;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.lifecycle.persistence.StateArchive;
import alma.scheduling.StartSessionEvent;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ModelAccessor;
import alma.scheduling.utils.LoggerFactory;
import alma.statearchiveexceptions.wrappers.AcsJInappropriateEntityTypeEx;
import alma.statearchiveexceptions.wrappers.AcsJNoSuchEntityEx;
import alma.statearchiveexceptions.wrappers.AcsJNullEntityIdEx;

/**
 * The ReadyExecutionState represents a SchedBlock that is ready for execution.
 * When the startObservation() method is called, the execution will be triggered in
 * Control, and the execution will remain in this state until the
 * processExecBlockStartedEvent() method is called. At this point the state will
 * transition to the RunningExecutionState state.
 * 
 * @author Rafael Hiriart (rhiriart@nrao.edu)
 *
 */
public class ReadyExecutionState extends ExecutionState {

    private Logger logger = LoggerFactory.getLogger(getClass());
        
    public ReadyExecutionState(ExecutionContext context) {
        super(context);
    }

    @Override
    public void startObservation() {
        // Send the StartSession event
        ModelAccessor model = context.getModel();
        SchedBlock sb = context.getSchedBlock();
        ObsUnitSet ous = sb.getParent();
        model.getSchedBlockDao().hydrateObsUnitSet(ous);
        StateArchive stateArchive = model.getStateArchive();
        OUSStatus ouss = null;
        try {
            logger.fine("state archive: " + stateArchive);
            ouss = stateArchive.getOUSStatus(ous.getStatusEntity());
        } catch (AcsJNullEntityIdEx e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AcsJNoSuchEntityEx e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AcsJInappropriateEntityTypeEx e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        IDLEntityRef sessionRef = new IDLEntityRef();
        sessionRef.entityId = ouss.getOUSStatusEntity().getEntityId();
        sessionRef.partId = Utils.genPartId(); // why?
        sessionRef.entityTypeName = ouss.getOUSStatusEntity().getEntityTypeName();
        sessionRef.instanceVersion = "1.0";
        context.setSessionRef(sessionRef);
          
        IDLEntityRef sbRef = new IDLEntityRef();
        sbRef.entityId = sb.getUid();
        sbRef.partId = "";
        sbRef.entityTypeName  = "SchedBlock";
        sbRef.instanceVersion = "1.0";
        context.setSchedBlockRef(sbRef);
        
        if (sb.getRunQuicklook()) {
            // ... run quicklook ...
        }
        
        StartSessionEvent event = new StartSessionEvent(
                UTCUtility.utcJavaToOmg(System.currentTimeMillis()),
                sessionRef, sbRef);
        try {
            context.getEventPublisher().publish(event);
        } catch (AcsJException e) {
            e.printStackTrace();
            // ... move to failed state
        }
        
        try {
            context.getControlArray().observe(sbRef, sessionRef);
        } catch (Exception e) {
            e.printStackTrace();
            // ... move to failed state
        }
        
        // blocks waiting for the ExecBlockStartedEvent
        logger.info("waiting for ExecBlockStartedEvent");
        ExecBlockStartedEvent sev = context.waitForExecBlockStartedEvent(10000);
        if (sev == null) {
            // move to the error state
        }
        
        context.setState(new RunningExecutionState(context));
        context.startObservation();
    }
}
