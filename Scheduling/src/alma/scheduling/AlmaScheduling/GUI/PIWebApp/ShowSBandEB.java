package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;

import java.awt.event.*;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.*;

import java.awt.Graphics;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.io.*;
import java.lang.*;
//alma

import alma.entity.xmlbinding.projectstatus.*;
import alma.entity.xmlbinding.projectstatus.types.*;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.obsproject.types.*;
import alma.entity.xmlbinding.obsproposal.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.schedblock.types.*;
import alma.entity.xmlbinding.projectstatus.ObsUnitSetStatusTChoice;
import alma.scheduling.AlmaScheduling.ProjectUtil;
import alma.scheduling.Define.*;

public class ShowSBandEB extends JApplet implements ActionListener{

	URL filename,filename1,filename2;
	JTable SBControl;
	String[] SBheader={"SBName","Source","Mode","Status","SBDuration","numEB","numScan","numRepeat",
						"frequence","frequenceBand","line"};
	Object[][] SBvalue ={
			{"SB1","Ori_1686","Expert","Running","60480000","2","3","3","240","2","1"},
			{"","","","","","","","","","",""},
			{"","","","","","","","","","",""},
	};
	private final JTabbedPane pane = new JTabbedPane();
	private final JTabbedPane EBpane = new JTabbedPane();
	//JTextArea Info;
	JPanel mainpanel1,mainpanel2,mainpanel3;
	JPanel DPSPanel,PQLPanel,ESBPanel,PlSPanel;
	JPanel[] panelgroup = {DPSPanel,PlSPanel,ESBPanel,PQLPanel};
	String[] panelname = {"Science Goal","Weather Constraint","Execute SchedBlock","QuickLook Picture"};
	JLabel picture ;
	
	
	public void start()  {
		//System.out.print("start to run mothod in Applet");
		try {
			DateTime datetime = DateTime.currentSystemTime();
	        //System.out.print(datetime);
	        SchedBlock[] sbs = new SchedBlock[1];
	        
	        // read  project status in xml file
	        filename1=new URL("http://frank.aoc.nrao.edu:8080/xmlDocs/ProjectStatus.xml");
			BufferedReader fr1 = new BufferedReader(new InputStreamReader(filename1.openStream()));
            ProjectStatus obj1 = ProjectStatus.unmarshalProjectStatus(fr1);
            // read obsproject status in xml file
            filename2=new URL("http://frank.aoc.nrao.edu:8080/xmlDocs/ObsProject.xml");
			BufferedReader fr2 = new BufferedReader(new InputStreamReader(filename2.openStream()));
			ObsProject obj2 = ObsProject.unmarshalObsProject(fr2);
            //read schedBlock in xml file
            filename=new URL("http://frank.aoc.nrao.edu:8080/xmlDocs/SchedBlock.xml");
            BufferedReader fr = new BufferedReader(new InputStreamReader(filename.openStream()));
            SchedBlock obj = SchedBlock.unmarshalSchedBlock(fr);
            
            sbs[0] = obj;
	        Project project1 = ProjectUtil.map(obj2, sbs, obj1, datetime);
	        /*
	        System.out.println("DataReduction:"+project1.getProgram().getDataReductionProcedureName());
	        Info.append("DataReduction:"+project1.getProgram().getDataReductionProcedureName()+"\n");
	        Info.append("ID:"+project1.getId()+"\n");
	        Info.append("Project ID:"+project1.getObsProjectId()+"\n");
	        Info.append("PI:"+project1.getPI()+"\n");
	        Info.append("Project name:"+project1.getProjectName()+"\n");
	        Info.append("Status ID:"+project1.getProjectStatusId()+"\n");
	        Info.append("Proposal:"+project1.getProposalId()+"\n");
	        //Info.append("Proj:"+project1.toString().toString()+"\n");
	        
	        Info.append("Compled:"+Integer.toString(project1.getNumberProgramsCompleted())+"\n");
	        Info.append("used time:"+Integer.toString(project1.getTotalUsedTimeInSeconds())+"\n");
	        Info.append("unitset "+project1.getProgram().getObsUnitSetStatusId()+"\n");
	        */
	        
            System.out.println(project1.getNumberProgramsCompleted());
            System.out.println(project1.getTotalUsedTimeInSeconds());
            //Info.addNotify();
            mainpanel1.addNotify();
		}
		catch (Exception e) {
		}
		
	}
	
