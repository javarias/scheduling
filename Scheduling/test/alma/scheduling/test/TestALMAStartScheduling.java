/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File TestALMAStartScheduling.java
 */
package alma.scheduling.test;

import java.util.logging.Logger;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.client.ComponentClient;

import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.NothingCanBeScheduledEvent;

import alma.xmlentity.XmlEntityStruct;

/**
  * This test assumes that the observing tool has been run and there is one project
  * with one sb in the archive.
  *
  * @author Sohaila Lucero
  */
public class TestALMAStartScheduling {
    private ContainerServices container;
    private Logger logger;
    private boolean stopCommand=false;
    private MasterSchedulerIF masterScheduler;
    
    public TestALMAStartScheduling(ContainerServices cs){
        this.container = cs;
        this.logger = cs.getLogger();
        stopCommand = false;
    }

    public void receive(NothingCanBeScheduledEvent event) {
        stopCommand = true;
    }

    public boolean getStopCommand(){
        return stopCommand;
    }

    public static void main(String[] args) {
        String name = new String("Start Scheduling Test");
        String managerLocation = System.getProperty("ACS.manager");
        try {
            ComponentClient client = new ComponentClient(null, managerLocation, name);

            TestALMAStartScheduling test = new TestALMAStartScheduling(client.getContainerServices());
            
            MasterSchedulerIF ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
                client.getContainerServices().getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"));

            ms.startScheduling(new XmlEntityStruct());                    
            

            while (!test.getStopCommand()) {
                client.getContainerServices().:
            }
            
        } catch(Exception e) {
        }
    }
}
