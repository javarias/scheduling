/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */

package alma.scheduling.planning_mode_sim.define;

/**
 * The ComponentLifecycle interface defines methods that are related
 * to the lifecycle of a component.
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public interface ComponentLifecycle
{

    /**
     * Sets the name that is assigned at deployment time or dynamically
     * by the framework.
     */
    public void setComponentName(String name);


    /**
     * Sets a callback handle to the container.
     * 
     * @param containerServices	the interface of the container offering
     * 								services to the component.
     */
    public void setContainerServices(ContainerServices containerServices);


    /**
     * Called to give the component time to initialize itself. 
     * For instance, the component could retrieve connections, read in 
     * configuration files/parameters, build up in-memory tables, ...
     * <p>
     * Called after {@link ComponentLifecycle#setContainerServices} but before 
     * {@link ComponentLifecycle#execute}.
     * In fact, this method might be called quite some time before functional requests 
     * can be sent to the component.
     * <p>
     * Must be implemented as a synchronous (blocking) call.
     * <p>
     * Note: for the more advanced feature of on-the-fly reinitialization (e.g. to read in 
     * modified config parameters from the CDB, we could either have a separate 
     * method like <code>reinitialize</code>, or just call <code>initialize</code> 
     * again on the running component.
     * While implementing this method, please think about which of these alternatives 
     * would be better for you so that you can give qualified feedback later.
     */
    public void initialize() throws SimulationException;


    /**
     * Called after {@link ComponentLifecycle#initialize} to tell the component that it has to be ready to accept 
     * incoming functional calls any time. 
     * <p>
     * Examples:
     * <ul>
     * <li>last-minute initializations for which <code>initialize</code> seemed too early
     * <li>component could start actions which aren't triggered by any functional call, 
     * 	 e.g. the Scheduler could start to rank SBs in a separate thread.
     * </ul>
     * <p>
     * Must be implemented as a synchronous (blocking) call (can spawn threads though).
     */
    public void execute() throws SimulationException; 


    /**
     * Called after the last functional call to the component has finished.
     * The component should then orderly release resources etc.
     */
    public void cleanUp();


    /**
     * Called when due to some error condition the component is about to be forcefully removed
     * some unknown amount of time later (usually not very much...).
     * <p>
     * The component should make an effort to die as neatly as possible.
     * <p>
     * Because of its urgency, this method will be called asynchronously to the execution of 
     * any other method of the component.
     */
    public void aboutToAbort();


}
