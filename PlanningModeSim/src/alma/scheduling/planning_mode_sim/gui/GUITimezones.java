
package alma.scheduling.planning_mode_sim.gui;

import javax.swing.JComboBox;

public class GUITimezones extends JComboBox {
    
    public GUITimezones() {
        super();
        fillTimezones();
    }
    private void fillTimezones() {
        addItem("GMT-12:00");
        addItem("GMT-11:00");
        addItem("GMT-10:00");
        addItem("GMT-09:00");
        addItem("GMT-08:00");
        addItem("GMT-07:00");
        addItem("GMT-06:00");
        addItem("GMT-04:00");
        addItem("GMT-03:00");
        addItem("GMT-03:30");
        addItem("GMT-02:00");
        addItem("GMT-01:00");
        addItem("GMT 00:00");
        addItem("GMT+01:00");
        addItem("GMT+02:00");
        addItem("GMT+03:00");
        addItem("GMT+03:30");
        addItem("GMT+04:00");
        addItem("GMT+04:30");
        addItem("GMT+05:30");
        addItem("GMT+05:45");
        addItem("GMT+06:00");
        addItem("GMT+06:30");
        addItem("GMT+07:00");
        addItem("GMT+08:00");
        addItem("GMT+09:00");
        addItem("GMT+09:30");
        addItem("GMT+10:00");
        addItem("GMT+11:00");
        addItem("GMT+12:00");
        addItem("GMT+13:00");
    }
}
