
package alma.scheduling.planning_mode_sim.test;

import alma.scheduling.planning_mode_sim.gui.AlgorithmWeightsTab;

public class TestAlgorithmWeightsTab {

    public static void main(String[] args) {
        AlgorithmWeightsTab tab = new AlgorithmWeightsTab();

        tab.setPositionElevationWeight("10");
        if( (tab.getPositionElevationWeight()).equals("10") ){
            System.out.println("Position Elevation Weight set correctly");
        } else {
            System.out.println("Position Elevation Weight NOT set correctly");
        }

        tab.setPositionMaxWeight("9");
        if( (tab.getPositionMaxWeight()).equals("9") ){
            System.out.println("Position Max Weight set correctly");
        } else {
            System.out.println("Position Max Weight NOT set correctly");
        }

        tab.setWeatherWeight("8");
        if( (tab.getWeatherWeight()).equals("8") ){
            System.out.println("Weather Weight set correctly");
        } else {
            System.out.println("Weather Weight NOT set correctly");
        }
        
        tab.setSPSBWeight("7");
        if( (tab.getSPSBWeight()).equals("7") ){
            System.out.println("SPSB Weight set correctly");
        } else {
            System.out.println("SPSB Weight NOT set correctly");
        }
        
        tab.setSPDBWeight("6");
        if( (tab.getSPDBWeight()).equals("6") ){
            System.out.println("SPDB Weight set correctly");
        } else {
            System.out.println("SPDB Weight NOT set correctly");
        }
        
        tab.setDPSBWeight("5");
        if( (tab.getDPSBWeight()).equals("5") ){
            System.out.println("DPSB Weight set correctly");
        } else {
            System.out.println("DPSB Weight NOT set correctly");
        }
        
        tab.setDPDBWeight("4");
        if( (tab.getDPDBWeight()).equals("4") ){
            System.out.println("DPDB Weight set correctly");
        } else {
            System.out.println("DPDB Weight NOT set correctly");
        }
        
        tab.setNewProjectWeight("3");
        if( (tab.getNewProjectWeight()).equals("3") ){
            System.out.println("New Project Weight set correctly");
        } else {
            System.out.println("New Project Weight NOT set correctly");
        }
        
        tab.setPriorityWeight("2");
        if( (tab.getPriorityWeight()).equals("2") ){
            System.out.println("Priority Weight set correctly");
        } else {
            System.out.println("Priority Weight NOT set correctly");
        }
        
        tab.setLastSBWeight("1");
        if( (tab.getLastSBWeight()).equals("1") ){
            System.out.println("Last SB Weight set correctly");
        } else {
            System.out.println("Last SB Weight NOT set correctly");
        }

    }

}
