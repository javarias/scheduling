/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 */

package alma.scheduling.psm.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.TimeZone;
import java.util.TreeMap;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.SchedBlockResult;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.psm.reports.domain.ObsProjectReportBean;
import alma.scheduling.psm.reports.domain.SchedBlockReportBean;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.psm.util.XsltTransformer;
import alma.scheduling.utils.Constants;
import alma.scheduling.utils.CoordinatesUtil;

public class ReportGenerator extends PsmContext {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

	private static final double ReceiverBandsRange[][] = 
	{
		{ 31.3, 45}, 
		//FIXME: Fix the manner bands are assigned for reports. This upper border should be 90.
		{ 67  , 84}, 
		{ 84  ,116}, 
		{125  ,163}, 
		{163  ,211}, 
		{211  ,275}, 
		{275  ,370},
		{385  ,500}, 
		{602  ,720}, 
		{787  ,950}
		};
	
	private static final String Executives[] = {"EU", "NA", "EA", "CHILE", "NONALMA"};
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static {dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));}
	
	private static enum reportTypes {
		LST_BEFORE_SIM,
		LST_AFTER_SIM,
		EXECUTIVE_BEFORE_SIM,
		EXECUTIVE_AFTER_SIM,
		BAND_BEFORE_SIM,
		BAND_AFTER_SIM,
		COMPLETION,
		COMPLETE
	}

	public ReportGenerator(String workDir){
		super(workDir);
	}

	/** Retrieves data por LST Ranges report, previous any simulation execution.
	 * Retrieves frequencies, bands, time requested, and executive, but orders entries according to LST Range.
	 * FIXME: At this moment, only uses RA. Change to LST.
	 * @return A collection of beans containing the data of all ScheckBlocks.
	 */
	public JRDataSource getLstRangesBeforeSim(){
		JRBeanCollectionDataSource dataSource = null;
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
		ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");
		TreeMap<String, ArrayList<SchedBlockReportBean>> SBPerLstRange = new TreeMap<String, ArrayList<SchedBlockReportBean>>();
		ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
		Collection<SchedBlock> sbs = sbDao.findAll();

		for (int i = 0; i < 24; i++) 
			SBPerLstRange.put( determineLst( 0.1 + i ), new ArrayList<SchedBlockReportBean>() );

		for( SchedBlock sb : sbs ){
			sbDao.hydrateSchedBlockObsParams(sb);
			
			// We exclude all SBs that will not be scheduled.
			if(sb.getSchedBlockControl().getState() == SchedBlockState.CANCELED || sb.getLetterGrade() == ScienceGrade.D )
				continue;
			
			for (int i = 0; i < 24; i++) {
				double ra = sb.getSchedulingConstraints().getRepresentativeTarget().getSource().getCoordinates().getRA() * 24 / 360;
				if (ra >= i && ra <= (i + 1)) {
					SchedBlockReportBean sbrb = new SchedBlockReportBean();

					for( ObservingParameters op : sb.getObservingParameters() )
						if (op instanceof ScienceParameters)
							sbrb.setFrequency(((ScienceParameters) op).getRepresentativeFrequency());
					sbrb.setBand( determineBand( sbrb.getFrequency() ) );
					sbrb.setExecutionTime( sb.getSchedBlockControl().getSbMaximumTime() );
					sbrb.setExecutive( execDao.getExecutive( sb.getPiName()).getName() );

					sbrb.setLstRange( determineLst( ra ) );

					if( SBPerLstRange.containsKey( sbrb.getLstRange() )){
						SBPerLstRange.get( sbrb.getLstRange() ).add( sbrb );
					}else{
						ArrayList<SchedBlockReportBean> list = new ArrayList<SchedBlockReportBean>();
						list.add( sbrb );
						SBPerLstRange.put( sbrb.getLstRange(), list );
					}
					break;
				}
			}
		}

		for (int i = 0; i < 24; i++){
			ArrayList<SchedBlockReportBean> listTmp = SBPerLstRange.get( determineLst( 0.1 + i ) );
			data.addAll( listTmp );
		}
		dataSource =  new JRBeanCollectionDataSource(data);
		return dataSource;
	}

	/** Creates the RA ranges requested time jasper report.
	 * The returned object can then be rendered to screen, or other media, such as PDF.
	 * @return JasperPrint object with the generated report
	 */
	public JasperPrint createLstRangesBeforeSimReport(){
		// Parameters
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
//		OutputDao outDao = (OutputDao) ctx.getBean("outDao");

		TreeMap<String, Object> props = new TreeMap<String, Object>();
//		props.put("totalAvailableTime", Double.toString( outDao.getResults().get(0).getAvailableTime() ) );
//		props.put("scientificTime", Double.toString( outDao.getResults().get(0).getScientificTime() ) );
//		props.put("seasonStart", formatter.format( outDao.getResults().get(0).getObsSeasonStart() ) );
//		props.put("seasonEnd", formatter.format( outDao.getResults().get(0).getObsSeasonEnd() ) );
		props.put("title", "Right Ascension Distribution");
		props.put("subtitle", "Requested time per RA range");

		JRDataSource dataSource = getLstRangesBeforeSim();
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/lstRangesBeforeSim.jasper");
		logger.info("Creating RA ranges before simulation report");
		try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			print.setName("ra_time_requested_report");
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JasperPrint createLstRangesBeforeSimReport(Long id) {
		// Parameters
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		Results result = outDao.getResult(id);
		TreeMap<String, Object> props = new TreeMap<String, Object>();
		props.put("totalAvailableTime", Double.toString(result.getAvailableTime()));
		props.put("seasonStart", dateFormatter.format(result.getObsSeasonStart()));
		props.put("seasonEnd", dateFormatter.format(result.getObsSeasonEnd()));
		props.put("title", "Frequency Bands Usage");
		props.put("subtitle", "Requested time per frequency bands");
		JRDataSource dataSource = getBandsBeforeSimData();
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/lstRangesBeforeSim.jasper");
		logger.info("Creating RA ranges before simulation report");
		try {
			JasperPrint print = JasperFillManager.fillReport(reportStream,
					props, dataSource);
			print.setName("ra_time_requested_report");
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** Pops up a report using Swing interface providad by JasperReports.
	 * Useful for console version of PMS.
	 */
	public void lstRangesBeforeSim(){
		JasperViewer.viewReport( createLstRangesBeforeSimReport() );
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** Retrieves data por LST Ranges report, after a simulation execution.
	 * Retrieves frequencies, bands, time requested, and executive, but orders
	 *  entries according to LST Range.
	 * FIXME: At this moment, only uses RA. Change to LST.
	 * @return A collection of beans containing the data of all ScheckBlocks.
	 */

	public JRDataSource getLstRangeAfterSimData(Results result){
		JRBeanCollectionDataSource dataSource = null;
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
		
		ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
		TreeMap<String, ArrayList<SchedBlockReportBean>> SBPerLST = new TreeMap<String, ArrayList<SchedBlockReportBean>>();
		for(int i = 0; i < 24; i++)
			SBPerLST.put(i + " - " + (i+1), new ArrayList<SchedBlockReportBean>());
		for (ObservationProject p : result.getObservationProject()) {
			for (SchedBlockResult sbr : p.getSchedBlock()) {
				SchedBlock sb = sbDao.findById(sbr.getOriginalId());
				if (sb == null){
					logger.warn("Trying to retrieve SchedBlock " + sbr.getOriginalId() + "returned an empty reference");
					continue;
				}
				sbDao.hydrateSchedBlockObsParams(sb);
				double ra = sb.getSchedulingConstraints().getRepresentativeTarget().getSource()
						.getCoordinates().getRA() * 24 / 360;
				for (int i = 0; i < 24; i++) {
					if (ra >= i && ra <= (i + 1)) {
						ArrayList<SchedBlockReportBean> list;
						list = SBPerLST.get(i + " - " + (i + 1));
						SchedBlockReportBean sbrb = new SchedBlockReportBean();
						sbrb.setLstRange( determineLst( ra ) );
						double execTime = (sbr.getEndDate().getTime() - sbr
								.getStartDate().getTime()) / 1000.0 / 3600.0;
						sbrb.setExecutionTime(execTime);
						sbrb.setTotalExecutionTime(sbrb.getTotalExecutionTime() + execTime);
						list.add(sbrb);
						break;
					}
				}
			}
		}

		for(int i = 0; i < 24; i++)
			data.addAll(SBPerLST.get(i + " - " + (i+1)));
		dataSource = new JRBeanCollectionDataSource(data);
		return dataSource;
	}

	/** Creates the RA ranges requested time jasper report.
	 * The returned object can then be rendered to screen, or other media, such as PDF.
	 * @return JasperPrint object with the generated report
	 */
	public JasperPrint createLstRangeAfterSimReport(long id) {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");

		Results result = outDao.getResult(id);
		TreeMap<String, Object> props = new TreeMap<String, Object>();
		props.put("totalAvailableTime", Double.toString(result.getAvailableTime()));
		props.put("scientificTime", Double.toString(result.getScientificTime()));
		props.put("seasonStart", dateFormatter.format(result.getObsSeasonStart()));
		props.put("seasonEnd", dateFormatter.format(result.getObsSeasonEnd()));
		props.put("title", "Right Ascension Distribution");
		props.put("subtitle", "Observed time per RA range");
		JRDataSource dataSource = getLstRangeAfterSimData(outDao.getResult(id));
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/lstRangesBeforeSim.jasper");
		logger.info("Creating RA ranges after simulation report");
		try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			print.setName("ra_time_used_report");
			return print;
		}catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JasperPrint createLstRangeAfterSimReport() {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		return createLstRangeAfterSimReport(outDao.getLastResultId());
	}

	/** Pops up a report using Swing interface providad by JasperReports.
	 * Useful for console version of PMS.
	 */
	public void lstRangesAfterSim() {
		JasperViewer.viewReport(createLstRangeAfterSimReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** Obtains and orders necessary data for the creation of executives reports.
	 * The returned data is used by "createExecutiveReport()" and "createCompletionReport()"
	 *  to create the reports.
	 * Data returned is a collection (JRDataSource), of ObsProjectReportBean,
	 * which contains only a brief summary of observation projects executions.
	 * @return JRDataSource, a collection of ObsProjectReportsBean, with summary of executions.
	 */
	private JRDataSource getExecutiveAfterSimData(Results result) {
		JRBeanCollectionDataSource dataSource = null;
		TreeMap<String, ArrayList<ObsProjectReportBean>> OPPerExecutive = new TreeMap<String, ArrayList<ObsProjectReportBean>>();
		ArrayList<ObsProjectReportBean> data = new ArrayList<ObsProjectReportBean>();
		
		for(ObservationProject op: result.getObservationProject()) {
			ObsProjectReportBean oprb = new ObsProjectReportBean();
			oprb.setExecutive( op.getAffiliation().iterator().next().getExecutive() );
			oprb.setExecutionTime( op.getExecutionTime() );
			oprb.setGrade( op.getGrade() );
			oprb.setScienceRank( op.getScienceRank() );
			oprb.setScienceScore( op.getScienceScore() );
			oprb.setStatus(op.getStatus().toString());
			if( OPPerExecutive.containsKey( oprb.getExecutive() )){
				OPPerExecutive.get( oprb.getExecutive() ).add( oprb );
			}else{
				ArrayList<ObsProjectReportBean> list = new ArrayList<ObsProjectReportBean>();
				list.add( oprb );
				OPPerExecutive.put( oprb.getExecutive(), list );
			}
		}
		
		for( int i = 0; i < Executives.length ; i++ )
			try {
			data.addAll( OPPerExecutive.get( Executives[i] ) );
			} catch (NullPointerException ex ){ 
				logger.warn("No data available for " + Executives[i]);
			}
		dataSource =  new JRBeanCollectionDataSource(data);
		return dataSource;
	}
	
	public JRDataSource getExecutiveAfterSimData(long id) {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		return getExecutiveAfterSimData(outDao.getResult(id));
		
	}

	/** Creates the executive time allocation jasper report.
	 * The returned object can then be rendered to screen, or other media, such as PDF.
	 * @return JasperPrint object with the generated report
	 */
	public JasperPrint createExecutiveReport(long id) {
		JasperPrint print = null;
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream(
		"alma/scheduling/psm/reports/executiveReport.jasper");

		// Parameters
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");

		TreeMap<String, Object> props = new TreeMap<String, Object>();
		props.put("totalAvailableTime", Double.toString( outDao.getResults().get(0).getAvailableTime() ) );
		props.put("scientificTime", Double.toString( outDao.getResults().get(0).getScientificTime() ) );
		props.put("seasonStart", dateFormatter.format( outDao.getResults().get(0).getObsSeasonStart() ) );
		props.put("seasonEnd", dateFormatter.format( outDao.getResults().get(0).getObsSeasonEnd() ) );
		props.put("title", "Executive Percentage Balance");
		props.put("subtitle", "Observed time dedicated per executive");
		synchronized (this) {
			JRDataSource dataSource = getExecutiveAfterSimData(id);
			try {
				print = JasperFillManager.fillReport(reportStream, props,
						dataSource);
				print.setName("executive_time_used_report");
				return print;
			} catch (JRException e) {
				e.printStackTrace();
			}
		}
		return print;
	}
	
	public JasperPrint createExecutiveReport() {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		return createExecutiveReport(outDao.getLastResultId());
	}

	/** Pops up a report using Swing interface providad by JasperReports.
	 * Useful for console version of PMS.
	 */
	public void executiveAfterSim() {
		logger.info("Creating executive report");
		try {
			JasperViewer.viewReport(createExecutiveReport());
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dummy method, implement the real report.   
	 */
	public void executiveBeforeSim() {
		logger.info("Creating executive report");
		try {
			JasperViewer.viewReport(createExecutiveReport());
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** Creates the report for the observation projects completion report.
	 * This can then be rendered to different media. 
	 * 
	 * @return JasperPrint object containing the processed completion report.
	 */
	public JasperPrint createCompletionReport(long id) {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");

		TreeMap<String, Object> props = new TreeMap<String, Object>();
		
		props.put("totalAvailableTime", Double.toString( outDao.getResults().get(0).getAvailableTime() ) );
		props.put("seasonStart", dateFormatter.format( outDao.getResults().get(0).getObsSeasonStart() ) );
		props.put("seasonEnd", dateFormatter.format( outDao.getResults().get(0).getObsSeasonEnd() ) );
		props.put("title", "Observation Projects Completion");
		props.put("subtitle", "Number of observation projects per executive");

		JasperPrint print = null;
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream(
		"alma/scheduling/psm/reports/completionReport.jasper");
		synchronized (this) {
			JRDataSource dataSource = getExecutiveAfterSimData(id);
			try {
				print = JasperFillManager.fillReport(reportStream, props, dataSource);
				print.setName("completion_report");
				return print;
			} catch (JRException e) {
				e.printStackTrace();
			}
		}
		return print;
	}
	
	public JasperPrint createCompletionReport() {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		return createCompletionReport(outDao.getLastResultId());
	}

	/** Pops up a window with a rendered version of the Observation Projects completion report.
	 * This method requires a complete simulation.
	 * This is useful for the CLI version of the PMS.
	 */
	public void completionReport() {
		logger.info("Creating completion report.");
		try {
			JasperViewer.viewReport(createCompletionReport());
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Creation of completion report complete");
	}
	
	/** Obtains and orders necessary data for the creation of bands requested time reports.
	 * The returned data is used by "createBandsBeforeSimReport()" to create the reports.
	 * Data returned is a collection (JRDataSource), of SchedBlockReportBean,
	 * which contains only a brief summary of scheduling blocks to be executed.
	 * 
	 * @return JRDataSource, a collection of SchedBlockReportBean, with summary of requests.
	 */   
	public JRDataSource getBandsBeforeSimData(){
		JRBeanCollectionDataSource dataSource = null;
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
		ArrayList<SchedBlockReportBean> data =  new ArrayList<SchedBlockReportBean>();
		
		// Create inmediately the entries for bands, so we keep order (if we rely on DB, the may not be ordered
		TreeMap<String, ArrayList<SchedBlockReportBean>> SBPerBand = new TreeMap<String, ArrayList<SchedBlockReportBean>>();
		for( int i = ReportGenerator.ReceiverBandsRange.length -1; i >= 0; i--) {
			ArrayList<SchedBlockReportBean> list = new ArrayList<SchedBlockReportBean>();
			SBPerBand.put( determineBand( ReportGenerator.ReceiverBandsRange[i][0] + 0.1), list );
		}
		
		// Retrieve SBs
		logger.info("Retrieving Data...");
		Collection<SchedBlock> sbs = sbDao.findAll();
		
		for(SchedBlock sb: sbs){
			sbDao.hydrateSchedBlockObsParams( sb );
			// We exclude all SBs that will not be scheduled.
			if(sb.getSchedBlockControl().getState() == SchedBlockState.CANCELED 
					|| sb.getLetterGrade() == ScienceGrade.D )
				continue;
			
			SchedBlockReportBean sbrb = new SchedBlockReportBean();
			for( ObservingParameters op : sb.getObservingParameters() )
				if (op instanceof ScienceParameters)
					sbrb.setFrequency(((ScienceParameters) op).getRepresentativeFrequency());
			sbrb.setBand( determineBand( sbrb.getFrequency()));
			sbrb.setExecutionTime(sb.getObsUnitControl().getEstimatedExecutionTime() );
			
			ArrayList<SchedBlockReportBean> list = SBPerBand.get( sbrb.getBand() );
			if( list == null ){
				logger.error("Frequency not in a band that ALMA currently manages: " + sbrb.getFrequency() );
				continue;
			}
			list.add(sbrb);
		}
		
		Stack<ArrayList<SchedBlockReportBean>> tmpStack = new Stack<ArrayList<SchedBlockReportBean>>();
		for (ArrayList<SchedBlockReportBean> list : SBPerBand.values())
			tmpStack.push( list );
			
		while( !tmpStack.isEmpty() )
			data.addAll( tmpStack.pop() );

		dataSource = new JRBeanCollectionDataSource(data);
		return dataSource;
	}

	/** Creates the bands requested time jasper report.
	 * The returned object can then be rendered to screen, or other media, such as PDF.
	 * @return JasperPrint object with the generated report
	 */
	public JasperPrint createBandsBeforeSimReport(){
		// Parameters
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		ExecutiveDAO execDao = (ExecutiveDAO) ctx.getBean("execDao");

		ObservingSeason currObsSeason = execDao.getCurrentSeason();
		TreeMap<String, Object> props = new TreeMap<String, Object>();
//		props.put("totalAvailableTime", Double.toString( outDao.getResults().get(0).getAvailableTime() ) );
		props.put("seasonStart", dateFormatter.format( currObsSeason.getStartDate() ) );
		props.put("seasonEnd", dateFormatter.format( currObsSeason.getEndDate() ) );
		props.put("title", "Frequency Bands Usage");
		props.put("subtitle", "Requested time per frequency bands");
		JRDataSource dataSource = getBandsBeforeSimData();
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/bandsBeforeSim.jasper");
		logger.info("Creating crowding report");
		try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			print.setName("band_time_requested_report");
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/** Creates the bands requested time jasper report.
	 * The returned object can then be rendered to screen, or other media, such as PDF.
	 * @return JasperPrint object with the generated report
	 */
	public JasperPrint createBandsBeforeSimReport(long id){
		// Parameters
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");

		Results result = outDao.getResult(id);
		TreeMap<String, Object> props = new TreeMap<String, Object>();
		props.put("totalAvailableTime", Double.toString(result.getAvailableTime()));
		props.put("seasonStart", dateFormatter.format(result.getObsSeasonStart()));
		props.put("seasonEnd", dateFormatter.format(result.getObsSeasonEnd()));
		props.put("title", "Frequency Bands Usage");
		props.put("subtitle", "Requested time per frequency bands");
		JRDataSource dataSource = getBandsBeforeSimData();
		InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/bandsBeforeSim.jasper");
		logger.info("Creating crowding report");
		try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			print.setName("band_time_requested_report");
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}


	/** Pops up a report using Swing interface providad by JasperReports.
	 * Useful for console version of PMS.
	 */
	public void bandsBeforeSim() {
		JasperViewer.viewReport(createBandsBeforeSimReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private JRDataSource getBandsAfterSimData(Results result) {
		JRBeanCollectionDataSource dataSource = null;
		ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
		// Create inmediately the entries for bands, so we keep order (if we rely on DB, the may not be ordered
		TreeMap<String, ArrayList<SchedBlockReportBean>> SBPerBand = new TreeMap<String, ArrayList<SchedBlockReportBean>>();
		for( int i = ReportGenerator.ReceiverBandsRange.length -1; i >= 0; i--) {
			ArrayList<SchedBlockReportBean> list = new ArrayList<SchedBlockReportBean>();
			SBPerBand.put( determineBand( ReportGenerator.ReceiverBandsRange[i][0] + 0.1), list );
		}		
		
		SchedBlockReportBean.totalExecutionTime = 0;
		for (ObservationProject op : result.getObservationProject()) {
			for (SchedBlockResult sbr : op.getSchedBlock()) {
				SchedBlockReportBean sbrb = new SchedBlockReportBean();
				sbrb.setFrequency( sbr.getRepresentativeFrequency() );
				sbrb.setBand( determineBand(sbr.getRepresentativeFrequency()) );
				sbrb.setExecutionTime( sbr.getExecutionTime() );
				
				ArrayList<SchedBlockReportBean> list = SBPerBand.get( sbrb.getBand() );
				if( list == null ){
					logger.error("Frequency not in ALMA band currently managed: " + sbrb.getFrequency() );
					continue;
				}
				list.add(sbrb);
			}
		}

		Stack<ArrayList<SchedBlockReportBean>> tmpStack = new Stack<ArrayList<SchedBlockReportBean>>();
		for (ArrayList<SchedBlockReportBean> list : SBPerBand.values())
			tmpStack.push( list );
			
		while( !tmpStack.isEmpty() )
			data.addAll( tmpStack.pop() );
		
		dataSource = new JRBeanCollectionDataSource(data);
		return dataSource;
	}
	
	public JRDataSource getBandsAfterSimData(long id) {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		return getBandsAfterSimData(outDao.getResult(id));
	}

	/** Creates a project for band crowding data, after simulations.
	 * This method returns a JasperPrint object, which can be used for rendering
	 * the report to several different medias.
	 * @param id the results used to create the report
	 * @return JasperPrint object, containing the receiver band report after simulations.
	 */
	public JasperPrint createBandsAfterSimReport(long id) {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");


		TreeMap<String, Object> props = new TreeMap<String, Object>();
		Results result = outDao.getResult(id);
		props.put("totalAvailableTime", Double.toString(result.getAvailableTime()));
		props.put("seasonStart", dateFormatter.format(result.getObsSeasonStart()));
		props.put("seasonEnd", dateFormatter.format(result.getObsSeasonEnd()));
		props.put("title", "Receiver Bands Crowding");
		props.put("subtitle", "Time observed per band");
		JRDataSource dataSource = getBandsAfterSimData(id);
		InputStream reportStream = getClass().getClassLoader()
		.getResourceAsStream(
				"alma/scheduling/psm/reports/bandsBeforeSim.jasper");
		synchronized (this) {
			logger.info("Creating band usage report");
			try {
				JasperPrint print = JasperFillManager.fillReport(reportStream,
						props, dataSource);
				print.setName("band_time_used_report");
				return print;
			} catch (JRException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/** 
	 * Uses the very last result to create the report.
	 * 
	 * @see ReportGenerator#createBandsAfterSimReport(long)
	 * @return JasperPrint object, containing the receiver band report after simulations.
	 */
	public JasperPrint createBandsAfterSimReport() {
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		return createBandsAfterSimReport(outDao.getLastResultId());
	}

	/** Pops up a window with the Receiver bands report after simulation.
	 * Useful for CLI version of PMS. 
	 */
	public void bandsAfterSim() {
		JasperViewer.viewReport(createBandsAfterSimReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}





	@SuppressWarnings("unchecked")
	public void printLSTRangesReport() {
		logger.info("LST Range\tNumber of SchedBlocks\tList of SchedBlocks");

		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");

		Collection<SchedBlock> sbs = sbDao.findAll();
		ArrayList<SchedBlock> lstRanges[] = new ArrayList[24];
		for(int i = 0; i < lstRanges.length; i++)
			lstRanges[i] = new ArrayList<SchedBlock>();
		for(SchedBlock sb: sbs){
			SkyCoordinates coord = sb.getSchedulingConstraints().getRepresentativeTarget().getSource().getCoordinates();
			FieldSourceObservability fso = CoordinatesUtil
			.getRisingAndSettingParameters(coord,
					Constants.CHAJNANTOR_LATITUDE,
					Constants.CHAJNANTOR_LONGITUDE);
			if(fso.getSettingTime() == null || fso.getRisingTime() == null)
				break;
			double zenithTime = (fso.getSettingTime() - fso.getRisingTime()) / 2.0;
			if(zenithTime < 0){
				zenithTime = (fso.getRisingTime() - fso.getSettingTime()) / 2.0;
				zenithTime += fso.getRisingTime();
				if(zenithTime >= 24)
					zenithTime -= 24;
			}
			System.out.println(zenithTime);
			lstRanges[(int)zenithTime].add(sb);
		}

		for(int i = 0; i < lstRanges.length ; i++){
			System.out.println(i + "-" + (i+1) + "\t" + lstRanges[i].size());
		}
	}


	public void finalreport(){
		ApplicationContext ctx = getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		Results lastResult = outDao.getResults().get(outDao.getResults().size()-1 );

		URL xslt = getClass().getClassLoader()
		.getResource("alma/scheduling/psm/reports/general_report.xsl");
		// TODO: Change names to a more characteristic name
		String xmlIn = this.getOutputDirectory() + "/" + 
		"output_" +
		lastResult.getStartRealDate().getTime() + 
		".xml";
		String htmlOut = this.getReportDirectory() + "/" + 
		"report_" +
		lastResult.getStartRealDate().getTime() + 
		".html";
		System.out.println("URL ofr xslt: " + xslt.toString());

		XsltTransformer.transform( xslt.toString(), xmlIn, htmlOut );
	}
	
	public void writeFinalReportToDisc() {
		ApplicationContext ctx = getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		Results lastResult = outDao.getResults().get(outDao.getResults().size()-1 );
		String htmlOut = this.getReportDirectory() + "/" +  "report_" +
			lastResult.getStartRealDate().getTime() + ".html";
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(htmlOut);
			InputStream html = getFinalReport();
			int b = 0;
			while ((b = html.read()) != -1 ){
				fos.write(b);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param entityId corresponds to the id of {@code Results}
	 * @return The InputStream of the processed report in HTML.
	 * @throws FileNotFoundException if the report was not 
	 * generated previously at the end of the simulated season. 
	 */
	public InputStream getFinalReport(long entityId) throws FileNotFoundException {
		ApplicationContext ctx = getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		Results result = outDao.getResult(entityId);
		return getFinalReport(result);
	}
	
	/**
	 * 
	 * @return the report for the very last results as InputStream of 
	 * the processed report in HTML.
	 * @throws FileNotFoundException if the report was not 
	 * generated previously at the end of the simulated season.
	 */
	public InputStream getFinalReport() throws FileNotFoundException {
		ApplicationContext ctx = getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		Results result = outDao.getLastResult();
		return getFinalReport(result);
	}
	
	private InputStream getFinalReport(Results result) throws FileNotFoundException {
		URL xslt = getClass().getClassLoader()
			.getResource("alma/scheduling/psm/reports/general_report.xsl");
		// TODO: Change names to a more characteristic name
		try {
			String xmlIn = this.getOutputDirectory() + "/" +  "output_" +
					result.getStartRealDate().getTime() + ".xml";
			logger.debug("URL for xslt: " + xslt.toString());
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			FileInputStream is;
			is = new FileInputStream(xmlIn);
			XsltTransformer.transform( xslt.toString(), is, os );
			return new ByteArrayInputStream(os.toByteArray());
		} catch (Exception ex) {
			String html = "<html><title>Error. Report not found</title>" +
					"<body><h1>The report you tried to obatain doesn't exist or it has been not yet created.</h1></body></html>";
			return new ByteArrayInputStream(html.getBytes());
		}
	}
	
	public void createAllReports(){

		try{

			JasperPrint report = createLstRangesBeforeSimReport();
			OutputStream output = new FileOutputStream(new File( getFilename(reportTypes.LST_BEFORE_SIM)));
			JasperExportManager.exportReportToPdfStream(report, output);

			report = createLstRangeAfterSimReport();
			output = new FileOutputStream(new File( getFilename(reportTypes.LST_AFTER_SIM)));
			JasperExportManager.exportReportToPdfStream(report, output);

			//    	report = createLstRangeAfterSimReport();
			//    	output = new FileOutputStream(new File( getFilename(reportTypes.EXECUTIVE_BEFORE_SIM)));
			//    	JasperExportManager.exportReportToPdfStream(report, output);

			report = createExecutiveReport();
			output = new FileOutputStream(new File( getFilename(reportTypes.EXECUTIVE_AFTER_SIM)));
			JasperExportManager.exportReportToPdfStream(report, output);

	    	report = createBandsBeforeSimReport();
	    	output = new FileOutputStream(new File( getFilename(reportTypes.BAND_BEFORE_SIM)));
	    	JasperExportManager.exportReportToPdfStream(report, output);
	    	
	    	report = createBandsAfterSimReport();
	    	output = new FileOutputStream(new File( getFilename(reportTypes.BAND_AFTER_SIM)));
	    	JasperExportManager.exportReportToPdfStream(report, output);

			report = createCompletionReport();
			output = new FileOutputStream(new File( getFilename(reportTypes.COMPLETION)));
			JasperExportManager.exportReportToPdfStream(report, output);
			//    	
			//    	report = createLstRangeAfterSimReport();
			//    	output = new FileOutputStream(new File( getFilename(reportTypes.COMPLETE)));
			//    	JasperExportManager.exportReportToPdfStream(report, output);

		}catch(JRException e){
			e.printStackTrace();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}

	private String getFilename(reportTypes type){
		ApplicationContext ctx = getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		Results lastResult = outDao.getResults().get(outDao.getResults().size()-1 );
		Date date = lastResult.getStartRealDate();

		String s = dateFormatter.format(date);

		String base = this.getReportDirectory() + "/";
		if( type == reportTypes.LST_BEFORE_SIM )
			return base + "LSTRangesBeforeSimReport" +  "_" + s + ".pdf";
		else if( type == reportTypes.LST_AFTER_SIM )
			return base + "LSTRangesAfterSimReport" +  "_" + s + ".pdf";
		else if( type == reportTypes.EXECUTIVE_BEFORE_SIM )
			return base + "ExecutiveTimeBeforeSim" +  "_" + s + ".pdf";
		else if( type == reportTypes.EXECUTIVE_AFTER_SIM )
			return base + "ExecutiveTimeAfterSim" +  "_" + s + ".pdf";
		else if( type == reportTypes.BAND_BEFORE_SIM )
			return base + "BandsBeforeSim" +  "_" + s + ".pdf";
		else if( type == reportTypes.BAND_AFTER_SIM )
			return base + "BandsAfterSim" +  "_" + s + ".pdf";
		else if( type == reportTypes.COMPLETION )
			return base + "ObsProjectCompletion" +  "_" + s + ".pdf";
		else if( type == reportTypes.COMPLETE )
			return base + "Complete" +  "_" + s + ".html";

		return null;

	}

	private String determineBand( double frequency){
		for( int i = 0; i < ReportGenerator.ReceiverBandsRange.length; i++) {
			if( frequency >= ReportGenerator.ReceiverBandsRange[i][0] && frequency <= ReportGenerator.ReceiverBandsRange[i][1] )
				return new String("Band " + (i+1) );
		}
		return new String("Band N/A");
	}

	private String determineLst( double ra ){
		for (int i = 0; i < 24; i++) {
			if (ra >= i && ra <= (i + 1))
				return new String( i + "-" + (i + 1) );
		}
		return new String("N/A");
	}

}
