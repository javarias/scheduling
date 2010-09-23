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

import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.nc.CorbaNotificationChannel;
import alma.acs.nc.Receiver;
import alma.pipelinescience.ScienceProcessingDoneEvent;
import alma.scheduling.MasterSchedulerIF;
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
    private Receiver r;
    
    public TestALMAStartScheduling(ContainerServices cs){
        this.container = cs;
        this.logger = cs.getLogger();
        stopCommand = false;
        r = CorbaNotificationChannel.getCorbaReceiver(alma.pipelinescience.CHANNELNAME_SCIPIPEMANAGER.value,
                container);
        r.attach("alma.pipelinescience.ScienceProcessingDoneEvent",this);
        r.begin();
    }

    public void receive(ScienceProcessingDoneEvent event) {
        logger.info("SCHED_TEST: got pipeline end event. ");
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
                client.getContainerServices().getComponent("SCHEDULING_MASTERSCHEDULER"));

            ms.startScheduling(new XmlEntityStruct());                    
            

            while (!test.getStopCommand()) {
                try {
                    Thread.sleep(1000);
                }catch(Exception e) {}
            }
            client.getContainerServices().releaseComponent("CONTROL_SYSTEM_COMPONENT");
            client.getContainerServices().releaseComponent("PIPELINE_SCIENCE");
            client.getContainerServices().releaseComponent("SCHEDULING_MASTERSCHEDULER");

        } catch(Exception e) {
        }
    }
}
