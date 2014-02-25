package alma.scheduling.psm.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Grid;
import org.zkoss.zul.api.Window;

import alma.scheduling.datamodel.output.SimulationResults;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.datamodel.output.dao.XmlOutputDaoImpl;
import alma.scheduling.utils.DSAContextFactory;

public class ResultsListComposer extends GenericForwardComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -173789368957789234L;

	private OutputDao outDao;
	
	//widgets
	Window resultsList;
	Grid grid;
	Button buttonLoadResults;
	Button buttonDeleteAllResults;
	
	public ResultsListComposer() {
		outDao = (OutputDao) DSAContextFactory.getContext().getBean("outDao");
	}
	
	public void onCreate$resultsList() {
		refreshTableGrid();
	}
	
	public void onClick$buttonDeleteAllResults() {
		org.springframework.context.ApplicationContext ctx = alma.scheduling.psm.util.PsmContext.getApplicationContext();
		OutputDao outDao = (OutputDao)ctx.getBean("outDao");
		outDao.deleteAll();
		Event closeEvent = new Event("onClose", resultsList);
		Events.postEvent(closeEvent);
	}
	
	public void onUpload$buttonLoadResults(UploadEvent event) {
		Media media = event.getMedia();
		XmlOutputDaoImpl xmlOutDao = new XmlOutputDaoImpl();
		ZipInputStream zis = new ZipInputStream(media.getStreamData());
		ZipEntry ze = null;
		ArrayList<InputStream> inputs = new ArrayList<InputStream>();
		try {
			byte[] buf = new byte[1024];
			int size = 0;
			while ((ze = zis.getNextEntry()) != null) {
				System.out.println("Processing " + ze.getName());
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				while((size = zis.read(buf)) >= 0)
					os.write(buf, 0, size);
				inputs.add(new ByteArrayInputStream(os.toByteArray()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<SimulationResults> results = xmlOutDao.loadResults(inputs);
		outDao.saveResults(results);
		refreshTableGrid();
	}
	
	private void refreshTableGrid() {
		final java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		org.springframework.context.ApplicationContext ctx = alma.scheduling.psm.util.PsmContext.getApplicationContext();
		OutputDao outDao = (OutputDao)ctx.getBean("outDao");
		List<SimulationResults> results = outDao.getResults();
		
		Object[][] model = new Object[results.size()][7];
		
		for(int i = 0; i < results.size(); i++) {
			SimulationResults r = results.get(i);
			model[i][0] = r.getId().toString();
			model[i][1] = (r.getName() == null) ? "" : r.getName(); 
			model[i][2] = format.format(r.getStartRealDate());
			model[i][3] = format.format(r.getStopRealDate());
			model[i][4] = format.format(r.getStartSimDate());
			model[i][5] = format.format(r.getStopSimDate());
			Button b = new org.zkoss.zul.Button("Get");
			b.setHref("servlets/get_reports?id=" + r.getId());
			model[i][6] = b;
		}
		ListModel strset = new SimpleListModel(model);
		grid.setModel(strset);
		grid.setRowRenderer(new alma.scheduling.psm.web.GenericRowRenderer());
	}
}
