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
 * File GUITimezones.java
 */

package alma.scheduling.planning_mode_sim.gui;

import javax.swing.JComboBox;
/**
 * This class extends the JComboBox from java so that it contains all
 * timezones. This class is used within the GUI classes that require
 * timezones to be set and retrieved.
 * 
 * @author Sohaila Roberts
 */
public class GUITimezones extends JComboBox {
    
    public GUITimezones() {
        super();
        fillTimezones();
    }

    /**
     * Adds all the timezones.
     */
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

    /**
     * Returns as a string the value of the timezone that is 
     * currently selected.
     *
     * @return String 
     */
    public String getValue() {
        String value1 = (String)getSelectedItem();
        String substring = value1.substring(3);
        return substring;
    }


    /**
     * Sets the timezone to the given value.
     * @param s The string representation of what the timezone should be set to.
     */
    public void setValue(String s) {
        if ( s.equals("-12") || s.equals("-12:00") ) {
            setSelectedItem("GMT-12:00");
        } else if ( s.equals("-11") || s.equals("-11:00") ){
            setSelectedItem("GMT-11:00");
        } else if ( s.equals("-10") || s.equals("-10:00") ){
            setSelectedItem("GMT-10:00");
        } else if ( s.equals("-09") || s.equals("-09:00") || s.equals("-9") ){
            setSelectedItem("GMT-09:00");
        } else if ( s.equals("-08") || s.equals("-08:00") || s.equals("-8") ){
            setSelectedItem("GMT-08:00");
        } else if ( s.equals("-07") || s.equals("-07:00") || s.equals("-7") ){
            setSelectedItem("GMT-07:00");
        } else if ( s.equals("-06") || s.equals("-06:00") || s.equals("-6") ){
            setSelectedItem("GMT-06:00");
        } else if ( s.equals("-05") || s.equals("-05:00") || s.equals("-5") ){
            setSelectedItem("GMT-05:00");
        } else if ( s.equals("-04") || s.equals("-04:00") || s.equals("-4") ){
            setSelectedItem("GMT-04:00");
        } else if ( s.equals("-03:30") || s.equals("-3:30") ) {
            setSelectedItem("GMT-03:30");
        } else if ( s.equals("-03") || s.equals("-03:00") || s.equals("-3") ){
            setSelectedItem("GMT-03:00");
        } else if ( s.equals("-02") || s.equals("-02:00") || s.equals("-2") ){
            setSelectedItem("GMT-02:00");
        } else if ( s.equals("-01") || s.equals("-01:00") || s.equals("-1") ){
            setSelectedItem("GMT-01:00");
        } else if ( s.equals("00") || s.equals("0") ){
            setSelectedItem("GMT 00:00");
        } else if ( s.equals("+01") || s.equals("+01:00") || s.equals("+1") ){
            setSelectedItem("GMT+01:00");
        } else if ( s.equals("+02") || s.equals("+02:00") || s.equals("+2") ){
            setSelectedItem("GMT+02:00");
        } else if ( s.equals("+03") || s.equals("+03:00") || s.equals("+3")){
            setSelectedItem("GMT+03:00");
        } else if ( s.equals("+03:30") || s.equals("+3:30")){
            setSelectedItem("GMT+03:30");
        } else if ( s.equals("+04") || s.equals("+04:00") || s.equals("+4")){
            setSelectedItem("GMT+04:00");
        } else if ( s.equals("+04:30") || s.equals("+4:30")){
            setSelectedItem("GMT+04:30");
        } else if ( s.equals("+05") || s.equals("+05:00") || s.equals("+5") ){ 
            setSelectedItem("GMT+05:00");
        } else if ( s.equals("+05:30") || s.equals("+5:30")){
            setSelectedItem("GMT+05:30");
        } else if ( s.equals("+05:45") || s.equals("+5:45") ){
            setSelectedItem("GMT+05:45");
        } else if ( s.equals("+06") || s.equals("+06:00") || s.equals("+6")){
            setSelectedItem("GMT+06:00");
        } else if ( s.equals("+06:30") || s.equals("+6:30")|| s.equals("+6:30") ){
            setSelectedItem("GMT+06:30");
        } else if ( s.equals("+07") || s.equals("+07:00")|| s.equals("+7") ){
            setSelectedItem("GMT+07:00");
        } else if ( s.equals("+08") || s.equals("+08:00") || s.equals("+8")){
            setSelectedItem("GMT+08:00");
        } else if ( s.equals("+09") || s.equals("+09:00") || s.equals("+9")){
            setSelectedItem("GMT+09:00");
        } else if ( s.equals("+09:30") || s.equals("+9:30")) {
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

    /**
     * Returns the string representation of the currently set timezone
     * @return String
     */
    public String toString() {
        return (String)getSelectedItem();
    }
}
