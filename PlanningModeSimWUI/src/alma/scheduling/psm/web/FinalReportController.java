package alma.scheduling.psm.web;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.api.Iframe;

import alma.scheduling.psm.sim.ReportGenerator;

public class FinalReportController extends GenericForwardComposer implements
		Initiator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1207893894762397342L;
	private Iframe reportFrame;
	
	@Override
	public void doInit(Page page, Map args) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void doAfterCompose(Page page) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public void onCreate$reportWindowFinalReport(Event event) {
		String workDir = System.getenv("APRC_WORK_DIR");
		ReportGenerator rg = new ReportGenerator(workDir);
		try {
			InputStream is = rg.getFinalReport();
			AMedia amedia = new AMedia("final_report.html", "html", "text/html", is);
			reportFrame.setContent(amedia);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	

}
