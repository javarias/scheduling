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
 * File SchedTest6.java
 * 
 */
package alma.scheduling.test;

import java.net.InetAddress;
import java.util.logging.Logger;
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

import alma.scheduling.master_scheduler.ALMAArchive;
import alma.acs.component.client.ComponentClient;
/**
 *  This class retrieves all the SchedBlocks currently in the archive.
 *  Which is 5 SchedBlocks coz it first creates them and stores them 
 *  in the archive.
 *
 *  @author Sohaila Roberts
 */
public class SchedTest6 {
    private ALMAArchive archive;
    private EntitySerializer entitySerializer;
    private ContainerServices containerServices;
    
    public SchedTest6(ContainerServices c) {
        this.containerServices = c;
        archive = new ALMAArchive(true, c);
        System.out.println("Archive Created");
    }
    

    public void populateArchive() {
        entitySerializer = EntitySerializer.getEntitySerializer(Logger.getLogger("serializer"));
        try {
            Operational archive = alma.xmlstore.OperationalHelper.narrow(
                containerServices.getComponent("OPERATIONAL_ARCHIVE"));
            for(int i=0; i < 5; i++) {
                archive.update( createSB() );
                System.out.println("Stored in archive");
            }
        } catch(ContainerException e) {
            System.out.println(e.toString());
        } catch(MalformedURI e) {
            System.out.println(e.toString());
        } catch(ArchiveInternalError e) {
            System.out.println(e.toString());
        }
    }
    
    private XmlEntityStruct createSB() {
        try {
            SchedBlock sb = new SchedBlock();
            SchedBlockEntityT sb_entity = new SchedBlockEntityT();
            containerServices.assignUniqueEntityId(sb_entity);
            sb.setSchedBlockEntity(sb_entity);
            return entitySerializer.serializeEntity(sb);
        } catch (ContainerException e) {
            System.out.println(e.toString());
        } catch (EntityException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public void getSBs() {
        System.out.println("Getting SchedBlocks out of the archive.");
        SchedBlock[] sbs = archive.getSchedBlock(); //queries the archive for all SchedBlock
        System.out.println("number of sbs "+sbs.length);
        for(int i=0; i<sbs.length; i++) {
            System.out.println(((SchedBlock)sbs[i]).toString());
        }
    }

    public static void main(String[] args) {
        try {
            ComponentClient client = new ComponentClient(Logger.getLogger("SchedTest6"), 
                    //"corbaloc::" + InetAddress.getLocalHost().getHostName() + ":3000/Manager",
                    System.getProperty("ACS.manager"),
                        "SchedTest6");
            SchedTest6 test6 = new SchedTest6(client.getContainerServices());
            test6.populateArchive();
            test6.getSBs();
        } catch(Exception e) {
            System.err.println("EXCEPTION: "+e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}
