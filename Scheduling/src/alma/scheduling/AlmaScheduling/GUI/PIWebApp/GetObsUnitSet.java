package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.AdvancedComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.PipelineProcessingRequestT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Identifier;
import alma.xmlstore.Operational;
import alma.xmlstore.ArchiveConnectionPackage.ArchiveException;
import alma.xmlstore.ArchiveConnectionPackage.PermissionException;
import alma.xmlstore.ArchiveConnectionPackage.UserDoesNotExistException;



//ALMA library

/**
 * Servlet Class
 *
 * @web.servlet              name="GetProject"
 *                           display-name="Name for GetProject"
 *                           description="Description for GetProject"
 * @web.servlet-mapping      url-pattern="/GetProject"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class GetObsUnitSet extends HttpServlet {

	private ALMAArchive archive;
	private ContainerServices cs;
	private String manager="corbaloc::146.88.7.49:3100/Manager";;
	private Logger m_logger = Logger.getLogger("PIWebPage");
	private AdvancedComponentClient m_componentClient = null;
	private ArchiveConnection archConnectionComp;
	private Identifier archIdentifierComp;
	private Operational archOperationComp;
	private EntitySerializer entitySerializer;
	private EntityDeserializer entityDeserializer;
	

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		
		
	
		resp.setContentType("text/html");
		java.io.PrintWriter out=resp.getWriter();
		String targetURL = "/ObsUnitSet.jsp";
		String uid="uid://X00/X15/X1";
		
		getArchiveComponents();
		
		try {
			m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");

			cs = m_componentClient.getContainerServices();
			//archive = new ALMAArchive(cs, new ALMAClock());
			
			ObsProject proj;
			ProjectStatus ps = null;
            OUSStatus outT;
	       
	        XmlEntityStruct xml = archOperationComp.retrieveDirty(uid);
	        proj = (ObsProject) entityDeserializer.deserializeEntity(xml, ObsProject.class);
            ObsUnitSetT obsT= proj.getObsProgram().getObsPlan();
            req.setAttribute("Name", obsT.getName());        
            req.setAttribute("PipelineProcessScriptName",obsT.getScienceProcessingScript());
            
       
            xml = archOperationComp.retrieveDirty(proj.getProjectStatusRef().getEntityId());
            ps = (ProjectStatus)entityDeserializer.deserializeEntity(xml, ProjectStatus.class);
            xml = archOperationComp.retrieveDirty(ps.getObsProgramStatusRef().getEntityId());
            outT = (OUSStatus)entityDeserializer.deserializeEntity(xml, OUSStatus.class);
            req.setAttribute("StartTime",outT.getStatus().getStartTime().toString());
            req.setAttribute("EndTime",outT.getStatus().getEndTime().toString());
            req.setAttribute("NumberSB",Integer.toString(outT.getTotalSBs()));
            req.setAttribute("NumberSBCompleted",Integer.toString(outT.getNumberSBsCompleted()));
            req.setAttribute("NumberSBFailted",Integer.toString(outT.getNumberSBsFailed()));
            req.setAttribute("Status",outT.getStatus().getState().toString());
            
            PipelineProcessingRequestT PPRT=outT.getPipelineProcessingRequest();
            if(PPRT!=null){
            if(PPRT.getTimeOfCreation()==null) {
            	req.setAttribute("PPRCreateTime","Not Set");
            }
            else {
            		req.setAttribute("PPRCreateTime",PPRT.getTimeOfCreation().toString());
            }
            
            if(PPRT.getTimeOfUpdate()==null) {
            	req.setAttribute("PPRCreateTime","Not Set");
            }
            else {
            		req.setAttribute("PPRUpdateTime",PPRT.getTimeOfUpdate().toString());
            }
           
            req.setAttribute("PPRRequest",PPRT.getRequestStatus().toString());
            req.setAttribute("PPRCompletion",PPRT.getCompletionStatus().toString());
            req.setAttribute("PPRComment",PPRT.getComment());
            req.setAttribute("PPRImageName",PPRT.getImagingProcedureName());
            }
            
            RequestDispatcher rd;
        	rd=getServletContext().getRequestDispatcher(targetURL);
        	rd.forward(req,resp);
        	
	}
		catch (Exception e) {
		    
		    StackTraceElement[] stackTraces = e.getStackTrace();
		    for(int i = 0; i < stackTraces.length; i++)
		    {
		    	out.println("<h2>" + stackTraces[i] + "</h2>");
		    }
		    m_componentClient = null;
		}
		
	}
	
	private void getArchiveComponents() {
		
		
		try {
		m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
		cs = m_componentClient.getContainerServices();
		
            m_logger.fine("SCHEDULING: Getting archive components");
            org.omg.CORBA.Object obj = cs.getDefaultComponent("IDL:alma/xmlstore/ArchiveConnection:1.0");
            this.archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(obj);
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
}
