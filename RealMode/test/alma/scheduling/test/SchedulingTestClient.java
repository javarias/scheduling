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
 * File SchedulingTestClient.java
 * 
 */
package alma.scheduling.test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.logging.Level;

import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityException;

import alma.xmlentity.XmlEntityStruct;

import alma.entities.commonentity.EntityT;

// below imports found in ControlEntities.xml
import alma.entity.xmlbinding.schedblock.SchedBlock; 
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT; 
import alma.entity.xmlbinding.schedblock.ObsProcedureT;

// Archive imports
import alma.xmlstore.*;
import alma.xmlstore.OperationalPackage.*;


public class SchedulingTestClient {
	
    private ComponentClient m_componentClient;
    private ContainerServices containerServices;
    private Operational m_operArchiveComp;
    
    private SchedBlock sb;
    private SchedBlockEntityT sb_entity;
    private XmlEntityStruct xmlEntityStruct;

	private Logger m_logger;
    private EntitySerializer entitySerializer;
	
	public SchedulingTestClient() {
		String name = "SchedulingTestClient";
		m_logger = Logger.getLogger(name);
		String managerLoc = System.getProperty("ACS.manager");
		try {
			//managerLoc = "corbaloc::" + InetAddress.getLocalHost().getHostName() + ":3000/Manager";
			m_componentClient = new ComponentClient(m_logger, managerLoc, name);	
		} catch (Exception e) {
			System.out.println("created error "+e.toString());
			e.printStackTrace();
		}
	}
	
    public void setup() throws ContainerException {
    	try {
	   	    containerServices = m_componentClient.getContainerServices();
        	/** Gets operational_archive from cobs.xml **/
    		m_operArchiveComp = alma.xmlstore.OperationalHelper.narrow(
        		containerServices.getComponent("OPERATIONAL_ARCHIVE"));
            entitySerializer = EntitySerializer.getEntitySerializer(m_logger);
    	} catch (Exception e){
    		System.out.println(e.toString());    	
    	}
    }
    
    
    public XmlEntityStruct getXmlEntityStruct() {
    	return xmlEntityStruct;
    }
    
	/**
	 * Creates and empty SchedBlock that has a unique identifier gotten from the
	 * container.
	 *
	 */
 
    public void createSchedBlock() {
        try {
        	// Empty SchedBlock for now
            sb = new SchedBlock(); 
            sb_entity = new SchedBlockEntityT();
            containerServices.assignUniqueEntityId(sb_entity);
            sb.setSchedBlockEntity(sb_entity);
            // convert to xmlentitystruct
            xmlEntityStruct = entitySerializer.serializeEntity(sb, sb_entity);
        } catch (ContainerException e) {
            System.out.println("ContainerException: "+ e.toString());
        } catch(EntityException e) {
            System.out.println("EntityException: "+ e.toString());
        }
        
    }

	public void storeSchedBlock() {
		try {
			
			m_operArchiveComp.update(xmlEntityStruct);

        //} catch(EntityException e){
        //    System.err.println(e.toString());
        } catch(ArchiveInternalError e) {
            System.err.println(e.toString());
        } catch(MalformedURI e) {
            System.err.println(e.toString());
        } catch(InternalError e) {
            System.err.println(e.toString());
        }

	}
	
	public void retrieveSchedBlock() {
        try {
			XmlEntityStruct retrievedStruct = m_operArchiveComp.retrieve(xmlEntityStruct.entityId, -1);
			System.out.println(retrievedStruct.xmlString+ " " + retrievedStruct.entityId);
        } catch(ArchiveInternalError e) {
        } catch(NotFound e) {
        } catch(MalformedURI e) {
        }
	}

	public void updateSchedBlock() {
		try {
            ObsProcedureT obsT = new ObsProcedureT();
            sb.setObsProcedure(obsT);
			xmlEntityStruct = entitySerializer.serializeEntity(sb, sb_entity);
			m_operArchiveComp.update(xmlEntityStruct);
        } catch(EntityException e){
            System.err.println(e.toString());
        } catch(ArchiveInternalError e) {
            System.err.println(e.toString());
        } catch(MalformedURI e) {
            System.err.println(e.toString());
        } catch(InternalError e) {
            System.err.println(e.toString());
        }
	}

	public void removeSchedBlock() {
        try {
			m_operArchiveComp.delete(xmlEntityStruct.entityId, -1, false);
        } catch(ArchiveInternalError e) {
        } catch(NotFound e) {
        } catch(MalformedURI e) {
        }
	}

	
    public static void main(String[] args) {
        try {
            SchedulingTestClient client = new SchedulingTestClient();
            client.setup();
            System.out.println("Started");
            client.createSchedBlock();
            System.out.println("SchedBlock created: " + client.getXmlEntityStruct().xmlString);
            client.storeSchedBlock();
            System.out.println("SchedBlock Stored");
            System.out.println(client.getXmlEntityStruct().xmlString + "" + client.getXmlEntityStruct().entityId);
            client.retrieveSchedBlock();
            System.out.println("SchedBlock retrieved");
            client.updateSchedBlock();
            System.out.println("SchedBlock updated: " + client.getXmlEntityStruct().xmlString);
            client.retrieveSchedBlock();
            System.out.println("SchedBlock retrieved");
            client.removeSchedBlock();
            System.out.println("SchedBlock removed");
        } catch(Exception e) {
            System.out.println("Exception occured!"); 
            e.printStackTrace();
        }
        System.exit(0);
    }
}
