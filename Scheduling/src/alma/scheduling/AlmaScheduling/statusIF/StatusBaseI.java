package alma.scheduling.AlmaScheduling.statusIF;


public interface StatusBaseI {

	/*
	 * ================================================================
	 * Utilities
	 * ================================================================
	 */
	/**
	 * @return the EntityId of the thing for which we are a proxy.
	 */
	public String getUID();
	
	/**
	 * @return the EntityId of the domain object for which the thing
	 *         for which we are a proxy is the status.
	 */
	public String getDomainEntityId();
	/*
	 * End of Utilities
	 * ============================================================= */

	
	
	/*
	 * ================================================================
	 * Conversion between local cached and remote versions
	 * ================================================================
	 */
	/**
	 * Return a local cache of this status object which will not synch
	 * with the remote store.
	 * @return StatusBaseI
	 */
	public StatusBaseI asLocal();

	/**
	 * Return status object which synchs with the remote store
	 * @return StatusBaseI
	 */
	public StatusBaseI asRemote();
	
	/**
	 * Does this proxy keep itself synchronised with the State Archive.
	 * 
	 * @return <code>true</true> if it does and <code>false</code> if
	 *         it doesn't.
	 */
	public boolean isSynched();
	/*
	 * End of Conversion between local cached and remote versions
	 * ============================================================= */
}