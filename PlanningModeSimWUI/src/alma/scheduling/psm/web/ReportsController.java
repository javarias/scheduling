package alma.scheduling.psm.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zkex.zul.api.Jasperreport;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.api.Button;

import alma.scheduling.psm.sim.ReportGenerator;

public class ReportsController extends GenericForwardComposer implements
		Initiator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Jasperreport report;
	private Button buttonReport;
	
	
	public void onClick$buttonReport(Event event){
		Component c =Path.getComponent("//");
		List l = c.getChildren();
		for(int i = 0; i< l.size(); i++){
			if(((Component)l.get(i)).getId().compareTo("report") == 0)
				report = (Jasperreport) l.get(i);
		}
		String workDir = System.getenv("APRC_WORK_DIR");
		ReportGenerator rg = new ReportGenerator(workDir);
	//	ReportGenerator.setApplicationContext(SpringUtil
	//	.getApplicationContext());
		report.setSrc("alma/scheduling/psm/reports/crowdingReport.jasper");
		report.setDatasource(rg.getCrowdingReportData());
		report.setParameters(new HashMap());
		report.setType("html");
	}
	
	@Override
	public void doAfterCompose(Page page) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void doInit(Page page, Map args) throws Exception {
		System.out.println("ReportsController.doInit()");
		System.out.println( (String)Sessions.getCurrent().getAttribute("workDir") );
	}

}
