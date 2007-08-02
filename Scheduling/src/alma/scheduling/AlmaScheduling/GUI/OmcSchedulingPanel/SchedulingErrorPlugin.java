package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.*;
import javax.swing.JPanel;
import javax.swing.JLabel;

/**
  * A very simple plugin which is launched from the invisible scheduler
  * starter plugin when someone tries to open a scheduler on a tab which
  * has no array associated to it. However this is a generic plugin so it
  * can be used with any error message.
  *
  * @author sslucero
  */
public class SchedulingErrorPlugin extends JPanel implements SubsystemPlugin{

    private PluginContainerServices container;
    private Logger logger;
    private String error;

    public SchedulingErrorPlugin(String e){
        super();
        error = e;
    }
    public SchedulingErrorPlugin(String e, PluginContainerServices cs){
        this(e);
        container = cs;
        logger = cs.getLogger();
    }

    public void setServices(PluginContainerServices cs){
        container = cs;
        logger = cs.getLogger();
    }
    
    public void start(){
        displayError();
    }
    
    public void stop(){
    }
    
    public boolean runRestricted(boolean b){
        return b;
    }
    
    private void displayError(){
        JLabel l = new JLabel(error);
        add(l);
        validate();
    }
}
