package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
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
public class GetProject extends HttpServlet {

	private ALMAArchive archive;
	private ContainerServices cs;
	String manager;
	private Logger m_logger = null;
	private AdvancedComponentClient m_componentClient = null;
	String acsComponent ="USERREPOSITORY";

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		
		m_logger = Logger.getLogger("PIWebPage");
		manager = System.getProperty("ACS.manager");
		manager = "corbaloc::146.88.7.49:3100/Manager";
		
		
		//Properties props = System.getProperties();
		//System.out.println("testACS:properties:"+props);
		//manager = System.getProperty("user.dir");
		resp.setContentType("text/html");
		java.io.PrintWriter out=resp.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>ALMA PI Web page Project Status</title>");
		out.println("</head><body>");
		out.println("<h2>this is an example servlet.</h2>");
		//out.println("<h5>props:"+props+"<h5>");
		out.println("<h2>write down the manager to the code</h2>");
		out.println("<h2>manager="+manager+"</h2>");
		
		try {
			out.println("<h2>this is before run m component</h2>");
		m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
		out.println("<h2>this is after run m component</h2>");
		cs = m_componentClient.getContainerServices();
		//org.omg.CORBA.Object compObj=cs.getComponent(acsComponent);
		//UserRepositoryComponent urComp = UserRepositoryComponentHelper.narrow(compObj);
		archive = new ALMAArchive(cs, new ALMAClock());
		try {
            Project[] p = archive.getAllProject();
            out.println("<h2>Got "+p.length+" projects</h2>");
            String uid1;
            for (int i=0; i < p.length;i++){
                uid1 = p[i].getId();
                out.println(uid1);
            }

            
        } catch (Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
            throw new Exception(e);
        }

			out.println("<h2>getAllSB="+archive.getAllSB().toString()+"</h2>");
			out.println("<h2>getallproject="+archive.getAllProject().toString()+"</h2>");
		}
		catch (Exception e) {
		    out.println ("<h2>Schedule_PI_TEST: Contructor error " +
		        e.toString()+ "</h2>");
		   // e.getStackTrace().toString()
		    out.println ("<h2>Schedule_PI_TEST: Contructor error </h2>");
		    StackTraceElement[] stackTraces = e.getStackTrace();
		    for(int i = 0; i < stackTraces.length; i++)
		    {
		    	out.println("<h2>" + stackTraces[i] + "</h2>");
		    }
		    //e.printStackTrace();
		    m_componentClient = null;
		}

		
		
		out.println("<h2>componentclient="+m_componentClient+"<h2>");
		out.println("<h2>containerService="+cs+"<h2>");
		out.println("</body></html>");
	}
}
