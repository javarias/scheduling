/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
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
 * File Message.java
 * 
 */

package alma.scheduling.master_scheduler;

import alma.entities.commonentity.EntityT;
/**
 * Class to hold message id, its thread and a reply
 *
 * @author Sohaila Roberts
 */
public class Message {
    private EntityT messageEntity; // so we can assign a uid and a type to is
    private String type = "SelectSBMessage";
    private Thread thread=null;
    private String reply=null;
    
    public Message() {
        this.messageEntity = new EntityT();
    }
    /*
    public Message(String id) {
        this();
        this.messageEntity.setEntityId(id);
    }
    */
    /*
    public Message (Thread t) {
        this.thread = t;
    }
    */
    public Message(String id, Thread t) {
        this();
        this.thread = t;
        this.messageEntity.setEntityId(id);
    }

    public String getMessageId() { return messageEntity.getEntityId(); }
    public Thread getThread()    { return thread;    }
    public String getReply()     { return reply;     }
    
    public EntityT getMessageEntity() {
        return messageEntity;
    }
    public void setThread(Thread t) {
        thread = t;
    }
    public void setReply(String r) {
        reply = r;
    }

    public String toString() {
        return "Reply="+reply+" MessageId="+messageEntity.getEntityId() ;
    }
}

