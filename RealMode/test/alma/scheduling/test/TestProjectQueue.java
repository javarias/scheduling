
package alma.scheduling.test;

import java.util.Vector;
import alma.scheduling.project_manager.ProjectQueue;
import alma.scheduling.define.SProject;
import alma.entity.xmlbinding.obsproject.ObsProject;

public class TestProjectQueue {
    private ProjectQueue pq;
    
    public static void main(String[] args){ 
        System.out.println("SCHED_TEST: ProjectQueue test.");
        
        ProjectQueue pq = new ProjectQueue();
        
        SProject sp = new SProject();
        
        System.out.println("SCHED_TEST: addProject(SProject)");
        pq.addProject(sp);
        
        sp = new SProject();
        
        System.out.println("SCHED_TEST: addProject(SProject)");
        pq.addProject(sp);
        
        System.out.println("SCHED_TEST: getProject(int)");
        sp = pq.getProject(1);
        
        System.out.println("SCHED_TEST: getProject() ");
        sp = pq.getProject();
        
        System.out.println("SCHED_TEST: queueToVector() ");
        Vector v = pq.queueToVector();
        
        System.out.println("SCHED_TEST: isProjectComplete(SProject)");
        boolean res = pq.isProjectComplete(sp);

        System.out.println("SCHED_TEST: getCompletedProjects() ");
        String[] s = pq.getCompletedProjects();
        
        System.out.println("SCHED_TEST: getQueueSize()");
        int i = pq.getQueueSize();
        
    }
}
