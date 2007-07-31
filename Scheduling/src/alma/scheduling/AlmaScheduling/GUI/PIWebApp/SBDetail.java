/**
 * 
 */
package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
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

/**
 * @author wlin
 *
 */
public class SBDetail extends JApplet implements ActionListener {

	URL filename,filename1,filename2;
	JTable SBControl;
	String[] SBheader={"SB name","Mode","Status","execute count","Maximum time"};
	Object[][] SBvalue ={
			{"","","","",""},
			{"","","","",""},
			{"","","","",""}
	};
	private final JTabbedPane pane = new JTabbedPane();
	JTextArea Info;
	JPanel mainpanel1,mainpanel2;
	JPanel ExecuteBlock,RepTPanel,SchedCPanel,WeatherandTempCPanel;
	JPanel[] panelgroup = {ExecuteBlock,RepTPanel,SchedCPanel,WeatherandTempCPanel};
	String[] panelname = {"Execute Block","Representative Target","Scheduler Constraints","WeatherandTemporal Constraints"};
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
            System.out.println(project1.getNumberProgramsCompleted());
            System.out.println(project1.getTotalUsedTimeInSeconds());
            Info.addNotify();
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
		mainpanel1.setLayout(new GridLayout(2,2));
		mainpanel2 = new JPanel();
		mainpanel2.setLayout(new GridLayout(2,2));
		SBControl = new JTable(SBvalue,SBheader);
		SBvalue[0][0]="sb1";
		SBvalue[0][1]="Expert";
		SBvalue[0][2]="Ready";
		SBvalue[0][3]="1";
		SBvalue[0][4]="3600";
		
		JScrollPane SBscrollPane = new JScrollPane(SBControl);
		TitledBorder SBtitle = BorderFactory.createTitledBorder("SBStatus");
		SBtitle.setTitleJustification(TitledBorder.CENTER);
		SBtitle.setTitleColor(Color.RED);
		SBscrollPane.setBorder(SBtitle);
		SBscrollPane.setPreferredSize(new Dimension(800,100));
		SBControl.addNotify();
		Info = new JTextArea(200,250);
		Info.setPreferredSize(new Dimension(300,120));
		JScrollPane scrollPane = 
		    new JScrollPane(Info,
		                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		Info.setEditable(false);
	    SBdetail(mainpanel1);
	    //SBdetail(mainpanel2);
		pane.addTab("SchedBlock1",mainpanel1);
		//pane.addTab("SchedBlock2",mainpanel2);
		//mainpanel.add(Info,BorderLayout.CENTER);
		container.add(SBscrollPane,BorderLayout.NORTH);
		container.add(pane,BorderLayout.CENTER);
	}
	
	
	    void SBdetail(JPanel mainpanel) {
	    	for (int i=0;i<panelgroup.length;i++) {
	    		panelgroup[i]= new JPanel();
	    		if (i==0) {
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
	    		
	    		if(i==1){
	    			String[] RepresentativeTargetheader= {"abstractInstrumentSpecId","fieldSourceId",
	    									"observingParametersIdList","TotalPowerMeasurement"};
	    			String[][] RepresentativeTargetvalue ={
	    				{"1152548792442/341322 2961012348","1152548792614/7797531 3188856610","1152548792418/22178007 1822728600",""},
	    				{"1152548792442/341322 2961012348","1152548792468/10688173 3188856610","1152548792418/22178007 1822728600",""},
	    				{"1152548792442/341322 2961012348","1152548792581/10185078 3188856610","1152548792418/22178007 1822728600",""},
	    				{"1152548792442/341322 2961012348","1152548792590/31582617 3188856610","1152548792418/22178007 1822728600",""},
	    				{"1152548792442/341322 2961012348","1152548792599/20211730 3188856610","1152548792418/22178007 1822728600",""},
	    				{"1152548792442/341322 2961012348","1152548792606/2220309 3188856610","1152548792418/22178007 1822728600",""}
	    			};
	    			JTable RerTargettable = new JTable(RepresentativeTargetvalue,RepresentativeTargetheader);
	    			panelgroup[i].setLayout(new GridLayout(1,1));
	    		    JScrollPane scrollpane = new JScrollPane(RerTargettable);
	    		    panelgroup[i].add(scrollpane);
	    		}
				
	    		if (i==2) {
	    			String[] SchedConstheader = {"Parameter","Value","Unit"};
	    			String[][] SchedConstvalue ={ {"representativeFrequency","unknow","GHz"},
	    								  {"minAcceptableAngResolution"	,"unknow","arcsec"},
	    								  {"maxAcceptableAngResolution"	,"unknow","arcsec"}
	    			};
	    			JTable schedConstable = new JTable(SchedConstvalue,SchedConstheader);
	    			JScrollPane scrollpane = new JScrollPane(schedConstable);
	    			panelgroup[i].setLayout(new GridLayout(1,1));
	    			panelgroup[i].add(scrollpane);
	    		}
	    		
	    		if(i==3)  {
	    			String[] WeathConstheader ={"Parameter","Value","Unit"};
	    			String[][] WeathConstvalue ={
	    					{"maxPWVC","unknow","unknow"},
	    					{"seeing","unknow","unknow"},
	    					{"phaseStability","unknow","unknow"}
	    			};
	    			JTable Weathtable = new JTable(WeathConstvalue,WeathConstheader);
	    			JScrollPane scrollpane = new JScrollPane(Weathtable);
	    			String [] Temporalheader ={"Parameter","Value"};
		    		String[][] Temporalvalue ={{"startTime",""},{"EndTime",""},{"allowedMargin",""}};
		    		JTable Temporaltable = new JTable(Temporalvalue,Temporalheader);
		    		JScrollPane scrollpane1 = new JScrollPane(Temporaltable);
		    		TitledBorder title = BorderFactory.createTitledBorder("Temporal Constraints");
					title.setTitleJustification(TitledBorder.CENTER);
					title.setTitleColor(Color.RED);
					scrollpane1.setBorder(title);
	    			panelgroup[i].setLayout(new GridLayout(2,1));
	    			panelgroup[i].add(scrollpane);
	    			panelgroup[i].add(scrollpane1);
	    			
	    		}
 				panelgroup[i].setPreferredSize(new Dimension(200,250));
				TitledBorder title = BorderFactory.createTitledBorder(panelname[i]);
				title.setTitleJustification(TitledBorder.CENTER);
				title.setTitleColor(Color.RED);
				panelgroup[i].setBorder(title);
				mainpanel.add(panelgroup[i]);
			}
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

