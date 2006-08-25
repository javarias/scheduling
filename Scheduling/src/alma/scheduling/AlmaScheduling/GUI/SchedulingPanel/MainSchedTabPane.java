package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.awt.*;
//import java.awt.image.BufferedImage;
import java.util.EventListener;
import java.util.Vector;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
//import javax.swing.text.View;
import java.util.logging.Logger;

import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SBLite;
import alma.exec.extension.subsystemplugin.SubsystemPlugin.PluginContainerServices;

//import alma.acs.container.ContainerServices;
//import javax.swing.plaf.basic.BasicTabbedPaneUI;
//import com.sun.java.swing.plaf.windows.WindowsIconFactory;

public class MainSchedTabPane extends JTabbedPane {
    private final String[] schedColumnInfo = 
        {"Scheduler Name", "Array Name","Type","Status"};
    private Logger logger;
    private int schedulerCount=0;
    private Vector<SchedulerTab> schedulerInfo;
    private TableModel schedulerTableModel;
    private JTable schedulerTable;
    private JPopupMenu rightClickMenu;
    private Object[][] schedRowInfo;
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler;
    private int overTabIndex;
    private JDesktopPane desktop;
    private Vector<Window> openWindows;

    public MainSchedTabPane(PluginContainerServices cs){
        super(JTabbedPane.TOP);
        container = cs;
        try {
            logger = cs.getLogger();
        } catch(Exception e) {
            logger = Logger.getLogger("OfflineMode");
        }
        setup();
    }

    private void setup() {
        getMasterSchedulerRef();
        addTab("Main",createMainView());
        //System.out.println("added main, tab count ="+getTabCount());
        super.setUI(new SchedTabUI());
        addCloseTabListener(new CloseTabListener(){
            public void closeOperation(MouseEvent e) {
                logger.info("in close operation");
                closeTab(overTabIndex);
                //remove(overTabIndex);
            }
        });
        openWindows = new Vector<Window>();
        schedulerInfo = new Vector<SchedulerTab>();
    }

