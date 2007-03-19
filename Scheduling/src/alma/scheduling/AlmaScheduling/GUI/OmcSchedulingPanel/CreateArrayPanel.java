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
 * File CreateArrayPanel.java
 *
 */
package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.common.gui.chessboard.*;

public class CreateArrayPanel extends SchedulingPanelGeneralPanel {


    private String[] availableAntennas;
    private Vector<String> allArrays;
    private int columnIndex = 0;
    private JButton createArrayB;
    private JButton cancelB;
    private String arrayMode;
    private CreateArrayController controller;
    private JTabbedPane parent;
    private ChessboardPanel twelveMeterChessboard;
    private ChessboardPanel sevenMeterChessboard;
    private ChessboardPanel tpChessboard;
    private JPanel chessboardPanel;

    public CreateArrayPanel() {
        super();
        super.setBorder(new TitledBorder("Create Array"));
        allArrays = new Vector<String>();
        setSize(400,300);
        controller= new CreateArrayController();
        createGenericAntennaChessboards();
        add(chessboardPanel, BorderLayout.CENTER);
    }

    public void setOwner(JTabbedPane p){
        parent = p;
    }

    public void connectedSetup(PluginContainerServices cs) {
        super.onlineSetup(cs);
        controller.secondSetup(cs);
        createALMAAntennaChessboards();
        add(chessboardPanel, BorderLayout.CENTER);
        setEnabled(false);
    }

    protected void initializeChessboards() {
        controller.updateChessboardWithALMANames();
    }

    public void setEnabled(boolean enabled){
        createArrayB.setEnabled(enabled);
        cancelB.setEnabled(enabled);
        disableChessboards(enabled);
        repaint();
        validate();
    }
    
