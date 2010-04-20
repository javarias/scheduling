/**
 * 
 */
package alma.scheduling.acsFacades;

import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.projectlifecycle.StateSystemHelper;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.AlmaScheduling.facades.BarfingStateSystem;
import alma.scheduling.AlmaScheduling.facades.LoggingStateSystem;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionHelper;
import alma.xmlstore.Identifier;
import alma.xmlstore.IdentifierHelper;
import alma.xmlstore.IdentifierOperations;
import alma.xmlstore.OperationalOperations;

/**
 * @author dclarke
 *
 */
public class ACSComponentFactory implements ComponentFactory {

    
    
    /*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** Where to log anything we need to log */
	private Logger logger;
	
    /** Container services for anything we need */
    private ContainerServices containerServices;
    /* end of Fields
     * -------------------------------------------------------------- */

     
     
     /*
 	 * ================================================================
 	 * Construction
 	 * ================================================================
 	 */
	/**
	 * @param containerServices
	 */
	public ACSComponentFactory(ContainerServices containerServices) {
		this.containerServices = containerServices;
		this.logger = containerServices.getLogger();
	}
    /* end of Construction
     * -------------------------------------------------------------- */

     
     
    public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource("ALMAArchive");
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(System.currentTimeMillis()));
            Properties prop = new Properties();
            prop.setProperty(ACSFaultState.ASI_PREFIX_PROPERTY, "prefix");
			prop.setProperty(ACSFaultState.ASI_SUFFIX_PROPERTY, "suffix");
			prop.setProperty("ALMAMasterScheduling_PROPERTY", "ConnArchiveException");
			state.setUserProperties(prop);
            alarmSource.push(state);
        } catch(Exception e) {
        	e.printStackTrace();
        }
        
    }

    
    
    /*
	 * ================================================================
	 * State System
	 * ================================================================
	 */
    /**
     * Wrap the provided ACS component (start) with other
     * implementations of the same interface which provide diagnostic
     * information. Which wrappers to use are determined by the list of
     * ComponentDiagnosticTypes. Return the outermost wrapper. Goes to
     * a bit of trouble to ensure that the wrappers take effect in the
     * order specified.
     * 
     * @param start - the actual component you ultimately want to use
     * @param diags - the list of diagnostic types you want.
     * @return the outermost wrapper created, which you use just as you
     *         would have used <code>start</code>.
     */
    private StateSystemOperations wrapStateSystemComponent(
		    StateSystemOperations start,
		    ComponentDiagnosticTypes... diags) {
    	StateSystemOperations result = start;
    	String tag = " ";
    	try {
    		for (int i = diags.length-1; i >= 0; i--) {
    			final ComponentDiagnosticTypes diag = diags[i];
    			switch(diag) {
    			case LOGGING:
    				result = new LoggingStateSystem(containerServices, result);
    				break;
    			case BARFING:
    				result = new BarfingStateSystem(containerServices, result);
    				break;
    			default:
    				logger.warning(String.format(
    						"Asking for a %s State System component, no implementation of which is known - ignoring",
    						diag));
    				break;
    			}
    			tag = String.format(" %s%s", diag, tag);
    		}
    	} catch(AcsJContainerServicesEx e) {
    		logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
    		sendAlarm("Scheduling","SchedStateSystemConnAlarm",1,ACSFaultState.ACTIVE);
    		result = null;
    	}
    	if (result != null) {
    		logger.fine(String.format(
    				"SCHEDULING: The ALMA%sState Engine has been constructed.",
    				tag));
    	} else {
    		logger.warning(String.format(
    				"SCHEDULING: The ALMA%sState Engine has NOT been constructed.",
    				tag));
    	}
    	return result;
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.acsFacades.ComponentFactory#getDefaultStateSystem(alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes[])
	 */
	@Override
	public StateSystemOperations getDefaultStateSystem(
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx {
       final org.omg.CORBA.Object obj = containerServices.getDefaultComponent(StateSystemIFName);
       return wrapStateSystemComponent(StateSystemHelper.narrow(obj), diags);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.acsFacades.ComponentFactory#getStateSystem(java.lang.String, alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes[])
	 */
	@Override
	public StateSystemOperations getStateSystem(
			String name,
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx {
       final org.omg.CORBA.Object obj = containerServices.getComponent(name);
       return wrapStateSystemComponent(StateSystemHelper.narrow(obj), diags);
	}
   /* end of State System
    * -------------------------------------------------------------- */

    
    
    /*
	 * ================================================================
	 * Archive XMLStore
	 * ================================================================
	 */
    /**
     * Wrap the provided ACS component (start) with other
     * implementations of the same interface which provide diagnostic
     * information. Which wrappers to use are determined by the list of
     * ComponentDiagnosticTypes. Return the outermost wrapper. Goes to
     * a bit of trouble to ensure that the wrappers take effect in the
     * order specified.
     * 
     * @param start - the actual component you ultimately want to use
     * @param diags - the list of diagnostic types you want.
     * @return the outermost wrapper created, which you use just as you
     *         would have used <code>start</code>.
     */
   private OperationalOperations wrapArchiveComponent(
		   OperationalOperations start,
		   ComponentDiagnosticTypes... diags) {
	   OperationalOperations result = start;
	   String tag = " ";
       try {
           for (int i = diags.length-1; i >= 0; i--) {
        	   final ComponentDiagnosticTypes diag = diags[i];
        	   switch(diag) {
        	   case LOGGING:
        		   result = new LoggingOperational(containerServices, result);
        		   break;
        	   default:
        		   logger.warning(String.format(
        				   "Asking for a %s Archive component, no implementation of which is known - ignoring",
        				   diag));
        		   break;
        	   }
        	   tag = String.format(" %s%s", diag, tag);
           }
       } catch(AcsJContainerServicesEx e) {
           logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
           sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
           result = null;
       }
       if (result != null) {
           logger.fine(String.format(
        		   "SCHEDULING: The ALMA%sArchive has been constructed.",
        		   tag));
       } else {
           logger.warning(String.format(
        		   "SCHEDULING: The ALMA%sArchive has NOT been constructed.",
        		   tag));
       }
       return result;
   }

	/* (non-Javadoc)
	 * @see alma.scheduling.acsFacades.ComponentFactory#getDefaultStateSystem(alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes[])
	 */
	@Override
	public OperationalOperations getDefaultArchive(
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx, UserException {
	       final org.omg.CORBA.Object obj = containerServices.getDefaultComponent(ArchiveIFName);
	       final ArchiveConnection con = ArchiveConnectionHelper.narrow(obj);
	       return wrapArchiveComponent(con.getOperational("SCHEDULING"), diags);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.acsFacades.ComponentFactory#getStateSystem(java.lang.String, alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes[])
	 */
	@Override
	public OperationalOperations getArchive(
			String name,
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx, UserException {
       final org.omg.CORBA.Object obj = containerServices.getComponent(name);
       final ArchiveConnection con = ArchiveConnectionHelper.narrow(obj);
       return wrapArchiveComponent(con.getOperational("SCHEDULING"), diags);
	}
   /* end of Archive XMLStore
    * -------------------------------------------------------------- */


    
    
    /*
	 * ================================================================
	 * Archive Identifiers
	 * ================================================================
	 */
    /**
     * Wrap the provided ACS component (start) with other
     * implementations of the same interface which provide diagnostic
     * information. Which wrappers to use are determined by the list of
     * ComponentDiagnosticTypes. Return the outermost wrapper. Goes to
     * a bit of trouble to ensure that the wrappers take effect in the
     * order specified.
     * 
     * @param start - the actual component you ultimately want to use
     * @param diags - the list of diagnostic types you want.
     * @return the outermost wrapper created, which you use just as you
     *         would have used <code>start</code>.
     */
   private IdentifierOperations wrapIdentifierComponent(
		   IdentifierOperations start,
		   ComponentDiagnosticTypes... diags) {
	   IdentifierOperations result = start;
	   String tag = " ";
//       try {
           for (int i = diags.length-1; i >= 0; i--) {
        	   final ComponentDiagnosticTypes diag = diags[i];
        	   switch(diag) {
        	   default:
        		   logger.warning(String.format(
        				   "Asking for a %s Identifier component, no implementation of which is known - ignoring",
        				   diag));
        		   break;
        	   }
        	   tag = String.format(" %s%s", diag, tag);
           }
//       } catch(AcsJContainerServicesEx e) {
//           logger.severe("SCHEDULING: AcsJContainerServicesEx: "+e.toString());
//           sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
//           result = null;
//       }
       if (result != null) {
           logger.fine(String.format(
        		   "SCHEDULING: The ALMA%sIdentifier component has been constructed.",
        		   tag));
       } else {
           logger.warning(String.format(
        		   "SCHEDULING: The ALMA%sIdentifier component has NOT been constructed.",
        		   tag));
       }
       return result;
   }

	/* (non-Javadoc)
	 * @see alma.scheduling.acsFacades.ComponentFactory#getDefaultStateSystem(alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes[])
	 */
	@Override
	public IdentifierOperations getDefaultIdentifier(
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx {
	       final org.omg.CORBA.Object obj = containerServices.getDefaultComponent(IdentifierIFName);
	       return wrapIdentifierComponent(IdentifierHelper.narrow(obj), diags);
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.acsFacades.ComponentFactory#getStateSystem(java.lang.String, alma.scheduling.acsFacades.ComponentFactory.ComponentDiagnosticTypes[])
	 */
	@Override
	public IdentifierOperations getIdentifier(
			String name,
			ComponentDiagnosticTypes... diags)
		throws AcsJContainerServicesEx {
       final org.omg.CORBA.Object obj = containerServices.getComponent(name);
       return wrapIdentifierComponent(IdentifierHelper.narrow(obj), diags);
	}
	/* end of Archive Identifiers
	 * -------------------------------------------------------------- */
}
