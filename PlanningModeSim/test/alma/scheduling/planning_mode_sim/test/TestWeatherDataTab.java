package alma.scheduling.planning_mode_sim.test;

import alma.scheduling.planning_mode_sim.gui.WeatherDataTab;

public class TestWeatherDataTab {

    public static void main(String[] args) {
        WeatherDataTab tab = new WeatherDataTab();
        
        tab.setWeatherName("test 1");
        if( (tab.getWeatherName()).equals("test 1") ){
            System.out.println("Weather name set.");
        } else {
            System.out.println("Weather name NOT set.");
        }

        tab.setUnits("1");
        if( (tab.getUnits()).equals("1") ) {
            System.out.println("Units set.");
        } else {
            System.out.println("Units not set.");
        }

        tab.setP0("p0");
        if( (tab.getP0()).equals("p0") ) {
            System.out.println("P0 set.");
        } else {
            System.out.println("P0 not set.");
        }
        tab.setP1("p1");
        if( (tab.getP1()).equals("p1") ) {
            System.out.println("P1 set.");
        } else {
            System.out.println("P1 not set.");
        }
        tab.setP2("p2");
        if( (tab.getP2()).equals("p2") ) {
            System.out.println("P2 set.");
        } else {
            System.out.println("P2 not set.");
        }

        tab.setS0("s0");
        if( (tab.getS0()).equals("s0") ) {
            System.out.println("S0 set.");
        } else {
            System.out.println("S0 not set.");
        }

        tab.setS1("s1");
        if( (tab.getS1()).equals("s1") ) {
            System.out.println("S1 set.");
        } else {
            System.out.println("S1 not set.");
        }
        tab.setT0("t0");
        if( (tab.getT0()).equals("t0") ) {
            System.out.println("T0 set.");
        } else {
            System.out.println("T0 not set.");
        }
        tab.setT1("t1");
        if( (tab.getT1()).equals("t1") ) {
            System.out.println("T1 set.");
        } else {
            System.out.println("T1 not set.");
        }
        
    }
    
}
