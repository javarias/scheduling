package alma.scheduling.psm.web.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.datamodel.output.dao.StreamBasedOutputDao;
import alma.scheduling.datamodel.output.dao.XmlOutputDaoImpl;
import alma.scheduling.utils.DSAContextFactory;

public class ExportZippedOutputServlet extends HttpServlet {

	private static final long serialVersionUID = 13478978945797239L;
	private final StreamBasedOutputDao xmlOutDao;
	private final OutputDao outDao;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	
	public ExportZippedOutputServlet() {
		super();
		xmlOutDao = new XmlOutputDaoImpl();
		ApplicationContext ctx = DSAContextFactory.getContext();
		outDao = (OutputDao) ctx.getBean("outDao");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ByteArrayOutputStream byteZipOut = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(byteZipOut);
		List<InputStream> results = xmlOutDao.getResultsAsStream(outDao.getResults());
		int i = 0;
		for(InputStream in: results) {
			addNewZipXmlEntry(zout, "output_file_" + i++, in);
		}
		zout.close();
//		resp.setBufferSize(1024);
		
		resp.setContentType("application/zip");
		resp.setContentLength(byteZipOut.size());
		resp.setHeader("Content-Transfer-Encoding", "binary");
		resp.setHeader("Content-Disposition","attachment; filename=\"" + "output_dump-" + dateFormat.format(new Date()) + ".zip\"");
		ServletOutputStream out = resp.getOutputStream();
		out.write(byteZipOut.toByteArray());
		out.flush();
		out.close();
	}
	
	private void addNewZipXmlEntry(ZipOutputStream zout, String fileName, InputStream in) throws IOException {
		ZipEntry ze = new ZipEntry(fileName + ".xml");
		zout.putNextEntry(ze);
		byte buf[] = new byte[1024];
		int rs = 0;
		while ((rs = in.read(buf)) >= 0) {
			zout.write(buf, 0, rs);
		}
		zout.closeEntry();
	}
	
	
}
