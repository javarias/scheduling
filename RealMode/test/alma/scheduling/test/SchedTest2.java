/**
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
import java.lang.Thread;
import alma.acs.component.client.ComponentClient;
import alma.scheduling.master_scheduler.MasterScheduler;
import alma.entity.xmlbinding.schedblock.SchedBlock; 
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT; 
/**
 *  Tests selectSB function.
 *
 *  @author Sohaila Roberts
 */
public class SchedTest2 {
    private MasterScheduler masterScheduler;
    private SchedBlock sb;
    private SchedBlockEntityT sb_entity;
    
    public SchedTest2(MasterScheduler ms) {
        this.masterScheduler = ms;
        masterScheduler.initialize();
        masterScheduler.execute();
    }

    public void stop() {
        masterScheduler.cleanUp();
    }

    public void testSelectSB() {
        masterScheduler.selectSB();
    }
    
    public static void main(String[] args) {
        try {
            ComponentClient client = new ComponentClient(
                Logger.getLogger("SchedTest2"), 
                    "corbaloc::" + InetAddress.getLocalHost().getHostName() + ":3000/Manager",
                        "SchedTest2");
                        
            MasterScheduler ms = new MasterScheduler();
            ms.setComponentName("SchedTest2");
            ms.setContainerServices(client.getContainerServices());
            SchedTest2 test2 = new SchedTest2(ms);
            try {
                for(int i=0; i < 6; i++) {
                    test2.testSelectSB();
                    Thread.sleep(1000*20);
                }
            } catch(InterruptedException e) {}
            test2.stop();
        } catch (Exception e) {
            System.err.println("EXCEPTION: "+ e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}
