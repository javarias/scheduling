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
package alma.scheduling.planning_mode_sim.define.acs.container;

import java.util.logging.Logger;

import alma.scheduling.planning_mode_sim.define.acs.commonentity.EntityT;



/**
 * Through this interface, the container offers services to its hosted components.
 * The component must call these methods explicitly; in this respect, 
 * <code>ContainerServices</code> is different from the other services that the container
 * provides without the component implementation knowing about it. 
 * It can be thought of as a callback handle or a library.
 * <p>
 * Currently, methods are added to this interface as the functionality becomes available. 
 * At some point we will have to declutter the interface by introducing 2nd-level interfaces
 * that harbor cohesive functionality. For example, instead of calling 
 * <code>myContainerServices.getComponent(...)</code>, the new call will then be something like
 * <code>myContainerServices.communication().getComponent(...)</code>.
 * <p>
 * created on Oct 24, 2002 12:56:36 PM
 * @author hsommer
 */
public interface ContainerServices 
{
	/**
	 * Gets a <code>Logger</code> object that the component should use for logging.
	 * <p>
	 * The <code>Logger</code> will be set up with a namespace specific to the component
	 * that uses this <code>ContainerServices</code> instance.  
	 *   
	 * @return Logger
	 */
	public Logger getLogger();
	
	
	/**
	 * Creates a unique id and sets it on the given (binding class) entity object.
	 * The id will be redundantly stored in an encrypted format so that later it can be verified that 
	 * the id is indeed the one originally assigned. 
	 * 
	 * @param entity 	must be freshly created and 
	 */
	public void assignUniqueEntityId(EntityT entity) throws ContainerException;
	
	
	
	/**
	 * Gets the specified component as a Corba object.
	 * 
	 * <ul>
	 * <li> This method is necessarily generic and will require a cast to the requested interface,
	 * like in <code>HelloComponent helloComp = HelloComponentHelper.narrow(
	 * containerServices.getComponent("HELLOCOMP1"));</code>.
	 * 
	 * <li> if requested, we may come up with some additional way (e.g. a code-generated singleton class) 
	 * 	to give type safe access to other components, like done with EJB xxxHome.
	 * 
	 * <li> the Container will later shortcut calls for components inside the same process. 
	 *     Like in EJB and CORBA, the implementation must therefore not assume receiving a 
	 *     by-value copy of any parameter from the other component.
	 * </ul>
	 * 
	 * @param componentUrl  the ACS CURL of the deployed component instance.
	 * @return  the CORBA proxy for the component.
	 * @throws ContainerException  if something goes wrong.
	 */
	public org.omg.CORBA.Object getComponent(String componentUrl) throws ContainerException;
	
	
	
	/**
	 * Finds components by their instance name (curl) and/or by their type.
	 * Wildcards can be used for the curl and type.
	 * This method returns a possibly empty array of component curls; 
	 * for each curl, you may use {@link #getComponent} to obtain the reference.
	 * 
	 * @param nameWildcard (<code>null</code> is understood as "*")
	 * @param typeWildcard (<code>null</code> is understood as "*")
	 * @return the curls of the component(s) that match the search.
	 * @see si.ijs.maci.ManagerOperations#get_COB_info
	 */
	public String[] findComponents(String curlWildcard, String typeWildcard)  throws ContainerException;;


	/**
	 * Releases the specified component. This involves notification of the manager,
	 * as well as calling <code>_release()</code> on the CORBA stub for that component.
	 * If the curl is not known to the container, the request will be ignored.
	 * @param componentUrl  the name/curl of the component instance as used by the manager  
	 */
	public void releaseComponent(String componentUrl);
	
	
	/**
	 * Encapsulates {@link org.omg.CORBA.ORB#object_to_string(org.omg.CORBA.Object)}.
	 * @param objRef the corba stub 
	 * @return standardized string representation of <code>objRef</code>. 
	 */
	public String corbaObjectToString(org.omg.CORBA.Object objRef);

	/**
	 * Encapsulates {@link org.omg.CORBA.ORB#string_to_object(String)}.
	 * @param strObjRef
	 * @return org.omg.CORBA.Object
	 */
	public org.omg.CORBA.Object corbaObjectFromString(String strObjRef);


	/**
	 * Takes a reference to another component and creates a wrapper object that
	 * can translate between entity object parameters as serialized XML 
	 * (in <code>corbaOperationsIF</code>) and the corresponding Java binding classes
	 * (in <code>componentInterface</code>).
	 *     
	 * @param componentInterface  component interface with XML binding classes.
	 * @param componentReference  reference to the component to be wrapped, as 
	 * 								obtained through <code>getComponent(String)</code>, 
	 * 								thus implements <code>corbaOperationsIF</code>. 
	 * @param corbaOperationsIF  component interface where entity objects are represented as \
	 * 								serialized XML inside a CORBA struct.
	 * @return the dynamically created wrapper object that implements 
	 * 			<code>componentInterface</code>.
	 */
	public Object createXmlBindingWrapper(
		Class componentInterface,
		org.omg.CORBA.Object componentReference,
		Class corbaOperationsIF) 
		throws ContainerException;
	
}
