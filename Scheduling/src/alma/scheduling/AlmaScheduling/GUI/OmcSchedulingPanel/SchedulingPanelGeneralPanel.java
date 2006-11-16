package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import javax.swing.*;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class SchedulingPanelGeneralPanel extends JPanel{
    protected PluginContainerServices container;
    protected Logger logger;

    public SchedulingPanelGeneralPanel(){
        super();
    }
    public void onlineSetup(PluginContainerServices cs){
        container = cs;
        logger = cs.getLogger();
    }
}
