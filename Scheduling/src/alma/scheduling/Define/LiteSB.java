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
 * File LiteSB.java
 */

package alma.scheduling.Define;

public class LiteSB {

    private String schedBlockRef;
    private String projectRef;
    private String obsUnitsetRef;
    private String sbName;
    private String projectName;
    private String PI;
    private String priority;
    private double ra;
    private double dec;
    private double freq;
    private int maxTime;
    private double score;
    private double success;
    private double rank;

    public LiteSB(){
    }

    public LiteSB(String sbRef,
                  String projRef,
                  String ousRef,
                  String sbName,
                  String projName,
                  String pi,
                  String pri,
                  double ra,
                  double dec,
                  double freq,
                  int mt,
                  double sc,
                  double suc,
                  double r) {
        
        this.schedBlockRef=sbRef;
        this.projectRef= projRef;
        this.obsUnitsetRef=ousRef;
        this.sbName=sbName;
        this.projectName= projName;
        this.PI=pi;
        this.priority=pri;
        this.ra = ra;
        this.dec= dec;
        this.freq=freq;
        this.maxTime= mt;
        this.score=sc;
        this.success=suc;
        this.rank= r;
    }

    // GETTERS
    public String getSBRef() {
        return schedBlockRef;
    }
    public String getProjRef(){
        return projectRef;
    }
    public String getOUSRef() {
        return obsUnitsetRef;
    }
    public String getSBName(){
        return sbName;
    }
    public String getProjName(){
        return projectName;
    }
    public String getPI(){
        return PI;
    }
    public String getPri(){
        return priority;
    }
    public double getRA(){
        return ra;
    }
    public double getDEC() {
        return dec;
    }
    public double getFreq(){
        return freq;
    }
    public int getMaxTime(){
        return maxTime;
    }
    public double getScore(){
        return score;
    }
    public double getSuccess(){
        return success;
    }
    public double getRank(){
        return rank;
    }
    
    // SETTERS
    public void setSBRef(String s) {
        schedBlockRef=s;
    }
    public void setProjRef(String s){
        projectRef=s;
    }
    public void setOUSRef(String s) {
        obsUnitsetRef=s;
    }
    public void setSBName(String s){
        sbName=s;
    }
    public void setProjName(String s){
        projectName=s;
    }
    public void setPI(String s){
        PI=s;
    }
    public void setPri(String s){
        priority=s;
    }
    public void setRA(double d){
        ra=d;
    }
    public void setDEC(double d) {
        dec=d;
    }
    public void setFreq(double d){
        freq=d;
    }
    public void setMaxTime(int l){
        maxTime=l;
    }
    public void setScore(double d){
        score=d;
    }
    public void setSucces(double d){
        success=d;
    }
    public void setRank(double d){
        rank=d;
    }
}
