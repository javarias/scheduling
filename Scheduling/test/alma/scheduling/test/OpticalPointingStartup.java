package alma.scheduling.test;

import alma.exec.operatorbase.portable.extension.BatchSequence;
import alma.exec.operatorbase.portable.extension.BatchSequenceController;
import alma.exec.operatorbase.portable.tasks.ExecTask;

import java.io.File;

public class OpticalPointingStartup extends BatchSequence {
    public OpticalPointingStartup(BatchSequenceController ctrl) {
        super(ctrl);
    }

    public void autoConfigure() throws Throwable{}

    public void exectaskaction() throws Throwable{

	ctrl.runTask(ctrl.getCommands().giveStartAcs());
	ctrl.runTask(ctrl.getConnectivity().givePrepareConnectivity());
	ctrl.runTask(ctrl.getGui().giveUpdateGuiWithMaci());
	ctrl.runTask(ctrl.getIntelligence().giveStartAcsSurveillance());
	ctrl.runTask(ctrl.getIntelligence().giveStartCorbacsSurveillance());
	ctrl.runTask(ctrl.getIntelligence().giveStartNotificationChannelSurveillance());
	//up the time out to 90 seconds
	ctrl.getData().properties.setProperty("StartContainerTask:Timeout", "90000");
	ctrl.runTask(ctrl.getCommands().giveStartAllContainers());

	//ctrl.runTask(ctrl.getCommands().giveBringAlmaToRunlevel("Alma_Operational"));
	/////////////Ok for prologue to stop now
	File f1= new File("acsStartedByOMC");
	if(f1.createNewFile()){
	    System.out.println("acsStartedByOMC created");
	}
	    //the happy
	//else 	
	    //not so happy...

	/////////////Needed to shut down everything
	File f2 = new File("ShutdownAllowed");
	while(!f2.exists());
	
	if(f2.exists()) {
		ctrl.runTask(ctrl.getCommands().giveBringAlmaToRunlevel("Alma_Shutdown"));
		ctrl.runTask(ctrl.getCommands().giveStopAllContainers());
        	ctrl.runTask(ctrl.getCommands().giveStopAcs());
		File f3= new File("acsStoppedByOMC");
		if(f3.createNewFile()){; 
		    System.out.println("acsStoppedByOMC created");
		}
	        ctrl.runTask(ctrl.giveShutdownMain());
	}
    }
}
