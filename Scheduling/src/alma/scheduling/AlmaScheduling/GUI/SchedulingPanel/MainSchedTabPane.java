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

import alma.acs.container.ContainerServices;
//import javax.swing.plaf.basic.BasicTabbedPaneUI;
//import com.sun.java.swing.plaf.windows.WindowsIconFactory;

public class MainSchedTabPane extends JTabbedPane {
    private Logger logger;
    private int schedulerCount=0;
    private TableModel schedulerTableModel;
    private JTable schedulerTable;
    private JPopupMenu rightClickMenu;
    private Object[][] schedRowInfo;
    private ContainerServices container;
    private MasterSchedulerIF masterScheduler;
    private int overTabIndex;
    private JDesktopPane desktop;
    private Vector<Window> openWindows;

    public MainSchedTabPane(ContainerServices cs){
        super(JTabbedPane.TOP);
        //getParent().setContentPane(desktop);
        container = cs;
        try {
            logger = cs.getLogger();
        } catch(Exception e) {
            logger = Logger.getLogger("OfflineMode");
        }
        getMasterSchedulerRef();
        addTab("Main",createMainView());
        super.setUI(new SchedTabUI());
        addCloseTabListener(new CloseTabListener(){
            public void closeOperation(MouseEvent e) {
                remove(overTabIndex);
            }
        });
        openWindows = new Vector<Window>();
        //showCreateArrayPopup();
    }

    private void getMasterSchedulerRef(){
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            logger.info("SCHEDULING_PANEL: Got MS");
        } catch (Exception e) {
            //logger.severe("SCHEDULING_PANEL: Error getting MS");
            masterScheduler = null;
            //e.printStackTrace();
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
        //p.setSize( new Dimension(200, 75));
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
        final String[] schedColumnInfo = {"Name", "ArrayName","Type","Status"};
        schedRowInfo = new Object[1][schedColumnInfo.length];
        schedulerTableModel = new AbstractTableModel() {
            public int getColumnCount() { return schedColumnInfo.length; }
            public String getColumnName(int column) { return schedColumnInfo[column]; }
            public int getRowCount() { return schedRowInfo.length;     }
            public Object getValueAt(int row, int col) { return schedRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { schedRowInfo[row][col]= val; }
        };
        schedulerTable = new JTable(schedulerTableModel);
        Dimension d = new Dimension(200, 75);
        schedulerTable.setPreferredScrollableViewportSize(d);
    }

    private JPanel bottomSection(){
        JPanel p = new JPanel(new GridLayout(4,2));
        p.setBorder(new TitledBorder("Scheduler Details"));
        p.add(new JLabel("Detail 1"));
        p.add(new JLabel());
        p.add(new JLabel("Detail 2"));
        p.add(new JLabel());
        p.add(new JLabel("Detail 3"));
        p.add(new JLabel());
        p.add(new JLabel("Detail 4"));
        p.add(new JLabel());
        return p;
    }
    private void createRightClickMenu(){
        rightClickMenu = new JPopupMenu("Master Scheduler Functions");
        JMenuItem menuItem;
        if(masterScheduler!=null){
            menuItem = new JMenuItem("Get brief SB info");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                // call masterScheduler.getSBLites();
                }
            });
            rightClickMenu.add(menuItem);
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
                }
            });
            rightClickMenu.add(menuItem);
            menuItem = new JMenuItem("Create an Array");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    showCreateArrayPopup();
                }
            });
            rightClickMenu.add(menuItem);
            menuItem = new JMenuItem("Create Queued Scheduler");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    //createQueuedScheduler();
                    //prompt to create array
                    // use array name in tab title
                    String arrayname ="Array 1";
                    addTab("QS - "+arrayname,createQueuedSchedulingView()); 
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
                    getMasterSchedulerRef();
                }
            });
            rightClickMenu.add(menuItem);
            menuItem = new JMenuItem("Create an Array");
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    showCreateArrayPopup();
                }
            });
            rightClickMenu.add(menuItem);
        }
        addMouseListener(new PopupListener());
    }
    

    public void addScheduler(String name) {
        schedulerCount++;
        addTab("Scheduler."+schedulerCount,
                createInteractiveSchedulingView());
    }
    private JScrollPane createInteractiveSchedulingView() {
        return new InteractiveSchedTab();
    }
    
    //first create a queued scheduler tab
    //then query for SBs(sblites)
    private JScrollPane createQueuedSchedulingView(){
        logger.info("SCHEDULING_PANEL: Start Queued Scheduling ");
        return new QueuedSchedTab(container);
    }

    private void showCreateArrayPopup() {
        CreateArrayFrame f = new CreateArrayFrame(container);
        f.setVisible(true);
   //     System.out.println(getParent().getParent().getParent().getClass().getName());
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
        System.out.println(index);
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
        EventListener close[] = getListeners(CloseTabListener.class);
        overTabIndex = tabIndex;
        for(int i=0; i< close.length; i++){
            ((CloseTabListener)close[i]).closeOperation(e);
        }
    }

    public void exit() {
        //release all components.
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
