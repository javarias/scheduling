package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.EventListener;
import java.util.Vector;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.logging.Logger;

import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.SBLite;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.acs.container.ContainerServices;

public class MainSchedTabPane extends JTabbedPane {
    private PluginContainerServices container;
    private MainSchedTabPaneController controller;
    private JPopupMenu rightClickMenu;
    private boolean connectedToSystem = false;
    private boolean createArrayEnabled = false;
    private Logger logger;
    private int overTabIndex;
    private JPanel mainPanel;
    private JPanel topPanel;
    private SearchArchiveOnlyTab archiveTab;
    private CreateArrayPanel middlePanel;
    private JPanel showAntennaPanel;
    private JPanel middleButtonPanel;
    private Vector<Window> openWindows;
    private JButton interactiveB;
    private JButton queuedB;
    private JButton dynamicB;

    /**
      * Constructor
      */
    public MainSchedTabPane(){
        super(JTabbedPane.TOP);
        setup();
        createRightClickMenu();   
        super.addMouseListener(new PopupListener());
        Dimension d = getPreferredSize();
        setMaximumSize(d);
    }
        
    public void setup() {
        openWindows = new Vector<Window>();
        createSearchArchiveOnlyTab();
        addTab("Main",createMainTab());
        addTab("Search", archiveTab);
        super.setUI(new SchedTabUI());
        addCloseTabListener(new CloseTabListener(){
            public void closeOperation(MouseEvent e) {
                logger.info("in close operation");
                closeTab(overTabIndex);
            }
        });
    }
    
    public void secondSetup(PluginContainerServices cs){
        container = cs;
        controller = new MainSchedTabPaneController(cs);
        logger = cs.getLogger();
        archiveTab.connectedSetup(cs);
        middlePanel.connectedSetup(cs);
        logger.info("SCHEDULING_PANEL: Second setup, connected to manager");
    }

