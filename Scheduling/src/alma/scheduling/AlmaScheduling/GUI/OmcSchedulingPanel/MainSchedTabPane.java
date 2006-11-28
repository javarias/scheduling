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
        controller = new MainSchedTabPaneController (this);
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

    public void setDefaults(){
        doInteractiveButton();
        //select antenna 1 already..
        middlePanel.selectDefaultAntenna();
    }

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
    private void doInteractiveButton() {
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
        middlePanel.prepareCreateArray("interactive");
    }

    private void doQueuedButton() {
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
       // middlePanel.setArrayMode("queued");
        middlePanel.prepareCreateArray("queued");
        //createArray with mode 'queued'
    }

    private void doDynamicButton(){ 
        createArrayEnabled = true;
        middlePanel.setEnabled(true);
        middlePanel.prepareCreateArray("dynamic");
        //createArray with mode 'dynamic'
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
                        doInteractiveButton();
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
                        doQueuedButton();
                    } else {
                        showConnectMessage();
                        return;
                    }
                }
        });               
        dynamicB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(areWeConnected()){
                        doDynamicButton();
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
        String title="";
        if(mode.equals("interactive")){
            tab = new InteractiveSchedTab(container, array);
            allSchedulers.add(tab);
            title = array +"(Interactive)";
            addTab(title, (JPanel)tab);
        } else if (mode.equals("queued")){
            title = array +"(Queued)";
            tab = new QueuedSchedTab(container, title, array);
            allSchedulers.add(tab);
            addTab(title, (JPanel)tab);
        } else if (mode.equals("dynamic")){
            title = array +"(Dynamic)";
            //tab = new DynamicSchedTab(container, title, array);
            //allSchedulers.add(tab);
            //addTab(title, tab);
        }
        int i = indexOfTab(title);
        setSelectedIndex(i);
    }

    private void showConnectMessage(){
        JOptionPane.showMessageDialog(this,"System not operational yet.",
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
        setSelectedIndex(0); //default to main tab when something is closed
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
