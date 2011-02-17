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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import org.springframework.context.ApplicationContext;

import alma.ControlSB.SB;
import alma.scheduling.algorithm.astro.Constants;
import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.output.ObservationProject;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.SchedBlockResult;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.psm.reports.domain.SchedBlockReportBean;
import alma.scheduling.psm.reports.domain.ObsProjectReportBean;
import alma.scheduling.psm.sim.ReportGenerator;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.psm.util.XsltTransformer;

public class ReportGenerator extends PsmContext {
	
    public static final int ReceiverBandsRange[][] = 
    {{0,0}, {0,0}, {0,0}, {84,116}, {125,169}, {163,211}, {211,275},
        {275,373}, {385,500}, {602,720}};
    
    public ReportGenerator(String workDir){
    	super(workDir);
    }
    
    public JRDataSource getCrowdingReportData(){
    	JRBeanCollectionDataSource dataSource = null;
        ApplicationContext ctx = ReportGenerator.getApplicationContext();
        SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
        ArrayList<SchedBlockReportBean> data =  new ArrayList<SchedBlockReportBean>();
        for (int i = 3; i < ReportGenerator.ReceiverBandsRange.length; i++) {
        	System.out.println("Retrieving Data...");
            List<SchedBlock> sbs = sbDao.findSchedBlockBetweenFrequencies(
                    ReportGenerator.ReceiverBandsRange[i][0],
                    ReportGenerator.ReceiverBandsRange[i][1]);
            for(SchedBlock sb: sbs){
            	SchedBlockReportBean sbrb = new SchedBlockReportBean();
            	sbrb.setExecutionTime(sb.getObsUnitControl().getMaximumTime());
            	sbrb.setBand("Band " + i);
            	data.add(sbrb);
            }
        }
        dataSource =  new JRBeanCollectionDataSource(data);
        return dataSource;
    }
    
	public JasperPrint createCrowdingReport(){
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("title", "Requested Time per ALMA Band");
		JRDataSource dataSource = getCrowdingReportData();
        InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/crowdingReport.jasper");
        System.out.println("Creating report");
        try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
    }
    
