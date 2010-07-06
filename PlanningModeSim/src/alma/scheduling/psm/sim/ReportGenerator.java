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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import alma.scheduling.algorithm.astro.Constants;
import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.output.Results;
import alma.scheduling.datamodel.output.dao.OutputDao;
import alma.scheduling.psm.sim.ReportGenerator;
import alma.scheduling.psm.util.PsmContext;
import alma.scheduling.psm.util.XsltTransformer;

public class ReportGenerator extends PsmContext {
	
	private SchedBlockDao sbDao;
    public static final int ReceiverBandsRange[][] = 
    {{0,0}, {0,0}, {0,0}, {84,116}, {125,169}, {163,211}, {211,275},
        {275,373}, {385,500}, {602,720}};
    
    public ReportGenerator(String contextFile){
    	super(contextFile);
    }
    
    public void crowdingReport() {
        System.out.println("Band\t\tNumber of SchedBlocks\tList of SchedBlocks");
        ApplicationContext ctx = new FileSystemXmlApplicationContext( this.getContextFile() );
        SchedBlockDao sbDao = (SchedBlockDao) ctx.getBean("sbDao");
        
        for (int i = 3; i < ReportGenerator.ReceiverBandsRange.length; i++) {
            List<SchedBlock> sbs = sbDao.findSchedBlockBetweenFrequencies(
                    ReportGenerator.ReceiverBandsRange[i][0],
                    ReportGenerator.ReceiverBandsRange[i][1]);
            String line = i + " (" + ReportGenerator.ReceiverBandsRange[i][0] + "-"
            + ReportGenerator.ReceiverBandsRange[i][1] +")\t" + sbs.size() + "\t";
            //for(SchedBlock sb: sbs){
            //    line += sb.getId() + ";";
            //}
            System.out.println(line);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void printLSTRangesReport() {
        System.out.println("LST Range\tNumber of SchedBlocks\tList of SchedBlocks");
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
        ApplicationContext ctx = new FileSystemXmlApplicationContext( this.getContextFile() );
        OutputDao outDao = (OutputDao) ctx.getBean("outDao");
        Results lastResult = outDao.getResults().get(outDao.getResults().size()-1 );
        
        URL xslt = getClass().getClassLoader()
                                .getResource("alma/scheduling/planning_mode_sim/reports/general_report.xsl");
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
        
        XsltTransformer.transform(xslt.toString(), xmlIn, htmlOut );
    }
    
}
