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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import alma.Control.ControlMaster;
import alma.Control.CorrelatorType;
import alma.Control.InaccessibleException;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.SchedulingMasterExceptions.ACSInternalExceptionEx;
import alma.SchedulingMasterExceptions.ControlInternalExceptionEx;
import alma.SchedulingMasterExceptions.SchedulingInternalExceptionEx;
import alma.TMCDB.Access;
import alma.TMCDB.AccessHelper;
import alma.TMCDB_IDL.StartupAntennaIDL;
import alma.common.gui.chessboard.ChessboardEntry;
import alma.common.gui.chessboard.internals.MapToNumber;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.Array;
import alma.scheduling.ArrayCreationInfo;
import alma.scheduling.ArrayDescriptor;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.ArraySchedulerLifecycleType;
import alma.scheduling.SchedulingException;
import alma.scheduling.formatting.Format;
import alma.scheduling.utils.Constants;
import alma.scheduling.utils.ErrorHandling;

public class CreateArrayController extends SchedulingPanelController
			implements ArrayStatusListener {

    private ControlMaster control;
    private Access tmcdb = null;
    private ChessboardEntry[][] allEntries = null;   
    private ChessboardEntry[] entries12 = new ChessboardEntry[50];
    private ChessboardEntry[] entries7 = new ChessboardEntry[12];
    private ChessboardEntry[] entriesTP = new ChessboardEntry[4];
    private Map<String,String> map12;
    private Map<String,String> map7;
    private Map<String,String> mapTP;
    private final static int NUMBER_OF_ACS_ANTENNAS = 50;
    private final static int NUMBER_OF_ACA_ANTENNAS = 12;
    private final static int NUMBER_OF_TP_ANTENNAS = 4;
    private ArrayStatusCallbackImpl callback;

    public CreateArrayController(){//PluginContainerServices cs){
        super();
        initialiseArrayMaps();
    }
    
    public void secondSetup(PluginContainerServices cs){
        super.onlineSetup(cs);
        if (cs != null) {
        	callback = new ArrayStatusCallbackImpl(this);
    		try {
    			cs.activateOffShoot(callback);
    			getMSRef();
    			masterScheduler.addMonitorMaster(
    					InetAddress.getLocalHost().getHostName() + "_"
    							+ this.toString() + "_"
    							+ System.currentTimeMillis(), callback._this());
    		} catch (AcsJContainerServicesEx e) {
    			e.printStackTrace();
    		} catch (UnknownHostException e) {
    			cs.getLogger().info(e.getMessage());
    			masterScheduler.addMonitorMaster(
    					this.toString() + "_" + System.currentTimeMillis(),
    					callback._this());
    		} finally {
    		}
    		getArrayInformation();
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
        	   container.releaseComponent(tmcdbName, null);
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
                    container.getComponentNonSticky(Constants.CONTROL_MASTER_URL));
            logger.fine("SCHEDULING_PANEL: Got control system in array creator");
            isControlMasterReady = true;
            return isControlMasterReady;
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting components from array creator");
        }
        
        return isControlMasterReady;
    }
    
    @SuppressWarnings("unused")
	private void releaseControlRef() {
        try {
            container.releaseComponent(control.name(), null);
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
            
            final int Max_ACS_Display = NUMBER_OF_ACS_ANTENNAS;
            final int Max_ACA_Display = Max_ACS_Display + NUMBER_OF_ACA_ANTENNAS;
            final int Max_TP_Display  = Max_ACA_Display + NUMBER_OF_TP_ANTENNAS;

            for (final StartupAntennaIDL startup : startupInfo) {
            	if (startup.uiDisplayOrder > 0 && startup.uiDisplayOrder <= Max_ACS_Display) {
                    antennaNames[startup.uiDisplayOrder-1] = startup.antennaName;
            	} else if (startup.uiDisplayOrder <= Max_ACA_Display) {
                    ACAAntennaNames[startup.uiDisplayOrder-Max_ACS_Display-1] = startup.antennaName;
            	} else if (startup.uiDisplayOrder <= Max_TP_Display) {
                    TPAntennaNames[startup.uiDisplayOrder-Max_ACA_Display-1] = startup.antennaName;
            	} else {
            		logger.warning("Unexpected display slot (" + startup.uiDisplayOrder +
            				") for antenna " + startup.antennaName +
            				" obtained from TMCDB");
            	}
            }
            
            logTMCDBAntennas("12m", antennaNames);
            logTMCDBAntennas("7m", ACAAntennaNames);
            logTMCDBAntennas("TP", TPAntennaNames);
                    
            map12 = MapToNumber.createMapping(antennaNames, "twelveMeter");
            map7  = MapToNumber.createMapping(ACAAntennaNames, "sevenMeter");
            mapTP = MapToNumber.createMapping(TPAntennaNames, "totalPower");
            
            diagnoseAntennas("createofflineChessboards");
            String hover;
            String name;
            
            String[] twelve =  map12.keySet().toArray(new String[0]);
            for(int i=0; i < twelve.length; i++){
            	name = antennaNames[i];
                hover = toolTipForAntenna(name);
                entries12[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE,
                		name, hover); 
            }
            
            String[] seven = map7.values().toArray(new String[0]);
            for(int i=0; i < seven.length; i++){
            	name = ACAAntennaNames[i];
                hover = toolTipForAntenna(name);
                entries7[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE,
                		name, hover); 
            }
            
            String[] tp = mapTP.values().toArray(new String[0]);
            for(int i=0; i < tp.length; i++){
            	name = TPAntennaNames[i];
                hover = toolTipForAntenna(name);
            	entriesTP[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE,
            			name, hover);
            }

            allEntries = new ChessboardEntry[3][];
            allEntries[0] = entries12;
            allEntries[1] = entries7;
            allEntries[2] = entriesTP;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void logTMCDBAntennas(String label, String[] antennaNames) {
    	// The TMCDB gives us missing antennas for some reason, so
    	// filter them out. "Real" antennas have 4 character names.
    	List<String> filtered = new Vector<String>();
    	for (final String name : antennaNames) {
    		if (name.length() >= 4) {
    			filtered.add(name);
    		}
    	}
    	logger.info(label + " antennas from the TMCDB: " +
    			Format.format(filtered, "", ", ", ""));
	}

    protected void logControlAntennas(String label, String[] antennaNames) {
    	logger.info(label + " antennas from Control: " +
    			Format.formatArray(antennaNames, "", ", ", ""));
	}
    
	/**
      * For displaying only, can't do anything with these 
      */
    public ChessboardEntry[][] getGenericAntennaMapping() {
        Map<String, String> generic1 = MapToNumber.createEmptyMap(NUMBER_OF_ACS_ANTENNAS);
        Map<String, String> generic2 = MapToNumber.createEmptyMap(NUMBER_OF_ACA_ANTENNAS);
        Map<String, String> generic3 = MapToNumber.createEmptyMap(NUMBER_OF_TP_ANTENNAS);
        
        String[] foo1 = generic1.values().toArray(new String[0]); 
        for(int i=0; i < foo1.length; i++){
            entries12[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE,
            		foo1[i], "not connected to TMCDB yet");
        }
        String[] foo2 = generic2.values().toArray(new String[0]); 
        for(int i=0; i < foo2.length; i++){
            entries7[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE,
            		foo2[i], "not connected to TMCDB yet");
        }
        String[] foo3 = generic3.values().toArray(new String[0]); 
        for(int i=0; i < foo3.length; i++){
            entriesTP[i] = new ChessboardEntry(SPAntennaStatus.OFFLINE,
            		foo3[i], "not connected to TMCDB yet");
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

        try {
            getControlRef();
            antennas= control.getAvailableAntennas();
            logControlAntennas("Got the following", antennas);
        } catch (Exception e){
            e.printStackTrace();
        }
        //return cbeNames;
        return antennas;
    }
    
    public String createArray(ArrayModeEnum arrayMode,
    						  Collection<ChessboardEntry> cbEntries,
    						  String[] photonicsChoice,
    						  CorrelatorType correlator,
    						  String schedulingPolicy) 
        throws SchedulingException, ControlInternalExceptionEx, SchedulingInternalExceptionEx, ACSInternalExceptionEx 
    {
    	final List<String> antennas = new Vector<String>();
    	boolean ok = true; // until proven otherwise
    	
    	for (final ChessboardEntry cb : cbEntries) {
    		final String name = cb.getDisplayName();
    		if (map12.containsKey(name)) {
                logger.fine("Using 12m antenna " + name);
                antennas.add(name);
    		} else if (map7.containsKey(name)) {
                logger.fine("Using 7m antenna " + name);
                antennas.add(name);
    		} else if (mapTP.containsKey(name)) {
                logger.fine("Using TP antenna " + name);
                antennas.add(name);
    		} else {
    			logger.warning("Antenna name '" + name + "' is unknown");
    			ok = false;
    		}
    	}
    	
    	if (!ok) {
            throw new SchedulingException("Bizarrely, one or more unknown antennas have been specified");
    	}
    	
    	logger.fine("Found " + antennas.size() + " antenna(s)");
        String[] antennaArray = antennas.toArray(new String[0]);
           
        logger.fine("Antennas for new array:   " + Format.formatArray(antennaArray));
        logger.fine("Photonics for new array:  " + Format.formatArray(photonicsChoice));
        logger.fine("Correlator for new array: " + correlator);
        logger.fine("Policy for new array:     " + schedulingPolicy);
        
        ArrayCreationInfo arrayInfo = null;
        getMSRef();
        final ArrayDescriptor details = new ArrayDescriptor(
        			antennaArray,
        			photonicsChoice,
        			correlator,
        			arrayMode,
        			ArraySchedulerLifecycleType.NORMAL,
        			schedulingPolicy);
            arrayInfo = masterScheduler.createArray(details);
        return arrayInfo.arrayId;

    }

    
    
    /*
     * ================================================================
     * Keeping track of antennas <-> array mappings
     * ================================================================
     */
    private Map<String, SortedSet<String>> array2Antennas;
    private Map<String, String> antenna2Array;

    private void diagnoseAntennas(String label) {
    	final StringBuilder b = new StringBuilder();
    	
    	try {
    		b.append("Array & Antenna correspondence - ");
    		b.append(label);
    		b.append('\n');
    		for (final String array : array2Antennas.keySet()) {
    			b.append("\t'");
    			b.append(array);
    			b.append("' -> ");
    			b.append(Format.format(array2Antennas.get(array), "{", ", ", "}"));
    			b.append('\n');
    		}
    		for (final String antenna : antenna2Array.keySet()) {
    			b.append("\t'");
    			b.append(antenna);
    			b.append("' in ");
    			b.append(antenna2Array.get(antenna));
    			b.append('\n');
    		}
    	} catch (Exception e) {
			String message = e.getMessage();
			if (message == null) {
				message = e.getClass().getSimpleName();
			}
			ErrorHandling.warning(logger, String.format(
					"Error diagnosing antennas and arrays - %s",
					message), e);
    	} finally {
    		logger.info(b.toString());
    	}
    }
    
    private void initialiseArrayMaps() {
    	array2Antennas = new TreeMap<String, SortedSet<String>>();
    	antenna2Array  = new TreeMap<String, String>();
    }

    private void getArrayInformation(String arrayName) {
    	final Array array = getArray(arrayName);
    	final ArrayDescriptor descriptor = array.getDescriptor();
    	
    	final SortedSet<String> antennas = new TreeSet<String>();
    	for (final String antenna : descriptor.antennaIdList) {
    		antennas.add(antenna);
    		antenna2Array.put(antenna, arrayName);
    	}
    	
    	array2Antennas.put(arrayName, antennas);
    }

    private void getArrayInformation(String[] arrayNames) {
    	for (final String arrayName : arrayNames) {
    		getArrayInformation(arrayName);
    	}
    }
    
    private void getArrayInformation() {
    	getMSRef();
    	if (masterScheduler != null) {
    		getArrayInformation(masterScheduler.getActiveAutomaticArrays());
    		getArrayInformation(masterScheduler.getActiveManualArrays());
    	}
		diagnoseAntennas("initial information");
    }
    
	@Override
	public void notifyArrayCreation(String name, ArrayModeEnum arrayMode) {
		getArrayInformation(name);
		diagnoseAntennas("after array creation");
	}

	@Override
	public void notifyArrayDestruction(String name) {
		try {
			Set<String> antennas = array2Antennas.get(name);
			for (final String antenna : antennas) {
				antenna2Array.remove(antenna);
			}
			array2Antennas.remove(name);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null) {
				message = e.getClass().getSimpleName();
			}
			ErrorHandling.warning(logger, String.format(
					"Error keeping track of destruction of %s - %s",
					name, message), e);
		}
		diagnoseAntennas("after array destruction");
	}
	
	protected String toolTipForAntenna(String antennaName) {
		String result;
        if (antenna2Array.containsKey(antennaName)) {
        	result = antennaName + " in " + antenna2Array.get(antennaName);
        } else {
        	result = antennaName;
        }
        return result;
	}
    /* End Keeping track of antennas <-> array mappings
     * ============================================================= */

}
