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
 * File SchedTest1.java
 * 
 */
package alma.scheduling.test;

import java.net.InetAddress;
import java.util.logging.Logger;
import alma.acs.component.client.ComponentClient;
import alma.scheduling.master_scheduler.MasterScheduler;
import alma.scheduling.master_scheduler.ALMAArchive;
import alma.entity.xmlbinding.schedblock.SchedBlock; 
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT; 
import alma.entity.xmlbinding.execblock.ExecBlock; 
import alma.entity.xmlbinding.execblock.ExecBlockEntityT; 
import alma.entity.xmlbinding.execblock.ExecBlock; 
import alma.entity.xmlbinding.execblock.ExecBlockEntityT;
import alma.entity.xmlbinding.obsproject.types.SchedStatusT;
import alma.entity.xmlbinding.obsproject.ObsUnitControl;

/**
 *  Tests creating a SchedBlock & its execution block, changing its status 
 *  and storing it all in the archive.
 *
 *  @author Sohaila Roberts
 */
public class SchedTest1 {
    private MasterScheduler masterScheduler;
    private ALMAArchive archive;
    private SchedBlock sb;
    private SchedBlockEntityT sb_entity;
    private ExecBlock exec;
    private ExecBlockEntityT exec_entity;
    
    public SchedTest1(MasterScheduler ms) {
        this.masterScheduler = ms;
        masterScheduler.initialize();
        masterScheduler.execute();
        archive = masterScheduler.getArchive();
    }

    /**
     *  Creates a SchedBlock, assigns it a unique id, creates an ObsUnitControl
     *  then sets the status of the SchedBlock and stores it in the archive.
     */
    public void storeSB() {
        sb = new SchedBlock();
        sb_entity = new SchedBlockEntityT();
        masterScheduler.assignId(sb_entity);
        sb.setSchedBlockEntity(sb_entity);
        ObsUnitControl ouc = new ObsUnitControl();
        ouc.setSchedStatus(SchedStatusT.WAITING);
        sb.setObsUnitControl(ouc);
        archive.store(sb, sb_entity);
        System.out.println("SB Stored");
    }

    /**
     *  Returns the status of the SchedBlock
     *  @return String The status of the scheduling block.
     */
    public String getSBStatus() {
        String res = "";
        res = sb.getObsUnitControl().getSchedStatus().toString();
        return res;
    }

    /**
     *  Sets the status of the SchedBlock
     *  @param block The SchedBlock that will have its status changed.
     *  @param s The status type that will be set to the SchedBlock.
     */
    public void setSBStatus(SchedBlock block, SchedStatusT s) {
        ObsUnitControl ouc = block.getObsUnitControl();
        ouc.setSchedStatus(s);
        block.setObsUnitControl(ouc);
    }

    /**
     *  Creates an ExecBlock, assigns it to a particular scheduling block,
     *  stores the execblock in the archive and updates the corresponding 
     *  scheduling block in the archive.
     */
    public void storeExecRec() {
        exec = new ExecBlock();
        exec_entity = new ExecBlockEntityT();
        masterScheduler.assignId(exec_entity);
        exec.setExecBlockEntityT(exec_entity);
        exec.setSchedBlockId(sb.getSchedBlockEntity().getEntityId());
        //exec.setSchedBlockId(sb.getId()); //will be this when using sched block wrapper clases
        setSBStatus(sb, SchedStatusT.READY);
        archive.store(exec, exec_entity);
        archive.update(sb, sb.getSchedBlockEntity());
        System.out.println("exec Stored");
    }
    
    public void stop() {
        masterScheduler.cleanUp();
    }
    
    public static void main(String[] args) {
        try {
            ComponentClient client = new ComponentClient(
                Logger.getLogger("SchedTest1"), 
                    "corbaloc::" + InetAddress.getLocalHost().getHostName() + ":3000/Manager",
                        "SchedTest1");
                        
            MasterScheduler ms = new MasterScheduler();
            ms.setComponentName("SchedTest1");
            ms.setContainerServices(client.getContainerServices());
            SchedTest1 test1 = new SchedTest1(ms);
            test1.storeSB();
            test1.storeExecRec();
            test1.stop();
        } catch (Exception e) {
            System.err.println("EXCEPTION: "+ e.toString());
            System.exit(1);
        }
        System.exit(0);
    }
}
