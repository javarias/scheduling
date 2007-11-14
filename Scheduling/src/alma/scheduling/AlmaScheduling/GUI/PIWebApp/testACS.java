package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.Define.*;
import alma.acs.container.ContainerServices;
import alma.acs.component.client.ComponentClient;
import alma.acs.component.client.AdvancedComponentClient;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.io.IOException;
import java.io.*;
//for ICD/HLA
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.types.*;
import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
import alma.xmlentity.XmlEntityStruct;

import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityException;

import alma.xmlstore.Operational;
import alma.xmlstore.Identifier;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionPackage.*;
import alma.xmlstore.OperationalPackage.*;
import alma.xmlstore.ArchiveInternalError;
//import alma.entity.xmlstore.obsproject.*;
//import alma.entity.xmlstore.schedblock.*;
import alma.xmlstore.Cursor;
import alma.xmlstore.CursorPackage.QueryResult;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;


public class testACS {

	 ContainerServices cs;
	 String manager;
	 Logger m_logger = null;
	 ComponentClient m_componentClient = null;
	 ALMAArchive archive;
	 private ArchiveConnection archConnectionComp;
	 private Identifier archIdentifierComp;
	 private Operational archOperationComp;
	 private EntitySerializer entitySerializer;
	 private EntityDeserializer entityDeserializer;
	 
