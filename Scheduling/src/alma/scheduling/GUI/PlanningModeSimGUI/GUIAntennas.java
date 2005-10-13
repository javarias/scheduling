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
 * File GUIAntennas.java
 */

package alma.scheduling.GUI.PlanningModeSimGUI;

import javax.swing.JComboBox;

public class GUIAntennas extends JComboBox {
    
    public GUIAntennas() {
        super();
        fillAntennas();
    }
    private void fillAntennas() {
        addItem("0");
        addItem("1"); addItem("2"); addItem("3"); 
        addItem("4"); addItem("5"); addItem("6"); 
        addItem("7"); addItem("8"); addItem("9"); 
        addItem("10"); addItem("11"); addItem("12"); 
        addItem("13"); addItem("14"); addItem("15"); 
        addItem("16"); addItem("17"); addItem("18"); 
        addItem("19"); addItem("20"); addItem("21"); 
        addItem("22"); addItem("23"); addItem("24"); 
        addItem("28"); addItem("29"); addItem("30"); 
        addItem("31"); addItem("32"); addItem("33"); 
        addItem("34"); addItem("35"); addItem("36"); 
        addItem("37"); addItem("38"); addItem("39"); 
        addItem("40"); addItem("41"); addItem("42"); 
        addItem("43"); addItem("44"); addItem("45"); 
        addItem("46"); addItem("47"); addItem("48"); 
        addItem("49"); addItem("50"); addItem("51"); 
        addItem("52"); addItem("53"); addItem("54"); 
        addItem("55"); addItem("56"); addItem("57"); 
        addItem("58"); addItem("59"); addItem("60"); 
        addItem("61"); addItem("62"); addItem("63"); 
        addItem("64");

        setSelectedItem("0");
    }

    public void setValue(String s) {
        setSelectedItem(s);
    }

    public String getValue(){
        return (String)getSelectedItem();
    }
}
