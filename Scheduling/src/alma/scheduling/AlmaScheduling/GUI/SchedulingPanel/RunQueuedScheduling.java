package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import alma.acs.container.ContainerServices;
import alma.scheduling.MasterSchedulerIF;

public class RunQueuedScheduling implements Runnable {
    private ContainerServices container;
    private MasterSchedulerIF masterScheduler = null;
    private String[] sb_ids;
    private String arrayname;
    
    public RunQueuedScheduling(ContainerServices cs, String[] ids, String array){
        sb_ids = ids;
        arrayname = array;
        container = cs;
        getMS();
    }
    private void  getMS(){
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    private void releaseMS(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        if(masterScheduler == null) {
            System.out.println("NO Connection to MasterScheduler. Cannot schedule");
            return;
        }
        try {
            masterScheduler.startQueuedScheduling(sb_ids, arrayname);
        } catch(Exception e) {
            releaseMS();
            e.printStackTrace();
        }
    }
    
    public void stop() {
        try {
            container.releaseComponent(masterScheduler.name());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
