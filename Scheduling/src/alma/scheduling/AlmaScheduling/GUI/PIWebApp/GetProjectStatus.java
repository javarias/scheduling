package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.Define.*;
import alma.acs.container.ContainerServices;
import alma.acs.component.client.ComponentClient;
import alma.acs.component.client.AdvancedComponentClient;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.projectstatus.*;


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
public class GetProjectStatus extends HttpServlet {

	private ALMAArchive archive;
	private ContainerServices cs;
	private String manager;
	private Logger m_logger = null;
	private AdvancedComponentClient m_componentClient = null;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		
		m_logger = Logger.getLogger("PIWebPage");
		manager = System.getProperty("ACS.manager");
		manager = "corbaloc::146.88.7.49:3100/Manager";
	
		resp.setContentType("text/html");
		java.io.PrintWriter out=resp.getWriter();
		String targetURL = "/ProjectStatus.jsp";
		//ArrayList tempinfo,allinfo;
		//String test="";
		
		try {
			//out.println("<h2>this is before run m component</h2>");
			m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
			//out.println("<h2>this is after run m component</h2>");
			cs = m_componentClient.getContainerServices();
			//org.omg.CORBA.Object compObj=cs.getComponent(acsComponent);
			//UserRepositoryComponent urComp = UserRepositoryComponentHelper.narrow(compObj);
			archive = new ALMAArchive(cs, new ALMAClock());
		//try {
        	        Project[] p = archive.getAllProject();
            //out.println("<h2>Got "+p.length+" projects</h2>");
            		String uid1;
            		//tempinfo = new ArrayList();
            		
            		for (int i=0; i < 1 ;i++){
            			//i=p.length-1;
            			
                	uid1 = p[i].getId();
	                //out.println(uid1);
        	        //String ProjectName=p[i].getProjectName();
                	req.setAttribute("ProjectName",p[i].getProjectName());
                	//tempinfo.add(p[i].getProjectName());
                	//String PIName=p[i].getPI();
                	req.setAttribute("PIName",p[i].getPI());
                	//tempinfo.add(p[i].getPI());
                	req.setAttribute("TimeOfUpdate", p[i].getTimeOfUpdate().toString());
                	//tempinfo.add(p[i].getTimeOfUpdate().toString());
                	req.setAttribute("ProjectStatus", p[i].getStatus().getStatus());
                	//tempinfo.add(p[i].getPI());
                	if(p[i].getStatus().getReadyTime()==null) {
                		req.setAttribute("ReadyTime", "Not set");
                		//tempinfo.add(p[i].getPI());
                	}
                	else {
                		req.setAttribute("ReadyTime", p[i].getStatus().getReadyTime().toString());
                		//tempinfo.add(p[i].getPI());
                	}
                	if(p[i].getStatus().getStartTime()==null) {
                		req.setAttribute("StartTime", "Not set");
                		//tempinfo.add(p[i].getPI());
                	}
                	else {
                		req.setAttribute("StartTime", p[i].getStatus().getStartTime().toString());
                		//tempinfo.add(p[i].getPI());
                	}
                	if(p[i].getStatus().getEndTime()==null) {
                		req.setAttribute("EndTime", "Not set");
                		//tempinfo.add(p[i].getPI());
                	}
                	else {
                		req.setAttribute("EndTime", p[i].getStatus().getEndTime().toString());
                		//tempinfo.add(p[i].getPI());
                	}
                
                	req.setAttribute("TotalSB", Integer.toString(p[i].getTotalSBs()));
                	req.setAttribute("NumberSBComplete", Integer.toString(p[i].getNumberSBsCompleted()));
                	req.setAttribute("NumberSBFail", Integer.toString(p[i].getNumberSBsFailed()));
                	// get ObsunitSet info from get Program
                	//req.setAttribute("TotalObsUnitSet", Integer.toString(p[i].getTotalPrograms()));
                	//req.setAttribute("NumbetObsUnitSetComplete",Integer.toString(p[i].getNumberProgramsCompleted()));
                	//req.setAttribute("NumbetObsUnitSetFail", Integer.toString(p[i].getNumberProgramsFailed()));
                	//SB[] AllSB=p[i].getAllSBs();
                	//SB[] AllSB= archive.getAllSB();
                	//out.print("SB length:");
                	//out.println(AllSB.length);
                	//for(int j=0;j<AllSB.length;j++){
                	//	if(AllSB[j].getProject().getProjectName().equals(p[i].getProjectName()))
                	//	{
                	//		AllSB[0]=AllSB[j];
                	//		System.out.println("SB:"+AllSB[0].getSBName().toString());
                	//	}
                	//}
                	
                	//req.setAttribute("SBTimeOfUpdate", AllSB[0].getTimeOfUpdate().toString());
                	//req.setAttribute("SBStatus", AllSB[0].getStatus().getStatus());
                	/*
                	if(AllSB[0].getStatus().getReadyTime()==null)
                		req.setAttribute("SBReadyTime", "Not set");
                	else 
                		req.setAttribute("SBReadyTime", p[i].getStatus().getReadyTime().toString());
                	if(AllSB[0].getStatus().getStartTime()==null)
                		req.setAttribute("SBStartTime", "Not set");
                	else
                		req.setAttribute("SBStartTime", p[i].getStatus().getStartTime().toString());
                	if(AllSB[0].getStatus().getEndTime()==null)
                		req.setAttribute("SBEndTime", "Not set");
                	else 
                		req.setAttribute("SBEndTime", p[i].getStatus().getEndTime().toString());
                	req.setAttribute("SBTotalRequireTime", Integer.toString(AllSB[0].getTotalRequiredTimeInSeconds()));
                	req.setAttribute("SBTotalUsedTime", Integer.toString(AllSB[0].getTotalUsedTimeInSeconds()));
                	req.setAttribute("SBPercentage", 
                			Float.toString(AllSB[0].getTotalUsedTimeInSeconds()/AllSB[0].getTotalRequiredTimeInSeconds()));
                	ProjectStatus ps= archive.getProjectStatus(p[i]);
                	*/
                	//ps.getObsProgramStatus().getObsUnitSetRef()
                	//SessionT st=ps.getObsProgramStatus().getObsUnitSetStatusTChoice().getObsUnitSetStatus(0).getSession(0);
                	//out.print("<h2>Session t:"+st.getExecBlockRefCount()+"</h2>");
                	//req.setAttribute("EBCount", Integer.toString(st.getExecBlockRefCount()));
                	//SchedBlock AllSB[0].getSbStatusId().
                	/*ExecBlock[] AllEB=AllSB[0].getExec();
                	Source[] source=AllSB[0].getSource();
                	
                	//req.setAttribute("EBSource", source[0].getSourceName().toString());
                	out.println("<h2>SB Status:"+AllSB[0].getStatus().getStatus() +"</h2>");
                	out.println("<h2>EB length:"+AllEB.length + "</h2>");
                	if(AllEB.length>0) {
                	req.setAttribute("EBName", AllEB[0].getParent().getSBName());
                	req.setAttribute("EBStatus", AllEB[0].getStatus().getStatus());
                	req.setAttribute("EBNumberOfAntenna", Integer.toString(AllEB[0].getParent().getAntennaList().length));
                	req.setAttribute("EBConfigName", AllEB[0].getArrayName().toString());
                	}*/
                	//req.setAttribute(arg0, arg1);
                	//req.setAttribute(arg0, arg1)
                	
                	//get info about obsunit set
                	//req.setAttribute("ObsUnitSet", p[i].)
                	//req.setAttribute("array", tempinfo);
                	RequestDispatcher rd;
                	rd=getServletContext().getRequestDispatcher(targetURL);
                	rd.forward(req,resp);
                	//p[i].getStatus().getStartTime().e
	                //out.println("<h2>TimeofUpdate "+p[i].getTimeOfUpdate().toString() +"</h2>");
        	        //out.println("<h2>Status "+p[i].getStatus().getStatus() +"</h2>");
                //	out.println("<h2>ReadyTime "+p[i].getStatus().getReadyTime()+"</h2>");
	          //      out.println("<h2>StartTime "+p[i].getStatus().getStartTime()+"</h2>");
        	    //    out.println("<h2>EndTime "+p[i].getStatus().getEndTime()+"</h2>");
                //	out.println("<h2>totalRequireTime "+ p[i].getTotalRequiredTimeInSeconds()+"</h2>");
	          //      out.println("<h2>totalUsedTime "+p[i].getTotalUsedTimeInSeconds() +"</h2>");
        	    //    out.println("<h2>totalSB "+p[i].getTotalSBs() +"</h2>");
                	//out.println("<h2> "+ +"</h2>");
	                //out.println("<h2> "+ +"</h2>");
        		    //}
        //} catch (Exception e) {
        //    m_logger.severe("SCHED_TEST: Error");
        //    e.printStackTrace();
        //    throw new Exception(e);
        //}

			//out.println("<h2>getAllSB="+archive.getAllSB().toString()+"</h2>");
		//	out.println("<h2>getallproject="+archive.getAllProject().toString()+"</h2>");
		}
	}
		catch (Exception e) {
		    out.println ("<h2>Schedule_PI_TEST: Contructor error " +
		        e.toString()+ "</h2>");
		   // e.getStackTrace().toString()
		    out.println ("<h2>Schedule_PI_TEST: Contructor error </h2>");
		    //out.println("<h2>TotalSB:"+test + "</h2>");
		    StackTraceElement[] stackTraces = e.getStackTrace();
		    for(int i = 0; i < stackTraces.length; i++)
		    {
		    	out.println("<h2>" + stackTraces[i] + "</h2>");
		    }
		    //e.printStackTrace();
		    m_componentClient = null;
		}
		//out.println("<h2>componentclient="+m_componentClient+"<h2>");
		//out.println("<h2>containerService="+cs+"<h2>");
		//out.println("</body></html>");
	}
}
