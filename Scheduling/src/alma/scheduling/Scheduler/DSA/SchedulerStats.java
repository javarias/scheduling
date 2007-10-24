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
 * File SchedulerStats.java
 */
package alma.scheduling.Scheduler.DSA;

import alma.scheduling.Define.DateTime;
import java.util.ArrayList;

public class SchedulerStats {

    private String name;
    private DateTime time;
    private int timeMin;
    private double opacity;
    private double scaledOp;
    private double scaledRms; //scaled rms
    private double rms; //non-scaled rms
    private double wind;
    private double freq;
    private double ha;
    private double el;
    private int priority;
    private boolean executed;
    private ScoreMapping[] scores;
    private ArrayList scoresL;
    
    public SchedulerStats(){
        name = "not set";
        time = null;
        timeMin = 0;
        scaledRms = 0.0;
        rms = 0.0;
        wind = 0.0;
        opacity = 0.0;
        scaledOp = 0.0;
        executed = false;
        scores = null;
        scoresL = new ArrayList();
        priority = 0;
        el = 0.0;
        ha = 0.0;
        freq = 0.0;
    }
    
    public void addScoreMapping(String n, double s){
        ScoreMapping x = new ScoreMapping(n,s);
        scoresL.add(x);
    }

    public void setSBName(String n){
        name = n;
    }
    public void setTime(DateTime t){
        time = t;
    }

    public void setScaledRms(double r){
        scaledRms = r;
    }
    public void setRms(double r){
        rms = r;
    }
    public void setWind(double w){
        wind = w;
    }
    public void setScaedOpacity(double o){
        scaledOp = o;
    }
    public void setOpacity(double o){
        opacity = o;
    }
    public void setFrequency(double f){
        freq= f;
    }
    public void setHa(double x){
        ha = x;
    }
    public void setElevation(double e){
        el = e;
    }
    public  void setPriority(int i){
        priority = i;
    }
    
    public void setExecuted(boolean e){
        executed = e;
    }
    
    /*----------------------------------------------------------*/
    public DateTime getTime(DateTime t){
        return time;
    }
    public double getScaledRms(){
        return scaledRms;
    }
    public double getRms(){
        return rms;
    }
    public double getWind(){
        return wind;
    }
    public double getOpacity(){
        return opacity;
    }
    public double getScaledOpacity(){
        return scaledOp;
    }

    public String getSBName(){
        return name;
    }
    public int getTimeMinutes(){
        return timeMin;
    }
    public double getElevation(){
        return el;
    }
    public int getPriority(){
        return priority;
    }
    public boolean wasExecuted(){
        return executed;
    }
    public ScoreMapping[] getAllScoreMappings(){
        scores = new ScoreMapping[scoresL.size()];
        scores = (ScoreMapping[])scoresL.toArray((ScoreMapping[])scores);
        return scores;
    }

    public String getHeader(){
        return "##NOTE: Each of these entries is NOT an execution! \n"+
               "##1. SB name \n"+
               "##2. Time [y-m-day-h-m-s]\n"+
               "##3. Time [minutes from start of schedule]\n"+
               "##4. Opacity [tau225]\n"+
               "##5. Opacity [at freq. & src el observed]\n"+
               "##6. RMS [at 11.2 GHz]\n"+
               "##7. RMS [at freq. & src el observed]\n"+
               "##8. Wind speed [m/s] \n"+
               "##9. frequency [GHz]-- if nothing scheduled then set to 0.0\n"+
               "##10. HA = LST - RA [hours in range -12 to +12]\n"+
               "##11. Source elevation [ degrees]  - if nothing scheduled then set to 0.0\n"+
               "##12. SB priority [numerical value]\n"+
               //"## --- All sbs considered for this time and the score they got\n"+
               "";
    }
    public String toString(){
        String s = name+"; "+time.toString()+"; "+timeMin+"; "+opacity+"; "+scaledOp+"; "+
            rms+"; "+scaledRms+"; "+wind+"; "+freq+"; "+ha+"; "+el+"; "+priority;
        /*
        ScoreMapping[] x = getAllScoreMappings();
        for(int i=0; i < x.length; i++){
            s += "\n\t"+ x[i].toString();
        }*/
        return s;
    }

    class ScoreMapping {
        private String sbname;
        private double score;

        public ScoreMapping(String n, double d){
            sbname = n;
            score = d;
        }
        public String toString(){
            return sbname+": "+score;
        }
    }
}
