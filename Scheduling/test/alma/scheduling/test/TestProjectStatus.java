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
import alma.acs.container.ContainerException;
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
        ProjectStatus ps = createProjectStatus("xmldocs/ProjectStatus1.xml");
        assertNotNull(ps);
        //create a project obj
        ObsProject obsproject = createObsProject("xmldocs/ObsProject.xml");
        //ObsProject obsproject = createObsProject("xmldocs/ObsProject1.xml");
        assertNotNull(obsproject);
        //create 2 SB objects
        SchedBlock[] sbs = new SchedBlock[1];
        //SchedBlock sb = createSchedBlock("xmldocs/SchedBlock1.xml");
        SchedBlock sb = createSchedBlock("xmldocs/SchedBlock0.xml");
        assertNotNull(sb);
        sbs[0] = sb;
        //sb = createSchedBlock("xmldocs/SchedBlock2.xml");
        
        //assertNotNull(sb);
        //sbs[1] = sb;
        //assertNotSame(sbs[0].getSchedBlockEntity().getEntityId(), 
        //              sbs[1].getSchedBlockEntity().getEntityId());


        DateTime datetime = DateTime.currentSystemTime();
        //get orig project
        Project project1 = ProjectUtil.map(obsproject, sbs, ps, datetime);
        assertNotNull(project1);

        //get orig PS
        ps = ProjectUtil.map(project1, datetime);
        assertNotNull(ps);
        
        String psXmlString = createProjectStatusXml(ps);

        //update project
        /*
        Project project2 = ProjectUtil.updateProject(
                obsproject, project1, sbs, datetime);
        
        //get a PS for the updated project
        ProjectStatus ps2 = ProjectUtil.map(project2, datetime);
        String psXmlString2 = createProjectStatusXml(ps2);
        //since our updated project was the same as our original project
        //the 2 project status' should be the same
        assertEquals(psXmlString, psXmlString2);
        //get another sb and add it to the project
        //so update project object
        logger.info("So far so good");
        ObsProject obsproject2 = createObsProject("xmldocs/ObsProject2.xml");
        assertNotNull(obsproject);
        SchedBlock[] newSBs = new SchedBlock[3];
        newSBs[0] = sbs[0];
        newSBs[1] = sbs[1];
        sb = createSchedBlock("xmldocs/SchedBlock3.xml");
        assertNotNull(sb);
        newSBs[2] = sb;
        
        //update obsproject to a new project
        
        Project project3 = ProjectUtil.updateProject(obsproject2, project1, newSBs, datetime);
        assertNotNull(project3);
        logger.info("updated project with projec that has another SB");
                
        //Map the project status for this new project
        ProjectStatus ps3 = ProjectUtil.map(project3, datetime);
        logger.info("mapped project of obsproject 2 to PS");
        String psXmlString3 = createProjectStatusXml(ps3);
        //should not be the same as project status for first obsproject/project
        //because we've added another SB
        assertFalse(psXmlString2.equals(psXmlString3));
        //update project1 with obsproject2
        //Project updatedProject = ProjectUtil.updateProject(obsproject2,
        //        project1, newSBs, datetime);
        //logger.info("updated project 1 with obsproject 2");
        //get ps for updated project
        ProjectStatus ps4 = ProjectUtil.map(project3,datetime);
        logger.info("mapped project of obsproject 2 to another PS");
        String psXmlString4 = createProjectStatusXml(ps4);
        //ps of obsproject2 mapped should be equal to ps of obsproject2 
        //well except for the entityPartIds will be different.. so need 
        //something else to make sure... its doing right thing..
        //updated to project1
        //System.out.println(psXmlString3);
        //System.out.println(psXmlString4);
        assertEquals(psXmlString3, psXmlString4);
    */
    }
    
}
