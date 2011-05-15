package alma.scheduling.psm.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;

import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.event.Event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Iframe;

import alma.scheduling.psm.sim.ReportGenerator;

public class ReportsController extends GenericForwardComposer implements
		Initiator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private Iframe report;
	private Button buttonReport;
	
	
	public void onClick$buttonReport(Event event){
		Iframe report = null;
		Component c =Path.getComponent("/reportsWindow");
		List l = c.getChildren();
		for(int i = 0; i< l.size(); i++){
			System.out.println(l.get(i));
			if(((Component)l.get(i)).getId().compareTo("report") == 0)
				report = (Iframe) l.get(i);
		}
		String workDir = System.getenv("APRC_WORK_DIR");
		System.out.println("CATALINA_TMP: " + System.getenv("CATALINA_TMP"));
		ReportGenerator rg = new ReportGenerator(workDir);
	//	ReportGenerator.setApplicationContext(SpringUtil
	//	.getApplicationContext());
		JasperPrint print = rg.createLstRangesBeforeSimReport();
		JRHtmlExporter exporter = new JRHtmlExporter();
		final StringBuffer out = new StringBuffer();
    	exporter.setParameter(JRHtmlExporterParameter.OUTPUT_STRING_BUFFER, out);
    	exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "image?image=");
    	exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);
    	try {
			exporter.exportReport();
		} catch (JRException e) {
			e.printStackTrace();
			return;
		}
		System.out.println(out);
		InputStream mediais = new ByteArrayInputStream(out.toString().getBytes());
		AMedia amedia = new AMedia("report.html", "html", "text/html", mediais);
		System.out.println(amedia);
		System.out.println(report);
		try{
			report.setContent(amedia);
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
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
