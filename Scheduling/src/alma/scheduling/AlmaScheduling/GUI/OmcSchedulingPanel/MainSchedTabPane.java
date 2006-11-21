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
    //private MainSchedTabPaneController controller;
    //private JPopupMenu rightClickMenu;
    //private boolean connectedToALMA = false;
    private boolean createArrayEnabled = false;
    private Logger logger;
    private int overTabIndex;
    private JPanel mainPanel;
    private JPanel topPanel;
    private SearchArchiveOnlyTab archiveTab;
    private CreateArrayPanel middlePanel;
    private JPanel showAntennaPanel;
    private JPanel middleButtonPanel;
    private Vector<SchedulerTab> allSchedulers;
    private JButton interactiveB;
    private JButton queuedB;
    private JButton dynamicB;
    private MainSchedTabPaneController controller;

    /**
      * Constructor
      */
    public MainSchedTabPane(){
        super(JTabbedPane.TOP);
        setup();
        //createRightClickMenu();   
        //super.addMouseListener(new PopupListener());
        Dimension d = getPreferredSize();
        setMaximumSize(d);
        controller = new MainSchedTabPaneController ();
    }
        
    public void setup() {
        allSchedulers = new Vector<SchedulerTab>();
        createMainTab();
        createSearchArchiveOnlyTab();
        addTab("Main",mainPanel);
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
        controller.setup(cs);
        logger = cs.getLogger();
        archiveTab.connectedSetup(cs);
        middlePanel.connectedSetup(cs);
        logger.info("SCHEDULING_PANEL: Second setup, connected to manager");
    }

    /*
    private void createRightClickMenu(){
        rightClickMenu = new JPopupMenu("Master Scheduler Functions");
        JMenuItem menuItem;
        if(connectedToALMA) {
            //don't need anything...
            menuItem = new JMenuItem("disconnect");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    connectedToALMA = false;
                    archiveTab.connectToALMA(false);
                    createRightClickMenu();
                }
            });

            rightClickMenu.add(menuItem);
        } else {
            menuItem = new JMenuItem("Connect");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    connectedToALMA = true;
                    archiveTab.connectToALMA(true);
                    createRightClickMenu();
                }
            });

            rightClickMenu.add(menuItem);
        }
    }
    */

    public void createSearchArchiveOnlyTab() {
        archiveTab = new SearchArchiveOnlyTab();
    }
    public void createMainTab(){ 
        mainPanel = new JPanel(new BorderLayout());
        try {
            createTopPanel(); //buttons
            createMiddlePanel(); //createArrayPanel
            mainPanel.add(topPanel, BorderLayout.NORTH);
            mainPanel.add(middlePanel, BorderLayout.CENTER);
        }catch(Exception e){
            e.printStackTrace();
        }
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
                    if(areWeConnected()){
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
                    if(areWeConnected()){
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
                    if(areWeConnected()){
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
        middlePanel.setOwner(this);
        middlePanel.setEnabled(false);
    }

    private void updateAntennaView(){
    }

    public void openSchedulerTab(String mode, String array) {
        SchedulerTab tab;
        String name;
        if(mode.equals("interactive")){
            //name = "IS_"+array;
            tab = new InteractiveSchedTab(container, array);
            allSchedulers.add(tab);
            addTab(array +" (Interactive)", (JPanel)tab);
        } else if (mode.equals("queued")){
            name="QS_"+array;
            //tab = new QueuedSchedTab(container, name, array);
            //allSchedulers.add(tab);
            //addTab(name, tab);
        } else if (mode.equals("dynamic")){
            name="DS_"+array;
            //tab = new DynamicSchedTab(container, name, array);
            //allSchedulers.add(tab);
            //addTab(name, tab);
        }
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
        //remove it from all schedulers list
        int x = getSchedulerPosition(tab);
        if(x!=-1){
            allSchedulers.removeElementAt(x);
        }
    }

    private int getSchedulerPosition(SchedulerTab tab){
        for(int i=0; i< allSchedulers.size(); i++){
            if(allSchedulers.elementAt(i).getArrayName().equals(tab.getArrayName()) &&
               allSchedulers.elementAt(i).getSchedulerName().equals(tab.getSchedulerName()) &&
               allSchedulers.elementAt(i).getSchedulerType().equals(tab.getSchedulerType())){
                  return i;
            }
        }
        return -1;
    }

    private boolean areWeConnected(){
        return controller.areWeConnected();
    }
    public void exit(){
    }

    /*
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
}
