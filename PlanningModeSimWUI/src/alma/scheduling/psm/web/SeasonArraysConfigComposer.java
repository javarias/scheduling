package alma.scheduling.psm.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.zkforge.timeline.Bandinfo;
import org.zkforge.timeline.Timeline;
import org.zkforge.timeline.data.OccurEvent;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Checkbox;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Grid;
import org.zkoss.zul.api.Timebox;
import org.zkoss.zul.api.Window;

import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.TimeInterval;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.observatory.dao.ObservatoryDao;
import alma.scheduling.datamodel.observatory.dao.XmlObservatoryDao;
import alma.scheduling.utils.DSAContextFactory;

public class SeasonArraysConfigComposer extends GenericForwardComposer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1237893763894738947L;

	private List<ArrayConfiguration> arrayConfigs;
	private ObservingSeason obsSeason;
	private TimeInterval previousTI;
	private ExecutiveDAO execDao;
	private ObservatoryDao obsDao;
	private XmlObservatoryDao xmlObsDao;
	private ArrayList<OccurEvent> eventsInTimeline;
	private boolean changedArrayConfigs;
	
	//widgets
	Datebox dateboxStartDate, dateboxStopDate;
	Checkbox checkboxAllowDailyInterval;
	Timebox timeboxStartTime, timeboxEndTime;
	Button buttonLoadConfig, buttonSaveToDiskConfig, buttonSave;
	Grid gridArrayConfigs;
	Timeline tl1;
	Bandinfo b1, b2;
	Window windowSeasonArrayConfig;

	
	public SeasonArraysConfigComposer() {
		execDao = (ExecutiveDAO) DSAContextFactory.getContext().getBean(DSAContextFactory.SCHEDULING_EXECUTIVE_DAO_BEAN);
		obsDao = (ObservatoryDao) DSAContextFactory.getContext().getBean(DSAContextFactory.SCHEDULING_OBSERVATORY_DAO);
		xmlObsDao = (XmlObservatoryDao) DSAContextFactory.getContext().getBean(DSAContextFactory.SCHEDULING_XML_OBSERVATORY_DAO);
		arrayConfigs = obsDao.findArrayConfigurations();
		obsSeason = execDao.getCurrentSeason();
		eventsInTimeline = new ArrayList<OccurEvent>();
		changedArrayConfigs = false;
	}
	
	public void onCheck$checkboxAllowDailyInterval() {
		boolean val = checkboxAllowDailyInterval.isChecked();
		timeboxStartTime.setDisabled(!val);
		timeboxEndTime.setDisabled(!val);
		if (val) {
			if (obsSeason.getObservingInterval() == null) {
				if (previousTI != null) 
					obsSeason.setObservingInterval(previousTI);
				else 
					obsSeason.setObservingInterval(new TimeInterval(new Date(28800000), new Date(72000000)));
			}
			timeboxStartTime.setValue(new Date(obsSeason.getObservingInterval().getStartTime()));
			timeboxEndTime.setValue(new Date(obsSeason.getObservingInterval().getStartTime() 
					+ obsSeason.getObservingInterval().getDuration()));
		} else {
			//Save the current Value
			if (timeboxStartTime.getValue() != null && timeboxEndTime.getValue() != null)
				obsSeason.setObservingInterval(new TimeInterval(timeboxStartTime.getValue(), timeboxEndTime.getValue()));
			//
			previousTI = obsSeason.getObservingInterval();
			obsSeason.setObservingInterval(null);
			timeboxStartTime.setValue(null);
			timeboxEndTime.setValue(null);
		}
	}
	
	public void onUpload$buttonLoadConfig(UploadEvent event) {
		Media media = event.getMedia();
		alma.scheduling.datamodel.observatory.dao.ArrayConfigurationLiteReader reader 
		= new alma.scheduling.datamodel.observatory.dao.ArrayConfigurationLiteReader(media.getReaderData());
		try {
			List<ArrayConfiguration> configs = reader.getArrayConfiguration();
			putGridData(configs);
			arrayConfigs = configs;
			changedArrayConfigs = true;
			setTimelineEvents();
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
	
	public void onChange$dateboxStartDate() {
		obsSeason.setStartDate(dateboxStartDate.getValue());
		setTimelineEvents();
	}
	
	public void onChange$dateboxStopDate() {
		obsSeason.setEndDate(dateboxStopDate.getValue());
		setTimelineEvents();
	}
	
	public void onChange$timeboxStartTime() {
		if (timeboxStartTime.getValue() != null && timeboxEndTime.getValue() != null)
			obsSeason.setObservingInterval(new TimeInterval(timeboxStartTime.getValue(), timeboxEndTime.getValue()));
	}
	
	public void onChange$timeboxEndTime() {
		if (timeboxStartTime.getValue() != null && timeboxEndTime.getValue() != null)
			obsSeason.setObservingInterval(new TimeInterval(timeboxStartTime.getValue(), timeboxEndTime.getValue()));
	}
	
	public void onClick$buttonSave() {
		if (changedArrayConfigs){
			obsDao.deleteAllArrayConfigurations();
			obsDao.saveOrUpdate(arrayConfigs);
		}
		execDao.saveOrUpdate(obsSeason);
		xmlObsDao.saveOrUpdate(arrayConfigs);
		Event closeWindowEvent = new Event("onClose", windowSeasonArrayConfig);
		Events.postEvent(closeWindowEvent);
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		dateboxStartDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateboxStopDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeboxStartTime.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeboxEndTime.setTimeZone(TimeZone.getTimeZone("UTC"));
		if (obsSeason.getObservingInterval() != null) {
			checkboxAllowDailyInterval.setChecked(true);
		}
		onCheck$checkboxAllowDailyInterval();
		arrayConfigs = obsDao.findArrayConfigurations();
		if (arrayConfigs != null)
			putGridData(arrayConfigs);
		setTimelineEvents();
	}
	
	public Date getSeasonStartDate() {
		return obsSeason.getStartDate();
	}
	
	public void setSeasonStartDate(Date startDate) {
		this.obsSeason.setStartDate(startDate);
	}
	
	public Date getSeasonStopDate() {
		return obsSeason.getEndDate();
	}
	
	public void setSeasonStopDate(Date stopDate) {
		this.obsSeason.setEndDate(stopDate);
	}
	
	public Date getStartTime() {
		if(obsSeason.getObservingInterval() != null){
			return new Date(obsSeason.getObservingInterval().getStartTime());
		}
		return null;
	}
	
	public void setStartTime(Date startTime) {
		if(obsSeason.getObservingInterval() != null)
			obsSeason.getObservingInterval().setStartTime(startTime.getTime());
	}
	
	public Date getStopTime() {
		if(obsSeason.getObservingInterval() != null){
			return new Date(obsSeason.getObservingInterval().getStartTime() 
				+ obsSeason.getObservingInterval().getDuration());
		}
		return null;
	}
	
	public void setStopTime(Date stopTime) {
		if(obsSeason.getObservingInterval() != null)
			obsSeason.setObservingInterval(new TimeInterval(new Date(obsSeason.getObservingInterval().getStartTime()), stopTime));
	}

	
	private void putGridData(List<ArrayConfiguration> configs) {
		final java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		String [][] model = new String[configs.size()][7];
		for(int i = 0; i < configs.size(); i++) {
			alma.scheduling.datamodel.observatory.ArrayConfiguration c = 
					(alma.scheduling.datamodel.observatory.ArrayConfiguration) configs.get(i);
			model[i][0] = c.getArrayName();
			model[i][1] = c.getConfigurationName();
			model[i][2] = String.valueOf(c.getNumberOfAntennas());
			if (c.getMinBaseline() == null)
				model[i][3] = "N/A";
			else
				model[i][3] = String.valueOf(c.getMinBaseline());
			if (c.getMaxBaseline() == null)
				model[i][4] = "N/A";
			else
				model[i][4] = String.valueOf(c.getMaxBaseline());
			model[i][5] = format.format(c.getStartTime());
			model[i][6] = format.format(c.getEndTime());
		}
		ListModel strset = new SimpleListModel(model);
		gridArrayConfigs.setModel(strset);
		gridArrayConfigs.setRowRenderer(new alma.scheduling.psm.web.GenericRowRenderer());
	}
	
	private void setTimelineEvents() {
		//First remove all events from timeline
		for (OccurEvent e: eventsInTimeline) {
			b1.removeOccurEvent(e);
			b2.removeOccurEvent(e);
		}
		eventsInTimeline.clear();
		
		b1.setDate(obsSeason.getStartDate());
		b2.setDate(obsSeason.getStartDate());
		b1.scrollToCenter(obsSeason.getStartDate());
		b2.scrollToCenter(obsSeason.getStartDate());
		
		OccurEvent seasonDuration = new OccurEvent();
		seasonDuration.setColor("teal");
		seasonDuration.setStart(obsSeason.getStartDate());
		seasonDuration.setEnd(obsSeason.getEndDate());
		seasonDuration.setText("Cycle duration");
		b1.addOccurEvent(seasonDuration);
		b2.addOccurEvent(seasonDuration);
		eventsInTimeline.add(seasonDuration);
		for (ArrayConfiguration c: arrayConfigs) {
			OccurEvent configDuration = new OccurEvent();
			configDuration.setColor("purple");
			configDuration.setStart(c.getStartTime());
			configDuration.setEnd(c.getEndTime());
			configDuration.setText(c.getConfigurationName());
			b1.addOccurEvent(configDuration);
			b2.addOccurEvent(configDuration);
			eventsInTimeline.add(configDuration);
		}
		
	}
}
