/**
 * 
 */
package alma.scheduling.AlmaScheduling.statusImpl;

import java.util.logging.Logger;

import alma.scheduling.AlmaScheduling.statusIF.AbstractStatusFactory;
import alma.scheduling.AlmaScheduling.statusIF.StatusBaseI;



/**
 * @author dclarke
 *
 */
public abstract class CachedStatusBase implements StatusBaseI {

	/*
	 * ================================================================
	 * Construction and initialisation of instances
	 * ================================================================
	 */
    /* Hide the default constructor from the outside world */
    protected CachedStatusBase() { /* Empty */ }
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