    private void createRightClickMenu(){
        rightClickMenu = new JPopupMenu("Master Scheduler Functions");
        JMenuItem menuItem;
        if(connectedToSystem) {
            //don't need anything...
            menuItem = new JMenuItem("disconnect");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    connectedToSystem = false;
                    createRightClickMenu();
                }
            });

            rightClickMenu.add(menuItem);
        } else {
            menuItem = new JMenuItem("Connect");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    connectedToSystem = true;
                    createRightClickMenu();
                }
            });

            rightClickMenu.add(menuItem);
        }
    }

    public void createSearchArchiveOnlyTab() {
        archiveTab = new SearchArchiveOnlyTab();
    }
    public JPanel createMainTab(){ 
        mainPanel = new JPanel(new BorderLayout());
        try {
        createTopPanel();
        createMiddlePanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        }catch(Exception e){
            e.printStackTrace();
        }
        return mainPanel;
    }
    
    public void createTopPanel(){
        topPanel = new JPanel(new GridLayout(1,2));
        topPanel.setBorder(new TitledBorder("Start New Scheduler"));
        JPanel buttons = new JPanel(new GridLayout(1,3));
        interactiveB = new JButton("Interactive");
        queuedB = new JButton("Queued");
        dynamicB = new JButton("Dynamic");

        interactiveB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(connectedToSystem){
                        createArrayEnabled = true;
                        middlePanel.setEnabled(true);
                        middlePanel.prepareCreateArray("interactive");
                        
                        //createArray with mode 'interactive'
                        //if array created open interactive tab
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               
        queuedB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(connectedToSystem){
                        createArrayEnabled = true;
                        middlePanel.setEnabled(true);
                       // middlePanel.setArrayMode("queued");
                        middlePanel.prepareCreateArray("queued");
                        //createArray with mode 'queued'
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               
        dynamicB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(connectedToSystem){
                        createArrayEnabled = true;
                        middlePanel.setEnabled(true);
                        middlePanel.prepareCreateArray("dynamic");
                        //createArray with mode 'dynamic'
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               

        buttons.add(interactiveB);
        buttons.add(queuedB);
        buttons.add(dynamicB);
        topPanel.add(buttons);
       // topPanel.add(new JPanel()); //spacer
    }
    
    public void createMiddlePanel() {
        middlePanel = new CreateArrayPanel();
        middlePanel.setEnabled(false);
    }

    private void updateAntennaView(){
    }

    private void showConnectMessage(){
        JOptionPane.showMessageDialog(this,"Not connected to the System",
                "Not Connected", JOptionPane.ERROR_MESSAGE);
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
        SchedulerTab tab = (SchedulerTab)getComponentAt(i);
        tab.exit();
        remove(i);
        //remove it from open windows?
    }

    public void exit(){
    }

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
    /* OLD View
    private final String[] schedColumnInfo = 
        {"Scheduler Name", "Array Name","Type","Status"};
    private Logger logger;
    private int schedulerCount=0;
    private Vector<SchedulerTab> schedulerInfo;
    private TableModel schedulerTableModel;
    private JTable schedulerTable;
    private JPopupMenu rightClickMenu;
    private Object[][] schedRowInfo;
    public PluginContainerServices container;
    //public ContainerServices container;
    private MasterSchedulerIF masterScheduler;
    private int overTabIndex;
    private JDesktopPane desktop;
    private Vector<Window> openWindows;
    private JLabel allSched;
    private int allSchedInt;
    private JLabel dynamicSched;
    private int dynamicSchedInt;
    private JLabel interactiveSched;
    private int interactiveSchedInt;
    private JLabel queuedSched;
    private int queuedSchedInt;
    private int msCounter;


    //public MainSchedTabPane(ContainerServices cs){
    public MainSchedTabPane() {//PluginContainerServices cs){
        super(JTabbedPane.TOP);
        setup();
        msCounter = 0;
        allSchedInt =0;
        dynamicSchedInt =0;
        interactiveSchedInt =0;
        queuedSchedInt =0;
    }

    public void setup() {
        openWindows = new Vector<Window>();
        schedulerInfo = new Vector<SchedulerTab>();
        addTab("Main",createMainView());
        //System.out.println("added main, tab count ="+getTabCount());
        super.setUI(new SchedTabUI());
        //addTab("test",createMainView());
        addCloseTabListener(new CloseTabListener(){
            public void closeOperation(MouseEvent e) {
                logger.info("in close operation");
                closeTab(overTabIndex);
                //remove(overTabIndex);
            }
        });
    }
    public void secondSetup(PluginContainerServices cs){
        container = cs;
        try {
            logger = cs.getLogger();
        } catch(Exception e) {
            logger = Logger.getLogger("OfflineMode");
        }
        logger.info("SCHEDULING_PANEL: in second setup for SP.");
        createRightClickMenu();
    }

    private void getMSRef(){
        try {
            masterScheduler =
                alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            logger.info("SCHEDULING_PANEL: Got MS");
            msCounter++;
        } catch (Exception e) {
            //logger.info("SCHEDULING_PANEL: failed to get MS reference, "+e.toString());
            //e.printStackTrace();
            masterScheduler = null;
        }
        createRightClickMenu();
    }
    private void releaseMSRef(){
        if(masterScheduler != null){
            container.releaseComponent(masterScheduler.name());
            msCounter--;
            if(msCounter == 0) {
                masterScheduler = null;
            }
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
        allSched= new JLabel(""+allSchedInt);
        p.add(allSched);
        //2
        p.add(new JLabel("Dynamic Schedulers "));
        dynamicSched = new JLabel(""+dynamicSchedInt);
        p.add(dynamicSched);
        //3
        p.add(new JLabel("Queued Schedulers "));
        queuedSched = new JLabel(""+queuedSchedInt);
        p.add(queuedSched);
        //4
        p.add(new JLabel("Interactive Schedulers "));
        interactiveSched = new JLabel(""+interactiveSchedInt);
        p.add(interactiveSched);        
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
                String arrayname = ShowArrayFrame.showArraySelectFrame(container,true, "queued");
                if(arrayname.equals("") || arrayname == null) {
                    return;
                }
                logger.info("Selected array = "+arrayname);
                addTab("QS - "+arrayname,createQueuedSchedulingView(arrayname)); 
                queuedSchedInt++;
                queuedSched.setText(""+queuedSchedInt);
                allSchedInt++;
                allSched.setText(""+allSchedInt);
                repaint();
                validate();
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
                String arrayname = ShowArrayFrame.showArraySelectFrame(container,true, "interactive");
                if(arrayname.equals("") || arrayname == null) {
                    return;
                }
                logger.info("Selected array = "+arrayname);
                try {
                    getMSRef();
                    String is = masterScheduler.startInteractiveScheduling1(
                        arrayname);
                    addTab("IS - "+arrayname,
                        createInteractiveSchedulingView(is, arrayname)); 
                    releaseMSRef();
                    //System.out.println("added IS, tab count ="+getTabCount());
                } catch(Exception e){
                    e.printStackTrace();
                }
                interactiveSchedInt++;
                interactiveSched.setText(""+interactiveSchedInt);
                allSchedInt++;
                allSched.setText(""+allSchedInt);
                repaint();
                validate();
            }
        });
        p2.add(b)import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
;
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
                int result = pickTheirOwnArray();
                if(result == 0 ) {
                //bring up array selector!
                //    arrayname = ShowArrayFrame.showArraySelectFrame(container,true, "dynamic");
                    //here means when arrayname == "" user has canceled manual \
                    //array creation.. cannot continue from this point so exits
                //    if(arrayname.equals("")){
                //        showErrorMessage(
                 //           "Cannot create Dynamic Scheduler without an array!");
                //        return;
               //     }
                  //  addTab("DS", createDynamicSchedulingView(arrayname));
                //TODO replace these 2 lines with the above stuff when we let them pick the array
                    arrayname = "";
                    addTab("DS", createDynamicSchedulingView(arrayname));
                }else if(result ==1){
                    //here with arrayname == to "" means scheduler will create array
                    arrayname = "";
                    addTab("DS", createDynamicSchedulingView(arrayname));
                } else if(result== -1){
                    logger.info("SCHEDULING_PANEL: Chosen to cancel DS");
                 import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
   return;
                }
                dynamicSchedInt++;
                dynamicSched.setText(""+dynamicSchedInt);
                allSchedInt++;
                allSched.setText(""+allSchedInt);
                repaint();
                validate();
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

    private int pickTheirOwnArray() {
        int answer = JOptionPane.showConfirmDialog(this, 
                "Right now you don't get to pick your array for DS","Array Needed?",
                //"Do you need to create your own array?", "Array Needed?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        // TODO: Uncomment this when they do get to pick an array for DS.
        //if(answer == JOptionPane.YES_OPTION){
        //    return true;
        //} else {
        //    return false;
        //}
        //
        return answer;
    }

    private void createRightClickMenu(){
        rightClickMenu = new JPopupMenu("Master Scheduler Functions");
        JMenuItem menuItem;
        if(masterScheduler!=null){
            logger.info("creating menu with non-null ms ref");
        
        //   TODO add back in when it works inside the OMC
        //    menuItem = new JMenuItem("Detach");
         //   menuItem.addActionListener(new ActionListener() {
        //        public void actionPerformed(ActionEvent event){
        //            detachTab(getSelectedIndex());
         //       }
         //   });
         //   rightClickMenu.add(menuItem);
            
            menuItem = new JMenuItem("Get Array Info");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    ShowArrayFrame f = new ShowArrayFrame(container, false);
                    f.setVisible(true);
                }
            });
            rightClickMenu.add(menuItem);
            menuItem = new JMenuItem("Disconnect Master Scheduler");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    releaseMSRef();
                }
            });
            rightClickMenu.add(menuItem);
        } else {
            menuItem = new JMenuItem("Connect To MasterScheduler");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    getMSRef();
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
      *
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
			public void windowClosed(WindowEvent event) {
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
		 *
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
        //logger.info("in close tab");
        SchedulerTab tab = (SchedulerTab)getComponentAt(i);
        tab.exit();
        removeRowFromSchedulerTable(tab);
        remove(i);
        String className=tab.getClass().getName();
        //System.out.println(className);
        if(className.contains("InteractiveSchedTab")){
            //System.out.println("Closed interactive tab");
            interactiveSchedInt--;
            interactiveSched.setText(""+interactiveSchedInt);
        }else if (className.contains("DynamicSchedTab")) {
            //System.out.println("Closed dynamic tab");
            dynamicSchedInt--;
            dynamicSched.setText(""+dynamicSchedInt);
        }else if (className.contains("QueuedSchedTab")) {
            //System.out.println("Closed queued tab");
            queuedSchedInt--;
            queuedSched.setText(""+queuedSchedInt);
        }
        allSchedInt--;
        allSched.setText(""+allSchedInt);
        repaint();
        validate();
    }

    public void exit() {
        //release all components.
        logger.info("SCHEDULING_PANEL: in exit of MainSchedulingTab");
        //make sure there are no open dialogs/frames/etc..
        
        //try {
         //   container.releaseComponent(masterScheduler.name());
        //} catch(Exception e){
        //    logger.warning("SCHEUDLING_PANEL: Error releasing components");
        //}
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
*/
