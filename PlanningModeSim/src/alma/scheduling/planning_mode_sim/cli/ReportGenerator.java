package alma.scheduling.planning_mode_sim.cli;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import alma.scheduling.algorithm.astro.Constants;
import alma.scheduling.algorithm.astro.CoordinatesUtil;
import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ReportGenerator {
    public static final int ReceiverBandsRange[][] = 
    {{0,0}, {0,0}, {0,0}, {84,116}, {125,169}, {163,211}, {211,275},
        {275,373}, {385,500}, {602,720}};
    
    private SchedBlockDao sbDao;
    
    public ReportGenerator(SchedBlockDao sbDao){
        this.sbDao = sbDao;
    }
    
    public void printALMAReceiverBandReport() {
        System.out.println("Band\t\tNumber of SchedBlocks\tList of SchedBlocks");
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
    
}
