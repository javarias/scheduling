package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
//acs stuff & other alma stuff
import alma.acs.container.ContainerServices;
import alma.acs.nc.Consumer;
import alma.acs.entityutil.EntityDeserializer;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.xmlstore.Operational;
import alma.xmlstore.Identifier;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionPackage.*;
import alma.xmlstore.OperationalPackage.*;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.CursorPackage.QueryResult;
import alma.xmlentity.XmlEntityStruct;
//scheduling stuff
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.AlmaScheduling.ALMASchedulingUtility;
import alma.scheduling.GUI.InteractiveSchedGUI.OpenOT;
//

public class InteractiveSchedTab extends JScrollPane implements SchedulerTab {
    private ArchiveConnection archiveConnection;
    private Operational archive;
    private EntityDeserializer entityDeserializer;
    private String arrayname;
    private String schedulername;
    private String type;
    private String currentProjectId;
    private ProjectLite currentProjectLite;
    private SBLite[] currentProjectSBs;
    private ContainerServices container;
    private Interactive_PI_to_Scheduling scheduler;
    private MasterSchedulerIF masterScheduler;
    private Logger logger;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel middlePanel;
    private JPanel tableDisplayPanel;
    private JPanel selectionDetailPanel;
    private JPanel buttonPanel;
    private JTextField projectQueryTF;
    private JTextField piQueryTF;
    private Object[][] projRowInfo;
    private JTable projTable;
    private TableModel projTableModel;
    private ProjectLite[] projects;
    private Object[][] sbRowInfo;
    private JTable sbTable;
    private TableModel sbTableModel;
    private Consumer consumer = null;
    private Vector<String> allSessionUIDs;
    
