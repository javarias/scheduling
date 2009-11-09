/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2009
 * (c) Associated Universities Inc., 2009
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File ALMAStatusEntity.java
 * $Id: RemoteStatusBase.java,v 1.2 2009/11/09 22:58:45 rhiriart Exp $
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Logger;

import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.projectlifecycle.StateSystem;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.StatusBaseI;
import alma.scheduling.Define.SchedulingException;

/**
 * Common superclass for <code>ALMAProjectStatus</code>,
 * <code>ALMAOUSStatus</code> and <code>ALMASBStatus</code>.
 * 
 * @author dclarke
 */
public abstract class RemoteStatusBase implements StatusBaseI {

	/*
	 * ================================================================
	 * The State System connection
	 * ================================================================
	 */
	/*
	 * Fields and methods associated with accessing the State System -
	 * not just the State System itself but also serialization and
	 * deserialization objects, a clock and, of course, a logger.
	 * There's only one StateSystem, so these are static and thus
	 * common between all instances of all sub-classes. They should
	 * all be set up using <code>setStatusSystem()</code>.
	 */
	/** The common connection to the State System */
	protected static StateSystem stateSystem;
	/** The common entity deserializer */
	protected static EntityDeserializer entityDeserializer;
	/** The common entity serializer */
	protected static EntitySerializer entitySerializer;
	/** The common clock */
    protected static ALMAClock clock;
	/** The common logger */
    protected static Logger logger;
	
	/**
	 * Tell the various status entity proxies which resources to use
	 * for their various activites.
	 * 
	 * @param stateSystem
	 * @param entitySerializer
	 * @param entityDeserializer
	 * @param clock
	 * @param logger
	 */
	protected static void setStatusSystem(
			StateSystem        stateSystem,
			EntitySerializer   entitySerializer,
			EntityDeserializer entityDeserializer,
			ALMAClock          clock,
			Logger             logger) {
		RemoteStatusBase.stateSystem        = stateSystem;
		RemoteStatusBase.entitySerializer   = entitySerializer;
		RemoteStatusBase.entityDeserializer = entityDeserializer;
		RemoteStatusBase.clock              = clock;
		RemoteStatusBase.logger             = logger;
	}
	/*
	 * End of The State System connection
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Being an abstract status entity
	 * ================================================================
	 */
	/** The entity id of the object being proxied */
	protected String uid;
	
	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.CachedStatusBase#getUID()
	 */
	public String getUID() {
		return uid;
	}
	
    /**
     * Check that the proxied entity can be got from the State System.
     * Throw an appropriate exception if it cannot be.
     * @throws SchedulingException 
     */
    protected abstract void checkEntity() throws SchedulingException;
	
    /**
     * Get (and cache) the proxied entity from the State System
     * @return <code>&lt;? extends StatusBaseT&gt;</code> -
     *         implementations should extend the return type to
     *         particular concrete status entities.
     */
    protected abstract StatusBaseT getEntity();
    
    /**
     * Write the modified entity back to the State System
     */
    protected abstract void putEntity();
	/*
	 * End of Being an abstract status entity
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction and initialisation of instances
	 * ================================================================
	 */
    /* Hide the default constructor */
    private RemoteStatusBase() { /* Empty */ }
    
    /**
     * Set up a proxy for the status entity with the given uid.
     * @param uid
     */
    protected RemoteStatusBase(String uid) {
    	this.uid = uid; 
    }
    
	/*
	 * End of Construction and initialisation of instances
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Conversion between local cached and remote versions
	 * ================================================================
	 */
    /* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.CachedStatusBase#asLocal()
	 */
    public abstract StatusBaseI asLocal();
    
    /* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.status.CachedStatusBase#asRemote()
	 */
    public abstract StatusBaseI asRemote();

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#isSynched()
	 */
	public boolean isSynched() {
		return true;
	}
	/*
	 * End of Conversion between local cached and remote versions
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
    /**
     * Send an alarm whingeing about problems with the archive.
     * @param ff
     * @param fm
     * @param fc
     * @param fs
     */
    protected void sendArchiveAlarm(String ff, String fm, int fc, String fs) {
        try {
            final ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource("ALMAArchive");
            final ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
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
    
    /**
     * Get a <em>Factory</em> for making instances (for use when
     * resolving entity references).
     * 
     * @return
     */
    protected AbstractStatusFactory getFactory() {
    	return RemoteStatusFactory.getInstance();
    }
    
	/*
	 * End of Utilities
	 * ============================================================= */
}
