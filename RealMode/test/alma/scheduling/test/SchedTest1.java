package alma.scheduling.test;

import java.net.InetAddress;
import java.util.logging.Logger;
import alma.acs.component.client.ComponentClient;
import alma.scheduling.master_scheduler.MasterScheduler;
import alma.scheduling.master_scheduler.ArchiveProxy;
import alma.entity.xmlbinding.schedblock.SchedBlock; 
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT; 
import alma.entity.xmlbinding.execblock.ExecBlock; 
import alma.entity.xmlbinding.execblock.ExecBlockEntityT; 
import alma.entity.xmlbinding.execblock.ExecBlock; 
import alma.entity.xmlbinding.execblock.ExecBlockEntityT;
import alma.entity.xmlbinding.obsproject.types.SchedStatusT;
import alma.entity.xmlbinding.obsproject.ObsUnitControl;
//import alma.entity.xmlbinding.obsproject.ObsUnitT;

public class SchedTest1 {
    private MasterScheduler masterScheduler;
    private ArchiveProxy archive;
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

    public String getSBStatus() {
        String res = "";
        res = sb.getObsUnitControl().getSchedStatus().toString();
        return res;
    }
    public void setSBStatus(SchedBlock block, SchedStatusT s) {
        ObsUnitControl ouc = block.getObsUnitControl();
        ouc.setSchedStatus(s);
        block.setObsUnitControl(ouc);
    }

    public void storeExecRec() {
        exec = new ExecBlock();
        exec_entity = new ExecBlockEntityT();
        masterScheduler.assignId(exec_entity);
        exec.setExecBlockEntityT(exec_entity);
        exec.setSchedBlockId(sb.getSchedBlockEntity().getEntityId());
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
                        
            //SchedTest1 test1 = new SchedTest1(client);
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
