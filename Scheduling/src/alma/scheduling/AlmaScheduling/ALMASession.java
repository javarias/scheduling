/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File ALMASession.java
 * 
 */

package alma.scheduling.AlmaScheduling;

import alma.scheduling.Define.Session;

import alma.entity.xmlbinding.session.SessionEntityT;
import alma.entity.xmlbinding.session.SessionSequence;
import alma.entity.xmlbinding.session.SessionSequenceItem;
import alma.entity.xmlbinding.session.ExecutionT;
import alma.entities.commonentity.EntityRefT;

/**
 * This class extends the Session class in the define package.
 *
 * @author Sohaila Lucero
 */
public class ALMASession extends Session {
    private alma.entity.xmlbinding.session.Session session;
    
    public ALMASession() {
        super();
        session = new alma.entity.xmlbinding.session.Session();
        SessionEntityT s_entity = new SessionEntityT();
        session.setSessionEntity(s_entity);
    }
    
    public ALMASession(alma.entity.xmlbinding.session.Session s) {
        super();
        this.session = s;
        setId(s.getSessionEntity().getEntityId());
        setStartTime(s.getStartTime());
        setEndTime(s.getEndTime());
        //setObsUnitSetId(s.getObsUnitsetReference().getEntityId());
        
    }

    /** 
     * Returns the session object.
     * @return alma.entity.xmlbinding.session.Session
     */
    public alma.entity.xmlbinding.session.Session getSession() {
        return session;
    }

    public void addExecBlockId(String s) {
        super.addExecBlockId(s);
        EntityRefT entityRef = new EntityRefT();
        entityRef.setEntityId(s);
        entityRef.setEntityTypeName("ExecBlock");
        ExecutionT exec = new ExecutionT();
        exec.setExecBlockReference(entityRef);
        SessionSequenceItem item = new SessionSequenceItem();
        item.setExecution(exec);
        SessionSequence[] seq = session.getSessionSequence();
        if((seq == null) || (session.getSessionSequenceCount() <= 0) ) { 
            // no executions yet! 
            seq = new SessionSequence[1];
            seq[0] = new SessionSequence();
            seq[0].setSessionSequenceItem(item);
            session.setSessionSequence(seq);
        } else { //there has been at least one execution! 
            SessionSequence ss = new SessionSequence();
            ss.setSessionSequenceItem(item);
            session.addSessionSequence(ss);
        }
    }
    
}
