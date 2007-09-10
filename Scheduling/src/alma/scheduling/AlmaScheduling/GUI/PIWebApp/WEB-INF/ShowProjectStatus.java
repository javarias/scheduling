/**
 * 
 */
package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.*;

import java.awt.Graphics;
import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.io.*;
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

//acs


/**
 * @author wlin
 *
 */
public class ShowProjectStatus extends JApplet {

	//int loopslot = -1;  //the current frame number
	//Timer timer;
	//String filename="/export/home/frank/wlin/workspace/Scheduling/Scheduling/src/alma/scheduling/AlmaScheduling/GUI/PIWebApp/xmlDocs/ProjectStatus.xml"; 
	//String filename="http://frank.aoc.nrao.edu:8080/xmlDocs/ProjectStatus.xml";
	URL filename;
	JLabel testLabel = new JLabel("test");
	JPanel mainPanel,ObsandSBpanel ;
	JTable ProjectStatustable,ObsProgramStatustable,ObsandSBtable;
	String[] ProjcolumnNames = {"Project Name","PI Name","TimeOfUpdate","Project Status",
								"ReadyTime","StartTime","EndTime"};
	Object[][] projdata = {
							{"","","","","","",""}
							};
	//	component for ObsprogramStatus
	
	String[] ObsProgramColumnNames = {"Timeofupdate","Status","ReadyTime",
			  							"StartTime","EndTime","TotalRequredTime",
			  							"TotalUsedTime","TotalObsUnitset","NumberObsUnitSetsComplete",
			  							"NumberObsUnitSetsfailed","TotalSB","NumberSBComplete",
			  							"NumberSBfailed"};
	Object[][] obsprogram = {
								{"","","","","","","","","","","","",""}						  
	};
	
	String[] SBheader = {"TimeOfUpdate","Status","ReadyTime","StartTime","EndTime","TotalRequredTime",
							"TotalUsedTime","finish percentage"};
	Object[][] SBvalue ={
								{"","","","","","","",""},
								{"","","","","","","",""},
								{"","","","","","","",""},
								{"","","","","","","",""},
								{"","","","","","","",""}
	};
	         
	DefaultTableModel dm;
	JProgressBar progressBar;
    //the timer animating the images
	
	protected void loadAppletParameters() {
        //Get the applet parameters.
        
    }
	
