package alma.scheduling.planning_mode_sim.test;

import alma.scheduling.planning_mode_sim.gui.SimulationPropertiesTab;


public class TestSimulationPropertiesTab {
    
    public static void main(String[] args) {
        SimulationPropertiesTab tab = new SimulationPropertiesTab();

        tab.setBeginTime("wrong format!"); //totally wrong
        tab.setBeginTime("2003:04-02T11:11:11"); //wrong format
        tab.setBeginTime("2003-04:02T11:11:1"); //wrong format
        tab.setBeginTime("2003-04-02t11:11:1"); //wrong format
        tab.setBeginTime("2003-04-02T11-11:1"); //wrong format
        tab.setBeginTime("2003-04-02T11:11-1"); //wrong format

        tab.setBeginTime("2003-04-02T11:11:1"); //too short
        tab.setBeginTime("2003-04-02T11:11:111"); //too long
    }
}
