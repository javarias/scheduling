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
 * File SchedTest5.java
 * 
 */

package alma.scheduling.test;

import java.net.InetAddress;
import java.util.logging.Logger;
import alma.acs.component.client.ComponentClient;
import alma.scheduling.master_scheduler.MasterScheduler;
import alma.scheduling.master_scheduler.ALMADispatcher;
import alma.scheduling.define.STime;
import alma.scheduling.scheduler.Scheduler;

import alma.xmlstore.Operational;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityException;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.OperationalPackage.MalformedURI;
import alma.xmlstore.ArchiveInternalError;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.entity.xmlbinding.schedblock.SchedBlock; 
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;
/**
 *  This class test getting a SB from the queue and sending it to the
 *  dispatcher who sends it off to the control system to be executed.
 *
 *  @author Sohaila Roberts
 */
public class SchedTest5 {
    private MasterScheduler masterScheduler;
    private static EntitySerializer entitySerializer;

    public SchedTest5(MasterScheduler ms) {
        this.masterScheduler = ms;
        masterScheduler.initialize();
        masterScheduler.execute();
    }

    public void dispatchSB() {
        masterScheduler.startScheduler("dynamic");
        Scheduler s = masterScheduler.getScheduler();
        System.out.println("got scheduler");
        SchedBlock sb = s.getSB();
        System.out.println("got SB");
        ALMADispatcher d = masterScheduler.getDispatcher();
        System.out.println("got dispatcher");
        d.sendToControl(sb.getSchedBlockEntity().getEntityId(), new STime());
        
    }
    
    public void stop() {
        masterScheduler.cleanUp();
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
            ComponentClient client = new ComponentClient(
                Logger.getLogger("SchedTest5"), 
                    "corbaloc::" + InetAddress.getLocalHost().getHostName() + ":3000/Manager",
                        "SchedTest5");
            // Bad fix to populate the archive with 5 sched blocks.            
            SchedTest1.populateArchive(client.getContainerServices());
            
            MasterScheduler ms = new MasterScheduler();
            ms.setComponentName("SchedTest5");
            ms.setContainerServices(client.getContainerServices());
            SchedTest5 test5 = new SchedTest5(ms);
            test5.dispatchSB();
            test5.stop();
        } catch (Exception e) {
            System.err.println("EXCEPTION: "+e.toString());
            System.exit(1);
        }
        System.exit(0);

    }
}
