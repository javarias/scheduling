package alma.scheduling.psm.web.report;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import alma.scheduling.psm.sim.ReportGenerator;

public class FinalReportServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 23479329847978234L;
	
	private static final String workDir = System.getenv("APRC_WORK_DIR");
	private final ReportGenerator rg;

	public FinalReportServlet() {
		rg = new ReportGenerator(workDir);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String id = req.getParameter("id");
		InputStream is = null;
		if (id == null)
			is = rg.getFinalReport();
		else
			is = rg.getFinalReport(Long.valueOf(id));
		int b = 0;
		while((b = is.read()) != -1) {
			resp.getWriter().write((char)b);
		}
	}
	
}