    private void getMasterSchedulerRef(){
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            logger.info("SCHEDULING_PANEL: Got MS");
        } catch (Exception e) {
            masterScheduler = null;
        }
        createRightClickMenu();
    }
    private void releaseMSRef(){
        if(masterScheduler != null){
            container.releaseComponent(masterScheduler.name());
            masterScheduler = null;
        }
        createRightClickMenu();
    }

    private JScrollPane createMainView(){
        JScrollPane s = new JScrollPane();
        JPanel p = new JPanel(new BorderLayout());
        p.add(topSection(), BorderLayout.NORTH);
        p.add(middleSection(), BorderLayout.CENTER);
        p.add(bottomSection(), BorderLayout.SOUTH);
        s.getViewport().add(p);
        s.addMouseListener(new PopupListener());
        return s;
    }

    private JPanel topSection(){
        JPanel p = new JPanel(new GridLayout(4,2));
        p.setBorder(new TitledBorder("Scheduler Info"));
        //1
        p.add(new JLabel("Active Schedulers"));
        p.add(new JLabel("0"));
        //2
        p.add(new JLabel("Dynamic Schedulers "));
        p.add(new JLabel("0"));
        //3
        p.add(new JLabel("Queued Schedulers "));
        p.add(new JLabel("0"));
        //4
        p.add(new JLabel("Interactive Schedulers "));
        p.add(new JLabel("0"));
        return p;
    }

    private JPanel middleSection(){
        JPanel p = new JPanel();
        createSchedulerTableModel();
        JScrollPane hmm = new JScrollPane(schedulerTable);
        p.add(hmm);
        return p;
    }

    private void createSchedulerTableModel(){
        schedRowInfo = new Object[0][schedColumnInfo.length];
        schedulerTableModel = new AbstractTableModel() {
            public int getColumnCount() { return schedColumnInfo.length; }
            public String getColumnName(int column) { return schedColumnInfo[column]; }
            public int getRowCount() { return schedRowInfo.length;     }
            public Object getValueAt(int row, int col) { return schedRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { schedRowInfo[row][col]= val; }
        };
        schedulerTable = new JTable(schedulerTableModel);
        schedulerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Dimension d = new Dimension(250, 75);
        schedulerTable.setPreferredScrollableViewportSize(d);
        ((DefaultTableCellRenderer)schedulerTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        manageTableColumnSize();
    }
    private void manageTableColumnSize(){
        TableColumn column;
        int prev=0;
        for(int j=0; j < schedColumnInfo.length; j++){
            column = schedulerTable.getColumnModel().getColumn(j);
            int w = column.getWidth();
            int n = schedulerTable.getRowCount();
            for (int i = 0; i < n; i ++) {
                TableCellRenderer r = schedulerTable.getCellRenderer(i, j);
                Component c = r.getTableCellRendererComponent(
                        schedulerTable,
                        schedulerTable.getValueAt(i, j),
                        false,
                        false,
                        i,
                        j);
                w = Math.max(w, c.getPreferredSize().width);
            }
            /*
            if(prev < 58 || w < 58){
                prev = 58;
                w = 58;
            }*/
            column.setPreferredWidth(w+5);
        }
    }

    private void updateSchedulerTable(SchedulerTab t){
        schedulerInfo.add(t);
        schedRowInfo = new Object[schedulerInfo.size()][schedColumnInfo.length];
        for(int i=0; i < schedulerInfo.size(); i++){
            schedRowInfo[i][0] = schedulerInfo.elementAt(i).getSchedulerName(); 
            schedRowInfo[i][1] = schedulerInfo.elementAt(i).getArrayName();
            schedRowInfo[i][2] = schedulerInfo.elementAt(i).getSchedulerType();
            schedRowInfo[i][3] = "N/A";
        }
        manageTableColumnSize();
        schedulerTable.repaint();
    }
    private void removeRowFromSchedulerTable(SchedulerTab tab) {
        //get tab from schedulerInfo vector
        int x = getTabPositionInVector(tab);
        if(x!= -1) {
            schedulerInfo.removeElementAt(x);
        }
        //delete row that matches this tab
        int y = getTabPositionInRow(tab);
        if(y != -1) {
            if(schedRowInfo.length >1){
                Object[][] temp = schedRowInfo;
                schedRowInfo = new Object[temp.length -1][temp[0].length];
                for(int i=0; i < temp.length; i++){
                    if(y != i){
                        schedRowInfo[i] = temp[i];
                    } else {
                    }
                }
            } else {
                Object[][] temp = schedRowInfo;
                schedRowInfo = new Object[0][temp[0].length];
                //there's only one, make sure it matches and then remove it!
            }
        }

        manageTableColumnSize();
    }

    private int getTabPositionInVector(SchedulerTab tab){
        for(int i=0; i< schedulerInfo.size(); i++){
            if(schedulerInfo.elementAt(i).getArrayName().equals(tab.getArrayName()) &&
               schedulerInfo.elementAt(i).getSchedulerName().equals(tab.getSchedulerName()) &&
               schedulerInfo.elementAt(i).getSchedulerType().equals(tab.getSchedulerType())){
                  return i;
            }
        }
        return -1;
    }
    private int getTabPositionInRow(SchedulerTab tab){
        for(int i=0; i< schedRowInfo.length; i++){
            if(schedRowInfo[i][0].equals(tab.getSchedulerName()) &&
               schedRowInfo[i][1].equals(tab.getArrayName()) &&
               schedRowInfo[i][2].equals(tab.getSchedulerType())){
                  return i;
            }
        }
        return -1;
    }

    private JPanel bottomSection(){
        JPanel p1 = new JPanel(new BorderLayout());
        JPanel p2 = new JPanel(new GridLayout(2,2));
        p2.setPreferredSize(new Dimension(50,50));
        p1.setBorder(new TitledBorder("Start Schedulers"));
        JButton b = new JButton("Create Array");
        b.setToolTipText("Create an array");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                if(masterScheduler == null){
                    showConnectToMSMessage();
                    return;
                }
                showCreateArrayPopup();
            }
        });
        p2.add(b);
        b = new JButton("Queued");
        b.setToolTipText("Create a Queued Scheduler");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                //check to see if MS is connected.
                if(masterScheduler == null){
                    showConnectToMSMessage();
                    return;
                }
                //prompt to select an array
                String arrayname = ShowArrayFrame.showArraySelectFrame(container,true);
                if(arrayname.equals("") || arrayname == null) {
                    return;
                }
                logger.info("Selected array = "+arrayname);
                addTab("QS - "+arrayname,createQueuedSchedulingView(arrayname)); 
                //System.out.println("added QS, tab count ="+getTabCount());
            }
        });
        p2.add(b);
        b = new JButton("Interactive");
        b.setToolTipText("Create an Interactive Scheduler");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                //check to see if MS is connected.
                if(masterScheduler == null){
                    showConnectToMSMessage();
                    return;
                }
                //prompt to select an array
                String arrayname = ShowArrayFrame.showArraySelectFrame(container,true);
                if(arrayname.equals("") || arrayname == null) {
                    return;
                }
                logger.info("Selected array = "+arrayname);
                try {
                    String is = masterScheduler.startInteractiveScheduling1(
                        arrayname);
                    addTab("IS - "+arrayname,
                        createInteractiveSchedulingView(is, arrayname)); 
                    //System.out.println("added IS, tab count ="+getTabCount());
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        p2.add(b);
        b = new JButton("Dynamic");
        b.setToolTipText("Create a Dynamic Scheduler");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event){
                //check to see if MS is connected.
                if(masterScheduler == null){
                    showConnectToMSMessage();
                    return;
                }
                //ask if they are going to create their own array
                String arrayname = null;
                if(pickTheirOwnArray()) {
                //bring up array selector!
                    arrayname = ShowArrayFrame.showArraySelectFrame(container,true);
                    //here means when arrayname == "" user has canceled manual \
                    //array creation.. cannot continue from this point so exits
                    if(arrayname.equals("")){
                        showErrorMessage(
                            "Cannot create Dynamic Scheduler without an array!");
                        return;
                    }
                }else {
                    //here with arrayname == to "" means scheduler will create array
                    arrayname = "";
                }
                addTab("DS", createDynamicSchedulingView(arrayname));
                //System.out.println("added DS, tab count ="+getTabCount());
            }
        });
        p2.add(b);
        p1.add(p2, BorderLayout.CENTER);
        return p1;
    }

    private void showConnectToMSMessage(){
        JOptionPane.showMessageDialog(this,"Not connected to the MasterScheduler",
                "Not Connected", JOptionPane.ERROR_MESSAGE);
    }
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, 
                "Task not completed.", JOptionPane.ERROR_MESSAGE);
    }

    private boolean pickTheirOwnArray() {
        int answer = JOptionPane.showConfirmDialog(this, 
                "Right now you don't get to pick your array for DS","Array Needed?",
                //"Do you need to create your own array?", "Array Needed?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        /* TODO: Uncomment this when they do get to pick an array for DS.
        if(answer == JOptionPane.YES_OPTION){
            return true;
        } else {
            return false;
        }
        */
        return false;
    }

    private void createRightClickMenu(){
        rightClickMenu = new JPopupMenu("Master Scheduler Functions");
        JMenuItem menuItem;
        if(masterScheduler!=null){
            menuItem = new JMenuItem("Detach");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    detachTab(getSelectedIndex());
                }
            });
            rightClickMenu.add(menuItem);
            menuItem = new JMenuItem("Get Array Info");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    ShowArrayFrame f = new ShowArrayFrame(container, false);
                    f.setVisible(true);
                }
            });
            rightClickMenu.add(menuItem);
            /*
            menuItem = new JMenuItem("Disconnect Master Scheduler");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    releaseMSRef();
                }
            });
            rightClickMenu.add(menuItem);
            */
        } else {
            menuItem = new JMenuItem("Connect To MasterScheduler");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    getMasterSchedulerRef();
                }
            });
            rightClickMenu.add(menuItem);
        }
        addMouseListener(new PopupListener());
    }
    

    private JScrollPane createInteractiveSchedulingView
        (String schedname, String arrayname) {
        
        logger.info("SCHEDULING_PANEL: Start Interactive Scheduling ");
        SchedulerTab tab =new InteractiveSchedTab(container, schedname, arrayname); 
        updateSchedulerTable(tab);
        return (JScrollPane)tab;
    }
    
    private JScrollPane createDynamicSchedulingView(String arrayname) {
        logger.info ("SCHEDULING_PANEL: Start dynamic scheduling");
        SchedulerTab tab = new DynamicSchedTab(container,"DS",arrayname);
        updateSchedulerTable(tab);
        return(JScrollPane)tab;
    }
    //first create a queued scheduler tab
    //then query for SBs(sblites)
    private JScrollPane createQueuedSchedulingView(String arrayname){
        logger.info("SCHEDULING_PANEL: Start Queued Scheduling ");
        SchedulerTab tab =new QueuedSchedTab(container, arrayname); 
        updateSchedulerTable(tab);
        return (JScrollPane)tab;
    }

    private void showCreateArrayPopup() {
        CreateArrayFrame f = new CreateArrayFrame(container);
        f.setVisible(true);
        openWindows.add(f);
        //Frame parent = JOptionPane.getFrameForComponent(this);
    }

    private void showErrorPopup() {
    }

    /**
      * Detaches frame at index from rest of GUI, puts it back into the GUI 
      * when closed.
      * Code gotten from web: Credit to 
      *     David Bismut, davidou@mageos.com
      *     Intern, SETLabs, Infosys Technologies Ltd. May 2004 - Jul 2004
      *     Ecole des Mines de Nantes, France
      */
	public void detachTab(int index) {
        //System.out.println(index);
		if (index < 0 || index >= getTabCount())
			return;

		final JFrame frame = new JFrame();
		Window parentWindow = SwingUtilities.windowForComponent(this);
		final int tabIndex = index;
		final JComponent c = (JComponent) getComponentAt(tabIndex);
		final Icon icon = getIconAt(tabIndex);
		final String title = getTitleAt(tabIndex);
		final String toolTip = getToolTipTextAt(tabIndex);
		final Border border = c.getBorder();
		removeTabAt(index);
		c.setPreferredSize(c.getSize());
		frame.setTitle(title);
		frame.getContentPane().add(c);
		frame.setLocation(parentWindow.getLocation());
		frame.pack();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				frame.dispose();
				insertTab(title, icon, c, toolTip, Math.min(tabIndex,
						getTabCount()));
				c.setBorder(border);
				setSelectedComponent(c);
			}
		});
		WindowFocusListener windowFocusListener = new WindowFocusListener() {
			long start;
			long end;
			public void windowGainedFocus(WindowEvent e) {
				start = System.currentTimeMillis();
			}
			public void windowLostFocus(WindowEvent e) {
				end = System.currentTimeMillis();
				long elapsed = end - start;
				if (elapsed < 100)
					frame.toFront();

				frame.removeWindowFocusListener(this);
			}
		};
		/*
		 * This is a small hack to avoid Windows GUI bug, that prevent a new
		 * window from stealing focus (without this windowFocusListener, most of
		 * the time the new frame would just blink from foreground to
		 * background). A windowFocusListener is added to the frame, and if the
		 * time between the frame beeing in foreground and the frame beeing in
		 * background is less that 100ms, it just brings the windows to the
		 * front once again. Then it removes the windowFocusListener. Note that
		 * this hack would not be required on Linux or UNIX based systems.
		 */
		frame.addWindowFocusListener(windowFocusListener);
		frame.setVisible(true);
		frame.toFront();
	}
    
    public synchronized void addCloseTabListener(CloseTabListener l){
        listenerList.add(CloseTabListener.class, l);
    }

    public void closeTabEvent(MouseEvent e, int tabIndex) {
        logger.info("in close tab event");
        EventListener close[] = getListeners(CloseTabListener.class);
        overTabIndex = tabIndex;
        for(int i=0; i< close.length; i++){
            ((CloseTabListener)close[i]).closeOperation(e);
        }
    }
    private void closeTab(int i) {
        logger.info("in close tab");
        SchedulerTab tab = (SchedulerTab)getComponentAt(i);
        tab.exit();
        removeRowFromSchedulerTable(tab);
        remove(i);
    }

    public void exit() {
        //release all components.
        logger.info("SCHEDULING_PANEL: in exit of MainSchedulingTab");
        //make sure there are no open dialogs/frames/etc..
        try {
            container.releaseComponent(masterScheduler.name());
        } catch(Exception e){
            logger.warning("SCHEUDLING_PANEL: Error releasing components");
        }
        try {
            for(int i=0; i<openWindows.size();i++){
                openWindows.elementAt(i).dispose();
            }
        }catch(Exception e){
            //logger.warning("SCHEDULING_PANEL: Error
        }
        try {
            int all= getComponentCount();
            for(int i=0; i < all;i++){
                logger.info("calling closetab in exit");
                closeTab(i);
            }
        } catch (Exception e) {
        }
    }
///////////////////////////////////////////////////////
    
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            maybeShowPopup(e);
        }
        public void mouseReleased(MouseEvent e){
            maybeShowPopup(e);
        }
        private void maybeShowPopup(MouseEvent e){
            if (e.isPopupTrigger()) {
                rightClickMenu.show(e.getComponent(),
                       e.getX(), e.getY());
            }
        }
    }

}
