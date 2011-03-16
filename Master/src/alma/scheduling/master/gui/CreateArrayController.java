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
package alma.scheduling.master.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import alma.Control.ControlMaster;
import alma.Control.CorrelatorType;
import alma.Control.InaccessibleException;
import alma.TMCDB.Access;
import alma.TMCDB.AccessHelper;
import alma.TMCDB_IDL.StartupAntennaIDL;
import alma.common.gui.chessboard.ChessboardEntry;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.SchedulingException;

public class CreateArrayController extends SchedulingPanelController {

    private ControlMaster control;
    private Access tmcdb = null;
    private ChessboardEntry[][] allEntries = null;   
    private ChessboardEntry[] entries12 = new ChessboardEntry[50];
    private ChessboardEntry[] entries7 = new ChessboardEntry[12];
    private ChessboardEntry[] entriesTP = new ChessboardEntry[4];
    private Map map12;
    private Map map7;
    private Map mapTP;
    private final static int NUMBER_OF_ACS_ANTENNAS = 50;
    private final static int NUMBER_OF_ACA_ANTENNAS = 12;
    private final static int NUMBER_OF_TP_ANTENNAS = 4;

    public CreateArrayController(){//PluginContainerServices cs){
        super();
    }
    public void secondSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        if(cs == null) {
            //logger = Logger.getLogger("OFFLINE SP");
            //logger.warning("SchedulingPanel: problem getting CS");
        }
    }

    protected void updateChessboardWithALMANames(){
        getTMCDBComponent();
        createOfflineChessboards();
    }

    public boolean getTMCDBComponent(){
    	boolean isTMCDBReady = false;
       try {
           if(tmcdb == null){
        	   tmcdb = AccessHelper.narrow(container.getDefaultComponent("IDL:alma/TMCDB/Access:1.0"));
        	   String tmcdbName = tmcdb.name();
        	   container.releaseComponent(tmcdbName);
               tmcdb = AccessHelper.narrow(container.getComponentNonSticky(tmcdbName));
           }
           if(tmcdb !=null) {
        	   isTMCDBReady = true;
           }
           
        } catch (Exception e) {
            //TODO do more here
            e.printStackTrace();
            tmcdb = null;
        }
        return isTMCDBReady;
    }
    
    public boolean getControlRef() {
    	boolean isControlMasterReady =false;
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                    container.getComponentNonSticky(alma.scheduling.master.compimpl.Constants.CONTROL_MASTER_URL));
            logger.fine("SCHEDULING_PANEL: Got control system in array creator");
            isControlMasterReady = true;
            return isControlMasterReady;
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting components from array creator");
        }
        
        return isControlMasterReady;
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
            if(logger == null) {
                return;
            }
        }
        StartupAntennaIDL[] startupInfo ;
        try {
            startupInfo = tmcdb.getStartupAntennasInfo(); 
        
            String[] antennaNames = new String[NUMBER_OF_ACS_ANTENNAS];
            String[] ACAAntennaNames = new String[NUMBER_OF_ACA_ANTENNAS];
            String[] TPAntennaNames = new String[NUMBER_OF_TP_ANTENNAS];
            
            for(int i=0;i<NUMBER_OF_ACS_ANTENNAS;i++) {
            	antennaNames[i]= String.valueOf(i) ;
            }
            
            for(int i=0;i<NUMBER_OF_ACA_ANTENNAS;i++) {
            	ACAAntennaNames[i]= String.valueOf(i) ;
            }
            
            for(int i=0;i<NUMBER_OF_TP_ANTENNAS;i++) {
            	TPAntennaNames[i]= String.valueOf(i) ;
            }
            
            
            for(int i=0;i<startupInfo.length;i++){
            	if(startupInfo[i].uiDisplayOrder > 0 && startupInfo[i].uiDisplayOrder <= 50) {
                    antennaNames[startupInfo[i].uiDisplayOrder-1] = startupInfo[i].antennaName;
                    logger.fine("ACS Antenna name from CTRL: "+ startupInfo[i].antennaName);
            	} else if(startupInfo[i].uiDisplayOrder >= 51 && startupInfo[i].uiDisplayOrder <= 62) {
                    ACAAntennaNames[startupInfo[i].uiDisplayOrder-1-50]=startupInfo[i].antennaName;
            	} else if(startupInfo[i].uiDisplayOrder >= 63 && startupInfo[i].uiDisplayOrder <= 66) {
                    TPAntennaNames[startupInfo[i].uiDisplayOrder-62-1]= startupInfo[i].antennaName;
                    logger.fine("TP Antenna name from CTRL: "+ startupInfo[i].antennaName);
            	}
            }
                    
            map12 = alma.common.gui.chessboard.internals.MapToNumber.createMapping(antennaNames,"twelveMeter");
            map7 = alma.common.gui.chessboard.internals.MapToNumber.createMapping(ACAAntennaNames,"sevenMeter");
            mapTP = alma.common.gui.chessboard.internals.MapToNumber.createMapping(TPAntennaNames,"totalPower");
            String[] twelve = new String[map12.size()];
            //int[] twelveLocation = (String[])map12.values().toArray(twelve);
            twelve = (String[])map12.keySet().toArray(twelve);
            //String[] twelve_real = new String[map12.size()];
            //twelve_real = (String[])map12.keySet().toArray(twelve_real);
            for(int i=0; i < 50; i++){
                entries12[i] = null;
                entries12[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, antennaNames[i], antennaNames[i]); 
            }
            
            String[] seven = new String[map7.size()];
            seven = (String[])map7.values().toArray(seven);
            
            for(int i=0; i < 12; i++){
                entries7[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, ACAAntennaNames[i], ACAAntennaNames[i]); 
            }
            String[] tp = new String[mapTP.size()];
            tp = (String[])mapTP.values().toArray(tp);
            
            //TODO need to redesign after more antenna add into ALMA....
            
            for(int i=0; i < 4; i++){
            		entriesTP[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, TPAntennaNames[i],TPAntennaNames[i]);
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
        Map generic1 = alma.common.gui.chessboard.internals.MapToNumber.createEmptyMap(50);
        Map generic2 = alma.common.gui.chessboard.internals.MapToNumber.createEmptyMap(12);
        Map generic3 = alma.common.gui.chessboard.internals.MapToNumber.createEmptyMap(4);
        String[] foo1 = new String[generic1.size()];
        foo1 = (String[])generic1.values().toArray(foo1); 
        for(int i=0; i < foo1.length; i++){
            entries12[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE, (String)foo1[i],"not connected to TMCDB yet");
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
        createOfflineChessboards();
        return allEntries;
    }
    
    public String[] getAvailableCLOPhotonics() {
    	String[] availablePhotonics=null;
    	try {
    		availablePhotonics = control.getAvailablePhotonicReferences();
    	}
    	catch (InaccessibleException e) {
    		
    	}
    	return availablePhotonics;
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
            logger.fine("Got antennas from CONTROL:");
            //releaseControlRef();
            String tmpName;
            //for now create an array of 'offline' entries
            //get online ones and update the array.
            int loc=0;
            cbeNames = new String[antennas.length];
            for(int i=0; i < antennas.length; i++){
                //Eventaully will have to go get details of antenna from TMCDB
            	logger.fine("antennas name"+i+antennas[i]);
                cbeNames[i] = (String)map12.get(antennas[i]);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        //return cbeNames;
        return antennas;
    }
    
    public String createArray(ArrayModeEnum arrayMode,
    						  ChessboardEntry[] cbEntries,
    						  String[] photonicsChoice,
    						  CorrelatorType correlator,
    						  String schedulingPolicy) 
        throws SchedulingException 
    {
        String[] antennas = new String[cbEntries.length];
        
        String[] keys = new String[map12.size()];
        keys = (String[])((LinkedHashMap)map12).keySet().toArray(keys);
        String[] values = new String[map12.size()];
        //values = (String[])map12.values().toArray(values);
        values = (String[])map12.keySet().toArray(values);
        
        //map total power antennas
        String[] TPkeys = new String[mapTP.size()];
        TPkeys = (String[])((LinkedHashMap)mapTP).keySet().toArray(TPkeys);
        String[] TPvalues = new String[mapTP.size()];
        TPvalues = (String[])mapTP.keySet().toArray(TPvalues);
        //logger.info("Number of Antennas be selected:"+cbEntries.length);
        for(int i=0; i < cbEntries.length; i++){
            //antennas[i] = (cbEntries[i].getDisplayName());
            //get index of cbEntries display name in values and then get the key
            //of that index coz its the real antenna name
        	//logger.info("index "+i +" : "+cbEntries[i].getDisplayName());
            for(int x=0; x < values.length; x++){
            	//logger.info("value :"+values[x]);
                if(values[x].equals(cbEntries[i].getDisplayName()) ){
                    antennas[i] = keys[x];
                    logger.fine("AntennaName = "+antennas[i]);
                    break;
                }
            }
            
           
            for(int x=0; x < TPvalues.length; x++){
                if(TPvalues[x].equals(cbEntries[i].getDisplayName()) ){
                    antennas[i] = TPkeys[x];
                    logger.fine("AntennaName = "+antennas[i]);
                    break;
                }
            }
            
        }

     
//        logArray(logger, "Antennas to create array with   ", antennas);
//        logArray(logger, "Photonics to create array with  ", photonicsChoice);
//        logger.fine("Correlator to create array with = "+ correlator);
//        logger.fine("Policy to create array with = " + schedulingPolicy);
        ArrayCreationInfo arrayInfo = null;
        getMSRef();
        try {
            arrayInfo = masterScheduler.createArray(
                        	antennas,
                        	photonicsChoice,
                        	correlator,
                        	arrayMode,
                        	ArraySchedulerLifecycleType.NORMAL,
                        	schedulingPolicy);
        } catch(Exception e) {
            //releaseMSRef();
            e.printStackTrace();
            throw new SchedulingException (e);
        }
        //releaseMSRef();
        return arrayInfo.arrayId;

    }
	private void logArray(Logger logger, String label, String[] choices) {
		final StringBuilder sb = new StringBuilder();

		sb.append(label);
		sb.append(String.format("[%2d] = [", choices.length));
		
		String sep = "";
		for (String choice : choices) {
			sb.append(sep);
			sb.append(choice);
			sep = ", ";
		}
		sb.append("]");
		
		logger.fine(sb.toString());
	}

}