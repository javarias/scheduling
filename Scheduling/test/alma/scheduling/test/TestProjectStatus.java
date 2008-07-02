package alma.scheduling.test;
//java
import java.util.logging.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
//junit
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
//acs
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
//alma
import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.obsproject.types.*;
import alma.entity.xmlbinding.obsproposal.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.schedblock.types.*;
import alma.scheduling.AlmaScheduling.ProjectUtil;
import alma.scheduling.Define.*;


public class TestProjectStatus extends ComponentClientTestCase {
    private Logger logger=null;

    public TestProjectStatus() throws Exception {
        super(TestProjectStatus.class.getName());
    }
    public void tearDown() throws Exception {
    }

    protected void setUp() throws Exception {
       // super.setUp();
        //logger= getContainerServices().getLogger();
        logger = Logger.getLogger("TestProjectStatusLogger");
        assertNotNull(logger);
        logger.config("TestProjectStatus: End Setup");
    }

    private ProjectStatus createProjectStatus(String filename) throws Exception{
        File xmlfile = new File(filename);
        FileReader fr = new FileReader(xmlfile);
        ProjectStatus obj = ProjectStatus.unmarshalProjectStatus(fr);
        return obj;
    }

    private ObsProject createObsProject(String filename) throws Exception {
        File xmlfile = new File(filename);
        FileReader fr = new FileReader(xmlfile);
        ObsProject obj = ObsProject.unmarshalObsProject(fr);
        return obj;
    }
    private ObsProposal createObsProposal(String filename) throws Exception {
        File xmlfile = new File(filename);
        FileReader fr = new FileReader(xmlfile);
        ObsProposal obj = ObsProposal.unmarshalObsProposal(fr);
        return obj;
    }
    private SchedBlock createSchedBlock(String filename) throws Exception {
        File xmlfile = new File(filename);
        FileReader fr = new FileReader(xmlfile);
        SchedBlock obj = SchedBlock.unmarshalSchedBlock(fr);
        return obj;
    }

    private String createProjectStatusXml(ProjectStatus ps) throws Exception {
        String xml;
        StringWriter writer = new StringWriter();
        ps.marshal(writer);
        xml = writer.toString();
        return xml;
    }
    private String createProjectXml(ObsProject proj) throws Exception {
        String xml;
        StringWriter writer = new StringWriter();
        proj.marshal(writer);
        xml = writer.toString();
        return xml;
    }

    public void testProjectStatus() throws Exception {
        //create a project status obj
        ProjectStatus ps = createProjectStatus("newxmldocs/HG/EmptyProjectStatus.xml");
        assertNotNull(ps);
        //create a project obj
        ObsProject obsproject = createObsProject("newxmldocs/HG/ObsProject.xml");
        //ObsProject obsproject = createObsProject("xmldocs/ObsProject1.xml");
        assertNotNull(obsproject);
        //create 2 SB objects
        SchedBlock[] sbs = new SchedBlock[1];
        //SchedBlock sb = createSchedBlock("xmldocs/SchedBlock1.xml");
        SchedBlock sb = createSchedBlock("newxmldocs/HG/SchedBlock0.xml");
        //System.out.println(sb.getModeName());
            
        assertNotNull(sb);
        sbs[0] = sb;
        //sb = createSchedBlock("xmldocs/SchedBlock2.xml");
        
        //assertNotNull(sb);
        //sbs[1] = sb;
        //assertNotSame(sbs[0].getSchedBlockEntity().getEntityId(), 
        //              sbs[1].getSchedBlockEntity().getEntityId());


        DateTime datetime = DateTime.currentSystemTime();
        //get orig project

        Project project1 = new ProjectUtil(logger).map(obsproject, sbs, ps, datetime);
        assertNotNull(project1);
        SB[] foos = project1.getAllSBs();
        /*
        for(int i=0;i< foos.length; i++) {
            System.out.println("RA: "+foos[i].getTarget().getCenter().getRa());
        }*/

        //get orig PS
        ps = new ProjectUtil(logger).map(project1, datetime);
        assertNotNull(ps);
        
        String psXmlString = createProjectStatusXml(ps);

    }

    public void testObsProjectMapping() throws Exception{
        DateTime datetime = DateTime.currentSystemTime();
        ProjectStatus ps = createProjectStatus("newxmldocs/HG/EmptyProjectStatus.xml");
        assertNotNull(ps);
        SchedBlock[] sbs = new SchedBlock[1];
        ObsProject obsproject = createObsProject("newxmldocs/HG/ObsProject.xml");
        SchedBlock sb = createSchedBlock("newxmldocs/HG/SchedBlock0.xml");
        sbs[0] = sb;
        Project project1 = new ProjectUtil(logger).map(obsproject, sbs, ps, datetime);
    }

    /*
    public void testObsProjectUpdating() throws Exception {
        DateTime datetime = DateTime.currentSystemTime();
        ProjectStatus ps = createProjectStatus("newxmldocs/HG/EmptyProjectStatus.xml");
        assertNotNull(ps);
        SchedBlock[] sbs = new SchedBlock[1];
        ObsProject obsproject = createObsProject("newxmldocs/HG/ObsProject.xml");
        SchedBlock sb1 = createSchedBlock("newxmldocs/HG/SchedBlock0.xml");
        sbs[0] = sb1;
        Project project1 = ProjectUtil.map(obsproject, sbs, ps, datetime);
        //print first project
        System.out.println("Printing Project after map with one SB.");
        project1.printTree(System.out, "\t");
        //add a sb and update
        obsproject = createObsProject("newxmldocs/HG/twoTargets/ObsProject.xml");
        sb1 = createSchedBlock("newxmldocs/HG/twoTargets/SchedBlock0.xml");
        SchedBlock sb2 = createSchedBlock("newxmldocs/HG/twoTargets/SchedBlock1.xml");
        sbs = new SchedBlock[2];
        sbs[0] = sb1;
        sbs[1] = sb2;
        datetime = DateTime.currentSystemTime();
        project1 = ProjectUtil.updateProject(obsproject, project1, sbs, datetime);
        System.out.println("Printing Project after update with two SBs.");
        project1.printTree(System.out, "\t");
        //modify second SB's name and update.
        SchedBlock sb3 = createSchedBlock("newxmldocs/HG/twoTargets/SchedBlock2.xml");
        sbs[1] = sb3;
        datetime = DateTime.currentSystemTime();
        project1 = ProjectUtil.updateProject(obsproject, project1, sbs, datetime);
        System.out.println("Printing Project after update with second SB updated.");
        project1.printTree(System.out, "\t");
        //remove a sb and update project
        obsproject = createObsProject("newxmldocs/HG/ObsProject.xml");
        sb1 = createSchedBlock("newxmldocs/HG/SchedBlock0.xml");
        sbs = new SchedBlock[1];
        sbs[0] = sb1;
        project1 = ProjectUtil.updateProject(obsproject, project1, sbs, datetime);
        System.out.println("Printing Project after update with second SB deleted.");
        project1.printTree(System.out, "\t");
    }
    
    */
}