    private void createGenericAntennaChessboards(){
        chessboardPanel = new JPanel(new BorderLayout());
        JPanel cbPanel = new JPanel(new GridLayout(3,1));
        cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.Y_AXIS));
        ChessboardEntry[][] all = controller.getGenericAntennaMapping();
        //all[0] is antennas for TwelveMeterChessboard
        cbPanel.add(createTwelveMeterChessboard(all[0]));
        //all[1] is antennas for SevenMeterChessboard
        cbPanel.add(createSevenMeterChessboard(all[1]));
        //all[2] is antennas for TP Chessboard
        cbPanel.add(createTPChessboard(all[2]));
        chessboardPanel.add(cbPanel, BorderLayout.CENTER);
        chessboardPanel.add(createSouthPanel(),BorderLayout.SOUTH);
    }

    private void createALMAAntennaChessboards(){
        chessboardPanel.removeAll();
        chessboardPanel = new JPanel(new BorderLayout());
        JPanel cbPanel = new JPanel(new GridLayout(3,1));
        cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.Y_AXIS));
        ChessboardEntry[][] all = controller.getAntennasForOfflineChessboards();
        //all[0] is antennas for TwelveMeterChessboard
        cbPanel.add(createTwelveMeterChessboard(all[0]));
        //all[1] is antennas for SevenMeterChessboard
        cbPanel.add(createSevenMeterChessboard(all[1]));
        //all[2] is antennas for TP Chessboard
        cbPanel.add(createTPChessboard(all[2]));
        chessboardPanel.add(cbPanel, BorderLayout.CENTER);
        chessboardPanel.add(createSouthPanel(),BorderLayout.SOUTH);
    }

    private JPanel createTwelveMeterChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[5][10];
        int ctr=0;
        for(int i=0; i < 5;i++){
            for(int j=0; j < 10; j++){
                entries[i][j] = foo[ctr++];
            }
        }
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("Twelve Meter Antennas"));
        twelveMeterChessboard = new ChessboardPanel(entries, true, null, null);
        twelveMeterChessboard.setDetailsDisplayEnabled(false);
        p.add(twelveMeterChessboard);
        return p;
    }
    
    private JPanel createSevenMeterChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[2][6];
        int ctr=0;
        for(int i=0; i < 2;i++){
            for(int j=0; j < 6; j++){
                entries[i][j] = foo[ctr++];
            }
        }
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("Seven Meter Antennas"));
        sevenMeterChessboard = new ChessboardPanel(entries, true, null, null);
        sevenMeterChessboard.setDetailsDisplayEnabled(false);
        p.add(sevenMeterChessboard);
        return p;
    }

    private JPanel createTPChessboard(ChessboardEntry[] foo) {
        ChessboardEntry[][] entries = new ChessboardEntry[1][4];
        int ctr=0;
         for(int j=0; j < 4; j++){
            entries[0][j] = foo[ctr++];
        }
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("Total Power Antennas"));
        tpChessboard = new ChessboardPanel(entries, true, null, null);
        tpChessboard.setDetailsDisplayEnabled(false);
        p.add(tpChessboard);
        return p;
    }
    
    private JPanel createSouthPanel() {
        JPanel p = new JPanel();
        p.add(actionButtons(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel actionButtons(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createArrayB = new JButton("Create");
        createArrayB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                CreateArrayThread at = new CreateArrayThread();
                Thread t = controller.getCS().getThreadFactory().newThread(at);
                t.start();
            }
        });
        cancelB = new JButton("Cancel");
        cancelB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                exit();
                setEnabled(false);
                ((MainSchedTabPane)parent).resetMainViewButtons();
            }
        });
        p.add(createArrayB);
        p.add(cancelB);
        return p;
    }

    private boolean isIn(int is, int[] in){
        for(int i=0; i < in.length; i++){
            if(in[i] == is) {
                return true;
            }
        }
        return false;
    }

    private void updateChessboard(String[] antNames) {
    //show the available antennas as online
        //for each available antenna
        //call chessboard.processStatusChange(..) with new ChessboardStatusEvent
        //will need cb entry's name
        //String cbeName;
        ChessboardStatusEvent event=null;
        for (int i=0; i < antNames.length; i++){
            event = new ChessboardStatusEvent(antNames[i], SPAntennaStatus.ONLINE, "");
            twelveMeterChessboard.processStatusChange(event);
        }
    }

    private void disableChessboards(boolean enabled) {
        ChessboardStatusEvent event=null;
        twelveMeterChessboard.clearSelection();
        sevenMeterChessboard.clearSelection();
        tpChessboard.clearSelection();
        twelveMeterChessboard.setEnabled(enabled);
        sevenMeterChessboard.setEnabled(enabled);
        tpChessboard.setEnabled(enabled);
        if( !enabled) {
            ChessboardEntry[][] all = controller.getAntennasForOfflineChessboards();
            for(int i=0; i < all[0].length; i++){
                event = new ChessboardStatusEvent(all[0][i].getDisplayName(), SPAntennaStatus.OFFLINE, "");
                twelveMeterChessboard.processStatusChange(event);
            }
        }
    }
    

    public void prepareCreateArray(String mode){
        arrayMode = mode;
        GetAntennaThread ant = new GetAntennaThread();
        if(controller == null){
            System.out.println("crappy controller");
        }
        if(controller.getCS() == null){
            System.out.println("crappy CS");
        }
        Thread t = controller.getCS().getThreadFactory().newThread(ant);
        t.start();
    }
       

    private boolean createArray() {
        //get selected antenns from chessboard
        ChessboardEntry[] selected = twelveMeterChessboard.getSelectedEntries();
        //check the ones selected have online status
        //temporary: change so that can't select ones not online
        for(int i=0; i <selected.length; i++){
            if(selected[i].getCurrentStatus() != SPAntennaStatus.ONLINE){
                JOptionPane.showMessageDialog(this, 
                        "Selected Antenna not available",
                        "Antenna "+selected[i].getDisplayName()+" is not available "+
                        "to be included in an array", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        String arrayName;
        try {
            arrayName = controller.createArray(arrayMode, selected);
            allArrays.add(arrayName);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, e.toString()+
                    "\nMake sure these antennas are really available to "+
                    "create this array. Also check state of Control System.", 
                    "Error creating array", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        //tell parent component to open new scheduler tab.
        openNewSchedulerTab(arrayMode, arrayName);
        return true;
    }

    /**
      * @param am ArrayMode
      * @param an Array Name
      */
    public void openNewSchedulerTab(String am, String an) {
        OpenSchedulerTab newTab = new OpenSchedulerTab(am, an);
        Thread t = new Thread(newTab);
        t.start();
    }


    public void exit() {
        //clearAntennaTables();
    }
    class OpenSchedulerTab implements Runnable {
        private String mode;
        private String array;
        public OpenSchedulerTab(String m, String arrayName) {
            mode =m;
            array = arrayName;
        }
        public void run() {
            ((MainSchedTabPane)parent).openSchedulerTab(mode, array);
            //unselect the button now
        }
    }
    class GetAntennaThread implements Runnable {
        public GetAntennaThread(){
        }
        public void run(){ 
            String[] onlineAntennasForChessboard = controller.getAntennasForActiveChessboards();
            updateChessboard(onlineAntennasForChessboard);
        }
    }

    class CreateArrayThread implements Runnable{
        public CreateArrayThread(){
        }
        public void run(){
            if(createArray()) {
                exit();
                setEnabled(false);
                ((MainSchedTabPane)parent).resetMainViewButtons();
            }
        }
    }
}