	public testACS() {
		super();
		getArchiveComponents();
		readvalue();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public  String readvalue() {
		// TODO Auto-generated method stub
		try {
			ProjectStatus ps;
	    //ObsProject[] obsProj = archive.getAllObsProjects();
	    //m_logger.info("Got Obs Project amount:"+obsProj.length);
            Project[] p = archive.getAllProject();
            String ps_id = "" ; 
            m_logger.info("Got "+p.length+" projects");
            String uid1;

            //p = archive.getAllProject();
            //m_logger.info("after Got "+p.length+" projects");

	    //m_logger.info("Got Obs Project amount:"+obsProj.length);
            for (int i=0; i < p.length;i++){
	    ObsProject obsProj = archive.getObsProject(p[i].getId());
                uid1 = p[i].getId();
                //m_logger.info("PI:"+obsProj.getPI());
               	//m_logger.info("obsProj create time:"+obsProj.getTimeOfCreation());
		//m_logger.info("getId:"+uid1);
                //String uid=""
                if(uid1.equals("uid://X00/X15/X1")) {
                ps = archive.getProjectStatus(p[i]);
                ps_id = p[i].getProjectStatusId();
                //m_logger.info("ProjectStatus:"+p[i]);
                ObsUnitSetStatusTChoice choice = ps.getObsProgramStatus().getObsUnitSetStatusTChoice();
                SBStatusT[] sbs = choice.getSBStatus();
                //SessionT st= ps.getObsProgramStatus().getSession(i);
		
                for(int j=0;j<sbs.length;j++) {
			m_logger.info("Ist SBStatusT info:");
			//m_logger.info("SB Status:"+sbs[j].getSBStatus());
			m_logger.info("SB SchedBlockRef:"+sbs[j].getSchedBlockRef());
                	m_logger.info("SBS execblock number:"+sbs[j].getExecStatusCount());
			for(int k=0;k<sbs[j].getExecStatusCount();k++) {
				ExecStatusT EST=sbs[j].getExecStatus(k);
				m_logger.info("ExecStatusT:"+EST.getArrayName());	
			}
                	m_logger.info("SB update:"+sbs[j].getTimeOfUpdate());
                	if(sbs[j].getExecStatusCount()>0) {
                		ExecStatusT es=sbs[j].getExecStatus(0);
                		//es.getExecBlockRef().getExecBlockId()
                		m_logger.info("Array_name:"+es.getArrayName());	
                		m_logger.info("ES:"+es.getTimeOfCreation());
                		m_logger.info("ES Update:"+es.getTimeOfUpdate());
                		m_logger.info("Best SB:"+es.getBestSB());
                		m_logger.info("EntityPartId:"+es.getEntityPartId());
				m_logger.info("ALMA type:"+es.getAlmatype());
				m_logger.info("EB ExecBlockRef:"+es.getExecBlockRef());
				m_logger.info("EB Status:"+es.getStatus());
				m_logger.info("Start to read Project status......");
				m_logger.info("Project Name:"+p[i].getProjectName());
				m_logger.info("Program:"+p[i].getProgram());
				m_logger.info("ObsProject id:"+p[i].getObsProjectId());
				m_logger.info("scientificpriority:"+p[i].getScientificPriority());
				m_logger.info("PI Name:"+p[i].getPI());
				m_logger.info("ProjectStatusEntity:"+p[i].getStatus());
				m_logger.info("Project Status:"+p[i].getStatus());
                		m_logger.info("Project create info:"+p[i].getTimeOfCreation().toString());
                		m_logger.info("Project update info:"+p[i].getTimeOfUpdate().toString());
				m_logger.info("ObsProject updatetime:"+ps.getTimeOfUpdate().toString());         
       		//XmlEntityStruct xml = archOperationComp.retrieveDirty(es.getExecBlockRef());
       		//XmlEntityStruct xml = archOperationComp.retrieveDirty(ps_id);
                //		ExecBlock EBB = (ExecBlock) entityDeserializer.deserializeEntity(xml, SchedBlock.class);a
	//			ExecBlock ebb= new ExecBlock(es.getExecBlockRef().getExecBlockId(),es.getArrayName());
          //      	        m_logger.info("ExecBlock:"+ebb.getProject());	
                		//st.
			}	
            			}				
            	}
            }
            //ps.
            //get project status from archive.....
            XmlEntityStruct xml = archOperationComp.retrieveDirty(ps_id);
            ProjectStatus pjs = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
            //FileWriter writer = new FileWriter("test.xml");
            //ProjectStatus.marshalProjectStatus(writer);
            //m_logger.info("project update:"+pjs.getTimeOfUpdate().toString());
            ///m_logger.info("Project name:"+p[p.length-1].getProjectName().toString());
            //m_logger.info("update:"+p[p.length-1].getTimeOfUpdate());
            //m_logger.info("create:"+p[p.length-1].getTimeOfCreation());
		Cursor cursor = archOperationComp.query("/sbl:SchedBlock","SchedBlock");
		Vector tmp = new Vector();
		while(cursor.hasNext()){
			QueryResult res = cursor.next();
			xml = archOperationComp.retrieve(res.identifier);
			SchedBlock SBlock = (SchedBlock)entityDeserializer.deserializeEntity(xml,SchedBlock.class);
			//m_logger.info("SchedBlock:"+SBlock.getSchedBlockEntity().getEntityId());
			
		}

            
		}
		catch (Exception e) {
		    System.out.println ("TestACS: Contructor error " +
		        e.toString());
		    //e.printStackTrace();
		    m_componentClient = null;
		}
		return manager;
	}
	
	private void getArchiveComponents() {
		m_logger = Logger.getLogger("PIWebPage");
		manager = System.getProperty("ACS.manager");
		manager = "corbaloc::146.88.7.49:3100/Manager";
		Properties props = System.getProperties();
		//System.out.println("testACS:properties:"+props);
		//System.out.println("testACS:manager:"+manager);
		try {
		m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
		cs = m_componentClient.getContainerServices();
		archive = new ALMAArchive(cs, new ALMAClock());
        
            m_logger.fine("SCHEDULING: Getting archive components");
            org.omg.CORBA.Object obj = cs.getDefaultComponent("IDL:alma/xmlstore/ArchiveConnection:1.0");
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(obj);
            
            this.archConnectionComp.getAdministrative("SCHEDULING").init();
            this.archOperationComp = archConnectionComp.getOperational("SCHEDULING");
            this.archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                    cs.getDefaultComponent(
                        "IDL:alma/xmlstore/Identifier:1.0"));
        } catch(AcsJContainerServicesEx e) {
            m_logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
            //sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch (ArchiveException e) {
            m_logger.severe("SCHEDULING: Archive error: "+e.toString());
            //sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch(UserDoesNotExistException e) {
            m_logger.severe("SCHEDULING: Archive error: "+e.toString());
            //sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch (PermissionException e) {
            m_logger.severe("SCHEDULING: Archive error: "+e.toString());
            //sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch(ArchiveInternalError e) {
            m_logger.severe("SCHEDULING: Archive error: "+e.toString());
            //sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            archConnectionComp =null;
            archOperationComp =null;
            archIdentifierComp = null;
        } catch(Exception e) {
        	m_logger.severe("SCHEDULING: Archive error: "+e.toString());
        }
        
        entitySerializer = EntitySerializer.getEntitySerializer(
            cs.getLogger());
        entityDeserializer = EntityDeserializer.getEntityDeserializer(
            cs.getLogger());
        m_logger.fine("SCHEDULING: The ALMAArchive has been constructed.");
    }
	
	public static void main(String[] args){
		testACS aaa = new testACS();
	}

	
}
