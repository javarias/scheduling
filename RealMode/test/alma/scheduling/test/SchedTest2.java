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
 * File SchedTest2.java
 * 
 */
package alma.scheduling.test;

import java.net.InetAddress;
import java.util.logging.Logger;
import java.util.Vector;
import java.lang.Thread;
import alma.acs.component.client.ComponentClient;
import alma.scheduling.UnidentifiedResponse;
import alma.scheduling.master_scheduler.MasterScheduler;
import alma.scheduling.master_scheduler.ALMATelescopeOperator;
import alma.scheduling.master_scheduler.Message;
import alma.scheduling.master_scheduler.MasterSBQueue;
import alma.scheduling.master_scheduler.MessageQueue;
import alma.entity.xmlbinding.schedblock.SchedBlock; 
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT; 

import alma.xmlstore.Operational;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityException;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.OperationalPackage.MalformedURI;
import alma.xmlstore.ArchiveInternalError;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;


/**
 *  Tests selectSB function.
 *
 *  @author Sohaila Roberts
 */
public class SchedTest2 {
    private MasterScheduler masterScheduler;
    private SchedBlock sb;
    private SchedBlockEntityT sb_entity;
    private static EntitySerializer entitySerializer;
    
    public SchedTest2(ContainerServices c) {
        this.masterScheduler = new MasterScheduler();
        masterScheduler.setComponentName("SchedTest2");
        masterScheduler.setContainerServices(c);
        masterScheduler.initialize();
        masterScheduler.execute();
    }

    public void stop() {
        masterScheduler.cleanUp();
    }

    public void testSelectSB() {
        ALMATelescopeOperator operator = masterScheduler.getOperator();
        MasterSBQueue queue = masterScheduler.getSBQueue();

        Vector idList = queue.getAllUid();
        Message message = new Message();
        masterScheduler.assignId(message.getMessageEntity());
        System.out.println("New message = "+message.toString());
        String m_id = message.getMessageId();
        int size = idList.size();
        String[] ids = new String[size];
        for(int i = 0; i < size; i++) {
            ids[i] = (String)idList.elementAt(i);
        }
        String selectedSB = operator.selectSB(ids, m_id);
        System.out.println("Selected SB = "+ selectedSB);
    }


    public static void populateArchive(ContainerServices c) {
        entitySerializer = EntitySerializer.getEntitySerializer(Logger.getLogger("serializer"));
        Operational archive = null;
        try {
            archive = alma.xmlstore.OperationalHelper.narrow(c.getComponent("OPERATIONAL_ARCHIVE"));
            for(int i=0; i < 5; i++) {
                archive.update( createSB(c) );
            }
        } catch(ContainerException e) {
        } catch(MalformedURI e) {
        } catch(ArchiveInternalError e) {
        }

    }

    private static XmlEntityStruct createSB(ContainerServices c) {
        try {
            SchedBlock sb = new SchedBlock();
            SchedBlockEntityT sb_entity = new SchedBlockEntityT();
            c.assignUniqueEntityId(sb_entity);
            sb.setSchedBlockEntity(sb_entity);
            return entitySerializer.serializeEntity(sb);
        } catch (ContainerException e) {
        } catch (EntityException e) {
        }
        return null;
    }
    
    public static void main(String[] args) {
        try {
            String name = "Test2";
            String managerLoc = System.getProperty("ACS.manager");
            ComponentClient client = new ComponentClient(
                    null, managerLoc, name);   
            // Bad fix to populate the archive with 5 sched blocks.            
            SchedTest1.populateArchive(client.getContainerServices());
            ContainerServices cs = client.getContainerServices();            
            //MasterScheduler ms = new MasterScheduler();
            //ms.setComponentName("SchedTest2");
            //ms.setContainerServices(client.getContainerServices());
            SchedTest2 test2 = new SchedTest2(cs);
            //try {
                //for(int i=0; i < 6; i++) {
                    test2.testSelectSB();
                  //  Thread.sleep(1000*5);
                //}
            //} catch(InterruptedException e) {}
            test2.stop();
        } catch (Exception e) {
            System.err.println("EXCEPTION: "+ e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}
