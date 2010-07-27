package alma.scheduling.psm.web;

import javax.servlet.ServletContext;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.util.SessionInit;

import alma.scheduling.psm.util.PsmContext;

public class MySessionInit implements SessionInit {
	public void init(Session session, Object request) throws Exception {
		
		String workDir = System.getenv("APRC_WORK_DIR");
		PsmContext psmCtx = new PsmContext(workDir);
		PsmContext.setApplicationContext(
				WebApplicationContextUtils.getRequiredWebApplicationContext(
						(ServletContext)session.getWebApp().getNativeContext() ) );
		
		session.setAttribute("workDir", workDir );
		
		// aprc configuration reference:
		// TODO: Separe this into simulator config and simulations configs.
		session.setAttribute("configuration", psmCtx.getAprcConfig() );

		session.setAttribute("executiveController", new ExecutiveController());
		session.setAttribute("mainWindowController", new MainWindowController());
		session.setAttribute("configurationController", new ConfigurationController());
		session.setAttribute("simulationController", new SimulationController());
		return;
	}
}