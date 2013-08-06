package alma.scheduling.psm.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Iframe;
import org.zkoss.zul.api.Window;

import alma.scheduling.psm.sim.ReportGenerator;

public class ReportsController extends GenericForwardComposer implements
		Initiator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Iframe reportFrame;
	protected Button buttonReport;
	protected AMedia pdfMedia;

	public void doAfterCompose(Page page) throws Exception {
		// TODO Auto-generated method stub

	}

	public void doInit(Page page, Map args) throws Exception {
		System.out.println("ReportsController.doInit()");
		System.out.println((String) Sessions.getCurrent().getAttribute(
				"workDir"));
	}

	public void showReport() {
		Component parent = Path.getComponent("//");
		Window w = null;
		for (Object o : parent.getChildren()) {
			try {
				w = (Window) o;
			} catch (ClassCastException ex) {

			}
		}
		System.out.println(w.getId());
		String workDir = System.getenv("APRC_WORK_DIR");
		ReportGenerator rg = new ReportGenerator(workDir);
		JasperPrint print = null;
		if (w.getId().compareTo("reportWindowBeforeSimBand") == 0) {
			print = rg.createBandsBeforeSimReport();
			print.setName("band_time_requested_report");
		} else if (w.getId().compareTo("reportWindowBeforeSimLST") == 0) {
			print = rg.createLstRangesBeforeSimReport();
			print.setName("ra_time_requested_report");
		} else if (w.getId().compareTo("reportWindowAfterSimExec") == 0) {
			print = rg.createExecutiveReport();
			print.setName("executive_time_used_report");
		} else if (w.getId().compareTo("reportWindowAfterSimLST") == 0) {
			print = rg.createLstRangeAfterSimReport();
			print.setName("ra_time_used_report");
		} else if (w.getId().compareTo("reportWindowAfterSimBand") == 0) {
			print = rg.createBandsAfterSimReport();
			print.setName("band_time_used_report");
		} else if (w.getId().compareTo("reportWindowAfterSimCompletion") == 0) {
			print = rg.createCompletionReport();
			print.setName("completion_report");
		}
		showReport(print, reportFrame);
	}

	void showReport(JasperPrint print, org.zkoss.zul.api.Iframe report) {
		JRExporter exporter = null;
		String localString = null;
		//Pdf Rendering. Saving it for future reference
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		exporter = new JRPdfExporter();
		exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, out);
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
		} catch (JRException e) {
			e.printStackTrace();
			return;
		}
		InputStream mediais = new ByteArrayInputStream(out.toByteArray());
		pdfMedia = new AMedia(print.getName() + ".pdf", "pdf",
				"application/pdf", mediais);
		
		//html rendering. To show immediately in the window
		out = new ByteArrayOutputStream();
		String contextPath = Executions.getCurrent().getContextPath();
		exporter = new JRHtmlExporter();
		Sessions.getCurrent().setAttribute(
				ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, print);
		report.setAttribute(
				ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, print,
				Iframe.SESSION_SCOPE);
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, contextPath
				+ "/servlets/jasperimage?image=");
		exporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, out);
		exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
		} catch (JRException e) {
			e.printStackTrace();
			return;
		}
		try {
			localString = out.toString("ISO-8859-1");
		} catch (java.io.UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		mediais = new ByteArrayInputStream(localString.getBytes());
		AMedia amedia = null;
		amedia = new AMedia(print.getName() + ".html", "html", "text/html",
				mediais);
		try {
			report.setContent(amedia);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	void exportReport() {
		reportFrame.setContent(pdfMedia);
	}

	public void onClick$pdfButton(Event event) {
		exportReport();
	}
}
