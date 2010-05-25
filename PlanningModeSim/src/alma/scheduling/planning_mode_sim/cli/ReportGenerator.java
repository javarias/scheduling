package alma.scheduling.planning_mode_sim.cli;

import java.util.List;

import alma.scheduling.datamodel.obsproject.SchedBlock;
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
    
}
