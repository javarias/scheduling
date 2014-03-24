package alma.scheduling.psm.web.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import alma.scheduling.psm.sim.ReportGenerator;

public class ZippedReportsServlet extends HttpServlet {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 39759348347589345L;

	
	private static final String workDir = System.getenv("APRC_WORK_DIR");
	private final ReportGenerator rg;
	
	public ZippedReportsServlet() {
		rg = new ReportGenerator(workDir);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String id = req.getParameter("id");
		
		JRPdfExporter exporter = new JRPdfExporter();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, out);
		
		ByteArrayOutputStream byteZipOut = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(byteZipOut);
		
		JasperPrint print = null;
		if (id != null)
			print = rg.createArrayConfigurationReport(Long.valueOf(id));
		else
			print = rg.createArrayConfigurationReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createBandsAfterSimReport(Long.valueOf(id));
		else
			print = rg.createBandsAfterSimReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createBandsBeforeSimReport(Long.valueOf(id));
		else
			print = rg.createBandsBeforeSimReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createCompletionReport(Long.valueOf(id));
		else
			print = rg.createCompletionReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createExecutiveReport(Long.valueOf(id));
		else
			print = rg.createExecutiveReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createLstRangeAfterSimReport(Long.valueOf(id));
		else 
			print = rg.createLstRangeAfterSimReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createLstRangesBeforeSimReport(Long.valueOf(id));
		else 
			print = rg.createLstRangesBeforeSimReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		if (id != null)
			print = rg.createRaExecutiveBreakdownReport(Long.valueOf(id));
		else 
			print = rg.createRaExecutiveBreakdownReport();
		exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
		try {
			exporter.exportReport();
			addNewZipPdfEntry(zout, print.getName(), out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.reset();
		}
		
		InputStream is = null;
		if (id == null)
			is = rg.getFinalReport();
		else
			is = rg.getFinalReport(Long.valueOf(id));
		int b = 0;
		while((b = is.read()) != -1) {
			out.write(b);
		}
		try {
			addNewZipHtmlEntry(zout, "final_report", out);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			out.reset();
		}
		
		zout.close();
		resp.setBufferSize(1024);
		
		resp.setContentType("application/zip");
		resp.setContentLength(byteZipOut.toString().length());
		resp.setHeader("Content-Transfer-Encoding", "binary");
		resp.setHeader("Content-Disposition","attachment; filename=\"" + "reports_id-" + id + ".zip\"");
		ServletOutputStream sOut = resp.getOutputStream();
		sOut.write(byteZipOut.toByteArray());
		sOut.flush();
		sOut.close();
	}
	
	private void addNewZipPdfEntry(ZipOutputStream zout, String reportName, ByteArrayOutputStream reportOut) throws IOException {
		try {
			ZipEntry ze = new ZipEntry(reportName + ".pdf");
			ze.setSize(reportOut.size());
			ze.setTime(System.currentTimeMillis());
			zout.putNextEntry(ze);
			int b = 0;
			ByteArrayInputStream reportIs =  new ByteArrayInputStream(reportOut.toByteArray());
			while ((b = reportIs.read()) != -1) {
				zout.write(b);
			}
		} finally {
			if (zout != null)
				zout.closeEntry();
		}
	}
	
	private void addNewZipHtmlEntry(ZipOutputStream zout, String reportName, ByteArrayOutputStream reportOut) throws IOException {
		try {
			ZipEntry ze = new ZipEntry(reportName + ".htm");
			ze.setSize(reportOut.size());
			ze.setTime(System.currentTimeMillis());
			zout.putNextEntry(ze);
			int b = 0;
			ByteArrayInputStream reportIs =  new ByteArrayInputStream(reportOut.toByteArray());
			while ((b = reportIs.read()) != -1) {
				zout.write(b);
			}
		} finally {
			if (zout != null)
				zout.closeEntry();
		}
	}
}
