
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
        addItem("GMT-05:00");
        addItem("GMT-04:00");
        addItem("GMT-03:30");
        addItem("GMT-03:00");
        addItem("GMT-02:00");
        addItem("GMT-01:00");
        addItem("GMT 00:00");
        addItem("GMT+01:00");
        addItem("GMT+02:00");
        addItem("GMT+03:00");
        addItem("GMT+03:30");
        addItem("GMT+04:00");
        addItem("GMT+04:30");
        addItem("GMT+05:00");
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

        setSelectedItem("GMT-06:00");

    }

    public String getValue() {
        String value1 = (String)getSelectedItem();
        String substring = value1.substring(3);
        return substring;
    }


    public void setValue(String s) {
        if ( s.equals("-12") || s.equals("-12:00") ) {
            setSelectedItem("GMT-12:00");
        } else if ( s.equals("-11") || s.equals("-11:00") ){
            setSelectedItem("GMT-11:00");
        } else if ( s.equals("-10") || s.equals("-10:00") ){
            setSelectedItem("GMT-10:00");
        } else if ( s.equals("-09") || s.equals("-09:00") ){
            setSelectedItem("GMT-09:00");
        } else if ( s.equals("-08") || s.equals("-08:00") ){
            setSelectedItem("GMT-08:00");
        } else if ( s.equals("-07") || s.equals("-07:00") ){
            setSelectedItem("GMT-07:00");
        } else if ( s.equals("-06") || s.equals("-06:00") ){
            setSelectedItem("GMT-06:00");
        } else if ( s.equals("-05") || s.equals("-05:00") ){
            setSelectedItem("GMT-05:00");
        } else if ( s.equals("-04") || s.equals("-04:00") ){
            setSelectedItem("GMT-04:00");
        } else if ( s.equals("-03:30") ) {
            setSelectedItem("GMT-03:30");
        } else if ( s.equals("-03") || s.equals("-03:00") ){
            setSelectedItem("GMT-03:00");
        } else if ( s.equals("-02") || s.equals("-02:00") ){
            setSelectedItem("GMT-02:00");
        } else if ( s.equals("-01") || s.equals("-01:00") ){
            setSelectedItem("GMT-01:00");
        } else if ( s.equals("00") ){
            setSelectedItem("GMT 00:00");
        } else if ( s.equals("+01") || s.equals("+01:00") ){
            setSelectedItem("GMT+01:00");
        } else if ( s.equals("+02") || s.equals("+02:00") ){
            setSelectedItem("GMT+02:00");
        } else if ( s.equals("+03") || s.equals("+03:00") ){
            setSelectedItem("GMT+03:00");
        } else if ( s.equals("+03:30") ){
            setSelectedItem("GMT+03:30");
        } else if ( s.equals("+04") || s.equals("+04:00") ){
            setSelectedItem("GMT+04:00");
        } else if ( s.equals("+04:30") ){
            setSelectedItem("GMT+04:30");
        } else if ( s.equals("+05") || s.equals("+05:00") ){
            setSelectedItem("GMT+05:00");
        } else if ( s.equals("+05:30") ){
            setSelectedItem("GMT+05:30");
        } else if ( s.equals("+05:45") ){
            setSelectedItem("GMT+05:45");
        } else if ( s.equals("+06") || s.equals("+06:00") ){
            setSelectedItem("GMT+06:00");
        } else if ( s.equals("+06:30") ){
            setSelectedItem("GMT+06:30");
        } else if ( s.equals("+07") || s.equals("+07:00") ){
            setSelectedItem("GMT+07:00");
        } else if ( s.equals("+08") || s.equals("+08:00") ){
            setSelectedItem("GMT+08:00");
        } else if ( s.equals("+09") || s.equals("+09:00") ){
            setSelectedItem("GMT+09:00");
        } else if ( s.equals("+09:30") ) {
            setSelectedItem("GMT+09:30");
        } else if ( s.equals("+10") || s.equals("+10:00") ){
            setSelectedItem("GMT+10:00");
        } else if ( s.equals("+11") || s.equals("+11:00") ){
            setSelectedItem("GMT+11:00");
        } else if ( s.equals("+12") || s.equals("+12:00") ){
            setSelectedItem("GMT+12:00");
        } else if ( s.equals("+13") || s.equals("+13:00") ){
            setSelectedItem("GMT+13:00");
        }
    }

    public String toString() {
        return (String)getSelectedItem();
    }
}
