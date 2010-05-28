package alma.scheduling.psm.web;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

public class ExecutiveController extends GenericForwardComposer {
    private Textbox firstName;
    private Textbox lastName;
    private Label fullName;
 
    //onChange event from firstName component
    public void onChange$firstName(Event event) { 
        fullName.setValue(firstName.getValue()+" "+lastName.getValue());
    }
 
    //onChange event from lastName component
    public void onChange$lastName(Event event) {
        fullName.setValue(firstName.getValue()+" "+lastName.getValue());
    }
}