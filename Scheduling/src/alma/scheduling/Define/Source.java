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
 * File Source.java
 */

package alma.scheduling.Define;
/**
  * A class to keep information about the SB's source. 
  * @author sslucero
  */
public class Source {

    private String sourceName;
    private String solarSystemObj;
    private int numberSourceProperties;
    private double visibleMagnitude;
    private String restFrequency; // created with a double and units
    private String transition;
        

    public Source(){
        setToNull();
    }
    
    public void setToNull() {
        sourceName = null;
        solarSystemObj = null;
        numberSourceProperties =0;
        visibleMagnitude = 0.0;
        restFrequency = null;
        transition= null;
    }

    //////////
    public void setSourceName(String s){
        sourceName = s;
    }
    public String getSourceName(){
        return sourceName;
    }
    
    //////////
    public void setSolarSystemObj(String s){
        solarSystemObj =s;
    }
    public String getSolarSystemObj(){
        return solarSystemObj;
    }

    //////////
    public void setNumberSourceProperties(int i){
        numberSourceProperties =i;
    }
    public int getNumberSourceProperties(){
        return numberSourceProperties;
    }
    
    //////////
    public void setVisibleMagnitude(double d){
        visibleMagnitude =d;
    }
    public double getVisibleMagnitude(){
        return visibleMagnitude;
    }
    
    //////////
    public void setRestFrequency(double val, String unit){
        restFrequency = String.valueOf(val) + unit;
    }
    public String getRestFrequency(){
        return restFrequency;
    }

    //////////
    public void setTransition(String s){
        transition = s;
    }
    public String getTransition() {
        return transition;
    }
}
