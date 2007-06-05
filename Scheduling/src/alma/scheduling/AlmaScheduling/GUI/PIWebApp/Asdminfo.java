/**
 * 
 */

package alma.scheduling.AlmaScheduling.GUI.PIWebApp;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * @author wlin
 *
 */
public class Asdminfo extends JApplet implements ActionListener {

	/**
	 * 
	 */
	//ContentPane contentpane;
	JScrollPane Ascrollpane;
	JPanel AntennaPanel,ExecBlockPanel,StationPanel;
	JLabel AntennaLabel,ExecBlockLabel,StationLabel;
	JButton AntennaButton,ExecBlockButton,StationButton;
	JTable AntennaTable,ExecBlockTable,StationTable;
	
	public Asdminfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public void start()  { }
	void makeUI(Container container) { 
		container.setLayout(new BorderLayout(5,5));
		container.setPreferredSize(new Dimension(800,200));
		AntennaPanel = new JPanel();
		AntennaPanel.setLayout(new BorderLayout());

		AntennaButton = new JButton("Click to Show Antenna Info");

		AntennaPanel.add(AntennaButton,BorderLayout.NORTH);
		//AntennaPanel.setPreferredSize(new Dimension(800,200));
		AntennaButton.addActionListener(this);
		String[] AntennaColumnsNames = {"antennaId","name","type","xPosition",
										"yPosition","zPosition","time","xOffset",
										"yOffset","zOffset","dishDiameter",
										"flagRow","stationId"};
		Object[][] data = {
				{"antennaId","name","type","xPosition",	"yPosition","zPosition","time","xOffset",
					"yOffset","zOffset","dishDiameter",	"flagRow","stationId"},
				{"Antenna_0","ALMA01","GROUNDBASE","0.0","0.0","0.0",
					"4675343410365000000","0.0","0.0","0.0","12.0","flase","Station_0"},
				{"Antenna_1","ALMA02","GROUNDBASE","0.0","0.0","0.0",
						"4675343410365000000","0.0","0.0","0.0","12.0","flase","Station_1"},
		};
		AntennaTable = new JTable(data,AntennaColumnsNames);
		AntennaTable.setVisible(false);
		//Ascrollpane = new JScrollPane(AntennaTable);
		//Ascrollpane.setVisible(false);
		AntennaPanel.add(AntennaTable,BorderLayout.CENTER);
		ExecBlockPanel = new JPanel();
		ExecBlockPanel.setLayout(new BorderLayout());
		ExecBlockButton = new JButton("Click to Show ExecBlock Info");
		ExecBlockButton.addActionListener(this);
		ExecBlockPanel.add(ExecBlockButton,BorderLayout.NORTH);
		String[] ExecColumnsNames = {"","","","","","","","","","","","","","","","","","","",""};
		Object[][] Execdata ={
				{"execBlockId","telescopeName","configName","numAntenna","baseRangeMin","baseRangeMax",
				 "baseRmsMinor","baseRmsMajor","basePa","timeInterval","observerName","observingLog",
				 "schedulerMode","projectId","siteLongitude","siteAltitude","flagRow","execBlockUID",
				 "aborted","antennaId"},
				 {"ExecBlock_0","test","A","2","0.0","0.0","0.0","0.0","0.0","467534341036500000010118000000",
				  "No Name","1 1 one","1 2 Fame Glory","","0.0","0.0","0.0","false","","false","1 2 Antenna_0"}
		};
		ExecBlockTable=new JTable(Execdata,ExecColumnsNames);
		ExecBlockTable.setVisible(false);
		ExecBlockPanel.add(ExecBlockTable,BorderLayout.CENTER);
		//ExecBlockPanel.setPreferredSize(new Dimension(800,80));
		//Station
		StationPanel = new JPanel();
		StationPanel.setLayout(new BorderLayout());
		StationButton = new JButton("Click to Show Station Info");
		StationButton.addActionListener(this);
		String[] StationColumnsNames = {"name","position","type","stationId"};
		Object[][] Stationdata = {
				{"name","position","type","stationId"},
				{"PAD001","1 3 -1601361.555455 -5042191.805932 3554531.803007","ANTENNA","Station_0"},
				{"PAD002"," 1 3 -1601328.438917 -5042203.194271 3554532.360703","ANTENNA","Station_1"},
		};
		StationTable = new JTable(Stationdata,StationColumnsNames);
		StationTable.setVisible(false);
		StationPanel.add(StationButton,BorderLayout.NORTH);
		StationPanel.add(StationTable,BorderLayout.CENTER);
		StationPanel.setPreferredSize(new Dimension(800,260));
		//ExecBlockPanel.add(StationPanel,BorderLayout.SOUTH);
		//add panel to container
		container.add(AntennaPanel,BorderLayout.NORTH);
		container.add(ExecBlockPanel,BorderLayout.CENTER);
		//container.add(StationPanel,BorderLayout.SOUTH);
		
		//container.addNotify();
	}
	
	public void actionPerformed(ActionEvent e) {
			
			if(e.getSource()==AntennaButton){
				
				if(0==AntennaButton.getText().compareTo("Click to Show Antenna Info")){
				AntennaButton.setText("Click to hiden the info");
				AntennaTable.setVisible(true);}
				else {
				AntennaButton.setText("Click to Show Antenna Info");
				AntennaTable.setVisible(false);
				}
				
			}
			else if(e.getSource()==ExecBlockButton) {
				if(0==ExecBlockButton.getText().compareTo("Click to Show ExecBlock Info")){
					ExecBlockButton.setText("Click to hiden the info");
					ExecBlockTable.setVisible(true);}
					else {
						ExecBlockButton.setText("Click to Show ExecBlock Info");
						ExecBlockTable.setVisible(false);
					}
			}
			else if(e.getSource()==StationButton) {
				if(0==StationButton.getText().compareTo("Click to Show Station Info")){
					StationButton.setText("Click to hiden the info");
					StationTable.setVisible(true);}
					else {
						StationButton.setText("Click to Show Station Info");
						StationTable.setVisible(false);
					}
			}
			
			//AntennaPanel.add(AntennaTable,BorderLayout.CENTER);
			AntennaPanel.addNotify();
			getContentPane().addNotify();
		//}
	}
	
	public void init() {
		
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
