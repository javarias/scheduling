package alma.scheduling.psm.web.timeline;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TimelineEventServlet extends HttpServlet {

	private TimelineCollector collector = TimelineCollector.getInstance();
	/**
	 * 
	 */
	private static final long serialVersionUID = 3744983748974L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		writer.write(collector.getXML());
	}

	
}
