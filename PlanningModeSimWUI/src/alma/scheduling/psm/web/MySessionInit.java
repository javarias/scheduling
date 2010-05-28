package alma.scheduling.psm.web;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.util.SessionInit;
import org.zkoss.zkplus.spring.SpringUtil;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;

public class MySessionInit implements SessionInit {
	public void init(Session session, Object request) throws Exception {

		session.setAttribute("executiveController", new ExecutiveController());
		session.setAttribute("mainWindowController", new MainWindowController());
		session.setAttribute("configurationController", new ConfigurationController());
		return;
	}
}