    public InteractiveSchedTab(ContainerServices cs, String s, String an){
        mainPanel = new JPanel(new BorderLayout());//new GridLayout(4,1));
        getViewport().add(mainPanel);
        container = cs;
        logger = container.getLogger();
        arrayname = an;
        type = "interactive";
        schedulername = s;
        //allSessionUIDs = new Vector<String>();
        setToNull();
        getComponentRefs();
        logger.info("Interactive Sched Tab created");
        createSearchView();
        try{
            consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,container);
            consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            consumer.consumerReady();
        } catch (Exception e){
            e.printStackTrace();
        }
        entityDeserializer = EntityDeserializer.getEntityDeserializer(container.getLogger());
    }
    
    private void setToNull(){
        currentProjectId = null;
        currentProjectLite = null;
        currentProjectSBs = null;
        allSessionUIDs = new Vector<String>();
        sbRowInfo = null;
        sbTable = null;
        projRowInfo = null;
        projTable = null;
    }
    
    private void getComponentRefs() {
        try {
            masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            scheduler = alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                    container.getComponent(schedulername));
        }catch(Exception e){
            e.printStackTrace();
        }
        logger.info("Got interactive scheduler reference");
    }

    private void createSearchView() {
        try {
            mainPanel.removeAll();
            middlePanel.removeAll();
            currentProjectId = null;
            currentProjectLite = null;
        } catch(Exception e){/* don't care if it complains*/}
        topPanel = new JPanel(); 
        topPanel.setBorder(new TitledBorder("Search"));
        tableDisplayPanel = new JPanel();
        tableDisplayPanel.setBorder(new TitledBorder("Projects"));
        selectionDetailPanel = new JPanel();
        selectionDetailPanel.setBorder(new TitledBorder("Project Details"));
        buttonPanel = new JPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        middlePanel = new JPanel(new GridLayout(2,1));
        middlePanel.add(tableDisplayPanel);
        middlePanel.add(selectionDetailPanel);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        //mainPanel.add(tableDisplayPanel);
        //mainPanel.add(selectionDetailPanel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        topPanel.add(initialTopPanel());//, BorderLayout.NORTH);
        validate();
    }
    private JPanel initialTopPanel() {
        JPanel search = new JPanel(new GridLayout(2,1));
        JPanel p1 = new JPanel(new GridLayout(2,2));
        projectQueryTF = new JTextField();
        piQueryTF = new JTextField();
        p1.add(new JLabel("PI Name: ")); 
        p1.add(piQueryTF);
        p1.add(new JLabel("Project Name: ")); 
        p1.add(projectQueryTF);
        ////
        //JPanel p2 = new JPanel(new FlowLayout());
        JPanel p2 = new JPanel(new GridLayout(2,2));
        JButton b = new JButton("Search");
        b.setToolTipText("Search for project given the entered search criteria");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String projectStr =projectQueryTF.getText(); 
                    String piStr =piQueryTF.getText();
                    if(projectStr.equals("")){ projectStr = "*"; } 
                    if(piStr.equals("")){ piStr ="*"; }
                    String[] res = masterScheduler.queryForProject(projectStr,piStr);
                    displayProjectSearchResults(res);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        p2.add(b);
        b = new JButton("Clear");
        b.setToolTipText("Clear text in search fields");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
          //      clearTFs();
            }
        });
        p2.add(b);
        b = new JButton("Select");
        b.setToolTipText(
          "Selects the highlighted item in the table as the project to login to.");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginToInteractiveScheduling();
            }
        });
        p2.add(b);
        b = new JButton("Exit");
        b.setToolTipText("Not yet implemented. Just close the tab for now.");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               // exit();
            //  and close tab somehow.
            }
        });
        p2.add(b);
        ////
        search.add(p1,BorderLayout.CENTER);
        search.add(p2,BorderLayout.SOUTH);
        return search;
    }

    private void loginToInteractiveScheduling() {
        if(!checkSomethingSelected()){
            return;
        }
        if(!checkOneSelected()){
            return;
        }
        //remove everything
        //set default project id
        int row = projTable.getSelectedRow();
        currentProjectId = (String)projRowInfo[row][3];
        addUIDToSession(currentProjectId);
        currentProjectLite = getProjectLiteForId(currentProjectId); 
        //create interactive scheduling view
        createInteractiveSchedulingView();
        loginToInteractiveScheduler();
    }

    private void createInteractiveSchedulingView(){
        try {
            mainPanel.removeAll();
            middlePanel.removeAll();
        } catch(Exception e) {/*dont' care if it complains */}
        JPanel info = new JPanel(new GridLayout(4,1));
        topPanel = new JPanel();
        topPanel.setBorder(new TitledBorder("Project Information"));
        info.add(new JLabel("Project Name: "+currentProjectLite.projectName));
        info.add(new JLabel("PI: "+currentProjectLite.piName));
        info.add(new JLabel("Time Created: "+currentProjectLite.creationTime));
        info.add(new JLabel("Project's Status: "+currentProjectLite.status));
        topPanel.add(info);
        tableDisplayPanel = new JPanel();
        tableDisplayPanel.setBorder(new TitledBorder("Project's SBs"));
        displayProjectSBs();
        selectionDetailPanel = new JPanel();
        selectionDetailPanel.setBorder(new TitledBorder("SB Details"));
        buttonPanel.add(sessionButtonView());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        //middlePanel = new JPanel(new GridLayout(2,1));
        middlePanel.add(tableDisplayPanel);
        middlePanel.add(selectionDetailPanel);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
       // mainPanel.add(tableDisplayPanel);
        //mainPanel.add(selectionDetailPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        validate();
    }

    private void loginToInteractiveScheduler() {
        try {
            scheduler.startSession(currentProjectLite.piName, currentProjectId);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void displayProjectSearchResults(String[] r){
        projRowInfo = new Object[r.length][4];
        try {
            projects = masterScheduler.getProjectLites(r);//scheduler.getProjectDisplayInfo(r);
            for(int i=0; i < r.length; i++){
                projRowInfo[i][0] = projects[i].projectName;
                projRowInfo[i][1] = projects[i].piName;
                projRowInfo[i][2] = projects[i].version;
                projRowInfo[i][3] = projects[i].uid;
            }
        } catch(Exception e){
                projRowInfo[0][0] = "N/A";
                projRowInfo[0][1] = "N/A";
                projRowInfo[0][2] = "N/A";
                projRowInfo[0][3] = "N/A";
        }
        final String[] projColumnInfo = {"Project Name", "PI", "Version"};
        projTableModel = new AbstractTableModel() {
            public int getColumnCount() { return projColumnInfo.length; }
            public String getColumnName(int column) { return projColumnInfo[column]; }
            public int getRowCount() { return projRowInfo.length; }
            public Object getValueAt(int row, int col) { return projRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { projRowInfo[row][col] = val; }
        };
        projTable = new JTable(projTableModel);
        projTable.setPreferredScrollableViewportSize(new Dimension(200,100));
        projTable.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showProjectInfo();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
        JScrollPane projListPane = new JScrollPane(projTable);
        projTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JPanel p = new JPanel();
        p.add(projListPane);
        try {
            tableDisplayPanel.removeAll();
        } catch(Exception e) {/*don't care if it complains*/}
        tableDisplayPanel.add(p);
        validate();
    }

    private void displayProjectSBs() {
        final String[] sbColumnInfo = { "SB Name", "Priority", "Freq."};
        sbRowInfo = new Object[currentProjectLite.allSBIds.length][4];
        currentProjectSBs= new SBLite[1]; 
        try {
            currentProjectSBs = masterScheduler.getSBLite(currentProjectLite.allSBIds); 
        } catch (Exception e) {
            currentProjectSBs[0] = new SBLite();
            currentProjectSBs[0].schedBlockRef="N/A";
            currentProjectSBs[0].projectRef="N/A";
            currentProjectSBs[0].obsUnitsetRef="N/A";
            currentProjectSBs[0].sbName="N/A";
            currentProjectSBs[0].projectName="N/A";
            currentProjectSBs[0].PI="N/A";
            currentProjectSBs[0].priority="N/A";
            currentProjectSBs[0].ra=0.0;
            currentProjectSBs[0].dec=0.0;
            currentProjectSBs[0].freq=0.0;
            currentProjectSBs[0].maxTime=0;
            currentProjectSBs[0].score=0.0;
            currentProjectSBs[0].success=0.0;
            currentProjectSBs[0].rank=0.0;
        }
        for(int i=0; i < currentProjectSBs.length; i++){
            sbRowInfo[i][0] = currentProjectSBs[i].sbName;
            sbRowInfo[i][1] = currentProjectSBs[i].priority;
            sbRowInfo[i][2] = String.valueOf(currentProjectSBs[i].freq);
            sbRowInfo[i][3] = currentProjectSBs[i].schedBlockRef;
            addUIDToSession((String)sbRowInfo[i][3]);
        }
        sbTableModel = new AbstractTableModel() {
            public int getColumnCount() { return sbColumnInfo.length; }
            public String getColumnName(int column) { return sbColumnInfo[column]; }
            public int getRowCount() { return sbRowInfo.length; }
            public Object getValueAt(int row, int col) { return sbRowInfo[row][col]; }
            public void setValueAt(Object val, int row, int col) { sbRowInfo[row][col] = val; }
        };
        sbTable = new JTable(sbTableModel);
        sbTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sbTable.setPreferredScrollableViewportSize(new Dimension(200,100));
        sbTable.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                showSBInfo();
            }
            public void mouseEntered(MouseEvent e){ }
            public void mouseExited(MouseEvent e){ }
            public void mousePressed(MouseEvent e){ }
            public void mouseReleased(MouseEvent e){}
        });
        JScrollPane sbListPane = new JScrollPane(sbTable);
        JPanel p = new JPanel();
        p.add(sbListPane);
        try {
            tableDisplayPanel.removeAll();
        } catch(Exception e) {/*don't care if it complains*/}
        tableDisplayPanel.add(p);
        validate();
            
    }

    private ProjectLite getProjectLiteForId(String id){
        for(int i=0; i< projects.length; i++){
            if(projects[i].uid.equals(id)){
                return projects[i];
            }
        }
        return null;
    }
    private SBLite getSBLiteForId(String id){
        if(currentProjectSBs == null){
            return null;
        }
        for(int i=0; i < currentProjectSBs.length; i++){
            if(currentProjectSBs[i].schedBlockRef.equals(id)){
                return currentProjectSBs[i];
            }
            
        }
        return null;
    }

    private boolean hasSearchBeenDone(){
        if(projTable == null) {
            return false;
        }
        return true;
    }
    private boolean checkSomethingSelected() {
        if(!hasSearchBeenDone()){
            JOptionPane.showMessageDialog(this, 
                "Do a search first!",
                "Nothing Searched", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int[] rows = projTable.getSelectedRows();
        if(rows.length ==0 ) {
            JOptionPane.showMessageDialog(this, 
                "Select one!",
                "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    private boolean checkOneSelected(){
        if(!hasSearchBeenDone()){
            JOptionPane.showMessageDialog(this, 
                "Do a search first!",
                "Nothing Searched", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        int[] rows = projTable.getSelectedRows();
        if(rows.length > 1) {
            JOptionPane.showMessageDialog(this, 
                "Select only one.",
                "Too Many Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void showProjectInfo() {
        try {
            selectionDetailPanel.removeAll();
        } catch(Exception e) { /*dont' care if it complains */ }
        if(!checkSomethingSelected()){
            return;
        }
        if(!checkOneSelected()){
            return;
        }
        int row = projTable.getSelectedRow();
        String id = (String)projRowInfo[row][3];
        JTextArea info = new JTextArea();
        try {
            ProjectLite p = getProjectLiteForId(id);
            info.append("Project Name = "+p.projectName +"\n");
            info.append("PI Name = "+p.piName+"\n");
            info.append("Time of creation = "+p.creationTime +"\n");
            info.append("\n");
            info.append("Status = "+p.status +"\n");  
            info.append("\n");
            info.append("Total number of SBs = "+p.totalSBs +"\n");  
            info.append("Total number of SBs completed = "+p.completeSBs +"\n");  
            info.append("Total number of SBs failed = "+p.failedSBs +"\n");  

        } catch(Exception e) {
            info.append(e.toString());
        }
        JScrollPane pane = new JScrollPane(info);
        selectionDetailPanel.add(pane);
        validate();
    }

    private void showSBInfo(){
        try {
            selectionDetailPanel.removeAll();
        } catch(Exception e) { /*dont' care if it complains */ }
        if(!checkSomethingSelected()){
            return;
        }
        if(!checkOneSelected()){
            return;
        }
        int row = sbTable.getSelectedRow();
        String id =(String)sbRowInfo[row][3];
        JTextArea info = new JTextArea();
        SBLite sb = getSBLiteForId(id);
        try{
            info.append("SB Name: "+sb.sbName+"\n");
            info.append("Priority: "+sb.priority+"\n");
            info.append("RA: "+sb.ra+"\n");
            info.append("DEC: "+sb.dec+"\n");
            info.append("Frequency: "+sb.freq+"\n");
            info.append("Score: "+sb.score+"\n");
            info.append("Success: "+sb.success+"\n");
            info.append("Rank: "+sb.rank+"\n");
        } catch(Exception e){
            info.append(e.toString()+"\n");
        }
        JScrollPane pane = new JScrollPane(info);
        selectionDetailPanel.add(pane);
        validate();
    }
    
    private JPanel sessionButtonView() {
        try {
            buttonPanel.removeAll();
        } catch(Exception e) {/*dont' care if it complains */}
        JPanel p = new JPanel(new GridLayout(2,1));
        JPanel p1 = new JPanel(new GridLayout(2,2));
        JButton b = new JButton("Execute");
        b.setToolTipText("Sends the highlighted SB to be executed.");
        b.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    executeSB();
                }
        });
        p1.add(b);
        b = new JButton("Stop");
        b.setToolTipText("Tells the Control system to stop executing this SB.");
        b.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    stopSB();
                }
        });
        p1.add(b);
        b = new JButton("Modify");
        b.setToolTipText("Not implemented yet");
        b.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    modify();
                }
        });
        p1.add(b);
        b = new JButton("Remove");
        b.setToolTipText("Not implemented yet");
        b.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    remove();
                }
        });
        p1.add(b);
        JPanel p2 = new JPanel();
        b = new JButton("Search Again");
        b.setToolTipText("Logs out of this project and returns to search screen.");
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    logoutOfProject();
                    createSearchView();
                }
        });
        p2.add(b);
        p.add(p1);
        p.add(p2);
        return p;
    }

    private void executeSB() {
        //do we have only one selected?
        if(!isSBTableSelectStatusGood()){
            return;
        }
        //get selected sb
        int row = sbTable.getSelectedRow();
        String sbId =(String) sbRowInfo[row][3];
        //send sb to scheduler comp to be executed
        try {
            scheduler.executeSB(sbId);
        }catch(Exception e){} 
    }
    private boolean isSBTableSelectStatusGood() {
        int[] rows = sbTable.getSelectedRows();
        if(rows.length != 1){
            JOptionPane.showMessageDialog(this, 
                "Select one SB!",
                "Bad Selection", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void stopSB(){
        try {
            scheduler.stopSB();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void modify() {
        try {
            OpenOT ot = new OpenOT(currentProjectId,container);
            Thread t = container.getThreadFactory().newThread(ot);
            t.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSBRowInfo(){
        System.out.println("updating SB Row info and table"); 
        for(int i=0; i < currentProjectSBs.length; i++){
            System.out.println((String)sbRowInfo[i][0]);
            sbRowInfo[i][0] = currentProjectSBs[i].sbName;
            System.out.println(currentProjectSBs[i].sbName);
            sbRowInfo[i][1] = currentProjectSBs[i].priority;
            sbRowInfo[i][2] = String.valueOf(currentProjectSBs[i].freq);
            sbRowInfo[i][3] = currentProjectSBs[i].schedBlockRef;
        }
        sbTable.validate();
        sbTable.repaint();
        tableDisplayPanel.validate();
        validate();
    }

    private void remove(){
    }

    private void logoutOfProject(){
        setToNull();
        try {
            scheduler.endSession();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

//////////////////////////////////////

//SchedulerTab interface implementation
    public String getSchedulerName(){
        return schedulername;
    }
    
    public String getArrayName(){
        return arrayname;
    }
    
    public String getSchedulerType(){
        return type;
    }

    public void exit() {
        try{
            logger.info("About to release "+scheduler.name());
            container.releaseComponent(scheduler.name());
            container.releaseComponent(masterScheduler.name());
            consumer.disconnect();
        } catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Exiting Interactive Scheduler on array "+arrayname);
    }

///////////////////////////////////////     
    private void connectToArchive() throws SchedulingException {
        try{
            archiveConnection = alma.xmlstore.ArchiveConnectionHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/xmlstore/ArchiveConnection:1.0"));
            archiveConnection.getAdministrative("SCHEDULING_PANEL").init();
            archive = archiveConnection.getOperational("SCHEDULING_PANEL");
            logger.info("SCHEDULING_PANEL: connected to archive");
        } catch(Exception e){
            logger.severe("SCHEDULING_PANEL: error connecting to archive, "+e.toString());
            throw new SchedulingException(e);
        }
    }
    private void disconnectFromArchive() throws SchedulingException {
        try{
       //     container.releaseComponent(archive.name());
            container.releaseComponent(archiveConnection.name());
            logger.info("SCHEDULING_PANEL: released archive components");
        }catch (Exception e){
            logger.severe("SCHEDULING_PANEL: error releasing archive comps.");
            throw new SchedulingException(e);
        }
        
    }
///////////////////////////////////////     

    public void receive(XmlStoreNotificationEvent event) {
        logger.info("SCHEDULING_PANEL: Got XML Store event");
        CheckArchiveEvent processor = new CheckArchiveEvent(event);
        Thread t = new Thread(processor);
        t.start();
    }

    private void addUIDToSession(String uid) {
        //if(isUidPartOfThisSession(uid)){
          //  return;
        //}
        allSessionUIDs.add(uid);
    }
    private void updateExistingEntity(String uid) {
        int size = allSessionUIDs.size();
        for(int i=0; i < size; i++){
            logger.info("in vector, uid = "+allSessionUIDs.elementAt(i));
            logger.info("comparing it to "+uid);
            if(allSessionUIDs.elementAt(i).equals(uid)){
                System.out.println("EQUAL");
                try{
                    //already in the list so update whole list and validate
                    currentProjectSBs = masterScheduler.getSBLite(currentProjectLite.allSBIds); 
                 //   Thread.sleep(5);
                    updateSBRowInfo();
                    return;
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void addNewEntity(String uid) {
        try {
            XmlEntityStruct xml = archive.retrieveDirty(uid);
            if(xml.entityTypeName.equals("SchedBlock")){
                SchedBlock sb = (SchedBlock)entityDeserializer.deserializeEntity(xml,SchedBlock.class);
                String projectID = sb.getObsProjectRef().getEntityId();
                if(projectID.equals(currentProjectId)){
                    String[] ids = {uid};
                    SBLite[] newSB = masterScheduler.getSBLite( ids );
                    int oldLen = currentProjectSBs.length;
                    int newLen = oldLen +1;
                    SBLite[] addOneMoreSBLite = new SBLite[newLen];
                    addOneMoreSBLite = currentProjectSBs;
                    addOneMoreSBLite[oldLen] = new SBLite();// 0 indexed arrays so (newLen - 1) == oldLen
                    addOneMoreSBLite[oldLen] = newSB[0]; //only asked for one so will get only one.
                    currentProjectSBs = addOneMoreSBLite;
                    validate();
                    logger.info("SCHEDULING_PANEL: adding new SB");
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
        //need to connect to the archive now and get the entity 
        //so that we can see what class it is

        //if it is an SB, check to see if the projectRef's id is the current projects
        //if it is a project, its most likely not our current one or the is it 
        //part of the session method would have seen it,
        
        //if its the proposal then it shouldne't be updated..
        // if its the project status we can update views
   
    public void processXmlStoreNotificationEvent(XmlStoreNotificationEvent e){
        String uid = e.uid;
        //check to see if the uid being stored/updated in archive is something
        //that we care about. if its not, ignore it!
        //if(isUidPartOfThisSession(uid)) {
        //    return;
        //}
        logger.info("uid = "+uid);
        alma.xmlstore.operationType type = e.operation;
        if(type == alma.xmlstore.operationType.STORED_XML){
            logger.info("operation = store");
            //check here to see if what is stored has our project's reference
            logger.info("SCHEDULING_PANEL: got store for entity "+uid);
            addNewEntity(uid);
        } else if(type == alma.xmlstore.operationType.UPDATED_XML){
            logger.info("operation = update");
            //check here to see if what is updated belongs in our queue of sbs 
            //or is our project/ps's uids
            logger.info("SCHEDULING_PANEL: got update for entity "+uid);
            updateExistingEntity(uid);
        } else if(type == alma.xmlstore.operationType.DELETED_XML){
            logger.info("operation = delete");
            //shouldn't happen but check
        }
    }

        
    class CheckArchiveEvent implements Runnable {
        private XmlStoreNotificationEvent event;
        
        public CheckArchiveEvent(XmlStoreNotificationEvent e) {
            event = e;
        }
        public void run(){
            processXmlStoreNotificationEvent(event);
        }
    }
        
}