	public void init() {
        //loadAppletParameters();
        makeUI(getContentPane());
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
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
	
	void makeUI(Container container) {
		ObsandSBpanel = new JPanel();
		ObsandSBpanel.setPreferredSize(new Dimension(800,250));
		container.setLayout(new BorderLayout(5,20));
	
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		mainPanel.setPreferredSize(new Dimension(800,200));
		TitledBorder title,Obsprogtitle,SBtitle;
		title = BorderFactory.createTitledBorder("Project Status");
		title.setTitleJustification(TitledBorder.CENTER);
		ProjectStatustable = new JTable(projdata,ProjcolumnNames);
 		ProjectStatustable.setPreferredScrollableViewportSize(new Dimension(800,35));
 		//ProjectStatustable.setEnabled(false);
 		ProjectStatustable.setRowHeight(30);
 		//ProjectStatustable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		//JScrollPane scrollpane= new JScrollPane(ProjectStatustable);
 		// set up the Obs Program table
 		Obsprogtitle = BorderFactory.createTitledBorder("ObsProgram Status");
 		Obsprogtitle.setTitleColor(Color.BLUE);
 		Obsprogtitle.setTitleJustification(TitledBorder.CENTER);

 		dm=new DefaultTableModel() {
			public Class getColumnClass(int columnIndex){
				return String.class;
			}
		};
	    dm.setDataVector(obsprogram,ObsProgramColumnNames);
	    ObsProgramStatustable = new JTable(dm);
	    ObsProgramStatustable.setPreferredScrollableViewportSize(new Dimension(800,35));
 		ObsProgramStatustable.setRowHeight(30);
 		//ObsProgramStatustable.setEnabled(false);
 		ObsProgramStatustable.setDefaultRenderer(String.class,new MultiLineCellRenderer());
 		
 		JScrollPane Obsscrollpane= new JScrollPane(ObsProgramStatustable);
 		JScrollPane Progscrollpane= new JScrollPane(ProjectStatustable);
		Progscrollpane.setPreferredSize(new Dimension(800,40));
		//mainPanel.setBorder(title);
		Progscrollpane.setBorder(title);
		Obsscrollpane.setBorder(Obsprogtitle);
		Obsscrollpane.setPreferredSize(new Dimension(800,60));
		mainPanel.add(Progscrollpane);
		mainPanel.add(Obsscrollpane);
		ObsandSBtable = new JTable(SBvalue,SBheader);
		JScrollPane SBscrollpane= new JScrollPane(ObsandSBtable);
		SBscrollpane.setPreferredSize(new Dimension(800,200));
		SBtitle = BorderFactory.createTitledBorder("SBStatus");
		SBtitle.setTitleJustification(TitledBorder.CENTER);
		SBtitle.setTitleColor(Color.RED);
		SBscrollpane.setBorder(SBtitle);
		ObsandSBpanel.add(SBscrollpane);
		//ObsandSBpanel.setBackground(Color.blue);
		progressBar = new JProgressBar(0, 100);
		container.add(mainPanel,BorderLayout.NORTH);
		container.add(ObsandSBpanel,BorderLayout.CENTER);
		//container.add(progressBar,BorderLayout.SOUTH);
	}
	
	public void start()  {
		
		try {
			//System.out.println("path:"+(String)System.getProperty("user.home"));
			filename=new URL("http://frank.aoc.nrao.edu:8080/xmlDocs/ProjectStatus.xml");
				
		    //File xmlfile = new File(filename);
            //FileReader fr = new FileReader(xmlfile);
			BufferedReader fr = new BufferedReader(new InputStreamReader(filename.openStream()));
            ProjectStatus obj = ProjectStatus.unmarshalProjectStatus(fr);
            //File xmlfile1 = new File(filename1);
            //FileReader fr1 = new FileReader(xmlfile1);
            //ObsUnitSetStatusTChoice obsunit= ObsUnitSetStatusTChoice.unmarshalObsUnitSetStatusTChoice(fr1);
            projdata[0][0]=obj.getName();
            projdata[0][1]=obj.getPI();
            projdata[0][2]=obj.getTimeOfUpdate();
            projdata[0][3]=obj.getStatus().getState().toString();
            projdata[0][4]=obj.getStatus().getReadyTime();
            projdata[0][5]=obj.getStatus().getStartTime();
            projdata[0][6]=obj.getStatus().getEndTime();
            ProjectStatustable.addNotify();
            //collect information for ObsprogramStatus
            obsprogram[0][0]=obj.getObsProgramStatus().getTimeOfUpdate();
            obsprogram[0][1]=obj.getObsProgramStatus().getStatus().getState().toString();
            obsprogram[0][2]=obj.getStatus().getReadyTime();
            obsprogram[0][3]=obj.getStatus().getStartTime();
            obsprogram[0][4]=obj.getStatus().getEndTime();
            obsprogram[0][5]=obj.getObsProgramStatus().getTotalRequiredTimeInSec();
            obsprogram[0][6]=obj.getObsProgramStatus().getTotalUsedTimeInSec();
            obsprogram[0][7]=obj.getObsProgramStatus().getTotalObsUnitSets();
            obsprogram[0][8]=obj.getObsProgramStatus().getNumberObsUnitSetsCompleted();
            obsprogram[0][9]=obj.getObsProgramStatus().getNumberObsUnitSetsFailed();
            obsprogram[0][10]=obj.getObsProgramStatus().getTotalSBs();
            obsprogram[0][11]=obj.getObsProgramStatus().getNumberSBsCompleted();
            obsprogram[0][12]=obj.getObsProgramStatus().getNumberSBsFailed();
            dm.setDataVector(obsprogram,ObsProgramColumnNames);
            SBvalue[0][0]=obj.getObsProgramStatus().getTimeOfUpdate();
            SBvalue[0][1]=obj.getObsProgramStatus().getStatus().getState().toString();
            SBvalue[0][2]=obj.getStatus().getReadyTime();
            SBvalue[0][3]=obj.getStatus().getStartTime();
            SBvalue[0][4]=obj.getStatus().getEndTime();
            SBvalue[0][5]=obj.getObsProgramStatus().getTotalRequiredTimeInSec();
            SBvalue[0][6]=obj.getObsProgramStatus().getTotalUsedTimeInSec();
            SBvalue[0][7]="%0";
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            mainPanel.addNotify();
		}
		catch (Exception e){
			System.out.println("file reader error"+e.toString());
		}
	}
}

	//	The component that actually presents the GUI.
	class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
		 
	    public MultiLineCellRenderer() {
	      setLineWrap(true);
	      setWrapStyleWord(true);
	      setOpaque(true);
	    }
	 
	    public Component getTableCellRendererComponent(JTable table,
	        Object value, boolean isSelected, boolean hasFocus, int row,
	        int column) {
	      if (isSelected) {
	        setForeground(table.getSelectionForeground());
	        setBackground(table.getSelectionBackground());
	      } else {
	        setForeground(table.getForeground());
	        setBackground(table.getBackground());
	      }
	      setFont(table.getFont());
	      if (hasFocus) {
	        setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
	        if (table.isCellEditable(row, column)) {
	          setForeground(UIManager
	              .getColor("Table.focusCellForeground"));
	          setBackground(UIManager
	              .getColor("Table.focusCellBackground"));
	        }
	      } else {
	        setBorder(new EmptyBorder(1, 2, 1, 2));
	      }
	      setText((value == null) ? "" : value.toString());
	      /* To set the table row height dynamically */
	      int height = (int) getPreferredSize().getHeight();
	      if (height > table.getRowHeight(row))
	        table.setRowHeight(row, height);
	      return this;
	    }
	  }
