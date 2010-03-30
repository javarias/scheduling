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
 * File CachedStatusBase.java
 * $Id: CachedStatusBase.java,v 1.3 2010/03/30 17:52:08 dclarke Exp $
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.util.logging.Logger;

import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.StatusBaseI;
import alma.scheduling.Define.SchedulingException;

/**
 * Common superclass for <code>CachedProjectStatus</code>,
 * <code>CachedOUSStatus</code> and <code>CachedSBStatus</code>.
 * 
 * @author dclarke
 */
public abstract class CachedStatusBase implements StatusBaseI {

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
	protected static StateSystemOperations stateSystem;
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
			StateSystemOperations stateSystem,
			EntitySerializer      entitySerializer,
			EntityDeserializer    entityDeserializer,
			ALMAClock             clock,
			Logger                logger) {
		CachedStatusBase.stateSystem        = stateSystem;
		CachedStatusBase.entitySerializer   = entitySerializer;
		CachedStatusBase.entityDeserializer = entityDeserializer;
		CachedStatusBase.clock              = clock;
		CachedStatusBase.logger             = logger;
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
	/*
	 * End of Being an abstract status entity
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Construction and initialisation of instances
	 * ================================================================
	 */
    /* Hide the default constructor from the outside world */
    protected CachedStatusBase() { /* Empty */ }
    
    /**
     * Set up a proxy for the status entity with the given uid.
     * @param uid
     */
    protected CachedStatusBase(String uid) {
    	this.uid = uid; 
    }
	/*
	 * End of Construction and initialisation of instances
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
    /**
     * Get a <em>Factory</em> for making instances (for use when
     * resolving entity references).
     * 
     * @return
     */
    protected AbstractStatusFactory getFactory() {
    	return CachedStatusFactory.getInstance();
    }
    
    /**
     * Get a <em>Logger</em>
     * 
     * @return Logger
     */
    protected Logger getLogger() {
    	return getFactory().getLogger();
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.AlmaScheduling.statusIF.StatusBaseI#isSynched()
	 */
	public boolean isSynched() {
		return false;
	}
	/*
	 * End of Utilities
	 * ============================================================= */
}
