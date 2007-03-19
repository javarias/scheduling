/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File CreateArrayController.java
 *
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.Control.ControlMaster;
import alma.Control.AntennaStateEvent;
import alma.scheduling.Define.*;
import alma.scheduling.ArrayModeEnum;

import alma.common.gui.chessboard.*;
import alma.TMCDB.*;
import alma.TMCDB.generated.*;
import alma.TMCDB_IDL.*;

public class CreateArrayController extends SchedulingPanelController {

    private ControlMaster control;
    private TMCDBComponent tmcdb = null;
    private ChessboardEntry[][] allEntries = null;   
    private ChessboardEntry[] entries12 = new ChessboardEntry[50];
    private ChessboardEntry[] entries7 = new ChessboardEntry[12];
    private ChessboardEntry[] entriesTP = new ChessboardEntry[4];
    private Map map12;

    public CreateArrayController(){//PluginContainerServices cs){
        super();
    }
    public void secondSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        if(cs == null) {
            logger.info("**************** probelm ");
        }
    }

    protected void updateChessboardWithALMANames(){
        askTMCDBForRealAntennaInfo();
        createOfflineChessboards();
    }

    private void askTMCDBForRealAntennaInfo(){ 
       try {
            tmcdb = TMCDBComponentHelper.narrow(container.getDefaultComponent("IDL:alma/TMCDB/TMCDBComponent:1.0"));
        } catch (Exception e) {
            //TODO do more here
            e.printStackTrace();
        }
    }
    
    private void getControlRef() {
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                    container.getComponent("CONTROL/MASTER"));
            logger.info("SCHEDULING_PANEL: Got control system in array creator");
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting components from array creator");
        }
    }
    
    private void releaseControlRef() {
        try {
            container.releaseComponent(control.name());
        } catch(Exception e) {
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error releasing components from array creator");
        }
    }

    public String[] getAntennas() {
        String[] antennas=null;
        try{
            getControlRef();
            antennas= control.getAvailableAntennas();
            releaseControlRef();
        } catch(Exception e){
            logger.severe("SCHEDULING_PANEL: Control not accessible yet. Try again when its opeational");
            e.printStackTrace();
        }
        return antennas;
    }

    //Row one for 12 meter
    //Row two for 7 meter
    //row three for TP
    private void createOfflineChessboards(){
        if(tmcdb == null){
            logger.severe("SP: CANNOT CONNECT TO TMCDB");
            return;
        }
        StartupAntennaIDL[] startupInfo ;
        try {
            startupInfo = tmcdb.getStartupAntennasInfo(); 
        
            String[] antennaNames = new String[startupInfo.length];
            for(int i=0; i < startupInfo.length; i++){
                antennaNames[i] = startupInfo[i].antennaName;
                System.out.println("Antenna name from CTRL: "+ antennaNames[i]);
            }
            map12 = alma.common.gui.chessboard.MapToNumber.createMapping(antennaNames,"twelveMeter");
            Map map7 = alma.common.gui.chessboard.MapToNumber.createEmptyMap(12);
            Map mapTP = alma.common.gui.chessboard.MapToNumber.createEmptyMap(4);
            String[] twelve = new String[map12.size()];
            twelve = (String[])map12.values().toArray(twelve);
            for(int i=0; i < 50; i++){
                entries12[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, twelve[i]); 
            }
            String[] seven = new String[map7.size()];
            seven = (String[])map7.values().toArray(seven);
            for(int i=0; i < 12; i++){
                entries7[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, seven[i]); 
            }
            String[] tp = new String[mapTP.size()];
            tp = (String[])mapTP.values().toArray(tp);
            for(int i=0; i < 4; i++){
                entriesTP[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, tp[i]); 
            }
            allEntries = new ChessboardEntry[3][];
            allEntries[0] = entries12;
            allEntries[1] = entries7;
            allEntries[2] = entriesTP;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
      * For displaying only, can't do anything with these 
      */
    public ChessboardEntry[][] getGenericAntennaMapping() {
        Map generic1 = alma.common.gui.chessboard.MapToNumber.createEmptyMap(50);
        Map generic2 = alma.common.gui.chessboard.MapToNumber.createEmptyMap(12);
        Map generic3 = alma.common.gui.chessboard.MapToNumber.createEmptyMap(4);
        String[] foo1 = new String[generic1.size()];
        foo1 = (String[])generic1.values().toArray(foo1); 
        for(int i=0; i < foo1.length; i++){
            //System.out.println((String)foo1[i]);
            entries12[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, (String)foo1[i]);
        }
        String[] foo2 = new String[generic2.size()];
        foo2 = (String[])generic2.values().toArray(foo2); 
        for(int i=0; i < foo2.length; i++){
            entries7[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, (String)foo2[i]);
        }
        String[] foo3 = new String[generic3.size()];
        foo3 = (String[])generic3.values().toArray(foo3); 
        for(int i=0; i < foo3.length; i++){
            entriesTP[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, (String)foo3[i]);
        }
        allEntries = new ChessboardEntry[3][];
        allEntries[0] = entries12;
        allEntries[1] = entries7;
        allEntries[2] = entriesTP;
        return allEntries;
    }
    public ChessboardEntry[][] getAntennasForOfflineChessboards(){
        //createOfflineChessboard();
        return allEntries;
    }

    public void resetChessboardToOffline() {
        createOfflineChessboards();
    }
    
    public String[] getAntennasForActiveChessboards() {
        String[] antennas=null;
        String[] cbeNames=null;
        try {
            getControlRef();
            antennas= control.getAvailableAntennas();
            System.out.println("Got antennas from CONTROL:");
            //System.out.println(antennas[0]);
            //System.out.println(antennas[1]);
            releaseControlRef();
            String tmpName;
            //for now create an array of 'offline' entries
            //get online ones and update the array.
            int loc=0;
            cbeNames = new String[antennas.length];
            for(int i=0; i < antennas.length; i++){
                //Eventaully will have to go get details of antenna from TMCDB
                cbeNames[i] = (String)map12.get(antennas[i]);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return cbeNames;
    }
    
    public String createArray(String arrayMode, ChessboardEntry[] cbEntries) 
        throws SchedulingException 
    {
        String[] antennas = new String[cbEntries.length];
        String[] keys = new String[map12.size()];
        keys = (String[])((LinkedHashMap)map12).keySet().toArray(keys);
        String[] values = new String[map12.size()];
        values = (String[])map12.values().toArray(values);
        for(int i=0; i < cbEntries.length; i++){
        //    antennas[i] = (cbEntries[i].getDisplayName());
            
            //get index of cbEntries display name in values and then get the key
            //of that index coz its the real antenna name
            for(int x=0; x < values.length; x++){
                if(values[x].equals(cbEntries[i].getDisplayName()) ){
                    antennas[i] = keys[x];
                    break;
                }
            }
            System.out.println("AntennaName = "+antennas[i]);
        }
        System.out.println("Antennas to create array with = "+antennas.length);
        String arrayName = null;
        getMSRef();
        try {
            if(arrayMode.toLowerCase().equals("dynamic")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.DYNAMIC);
            } else if(arrayMode.toLowerCase().equals("interactive")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.INTERACTIVE);
            } else if(arrayMode.toLowerCase().equals("queued")) {
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.QUEUED);
            } else if(arrayMode.toLowerCase().equals("manual")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.MANUAL);
            }
        } catch(Exception e) {
            releaseMSRef();
            e.printStackTrace();
            throw new SchedulingException (e);
        }
        releaseMSRef();
        return arrayName;

    }

}
