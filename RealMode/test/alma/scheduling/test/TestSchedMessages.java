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
 * File TestSchedMessages.java
 * 
 */

package ALMA.scheduling.test;

import ALMA.scheduling.master_scheduler.Message;
import ALMA.scheduling.master_scheduler.MessageQueue;

import alma.entities.commonentity.EntityT;

/**
 * This class tests the Message.java class and the  MessageQueue.java class
 * @author Sohaila Roberts
 */
public class TestSchedMessages {
    private MessageQueue mq;
    private Message m;

    public TestSchedMessages() {
        System.out.println("SCHED_TEST: function MessageQueue()");
        mq = new MessageQueue();
        System.out.println("SCHED_TEST: function Message()");
        m = new Message();
    }

    public void testMessages() {
        System.out.println("SCHED_TEST: function MessageQueue.addMessage(Message m)");
        System.out.println("SCHED_TEST: Should get error coz it has no entity!");
        try {
            mq.addMessage(m);
        } catch(Exception e) {
            System.out.println("SCHED_TEST: "+e.toString());
        }
        System.out.println("SCHED_TEST: function Message(id, thread)");
        m = new Message("tmp id 3", new Thread("tmp id 3"));
        System.out.println("SCHED_TEST: function MessageQueue.addMessage(Message m) no error");
        try {
            mq.addMessage(m);
        } catch(Exception e) {
            System.out.println("SCHED_TEST: "+e.toString());
        }
        System.out.println("SCHED_TEST: function MessageQueue.addMessage(String id, Thread timer)");
        String id = "tmp id 2";
        mq.addMessage(id, new Thread("tmp id 2 thread"));
        System.out.println("SCHED_TEST: function MessageQueue.getMessage(id)");
        m = mq.getMessage(id);
        System.out.println("SCHED_TEST: Message = "+m.toString());
        System.out.println("SCHED_TEST: function MessageQueue.size()");
        System.out.println("SCHED_TEST: size="+mq.size());
        System.out.println("SCHED_TEST: function MessageQueue.removeMessage(id)");
        mq.removeMessage(m.getMessageId());

        System.out.println("SCHED_TEST: function Message.setReply()");
        m.setReply("reply 1");
        System.out.println("SCHED_TEST: function Message.setThread()");
        m.setThread(new Thread("reply 1 thread"));
        
        System.out.println("SCHED_TEST: Message.getMessageEntity()");
        EntityT et = m.getMessageEntity();
        System.out.println("SCHED_TEST: got entity");
        System.out.println("SCHED_TEST: Message.getMessageId()");
        System.out.println("SCHED_TEST: id = "+m.getMessageId());
        System.out.println("SCHED_TEST: function Message.getReply()");
        System.out.println("SCHED_TEST: reply = "+m.getReply());
        System.out.println("SCHED_TEST: fundtion Message.getThread()");
        Thread t = m.getThread();
        System.out.println("SCHED_TEST: got thread ");
    }

    public static void main(String[] args) {
        TestSchedMessages test = new TestSchedMessages();
        test.testMessages();
    }
}
