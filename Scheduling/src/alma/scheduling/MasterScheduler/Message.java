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
 * File Message.java
 * 
 */

package alma.scheduling.MasterScheduler;

import alma.scheduling.Define.DateTime;
/**
 * Class to hold message id, its timer and a reply. When the scheduler
 * wants a SB to schedule, either dynamically or interactively, it sets
 * a time out so that if the Telescope Operator wants to intervene they'll 
 * have time to do so.
 *
 * @author Sohaila Lucero
 */
public class Message {
    private String message_id = null;
    private String message = null;
    private Thread timer=null;
    private String reply=null;
    private DateTime timeSent;
    private DateTime timeReply;
    
    public Message() {
    }

    public Message(String id, Thread t) {
        this();
        this.timer = t;
        this.message_id = id;
    }

    /** 
     * Returns the ID of this message.
     * @return String The id.
     */
    public String getMessageId() { 
        return message_id; 
    }

    /**
     * Returns the Thread of this message.
     * @return Thread
     */
    public Thread getTimer()    { 
        return timer;    
    }

    /**
     * Returns the Reply of this message.
     * @return String
     */
    public String getReply()     { 
        return reply;     
    }

    /**
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     */
    public DateTime getTimeSent() {
        return timeSent;
    }
    
    /**
     *
     */
    public DateTime getTimeReply() {
        return timeReply;
    }
    /**
     *
     */
    public void setMessageId(String id) {
        message_id = id;
    }

    /**
     *
     */
    public void setTimer(Thread t) {
        timer = t;
    }

    /**
     *
     */
    public void setReply(String r) {
        reply = r;
    }

    /**
     *
     */
    public void setMessage(String m) {
        message = m;
    }
    
    /**
     *
     */
    public void setTimeSent(DateTime t) {
        timeSent = new DateTime(t);
    }
    
    /**
     *
     */
    public void setTimeReply(DateTime t) {
        timeReply = new DateTime(t);
    }
    
    /**
     *
     */
    public String toString() {
        return "Reply="+reply+" MessageId="+message_id;
    }
}

