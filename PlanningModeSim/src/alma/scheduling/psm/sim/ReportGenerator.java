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
            	SchedBlockReportBean sbr = new SchedBlockReportBean();
            	sbr.setBand("Band " + i);
            	data.add(sbr);
            }
        }
        dataSource =  new JRBeanCollectionDataSource(data);
        return dataSource;
    }
    
	public JasperPrint createCrowdingReport(){
		JRDataSource dataSource = getCrowdingReportData();
        InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/crowdingReport.jasper");
        System.out.println("Creating report");
        try {
			JasperPrint print = JasperFillManager.fillReport(reportStream, new HashMap<Object, Object>(),dataSource);
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
        ArrayList<SchedBlockReportBean> data = new ArrayList<SchedBlockReportBean>();
        HashMap<String, ArrayList<SchedBlockReportBean>> SBPerExecutive = new HashMap<String, ArrayList<SchedBlockReportBean>>();
        System.out.println("Retrieving Data...");
        List<Results> results = outDao.getResults();
        System.out.println("Processing Data...");
        for(Results r: results)
        	for(ObservationProject op: r.getObservationProject()){
        		String exec = op.getAffiliation().iterator().next().getExecutive();
        		ArrayList<SchedBlockReportBean> list = SBPerExecutive.get(exec);
        		if(list == null){
        			list = new ArrayList<SchedBlockReportBean>();
        			SBPerExecutive.put(exec, list);
        		}
        		for(SchedBlockResult sbr: op.getSchedBlock()){
        			SchedBlockReportBean sbrb = new SchedBlockReportBean();
        			double execTime = (sbr.getEndDate().getTime() - sbr.getStartDate().getTime())/1000.0/3600.0;
        			sbrb.setExecutive(exec);
        			sbrb.setExecutionTime(execTime);
        			sbrb.setTotalExecutionTime(sbrb.getTotalExecutionTime() + execTime);
        			list.add(sbrb);
        		}
        	}
        for(ArrayList<SchedBlockReportBean> list: SBPerExecutive.values())
        	data.addAll(list);
        dataSource =  new JRBeanCollectionDataSource(data);
        return dataSource;
	}
	
	public JasperPrint createExecutiveReport() {
		HashMap<String, String> param = new HashMap<String, String>();
		JRDataSource dataSource = getExecutiveUsageOutputData();
        InputStream reportStream = getClass().getClassLoader().getResourceAsStream("alma/scheduling/psm/reports/executiveReport.jasper");
        try {
			return JasperFillManager.fillReport(reportStream, param,dataSource);
		} catch (JRException e) {
			e.printStackTrace();
			return null;
		}
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
