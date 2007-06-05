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
	private ComponentClient m_componentClient = null;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException,
		IOException {
		
		m_logger = Logger.getLogger("PIWebPage");
		manager = System.getProperty("ACS.manager");
		Properties props = System.getProperties();
		System.out.println("testACS:properties:"+props);
		//manager = System.getProperty("user.dir");
		try {
		m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
		cs = m_componentClient.getContainerServices();
		archive = new ALMAArchive(cs, new ALMAClock());
		}
		catch (Exception e) {
		    System.out.println ("Schedule_PI_TEST: Contructor error " +
		        e.toString());
		    //e.printStackTrace();
		    m_componentClient = null;
		}

		resp.setContentType("text/html");
		java.io.PrintWriter out=resp.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>ALMA PI Web page Project Status</title>");
		out.println("</head><body>");
		out.println("<h2>this is an example servlet.</h2>");
		out.println("<h5>props:"+props+"<h5>");
		out.println("<h2>manager="+manager+"</h2>");
		out.println("<h2>componentclient="+cs+"<h2>");
		out.println("</body></html>");
	}
}
