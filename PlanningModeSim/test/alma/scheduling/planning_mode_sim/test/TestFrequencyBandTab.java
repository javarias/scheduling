package alma.scheduling.planning_mode_sim.test;

import java.util.Vector;

import alma.scheduling.planning_mode_sim.gui.FrequencyBandTab;

public class TestFrequencyBandTab {

    public static void main(String[] args) {
    
        FrequencyBandTab tab = new FrequencyBandTab();

        tab.setTotalFreq("15"); // expect an error
        
        tab.setTotalFreq("5");
        if( (tab.getTotalFreq()).equals("5") ) {
            System.out.println("Total number of frequency bands set");
        } else {
            System.out.println("Total number of frequency bands NOT set");
        }
        
        Vector v = new Vector();
        v.add("name1"); v.add("30"); v.add("60");
        v.add("name2"); v.add("40"); v.add("50");
        v.add("name3"); v.add("30"); v.add("80");
        v.add("name4"); v.add("10"); v.add("40");
        v.add("name5"); v.add("40"); v.add("90");

        tab.setFrequencyValues(5, v);

        
    }

}