	public void crowdingReport() {
		JasperViewer.viewReport(createCrowdingReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
    }
	
	
	public JRDataSource getExecutiveUsageOutputData() {
		JRBeanCollectionDataSource dataSource = null;
	        ApplicationContext ctx = ReportGenerator.getApplicationContext();
	        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		HashMap<String, ArrayList<ObsProjectReportBean>> OPPerExecutive = new HashMap<String, ArrayList<ObsProjectReportBean>>();
	        ArrayList<ObsProjectReportBean> data = new ArrayList<ObsProjectReportBean>();
	        System.out.println("Retrieving Data...");
	        List<Results> results = outDao.getResults();
	        System.out.println("Processing Data...");
	        for(Results r: results)
	        	for(ObservationProject op: r.getObservationProject()){
				ObsProjectReportBean oprb = new ObsProjectReportBean();
				oprb.setExecutive( op.getAffiliation().iterator().next().getExecutive() );
				oprb.setExecutionTime( op.getExecutionTime() );
				oprb.setGrade( op.getGrade() );
				oprb.setScienceRank( op.getScienceRank() );
				oprb.setScienceScore( op.getScienceScore() );
				if( OPPerExecutive.containsKey( oprb.getExecutive() )){
					OPPerExecutive.get( oprb.getExecutive() ).add( oprb );
				}else{
					ArrayList<ObsProjectReportBean> list = new ArrayList<ObsProjectReportBean>();
					list.add( oprb );
					OPPerExecutive.put( oprb.getExecutive(), list );
				}
	        	}
		for( ArrayList<ObsProjectReportBean> listTmp: OPPerExecutive.values() )
			data.addAll( listTmp );
	        dataSource =  new JRBeanCollectionDataSource(data);
	        return dataSource;
	}
	
	public JasperPrint createExecutiveReport() {
		HashMap<String, String> param = new HashMap();
		JasperPrint print = null;
		InputStream reportStream = getClass()
		.getClassLoader()
		.getResourceAsStream(
				"alma/scheduling/psm/reports/executiveReport.jasper");

		// Parameters
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
                OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		param.put("totalAvailableTime", Double.toString( outDao.getResults().get(0).getAvailableTime() ) );
		synchronized (this) {
			JRDataSource dataSource = getExecutiveUsageOutputData();
			try {
				print = JasperFillManager.fillReport(reportStream, param,
						dataSource);
				return print;
			} catch (JRException e) {
				e.printStackTrace();
			}
		}
		return print;
	}
	
	/**
	 * This method requires a complete simulation   
	 * 
	 */
	public void executiveReport() {
	    System.out.println("Creating report");
        try {
        	JasperViewer.viewReport(createExecutiveReport());
        	Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
	public JRDataSource getBandUsageOutputData() {
		JRBeanCollectionDataSource dataSource = null;
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		OutputDao outDao = (OutputDao) ctx.getBean("outDao");
		ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
		HashMap<String, ArrayList<SchedBlockReportBean>> SBPerBand = new HashMap<String, ArrayList<SchedBlockReportBean>>();
		System.out.println("Retrieving Data...");
		List<Results> results = outDao.getResults();
		System.out.println("Processing Data...");
		SchedBlockReportBean.totalExecutionTime = 0;
		for (Results r : results)
			for (ObservationProject op : r.getObservationProject()) {
				for (SchedBlockResult sbr : op.getSchedBlock()) {
					for (int i = 3; i < ReportGenerator.ReceiverBandsRange.length; i++) {
						if (sbr.getRepresentativeFrequency() >= ReportGenerator.ReceiverBandsRange[i][0]
								&& sbr.getRepresentativeFrequency() < ReportGenerator.ReceiverBandsRange[i][1]) {
							ArrayList<SchedBlockReportBean> list = SBPerBand
									.get("Band " + i);
							if (list == null){
								list = new ArrayList<SchedBlockReportBean>();
								SBPerBand.put("Band " + i, list);
							}
							
							SchedBlockReportBean sbrb = new SchedBlockReportBean();
							sbrb.setBand("Band " + i);
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
		for (ArrayList<SchedBlockReportBean> list : SBPerBand.values())
			data.addAll(list);
		dataSource = new JRBeanCollectionDataSource(data);
		return dataSource;
	}
	
	public JasperPrint createBandUsageReport() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("title", "Executed time per ALMA band");
		JRDataSource dataSource = getBandUsageOutputData();
		InputStream reportStream = getClass().getClassLoader()
				.getResourceAsStream(
						"alma/scheduling/psm/reports/crowdingReport.jasper");
		synchronized (this) {
			System.out.println("Creating report");
			try {
				JasperPrint print = JasperFillManager.fillReport(reportStream,
						props, dataSource);
				return print;
			} catch (JRException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public void bandUsageReport() {
		JasperViewer.viewReport(createBandUsageReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
    }
	
	public JRDataSource getLstRangeBeforeSimData() {
		JRBeanCollectionDataSource dataSource = null;
		ApplicationContext ctx = ReportGenerator.getApplicationContext();
		SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
		ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
		HashMap<String, ArrayList<SchedBlockReportBean>> SBPerLST = new HashMap<String, ArrayList<SchedBlockReportBean>>();
		for (int i = 0; i < 24; i++)
			SBPerLST.put(i + " - " + (i + 1),
					new ArrayList<SchedBlockReportBean>());
		List<SchedBlock> sbs = sbDao.findAll();
		for (SchedBlock sb : sbs) {
			sbDao.hydrateSchedBlockObsParams(sb);
			double ra = sb.getSchedulingConstraints().getRepresentativeTarget()
					.getSource().getCoordinates().getRA() * 24 / 360;
			for (int i = 0; i < 24; i++) {
				if (ra >= i && ra <= (i + 1)) {
					ArrayList<SchedBlockReportBean> list;
					list = SBPerLST.get(i + " - " + (i + 1));
					SchedBlockReportBean sbrb = new SchedBlockReportBean();
					sbrb.setLstRange(i + " - " + (i + 1));
					sbrb.setExecutionTime(sb.getObsUnitControl()
							.getMaximumTime());
					sbrb.setTotalExecutionTime(sbrb.getTotalExecutionTime()
							+ sb.getObsUnitControl().getMaximumTime());
					list.add(sbrb);
					break;
				}
			}
		}
        
        for(int i = 0; i < 24; i++)
        	data.addAll(SBPerLST.get(i + " - " + (i+1)));
        dataSource = new JRBeanCollectionDataSource(data);
        return dataSource;
	}
	

	
	public JasperPrint createLstRangeBeforeSimReport() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("title", "Requested time per LST range ");
		JRDataSource dataSource = getLstRangeBeforeSimData();
        InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/LSTRangesReport.jasper");
        System.out.println("Creating report");
        try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void lstRangeBeforeSimReport() {
		JasperViewer.viewReport(createLstRangeBeforeSimReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
        }
	}
	
	public JRDataSource getLstRangeAfterSimData(){
		JRBeanCollectionDataSource dataSource = null;
        ApplicationContext ctx = ReportGenerator.getApplicationContext();
        SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
        ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
        HashMap<String, ArrayList<SchedBlockReportBean>> SBPerLST = new HashMap<String, ArrayList<SchedBlockReportBean>>();
        for(int i = 0; i < 24; i++)
        	SBPerLST.put(i + " - " + (i+1), new ArrayList<SchedBlockReportBean>());
		List<Results> result = outDao.getResults();
		for (Results r : result) {
			for (ObservationProject p : r.getObservationProject()) {
				for (SchedBlockResult sbr : p.getSchedBlock()) {
					SchedBlock sb = sbDao.findById(SchedBlock.class, sbr.getOriginalId());
					sbDao.hydrateSchedBlockObsParams(sb);
					double ra = sb.getSchedulingConstraints()
							.getRepresentativeTarget().getSource()
							.getCoordinates().getRA() * 24 / 360;
					for (int i = 0; i < 24; i++) {
						if (ra >= i && ra <= (i + 1)) {
							ArrayList<SchedBlockReportBean> list;
							list = SBPerLST.get(i + " - " + (i + 1));
							SchedBlockReportBean sbrb = new SchedBlockReportBean();
							sbrb.setLstRange(i + " - " + (i + 1));
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
		}
        
        for(int i = 0; i < 24; i++)
        	data.addAll(SBPerLST.get(i + " - " + (i+1)));
        dataSource = new JRBeanCollectionDataSource(data);
        return dataSource;
	}
	
	public JasperPrint createLstRangeAfterSimReport() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put("title", "Executed time per LST range ");
		JRDataSource dataSource = getLstRangeAfterSimData();
        InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/LSTRangesReport.jasper");
        System.out.println("Creating report");
        try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, props, dataSource);
			return print;
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void lstRangeAfterSimReport() {
		JasperViewer.viewReport(createLstRangeAfterSimReport());
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
        }
	}
	
    @SuppressWarnings("unchecked")
    public void printLSTRangesReport() {
        System.out.println("LST Range\tNumber of SchedBlocks\tList of SchedBlocks");
        
        ApplicationContext ctx = ReportGenerator.getApplicationContext();
        SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
        
        List<SchedBlock> sbs = sbDao.findAll();
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
    	ApplicationContext ctx = this.getApplicationContext();
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
    
}
