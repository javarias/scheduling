/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.scheduling.acsFacades;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.ACS.ACSComponent;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.projectlifecycle.StateSystem;
import alma.projectlifecycle.StateSystemHelper;
import alma.projectlifecycle.StateSystemOperations;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionHelper;
import alma.xmlstore.Identifier;
import alma.xmlstore.IdentifierHelper;
import alma.xmlstore.IdentifierOperations;
import alma.xmlstore.Operational;
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
	
    /** Component handles we have, for later tidying */
    private List<ACSComponent> components;
    /* end of Fields
     * -------------------------------------------------------------- */

     
     
     /*
 	 * ================================================================
 	 * Construction and Finalisation
 	 * ================================================================
 	 */
	/**
	 * @param containerServices
	 */
	public ACSComponentFactory(ContainerServices containerServices) {
		this.containerServices = containerServices;
		this.logger = containerServices.getLogger();
		components = new Vector<ACSComponent>();
	}
	
	protected void finalize() throws Throwable {
		tidyUp();
	}
    /* end of Construction and Finalisation
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
    			case PROFILING:
    				result = new ProfilingStateSystem(containerServices, result);
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
		final StateSystem con = StateSystemHelper.narrow(obj);
		StateSystemOperations result = null;

		if (con != null) {
			components.add(con);
			result = wrapStateSystemComponent(con, diags);
		} else {
			logger.warning("SCHEDULING: Cannot find default ALMA State System component.");
		}
		return result;
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
		final StateSystem con = StateSystemHelper.narrow(obj);
		StateSystemOperations result = null;

		if (con != null) {
			components.add(con);
			result = wrapStateSystemComponent(con, diags);
		} else {
			logger.warning(String.format(
					"SCHEDULING: Cannot find ALMA State System component called %s.",
					name));
		}
		return result;
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
        	   case PROFILING:
        		   result = new ProfilingOperational(containerServices, result);
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
	       OperationalOperations result = null;
	       
	       if (con != null) {
	    	   final Operational ops = con.getOperational("SCHEDULING");
	    	   if (ops != null) {
	               result = wrapArchiveComponent(ops, diags);
	    	   } else {
	    		   logger.warning("SCHEDULING: Cannot get Operational offshoot from default ALMA Archive.");
	    	   }
	    	   components.add(con);
	       } else {
	    	   logger.warning("SCHEDULING: Cannot find default ALMA Archive component.");
	       }

	       return result;
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
       OperationalOperations result = null;
       
       if (con != null) {
    	   final Operational ops = con.getOperational("SCHEDULING");
    	   if (ops != null) {
               result = wrapArchiveComponent(ops, diags);
    	   } else {
    		   logger.warning(String.format(
    				   "SCHEDULING: Cannot get Operational offshoot from ALMA Archive component called %s.",
    				   name));
    	   }
    	   components.add(con);
       } else {
    	   logger.warning(String.format(
    			   "SCHEDULING: Cannot find ALMA Archive component called %s.",
    			   name));
       }

       return result;
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
		final Identifier con = IdentifierHelper.narrow(obj);
		IdentifierOperations result = null;
		
		if (con != null) {
			components.add(con);
			result = wrapIdentifierComponent(con, diags);
		} else {
			logger.warning("SCHEDULING: Cannot find default ALMA Identifier component.");
		}
		return result;
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
		final Identifier con = IdentifierHelper.narrow(obj);
		IdentifierOperations result = null;

		if (con != null) {
			components.add(con);
			result = wrapIdentifierComponent(con, diags);
		} else {
			logger.warning(String.format(
					"SCHEDULING: Cannot find ALMA Identifier component called %s.",
					name));
		}
		return result;
	}
	/* end of Archive Identifiers
	 * -------------------------------------------------------------- */


    
    
    /*
	 * ================================================================
	 * Tidying up
	 * ================================================================
	 */
	@Override
	public void tidyUp() {
		if (components != null) {
			for (ACSComponent comp : components) {
				containerServices.releaseComponent(comp.name());
			}
			components = null;
		}
	}
	/* end of Tidying up
	 * -------------------------------------------------------------- */
}