	void makeUI(Container container) {
		System.out.println("Start to deploy the GUI");
		container.setLayout(new BorderLayout(5,5));
		container.setPreferredSize(new Dimension(800,600));
		mainpanel1 = new JPanel();
		//mainpanel1.setLayout(new GridLayout(1,2));
		mainpanel1.setLayout(new BorderLayout(5,5));
		mainpanel2 = new JPanel();
		mainpanel2.setLayout(new GridLayout(1,2));
		mainpanel3 = new JPanel();
		mainpanel3.setLayout(new GridLayout(1,2));
		
		SBControl = new JTable(SBvalue,SBheader);
		SBvalue[0][0]="sb1";
		SBvalue[0][1]="Expert";
		SBvalue[0][2]="Ready";
		SBvalue[0][3]="0";
		SBvalue[0][4]="0";
		SBvalue[0][5]="Ready";
		SBvalue[0][6]="%0";
		SBvalue[0][7]="3600 sec";
		
		JScrollPane SBscrollPane = new JScrollPane(SBControl);
		TitledBorder SBtitle = BorderFactory.createTitledBorder("ObsUnitSet");
		SBtitle.setTitleJustification(TitledBorder.CENTER);
		SBtitle.setTitleColor(Color.RED);
		SBscrollPane.setBorder(SBtitle);
		SBscrollPane.setPreferredSize(new Dimension(800,100));
		SBControl.addNotify();
		//Info = new JTextArea(200,250);
		//Info.setPreferredSize(new Dimension(300,120));
		//JScrollPane scrollPane = 
		//    new JScrollPane(Info,
		//                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		//                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//Info.setEditable(false);
	    SBdetail(mainpanel1);
	    //SBdetail(mainpanel2);
		mainpanel1.add(mainpanel3,BorderLayout.CENTER);
		EBpane.addTab("EB1",mainpanel2);
		mainpanel1.add(EBpane,BorderLayout.SOUTH);
		pane.addTab("SB1",mainpanel1);
		//EBpane.addTab("EB2",mainpanel2);

		//pane.addTab("SchedBlock2",mainpanel2);
		
		//mainpanel.add(Info,BorderLayout.CENTER);
		container.add(SBscrollPane,BorderLayout.NORTH);
		container.add(pane,BorderLayout.CENTER);
		//container.add(EBpane,BorderLayout.SOUTH);
	}
	
	
	    void SBdetail(JPanel mainpanel) {
	    	JScrollPane SciencePane=null;
	    	for (int i=0;i<panelgroup.length;i++) {
	    		panelgroup[i]= new JPanel();
	    		if (i==0) {
	    			JLabel ScienceGoal,unit,representFreq,representFreqvalue,minAcceptAngResol,minAcceptAngResolvalue,
	    				maxacceptAngresol,maxacceptAngresolvalue,OBMode,unit1,OBminAcceptAngResol,OBminAcceptAngResolvalue,
	    				OBmaxacceptAngresol,OBmaxacceptAngresolvalue;
		    		
	    			ScienceGoal = new JLabel("ScienceGoal");
	    			unit = new JLabel(" ");
	    			representFreq=new JLabel("representativeFrequence:");
	    			representFreqvalue=new JLabel("240 GHz");
	    			minAcceptAngResol=new JLabel("MinAcceptableAngResolution:");
	    			minAcceptAngResolvalue=new JLabel("none");
	    			maxacceptAngresol = new JLabel("MaxAcceptableAngResolution:");
	    			maxacceptAngresolvalue = new JLabel("none");
	    			
	    			OBMode = new JLabel("ObservingMode");
	    			unit1 = new JLabel(" ");
	    			OBminAcceptAngResol=new JLabel("MinAcceptableAngResolution:");
	    			OBminAcceptAngResolvalue=new JLabel("none");
	    			OBmaxacceptAngresol = new JLabel("MaxAcceptableAngResolution:");
	    			OBmaxacceptAngresolvalue = new JLabel("none");
	    		
	    			JLabel[] labelgroup ={ScienceGoal,unit,representFreq,representFreqvalue,minAcceptAngResol,minAcceptAngResolvalue,
		    				maxacceptAngresol,maxacceptAngresolvalue,OBMode,unit1,OBminAcceptAngResol,OBminAcceptAngResolvalue,
		    				OBmaxacceptAngresol,OBmaxacceptAngresolvalue};
	    		
	    		panelgroup[i].setLayout(new GridLayout(7,2));
	    		for (int j=0;j<labelgroup.length;j++){
	    		panelgroup[i].add(labelgroup[j]);
	    		}
	    		SciencePane = new JScrollPane(panelgroup[i]);
	    		}
	    		
	    		if(i==1){
	    			panelgroup[i].setLayout(new GridLayout(4,2));
	    			JLabel MaxPWVC,MaxPWVCvalue,Seeing,Seeingvalue,PhaseStability,PhaseStabilityvalue;
	    			//JLabel[] Weathergroup = {MaxPWVC,MaxPWVCvalue,Seeing,Seeingvalue,PhaseStability,PhaseStabilityvalue};
	    			MaxPWVC= new JLabel("MaxPWVC:");
	    			MaxPWVCvalue= new JLabel("none");
	    			Seeing= new JLabel("Seeing");
	    			Seeingvalue=new JLabel("none");
	    			PhaseStability = new JLabel("PhaseStability:");
	    			PhaseStabilityvalue = new JLabel("none");
	    			JLabel[] Weathergroup = {MaxPWVC,MaxPWVCvalue,Seeing,Seeingvalue,PhaseStability,PhaseStabilityvalue};
	    			for (int j=0;j<Weathergroup.length;j++){
	    	    		panelgroup[i].add(Weathergroup[j]);
	    	    		}
	    		}
				
	    		if (i==2) {
	    			String[] EBheader = {"Parameter","Value"};
	    			String[][] EBvalue ={{"execBlockId","ExecBlock_0"},{"telescopeName","test"},
	    							   {"configName","A"},{"numAntenna","2"},
	    							   {"baseRangeMin","0.0"},{"baseRangeMax","0.0"},
	    							   {"baseRmsMinor","0.0"},{"baseRmsMajor","0.0"},
	    							   {"basePa","0.0"},{"timeInterval","4675343410339384758934430"},
	    							   {"observeName","No Name"},{"observingLog","1 1 \"one\""},
	    							   {"schedulerMode","1 2 \"Fame\"\"Glory\""},{"projectId",""},
	    							   {"siteLongitude","0.0"},{"siteLatitude","0.0"},
	    							   {"siteAltitude","0.0"},{"flagRow","false"},
	    							   {"execBlockUID",""},{"aborted","false"},
	    							   {"antennaId","1 2 Antenna_0 Antenna_1"}
	    			};
	    			JTable EBtable = new JTable(EBvalue,EBheader);
	    			JScrollPane scrollpane = new JScrollPane(EBtable);
	    			panelgroup[i].setLayout(new GridLayout(1,1));
	    			panelgroup[i].add(scrollpane);
	    		}
	    		
	    		if(i==3)  {
	    			String[] Calibration= {"AmplitudeChart","AtmosphereSysTempChart",
							"AtmosphereTauChart","CurvAmplitudeChart",
							"CurvePhaseChart","DelayChart",
							"FocusChart","PhaseChart",
							"PointingChart","SeeingChart"};
	    			JComboBox QuickLookList = new JComboBox(Calibration);
	    			picture = new JLabel();
	    			QuickLookList.setSelectedIndex(4);
	    			QuickLookList.addActionListener(this);
	    			picture.setFont(picture.getFont().deriveFont(Font.ITALIC));
	    			picture.setHorizontalAlignment(JLabel.CENTER);
	    			updateLabel("http://frank.aoc.nrao.edu:8080/quicklook.jpg");
	    			picture.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
	    			picture.setPreferredSize(new Dimension(177, 122+10));    
	    			panelgroup[i].setLayout(new BorderLayout());
	    			panelgroup[i].add(QuickLookList,BorderLayout.NORTH);
	    			panelgroup[i].add(picture,BorderLayout.CENTER);	
	    		}
 				panelgroup[i].setPreferredSize(new Dimension(200,250));
				TitledBorder title = BorderFactory.createTitledBorder(panelname[i]);
				title.setTitleJustification(TitledBorder.CENTER);
				title.setTitleColor(Color.RED);
				panelgroup[i].setBorder(title);
			}
	    	mainpanel3.add(SciencePane);
	    	mainpanel3.add(panelgroup[1]);
	    	mainpanel2.add(panelgroup[2]);
	    	mainpanel2.add(panelgroup[3]);
	    	//mainpanel1.add(mainpanel2,BorderLayout.SOUTH);
	    }
	    
	    /** Listens to the combo box. */
	    public void actionPerformed(ActionEvent e) {
	        JComboBox cb = (JComboBox)e.getSource();
	        String petName = (String)cb.getSelectedItem();
	        updateLabel(petName);
	    }

	    protected void updateLabel(String name) {
	    	try {
	    	URL imgURL= new URL(name);
	        ImageIcon icon = new ImageIcon(imgURL);
	        picture.setIcon(icon);
	        picture.setToolTipText("A drawing of a " + name.toLowerCase());
	        if (icon != null) {
	            picture.setText(null);
	        } else {
	            picture.setText("Image not found");
	        }
	    	} 
	    	catch(Exception e) {
	    		System.out.println("can not find the URL files");
	    	}
	       
	    }
	    
	/**
	 * @param args
	 */
	public void init() {
		// TODO Auto-generated method stub
		System.out.println("Init method called");
		makeUI(getContentPane());
		
		try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    //createGUI();
                }
            });
        } catch (Exception e) { 
            System.err.println("createGUI didn't successfully complete");
        }
	}
}
